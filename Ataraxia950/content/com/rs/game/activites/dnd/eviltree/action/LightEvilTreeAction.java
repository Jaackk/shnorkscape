package com.rs.game.activites.dnd.eviltree.action;

import java.util.concurrent.ThreadLocalRandom;

import com.rs.game.Animation;
import com.rs.game.activites.dnd.eviltree.EvilTree;
import com.rs.game.activites.dnd.eviltree.entity.EvilTreeObject;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.utils.Utils;

/**
 * @author lare96 <http://github.com/lare96>
 */
public class LightEvilTreeAction extends Action {

    private final EvilTree tree;
    private final EvilTreeObject treeObject;

    public LightEvilTreeAction(EvilTree tree, EvilTreeObject treeObject) {
        this.tree = tree;
        this.treeObject = treeObject;
    }

    @Override
    public boolean process(Player player) {
        player.setNextAnimation(new Animation(16700));
        boolean req = !treeObject.isDead() && checkReqs(player);
        if (req) {
            if (ThreadLocalRandom.current().nextInt(tree.getType().attackRate - 2) == 0) {
                tree.doRandomAttack(player);
            }
            return true;
        }
        return false;
    }

    @Override
    public int processWithDelay(Player player) {
        double xp = tree.getType().burnXp;
        if (player.getPerkManager().hasPerkActive(DonationPerk.TREE_HUNTER)) {
            xp *= 1.25;
        }
        player.getSkills().addXp(Skills.FIREMAKING, xp);
        player.getInventory().deleteItem(14666, 1);
        player.sendFilteredMessage("You light the base of the tree.");
        treeObject.registerLightAction(player);
        if (player.naturesEssenceDelay > 0) {
            return ThreadLocalRandom.current().nextBoolean() ? 1 : 2;
        }
        return Utils.random(3, 6);
    }

    @Override
    public boolean start(Player player) {
        return checkReqs(player);
    }

    @Override
    public void stop(Player player) {
        player.setNextAnimation(new Animation(-1));
    }

    private boolean checkReqs(Player player) {
        int reqLevel = tree.getType().firemakingLevel;
        if (treeObject.isDead()) {
            return false;
        }
        if (player.getSkills().getLevel(Skills.FIREMAKING) < reqLevel) {
            player.sendMessage("You need a Firemaking level of " + reqLevel + " to light this.");
            return false;
        }
        if (!player.getInventory().containsItem(14666, 1)) {
            player.sendMessage("You need Evil root kindling in order to light this.");
            return false;
        }
        if (!player.getInventory().containsItem(590, 1) && !player.getToolBelt().contains(590)) {
            player.sendMessage("You need a tinderbox in order to light this.");
            return false;
        }
        return player.withinDistance(treeObject, treeObject.getDefinitions().sizeX * treeObject.getDefinitions().sizeY);
    }
}