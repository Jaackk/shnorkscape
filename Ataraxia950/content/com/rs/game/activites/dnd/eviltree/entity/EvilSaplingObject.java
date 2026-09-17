package com.rs.game.activites.dnd.eviltree.entity;

import com.google.common.collect.ImmutableList;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.activites.dnd.eviltree.EvilTree;
import com.rs.game.activites.dnd.eviltree.action.NurtureEvilSaplingAction;
import com.rs.game.player.Player;
import com.rs.utils.Colors;
import lombok.val;

import java.util.concurrent.TimeUnit;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class EvilSaplingObject extends WorldObject {

    private static final ImmutableList<Integer> STAGES = ImmutableList.of(11392, 11393, 11394, 11395);

    private final EvilTree tree;
    private final int nextPhase;
    private boolean nurtured;

    private EvilSaplingObject(EvilTree tree, int phase) {
        super(STAGES.get(phase++), 10, 0, tree.getTreeTile());
        this.tree = tree;
        nextPhase = phase;
    }

    public EvilSaplingObject(EvilTree tree) {
        this(tree, 0);
    }

    public void nurture(Player player) {
        if (tree.getGrowthTask() == null) {
            throw new IllegalStateException("Sapling does not have growth task.");
        }
        player.getActionManager().setAction(new NurtureEvilSaplingAction(tree, this));
    }

    public void inspect(Player player) {
        if (tree.getGrowthTask() == null) {
            throw new IllegalStateException("Sapling does not have growth task.");
        }
        long minutesLeft = TimeUnit.MINUTES.convert(tree.getGrowthTask().getTaskInfo().getRemainingTicks() * 600,
                TimeUnit.MILLISECONDS);
        String nurturedText = nurtured ? Colors.DARK_GREEN + "This " + tree.getType().formattedName + " sapling has been nurtured. " :
                Colors.DARK_RED + "This " + tree.getType().formattedName + " sapling has not yet been nurtured. ";
        String timeLeftText = minutesLeft > 0 ? "It will grow again in " + minutesLeft + " minute(s)." :
                "It will grow again in a few seconds!";
        player.sendMessage(nurturedText + timeLeftText);
    }

    public boolean grow() {
        if (nextPhase >= 4) {
            if (tree.getWeedsObject() != null) {
                tree.getWeedsObject().setBurning();
            }
            tree.spawnTree();
            EvilTree.sendAllMessage("The Evil Tree is now fully grown! Type ;;eviltree or talk to the Evil Tree Hunter to access it.");
            return false;
        }
        World.removeObject(this);
        val nextSapling = new EvilSaplingObject(tree, nextPhase);
        tree.setSaplingObject(nextSapling);
        World.spawnObject(nextSapling);
        EvilTree.sendAllMessage("The Evil Tree has grown a little. Type ;;eviltree or talk to the Evil Tree Hunter to access it.");
        return true;
    }

    public boolean isNurtured() {
        return nurtured;
    }

    public void setNurtured(boolean nurtured) {
        this.nurtured = nurtured;
    }

    public int getNextPhase() {
        return nextPhase;
    }
}