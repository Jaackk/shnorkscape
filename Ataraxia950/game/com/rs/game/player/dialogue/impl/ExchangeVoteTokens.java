package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;

/**
 * @author Noele
 * see https://noeles.life || noele@zenyte.com
 */
public class ExchangeVoteTokens extends Dialogue {

    private int amount;

    @Override
    public void start() {
        amount = (Integer) parameters[0];

        sendItemDialogue(41418, 100, "Would you like to exchange these vote tokens for vote points?");
        stage = 0;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch(stage) {
            case 0:
                sendOptionsDialogue("Exchange "+amount+" vote tokens?", "Yes", "No");
                stage = 1;
                break;

            case 1:
                switch(componentId) {

                    case OPTION_1:
                        player.getInventory().deleteItem(41418, amount);
                        player.setVotePoints(player.getVotePoints() + amount);
                        player.sendMessage("You have redeemed "+amount+" vote tokens for vote points!", false);
                        break;
                }


                finish();
                break;
        }
    }

    @Override
    public void finish() {
        player.getInterfaceManager().closeChatBoxInterface();
    }
}
