package com.rs.game.player.content.dungeoneering;

import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.utils.data.parsers.items.ItemExaminesDataParser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DungeoneeringBinds implements Serializable {

	private static final long serialVersionUID = -7838252318287811910L;

	private final Item[] boundItems = new Item[10];
	private final Item[][] loadouts = new Item[3][5];
	private transient Player player;
	private final boolean[] activeLoadouts = new boolean[3];
	
	public void setPlayer(Player player) {
		this.player = player;
	}
	
	public DungeoneeringBinds() {
		activeLoadouts[0] = true;
	}
	
	private final Item getItem(int componentId) {
		if (componentId >= 44 && componentId <= 53)
			return boundItems[componentId - 44];
		if (componentId >= 59 && componentId <= 63)
			return loadouts[0][componentId - 59];
		if (componentId >= 69 && componentId <= 73)
			return loadouts[1][componentId - 63];
		if (componentId >= 79 && componentId <= 83)
			return loadouts[2][componentId - 79];
		return null;
	}
	
	public final boolean containsItem(Item item) {
		for (Item i : boundItems) {
			if (i == null)
				continue;
			if (i.getId() == item.getId())
				return true;
		}
		return false;
	}
	
	public final boolean processButtonClick(int interfaceId, int componentId) {
		if (interfaceId == 116) {
			if (componentId >= 44 && componentId <= 53) {
				Item item = getItem(componentId);
				if (item != null)
					player.sendMessage(ItemExaminesDataParser.getExamine(item));
			}
			if (componentId >= 59 && componentId <= 63)
				loadouts[0][componentId - 59] = null;
			if (componentId >= 69 && componentId <= 73)
				loadouts[1][componentId - 69] = null;
			if (componentId >= 79 && componentId <= 83)
				loadouts[2][componentId - 79] = null;
			updateInterface();
			return true;
		}
		return false;
	}

	public final boolean handleInterfaceSwitching(int interfaceId, int fromComponent, int toComponent) {
		if (interfaceId == 116) {
			Item item = getItem(fromComponent);
			if (item != null && toComponent == 17) {
				if (fromComponent >= 44 && fromComponent <= 53) {
					boundItems[fromComponent - 44] = null;
					for (int i = 0; i < 3; i++) {
						for (int x = 0; x < 5; x++) {
							if (loadouts[i][x] != null) {
								if (loadouts[i][x].getId() == item.getId())
									loadouts[i][x] = null;
							}
						}
					}
					updateInterface();
				} else {
					if (fromComponent >= 59 && fromComponent <= 63)
						loadouts[0][fromComponent - 59] = null;
					if (fromComponent >= 69 && fromComponent <= 73)
						loadouts[1][fromComponent - 63] = null;
					if (fromComponent >= 79 && fromComponent <= 83)
						loadouts[2][fromComponent - 79] = null;
					updateInterface();
				}
			} else if (item != null) {
				if (toComponent >= 59 && toComponent <= 63 || toComponent >= 69 && toComponent <= 73 || toComponent >= 79 && toComponent <= 83) {
					if (containsItem(item, toComponent < 64 ? 0 : toComponent < 74 ? 1 : 2)) 
						return true;
					loadouts[toComponent < 64 ? 0 : toComponent < 74 ? 1 : 2][toComponent - (toComponent < 64 ? 59 : toComponent < 74 ? 69 : 79)] = item;
					player.sendMessage("You add the " + item.getName() + " to your loadout.");
					updateInterface();
				}
			}
			return true;
		}
		return false;
	}
	
	private final boolean containsItem(Item item, int loadout) {
		for (Item i : loadouts[loadout]) {
			if (i == null)
				continue;
			if (i.getId() == item.getId()) {
				player.sendMessage("You've already added this item to that loadout.");
				return true;
			}
		}
		return false;
	}
	
	private final void updateInterface() {
		List<Item> i = new ArrayList<Item>(25);
		for (Item x : boundItems) {
			if (x != null)
				i.add(x);
			else
				i.add(new Item(-1, 1));
		}
		for (int a = 0; a < 3; a++) {
			for (int b = 0; b < 5; b++) {
				if (loadouts[a][b] != null)
					i.add(loadouts[a][b]);
				else
					i.add(new Item(-1, 1));
			}
		}
		player.getPackets().sendItems(573, i.toArray(new Item[i.size()]));
	}
	
	public void bindItem(Item item) {
		boolean full = true;
		for (Item i : boundItems)
			if (i == null)
				full = false;
		if (full) {
			player.getPackets().sendGameMessage("A currently bound item must be destroyed before another item may be bound.");
			return;
		}
		int bindId = DungeonUtils.getBindedId(item);
		if (bindId == -1)
			return;
		Item bound = new Item(bindId, 1);
		if (DungeonUtils.isBindAmmo(item)) {
			for (Item bounds : boundItems) {
				if (bounds == null)
					continue;
				if (bounds.getId() == bound.getId())
					bound.setAmount(bounds.getAmount() + item.getAmount() > 255 ? 255 - bounds.getAmount() : item.getAmount());
			}
		} else {
			if (containsItem(bound)) {
				player.sendMessage("You've already bound this item to you.");
				return;
			}
		}
		item.setId(bindId);
		player.getInventory().refresh();
		player.sendMessage("You bind the " + item.getName().replaceAll(" (b)", "") + " to you. Check in the smuggler to manage your bound items.");
		for (int i = 0; i < 10; i++)
			if (boundItems[i] == null) {
				boundItems[i] = bound;
				break;
			}
	}
	
	public void unbindItem(final Item item) {
		for (int i = 0; i < 10; i++) {
			if (boundItems[i] == null)
				continue;
			if (boundItems[i].getId() == item.getId()) {
				player.sendMessage("You've unbound the " + item.getName().replaceAll(" (b)", "") + ".");
				boundItems[i] = null;
				break;
			}
		}
	}
	
	public void sendInterface() {
		for (int i = 0; i < 3; i++)
			player.getPackets().sendIComponentSprite(116, 18 + i, i == 2 ? 6012 : 6009);
		updateInterface();
		for (int i = 21; i < 24; i++)
			player.getPackets().sendHideIComponent(116, i, true);
		player.getInterfaceManager().sendInterface(116);

	}
	
}
