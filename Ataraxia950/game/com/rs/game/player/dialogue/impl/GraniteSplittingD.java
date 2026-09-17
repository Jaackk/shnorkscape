package com.rs.game.player.dialogue.impl;

import com.rs.game.player.actions.crafting.Granite;
import com.rs.game.player.content.SkillsDialogue;
import com.rs.game.player.dialogue.Dialogue;

public class GraniteSplittingD extends Dialogue {

	private int granite;

	@Override
	public void finish() {

	}

	@Override
	public void run(int interfaceId, int componentId) {
		player.getActionManager().setAction(new Granite(granite, SkillsDialogue.getQuantity(player)));
		end();
	}

	@Override
	public void start() {
		this.granite = (int) parameters[0];
		SkillsDialogue.sendSkillsDialogue(player, SkillsDialogue.CUT, "Choose how many you wish to split.", player.getInventory().getItems().getNumberOf(granite), new int[] { granite == 6983 ? 6981 : 6979 }, null);
	}

}
