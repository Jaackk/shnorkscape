package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.filestore.store.Store;
import com.rs.cache.loaders.ObjectDefinitions;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.Region;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.item.floor.FloorItem;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.firemaking.Firemaking;
import com.rs.game.player.actions.firemaking.defs.Log;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/** Native boundary for the original Firemaking/Bonfire actions and their shared Log definitions. */
public final class Native950Firemaking {
    private static final Properties PINS = pins();
    private static final String PENDING="native950.firemaking.pending";
    private static Store verifiedStore;
    private static final Map<String,Boolean> verified = new HashMap<String,Boolean>();
    private static final Access LIVE = new Access() {
        public boolean supported(Log log) { return supports(log); }
        public boolean clear(WorldTile tile) {
            return World.canMoveNPC(tile.getPlane(),tile.getX(),tile.getY(),1)
                    && World.getObjectWithSlot(tile, Region.OBJECT_SLOT_FLOOR) == null;
        }
        public FloorItem drop(Player player, Log log, WorldTile tile) {
            FloorItem pile=World.addGroundItem(new Item(log.getLogId(),1),tile,player,true,60,0,false);
            if (pile != null) pile.setPublicTransferAllowed(com.rs.game.player.content.ItemConstants.isTradeable(pile));
            return pile;
        }
        public boolean present(FloorItem pile) {
            for (FloorItem item : World.getRegion(pile.getTile().getRegionId()).getGroundItemsSafe())
                if (item == pile) return true;
            return false;
        }
        public boolean remove(FloorItem pile) { return World.removeGroundItem(pile); }
        public void fire(Log log, WorldTile tile) {
            World.spawnTempGroundObject(new WorldObject(log.getFireId(),10,0,tile.getX(),tile.getY(),tile.getPlane()),592,log.getLife(),true);
        }
        public void later(Runnable task) { WorldTasksManager.schedule(new WorldTask() {
            @Override public void run() { task.run(); }
        },1); }
        public void consume(Player player, Log log) { Native950Skilling.consumeItem(player,log.getLogId(),1); }
        public void stepAside(Player player) {
            if (!player.addWalkSteps(player.getX()-1,player.getY(),1))
                if (!player.addWalkSteps(player.getX()+1,player.getY(),1))
                    if (!player.addWalkSteps(player.getX(),player.getY()+1,1))
                        player.addWalkSteps(player.getX(),player.getY()-1,1);
        }
        public boolean fireExists(WorldObject object) { return isFireObject(object) && World.getObjectWithType(object,object.getType()) == object; }
    };
    private Native950Firemaking() { }

    interface Access {
        boolean supported(Log log);
        boolean clear(WorldTile tile);
        FloorItem drop(Player player,Log log,WorldTile tile);
        boolean present(FloorItem pile);
        boolean remove(FloorItem pile);
        void fire(Log log,WorldTile tile);
        void later(Runnable task);
        void consume(Player player,Log log);
        void stepAside(Player player);
        boolean fireExists(WorldObject object);
    }

    /** Protean actions have a separate resource/XP rule; ordinary authored logs use this path. */
    public static boolean isSupportedLog(int itemId) { return supports(Log.forId(itemId)); }

    public static boolean supports(Log log) {
        if (log == null || log == Log.PROTEAN || log == Log.EVIL_BARK || log.getExperience() <= 0) return false;
        try {
            Native950ItemCatalog.Entry item = itemEntry(log.getLogId());
            return item != null && asset("object",log.getFireId())
                    && asset("sequence",16700) && asset("sequence",16703)
                    && itemEntry(590) != null && itemEntry(592) != null;
        } catch (RuntimeException unavailable) { return false; }
    }

    /** Current-cache inventory metadata, without requiring unrelated loot identity/menu policy. */
    public static Native950ItemCatalog.Entry itemEntry(int id) {
        if (id == 3239 || !asset("item",id)) return null; // Old Evil bark is now ordinary Bark.
        try {
            byte[] raw=Cache.STORE.getIndexes()[19].getFile(id>>>8,id&255);
            ItemDefinitions item=ItemDefinitions.decodeStrict947(id,raw,null);
            if (item.certTemplateId != -1 || item.lendTemplateId != -1 || item.bindTemplateId != -1
                    || item.shardTemplateId != -1 || item.name == null || item.name.trim().isEmpty()) return null;
            String[] supportedOptions=new String[5];
            if (Log.forId(id) != null && item.inventoryOptions != null)
                for (int index=0;index<Math.min(5,item.inventoryOptions.length);index++)
                    if ("Light".equalsIgnoreCase(item.inventoryOptions[index])) supportedOptions[index]=item.inventoryOptions[index];
            return new Native950ItemCatalog.Entry(id,item.name,item.isStackable(),supportedOptions);
        } catch (RuntimeException unavailable) { return null; }
    }

    public static boolean isFireObject(WorldObject object) {
        if (object == null || object.getType() != 10 || !asset("object",object.getId())) return false;
        ObjectDefinitions definition=object.getDefinitions();
        return definition.transforms == null && "Fire".equalsIgnoreCase(definition.name)
                && definition.options != null && definition.options.length > 4
                && "Use".equalsIgnoreCase(definition.options[4]);
    }

    public static Attempt begin(Player player, Log log) { return begin(player,log,LIVE); }
    static Attempt begin(Player player, Log log, Access access) {
        cancelPending(player);
        if (!requirements(player,log,access,true)) return null;
        if (!Firemaking.locationAllowsFire(player) || !access.clear(player)) {
            player.getPackets().sendGameMessage("You cannot light a fire here; find a different area.");return null;
        }
        WorldTile tile=new WorldTile(player);
        int amount=player.getInventory().getAmountOf(log.getLogId());
        access.consume(player,log);
        if (player.getInventory().getAmountOf(log.getLogId()) != amount-1) return null;
        FloorItem pile=access.drop(player,log,tile);
        if (pile == null) throw new IllegalStateException("Firemaking log was not placed in the world");
        Long previous=(Long)player.getTemporaryAttributtes().remove("Fire");
        boolean quick=previous != null && previous > com.rs.utils.Utils.currentTimeMillis();
        player.setNextAnimation(new Animation(quick ? -1 : 16700));
        player.getPackets().sendGameMessage("You attempt to light the logs.",true);
        Attempt attempt=new Attempt(player,log,tile,pile,quick,access);
        player.getTemporaryAttributtes().put(PENDING,attempt);
        return attempt;
    }

    /** Explicit user input or session close also retires a completed action's deferred ignition. */
    public static void cancelPending(Player player) {
        if (player == null) return;
        Object pending=player.getTemporaryAttributtes().remove(PENDING);
        if (pending instanceof Attempt) ((Attempt)pending).finished=true;
    }

    private static boolean requirements(Player player,Log log,Access access,boolean needLog) {
        if (player == null || !player.isNative950() || player.isDead() || player.hasFinished()
                || !player.isActive() || player.isLocked() || player.getNextWorldTile() != null || !access.supported(log)) return false;
        if (!player.getInventory().containsItem(590,1) && !Native950Toolbelt.has(player,590)) {
            player.getPackets().sendGameMessage("You need a tinderbox to light logs."); return false;
        }
        if (player.getSkills().getLevel(Skills.FIREMAKING) < log.getLevel()) {
            player.getPackets().sendGameMessage("You need level "+log.getLevel()+" Firemaking to burn these logs.");return false;
        }
        return !needLog || player.getInventory().containsItem(log.getLogId(),1);
    }

    /** Tracks the exact original owned pile: picking it up or replacing it cannot create free XP or a fire. */
    public static final class Attempt {
        private final Player player;
        private final Log log;
        private final WorldTile tile;
        private final FloorItem pile;
        private final Access access;
        private final boolean quick;
        private boolean queued,finished;
        private Attempt(Player player,Log log,WorldTile tile,FloorItem pile,boolean quick,Access access) {
            this.player=player;this.log=log;this.tile=tile;this.pile=pile;this.quick=quick;this.access=access;
        }
        public int delay() { return quick ? 1 : 2; }
        public boolean process() {
            return !queued && !finished && requirements(player,log,access,false)
                    && player.matches(tile) && access.clear(tile) && access.present(pile);
        }
        public void finish() {
            if (!process()) return;
            queued=true;
            access.stepAside(player);
            access.later(() -> {
                if (finished) return;
                finished=true;
                if (player.getTemporaryAttributtes().get(PENDING) != this) return;
                player.getTemporaryAttributtes().remove(PENDING);
                if (!player.isActive() || player.hasFinished() || player.isDead() || player.isLocked()
                        || player.getRealChannel() == null || !player.getRealChannel().isActive()
                        || player.getPlane() != tile.getPlane() || !player.withinDistance(tile,1)) return;
                if (!access.present(pile) || !access.clear(tile) || !access.remove(pile)) return;
                access.fire(log,tile);
                award(player,log);
                player.setNextFaceWorldTile(tile);
                player.getTemporaryAttributtes().put("Fire",com.rs.utils.Utils.currentTimeMillis()+1800);
                player.getPackets().sendGameMessage("The fire catches and the logs begin to burn.",true);
            });
        }
        public void stop() {
            if (!queued) {
                finished=true; // Cancellation leaves the original owned log available to Take.
                if (player.getTemporaryAttributtes().get(PENDING) == this) player.getTemporaryAttributtes().remove(PENDING);
            }
            player.setNextAnimation(new Animation(-1));
        }
    }

    public static boolean checkBonfire(Player player,Log log,WorldObject object) {
        return checkBonfire(player,log,object,LIVE);
    }
    static boolean checkBonfire(Player player,Log log,WorldObject object,Access access) {
        if (player == null || !player.isNative950() || player.isDead() || player.hasFinished() || !player.isActive()
                || player.isLocked() || player.getNextWorldTile() != null || log == null || !access.supported(log) || object == null || !access.fireExists(object)
                || player.getPlane() != object.getPlane() || !player.withinDistance(object,1)
                || !player.getInventory().containsItem(log.getLogId(),1)) return false;
        if (player.getSkills().getLevel(Skills.FIREMAKING) < log.getLevel()) {
            player.getPackets().sendGameMessage("You need level "+log.getLevel()+" Firemaking to burn these logs.");return false;
        }
        return true;
    }
    public static int burnBonfireLog(Player player,Log log,WorldObject object) {
        return burnBonfireLog(player,log,object,LIVE);
    }
    static int burnBonfireLog(Player player,Log log,WorldObject object,Access access) {
        if (!checkBonfire(player,log,object,access)) return -1;
        int amount=player.getInventory().getAmountOf(log.getLogId());
        access.consume(player,log);
        if (player.getInventory().getAmountOf(log.getLogId()) != amount-1) return -1;
        award(player,log);
        player.setNextAnimation(new Animation(16703));
        if (Native950PlayerEffects.isVerifiedGraphic(log.getBonfireGFX()))
            player.setNextGraphics(new Graphics(log.getBonfireGFX()));
        player.setNextFaceWorldTile(object);
        player.getPackets().sendGameMessage("You add a log to the fire.",true);
        return 6; // Original ordinary Bonfire action cadence, processed by ActionManager.
    }
    public static void stopBonfire(Player player) {
        player.setNextAnimation(new Animation(asset("sequence",16702) ? 16702 : -1));
        player.clickedObject=null;
    }
    private static void award(Player player,Log log) {
        player.getSkills().addXp(Skills.FIREMAKING,Firemaking.increasedExperience(player,log.getExperience()));
        player.addLogsBurned();
    }

    private static synchronized boolean asset(String kind,int id) {
        if (!PINS.containsKey(kind+"."+id) || Cache.STORE == null || !Cache.isFlatReadOnly()) return false;
        if (verifiedStore != Cache.STORE) { verifiedStore=Cache.STORE;verified.clear(); }
        String key=kind+"."+id;
        if (!verified.containsKey(key)) {
            boolean valid=false;
            try {
                int index="object".equals(kind)?16:"item".equals(kind)?19:20,shift=index==20?7:8;
                byte[] raw=Cache.STORE.getIndexes()[index].getFile(id>>>shift,id&((1<<shift)-1));
                valid=matchesPin(key,raw);
            } catch (RuntimeException unavailable) { valid=false; }
            verified.put(key,valid);
        }
        return verified.get(key);
    }
    static boolean matchesPin(String key,byte[] raw) {
        String expected=PINS.getProperty(key);
        if (expected == null || raw == null) return false;
        try {
            StringBuilder hash=new StringBuilder();
            for (byte b:MessageDigest.getInstance("SHA-256").digest(raw)) hash.append(String.format("%02x",b&255));
            return expected.equals(hash.toString());
        } catch (java.security.NoSuchAlgorithmException impossible) { throw new AssertionError(impossible); }
    }
    private static Properties pins() {
        Properties pins=new Properties();
        try (InputStream input=Native950Firemaking.class.getResourceAsStream("/native950/firemaking-assets-950.properties")) {
            if (input == null) throw new IllegalStateException("Missing firemaking asset bindings");
            pins.load(input);return pins;
        } catch (java.io.IOException error) { throw new ExceptionInInitializerError(error); }
    }
}
