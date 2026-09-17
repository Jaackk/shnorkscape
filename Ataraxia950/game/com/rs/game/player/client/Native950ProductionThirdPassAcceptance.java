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
public final class Native950ProductionThirdPassAcceptance {
 private static int frames,ticks;
 public static void main(String[] args)throws Exception{
  require(args.length==1,"Usage: Native950ProductionThirdPassAcceptance <950-flat-cache>");require(NativeCacheVerification.isEnforced(),"Verification disabled");
  System.setProperty(Native950World.SPAWNS_PROPERTY,"false");System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");Cache.initFlatReadOnly(Paths.get(args[0]));
  Native950World.getInstance().execute(()->{Native950Crafting.verifyCacheBindings();try(Fixture f=new Fixture()){check(f);}return null;}).get(120,TimeUnit.SECONDS);
  require(CoresManager.getNative950Scheduler().failed()==0,"Scheduler failed");System.out.println("PASS: third-pass production routes, "+ticks+" ticks, "+frames+" decoded encrypted950 frames; no account write, listener or authentication.");
 }
 private static void check(Fixture f){
  require(Native950Production.recipes().size()==276,"Expected276 ordinary recipes, got "+Native950Production.recipes().size());
  require(Native950Crafting.recipes().size()==117,"Expected117 crafting recipes");
  Native950Construction.verifyCacheBindings();require(Native950Baking.foods().size()==3,"Missing baked foods");
  for(Native950Production.Recipe r:Native950Production.recipes())require(Native950Production.verified(r),"Unverified recipe "+r.label);
  f.clear();f.level(Skills.HERBLORE,18);f.give(1939,30);f.give(249,2);f.give(233,1);f.run(2);double xp=f.xp(Skills.HERBLORE);
  f.use(1939,249);f.choose(0);f.choose(3);f.run(10);require(f.amount(10142)==0&&f.xp(Skills.HERBLORE)==xp,"Tar ignored level19");
  f.level(Skills.HERBLORE,19);f.use(249,1939);f.choose(0);f.choose(3);f.run(15);require(f.amount(10142)==30&&f.amount(1939)==0&&f.amount(233)==1&&f.hasStat(Skills.HERBLORE),"Tar Make-all materials/tool/XP failed");
  f.clear();f.level(Skills.FLETCHING,56);f.give(1607,2);f.give(1755,1);f.run(2);f.use(1755,1607);f.choose(0);f.choose(3);f.run(15);require(f.amount(9189)==24&&f.amount(1607)==0&&f.amount(1755)==1,"Sapphire bolt tips route failed");
  Native950Production.Recipe tips=null;for(Native950Production.Recipe r:Native950Production.pair(1607,1755))if(r.produced()[0].getId()==9189)tips=r;require(tips!=null&&tips.xp==4.7,"Gem cutting accidentally awards Crafting XP or XP per tip");
  f.clear();f.level(Skills.CRAFTING,54);f.give(1391,2);f.give(571,2);f.run(2);f.use(571,1391);f.choose(0);f.choose(3);f.run(15);require(f.amount(1395)==2&&f.amount(571)==0,"Water battlestaff assembly failed");
  f.clear();f.level(Skills.CRAFTING,54);f.give(1775,1);f.give(1785,1);f.run(2);Native950Production.Recipe bomb=null;for(Native950Production.Recipe r:Native950Crafting.recipes())if(r.produced()[0].getId()==48961)bomb=r;require(bomb!=null&&Native950Crafting.startInventory(f.player,bomb,1),"Bomb vial cannot start");f.run(8);require(f.amount(48961)==1&&f.amount(1785)==1,"Bomb vial failed");
  f.clear();f.level(Skills.COOKING,99);f.give(1933,1);f.give(1929,1);f.run(2);f.use(1933,1929);f.choose(0);f.choose(0);f.run(8);require(f.amount(2307)==1&&f.amount(1931)==1&&f.amount(1925)==1,"Dough lost empty water/flour containers");
  com.rs.game.WorldObject range=f.station(9682);f.run(2);f.useObject(2307,f.slot(2307),(1473<<16)|5,range);f.run(3);f.choose(0);f.choose(0);f.run(8);require(f.amount(2309)==1&&f.amount(2307)==0&&f.hasStat(Skills.COOKING),"Bread baking failed");
  f.clear();f.give(1887,1);f.give(1933,1);f.give(1944,1);f.give(1927,1);f.run(2);f.use(1887,1944);f.choose(0);f.choose(0);f.run(8);require(f.amount(1889)==1&&f.amount(1931)==1&&f.amount(1925)==1,"Cake preparation failed");require(Native950Cooking.start(f.player,range,1889,1),"Cake baking cannot start");f.run(8);require(f.amount(1891)==1&&f.amount(1887)==1,"Cake did not return tin");
  f.clear();f.give(1889,28);f.run(2);xp=f.xp(Skills.COOKING);require(!Native950Cooking.start(f.player,range,1889,1),"Full cake inventory accepted extra tin output");require(f.amount(1889)==28&&f.xp(Skills.COOKING)==xp,"Full cake inventory mutated");
  f.clear();f.give(2307,2);f.run(2);require(Native950Cooking.start(f.player,range,2307,2),"Baking cancellation fixture start failed");f.input.walking();f.run(8);require(f.amount(2307)==2,"Baking continued after walking");
  f.clear();f.level(Skills.CONSTRUCTION,99);com.rs.game.WorldObject bench=f.station(13704);f.give(960,4);f.give(4819,2);f.give(4824,2);f.give(2347,1);f.give(8794,1);f.run(2);
  Native950Production.Recipe chair=null;for(Native950Production.Recipe r:Native950Construction.recipes())if(r.produced()[0].getId()==61929)chair=r;require(chair!=null&&chair.maximumByMaterials(f.player)==2,"Mixed nail batches counted incorrectly");
  f.useObject(4819,f.slot(4819),(1473<<16)|5,bench);f.run(4);f.choose(0);f.choose(3);f.run(16);require(f.amount(61929)==2&&f.amount(4819)==0&&f.amount(4824)==0&&f.amount(960)==0,"Construction did not switch bronze to rune nails per batch");
  Native950Production.Recipe high=null;for(Native950Production.Recipe r:Native950Construction.recipes())if(r.level>20){high=r;break;}require(high!=null&&!Native950Construction.start(f.player,bench,high,1),"Wooden workbench ignored level20 limit");
  require(Native950Smelting.recipes().size()==12,"Missing cache smelting bars");
  // Every new ordinary row gets a real one-batch transaction, not only a metadata lookup.
  for(int row=243;row<Native950Production.recipes().size();row++){
   Native950Production.Recipe r=Native950Production.recipes().get(row);f.clear();f.level(r.skill,99);
   for(Item item:r.consumed())f.give(item.getId(),item.getAmount());for(int tool:r.tools())if(f.amount(tool)==0)f.give(tool,1);f.run(2);
   require(f.player.getActionManager().setAction(new Native950ProductionAction(r,1)),"New recipe cannot start: "+r.label);f.run(9);
   for(Item output:r.produced())require(f.amount(output.getId())==output.getAmount(),"Wrong output for "+r.label+" id="+output.getId());
   for(int tool:r.tools())require(f.amount(tool)>=1,"Consumed tool for "+r.label);
  }
  f.clear();com.rs.game.WorldObject furnace=f.station(11010);f.level(Skills.SMITHING,1);f.give(436,2);f.give(438,2);f.run(2);
  Native950Production.Recipe bronze=null;for(Native950Production.Recipe r:Native950Smelting.recipes())if(r.produced()[0].getId()==2349)bronze=r;
  require(bronze!=null&&bronze.xp==1,"Old bronze smelting XP remains");require(Native950Smelting.start(f.player,furnace,bronze,2),"Smelting cannot start");f.run(4);require(f.amount(2349)==0,"Base smelting completed before5 ticks");f.run(1);require(f.amount(2349)==1,"Base smelting did not finish on fifth tick");f.run(5);require(f.amount(2349)==2,"Second smelting batch duration drifted");
  require(Native950Smelting.smeltingTicks(2349,2)==4&&Native950Smelting.smeltingTicks(2349,5)==3&&Native950Smelting.smeltingTicks(44844,90)==5&&Native950Smelting.smeltingTicks(44844,91)==4&&Native950Smelting.smeltingTicks(44844,94)==3,"Smelting milestones incorrect");
  System.out.println("PASS: all33 new ordinary recipes; tar, bolt tips, staves, advanced glass, food preparation/baking, mixed-nail construction, workbench limits and smelting timing");
 }
    static final class Fixture implements AutoCloseable {
        final int[] incoming={9,5,0,2},outgoing={59,55,50,52};
        final Native950Isaac clientCipher=new Native950Isaac(incoming),cipher=new Native950Isaac(outgoing);
        final Native950GameTransport transport=new Native950GameTransport(new Native950Isaac(incoming),new Native950Isaac(outgoing),Thread.currentThread());
        final EmbeddedChannel channel=new EmbeddedChannel(transport);
        final List<ServerPacket> output=new ArrayList<>();
        static final class CapturedPacket {
            final ServerPacket kind;final byte[] body;
            CapturedPacket(ServerPacket kind,byte[] body){this.kind=kind;this.body=body;}
        }
        final List<CapturedPacket> captured=new ArrayList<>();
        final List<Integer> scripts=new ArrayList<>();
        final Map<Integer,Integer> varps=new HashMap<>();
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
        boolean nativeQuantityStep;
        Native950ProductionMenu menu(){try{java.lang.reflect.Field f=Native950Interactions.class.getDeclaredField("productionMenu");f.setAccessible(true);return (Native950ProductionMenu)f.get(input);}catch(ReflectiveOperationException ex){throw new AssertionError(ex);}}
        void choose(int row){
            Native950ProductionMenu m=menu();
            if(!m.isNativeOpen()){nativeQuantityStep=false;response(1188,8+5*row);return;}
            if(!nativeQuantityStep){
                require(row==0,"Native fixture needs explicit product selection for row "+row);
                Native950ProductionUiCatalog.Category c=Native950ProductionUiCatalog.byId(m.currentCategoryId());
                require(c!=null,"Unknown native category");int ordinal=-1;for(int i=0;i<c.displayIds.length;i++)if(c.displayIds[i]==m.selectedDisplayId()){ordinal=i;break;}
                require(ordinal>=0,"Selected native product missing from cache category");
                nativeButton(1371,22,1+4*ordinal,m.selectedDisplayId());nativeQuantityStep=true;
            }else{
                int amount=row==3?m.maximumQuantity():row==0?1:row==1?5:row==2?10:0;
                require(amount>0,"Unsupported native quantity choice");amount=Math.min(amount,m.maximumQuantity());
                nativeButton(1371,20,amount-1,-1);response(1370,30);nativeQuantityStep=false;
            }
        }
        void nativeButton(int face,int component,int actor,int item){int hash=(face<<16)|component;ByteBuf b=Unpooled.buffer(10);b.writeByte((18+clientCipher.getAsInt())&255);b.writeMedium(item);b.writeByte(hash>>>16);b.writeByte(hash>>>24);b.writeByte(hash);b.writeByte(hash>>>8);b.writeShort(actor);send(b);}
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
                    if(kind==ServerPacket.IF_OPENSUB||kind==ServerPacket.IF_CLOSESUB||kind==ServerPacket.IF_SETHIDE||kind==ServerPacket.RUNCLIENTSCRIPT){
                        byte[] body=new byte[length];bytes.getBytes(bytes.readerIndex(),body);captured.add(new CapturedPacket(kind,body));
                    }
                    if(kind==ServerPacket.VARP_LARGE&&length==6){int at=bytes.readerIndex();int id=((bytes.getUnsignedByte(at)-128)&255)|(bytes.getUnsignedByte(at+1)<<8);int value=(bytes.getUnsignedByte(at+2)<<8)|bytes.getUnsignedByte(at+3)|(bytes.getUnsignedByte(at+4)<<24)|(bytes.getUnsignedByte(at+5)<<16);varps.put(id,value);}
                    if(kind==ServerPacket.RUNCLIENTSCRIPT&&length>=4)scripts.add(bytes.getInt(bytes.readerIndex()+length-4));
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
