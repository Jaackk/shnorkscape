package com.rs.game.player.client;
import com.rs.game.*;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.divination.*;
import com.rs.game.player.actions.divination.DivinationConvert.ConvertMode;
import com.rs.game.route.RouteFinder;
import com.rs.game.route.strategy.EntityStrategy;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
/** Original ordinary harvesting/conversion actions, with current950 identity and atomic inventory. */
public final class Native950Divination {
 private Native950Divination(){}
 /** Weak keys cannot keep a removed NPC alive. Values intentionally contain no NPC/player reference. */
 private static final Map<NPC,HarvestPause> HARVEST_PAUSES=new WeakHashMap<>();
 static final class HarvestPause {final int previousRadius;int users;HarvestPause(int radius){previousRadius=radius;}}
 static synchronized HarvestPause pauseHarvestWander(NPC npc){
  HARVEST_PAUSES.entrySet().removeIf(e->e.getKey()==null||e.getKey().hasFinished());
  if(npc.hasFinished())throw new IllegalStateException("Cannot harvest a finished wisp");
  HarvestPause pause=HARVEST_PAUSES.get(npc);
  if(pause==null){pause=new HarvestPause(npc.getNative950Wander());HARVEST_PAUSES.put(npc,pause);if(pause.previousRadius!=0)npc.setNative950Wander(0);npc.resetWalkSteps();}
  pause.users++;return pause;
 }
 static synchronized void releaseHarvestWander(NPC npc,HarvestPause pause){
  if(pause==null||pause.users<=0)return;
  pause.users--;
  if(HARVEST_PAUSES.get(npc)!=pause)return;
  if(npc.hasFinished()){HARVEST_PAUSES.remove(npc);return;}
  if(pause.users==0){
   HARVEST_PAUSES.remove(npc);
   // Do not override a later wandering decision made by another NPC system.
   if(npc.getNative950Wander()==0){npc.resetWalkSteps();if(pause.previousRadius!=0)npc.setNative950Wander(pause.previousRadius);}
  }
 }
 static synchronized int activeHarvestPauses(){HARVEST_PAUSES.entrySet().removeIf(e->e.getKey()==null||e.getKey().hasFinished());return HARVEST_PAUSES.size();}
 public static WispInfo definition(NPC npc,int option){
  if(npc==null||!npc.isNative950()||option!=1||!"Harvest".equalsIgnoreCase(npc.getNative950MenuOption(option))||!Native950RunecraftingDivinationAssets.verified("npc",npc.getId()))return null;
  for(WispInfo info:WispInfo.values())if(npc.getId()==info.getNpcId()||npc.getId()==info.getEnrichedNpcId()||npc.getId()==info.getSpringNpcId()||npc.getId()==info.getEnrichedSpringNpcId())
   return supports(info)?info:null;
  return null;
 }

 /** Original MemoryInfo ordinals remain stable; slot zero is preserved for old snapshots. */
 public static final int BOON_SLOTS=12;
 public static boolean[] validateBoons(boolean[] flags){if(flags==null||flags.length!=BOON_SLOTS)throw new IllegalArgumentException("Divination boon slots");return flags.clone();}
 public static boolean[] boons(Player p){return p.getBoons()==null?new boolean[BOON_SLOTS]:validateBoons(p.getBoons());}
 public static void restoreBoons(Player p,boolean[] flags){p.setBoons(flags==null?new boolean[BOON_SLOTS]:validateBoons(flags));}
 public static final class Boon {
  public final int itemId,index,level,cost;public final double xp;public final String name;private final int[] energies;
  private Boon(int id,int index,com.rs.cache.loaders.ItemDefinitions d,int[] energies){itemId=id;this.index=index;level=d.getCSOpcode(2645,0);cost=d.getCSOpcode(2665,0);xp=d.getCSOpcode(2697,0)/10.0;name=d.name;this.energies=energies.clone();}
  public int[] energyIds(){return energies.clone();}
  boolean accepts(int id){for(int energy:energies)if(energy==id)return true;return false;}
 }
 /** Recipe requirements, costs, alternatives and XP come from exact paired950 items/structs. */
 public static List<Boon> boonRecipes(){
  List<Boon> recipes=new ArrayList<>();
  for(int index=1;index<BOON_SLOTS;index++){
   int id=29372+index;if(!Native950RunecraftingDivinationAssets.verified("item",id))throw new IllegalStateException("Unverified Divination boon "+id);
   com.rs.cache.loaders.ItemDefinitions d=com.rs.cache.loaders.ItemDefinitions.getItemDefinitions(id);
   MemoryInfo tier=MemoryInfo.values()[index];String expected="Boon of "+tier.name().toLowerCase(Locale.ROOT)+" energy";
   int struct=d.getCSOpcode(2675,0);
   if(!expected.equals(d.name)||d.getCSOpcode(2640,-1)!=Skills.DIVINATION+1||d.getCSOpcode(2696,-1)!=Skills.DIVINATION+1
      ||d.getCSOpcode(2645,0)!=tier.getLevel()||d.getCSOpcode(2665,0)<1||d.getCSOpcode(2665,0)>10000||d.getCSOpcode(2697,0)<1
      ||!Native950RunecraftingDivinationAssets.verified("struct",struct))throw new IllegalStateException("Invalid Divination boon recipe "+id);
   com.rs.cache.loaders.rs3.RS3GeneralRequirementMap alternatives=com.rs.cache.loaders.rs3.RS3GeneralRequirementMap.getMap(struct);
   List<Integer> energies=new ArrayList<>();for(int key=2655;key<=2664;key++){int energy=alternatives.getIntValue(key);if(energy==0)continue;
    if(!isWeavable(energy,1))throw new IllegalStateException("Unverified boon energy "+energy);energies.add(energy);}
   if(energies.size()<2||energies.size()>3||!energies.contains(tier.getEnergyId()))throw new IllegalStateException("Invalid boon alternatives "+id);
   recipes.add(new Boon(id,index,d,energies.stream().mapToInt(Integer::intValue).toArray()));
  }return Collections.unmodifiableList(recipes);
 }
 public static boolean isWeavable(int id,int option){Native950ItemCatalog.Entry e=(id>=29313&&id<=29324||id==31312)?Native950RunecraftingDivinationAssets.item(id):null;return option==1&&e!=null&&"Weave".equals(e.option(option));}
 static boolean weaveReady(Player p){return p!=null&&p.isNative950()&&Native950Runecrafting.stationary(p)&&!Native950Thieving.underCombat(p);}
 public static List<Native950ProductionMenu.Choice> weaveChoices(Player p,int slot,int id,int option){
  if(!weaveReady(p)||slot<0||slot>=28||!isWeavable(id,option))return Collections.emptyList();
  Item selected=p.getInventory().getItem(slot);if(selected==null||selected.getId()!=id)return Collections.emptyList();
  int count=selected.getAmount();WorldTile origin=new WorldTile(p);Object controller=p.getControlerManager().getControler();
  List<Native950ProductionMenu.Choice> choices=new ArrayList<>();
  for(Boon recipe:boonRecipes())if(recipe.accepts(id)&&!boon(p,recipe.index))choices.add(new Native950ProductionMenu.Choice(
   recipe.name+" (level "+recipe.level+", "+recipe.cost+" energy)",()->{
    if(!weaveReady(p)||!p.matches(origin)||controller!=p.getControlerManager().getControler()||p.getInventory().getItem(slot)!=selected||selected.getAmount()!=count){p.sendMessage("Your items or position changed. Choose Weave again.");return;}
    p.getActionManager().setAction(new WeaveBoon(slot,selected,count,recipe));}));
  if(choices.isEmpty())p.sendMessage("You have already unlocked every supported boon made with this energy.");
  return choices;
 }
 static final class WeaveBoon extends com.rs.game.player.actions.Action {
  final int slot,count;final Item selected;final Boon recipe;WorldTile origin;Object controller;boolean ended;
  WeaveBoon(int slot,Item selected,int count,Boon recipe){this.slot=slot;this.selected=selected;this.count=count;this.recipe=recipe;}
  String refusal(Player p){
   if(ended||!weaveReady(p)||origin==null||!p.matches(origin)||controller!=p.getControlerManager().getControler())return "You cannot weave a boon while moving or in combat.";
   if(p.getInventory().getItem(slot)!=selected||selected.getAmount()!=count||!recipe.accepts(selected.getId()))return "That energy stack has changed. Choose Weave again.";
   if(boon(p,recipe.index))return "You have already unlocked this boon.";
   if(p.getSkills().getLevel(Skills.DIVINATION)<recipe.level)return "You need Divination level "+recipe.level+" to weave this boon.";
   if(count<recipe.cost)return "You need "+recipe.cost+" "+Native950Production.name(selected.getId())+" to weave this boon.";
   if(!Native950Skilling.canExchangeSlot(p,slot,selected.getId(),recipe.cost,new Item[0]))return "That energy cannot be used to weave a boon.";return null;
  }
  public boolean start(Player p){origin=new WorldTile(p);controller=p.getControlerManager().getControler();String why=refusal(p);if(why!=null){p.sendMessage(why);return false;}setActionDelay(p,1);return true;}
  public boolean process(Player p){String why=refusal(p);if(why!=null&&!ended)p.sendMessage(why);return why==null;}
  public int processWithDelay(Player p){if(!process(p))return -1;boolean[] flags=boons(p);if(!Native950Skilling.exchangeSlot(p,slot,selected.getId(),recipe.cost,new Item[0]))return -1;
   flags[recipe.index]=true;restoreBoons(p,flags);ended=true;p.getSkills().addXp(Skills.DIVINATION,recipe.xp);p.sendMessage("You weave the "+recipe.name.toLowerCase(Locale.ROOT)+". Converting this tier's memories now gives 10% more experience and energy.");return -1;}
  public void stop(Player p){ended=true;setActionDelay(p,1);}
 }

 static boolean boon(Player p,int index){boolean[] boons=p.getBoons();return boons!=null&&index>=0&&index<boons.length&&boons[index];}
 public static boolean supports(WispInfo info){return info!=null&&Native950RunecraftingDivinationAssets.item(info.getMemoryId())!=null
  &&Native950RunecraftingDivinationAssets.item(info.getEnergyId())!=null&&Native950RunecraftingDivinationAssets.item(info.getEnrichedMemoryId())!=null
  &&Native950RunecraftingDivinationAssets.effects(21231,4235)&&Native950RunecraftingDivinationAssets.verified("graphic",4236);}
 public static boolean isHarvestable(NPC npc,int option){return definition(npc,option)!=null;}
 public static boolean startHarvest(Player p,NPC npc,int option){WispInfo info=definition(npc,option);return p!=null&&p.isNative950()&&info!=null&&p.getActionManager().setAction(new DivinationHarvest(npc,info));}
 public static boolean isRift(WorldObject rift){
  if(rift==null||rift.getType()<0||rift.getType()>22||!Native950RunecraftingDivinationAssets.verified("object",rift.getId()))return false;
  com.rs.cache.loaders.ObjectDefinitions d=rift.getDefinitions();return rift.getId()==87306&&d.loaded&&d.transforms==null&&"Energy rift".equals(d.name)
   &&d.options!=null&&d.options.length>0&&"Convert memories".equals(d.options[0])
   &&Native950RunecraftingDivinationAssets.effects(21232,4239)&&Native950RunecraftingDivinationAssets.effects(21234,4240);
 }
 public static boolean startConvert(Player p,WorldObject rift,int option){return p!=null&&option==1&&startConvert(p,rift,p.getConvertMode());}
 public static boolean startConvert(Player p,WorldObject rift,ConvertMode mode){return p!=null&&p.isNative950()&&mode!=null&&isRift(rift)&&p.getActionManager().setAction(new DivinationConvert(rift,mode));}
 public static Native950ItemCatalog.Entry itemEntry(int id){return id>=29313&&id<=29324||id==31312||id>=29384&&id<=29406?Native950RunecraftingDivinationAssets.item(id):null;}
 public static void verifyCacheBindings(){Native950RunecraftingDivinationAssets.verifyAll();boonRecipes();for(WispInfo info:WispInfo.values())if(!supports(info))throw new IllegalStateException("Unverified Divination tier "+info);if(!isRift(new WorldObject(87306,10,0,3217,3258,0)))throw new IllegalStateException("Unverified energy rift");}
 public static List<Native950ProductionMenu.Choice> conversionChoices(final Player p,final WorldObject rift){
  if(p==null||!isRift(rift)||!Native950Mining.current(rift)||!Native950Mining.inReach(p,rift))return Collections.emptyList();
  List<Native950ProductionMenu.Choice> choices=new ArrayList<>();
  choices.add(new Native950ProductionMenu.Choice("Convert to energy",(Runnable)()->startConvert(p,rift,ConvertMode.CONVERT_TO_ENERGY)));
  choices.add(new Native950ProductionMenu.Choice("Convert to experience",(Runnable)()->startConvert(p,rift,ConvertMode.CONVERT_TO_XP)));
  choices.add(new Native950ProductionMenu.Choice("Use energy for more experience",(Runnable)()->startConvert(p,rift,ConvertMode.CONVERT_TO_MORE_XP)));
  return choices;
 }
 public static List<Native950ItemCatalog.Entry> itemEntries(){
  Map<Integer,Native950ItemCatalog.Entry> items=new LinkedHashMap<>();for(WispInfo info:WispInfo.values())for(int id:new int[]{info.getEnergyId(),info.getMemoryId(),info.getEnrichedMemoryId()}){
   Native950ItemCatalog.Entry item=Native950RunecraftingDivinationAssets.item(id);if(item!=null)items.put(id,item);
  }Native950ItemCatalog.Entry elder=Native950RunecraftingDivinationAssets.item(31312);if(elder!=null)items.put(31312,elder);return Collections.unmodifiableList(new ArrayList<>(items.values()));
 }
 static boolean npcReach(Player p,NPC npc){
  if(npc.getIndex()<1||World.getNPCs().get(npc.getIndex())!=npc||npc.hasFinished()||npc.isDead()||npc.isCantInteract()||npc.getPlane()!=p.getPlane())return false;
  int size=npc.getSize(),x=p.getX(),y=p.getY();
  if(x<npc.getX()-1||x>npc.getX()+size||y<npc.getY()-1||y>npc.getY()+size||x>=npc.getX()&&x<npc.getX()+size&&y>=npc.getY()&&y<npc.getY()+size)return false;
  int route=RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER,x,y,p.getPlane(),p.getSize(),new EntityStrategy(npc),false);
  return route==0&&!RouteFinder.lastIsAlternative();
 }
 public static final class HarvestJourney {
  final NPC npc;final WispInfo info;final WorldTile npcTile;final int npcId;WorldTile origin;Object controller;boolean stopped;int animationDelay;HarvestPause pause;
  public HarvestJourney(NPC npc,WispInfo info){this.npc=npc;this.info=info;this.npcTile=npc==null?null:new WorldTile(npc);npcId=npc==null?-1:npc.getId();}
  public boolean start(Player p){
   origin=new WorldTile(p);controller=p.getControlerManager().getControler();
   if(p.getNextWalkDirection()!=-1||p.hasTeleported()||!environment(p)){
    p.sendMessage("You cannot harvest that wisp from here. Move beside it and try again.");return false;
   }
   if(!valid(p))return false;
   pause=pauseHarvestWander(npc);
   try{animate(p);return true;}catch(RuntimeException failure){releasePause();throw failure;}
  }
  private void releasePause(){HarvestPause owned=pause;pause=null;releaseHarvestWander(npc,owned);}
  public boolean process(Player p){if(!valid(p))return false;if(--animationDelay<=0)animate(p);return true;}
  private boolean environment(Player p){
   return !stopped&&controller==p.getControlerManager().getControler()&&!Native950Thieving.underCombat(p)&&Native950Runecrafting.stationary(p)&&origin!=null&&p.matches(origin)&&npc!=null&&npc.getId()==npcId&&npc.matches(npcTile)&&definition(npc,1)==info&&npcReach(p,npc);
  }
  private boolean valid(Player p){
   if(!environment(p))return false;
   if(p.getSkills().getLevel(Skills.DIVINATION)<info.getLevel()){p.sendMessage("You need a Divination level of "+info.getLevel()+" to harvest here.");return false;}
   if(!Native950Skilling.canGiveItems(p,new Item[]{new Item(info.getEnergyId(),DivinationHarvest.ordinaryEnergyAmount(p.getSkills().getLevel(Skills.DIVINATION))),new Item(info.getMemoryId(),1)})){
    p.sendMessage("Your backpack has no room for more memories and energy. Bank some items or convert your memories, then try again.");return false;}
   return true;
  }
  public int harvest(Player p){
   if(!valid(p))return -1;int level=p.getSkills().getLevel(Skills.DIVINATION);
   boolean memory=ThreadLocalRandom.current().nextInt(100)<DivinationHarvest.ordinaryMemoryChance(level,info);
   boolean enrichedSpring=info!=WispInfo.PALE&&(npcId==info.getEnrichedNpcId()||npcId==info.getEnrichedSpringNpcId());
   boolean enriched=memory&&info!=WispInfo.PALE&&(enrichedSpring||ThreadLocalRandom.current().nextInt(100)<DivinationHarvest.ordinaryEnrichedChance(level,info));
   List<Item> outputs=new ArrayList<>();outputs.add(new Item(info.getEnergyId(),DivinationHarvest.ordinaryEnergyAmount(level)));
   if(memory)outputs.add(new Item(enriched?info.getEnrichedMemoryId():info.getMemoryId(),1));
   if(!Native950Skilling.giveItems(p,outputs.toArray(new Item[0])))return -1;
   p.getSkills().addXp(Skills.DIVINATION,info.getHarvestXp()*(enriched||enrichedSpring?2:1));
   p.setNextGraphics(new Graphics(enriched?4236:4235));if(memory)p.addMemoriesCollected();return 3;
  }
  void animate(Player p){p.setNextAnimation(new Animation(21231));p.setNextFaceEntity(npc);animationDelay=Native950Mining.animationDelay(21231);}
  public void stop(Player p){stopped=true;releasePause();p.setNextAnimation(new Animation(-1));p.setNextFaceEntity(null);}
 }
 public static final class ConvertJourney {
  final WorldObject rift;final ConvertMode mode;WorldTile origin;Object controller;MemoryInfo info;boolean enriched,stopped;
  public ConvertJourney(WorldObject rift,ConvertMode mode){this.rift=rift;this.mode=mode;}
  public boolean start(Player p){origin=new WorldTile(p);controller=p.getControlerManager().getControler();if(p.getNextWalkDirection()!=-1||p.hasTeleported()||!process(p)){p.sendMessage("You have no suitable memories to convert here.");return false;}p.setNextFaceWorldTile(rift);return true;}
  public boolean process(Player p){
   if(stopped||controller!=p.getControlerManager().getControler()||Native950Thieving.underCombat(p)||!Native950Runecrafting.stationary(p)||origin==null||!p.matches(origin)||!isRift(rift)||!Native950Mining.current(rift)||!Native950Mining.inReach(p,rift))return false;
   if(mode==ConvertMode.CONVERT_TO_MORE_XP)for(MemoryInfo candidate:MemoryInfo.values())if(candidate!=MemoryInfo.PALE&&select(p,candidate,true))return true;
   for(MemoryInfo candidate:MemoryInfo.values())if(select(p,candidate,false)||(mode!=ConvertMode.CONVERT_TO_MORE_XP&&candidate!=MemoryInfo.PALE&&select(p,candidate,true)))return true;
   return false;
  }
  boolean select(Player p,MemoryInfo candidate,boolean enriched){
   int id=enriched?candidate.getEnrichedMemoryId():candidate.getMemoryId();
   if(p.getSkills().getLevel(Skills.DIVINATION)<candidate.getLevel()||Native950RunecraftingDivinationAssets.item(id)==null||!p.getInventory().containsItem(id,1))return false;
   info=candidate;this.enriched=enriched;return true;
  }
  public int convert(Player p){
   if(!process(p))return -1;
   boolean more=mode==ConvertMode.CONVERT_TO_MORE_XP;int cost=enriched?10:5;
   boolean paid=more&&p.getInventory().containsItem(info.getEnergyId(),cost);
   List<Item> inputs=new ArrayList<>();inputs.add(new Item(enriched?info.getEnrichedMemoryId():info.getMemoryId(),1));if(paid)inputs.add(new Item(info.getEnergyId(),cost));
   Item[] outputs=new Item[0];
   if(mode==ConvertMode.CONVERT_TO_ENERGY){double rate=DivinationConvert.ordinaryEnergyRate(info,p.getSkills().getLevel(Skills.DIVINATION),enriched,boon(p,info.ordinal()));int amount=(int)rate+(ThreadLocalRandom.current().nextDouble()<rate-(int)rate?1:0);outputs=new Item[]{new Item(info.getEnergyId(),Math.max(1,amount))};}
   if(!Native950Skilling.exchange(p,inputs.toArray(new Item[0]),outputs)){p.sendMessage("Your backpack cannot hold the converted energy.");return -1;}
   p.getSkills().addXp(Skills.DIVINATION,DivinationConvert.ordinaryXp(info,enriched,mode,paid,boon(p,info.ordinal())));
   boolean energy=mode==ConvertMode.CONVERT_TO_ENERGY;p.setNextAnimation(new Animation(energy?21232:21234));p.setNextGraphics(new Graphics(energy?4239:4240));return 1;
  }
  public void stop(Player p){stopped=true;p.setNextAnimation(new Animation(-1));}
 }
}
