package com.rs.game.player.dialogue.impl;

import com.rs.game.player.actions.crafting.Looming;
import com.rs.game.player.actions.crafting.Looming.Loom;
import com.rs.game.player.content.SkillsDialogue;
import com.rs.game.player.dialogue.Dialogue;

/**
 * @author Tom
 * @date April 9, 2017
 */

public class LoomingD extends Dialogue {

	@SuppressWarnings("unused")
	private Loom loom;

	@Override
	public void start() {
		this.loom = (Loom) parameters[0];
		SkillsDialogue.sendSkillsDialogue(player, SkillsDialogue.MAKE,
				"Choose how many you wish to loom,<br>then click on the item to begin.",
				player.getInventory().getItems().getNumberOf(1759), new int[] { 20754, 20755, 20756, 20757, 20758, 20759, 20760, 20761, 20762 }, null);
	}

	@Override
	public void run(int interfaceId, int componentId) {
		player.getActionManager().setAction(new Looming(Loom.VALUES.get(componentId - 14), SkillsDialogue.getMaxQuantity(player)));
		end();
	}

	@Override
	public void finish() {

	}

}
