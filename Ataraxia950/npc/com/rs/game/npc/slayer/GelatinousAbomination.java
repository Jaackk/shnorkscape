package com.rs.game.npc.slayer;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

public class GelatinousAbomination extends NPC {

	private static final long serialVersionUID = -2415678019991462921L;

	public GelatinousAbomination(final int id, final WorldTile tile, final int mapAreaNameHash, final boolean canBeAttackFromOutOfArea, final boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
	}

	@Override
	public void setRespawnTask() {
		if (!hasFinished()) {
			reset();
			setLocation(getRespawnTile());
			finish();
		}
		
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				spawn();
				setNextAnimation(new Animation(-1));				
			}
			
		}, getCombatDefinitions().getRespawnDelay());	}

	@Override
	public void sendDeath(final Entity source) {
		if (source instanceof Player) {
			final Player player = (Player) source;
			if (player.getEquipment().getGlovesId() == 23035) {
				resetWalkSteps();
				WorldTasksManager.schedule(new WorldTask() {
					int loop;
					final NPCCombatDefinition defs = getCombatDefinitions();

					@Override
					public void run() {
						if (loop == 0) {
							setNextAnimation(new Animation(defs.getDeathEmote()));
						} else if (loop >= defs.getDeathDelay()) {
							drop();
							reset();
							setLocation(getRespawnTile());
							finish();
							if (!isSpawned()) {
								setRespawnTask();
							}
							stop();

						}
						loop++;
					}
				}, 0, 1);
			} else {
				getCombat().removeTarget();
			}

			return;
		}
	}

	public static boolean handleDeath(final NPC npc, final Player player) {
		if (npc.getId() == 14849) {
			if (player.getEquipment().getGlovesId() != 23035) {
				player.sendMessage("You need to wear some spiked gauntlets to finish the Gelatinous Abomination off!", true);
				return true;
			}
			if (player.getEquipment().getGlovesId() == 23035) {
				if (npc.getHitpoints() <= 1) {
					player.faceEntity(npc);
					player.setNextAnimation(new Animation(14417));
					npc.resetWalkSteps();
					npc.sendDeath(player);
				}
			}
			return true;
		}
		return false;
	}

}