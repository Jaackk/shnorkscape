package com.rs.game.player.dialogue.impl;

import com.rs.game.player.content.death.Gravestone;
import com.rs.game.player.dialogue.Dialogue;

public class InspectGravestone extends Dialogue {

	private Gravestone gravestone;
	
	@Override
	public void start() {
		gravestone = (Gravestone) parameters[0];
		sendOptionsDialogue("Select an Option", "Inspect gravestone.", "Demolish gravestone (Items will become public).");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (componentId == OPTION_1)
			gravestone.sendGraveInscription(player);
		else
			gravestone.demolish(player);
		end();
	}

	@Override
	public void finish() {
		
	}

}
