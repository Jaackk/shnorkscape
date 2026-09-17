package com.rs.game.player.combat.rs2;

import com.rs.game.player.content.Combat;

/**
 * Immutable per-item or per-equipment-set classic bonus snapshot. Mirrors the
 * pre-EOC item bonus schema used by OSRS / 2009scape and consumed by
 * Rs2CombatFormula.roll().
 *
 * Bonuses are signed 32-bit ints. Negative defences are valid in the classic
 * model (mage gear conventionally carries negative stab/slash/crush def),
 * though Phase A's tier-curve resolver doesn't yet emit them.
 */
public final class ClassicBonuses {

    public static final ClassicBonuses ZERO = new ClassicBonuses(
            0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,
            0, 0, 0, 0);

    public final int stabAtk, slashAtk, crushAtk, magicAtk, rangeAtk;
    public final int stabDef, slashDef, crushDef, magicDef, rangeDef;
    public final int strBonus;
    public final int rangeStrBonus;
    public final int magicDamagePercent;
    public final int prayerBonus;

    public ClassicBonuses(
            int stabAtk, int slashAtk, int crushAtk, int magicAtk, int rangeAtk,
            int stabDef, int slashDef, int crushDef, int magicDef, int rangeDef,
            int strBonus, int rangeStrBonus, int magicDamagePercent, int prayerBonus) {
        this.stabAtk = stabAtk;
        this.slashAtk = slashAtk;
        this.crushAtk = crushAtk;
        this.magicAtk = magicAtk;
        this.rangeAtk = rangeAtk;
        this.stabDef = stabDef;
        this.slashDef = slashDef;
        this.crushDef = crushDef;
        this.magicDef = magicDef;
        this.rangeDef = rangeDef;
        this.strBonus = strBonus;
        this.rangeStrBonus = rangeStrBonus;
        this.magicDamagePercent = magicDamagePercent;
        this.prayerBonus = prayerBonus;
    }

    public ClassicBonuses plus(ClassicBonuses other) {
        if (other == null || other == ZERO) return this;
        return new ClassicBonuses(
                stabAtk + other.stabAtk, slashAtk + other.slashAtk, crushAtk + other.crushAtk,
                magicAtk + other.magicAtk, rangeAtk + other.rangeAtk,
                stabDef + other.stabDef, slashDef + other.slashDef, crushDef + other.crushDef,
                magicDef + other.magicDef, rangeDef + other.rangeDef,
                strBonus + other.strBonus, rangeStrBonus + other.rangeStrBonus,
                magicDamagePercent + other.magicDamagePercent, prayerBonus + other.prayerBonus);
    }

    /**
     * Returns the attack bonus for the given combat style id (Combat.STAB_STYLE
     * etc). Magic spell-style ids 1-4 collapse to magicAtk; ARROW/BOLT/THROWN
     * collapse to rangeAtk; melee styles return their individual stab/slash/
     * crush bonus.
     */
    public int attackBonusForStyle(int attackStyleId) {
        if (attackStyleId == Combat.STAB_STYLE) return stabAtk;
        if (attackStyleId == Combat.SLASH_STYLE) return slashAtk;
        if (attackStyleId == Combat.CRUSH_STYLE) return crushAtk;
        if (attackStyleId == Combat.ARROW_STYLE
                || attackStyleId == Combat.BOLT_STYLE
                || attackStyleId == Combat.THROWN_STYLE) return rangeAtk;
        if (attackStyleId >= 1 && attackStyleId <= 4) return magicAtk;
        if (attackStyleId == Combat.NO_SPELL_SELECTED_STYLE) return magicAtk;
        return 0;
    }

    /**
     * Returns the defence bonus for an incoming attack of the given style id.
     * Mirrors attackBonusForStyle's mapping rules.
     */
    public int defenceBonusForStyle(int incomingAttackStyleId) {
        if (incomingAttackStyleId == Combat.STAB_STYLE) return stabDef;
        if (incomingAttackStyleId == Combat.SLASH_STYLE) return slashDef;
        if (incomingAttackStyleId == Combat.CRUSH_STYLE) return crushDef;
        if (incomingAttackStyleId == Combat.ARROW_STYLE
                || incomingAttackStyleId == Combat.BOLT_STYLE
                || incomingAttackStyleId == Combat.THROWN_STYLE) return rangeDef;
        if (incomingAttackStyleId >= 1 && incomingAttackStyleId <= 4) return magicDef;
        if (incomingAttackStyleId == Combat.NO_SPELL_SELECTED_STYLE) return magicDef;
        // Bare attack-type fallbacks: 0=MELEE, 1=RANGE, 2=MAGIC.
        if (incomingAttackStyleId == Combat.MELEE_TYPE) return slashDef;
        if (incomingAttackStyleId == Combat.RANGE_TYPE) return rangeDef;
        if (incomingAttackStyleId == Combat.MAGIC_TYPE) return magicDef;
        return 0;
    }
}
