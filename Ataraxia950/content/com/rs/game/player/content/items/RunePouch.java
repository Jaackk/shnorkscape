package com.rs.game.player.content.items;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.utils.Colors;

import java.util.Arrays;
import java.util.List;

public class RunePouch {
	
	private static final Integer[] runes_arr = {
		554, 555, 556, 557, 558, 559, 560,
		561, 562, 563, 564, 565, 566, 9075
	};
	
	private static final List<Integer> runes = Arrays.asList(runes_arr);

	public static void check(final Player player, final boolean small) {		
		if((small ? player.getSmallRunePouch() : player.getBigRunePouch()) == null)
			return;
		
		if((small ? player.getSmallRunePouch() : player.getBigRunePouch()).size() == 0) {
			player.sendMessage(Colors.RED+"There are currently no runes in your pouch!");
			return;
		}
		
		for(Item rune : (small ? player.getSmallRunePouch() : player.getBigRunePouch()).values())
			player.sendMessage(Colors.GREEN+ItemDefinitions.getItemDefinitions(rune.getId()).getName()+": "+rune.getAmount()+"x in your pouch!");
	}
	
	
	public static void fill(final Player player, final Item rune, final boolean small) {
		if((small ? player.getSmallRunePouch() : player.getBigRunePouch()) == null)
			return;
		
		int amount = 0;
		
		if(!runes.contains(rune.getId())) {
			player.sendMessage("This item is not a rune! Please report this to an admin if this is a mistake.");
			return;
		}
		if(!player.containsRuneInPouch(rune.getId(), small) && (small ? player.getSmallRunePouch() : player.getBigRunePouch()).size() == (small ? 2 : 3)) {
			player.sendMessage(Colors.RED+"Your rune pouch is full! Please remove some runes from it first.");
			return;
		}
		
		for(Item item : ((small ? player.getSmallRunePouch() : player.getBigRunePouch())).values()) {
			if(item.getId() == rune.getId()) {
				amount = item.getAmount();
				
				if (item.getAmount() == 16000) {
					player.sendMessage("You cannot put any more of this rune in the pouch");
					return;
				}
			}
		}
		int amountToadd = rune.getAmount();
		if (amount + amountToadd > 16000)
		    amountToadd = 16000 - amount;
		if (amountToadd == 0)
		    return;
		player.getInventory().deleteItem(rune.getId(), amountToadd);
		player.addRuneToPouch(new Item(rune.getId(), amount + amountToadd), small);
		player.sendMessage(Colors.GREEN+"You have added "+amountToadd+"x "+ItemDefinitions.getItemDefinitions(rune.getId()).getName()+" to your rune pouch!");
		
	} 
	
	public static void empty(final Player player, final boolean small) {
		if(((small ? player.getSmallRunePouch() : player.getBigRunePouch())) == null)
			return;
		
		final Item[] runes = (small ? player.getSmallRunePouch() : player.getBigRunePouch()).values().toArray(new Item[(small ? player.getSmallRunePouch() : player.getBigRunePouch()).size()]);

		if(player.getInventory().getFreeSlots() < (small ? player.getSmallRunePouch() : player.getBigRunePouch()).size()) {
			player.sendMessage("You do not have enough inventory space!");
			return;
		}
		
		for(Item rune : runes) {
			player.removeRune(rune, small);
			player.getInventory().addItem(rune);
			player.sendMessage(Colors.GREEN+"Removed "+rune.getAmount()+"x "+ItemDefinitions.getItemDefinitions(rune.getId()).getName()+" from your rune pouch!");
		}
	}
	
	
}
