package com.rs.game.player.content.items;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

/**
 * Handles the Herblore Box item.
 *
 * @author Noel
 */
public class HerbloreBox {

    /**
     * Enum containing all of the common rewards.
     */
    public static Item[] COMMON = {new Item(259, 5), new Item(203, 5),new Item(261, 5), new Item(263, 5), new Item(265, 5),
            new Item(267, 5), new Item(269, 5), new Item(2481, 5), new Item(3000, 5), new Item(2998, 5), new Item(21624, 5)};
    /**
     * Enum containing all of the uncommon rewards.
     */
    public static Item[] UNCOMMON = {new Item(259, 10), new Item(261, 10), new Item(263, 10), new Item(265, 10),
            new Item(267, 8), new Item(269, 8), new Item(2481, 8), new Item(3000, 8), new Item(2998, 8), new Item(21624, 8),
            new Item(5315, 3), new Item(5316, 3), new Item(5289, 3), new Item(5288, 3)};
    /**
     * Enum containing all of the rare rewards.
     */
    public static Item[] RARES = {new Item(259, 15), new Item(261, 15), new Item(263, 15), new Item(265, 15),
            new Item(267, 15), new Item(269, 15), new Item(2481, 15), new Item(3000, 15), new Item(2998, 15), new Item(21624, 15),
            new Item(5315, 5), new Item(5316, 5), new Item(5289, 5), new Item(5288, 5)};

    /**
     * Handles the item.
     *
     * @param player The player.
     */
    public static void open(Player player, int amount) {
        int baseAmount = player.isGroupIronman()? 3 : 5;
        if (player.getInventory().deleteOneItem(new Item(3062, amount))) {
            Multiset<Integer> items = HashMultiset.create();
            for (int i = 0; i < amount * baseAmount; i++) {
                Item item = (Utils.random(1000) < 800 ? COMMON[Utils.random(COMMON.length)]
                        : (Utils.random(1000) >= 800 && Utils.random(1000) < 935 ? UNCOMMON[Utils.random(UNCOMMON.length)]
                        : RARES[Utils.random(RARES.length)]));
                items.add(item.getId(), item.getAmount());
            }

            for (Multiset.Entry<Integer> e : items.entrySet()) {
                Item item = new Item(e.getElement(), e.getCount());
                player.sendFilteredMessage(Colors.RED + "You receive: " + item.getName() + " (x" + item.getAmount() + ").");
                player.addItem(item);
                player.getBank().refreshItems();
            }
        } else {
            //player.sendMessage("Box Reward error. Go cheat elsewhere, you prick :)");
        }
    }

}