package com.rs.game.player.dialogue.impl.heartofgielinor;

import com.rs.game.player.content.HeartOfGielinor;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

public class IngressionFragmentD extends Dialogue {

	private int faction = -1;
	
	@Override
	public void start() {
		sendOptionsDialogue("Which faction would you like to reinforce?",
				"Sliske",
				"Zaros",
				"Zamorak",
				"Seren");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1) {
			switch(componentId) {
			case OPTION_1:
				faction = HeartOfGielinor.SLISKE;
				break;
			case OPTION_2:
				faction = HeartOfGielinor.ZAROS;
				break;
			case OPTION_3:
				faction = HeartOfGielinor.ZAMORAK;
				break;
			default:
				faction = HeartOfGielinor.SEREN;
				break;
			}
			sendOptionsDialogue("How many fragments would you like to use?",
					"Small summoning (100 fragments)(+5 rep)",
					(player.getHeart().getReputation(faction) < 250 ? Colors.RED : "") + "Large summoning (250 fragments)(+15 rep)");
		} else if (stage == 0) {
			final int amount = componentId == OPTION_1 ? 100 : 250;
			if (faction != -1) {
				if (player.getHeart().getReputation(faction) < 250 && amount == 250) {
					player.sendMessage("You need at least 250 reputation to summon large reinforcements.");
					end();
					return;
				}
				if (!player.getInventory().containsItem(37008, amount)) {
					end();
					player.sendMessage("You need at least " + amount + " ingression fragments to summon reinforcements.");
					return;
				}
				player.getHeart().summonReinforcements(faction, amount == 100);
			}
			end();
		}
		stage++;
	}

	@Override
	public void finish() {
		
	}

}
