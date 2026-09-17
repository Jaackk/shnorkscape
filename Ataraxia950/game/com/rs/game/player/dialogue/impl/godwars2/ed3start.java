package com.rs.game.player.dialogue.impl.godwars2;

import com.rs.game.WorldTile;
import com.rs.game.player.content.eds.EliteDungeonPartyManager;
import com.rs.game.player.dialogue.Dialogue;
public class ed3start extends Dialogue {
	public void start() {
		sendOptionsDialogue("Shadow Reef Elite Dungon 3", "Active Shadow Reef",
				"Close");
		stage = 0;
	}
	public void run(int interfaceId, int componentId) {
		if (componentId == OPTION_1) {
			player.unlock();
			end();
			EliteDungeonPartyManager.enterDungeon(player, 3);

		}


		if (componentId == OPTION_2) {
			player.unlock();
			player.setNextWorldTile(new WorldTile(3367, 3887, 0));
			end();
		}
		return;
	}









	public void finish() {

		player.unlock();
	}

}
