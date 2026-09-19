package com.rs.game.player.client;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class Native950ItemBrowserTest {
    private EmbeddedChannel channel;
    private Player player;
    private Native950ItemBrowser browser;

    @Before public void setup() {
        channel=new EmbeddedChannel();
        player=Player.createNative950("item-browser-test",new WorldTile(3217,3258,0),channel);
        player.setActive(true);player.setRights(2);
        List<Native950ItemCatalog.Entry> entries=new ArrayList<Native950ItemCatalog.Entry>();
        Set<Integer> ids=new LinkedHashSet<Integer>();
        for(Native950ContentCommands.ItemSearchEntry entry:Native950ContentCommands.testingKitItemBrowserEntries())ids.add(entry.id);
        for(Native950ContentCommands.ItemSearchEntry entry:Native950ContentCommands.itemMatches("torva",5))ids.add(entry.id);
        ids.add(995);
        Set<Integer> torvaIds=new LinkedHashSet<Integer>();
        for(Native950ContentCommands.ItemSearchEntry entry:Native950ContentCommands.itemMatches("torva",5))torvaIds.add(entry.id);
        for(int id:ids)entries.add(new Native950ItemCatalog.Entry(id,"Fixture "+id,
                id==995||id==556||id==63284||torvaIds.contains(id),new String[5]));
        Native950Skilling.attach(player,new Native950Containers(player,new Native950ItemCatalog(entries)));
        Native950Dialogues dialogues=new Native950Dialogues(player,channel,()->{},()->{});
        player.setNative950Dialogues(dialogues);
        browser=new Native950ItemBrowser(player,channel,dialogues,()->{},new Native950QuantityInput(()->{}));
    }

    @After public void cleanup(){browser.dispose();Native950Skilling.detach(player);channel.finishAndReleaseAll();}

    @Test public void fiveDistinctSearchSlotsGrantFiveDistinctItems() throws Exception {
        search("torva");
        assertTrue(browser.visibleResultCountForTests()>=5);
        Set<Integer> clicked=new LinkedHashSet<Integer>();
        for(int slot=0;slot<5;slot++){
            int id=browser.visibleResultIdForTests(slot);clicked.add(id);
            assertTrue(browser.handle(interfaceAction(1,1265,20,slot,-1)));
            assertEquals("slot "+slot+" granted the wrong visible item",1,player.getInventory().getAmountOf(id));
        }
        assertEquals(5,clicked.size());
        assertEquals(5,browser.recentCountForTests());
    }

    @Test public void recentSlotsRetainTheirOwnIdsAcrossReordering() throws Exception {
        search("torva");
        List<Integer> granted=new ArrayList<Integer>();
        for(int slot=0;slot<5;slot++){
            int id=browser.visibleResultIdForTests(slot);granted.add(id);
            browser.handle(interfaceAction(1,1265,20,slot,id));
        }
        browser.handle(interfaceAction(1,1265,32,-1,-1));
        assertEquals("RECENT",browser.viewForTests());
        for(int id:granted){
            int slot=browser.visibleSlotForTests(id);assertTrue(slot>=0);
            int before=player.getInventory().getAmountOf(id);
            browser.handle(interfaceAction(1,1265,20,slot,id));
            assertEquals("Recent granted a different displayed item",before+1,player.getInventory().getAmountOf(id));
        }
    }

    @Test public void clientItemClaimCanRecoverAStaleSlotButNeverAnUnrenderedItem() throws Exception {
        search("torva");
        int requested=browser.visibleResultIdForTests(4);
        int first=browser.visibleResultIdForTests(0);
        browser.handle(interfaceAction(1,1265,20,0,requested));
        assertEquals(1,player.getInventory().getAmountOf(requested));
        assertEquals(0,player.getInventory().getAmountOf(first));

        browser.handle(interfaceAction(1,1265,20,0,995));
        assertEquals(0,player.getInventory().getAmountOf(995));
    }

    @Test public void rightClickQuantitiesGiveXAndSearchAgainStayInWorkflow() throws Exception {
        search("995");
        browser.handle(interfaceAction(2,1265,20,0,995));
        browser.handle(interfaceAction(3,1265,20,0,995));
        browser.handle(interfaceAction(4,1265,20,0,995));
        browser.handle(interfaceAction(5,1265,20,0,995));
        assertEquals(116,player.getInventory().getAmountOf(995));

        browser.handle(interfaceAction(6,1265,20,0,995));
        assertTrue(player.getInterfaceManager().containsInterface(1469));
        browser.handle(countAction(37));
        assertEquals(153,player.getInventory().getAmountOf(995));
        assertTrue(player.getInterfaceManager().containsInterface(1265));

        browser.handle(interfaceAction(1,1265,41,-1,-1));
        browser.handle(stringAction("torva"));
        assertTrue(browser.viewForTests().startsWith("SEARCH: torva"));
    }

    @Test public void differentQuantityOptionsRetainTheClickedSlotIdentity() throws Exception {
        search("torva");
        int[] options={1,3,4,5,6},amounts={1,5,10,100,37};
        for(int slot=0;slot<options.length;slot++){
            int id=browser.visibleResultIdForTests(slot);
            browser.handle(interfaceAction(options[slot],1265,20,slot,id));
            assertEquals("option "+options[slot]+" selected the wrong rendered item",id,browser.selectedIdForTests());
            if(options[slot]==6)browser.handle(countAction(amounts[slot]));
            assertEquals("option "+options[slot]+" granted the wrong rendered item",amounts[slot],player.getInventory().getAmountOf(id));
        }
    }

    @Test public void removeRecentOnlyChangesHistory() throws Exception {
        search("995");browser.handle(interfaceAction(1,1265,20,0,995));
        assertEquals(1,player.getInventory().getAmountOf(995));
        browser.handle(interfaceAction(1,1265,32,-1,-1));
        browser.handle(interfaceAction(7,1265,20,0,995));
        assertEquals(1,player.getInventory().getAmountOf(995));
        assertEquals(0,browser.recentCountForTests());
        assertEquals("TESTING KIT",browser.viewForTests());
    }

    @Test public void broadSearchUsesNativeScrollableContainerWithoutTruncation() throws Exception {
        search("rune");
        assertEquals(Native950ContentCommands.itemMatches("rune",Integer.MAX_VALUE).size(),browser.visibleResultCountForTests());
        assertTrue(browser.visibleResultCountForTests()>40);
        assertNotEquals(browser.visibleResultIdForTests(0),browser.visibleResultIdForTests(40));
        int rows=(browser.visibleResultCountForTests()+7)/8;
        assertTrue(contains(packets(),itemOptions(rows,false)));
    }

    @Test public void recentPublishesRemoveOptionWithoutChangingSnapshotIdentity() throws Exception {
        search("995");browser.handle(interfaceAction(1,1265,20,0,995));
        browser.handle(interfaceAction(1,1265,32,-1,-1));
        assertTrue(contains(packets(),itemOptions(1,true)));
        assertEquals(995,browser.visibleResultIdForTests(0));
    }

    @Test public void fullInventoryRefusesGrantWithoutClosingBrowser() throws Exception {
        int nonStackable=Native950ContentCommands.testingKitItemBrowserEntries().get(0).id;
        assertTrue(Native950Skilling.giveItem(player,nonStackable,28));
        search("995");
        browser.handle(interfaceAction(1,1265,20,0,995));
        assertEquals(0,player.getInventory().getAmountOf(995));
        assertTrue(browser.isOpen());
        assertTrue(player.getInterfaceManager().containsInterface(1265));
    }

    @Test public void openingAlwaysReturnsToUsefulTestingKit() throws Exception {
        Native950ItemBrowser.open(player);
        assertEquals("TESTING KIT",browser.viewForTests());
        assertEquals(19,browser.visibleResultCountForTests());
        search("torva");
        Native950ItemBrowser.open(player);
        assertEquals("TESTING KIT",browser.viewForTests());
        assertEquals(19,browser.visibleResultCountForTests());
    }

    private void search(String query) throws Exception {
        Native950ItemBrowser.open(player);
        assertTrue(browser.handle(interfaceAction(1,1265,41,-1,-1)));
        assertTrue(browser.handle(stringAction(query)));
        assertTrue(player.getInterfaceManager().containsInterface(1265));
    }

    private static Native950Packets.Packet itemOptions(int rows,boolean recent){
        return Native950Packets.runClientScript(150,(1265<<16)|20,139,8,rows,0,-1,
                "Give 1","","Give 5","Give 10","Give 100","Give X",recent?"Remove from Recent":"","","",0);
    }

    private List<Native950Packets.Packet> packets(){
        List<Native950Packets.Packet> packets=new ArrayList<Native950Packets.Packet>();Object value;
        channel.flushOutbound();
        while((value=channel.readOutbound())!=null)if(value instanceof Native950Packets.Packet)packets.add((Native950Packets.Packet)value);
        return packets;
    }

    private static boolean contains(List<Native950Packets.Packet> packets,Native950Packets.Packet expected){
        for(Native950Packets.Packet packet:packets)if(packet.type()==expected.type()
                &&Arrays.equals(packet.payload(),expected.payload()))return true;
        return false;
    }

    private static Native950Actions.InterfaceAction interfaceAction(int option,int interfaceId,int component,int slot,int itemId) throws Exception {
        return construct(Native950Actions.InterfaceAction.class,new Class<?>[]{int.class,int.class,int.class,int.class},
                option,(interfaceId<<16)|component,slot,itemId);
    }

    private static Native950Actions.StringDialogueAction stringAction(String text) throws Exception {
        return construct(Native950Actions.StringDialogueAction.class,new Class<?>[]{boolean.class,String.class},false,text);
    }

    private static Native950Actions.CountDialogueAction countAction(long count) throws Exception {
        return construct(Native950Actions.CountDialogueAction.class,new Class<?>[]{long.class},count);
    }

    private static <T> T construct(Class<T> type,Class<?>[] parameterTypes,Object... values) throws Exception {
        Constructor<T> constructor=type.getDeclaredConstructor(parameterTypes);constructor.setAccessible(true);
        return constructor.newInstance(values);
    }
}
