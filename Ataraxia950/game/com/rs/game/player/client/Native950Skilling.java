package com.rs.game.player.client;

import com.rs.game.item.Item;
import com.rs.game.player.Player;

/** Native inventory boundary for the original910 Action/ActionManager skill actions. */
public final class Native950Skilling {
    private static final String CONTAINERS = "native950.skilling.containers";
    private Native950Skilling() { }
    static void attach(Player player, Native950Containers containers) {
        player.getTemporaryAttributtes().put(CONTAINERS, containers);
    }
    static void detach(Player player) { player.getTemporaryAttributtes().remove(CONTAINERS); }
    static Native950Containers containers(Player player) {
        if (player == null || !player.isNative950()) return null;
        Object value = player.getTemporaryAttributtes().get(CONTAINERS);
        return value instanceof Native950Containers ? (Native950Containers)value : null;
    }
    public static Native950ItemCatalog.Entry itemType(Player player, int id) {
        if (id < 0 || id > Native950ItemCatalog.MAX_ITEM_ID) return null;
        Native950Containers containers = containers(player);
        return containers == null ? null : containers.itemType(id);
    }
    public static boolean hasSpace(Player player, int id, int amount) {
        if (id < 0 || id > Native950ItemCatalog.MAX_ITEM_ID || amount < 1) return false;
        Native950Containers containers = containers(player);
        return containers != null && containers.canReceiveItem(new Item(id, amount));
    }
    public static boolean giveItem(Player player, int id, int amount) {
        if (id < 0 || id > Native950ItemCatalog.MAX_ITEM_ID || amount < 1) return false;
        Native950Containers containers = containers(player);
        return containers != null && player.getControlerManager().canAddInventoryItem(id, amount)
                && containers.receiveGroundItem(new Item(id, amount)).moved == amount;
    }
    public static boolean canGiveItems(Player player, Item[] rewards) {
        Native950Containers containers = containers(player);
        if (containers == null || !containers.canReceiveItems(rewards)) return false;
        for (Item item : rewards)
            if (!player.getControlerManager().canAddInventoryItem(item.getId(), item.getAmount())) return false;
        return true;
    }
    /** Multi-output harvests never grant just the first reward when the rest do not fit. */
    public static boolean giveItems(Player player, Item[] rewards) {
        return canGiveItems(player, rewards) && containers(player).receiveItems(rewards);
    }
    public static boolean canExchange(Player player, Item[] consumed, Item[] produced) {
        Native950Containers containers = containers(player);
        return containers != null && containers.canExchangeItems(consumed, produced);
    }

    /** Shared production boundary: refused output/capacity never destroys ingredients or grants XP. */
    public static boolean exchange(Player player, Item[] consumed, Item[] produced) {
        Native950Containers containers = containers(player);
        if (containers == null || !containers.canExchangeItems(consumed, produced)) return false;
        for (Item item : consumed)
            if (!player.getControlerManager().canDeleteInventoryItem(item.getId(), item.getAmount())) return false;
        for (Item item : produced)
            if (!player.getControlerManager().canAddInventoryItem(item.getId(), item.getAmount())) return false;
        return containers.exchangeItems(consumed, produced);
    }

    /** Inventory menu actions consume the selected resource, including duplicate ordinary items. */
    public static boolean canExchangeSlot(Player player, int slot, int expectedId, int amount, Item[] produced) {
        Native950Containers containers = containers(player);
        if (containers == null || !containers.ownsInventory(player) || slot < 0 || slot >= Native950Containers.INVENTORY_SIZE) return false;
        Item selected = player.getInventory().getItem(slot);
        return selected != null && containers.canExchangeSlot(slot, selected, expectedId, selected.getAmount(), amount, produced);
    }

    /** Preserve the original selected object and quantity through every controller decision. */
    public static boolean exchangeSlot(Player player, int slot, int expectedId, int amount, Item[] produced) {
        Native950Containers containers = containers(player);
        if (containers == null || !containers.ownsInventory(player) || slot < 0 || slot >= Native950Containers.INVENTORY_SIZE) return false;
        Item selected = player.getInventory().getItem(slot);
        if (selected == null) return false;
        int selectedAmount = selected.getAmount();
        if (!containers.canExchangeSlot(slot, selected, expectedId, selectedAmount, amount, produced)) return false;
        if (!player.getControlerManager().canDeleteInventoryItem(expectedId, amount)) return false;
        for (Item item : produced)
            if (!player.getControlerManager().canAddInventoryItem(item.getId(), item.getAmount())) return false;
        return containers.exchangeSlot(slot, selected, expectedId, selectedAmount, amount, produced);
    }

    public static boolean consumeItem(Player player, int id, int amount) {
        if (id < 0 || id > Native950ItemCatalog.MAX_ITEM_ID || amount < 1) return false;
        Native950Containers containers = containers(player);
        return containers != null && player.getControlerManager().canDeleteInventoryItem(id, amount)
                && containers.consumeItem(id, amount);
    }
}
