package com.rs.game.player.client;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.player.Skills;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/** Arbitrary synthetic950 definitions prove generic admission without per-ID pins or legacy item names. */
public final class Native950EquipmentTypesTest {
    private static final Native950EquipmentTypes.BasResolver BAS=(d,combat)->combat?2689:2697;
    private static ItemDefinitions definition(int id,int slot){return ItemDefinitions.decodeStrict947(id,raw(slot),null);}
    private static byte[] raw(int slot){return new byte[]{2,'N','e','w',' ','g','e','a','r',0,13,(byte)slot,36,'W','e','a','r',0,0};}
    private static Native950EquipmentTypes.Type type(ItemDefinitions d){return Native950EquipmentTypes.fromDefinition(d,id->true,BAS);}

    @Test public void arbitraryCurrentCacheIdentityGrantsActualEquipmentCapability(){
        ItemDefinitions d=definition(123456,3);d.name="New staff with no legacy identity";d.inventoryOptions[1]="Wield";
        d.equipType=5;d.maleEquip1=90001;d.femaleEquip1=90002;
        Native950EquipmentTypes.Type t=type(d);assertNotNull(t);assertEquals(123456,t.id);assertEquals(3,t.slot);
        assertTrue(t.twoHanded);assertArrayEquals(new int[]{3,5},t.conflictSlots());assertEquals(2697,t.bas);assertEquals(2689,t.combatBas);
        assertTrue(t.isWearOption(2));assertFalse(t.isWearOption(1));assertTrue(t.genderSupported(true));assertTrue(t.genderSupported(false));
    }
    @Test public void allActualWearWieldEquipIndicesAreAcceptedWithoutInventingOtherActions(){
        ItemDefinitions d=definition(54321,0);d.inventoryOptions=new String[]{"Check","Wear","Wield","Equip","wear"};
        Native950EquipmentTypes.Type t=type(d);assertArrayEquals(new int[]{2,3,4,5},t.equipOptions());assertFalse(t.isWearOption(1));
        d.inventoryOptions=new String[]{"Activate",null,null,null,"Drop"};assertNull(type(d));
    }
    @Test public void notesShardsAndNoSlotCannotTurnAnInheritedWearLabelIntoEquipment(){
        ItemDefinitions d=definition(54321,-1);assertNull(type(d));
        d=definition(54321,0);d.noted=true;assertNull(type(d));
        d=definition(54321,0);d.certTemplateId=799;assertNull(type(d));
        d=definition(54321,0);d.shardTemplateId=30052;assertNull(type(d));
        d=definition(54321,19);assertNull(type(d));
        d=definition(54321,0);d.equipType=19;assertNull(type(d));
    }
    @Test public void modelReferencesMustExistButAbsentModelsAndGenderSpecificEquipmentAreValid(){
        ItemDefinitions d=definition(54321,4);d.maleEquip1=500;d.femaleEquip1=-1;
        assertNotNull(Native950EquipmentTypes.fromDefinition(d,id->id==500,BAS));
        d.femaleEquipModelId3=501;assertNull(Native950EquipmentTypes.fromDefinition(d,id->id==500,BAS));
        d=definition(54321,13);d.stackable=1;assertNotNull(Native950EquipmentTypes.fromDefinition(d,id->false,BAS));
        d=definition(54321,3);d.stackable=1;assertTrue(type(d).stackable);
    }
    @Test public void validNonHandEquipmentDoesNotDependOnCombatOrWeaponProfiles(){
        assertNotNull(Native950EquipmentTypes.fromDefinition(definition(54321,17),id->true,(d,c)->{throw new AssertionError("Pocket queried weapon BAS");}));
        assertNull(Native950EquipmentTypes.fromDefinition(definition(54321,3),id->true,(d,c)->-1));
    }
    @Test public void cacheSkillPairsAreMergedAtTheirStrongestLevelWithoutLegacyRewrites(){
        ItemDefinitions d=definition(54321,3);d.name="Iron platebody";d.clientScriptData=new HashMap<>();
        d.clientScriptData.put(749,6);d.clientScriptData.put(750,99);d.clientScriptData.put(751,1);d.clientScriptData.put(752,95);
        d.clientScriptData.put(753,6);d.clientScriptData.put(754,90);
        Map<Integer,Integer> req=type(d).requirements;assertEquals(Integer.valueOf(99),req.get(6));assertEquals(Integer.valueOf(95),req.get(1));assertEquals(2,req.size());
    }
    @Test public void capeAndMasterCapeRequirementsUseCacheSkillAndMasterFlag(){
        ItemDefinitions d=definition(54321,1);d.clientScriptData=new HashMap<>();d.clientScriptData.put(277,28);
        assertEquals(Integer.valueOf(99),type(d).requirements.get(28));d.clientScriptData.put(4244,1);
        assertEquals(Integer.valueOf(120),type(d).requirements.get(28));d.clientScriptData.put(277,0);
        assertEquals(Integer.valueOf(120),type(d).requirements.get(0));
    }
    @Test public void malformedSkillPairsAndUnexpectedTypesCannotSilentlyLoseRequirements(){
        ItemDefinitions d=definition(54321,0);d.clientScriptData=new HashMap<>();d.clientScriptData.put(749,6);assertNotNull(type(d));
        d.clientScriptData.put(750,"99");assertNull(type(d));d.clientScriptData.put(750,99);d.clientScriptData.put(749,Skills.SKILL_COUNT);assertNull(type(d));
        d.clientScriptData.put(749,6);d.clientScriptData.put(750,-1);assertNull(type(d));
    }
    @Test public void currentParamDefaultsAndVirtualLevelThresholdsAreRespected(){
        ItemDefinitions d=definition(54321,1);d.clientScriptData=new HashMap<>();d.clientScriptData.put(750,65);
        assertNotNull(type(d));assertTrue(type(d).requirements.isEmpty());
        d.clientScriptData.put(751,14);assertNotNull(type(d));assertTrue(type(d).requirements.isEmpty());
        d.clientScriptData.put(277,0);d.clientScriptData.put(4244,1);assertEquals(Integer.valueOf(120),type(d).requirements.get(0));
        assertEquals(104273167,Skills.getXPForLevel(0,120));
        d.clientScriptData.put(749,0);d.clientScriptData.put(750,255);assertNull(type(d));
    }
    @Test public void unrelatedIntegerParamsAfterTheSixSkillPairsAreNotTreatedAsSkills(){
        ItemDefinitions d=definition(54321,0);d.clientScriptData=new HashMap<>();
        d.clientScriptData.put(761,40);d.clientScriptData.put(764,8);d.clientScriptData.put(768,-1);
        assertNotNull(type(d));assertTrue(type(d).requirements.isEmpty());
    }
    @Test public void resolvedLentAndBoundTemplatesInheritGenericWearContract(){
        Map<Integer,byte[]> files=new HashMap<>();files.put(40000,raw(3));files.put(40001,new byte[]{0});
        files.put(40002,transform(203,40000,204,40001));files.put(40003,transform(205,40000,206,40001));
        Native950EquipmentTypes.Type lent=Native950EquipmentTypes.resolve(40002,files::get,id->true,BAS);
        Native950EquipmentTypes.Type bound=Native950EquipmentTypes.resolve(40003,files::get,id->true,BAS);
        assertNotNull(lent);assertNotNull(bound);assertEquals(3,lent.slot);assertEquals(40000,lent.sourceId);
        assertEquals(Native950EquipmentTypes.Transform.LENT,lent.transform);assertEquals(Native950EquipmentTypes.Transform.BOUND,bound.transform);
        assertTrue(lent.isWearOption(2));assertFalse(lent.isWearOption(5));
    }
    @Test public void cyclicOrMissingTemplateReferenceCannotYieldEquipment(){
        Map<Integer,byte[]> files=new HashMap<>();files.put(40000,raw(0));files.put(40002,transform(205,40000,206,40001));
        assertNull(Native950EquipmentTypes.resolve(40002,files::get,id->true,BAS));
        files.put(40001,new byte[]{0});files.put(40000,transform(205,40002,206,40001));
        assertNull(Native950EquipmentTypes.resolve(40002,files::get,id->true,BAS));
    }
    @Test public void immutableTypesDoNotLetCallersChangeSlotConflictsOrRequirements(){
        Native950EquipmentTypes.Type t=type(definition(54321,0));int[] options=t.equipOptions();options[0]=5;
        int[] conflicts=t.conflictSlots();conflicts[0]=3;assertTrue(t.isWearOption(2));assertArrayEquals(new int[]{0},t.conflictSlots());
        try{t.requirements.put(6,99);fail("Mutable requirements");}catch(UnsupportedOperationException expected){}
    }
    private static byte[] transform(int baseOp,int base,int templateOp,int template){
        ByteArrayOutputStream out=new ByteArrayOutputStream();out.write(baseOp);u24(out,base);out.write(templateOp);u24(out,template);out.write(0);return out.toByteArray();
    }
    private static void u24(ByteArrayOutputStream out,int n){out.write(n>>>16);out.write(n>>>8);out.write(n);}
}