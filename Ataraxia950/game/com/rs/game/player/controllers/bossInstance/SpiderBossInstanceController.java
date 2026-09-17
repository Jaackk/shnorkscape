package com.rs.game.player.controllers.bossInstance;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceMovement;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.map.bossInstance.BossInstance;
import com.rs.game.map.bossInstance.BossInstanceHandler.Boss;
import com.rs.game.map.bossInstance.impl.SpiderBossInstance;
import com.rs.game.npc.NPC;
import com.rs.game.npc.spiderboss.Araxxor;
import com.rs.game.npc.spiderboss.AraxxorMinion;
import com.rs.game.player.content.InterfaceManager;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class SpiderBossInstanceController extends BossInstanceController {
    private long ticksDelay;
    private boolean sentInterfaces;
    private boolean sentErrorMessage;
    private long spidersDelay;

    @Override
    public void start() {
        ticksDelay = Utils.currentTimeMillis() + 600;
        spidersDelay = Utils.currentTimeMillis() + 40000;
        player.getInterfaceManager().sendOverlay(1515, true);
        player.getPackets().sendExecuteScript(10094, 0);
        super.start();
    }

    @Override
    public void process() {
        if (spidersDelay != 0 && Utils.currentTimeMillis() >= spidersDelay) {
            int spiderType = Utils.random(4);
            int moveRot = Utils.random(4);
            int dir = Utils.random(2);
            player.getPackets().sendExecuteScript(10099, moveRot, dir, spiderType);
            spidersDelay = Utils.currentTimeMillis() + 40000;
        }
        if (!sentInterfaces && getSpiderBossInstance().playerIsInsideBattle(player) && getSpiderBossInstance().getAraxxor() != null && !getSpiderBossInstance().getAraxxor().hasFinished())
            sendInterfaces();
        super.process();
    }

    @Override
    public boolean keepCombating(boolean mainHand, Entity target) {
        if (player.getTemporaryAttributtes().get("cocoonAttacks") != null) {
            decreaseCocoonAttacks();
            return false;
        }
        if (player.getTemporaryAttributtes().get("undercutscene") != null)
            return false;
        Long cantMoveDelay = (Long) player.getTemporaryAttributtes().get("cantMove");
        if (cantMoveDelay == null)
            return true;
        if (cantMoveDelay > Utils.currentTimeMillis())
            return false;
        if (player.getTemporaryAttributtes().get("cantMove") != null) {
            player.resetWalkSteps();
            player.getTemporaryAttributtes().remove("cantMove");
        }
        return true;
    }

    @Override
    public boolean processButtonClick(int interfaceId, int componentId, int slotId, int slotId2, int packetId) {
        if (interfaceId == 1515) {
            if (componentId >= 32 && componentId <= 35) {
                player.getTemporaryAttributtes().put("DodgeEmote", componentId - 32);
                return false;
            }
        }
        if (interfaceId == 1284) {
            getSpiderBossInstance().HandleButtons(player, interfaceId, componentId, slotId, slotId2, packetId);
            return false;
        }
        return true;
    }

    @Override
    public boolean processKeyPress(int keyId) {
        return true;
    }

    private void decreaseCocoonAttacks() {
        if (ticksDelay > Utils.currentTimeMillis() || player.getTemporaryAttributtes().get("cocoonAttacks") == null)
            return;
        int amount = (int) player.getTemporaryAttributtes().get("cocoonAttacks");
        player.setNextAnimation(new Animation(amount - 1 < 0 ? -1 : 24115));
        player.getTemporaryAttributtes().put("cocoonAttacks", amount - 1);
        if (amount - 1 < 0) {
            player.getTemporaryAttributtes().put("cantMove", Utils.currentTimeMillis() + (long) 1000);
            player.getTemporaryAttributtes().remove("cocoonAttacks");
            player.resetWalkSteps();
            player.getAppearence().transformIntoNPC(-1);
            player.getAppearence().setRenderEmote(-1);
            getSpiderBossInstance().sendMessage(player, "You successfully struggle free from the cocoon.");
            return;
        }
        ticksDelay = Utils.currentTimeMillis() + 500;
    }

    @Override
    public boolean canHit(Entity entity) {
        if (player.getTemporaryAttributtes().get("cocoonAttacks") != null) {
            decreaseCocoonAttacks();
            return false;
        }
        if (player.getTemporaryAttributtes().get("undercutscene") != null)
            return false;
        Long cantMoveDelay = (Long) player.getTemporaryAttributtes().get("cantMove");
        if (cantMoveDelay == null)
            return true;
        if (cantMoveDelay > Utils.currentTimeMillis())
            return false;
        if (player.getTemporaryAttributtes().get("cantMove") != null) {
            player.resetWalkSteps();
            player.getTemporaryAttributtes().remove("cantMove");
        }
        return super.canHit(entity);
    }

    @Override
    public void processIngoingHit(Hit hit) {
        if (player.getTemporaryAttributtes().get("undercutscene") != null) {
            if (hit == null || hit.getLook() == null || hit.getLook() != HitLook.UNBLOCKABLE_REGULAR_DAMAGE)
                return;
        }
        super.processIngoingHit(hit);
    }

    @Override
    public boolean checkWalkStep(int lastX, int lastY, int nextX, int nextY) {
        if (getSpiderBossInstance() == null)
            return true;
        Araxxor spider = getSpiderBossInstance().getAraxxor();
        if (spider != null && !spider.hasFinished())
            spider.checkPhaseChangeByWorldTile(lastX, lastY);
        return super.checkWalkStep(lastX, lastY, nextX, nextY);
    }

    @Override
    public boolean canMove(int dir) {
        if (player.getTemporaryAttributtes().get("cocoonAttacks") != null) {
            decreaseCocoonAttacks();
            return false;
        }
        if (player.getTemporaryAttributtes().get("undercutscene") != null)
            return false;
        Long cantMoveDelay = (Long) player.getTemporaryAttributtes().get("cantMove");
        if (cantMoveDelay == null)
            return super.canMove(dir);
        if (cantMoveDelay > Utils.currentTimeMillis())
            return false;
        if (player.getTemporaryAttributtes().get("cantMove") != null) {
            player.resetWalkSteps();
            player.getTemporaryAttributtes().remove("cantMove");
        }
        return super.canMove(dir);
    }

    @Override
    public boolean canDoAnimation(Animation animation) {
        if ((player.getAppearence().getTransformedNpcId() != -1 || player.getTemporaryAttributtes().get("cocoonAttacks") != null) && animation.getIds()[0] != 24115)
            return false;
        if (player.getTemporaryAttributtes().get("undercutscene") != null) {
            int[] playerEmotes = { 24100, 24112, 24102, 24114, 11785 };
            int animationId = animation.getIds()[0];
            for (int emote : playerEmotes)
                if (animationId == emote)
                    return true;
            return false;
        }
        Long cantMoveDelay = (Long) player.getTemporaryAttributtes().get("cantMove");
        if (cantMoveDelay == null)
            return true;
        if (cantMoveDelay > Utils.currentTimeMillis())
            return false;
        if (player.getTemporaryAttributtes().get("cantMove") != null) {
            player.resetWalkSteps();
            player.getTemporaryAttributtes().remove("cantMove");
        }
        return super.canDoAnimation(animation);
    }

    @Override
    public void sendInterfaces() {
        sentInterfaces = true;
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                player.getPackets().sendHideIComponent(1515, 14, false);
                player.getPackets().closeInterface(InterfaceManager.getComponentUId(1515, 14));
                player.getInterfaceManager().setInterface(true, 1515, 14, 1648);
                getSpiderBossInstance().updateInterface(player, false);
            }
        }, 2);
    }

    @Override
    public boolean login() {
        Boss boss = Boss.Spider_Boss;
        if (boss != null)
            player.setNextWorldTile(new WorldTile(boss.getOutsideTile()));
        removeControler();
        player.getControlerManager().startControler("AraxxorHiveControler");
        World.checkControlersAtMove(player);
        return false;
    }

    @Override
    public boolean processNPCClick1(NPC npc) {
        if (getSpiderBossInstance() == null)
            return true;
        Araxxor spider = getSpiderBossInstance().getAraxxor();
        if (spider != null && !spider.hasFinished() && npc.getId() == 19471) {
            player.lock();
            player.setNextAnimation(new Animation(18130));
            WorldTasksManager.schedule(new WorldTask() {

                @Override
                public void run() {
                    player.unlock();
                    ((AraxxorMinion) npc).setFollowTarget(player);
                }
            }, 1);
            return false;
        }
        return true;
    }

    @Override
    public boolean logout() {
        getSpiderBossInstance().leaveInstance(player, BossInstance.LOGGED_OUT);
        removeControler();
        return true;
    }

    @Override
    public boolean processMagicTeleport(WorldTile toTile) {
        if (getSpiderBossInstance().playerIsInsideBattle(player)) {
            player.getDialogueManager().startDialogue("SimpleMessage", "You can't leave just like that!");
            return false;
        }
        getSpiderBossInstance().leaveInstance(player, BossInstance.TELEPORTED);
        return true;
    }

    @Override
    public boolean processItemTeleport(WorldTile toTile) {
        if (getSpiderBossInstance().playerIsInsideBattle(player)) {
            player.getDialogueManager().startDialogue("SimpleMessage", "You can't leave just like that!");
            return false;
        }
        getSpiderBossInstance().leaveInstance(player, BossInstance.TELEPORTED);
        return true;
    }

    @Override
    public boolean processObjectTeleport(WorldTile toTile) {
        if (getSpiderBossInstance().playerIsInsideBattle(player)) {
            player.getDialogueManager().startDialogue("SimpleMessage", "You can't leave just like that!");
            return false;
        }
        getSpiderBossInstance().leaveInstance(player, BossInstance.TELEPORTED);
        return true;
    }

    @Override
    public boolean processObjectClick1(WorldObject object) {
        SpiderBossInstance instance = getSpiderBossInstance();
        if (object.getId() == 91500) {
            instance.enterFightRoom(player);
            return false;
        }
        if (object.getId() == 91673) {
            getSpiderBossInstance().openRewardChest(player);
            return false;
        }

        for (int i = 0; i < SpiderBossInstance.PATH_SPECIFIC_OBJECT_IDS.length; i++) {
            if (object.getId() == SpiderBossInstance.PATH_SPECIFIC_OBJECT_IDS[i][2]) {
                if (instance.getAraxxor() == null || instance.getAraxxor().getBurnWebDelay() > Utils.currentTimeMillis()) {
                    if (!sentErrorMessage) {
                        getSpiderBossInstance().sendMessage(player, "You can't do that yet.");
                        sentErrorMessage = true;
                    }
                    return false;
                }
                instance.getAraxxor().choosePath(i);
                return false;
            }
        }
        if (object.getId() == SpiderBossInstance.BROKEN_RAMP_ID) {
            if (instance.getAraxxor() == null || instance.getAraxxor().getTemporaryAttributtes().get("changingPhase") != null) {
                return false;
            }
            player.lock();
            player.setForceNextMapLoadRefresh(true);
            player.loadMapRegions();
            player.resetWalkSteps();
            WorldTile toLocation = instance.getTile(new WorldTile(4560, 6264, 1));
            player.setNextFaceWorldTile(toLocation);
            WorldTasksManager.schedule(new WorldTask() {
                int loop = 0;

                @Override
                public void run() {
                    if (loop == 0) {
                        player.setNextAnimation(new Animation(2588));
                        player.setNextForceMovement(new ForceMovement(toLocation, 1, ForceMovement.EAST));
                    } else if (loop == 1) {
                        player.unlock();
                        player.setNextWorldTile(toLocation);
                        player.loadMapRegions();
                        stop();
                    }
                    loop++;
                }
            }, 0, 0);
            return false;
        }
        return true;
    }

    @Override
    public boolean processObjectClick2(WorldObject object) {
        if (object.getId() == 91673) {
            player.getDialogueManager().startDialogue(new Dialogue() {

                @Override
                public void start() {
                    sendOptionsDialogue("Are you ready to leave?", "Leave.", "Stay here.");
                }

                @Override
                public void run(int interfaceId, int componentId) {
                    switch (stage) {
                    case -1:
                        end();
                        if (componentId == OPTION_1) {
                            player.lock();
                            player.setNextAnimation(new Animation(3254));
                            player.setNextGraphics(new Graphics(5009));
                            WorldTasksManager.schedule(new WorldTask() {
                                @Override
                                public void run() {
                                    player.setNextAnimation(new Animation(3255));
                                    player.unlock();
                                    removeControler();
                                    getSpiderBossInstance().leaveInstance(player, BossInstance.EXITED);
                                }
                            }, 2);
                        }
                        break;
                    }
                }

                @Override
                public void finish() {

                }
            });
            return false;
        }
        return true;
    }

    @Override
    public boolean sendDeath() {
        player.getTemporaryAttributtes().remove("cantMove");
        player.getTemporaryAttributtes().remove("cocoonAttacks");
        player.getAppearence().transformIntoNPC(-1);
        player.getAppearence().setRenderEmote(-1);
        return super.sendDeath();
    }

    public SpiderBossInstance getSpiderBossInstance() {
        return (SpiderBossInstance) getInstance();
    }

    @Override
    public void processIncommingHit(Hit hit, Entity target) {
//        hit.setDamage(5000);
        super.processIncommingHit(hit, target);
    }
    
    
}
