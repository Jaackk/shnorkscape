package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.filestore.store.Store;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.ObjectDefinitions;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.woodcutting.AxeDef;
import com.rs.game.player.actions.woodcutting.Woodcutting;
import com.rs.game.player.actions.woodcutting.WoodcuttingDefinitions.TreeDefinitions;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/** Cache admission for the original Woodcutting action; no second action scheduler. */
public final class Native950Woodcutting {
    private static final Properties PINS = pins();
    private static Store verifiedStore;
    private static final Map<String,Boolean> verified = new HashMap<>();
    private Native950Woodcutting() { }

    public static boolean isTree(WorldObject tree) { return definition(tree) != null; }
    public static boolean start(Player player, WorldObject tree) {
        if (player == null || !player.isNative950()) return false;
        TreeDefinitions type = definition(tree);
        return type != null && player.getActionManager().setAction(new Woodcutting(tree, type, null));
    }
    public static TreeDefinitions definition(WorldObject tree) {
        if (tree == null || Cache.STORE == null || !Cache.isFlatReadOnly() || !Native950Scenery.isHarvestableShape(tree)) return null;
        try {
            ObjectDefinitions d = tree.getDefinitions();
            if (!d.loaded || d.transforms != null || d.sizeX < 1 || d.sizeY < 1
                    || d.sizeX > 16 || d.sizeY > 16) return null;
            return definition(d.name, d.options);
        } catch (RuntimeException unavailable) { return null; }
    }
    /** Same semantic name/menu dispatch as the original ObjectHandler, read from950. */
    public static TreeDefinitions definition(String name, String[] options) {
        boolean chop = false;
        if (options != null) for (String option : options) chop |= "Chop down".equalsIgnoreCase(option);
        if (!chop || name == null) return null;
        switch (name.toLowerCase(Locale.ROOT)) {
            case "tree": return TreeDefinitions.NORMAL;
            case "evergreen": return TreeDefinitions.EVERGREEN;
            case "dead tree": return TreeDefinitions.DEAD;
            case "oak": return TreeDefinitions.OAK;
            case "willow": return TreeDefinitions.WILLOW;
            case "maple tree": return TreeDefinitions.MAPLE;
            case "mahogany": return TreeDefinitions.MAHOGANY;
            case "teak": return TreeDefinitions.TEAK;
            case "yew": return TreeDefinitions.YEW;
            case "magic tree": return TreeDefinitions.MAGIC;
            case "elder tree": return TreeDefinitions.ELDER;
            default: return null;
        }
    }
    /** A depleted/replaced world slot must not continue yielding from the raw map object. */
    public static boolean current(WorldObject tree) {
        if (tree == null || tree.getType() < 0 || tree.getType() > 22) return false;
        WorldObject actual = World.getRegion(tree.getRegionId()).getObjectWithType(
                tree.getPlane(), tree.getXInRegion(), tree.getYInRegion(), tree.getType());
        return actual != null && actual.getId() == tree.getId() && actual.getRotation() == tree.getRotation();
    }
    public static boolean inReach(Player player, WorldObject tree) {
        return player!=null && tree!=null && Native950Mining.inReach(player,tree);
    }
    public static AxeDef bestAxe(Player player) {
        int level=player.getSkills().getLevelForXp(Skills.WOODCUTTING);
        for (AxeDef axe:AxeDef.ALL.reverse()) {
            if ((player.getInventory().containsItem(axe.itemId,1)
                    || player.getEquipment().getWeaponId()==axe.itemId || Native950Toolbelt.has(player,axe.itemId))
                    && itemEntry(axe.itemId)!=null && animation(axe)>=0
                    && level>=strictItem(axe.itemId).getCSOpcode(771,axe.level)) return axe;
        }
        return null;
    }
    /** Only the paired-cache, named Lumberjack role is emitted; old regular IDs were repurposed. */
    public static int animation(AxeDef axe) {
        if (axe==null || !verify("item",axe.itemId,19,8) || !verify("sequence",axe.specialEmote,20,7)) return -1;
        ItemDefinitions item = strictItem(axe.itemId);
        return item!=null && item.getCSOpcode(8835,-1)==axe.specialEmote ? axe.specialEmote : -1;
    }
    public static boolean validStump(TreeDefinitions type) {
        if (type==TreeDefinitions.TEAK) return true; // Original definition deliberately removes the tree with no stump.
        int id=type.getStumpId();
        if (!verify("object",id,16,8)) return false;
        ObjectDefinitions d=ObjectDefinitions.getObjectDefinitions(id);
        return "Tree stump".equalsIgnoreCase(d.name) && d.transforms==null;
    }
    /** Exact950 item identities, stack semantics and Wield/Light operations for both skills. */
    public static Native950ItemCatalog.Entry itemEntry(int id) {
        if (!verify("item",id,19,8)) return null;
        ItemDefinitions item=strictItem(id);
        if (item==null || item.certTemplateId!=-1 || item.lendTemplateId!=-1 || item.bindTemplateId!=-1
                || item.shardTemplateId!=-1 || item.isStackable()) return null;
        boolean axe=false;for(AxeDef candidate:AxeDef.ALL)axe|=candidate.itemId==id;
        String[] options=new String[5];
        if (axe) {
            if (item.getEquipSlot()!=3 || !item.containsInventoryOption(1,"Wield")
                    || item.getMaleWornModelId1()<0 || item.getFemaleWornModelId1()<0) return null;
            options[1]="Wield";
            return new Native950ItemCatalog.Entry(id,item.getName(),false,options,3,2);
        }
        if (!item.containsInventoryOption(1,"Light")) return null;
        options[1]="Light";
        return new Native950ItemCatalog.Entry(id,item.getName(),false,options);
    }
    private static ItemDefinitions strictItem(int id) {
        try { return ItemDefinitions.decodeStrict947(id,Cache.STORE.getIndexes()[19].getFile(id>>>8,id&255),null); }
        catch (RuntimeException invalid) { return null; }
    }
    private static synchronized boolean verify(String kind,int id,int index,int shift) {
        if (Cache.STORE==null || !Cache.isFlatReadOnly()) return false;
        if (verifiedStore!=Cache.STORE) { verifiedStore=Cache.STORE;verified.clear(); }
        String key=kind+"."+id;
        if (!verified.containsKey(key)) {
            String expected=PINS.getProperty(key);boolean valid=false;
            if (expected!=null) try {
                byte[] bytes=Cache.STORE.getIndexes()[index].getFile(id>>>shift,id&((1<<shift)-1));
                if(bytes!=null) {
                    StringBuilder hex=new StringBuilder();
                    for(byte b:MessageDigest.getInstance("SHA-256").digest(bytes))hex.append(String.format("%02x",b&255));
                    valid=expected.equals(hex.toString());
                }
            } catch(Exception invalid) { valid=false; }
            verified.put(key,valid);
        }
        return verified.get(key);
    }
    private static Properties pins() {
        Properties result=new Properties();
        try(InputStream in=Native950Woodcutting.class.getResourceAsStream("/native950/woodcutting-assets-950.properties")) {
            if(in==null)throw new IllegalStateException("Missing950 woodcutting bindings");
            result.load(in);return result;
        } catch(java.io.IOException invalid) {throw new ExceptionInInitializerError(invalid);}
    }
}
