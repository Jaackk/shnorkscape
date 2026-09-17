package com.rs.game.player.controllers.bossInstance;

import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.Animation;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.map.bossInstance.BossInstance;
import com.rs.game.map.bossInstance.BossInstanceHandler.Boss;
import com.rs.game.map.bossInstance.impl.TheMagisterInstance;
import com.rs.game.npc.themagister.TheMagister;
import com.rs.game.player.actions.slayer.sophanemdungeon.SophanemSlayerDungeon;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import java.util.concurrent.TimeUnit;

public class TheMagisterInstanceController extends BossInstanceController {

    @Override
    public boolean login() {
        Boss boss = Boss.THE_MAGISTER;
        if (boss != null)
            player.setNextWorldTile(new WorldTile(boss.getOutsideTile()));
        removeControler();
        player.getControlerManager().startControler(SophanemSlayerDungeon.class.getSimpleName());
        return false;
    }

    @Override
    public boolean checkWalkStep(int lastX, int lastY, int nextX, int nextY) {
        TheMagister theMagister = (TheMagister) player.getTemporaryAttributtes().get("SoulSiphon");
        if (theMagister != null && !new WorldTile(nextX, nextY, player.getPlane()).withinDistance(theMagister, 7)) {
            player.getTemporaryAttributtes().remove("SoulSiphon");
            theMagister.setNextAnimation(new Animation(-1));
            player.getPackets().sendGameMessage("You run away from the Magister's life draining attack.");
        }
        return super.checkWalkStep(lastX, lastY, nextX, nextY);
    }

    @Override
    public boolean logout() {
        getTheMagisterInstance().leaveInstance(player, BossInstance.LOGGED_OUT);
        return false;
    }

    @Override
    public boolean processMagicTeleport(WorldTile toTile) {
        TheMagister theMagister = getTheMagisterInstance().getTheMagister();
        if (theMagister != null && !theMagister.hasFinished()) {
            player.getDialogueManager().startDialogue("SimpleMessage", "You can't leave just like that!");
            return false;
        }
        removeControler();
        getTheMagisterInstance().leaveInstance(player, BossInstance.TELEPORTED);
        return true;
    }

    @Override
    public boolean processItemTeleport(WorldTile toTile) {
        TheMagister theMagister = getTheMagisterInstance().getTheMagister();
        if (theMagister != null && !theMagister.hasFinished()) {
            player.getDialogueManager().startDialogue("SimpleMessage", "You can't leave just like that!");
            return false;
        }
        removeControler();
        getTheMagisterInstance().leaveInstance(player, BossInstance.TELEPORTED);
        return true;
    }

    @Override
    public boolean processObjectTeleport(WorldTile toTile) {
        TheMagister theMagister = getTheMagisterInstance().getTheMagister();
        if (theMagister != null && !theMagister.hasFinished()) {
            player.getDialogueManager().startDialogue("SimpleMessage", "You can't leave just like that!");
            return false;
        }
        removeControler();
        getTheMagisterInstance().leaveInstance(player, BossInstance.TELEPORTED);
        return true;
    }

    @Override
    public boolean processObjectClick1(WorldObject object) {
        if (object.getId() == 109499) {
            sendLeaveConfirmation();
            return false;
        }
        if (object.getId() == 109157) {
            if (!hasKeystone(false)) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You need a Key to the Crossing to start the battle.");
                return false;
            }
            if (getTheMagisterInstance().getTheMagister() != null && getTheMagisterInstance().getTheMagister().isDieing())
                return false;
            sendSpawnTheMagisterConfirmation();
            return false;
        }
        if (object.getId() == 109154) {
            player.setNextAnimation(new Animation(423));
            World.removeObject(object);
            TheMagister theMagister = getTheMagisterInstance().getTheMagister();
            boolean indirection = theMagister != null && !theMagister.hasFinished() && !theMagister.isDead() && Utils.isInFaceDirection(player, theMagister);
            if (indirection) {
                World.sendProjectileCycles(object, theMagister, 6717, 5, 40, 30, 40, 5, 0);
                long projectileCycles = 600;
                CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
                    @Override
                    public boolean repeat() {
                        try {
                            int damage = 400;
                            theMagister.applyHit(new Hit(theMagister, damage, HitLook.UNBLOCKABLE_REGULAR_DAMAGE));
                            return false;
                        } catch (Exception e) {
                            Logger.getGlobal().catching(e);
                            return false;
                        }
                    }
                }, projectileCycles, 600, TimeUnit.MILLISECONDS);
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean processObjectClick2(WorldObject object) {
        if (object.getId() == 109499) {
            removeControler();
            getTheMagisterInstance().leaveInstance(player, BossInstance.EXITED);
            return false;
        }
        return true;
    }

    public TheMagisterInstance getTheMagisterInstance() {
        return (TheMagisterInstance) getInstance();
    }

    public void sendSpawnTheMagisterConfirmation() {
        player.getDialogueManager().startDialogue(new Dialogue() {

            @Override
            public void start() {
                sendOptionsDialogue("USE A KEY TO THE CROSSING?", "Yes.", "No.");
            }

            @Override
            public void run(int interfaceId, int componentId) {
                end();
                if (componentId == OPTION_1) {
                    if (!hasKeystone(false))
                        return;
                    hasKeystone(true);
                }
            }

            @Override
            public void finish() {
            }
        });
    }

    public void sendLeaveConfirmation() {
        player.getDialogueManager().startDialogue(new Dialogue() {
            @Override
            public void start() {
                if (!getTheMagisterInstance().getSettings().hasTimeRemaining()) {
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
                        getTheMagisterInstance().leaveInstance(player, BossInstance.EXITED);
                        player.getControlerManager().startControler(SophanemSlayerDungeon.class.getSimpleName());
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

    private boolean hasKeystone(boolean remove) {
        if (remove) {
            if (getTheMagisterInstance().getTheMagister() == null)
                getTheMagisterInstance().startBattle();
            else
                getTheMagisterInstance().getTheMagister().respawn();
            return player.getInventory().deleteOneItem(new Item(40310));
        }
        return player.getInventory().containsOneItem(40310);
    }

}
