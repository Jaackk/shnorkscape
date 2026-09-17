package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;

/**
 * Handles the Musician NPC dialogue.
 *
 * @author Noel
 */
public class MusicianD extends Dialogue {

	// A long pre-formatted string because the interface only takes one CID.
	private static final String longString = "You may start running by clicking once on the Run button, "
			+ "which is the<br>running man icon at the top-right of the minimap. "
			+ "Clicking the Run button<br>a second time will switch you back to walking. "
			+ "It tells you how much run<br>energy you currently have.";
	/**
	 * Represents the NPC ID as Integer.
	 */
	private int npcId;

	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Who are you?", "Can I ask you some questions about resting?",
				"Can I ask you some questions about running?", "That's all for now.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			stage = 0;
			switch (componentId) {
			case OPTION_1:
				stage = 1;
				sendNPCDialogue(npcId, NORMAL, "Me? I'm a musician! Let me help you relax: sit down, rest<br>",
						"your weary limbs and allow me to wash away the troubles<br>",
						"of the day. After a long trek, what could be better than<br>",
						"some music to give you the energy to continue? Did you");
				break;
			case OPTION_2:
				stage = 9;
				sendOptionsDialogue("Can I ask you some questions about resting?", "How does resting work?",
						"What's special about resting by a musician?", "Can you summarise the effects for me?",
						"That's all for now.");
				break;
			case OPTION_3:
				stage = 20;
				sendNPCDialogue(npcId, NORMAL, "Running? Of course! Not that I do much running, I prefer<br>",
						"to saunter. But you adventuring types always seem to be<br>",
						"in a rush, zipping hither and thither.");
				break;
			case OPTION_4:
				stage = 126;
				sendNPCDialogue(npcId, NORMAL, "Well, don't forget to have a rest every now and again.");
				break;
			default:
				stage = 126;
				break;
			}
			break;
		case 1:
			stage = 2;
			sendNPCDialogue(npcId, NORMAL, "Know music has curative properties? Music stimulates the<br>",
					"healing humours in the body, so they say.");
			break;
		case 2:
			stage = 3;
			sendPlayerDialogue(NORMAL, "Who says that, then?");
			break;
		case 3:
			stage = 4;
			sendNPCDialogue(npcId, NORMAL, "I was told by a travelling medical practitioner, selling oil<br>",
					"extracted from snakes. It's a commonly known fact, so<br>",
					"he said. After resting to some music, you will be able to<br>",
					"run longer and your life points will increase noticeably. A");
			break;
		case 4:
			stage = 5;
			sendNPCDialogue(npcId, NORMAL, "pancea, if you will. Ah, the power of music.");
			break;
		case 5:
			stage = 6;
			sendPlayerDialogue(NORMAL, "So, just listening to some music will cure me of all my ills?");
			break;
		case 6:
			stage = 7;
			sendNPCDialogue(npcId, NORMAL, "Well, not quite. Poison, lack of faith and dismembered<br>",
					"limbs are all a bit beyond even the most rousing of<br>",
					"harmonies, but I guarantee you will feel refreshed, and<br>",
					"better equipped to take on the challenges of the day.");
			break;
		case 7:
			stage = 8;
			sendPlayerDialogue(NORMAL, "Does this cost me anything?");
			break;
		case 8:
			stage = 127;
			sendNPCDialogue(npcId, NORMAL, "Oh, no! My reward is the pleasure I bring to the masses.<br>",
					"Just remember me and tell your friends, and that is<br>",
					"payment enough. So sit down and enjoy!");
			break;
		case 9:
			switch (componentId) {
			case OPTION_1:
				stage = 10;
				sendNPCDialogue(npcId, NORMAL, "Have you ever been on a long journey, and simply wanted<br>",
						"to have a rest? When you're running from city to city,<br>",
						"it's so easy to run out of breath, don't you find?");
				break;
			case OPTION_2:
				stage = 17;
				sendNPCDialogue(npcId, NORMAL, "The effects of resting are enhanced by music. Your run<br>",
						"energy will recharge many times the normal rate, and<br>",
						"your life points three times as fast.");
				break;
			case OPTION_3:
				stage = 18;
				sendNPCDialogue(npcId, NORMAL, "Certainly. You can rest anywhere, simply choose the Rest<br>",
						"option on the run buttons.");
				break;
			case OPTION_4:
				stage = 126;
				sendNPCDialogue(npcId, NORMAL, "Well, don't forget to have a rest every now and again.");
				break;

			default:
				stage = 126;
				break;
			}
			break;
		case 10:
			stage = 11;
			sendPlayerDialogue(NORMAL, "Yes. I can never run as far as I'd like.");
			break;
		case 11:
			stage = 12;
			sendNPCDialogue(npcId, NORMAL, "Well, you may rest anywhere, simply choose the Rest<br>",
					"option on the run buttons.");
			break;
		case 12:
			stage = 13;
			sendNPCDialogue(npcId, NORMAL, "When you rest, you will sit on the floor. When you are<br>",
					"nice and relaxed, you will recharge your run energy<br>",
					"more quickly and your life points twice as fast as you<br>", "would do normally.");
			break;
		case 13:
			stage = 14;
			sendNPCDialogue(npcId, NORMAL, "Of course, you can't do anything else while you're resting,<br>",
					"other than talk.");
			break;
		case 14:
			stage = 15;
			sendPlayerDialogue(NORMAL, "Why not?");
			break;
		case 15:
			stage = 16;
			sendNPCDialogue(npcId, NORMAL, "Well, you wouldn't be resting, now would you?");
			break;
		case 16:
			stage = 127;
			sendNPCDialogue(npcId, NORMAL, "Also, you should know that resting by a musician, has a<br>",
					"similar effect but the benefits are greater.");
			break;
		case 17:
			stage = 127;
			sendNPCDialogue(npcId, NORMAL, "Simply sit down and rest as you would normally, nice and<br>",
					"close to the musician. You'll turn to face the musician<br>",
					"and hear the music. Like resting anywhere, if you do<br>",
					"anything other than talk, you will stop resting.");
			break;
		case 18:
			stage = 19;
			sendNPCDialogue(npcId, NORMAL, "Resting anywhere will replenish your run energy more<br>",
					"quickly than normal, your life points will replenish twice<br>", "as fast as well!");
			break;
		case 19:
			stage = 127;
			sendNPCDialogue(npcId, NORMAL, "Resting by a musician will replenish your run energy many<br>",
					"times faster than normal, and your life points will also<br>", "replenish three times as fast.");
			break;
		case 20:
			stage = 21;
			sendPlayerDialogue(NORMAL, "Why do I need to run anyway?");
			break;
		case 21:
			stage = 22;
			sendNPCDialogue(npcId, NORMAL, "Running is the simplest way to get somewhere quickly.<br>",
					"When you run you move twice as fast as you normally<br>",
					"would. Also, you don't look like the cowardly type, but<br>",
					"most creatures can't run very fast, so if you don't want");
			break;
		case 22:
			stage = 23;
			sendNPCDialogue(npcId, NORMAL, "to fight, you can always run away.");
			break;
		case 23:
			stage = 24;
			sendPlayerDialogue(NORMAL, "Can I keep running forever?");
			break;
		case 24:
			stage = 25;
			sendNPCDialogue(npcId, NORMAL, "No, eventually you'll get tired. When that happens you will<br>",
					"stop running, and start walking. It takes a while to get<br>",
					"your breath back, but once you've recovered a little, you<br>",
					"can start running again. You recover quickly whilst");
			break;
		case 25:
			stage = 26;
			sendNPCDialogue(npcId, NORMAL, "resting, or more slowly whilst walking.");
			break;
		case 26:
			stage = 127;
			player.getInterfaceManager().sendChatBoxInterface(1186);
			player.getPackets().sendIComponentText(1186, 3, longString);
			break;

		case 126:
			end();
			break;
		case 127:
			stage = -1;
			sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Who are you?",
					"Can I ask you some questions about resting?", "Can I ask you some questions about running?",
					"That's all for now.");
			break;

		default:
			end();
			break;
		}
	}

	@Override
	public void finish() {
	}

}