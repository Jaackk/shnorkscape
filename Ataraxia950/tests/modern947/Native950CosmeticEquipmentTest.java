package com.rs.game.player.client;

import com.rs.cache.loaders.ItemDefinitions;
import org.junit.Test;
import static org.junit.Assert.*;

/** Literal paired-cache definitions; requires no cache root or mutable game state. */
public final class Native950CosmeticEquipmentTest {
    private static final String PREFIX="0700010800010401b80607350500792457656172000d001700bb19016b5a800099cc5b8000978bb209010a4b414500000002";
    private static final String SUFFIX="f90300000893000000070000027000000001000002190000105f90002d97003300";
    private static final String[] MIDDLES={
        "0252656420706172747968617400c900040fcb0034db",
        "2801039e2bc00259656c6c6f7720706172747968617400c9000411cb0034dc",
        "2801039eabc002426c756520706172747968617400c9000413cb0034dd",
        "2801039e57c002477265656e20706172747968617400c9000415cb0034de",
        "2801039ec7c002507572706c6520706172747968617400c9000417cb0034df",
        "2801039e007f02576869746520706172747968617400c9000419cb0034e0"
    };
    private static byte[] raw(int row){return hex(PREFIX+MIDDLES[row]+SUFFIX);}
    private static ItemDefinitions green(){return ItemDefinitions.decodeStrict947(1044,raw(3),null);}
    @Test public void allSixExactOrdinaryHatsHaveTheirCurrentMetadataAndBothGenderModels(){
        for(int row=0;row<6;row++)assertTrue(Native950CosmeticEquipment.acceptsDefinition(1038+2*row,raw(row)));
        assertEquals(187,green().getMaleWornModelId1());assertEquals(363,green().getFemaleWornModelId1());
        assertEquals(0,green().equipSlot);assertEquals(-1,green().getEquipType());
    }
    @Test public void namesOrWearStringsDoNotAdmitArbitraryEquipmentOrNotedCounterparts(){
        assertFalse(Native950CosmeticEquipment.acceptsDefinition(1039,raw(0)));
        assertFalse(Native950CosmeticEquipment.acceptsDefinition(1053,raw(3)));
        assertFalse(Native950CosmeticEquipment.acceptsDefinition(1044,raw(1)));
        assertFalse(Native950CosmeticEquipment.acceptsDefinition(1044,null));
        assertFalse(Native950CosmeticEquipment.isVerifiedCosmetic(1053));
        assertFalse(Native950CosmeticEquipment.isVerifiedCosmetic(314));
        byte[] changed=raw(3);changed[10]^=1;assertFalse(Native950CosmeticEquipment.acceptsDefinition(1044,changed));
    }
    @Test public void slotModelsHairHidingAndTransformsAreCheckedSeparatelyFromHash(){
        ItemDefinitions d=green();d.equipSlot=3;assertFalse(Native950CosmeticEquipment.matchesMetadata(1044,d));
        d=green();d.maleEquip1=-1;assertFalse(Native950CosmeticEquipment.matchesMetadata(1044,d));
        d=green();d.femaleEquip1=187;assertFalse(Native950CosmeticEquipment.matchesMetadata(1044,d));
        d=green();d.equipType=8;assertFalse(Native950CosmeticEquipment.matchesMetadata(1044,d));
        d=green();d.certTemplateId=799;assertFalse(Native950CosmeticEquipment.matchesMetadata(1044,d));
        d=green();d.bindTemplateId=799;assertFalse(Native950CosmeticEquipment.matchesMetadata(1044,d));
        d=green();d.stackable=1;assertFalse(Native950CosmeticEquipment.matchesMetadata(1044,d));
    }
    @Test public void unreviewedEffectsAndMissingModelsCannotAcquireNeutralCombatCapability(){
        ItemDefinitions d=green();d.clientScriptData.put(644,2584);assertFalse(Native950CosmeticEquipment.matchesMetadata(1044,d));
        d=green();d.inventoryOptions[1]="Activate";assertFalse(Native950CosmeticEquipment.matchesMetadata(1044,d));
        assertFalse(Native950CosmeticEquipment.acceptsModel(187,null));
        assertFalse(Native950CosmeticEquipment.acceptsModel(363,new byte[]{1,2,3}));
        assertFalse(Native950CosmeticEquipment.acceptsModel(42,new byte[]{1,2,3}));
    }
    @Test public void liveNeutralContractDoesNotDependOnPartyhatIdentityNameModelsOrHeadSlot(){
        ItemDefinitions d=ItemDefinitions.decodeStrict947(333333,raw(3),null);
        d.name="Future cosmetic";d.equipSlot=1;d.maleEquip1=88888;d.femaleEquip1=99999;
        assertTrue(Native950CosmeticEquipment.neutralContract(d,1));
        assertFalse(Native950CosmeticEquipment.neutralContract(d,0));
        assertFalse(Native950CosmeticEquipment.neutralContract(d,-1));
    }
    @Test public void liveNeutralContractRejectsUnreviewedStatsEffectsTypesAndCertificateIdentity(){
        ItemDefinitions d=green();assertTrue(Native950CosmeticEquipment.neutralContract(d,0));
        d.clientScriptData.put(8881,50);assertFalse(Native950CosmeticEquipment.neutralContract(d,0));
        d=green();d.clientScriptData.put(2195,"7");assertFalse(Native950CosmeticEquipment.neutralContract(d,0));
        d=green();d.clientScriptData.put(624,0);assertFalse(Native950CosmeticEquipment.neutralContract(d,0));
        d=green();d.clientScriptData.put(537,4166);assertFalse(Native950CosmeticEquipment.neutralContract(d,0));
        d=green();d.noted=true;assertFalse(Native950CosmeticEquipment.neutralContract(d,0));
        d=green();d.certTemplateId=799;assertFalse(Native950CosmeticEquipment.neutralContract(d,0));
    }
    private static byte[] hex(String text){byte[] result=new byte[text.length()/2];for(int i=0;i<result.length;i++)result[i]=(byte)Integer.parseInt(text.substring(i*2,i*2+2),16);return result;}
}