package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.game.World;
import com.rs.network.modern.Native950Isaac;
import com.rs.network.protocol.modern950.Native950Protocol.ServerPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.ReferenceCountUtil;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/** Opt-in paired-cache dialogue/amount wire probe; saves only to a fresh isolated build directory. */
public final class Native950DialogueInputSmoke {
    private static final String PROFILE = "dialogue947";
    public static void main(String[] args) throws Exception {
        if (args.length != 1)
            throw new IllegalArgumentException("Usage: Native950DialogueInputSmoke <flat-cache-directory>");
        Cache.initFlatReadOnly(Paths.get(args[0]));
        Native950InteractionsSmoke.captureLegacyDispatchBaseline();
        Path build = Paths.get("build").toAbsolutePath().normalize();
        Files.createDirectories(build);
        Path directory = Files.createTempDirectory(build, "dialogue947-smoke-");
        Native950SaveStore saves = new Native950SaveStore(directory.resolve("profiles"));
        Native950World world = Native950World.getInstance();
        try (Connection c = new Connection(world, saves)) {
            Native950Session.Snapshot state = c.await(s -> s.npc != null && s.npc.visible,
                    "banker published to the disposable character");
            int index = state.npc.index;
            c.npc(3, index);
            require(c.dialogue() == 1184, "Talk-to must render the existing Banker's greeting");
            c.reply(1184, 11); // The continue graphic is not the interactive component.
            require(c.dialogue() == 1184, "Wrong dialogue component cannot advance greeting");
            c.reply(1184, 15);
            require(c.dialogue() == 1188, "Greeting continue must show bank/cancel choices");
            c.reply(1184, 15); // A duplicate greeting cannot choose from the new page.
            c.reply(1188, 18); // The legacy third service is deliberately absent.
            require(c.dialogue() == 1188 && !c.state().interactions.bankOpen,
                    "Stale greeting and invisible option cannot open a service");
            c.reply(1188, 13);
            require(c.dialogue() == -1 && !c.managerActive(), "Never mind must retire manager and presentation");
            c.reply(1188, 8);
            require(!c.state().interactions.bankOpen, "A choice arriving after cancellation cannot open bank");

            c.npc(3, index);
            c.reply(1184, 15);
            c.reply(1188, 8);
            state = c.await(s -> s.interactions.bankOpen, "Banker dialogue opens the real bank");
            require(c.dialogue() == -1 && !c.managerActive(), "Bank choice must finish the manager");
            System.out.println("PASS: encrypted Talk-to and opcode15 traverse the 910 Banker; wrong, stale and hidden replies rejected");

            long transactions = state.interactions.transactions;
            // Deposit-All's inventory actor has already become -1 before native sending.
            c.button(7, 15, slot(state.interactions.inventory, 995), -1);
            state = c.await(s -> total(s.interactions.bank, 995) == 1000, "deposit the actual starter coins");
            require(state.interactions.transactions == transactions + 1, "One actual deposit expected");
            transactions = state.interactions.transactions;
            c.beginAmount();
            c.count(7);
            state = c.state();
            require(!c.quantityActive(), "Count reply must consume ownership");
            require(state.interactions.transactions == transactions + 1
                            && total(state.interactions.inventory, 995) == 7
                            && total(state.interactions.bank, 995) == 993,
                    "Withdraw-X must move seven actual coins through the 910 bank");
            require(state.checkpoints > 0, "The real session must checkpoint the withdrawal");
            Native950Save saved = saves.load(PROFILE);
            require(saved != null && total(saved.inventoryIds(), saved.inventoryAmounts(), 995) == 7
                            && total(saved.bankIds(), saved.bankAmounts(), 995) == 993,
                    "The actual session checkpoint must persist Withdraw-X as seven carried and 993 banked coins");
            transactions = state.interactions.transactions;
            c.count(7);
            require(c.state().interactions.transactions == transactions, "Duplicate count must not withdraw twice");

            for (long invalid : new long[] {0, -1, 2147483648L, Long.MAX_VALUE}) {
                c.beginAmount();
                c.count(invalid);
                require(!c.quantityActive(), "Invalid signed count must still retire its request");
                require(c.state().interactions.transactions == transactions, "Invalid count must not mutate containers: " + invalid);
            }
            System.out.println("PASS: Withdraw-X transfers once; zero, negative, overflow and duplicate counts cannot transfer");

            c.beginAmount();
            c.send(41, new byte[0]); // Cache 112 -> 1750: Escape from the quantity entry.
            require(!c.quantityActive() && c.state().interactions.bankOpen,
                    "Quantity Escape must close its own frame and preserve banking");
            c.count(9);
            require(c.state().interactions.transactions == transactions, "Late amount after Escape is rejected");

            c.beginAmount();
            c.send(55, new byte[0]);
            require(!c.quantityActive() && !c.state().interactions.bankOpen,
                    "CLOSE_MODAL must retire both bank and amount ownership");
            c.count(9);
            require(c.state().interactions.transactions == transactions, "Late amount after bank close is rejected");
            c.npc(1, index);
            c.await(s -> s.interactions.bankOpen, "reopen banker directly");
            c.count(9);
            require(c.state().interactions.transactions == transactions,
                    "Reopened bank cannot restore an old amount request");

            c.beginAmount();
            state = c.state();
            c.walk(state.x, state.y); // Even a same-tile walk request retires the interaction context.
            require(!c.quantityActive() && !c.state().interactions.bankOpen,
                    "Walking must retire amount and bank ownership");
            c.count(9);
            state = c.state();
            require(state.interactions.transactions == transactions, "Late amount after walking is rejected");
            assertTotals(state);
            require(state.interactions.unhandledActions == 0, "All probe inputs require handlers: " + state.interactions.unhandledActionReport);
            require(state.unhandledFrames == 0 && state.tickFailures == 0 && state.facadeStrictHits == 0,
                    "The dialogue/input path must keep the native session healthy: " + state);
            System.out.println("PASS: input-only Escape preserves bank; modal close, reopen and walking invalidate late counts; coins conserved");

            c.npc(1, index);
            c.await(s -> s.interactions.bankOpen, "bank before disconnect cleanup");
            c.beginAmount();
            c.disconnect();
            require(!c.quantityActive(), "Session disconnect must release its pending amount request");
            require(!c.managerActive(), "Session disconnect must release its dialogue manager");
            System.out.println("PASS: pending request retired on disconnect; withdrawal checkpoint saved to isolated profile");
        }
        try (Connection c = new Connection(world, saves)) {
            Native950Session.Snapshot restored = c.await(s -> s.npc != null && s.npc.visible,
                    "fresh banker view after restoring isolated profile");
            require(total(restored.interactions.inventory, 995) == 7
                            && total(restored.interactions.bank, 995) == 993,
                    "Reconnect must restore the exact Withdraw-X result without starter duplication");
            assertTotals(restored);
            require(!restored.interactions.bankOpen && !c.quantityActive() && !c.managerActive() && c.dialogue() == -1,
                    "Reconnect restores contents but never an old bank, dialogue or amount request");
            c.count(9);
            require(c.state().interactions.transactions == 0,
                    "A delayed pre-disconnect count cannot acquire ownership on the new session");
            c.npc(1, restored.npc.index);
            restored = c.await(s -> s.interactions.bankOpen, "open the restored real bank");
            require(total(restored.interactions.inventory, 995) == 7
                            && total(restored.interactions.bank, 995) == 993,
                    "Opening the restored bank must preserve the checkpoint quantities");
            assertTotals(restored);
            require(restored.unhandledFrames == 0 && restored.interactions.unhandledActions == 0
                            && restored.tickFailures == 0 && restored.facadeStrictHits == 0,
                    "Restored session must remain healthy");
        }
        require(world.reservedSlots() == 0 && World.getPlayers().isEmpty() && World.getNPCs().isEmpty(),
                "Reconnected smoke character and banker must also clean up");
        System.out.println("PASS: actual checkpoint and reconnect preserve Withdraw-X: inventory coins=7, bank coins=993; no stale request restored");
        System.out.println("Isolated dialogue/input smoke profile: " + directory);
    }

    private static final class Connection implements AutoCloseable {
        final Native950World world;
        final EmbeddedChannel channel = new EmbeddedChannel();
        final Native950Isaac client = new Native950Isaac(new int[] {947, 7, 2026, 16});
        final Native950Isaac output = new Native950Isaac(new int[] {997, 57, 2076, 66});
        final Native950Session session;
        final Native950QuantityInput quantity;

        Connection(Native950World world, Native950SaveStore saves) throws Exception {
            this.world = world;
            Native950Isaac incoming = new Native950Isaac(new int[] {947, 7, 2026, 16});
            Native950Isaac outgoing = new Native950Isaac(new int[] {997, 57, 2076, 66});
            for (int i = 0; i < 3; i++) { client.getAsInt(); incoming.getAsInt(); }
            for (int i = 0; i < 2; i++) { output.getAsInt(); outgoing.getAsInt(); }
            CompletableFuture<Native950Session> future = world.attach(channel, PROFILE, incoming,
                    outgoing, new byte[] {0}, new Native950World.SceneConfig(3217, 3258, 0, 1, 7, 0, 0, 0),
                    Collections.emptyList(), content(), saves);
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
            while (!future.isDone() && System.nanoTime() < deadline) { channel.runPendingTasks(); Thread.sleep(20); }
            session = future.get(1, TimeUnit.SECONDS);
            quantity = world.execute(() -> (Native950QuantityInput) field(field(session, "interactions"), "quantityInput"))
                    .get(5, TimeUnit.SECONDS);
        }

        Native950Session.Snapshot state() throws Exception {
            pump();
            Native950Session.Snapshot value = world.snapshot().get(5, TimeUnit.SECONDS);
            pump();
            require(value != null && value.active, "Disposable character must remain active");
            return value;
        }

        Native950Session.Snapshot await(Predicate<Native950Session.Snapshot> predicate, String description) throws Exception {
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(12);
            Native950Session.Snapshot value;
            do {
                value = state();
                if (predicate.test(value)) return value;
                Thread.sleep(20);
            } while (System.nanoTime() < deadline);
            throw new AssertionError("Timed out: " + description + "; last=" + value);
        }

        void send(int opcode, byte[] body) throws Exception {
            long drained = state().actionsDrained;
            require(channel.isActive(), "Input requires a live connection");
            channel.writeInbound(Unpooled.wrappedBuffer(new byte[] {(byte) (opcode + client.getAsInt())}));
            if (body.length != 0) channel.writeInbound(Unpooled.wrappedBuffer(body));
            await(s -> s.actionsDrained > drained, "consume encrypted opcode " + opcode);
        }

        void npc(int option, int index) throws Exception {
            send(new int[] {88, 115, 33, 60, 123, 104}[option - 1],
                    new byte[] {(byte) (index >>> 8), (byte) index, (byte) 128});
        }

        void reply(int panel, int component) throws Exception {
            int hash = (panel << 16) | component;
            send(15, new byte[] {(byte) 255, 127, (byte) (hash >>> 24), (byte) (hash >>> 16),
                    (byte) (hash >>> 8), (byte) hash});
        }

        void button(int option, int component, int slot, int item) throws Exception {
            int hash = (517 << 16) | component;
            send(new int[] {96, 77, 4, 95, 29, 51, 5, 21, 18, 36}[option - 1],
                    new byte[] {(byte) (item >>> 8), (byte) item, (byte) (hash >>> 8), (byte) hash,
                            (byte) (hash >>> 24), (byte) (hash >>> 16), (byte) (slot >>> 8), (byte) slot});
        }

        void beginAmount() throws Exception {
            Native950Session.Snapshot value = state();
            require(value.interactions.bankOpen, "Withdraw-X requires the genuinely opened bank");
            // Option 6 predicts zero quantity, so the actor remains the original item ID.
            button(6, 201, slot(value.interactions.bank, 995), 995);
            require(quantityActive(), "Option 6 must acquire an amount request");
            require(world.execute(() -> session.player().getInterfaceManager().containsInterface(1418))
                    .get(5, TimeUnit.SECONDS), "Amount frame must be tracked by the interface manager");
        }

        void count(long value) throws Exception { send(16, ByteBuffer.allocate(8).putLong(value).array()); }
        void walk(int x, int y) throws Exception {
            send(3, new byte[] {(byte) (y >>> 8), (byte) (y + 128), (byte) (x >>> 8), (byte) x, (byte) 128});
        }
        int dialogue() throws Exception {
            return world.execute(() -> session.player().getNative950Dialogues().interfaceId()).get(5, TimeUnit.SECONDS);
        }
        boolean managerActive() throws Exception {
            return world.execute(() -> session.player().getDialogueManager().hasDialogue()).get(5, TimeUnit.SECONDS);
        }
        boolean quantityActive() throws Exception { return world.execute(quantity::active).get(5, TimeUnit.SECONDS); }

        void pump() {
            channel.runPendingTasks();
            Object value;
            while ((value = channel.readOutbound()) != null) {
                try {
                    require(value instanceof ByteBuf, "Output must pass through the encrypted native encoder");
                    ByteBuf bytes = (ByteBuf) value;
                    while (bytes.isReadable()) {
                        int opcode = (bytes.readUnsignedByte() - output.getAsInt()) & 255;
                        if (opcode >= 128) opcode = ((opcode - 128) << 8) | ((bytes.readUnsignedByte() - output.getAsInt()) & 255);
                        ServerPacket type = null;
                        for (ServerPacket candidate : ServerPacket.values()) if (candidate.opcode() == opcode) type = candidate;
                        require(type != null, "Unknown/cipher-misaligned output opcode " + opcode);
                        int size = type.size();
                        if (size == -1) size = bytes.readUnsignedByte(); else if (size == -2) size = bytes.readUnsignedShort();
                        require(size <= bytes.readableBytes(), "Truncated native output frame");
                        bytes.skipBytes(size);
                    }
                } finally { ReferenceCountUtil.release(value); }
            }
            channel.checkException();
        }

        void disconnect() throws Exception {
            channel.close();
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (world.reservedSlots() > 0 && System.nanoTime() < deadline) { pump(); Thread.sleep(20); }
            require(world.reservedSlots() == 0 && World.getPlayers().isEmpty(), "Disconnect must remove the disposable character");
        }

        @Override public void close() throws Exception {
            try { if (channel.isActive()) disconnect(); }
            finally { channel.finishAndReleaseAll(); }
        }
    }

    private static Object field(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

    private static int total(Native950Containers.Snapshot state, int item) {
        return total(state.ids, state.amounts, item);
    }
    private static int total(int[] ids, int[] amounts, int item) {
        int count = 0;
        for (int i = 0; i < ids.length; i++) if (ids[i] == item) count += amounts[i];
        return count;
    }
    private static int slot(Native950Containers.Snapshot state, int item) {
        for (int i = 0; i < state.ids.length; i++) if (state.ids[i] == item) return i;
        throw new AssertionError("Item absent from container: " + item);
    }
    private static void assertTotals(Native950Session.Snapshot state) {
        for (int[] expected : new int[][] {{995, 1000}, {1511, 5}, {315, 5}})
            require(total(state.interactions.inventory, expected[0]) + total(state.interactions.bank, expected[0]) == expected[1],
                    "Container conservation failed for item " + expected[0]);
    }
    private static void require(boolean condition, String description) {
        if (!condition) throw new AssertionError(description);
    }
    private static Native950Content content() {
        Native950ItemCatalog items = new Native950ItemCatalog(Arrays.asList(
                new Native950ItemCatalog.Entry(995, "Coins", true, new String[] {"Add to pouch"}),
                new Native950ItemCatalog.Entry(1511, "Logs", false, new String[] {"Craft"}),
                new Native950ItemCatalog.Entry(315, "Shrimps", false, new String[] {"Eat"})));
        int[] amounts = new int[11]; amounts[1] = amounts[2] = 1;
        amounts[3] = 5; amounts[4] = 10; amounts[7] = Integer.MAX_VALUE;
        return new Native950Content(items, new Native950Content.BankUi(517, 201, 15, 317, 39,
                amounts, amounts, Collections.emptyList(), Collections.emptyList(), 6),
                new Native950Content.BankerNpc(494, "Banker", 3217, 3257, 0, 1, 1, 3));
    }
}
