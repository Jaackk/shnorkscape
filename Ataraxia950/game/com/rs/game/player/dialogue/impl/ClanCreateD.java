package com.rs.game.player.dialogue.impl;

import com.rs.game.player.Player;
import com.rs.game.player.content.clans.ClansManager;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.InputNameEvent;

public class ClanCreateD extends Dialogue {

	@Override
	public void start() {
		sendDialogue("You must be a member of a clan in order to join their channel.",
				"Would you like to create a clan?");

	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1) {
			player.sendInputName("Enter the clan name you'd like to have.", new InputNameEvent() {
				@Override
				public void run(Player player) {
					ClansManager.createClan(player, getString());
				}
			});
			end();
		}

	}

	@Override
	public void finish() {
		
	}

}
