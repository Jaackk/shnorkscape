package com.rs.game.npc.slayer;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Logger;

public class Cockatrice extends NPC {

	private static final long serialVersionUID = 3796558670687414932L;

	public Cockatrice(final int id, final WorldTile tile, final int mapAreaNameHash, final boolean canBeAttackFromOutOfArea, final boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		setForceAgressive(true);
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
				try {
					spawn();
				setNextAnimation(new Animation(7765));
				} catch (final Throwable e) {
					Logger.getGlobal().catching(e);
				}
			}
		}, getCombatDefinitions().getRespawnDelay());
	}

	@Override
	public void sendDeath(final Entity source) {
		resetWalkSteps();
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					setNextAnimation(new Animation(7763));
				} else if (loop == 3) {
					getCombat().removeTarget();
					drop();
					reset();
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