package com.rs.game.player.client;

import com.rs.game.World;
import com.rs.game.item.floor.FloorItem;
import com.rs.game.player.Player;
import java.util.List;

/** Owner-thread transfer between the original Region floor list and Player inventory. */
final class Native950GroundPickup {
    private Native950GroundPickup() { }
    private static boolean mayTakeOwner(Player player,FloorItem item) {
        if(!item.hasOwner() || player.getUsername().equalsIgnoreCase(item.getOwner()) || player.isOwner())return true;
        if(item.getAttributes()!=null)return false;
        if(player.isGroupIronman()) {
            Player owner=World.getPlayer(item.getOwner());
            return owner!=null && player.canGimInteractWith(owner);
        }
        return !(player.isIronMan() || player.isHCIronMan() || player.isNoviceIronMan()
                || player.isExpertIronMan() || player.isIntermediateIronMan() || player.isKingOfTheSkillGameMode());
    }
    static Native950Containers.Result take(Player player,Native950Containers containers,FloorItem item) {
        if(item==null)return Native950Containers.Result.STALE;
        return take(player,containers,item,World.getRegion(item.getTile().getRegionId()).getGroundItemsSafe());
    }
    static Native950Containers.Result take(Player player,Native950Containers containers,FloorItem item,List<FloorItem> pile) {
        if(item==null || !item.isNative950() || player==null || !player.isNative950() || !player.isActive() || player.hasFinished() || player.isDead()
                || player.isLocked() || player.isNative950ForceMovementActive() || player.getNextWorldTile()!=null
                || Boolean.TRUE.equals(player.getTemporaryAttributtes().get("teleporting"))
                || !player.matches(item.getTile()) || !Native950GroundItemsView.visibleTo(player,item)
                || !mayTakeOwner(player,item) || !player.getControlerManager().canTakeItem(item)
                || !player.getControlerManager().canAddInventoryItem(item.getId(),item.getAmount())
                || !pile.stream().anyMatch(candidate -> candidate==item))return Native950Containers.Result.STALE;
        Native950Containers.Result result=containers.receiveGroundItem(item);
        if(result.moved>0) {
            // No other world action runs between inventory commit and this identity removal.
            // Session ground views publish the resulting delta; WorldTasks expiry sees the same list.
            pile.removeIf(candidate -> candidate==item);
        }
        return result;
    }
}
