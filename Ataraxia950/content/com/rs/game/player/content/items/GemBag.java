package com.rs.game.player.content.items;

import com.rs.game.item.Item;
import com.rs.game.player.Player;

import java.io.Serializable;

public class GemBag implements Serializable {

	private static final long serialVersionUID = -932842085617754012L;
	private transient Player player;
	private int sapphire, emerald, ruby, diamond;
	
	public GemBag(Player player) {
		this.player = player;
	}
	
	private int getTotal() {
		return sapphire + emerald + ruby + diamond;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public boolean inspectBag(int itemId) {
		if (itemId != 18338)
			return false;
		if (getTotal() == 0) 
			player.sendMessage("Your gem bag is currently empty.");
		else
			player.sendMessage("Your gem bag currently holds " + sapphire + " uncut sapphire" + (sapphire == 1 ? "" : "s") + ", " + emerald + " uncut emerald" + (emerald == 1 ? "" : "s") + ", " + ruby + " uncut rub" + (ruby == 1 ? "y" : "ies") + " and " + diamond + " uncut diamond" + (diamond == 1 ? "" : "s") + ".");
		return true;
	}
	
	public boolean withdraw(int itemId) {
		if (itemId != 18338)
			return false;
		int iSpace = player.getInventory().getFreeSlots();
		if (getTotal() == 0) 
			player.sendMessage("Your gem bag is currently empty.");
		else if (iSpace == 0)
			player.sendMessage("You haven't got enough free inventory space to do this.");
		else {
			for (int i = 1617; i < 1924; i += 2)
				withdrawGem(i);
			player.sendMessage("You withdraw some gems.", true);
		}
		return true;
	}
	
	public void empty() {
		sapphire = 0;
		emerald = 0;
		ruby = 0;
		diamond = 0;
	}
	
	private void withdrawGem(int itemId) {
		int iSpace = player.getInventory().getFreeSlots();
		int gem = itemId == 1623 ? sapphire : itemId == 1621 ? emerald : itemId == 1619 ? ruby : diamond;
		int withdrawGems = gem > iSpace ? iSpace : gem;
		if (withdrawGems != 0) {
			player.getInventory().addItem(new Item(itemId, withdrawGems));
			if (itemId == 1623)
				sapphire -= withdrawGems;
			else if (itemId == 1621)
				emerald -= withdrawGems;
			else if (itemId == 1619)
				ruby -= withdrawGems;
			else
				diamond -= withdrawGems;
		}
	}
	
	public boolean fill(int itemId) {
		if (itemId != 18338)
			return false;
		int gems = 0;
		for (int i = 1617; i < 1924; i += 2)
			gems += player.getInventory().getAmountOf(i);
		if (getTotal() == 100)
			player.sendMessage("Your gem bag cannot hold any more gems.");
		else if (gems == 0)
			player.sendMessage("You have no gems to fill the bag with.");
		else {
			for (int i = 0; i < 28; i++) {
				if (player.getInventory().getItem(i) == null)
					continue;
				if (getTotal() == 100)
					break;
				if (player.getInventory().getItem(i).getId() == 1617 || player.getInventory().getItem(i).getId() == 1619 || player.getInventory().getItem(i).getId() == 1621 || player.getInventory().getItem(i).getId() == 1623) 
					fillGem(player.getInventory().getItem(i).getId());
			}
			player.sendMessage("You add some gems to your gem bag.");
		}
		return true;
	}
	
	private void fillGem(int id) {
		player.getInventory().deleteItem(id, 1);
		id = id == 1623 ? sapphire++ : id == 1621 ? emerald++ : id == 1619 ? ruby++ : diamond++;
	}
	
}
