package com.rs.game.activites.dnd.eviltree.action;

import java.util.concurrent.ThreadLocalRandom;

import com.rs.game.Animation;
import com.rs.game.activites.dnd.eviltree.EvilTree;
import com.rs.game.activites.dnd.eviltree.EvilTreeType;
import com.rs.game.activites.dnd.eviltree.entity.EvilSaplingObject;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.utils.Colors;

import lombok.val;

/**
 * @author lare96 <http://github.com/lare96>
 */
public class NurtureEvilSaplingAction extends Action {
    private static final int EVIL_HERB_ID = 24783;
    public static final int EVIL_SEED_ID = 24778;

    private final EvilTree tree;
    private final EvilSaplingObject sapling;
    private int actionsLeft;

    public NurtureEvilSaplingAction(EvilTree tree, EvilSaplingObject sapling) {
        this.tree = tree;
        this.sapling = sapling;
        actionsLeft = ThreadLocalRandom.current().nextInt(3, 15);
    }

    @Override
    public boolean process(Player player) {
        return canNurture(player);
    }

    @Override
    public int processWithDelay(Player player) {
        EvilTreeType treeType = tree.getType();
        double chance = treeType.getHarvestChance(player);
        double xp;
        if (!sapling.isNurtured()) {
            chance *= 0.35;
            sapling.setNurtured(true);
            val taskInfo = tree.getGrowthTask().getTaskInfo();
            int halved = taskInfo.getRemainingTicks() / 2;
            if (halved < 1) {
                halved = 1;
            }
            taskInfo.setRemainingTicks(halved);
            player.sendMessage("The sapling now seems to have all the nutrients it needs to grow faster!");
            xp = treeType.nurtureXp * 10;
        } else {
            chance *= 0.90;
            player.sendFilteredMessage("You nurture the sapling.");
            xp = treeType.nurtureXp;
        }
        if (player.getPerkManager().hasPerkActive(DonationPerk.TREE_HUNTER)) {
            xp *= 1.25;
            chance *= 0.75;
        }
        if (chance < 2) {
            chance = 2;
        }
        if (ThreadLocalRandom.current().nextInt((int) (chance * 100)) == 0) {
            player.getInventory().addItem(EVIL_SEED_ID, 1);
            player.sendMessage(Colors.RED + "You find an evil seed.");
        } else if (ThreadLocalRandom.current().nextInt((int) chance) == 0) {
            player.getInventory().addItem(EVIL_HERB_ID, 1);
            player.sendMessage(Colors.RED + "You get an evil herb.");
        }
        player.setNextAnimation(new Animation(2295));
        player.getSkills().addXp(Skills.FARMING, xp);
        if (--actionsLeft <= 0) {
            stun(player);
            player.getActionManager().forceStop();
        }
        return 4;
    }

    @Override
    public boolean start(Player player) {
        return canNurture(player);
    }

    @Override
    public void stop(Player player) {

    }

    private void stun(Player player) {
        player.sendMessage("You are attacked by the sapling!");
        tree.stunAndRepel(player, 1);
    }

    private boolean canNurture(Player player) {
        EvilTreeType treeType = tree.getType();
        int requiredLvl = treeType.nurtureFarmingLevel;
        if (tree.getSaplingObject() != sapling) {
            return false;
        }
        if (requiredLvl > player.getSkills().getLevel(Skills.FARMING)) {
            player.sendMessage("You need a Farming level of " + requiredLvl + " to nurture " + treeType.formattedName + " sapling.");
            return false;
        }
        if (!player.getInventory().hasFreeSlots()) {
            player.sendMessage("You do not have enough space in your inventory.");
            return false;
        }
        return player.withinDistance(sapling, sapling.getDefinitions().sizeX * sapling.getDefinitions().sizeY);
    }
}