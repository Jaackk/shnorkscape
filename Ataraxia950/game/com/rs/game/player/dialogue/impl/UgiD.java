package com.rs.game.player.dialogue.impl;

import com.rs.game.npc.others.Ugi;
import com.rs.game.player.TreasureTrails;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Utils;

public class UgiD extends Dialogue {

	@Override
	public void start() {
		Ugi npc = (Ugi) parameters[0];
		stage = npc.getTarget() == player && player.getTreasureTrails().getPhase() == 4 ? (byte) 0 : (byte) -1;
		run(-1, -1);
	}

	@Override
	public void run(int interfaceId, int componentId) {
		Ugi npc = (Ugi) parameters[0];
		if (stage == 0) {
			sendNPCDialogue(npc.getId(), NORMAL,
					TreasureTrails.UGIS_QUOTES[Utils.random(TreasureTrails.UGIS_QUOTES.length)]);
			stage = 1;
		} else if (stage == 1) {
			sendPlayerDialogue(NORMAL, "What?");
			stage = 2;
		} else if (stage == 2) {
			end();
			npc.finish();

			player.getTreasureTrails().setPhase(5);
			player.getTreasureTrails().setNextClue(TreasureTrails.SOURCE_EMOTE);
		} else if (stage == -1) {
			sendNPCDialogue(npc.getId(), NORMAL, TreasureTrails.UGI_BADREQS);
			stage = -2;
		} else {
			end();
		}
	}

	@Override
	public void finish() {

	}
}
