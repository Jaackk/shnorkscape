package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;

/**
 * Handles the Ellis's dialogue (Hide tanner).
 *
 * @author Noel
 */
public class EllisD extends Dialogue {

	private int npcId;

	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		sendNPCDialogue(npcId, NORMAL, "Greetings friend. I am a manufacturer of leather.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			sendOptionsDialogue("What would you like to say?", "Can I buy some leather then?",
					"Leather is rather weak stuff.");
			stage = 0;
			break;
		case 0:
			switch (componentId) {
			case OPTION_1:
				sendPlayerDialogue(WHY1, "Can I buy some leather then?");
				stage = 1;
				break;
			case OPTION_2:
				sendPlayerDialogue(CALM, "Leather is rather weak stuff.");
				stage = 2;
				break;
			}
			break;
		case 1:
			sendNPCDialogue(npcId, CALM, "I make leather from animal hides. Bring me some "
					+ "cowhides and 100 gold coins per hide, and I'll tan them into soft " + "leather for you.");
			stage = 6;
			break;
		case 2:
			sendNPCDialogue(npcId, NORMAL,
					"Normal leather may be quite weak, but it's very "
							+ "cheap - I make it from cowhides for only 100 gp per hide - and it's "
							+ "so easy to craft that anyone can work with it.");
			stage = 3;
			break;
		case 3:
			sendNPCDialogue(npcId, NORMAL,
					"Alternatively you could try hard leather. It's not "
							+ "so easy to craft, but I only charge 100 gp per cowhide to prepare it, "
							+ "and it makes much sturdier armour.");
			stage = 4;
			break;
		case 4:
			sendNPCDialogue(npcId, NORMAL, "I can also tan snake hides and dragonhides, suitable "
					+ "for crafting into the highest quality armour for rangers.");
			stage = 5;
			break;
		case 5:
			sendPlayerDialogue(NORMAL, "Thanks, I'll bear it in mind.");
			stage = 6;
			break;
		case 6:
			finish();
			break;
		}
	}

	@Override
	public void finish() {
		player.getInterfaceManager().closeChatBoxInterface();
	}
}