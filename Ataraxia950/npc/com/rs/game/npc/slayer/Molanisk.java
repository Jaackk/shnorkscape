package com.rs.game.npc.slayer;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

import java.util.List;

public class Molanisk extends NPC {

	private static final long serialVersionUID = -7677090718141679590L;

	public Molanisk(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
	}

	@Override
	public void sendDeath(final Entity source) {
		resetWalkSteps();
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					setNextAnimation(new Animation(6014));
				} else if (loop == 3) {
					getCombat().removeTarget();
					drop();
					reset();
					finish();
					stop();
				}
				loop++;
			}
		}, 0, 1);
	}

	public static boolean useBell(final Player player, final Item item) {
		if (item.getId() == 10952) {
			if (player.getSkills().getLevel(Skills.SLAYER) < 39) {
				player.sendMessage("You need a Slayer level of at least 39 to ring the bell.");
				return true;
			}
			player.lock(3);
			player.setNextAnimation(new Animation(6083));
			List<WorldObject> objects = World.getRegion(player.getRegionId()).getAllObjects();
			if (objects == null)
				return true;
			for (final WorldObject object : objects) {
				if (!object.withinDistance(player, 3) || object.getId() != 22545)
					continue;
				player.sendMessage("The bell re-sounds loudly throughout the cavern.", true);
				WorldTasksManager.schedule(new WorldTask() {

					@Override
					public void run() {
						NPC npc = World.spawnNPC(5751, object, -1, true);
						npc.getCombat().setTarget(player);
						WorldObject o = new WorldObject(object);
						o.setId(22544);
						World.spawnTemporaryObject(o, 15000);
					}
				}, 1);
				return true;
			}
		}
		return false;
	}

}