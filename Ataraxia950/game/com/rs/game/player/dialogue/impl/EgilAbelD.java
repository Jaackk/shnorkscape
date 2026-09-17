package com.rs.game.player.dialogue.impl;

import com.rs.game.player.Skills;
import com.rs.game.player.content.artisansworkshop.ArtisansWorkShop.CeremonialSwordPlan;
import com.rs.game.player.dialogue.Dialogue;

public class EgilAbelD extends Dialogue {

	private int npcId;

	@Override
	public void start() {
		npcId = (int) parameters[0] + 1;
		sendNPCDialogue(npcId, NORMAL, "Hello there, how can I help you?");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		CeremonialSwordPlan currentPlan = player.getArtisansWorkShop().getCurrentPlan();
		switch (stage) {
		case -1:
			stage = 0;
			sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, currentPlan == null
					? (hasDesign() ? "Can i get a different sword design?" : "Can I start making a ceremonial sword?")
					: "Can you score my sword?", "Nevermind.");
			break;
		case 0:
			if (componentId == OPTION_2) {
				end();
				return;
			}
			if (currentPlan == null) {
				stage = 1;
				sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Take iron sword design", "Take steel sword design",
						"Take mithril sword design", "Take adamant sword design", "Take rune sword design");
			} else {
				end();
				if (currentPlan.isShattered()) {
					player.getDialogueManager().startDialogue("SimpleNPCMessage", npcId,
							"You receive no experience for breaking the sword.");
					player.getArtisansWorkShop().setCurrentPlan(null);
					return;
				}

				double xp = currentPlan.getXp();
				int performance = currentPlan.getPerformance();
				if (!currentPlan.isPerfect())
					xp -= 0.2 * xp;
				xp *= ((double) performance / 100.00);
				if (xp <= 0) {
					player.getDialogueManager().startDialogue("SimpleNPCMessage", npcId,
							"You receive no experience for creating such a low performance sword.");
					player.getArtisansWorkShop().setCurrentPlan(null);
					return;
				}
				if (performance >= 90) {
					if (!player.getArtisansWorkShop().isFirstTime90()) {
						player.getArtisansWorkShop().setFirstTime90(true);
						player.getSkills().addXp(Skills.SMITHING, 5000);
						player.getPackets().sendGameMessage(
								"You receive bonus xp for creating a 90%+ performance sword for the first time.");
					}
					if (performance == 100 && !player.getArtisansWorkShop().isFirstTime100()) {
						player.getArtisansWorkShop().setFirstTime100(true);
						player.getSkills().addXp(Skills.SMITHING, 15000);
						player.getPackets().sendGameMessage(
								"You receive bonus xp for creating a 100% performance sword for the first time.");
					}
				}
				player.getArtisansWorkShop().increaseArtisansXPGained((int) xp);
				player.getSkills().addXp(Skills.SMITHING, xp);
				player.getDialogueManager().startDialogue("SimpleNPCMessage", npcId,
						currentPlan.isPerfect()
								? "For producing a perfect sword, you are awarded 120% of the normal experience. Excellent work!"
								: "Your sword performance was " + performance + "%. Not bad! ");
				player.getArtisansWorkShop().setCurrentPlan(null);
			}
			break;
		case 1:
			end();
			if (hasDesign())
				for (int i = 0; i < 5; i++)
					player.getInventory().deleteItem(20560 + i, 1);
			if (player.getInventory().getFreeSlots() == 0) {
				player.getDialogueManager().startDialogue("SimpleNPCMessage", npcId,
						"You don't have enough space in your inventory.");
				return;
			}
			int type = getOrdinal(componentId);
			player.getInventory().addItem(20560 + type, 1);
			player.getDialogueManager().startDialogue("SimpleNPCMessage", npcId,
					"Hmm, I'll have a look. Ah, yes, I'm commissioning "
							+ (type == 0 ? "an iron"
									: type == 1 ? "a steel"
											: type == 2 ? "a mithril" : type == 3 ? "an adamant" : "a rune")
							+ " sword for a goblin general. Here are the plans.");
			break;
		}

	}

	private boolean hasDesign() {
		for (int i = 0; i < 5; i++)
			if (player.getInventory().containsItem(20560 + i, 1))
				return true;
		return false;
	}

	@Override
	public void finish() {

	}

}
