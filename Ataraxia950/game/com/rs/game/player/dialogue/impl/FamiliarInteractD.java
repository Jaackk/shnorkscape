package com.rs.game.player.dialogue.impl;

import com.rs.game.npc.NPC;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.player.dialogue.Dialogue;

public class FamiliarInteractD extends Dialogue {

	private NPC npc;

	@Override
	public void start() {
		this.npc = (NPC) parameters[0];
		if (((Familiar) npc).getScrolls() == null)
			sendOptionsDialogue("Select an Option", "Talk-to", "Set special usage speed", "Permanently set special usage speed");
		else
			sendOptionsDialogue("Select an Option", "Talk-to", "Set special usage speed", "Permanently set special usage speed", "Remove scrolls");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			switch (componentId) {
			case OPTION_1:
				end();
				player.getDialogueManager().startDialogue("SimplePlayerMessage", "Why would I talk to a familiar? That's just weird.");
				break;
			case OPTION_2:
				sendOptionsDialogue("Select a Speed", "Slow", "Medium", "Fast");
				stage = 9;
				break;
			case OPTION_3:
				sendOptionsDialogue("Select a Speed", "Slow", "Medium", "Fast", "Remove permanent speed");
				stage = 19;
				break;
			case OPTION_4:
				if (player.getFamiliar() != null)
					player.getFamiliar().removeScrolls();
				end();
				break;
			}
			break;
		case 10:
			if (componentId == OPTION_1) {
				if (player.getFamiliar() != null)
					player.getFamiliar().setSpecialSpeed(1);
			} else if (componentId == OPTION_2) {
				if (player.getFamiliar() != null)
					player.getFamiliar().setSpecialSpeed(2);
			} else if (componentId == OPTION_3) {
				if (player.getFamiliar() != null)
					player.getFamiliar().setSpecialSpeed(3);
			}
			end();
			break;
		case 20:
			if (componentId == OPTION_1) {
				if (player.getFamiliar() != null)
					player.getFamiliar().setSpecialSpeed(1);
				player.alwaysUseSpecialSpeed = 1;
			} else if (componentId == OPTION_2) {
				if (player.getFamiliar() != null)
					player.getFamiliar().setSpecialSpeed(2);
				player.alwaysUseSpecialSpeed = 2;
			} else if (componentId == OPTION_3) {
				if (player.getFamiliar() != null)
					player.getFamiliar().setSpecialSpeed(3);
				player.alwaysUseSpecialSpeed = 3;
			} else if (componentId == OPTION_4)
				player.alwaysUseSpecialSpeed = 0;
			end();
			break;
		}
		stage++;
	}

	@Override
	public void finish() {

	}

}
