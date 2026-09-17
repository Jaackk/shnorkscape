package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.player.content.InterfaceManager;
import com.rs.network.modern.Native950Isaac;
import com.rs.network.protocol.modern950.Native950Packets;
import com.rs.network.protocol.modern950.Native950Protocol.ServerPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.ReferenceCountUtil;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/** Opt-in encrypted settings/scene/dialogue regression; never loads or saves profiles. */
public final class Native950SettingsSmoke {
    public static void main(String[] args) throws Exception {
        if (args.length != 1)
            throw new IllegalArgumentException("Usage: Native950SettingsSmoke <flat-cache-directory>");
        Cache.initFlatReadOnly(Paths.get(args[0]));
        Native950InteractionsSmoke.captureLegacyDispatchBaseline();
        String previousSpawns = System.getProperty(Native950World.SPAWNS_PROPERTY);
        String previousMap = System.getProperty(Native950WorldMap.PROPERTY);
        System.setProperty(Native950World.SPAWNS_PROPERTY, "true");
        System.setProperty(Native950WorldMap.PROPERTY, "true");
        Native950World world = Native950World.getInstance();
        try (Connection c = new Connection(world)) {
            c.await(s -> s.npc != null && s.npc.publishedCount > 0, "data NPCs published");
            c.assertRestoredScene();
            require(c.sceneCloses == 0, "Initial scene bootstrap must open the game view without closing it");
            int cookIndex = world.execute(() -> {
                NPC found = null;
                for (NPC npc : World.getNPCs()) {
                    if (npc.getId() != 278 || npc.getPlane() != 0
                            || Math.abs(npc.getX() - 3209) > 3 || Math.abs(npc.getY() - 3215) > 3) continue;
                    require(found == null, "Exactly one nearby data-spawned Cook is required");
                    found = npc;
                }
                require(found != null && found.isNative950(), "Existing Lumbridge Cook must spawn from data");
                return found.getIndex();
            }).get(5, TimeUnit.SECONDS);

            c.npc(cookIndex);
            c.awaitDialogue(1184, "Cook greeting through the generic native NPC route");
            c.button(1431, 0, 6, -1); // Adjacent unsupported ribbon actor cannot replace Cook.
            require(c.dialogue() == 1184 && !c.settingsOpen(), "Malformed settings entry must preserve Cook");
            c.button(1431, 0, 7, -1);
            c.assertGraphics();
            require(c.dialogue() == -1 && !world.execute(() -> c.session.player().getDialogueManager().hasDialogue())
                    .get(5, TimeUnit.SECONDS), "Settings entry must retire the existing Cook dialogue");
            c.reply(1184, 15);
            c.assertGraphics();
            require(c.dialogue() == -1, "Late Cook Continue cannot displace Settings");
            c.button(1477, 714, 15, -1);
            c.assertAudio();
            c.button(1477, 714, 7, -1);
            c.assertGraphics();
            // Gameplay and Accessibility share interface365 but have distinct
            // native onLoad state. Check the encrypted selection/reopen order.
            for (int page : new int[] {1, 6, 1, 3, 5, 2, 4}) {
                c.selectTab(page, true);
                c.selectTab(page, false);
                c.assertRestoredScene();
            }
            c.selectTab(1, true); // Audio must also clean up into a newly enabled page.
            c.button(365, 19, 10240, -1); // Unported Full Manual must recover, not change the owner.
            c.assertSimplePage(1);
            c.selectTab(4, true);
            c.button(1477, 717, 1, -1);
            c.assertClosed();
            for (int actor : new int[] {3, 7, 11, 15, 19, 23}) {
                c.button(1477, 714, actor, -1);
                c.assertClosed();
            }
            c.assertRestoredScene();
            require(c.sceneCloses == 0, "Settings and stale page actions must never close the scene");

            // Both entry paths and the native root toggle must agree on one owner.
            c.button(1477, 8, -1, -1);
            c.assertGraphics();
            c.button(1477, 714, 15, -1);
            c.assertAudio();
            c.button(1477, 8, -1, -1);
            c.assertClosed();
            c.button(1431, 0, 7, -1);
            c.assertGraphics();
            c.button(1465, 11, -1, -1);
            require(!c.settingsOpen() && world.execute(() -> c.manager().containsWorldMapInterface())
                    .get(5, TimeUnit.SECONDS), "Map entry must retire Settings and own the map layers");
            int closesAfterMap = c.stateAndSceneCloses();
            require(closesAfterMap == 1, "Only opening the map may close the scene in this probe");
            c.button(1431, 0, 7, -1); // Settings must close map and restore the scene first.
            c.assertGraphics();
            c.assertRestoredScene();
            c.send(55, new byte[0]);
            c.assertClosed();
            c.button(1477, 714, 15, -1);
            c.assertClosed();
            c.assertRestoredScene();

            // Real stopAll()/Player.closeInterfaces must not confuse restored scene with map/settings.
            c.npc(cookIndex);
            c.awaitDialogue(1184, "Cook again after Settings and map roundtrips");
            c.assertRestoredScene();
            c.reply(1184, 15);
            require(c.dialogue() == 1188, "Cook greeting must still advance to existing Yes/No options");
            c.reply(1188, 13); // No, leaving the quest unchanged.
            c.assertClosed();
            c.assertRestoredScene();
            require(c.dialogue() == -1 && !world.execute(() -> c.session.player().getDialogueManager().hasDialogue()
                            || c.session.player().hasTalkedtoCook() || c.session.player().isKilledCulinaromancer())
                    .get(5, TimeUnit.SECONDS), "Cook No must close the dialogue without changing quest state");
            require(c.sceneCloses == closesAfterMap,
                    "Settings close and later generic NPC cleanup must preserve1482@1477:30");
            Native950Session.Snapshot state = c.state();
            require(state.tickFailures == 0 && state.facadeStrictHits == 0 && state.unhandledFrames == 0
                            && state.interactions.unhandledActions == 0
                            && state.interactions.routerReport.contains("handlerFailures=0"),
                    "Settings regression must remain healthy: " + state.interactions.routerReport + "; " + state);
            System.out.println("PASS: all six encrypted Settings tabs, repeated selections, shared Gameplay/Accessibility reinitialization and Audio cleanup");
            System.out.println("PASS: all six stale tab actors remain closed; settings transitions never close the scene");
            System.out.println("PASS: Settings -> map -> Settings -> modal close -> generic Cook -> No preserves scene and quest state");
            System.out.println("PASS: exactly one scene-close packet belongs to map entry; no profiles loaded or saved");
        } finally {
            world.setDataSpawnsEnabled(false);
            restore(Native950World.SPAWNS_PROPERTY, previousSpawns);
            restore(Native950WorldMap.PROPERTY, previousMap);
        }
        // freeSlot runs before clearNativeNpcs in closeOnWorld. Observe both on
        // that same world thread, after its close command, instead of racing it.
        require(world.execute(() -> world.reservedSlots() == 0
                        && World.getPlayers().isEmpty() && World.getNPCs().isEmpty()).get(5, TimeUnit.SECONDS),
                "Disposable Settings probe must release its player and spawned NPCs");
    }

    private static final class Connection implements AutoCloseable {
        final Native950World world;
        final EmbeddedChannel channel = new EmbeddedChannel();
        final Native950Isaac client = new Native950Isaac(new int[] {947, 9, 137, 429});
        final Native950Isaac output = new Native950Isaac(new int[] {997, 59, 187, 479});
        final Native950Session session;
        final List<Frame> settingsFrames = new ArrayList<>();
        int sceneCloses;
        int selectedPage;
        long flushedTickEnds;

        Connection(Native950World world) throws Exception {
            this.world = world;
            Native950Isaac incoming = new Native950Isaac(new int[] {947, 9, 137, 429});
            Native950Isaac outgoing = new Native950Isaac(new int[] {997, 59, 187, 479});
            for (int i = 0; i < 3; i++) { client.getAsInt(); incoming.getAsInt(); }
            for (int i = 0; i < 2; i++) { output.getAsInt(); outgoing.getAsInt(); }
            CompletableFuture<Native950Session> attached = world.attach(channel, "settings_probe", incoming,
                    outgoing, new byte[] {0}, new Native950World.SceneConfig(3209, 3217, 0, 1, 7, 0, 0, 0),
                    Arrays.asList(Native950Packets.openTop(1477), Native950Packets.openSub(1477, 30, 1482, true)),
                    content());
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
            while (!attached.isDone() && System.nanoTime() < deadline) { channel.runPendingTasks(); Thread.sleep(20); }
            session = attached.get(1, TimeUnit.SECONDS);
            awaitFlushed(s -> true, "initial settings probe tick");
        }

        InterfaceManager manager() { return session.player().getInterfaceManager(); }
        boolean settingsOpen() throws Exception {
            return world.execute(() -> manager().containsNative950Settings()).get(5, TimeUnit.SECONDS);
        }
        void assertGraphics() throws Exception {
            state();
            require(world.execute(() -> manager().containsNative950Settings()
                            && manager().getInterfaceParentId(1448) == ((1477 << 16) | 715)
                            && manager().getInterfaceParentId(1426) == ((1448 << 16) | 3)
                            && manager().getInterfaceParentId(742) == ((1426 << 16) | 0)
                            && !manager().containsInterface(429) && !manager().containsInterface(365)
                            && !manager().containsInterface(1444) && !manager().containsInterface(567))
                    .get(5, TimeUnit.SECONDS),
                    "Graphics must own the management window and its exact nested native content");
            require(selectedPage == 2, "Graphics must select native settings page2");
        }
        void assertAudio() throws Exception {
            state();
            require(world.execute(() -> manager().containsNative950Settings()
                            && manager().getInterfaceParentId(1448) == ((1477 << 16) | 715)
                            && manager().getInterfaceParentId(429) == ((1448 << 16) | 5)
                            && manager().getInterfaceParentId(1426) == ((1448 << 16) | 3)
                            && !manager().containsInterface(742) && !manager().containsInterface(365)
                            && !manager().containsInterface(1444) && !manager().containsInterface(567))
                    .get(5, TimeUnit.SECONDS), "Audio must occupy its native column with an empty resize placeholder");
            require(selectedPage == 4, "Audio must select native settings page4");
        }
        void assertSimplePage(int page) throws Exception {
            state();
            int interfaceId = pageInterface(page);
            require(world.execute(() -> {
                if (!manager().containsNative950Settings()
                        || manager().getInterfaceParentId(1448) != ((1477 << 16) | 715)
                        || manager().getInterfaceParentId(interfaceId) != ((1448 << 16) | 3)) return false;
                for (int other : new int[] {365, 1444, 567, 1426, 742, 429})
                    if (other != interfaceId && manager().containsInterface(other)) return false;
                return true;
            }).get(5, TimeUnit.SECONDS), "Settings page" + page + " must exclusively own its native content");
            require(selectedPage == page, "Native page selection must distinguish shared interface365: expected "
                    + page + ", got " + selectedPage);
        }
        void selectTab(int page, boolean replace) throws Exception {
            int before = settingsFrames.size();
            int previous = selectedPage;
            button(1477, 714, page * 4 - 1, -1);
            if (page == 2) assertGraphics(); else if (page == 4) assertAudio(); else assertSimplePage(page);
            int select = frameIndex(before, Native950Packets.varbitSmall(19001, page));
            require(select >= before, "A tab response must select its native page");
            require(frameIndex(before, Native950Packets.hideInterface(1448, 1, true)) >= before,
                    "Every tab response must dismiss the native Loading overlay");
            if (page == 1) {
                require(frameIndex(before, Native950Packets.interfaceEvents(365, 19, 10240, 10242, 2)) >= before
                                && frameIndex(before, Native950Packets.interfaceEvents(365, 19, 15872, 15872, 2)) >= before,
                        "Waiting combat choices must be able to notify the server");
            }
            if (replace && previous == 1) {
                require(frameIndex(before, Native950Packets.interfaceEvents(365, 19, 10240, 10242, 0)) >= before
                                && frameIndex(before, Native950Packets.interfaceEvents(365, 19, 15872, 15872, 0)) >= before,
                        "Leaving Gameplay must retire its checkbox notification overrides");
            }
            if (replace) {
                int close = frameIndex(before, Native950Packets.closeSub(1448, 3));
                int open = frameIndex(before, Native950Packets.openSub(1448, 3, pageInterface(page), true));
                require(close >= before && close < select && select < open,
                        "A changed tab must close its old host and select its page before opening native controls");
                if (previous == 4) require(frameIndex(before, Native950Packets.closeSub(1448, 5)) >= before,
                        "Leaving Audio must retire its additional native column");
            } else {
                for (int i = before; i < settingsFrames.size(); i++)
                    require(settingsFrames.get(i).type != ServerPacket.IF_CLOSESUB
                                    && settingsFrames.get(i).type != ServerPacket.IF_OPENSUB,
                            "A repeated tab must refresh visibility without recreating native controls");
            }
        }
        int frameIndex(int from, Native950Packets.Packet expected) {
            for (int i = from; i < settingsFrames.size(); i++)
                if (settingsFrames.get(i).matches(expected)) return i;
            return -1;
        }
        void assertClosed() throws Exception {
            state();
            require(world.execute(() -> !manager().containsNative950Settings()
                            && !manager().containsInterface(1448) && !manager().containsInterface(429)
                            && !manager().containsInterface(1426) && !manager().containsInterface(742)
                            && !manager().containsInterface(365) && !manager().containsInterface(1444)
                            && !manager().containsInterface(567))
                    .get(5, TimeUnit.SECONDS), "Retired Settings must leave no owned content registered");
        }
        void assertRestoredScene() throws Exception {
            state();
            require(world.execute(() -> manager().getInterfaceParentId(1482) == ((1477 << 16) | 30)
                            && !manager().containsWorldMapInterface() && !manager().containsInterface(1421)
                            && !manager().containsInterface(1422)).get(5, TimeUnit.SECONDS),
                    "The scene must remain at1477:30 without map layers");
        }
        int stateAndSceneCloses() throws Exception { state(); return sceneCloses; }
        Native950Session.Snapshot state() throws Exception {
            pump();
            Native950Session.Snapshot state = world.snapshot().get(5, TimeUnit.SECONDS);
            pump();
            require(state != null && state.active, "Disposable Settings character must remain active");
            return state;
        }
        void await(Predicate<Native950Session.Snapshot> condition, String description) throws Exception {
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(12);
            Native950Session.Snapshot state;
            do {
                state = state();
                if (condition.test(state)) return;
                Thread.sleep(20);
            } while (System.nanoTime() < deadline);
            throw new AssertionError("Timed out: " + description + "; " + state);
        }
        void awaitFlushed(Predicate<Native950Session.Snapshot> condition, String description) throws Exception {
            // One bootstrap boundary precedes ready(); each completed world tick
            // contributes one more. Wait for actual encrypted output, not only state.
            await(s -> condition.test(s) && s.ticks > 0 && flushedTickEnds >= s.ticks + 1, description);
        }
        void send(int opcode, byte[] body) throws Exception {
            long drained = state().actionsDrained;
            channel.writeInbound(Unpooled.wrappedBuffer(new byte[] {(byte) (opcode + client.getAsInt())}));
            if (body.length != 0) channel.writeInbound(Unpooled.wrappedBuffer(body));
            awaitFlushed(s -> s.actionsDrained > drained, "consume and flush encrypted opcode " + opcode);
        }
        void npc(int index) throws Exception { send(88, new byte[] {(byte) (index >>> 8), (byte) index, (byte) 128}); }
        void reply(int panel, int component) throws Exception {
            int hash = (panel << 16) | component;
            send(15, new byte[] {-1, 127, (byte) (hash >>> 24), (byte) (hash >>> 16),
                    (byte) (hash >>> 8), (byte) hash});
        }
        void button(int panel, int component, int slot, int item) throws Exception {
            send(96, new byte[] {(byte) (item >>> 8), (byte) item, (byte) (component >>> 8), (byte) component,
                    (byte) (panel >>> 8), (byte) panel, (byte) (slot >>> 8), (byte) slot});
        }
        int dialogue() throws Exception {
            return world.execute(() -> session.player().getNative950Dialogues().interfaceId()).get(5, TimeUnit.SECONDS);
        }
        void awaitDialogue(int panel, String description) throws Exception {
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(12);
            do {
                state();
                if (dialogue() == panel) return;
                Thread.sleep(20);
            } while (System.nanoTime() < deadline);
            throw new AssertionError("Timed out: " + description + "; " + state().interactions.routerReport);
        }
        void pump() {
            channel.runPendingTasks();
            Object value;
            while ((value = channel.readOutbound()) != null) {
                try {
                    require(value instanceof ByteBuf, "Output must use the native encrypted encoder");
                    ByteBuf bytes = (ByteBuf) value;
                    while (bytes.isReadable()) {
                        int opcode = (bytes.readUnsignedByte() - output.getAsInt()) & 255;
                        if (opcode >= 128) opcode = ((opcode - 128) << 8) | ((bytes.readUnsignedByte() - output.getAsInt()) & 255);
                        ServerPacket packet = null;
                        for (ServerPacket candidate : ServerPacket.values()) if (candidate.opcode() == opcode) packet = candidate;
                        require(packet != null, "Unknown/cipher-misaligned output opcode " + opcode);
                        int size = packet.size();
                        if (size == -1) size = bytes.readUnsignedByte(); else if (size == -2) size = bytes.readUnsignedShort();
                        require(size <= bytes.readableBytes(), "Truncated native output frame");
                        if (packet == ServerPacket.SERVER_TICK_END) {
                            require(size == 0, "Native tick boundary must have an empty body");
                            flushedTickEnds++;
                        } else if (packet == ServerPacket.IF_CLOSESUB || packet == ServerPacket.IF_OPENSUB
                                || packet == ServerPacket.VARBIT_SMALL || packet == ServerPacket.IF_SETHIDE
                                || packet == ServerPacket.IF_SETEVENTS) {
                            byte[] body = new byte[size];
                            bytes.readBytes(body);
                            Frame frame = new Frame(packet, body);
                            settingsFrames.add(frame);
                            if (frame.matches(Native950Packets.closeSub(1477, 30))) sceneCloses++;
                            if (packet == ServerPacket.VARBIT_SMALL) {
                                require(size == 3, "Native small varbit must have a three-byte body");
                                if (((body[0] & 255) | ((body[1] & 255) << 8)) == 19001)
                                    selectedPage = ((body[2] & 255) - 128) & 255;
                            }
                        } else bytes.skipBytes(size);
                    }
                } finally { ReferenceCountUtil.release(value); }
            }
            channel.checkException();
        }
        @Override public void close() throws Exception {
            try {
                channel.close();
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
                while (world.reservedSlots() > 0 && System.nanoTime() < deadline) { pump(); Thread.sleep(20); }
                require(world.reservedSlots() == 0, "Disconnect must release the disposable character");
            } finally { channel.finishAndReleaseAll(); }
        }
    }

    private static final class Frame {
        final ServerPacket type;
        final byte[] body;
        Frame(ServerPacket type, byte[] body) { this.type = type; this.body = body; }
        boolean matches(Native950Packets.Packet expected) {
            return type == expected.type() && Arrays.equals(body, expected.payload());
        }
    }
    private static int pageInterface(int page) {
        switch (page) {
            case 1: case 6: return 365;
            case 2: case 4: return 1426;
            case 3: return 1444;
            case 5: return 567;
            default: throw new IllegalArgumentException("Unknown Settings page " + page);
        }
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
    private static void restore(String property, String value) {
        if (value == null) System.clearProperty(property); else System.setProperty(property, value);
    }
    private static void require(boolean condition, String description) {
        if (!condition) throw new AssertionError(description);
    }
}
