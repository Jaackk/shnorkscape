package com.rs.game.player.dialogue.impl;

import com.rs.game.npc.NPC;
import com.rs.game.player.controllers.RunespanController;
import com.rs.game.player.dialogue.Dialogue;

public class WizardFinix extends Dialogue {

	private NPC npc;

	@Override
	public void start() {
		npc = (NPC) parameters[1];
		sendNPCDialogue(npc.getId(), HAPPY_FACE, "Hello. How can I help you?");
		stage = 0;
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case 0:
			sendOptionsDialogue("Select an Option",
					"Hello. Who are you?",
					"What can I do here?",
					"Can I have some runes?",
					"Can you help me find the yellow wizard?",
					"Can you teleport me back to the Wizards' Tower?");
			stage = 1;
			break;
		case 1:
			if (componentId == OPTION_1) {
				sendNPCDialogue(npc.getId(), HAPPY_FACE, "I'm Wizard Finix. I study runic energy here in the Runespan.");
				stage = 2;
			} else if (componentId == OPTION_2) {
				sendNPCDialogue(npc.getId(), HAPPY_FACE, "Collect essence, siphon creatures and energy nodes into runes, then use those runes to cross platforms. When you leave, your remaining runes become Runespan points.");
				stage = 2;
			} else if (componentId == OPTION_3) {
				if (player.getControlerManager().getControler() instanceof RunespanController)
					((RunespanController) player.getControlerManager().getControler()).exchangeStarterRunes();
				end();
			} else if (componentId == OPTION_4) {
				if (player.getControlerManager().getControler() instanceof RunespanController)
					((RunespanController) player.getControlerManager().getControler()).showYellowWizardHint();
				end();
			} else if (componentId == OPTION_5) {
				player.getDialogueManager().startDialogue(RuneSpanLeaving.class.getSimpleName());
			}
			break;
		case 2:
			end();
			break;
		}
	}

	@Override
	public void finish() {
		player.getInterfaceManager().closeChatBoxInterface();
	}
}
