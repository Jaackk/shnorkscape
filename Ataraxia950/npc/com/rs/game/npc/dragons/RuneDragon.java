package com.rs.game.npc.dragons;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

/**
 * A class used to handle Rune Dragon behaviour.
 *
 * @author Noel
 */
public class RuneDragon extends NPC {

	/**
	 * The generated serial UID.
	 */
	private static final long serialVersionUID = 8811721304905284108L;
	/**
	 * Phasing Integer.
	 */
	private int phase;

	/**
	 * Init the dragon in World.
	 *
	 * @param id
	 *            The NPC ID.
	 * @param tile
	 *            The WorldTile.
	 * @param mapAreaNameHash
	 *            Region hash.
	 * @param canBeAttackFromOutOfArea
	 *            can be attacked out of its area.
	 * @param spawned
	 *            if spawned.
	 */
	public RuneDragon(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		setCapDamage(952);
		setLureDelay(0);
		setPhase(1);
	}

	/**
	 * Gets dragons phase.
	 *
	 * @return the phase Integer.
	 */
	public int getPhase() {
		return this.phase;
	}

	/**
	 * Sets dragons phase.
	 *
	 * @param i
	 *            The phase to set.
	 */
	public void setPhase(int i) {
		this.phase = i;
	}

	@Override
	public void processNPC() {
		super.processNPC();
		// Switch back to default and healing since not in combat anymore.
		if (getId() != 21136 && !isCantInteract() && !isUnderCombat() && getHitpoints() > 0) {
			setCantInteract(true);
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					if (getPhase() == 2)
						setNextAnimation(new Animation(26528));
					transformIntoNPC(21136);
					setHitpoints(getMaxHitpoints());
					resetReceivedDamage();
					setCantInteract(false);
					stop();
				}
			}, 2);
		}
		// Switch to phase 2 while in combat
		if (getHitpoints() < 4705 && getPhase() == 1 && isUnderCombat()) {
			setNextAnimation(new Animation(26526));
			transformIntoNPC(21137);
			setPhase(2);
		}
		// Switch to phase 3 while in combat
		if (getHitpoints() < 2520 && getPhase() == 2 && isUnderCombat()) {
			setNextAnimation(new Animation(26528));
			transformIntoNPC(21140);
			setPhase(3);
		}
	}

	@Override
	public void handleIngoingHit(Hit hit) {
		if (hit != null && hit.getDamage() > 0) {
			if (getPhase() == 1)
				hit.setDamage(hit.getDamage() / 2);
			else if (getPhase() == 2 || getPhase() == 3) {
				hit.setDamage((int) (hit.getDamage() * 0.9));
				if (getPhase() == 2) {
					if (hit.getLook() == HitLook.MELEE_DAMAGE)
						hit.setDamage(0);
				}
			}
		}
		super.handleIngoingHit(hit);
	}

	@Override
	public void sendDeath(final Entity source) {
		setNextNPCTransformation(21136);
		setNextAnimation(new Animation(-1));
		final NPCCombatDefinition defs = getCombatDefinitions();
		resetWalkSteps();
		resetCombat();

		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0)
					setNextAnimation(new Animation(defs.getDeathEmote()));
				else if (loop == 3) {
					drop();
					reset();
					getCombat().removeTarget();
					setLocation(getRespawnTile());
					finish();
					setRespawnTask();
					stop();
				}
				loop++;
			}
		}, 0, 1);
	}
}