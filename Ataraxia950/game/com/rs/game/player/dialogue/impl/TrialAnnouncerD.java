package com.rs.game.player.dialogue.impl;

import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.content.Magic;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Utils;

public class TrialAnnouncerD extends Dialogue {

	private int npcId;

	/**
	 * Teleports the player to the Botany Bay.
	 *
	 * @param player
	 *            The player to teleport.
	 */
	public static void teleport(Player player) {
		if (Utils.random(1) == 0)
			Magic.sendNormalTeleportSpell(player, 0, 0, new WorldTile(3672, 3616, 0));
		else
			Magic.sendNormalTeleportSpell(player, 0, 0, new WorldTile(3688, 3616, 0));
	}

	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		sendNPCDialogue(npcId, NORMAL, "Ho there, loyal citizen. How can I help you?");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			sendOptionsDialogue("Select an Option", "Who are you?", "Who's on trial today?",
					"Can you take me to the courtroom?", "Do you have a spare pitchfork?", "I have to go.");
			stage = 0;
			break;
		case 0:
			switch (componentId) {
			case OPTION_1:
				sendNPCDialogue(npcId, CALM, "Who I am is unimportant. All you need to know is that I work "
						+ "for the Botfinder General: Mathew Hopscotch!");
				stage = 1;
				break;
			case OPTION_2:
				sendNPCDialogue(npcId, CALM, "Nobody at the moment.");
				stage = 13;
				break;
			case OPTION_3:
				sendNPCDialogue(npcId, CALM, "Okay. Brace yourself for transportation.");
				stage = 15;
				break;
			case OPTION_4:
				sendNPCDialogue(npcId, CALM, "I have given you a Pitchfork of Vigilantism.");
				player.addItem(new Item(25114));
				stage = 17;
				break;
			case OPTION_5:
				sendNPCDialogue(npcId, CALM, "See you around then. Kepp an eye out for bots!");
				stage = 19;
				break;
			}
			break;
		case 1:
			sendPlayerDialogue(CROOKED_HEAD, "The who-finder what now?");
			stage = 2;
			break;
		case 2:
			sendNPCDialogue(npcId, ANGRY, "The Botfinder General!");
			stage = 3;
			break;
		case 3:
			sendNPCDialogue(npcId, CALM, "The brave, solid, incorruptible individual who has been "
					+ "scouring the lands to rid them of the scourge of bots.");
			stage = 4;
			break;
		case 4:
			sendPlayerDialogue(CROOKED_HEAD, "So how does he tell someone apart from a bot?");
			stage = 5;
			break;
		case 5:
			sendNPCDialogue(npcId, UNSURE, "That's a secret. But he is never wrong.");
			stage = 6;
			break;
		case 6:
			sendNPCDialogue(npcId, CALM, "Anyway, I and my companions have been stationed around "
					+ "the world to announce when he is putting another tin soldier on trial.");
			stage = 7;
			break;
		case 7:
			sendPlayerDialogue(CROOKED_HEAD, "Why would you do that?");
			stage = 8;
			break;
		case 8:
			sendNPCDialogue(npcId, CALM, "Well, we can take you to his secret courthouse so fine, "
					+ "upstanding folk like you can sentence the clockwork ones to their deaths.");
			stage = 9;
			break;
		case 9:
			sendPlayerDialogue(SCARED, "Really? But I don't think I'm qualified to make that sort " + "of decision.");
			stage = 10;
			break;
		case 10:
			sendNPCDialogue(npcId, CROOKED_HEAD, "We give out free pitchforks.");
			stage = 11;
			break;
		case 11:
			sendPlayerDialogue(NORMAL, "Sold!");
			stage = 12;
			break;
		case 12:
			sendNPCDialogue(npcId, NORMAL, "Good " + (player.getAppearence().isMale() ? "boy" : "girl")
					+ "! Is there anything else you want to know?");
			stage = -1;
			break;
		case 13:
			sendPlayerDialogue(CALM, "Oh well. I'm sure there will be another trial soon.");
			stage = 14;
			break;
		case 14:
			sendNPCDialogue(npcId, CALM, "Oh, there usually is. Anything else I can do for you?");
			stage = -1;
			break;
		case 15:
			finish();
			teleport(player);
			break;
		case 17:
			sendNPCDialogue(npcId, CALM, "I have given you a Pitchfork of Justice.");
			player.addItem(new Item(25115));
			stage = 18;
			break;
		case 18:
			sendNPCDialogue(npcId, NORMAL, "Since we have squashed so many bots in the last few months, "
					+ "we have decided to give everyone all the rewards.");
			stage = -1;
			break;
		case 19:
			finish();
			break;
		}
	}

	@Override
	public void finish() {
		player.getInterfaceManager().closeChatBoxInterface();
	}
}