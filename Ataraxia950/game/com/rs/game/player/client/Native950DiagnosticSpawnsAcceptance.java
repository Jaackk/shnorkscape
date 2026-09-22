package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.ObjectDefinitions;
import com.rs.cores.CoresManager;
import com.rs.game.Region;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.network.modern.Native950GameTransport;
import com.rs.network.modern.Native950Isaac;
import com.rs.network.protocol.modern950.Native950Packets;
import com.rs.network.protocol.modern950.Native950Protocol.ServerPacket;
import com.rs.utils.data.parsers.npcs.NPCCombatDefinitionsDataParser;
import com.rs.utils.data.parsers.npcs.NPCStatsDataParser;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.ReferenceCountUtil;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Isolated actual-cache diagnostic placement/registries/native-wire probe; never touches account saves. */
public final class Native950DiagnosticSpawnsAcceptance {
    private static int frames;
    public static void main(String[] args) throws Exception {
        require(args.length == 1,"Usage: Native950DiagnosticSpawnsAcceptance <950-flat-cache-directory>");
        System.setProperty(Native950World.SPAWNS_PROPERTY,"false");
        System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");
        Cache.initFlatReadOnly(Paths.get(args[0]));
        Native950World world = Native950World.getInstance();
        world.execute(() -> {
            require(World.getPlayers().isEmpty() && World.getNPCs().isEmpty(),"Fresh isolated JVM required");
            NPCCombatDefinitionsDataParser.init(); NPCStatsDataParser.init();
            try (Fixture f = new Fixture()) { verify(world,f); }
            finally { world.clearNativeNpcs(); }
            require(World.getPlayers().isEmpty() && World.getNPCs().isEmpty(),"Probe registry cleanup failed");
            return null;
        }).get(120,TimeUnit.SECONDS);
        require(CoresManager.getNative950Scheduler().failed() == 0,"Scheduler recorded a failure");
        System.out.println("PASS: separated diagnostic NPCs, exact-tile objects, combat and viewport registration, occupied-slot refusal, "
                + frames + " parsed encrypted 950 frames; no account save or listening socket.");
        System.out.println("LIMIT: client rendering remains a manual check.");
    }

    private static void verify(Native950World world, Fixture f) {
        WorldTile tile = new WorldTile(f.player);
        for (int id : new int[]{42,12353}) {
            int before = world.nativeNpcs().size();
            String response = Native950DiagnosticSpawns.spawnNpc(f.player,id);
            require(response.startsWith("Spawned "),response);
            require(world.nativeNpcs().size() == before+1,"NPC missing from owner roster");
            NPC npc = world.nativeNpcs().get(before);
            require(npc.getId() == id && npc.isNative950DiagnosticDefinition(),"Wrong diagnostic identity");
            require(!same(npc,tile) && same(f.player,tile),"Diagnostic NPC must not overlap the player");
            require(World.getNPCs().get(npc.getIndex()) == npc
                    && World.getRegion(npc.getRegionId()).getNPCsIndexes().contains(npc.getIndex()),"NPC registry/region missing");
            if (id == 12353) require(npc.getNative950CombatProfile() != null,"Existing generic goblin combat was not registered");
            if (id == 42) require(!Native950IdValidity.get().isSafe(Native950IdValidity.Kind.NPC,id),"Repurposed ID witness changed");
            System.out.println("PASS: " + response);
        }
        Native950NpcViewport viewport = new Native950NpcViewport();
        f.channel.write(viewport.frame(f.player,7,false,new ArrayList<NPC>(world.nativeNpcs())));
        require(viewport.snapshot().indices.length == 2,"Diagnostic actual-cache NPC not published");
        f.drain(); require(f.count(ServerPacket.NPC_INFO) == 1,"Encrypted NPC_INFO missing");
        int before = world.nativeNpcs().size();
        require(!Native950DiagnosticSpawns.spawnNpc(f.player,65535).startsWith("Spawned "),"Invalid NPC ID accepted");
        require(before == world.nativeNpcs().size(),"Rejected NPC altered roster");
        String group=Native950DiagnosticSpawns.spawnNpcs(f.player,12353,3,false);
        require(group.startsWith("Spawned 3/3"),group);
        require(world.nativeNpcs().size()==before+3,"Amount did not register three distinct NPCs");
        for(int i=before;i<world.nativeNpcs().size();i++)for(int j=0;j<i;j++)
            require(!Native950DiagnosticSpawns.overlaps(world.nativeNpcs().get(i),world.nativeNpcs().get(i).getSize(),
                    world.nativeNpcs().get(j),world.nativeNpcs().get(j).getSize(),1),"Spawned NPCs overlap");
        String repeat=Native950DiagnosticSpawns.spawnNpcs(f.player,12353,1,true);
        require(repeat.startsWith("Spawned 1/1"),repeat);
        require(world.nativeNpcs().size()==before+4,"Repeat NPC was not registered as a distinct encounter");
        require(!Native950DiagnosticSpawns.matchingNpcIds(f.player,"nex").isEmpty(),
                ";;npc nex could not resolve any concrete cache NPC choices");

        String response = Native950DiagnosticSpawns.spawnObject(f.player,70755);
        require(response.startsWith("Spawned "),response);
        WorldObject fire = World.getObjectWithSlot(tile,Region.OBJECT_SLOT_FLOOR);
        require(fire != null && fire.getId() == 70755 && fire.getType() == 10 && fire.getRotation() == 0
                && same(fire,tile) && same(f.player,tile),"Object ID/shape/tile mismatch");
        f.created.add(fire); f.objects.refresh(f.player); f.drain();
        require(f.count(ServerPacket.LOC_ADD_CHANGE) == 1,"Native object add missing");
        require(!Native950DiagnosticSpawns.spawnObject(f.player,70756).startsWith("Spawned "),"Occupied object slot replaced");
        require(World.getObjectWithSlot(tile,Region.OBJECT_SLOT_FLOOR) == fire,"Refused placement modified live fire");
        World.removeObject(fire); f.created.remove(fire); f.objects.refresh(f.player); f.drain();
        require(f.count(ServerPacket.LOC_DEL) == 1,"Native object removal missing");
        System.out.println("PASS: fire appears at exact player tile, occupied slot is preserved, removal restores native scene");

        WorldObject wall = findWall(f.player);
        response = Native950DiagnosticSpawns.spawnObject(f.player,wall.getId());
        require(response.startsWith("Spawned "),response);
        WorldObject placed = World.getObjectWithSlot(tile,Region.OBJECT_SLOT_WALL);
        require(placed != null && placed.getId() == wall.getId() && placed.getType() == 0 && same(placed,tile),
                "Wall object did not select its actual cache shape");
        f.created.add(placed); f.objects.refresh(f.player); f.drain();
        require(f.count(ServerPacket.LOC_ADD_CHANGE) == 2,"Native wall add missing");
        require(!Native950DiagnosticSpawns.spawnObject(f.player,wall.getId(),10,0).startsWith("Spawned "),
                "Wall with no scenery model was incorrectly accepted as type10");
        System.out.println("PASS: " + response + " Unsupported explicit model shape is refused.");
        require(!World.isWallsFree(tile.getPlane(),tile.getX(),tile.getY())
                && World.isFloorFree(tile.getPlane(),tile.getX(),tile.getY()),
                "Wall witness must block an edge while leaving the floor free");
        WorldObject large = findMultiTileObject(f.player);
        response = Native950DiagnosticSpawns.spawnObject(f.player,large.getId());
        require(response.contains("touches a wall"),"Multi-tile wall overlap not specifically refused: " + response);
        require(World.getObjectWithSlot(tile,Region.OBJECT_SLOT_FLOOR) == null
                && World.getObjectWithSlot(tile,Region.OBJECT_SLOT_WALL) == placed,
                "Refused multi-tile overlap changed floor or wall state");
        System.out.println("PASS: multi-tile object " + large.getId() + " cannot cross wall edges on an otherwise free floor");
        viewport.close();
    }

    private static WorldObject findWall(Player player) {
        for (int regionId : player.getMapRegionsIds()) {
            List<WorldObject> objects = World.getRegion(regionId,true).getAllObjects();
            if (objects == null) continue;
            for (WorldObject object : objects) {
                if (object.getType() != 0) continue;
                ObjectDefinitions d = object.getDefinitions();
                if (d.transforms != null || d.sizeX != 1 || d.sizeY != 1 || d.getClipType() == 0) continue;
                try {
                    if (Native950DiagnosticSpawns.selectShape(d.shapes,d.models,-1) == 0) return object;
                } catch (IllegalArgumentException ignored) { }
            }
        }
        throw new IllegalStateException("No ordinary shape0 wall found in actual loaded scene");
    }

    private static WorldObject findMultiTileObject(Player player) {
        for (int regionId : player.getMapRegionsIds()) {
            List<WorldObject> objects = World.getRegion(regionId,true).getAllObjects();
            if (objects == null) continue;
            for (WorldObject object : objects) {
                ObjectDefinitions d = object.getDefinitions();
                if (d.transforms != null || (d.sizeX <= 1 && d.sizeY <= 1)
                        || d.sizeX < 1 || d.sizeY < 1 || d.sizeX > 64 || d.sizeY > 64) continue;
                try {
                    int type = Native950DiagnosticSpawns.selectShape(d.shapes,d.models,-1);
                    if (type >= 9 && type <= 21) return object;
                } catch (IllegalArgumentException ignored) { }
            }
        }
        throw new IllegalStateException("No multi-tile scenery definition in actual loaded scene");
    }

    private static final class Fixture implements AutoCloseable {
        final Native950Isaac cipher = new Native950Isaac(new int[]{59,55,50,52});
        final Native950GameTransport transport = new Native950GameTransport(new Native950Isaac(new int[]{9,5,0,2}),
                new Native950Isaac(new int[]{59,55,50,52}),Thread.currentThread());
        final EmbeddedChannel channel = new EmbeddedChannel(transport);
        final Native950ObjectsView objects = new Native950ObjectsView(Thread.currentThread(),packet -> channel.write(packet));
        final List<ServerPacket> packets = new ArrayList<ServerPacket>();
        final List<WorldObject> created = new ArrayList<WorldObject>();
        final Player player;
        Fixture() {
            player = Player.createNative950("diagnostic-probe",new WorldTile(3217,3258,0),channel);
            player.setActive(true); player.setRunning(true);
            World.addNative950Player(player,1); World.updateEntityRegion(player);
            player.loadMapRegions(); player.setClientHasLoadedMapRegion();
            require(World.isFloorFree(0,player.getX(),player.getY()),"Probe tile is not clear");
            objects.refresh(player); drain(); packets.clear();
        }
        int count(ServerPacket kind) { int count=0; for(ServerPacket packet:packets) if(packet==kind) count++; return count; }
        void drain() {
            channel.flush(); channel.runPendingTasks(); Object message;
            while ((message=channel.readOutbound()) != null) try {
                require(message instanceof ByteBuf,"Transport did not encode bytes"); ByteBuf bytes=(ByteBuf)message;
                while(bytes.isReadable()) {
                    int opcode=(bytes.readUnsignedByte()-cipher.getAsInt())&255;
                    if(opcode>=128) opcode=((opcode-128)<<8)|((bytes.readUnsignedByte()-cipher.getAsInt())&255);
                    ServerPacket kind=null; for(ServerPacket row:ServerPacket.values()) if(row.opcode()==opcode){kind=row;break;}
                    require(kind!=null,"Unknown encrypted opcode "+opcode);
                    int size=kind.size(); if(size==-1)size=bytes.readUnsignedByte();else if(size==-2)size=bytes.readUnsignedShort();
                    require(size>=0 && size<=bytes.readableBytes(),"Truncated native frame "+kind);
                    bytes.skipBytes(size); packets.add(kind); frames++;
                }
            } finally {ReferenceCountUtil.release(message);}
            channel.checkException(); require(channel.isActive()&&transport.terminalFailure()==null,"Transport failed");
        }
        public void close() {
            for(WorldObject object:created) World.removeObject(object);
            World.removeNative950Player(player); channel.finishAndReleaseAll();
        }
    }
    private static boolean same(WorldTile a,WorldTile b){return a.getX()==b.getX()&&a.getY()==b.getY()&&a.getPlane()==b.getPlane();}
    private static void require(boolean condition,String message){if(!condition)throw new IllegalStateException(message);}
}
