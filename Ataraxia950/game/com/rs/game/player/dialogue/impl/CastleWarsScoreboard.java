package com.rs.game.player.dialogue.impl;

import com.rs.game.activites.CastleWars;
import com.rs.game.player.dialogue.Dialogue;

public class CastleWarsScoreboard extends Dialogue {

	@Override
	public void finish() {

	}

	@Override
	public void run(int interfaceId, int componentId) {
		end();

	}

	@Override
	public void start() {
		CastleWars.viewScoreBoard(player);

	}

}
