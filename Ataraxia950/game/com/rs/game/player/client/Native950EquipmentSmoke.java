package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.route.RouteFinder;
import com.rs.game.route.strategy.FixedTileStrategy;
import com.rs.network.modern.Native950Isaac;
import com.rs.network.protocol.modern950.Native950PlayerInfo;
import com.rs.network.protocol.modern950.Native950Protocol.ServerPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.ReferenceCountUtil;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/** Opt-in encrypted equipment/appearance probe; all saved profiles live in a new build directory. */
public final class Native950EquipmentSmoke {
    private static final String PROFILE = "equipprobe";
    private static final int X = 3217, Y = 3258;
    private static final int[] WEAR_POSITIONS = {0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,1,0};
    /**
     * M4: the mask bits {@code Native950EntityMasks} can produce from real entity state -
     * appearance 0x4, animation 0x40, face entity 0x20, face angle 0x80, force talk 0x10000,
     * hits 0x8 - plus the three header extension markers 0x1 / 0x4000 / 0x40000. Anything else
     * in a header means an unsourced or CANDIDATE block reached the wire.
     */
    private static final int SOURCED_MASKS = 0x4|0x40|0x20|0x80|0x10000|0x8|0x1|0x4000|0x40000;

    public static void main(String[] args) throws Exception {
        if (args.length == 3 && args[0].equals("--restart-check")) {
            Cache.initFlatReadOnly(Paths.get(args[1]));
            checkRestored(new Native950SaveStore(Paths.get(args[2])), true);
            return;
        }
        if (args.length != 1) throw new IllegalArgumentException("Usage: Native950EquipmentSmoke <flat-cache-directory>");
        Path cache = Paths.get(args[0]).toAbsolutePath().normalize();
        Cache.initFlatReadOnly(cache);
        Native950InteractionsSmoke.captureLegacyDispatchBaseline();
        // RouteFinder is shared mutable state; validate this one-step test before starting its owner thread.
        World.getRegion(new WorldTile(X, Y, 0).getRegionId(), true);
        require(World.isFloorFree(0, X, Y) && World.isFloorFree(0, X, Y + 1), "Equipment smoke needs real walkable floor");
        require(RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER, X, Y, 0, 1,
                new FixedTileStrategy(X, Y + 1), false) == 1 && !RouteFinder.lastIsAlternative(),
                "Equipment movement test must have an exact adjacent route");
        Path directory = Files.createTempDirectory(Files.createDirectories(Paths.get("build")), "equipment947-smoke-");
        Native950SaveStore store = new Native950SaveStore(directory.resolve("profiles"));
        Native950World world = Native950World.getInstance();
        try (Connection c = new Connection(store)) {
            Native950Session.Snapshot state = c.await(s -> s.ticks >= 1 && s.npc.visible, "initial banker and equipment bootstrap");
            assertTotals(state, false);
            c.assertEquipment(state, emptyEquipment());
            require(!state.interactions.equipmentKitClaimed, "Fresh profile must have an unclaimed equipment kit");
            long transactions = state.interactions.transactions;
            c.npc(4, state.npc.index);
            state = c.transaction(transactions);
            assertTotals(state, true);
            require(state.interactions.equipmentKitClaimed, "Collect must persist its claim flag");
            long rejected = state.interactions.rejectedActions;
            c.npc(4, state.npc.index);
            state = c.rejected(rejected);
            assertTotals(state, true);

            transactions = state.interactions.transactions;
            int swordSlot = slotOf(state.interactions.inventory, 1277);
            c.button(2, 1473, 5, swordSlot, 1277);
            state = c.transaction(transactions);
            int[] worn = emptyEquipment(); worn[3] = 1277;
            c.assertEquipment(state, worn);
            rejected = state.interactions.rejectedActions;
            c.button(2, 1473, 5, swordSlot, 1277);
            state = c.rejected(rejected);
            c.assertEquipment(state, worn);
            transactions = state.interactions.transactions;
            c.button(2, 1473, 5, slotOf(state.interactions.inventory, 1173), 1173);
            state = c.transaction(transactions);
            worn[5] = 1173;
            c.assertEquipment(state, worn);

            transactions = state.interactions.transactions;
            long movingAppearance = c.movingAppearances;
            long appearanceFrames = c.appearances;
            c.walkAndWear(X, Y + 1, slotOf(state.interactions.inventory, 1139), 1139);
            state = c.transaction(transactions);
            worn[0] = 1139;
            // P5 assertion change (was: "one combined movement/appearance frame").
            // The wear is now performed by the 910 InventoryOptionsHandler, which
            // batches switches through Player.getSwitchItemCache() and runs
            // ButtonHandler.sendWear from a WorldTasksManager task, so the worn set
            // changes on the FOLLOWING tick -- exactly as it does for a legacy 910
            // player. The walk step is still applied on the click tick. The two
            // therefore land in two frames (a movement frame, then an appearance
            // frame) instead of one; assertEquipment below still proves the
            // appearance frame carries the helmet, so nothing about the visible
            // result changed. Restoring a single frame would mean bypassing the 910
            // handler again, which is what P5 removes.
            c.assertEquipment(state, worn);
            require(state.x == X && state.y == Y + 1,
                    "The walk step of a combined walk+wear client frame must still be applied on the click tick");
            require(c.appearances == appearanceFrames + 1 && c.movingAppearances == movingAppearance,
                    "The 910 wear must publish exactly one appearance frame, on the tick after the movement frame");
            assertTotals(state, true);
            System.out.println("PASS: encrypted one-time Collect, three native wear operations, stale rejection"
                    + " and the 910 deferred wear published as its own appearance frame");

            transactions = state.interactions.transactions;
            c.button(1, 1462, 31, 5, 1173);
            state = c.transaction(transactions);
            worn[5] = -1;
            c.assertEquipment(state, worn);
            c.npc(1, state.npc.index);
            state = c.await(s -> s.interactions.bankOpen, "bank after removing shield");
            transactions = state.interactions.transactions;
            // Native bank scripts clear the exhausted backpack actor before its sender reads the ID.
            c.button(1, 517, 15, slotOf(state.interactions.inventory, 1173), -1);
            state = c.transaction(transactions);
            require(total(state.interactions.bank,1173) == 1 && total(state.interactions.inventory,1173) == 0,
                    "Removed equipment must deposit into the real bank");
            transactions = state.interactions.transactions;
            // The last exhausted bank group reports the native empty-group sentinel.
            c.button(1, 517, 201, slotOf(state.interactions.bank,1173), 48447);
            state = c.transaction(transactions);
            require(total(state.interactions.bank,1173) == 0 && total(state.interactions.inventory,1173) == 1,
                    "Banked equipment must withdraw exactly once");
            c.button(1, 517, 317, -1, -1);
            state = c.await(s -> !s.interactions.bankOpen, "bank close before wearing shield");
            transactions = state.interactions.transactions;
            c.button(2, 1473, 5, slotOf(state.interactions.inventory,1173), 1173);
            state = c.transaction(transactions);
            worn[5] = 1173;
            c.assertEquipment(state, worn);
            assertTotals(state, true);
            require(state.x == X && state.y == Y && state.unhandledFrames == 0,
                    "Bank route should return to the saved adjacent tile using only verified input");
            Native950Save saved = store.load(PROFILE);
            require(saved != null && saved.equipmentKitClaimed() && Arrays.equals(worn,saved.equipmentIds()),
                    "Accepted worn state and kit claim must already be durably saved");
            Native950InteractionsSmoke.reportRouting("equipment", state, 0, 0, 0, 7);
            System.out.println("PASS: native Remove, gear bank deposit/withdraw, re-equip, actual container output and durable item conservation");
        }
        require(world.reservedSlots() == 0 && World.getPlayers().isEmpty() && World.getNPCs().isEmpty(),
                "Equipment disconnect must release the real player and banker");
        checkRestored(store, false);
        Process restarted = new ProcessBuilder(Paths.get(System.getProperty("java.home"), "bin", "java.exe").toString(),
                "-Xmx2g", "-cp", System.getProperty("java.class.path"), Native950EquipmentSmoke.class.getName(),
                "--restart-check", cache.toString(), directory.resolve("profiles").toAbsolutePath().toString()).inheritIO().start();
        if (!restarted.waitFor(35, TimeUnit.SECONDS)) {
            restarted.destroyForcibly(); throw new AssertionError("Fresh-JVM equipment restore timed out");
        }
        require(restarted.exitValue() == 0, "Fresh-JVM equipment persistence failed");
        System.out.println("Isolated equipment smoke profile: " + directory.toAbsolutePath());
    }

    private static void checkRestored(Native950SaveStore store, boolean freshJvm) throws Exception {
        Native950World world = Native950World.getInstance();
        try (Connection c = new Connection(store)) {
            Native950Session.Snapshot state = c.await(s -> s.ticks >= 1 && s.npc.visible, "restored worn equipment and banker");
            int[] worn = emptyEquipment(); worn[0] = 1139; worn[3] = 1277; worn[5] = 1173;
            c.assertEquipment(state,worn);
            require(c.appearances == 1 && Arrays.equals(c.initialAppearanceEquipment,worn),
                    "Reconnect must include worn gear in the initial native appearance, without a corrective second appearance");
            require(state.x == X && state.y == Y && !state.interactions.bankOpen && state.interactions.equipmentKitClaimed,
                    "Equipment profile must restore its position and claim while banking starts closed");
            assertTotals(state,true);
            long rejected = state.interactions.rejectedActions;
            c.npc(4,state.npc.index);
            state = c.rejected(rejected);
            assertTotals(state,true);
            require(state.unhandledFrames == 0,"Restored profile used an unverified input decoder");
        }
        require(world.reservedSlots() == 0 && World.getPlayers().isEmpty() && World.getNPCs().isEmpty(),
                "Restored equipment character and banker must both clean up");
        System.out.println("PASS: " + (freshJvm ? "fresh-JVM" : "same-JVM")
                + " equipped-profile restore, correct first appearance, no kit duplication and session cleanup");
    }

    private static Native950Content content() {
        Native950ItemCatalog items = new Native950ItemCatalog(Arrays.asList(
                new Native950ItemCatalog.Entry(995,"Coins",true,new String[]{"Add to pouch"}),
                new Native950ItemCatalog.Entry(1511,"Logs",false,new String[]{"Craft"}),
                new Native950ItemCatalog.Entry(315,"Shrimps",false,new String[]{"Eat"}),
                new Native950ItemCatalog.Entry(1277,"Bronze sword",false,new String[]{null,"Wield"},3,2),
                new Native950ItemCatalog.Entry(1173,"Bronze square shield",false,new String[]{null,"Wield"},5,2),
                new Native950ItemCatalog.Entry(1139,"Bronze med helm",false,new String[]{null,"Wear"},0,2)));
        int[] amounts = new int[11];
        amounts[1] = amounts[2] = 1; amounts[3] = 5; amounts[4] = 10; amounts[7] = Integer.MAX_VALUE;
        return new Native950Content(items, new Native950Content.BankUi(517,201,15,317,39,
                amounts,amounts,Collections.emptyList(),Collections.emptyList()),
                new Native950Content.BankerNpc(494,"Banker",X,Y-1,0,1,1,3,4),
                new Native950Content.EquipmentUi(1462,31,94,Collections.emptyList()),new Native950Appearance(WEAR_POSITIONS));
    }

    private static final class Connection implements AutoCloseable {
        final Native950World world = Native950World.getInstance();
        final EmbeddedChannel channel = new EmbeddedChannel();
        final Native950Isaac client = new Native950Isaac(new int[]{947,3,2026,1277});
        final Native950Isaac output = new Native950Isaac(new int[]{997,53,2076,1327});
        long appearances, movingAppearances;
        int playerFramesThisTick, lastMovingDx, lastMovingDy;
        int[] lastAppearanceEquipment, initialAppearanceEquipment, lastContainerEquipment;

        Connection(Native950SaveStore store) throws Exception {
            Native950Isaac incoming = new Native950Isaac(new int[]{947,3,2026,1277});
            Native950Isaac outgoing = new Native950Isaac(new int[]{997,53,2076,1327});
            for(int i=0;i<3;i++) { client.getAsInt(); incoming.getAsInt(); }
            for(int i=0;i<2;i++) { output.getAsInt(); outgoing.getAsInt(); }
            CompletableFuture<Native950Session> future = world.attach(channel,PROFILE,incoming,outgoing,new byte[]{0},
                    new Native950World.SceneConfig(X,Y,0,1,7,0,0,0),Collections.emptyList(),content(),store);
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
            while(!future.isDone() && System.nanoTime()<deadline) { channel.runPendingTasks(); Thread.sleep(20); }
            future.get(1,TimeUnit.SECONDS);
        }

        void npc(int option,int index) {
            input(new int[]{88,115,33,60,123,104}[option-1],new byte[]{(byte)(index>>>8),(byte)index,(byte)128});
        }
        void button(int option,int face,int component,int slot,int item) {
            input(buttonOpcode(option),buttonBody(face,component,slot,item));
        }
        void walkAndWear(int x,int y,int slot,int item) {
            ByteBuf bytes = Unpooled.buffer(15);
            bytes.writeByte(3+client.getAsInt()).writeBytes(new byte[]{(byte)(y>>>8),(byte)(y+128),(byte)(x>>>8),(byte)x,(byte)128});
            bytes.writeByte(buttonOpcode(2)+client.getAsInt()).writeBytes(buttonBody(1473,5,slot,item));
            channel.writeInbound(bytes);
        }
        private static int buttonOpcode(int option) { return new int[]{96,77,4,95,29,51,5,21,18,36}[option-1]; }
        private static byte[] buttonBody(int face,int component,int slot,int item) {
            int hash=(face<<16)|component;
            return new byte[]{(byte)(item>>>8),(byte)item,(byte)(hash>>>8),(byte)hash,
                    (byte)(hash>>>24),(byte)(hash>>>16),(byte)(slot>>>8),(byte)slot};
        }
        void input(int opcode,byte[] body) {
            require(channel.isActive(),"Input requires an active equipment connection");
            channel.writeInbound(Unpooled.wrappedBuffer(new byte[]{(byte)(opcode+client.getAsInt())}));
            channel.writeInbound(Unpooled.wrappedBuffer(body));
        }
        Native950Session.Snapshot transaction(long previous) throws Exception {
            Native950Session.Snapshot state = await(s -> s.interactions.transactions>previous,"equipment transaction");
            require(state.interactions.transactions==previous+1,"Exactly one requested transaction must occur");
            return state;
        }
        Native950Session.Snapshot rejected(long previous) throws Exception {
            return await(s -> s.interactions.rejectedActions>previous,"invalid or repeated equipment action");
        }
        Native950Session.Snapshot await(Predicate<Native950Session.Snapshot> test,String description) throws Exception {
            long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(10);
            Native950Session.Snapshot state;
            do {
                pump(); state=world.snapshot().get(2,TimeUnit.SECONDS); pump();
                require(state!=null && state.active && state.interactions!=null && state.npc!=null,"Equipment smoke lost its active player");
                if(test.test(state)) return state;
                Thread.sleep(20);
            } while(System.nanoTime()<deadline);
            throw new AssertionError("Timed out: "+description+"; last="+state);
        }
        void assertEquipment(Native950Session.Snapshot state,int[] expected) {
            require(Arrays.equals(expected,state.interactions.equipment.ids),"Authoritative worn equipment differs from expected");
            require(Arrays.equals(expected,lastContainerEquipment),"Native equipment container94 differs from authoritative state");
            require(Arrays.equals(expected,lastAppearanceEquipment),"Native player appearance differs from authoritative worn items");
        }
        void pump() throws Exception {
            channel.runPendingTasks();
            Object item;
            while((item=channel.readOutbound())!=null) {
                try {
                    require(item instanceof ByteBuf,"Native output must be framed bytes");
                    ByteBuf bytes=(ByteBuf)item;
                    while(bytes.isReadable()) {
                        int opcode=(bytes.readUnsignedByte()-output.getAsInt())&255;
                        if(opcode>=128) opcode=((opcode-128)<<8)|((bytes.readUnsignedByte()-output.getAsInt())&255);
                        ServerPacket packet=null;
                        for(ServerPacket candidate:ServerPacket.values()) if(candidate.opcode()==opcode) packet=candidate;
                        require(packet!=null,"Unknown or cipher-misaligned output opcode "+opcode);
                        int length=packet.size();
                        if(length==-1) length=bytes.readUnsignedByte(); else if(length==-2) length=bytes.readUnsignedShort();
                        require(length<=bytes.readableBytes(),"Truncated native output frame");
                        byte[] body=new byte[length]; bytes.readBytes(body);
                        if(packet==ServerPacket.PLAYER_INFO) playerOutput(body);
                        if(packet==ServerPacket.UPDATE_INV_FULL) containerOutput(body);
                        if(packet==ServerPacket.SERVER_TICK_END) {
                            require(playerFramesThisTick==1,"Every native tick must contain exactly one combined player update");
                            playerFramesThisTick=0;
                        }
                    }
                } finally { ReferenceCountUtil.release(item); }
            }
            channel.checkException();
        }
        private void containerOutput(byte[] body) throws Exception {
            try(DataInputStream data=new DataInputStream(new ByteArrayInputStream(body))) {
                if(data.readUnsignedShort()!=94) return;
                require(data.readUnsignedByte()==0 && data.readUnsignedShort()==19,"Equipment container must use primary19-slot storage");
                int[] equipment=new int[19];
                for(int slot=0;slot<19;slot++) {
                    equipment[slot]=data.readUnsignedShort()-1;
                    int amount=data.readUnsignedByte(); if(amount==255) amount=data.readInt();
                    require(amount==(equipment[slot]<0?0:1),"Worn native item quantities must be exactly one");
                }
                require(data.read()==-1,"Unexpected native equipment container tail");
                lastContainerEquipment=equipment;
            }
        }
        private void playerOutput(byte[] body) throws Exception {
            playerFramesThisTick++;
            require(body.length>=3,"Truncated PLAYER_INFO");
            boolean mask=(body[0]&0x40)!=0;
            // M4: the local pass now carries three movement forms, not one. Bits 5..4 of the
            // first byte are the 2-bit form selector the client reads at 0x1401258C5:
            // 1 = walk (3 direction bits + the extra-step flag, 0x1401259E8/0x1401259F8),
            // 2 = run (4 direction bits, 0x140125BB1), 3 = the absolute write whose next bit
            // chooses the 15-bit short form (0x140125D57) or the 30-bit long one (0x140125E89).
            // Forms 1 and 2 fit the first byte; the short absolute form spans three and the long
            // one five. Every pass rounds up to a byte boundary, so the external pass - still
            // exactly the verified 7F F4 skip span - starts right after.
            int form=(body[0]>>>4)&3;
            boolean longForm=form==3 && (body[0]&0x08)!=0;
            int localBytes=form==3?(longForm?5:3):1;
            int maskOffset=localBytes+2;
            require(body.length>=maskOffset && body[maskOffset-2]==0x7f && (body[maskOffset-1]&255)==0xf4,
                    "Player output must keep the verified external-player skip span: "+hex(body));
            if(!mask) { require(body.length==maskOffset,"Unexpected mask-free player tail: "+hex(body)); return; }
            // M4: the CONFIRMED masks are fed from real entity state, so an update block can
            // now carry more than the appearance - collecting the kit makes the character face
            // the banker, which is the face-angle block. The two skipped bytes are unchanged and
            // the header is 1..4 bytes, extended by the markers 0x1 / 0x4000 / 0x40000.
            require(body.length>=maskOffset+3 && body[maskOffset]==0 && body[maskOffset+1]==0,
                    "Every player mask block starts with the two bytes the client skips: "+hex(body));
            int headerAt=maskOffset+2;
            int maskBits=body[headerAt]&255, headerBytes=1;
            if((maskBits&0x1)!=0) { maskBits|=(body[headerAt+1]&255)<<8; headerBytes=2; }
            if((maskBits&0x4000)!=0) { maskBits|=(body[headerAt+2]&255)<<16; headerBytes=3; }
            if((maskBits&0x40000)!=0) { maskBits|=(body[headerAt+3]&255)<<24; headerBytes=4; }
            require((maskBits&~SOURCED_MASKS)==0,
                    "Only masks fed from confirmed entity state may reach the wire: "+hex(body));
            boolean appearance=(maskBits&0x4)!=0;
            // Appearance is the first block this port ever emits: the only block ahead of it in
            // the client's consumption order is force movement, which has no mask source.
            int appearanceAt=headerAt+headerBytes+1;
            int length=appearance?(((body[headerAt+headerBytes]&255)-128)&255):0;
            if(appearance) require(length>0 && body.length>=appearanceAt+length,"Native appearance length mismatch");
            if(maskBits==0x4) require(body.length==appearanceAt+length,
                    "An appearance-only update must carry nothing after the body: "+hex(body));
            if(form==3 && !longForm) {
                // The full twenty-bit update consists of five prefix bits and a fifteen-bit relative position.
                int bits=((body[0]&255)<<12)|((body[1]&255)<<4)|((body[2]&255)>>>4);
                int relative=bits&0x7fff;
                lastMovingDx=(relative>>>5)&31; if(lastMovingDx>=16) lastMovingDx-=32;
                lastMovingDy=relative&31; if(lastMovingDy>=16) lastMovingDy-=32;
                require((relative>>>12)==Native950PlayerInfo.MOVEMENT_WALK,
                        "Walking appearance must preserve its native relative-position mode");
                movingAppearances++;
            } else if(form==Native950PlayerInfo.FORM_WALK) {
                int direction=(body[0]>>>1)&0x7;
                require((body[0]&0x1)==0,"The walk form's extra-step flag must stay clear");
                require(resolveWalk(direction)!=null,"Walk direction "+direction+" is not in the verified table");
                lastMovingDx=resolveWalk(direction)[0];
                lastMovingDy=resolveWalk(direction)[1];
                movingAppearances++;
            } else if(form==Native950PlayerInfo.FORM_RUN) {
                int direction=body[0]&0x0F;
                require(resolveRun(direction)!=null,"Run direction "+direction+" is not in the verified table");
                lastMovingDx=resolveRun(direction)[0];
                lastMovingDy=resolveRun(direction)[1];
                movingAppearances++;
            }
            if(!appearance) return;
            try(DataInputStream data=new DataInputStream(new ByteArrayInputStream(body,appearanceAt,length))) {
                require(data.readUnsignedByte()==0 && data.readUnsignedByte()==0,"Unexpected player appearance flags");
                int[] equipment=emptyEquipment();
                int[] looks=new int[19];
                for(int slot=0;slot<19;slot++) {
                    if(WEAR_POSITIONS[slot]==1) continue;
                    int value=data.readUnsignedByte();
                    if(value!=0) value=(value<<8)|data.readUnsignedByte();
                    looks[slot]=value;
                    if(value>=0x800) equipment[slot]=value-0x800;
                }
                require(looks[8]==(equipment[0]==1139?0:0x103) && looks[11]==0x10e,
                        "Med helm must hide hair while retaining the beard body kit");
                require(data.readUnsignedShort()==0,"Unexpected customization in basic equipment appearance");
                for(int i=0;i<20;i++) data.readUnsignedByte();
                int hand = equipment[3] >= 0 ? equipment[3] : equipment[5];
                int expectedBas = hand < 0 ? 2699 : Native950EquipmentTypes.resolve(hand).bas;
                require(data.readUnsignedShort()==expectedBas,"Appearance BAS must follow the actual950 equipped combat profile");
                StringBuilder name=new StringBuilder();
                for(int value;(value=data.readUnsignedByte())!=0;) name.append((char)value);
                require(name.toString().equals(PROFILE),"Wrong appearance profile name");
                require(data.readUnsignedByte()==3 && data.readUnsignedByte()==0 && data.readUnsignedByte()==255
                        && data.readUnsignedByte()==0 && data.read()==-1,"Unexpected appearance tail");
                lastAppearanceEquipment=equipment;
                if(appearances++==0) initialAppearanceEquipment=equipment.clone();
            }
        }
        @Override public void close() throws Exception {
            channel.close();
            long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(5);
            while(world.reservedSlots()>0 && System.nanoTime()<deadline) { pump(); Thread.sleep(20); }
            pump(); channel.finishAndReleaseAll();
            require(world.reservedSlots()==0,"Equipment disconnect did not release the world reservation");
        }
    }

    private static int[] emptyEquipment() { int[] ids=new int[19]; Arrays.fill(ids,-1); return ids; }
    private static void assertTotals(Native950Session.Snapshot state,boolean kit) {
        int[] ids={995,1511,315,1277,1173,1139}, totals={1000,5,5,kit?1:0,kit?1:0,kit?1:0};
        for(int i=0;i<ids.length;i++) require(total(state.interactions.inventory,ids[i])+total(state.interactions.bank,ids[i])
                +total(state.interactions.equipment,ids[i])==totals[i],"Equipment flow changed the total amount of item "+ids[i]);
    }
    private static long total(Native950Containers.Snapshot container,int id) {
        long count=0;
        for(int slot=0;slot<container.ids.length;slot++) if(container.ids[slot]==id) count+=container.amounts[slot];
        return count;
    }
    private static int slotOf(Native950Containers.Snapshot container,int id) {
        for(int slot=0;slot<container.ids.length;slot++) if(container.ids[slot]==id) return slot;
        throw new AssertionError("Missing item "+id);
    }
    private static void require(boolean value,String message) { if(!value) throw new AssertionError(message); }
    private static String hex(byte[] body) { StringBuilder t=new StringBuilder(); for(byte b:body) t.append(String.format("%02x ",b&255)); return t.toString().trim(); }
    /**
     * The tile delta a 3-bit walk direction stands for, or null when the index has no entry.
     * Inverted from {@link Native950PlayerInfo#walkDirection} so the decoder and the encoder
     * cannot drift apart: the table itself lives in exactly one place.
     */
    private static int[] resolveWalk(int direction) {
        for(int dy=-1;dy<=1;dy++) for(int dx=-1;dx<=1;dx++)
            if(Native950PlayerInfo.walkDirection(dx,dy)==direction) return new int[]{dx,dy};
        return null;
    }
    /** The same for a 4-bit run direction, inverted from {@link Native950PlayerInfo#runDirection}. */
    private static int[] resolveRun(int direction) {
        for(int dy=-2;dy<=2;dy++) for(int dx=-2;dx<=2;dx++)
            if(Native950PlayerInfo.runDirection(dx,dy)==direction) return new int[]{dx,dy};
        return null;
    }
    private Native950EquipmentSmoke() { }
}
