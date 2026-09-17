package com.rs.game.player.content;

import com.rs.Settings;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Entity;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.npc.eds.EliteDungeonNPC;
import com.rs.game.player.*;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.actions.invention.InventionConstants.Perks;
import com.rs.game.player.actions.invention.InventionData.Perk;
import com.rs.game.player.combat.PlayerCombat;
import com.rs.game.player.combat.rs2.Rs2AtaraxiaNumerics;
import com.rs.utils.Utils;

/**
 * Handles everything related to combat configurations.
 *
 * @author Noel
 */
public final class Combat {
    public static final int MELEE_TYPE = 0, RANGE_TYPE = 1, MAGIC_TYPE = 2, ALL_TYPE = 3;
    public static final int SILVER_LIGHT_WEAKNESS = 1, BALMUNG_WEAKNESS = 2, BLISTERWOOD_WEAKNESS = 3, KERIS_DAGGER_WEAKNESS = 4, SALVE_AMULET_WEAKNESS = 5, BANE_AMMUNITION_WEAKNESS = 6;
    public static final int NONE_STYLE = 0, STAB_STYLE = 5, SLASH_STYLE = 6, CRUSH_STYLE = 7, ARROW_STYLE = 8, BOLT_STYLE = 9, THROWN_STYLE = 10;
    public static final int TYPE_AIR = 1, TYPE_WATER = 2, TYPE_EARTH = 3, TYPE_FIRE = 4;
    public static final int NO_SPELL_SELECTED_STYLE = 1337;





    /**
	 * Checks if the target has anti-dragon protection.
	 *
	 * @param target The target.
	 * @return if has protection.
	 */
	public static boolean hasAntiDragProtection(Entity target) {
		if (target instanceof NPC)
			return false;
		Player p2 = (Player) target;
		int shieldId = p2.getEquipment().getShieldId();
		return shieldId == 1540 || shieldId == 11283 || shieldId == 11284 || p2.getPerkManager().hasPerkActive(DonationPerk.DRAGON_TRAINER);
	}

	/**
	 * Gets the dragonfire protect message.
	 *
	 * @param player The player.
	 * @return The message to send, or {@code null} if the player was unprotected.
	 */
	public static final String getProtectMessage(Player player) {
		boolean hasFireImmune = player.getFireImmune() > Utils.currentTimeMillis() && player.getFireImmune() != 0;
		boolean hasFirePrayerProtection = player.getPrayer().isMageProtecting();
		if (player.getPerkManager().hasPerkActive(DonationPerk.DRAGON_TRAINER))
			return "Your dragon trainer perk fully absorbs the heat of the dragon's breath!";
		if (player.getSuperAntiFire() > Utils.currentTimeMillis())
			return "Your potion fully absorbs the heat of the dragon's breath!";
		if (hasFireImmune && hasFirePrayerProtection)
			return "Your prayer and potion fully absorbs the heat of the dragon's breath!";
		if (hasDFS(player) && hasFirePrayerProtection)
			return "Your prayer and shield fully absorbs the heat of the dragon's breath!";
		if (hasFireImmune && hasDFS(player))
			return "Your potion and shield fully absorbs the heat of the dragon's breath!";
		if (hasDFS(player))
			return "Your shield absorbs some of the dragon's breath!";
		if (hasFireImmune)
			return "Your potion absorbs some of the dragon's breath!";
		if (hasFirePrayerProtection)
			return "Your prayer absorbs some of the dragon's breath!";
		return null;
	}

    /**
     * Gets the Slayer level required to attack certain slayer NPC's.
     *
     * @param id The NPC Id.
     * @return the slayer level required.
     */
    public static int getSlayerLevelForNPC(int id) {
        switch (id) {
        case 24170:
        case 14696:
        case 14698:
            return 95;
        case 6221:
        case 6231:
        case 6257:
        case 6278:
            return 83;
        case 24171:
            return 98;
        case 24172:
            return 101;
        case 18621:
        case 18622:
            return 92;
        case 19109:
            return 90;
        case 9463:
            return 93;
        case 2783:
            return 90;
        case 14688:
            return 88;
        case 1615:
            return 85;
        case 14700:
            return 82;
        case 17144:
        case 17145:
        case 17146:
        case 17147:
        case 17148:
        case 17149:
        case 17150:
            return 81;
        case 1610:
            return 75;
        default:
            return 0;
        }
    }

    /**
     * Gets the defence combat animation to show.
     *
     * @param target The target animating.
     * @return the animation id as Integer.
     */
    public static int getDefenceEmote(Entity target) {
        if (target instanceof NPC) {
            NPC n = (NPC) target;
            if(n instanceof EliteDungeonNPC)
                return ((EliteDungeonNPC) n).getDefenceEmote();
            return n.getCombatDefinitions().getDefenceEmote();
        } else {
            Player p = (Player) target;
            if (p.isShadow())
                return 28222;
            if (p.getEquipment().getShieldId() != -1)
                return 18346;
            Item weapon = p.getEquipment().getItem(Equipment.SLOT_WEAPON);
            if (weapon == null)
                return 18346;
            int emote = weapon.getDefinitions().getCombatOpcode(2917);
            return emote == 0 ? 18346 : emote;
        }
    }

	/**
	 * Checks if the Player has a dragonfire-shield.
	 *
	 * @param player The player to check.
	 * @return if has protection.
	 */
	public static boolean hasDFS(Player player) {
		int shieldId = player.getEquipment().getShieldId();
		return shieldId == 1540 || shieldId == 11283 || shieldId == 11284 || shieldId == 25558 || shieldId == 25559
				|| shieldId == 25561 || shieldId == 25562 || shieldId == 16933 || player.getPerkManager().hasPerkActive(DonationPerk.DRAGON_TRAINER)
				|| ItemDefinitions.getItemDefinitions(shieldId).getName().equalsIgnoreCase("dragonfire ward")
				|| ItemDefinitions.getItemDefinitions(shieldId).getName().equalsIgnoreCase("dragonfire deflector");
	}

    /**
     * Checks if the Player has an Avas device.
     *
     * @param player The player to check.
     * @return if has Ava's device.
     */
    public static boolean hasAvas(Player player) {
        int capeId = player.getEquipment().getCapeId();
        return capeId == 10498 || capeId == 10499 || capeId == 20068 || capeId == 20769 || capeId == 20771
                || capeId == 31610 || capeId == 32152 || capeId == 32153 || capeId == 31603 || capeId == 9756
                || capeId == 9757 || capeId == 10642 || capeId == 34252 || capeId == 34253 || capeId == 34555
                || capeId == 34556 || capeId == 34557 || capeId == 34558 || capeId == 31271;
    }

    public static boolean hasDragonFire(Entity target) {
        if (target == null || !(target instanceof Player))
            return false;
        final Player player = (Player) target;
        return hasAntiDragProtection(player);
    }

    public static int getDefenceEmote(Entity target, boolean secondary) {
        if (target instanceof NPC) {
            NPC n = (NPC) target;
            return n.getCombatDefinitions().getDefenceEmote();
        } else {
            Player p = (Player) target;
            final Item weapon = p.getEquipment().getItem(Equipment.SLOT_WEAPON), shield = p.getEquipment().getItem(Equipment.SLOT_SHIELD);
            if (shield == null) {
                if (weapon != null)
                    return weapon.getDefinitions().getCombatDefenceEmote(com.rs.game.player.combat.PlayerCombat.LEGACY);
                return 424;
            } else {
                if (!secondary) {
                    if (weapon == null)
                        return shield.getDefinitions().getCombatDefenceEmote(PlayerCombat.LEGACY);
                    else
                        return weapon.getDefinitions().getCombatDefenceEmote(PlayerCombat.LEGACY);
                }
                return shield.getDefinitions().getCombatDefenceEmote(PlayerCombat.LEGACY);
            }
        }
    }

    public static int getAffinity(Entity source, Entity target, int attackStyle) {
        if (target instanceof NPC) {
            NPC targetNPC = (NPC) target;
            int weaknessStyle = targetNPC.getDefinitions().getWeaknessStyle();
            int combatType = source instanceof NPC ? attackStyle : getStyleType(attackStyle);
            if (combatType != ALL_TYPE && targetNPC.getStats().getWeaknessAffinity() > 0 && attackStyle == weaknessStyle)
                return targetNPC.getStats().getWeaknessAffinity();
            if (combatType == MAGIC_TYPE)
                return targetNPC.getStats().getMagicAffinity();
            else if (combatType == RANGE_TYPE) {
                return targetNPC.getStats().getRangeAffinity();
            } else if (combatType == MELEE_TYPE || combatType == ALL_TYPE) {
                return targetNPC.getStats().getMeleeAffinity();
            } else {
                return 55;
            }
        } else if (target instanceof Player) {
            Player targetPlayer = (Player) target;
            int combatType = source instanceof NPC ? attackStyle : getStyleType(attackStyle);
            return targetPlayer.getCombatDefinitions().getAffinity(combatType);
        }
        return 0;
    }

    public static int getStyleType(int style) {
        if (style >= 1 && style <= 4 || style == NO_SPELL_SELECTED_STYLE)
            return MAGIC_TYPE;
        if (style >= 5 && style <= 7)
            return MELEE_TYPE;
        if (style >= 8 && style <= 10)
            return RANGE_TYPE;
        return ALL_TYPE;
    }

    public static boolean hasDarkbow(Player player) {
        int weaponId = player.getEquipment().getWeaponId();
        return (weaponId == 11235 || weaponId >= 15701 && weaponId <= 15704);
    }

    public static boolean hasAscensionCrossbow(Player player, boolean mainHand) {
        Item item = player.getEquipment().getItem(mainHand ? Equipment.SLOT_WEAPON : Equipment.SLOT_SHIELD);
        if (item != null)
            return item.getName().toLowerCase().contains(mainHand ? "ascension crossbow" : "off-hand ascension crossbow");
        return false;
    }

    public static int getArmourAffinity(double wornArmour, double weakArmour, double neutralArmor, double strongArmor) {
        return (int) (wornArmour == 0 ? 55 : ((weakArmour * 45 + neutralArmor * 55 + strongArmor * 65) / wornArmour));
    }

    public static double getStatBonus(double tierLevel) {
        return ((0.0008 * Math.pow(tierLevel, 3) + tierLevel * 4 + 40));
    }




    public static double getHitChance(Entity source, Entity target, int attackStyle, boolean mainHand) {
        if (Settings.RS2_COMBAT) {
            double base = Rs2AtaraxiaNumerics.getHitChancePercent(source, target, attackStyle, mainHand);
            if (source instanceof Player) {
                /*
                 * Apply the trimmed RS2 accuracy stack: magic void, salve
                 * amulet, slayer helmet/black mask on task. Modern post-2009
                 * accuracy multipliers (auras, invention/donor perks,
                 * contract Reaper Hood, familiar accuracy bonuses) are gated
                 * out via getAccuracyModifier's RS2_COMBAT early return.
                 */
                base *= PlayerCombat.getAccuracyModifier((Player) source, target, attackStyle, mainHand);
            }
            return base;
        }
        double aff = getAffinity(source, target, attackStyle) + getAffinityModifier(target);
        double d;
        if (target instanceof Player) {
            Player targetPlayer = (Player) target;
            int defenceLevel = Math.max(1, targetPlayer.getSkills().getLevel(Skills.DEFENCE) + targetPlayer.getPrayer().getStatBonuses(Skills.DEFENCE) - targetPlayer.getPrayer().getDebuffStats(Skills.DEFENCE) + targetPlayer.getAuraManager().getStatModifier(Skills.DEFENCE));
            d = targetPlayer.getCombatDefinitions().getBonuses()[CombatDefinitions.WORN_ARMOUR] + getStatBonus(defenceLevel);
            Perk lunging = targetPlayer.getInventionManager().hasPerk(Perks.LUNGING);
            if (lunging != null)
                d -= d * 0.15;
            Perk plantedFeet = targetPlayer.getInventionManager().hasPerk(Perks.PLANTED_FEET);
            if (plantedFeet != null)
                d += d * 0.10;
        } else {
            NPC targetNPC = (NPC) target;
            d = targetNPC.getBonus(6) + getStatBonus(Math.max(1, targetNPC.getStats().getDefenceLevel() - Math.min(15, targetNPC.getPrayerDebuff(Prayer.DEFENCE_LEVEL))));
        }
        if (source instanceof Player) {
            Player attacker = (Player) source;
            int combatType = getStyleType(attackStyle);
            int skillId = combatType == MAGIC_TYPE ? Skills.MAGIC : combatType == MELEE_TYPE || combatType == ALL_TYPE ? Skills.ATTACK : Skills.RANGE;
            int attackLevel = attacker.getSkills().getLevel(skillId) + attacker.getPrayer().getStatBonuses(skillId) - attacker.getPrayer().getDebuffStats(skillId) + attacker.getAuraManager().getStatModifier(skillId);
            double a = getStatBonus(Math.max(1, attackLevel)) + ((double) attacker.getCombatDefinitions().getBonuses()[mainHand ? CombatDefinitions.MAINHAND_ACCURACY : CombatDefinitions.OFFHAND_ACCURACY]);
            double accuracyModifier = PlayerCombat.getAccuracyModifier(attacker, target, attackStyle, mainHand);
            a -= attacker.getCombatDefinitions().getAccuracyPenalty(combatType);
            if (a < 0)
                a = 0;
            return (aff * accuracyModifier * a) / d;
        } else {
            NPC attacker = (NPC) source;
            int accuracy = attacker.getBonus(attackStyle == Combat.MELEE_TYPE ? 3 : attackStyle == Combat.RANGE_TYPE ? 4 : 5);
            int level = attackStyle == Combat.MELEE_TYPE ? attacker.getStats().getAttackLevel() : attackStyle == Combat.RANGE_TYPE ? attacker.getStats().getRangeLevel() : attacker.getStats().getMagicLevel();
            double a = accuracy + getStatBonus(Math.max(1, level - Math.min(15, attacker.getPrayerDebuff(attackStyle == Combat.MELEE_TYPE ? Prayer.MELEE_LEVEL : attackStyle == Combat.RANGE_TYPE ? Prayer.RANGE_LEVEL : Prayer.MAGIC_LEVEL))));
            if (a < 0)
                a = 0;
            return (aff * a) / d;
        }
    }

    private static int getAffinityModifier(Entity target) {
        return 0;
    }

    public static double getDamageDebuff(Entity e, int combatStyle) {
        int debuffIndex = combatStyle == Combat.ALL_TYPE ? -1 : combatStyle == Combat.MELEE_TYPE ? Prayer.MELEE_DAMAGE : combatStyle == Combat.RANGE_TYPE ? Prayer.RANGE_DAMAGE : Prayer.MAGIC_DAMAGE;
        if (debuffIndex == -1)
            return 0;
        double debuffAmount = e.getPrayerDebuff(debuffIndex);
        if (debuffAmount > 15)
            debuffAmount = 15;
        return (debuffAmount) / 100.00;
    }

    public static double getVanguardDamageModifier(Player player) {
        double modifier = 0.00;
        for (int id : player.getEquipment().getEquipmentIds()) {
            switch (id) {
                case 21558:
                    modifier += 0.02;
                    break;
                case 21560:
                case 21562:
                    modifier += 0.03;
                    break;
                case 21564:
                case 21566:
                    modifier += 0.01;
                    break;
            }
        }
        return modifier;
    }

    public static double getTricksterDamageModifier(Player player) {
        double modifier = 0.00;
        for (int id : player.getEquipment().getEquipmentIds()) {
            switch (id) {
                case 21548:
                    modifier += 0.02;
                    break;
                case 21550:
                case 21552:
                    modifier += 0.03;
                    break;
                case 21554:
                case 21556:
                    modifier += 0.01;
                    break;
            }
        }
        return modifier;
    }

    public static double getBattleMageDamageModifier(Player player) {
        double modifier = 0.00;
        for (int id : player.getEquipment().getEquipmentIds()) {
            switch (id) {
                case 21538:
                    modifier += 0.02;
                    break;
                case 21540:
                case 21542:
                    modifier += 0.03;
                    break;
                case 21544:
                case 21546:
                    modifier += 0.01;
                    break;
            }
        }
        return modifier;
    }

    public static boolean fullGuthanEquipped(Player player) {
        int helmId = player.getEquipment().getHatId();
        int chestId = player.getEquipment().getChestId();
        int legsId = player.getEquipment().getLegsId();
        int weaponId = player.getEquipment().getWeaponId();
        if (helmId == -1 || chestId == -1 || legsId == -1 || weaponId == -1)
            return false;
        return ItemDefinitions.getItemDefinitions(helmId).getName().toLowerCase().contains("guthan's")
                && ItemDefinitions.getItemDefinitions(chestId).getName().toLowerCase().contains("guthan's")
                && ItemDefinitions.getItemDefinitions(legsId).getName().toLowerCase().contains("guthan's")
                && ItemDefinitions.getItemDefinitions(weaponId).getName().toLowerCase().contains("guthan's");
    }
    
    public static final boolean fullDharokEquipped(Player player) {
        int helmId = player.getEquipment().getHatId();
        int chestId = player.getEquipment().getChestId();
        int legsId = player.getEquipment().getLegsId();
        int weaponId = player.getEquipment().getWeaponId();
        if (helmId == -1 || chestId == -1 || legsId == -1 || weaponId == -1)
            return false;
        return ItemDefinitions.getItemDefinitions(helmId).getName().toLowerCase().contains("dharok's")
                && ItemDefinitions.getItemDefinitions(chestId).getName().toLowerCase().contains("dharok's")
                && ItemDefinitions.getItemDefinitions(legsId).getName().toLowerCase().contains("dharok's")
                && ItemDefinitions.getItemDefinitions(weaponId).getName().toLowerCase().contains("dharok's");
    }

    public static boolean fullVeracsEquipped(Player player) {
        int helmId = player.getEquipment().getHatId();
        int chestId = player.getEquipment().getChestId();
        int legsId = player.getEquipment().getLegsId();
        int weaponId = player.getEquipment().getWeaponId();
        if (helmId == -1 || chestId == -1 || legsId == -1 || weaponId == -1)
            return false;
        return ItemDefinitions.getItemDefinitions(helmId).getName().toLowerCase().contains("verac's")
                && ItemDefinitions.getItemDefinitions(chestId).getName().toLowerCase().contains("verac's")
                && ItemDefinitions.getItemDefinitions(legsId).getName().toLowerCase().contains("verac's")
                && ItemDefinitions.getItemDefinitions(weaponId).getName().toLowerCase().contains("verac's");
    }
    
    public static boolean fullVanguardEquipped(Player player) {
        int helmId = player.getEquipment().getHatId();
        int chestId = player.getEquipment().getChestId();
        int legsId = player.getEquipment().getLegsId();
        int weaponId = player.getEquipment().getWeaponId();
        int bootsId = player.getEquipment().getBootsId();
        if (helmId == -1 || chestId == -1 || legsId == -1 || weaponId == -1 || bootsId == -1)
            return false;
        return ItemDefinitions.getItemDefinitions(helmId).getName().toLowerCase().contains("vanguard")
                && ItemDefinitions.getItemDefinitions(chestId).getName().toLowerCase().contains("vanguard")
                && ItemDefinitions.getItemDefinitions(legsId).getName().toLowerCase().contains("vanguard")
                && ItemDefinitions.getItemDefinitions(weaponId).getName().toLowerCase().contains("vanguard")
                && ItemDefinitions.getItemDefinitions(bootsId).getName().toLowerCase().contains("vanguard");
    }
    
    public static boolean fullTricksterEquipped(Player player) {
        int helmId = player.getEquipment().getHatId();
        int chestId = player.getEquipment().getChestId();
        int legsId = player.getEquipment().getLegsId();
        int weaponId = player.getEquipment().getWeaponId();
        int bootsId = player.getEquipment().getBootsId();
        if (helmId == -1 || chestId == -1 || legsId == -1 || weaponId == -1 || bootsId == -1)
            return false;
        return ItemDefinitions.getItemDefinitions(helmId).getName().toLowerCase().contains("trickster")
                && ItemDefinitions.getItemDefinitions(chestId).getName().toLowerCase().contains("trickster")
                && ItemDefinitions.getItemDefinitions(legsId).getName().toLowerCase().contains("trickster")
                && ItemDefinitions.getItemDefinitions(weaponId).getName().toLowerCase().contains("trickster")
                && ItemDefinitions.getItemDefinitions(bootsId).getName().toLowerCase().contains("trickster");
    }

    public static boolean fullBattlemageEquipped(Player player) {
        int helmId = player.getEquipment().getHatId();
        int chestId = player.getEquipment().getChestId();
        int legsId = player.getEquipment().getLegsId();
        int weaponId = player.getEquipment().getWeaponId();
        int bootsId = player.getEquipment().getBootsId();
        if (helmId == -1 || chestId == -1 || legsId == -1 || weaponId == -1 || bootsId == -1)
            return false;
        return ItemDefinitions.getItemDefinitions(helmId).getName().toLowerCase().contains("battle-mage")
                && ItemDefinitions.getItemDefinitions(chestId).getName().toLowerCase().contains("battle-mage")
                && ItemDefinitions.getItemDefinitions(legsId).getName().toLowerCase().contains("battle-mage")
                && ItemDefinitions.getItemDefinitions(weaponId).getName().toLowerCase().contains("battle-mage")
                && ItemDefinitions.getItemDefinitions(bootsId).getName().toLowerCase().contains("battle-mage");
    }
}