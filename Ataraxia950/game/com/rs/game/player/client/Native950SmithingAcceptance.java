package com.rs.game.player.client;
import com.rs.cache.Cache;import com.rs.cores.CoresManager;import com.rs.game.World;import com.rs.game.WorldTile;import com.rs.game.WorldObject;
import com.rs.game.item.Item;import com.rs.game.player.Player;import com.rs.game.player.Skills;import com.rs.network.modern.Native950GameTransport;
import com.rs.network.modern.Native950Isaac;import com.rs.network.protocol.modern950.Native950Packets;import com.rs.network.protocol.modern950.Native950Protocol.ServerPacket;
import io.netty.buffer.ByteBuf;import io.netty.channel.embedded.EmbeddedChannel;import io.netty.util.ReferenceCountUtil;import java.nio.file.Paths;import java.util.*;import java.util.concurrent.TimeUnit;
/** Paired950 anvil metadata, every admitted atomic recipe, and original engine action/collision proof. */
public final class Native950SmithingAcceptance {
 private static int ticks,frames,stats,gauges;
 public static void main(String[] a)throws Exception{
  require(a.length==1,"Usage: Native950SmithingAcceptance <950-cache>");require(NativeCacheVerification.isEnforced(),"Verification disabled");
  System.setProperty(Native950World.SPAWNS_PROPERTY,"false");System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");Cache.initFlatReadOnly(Paths.get(a[0]));
  Native950World.getInstance().execute(()->{Native950Smithing.verifyCacheBindings();try(Fixture f=new Fixture()){check(f);}return null;}).get(120,TimeUnit.SECONDS);
  require(CoresManager.getNative950Scheduler().failed()==0,"Scheduler failed");
  System.out.println("PASS:702 paired950 anvil recipes, all atomic material/output/XP exchanges, ten metal-family action loops plus upgrades; "+ticks+" engine ticks, "+frames+" encrypted native frames, "+stats+" stat updates, "+gauges+" gauge updates/removals. No save/listener/authentication.");
  System.out.println("LIMIT: native menu/animation/gauge pixels remain a manual check; metal-bank storage and persistent unfinished projects are outside this baseline.");
 }
 private static Native950Production.Recipe find(int output){for(Native950Production.Recipe r:Native950Smithing.recipes())if(r.produced()[0].getId()==output)return r;throw new AssertionError("Missing product "+output);}
 private static int input(Native950Production.Recipe r,int id){for(Item i:r.consumed())if(i.getId()==id)return i.getAmount();return 0;}
 private static void check(Fixture f){
  List<Native950Production.Recipe> rows=Native950Smithing.recipes();require(rows.size()==702,"Recipe count");
  require(input(find(1205),2349)==2&&find(1205).xp==30,"Old bronze dagger recipe leaked through");
  require(find(39).produced()[0].getAmount()==75&&find(39).xp==15,"Wrong current arrowhead batch");
  require(find(45543).level==50&&input(find(45543),2363)==5,"Wrong current rune platebody identity");
  require(input(find(45554),45549)==1&&input(find(45554),44844)==2,"Upgrade omitted previous product");
  WorldObject anvil=f.station(11497);require(Native950Smithing.accepts(anvil,1)&&!Native950Smithing.accepts(anvil,2),"Anvil cache operation mismatch");
  require(!Native950Smithing.isStation(new WorldObject(3531,10,0,anvil)),"Decorative anvil accepted");
  for(Native950Production.Recipe r:rows){f.clear();f.level(Skills.SMITHING,99);f.materials(r,1);double xp=f.xp(Skills.SMITHING);require(r.complete(f.p),"Atomic recipe failed "+r.label);f.verify(r,xp);f.drain();}
  System.out.println("PASS: every admitted current-cache recipe exchanges its exact bars/base products for exact output, preserves hammer and awards Smithing XP");
  Set<Integer> levels=new HashSet<>();List<Native950Production.Recipe> actions=new ArrayList<>();
  for(Native950Production.Recipe r:rows)if(levels.add(r.level))actions.add(r);actions.add(find(45554));actions.add(find(45543));
  for(Native950Production.Recipe r:actions){f.clear();f.beside(anvil);f.level(Skills.SMITHING,99);f.materials(r,1);double xp=f.xp(Skills.SMITHING);
   require(!Native950Smithing.choices(f.p,anvil).isEmpty(),"No native selection choices");require(Native950Smithing.startStation(f.p,anvil,r,1),"Anvil refused "+r.label);
   int bound=3000;while(f.p.getActionManager().hasSkillWorking()&&bound-->0)f.run(1);require(bound>0,"Anvil did not finish "+r.label);f.verify(r,xp);
  }
  Native950Production.Recipe bronze=find(1205);f.clear();f.beside(anvil);f.materials(bronze,1);double xp=f.xp(Skills.SMITHING);
  require(Native950Smithing.startStation(f.p,anvil,bronze,1),"Cancellation setup");f.p.getActionManager().forceStop();f.run(4);require(f.amount(2349)==2&&f.amount(1205)==0&&f.xp(Skills.SMITHING)==xp,"Cancelled forging gave rewards");
  require(Native950Smithing.startStation(f.p,anvil,bronze,1),"Removed anvil setup");World.removeObject(anvil);f.run(4);require(f.amount(2349)==2&&f.amount(1205)==0,"Removed anvil still produced");World.spawnObject(anvil);
  f.p.setLocation(new WorldTile(anvil.getX()+8,anvil.getY()+8,0));require(!Native950Smithing.startStation(f.p,anvil,bronze,1),"Remote anvil started");f.beside(anvil);
  require(Native950Smithing.startStation(f.p,anvil,bronze,1),"Movement setup");f.p.setLocation(new WorldTile(anvil.getX()+8,anvil.getY()+8,0));f.run(4);require(f.amount(2349)==2&&f.amount(1205)==0,"Walked-away forge gave rewards");
  f.beside(anvil);f.clear();f.level(Skills.SMITHING,1);f.materials(find(45549),1);require(!Native950Smithing.startStation(f.p,anvil,find(45549),1),"Level1 forged elder rune");
  f.clear();f.materials(bronze,1);f.p.getInventory().items.set(f.p.getInventory().items.lookupSlot(2347),null);require(!Native950Smithing.startStation(f.p,anvil,bronze,1),"Missing hammer accepted");
  require(gauges>0&&stats>0,"Native gauges or XP updates absent");System.out.println("PASS: real anvil collision/options, original action cadence and heat/work, movement/removal/cancellation/level/tool guards");
 }
 private static final class Fixture implements AutoCloseable{
  final int[] in={9,5,0,3},out={59,55,50,53};final Native950Isaac cipher=new Native950Isaac(out);final Native950GameTransport transport=new Native950GameTransport(new Native950Isaac(in),new Native950Isaac(out),Thread.currentThread());
  final EmbeddedChannel channel=new EmbeddedChannel(transport);final Player p;final List<WorldObject> objects=new ArrayList<>();
  Fixture(){p=Player.createNative950("smith-proof",new WorldTile(3217,3258,0),channel);p.setActive(true);Native950World.installVarpSink(p);World.addNative950Player(p,1);World.updateEntityRegion(p);p.loadMapRegions();p.setClientHasLoadedMapRegion();
   Map<Integer,Native950ItemCatalog.Entry> entries=new HashMap<>();for(Native950Production.Recipe r:Native950Smithing.recipes()){for(Item i:r.consumed())entries.put(i.getId(),Native950Smithing.itemEntry(i.getId()));for(Item i:r.produced())entries.put(i.getId(),Native950Smithing.itemEntry(i.getId()));for(int id:r.tools())entries.put(id,Native950Smithing.itemEntry(id));}
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
  void run(int n){for(int i=0;i<n;i++){ticks++;p.processEntity();p.processEntityUpdate();if(!p.getNextHitBars().isEmpty()){Native950Hitbars.Result bars=Native950Hitbars.fromRunningCache(p);require(bars.refusals()==0,"Smithing gauge refused");gauges+=bars.playerBars().size();}channel.write(Native950Packets.tickEnd());drain();p.resetMasks();}}
  void verify(Native950Production.Recipe r,double xp){require(amount(r.produced()[0].getId())==r.produced()[0].getAmount(),"Output quantity "+r.label);for(Item i:r.consumed())require(amount(i.getId())==0,"Unconsumed material "+r.label+":"+i.getId());for(int tool:r.tools())require(amount(tool)==1,"Lost tool "+r.label);require(r.xp==0?this.xp(r.skill)==xp:this.xp(r.skill)>xp,"XP "+r.label);require(!p.getActionManager().hasSkillWorking(),"Finished recipe kept running");}
  void drain(){channel.flush();channel.runPendingTasks();Object message;while((message=channel.readOutbound())!=null)try{require(message instanceof ByteBuf,"Non-byte transport");ByteBuf b=(ByteBuf)message;while(b.isReadable()){int op=(b.readUnsignedByte()-cipher.getAsInt())&255;if(op>=128)op=((op-128)<<8)|((b.readUnsignedByte()-cipher.getAsInt())&255);ServerPacket kind=null;for(ServerPacket k:ServerPacket.values())if(k.opcode()==op){kind=k;break;}require(kind!=null,"Unknown950 output "+op);int len=kind.size();if(len==-1)len=b.readUnsignedByte();else if(len==-2)len=b.readUnsignedShort();require(len>=0&&len<=b.readableBytes(),"Invalid950 output length");b.skipBytes(len);frames++;if(kind==ServerPacket.UPDATE_STAT)stats++;}}finally{ReferenceCountUtil.release(message);}channel.checkException();require(transport.terminalFailure()==null,"Transport failed");}
  public void close(){p.getActionManager().forceStop();for(WorldObject o:objects)World.removeObject(o);Native950Skilling.detach(p);World.removeNative950Player(p);drain();channel.finishAndReleaseAll();}
 }
 private static void require(boolean ok,String message){if(!ok)throw new AssertionError(message);}
}
