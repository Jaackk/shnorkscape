package com.rs.game.npc.slayer;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

public class DesertLizard extends NPC {

	private static final long serialVersionUID = 2368995217514394501L;

	public DesertLizard(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
	}

	public static boolean handleDeath(NPC npc, Player player, Item item) {
		if (npc.getId() == 1631 || npc.getId() == 1632) {
			if (item.getId() == 4161) {
				player.getInventory().deleteItem(4161, 1);
				player.faceEntity(npc);
				player.setNextAnimation(new Animation(9504));
				if (npc.getHitpoints() <= 37) {
					npc.resetWalkSteps();
					WorldTasksManager.schedule(new WorldTask() {
						int loop;

						@Override
						public void run() {
							if (loop == 0)
								npc.setNextAnimation(new Animation(9507));
							else if (loop == 2) {
								npc.getCombat().removeTarget();
								npc.drop();
								npc.reset();
								npc.setLocation(npc.getRespawnTile());
								npc.finish();
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
	public void sendDeath(final Entity source) {}

}