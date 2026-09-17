package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.game.item.Item;

public class ItemCreationD extends Dialogue {

    int ingredient;
    int special;
    int reward;
    String name;

    @Override
    public void start() {
        ingredient = (Integer) parameters[0];
        special = (Integer) parameters[1];
        reward = (Integer) parameters[2];
        name = (String) parameters[3];
        sendItemDialogue(reward, 1, "You will be sacrificing both of these items to create a " + Colors.SHAD + Colors.SALMON + name + "!");
        stage = 0; // normal transition
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
        case 0:
            sendOptionsDialogue(Colors.SALMON + "Do you want to make a " + name + "?</col>", "Yes", "No");
            stage = 1;
            break;
        case 1:
            finish();
            if (componentId == OPTION_1) {
                player.getInventory().deleteItem(ingredient, 1);
                player.getInventory().deleteItem(special, 1);
                Item rewardItem = new Item(reward, 1);
                rewardItem = player.getChargesManagerNew().createDegradeableItem(rewardItem, ingredient, special);
                player.getInventory().addItem(rewardItem);
                player.sendMessage(Colors.SALMON + Colors.SHAD + "You have created a " + name + "!", false);
            }
            break;
        }
    }

    @Override
    public void finish() {
        player.getInterfaceManager().closeChatBoxInterface();
    }

}
