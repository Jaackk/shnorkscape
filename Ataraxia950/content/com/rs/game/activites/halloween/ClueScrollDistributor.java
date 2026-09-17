package com.rs.game.activites.halloween;

import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.TreasureTrails;
import com.rs.utils.Utils;

public class ClueScrollDistributor {
    public static final int SKILLING_PERCENT = 1;

    public static void givePlayerClueScrollIfProbable(Player player, int percentChance) {
        if (Utils.random(3000) <= percentChance) {
            int chance = Utils.random(101);
            int level = chance <= 10 ? 1 : chance <= 85 ? 2 : 3; //0 = easy,1 = med,2 = hard,3 = elite
            if (!player.getTreasureTrails().hasClueScrollItem()) {
                givePlayerClueScroll(player, level);
                player.sendMessage("The gods have blessed you with a clue scroll in your inventory.");
            }
        }
    }

    public static boolean isClueScroll(int itemId) {
        for (int clueScrollItemId : TreasureTrails.CLUE_SCROLLS) {
            if (itemId == clueScrollItemId) {
                return true;
            }
        }
        return false;
    }

    public static void givePlayerClueScroll(Player player, int level) {
        givePlayerClueScroll(player, level, true);
    }

    public static void givePlayerClueScroll(Player player, int level, boolean item) {
        if (item) {
            player.addItem(getItemForLevel(level));
        }
        player.getTreasureTrails().setCurrentClue(level);
    }

    private static Item getItemForLevel(int level) {
        return new Item(TreasureTrails.CLUE_SCROLLS[level]);
    }

    public static int getLevelForId(int itemId) {
        for (int i = 0; i < TreasureTrails.CLUE_SCROLLS.length; i++) {
            if (TreasureTrails.CLUE_SCROLLS[i] == itemId) {
                return i;
            }
        }
        return -1;
    }
}
