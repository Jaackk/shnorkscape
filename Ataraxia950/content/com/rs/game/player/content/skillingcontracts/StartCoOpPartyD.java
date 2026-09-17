package com.rs.game.player.content.skillingcontracts;

import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class StartCoOpPartyD extends Dialogue {

    Player partner;
    boolean partnerAdvanced;
    boolean advanced;
    CoOpRequest pending;
    boolean finished;

    @Override
    public void start() {
        partner = (Player) parameters[0];
        partnerAdvanced = (boolean) parameters[1];
        advanced = (boolean) parameters[2];
        sendMenu();
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -1:
                sendMenu();
                break;
            case 0:
                sendOptionsDialogue("Select an option.",
                        Colors.GREEN + "Regular",
                        (advanced && partnerAdvanced ? Colors.GREEN : Colors.RED) + "Advanced (2x rewards)");
                stage = 1;
                break;
            case 1:
                pending = new CoOpRequest(partner);
                if (componentId == OPTION_1) {
                    pending.advanced = false;
                } else if (componentId == OPTION_2) {
                    if (!advanced || !partnerAdvanced) {
                        sendDialogue("You and your requested partner must be able to talk to the advanced skilling master to select this option.");
                        stage = -1;
                        break;
                    }
                    pending.advanced = true;
                }
                sendDialogue("How long would you like the contract to be?");
                stage = 2;
                break;
            case 2:
                sendOptionsDialogue("Select an option.",
                        "Short",
                        "Long (2x longer, 4x rewards)");
                stage = 3;
                break;
            case 3:
                if (componentId == OPTION_1) {
                    pending.isShort = true;
                } else if (componentId == OPTION_2) {
                    pending.isShort = false;
                }
                player.coOpRequest = pending;
                finished = true;
                sendDialogue("Your co-op skilling request has been sent to " + Colors.RED + partner.getDisplayName() + "</col>.",
                        "They can accept it through the dialogue or by using their skilling backpack/gem on you.",
                        "This request will expire after 2 minutes.");
                pending.start();
                partner.getWalkSteps().clear();
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        partner.getWalkSteps().clear();
                        partner.getDialogueManager().startDialogue("AcceptCoOpPartyD", player);
                    }
                }, 1);
                stage = 4;
                break;
            case 4:
                end();
                break;
        }
    }

    @Override
    public void finish() {
        if (!finished) {
            player.getContracts().coOpMessage("The request was discarded. Send it before you close the dialogue.");
            player.coOpRequest = null;
        }
    }

    private void sendMenu() {
        sendDialogue("What type of co-op skilling contract would you like?");
        stage = 0;
    }
}