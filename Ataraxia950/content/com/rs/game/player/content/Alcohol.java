package com.rs.game.player.content;

import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import lombok.val;

/**
 * Gets a Player drunk.
 * Created by Arham 4 on 7/7/14.
 */
class Alcohol {

    /**
     * Can the Player drink?
     *
     * @param item The item being drunk.
     * @return If the Player can drink.
     */
    static boolean canDrink(final Item item) {
        val itemName = item.getName().toLowerCase();
        return itemName.contains("beer") || itemName.contains("vodka") || itemName.contains("wine") || itemName.contains("ale") || itemName.contains("mind bomb");
    }

    /**
     * Intakes the alcohol.
     *
     * @param player The Player drinking.
     */
    static void intake(final Player player) {
        player.setAlcoholIntake(player.getAlcoholIntake() + 1);
        if (player.getAlcoholIntake() >= 3) {
            player.getAppearence().setRenderEmote(290);
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    player.setAlcoholIntake(0);
                    player.getAppearence().setRenderEmote(-1);
                    this.stop();
                }
            }, 1000);
        }
    }
}