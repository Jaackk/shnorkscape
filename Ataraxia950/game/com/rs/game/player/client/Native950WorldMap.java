package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.Channel;
import java.security.MessageDigest;

/** Local world-map adapter. The cache owns pan, zoom, legend and search controls. */
public final class Native950WorldMap {
    public static final String PROPERTY = "ataraxia950.worldMap";
    private static final int VIEWPORT_HOST = 31;
    private static final int CONTROLS_HOST = 37;
    private static volatile boolean verified;
    private final Player player;
    private final Channel channel;
    private final Runnable verifier;
    private boolean open;
    private Runnable developerReturn;
    public void openDeveloper(Runnable back){
        if(!Native950DeveloperActions.permitted(player,channel))throw new IllegalArgumentException("Developer permission is required.");
        open();developerReturn=back;
    }

    Native950WorldMap(Player player, Channel channel) {
        this(player, channel, Native950WorldMap::verify);
    }

    /** Cache verification is injectable only for isolated package-level state tests. */
    Native950WorldMap(Player player, Channel channel, Runnable verifier) {
        this.player = player;
        this.channel = channel;
        this.verifier = java.util.Objects.requireNonNull(verifier, "verifier");
        player.getInterfaceManager().setNative950WorldMap(this);
    }

    /** Shared gameplay cleanup must distinguish the map from the scene at 1477:30. */
    public boolean isOpen() { return open; }

    static boolean isOpenRequest(Native950Actions.InterfaceAction action) {
        return Boolean.parseBoolean(System.getProperty(PROPERTY, "false"))
                && action.interfaceId() == 1465 && action.componentId() == 11 && action.option() == 1;
    }

    boolean handle(Native950Actions.InterfaceAction action) {
        if (isOpenRequest(action)) { open(); return true; }
        if (open && action.interfaceId() == 1422 && action.componentId() == 111 && action.option() == 1) {
            Runnable back=developerReturn;close();if(back!=null)back.run();
            return true;
        }
        // Unknown controls keep their existing unmatched-pair counter. Some have
        // client hooks, while others need server behavior that is not ported yet.
        return false;
    }

    private void open() {
        if (open) return;
        verifier.run(); // Nothing mutates until the selected cache agrees with the observed files.
        player.resetWalkSteps();
        player.setRouteEvent(null);
        channel.write(Native950Packets.varcLarge(622, player.getTileHash()));
        channel.write(Native950Packets.varcLarge(674, player.getTileHash()));
        // Both map layers must be siblings inside the game-view container.
        // An attachment on 30 covers its static children, including the map's
        // native input-hook layer 37. Leave 30 unattached while the map is open.
        channel.write(Native950Packets.closeSub(1477, 30));
        channel.write(Native950Packets.openSub(1477, VIEWPORT_HOST, 1421, true));
        // Cache script 343 installs wheel and gesture hooks on 37. Keep both
        // attachments permanent/walkable; close() explicitly retires the pair.
        channel.write(Native950Packets.openSub(1477, CONTROLS_HOST, 1422, true));
        player.getInterfaceManager().unregisterNativeOpen(1482);
        player.getInterfaceManager().registerNativeOpen(1421, 1477, VIEWPORT_HOST);
        player.getInterfaceManager().registerNativeOpen(1422, 1477, CONTROLS_HOST);
        open = true;
        System.out.println("[Ataraxia950] World map opened for player " + player.getIndex());
    }

    public void close() {
        developerReturn=null;
        if (!open) return;
        open = false;
        // The cache's close script restores its HUD flags and removes map input hooks.
        // Any resulting CLOSE_MODAL notification is harmless: state is already closed.
        channel.write(Native950Packets.runClientScript(1898));
        channel.write(Native950Packets.closeSub(1477, CONTROLS_HOST));
        channel.write(Native950Packets.closeSub(1477, VIEWPORT_HOST));
        channel.write(Native950Packets.openSub(1477, 30, 1482, true));
        player.getInterfaceManager().unregisterNativeOpen(1422);
        player.getInterfaceManager().unregisterNativeOpen(1421);
        player.getInterfaceManager().registerNativeOpen(1482, 1477, 30);
        System.out.println("[Ataraxia950] World map closed for player " + player.getIndex());
    }

    public static synchronized void verify() {
        if (verified) return;
        if (!Cache.isFlatReadOnly()) throw new IllegalStateException("World map requires the paired flat cache");
        pin(22, 664, 27, "1d3e4f9c1da82d9e3a576cd2f9e4996cf17e3440b620c29e269b31a3936a7c61"); // Game View struct 21275.
        pin(3, 1421, 0, "ea9359e9a148f967d2b52c37c0fba5d5b775c67ce7b2138e0c03cd41bfa9ac9f");
        pin(3, 1422, 0, "e0643c8e81aae11cf8c589d4ece24cc692c12b05a635f98d2a81bb4ed6fb7a19");
        pin(3, 1422, 111, "9e4dfa917c3921ba08e5c4c633492e582630fb03a1144345c2494f72e07e892a");
        pin(3, 1477, 27, "af6e31a36e133311dfedb970fe23c023efd8635d947d0541f9619c16e866c838");
        pin(3, 1477, 28, "35b43eacc2ab9c2cb8005854d715d688fa6933043683192ea7189208f5b51c2d");
        pin(3, 1477, 30, "bc175c4458b57c05230e398377b7d7725ee403fae1ac23f8445f2f8479b82598");
        pin(3, 1477, 31, "48b692fb63d054cd96fc71ae0e939ef6b635dab620467d9c39214dd09f9d65c6");
        pin(3, 1477, 37, "ff11312f9c9e080a4ca98211e4c6357abe4028d4203302171fde1babb93f76de");
        pin(3, 1465, 11, "7c8b227a1ebffc100be5728550972ce77a1add8401f97c63e43b13fa4eb3c45b");
        pin(12, 1369, 0, "e455b63442c739606d9d301e2f6713785e74ffa073de864a586c54049236af94");
        pin(12, 343, 0, "0721f17e4a3befcf6a466c6012f8be56362c6bc638feb464d767076e70f003ce");
        pin(12, 10420, 0, "11bf251ddafddb3ffa2eca9f8cf339d6462d76a3e2a22d68f0dfe41d7d5ece3b");
        pin(12, 1898, 0, "34ee0c94d21c345357d6c6db9a1d2ee571a07bc50089ab3988dab35e502713aa");
        pin(12, 8105, 0, "f1cf5757548ce7a7c19baeda48088f28f3b5fcd3d1eae3933f9acfa00de8fd0b");
        verified = true;
    }

    private static void pin(int index, int group, int file, String expected) {
        try {
            byte[] data = Cache.STORE.getIndexes()[index].getFile(group, file);
            if (data == null) throw new IllegalStateException("Missing map file " + index + "/" + group + "/" + file);
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(data);
            StringBuilder actual = new StringBuilder();
            for (byte b : digest) actual.append(String.format("%02x", b & 255));
            NativeCacheVerification.requireBinding("World-map",
                    index + "/" + group + "/" + file, expected, actual.toString());
        } catch (java.security.NoSuchAlgorithmException impossible) {
            throw new AssertionError(impossible);
        }
    }
}
