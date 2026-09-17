package com.rs.game.player.client;

import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

/** Arbitrary metadata IDs keep container correctness independent of any admitted item list. */
public class Native950EquipmentTransactionsTest {
    private final EmbeddedChannel channel = new EmbeddedChannel();
    private final Player player = Player.createNative950("exchange", new WorldTile(3217,3258,0), channel);
    private final Native950ItemCatalog catalog = new Native950ItemCatalog(Arrays.asList(
        new Native950ItemCatalog.Entry(80001,"First weapon",false,new String[]{null,"Wield"},3,2),
        new Native950ItemCatalog.Entry(80002,"Second weapon",false,new String[]{null,"Wield"},3,2),
        new Native950ItemCatalog.Entry(80003,"Offhand",false,new String[]{null,"Wield"},5,2),
        new Native950ItemCatalog.Entry(80004,"Ammunition",true,new String[]{null,"Equip"},13,2),
        new Native950ItemCatalog.Entry(80005,"Different ammunition",true,new String[]{null,"Wield"},13,2),
        new Native950ItemCatalog.Entry(80006,"Resource",false,new String[]{null,null,null,null,"Drop"})));
    private final Native950Containers containers = new Native950Containers(player,catalog);
    @After public void close(){channel.finishAndReleaseAll();}

    @Test public void twoHandedSwapNeedsSpaceForBothDisplacedItemsAndCommitsAtomically(){
        player.getEquipment().getItems().set(3,new Item(80001,1));
        player.getEquipment().getItems().set(5,new Item(80003,1));
        fill();player.getInventory().items.set(7,new Item(80002,1));
        Item original=player.getInventory().items.get(7);
        assertEquals(0,containers.prepareEquip(7,80002,new int[]{3,5}).commit().moved);
        assertSame(original,player.getInventory().items.get(7));assertEquals(80003,worn(5).getId());
        player.getInventory().items.set(15,null);
        assertEquals(1,containers.prepareEquip(7,80002,new int[]{3,5}).commit().moved);
        assertEquals(80002,worn(3).getId());assertNull(worn(5));
        assertEquals(80001,player.getInventory().items.get(7).getId());
        assertEquals(80003,player.getInventory().items.get(15).getId());
        assertEquals(1,total(80001));assertEquals(1,total(80002));assertEquals(1,total(80003));
    }
    @Test public void ammunitionMergeAndRemoveWorkWithFullBackpackAndNeverOverflow(){
        player.getEquipment().getItems().set(13,new Item(80004,20));
        fill();player.getInventory().items.set(4,new Item(80004,30));
        assertEquals(30,containers.prepareEquip(4,80004,new int[]{13}).commit().moved);
        assertEquals(50,worn(13).getAmount());assertNull(player.getInventory().items.get(4));
        player.getInventory().items.set(4,new Item(80004,Integer.MAX_VALUE-49));
        assertEquals(0,containers.unequip(13,80004).moved);assertEquals(50,worn(13).getAmount());
        player.getInventory().items.set(4,new Item(80004,Integer.MAX_VALUE-50));
        assertEquals(50,containers.unequip(13,80004).moved);
        assertEquals(Integer.MAX_VALUE,player.getInventory().items.get(4).getAmount());assertNull(worn(13));
        player.getEquipment().getItems().set(13,new Item(80004,1));
        assertEquals(0,containers.equip(4,80004).moved);assertEquals(1,worn(13).getAmount());
    }
    @Test public void switchingAmmunitionKeepsEntireStacksAndSaveRestoresQuantity(){
        player.getEquipment().getItems().set(13,new Item(80004,100));
        player.getInventory().items.set(3,new Item(80005,500));
        assertEquals(500,containers.equip(3,80005).moved);
        assertEquals(100,player.getInventory().items.get(3).getAmount());
        assertEquals(500,worn(13).getAmount());
        Native950Save save=containers.saveSnapshot("exchange",3217,3258,0);
        EmbeddedChannel other=new EmbeddedChannel();try{
            Player restored=Player.createNative950("exchange",new WorldTile(3217,3258,0),other);
            Native950Containers restoredContainers=new Native950Containers(restored,catalog);restoredContainers.restore(save);
            assertEquals(500,restoredContainers.equipmentSnapshot().amounts[13]);
            assertEquals(80005,restoredContainers.equipmentSnapshot().ids[13]);
        }finally{other.finishAndReleaseAll();}
    }
    @Test public void callbacksCannotReplaceSourceOrAlterQuantityBetweenPlanAndCommit(){
        player.getInventory().items.set(0,new Item(80004,10));
        Native950Containers.EquipmentChange plan=containers.prepareEquip(0,80004,new int[]{13});
        player.getInventory().items.set(0,new Item(80004,10));
        assertEquals(0,plan.commit().moved);assertNull(worn(13));
        plan=containers.prepareEquip(0,80004,new int[]{13});player.getInventory().items.get(0).setAmount(11);
        assertEquals(0,plan.commit().moved);assertNull(worn(13));
        plan=containers.prepareEquip(0,80004,new int[]{13});assertEquals(11,plan.commit().moved);
        assertEquals(0,plan.commit().moved);assertEquals(11,total(80004));
    }
    @Test public void skillRequirementsUseSuppliedBaseLevelsAndReportAllMissingSkills(){
        Map<Integer,Integer> required=new LinkedHashMap<Integer,Integer>();required.put(Skills.MAGIC,99);required.put(Skills.DEFENCE,80);
        assertEquals("You need Magic 99, Defence 80 to equip this item.",Native950EquipmentActions.missingRequirements(required,skill->1));
        assertEquals("You need Defence 80 to equip this item.",Native950EquipmentActions.missingRequirements(required,skill->skill==Skills.MAGIC?99:1));
        assertNull(Native950EquipmentActions.missingRequirements(required,skill->120));
        required.put(99,1);assertNotNull(Native950EquipmentActions.missingRequirements(required,skill->120));
    }
    @Test public void virtualLevelRequirementsUseStoredXpWithoutBaseLevelCapOrBoosts(){
        Map<Integer,Integer> required=new LinkedHashMap<Integer,Integer>();required.put(Skills.ATTACK,120);
        int threshold=Skills.getXPForLevel(Skills.ATTACK,120);
        assertNotNull(Native950EquipmentActions.missingRequirementsFromXp(required,skill->threshold-1));
        assertNull(Native950EquipmentActions.missingRequirementsFromXp(required,skill->threshold));
        required.clear();required.put(Skills.MAGIC,99);
        assertNotNull(Native950EquipmentActions.missingRequirementsFromXp(required,skill->0));
    }
    @Test public void authoredEquipmentRemovalLockIsNotBypassed(){
        com.rs.cache.loaders.ItemDefinitions d=com.rs.cache.loaders.ItemDefinitions.decodeStrict947(80001,new byte[]{0},null);
        assertTrue(Native950EquipmentActions.cacheAllowsRemoval(d));
        d.clientScriptData=new java.util.HashMap<Integer,Object>();d.clientScriptData.put(2091,1);
        assertFalse(Native950EquipmentActions.cacheAllowsRemoval(d));
        assertFalse(Native950EquipmentActions.cacheAllowsRemoval(null));
    }
    private Item worn(int slot){return player.getEquipment().getItems().get(slot);}
    private void fill(){for(int slot=0;slot<28;slot++)player.getInventory().items.set(slot,new Item(80006,1));}
    private long total(int id){long result=0;for(Native950Containers.Snapshot s:Arrays.asList(containers.inventorySnapshot(),containers.equipmentSnapshot()))for(int slot=0;slot<s.ids.length;slot++)if(s.ids[slot]==id)result+=s.amounts[slot];return result;}
}
