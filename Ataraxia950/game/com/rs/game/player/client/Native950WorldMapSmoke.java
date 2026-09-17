package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.embedded.EmbeddedChannel;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

/** Paired-cache map lifecycle probe; no server sockets or account saves. */
public final class Native950WorldMapSmoke {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Pass the paired flat cache directory");
        Cache.initFlatReadOnly(Paths.get(args[0]));
        Native950WorldMap.verify();
        System.setProperty(Native950WorldMap.PROPERTY, "true");
        EmbeddedChannel channel = new EmbeddedChannel();
        try {
            Player player = Player.createNative950("world-map-smoke", new WorldTile(3217, 3258, 0), channel);
            player.setActive(true);
            player.getInterfaceManager().registerNativeOpen(1482, 1477, 30);
            Native950ItemCatalog items = new Native950ItemCatalog(Arrays.asList(
                    new Native950ItemCatalog.Entry(995, "Coins", true, new String[] {"Add to pouch"}),
                    new Native950ItemCatalog.Entry(1511, "Logs", false, new String[] {"Craft"}),
                    new Native950ItemCatalog.Entry(315, "Shrimps", false, new String[] {"Eat"})));
            int[] amounts = new int[11]; amounts[1] = 1;
            Native950Interactions interactions = new Native950Interactions(player, channel,
                    new Native950Content(items, new Native950Content.BankUi(517, 201, 15, 317, 39,
                            amounts, amounts, Collections.emptyList(), Collections.emptyList())));
            interactions.handle(button(1465, 11));
            require(player.getInterfaceManager().containsInterface(1422), "map open bookkeeping");
            require(drain(channel) == 5, "map open burst has two varcs, removed scene and two sibling attachments");
            interactions.handle(button(1465, 11));
            require(drain(channel) == 0, "duplicate open is idempotent");
            interactions.handle(button(1422, 112));
            require(player.getInterfaceManager().containsInterface(1422) && drain(channel) == 0,
                    "a map control cannot close the map");
            require(interactions.snapshot().unmatchedPairs == 1,
                    "an unknown map control must retain its unmatched-pair diagnostic");
            interactions.handle(button(1422, 111));
            require(player.getInterfaceManager().containsInterface(1482)
                    && !player.getInterfaceManager().containsInterface(1422), "explicit close restores game view");
            require(drain(channel) == 4, "close burst removes both map layers and restores 1482");
            interactions.handle(button(1465, 11));
            drain(channel);
            // Close modal is 950 opcode 5 (size 0); 947 used 55, which on 950 is a
            // variable-length row belonging to something else entirely.
            interactions.handle(Native950Actions.decode(5, new byte[0]));
            require(!player.getInterfaceManager().containsInterface(1422)
                    && player.getInterfaceManager().containsInterface(1482), "CLOSE_MODAL restores game view");
            require(!interactions.snapshot().bankOpen && interactions.snapshot().unhandledActions == 0,
                    "CLOSE_MODAL leaves bank closed and is handled");
            interactions.afterMovement();
            require(!player.getInterfaceManager().containsInterface(1422), "map stays closed next tick");
            drain(channel);
            System.setProperty(Native950WorldMap.PROPERTY, "false");
            interactions.handle(button(1465, 11));
            require(!player.getInterfaceManager().containsInterface(1422) && drain(channel) == 0,
                    "disabled map flag produces no map packets or player error");
            System.out.println("PASS: paired-cache map pins, open/idempotence, client controls, close/reopen, 1482 restore, CLOSE_MODAL and disabled flag");
        } finally { channel.finishAndReleaseAll(); }
    }

    private static int drain(EmbeddedChannel channel) {
        channel.flush(); int count = 0;
        Object packet;
        while ((packet = channel.readOutbound()) != null) {
            require(packet instanceof Native950Packets.Packet, "expected native packet");
            count++;
        }
        return count;
    }
    /**
     * One IF_BUTTON option-1 frame as a 950 client sends it: opcode 18, nine bytes.
     *
     * b0..b2 are the item id as a big-endian u24 - widened from 947's u16, which is why the frame
     * grew by a byte - and 0xFFFFFF means absent. b3..b6 carry the component hash under the intv2
     * permutation [h>>>16, h>>>24, h, h>>>8]; that order is the one the live-verified 950 decoder
     * in OpenNXT's Native950WorldBootstrap.readInterfaceButton reads back, and plain big-endian,
     * little-endian and the 947 order all resolve to components the 950 cache does not define.
     * b7..b8 are the dynamic slot, big-endian, 0xFFFF for absent.
     */
    private static Native950Actions.InterfaceAction button(int panel, int component) {
        int hash = (panel << 16) | component;
        return (Native950Actions.InterfaceAction) Native950Actions.decode(18, new byte[] {
                -1, -1, -1,
                (byte) (hash >>> 16), (byte) (hash >>> 24), (byte) hash, (byte) (hash >>> 8),
                -1, -1});
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    private Native950WorldMapSmoke() { }
}
