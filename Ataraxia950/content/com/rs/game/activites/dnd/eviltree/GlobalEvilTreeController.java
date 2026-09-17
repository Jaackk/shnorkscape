package com.rs.game.activites.dnd.eviltree;

import com.rs.game.WorldTile;
import com.rs.game.player.controllers.Controller;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class GlobalEvilTreeController extends Controller {

    private static final int DIST = 15;

    @Override
    public void start() {
        if (EvilTreeHandler.isActive() || EvilTreeHandler.isAlive()) {
            EvilTree.openInter(player, EvilTreeHandler.current());
        } else {
            player.getControlerManager().forceStop();
        }
    }

    @Override
    public boolean processMagicTeleport(WorldTile toTile) {
        if (!toTile.withinDistance(player, DIST)) {
            player.getControlerManager().forceStop();
        }
        return true;
    }

    @Override
    public boolean canProcessMovement(int x, int y) {
        WorldTile toTile = new WorldTile(x, y, player.getPlane());
        if (!toTile.withinDistance(player, DIST)) {
            player.getControlerManager().forceStop();
        }
        return true;
    }

    @Override
    public boolean logout() {
        player.getControlerManager().forceStop();
        return true;
    }

    @Override
    public void forceClose() {
        player.getInterfaceManager().closeOverlay(false);
    }

    @Override
    public void process() {
        if (!player.evilTreeInter) {
            player.getControlerManager().forceStop();
            return;
        }
        if (EvilTreeHandler.isActive() || EvilTreeHandler.isAlive() && EvilTreeHandler.current().getTreeTile().withinDistance(player, DIST)) {
            if (EvilTreeHandler.current().isTree() && !EvilTreeHandler.current().isAlive()) {
                player.getControlerManager().forceStop();
            } else {
                EvilTree.openInter(player, EvilTreeHandler.current());
            }
        } else {
            player.getControlerManager().forceStop();
        }
    }

    @Override
    public boolean sendDeath() {
        EvilTreeHandler.clearDamage(player, EvilTreeHandler.current());
        return true;
    }
}