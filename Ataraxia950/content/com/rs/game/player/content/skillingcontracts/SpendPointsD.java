package com.rs.game.player.content.skillingcontracts;

import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class SpendPointsD extends Dialogue {

    boolean highLevel;
    int npc;
    int skipCost;

    @Override
    public void start() {
        highLevel = (boolean) parameters[0];
        npc = highLevel ? 219 : 943;
        loadMainMenu();
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -1:
                loadMainMenu();
                break;
            case 0:
                if (componentId == OPTION_1) {
                    player.getDialogueManager().startDialogue("ContractBlockingD", highLevel);
                } else if (componentId == OPTION_2) {
                    player.getDialogueManager().startDialogue("BlockContractD", highLevel);
                } else if (componentId == OPTION_3) {
                    player.getDialogueManager().startDialogue("TempEffectsD");
                } else if (componentId == OPTION_4) {
                    AssignedSkillingContract current = player.getContracts().current;
                    if (current != null) {
                        current = player.getContracts().fixInvalidContract();
                        if (current.getContract() == null) {
                            end();
                            return;
                        }
                        skipCost = player.getContracts().getSkipCost(current.getContract());
                        sendNPCDialogue(npc, ANGRY, "This will cost you " + skipCost + " Skilling tickets.",
                                "Are you sure you would like to purchase this?");
                        stage = 1;
                    } else {
                        sendNPCDialogue(npc, ANGRY, "You don't have any contract to reset, moron!");
                        stage = -1;
                    }
                }
                break;
            case 1:
                sendOptionsDialogue("Select an option.", "Yes", "No");
                stage = 2;
                break;
            case 2:
                if (componentId == OPTION_1) {
                    stage = -1;
                    AssignedSkillingContract current = player.getContracts().current;
                    if (current == null || player.getContracts().current.getContract() == null) {
                        sendNPCDialogue(npc, ANGRY, "You don't have any contract to reset, moron!");
                        player.getContracts().current = null;
                        return;
                    }
                    if (player.getContracts().getTotalTickets() < skipCost) {
                        sendNPCDialogue(npc, ANGRY, "Are you kidding me? You don't even have enough Skilling tickets for this!");
                    } else {
                        if (player.oldTomeActivated) {
                            player.oldTomeActivated = false;
                            player.sendMessage(Colors.RED + "You have lost your blessing from the old tome.");
                        }
                        player.getContracts().removeTickets(skipCost);
                        player.getContracts().resetContract();
                        player.skippedLastContract = true;
                        sendNPCDialogue(npc, ANGRY, "Done. What else do you want from me?");
                    }
                } else if (componentId == OPTION_2) {
                    loadMainMenu();
                }
                break;
            case 3:
                end();
                break;
        }
    }

    @Override
    public void finish() {
        player.getInterfaceManager().closeChatBoxInterface();
    }

    public void loadMainMenu() {
        if (player.getContracts().hasCoOpContract()) {
            sendOptionsDialogue("Select an option.",
                    "I would like to raise my contract blocking limit.",
                    "I would like to block contracts.",
                    "I would like to purchase temporary effects.",
                    "Please reset my current co-op contract.");
        } else if (player.getContracts().hasContract()) {
            sendOptionsDialogue("Select an option.",
                    "I would like to raise my contract blocking limit.",
                    "I would like to block contracts.",
                    "I would like to purchase temporary effects.",
                    "Please reset my current contract.");
        } else {
            sendOptionsDialogue("Select an option.",
                    "I would like to raise my contract blocking limit.",
                    "I would like to block contracts.",
                    "I would like to purchase temporary effects.");
        }
        stage = 0;
    }
}