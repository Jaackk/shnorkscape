package com.rs.game.player.dialogue.impl;

import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.player.dialogue.Dialogue;

/**
 * Handles Mwynen's and Essyllt's dialogue.
 *
 * @author Noel
 */
public class MwynenAndEssyllt extends Dialogue {

	/**
	 * Represents the NPC we started the dialogue with.
	 */
	private NPC npc;

	/**
	 * Represents Essyllt's NPC.
	 */
	private NPC essyllt, mwynen;

	@Override
	public void start() {
		npc = (NPC) parameters[0];
		essyllt = World.findNPC(20294);
		mwynen = World.findNPC(20295);
		sendNPCDialogue(npc.getId(), NORMAL, "Oh, hello. May I be of service?");
		essyllt.faceEntity(player);
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			sendNPCDialogue(essyllt.getId(), SCARED, "Aah!");
			stage = 0;
			break;
		case 0:
			sendNPCDialogue(mwynen.getId(), GLANCE_DOWN, "What's the matter?");
			mwynen.faceEntity(essyllt);
			stage = 1;
			break;
		case 1:
			sendPlayerDialogue(CALM, "Oh, it's you. I didn't recognise you there.");
			player.faceEntity(essyllt);
			stage = 2;
			break;
		case 2:
			sendNPCDialogue(essyllt.getId(), SCARED, "I...I promised you'd never see me again. "
					+ "I didn't think the elder would actually permit other races to enter Prifddinas!");
			essyllt.faceEntity(player);
			stage = 3;
			break;
		case 3:
			sendNPCDialogue(essyllt.getId(), SCARED, "I've given up the military life, and settled with my wife.");
			essyllt.faceEntity(player);
			stage = 4;
			break;
		case 4:
			sendNPCDialogue(mwynen.getId(), CROOKED_HEAD, "Essyllt, I don't understand. What's going on?");
			mwynen.faceEntity(essyllt);
			stage = 5;
			break;
		case 5:
			sendNPCDialogue(essyllt.getId(), GLANCE_DOWN,
					"When I was... working... this human had the choice to kill me.");
			essyllt.faceEntity(mwynen);
			mwynen.faceEntity(essyllt);
			stage = 6;
			break;
		case 6:
			sendNPCDialogue(mwynen.getId(), SCARED, "W-what? Kill him?");
			mwynen.faceEntity(player);
			stage = 7;
			break;
		case 7:
			sendNPCDialogue(mwynen.getId(), ANGRY,
					"What manner of brute are you, that you would threaten " + "Lord Iorwerth's emissary of peace?");
			mwynen.faceEntity(player);
			stage = 8;
			break;
		case 8:
			sendOptionsDialogue("Select an Option?", "Emissary of peace?", "I thought you had children.",
					"What are you doing now?", "Bye.");
			stage = 9;
			break;
		case 9:
			switch (componentId) {
			case OPTION_1:
				sendPlayerDialogue(GLANCE_DOWN, "Emissary of peace? What?");
				player.faceEntity(essyllt);
				stage = 10;
				break;
			case OPTION_2:
				sendPlayerDialogue(CROOKED_HEAD, "You said you had children. Where are they?");
				player.faceEntity(essyllt);
				stage = 19;
				break;
			case OPTION_3:
				sendPlayerDialogue(CROOKED_HEAD, "What are you doing now?");
				player.faceEntity(essyllt);
				stage = 29;
				break;
			case OPTION_4:
				sendPlayerDialogue(CALM, "Bye.");
				player.faceEntity(npc);
				stage = 30;
				break;
			}
			break;
		case 10:
			sendNPCDialogue(essyllt.getId(), GLANCE_DOWN, "Yes, the work I was doing when you met me.");
			essyllt.faceEntity(player);
			stage = 11;
			break;
		case 11:
			sendOptionsDialogue("Select an Option", "Play along", "Tell the truth");
			stage = 12;
			break;
		case 12:
			switch (componentId) {
			case OPTION_1:
				sendPlayerDialogue(NORMAL,
						"Oh, of course. Well it's a long story. I'm sure Essyllt can tell you " + "some other time.");
				player.faceEntity(mwynen);
				essyllt.faceEntity(player);
				essyllt.setNextAnimation(new Animation(0x84F));
				stage = 9;
				break;
			case OPTION_2:
				sendPlayerDialogue(GOOFY_LAUGH, "Hah! Wow, that's a good one. I'm glad I let you live just for that!");
				player.faceEntity(mwynen);
				essyllt.faceEntity(mwynen);
				stage = 13;
				break;
			}
			break;
		case 13:
			sendNPCDialogue(mwynen.getId(), CROOKED_HEAD, "What's so funny?");
			mwynen.faceEntity(player);
			stage = 14;
			break;
		case 14:
			sendPlayerDialogue(CALM, "I remember his plan. Had I not stopped him, this 'emissary of peace' would have "
					+ "wiped out the entire population of West Ardougne by herding them into a cavern and collapsing the roof!");
			player.faceEntity(mwynen);
			essyllt.faceEntity(mwynen);
			stage = 15;
			break;
		case 15:
			sendNPCDialogue(mwynen.getId(), SCARED, "What? Essyllt...that's a lie. Tell me it's a lie.");
			mwynen.faceEntity(essyllt);
			essyllt.faceEntity(mwynen);
			stage = 16;
			break;
		case 16:
			sendNPCDialogue(essyllt.getId(), SAD, "I...Why did you...");
			mwynen.faceEntity(essyllt);
			essyllt.faceEntity(mwynen);
			stage = 17;
			break;
		case 17:
			sendNPCDialogue(mwynen.getId(), SAD, "It's...true, isn't it?");
			mwynen.faceEntity(essyllt);
			essyllt.faceEntity(mwynen);
			stage = 18;
			break;
		case 18:
			sendNPCDialogue(mwynen.getId(), ANGRY,
					"Please excuse us, Player. My husband and I need to speak in private.");
			mwynen.faceEntity(player);
			essyllt.faceEntity(mwynen);
			stage = 8;
			break;
		case 19:
			sendNPCDialogue(essyllt.getId(), GLANCE_DOWN,
					"They're not here right now. There's um... Dwyfor... Grenfron... " + "and Aeronwen.");
			essyllt.faceEntity(player);
			stage = 20;
			break;
		case 20:
			sendPlayerDialogue(CROOKED_HEAD,
					"I've been all around Prifddinas, and I'm yet to encounter elves with those names.");
			stage = 21;
			break;
		case 21:
			sendNPCDialogue(essyllt.getId(), GLANCE_DOWN,
					"Dwyfor was also in Lord Iorwerth's service, I last saw him entering " + "the Underground Pass.");
			stage = 22;
			break;
		case 22:
			sendNPCDialogue(essyllt.getId(), GLANCE_DOWN, "Grenfron is assisting in cleaning the Undercity.");
			stage = 23;
			break;
		case 23:
			sendNPCDialogue(essyllt.getId(), GLANCE_DOWN,
					"Aeronwy never really stays in one place, she's probably wandering " + "around somewhere.");
			stage = 24;
			break;
		case 24:
			sendOptionsDialogue("Select an Option", "What was that last name again?", "I'll keep an eye out.");
			stage = 25;
			break;
		case 25:
			switch (componentId) {
			case OPTION_1:
				sendNPCDialogue(essyllt.getId(), GLANCE_DOWN, "Aeronwy? Yes, Aeronwy.");
				stage = 26;
				break;
			case OPTION_2:
				sendPlayerDialogue(CROOKED_HEAD, "I'll keep an eye out.");
				mwynen.faceEntity(essyllt);
				player.faceEntity(essyllt);
				stage = 8;
				break;
			}
			break;
		case 26:
			sendPlayerDialogue(CROOKED_HEAD, "Didn't you say Aeronwen before?");
			stage = 27;
			break;
		case 27:
			sendNPCDialogue(mwynen.getId(), ANGRY, "Just stop already. Your lies are painful to listen to.");
			mwynen.faceEntity(essyllt);
			player.faceEntity(mwynen);
			stage = 28;
			break;
		case 28:
			sendNPCDialogue(essyllt.getId(), GLANCE_DOWN,
					"Fine. I lied about having children. Maybe one day. " + "I just wanted you to let me live.");
			stage = 8;
			break;
		case 29:
			sendNPCDialogue(essyllt.getId(), CALM, "I've retired. I'm just spending time with my wife, Mwynen.");
			stage = 8;
			break;
		case 30:
			sendNPCDialogue(mwynen.getId(), CALM, "It's certainly been interesting speaking with you, Player. "
					+ "It seems Essylt and I have a lot to talk about.");
			stage = 31;
			break;
		case 31:
			sendNPCDialogue(essyllt.getId(), SAD, "I...Bye.");
			mwynen.faceEntity(essyllt);
			essyllt.faceEntity(mwynen);
			stage = 32;
			break;
		case 32:
			end();
			break;
		}
	}

	@Override
	public void finish() {
	}
}