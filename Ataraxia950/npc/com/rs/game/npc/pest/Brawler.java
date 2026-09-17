package com.rs.game.npc.pest;

import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.activites.pest.PestControl;
import com.rs.game.player.Player;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class Brawler extends PestMonsters {

	public Brawler(int id, WorldTile tile, int mapAreaNameHash, int index, PestControl manager) {
		super(id, tile, -1, index, manager);
		manager.addNPC(this);
		for (int i = 0; i < getBonuses().length; i++)
			setBonus(i, (i == 8 ? getCombatLevel() / 2 : getCombatLevel() * 2) / (i < 5 ? 2 : 1));
	}
	
	public boolean canWalkNPC(int toX, int toY) {
		return true;
	}

	@Override
	public void processNPC() {
		super.processNPC();
	}

	@Override
	public ArrayList<Entity> getPossibleTargets() {
		ArrayList<Entity> possibleTarget = new ArrayList<Entity>();
		for (Player player : manager.getPlayers()) {
			if (player == null || player.isDead() || player.hasFinished() || !player.isRunning()
					|| !player.withinDistance(this, 10))
				continue;
			possibleTarget.add(player);
		}
		return possibleTarget;
	}
}
