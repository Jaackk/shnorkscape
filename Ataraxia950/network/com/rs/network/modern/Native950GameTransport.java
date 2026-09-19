package com.rs.network.modern;

import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Actions.Action;
import com.rs.network.protocol.modern950.Native950Actions.WalkRequest;
import com.rs.network.protocol.modern950.Native950InboundDecoder;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;

import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

/**
 * Authenticated native 950 game traffic only. Install after the modern login
 * exchange has established the two independent ISAAC streams. This handler
 * does not authenticate an account or initialize a legacy Player.
 *
 * Inbound bytes become bounded semantic actions. The named game thread must
 * call drainActions exactly once each tick and validate collision, range,
 * account ownership and movement policy before applying them. No gameplay runs
 * on the Netty event loop. Write Native950Packets.Packet through the channel;
 * framing and outgoing ISAAC consumption then occur on its owning event loop.
 *
 * Overflow policy, by lane:
 *   - Keepalive (NO_TIMEOUT, client opcode 27): counted, never queued. A keepalive
 *     burst cannot fill the queue, cost a tick budget, or count as unhandled.
 *   - Chat (MESSAGE_PUBLIC 69, MESSAGE_PRIVATE 68): queued in order with everything
 *     else, but lossy under pressure. Queued chat is what gives way, whatever the
 *     arriving frame is: any action that finds the queue full evicts the oldest queued
 *     chat action rather than failing the connection, so a chat burst can never cost
 *     the player the next click. With no chat in the queue an arriving chat frame is
 *     itself dropped. A chat frame that cannot be decoded because no huffman table is
 *     installed is also dropped rather than treated as malformed. Every such drop is
 *     counted in chatFramesDropped.
 *   - Everything else: unchanged. A full queue that holds no chat at all, or a malformed
 *     frame of an implemented opcode, still closes the connection.
 */
public final class Native950GameTransport extends ChannelDuplexHandler {
    public static final int DEFAULT_QUEUE_CAPACITY = 64;
    public static final int DEFAULT_ACTIONS_PER_TICK = 16;
    private static final int READ_SLICE_BYTES = 8192;

    private final Native950InboundDecoder decoder;
    private final IntSupplier outgoingCipher;
    private final Thread gameThread;
    private final ArrayBlockingQueue<Action> actions;
    private final int actionsPerTick;
    private final AtomicLong unhandledFrames = new AtomicLong();
    private final AtomicLong keepAliveFrames = new AtomicLong();
    private final AtomicLong chatFramesDropped = new AtomicLong();
    private final java.util.Set<Integer> observedUnhandledOpcodes = new java.util.HashSet<>();
    private volatile int lastUnhandledOpcode = -1;
    /** Installed only by the owning native session for opt-in diagnostics. */
    private volatile Consumer<UnhandledFrame> unhandledFrameObserver;
    /**
     * Installed only while an explicitly enabled diagnostic needs packet-family counts.
     * It observes already-framed game traffic and must never affect decoding or routing.
     */
    private volatile Consumer<InboundFrame> inboundFrameObserver;
    private volatile Throwable terminalFailure;
    private volatile ChannelHandlerContext context;

    /**
     * Constructs fresh game ciphers from the four already-authenticated login
     * seed words. If login consumed either stream, supply those live streams to
     * the constructor instead. The caller's seed array is never modified.
     */
    public static Native950GameTransport afterAuthentication(int[] loginSeeds, Thread gameThread) {
        Objects.requireNonNull(loginSeeds, "loginSeeds");
        if (loginSeeds.length != 4) {
            throw new IllegalArgumentException("Native 950 login requires four ISAAC seed words");
        }
        int[] outgoingSeeds = loginSeeds.clone();
        for (int i = 0; i < outgoingSeeds.length; i++) {
            outgoingSeeds[i] += 50;
        }
        return new Native950GameTransport(new Native950Isaac(loginSeeds), new Native950Isaac(outgoingSeeds), gameThread);
    }

    public Native950GameTransport(IntSupplier incomingCipher, IntSupplier outgoingCipher, Thread gameThread) {
        this(incomingCipher, outgoingCipher, gameThread, DEFAULT_QUEUE_CAPACITY, DEFAULT_ACTIONS_PER_TICK);
    }

    public Native950GameTransport(IntSupplier incomingCipher, IntSupplier outgoingCipher, Thread gameThread,
                                  int queueCapacity, int actionsPerTick) {
        if (queueCapacity < 1 || actionsPerTick < 1 || actionsPerTick > queueCapacity) {
            throw new IllegalArgumentException("Require 1 <= actionsPerTick <= queueCapacity");
        }
        this.decoder = new Native950InboundDecoder(Objects.requireNonNull(incomingCipher, "incomingCipher"));
        this.outgoingCipher = Objects.requireNonNull(outgoingCipher, "outgoingCipher");
        this.gameThread = Objects.requireNonNull(gameThread, "gameThread");
        this.actions = new ArrayBlockingQueue<Action>(queueCapacity);
        this.actionsPerTick = actionsPerTick;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        context = ctx;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) {
        try {
            if (!(message instanceof ByteBuf)) {
                throw new IllegalArgumentException("Native 950 game input must be a ByteBuf");
            }
            if (terminalFailure != null || !ctx.channel().isActive()) {
                return;
            }
            ByteBuf input = (ByteBuf) message;
            while (input.isReadable()) {
                byte[] bytes = new byte[Math.min(input.readableBytes(), READ_SLICE_BYTES)];
                input.readBytes(bytes);
                for (Native950InboundDecoder.Frame frame : decoder.feed(bytes)) {
                    Consumer<InboundFrame> inboundObserver = inboundFrameObserver;
                    if (inboundObserver != null) inboundObserver.accept(new InboundFrame(frame.opcode(), frame.payload()));
                    Action action = Native950Actions.decode(frame);
                    if (action instanceof Native950Actions.KeepAliveAction) {
                        // Telemetry lane: NO_TIMEOUT is a timer-driven liveness signal with no
                        // request in it. It never enters the action queue, so a keepalive burst
                        // can neither overflow the queue nor consume a tick budget.
                        keepAliveFrames.incrementAndGet();
                    } else if (action != null) {
                        if (!actions.offer(action) && !evictOldestChatFor(action)) {
                            throw new IllegalStateException("Native 950 action queue capacity exceeded");
                        }
                    } else if (Native950Actions.isChatOpcode(frame.opcode())
                            && !Native950Actions.isHuffmanInstalled()) {
                        // Fail closed rather than guess the huffman table: the frame is
                        // well-framed but undecodable here, so count it and stay connected.
                        chatFramesDropped.incrementAndGet();
                    } else if (Native950Actions.isImplemented(frame.opcode())) {
                        throw new IllegalArgumentException("Malformed native 950 action for opcode " + frame.opcode());
                    } else {
                        // Framing is verified, semantics are not. Never forward it
                        // to the legacy PacketRepository or infer a gameplay action.
                        lastUnhandledOpcode = frame.opcode();
                        unhandledFrames.incrementAndGet();
                        Consumer<UnhandledFrame> observer = unhandledFrameObserver;
                        if (observer != null) observer.accept(new UnhandledFrame(frame.opcode(), frame.payload()));
                        if (observedUnhandledOpcodes.add(frame.opcode()))
                            System.out.println("[Ataraxia950] Unhandled native input opcode=" + frame.opcode()
                                    + " bytes=" + frame.payload().length);
                    }
                }
            }
        } catch (RuntimeException failure) {
            fail(ctx, failure);
        } finally {
            ReferenceCountUtil.release(message);
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object message, ChannelPromise promise) {
        if (!(message instanceof Native950Packets.Packet)) {
            ReferenceCountUtil.release(message);
            promise.setFailure(new IllegalArgumentException("Write a verified Native950Packets.Packet; raw legacy output is forbidden"));
            return;
        }
        try {
            // Netty invokes outbound handlers on their owning event executor.
            byte[] framed = ((Native950Packets.Packet) message).frame(outgoingCipher);
            ctx.write(Unpooled.wrappedBuffer(framed), promise);
        } catch (RuntimeException failure) {
            promise.tryFailure(failure);
            fail(ctx, failure);
        }
    }

    /** Called once per world tick by the thread supplied at construction. */
    public int drainActions(Consumer<Action> apply) {
        Objects.requireNonNull(apply, "apply");
        if (!canDrain()) return 0;
        int drained = 0;
        Action action;
        while (drained < actionsPerTick && (action = actions.poll()) != null) {
            apply.accept(action);
            drained++;
        }
        return drained;
    }

    /**
     * Compatibility for walk-only consumers. Stops at another action so it
     * cannot reorder a bank click and a following walk or discard that click.
     * Interactive worlds must call drainActions instead, once per tick.
     */
    public int drainWalkRequests(Consumer<WalkRequest> apply) {
        Objects.requireNonNull(apply, "apply");
        if (!canDrain()) return 0;
        int drained = 0;
        while (drained < actionsPerTick && actions.peek() instanceof WalkRequest) {
            // The configured world thread is the queue's only consumer.
            WalkRequest walk = (WalkRequest) actions.poll();
            if (walk == null) break;
            apply.accept(walk);
            drained++;
        }
        return drained;
    }

    /**
     * Second, lossy lane for chat-class traffic. MESSAGE_PUBLIC and MESSAGE_PRIVATE take
     * the ordinary queue so a normal conversation keeps its order against clicks and walks,
     * but a chat flood must never cost the player their connection. Eviction therefore
     * depends on what is QUEUED, not on what is arriving: whenever the queue is full, the
     * oldest queued chat action is evicted to make room for the arriving action, whether
     * that action is a walk, a click or more chat. Only when the queue holds no chat at all
     * does the arriving frame decide the outcome: an arriving chat action is dropped, and an
     * arriving gameplay action fails the connection exactly as before. Every drop, including
     * one made to admit a gameplay action, is counted in chatFramesDropped.
     *
     * @return true when the arriving action was queued or was itself a dropped chat frame;
     *         false when the caller must fail the connection.
     */
    private boolean evictOldestChatFor(Action arriving) {
        Action oldest = null;
        for (Action queued : actions) {
            if (isChatAction(queued)) { oldest = queued; break; }
        }
        // Identity removal: Action has no equals, and this handler is the only producer.
        if (oldest != null && actions.remove(oldest)) {
            chatFramesDropped.incrementAndGet();
            if (actions.offer(arriving)) return true;
        } else if (actions.offer(arriving)) {
            // The world thread drained between the failed offer and here.
            return true;
        }
        if (isChatAction(arriving)) {
            chatFramesDropped.incrementAndGet();
            return true;
        }
        return false;
    }

    private static boolean isChatAction(Action action) {
        return action instanceof Native950Actions.PublicChatAction
                || action instanceof Native950Actions.PrivateChatAction;
    }

    private boolean canDrain() {
        if (Thread.currentThread() != gameThread) {
            throw new IllegalStateException("Native 950 actions may only be applied on the configured game thread");
        }
        ChannelHandlerContext ctx = context;
        if (terminalFailure != null || ctx == null || !ctx.channel().isActive()) {
            actions.clear();
            return false;
        }
        return true;
    }

    public int pendingActions() { return actions.size(); }
    public int pendingWalkRequests() {
        int count = 0;
        for (Action action : actions) if (action instanceof WalkRequest) count++;
        return count;
    }
    public long unhandledFrameCount() { return unhandledFrames.get(); }
    public int lastUnhandledOpcode() { return lastUnhandledOpcode; }
    /**
     * Receives only complete, framed packets whose opcode has deliberately not been given
     * gameplay semantics. The observer runs on Netty's event loop and must return quickly.
     */
    public void setUnhandledFrameObserver(Consumer<UnhandledFrame> observer) { unhandledFrameObserver = observer; }
    /** Enables or removes the opt-in complete-frame observer. */
    public void setInboundFrameObserver(Consumer<InboundFrame> observer) { inboundFrameObserver = observer; }
    /** NO_TIMEOUT frames observed. They are liveness only and never become actions. */
    public long keepAliveFrameCount() { return keepAliveFrames.get(); }
    /**
     * Chat frames discarded by the drop lane: evicted to admit any action under queue
     * pressure, dropped on arrival with no chat left to evict, or undecodable because no
     * huffman table is installed.
     */
    public long chatFramesDropped() { return chatFramesDropped.get(); }
    public Throwable terminalFailure() { return terminalFailure; }

    /** Immutable bounded-diagnostic value. Payload ownership remains with this value. */
    public static final class UnhandledFrame {
        private final int opcode;
        private final byte[] payload;
        UnhandledFrame(int opcode, byte[] payload) {
            this.opcode = opcode;
            this.payload = payload == null ? new byte[0] : payload.clone();
        }
        public int opcode() { return opcode; }
        public byte[] payload() { return payload.clone(); }
    }

    /** Immutable diagnostic-only view of an already framed inbound game packet. */
    public static final class InboundFrame {
        private final int opcode;
        private final byte[] payload;
        InboundFrame(int opcode, byte[] payload) {
            this.opcode = opcode;
            this.payload = payload == null ? new byte[0] : payload.clone();
        }
        public int opcode() { return opcode; }
        public byte[] payload() { return payload.clone(); }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        actions.clear();
        super.channelInactive(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        context = null;
        unhandledFrameObserver = null;
        inboundFrameObserver = null;
        actions.clear();
    }

    private void fail(ChannelHandlerContext ctx, RuntimeException failure) {
        terminalFailure = failure;
        actions.clear();
        ctx.close();
        ctx.fireExceptionCaught(failure);
    }
}
