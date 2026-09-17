package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.modern.FlatCacheRepository;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.network.modern.Native950Isaac;
import com.rs.network.protocol.modern950.Native950Packets;
import com.rs.network.protocol.modern950.Native950Protocol.ServerPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.ReferenceCountUtil;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.zip.CRC32;

/** Installed-cache music/session probe. Its characters never load or save profiles. */
public final class Native950RegionMusicSmoke {
    public static void main(String[] args) throws Exception {
        if (args.length < 1 || args.length > 2 || (args.length == 2 && !"--http".equals(args[1])))
            throw new IllegalArgumentException("Usage: Native950RegionMusicSmoke <flat-cache-directory> [--http]");
        Path cache = Paths.get(args[0]);
        Cache.initFlatReadOnly(cache);
        Native950InteractionsSmoke.captureLegacyDispatchBaseline();
        Native950RegionMusicCatalog catalog = new Native950RegionMusicCatalog();
        requireTrack(catalog.lookup(12850), "Harmony", 58, 36067);
        requireTrack(catalog.lookup(12849), "Yesteryear", 161, 37355);
        requireTrack(catalog.lookup(12851), "Autumn Voyage", 17, 37990);
        require(catalog.lookup(13106) == null, "The verified unmapped region must resolve to silence");
        for (int region : new int[] {12849, 13106}) {
            int square = (region >>> 8) | ((region & 255) << 7);
            require(Cache.STORE.getIndexes()[5].fileExists(square, 0)
                            && Cache.STORE.getIndexes()[5].fileExists(square, 3),
                    "Transition region requires real terrain and locations: " + region);
        }
        FlatCacheRepository repository = new FlatCacheRepository(cache);
        int chunks = 0;
        for (int archive : new int[] {36067, 37355, 37990})
            chunks += verifyJaga(repository, archive, args.length == 2);
        verifySameTrackRegions(catalog);

        String previousSpawns = System.getProperty(Native950World.SPAWNS_PROPERTY);
        System.setProperty(Native950World.SPAWNS_PROPERTY, "false");
        Native950World world = Native950World.getInstance();
        Connection previous;
        try {
            try (Connection c = new Connection(world)) {
                previous = c;
                c.await(s -> s.musicRegionId == 12850 && s.musicArchiveId == 36067, "initial Harmony selection");
                c.assertTrack(12850, 58, 36067, 1);
                c.assertMusic(36067);
                c.sendReplay(37355); // An unrelated resource is not authority to select it.
                c.sendReplay(-1);
                c.assertTrack(12850, 58, 36067, 1);
                c.assertMusic(36067);
                c.sendReplay(36067);
                c.assertTrack(12850, 58, 36067, 2);
                c.assertMusic(36067, 36067);
                c.sendClose(); // A real encrypted UI notification must not restart regional music.
                c.teleport(3210, 3217);
                c.awaitTicks(4);
                c.assertTrack(12850, 58, 36067, 2);
                c.assertMusic(36067, 36067);
                c.teleportWithReplay(3209, 3153, 36067);
                c.await(s -> s.musicRegionId == 12849, "post-movement Yesteryear region");
                c.assertTrack(12849, 161, 37355, 3);
                c.assertMusic(36067, 36067, 37355);
                c.sendReplay(36067); // Old completion after the region changed is stale.
                c.assertTrack(12849, 161, 37355, 3);
                c.assertMusic(36067, 36067, 37355);
                c.sendReplay(37355);
                c.assertTrack(12849, 161, 37355, 4);
                c.assertMusic(36067, 36067, 37355, 37355);
                c.teleport(3268, 3217);
                c.await(s -> s.musicRegionId == 13106, "unmapped region becomes silent");
                c.assertTrack(13106, -1, -1, 5);
                c.sendReplay(37355);
                c.sendReplay(-1);
                c.awaitTicks(4);
                c.assertTrack(13106, -1, -1, 5);
                c.assertMusic(36067, 36067, 37355, 37355, -1);
                c.assertHealthy();
            }
            require(world.reservedSlots() == 0 && World.getPlayers().isEmpty(), "First probe session must fully disconnect");
            try (Connection reconnected = new Connection(world)) {
                reconnected.await(s -> s.musicArchiveId == 36067, "fresh session starts the current region again");
                reconnected.awaitTicks(4);
                reconnected.assertTrack(12850, 58, 36067, 1);
                reconnected.assertMusic(36067);
                reconnected.assertHealthy();
                previous.assertNoMusicUiDrops();
                require(previous.session.isClosed() && previous.session.player().hasFinished(),
                        "The old character must stay retired while a new session plays");
            }
        } finally {
            world.setDataSpawnsEnabled(false);
            if (previousSpawns == null) System.clearProperty(Native950World.SPAWNS_PROPERTY);
            else System.setProperty(Native950World.SPAWNS_PROPERTY, previousSpawns);
        }
        require(world.reservedSlots() == 0 && World.getPlayers().isEmpty() && World.getNPCs().isEmpty(),
                "Music probe must release all disposable world entities");
        System.out.println("PASS: three paired-cache JAGA headers and " + chunks + " audio chunks validate with reference CRCs");
        if (args.length == 2) System.out.println("PASS: localhost HTTP serves headers and continuations with the native container length and CRC");
        System.out.println("PASS: encrypted session starts Harmony, changes to Yesteryear, enters silence, and reconnects cleanly");
        System.out.println("PASS: client59 replays only the current resource; stale, negative and same-tick pre-teleport events are ignored");
        System.out.println("PASS: same-track regions do not restart playback; no stale legacy music UI writes or saved profiles");
    }

    private static void verifySameTrackRegions(Native950RegionMusicCatalog catalog) {
        Native950RegionMusicCatalog.Track a = catalog.lookup(9008), b = catalog.lookup(9007);
        require(a != null && b != null && a.archiveId == b.archiveId && "Lost Soul".equals(a.name),
                "The paired catalogue must retain the two Lost Soul regions");
        List<Integer> writes = new ArrayList<Integer>();
        Native950RegionMusic controller = new Native950RegionMusic(catalog::lookup, writes::add);
        controller.start(9008);
        controller.update(9007);
        require(controller.regionId() == 9007 && writes.equals(Collections.singletonList(a.archiveId)),
                "Changing regions with the same actual archive must not restart it");
        controller.close();
        controller.requestReplay(a.archiveId);
        controller.update(12850);
        require(writes.size() == 1, "Closed music ownership cannot emit a late selection");
    }

    private static int verifyJaga(FlatCacheRepository repository, int archive, boolean http) throws Exception {
        byte[] header = repository.readFile(40, archive, 0);
        require(header != null && header.length >= 32, "Missing music header40/" + archive);
        ByteBuffer ints = ByteBuffer.wrap(header);
        require(ints.getInt() == 0x4a414741, "Music entry must contain a JAGA header: " + archive);
        require(ints.getInt() == 0 && ints.getInt() > 0, "Unsupported JAGA stream header: " + archive);
        int rate = ints.getInt(), channels = ints.getInt(), count = ints.getInt();
        require((rate == 22050 || rate == 44100) && channels == 2 && count > 0 && count <= 512,
                "Invalid stereo music stream metadata: " + archive);
        int tableEnd = 24 + count * 8;
        require(tableEnd <= header.length, "Truncated JAGA segment table: " + archive);
        boolean checkedContinuationHttp = false;
        int inlineOffset = tableEnd;
        for (int i = 0; i < count; i++) {
            int length = ints.getInt(), segment = ints.getInt();
            require(length > 0 && segment >= 0, "Invalid JAGA segment reference");
            byte[] bytes;
            if (segment == 0) {
                require(inlineOffset + (long) length <= header.length, "Truncated inline music segment");
                bytes = Arrays.copyOfRange(header, inlineOffset, inlineOffset + length);
                inlineOffset += length;
            } else {
                bytes = repository.readFile(40, segment, 0);
                require(bytes != null && bytes.length == length, "Missing or incomplete continuation40/" + segment);
                if (http && !checkedContinuationHttp) {
                    assertHttp(repository, segment);
                    checkedContinuationHttp = true;
                }
            }
            require(bytes.length >= 4 && ByteBuffer.wrap(bytes).getInt() == 0x4f676753,
                    "Music segment must begin with OggS: " + archive + "/" + i);
        }
        require(inlineOffset == header.length, "Unexpected unconsumed JAGA inline bytes");
        if (http) {
            assertHttp(repository, archive);
            require(checkedContinuationHttp, "HTTP probe requires a streamed continuation");
        }
        return count;
    }

    private static void assertHttp(FlatCacheRepository repository, int archive) throws Exception {
        byte[] stored = repository.readContainer(40, archive);
        int wireLength = ByteBuffer.wrap(stored, 1, 4).getInt() + (stored[0] == 0 ? 5 : 9);
        byte[] expected = Arrays.copyOf(stored, wireLength);
        CRC32 expectedCrc = new CRC32();
        expectedCrc.update(expected);
        int version = stored.length == wireLength + 2
                ? ((stored[wireLength] & 255) << 8) | (stored[wireLength + 1] & 255) : 0;
        HttpURLConnection connection = (HttpURLConnection) new URL("http://127.0.0.1:8080/ms?m=0&a=40&k=947&g="
                + archive + "&c=" + (int) expectedCrc.getValue() + "&v=" + version).openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        try {
            require(connection.getResponseCode() == 200, "Local music HTTP request failed: " + archive);
            require("application/octet-stream".equals(connection.getContentType()), "Wrong local music content type");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (InputStream in = connection.getInputStream()) {
                byte[] buffer = new byte[8192];
                for (int n; (n = in.read(buffer)) != -1;) {
                    require(out.size() + n <= 16 * 1024 * 1024, "Unexpectedly large music HTTP response");
                    out.write(buffer, 0, n);
                }
            }
            byte[] payload = out.toByteArray();
            require(Arrays.equals(expected, payload),
                    "HTTP must serve the JS5 container without its on-disk version trailer for40/" + archive);
            CRC32 receivedCrc = new CRC32(); receivedCrc.update(payload);
            require(receivedCrc.getValue() == expectedCrc.getValue(),
                    "HTTP body CRC must match the native c= request for40/" + archive);
            require(connection.getContentLengthLong() == payload.length, "Music HTTP length must match the body");
        } finally { connection.disconnect(); }
    }

    private static final class Connection implements AutoCloseable {
        final Native950World world;
        final EmbeddedChannel channel = new EmbeddedChannel();
        final Native950Isaac client = new Native950Isaac(new int[] {947, 40, 58, 36067});
        final Native950Isaac output = new Native950Isaac(new int[] {997, 90, 108, 36117});
        final Native950Session session;
        final List<byte[]> music = new ArrayList<byte[]>();
        long flushedTickEnds;

        Connection(Native950World world) throws Exception {
            this.world = world;
            Native950Isaac incoming = new Native950Isaac(new int[] {947, 40, 58, 36067});
            Native950Isaac outgoing = new Native950Isaac(new int[] {997, 90, 108, 36117});
            for (int i = 0; i < 3; i++) { client.getAsInt(); incoming.getAsInt(); }
            for (int i = 0; i < 2; i++) { output.getAsInt(); outgoing.getAsInt(); }
            CompletableFuture<Native950Session> attached = world.attach(channel, "music_probe", incoming,
                    outgoing, new byte[] {0}, new Native950World.SceneConfig(3209, 3217, 0, 1, 7, 0, 0, 0),
                    Arrays.asList(Native950Packets.openTop(1477), Native950Packets.openSub(1477, 30, 1482, true)), content());
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
            while (!attached.isDone() && System.nanoTime() < deadline) { channel.runPendingTasks(); Thread.sleep(20); }
            session = attached.get(1, TimeUnit.SECONDS);
            awaitFlushed(s -> true, "initial game tick");
        }
        Native950Session.Snapshot state() throws Exception {
            pump();
            Native950Session.Snapshot state = world.snapshot().get(5, TimeUnit.SECONDS);
            pump();
            require(state != null && state.active, "Disposable music character must remain active");
            return state;
        }
        void await(Predicate<Native950Session.Snapshot> condition, String description) throws Exception {
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(12);
            Native950Session.Snapshot state;
            do { state = state(); if (condition.test(state)) return; Thread.sleep(20); }
            while (System.nanoTime() < deadline);
            throw new AssertionError("Timed out: " + description + "; " + state);
        }
        void awaitFlushed(Predicate<Native950Session.Snapshot> condition, String description) throws Exception {
            // This fixture emits one bootstrap tick-end before ready(), then one
            // per completed session tick. A snapshot can precede the event-loop
            // flush, so require its corresponding encrypted boundary as evidence.
            await(s -> condition.test(s) && s.ticks > 0 && flushedTickEnds >= s.ticks + 1,
                    description + " with completed tick output");
        }
        void awaitTicks(int count) throws Exception { long target = state().ticks + count; awaitFlushed(s -> s.ticks >= target, "settled music ticks"); }
        void teleport(int x, int y) throws Exception {
            world.execute(() -> { session.player().setNextWorldTile(new WorldTile(x, y, 0)); return null; }).get(5, TimeUnit.SECONDS);
            awaitFlushed(s -> s.x == x && s.y == y, "authoritative teleport to " + x + "," + y);
        }
        void sendClose() throws Exception {
            long drained = state().actionsDrained;
            channel.writeInbound(Unpooled.wrappedBuffer(new byte[] {(byte) (55 + client.getAsInt())}));
            awaitFlushed(s -> s.actionsDrained > drained, "encrypted modal close");
        }
        void sendReplay(int archive) throws Exception {
            long drained = state().actionsDrained;
            channel.writeInbound(Unpooled.wrappedBuffer(replayFrame(archive)));
            awaitFlushed(s -> s.actionsDrained > drained, "encrypted music completion/unmute notification");
        }
        void teleportWithReplay(int x, int y, int oldArchive) throws Exception {
            // One world-thread command installs both inputs before the next input/movement
            // phases, proving a pending old-track completion cannot outrun the new tile.
            byte[] event = replayFrame(oldArchive);
            long drained = state().actionsDrained;
            world.execute(() -> {
                session.player().setNextWorldTile(new WorldTile(x, y, 0));
                channel.writeInbound(Unpooled.wrappedBuffer(event));
                return null;
            }).get(5, TimeUnit.SECONDS);
            awaitFlushed(s -> s.x == x && s.y == y && s.actionsDrained > drained,
                    "same-tick teleport and music completion");
        }
        private byte[] replayFrame(int archive) {
            // Client59 is a fixed four-byte signed int, in big-endian order.
            return ByteBuffer.allocate(5).put((byte) (59 + client.getAsInt())).putInt(archive).array();
        }
        void assertTrack(int region, int track, int archive, long changes) throws Exception {
            Native950Session.Snapshot s = state();
            require(s.musicRegionId == region && s.musicTrackId == track && s.musicArchiveId == archive
                            && s.musicChanges == changes, "Unexpected authoritative music state: " + s);
        }
        void assertMusic(int... archives) throws Exception {
            awaitFlushed(s -> music.size() >= archives.length, "music packet delivery");
            require(music.size() == archives.length, "Music writes must be deduplicated: expected " + archives.length + ", got " + music.size());
            for (int i = 0; i < archives.length; i++)
                require(Arrays.equals(Native950Packets.music(archives[i], 255).payload(), music.get(i)),
                        "Unexpected encrypted music payload at selection " + i);
        }
        void assertNoMusicUiDrops() throws Exception {
            require(world.execute(() -> {
                Map<Integer, Long> ids = ((Native950PacketDispatcher) session.player().getPackets()).counters()
                        .discardedIds("sendIComponentText");
                return !ids.containsKey(187);
            }).get(5, TimeUnit.SECONDS), "Legacy music must not write unowned interface187");
        }
        void assertHealthy() throws Exception {
            Native950Session.Snapshot s = state();
            assertNoMusicUiDrops();
            require(s.tickFailures == 0 && s.facadeStrictHits == 0 && s.schedulerFailed == 0
                            && s.unhandledFrames == 0 && s.interactions.unhandledActions == 0,
                    "Music lifecycle must remain healthy: " + s);
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
                        require(packet != null, "Unknown or cipher-misaligned output opcode " + opcode);
                        int size = packet.size();
                        if (size == -1) size = bytes.readUnsignedByte(); else if (size == -2) size = bytes.readUnsignedShort();
                        require(size <= bytes.readableBytes(), "Truncated native output frame");
                        if (packet == ServerPacket.SERVER_TICK_END) {
                            require(size == 0, "Tick boundary must have an empty body");
                            flushedTickEnds++;
                        } else if (packet == Native950Packets.music(-1, 255).type()) {
                            byte[] body = new byte[size]; bytes.readBytes(body); music.add(body);
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
                require(world.reservedSlots() == 0, "Disconnect must release the disposable music session");
            } finally { channel.finishAndReleaseAll(); }
        }
    }

    private static Native950Content content() {
        Native950ItemCatalog items = new Native950ItemCatalog(Arrays.asList(
                new Native950ItemCatalog.Entry(995, "Coins", true, new String[] {"Add to pouch"}),
                new Native950ItemCatalog.Entry(1511, "Logs", false, new String[] {"Craft"}),
                new Native950ItemCatalog.Entry(315, "Shrimps", false, new String[] {"Eat"})));
        return new Native950Content(items, new Native950Content.BankUi(517, 201, 15, 317, 39,
                new int[11], new int[11], Collections.emptyList(), Collections.emptyList(), 6));
    }
    private static void requireTrack(Native950RegionMusicCatalog.Track track, String name, int id, int archive) {
        require(track != null && track.trackId == id && track.archiveId == archive && name.equals(track.name),
                "Paired music mapping changed for " + name);
    }
    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
