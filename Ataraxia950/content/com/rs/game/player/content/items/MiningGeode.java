package com.rs.game.player.content.items;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Handles Mining and Smithing rework geodes.
 */
public final class MiningGeode {

    private static final int SEDIMENTARY_GEODE = 44816;
    private static final int IGNEOUS_GEODE = 44817;
    private static final int METAMORPHIC_GEODE = 44818;

    private static final int SPIRIT_SHARDS = 12183;

    private static final Reward[] SEDIMENTARY_REWARDS = {
            new Reward(SPIRIT_SHARDS, 1, 1, 100),
            new Reward(1625, 1, 1, 40),
            new Reward(1627, 1, 1, 25),
            new Reward(1629, 1, 1, 15),
            new Reward(1623, 1, 1, 10),
            new Reward(1621, 1, 1, 5),
            new Reward(1619, 1, 1, 3),
            new Reward(1617, 1, 1, 2)
    };

    private static final Reward[] IGNEOUS_REWARDS = {
            new Reward(SPIRIT_SHARDS, 1, 1, 50),
            new Reward(1625, 1, 1, 5),
            new Reward(1627, 1, 1, 6),
            new Reward(1629, 1, 1, 7),
            new Reward(1623, 1, 1, 8),
            new Reward(1621, 1, 1, 9),
            new Reward(1619, 1, 1, 8),
            new Reward(1617, 1, 1, 5),
            new Reward(1631, 1, 1, 2)
    };

    private static final int[] DRAGON_EQUIPMENT = {
            1377, 14484, 31377, 1305, 1434, 4587, 29534, 7158, 3204, 1249,
            11335, 1149, 2513, 24365, 1187, 4087, 4585, 11732
    };

    private static final int[] TRISKELION_FRAGMENTS = { 28547, 28548, 28549 };

    private MiningGeode() {

    }

    public static boolean open(Player player, int itemId, int amount) {
        if (!isGeode(itemId))
            return false;
        int opened = 0;
        for (int i = 0; i < amount; i++) {
            if (!openOne(player, itemId))
                break;
            opened++;
        }
        if (opened == 0)
            player.sendMessage("You need more inventory space before opening this geode.");
        return true;
    }

    public static boolean openAll(Player player, int itemId) {
        if (!isGeode(itemId))
            return false;
        int opened = 0;
        while (true) {
            int geode = nextGeode(player);
            if (geode == -1)
                break;
            if (!openOne(player, geode))
                break;
            opened++;
        }
        if (opened == 0)
            player.sendMessage("You need more inventory space before opening these geodes.");
        return true;
    }

    public static boolean isGeode(int itemId) {
        return itemId == SEDIMENTARY_GEODE || itemId == IGNEOUS_GEODE || itemId == METAMORPHIC_GEODE;
    }

    private static int nextGeode(Player player) {
        if (player.getInventory().getNumberOf(SEDIMENTARY_GEODE) > 0)
            return SEDIMENTARY_GEODE;
        if (player.getInventory().getNumberOf(IGNEOUS_GEODE) > 0)
            return IGNEOUS_GEODE;
        if (player.getInventory().getNumberOf(METAMORPHIC_GEODE) > 0)
            return METAMORPHIC_GEODE;
        return -1;
    }

    private static boolean openOne(Player player, int itemId) {
        if (player.getInventory().getNumberOf(itemId) <= 0)
            return false;
        Item reward = rollReward(itemId);
        if (!canReceiveReward(player, itemId, reward))
            return false;
        player.getInventory().deleteItem(itemId, 1);
        player.getInventory().addItem(reward);
        player.sendMessage("You crack open the geode and find " + Utils.getAorAn(reward.getName().toLowerCase())
                + (reward.getAmount() > 1 ? " x " + reward.getAmount() : "") + ".");
        return true;
    }

    private static boolean canReceiveReward(Player player, int geodeId, Item reward) {
        ItemDefinitions definitions = ItemDefinitions.getItemDefinitions(reward.getId());
        int freeSlots = player.getInventory().getFreeSlots();
        if (player.getInventory().getNumberOf(geodeId) == 1)
            freeSlots++;
        if (definitions.isStackable())
            return freeSlots > 0 || player.getInventory().containsItemInInventory(reward.getId(), 1);
        return freeSlots >= reward.getAmount();
    }

    private static Item rollReward(int itemId) {
        if (itemId == SEDIMENTARY_GEODE)
            return rollWeighted(SEDIMENTARY_REWARDS, 200);
        if (itemId == IGNEOUS_GEODE)
            return rollWeighted(IGNEOUS_REWARDS, 100);
        return rollMetamorphic();
    }

    private static Item rollWeighted(Reward[] rewards, int totalWeight) {
        int roll = ThreadLocalRandom.current().nextInt(totalWeight);
        for (Reward reward : rewards) {
            roll -= reward.weight;
            if (roll < 0)
                return reward.toItem();
        }
        return rewards[rewards.length - 1].toItem();
    }

    private static Item rollMetamorphic() {
        int roll = ThreadLocalRandom.current().nextInt(14);
        switch (roll) {
            case 0:
                return new Item(18778, 1); // Starved ancient effigy
            case 1:
                return new Item(44819, 1); // First Age coin
            case 2:
                return new Item(36918, 1); // Anima crystal
            case 3:
                return ThreadLocalRandom.current().nextInt(100) == 0 ? new Item(42010, 1) : new Item(42009, 1);
            case 4:
                return new Item(TRISKELION_FRAGMENTS[ThreadLocalRandom.current().nextInt(TRISKELION_FRAGMENTS.length)], 1);
            case 5:
                return new Item(44813, Utils.inclusive(25, 75));
            case 6:
                return new Item(44814, Utils.inclusive(10, 30));
            case 7:
                return new Item(44815, Utils.inclusive(10, 30));
            case 8:
                return new Item(32262, Utils.inclusive(100, 300));
            case 9:
                return new Item(45985, Utils.inclusive(1, 10)); // Noted concentrated alloy bars.
            case 10:
                return new Item(45986, 1);
            case 11:
                return new Item(31867, Utils.inclusive(25, 50));
            case 12:
                return ThreadLocalRandom.current().nextInt(10) == 0
                        ? new Item(6571, 1)
                        : new Item(42954, Utils.inclusive(5, 15));
            default:
                return new Item(DRAGON_EQUIPMENT[ThreadLocalRandom.current().nextInt(DRAGON_EQUIPMENT.length)], 1);
        }
    }

    private static final class Reward {
        private final int itemId;
        private final int minAmount;
        private final int maxAmount;
        private final int weight;

        private Reward(int itemId, int minAmount, int maxAmount, int weight) {
            this.itemId = itemId;
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
            this.weight = weight;
        }

        private Item toItem() {
            return new Item(itemId, Utils.inclusive(minAmount, maxAmount));
        }
    }
}
