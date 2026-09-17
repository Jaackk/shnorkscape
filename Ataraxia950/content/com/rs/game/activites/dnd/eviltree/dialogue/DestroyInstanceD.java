package com.rs.game.activites.dnd.eviltree.dialogue;

import com.rs.game.activites.dnd.eviltree.EvilTreeInstance;
import com.rs.game.map.bossInstance.BossInstance;
import com.rs.game.map.bossInstance.BossInstanceHandler;
import com.rs.game.map.bossInstance.BossInstanceHandler.Boss;
import com.rs.game.player.dialogue.Dialogue;

/**
 * @author lare96 <http://github.com/lare96>
 */
public class DestroyInstanceD extends Dialogue {

    @Override
    public void start() {
        BossInstance instance = BossInstanceHandler.findInstance(Boss.Evil_Tree, player.getUsername());
        if (instance == null) {
            sendDialogue("You don't have an Evil Tree instance.");
        } else {
            sendDialogue("Are you sure you want to destroy your Evil Tree instance?");
            stage = 0;
        }
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 0:
                sendOptionsDialogue("Select an option.", "Yes", "No");
                stage = 1;
                break;
            case 1:
                EvilTreeInstance instance = (EvilTreeInstance) BossInstanceHandler.findInstance(Boss.Evil_Tree, player.getUsername());
                if (componentId == OPTION_1 && instance != null) {
                    instance.destroy("The instance has been destroyed by the owner.");
                }
                end();
                break;
        }
    }

    @Override
    public void finish() {

    }
}