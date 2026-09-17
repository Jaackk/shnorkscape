package com.rs.game.player.client;
import com.rs.cache.Cache;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.cores.CoresManager;
import com.rs.game.*;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.actions.Fishing;
import com.rs.game.player.actions.divination.DivinationHarvest;
import com.rs.game.player.actions.thieving.PickPocketAction;
import com.rs.game.player.controllers.Controller;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.network.modern.Native950GameTransport;
import com.rs.network.modern.Native950Isaac;
import com.rs.network.protocol.modern950.Native950Packets;
import com.rs.network.protocol.modern950.Native950Protocol.ServerPacket;
import com.rs.utils.data.parsers.npcs.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.ReferenceCountUtil;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
/** Real menu packets -> published NPC identity -> walking/controller gate -> original actions and reward frames. */
public final class Native950NpcSkillsAcceptance {
 static int frames,ticks;
 public static void main(String[] args)throws Exception {
  require(args.length==1,"Usage: Native950NpcSkillsAcceptance <cache>");
  System.setProperty(Native950World.SPAWNS_PROPERTY,"false");System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");System.setProperty(Native950SpawnScope.PROPERTY,"0");
  Cache.initFlatReadOnly(Paths.get(args[0]));Native950World world=Native950World.getInstance();
  world.execute(()->{
   NPCWeaknessesDataParser.init();NPCCombatDefinitionsDataParser.init();NPCStatsDataParser.init();NPCSpawnsDataParser.init();
   population(world);
   for(boolean run:new boolean[]{false,true})for(int[] spec:new int[][]{{1,3,Skills.THIEVING,2},{327,1,Skills.FISHING,1},{327,3,Skills.FISHING,2},{18150,1,Skills.DIVINATION,1},{5085,1,Skills.HUNTER,1},{1028,1,Skills.HUNTER,1}})
    try(Fixture f=new Fixture(world,spec[0],spec[1],spec[2],spec[3],run)){f.check();}
   try(Fixture f=new Fixture(world,18150,1,Skills.DIVINATION,1,true)){f.checkHarvestCapacityFeedback();}
   try(Fixture f=new Fixture(world,5085,1,Skills.HUNTER,1,false)){f.respawnCancelledByClear();}
   require(World.getPlayers().isEmpty()&&World.getNPCs().isEmpty()&&world.nativeNpcs().isEmpty(),"Probe cleanup left entities");return null;
  }).get(180,TimeUnit.SECONDS);
  require(CoresManager.getNative950Scheduler().failed()==0,"Scheduler recorded failure");
  System.out.println("PASS: six NPC menu paths walking/running, client visibility, stationary arrival, controller mapping/veto, final-arrival cancellation, original actions/rewards, butterfly roster/viewport respawn and generation clear; "+ticks+" ticks, "+frames+" encrypted950 frames; no socket or save writes");
 }
 static void population(Native950World world){
  Set<Integer> regions=new TreeSet<>(),species=new TreeSet<>();int rows=0,clear=0,blocked=0,spawned=0;
  for(int region=0;region<=65535;region++)for(NPCSpawnsDataParser.Native950Spawn row:NPCSpawnsDataParser.native947Spawns(region))if(Native950SkillNpcPopulation.candidate(row.npcId))regions.add(region);
  for(int region:regions){World.getRegion(region,true);
   for(NPCSpawnsDataParser.Native950Spawn row:NPCSpawnsDataParser.native947Spawns(region))if(Native950SkillNpcPopulation.candidate(row.npcId)){
    rows++;NPCDefinitions d=NPCDefinitions.getNPCDefinitions(row.npcId);if(!Native950SkillNpcPopulation.verified(row.npcId,d)){blocked++;System.out.println("SKIP unsupported950 skill spawn "+row);continue;}
    if(World.isFloorFree(row.plane,row.x,row.y)){clear++;species.add(row.npcId);}else{blocked++;System.out.println("SKIP blocked950 skill spawn "+row);}
   }
   spawned+=world.spawnRegion(region);require(world.spawnRegion(region)==0,"Region duplicated its skill NPCs");
  }
  require(rows>=187&&species.size()>=25,"Expected original wisps/butterflies plus implings and novice master, got "+rows+"/"+species.size());require(species.contains(8461)&&species.contains(1028)&&species.contains(1030),"Missing verified master/impling population");
  require(spawned==clear&&world.nativeNpcs().size()==clear,"Source/clear/owner spawn counts differ");
  for(NPC npc:world.nativeNpcs())require(Native950SkillNpcPopulation.candidate(npc.getId()),"Outside Lumbridge populated unrelated NPC "+npc.getId());
  System.out.println("PASS: original skill population "+rows+" source rows in "+regions.size()+" regions; "+spawned+" clear950 spawns, "+blocked+" blocked skips,"+species.size()+" species; unrelated NPCs remain excluded");
  world.clearNativeNpcs();
 }
 static final class Gate extends Controller {int called;boolean allow=true;public void start(){}@Override public boolean processNPCClick1(NPC n){called=1;return allow;}@Override public boolean processNPCClick2(NPC n){called=2;return allow;}@Override public boolean processNPCClick3(NPC n){called=3;return allow;}}
 static final class Fixture implements AutoCloseable {
  final Native950World world;final int id,option,skill,gate;final boolean running;
  final int[] incoming={9,5,0,2},outgoing={59,55,50,52};
  final Native950Isaac clientCipher=new Native950Isaac(incoming),serverCipher=new Native950Isaac(outgoing);
  final Native950GameTransport transport=new Native950GameTransport(new Native950Isaac(incoming),new Native950Isaac(outgoing),Thread.currentThread());
  final EmbeddedChannel channel=new EmbeddedChannel(transport);
  final Native950EntityFrames entities=new Native950EntityFrames();final Native950NpcView npcs=new Native950NpcView(null,null);
  final Player player=Player.createNative950("npcskills",new WorldTile(3217,3258,0),channel);
  final Gate controller=new Gate();final Native950Interactions input;NPC npc;int statFrames,inventoryFrames,npcFrames;
  Fixture(Native950World world,int id,int option,int skill,int gate,boolean running){
   this.world=world;this.id=id;this.option=option;this.skill=skill;this.gate=gate;this.running=running;
   player.setActive(true);player.setRun(running);Native950World.installVarpSink(player);World.addNative950Player(player,1);World.updateEntityRegion(player);player.loadMapRegions();player.setClientHasLoadedMapRegion();
   Native950ItemCatalog catalog=new Native950ItemCatalog(Collections.<Native950ItemCatalog.Entry>emptyList()).withLegacyDrops();
   Native950Content.BankUi bank=new Native950Content.BankUi(517,201,15,317,39,new int[11],new int[11],Collections.emptyList(),Collections.emptyList());
   input=new Native950Interactions(player,channel,new Native950Content(catalog,bank),null,npcs);input.bootstrap();
   for(int i=0;i<28;i++)player.getInventory().items.set(i,null);
   player.getSkills().setXpWithoutRefresh(skill,13034431);player.getSkills().setLevelWithoutRefresh(skill,99);player.setHitpoints(player.getMaxHitpoints());
   player.getControlerManager().startControler(controller);
   WorldTile target=player.transform(5,0,0);require(World.isFloorFree(0,target.getX(),target.getY()),"Fixture NPC tile blocked");npc=NPC.createNative950Diagnostic(id,target);world.addDiagnosticNpc(npc);
   if(skill==Skills.FISHING){give(option==1?303:307,1);if(option==3)give(313,50);}
   if(skill==Skills.HUNTER){give(10010,1);give(id==1028?11260:10012,5);wield(10010);}
   player.getAppearence().generateAppearenceData();Native950World.SceneConfig scene=new Native950World.SceneConfig(player.getX(),player.getY(),0,1,7,0,0,0);
   for(Native950Packets.Packet packet:entities.admit(player,scene,Collections.singletonList(player)))channel.write(packet);drain();
  }
  void check(){
   double before=player.getSkills().getXp(skill);click();require(!player.hasWalkSteps()&&!player.getActionManager().hasSkillWorking(),"Unpublished NPC was clickable");tick();
   if(skill==Skills.HUNTER){tick();require(player.getEquipment().getWeaponId()==10010,"Routed net Wield did not finish through the original scheduler");}
   require(npcs.canInteract(player,npc.getIndex()),"NPC absent from published viewport");click();require(player.hasWalkSteps(),"Distant NPC click did not route");
   int steps=0;while(player.hasWalkSteps()&&steps++<100)tick();require(steps>1&&steps<100,"Failed collision approach");
   require(!player.getActionManager().hasSkillWorking(),"NPC action started in final walking frame");
   input.walking();player.resetWalkSteps();for(int i=0;i<5;i++)tick();require(player.getSkills().getXp(skill)==before&&!player.getActionManager().hasSkillWorking(),"Arrival cancellation still awarded skill");
   controller.called=0;click();tick();require(actionMatches(),"NPC route did not start expected action for "+id+" op"+option);
   require(controller.called==gate,"Wrong legacy controller callback for NPC "+id+" op"+option+": "+controller.called+" expected"+gate);
   for(int attempt=0;attempt<20&&player.getSkills().getXp(skill)==before;attempt++){
    for(int i=0;i<40&&player.getSkills().getXp(skill)==before;i++)tick();
    if(player.getSkills().getXp(skill)==before){player.unlock();click();tick();}
   }
   require(player.getSkills().getXp(skill)>before&&statFrames>0&&inventoryFrames>0,"Routed NPC action did not send authoritative item/XP updates");
   if(skill==Skills.HUNTER){require(npc.hasFinished()&&!world.nativeNpcs().contains(npc),"Captured NPC retained in owner roster");for(int i=0;i<(id==1028?100:Native950Hunter.RESPAWN_TICKS)+3;i++)tick();NPC replacement=null;for(NPC n:world.nativeNpcs())if(n.getId()==id)replacement=n;
    require(replacement!=null&&replacement!=npc&&npcs.canInteract(player,replacement.getIndex()),"Respawn missing from owner roster or published viewport");npc=replacement;
   }
   input.walking();player.resetWalkSteps();player.unlock();for(int i=0;i<2;i++)tick();before=player.getSkills().getXp(skill);
   controller.allow=false;controller.called=0;click();for(int i=0;i<8;i++)tick();require(controller.called==gate&&!player.getActionManager().hasSkillWorking()&&player.getSkills().getXp(skill)==before,"Controller veto did not prevent routed skill");
   System.out.println("PASS: encrypted NPC"+id+" option"+option+" running="+running+" -> "+steps+" approach ticks -> final-frame cancellation -> stationary original action -> callback"+gate+" -> inventory/XP frames; NPC_INFO="+npcFrames);
  }
  void checkHarvestCapacityFeedback(){
   player.getSkills().setXpWithoutRefresh(Skills.DIVINATION,0);player.getSkills().setLevelWithoutRefresh(Skills.DIVINATION,1);
   for(int slot=0;slot<26;slot++)player.getInventory().items.set(slot,new Item(1436,1));
   player.getInventory().items.set(26,new Item(29313,1));player.getInventory().items.set(27,new Item(29384,1));
   tick();messages.clear();double before=player.getSkills().getXp(Skills.DIVINATION);click();
   for(int i=0;i<50;i++)tick();
   require(!player.getActionManager().hasSkillWorking()&&player.getSkills().getXp(Skills.DIVINATION)==before,"Full backpack still harvested");
   require(player.getInventory().getAmountOf(29313)==1&&player.getInventory().getAmountOf(29384)==1,"Full backpack changed rewards");
   require(messages.size()==1&&messages.get(0).contains("backpack")&&messages.get(0).contains("memories"),"Expected just the actionable capacity message, got "+messages);
   player.getInventory().items.set(27,null);messages.clear();click();tick();
   require(player.getActionManager().getAction() instanceof DivinationHarvest,"Freeing one memory slot did not allow harvesting");
   for(int i=0;i<100&&player.getActionManager().hasSkillWorking();i++)tick();
   require(player.getInventory().getAmountOf(29313)>1&&player.getInventory().getAmountOf(29384)==1&&player.getSkills().getXp(Skills.DIVINATION)>before,"Harvest did not fill freed slot and award XP");
   require(!player.getActionManager().hasSkillWorking()&&messages.size()==1&&messages.get(0).contains("backpack"),"Filling the last slot did not stop with one capacity message: "+messages);
   // A different refusal must stay specific too; no generic tail after the level explanation.
   input.walking();world.clearNativeNpcs();player.getInventory().items.set(27,null);
   npc=NPC.createNative950Diagnostic(18151,player.transform(1,0,0));world.addDiagnosticNpc(npc);tick();tick();require(npcs.canInteract(player,npc.getIndex()),"Replacement higher-tier wisp not yet published");messages.clear();click();tick();
   require(!player.getActionManager().hasSkillWorking()&&messages.size()==1&&messages.get(0).contains("Divination level of 10"),"Wrong low-level feedback: "+messages);
   System.out.println("PASS: level1 encrypted Harvest with energy/memory and full backpack -> one specific unfiltered refusal, no reward; free one slot -> items/XP -> last-slot stop; higher-tier level refusal stays specific");
  }
  void respawnCancelledByClear(){
   tick();world.respawnCapturedButterfly(npc,3);require(!world.nativeNpcs().contains(npc)&&npc.hasFinished(),"Capture did not release owner slot");
   world.clearNativeNpcs();for(int i=0;i<6;i++)tick();require(world.nativeNpcs().isEmpty()&&World.getNPCs().isEmpty(),"Old world generation respawned an orphan NPC");
   System.out.println("PASS: clearing last-world NPC population cancels pending butterfly respawn");
  }
  boolean actionMatches(){Action a=player.getActionManager().getAction();return skill==Skills.THIEVING?a instanceof PickPocketAction:skill==Skills.FISHING?a instanceof Fishing:skill==Skills.DIVINATION?a instanceof DivinationHarvest:a instanceof Native950Hunter.CatchAction;}
  void give(int id,int amount){require(Native950Skilling.giveItem(player,id,amount),"Cannot give "+id);}
  void wield(int id){int slot=player.getInventory().getItems().getThisItemSlot(id),hash=(1473<<16)|5;ByteBuf b=Unpooled.buffer(10);b.writeByte((122+clientCipher.getAsInt())&255);b.writeMedium(id);b.writeByte(hash>>>16);b.writeByte(hash>>>24);b.writeByte(hash);b.writeByte(hash>>>8);b.writeShort(slot);send(b);}
  void click(){int[] op={60,92,27,107,13,36};ByteBuf b=Unpooled.buffer(4);b.writeByte((op[option-1]+clientCipher.getAsInt())&255);b.writeByte(0);b.writeByte(npc.getIndex()>>>8);b.writeByte(npc.getIndex()+128);send(b);}
  void send(ByteBuf bytes){channel.writeInbound(bytes);channel.runPendingTasks();require(transport.drainActions(input::handle)==1,"Encrypted click failed decoding");}
  void tick(){ticks++;int x=player.getX(),y=player.getY(),plane=player.getPlane();WorldTasksManager.processTasks();input.beginTick();player.processEntity();player.processEntityUpdate();input.afterMovement();
   if(player.getActionManager().hasSkillWorking()&&player.getNextAnimation()!=null&&player.getNextAnimation().getIds()[0]>=0)require(player.getX()==x&&player.getY()==y&&plane==player.getPlane(),"Skill animation shared final walking frame");
   entities.beginFrames(Collections.singletonList(player));entities.encode(new Native950Frames.Frame(player,channel,Collections.singletonList(player),new ArrayList<NPC>(world.nativeNpcs()),npcs,false,7,x,y,plane));channel.write(Native950Packets.tickEnd());drain();player.resetMasks();for(NPC n:world.nativeNpcs())n.resetMasks();
  }
  final List<String> messages=new ArrayList<>();
  void drain(){channel.flush();channel.runPendingTasks();Object msg;while((msg=channel.readOutbound())!=null)try{require(msg instanceof ByteBuf,"Unencoded outbound frame");ByteBuf b=(ByteBuf)msg;while(b.isReadable()){
   int op=(b.readUnsignedByte()-serverCipher.getAsInt())&255;if(op>=128)op=((op-128)<<8)|((b.readUnsignedByte()-serverCipher.getAsInt())&255);ServerPacket packet=null;for(ServerPacket p:ServerPacket.values())if(p.opcode()==op){packet=p;break;}require(packet!=null,"Unknown encrypted opcode"+op);int length=packet.size();if(length==-1)length=b.readUnsignedByte();else if(length==-2)length=b.readUnsignedShort();require(length>=0&&length<=b.readableBytes(),"Invalid packet length"+packet);if(packet==ServerPacket.MESSAGE_GAME){ByteBuf msgBody=b.slice(b.readerIndex(),length);int messageType=msgBody.readUnsignedByte();if(messageType>=128)messageType=((messageType-128)<<8)|msgBody.readUnsignedByte();msgBody.skipBytes(4);require(msgBody.readUnsignedByte()==0,"Unexpected named game message");String text=msgBody.toString(msgBody.readerIndex(),msgBody.readableBytes()-1,java.nio.charset.Charset.forName("windows-1252"));if(text.contains("backpack")||text.contains("skill action")||text.contains("Divination level")){require(messageType==0,"Skill refusal is filtered");messages.add(text);}}b.skipBytes(length);frames++;if(packet==ServerPacket.UPDATE_STAT)statFrames++;if(packet==ServerPacket.UPDATE_INV_FULL||packet==ServerPacket.UPDATE_INV_PARTIAL)inventoryFrames++;if(packet==ServerPacket.NPC_INFO)npcFrames++;
  }}finally{ReferenceCountUtil.release(msg);}channel.checkException();require(channel.isActive()&&transport.terminalFailure()==null,"Native transport failed");}
  public void close(){input.close();entities.release(player);npcs.close();World.removeNative950Player(player);world.clearNativeNpcs();channel.finishAndReleaseAll();}
 }
 static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
}
