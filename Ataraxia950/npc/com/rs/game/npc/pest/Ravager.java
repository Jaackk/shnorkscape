package com.rs.game.npc.pest;

import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.activites.pest.PestControl;
import com.rs.game.activites.pest.PestControlObject;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("serial")
public class Ravager extends PestMonsters {

	private int ticks;
	private List<PestControlObject> objects;

	public Ravager(int id, WorldTile tile, int mapAreaNameHash, int index, PestControl manager) {
		super(id, tile, -1, index, manager);
		objects = new ArrayList<PestControlObject>();
		manager.addNPC(this);
		for (int i = 0; i < getBonuses().length; i++)
			setBonus(i, i == 8 ? getCombatLevel() / 2 : getCombatLevel());
	}
	
	public boolean canWalkNPC(int toX, int toY) {
		return true;
	}

	@Override
	public void processNPC() {
		if (manager.getPestControlObjects() != null && !manager.getPestControlObjects().isEmpty()) {
			objects.addAll(manager.getPestControlObjects());
			objects = objects.stream().filter(o -> Utils.getDistance(this, o) <= 15 && (o.getDefinitions().getName().toLowerCase().contains("gate") && o.getId() != 91335 && o.getId() != 91339) || (o.getDefinitions().getName().toLowerCase().contains("barricade") && o.getId() >= 14224 && o.getId() <= 14226)).sorted(new Comparator<PestControlObject>() {
				@Override
				public int compare(PestControlObject o1, PestControlObject o2) {
					int dist1 = Utils.getDistance(Ravager.this, o1);
					int dist2 = Utils.getDistance(Ravager.this, o2);
					if (dist1 > dist2)
						return 1;
					else if (dist1 < dist2)
						return -1;
					else
						return 0;
				}
			}).collect(Collectors.toList());
			if (!objects.isEmpty()) {
				resetCombat();
				getCombat().removeTarget();
				for (PestControlObject o : objects) {
					if (o == null) {
						objects.clear();
						return;
					}
					if (!Utils.isOnRange(this, o, 1, getSize(), 1)) {
						resetWalkSteps();
						calcFollow(o, 4, true, isIntelligentRouteFinder());
					} else {
						if (ticks >= 4) {
							o.destroyObject(this);
							ticks = 0;
						}
						ticks++;
					}
					objects.clear();
					return;
				}
				objects.clear();
			}

		}

		super.processNPC();
	}

	@Override
	public ArrayList<Entity> getPossibleTargets() {
		ArrayList<Entity> possibleTarget = new ArrayList<Entity>();
		for (Player player : manager.getPlayers()) {
			if (player == null || player.isDead() || player.hasFinished() || !player.isRunning() || !player.withinDistance(this, 10))
				continue;
			possibleTarget.add(player);
		}
		return possibleTarget;
	}
}
