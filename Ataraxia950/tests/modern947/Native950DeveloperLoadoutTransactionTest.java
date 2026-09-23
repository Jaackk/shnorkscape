package com.rs.game.player.client;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Bank;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class Native950DeveloperLoadoutTransactionTest {
    private static final class Fixture implements AutoCloseable {
        final EmbeddedChannel channel=new EmbeddedChannel();
        final Player p=Player.createNative950("transaction",new WorldTile(3217,3258,0),channel);
        final Native950Containers c=new Native950Containers(p,new Native950ItemCatalog(Collections.emptyList(),id->
                id==100?new Native950ItemCatalog.Entry(id,"weapon",false,new String[]{"Wield",null,null,null,null},3,1):
                new Native950ItemCatalog.Entry(id,"resource",id==995,new String[5])));
        public void close(){channel.finishAndReleaseAll();}
    }
    @Test public void allPropertyIsBankedBeforeFullInventoryAndEquipmentAreReplaced(){try(Fixture f=new Fixture()){
        f.p.getBank().bankTabs=new Item[][]{{new Item(995,100)}};f.p.getBank().restoreNativePreferences(true,73,5);
        for(int i=0;i<28;i++)f.p.getInventory().items.set(i,new Item(200+i,1));
        f.p.getEquipment().getItems().set(3,new Item(100,1));
        Item[] inv=new Item[28],eq=new Item[19];inv[0]=new Item(995,10000);eq[3]=new Item(100,1);
        Native950Containers.DeveloperLoadoutChange change=f.c.prepareDeveloperLoadout(inv,eq);
        assertEquals(1,change.commit().moved);assertEquals(0,change.commit().moved);
        assertEquals(30,f.p.getBank().bankTabs[0].length);assertEquals(100,f.p.getBank().bankTabs[0][0].getAmount());
        assertEquals(10000,f.c.inventorySnapshot().amounts[0]);assertEquals(100,f.c.equipmentSnapshot().ids[3]);
        assertTrue(f.p.getBank().getWithdrawNotes());assertEquals(73,f.p.getBank().getLastX());
    }}
    @Test public void fullBankAndOverflowRefuseWithoutPartialDisplacement(){try(Fixture f=new Fixture()){
        Item[] bank=new Item[Bank.MAX_BANK_SIZE];for(int i=0;i<bank.length;i++)bank[i]=new Item(1000+i,1);
        f.p.getBank().bankTabs=new Item[][]{bank};Item original=new Item(995,4);f.p.getInventory().items.set(0,original);
        assertEquals(0,f.c.prepareDeveloperLoadout(new Item[28],new Item[19]).commit().moved);
        assertSame(original,f.p.getInventory().items.get(0));assertSame(bank,f.p.getBank().bankTabs[0]);
        f.p.getBank().bankTabs=new Item[][]{{new Item(995,Integer.MAX_VALUE)}};
        assertEquals(0,f.c.prepareDeveloperLoadout(new Item[28],new Item[19]).commit().moved);assertSame(original,f.p.getInventory().items.get(0));
    }}
    @Test public void staleIdentityOrAmountCannotCommit(){try(Fixture f=new Fixture()){
        Item item=new Item(995,10);f.p.getInventory().items.set(0,item);
        Native950Containers.DeveloperLoadoutChange change=f.c.prepareDeveloperLoadout(new Item[28],new Item[19]);
        item.setAmount(11);assertEquals(0,change.commit().moved);assertEquals(11,f.p.getInventory().items.get(0).getAmount());
        change=f.c.prepareDeveloperLoadout(new Item[28],new Item[19]);
        f.p.getInventory().items.set(0,new Item(995,11));assertEquals(0,change.commit().moved);
    }}
    @Test public void playersNeverShareTransactionStorage(){try(Fixture a=new Fixture();Fixture b=new Fixture()){
        a.p.getInventory().items.set(0,new Item(995,12));b.p.getInventory().items.set(0,new Item(995,34));
        Item[][] bank=b.p.getBank().bankTabs;
        assertEquals(1,a.c.prepareDeveloperLoadout(new Item[28],new Item[19]).commit().moved);
        assertSame(bank,b.p.getBank().bankTabs);assertEquals(34,b.p.getInventory().items.get(0).getAmount());
    }}
    @Test public void invalidGeneratedEquipmentDoesNotChangeProperty(){try(Fixture f=new Fixture()){
        Item original=new Item(995,12);f.p.getInventory().items.set(0,original);Item[] eq=new Item[19];eq[5]=new Item(100,1);
        try{f.c.prepareDeveloperLoadout(new Item[28],eq);fail();}catch(IllegalArgumentException expected){}
        assertSame(original,f.p.getInventory().items.get(0));assertEquals(0,f.p.getBank().bankTabs[0].length);
    }}
}
