package com.rs.game.npc.pest;

import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.activites.pest.PestControl;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

import java.util.ArrayList;

public class PestMonsters extends NPC {

	private static final long serialVersionUID = -2937010833461775954L;
	protected PestControl manager;
	protected int portalIndex;

	public PestMonsters(int id, WorldTile tile, int mapAreaNameHash, int index, PestControl manager) {
		super(id, tile, mapAreaNameHash, true, true);
		this.manager = manager;
		this.portalIndex = index;
		setForceMultiArea(true);
		setForceAgressive(true);
		setForceTargetDistance(70);
		setIntelligentRouteFinder(true);
		setNoDistanceCheck(true);
		if (!manager.containsNPC(this))
			manager.addNPC(this);
	}
	
	public boolean canWalkNPC(int toX, int toY) {
		return true;
	}

	@Override
	public boolean checkAgressivity() {
		ArrayList<Entity> possibleTarget = getPossibleTargets();
		if (!possibleTarget.isEmpty()) {
			Entity target = possibleTarget.get(Utils.random(possibleTarget.size()));
			if (target == null)
				return false;
			setTarget(target);
			target.setAttackedBy(target);
			target.setFindTargetDelay(Utils.currentTimeMillis() + 10000);
			return true;
		}
		return false;
	}

	@Override
	public boolean canWalkNPC(int toX, int toY, boolean checkUnder) {
		return true;
	}

	@Override
	public ArrayList<Entity> getPossibleTargets() {
		ArrayList<Entity> possibleTarget = new ArrayList<Entity>();
		if (Utils.getDistance(manager.getKnight(), this) >= 10 || Utils.random(7) == 0)
			for (Player player : manager.getPlayers()) {
				if (player == null || player.isDead() || player.hasFinished() || !player.isRunning()
						|| !player.withinDistance(this, 10))
					continue;
				possibleTarget.add(player);
			}
		else
			possibleTarget.add(manager.getKnight());
		return possibleTarget;
	}

	@Override
	public void sendDeath(Entity source) {
		super.sendDeath(source);
		manager.getPestCounts()[portalIndex]--;
	}
}
