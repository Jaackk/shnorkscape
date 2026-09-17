package com.rs.game.player.client;

import org.junit.Test;
import static org.junit.Assert.*;

/** Validate declared animation dependencies, not the legacy BAS constructor's synthetic zero defaults. */
public class Native950EquipmentAnimationsTest {
    @Test public void aDeclaredMovementSetRequiresEveryReferencedSequence() {
        byte[] bas={1,0,12,0,13,6,0,14,0};
        assertTrue(Native950EquipmentAnimations.validate(500,bas,id -> id>=12&&id<=14?new byte[]{0}:null));
        assertFalse(Native950EquipmentAnimations.validate(500,bas,id -> id==14?null:new byte[]{0}));
        assertFalse(Native950EquipmentAnimations.validate(500,bas,id -> new byte[]{(byte)255,0}));
    }
    @Test public void absentZeroDefaultsAreNotSequenceReferences() {
        assertTrue(Native950EquipmentAnimations.validate(500,new byte[]{1,0,12,0,13,0},
                id -> id==12||id==13?new byte[]{0}:null));
        assertTrue(Native950EquipmentAnimations.validate(500,new byte[]{1,0,0,0,13,0},
                id -> id==0||id==13?new byte[]{0}:null));
        assertFalse(Native950EquipmentAnimations.validate(500,new byte[]{1,0,0,0,13,0},
                id -> id==13?new byte[]{0}:null));
    }
    @Test public void optionalLoopsAreDependenciesAndMalformedBasIsRejected() {
        byte[] bas={52,1,0,77,1,0,0};
        assertTrue(Native950EquipmentAnimations.validate(500,bas,id -> id==77?new byte[]{0}:null));
        assertFalse(Native950EquipmentAnimations.validate(500,bas,id -> null));
        assertFalse(Native950EquipmentAnimations.validate(500,new byte[]{0},id -> new byte[]{0}));
        assertFalse(Native950EquipmentAnimations.validate(500,new byte[]{1,0,12},id -> new byte[]{0}));
        assertFalse(Native950EquipmentAnimations.validate(500,new byte[]{1,0,12,0,13,0,9},id -> new byte[]{0}));
        assertFalse(Native950EquipmentAnimations.validate(65535,bas,id -> new byte[]{0}));
    }
}
