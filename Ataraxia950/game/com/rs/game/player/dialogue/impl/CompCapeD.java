package com.rs.game.player.dialogue.impl;

import com.rs.game.player.Skills;
import com.rs.game.player.content.Magic;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Utils;

/**
 * Handles Completionist Cape features option.
 *
 * @author Noel
 */
public class CompCapeD extends Dialogue {

	@Override
	public void start() {
		if (!player.isComp() || !player.isCompT()) {
			player.sendMessage("<col=ff0000><shad=000000>You are not worthy enough!");
			if (!player.isComp())
				player.getDistinctionCapes().sendCompletionistInterface();
			else
				player.getDistinctionCapes().sendTrimmedCompletionistInterface();
			return;
		}
		sendOptionsDialogue("Completionist cape", "Teleport to Kandarin Monastery", "Teleport to Ardougne Farm",
				"Teleport to Max Guild", "Summoning point restoration");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1) {
			end();
			switch (componentId) {
			case OPTION_1:
				Magic.compCapeTeleport(player, 2606, 3222, 0);
				break;
			case OPTION_2:
				Magic.compCapeTeleport(player, 2664, 3375, 0);
				break;
			case OPTION_3:
				Magic.compCapeTeleport(player, 2274, 3340, 1);
				break;
			case OPTION_4:
				Long sumRestore = (Long) player.getTemporaryAttributtes().get("sum_restore");
				if (sumRestore != null && sumRestore + 1500000 > Utils.currentTimeMillis()) {
					player.sendMessage("You can only restore your summoning points once every 15 minutes.");
					return;
				}
				player.getSkills().set(Skills.SUMMONING, 99);
				player.getTemporaryAttributtes().put("sum_restore", Utils.currentTimeMillis());
				player.sendMessage("Summoning points restored, you can do this again in 15 minutes.");
				break;
			}
		}
	}

	@Override
	public void finish() {
	}

}