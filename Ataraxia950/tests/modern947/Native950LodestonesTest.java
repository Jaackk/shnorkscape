package com.rs.game.player.client;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.*;
import java.util.*;
import static org.junit.Assert.*;

public class Native950LodestonesTest {
    private EmbeddedChannel channel;
    private Player player;
    private Native950Lodestones network;
    private int verifications;
    private boolean accept;
    private List<WorldTile> attempts;
    @Before public void setup() {
        channel=new EmbeddedChannel(); player=Player.createNative950("lodestone-test",new WorldTile(3217,3258,0),channel);
        player.setActive(true); player.getInterfaceManager().registerNativeOpen(1482,1477,30);
        attempts=new ArrayList<>(); accept=true;
        network=new Native950Lodestones(player,channel,()->verifications++, (p,t)->{attempts.add(t);return accept;});
    }
    @After public void cleanup() { channel.finishAndReleaseAll(); }
    @Test public void bootstrapPublishesUnlocksBeforeTheFirstNetworkOnLoad() {
        assertEquals(0,verifications); assertTrue(packets().isEmpty()); network.bootstrap();
        List<Native950Packets.Packet> out=packets();
        assertEquals(1,verifications); assertFalse(network.isOpen());
        for(Native950Lodestones.Destination row:Native950Lodestones.destinations())
            assertTrue(contains(out,Native950Packets.varbitLarge(row.unlockVarbit,row.unlockValue)));
        assertTrue(contains(out,Native950Packets.varbitLarge(23198,400)));
        assertTrue(contains(out,Native950Packets.varbitSmall(36140,100)));
        assertTrue(contains(out,Native950Packets.varp(2102,15)));
        assertTrue(contains(out,Native950Packets.interfaceEvents(1465,34,-1,-1,6)));
        assertFalse(contains(out,Native950Packets.openSub(1477,735,1092,false)));
        network.handle(button(1465,34,-1,-1,1));
        assertTrue(network.isOpen()); assertEquals(1,verifications);
    }
    @Test public void changedCacheCannotPublishUnlocksDuringBootstrap() {
        network=new Native950Lodestones(player,channel,()->{throw new IllegalStateException("changed cache");},(p,t)->true);
        try {network.bootstrap();fail();} catch(IllegalStateException expected){assertEquals("changed cache",expected.getMessage());}
        assertFalse(network.isOpen());assertTrue(packets().isEmpty());
    }
    @Test public void rejectsWrongStaticSentinelsTargetsAndClosedDestinationPackets() {
        assertTrue(Native950Lodestones.isOpenRequest(button(1465,34,-1,-1,1)));
        assertTrue(Native950Lodestones.isOpenRequest(button(1465,34,-1,-1,2)));
        for(Native950Actions.InterfaceAction bad:Arrays.asList(button(1465,33,-1,-1,1),button(1465,34,0,-1,1),button(1465,34,-1,0,1),button(1092,17,-1,-1,1)))
            assertFalse(network.handle(bad));
        assertEquals(0,verifications); assertTrue(attempts.isEmpty()); assertTrue(packets().isEmpty());
    }
    @Test public void aChangedCacheDoesNotMutateTheUi() {
        network=new Native950Lodestones(player,channel,()->{throw new IllegalStateException("changed cache");},(p,t)->true);
        try {network.handle(button(1465,34,-1,-1,1));fail();} catch(IllegalStateException expected){assertEquals("changed cache",expected.getMessage());}
        assertFalse(network.isOpen());assertFalse(player.getInterfaceManager().containsInterface(1092));assertTrue(packets().isEmpty());
    }
    @Test public void mountsCentralNetworkAndClosesKeyboardContextWithoutClosingScene() {
        network.handle(button(1465,34,-1,-1,1));
        assertTrue(network.isOpen()); assertEquals(1,verifications);
        assertEquals((1477<<16)|735,player.getInterfaceManager().getInterfaceParentId(1092));
        List<Native950Packets.Packet> out=packets();
        int mount=index(out,Native950Packets.openSub(1477,735,1092,false));
        int wrapper=index(out,Native950Packets.runClientScript(11145,576,376,0,0,(1477<<16)|732));
        int host=index(out,Native950Packets.runClientScript(11145,576,360,0,0,(1477<<16)|735));
        assertTrue("Both clipping ancestors must fit before mounting the panel",wrapper>=0&&host>wrapper&&mount>host);
        assertTrue(contains(out,Native950Packets.hideInterface(1477,732,false)));
        for(Native950Lodestones.Destination row:Native950Lodestones.destinations()) {
            assertTrue(contains(out,Native950Packets.varbitLarge(row.unlockVarbit,row.unlockValue)));
            assertTrue(contains(out,Native950Packets.interfaceEvents(1092,row.component,-1,-1,6)));
        }
        for(int component=36;component<=40;component++) assertTrue(contains(out,Native950Packets.hideInterface(1092,component,true)));
        assertFalse(contains(out,Native950Packets.closeSub(1477,30)));
        network.handle(button(1092,66,-1,-1,1)); assertFalse(network.isOpen());
        out=packets();assertTrue(contains(out,Native950Packets.runClientScript(8841,30,0)));assertTrue(contains(out,Native950Packets.closeSub(1477,735)));
        assertTrue(index(out,Native950Packets.runClientScript(11145,512,352,0,0,(1477<<16)|732))
                >index(out,Native950Packets.closeSub(1477,735)));
        assertTrue(contains(out,Native950Packets.runClientScript(8389)));
        assertFalse(contains(out,Native950Packets.closeSub(1477,30)));assertTrue(player.getInterfaceManager().containsInterface(1482));
        assertFalse(player.getInterfaceManager().containsInterface(1092));network.close();assertTrue(packets().isEmpty());
    }
    @Test public void everyOrdinaryDestinationUsesCurrentCacheCoordinatesAndNoSeasonalCanTeleport() {
        assertEquals(29,Native950Lodestones.destinations().size());
        assertEquals(34213704,Native950Lodestones.byComponent(9).packedAnchor);
        assertEquals("Fort Forinthry",Native950Lodestones.byComponent(22).name);
        assertEquals("City of Um",Native950Lodestones.byComponent(35).name);
        assertEquals("Wendlewick",Native950Lodestones.byComponent(41).name);
        for(Native950Lodestones.Destination row:Native950Lodestones.destinations()) {
            network.handle(button(1465,34,-1,-1,1));packets();
            assertTrue(network.handle(button(1092,row.component,-1,-1,1)));assertFalse(network.isOpen());
            WorldTile target=attempts.get(attempts.size()-1);assertEquals(row.arrival(),target);
            assertEquals(1,Math.abs(row.anchor().getX()-target.getX())+Math.abs(row.anchor().getY()-target.getY()));
            assertTrue(contains(packets(),Native950Packets.varbitSmall(41,row.networkId)));
        }
        assertEquals(29,attempts.size());network.handle(button(1465,34,-1,-1,1));packets();
        for(int component=36;component<=40;component++) assertFalse(network.handle(button(1092,component,-1,-1,1)));
        assertEquals(29,attempts.size());
    }
    @Test public void deniedTeleportKeepsNetworkAndPreviousDestinationUnchanged() {
        network.handle(button(1465,34,-1,-1,1));packets();accept=false;
        assertTrue(network.handle(button(1092,17,-1,-1,1)));assertTrue(network.isOpen());assertTrue(packets().isEmpty());
        network.close();packets();network.handle(button(1465,34,-1,-1,2));assertTrue(network.isOpen());assertEquals(1,attempts.size());
    }
    @Test public void previousDestinationAndQuickFallbackUseTheSameOrdinaryAction() {
        network.handle(button(1465,34,-1,-1,1));packets();
        assertTrue(network.handle(button(1092,17,-1,-1,2)));assertFalse(network.isOpen());
        WorldTile first=attempts.get(0);packets();network.handle(button(1465,34,-1,-1,2));
        assertEquals(2,attempts.size());assertEquals(first,attempts.get(1));assertFalse(network.isOpen());
    }
    @Test public void wrongDestinationSentinelsAndQuickChargeControlsCannotConsumeOrTeleport() {
        network.handle(button(1465,34,-1,-1,1));packets();
        assertFalse(network.handle(button(1092,17,0,-1,1)));assertFalse(network.handle(button(1092,17,-1,0,1)));
        assertTrue(network.handle(button(1092,64,-1,-1,1)));assertTrue(network.handle(button(1092,61,-1,-1,1)));
        assertTrue(attempts.isEmpty());assertTrue(network.isOpen());
    }
    private List<Native950Packets.Packet> packets() {
        channel.flush();List<Native950Packets.Packet> result=new ArrayList<>();Object packet;
        while((packet=channel.readOutbound())!=null)result.add((Native950Packets.Packet)packet);return result;
    }
    private static int index(List<Native950Packets.Packet> packets,Native950Packets.Packet expected) {
        for(int i=0;i<packets.size();i++)if(packets.get(i).type()==expected.type()
                && Arrays.equals(packets.get(i).payload(),expected.payload()))return i;
        return -1;
    }
    private static boolean contains(List<Native950Packets.Packet> packets,Native950Packets.Packet expected) {
        for(Native950Packets.Packet packet:packets) if(packet.type()==expected.type() && Arrays.equals(packet.payload(),expected.payload()))return true;return false;
    }
    private static Native950Actions.InterfaceAction button(int panel,int component,int slot,int item,int option) {
        int hash=(panel<<16)|component;
        return (Native950Actions.InterfaceAction)Native950Actions.decode(option==1?18:122,new byte[]{(byte)(item>>>16),(byte)(item>>>8),(byte)item,
                (byte)(hash>>>16),(byte)(hash>>>24),(byte)hash,(byte)(hash>>>8),(byte)(slot>>>8),(byte)slot});
    }
}
