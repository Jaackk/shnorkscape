package com.rs.game.npc.vorago;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class Scopulus extends NPC {

	private final transient Vorago vorago;
	private boolean enraged;

	public Scopulus(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, Vorago vorago) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
		this.vorago = vorago;
		setForceFollowClose(true);
		setIntelligentRouteFinder(true);
		setForceMultiArea(true);
	}

	@Override
	public boolean isStunImmune() {
		return true;
	}

	@Override
	public ArrayList<Entity> getPossibleTargets() {
		return getPossibleTargets(false, true);
	}

	public ArrayList<Entity> getPossibleTargets(boolean checkNPCs, boolean checkPlayers) {
		ArrayList<Entity> possibleTarget = new ArrayList<Entity>();
		if (vorago == null || vorago.getInstance() == null || vorago.getInstance().getPlayersOnBattle() == null)
			return possibleTarget;
		for (Player player : vorago.getInstance().getPlayersOnBattle()) {
			if (player == null || player.isDead() || player.hasFinished())
				continue;
			possibleTarget.add(player);
		}
		return possibleTarget;
	}

	@Override
	public boolean checkAgressivity() {
		if (getHitpoints() == 0)
			return false;
		ArrayList<Entity> possibleTarget = getPossibleTargets();
		if (!possibleTarget.isEmpty()) {
			Entity target = possibleTarget.get(Utils.random(possibleTarget.size()));
			setTarget(target);
			target.setAttackedBy(target);
			target.setFindTargetDelay(Utils.currentTimeMillis() + 10000);
			return true;
		}
		return false;
	}

	public boolean isEnraged() {
		return enraged;
	}

	public void setEnraged(boolean enraged) {
		this.enraged = enraged;
	}

	@Override
	public void sendDeath(Entity source) {
		final NPCCombatDefinition defs = getCombatDefinitions();
		resetWalkSteps();
		getCombat().removeTarget();
		setNextAnimation(null);
		if (!isDead())
			setHitpoints(0);
		final int deathDelay = defs.getDeathDelay() - (getId() == 50 ? 2 : 1);
		Scopulus thisNPC = this;
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					setNextAnimation(new Animation(defs.getDeathEmote()));
				} else if (loop >= deathDelay) {
					if (source instanceof Player)
						((Player) source).getControlerManager().processNPCDeath(thisNPC);
					if (source != null)
						vorago.sendScopulusDeath(thisNPC);
					reset();
					finish();
					stop();
				}
				loop++;
			}
		}, 0, 1);
	}

	public Vorago getVorago() {
		return vorago;
	}

	@Override
	public double getMeleePrayerMultiplier() {
		return 0.25;
	}

	@Override
	public double getMagePrayerMultiplier() {
		return 0.25;
	}

	@Override
	public double getRangePrayerMultiplier() {
		return 0.25;
	}
}
