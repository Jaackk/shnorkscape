package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.WorldObject;
import com.rs.cache.loaders.ObjectDefinitions;
import java.util.ArrayList;
import java.util.List;
import com.rs.game.Graphics;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.Channel;
import com.rs.game.player.Skills;
import com.rs.game.player.content.Burying.Bone;
import com.rs.game.player.content.items.AshScattering.AshesData;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/** Native ordinary offerings, sharing the910 Bone/AshesData tables and original Skills XP owner. */
public final class Native950Prayer {
    private static final Properties PINS=loadPins();
    private static final Item[] NOTHING=new Item[0];
    private static final Map<Integer,Native950ItemCatalog.Entry> ENTRIES=new HashMap<>();
    private static Object store;
    private Native950Prayer() { }

    /** The cache's normal Prayer/Curses grid; the existing Prayer owner remains authoritative. */
    // OpenNXT's revision-950 bootstrap installs the grid event mask on 1458:39.  It is
    // the clickable child that backs the visible Prayer icons; 33 is only the container.
    static void enableInterface(Channel channel){channel.write(Native950Packets.interfaceEvents(1458,39,0,38,8388610));}
    static boolean button(Player player,Native950Actions.InterfaceAction action){
        if(action.interfaceId()==1430&&action.componentId()==16){
            if(!player.isDead()&&!player.isLocked()){
                if(action.option()==1)player.getPrayer().delaySwitchQuickPrayers();
                else if(action.option()==2)player.getPrayer().openPrayerPresetsInterface();
                else player.getPrayer().delaySwitchQuickPrayers(quickPrayerPreset(action.option()));
                Native950BugTest.event(player,"prayer","orb-click","option",action.option());
            }
            return true;
        }
        if(action.interfaceId()!=1458||action.componentId()!=39)return false;
        if(action.option()==1&&action.slot()>=0&&action.slot()<=38&&!player.isDead()&&!player.isLocked()) {
            Native950BugTest.event(player,"prayer","click","slot",action.slot(),"points",player.getPrayer().getPrayerpoints());
            player.getPrayer().delayUsePrayer(action.slot(),false);
        }
        return true;
    }
    static int quickPrayerPreset(int option){return option==3?0:option==4?1:option==5?2:option==9?3:option==6?4:option==7?5:option==10?6:7;}

    /** Retains only the actual ordinary offering operation, not unported Craft/Grind/etc. */
    public static synchronized Native950ItemCatalog.Entry itemEntry(int id) {
        if(Cache.STORE==null || !Cache.isFlatReadOnly() || !PINS.containsKey("item."+id))return null;
        if(store!=Cache.STORE) { ENTRIES.clear();store=Cache.STORE; }
        if(ENTRIES.containsKey(id))return ENTRIES.get(id);
        Native950ItemCatalog.Entry result=null;
        try {
            Bone bone=Bone.forId(id);AshesData ashes=AshesData.forId(id);
            String option=bone!=null ? "Bury" : ashes!=null ? "Scatter" : null;
            byte[] raw=file(19,id>>>8,id&255);
            if(option!=null && pin("item."+id,raw) && sequence(bone!=null ? 827 : 445)
                    && (bone!=null || Native950PlayerEffects.isVerifiedGraphic(ashes.getGFX()))) {
                ItemDefinitions definition=ItemDefinitions.decodeStrict947(id,raw,null);
                if(definition.certTemplateId==-1 && definition.lendTemplateId==-1 && definition.bindTemplateId==-1
                        && definition.shardTemplateId==-1 && definition.name!=null && !definition.name.trim().isEmpty()
                        && definition.inventoryOptions!=null && definition.inventoryOptions.length>0
                        && option.equalsIgnoreCase(definition.inventoryOptions[0])) {
                    String[] options=new String[5];options[0]=option;
                    result=new Native950ItemCatalog.Entry(id,definition.name,definition.isStackable(),options);
                }
            }
        } catch(RuntimeException unavailable) { result=null; }
        ENTRIES.put(id,result);return result;
    }
    public static boolean offer(Player player,int slot,int itemId,int option) {
        return offer(player,slot,itemId,option,itemEntry(itemId));
    }
    // Tests substitute cache identity only; the real container transaction and Skills commit remain.
    static boolean offer(Player player,int slot,int itemId,int option,Native950ItemCatalog.Entry definition) {
        if(player==null || !player.isNative950() || !player.isActive() || player.hasFinished() || player.isDead()
                || player.isLocked() || player.closeInterfaceLocked || player.isNative950ForceMovementActive()
                || player.getNextForceMovement()!=null || player.getNextWorldTile()!=null || player.hasTeleported()
                || slot<0 || slot>=28 || option!=1 || definition==null || definition.id!=itemId)return false;
        Bone bone=Bone.forId(itemId);AshesData ashes=AshesData.forId(itemId);
        String verb=bone!=null ? "Bury" : ashes!=null && itemId!=3325 ? "Scatter" : null;
        if(verb==null || !verb.equalsIgnoreCase(definition.option(option)))return false;
        Item held=player.getInventory().getItem(slot);
        if(held==null || held.getId()!=itemId || held.getAmount()<1 || held.getAttributes()!=null || held.getCharges()!=0)return false;
        // Commit consumption before XP/effects. Missing containers or a controller veto do nothing.
        if(!Native950Skilling.exchange(player,new Item[]{new Item(itemId,1)},NOTHING))return false;
        Native950Firemaking.cancelPending(player);
        player.getActionManager().forceStop();player.setRouteEvent(null);player.resetWalkSteps();
        player.lock(1);
        player.setNextAnimation(new Animation(bone!=null ? 827 : 445));
        if(ashes!=null)player.setNextGraphics(new Graphics(ashes.getGFX()));
        player.addBonesOffered();
        player.getSkills().addXp(Skills.PRAYER,bone!=null ? bone.getExperience() : ashes.getExp());
        player.getPackets().sendGameMessage("You "+verb.toLowerCase(Locale.ROOT)+" the "+definition.name.toLowerCase(Locale.ROOT)+".");
        return true;
    }
    // Ordinary, natural Saradomin altars verified in the paired scene. Do not guess POH/quest/GWD multipliers.
    private static final int[] ALTARS={409,2640,24343};
    public static boolean isAltar(WorldObject object) {
        if(object==null||object.getType()!=10||Cache.STORE==null||!Cache.isFlatReadOnly())return false;
        boolean admitted=false;for(int id:ALTARS)admitted|=object.getId()==id;if(!admitted)return false;
        ObjectDefinitions d=object.getDefinitions();
        return d.loaded&&d.transforms==null&&"Altar".equals(d.name)&&d.sizeX==2&&d.sizeY==1
            &&d.options!=null&&d.options.length>1&&"Pray at".equals(d.options[0])&&"Offer".equals(d.options[1])
            &&pin("object."+object.getId(),file(16,object.getId()>>>8,object.getId()&255))&&sequence(896)&&sequence(645);
    }
    public static boolean accepts(WorldObject object,int option){return (option==1||option==2)&&isAltar(object);}
    public static boolean acceptsItem(WorldObject object,int id){return isAltar(object)&&itemEntry(id)!=null&&id!=3325;}
    public static boolean inReach(Player player,WorldObject object){
        return Native950Production.Recipe.ready(player)&&player.getNextWalkDirection()==-1&&isAltar(object)
            &&Native950Mining.current(object)&&Native950Mining.inReach(player,object);
    }
    /** The native Prayer object still owns points and their varbit synchronization. */
    public static boolean recharge(Player player,WorldObject object){
        if(!inReach(player,object))return false;
        int maximum=player.getSkills().getLevelForXp(Skills.PRAYER)*10+(object.getId()==2640?20:0);
        if(player.getPrayer().getPrayerpoints()>=maximum){player.sendMessage("You already have full Prayer points.");return true;}
        player.getActionManager().forceStop();player.setNextFaceWorldTile(object);player.setNextAnimation(new Animation(645));player.lock(1);
        player.getPrayer().setPrayerpoints(maximum);player.getPrayer().refreshPrayerPoints();
        player.sendMessage("You recharge your Prayer points.");return true;
    }
    public static List<Native950ProductionMenu.Choice> choices(Player player,WorldObject object){
        List<Native950ProductionMenu.Choice> choices=new ArrayList<>();if(!inReach(player,object))return choices;
        java.util.Set<Integer> seen=new java.util.LinkedHashSet<>();
        for(Item item:player.getInventory().items.getItems())if(item!=null&&seen.add(item.getId())&&acceptsItem(object,item.getId())){
            int id=item.getId();Native950Production.Recipe recipe=altarRecipe(id);
            choices.add(new Native950ProductionMenu.Choice("Offer "+Native950Production.name(id),recipe,n->startAltar(player,object,id,n)));
        }
        if(choices.isEmpty())player.sendMessage("Bring unnoted bones or demonic ashes to offer at this altar.");
        return choices;
    }
    static Native950Production.Recipe altarRecipe(int itemId){
        if(itemId==3325||itemEntry(itemId)==null)return null;
        Bone bone=Bone.forId(itemId);AshesData ashes=AshesData.forId(itemId);
        if(bone==null&&ashes==null)return null;
        // Preserve this server's910 base experience; ordinary950 altars add50%, not the old blanket2.5x.
        double xp=(bone!=null?bone.getExperience():ashes.getExp())*1.5;
        return new Native950Production.Recipe("Offer "+Native950Production.name(itemId),Skills.PRAYER,1,xp,896,3,
            new Item[]{new Item(itemId,1)},NOTHING);
    }
    public static boolean startAltar(Player player,WorldObject object,int itemId,int quantity){
        if(quantity<1||quantity>10000||!inReach(player,object)||!acceptsItem(object,itemId))return false;
        Native950Production.Recipe recipe=altarRecipe(itemId);if(recipe==null)return false;
        WorldObject target=new WorldObject(object);player.setNextFaceWorldTile(target);
        return player.getActionManager().setAction(new Native950ProductionAction(recipe,quantity){
            @Override protected boolean environment(Player p){return inReach(p,target);}
            @Override public int processWithDelay(Player p){int delay=super.processWithDelay(p);if(delay>=0)p.addBonesOffered();return delay;}
        });
    }
    public static void verifyCacheBindings(){
        for(int id:ALTARS)if(!isAltar(new WorldObject(id,10,0,3217,3258,0)))throw new IllegalStateException("Changed950 Prayer altar "+id);
    }
    private static boolean sequence(int id) { return pin("sequence."+id,file(20,id>>>7,id&127)); }
    private static byte[] file(int index,int group,int file) {return Cache.STORE.getIndexes()[index].getFile(group,file);}
    private static boolean pin(String key,byte[] bytes) {
        if(bytes==null)return false;
        try {
            StringBuilder actual=new StringBuilder();
            for(byte value:MessageDigest.getInstance("SHA-256").digest(bytes))actual.append(String.format("%02x",value&255));
            return actual.toString().equals(PINS.getProperty(key));
        } catch(java.security.NoSuchAlgorithmException impossible) {throw new AssertionError(impossible);}
    }
    private static Properties loadPins() {
        Properties result=new Properties();
        try(InputStream input=Native950Prayer.class.getResourceAsStream("/native950/prayer-assets-950.properties")) {
            if(input==null)throw new IllegalStateException("Missing native950 prayer assets");result.load(input);
        }catch(java.io.IOException error){throw new IllegalStateException("Cannot load native950 prayer assets",error);}
        if(!"1".equals(result.getProperty("format")) || !"950".equals(result.getProperty("revision")))
            throw new IllegalStateException("Unexpected native950 prayer asset format");
        return result;
    }
}
