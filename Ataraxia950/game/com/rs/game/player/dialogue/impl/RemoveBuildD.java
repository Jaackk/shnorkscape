package com.rs.game.player.dialogue.impl;

import com.rs.game.WorldObject;
import com.rs.game.player.dialogue.Dialogue;

public class RemoveBuildD extends Dialogue {

	WorldObject object;

	@Override
	public void start() {
		this.object = (WorldObject) parameters[0];
		sendOptionsDialogue("Destroy " + object.getDefinitions().getName() + "?", "Yes.", "No.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (componentId == OPTION_1)
			player.getHouse().removeBuild(object);
		end();
	}

	@Override
	public void finish() {
	}
}