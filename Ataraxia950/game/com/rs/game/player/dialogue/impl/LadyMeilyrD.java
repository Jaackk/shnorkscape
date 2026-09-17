package com.rs.game.player.dialogue.impl;

import com.rs.game.player.content.items.MeilyrRecipes;
import com.rs.game.player.dialogue.Dialogue;

public class LadyMeilyrD extends Dialogue {

	private int option;

	@Override
	public void start() {
		option = (int) parameters[0];
		if (option == 1) {
			sendNPCDialogue(20286, NORMAL, "Hello, " + player.getDisplayName()
					+ ", are you interested in buying recipes for the combination potions?");
			stage = 1;
		} else if (option == 2) {
            end();
            MeilyrRecipes.openRecipeShop(player);
		}
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage){
		case 1:
			sendOptionsDialogue("Select an option", "Yes.", "No.");
			stage = 2;
			break;
		case 2:
			switch (componentId){
			case OPTION_1:
	            end();
	            MeilyrRecipes.openRecipeShop(player);
				break;
			case OPTION_2:
				end();
				break;
			}
			break;
		}

	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub

	}

}
