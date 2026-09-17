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
public final class Native950CraftingRoutingAcceptance {
    private static int frames,ticks;
    public static void main(String[] args)throws Exception {
        require(args.length==1,"Usage: Native950CraftingRoutingAcceptance <950-flat-cache>");
        require(NativeCacheVerification.isEnforced(),"Cache verification must remain enabled");
        System.setProperty(Native950World.SPAWNS_PROPERTY,"false");
        System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");
        Cache.initFlatReadOnly(Paths.get(args[0]));
        Native950World.getInstance().execute(()->{
            Native950Crafting.verifyCacheBindings();Native950Summoning.verifyCacheBindings();
            try(Fixture f=new Fixture()){check(f);checkSelectedTargets(f);checkNewSkills(f);}
            return null;
        }).get(120,TimeUnit.SECONDS);
        require(CoresManager.getNative950Scheduler().failed()==0,"World scheduler failed");
        System.out.println("PASS: "+(Native950Crafting.recipes().size()+Native950Summoning.recipes().size())+" pinned additional recipes, "+ticks+" original engine ticks, "+frames+" decoded encrypted950 frames; no account file, listener or authentication");
        System.out.println("LIMIT: menu pixels and animation rendering still require the real client.");
    }
    private static void check(Fixture f) {
        f.give(1733,1);f.give(1734,1);f.give(1741,6);double xp=f.xp(Skills.CRAFTING);
        f.use(1733,1741);f.choose(0);f.choose(1);f.run(30);
        require(f.amount(1061)==5&&f.amount(1741)==1&&f.amount(1733)==1&&f.amount(1734)==0,"Needle/leather menu and first thread reel");
        require(f.xp(Skills.CRAFTING)>xp&&f.hasStat(Skills.CRAFTING),"Crafting XP output");
        f.clear();f.give(1785,1);f.give(1775,1);f.button(1775,1);f.choose(0);f.choose(0);f.run(12);
        require(f.amount(1919)==1&&f.amount(1785)==1&&f.amount(1775)==0,"Craft inventory operation -> glass production");
        f.clear();com.rs.game.WorldObject wheel=f.station(2644);f.give(1737,2);f.object(wheel,2);f.run(2);f.choose(0);f.choose(1);f.run(20);
        require(f.amount(1759)==2&&f.amount(1737)==0,"Actual Spin option2 -> routed wheel menu -> wool");
        f.clear();com.rs.game.WorldObject furnace=f.station(11010);f.level(Skills.CRAFTING,5);f.give(2357,1);f.give(1592,1);f.object(furnace,1);f.run(2);
        for(int i=0;i<4;i++)f.choose(3);f.choose(0);f.choose(0);f.run(12);
        require(f.amount(1635)==1&&f.amount(2357)==0&&f.amount(1592)==1,"Shared smelting/crafting furnace pages -> gold ring");
        f.clear();com.rs.game.WorldObject obelisk=f.station(67036);f.give(12155,2);f.give(12158,2);f.give(12183,14);f.give(2859,2);xp=f.xp(Skills.SUMMONING);
        f.object(obelisk,1);f.run(2);f.choose(0);f.choose(1);f.run(20);
        require(f.amount(12047)==2&&f.amount(12155)==0&&f.amount(12158)==0&&f.amount(12183)==0&&f.amount(2859)==0,"Infuse menu quantities");
        require(f.xp(Skills.SUMMONING)>xp&&f.hasStat(Skills.SUMMONING),"Summoning XP output");
        f.object(obelisk,1);f.run(2);f.choose(0);f.choose(1);f.run(20);
        require(f.amount(12047)==0&&f.amount(12425)==20,"Pouch-to-scroll menu quantities");
        f.choose(0);f.choose(1);f.run(8);require(f.amount(12425)==20,"Retired menu duplicated scrolls");
        System.out.println("PASS: encrypted item-use and Craft option, wheel option2, shared furnace pages, obelisk pouch/scroll menus, quantity limits, retained tools, native inventory/XP output and stale-response rejection");
    }
    private static void checkSelectedTargets(Fixture f) {
        // Publish fixture grants, then enter the next input tick before selecting their actors.
        // A client cannot select an item that has not yet arrived in its inventory update.
        f.clear();com.rs.game.WorldObject fire=f.station(70755);f.level(Skills.COOKING,99);f.give(317,1);f.run(2);
        double cooking=f.xp(Skills.COOKING);int source=f.slot(317);
        // A foreign interface hash and a stale source slot cannot select this real backpack fish.
        f.useObject(317,source,(517<<16)|15,fire);f.run(3);
        f.useObject(317,27,(1473<<16)|5,fire);f.run(3);
        require(f.amount(317)==1&&f.amount(315)==0&&f.xp(Skills.COOKING)==cooking,"Foreign/stale Use source consumed food or awarded XP");
        f.moveAway(fire);f.useObject(317,source,(1473<<16)|5,fire);
        require(f.player.hasWalkSteps()&&!(f.player.getActionManager().getAction() instanceof com.rs.game.player.actions.Cooking),"Use-on-fire did not approach before cooking");
        f.run(40);f.choose(0);f.choose(0);f.run(12);
        require(f.amount(317)==0&&f.amount(315)==1&&f.xp(Skills.COOKING)>cooking,"Encrypted raw food Use-on-fire failed to cook");
        // Actual950 ordinary Eat dispatch must consume/heal without entering unrelated legacy handlers.
        require(Native950Food.supports(315),"Shrimps lack verified ordinary Eat support");
        f.player.setHitpoints(10);f.player.addFoodDelay(-10000);f.button(315,1);
        require(f.amount(315)==0&&f.player.getHitpoints()==40,"Encrypted Eat did not consume cooked shrimp and heal30 engine HP");f.run(2);

        f.clear();fire=f.station(70755);f.give(1511,2);f.run(2);double fireXp=f.xp(Skills.FIREMAKING);
        f.useObject(1511,f.slot(1511),(1473<<16)|5,fire);f.run(24);
        require(f.amount(1511)==0&&f.xp(Skills.FIREMAKING)>fireXp,"Encrypted logs Use-on-fire failed to reach original Bonfire");

        f.clear();com.rs.game.WorldObject altar=f.station(2478);f.level(Skills.RUNECRAFTING,1);f.give(1436,3);f.run(2);
        double runeXp=f.xp(Skills.RUNECRAFTING);source=f.slot(1436);
        f.useObject(1436,source,(1473<<16)|5,altar);
        f.player.getInventory().items.set(source,new Item(315,1));f.run(4);
        require(f.amount(1436)==2&&f.amount(556)==0&&f.xp(Skills.RUNECRAFTING)==runeXp,"Selected source changed before arrival but altar still consumed essence");
        f.useObject(1436,f.slot(1436),(1473<<16)|5,altar);f.run(12);
        require(f.amount(1436)==0&&f.amount(556)==2&&f.xp(Skills.RUNECRAFTING)>runeXp,"Encrypted essence Use-on-altar failed to run original multiplier/XP conversion");
        f.player.getControlerManager().startControler(new com.rs.game.player.controllers.Controller(){
            public void start() { }
            @Override public boolean handleItemOnObject(com.rs.game.WorldObject object,Item item){return false;}
        });
        f.clear();f.give(1436,1);f.run(2);runeXp=f.xp(Skills.RUNECRAFTING);
        f.useObject(1436,f.slot(1436),(1473<<16)|5,altar);f.run(4);
        require(f.amount(1436)==1&&f.amount(556)==0&&f.xp(Skills.RUNECRAFTING)==runeXp,"Controller item-on-object veto was ignored");
        System.out.println("PASS: encrypted selected-item opcode90 foreign/stale/arrival revalidation, walking before cooking, raw fish->fire->Eat, logs->Bonfire, essence->altar and controller veto");
    }
    private static void checkNewSkills(Fixture f) {
        f.player.getControlerManager().forceStop();f.clear();
        com.rs.game.WorldObject anvil=f.station(11497);f.level(Skills.SMITHING,1);f.give(2347,1);f.give(2349,10);f.run(2);
        List<Native950ProductionMenu.Choice> options=Native950Smithing.choices(f.player,anvil);int selected=-1;
        for(int i=0;i<options.size();i++)if(options.get(i).label.startsWith("Bronze dagger ("))selected=i;
        require(selected>=0,"Bronze dagger recipe not offered");double smithing=f.xp(Skills.SMITHING);
        f.moveAway(anvil);f.object(anvil,1);require(f.player.hasWalkSteps(),"Anvil click failed to approach");f.run(40);
        for(int page=0;page<selected/3;page++)f.choose(3);f.choose(selected%3);f.choose(1);f.run(220);
        require(f.amount(1205)==5&&f.amount(2349)==0&&f.amount(2347)==1&&f.xp(Skills.SMITHING)>smithing&&f.hasStat(Skills.SMITHING),"Encrypted anvil/menu/Make5 did not produce five bronze daggers with XP");
        f.clear();f.give(2347,1);f.give(2349,2);f.run(2);f.moveAway(anvil);
        f.useObject(2349,f.slot(2349),(1473<<16)|5,anvil);require(f.player.hasWalkSteps(),"Bar Use-on-anvil did not approach");f.run(40);
        for(int page=0;page<selected/3;page++)f.choose(3);f.choose(selected%3);f.choose(0);f.run(70);
        require(f.amount(1205)==1&&f.amount(2349)==0&&f.amount(2347)==1,"Bar Use-on-anvil failed shared Smithing menu");
        for(com.rs.game.player.actions.hunter.FlyingEntityHunter.FlyingEntities data:com.rs.game.player.actions.hunter.FlyingEntityHunter.FlyingEntities.values())if(data.isImpling()) {
            f.clear();f.give(data.getReward(),1);f.run(2);double hunter=f.xp(Skills.HUNTER);
            require(Native950InventoryMenu.usesOrdinaryOperations(data.getReward()),"Impling jar uses unhandled specialized menu");
            f.button(data.getReward(),3);f.run(2);
            require(f.amount(data.getReward())==0&&f.xp(Skills.HUNTER)==hunter,"Encrypted Loot operation3 failed or awarded catch XP: "+data);
        }
        System.out.println("PASS: encrypted distant anvil -> approach -> recipe pages -> Make5 -> bars/tool/output/XP; all12 current Loot operation3 routes and zero repeated catch XP");
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
