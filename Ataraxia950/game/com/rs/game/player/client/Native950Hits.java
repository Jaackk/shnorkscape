package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.filestore.store.Store;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950PlayerMasks;
import com.rs.network.protocol.modern950.Native950NpcMasks;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Bounded ordinary melee display in native life points; see melee-rendering-950.md. */
public final class Native950Hits {
    public static final int LIFE_POINTS_PER_ENGINE_HP=10;
    private static final int[] IDS={0,14,458,464};
    private static final String[] HASHES={
        "0bcb6b16d9f3da7d610278e3b6dce71e6d5244fa7b94a7eba3a503b2e6a99b9b",
        "2c5cae07849cfb335cba144f55bb65403c79c9a0c64b56694da243fc0d845e07",
        "fa0aacc41d29bd6dff34074609ad960b98c821320aff31299820e63d47775a0c",
        "fc0ba0f97fa3c173c16ac9841ff20a2702b1b46055bf2f80f4b8cf17a484956a"};
    private static final int[] ZERO_SPRITES={23354,23355,23356,23357,23358,23359};
    private static final String[] ZERO_SPRITE_HASHES={
        "d01f7b914dc73f7a3c8f8232772703d4c35f4ba84c0d4ffc48f2f818d849e713",
        "48473739f04cacbf0ed3f5b1edfee11faf93b1aadd732bea3d019f6e27e14327",
        "234cfbafbb1ddd25a8abc633983367f03bafa18c985a1a03518f11df397b55ab",
        "8b30ecbe157012c0de26e6668f13a91901839598310dcd61b2cf9d5108509bab",
        "3918f4cbae927ccb9c21e26d7198aed4e0598ecbd081385ff212bc1c63748bea",
        "616a746bbdabcb22ea96b17db4e92817a0f090275fab31200051e7102bbc050a"};
    private static Store verifiedStore;
    private Native950Hits() { }
    public static synchronized boolean verifyCache() {
        if(Cache.STORE==null || !Cache.isFlatReadOnly())
            throw new IllegalStateException("950 melee hitmarks require the paired flat cache");
        if(verifiedStore==Cache.STORE)return true;
        boolean valid=true;
        for(int i=0;i<IDS.length;i++)
            valid &= NativeCacheVerification.requireBinding("950 ordinary melee hitmark","2/46/"+IDS[i],
                    HASHES[i],hash(Cache.STORE.getIndexes()[2].getFile(46,IDS[i])));
        for(int i=0;i<ZERO_SPRITES.length;i++)
            valid &= NativeCacheVerification.requireBinding("950 numeric zero sprite","8/"+ZERO_SPRITES[i]+"/0",
                    ZERO_SPRITE_HASHES[i],hash(Cache.STORE.getIndexes()[8].getFile(ZERO_SPRITES[i],0)));
        if(valid)verifiedStore=Cache.STORE;
        return valid;
    }
    private static String hash(byte[] data) {
        if(data==null)return "missing";
        try {
            StringBuilder out=new StringBuilder();
            for(byte b:MessageDigest.getInstance("SHA-256").digest(data))out.append(String.format("%02x",b&255));
            return out.toString();
        } catch(java.security.NoSuchAlgorithmException impossible){throw new AssertionError(impossible);}
    }
    public static Snapshot fromRunningCache(Entity victim) {
        List<Hit> hits=victim==null?null:victim.getNextHits();
        if(hits==null || hits.isEmpty())return from(victim);
        if(Cache.STORE!=null && verifyCache())return from(victim);
        return new Snapshot(Collections.<Entry>emptyList(),hits.size());
    }
    /** Pure immutable snapshot; live callers must use fromRunningCache. */
    public static Snapshot from(Entity victim) {
        List<Entry> entries=new ArrayList<>();int refused=0;
        List<Hit> hits=victim==null?null:victim.getNextHits();
        if(hits!=null)for(Hit hit:hits) {
            if(hit==null){refused++;continue;}
            long damage=(long)hit.getDamage()*LIFE_POINTS_PER_ENGINE_HP;
            // Delays in arbitrary legacy content have not been assigned a native clock unit.
            // The basic loop applies damage and queues its display on the same world tick.
            if(entries.size()==255 || damage<0 || damage>32767 || hit.getDelay()!=0
                    || hit.isCriticalHit() || hit.isSpecialHit() || hit.getSoaking()!=null
                    || (hit.getLook()!=Hit.HitLook.MELEE_DAMAGE && hit.getLook()!=Hit.HitLook.RANGE_DAMAGE && hit.getLook()!=Hit.HitLook.MAGIC_DAMAGE
                        && !(hit.getLook()==Hit.HitLook.MISSED && damage==0))) {
                refused++;continue;
            }
            entries.add(new Entry(playerIndex(hit.getSource()),playerIndex(victim),(int)damage));
        }
        return new Snapshot(entries,refused);
    }
    private static int playerIndex(Entity entity) {
        return entity instanceof Player && entity.getIndex()>0 ? entity.getIndex() : -1;
    }
    private static final class Entry {
        final int source,victim,damage;
        Entry(int source,int victim,int damage){this.source=source;this.victim=victim;this.damage=damage;}
        int type(int viewer) {
            boolean involved=viewer>0 && (viewer==source || viewer==victim);
            // Positive damage uses standard targets of wrappers133/150. Zero uses the literal
            // blue0 targets of wrappers482/492; old miss141/158 targets now say "Dodged".
            // Direct definitions avoid changing client preferences or unrelated damage scaling.
            return damage==0 ? (involved?458:464) : (involved?0:14);
        }
    }
    public static final class Snapshot {
        private final List<Entry> entries;
        private final int refused;
        private Snapshot(List<Entry> entries,int refused){this.entries=Collections.unmodifiableList(new ArrayList<>(entries));this.refused=refused;}
        public int refusals(){return refused;}
        public int size(){return entries.size();}
        public List<Native950PlayerMasks.Hit> playerHits(int viewerIndex) {
            List<Native950PlayerMasks.Hit> result=new ArrayList<>(entries.size());
            for(Entry e:entries)result.add(Native950PlayerMasks.Hit.of(e.type(viewerIndex),e.damage,0));
            return Collections.unmodifiableList(result);
        }
        public List<Native950NpcMasks.Hit> npcHits(int viewerIndex) {
            List<Native950NpcMasks.Hit> result=new ArrayList<>(entries.size());
            for(Entry e:entries)result.add(Native950NpcMasks.Hit.of(e.type(viewerIndex),e.damage,0));
            return Collections.unmodifiableList(result);
        }
    }
}
