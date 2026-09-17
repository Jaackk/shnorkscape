package com.rs.game.player.dialogue.impl;

import com.rs.game.player.actions.fletching.Fletching;
import com.rs.game.player.content.dungeoneering.rooms.PuzzleRoom;
import com.rs.game.player.content.dungeoneering.rooms.puzzles.FremennikCampRoom;
import com.rs.game.player.content.dungeoneering.skills.smithing.DungeoneeringSmithing;
import com.rs.game.player.dialogue.Dialogue;

public class FremennikScoutD extends Dialogue {

	@Override
	public void start() {
		PuzzleRoom room = (PuzzleRoom) parameters[0];
		if (room.isComplete()) 
			sendNPCDialogue(FremennikCampRoom.FREMENNIK_SCOUT, NORMAL, "Wonderful! That was the last of them. As promised, I'll unlock the door for you.");
		else {
			sendNPCDialogue(FremennikCampRoom.FREMENNIK_SCOUT, NORMAL, "Need some tools?");
			stage = 1;
		}
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == 1) {
			if (!player.getInventory().containsItem(DungeoneeringSmithing.DUNGEONEERING_HAMMER, 1) && !player.getDungeoneeringToolbelt().containsTool(DungeoneeringSmithing.DUNGEONEERING_HAMMER)) {
				player.getInventory().addItem(DungeoneeringSmithing.DUNGEONEERING_HAMMER, 1);
			}
			if (!player.getInventory().containsItem(Fletching.DUNGEONEERING_KNIFE, 1) && !player.getDungeoneeringToolbelt().containsTool(Fletching.DUNGEONEERING_KNIFE)) {
				player.getInventory().addItem(Fletching.DUNGEONEERING_KNIFE, 1);
			}
			stage = 100;
		}
			end();
	}

	@Override
	public void finish() {

	}

}
