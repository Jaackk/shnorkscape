package com.rs.game.player.combat.rs2;

import com.rs.Settings;
import com.rs.game.npc.NPC;
import com.rs.game.player.CombatDefinitions;
import com.rs.game.player.Player;
import com.rs.game.player.content.Combat;

/**
 * Converts Ataraxia's currently available cache-derived combat numbers into
 * the smaller "classic bonus" values expected by pre-EOC combat formulae.
 *
 * This is intentionally only an adapter. For true authenticity, these values
 * should eventually come from classic-style item and NPC bonuses: stab/slash/
 * crush/ranged/magic attack, strength/ranged strength, and style defence.
 * Until that data exists, this class compresses Ataraxia's larger EOC-era
 * ratings into numbers that behave reasonably in old-roll arithmetic.
 */
public final class Rs2AtaraxiaCacheBonuses {

    /*
     * These divisors are temporary tuning knobs, not the combat formula.
     *
     * Ataraxia equipment accuracy/damage ratings are much larger than classic
     * RuneScape bonuses, so they need to be compressed before the pre-EOC
     * formula adds its +64 base. Armour is compressed too, because full
     * Ataraxia armour totals are far above classic style-defence bonuses.
     */
    public static final double PLAYER_ACCURACY_RATING_DIVISOR = 10.0D;
    public static final double PLAYER_DAMAGE_RATING_DIVISOR = 10.0D;
    public static final double PLAYER_ARMOUR_RATING_DIVISOR = 10.0D;

    public static final double NPC_ACCURACY_RATING_DIVISOR = 10.0D;
    public static final double NPC_ARMOUR_RATING_DIVISOR = 10.0D;

    /*
     * NPC bonus slot semantics, per NPCDefinitions#getCacheBonuses():
     *  [0/1/2] melee/range/mage damage rating (cache value /10) -- an RS3
     *      damage-shaped value, NOT a classic RS2 strength bonus. Not exposed
     *      here: NPC damage is always sourced from scripted maxHit values via
     *      Rs2AtaraxiaNumerics.rollNpcScriptedHit, which leaves boss tuning
     *      intact. Restoring a formula-based NPC max would require real
     *      RS2-style strength bonuses on NPCStats first.
     *  [3/4/5] melee/range/magic accuracy
     *  [6]     armour
     *  [7]     crit bonus (unused here)
     */
    private static final int NPC_MELEE_ACCURACY = 3;
    private static final int NPC_RANGE_ACCURACY = 4;
    private static final int NPC_MAGIC_ACCURACY = 5;
    private static final int NPC_ARMOUR = 6;

    private Rs2AtaraxiaCacheBonuses() {
    }

    public static int playerAttackBonus(Player player, boolean mainHand, int attackStyle) {
        if (Settings.RS2_ITEM_BONUSES) {
            return ClassicItemBonusResolver.sumEquipped(player, mainHand)
                    .attackBonusForStyle(attackStyle);
        }
        int slot = mainHand ? CombatDefinitions.MAINHAND_ACCURACY : CombatDefinitions.OFFHAND_ACCURACY;
        int attackType = normaliseAttackType(attackStyle);
        int rating = player.getCombatDefinitions().getBonuses()[slot]
                - player.getCombatDefinitions().getAccuracyPenalty(attackType);
        return fromRating(rating, PLAYER_ACCURACY_RATING_DIVISOR);
    }

    public static int playerStrengthBonus(Player player, boolean mainHand, int attackType) {
        if (Settings.RS2_ITEM_BONUSES) {
            ClassicBonuses summed = ClassicItemBonusResolver.sumEquipped(player, mainHand);
            int normalised = normaliseAttackType(attackType);
            if (normalised == Combat.RANGE_TYPE) return summed.rangeStrBonus;
            if (normalised == Combat.MAGIC_TYPE) return 0; // magic dmg comes from spell + prayer
            return summed.strBonus;
        }
        int slot = mainHand ? CombatDefinitions.MAINHAND_DAMAGE : CombatDefinitions.OFFHAND_DAMAGE;
        return fromRating(player.getCombatDefinitions().getBonuses()[slot], PLAYER_DAMAGE_RATING_DIVISOR);
    }

    public static int playerDefenceBonus(Player player, int incomingAttackStyleOrType) {
        if (Settings.RS2_ITEM_BONUSES) {
            return ClassicItemBonusResolver.sumEquipped(player, true)
                    .defenceBonusForStyle(incomingAttackStyleOrType);
        }
        int incomingAttackType = normaliseAttackType(incomingAttackStyleOrType);
        int rating = player.getCombatDefinitions().getBonuses()[CombatDefinitions.WORN_ARMOUR]
                + player.getCombatDefinitions().getStyleBonus(incomingAttackType);
        return fromRating(rating, PLAYER_ARMOUR_RATING_DIVISOR);
    }

    public static int npcAttackBonus(NPC npc, int attackType) {
        switch (normaliseAttackType(attackType)) {
            case Combat.RANGE_TYPE:
                return fromRating(npc.getBonus(NPC_RANGE_ACCURACY), NPC_ACCURACY_RATING_DIVISOR);
            case Combat.MAGIC_TYPE:
                return fromRating(npc.getBonus(NPC_MAGIC_ACCURACY), NPC_ACCURACY_RATING_DIVISOR);
            case Combat.MELEE_TYPE:
            default:
                return fromRating(npc.getBonus(NPC_MELEE_ACCURACY), NPC_ACCURACY_RATING_DIVISOR);
        }
    }

    public static int npcDefenceBonus(NPC npc, int incomingAttackStyleOrType) {
        return fromRating(npc.getBonus(NPC_ARMOUR), NPC_ARMOUR_RATING_DIVISOR);
    }

    public static int normaliseAttackType(int attackStyleOrType) {
        if (attackStyleOrType == Combat.RANGE_TYPE || attackStyleOrType == Combat.MAGIC_TYPE) {
            return attackStyleOrType;
        }

        int styleType = Combat.getStyleType(attackStyleOrType);
        if (styleType == Combat.RANGE_TYPE || styleType == Combat.MAGIC_TYPE) {
            return styleType;
        }
        return Combat.MELEE_TYPE;
    }

    private static int fromRating(int rating, double divisor) {
        if (divisor <= 0.0D) {
            return Math.max(0, rating);
        }
        return Math.max(0, (int) Math.round(rating / divisor));
    }
}
