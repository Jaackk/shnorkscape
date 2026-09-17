package com.rs.game.activites.dnd.eviltree.dialogue;

import com.rs.game.activites.dnd.eviltree.EvilTree;
import com.rs.game.activites.dnd.eviltree.EvilTreeHandler;
import com.rs.game.player.dialogue.Dialogue;

/**
 * @author lare96 <http://github.com/lare96>
 */
public class EvilTreeTeleportD extends Dialogue {
    @Override
    public void start() {
        if (EvilTreeHandler.isAlive()) {
            sendDialogue("Would you like to teleport to the Evil Tree?");
            stage = 0;
        } else {
            sendDialogue("There is no Evil Tree to teleport to.");
            stage = -1;
        }
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
                    if(!player.getInventory().containsItem(40986, 1)) {
                        end();
                        return;
                    }
                    player.getInventory().deleteItem(40986, 1);
                    if (!EvilTreeHandler.isAlive()) {
                        sendDialogue("There is no Evil Tree to teleport to.");
                        stage = -1;
                        return;
                    } else {
                        EvilTree.curseTeleport(player, EvilTreeHandler.current()
                                .getTreeHunterNpc().transform(-1, 0, 0));
                    }
                }
                end();
                break;
        }
    }

    @Override
    public void finish() {

    }
}