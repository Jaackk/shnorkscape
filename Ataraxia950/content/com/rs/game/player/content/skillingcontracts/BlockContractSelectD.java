package com.rs.game.player.content.skillingcontracts;

import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;


/**
 * @author lare96 <http://github.com/lare96>
 */
public final class BlockContractSelectD extends Dialogue {
    int skill;
    int selected;
    SkillingContract contract;
    boolean highLevel;
    int npc;

    @Override
    public void start() {
        skill = (int) parameters[0];
        selected = (int) parameters[1];
        highLevel = (boolean) parameters[2];
        npc = highLevel ? 219 : 943;
        contract = SkillingContractManager.lookup(skill, selected);
        if (contract == null) {
            sendNPCDialogue(npc, ANGRY, "You've entered an invalid identifier! Try again.");
            stage = 4;
        } else if (player.getContracts().getBlocks().containsEntry(skill, selected)) {
            sendNPCDialogue(npc, ANGRY, "Are you sure you want to " + Colors.GREEN + "unblock</col>",
                    "'" + contract.description.replace(" <amount> ", " ") + "'",
                    "for " + SkillingContractManager.BLOCK_COST + " Skilling tickets?");
            stage = 0;
        } else {
            sendNPCDialogue(npc, ANGRY, "Are you sure you want to " + Colors.RED + "block</col>",
                    "'" + contract.description.replace(" <amount> ", " ") + "'",
                    "for " + SkillingContractManager.BLOCK_COST + " Skilling tickets?");
            stage = 1;
        }
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 0:
                sendOptionsDialogue("Select an option.", "Yes", "No");
                stage = 2;
                break;
            case 1:
                sendOptionsDialogue("Select an option.", "Yes", "No");
                stage = 3;
                break;
            case 2:
                if (componentId == OPTION_1) {
                    if (player.getContracts().getTotalTickets() < SkillingContractManager.BLOCK_COST) {
                        sendNPCDialogue(npc, ANGRY, "You don't have enough tickets for this.");
                    } else if (player.getContracts().getBlocks().remove(skill, selected)) {
                        sendNPCDialogue(npc, ANGRY, "Done. What else do you want?");
                        player.sendMessage("You have " + Colors.GREEN + "unblocked</col> '" + contract.description + "'.");
                        player.getContracts().removeTickets(SkillingContractManager.BLOCK_COST);
                    }
                    stage = 4;
                } else if (componentId == OPTION_2) {
                    player.getDialogueManager().startDialogue("BlockContractD", highLevel);
                }
                break;
            case 3:
                if (componentId == OPTION_1) {
                    if (player.getContracts().getTotalTickets() < SkillingContractManager.BLOCK_COST) {
                        sendNPCDialogue(npc, ANGRY, "You don't have enough Skilling tickets for this.");
                    } else if (player.getContracts().getBlocks().size() >= player.getContracts().getCurrentBlock().level) {
                        sendNPCDialogue(npc, ANGRY, "You have reached your maximum number of blocks.");
                    } else if (player.getContracts().getBlocks().put(skill, selected)) {
                        sendNPCDialogue(npc, ANGRY, "Done. What else do you want?");
                        player.sendMessage("You have " + Colors.RED + "blocked</col> '" + contract.description + "'.");
                        player.getContracts().removeTickets(SkillingContractManager.BLOCK_COST);
                    }
                    stage = 4;
                } else if (componentId == OPTION_2) {
                    player.getDialogueManager().startDialogue("BlockContractD", highLevel);
                }
                break;
            case 4:
                player.getDialogueManager().startDialogue("BlockContractD", highLevel);
                break;
        }
    }

    @Override
    public void finish() {

    }
}