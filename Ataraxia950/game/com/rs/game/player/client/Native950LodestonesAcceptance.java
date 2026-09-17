package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cores.CoresManager;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.actions.HomeTeleport;
import com.rs.game.player.client.ui.Native950Bindings;
import com.rs.game.player.content.InterfaceManager;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.network.modern.Native950GameTransport;
import com.rs.network.modern.Native950Isaac;
import com.rs.network.protocol.modern950.Native950Packets;
import com.rs.network.protocol.modern950.Native950PlayerInfo;
import com.rs.network.protocol.modern950.Native950Protocol.ServerPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.ReferenceCountUtil;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/** Fresh-JVM paired-cache acceptance through actual950 input, session movement/rebuild and HomeTeleport. */
public final class Native950LodestonesAcceptance {
    private static int actions, ticks, frames, journeys;
    private Native950LodestonesAcceptance() { }

    public static void main(String[] args) throws Exception {
        require(args.length == 1, "Usage: Native950LodestonesAcceptance <950-flat-cache-directory>");
        require(NativeCacheVerification.isEnforced(), "Cache pin enforcement must remain enabled");
        System.setProperty(Native950World.SPAWNS_PROPERTY, "false");
        System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY, "false");
        System.setProperty(Native950WorldMap.PROPERTY, "true");
        Cache.initFlatReadOnly(Paths.get(args[0]));
        Native950World owner = Native950World.getInstance();
        owner.execute(() -> {
            require(World.getPlayers().isEmpty() && World.getNPCs().isEmpty(), "Fresh isolated JVM required");
            Native950Bindings table = Native950Bindings.tryLoad();
            require(table != null, "Actual950 UI bindings failed to load");
            Native950Lodestones.verify();
            Native950MapAreas.verify();
            require(Native950LodestoneTeleport.verifyAssets(), "Actual950 HomeTeleport assets failed verification");
            Native950Settings.verify();
            Native950WorldMap.verify();
            List<Native950Lodestones.Destination> destinations = Native950Lodestones.destinations();
            require(destinations.size() == 29, "Expected all29 ordinary950 destinations");
            Set<Integer> components = new HashSet<Integer>();
            List<String> unusable = new ArrayList<String>();
            for (Native950Lodestones.Destination destination : destinations) {
                require(components.add(destination.component), "Duplicate lodestone button");
                WorldTile arrival = destination.arrival();
                require(Native950MapAreas.resolvedAreaType(arrival.getX(),arrival.getY())>=0,"Missing actual cache area for "+destination.name);
                World.getRegion(arrival.getRegionId(), true);
                // Collected rather than thrown: one unusable arrival used to abort the whole run,
                // which hid the state of the other 28 and of every journey below it.
                try {
                    require(World.isRegionLoaded(arrival.getRegionId())
                            && World.isFloorFree(arrival.getPlane(), arrival.getX(), arrival.getY()),
                            "Actual950 arrival is unloaded or blocked: " + destination.name + " at " + tile(arrival));
                    adjacent(arrival); // independently require a collision-valid way to walk off every landing.
                } catch (AssertionError unusableArrival) {
                    unusable.add(destination.name + ": " + unusableArrival.getMessage());
                    System.out.println("PREFLIGHT FAIL: " + destination.name + " at " + tile(arrival)
                            + " - " + unusableArrival.getMessage());
                }
            }
            if (unusable.isEmpty())
                System.out.println("PASS: all29 cache-pinned950 network destinations have loaded clear arrivals and a walkable exit");
            else
                System.out.println("PREFLIGHT: " + unusable.size() + " of " + destinations.size()
                        + " destinations have an unusable arrival; journeys below skip them");
            List<String> journeyFailures = new ArrayList<String>();
            try (Fixture fixture = new Fixture(table)) {
                uiTransitions(fixture);
                cancellation(fixture, named(destinations, "Varrock"));
                // Each journey is independently reported. A destination whose arrival already
                // failed pre-flight is skipped rather than attempted: HomeTeleport cannot land on a
                // blocked tile, so running it would only restate the pre-flight failure, and a
                // half-completed journey can leave the fixture in a state that corrupts the next.
                for (Native950Lodestones.Destination destination : destinations) {
                    boolean preflightFailed = false;
                    for (String entry : unusable)
                        if (entry.startsWith(destination.name + ": ")) preflightFailed = true;
                    if (preflightFailed) {
                        journeyFailures.add(destination.name + ": skipped, arrival failed pre-flight");
                        continue;
                    }
                    try {
                        complete(fixture, destination);
                    } catch (AssertionError journeyFailure) {
                        journeyFailures.add(destination.name + ": " + journeyFailure.getMessage());
                        System.out.println("JOURNEY FAIL: " + destination.name + " - " + journeyFailure.getMessage());
                    }
                }
                complete(fixture, named(destinations, "Lumbridge"));
                fixture.assertHealthy();
                Native950Session.Snapshot state = fixture.session.snapshot();
                require(state.checkpoints == 0, "Ephemeral acceptance must never write character checkpoints");
                require(state.tickFailures == 0 && state.facadeStrictHits == 0 && state.interactions.handlerFailures == 0,
                        "Native tick/facade/handler failure: " + state);
                require(state.interactions.unhandledActions == 0, "A decoded action went unhandled");
                require(fixture.sceneCloses == 1, "Only the explicit world-map transition may close scene1482");
            }
            // A fresh session started in Um must select mainland again when leaving it.
            try(Fixture returning=new Fixture(table,named(destinations,"City of Um").arrival())) {
                complete(returning,named(destinations,"Lumbridge"));
                returning.assertHealthy();
                System.out.println("PASS: fresh City of Um session returns to mainland area474");
            }
            // Still fail-closed: every collected problem is a failure and the process exits
            // non-zero. The difference is that it now names all of them at once.
            if (!unusable.isEmpty() || !journeyFailures.isEmpty()) {
                System.out.println();
                System.out.println("=== " + (unusable.size() + journeyFailures.size()) + " lodestone problem(s) ===");
                for (String entry : unusable) System.out.println("  arrival: " + entry);
                for (String entry : journeyFailures) System.out.println("  journey: " + entry);
                throw new AssertionError(unusable.size() + " unusable arrival(s) and "
                        + journeyFailures.size() + " failed journey(s); see the list above");
            }
            return null;
        }).get(180, TimeUnit.SECONDS);
        require(CoresManager.getNative950Scheduler().failed() == 0, "Native scheduler recorded a failure");
        System.out.println("PASS: " + journeys + " complete original HomeTeleport journeys including City of Um, Fort Forinthry, Wendlewick and return to Lumbridge");
        System.out.println("PASS: " + actions + " encrypted950 actions, " + ticks + " original session ticks, " + frames
                + " parsed outbound frames; no listener, authentication, account load or save");
        System.out.println("LIMIT: cache scripts, client rendering, keyboard shortcuts and visual/audio timing require the real client");
    }

    private static void uiTransitions(Fixture f) {
        f.home(); f.assertOpen(); f.assertScene();
        int mounted=f.find(0,ServerPacket.IF_OPENSUB,1477,735,1092);
        int wrapper=f.packetIndex(0,Native950Packets.runClientScript(11145,576,376,0,0,hash(1477,732)));
        int host=f.packetIndex(0,Native950Packets.runClientScript(11145,576,360,0,0,hash(1477,735)));
        require(wrapper>=0 && host>wrapper && mounted>host,"Panel mounted before both clipping ancestors were sized");
        int start = f.output.size();
        f.button(1092,66,-1); f.assertClosed(); f.assertScene();
        require(f.find(start,ServerPacket.IF_CLOSESUB,1477,735,-1) >= start, "Network close omitted native closeSub");
        f.home(); f.button(1431,0,7);
        require(f.manager().containsNative950Settings(), "Lodestone -> Settings did not open Settings");
        f.assertClosed(); f.assertScene();
        f.home(); f.assertOpen();
        require(!f.manager().containsNative950Settings(), "Settings -> Lodestone retained Settings owner");
        f.button(1465,11,-1);
        f.assertClosed();
        require(f.manager().containsWorldMapInterface() && !f.manager().containsInterface(1482),
                "Lodestone -> world map did not transfer scene ownership");
        start = f.output.size();
        f.home(); f.assertOpen(); f.assertScene();
        int scene = f.find(start,ServerPacket.IF_OPENSUB,1477,30,1482);
        int network = f.find(start,ServerPacket.IF_OPENSUB,1477,735,1092);
        require(scene >= start && network > scene, "Map -> network must restore scene before opening network");
        f.send(5,new byte[0]); f.assertClosed(); f.assertScene();
        require(f.sceneCloses == 1, "Lodestone/Settings/modal close unexpectedly closed the world scene");
        System.out.println("PASS: encrypted1465:34 Home ->1092; close66; Settings/map handoffs and CLOSE_MODAL retain/restore scene1482");
    }

    private static void cancellation(Fixture f, Native950Lodestones.Destination target) {
        WorldTile origin = new WorldTile(f.player);
        f.home(); f.button(1092,target.component,-1);
        require(f.player.getActionManager().getAction() instanceof HomeTeleport, "Network selection did not start original HomeTeleport");
        f.assertClosed();
        for (int tick = 0; tick < 4; tick++) f.tick();
        require(f.player.matches(origin) && !f.player.isLocked(), "Departure moved/locked the character too early");
        WorldTile walk = adjacent(origin);
        f.walk(walk);
        require(!f.player.getActionManager().hasSkillWorking(), "Encrypted walk did not cancel HomeTeleport");
        for (int tick = 0; tick < 32; tick++) f.tick();
        require(f.player.matches(walk) && f.player.getNextWorldTile() == null && !f.player.isLocked(),
                "Cancelled HomeTeleport later teleported/locked the character");
        f.assertScene();
        // Opening an owned modal must retire the departure action just like a ground click.
        f.home(); f.button(1092,target.component,-1); f.tick(); f.tick();
        f.button(1431,0,7);
        require(!f.player.getActionManager().hasSkillWorking() && f.manager().containsNative950Settings(),
                "Opening Settings failed to cancel an active departure");
        for (int tick = 0; tick < 28; tick++) f.tick();
        require(f.player.matches(walk) && f.player.getNextWorldTile() == null, "Modal-cancelled departure committed later");
        f.send(5,new byte[0]); f.assertScene();
        System.out.println("PASS: real950 ground walk and Settings cancel original HomeTeleport before commit, with no delayed teleport");
    }

    private static void complete(Fixture f, Native950Lodestones.Destination target) {
        WorldTile origin = new WorldTile(f.player), arrival = target.arrival();
        f.home(); f.assertOpen();
        int start = f.output.size();
        f.button(1092,target.component,-1);
        require(f.player.getActionManager().getAction() instanceof HomeTeleport,
                "Destination did not start original HomeTeleport: " + target.name + "; " + f.recentMessages(start));
        f.assertClosed();
        int elapsed = 0;
        long initialRebuilds = f.session.snapshot().sceneRebuilds;
        for (; elapsed < 40 && f.player.getActionManager().hasSkillWorking(); elapsed++) {
            f.tick();
            if (elapsed < 17) require(f.player.matches(origin), "HomeTeleport moved before original commit tick: " + target.name);
        }
        require(elapsed == 25, "Original HomeTeleport timeline changed for " + target.name + ": " + elapsed + " ticks");
        require(f.player.matches(arrival) && !f.player.isLocked() && f.player.getNextWorldTile() == null,
                "Wrong/unreleased final arrival for " + target.name + ": " + tile(f.player) + " expected " + tile(arrival));
        require(f.player.getLastRegionId() == arrival.getRegionId()
                && f.player.getMapRegionsIds().contains(arrival.getRegionId())
                && World.isRegionLoaded(arrival.getRegionId()) && f.player.clientHasLoadedMapRegion(),
                "Arrival was not registered and loaded for " + target.name);
        long rebuilds = f.session.snapshot().sceneRebuilds - initialRebuilds;
        require(rebuilds > 0, "Distant lodestone journey did not rebuild the scene: " + target.name);
        int rebuild = f.findKind(start,ServerPacket.REBUILD_NORMAL);
        int playerInfo = f.findKind(rebuild+1,ServerPacket.PLAYER_INFO);
        require(rebuild >= start && playerInfo > rebuild, "Map rebuild must precede destination PLAYER_INFO");
        byte[] header = f.output.get(rebuild).body;
        require(header.length == 18 && (header[2]&255) == 5
                && ((header[6]&255)<<8 | (header[7]&255)) == (arrival.getX()>>>3)
                && ((header[0]&255)<<8 | ((header[1]-128)&255)) == (arrival.getY()>>>3),
                "Native256 scene header did not carry the destination chunks: " + target.name);
        // The client builds only the map squares the declared area permits. Wendlewick and City
        // of Um each need their own areaType; sending the mainland's leaves them as empty sky.
        int wireArea = ((header[8]&255) << 8) | (header[9]&255);
        int wantArea = Native950MapAreas.areaTypeFor(arrival.getX(), arrival.getY(),
                Native950MapAreas.defaultAreaType());
        require(wireArea == wantArea, "Scene header declared the wrong world area for " + target.name
                + ": sent " + wireArea + ", " + tile(arrival) + " needs " + wantArea);
        require(f.findKind(start,ServerPacket.PLAYER_INFO) >= start,
                "HomeTeleport effects never reached PLAYER_INFO for " + target.name);
        // REBUILD_NORMAL re-centres the scene but carries no position: after the first one the
        // client only learns where the avatar is from the PLAYER_INFO movement bitstream. A
        // correct header with no teleport record leaves the client building a scene around a
        // stale avatar, which is exactly what an empty arrival looks like in game, so decode it.
        int[] record = localMovement(f.output.get(playerInfo).body);
        require(record[0] == Native950PlayerInfo.FORM_ABSOLUTE,
                "Destination PLAYER_INFO did not carry an absolute local move for " + target.name
                        + ": form " + record[0]);
        require(record[1] == Native950PlayerInfo.MOVEMENT_TELEPORT,
                "Destination local move was not a teleport for " + target.name + ": speed " + record[1]);
        require(origin.getX() + record[2] == arrival.getX()
                && origin.getY() + record[3] == arrival.getY()
                && ((origin.getPlane() + record[4]) & 0x3) == arrival.getPlane(),
                "Destination local move landed the client elsewhere for " + target.name + ": "
                        + tile(origin) + " + (" + record[2] + "," + record[3] + "," + record[4] + ")"
                        + " expected " + tile(arrival));
        f.assertScene();
        WorldTile exit = adjacent(arrival);
        f.walk(exit);
        for (int tick=0; tick<4 && !f.player.matches(exit); tick++) f.tick();
        require(f.player.matches(exit) && !f.player.hasWalkSteps(), "Cannot walk off arrived lodestone " + target.name);
        f.assertHealthy(); journeys++;
        System.out.println("PASS:1092:" + target.component + " " + target.name + " -> " + tile(arrival)
                + "; area " + wireArea + ", original25 ticks, " + rebuilds + " scene rebuild, teleport("
                + record[2] + "," + record[3] + "," + record[4] + "), collision walk-off");
    }

    /**
     * Decode the first record of a PLAYER_INFO movement bitstream. This fixture holds exactly one
     * player, so pass 1 or pass 2 writes its record at bit zero and nothing precedes it.
     *
     * <p>Returns {form, speed, dx, dy, dplane}. form is -1 when the player published no movement
     * at all this frame, in which case the remaining entries are zero. The bit layout is the
     * encoder's: 1 presence bit, 1 pending-mask bit, 2 form bits, then per form - the absolute
     * form's long variant is 1 flag bit, 3 speed bits and 30 packed position bits
     * ({@code 0x140125E89}), its short variant a single 15-bit word ({@code 0x140125D57}).
     */
    private static int[] localMovement(byte[] body) {
        BitReader bits = new BitReader(body);
        if (bits.read(1) == 0) return new int[] {-1, 0, 0, 0, 0};
        bits.read(1); // pending-mask flag; does not move the movement cursor
        int form = bits.read(2);
        if (form != Native950PlayerInfo.FORM_ABSOLUTE) return new int[] {form, 0, 0, 0, 0};
        if (bits.read(1) == 0) {
            int packed = bits.read(15);
            return new int[] {form, (packed >>> 12) & 0x7, signed((packed >>> 5) & 0x1F, 5),
                    signed(packed & 0x1F, 5), (packed >>> 10) & 0x3};
        }
        int speed = bits.read(3);
        int packed = bits.read(30);
        return new int[] {form, speed, signed((packed >>> 14) & 0x3FFF, 14),
                signed(packed & 0x3FFF, 14), (packed >>> 28) & 0x3};
    }

    /** Two's complement over {@code width} bits, the sign extension the client applies. */
    private static int signed(int value, int width) {
        int sign = 1 << (width - 1);
        return (value & sign) != 0 ? value - (sign << 1) : value;
    }

    private static final class BitReader {
        private final byte[] data; private int cursor;
        BitReader(byte[] data) { this.data = data; }
        int read(int count) {
            int value = 0;
            for (int i = 0; i < count; i++) {
                int bit = cursor + i;
                if ((bit >>> 3) >= data.length)
                    throw new AssertionError("PLAYER_INFO movement bitstream ended early");
                value = (value << 1) | ((data[bit >>> 3] >>> (7 - (bit & 7))) & 1);
            }
            cursor += count;
            return value;
        }
    }

    private static Native950Lodestones.Destination named(List<Native950Lodestones.Destination> rows, String name) {
        for (Native950Lodestones.Destination row : rows) if (row.name.equals(name)) return row;
        throw new AssertionError("Missing network destination " + name);
    }
    private static WorldTile adjacent(WorldTile origin) {
        for (int[] direction : new int[][]{{1,0},{0,1},{-1,0},{0,-1}}) {
            int x=origin.getX()+direction[0], y=origin.getY()+direction[1];
            if (x<0 || x>16383 || y<0 || y>16383) continue;
            WorldTile tile = new WorldTile(x,y,origin.getPlane());
            World.getRegion(tile.getRegionId(),true);
            if (World.isFloorFree(tile.getPlane(),x,y)
                    && World.checkWalkStep(origin.getPlane(),origin.getX(),origin.getY(),direction[0],direction[1],1)
                    && World.checkWalkStep(origin.getPlane(),x,y,-direction[0],-direction[1],1)) return tile;
        }
        throw new AssertionError("No clear cardinal step from actual950 lodestone arrival " + tile(origin));
    }

    private static final class Fixture implements AutoCloseable {
        final int[] incoming={9,5,0,29}, outgoing={59,55,50,79};
        final Native950Isaac inputCipher=new Native950Isaac(incoming), outputCipher=new Native950Isaac(outgoing);
        final Native950GameTransport transport=new Native950GameTransport(
                new Native950Isaac(incoming),new Native950Isaac(outgoing),Thread.currentThread());
        final EmbeddedChannel channel=new EmbeddedChannel(transport);
        final Native950EntityFrames entities=new Native950EntityFrames();
        final List<Frame> output=new ArrayList<Frame>();
        final Player player;
        final Native950Session session;
        int sceneCloses;
        final long initialMaskRefusals=Native950EntityMasks.refusals();
        Fixture(Native950Bindings table) { this(table,new WorldTile(3217,3258,0)); }
        Fixture(Native950Bindings table,WorldTile origin) {
            // 9 characters, not 15. The appearance encoder caps a name at 12 (the width of the
            // wire field it goes into), so "lodestone-probe" made generateAppearenceData withhold
            // the body and return null, failing fixture construction before a single journey ran.
            // The City of Um pre-flight abort hid that, so this harness had never reached the
            // journeys it exists to run.
            player=Player.createNative950("lodeprobe",new WorldTile(origin),channel);
            player.setActive(true);player.setRunning(true);Native950World.installVarpSink(player);
            World.addNative950Player(player,1);World.updateEntityRegion(player);player.loadMapRegions();
            // areaType mirrors the deployed server.toml value rather than 0: the session resolves
            // each rebuild's area from the player's position and falls back to this one, so a
            // fixture that started at 0 would not exercise the mainland case the way live does.
            Native950World.SceneConfig scene=new Native950World.SceneConfig(player.getX(),player.getY(),player.getPlane(),1,7,
                    Native950MapAreas.defaultAreaType(),0,0);
            session=new Native950Session(player,channel,transport,scene,content(table),null,null,null);
            manager().registerNativeOpen(1482,1477,30);
            manager().registerNativeOpen(1465,1477,94);
            player.getAppearence().generateAppearenceData();
            require(player.getAppearence().getAppeareanceData()!=null,"Actual950 appearance generation failed");
            Native950World.SceneConfig entry=new Native950World.SceneConfig(player.getX(),player.getY(),player.getPlane(),1,7,
                    Native950MapAreas.areaTypeFor(player.getX(),player.getY(),scene.areaType),scene.hash1,scene.hash2);
            for(Native950Packets.Packet packet:entities.admit(player,entry,Collections.singletonList(player)))channel.write(packet);
            player.consumeNative950MapRefresh(); // initial REBUILD_NORMAL above already publishes the first scene.
            session.ready(entities);drain();
            for(Native950Lodestones.Destination destination:Native950Lodestones.destinations())
                require(packetIndex(0,Native950Packets.varbitLarge(destination.unlockVarbit,destination.unlockValue))>=0,
                        "Login omitted unlock presentation for "+destination.name);
            require(find(0,ServerPacket.IF_OPENSUB,1477,735,1092)<0,"Login unexpectedly opened lodestone UI");
            output.clear();sceneCloses=0;
        }
        int packetIndex(int start,Native950Packets.Packet expected) {
            for(int i=start;i<output.size();i++)if(output.get(i).kind==expected.type()
                    && java.util.Arrays.equals(output.get(i).body,expected.payload()))return i;
            return -1;
        }
        InterfaceManager manager(){return player.getInterfaceManager();}
        void home(){button(1465,34,-1);}
        void button(int panel,int component,int slot){
            int hash=(panel<<16)|component;
            send(18,new byte[]{-1,-1,-1,(byte)(hash>>>16),(byte)(hash>>>24),(byte)hash,(byte)(hash>>>8),(byte)(slot>>>8),(byte)slot});
        }
        void walk(WorldTile tile){
            send(88,new byte[]{(byte)(tile.getY()>>>8),(byte)tile.getY(),(byte)128,(byte)(tile.getX()+128),(byte)(tile.getX()>>>8)});
        }
        void send(int opcode,byte[] body){
            ByteBuf bytes=Unpooled.buffer(body.length+1);
            bytes.writeByte((opcode+inputCipher.getAsInt())&255);bytes.writeBytes(body);
            channel.writeInbound(bytes);channel.runPendingTasks();
            long before=session.snapshot().actionsDrained;
            session.tickInput();
            require(session.snapshot().actionsDrained==before+1,"Exactly one encrypted950 action must reach session input");
            actions++;drain();assertHealthy();
        }
        void tick(){
            ticks++;WorldTasksManager.processTasks();session.tickInput();session.tickMove();
            entities.beginFrames(Collections.singletonList(player));
            session.tickFrame(entities,Collections.emptyList(),Collections.singletonList(player));session.tickEnd();drain();
        }
        void assertOpen(){require(manager().containsNative950Lodestones()
                && manager().getInterfaceParentId(1092)==hash(1477,735),"Native lodestone owner missing at1477:735");}
        void assertClosed(){require(!manager().containsNative950Lodestones()&&!manager().containsInterface(1092),"Lodestone owner remained open");}
        void assertScene(){require(manager().getInterfaceParentId(1482)==hash(1477,30)
                && !manager().containsWorldMapInterface(),"World scene1482 was lost or not restored");}
        void assertHealthy(){
            channel.checkException();Native950Session.Snapshot state=session.snapshot();
            require(channel.isActive()&&transport.terminalFailure()==null,"Native950 transport disconnected: "+transport.terminalFailure());
            require(transport.pendingActions()==0&&transport.unhandledFrameCount()==0,"Native950 input did not decode/drain");
            require(state.tickFailures==0&&state.facadeStrictHits==0&&state.interactions.handlerFailures==0,
                    "Session failed while processing lodestones: "+state);
            require(Native950EntityMasks.refusals()==initialMaskRefusals,"Native teleport effect mask was refused");
        }
        int findKind(int start,ServerPacket kind){for(int i=Math.max(0,start);i<output.size();i++)if(output.get(i).kind==kind)return i;return -1;}
        int find(int start,ServerPacket kind,int panel,int component,int child){
            for(int i=start;i<output.size();i++){Frame frame=output.get(i);if(frame.kind==kind&&frame.parent()==hash(panel,component)
                    &&(child<0||frame.child()==child))return i;}return -1;
        }
        String recentMessages(int start){
            StringBuilder text=new StringBuilder();
            for(int i=start;i<output.size();i++)if(output.get(i).kind==ServerPacket.MESSAGE_GAME)
                text.append(new String(output.get(i).body,java.nio.charset.StandardCharsets.ISO_8859_1)).append(' ');
            return text.toString();
        }
        void drain(){
            channel.flush();channel.runPendingTasks();Object message;
            while((message=channel.readOutbound())!=null)try{
                require(message instanceof ByteBuf,"Native950 transport did not emit framed bytes");ByteBuf bytes=(ByteBuf)message;
                while(bytes.isReadable()){
                    int opcode=(bytes.readUnsignedByte()-outputCipher.getAsInt())&255;
                    if(opcode>=128)opcode=((opcode-128)<<8)|((bytes.readUnsignedByte()-outputCipher.getAsInt())&255);
                    ServerPacket kind=null;for(ServerPacket row:ServerPacket.values())if(row.opcode()==opcode){kind=row;break;}
                    require(kind!=null,"Unknown950 opcode/cipher mismatch "+opcode);
                    int length=kind.size();if(length==-1)length=bytes.readUnsignedByte();else if(length==-2)length=bytes.readUnsignedShort();
                    require(length>=0&&length<=bytes.readableBytes(),"Invalid950 frame length for "+kind);
                    byte[] body=new byte[length];bytes.readBytes(body);Frame frame=new Frame(kind,body);output.add(frame);frames++;
                    if(kind==ServerPacket.IF_CLOSESUB&&frame.parent()==hash(1477,30))sceneCloses++;
                }
            }finally{ReferenceCountUtil.release(message);}
            channel.checkException();
        }
        public void close(){entities.release(player);session.close();channel.finishAndReleaseAll();}
    }
    private static Native950Content content(Native950Bindings table){
        Native950ItemCatalog items=new Native950ItemCatalog(Collections.<Native950ItemCatalog.Entry>emptyList()).withLegacyDrops();
        int item=table.componentHash("bank","items"),inventory=table.componentHash("bank","inventory");
        int close=table.componentHash("bank","close"),deposit=table.componentHash("bank","deposit_all");
        return new Native950Content(items,new Native950Content.BankUi(item>>>16,item&65535,inventory&65535,
                close&65535,deposit&65535,new int[11],new int[11],Collections.emptyList(),Collections.emptyList()));
    }
    private static final class Frame{
        final ServerPacket kind;final byte[] body;Frame(ServerPacket kind,byte[] body){this.kind=kind;this.body=body;}
        int parent(){if(kind==ServerPacket.IF_OPENSUB)return i32be(body,0);
            if(kind==ServerPacket.IF_CLOSESUB)return((body[1]&255)<<24)|((body[0]&255)<<16)|((body[3]&255)<<8)|(body[2]&255);return-1;}
        int child(){return kind==ServerPacket.IF_OPENSUB?((body[16]-128)&255)|((body[17]&255)<<8):-1;}
    }
    private static int hash(int panel,int component){return(panel<<16)|component;}
    private static int i32be(byte[] b,int offset){return(b[offset]&255)<<24|(b[offset+1]&255)<<16|(b[offset+2]&255)<<8|(b[offset+3]&255);}
    private static String tile(WorldTile tile){return tile.getX()+","+tile.getY()+","+tile.getPlane();}
    private static void require(boolean condition,String message){if(!condition)throw new AssertionError(message);}
}
