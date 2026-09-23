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

/** Cache-pinned native combat hitmarks in life points. */
public final class Native950Hits {
    public static final int LIFE_POINTS_PER_ENGINE_HP=10;
    private static final int[] IDS={0,1,2,14,15,16,51,52,53,103,104,105,
            133,134,136,137,139,140,150,151,153,154,156,157,458,464,477,478,480};
    private static final String[] HASHES={
        "0bcb6b16d9f3da7d610278e3b6dce71e6d5244fa7b94a7eba3a503b2e6a99b9b",
        "c00c3808ea40db900c62ef92fc3d35f5c6a5877c80d9ff49fd0eb00fe1b658b8",
        "b628ff5f72e79879fb9501986180117d123159cd14e06607a6b9b60662a4ebe2",
        "2c5cae07849cfb335cba144f55bb65403c79c9a0c64b56694da243fc0d845e07",
        "915d1c0c5ec6473c69a547c535f094aaac46e4f9f757b78dfbec019187ffafd6",
        "38c4e0c110e8e77663fd3e3839a2614be03d153a72a8221d56723de0bd9373f0",
        "8ba5221af343392dea276e8b96e9633b448e220318a533ac2db412fbadbaa705",
        "b34005bf25eda467321eefd08e7b3210b208a2e33ed8552e8e8e952d7e3c38dc",
        "5db69405c489f39fba8561622a215c568eb0ab85ff71575df6da92af05857443",
        "0881d0d273b0e1a483acf13da2ef672c48d33b027018993762a7a841d3f5629b",
        "cc5811e769fa208e927c689db5b3865f7a6699320b7a97918aee27d6855dc166",
        "7f36dd85e9cb60176b17098ee75fdbda9f4da95eefb346bd576795d5bccea787",
        "5c4e17c3da80f49416a5e5ba581d9392a6026d72ad76d9c8bd0e60f8f3cac08d",
        "9b9742cc7c9ab67592b9fc21b2d2dc5e1ac18a80f0703e256fb99348ef483fc9",
        "dbd8bdd2326697948d297531647694db6f9c13e508d9b47196f038d78e998873",
        "220c38a467066a0d2daf84ab95a40ef26c5471bb75c5a73b80e381d75ce329d5",
        "e5f4151421526c1c3261c7d585f7d2162db9207b3356b7012c3904d7dc8edd97",
        "d6f7afcf9218c2970c99dedc0ce3d10f03807bb8ac293a2bcbb0c2315b879d28",
        "8adb82f9b57f12783a516cf7bc7b5b3bbc4a9250530afa9c063865a6b03180d4",
        "6dbd97789c2360f217078ce635b715749b0e837d3a1f82f22e6d08dad255d7f6",
        "9fe39364f4c1acf23aac1e0a70b3b985af11fe48f03e37da438b7a25a3554bf1",
        "b8ecc9be3b285a39949862a27796c284ba53b39c871e684966a6dfe67b563fac",
        "4e4b030f5ef1e816e33278ce268594cb6608a6796e1bfd2d2d1c330befbbc7f7",
        "db2d421eed95a01386432aa0c215fb43ab189027fa83bf424712d04dfc299271",
        "fa0aacc41d29bd6dff34074609ad960b98c821320aff31299820e63d47775a0c",
        "fc0ba0f97fa3c173c16ac9841ff20a2702b1b46055bf2f80f4b8cf17a484956a",
        "f3fc7bd2bd0d7bad82a4ac2af539f2772a8d6e1baa2153b40809c80e109f37f0",
        "6b136765a312d9a0f8268aa4b55328e5d712b6efed7014433345be187df8d882",
        "162772692c60f5226d65b998ee5dd28d5ab4af4008891b239200befda681d9a0"};
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
                    || hit.isSpecialHit() || hit.getSoaking()!=null
                    || (hit.getLook()!=Hit.HitLook.MELEE_DAMAGE && hit.getLook()!=Hit.HitLook.RANGE_DAMAGE && hit.getLook()!=Hit.HitLook.MAGIC_DAMAGE
                        && hit.getLook()!=Hit.HitLook.NECROMANCY_DAMAGE && hit.getLook()!=Hit.HitLook.CONJURE_DAMAGE
                        && !(hit.getLook()==Hit.HitLook.MISSED && damage==0))) {
                refused++;continue;
            }
            entries.add(new Entry(playerIndex(hit.getSource()),playerIndex(victim),(int)damage,
                    hit.getLook(),hit.isCriticalHit()));
        }
        return new Snapshot(entries,refused);
    }
    private static int playerIndex(Entity entity) {
        return entity instanceof Player && entity.getIndex()>0 ? entity.getIndex() : -1;
    }
    private static final class Entry {
        final int source,victim,damage;
        final Hit.HitLook look;
        final boolean critical;
        Entry(int source,int victim,int damage,Hit.HitLook look,boolean critical){
            this.source=source;this.victim=victim;this.damage=damage;this.look=look;this.critical=critical;
        }
        int type(int viewer) {
            boolean involved=viewer>0 && (viewer==source || viewer==victim);
            if(damage==0)return involved?458:464;
            if(look==Hit.HitLook.NECROMANCY_DAMAGE)return critical?478:477;
            if(look==Hit.HitLook.CONJURE_DAMAGE)return 480;
            if(critical){
                if(look==Hit.HitLook.RANGE_DAMAGE)return involved?137:154;
                if(look==Hit.HitLook.MAGIC_DAMAGE)return involved?140:157;
                return involved?134:151;
            }
            if(look==Hit.HitLook.RANGE_DAMAGE)return involved?136:153;
            if(look==Hit.HitLook.MAGIC_DAMAGE)return involved?139:156;
            return involved?133:150;
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
