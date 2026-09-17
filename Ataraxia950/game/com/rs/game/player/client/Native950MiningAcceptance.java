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
/** Actual cache mining acceptance: original engine action, encrypted clicks, scenery and gauge updates. */
public final class Native950MiningAcceptance {
    private static int frames,movementTicks;
    public static void main(String[] args) throws Exception {
        require(args.length==1,"Usage: Native950MiningAcceptance <950-flat-cache-directory>");
        require(NativeCacheVerification.isEnforced(),"Cache verification must stay enabled");
        System.setProperty(Native950World.SPAWNS_PROPERTY,"false");
        System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");
        Cache.initFlatReadOnly(Paths.get(args[0]));
        Native950World world=Native950World.getInstance();
        world.execute(() -> {
            require(World.getPlayers().isEmpty()&&World.getNPCs().isEmpty(),"Fresh isolated world required");
            for(boolean running:new boolean[]{false,true}) {
                for(int id:new int[]{113146,113148})
                    try(Fixture f=new Fixture("arr"+id+(running?"r":"w"),1,3239,3155)) {
                        f.player.setRun(running); arrival(f,id,false);
                    }
                try(Fixture f=new Fixture("cancel"+(running?"r":"w"),1,3239,3155)) {
                    f.player.setRun(running); arrival(f,113146,true);
                }
            }
            try(Fixture f=new Fixture("mining-probe",1,3285,3365)) { skills(f); }
            try(Fixture f=new Fixture("iron-probe",1,3182,3370)) { iron(f); }
            try(Fixture f=new Fixture("clay-probe",1,3142,3318)) { clay(f); }
            return null;
        }).get(120,TimeUnit.SECONDS);
        require(CoresManager.getNative950Scheduler().failed()==0,"Scheduler recorded a failure");
        System.out.println("PASS: "+movementTicks+" original engine skill ticks, "+frames+" parsed encrypted950 frames; no account save, listener or authentication");
        System.out.println("LIMIT: real client rendering remains a manual check.");
    }
    private static void arrival(Fixture f,int id,boolean cancelAtArrival) {
        int targetX=id==113146?3229:3230, targetY=id==113146?3148:3147;
        WorldObject rock=null;
        for(WorldObject candidate:World.getRegion(new WorldTile(targetX,targetY,0).getRegionId(),true).getObjects().values())
            if(candidate.getId()==id && candidate.getX()==targetX && candidate.getY()==targetY && candidate.getPlane()==0) rock=candidate;
        require(rock!=null,"Missing user's actual Lumbridge copper rock "+id);
        require(Native950Skilling.giveItem(f.player,1265,1),"Arrival fixture pickaxe missing");
        f.watchedRock=rock;f.tick();
        double before=f.player.getSkills().getXp(Skills.MINING);
        f.object(rock,1);
        require(f.player.hasWalkSteps(),"Arrival fixture must approach from several tiles away");
        int steps=0;
        while(f.player.hasWalkSteps() && steps++<100) f.tick();
        require(steps>1 && steps<100 && Native950Mining.inReach(f.player,rock),"Approach failed to reach actual rock edge");
        require(!(f.player.getActionManager().getAction() instanceof Mining) && f.gaugeUpdates==0,
                "Mining started in the final movement frame before the client could finish arriving");
        if(cancelAtArrival) {
            f.input.walking();f.player.resetWalkSteps();
            for(int i=0;i<12;i++)f.tick();
            require(!(f.player.getActionManager().getAction() instanceof Mining) && f.gaugeUpdates==0
                    && f.inventoryAmount(436)==0 && f.player.getSkills().getXp(Skills.MINING)==before,
                    "Cancelling between arrival and start still started mining or granted ore/XP");
            System.out.println("PASS: cancellation after final arrival frame, running="+f.player.getRun());
            return;
        }
        f.tick();
        require(f.player.getActionManager().getAction() instanceof Mining && f.gaugeUpdates==1 && f.staminaUpdates==0,
                "Stationary level1 arrival must show ore progress only; stamina unlocks at15");
        f.input.walking();f.player.resetWalkSteps();f.tick();
        int gauges=f.gaugeUpdates;
        f.object(rock,1);f.tick();
        require(f.player.getActionManager().getAction() instanceof Mining && f.gaugeUpdates==gauges+1 && f.staminaUpdates==0,
                "Already adjacent click must start without an extra approach delay");
        f.input.walking();f.player.resetWalkSteps();f.tick();
        System.out.println("PASS: rock "+id+", running="+f.player.getRun()+", "+steps
                +" approach ticks -> final movement without action/gauges -> stationary mining -> adjacent restart");
    }
    private static void skills(Fixture f) {
        require(Native950Hitbars.verifyCache(), "Paired mining gauges/sprites missing");
        for(Native950Mining.PickaxeDef pick:Native950Mining.PICKAXES) {
            require(Native950Mining.itemEntry(pick.itemId)!=null, "950 pickaxe metadata missing "+pick.itemId);
            require(Native950Mining.animation(pick)>=0, "950 pickaxe hand binding missing "+pick.itemId);
        }
        for(int stale:new int[]{1269,1271,1273,1275})require(Native950Mining.itemEntry(stale)==null,"Obsolete pickaxe admitted "+stale);
        System.out.println("PASS: all 13 current pickaxes, held-tool sequence identities, paired gauge/sprite bindings");
        f.player.getSkills().setLevelWithoutRefresh(Skills.MINING,14);
        Native950Mining.progress(f.player,100,1,100);
        for(com.rs.game.hitbar.HitBar bar:f.player.getNextHitBars())if(bar.getType()==7)
            require(((MiningHitBar)bar).isRemoval(),"Stamina appeared before level15 unlock");
        f.player.resetMasks();
        f.player.getSkills().setXpWithoutRefresh(Skills.MINING,Skills.getXPForLevel(Skills.MINING,15));
        f.player.getSkills().setLevelWithoutRefresh(Skills.MINING,15);
        WorldObject rock=findRock(f.player,RockDefinitions.Copper_Ore);
        f.watchedRock=rock;
        double before=f.player.getSkills().getXp(Skills.MINING);
        f.object(rock,1);
        for(int i=0;i<100&&(f.player.hasWalkSteps()||i==0);i++)f.tick();
        f.tick();require(!f.player.getActionManager().hasSkillWorking()&&f.inventoryAmount(436)==0,"No-pickaxe rock yielded ore");
        require(Native950Skilling.giveItem(f.player,1265,1),"Bronze pickaxe grant failed");
        f.tick();f.tick();
        f.light(1265);f.tick();f.tick();
        require(f.player.getEquipment().getWeaponId()==1265,"Current bronze pickaxe did not wield");
        f.object(rock,1);f.tick();
        require(f.player.getActionManager().getAction() instanceof Mining,"Copper Mine did not start original action");
        require(f.gaugeUpdates>=2,"Starting mining omitted the two native gauges");
        f.input.walking();f.player.resetWalkSteps();f.tick();
        require(f.gaugeRemovals>=2,"Walking did not explicitly remove both mining gauges");
        for(int i=0;i<12;i++)f.tick();
        require(f.inventoryAmount(436)==0&&f.player.getSkills().getXp(Skills.MINING)==before,"Cancelled mining granted ore/XP");
        f.object(rock,1);
        for(int i=0;i<300&&f.inventoryAmount(436)<2;i++)f.tick();
        require(f.inventoryAmount(436)>=2&&f.player.getSkills().getXp(Skills.MINING)>before,"Original damage loop did not produce copper/XP");
        require(Native950Mining.current(rock),"Core ore rock unexpectedly depleted");
        require(f.count(ServerPacket.UPDATE_STAT)>0,"Mining XP stat packet missing");
        require(f.gaugeUpdates>4,"Swing gauge updates missing");
        f.input.walking();f.player.resetWalkSteps();f.tick();
        System.out.println("PASS: encrypted Mine -> collision approach -> tool refusal -> Wield -> original action -> two gauges -> cancellation -> copper and XP; core rock remains");
        for(int i=0;i<28;i++)if(f.player.getInventory().items.get(i)==null)f.player.getInventory().items.set(i,new Item(1511,1));
        require(!Native950Mining.start(f.player,rock),"Full inventory started mining");
        for(int i=0;i<28;i++)f.player.getInventory().items.set(i,null);
        WorldTile near=new WorldTile(f.player);
        f.player.setLocation(new WorldTile(near.getX()+20,near.getY()+20,near.getPlane()));
        f.player.resetWalkSteps();f.player.resetMasks();
        require(!Native950Mining.start(f.player,rock),"Distant direct mining started");
        require(f.player.getNextAnimation()==null||f.player.getNextAnimation().getIds()[0]<0,"Distant start emitted animation");
        f.player.setLocation(near);f.player.resetMasks();
        System.out.println("PASS: full-bag and distant-start animation gates");
    }
    private static void iron(Fixture f) {
        require(Native950Skilling.giveItem(f.player,1265,1),"Clay fixture tool missing");f.tick();
        WorldObject iron=findRock(f.player,RockDefinitions.Iron_Ore);
        f.watchedRock=iron;f.object(iron,1);
        for(int i=0;i<100&&(f.player.hasWalkSteps()||i==0);i++)f.tick();f.tick();
        require(!f.player.getActionManager().hasSkillWorking()&&f.inventoryAmount(440)==0,"Low-level iron mining accepted");
    }
    private static void clay(Fixture f) {
        require(Native950Skilling.giveItem(f.player,1265,1),"Clay fixture tool missing");f.tick();
        f.player.getSkills().setXp(Skills.MINING,13034431);f.player.getSkills().set(Skills.MINING,99);
        WorldObject clay=findRock(f.player,RockDefinitions.CLAY);
        f.watchedRock=clay;f.object(clay,1);
        for(int i=0;i<100&&(f.player.hasWalkSteps()||i==0);i++)f.tick();
        for(int i=0;i<300&&Native950Mining.current(clay);i++)f.tick();
        require(f.inventoryAmount(434)>0&&!Native950Mining.current(clay),"Original clay resource did not yield/deplete");
        require(f.count(ServerPacket.LOC_ADD_CHANGE)>0,"Depleted rock did not reach native scenery frames");
        require(!Native950Mining.start(f.player,clay),"Stale depleted rock accepted");
        for(int i=0;i<30&&!Native950Mining.current(clay);i++)f.tick();
        require(Native950Mining.current(clay),"Original clay respawn did not restore effective map object");
        Native950Save saved=Native950PlayerBinder.capture(f.player,f.input.saveSnapshot(),System.currentTimeMillis());
        require(saved.skills().xp(Skills.MINING)==f.player.getSkills().getXp(Skills.MINING),"Mining XP missing from existing save capture");
        System.out.println("PASS: full-bag and level gates, distant-start animation refusal, clay yield -> paired empty rock -> timer regrowth -> save capture");
    }
    private static WorldObject findRock(Player player,RockDefinitions desired) {
        WorldObject best=null;int bestDistance=Integer.MAX_VALUE;
        for(int regionId:player.getMapRegionsIds()) {
            java.util.Map<Short,WorldObject> objects=World.getRegion(regionId,true).getObjects();
            if(objects==null)continue;
            for(WorldObject rock:objects.values()) {
                if(rock.getPlane()!=player.getPlane()||Native950Mining.definition(rock)!=desired)continue;
                int distance=Math.max(Math.abs(rock.getX()-player.getX()),Math.abs(rock.getY()-player.getY()));
                if(distance>=bestDistance||distance>60)continue;
                int route=RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER,player.getX(),player.getY(),player.getPlane(),1,new ObjectStrategy(rock),false);
                if(route<0||RouteFinder.lastIsAlternative())continue;
                best=rock;bestDistance=distance;
            }
        }
        require(best!=null,"No reachable actual950 "+desired+" near mining fixture");
        System.out.println("Using actual950 rock "+best.getId()+" shape="+best.getType()+" at "+best.getX()+","+best.getY());return best;
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
        WorldObject watchedRock;int gaugeUpdates,gaugeRemovals,staminaUpdates;
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
                    && player.getNextAnimation().getIds()[0]>=0) {
                require(Native950Mining.inReach(player,watchedRock),"Mining animation emitted before collision arrival");
                require(player.getX()==x && player.getY()==y && player.getPlane()==plane,
                        "Mining animation shares the final movement frame and can block client arrival");
            }
            for(com.rs.game.hitbar.HitBar bar:player.getNextHitBars())if(bar instanceof MiningHitBar) {
                if(((MiningHitBar)bar).isRemoval())gaugeRemovals++;else {gaugeUpdates++;if(bar.getType()==7)staminaUpdates++;}
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
