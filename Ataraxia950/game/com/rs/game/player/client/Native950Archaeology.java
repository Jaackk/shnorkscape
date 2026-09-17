package com.rs.game.player.client;
import com.rs.cache.Cache;import com.rs.cache.loaders.ItemDefinitions;import com.rs.game.*;import com.rs.game.item.Item;import com.rs.game.player.Player;import com.rs.game.player.Skills;import com.rs.game.player.actions.Action;import java.util.*;import com.rs.game.hitbar.impl.MiningHitBar;
/** First950 Archaeology baseline;910 has no Archaeology implementation to port. */
public final class Native950Archaeology {
 public static final int MATERIAL_SLOTS=64;private static final String STATE="native950.archaeology.materials";
 // Stable saved slot mapping: append only. Never reorder when adding later excavation sites.
 static final int[] MATERIAL_IDS={49444,49445,49460,49514};
 private static final int[] MATTOCKS={49539,49542,49545,49548,49551,49554,49557,49560,49562,49564,49566,49571,49574,49581,49584};
 // Wiki mattock precision, paired with the already SHA-pinned950 item identities.
 private static final int[] PRECISION={5,10,15,20,25,30,40,50,60,70,50,60,70,80,85};
 private static final String[] MATTOCK_NAMES={"Bronze mattock","Iron mattock","Steel mattock","Mithril mattock","Adamant mattock","Rune mattock","Orikalkum mattock","Necronium mattock","Bane mattock","Elder rune mattock","Dragon mattock","Crystal mattock","Imcando mattock","Mattock of Time and Space","Guildmaster Tony's mattock"};
 private static Object store;private static List<Native950Production.Recipe> recipes;
 public enum Site {CENTURION(116393,1,49741,49741,49444,49445,5,5),VENATOR(117101,5,49921,49923,49460,49514,16,16);
  public final int object,level;final int first,second,left,right,leftCount,rightCount;
  Site(int object,int level,int first,int second,int left,int right,int leftCount,int rightCount){this.object=object;this.level=level;this.first=first;this.second=second;this.left=left;this.right=right;this.leftCount=leftCount;this.rightCount=rightCount;}
 }
 // Stable save slots: append only, independent of enum ordering when more sites are added.
 private static final Site[] SAVED_EXCAVATIONS={Site.CENTURION,Site.VENATOR};
 private static String progressKey(Site site){return "native950.archaeology.progress."+site.name();}
 public static int[] validateExcavationProgress(int[] values){
  if(values==null||values.length!=SAVED_EXCAVATIONS.length)throw new IllegalArgumentException("Archaeology excavation slots");
  int[] copy=values.clone();for(int i=0;i<copy.length;i++)if(copy[i]<0||copy[i]>SAVED_EXCAVATIONS[i].leftCount+SAVED_EXCAVATIONS[i].rightCount)throw new IllegalArgumentException("Invalid Archaeology excavation progress");return copy;
 }
 public static int[] excavationProgress(Player p){
  int[] values=new int[SAVED_EXCAVATIONS.length];for(int i=0;i<values.length;i++){Object saved=p.getTemporaryAttributtes().get(progressKey(SAVED_EXCAVATIONS[i]));if(saved instanceof Integer)values[i]=(Integer)saved;}return validateExcavationProgress(values);
 }
 public static void restoreExcavationProgress(Player p,int[] values){
  int[] copy=validateExcavationProgress(values);for(int i=0;i<copy.length;i++)p.getTemporaryAttributtes().put(progressKey(SAVED_EXCAVATIONS[i]),copy[i]);
 }
 public static int[] materials(Player p){Object v=p.getTemporaryAttributtes().get(STATE);if(!(v instanceof int[])){v=new int[MATERIAL_SLOTS];p.getTemporaryAttributtes().put(STATE,v);}return ((int[])v).clone();}
 public static void restoreMaterials(Player p,int[] v){if(v==null){p.getTemporaryAttributtes().put(STATE,new int[MATERIAL_SLOTS]);return;}if(v.length!=MATERIAL_SLOTS)throw new IllegalArgumentException("Archaeology material slots");for(int n:v)if(n<0)throw new IllegalArgumentException("Negative Archaeology materials");p.getTemporaryAttributtes().put(STATE,v.clone());}
 static int materialSlot(int id){for(int i=0;i<MATERIAL_IDS.length;i++)if(MATERIAL_IDS[i]==id)return i;return -1;}
 public static Site site(WorldObject o){if(o==null||!Native950InventionAssets.pin("object",o.getId())||!o.getDefinitions().loaded||o.getDefinitions().transforms!=null)return null;for(Site s:Site.values())if(s.object==o.getId()&&((s==Site.CENTURION&&"Centurion remains".equals(o.getDefinitions().name))||(s==Site.VENATOR&&"Venator remains".equals(o.getDefinitions().name))))return s;return null;}
 public static boolean isExcavation(WorldObject o){return site(o)!=null;}
 public static boolean isStation(WorldObject o){return o!=null&&(o.getId()==115421||o.getId()==125133)&&Native950InventionAssets.pin("object",o.getId())&&o.getDefinitions().loaded&&o.getDefinitions().transforms==null&&"Archaeologist's workbench".equals(o.getDefinitions().name);}
 public static boolean accepts(WorldObject o,int option){if(o==null||option<1||option>5||o.getDefinitions().options==null)return false;String op=o.getDefinitions().options[option-1];return isExcavation(o)?"Excavate".equals(op):isStation(o)&&("Restore".equals(op)||"Store".equals(op));}
 static boolean reach(Player p,WorldObject o){return p!=null&&p.isNative950()&&Native950Runecrafting.stationary(p)&&p.getNextWalkDirection()==-1&&!p.hasTeleported()&&Native950Mining.current(o)&&Native950Mining.inReach(p,o);}
 static int precision(int id){for(int i=0;i<MATTOCKS.length;i++)if(MATTOCKS[i]==id)return PRECISION[i];return 0;}
 static boolean usableMattock(Player p,int id){
  int index=-1;for(int i=0;i<MATTOCKS.length;i++)if(MATTOCKS[i]==id)index=i;
  if(index<0||!Native950InventionAssets.item(id))return false;ItemDefinitions d=ItemDefinitions.getItemDefinitions(id);
  return MATTOCK_NAMES[index].equals(d.name)&&d.getCSOpcode(770,-1)==Skills.ARCHAEOLOGY&&d.getCSOpcode(771,0)>0
          &&p.getSkills().getLevelForXp(Skills.ARCHAEOLOGY)>=d.getCSOpcode(771,0);
 }
 /** Equipped tool has priority, otherwise choose the strongest usable carried tool. */
 static int mattock(Player p){
  int equipped=p.getEquipment().getWeaponId();if(usableMattock(p,equipped))return equipped;
  int best=-1;for(int id:MATTOCKS)if(p.getInventory().containsItem(id,1)&&usableMattock(p,id)&&precision(id)>precision(best))best=id;
  if(best>=0)return best;
  for(int id:Native950Toolbelt.tools(p))if(usableMattock(p,id)&&precision(id)>precision(best))best=id;
  return best;
 }
 static int advanceProgress(Site site,int progress,int mattock){
  int precision=precision(mattock);if(precision<5||progress<0||progress>site.leftCount+site.rightCount)throw new IllegalArgumentException("Invalid excavation progress/tool");
  // Existing saves count bronze-mattock units; one unit is five precision.
  return Math.min(site.leftCount+site.rightCount,progress+precision/5);
 }
 static void progressBar(Player p,Site site,int progress,boolean remove){
  p.getNextHitBars().removeIf(bar->bar instanceof MiningHitBar&&bar.getType()==MiningHitBar.PROGRESS);
  p.getNextHitBars().add(remove?MiningHitBar.removeProgress():MiningHitBar.progress(progress*100/(site.leftCount+site.rightCount)));
 }
 public static boolean start(Player p,WorldObject o){Site s=site(o);return s!=null&&p!=null&&p.getActionManager().setAction(new Excavation(o,s));}
 private static final class Excavation extends Action {final WorldObject object;final Site site;int progress;WorldTile origin;Object controller;Excavation(WorldObject o,Site site){object=o;this.site=site;}
  String refusal(Player p){if(!reach(p,object)||site(object)!=site)return "Move beside the excavation spot first.";if(p.getSkills().getLevelForXp(Skills.ARCHAEOLOGY)<site.level)return "You need Archaeology level "+site.level+" to excavate these remains.";if(mattock(p)<0)return "You need a mattock you can use in your backpack, weapon slot, or tool belt.";return null;}
  public boolean start(Player p){String why=refusal(p);if(why!=null){p.sendMessage(why);return false;}origin=new WorldTile(p);controller=p.getControlerManager().getControler();Object saved=p.getTemporaryAttributtes().get(progressKey(site));progress=saved instanceof Integer?Math.max(0,Math.min(site.leftCount+site.rightCount,(Integer)saved)):0;p.setNextFaceWorldTile(object);progressBar(p,site,progress,false);p.sendMessage("You excavate with your "+Native950Production.name(mattock(p)).toLowerCase(Locale.ROOT)+".");setActionDelay(p,1);return true;}
  public boolean process(Player p){return origin!=null&&p.matches(origin)&&controller==p.getControlerManager().getControler()&&!Native950Thieving.underCombat(p)&&refusal(p)==null;}
  public int processWithDelay(Player p){if(!process(p))return -1;int total=site.leftCount+site.rightCount;boolean discovery=progress==total;int id=discovery?(com.rs.utils.Utils.random(2)==0?site.first:site.second):(com.rs.utils.Utils.random(2)==0?site.left:site.right);
   if(!Native950Skilling.giveItem(p,id,1)){p.sendMessage("Your backpack is full. Store materials at an Archaeology workbench, then return to excavate.");return -1;}
   p.getSkills().addXp(Skills.ARCHAEOLOGY,discovery?site.level*5.0:site.level*2.0);p.setNextAnimation(new Animation(830));progress=discovery?0:advanceProgress(site,progress,mattock(p));progressBar(p,site,progress,false);p.getTemporaryAttributtes().put(progressKey(site),progress);if(discovery)p.sendMessage("You uncover "+Native950Production.name(id)+". Restore it at an Archaeology workbench.");return 3;}
  public void stop(Player p){progressBar(p,site,0,true);p.setNextAnimation(new Animation(-1));setActionDelay(p,1);}
 }
 public static synchronized List<Native950Production.Recipe> recipes(){if(Cache.STORE==null)return Collections.emptyList();if(store==Cache.STORE&&recipes!=null)return recipes;store=Cache.STORE;List<Native950Production.Recipe> out=new ArrayList<>();for(int id:new int[]{49742,49922,49924}){if(!Native950InventionAssets.item(id))continue;ItemDefinitions d=ItemDefinitions.getItemDefinitions(id);if(d.getCSOpcode(2640,0)!=28||d.getCSOpcode(2641,0)!=64||d.getCSOpcode(2646,0)!=1||d.getCSOpcode(2696,0)!=28||d.getCSOpcode(2698,0)!=0)continue;List<Item> in=new ArrayList<>();boolean ok=true;for(int i=0;i<10;i++){int item=d.getCSOpcode(2655+i,0),amount=d.getCSOpcode(2665+i,0);if(item==0)continue;if(!Native950InventionAssets.item(item)||amount<1||amount>100000){ok=false;break;}if(i>0&&materialSlot(item)<0){ok=false;break;}in.add(new Item(item,amount));}int level=d.getCSOpcode(2645,0),xp=d.getCSOpcode(2697,-1);if(ok&&in.size()>1&&level>0&&xp>=0)out.add(new Native950Production.Recipe("Restore "+d.name,Skills.ARCHAEOLOGY,level,xp/10.0,-1,3,in.toArray(new Item[0]),new Item[]{new Item(id,1)}));}recipes=Collections.unmodifiableList(out);return recipes;}
 public static List<Native950ProductionMenu.Choice> choices(Player p,WorldObject o){List<Native950ProductionMenu.Choice> choices=new ArrayList<>();if(!isStation(o)||!reach(p,o))return choices;
  choices.add(new Native950ProductionMenu.Choice("Store backpack materials",()->storeMaterials(p,o)));choices.add(new Native950ProductionMenu.Choice("View stored materials",()->showMaterials(p)));
  int[] stock=materials(p);for(int i=0;i<MATERIAL_IDS.length;i++)if(stock[i]>0){final int id=MATERIAL_IDS[i];choices.add(new Native950ProductionMenu.Choice("Withdraw "+Native950Production.name(id)+" ("+stock[i]+" stored)",n->withdrawMaterials(p,o,id,n)));}
  for(Native950Production.Recipe r:recipes())choices.add(new Native950ProductionMenu.Choice(Native950Production.name(r.produced()[0].getId())+" (level "+r.level+")",n->startRestore(p,o,r,n)));return choices;
 }
 public static boolean storeMaterials(Player p,WorldObject o){if(!isStation(o)||!reach(p,o))return false;int[] stock=materials(p);List<Item> take=new ArrayList<>();for(int i=0;i<MATERIAL_IDS.length;i++){int n=p.getInventory().getAmountOf(MATERIAL_IDS[i]);if(n<1)continue;if((long)stock[i]+n>Integer.MAX_VALUE){p.sendMessage("Material storage is full.");return false;}take.add(new Item(MATERIAL_IDS[i],n));stock[i]+=n;}if(take.isEmpty()){p.sendMessage("You have no excavation materials to store.");return false;}if(!Native950Skilling.exchange(p,take.toArray(new Item[0]),new Item[0]))return false;restoreMaterials(p,stock);p.sendMessage("Your excavation materials are now stored for restoration.");return true;}
 public static void showMaterials(Player p){int[] stock=materials(p);for(int i=0;i<MATERIAL_IDS.length;i++)p.sendMessage(Native950Production.name(MATERIAL_IDS[i])+": "+stock[i]);}
 public static boolean startRestore(Player p,WorldObject o,Native950Production.Recipe r,int amount){return p!=null&&isStation(o)&&recipes().contains(r)&&amount>0&&amount<=10000&&p.getActionManager().setAction(new Restore(o,r,amount));}
 static Item[] backpackInputs(Native950Production.Recipe r){List<Item> result=new ArrayList<>();for(Item in:r.consumed())if(materialSlot(in.getId())<0)result.add(in);return result.toArray(new Item[0]);}
 /** A single restoration plan divides ingredients across the backpack and saved material store. */
 static final class RestorationPlan {
  final int[] before,after;final Item[] inputs;final String refusal;
  RestorationPlan(int[] before,int[] after,List<Item> inputs,String refusal){this.before=before;this.after=after;this.inputs=inputs.toArray(new Item[0]);this.refusal=refusal;}
 }
 static RestorationPlan restorationPlan(Player p,Native950Production.Recipe r){
  int[] before=materials(p),after=before.clone();List<Item> inputs=new ArrayList<>();
  for(Item in:r.consumed()){
   int slot=materialSlot(in.getId());if(slot<0){inputs.add(in);continue;}
   int carried=p.getInventory().getAmountOf(in.getId()),used=Math.min(carried,in.getAmount()),fromStore=in.getAmount()-used;
   if(after[slot]<fromStore)return new RestorationPlan(before,after,inputs,"You need "+in.getAmount()+" "+Native950Production.name(in.getId())+" (backpack: "+carried+", stored: "+before[slot]+").");
   // Using carried materials first also makes room for the restored artefact in a full backpack.
   if(used>0)inputs.add(new Item(in.getId(),used));after[slot]-=fromStore;
  }
  return new RestorationPlan(before,after,inputs,null);
 }
 public static boolean withdrawMaterials(Player p,WorldObject o,int id,int requested){
  int slot=materialSlot(id);if(slot<0||requested<1||requested>10000||!isStation(o)||!reach(p,o)||Native950Thieving.underCombat(p))return false;
  int[] stock=materials(p);int amount=Math.min(requested,stock[slot]);
  if(amount<1){p.sendMessage("You have no "+Native950Production.name(id)+" stored.");return false;}
  int low=0,high=amount;while(low<high){int n=low+(high-low+1)/2;if(Native950Skilling.hasSpace(p,id,n))low=n;else high=n-1;}
  if(low<1){p.sendMessage("Your backpack has no room for these materials.");return false;}
  if(!Native950Skilling.giveItem(p,id,low))return false;stock[slot]-=low;restoreMaterials(p,stock);p.sendMessage("You withdraw "+low+" "+Native950Production.name(id)+".");return true;
 }
 private static final class Restore extends Action {
  final WorldObject o;final Native950Production.Recipe r;int remaining;WorldTile origin;Object controller;boolean stopped;
  Restore(WorldObject o,Native950Production.Recipe r,int n){this.o=o;this.r=r;remaining=n;}
  String refusal(Player p){
   if(stopped||!reach(p,o)||!isStation(o))return "Move beside an Archaeology workbench first.";
   if(p.getSkills().getLevelForXp(Skills.ARCHAEOLOGY)<r.level)return "You need Archaeology level "+r.level+" to restore this artefact.";
   RestorationPlan plan=restorationPlan(p,r);if(plan.refusal!=null)return plan.refusal;
   if(!Native950Skilling.canExchange(p,plan.inputs,r.produced()))return "You need the damaged artefact and ordinary materials in your backpack, with room for the restored artefact.";return null;
  }
  public boolean start(Player p){String why=refusal(p);if(why!=null){p.sendMessage(why);return false;}origin=new WorldTile(p);controller=p.getControlerManager().getControler();p.setNextFaceWorldTile(o);setActionDelay(p,1);return true;}
  public boolean process(Player p){if(remaining<=0||origin==null||!p.matches(origin)||controller!=p.getControlerManager().getControler()||Native950Thieving.underCombat(p))return false;String why=refusal(p);if(why!=null)p.sendMessage(why);return why==null;}
  public int processWithDelay(Player p){if(!process(p))return -1;RestorationPlan plan=restorationPlan(p,r);
   if(plan.refusal!=null||!Arrays.equals(materials(p),plan.before)||!Native950Skilling.exchange(p,plan.inputs,r.produced()))return -1;
   restoreMaterials(p,plan.after);p.getSkills().addXp(Skills.ARCHAEOLOGY,r.xp);p.sendMessage("You restore "+Native950Production.name(r.produced()[0].getId())+".");remaining--;return 3;
  }
  public void stop(Player p){stopped=true;p.setNextAnimation(new Animation(-1));setActionDelay(p,1);}
 }
 public static List<Native950ItemCatalog.Entry> itemEntries(){Set<Integer> ids=new LinkedHashSet<>();for(int id:MATTOCKS)ids.add(id);for(int id:MATERIAL_IDS)ids.add(id);for(Native950Production.Recipe r:recipes()){for(Item i:r.consumed())ids.add(i.getId());for(Item i:r.produced())ids.add(i.getId());}List<Native950ItemCatalog.Entry> out=new ArrayList<>();for(int id:ids){Native950ItemCatalog.Entry e=Native950InventionAssets.itemEntry(id);if(e!=null)out.add(e);}return out;}
 /** Starter plots remain accessible without the retail tutorial's temporary instance. */
 public static void populateRegion(int region){if(region!=13365)return;place(region,116393,3363,3393);place(region,117101,3363,3384);}
 static void place(int region,int id,int anchorX,int anchorY){
  Region r=World.getRegion(region);List<WorldObject> existing=r.getAllObjects();if(existing==null)return;existing.addAll(r.getSpawnedObjects());for(WorldObject old:existing)if(old.getId()==id&&Math.abs(old.getX()-anchorX)<=12&&Math.abs(old.getY()-anchorY)<=12)return;
  for(int radius=0;radius<=10;radius++)for(int dx=-radius;dx<=radius;dx++)for(int dy=-radius;dy<=radius;dy++){
   if(Math.max(Math.abs(dx),Math.abs(dy))!=radius)continue;int x=anchorX+dx,y=anchorY+dy;WorldObject o=new WorldObject(id,10,0,x,y,0);int sx=o.getDefinitions().sizeX,sy=o.getDefinitions().sizeY;if(sx<1||sy<1||sx>8||sy>8)throw new IllegalStateException("Unexpected starter footprint "+id);boolean clear=true;
   for(int xx=x-1;xx<=x+sx&&clear;xx++)for(int yy=y-1;yy<=y+sy;yy++){if(((xx>>6)<<8|(yy>>6))!=region||!World.isFloorFree(0,xx,yy,1)){clear=false;break;}if(xx>=x&&xx<x+sx&&yy>=y&&yy<y+sy){for(int slot=0;slot<4;slot++)if(r.getObjectWithSlot(0,xx&63,yy&63,slot)!=null){clear=false;break;}}}
   if(clear){World.spawnObject(o);System.out.println("[Native950 skills] Placed "+o.getDefinitions().name+" at "+x+","+y+",0");return;}
  }throw new IllegalStateException("No clear starter skill footprint near "+anchorX+","+anchorY);
 }
 public static void verifyCacheBindings(){if(!Native950InventionAssets.pin("sequence",830))throw new IllegalStateException("Excavation animation changed");for(Site s:Site.values())if(site(new WorldObject(s.object,10,0,3217,3258,0))!=s)throw new IllegalStateException("Excavation site changed");for(int id:new int[]{115421,125133})if(!isStation(new WorldObject(id,10,0,3217,3258,0)))throw new IllegalStateException("Archaeology station changed");if(recipes().size()!=3||recipes().get(0).level!=1||recipes().get(1).level!=5||recipes().get(1).xp!=305.1)throw new IllegalStateException("Archaeology restoration recipes changed");}
}




