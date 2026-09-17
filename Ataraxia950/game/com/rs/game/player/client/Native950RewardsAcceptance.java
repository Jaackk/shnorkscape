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

/** Actual paired-cache reward transport/route proof in a fresh ephemeral owner-thread world. */
public final class Native950RewardsAcceptance {
    private static int frames, movementTicks;
    private Native950RewardsAcceptance() { }
    public static void main(String[] args) throws Exception {
        require(args.length==1,"Usage: Native950RewardsAcceptance <950-flat-cache-directory>");
        require(NativeCacheVerification.isEnforced(),"Cache verification must stay enabled");
        System.setProperty(Native950World.SPAWNS_PROPERTY,"false");
        System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");
        Cache.initFlatReadOnly(Paths.get(args[0]));
        Native950World world=Native950World.getInstance();
        world.execute(() -> {
            require(World.getPlayers().isEmpty()&&World.getNPCs().isEmpty(),"Fresh isolated world required");
            metadata();
            try(Fixture owner=new Fixture("reward-owner",1); Fixture other=new Fixture("reward-other",2)) {
                dropsAndTake(owner,other);
                inventoryDropAndTake(owner,other);
                experience(owner);
            }
            require(World.getPlayers().isEmpty(),"Ephemeral players were not cleaned up");
            return null;
        }).get(90,TimeUnit.SECONDS);
        require(CoresManager.getNative950Scheduler().failed()==0,"World scheduler recorded a failure");
        System.out.println("PASS: "+frames+" parsed encrypted950 reward frames; "+movementTicks+" actual collision/path pickup ticks; no disconnected transport");
        System.out.println("LIMIT: validates cache, authoritative state and wire composition; rendering and popup placement require the real client. No login, save, network listener or authenticated account was used.");
    }
    private static void metadata() {
        int[] ids={526,2138,314,995};boolean[] stackable={false,false,true,true};
        for(int i=0;i<ids.length;i++) {
            Native950NpcDrops.ItemMetadata item=Native950NpcDrops.metadata(ids[i]);
            require(item!=null&&item.id==ids[i]&&item.stackable==stackable[i]&&!item.noted,"Unexpected actual950 loot metadata "+ids[i]);
            System.out.println("PASS: actual950 loot metadata "+item.id+" "+item.name+" stackable="+item.stackable);
        }
        Native950NpcDrops.ItemMetadata note=Native950NpcDrops.metadata(527);
        if(note!=null)require(note.noted&&note.stackable&&note.baseId==526,"Bones certificate identity changed");
        System.out.println(note==null?"LIMIT: noted bones527 refused by identity/certificate admission":"PASS: actual950 noted bones527 maps reciprocally to bones526 and stays stackable");
    }
    private static void dropsAndTake(Fixture owner,Fixture other) {
        WorldTile tile=new WorldTile(owner.player);
        FloorItem coins=owner.drop(995,10,tile,true);
        owner.publish(); other.publish();
        require(owner.count(ServerPacket.OBJ_ADD)==1&&other.count(ServerPacket.OBJ_ADD)==0,"Private owner visibility failed");
        require(Arrays.equals(owner.last(ServerPacket.OBJ_ADD).body,hex("e3 03 00 6e 00 0a")),"Coin add literal differs from native950 witness");
        require(Arrays.equals(owner.last(ServerPacket.UPDATE_ZONE_PARTIAL_FOLLOWS).body,hex("10 f0 80")),"Actual256-tile scene origin differs from native proof");
        coins.setAmount(30);owner.publish();
        require(owner.count(ServerPacket.OBJ_COUNT)==1&&owner.count(ServerPacket.OBJ_ADD)==0,"Quantity change re-added a duplicate pile");
        require(Arrays.equals(owner.last(ServerPacket.OBJ_COUNT).body,hex("12 00 03 e3 00 0a 00 1e")),"Coin count literal differs from native950 witness");
        require(!other.view.canTake(other.player,995,tile.getX(),tile.getY()),"Foreign private pile was admitted");
        coins.setInvisible(false);other.publish();
        require(other.count(ServerPacket.OBJ_ADD)==1,"Public transition missed the second viewer");
        try(Fixture late=new Fixture("reward-late",3)) {
            late.publish();require(late.count(ServerPacket.OBJ_ADD)==1,"Late join did not publish existing public loot");
        }
        owner.output.clear();owner.view.beforeRebuild();
        owner.channel.write(Native950Packets.rebuildScene(owner.player.getChunkX(),owner.player.getChunkY(),7,0,0,0));
        owner.view.refresh(owner.player);owner.drain();
        require(owner.index(ServerPacket.OBJ_DEL)<owner.index(ServerPacket.REBUILD_NORMAL)
                &&owner.index(ServerPacket.REBUILD_NORMAL)<owner.index(ServerPacket.OBJ_ADD),"Rebuild must retire old piles before republishing overlap");
        int before=owner.inventoryAmount(995);
        owner.clickTake(995,tile);
        owner.moveUntilPicked(coins);
        require(owner.inventoryAmount(995)==before+30&&owner.count(ServerPacket.UPDATE_INV_FULL)>0,"Take did not commit and refresh the original inventory");
        owner.publish();other.publish();
        require(owner.count(ServerPacket.OBJ_DEL)==1&&other.count(ServerPacket.OBJ_DEL)==1,"Taken public pile survived for a viewer");
        require(!owner.view.canTake(owner.player,995,tile.getX(),tile.getY()),"Removed pile stayed in published pickup admission");
        System.out.println("PASS: real Region/FloorItem private/public/latejoin -> quantity -> ordered rebuild -> encrypted Take -> inventory -> both-viewer removal");

        WorldTile target=reachableTile(owner.player,4);
        FloorItem bones=owner.drop(526,1,target,true);
        owner.publish();
        require(owner.view.canTake(owner.player,526,target.getX(),target.getY()),"Remote bones were not published");
        owner.clickTake(526,target);
        require(owner.player.hasWalkSteps(),"Remote Take did not install the original routefinder path");
        owner.moveUntilPicked(bones);
        require(owner.player.matches(target)&&owner.inventoryAmount(526)==1,"Actual collision route did not reach and collect bones");
        owner.publish();require(owner.count(ServerPacket.OBJ_DEL)==1,"Routed bones pickup did not remove the pile");
        System.out.println("PASS: actual950 path to bones at"+target.getX()+","+target.getY()+" completed authoritative ground pickup");
    }
    private static void inventoryDropAndTake(Fixture owner,Fixture other) {
        owner.clearInventory();
        Item first=new Item(526,1),selected=new Item(526,1);
        owner.player.getInventory().items.set(2,first);
        owner.player.getInventory().items.set(17,selected);
        owner.prepareInventoryTick();
        require("Drop".equalsIgnoreCase(Native950CacheItems.entry(526).option(5)),"Actual950 bones no longer have ordinary Drop");
        owner.buttonAt(17,526,5);
        require(owner.player.getInventory().items.get(17)==selected,"Obsolete UI option5 consumed the item");
        require(owner.inventoryGroundCount()==0,"Obsolete UI option5 published a floor item");
        owner.buttonAt(17,526,8);
        require(owner.player.getInventory().items.get(2)==first&&owner.player.getInventory().items.get(17)==null,
                "Encrypted IF_BUTTON8 did not remove only the selected duplicate nonstackable slot");
        require(owner.count(ServerPacket.UPDATE_INV_FULL)>0,"Inventory Drop did not publish the authoritative backpack");
        FloorItem bones=owner.recordInventoryDrop(526);
        require(bones.getAmount()==1&&bones.isInvisible()&&bones.getOwner().equals(owner.player.getUsername()),
                "Inventory Drop did not create one owner-private ordinary floor item");
        owner.publish();other.publish();
        require(owner.count(ServerPacket.OBJ_ADD)==1&&other.count(ServerPacket.OBJ_ADD)==0,
                "Inventory Drop OBJ_ADD was not private to the owner");
        require(!other.view.canTake(other.player,526,bones.getTile().getX(),bones.getTile().getY()),
                "Another viewer can take a private inventory drop");
        // Same-tick stale duplicate: another action has put an equal-ID item in that slot.
        // The exact client ID now matches again, so this exercises changed-slot admission.
        owner.player.getInventory().items.set(2,null);
        owner.player.getInventory().items.set(17,first);
        owner.buttonAt(17,526,8);
        require(owner.player.getInventory().items.get(17)==first&&owner.inventoryGroundCount()==1,
                "A duplicate same-tick Drop consumed the replacement in the selected slot");
        owner.input.beginTick();
        owner.clickTake(526,bones.getTile());owner.moveUntilPicked(bones);
        require(owner.inventoryAmount(526)==2,"Taking inventory-dropped bones did not restore the original total");
        owner.publish();other.publish();
        require(owner.count(ServerPacket.OBJ_DEL)==1&&other.count(ServerPacket.OBJ_DEL)==0,
                "Private inventory drop did not disappear from only the owner's view");

        for(int id:new int[]{995,527}) {
            owner.clearInventory();
            Native950ItemCatalog.Entry current=Native950CacheItems.entry(id);
            require(current!=null&&current.stackable&&"Drop".equalsIgnoreCase(current.option(5)),
                    "Actual950 stack/note Drop metadata missing for "+id);
            owner.player.getInventory().items.set(9,new Item(id,12345));
            owner.prepareInventoryTick();owner.buttonAt(9,id,8);
            require(owner.player.getInventory().items.get(9)==null,"Drop retained some of stack "+id);
            FloorItem stack=owner.recordInventoryDrop(id);
            require(stack.getId()==id&&stack.getAmount()==12345,"Drop changed stack/note identity or quantity "+id);
            require(owner.count(ServerPacket.UPDATE_INV_FULL)>0,"Stack Drop omitted backpack publication");
            owner.publish();other.publish();
            require(owner.count(ServerPacket.OBJ_ADD)==1&&other.count(ServerPacket.OBJ_ADD)==0,
                    "Stack/note Drop was not published owner-private "+id);
            owner.input.beginTick();owner.clickTake(id,stack.getTile());owner.moveUntilPicked(stack);
            require(owner.inventoryAmount(id)==12345,"Pickup failed to restore entire stack/note "+id);
            int occupied=0;for(Item held:owner.player.getInventory().items.getItems())if(held!=null)occupied++;
            require(occupied==1,"Noted or ordinary stack split across slots on pickup "+id);
            owner.publish();other.publish();
            require(owner.count(ServerPacket.OBJ_DEL)==1,"Stack/note pickup left a visible ground item "+id);
        }

        owner.clearInventory();
        Native950ItemCatalog.Entry destroy=Native950CacheItems.entry(2677);
        require(destroy!=null&&"Destroy".equalsIgnoreCase(destroy.option(5)),"Actual950 easy clue scroll Destroy fixture changed");
        Item clue=new Item(2677,1);owner.player.getInventory().items.set(6,clue);
        owner.prepareInventoryTick();owner.buttonAt(6,2677,8);
        require(owner.player.getInventory().items.get(6)==clue&&owner.inventoryGroundCount()==0,
                "Unsupported Destroy was silently treated as Drop");
        owner.clearInventory();owner.prepareInventoryTick();
        System.out.println("PASS: actual950 encrypted IF_BUTTON8(opcode66) -> exact duplicate slot/whole coin+note stacks -> private OBJ_ADD -> encrypted pickup; stale same-tick duplicate and obsolete UI5 cannot drop, Destroy preserves item");
    }

    private static void experience(Fixture owner) {
        require(Native950XpDrops.verifyCache(),"Paired950 XP renderer cache pins failed");
        owner.player.getSkills().init();owner.drain();owner.output.clear();
        Native950XpDrops.open(owner.player);owner.drain();
        Frame attachment=owner.last(ServerPacket.IF_OPENSUB);
        require(attachment!=null&&attachment.body.length==23,"Native XP renderer was not attached");
        byte[] b=attachment.body;
        int parent=((b[0]&255)<<24)|((b[1]&255)<<16)|((b[2]&255)<<8)|(b[3]&255);
        int panel=((b[17]&255)<<8)|((b[16]-128)&255);
        require(parent==((1477<<16)|668)&&panel==1213,"Wrong XP renderer interface/parent");
        owner.output.clear();
        NPC target=NPC.createNative950(12353,new WorldTile(owner.player.getX()+1,owner.player.getY(),0),1);
        double[] before=owner.player.getSkills().getXpCopy();
        Native950CombatExperience.awardMelee(owner.player,target,10);owner.drain();
        for(int skill:new int[]{Skills.ATTACK,Skills.STRENGTH,Skills.DEFENCE,Skills.HITPOINTS}) {
            require(owner.player.getSkills().getXp(skill)>before[skill],"Original Skills did not award combat XP to "+skill);
            require(owner.hasStat(skill,(int)owner.player.getSkills().getXp(skill)),"Native UPDATE_STAT did not carry authoritative skillXP "+skill);
        }
        require(owner.count(ServerPacket.UPDATE_STAT)==4,"Expected one UPDATE_STAT for each selected melee skill and Constitution");
        require(owner.count(ServerPacket.RUNCLIENTSCRIPT)==0,"XP award unexpectedly used an explicit guessed client script");
        System.out.println("PASS: cache-pinned1213 XP renderer attaches to1477:668; original CombatDefinitions/Skills award emits four authoritative encrypted UPDATE_STAT frames");
    }
    private static WorldTile reachableTile(Player player,int distance) {
        List<WorldTile> queue=new ArrayList<WorldTile>();queue.add(new WorldTile(player));
        java.util.Set<Integer> seen=new java.util.HashSet<Integer>();seen.add(player.getTileHash());
        for(int i=0;i<queue.size();i++) {
            WorldTile tile=queue.get(i);
            if(Math.abs(tile.getX()-player.getX())+Math.abs(tile.getY()-player.getY())==distance)return tile;
            for(int[] d:new int[][]{{1,0},{0,1},{-1,0},{0,-1}}) {
                WorldTile next=new WorldTile(tile.getX()+d[0],tile.getY()+d[1],tile.getPlane());
                if(seen.contains(next.getTileHash())||!World.isFloorFree(next.getPlane(),next.getX(),next.getY(),1)
                        ||!World.checkWalkStep(tile.getPlane(),tile.getX(),tile.getY(),d[0],d[1],1))continue;
                seen.add(next.getTileHash());queue.add(next);
            }
        }
        throw new AssertionError("No clear pickup route in actual950 cache");
    }
    private static final class Fixture implements AutoCloseable {
        final int[] incoming={9,5,0,2},outgoing={59,55,50,52};
        final Native950Isaac clientCipher=new Native950Isaac(incoming),cipher=new Native950Isaac(outgoing);
        final Native950GameTransport transport=new Native950GameTransport(new Native950Isaac(incoming),new Native950Isaac(outgoing),Thread.currentThread());
        final EmbeddedChannel channel=new EmbeddedChannel(transport);
        final Native950GroundItemsView view=new Native950GroundItemsView(Thread.currentThread(),packet -> channel.write(packet));
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
            drain();output.clear();
        }
        FloorItem drop(int id,int amount,WorldTile tile,boolean hidden) {
            FloorItem item=World.addGroundItem(new Item(id,amount),tile,player,hidden,-1,2,-1,false);
            require(item.isNative950(),"Native FloorItem lifecycle marker missing");created.add(item);return item;
        }
        void clearInventory() { for(int slot=0;slot<28;slot++)player.getInventory().items.set(slot,null); }
        void prepareInventoryTick() {
            // The fixture changed slots directly; first publish that authoritative state,
            // then begin the tick in which the client is allowed to click the new items.
            input.beginTick();drain();input.beginTick();drain();output.clear();
        }
        void buttonAt(int slot,int id,int operation) {
            int hash=(1473<<16)|5;
            int[] opcodes={18,122,89,100,81,126,49,66,31,59};
            require(operation>=1&&operation<=opcodes.length,"Invalid inventory operation fixture");
            ByteBuf bytes=Unpooled.buffer(10);
            bytes.writeByte((opcodes[operation-1]+clientCipher.getAsInt())&255);bytes.writeMedium(id);
            bytes.writeByte(hash>>>16);bytes.writeByte(hash>>>24);bytes.writeByte(hash);bytes.writeByte(hash>>>8);
            bytes.writeShort(slot);
            channel.writeInbound(bytes);channel.runPendingTasks();
            require(transport.drainActions(action -> {
                require(action instanceof Native950Actions.InterfaceAction,"Inventory wire did not decode to InterfaceAction");
                Native950Actions.InterfaceAction click=(Native950Actions.InterfaceAction)action;
                require(click.option()==operation&&click.componentHash()==hash&&click.slot()==slot&&click.itemId()==id,
                        "Encrypted inventory operation decoded with changed fields");
                input.handle(action);
            })==1,"Encrypted950 inventory operation did not reach the interaction router");
            drain();
        }
        FloorItem recordInventoryDrop(int id) {
            for(FloorItem item:World.getRegion(player.getRegionId()).getGroundItemsSafe()) {
                if(item.isNative950()&&item.getId()==id&&item.getTile().matches(player)
                        &&player.getUsername().equals(item.getOwner())&&created.stream().noneMatch(old -> old==item)) {
                    created.add(item);return item;
                }
            }
            throw new AssertionError("No new authoritative inventory drop "+id);
        }
        int inventoryGroundCount() {
            int count=0;
            for(FloorItem item:World.getRegion(player.getRegionId()).getGroundItemsSafe())
                if(item.isNative950()&&item.getTile().matches(player)&&player.getUsername().equals(item.getOwner()))count++;
            return count;
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
        public void close(){for(FloorItem item:created)World.getRegion(item.getTile().getRegionId()).getGroundItemsSafe().removeIf(value -> value==item);input.close();World.removeNative950Player(player);channel.finishAndReleaseAll();}
    }
    private static byte[] hex(String text){String[] values=text.split(" ");byte[] bytes=new byte[values.length];for(int i=0;i<values.length;i++)bytes[i]=(byte)Integer.parseInt(values[i],16);return bytes;}
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
    private static final class Frame { final ServerPacket kind;final byte[] body;Frame(ServerPacket kind,byte[] body){this.kind=kind;this.body=body;} }
}
