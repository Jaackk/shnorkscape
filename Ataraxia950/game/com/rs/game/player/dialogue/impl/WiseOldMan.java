package com.rs.game.player.dialogue.impl;

import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.data.parsers.misc.ShopsDataParser;

/**
 * Class used to handle the WiseOldMan dialogue.
 *
 * @author Noel
 */
public class WiseOldMan extends Dialogue {
    private final int npcId = 2253;
    private boolean isGim;

    @Override
    public void start() {
        isGim = player.isGroupIronman();
        sendOptionsDialogue("Choose an Option", "Trivia point exchange", "Skill capes store", "Skill hoods store", "Mastery capes store", isGim ? "Diamond/Platinum donor outfits" : "Claim diamond outfit");
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -3:
                end();
                break;
			case -2:
				if(componentId == OPTION_1) {
					sendClaimOutfit();
				} else if(componentId == OPTION_2) {
				    if(!player.isPlatinumDonor()) {
                        sendNPCDialogue(npcId, 9827, "Only platinum donators can purchase this.");
                        stage = -3;
                    } else {
                        ShopsDataParser.openShop(player, 67);
                    }
				}
				break;
            case -1:
                switch (componentId) {
                    case OPTION_1:
                        finish();
                        ShopsDataParser.openShop(player, 46);
                        break;
                    case OPTION_2:
                        sendOptionsDialogue("Which skillcapes would you like?", "RS3 Capes", "Retro Capes");
                        stage = 0;
                        break;
                    case OPTION_3:
                        sendOptionsDialogue("Which skill hoods would you like?", "RS3 Hoods", "Retro hooded capes");
                        stage = 1;
                        break;
                    case OPTION_4:
                        finish();
                        ShopsDataParser.openShop(player, 50);
                        break;
                    case OPTION_5:
                        if (isGim) {
                            sendOptionsDialogue("Select an option",
                                    "Claim diamond skilling outfit",
                                    "Purchase platinum skele outfit");
                            stage = -2;
                        } else {
                            sendClaimOutfit();
                        }
                        break;
                }
                break;
            case 0:
                switch (componentId) {
                    case OPTION_1:
                        finish();
                        ShopsDataParser.openShop(player, 48);
                        break;
                    case OPTION_2:
                        finish();
                        ShopsDataParser.openShop(player, 61);
                        break;
                }
                break;
            case 1:
                switch (componentId) {
                    case OPTION_1:
                        finish();
                        ShopsDataParser.openShop(player, 49);
                        break;
                    case OPTION_2:
                        finish();
                        ShopsDataParser.openShop(player, 62);
                        break;
                }
                break;
            case 2:
                sendNPCDialogue(npcId, 9827, "Let me just see if you are worthy.. This Skilling set doesn't come cheap you know?");
                stage = 3;
                break;
            case 3:
                finish();
                giveOutfit(player);
                break;
        }
    }

    private void sendClaimOutfit() {
        sendPlayerDialogue(9827, "I would like to claim my diamond donor outfit.");
        stage = 2;
    }

    private static void giveOutfit(final Player player) {

        if (player.isDiamondDonor()) {
            player.addItem(new Item(39870, 1));
            player.addItem(new Item(39872, 1));
            player.addItem(new Item(39874, 1));
            player.addItem(new Item(39876, 1));
            player.addItem(new Item(39878, 1));
            return;
        }
        if (player.getMoneySpent() < 500) {
            player.getDialogueManager().startDialogue("SimpleMessage", "You have to be a diamond donator to claim this outfit!");
            return;
        }
    }

    @Override
    public void finish() {
        player.getInterfaceManager().closeChatBoxInterface();
    }

}