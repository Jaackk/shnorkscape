package com.rs.game.player.content.decantation;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.World;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.content.Pots.Pot;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Decanting {

	private final Player player;
	private final HashMap<DecantingPot, Integer> heldPotions;
	private final HashMap<DecantingPot, Integer> heldFlasks;
	private int emptyFlasks, emptyPotions, emptyNFlasks, emptyNPotions;
	private HashMap<Integer, Integer> decantedPotions;
	private HashMap<Integer, Integer> decantedFlasks;
	private DecantingPot previousPot;
	private boolean notEnoughSpace;
	
	public Decanting(Player player) {
		this.player = player;
		heldPotions = new HashMap<DecantingPot, Integer>();
		heldFlasks = new HashMap<DecantingPot, Integer>();
		decantedPotions = new HashMap<Integer, Integer>();
		decantedFlasks = new HashMap<Integer, Integer>();
	}
	
	public void decantAllPotions() {
		for (Item items : player.getInventory().getItems().getItems()) {
			if (items == null)
				continue;
			for (Pot potions : Pot.values()) {
				if (potions == Pot.BEER || potions == Pot.JUG)
					continue;
				int dosesIndex = 0;
				for (int doses : potions.getPotions()) {
					if (doses == potions.getPotions()[0]) {
						dosesIndex++;
						continue;
					}
					int notedId = -1;
					if (items.getDefinitions().isNoted())
						notedId = ItemDefinitions.getItemDefinitions(doses).certId;
					if (potions.isPotion() && doses == items.getId() || notedId != -1 && potions.isPotion() && items.getId() == notedId) {
						previousPot = new DecantingPot(potions, items.getDefinitions().isNoted());
						heldPotions.forEach((k, v) -> {
							if (k.getPot() == potions && k.isNoted() == items.getDefinitions().isNoted())
								previousPot = k;
							
						});
						heldPotions.put(previousPot, heldPotions.get(previousPot) == null ? (4 - dosesIndex) * items.getAmount() : ((4 - dosesIndex) * items.getAmount()) + heldPotions.get(previousPot));
						if (notedId == -1)
							emptyPotions++;
						else
							emptyNPotions += items.getAmount();
					} else if (potions.isFlask() && doses == items.getId() || notedId != -1 && potions.isFlask() && items.getId() == notedId) {
						previousPot = new DecantingPot(potions, items.getDefinitions().isNoted());
						heldFlasks.forEach((k, v) -> {
							if (k.getPot() == potions && k.isNoted() == items.getDefinitions().isNoted())
								previousPot = k;
						});
						heldFlasks.put(previousPot, heldFlasks.get(previousPot) == null ? (6 - dosesIndex) * items.getAmount() : ((6 - dosesIndex) * items.getAmount()) + heldFlasks.get(previousPot));
						if (notedId == -1) 
							emptyFlasks++;
						 else
							emptyNFlasks += items.getAmount();
					}
					dosesIndex++;
				}
			}
		}
		for (Item items : player.getInventory().getItems().getItems()) {
			if (items == null)
				continue;
			for (Pot potions : Pot.values()) {
				for (int doses : potions.getPotions()) {
					int notedId = ItemDefinitions.getItemDefinitions(doses).certId;
					if (doses == items.getId() && doses != potions.getPotions()[0] || notedId != -1 && notedId == items.getId() && doses != potions.getPotions()[0]) {
						player.getInventory().deleteItem(items.getId(), items.getAmount());
					}
				}
			}
		}
		heldFlasks.forEach((flask, doses) -> {
			int fullFlasks = (int) Math.floor(doses / 6);
			int remainingFlask = doses % 6;
			boolean noted = flask.isNoted();
			if (fullFlasks != 0) {
				decantedFlasks.put(noted ? ItemDefinitions.getItemDefinitions(flask.getPot().id[0]).certId : flask.getPot().id[0], fullFlasks);
				if (noted)
					emptyNFlasks -= fullFlasks;
				else
					emptyFlasks -= fullFlasks;
			} if (remainingFlask != 0) {
				decantedFlasks.put(noted ? ItemDefinitions.getItemDefinitions(flask.getPot().id[6 - remainingFlask]).certId : flask.getPot().id[6 - remainingFlask], 1);
				if (noted)
					emptyNFlasks--;
				else
					emptyFlasks--;
			}
		});
		heldPotions.forEach((potion, doses) -> {
			int fullPotions = (int) Math.floor(doses / 4);
			int remainingPotion = doses % 4;
			boolean noted = potion.isNoted();
			if (fullPotions != 0) {
				decantedPotions.put(noted ? ItemDefinitions.getItemDefinitions(potion.getPot().id[0]).certId : potion.getPot().id[0], fullPotions);
				if (noted)
					emptyNPotions -= fullPotions;
				else
					emptyPotions -= fullPotions;
			} if (remainingPotion != 0) {
				decantedPotions.put(noted ? ItemDefinitions.getItemDefinitions(potion.getPot().id[4 - remainingPotion]).certId : potion.getPot().id[4 - remainingPotion], 1);
				if (noted)
					emptyNPotions--;
				else
					emptyPotions--;
			}
		});
		decantedFlasks = sortByValues(decantedFlasks);
		decantedPotions = sortByValues(decantedPotions);
		decantedFlasks.forEach((flask, amount) -> {
			if (player.getInventory().hasFreeSlots())
				player.getInventory().addItem(new Item(flask, amount));
			else {
				World.addGroundItem(new Item(flask, amount), player, player, true, 180);
				notEnoughSpace = true;
			}
		});
		decantedPotions.forEach((potion, amount) ->{
			if (player.getInventory().hasFreeSlots())
				player.getInventory().addItem(new Item(potion, amount));
			else {
				World.addGroundItem(new Item(potion, amount), player, player, true, 180);
				notEnoughSpace = true;
			}
		});
		if (emptyNFlasks > 0) 
			addItem(23192, emptyNFlasks);
		if (emptyNPotions > 0) 
			addItem(230, emptyNPotions);
		if (emptyFlasks > 0) 
			addItem(23191, emptyFlasks);
		if (emptyPotions > 0)
			addItem(229, emptyPotions);
		if (notEnoughSpace)
			player.sendMessage("One or more of the vessels have been placed beneath you due to lack of space.");
	}
	
	@SuppressWarnings("all")
	public HashMap<Integer, Integer> sortByValues(HashMap map) {
		List list = new LinkedList(map.entrySet());
		Collections.sort(list, new Comparator() {
			public int compare(Object o2, Object o1) {
				return ((Comparable) ((Map.Entry) (o1)).getValue()).compareTo(((Map.Entry) (o2)).getValue());
			}
		});
		HashMap sortedHashMap = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			sortedHashMap.put(entry.getKey(), entry.getValue());
		}
		return sortedHashMap;
	}
	
	private void addItem(int id, int amount) {
		if (player.getInventory().getFreeSlots() >= amount || ItemDefinitions.getItemDefinitions(id).isNoted() && player.getInventory().hasFreeSlots() || ItemDefinitions.getItemDefinitions(id).isNoted() && player.getInventory().containsItem(id, 1))
			player.getInventory().addItem(new Item(id, amount));
		else {
			int freeSlots = player.getInventory().getFreeSlots();
			if (player.getInventory().hasFreeSlots()) 
				player.getInventory().addItem(new Item(id, freeSlots));
			World.addGroundItem(new Item(id, amount - freeSlots), player, player, true, 180);
			notEnoughSpace = true;
		}
	}
	
}

class DecantingPot {
	
	private final Pot pot;
	private final boolean noted;
	
	DecantingPot(Pot pot, boolean noted) {
		this.pot = pot;
		this.noted = noted;
	}
	
	public Pot getPot() {
		return pot;
	}
	
	public boolean isNoted() {
		return noted;
	}
	
}
