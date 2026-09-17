package com.rs.game.activites.gambling;

import com.rs.game.activites.gambling.flowerpoker.FlowerPokerSession;
import com.rs.game.activites.gambling.flowerpoker.session.FlowerPokerGame;
import com.rs.game.player.Player;
import com.rs.game.player.content.RouteEvent;
import com.rs.game.player.dialogue.Dialogue;

public class GambleWithDialogue extends Dialogue {
    private Player target;

    @Override
    public void start() {
        target = (Player) parameters[0];
        sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Dice", "Flower poker");
        stage = 0;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 0:
                switch (componentId) {
                    case OPTION_1:
                        sendDialogue("Speak to Hops to dice with another player.");
                        stage = -1;
                        break;
                    case OPTION_2:
                        if (canFlowerPoker()) {
                            if (hasTargetAlreadySentRequestToPlayer()) {
                                GamblingAreaController.handleFlowerPokerRequest(player, target);
                            } else {
                                sendDialogue("Sent " + target.getDisplayName() + " a request to play flower poker.");
                                player.setRouteEvent(new RouteEvent(target, this::sendFlowerPokerRequest));
                            }
                        } else {
                            sendDialogue("There are currently too many flower poker games running right now. Please try again momentarily.");
                        }
                        stage = -1;
                        break;
                }
                break;
            case -1:
                end();
                break;
        }
    }

    private boolean canFlowerPoker() {
        return FlowerPokerGame.getActiveGames() < FlowerPokerGame.MAX_CAPACITY_GAMES;
    }

    private void sendFlowerPokerRequest() {
        target.getPackets().sendMessage(100, "wishes to play flower poker with you.", player);
        player.getTemporaryAttributtes().put(FlowerPokerSession.FLOWER_POKER_REQUEST_TARGET_KEY, target);
    }

    private boolean hasTargetAlreadySentRequestToPlayer() {
        Player targetTempAttribute = (Player) target.getTemporaryAttributtes().get(FlowerPokerSession.FLOWER_POKER_REQUEST_TARGET_KEY);
        return targetTempAttribute == player;
    }

    @Override
    public void finish() {

    }
}
