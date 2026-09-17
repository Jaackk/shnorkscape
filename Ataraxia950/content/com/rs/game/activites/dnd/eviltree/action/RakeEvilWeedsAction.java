package com.rs.game.activites.dnd.eviltree.action;

import java.util.concurrent.ThreadLocalRandom;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.WorldObject;
import com.rs.game.activites.dnd.eviltree.EvilTree;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.utils.Colors;

/**
 * Handles the completely AFK training action, raking evil weeds.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class RakeEvilWeedsAction extends Action {

    private final EvilTree tree;

    public RakeEvilWeedsAction(EvilTree tree) {
        this.tree = tree;
    }

    @Override
    public boolean process(Player player) {
        if (tree.getWeedsObject() != null && !tree.getWeedsObject().isAshes() && canRakeWeeds(player)) {
            if (tree.getWeedsObject().isBurning() &&
                    ThreadLocalRandom.current().nextInt(4) == 0) {
                int damage = (int) (player.getMaxHitpoints() * 0.05);
                if (damage <= 0) {
                    damage = 1;
                }
                player.sendMessage(Colors.RED + "You are hurt by the burning patch!");
                player.setNextGraphics(new Graphics(1154));
                player.applyHit(new Hit(damage, Hit.HitLook.UNBLOCKABLE_REGULAR_DAMAGE));
            }
            return true;
        }
        return false;
    }

    @Override
    public int processWithDelay(Player player) {
        double xp = tree.getType().rakeXp;
        double chance = tree.getType().getHarvestChance(player) * 150;
        if (player.getPerkManager().hasPerkActive(DonationPerk.TREE_HUNTER)) {
            xp *= 1.25;
            chance *= 0.75;
        }
        player.getSkills().addXp(Skills.FARMING, xp);
        player.setNextAnimation(new Animation(2273));
        player.sendFilteredMessage("You rake the evil patch. It seems to never stop growing...");
        if (ThreadLocalRandom.current().nextInt((int) chance) == 0) {
            player.getInventory().addItem(NurtureEvilSaplingAction.EVIL_SEED_ID, 1);
            player.sendMessage(Colors.RED + "You find an evil seed.");
        }
        return 6;
    }

    @Override
    public boolean start(Player player) {
        if (tree.getWeedsObject() != null && tree.getWeedsObject().isBurning()) {
            player.sendMessage("Why would you want to rake a patch that's on fire?");
            return false;
        }
        if (tree.getTreeObject() != null || tree.getWeedsObject() != null && tree.getWeedsObject().isAshes()) {
            return false;
        }
        return canRakeWeeds(player);
    }

    @Override
    public void stop(Player player) {

    }

    private boolean canRakeWeeds(Player player) {
        if (!player.getInventory().hasFreeSlots()) {
            player.sendMessage("You need at least one inventory space to do this.");
            return false;
        }
        int requiredLvl = tree.getType().rakeFarmingLevel;
        if (requiredLvl > player.getSkills().getLevel(Skills.FARMING)) {
            player.sendMessage("You need a Farming level of " + requiredLvl + " to rake this evil patch.");
            return false;
        }
        WorldObject target = tree.getWeedsObject();
        return player.withinDistance(target, target.getDefinitions().sizeX * target.getDefinitions().sizeY);
    }
}