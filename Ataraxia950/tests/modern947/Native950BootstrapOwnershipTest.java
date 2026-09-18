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
    @Test public void nativeAbilityBookMountsBecomeValidInputOwners() {
        EmbeddedChannel ch=new EmbeddedChannel();
        try {
            Player p=Player.createNative950("bootstrap-books",new WorldTile(3222,3222,0),ch);
            // These are the concrete 950 cache slots selected by the login handoff:
            // 6=Melee, 7=Ranged and 39=Defence.  Mirroring the exact mounts is what
            // lets the action-bar input path accept a drag from a visible book.
            Native950World.mirrorBootstrapInterfaces(p,Arrays.asList(
                Native950Packets.openTop(1477),Native950Packets.openSub(1477,147,1460,true),
                Native950Packets.openSub(1477,158,1452,true),Native950Packets.openSub(1477,268,1880,true)));
            assertTrue(p.getInterfaceManager().containsInterface(1460));
            assertTrue(p.getInterfaceManager().containsInterface(1452));
            assertTrue(p.getInterfaceManager().containsInterface(1880));
            assertEquals((1477<<16)|147,p.getInterfaceManager().getInterfaceParentId(1460));
            assertEquals((1477<<16)|158,p.getInterfaceManager().getInterfaceParentId(1452));
            assertEquals((1477<<16)|268,p.getInterfaceManager().getInterfaceParentId(1880));
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
