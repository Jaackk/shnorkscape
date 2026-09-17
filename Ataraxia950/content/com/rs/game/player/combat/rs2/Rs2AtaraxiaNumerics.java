package com.rs.game.player.combat.rs2;

import com.rs.cache.loaders.GeneralRequirementMap;
import com.rs.game.Entity;
import com.rs.game.npc.NPC;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.Prayer;
import com.rs.game.player.Skills;
import com.rs.game.player.content.Combat;
import com.rs.game.player.content.Magic;

/**
 * Draft integration layer for pre-Evolution-of-Combat numerical combat.
 *
 * This file is deliberately not wired into Ataraxia yet. The important rule for
 * later integration is that PlayerCombat#getDamageModifier(...) should not be
 * passed wholesale into this class. Modern prayers, specials, bosses, slayer
 * effects, void, salve, and custom content should be translated into explicit
 * pre-EOC-style multipliers through PlayerDamageContext or equivalent call-site
 * logic.
 */
public final class Rs2AtaraxiaNumerics {

    private Rs2AtaraxiaNumerics() {
    }

    /**
     * Explicit player-side modifiers that preserve pre-EOC formula semantics.
     *
     * Examples for later integration:
     * - special attacks usually multiply maxHit, after base max hit is floored
     * - void-style effects usually multiply effective levels or rolls
     * - salve/slayer-style effects should be modelled as targeted roll/max-hit
     *   multipliers, not as Ataraxia's current global additive damage stack
     */
    public static final class PlayerDamageContext {
        private static final PlayerDamageContext NONE = new PlayerDamageContext(
                1.0D, 1.0D, 1.0D, 1.0D, 0, false);

        private final double accuracyRollMultiplier;
        private final double accuracyEffectiveLevelMultiplier;
        private final double maxHitEffectiveLevelMultiplier;
        private final double maxHitMultiplier;
        private final int flatMaxHitBonusRs2;
        private final boolean guaranteedHit;

        private PlayerDamageContext(
                double accuracyRollMultiplier,
                double accuracyEffectiveLevelMultiplier,
                double maxHitEffectiveLevelMultiplier,
                double maxHitMultiplier,
                int flatMaxHitBonusRs2,
                boolean guaranteedHit) {

            this.accuracyRollMultiplier = Rs2CombatFormula.positiveMultiplier(accuracyRollMultiplier);
            this.accuracyEffectiveLevelMultiplier = Rs2CombatFormula.positiveMultiplier(accuracyEffectiveLevelMultiplier);
            this.maxHitEffectiveLevelMultiplier = Rs2CombatFormula.positiveMultiplier(maxHitEffectiveLevelMultiplier);
            this.maxHitMultiplier = Rs2CombatFormula.positiveMultiplier(maxHitMultiplier);
            this.flatMaxHitBonusRs2 = flatMaxHitBonusRs2;
            this.guaranteedHit = guaranteedHit;
        }

        public static PlayerDamageContext none() {
            return NONE;
        }

        public static PlayerDamageContext specialAttack(double maxHitMultiplier) {
            return NONE.multiplyMaxHit(maxHitMultiplier);
        }

        public PlayerDamageContext withGuaranteedHit() {
            return new PlayerDamageContext(
                    accuracyRollMultiplier,
                    accuracyEffectiveLevelMultiplier,
                    maxHitEffectiveLevelMultiplier,
                    maxHitMultiplier,
                    flatMaxHitBonusRs2,
                    true);
        }

        public PlayerDamageContext multiplyAccuracyRoll(double multiplier) {
            return new PlayerDamageContext(
                    accuracyRollMultiplier * multiplier,
                    accuracyEffectiveLevelMultiplier,
                    maxHitEffectiveLevelMultiplier,
                    maxHitMultiplier,
                    flatMaxHitBonusRs2,
                    guaranteedHit);
        }

        public PlayerDamageContext multiplyAccuracyEffectiveLevel(double multiplier) {
            return new PlayerDamageContext(
                    accuracyRollMultiplier,
                    accuracyEffectiveLevelMultiplier * multiplier,
                    maxHitEffectiveLevelMultiplier,
                    maxHitMultiplier,
                    flatMaxHitBonusRs2,
                    guaranteedHit);
        }

        public PlayerDamageContext multiplyMaxHitEffectiveLevel(double multiplier) {
            return new PlayerDamageContext(
                    accuracyRollMultiplier,
                    accuracyEffectiveLevelMultiplier,
                    maxHitEffectiveLevelMultiplier * multiplier,
                    maxHitMultiplier,
                    flatMaxHitBonusRs2,
                    guaranteedHit);
        }

        public PlayerDamageContext multiplyMaxHit(double multiplier) {
            return new PlayerDamageContext(
                    accuracyRollMultiplier,
                    accuracyEffectiveLevelMultiplier,
                    maxHitEffectiveLevelMultiplier,
                    maxHitMultiplier * multiplier,
                    flatMaxHitBonusRs2,
                    guaranteedHit);
        }

        public PlayerDamageContext addFlatMaxHitRs2(int bonus) {
            return new PlayerDamageContext(
                    accuracyRollMultiplier,
                    accuracyEffectiveLevelMultiplier,
                    maxHitEffectiveLevelMultiplier,
                    maxHitMultiplier,
                    flatMaxHitBonusRs2 + bonus,
                    guaranteedHit);
        }
    }

    public static double getHitChancePercent(Entity source, Entity target, int attackStyleOrType, boolean mainHand) {
        int attackType = attackTypeFor(source, attackStyleOrType);
        PlayerDamageContext context = source instanceof Player ? PlayerDamageContext.none() : null;
        long attackRoll = calculateAttackRoll(source, attackStyleOrType, attackType, mainHand, context);
        long defenceRoll = calculateDefenceRoll(target, source, attackStyleOrType, attackType);
        return Rs2CombatFormula.hitChancePercent(attackRoll, defenceRoll);
    }

    public static double getPlayerHitChancePercent(
            Player player,
            Entity target,
            int attackStyle,
            boolean mainHand,
            PlayerDamageContext context) {

        context = contextOrNone(context);
        int attackType = attackTypeFor(player, attackStyle);
        long attackRoll = calculateAttackRoll(player, attackStyle, attackType, mainHand, context);
        long defenceRoll = calculateDefenceRoll(target, player, attackStyle, attackType);
        return Rs2CombatFormula.hitChancePercent(attackRoll, defenceRoll);
    }

    public static int rollPlayerHit(
            Player player,
            Entity target,
            double specialAttackMultiplier,
            boolean mainHand,
            boolean ignoreDefence,
            boolean maxHitOnly) {

        return rollPlayerHit(
                player,
                target,
                PlayerDamageContext.specialAttack(specialAttackMultiplier),
                mainHand,
                ignoreDefence,
                maxHitOnly);
    }

    public static int rollPlayerHit(
            Player player,
            Entity target,
            PlayerDamageContext context,
            boolean mainHand,
            boolean ignoreDefence,
            boolean maxHitOnly) {

        context = contextOrNone(context);

        /*
         * Ataraxia's existing PlayerCombat#getHit passes !mainHand into
         * getStyle/getHandDamage. This draft keeps the public flag sane:
         * true means main hand, false means off-hand.
         */
        int attackStyle = player.getCombatDefinitions().getStyle(!mainHand);
        int attackType = attackTypeFor(player, attackStyle);

        if (!ignoreDefence && !context.guaranteedHit) {
            long attackRoll = calculateAttackRoll(player, attackStyle, attackType, mainHand, context);
            long defenceRoll = calculateDefenceRoll(target, player, attackStyle, attackType);
            if (!Rs2CombatFormula.rollsAccurate(attackRoll, defenceRoll)) {
                return 0;
            }
        }

        int oldHpMaxHit = calculatePlayerMaxHitRs2(player, attackStyle, attackType, mainHand, context);
        int maxHit = Rs2CombatFormula.scaleDamageForAtaraxia(oldHpMaxHit);
        return maxHitOnly ? maxHit : Rs2CombatFormula.rollScaledDamageInclusive(oldHpMaxHit);
    }

    /**
     * NPC/boss-safe option: keep scripts' existing max-hit values and only swap
     * the accuracy curve and damage roll distribution. Boss max-hit tuning is
     * intentionally left to script data.
     */
    public static int rollNpcScriptedHit(
            NPC npc,
            Entity target,
            int scriptedMaxHitAtaraxiaUnits,
            int npcAttackType,
            double scriptedMaxHitMultiplier,
            boolean cannotMiss) {

        int attackType = attackTypeFor(npc, npcAttackType);

        if (!cannotMiss) {
            long attackRoll = calculateAttackRoll(npc, npcAttackType, attackType, true, null);
            long defenceRoll = calculateDefenceRoll(target, npc, npcAttackType, attackType);
            if (!Rs2CombatFormula.rollsAccurate(attackRoll, defenceRoll)) {
                return 0;
            }
        }

        int maxHit = Rs2CombatFormula.applyMaxHitMultiplier(
                scriptedMaxHitAtaraxiaUnits,
                scriptedMaxHitMultiplier);
        return Rs2CombatFormula.rollDamageInclusive(maxHit);
    }

    public static int calculatePlayerMaxHitAtaraxia(
            Player player,
            int attackStyle,
            int attackType,
            boolean mainHand,
            double specialAttackMultiplier) {

        return calculatePlayerMaxHitAtaraxia(
                player,
                attackStyle,
                attackType,
                mainHand,
                PlayerDamageContext.specialAttack(specialAttackMultiplier));
    }

    public static int calculatePlayerMaxHitAtaraxia(
            Player player,
            int attackStyle,
            int attackType,
            boolean mainHand,
            PlayerDamageContext context) {

        return Rs2CombatFormula.scaleDamageForAtaraxia(
                calculatePlayerMaxHitRs2(player, attackStyle, attackType, mainHand, context));
    }

    public static int calculatePlayerMaxHitRs2(
            Player player,
            int attackStyle,
            int attackType,
            boolean mainHand,
            double specialAttackMultiplier) {

        return calculatePlayerMaxHitRs2(
                player,
                attackStyle,
                attackType,
                mainHand,
                PlayerDamageContext.specialAttack(specialAttackMultiplier));
    }

    public static int calculatePlayerMaxHitRs2(
            Player player,
            int attackStyle,
            int attackType,
            boolean mainHand,
            PlayerDamageContext context) {

        context = contextOrNone(context);

        if (attackType == Combat.MAGIC_TYPE) {
            return calculatePlayerMagicMaxHitRs2(player, mainHand, context);
        }

        int skillId = attackType == Combat.RANGE_TYPE ? Skills.RANGE : Skills.STRENGTH;
        int visibleLevel = visibleLevel(player, skillId);
        double prayerBonus = damagePrayerBonus(player, attackType);
        int stanceBonus = offensiveStanceBonus(player, attackType, attackStyle);
        double effectiveLevelMultiplier = intrinsicStrengthEffectiveLevelMultiplier(player, attackType, skillId)
                * context.maxHitEffectiveLevelMultiplier;

        int effective = Rs2CombatFormula.effectiveLevel(
                visibleLevel,
                prayerBonus,
                stanceBonus,
                effectiveLevelMultiplier);
        int strengthBonus = Rs2AtaraxiaCacheBonuses.playerStrengthBonus(player, mainHand, attackType);
        double maxHitMultiplier = intrinsicMaxHitMultiplier(player, attackType, skillId)
                * context.maxHitMultiplier;
        int maxHit = Rs2CombatFormula.meleeOrRangedMaxHit(effective, strengthBonus, maxHitMultiplier);
        return Math.max(0, maxHit + context.flatMaxHitBonusRs2);
    }

    private static int calculatePlayerMagicMaxHitRs2(Player player, boolean mainHand, PlayerDamageContext context) {
        int oldHpMax = spellMaxHitRs2(player, !mainHand);

        /*
         * Classic magic max hits primarily come from the spell table. Ataraxia's
         * newer prayers can still be preserved here by translating their magic
         * damage percent into an explicit spell max-hit multiplier.
         */
        double magicPrayerMultiplier = 1.0D + damagePrayerBonus(player, Combat.MAGIC_TYPE);
        double multiplier = context.maxHitMultiplier * magicPrayerMultiplier;
        int maxHit = Rs2CombatFormula.applyMaxHitMultiplier(oldHpMax, multiplier);
        return Math.max(0, maxHit + context.flatMaxHitBonusRs2);
    }

    private static long calculateAttackRoll(
            Entity source,
            int attackStyleOrType,
            int attackType,
            boolean mainHand,
            PlayerDamageContext context) {

        if (source instanceof Player) {
            Player player = (Player) source;
            context = contextOrNone(context);
            int skillId = offensiveAccuracySkill(attackType);
            int visibleLevel = visibleLevel(player, skillId);
            double prayerBonus = levelPrayerBonus(player, skillId);
            int stanceBonus = accuracyStanceBonus(player, attackType, attackStyleOrType);
            double effectiveLevelMultiplier = intrinsicAccuracyEffectiveLevelMultiplier(player, attackType, skillId)
                    * context.accuracyEffectiveLevelMultiplier;
            int effective = Rs2CombatFormula.effectiveLevel(
                    visibleLevel,
                    prayerBonus,
                    stanceBonus,
                    effectiveLevelMultiplier);
            int bonus = Rs2AtaraxiaCacheBonuses.playerAttackBonus(player, mainHand, attackStyleOrType);
            return Rs2CombatFormula.applyRollMultiplier(
                    Rs2CombatFormula.roll(effective, bonus),
                    context.accuracyRollMultiplier);
        }

        NPC npc = (NPC) source;
        int level = npcOffensiveAccuracyLevel(npc, attackType);
        int bonus = Rs2AtaraxiaCacheBonuses.npcAttackBonus(npc, attackType);
        return Rs2CombatFormula.roll(Rs2CombatFormula.npcEffectiveLevel(level), bonus);
    }

    private static long calculateDefenceRoll(Entity target, Entity attacker, int attackStyleOrType, int attackType) {
        if (target instanceof Player) {
            Player player = (Player) target;
            int defenceBonus = Rs2AtaraxiaCacheBonuses.playerDefenceBonus(player, attackStyleOrType);
            int effective;

            if (attackType == Combat.MAGIC_TYPE) {
                effective = Rs2CombatFormula.magicDefenceEffectiveLevel(
                        visibleLevel(player, Skills.DEFENCE),
                        levelPrayerBonus(player, Skills.DEFENCE),
                        visibleLevel(player, Skills.MAGIC),
                        levelPrayerBonus(player, Skills.MAGIC),
                        defensiveStanceBonus(player, attackType),
                        defensiveSetMultiplier(player, attacker, attackType));
            } else {
                effective = Rs2CombatFormula.effectiveLevel(
                        visibleLevel(player, Skills.DEFENCE),
                        levelPrayerBonus(player, Skills.DEFENCE),
                        defensiveStanceBonus(player, attackType),
                        defensiveSetMultiplier(player, attacker, attackType));
            }

            return Rs2CombatFormula.roll(effective, defenceBonus);
        }

        NPC npc = (NPC) target;
        int defenceBonus = Rs2AtaraxiaCacheBonuses.npcDefenceBonus(npc, attackStyleOrType);
        int level = attackType == Combat.MAGIC_TYPE
                ? npcLevelAfterDebuff(npc, npc.getStats().getMagicLevel(), Prayer.MAGIC_LEVEL)
                : npcLevelAfterDebuff(npc, npc.getStats().getDefenceLevel(), Prayer.DEFENCE_LEVEL);
        return Rs2CombatFormula.roll(Rs2CombatFormula.npcEffectiveLevel(level), defenceBonus);
    }

    private static int attackTypeFor(Entity source, int attackStyleOrType) {
        if (source instanceof NPC) {
            if (attackStyleOrType == Combat.RANGE_TYPE || attackStyleOrType == Combat.MAGIC_TYPE) {
                return attackStyleOrType;
            }
            return Combat.MELEE_TYPE;
        }
        return Rs2AtaraxiaCacheBonuses.normaliseAttackType(attackStyleOrType);
    }

    private static int offensiveAccuracySkill(int attackType) {
        if (attackType == Combat.RANGE_TYPE) {
            return Skills.RANGE;
        }
        if (attackType == Combat.MAGIC_TYPE) {
            return Skills.MAGIC;
        }
        return Skills.ATTACK;
    }

    private static int npcOffensiveAccuracyLevel(NPC npc, int attackType) {
        if (attackType == Combat.RANGE_TYPE) {
            return npcLevelAfterDebuff(npc, npc.getStats().getRangeLevel(), Prayer.RANGE_LEVEL);
        }
        if (attackType == Combat.MAGIC_TYPE) {
            return npcLevelAfterDebuff(npc, npc.getStats().getMagicLevel(), Prayer.MAGIC_LEVEL);
        }
        return npcLevelAfterDebuff(npc, npc.getStats().getAttackLevel(), Prayer.MELEE_LEVEL);
    }

    private static int npcLevelAfterDebuff(NPC npc, int baseLevel, int prayerDebuffIndex) {
        return Math.max(1, baseLevel - Math.min(15, npc.getPrayerDebuff(prayerDebuffIndex)));
    }

    private static int visibleLevel(Player player, int skillId) {
        /*
         * Pre-EOC formulae use current visible levels. Aura modifiers are kept
         * because Ataraxia has modern content, but they are still applied as a
         * visible-level adjustment rather than as a post-formula damage boost.
         */
        return Math.max(1, player.getSkills().getLevel(skillId) + player.getAuraManager().getStatModifier(skillId));
    }

    private static double levelPrayerBonus(Player player, int skillId) {
        /*
         * Ataraxia exposes level prayer buffs as percents. Keeping that source
         * lets newer prayers participate while preserving the old effective
         * level calculation.
         */
        int bonusPercent = player.getPrayer().getStatBonuses(skillId) - player.getPrayer().getDebuffStats(skillId);
        return bonusPercent / 100.0D;
    }

    private static double damagePrayerBonus(Player player, int attackType) {
        /*
         * For melee/ranged, this becomes the pre-EOC strength/ranged-strength
         * prayer part of effective level. For magic, it becomes an explicit
         * spell max-hit multiplier so newer Ataraxia prayers can still matter.
         */
        return player.getPrayer().getDamageMultiplier(attackType) - Combat.getDamageDebuff(player, attackType);
    }

    /*
     * Pre-EOC stances (accurate/aggressive/defensive/controlled for melee,
     * accurate/rapid/longrange for ranged, normal/defensive for magic).
     *
     * Ataraxia removed the 4-stance picker UI, so we derive the player's
     * stance from their selected XP gain checkboxes - the post-EOC equivalent
     * for picking what XP a player wants out of a fight. This preserves the
     * RS2 +3 single-stat boost (or +1+1+1 for "share") on the player's
     * effective levels without requiring a UI rebuild.
     *
     *   Melee XP boxes: [ATTACK, STRENGTH, DEFENCE]
     *     only ATTACK   -> ACCURATE     (+3 attack)
     *     only STRENGTH -> AGGRESSIVE   (+3 strength)
     *     only DEFENCE  -> DEFENSIVE    (+3 defence)
     *     all three     -> CONTROLLED   (+1 to all)
     *     two boxes     -> bias toward the more offensive of the two
     *
     *   Ranged XP boxes: [RANGE, DEFENCE]
     *     RANGE only    -> ACCURATE     (+3 range)
     *     DEFENCE on    -> LONG_RANGE   (+3 defence)
     *
     *   Magic XP boxes: [MAGIC, DEFENCE]
     *     DEFENCE on    -> DEFENSIVE_CAST (+3 defence, no offence)
     *     otherwise     -> standard cast (no offence/defence stance bonus)
     */
    private enum Stance {
        ACCURATE, AGGRESSIVE, DEFENSIVE, CONTROLLED,
        RAPID, LONG_RANGE,
        MAGIC_NORMAL, MAGIC_DEFENSIVE
    }

    private static Stance derivePlayerStance(Player player, int attackType) {
        switch (attackType) {
            case Combat.MELEE_TYPE:
            case Combat.ALL_TYPE: {
                boolean[] xp = player.getCombatDefinitions().getMeleeCombatExperienceGain();
                boolean att = xp[0], str = xp[1], def = xp[2];
                int count = (att ? 1 : 0) + (str ? 1 : 0) + (def ? 1 : 0);
                if (count == 3) return Stance.CONTROLLED;
                if (count == 1) {
                    if (att) return Stance.ACCURATE;
                    if (str) return Stance.AGGRESSIVE;
                    return Stance.DEFENSIVE;
                }
                if (att && str) return Stance.AGGRESSIVE;
                if (att && def) return Stance.ACCURATE;
                return Stance.DEFENSIVE; // str + def
            }
            case Combat.RANGE_TYPE: {
                boolean[] xp = player.getCombatDefinitions().getRangedCombatExperienceGain();
                boolean def = xp[1];
                return def ? Stance.LONG_RANGE : Stance.ACCURATE;
            }
            case Combat.MAGIC_TYPE: {
                boolean[] xp = player.getCombatDefinitions().getMagicCombatExperienceGain();
                return xp[1] ? Stance.MAGIC_DEFENSIVE : Stance.MAGIC_NORMAL;
            }
            default:
                return Stance.CONTROLLED;
        }
    }

    private static int accuracyStanceBonus(Player player, int attackType, int attackStyle) {
        if (attackType == Combat.MAGIC_TYPE) {
            return 0;
        }
        Stance s = derivePlayerStance(player, attackType);
        switch (s) {
            case ACCURATE:    return 3;
            case CONTROLLED:  return 1;
            default:          return 0;
        }
    }

    private static int offensiveStanceBonus(Player player, int attackType, int attackStyle) {
        if (attackType == Combat.MAGIC_TYPE) {
            return 0;
        }
        Stance s = derivePlayerStance(player, attackType);
        switch (s) {
            case AGGRESSIVE:  return 3;
            case CONTROLLED:  return 1;
            default:          return 0;
        }
    }

    private static int defensiveStanceBonus(Player player, int incomingAttackType) {
        int weaponCombatType = playerWeaponCombatType(player);
        Stance s = derivePlayerStance(player, weaponCombatType);
        switch (s) {
            case DEFENSIVE:
            case LONG_RANGE:
            case MAGIC_DEFENSIVE:
                return 3;
            case CONTROLLED:
                return 1;
            default:
                return 0;
        }
    }

    private static int playerWeaponCombatType(Player player) {
        int t = player.getCombatDefinitions().getType(Equipment.SLOT_WEAPON);
        return t == Combat.ALL_TYPE ? Combat.MELEE_TYPE : t;
    }

    private static double intrinsicAccuracyEffectiveLevelMultiplier(Player player, int attackType, int skillId) {
        /*
         * Void, salve, black mask/slayer helmet, and custom accuracy boosts can
         * be represented through PlayerDamageContext during integration.
         */
        return 1.0D;
    }

    private static double intrinsicStrengthEffectiveLevelMultiplier(Player player, int attackType, int skillId) {
        /*
         * Effects that historically multiplied effective strength/ranged level
         * belong here or in PlayerDamageContext, not in a global damage stack.
         */
        return 1.0D;
    }

    private static double intrinsicMaxHitMultiplier(Player player, int attackType, int skillId) {
        double multiplier = 1.0D;

        if (attackType == Combat.MELEE_TYPE && skillId == Skills.STRENGTH && Combat.fullDharokEquipped(player)) {
            int missingOldHp = Math.max(0, player.getMaxHitpoints() - player.getHitpoints())
                    / Rs2CombatFormula.ATARAXIA_DAMAGE_SCALE;
            multiplier *= 1.0D + (missingOldHp * 0.01D);
        }

        return multiplier;
    }

    private static double defensiveSetMultiplier(Player player, Entity attacker, int incomingAttackType) {
        return 1.0D;
    }

    private static int spellMaxHitRs2(Player player, boolean offHand) {
        int spellId = player.getCombatDefinitions().getSpellId(offHand);
        if (spellId >= 1000) {
            spellId -= 1000;
        }

        GeneralRequirementMap data = Magic.getSpellData(spellId);
        /*
         * The cached value at key 2871 is the spell's book id - except
         * Ataraxia's combat-spell GR maps load with null values, so the
         * cache returns 0 (NORMAL_BOOK) for every spell. Fall back to the
         * player's actively-selected spell book so the ancient table is
         * reachable instead of always shadowing it under NORMAL.
         */
        int spellBook = data == null ? Magic.NORMAL_BOOK : data.getIntValue(2871);
        if (spellBook == Magic.NORMAL_BOOK)
            spellBook = player.getCombatDefinitions().getSpellBook();

        int registered = Rs2SpellMaxHits.lookup(player, spellId, spellBook);
        if (registered >= 0) {
            return registered;
        }
        return ataraxiaFallbackSpellMaxRs2(player, offHand);
    }

    private static int ataraxiaFallbackSpellMaxRs2(Player player, boolean offHand) {
        /*
         * Post-2009 spells aren't in Rs2SpellMaxHits yet. Convert Ataraxia's
         * EOC-scale spell max back into old HP units so the surrounding roll
         * distribution stays pre-EOC shaped. Replace with real classic-style
         * values in Rs2SpellMaxHits as content is audited.
         */
        int handDamage = player.getCombatDefinitions().getHandDamage(offHand);
        int currentMaxAtaraxia = (int) Math.ceil(handDamage * 2.3D);
        return Math.max(1, currentMaxAtaraxia / Rs2CombatFormula.ATARAXIA_DAMAGE_SCALE);
    }

    private static PlayerDamageContext contextOrNone(PlayerDamageContext context) {
        return context == null ? PlayerDamageContext.none() : context;
    }
}
