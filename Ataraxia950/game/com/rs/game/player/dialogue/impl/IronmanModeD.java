package com.rs.game.player.dialogue.impl;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.SerializableFilesManager;

public class IronmanModeD extends Dialogue{

	private String choice;
	private final String[] modes = {
			"Novice Ironman", "Intermediate Ironman", "Expert Ironman"
	};
	
	@Override
	public void start() {
		if(player.isGroupIronman()) {
			player.sendMessage("Group Ironmen cannot change their game mode!");
			return;
		}
		if (!player.isIronMan()) {
			player.sendMessage("You have to be x5 Ironman to change mode!");
			return;
		}
		sendOptionsDialogue("What Ironman mode would you like?",
				"Novice Ironman - 100x XP - +0% drop rate",
				"Intermediate Ironman - 25x XP - +5% drop rate",
				"Expert Ironman - 5x XP - +10% drop rate",
				"Nevermind");
		stage = 0;
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch(stage) {
		case 0:
			if (componentId == OPTION_5)
			    end();
			else {
				if (!player.getEquipment().wearingArmour()) {
					int option = getOrdinal(componentId);
					sendDialogue(Colors.RED+Colors.SHAD+"WARNING: To become "+
							(Colors.GREEN+modes[option])+" mode"+Colors.RED+
							" you will never be able to change back!");
					choice = modes[option];
					stage = 1;
				} else {
					sendDialogue(Colors.RED+"You cannot be wearing armor when doing this!");
					stage = 3;
				}
			}
			break;
		case 1:
			sendOptionsDialogue(Colors.WHITE+"Are you sure you want to change Ironman modes?",
					Colors.GREEN+Colors.SHAD+"Yes", Colors.RED+Colors.SHAD+"No");
			stage = 2;
			break;
		case 2:
			if (componentId == OPTION_1)
				switchMode();
			end();
			break;
		case 3:
		    end();
			break;
		}
	}

	@Override
	public void finish() {
		player.getInterfaceManager().closeChatBoxInterface();
	}
	
	public void switchMode() {
		switch(choice) {
		case "Novice Ironman":
			player.setNoviceIronMan(true);
			player.setIntermediateIronMan(false);
			player.setExpertIronMan(false);
			player.setIronMan(false);
			break;
		case "Intermediate Ironman":
			player.setNoviceIronMan(false);
			player.setIntermediateIronMan(true);
			player.setExpertIronMan(false);
			player.setIronMan(false);
			break;
		case "Expert Ironman":
			player.setNoviceIronMan(false);
			player.setIntermediateIronMan(false);
			player.setExpertIronMan(true);
			player.setIronMan(false);
			break;
		}
		player.setHCIronMan(false);
		player.endKingOfTheSkillGameMode();
		SerializableFilesManager.savePlayer(player);
		player.sendMessage(Colors.GREEN+"You have switched your game mode to: "+
				(Colors.GREEN+Colors.SHAD+choice)+"</col></shad>!");
	}
}
