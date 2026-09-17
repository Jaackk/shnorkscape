package com.rs.game.player.dialogue.impl;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.content.SkillsDialogue;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.InputIntegerEvent;

public class ProteanConvertingD extends Dialogue {

	private Item item;

	@Override
	public void finish() {
	}

	@Override
	public void run(int interfaceId, int componentId) {
		player.sendInputInteger("Select the amount you wish to convert:", new InputIntegerEvent() {
			@Override
			public void run(Player player) {
				int slot = SkillsDialogue.getItemSlot(componentId);
				if (slot > getItems().length || slot < 0) {
					end();
					return;
				}
				int toItem = getItems()[slot];
				int toConvert = getInteger();
				if (getInteger() > player.getInventory().getAmountOf(item.getId())) {
					toConvert = player.getInventory().getAmountOf(item.getId());
				}
				if (toConvert % 3 != 0) {
					toConvert = toConvert - toConvert % 3;
				}
				if (toConvert == 0) {
					player.sendMessage("You need at least 3 protean pieces to convert.");
					return;
				}
				int toRemove = (int) Math.floor(toConvert / 3) * 2;
				player.getInventory().deleteItem(new Item(item.getId(), toConvert));
				player.getInventory().addItem(new Item(toItem, toRemove));
				player.sendMessage("You convert " + toConvert + " x " + item.getName() + " into " + toRemove + " " + ItemDefinitions.getItemDefinitions(toItem).getName() + ".");

			}
		});
		end();
	}

	private static final int[] PROTEAN_ITEMS = new int[] { 30037, 31350, 32337, 33740, 34528/*, 37363*/ };

	private int[] getItems() {
		int[] items = new int[4];
		int i = 0, x = 0;
		for (int protean : PROTEAN_ITEMS) {
			if (protean == item.getId()) { 
				x++;
				continue;
			}
			items[i] = PROTEAN_ITEMS[x];
			i++;
			x++;
		}
		return items;
	}

	@Override
	public void start() {
		this.item = (Item) parameters[0];
		SkillsDialogue.sendSkillsDialogue(player, SkillsDialogue.MAKE, "Select the amount you wish to convert,<br>then click the item to begin.", player.getInventory().getItems().getNumberOf(item.getId()), getItems(), null);

	}

}
