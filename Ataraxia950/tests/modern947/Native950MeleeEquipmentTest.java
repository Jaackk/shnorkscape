package com.rs.game.player.client;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.player.combat.rs2.ClassicBonuses;
import org.junit.Test;
import static org.junit.Assert.*;

/** Literal index19 files and classic rows from the paired cache/data, with no cache or data root. */
public final class Native950MeleeEquipmentTest {
    private static final byte[] SHIELD = hex("245769656c64000d055e0012178001212719800121270700010800010408320600ba0501870d055e0012b50000000000"
            + "0000a8b209018000ea254128030039161486ad8aaf722976290242726f6e7a652073717561726520736869656c6400c9"
            + "000496cb00b174f91a0000007600001c2f00000b040001212800001166000000010000057500000001000002ed000000"
            + "01000002ee0000000100000b050000000100000b340000004600000b320000003c00000b330000003200000b36000001"
            + "2c000011f4000000050000052e0000000000000b10000000010000021900001045000010640000000100000893000000"
            + "1300000a500000000e00000a550000000100000a510000003e00000a56000000bb00000a730000a90900000a88000000"
            + "0e00000a890000012c00001e79000000c800000a5a0000092b90002d97003300");
    private static final byte[] HELM = hex("2457656172000d000e085e0002178000de12198000e0755a8000dd945b8000dd8b0402920600295f07d6050714245765"
            + "617200b500000000000000a85c800227215d8002271b18800227201a8002271ab209018000e4e4410242726f6e7a6520"
            + "6d65642068656c6d00c9000474cb00b16ff9170000007600001c0600001166000000010000057500000001000002ed00"
            + "000001000002ee0000000100000b050000000100000b340000004600000b320000003c00000b330000003200000b3600"
            + "00012c000011f40000000500000219000010450000106400000001000008930000001300000a500000000e00000a5500"
            + "00000100000a510000003e00000a56000000bb00000a730000a90900000a880000000e00000a890000012c00001e7900"
            + "0000c800000a5a0000092b90002d97003300");
    private static final ClassicBonuses SHIELD_BONUSES = new ClassicBonuses(0, 0, 0, -6, -2, 5, 6, 4, 0, 5, 0, 0, 0, 0);
    private static final ClassicBonuses HELM_BONUSES = new ClassicBonuses(0, 0, 0, -3, -1, 3, 4, 2, -1, 3, 0, 0, 0, 0);

    @Test public void pinnedRenamedKitDefinitionsHaveTheReviewedSlotsAndArmourFlags() {
        assertTrue(Native950MeleeEquipment.acceptsDefinition(1173, SHIELD));
        assertTrue(Native950MeleeEquipment.acceptsDefinition(1139, HELM));
        ItemDefinitions shield = ItemDefinitions.decodeStrict947(1173, SHIELD, null);
        ItemDefinitions helm = ItemDefinitions.decodeStrict947(1139, HELM, null);
        assertEquals("Bronze square shield", shield.getName()); assertEquals(5, shield.getEquipSlot());
        assertTrue(shield.isShield()); assertFalse(shield.isMeleeTypeWeapon());
        assertEquals("Bronze med helm", helm.getName()); assertEquals(0, helm.getEquipSlot());
        assertFalse(helm.isShield()); assertFalse(helm.isMeleeTypeWeapon());
    }
    @Test public void unknownOrChangedDefinitionsCannotUseTheException() {
        assertFalse(Native950MeleeEquipment.acceptsDefinition(1175, SHIELD));
        assertFalse(Native950MeleeEquipment.acceptsDefinition(1173, HELM));
        assertFalse(Native950MeleeEquipment.acceptsDefinition(1139, null));
        byte[] changed = SHIELD.clone(); changed[1] ^= 1;
        assertFalse(Native950MeleeEquipment.acceptsDefinition(1173, changed));
        assertFalse(Native950MeleeEquipment.isVerifiedKitItem(1175));
    }
    @Test public void decodedIdentityAndSlotAreCheckedSeparatelyFromTheHash() {
        assertFalse(Native950MeleeEquipment.matchesMetadata(1173, ItemDefinitions.decodeStrict947(1175, SHIELD, null)));
        // This raw file repeats opcode13 at offsets7 and37; change both slot assignments.
        byte[] wrongSlot = SHIELD.clone(); assertEquals(13, wrongSlot[7]); assertEquals(13, wrongSlot[37]);
        wrongSlot[8] = 0; wrongSlot[38] = 0;
        assertFalse(Native950MeleeEquipment.matchesMetadata(1173, ItemDefinitions.decodeStrict947(1173, wrongSlot, null)));
        byte[] wrongName = SHIELD.clone();
        for (int i = 0; i < wrongName.length - 5; i++) {
            if (wrongName[i] == 'B' && wrongName[i + 1] == 'r' && wrongName[i + 2] == 'o') {wrongName[i] = 'I'; break;}
        }
        assertFalse(Native950MeleeEquipment.matchesMetadata(1173, ItemDefinitions.decodeStrict947(1173, wrongName, null)));
    }
    @Test public void classicAliasIsRestrictedToExactIdAndNativeName() {
        assertEquals("Bronze sq shield", Native950MeleeEquipment.classicName(1173, "Bronze square shield"));
        assertEquals("Bronze med helm", Native950MeleeEquipment.classicName(1139, "Bronze med helm"));
        assertEquals("Bronze square shield", Native950MeleeEquipment.classicName(1175, "Bronze square shield"));
        assertEquals("Iron square shield", Native950MeleeEquipment.classicName(1173, "Iron square shield"));
        assertNull(Native950MeleeEquipment.classicName(1173, null));
    }
    @Test public void classicAdmissionRequiresTheActualCompleteReviewedBonusRow() {
        assertTrue(Native950MeleeEquipment.hasExpectedClassicBonuses(1173, SHIELD_BONUSES));
        assertTrue(Native950MeleeEquipment.hasExpectedClassicBonuses(1139, HELM_BONUSES));
        assertFalse(Native950MeleeEquipment.hasExpectedClassicBonuses(1173, HELM_BONUSES));
        assertFalse(Native950MeleeEquipment.hasExpectedClassicBonuses(1175, SHIELD_BONUSES));
        assertFalse(Native950MeleeEquipment.hasExpectedClassicBonuses(1173, null));
        assertFalse(Native950MeleeEquipment.hasExpectedClassicBonuses(1139, ClassicBonuses.ZERO));
        ClassicBonuses forged = SHIELD_BONUSES.plus(new ClassicBonuses(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0));
        assertFalse(Native950MeleeEquipment.hasExpectedClassicBonuses(1173, forged));
    }
    private static byte[] hex(String value) {
        byte[] bytes = new byte[value.length() / 2];
        for (int i = 0; i < bytes.length; i++) bytes[i] = (byte) Integer.parseInt(value.substring(i * 2, i * 2 + 2), 16);
        return bytes;
    }
}
