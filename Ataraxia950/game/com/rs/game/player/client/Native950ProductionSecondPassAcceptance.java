package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cores.CoresManager;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.network.modern.Native950GameTransport;
import com.rs.network.modern.Native950Isaac;
import com.rs.network.protocol.modern950.Native950Packets;
import com.rs.network.protocol.modern950.Native950Protocol.ServerPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.ReferenceCountUtil;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

/** Actual paired-cache, encrypted input, ordinary ActionManager and native XP/save acceptance. */
public final class Native950ProductionSecondPassAcceptance {
 private static int frames,ticks;
 public static void main(String[] args)throws Exception{
  require(args.length==1,"Usage: Native950ProductionSecondPassAcceptance <950-flat-cache>");require(NativeCacheVerification.isEnforced(),"Verification disabled");
  System.setProperty(Native950World.SPAWNS_PROPERTY,"false");System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");Cache.initFlatReadOnly(Paths.get(args[0]));
  Native950World.getInstance().execute(()->{Native950Crafting.verifyCacheBindings();Native950Prayer.verifyCacheBindings();try(Fixture f=new Fixture()){check(f);}return null;}).get(120,TimeUnit.SECONDS);
  require(CoresManager.getNative950Scheduler().failed()==0,"Scheduler failed");System.out.println("PASS: second-pass ordinary crafting/altar routes, "+ticks+" ticks, "+frames+" decoded encrypted950 frames; no account write, listener or authentication.");
 }
 private static void check(Fixture f){
  f.level(Skills.CRAFTING,7);f.give(1673,1);f.give(1759,1);f.run(2);double xp=f.xp(Skills.CRAFTING);f.use(1673,1759);f.choose(0);f.choose(0);f.run(8);
  require(f.amount(1673)==1&&f.amount(1692)==0&&f.xp(Skills.CRAFTING)==xp,"Stringing ignored current950 level8 requirement");
  f.clear();f.level(Skills.CRAFTING,8);f.give(1673,14);f.give(1759,14);f.run(2);require(f.player.getInventory().getFreeSlots()==0,"Expected full backpack");f.use(1759,1673);f.choose(0);f.choose(3);f.run(65);
  require(f.amount(1692)==14&&f.amount(1673)==0&&f.amount(1759)==0&&f.hasStat(Skills.CRAFTING),"Make-all stringing failed full-backpack replacement");
  f.choose(0);f.choose(3);f.run(4);require(f.amount(1692)==14,"Retired stringing menu duplicated outputs");
  f.clear();f.level(Skills.CRAFTING,99);f.give(1675,2);f.give(1759,2);f.run(2);f.use(1675,1759);f.choose(0);f.choose(3);f.input.walking();f.run(10);require(f.amount(1694)==0&&f.amount(1759)==2,"Walking did not cancel stringing");
  require(Native950Crafting.pair(1759,1720).isEmpty(),"Quest-gated unpowered symbol bypassed extra requirement");
  System.out.println("PASS: current-cache stringing level, both selected-item directions, full backpack Make-all, cancellation and stale replies");
  f.clear();com.rs.game.WorldObject altar=f.station(24343);f.give(526,3);f.give(20264,2);f.run(2);xp=f.xp(Skills.PRAYER);f.moveAway(altar);f.object(altar,2);require(f.player.hasWalkSteps(),"Offer did not approach altar");f.run(40);
  require(f.amount(526)==3&&f.xp(Skills.PRAYER)==xp,"Remote Offer awarded XP before selection");f.choose(0);f.choose(1);f.run(30);
  require(f.amount(526)==0&&f.amount(20264)==2&&f.xp(Skills.PRAYER)>xp&&f.hasStat(Skills.PRAYER),"Offer menu did not consume only the selected bones");
  f.object(altar,2);f.run(2);f.choose(0);f.choose(3);f.run(24);require(f.amount(20264)==0,"Ash offering failed");
  f.clear();f.give(526,2);f.run(2);f.moveAway(altar);f.useObject(526,f.slot(526),(1473<<16)|5,altar);require(f.player.hasWalkSteps(),"Bones Use did not approach");f.run(50);require(f.amount(526)==0,"Selected bones Use-on-altar did not offer all");
  f.clear();f.give(526,1);f.run(2);f.moveAway(altar);f.useObject(526,f.slot(526),(1473<<16)|5,altar);f.input.walking();f.run(30);require(f.amount(526)==1,"Cancelled approach consumed bone");
  f.player.setLocation(new WorldTile(altar.getX()-1,altar.getY(),0));World.updateEntityRegion(f.player);f.player.resetMasks();f.run(2);f.player.getPrayer().setPrayerpoints(0);f.object(altar,1);f.run(4);require(f.player.getPrayer().getPrayerpoints()==f.player.getSkills().getLevelForXp(Skills.PRAYER)*10,"Pray-at failed native prayer recharge");
  f.clear();f.give(526,2);f.run(2);f.object(altar,2);f.run(2);f.choose(0);f.choose(3);World.removeObject(altar);f.run(12);require(f.amount(526)==2,"Removed altar continued offering");
  System.out.println("PASS: encrypted Offer/quantity and selected-item Use, approach, bones/ashes, recharge, cancellation and removed-station checks");
 }
    private static final class Fixture implements AutoCloseable {
        final int[] incoming={9,5,0,2},outgoing={59,55,50,52};
        final Native950Isaac clientCipher=new Native950Isaac(incoming),cipher=new Native950Isaac(outgoing);
        final Native950GameTransport transport=new Native950GameTransport(new Native950Isaac(incoming),new Native950Isaac(outgoing),Thread.currentThread());
        final EmbeddedChannel channel=new EmbeddedChannel(transport);
        final List<ServerPacket> output=new ArrayList<>();
        final Player player;final Native950Interactions input;
        Fixture(){
            player=Player.createNative950("craft-route",new WorldTile(3217,3258,0),channel);
            player.setActive(true);player.setRunning(true);Native950World.installVarpSink(player);
            World.addNative950Player(player,1);World.updateEntityRegion(player);player.loadMapRegions();player.setClientHasLoadedMapRegion();
            Native950ItemCatalog catalog=new Native950ItemCatalog(Collections.<Native950ItemCatalog.Entry>emptyList()).withLegacyDrops();
            Native950Content.BankUi bank=new Native950Content.BankUi(517,201,15,317,39,new int[11],new int[11],Collections.emptyList(),Collections.emptyList());
            input=new Native950Interactions(player,channel,new Native950Content(catalog,bank),null,null);
            for(int slot=0;slot<28;slot++)player.getInventory().items.set(slot,null);
            input.bootstrap();drain();output.clear();
        }
        final List<com.rs.game.WorldObject> stations=new ArrayList<>();
        void clear(){input.walking();player.resetWalkSteps();player.resetMasks();for(int i=0;i<28;i++)player.getInventory().items.set(i,null);run(2);}
        com.rs.game.WorldObject station(int id){
            for(int y=3255;y<3290;y++)for(int x=3205;x<3250;x++){
                com.rs.game.WorldObject o=new com.rs.game.WorldObject(id,10,0,x,y,0);boolean clear=true;
                for(int dx=-1;dx<=o.getDefinitions().sizeX;dx++)for(int dy=-1;dy<=o.getDefinitions().sizeY;dy++)if(!World.isFloorFree(0,x+dx,y+dy,1))clear=false;
                if(!clear)continue;World.spawnObject(o);stations.add(o);
                for(int dx=-1;dx<=o.getDefinitions().sizeX;dx++)for(int dy=-1;dy<=o.getDefinitions().sizeY;dy++){
                    if(!World.isFloorFree(0,x+dx,y+dy,1))continue;player.setLocation(new WorldTile(x+dx,y+dy,0));World.updateEntityRegion(player);player.resetMasks();
                    if(Native950Mining.current(o)&&Native950Mining.inReach(player,o)){run(2);return o;}
                }
            }throw new AssertionError("Cannot place station "+id);
        }
        void object(com.rs.game.WorldObject o,int option){int[] opcodes={34,48,24,41,73};ByteBuf b=Unpooled.buffer(10);b.writeByte((opcodes[option-1]+clientCipher.getAsInt())&255);b.writeByte(o.getY()+128);b.writeByte(o.getY()>>>8);b.writeInt(o.getId());b.writeByte(o.getX()>>>8);b.writeByte(o.getX()+128);b.writeByte(0);send(b);}
        void moveAway(com.rs.game.WorldObject object) {
            for(int radius=5;radius<=8;radius++)for(int dx=-radius;dx<=radius;dx++)for(int dy=-radius;dy<=radius;dy++) {
                if(Math.max(Math.abs(dx),Math.abs(dy))!=radius)continue;
                int x=object.getX()+dx,y=object.getY()+dy;
                if(!World.isFloorFree(object.getPlane(),x,y,1))continue;
                int route=com.rs.game.route.RouteFinder.findRoute(com.rs.game.route.RouteFinder.WALK_ROUTEFINDER,x,y,object.getPlane(),1,
                        new com.rs.game.route.strategy.ObjectStrategy(object),false);
                if(route<=0||com.rs.game.route.RouteFinder.lastIsAlternative())continue;
                player.setLocation(new WorldTile(x,y,object.getPlane()));World.updateEntityRegion(player);player.resetMasks();run(2);return;
            }
            throw new AssertionError("No reachable distant Use approach for "+object.getId());
        }
        void useObject(int source,int slot,int hash,com.rs.game.WorldObject object) {
            ByteBuf b=Unpooled.buffer(19);b.writeByte((90+clientCipher.getAsInt())&255);
            b.writeByte(0);b.writeByte(object.getX()>>>8);b.writeByte(object.getX()+128);b.writeShort(slot);
            b.writeByte(object.getY()+128);b.writeByte(object.getY()>>>8);b.writeMediumLE(source);b.writeIntLE(object.getId());
            b.writeByte(hash>>>8);b.writeByte(hash);b.writeByte(hash>>>24);b.writeByte(hash>>>16);send(b);
        }
        void give(int id,int amount){require(Native950Skilling.giveItem(player,id,amount),"Fixture cannot give actual950 item "+id);}
        void level(int skill,int level){player.getSkills().setXpWithoutRefresh(skill,Skills.getXPForLevel(skill,level));player.getSkills().setLevelWithoutRefresh(skill,level);}
        double xp(int skill){return player.getSkills().getXp(skill);}
        int amount(int id){return player.getInventory().items.getNumberOf(id);}
        int slot(int id){int slot=player.getInventory().items.lookupSlot(id);require(slot>=0,"Missing inventory endpoint "+id);return slot;}
        void run(int count){for(int i=0;i<count;i++){ticks++;input.beginTick();player.processEntity();player.processEntityUpdate();input.afterMovement();channel.write(Native950Packets.tickEnd());drain();player.resetMasks();}}
        void choose(int row){response(1188,8+5*row);}
        void response(int face,int component){ByteBuf b=Unpooled.buffer(7);b.writeByte((101+clientCipher.getAsInt())&255);b.writeInt((face<<16)|component);b.writeByte(255);b.writeByte(127);send(b);}
        void button(int id,int option){int hash=(1473<<16)|5,slot=slot(id);int[] opcodes={18,122,89,100,81};
            ByteBuf b=Unpooled.buffer(10);b.writeByte((opcodes[option-1]+clientCipher.getAsInt())&255);b.writeMedium(id);
            b.writeByte(hash>>>16);b.writeByte(hash>>>24);b.writeByte(hash);b.writeByte(hash>>>8);b.writeShort(slot);send(b);}
        void use(int source,int target){int src=slot(source),dst=slot(target),hash=(1473<<16)|5;
            ByteBuf b=Unpooled.buffer(19);b.writeByte((69+clientCipher.getAsInt())&255);
            b.writeByte(src>>>8);b.writeByte(src+128);b.writeByte(source>>>16);b.writeByte(source);b.writeByte(source>>>8);
            b.writeByte(hash>>>8);b.writeByte(hash);b.writeByte(hash>>>24);b.writeByte(hash>>>16);b.writeShort(dst);
            b.writeByte(hash>>>8);b.writeByte(hash);b.writeByte(hash>>>24);b.writeByte(hash>>>16);b.writeMediumLE(target);send(b);}
        void send(ByteBuf bytes){channel.writeInbound(bytes);channel.runPendingTasks();require(transport.drainActions(input::handle)==1,"Encrypted production click failed to reach router");}
        boolean hasStat(int skill){return statSkills.contains(skill);}
        final Set<Integer> statSkills=new HashSet<>();
        int count(ServerPacket kind){int count=0;for(ServerPacket row:output)if(row==kind)count++;return count;}
        void drain(){channel.flush();channel.runPendingTasks();Object message;
            while((message=channel.readOutbound())!=null)try{
                require(message instanceof ByteBuf,"Transport emitted nonbytes");ByteBuf bytes=(ByteBuf)message;
                while(bytes.isReadable()){
                    int opcode=(bytes.readUnsignedByte()-cipher.getAsInt())&255;
                    if(opcode>=128)opcode=((opcode-128)<<8)|((bytes.readUnsignedByte()-cipher.getAsInt())&255);
                    ServerPacket kind=null;for(ServerPacket row:ServerPacket.values())if(row.opcode()==opcode){kind=row;break;}
                    require(kind!=null,"Unrecognized950 opcode "+opcode);int length=kind.size();
                    if(length==-1)length=bytes.readUnsignedByte();else if(length==-2)length=bytes.readUnsignedShort();
                    require(length>=0&&length<=bytes.readableBytes(),"Bad950 frame length "+kind);
                    if(kind==ServerPacket.UPDATE_STAT&&length>0)statSkills.add((-bytes.getUnsignedByte(bytes.readerIndex()))&255);
                    bytes.skipBytes(length);output.add(kind);frames++;
                }
            }finally{ReferenceCountUtil.release(message);}
            channel.checkException();require(channel.isActive()&&transport.terminalFailure()==null,"Production transport failed: "+transport.terminalFailure());
        }
        public void close(){input.close();for(com.rs.game.WorldObject o:stations)World.removeObject(o);World.removeNative950Player(player);channel.finishAndReleaseAll();}
    }
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
}
