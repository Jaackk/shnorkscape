package com.rs.game.player.dialogue.impl;

import com.rs.game.player.actions.magic.lunar.impl.MakeLeather;
import com.rs.game.player.dialogue.Dialogue;

public class MakeLeatherOptionD extends Dialogue {

	private MakeLeather spell;
	
	@Override
	public void start() {
		spell = (MakeLeather) parameters[0];
		sendOptionsDialogue("Select the type",
				"Leather",
				"Hard leather");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		player.getTemporaryAttributtes().put("makeLeather", componentId == OPTION_1 ? 0 : 1);
		spell.spellEffect(player, new com.rs.game.item.Item(1739, 1));
		end();
	}

	@Override
	public void finish() {
	
	}

}
