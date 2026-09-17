package com.rs.game.player.actions.crafting;

import com.rs.game.Animation;
import com.rs.game.activites.halloween.ClueScrollDistributor;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.content.achievementsystem.AchievementList;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

public class JewellerySmithing {

    public static final int INTERFACE = 675;

    private static final int[] MOLDS = {1592, 1597, 1595, 11065};

    private static final int[] GEMS = {2357, 1607, 1605, 1603, 1601, 1615, 6573};

    private static final int[][] ITEMS = {{1635, 1637, 1639, 1641, 1643, 1645, 6575}, {1654, 1656, 1658, 1660, 1662, 1664, 6577}, {1673, 1675, 1677, 1679, 1681, 1683, 6579}, {11069, 11072, 11076, 11085, 11092, 11115, 11130}};

    private static final int[] COMPONENTS_BASE = {20, 39, 58, 77};

    private static final double[][] EXPERIENCE = {{15, 40, 55, 70, 85, 100, 115}, {20, 55, 60, 75, 90, 105, 120}, {25, 60, 65, 80, 95, 110, 125}, {30, 65, 70, 85, 100, 150, 165}};

    private static final byte[][] LEVEL = {{5, 20, 27, 34, 43, 55, 67}, {6, 22, 29, 40, 56, 72, 82}, {7, 23, 30, 42, 58, 74, 84}, {8, 24, 31, 50, 70, 80, 90}};

    private static final int[] ONYX = {6575, 6577, 6579, 11130};

    public static void openInterface(Player player) {
        player.getInterfaceManager().sendInterface(INTERFACE);
        callCS2(player);
        for (int primaryIndex = 0; primaryIndex < MOLDS.length; primaryIndex++) {
            player.getPackets().sendIComponentText(INTERFACE, 16 + (primaryIndex * 19), "");
            for (int secondaryIndex = 0; secondaryIndex < ITEMS[primaryIndex].length; secondaryIndex++) {
                player.getPackets().sendItems((299 + (primaryIndex * 14) + secondaryIndex), new Item[]{new Item(ITEMS[primaryIndex][secondaryIndex])});
            }
        }
    }

    private static void callCS2(Player player) {
        for (int primaryIndex = 0; primaryIndex < COMPONENTS_BASE.length; primaryIndex++) {
            for (int secondaryIndex = 0; secondaryIndex < ITEMS[primaryIndex].length; secondaryIndex++) {
                player.getPackets().sendInterSetItemsOptionsScript(INTERFACE, (COMPONENTS_BASE[primaryIndex] + secondaryIndex * 2), (299 + (primaryIndex * 14) + secondaryIndex), false, 6, 4, "Make - 1", "Make - 5", "Make - 10", "Make - All");
                player.getPackets().sendUnlockIComponentOptionSlots(INTERFACE, (COMPONENTS_BASE[primaryIndex] + secondaryIndex * 2), 0, 28, 0, 1, 2, 3, 4);
            }
        }
    }

    public static void handleButtonClick(Player player, int componentId, final int tick) {
        for (int primaryIndex = 0; primaryIndex < COMPONENTS_BASE.length; primaryIndex++) {
            for (int secondaryIndex = 0; secondaryIndex < ITEMS[primaryIndex].length; secondaryIndex++) {
                if (componentId == (COMPONENTS_BASE[primaryIndex] + secondaryIndex * 2)) {
                    player.closeInterfaces();
                    startProduct(player, getActualProduct(primaryIndex, secondaryIndex), tick);
                    return;
                }
            }
        }
    }

    public static boolean isProduct(int productId) {
        return getProductIndexes(productId) != null;
    }

    public static int getLevel(int productId) {
        int[] indexes = getProductIndexes(productId);
        return indexes == null ? 1 : LEVEL[indexes[0]][indexes[1]];
    }

    public static int getMaxQuantity(Player player, int productId) {
        int[] indexes = getProductIndexes(productId);
        if (indexes == null)
            return 0;
        int max = player.getInventory().getAmountOf(2357);
        if (indexes[1] != 0)
            max = Math.min(max, player.getInventory().getAmountOf(GEMS[indexes[1]]));
        return Math.min(max, 60);
    }

    public static void startProduct(Player player, int productId, int quantity) {
        final int[] indexes = getProductIndexes(productId);
        if (indexes == null) {
            player.sendMessage("That item is not wired into jewellery crafting yet.");
            return;
        }
        final int actionPrimaryIndex = indexes[0];
        final int actionSecondaryIndex = indexes[1];
        final int actionProductId = getActualProduct(actionPrimaryIndex, actionSecondaryIndex);
        final int requestedTicks = Math.max(1, Math.min(60, quantity));
        player.getActionManager().setAction(new Action() {

            int ticks;

            @Override
            public boolean start(Player player) {
                this.ticks = Math.min(requestedTicks, Math.max(1, getMaxQuantity(player, actionProductId)));
                return process(player);
            }

            @Override
            public boolean process(Player player) {
                int level = LEVEL[actionPrimaryIndex][actionSecondaryIndex];
                if (player.getSkills().getLevel(Skills.CRAFTING) < level) {
                    player.sendMessage("You need a Crafting level of " + level + ".");
                    return false;
                } else if (!player.getInventory().containsItem(2357, 1)) {
                    player.sendMessage("You need a gold bar in order to do this.");
                    return false;
                } else if (actionSecondaryIndex != 0 && !player.getInventory().containsItem(GEMS[actionSecondaryIndex], 1)) {
                    player.sendMessage("You are missing the required items in order to do this.");
                    return false;
                }
                return true;
            }

            @Override
            public int processWithDelay(Player player) {
                player.setNextAnimation(new Animation(32626));
                player.getInventory().deleteItem(2357, 1);
                if (actionSecondaryIndex != 0)
                    player.getInventory().deleteItem(GEMS[actionSecondaryIndex], 1);
                player.getInventory().addItem(actionProductId, 1);
                player.getSkills().addXp(Skills.CRAFTING, EXPERIENCE[actionPrimaryIndex][actionSecondaryIndex]);
                player.addItemsMade();
                player.sendMessage("You've successfully made jewellery; " + "items crafted: " + Colors.RED + Utils.getFormattedNumber(player.getItemsMade()) + "</col>.", true);
                if (actionProductId == 1683) {
                    player.getAchievements().updateProgress(1, AchievementList.CRAFT_DRAGONSTONE_AMULET);
                }
                ClueScrollDistributor.givePlayerClueScrollIfProbable(player, ClueScrollDistributor.SKILLING_PERCENT);
                ticks--;
                if (ticks <= 0)
                    return -1;
                return 2;
            }

            @Override
            public void stop(Player player) {
                setActionDelay(player, 3);
            }
        });
    }

    private static int[] getProductIndexes(int productId) {
        for (int primaryIndex = 0; primaryIndex < ITEMS.length; primaryIndex++) {
            for (int secondaryIndex = 0; secondaryIndex < ITEMS[primaryIndex].length; secondaryIndex++) {
                if (getActualProduct(primaryIndex, secondaryIndex) == productId)
                    return new int[] { primaryIndex, secondaryIndex };
            }
        }
        return null;
    }

    private static int getActualProduct(int primaryIndex, int secondaryIndex) {
        return secondaryIndex == 6 ? ONYX[primaryIndex] : ITEMS[primaryIndex][secondaryIndex];
    }

}
