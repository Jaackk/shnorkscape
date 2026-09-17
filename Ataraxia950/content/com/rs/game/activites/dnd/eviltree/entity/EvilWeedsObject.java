package com.rs.game.activites.dnd.eviltree.entity;

import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activites.dnd.eviltree.EvilTree;
import com.rs.game.activites.dnd.eviltree.action.CollectEvilDustAction;
import com.rs.game.activites.dnd.eviltree.action.RakeEvilWeedsAction;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

/**
 * Handles functionality for the Evil weeds patch.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class EvilWeedsObject extends WorldObject {

    private final class BurningWeedsTask extends WorldTask {

        private int stage = 20;

        @Override
        public void run() {
            if (tree.getWeedsObject() == null) {
                stop();
                return;
            }

            showFire();
            if (--stage <= 0) {
                World.removeObject(EvilWeedsObject.this);
                EvilWeedsObject ashes = new EvilWeedsObject(tree, true);
                tree.setWeedsObject(ashes);
                World.spawnObject(ashes);
                stop();
            }
        }

        private void showFire() {
            Graphics fireGraphics = new Graphics(453);
            WorldTile tile = EvilWeedsObject.this;
            World.sendGraphics(null, fireGraphics, tile);
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    WorldTile nextTile = tile.transform(x, y, 0);
                    World.sendGraphics(null, fireGraphics, nextTile);
                }
            }
        }
    }

    private final EvilTree tree;
    private int ashesLeft = -1;
    private boolean burning;

    public EvilWeedsObject(EvilTree tree, boolean ashes) {
        super(ashes ? 110336 : 28551, 10, 0, tree.getLocation().weedsTile);
        this.tree = tree;
        if (ashes) {
            ashesLeft = tree.getType().getDustAmount();
        }
    }

    public void rake(Player player) {
        player.getActionManager().setAction(new RakeEvilWeedsAction(tree));
    }

    public void takeDust(Player player) {
        if (ashesLeft > 0) {
            player.getActionManager().setAction(new CollectEvilDustAction(tree, this));
        } else {
            player.sendFilteredMessage("Too late!");
        }
    }

    public void inspect(Player player) {
        if (isAshes()) {
            if (ashesLeft > 0) {
                player.sendMessage("There seems to be enough ash left for about " + ashesLeft + " Evil dust.");
            } else {
                World.removeObject(this);
                tree.setWeedsObject(null);
            }
        } else if (!burning) {
            player.sendMessage("This patch looks okay to rake.");
        } else {
            player.sendMessage("This patch is on fire! It'll be all burned up soon...");
        }
    }

    public void registerTakeDust() {
        ashesLeft--;
    }

    public int getAshesLeft() {
        return ashesLeft;
    }

    public boolean isAshes() {
        return getId() == 110336;
    }

    public boolean isBurning() {
        return burning;
    }

    public void setBurning() {
        if (!burning) {
            burning = true;
            BurningWeedsTask task = new BurningWeedsTask();
            World.sendGraphics(null, new Graphics(4131), this);
            WorldTasksManager.schedule(task, 4, 4);
        }
    }
}