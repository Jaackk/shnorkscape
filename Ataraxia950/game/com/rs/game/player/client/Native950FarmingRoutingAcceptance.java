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
public final class Native950FarmingRoutingAcceptance {
    private static int frames,ticks;
    public static void main(String[] args)throws Exception {
        require(args.length==1,"Usage: Native950FarmingRoutingAcceptance <950-flat-cache>");
        require(NativeCacheVerification.isEnforced(),"Cache verification must remain enabled");
        System.setProperty(Native950World.SPAWNS_PROPERTY,"false");
        System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");
        Cache.initFlatReadOnly(Paths.get(args[0]));
        Native950World.getInstance().execute(()->{
            Native950Farming.verifyCacheBindings();Native950Construction.verifyCacheBindings();
            try(Fixture f=new Fixture()){check(f);}
            return null;
        }).get(120,TimeUnit.SECONDS);
        require(CoresManager.getNative950Scheduler().failed()==0,"World scheduler failed");
        System.out.println("PASS: "+(Native950Farming.crops().size()+Native950Construction.recipes().size())+" pinned additional recipes, "+ticks+" original engine ticks, "+frames+" decoded encrypted950 frames; no account file, listener or authentication");
        System.out.println("LIMIT: menu pixels and animation rendering still require the real client.");
    }
    private static void check(Fixture f) {
        f.level(Skills.FARMING,99);f.level(Skills.CONSTRUCTION,99);
        Native950Farming.refresh(f.player);f.run(2);
        for(int parent:new int[]{12,13,14,16,23})require(f.varpValues.getOrDefault(parent,Collections.emptyList()).contains(0),"Fresh login omitted Farming parent varp "+parent);
        List<Native950Farming.Plot> all=new ArrayList<>();for(int id:Native950Farming.patchIds())all.add(new Native950Farming.Plot(id,3050,3307,0,0,0,true,0,0));
        Native950Farming.restore(f.player,all);Native950Farming.refresh(f.player);f.run(2);
        for(int parent:new int[]{12,13,14,16})require(f.varpValues.getOrDefault(parent,Collections.emptyList()).contains(0x03030303),"Four crop slices failed native varp "+parent);
        require(f.varpValues.getOrDefault(23,Collections.emptyList()).contains(0x00030303),"Three herb crop slices failed native varp23");
        Native950Farming.restore(f.player,Collections.emptyList());Native950Farming.refresh(f.player);f.run(2);
        com.rs.game.WorldObject patch=f.station(8550);
        f.give(5341,1);f.give(5325,1);f.give(952,1);f.give(5318,6);f.run(2);
        // The client advertises the current morph's id, while collision stores its parent loc.
        com.rs.game.WorldObject weeds=new com.rs.game.WorldObject(8576,patch.getType(),patch.getRotation(),patch.getX(),patch.getY(),patch.getPlane());
        f.moveAway(patch);f.object(weeds,1);require(f.player.hasWalkSteps(),"Rake click did not approach base patch");
        require(f.amount(6055)==0,"Remote rake granted weeds");f.run(40);
        require(f.amount(6055)==3&&Native950Farming.snapshot(f.player).get(0).cleared,"Morphed Rake packet did not clear patch");require(f.varpValues.getOrDefault(12,Collections.emptyList()).contains(3),"Raked crop model was never sent to client");
        com.rs.game.WorldObject secondTile=f.station(8550);require(Native950Farming.definition(f.player,secondTile).id==8573,"Same allotment different loc lost raked state");
        com.rs.game.WorldObject empty=new com.rs.game.WorldObject(8573,secondTile.getType(),secondTile.getRotation(),secondTile.getX(),secondTile.getY(),secondTile.getPlane());
        f.moveAway(secondTile);f.useObject(5318,f.slot(5318),(1473<<16)|5,empty);require(f.player.hasWalkSteps(),"Seed Use did not approach base patch");f.run(40);
        require(f.amount(5318)==3&&Native950Farming.snapshot(f.player).get(0).seedId==5318,"Morphed seed use did not plant");require(Native950Farming.snapshot(f.player).size()==1,"Different scenery tiles made separate crop records");
        require(f.varpValues.getOrDefault(12,Collections.emptyList()).contains(6),"Planted potato model was never sent to client");
        Native950Farming.Crop potato=Native950Farming.crop(5318);
        Native950Farming.restore(f.player,Collections.singletonList(new Native950Farming.Plot(patch.getId(),patch.getX(),patch.getY(),patch.getPlane(),5318,System.currentTimeMillis()-potato.growMillis-1000,true,0,7)));
        Native950Farming.refresh(f.player);f.run(2);require(f.varpValues.getOrDefault(12,Collections.emptyList()).contains(10),"Mature potato model was never sent to client");
        com.rs.game.WorldObject mature=new com.rs.game.WorldObject(Native950Farming.definition(f.player,patch).id,patch.getType(),patch.getRotation(),patch.getX(),patch.getY(),patch.getPlane());
        f.object(mature,1);f.run(30);require(f.amount(1942)==7&&Native950Farming.snapshot(f.player).get(0).seedId==0,"Morphed harvest did not retain base patch/state");
        f.object(mature,1);f.run(4);require(f.amount(1942)==7,"Retired harvest actor duplicated crop");
        f.clear();com.rs.game.WorldObject bench=f.station(139147);f.give(2347,1);f.give(8794,1);f.give(960,4);f.give(1539,4);f.run(2);
        List<Native950ProductionMenu.Choice> rows=Native950Construction.choices(f.player,bench);int selected=-1;
        for(int i=0;i<rows.size();i++)if(rows.get(i).label.startsWith("Crude wooden chair ("))selected=i;
        require(selected>=0,"Crude chair absent from Construction menu");f.moveAway(bench);f.object(bench,1);require(f.player.hasWalkSteps(),"Construct click did not approach");f.run(40);
        for(int page=0;page<selected/3;page++)f.choose(3);f.choose(selected%3);f.choose(1);f.run(40);
        require(f.amount(61929)==2&&f.amount(960)==0&&f.amount(1539)==0&&f.amount(2347)==1&&f.amount(8794)==1,"Construct menu Make5 failed to stop at available materials or used wrong flatpack");
        require(f.hasStat(Skills.FARMING)&&f.hasStat(Skills.CONSTRUCTION),"Native skill XP frames missing");
        System.out.println("PASS actual VARP_SMALL/LARGE for all19 crop slices and5parents; encrypted950 morph Rake/seed Use/Harvest/stale-actor, distant approach, Construct/quantity menu, real materials/output and skill XP");
    }
    private static final class Fixture implements AutoCloseable {
        final int[] incoming={9,5,0,2},outgoing={59,55,50,52};
        final Native950Isaac clientCipher=new Native950Isaac(incoming),cipher=new Native950Isaac(outgoing);
        final Native950GameTransport transport=new Native950GameTransport(new Native950Isaac(incoming),new Native950Isaac(outgoing),Thread.currentThread());
        final EmbeddedChannel channel=new EmbeddedChannel(transport);
        final List<ServerPacket> output=new ArrayList<>();
        final Map<Integer,List<Integer>> varpValues=new HashMap<>();
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
                    if(kind==ServerPacket.VARP_LARGE){
                        require(length==6,"Bad native varp length");int at=bytes.readerIndex();
                        int id=((bytes.getUnsignedByte(at)-128)&255)|(bytes.getUnsignedByte(at+1)<<8);
                        int value=(bytes.getUnsignedByte(at+4)<<24)|(bytes.getUnsignedByte(at+5)<<16)|(bytes.getUnsignedByte(at+2)<<8)|bytes.getUnsignedByte(at+3);
                        varpValues.computeIfAbsent(id,k->new ArrayList<>()).add(value);
                    }
                    if(kind==ServerPacket.VARP_SMALL){
                        require(length==3,"Bad native small varp length");int at=bytes.readerIndex();
                        int id=((bytes.getUnsignedByte(at+2)-128)&255)|(bytes.getUnsignedByte(at+1)<<8);
                        int value=bytes.getByte(at);varpValues.computeIfAbsent(id,k->new ArrayList<>()).add(value);
                    }
                    bytes.skipBytes(length);output.add(kind);frames++;
                }
            }finally{ReferenceCountUtil.release(message);}
            channel.checkException();require(channel.isActive()&&transport.terminalFailure()==null,"Production transport failed: "+transport.terminalFailure());
        }
        public void close(){input.close();for(com.rs.game.WorldObject o:stations)World.removeObject(o);World.removeNative950Player(player);channel.finishAndReleaseAll();}
    }
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
}
