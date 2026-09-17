package com.rs.game.player.controllers.bossInstance;

import com.rs.game.EffectsManager.EffectType;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.TemporaryAtributtes.Key;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.map.bossInstance.BossInstance;
import com.rs.game.map.bossInstance.BossInstanceHandler.Boss;
import com.rs.game.map.bossInstance.impl.TelosInstance;
import com.rs.game.npc.NPC;
import com.rs.game.npc.telos.Font;
import com.rs.game.npc.telos.Telos;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

public class TelosInstanceController extends BossInstanceController {

    @Override
    public boolean processObjectClick1(final WorldObject object) {
        if (object.getId() == 103521) {// TODO missing animation
            player.setNextWorldTile(getTelosInstance().getTile(TelosInstance.PLAYER_START_TILE[0]));
            return false;
        } else if (object.getId() == 103568) {
            if (player.getCurrentTelosReward() == null) {
                removeControler();
                getInstance().leaveInstance(player, BossInstance.EXITED);
                return false;
            }
            player.openTelosTrove();
            return false;
        } else if (object.getId() == 103520) {
            sendLeaveConfirmation();
            return false;
        }
        return true;
    }

    @Override
    public boolean checkWalkStep(int lastX, int lastY, int nextX, int nextY) {
        return getTelosInstance().getTelos().getTemporaryAttributtes().get("GripAttack") == null;
    }

    @Override
    public boolean processNPCClick1(NPC npc) {
        if (npc instanceof Font) {
            Font font = (Font) npc;
            if (font.getCharge() >= 2) {
                if (getTelosInstance().getTelos().getAttackRotation() != 3) {
                    player.getPackets().sendGameMessage("You need to wait untill telos uses his anima bomb attack.");
                    return false;
                }
                getTelosInstance().getTelos().stun(font);
                return false;
            }
            player.getPackets().sendGameMessage("Charge:" + (font.getCharge() * 50) + "%");
            return false;
        }
        return true;
    }

    @Override
    public boolean processObjectClick2(final WorldObject object) {
        if (object.getId() == 103568) {
            removeControler();
            getInstance().leaveInstance(player, BossInstance.EXITED);
            return false;
        }
        return object.getId() != 103521;
    }

    @Override
    public void processIngoingHit(Hit hit) {
        if (getTelosInstance().getTelos().nullDamages())
            hit.setDamage(0);
        if (player.getEffectsManager().hasActiveEffect(EffectType.BLACK_STREAM)) {
            hit.setDamage((int) (hit.getDamage() * 0.7));
        }
        if (player.getEffectsManager().hasActiveEffect(EffectType.RED_STREAM))
            hit.setDamage((int) (hit.getDamage() * 1.3));
        super.processIngoingHit(hit);
    }

    @Override
    public void processIncommingHit(Hit hit, Entity target) {
        if (getTelosInstance().getTelos().nullDamages())
            hit.setDamage(0);
        if (player.getEffectsManager().hasActiveEffect(EffectType.BLACK_STREAM)) {
            hit.setDamage((int) (hit.getDamage() * 0.7));
        }
        if (player.getEffectsManager().hasActiveEffect(EffectType.RED_STREAM))
            hit.setDamage(((int) (hit.getDamage() * 1.3) > getTelosInstance().getTelos().getCapDamage()) ? getTelosInstance().getTelos().getCapDamage() : (int) (hit.getDamage() * 1.3));
        super.processIncommingHit(hit, target);
    }

    public TelosInstance getTelosInstance() {
        return (TelosInstance) getInstance();
    }

    @Override
    public boolean login() {
        removeControler();
        player.getTemporaryAttributtes().put(Key.FORCE_REMOVE_EFFECT_1, Boolean.TRUE);
        player.getEffectsManager().removeEffect(EffectType.GREEN_VIRUS);
        player.getTemporaryAttributtes().put(Key.FORCE_REMOVE_EFFECT_1, Boolean.TRUE);
        player.getEffectsManager().removeEffect(EffectType.BLACK_VIRUS);
        player.getTemporaryAttributtes().put(Key.FORCE_REMOVE_EFFECT_1, Boolean.TRUE);
        player.getEffectsManager().removeEffect(EffectType.RED_VIRUS);
        player.getTemporaryAttributtes().remove(Key.FORCE_REMOVE_EFFECT_1);
        player.getEffectsManager().removeEffect(EffectType.GREEN_STREAM);
        player.getEffectsManager().removeEffect(EffectType.BLACK_STREAM);
        player.getEffectsManager().removeEffect(EffectType.RED_STREAM);
        player.setNextWorldTile(new WorldTile(Boss.Telos.getOutsideTile()));
        return false;
    }

    @Override
    public boolean logout() {
        getInstance().leaveInstance(player, BossInstance.LOGGED_OUT);
        removeControler();
        return false;
    }

    @Override
    public void moved() {
        Telos telos = getTelosInstance().getTelos();
        if (telos == null)
            return;
        if (player.withinDistance(telos.getMiddleWorldTile(), 7)) {// hmm maybe the issue is that they are not aggressive in the defs>?
            telos.startFight();
        }
    }

    @Override
    public void sendInterfaces() {
        getTelosInstance().getTelos().updateInterface();
        player.getInterfaceManager().sendOverlay(1648, true);
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                player.getInterfaceManager().setInterface(true, 1648, 25, 1770);
                player.getPackets().sendHideIComponent(1648, 10, getTelosInstance().getTelos().getPhase() == 4);
            }

        }, 10);
    }

    @Override
    public boolean sendDeath() {
        if (getTelosInstance().getTelos() != null && getTelosInstance().getTelos().getId() != 22892)
            player.setTelosStreak(0);
        return super.sendDeath();
    }

    @Override
    public boolean processMagicTeleport(WorldTile toTile) {
        if (getTelosInstance().getTelos() != null && getTelosInstance().getTelos().getId() == 22892)
            return true;
        player.getDialogueManager().startDialogue("SimpleMessage", "You can't leave just like that!");
        return false;
    }

    @Override
    public boolean processItemTeleport(WorldTile toTile) {
        if (getTelosInstance().getTelos() != null && getTelosInstance().getTelos().getId() == 22892)
            return true;
        player.getDialogueManager().startDialogue("SimpleMessage", "You can't leave just like that!");
        return false;
    }

    @Override
    public boolean processObjectTeleport(WorldTile toTile) {
        if (getTelosInstance().getTelos() != null && getTelosInstance().getTelos().getId() == 22892)
            return true;
        player.getDialogueManager().startDialogue("SimpleMessage", "You can't leave just like that!");
        return false;
    }

    public void sendLeaveConfirmation() {
        player.getDialogueManager().startDialogue(new Dialogue() {
            @Override
            public void start() {
                sendDialogue("Are you sure you want to leave this instance?", "<col=ff0000>If you leave this instance it will be removed.");
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
