package com.rs.game.activites.quest.root_of_evil;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.WorldObject;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.actions.woodcutting.AxeDef;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import lombok.val;

import java.util.concurrent.ThreadLocalRandom;

public final class ChopCarcassAction extends Action {

    AxeDef axe;
    private final VaultController controller;
    private final WorldObject target;

    public ChopCarcassAction(VaultController controller, WorldObject target) {
        this.controller = controller;
        this.target = target;
    }

    @Override
    public boolean process(Player player) {
        if (player.getAnimations().hasRoundHouseWc && player.getAnimations().roundHouseWc) {
            player.setNextAnimation(new Animation(17304));
            player.setNextGraphics(new Graphics(3301));
        } else if (player.getAnimations().hasStrongWc && player.getAnimations().strongWc) {
            player.setNextAnimation(new Animation(20302));
            player.setNextGraphics(new Graphics(4006));
        } else if (player.getAnimations().hasExplosiveWc && player.getAnimations().explosiveWc) {
            player.setNextAnimation(new Animation(17948));
            player.setNextGraphics(new Graphics(3457));
        } else if (player.getAnimations().hasSingerWc && player.getAnimations().singerWc) {
            player.setNextAnimation(new Animation(24621));
            player.setNextGraphics(new Graphics(5157));
        } else if (player.getAnimations().hasLumberjackWc && player.getAnimations().lumberjackWc) {
            player.setNextAnimation(new Animation(axe.specialEmote));
        } else {
            player.setNextAnimation(new Animation(axe.emote));
        }
        return checkInvSpace(player) && checkReq(player);
    }

    @Override
    public int processWithDelay(Player player) {
        if (onChop(player)) {
            return ThreadLocalRandom.current().nextInt(4, 8);
        }
        return -1;
    }

    @Override
    public boolean start(Player player) {
        if (checkReq(player) && checkInvSpace(player)) {
            axe = AxeDef.get(player);
            if (axe == null) {
                axe = AxeDef.BRONZE;
            }
            return true;
        }
        return false;
    }

    @Override
    public void stop(Player player) {
        player.setNextAnimation(new Animation(-1));
    }

    private boolean checkReq(Player player) {
        if (controller.vaultStage != 3) {
            player.sendMessage("I've already loaded enough logs into the generator.");
            return false;
        }
        int amount = player.getInventory().getAmountOf(RootOfEvil.EVIL_LOGS_ID);
        if (amount + controller.logsLoaded >= RootOfEvil.EVIL_LOGS_REQ) {
            player.lock();
            player.getHintIconsManager().removeUnsavedHintIcon();
            player.resetWalkSteps();
            WorldTasksManager.schedule(new WorldTask() {
                boolean looped = false;

                @Override
                public void run() {
                    if (looped) {
                        player.unlock();
                        stop();
                        return;
                    }
                    if (amount > 0) {
                        Dialogue.sendSinglePlayerDialogue(player, Dialogue.NORMAL, "I have " + RootOfEvil.EVIL_LOGS_REQ + " logs now. I should load them into the generator.");
                        val hintIconPos = controller.instance.getInstanceTile(2390, 9826, 0);
                        player.getHintIconsManager().addHintIcon(hintIconPos.getX(), hintIconPos.getY(), hintIconPos.getPlane(), 85, 5, 0, -1, false);
                    } else {
                        val hintIconPos = controller.instance.getInstanceTile(2391, 9826, 0);
                        player.getHintIconsManager().addHintIcon(hintIconPos.getX(), hintIconPos.getY(), hintIconPos.getPlane(), 85, 5, 0, -1, false);
                        Dialogue.sendSinglePlayerDialogue(player, Dialogue.NORMAL, "The generator is active. I should program it now.");
                    }
                    looped = true;
                }
            }, 1, 1);
            return false;
        }
        if (player.getSkills().getLevel(Skills.WOODCUTTING) < RootOfEvil.WOODCUTTING_REQ) {
            player.sendMessage("You need a Woodcutting level of " + RootOfEvil.WOODCUTTING_REQ + " to cut this.");
            return false;
        }
        return player.withinDistance(target, target.getDefinitions().sizeX * target.getDefinitions().sizeY);
    }

    private boolean checkInvSpace(Player player) {
        if (!player.getInventory().hasFreeSlots()) {
            player.sendMessage("You do not have enough space in your inventory.");
            return false;
        }
        return true;

    }

    private boolean onChop(Player player) {
        player.getInventory().addItem(RootOfEvil.EVIL_LOGS_ID, 1);
        player.getSkills().addXp(Skills.WOODCUTTING, 2500);
        player.sendMessage("You get some evil logs.");
        return true;
    }
}
