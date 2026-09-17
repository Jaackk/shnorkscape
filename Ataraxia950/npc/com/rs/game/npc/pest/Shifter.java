package com.rs.game.npc.pest;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.activites.pest.PestControl;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

@SuppressWarnings("serial")
public class Shifter extends PestMonsters {

	public Shifter(int id, WorldTile tile, int mapAreaNameHash, int index, PestControl manager) {
		super(id, tile, mapAreaNameHash, index, manager);
		manager.addNPC(this);
		for (int i = 0; i < getBonuses().length; i++)
			setBonus(i, i == 8 ? getCombatLevel() : getCombatLevel() * 2);
	}
	
	public boolean canWalkNPC(int toX, int toY) {
		return canWalkNPC(toX, toY, false);
	}

	public void teleport(Entity target, int random) {
		if (target instanceof Player && !manager.getPlayers().contains(target))
			return;
		WorldTile teleTile = new WorldTile(target);
		for (int trycount = 0; trycount < 10; trycount++) {
			if (target == null)
				break;
			teleTile = new WorldTile(new WorldTile(target), random);
			if (target == null || teleTile == null)
				break;
			if (!Utils.colides(this, target)
					&& World.canMoveNPC(target.getPlane(), teleTile.getX(), teleTile.getY(), 1))
				break;
			teleTile = new WorldTile(target);
		}
		setNextWorldTile(teleTile);
		setNextAnimation(new Animation(3904));
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				setNextGraphics(new Graphics(654));// 1502
			}
		});
	}
}
