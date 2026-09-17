package com.rs.game.player.content.items;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;

import java.util.Map;

/**
 * Shared ore box storage based on RuneScape's post-rework ore boxes.
 */
public final class OreBox {

    private static final int BASE_CAPACITY = 100;
    private static final int LEVEL_CAPACITY_BONUS = 20;

    private static final OreBoxTier[] BOXES = {
            new OreBoxTier(44779, "bronze ore box", 7),
            new OreBoxTier(44781, "iron ore box", 18),
            new OreBoxTier(44783, "steel ore box", 29),
            new OreBoxTier(44785, "mithril ore box", 37),
            new OreBoxTier(44787, "adamant ore box", 41),
            new OreBoxTier(44789, "rune ore box", 55),
            new OreBoxTier(44791, "orikalkum ore box", 66),
            new OreBoxTier(44793, "necronium ore box", 72),
            new OreBoxTier(44795, "bane ore box", 85),
            new OreBoxTier(44797, "elder rune ore box", 95)
    };

    private static final OreBoxItem[] ITEMS = {
            new OreBoxItem(436, 0, false), new OreBoxItem(44799, 0, false),
            new OreBoxItem(438, 0, false), new OreBoxItem(44800, 0, false),
            new OreBoxItem(440, 1, false), new OreBoxItem(44801, 1, false),
            new OreBoxItem(453, 2, false), new OreBoxItem(44804, 2, false),
            new OreBoxItem(442, 2, true), new OreBoxItem(44802, 2, true),
            new OreBoxItem(447, 3, false), new OreBoxItem(44805, 3, false),
            new OreBoxItem(449, 4, false), new OreBoxItem(44807, 4, false),
            new OreBoxItem(44820, 4, false), new OreBoxItem(44806, 4, false),
            new OreBoxItem(444, 4, true), new OreBoxItem(44803, 4, true),
            new OreBoxItem(451, 5, false), new OreBoxItem(44808, 5, false),
            new OreBoxItem(44822, 6, false), new OreBoxItem(44809, 6, false),
            new OreBoxItem(44824, 6, false), new OreBoxItem(44810, 6, false),
            new OreBoxItem(44826, 7, false), new OreBoxItem(44811, 7, false),
            new OreBoxItem(44828, 7, false), new OreBoxItem(44812, 7, false),
            new OreBoxItem(21778, 8, false), new OreBoxItem(44813, 8, false),
            new OreBoxItem(44830, 9, false), new OreBoxItem(44814, 9, false),
            new OreBoxItem(44832, 9, false), new OreBoxItem(44815, 9, false)
    };

    private OreBox() {

    }

    public static boolean isOreBox(int itemId) {
        return getBox(itemId) != null;
    }

    public static boolean fill(Player player, int itemId) {
        OreBoxTier box = getBox(itemId);
        if (box == null)
            return false;
        if (countOreBoxes(player) > 1) {
            player.sendMessage("I can't hold all these ore boxes!");
            return true;
        }
        int moved = 0;
        for (OreBoxItem item : ITEMS) {
            if (!box.canStore(item))
                continue;
            int available = player.getInventory().getNumberOf(item.itemId);
            if (available <= 0)
                continue;
            int amount = Math.min(available, getFreeCapacity(player, box, item));
            if (amount <= 0)
                continue;
            player.getInventory().deleteItem(item.itemId, amount, true);
            addStored(player, item.itemId, amount);
            moved += amount;
        }
        if (moved > 0)
            player.sendMessage("You add " + moved + " item" + (moved == 1 ? "" : "s") + " to your ore box.");
        else
            player.sendMessage("You have no ore or stone spirits that can be added to this ore box.");
        return true;
    }

    public static boolean check(Player player, int itemId) {
        OreBoxTier box = getBox(itemId);
        if (box == null)
            return false;
        StringBuilder message = new StringBuilder("Ore box: ");
        boolean any = false;
        for (OreBoxItem item : ITEMS) {
            if (!box.canStore(item))
                continue;
            int amount = getStored(player, item.itemId);
            if (amount <= 0)
                continue;
            if (any)
                message.append(", ");
            message.append(ItemDefinitions.getItemDefinitions(item.itemId).getName())
                    .append(" ")
                    .append(amount)
                    .append("/")
                    .append(getCapacity(player, box, item));
            any = true;
        }
        player.sendMessage(any ? message.toString() : "Your ore box is empty.");
        return true;
    }

    public static boolean emptyToMetalBank(Player player, int itemId) {
        OreBoxTier box = getBox(itemId);
        if (box == null)
            return false;
        int moved = 0;
        Map<Integer, Integer> contents = player.getOreBoxContents();
        for (OreBoxItem item : ITEMS) {
            if (!box.canStore(item))
                continue;
            int amount = getStored(player, item.itemId);
            if (amount <= 0)
                continue;
            contents.remove(item.itemId);
            player.addMetalBankItem(item.itemId, amount);
            moved += amount;
        }
        if (moved > 0)
            player.sendMessage("You empty your ore box into your metal bank.");
        else
            player.sendMessage("Your ore box is empty.");
        return true;
    }

    public static boolean canStore(Player player, int itemId, int amount) {
        OreBoxTier box = getSingleHeldBox(player);
        OreBoxItem item = getItem(itemId);
        return box != null && item != null && box.canStore(item) && getFreeCapacity(player, box, item) >= amount;
    }

    public static int store(Player player, int itemId, int amount) {
        if (amount <= 0)
            return 0;
        OreBoxTier box = getSingleHeldBox(player);
        OreBoxItem item = getItem(itemId);
        if (box == null || item == null || !box.canStore(item))
            return amount;
        int stored = Math.min(amount, getFreeCapacity(player, box, item));
        if (stored <= 0)
            return amount;
        addStored(player, itemId, stored);
        player.sendMessage("Your ore box stores " + stored + " "
                + ItemDefinitions.getItemDefinitions(itemId).getName().toLowerCase() + ".");
        return amount - stored;
    }

    private static OreBoxTier getSingleHeldBox(Player player) {
        if (countOreBoxes(player) != 1)
            return null;
        for (Item item : player.getInventory().getItems().getItems()) {
            if (item == null)
                continue;
            OreBoxTier box = getBox(item.getId());
            if (box != null)
                return box;
        }
        return null;
    }

    private static int countOreBoxes(Player player) {
        int count = 0;
        for (Item item : player.getInventory().getItems().getItems()) {
            if (item != null && getBox(item.getId()) != null)
                count += item.getAmount();
        }
        return count;
    }

    private static int getFreeCapacity(Player player, OreBoxTier box, OreBoxItem item) {
        return Math.max(0, getCapacity(player, box, item) - getStored(player, item.itemId));
    }

    private static int getCapacity(Player player, OreBoxTier box, OreBoxItem item) {
        if (item.fixedCapacity)
            return BASE_CAPACITY;
        int capacity = BASE_CAPACITY;
        if (player.getSkills().getLevelForXp(Skills.MINING) >= item.capacityLevel)
            capacity += LEVEL_CAPACITY_BONUS;
        return capacity;
    }

    private static int getStored(Player player, int itemId) {
        Integer amount = player.getOreBoxContents().get(itemId);
        return amount == null ? 0 : amount;
    }

    private static void addStored(Player player, int itemId, int amount) {
        Map<Integer, Integer> contents = player.getOreBoxContents();
        int current = getStored(player, itemId);
        contents.put(itemId, current + amount);
    }

    private static OreBoxTier getBox(int itemId) {
        for (OreBoxTier box : BOXES) {
            if (box.itemId == itemId)
                return box;
        }
        return null;
    }

    private static OreBoxItem getItem(int itemId) {
        for (OreBoxItem item : ITEMS) {
            if (item.itemId == itemId)
                return item;
        }
        return null;
    }

    private static final class OreBoxTier {
        private final int itemId;
        private final String name;
        private final int capacityLevel;

        private OreBoxTier(int itemId, String name, int capacityLevel) {
            this.itemId = itemId;
            this.name = name;
            this.capacityLevel = capacityLevel;
        }

        private boolean canStore(OreBoxItem item) {
            return item.minBoxItemId <= itemId;
        }
    }

    private static final class OreBoxItem {
        private final int itemId;
        private final int minBoxItemId;
        private final int capacityLevel;
        private final boolean fixedCapacity;

        private OreBoxItem(int itemId, int minBoxIndex, boolean fixedCapacity) {
            this.itemId = itemId;
            this.minBoxItemId = BOXES[minBoxIndex].itemId;
            this.capacityLevel = BOXES[minBoxIndex].capacityLevel;
            this.fixedCapacity = fixedCapacity;
        }
    }
}
