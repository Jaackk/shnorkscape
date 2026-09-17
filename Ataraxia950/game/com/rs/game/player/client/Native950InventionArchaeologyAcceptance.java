package com.rs.game.player.client;
import com.rs.cache.Cache;
import com.rs.cores.CoresManager;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.WorldObject;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.network.modern.Native950GameTransport;
import com.rs.network.modern.Native950Isaac;
import com.rs.network.protocol.modern950.Native950Packets;
import com.rs.network.protocol.modern950.Native950Protocol.ServerPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.ReferenceCountUtil;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
/** Full paired-cache production audit with original engine scheduling and native output framing. */
public final class Native950InventionArchaeologyAcceptance {
 private static int ticks,frames,stats;
 public static void main(String[] a)throws Exception{
  require(a.length==1,"Usage: Native950InventionArchaeologyAcceptance <950-cache>");require(NativeCacheVerification.isEnforced(),"Verification disabled");
  System.setProperty(Native950World.SPAWNS_PROPERTY,"false");System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");Cache.initFlatReadOnly(Paths.get(a[0]));
  Native950World.getInstance().execute(()->{Native950Invention.verifyCacheBindings();Native950Archaeology.verifyCacheBindings();try(Fixture f=new Fixture()){check(f);}return null;}).get(120,TimeUnit.SECONDS);
  require(CoresManager.getNative950Scheduler().failed()==0,"Scheduler failed");
  System.out.println("PASS: "+Native950Invention.recipes().size()+" Invention and "+Native950Archaeology.recipes().size()+" Archaeology recipes; "+ticks+" original engine ticks, "+frames+" decoded native frames, "+stats+" stat updates. No account save, listener, or authentication.");
  System.out.println("LIMIT: live client menu and animation pixels require manual verification; excavation animation is generic digging, and blueprint/relic systems are deferred.");
 }
 private static void check(Fixture f){
  f.level(Skills.CRAFTING,99);f.level(Skills.SMITHING,99);f.level(Skills.DIVINATION,99);f.level(Skills.INVENTION,120);f.level(Skills.ARCHAEOLOGY,99);
  WorldObject inventor=f.station(100874),arch=f.station(115421),centurion=f.station(116393),venator=f.station(117101);
  for(Native950Invention.Recipe r:Native950Invention.recipes()){
   f.clear();f.beside(inventor);Native950Invention.restoreMaterials(f.p,r.componentCosts);for(Item i:r.inputs)f.give(i.getId(),i.getAmount());if(r.alternatives.length>0)f.give(r.alternatives[0].getId(),r.alternatives[0].getAmount());double xp=f.xp(Skills.INVENTION);
   require(Native950Invention.startManufacture(f.p,inventor,r,1),"Cannot manufacture "+r.product);f.run(12);require(f.amount(r.product)==r.quantity,"Missing product "+r.product);require(f.xp(Skills.INVENTION)>xp,"No manufacture XP");for(int n:Native950Invention.materials(f.p))require(n==0,"Materials not consumed");
  }
  f.clear();f.beside(inventor);f.give(1513,1);double xp=f.xp(Skills.INVENTION);require(Native950Invention.startDisassemble(f.p,inventor,1513,1),"Cannot disassemble magic logs");f.run(12);require(f.amount(1513)==0&&f.xp(Skills.INVENTION)>xp,"Disassembly failed");int sum=0;for(int n:Native950Invention.materials(f.p))sum+=n;require(sum==1,"Wrong component quantity");
  for(Native950Production.Recipe r:Native950Archaeology.recipes()){
   f.clear();f.beside(arch);int[] stored=new int[64];for(Item i:r.consumed()){int slot=Native950Archaeology.materialSlot(i.getId());if(slot>=0)stored[slot]+=i.getAmount();else f.give(i.getId(),i.getAmount());}Native950Archaeology.restoreMaterials(f.p,stored);xp=f.xp(Skills.ARCHAEOLOGY);require(Native950Archaeology.startRestore(f.p,arch,r,1),"Cannot restore "+r.label);f.run(12);require(f.amount(r.produced()[0].getId())==1&&f.xp(Skills.ARCHAEOLOGY)>xp,"Restoration failed");for(int n:Native950Archaeology.materials(f.p))require(n==0,"Restoration materials not consumed");
  }
  mixedRestoration(f,arch);
  mattockProgression(f,centurion);
  f.clear();f.beside(venator);f.give(49539,1);xp=f.xp(Skills.ARCHAEOLOGY);require(Native950Archaeology.start(f.p,venator),"Cannot excavate Venator");f.run(180);require(f.p.getInventory().getFreeSlots()==0,"Excavation must fill backpack");require(f.xp(Skills.ARCHAEOLOGY)>xp,"No excavation XP");f.beside(arch);require(Native950Archaeology.storeMaterials(f.p,arch),"Cannot store materials");f.beside(venator);require(Native950Archaeology.start(f.p,venator),"Cannot resume after storage");f.run(40);require(f.amount(49921)+f.amount(49923)>0,"Full backpack lost excavation progress and prevented all artefacts");
  f.clear();f.beside(centurion);f.give(49539,1);f.level(Skills.ARCHAEOLOGY,1);require(Native950Archaeology.start(f.p,centurion),"Level1 cannot excavate tutorial remains");f.run(50);require(f.amount(49741)>0,"No tutorial artefact");
  f.clear();f.beside(centurion);require(!Native950Archaeology.start(f.p,centurion),"Excavation starts without mattock");f.give(49539,1);require(Native950Archaeology.start(f.p,centurion),"Cancellation setup");f.p.getActionManager().forceStop();xp=f.xp(Skills.ARCHAEOLOGY);f.run(10);require(f.xp(Skills.ARCHAEOLOGY)==xp,"Cancelled excavation granted XP");
  f.clear();f.beside(inventor);f.level(Skills.CRAFTING,79);f.give(1513,1);require(!Native950Invention.startDisassemble(f.p,inventor,1513,1),"Underlevel Invention starts");f.level(Skills.CRAFTING,99);require(Native950Invention.startDisassemble(f.p,inventor,1513,1),"Remote setup");f.p.setLocation(new WorldTile(inventor.getX()+20,inventor.getY()+20,0));f.run(10);require(f.amount(1513)==1,"Remote disassembly consumed logs");
  System.out.println("PASS: eight manufacture recipes, three restorations, ordinary910 disassembly, component storage, full-backpack excavation resume, level1 tutorial, level/tool/cancellation/distance guards");
 }
 private static void mixedRestoration(Fixture f,WorldObject station){
  for(Native950Production.Recipe recipe:Native950Archaeology.recipes()){
   f.clear();f.beside(station);int[] stored=new int[64];
   for(Item item:recipe.consumed()){int slot=Native950Archaeology.materialSlot(item.getId());if(slot<0)f.give(item.getId(),item.getAmount());else{int carried=item.getAmount()/2;f.give(item.getId(),carried);stored[slot]=item.getAmount()-carried;}}
   Native950Archaeology.restoreMaterials(f.p,stored);double xp=f.xp(Skills.ARCHAEOLOGY);
   require(Native950Archaeology.startRestore(f.p,station,recipe,1),"Mixed material sources refused "+recipe.label);f.run(12);
   require(f.amount(recipe.produced()[0].getId())==1&&f.xp(Skills.ARCHAEOLOGY)>xp,"Mixed restore missing reward");
   for(Item item:recipe.consumed())require(f.amount(item.getId())==0,"Backpack material not consumed");for(int amount:Native950Archaeology.materials(f.p))require(amount==0,"Stored material not consumed");
  }
  Native950Production.Recipe recipe=Native950Archaeology.recipes().get(0);f.clear();f.beside(station);Native950Archaeology.restoreMaterials(f.p,null);
  for(Item input:recipe.consumed())f.give(input.getId(),input.getAmount());
  while(f.p.getInventory().getFreeSlots()>0)f.give(49539,1);
  require(Native950Archaeology.startRestore(f.p,station,recipe,1),"Full backpack carried-material restore refused");f.run(12);require(f.amount(recipe.produced()[0].getId())==1,"Full backpack restore lost output");
  f.clear();f.beside(station);int[] stored=new int[64];stored[0]=40;Native950Archaeology.restoreMaterials(f.p,stored);
  require(Native950Archaeology.withdrawMaterials(f.p,station,49444,10000),"Material withdrawal refused");require(f.amount(49444)==28&&Native950Archaeology.materials(f.p)[0]==12,"Withdraw-all must preserve overflow in storage");
  require(!Native950Archaeology.withdrawMaterials(f.p,station,49444,1)&&Native950Archaeology.materials(f.p)[0]==12,"Full bag withdrawal lost stored materials");
  require(!Native950Archaeology.withdrawMaterials(f.p,station,995,1),"Non-material storage withdrawal admitted");
  f.clear();f.beside(station);Native950Archaeology.restoreMaterials(f.p,null);for(Item item:recipe.consumed())f.give(item.getId(),item.getAmount());
  require(Native950Archaeology.startRestore(f.p,station,recipe,1),"Cancellation restore setup");com.rs.game.player.actions.Action cancelled=f.p.getActionManager().getAction();f.p.getActionManager().forceStop();double xp=f.xp(Skills.ARCHAEOLOGY);
  require(cancelled.processWithDelay(f.p)==-1&&f.amount(recipe.produced()[0].getId())==0&&f.xp(Skills.ARCHAEOLOGY)==xp,"Cancelled restore callback rewarded");
  f.clear();f.beside(station);for(Item item:recipe.consumed())if(Native950Archaeology.materialSlot(item.getId())<0)f.give(item.getId(),item.getAmount());
  require(!Native950Archaeology.startRestore(f.p,station,recipe,1),"Restoration without materials admitted");
  System.out.println("PASS:three cache restoration recipes combine backpack and storage, full-backpack material consumption frees room, material withdrawal preserves surplus, full-bag/unrecognized/cancelled actions do not lose materials or award XP");
 }
 private static void mattockProgression(Fixture f,WorldObject remains){
  f.clear();f.beside(remains);f.level(Skills.ARCHAEOLOGY,49);f.give(49539,1);f.give(49554,1);
  require(Native950Archaeology.mattock(f.p)==49539,"Cache mattock level gate ignored");
  f.level(Skills.ARCHAEOLOGY,50);require(Native950Archaeology.mattock(f.p)==49554,"Strongest carried mattock not selected");
  f.p.getEquipment().getItems().set(3,new Item(49539,1));
  require(Native950Archaeology.mattock(f.p)==49539,"Equipped usable mattock must have priority");
  f.p.getEquipment().getItems().set(3,null);
  Native950Archaeology.restoreExcavationProgress(f.p,new int[]{0,0});
  require(Native950Archaeology.start(f.p,remains),"Rune mattock excavation refused");f.run(2);
  require(Native950Archaeology.excavationProgress(f.p)[0]==6,"Rune precision did not produce six bronze units");
  f.run(4);require(Native950Archaeology.excavationProgress(f.p)[0]==10,"Excavation must cap at discovery threshold");
  f.run(4);require(f.amount(49741)==1&&Native950Archaeology.excavationProgress(f.p)[0]==0,"Discovery failed to grant/reset exactly once");
  f.p.getActionManager().forceStop();
  boolean removed=false;for(com.rs.game.hitbar.HitBar b:f.p.getNextHitBars())if(b.getType()==49&&b instanceof com.rs.game.hitbar.impl.MiningHitBar&&((com.rs.game.hitbar.impl.MiningHitBar)b).isRemoval())removed=true;
  require(removed,"Excavation stop left blue progress gauge active");
  f.clear();f.beside(remains);f.give(49539,1);Native950Archaeology.restoreExcavationProgress(f.p,new int[]{0,0});
  require(Native950Archaeology.start(f.p,remains),"Bronze excavation refused");f.run(2);
  require(Native950Archaeology.excavationProgress(f.p)[0]==1,"Bronze progression/save units changed");
  double xp=f.xp(Skills.ARCHAEOLOGY);f.p.setNextWorldTile(f.p.transform(1,0,0));f.run(8);
  require(!f.p.getActionManager().hasSkillWorking()&&f.xp(Skills.ARCHAEOLOGY)==xp&&Native950Archaeology.excavationProgress(f.p)[0]==1,"Moving during excavation awarded material/progress");
  f.level(Skills.ARCHAEOLOGY,99);
  System.out.println("PASS: actual950 mattock requirements/strongest backpack/equipped priority, bronze1 vs rune6 progress, capped discovery/reset, blue gauge removal and movement cancellation");
 }
 private static final class Fixture implements AutoCloseable{
  final int[] in={9,5,0,3},out={59,55,50,53};final Native950Isaac cipher=new Native950Isaac(out);final Native950GameTransport transport=new Native950GameTransport(new Native950Isaac(in),new Native950Isaac(out),Thread.currentThread());
  final EmbeddedChannel channel=new EmbeddedChannel(transport);final Player p;final List<WorldObject> objects=new ArrayList<>();
  Fixture(){p=Player.createNative950("craft-summon",new WorldTile(3217,3258,0),channel);p.setActive(true);Native950World.installVarpSink(p);World.addNative950Player(p,1);World.updateEntityRegion(p);p.loadMapRegions();p.setClientHasLoadedMapRegion();
   Map<Integer,Native950ItemCatalog.Entry> entries=new HashMap<>();for(Native950ItemCatalog.Entry e:Native950Invention.itemEntries())entries.put(e.id,e);for(Native950ItemCatalog.Entry e:Native950Archaeology.itemEntries())entries.put(e.id,e);entries.put(1513,Native950InventionAssets.itemEntry(1513));
   Native950Skilling.attach(p,new Native950Containers(p,new Native950ItemCatalog(new ArrayList<>(entries.values()))));clear();p.resetMasks();drain();
  }
  WorldObject station(int id){
   for(int y=3255;y<3290;y++)for(int x=3205;x<3250;x++){
    WorldObject o=new WorldObject(id,10,0,x,y,0);boolean clear=true;
    for(int dx=-1;dx<=o.getDefinitions().sizeX;dx++)for(int dy=-1;dy<=o.getDefinitions().sizeY;dy++)if(!World.isFloorFree(0,x+dx,y+dy,1))clear=false;
    if(!clear)continue;World.spawnObject(o);objects.add(o);beside(o);return o;
   }throw new AssertionError("Cannot place station "+id);
  }
  void beside(WorldObject o){p.resetWalkSteps();for(int dx=-1;dx<=o.getDefinitions().sizeX;dx++)for(int dy=-1;dy<=o.getDefinitions().sizeY;dy++){if(!World.isFloorFree(0,o.getX()+dx,o.getY()+dy,1))continue;p.setLocation(new WorldTile(o.getX()+dx,o.getY()+dy,0));World.updateEntityRegion(p);p.resetMasks();if(Native950Mining.current(o)&&Native950Mining.inReach(p,o))return;}throw new AssertionError("Station adjacency "+o.getId()+" current="+Native950Mining.current(o));}
  void level(int skill,int n){p.getSkills().setXpWithoutRefresh(skill,Skills.getXPForLevel(skill,n));p.getSkills().setLevelWithoutRefresh(skill,n);}
  double xp(int skill){return p.getSkills().getXp(skill);}
  void clear(){p.getActionManager().forceStop();p.resetWalkSteps();p.resetMasks();for(int i=0;i<28;i++)p.getInventory().items.set(i,null);}
  int amount(int id){return p.getInventory().items.getNumberOf(id);}
  void give(int id,int n){require(Native950Skilling.giveItem(p,id,n),"Cannot provide "+id+"x"+n);}
  void materials(Native950Production.Recipe r,int n){for(Item i:r.consumed())give(i.getId(),i.getAmount()*n);for(int tool:r.tools())if(amount(tool)==0)give(tool,1);}
  void run(int n){for(int i=0;i<n;i++){ticks++;p.processEntity();p.processEntityUpdate();channel.write(Native950Packets.tickEnd());drain();p.resetMasks();}}
  void verify(Native950Production.Recipe r,double xp){require(amount(r.produced()[0].getId())==r.produced()[0].getAmount(),"Output quantity "+r.label);for(Item i:r.consumed())require(amount(i.getId())==0,"Unconsumed material "+r.label+":"+i.getId());for(int tool:r.tools())require(amount(tool)==1,"Lost tool "+r.label);require(r.xp==0?this.xp(r.skill)==xp:this.xp(r.skill)>xp,"XP "+r.label);require(!p.getActionManager().hasSkillWorking(),"Finished recipe kept running");}
  void drain(){channel.flush();channel.runPendingTasks();Object message;while((message=channel.readOutbound())!=null)try{require(message instanceof ByteBuf,"Non-byte transport");ByteBuf b=(ByteBuf)message;while(b.isReadable()){int op=(b.readUnsignedByte()-cipher.getAsInt())&255;if(op>=128)op=((op-128)<<8)|((b.readUnsignedByte()-cipher.getAsInt())&255);ServerPacket kind=null;for(ServerPacket k:ServerPacket.values())if(k.opcode()==op){kind=k;break;}require(kind!=null,"Unknown950 output "+op);int len=kind.size();if(len==-1)len=b.readUnsignedByte();else if(len==-2)len=b.readUnsignedShort();require(len>=0&&len<=b.readableBytes(),"Invalid950 output length");b.skipBytes(len);frames++;if(kind==ServerPacket.UPDATE_STAT)stats++;}}finally{ReferenceCountUtil.release(message);}channel.checkException();require(transport.terminalFailure()==null,"Transport failed");}
  public void close(){p.getActionManager().forceStop();for(WorldObject o:objects)World.removeObject(o);Native950Skilling.detach(p);World.removeNative950Player(p);channel.finishAndReleaseAll();}
 }
 private static void require(boolean ok,String message){if(!ok)throw new AssertionError(message);}
}
