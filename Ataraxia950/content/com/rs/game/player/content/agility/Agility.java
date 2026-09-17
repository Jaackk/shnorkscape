package com.rs.game.player.content.agility;

import com.rs.game.npc.NPC;
import com.rs.game.npc.others.randomevent.impl.AgilityRandomEvent;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.utils.Utils;

public class Agility {

	public static boolean hasLevel(Player player, int level) {
		if (player.getSkills().getLevel(Skills.AGILITY) < level) {
			player.sendMessage("You need an agility level of " + level + " to use this obstacle.", true);
			return false;
		}
		return true;
	}

	/**
	 * Gives a random Agility NPC.
	 *
	 * @param player
	 */
	public static void checkAgilityRandom(Player player) {
		if ((Utils.random(50) == 0 || player.getLapsRan() % 100 == 0) && player.hasRandomEvent()) {
			if (player.followedByRandomEventNPC())
				return;

			NPC npc = new AgilityRandomEvent(player, player);
			if (npc.withinDistance(player, 14)) {
				player.setCurrentRandomEventNPC(npc);
				player.sendMessage("<col=ff0000>An Agility Instructor has appeared out of nowhere.");
			}
		}
	}
}