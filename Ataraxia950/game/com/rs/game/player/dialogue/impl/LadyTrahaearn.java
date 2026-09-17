package com.rs.game.player.dialogue.impl;

import com.rs.game.player.Skills;
import com.rs.game.player.dialogue.Dialogue;

/**
 * Handles Prifddinas Cities Lady Trahaern's dialogue.
 *
 * @author Noel
 */
public class LadyTrahaearn extends Dialogue {

	/**
	 * Integer representing Ianto's ID.
	 */
	private int npcId;

	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		sendNPCDialogue(npcId, CALM, "Ah, hello again young Player, how can I help you?");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			sendOptionsDialogue("Select an Option", "Ask about the Seren Stones...", "Ask about Lady Trahaearn...",
					"Ask about Clan Trahaearn...", "Ask about Trahaearn skills...", "Nothing, thanks.");
			stage = 0;
			break;
		case 0:
			switch (componentId) {
			case OPTION_1:
				sendPlayerDialogue(CROOKED_HEAD, "Can you tell me about the Seren stones?");
				stage = 1;
				break;
			case OPTION_2:
				sendPlayerDialogue(CROOKED_HEAD, "Can you tell me about yourself?");
				stage = 3;
				break;
			case OPTION_3:
				sendPlayerDialogue(CROOKED_HEAD, "What can you tell me about Clan Trahaearn?");
				stage = 14;
				break;
			case OPTION_4:
				sendPlayerDialogue(CROOKED_HEAD, "What can you tell me about Clan Trahaearn's skills?");
				stage = 18;
				break;
			case OPTION_5:
				sendPlayerDialogue(NORMAL, "Nothing, thanks!");
				stage = 99;
				break;
			}
			break;
		case 1:
			sendNPCDialogue(npcId, CALM, "These Seren stones are crystalline rocks that are covered in Corrupted ore, "
					+ "and can only be found in our clan district of Prifddinas.");
			stage = 2;
			break;
		case 2:
			sendNPCDialogue(npcId, CALM, "Help us get rid of the Corruption and we'll reward you.");
			player.sendMessage("Mine 100 Seren stones to unlock '<col=FF08A0>of the Trahaearn</col>' Loyalty Title.");
			stage = -1;
			break;
		case 3:
			sendNPCDialogue(npcId, CROOKED_HEAD, "Huh? What?");
			stage = 4;
			break;
		case 4:
			sendPlayerDialogue(GLANCE_DOWN, "Erm...can you tell me about yourself?");
			stage = 5;
			break;
		case 5:
			sendNPCDialogue(npcId, CALM, "What? Speak up! I can't hear you.");
			stage = 6;
			break;
		case 6:
			sendPlayerDialogue(ANGRY, "Can - you - tell - me - ab...");
			stage = 7;
			break;
		case 7:
			sendNPCDialogue(npcId, GOOFY_LAUGH,
					"AHAHAHAHAHAHAAA! Ahem, sorry, it was too easy to pass up. " + "I can hear you fine.");
			stage = 8;
			break;
		case 8:
			sendNPCDialogue(npcId, CALM,
					"I am Lady Trahaearn, leader of Clan Trahaearn. And yes, I am the oldest "
							+ "living elf in the city...likely the world. Traditionally when the lord of a clan dies, "
							+ "they are succeeded by another who then leads the clan.");
			stage = 9;
			break;
		case 9:
			sendNPCDialogue(npcId, CALM,
					"But look around you. Look at the youngsters these days. No sense of decorum, "
							+ "or of how things should be. How could I leave my beloved clan to these children? No no no, "
							+ "it will not do. So I built this device - my masterpiece!");
			stage = 10;
			break;
		case 10:
			sendNPCDialogue(npcId, CALM,
					"It extends my life through a combination of crystal vapours and precise "
							+ "enchantments. With it I can continue to govern my beloved Trahaearn until a more suitable "
							+ "successor arrives. Until then, my automatons and I will watch over the clan.");
			stage = 11;
			break;
		case 11:
			sendPlayerDialogue(GLANCE_DOWN, "You would trust machines over your own clan?");
			stage = 12;
			break;
		case 12:
			sendNPCDialogue(npcId, CALM,
					"It's not a matter of trust, but a matter of efficiency. "
							+ "My automatons get things done. They are efficient and unquestioning. "
							+ "They do not destroy with 'improvements', nor add features that only serve to ruin the "
							+ "overall design.");
			stage = 13;
			break;
		case 13:
			sendNPCDialogue(npcId, CALM,
					"They do what is asked of them and they do it well. " + "Unlike some of these youngsters.");
			stage = -1;
			break;
		case 14:
			sendNPCDialogue(npcId, CALM,
					"We are an ancient and noble clan of workers. We reshape the earth itself "
							+ "according to our designs. Oh but we are much the same as the earth we shape, "
							+ "strong and each shaped to serve their purpose.");
			stage = 16;
			break;
		case 16:
			sendNPCDialogue(npcId, CALM, "Like an anvil we hold firm the ways of the elves. Like a hammer, "
					+ "we force shape upon elvenkind, giving it purpose and meaning.");
			stage = 17;
			break;
		case 17:
			sendNPCDialogue(npcId, CALM, "Seren herself saw in us the future of the elves and she honoured our "
					+ "malleability and durability. I like to think she would be proud of what we have achieved.");
			stage = -1;
			break;
		case 18:
			sendNPCDialogue(npcId, CALM,
					"We focus on the skills that lead to true creation: mining and smithing. "
							+ "We draw our raw materials from the very depths of the earth itself and then we refine "
							+ "them in the heat of our forges.");
			stage = 19;
			break;
		case 19:
			sendNPCDialogue(npcId, CALM, "Is there a greater art? We find new purpose for dirt and metal, "
					+ "beyond just something pretty to look at. Oh, we are more than just artists.");
			stage = 20;
			break;
		case 20:
			sendNPCDialogue(npcId, CALM, "We are also the chosen ones, who cleanse the corruption from Seren's "
					+ "broken body. We are the ones who chip the corrupted ore away from Seren's stones.");
			stage = 21;
			break;
		case 21:
			sendPlayerDialogue(CROOKED_HEAD, "What are the Seren Stones?");
			stage = 22;
			break;
		case 22:
			sendNPCDialogue(npcId, CALM,
					"After Seren, in her grief and love for the elves, shattered herself. "
							+ "Pieces of her grow in the elven lands. But there is a darkness that grows there as well - "
							+ "a corruption.");
			stage = 23;
			break;
		case 23:
			sendNPCDialogue(npcId, CALM, "We find these stones and we bring them here. Then we spend our time "
					+ "carefully chipping the corruption away, purifying the stones.");
			stage = 24;
			break;
		case 24:
			sendNPCDialogue(npcId, CALM,
					"Oh, Hefin will say that they are the ones doing the purification, "
							+ "but they merely handle the final stage. It is through the sweat and toil of Trahaearn "
							+ "that the stones are purified.");
			stage = 25;
			break;
		case 25:
			sendPlayerDialogue(CROOKED_HEAD, "Can I help?");
			stage = 26;
			break;
		case 26:
			if (player.getSkills().getLevel(Skills.MINING) >= 89)
				sendNPCDialogue(npcId, CALM, "Certainly. Simply get a pickaxe and start mining!");
			else
				sendNPCDialogue(npcId, CALM, "Forgive me, but you do not yet possess the skill required to delicately "
						+ "remove the corruption. Perhaps with more training, but not now.");
			stage = -1;
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