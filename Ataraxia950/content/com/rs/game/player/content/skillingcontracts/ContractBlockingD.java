package com.rs.game.player.content.skillingcontracts;

import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class ContractBlockingD extends Dialogue {

    BlockLevel selected;
    boolean highLevel;
    int npc;

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
                    if (selected == null) {
                        loadMainMenu();
                        return;
                    }
                    sendNPCDialogue(npc, ANGRY, "This will cost you " + selected.points + " Skilling tickets.",
                            "Are you sure you would like to purchase this?");
                    stage = 1;
                } else if (componentId == OPTION_2) {
                    player.getDialogueManager().startDialogue("SpendPointsD", highLevel);
                }
                break;
            case 1:
                sendOptionsDialogue("Select an option.", "Yes", "No");
                stage = 2;
                break;
            case 2:
                if (componentId == OPTION_1) {
                    if (player.getContracts().getTotalTickets() < selected.points) {
                        sendNPCDialogue(npc, ANGRY, "Are you kidding me? You don't even have enough Skilling tickets for this!");
                    } else {
                        player.getContracts().currentBlock = selected;
                        player.getContracts().removeTickets(selected.points);
                        sendNPCDialogue(npc, ANGRY, "Done. Are you finished bothering me now?");
                    }
                    stage = -1;
                } else if (componentId == OPTION_2) {
                    loadMainMenu();
                }
                break;
        }
    }

    @Override
    public void finish() {
        selected = null;
        player.getInterfaceManager().closeChatBoxInterface();
    }

    private BlockLevel getNextLevel() {
        BlockLevel currentBlock = player.getContracts().currentBlock;
        if (currentBlock == null) {
            return BlockLevel.T30;
        }
        switch (currentBlock) {
            case T30:
                return BlockLevel.T40;
            case T40:
                return BlockLevel.T50;
            case T50:
                return BlockLevel.T60;
            case T60:
                return BlockLevel.T70;
            case T70:
                return null;
            default:
                throw new IllegalStateException("Invalid block level " + currentBlock);
        }
    }

    private void loadMainMenu() {
        selected = getNextLevel();
        sendOptionsDialogue("Select an option.",
                selected == null ? Colors.RED + "You have already reached the maximum threshold of 7 blocks." :
                        selected.description,
                "Nevermind");
        stage = 0;
    }
}