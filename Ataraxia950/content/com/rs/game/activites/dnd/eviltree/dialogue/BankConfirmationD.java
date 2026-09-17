package com.rs.game.activites.dnd.eviltree.dialogue;

import com.rs.game.activites.dnd.eviltree.EvilTree;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.player.PerkManager.DonationPerk;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class BankConfirmationD extends Dialogue {

    private static final int BANK_COST = 30;

    private final boolean redirect;
    private final EvilTree tree;

    public BankConfirmationD(EvilTree tree, boolean redirect) {
        this.tree = tree;
        this.redirect = redirect;
    }

    @Override
    public void start() {
        if (tree == null || tree.getTreeHunterNpc() == null) {
            end();
            return;
        }
        if (player.getPerkManager().hasPerkActive(DonationPerk.TREE_HUNTER) ||
                tree.getTreeHunterNpc().getPaidForBank().contains(player.getUsername())) {
            player.getBank().openBank();
        } else {
            sendNPCDialogue(13790, NORMAL, "That's a lot of work. How 'bout you give me " + BANK_COST + " Skilling tickets for my troubles?",
                    "I won't charge ya again until this tree is gone.");
            stage = 0;
        }
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -1:
                if (redirect) {
                    player.getDialogueManager().startDialogue(new EvilTreeHunterD(tree));
                } else {
                    end();
                }
                break;
            case 0:
                sendOptionsDialogue("Select an option.",
                        "Yes",
                        "No");
                stage = 1;
                break;
            case 1:
                if (componentId == OPTION_1) {
                    if (player.getContracts().removeTickets(BANK_COST)) {
                        tree.getTreeHunterNpc().getPaidForBank().add(player.getUsername());
                        player.getBank().openBank();
                    } else {
                        sendNPCDialogue(13790, NORMAL, "You don't have enough Skilling tickets.");
                        stage = -1;
                    }
                } else if (componentId == OPTION_2) {
                    sendNPCDialogue(13790, NORMAL, "That's alright. I'll still note items free of charge.");
                    stage = 2;
                }
                break;
            case 2:
                end();
                break;
        }
    }

    @Override
    public void finish() {

    }
}