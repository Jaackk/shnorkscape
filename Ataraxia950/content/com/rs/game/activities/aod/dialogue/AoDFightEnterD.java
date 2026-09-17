package com.rs.game.activities.aod.dialogue;

import com.rs.game.activities.aod.AngelOfDeath;
import com.rs.game.player.dialogue.Dialogue;

/**
 * A dialogue that sends the player from the balcony to the fight. Can only be done if
 * the fight hasn't already been initiated.
 * @author Kris | 30. sept 2017 : 17:06.26
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public final class AoDFightEnterD extends Dialogue {

	@Override
	public void start() {
		sendOptionsDialogue("LOOKS LIKE A ONE WAY TRIP..",
				"Touch the pillar.",
				"Stay here.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		end();
		if (componentId == OPTION_1) {
			final AngelOfDeath aod = (AngelOfDeath) player.getTemporaryAttributtes().get("aodinstance");
			if (aod == null)
				return;
			aod.getPlayers().add(player);
			player.setNextWorldTile(aod.getWorldTile(2848, 1814));
		}
	}

	@Override
	public void finish() {
		
	}

}
