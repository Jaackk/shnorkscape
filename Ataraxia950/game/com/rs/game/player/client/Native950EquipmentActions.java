package com.rs.game.player.client;

import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.IntUnaryOperator;

/** Cache equipment policy over the original player's inventory, equipment, and controller ownership. */
public final class Native950EquipmentActions {
    private Native950EquipmentActions() { }

    public static Result equip(Player player, Native950Containers containers, int slot, int id, int cacheOption) {
        String unavailable = unavailable(player, containers);
        if (unavailable != null) return Result.refused(unavailable);
        if (slot < 0 || slot >= Native950Containers.INVENTORY_SIZE) return Result.refused("That backpack slot is not available.");
        Item source = player.getInventory().items.get(slot);
        if (source == null || source.getId() != id) return Result.refused("The item in that slot has changed.");
        if (!ordinary(source)) return Result.refused("Equipping this item's saved charges or custom state is not supported yet.");
        Native950EquipmentTypes.Type type = Native950EquipmentTypes.resolve(id);
        if (type == null || !type.isWearOption(cacheOption))
            return Result.refused("This item has no valid equipment definition in the current cache.");
        if (!type.genderSupported(player.getAppearence().isMale()))
            return Result.refused("This item cannot be worn with your current character model.");
        String requirement = missingRequirementsFromXp(type.requirements, player.getSkills()::getXp);
        if (requirement != null) return Result.refused(requirement);
        Set<Integer> conflicts = new LinkedHashSet<Integer>();
        for (int conflict : type.conflictSlots()) conflicts.add(conflict);
        // Conflicts are symmetric: equipping an offhand must also displace a two-handed weapon.
        for (int wornSlot = 0; wornSlot < Native950Containers.EQUIPMENT_SIZE; wornSlot++) {
            Item worn = player.getEquipment().getItems().get(wornSlot);
            if (worn == null) continue;
            Native950EquipmentTypes.Type existing = Native950EquipmentTypes.resolve(worn.getId());
            if (existing == null) return Result.refused("Your equipped item has no valid current-cache definition.");
            for (int conflict : existing.conflictSlots()) if (conflict == type.slot) conflicts.add(wornSlot);
        }
        int[] slots = new int[conflicts.size()]; int cursor = 0;
        for (int conflict : conflicts) slots[cursor++] = conflict;
        Native950Containers.EquipmentChange change = containers.prepareEquip(slot, id, slots);
        if (change.result.moved == 0) return Result.refused(change.result.reason);
        if (!player.getControlerManager().canEquip(type.slot, id)
                || !player.getControlerManager().canDeleteInventoryItem(id, source.getAmount()))
            return Result.refused("You cannot equip that item here.");
        String blocked = removalsAllowed(player, change);
        if (blocked != null) return Result.refused(blocked);
        return commit(player, containers, change, type);
    }

    public static Result remove(Player player, Native950Containers containers, int slot, int id) {
        String unavailable = unavailable(player, containers);
        if (unavailable != null) return Result.refused(unavailable);
        if (slot < 0 || slot >= Native950Containers.EQUIPMENT_SIZE) return Result.refused("That equipment slot is not available.");
        Item source = player.getEquipment().getItems().get(slot);
        if (source == null || source.getId() != id) return Result.refused("The equipment in that slot has changed.");
        if (!ordinary(source)) return Result.refused("Removing this item's saved charges or custom state is not supported yet.");
        Native950Containers.EquipmentChange change = containers.prepareUnequip(slot, id);
        if (change.result.moved == 0) return Result.refused(change.result.reason);
        String blocked = removalsAllowed(player, change);
        if (blocked != null) return Result.refused(blocked);
        return commit(player, containers, change, null);
    }

    private static String removalsAllowed(Player player, Native950Containers.EquipmentChange change) {
        for (int slot = 0; slot < change.beforeEquipment.length; slot++) {
            Item before = change.beforeEquipment[slot], after = change.nextEquipment[slot];
            if (before == null || (after != null && before.getId() == after.getId() && after.getAmount() >= before.getAmount())) continue;
            if (!cacheAllowsRemoval(Native950CacheItems.definition(before.getId())))
                return "This equipment requires its own removal action.";
            int returned = after != null && before.getId() == after.getId() ? before.getAmount() - after.getAmount() : before.getAmount();
            if (!player.getControlerManager().canRemoveEquip(slot, before.getId())
                    || !player.getControlerManager().canAddInventoryItem(before.getId(), returned))
                return "You cannot remove or return that equipment here.";
        }
        return null;
    }

    private static Result commit(Player player, Native950Containers containers,
            Native950Containers.EquipmentChange change, Native950EquipmentTypes.Type type) {
        // Controllers may alter activity, teleport state, gender or XP without touching an item.
        String blocked = unavailable(player, containers);
        if (blocked != null) return Result.refused(blocked);
        if (type != null) {
            blocked = missingRequirementsFromXp(type.requirements, player.getSkills()::getXp);
            if (blocked != null) return Result.refused(blocked);
            if (!type.genderSupported(player.getAppearence().isMale()))
                return Result.refused("This item cannot be worn with your current character model.");
        }
        Native950Containers.Result committed = change.commit();
        if (committed.moved == 0) return Result.refused(committed.reason);
        // Native appearance reads the cache models, hidden slots and movement profile.
        // Native combat owns its own loadout; do not run legacy item-ID bonuses or Invention hooks here.
        player.getAppearence().generateAppearenceData();
        return new Result(true, null);
    }

    static String missingRequirements(Map<Integer,Integer> requirements, IntUnaryOperator level) {
        return missingRequirements(requirements, (skill, required) -> level.applyAsInt(skill) >= required);
    }

    /** Stored XP uses each skill's verified curve, including virtual120 cape requirements. */
    static String missingRequirementsFromXp(Map<Integer,Integer> requirements, java.util.function.IntToDoubleFunction xp) {
        return missingRequirements(requirements, (skill, required) ->
                xp.applyAsDouble(skill) >= Skills.getXPForLevel(skill, required));
    }

    private static String missingRequirements(Map<Integer,Integer> requirements, java.util.function.BiPredicate<Integer,Integer> meets) {
        StringBuilder missing = new StringBuilder();
        for (Map.Entry<Integer,Integer> requirement : requirements.entrySet()) {
            int skill = requirement.getKey(), required = requirement.getValue();
            if (skill < 0 || skill >= Skills.SKILL_NAME.length || required < 1)
                return "This item's cache requirement cannot be checked yet.";
            if (meets.test(skill, required)) continue;
            if (missing.length() > 0) missing.append(", ");
            missing.append(Skills.SKILL_NAME[skill]).append(" ").append(required);
        }
        return missing.length() == 0 ? null : "You need " + missing + " to equip this item.";
    }

    static boolean cacheAllowsRemoval(com.rs.cache.loaders.ItemDefinitions definition) {
        // Native950 equipment script8471 exposes ordinary Remove only for default-zero2091.
        return definition != null && definition.getCSOpcode(2091) == 0;
    }

    private static boolean ordinary(Item item) {
        return item.getAmount() > 0 && item.getCharges() == 0 && item.getAttributes() == null && item.getInventionData() == null;
    }
    private static String unavailable(Player player, Native950Containers containers) {
        if (player == null || !player.isNative950() || containers == null || !containers.ownsInventory(player))
            return "Your equipment is not available.";
        if (!player.isActive() || player.hasFinished() || player.isDead() || player.isLocked()
                || player.isNative950ForceMovementActive() || player.getNextWorldTile() != null
                || Boolean.TRUE.equals(player.getTemporaryAttributtes().get("teleporting")))
            return "You cannot change equipment right now.";
        return null;
    }
    public static final class Result {
        public final boolean accepted;
        public final String reason;
        private Result(boolean accepted, String reason) { this.accepted = accepted; this.reason = reason; }
        private static Result refused(String reason) { return new Result(false, reason); }
    }
}
