package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.data.parsers.misc.ShopsDataParser;

public class DonatorZoneNpcD extends Dialogue {

    private int amountDonated;

    @Override
    public void start() {
        amountDonated = player.getMoneySpent();
        sendMainMenu();
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -1:
                switch (componentId) {
                    case OPTION_1:
                    sendOptionsDialogue("Select a shop to view.", Colors.wrap(Colors.check(amountDonated >= 20), "Bronze"), Colors.wrap(Colors.check(amountDonated >= 250), "Platinum"), "Back...");
                    stage++;
                    break;
                }
                break;
            case 0:
                switch (componentId) {
                    case OPTION_1:
                    case OPTION_2:
                        if (canViewShop(componentId)) {
                            ShopsDataParser.openShop(player, componentId == OPTION_1 ? 159 : 169);
                        } else {
                            player.sendMessage("You need to be a " + (componentId == OPTION_1 ? "Bronze" : "Platinum") + " donator to view this shop.");
                        }
                        end();
                        break;
                    case OPTION_3:
                        sendMainMenu();
                        break;
                }
                break;
            case 1:

                break;
            case 2:

                break;
            case 3:

                break;
            case 4:

                break;
        }
    }

    @Override
    public void finish() {

    }

    private boolean canViewShop(int componentId) {
        switch (componentId) {
            case OPTION_1:
                return amountDonated >= 20;

            case OPTION_2:
                return amountDonated >= 250;
        }
        return false;
    }

    private void sendMainMenu() {
        sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Donator shops", "2", "3", "4", "5");
        stage = -1;
    }

}
