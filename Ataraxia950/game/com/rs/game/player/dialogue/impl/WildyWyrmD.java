package com.rs.game.player.dialogue.impl;

import com.rs.game.activities.wildywyrm.WildyWyrm;
import com.rs.game.player.Skills;
import com.rs.game.player.dialogue.Dialogue;

public class WildyWyrmD extends Dialogue {

	@Override
	public void start() {
		if (player.getSkills().getLevel(Skills.SLAYER) < 94) {
			player.sendMessage("You need a Slayer level of at least 94 to provoke a WildyWyrm.");
			end();
		} else
			sendDialogue("WildyWyrm is a dangerous monster rarely found lurking around the depths of Wilderness. Are you sure you wish to start a fight against it? It will never retreat once provoked.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1)
			sendOptionsDialogue("Start the fight?", "Yes.", "No.");
		else {
			if (componentId == OPTION_1)
				WildyWyrm.getWildywyrm().provokeWyrm(player);
			end();
		}
		stage++;
	}

	@Override
	public void finish() {}

}
