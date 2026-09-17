package com.rs.game.player.content.dungeoneering.rooms.puzzles;

import com.rs.game.Animation;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.npc.dungeonnering.DungeonNPC;
import com.rs.game.player.Player;
import com.rs.game.player.content.dungeoneering.DungeonManager;
import com.rs.game.player.content.dungeoneering.rooms.PuzzleRoom;
import com.rs.game.player.content.dungeoneering.skills.DungeoneeringMining;
import com.rs.game.player.content.dungeoneering.skills.DungeoneeringMining.DungeoneeringRocks;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class ThreeStatueWeaponRoom extends PuzzleRoom {

	private static final int[][] EMPTY_STATUES = { 
			{ 11036, 11037, 11038, 11037, 11037 },//melee
			{ 11039, 11040, 11041, 11040, 11040 },//ranged
			{ 11042, 11043, 11044, 11043, 11043 }//magic
			};
	private static final int[][] STATUES = {
			{ 11051, 11052, 11053, 11052, 11052 },
			{ 11045, 11046, 11047, 11046, 11046 },
			{ 11048, 11049, 11050, 11049, 11049 }
	};
	
	private boolean armed;
	
	@Override
	public void openRoom() {
		manager.spawnRandomNPCS(reference);
		manager.spawnItem(reference, new Item(17444), 7, 7, false);
		final int unarmedStatue = Utils.random(3);
		int count = 0;
		for (int x = 0; x < 16; x++) {
			for (int y = 0; y < 16; y++) {
				final WorldObject o = manager.getObjectWithType(reference, 10, x, y);
				if (o != null && o.getId() == 49653) {
					manager.spawnObject(reference, 49063, 10, 0, x, y);
					new Statue(count == unarmedStatue ? EMPTY_STATUES[count][type] : STATUES[count][type], o, manager, 0.0);
					count++;
				}
			}
		}
		if (count < 3) {
			setComplete();
		}
	}
	
	@Override
	public boolean processObjectClick1(final Player p, final WorldObject object) {
		if (object.getDefinitions().getName().equals("Crumbling wall")) {
			p.getActionManager().setAction(new DungeoneeringMining(object, DungeoneeringRocks.CRUMBLING_WALL));
			return false;
		}
		return true;
	}
	
	@Override
	public boolean processNPCClick1(final Player player, final NPC npc) {
		for (int i = 0; i < 5; i++) {
			for (int x = 0; x < 3; x++) {
				if (EMPTY_STATUES[x][i] == npc.getId()) {
					if (player.getInventory().containsItem(17416 + (x * 2), 1)) {
						if (armed) {
							return false;
						}
						final int group = x, slot = i;
						player.lock();
						armed = true;
						player.sendMessage("You put the weapon into the statue's hands.");
						player.getInventory().deleteItem(new Item(17416 + (x * 2), 1));
						player.setNextAnimation(new Animation(9719));
						WorldTasksManager.schedule(new WorldTask() {
							@Override
							public void run() {
								npc.transformIntoNPC(STATUES[group][slot]);
								setComplete();
								player.unlock();
							}
						});
					} else {
						player.sendMessage("You do not have the correct weapon to arm the statue with.");
					}
					return false;
				}
			}
		}
		return true;
	}
	
	public static class Statue extends DungeonNPC {

		private static final long serialVersionUID = -5481570086922572536L;

		public Statue(final int id, final WorldTile tile, final DungeonManager manager, final double multiplier) {
			super(id, tile, manager, multiplier);
		}

		@Override
		public int getDirection() {
			return 0;
		}
	}
	
}
