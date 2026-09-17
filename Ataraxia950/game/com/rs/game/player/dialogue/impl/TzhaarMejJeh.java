package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;

public class TzhaarMejJeh extends Dialogue {

    @Override
    public void start() {
        sendOptionsDialogue("What would you like to do?",
                "Create enhanced fire cape",
                "Gamble capes",
                "Nevermind");
        stage = 0;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 0:
                switch (componentId) {
                    case OPTION_1:
                        /** Enhanced firecape */
                        if (player.getInventory().containsItem(23659, 1)
                                && player.getInventory().containsItem(31610, 1)
                                && player.getInventory().containsItem(31611, 1)) {
                            player.getInventory().deleteItem(31610, 1);
                            player.getInventory().deleteItem(31611, 1);
                            player.getInventory().deleteItem(23659, 1);
                            player.getInventory().addItem(31603, 1);
                            sendItemDialogue(31603, 1, "You have forged an enhanced fire cape!");
                            player.setUnlockedEFC(true);
                        } else
                            sendNPCDialogue(15161, NORMAL, "You need all 3 kiln capes to create this cape!");
                        stage = 1;
                        break;
                    case OPTION_2:
                        player.getDialogueManager().startDialogue(new GambleCapeD());
                        break;
                    case OPTION_3:
                        finish();
                        break;
                }
                break;
            case 1:
                finish();
                break;
        }
    }

    @Override
    public void finish() {
        player.getInterfaceManager().closeChatBoxInterface();
    }

}
