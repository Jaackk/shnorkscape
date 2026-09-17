package com.rs.game.activites.dnd.eviltree.dialogue;

import com.rs.game.activites.dnd.eviltree.EvilTree;
import com.rs.game.activites.dnd.eviltree.EvilTreeHandler;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.content.Magic;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Utils;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class TeleportConfirmationD extends Dialogue {
    private static final int TELEPORT_TICKETS_PRICE = 15;
    private static final int TELEPORT_COINS_PRICE = 500_000;

    private final boolean redirect;
    private final EvilTree tree;

    public TeleportConfirmationD(boolean redirect) {
        this.redirect = redirect;
        tree = EvilTreeHandler.current();
    }

    @Override
    public void start() {
        if (tree == null || !tree.isAlive()) {
            sendNPCDialogue(13790, NORMAL, "I'm afraid there's no Evil Tree to hunt at the moment.");
            stage = -1;
        } else if (player.getPerkManager().hasPerkActive(DonationPerk.TREE_HUNTER)) {
            Magic.sendObjectTeleportSpell(player, false, tree.getTreeHunterNpc().transform(-1, 0, 0));
            end();
        } else {
            sendNPCDialogue(13790, NORMAL, "You ain't special, so I'm afraid it's gonna cost ya " + TELEPORT_TICKETS_PRICE + " Skilling tickets or " + Utils.formatNumber(TELEPORT_COINS_PRICE) + " coins.",
                    "That okay?");
            stage = 0;
        }
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -1:
                if (redirect) {
                    player.getDialogueManager().startDialogue(new EvilTreeHunterD(tree != null && tree.isInstanced() ? tree : null));
                } else {
                    end();
                }
                break;
            case 0:
                sendOptionsDialogue("Select an option.",
                        "Yes, I'll pay with coins",
                        "Yes, I'll pay with tickets",
                        "Nevermind");
                stage = 1;
                break;
            case 1:
                if (componentId == OPTION_1) {
                    if (tree == null || !tree.isAlive()) {
                        sendNPCDialogue(13790, NORMAL, "I'm afraid there's no Evil Tree to hunt at the moment.");
                        stage = -1;
                        return;
                    } else if (player.removeMoney(TELEPORT_COINS_PRICE)) {
                        teleport();
                    } else {
                        sendNPCDialogue(13790, NORMAL, "You don't have enough coins. Sorry!");
                        stage = -1;
                    }

                } else if (componentId == OPTION_2) {
                    if (tree == null || !tree.isAlive()) {
                        sendNPCDialogue(13790, NORMAL, "I'm afraid there's no Evil Tree to hunt at the moment.");
                        stage = -1;
                        return;
                    } else if (player.getContracts().removeTickets(TELEPORT_TICKETS_PRICE)) {
                        teleport();
                    } else {
                        sendNPCDialogue(13790, NORMAL, "You don't have enough tickets. Sorry!");
                        stage = -1;
                    }
                } else if (componentId == OPTION_3) {
                    sendNPCDialogue(13790, NORMAL, "Fair enough. Have fun walkin'!");
                    stage = -1;
                }
                break;
        }
    }

    @Override
    public void finish() {

    }

    private void teleport() {
        player.sendMessage("You have been teleported directly to the Evil Tree.");
        Magic.sendObjectTeleportSpell(player, false, tree.getTreeHunterNpc().transform(-1, 0, 0));
        end();
    }
}