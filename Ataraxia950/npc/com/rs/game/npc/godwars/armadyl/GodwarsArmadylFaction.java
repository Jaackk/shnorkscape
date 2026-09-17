package com.rs.game.npc.godwars.armadyl;

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

@SuppressWarnings("serial")
public class GodwarsArmadylFaction extends NPC {

	public GodwarsArmadylFaction(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea,
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
				if (t instanceof GodwarsArmadylFaction || (t instanceof Player && hasGodItem((Player) t)))
					continue;
				targetsCleaned.add(t);
			}
			return targetsCleaned;
		}
	}

	private boolean hasGodItem(Player player) {
		for (Item item : player.getEquipment().getItems().getItems()) {
			if (item == null)
				continue; // shouldn't happen
			String name = item.getDefinitions().getName().toLowerCase();
			// using else as only one item should count
			if (name.contains("armadyl helmet") || name.contains("torva full helm") || name.contains("pernix cowl")
					|| name.contains("virtus mask"))
				return true;
			else if (name.contains("armadyl cloak"))
				return true;
			else if (name.contains("armadyl pendant") || name.contains("armadyl stole"))
				return true;
			else if (name.contains("armadyl godsword") || name.contains("armadyl crozier")
					|| name.contains("zaryte bow"))
				return true;
			else if (name.contains("armadyl body") || name.contains("armadyl robe top")
					|| name.contains("armadyl chestplate") || name.contains("armadyl platebody")
					|| name.contains("torva platebody") || name.contains("pernix body")
					|| name.contains("virtus robe top"))
				return true;
			else if (name.contains("illuminated book of law") || name.contains("book of law")
					|| name.contains("armadyl kiteshield"))
				return true;
			else if (name.contains("armadyl robe legs") || name.contains("armadyl plateskirt")
					|| name.contains("armadyl chaps") || name.contains("armadyl platelegs")
					|| name.contains("armadyl Chainskirt") || name.contains("torva platelegs")
					|| name.contains("pernix chaps") || name.contains("virtus robe legs"))
				return true;
			else if (name.contains("armadyl vambraces") || name.contains("armadyl gloves"))
				return true;
			else if (name.contains("armadyl boots"))
				return true;
			else if (name.contains("armadyl buckler"))
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