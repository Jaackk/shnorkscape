package com.rs.game.player.dialogue.impl;

import com.rs.Settings;
import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.misc.ShopsDataParser;

/**
 * Handles the Party Pete dialogue.
 *
 * @author Noel
 */
public class PartyPete extends Dialogue {

	int npcId;
	private final int[] danceMoves = { 2109, 866, 2106, 2107, 2108, 3544, 3543 };

	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		sendPlayerDialogue(NORMAL, "Hi!");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			sendNPCDialogue(npcId, NORMAL, "Hi! I'm Party Pete. Welcome to the Party Room!");
			stage = 0;
			break;
		case 0:
			sendOptionsDialogue("Select an Option", "So, what's this room for?", "What's the big lever over there for?",
					"What's the gold chest for?", "I wanna party!", "More.");
			stage = 1;
			break;
		case 1:
			switch (componentId) {
			case OPTION_1:
				sendPlayerDialogue(CALM, "So what's this room for?");
				stage = 2;
				break;
			case OPTION_2:
				sendPlayerDialogue(CALM, "What's the big lever over there for?");
				stage = 20;
				break;
			case OPTION_3:
				sendPlayerDialogue(CALM, "What's the gold chest for?");
				stage = 30;
				break;
			case OPTION_4:
				sendPlayerDialogue(CALM, "I wanna party!");
				stage = 40;
				break;
			case OPTION_5:
				sendOptionsDialogue("Select an Option", "I love your hair!", "Why's there a chameleon in here?",
						"Back.");
				stage = 60;
				break;
			}
			break;
		case 2:
			sendNPCDialogue(npcId, NORMAL, "This room is for partying the night away!");
			stage = 3;
			break;
		case 3:
			sendPlayerDialogue(NORMAL, "How do you have a party in " + Settings.SERVER_NAME + "?");
			stage = 4;
			break;
		case 4:
			sendNPCDialogue(npcId, NORMAL, "Get a few mates around, get the beers in and have fun!");
			stage = 5;
			break;
		case 5:
			sendNPCDialogue(npcId, NORMAL, "Some players organise parties so keep an eye open!");
			stage = 6;
			break;
		case 6:
			sendPlayerDialogue(NORMAL, "Woop! Thanks Pete!");
			stage = 99;
			break;

		case 20:
			sendNPCDialogue(npcId, NORMAL, "Simple. With the lever you can do some fun stuff.");
			stage = 21;
			break;
		case 21:
			sendPlayerDialogue(NORMAL, "What kind of stuff?");
			stage = 22;
			break;
		case 22:
			sendNPCDialogue(npcId, CALM,
					"A balloon drop costs 1000 gold. "
							+ "For this, you get 200 balloons dropped across the whole of the party room. "
							+ "You can then have fun popping the balloons!");
			stage = 23;
			break;
		case 23:
			sendNPCDialogue(npcId, CALM,
					"Any items in the Party Drop Chest will be put into balloons " + "as soon as you pull the lever.");
			stage = 24;
			break;
		case 24:
			sendNPCDialogue(npcId, CALM,
					"When the balloons are released, you can burst them " + "to get at the items!");
			stage = 25;
		case 25:
			sendNPCDialogue(npcId, NORMAL, "For 500 gold, you can summon the Party Room Knights, "
					+ "who will dance for your delight. Their singing isn't a delight, though!");
			stage = 99;
			break;

		case 30:
			sendNPCDialogue(npcId, NORMAL,
					"Any items in the chest will be dropped inside the balloons " + "when you pull the lever.");
			stage = 31;
			break;
		case 31:
			sendPlayerDialogue(NORMAL, "Cool! Sounds like a fun way to do a drop party!");
			stage = 32;
			break;
		case 32:
			sendNPCDialogue(npcId, NORMAL, "Exactly!");
			stage = 33;
			break;
		case 33:
			sendNPCDialogue(npcId, CALM, "A word of warning, though. Any items that you put into the "
					+ "chest can't be taken out again, and it costs 1000 gold pieces for each drop party.");
			stage = 99;
			break;

		case 40:
			sendNPCDialogue(npcId, NORMAL,
					"I've won the Dance Trophy at the Kandarin Ball three years " + "in a trot!");
			stage = 41;
			break;
		case 41:
			sendPlayerDialogue(NORMAL, "Show me your moves Pete!");
			stage = 42;
			break;
		case 42:
			finish();
			final NPC pete = World.findNPC(npcId);
			WorldTasksManager.schedule(new WorldTask() {
				int stage;

				@Override
				public void run() {
					stage++;
					if (stage == 2) {
						pete.setNextAnimation(new Animation(danceMoves[Utils.random(danceMoves.length)]));
					}
					if (stage == 4) {
						pete.setNextAnimation(new Animation(danceMoves[Utils.random(danceMoves.length)]));
					}
					if (stage == 6) {
						pete.setNextAnimation(new Animation(danceMoves[Utils.random(danceMoves.length)]));
						stop();
					}
				}
			}, 0, 1);
			break;

		case 60:
			switch (componentId) {
			case OPTION_1:
				sendPlayerDialogue(NORMAL, "I love your hair!");
				stage = 61;
				break;
			case OPTION_2:
				sendPlayerDialogue(NORMAL, "Why's there a chameleon in here?");
				stage = 70;
				break;
			}
			break;
		case 61:
			sendNPCDialogue(npcId, NORMAL, "Isn't it groovy? I liked it so much, I had extras "
					+ "made for my party goers. Would you like to buy one?");
			stage = 62;
			break;
		case 62:
			sendOptionsDialogue("Would you like to buy an Afro Wig?", "Yes.", "No.");
			stage = 63;
			break;
		case 63:
			switch (componentId) {
			case OPTION_1:
				finish();
				ShopsDataParser.openShop(player, 15);
				break;
			case OPTION_2:
				finish();
				break;
			}
			break;
		case 70:
			sendNPCDialogue(npcId, NORMAL,
					"Karma's my pet. I got him for Christmas one year. "
							+ "He keeps the Party Room free of flies, and he loves watching me dance. "
							+ "Karma karma karma cha...");
			stage = 71;
			break;
		case 71:
			sendOptionsDialogue("What would you like to say?", "Can you talk to him?", "Christmas is over.",
					"Aww, that's nice.");
			stage = 72;
			break;
		case 72:
			switch (componentId) {
			case OPTION_1:
				sendPlayerDialogue(NORMAL, "Can you talk to him?");
				stage = 73;
				break;
			case OPTION_2:
				sendPlayerDialogue(NORMAL, "Christmas is over. Why've you still got him " + "hanging around?");
				stage = 80;
				break;
			case OPTION_3:
				sendPlayerDialogue(NORMAL, "Aww, that's nice.");
				stage = 99;
				break;
			}
			break;
		case 73:
			sendNPCDialogue(npcId, NORMAL,
					"Sure, I talk to the little fellow all the time. "
							+ "My summoning level's not high enough to understand what he says back, "
							+ "but he's still great company.");
			stage = 74;
			break;
		case 74:
			sendOptionsDialogue("What would you like to say?", "Can you talk to him?", "Christmas is over.",
					"Aww, that's nice.");
			stage = 72;
			break;
		case 80:
			sendNPCDialogue(npcId, SCARED, "I couldn't chuck the little chappy out! A pet " + "is for life!");
			stage = 81;
			break;
		case 81:
			sendOptionsDialogue("What would you like to say?", "Can you talk to him?", "Christmas is over.",
					"Aww, that's nice.");
			stage = 72;
			break;
		case 99:
			finish();
		}
	}

	@Override
	public void finish() {
		player.getInterfaceManager().closeChatBoxInterface();
	}
}
