package com.rs.game.player.dialogue.impl;

import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.player.dialogue.Dialogue;

public class FlowerOption extends Dialogue {

	WorldObject flowerObject;

	@Override
	public void finish() {

	}

	public int getFlowerId(int objectId) {
		return 2460 + ((objectId - 2980) * 2);
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == 1) {
			if (componentId == 11) {
				player.setNextAnimation(new Animation(827));
				player.getInventory().addItem(getFlowerId(flowerObject.getId()), 1);
				player.getInventory().refresh();
				World.removeObject(flowerObject, false);
			}
			end();
		}
	}

	@Override
	public void start() {
		flowerObject = (WorldObject) parameters[0];
		sendOptionsDialogue("What do you want to do with the flowers?", "Pick", "Leave them");
		stage = 1;
	}
}