package com.rs.game.activites.dnd.eviltree.action;

import com.rs.game.WorldObject;
import com.rs.game.activites.dnd.eviltree.EvilTree;
import com.rs.game.activites.dnd.eviltree.entity.EvilRootObject;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.utils.Utils;

/**
 * Handles the chopping of an {@link EvilRootObject}.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class ChopEvilRootsAction extends ChopAction {
    private final EvilRootObject root;

    public ChopEvilRootsAction(EvilTree tree, EvilRootObject root) {
        super(tree, 2, 4);
        this.root = root;
    }

    @Override
    public boolean onStart(Player player) {
        if (root.isAlive() && tree.isAlive()) {
            player.sendFilteredMessage("You begin chopping the evil roots.");
            return true;
        }
        return false;
    }

    @Override
    public boolean onProcess(Player player) {
        return root.isAlive();
    }

    @Override
    public boolean onChop(Player player) {
        int kindlingAmount = getKindlingAmount();
        player.getInventory().addItem(14666, kindlingAmount);
        player.sendFilteredMessage("You get " + kindlingAmount + " Evil tree kindling.");
        double xp = tree.getType().rootXp;
        if (player.getPerkManager().hasPerkActive(DonationPerk.TREE_HUNTER)) {
            xp *= 1.25;
        }
        player.getSkills().addXp(Skills.WOODCUTTING, xp);
        if (root.registerChopAction()) {
            player.getActionManager().forceStop();
        }
        return true;
    }

    @Override
    public WorldObject target() {
        return root;
    }

    private int getKindlingAmount() {
        int halvedAxeTime = axe.time / 2;
        if (halvedAxeTime < 3) {
            halvedAxeTime = 3;
        }
        return Utils.random(1, halvedAxeTime);
    }
}