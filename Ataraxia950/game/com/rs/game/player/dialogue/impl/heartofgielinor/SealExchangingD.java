package com.rs.game.player.dialogue.impl.heartofgielinor;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.player.Player;
import com.rs.game.player.content.HeartOfGielinor;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.InputIntegerEvent;

public class SealExchangingD extends Dialogue {

	private static final int[] SEALS = new int[] { 37103, 37101, 37100, 37102 };
	private static final int[] CHOSENS = new int[] { 22435, 22433, 22436, 22434 };
	
	private int itemId, npcId, index, god;
	
	@Override
	public void start() {
		this.itemId = (int) parameters[0];
		this.npcId = (int) parameters[1];
		for (int i = 0; i < SEALS.length; i++) {
			if (SEALS[i] == itemId) 
				index = i;
			else if (CHOSENS[i] == npcId) 
				god = i;
		}
		if (npcId == CHOSENS[index]) {
			sendNPCDialogue(CHOSENS[god], NORMAL, "You cannot exchange the " + ItemDefinitions.getItemDefinitions(SEALS[index]).getName() + " with the " + HeartOfGielinor.getGod(index) + " faction!");
			stage = 100;
		} else
			sendItemDialogue(itemId, 1, "How many seals do you wish to exchange with the " + HeartOfGielinor.getGod(god) + " faction?");

	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch(stage) {
		case -1:
			player.sendInputInteger("How many seals would you like to exchange?", new InputIntegerEvent() {
				@Override
				public void run(Player player) {
					int value = this.getInteger();
					if (value <= 0 || player.getInventory().getAmountOf(SEALS[index]) == 0) 
						return;
					if (value > player.getInventory().getAmountOf(SEALS[index])) 
						value = player.getInventory().getAmountOf(SEALS[index]);
					player.getInventory().deleteItem(SEALS[index], value);
					player.getHeart().setReputation(god, player.getHeart().getReputation(god) + (value * 5));
					sendNPCDialogue(CHOSENS[god], NORMAL, "Thank you for the seals. You've earned " + (value * 5) + " reputation with the " + HeartOfGielinor.getGod(god) + " faction. You now have " + player.getHeart().getReputation(god) + " reputation with " + HeartOfGielinor.getGod(god) + " faction!");
					stage = 100;
				}
			});
			return;
		default:
			end();
			return;
		}
	}

	@Override
	public void finish() {
		
	}

}
