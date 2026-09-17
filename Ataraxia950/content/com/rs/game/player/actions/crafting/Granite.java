package com.rs.game.player.actions.crafting;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.actions.Action;

public class Granite extends Action {

	private final int graniteId;
    private int amount;
	
	public Granite(int id, int amount) {
		this.graniteId = id;
		this.amount = amount;
	}
	
	@Override
	public boolean process(Player player) {
		int spaceRequired = 3;
		if (player.getInventory().getFreeSlots() < spaceRequired) {
			player.sendMessage("You need at least " + spaceRequired + " free inventory space to split this granite.", true);
			return false;
		}
        return amount != 0;
    }

	@Override
	public int processWithDelay(Player player) {
		if (graniteId == 6983) {
			player.getInventory().deleteItem(6983, 1);
			player.getInventory().addItem(new Item(6981, 2));
			player.getInventory().addItem(new Item(6979, 2));
		} else {
			player.getInventory().deleteItem(6981, 1);
			player.getInventory().addItem(new Item(6979, 4));
		}
		player.setNextAnimation(new Animation(1309));
		player.sendMessage("You split the granite into smaller chunks.", true);
		amount--;
		return 2;
	}

	@Override
	public boolean start(Player player) {
		if (player.getInterfaceManager().containsScreenInter()
				|| player.getInterfaceManager().containsInventoryInter()) {
			player.getPackets().sendGameMessage("Please finish what you're doing before doing this action.");
			return false;
		}
		if (!player.getInventory().containsItem(1755, 1) && !player.getToolBelt().contains(1755)) {
			player.sendMessage("You don't have a chisel to split the " + ItemDefinitions.getItemDefinitions(graniteId).getName().toLowerCase() + ".");
			return false;
		}
		int spaceRequired = 3;
		if (player.getInventory().getFreeSlots() < spaceRequired) {
			player.sendMessage("You need at least " + spaceRequired + " free inventory space to split this granite.", true);
			return false;
		}
		return true;
	}

	@Override
	public void stop(Player player) {}

}
