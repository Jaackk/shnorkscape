package com.rs.game.player.client;

import com.rs.cache.loaders.ObjectDefinitions;
import com.rs.game.*;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntUnaryOperator;

/** Per-character crops on the original ActionManager, inventory and XP boundaries. */
public final class Native950Farming {
 public static final int MAX_PLOTS=256,MAX_HARVEST=60;
 private static final String KEY="native950.farming.plots";
 private static final Map<Integer,Integer> PATCHES=new TreeMap<>();
 private static final Map<Integer,Crop> CROPS=new LinkedHashMap<>();
 static{for(String[] r:Native950FarmingConstructionAssets.rows("farming-patches-950.tsv"))PATCHES.put(Integer.parseInt(r[0]),Integer.parseInt(r[1]));for(String[] r:Native950FarmingConstructionAssets.rows("farming-crops-950.tsv")){Crop c=new Crop(r);CROPS.put(c.seed,c);}}
 private Native950Farming(){}
 public static final class Crop {
  public final int seed,type,level,produce,seeds,start,stages;public final double plantXp,harvestXp;public final long growMillis;
  Crop(String[] r){seed=Integer.parseInt(r[0]);type=Integer.parseInt(r[1]);level=Integer.parseInt(r[2]);produce=Integer.parseInt(r[3]);plantXp=Double.parseDouble(r[4]);harvestXp=Double.parseDouble(r[5]);growMillis=Long.parseLong(r[6])*60000L;seeds=Integer.parseInt(r[7]);start=Integer.parseInt(r[8]);stages=Integer.parseInt(r[9]);}
  public String name(){return Native950Production.name(seed).replace(" seed","").replace(" Seed","");}
  public boolean usesHarvestLives(){return type==0||type==2||type==6;}
  /** Guaranteed minimum; compost does not multiply ordinary flowers. */
  public int yield(int compost){return usesHarvestLives()?3+Math.max(0,Math.min(3,compost)):seed==5100?3:1;}
  public int maximumYield(){return usesHarvestLives()?MAX_HARVEST:3;} // Also accepts first-pass flower saves.
  public double harvestExperience(){return seed==5100?harvestXp/3.0:harvestXp;}

 }
 /** Immutable save DTO. The base object id is the logical patch: one allotment contains many locs sharing one varbit. Each update replaces a plot, preventing stale actions from awarding twice. */
 public static final class Plot {
  public final int objectId,x,y,plane,seedId,compost,harvestRemaining;public final long plantedAt;public final boolean cleared;
  public Plot(int objectId,int x,int y,int plane,int seedId,long plantedAt,boolean cleared,int compost,int harvestRemaining){this.objectId=objectId;this.x=x;this.y=y;this.plane=plane;this.seedId=seedId;this.plantedAt=plantedAt;this.cleared=cleared;this.compost=compost;this.harvestRemaining=harvestRemaining;}
  String key(){return Integer.toString(objectId);}
  public boolean mature(long now){Crop c=CROPS.get(seedId);return c!=null&&now>=plantedAt&&now-plantedAt>=c.growMillis;}
  public int value(long now){Crop c=CROPS.get(seedId);if(c==null)return cleared?3:0;long elapsed=Math.max(0,now-plantedAt);int stage=elapsed>=c.growMillis?c.stages:(int)(elapsed*c.stages/c.growMillis);return c.start+stage;}
 }
 public static boolean valid(Plot p){if(p==null||!PATCHES.containsKey(p.objectId)||p.x<0||p.x>16383||p.y<0||p.y>16383||p.plane<0||p.plane>3||p.compost<0||p.compost>3||p.harvestRemaining<0||p.harvestRemaining>MAX_HARVEST||p.plantedAt<0)return false;if(p.seedId==0)return p.plantedAt==0&&p.harvestRemaining==0;Crop c=CROPS.get(p.seedId);return c!=null&&c.type==PATCHES.get(p.objectId)&&p.cleared&&p.plantedAt>0&&p.plantedAt<4102444800000L&&p.harvestRemaining>=1&&p.harvestRemaining<=c.maximumYield();}
 @SuppressWarnings("unchecked") private static Map<String,Plot> state(Player p){Object old=p.getTemporaryAttributtes().get(KEY);if(old instanceof Map)return (Map<String,Plot>)old;Map<String,Plot> fresh=new LinkedHashMap<>();p.getTemporaryAttributtes().put(KEY,fresh);return fresh;}
 public static List<Plot> snapshot(Player p){return Collections.unmodifiableList(new ArrayList<>(state(p).values()));}
 public static void restore(Player p,List<Plot> rows){if(rows==null)rows=Collections.emptyList();if(rows.size()>MAX_PLOTS)throw new IllegalArgumentException("Too many saved farming patches");Map<String,Plot> next=new LinkedHashMap<>();for(Plot r:rows){if(!valid(r)||next.put(r.key(),r)!=null)throw new IllegalArgumentException("Invalid or duplicate saved farming patch");}p.getTemporaryAttributtes().put(KEY,next);p.getTemporaryAttributtes().remove(KEY+".vars");}
 private static Plot plot(Player p,WorldObject o){String key=Integer.toString(o.getId());Plot existing=state(p).get(key);return existing==null?new Plot(o.getId(),o.getX(),o.getY(),o.getPlane(),0,0,false,0,0):existing;}
 private static boolean same(Plot a,Plot b){return a.objectId==b.objectId&&a.x==b.x&&a.y==b.y&&a.plane==b.plane&&a.seedId==b.seedId&&a.plantedAt==b.plantedAt&&a.cleared==b.cleared&&a.compost==b.compost&&a.harvestRemaining==b.harvestRemaining;}
 private static void put(Player p,WorldObject o,Plot next){if(!valid(next))throw new IllegalArgumentException("Invalid farming transition");Map<String,Plot> rows=state(p);if(!rows.containsKey(next.key())&&rows.size()>=MAX_PLOTS)throw new IllegalStateException("Farming patch limit");rows.put(next.key(),next);refresh(p,o);}
 public static List<Crop> crops(){return Collections.unmodifiableList(new ArrayList<>(CROPS.values()));}
 public static Crop crop(int seed){return CROPS.get(seed);}
 public static Set<Integer> patchIds(){return Collections.unmodifiableSet(PATCHES.keySet());}
 public static boolean isPatch(WorldObject o){if(o==null||o.getType()<0||o.getType()>22||!PATCHES.containsKey(o.getId())||!Native950FarmingConstructionAssets.pin("object",o.getId()))return false;ObjectDefinitions d=o.getDefinitions();return d.loaded&&d.transforms!=null&&d.transforms.length>4&&d.configFileId>=0&&Native950FarmingConstructionAssets.pin("varbit",d.configFileId);}
 public static ObjectDefinitions definition(Player p,WorldObject o){if(!isPatch(o))return null;int value=plot(p,o).value(System.currentTimeMillis());int[] transforms=o.getDefinitions().transforms;if(value<0||value>=transforms.length-1||transforms[value]<0||!Native950FarmingConstructionAssets.pin("object",transforms[value]))return null;return ObjectDefinitions.getObjectDefinitions(transforms[value]);}
 public static boolean matches(WorldObject base,int clickedId,Player p){ObjectDefinitions d=definition(p,base);return d!=null&&(base.getId()==clickedId||d.id==clickedId);}
 public static boolean handles(WorldObject o,int option){return isPatch(o)&&option>=1&&option<=5;}
 public static boolean accepts(Player p,WorldObject o,int option){ObjectDefinitions d=definition(p,o);return d!=null&&option>=1&&option<=5&&d.options!=null&&d.options[option-1]!=null;}
 public static boolean inReach(Player p,WorldObject o){return p!=null&&p.isNative950()&&p.isActive()&&!p.hasFinished()&&!p.isDead()&&!p.isLocked()&&!p.closeInterfaceLocked&&p.getNextWorldTile()==null&&!p.isNative950ForceMovementActive()&&p.getNextForceMovement()==null&&p.getNextWalkDirection()==-1&&!p.hasTeleported()&&isPatch(o)&&Native950Mining.current(o)&&Native950Mining.inReach(p,o);}
 public static boolean acceptsItem(WorldObject o,int item){return isPatch(o)&&(CROPS.containsKey(item)||item==5341||item==5325||item==952||item==6032||item==6034||item==43966);}
 /** Recombine every owned crop slice, then publish each parent once. First login/restore explicitly resets empty patches too. */
 public static void refresh(Player p){
  boolean force=!Boolean.TRUE.equals(p.getTemporaryAttributtes().get(KEY+".vars"));Map<Integer,Integer> previous=new TreeMap<>();
  for(int id:PATCHES.keySet()){
   ObjectDefinitions d=ObjectDefinitions.getObjectDefinitions(id);
   if(d.configFileId<0||!Native950FarmingConstructionAssets.pin("varbit",d.configFileId))continue;
   com.rs.cache.loaders.VarBitDefinitions bit=com.rs.cache.loaders.VarBitDefinitions.getClientVarpBitDefinitions(d.configFileId);
   if(!Native950FarmingConstructionAssets.pin("varp",bit.baseVar))throw new IllegalStateException("Unverified Farming parent variable "+bit.baseVar);
   if(!previous.containsKey(bit.baseVar))previous.put(bit.baseVar,p.getVarsManager().getValue(bit.baseVar));
   Plot saved=state(p).get(Integer.toString(id));p.getVarsManager().setVarBit(d.configFileId,saved==null?0:saved.value(System.currentTimeMillis()));
  }
  for(Map.Entry<Integer,Integer> old:previous.entrySet()){int value=p.getVarsManager().getValue(old.getKey());if(force||value!=old.getValue())p.getVarsManager().forceSendVar(old.getKey(),value);}
  p.getTemporaryAttributtes().put(KEY+".vars",Boolean.TRUE);
 }
 private static void refresh(Player p,WorldObject o){if(isPatch(o))p.getVarsManager().sendVarBit(o.getDefinitions().configFileId,plot(p,o).value(System.currentTimeMillis()));}
 public static boolean handle(Player p,WorldObject o,int option){if(!inReach(p,o)||!accepts(p,o,option))return false;Plot a=plot(p,o);if(option==2||option==4||option==5){inspect(p,o);return true;}if(option==3&&a.seedId!=0)return clear(p,o);if(!a.cleared)return rake(p,o);if(a.seedId!=0&&a.mature(System.currentTimeMillis()))return harvest(p,o);inspect(p,o);return true;}
 public static void inspect(Player p,WorldObject o){Plot a=plot(p,o);if(!a.cleared)p.sendMessage("This patch needs raking. Bring a rake and use seeds on the cleared patch.");else if(a.seedId==0)p.sendMessage("This patch is ready. Use suitable seeds on it with a seed dibber in your backpack or tool belt. Compost improves the harvest.");else if(a.mature(System.currentTimeMillis()))p.sendMessage("Your "+CROPS.get(a.seedId).name()+" is ready to harvest ("+a.harvestRemaining+" remaining).");else{long left=Math.max(1,(CROPS.get(a.seedId).growMillis-(System.currentTimeMillis()-a.plantedAt)+59999)/60000);p.sendMessage("Your healthy "+CROPS.get(a.seedId).name()+" is growing. About "+left+" minute(s) remain; growth continues while logged out.");}}
 static boolean hasTool(Player p,int id){return p.getInventory().containsItem(id,1)||Native950Toolbelt.has(p,id);}
 public static List<Native950ProductionMenu.Choice> choices(Player p,WorldObject o){List<Native950ProductionMenu.Choice> out=new ArrayList<>();if(!inReach(p,o))return out;Plot a=plot(p,o);if(!a.cleared)out.add(new Native950ProductionMenu.Choice("Rake patch",()->rake(p,o)));else if(a.seedId==0){for(Crop c:CROPS.values())if(c.type==PATCHES.get(o.getId())&&p.getInventory().containsItem(c.seed,1))out.add(new Native950ProductionMenu.Choice("Plant "+c.name()+" (level "+c.level+")",()->plant(p,o,c.seed)));}else{if(a.mature(System.currentTimeMillis()))out.add(new Native950ProductionMenu.Choice("Harvest crop",()->harvest(p,o)));out.add(new Native950ProductionMenu.Choice("Clear crop",()->clear(p,o)));}out.add(new Native950ProductionMenu.Choice("Inspect patch",()->inspect(p,o)));return out;}
 public static boolean use(Player p,WorldObject o,int item){if(!inReach(p,o)||!acceptsItem(o,item))return false;if(CROPS.containsKey(item))return plant(p,o,item);if(item==5341)return rake(p,o);if(item==952)return clear(p,o);if(item==6032||item==6034||item==43966)return compost(p,o,item);inspect(p,o);return true;}
 private static boolean room(Player p,WorldObject o){if(state(p).size()<MAX_PLOTS||state(p).containsKey(plot(p,o).key()))return true;p.sendMessage("You have too many saved farming patches.");return false;}
 public static boolean rake(Player p,WorldObject o){if(!inReach(p,o)||!room(p,o))return false;Plot a=plot(p,o);if(a.cleared){inspect(p,o);return true;}return p.getActionManager().setAction(new PatchAction(o,a,5341,2273,3){@Override boolean complete(Player p){if(!Native950Skilling.giveItems(p,new Item[]{new Item(6055,3)})){p.sendMessage("You need room for the weeds in your backpack or tool belt.");return false;}put(p,o,new Plot(a.objectId,a.x,a.y,a.plane,0,0,true,0,0));p.getSkills().addXp(Skills.FARMING,12);p.sendMessage("You rake the patch clear.");return true;}});}
 /** Original910 harvest lives and level-sensitive life-save curve, rolled once into the saved crop count. */
 static int harvestAmount(Crop crop,int level,int compost,IntUnaryOperator rolls){
  return crop.usesHarvestLives()?harvestLives(crop,level,3+Math.max(0,Math.min(3,compost)),rolls):crop.yield(compost);
 }
 static int harvestLives(Crop crop,int level,int lives,IntUnaryOperator rolls){
  int chance=Math.min(75,10+Math.max(1,Math.min(120,level))/3+(crop.type==6?5:0)),harvests=0;
  while(lives>0&&harvests<MAX_HARVEST){harvests++;if(rolls.applyAsInt(100)>=chance)lives--;}
  return harvests;
 }
 static double compostExperience(int id){return id==6032?18:id==6034?26:id==43966?36:0;}
 public static boolean plant(Player p,WorldObject o,int seed){if(!inReach(p,o)||!room(p,o))return false;Crop c=CROPS.get(seed);Plot a=plot(p,o);if(c==null)return false;if(c.type!=PATCHES.get(o.getId())){p.sendMessage("That seed belongs in a different type of farming patch.");return true;}if(!a.cleared||a.seedId!=0){p.sendMessage(!a.cleared?"Rake the weeds before planting.":"There is already a crop growing here.");return true;}if(p.getSkills().getLevel(Skills.FARMING)<c.level){p.sendMessage("You need Farming level "+c.level+" to plant that seed.");return true;}if(!p.getInventory().containsItem(seed,c.seeds)){p.sendMessage("You need "+c.seeds+" of that seed to plant this patch.");return true;}return p.getActionManager().setAction(new PatchAction(o,a,5325,2291,1){@Override boolean complete(Player p){if(p.getSkills().getLevel(Skills.FARMING)<c.level||!Native950Skilling.exchange(p,new Item[]{new Item(seed,c.seeds)},new Item[0]))return false;put(p,o,new Plot(a.objectId,a.x,a.y,a.plane,seed,System.currentTimeMillis(),true,a.compost,harvestAmount(c,p.getSkills().getLevel(Skills.FARMING),a.compost,bound->ThreadLocalRandom.current().nextInt(bound))));p.getSkills().addXp(Skills.FARMING,c.plantXp);p.sendMessage("You plant the seeds. Growth continues while you are logged out.");return true;}});}
 public static boolean compost(Player p,WorldObject o,int id){
  if(!inReach(p,o)||compostExperience(id)==0)return false;Plot a=plot(p,o);int rank=id==43966?3:id==6034?2:1;
  if(!a.cleared){p.sendMessage("Rake the weeds before applying compost.");return true;}
  if(rank<=a.compost){p.sendMessage("This patch already has compost of that quality or better.");return true;}
  if(a.seedId!=0&&a.mature(System.currentTimeMillis())){p.sendMessage("That crop is already fully grown. Compost the patch before planting or while it is growing.");return true;}
  return p.getActionManager().setAction(new PatchAction(o,a,0,830,1){@Override boolean complete(Player p){
   if(!Native950Skilling.exchange(p,new Item[]{new Item(id,1)},new Item[]{new Item(1925,1)})){p.sendMessage("You need the compost and room for the empty bucket.");return false;}
   Crop crop=CROPS.get(a.seedId);int remaining=a.harvestRemaining;
   if(crop!=null&&crop.usesHarvestLives())remaining=Math.min(MAX_HARVEST,remaining+harvestLives(crop,p.getSkills().getLevel(Skills.FARMING),rank-a.compost,bound->ThreadLocalRandom.current().nextInt(bound)));
   put(p,o,new Plot(a.objectId,a.x,a.y,a.plane,a.seedId,a.plantedAt,true,rank,remaining));
   p.getSkills().addXp(Skills.FARMING,compostExperience(id));p.sendMessage("You enrich the soil with "+Native950Production.name(id).toLowerCase(Locale.ROOT)+".");return true;
  }});
 }
 public static boolean clear(Player p,WorldObject o){if(!inReach(p,o))return false;Plot a=plot(p,o);if(a.seedId==0){inspect(p,o);return true;}return p.getActionManager().setAction(new PatchAction(o,a,952,830,2){@Override boolean complete(Player p){put(p,o,new Plot(a.objectId,a.x,a.y,a.plane,0,0,true,0,0));p.sendMessage("You clear the crop from the patch.");return true;}});}
 public static boolean harvest(Player p,WorldObject o){if(!inReach(p,o))return false;Plot a=plot(p,o);Crop c=CROPS.get(a.seedId);if(c==null||!a.mature(System.currentTimeMillis())){inspect(p,o);return true;}return p.getActionManager().setAction(new Action(){Plot expected=a;WorldTile origin;Object controller;
  @Override public boolean start(Player p){origin=new WorldTile(p);controller=p.getControlerManager().getControler();if(!inReach(p,o)||!hasTool(p,952)){p.sendMessage("Bring a spade to harvest this patch.");return false;}p.setNextFaceWorldTile(o);setActionDelay(p,1);return true;}
  @Override public boolean process(Player p){return inReach(p,o)&&p.matches(origin)&&controller==p.getControlerManager().getControler()&&!Native950Thieving.underCombat(p)&&same(expected,plot(p,o))&&expected.seedId!=0&&expected.mature(System.currentTimeMillis())&&hasTool(p,952);}
  @Override public int processWithDelay(Player p){if(!process(p))return -1;if(!Native950Skilling.giveItems(p,new Item[]{new Item(c.produce,1)})){p.sendMessage("Your backpack is full. Your remaining harvest stays in the patch.");return -1;}int left=expected.harvestRemaining-1;expected=new Plot(a.objectId,a.x,a.y,a.plane,left==0?0:a.seedId,left==0?0:a.plantedAt,true,left==0?0:a.compost,left);put(p,o,expected);p.getSkills().addXp(Skills.FARMING,c.harvestExperience());p.setNextAnimation(new Animation(c.type==6?2282:c.type==3?2292:830));if(left==0)p.sendMessage("The patch is empty and ready for more seeds.");return 2;}
  @Override public void stop(Player p){p.setNextAnimation(new Animation(-1));setActionDelay(p,1);}
 });}
 private abstract static class PatchAction extends Action {final WorldObject object;final Plot before;final int tool,animation,delay;WorldTile origin;Object controller;boolean finished;PatchAction(WorldObject o,Plot a,int tool,int animation,int delay){object=o;before=a;this.tool=tool;this.animation=animation;this.delay=delay;}
  @Override public boolean start(Player p){origin=new WorldTile(p);controller=p.getControlerManager().getControler();if(!inReach(p,object)||tool>0&&!hasTool(p,tool)){if(tool>0)p.sendMessage("You need "+Native950Production.name(tool)+" in your backpack or tool belt.");return false;}p.setNextFaceWorldTile(object);p.setNextAnimation(new Animation(animation));setActionDelay(p,delay);return true;}
  @Override public boolean process(Player p){return !finished&&inReach(p,object)&&p.matches(origin)&&controller==p.getControlerManager().getControler()&&!Native950Thieving.underCombat(p)&&same(before,plot(p,object))&&(tool==0||hasTool(p,tool));}
  @Override public int processWithDelay(Player p){if(!process(p))return -1;finished=true;complete(p);return -1;}abstract boolean complete(Player p);
  @Override public void stop(Player p){p.setNextAnimation(new Animation(-1));setActionDelay(p,1);}
 }
 public static Native950ItemCatalog.Entry itemEntry(int id){return Native950FarmingConstructionAssets.entry(id);}public static List<Native950ItemCatalog.Entry> itemEntries(){return Native950FarmingConstructionAssets.entries();}
 public static void verifyCacheBindings(){Native950FarmingConstructionAssets.verify();for(int id:PATCHES.keySet())if(!isPatch(new WorldObject(id,10,0,3217,3257,0)))throw new IllegalStateException("Invalid farming patch "+id);}
}
