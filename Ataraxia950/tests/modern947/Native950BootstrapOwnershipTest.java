package com.rs.game.player.client;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public final class Native950BootstrapOwnershipTest {
    @Test public void actualHudMountsAreMirroredWithoutInventingMissingPanels() {
        EmbeddedChannel ch=new EmbeddedChannel();
        try {
            Player p=Player.createNative950("bootstrap-owner",new WorldTile(3222,3222,0),ch);
            Native950World.mirrorBootstrapInterfaces(p,Arrays.asList(
                Native950Packets.openTop(1477),Native950Packets.openSub(1477,30,1482,true),
                Native950Packets.openSub(1477,114,1462,true),Native950Packets.openSub(1477,110,1473,true)));
            assertEquals((1477<<16)|114,p.getInterfaceManager().getInterfaceParentId(1462));
            assertEquals((1477<<16)|30,p.getInterfaceManager().getInterfaceParentId(1482));
            assertEquals(-1,p.getInterfaceManager().getInterfaceParentId(1433));
        } finally { ch.finishAndReleaseAll(); }
    }
    @Test public void replacementAndCloseRetireNestedPagesButPreserveUnrelatedHud() {
        Map<Integer,Integer> state=Native950World.bootstrapInterfaceParents(Arrays.asList(
            Native950Packets.openSub(1477,114,1462,true),Native950Packets.openSub(1477,715,1448,true),
            Native950Packets.openSub(1448,3,1426,true),Native950Packets.openSub(1426,0,742,true),
            Native950Packets.openSub(1477,715,1433,true)));
        assertEquals(2,state.size());
        assertEquals(Integer.valueOf(1462),state.get((1477<<16)|114));
        assertEquals(Integer.valueOf(1433),state.get((1477<<16)|715));
        state=Native950World.bootstrapInterfaceParents(Arrays.asList(
            Native950Packets.openSub(1477,715,1448,true),Native950Packets.openSub(1448,3,1426,true),
            Native950Packets.openSub(1426,0,742,true),Native950Packets.closeSub(1477,715)));
        assertTrue(state.isEmpty());
    }
    @Test public void topReplacementAndEmptyProbeLeaveOnlyActualFinalOpens() {
        assertTrue(Native950World.bootstrapInterfaceParents(Collections.emptyList()).isEmpty());
        Map<Integer,Integer> state=Native950World.bootstrapInterfaceParents(Arrays.asList(
            Native950Packets.openSub(1477,114,1462,true),Native950Packets.openTop(1477),
            Native950Packets.openSub(1477,30,1482,true)));
        assertEquals(1,state.size());
        assertEquals(Integer.valueOf(1482),state.get((1477<<16)|30));
    }
}
