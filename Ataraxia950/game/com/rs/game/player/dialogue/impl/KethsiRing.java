package com.rs.game.player.dialogue.impl;

import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.content.Magic;
import com.rs.game.player.dialogue.Dialogue;

/**
 * Handles Kethsi ring's teleport options.
 *
 * @author Noel
 */
public class KethsiRing extends Dialogue {

	/**
	 * Represents the ring id.
	 */
	private Item ring;

	/**
	 * Represents if ring used from inventory.
	 */
	private boolean inventory;

	@Override
	public void start() {
		inventory = (Boolean) parameters[1];
		sendOptionsDialogue("Select a location to teleport to", "Adamant dragons", "Rune dragons");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1) {
			if (inventory)
				ring = player.getInventory().getItems().lookup((Integer) parameters[0]);
			else
				ring = player.getEquipment().getItems().lookup((Integer) parameters[0]);

			if (ring.getId() >= 34987 && ring.getId() <= 34993)
				ring.setId(ring.getId() + 2);
			else {
				if (inventory)
					player.getInventory().deleteItem(player.getInventory().getItems().getThisItemSlot(ring), ring);
				else
					player.getEquipment().deleteItem(ring.getId(), 1);
				player.sendMessage("The ring turns into dust upon using its last charge.");
			}
			if (inventory) {
				player.getInventory().refresh(player.getInventory().getItems().getThisItemSlot(ring));
				player.getInventory().refresh();
			} else {
				player.getEquipment().refresh(player.getEquipment().getItems().getThisItemSlot(ring));
				player.getEquipment().refresh();
			}
			switch (componentId) {
			case OPTION_1:
				end();
				Magic.vineTeleport(player, new WorldTile(4512, 6045, 0));
				break;
			case OPTION_2:
				end();
				Magic.vineTeleport(player, new WorldTile(4767, 6077, 1));
				break;
			}
		}
	}

	@Override
	public void finish() {
	}

}