package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.filestore.store.Store;
import com.rs.cache.loaders.AnimationDefinitions;
import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.actions.HomeTeleport;
import com.rs.game.player.content.Magic;
import com.rs.game.activites.gambling.flowerpoker.FlowerPokerSession;
import com.rs.utils.Utils;
import java.security.MessageDigest;

/** Native effects and cancellation boundary around the original HomeTeleport ActionManager timeline. */
public final class Native950LodestoneTeleport {
    private static final int[] SEQUENCES = {16385,16386,16393};
    private static final int[] DURATIONS = {510,120,60};
    // All three raw index20 definitions are identical in the paired910 and950 caches.
    private static final String[] SHA256 = {
        "cc1cf761d3c8257436d38bb3c608680f0765ece282a4bf2b664e389940011400",
        "be1b466ae0350eeda46458102d2c29e78522c2f70f0fbc64f9c50d8f75914e59",
        "f383d321fd34f367ecb795532555a070cb46dd5dfe3d0531599ace3412a1fd2c"};
    private static Store checkedStore;
    private static boolean checkedAssets;
    private Native950LodestoneTeleport() { }

    interface Access { long now(); boolean assets(); boolean clear(WorldTile tile); }
    private static final Access LIVE = new Access() {
        public long now() { return Utils.currentTimeMillis(); }
        public boolean assets() { return verifyAssets(); }
        public boolean clear(WorldTile tile) {
            World.getRegion(tile.getRegionId(),true);
            return World.isFloorFree(tile.getPlane(),tile.getX(),tile.getY());
        }
    };

    /** The caller supplies the final safe arrival tile, already selected from the actual950 network. */
    public static boolean start(Player player, WorldTile arrival) { return start(player,arrival,LIVE); }

    static boolean start(Player player, WorldTile arrival, Access access) {
        String refusal = refusal(player,access.now(),false);
        if (refusal != null) { message(player,refusal); return false; }
        if (player.getNextWorldTile()!=null) {
            message(player,"Finish your current movement before teleporting.");return false;
        }
        if (!validTile(arrival) || !access.assets() || !access.clear(arrival)) {
            message(player,"That lodestone's arrival or teleport effects are unavailable in this cache."); return false;
        }
        // Successful selection uses the original ActionManager. Its stop callback retires the old action.
        player.resetWalkSteps(); player.setRouteEvent(null);
        boolean started=player.getActionManager().setAction(new HomeTeleport(arrival,new Journey(player,arrival,access)));
        if(started)player.getActionManager().setActionDelay(0);
        return started;
    }

    public static synchronized boolean verifyAssets() {
        if (Cache.STORE == null || !Cache.isFlatReadOnly()) return false;
        if (checkedStore == Cache.STORE) return checkedAssets;
        checkedStore = Cache.STORE; checkedAssets = false;
        try {
            for (int i=0;i<SEQUENCES.length;i++) {
                int id=SEQUENCES[i];
                byte[] raw=Cache.STORE.getIndexes()[20].getFile(id>>>7,id&127);
                if(raw==null || !SHA256[i].equals(hash(raw))) return false;
                AnimationDefinitions d=AnimationDefinitions.decodeStrict947(id,raw,null);
                int cycles=d.modernInt26b;
                if(d.anIntArray2153!=null)for(int duration:d.anIntArray2153)cycles+=duration;
                if(cycles!=DURATIONS[i])return false;
            }
            checkedAssets=Native950PlayerEffects.isVerifiedGraphic(3017)&&Native950PlayerEffects.isVerifiedGraphic(3018);
        } catch(RuntimeException missing) { return false; }
        return checkedAssets;
    }

    /** One trip's ownership; HomeTeleport alone owns the counter and ActionManager alone drives it. */
    public static final class Journey {
        private final Player owner;
        private final WorldTile arrival;
        private final Access access;
        private WorldTile origin,queuedArrival;
        private long ownedLock;
        private boolean arrived,stopped;
        Journey(Player owner,WorldTile arrival,Access access) {
            this.owner=owner;this.arrival=new WorldTile(arrival);this.access=access;
        }
        public boolean start(Player player) {
            if(player!=owner || stopped || player.getNextWorldTile()!=null) return false;
            String refusal=refusal(player,access.now(),false);
            if(refusal!=null){message(player,refusal);return false;}
            if(!player.getControlerManager().processMagicTeleport(arrival))return false;
            origin=new WorldTile(player);return true;
        }
        public boolean process(Player player) {
            if(player!=owner || origin==null || stopped)return false;
            boolean ourLock=ownedLock!=0 && player.getLockDelay()==ownedLock;
            String refusal=refusal(player,access.now(),ourLock);
            if(refusal!=null){message(player,refusal);return false;}
            WorldTile pending=player.getNextWorldTile();
            if(pending!=null && pending!=queuedArrival)return false;
            if(player.hasWalkSteps())return false;
            if(!same(player,arrived?arrival:origin) && !(arrived && pending==queuedArrival))return false;
            return true;
        }
        public void departureEffects(Player player) {
            player.setNextAnimation(new Animation(16385));player.setNextGraphics(new Graphics(3017));
        }
        public boolean teleport(Player player) {
            if(!process(player) || !access.clear(arrival)
                    || !player.getControlerManager().processMagicTeleport(arrival)) {
                message(player,"You cannot teleport to that lodestone right now.");return false;
            }
            if(player.getNative950Combat()!=null)player.getNative950Combat().stop(player);
            player.resetWalkSteps();player.setRouteEvent(null);
            player.lock(8);ownedLock=player.getLockDelay();
            queuedArrival=new WorldTile(arrival);player.setNextWorldTile(queuedArrival);arrived=true;
            player.getControlerManager().magicTeleported(Magic.MAGIC_TELEPORT);
            // Do not activate unported Wilderness, clan-war or dungeon controllers through Magic.teleControlersCheck.
            player.setNextFaceWorldTile(arrival.transform(0,-1,0));
            return true;
        }
        public void arrivalEffects(Player player) {
            player.setNextGraphics(new Graphics(3018));player.setNextAnimation(new Animation(16386));
        }
        public void land(Player player) {
            player.setNextAnimation(new Animation(16393));unlock(player);
        }
        public void stop(Player player) {
            if(player!=owner || stopped)return;
            stopped=true;
            if(queuedArrival!=null && player.getNextWorldTile()==queuedArrival)player.setNextWorldTile(null);
            player.setNextAnimation(new Animation(-1));Native950PlayerEffects.queueClear(player);unlock(player);
        }
        private void unlock(Player player) {
            if(ownedLock!=0 && player.getLockDelay()==ownedLock)player.unlock();
            ownedLock=0;
        }
    }

    private static String refusal(Player player,long now,boolean ourLock) {
        if(player==null || !player.isNative950() || !player.isActive() || player.hasFinished() || player.isDead())
            return "You cannot teleport right now.";
        if((player.isLocked()&&!ourLock) || player.isNative950ForceMovementActive()
                || player.getNextForceMovement()!=null || Boolean.TRUE.equals(player.getTemporaryAttributtes().get("teleporting")))
            return "Finish your current movement before teleporting.";
        if(player.getAttackedBy()!=null || player.getAttackedByDelay()+10000>now || player.getAttackingDelay()+10000>now)
            return "You cannot home teleport until 10 seconds after the end of combat.";
        if(player.getTemporaryAttributtes().get(FlowerPokerSession.PLAYING_FLOWER_POKER_KEY)==Boolean.TRUE)
            return "You are too busy to teleport.";
        return null;
    }
    private static boolean validTile(WorldTile tile) {
        return tile!=null&&tile.getX()>=0&&tile.getX()<=16383&&tile.getY()>=0&&tile.getY()<=16383
                &&tile.getPlane()>=0&&tile.getPlane()<=3;
    }
    private static boolean same(WorldTile a,WorldTile b){return b!=null&&a.getX()==b.getX()&&a.getY()==b.getY()&&a.getPlane()==b.getPlane();}
    private static void message(Player player,String message){if(player!=null)player.getPackets().sendGameMessage(message);}
    private static String hash(byte[] bytes){
        try{StringBuilder out=new StringBuilder();for(byte b:MessageDigest.getInstance("SHA-256").digest(bytes))out.append(String.format("%02x",b&255));return out.toString();}
        catch(java.security.NoSuchAlgorithmException impossible){throw new AssertionError(impossible);}
    }
}
