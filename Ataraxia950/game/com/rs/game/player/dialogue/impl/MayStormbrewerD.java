package com.rs.game.player.dialogue.impl;

import com.rs.game.item.Item;
import com.rs.game.player.Bank;
import com.rs.game.player.Skills;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

/**
 * Handles the dialogue for NPC 'May Stormbrewer'; Chronicle cash-in.
 *
 * @author Noel
 */
public class MayStormbrewerD extends Dialogue {

	/**
	 * An int representing the NPC ID we're talking to.
	 */
	int npcId;

	/**
	 * A Boolean representing if we're offering Chronicles to this NPC.
	 */
	boolean offer;

	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		offer = (Boolean) parameters[1];
		if (!offer)
			sendOptionsDialogue("Select an Option", "I'd like to talk about chronicles.",
					"I'd like to talk about something else.");
		else {
			if (getChronicles() > 0) {
				sendItemDialogue(29293, 1,
						"You exchanged " + getChronicles() + " chronicle fragments " + "for Divination EXP.");
				handleOffer();
			} else
				sendItemDialogue(29293, 1, "You don't have any chronicle fragments to hand in.");
			stage = 8;
		}
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			switch (componentId) {
			case OPTION_1:
				sendNPCDialogue(npcId, NORMAL, "Ah, chronicles are a rare find. As you may have seen, "
						+ "sometimes a chronicle will appear when harvesting a wisp.");
				stage = 0;
				break;
			case OPTION_2:
				sendNPCDialogue(npcId, TALK_SWING, "If you're looking for things to learn, I'd recommend "
						+ "you speak to Faizan. His research has been flourishing!");
				stage = 7;
				break;
			}
			break;
		case 0:
			sendNPCDialogue(npcId, TALK_SWING, "Chronicles can be thought of as particularly strong "
					+ "memories; powerful remnants of Guthix's life force.");
			stage = 1;
			break;
		case 1:
			sendPlayerDialogue(TALK_SWING, "Offer them, where?");
			stage = 2;
			break;
		case 2:
			sendNPCDialogue(npcId, TALK_SWING, "It must be somewhere with a powerful connection to Guthix.");
			player.sendMessage("Those who have spent at least 20$ on our ::store (with donator rank) may offer "
					+ "their chronicles for additional experience.");
			stage = 3;
			break;
		case 3:
			sendNPCDialogue(npcId, TALK_SWING, "So far, we have discovered two places. The one available "
					+ "to you is roght here in the crater.");
			stage = 4;
			break;
		case 4:
			sendNPCDialogue(npcId, TALK_SWING, "This area seems to be full of Guthix's residual energy. "
					+ "If you choose to, I can help you offer your chronicles here.");
			stage = 5;
			break;
		case 5:
			/** Slightly customizing the dialogue here **/
			sendNPCDialogue(npcId, TALK_SWING,
					"There's another area you can offer your chronicles that " + "will give you a greater reward, "
							+ (!player.isDonator() ? "but something tells me " + "you are not ready to go there yet"
									: "which is at the Donator zone")
							+ ".");
			stage = 6;
			break;
		case 6:
			sendPlayerDialogue(TALK_SWING, "Thanks.");
			stage = 8;
			break;
		case 7:
			sendNPCDialogue(npcId, NORMAL, "When you have a bit more experience, I'll be glad to share "
					+ "my research on the history of divination with you.");
			stage = 8;
			break;
		case 8:
			end();
			break;
		}
	}

	/**
	 * Checks if the player has any Chronicle fragments, if yes - how many.
	 *
	 * @param player
	 *            the Player offering.
	 * @param extra
	 *            if awarding extra XP.
	 */
	private int getChronicles() {
		int amount = 0;
		amount += player.getInventory().getNumberOf(29293);
		if (player.getBank().getItem(29293) != null)
			amount += player.getBank().getItem(29293).getAmount();
		return amount;
	}

	/**
	 * Handles Chronicle Fragment offering.
	 */
	private void handleOffer() {
		player.addChroniclesOffered(getChronicles());
		player.sendMessage("You've offered a total of " + Colors.RED
				+ Utils.getFormattedNumber(player.getChroniclesOffered()) + "</col> Chronicle Fragments.", true);
		if (!player.hasGuthixTitleUnlocked()) {
			if (player.getChroniclesOffered() >= 100) {
				player.unlockGuthixTitle();
				player.sendMessage("Congratulations! You've unlocked '" + Colors.GREEN
						+ "<shad=000000> of Guthix</shad></col>' Loyalty Title.");
			}
		}
		player.getSkills().addXp(Skills.DIVINATION, getXp());
		int inventory = player.getInventory().getNumberOf(29293);
		player.getInventory().deleteItem(29293, inventory);
		Item bankItem = player.getBank().getItem(29293);
		if (bankItem != null)
			player.getBank().removeItem(player.getBank().getItemSlot(bankItem), bankItem.getAmount(), true, Bank.DESTROY_ITEM);
	}

	/**
	 * Gets the amount of Divination Experience to reward.
	 *
	 * @return the amount of XP.
	 */
	private int getXp() {
		int exp = player.isDonator() ? 200 : 100;
		exp *= player.getPlane() == 1 ? 1.05 : 1;
		return getChronicles() * exp;
	}

	@Override
	public void finish() {
	}

}