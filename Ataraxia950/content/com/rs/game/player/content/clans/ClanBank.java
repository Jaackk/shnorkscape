package com.rs.game.player.content.clans;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.item.ItemsContainer;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.player.Player;
import com.rs.game.player.content.ItemConstants;
import com.rs.game.player.content.grandExchange.GrandExchange;
import com.rs.network.packet.PacketDispatcher;
import com.rs.utils.PushList;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.items.ItemExaminesDataParser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * ClanBank.java | 3:07:08 PM
 * 
 * @author Chryonic
 * @date Feb 21, 2017
 */
public class ClanBank implements Serializable {

	private static final long serialVersionUID = -5779788019572863016L;

	private Item[][] bankTabs;
	private PushList<String> history = new PushList<String>(300);
	private Hashtable<String, Long> playerDeposits;
	private Hashtable<Integer, Integer> clanBankRestrictions;
	private boolean second;

	private transient ArrayList<Player> players = new ArrayList<Player>();
	private transient Item[] lastContainerCopy;

	private static final long MAX_BANK_SIZE = 516;

	public ClanBank() {
		bankTabs = new Item[1][0];
		players = new ArrayList<Player>();
	}

	public ArrayList<Player> getPlayers() {
		ArrayList<Player> newPlayers = new ArrayList<Player>();
		for (Player player : players) {
			if (player.hasFinished() || player.getTemporaryAttributtes().get("clanBank") == null)
				continue;
			newPlayers.add(player);
		}
		players.clear();
		players.addAll(newPlayers);
		return newPlayers;
	}

	public void addItem(Player player, int id, int quantity, boolean refresh) {
		addItem(player, id, quantity, player.getBank().getCurrentTab(), refresh);
	}

	public void addItem(Player player, int id, int quantity, int creationTab, boolean refresh) {
		final int[] slotInfo = getItemSlot(id);
		if (slotInfo == null) {
			if (creationTab >= bankTabs.length)
				creationTab = bankTabs.length - 1;
			if (creationTab < 0) // fixed now, alex
				creationTab = 0;
			final int slot = bankTabs[creationTab].length;
			final Item[] tab = new Item[slot + 1];
			System.arraycopy(bankTabs[creationTab], 0, tab, 0, slot);
			tab[slot] = new Item(id, quantity);
			bankTabs[creationTab] = tab;
			if (refresh)
				refreshTab(player, creationTab);
		} else {
			final Item item = bankTabs[slotInfo[0]][slotInfo[1]];
			bankTabs[slotInfo[0]][slotInfo[1]] = new Item(item.getId(), item.getAmount() + quantity);
		}
		if (refresh)
			refreshItems(player);
	}

	public void addItem(Player player, Item item, boolean refresh) {
		addItem(player, item.getId(), item.getAmount(), refresh);
	}

	public void collapse(Player player, int tabId) {
		if (tabId == 0 || tabId >= bankTabs.length)
			return;
		final Item[] items = bankTabs[tabId];
		for (final Item item : items)
			removeItem(player, getItemSlot(item.getId()), item.getAmount(), false, true);
		for (final Item item : items)
			addItem(player, item.getId(), item.getAmount(), 0, false);
		refreshTabs(player);
		refreshItems(player);
	}

	public void createTab() {
		final int slot = bankTabs.length;
		final Item[][] tabs = new Item[slot + 1][];
		System.arraycopy(bankTabs, 0, tabs, 0, slot);
		tabs[slot] = new Item[0];
		bankTabs = tabs;
	}

	public void depositAllBob(Player player, boolean banking) {
		final Familiar familiar = player.getFamiliar();
		if (familiar == null || familiar.getBob() == null)
			return;
		boolean notEnoughSpace = false;
		for (int i = 0; i < player.getFamiliar().getBob().getBeastItems().getSize(); i++) {
			if (depositAllItem(player, player.getFamiliar().getBob().getBeastItems().get(i), Integer.MAX_VALUE, true)) {
				notEnoughSpace = true;
			}
		}
		if (notEnoughSpace) {
			player.sm("Not enough space in your bank.");
		}
		// LogSystem.clanBankLog(player, notEnoughSpace, null,
		// player.getClanManager().getClan().getClanName());
		refreshTab(player, player.getBank().getCurrentTab());
		refreshItems(player);
		familiar.getBob().sendInterItems();
	}

	public void depositAllEquipment(Player player, boolean banking) {
		boolean notEnoughSpace = false;
		for (int i = 0; i < player.getEquipment().getItems().getSize(); i++) {
			if (depositAllItem(player, player.getEquipment().getItem(i), Integer.MAX_VALUE, false)) {
				notEnoughSpace = true;
			}
		}
		if (notEnoughSpace) {
			player.sm("Not enough space in your bank.");
		}
		player.getEquipment().init();
		player.getAppearence().generateAppearenceData();
		refreshTab(player, player.getBank().getCurrentTab());
		refreshItems(player);
	}

	public void depositAllInventory(Player player, boolean banking) {
		boolean notEnoughSpace = false;
		for (int i = 0; i < 28; i++) {
			if (depositItem(player, i, Integer.MAX_VALUE, false)) {
				notEnoughSpace = true;
			}
		}
		if (notEnoughSpace) {
			player.sm("Not enough space in your bank.");
		}
		refreshTab(player, player.getBank().getCurrentTab());
		refreshItems(player);
	}

	public boolean depositItem(Player player, int invSlot, int quantity, boolean refresh) {

		if (quantity < 1 || invSlot < 0 || invSlot > 27)
			return false;

		Item item = player.getInventory().getItem(invSlot);

		if (item == null)
			return false;

		if (!ItemConstants.isTradeable(item)) {
			player.sm(item.getDefinitions().getName()
					+ " is not tradeable and therefore cannot be stored in the Clan Bank.");
			return false;
		}

		if (item.getId() == 995) {
			player.sm("Currency cannot be added to the Clan Bank.");
			return false;
		}

		final int amt = player.getInventory().getItems().getNumberOf(item);

		if (amt < quantity)
			item = new Item(item.getId(), amt);
		else
			item = new Item(item.getId(), quantity);

		final ItemDefinitions defs = item.getDefinitions();

		final int originalId = item.getId();

		if (defs.isNoted() && defs.getCertId() != -1)
			item.setId(defs.getCertId());

		final Item bankedItem = getItem(item.getId());
		boolean notEnoughSpace = false;
		if (bankedItem != null) {
			if (bankedItem.getAmount() + item.getAmount() <= 0) {
				item.setAmount(Integer.MAX_VALUE - bankedItem.getAmount());
				player.sm("Not enough space in your bank.");
				notEnoughSpace = true;
			}
		} else if (!hasBankSpace()) {
			player.sm("Not enough space in your bank.");
			return true;
		}

		player.getInventory().deleteItem(invSlot, new Item(originalId, item.getAmount()));

		int value = new Item(item.getId(), item.getAmount()).getDefinitions().getValue() * item.getAmount();
		setDeposit(player.getUsername(), getDeposit(player.getUsername()) + value);
		player.sm("You have deposited a total of " + Utils.moneyToString(getDeposit(player.getUsername())) + ".");
		addHistory(player, item, "deposited");
		addItem(player, item, refresh);
		// LogSystem.clanBankLog(player, false, bankedItem,
		// player.getClanManager().getClan().getClanName());
		return notEnoughSpace;
	}

	public boolean depositAllItem(Player player, Item item, int quantity, boolean bob) {
		if (item == null)
			return false;
		if (!ItemConstants.isTradeable(item)) {
			player.sm(item.getDefinitions().getName()
					+ " is not tradeable and therefore cannot be stored in the Clan Bank.");
			return false;
		}
		if (item.getId() == 995) {
			player.sm("Currency cannot be added to the Clan Bank.");
			return false;
		}
		final int amt = item.getAmount();
		if (amt < quantity)
			item = new Item(item.getId(), amt);
		else
			item = new Item(item.getId(), quantity);
		final ItemDefinitions defs = item.getDefinitions();
		final int originalId = item.getId();
		if (defs.isNoted() && defs.getCertId() != -1)
			item.setId(defs.getCertId());
		final Item bankedItem = getItem(item.getId());
		boolean notEnoughSpace = false;
		if (bankedItem != null) {
			if (bankedItem.getAmount() + item.getAmount() <= 0) {
				item.setAmount(Integer.MAX_VALUE - bankedItem.getAmount());
				player.sm("Not enough space in your bank.");
				notEnoughSpace = true;
			}
		} else if (!hasBankSpace()) {
			return true;
		}
		if (bob)
			player.getFamiliar().getBob().getBeastItems().remove(new Item(originalId, item.getAmount()));
		else
			player.getEquipment().deleteItem(originalId, item.getAmount());
		int value = new Item(item.getId(), item.getAmount()).getDefinitions().getValue() * item.getAmount();
		setDeposit(player.getUsername(), getDeposit(player.getUsername()) + value);
		// LogSystem.clanBankLog(player, false, bankedItem,
		// player.getClanManager().getClan().getClanName());
		player.sm("You have deposited a total of " + Utils.moneyToString(getDeposit(player.getUsername())) + ".");
		addHistory(player, item, "deposited");
		addItem(player, item, false);
		return notEnoughSpace;
	}

	public void depositLastAmount(Player player, int bankSlot) {
		depositItem(player, bankSlot, player.getBank().getLastX(), true);
	}

	public void depositOtherItem(Player player, ItemsContainer<Item> container, int slot, int quantity,
			boolean refresh) {
		Item item = container.get(slot);
		if (item == null)
			return;
		if (!ItemConstants.isTradeable(item)) {
			player.sm(item.getDefinitions().getName()
					+ " is not tradeable and therefore cannot be stored in the Clan Bank.");
			return;
		}
		if (item.getId() == 995) {
			player.sm("Currency cannot be added to the Clan Bank.");
			return;
		}
		final int amt = item.getAmount();
		if (amt < quantity)
			item = new Item(item.getId(), amt);
		else
			item = new Item(item.getId(), quantity);
		final ItemDefinitions defs = item.getDefinitions();
		final int originalId = item.getId();
		if (defs.isNoted() && defs.getCertId() != -1)
			item.setId(defs.getCertId());
		final Item bankedItem = getItem(item.getId());
		int amountLeft = 0;
		if (bankedItem != null) {
			if (bankedItem.getAmount() + item.getAmount() <= 0) {
				final int amount = item.getAmount();
				item.setAmount(Integer.MAX_VALUE - bankedItem.getAmount());
				player.sm("Not enough space in your bank.");
				amountLeft = amount - item.getAmount();
			}
		} else if (!hasBankSpace()) {
			player.sm("Not enough space in your bank.");
			return;
		}
		if (amountLeft == 0)
			container.set(slot, null);
		else
			container.set(slot, new Item(originalId, amountLeft));
		int value = new Item(item.getId(), item.getAmount()).getDefinitions().getValue() * item.getAmount();
		setDeposit(player.getUsername(), getDeposit(player.getUsername()) + value);
		// LogSystem.clanBankLog(player, false, bankedItem,
		// player.getClanManager().getClan().getClanName());

		player.sm("You have deposited a total of " + Utils.moneyToString(getDeposit(player.getUsername())) + ".");
		addHistory(player, item, "deposited");
		addItem(player, item, refresh);
	}

	public void destroyTab(Player player, int slot) {
		final Item[][] tabs = new Item[bankTabs.length - 1][];
		System.arraycopy(bankTabs, 0, tabs, 0, slot);
		System.arraycopy(bankTabs, slot + 1, tabs, slot, bankTabs.length - slot - 1);
		bankTabs = tabs;
		if (player.getBank().getCurrentTab() != 0 && player.getBank().getCurrentTab() >= slot)
			player.getBank().setCurrentTab(player.getBank().getCurrentTab() - 1);
	}

	public Item[] generateContainer() {
		final Item[] container = new Item[getBankSize()];
		int count = 0;
		for (int slot = 1; slot < bankTabs.length; slot++) {
			System.arraycopy(bankTabs[slot], 0, container, count, bankTabs[slot].length);
			count += bankTabs[slot].length;
		}
		System.arraycopy(bankTabs[0], 0, container, count, bankTabs[0].length);
		return container;
	}

	public int getBankSize() {
		int size = 0;
		for (int i = 0; i < bankTabs.length; i++)
			size += bankTabs[i].length;
		return size;
	}

	public Item[] getContainerCopy() {
		if (lastContainerCopy == null)
			lastContainerCopy = generateContainer();
		return lastContainerCopy;
	}

	public Item getItem(int id) {
		for (int slot = 0; slot < bankTabs.length; slot++) {
			for (final Item item : bankTabs[slot])
				if (item.getId() == id)
					return item;
		}
		return null;
	}

	public Item getItem(int[] slot) {
		if (slot == null)
			return null;
		return bankTabs[slot[0]][slot[1]];
	}

	public int[] getItemSlot(int id) {
		for (int tab = 0; tab < bankTabs.length; tab++) {
			for (int slot = 0; slot < bankTabs[tab].length; slot++)
				if (bankTabs[tab][slot].getId() == id)
					return new int[] { tab, slot };
		}
		return null;
	}

	public int[] getRealSlot(int slot) {
		for (int tab = 1; tab < bankTabs.length; tab++) {
			if (slot >= bankTabs[tab].length)
				slot -= bankTabs[tab].length;
			else
				return new int[] { tab, slot };
		}
		if (slot >= bankTabs[0].length)
			return null;
		return new int[] { 0, slot };
	}

	public int getStartSlot(int tabId) {
		int slotId = 0;
		for (int tab = 1; tab < (tabId == 0 ? bankTabs.length : tabId); tab++)
			slotId += bankTabs[tab].length;

		return slotId;

	}

	public int getTabSize(int slot) {
		if (slot >= bankTabs.length)
			return 0;
		return bankTabs[slot].length;
	}

	public boolean hasBankSpace() {
		return getBankSize() < MAX_BANK_SIZE;
	}

	public void insert(Player player, int fromSlot, int toSlot) {
		final int[] fromRealSlot = getRealSlot(fromSlot);
		final Item fromItem = getItem(fromRealSlot);
		if (fromItem == null)
			return;

		final int[] toRealSlot = getRealSlot(toSlot);
		final Item toItem = getItem(toRealSlot);
		if (toItem == null)
			return;

		if (toRealSlot[0] != fromRealSlot[0])
			return;

		if (toRealSlot[1] > fromRealSlot[1]) {
			for (int slot = fromRealSlot[1]; slot < toRealSlot[1]; slot++) {
				final Item temp = bankTabs[toRealSlot[0]][slot];
				bankTabs[toRealSlot[0]][slot] = bankTabs[toRealSlot[0]][slot + 1];
				bankTabs[toRealSlot[0]][slot + 1] = temp;
			}
		} else if (fromRealSlot[1] > toRealSlot[1]) {
			for (int slot = fromRealSlot[1]; slot > toRealSlot[1]; slot--) {
				final Item temp = bankTabs[toRealSlot[0]][slot];
				bankTabs[toRealSlot[0]][slot] = bankTabs[toRealSlot[0]][slot - 1];
				bankTabs[toRealSlot[0]][slot - 1] = temp;
			}
		}
		refreshItems(player);
	}

	public void openBank(final Player player) {
		if (player.getRights() >= 2) {
			player.sm("Admins cannot use this feature.");
			return;

		} else if (player.isIronMan() || 
        		player.isNoviceIronMan() || player.isExpertIronMan() || player.isIntermediateIronMan()) {
			player.sm("Iron Man mode cannot access clan banks.");
			return;
		} else if (player.isKingOfTheSkillGameMode()) {
			player.getKingOfTheSkillGameModeHandler().sendNoClanBankMessageToPlayer();
			return;
		}
		player.stopAll();
		player.getTemporaryAttributtes().remove("checkBank");
		player.getTemporaryAttributtes().put("clanBank", Boolean.TRUE);
//		player.getInterfaceManager().sendInterface(762);
//		player.getInterfaceManager().sendInventoryInterface(763);
//		player.getPackets().sendHideIComponent(762, 22, false);
//		player.getPackets().sendHideIComponent(762, 41, false);
//		player.getPackets().sendIComponentText(762, 47,
//				"Clan Bank of: " + player.getClanManager().getClan().getClanName());
		player.getBank().refreshViewingTab();
		refreshTabs(player);
		player.getBank().unlockButtons();
		sendItems(player);
		player.getBank().refreshLastX();
		if (players == null)
			players = new ArrayList<Player>();
		players.add(player);
		player.setCloseInterfacesEvent(new Runnable() {
			@Override
			public void run() {
				removePlayer(player);
			}
		});
	}

	public void refreshItems(Player player) {
		refreshItems(player, generateContainer(), getContainerCopy());
	}

	public void refreshItems(Player mainPlayer, int[] slots) {
		if (players == null)
			players = new ArrayList<Player>();
		mainPlayer.getPackets().sendUpdateItems(95, getContainerCopy(), slots);
		for (final Player player : players) {
			if (player != null && !player.hasFinished()) {
				player.getPackets().sendUpdateItems(95, getContainerCopy(), slots);
			}
		}
	}

	public void refreshItems(Player player, Item[] itemsAfter, Item[] itemsBefore) {
		if (itemsBefore.length != itemsAfter.length) {
			lastContainerCopy = itemsAfter;
			sendItems(player);
			return;
		}
		final int[] changedSlots = new int[itemsAfter.length];
		int count = 0;
		for (int index = 0; index < itemsAfter.length; index++) {
			if (itemsBefore[index] != itemsAfter[index])
				changedSlots[count++] = index;
		}
		final int[] finalChangedSlots = new int[count];
		System.arraycopy(changedSlots, 0, finalChangedSlots, 0, count);
		lastContainerCopy = itemsAfter;
		refreshItems(player, finalChangedSlots);
	}

	public void refreshTabs(Player mainPlayer) {
		for (int slot = 1; slot < 9; slot++)
			refreshTab(mainPlayer, slot);
	}

	public void refreshTab(Player mainPlayer, int slot) {
		if (slot == 0)
			return;
		if (players == null)
			players = new ArrayList<Player>();
		mainPlayer.getPackets().sendConfigByFile(4885 + (slot - 1), getTabSize(slot));
		for (final Player player : players) {
			if (player != null && !player.hasFinished()) {
				mainPlayer.getPackets().sendConfigByFile(4885 + (slot - 1), getTabSize(slot));
			}
		}
	}

	public void removeItem(int id) {
		if (bankTabs != null) {
			for (int i = 0; i < bankTabs.length; i++) {
				for (int i2 = 0; i2 < bankTabs[i].length; i2++) {
					if (bankTabs[i][i2].getId() == id)
						bankTabs[i][i2].setId(0); // dwarf remains
				}
			}
		}
	}

	public boolean removeItem(Player player, int fakeSlot, int quantity, boolean refresh, boolean forceDestroy) {
		return removeItem(player, getRealSlot(fakeSlot), quantity, refresh, forceDestroy);
	}

	public boolean removeItem(Player player, int[] slot, int quantity, boolean refresh, boolean forceDestroy) {
		if (slot == null)
			return false;
		final Item item = bankTabs[slot[0]][slot[1]];
		boolean destroyed = false;
		if (quantity >= item.getAmount()) {
			if (bankTabs[slot[0]].length == 1 && (forceDestroy || bankTabs.length != 1)) {
				destroyTab(player, slot[0]);
				if (refresh)
					refreshTabs(player);
				destroyed = true;
			} else {
				final Item[] tab = new Item[bankTabs[slot[0]].length - 1];
				System.arraycopy(bankTabs[slot[0]], 0, tab, 0, slot[1]);
				System.arraycopy(bankTabs[slot[0]], slot[1] + 1, tab, slot[1], bankTabs[slot[0]].length - slot[1] - 1);
				bankTabs[slot[0]] = tab;
				if (refresh)
					refreshTab(player, slot[0]);
			}
		} else
			bankTabs[slot[0]][slot[1]] = new Item(item.getId(), item.getAmount() - quantity);
		if (refresh)
			refreshItems(player);
		return destroyed;
	}

	public void removePlayer(Player player) {
		if (players == null)
			players = new ArrayList<Player>();
		if (player != null)
			players.remove(player);
	}

	public void sendExamine(Player player, int fakeSlot) {
		final int[] slot = getRealSlot(fakeSlot);
		if (slot == null)
			return;
		final Item item = bankTabs[slot[0]][slot[1]];
		player.sm(ItemExaminesDataParser.getExamine(item));
		player.getPackets().sendGameMessage("Grand Exchange guide price: " + Utils.getFormattedNumber(GrandExchange.getPrice(item)) + ".");
	}

	public void sendItems(Player mainPlayer) {
		if (players == null)
			players = new ArrayList<Player>();
		mainPlayer.getPackets().sendItems(95, getContainerCopy());
		sendBankSize(mainPlayer);
		for (final Player player : players) {
			if (player != null && !player.hasFinished()) {
				player.getPackets().sendItems(95, getContainerCopy());
				sendBankSize(player);
			}
		}
	}

	public void sendBankSize(Player mainPlayer) {
		// player.getPackets().sendIComponentText(762, 29,
		// ""+getBankSize());
		mainPlayer.getPackets().sendIComponentText(762, 31, "" + getBankSize());
		// player.getPackets().sendIComponentText(762, 30,
		// "516");
		mainPlayer.getPackets().sendIComponentText(762, 32, "516");
	}

	@SuppressWarnings("null")
	public void setItem(Player player, int slotId, int amt) {
		final Item item = getItem(slotId);
		if (item == null) {
			item.setAmount(amt);
			refreshItems(player);
			refreshTabs(player);
			player.getBank().refreshViewingTab();
		}
	}

	public void switchItem(Player player, int fromSlot, int toSlot) {
		final int[] fromRealSlot = getRealSlot(fromSlot);
		final Item fromItem = getItem(fromRealSlot);
		if (fromItem == null)
			return;
		final int[] toRealSlot = getRealSlot(toSlot);
		final Item toItem = getItem(toRealSlot);
		if (toItem == null)
			return;
		bankTabs[fromRealSlot[0]][fromRealSlot[1]] = toItem;
		bankTabs[toRealSlot[0]][toRealSlot[1]] = fromItem;
		refreshTab(player, fromRealSlot[0]);
		if (fromRealSlot[0] != toRealSlot[0])
			refreshTab(player, toRealSlot[0]);
		refreshItems(player);
	}

	public void switchItem(Player player, int fromSlot, int toSlot, int fromComponentId, int toComponentId) {
		if (player.getClanManager() != null && player.getClanManager().getClan() != null
				&& player.getClanManager().getRank(player) <= Clan.RECRUIT) {
			player.sm("You must be of a rank higher than recruit to move items around in the clan bank.");
			return;
		}
		if (toSlot == 65535 || toSlot == -1) {
			int toTab = toComponentId >= 76 ? 8 - (84 - toComponentId) : 9 - ((toComponentId - 46) / 2);
			if (toTab < 0 || toTab > 9)
				return;
			if (bankTabs.length == toTab) {
				final int[] fromRealSlot = getRealSlot(fromSlot);
				if (fromRealSlot == null)
					return;
				if (toTab == fromRealSlot[0]) {
					switchItem(player, fromSlot, getStartSlot(toTab));
					return;
				}
				final Item item = getItem(fromRealSlot);
				if (item == null)
					return;
				removeItem(player, fromSlot, item.getAmount(), false, true);
				createTab();
				bankTabs[bankTabs.length - 1] = new Item[] { item };
				refreshTab(player, fromRealSlot[0]);
				refreshTab(player, toTab);
				refreshItems(player);
			} else if (bankTabs.length > toTab) {
				final int[] fromRealSlot = getRealSlot(fromSlot);
				if (fromRealSlot == null)
					return;
				if (toTab == fromRealSlot[0]) {
					switchItem(player, fromSlot, getStartSlot(toTab));
					return;
				}
				final Item item = getItem(fromRealSlot);
				if (item == null)
					return;
				final boolean removed = removeItem(player, fromSlot, item.getAmount(), false, true);
				if (!removed)
					refreshTab(player, fromRealSlot[0]);
				else if (fromRealSlot[0] != 0 && toTab >= fromRealSlot[0])
					toTab -= 1;
				refreshTab(player, fromRealSlot[0]);
				addItem(player, item.getId(), item.getAmount(), toTab, true);
			}
		} else {
			if (!player.getBank().isInsertItems())
				switchItem(player, fromSlot, toSlot);//
			else
				insert(player, fromSlot, toSlot);
		}
	}

	public void withdrawItem(Player player, int bankSlot, int quantity) {
		if (quantity < 1)
			return;
		Item item = getItem(getRealSlot(bankSlot));
		if (item == null)
			return;
		if (item.getAmount() < quantity)
			item = new Item(item.getId(), item.getAmount());
		else
			item = new Item(item.getId(), quantity);
		boolean noted = false;
		final ItemDefinitions defs = item.getDefinitions();
		if (player.getBank().getWithdrawNotes()) {
			if (!defs.isNoted() && defs.getCertId() != -1) {
				item.setId(defs.getCertId());
				noted = true;
			} else
				player.sm("You cannot withdraw this item as a note.");
		}
		if (noted || defs.isStackable()) {
			if (player.getInventory().getItems().containsOne(item)) {
				final int slot = player.getInventory().getItems().getThisItemSlot(item);
				final Item invItem = player.getInventory().getItems().get(slot);
				if (invItem.getAmount() + item.getAmount() <= 0) {
					item.setAmount(Integer.MAX_VALUE - invItem.getAmount());
					player.sm("Not enough space in your inventory.");
				}
			} else if (!player.getInventory().hasFreeSlots()) {
				player.sm("Not enough space in your inventory.");
				return;
			}
		} else {
			final int freeSlots = player.getInventory().getFreeSlots();
			if (freeSlots == 0) {
				player.sm("Not enough space in your inventory.");
				return;
			}
			if (freeSlots < item.getAmount()) {
				item.setAmount(freeSlots);
				player.sm("Not enough space in your inventory.");
			}
		}
		if (!hasRankToWithdraw(player, defs.getValue() * item.getAmount())) {
			return;
		}
		removeItem(player, bankSlot, item.getAmount(), true, false);
		// LogSystem.clanBankLog(player, true, item,
		// player.getClanManager().getClan().getClanName());

		addHistory(player, item, "withdrew");
		/*
		 * if (history == null) history = new PushList<String>(300); if
		 * (item.getDefinitions().getValue() * item.getAmount() > 50000)
		 * history.add(player.getUsername()+"," +item.getName()+","
		 * +"withdrew"+"," +item.getAmount()+","
		 * +(item.getDefinitions().getValue() * item.getAmount()));
		 */
		player.getInventory().addItemMoneyPouch(item);
	}

	public void depositAllMoneyPouch(Player player, boolean banking) {
		int coinsInPouch = player.getMoneyPouch().getTotal();
		int coinsToDeposit = coinsInPouch;
		int space = 0;
		Item coinsInBank = getItem(995);
		int coinsCanPutIn = coinsInBank == null ? Integer.MAX_VALUE : Integer.MAX_VALUE - coinsInBank.getAmount();
		if (coinsInBank != null) {
			if (coinsCanPutIn < coinsToDeposit) {
				coinsToDeposit = coinsCanPutIn;
				player.sm("Not enough space in your bank.");
			}
		}
		if (coinsToDeposit != 0) {
			space = addItems(player, new Item[] { new Item(995, coinsToDeposit) }, banking);
			if (space != 0) {
				if (space < 1) {
					player.sm("Not enough space in your bank.");
					return;
				}
				int value = coinsToDeposit;
				addHistory(player, new Item(995, value), "deposited");
				setDeposit(player.getUsername(), getDeposit(player.getUsername()) + value);
				// LogSystem.clanBankLog(player, false, value,
				// player.getClanManager().getClan().getClanName());

				player.sm(
						"You have deposited a total of " + Utils.moneyToString(getDeposit(player.getUsername())) + ".");
				player.getMoneyPouch().removeMoneyMisc(coinsToDeposit);
			}
		}
	}

	public int addItems(Player player, Item[] items, boolean refresh) {
		int space = (int) (MAX_BANK_SIZE - getBankSize());
		if (space != 0) {
			space = (space < items.length ? space : items.length);
			for (int i = 0; i < space; i++) {
				if (items[i] == null)
					continue;
				addItem(player, items[i], false);
			}
			if (refresh) {
				refreshTabs(player);
				refreshItems(player);
			}
		}
		return space;
	}

	public void withdrawItemButOne(Player player, int fakeSlot) {
		final int[] fromRealSlot = getRealSlot(fakeSlot);
		final Item item = getItem(fromRealSlot);
		if (item == null)
			return;
		if (item.getAmount() <= 1) {
			player.sm("You only have one of this item in your bank");
			return;
		}
		withdrawItem(player, fakeSlot, item.getAmount() - 1);
		// LogSystem.clanBankLog(player, true, item,
		// player.getClanManager().getClan().getClanName());
	}

	public void withdrawLastAmount(Player player, int bankSlot) {
		withdrawItem(player, bankSlot, player.getBank().getLastX());

	}

	public void setClanRankRestriction(int rank, int amount) {
		if (clanBankRestrictions == null) {
			clanBankRestrictions = new Hashtable<Integer, Integer>();
		}
		clanBankRestrictions.put(rank, amount);
	}

	/**
	 * @return maximum debt/overdraft the bank will allow can be changed in
	 *         ::cbsettings -1 if not set up at all -1 if not set up
	 */
	public int getDefaultDeposit(int rank) {
		if (clanBankRestrictions == null) {
			if (rank <= Clan.COORDINATOR)
				return 1000000;// default
			return -1;
		}
		Integer amount = clanBankRestrictions.get(rank);
		if (amount != null)
			return amount;
		if (rank <= Clan.COORDINATOR)
			return 1000000;// default
		return -1;
	}

	public void setDeposit(String username, long amount) {
		if (playerDeposits == null) {
			playerDeposits = new Hashtable<String, Long>();
		}
		playerDeposits.put(username, amount);
	}

	public long getDeposit(String username) {
		if (playerDeposits == null) {
			return 0;
		}
		Long amount = playerDeposits.get(username);
		if (amount != null)
			return amount;
		return 0;
	}

	public boolean hasRankToWithdraw(Player player, int value) {
		int rank = player.getClanManager().getClan().getMemberByName(player.getUsername()).getRank();
		int maxOverdraft = getDefaultDeposit(rank);
		long deposit = getDeposit(player.getUsername());
		if (maxOverdraft <= 0) {
			setDeposit(player.getUsername(), deposit - value);
			return true;
		}
		if (0 >= deposit + maxOverdraft) {
			player.sendMessage("You cannot withdraw any more items today.");
			return false;
		}
		long amountCanWithDraw = deposit + maxOverdraft;
		if (value > amountCanWithDraw) {
			player.sendMessage("You cannot withdraw any more items today.");
			return false;
		}
		player.sm("You can withdraw up to " + Utils.moneyToString(deposit + maxOverdraft) + " from the clan bank.");
		setDeposit(player.getUsername(), deposit - value);
		return true;
	}

	public void addHistory(Player player, Item item, String type) {
		if (history == null)
			history = new PushList<String>(300);
		if (item.getDefinitions().getValue() * item.getAmount() > 50000)
			history.add(player.getUsername() + "," + item.getName() + "," + type + "," + item.getAmount() + ","
					+ (item.getDefinitions().getValue() * item.getAmount()));
	}

	public void showWithdrawals(Player player) {
		if (history == null)
			return;
		String[] list = new String[history.size()];
		// int i = withdrawalList.size() - 1;
		int i = 0;
		for (int i2 = 0; i2 < history.getSize(); i2++) {
			String line = history.get(i2);
			String[] seperator = line.split(",");
			String username = seperator[0];
			String name = seperator[1];
			String type = seperator[2];
			int amount = Integer.parseInt(seperator[3]);
			int price = Integer.parseInt(seperator[4]);
			list[i++] = getColorForPrice(price) + username + " " + type + " " + amount + " "
					+ name.replace("(", "").replace(")", "") + ". " + Utils.moneyToString(price);
		}
		PacketDispatcher.sendScroll(player, "Recent Withdrawals", list);
	}

	public static String getColorForPrice(int price) {
		if (price > 1000000) {
			return "<col=CC0000><shad=660000>";
		} else if (price > 500000) {
			return "<col=FF6600><shad=B24700>";
		} else if (price > 250000) {
			return "<col=FFFF00><shad=474700>";
		}
		return "<col=000000>";
	}

	public boolean isSecond() {
		return second;
	}

	public void setSecond(boolean second) {
		this.second = second;
	}
}