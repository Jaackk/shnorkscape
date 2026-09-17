package com.rs.game.activites.dnd.eviltree.action;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.WorldObject;
import com.rs.game.activites.dnd.eviltree.EvilTree;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.actions.woodcutting.AxeDef;

import java.util.concurrent.ThreadLocalRandom;

/**
 * A base implementation for chopping parts of an {@link EvilTree}.
 *
 * @author lare96 <http://github.com/lare96>
 */
public abstract class ChopAction extends Action {

    final EvilTree tree;
    AxeDef axe;
    private final int rateMin;
    private final int rateMax;

    ChopAction(EvilTree tree, int rateMin, int rateMax) {
        this.tree = tree;
        this.rateMin = rateMin;
        this.rateMax = rateMax;
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
        boolean req = checkInvSpace(player) && checkReq(player) && onProcess(player);
        if (req) {
            if (ThreadLocalRandom.current().nextInt(tree.getType().attackRate) == 0) {
                tree.doRandomAttack(player);
            }
            return true;
        }
        return false;
    }

    @Override
    public int processWithDelay(Player player) {
        if (onChop(player)) {
            if (player.naturesEssenceDelay > 0) {
                return 1;
            }
            return ThreadLocalRandom.current().nextInt(rateMin, rateMax + 1);
        }
        return -1;
    }

    @Override
    public boolean start(Player player) {
        if (tree.isTree() && !tree.getTreeObject().isDead()) {
            axe = AxeDef.get(player);
            if (axe == null) {
                player.sendMessage("You need a hatchet in order to cut this.");
                return false;
            }
            if (checkReq(player)) {
                return checkInvSpace(player) && onStart(player);
            }
        }
        return false;
    }

    @Override
    public void stop(Player player) {
        player.setNextAnimation(new Animation(-1));
    }
    public abstract WorldObject target();

    private boolean checkReq(Player player) {
        int reqLvl = tree.getType().woodcuttingLevel;
        if (player.getSkills().getLevel(Skills.WOODCUTTING) < reqLvl) {
            player.sendMessage("You need a Woodcutting level of " + reqLvl + " to cut this.");
            return false;
        }
        WorldObject target = target();
        return player.withinDistance(target, target.getDefinitions().sizeX * target.getDefinitions().sizeY);
    }

    private boolean checkInvSpace(Player player) {
        if (!player.getInventory().hasFreeSlots()) {
            player.sendMessage("You do not have enough space in your inventory.");
            return false;
        }
        return true;

    }

    public boolean onProcess(Player player) {
        return true;
    }

    public boolean onStart(Player player) {
        return true;
    }

    public abstract boolean onChop(Player player);
}