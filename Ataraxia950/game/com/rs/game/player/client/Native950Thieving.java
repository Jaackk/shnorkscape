package com.rs.game.player.client;

import com.rs.cache.loaders.ObjectDefinitions;
import com.rs.game.*;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.actions.thieving.PickPocketAction;
import com.rs.game.player.actions.thieving.def.PickPocketableNPC;
import com.rs.game.player.actions.thieving.def.Stalls;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntUnaryOperator;

/** Native boundaries around910 pickpocket data/action and stall data; no parallel skill scheduler. */
public final class Native950Thieving {
    private static final int[] ANIMATIONS={24887,5074,5075,5078};
    private static final Map<String,Object> DEPLETED=new HashMap<>();
    private Native950Thieving(){}
    public static boolean verifyCacheBindings(){return Native950ThievingHunterAssets.verifyAll();}
    public static Set<Integer> pickpocketIds(){return Native950ThievingHunterAssets.ids("pickpocket");}
    public static Set<Integer> stallIds(){return Native950ThievingHunterAssets.ids("stall");}
    public static boolean isPickpocket(NPC npc,int option){return definition(npc,option)!=null;}
    public static PickPocketableNPC definition(NPC npc,int option){
        if(npc==null||option<1||option>5||!"Pickpocket".equalsIgnoreCase(npc.getNative950MenuOption(option))
                ||!Native950ThievingHunterAssets.verified("npc",npc.getId()))return null;
        String type=Native950ThievingHunterAssets.binding("pickpocket."+npc.getId());
        if(type==null)return null;
        PickPocketableNPC data=PickPocketableNPC.valueOf(type);
        return loot(data).length==0?null:data;
    }
    public static boolean startPickpocket(Player p,NPC npc,int option){
        PickPocketableNPC data=definition(npc,option);
        return p!=null&&p.isNative950()&&data!=null&&p.getActionManager().setAction(new PickPocketAction(npc,data));
    }
    public static boolean isStall(WorldObject object,int option){return stall(object,option)!=null;}
    public static Stalls stall(WorldObject object,int option){
        if(object==null||option<1||option>5||!Native950Scenery.isHarvestableShape(object)
                ||!Native950ThievingHunterAssets.verified("object",object.getId()))return null;
        ObjectDefinitions d=object.getDefinitions();String binding=Native950ThievingHunterAssets.binding("stall."+object.getId());
        if(binding==null||!d.loaded||d.transforms!=null||d.sizeX<1||d.sizeY<1||d.sizeX>16||d.sizeY>16
                ||d.options==null||option>d.options.length||!isStealOption(d.options[option-1])
                ||!d.name.equals(Native950ThievingHunterAssets.binding("objectName."+object.getId())))return null;
        Stalls data=Stalls.valueOf(binding);return loot(data).length==0?null:data;
    }
    static boolean isStealOption(String option){return "Steal-from".equalsIgnoreCase(option)||"Steal from".equalsIgnoreCase(option);}
    public static boolean startStall(Player p,WorldObject object,int option){Stalls data=stall(object,option);
        return p!=null&&p.isNative950()&&data!=null&&p.getActionManager().setAction(new StallAction(object,option,data));}
    /** Refuse repurposed legacy rewards; retaining verified rows preserves each admitted row's weight. */
    static Item[] loot(PickPocketableNPC data){List<Item> items=new ArrayList<>();for(Item item:data.getLoot())
        if(item.getAmount()>0&&Native950ThievingHunterAssets.legacyLoot(item.getId()))items.add(new Item(item.getId(),item.getAmount()));return items.toArray(new Item[0]);}
    static int[] loot(Stalls data){return Arrays.stream(data.getItem()).filter(Native950ThievingHunterAssets::legacyLoot).toArray();}
    static boolean underCombat(Player p){return p.getAttackedBy()!=null&&p.getAttackedByDelay()>Utils.currentTimeMillis();}
    static int lootMultiplier(PickPocketableNPC data,int thieving,int agility,boolean special){
        int index=0;if(special)for(int i=1;i<Math.min(data.getThievingLevels().length,data.getAgilityLevels().length);i++)
            if(thieving>=data.getThievingLevels()[i]&&agility>=data.getAgilityLevels()[i])index=i;return index+1;
    }
    /** Original success calculation, including its integer division and ordinary +15 chance. */
    static boolean success(int thieving,int required,int levelRoll,int difficultyRoll,double aura){
        int ratio=levelRoll/(difficultyRoll+1);return Math.round((double)ratio*thieving)>=required/aura;
    }
    static int stallRespawnTicks(Stalls data){return Math.max(1,(int)Math.ceil(1500.0*data.getTime()/600.0));}
    private static String key(WorldObject o){return o.getPlane()+":"+o.getX()+":"+o.getY()+":"+Region.OBJECT_SLOTS[o.getType()];}

    /** Per-instance state owned by the original PickPocketAction and ActionManager. */
    public static final class Journey {
        private final NPC npc;private final int id;private final PickPocketableNPC data;private final IntUnaryOperator rolls;
        private WorldTile origin;private Object controller;private Item[] rewards;private boolean resolved;private int multiplier;
        public Journey(NPC npc,PickPocketableNPC data){this(npc,data,bound->ThreadLocalRandom.current().nextInt(bound));}
        Journey(NPC npc,PickPocketableNPC data,IntUnaryOperator rolls){this.npc=npc;this.id=npc.getId();this.data=data;this.rolls=rolls;}
        public boolean start(Player p){
            origin=new WorldTile(p);controller=p.getControlerManager().getControler();
            if(!valid(p)||definition(npc,3)!=data)return false;
            if(!prepare(p))return false;
            p.sendMessage("You attempt to pick the "+npc.getName().toLowerCase(Locale.ROOT)+"'s pocket.");
            p.getActionManager().setActionDelay(2);return true;
        }
        private boolean prepare(Player p){
            int level=p.getSkills().getLevel(Skills.THIEVING),agility=p.getSkills().getLevel(Skills.AGILITY);
            multiplier=lootMultiplier(data,level,agility,rolls.applyAsInt(51)<5);
            int animation=ANIMATIONS[multiplier-1];if(!Native950ThievingHunterAssets.verified("sequence",animation)
                    ||!Native950ThievingHunterAssets.verified("sequence",424))return false;
            Item[] possible=loot(data);List<Item> selected=new ArrayList<>();
            for(int i=0;i<multiplier;i++){Item item=possible[rolls.applyAsInt(possible.length)];selected.add(new Item(item.getId(),item.getAmount()));}
            rewards=selected.toArray(new Item[0]);
            if(!Native950Skilling.canGiveItems(p,rewards)){p.sendMessage("Your backpack has no room for the stolen items.");return false;}
            p.setNextFaceEntity(npc);p.setNextAnimation(new Animation(animation));return true;
        }
        public boolean process(Player p){return !resolved&&valid(p);}
        private boolean valid(Player p){
            if(!Native950ThievingHunterAssets.ready(p,origin,controller)||!Native950ThievingHunterAssets.npcCurrent(npc,id)
                    ||!Native950ThievingHunterAssets.npcReach(p,npc)||underCombat(p)||definition(npc,3)!=data)return false;
            if(p.getSkills().getLevel(Skills.THIEVING)<data.getThievingLevels()[0]){p.sendMessage("You need Thieving level "+data.getThievingLevels()[0]+" to pick this pocket.");return false;}
            return true;
        }
        public int finish(Player p){
            if(!process(p))return -1;
            int level=p.getSkills().getLevel(Skills.THIEVING),required=data.getThievingLevels()[0];
            if(!success(level,required,rolls.applyAsInt(level+16),rolls.applyAsInt(required+6),p.getAuraManager().getThievingAccurayMultiplier())){
                resolved=true;p.setNextAnimation(new Animation(424));
                // Native life points/display are owned here, as in Native950MeleeCombat.damage.
                // General skill-death recovery is not yet ported, so an ordinary stun leaves1 HP.
                int damage=Math.min(data.getStunDamage(),Math.max(0,p.getHitpoints()-1));
                p.setHitpoints(p.getHitpoints()-damage);p.getNextHits().add(new Hit(npc,damage,Hit.HitLook.MELEE_DAMAGE,0));p.addHitBars();p.refreshHitPoints();
                p.lock(data.getStunTime());p.sendMessage("You fail to pick the pocket and are stunned.");return -1;
            }
            if(!Native950Skilling.giveItems(p,rewards)){resolved=true;p.sendMessage("Your backpack has no room for the stolen items. You stop pickpocketing.");return -1;}
            p.addTimesStolen();p.getSkills().addXp(Skills.THIEVING,data.getExperience());
            p.sendMessage(multiplier>1?"You successfully steal "+multiplier+" lots of loot.":"You successfully pick the pocket.");
            if(!valid(p)||!prepare(p)){resolved=true;return -1;}
            return 2; // ActionManager's countdown makes this three engine ticks per repeat.

        }
        public void stop(Player p){if(!resolved)p.setNextAnimation(new Animation(-1));resolved=true;rewards=null;p.setNextFaceEntity(null);p.getActionManager().setActionDelay(3);}
    }
    static final class StallAction extends Action {
        private final WorldObject object;private final int option;private final Stalls data;private WorldTile origin;private Object controller;private Item reward;private boolean resolved;
        StallAction(WorldObject object,int option,Stalls data){this.object=object;this.option=option;this.data=data;}
        @Override public boolean start(Player p){
            origin=new WorldTile(p);controller=p.getControlerManager().getControler();if(!process(p)||!Native950ThievingHunterAssets.verified("sequence",881))return false;
            int[] rewards=loot(data);int id=rewards[ThreadLocalRandom.current().nextInt(rewards.length)];
            reward=new Item(id,ThreadLocalRandom.current().nextInt(data.getAmount())+1);
            if(!Native950Skilling.canGiveItems(p,new Item[]{reward})){p.sendMessage("Your backpack has no room for the stolen items.");return false;}
            p.setNextFaceWorldTile(object);p.setNextAnimation(new Animation(881));setActionDelay(p,2);return true;
        }
        @Override public boolean process(Player p){
            if(resolved||!Native950ThievingHunterAssets.ready(p,origin,controller)||underCombat(p)||stall(object,option)!=data
                    ||!Native950Woodcutting.current(object)||!Native950Mining.inReach(p,object)||DEPLETED.containsKey(key(object)))return false;
            if(p.getSkills().getLevel(Skills.THIEVING)<data.getLevel()){p.sendMessage("You need Thieving level "+data.getLevel()+" to steal from this stall.");return false;}return true;
        }
        @Override public int processWithDelay(Player p){
            if(!process(p)||!Native950Skilling.giveItems(p,new Item[]{reward}))return -1;
            resolved=true;p.addTimesStolen();p.getSkills().addXp(Skills.THIEVING,data.getExperience());p.sendMessage("You steal from the stall.");
            final String key=key(object);final Object token=new Object();DEPLETED.put(key,token);World.removeObject(object);
            WorldTasksManager.schedule(new WorldTask(){@Override public void run(){
                if(DEPLETED.get(key)!=token)return;DEPLETED.remove(key);
                WorldObject current=World.getRegion(object.getRegionId()).getObjectWithType(object.getPlane(),object.getXInRegion(),object.getYInRegion(),object.getType());
                if(current==null)World.spawnObject(object);
            }},stallRespawnTicks(data));return -1;
        }
        @Override public void stop(Player p){if(!resolved)p.setNextAnimation(new Animation(-1));setActionDelay(p,3);}
    }
}
