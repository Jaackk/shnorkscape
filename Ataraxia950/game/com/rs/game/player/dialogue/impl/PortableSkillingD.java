package com.rs.game.player.dialogue.impl;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

public final class PortableSkillingD extends Dialogue {

    private int itemId;
    private String itemName;

    @Override
    public void start() {
        sendOptionsDialogue("Select an option.",
                "Claim portable range",
                "Claim portable well",
                "Claim portable brazier",
                "Claim portable fletcher",
                "Claim portable crafter");
        stage = 0;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        if (stage == 0) {
            if (componentId == OPTION_1) {
                itemId = 31042;
            } else if (componentId == OPTION_2) {
                itemId = 31044;
            } else if (componentId == OPTION_3) {
                itemId = 35228;
            } else if (componentId == OPTION_4) {
                itemId = 35227;
            } else if (componentId == OPTION_5) {
                itemId = 35226;
            }
            itemName = ItemDefinitions.getName(itemId);
            sendDialogue("Are you sure you would like to claim a " + Colors.DARK_RED + itemName + "</col>?");
            stage = 1;
        } else if (stage == 1) {
            sendOptionsDialogue("Select an option.", "Yes", "No");
            stage = 2;
        } else if (stage == 2) {
            stage = -1;
            if (componentId == OPTION_1) {
                if (player.getInventory().containsItem(34026, 1)) {
                    player.getInventory().deleteItem(34026, 1);
                    player.getInventory().addItem(itemId, 1);
                    sendItemDialogue(itemId, 1, "You have claimed a " + itemName + "!");
                } else {
                    sendDialogue("Portable skilling pack could not be found. Please try again.");
                }
            } else if (componentId == OPTION_2) {
                end();
            }
        } else {
            end();
        }
    }

    @Override
    public void finish() {

    }
}
