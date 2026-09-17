package com.rs.game.player.dialogue.impl;

import com.rs.game.player.actions.fletching.BoltTipFletching;
import com.rs.game.player.actions.fletching.defs.BoltTips;
import com.rs.game.player.content.SkillsDialogue;
import com.rs.game.player.dialogue.Dialogue;

/**
 * BoltTipFletchingD.java | 11:46:07 AM
 * @author Chryonic
 * @date Apr 15, 2017
 */
public class BoltTipFletchingD extends Dialogue {

	private BoltTips tips;

	@Override
	public void start() {
		this.tips = (BoltTips) parameters[0];
		SkillsDialogue.sendSkillsDialogue(player, SkillsDialogue.CUT,
				"Choose how many you wish to fletch,<br>then click on the item to begin.",
				player.getInventory().getItems().getNumberOf(tips.getGemId()), new int[] { tips.getGemId() }, null);

	}

	@Override
	public void run(int interfaceId, int componentId) {
		player.getActionManager().setAction(new BoltTipFletching(tips, SkillsDialogue.getQuantity(player)));
		end();
	}

	@Override
	public void finish() {
	}
}