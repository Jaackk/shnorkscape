package com.rs.game.player.dialogue.impl;

import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.LendingManager;
import com.rs.game.player.Player;
import com.rs.game.player.content.Magic;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Lend;

public class PkingTeleports extends Dialogue {

	@Override
	public void finish() {
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1) {
			if (componentId == OPTION_1) {
				Magic.vineTeleport(player, new WorldTile(3359, 3671, 0));
				end();
			}
			if (componentId == OPTION_2) {
				Magic.vineTeleport(player, new WorldTile(3071, 3649, 0));
				end();
			}
			if (componentId == OPTION_3) {
				Magic.vineTeleport(player, new WorldTile(2998, 3912, 0));
				end();
			}
			if (componentId == OPTION_4) {
				Magic.vineTeleport(player, new WorldTile(2539, 4715, 0));
				end();
			}
			if (componentId == OPTION_5) {
				Magic.vineTeleport(player, new WorldTile(3337, 3889, 0));
				end();
			}
		}
	}

	@Override
	public void start() {
		if (player.isLocked()) {
			end();
			return;
		}
		Lend lend = LendingManager.getLend(player);
		if (lend != null) {
			Player lender = World.getPlayer(lend.getLendee());
			if (lender != null) {
				if (lender.getEquipment().getItemsContainer()
						.containsOne(new Item(lend.getItem().getDefinitions().getLendId()))
						|| lender.getInventory().containsItem(lend.getItem().getDefinitions().getLendId(), 1)) {
					player.sendMessage("You can't bring lendable items into the Wilderness.");
					player.closeInterfaces();
					end();
					return;
				}
			}
		}
		sendOptionsDialogue("Choose your destination", "East Dragons", "Forinthry Dungeon", "Agility Course (50 Wildy)",
				"Mage Bank", "New Gates (47 Wildy)");
	}
}