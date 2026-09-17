package com.rs.game.player.dialogue.impl;

import com.rs.game.World;
import com.rs.game.item.Item;
import com.rs.game.player.content.achievementsystem.AchievementList;
import com.rs.game.player.dialogue.Dialogue;

/**
 * Handles the NPC Ocellus's dialogue + options.
 *
 * @author Noel
 */
public class Ocellus extends Dialogue {

	/**
	 * Items required to make an Ascension Crossbow.
	 */
	Item[] items = { new Item(25917), new Item(28457), new Item(28458), new Item(28459), new Item(28460),
			new Item(28461), new Item(28462), new Item(28436, 100) };
	/**
	 * The NPC ID.
	 */
	private int npcId;
	/**
	 * The NPC option clicked.
	 */
	private byte option;

	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		option = (byte) parameters[1];
		switch (option) {
		case 1:
			sendNPCDialogue(npcId, CALM, "What?");
			stage = -1;
			break;
		case 2:
			sendNPCDialogue(npcId, CALM, "Algarum thread. Taken from Ascended. 500,000 coins to you.");
			stage = 41;
			break;
		case 3:
			sendNPCDialogue(npcId, CALM, "Legiones. Ascended leaders. Carry magical signets.");
			stage = 33;
			break;
		}
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			sendOptionsDialogue("Select an Option", "What's the Order of Ascension?", "Who are you?",
					"Can you give me anything to help?");
			stage = 0;
			break;
		case 0:
			switch (componentId) {
			case OPTION_1:
				sendPlayerDialogue(CROOKED_HEAD, "What's the Order of Ascension?");
				stage = 1;
				break;
			case OPTION_2:
				sendPlayerDialogue(CROOKED_HEAD, "Who are you?");
				stage = 20;
				break;
			case OPTION_3:
				sendPlayerDialogue(CROOKED_HEAD, "Can you give me anything to help?");
				stage = 30;
				break;
			}
			break;
		case 1:
			sendNPCDialogue(npcId, CALM, "Disappointment. Failure. Worshippers of Guthix.");
			stage = 2;
			break;
		case 2:
			sendOptionsDialogue("Select an Option", "Why should I fight worshippers of Guthix?", "What are they doing?",
					"Where did they come from?", "How do I fight them?");
			stage = 3;
			break;
		case 3:
			switch (componentId) {
			case OPTION_1:
				sendPlayerDialogue(CROOKED_HEAD, "Why should I fight worshippers of Guthix?");
				stage = 4;
				break;
			case OPTION_2:
				sendPlayerDialogue(CROOKED_HEAD, "What are they doing?");
				stage = 6;
				break;
			case OPTION_3:
				sendPlayerDialogue(CROOKED_HEAD, "Where did they come from?");
				stage = 14;
				break;
			case OPTION_4:
				sendPlayerDialogue(CROOKED_HEAD, "How do I fight them?");
				stage = 17;
				break;
			}
			break;
		case 4:
			sendNPCDialogue(npcId, CALM, "Guthix refuse worship. Worship is slavery. Worse crime though.");
			stage = 5;
			break;
		case 5:
			sendNPCDialogue(npcId, CALM, "Ascended doing terrible thing. Terrible thing in Guthix's name.");
			stage = 2;
			break;
		case 6:
			sendNPCDialogue(npcId, CALM, "Death of Guthix shock Ascended. Create bad plan.");
			stage = 7;
			break;
		case 7:
			sendNPCDialogue(npcId, CALM, "Creating new god. New Guthix to worship.");
			stage = 8;
			break;
		case 8:
			sendNPCDialogue(npcId, CALM, "Body of crystal matrix. Spirit from humans. Dead humans.");
			stage = 9;
			break;
		case 9:
			sendNPCDialogue(npcId, CALM, "Still unfinished. Stop before they finish.");
			stage = 10;
			break;
		case 10:
			sendPlayerDialogue(CROOKED_HEAD, "Isn't creating a new Guthix a good thing?");
			stage = 11;
			break;
		case 11:
			sendNPCDialogue(npcId, CALM, "Ascended creation mindless monster. Not Guthix at all.");
			stage = 12;
			break;
		case 12:
			sendNPCDialogue(npcId, CALM, "Would have much power and no wisdom. Like god. Bring ruin.");
			stage = 2;
			break;
		case 14:
			sendNPCDialogue(npcId, CALM, "Founded in this place. Long history. Secret. Mistake.");
			stage = 15;
			break;
		case 15:
			sendNPCDialogue(npcId, CALM, "Made to stand against worship. Now stands for worship.");
			stage = 2;
			break;
		case 17:
			sendNPCDialogue(npcId, CALM, "Ascended use powerful magic. Taken from Guthix.");
			stage = 18;
			break;
		case 18:
			sendNPCDialogue(npcId, CALM, "Bring bows, crossbows. Pierce magic. Kill Ascended.");
			player.sendMessage("You need a ranged weapon and a Slayer level of 81 to fight Ascended.");
			stage = -1;
			break;
		case 20:
			sendNPCDialogue(npcId, CALM, "Ocellus. Virius. Guardian.");
			stage = 21;
			break;
		case 21:
			sendOptionsDialogue("Select an Option", "Guardian of what?", "What does Virius mean?", "Why are you here?");
			stage = 22;
			break;
		case 22:
			switch (componentId) {
			case OPTION_1:
				sendPlayerDialogue(CROOKED_HEAD, "Guardian of what?");
				stage = 23;
				break;
			case OPTION_2:
				sendPlayerDialogue(CROOKED_HEAD, "What does Virius mean?");
				stage = 26;
				break;
			case OPTION_3:
				sendPlayerDialogue(CROOKED_HEAD, "Why are you here?");
				stage = 27;
				break;
			}
			break;
		case 23:
			sendNPCDialogue(npcId, CALM, "Guthix.");
			stage = 24;
			break;
		case 24:
			sendPlayerDialogue(CROOKED_HEAD, "Why weren't you with us at Cres's chamber?");
			stage = 25;
			break;
		case 25:
			sendNPCDialogue(npcId, CALM, "Important work. Guthix need work. Not need bodyguards.");
			stage = 21;
			break;
		case 26:
			sendNPCDialogue(npcId, CALM, "My people. Virii. Not from this world.");
			stage = 21;
			break;
		case 27:
			sendNPCDialogue(npcId, CALM,
					"Ascended need stopping. Mock Guthix. Kill humans wastefully. Create aberration.");
			stage = -1;
			break;
		case 30:
			sendNPCDialogue(npcId, CALM, "Crossbow? Algarum thread?");
			stage = 31;
			break;
		case 31:
			sendOptionsDialogue("Select an Option", "Crossbow.", "Algarum thread.");
			stage = 32;
			break;
		case 32:
			switch (componentId) {
			case OPTION_1:
				sendNPCDialogue(npcId, CALM, "Legiones. Ascended leaders. Carry magical signets.");
				stage = 33;
				break;
			case OPTION_2:
				sendNPCDialogue(npcId, CALM, "Algarum thread. Taken from Ascended. 500,000 coins to you.");
				stage = 41;
				break;
			}
			break;
		case 33:
			sendNPCDialogue(npcId, CALM,
					"Kill Legiones. Take signets. Kill lesser Ascended. Take shards. Many shards.");
			stage = 34;
			break;
		case 34:
			sendNPCDialogue(npcId, CALM, "Bring signets and shards. Bring powerful crossbow. Dragonkin-made.");
			stage = 35;
			break;
		case 35:
			sendNPCDialogue(npcId, CALM, "Fashion more powerful weapon. Very powerful.");
			stage = 36;
			break;
		case 36:
			sendDialogue("Bring 1 dragon crossbow, 6 different Ascension signets and 100 Ascension shards to Ocellus "
					+ "and he will create an Ascension crossbow.");
			stage = 37;
			break;
		case 37:
			sendPlayerDialogue(CROOKED_HEAD, "Do I have everything you need?");
			stage = 38;
			break;
		case 38:
			if (player.getInventory().containsItems(items)) {
				sendNPCDialogue(npcId, CALM, "Yes. Create?");
				stage = 39;
			} else {
				sendNPCDialogue(npcId, CALM, "No.");
				stage = 99;
			}
			break;
		case 39:
			sendOptionsDialogue("Create Ascension crossbow?", "Mainhand", "Off-hand");
			stage = 40;
			break;
		case 40:
			switch (componentId) {
			case OPTION_1:
				if (player.getInventory().containsItems(items)) { // Another
																	// check
																	// just
																	// incase.
					sendItemDialogue(28437, 1, "Ocellus has made you an Ascension crossbow.");
					player.getInventory().removeItems(items);
					player.getInventory().addItem(new Item(28437));
					player.getAchievements().updateProgress(1, AchievementList.CRAFT_ASCENSION_CBOW);
				} else
					sendNPCDialogue(npcId, CALM, "No.");
				stage = 99;
				break;
			case OPTION_2:
				if (player.getInventory().containsItems(items)) { // Another
					// check
					// just
					// incase.
					sendItemDialogue(28441, 1, "Ocellus has made you an Off-hand Ascension crossbow.");
					player.getInventory().removeItems(items);
					player.getInventory().addItem(new Item(28441));
					player.getAchievements().updateProgress(1, AchievementList.CRAFT_ASCENSION_CBOW);
				} else
					sendNPCDialogue(npcId, CALM, "No.");
				stage = 99;
				break;
			}
			break;
		case 41:
			sendPlayerDialogue(CROOKED_HEAD, "What is it?");
			stage = 42;
			break;
		case 42:
			sendNPCDialogue(npcId, CALM,
					"Found deep in ocean. Woven together. Strong thread. Traders found. Brought to surface.");
			stage = 43;
			break;
		case 43:
			sendNPCDialogue(npcId, CALM, "Ascended stole it. Making armour. Took it from them. Stopped them making.");
			stage = 44;
			break;
		case 44:
			sendNPCDialogue(npcId, CALM,
					"Combine with sirenic scale. Make very strong armour. Good to use with bow, crossbow.");
			stage = 45;
			break;
		case 45:
			sendOptionsDialogue("Buy Algarum thread?", "Yes.", "No.");
			stage = 46;
			break;
		case 46:
			switch (componentId) {
			case OPTION_1:
				sendPlayerDialogue(NORMAL, "I'll take it.");
				stage = 47;
				break;
			case OPTION_2:
				sendPlayerDialogue(CALM, "No thanks.");
				stage = 49;
				break;
			}
			break;
		case 47:
			if (player.hasMoney(500000)) {
				Item thread = new Item(29864);
				sendItemDialogue(thread.getId(), thread.getAmount(),
						"You've purchased x1 of Algarum thread for 500'000 coins.");
				player.takeMoney(500000);
				if (!player.getInventory().addItem(thread))
					World.addGroundItem(thread, player, player, false, 60);
				stage = 99;
			} else {
				sendNPCDialogue(npcId, CALM, "Not enough gold.");
				stage = 99;
			}
			break;
		case 49:
			sendNPCDialogue(npcId, CALM, "Okay then.");
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