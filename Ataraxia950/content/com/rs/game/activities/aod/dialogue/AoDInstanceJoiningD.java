package com.rs.game.activities.aod.dialogue;

import com.rs.game.World;
import com.rs.game.activities.aod.AngelOfDeath;
import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.InputNameEvent;
import com.rs.utils.InputStringEvent;

/**
 * A dialogue to join an AoD instance. Requires password if the instance is protected by it.
 * @author Kris | 30. sept 2017 : 17:10.45
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public final class AoDInstanceJoiningD extends Dialogue {

	@Override
	public void start() {
		player.sendInputName("Whose instance would you like to join?", new InputNameEvent() {
			@Override
			public void run(Player player) {
				final String displayName = getString();
				if (displayName.equals("")) {
					end();
					return;
				}
				String name = null;
				final Player p = World.getPlayerByDisplayName(displayName);
				if (p != null)
					name = p.getUsername();
				for (AngelOfDeath instance : AngelOfDeath.INSTANCES) {
					if (instance == null)
						continue;
					if (instance.getOwner().equals(name) || instance.getOwner().equals(displayName)) {
						if (instance.getPassword() == null) {
							instance.addPlayer(player);
							end();
							return;
						} else {
							player.sendInputString("Enter password:", new InputStringEvent() {
								@Override
								public void run(Player player) {
									final String password = getString();
									if (password.equals("")) {
										end();
										return;
									}
									if (password.equalsIgnoreCase(instance.getPassword())) {
										instance.addPlayer(player);
										end();
										return;
									}
									sendDialogue("Invalid password entered!");
									end();
									return;
								}
							});
						}
						return;
					}
				}
				stage = 10;
				sendDialogue("Could not find an instance for the player by the name of " + displayName + ".");
			}
		});
	}

	@Override
	public void run(int interfaceId, int componentId) {
		end();
	}

	@Override
	public void finish() {
		
	}

}
