package com.rs.game.player.client;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;

/** Session ownership tests; paired-cache evidence independently supplies the native recipe. */
public class Native950SkillGuideTest {
    private EmbeddedChannel channel;
    private Player player;
    private Native950SkillGuide guide;
    private int verified;
    @Before public void setup() {
        channel = new EmbeddedChannel();
        player = Player.createNative950("skill-guide-test",new WorldTile(3208,3215,0),channel);
        player.setActive(true);
        player.getInterfaceManager().registerNativeOpen(1482,1477,30);
        guide = new Native950SkillGuide(player,channel,() -> verified++);
    }
    @After public void cleanup() { channel.finishAndReleaseAll(); }
    @Test public void onlyExactSkillCellRequestsOpenAndNewSkillsHaveTheirOwnMapping() {
        for (int slot=0;slot<29;slot++) assertTrue(Native950SkillGuide.isOpenRequest(button(1466,7,slot,-1,1)));
        for (Native950Actions.InterfaceAction a : Arrays.asList(button(1466,7,-1,-1,1),button(1466,7,29,-1,1),
                button(1466,7,0,1,1),button(1466,7,0,-1,2),button(1466,6,0,-1,1),button(1218,31,-1,-1,1))) {
            assertFalse(Native950SkillGuide.isOpenRequest(a)); assertFalse(guide.handle(a));
        }
        assertEquals(27,Native950SkillGuide.skillForCell(27));
        assertEquals(28,Native950SkillGuide.skillForCell(28));
        assertEquals(28,Native950SkillGuide.guideArgument(27));
        assertEquals(29,Native950SkillGuide.guideArgument(28));
        assertEquals(14,Native950SkillGuide.skillForCell(2));
        assertEquals(13,Native950SkillGuide.guideArgument(14));
        assertTrue(packets().isEmpty()); assertEquals(0,verified);
    }
    @Test public void cacheMismatchCannotReplaceAnotherOwnedInterface() {
        guide = new Native950SkillGuide(player,channel,() -> {throw new IllegalStateException("changed");});
        try { guide.handle(button(1466,7,2,-1,1));fail(); }
        catch(IllegalStateException expected) {assertEquals("changed",expected.getMessage());}
        assertFalse(guide.isOpen()); assertFalse(player.getInterfaceManager().containsInterface(1448));
        assertScene();assertTrue(packets().isEmpty());
    }
    @Test public void heroSkillsSelectionPrecedesMountAndNativeLayoutAndRegistersNestedContent() {
        assertTrue(guide.handle(button(1466,7,2,-1,1)));
        List<Native950Packets.Packet> p=packets();
        int selection=index(p,Native950Packets.varbitSmall(18995,2));
        assertTrue(selection>=0 && selection<index(p,Native950Packets.openSub(1477,715,1448,true)));
        assertTrue(selection<index(p,Native950Packets.runClientScript(8288,0)));
        assertTrue(index(p,Native950Packets.runClientScript(8283,21144,0))>=0);
        assertTrue(index(p,Native950Packets.varbitSmall(18994,9))<0);
        assertTrue(index(p,Native950Packets.runClientScript(8283,21179,0))<0);
        assertTrue(index(p,Native950Packets.hideInterface(1477,714,true))<0);
        assertTrue(index(p,Native950Packets.runClientScript(5682,13))<index(p,Native950Packets.openSub(1218,0,1217,false)));
        assertEquals((1477<<16)|715,player.getInterfaceManager().getInterfaceParentId(1448));
        assertEquals((1448<<16)|3,player.getInterfaceManager().getInterfaceParentId(1218));
        assertEquals(1218<<16,player.getInterfaceManager().getInterfaceParentId(1217));
        assertEquals(1,verified); assertScene();
    }
    @Test public void everyGuideIconRefreshesRowsWithoutReplayingSelectionOrRemountingHero() {
        guide.handle(button(1466,7,0,-1,1));packets();
        // Expected native onOp5682 arguments from the paired1218 component definitions.
        for(int[] sample:new int[][]{{7,18},{15,8},{23,28},{31,1},{39,6},{47,22},{55,16},
                {63,11},{71,5},{79,26},{87,25},{95,21},{103,17},{111,15},{119,19},
                {127,9},{135,23},{143,27},{152,4},{160,13},{168,29},{176,7},{184,3},
                {192,12},{200,20},{208,14},{216,2},{224,24},{232,10}}) {
            assertTrue(guide.handle(button(1218,sample[0],-1,-1,1)));
            List<Native950Packets.Packet> p=packets();
            assertEquals("A skill change must not rebuild or reopen Hero",1,p.size());
            assertEquals(0,index(p,Native950Packets.runClientScript(5690)));
            assertGuideParents();
            // The server must still track a client-owned selection for later filters.
            assertTrue(guide.handle(button(1218,262,-1,-1,1)));
            p=packets();assertEquals(1,p.size());
            assertEquals(0,index(p,Native950Packets.runClientScript(5691,sample[1])));
        }
        assertEquals(1,verified);
    }
    @Test public void externalSkillSelectionWhileOpenSelectsThenFillsWithoutRemounting() {
        guide.handle(button(1466,7,0,-1,1));packets();
        for(int slot=0;slot<29;slot++) {
            assertTrue(guide.handle(button(1466,7,slot,-1,1)));
            List<Native950Packets.Packet> p=packets();
            assertEquals("Only the selected skill and its rows should change",2,p.size());
            assertEquals(0,index(p,Native950Packets.runClientScript(5682,
                    Native950SkillGuide.guideArgument(Native950SkillGuide.skillForCell(slot)))));
            assertEquals(1,index(p,Native950Packets.runClientScript(5690)));
            assertGuideParents();
        }
        assertEquals(1,verified);
    }
    @Test public void categoryAndSortNotificationsDoNotResetTheClientSelection() {
        guide.handle(button(1466,7,2,-1,1));packets();
        assertTrue(guide.handle(button(1218,258,-1,-1,1)));
        assertTrue(guide.handle(button(1218,261,-1,-1,1)));
        assertTrue(packets().isEmpty());
        assertTrue(guide.handle(button(1218,262,-1,-1,1)));
        List<Native950Packets.Packet> p=packets();
        assertEquals(1,p.size());assertTrue(index(p,Native950Packets.runClientScript(5691,13))>=0);
    }
    @Test public void heroTabSwitchReplacesSkillsPageWithoutRecreatingManagementShell() {
        guide.handle(button(1466,7,2,-1,1));packets();
        assertTrue(guide.handle(button(1477,714,3,-1,1)));
        List<Native950Packets.Packet> p=packets();
        assertTrue(index(p,Native950Packets.varbitSmall(18995,1))>=0);
        assertTrue(index(p,Native950Packets.runClientScript(8283,21143,0))>=0);
        assertTrue(index(p,Native950Packets.closeSub(1218,0))>=0);
        assertTrue(index(p,Native950Packets.openSub(1448,3,320,true))>=0);
        assertTrue(index(p,Native950Packets.openSub(1448,5,1446,true))>=0);
        assertTrue(index(p,Native950Packets.openSub(1477,715,1448,true))<0);
        assertTrue(index(p,Native950Packets.hideInterface(1448,3,false))>=0);
        assertTrue(index(p,Native950Packets.openSub(1218,0,1217,false))<0);
        assertScene();
    }
    @Test public void closeEscapeAndReopenLeaveSceneAndSettingsKeyIntact() {
        for(Native950Actions.InterfaceAction close:Arrays.asList(button(1477,717,1,-1,1),button(1477,8,-1,-1,1))) {
            guide.handle(button(1466,7,0,-1,1));packets();
            assertTrue(guide.handle(close)); assertFalse(guide.isOpen());
            List<Native950Packets.Packet> p=packets();
            assertTrue(index(p,Native950Packets.closeSub(1218,0))>=0);
            assertTrue(index(p,Native950Packets.closeSub(1448,3))>=0);
            assertTrue(index(p,Native950Packets.closeSub(1477,715))>=0);
            assertTrue(index(p,Native950Packets.closeSub(1477,30))<0);
            assertTrue(index(p,Native950Packets.interfaceEvents(1477,8,-1,-1,254))>=0);
            for(int id:new int[]{1448,1218,1217}) assertFalse(player.getInterfaceManager().containsInterface(id));
            guide.close();assertFalse(guide.handle(button(1218,31,-1,-1,1)));
            assertTrue(packets().isEmpty());assertScene();
        }
    }
    private void assertGuideParents() {
        assertTrue(guide.isOpen());
        assertEquals((1477<<16)|715,player.getInterfaceManager().getInterfaceParentId(1448));
        assertEquals((1448<<16)|3,player.getInterfaceManager().getInterfaceParentId(1218));
        assertEquals(1218<<16,player.getInterfaceManager().getInterfaceParentId(1217));
        assertScene();
    }
    private void assertScene() {assertEquals((1477<<16)|30,player.getInterfaceManager().getInterfaceParentId(1482));}
    private List<Native950Packets.Packet> packets() {
        List<Native950Packets.Packet> result=new ArrayList<>();Object value;
        channel.flushOutbound();while((value=channel.readOutbound())!=null)result.add((Native950Packets.Packet)value);
        return result;
    }
    private static int index(List<Native950Packets.Packet> list,Native950Packets.Packet expected) {
        for(int i=0;i<list.size();i++)if(list.get(i).type()==expected.type() && Arrays.equals(list.get(i).payload(),expected.payload()))return i;
        return -1;
    }
    private static Native950Actions.InterfaceAction button(int iface,int component,int slot,int item,int op) {
        int hash=(iface<<16)|component;
        return (Native950Actions.InterfaceAction) Native950Actions.decode(op==1 ? 18 : 122,
            new byte[] {(byte)(item>>>16),(byte)(item>>>8),(byte)item,
                (byte)(hash>>>16),(byte)(hash>>>24),(byte)hash,(byte)(hash>>>8),(byte)(slot>>>8),(byte)slot});
    }
}
