package com.rs.game.player;

import com.rs.Settings;
import com.rs.game.World;
import com.rs.game.item.Item;
import com.rs.game.player.content.ItemConstants;
import com.rs.utils.Lend;
import com.rs.utils.Logger;
import com.rs.utils.SerializableFilesManager;
import com.rs.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class LendingManager implements Serializable {

	private static final long serialVersionUID = 2496224027921198991L;
	public static CopyOnWriteArrayList<Lend> list;

	@SuppressWarnings("unchecked")
	public static void init() {
		File file = new File("data/lentItems.ser");
		if (file.exists()) {
			try {
				list = (CopyOnWriteArrayList<Lend>) SerializableFilesManager.loadSerializedFile(file);
				return;
			} catch (Exception e) {
				Logger.getGlobal().catching(e);
			}
		}
		list = new CopyOnWriteArrayList<Lend>();
	}

	public static void lend(Lend lend) {
		list.add(lend);
		save();
	}

	public static boolean hasLendedItem(Player player) {
		for (Lend lend : list) {
			if (lend.getLendee().equals(player.getUsername()))
				return true;
		}
		return false;
	}

	public static boolean hasLendedOut(Player player) {
		for (Lend lend : list) {
			if (lend.getLender().equals(player.getUsername()))
				return true;
		}
		return false;
	}

	public static Lend getLend(Player player) {
		for (Lend lend : list)
			if (lend.getLendee().equals(player.getUsername()))
				return lend;
		return null;
	}

	public static Lend getHasLendedItemsOut(Player player) {
		for (Lend lend : list)
			if (lend.getLender().equals(player.getUsername()))
				return lend;
		return null;
	}

	public static boolean hasLendedOn(Player player) {
		for (Item item : player.getInventory().getItems().toArray()) {
			if (item == null)
				continue;
			if (isLendedItem(player, item))
				return true;
		}
		for (Item item : player.getEquipment().getItemsContainer().toArray()) {
			if (item == null)
				continue;
			if (isLendedItem(player, item))
				return true;
		}
		return false;
	}

	public static void remove(Lend lend) {
		list.remove(lend);
		save();
	}

	public static void save() {
		try {
			SerializableFilesManager.storeSerializableClass(list, new File("data/lentItems.ser"));
		} catch (IOException e) {
			Logger.getGlobal().catching(e);
		}
	}

	public static int getHoursLeft(long millis) {
		long mil = millis - Utils.currentTimeMillis();
		long sec = (mil / 1000);
		long min = (sec / 60);
		int hour = 0;
		while (min > 60) {
			hour++;
			min -= 60;
		}
		return hour;
	}

	public static int getMinutesLeft(long millis) {
		long mil = millis - Utils.currentTimeMillis();
		long sec = (mil / 1000);
		long min = (sec / 60);
		while (min > 60) {
			min -= 60;
		}
		return (int) min;
	}

	public static boolean isLendedItem(Player player, Item item) {
		for (Lend lend : list)
			if (lend.getItem().getDefinitions().getLendId() == item.getId()
					&& lend.getLendee().equals(player.getUsername()))
				return true;
		return false;
	}

	public static void unLend(Lend lend) {
		boolean lenderLog = true;
		boolean lendeeLog = true;
		Player lender = World.getPlayer(lend.getLender());
		Player lendee = World.getPlayer(lend.getLendee());
		if (lender == null) {
			lenderLog = false;
			lender = SerializableFilesManager.loadPlayer(lend.getLender());
		}
		if (lendee == null) {
			lendeeLog = false;
			lendee = SerializableFilesManager.loadPlayer(lend.getLendee());
		}
		if (lendee.getInventory().containsItem(lend.getItem().getDefinitions().getLendId(), 1)) {
			lendee.getInventory().getItems().remove(new Item(lend.getItem().getDefinitions().getLendId()));
			if (lendeeLog)
				lendee.getInventory().refresh();
		}
		if (lendee.getEquipment().getItemsContainer()
				.containsOne(new Item(lend.getItem().getDefinitions().getLendId()))) {
			lendee.getEquipment().getItemsContainer().remove(new Item(lend.getItem().getDefinitions().getLendId()));
			if (lendeeLog) {
				lendee.getEquipment().init();
				lendee.getAppearence().generateAppearenceData();
			}
		}
		if (lendee.getBank().containsItem(lend.getItem().getDefinitions().getLendId(), 1)) {
			lendee.getBank().removeItem(new Item(lend.getItem().getDefinitions().getLendId(), 1));
			if (lendeeLog)
				lendee.getBank().refreshItems();
		}
		// lender.getBank().addItem(lend.getItem().getId(), 1, lenderLog);
		lender.getBank().addCollectableItem(lend.getItem().getId());
		if (lenderLog)
			lender.getPackets().sendGameMessage("<col=FF0000>An item you lent out is now available to be collected.");
		if (lendeeLog)
			lendee.getPackets().sendGameMessage("<col=FF0000>An item you borrowed has been returned to the owner.");
		try {
			if (!lenderLog) {
				lender.lendMessage = 1;
				SerializableFilesManager.storeSerializableClass(lender,
						new File("data/playersaves/characters/" + lend.getLender() + ".p"));
			}
			if (!lendeeLog) {
				lendee.lendMessage = 2;
				SerializableFilesManager.storeSerializableClass(lendee,
						new File("data/playersaves/characters/" + lend.getLendee() + ".p"));
			}
		} catch (IOException e) {
			Logger.getGlobal().catching(e);
		}
		list.remove(lend);
		save();
	}

	public static void processs() {
		List<Lend> toRemove = new ArrayList<Lend>();
		for (Lend lend : list) {
			if (lend.getTime() <= Utils.currentTimeMillis()) {
				toRemove.add(lend);
				boolean lenderLog = true;
				boolean lendeeLog = true;
				Player lender = World.getPlayer(lend.getLender());
				Player lendee = World.getPlayer(lend.getLendee());
				if (lender == null) {
					lenderLog = false;
					lender = SerializableFilesManager.loadPlayer(lend.getLender());
				}
				if (lendee == null) {
					lendeeLog = false;
					lendee = SerializableFilesManager.loadPlayer(lend.getLendee());
				}
				if (lendee.getInventory().containsItem(lend.getItem().getDefinitions().getLendId(), 1)) {
					lendee.getInventory().getItems().remove(new Item(lend.getItem().getDefinitions().getLendId()));
					if (lendeeLog)
						lendee.getInventory().refresh();
				}
				if (lendee.getEquipment().getItemsContainer()
						.containsOne(new Item(lend.getItem().getDefinitions().getLendId()))) {
					lendee.getEquipment().getItemsContainer()
							.remove(new Item(lend.getItem().getDefinitions().getLendId()));
					if (lendeeLog) {
						lendee.getEquipment().init();
						lendee.getAppearence().generateAppearenceData();
					}
				}
				if (lendee.getBank().containsItem(lend.getItem().getDefinitions().getLendId(), 1)) {
					lendee.getBank().removeItem(new Item(lend.getItem().getDefinitions().getLendId(), 1));
					if (lendeeLog)
						lendee.getBank().refreshItems();
				}
				lender.getBank().addItem(lend.getItem().getId(), 1, 0, null, lenderLog);
				if (lenderLog)
					lender.getPackets()
							.sendGameMessage("<col=FF0000>An item you lent out has been added back to your bank.");
				if (lendeeLog)
					lendee.getPackets()
							.sendGameMessage("<col=FF0000>An item you were lent has been returned to the owner.");
				try {
					if (!lenderLog) {
						lender.lendMessage = 1;
						SerializableFilesManager.storeSerializableClass(lender,
								new File("data/characters/" + lend.getLender() + ".p"));
					}
					if (!lendeeLog) {
						lendee.lendMessage = 2;
						SerializableFilesManager.storeSerializableClass(lendee,
								new File("data/characters/" + lend.getLendee() + ".p"));
					}
				} catch (IOException e) {
					Logger.getGlobal().catching(e);
				}
			}
		}
		list.removeAll(toRemove);
		if (toRemove.size() > 0)
			save();
		toRemove.clear();
	}

	public static void handleButtons(final Player player, int slotId) {
		Item item = player.getInventory().getItems().get(slotId);
		if (item == null)
			return;
		if (item.getDefinitions().getLendId() == -1) {
			player.getPackets().sendGameMessage("You can't lend that item.");
			return;
		}
		if (!ItemConstants.isTradeable(item)) {
			player.getPackets().sendGameMessage("You can't lend that item.");
			return;
		}
		if (hasLendedOut(player)) {
			player.getPackets().sendGameMessage("You have already lent out an item.");
			return;
		}
		if (item.getDefinitions().isLended()) {
			player.sendMessage("You can't lend an already lent item to you.");
			return;
		}
		if (Settings.LENDING_DISABLED) {
			player.sendMessage("Item lending is disabled.");
			return;
		}
		player.getTemporaryAttributtes().put("lend_item_time", slotId);
        player.getPackets().sendInputIntegerScript("How long do you wish to lend this item for?");
	}
}