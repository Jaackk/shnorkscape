package com.rs.game.player.content.skillingcontracts;

import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;


/**
 * @author lare96 <http://github.com/lare96>
 */
public final class AcceptCoOpPartyD extends Dialogue {

    Player requester;
    boolean advanced;
    boolean isShort;

    @Override
    public void start() {
        requester = (Player) parameters[0];
        advanced = requester.coOpRequest.advanced;
        isShort = requester.coOpRequest.isShort;
        String type = advanced ? "an " + Colors.RED + "Advanced" : "a " + Colors.RED + "Regular";
        String length = isShort ? "Short" : "Long";
        String joined = type + " (" + length + ")</col>";
        sendDialogue(Colors.RED + requester.getDisplayName() + "</col> has sent you ", joined, " co-op skilling request.",
                "Would you like to join their party?");
        player.getContracts().coOpMessage(Colors.RED+requester.getDisplayName(), " has sent you ", joined, " co-op skilling request. Use your backpack on them to accept it.");
        stage = 0;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -1:
                end();
                break;
            case 0:
                sendOptionsDialogue("Select an option.", "Yes", "No");
                stage = 1;
                break;
            case 1:
                if (componentId == OPTION_1) {
                    if (!requester.isActive()) {
                        sendDialogue("The request has expired because " + requester.getDisplayName() + " has disconnected.");
                    } else if (requester.coOpRequest.isExpired()) {
                        sendDialogue("The request has expired. Don't wait too long before accepting!");
                    } else {
                        requester.getContracts().assignCoOpContract();
                        end();
                    }
                } else if (componentId == OPTION_2) {
                    requester.getInterfaceManager().closeChatBoxInterface();
                    requester.getContracts().coOpMessage(player.getDisplayName() + " has declined your co-op skilling request.");
                    player.getContracts().coOpMessage("You have declined " + requester.getDisplayName() + "'s co-op skilling request.");
                    requester.coOpRequest = null;
                    end();
                }
                stage = -1;
                break;
        }
    }

    @Override
    public void finish() {

    }
}