package com.rs.game.player.client;

import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.item.floor.FloorItem;
import com.rs.game.player.Player;
import com.rs.game.player.content.ItemConstants;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/** Ordinary inventory Drop using the original Region and World floor-item lifetime. */
public final class Native950InventoryDrop {
    private Native950InventoryDrop() { }

    public static Result drop(Player player, Native950Containers containers, int slot, int expectedId) {
        return drop(player, containers, slot, expectedId,
                tile -> World.getRegion(tile.getRegionId()).getGroundItemsSafe(),
                ItemConstants::isTradeable, Native950InventoryDrop::scheduleLifetime);
    }

    static Result drop(Player player, Native950Containers containers, int slot, int expectedId,
            Function<WorldTile, List<FloorItem>> ground, Predicate<Item> transferable,
            Consumer<FloorItem> schedule) {
        if (player == null || !player.isNative950() || containers == null || !containers.ownsInventory(player))
            return Result.refused("That backpack is not available.");
        if (!player.isActive() || player.hasFinished() || player.isDead() || player.isLocked()
                || player.isNative950ForceMovementActive() || player.getNextWorldTile() != null
                || Boolean.TRUE.equals(player.getTemporaryAttributtes().get("teleporting")))
            return Result.refused("You cannot drop items right now.");
        if (slot < 0 || slot >= Native950Containers.INVENTORY_SIZE)
            return Result.refused("That backpack slot is not available.");
        Item source = player.getInventory().items.get(slot);
        if (source == null || source.getId() != expectedId)
            return Result.refused("The item in that slot has changed.");
        int amount = source.getAmount();
        Native950ItemCatalog.Entry type = containers.itemType(expectedId);
        if (amount < 1 || source.getCharges() != 0 || source.getAttributes() != null || source.getInventionData() != null
                || type == null || (!type.stackable && amount != 1))
            return Result.refused("Dropping that item's special state is not supported yet.");
        boolean ordinaryDrop = false;
        for (int option = 1; option <= 5; option++) {
            String label = type.option(option);
            if ("Destroy".equalsIgnoreCase(label) || "Discard".equalsIgnoreCase(label))
                return Result.refused("That item requires its own destroy or discard action.");
            ordinaryDrop |= "Drop".equalsIgnoreCase(label);
        }
        if (!ordinaryDrop) return Result.refused("That item does not have a Drop option.");
        // Preserve both original controller gates. A controller may replace or consume its
        // item, so the exact identity and quantity are checked again at the final commit.
        if (!player.getControlerManager().canDropItem(source)
                || !player.getControlerManager().canDeleteInventoryItem(expectedId, amount))
            return Result.refused("You cannot drop that item here.");
        WorldTile tile = new WorldTile(player);
        boolean mayTransfer = transferable.test(source);
        FloorItem floor = new FloorItem(new Item(expectedId, amount), tile, player, false, true);
        floor.setPublicTransferAllowed(mayTransfer);
        List<FloorItem> pile = ground.apply(tile);
        if (pile == null) return Result.refused("The ground here is not available.");
        if (!containers.removeInventorySlot(slot, source, expectedId, amount))
            return Result.refused("The item in that slot has changed.");
        try {
            // Owner-thread-only transaction: session projections run after both mutations.
            // Keep equal piles separate; the native viewport combines their visible counts.
            if (!pile.add(floor)) throw new IllegalStateException("Ground item was not stored");
            schedule.accept(floor);
        } catch (RuntimeException failure) {
            // A failing publisher may already have inserted the item. Remove this identity
            // only, never an equal existing pile, before restoring the exact source slot.
            if (pile.stream().anyMatch(item -> item == floor)) pile.removeIf(item -> item == floor);
            containers.restoreInventorySlot(slot, source);
            return Result.refused("The item could not be dropped. It is still in your backpack.");
        }
        return new Result(amount, null, floor);
    }

    static void scheduleLifetime(final FloorItem floor) {
        WorldTasksManager.schedule(new WorldTask() {
            @Override public void run() {
                stop();
                // Existing native identity checks make picked-up or rolled-back timers
                // harmless, including when another equal pile occupies the same tile.
                World.turnPublic(floor, Native950Loot.PUBLIC_SECONDS);
            }
        }, Utils.secondsToTicks(Native950Loot.PRIVATE_SECONDS));
    }

    public static final class Result {
        public final int moved;
        public final String reason;
        public final FloorItem groundItem;
        private Result(int moved, String reason, FloorItem groundItem) {
            this.moved = moved; this.reason = reason; this.groundItem = groundItem;
        }
        private static Result refused(String reason) { return new Result(0, reason, null); }
    }
}
