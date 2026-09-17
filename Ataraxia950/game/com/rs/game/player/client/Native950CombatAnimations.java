package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.filestore.store.Store;
import com.rs.game.player.content.Combat;
import java.security.MessageDigest;

/** Verified 950 sequences plus explicit basic RS2 cadence; see melee-rendering-950.md. */
public final class Native950CombatAnimations {
    private static final int[] SEQUENCES={422,425,836,5387,5388,5389,6182,6183,6184,37378,37385,18292};
    private static final Pin[] PINS={
        new Pin(20,3,38,"438610e5536c95f6f13116c501622c45f87c87effa69f15d462c4a03ec006800"),
        new Pin(20,3,41,"d9ab5334c7395031ead91c4a1c94ee8104285068d420513dae45d27a1299dc69"),
        new Pin(20,6,68,"b77d721a5b172cc442ca6576eb89bd164a419a4728283d680a7b153af45ae422"),
        new Pin(20,42,11,"e48c5d1f3a81f3713dbfdb27567bf17a8769f7e350e3500cbfc1fd97151df5cf"),
        new Pin(20,42,12,"82d5d37620fbda84ec399b9e2a0c7a731a033b8b922232a69a591fb64c711b80"),
        new Pin(20,42,13,"f688b472b9989b990943b250b70f36588ceae2d8634ef01999820286be3add9d"),
        new Pin(20,48,38,"40a8463b6df19387911c319068f5440f03c3f200c80a558e4ec0a26b4e0608b0"),
        new Pin(20,48,39,"87363a3686cb8644c973efcfd1111bfd4fac39ce8fca31893830c88757ce01e9"),
        new Pin(20,48,40,"dad7b9fc86262d8be6896e4986c04f12d7b72d1ec7f04c0f18e6f61e2f2aa5c9"),
        new Pin(20,292,2,"f27bb87e0d139983460fc67e6251e37d69c428517aaa7f8a631ccec71b79521a"),
        new Pin(20,292,9,"0fae2290f5bef2e017b1ae6535c9c812acfd4af86cdd8e13177be7d9b1ec84a3"),
        new Pin(20,142,116,"d3d8373e57c0442d519ac3386c9996d5ee4b16920899f433f2d55cbf3560727f"),
        new Pin(19,4,253,"6433fee53de4734fc83e785825ad1219a2591d3134e33e5abba3491fffc5d95b"),
        new Pin(19,5,11,"778b1e30f8c1a89b176f6041506cd0bff581e7de318fbcab4d097e449a6274e9"),
        new Pin(19,5,41,"89f81bd5e7451651d51ef9f9c0553cfc9de9d612299a07907068362e05730289"),
        new Pin(22,466,10,"b0013183c9cac8ac3806b6f4fcb018a51b4ecbd1756a94d8246c3c81c42a2bbf"),
        new Pin(22,466,11,"603237a4b47e1de9f61c5f7fed11a58d9dffca7c907a83fc2630d921cade0444"),
        new Pin(22,466,12,"c4fc59a127a7ea3701506709c423c517af35ee9ae3f86c686ed4abe8f7783ff3")
    };
    private static Store verifiedStore;
    private Native950CombatAnimations() { }
    public static synchronized boolean verifyCache() {
        if(Cache.STORE==null || !Cache.isFlatReadOnly())throw new IllegalStateException("950 melee animations require the paired flat cache");
        if(verifiedStore==Cache.STORE)return true;
        boolean valid=true;
        for(Pin pin:PINS) valid &= NativeCacheVerification.requireBinding("950 melee animation catalog",
                pin.index+"/"+pin.group+"/"+pin.file,pin.hash,hash(Cache.STORE.getIndexes()[pin.index].getFile(pin.group,pin.file)));
        if(valid)verifiedStore=Cache.STORE;
        return valid;
    }
    public static boolean acceptedFromRunningCache(int sequenceId) {
        boolean supported=false;
        for(int id:SEQUENCES)if(id==sequenceId){supported=true;break;}
        return supported && Cache.STORE!=null && verifyCache();
    }
    public static boolean supportsWeapon(int id){return id==-1 || id==1277 || id==1291 || id==1321;}
    private static void requireWeapon(int id){if(!supportsWeapon(id))throw new IllegalArgumentException("Unported basic melee weapon "+id);}
    /** Actual950 combat map2914, or the explicit legacy unarmed punch422. */
    public static int attackAnimation(int weaponId){requireWeapon(weaponId);return weaponId==-1?422:weaponId==1277?37378:37385;}
    /** Actual950 weapon map2917; unarmed uses the established legacy block425. */
    public static int blockAnimation(int weaponId){requireWeapon(weaponId);return weaponId==-1?425:18292;}
    public static int deathAnimation(){return 836;}
    /** Gameplay policy in600ms ticks, not a claim that the modern cache uses RS2 speed. */
    public static int attackSpeed(int weaponId){requireWeapon(weaponId);return weaponId==1291?5:4;}
    /** Combat.STAB_STYLE=5, SLASH_STYLE=6, CRUSH_STYLE=7. */
    public static int attackStyle(int weaponId){requireWeapon(weaponId);return weaponId==-1?Combat.CRUSH_STYLE:weaponId==1277?Combat.STAB_STYLE:Combat.SLASH_STYLE;}
    /** Raw20ms duration from SeqType1/26; player836 includes a5000-cycle final-pose hold. */
    public static int durationCycles(int sequenceId){
        switch(sequenceId){
            case 422:return 39;
            case 425:return 44;
            case 836:return 5119;
            case 5387:return 35;
            case 5388:return 56;
            case 5389:return 153;
            case 6182:return 90;
            case 6183:return 30;
            case 6184:return 55;
            case 37378:return 60;
            case 37385:return 60;
            case 18292:return 22;
            default:throw new IllegalArgumentException("Unverified melee sequence "+sequenceId);
        }
    }
    private static String hash(byte[] data){
        if(data==null)return "missing";
        try{StringBuilder out=new StringBuilder();for(byte b:MessageDigest.getInstance("SHA-256").digest(data))out.append(String.format("%02x",b&255));return out.toString();}
        catch(java.security.NoSuchAlgorithmException impossible){throw new AssertionError(impossible);}
    }
    private static final class Pin {
        final int index,group,file;final String hash;
        Pin(int index,int group,int file,String hash){this.index=index;this.group=group;this.file=file;this.hash=hash;}
    }
}
