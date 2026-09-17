package com.rs.game.player.actions.crafting;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.actions.Action;
import com.rs.utils.Utils;

/**
 * @author Tom
 * @date May 4, 2017
 */

public class AnimaCoreCreation extends Action {

	public enum AnimaCoreData {

		ANIMA_CORE_HELM_OF_ZAROS(new int[] { 37009, 37018 }, 37034),
		ANIMA_CORE_LEGS_OF_ZAROS(new int[] { 37015, 37018 }, 37040),
		ANIMA_CORE_BODY_OF_ZAROS(new int[] { 37012, 37018 }, 37037),
		
		ANIMA_CORE_HELM_OF_SEREN(new int[] { 37009, 37027 }, 37052),
		ANIMA_CORE_LEGS_OF_SEREN(new int[] { 37015, 37027 }, 37058),
		ANIMA_CORE_BODY_OF_SEREN(new int[] { 37012, 37027 }, 37055),
		
		ANIMA_CORE_HELM_OF_ZAMORAK(new int[] { 37009, 37024 }, 37043),
		ANIMA_CORE_LEGS_OF_ZAMORAK(new int[] { 37015, 37024 }, 37049),
		ANIMA_CORE_BODY_OF_ZAMORAK(new int[] { 37012, 37024 }, 37046),
		
		ANIMA_CORE_HELM_OF_SLISKE(new int[] { 37009, 37021 }, 37061),
		ANIMA_CORE_LEGS_OF_SLISKE(new int[] { 37015, 37021 }, 37067),
		ANIMA_CORE_BODY_OF_SLISKE(new int[] { 37012, 37021 }, 37064);

		private final int[] material;
		private final int product;

		AnimaCoreData(int[] material, int product) {
			this.material = material;
			this.product = product;
		}

		public int getProduct() {
			return product;
		}

		public int[] getMaterial() {
			return material;
		}
			
		public static AnimaCoreData getProduct(int id) {
			for (AnimaCoreData anima : AnimaCoreData.values()) {
					if (anima.getProduct() == id)
						return anima;
			}
			return null;
		}
	}

	private final AnimaCoreData anima;

	public AnimaCoreCreation(AnimaCoreData anima) {
		this.anima = anima;
	}

	public boolean checkRequirements(Player player) {
		if (player.getInterfaceManager().containsScreenInter()
				|| player.getInterfaceManager().containsInventoryInter()) {
			player.getPackets().sendGameMessage("Please finish what you're doing before doing this action.");
			return false;
		}
		if (!player.getInventory().containsItem(anima.getMaterial()[0], 1)) {
			player.getInterfaceManager().closeChatBoxInterface();
			player.getPackets().sendGameMessage(
					"You don't have any " + ItemDefinitions.getItemDefinitions(anima.getMaterial()[0]).getName().toLowerCase() + " to create this armour piece.");
			return false;
		}
		if (!player.getInventory().containsItem(anima.getMaterial()[1], 1)) {
			player.getInterfaceManager().closeChatBoxInterface();
			player.getPackets().sendGameMessage(
					"You don't have any " +ItemDefinitions.getItemDefinitions(anima.getMaterial()[1]).getName().toLowerCase() + " to create this armour piece.");
			return false;
		}
		return true;
	}

	@Override
	public boolean start(Player player) {
        return checkRequirements(player);
    }

	@Override
	public boolean process(Player player) {
		return checkRequirements(player);
	}

	@Override
	public int processWithDelay(Player player) {
		for (int x = 0; x < anima.getMaterial().length; x++)
			player.getInventory().deleteItem(anima.getMaterial()[x], 1);
		player.getInventory().addItem(anima.getProduct(), 1);
		player.sendMessage("You have successfully created " + Utils.formatAorAn(new Item(anima.getProduct())) + ItemDefinitions.getItemDefinitions(anima.getProduct()).getName() + ".");
		return -1;
	}

	@Override
	public void stop(final Player player) {
		setActionDelay(player, 3);
	}

}
