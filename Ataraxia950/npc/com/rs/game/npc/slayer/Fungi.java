package com.rs.game.npc.slayer;

import com.rs.cores.CoresManager;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

import java.util.concurrent.TimeUnit;

public class Fungi extends NPC {

	private static final long serialVersionUID = -6046026862314870060L;

	public Fungi(final int id, final WorldTile tile, final int mapAreaNameHash, final boolean canBeAttackFromOutOfArea,
			final boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
	}

	public static boolean pickFungi(final Player player, final NPC npc) {
		if (npc.getId() == 3344 || npc.getId() == 3345) {
			player.setNextAnimation(new Animation(7271));
			CoresManager.getServiceProvider().executeWithDelay(() -> {
				npc.transformIntoNPC(3346);
				player.sendMessage("The fungi comes to life!", true);
				npc.setTarget(player);
			}, 400, TimeUnit.MILLISECONDS);
			return true;
		}
		return false;
	}

	public static boolean refillFungicide(final Player player, final Item spray, final Item fungicide) {
		if (spray.getId() >= 7422 && spray.getId() <= 7431 && fungicide.getId() == 7432
				|| fungicide.getId() >= 7422 && fungicide.getId() <= 7431 && spray.getId() == 7432) {
			if (fungicide.getId() == 7432) {
				spray.setId(7421);
				player.getInventory().deleteItem(fungicide.getId(), 1);
			} else {
				fungicide.setId(7421);
				player.getInventory().deleteItem(spray.getId(), 1);
			}
			player.getInventory().refresh();
			player.sendMessage("You refill the spray with a new can of fungicide, throwing the old one away..");
			return true;
		}
		return false;
	}

	@Override
	public void setRespawnTask() {
		if (!hasFinished()) {
			reset();
			setLocation(getRespawnTile());
			finish();
		}
		final NPC npc = this;
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				setFinished(false);
				World.addNPC(npc);
				npc.setLastRegionId(0);
				World.updateEntityRegion(npc);
				loadMapRegions();
			}

		}, getCombatDefinitions().getRespawnDelay());
	}

	public static boolean handleDeath(final NPC npc, final Player player, final Item item) {
		if (npc.getId() >= 3346 && npc.getId() <= 3347) {
			if (item.getId() >= 7421 && item.getId() <= 7430) {
				if (item.getId() != 7421) {
					item.setId(item.getId() + 1);
				}
				player.getInventory().refresh();
				player.faceEntity(npc);
				player.setNextAnimation(new Animation(9504));
				if (npc.getHitpoints() <= 20) {
					npc.resetWalkSteps();
					WorldTasksManager.schedule(new WorldTask() {
						int loop;

						@Override
						public void run() {
							if (loop == 0) {
								npc.setNextAnimation(new Animation(2777));
							} else if (loop == 2) {
								npc.getCombat().removeTarget();
								npc.drop();
								npc.reset();
								npc.setLocation(npc.getRespawnTile());
								npc.finish();
								npc.transformIntoNPC(3344);
								npc.setRespawnTask();
								stop();
							}
							loop++;
						}
					}, 0, 1);
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public void sendDeath(final Entity source) {
	}

}