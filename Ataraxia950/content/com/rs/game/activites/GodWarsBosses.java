package com.rs.game.activites;

import com.rs.game.Entity;
import com.rs.game.npc.godwars.GodWarMinion;

public final class GodWarsBosses {

	public static final GodWarMinion[] graardorMinions = new GodWarMinion[3];
	public static final GodWarMinion[] commanderMinions = new GodWarMinion[3];
	public static final GodWarMinion[] zamorakMinions = new GodWarMinion[3];
	public static final GodWarMinion[] armadylMinions = new GodWarMinion[3];

	public static void respawnArmadylMinions() {
		for (GodWarMinion minion : armadylMinions) {
			if (minion.hasFinished() || minion.isDead())
				minion.respawn();
		}
	}

	public static void respawnBandosMinions() {
		for (GodWarMinion minion : graardorMinions) {
			if (minion.hasFinished() || minion.isDead())
				minion.respawn();
		}
	}

	public static void respawnSaradominMinions() {
		for (GodWarMinion minion : commanderMinions) {
			if (minion.hasFinished() || minion.isDead())
				minion.respawn();
		}
	}

	public static void respawnZammyMinions() {
		for (GodWarMinion minion : zamorakMinions) {
			if (minion.hasFinished() || minion.isDead())
				minion.respawn();
		}
	}

	/**
	 * Checks if the NPC is at the GodWars Dungeon.
	 *
	 * @param npc
	 *            The NPC to check.
	 * @return if is in the dungeon.
	 */
	public static boolean isAtGodwars(Entity npc) {
		int destX = npc.getX();
		int destY = npc.getY();
		return /* South West */(destX >= 2817 && destY >= 5185 &&
		/* North East */destX <= 2954 && destY <= 5371);
	}
}