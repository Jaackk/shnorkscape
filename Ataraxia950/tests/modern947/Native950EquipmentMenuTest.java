package com.rs.game.player.client;

import com.rs.cache.loaders.ItemDefinitions;
import java.util.HashMap;
import org.junit.Test;
import static org.junit.Assert.*;

public final class Native950EquipmentMenuTest {
    private static ItemDefinitions item(int id) {
        ItemDefinitions d=ItemDefinitions.decodeStrict947(id,new byte[]{2,'T','e','s','t',0,13,0,0},null);
        d.inventoryOptions=new String[]{"Activate","Wield",null,"Configure","Drop"};
        return d;
    }
    @Test public void ordinaryAndSwappedAuthoredWearCaptionsResolveOnlyTheirActualCacheOption() {
        ItemDefinitions ordinary=item(58486);
        assertEquals(2,Native950InventoryMenu.equipmentCacheOption(ordinary,2,false));
        assertEquals(0,Native950InventoryMenu.equipmentCacheOption(ordinary,1,false));
        for(int id:new int[]{13561,24199,19865,27616,27996,20709}) {
            ItemDefinitions swapped=item(id);
            assertEquals(2,Native950InventoryMenu.equipmentCacheOption(swapped,1,false));
            assertEquals(0,Native950InventoryMenu.equipmentCacheOption(swapped,2,false));
        }
    }
    @Test public void categoryAndParamFamiliesFollowNativeDispatchPrecedence() {
        ItemDefinitions d=item(123456);d.itemCategory=3464;
        assertEquals(2,Native950InventoryMenu.equipmentCacheOption(d,1,false));
        d.itemCategory=0;d.clientScriptData=new HashMap<>();d.clientScriptData.put(4840,1);
        assertEquals(2,Native950InventoryMenu.equipmentCacheOption(d,1,false));
        d.clientScriptData.put(6799,1);
        assertEquals(2,Native950InventoryMenu.equipmentCacheOption(d,2,false));
        assertEquals(0,Native950InventoryMenu.equipmentCacheOption(d,1,false));
    }
    @Test public void excaliburSettingChangesWhichCaptionWieldsWithoutActivatingIt() {
        for(int id:new int[]{35,14632,36619,36620})for(boolean swap:new boolean[]{false,true}) {
            ItemDefinitions d=item(id);
            assertEquals(2,Native950InventoryMenu.equipmentCacheOption(d,swap?1:2,swap));
            assertEquals(0,Native950InventoryMenu.equipmentCacheOption(d,swap?2:1,swap));
        }
    }
    @Test public void questBranchesPreserveWearButDoNotBorrowConditionalActions() {
        for(int id:new int[]{21581,28575,42682,44155}) {
            ItemDefinitions d=item(id);
            assertEquals(2,Native950InventoryMenu.equipmentCacheOption(d,2,false));
            d.inventoryOptions[3]="Wear";
            assertEquals(0,Native950InventoryMenu.equipmentCacheOption(d,7,false));
        }
    }
    @Test public void equipmentTranslationNeverAdmitsDropDestroyDisassembleOrUnknownSpecialMenus() {
        for(int id:new int[]{58486,35,13561,21581,42682,44155,5509})for(String action:new String[]{"Drop","Destroy","Disassemble"}) {
            ItemDefinitions d=item(id);d.inventoryOptions[4]=action;
            for(boolean swap:new boolean[]{false,true})assertEquals(0,Native950InventoryMenu.equipmentCacheOption(d,8,swap));
        }
        assertEquals(0,Native950InventoryMenu.equipmentCacheOption(item(5509),2,false));
        assertFalse(Native950InventoryMenu.usesOrdinaryOperations(35,2752,0,0));
    }
    @Test public void arbitraryWearOptionIndicesUseAuthoredCaptionSlotsOnly() {
        ItemDefinitions d=item(123456);d.inventoryOptions=new String[]{"Wear","Wield","Equip","Wear","Wield"};
        for(int[] pair:new int[][]{{1,1},{2,2},{3,3},{7,4},{8,5}})
            assertEquals(pair[1],Native950InventoryMenu.equipmentCacheOption(d,pair[0],false));
        for(int op:new int[]{-1,0,4,5,6,9,10})assertEquals(0,Native950InventoryMenu.equipmentCacheOption(d,op,false));
    }
}
