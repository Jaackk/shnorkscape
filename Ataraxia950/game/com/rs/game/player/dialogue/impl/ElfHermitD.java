package com.rs.game.player.dialogue.impl;

import com.rs.Settings;
import com.rs.game.Animation;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.content.Magic;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

/**
 * Handles the Elf Hermit's dialogue - to take players to Prifddinas.
 *
 * @author Noel
 */
public class ElfHermitD extends Dialogue {

	private NPC npc;

	@Override
	public void start() {
		npc = (NPC) parameters[0];
		sendNPCDialogue(npc.getId(), NORMAL, "Greetings, adventurer!");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			sendOptionsDialogue("Select an Option", "What are you doing here?", "Can you take me to Prifddinas?");
			stage = 0;
			break;
		case 0:
			switch (componentId) {
			case OPTION_1:
				sendPlayerDialogue(CROOKED_HEAD, "What are you doing here, elf? Are you lost?");
				stage = 1;
				break;
			case OPTION_2:
				sendPlayerDialogue(NORMAL, "Can you take me to Prifddinas, please?");
				stage = 6;
				break;
			}
			break;
		case 1:
			sendNPCDialogue(npc.getId(), NORMAL, "I'm glad you asked!");
			stage = 2;
			break;
		case 2:
			sendNPCDialogue(npc.getId(), CALM,
					"I have travelled all around the world of " + Settings.SERVER_NAME + " to take care "
							+ "of brave adventurers" + (player.hasAccessToPrifddinas() ? " like yourself" : "") + ".");
			stage = 3;
			break;
		case 3:
			sendPlayerDialogue(CROOKED_HEAD,
					"Take care of adventurers" + (player.hasAccessToPrifddinas() ? " like me" : "") + "?");
			stage = 4;
			break;
		case 4:
			sendNPCDialogue(npc.getId(), NORMAL,
					"That's right. We allow adventurers" + (player.hasAccessToPrifddinas() ? ", like you," : "") + " "
							+ "to visit our home town - The Prifddinas, and use any of our features.");
			stage = 5;
			break;
		case 5:
			sendPlayerDialogue(NORMAL,
					"That's very interesting. Am I a worthy enough adventurer to travel to the Prifddinas city?");
			stage = 6;
			break;
		case 6:
			if (player.hasAccessToPrifddinas()) {
				sendNPCDialogue(npc.getId(), NORMAL,
						"You are one of the mightiest adventurers out there without doubt. "
								+ "Do you want me to take you to Prifddinas?");
				stage = 10;
				return;
			}
			sendNPCDialogue(npc.getId(), SAD, "I'm sorry, but you don't qualify.. at least yet.");
			player.sendMessage("You must have a total level of at least 2,250 to go to Prifddinas!");
			player.sendMessage("Buy the 'Elf Fiend' game perk to access Prifddinas whenever you like.", true);
			stage = 12;
			break;

		case 10:
			sendOptionsDialogue("Travel to Prifddinas?", "Yes", "No");
			stage = 11;
			break;
		case 11:
			switch (componentId) {
			case OPTION_1:
				finish();
				teleportToPrifddinas(npc);
				break;
			case OPTION_2:
				finish();
				break;
			}
			break;

		default:
			finish();
			break;
		}
	}

	@Override
	public void finish() {
		player.getInterfaceManager().closeChatBoxInterface();
	}

	/**
	 * Teleports the player to the Prifddinas city.
	 *
	 * @param player
	 *            The player.
	 * @param npc
	 *            Elf Hermit.
	 */
	private void teleportToPrifddinas(NPC npc) {
		npc.setNextForceTalk(new ForceTalk("Prifddino.. tolaportaree!"));
		npc.setNextAnimation(new Animation(1818));
		npc.setNextGraphics(new Graphics(184));

		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				Magic.sendNormalTeleportSpell(player, 0, 0, new WorldTile(2213, 3361, 1));
				this.stop();
			}
		}, 1);
	}
}