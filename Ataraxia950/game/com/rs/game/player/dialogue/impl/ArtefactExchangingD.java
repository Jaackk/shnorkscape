package com.rs.game.player.dialogue.impl;

import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.actions.ancientartefacts.Artefact;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

public class ArtefactExchangingD extends Dialogue {

	private static final int NPC = 6539;
	
	@Override
	public void start() {
		sendNPCDialogue(NPC, NORMAL, "I see you have some artefacts there with you. Would you like to sell me them for some cash?");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1) {
			sendOptionsDialogue("Sell your artefacts for " + Utils.formatNumber(getCostOfArtefacts(player)) + "?",
						"Sell them.",
						"Keep them.");
		} else {
			if (componentId == OPTION_1) {
				final int amount = getCostOfArtefacts(player);
				loop : for (int i = 0; i < 28; i++) {
					com.rs.game.item.Item item = player.getInventory().getItem(i);
					if (item == null)
						continue;
					for (Artefact artefact : Artefact.values())
						if (item.getId() == artefact.getId() && item.getCharges() == 0) {
							player.getInventory().deleteItem(i, item);
							continue loop;
						}
				}
				if (((long)amount + player.getInventory().getAmountOf(995)) < Integer.MAX_VALUE)
					player.getInventory().addItem(995, amount);
				else {
					int remaining = 0;
					remaining = amount - (Integer.MAX_VALUE - player.getInventory().getAmountOf(995));
					if (Integer.MAX_VALUE - player.getInventory().getAmountOf(995) != 0)
					player.getInventory().addItem(995, Integer.MAX_VALUE - player.getInventory().getAmountOf(995));
					if (remaining > 0) {
						World.addGroundItem(new com.rs.game.item.Item(995, remaining), new WorldTile(player), player, true, 180);
						player.getPackets().sendPlayerMessage(1, 15263739, Colors.RED + "WARNING: Some of the coins have been dropped on the floor due to overflow.", true);
					}
				}
			} else {
				end();
				player.getDialogueManager().startDialogue("NastrothD", 6539);
				return;
			}
			end();
		}
		stage++;
	}

	@Override
	public void finish() {
	
	}
	
	public static final int getCostOfArtefacts(final Player player) {
		int cost = 0;
		loop : for (com.rs.game.item.Item item : player.getInventory().getItems().getItems()) {
			if (item == null)
				continue;
			for (Artefact artefact : Artefact.values())
				if (item.getId() == artefact.getId() && item.getCharges() == 0) {
					cost += artefact.getPrice();
					continue loop;
				}
		}
		return cost;
	}
	
}
