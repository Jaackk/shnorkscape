package com.rs.game.player.actions;

import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.floor.FloorItem;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

public class SpellOnSomethingWalkAction extends Action {

    private final Object target;
    private final Runnable runnable;
    private final int range = 8;

    public SpellOnSomethingWalkAction(Object target, Runnable runnable) {
        this.target = target;
        this.runnable = runnable;
    }

    private boolean checkAll(Player player) {
        if (target == null || runnable == null)
            return false;
        if (player == null || player.isDead() || player.hasFinished() || player.isCantWalk())
            return false;
        WorldTile tile = null;
        if (target instanceof FloorItem) {
            FloorItem originalItem = (FloorItem) target;
            final FloorItem item = World.getRegion(originalItem.getTile().getRegionId()).getGroundItem(originalItem.getId(), originalItem.getTile(), player);
            if (item == null || item.getId() != originalItem.getId())
                return false;
            tile = item.getTile();
        } else if (target instanceof WorldObject)
            tile = (WorldObject) target;
        else if (target instanceof Entity)
            tile = new WorldTile((Entity) target);
        else
            return false;
        if (player.isFrozen())
            return !Utils.colides(player, tile, 1, 1);
        if (!Utils.isOnRange(player, tile, range, 1, 1) || (!(target instanceof WorldObject) && !player.clipedProjectile(tile, false))) {
            if (!player.hasWalkSteps()) {
                player.resetWalkSteps();
                return player.calcFollow(tile, player.getRun() ? 2 : 1, true, true, range, false);
            }
        } else
            player.resetWalkSteps();
        return true;
    }

    @Override
    public boolean process(Player player) {
        return checkAll(player);
    }

    @Override
    public int processWithDelay(Player player) {
        WorldTile tile = null;
        if (target instanceof FloorItem) {
            tile = ((FloorItem) target).getTile();
        } else if (target instanceof WorldObject)
            tile = (WorldObject) target;
        else if (target instanceof Entity)
            tile = new WorldTile((Entity) target);
        else
            return -1;
        if (!Utils.isOnRange(player, tile, range, 1, 1) || Utils.colides(player, tile, 1, 1) || (!(target instanceof WorldObject) && !player.clipedProjectile(tile, false))) // doesnt
            return 0;
        player.resetWalkSteps();
        runnable.run();
        return -1;
    }

    @Override
    public boolean start(Player player) {
        return checkAll(player);
    }

    @Override
    public void stop(Player player) {

    }

}
