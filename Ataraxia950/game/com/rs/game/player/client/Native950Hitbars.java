package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.filestore.store.Store;
import com.rs.game.Entity;
import com.rs.game.hitbar.HitBar;
import com.rs.game.hitbar.impl.EntityHitBar;
import com.rs.game.hitbar.impl.MiningHitBar;
import com.rs.network.protocol.modern950.Native950NpcMasks;
import com.rs.network.protocol.modern950.Native950PlayerMasks;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Standard 910 HP bars and two exact 950 mining gauges from paired config2/72 definitions.
 * See protocol-analysis/hitbars-hitmarks-950.md. Verify the selected cache at native startup.
 * This adapter reads queued state without mutating it and returns immutable per-tick values.
 */
public final class Native950Hitbars {
    private static final int[] IDS = {0, 3, 4, 7, 49};
    private static final String[] SHA256 = {
        "f0e4c0c5b3c4714f07a60b0d55c2c91e7b0ce3497fd6231f870b8de73bf52cc6",
        "f1c04433edb7b57f65f9c975782075768c94ccca7e7eb66449beb3d7d4e807a4",
        "8aa4da3217bfdd3df984661522f237cd2e8bf104dc2d3a280b43d66d0d2ef661",
        "2d9003f47e6ae1f8634b8f5db46cc95bc546309c2ccded5ab0c628202e1e4485",
        "972816b7e85c90fa3cdf7d5057282951ef433e9873353be02b73f6bb7028a7a7"
    };
    private static Store verifiedStore;
    private Native950Hitbars() { }

    /** Returns false when permissive research mode found a mismatched binding. */
    public static synchronized boolean verifyCache() {
        if (Cache.STORE == null || !Cache.isFlatReadOnly())
            throw new IllegalStateException("950 hitbars require the paired flat cache");
        if (verifiedStore == Cache.STORE) return true;
        try {
            boolean valid = true;
            for (int i = 0; i < IDS.length; i++) {
                byte[] data = Cache.STORE.getIndexes()[2].getFile(72, IDS[i]);
                String actual = "missing";
                if (data != null) {
                    StringBuilder value = new StringBuilder();
                    for (byte b : MessageDigest.getInstance("SHA-256").digest(data))
                        value.append(String.format("%02x", b & 255));
                    actual = value.toString();
                }
                valid &= NativeCacheVerification.requireBinding("950 hitbar", "2/72/" + IDS[i], SHA256[i], actual);
            }
            int[] sprites = {15253, 15254, 34222, 34223};
            String[] expectedSprites = {"86de025817b8f28f5fc9f6cc01b429f2e1238ba756b5df8bf925b020cbfcb8a3", "f448ae2084769b58fa37705b50131682ff49e39448b8fa445abbfd984e4aa420", "db493c1ef3f25080ebba0f7d335608237be2d6a3680332e786edba982ce9b151", "e18ada43cda68abff39c5c25255a44026d3a676ce03255785ad2b0751d7ebfe6"};
            for (int i = 0; i < sprites.length; i++) {
                byte[] data = Cache.STORE.getIndexes()[8].getFile(sprites[i], 0);
                StringBuilder actual = new StringBuilder();
                if (data != null) for (byte b : MessageDigest.getInstance("SHA-256").digest(data))
                    actual.append(String.format("%02x", b & 255));
                valid &= NativeCacheVerification.requireBinding("950 mining bar sprite", "8/" + sprites[i] + "/0",
                        expectedSprites[i], data == null ? "missing" : actual.toString());
            }
            if (valid) verifiedStore = Cache.STORE;
            return valid;
        } catch (java.security.NoSuchAlgorithmException impossible) {
            throw new AssertionError(impossible);
        }
    }

    /** Runtime entry point. A standalone preflight JVM cannot populate this process's cache. */
    public static Result fromRunningCache(Entity entity) {
        List<HitBar> queued = entity == null ? null : entity.getNextHitBars();
        if (queued == null || queued.isEmpty()) return from(queued);
        if (Cache.STORE != null && verifyCache()) return from(queued);
        return new Result(Collections.<Native950PlayerMasks.Hitbar>emptyList(),
                Collections.<Native950NpcMasks.Hitbar>emptyList(), queued.size());
    }

    public static Result from(Entity entity) {
        return from(entity == null ? null : entity.getNextHitBars());
    }

    /** Only the exact EntityHitBar class is admitted: its display() is viewer independent.
     * Subclasses/timers/adrenaline/custom bars may change visibility or use unrelated IDs.
     * Exact MiningHitBar adds immutable player-only stamina/progress updates and removals.
     * Its own percentage uses hp*255/maxHp; do not replace that with a 0..100 or 0..256 scale.
     */
    public static Result from(List<? extends HitBar> queued) {
        List<Native950PlayerMasks.Hitbar> player = new ArrayList<>();
        List<Native950NpcMasks.Hitbar> npc = new ArrayList<>();
        int refused = 0;
        if (queued != null) for (HitBar bar : queued) {
            if (bar != null && bar.getClass() == Native950SmithingGauge.class) {
                Native950SmithingGauge gauge=(Native950SmithingGauge)bar;
                if(player.size()==255){refused++;continue;}
                player.add(gauge.isRemoval()?Native950PlayerMasks.Hitbar.remove(gauge.getType())
                        :Native950PlayerMasks.Hitbar.update(gauge.getType(),0,0,gauge.getPercentage(),gauge.getPercentage()));
                continue;
            }
            if (bar != null && bar.getClass() == MiningHitBar.class) {
                MiningHitBar mining = (MiningHitBar) bar;
                if (player.size() == 255) { refused++; continue; }
                player.add(mining.isRemoval() ? Native950PlayerMasks.Hitbar.remove(mining.getType())
                        : Native950PlayerMasks.Hitbar.update(mining.getType(), 0, 0, mining.getPercentage(), mining.getPercentage()));
                continue; // Mining gauges are player state, never NPC hitbars.
            }
            if (bar == null || bar.getClass() != EntityHitBar.class) { refused++; continue; }
            int id = bar.getType(), percent = bar.getPercentage();
            int target = bar.getToPercentage(), delay = bar.getDelay();
            if ((id != 0 && id != 3 && id != 4) || percent < 0 || percent > 255
                    || target != percent || delay != 0 || player.size() == 255) {
                refused++; continue;
            }
            // Exact EntityHitBar has no transition or delay. Native cycle=0 reuses percent1;
            // the signed-null smart size=-1 suppresses the optional quantity fields.
            player.add(Native950PlayerMasks.Hitbar.update(id, 0, 0, percent, percent));
            npc.add(Native950NpcMasks.Hitbar.update(id, 0, 0, percent, percent));
        }
        return new Result(player, npc, refused);
    }

    public static final class Result {
        private final List<Native950PlayerMasks.Hitbar> player;
        private final List<Native950NpcMasks.Hitbar> npc;
        private final int refused;
        private Result(List<Native950PlayerMasks.Hitbar> player, List<Native950NpcMasks.Hitbar> npc, int refused) {
            this.player = Collections.unmodifiableList(player);
            this.npc = Collections.unmodifiableList(npc);
            this.refused = refused;
        }
        public List<Native950PlayerMasks.Hitbar> playerBars() { return player; }
        public List<Native950NpcMasks.Hitbar> npcBars() { return npc; }
        public int refusals() { return refused; }
    }
}
