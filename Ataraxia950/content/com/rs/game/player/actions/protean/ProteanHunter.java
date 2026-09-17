package com.rs.game.player.actions.protean;

import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.content.OwnedObjectManager;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

import java.util.List;

public class ProteanHunter extends Action {

	private static final int[][] TILES = new int[][] { { 0, 1 }, { 1, 0 }, { 0, -1 }, { -1, 0 }, { 1, 1 }, { 1, -1 }, { -1, -1 }, { -1, 1 }, { 0, 0 } };

	public ProteanHunter(int amount) {
		this.amount = amount;
	}

	private int amount;
	private boolean auto;

	@Override
	public boolean process(Player player) {
        return amount != 0;
    }

	private WorldTile getTile(Player player) {
		List<WorldObject> objects = World.getRegion(player.getRegionId()).getSpawnedObjects();
		x: for (int i = 0; i < TILES.length; i++) {
			if (World.isTileFree(player.getPlane(), player.getX() + TILES[i][0], player.getY() + TILES[i][1], 1)) {
				if (objects != null) {
					for (WorldObject object : objects) {
						if (object.getX() == player.getX() + TILES[i][0] && object.getY() == player.getY() + TILES[i][1] && object.getPlane() == player.getPlane())
							continue x;
					}
				}
				return new WorldTile(player.getX() + TILES[i][0], player.getY() + TILES[i][1], player.getPlane());
			}
		}
		return null;
	}

	@Override
	public int processWithDelay(Player player) {
		int trapsAmount = OwnedObjectManager.getObjectsforValue(player, 93381);
		int maxAmount = (player.getSkills().getLevelForXp(Skills.HUNTER) / 15) + (player.getPerkManager().hasPerkActive(DonationPerk.HUNTSMAN) ? 2 : 0);
		if (trapsAmount > maxAmount) {
			if (!auto) {
				player.getPackets().sendGameMessage("You cannot place more than " + maxAmount + " traps at once.");
				return -1;
			} else
				return 1;
		}
		WorldTile tile = getTile(player);
		if (tile == null) 
			return 1;
		player.setNextFaceWorldTile(tile);
		player.setNextAnimation(new Animation(5208));
		player.getInventory().deleteItem(new Item(32337, 1));
		player.lock(3);
		player.getPackets().sendGameMessage("You begin setting up the trap.", true);
		setActionDelay(player, 4);
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				OwnedObjectManager.addOwnedObjectManager(player, new WorldObject[] { new WorldObject(93381, 10, 0, tile.getX(), tile.getY(), tile.getPlane()) }, new long[] { 300000 });
				amount--;
			}
		}, 3);
		return 1;
	}

	@Override
	public boolean start(Player player) {
		auto = amount > 1;
		return true;
	}

	@Override
	public void stop(Player player) {}

}