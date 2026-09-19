package com.rs.game.player.client;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Actions;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.util.Arrays;

import static org.junit.Assert.*;

public class Native950ItemBrowserTest {
    private EmbeddedChannel channel;
    private Player player;
    private Native950Containers containers;
    private Native950ItemBrowser browser;

    @Before public void setup() {
        channel=new EmbeddedChannel();
        player=Player.createNative950("item-browser-test",new WorldTile(3217,3258,0),channel);
        player.setActive(true);player.setRights(2);
        containers=new Native950Containers(player,new Native950ItemCatalog(Arrays.asList(
                new Native950ItemCatalog.Entry(20135,"Torva full helm",false,new String[5]))));
        Native950Skilling.attach(player,containers);
        Native950Dialogues dialogues=new Native950Dialogues(player,channel,()->{},()->{});
        player.setNative950Dialogues(dialogues);
        browser=new Native950ItemBrowser(player,channel,dialogues,()->{});
    }

    @After public void cleanup(){browser.dispose();Native950Skilling.detach(player);channel.finishAndReleaseAll();}

    @Test public void openSearchGrantAndRepeatRemainInOneBrowserSession() throws Exception {
        Native950ItemBrowser.open(player);
        assertTrue(browser.isOpen());
        assertTrue(browser.handle(interfaceAction(1,1265,45,-1,-1)));
        assertTrue(browser.handle(stringAction("torva full helm")));

        assertTrue(browser.handle(interfaceAction(1,1265,20,0,20135)));
        assertTrue(browser.handle(dialogueAction(1188,8)));
        assertEquals(1,player.getInventory().getAmountOf(20135));
        assertTrue(browser.isOpen());

        assertTrue(browser.handle(interfaceAction(2,1265,20,0,20135)));
        assertEquals(2,player.getInventory().getAmountOf(20135));
        assertTrue(browser.isOpen());
    }

    @Test public void staleClientItemClaimCannotGrantAnotherItem() throws Exception {
        Native950ItemBrowser.open(player);
        browser.handle(interfaceAction(1,1265,45,-1,-1));
        browser.handle(stringAction("torva full helm"));
        assertTrue(browser.handle(interfaceAction(2,1265,20,0,995)));
        assertEquals(0,player.getInventory().getAmountOf(20135));
        assertTrue(browser.isOpen());
    }

    @Test public void fullInventoryRefusesGrantWithoutClosingBrowser() throws Exception {
        assertTrue(Native950Skilling.giveItem(player,20135,28));
        Native950ItemBrowser.open(player);
        browser.handle(interfaceAction(1,1265,45,-1,-1));
        browser.handle(stringAction("torva full helm"));
        assertTrue(browser.handle(interfaceAction(2,1265,20,0,20135)));
        assertEquals(28,player.getInventory().getAmountOf(20135));
        assertTrue(browser.isOpen());
    }

    private static Native950Actions.InterfaceAction interfaceAction(int option,int interfaceId,int component,int slot,int itemId) throws Exception {
        return construct(Native950Actions.InterfaceAction.class,new Class<?>[]{int.class,int.class,int.class,int.class},
                option,(interfaceId<<16)|component,slot,itemId);
    }

    private static Native950Actions.StringDialogueAction stringAction(String text) throws Exception {
        return construct(Native950Actions.StringDialogueAction.class,new Class<?>[]{boolean.class,String.class},false,text);
    }

    private static Native950Actions.DialogueClickAction dialogueAction(int interfaceId,int component) throws Exception {
        return construct(Native950Actions.DialogueClickAction.class,new Class<?>[]{int.class,int.class},(interfaceId<<16)|component,-1);
    }

    private static <T> T construct(Class<T> type,Class<?>[] parameterTypes,Object... values) throws Exception {
        Constructor<T> constructor=type.getDeclaredConstructor(parameterTypes);constructor.setAccessible(true);
        return constructor.newInstance(values);
    }
}
