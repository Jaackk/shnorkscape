package com.rs.game.player.dialogue.impl;

import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.content.Magic;
import com.rs.game.player.dialogue.Dialogue;

public class Transportation2 extends Dialogue {

	public static int EMOTE = 9603, GFX = 1684;

	@Override
	public void start() {
		sendOptionsDialogue("Choose your destination:", (String) parameters[0], (String) parameters[2],
				(String) parameters[4], (String) parameters[6]);
	}

	@Override
	public void run(int interfaceId, int componentId) {
		boolean teleported = false;
		if (componentId == OPTION_1)
			teleported = Magic.sendItemTeleportSpell(player, true, EMOTE, GFX, 4, (WorldTile) parameters[1]);
		else if (componentId == OPTION_2)
			teleported = Magic.sendItemTeleportSpell(player, true, EMOTE, GFX, 4, (WorldTile) parameters[3]);
		else if (componentId == OPTION_3)
			teleported = Magic.sendItemTeleportSpell(player, true, EMOTE, GFX, 4, (WorldTile) parameters[5]);
		else if (componentId == OPTION_4)
			teleported = Magic.sendItemTeleportSpell(player, true, EMOTE, GFX, 4, (WorldTile) parameters[7]);
		if (!teleported) {
			end();
			return;
		}
		Item item = player.getEquipment().getItems().lookup((Integer) parameters[8]);
		if (item.getId() >= 3853 && item.getId() <= 3865 // games neck
				|| item.getId() >= 10354 && item.getId() <= 10361 // glory (t)
				|| item.getId() >= 2552 && item.getId() <= 2564 // ring of duel
				|| item.getId() >= 11118
						&& item.getId() <= 11124 /* combat brace */
				|| item.getId() >= 11105
						&& item.getId() <= 11111 /* skills neck */)
			item.setId(item.getId() + 2);
		else if (item.getId() >= 13281 && item.getId() <= 13287) // ring of
																	// slaying
			item.setId(item.getId() + 1);
		else if (item.getId() == 3867 || item.getId() == 10362 || item.getId() == 2566)
			item.setId(995);
		else if (item.getId() == 2566)
			item.setId(1639);
		else if (item.getId() == 13288) {
			item.setId(4155);
			player.sendMessage("The ring collapses into a Slayer gem, which you stow in your pack.");
		} else {
			if (item.getId() == 11124) {
				item.setId(item.getId() + 2);
				player.sendMessage("Your combat bracelet has ran out of charges.");
			} else
				item.setId(item.getId() - 2);
		}

		player.getEquipment().refresh(player.getEquipment().getItems().getThisItemSlot(item));
		player.getEquipment().refresh();
		end();
	}

	@Override
	public void finish() {
	}
}