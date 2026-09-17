package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.network.modern.Native950Isaac;
import com.rs.network.protocol.modern950.Native950Protocol.ServerPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.ReferenceCountUtil;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/** Opt-in paired-cache generic NPC route probe; no profiles are loaded or saved. */
public final class Native950CookDialogueSmoke {
    public static void main(String[] args) throws Exception {
        if (args.length != 1)
            throw new IllegalArgumentException("Usage: Native950CookDialogueSmoke <flat-cache-directory>");
        Cache.initFlatReadOnly(Paths.get(args[0]));
        Native950InteractionsSmoke.captureLegacyDispatchBaseline();
        System.setProperty(Native950World.SPAWNS_PROPERTY, "true");
        String previousMapProperty = System.getProperty(Native950WorldMap.PROPERTY);
        System.setProperty(Native950WorldMap.PROPERTY, "true");
        Native950World world = Native950World.getInstance();
        try (Connection c = new Connection(world)) {
            c.await(s -> s.npc != null && s.npc.publishedCount > 0, "data NPCs published");
            NPC cook = world.execute(() -> {
                NPC found = null;
                for (NPC npc : World.getNPCs()) {
                    if (npc.getId() != 278 || npc.getPlane() != 0
                            || Math.abs(npc.getX() - 3209) > 3 || Math.abs(npc.getY() - 3215) > 3) continue;
                    require(found == null, "Exactly one nearby data-spawned Cook is required");
                    found = npc;
                }
                require(found != null && found.isNative950(), "Existing Lumbridge Cook must spawn from data");
                NPCDefinitions decoded = NPCDefinitions.decodeStrict947(278,
                        Cache.STORE.getIndexes()[18].getFile(278 >>> 7, 278 & 127), null);
                // Paired 947 cache18/2/file22 stores opcode30 as "Talk to" (with a space).
                require("Cook".equals(found.getName()) && found.hasMenuOption("Talk to"),
                        "Cook menu metadata must come from the paired cache; name=" + found.getName()
                                + ", decodedName=" + decoded.name + ", options=" + Arrays.toString(decoded.menuOptions));
                return found;
            }).get(5, TimeUnit.SECONDS);
            int index = world.execute(cook::getIndex).get(5, TimeUnit.SECONDS);
            // This is the ordinary native option1 path, not a direct startDialogue call.
            c.send(88, new byte[] {(byte) (index >>> 8), (byte) index, (byte) 128});
            c.awaitDialogue(1184, "generic Cook greeting through NPCHandler and its RouteEvent");
            c.reply(1184, 15);
            require(c.dialogue() == 1188, "Cook Continue must render his existing Yes/No choices");
            c.reply(1188, 8);
            require(c.dialogue() == 1191, "Yes must render the Cook dialogue's existing player speech");
            c.state(); // Drain the encrypted frame that carries the portrait.
            require(c.playerHeads == 1 && c.lastPlayerHead == ((1191 << 16) | 8),
                    "Player speech must send exactly one correctly addressed native player portrait");
            require(world.execute(() -> c.session.player().getDialogueManager().hasDialogue()
                            && !c.session.player().hasTalkedtoCook()
                            && !c.session.player().isKilledCulinaromancer()).get(5, TimeUnit.SECONDS),
                    "Probe must stop before Cook quest/combat state changes");
            c.button(1465, 11);
            require(c.dialogue() == -1 && world.execute(() ->
                            c.session.player().getInterfaceManager().containsInterface(1421)
                                    && c.session.player().getInterfaceManager().containsInterface(1422)
                                    && !c.session.player().getInterfaceManager().containsInterface(1482))
                    .get(5, TimeUnit.SECONDS), "Opening the real map must cancel Cook and replace the scene");
            c.send(55, new byte[0]);
            require(c.dialogue() == -1 && !world.execute(() -> c.session.player().getDialogueManager().hasDialogue())
                    .get(5, TimeUnit.SECONDS), "Map modal close must leave the generic dialogue retired");
            c.assertRestoredScene();
            int sceneClosesAfterMap = c.sceneCloses;
            c.reply(1191, 15);
            require(c.dialogue() == -1 && !world.execute(() -> c.session.player().hasTalkedtoCook())
                    .get(5, TimeUnit.SECONDS), "Late player Continue cannot resume a cancelled quest dialogue");
            // The old generic cleanup mistook restored1482@30 for the legacy map,
            // closed the scene, then attempted its unbound910 replacement at23.
            c.send(88, new byte[] {(byte) (index >>> 8), (byte) index, (byte) 128});
            c.awaitDialogue(1184, "Talk to Cook again after closing the world map");
            c.assertRestoredScene();
            require(c.sceneCloses == sceneClosesAfterMap,
                    "Generic NPC cleanup after a map roundtrip must never send CLOSESUB1477:30");
            c.send(55, new byte[0]);
            c.assertRestoredScene();
            require(c.dialogue() == -1 && c.sceneCloses == sceneClosesAfterMap
                            && !world.execute(() -> c.session.player().getDialogueManager().hasDialogue()
                                    || c.session.player().hasTalkedtoCook()).get(5, TimeUnit.SECONDS),
                    "Final dialogue cancellation must preserve the scene and untouched Cook quest state");
            Native950Session.Snapshot state = c.state();
            require(state.tickFailures == 0 && state.facadeStrictHits == 0 && state.unhandledFrames == 0
                            && state.interactions.unhandledActions == 0
                            && state.interactions.routerReport.contains("handlerFailures=0"),
                    "Generic Cook route must remain healthy: " + state.interactions.routerReport + "; " + state);
            System.out.println("PASS: data-spawned Cook -> encrypted native option1 -> original NPCHandler/RouteEvent -> greeting/options/player speech");
            System.out.println("PASS: player portrait opcode81 addresses1191:8; close and late Continue preserve untouched quest state");
            System.out.println("PASS: Cook -> map -> modal close -> Cook -> dialogue close preserves1482@1477:30 without a scene-close packet");
        } finally {
            world.setDataSpawnsEnabled(false);
            if (previousMapProperty == null) System.clearProperty(Native950WorldMap.PROPERTY);
            else System.setProperty(Native950WorldMap.PROPERTY, previousMapProperty);
        }
        require(world.reservedSlots() == 0 && World.getPlayers().isEmpty() && World.getNPCs().isEmpty(),
                "Disposable Cook probe must release its player and spawned NPCs");
    }

    private static final class Connection implements AutoCloseable {
        final Native950World world;
        final EmbeddedChannel channel = new EmbeddedChannel();
        final Native950Isaac client = new Native950Isaac(new int[] {947, 7, 278, 81});
        final Native950Isaac output = new Native950Isaac(new int[] {997, 57, 328, 131});
        final Native950Session session;
        int playerHeads, lastPlayerHead, sceneCloses;

        Connection(Native950World world) throws Exception {
            this.world = world;
            Native950Isaac incoming = new Native950Isaac(new int[] {947, 7, 278, 81});
            Native950Isaac outgoing = new Native950Isaac(new int[] {997, 57, 328, 131});
            for (int i = 0; i < 3; i++) { client.getAsInt(); incoming.getAsInt(); }
            for (int i = 0; i < 2; i++) { output.getAsInt(); outgoing.getAsInt(); }
            CompletableFuture<Native950Session> attached = world.attach(channel, "cook_probe", incoming,
                    outgoing, new byte[] {0}, new Native950World.SceneConfig(3209, 3217, 0, 1, 7, 0, 0, 0),
                    Collections.emptyList(), content());
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
            while (!attached.isDone() && System.nanoTime() < deadline) { channel.runPendingTasks(); Thread.sleep(20); }
            session = attached.get(1, TimeUnit.SECONDS);
        }

        Native950Session.Snapshot state() throws Exception {
            pump();
            Native950Session.Snapshot state = world.snapshot().get(5, TimeUnit.SECONDS);
            pump();
            require(state != null && state.active, "Disposable Cook character must remain active");
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

        void send(int opcode, byte[] body) throws Exception {
            long drained = state().actionsDrained;
            channel.writeInbound(Unpooled.wrappedBuffer(new byte[] {(byte) (opcode + client.getAsInt())}));
            if (body.length != 0) channel.writeInbound(Unpooled.wrappedBuffer(body));
            await(s -> s.actionsDrained > drained, "consume encrypted opcode " + opcode);
        }

        void reply(int panel, int component) throws Exception {
            int hash = (panel << 16) | component;
            send(15, new byte[] {(byte) 255, 127, (byte) (hash >>> 24), (byte) (hash >>> 16),
                    (byte) (hash >>> 8), (byte) hash});
        }

        void button(int panel, int component) throws Exception {
            send(96, new byte[] {-1, -1, (byte) (component >>> 8), (byte) component,
                    (byte) (panel >>> 8), (byte) panel, -1, -1});
        }

        void assertRestoredScene() throws Exception {
            state(); // Include the completed input phase's outbound packets in sceneCloses.
            require(world.execute(() ->
                            session.player().getInterfaceManager().getInterfaceParentId(1482) == ((1477 << 16) | 30)
                                    && !session.player().getInterfaceManager().containsInterface(1421)
                                    && !session.player().getInterfaceManager().containsInterface(1422))
                    .get(5, TimeUnit.SECONDS), "The restored game scene must stay at1477:30 without map layers");
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
                        if (packet == ServerPacket.IF_CLOSESUB) {
                            require(size == 4, "Native subinterface close is a four-byte component hash");
                            if (bytes.readIntLE() == ((1477 << 16) | 30)) sceneCloses++;
                        } else if (opcode == 81) {
                            require(size == 4, "Native player portrait is a four-byte component hash");
                            int b = bytes.readUnsignedByte(), a = bytes.readUnsignedByte();
                            int d = bytes.readUnsignedByte(), c = bytes.readUnsignedByte();
                            lastPlayerHead = (a << 24) | (b << 16) | (c << 8) | d;
                            playerHeads++;
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

    private static void require(boolean condition, String description) {
        if (!condition) throw new AssertionError(description);
    }
}
