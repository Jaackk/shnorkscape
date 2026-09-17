package com.rs.game.player.combat.rs2;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.Combat;

/**
 * Four-layer resolver that maps any item id to a pre-EOC-style ClassicBonuses
 * snapshot. Resolution order:
 *
 *   1) Manual override table - hand-curated for top-tier items and outliers
 *      where neither database has data and the curve misses badly.
 *   2) RS2BonusDatabase / 2009scape - authoritative for items present in the
 *      RS2 era (2009 game).
 *   3) RS2BonusDatabase / osrsbox - fallback for items 2009scape doesn't
 *      cover. May diverge from RS2 for items added 2007-2009 (chaotic, etc.).
 *   4) Tier-curve derivation - infers bonuses from the item's required level,
 *      equip slot, weapon vs. armour shape, and power vs. tank role. The
 *      catch-all for RS3-era content (2009-2019) absent from both databases.
 *
 * Resolved bonuses are cached per item id since item shape never changes at
 * runtime. Name matching against the database normalises off-hand prefixes,
 * poison suffixes, and charge counters - see RS2BonusDatabase.normalise.
 *
 * The tier curve is calibrated to land near classic reference values for
 * tier 60+ items, where most actual gameplay happens. Items in either
 * database always beat the curve.
 */
public final class ClassicItemBonusResolver {

    private static final Map<Integer, ClassicBonuses> CACHE = new ConcurrentHashMap<>();

    private ClassicItemBonusResolver() {
    }

    public static ClassicBonuses resolve(int itemId) {
        if (itemId <= 0) return ClassicBonuses.ZERO;
        ClassicBonuses cached = CACHE.get(itemId);
        if (cached != null) return cached;

        ClassicBonuses resolved = resolveUncached(itemId);
        CACHE.put(itemId, resolved);
        return resolved;
    }

    /**
     * Drops cached bonuses. Useful for /reload tooling during Phase B/C while
     * you're iterating on the override table.
     */
    public static void clearCache() {
        CACHE.clear();
    }

    /**
     * Returns the equipped items' summed classic bonuses. Phase A sums every
     * combat-relevant slot regardless of mainHand; off-hand weapon strength is
     * halved to avoid double-dipping with the main weapon's strength bonus.
     * The mainHand parameter is reserved for future per-hand routing.
     */
    public static ClassicBonuses sumEquipped(Player player, boolean mainHand) {
        Equipment equipment = player.getEquipment();
        ClassicBonuses total = ClassicBonuses.ZERO;
        int[] slots = {
                Equipment.SLOT_HAT, Equipment.SLOT_CAPE, Equipment.SLOT_AMULET,
                Equipment.SLOT_WEAPON, Equipment.SLOT_SHIELD,
                Equipment.SLOT_CHEST, Equipment.SLOT_LEGS,
                Equipment.SLOT_HANDS, Equipment.SLOT_FEET,
                Equipment.SLOT_RING, Equipment.SLOT_ARROWS
        };
        for (int slot : slots) {
            Item item = equipment.getItem(slot);
            if (item == null) continue;
            ClassicBonuses partial = resolve(item.getId());
            if (slot == Equipment.SLOT_SHIELD
                    && (partial.strBonus > 0 || partial.rangeStrBonus > 0)) {
                partial = halveOffhandStrength(partial);
            }
            total = total.plus(partial);
        }
        return total;
    }

    private static ClassicBonuses resolveUncached(int itemId) {
        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(itemId);
        String name = defs == null ? null : defs.getName();

        // Layer 1: Phase C overrides (id-keyed first, then name-keyed).
        ClassicBonuses override = RS2BonusDatabase.lookupOverride(itemId, name);
        if (override != null) return override;

        if (name != null) {
            // Layer 2: 2009scape authoritative for RS2-era items.
            ClassicBonuses fromRs2 = RS2BonusDatabase.lookup2009scape(name);
            if (fromRs2 != null) return fromRs2;
            // Layer 3: osrsbox fallback for items 2009scape doesn't cover.
            ClassicBonuses fromOsrs = RS2BonusDatabase.lookupOsrsbox(name);
            if (fromOsrs != null) return fromOsrs;
        }

        // Layer 4: tier-curve derivation - catch-all for RS3-only content.
        return deriveFromTier(itemId);
    }

    private static ClassicBonuses halveOffhandStrength(ClassicBonuses b) {
        return new ClassicBonuses(
                b.stabAtk, b.slashAtk, b.crushAtk, b.magicAtk, b.rangeAtk,
                b.stabDef, b.slashDef, b.crushDef, b.magicDef, b.rangeDef,
                b.strBonus / 2, b.rangeStrBonus / 2, b.magicDamagePercent, b.prayerBonus);
    }

    // ---------- Layer 3: tier-curve derivation ----------

    private static ClassicBonuses deriveFromTier(int itemId) {
        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(itemId);
        if (defs == null) return ClassicBonuses.ZERO;

        int slot = defs.getEquipSlot();
        int combatType = defs.getType();
        int prayerBonus = Math.max(0, defs.getPrayerBonus() / 10);

        boolean isWeaponSlot = slot == Equipment.SLOT_WEAPON || slot == Equipment.SLOT_SHIELD;
        boolean isWeapon = isWeaponSlot
                && (defs.isMeleeTypeWeapon() || defs.isRangeTypeWeapon() || defs.isMagicTypeWeapon());

        if (isWeapon) {
            return deriveWeapon(defs, combatType, prayerBonus);
        }
        return deriveArmour(defs, slot, combatType, prayerBonus);
    }

    private static ClassicBonuses deriveWeapon(ItemDefinitions defs, int combatType, int prayerBonus) {
        int tier = weaponTier(defs, combatType);
        boolean twoHand = defs.getEquipType() == 5;

        if (combatType == Combat.MAGIC_TYPE) {
            // Staves give a small magic accuracy bonus. Spell damage in RS2
            // mode comes from Rs2SpellMaxHits (spell-driven), not gear, so
            // magicDamagePercent stays at 0 in Phase A.
            int mageAtk = (int) Math.ceil(tier * 0.30 + 5);
            return new ClassicBonuses(
                    0, 0, 0, mageAtk, 0,
                    0, 0, 0, 0, 0,
                    0, 0, 0, prayerBonus);
        }
        if (combatType == Combat.RANGE_TYPE) {
            double atkMult = twoHand ? 1.30 : 1.10;
            double strMult = twoHand ? 1.30 : 1.00;
            int rangeAtk = (int) Math.ceil(tier * atkMult);
            int rangeStr = (int) Math.ceil(tier * strMult);
            return new ClassicBonuses(
                    0, 0, 0, 0, rangeAtk,
                    0, 0, 0, 0, 0,
                    0, rangeStr, 0, prayerBonus);
        }

        double atkMult = twoHand ? 1.40 : 1.20;
        double strMult = twoHand ? 1.50 : 1.10;
        int primaryAtk = (int) Math.ceil(tier * atkMult);
        int secondaryAtk = (int) Math.ceil(tier * 0.40);
        int str = (int) Math.ceil(tier * strMult);
        int style = defs.getCombatStyle();
        int stab = 0, slash = 0, crush = 0;
        if (style == Combat.STAB_STYLE) {
            stab = primaryAtk;
            slash = secondaryAtk;
        } else if (style == Combat.SLASH_STYLE) {
            slash = primaryAtk;
            stab = secondaryAtk;
            crush = secondaryAtk;
        } else if (style == Combat.CRUSH_STYLE) {
            crush = primaryAtk;
            slash = secondaryAtk;
        } else {
            slash = primaryAtk;
        }
        return new ClassicBonuses(
                stab, slash, crush, 0, 0,
                0, 0, 0, 0, 0,
                str, 0, 0, prayerBonus);
    }

    private static ClassicBonuses deriveArmour(ItemDefinitions defs, int slot, int combatType, int prayerBonus) {
        int tier = armourTier(defs, combatType);
        boolean power = defs.getDamage(combatType == Combat.RANGE_TYPE ? Combat.RANGE_TYPE
                : combatType == Combat.MAGIC_TYPE ? Combat.MAGIC_TYPE : Combat.MELEE_TYPE) > 0;
        return armourFromTier(slot,combatType,prayerBonus,tier,power);
    }
    /** Same original armour curve with caller-validated current-cache requirements. */
    public static ClassicBonuses armourFromTier(int slot,int combatType,int prayerBonus,int tier,boolean power) {
        if (tier <= 0 && slot != Equipment.SLOT_ARROWS) {
            // Non-combat or jewelry without level requirements gets 0 stats
            // (with prayer bonus passed through if any).
            return new ClassicBonuses(
                    0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0,
                    0, 0, 0, prayerBonus);
        }

        SlotShape shape = slotShape(slot);

        // Range / mage armour scales lower per tier than melee armour - there
        // isn't a great heuristic for the tier-70 spread (Karil's vs Armadyl
        // both req 70), so this lands in between. Phase B's per-item lookup
        // will replace this for items in the OSRS dataset.
        double styleAdjust;
        if (combatType == Combat.RANGE_TYPE) styleAdjust = 0.55;
        else if (combatType == Combat.MAGIC_TYPE) styleAdjust = 0.40;
        else styleAdjust = 1.00;

        int defValue = (int) Math.ceil(tier * shape.defMult * styleAdjust);
        int strBonus = (int) Math.ceil(tier * (power ? shape.strMult : 0.0));
        int rangeStr = combatType == Combat.RANGE_TYPE && slot == Equipment.SLOT_ARROWS
                ? (int) Math.ceil(Math.max(1, tier) * 1.20)
                : 0;

        int stabDef = 0, slashDef = 0, crushDef = 0, magicDef = 0, rangeDef = 0;
        if (combatType == Combat.MAGIC_TYPE) {
            magicDef = defValue;
        } else if (combatType == Combat.RANGE_TYPE) {
            rangeDef = defValue;
            stabDef = (int) Math.ceil(defValue * 0.50);
            slashDef = (int) Math.ceil(defValue * 0.50);
            crushDef = (int) Math.ceil(defValue * 0.50);
        } else {
            stabDef = defValue;
            slashDef = defValue;
            crushDef = (int) Math.ceil(defValue * 0.95);
            rangeDef = (int) Math.ceil(defValue * 0.30);
        }

        return new ClassicBonuses(
                0, 0, 0, 0, 0,
                stabDef, slashDef, crushDef, magicDef, rangeDef,
                strBonus, rangeStr, 0, prayerBonus);
    }

    private static int weaponTier(ItemDefinitions defs, int combatType) {
        Map<Integer, Integer> reqs = defs.getWearingSkillRequiriments();
        int level = 1;
        if (reqs != null) {
            if (combatType == Combat.RANGE_TYPE) {
                level = Math.max(level, valueOrZero(reqs, Skills.RANGE));
            } else if (combatType == Combat.MAGIC_TYPE) {
                level = Math.max(level, valueOrZero(reqs, Skills.MAGIC));
            } else {
                level = Math.max(level, valueOrZero(reqs, Skills.ATTACK));
                level = Math.max(level, valueOrZero(reqs, Skills.STRENGTH));
            }
        }
        return Math.min(99, Math.max(1, level));
    }

    private static int armourTier(ItemDefinitions defs, int combatType) {
        Map<Integer, Integer> reqs = defs.getWearingSkillRequiriments();
        if (reqs == null) return 0;
        int level = valueOrZero(reqs, Skills.DEFENCE);
        if (combatType == Combat.RANGE_TYPE) {
            level = Math.max(level, valueOrZero(reqs, Skills.RANGE));
        } else if (combatType == Combat.MAGIC_TYPE) {
            level = Math.max(level, valueOrZero(reqs, Skills.MAGIC));
        }
        return Math.min(99, level);
    }

    private static int valueOrZero(Map<Integer, Integer> reqs, int skillId) {
        Integer v = reqs.get(skillId);
        return v == null ? 0 : v;
    }

    private static SlotShape slotShape(int slot) {
        // Multipliers tuned so a tier-70 chest lands near OSRS Karil/Ahrim/
        // Bandos scale, a tier-60 helm lands near Neitiznot/Helm of Neitiznot,
        // etc. Refine in Phase B once osrsbox data is in.
        if (slot == Equipment.SLOT_HAT)    return new SlotShape(0.55, 0.03);
        if (slot == Equipment.SLOT_CAPE)   return new SlotShape(0.05, 0.02);
        if (slot == Equipment.SLOT_AMULET) return new SlotShape(0.10, 0.05);
        if (slot == Equipment.SLOT_CHEST)  return new SlotShape(1.50, 0.07);
        if (slot == Equipment.SLOT_SHIELD) return new SlotShape(0.55, 0.00);
        if (slot == Equipment.SLOT_LEGS)   return new SlotShape(1.10, 0.05);
        if (slot == Equipment.SLOT_HANDS)  return new SlotShape(0.20, 0.05);
        if (slot == Equipment.SLOT_FEET)   return new SlotShape(0.25, 0.05);
        if (slot == Equipment.SLOT_RING)   return new SlotShape(0.00, 0.05);
        if (slot == Equipment.SLOT_ARROWS) return new SlotShape(0.00, 0.00);
        return new SlotShape(0.00, 0.00);
    }

    private static final class SlotShape {
        final double defMult;
        final double strMult;
        SlotShape(double defMult, double strMult) {
            this.defMult = defMult;
            this.strMult = strMult;
        }
    }
}
