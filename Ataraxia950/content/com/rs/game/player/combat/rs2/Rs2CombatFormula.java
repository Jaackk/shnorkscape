package com.rs.game.player.combat.rs2;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Pure pre-Evolution-of-Combat combat math.
 *
 * This class intentionally has no dependency on Ataraxia classes. The adapter
 * class decides which cache values become "classic" attack, defence, and
 * strength bonuses; this class only performs the old-roll arithmetic.
 */
public final class Rs2CombatFormula {

    public static final int EFFECTIVE_LEVEL_BASE = 8;
    public static final int NPC_LEVEL_BASE = 9;
    public static final int ROLL_BONUS_BASE = 64;
    public static final double MAX_HIT_DIVISOR = 640.0D;

    /** Legacy post-Constitution conversion used by non-native combat callers. */
    public static final int ATARAXIA_DAMAGE_SCALE = 10;

    private Rs2CombatFormula() {
    }

    /**
     * Late RS2/pre-EOC accuracy curve:
     * - if attack > defence: 1 - ((defence + 2) / (2 * (attack + 1)))
     * - otherwise: attack / (2 * (defence + 1))
     */
    public static double hitChance(long attackRoll, long defenceRoll) {
        if (attackRoll <= 0L) {
            return 0.0D;
        }
        if (defenceRoll < 0L) {
            defenceRoll = 0L;
        }

        final double chance;
        if (attackRoll > defenceRoll) {
            chance = 1.0D - ((defenceRoll + 2.0D) / (2.0D * (attackRoll + 1.0D)));
        } else {
            chance = attackRoll / (2.0D * (defenceRoll + 1.0D));
        }
        return clamp(chance, 0.0D, 1.0D);
    }

    public static double hitChancePercent(long attackRoll, long defenceRoll) {
        return hitChance(attackRoll, defenceRoll) * 100.0D;
    }

    public static boolean rollsAccurate(long attackRoll, long defenceRoll) {
        return ThreadLocalRandom.current().nextDouble() < hitChance(attackRoll, defenceRoll);
    }

    /**
     * Late RS2/pre-EOC effective level shape:
     * floor(level + level * prayerBonus) + stanceBonus + 8, then set multiplier.
     *
     * prayerBonus is a fraction, for example 0.15 for 15 percent.
     */
    public static int effectiveLevel(int visibleLevel, double prayerBonus, int stanceBonus, double multiplier) {
        int effective = (int) Math.floor(visibleLevel + (visibleLevel * prayerBonus));
        effective += stanceBonus;
        effective += EFFECTIVE_LEVEL_BASE;
        effective = (int) Math.floor(effective * positiveMultiplier(multiplier));
        return Math.max(0, effective);
    }

    /**
     * Late RS2/pre-EOC attack/defence roll: effectiveLevel * (bonus + 64).
     */
    public static long roll(int effectiveLevel, int bonus) {
        return (long) Math.max(0, effectiveLevel) * Math.max(0, bonus + ROLL_BONUS_BASE);
    }

    /**
     * Late RS2/pre-EOC max-hit formula used for melee/ranged-style maxes:
     * floor(0.5 + effectiveStrength * (strengthBonus + 64) / 640).
     *
     * Special attacks and gear/content multipliers apply after the base max hit
     * has been floored.
     */
    public static int meleeOrRangedMaxHit(int effectiveStrengthLevel, int strengthBonus, double modifier) {
        int base = (int) Math.floor(0.5D
                + ((double) Math.max(0, effectiveStrengthLevel)
                * (strengthBonus + (double) ROLL_BONUS_BASE) / MAX_HIT_DIVISOR));
        return applyMaxHitMultiplier(base, modifier);
    }

    /**
     * Magic defence in this era is weighted defence/magic, then rolled against
     * the magic defence bonus.
     */
    public static int magicDefenceEffectiveLevel(
            int visibleDefenceLevel,
            double defencePrayerBonus,
            int visibleMagicLevel,
            double magicPrayerBonus,
            int stanceBonus,
            double multiplier) {

        int effectiveDefence = (int) Math.floor(visibleDefenceLevel + (visibleDefenceLevel * defencePrayerBonus));
        int effectiveMagic = (int) Math.floor(visibleMagicLevel + (visibleMagicLevel * magicPrayerBonus));
        int weighted = (int) Math.floor((effectiveDefence * 0.30D) + (effectiveMagic * 0.70D));
        weighted += stanceBonus;
        weighted += EFFECTIVE_LEVEL_BASE;
        weighted = (int) Math.floor(weighted * positiveMultiplier(multiplier));
        return Math.max(0, weighted);
    }

    public static int npcEffectiveLevel(int visibleLevel) {
        return Math.max(1, visibleLevel) + NPC_LEVEL_BASE;
    }

    public static long applyRollMultiplier(long roll, double multiplier) {
        return (long) Math.floor(Math.max(0L, roll) * positiveMultiplier(multiplier));
    }

    public static int applyMaxHitMultiplier(int maxHit, double multiplier) {
        return (int) Math.floor(Math.max(0, maxHit) * positiveMultiplier(multiplier));
    }

    public static double multiplierFromPercent(double percent) {
        return positiveMultiplier(1.0D + (percent / 100.0D));
    }

    public static int rollDamageInclusive(int maxHit) {
        if (maxHit <= 0) {
            return 0;
        }
        return ThreadLocalRandom.current().nextInt(maxHit + 1);
    }

    /**
     * Rolls in old HP units first, then converts to Ataraxia/Constitution
     * damage units. This preserves the RS2 "0..max old HP" distribution.
     */
    public static int rollScaledDamageInclusive(int rs2MaxHit) {
        return scaleDamageForAtaraxia(rollDamageInclusive(rs2MaxHit));
    }

    public static int scaleDamageForAtaraxia(int rs2Damage) {
        return Math.max(0, rs2Damage) * ATARAXIA_DAMAGE_SCALE;
    }

    /**
     * Native 950 hit packets use single life-point units. Preserve the legacy
     * magnitude while retaining a supplied one-unit remainder so native combat
     * is not artificially quantised to a trailing zero.
     */
    public static int scaleNative950Damage(int rs2Damage, int remainder) {
        if (rs2Damage <= 0) {
            return 0;
        }
        if (remainder < 0 || remainder >= ATARAXIA_DAMAGE_SCALE) {
            throw new IllegalArgumentException("Invalid native damage remainder");
        }
        return rs2Damage * ATARAXIA_DAMAGE_SCALE + remainder;
    }

    public static double positiveMultiplier(double multiplier) {
        if (Double.isNaN(multiplier) || Double.isInfinite(multiplier)) {
            return 0.0D;
        }
        return Math.max(0.0D, multiplier);
    }

    private static double clamp(double value, double min, double max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }
}
