package com.rs.game.player.dialogue.impl;

import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.dialogue.Dialogue;

/**
 * Handles Prifddinas Cities Lady Ithell's dialogue.
 *
 * @author Noel
 */
public class LadyIthell extends Dialogue {

	/**
	 * Integer representing Lady Ithell's NPC ID.
	 */
	private int npcId;
	private boolean seed;

	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		sendNPCDialogue(npcId, NORMAL, "It's wonderful, isn't it? The crystal spires of Prifddinas..it's more "
				+ "beautiful than I could've dreamed. The stories didn't do it justice.");
		seed = (Boolean) parameters[1];
		if (seed) {
			sendItemDialogue(32625, 1, "", "Lady Ithell gives you a free attuned crystal seed with which to"
					+ " show your dedication to the elves.");
			player.getInventory().addItem(32625, 1);
			player.receivedSeed = true;
			stage = 38;
		} else
			sendNPCDialogue(npcId, NORMAL, "Good day, " + player.getDisplayName());
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			sendOptionsDialogue("Select an Option", "Ask about Lady Ithell and her clan...",
					"Can you make me a crystal hatchet or pickaxe?", "Nothing, thanks.");
			stage = 0;
			break;
		case 0:
			switch (componentId) {
			case OPTION_1:
				sendOptionsDialogue("Select an Option", "Ask about Lady Ithell...", "Ask about Clan Ithell...",
						"Ask about Ithell skills...", "I have other questions...");
				stage = 1;
				break;
			case OPTION_2:
				sendNPCDialogue(npcId, CALM,
						"Hmm... They're rather large tools, but in principle it'd be "
								+ "no different from attuning, say, a crystal halberd. It would degrade to a seed "
								+ "like other tools, of course.");
				stage = 31;
				break;
			case OPTION_3:
				end();
				break;
			}
			break;
		case 1:
			switch (componentId) {
			case OPTION_1:
				sendPlayerDialogue(GLANCE_DOWN, "Can you tell me about yourself?");
				stage = 2;
				break;
			case OPTION_2:
				sendPlayerDialogue(GLANCE_DOWN, "Can you tell me about Clan Ithell?");
				stage = 20;
				break;
			case OPTION_3:
				sendPlayerDialogue(GLANCE_DOWN, "Can you tell me about Clan Ithell's skills?");
				stage = 27;
				break;
			case OPTION_4:
				sendOptionsDialogue("Select an Option", "Ask about Lady Ithell and her clan...",
						"Can you make me a crystal hatchet or pickaxe?", "Is there anything I can do for you?",
						"Is there anything else I can do for you?", "Nothing, thanks.");
				stage = 0;
				break;
			}
			break;
		case 2:
			sendNPCDialogue(npcId, GLANCE_DOWN, "Have you forgotten me already? I used to live in Lletya. "
					+ "I was just a day-dreaming young elf, with no idea of the destiny that awaited me.");
			stage = 3;
			break;
		case 3:
			sendNPCDialogue(npcId, CALM, "You built a statue of the former Lady Ithell, and when I "
					+ "gazed at her beauty, it spoke to me. I felt lady Ithell's memories enter my mind.");
			stage = 4;
			break;
		case 4:
			sendNPCDialogue(npcId, NORMAL,
					"The crystal spires...the delicate harmonies...the recreation "
							+ "of those harmonies in wood! I saw how she did it, I gained her knowledge, and I "
							+ "became the new Lady Ithell.");
			stage = 5;
			break;
		case 5:
			sendOptionsDialogue("Select an Option", "What happened with you and the statue?",
					"If you're Lady Ithell, what happened to Kelyn?", "Why didn't you ever build that statue yourself?",
					"How are you coping with your new role?", "I have other questions...");
			stage = 6;
			break;
		case 6:
			switch (componentId) {
			case OPTION_1:
				sendNPCDialogue(npcId, NORMAL, "The Riddle of the Lost Elders was literally true - Lady "
						+ "Ithell put her soul into her greatest work.");
				stage = 7;
				break;
			case OPTION_2:
				sendNPCDialogue(npcId, NORMAL,
						"I'm still Kelyn. I'm not possessed - my memories haven't " + "been changed.");
				stage = 10;
				break;
			case OPTION_3:
				sendNPCDialogue(npcId, NORMAL,
						"I must have walked past those plans on the walls every "
								+ "day for years, and never noticed them. Sometimes it takes an outsider's "
								+ "eye to see details.");
				stage = 12;
				break;
			case OPTION_4:
				sendNPCDialogue(npcId, SAD,
						"I..I'm not coping very well, to be honest. My head feels "
								+ "like a waterskin that's full to bursting. Sometimes I get these headaches, "
								+ "and when I sleep, I have frightening dreams.");
				stage = 17;
				break;
			case OPTION_5:
				sendOptionsDialogue("Select an Option", "Ask about Lady Ithell...", "Ask about Clan Ithell...",
						"Ask about Ithell skills...", "I have other questions...");
				stage = 1;
				break;
			}
			break;
		case 7:
			sendNPCDialogue(npcId, NORMAL,
					"She took part of her soul - the essential truths about who "
							+ "she was, and what made her Lady Ithell - and encoded them in the statue plans "
							+ "you found around Lletya.");
			stage = 8;
			break;
		case 8:
			sendNPCDialogue(npcId, NORMAL,
					"I'm a member of Clan Ithell, with a love of architecture. "
							+ "When I saw the statue, that sole spoke to me..I mean, spoke in the same way a "
							+ "work of art speaks to someone who looks at it.");
			stage = 9;
			break;
		case 9:
			sendNPCDialogue(npcId, NORMAL, "I gained the essential knowledge of the leader of Clan Ithell. "
					+ "I am the new Lady Ithell - I sang her part of the city into shape!");
			stage = 5;
			break;
		case 10:
			sendNPCDialogue(npcId, NORMAL,
					"But I have some new knowledge as well - abilities that the old "
							+ "Lady Ithell encoded into her statue plans. The knowledge I need to be the new "
							+ "Lady Ithell.");
			stage = 11;
			break;
		case 11:
			sendNPCDialogue(npcId, SAD, "Well, some of it, anyway. My head is full of chants and schematics "
					+ "and songs, and I'm still trying to make sense of it all.");
			stage = 5;
			break;
		case 12:
			sendNPCDialogue(npcId, SAD,
					"I doubted I could build the statue like you could. It took a "
							+ "master of construction to properly express all the details that made up Lady "
							+ "Ithell's soul.");
			stage = 13;
			break;
		case 13:
			sendNPCDialogue(npcId, CALM, "So, I set myself a task when I arrived in prifddinas. I tried "
					+ "to replicate your statue here.");
			stage = 14;
			break;
		case 14:
			sendNPCDialogue(npcId, SAD, "I don't think it's as good as yours, to be honest. I have "
					+ "Lady Ithell's knowledge, but talent isn't so easily inherited.");
			stage = 15;
			break;
		case 15:
			sendNPCDialogue(npcId, CALM, "I'll keep working at it. I want to have an image of Lady Ithell "
					+ "here with me, to watch over me, and help me remember her legacy.");
			stage = 16;
			break;
		case 16:
			sendNPCDialogue(npcId, NORMAL,
					"At least it's better than my first attempt. I wish the clan "
							+ "hadn't made that little statue a centrepiece of the harmonium. It's awful... "
							+ "the clumsy work of an amateur chanter, but I learned a lot from singing it.");
			stage = 5;
			break;
		case 17:
			sendNPCDialogue(npcId, GLANCE_DOWN,
					"The clan looks to me for guidance. They expect me to "
							+ "have Lady Ithell's wisdom, but my nose bleeds when I try too hard to understand "
							+ "her memories.");
			stage = 18;
			break;
		case 18:
			sendNPCDialogue(npcId, GLANCE_DOWN,
					"I wonder..did Lady Ithell choose me, or was I just " + "in the right place at the right time?");
			stage = 19;
			break;
		case 19:
			sendNPCDialogue(npcId, NORMAL,
					"I'm probably too young to be an elder, but I'm going to " + "do the best job I can.");
			stage = 5;
			break;
		case 20:
			sendNPCDialogue(npcId, CALM, "We're most well-known for our ability to sing crystal seeds "
					+ "- tiny, precious fragments of Seren herself - into new and glorious shapes.");
			stage = 21;
			break;
		case 21:
			sendNPCDialogue(npcId, SAD, "Quite a lot of these gifts from Seren were kept safe on "
					+ "Tarddiad, the true home of the elves.");
			stage = 22;
			break;
		case 22:
			sendNPCDialogue(npcId, NORMAL, "Thanks to you we now have a chance to go back there and "
					+ "recover them. Something we thought impossible after the Second Age.");
			stage = 23;
			break;
		case 23:
			sendNPCDialogue(npcId, CALM,
					"Guthix established his edicts after the God Wars of the "
							+ "Third Age, to protect Gielinor from the gods. Seren didn't want to be seperated "
							+ "from us, so to share her power with the elves, Seren shattered her body.");
			stage = 24;
			break;
		case 24:
			sendNPCDialogue(npcId, CALM, "These crystal seeds come from Seren. By vocalising at a "
					+ "pitch beyond human hearing, and by varying tone, a crystal chanter can "
					+ "change the shape and size of a crystal seed. Gifted chanters can even imbue " + "powers into");
			stage = 25;
			break;
		case 25:
			sendNPCDialogue(npcId, CALM, "these objects.");
			stage = 26;
			break;
		case 26:
			sendNPCDialogue(npcId, CALM, "Crystal singing isn't limited to the ithell clan, but more "
					+ "crystal chanters are born into our family than any of the other clans.");
			stage = 38;
			break;
		case 27:
			sendNPCDialogue(npcId, CALM,
					"We are the architects of Prifddinas, responsible for its " + "construction and maintenance.");
			stage = 28;
			break;
		case 28:
			sendNPCDialogue(npcId, CALM,
					"Most of the city's infrastructure was encoded in the Grand "
							+ "Library, but through crystal singing we shape the spires, buildings and walkways, "
							+ "as needed by the clans.");
			stage = 29;
			break;
		case 29:
			sendNPCDialogue(npcId, NORMAL,
					"We also chant crystal seeds into tools, instruments, and "
							+ "even weapons. I suppose it's roughly equivalent to what humans call 'crafting'.."
							+ "though our equipment is much prettier.");
			stage = 38;
			break;
		case 31:
			sendPlayerDialogue(CALM, "That's not good enough. I want a tool that I can store in my tool "
					+ "belt, never degrades, and is better than a dragon hatchet or pickaxe.");
			stage = 32;
			break;
		case 32:
			sendNPCDialogue(npcId, CALM, "Well, aren't we the demanding one? It's possible in theory, I "
					+ "suppose. I could coat a dragon metal item in harmonic dust, crystallise it...");
			stage = 33;
			break;
		case 33:
			sendNPCDialogue(npcId, NORMAL, "That would maintain the tool's structural integrity, and I "
					+ "could use an enchantment similar to the cyrstal chime, to inhibit degradation.");
			stage = 34;
			break;
		case 34:
			sendNPCDialogue(npcId, CALM, "Give me a dragon hatchet or pickaxe to upgrade, and at least "
					+ "4,000 harmonic dust, and I'll see what I can do.");
			stage = 35;
			break;
		case 35:
			sendOptionsDialogue("Select an Option", "Check for tools in my backpack...",
					"Tell me again about the process.", "I have other questions...");
			stage = 36;
			break;
		case 36:
			switch (componentId) {
			case OPTION_1:
				if (!player.getInventory().containsOneItem(15259) && !player.getInventory().containsOneItem(6739)) {
					sendNPCDialogue(npcId, SAD, "You are not carrying any dragon tools " + "that I can upgrade.");
					stage = 35;
					break;
				}
				sendOptionsDialogue("Select an Option", "Upgrade pickaxe", "Upgrade hatchet");
				stage = 37;
				break;
			case OPTION_2:
				sendNPCDialogue(npcId, CALM,
						"Hmm... They're rather large tools, but in principle it'd be "
								+ "no different from attuning, say, a crystal halberd. It would degrade to a seed "
								+ "like other tools, of course.");
				stage = 31;
				break;
			case OPTION_3:
				sendOptionsDialogue("Select an Option", "Ask about Lady Ithell and her clan...",
						"Can you make me a crystal hatchet or pickaxe?", "Is there anything I can do for you?",
						"Is there anything else I can do for you?", "Nothing, thanks.");
				stage = 0;
				break;
			}
			break;
		case 37:
			switch (componentId) {
			case OPTION_1:
				if (!player.getInventory().containsItem(32622, 4000)) {
					sendNPCDialogue(npcId, CALM, "I need 4,000 harmonic dust to upgrade the " + "dragon pickaxe.");
					stage = 35;
					break;
				}
				if (player.getInventory().containsItemInInventory(15259, 1)) {
					player.getInventory().deleteItem(new Item(15259));
					player.getInventory().deleteItem(new Item(32622, 4000));
					player.getInventory().addItem(new Item(32646));
					sendNPCDialogue(npcId, NORMAL,
							"I've successfully upgraded your dragon pickaxe " + "that was in your inventory.");
				} else
					sendNPCDialogue(npcId, SAD, "You are not carrying a dragon pickaxe " + "that I can upgrade.");
				stage = 99;
				break;
			case OPTION_2:
				if (!player.getInventory().containsItem(32622, 4000)) {
					sendNPCDialogue(npcId, CALM, "I need 4,000 harmonic dust to upgrade the " + "dragon hatchet.");
					stage = 35;
					break;
				}
				if (player.getInventory().containsItemInInventory(6739 , 1)) {
					player.getInventory().deleteItem(new Item(6739));
					player.getInventory().deleteItem(new Item(32622, 4000));
					player.getInventory().addItem(new Item(32645));
					sendNPCDialogue(npcId, NORMAL,
							"I've successfully upgraded your dragon hatchet " + "that was in your inventory.");
				} else
					sendNPCDialogue(npcId, SAD, "You are not carrying a dragon hatchet " + "that I can upgrade.");
				stage = 99;
				break;
			}
			break;
		case 38:
			sendNPCDialogue(20282, NORMAL, "I am looking forward to seeing what you create.");
			stage = 99;
			break;
		case 99:
			end();
			break;
		}
	}

	@Override
	public void finish() {
	}

}