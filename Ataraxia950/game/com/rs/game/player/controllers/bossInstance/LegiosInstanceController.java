package com.rs.game.player.controllers.bossInstance;

import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.map.bossInstance.BossInstance;
import com.rs.game.map.bossInstance.BossInstanceHandler.Boss;
import com.rs.game.map.bossInstance.impl.LegiosInstance;
import com.rs.game.player.dialogue.Dialogue;

public class LegiosInstanceController extends BossInstanceController {

    @Override
    public boolean processObjectClick1(WorldObject object) {
        if (object.getId() >= 84732 && object.getId() <= 84737) {
            sendLeaveConfirmation();
            return false;
        }
        return true;
    }

    @Override
    public boolean processObjectClick2(WorldObject object) {
        if (object.getId() >= 84732 && object.getId() <= 84737) {
            removeControler();
            getLegiosInstance().leaveInstance(player, BossInstance.EXITED);
            return false;
        }
        return true;
    }

    public LegiosInstance getLegiosInstance() {
        return (LegiosInstance) getInstance();
    }

    @Override
    public boolean login() {
        Boss boss = getArguments()[0] != null && getArguments()[0] instanceof Boss ? (Boss) getArguments()[0] : getLegiosInstance() != null && getLegiosInstance().getBoss() != null ? getLegiosInstance().getBoss() : null;
        if (boss != null)
            player.setNextWorldTile(new WorldTile(boss.getOutsideTile()));
        removeControler();
        return false;
    }

    @Override
    public boolean logout() {
        getLegiosInstance().leaveInstance(player, BossInstance.LOGGED_OUT);
        removeControler();
        return false;
    }

    public void sendLeaveConfirmation() {
        player.getDialogueManager().startDialogue(new Dialogue() {
            @Override
            public void start() {
                if (!getLegiosInstance().getSettings().hasTimeRemaining()) {
                    sendDialogue("This instance has no time remaining, Would you like to leave?");
                    return;
                }
                if (getLegiosInstance().getLegio() != null && (getLegiosInstance().getLegio().hasFinished() || getLegiosInstance().getLegio().isDead()) && hasKeystone(false)) {
                    stage = 1;
                    sendOptionsDialogue("YOU HAVE ANOTHER KEYSTONE FOR THIS ROOM.<br>DO YOU WISH TO RESTART?", "Yes.", "No, I want to leave.");
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
                        getLegiosInstance().leaveInstance(player, BossInstance.EXITED);
                    }
                    end();
                    break;
                case 1:
                    end();
                    if (componentId == OPTION_1) {
                        hasKeystone(true);
                        return;
                    }
                    removeControler();
                    getLegiosInstance().leaveInstance(player, BossInstance.EXITED);
                    break;
                }
            }

            @Override
            public void finish() {
            }
        });
    }

    private boolean hasKeystone(boolean remove) {
        int bossIndex = getLegiosInstance().getLegioId() - 17149;
        int keyId = 28445 + (bossIndex * 2);
        if (remove) {
            getLegiosInstance().getLegio().respawn();
            return player.getInventory().deleteOneItem(new Item(keyId));
        }
        return player.getInventory().containsOneItem(keyId);
    }
}