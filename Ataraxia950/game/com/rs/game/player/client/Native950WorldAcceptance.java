package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.cores.CoresManager;
import com.rs.game.ForceMovement;
import com.rs.game.Graphics;
import com.rs.game.Region;
import com.rs.game.WorldObject;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.network.modern.Native950GameTransport;
import com.rs.network.modern.Native950Isaac;
import com.rs.network.protocol.modern950.Native950NpcMasks;
import com.rs.network.protocol.modern950.Native950Packets;
import com.rs.network.protocol.modern950.Native950PlayerMasks;
import com.rs.network.protocol.modern950.Native950Protocol.ServerPacket;
import com.rs.utils.data.parsers.npcs.NPCSpawnsDataParser;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.ReferenceCountUtil;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/**
 * Standalone real-cache acceptance for scoped world spawns and live entity mask adapters.
 * Owns an ephemeral daemon world and EmbeddedChannel only: no listening socket, authentication,
 * account load/save, legacy world thread, fake cache verifier, or copied inbound 947 vectors.
 * Checks server state and 950 output bytes; never claims client rendering or ability reachability.
 */
public final class Native950WorldAcceptance {
    private static final int[] REGIONS={12594,12850,12851,13106};
    private static final int OUTSIDE_REGION=12852;
    private static final String SCOPE="12594,12850,12851,13106";
    // Independent September 2026 audit of staged910 rows against the selected950 cache.
    // Columns: NPC id, all source rows, rows inside REGIONS, admitted rows outside REGIONS.
    // Zero-row species are intentional: adding/removing supported types or source rows needs review.
    private static final int[][] SKILL_SPECIES={
        {1028,2,0,1},{1029,3,0,1},{1030,4,0,4},{1031,1,0,1},{1032,3,0,1},{1033,1,0,1},
        {1034,0,0,0},{1035,1,0,1},{1597,1,0,1},{1598,0,0,0},
        {5082,36,0,35},{5083,8,0,8},{5084,6,0,6},{5085,42,0,40},
        {6053,0,0,0},{6054,0,0,0},{7780,1,0,1},{7866,0,0,0},{7903,3,0,0},
        {8461,1,1,0},{8464,0,0,0},{8466,1,0,1},{9085,4,0,4},{9712,1,0,0},
        {18150,9,0,9},{18151,5,0,5},{18153,6,0,5},{18155,6,0,6},{18157,6,0,6},
        {18159,6,0,6},{18161,6,0,6},{18163,6,0,6},{18165,6,0,6},{18167,6,0,6},
        {18169,6,0,6},{18171,6,0,6},{20112,1,0,0}
    };
    // Object id, region, public anchor X/Y. The placement hook may find a nearby clear footprint.
    private static final int[][] PUBLIC_STATIONS={{116393,13365,3363,3393},
            {117101,13365,3363,3384},{100874,11829,2970,3406}};
    private Native950WorldAcceptance() { }

    public static void main(String[] args) throws Exception {
        require(args.length==1,"Usage: Native950WorldAcceptance <950-flat-cache-directory>");
        require(NativeCacheVerification.isEnforced(),"Cache pin enforcement must be ON");
        String oldScope=System.getProperty(Native950SpawnScope.PROPERTY);
        String oldSpawns=System.getProperty(Native950World.SPAWNS_PROPERTY);
        String oldLegacy=System.getProperty(Native950World.LEGACY_SPAWNS_PROPERTY);
        System.setProperty(Native950SpawnScope.PROPERTY,SCOPE);
        System.setProperty(Native950World.SPAWNS_PROPERTY,"false");
        System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");
        Native950World world=null; Bridge bridge=null;
        try {
            Cache.initFlatReadOnly(Paths.get(args[0]));
            world=Native950World.getInstance();
            final Native950World owner=world;
            Expectation expected=world.execute(()->{
                require(World.getNPCs().isEmpty() && World.getPlayers().isEmpty(),"Acceptance needs a fresh isolated JVM");
                WorldTile cookLanding=Native950DevelopmentCommands.cookDestination();
                require(cookLanding.getX()==3208 && cookLanding.getY()==3215 && cookLanding.getPlane()==0,
                        "Cook development command must use the verified western landing");
                World.getRegion(cookLanding.getRegionId(),true);
                require(World.canMoveNPC(cookLanding,1) && World.canMoveNPC(0,3209,3215,1),
                        "Actual950 cache must keep both Cook home and command landing clear");
                require(World.checkWalkStep(0,3208,3215,1,0,1) && World.checkWalkStep(0,3209,3215,-1,0,1),
                        "Cook landing must have a clear cardinal approach in the actual950 collision map");
                System.out.println("PASS: Cook command landing3208,3215 and unchanged home3209,3215 have clear950 collision and cardinal approach");
                com.rs.utils.data.parsers.npcs.NPCWeaknessesDataParser.init();
                com.rs.utils.data.parsers.npcs.NPCCombatDefinitionsDataParser.init();
                com.rs.utils.data.parsers.npcs.NPCStatsDataParser.init();
                NPCSpawnsDataParser.init();
                require(NPCSpawnsDataParser.native947SpawnRegions()>0,"Actual staged spawn data is required");
                auditSkillSource();
                Native950IdValidity validity=Native950IdValidity.get();
                Expectation result=new Expectation();
                for(int region:REGIONS) {
                    World.getRegion(region,true);
                    List<NPCSpawnsDataParser.Native950Spawn> rows=NPCSpawnsDataParser.native947Spawns(region);
                    if(!rows.isEmpty()) result.regions++;
                    for(NPCSpawnsDataParser.Native950Spawn row:rows) {
                        result.rows++;
                        Native950IdValidity.Verdict verdict=validity.verdict(Native950IdValidity.Kind.NPC,row.npcId);
                        if(!verdict.isSafe()) { result.refuse(verdict.name().toLowerCase()); continue; }
                        NPCDefinitions def=NPCDefinitions.getNPCDefinitions(row.npcId);
                        if(def==null || def.decodeFailure!=null || def.size<1 || def.size>255) result.undecodable++;
                        else result.safe++;
                    }
                }
                // Deterministically preload the only permitted outside-scope additions. A player's
                // 256-tile scene can otherwise discover one of these asynchronously during the check.
                for(int region=0;region<=65535;region++) {
                    if(inScope(region))continue;
                    List<NPCSpawnsDataParser.Native950Spawn> rows=NPCSpawnsDataParser.native947Spawns(region);
                    boolean skillRegion=false;for(NPCSpawnsDataParser.Native950Spawn row:rows)
                        if(Native950SkillNpcPopulation.candidate(row.npcId)){skillRegion=true;break;}
                    if(!skillRegion)continue;
                    result.skillRegions.add(region);World.getRegion(region,true);result.regions++;
                    for(NPCSpawnsDataParser.Native950Spawn row:rows) {
                        if(!Native950SkillNpcPopulation.candidate(row.npcId))continue;
                        result.rows++;
                        Native950IdValidity.Verdict verdict=validity.verdict(Native950IdValidity.Kind.NPC,row.npcId);
                        if(!verdict.isSafe()){result.refuse(verdict.name().toLowerCase());continue;}
                        NPCDefinitions def=NPCDefinitions.getNPCDefinitions(row.npcId);
                        if(def==null||def.decodeFailure!=null||def.size<1||def.size>255){result.undecodable++;continue;}
                        if(!Native950SkillNpcPopulation.verified(row.npcId,def)||!World.isFloorFree(row.plane,row.x,row.y)){
                            result.refuse("unavailableSkillSpawn");continue;
                        }
                        result.safe++;result.extraSafe++;increment(result.extraSpecies,row.npcId);
                    }
                }
                Map<Integer,Integer> audited=auditedSkillPopulation();
                require(result.extraSafe==179 && result.extraSpecies.equals(audited),
                        "Actual950 outside-scope admissions must match the independent per-species audit: "+result.extraSpecies);
                for(int[] station:PUBLIC_STATIONS) {
                    World.getRegion(station[1],true);
                    require(publicStations(station).isEmpty(),"Public starter fixture must be absent before region announcements: "+station[0]);
                }
                World.getRegion(OUTSIDE_REGION,true);
                require(!NPCSpawnsDataParser.native947Spawns(OUTSIDE_REGION).isEmpty(),"Outside-scope fixture needs actual rows");
                require(result.rows>0 && result.safe>0,"Scoped data needs usable native NPCs");
                owner.setDataSpawnsEnabled(true);
                return result;
            }).get(45,TimeUnit.SECONDS);
            waitFor(()->owner.spawnCounters().rows>=expected.rows,30,"Scoped spawn rows did not finish");
            world.execute(()->{checkSpawns(owner,expected,1);checkPublicStations(expected,1);return null;}).get(10,TimeUnit.SECONDS);
            int firstPopulation=world.spawnCounters().spawned;
            System.out.println("PASS: scoped 950 NPC roster, actual validity verdicts/definitions/region indexes; "+owner.spawnCounters());
            bridge=world.execute(Bridge::new).get(30,TimeUnit.SECONDS);
            final Bridge live=bridge;
            world.execute(()->{live.exercise();return null;}).get(20,TimeUnit.SECONDS);
            waitFor(()->{
                try { return owner.execute(live::arrived).get(5,TimeUnit.SECONDS); }
                catch(Exception failure) { throw new IllegalStateException(failure); }
            },5,"Native force movement did not reach the scheduled final tile");
            world.execute(()->{live.checkAfterArrival();live.checkCookCompletion();live.close();return null;}).get(10,TimeUnit.SECONDS);
            bridge=null;
            world.execute(()->{
                owner.clearNativeNpcs();
                require(World.getNPCs().isEmpty(),"Roster teardown must remove every native NPC");
                for(int region:REGIONS) World.getRegion(region,true);
                for(int region:expected.skillRegions) World.getRegion(region,true);
                for(int[] station:PUBLIC_STATIONS) World.getRegion(station[1],true);
                World.getRegion(OUTSIDE_REGION,true);
                return null;
            }).get(30,TimeUnit.SECONDS);
            waitFor(()->owner.spawnCounters().rows>=expected.rows*2,30,"Scoped roster did not repopulate");
            world.execute(()->{checkSpawns(owner,expected,2);checkPublicStations(expected,2);return null;}).get(10,TimeUnit.SECONDS);
            require(world.spawnCounters().spawned==firstPopulation*2,"Teardown/reload must preserve scoped population");
            System.out.println("PASS: scoped teardown/re-announcement repopulates once; only the audited37-type skill whitelist outside scope");
            require(CoresManager.getNative950Scheduler().failed()==0,"World tick scheduler recorded a failure");
            System.out.println("PASS: native world scheduler remained healthy; no accounts or network listeners were created");
            System.out.println("LIMIT: output framing/engine state only; no client rendering, animation assets, gameplay reachability, combat, or authenticated login is claimed");
        } finally {
            if(world!=null) {
                final Native950World owner=world; final Bridge remaining=bridge;
                world.execute(()->{
                    if(remaining!=null) remaining.close();
                    owner.setDataSpawnsEnabled(false); owner.clearNativeNpcs(); return null;
                }).get(10,TimeUnit.SECONDS);
            }
            restore(Native950SpawnScope.PROPERTY,oldScope); restore(Native950World.SPAWNS_PROPERTY,oldSpawns);
            restore(Native950World.LEGACY_SPAWNS_PROPERTY,oldLegacy);
        }
    }

    private static void checkSpawns(Native950World world,Expectation expected,int passes) {
        Native950World.SpawnCounters counters=world.spawnCounters();
        require(counters.rows==expected.rows*passes && counters.regions==expected.regions*passes,
                "Original region scope plus exact skill whitelist must gate source rows and regions: "+counters);
        require(counters.spawned==expected.safe*passes && counters.undecodable==expected.undecodable*passes
                && counters.failures==0,"Spawn outcomes must match actual definitions: "+counters);
        Map<String,Integer> refused=new TreeMap<String,Integer>();
        for(Map.Entry<String,Integer> row:expected.refused.entrySet()) refused.put(row.getKey(),row.getValue()*passes);
        require(refused.equals(counters.refusedByVerdict),"Validity refusals must match source rows exactly");
        require(counters.spawned+counters.refused()+counters.undecodable==counters.rows,"Every source row must be accounted for");
        int live=0,extraLive=0;Map<Integer,Integer> liveSpecies=new TreeMap<>();
        for(NPC npc:World.getNPCs()) {
            if(npc==null) continue; live++;
            require(Native950IdValidity.get().isSafe(Native950IdValidity.Kind.NPC,npc.getId()),"Unsafe NPC reached roster");
            if(!inScope(npc.getRespawnTile().getRegionId())) {
                extraLive++;increment(liveSpecies,npc.getId());
                require(Native950SkillNpcPopulation.candidate(npc.getId())
                        &&Native950SkillNpcPopulation.verified(npc.getId(),NPCDefinitions.getNPCDefinitions(npc.getId())),
                        "Unrelated/unverified NPC escaped original region scope: "+npc.getId());
                WorldTile home=npc.getRespawnTile();
                require(World.isFloorFree(home.getPlane(),home.getX(),home.getY()),"Outside skill NPC home is blocked in950");
                boolean source=false;
                for(NPCSpawnsDataParser.Native950Spawn row:NPCSpawnsDataParser.native947Spawns(home.getRegionId()))
                    if(row.npcId==npc.getId()&&row.x==home.getX()&&row.y==home.getY()&&row.plane==home.getPlane())source=true;
                require(source,"Outside skill NPC has no exact original source spawn row");
            }
            List<Integer> indices=World.getRegion(npc.getRegionId()).getNPCsIndexes();
            require(indices!=null && indices.contains(npc.getIndex()),"NPC lacks its live region registration");
        }
        require(live==expected.safe&&extraLive==expected.extraSafe,"World roster must equal exact scoped plus safe skill population");
        require(liveSpecies.equals(auditedSkillPopulation()),"Live outside-scope roster must match every audited species: "+liveSpecies);
    }

    private static final class Expectation {
        int rows,regions,safe,undecodable,extraSafe;final java.util.Set<Integer> skillRegions=new java.util.TreeSet<>(); final Map<String,Integer> refused=new TreeMap<String,Integer>();
        final Map<Integer,Integer> extraSpecies=new TreeMap<>();final List<WorldObject> publicObjects=new ArrayList<>();
        void refuse(String verdict) { Integer count=refused.get(verdict);refused.put(verdict,count==null?1:count+1); }
    }

    private static void increment(Map<Integer,Integer> counts,int id) {
        Integer count=counts.get(id);counts.put(id,count==null?1:count+1);
    }
    private static Map<Integer,Integer> auditedSkillPopulation() {
        Map<Integer,Integer> expected=new TreeMap<>();
        for(int[] row:SKILL_SPECIES)if(row[3]>0)expected.put(row[0],row[3]);
        return expected;
    }
    private static void auditSkillSource() {
        Map<Integer,int[]> expected=new TreeMap<>();Map<Integer,Integer> all=new TreeMap<>(),inside=new TreeMap<>();
        for(int[] row:SKILL_SPECIES) {
            require(expected.put(row[0],row)==null,"Duplicate species in audit");
            require(Native950SkillNpcPopulation.candidate(row[0]),"Audited species no longer recognized: "+row[0]);
        }
        for(int id:NPCSpawnsDataParser.native947SpawnIds())
            require(Native950SkillNpcPopulation.candidate(id)==expected.containsKey(id),
                    "Spawn whitelist differs from independent species fixture: "+id);
        for(int region=0;region<=65535;region++)
            for(NPCSpawnsDataParser.Native950Spawn row:NPCSpawnsDataParser.native947Spawns(region))if(expected.containsKey(row.npcId)) {
                increment(all,row.npcId);if(inScope(region))increment(inside,row.npcId);
            }
        for(int[] row:SKILL_SPECIES) {
            int total=all.containsKey(row[0])?all.get(row[0]):0,scoped=inside.containsKey(row[0])?inside.get(row[0]):0;
            require(total==row[1] && scoped==row[2],"Original source rows changed for NPC "+row[0]+": total="+total+", scoped="+scoped);
        }
        System.out.println("PASS: independent37-species source audit; expected outside roster162 wisps/butterflies +10 implings +7 Slayer masters; blocked tutor remains refused");
    }
    private static List<WorldObject> publicStations(int[] fixture) {
        Region region=World.getRegion(fixture[1]);List<WorldObject> objects=new ArrayList<>(region.getAllObjects());
        objects.addAll(region.getSpawnedObjects());List<WorldObject> found=new ArrayList<>();
        for(WorldObject object:objects)if(object.getId()==fixture[0] && object.getPlane()==0
                && Math.abs(object.getX()-fixture[2])<=12 && Math.abs(object.getY()-fixture[3])<=12)found.add(object);
        return found;
    }
    private static void checkPublicStations(Expectation expected,int pass) {
        for(int index=0;index<PUBLIC_STATIONS.length;index++) {
            int[] fixture=PUBLIC_STATIONS[index];List<WorldObject> found=publicStations(fixture);
            require(found.size()==1,"Region announcements must populate each public skill station exactly once: "+fixture[0]+" count="+found.size());
            WorldObject object=found.get(0);
            require(object.getType()==10 && World.getObjectWithSlot(object,Region.OBJECT_SLOT_FLOOR)==object,
                    "Public station must be the authoritative interactable region object: "+fixture[0]);
            require(object.getId()==100874?Native950Invention.isStation(object):Native950Archaeology.site(object)!=null,
                    "Public object must pass its current950 skill identity gate: "+fixture[0]);
            boolean option=false;for(String label:object.getDefinitions().options)
                if(object.getId()==100874?"Manufacture".equals(label):"Excavate".equals(label))option=true;
            require(option,"Public station lacks its current950 interaction menu: "+fixture[0]);
            int x=object.getX(),y=object.getY(),sx=object.getDefinitions().sizeX,sy=object.getDefinitions().sizeY;
            for(int dx=-1;dx<=sx;dx++)for(int dy=-1;dy<=sy;dy++)
                if(dx<0||dx==sx||dy<0||dy==sy)require(World.isFloorFree(0,x+dx,y+dy,1),
                        "Public skill station lost its clear perimeter: "+fixture[0]);
            if(pass==1)expected.publicObjects.add(object);
            else require(expected.publicObjects.get(index)==object,"Region re-announcement must preserve the original public object: "+fixture[0]);
        }
        System.out.println("PASS: actual region-loading hook "+(pass==1?"placed":"preserved without duplicates")
                +" both public Archaeology plots and Falador Invention workbench with current950 menus and clear approaches");
    }

    /** Constructed and used exclusively on the actual native world thread. */
    private static final class Bridge {
        final Native950GameTransport transport=new Native950GameTransport(new Native950Isaac(new int[]{9,5,0,1}),
                new Native950Isaac(new int[]{59,55,50,51}),Thread.currentThread());
        final Native950Isaac output=new Native950Isaac(new int[]{59,55,50,51});
        final EmbeddedChannel channel=new EmbeddedChannel(transport);
        final Native950EntityFrames frames=new Native950EntityFrames();
        final Native950NpcViewport npcView=new Native950NpcViewport();
        final List<Frame> outputFrames=new ArrayList<Frame>();
        final Player player; final NPC witness; final WorldTile destination;
        long forceGeneration;
        Bridge() {
            NPC found=null;
            for(NPC npc:World.getNPCs()) if(npc!=null && npc.getSize()==1 && inScope(npc.getRespawnTile().getRegionId())) {found=npc;break;}
            require(found!=null,"Need one real scoped NPC of size1"); witness=found;
            player=Player.createNative950("worldprobe",new WorldTile(witness),channel);
            player.setIndex(1); player.setActive(true); player.setRunning(true);
            player.loadMapRegions(); // actual movement phase needs the scene's loaded-region baseline
            destination=new WorldTile(player.getX()+1,player.getY(),player.getPlane());
            player.getAppearence().generateAppearenceData();
            require(player.getAppearence().getAppeareanceData()!=null,"Real cache must produce native appearance");
            List<Player> roster=Collections.singletonList(player);
            Native950World.SceneConfig scene=new Native950World.SceneConfig(player.getX(),player.getY(),player.getPlane(),1,7,0,0,0);
            for(Native950Packets.Packet packet:frames.admit(player,scene,roster)) channel.write(packet);
            npcView.synchronize(player,channel,7,false,npcs());
            drain(); require(npcView.snapshot().indices.length>0,"Regional NPCs must enter the actual native viewport");
            player.resetMasks(); outputFrames.clear();
        }
        void exercise() {
            player.setHitpoints(50); require(player.getMaxHitpoints()==100,"Fixture expects native starter maximum100");
            player.addHitBars();
            player.setNextGraphics(new Graphics(94,17,100,0)); // exact910=950 effect config, live SHA verification
            player.setNextForceMovement(new ForceMovement(player,0,destination,1,ForceMovement.EAST));
            forceGeneration=player.getNative950ForceMaskGeneration();
            require(forceGeneration>0 && player.getNative950ForceArrivalGeneration()==0,
                    "Original force mask must carry a fresh generation before any terminal arrival");
            Native950PlayerMasks.Builder built=Native950EntityMasks.playerMasks(player);
            require(built!=null,"Real player bridge must source queued effects/force/bar");
            byte[] expected=hex("51 80 04 04 00 01 80 de00 11006400 00 fffb1f"
                    +" 80 ff 00 00 00 81 00" // no hits; one standard half-full bar, percent127 negated
                    +" 80 00 ff 80 00 80 0000 001e 0030");
            require(Arrays.equals(expected,Native950PlayerMasks.encode(built.build())),"Real combined player bridge differs from independent950 fixture");
            witness.resetMasks(); witness.setHitpoints(witness.getMaxHitpoints()); witness.addHitBars();
            byte[] npcExpected=hex("0000 20 00 01 00 00 00 7f 00");
            require(Arrays.equals(npcExpected,Native950NpcMasks.maskBlock(Native950EntityMasks.npcMasks(witness,false))),
                    "Real NPC HP-bar bridge differs from independent950 fixture");
            List<Player> roster=Collections.singletonList(player);
            frames.beginFrames(roster);
            frames.encode(new Native950Frames.Frame(player,channel,roster,npcs(),null,false,7,
                    player.getX(),player.getY(),player.getPlane()));
            npcView.synchronize(player,channel,7,false,npcs());
            channel.write(Native950Packets.tickEnd()); drain();
            Frame playerFrame=last(ServerPacket.PLAYER_INFO), npcFrame=last(ServerPacket.NPC_INFO);
            require(playerFrame!=null && npcFrame!=null,"Actual entity encoders must emit both native frames");
            // Publication appends finalXY at ordinary queued WALK speed2 before the mask,
            // retaining the physical render origin. The original source-basis builder above
            // is unchanged; PlayerInfo rebases its immutable force block exactly once.
            byte[] publishedMask=hex("51 80 04 04 00 01 80 de00 11006400 00 fffb1f"
                    +" 80 ff 00 00 00 81 00"
                    +" 81 00 00 80 00 80 0000 001e 0030"); // origin is now dx=-1; final dx=0
            // Local1/mask1/form11/short0 + packed0x2020(speed2,dx1,dy0,plane0),
            // pad tobyte; then external pass7ff4 and two skipped mask bytes.
            require(Arrays.equals(concat(hex("f2 02 00 7f f4 00 00"),publishedMask),playerFrame.body),
                    "PLAYER_INFO must publish finalXY with queued speed2 and the independently rebased force mask");
            require(player.getX()==destination.getX()-1 && player.getY()==destination.getY()
                    && player.getPlane()==destination.getPlane() && player.getNextWorldTile()==null,
                    "Publishing client finalXY must preserve the authoritative origin until its scheduled arrival");
            require(endsWith(npcFrame.body,npcExpected),"NPC_INFO must carry the actual NPC bar mask");
            require(outputFrames.indexOf(playerFrame)<outputFrames.indexOf(npcFrame),"Player update must precede relative NPC update");
            player.resetMasks(); require(player.getNextNative950ForceMovement()==null,"Mask reset must retire its snapshot");
            healthy();
            System.out.println("PASS: actual regional NPC viewport + player/NPC frames through950 ISAAC transport");
            System.out.println("PASS: live-cache effect94 and HP bars share950 mask ordering; queued finalXY publication rebases force exactly once while server remains at its origin");
        }
        boolean arrived() {
            WorldTile tile=player.getNextWorldTile();
            return tile!=null && tile.getX()==destination.getX() && tile.getY()==destination.getY() && tile.getPlane()==destination.getPlane();
        }
        void checkAfterArrival() {
            require(arrived(),"Final endpoint must survive mask reset");
            require(player.getNative950ForceArrivalGeneration()==forceGeneration
                    && !player.isNative950ForceMovementActive(),
                    "Real world scheduler must tag the matching terminal generation and finish activity");
            int preX=player.getX(),preY=player.getY(),prePlane=player.getPlane();
            player.processMovement(); // consume the actual queued endpoint exactly as the world movement phase does
            require(player.getX()==destination.getX() && player.getY()==destination.getY()
                    && player.getPlane()==destination.getPlane() && player.getNextWorldTile()==null,
                    "Actual movement phase must commit the force destination before entity frames");
            require(player.hasTeleported() && player.getNative950ForceArrivalGeneration()==forceGeneration,
                    "Terminal movement must remain tagged through the ordinary entity movement phase");
            outputFrames.clear();
            List<Player> roster=Collections.singletonList(player);
            frames.beginFrames(roster);
            frames.encode(new Native950Frames.Frame(player,channel,roster,npcs(),null,false,7,preX,preY,prePlane));
            channel.write(Native950Packets.tickEnd());drain();
            Frame terminal=last(ServerPacket.PLAYER_INFO);
            // Independently pinned950 stationary single-player body: local no-update/skip0,
            // then2046 empty external slots. This viewer received the original force block;
            // sending the ordinary relative endpoint now would move its path base a second time.
            require(terminal!=null && Arrays.equals(hex("00 7f f4"),terminal.body),
                    "Viewer that received force must get a stationary terminal frame, not duplicate movement");
            player.resetMasks();
            require(player.getNative950ForceArrivalGeneration()==0,"Terminal generation must retire after its frame");
            healthy();
            System.out.println("PASS: native world scheduler -> actual movement -> entity frames ->950 transport commits force endpoint without duplicate movement to its recipient");
        }
        void checkCookCompletion() {
            int x=player.getX(),y=player.getY(),plane=player.getPlane(),count=World.getNPCs().size();
            require(!player.hasTalkedtoCook(),"Probe must start without legacy Cook progress");
            Native950Dialogues dialogue=new Native950Dialogues(player,channel,()->{});
            player.setNative950Dialogues(dialogue);
            player.getDialogueManager().startDialogue(new com.rs.game.player.dialogue.impl.Cook(),278);
            outputFrames.clear();
            int replies=0;
            while(player.getDialogueManager().hasDialogue()) {
                require(++replies<=12,"Cook completion must close without an unbounded dialogue loop");
                int panel=dialogue.interfaceId(),component=panel==1188?8:15;
                require(dialogue.consumeResponse(panel,component,-1),"Cook reply must belong to the rendered native page");
                player.getDialogueManager().continueDialogue(panel,component);
                drain();
            }
            require(replies==9 && !dialogue.isOpen(),"Full Cook Yes path must end after its unavailable-adventure page");
            boolean notice=false;
            for(Frame frame:outputFrames) if(frame.kind==ServerPacket.IF_SETTEXT
                    && new String(frame.body,java.nio.charset.StandardCharsets.ISO_8859_1).contains("That adventure is not available yet")) notice=true;
            require(notice,"The safe ending must reach actual950 framed dialogue output");
            require(!player.hasTalkedtoCook() && player.getControlerManager().getControler()==null
                    && player.getControlerManager().getLastControler()==null,
                    "Unavailable Cook encounter must not mutate progress or install a controller");
            require(player.getX()==x && player.getY()==y && player.getPlane()==plane
                    && player.getNextWorldTile()==null && !player.isLocked() && World.getNPCs().size()==count,
                    "Full Cook dialogue must preserve world position, movement ownership and native NPC roster");
            for(NPC npc:World.getNPCs()) require(npc==null||npc.isNative950(),"Cook must never insert a legacy NPC");
            player.getDialogueManager().startDialogue(new com.rs.game.player.dialogue.impl.Cook(),278);
            require(dialogue.isOpen(1184),"Cook dialogue must reopen after completing the safe ending");
            player.getDialogueManager().finishDialogue();drain();healthy();
            System.out.println("PASS: actual-cache full Cook Yes dialogue through native presentation and950 transport closes safely, reopens, preserves progress/location and spawns no legacy instance");
        }
        void close() {
            player.setNextForceMovement(null);
            if(player.getLastRegionId()>=0) World.getRegion(player.getLastRegionId()).removePlayerIndex(player.getIndex());
            player.setLastRegionId(-1);
            frames.release(player);npcView.close();channel.finishAndReleaseAll();
        }
        void healthy() {channel.checkException();require(channel.isActive() && transport.terminalFailure()==null,"Transport failed: "+transport.terminalFailure());}
        Frame last(ServerPacket kind) {for(int i=outputFrames.size()-1;i>=0;i--)if(outputFrames.get(i).kind==kind)return outputFrames.get(i);return null;}
        void drain() {
            channel.flush(); channel.runPendingTasks(); Object value;
            while((value=channel.readOutbound())!=null) try {
                require(value instanceof ByteBuf,"Native frames must reach byte transport"); ByteBuf bytes=(ByteBuf)value;
                while(bytes.isReadable()) {
                    int opcode=(bytes.readUnsignedByte()-output.getAsInt())&255;
                    if(opcode>=128)opcode=((opcode-128)<<8)|((bytes.readUnsignedByte()-output.getAsInt())&255);
                    ServerPacket kind=null;for(ServerPacket row:ServerPacket.values())if(row.opcode()==opcode)kind=row;
                    require(kind!=null,"Unknown native output/cipher mismatch "+opcode);
                    int length=kind.size();if(length==-1)length=bytes.readUnsignedByte();else if(length==-2)length=bytes.readUnsignedShort();
                    require(length>=0 && length<=bytes.readableBytes(),"Invalid framed length for "+kind);
                    byte[] body=new byte[length];bytes.readBytes(body);outputFrames.add(new Frame(kind,body));
                }
            } finally {ReferenceCountUtil.release(value);}
            healthy();
        }
    }
    private static final class Frame {final ServerPacket kind;final byte[] body;Frame(ServerPacket kind,byte[] body){this.kind=kind;this.body=body;}}
    private static List<NPC> npcs() {List<NPC> list=new ArrayList<NPC>();for(NPC npc:World.getNPCs())if(npc!=null)list.add(npc);return list;}
    private static boolean inScope(int region) {for(int allowed:REGIONS)if(region==allowed)return true;return false;}
    private static boolean endsWith(byte[] data,byte[] tail) {if(data.length<tail.length)return false;for(int i=0;i<tail.length;i++)if(data[data.length-tail.length+i]!=tail[i])return false;return true;}
    private static byte[] concat(byte[] a,byte[] b) {byte[] both=Arrays.copyOf(a,a.length+b.length);System.arraycopy(b,0,both,a.length,b.length);return both;}
    private static byte[] hex(String text) {String s=text.replace(" ","");byte[] b=new byte[s.length()/2];for(int i=0;i<b.length;i++)b[i]=(byte)Integer.parseInt(s.substring(2*i,2*i+2),16);return b;}
    private static void waitFor(BooleanSupplier done,int seconds,String message) throws InterruptedException {
        long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(seconds);
        while(!done.getAsBoolean()) {if(System.nanoTime()>=deadline)throw new AssertionError(message);Thread.sleep(50);}
    }
    private static void restore(String key,String old) {if(old==null)System.clearProperty(key);else System.setProperty(key,old);}
    private static void require(boolean condition,String message) {if(!condition)throw new AssertionError(message);}
}
