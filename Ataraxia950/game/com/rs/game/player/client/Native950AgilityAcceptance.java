package com.rs.game.player.client;
import com.rs.cache.Cache;
import com.rs.cores.CoresManager;
import com.rs.game.*;
import com.rs.game.player.*;
import com.rs.game.player.content.agility.GnomeAgility;
import com.rs.game.route.RouteFinder;
import com.rs.game.route.strategy.FixedTileStrategy;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.buffer.*;
import io.netty.util.ReferenceCountUtil;
import com.rs.network.modern.*;
import com.rs.network.protocol.modern950.*;
import com.rs.network.protocol.modern950.Native950Protocol.ServerPacket;
import java.util.*;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/** Actual scenery and collision routes through all seven course stages, without modifying a save. */
public final class Native950AgilityAcceptance {
 public static void main(String[] args)throws Exception {
  System.setProperty(Native950World.SPAWNS_PROPERTY,"false");System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");
  Cache.initFlatReadOnly(Paths.get(args[0]));Native950Agility.verifyCacheBindings();
  Native950World.getInstance().execute(()->{try(Fixture f=new Fixture()){f.check();}return null;}).get(120,TimeUnit.SECONDS);
  require(CoresManager.getNative950Scheduler().failed()==0,"Scheduler failed");
  System.out.println("PASS: seven actual950 Gnome obstacles, collision approach, balance/crawl BAS, stair planes, ordered Gnome/Barbarian/Wilderness lap XP, all19 expanded directions, cancellation and unlock/run restoration");
 }
 static final class Fixture implements AutoCloseable {
  final int[] incoming={9,5,0,8},outgoing={59,55,50,58};
  final Native950Isaac clientCipher=new Native950Isaac(incoming),cipher=new Native950Isaac(outgoing);
  final Native950GameTransport transport=new Native950GameTransport(new Native950Isaac(incoming),new Native950Isaac(outgoing),Thread.currentThread());
  final EmbeddedChannel channel=new EmbeddedChannel(transport);
  final Native950EntityFrames entities=new Native950EntityFrames();
  Native950Interactions input;int frames,clicks;
  final Player p=Player.createNative950("agility",new WorldTile(2474,3437,0),channel);
  Fixture(){p.setActive(true);p.setRunning(true);p.setRunHidden(true);Native950World.installVarpSink(p);World.addNative950Player(p,1);World.updateEntityRegion(p);p.loadMapRegions();p.setClientHasLoadedMapRegion();
   Native950Content.BankUi bank=new Native950Content.BankUi(517,201,15,317,39,new int[11],new int[11],Collections.emptyList(),Collections.emptyList());
   input=new Native950Interactions(p,channel,new Native950Content(new Native950ItemCatalog(Collections.emptyList()).withLegacyDrops(),bank),null,null);
   input.bootstrap();p.getAppearence().generateAppearenceData();
   for(Native950Packets.Packet packet:entities.admit(p,new Native950World.SceneConfig(p.getX(),p.getY(),0,1,7,0,0,0),Collections.singletonList(p)))channel.write(packet);
   drain();p.resetMasks();}
  WorldObject find(int id,int x,int y,int plane){
   for(WorldObject o:World.getRegion(new WorldTile(x,y,plane).getRegionId(),true).getObjects().values())
    if(o.getId()==id&&o.getX()==x&&o.getY()==y&&o.getPlane()==plane)return o;
   throw new AssertionError("Missing actual scenery "+id+" at "+x+","+y+","+plane);
  }
  void walk(WorldTile entry){
   require(p.getPlane()==entry.getPlane(),"Entrance plane wrong");
   int n=RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER,p.getX(),p.getY(),p.getPlane(),p.getSize(),new FixedTileStrategy(entry.getX(),entry.getY()),false);
   require(n>=0&&!RouteFinder.lastIsAlternative(),"No collision approach from "+p+" to "+entry);
   int[] xs=RouteFinder.getLastPathBufferX(),ys=RouteFinder.getLastPathBufferY();
   for(int i=n-1;i>=0;i--)p.addWalkSteps(xs[i],ys[i],128-p.getWalkSteps().size(),true);
   for(int i=0;i<60&&(p.hasWalkSteps()||!entry.matches(p)||p.getNextWalkDirection()!=-1);i++)tick();
   tick();require(entry.matches(p),"Failed approach "+entry+" current "+p);
  }
  void check(){
   int[][] rows={{69526,2474,3435,0},{69383,2473,3425,0},{69508,2473,3421,1},{2312,2478,3420,2},{69507,2487,3420,2},{69384,2485,3426,0},{69378,2483,3431,0}};
   double before=p.getSkills().getXp(Skills.AGILITY);int laps=p.getLapsRan();
   for(int step=0;step<rows.length;step++){
    int[] r=rows[step];WorldObject o=find(r[0],r[1],r[2],r[3]);WorldTile entry=Native950Agility.approach(p,o);
    require(Native950Agility.handles(o,1),"Course option missing "+step);
    if(!entry.matches(p))require(!Native950Agility.start(p,o,entry),"Started Agility at a distance");
    WorldTile end=Native950Agility.destination(step,entry);double xp=p.getSkills().getXp(Skills.AGILITY);
    click(o);
    for(int i=0;i<80&&!p.getActionManager().hasSkillWorking();i++)tick();
    require(p.getActionManager().hasSkillWorking()&&entry.matches(p),"Routed stage did not start at entrance "+step+" "+p);
    if(step==0||step==3||step==6){require(p.getAppearence().getRenderEmote()==(step==6?295:155),"Wrong render override");require(p.getAppearence().getNative950WithheldBodies()==0,"Appearance withheld");}
    for(int i=0;i<40&&p.getActionManager().hasSkillWorking();i++)tick();
    require(end.matches(p),"Wrong end at stage "+step+": "+p+" expected "+end);
    require(!p.isLocked()&&p.getRun(),"Lock/run state not restored at stage "+step);
    require(p.getSkills().getXp(Skills.AGILITY)>xp,"No XP stage "+step);
    require(GnomeAgility.getGnomeStage(p)==(step==6?-1:step),"Wrong ordered stage "+step);
   }
   require(p.getLapsRan()==laps+1&&p.getSkills().getXp(Skills.AGILITY)>before,"No completed lap");
   WorldObject log=find(69526,2474,3435,0);WorldTile entry=Native950Agility.approach(p,log);walk(entry);
   double xp=p.getSkills().getXp(Skills.AGILITY);require(Native950Agility.start(p,log,entry),"Restart refused");
   tick();tick();require(!entry.matches(p),"Gnome cancellation fixture must enter the log footprint");p.getActionManager().forceStop();tick();tick();require(entry.matches(p)&&p.getSkills().getXp(Skills.AGILITY)==xp&&!p.isLocked()&&p.getRun()&&!p.hasWalkSteps(),"Cancel stranded player, awarded XP or retained lock");
   require(Native950Agility.start(p,log,entry),"Gnome external-teleport setup");tick();WorldTile externalGnome=new WorldTile(3218,3257,0);p.setNextWorldTile(externalGnome);p.getActionManager().forceStop();tick();tick();require(externalGnome.matches(p),"Gnome cancellation replaced unrelated teleport");
   GnomeAgility.setGnomeStage(p,5);require(Native950Agility.complete(p,6,new WorldTile(2483,3437,0))==12,"Reverse pipe earned lap bonus");
   require(p.getLapsRan()==laps+1,"Reverse pipe incremented lap");
   expansion();
  }
  void relocate(WorldTile tile){input.walking();p.getActionManager().forceStop();p.resetWalkSteps();p.setNextWorldTile(tile);tick();tick();p.loadMapRegions();p.setClientHasLoadedMapRegion();}
  void expansion(){
   p.getSkills().setXp(Skills.AGILITY,Skills.getXPForLevel(Skills.AGILITY,60));p.getSkills().set(Skills.AGILITY,60);
   relocate(new WorldTile(2552,3561,0));
   int laps=p.getLapsRan();
   for(int index:new int[]{0,2,4,5,6,7,9,10})runObstacle(Native950AgilityCourses.OBSTACLES.get(index));
   require(p.getLapsRan()==laps+1,"Barbarian lap missing");
   require(Native950AgilityCourses.stage(p,Native950AgilityCourses.BARBARIAN)==-1,"Barbarian lap state survived finish");
   relocate(new WorldTile(2998,3915,0));
   for(int index:new int[]{11,13,15,16,17,18})runObstacle(Native950AgilityCourses.OBSTACLES.get(index));
   require(p.getLapsRan()==laps+2,"Wilderness lap missing");
   require(Native950AgilityCourses.stage(p,Native950AgilityCourses.WILDERNESS)==-1,"Wilderness lap state survived finish");
   runObstacle(Native950AgilityCourses.OBSTACLES.get(12));
   for(int index:new int[]{1,3,8,14}){Native950AgilityCourses.Obstacle extra=Native950AgilityCourses.OBSTACLES.get(index);relocate(extra.entry);runObstacle(extra);}
   Native950AgilityCourses.Obstacle pipe=Native950AgilityCourses.OBSTACLES.get(0);relocate(pipe.entry);
   WorldObject object=find(pipe.id,pipe.x,pipe.y,pipe.plane);
   require(!Native950Agility.handles(new WorldObject(object.getId(),object.getType(),object.getRotation(),object.getX()+1,object.getY(),object.getPlane()),1),"Unverified scenery position entered course");
   p.getSkills().set(Skills.AGILITY,29);double xp=p.getSkills().getXp(Skills.AGILITY);
   require(!Native950Agility.start(p,object,pipe.entry),"Level29 entered Barbarian course");
   require(!p.isLocked()&&p.getSkills().getXp(Skills.AGILITY)==xp,"Level refusal changed lock or XP");
   p.getSkills().set(Skills.AGILITY,60);require(Native950Agility.start(p,object,pipe.entry),"Pipe restart failed");
   tick();p.getActionManager().forceStop();tick();tick();
   require(!p.isLocked()&&p.getRun()&&p.getSkills().getXp(Skills.AGILITY)==xp&&pipe.entry.matches(p),"Expanded course cancel did not restore clear entrance/run/XP state");
   Native950AgilityCourses.Obstacle gate=Native950AgilityCourses.OBSTACLES.get(11);relocate(gate.entry);
   p.getSkills().set(Skills.AGILITY,51);require(!Native950Agility.start(p,find(gate.id,gate.x,gate.y,gate.plane),gate.entry),"Level51 entered Wilderness course");
   p.getSkills().set(Skills.AGILITY,60);
   Native950AgilityCourses.Obstacle balance=Native950AgilityCourses.OBSTACLES.get(4);relocate(balance.entry);
   require(Native950Agility.start(p,find(balance.id,balance.x,balance.y,balance.plane),balance.entry),"Balance restart failed");tick();
   WorldTile external=new WorldTile(3218,3257,0);p.setNextWorldTile(external);p.getActionManager().forceStop();tick();tick();
   require(external.matches(p)&&!p.isLocked()&&p.getRun()&&p.getSkills().getXp(Skills.AGILITY)==xp,"Agility cancellation replaced unrelated teleport or awarded XP");
   System.out.println("PASS: basic Barbarian and Wilderness complete collision-routed laps, entrance/exit gates, platform descent, current950 pipe endpoints and level/cancel gates");
  }
  void runObstacle(Native950AgilityCourses.Obstacle row){
   WorldObject object=find(row.id,row.x,row.y,row.plane);
   require(Native950Agility.handles(object,1),"Missing new course binding "+row.id);
   WorldTile entry=Native950Agility.approach(p,object);
   require(entry!=null&&entry.matches(row.entry),"Wrong course entrance for "+row.id+": "+entry);
   require(World.isFloorFree(row.end.getPlane(),row.end.getX(),row.end.getY(),1),"Blocked course endpoint "+row.id);
   if(!entry.matches(p))require(!Native950Agility.start(p,object,entry),"Course began before arrival "+row.id);
   double xp=p.getSkills().getXp(Skills.AGILITY);click(object);
   for(int t=0;t<100&&!p.getActionManager().hasSkillWorking();t++)tick();
   require(p.getActionManager().hasSkillWorking()&&entry.matches(p),"Course approach failed "+row.id+" at "+p+" expected "+entry);
   if(row.render!=-1)require(p.getAppearence().getRenderEmote()==row.render,"Incorrect course render override "+row.id);
   for(int t=0;t<80&&p.getActionManager().hasSkillWorking();t++)tick();
   require(!p.getActionManager().hasSkillWorking()&&row.end.matches(p),"Course movement failed "+row.id+" at "+p+" expected "+row.end);
   require(!p.isLocked()&&p.getRun(),"Course cleanup failed "+row.id);
   if(row.stage>=0)require(p.getSkills().getXp(Skills.AGILITY)>xp,"No course XP "+row.id);
   require(p.getAppearence().getNative950WithheldBodies()==0,"Course appearance withheld "+row.id);
   System.out.println("PASS obstacle "+row.id+" stage="+row.stage+" end="+row.end);
  }
  void click(WorldObject o){ByteBuf bytes=Unpooled.buffer(10);bytes.writeByte((34+clientCipher.getAsInt())&255);
   bytes.writeByte(o.getY()+128);bytes.writeByte(o.getY()>>>8);bytes.writeInt(o.getId());bytes.writeByte(o.getX()>>>8);bytes.writeByte(o.getX()+128);bytes.writeByte(0);
   channel.writeInbound(bytes);channel.runPendingTasks();require(transport.drainActions(input::handle)==1,"Encrypted course click not routed");clicks++;}
  void tick(){int x=p.getX(),y=p.getY(),plane=p.getPlane();input.beginTick();p.processEntity();p.processEntityUpdate();input.afterMovement();
   entities.beginFrames(Collections.singletonList(p));entities.encode(new Native950Frames.Frame(p,channel,Collections.singletonList(p),Collections.emptyList(),null,false,7,x,y,plane));
   channel.write(Native950Packets.tickEnd());drain();p.resetMasks();p.setClientHasLoadedMapRegion();}
  void drain(){channel.flush();channel.runPendingTasks();Object message;while((message=channel.readOutbound())!=null)try{
   require(message instanceof ByteBuf,"Non-byte950 output");ByteBuf b=(ByteBuf)message;while(b.isReadable()){
    int op=(b.readUnsignedByte()-cipher.getAsInt())&255;if(op>=128)op=((op-128)<<8)|((b.readUnsignedByte()-cipher.getAsInt())&255);
    ServerPacket kind=null;for(ServerPacket k:ServerPacket.values())if(k.opcode()==op){kind=k;break;}require(kind!=null,"Unknown950 output");int len=kind.size();if(len==-1)len=b.readUnsignedByte();else if(len==-2)len=b.readUnsignedShort();require(len>=0&&len<=b.readableBytes(),"Malformed950 output");b.skipBytes(len);frames++;
   }}finally{ReferenceCountUtil.release(message);}channel.checkException();require(transport.terminalFailure()==null,"Agility transport failed");}
  public void close(){System.out.println("Agility routed clicks="+clicks+" frames="+frames);input.close();entities.release(p);World.removeNative950Player(p);channel.finishAndReleaseAll();}
 }
 static void require(boolean b,String message){if(!b)throw new AssertionError(message);}
}
