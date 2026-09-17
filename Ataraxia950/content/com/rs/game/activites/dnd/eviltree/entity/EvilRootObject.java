package com.rs.game.activites.dnd.eviltree.entity;

import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activites.dnd.eviltree.EvilTree;
import com.rs.game.activites.dnd.eviltree.action.ChopEvilRootsAction;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class EvilRootObject extends WorldObject {

    public enum RootSpot {
        NORTH,
        SOUTH,
        EAST,
        WEST
    }

    private static final int RESPAWN_TIME = 100;
    private final EvilTree tree;
    private final RootSpot direction;
    private int actionsLeft;

    public EvilRootObject(EvilTree tree, WorldTile tile, RootSpot direction) {
        super(11426, 10, 0, tile);
        this.tree = tree;
        this.direction = direction;
        actionsLeft = computeActionsLeft();
    }

    public void chop(Player player) {
        player.getActionManager().setAction(new ChopEvilRootsAction(tree, this));
    }

    public boolean isAlive() {
        return actionsLeft > 0;
    }
    public boolean registerChopAction() {
        // Just for randomization.
        if (ThreadLocalRandom.current().nextInt(4) == 0) {
            actionsLeft--;
        }
        if (--actionsLeft <= 0) {
            actionsLeft = 0;
            World.removeObject( this);
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    stop();
                    if (tree.getTreeObject() == null || !tree.isAlive()) {
                        return;
                    }
                    spawnNewRoot();
                }
            }, Utils.random(RESPAWN_TIME, RESPAWN_TIME * 2));
            return true;
        }
        return false;
    }

    private void spawnNewRoot() {
        WorldTile newRootTile = EvilRootObject.this;
        EvilRootObject newRoot = new EvilRootObject(tree, newRootTile, direction);
        tree.setRoot(direction, newRoot);
        World.spawnObject(newRoot);
        for (Player player : World.getPlayers()) {
            if (player != null && player.matches(newRootTile)) {
                tree.stunAndRepel(player, 1);
                player.sendMessage("You are hit by an emerging root.");
            }
        }
    }

    public int getActionsLeft() {
        return actionsLeft;
    }

    private int computeActionsLeft() {
        switch (tree.getType()) {
            case NORMAL:
                return 15;
            case OAK:
                return 17;
            case WILLOW:
                return 19;
            case MAPLE:
                return 21;
            case YEW:
                return 23;
            case MAGIC:
                return 25;
            case ELDER:
                return 27;
            default:
                throw new IllegalStateException("Invalid tree type.");
        }
    }
}