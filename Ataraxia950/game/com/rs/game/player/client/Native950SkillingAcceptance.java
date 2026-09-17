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
import com.rs.game.tasks.WorldTasksManager;
import com.rs.game.route.RouteFinder;
import com.rs.game.route.strategy.ObjectStrategy;
import com.rs.game.player.actions.woodcutting.WoodcuttingDefinitions.TreeDefinitions;
import com.rs.game.player.actions.firemaking.defs.Log;
/** Full ordinary skill path using paired950 cache, original engine ticks and encrypted local transport. */
public final class Native950SkillingAcceptance {
    private static int frames,movementTicks;
    public static void main(String[] args) throws Exception {
        require(args.length==1,"Usage: Native950SkillingAcceptance <950-flat-cache-directory>");
        require(NativeCacheVerification.isEnforced(),"Cache verification must stay enabled");
        System.setProperty(Native950World.SPAWNS_PROPERTY,"false");
        System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");
        Cache.initFlatReadOnly(Paths.get(args[0]));
        Native950World world=Native950World.getInstance();
        world.execute(() -> {
            require(World.getPlayers().isEmpty()&&World.getNPCs().isEmpty(),"Fresh isolated world required");
            try(Fixture f=new Fixture("skill-probe",1)) { skills(f); }
            return null;
        }).get(120,TimeUnit.SECONDS);
        require(CoresManager.getNative950Scheduler().failed()==0,"Scheduler recorded a failure");
        System.out.println("PASS: "+movementTicks+" original engine skill ticks, "+frames+" parsed encrypted950 frames; no account save, listener or authentication");
        System.out.println("LIMIT: real client rendering remains a manual check.");
    }
    private static void skills(Fixture f) {
        for(int id:new int[]{1351,1349,1353,1361,1355,1357,1359,6739,13661,32645,1511,1521,1519,1517,1515,1513,6332,6333,29556})
            require(Native950Woodcutting.itemEntry(id)!=null,"Missing actual950 woodcutting item "+id);
        for(Log log:Log.values())if(log!=Log.PROTEAN&&log!=Log.EVIL_BARK)
            require(Native950Firemaking.isSupportedLog(log.getLogId()),"Missing actual950 firemaking log "+log);
        require(Native950Firemaking.itemEntry(590)!=null&&Native950Firemaking.itemEntry(592)!=null,"Tinderbox/ashes identity missing");
        System.out.println("PASS: ten actual950 hatchets, ordinary wood/fire logs, tinderbox, ashes, fire and animation cache bindings");
        WorldObject tree=findTree(f.player);
        double woodBefore=f.player.getSkills().getXp(Skills.WOODCUTTING);
        f.object(tree,1);
        for(int i=0;i<100&&(f.player.hasWalkSteps()||i==0);i++)f.tick();
        f.tick();
        require(!f.player.getActionManager().hasSkillWorking()&&f.inventoryAmount(1511)==0,"No-tool tree yielded a log");
        require(Native950Skilling.giveItem(f.player,1351,1)&&Native950Skilling.giveItem(f.player,590,1),"Tool grant failed");
        f.tick();f.tick();long withheld=f.player.getAppearence().getNative950WithheldBodies();
        byte[] unarmed=f.player.getAppearence().getMD5AppeareanceDataHash().clone();
        f.light(1351);f.tick();f.tick();
        require(f.player.getEquipment().getWeaponId()==1351&&f.inventoryAmount(1351)==0,"Original Wield did not equip the bronze hatchet");
        require(f.player.getAppearence().getNative950WithheldBodies()==withheld
                &&!Arrays.equals(unarmed,f.player.getAppearence().getMD5AppeareanceDataHash()),"Hatchet appearance was withheld or unchanged");
        f.removeHatchet();f.tick();
        require(f.player.getEquipment().getWeaponId()==-1&&f.inventoryAmount(1351)==1,"Original Remove did not return the hatchet to backpack");
        System.out.println("PASS: encryptedbronzehatchet Wield -> originalequipment -> actual950appearance -> encryptedRemove -> backpack");
        f.object(tree,1);f.tick();
        require(f.player.getActionManager().hasSkillWorking(),"Tree route did not start original Woodcutting action");
        f.input.walking();f.player.resetWalkSteps();
        for(int i=0;i<12;i++)f.tick();
        require(f.inventoryAmount(1511)==0&&f.player.getSkills().getXp(Skills.WOODCUTTING)==woodBefore,"Walking cancellation yielded logs/XP");
        f.object(tree,1);
        for(int i=0;i<1800&&Native950Woodcutting.current(tree);i++) {
            f.tick();
            if(f.inventoryAmount(1511)>15)require(Native950Skilling.consumeItem(f.player,1511,10),"Probe inventory cleanup failed");
        }
        require(!Native950Woodcutting.current(tree)&&f.inventoryAmount(1511)>0,"Original Woodcutting never yielded and depleted");
        require(f.player.getSkills().getXp(Skills.WOODCUTTING)>woodBefore&&f.count(ServerPacket.UPDATE_STAT)>0,"Woodcutting XP/UPDATE_STAT missing");
        require(f.count(ServerPacket.LOC_ADD_CHANGE)>0,"Tree stump native object frame missing");
        require(!Native950Woodcutting.start(f.player,tree),"Stale depleted tree accepted");
        for(int i=0;i<100&&!Native950Woodcutting.current(tree);i++)f.tick();
        require(Native950Woodcutting.current(tree),"Original tree respawn timer did not restore the effective object");
        require(f.objects.publishedObjects()==0,"Restored tree left a stale native object override");
        System.out.println("PASS: encryptedChop -> actual collision approach -> no-tool refusal -> walk cancellation -> original ActionManager logs+XP -> native stump -> original respawn");
        for(int i=0;i<28;i++)if(f.player.getInventory().items.get(i)==null)f.player.getInventory().items.set(i,new Item(1511,1));
        require(!Native950Woodcutting.start(f.player,tree),"Full backpack started woodcutting");
        for(int i=0;i<28;i++)if(f.player.getInventory().items.get(i)!=null&&f.player.getInventory().items.get(i).getId()==1511)f.player.getInventory().items.set(i,null);
        require(Native950Skilling.giveItem(f.player,1511,4),"Four log grant failed");
        f.tick();f.output.clear();
        double fireBefore=f.player.getSkills().getXp(Skills.FIREMAKING);
        WorldTile origin=new WorldTile(f.player);
        f.light(1511);
        for(int i=0;i<12&&World.getObjectWithSlot(origin,Region.OBJECT_SLOT_FLOOR)==null;i++)f.tick();
        WorldObject fire=World.getObjectWithSlot(origin,Region.OBJECT_SLOT_FLOOR);
        require(Native950Firemaking.isFireObject(fire)&&f.inventoryAmount(1511)==3,"Light failed to consume exactly one log and create fire");
        require(f.player.getSkills().getXp(Skills.FIREMAKING)>fireBefore&&f.count(ServerPacket.LOC_ADD_CHANGE)>0,"Fire XP or native object appearance missing");
        f.object(fire,5);
        for(int i=0;i<40&&f.inventoryAmount(1511)>0;i++)f.tick();
        require(f.inventoryAmount(1511)==0&&f.player.getSkills().getXp(Skills.FIREMAKING)>fireBefore+140,"Bonfire Use did not repeatedly consume logs and award original XP");
        f.tick();f.output.clear();
        for(int i=0;i<Log.NORMAL.getLife()+10&&World.getObjectWithSlot(origin,Region.OBJECT_SLOT_FLOOR)==fire;i++)f.tick();
        require(World.getObjectWithSlot(origin,Region.OBJECT_SLOT_FLOOR)==null&&f.count(ServerPacket.LOC_DEL)>0,"Fire did not expire on original timer/native remove");
        FloorItem ashes=null;for(FloorItem item:World.getRegion(origin.getRegionId()).getGroundItemsSafe())
            if(item.getId()==592&&item.getTile().matches(origin))ashes=item;
        require(ashes!=null&&ashes.isNative950()&&f.view.canTake(f.player,592,origin.getX(),origin.getY()),"Expired fire ashes not published/pickable");
        f.created.add(ashes);f.clickTake(592,origin);f.moveUntilPicked(ashes);
        require(f.inventoryAmount(592)==1,"Ash pickup did not reach original inventory");
        require(Native950Skilling.giveItem(f.player,1511,2),"Item-use logs unavailable");
        for(boolean reverse:new boolean[]{false,true}) {
            f.tick();f.tick();WorldTile useTile=new WorldTile(f.player);int before=f.inventoryAmount(1511);
            f.useTinderbox(reverse);
            for(int i=0;i<12&&World.getObjectWithSlot(useTile,Region.OBJECT_SLOT_FLOOR)==null;i++)f.tick();
            WorldObject useFire=World.getObjectWithSlot(useTile,Region.OBJECT_SLOT_FLOOR);
            require(Native950Firemaking.isFireObject(useFire)&&f.inventoryAmount(1511)==before-1,
                    "Encrypted tinderbox/log use failed, reverse="+reverse);
            World.removeObject(useFire);
        }
        System.out.println("PASS: encrypted item-on-item69 ignites logs in both source/target orders without inventory dragging");
        Native950Save saved=Native950PlayerBinder.capture(f.player,f.input.saveSnapshot(),System.currentTimeMillis());
        require(saved.skills().xp(Skills.WOODCUTTING)==f.player.getSkills().getXp(Skills.WOODCUTTING)
                && saved.skills().xp(Skills.FIREMAKING)==f.player.getSkills().getXp(Skills.FIREMAKING),
                "Skilling XP did not reach the existing character save snapshot");
        System.out.println("PASS: full-bag refusal -> encryptedLight -> one owned log -> step aside -> native fire+XP -> encryptedfireUse -> repeated bonfire logs+XP -> original expiry -> native ashes Take -> save snapshot");
    }
    private static WorldObject findTree(Player player) {
        WorldObject best=null;int bestDistance=Integer.MAX_VALUE;
        for(int regionId:player.getMapRegionsIds()) {
            java.util.Map<Short,WorldObject> objects=World.getRegion(regionId,true).getObjects();
            if(objects==null)continue;
            for(WorldObject tree:objects.values()) {
                TreeDefinitions type=Native950Woodcutting.definition(tree);
                if(tree.getPlane()!=player.getPlane()||(type!=TreeDefinitions.NORMAL&&type!=TreeDefinitions.EVERGREEN&&type!=TreeDefinitions.DEAD))continue;
                int distance=Math.max(Math.abs(tree.getX()-player.getX()),Math.abs(tree.getY()-player.getY()));
                if(distance>=bestDistance||distance>40)continue;
                int route=RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER,player.getX(),player.getY(),player.getPlane(),1,new ObjectStrategy(tree),false);
                if(route<0||RouteFinder.lastIsAlternative())continue;
                best=tree;bestDistance=distance;
            }
        }
        require(best!=null,"No reachable ordinary actual950 tree near Lumbridge banker");
        System.out.println("Using actual950 tree "+best.getId()+" at "+best.getX()+","+best.getY());return best;
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
        Fixture(String name,int index) {
            player=Player.createNative950(name,new WorldTile(3217,3258,0),channel);
            player.setActive(true);player.setRunning(true);Native950World.installVarpSink(player);
            World.addNative950Player(player,index);World.updateEntityRegion(player);player.loadMapRegions();player.setClientHasLoadedMapRegion();
            require(player.getMapSize()==0&&Native950Packets.SCENE_SIZE==256&&World.isFloorFree(0,3217,3258,1),"Fixture requires server104 interest, actual native256-tile wire scene and clear banker tile");
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
