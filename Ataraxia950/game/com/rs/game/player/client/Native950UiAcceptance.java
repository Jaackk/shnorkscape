package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.client.ui.Native950Bindings;
import com.rs.game.player.content.InterfaceManager;
import com.rs.network.modern.Native950GameTransport;
import com.rs.network.modern.Native950Isaac;
import com.rs.network.protocol.modern950.Native950Protocol.ServerPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.ReferenceCountUtil;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Real-cache, ephemeral settings/map/run acceptance. No authentication, listening socket,
 * world scheduler, account load/save, fake cache verifier or replacement InterfaceManager.
 * Exercises the 950 inbound decoder, real interaction route, packet facade and outbound
 * framing. This cannot execute clientscript bytecode or verify visual/audio preferences.
 */
public final class Native950UiAcceptance {
    private static final int[] INPUT_SEED = {950, 10, 71, 2026};
    private static final int[] OUTPUT_SEED = {1000, 60, 121, 2076};
    private final Native950Isaac input = new Native950Isaac(INPUT_SEED);
    private final Native950Isaac output = new Native950Isaac(OUTPUT_SEED);
    private final Native950GameTransport transport = new Native950GameTransport(
            new Native950Isaac(INPUT_SEED), new Native950Isaac(OUTPUT_SEED), Thread.currentThread());
    private final EmbeddedChannel channel = new EmbeddedChannel(transport);
    private final Player player;
    private final Native950Interactions interactions;
    private final List<Frame> frames = new ArrayList<Frame>();
    private int selectedPage, sceneCloses, actions;

    public static void main(String[] args) throws Exception {
        require(args.length == 1, "Usage: Native950UiAcceptance <950-flat-cache-directory>");
        require(NativeCacheVerification.isEnforced(), "Cache pin enforcement must be ON");
        Cache.initFlatReadOnly(Paths.get(args[0]));
        Native950Bindings table = Native950Bindings.tryLoad();
        require(table != null, "Real 950 UI bindings must load and verify");
        Native950Settings.verify();
        Native950WorldMap.verify();
        String previousMap = System.getProperty(Native950WorldMap.PROPERTY);
        System.setProperty(Native950WorldMap.PROPERTY, "true");
        Native950UiAcceptance probe = null;
        try {
            probe = new Native950UiAcceptance(table);
            probe.run();
        } finally {
            if (probe != null) {
                probe.interactions.close();
                probe.channel.finishAndReleaseAll();
            }
            if (previousMap == null) System.clearProperty(Native950WorldMap.PROPERTY);
            else System.setProperty(Native950WorldMap.PROPERTY, previousMap);
        }
    }

    private Native950UiAcceptance(Native950Bindings table) {
        player = Player.createNative950("ephemeral-ui-acceptance", new WorldTile(3217, 3258, 0), channel);
        player.setActive(true);
        interactions = new Native950Interactions(player, channel, content(table));
        manager().registerNativeOpen(1482, 1477, 30);
        manager().registerNativeOpen(1465, 1477, 94);
        drain(); // discard construction-only state before counting acceptance responses
        frames.clear(); sceneCloses = 0;
        interactions.bootstrap();
        drain();
        assertCompassHud();
    }

    private void run() {
        button(1431, 0, 7); // actual gear actor
        assertPage(2); assertScene();
        for (int page : new int[]{1, 6, 1, 3, 5, 2, 4}) {
            int start = frames.size();
            button(1477, 714, page * 4 - 1);
            assertPage(page); assertScene();
            int close = find(start, ServerPacket.IF_CLOSESUB, 1448, 3, -1);
            int select = findSelection(start, page);
            int open = find(start, ServerPacket.IF_OPENSUB, 1448, 3, pageInterface(page));
            require(close >= start && close < select && select < open,
                    "Page " + page + " must close old content, select page, then open controls");
            int repeat = frames.size();
            button(1477, 714, page * 4 - 1);
            assertPage(page);
            require(findSelection(repeat, page) >= repeat, "Repeated tab needs selection acknowledgment");
            for (int i = repeat; i < frames.size(); i++)
                require(frames.get(i).kind != ServerPacket.IF_OPENSUB && frames.get(i).kind != ServerPacket.IF_CLOSESUB,
                        "Repeated tab must not recreate controls");
        }
        for (int mute = 0; mute < 4; mute++) {
            int start = frames.size();
            button(429, 15 + 17 * mute, -1);
            require(script(start, 5523) >= start && script(start, 13818) > script(start, 5523),
                    "Audio mute must request the native setter followed by refresh");
            assertPage(4);
        }
        int start = frames.size();
        button(429, 7, -1);
        require(script(start, 9287) >= start, "Audio master control must request native script 9287");
        button(1477, 717, 1);
        assertClosed(); assertScene();
        for (int page = 1; page <= 6; page++) button(1477, 714, page * 4 - 1);
        assertClosed(); assertScene();
        require(sceneCloses == 0, "Settings must never close the scene");

        button(1477, 8, -1); // native root Options/Escape entry
        assertPage(2);
        button(1477, 8, -1);
        assertClosed(); assertScene();
        button(1431, 0, 7);
        button(1465, 11, -1);
        assertClosed();
        require(manager().containsWorldMapInterface()
                && manager().getInterfaceParentId(1421) == hash(1477, 31)
                && manager().getInterfaceParentId(1422) == hash(1477, 37)
                && !manager().containsInterface(1482), "Map must exclusively own both native map layers");
        require(sceneCloses == 1, "Map entry must close the scene exactly once");
        start = frames.size();
        button(1431, 0, 7);
        assertPage(2); assertScene();
        int sceneOpen = find(start, ServerPacket.IF_OPENSUB, 1477, 30, 1482);
        int settingsOpen = find(start, ServerPacket.IF_OPENSUB, 1477, 715, 1448);
        require(sceneOpen >= start && settingsOpen > sceneOpen,
                "Returning from map to settings must restore scene before management window");
        send(5, new byte[0]); // actual 950 CLOSE_MODAL
        assertClosed(); assertScene();
        button(1465, 11, -1);
        button(1422, 111, -1);
        assertClosed(); assertScene();
        require(sceneCloses == 2, "Only the two explicit map opens may close the scene");

        boolean originalRun = player.getRun();
        start = frames.size(); button(1465, 15, -1);
        require(player.getRun() != originalRun, "950 minimap 1465:15 must toggle actual engine run state");
        require(hasVarp(start, 463, player.getRun() ? 1 : 0), "Run toggle must emit native run-state varp 463");
        start = frames.size(); button(1465, 15, -1);
        require(player.getRun() == originalRun, "Second Run click must restore engine state");
        require(hasVarp(start, 463, originalRun ? 1 : 0), "Second toggle must refresh native run state");
        assertScene(); assertCompassHud(); healthy();
        require(interactions.router().handlerFailures() == 0, "No legacy handler may fail: " + interactions.router().report());
        System.out.println("PASS: real 950 cache pins and binding table; all six Settings tabs/repeats and native Audio controls");
        System.out.println("PASS: gear, root Options/Escape, modal close, map handoff and scene packet ordering");
        System.out.println("PASS: compass 1919 is mounted at 1465:12 as permanent HUD type 1; no Settings/map/modal-close route removes it");
        System.out.println("PASS: 1465:15 traverses native decoder/router and toggles engine Run twice with varp acknowledgment");
        System.out.println("PASS: " + actions + " encrypted local actions, " + frames.size() + " parsed outbound frames; channel remains active");
        System.out.println("LIMIT: no clientscript execution, rendering, audible mute, walking-speed measurement, or bank/NPC interaction is claimed");
    }

    private void button(int panel, int component, int slot) {
        int h = hash(panel, component);
        // Native 950 IF_BUTTON1 = 18, u24 item sentinel, hash [16,24,0,8], BE u16 slot.
        send(18, new byte[]{-1,-1,-1,(byte)(h>>>16),(byte)(h>>>24),(byte)h,(byte)(h>>>8),(byte)(slot>>>8),(byte)slot});
    }
    private void send(int opcode, byte[] body) {
        ByteBuf bytes = Unpooled.buffer(body.length + 1);
        bytes.writeByte((opcode + input.getAsInt()) & 255); bytes.writeBytes(body);
        channel.writeInbound(bytes);
        require(transport.drainActions(interactions::handle) == 1, "Exactly one decoded action must reach interactions");
        actions++; drain(); healthy();
    }
    private void healthy() {
        channel.checkException();
        require(channel.isActive() && transport.terminalFailure() == null,
                "Ephemeral channel disconnected: " + transport.terminalFailure());
        require(transport.pendingActions() == 0 && transport.unhandledFrameCount() == 0,
                "Every test input must decode and drain");
    }
    private void drain() {
        channel.flush(); channel.runPendingTasks();
        Object value;
        while ((value = channel.readOutbound()) != null) {
            try {
                require(value instanceof ByteBuf, "Packets must pass through native transport framing");
                ByteBuf bytes = (ByteBuf)value;
                while (bytes.isReadable()) {
                    int opcode = (bytes.readUnsignedByte() - output.getAsInt()) & 255;
                    if (opcode >= 128) opcode = ((opcode - 128) << 8) | ((bytes.readUnsignedByte() - output.getAsInt()) & 255);
                    ServerPacket kind = null;
                    for (ServerPacket candidate : ServerPacket.values()) if (candidate.opcode() == opcode) kind = candidate;
                    require(kind != null, "Unknown outbound opcode/cipher mismatch " + opcode);
                    int size = kind.size();
                    if (size == -1) size = bytes.readUnsignedByte(); else if (size == -2) size = bytes.readUnsignedShort();
                    require(size >= 0 && size <= bytes.readableBytes(), "Truncated outbound frame " + kind);
                    byte[] payload = new byte[size]; bytes.readBytes(payload);
                    Frame frame = new Frame(kind, payload); frames.add(frame);
                    if (kind == ServerPacket.VARBIT_SMALL && payload.length == 3 && u16le(payload,1) == 19001)
                        selectedPage = (payload[0] + 128) & 255;
                    if (kind == ServerPacket.IF_CLOSESUB && frame.parent() == hash(1477,30)) sceneCloses++;
                }
            } finally { ReferenceCountUtil.release(value); }
        }
    }
    private InterfaceManager manager() { return player.getInterfaceManager(); }
    private void assertScene() {
        require(manager().getInterfaceParentId(1482) == hash(1477,30)
                && !manager().containsWorldMapInterface() && !manager().containsInterface(1421)
                && !manager().containsInterface(1422), "Scene owner must remain/restored at 1477:30");
    }
    /** The native client removes type-0 children during its generic modal-close pass.
     * Decode the actual encrypted login packet instead of trusting a Java flag/name. */
    private void assertCompassHud() {
        int mount = find(0, ServerPacket.IF_OPENSUB, 1465, 12, 1919);
        require(mount >= 0, "Login bootstrap must mount compass 1919 at minimap 1465:12");
        for (int i=mount; i<frames.size(); i++) {
            Frame frame=frames.get(i);
            if (frame.parent()!=hash(1465,12)) continue;
            require(frame.kind!=ServerPacket.IF_CLOSESUB, "Settings/map cleanup must not close the compass");
            if (frame.kind==ServerPacket.IF_OPENSUB) {
                require(frame.child()==1919 && ((128-(frame.body[18]&255))&255)==1,
                        "Compass must use permanent HUD type 1, never closeable modal type 0");
            }
        }
    }

    private void assertClosed() {
        require(!manager().containsNative950Settings(), "Settings owner must be closed");
        for (int panel : new int[]{1448,1426,742,429,365,1444,567})
            require(!manager().containsInterface(panel), "Closed Settings retains panel " + panel);
    }
    private void assertPage(int page) {
        require(selectedPage == page, "Expected selected page " + page + ", got " + selectedPage);
        require(manager().containsNative950Settings() && manager().getInterfaceParentId(1448) == hash(1477,715), "Management owner missing");
        int panel = pageInterface(page);
        require(manager().getInterfaceParentId(panel) == hash(1448,3), "Wrong main content for page " + page);
        for (int other : new int[]{365,1444,567,1426,742,429}) {
            boolean expected = other == panel || page == 2 && other == 742 || page == 4 && other == 429;
            require(manager().containsInterface(other) == expected, "Wrong page ownership for " + other + " on page " + page);
        }
        if (page == 2) require(manager().getInterfaceParentId(742) == hash(1426,0), "Graphics renderer child missing");
        if (page == 4) require(manager().getInterfaceParentId(429) == hash(1448,5), "Audio column missing");
    }
    private int find(int start, ServerPacket kind, int panel, int component, int child) {
        for (int i=start; i<frames.size(); i++) {
            Frame f=frames.get(i);
            if (f.kind==kind && f.parent()==hash(panel,component) && (child<0 || f.child()==child)) return i;
        }
        return -1;
    }
    private int findSelection(int start, int page) {
        for(int i=start;i<frames.size();i++) { Frame f=frames.get(i);
            if(f.kind==ServerPacket.VARBIT_SMALL && f.body.length==3 && u16le(f.body,1)==19001 && ((f.body[0]+128)&255)==page) return i;
        } return -1;
    }
    private int script(int start,int id) {
        for(int i=start;i<frames.size();i++) { Frame f=frames.get(i);
            if(f.kind==ServerPacket.RUNCLIENTSCRIPT && f.body.length>=5 && i32be(f.body,f.body.length-4)==id) return i;
        } return -1;
    }
    private boolean hasVarp(int start,int id,int value) {
        for(int i=start;i<frames.size();i++) { Frame f=frames.get(i); byte[] b=f.body;
            if(f.kind==ServerPacket.VARP_SMALL && b.length==3 && (((b[1]&255)<<8)|((b[2]-128)&255))==id && b[0]==value) return true;
            if(f.kind==ServerPacket.VARP_LARGE && b.length==6 && (((b[1]&255)<<8)|((b[0]-128)&255))==id
                    && (((b[4]&255)<<24)|((b[5]&255)<<16)|((b[2]&255)<<8)|(b[3]&255))==value) return true;
        } return false;
    }
    private static Native950Content content(Native950Bindings table) {
        List<Native950ItemCatalog.Entry> items=new ArrayList<Native950ItemCatalog.Entry>();
        for(int id:new int[]{995,1511,315}) {
            ItemDefinitions def=ItemDefinitions.getItemDefinitions(id);
            require(def!=null && def.decodeFailure==null,"Starter metadata must decode from actual 950 cache: "+id);
            items.add(new Native950ItemCatalog.Entry(id,def.getName(),def.stackable!=0,
                    def.inventoryOptions==null ? new String[0] : def.inventoryOptions));
        }
        int item=table.componentHash("bank","items"), inventory=table.componentHash("bank","inventory");
        int close=table.componentHash("bank","close"), deposit=table.componentHash("bank","deposit_all");
        return new Native950Content(new Native950ItemCatalog(items),new Native950Content.BankUi(item>>>16,item&65535,
                inventory&65535,close&65535,deposit&65535,new int[11],new int[11],Collections.emptyList(),Collections.emptyList()));
    }
    private static final class Frame {
        final ServerPacket kind; final byte[] body;
        Frame(ServerPacket kind,byte[] body){this.kind=kind;this.body=body;}
        int parent(){
            if(kind==ServerPacket.IF_OPENSUB) return i32be(body,0);
            if(kind==ServerPacket.IF_CLOSESUB) return ((body[1]&255)<<24)|((body[0]&255)<<16)|((body[3]&255)<<8)|(body[2]&255);
            return -1;
        }
        int child(){return kind==ServerPacket.IF_OPENSUB ? ((body[16]-128)&255)|((body[17]&255)<<8) : -1;}
    }
    private static int pageInterface(int page){if(page==1||page==6)return 365;if(page==2||page==4)return 1426;if(page==3)return 1444;if(page==5)return 567;throw new AssertionError(page);}
    private static int hash(int panel,int component){return panel<<16|component;}
    private static int u16le(byte[] b,int p){return (b[p]&255)|((b[p+1]&255)<<8);}
    private static int i32be(byte[] b,int p){return (b[p]&255)<<24|(b[p+1]&255)<<16|(b[p+2]&255)<<8|(b[p+3]&255);}
    private static void require(boolean condition,String message){if(!condition)throw new AssertionError(message);}
}
