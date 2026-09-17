package com.rs.game.player.client;
import com.google.gson.Gson;
import com.rs.cache.Cache;import com.rs.cache.loaders.*;import com.rs.cache.loaders.rs3.RS3GeneralRequirementMap;
import com.rs.game.*;import com.rs.game.item.Item;import com.rs.game.player.Player;import com.rs.game.player.Skills;import com.rs.game.player.actions.Action;
import com.rs.utils.data.parsers.items.pojos.ItemDisassembleData;import com.rs.utils.data.parsers.items.pojos.ItemDisassembleData.Component;
import java.io.*;import java.util.*;
/** Original910 ordinary disassembly tables and Manufacture param contracts through native950 transactions. */
public final class Native950Invention {
 public static final int MATERIAL_SLOTS=128;public static final int JUNK_SLOT=82;private static final String STATE="native950.invention.materials";
 private static final int[] PRODUCTS={36389,36719,36721,36723,36725,36730,36390,41073};
 private static final int[] STATIONS={100873,100874,100875,100876,125056,125057};
 private static final Map<Integer,Row> ROWS=new LinkedHashMap<>();private static Object recipeStore;private static List<Recipe> recipes;
 static final class Row {int itemId;String name;ItemDisassembleData data;}
 public static int[] materials(Player p){Object v=p.getTemporaryAttributtes().get(STATE);if(!(v instanceof int[])){v=new int[MATERIAL_SLOTS];p.getTemporaryAttributtes().put(STATE,v);}return ((int[])v).clone();}
 public static void restoreMaterials(Player p,int[] values){if(values==null){p.getTemporaryAttributtes().put(STATE,new int[MATERIAL_SLOTS]);return;}if(values.length!=MATERIAL_SLOTS)throw new IllegalArgumentException("Invention material slots");for(int n:values)if(n<0)throw new IllegalArgumentException("Negative components");p.getTemporaryAttributtes().put(STATE,values.clone());}
 private static synchronized void load(){if(!ROWS.isEmpty())return;try(Reader r=Native950InventionAssets.reader("invention-disassembly-950.json")){for(Row row:new Gson().fromJson(r,Row[].class))if(validData(row.data))ROWS.put(row.itemId,row);}catch(IOException e){throw new IllegalStateException(e);}}
 static boolean validData(ItemDisassembleData d){if(d==null||d.getRequiredQuantity()<1||d.getRequiredQuantity()>10000||d.getMaterialCount()<1||d.getMaterialCount()>1000||!Double.isFinite(d.getXp())||d.getXp()<0||!Double.isFinite(d.getJunkChance())||d.getJunkChance()<0||d.getJunkChance()>100||d.getComponents()==null||d.getComponents().length==0)return false;for(Component c:d.getComponents())if(c==null||c.getId()<0||c.getId()>=MATERIAL_SLOTS||c.getAmount()<1||c.getAmount()>1000||!Double.isFinite(c.getChance())||c.getChance()<=0)return false;return true;}
 public static ItemDisassembleData disassembly(int id){load();id=baseItem(id);Row row=ROWS.get(id);if(row==null||!Native950InventionAssets.item(id)||!row.name.equals(ItemDefinitions.getItemDefinitions(id).name))return null;
  for(Component c:row.data.getComponents()){String name=materialName(c.getId());if(!c.getName().equalsIgnoreCase(name))return null;}return row.data;
 }
 /** Resolve ordinary notes using current950 template links; never bind/lend/shard aliases. */
 static int baseItem(int id){
  if(Cache.STORE==null||!Cache.isFlatReadOnly())return id;
  ItemDefinitions d=Native950CacheItems.definition(id);if(d==null||d.lendTemplateId!=-1||d.bindTemplateId!=-1||d.shardTemplateId!=-1)return -1;
  if(d.certTemplateId==-1)return id;
  ItemDefinitions base=Native950CacheItems.definition(d.certId);
  return base!=null&&base.certTemplateId==-1&&base.lendTemplateId==-1&&base.bindTemplateId==-1&&base.shardTemplateId==-1&&base.certId==id?d.certId:-1;
 }
 public static List<Native950ProductionMenu.Choice> pouchChoices(Player p){
  List<Native950ProductionMenu.Choice> out=new ArrayList<>();String why=requirement(p);if(why!=null){p.sendMessage(why);return out;}
  out.add(new Native950ProductionMenu.Choice("View stored components",()->showMaterials(p)));
  Set<Integer> seen=new HashSet<>();for(int slot=0;slot<28;slot++){
   Item item=p.getInventory().getItem(slot);if(!ordinary(item)||!seen.add(item.getId())||disassembly(item.getId())==null)continue;
   final int id=item.getId();out.add(new Native950ProductionMenu.Choice("Disassemble "+Native950Production.name(id),n->startDisassemble(p,null,id,n)));
   out.add(new Native950ProductionMenu.Choice("Analyse "+Native950Production.name(id),()->analyse(p,id)));
  }return out;
 }
 public static void analyse(Player p,int id){
  ItemDisassembleData d=disassembly(id);if(d==null){p.sendMessage("That item has no verified disassembly data yet.");return;}
  p.sendMessage(Native950Production.name(id)+": consumes "+d.getRequiredQuantity()+" per action; "+d.getMaterialCount()+" material rolls; "+d.getJunkChance()+"% base junk chance; "+String.format(java.util.Locale.ROOT,"%.2f",Native950InventionResearch.effectiveJunk(d.getJunkChance(),Native950InventionResearch.tier(p)))+"% after research.");
  StringJoiner names=new StringJoiner(", ");for(Component c:d.getComponents())names.add(materialName(c.getId()));p.sendMessage("Possible materials: "+names.toString()+".");
 }
 public static List<Native950ProductionMenu.Choice> disassemblyChoices(Player p,int slot,int id){
  List<Native950ProductionMenu.Choice> out=new ArrayList<>();Item selected=p.getInventory().getItem(slot);
  if(!ordinary(selected)||selected.getId()!=id){p.sendMessage("The selected item has changed.");return out;}
  String why=requirement(p);if(why!=null){p.sendMessage(why);return out;}
  if(disassembly(id)==null){p.sendMessage("That item has no verified disassembly data yet.");return out;}
  analyse(p,id);
  // Opening a quantity prompt is non-destructive. Keep the selected instance until confirmation.
  out.add(new Native950ProductionMenu.Choice("Disassemble "+Native950Production.name(id),n->{
   if(p.getInventory().getItem(slot)!=selected){p.sendMessage("The selected item has changed. Drag it again.");return;}
   startDisassemble(p,null,id,n);
  }));return out;
 }
 public static String materialName(int slot){if(Cache.STORE==null||!Native950InventionAssets.pin("enum",10742))return "material "+slot;ClientScriptMap map=ClientScriptMap.getMap(10742);if(slot<0||slot>=map.getSize())return "material "+slot;int data=map.getIntValue(slot);return Native950InventionAssets.pin("invention",data)?InventionDefinitions.getMaterialName(slot):"material "+slot;}
 public static boolean isStation(WorldObject o){if(o==null||!Native950InventionAssets.pin("object",o.getId()))return false;for(int id:STATIONS)if(id==o.getId())return o.getDefinitions().loaded&&o.getDefinitions().transforms==null&&"Inventor's workbench".equals(o.getDefinitions().name);return false;}
 public static boolean accepts(WorldObject o,int op){if(!isStation(o)||op<1||op>5)return false;String label=o.getDefinitions().options[op-1];return "Manufacture".equals(label)||"Discover".equals(label);}
 static boolean reach(Player p,WorldObject o){return disassemblyReady(p)&&!p.isUnderCombat()&&isStation(o)&&Native950Mining.current(o)&&Native950Mining.inReach(p,o);}
 public static String requirement(Player p){for(int skill:new int[]{Skills.CRAFTING,Skills.DIVINATION,Skills.SMITHING})if(p.getSkills().getLevelForXp(skill)<80)return "You need level 80 "+Skills.SKILL_NAME[skill]+" to use Invention.";return null;}
 public static List<Native950ProductionMenu.Choice> choices(Player p,WorldObject o){List<Native950ProductionMenu.Choice> out=new ArrayList<>();if(!reach(p,o))return out;String why=requirement(p);if(why!=null){p.sendMessage(why);return out;}
  out.add(new Native950ProductionMenu.Choice("View stored components",()->showMaterials(p)));
  out.add(Native950InventionResearch.nextChoice(p,o));
  Set<Integer> seen=new HashSet<>();for(int slot=0;slot<28;slot++){Item item=p.getInventory().getItem(slot);if(item==null||!ordinary(item)||!seen.add(item.getId())||disassembly(item.getId())==null)continue;final int id=item.getId();out.add(new Native950ProductionMenu.Choice("Disassemble "+Native950Production.name(id),n->startDisassemble(p,o,id,n)));}
  for(Recipe r:recipes())out.add(new Native950ProductionMenu.Choice("Manufacture "+Native950Production.name(r.product)+" (level "+r.level+")",n->startManufacture(p,o,r,n)));return out;
 }
 public static void showMaterials(Player p){int[] stock=materials(p);boolean any=false;for(int i=0;i<stock.length;i++)if(stock[i]>0){p.sendMessage(materialName(i)+": "+stock[i]);any=true;}if(!any)p.sendMessage("Your component pouch is empty. Drag an ordinary item onto the lightbulb in your backpack, or choose Disassemble from this menu.");}
 private static boolean ordinary(Item i){return i!=null&&i.getAttributes()==null&&i.getCharges()==0&&i.getInventionData()==null;}
 static int[] roll(ItemDisassembleData data,Random random){return roll(data,random,0);}
 static int[] roll(ItemDisassembleData data,Random random,int researchTier){double junk=Native950InventionResearch.effectiveJunk(data.getJunkChance(),researchTier);int[] earned=new int[MATERIAL_SLOTS];List<Component> randoms=new ArrayList<>();double weight=0;
  for(Component c:data.getComponents())if(c.getChance()>=1)earned[c.getId()]=Math.addExact(earned[c.getId()],c.getAmount());else{randoms.add(c);weight+=c.getChance();}
  for(int i=0;i<data.getMaterialCount();i++){if(random.nextDouble()*100<junk||randoms.isEmpty()){earned[JUNK_SLOT]++;continue;}double point=random.nextDouble()*weight;Component chosen=randoms.get(randoms.size()-1);for(Component c:randoms){point-=c.getChance();if(point<0){chosen=c;break;}}earned[chosen.getId()]++;}return earned;
 }
 static boolean canAdd(int[] stock,int[] add){if(stock.length!=MATERIAL_SLOTS||add.length!=MATERIAL_SLOTS)return false;for(int i=0;i<stock.length;i++)if(stock[i]<0||add[i]<0||(long)stock[i]+add[i]>Integer.MAX_VALUE)return false;return true;}
 public static boolean startDisassemble(Player p,WorldObject o,int id,int amount){ItemDisassembleData d=disassembly(id);if(p==null||d==null||amount<1||amount>10000||!disassemblyReady(p))return false;return p.getActionManager().setAction(new DisassembleAction(o,id,d,Math.min(60,amount)));}
 static boolean disassemblyReady(Player p){return p!=null&&p.isNative950()&&!p.isLocked()&&!p.isDead()&&Native950Runecrafting.stationary(p)&&p.getNextWalkDirection()==-1&&!p.hasTeleported();}
 static int materialCap(int slot){return slot==JUNK_SLOT?2000000000:100000;}
 static int addCapped(int[] stock,int[] add){int lost=0;for(int i=0;i<stock.length;i++){long room=Math.max(0L,(long)materialCap(i)-stock[i]);int gain=(int)Math.min(room,add[i]);stock[i]+=gain;lost+=add[i]-gain;}return lost;}
 private static final class DisassembleAction extends Action {final WorldObject o;final int id;final ItemDisassembleData data;WorldTile origin;Object controller;int remaining;DisassembleAction(WorldObject o,int id,ItemDisassembleData data,int n){this.o=o;this.id=id;this.data=data;remaining=n;}
  String refusal(Player p){if(!disassemblyReady(p))return "Stand still before disassembling items.";String why=requirement(p);if(why!=null)return why;for(int slot=0;slot<28;slot++){Item item=p.getInventory().getItem(slot);if(item!=null&&item.getId()==id&&!ordinary(item))return "Charged or modified items cannot be disassembled in this first pass.";}if(!Native950Skilling.canExchange(p,new Item[]{new Item(id,data.getRequiredQuantity())},new Item[0]))return "You need "+data.getRequiredQuantity()+" ordinary "+Native950Production.name(id)+" per disassembly.";return null;}
  public boolean start(Player p){String why=refusal(p);if(why!=null){p.sendMessage(why);return false;}origin=new WorldTile(p);controller=p.getControlerManager().getControler();setActionDelay(p,1);return true;}
  public boolean process(Player p){if(remaining<=0||p.getActionManager().getAction()!=this||origin==null||!origin.matches(p)||controller!=p.getControlerManager().getControler()||p.isUnderCombat())return false;String why=refusal(p);if(why!=null)p.sendMessage(why);return why==null;}
  public int processWithDelay(Player p){if(!process(p))return -1;int[] stock=materials(p),add=roll(data,new Random(),Native950InventionResearch.tier(p));
   if(!Native950Skilling.exchange(p,new Item[]{new Item(id,data.getRequiredQuantity())},new Item[0]))return -1;int lost=addCapped(stock,add);if(lost>0)p.sendMessage(lost+" materials were discarded because those components are at their storage cap.");restoreMaterials(p,stock);p.getSkills().addXp(Skills.INVENTION,data.getXp());p.setNextAnimation(new Animation(27997));remaining--;p.sendMessage("Disassembled "+data.getRequiredQuantity()+" x "+Native950Production.name(id)+".");return 3;}
  public void stop(Player p){p.setNextAnimation(new Animation(-1));setActionDelay(p,1);}
 }
 public static final class Recipe {public final int product,quantity,level;final int[] componentCosts;final Item[] inputs;final Item[] alternatives;final double inventionXp,divinationXp,craftingXp;
  Recipe(int product,int quantity,int level,int[] costs,Item[] inputs,Item[] alternatives,double inv,double div,double craft){this.product=product;this.quantity=quantity;this.level=level;componentCosts=costs;this.inputs=inputs;this.alternatives=alternatives;inventionXp=inv;divinationXp=div;craftingXp=craft;}
 }
 public static synchronized List<Recipe> recipes(){if(Cache.STORE==null)return Collections.emptyList();if(recipeStore==Cache.STORE&&recipes!=null)return recipes;recipeStore=Cache.STORE;List<Recipe> out=new ArrayList<>();for(int id:PRODUCTS){Recipe r=decode(id);if(r!=null)out.add(r);}recipes=Collections.unmodifiableList(out);return recipes;}
 private static Recipe decode(int id){if(!Native950InventionAssets.item(id)||!Native950InventionAssets.pin("enum",10742))return null;ItemDefinitions d=ItemDefinitions.getItemDefinitions(id);if(d.getCSOpcode(2640,0)!=27||d.getCSOpcode(2696,0)!=27)return null;int level=d.getCSOpcode(2645,0),qty=d.getCSOpcode(2653,1);if(level<1||level>120||qty<1||qty>1000)return null;
  int[] costs=new int[MATERIAL_SLOTS];List<Item> inputs=new ArrayList<>(),alternatives=new ArrayList<>();for(int i=0;i<10;i++){int item=d.getCSOpcode(2655+i,0),amount=d.getCSOpcode(2665+i,0);if(item==0)continue;if(amount<1||amount>100000)return null;if(item==36365){int material=InventionDefinitions.getMaterialIndex(d.getCSOpcode(5456+i,-1));if(material<0||material>=MATERIAL_SLOTS||!Native950InventionAssets.pin("invention",ClientScriptMap.getMap(10742).getIntValue(material)))return null;costs[material]=Math.addExact(costs[material],amount);}else {if(!Native950InventionAssets.item(item))return null;inputs.add(new Item(item,amount));}}
  int struct=d.getCSOpcode(2675,0);if(struct!=0){if(!Native950InventionAssets.pin("struct",struct))return null;RS3GeneralRequirementMap map=RS3GeneralRequirementMap.getMap(struct);for(int i=0;i<10;i++){int item=map.getIntValue(2655+i),amount=map.getIntValue(2665+i);if(item>0&&amount>0&&Native950InventionAssets.item(item))alternatives.add(new Item(item,amount));}if(alternatives.isEmpty())return null;}
  double inv=d.getCSOpcode(2697,-1)/10.0,div=0,craft=0;for(int i=1;i<3;i++){int type=d.getCSOpcode(2696+i*2,0);double xp=d.getCSOpcode(2697+i*2,0)/10.0;if(type==26)div+=xp;else if(type==11)craft+=xp;else if(type!=0)return null;}if(inv<0)return null;
  return new Recipe(id,qty,level,costs,inputs.toArray(new Item[0]),alternatives.toArray(new Item[0]),inv,div,craft);
 }
 public static boolean startManufacture(Player p,WorldObject o,Recipe r,int n){return p!=null&&r!=null&&recipes().contains(r)&&n>0&&n<=10000&&reach(p,o)&&p.getActionManager().setAction(new ManufactureAction(o,r,n));}
 private static final class ManufactureAction extends Action {final WorldObject o;final Recipe r;WorldTile origin;Object controller;int remaining;ManufactureAction(WorldObject o,Recipe r,int n){this.o=o;this.r=r;remaining=n;}
  Item[] ingredients(Player p){List<Item> in=new ArrayList<>(Arrays.asList(r.inputs));if(r.alternatives.length>0){Item chosen=null;for(Item i:r.alternatives)if(p.getInventory().containsItem(i.getId(),i.getAmount())){chosen=i;break;}if(chosen==null)return null;in.add(chosen);}return in.toArray(new Item[0]);}
  String refusal(Player p){if(!reach(p,o))return "Move beside the inventor's workbench first.";String why=requirement(p);if(why!=null)return why;if(p.getSkills().getLevelForXp(Skills.INVENTION)<r.level)return "You need Invention level "+r.level+".";int[] stock=materials(p);for(int i=0;i<stock.length;i++)if(stock[i]<r.componentCosts[i])return "You need "+r.componentCosts[i]+" "+materialName(i)+" (stored: "+stock[i]+").";Item[] in=ingredients(p);if(in==null)return "You need suitable divine energy in your backpack.";if(!(in.length==0?Native950Skilling.canGiveItems(p,new Item[]{new Item(r.product,r.quantity)}):Native950Skilling.canExchange(p,in,new Item[]{new Item(r.product,r.quantity)})))return "You need the physical ingredients and backpack space for the product.";return null;}
  public boolean start(Player p){String why=refusal(p);if(why!=null){p.sendMessage(why);return false;}origin=new WorldTile(p);controller=p.getControlerManager().getControler();setActionDelay(p,1);return true;}
  public boolean process(Player p){if(remaining<=0||p.getActionManager().getAction()!=this||origin==null||!origin.matches(p)||controller!=p.getControlerManager().getControler())return false;String why=refusal(p);if(why!=null)p.sendMessage(why);return why==null;}
  public int processWithDelay(Player p){if(!process(p))return -1;int[] stock=materials(p);Item[] in=ingredients(p);if(in==null||!(in.length==0?Native950Skilling.giveItems(p,new Item[]{new Item(r.product,r.quantity)}):Native950Skilling.exchange(p,in,new Item[]{new Item(r.product,r.quantity)})))return -1;for(int i=0;i<stock.length;i++)stock[i]-=r.componentCosts[i];restoreMaterials(p,stock);p.getSkills().addXp(Skills.INVENTION,r.inventionXp);if(r.divinationXp>0)p.getSkills().addXp(Skills.DIVINATION,r.divinationXp);if(r.craftingXp>0)p.getSkills().addXp(Skills.CRAFTING,r.craftingXp);p.setNextAnimation(new Animation(27997));remaining--;return 3;}
  public void stop(Player p){p.setNextAnimation(new Animation(-1));setActionDelay(p,1);}
 }
 /** Public workshop near the Falador lodestone while guild entrance quest travel remains unported. */
 public static void populateRegion(int region){if(region==11829)Native950Archaeology.place(region,100874,2970,3406);}
 public static void verifyCacheBindings(){Native950InventionResearch.verifyCacheBindings();load();if(ROWS.size()!=10844)throw new IllegalStateException("Disassembly table count changed");if(!Native950InventionAssets.pin("sequence",27997)||!Native950InventionAssets.pin("enum",10742))throw new IllegalStateException("Invention assets changed");if(!"Junk".equalsIgnoreCase(materialName(JUNK_SLOT)))throw new IllegalStateException("Invention material index changed");for(int id:STATIONS)if(!isStation(new WorldObject(id,10,0,3217,3258,0)))throw new IllegalStateException("Invention station "+id);if(recipes().size()!=PRODUCTS.length)throw new IllegalStateException("Expected "+PRODUCTS.length+" manufacture recipes, got "+recipes().size());if(disassembly(1513)==null||disassembly(1205)==null)throw new IllegalStateException("Ordinary disassembly changed");}
 public static List<Native950ItemCatalog.Entry> itemEntries(){Map<Integer,Native950ItemCatalog.Entry> entries=new LinkedHashMap<>();for(Recipe r:recipes()){entries.put(r.product,Native950InventionAssets.itemEntry(r.product));for(Item i:r.inputs)entries.put(i.getId(),Native950InventionAssets.itemEntry(i.getId()));for(Item i:r.alternatives)entries.put(i.getId(),Native950InventionAssets.itemEntry(i.getId()));}return new ArrayList<>(entries.values());}
}





