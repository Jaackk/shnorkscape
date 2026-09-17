package com.rs.game.player.client;

import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import io.netty.channel.embedded.EmbeddedChannel;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public final class Native950ItemCommandTest {
    private String previous;
    private final EmbeddedChannel channel = new EmbeddedChannel() {
        @Override protected SocketAddress remoteAddress0() { return new InetSocketAddress("127.0.0.1",43650); }
    };
    private final Player player = Player.createNative950("item-test",new WorldTile(3217,3258,0),channel);
    private final Native950Containers containers = new Native950Containers(player,new Native950ItemCatalog(Arrays.asList(
            new Native950ItemCatalog.Entry(995,"Coins",true,new String[5]),
            new Native950ItemCatalog.Entry(1511,"Logs",false,new String[5]))));
    @Before public void setup() {
        previous=System.getProperty(Native950DevelopmentCommands.PROPERTY);
        System.setProperty(Native950DevelopmentCommands.PROPERTY,"true");
        player.setActive(true);Native950Skilling.attach(player,containers);
    }
    @After public void cleanup() {
        Native950Skilling.detach(player);channel.finishAndReleaseAll();
        if(previous==null)System.clearProperty(Native950DevelopmentCommands.PROPERTY);
        else System.setProperty(Native950DevelopmentCommands.PROPERTY,previous);
    }
    private void run(String text) { Native950DevelopmentCommands.handle(player,channel,text); }
    @Test public void defaultQuantityAndExplicitNonstackablesUseRealInventory() {
        run(";;item 1511");run(";;item 1511 2");
        assertEquals(3,player.getInventory().getAmountOf(1511));
        assertEquals(25,player.getInventory().getFreeSlots());
        for(int i=0;i<3;i++)assertEquals(1,player.getInventory().items.get(i).getAmount());
        Native950Save saved=containers.saveSnapshot("item-test",3217,3258,0);
        assertEquals(1511,saved.inventoryIds()[0]);
    }
    @Test public void explicitStackableQuantityAndColonAliasStackWithoutExtraSlots() {
        run(";;ITEM 995 1000");run("::item\t995\t23");
        assertEquals(1023,player.getInventory().getAmountOf(995));
        assertEquals(27,player.getInventory().getFreeSlots());
    }
    @Test public void malformedUnknownAndOutOfRangeRequestsLeaveInventoryUnchanged() {
        for(String text:new String[]{";;item",";;item fish",";;item 995 0",";;item 995 -1",
                ";;item -1 1",";;item 65535 1",";;item 9999 1",";;item 995 2147483648",
                ";;item 995 1 2",";;item 995 1.5"})run(text);
        assertEquals(28,player.getInventory().getFreeSlots());
        assertFalse(Native950DevelopmentCommands.isCommand(";;itemize 995 1"));
    }
    @Test public void fullInventoryAndStackOverflowNeverPartiallyGrant() {
        for(int i=0;i<27;i++)player.getInventory().items.set(i,new Item(1511,1));
        run(";;item 1511 2");assertEquals(27,player.getInventory().getAmountOf(1511));
        player.getInventory().items.set(27,new Item(995,Integer.MAX_VALUE-1));
        run(";;item 995 2");assertEquals(Integer.MAX_VALUE-1,player.getInventory().getAmountOf(995));
        run(";;item 1511");assertEquals(27,player.getInventory().getAmountOf(1511));
    }
    @Test public void disabledDevelopmentModeOrInactivePlayerCannotGrant() {
        System.setProperty(Native950DevelopmentCommands.PROPERTY,"false");run(";;item 995 10");
        System.setProperty(Native950DevelopmentCommands.PROPERTY,"true");player.setActive(false);run(";;item 995 10");
        assertEquals(28,player.getInventory().getFreeSlots());
    }
}
