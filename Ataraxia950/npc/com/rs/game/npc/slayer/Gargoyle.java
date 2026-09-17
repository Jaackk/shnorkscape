package com.rs.game.npc.slayer;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

public class Gargoyle extends NPC {

	private static final long serialVersionUID = -6559745594910925088L;

	public Gargoyle(final int id, final WorldTile tile, final int mapAreaNameHash, final boolean canBeAttackFromOutOfArea, final boolean spawned,int dawdawd) {
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
				spawn();
				setNextAnimation(new Animation(-1));				
			}
			
		}, getCombatDefinitions().getRespawnDelay());
	}

	public static boolean handleDeath(final NPC npc, final Player player, final Item item) {
		if (npc.getId() == 1610) {
			if (!player.getInventory().containsItem(item)) {
				player.sendMessage("You need a rock hammer to finish the Gargoyle off!", true);
				return true;
			}
			if (item.getId() == 4162) {
				if (npc.getHitpoints() <= 37) {
					player.faceEntity(npc);
					player.setNextAnimation(new Animation(11066));
					npc.resetWalkSteps();
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							npc.getCombat().removeTarget();
							npc.drop();
							npc.reset();
							npc.setLocation(npc.getRespawnTile());
							npc.finish();
							npc.setRespawnTask();
							stop();
						}
					}, 0, 1);
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public void sendDeath(final Entity source) {
		getCombat().removeTarget();
		WorldTasksManager.schedule(new WorldTask() {
			int count;
			@Override
			public void run() {
				if (hasFinished()) {
					stop();
				}
				if (count == 30) {
					setHitpoints(getMaxHitpoints());
					reset();
					setNextAnimation(new Animation(-1));
					stop();
					return;
				}
				setNextAnimation(new Animation(9458));
				count++;
			}
		}, 0, 3);
		return;
	}

}