package com.rs.game.npc.slayer;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

public class Rockslug extends NPC {

	private static final long serialVersionUID = -6559745594910925088L;

	public Rockslug(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
	}

	public static boolean handleDeath(NPC npc, Player player, Item item) {
		if (npc.getId() == 1631 || npc.getId() == 1632) {
			if (item == null || item.getId() == 4161) {
				if (item == null) {
					if (!player.getInventory().containsItem(4161, 1)) {
						player.sendMessage("You need some salt to finish the Rockslug off.");
						return true;
					}
				}
				if (npc.getHitpoints() > 37) {
					player.sendMessage("The salt has no effect on the Rockslug this strong.");
					return true;
				}
				npc.resetWalkSteps();
				player.getInventory().deleteItem(4161, 1);
				player.faceEntity(npc);
				player.setNextAnimation(new Animation(9502));
				npc.setNextGraphics(new Graphics(327));
				if (npc.getHitpoints() <= 37) {
					npc.resetWalkSteps();
					WorldTasksManager.schedule(new WorldTask() {
						int loop;

						@Override
						public void run() {
							if (loop == 0) {
								npc.setNextAnimation(new Animation(npc.getCombatDefinitions().getDeathEmote()));
							}
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