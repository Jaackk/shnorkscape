package com.rs.game.player.combat;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.content.Combat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

/**
 * @author Jetset I'm not sure if this is the right name to pick or place to put
 *         it
 *         <p>
 *         http://runescape.wikia.com/wiki/Attack_range
 */
public class CombatUtils {

    public static final boolean fullGuthanEquipped(final Player player) {
        final int helmId = player.getEquipment().getHatId();
        final int chestId = player.getEquipment().getChestId();
        final int legsId = player.getEquipment().getLegsId();
        final int weaponId = player.getEquipment().getWeaponId();
        if (helmId == -1 || chestId == -1 || legsId == -1 || weaponId == -1) {
            return false;
        }
        return ItemDefinitions.getItemDefinitions(helmId).getName().contains("Guthan") && ItemDefinitions.getItemDefinitions(chestId).getName().contains("Guthan") && ItemDefinitions.getItemDefinitions(legsId).getName().contains("Guthan") && ItemDefinitions.getItemDefinitions(weaponId).getName().contains("Guthan");
    }

    public static final boolean berserkerNecklace(final Player player) {
        final int amuletId = player.getEquipment().getAmuletId();
        final int weaponId = player.getEquipment().getWeaponId();
        if (amuletId == 11128) {
            return weaponId == 6527 || weaponId == 6539 || weaponId == 6523 || weaponId == 6526;
        }
        return false;
    }

    public static final boolean fullVanguardEquipped(final Player player) {
        final int helmId = player.getEquipment().getHatId();
        final int chestId = player.getEquipment().getChestId();
        final int legsId = player.getEquipment().getLegsId();
        final int weaponId = player.getEquipment().getWeaponId();
        final int bootsId = player.getEquipment().getBootsId();
        final int glovesId = player.getEquipment().getGlovesId();
        if (helmId == -1 || chestId == -1 || legsId == -1 || weaponId == -1 || bootsId == -1 || glovesId == -1) {
            return false;
        }
        return ItemDefinitions.getItemDefinitions(helmId).getName().contains("Vanguard") && ItemDefinitions.getItemDefinitions(chestId).getName().contains("Vanguard") && ItemDefinitions.getItemDefinitions(legsId).getName().contains("Vanguard") && ItemDefinitions.getItemDefinitions(weaponId).getName().contains("Vanguard") && ItemDefinitions.getItemDefinitions(bootsId).getName().contains("Vanguard") && ItemDefinitions.getItemDefinitions(glovesId).getName().contains("Vanguard");
    }

    public static final boolean usingGoliathGloves(final Player player) {
        final String name = player.getEquipment().getItem(Equipment.SLOT_SHIELD) != null ? player.getEquipment().getItem(Equipment.SLOT_SHIELD).getDefinitions().getName().toLowerCase() : "";
        if (player.getEquipment().getItem((Equipment.SLOT_HANDS)) != null) {
            if (player.getEquipment().getItem(Equipment.SLOT_HANDS).getDefinitions().getName().toLowerCase().contains("goliath") && player.getEquipment().getWeaponId() == -1) {
                if (name.contains("defender") && name.contains("dragonfire shield")) {
                    return true;
                }
                return true;
            }
        }
        return false;
    }

    public static final boolean fullDharokEquipped(final Player player) {
        final int helmId = player.getEquipment().getHatId();
        final int chestId = player.getEquipment().getChestId();
        final int legsId = player.getEquipment().getLegsId();
        final int weaponId = player.getEquipment().getWeaponId();
        if (helmId == -1 || chestId == -1 || legsId == -1 || weaponId == -1) {
            return false;
        }
        return ItemDefinitions.getItemDefinitions(helmId).getName().contains("Dharok's") && ItemDefinitions.getItemDefinitions(chestId).getName().contains("Dharok's") && ItemDefinitions.getItemDefinitions(legsId).getName().contains("Dharok's") && ItemDefinitions.getItemDefinitions(weaponId).getName().contains("Dharok's");
    }

    public static final boolean fullVeracsEquipped(final Player player) {
        final int helmId = player.getEquipment().getHatId();
        final int chestId = player.getEquipment().getChestId();
        final int legsId = player.getEquipment().getLegsId();
        final int weaponId = player.getEquipment().getWeaponId();
        if (helmId == -1 || chestId == -1 || legsId == -1 || weaponId == -1) {
            return false;
        }
        return ItemDefinitions.getItemDefinitions(helmId).getName().contains("Verac's") && ItemDefinitions.getItemDefinitions(chestId).getName().contains("Verac's") && ItemDefinitions.getItemDefinitions(legsId).getName().contains("Verac's") && ItemDefinitions.getItemDefinitions(weaponId).getName().contains("Verac's");
    }

    public static final boolean fullAkrisaesEquipped(final Player player) {
        final int helmId = player.getEquipment().getHatId();
        final int chestId = player.getEquipment().getChestId();
        final int legsId = player.getEquipment().getLegsId();
        final int weaponId = player.getEquipment().getWeaponId();
        if (helmId == -1 || chestId == -1 || legsId == -1 || weaponId == -1) {
            return false;
        }
        return ItemDefinitions.getItemDefinitions(helmId).getName().contains("Akrisae's") && ItemDefinitions.getItemDefinitions(chestId).getName().contains("Akrisae's") && ItemDefinitions.getItemDefinitions(legsId).getName().contains("Akrisae's") && ItemDefinitions.getItemDefinitions(weaponId).getName().contains("Akrisae's");
    }

    public static final boolean fullLinzasEquipped(final Player player) {
        final int helmId = player.getEquipment().getHatId();
        final int chestId = player.getEquipment().getChestId();
        final int legsId = player.getEquipment().getLegsId();
        final int weaponId = player.getEquipment().getWeaponId();
        final int shieldId = player.getEquipment().getShieldId();
        if (helmId == -1 || chestId == -1 || legsId == -1 || weaponId == -1 || shieldId == -1) {
            return false;
        }
        return ItemDefinitions.getItemDefinitions(helmId).getName().contains("Linza's") && ItemDefinitions.getItemDefinitions(chestId).getName().contains("Linza's") && ItemDefinitions.getItemDefinitions(legsId).getName().contains("Linza's") && ItemDefinitions.getItemDefinitions(weaponId).getName().contains("Linza's") && ItemDefinitions.getItemDefinitions(shieldId).getName().contains("Linza's");
    }

    public static final boolean fullAhrimsEquipped(final Player player) {
        final int helmId = player.getEquipment().getHatId();
        final int chestId = player.getEquipment().getChestId();
        final int legsId = player.getEquipment().getLegsId();
        final int weaponId = player.getEquipment().getWeaponId();
        if (helmId == -1 || chestId == -1 || legsId == -1 || weaponId == -1) {
            return false;
        }
        return ItemDefinitions.getItemDefinitions(helmId).getName().contains("Ahrim's") && ItemDefinitions.getItemDefinitions(chestId).getName().contains("Ahrim's") && ItemDefinitions.getItemDefinitions(legsId).getName().contains("Ahrim's") && ItemDefinitions.getItemDefinitions(weaponId).getName().contains("Ahrim's");
    }

    public static final boolean fullToragsEquipped(final Player player) {
        final int helmId = player.getEquipment().getHatId();
        final int chestId = player.getEquipment().getChestId();
        final int legsId = player.getEquipment().getLegsId();
        final int weaponId = player.getEquipment().getWeaponId();
        if (helmId == -1 || chestId == -1 || legsId == -1 || weaponId == -1) {
            return false;
        }
        return ItemDefinitions.getItemDefinitions(helmId).getName().contains("Torag's") && ItemDefinitions.getItemDefinitions(chestId).getName().contains("Torag's") && ItemDefinitions.getItemDefinitions(legsId).getName().contains("Torag's") && ItemDefinitions.getItemDefinitions(weaponId).getName().contains("Torag's");
    }

    public static final boolean fullKarilsEquipped(final Player player) {
        final int helmId = player.getEquipment().getHatId();
        final int chestId = player.getEquipment().getChestId();
        final int legsId = player.getEquipment().getLegsId();
        final int weaponId = player.getEquipment().getWeaponId();
        if (helmId == -1 || chestId == -1 || legsId == -1 || weaponId == -1) {
            return false;
        }
        return ItemDefinitions.getItemDefinitions(helmId).getName().contains("Karil's") && ItemDefinitions.getItemDefinitions(chestId).getName().contains("Karil's") && ItemDefinitions.getItemDefinitions(legsId).getName().contains("Karil's") && ItemDefinitions.getItemDefinitions(weaponId).getName().contains("Karil's");
    }

    public static final VoidType getWornVoidType(final Player player, final VoidCombatType combatType) {
        val equipment = player.getEquipment();
        val helm = equipment.getItem(Equipment.SLOT_HAT);
        if (helm == null) {
            return null;
        }
        VoidType type = VoidType.SUPERIOR_ELITE_VOID;
        String name = helm.getName();
        val typeString = combatType.toString();
        if (!name.equals("Superior void knight " + typeString + " helm")) {
            type = VoidType.ELITE_VOID;
            if (!name.equals("Void knight " + typeString + " helm")) {
                return null;
            }
        }
        val shield = equipment.getItem(Equipment.SLOT_SHIELD);
        name = shield == null ? "null" : shield.getName();
        VoidType deflector = name.equals("Superior void knight deflector") ? VoidType.SUPERIOR_VOID : name.equalsIgnoreCase("Void knight deflector") ? VoidType.REGULAR_VOID : null;

        val chest = equipment.getItem(Equipment.SLOT_CHEST);
        name = chest == null ? "null" : chest.getName();
        if (chest == null) {
            if (deflector == null) {
                return null;
            }
            if (deflector != VoidType.SUPERIOR_VOID) {
                type = VoidType.REGULAR_VOID;
            }
            deflector = null;
            if (type.isElite()) {
                type = type.removeElite();
            }
        } else {
            if (!name.equals("Superior elite void knight top")) {
                if (type.isSuperior()) {
                    type = type.removeSuperior();
                }
                if (!name.equals("Elite void knight top")) {
                    type = VoidType.REGULAR_VOID;
                    if (!name.equals("Void knight top")) {
                        if (deflector == null) {
                            return null;
                        }
                        deflector = null;
                    }
                }
            }
        }

        val legs = equipment.getItem(Equipment.SLOT_LEGS);
        name = legs == null ? "null" : legs.getName();
        if (legs == null) {
            if (deflector == null) {
                return null;
            }
            if (deflector != VoidType.SUPERIOR_VOID) {
                type = VoidType.REGULAR_VOID;
            }
            deflector = null;
            if (type.isElite()) {
                type = type.removeElite();
            }
        } else {
            if (!name.equals("Superior elite void knight robe")) {
                if (type.isSuperior()) {
                    type = type.removeSuperior();
                }
                if (!name.equals("Elite void knight top")) {
                    type = VoidType.REGULAR_VOID;
                    if (!name.equals("Void knight robe")) {
                        if (deflector == null) {
                            return null;
                        }
                        deflector = null;
                    }
                }
            }
        }

        val gloves = equipment.getItem(Equipment.SLOT_HANDS);
        name = gloves == null ? "null" : gloves.getName();
        if (gloves == null) {
            if (deflector == null) {
                return null;
            }
            if (deflector != VoidType.SUPERIOR_VOID) {
                type = type.removeSuperior();
            }
            deflector = null;
        } else {
            if (!name.equals("Superior void knight gloves")) {
                if (deflector != VoidType.SUPERIOR_VOID && type.isSuperior()) {
                    type = type.removeSuperior();
                }
                if (!name.equals("Void knight gloves")) {
                    if (deflector == null) {
                        return null;
                    }
                    deflector = null;
                }
            }
        }
        return type;
    }

    public static boolean hasDefenderOrEquivalentEffect(Player player) {
        int shieldId = player.getEquipment().getShieldId();
        if (shieldId == -1)
            return false;
        int attackStyle = player.getCombatDefinitions().getStyle(false);
        int attackType = Combat.getStyleType(attackStyle);
        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(shieldId);
        int defenderType = defs.getName().toLowerCase().contains(" defender") ? Combat.MELEE_TYPE :  defs.getName().toLowerCase().contains(" repriser") ? Combat.RANGE_TYPE : 
            defs.getName().toLowerCase().contains(" rebounder") || defs.getName().toLowerCase().contains("ancient lantern") ? Combat.MAGIC_TYPE : -1;
        return attackType == defenderType;
    }

    public enum VoidCombatType {
        MAGE, RANGER, MELEE;

        @Override
        public String toString() {
            return name().toLowerCase();
        }

    }

    @AllArgsConstructor
    public enum VoidType {

        REGULAR_VOID("The set provides 3% melee accuracy and 5% melee damage.", "The set provides 3% ranged accuracy and 5% range damage.", "The set provides 3% magic accuracy and 5% magic damage."),

        ELITE_VOID("The set provides 3% melee accuracy and 5% melee damage.", "The set provides 3% ranged accuracy and 5% range damage.", "The set provides 3% magic accuracy and 5% magic damage."),

        SUPERIOR_VOID("The set provides 3% melee accuracy and 7% melee damage.", "The set provides 3% ranged accuracy and 7% range damage.", "The set provides 3% magic accuracy and 7% magic damage."),

        SUPERIOR_ELITE_VOID("The set provides 3% melee accuracy and 7% melee damage.", "The set provides 3% ranged accuracy and 7% range damage.", "The set provides 3% magic accuracy and 7% magic damage.");

        @Getter
        private final String meleeBoost, rangedBoost, magicBoost;

        @Override
        public String toString() {
            return name().toLowerCase().replaceAll("_", " ");
        }

        private boolean isElite() {
            return equals(ELITE_VOID) || equals(SUPERIOR_ELITE_VOID);
        }

        private boolean isSuperior() {
            return equals(SUPERIOR_VOID) || equals(SUPERIOR_ELITE_VOID);
        }

        private VoidType removeElite() {
            if (equals(SUPERIOR_ELITE_VOID)) {
                return SUPERIOR_VOID;
            }
            if (equals(ELITE_VOID)) {
                return REGULAR_VOID;
            }
            return this;
        }

        private VoidType removeSuperior() {
            if (equals(SUPERIOR_ELITE_VOID)) {
                return ELITE_VOID;
            }
            if (equals(SUPERIOR_VOID)) {
                return REGULAR_VOID;
            }
            return this;
        }
    }
}
