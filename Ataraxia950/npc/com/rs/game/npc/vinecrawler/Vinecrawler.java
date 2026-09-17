
package com.rs.game.npc.vinecrawler;

import java.util.ArrayList;
import java.util.List;

import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;

@SuppressWarnings("serial")
public class Vinecrawler extends NPC {

	public Vinecrawler(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		this.setLureDelay(0);
		this.setCapDamage(1750);

		this.setRun(true);
		this.setForceMultiAttacked(false);
		this.setForceAgressive(false);
		this.setForceTargetDistance(25);
		this.setHitpoints(3100);
		this.getCombatDefinitions().setHitpoints(3100);
	}

	@Override
	public void processNPC() {
		super.processNPC();
		if (isDead())
			return;
		checkReset();

	}
	@Override
    public double getMeleePrayerMultiplier() {
		return 0.50;
    }
	
	
	@Override
	public double getMagePrayerMultiplier() {
		return 0.275;
	}
	
	@Override
    public double getRangePrayerMultiplier() {
		return 0.275;
    }
	public void checkReset() {
		int maxhp = getMaxHitpoints();
		if (maxhp > getHitpoints() && !isUnderCombat() && getPossibleTargets().isEmpty())
			setHitpoints(maxhp);
	}
	
	//test
	@Override
	public ArrayList<Entity> getPossibleTargets() {
		ArrayList<Entity> possibleTarget = new ArrayList<Entity>();
		for (int regionId : getMapRegionsIds()) {
			List<Integer> playerIndexes = World.getRegion(regionId).getPlayerIndexes();
			if (playerIndexes != null) {
				for (int npcIndex : playerIndexes) {
					Player player = World.getPlayers().get(npcIndex);
					if (player == null || player.isDead() || player.hasFinished() || !player.isRunning()
							|| !player.withinDistance(this, 64)
							|| ((!isAtMultiArea() || !player.isAtMultiArea()) && player.getAttackedBy() != this
									&& player.getAttackedByDelay() > System.currentTimeMillis())
							|| !clipedProjectile(player, false))
						continue;
					possibleTarget.add(player);
				}
			}
		}
		return possibleTarget;
	}
	
	//test

}
