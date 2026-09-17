package com.rs.game.npc.godwars.zaros;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

import java.util.ArrayList;

/**
 * Handles the Godwars Zaros faction.
 *
 * @author Noel
 */
public class GodwarsZarosFaction extends NPC {

	/**
	 * The generated serial UID.
	 */
	private static final long serialVersionUID = -8818026872472088664L;

	public GodwarsZarosFaction(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea,
			boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
	}

	@Override
	public ArrayList<Entity> getPossibleTargets() {
		if (!withinDistance(new WorldTile(2881, 5306, 0), 200))
			return super.getPossibleTargets();
		else {
			ArrayList<Entity> targets = getPossibleTargets(true, true);
			ArrayList<Entity> targetsCleaned = new ArrayList<Entity>();
			for (Entity t : targets) {
				if (t instanceof GodwarsZarosFaction || (t instanceof Player && hasGodItem((Player) t)))
					continue;
				targetsCleaned.add(t);
			}
			return targetsCleaned;
		}
	}

	/**
	 * Checks for a God Item on the players equipment container.
	 *
	 * @param player
	 *            The player to check.
	 * @return if Has a God Item.
	 */
	private boolean hasGodItem(Player player) {
		for (Item item : player.getEquipment().getItems().getItems()) {
			if (item == null)
				continue;
			String name = item.getDefinitions().getName().toLowerCase();
			/** Else If's because only one item counts. **/
			if (name.contains("ceremonial"))
				return true;
			else if (name.contains("ceremonial"))
				return true;
			else if (name.contains("ceremonial"))
				return true;
			else if (name.contains("ceremonial"))
				return true;
			else if (name.contains("ceremonial"))
				return true;
			else if (name.contains("ceremonial"))
				return true;
			else if (name.contains("ceremonial"))
				return true;
			else if (name.contains("ceremonial"))
				return true;
		}
		return false;
	}

	@Override
	public void sendDeath(final Entity source) {
		final NPCCombatDefinition defs = getCombatDefinitions();
		checkGodwarsKillcount();
		resetWalkSteps();
		getCombat().removeTarget();
		setNextAnimation(null);
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					setNextAnimation(new Animation(defs.getDeathEmote()));
				} else if (loop == 1) {
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