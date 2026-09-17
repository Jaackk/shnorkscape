package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cores.CoresManager;
import com.rs.game.Region;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.item.floor.FloorItem;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.network.modern.Native950GameTransport;
import com.rs.network.modern.Native950Isaac;
import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Packets;
import com.rs.network.protocol.modern950.Native950Protocol.ServerPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.ReferenceCountUtil;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.rs.game.WorldObject;
import com.rs.game.hitbar.impl.MiningHitBar;
import com.rs.game.player.actions.mining.Mining;
import com.rs.game.player.actions.mining.defs.RockDefinitions;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.game.route.RouteFinder;
import com.rs.game.route.strategy.ObjectStrategy;
import com.rs.game.player.actions.woodcutting.WoodcuttingDefinitions.TreeDefinitions;
import com.rs.game.player.actions.firemaking.defs.Log;
/** Actual-cache smelting acceptance through existing engine actions, inventory exchange and native XP frames. */
public final class Native950SmeltingAcceptance {
    private static int frames,movementTicks;
    public static void main(String[] args) throws Exception {
        require(args.length==1,"Usage: Native950SmeltingAcceptance <950-flat-cache-directory>");
        require(NativeCacheVerification.isEnforced(),"Cache verification must stay enabled");
        System.setProperty(Native950World.SPAWNS_PROPERTY,"false");
        System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");
        Cache.initFlatReadOnly(Paths.get(args[0]));
        Native950World world=Native950World.getInstance();
        world.execute(() -> {
            require(World.getPlayers().isEmpty()&&World.getNPCs().isEmpty(),"Fresh isolated world required");
            try(Fixture f=new Fixture("smelt-probe",1,3220,3254)) { skills(f); }
            return null;
        }).get(120,TimeUnit.SECONDS);
        require(CoresManager.getNative950Scheduler().failed()==0,"Scheduler recorded a failure");
        System.out.println("PASS: "+movementTicks+" original engine skill ticks, "+frames+" parsed encrypted950 frames; no account save, listener or authentication");
        System.out.println("LIMIT: real client rendering remains a manual check.");
    }
    private static void skills(Fixture f) {
        List<Native950Production.Recipe> rows=Native950Smelting.recipes();
        require(rows.size()==12,"Expected twelve paired ordinary smelting recipes, got "+rows.size());
        int[][] expected={{2349,436,1,438,1},{2351,440,2},{2353,440,1,453,1},{2355,442,1},{2359,447,1,453,1},
                {2361,449,1,44820,1},{2357,444,1},{2363,451,1,44820,1},{44838,44822,1,44824,1},
                {44840,44826,1,44828,1},{44842,21778,2},{44844,2363,1,44830,1,44832,1}};
        for(int i=0;i<rows.size();i++) {
            Native950Production.Recipe row=rows.get(i);Item[] inputs=row.consumed();
            require(row.produced()[0].getId()==expected[i][0]&&inputs.length==(expected[i].length-1)/2,"Wrong product/ingredient count");
            for(int j=0;j<inputs.length;j++)require(inputs[j].getId()==expected[i][1+j*2]&&inputs[j].getAmount()==expected[i][2+j*2],"Wrong actual950 material for "+row.label);
        }
        System.out.println("PASS: all twelve recipe identities, exact current ore quantities and post-rework ingredients");
        WorldObject furnace=findFurnace(f.player);
        int route=RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER,f.player.getX(),f.player.getY(),0,1,new ObjectStrategy(furnace),false);
        require(route>=0&&!RouteFinder.lastIsAlternative(),"No furnace route");
        int[] xs=RouteFinder.getLastPathBufferX(),ys=RouteFinder.getLastPathBufferY();
        for(int i=route-1;i>=0;i--)f.player.addWalkSteps(xs[i],ys[i],128,true);
        for(int i=0;i<100&&f.player.hasWalkSteps();i++)f.tick();f.tick();
        require(Native950Smelting.inReach(f.player,furnace),"Furnace route did not arrive");
        require(!Native950Smelting.start(f.player,furnace,rows.get(0),1),"Empty backpack started smelting");
        for(Item item:rows.get(0).consumed())require(Native950Skilling.giveItem(f.player,item.getId(),item.getAmount()*2),"Missing bronze ore");
        double before=f.player.getSkills().getXp(Skills.SMITHING);
        require(Native950Smelting.start(f.player,furnace,rows.get(0),2),"Bronze smelting did not start");
        f.input.walking();f.player.resetWalkSteps();for(int i=0;i<5;i++)f.tick();
        require(f.inventoryAmount(2349)==0&&f.player.getSkills().getXp(Skills.SMITHING)==before,"Cancelled furnace action granted bar/XP");
        require(Native950Smelting.start(f.player,furnace,rows.get(0),2),"Repeated bronze did not start");
        for(int i=0;i<15;i++)f.tick();
        require(f.inventoryAmount(2349)==2&&f.inventoryAmount(436)==0&&f.inventoryAmount(438)==0,"Bronze did not atomically consume two ingredient pairs");
        require(f.player.getSkills().getXp(Skills.SMITHING)>before&&f.count(ServerPacket.UPDATE_STAT)>0,"Smithing XP pipeline missing");
        for(int i=0;i<28;i++)f.player.getInventory().items.set(i,null);
        for(Item item:rows.get(11).consumed())require(Native950Skilling.giveItem(f.player,item.getId(),item.getAmount()),"Missing elder rune inputs");
        require(!Native950Smelting.start(f.player,furnace,rows.get(11),1),"Low smithing level produced elder rune");
        f.player.getSkills().setXp(Skills.SMITHING,13034431);f.player.getSkills().set(Skills.SMITHING,99);
        for(Native950Production.Recipe row:rows) {
            for(int i=0;i<28;i++)f.player.getInventory().items.set(i,null);
            for(Item item:row.consumed())require(Native950Skilling.giveItem(f.player,item.getId(),item.getAmount()),"Missing furnace ingredient");
            require(Native950Smelting.start(f.player,furnace,row,1),"Furnace refused "+row.label);
            for(int i=0;i<5;i++)f.tick();
            require(f.inventoryAmount(row.produced()[0].getId())==1,"No finished "+row.label);
            for(Item item:row.consumed())require(f.inventoryAmount(item.getId())==0,"Unconsumed ingredient for "+row.label);
        }
        for(int i=0;i<28;i++)f.player.getInventory().items.set(i,null);
        for(Item item:rows.get(0).consumed())require(Native950Skilling.giveItem(f.player,item.getId(),item.getAmount()),"Missing final inputs");
        require(Native950Smelting.start(f.player,furnace,rows.get(0),1),"Final furnace action refused");
        World.removeObject(furnace);f.tick();World.spawnObject(furnace);
        require(f.inventoryAmount(2349)==0&&f.inventoryAmount(436)==1,"Replaced furnace still produced bar");
        System.out.println("PASS: actual furnace collision approach, original ActionManager, cancellation, level/material gates, all12 ore->bar exchanges and XP, replacement cancels work");
    }
    private static WorldObject findFurnace(Player player) {
        for(int regionId:player.getMapRegionsIds()) {
            java.util.Map<Short,WorldObject> objects=World.getRegion(regionId,true).getObjects();if(objects==null)continue;
            for(WorldObject object:objects.values()) {
                if(object.getPlane()!=player.getPlane()||!Native950Smelting.isFurnace(object)||!object.withinDistance(player,30))continue;
                int route=RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER,player.getX(),player.getY(),player.getPlane(),1,new ObjectStrategy(object),false);
                if(route>=0&&!RouteFinder.lastIsAlternative()){System.out.println("Using actual950 furnace "+object.getId()+" at "+object.getX()+","+object.getY());return object;}
            }
        }
        throw new AssertionError("No reachable actual furnace in Lumbridge");
    }
    private static final class Fixture implements AutoCloseable {
        final int[] incoming={9,5,0,2},outgoing={59,55,50,52};
        final Native950Isaac clientCipher=new Native950Isaac(incoming),cipher=new Native950Isaac(outgoing);
        final Native950GameTransport transport=new Native950GameTransport(new Native950Isaac(incoming),new Native950Isaac(outgoing),Thread.currentThread());
        final EmbeddedChannel channel=new EmbeddedChannel(transport);
        final Native950GroundItemsView view=new Native950GroundItemsView(Thread.currentThread(),packet -> channel.write(packet));
        final Native950ObjectsView objects=new Native950ObjectsView(Thread.currentThread(),packet -> channel.write(packet));
        final Native950EntityFrames entities=new Native950EntityFrames();
        final List<Frame> output=new ArrayList<Frame>();final List<FloorItem> created=new ArrayList<FloorItem>();
        final Player player;final Native950Interactions input;
        WorldObject watchedRock;int gaugeUpdates,gaugeRemovals;
        Fixture(String name,int index,int startX,int startY) {
            player=Player.createNative950(name,new WorldTile(startX,startY,0),channel);
            player.setActive(true);player.setRunning(true);Native950World.installVarpSink(player);
            World.addNative950Player(player,index);World.updateEntityRegion(player);player.loadMapRegions();player.setClientHasLoadedMapRegion();
            if(!World.isFloorFree(0,player.getX(),player.getY(),1)) {
                boolean placed=false;
                for(int d=1;d<=5&&!placed;d++)for(int dx=-d;dx<=d&&!placed;dx++)for(int dy=-d;dy<=d&&!placed;dy++) {
                    if(World.isFloorFree(0,startX+dx,startY+dy,1)) {player.setLocation(new WorldTile(startX+dx,startY+dy,0));World.updateEntityRegion(player);placed=true;}
                }
            }
            require(player.getMapSize()==0&&Native950Packets.SCENE_SIZE==256&&World.isFloorFree(0,player.getX(),player.getY(),1),"Fixture needs a clear tile and actual950 scene");
            Native950ItemCatalog catalog=new Native950ItemCatalog(Collections.<Native950ItemCatalog.Entry>emptyList()).withLegacyDrops();
            Native950Content.BankUi bank=new Native950Content.BankUi(517,201,15,317,39,new int[11],new int[11],Collections.emptyList(),Collections.emptyList());
            input=new Native950Interactions(player,channel,new Native950Content(catalog,bank),null,null);
            input.attachGroundItems(view);
            for(int slot=0;slot<28;slot++)player.getInventory().items.set(slot,null);
            input.bootstrap();
            player.getAppearence().generateAppearenceData();
            Native950World.SceneConfig scene=new Native950World.SceneConfig(player.getX(),player.getY(),0,1,7,0,0,0);
            for(Native950Packets.Packet packet:entities.admit(player,scene,Collections.singletonList(player)))channel.write(packet);
            drain();output.clear();
        }
        void tick() {
            movementTicks++;
            int x=player.getX(),y=player.getY(),plane=player.getPlane();
            WorldTasksManager.processTasks();input.beginTick();
            player.processEntity();player.processEntityUpdate();input.afterMovement();
            if(player.getActionManager().getAction() instanceof Mining && player.getNextAnimation()!=null
                    && player.getNextAnimation().getIds()[0]>=0)require(Native950Mining.inReach(player,watchedRock),"Mining animation emitted before collision arrival");
            for(com.rs.game.hitbar.HitBar bar:player.getNextHitBars())if(bar instanceof MiningHitBar) {
                if(((MiningHitBar)bar).isRemoval())gaugeRemovals++;else gaugeUpdates++;
            }
            view.refresh(player);objects.refresh(player);
            entities.beginFrames(Collections.singletonList(player));
            entities.encode(new Native950Frames.Frame(player,channel,Collections.singletonList(player),Collections.emptyList(),null,false,7,x,y,plane));
            channel.write(Native950Packets.tickEnd());drain();player.resetMasks();
        }
        void object(WorldObject object,int option) {
            int[] opcodes={34,48,24,41,73,79};
            ByteBuf b=Unpooled.buffer(10);b.writeByte((opcodes[option-1]+clientCipher.getAsInt())&255);
            b.writeByte(object.getY()+128);b.writeByte(object.getY()>>>8);b.writeInt(object.getId());
            b.writeByte(object.getX()>>>8);b.writeByte(object.getX()+128);b.writeByte(0);send(b);
        }
        void light(int id) {
            int slot=-1;for(int i=0;i<28;i++)if(player.getInventory().items.get(i)!=null&&player.getInventory().items.get(i).getId()==id){slot=i;break;}
            require(slot>=0,"Missing log to light");int hash=(1473<<16)|5;
            ByteBuf b=Unpooled.buffer(10);b.writeByte((122+clientCipher.getAsInt())&255);b.writeMedium(id);
            b.writeByte(hash>>>16);b.writeByte(hash>>>24);b.writeByte(hash);b.writeByte(hash>>>8);b.writeShort(slot);send(b);
        }
        void removeHatchet() {
            int hash=(1462<<16)|31;
            ByteBuf b=Unpooled.buffer(10);b.writeByte((18+clientCipher.getAsInt())&255);b.writeMedium(1351);
            b.writeByte(hash>>>16);b.writeByte(hash>>>24);b.writeByte(hash);b.writeByte(hash>>>8);b.writeShort(3);send(b);
        }
        void useTinderbox(boolean reverse) {
            int source=reverse?1511:590,target=reverse?590:1511,src=-1,dst=-1,hash=(1473<<16)|5;
            for(int i=0;i<28;i++)if(player.getInventory().items.get(i)!=null) {
                if(player.getInventory().items.get(i).getId()==source)src=i;
                if(player.getInventory().items.get(i).getId()==target)dst=i;
            }
            require(src>=0&&dst>=0,"Missing item use endpoint");
            ByteBuf b=Unpooled.buffer(19);b.writeByte((69+clientCipher.getAsInt())&255);
            b.writeByte(src>>>8);b.writeByte(src+128);b.writeByte(source>>>16);b.writeByte(source);b.writeByte(source>>>8);
            b.writeByte(hash>>>8);b.writeByte(hash);b.writeByte(hash>>>24);b.writeByte(hash>>>16);b.writeShort(dst);
            b.writeByte(hash>>>8);b.writeByte(hash);b.writeByte(hash>>>24);b.writeByte(hash>>>16);b.writeMediumLE(target);send(b);
        }
        void send(ByteBuf bytes) {
            channel.writeInbound(bytes);channel.runPendingTasks();
            require(transport.drainActions(input::handle)==1,"Encrypted950 skill click failed to reach the router");
        }
        FloorItem drop(int id,int amount,WorldTile tile,boolean hidden) {
            FloorItem item=World.addGroundItem(new Item(id,amount),tile,player,hidden,-1,2,-1,false);
            require(item.isNative950(),"Native FloorItem lifecycle marker missing");created.add(item);return item;
        }
        void publish(){output.clear();view.refresh(player);channel.write(Native950Packets.tickEnd());drain();}
        void clickTake(int id,WorldTile tile) {
            ByteBuf inputBytes=Unpooled.buffer(9);
            inputBytes.writeByte((22+clientCipher.getAsInt())&255);inputBytes.writeByte(128);
            inputBytes.writeShort(tile.getX());inputBytes.writeShort(tile.getY());inputBytes.writeMedium(id);
            channel.writeInbound(inputBytes);channel.runPendingTasks();
            require(transport.drainActions(input::handle)==1,"Encrypted950 Take did not reach the interaction router");
        }
        void moveUntilPicked(FloorItem item) {
            output.clear();
            for(int i=0;i<20&&present(item);i++) {
                movementTicks++;player.processMovement();input.afterMovement();player.resetMasks();
            }
            drain();require(!present(item),"Take failed to remove actual FloorItem after collision route; position="+player+" target="+item.getTile());
        }
        boolean present(FloorItem item) { return World.getRegion(item.getTile().getRegionId()).getGroundItemsSafe().stream().anyMatch(value -> value==item); }
        int inventoryAmount(int id){int amount=0;for(Item item:player.getInventory().items.getItems())if(item!=null&&item.getId()==id)amount+=item.getAmount();return amount;}
        int count(ServerPacket kind){int n=0;for(Frame frame:output)if(frame.kind==kind)n++;return n;}
        int index(ServerPacket kind){for(int i=0;i<output.size();i++)if(output.get(i).kind==kind)return i;return -1;}
        Frame last(ServerPacket kind){for(int i=output.size()-1;i>=0;i--)if(output.get(i).kind==kind)return output.get(i);return null;}
        boolean hasStat(int skill,int xp){for(Frame frame:output)if(frame.kind==ServerPacket.UPDATE_STAT){byte[] b=frame.body;int value=((b[2]&255)<<24)|((b[3]&255)<<16)|((b[4]&255)<<8)|(b[5]&255);if(((-b[0])&255)==skill&&value==xp)return true;}return false;}
        void drain(){
            channel.flush();channel.runPendingTasks();Object message;
            while((message=channel.readOutbound())!=null)try{
                require(message instanceof ByteBuf,"950 transport did not produce bytes");ByteBuf bytes=(ByteBuf)message;
                while(bytes.isReadable()) {
                    int opcode=(bytes.readUnsignedByte()-cipher.getAsInt())&255;
                    if(opcode>=128)opcode=((opcode-128)<<8)|((bytes.readUnsignedByte()-cipher.getAsInt())&255);
                    ServerPacket kind=null;for(ServerPacket row:ServerPacket.values())if(row.opcode()==opcode){kind=row;break;}
                    require(kind!=null,"Unknown950 reward opcode/cipher mismatch "+opcode);
                    int length=kind.size();if(length==-1)length=bytes.readUnsignedByte();else if(length==-2)length=bytes.readUnsignedShort();
                    require(length>=0&&length<=bytes.readableBytes(),"Invalid950 frame length for "+kind);
                    byte[] body=new byte[length];bytes.readBytes(body);output.add(new Frame(kind,body));frames++;
                }
            }finally{ReferenceCountUtil.release(message);}
            channel.checkException();require(channel.isActive()&&transport.terminalFailure()==null,"950 reward transport disconnected");
        }
        public void close(){for(FloorItem item:created)World.getRegion(item.getTile().getRegionId()).getGroundItemsSafe().removeIf(value -> value==item);input.close();entities.release(player);World.removeNative950Player(player);channel.finishAndReleaseAll();}
    }
    private static byte[] hex(String text){String[] values=text.split(" ");byte[] bytes=new byte[values.length];for(int i=0;i<values.length;i++)bytes[i]=(byte)Integer.parseInt(values[i],16);return bytes;}
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
    private static final class Frame { final ServerPacket kind;final byte[] body;Frame(ServerPacket kind,byte[] body){this.kind=kind;this.body=body;} }
}
