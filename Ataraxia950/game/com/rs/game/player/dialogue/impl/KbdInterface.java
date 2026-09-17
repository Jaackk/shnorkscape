package com.rs.game.player.dialogue.impl;

import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.player.content.Magic;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

public class KbdInterface extends Dialogue {

	private WorldObject artefact;

	@Override
	public void finish() {
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (interfaceId == 1361 && componentId == 13) {
			Magic.sendNormalTeleportSpell(player, 0, 0, new WorldTile(2273, 4681, 0));
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					player.faceObject(artefact);
				}
			}, 2);
		}
		player.closeInterfaces();
		end();
	}

	@Override
	public void start() {
		artefact = (WorldObject) parameters[0];
		player.getInterfaceManager().sendInterface(1361);
	}
}