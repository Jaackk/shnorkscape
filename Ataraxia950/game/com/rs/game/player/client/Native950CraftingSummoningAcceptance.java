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
public final class Native950CraftingSummoningAcceptance {
 private static int ticks,frames,stats;
 public static void main(String[] a)throws Exception{
  require(a.length==1,"Usage: Native950CraftingSummoningAcceptance <950-cache>");require(NativeCacheVerification.isEnforced(),"Verification disabled");
  System.setProperty(Native950World.SPAWNS_PROPERTY,"false");System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");Cache.initFlatReadOnly(Paths.get(a[0]));
  Native950World.getInstance().execute(()->{Native950Crafting.verifyCacheBindings();Native950Summoning.verifyCacheBindings();try(Fixture f=new Fixture()){check(f);}return null;}).get(120,TimeUnit.SECONDS);
  require(CoresManager.getNative950Scheduler().failed()==0,"Scheduler failed");
  System.out.println("PASS: "+Native950Crafting.recipes().size()+" Crafting and "+Native950Summoning.recipes().size()+" Summoning recipes; "+ticks+" original engine ticks, "+frames+" decoded native frames, "+stats+" stat updates. No account save, listener, or authentication.");
  System.out.println("LIMIT: live client menu and animation pixels require manual verification; familiar summon/control is not part of this port.");
 }
 private static Native950Production.Recipe find(List<Native950Production.Recipe> rows,int output){for(Native950Production.Recipe r:rows)if(r.produced()[0].getId()==output)return r;throw new AssertionError("Missing product "+output);}
 private static int input(Native950Production.Recipe r,int id){for(Item i:r.consumed())if(i.getId()==id)return i.getAmount();return 0;}
 private static void check(Fixture f){
  List<Native950Production.Recipe> crafting=Native950Crafting.recipes(),summoning=Native950Summoning.recipes();
  require(crafting.size()==117,"Crafting count changed: "+crafting.size());require(summoning.size()==161,"Summoning count changed: "+summoning.size());
  Native950Production.Recipe imphide=find(crafting,25845),wolf=find(summoning,12047),spider=find(summoning,12428);
  require(imphide.level==12&&input(imphide,25545)==2&&imphide.xp==20,"Stale910 imphide record");
  require(input(find(summoning,12025),12155)==1&&input(find(summoning,12093),12155)==1,"Legacy missing empty-pouch bug returned");
  require(find(summoning,12063)!=null&&find(summoning,12780)!=null&&find(summoning,12810)!=null,"Wrong or noted legacy pouch ids");
  int variants=0;for(Native950Production.Recipe r:summoning)if(r.produced()[0].getId()==12824)variants++;require(variants==3,"Titan scroll alternate pouch conversion records missing");
  require(spider.level==10&&spider.xp==0.2&&spider.produced()[0].getAmount()==10,"Stale spider scroll level/XP");
  require(input(find(crafting,10980),1775)==1,"Light orb bypassed empty-glass stage");
  System.out.println("PASS: current950 prerequisites/ingredients override stale910 enum rows; true unstrung symbols and empty light orbs");
  WorldObject furnace=f.station(11010),wheel=f.station(2644),pottery=f.station(2642),oven=f.station(2643),obelisk=f.station(67036);
  require(Native950Crafting.accepts(wheel,2)&&!Native950Crafting.accepts(wheel,1),"Spin option not matched to cache");
  require(Native950Crafting.accepts(pottery,1)&&Native950Crafting.accepts(oven,1)&&Native950Crafting.accepts(furnace,1),"Station option bindings");
  require(Native950Summoning.accepts(obelisk,1)&&Native950Summoning.isRenewOption(obelisk,2),"Obelisk Renew-points is not routed separately from infusion");
  f.level(Skills.CRAFTING,99);f.level(Skills.SUMMONING,99);
  for(Native950Production.Recipe r:crafting){
   f.clear();f.materials(r,1);double xp=f.xp(r.skill);boolean started;
   if(r.label.startsWith("Sew:")||r.label.startsWith("Glass:")||r.label.startsWith("String:")||r.label.startsWith("Battlestaff:"))started=Native950Crafting.startInventory(f.p,r,1);
   else{WorldObject o=r.label.startsWith("Spin:")?wheel:r.label.startsWith("Pottery:")?pottery:r.label.startsWith("Fire pottery:")?oven:furnace;f.beside(o);started=Native950Crafting.startStation(f.p,o,r,1);}
   require(started,"Could not start "+r.label);f.run(12);f.verify(r,xp);
  }
  for(Native950Production.Recipe r:summoning){f.clear();f.beside(obelisk);f.materials(r,1);double xp=f.xp(r.skill);require(Native950Summoning.start(f.p,obelisk,r,1),"Could not infuse "+r.label);f.run(10);f.verify(r,xp);}
  System.out.println("PASS: every admitted recipe runs its actual material exchange once, retains tools, awards original Skills XP, and stops at the selected amount");
  f.clear();f.beside(obelisk);f.materials(wolf,2);double xp=f.xp(Skills.SUMMONING);
  require(Native950Summoning.start(f.p,obelisk,wolf,2),"Cancellation setup");f.p.getActionManager().forceStop();f.run(10);require(f.amount(12047)==0&&f.xp(Skills.SUMMONING)==xp,"Cancelled infusion awarded rewards");
  require(Native950Summoning.start(f.p,obelisk,wolf,2),"Removal setup");World.removeObject(obelisk);f.run(10);require(f.amount(12047)==0&&f.xp(Skills.SUMMONING)==xp,"Removed obelisk awarded rewards");World.spawnObject(obelisk);
  f.p.setLocation(new WorldTile(obelisk.getX()+8,obelisk.getY()+8,0));require(!Native950Summoning.start(f.p,obelisk,wolf,1),"Remote infusion started");f.beside(obelisk);
  f.p.getInventory().items.set(f.p.getInventory().items.lookupSlot(12183),null);require(!Native950Summoning.start(f.p,obelisk,wolf,1),"Missing shards accepted");
  f.clear();f.materials(find(summoning,12093),1);f.level(Skills.SUMMONING,1);require(!Native950Summoning.start(f.p,obelisk,find(summoning,12093),1),"Level1 infused yak");
  f.level(Skills.CRAFTING,1);f.clear();f.materials(imphide,1);require(!Native950Crafting.startInventory(f.p,imphide,1),"Level1 crafted imphide hood");
  f.level(Skills.CRAFTING,99);f.clear();f.beside(furnace);Native950Production.Recipe gold=find(crafting,1635);f.materials(gold,1);xp=f.xp(Skills.CRAFTING);
  require(Native950Crafting.startStation(f.p,furnace,gold,1),"Furnace distance setup");f.p.setLocation(new WorldTile(furnace.getX()+8,furnace.getY()+8,0));f.run(10);require(f.amount(1635)==0&&f.xp(Skills.CRAFTING)==xp,"Remote furnace continued production");
  System.out.println("PASS: no rewards after cancellation, removed station, movement away, missing shards, or insufficient skill; real object-option contracts");
 }
 private static final class Fixture implements AutoCloseable{
  final int[] in={9,5,0,3},out={59,55,50,53};final Native950Isaac cipher=new Native950Isaac(out);final Native950GameTransport transport=new Native950GameTransport(new Native950Isaac(in),new Native950Isaac(out),Thread.currentThread());
  final EmbeddedChannel channel=new EmbeddedChannel(transport);final Player p;final List<WorldObject> objects=new ArrayList<>();
  Fixture(){p=Player.createNative950("craft-summon",new WorldTile(3217,3258,0),channel);p.setActive(true);Native950World.installVarpSink(p);World.addNative950Player(p,1);World.updateEntityRegion(p);p.loadMapRegions();p.setClientHasLoadedMapRegion();
   Map<Integer,Native950ItemCatalog.Entry> entries=new HashMap<>();for(List<Native950Production.Recipe> list:Arrays.asList(Native950Crafting.recipes(),Native950Summoning.recipes()))for(Native950Production.Recipe r:list){for(Item i:r.consumed())entries.put(i.getId(),Native950CraftingAssets.entry(i.getId(),new String[5]));for(Item i:r.produced())entries.put(i.getId(),Native950CraftingAssets.entry(i.getId(),new String[5]));for(int id:r.tools())entries.put(id,Native950CraftingAssets.entry(id,new String[5]));}
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
