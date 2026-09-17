package com.rs.game.player.dialogue.impl;

import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.content.Magic;
import com.rs.game.player.dialogue.Dialogue;

public class Transportation3 extends Dialogue {

	public static int EMOTE = 9603, GFX = 1684;

	@Override
	public void start() {
		sendOptionsDialogue("Choose your destination:", "Boss kill-log", (String) parameters[0],
				(String) parameters[2]);
	}

	@Override
	public void run(int interfaceId, int componentId) {
		boolean teleported = false;
		if (componentId == OPTION_1) {
			player.getInterfaceManager().sendBossKillLog();
			end();
			return;
		} else if (componentId == OPTION_2)
			teleported = Magic.sendItemTeleportSpell(player, true, EMOTE, GFX, 4, (WorldTile) parameters[1]);
		else if (componentId == OPTION_3)
			teleported = Magic.sendItemTeleportSpell(player, true, EMOTE, GFX, 4, (WorldTile) parameters[3]);

		if (!teleported) {
			end();
			return;
		}
		Item item = player.getInventory().getItems().lookup((Integer) parameters[4]);
		if (item.getId() >= 20655 && item.getId() <= 20659 // ring of wealth
		)
			item.setId(item.getId() - 2);
		else if (item.getId() == 20653)
			item.setId(2572);

		player.getInventory().refresh(player.getInventory().getItems().getThisItemSlot(item));
		player.getInventory().refresh();
		end();
	}

	@Override
	public void finish() {
	}
}