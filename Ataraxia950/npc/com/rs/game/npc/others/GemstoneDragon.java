package com.rs.game.npc.others;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class GemstoneDragon extends NPC {

	private static final long serialVersionUID = 8351507553038770190L;

	public GemstoneDragon(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
	}

	private long specialUsageDelay;
	
	public void addSpecialUsageDelay(int delay) {
		specialUsageDelay = Utils.currentTimeMillis() + delay;
	}
	
	public boolean canUseSpecial() {
		return specialUsageDelay < Utils.currentTimeMillis();
	}
	
	@Override
	public double getMagePrayerMultiplier() {
		return 0.5;
	}
	
	@Override
	public double getMeleePrayerMultiplier() {
		return 0.5;
	}
	
	@Override
	public void sendDeath(final Entity source) {
		final NPCCombatDefinition defs = getCombatDefinitions();
		resetWalkSteps();
		getCombat().removeTarget();
		/*
		 * Death combat reset deathcombat combatreset deathreset
		 */
		if (source instanceof Player)
			source.deathResetCombat();
		setNextAnimation(null);
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0)
					setNextAnimation(new Animation(defs.getDeathEmote()));
				else if (loop >= defs.getDeathDelay()) {
					if (source instanceof Player) {
						Player p = (Player) source;
						if (p.getTask() == null || p.getTask() != null && !p.getTask().getName(p).equals("Gemstone dragon"))
							p.addGemstoneKC(-1);
					}
					drop();
					reset();
					setLocation(getRespawnTile());
					finish();
					if (!isSpawned())
						setRespawnTask();
					stop();
				}
				loop++;
			}
		}, 0, 1);
	}
	
}
