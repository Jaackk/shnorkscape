package modern947;

import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.client.Native950Containers;
import com.rs.game.player.client.Native950ItemCatalog;
import com.rs.game.player.client.Native950Save;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public class Native950EquipmentTest {
    private final EmbeddedChannel channel = new EmbeddedChannel();
    private final Player player = Player.createNative950("equipment", new WorldTile(3217,3258,0), channel);
    private final Native950ItemCatalog catalog = new Native950ItemCatalog(Arrays.asList(
            new Native950ItemCatalog.Entry(995,"Coins",true,new String[]{"Add to pouch"}),
            new Native950ItemCatalog.Entry(1511,"Logs",false,new String[]{"Craft"}),
            new Native950ItemCatalog.Entry(315,"Shrimps",false,new String[]{"Eat"}),
            new Native950ItemCatalog.Entry(1277,"Bronze sword",false,new String[]{null,"Wield"},3,2),
            new Native950ItemCatalog.Entry(1173,"Bronze square shield",false,new String[]{null,"Wield"},5,2),
            new Native950ItemCatalog.Entry(1139,"Bronze med helm",false,new String[]{null,"Wear"},0,2)));
    private final Native950Containers containers = new Native950Containers(player,catalog);
    @After public void close() { channel.finishAndReleaseAll(); }

    @Test public void kitRequiresRoomAndCommitsAllThreeOnlyOnce() {
        for(int i=0;i<26;i++) player.getInventory().items.set(i,new Item(315,1));
        assertEquals(0,containers.claimEquipmentKit().moved);
        assertFalse(containers.equipmentKitClaimed());
        assertEquals(0,total(1277)); assertEquals(0,total(1173)); assertEquals(0,total(1139));
        player.getInventory().items.set(25,null);
        assertEquals(3,containers.claimEquipmentKit().moved);
        assertTrue(containers.equipmentKitClaimed());
        assertEquals(0,containers.claimEquipmentKit().moved);
        assertEquals(1,total(1277)); assertEquals(1,total(1173)); assertEquals(1,total(1139));
    }

    @Test public void wearAndRemoveUseActualPlayerStorageAndConserveItems() {
        containers.claimEquipmentKit();
        assertEquals(1,containers.equip(0,1277).moved);
        assertEquals(1,containers.equip(1,1173).moved);
        assertEquals(1,containers.equip(2,1139).moved);
        assertTrue(player.getInventory().items.isEmpty());
        assertEquals(1277,player.getEquipment().getItems().get(3).getId());
        assertEquals(1173,player.getEquipment().getItems().get(5).getId());
        assertEquals(1139,player.getEquipment().getItems().get(0).getId());
        assertEquals(1,containers.unequip(3,1277).moved);
        assertEquals(1277,player.getInventory().items.get(0).getId());
        assertNull(player.getEquipment().getItems().get(3));
        assertEquals(1,total(1277)); assertEquals(1,total(1173)); assertEquals(1,total(1139));
    }

    @Test public void fullBackpackRejectsRemoveButAllowsAnEquipmentSwap() {
        player.getEquipment().getItems().set(3,new Item(1277,1));
        for(int i=0;i<28;i++) player.getInventory().items.set(i,new Item(315,1));
        assertEquals(0,containers.unequip(3,1277).moved);
        assertEquals(1277,player.getEquipment().getItems().get(3).getId());
        player.getInventory().items.set(9,new Item(1277,1));
        assertEquals(1,containers.equip(9,1277).moved);
        assertEquals(1277,player.getInventory().items.get(9).getId());
        assertEquals(2,total(1277));
        player.getInventory().items.set(17,null);
        assertEquals(1,containers.unequip(3,1277).moved);
        assertEquals(1277,player.getInventory().items.get(17).getId());
        assertEquals(2,total(1277));
    }

    @Test public void staleRepeatedAndUnsupportedClicksDoNotMoveItems() {
        containers.claimEquipmentKit();
        assertEquals(0,containers.equip(0,1173).moved);
        assertEquals(0,containers.equip(-1,1277).moved);
        assertEquals(1,containers.equip(0,1277).moved);
        assertEquals(0,containers.equip(0,1277).moved);
        assertEquals(0,containers.unequip(3,1173).moved);
        assertEquals(0,containers.unequip(19,1277).moved);
        assertEquals(1,containers.unequip(3,1277).moved);
        assertEquals(0,containers.unequip(3,1277).moved);
        player.getInventory().items.set(4,new Item(315,1));
        assertEquals(0,containers.equip(4,315).moved);
        assertEquals(1,total(1277)); assertEquals(1,total(1173)); assertEquals(1,total(1139));
    }

    @Test public void bankingAndSaveRestorePreserveEquipmentAndClaim() {
        containers.seedStarterItems();
        containers.claimEquipmentKit();
        containers.equip(11,1277); containers.equip(13,1139);
        assertEquals(1,containers.deposit(12,1173,1).moved);
        Native950Save save=containers.saveSnapshot("equipment",3217,3258,0);
        Player restored=Player.createNative950("equipment",new WorldTile(3217,3258,0),channel);
        Native950Containers next=new Native950Containers(restored,catalog);
        next.restore(save);
        assertEquals(1277,restored.getEquipment().getItems().get(3).getId());
        assertEquals(1139,restored.getEquipment().getItems().get(0).getId());
        assertTrue(next.equipmentKitClaimed()); assertEquals(0,next.claimEquipmentKit().moved);
        assertEquals(1,next.withdraw(0,1173,1).moved);
        assertEquals(1,next.equip(11,1173).moved);
        assertEquals(1173,restored.getEquipment().getItems().get(5).getId());
        assertEquals(1000,restored.getInventory().items.get(0).getAmount());
    }

    @Test public void wrongSavedWearSlotIsRejectedBeforeAnyContainerCommits() {
        int[] inv=new int[28], gear=new int[19], counts=new int[19];
        Arrays.fill(inv,-1); Arrays.fill(gear,-1);
        int[] invCounts=new int[28]; inv[0]=995; invCounts[0]=50;
        gear[5]=1277; counts[5]=1;
        Native950Save save=new Native950Save("equipment",3217,3258,0,inv,invCounts,
                new int[]{315},new int[]{5},gear,counts,true);
        try { containers.restore(save); fail("wrong wear slot accepted"); }
        catch(IllegalArgumentException expected) { }
        assertTrue(player.getInventory().items.isEmpty());
        assertTrue(player.getEquipment().getItems().isEmpty());
        assertEquals(0,player.getBank().bankTabs[0].length); assertFalse(containers.equipmentKitClaimed());
    }

    @Test public void detachedSnapshotsAndWorldThreadOwnershipApplyToEquipment() throws Exception {
        containers.claimEquipmentKit(); containers.equip(0,1277);
        containers.equipmentSnapshot().ids[3]=-1;
        assertEquals(1277,player.getEquipment().getItems().get(3).getId());
        try { CompletableFuture.supplyAsync(() -> containers.unequip(3,1277)).get(3,TimeUnit.SECONDS); fail(); }
        catch(ExecutionException expected) { assertTrue(expected.getCause() instanceof IllegalStateException); }
        assertEquals(1,total(1277));
    }

    private long total(int id) {
        long result=0;
        for(Native950Containers.Snapshot s:Arrays.asList(containers.inventorySnapshot(),containers.bankSnapshot(),containers.equipmentSnapshot()))
            for(int i=0;i<s.ids.length;i++) if(s.ids[i]==id) result+=s.amounts[i];
        return result;
    }
}
