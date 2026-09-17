package com.rs.game.player.controllers.bossInstance;

import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.map.bossInstance.BossInstance;
import com.rs.game.map.bossInstance.BossInstanceHandler.Boss;
import com.rs.game.player.dialogue.Dialogue;

public class PZInstanceController extends BossInstanceController {
    @Override
    public boolean login() {
        Boss boss = Boss.PZ_BOSS;
        if (boss != null)
            player.setNextWorldTile(new WorldTile(boss.getOutsideTile()));
        removeControler();
        return false;
    }



    @Override
    public boolean logout() {
        if (getInstance() == null) {
            removeControler();
            Boss boss = Boss.PZ_BOSS;
            if (boss != null)
                player.setLocation(new WorldTile(boss.getOutsideTile()));
            return true;
        }
        getInstance().leaveInstance(player, BossInstance.LOGGED_OUT);
        return false;
    }
    
    @Override
    public boolean processObjectClick1(WorldObject object) {
        if (object.getId() == 4389) {
            sendLeaveConfirmation();
            return false;
        }
        return false;
    }
    
    public void sendLeaveConfirmation() {
        player.getDialogueManager().startDialogue(new Dialogue() {
            @Override
            public void start() {
                if (!getInstance().getSettings().hasTimeRemaining()) {
                    sendDialogue("This instance has no time remaining, Would you like to leave?");
                    return;
                }
                sendDialogue("Are you sure you want to leave this instance" + "?", "<col=ff0000>If you leave this instance it will be removed.");
            }

            @Override
            public void run(int interfaceId, int componentId) {
                switch (stage) {
                case -1:
                    stage = 0;
                    sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Yes i want to leave.", "Nervermind.");
                    break;
                case 0:
                    if (componentId == OPTION_1) {
                        removeControler();
                        getInstance().leaveInstance(player, BossInstance.EXITED);
                    }
                    end();
                    break;
                }
            }

            @Override
            public void finish() {
            }
        });
    }
}
