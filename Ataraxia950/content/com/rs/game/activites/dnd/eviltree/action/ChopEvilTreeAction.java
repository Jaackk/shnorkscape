package com.rs.game.activites.dnd.eviltree.action;

import java.util.concurrent.ThreadLocalRandom;

import com.rs.game.WorldObject;
import com.rs.game.activites.dnd.eviltree.EvilTree;
import com.rs.game.activites.dnd.eviltree.entity.EvilTreeObject;
import com.rs.game.item.Item;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.utils.Colors;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class ChopEvilTreeAction extends ChopAction {

    private static final int EVIL_BARK_ID = 3239;

    private final EvilTreeObject treeObject;

    public ChopEvilTreeAction(EvilTree tree, EvilTreeObject treeObject) {
        super(tree, 2, 5);
        this.treeObject = treeObject;
    }

    @Override
    public boolean onProcess(Player player) {
        return tree.getTreeObject() != null && !treeObject.isDead();
    }

    @Override
    public boolean onStart(Player player) {
        player.sendFilteredMessage("You begin chopping the evil tree.");
        return true;
    }

    @Override
    public boolean onChop(Player player) {
        Item weapon = player.getEquipment().getItem(Equipment.SLOT_WEAPON);
        boolean hasAugmentedTool = weapon != null && weapon.getInventionData() != null && (weapon.getName().toLowerCase().contains("dragon hatchet") || weapon.getName().toLowerCase().contains("crystal hatchet"));
            if (!hasAugmentedTool)
                weapon = null;
        double xp = tree.getType().treeXp;
        if(player.getPerkManager().hasPerkActive(DonationPerk.TREE_HUNTER)) {
            xp *= 1.25;
        }
        player.getSkills().addXp(Skills.WOODCUTTING, xp);
        player.getInventionManager().processSkillXp(Skills.WOODCUTTING, xp, weapon);
        int chance = tree.getType().getHarvestChance(player) /2;
        if(chance < 2)
            chance = 2;
        if (ThreadLocalRandom.current().nextInt(chance) == 0) {
            player.getInventory().addItem(EVIL_BARK_ID, 1);
            player.sendMessage(Colors.RED + "You get some evil bark.");
        }
        treeObject.registerChopAction(player);
        return true;
    }

    @Override
    public WorldObject target() {
        return treeObject;
    }
}