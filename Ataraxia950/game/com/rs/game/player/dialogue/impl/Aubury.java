package com.rs.game.player.dialogue.impl;

import com.rs.game.Animation;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.content.Magic;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.data.parsers.misc.ShopsDataParser;

/**
 * Handles Aubyr's dialogue.
 */
public class Aubury extends Dialogue {

	static NPC npc;

	/**
	 * Teleports the player to the essence mines.
	 *
	 * @param player
	 *            The player.
	 * @param npc
	 *            Aubury.
	 */
	public static void teleportToEssenceMines(Player player, NPC npc) {
		npc.setNextForceTalk(new ForceTalk("Senventior Disthine Molenko!"));
		npc.setNextAnimation(new Animation(1818));
		npc.setNextGraphics(new Graphics(184));
		Magic.sendNormalTeleportSpell(player, 0, 0, new WorldTile(2910, 4832, 0));
	}

	@Override
	public void start() {
		npc = (NPC) parameters[0];
		sendNPCDialogue(npc.getId(), NORMAL, "Hello, do you want to buy some runes?");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			sendOptionsDialogue("Select an Option", "Yes, please.", "No, thanks.",
					"Could you teleport me to the essence mines?");
			stage = 0;
			break;
		case 0:
			switch (componentId) {
			case OPTION_1:
				sendPlayerDialogue(NORMAL, "Yes, please.");
				stage = 1;
				break;
			case OPTION_2:
				sendPlayerDialogue(NORMAL, "No, thank you.");
				stage = 2;
				break;
			case OPTION_3:
				sendPlayerDialogue(UNSURE, "Could you teleport me to the essence mines?");
				stage = 4;
				break;
			}
			break;
		case 1:
			finish();
			ShopsDataParser.openShop(player, 7);
			break;
		case 2:
			finish();
			break;
		case 3:
			sendNPCDialogue(npc.getId(), NORMAL, "Certainly, hold on!");
			stage = 4;
			break;
		case 4:
			finish();
			teleportToEssenceMines(player, npc);
			break;
		}
	}

	@Override
	public void finish() {
		player.getInterfaceManager().closeChatBoxInterface();
	}
}