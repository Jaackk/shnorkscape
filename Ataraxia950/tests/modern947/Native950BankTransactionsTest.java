package com.rs.game.player.client;

import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.Arrays;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public final class Native950BankTransactionsTest {
    private final EmbeddedChannel channel=new EmbeddedChannel();
    private final Player player=Player.createNative950("bank",new WorldTile(3217,3258,0),channel);
    private final Native950ItemCatalog catalog=new Native950ItemCatalog(Arrays.asList(
        new Native950ItemCatalog.Entry(80001,"Weapon",false,new String[]{null,"Wield"},3,2),
        new Native950ItemCatalog.Entry(80002,"Note",true,new String[]{null,null,null,null,"Drop"}),
        new Native950ItemCatalog.Entry(80003,"Offhand",false,new String[]{null,"Wield"},5,2),
        new Native950ItemCatalog.Entry(80004,"Ammo",true,new String[]{null,"Equip"},13,2)),
        id->new Native950ItemCatalog.Entry(id,"Resource",false,new String[]{null,null,null,null,"Drop"}));
    private final Native950Containers containers=new Native950Containers(player,catalog);
    @After public void cleanup(){channel.finishAndReleaseAll();}

    @Test public void noteCapacityUsesOutputStackAndSourceBankIdentityNeverAliases() {
        player.getBank().bankTabs=new Item[][]{{new Item(80001,50)}};
        for(int slot=0;slot<28;slot++)player.getInventory().items.set(slot,new Item(1,1));
        player.getInventory().items.set(17,null);
        assertEquals(1,containers.withdrawableAmount(0,80001,50));
        assertEquals(50,containers.withdrawableAmount(0,80001,50,80002));
        assertEquals(0,containers.withdraw(0,80002,50,80002).moved);
        assertEquals(50,containers.withdraw(0,80001,50,80002).moved);
        assertEquals(80002,player.getInventory().items.get(17).getId());
        assertEquals(50,player.getInventory().items.get(17).getAmount());
        assertEquals(0,player.getBank().bankTabs[0].length);
        assertEquals(0,containers.withdraw(0,80001,50,80002).moved);
    }
    @Test public void notesMergeIntoFullBagWithCheckedOverflowAndCorrectRemainder() {
        player.getBank().bankTabs=new Item[][]{{new Item(80001,50)}};
        for(int slot=0;slot<28;slot++)player.getInventory().items.set(slot,new Item(1,1));
        player.getInventory().items.set(17,new Item(80002,Integer.MAX_VALUE-7));
        assertEquals(7,containers.withdrawableAmount(0,80001,50,80002));
        assertEquals(7,containers.withdraw(0,80001,50,80002).moved);
        assertEquals(Integer.MAX_VALUE,player.getInventory().items.get(17).getAmount());
        assertEquals(80001,player.getBank().bankTabs[0][0].getId());
        assertEquals(43,player.getBank().bankTabs[0][0].getAmount());
        assertEquals(0,containers.withdraw(0,80001,50,80002).moved);
    }
    @Test public void equipmentSubsetCommitsOnceAndKeepsSkippedSlotIdentity() {
        Item weapon=new Item(80001,1),shield=new Item(80003,1),ammo=new Item(80004,800);
        player.getEquipment().getItems().set(3,weapon);player.getEquipment().getItems().set(5,shield);player.getEquipment().getItems().set(13,ammo);
        Native950Containers.EquipmentBankChange change=containers.prepareEquipmentDeposit();
        assertEquals(1,change.include(3).moved);assertEquals(800,change.include(13).moved);
        assertSame(weapon,player.getEquipment().getItems().get(3));assertEquals(0,player.getBank().bankTabs[0].length);
        assertEquals(2,change.commit().moved);assertEquals(0,change.commit().moved);
        assertNull(player.getEquipment().getItems().get(3));assertNull(player.getEquipment().getItems().get(13));
        assertSame(shield,player.getEquipment().getItems().get(5));
        assertEquals(1,bankCount(80001));assertEquals(800,bankCount(80004));
    }
    @Test public void fullBankStillMergesGearAndNeverLosesUnfittingSlotsOrStacks() {
        Item[] bank=new Item[600];for(int slot=0;slot<600;slot++)bank[slot]=new Item(slot+1,1);
        bank[0]=new Item(80001,2);player.getBank().bankTabs=new Item[][]{bank};
        Item shield=new Item(80003,1);player.getEquipment().getItems().set(3,new Item(80001,1));player.getEquipment().getItems().set(5,shield);
        Native950Containers.EquipmentBankChange change=containers.prepareEquipmentDeposit();
        assertEquals(1,change.include(3).moved);assertEquals(0,change.include(5).moved);assertEquals(1,change.commit().moved);
        assertEquals(3,bankCount(80001));assertSame(shield,player.getEquipment().getItems().get(5));
        player.getEquipment().getItems().set(13,new Item(80004,20));player.getBank().bankTabs[0][1]=new Item(80004,Integer.MAX_VALUE-19);
        change=containers.prepareEquipmentDeposit();assertEquals(0,change.include(13).moved);assertEquals(0,change.commit().moved);
        assertEquals(20,player.getEquipment().getItems().get(13).getAmount());assertEquals(Integer.MAX_VALUE-19,bankCount(80004));
    }
    @Test public void equipmentBankCommitRejectsEqualIdentityReplacementAndBankMutation() {
        player.getEquipment().getItems().set(3,new Item(80001,1));
        Native950Containers.EquipmentBankChange change=containers.prepareEquipmentDeposit();change.include(3);
        Item replacement=new Item(80001,1);player.getEquipment().getItems().set(3,replacement);
        assertEquals(0,change.commit().moved);assertSame(replacement,player.getEquipment().getItems().get(3));
        change=containers.prepareEquipmentDeposit();change.include(3);player.getBank().bankTabs=new Item[][]{{new Item(1,1)}};
        assertEquals(0,change.commit().moved);assertSame(replacement,player.getEquipment().getItems().get(3));assertEquals(1,bankCount(1));
    }
    @Test public void nativeBankQuantityQueriesUseTheActiveBankAfterDepositsAndWithdrawals() {
        assertNotNull(player.getBanks());assertTrue(player.getBanks().isEmpty());
        player.getInventory().items.set(17,new Item(80002,20));
        assertEquals(20,containers.deposit(17,80002,20).moved);
        assertEquals(20,bankCount(80002));
        assertEquals(20,player.getBank().getNumberOf(80002));
        assertEquals(20,player.getBank().getNumberOf(new Item(80002,1)));
        assertEquals(0,player.getBank().getNumberOf(80004));
        assertTrue(player.getBank().containsItem(80002,20));
        assertTrue(player.getBank().containsItem(new Item(80002,20)));
        assertFalse(player.getBank().containsItem(80002,21));
        assertFalse(player.getBank().containsItem(80001,1)); // A stored note never proves ownership of an unnoted base.
        assertEquals(7,containers.withdraw(0,80002,7).moved);
        assertEquals(13,player.getBank().getNumberOf(80002));assertEquals(13,bankCount(80002));
        assertTrue(player.getBank().containsItem(80002,13));assertFalse(player.getBank().containsItem(80002,14));
        assertEquals(13,containers.withdraw(0,80002,Integer.MAX_VALUE).moved);
        assertEquals(0,player.getBank().getNumberOf(80002));assertEquals(0,bankCount(80002));
        assertFalse(player.getBank().containsItem(80002,1));
    }
    private int bankCount(int id){for(Item item:player.getBank().bankTabs[0])if(item.getId()==id)return item.getAmount();return 0;}
}