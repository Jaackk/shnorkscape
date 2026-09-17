package com.rs.game.player.dialogue.impl;

import com.rs.game.player.controllers.pestcontrol.PestControlGame;
import com.rs.game.player.dialogue.Dialogue;

public class PCSquire extends Dialogue {

	private int option;
	private static final int SQUIRE = 3781;
	
	@Override
	public void start() {
		option = (int) parameters[0];
		if (option == 1)
			sendNPCDialogue(SQUIRE, NORMAL, "Be quick, we're under attack!");
		else {
			if (player.getControlerManager().getControler() instanceof PestControlGame) {
				((PestControlGame) player.getControlerManager().getControler()).getGame().leave(player, false);
				end();
				player.sendMessage("You leave the game..");
			}
		}
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch(stage) {
		case -1:
			sendOptionsDialogue("Select an Option", 
					"What's going on?",
					"How do I repair things?",
					"I want to leave.",
					"I'd better get back to it then.");
			break;
		case 0:
			switch(componentId) {
			case OPTION_1:
				sendPlayerDialogue(NORMAL, "What's going on?");
				break;
			case OPTION_2:
				sendPlayerDialogue(NORMAL, "How do I repair things?");
				stage = 10;
				return;
			case OPTION_3:
				sendPlayerDialogue(NORMAL, "I want to leave.");
				stage = 20;
				return;
			case OPTION_4:
				sendPlayerDialogue(NORMAL, "I'd better get back to it then.");
				stage = 30;
				return;
			}
			break;
		case 1:
			sendNPCDialogue(SQUIRE, NORMAL, "This island is being invaded by outsiders and "
					+ "the Void Knight over there is using a ritual to unsummon their portals. "
					+ "We must defend the Void Knight at all costs, however if you get "
					+ "an opening you can destroy the portals yourself.");
			stage = -1;
			return;
		case 10:
			sendNPCDialogue(SQUIRE, NORMAL, "There are stockpiles of logs near the trees "
					+ "on the island. You'll need to collect logs from the stockpiles "
					+ "and use a hammer to repair the defences.");
			stage = -1;
			return;
		case 20:
			sendNPCDialogue(SQUIRE, NORMAL, "Away you go then, the lander will take you back.");
			break;
		case 21:
			if (player.getControlerManager().getControler() instanceof PestControlGame) {
				((PestControlGame) player.getControlerManager().getControler()).getGame().leave(player, false);
				end();
				player.sendMessage("You leave the game..");
			}
			return;
		case 30:
			end();
			return;
		}
		stage++;
	}

	@Override
	public void finish() {}

}
