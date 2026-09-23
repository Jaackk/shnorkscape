package com.rs.game.player.client;

import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Actions;
import io.netty.channel.embedded.EmbeddedChannel;
import java.net.*;
import java.lang.reflect.Constructor;
import java.util.*;
import org.junit.*;
import static org.junit.Assert.*;

public class Native950EquipmentLibraryTest {
    private String previousEnabled;
    private Fixture f;
    @Before public void setup(){previousEnabled=System.getProperty(Native950DevelopmentCommands.PROPERTY);System.setProperty(Native950DevelopmentCommands.PROPERTY,"true");f=new Fixture("librarytest");}
    @After public void cleanup(){f.close();if(previousEnabled==null)System.clearProperty(Native950DevelopmentCommands.PROPERTY);else System.setProperty(Native950DevelopmentCommands.PROPERTY,previousEnabled);}
    private static final class Fixture implements AutoCloseable {
        final EmbeddedChannel channel=new EmbeddedChannel(){@Override protected SocketAddress remoteAddress0(){return new InetSocketAddress("127.0.0.1",12345);}};
        final Player player;
        final Native950Containers containers;
        final Native950EquipmentLibrary library;
        final Native950EquipmentCatalogue catalogue;
        Fixture(String name){
            player=Player.createNative950(name,new WorldTile(3217,3258,0),channel);
            player.setActive(true);player.setRights(2);
            List<Native950ItemCatalog.Entry> definitions=Arrays.asList(
                    new Native950ItemCatalog.Entry(995,"Coins",true,new String[5]),
                    new Native950ItemCatalog.Entry(1277,"Sword",false,new String[5]),
                    new Native950ItemCatalog.Entry(52083,"Augmented weapon",2,new String[5]));
            containers=new Native950Containers(player,new Native950ItemCatalog(definitions));Native950Skilling.attach(player,containers);
            catalogue=new Native950EquipmentCatalogue(Arrays.asList(
                    new Native950EquipmentCatalogue.Entry(995,0,0,-1,10000,1,"Coins","coins"),
                    new Native950EquipmentCatalogue.Entry(1277,1,1,3,28,0,"Sword","sword"),
                    new Native950EquipmentCatalogue.Entry(52083,1,92,3,28,2,"Augmented weapon","weapon")));
            library=new Native950EquipmentLibrary(player,channel,()->{},()->{},()->catalogue,new Native950QuantityInput(()->{}));
            player.getBank().bankTabs=new Item[][]{{new Item(995,731)}};
        }
        void open(){Native950EquipmentLibrary.open(player);assertTrue(library.isOpen());}
        void click(int option,int component,int slot,int item)throws Exception{library.handle(button(option,component,slot,item));}
        int amount(int id){return player.getInventory().getAmountOf(id);}
        public void close(){library.dispose();Native950Skilling.detach(player);channel.finishAndReleaseAll();}
    }
    @Test public void browsingAndGrantsNeverReplaceBankOrPreferences()throws Exception{
        Item[][] bank=f.player.getBank().bankTabs;Item original=bank[0][0];
        f.player.getBank().restoreNativePreferences(true,73,5);
        Native950Containers.Snapshot before=f.containers.inventorySnapshot();f.open();
        assertArrayEquals(before.ids,f.containers.inventorySnapshot().ids);
        f.click(3,201,0,995);assertEquals(5,f.amount(995));
        f.click(1,39,-1,-1);f.click(1,42,-1,-1);f.click(1,127,-1,-1);f.click(1,153,-1,-1);
        assertSame(bank,f.player.getBank().bankTabs);assertSame(original,bank[0][0]);assertEquals(731,original.getAmount());
        assertTrue(f.player.getBank().getWithdrawNotes());assertEquals(73,f.player.getBank().getLastX());
        assertEquals(5,f.player.getBank().getNativeDefaultInteractionAmount());
        f.library.close();assertFalse(f.player.getInterfaceManager().containsInterface(517));assertSame(bank,f.player.getBank().bankTabs);
    }
    @Test public void nativeQuantitiesAndReplenishment()throws Exception{
        f.open();int[] options={2,3,4,7},amounts={1,5,10,10000};int total=0;
        for(int i=0;i<options.length;i++){
            f.library.tick();f.click(options[i],201,0,options[i]==7?48447:995);total+=amounts[i];assertEquals(total,f.amount(995));
            assertEquals(10000,f.catalogue.entries.get(0).quantity);
        }
        f.library.tick();f.click(1,96,-1,-1);f.click(1,201,0,995);assertEquals(total+5,f.amount(995));
    }
    @Test public void customQuantityIsOwnedCancelledAndCannotBeReplayed()throws Exception{
        f.open();assertFalse(f.library.handle(count(123)));
        f.click(6,201,0,995);assertTrue(f.library.handle(count(37)));assertEquals(37,f.amount(995));
        assertFalse(f.library.handle(count(37)));assertEquals(37,f.amount(995));
        f.library.tick();f.click(6,201,0,995);f.library.cancelInput();assertFalse(f.library.handle(count(12)));
        f.click(6,201,0,995);f.player.getInventory().items.set(2,new Item(1277,1));
        assertTrue(f.library.handle(count(99)));assertEquals(37,f.amount(995));
    }
    @Test public void inventoryCapacityAndAugmentedActors()throws Exception{
        f.open();for(int i=0;i<27;i++)f.player.getInventory().items.set(i,new Item(1277,1));
        f.click(4,201,2,48447);assertEquals(1,f.amount(52083));assertEquals(27,f.amount(1277));
        f.library.tick();f.click(2,201,0,995);assertEquals(0,f.amount(995));assertEquals(731,f.player.getBank().bankTabs[0][0].getAmount());
    }
    @Test public void customStackQuantityCanExceedDisplayDefaultButAllUsesDefault()throws Exception{
        f.open();f.click(6,201,0,995);f.library.handle(count(100001));assertEquals(100001,f.amount(995));
        f.library.tick();f.click(7,201,0,48447);assertEquals(110001,f.amount(995));
        assertEquals(10000,f.catalogue.entries.get(0).quantity);
    }
    @Test public void staleSlotsForeignClaimsAndSameTickDuplicatesFail()throws Exception{
        f.open();f.click(2,201,-1,995);f.click(2,201,999,995);f.click(2,201,0,1277);assertEquals(0,f.amount(995));
        f.click(2,201,0,995);f.click(2,201,0,995);assertEquals(1,f.amount(995));
        f.library.tick();f.click(2,201,0,995);assertEquals(2,f.amount(995));
    }
    @Test public void tabsAndLocalSearchPreserveOriginalSlotIdentity()throws Exception{
        f.open();f.click(1,169,3,-1);f.click(1,237,-1,-1);
        f.click(2,201,1,1277);assertEquals(1,f.amount(1277));assertEquals(0,f.amount(995));
        f.click(1,239,-1,-1);assertTrue(f.library.isOpen());
    }
    @Test public void sessionsAreIsolatedAndClosedLibraryCannotGrant()throws Exception{
        try(Fixture other=new Fixture("libraryother")){
            f.open();other.open();f.click(3,201,0,995);assertEquals(0,other.amount(995));assertTrue(other.library.isOpen());
            f.library.close();assertFalse(f.library.handle(button(2,201,0,995)));assertEquals(5,f.amount(995));
            other.click(2,201,0,995);assertEquals(1,other.amount(995));assertEquals(731,other.player.getBank().bankTabs[0][0].getAmount());
        }
    }
    @Test public void revokedPermissionAndMalformedCataloguesFailSafely()throws Exception{
        f.open();System.setProperty(Native950DevelopmentCommands.PROPERTY,"false");f.click(2,201,0,995);assertFalse(f.library.isOpen());assertEquals(0,f.amount(995));
        try{new Native950EquipmentCatalogue(Collections.singletonList(new Native950EquipmentCatalogue.Entry(-1,0,0,0,1,0,"Bad","bad")));fail();}catch(IllegalArgumentException expected){}
    }
    private static Native950Actions.InterfaceAction button(int option,int component,int slot,int item)throws Exception{
        Constructor<Native950Actions.InterfaceAction> c=Native950Actions.InterfaceAction.class.getDeclaredConstructor(int.class,int.class,int.class,int.class);c.setAccessible(true);
        return c.newInstance(option,(517<<16)|component,slot,item);
    }
    private static Native950Actions.CountDialogueAction count(long n)throws Exception{
        Constructor<Native950Actions.CountDialogueAction> c=Native950Actions.CountDialogueAction.class.getDeclaredConstructor(long.class);c.setAccessible(true);return c.newInstance(n);
    }
}
