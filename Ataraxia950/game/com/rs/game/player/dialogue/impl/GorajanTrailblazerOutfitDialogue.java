package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;

public class GorajanTrailblazerOutfitDialogue extends Dialogue {

	private int tier;
	
	@Override
	public void start() {
		sendOptionsDialogue("Select an Outfit", 
				"Frozen", 
				"Furnished", 
				"Abandoned", 
				"Occult", 
				"Warped");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1) {
			switch(componentId) {
			case OPTION_1:
				tier = 0;
				break;
			case OPTION_2:
				tier = 1;
				break;
			case OPTION_3:
				tier = 2;
				break;
			case OPTION_4:
				tier = 3;
				break;
			case OPTION_5:
				tier = 4;
				break;
			}
			sendOptionsDialogue("Select the piece you wish to override", 
					getPrefix(0) + "Head", 
					getPrefix(1) + "Body", 
					getPrefix(2) + "Legs", 
					getPrefix(3) + "Gloves", 
					getPrefix(4) + "Boots");
		} else {
			switch(componentId) {
			case OPTION_1:
				if (player.getGorajanTrailblazer().getPiece(tier, 0) == 1) 
					player.getGorajanTrailblazer().setPieceActive(tier, 0, true);
				else if (player.getGorajanTrailblazer().getPiece(tier, 0) == 2)
					player.getGorajanTrailblazer().setPieceActive(tier, 0, false);
				break;
			case OPTION_2:
				if (player.getGorajanTrailblazer().getPiece(tier, 1) == 1) 
					player.getGorajanTrailblazer().setPieceActive(tier, 1, true);
				else if (player.getGorajanTrailblazer().getPiece(tier, 1) == 2)
					player.getGorajanTrailblazer().setPieceActive(tier, 1, false);
				break;
			case OPTION_3:
				if (player.getGorajanTrailblazer().getPiece(tier, 2) == 1) 
					player.getGorajanTrailblazer().setPieceActive(tier, 2, true);
				else if (player.getGorajanTrailblazer().getPiece(tier, 2) == 2)
					player.getGorajanTrailblazer().setPieceActive(tier, 2, false);
				break;
			case OPTION_4:
				if (player.getGorajanTrailblazer().getPiece(tier, 3) == 1) 
					player.getGorajanTrailblazer().setPieceActive(tier, 3, true);
				else if (player.getGorajanTrailblazer().getPiece(tier, 3) == 2)
					player.getGorajanTrailblazer().setPieceActive(tier, 3, false);
				break;
			case OPTION_5:
				if (player.getGorajanTrailblazer().getPiece(tier, 4) == 1) 
					player.getGorajanTrailblazer().setPieceActive(tier, 4, true);
				else if (player.getGorajanTrailblazer().getPiece(tier, 4) == 2)
					player.getGorajanTrailblazer().setPieceActive(tier, 4, false);
				break;
			}
			sendOptionsDialogue("Select the piece you wish to override", 
					getPrefix(0) + "Head", 
					getPrefix(1) + "Body", 
					getPrefix(2) + "Legs", 
					getPrefix(3) + "Gloves", 
					getPrefix(4) + "Boots");
		}
		stage++;
	}
	
	private final String getPrefix(int slot) {
		int piece = player.getGorajanTrailblazer().getPiece(tier, slot);
		return piece == 0 ? "<col=ff0000>" : piece == 1 ? "<col=ffff00>" : "<col=00ff00>";
	}

	@Override
	public void finish() {
		
	}

}
