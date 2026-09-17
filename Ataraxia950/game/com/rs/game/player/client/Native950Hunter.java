package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.*;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.actions.hunter.FlyingEntityHunter.FlyingEntities;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntUnaryOperator;

/** Net-and-jar butterflies and implings using the original FlyingEntities data and catch formula. */
public final class Native950Hunter {
    static final int JAR=10012,NET=10010,MAGIC_NET=11259,CATCH_ANIMATION=6606,RESPAWN_TICKS=30;
    private Native950Hunter(){}
    public static boolean verifyCacheBindings(){return Native950ThievingHunterAssets.verifyAll()&&Native950Implings.verifyCacheBindings();}
    public static boolean isCatchable(NPC npc,int option){return definition(npc,option)!=null;}
    public static FlyingEntities definition(NPC npc,int option){
        if(npc==null||option<1||option>5||!"Catch".equalsIgnoreCase(npc.getNative950MenuOption(option)))return null;
        FlyingEntities data=FlyingEntities.forNPCId(npc.getId());
        return supports(data)?data:null;
    }
    public static boolean supports(FlyingEntities data){return Native950Implings.supports(data)||(data!=null&&!data.isImpling()
            &&Native950ThievingHunterAssets.verified("npc",data.getNpcId())
            &&Native950ThievingHunterAssets.verified("sequence",CATCH_ANIMATION)
            &&Native950ThievingHunterAssets.item(JAR)&&Native950ThievingHunterAssets.item(NET)
            &&Native950ThievingHunterAssets.item(data.getReward()));}
    public static boolean start(Player p,NPC npc,int option){FlyingEntities data=definition(npc,option);
        return p!=null&&p.isNative950()&&data!=null&&p.getActionManager().setAction(new CatchAction(npc,option,data));}
    static int catchChance(int level,int required,boolean magicNet){return Math.min(95,Math.max(5,35+Math.max(0,level-required)*2+(magicNet?15:10)));}
    public static Native950ItemCatalog.Entry itemEntry(int id){
        if(!Native950ThievingHunterAssets.item(id)&&!Native950Implings.item(id))return null;
        boolean net=id==NET||id==MAGIC_NET;FlyingEntities data=FlyingEntities.forId(id);
        if(!net&&(data==null||!supports(data)))return null;
        try{
            ItemDefinitions d=ItemDefinitions.decodeStrict947(id,Cache.STORE.getIndexes()[19].getFile(id>>>8,id&255),null);
            if(d.certTemplateId!=-1||d.lendTemplateId!=-1||d.bindTemplateId!=-1||d.shardTemplateId!=-1||d.isStackable())return null;
            String[] options=new String[5];
            if(net){if(d.getEquipSlot()!=3||!d.containsInventoryOption(1,"Wield")||d.getMaleWornModelId1()<0||d.getFemaleWornModelId1()<0)return null;
                options[1]="Wield";return new Native950ItemCatalog.Entry(id,d.name,false,options,3,2);}
            String verb=data.isImpling()?"Loot":"Release";
            int index=data.isImpling()?2:0;
            if(!d.containsInventoryOption(index,verb))return null;
            options[index]=verb;return new Native950ItemCatalog.Entry(id,d.name,false,options);
        }catch(RuntimeException unavailable){return null;}
    }
    public static boolean release(Player p,int slot,int itemId,int option){
        if(!Native950ThievingHunterAssets.ready(p)||slot<0||slot>=28||option!=1)return false;
        Native950ItemCatalog.Entry entry=itemEntry(itemId);FlyingEntities data=FlyingEntities.forId(itemId);
        if(entry==null||data==null||data.isImpling()||!"Release".equals(entry.option(option)))return false;
        Item held=p.getInventory().getItem(slot);
        if(held==null||held.getId()!=itemId||held.getAmount()!=1||held.getAttributes()!=null||held.getCharges()!=0||held.getInventionData()!=null)return false;
        // Validate the clicked ordinary jar before the authoritative inventory exchange.
        if(!Native950Skilling.exchangeSlot(p,slot,itemId,1,new Item[]{new Item(JAR,1)}))return false;
        p.getActionManager().forceStop();p.sendMessage("You release the butterfly and keep the empty jar.");return true;
    }
    static final class CatchAction extends Action {
        private final NPC npc;private final int option;private final FlyingEntities data;private final IntUnaryOperator rolls;
        private WorldTile origin;private Object controller;private boolean resolved;
        CatchAction(NPC npc,int option,FlyingEntities data){this(npc,option,data,bound->ThreadLocalRandom.current().nextInt(bound));}
        CatchAction(NPC npc,int option,FlyingEntities data,IntUnaryOperator rolls){this.npc=npc;this.option=option;this.data=data;this.rolls=rolls;}
        private int jar(){return data.isImpling()?Native950Implings.EMPTY_JAR:JAR;}
        @Override public boolean start(Player p){
            origin=new WorldTile(p);controller=p.getControlerManager().getControler();if(!process(p))return false;
            p.setNextFaceEntity(npc);p.setNextAnimation(new Animation(CATCH_ANIMATION));p.sendMessage("You swing your net...");setActionDelay(p,2);return true;
        }
        @Override public boolean process(Player p){
            if(resolved||!Native950ThievingHunterAssets.ready(p,origin,controller)||!Native950ThievingHunterAssets.npcCurrent(npc,data.getNpcId())
                    ||!Native950World.getInstance().nativeNpcs().contains(npc)||!Native950ThievingHunterAssets.npcReach(p,npc)||definition(npc,option)!=data||Native950Thieving.underCombat(p))return false;
            if(p.getSkills().getLevel(Skills.HUNTER)<data.getLevel()){p.sendMessage("You need Hunter level "+data.getLevel()+" to catch this "+(data.isImpling()?"impling":"butterfly")+".");return false;}
            int weapon=p.getEquipment().getWeaponId();
            if((weapon!=NET&&weapon!=MAGIC_NET)||itemEntry(weapon)==null){p.sendMessage("Equip a butterfly net to catch this "+(data.isImpling()?"impling":"butterfly")+".");return false;}
            if(!p.getInventory().containsItem(jar(),1)){p.sendMessage("You need an empty "+(data.isImpling()?"impling":"butterfly")+" jar.");return false;}
            return Native950Skilling.canExchange(p,new Item[]{new Item(jar(),1)},new Item[]{new Item(data.getReward(),1)});
        }
        @Override public int processWithDelay(Player p){
            if(!process(p))return -1;
            if(rolls.applyAsInt(100)>=catchChance(p.getSkills().getLevel(Skills.HUNTER),data.getLevel(),p.getEquipment().getWeaponId()==MAGIC_NET)){
                resolved=true;p.sendMessage("You stumble and miss the "+(data.isImpling()?"impling":"butterfly")+".");return -1;
            }
            if(!Native950Skilling.exchange(p,new Item[]{new Item(jar(),1)},new Item[]{new Item(data.getReward(),1)}))return -1;
            resolved=true;p.getSkills().addXp(Skills.HUNTER,data.getRsExperience());p.addCreaturesCaught();p.sendMessage("You catch the "+npc.getName().toLowerCase(Locale.ROOT)+".");
            Native950World.getInstance().respawnCapturedButterfly(npc,data.isImpling()?100:RESPAWN_TICKS);return -1;
        }
        @Override public void stop(Player p){if(!resolved)p.setNextAnimation(new Animation(-1));p.setNextFaceEntity(null);setActionDelay(p,1);}
    }
}
