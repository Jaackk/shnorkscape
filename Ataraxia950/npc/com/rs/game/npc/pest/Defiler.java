package com.rs.game.npc.pest;

import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.activites.pest.PestControl;
import com.rs.utils.Utils;

import java.util.ArrayList;

public class Defiler extends PestMonsters {

	private static final long serialVersionUID = 3179036062334765457L;

	public Defiler(int id, WorldTile tile, int mapAreaNameHash, int index, PestControl manager) {
		super(id, tile, mapAreaNameHash, index, manager);
		for (int i = 0; i < getBonuses().length; i++)
			setBonus(i, i == 8 ? getCombatLevel() : getCombatLevel() * 2);
	}
	
	@Override
	public boolean checkAgressivity() {
		ArrayList<Entity> possibleTarget = getPossibleTargets();
		if (!possibleTarget.isEmpty()) {
			Entity target = Utils.random(2) == 0 ? manager.getKnight() : possibleTarget.get(Utils.random(possibleTarget.size()));
			setTarget(target);
			target.setAttackedBy(target);
			target.setFindTargetDelay(Utils.currentTimeMillis() + 5000);
			return true;
		}
		return false;
	}

}
