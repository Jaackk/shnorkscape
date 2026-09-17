package com.rs.game.player.content.items;

import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.utils.Utils;

/**
 * Handles the Charming Imp item.
 *
 * @author Noel
 */
public class CharmingImp {

    /**
     * The low-level charms.
     */
    private static final Item[] LOW_CHARMS = {new Item(12158, Utils.random(1, 3)), new Item(12159, 2)};
    /**
     * The mid-level charms.
     */
    private static final Item[] MID_CHARMS = {new Item(12158, Utils.random(1, 3)), new Item(12159, Utils.random(1, 3)),
            new Item(12160, 2), new Item(12163, 1)};
    /**
     * The high-level charms.
     */
    private static final Item[] HIGH_CHARMS = {new Item(12158, Utils.random(1, 3)),
            new Item(12159, Utils.random(1, 3)), new Item(12160, Utils.random(1, 3)), new Item(12163, Utils.random(1, 3))};
    /**
     * Representing the item.
     */
    static Item item;

    /**
     * Handles the item.
     *
     * @param player The player.
     */
    public static boolean handleCharmDrops(Player player, NPC npc) {
        if (Utils.random((player.getPerkManager().hasPerkActive(DonationPerk.TREASURE_GOBLIN) ? 3 : 4)) == 0) {
            item = null;
            if (npc.getCombatLevel() > 100)
                item = HIGH_CHARMS[Utils.random(HIGH_CHARMS.length)];
            else if (npc.getCombatLevel() > 50)
                item = MID_CHARMS[Utils.random(MID_CHARMS.length)];
            else
                item = LOW_CHARMS[Utils.random(LOW_CHARMS.length)];
            if (player.getInventory().containsOneItem(15584)) {
                if (player.getInventory().addItem(item))
                    return true;
                else if (player.getPerkManager().hasPerkActive(DonationPerk.TREASURE_GOBLIN)) {
                    player.getBank().addItem(item, true);
                    player.sendMessage("<col=4286f4><shad=000000>Treasure Goblin: x " + item.getAmount() + " "
                            + "of " + item.getName() + " " + (item.getAmount() == 1 ? "has" : "have") + " "
                            + "been sent to your bank.", true);
                } else {
                    dropItems(player, npc, item, true);
                    return true;
                }
            } else if (player.getPerkManager().hasPerkActive(DonationPerk.TREASURE_GOBLIN)) {
                player.getBank().addItem(item, true);
                player.sendMessage("<col=4286f4><shad=000000>Treasure Goblin: x" + item.getAmount() + " " + "of "
                        + item.getName() + " " + (item.getAmount() == 1 ? "has" : "have") + " "
                        + "been sent to your bank.", true);
            } else {
                dropItems(player, npc, item, false);
                return true;
            }
        }
        return false;
    }

    /**
     * Drops an item.
     *
     * @param player The player.
     * @param npc the NPC.
     * @param loot The item.
     * @param underPlayer If under the player.
     */
    private static void dropItems(Player player, NPC npc, Item loot, boolean underPlayer) {
        player.catchDrop(loot, () -> World.updateGroundItem(loot, new WorldTile(underPlayer ? player : new WorldTile(npc.getCoordFaceX(npc.getSize()), npc.getCoordFaceY(npc.getSize()), npc.getId() == 22001 ? 0 : npc.getPlane())), player, 60, 0, false));
    }
}