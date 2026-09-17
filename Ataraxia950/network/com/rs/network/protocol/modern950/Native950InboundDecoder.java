package com.rs.network.protocol.modern950;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.IntSupplier;

/**
 * Incremental native 950 framing. Construct one instance per connection after login.
 * The supplier consumes that connection's incoming ISAAC stream. Never share it with
 * outbound traffic. Feed preserves cipher and framing state across arbitrary TCP splits.
 * Unnamed, size-verified packets are returned for diagnostics, never dispatched as 910.
 */
public final class Native950InboundDecoder {
    private final IntSupplier cipher;
    private int opcode = -1;
    private int lengthBytes;
    private int length;
    private int bodyOffset;
    private byte[] body;
    private boolean failed;

    public Native950InboundDecoder(IntSupplier cipher) {
        this.cipher = Objects.requireNonNull(cipher, "incoming ISAAC supplier");
    }

    public synchronized List<Frame> feed(byte[] bytes) {
        Objects.requireNonNull(bytes, "bytes");
        if (failed) throw new IllegalStateException("Decoder failed; close this connection");
        List<Frame> frames = new ArrayList<Frame>();
        try {
            for (byte wireByte : bytes) {
                int value = wireByte & 255;
                if (opcode < 0) {
                    // 950 opcodes are a single byte, 0..128 inclusive. The 947 two-byte escape at
                    // >= 128 is gone: opcode 128 is a real 950 packet and the escape would swallow
                    // it. The live opennxt framing agrees, short-circuiting its own >= 128 compat
                    // heuristic whenever the build is 950.
                    //
                    // The client's own descriptor table settles this rather than leaving it to
                    // inference: tools/reg950.py recovers every client registration stub out of
                    // .text, and the highest opcode any of them registers is 128. A client cannot
                    // send an opcode it holds no descriptor for, so there is nothing above 128 for
                    // an escape to encode, and 128 itself is an ordinary five-byte packet.
                    opcode = (value - cipher.getAsInt()) & 255;
                    int size = Native950Protocol.clientSize(opcode);
                    if (size == Native950Protocol.UNKNOWN_SIZE)
                        throw new IllegalArgumentException("Unknown native 950 client opcode " + opcode);
                    length = Math.max(0, size);
                    lengthBytes = Math.max(0, -size);
                    if (lengthBytes == 0) {
                        body = new byte[length];
                        if (length == 0) emit(frames);
                    }
                } else if (lengthBytes > 0) {
                    length = (length << 8) | value;
                    if (--lengthBytes == 0) {
                        // No cap beyond the length field's own width. An earlier revision of this
                        // decoder capped -1 at 0x104 and -2 at 0x2710 "the client's own caps"; both
                        // were wrong. 0x104 is unreachable - a one-byte length cannot exceed 255 -
                        // and 0x2710 is not a length at all: the only occurrence of that immediate
                        // in the 950 parser range is 0x140149759 `add rax, 0x2710` on a millisecond
                        // clock, a 10-second timer. Capping a two-byte length at 10000 would have
                        // silently closed the connection on any longer packet the client does send.
                        // The field width already bounds the allocation at 64KB, which is not a
                        // hazard, and a desynced stream is caught by the unknown-opcode check above.
                        body = new byte[length];
                        if (length == 0) emit(frames);
                    }
                } else {
                    body[bodyOffset++] = wireByte;
                    if (bodyOffset == body.length) emit(frames);
                }
            }
        } catch (RuntimeException error) {
            failed = true;
            throw error;
        }
        return frames;
    }

    private void emit(List<Frame> frames) {
        frames.add(new Frame(opcode, body));
        opcode = -1;
        length = lengthBytes = bodyOffset = 0;
        body = null;
    }

    public static final class Frame {
        private final int opcode;
        private final byte[] payload;
        private Frame(int opcode, byte[] payload) { this.opcode = opcode; this.payload = payload; }
        public int opcode() { return opcode; }
        public int length() { return payload.length; }
        public byte[] payload() { return payload.clone(); }
    }
}
