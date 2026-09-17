package com.rs.game.player.client;
import com.rs.game.*;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.content.RuneCrafting;
import java.util.*;
/** Ordinary altar crafting with the original910 level, XP and rune multiplier policy. */
public final class Native950Runecrafting {
 private Native950Runecrafting(){}
 public enum Altar {
  AIR(2478,556,1,false,RuneCrafting.AIR_RUNE_MULTIPLIERS), MIND(2479,558,1,false,RuneCrafting.MIND_RUNE_MULTIPLIERS),
  WATER(2480,555,5,false,RuneCrafting.WATER_RUNE_MULTIPLIERS), EARTH(2481,557,9,false,RuneCrafting.EARTH_RUNE_MULTIPLIERS),
  FIRE(2482,554,14,false,RuneCrafting.FIRE_RUNE_MULTIPLIERS), BODY(2483,559,20,false,RuneCrafting.BODY_RUNE_MULTIPLIERS),
  COSMIC(2484,564,27,true,RuneCrafting.COSMIC_RUNE_MULTIPLIERS), CHAOS(2487,562,35,true,RuneCrafting.CHAOS_RUNE_MULTIPLIERS),
  ASTRAL(17010,9075,40,true,RuneCrafting.ASTRAL_RUNE_MULTIPLIERS), NATURE(2486,561,44,true,RuneCrafting.NATURE_RUNE_MULTIPLIERS),
  LAW(2485,563,54,true,RuneCrafting.LAW_RUNE_MULTIPLIERS), DEATH(2488,560,65,true,RuneCrafting.DEATH_RUNE_MULTIPLIERS),
  BLOOD(30624,565,77,true,RuneCrafting.BLOOD_RUNE_MULTIPLIERS);
  public final int objectId,rune,level; public final boolean pureOnly; private final int[] multipliers;
  Altar(int id,int rune,int level,boolean pure,int[] multiples){objectId=id;this.rune=rune;this.level=level;pureOnly=pure;multipliers=multiples.clone();}
  public double xp(){return RuneCrafting.getStandardAltarExperience(rune);}
  public int output(int essence,int level){return RuneCrafting.getRuneOutput(essence,level,this.level,multipliers);}
 }
 public static Altar definition(WorldObject object){
  if(object==null||object.getType()<0||object.getType()>22||!Native950RunecraftingDivinationAssets.verified("object",object.getId()))return null;
  for(Altar altar:Altar.values())if(altar.objectId==object.getId()){
   com.rs.cache.loaders.ObjectDefinitions d=object.getDefinitions();
   String name=altar.name().substring(0,1)+altar.name().substring(1).toLowerCase(Locale.ROOT)+" altar";
   if(!d.loaded||d.transforms!=null||!name.equals(d.name)||d.options==null||d.options.length<3||!"Craft runes".equals(d.options[2]))return null;
   return Native950RunecraftingDivinationAssets.effects(791,186)&&Native950RunecraftingDivinationAssets.item(altar.rune)!=null
    &&Native950RunecraftingDivinationAssets.item(1436)!=null&&Native950RunecraftingDivinationAssets.item(7936)!=null?altar:null;
  }return null;
 }
 public static boolean isAltar(WorldObject object){return definition(object)!=null;}
 public static boolean handles(WorldObject object,int option){return ((option==1||option==3)&&isAltar(object))||Native950RunecraftingTravel.handles(object,option);}
 public static Native950ItemCatalog.Entry itemEntry(int id){
  if(id>=554&&id<=565||id==1436||id==7936||id==9075||id==13655)return Native950RunecraftingDivinationAssets.item(id);
  for(Native950RunecraftingTravel.Gateway g:Native950RunecraftingTravel.Gateway.values())if(id==g.talisman||id==g.tiara)return Native950RunecraftingDivinationAssets.item(id);return null;
 }
 public static void verifyCacheBindings(){Native950RunecraftingDivinationAssets.verifyAll();Native950RunecraftingTravel.verifyCacheBindings();for(Altar altar:Altar.values())if(definition(new WorldObject(altar.objectId,10,0,3217,3258,0))!=altar)throw new IllegalStateException("Unverified altar "+altar.objectId);}
 public static List<Native950ItemCatalog.Entry> itemEntries(){
  List<Native950ItemCatalog.Entry> list=new ArrayList<>();for(int id:new int[]{1436,7936,554,555,556,557,558,559,560,561,562,563,564,565,9075}){
   Native950ItemCatalog.Entry item=Native950RunecraftingDivinationAssets.item(id);if(item!=null)list.add(item);
  }list.addAll(Native950RunecraftingTravel.itemEntries());return Collections.unmodifiableList(list);
 }
 public static boolean start(Player player,WorldObject object){
  if(Native950RunecraftingTravel.handles(object,1))return Native950RunecraftingTravel.enter(player,object);
  Altar altar=definition(object);return player!=null&&player.isNative950()&&altar!=null&&player.getActionManager().setAction(new Craft(object,altar));
 }
 static boolean stationary(Player p){return p.isActive()&&!p.isDead()&&!p.hasFinished()&&!p.isLocked()&&!p.hasWalkSteps()
  &&p.getNextWorldTile()==null&&!p.isNative950ForceMovementActive()&&p.getNextForceMovement()==null;}
 private static final class Craft extends Action {
  final WorldObject altar;final Altar data;WorldTile origin;Object controller;boolean completed,stopped;
  Craft(WorldObject altar,Altar data){this.altar=altar;this.data=data;}
  private boolean environment(Player p){return !stopped&&controller==p.getControlerManager().getControler()&&!Native950Thieving.underCombat(p)&&stationary(p)&&origin!=null&&p.matches(origin)&&Native950Mining.current(altar)&&Native950Mining.inReach(p,altar)&&definition(altar)==data;}
  private int pure(Player p){return p.getInventory().getAmountOf(7936);}
  private int normal(Player p){return data.pureOnly?0:p.getInventory().getAmountOf(1436);}
  @Override public boolean start(Player p){
   origin=new WorldTile(p);controller=p.getControlerManager().getControler();
   if(p.getNextWalkDirection()!=-1||p.hasTeleported()||!environment(p))return false;
   if(p.getSkills().getLevel(Skills.RUNECRAFTING)<data.level){p.sendMessage("You need a Runecrafting level of "+data.level+" to craft these runes.");return false;}
   if(pure(p)+normal(p)<=0||pure(p)+normal(p)>28){p.sendMessage("You need "+(data.pureOnly?"pure":"rune or pure")+" essence in your backpack.");return false;}
   p.setNextFaceWorldTile(altar);setActionDelay(p,1);return true;
  }
  @Override public boolean process(Player p){return environment(p)&&(completed||p.getSkills().getLevel(Skills.RUNECRAFTING)>=data.level&&(pure(p)+normal(p)>0));}
  @Override public int processWithDelay(Player p){
   if(completed||!process(p))return -1;
   int pure=pure(p),normal=normal(p),used=pure+normal;List<Item> ingredients=new ArrayList<>();
   if(pure>0)ingredients.add(new Item(7936,pure));if(normal>0)ingredients.add(new Item(1436,normal));
   int made=data.output(used,p.getSkills().getLevel(Skills.RUNECRAFTING));
   if(!Native950Skilling.exchange(p,ingredients.toArray(new Item[0]),new Item[]{new Item(data.rune,made)})){p.sendMessage("Your backpack cannot hold the crafted runes.");return -1;}
   p.getSkills().addXp(Skills.RUNECRAFTING,data.xp()*used);p.addRunesMade(made);
   p.setNextAnimation(new Animation(791));p.setNextGraphics(new Graphics(186,0,110));
   p.sendMessage("You bind the temple's power into "+made+" "+Native950Skilling.itemType(p,data.rune).name.toLowerCase(Locale.ROOT)+"s.");
   completed=true;return Native950Mining.animationDelay(791);
  }
  @Override public void stop(Player p){stopped=true;p.setNextAnimation(new Animation(-1));setActionDelay(p,1);}
 }
}
