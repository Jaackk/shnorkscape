package com.rs.cache;

import java.io.IOException;
import java.nio.file.Path;

import com.rs.Settings;
import com.rs.cache.filestore.io.OutputStream;
import com.rs.cache.filestore.store.Index;
import com.rs.cache.filestore.store.ReferenceTable;
import com.rs.cache.filestore.store.Store;
import com.rs.cache.filestore.util.whirlpool.Whirlpool;
import com.rs.network.codec.ProtocolSet;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

public final class Cache {

    public static final int LEGACY_MUSIC_INDEX = 6;
    public static final int RS3_MUSIC_INDEX = 40;

    /*
     * MUSIC PLAYBACK DOES NOT WORK ON THIS BUILD. Don't waste time enabling
     * these flags expecting music; the rabbit hole has been investigated and
     * is documented here so the next person doesn't repeat the journey.
     *
     * Background:
     *   - This server runs the 910 cache (2019 RS3). Music data lives in
     *     main_file_cache.dat2m and is indexed at JS5 slot 40 (RS3_MUSIC_INDEX).
     *   - The Java client this codebase ships with hardcodes the legacy music
     *     slot at 6 (LEGACY_MUSIC_INDEX), which is empty in the 910 cache.
     *   - The intent of ALIAS_RS3_MUSIC_TO_LEGACY_INDEX is to transparently
     *     redirect JS5 slot-6 reads to slot 40 so the legacy client picks
     *     up the modern music data.
     *
     * What was tried (each failed in a different way):
     *   1) Flag alone -> login crash. createInformationStore writes the
     *      hardcoded sizes[40] (-2002458710 overflow) into slot 6's manifest
     *      entry. The client validates the size against an internal table
     *      and bails during the login handshake.
     *   2) Flag + manifest fix (write CRC/whirlpool of slot 40 but keep
     *      sizes[6]=0) -> login succeeds, character spawns, but the moment
     *      MusicsManager.playMusic() fires PacketDispatcher#sendMusic the
     *      game loop freezes. Character can't move, can't chat. No JS5
     *      error in the server log. Symptom is consistent with the client
     *      stuck in a JS5 load loop because the music opcode (129,
     *      MIDI_SONG_PACKET) is not the right opcode for this client
     *      revision (compare: NXT opcode 70 is explicitly documented as
     *      "not valid for this NXT client" in PacketDispatcher).
     *   3) Disabling MUSIC_PACKETS_ENABLED while leaving the alias on ->
     *      stable, but no music plays (server never sends the trigger
     *      packet, so the client never tries to load music).
     *
     * What would be needed to actually make this work:
     *   - Discover the correct music opcode for the 910 Java client (the
     *     codebase only knows about 129/70 and both fail) - probably
     *     requires capturing real RS3 server traffic, examining the
     *     client's RuneScript handler table, or guessing.
     *   - OR re-pack music into legacy slot 6 of the cache so no alias
     *     is needed. Requires cache encoding tooling.
     *   - OR replace the client with one that natively supports slot 40
     *     (an NXT-compatible build, which then needs its own working
     *     music opcode wired up).
     *
     * Everything below this comment supports either state - alias on or
     * off - so you can experiment without breaking the build. The default
     * "false" leaves music silently disabled which is the documented
     * working configuration.
     */
    public static final boolean ALIAS_RS3_MUSIC_TO_LEGACY_INDEX = false;
    public static Store STORE;

    public static final byte[] createInformationStore(boolean web) {
        OutputStream stream = new OutputStream();
        int length = web ? 41 : STORE.getIndexes().length;
        int[] sizes = { 332643862, 5509611, 1606310, 8564971, 0, 457079636, 0, 1110280462, 1381199907, 0, 256, 0, 7476388, 124476, 1276505868, 0, 4407179, 1499632, 2567205, 5621389, 7656374, 103986, 3910938, 13660581, 139655, 2158, 476606, 369305, 4538, 10560, 79657846, 123467, 1912672, 373, 3543056, 125571, 0, 0, 0, 0, -2002458710, 34621796, 15556, 0, 0, 0, 0, -612896636, 480457436, 9453, 0, 0, -2022219860, 616398958, 2084415676, -1974131040, 126997537 };
        stream.writeByte(length);
        for (int index = 0; index < length; index++) {
            // resolveJs5IndexId only changes anything when
            // ALIAS_RS3_MUSIC_TO_LEGACY_INDEX is true (currently false; see
            // the long explanation on the flag). Kept here so flipping the
            // flag for experiments doesn't require re-wiring the loop.
            // CAUTION: with the flag on, this also writes sizes[40]
            // (-2002458710 overflow) into slot 6's manifest entry, which
            // crashes the client. A fix-attempt was to use sizes[index]
            // instead of sizes[sourceIndex] - that fixed the crash but
            // the music opcode then hung the client anyway. See the flag's
            // comment for the full story.
            int sourceIndex = resolveJs5IndexId(index);
            Index storeIndex = sourceIndex >= 0 && STORE.getIndexes().length > sourceIndex ? STORE.getIndexes()[sourceIndex] : null;
            if (storeIndex == null) {
                stream.writeInt(0);
                stream.writeInt(0);
                stream.writeInt(0);
                stream.writeInt(0);
                stream.writeBytes(new byte[64]);
                continue;
            }
            stream.writeInt(storeIndex.getCRC());
            stream.writeInt(storeIndex.getTable().getRevision());
            stream.writeInt(storeIndex.getTable().getArchives().length);
            stream.writeInt(sourceIndex < sizes.length ? sizes[sourceIndex] : 0);
            stream.writeBytes(storeIndex.getWhirlpool());
        }
        byte[] archive = new byte[stream.getOffset()];
        stream.setOffset(0);
        stream.getBytes(archive, 0, archive.length);
        OutputStream hashStream = new OutputStream(65);
        hashStream.writeByte(10);
        hashStream.writeBytes(Whirlpool.getHash(archive, 0, archive.length));
        byte[] hash = new byte[hashStream.getOffset()];
        hashStream.setOffset(0);
        hashStream.getBytes(hash, 0, hash.length);
        hash = Utils.cryptRSA(hash, ProtocolSet.JS5_EXPONENET, ProtocolSet.JS5_MODULUS);
        stream.writeBytes(hash);
        archive = new byte[stream.getOffset()];
        stream.setOffset(0);
        stream.getBytes(archive, 0, archive.length);
        return archive;
    }

    public static void init() throws IOException {
        ReferenceTable.NEW_PROTOCOL = true;
        STORE = new Store(Settings.CACHE_PATH);
        Logger.getGlobal().info("Cache initiated from path " + Settings.CACHE_PATH + "!");
        if (isLegacyMusicIndexAliased()) {
            Logger.getGlobal().info("Aliasing JS5 music index " + LEGACY_MUSIC_INDEX + " to dat2m index "
                    + RS3_MUSIC_INDEX + ".");
        }
    }

    /** Opens native-client assets without writable dat2/index handles or repacking. */
    public static void initFlatReadOnly(Path path) throws IOException {
        STORE = Store.openFlatReadOnly(path);
        com.rs.cache.loaders.ObjectDefinitions.clearObjectDefinitions();
        Logger.getGlobal().info("Modern read-only cache initiated from " + path);
    }

    public static boolean isFlatReadOnly() { return STORE != null && STORE.isReadOnly(); }

    public static boolean isLegacyMusicIndexAliased() {
        return ALIAS_RS3_MUSIC_TO_LEGACY_INDEX
                && STORE != null && STORE.getIndexes() != null
                && STORE.getIndexes().length > RS3_MUSIC_INDEX
                && STORE.getIndexes()[RS3_MUSIC_INDEX] != null
                && (STORE.getIndexes().length <= LEGACY_MUSIC_INDEX || STORE.getIndexes()[LEGACY_MUSIC_INDEX] == null);
    }

    public static int resolveJs5IndexId(int indexId) {
        return indexId == LEGACY_MUSIC_INDEX && isLegacyMusicIndexAliased() ? RS3_MUSIC_INDEX : indexId;
    }

    public static Index getJs5Index(int indexId) {
        if (STORE == null || STORE.getIndexes() == null) {
            return null;
        }
        int sourceIndex = resolveJs5IndexId(indexId);
        return sourceIndex >= 0 && STORE.getIndexes().length > sourceIndex ? STORE.getIndexes()[sourceIndex] : null;
    }
}
