package com.rs.game.player.dialogue.impl;

import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.dialogue.Dialogue;

/**
 * Handles all the Heralds around the city of Prifddinas.
 *
 * @author Noel
 */
public class PrifddinasHerald extends Dialogue {

	/**
	 * Represents the Herald NPC.
	 */
	private NPC herald;

	/**
	 * Representes the NPC Option clicked as byte.
	 */
	private byte option;

	@Override
	public void start() {
		herald = (NPC) parameters[0];
		option = (Byte) parameters[1];
		switch (option) {
		case 1:
			sendNPCDialogue(herald.getId(), CALM, "Greetings.");
			stage = -1;
			break;
		case 2:
			sendPlayerDialogue(CROOKED_HEAD, "Could I buy your clan's cape?");
			stage = 3;
			break;
		case 3:
			sendOptionsDialogue("Purchase Seren's symbol for 1'000'000 coins?", "Yes.", "No.");
			stage = 9;
			break;
		}
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			sendPlayerDialogue(CROOKED_HEAD, "Where am I?");
			stage = 0;
			break;
		case 0:
			stage = 1;
			switch (herald.getId()) {
			case 19862:
				sendNPCDialogue(herald.getId(), NORMAL,
						"As you've probably DIVINED...you're in the heart of Clan Amlodd, "
								+ "masters of divination and summoning. I crack myself up sometimes.");
				break;
			case 19857:
				sendNPCDialogue(herald.getId(), NORMAL,
						"Why you find yourself amongst the Clan Cadarn friend. We are the "
								+ "noble protectors of elf kind, protecting the secrets of elven magic and archery.");
				break;
			case 19858:
				sendNPCDialogue(herald.getId(), NORMAL, "Around you are the members of the Clan Crwys, tending to the "
						+ "plants and trees within the city.");
				break;
			case 19863:
				sendNPCDialogue(herald.getId(), NORMAL, "Breathe in the calm air of serenity. Here Clan Hefin purified "
						+ "themselves in honour of Seren herself.");
				break;
			case 19856:
				sendNPCDialogue(herald.getId(), NORMAL, "Here you will find the great Clan Iorwerth, masters of melee "
						+ "combat and slayers of monsters.");
				break;
			case 19860:
				sendNPCDialogue(herald.getId(), NORMAL,
						"You are at the heart of elven craftsmanship, amongst the Ithell Clan.");
				break;
			case 19861:
				sendNPCDialogue(herald.getId(), NORMAL, "A noble explorer I see! Welcome to the Meilyr district.");
				break;
			case 19859:
				sendNPCDialogue(herald.getId(), NORMAL,
						"Toiling around you are the members of Clan Trahaearn, the miners and "
								+ "blacksmiths of elven kind.");
				break;
			}
			break;
		case 1:
			sendOptionsDialogue("Select an Option", "Could I buy your clan's cape?",
					"Do you have anything else I can buy?");
			stage = 2;
			break;
		case 2:
			switch (componentId) {
			case OPTION_1:
				sendPlayerDialogue(CROOKED_HEAD, "Could I buy your clan's cape?");
				stage = 3;
				break;
			case OPTION_2:
				sendPlayerDialogue(CROOKED_HEAD, "Do you have anything else I can buy?");
				stage = 6;
				break;
			}
			break;
		case 3:
			sendNPCDialogue(herald.getId(), NORMAL, "Certainly. That'll cost you 100'000 coins, though.");
			stage = 4;
			break;
		case 4:
			sendOptionsDialogue("Buy Clan cape for 100'000 coins?", "Yes.", "No.");
			stage = 5;
			break;
		case 5:
			stage = 99;
			switch (componentId) {
			case OPTION_1:
				if (player.hasMoney(100000) && getClanCape(herald) != -1) {
					player.takeMoney(100000);
					sendItemDialogue(getClanCape(herald), 1, herald.getName() + " hands you a cape.");
					player.addItem(new Item(getClanCape(herald)));
				} else
					sendPlayerDialogue(SAD, "Oh, it looks like I don't have enough coins. I'll be back later..");
				break;
			case OPTION_2:
				sendPlayerDialogue(CALM, "No thank you, I'll pass.");
				break;
			}
			break;
		case 6:
			sendNPCDialogue(herald.getId(), CALM,
					"There is one thing. An amulet, with Seren's symbol. This has been "
							+ "handed down for generations. It was said that when the clans were brought together its true "
							+ "nature would be revealed.");
			stage = 7;
			break;
		case 7:
			sendNPCDialogue(herald.getId(), CALM, "We believed this would occur as the elders returned and Prifddinas "
					+ "grew. Alas, this was not the case. Perhaps we are missing something, or perhaps it's just a story. "
					+ "In any case, we're willing to sell the amulet and allow others to attempt to unlock its secrets.");
			stage = 8;
			break;
		case 8:
			sendOptionsDialogue("Purchase Seren's symbol for 1'000'000 coins?", "Yes.", "No.");
			stage = 9;
			break;
		case 9:
			stage = 99;
			switch (componentId) {
			case OPTION_1:
				if (player.hasMoney(1000000)) {
					player.takeMoney(1000000);
					sendItemDialogue(32744, 1, herald.getName() + " hands you a Symbol of Seren.");
					player.addItem(new Item(32744));
				} else
					sendPlayerDialogue(SAD, "Oh, it looks like I don't have enough coins. I'll be back later..");
				break;
			case OPTION_2:
				sendPlayerDialogue(CALM, "No thank you, I'll pass.");
				break;
			}
			break;
		case 99:
			end();
			break;
		}
	}

	@Override
	public void finish() {
	}

	/**
	 * Gets the appropriate Item ID of the Clan cape for each Herald.
	 *
	 * @param herald
	 *            The NPC.
	 * @return the Clan cape Item ID.
	 */
	private int getClanCape(NPC herald) {
		if (herald.getId() == 19862) // Amlodd Herald
			return 32198;
		if (herald.getId() == 19857) // Cadarn Herald
			return 32199;
		if (herald.getId() == 19858) // Crwys Herald
			return 32200;
		if (herald.getId() == 19863) // Hefin Herald
			return 32201;
		if (herald.getId() == 19856) // Iorwerth Herald
			return 32202;
		if (herald.getId() == 19860) // Ithell Herald
			return 32203;
		if (herald.getId() == 19861) // Meilyr Herald
			return 32204;
		if (herald.getId() == 19859) // Trahaearn Herald
			return 32205;
		return -1;
	}
}