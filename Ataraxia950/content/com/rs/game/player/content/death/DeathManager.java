package com.rs.game.player.content.death;

import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.activites.duel.DuelArena;
import com.rs.game.activites.soulwars.AreaController;
import com.rs.game.activites.soulwars.GameController;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.content.ItemConstants;
import com.rs.game.player.content.Magic;
import com.rs.game.player.content.grandExchange.GrandExchange;
import com.rs.game.player.content.hcim_news.HcimNewsManager;
import com.rs.game.player.controllers.Controller;
import com.rs.game.player.controllers.DungeonController;
import com.rs.game.player.controllers.pestcontrol.PestControlGame;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.network.packet.PacketRepository;
import com.rs.utils.Colors;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.items.ItemExaminesDataParser;
import com.rs.utils.mysql.QueryExecutor;
import com.rs.utils.mysql.impl.Hiscores;
import com.rs.utils.mysql.impl.ResetUserHiscores;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class DeathManager implements Serializable {

	private static final long serialVersionUID = -6097364802617536275L;
	private transient Player player;
	private int[] itemsKept;
	private final ArrayList<Item> itemsLost = new ArrayList<Item>(40), safeItems = new ArrayList<Item>(40);
	private boolean skulled, protectItem;
	private int keptSlots, cost;
	private WorldTile deathTile;
	private boolean safeDeath;
	
	private static final String[][] GRAVESTONES = new String[][] {
		{ "Memorial plaque", "1:45 minutes" }, { "Flag", "2 minutes" }, { "Small gravestone", "2 minutes" },
		{ "Ornate gravestone", "2:15 minutes" }, { "Font of life", "2:30 minutes" }, { "Stele", "2:30 minutes" },
		{ "Symbol of Saradomin", "2:30 minutes" }, { "Symbol of Zamorak", "2:30 minutes" }, { "Symbol of Guthix", "2:30 minutes" },
		{ "Symbol of Bandos", "2:30 minutes" }, { "Symbol of Armadyl", "2:30 minutes" }, { "Ancient symbol", "2:30 minutes" },
		{ "Angel of Death", "2:45 minutes" }, { "Royal dwarven gravestone", "3 minutes" }
	};
	
	public final void setPlayer(Player player) {
		this.player = player;
	}
	
	public final void setSafe(boolean value) {
		this.safeDeath = value;
	}
	
	public final void setDeathCoordinates(final WorldTile tile) {
		this.deathTile = tile;
	}
	
	public final void reset() {
		skulled = player.hasSkull();
		protectItem = player.getPrayer().isProtectingItem();
		keptSlots = !skulled && protectItem ? 4 : !skulled && !protectItem ? 3 : skulled && protectItem ? 1 : 0;
		deathTile = new WorldTile(player);
		safeDeath = true;
		itemsLost.clear();
		safeItems.clear();
		cost = 0;
		Item item = null;
		for (int i = 0; i < 2; i++) {
			for (Item e : (i == 0 ? player.getEquipment().getItems().getItems() : player.getInventory().getItems().getItems())) {
				if (e == null)
					continue;
				item = new Item(e);
				if (e.getDefinitions().getCSOpcode(1397) == 1) {
					safeItems.add(e);
					continue;
				}
				itemsLost.add(item);
			}
		}
		Collections.sort(itemsLost, new Comparator<Item>() {
			@Override
			public int compare(Item item1, Item item2) {
				return Integer.compare(item2.getDefinitions().getValue(), item1.getDefinitions().getValue());
			}
		});
		Collections.sort(safeItems, new Comparator<Item>() {
			@Override
			public int compare(Item item1, Item item2) {
				return Integer.compare(item2.getDefinitions().getValue(), item1.getDefinitions().getValue());
			}
		});
		for (int i = 0; i < 4; i++)
			player.getPackets().sendConfigByFile(9222 + i, -1);
		itemsKept = new int[keptSlots];
		for (int i = 0; i < keptSlots; i++) {
			if (i >= itemsLost.size())
				break;
			itemsKept[i] = itemsLost.get(i).getId();
		}
		Controller controler = player.getControlerManager().getControler();
		if (!(controler instanceof DungeonController || controler instanceof DuelArena || controler instanceof AreaController || controler instanceof GameController || controler instanceof PestControlGame))
			safeDeath = false;
		boolean hasItems = false;
		boolean[] lost = new boolean[4];
		loop : for (Item i : itemsLost) {
			if (i.getAmount() <= 0)
				continue;
			for (int x = 0; x < itemsKept.length; x++) {
				if (i.getId() == itemsKept[x] && !lost[x]) {
					lost[x] = true;
					continue loop;
				}
			}
			hasItems = true;
			cost += (long) (getReclaimValue(i) * i.getAmount());
		}
		if (!hasItems)
			safeDeath = true;
	}
	
	/**
	 * All dangerous deaths in which the player loses items will be handled through this given method.
	 */
	public void handleDeathInPvP(Player killer) {
		if (player.isHCIronMan()) {
	        QueryExecutor.submit(new ResetUserHiscores(player.getUsername(), player.getTable()));
			if (player.getSkills().getTotalLevel(player) >= 500)
				World.sendWorldMessage(Colors.RED + "<shad=000000><img=18>News: " + player.getDisplayName()
						+ " just died in Hardcore Ironman mode with a skill total of " + player.getSkills().getTotalLevel(player)
						+ "!", false);
			player.setHCIronMan(false);
			player.setIronMan(true);
	        QueryExecutor.submit(new Hiscores(player, player.getTable()));
			player.sendMessage("Your account has been changed to a regular ironman as a result of a death in wilderness.");
			HcimNewsManager.getInstance().addDeath(player);
			/*player.setPermBanned(true);
			SerializableFilesManager.savePlayer(player);
			if (Settings.BETA_NETWORKING)
				player.getRealChannel().close();
			else
				player.getSession().getChannel().close();*/
			return;
		}
		if (killer == null)
			killer = player;
		if (!player.isCanPvp())
			return;
		//Random as fuck checks here because why the fuck not, someone made it and I don't wanna stir up shit.
		if (player.getUsername().equalsIgnoreCase("youtube") || killer.getUsername().equalsIgnoreCase("youtube"))
			return;
		if (player.getUsername().equalsIgnoreCase("xanax") || killer.getUsername().equalsIgnoreCase("xanax"))
			return;
		if (player.getUsername().equalsIgnoreCase("community") || killer.getUsername().equalsIgnoreCase("community"))
			return;
		if (killer.isOwner()) {
			player.sendMessage("You kept all your items on death from dying to an administrator.");
			World.addGroundItem(new Item(526, 1), new WorldTile(player), 60);
			return;
		}
		if (player.isOwner()) {
			World.addGroundItem(new Item(526, 1), new WorldTile(player), 60);
			return;
		}
		deathTile = new WorldTile(player);
//		player.getCharges().die();
		player.getAuraManager().removeAura();
		ArrayList<Item> itemsLost = new ArrayList<Item>(40), safeItems = new ArrayList<Item>(40);
		skulled = player.hasSkull();
		protectItem = player.getPrayer().isProtectingItem();
		keptSlots = !skulled && protectItem ? 4 : !skulled && !protectItem ? 3 : skulled && protectItem ? 1 : 0;
		Item item = null;
		for (int i = 0; i < 2; i++) {
			for (Item e : (i == 0 ? player.getEquipment().getItems().getItems() : player.getInventory().getItems().getItems())) {
				if (e == null)
					continue;
				item = new Item(e).setAttributes(e.getAttributes());
				if (item.getAmount() > 0)
					itemsLost.add(item);
			}
		}
		Collections.sort(itemsLost, new Comparator<Item>() {
			@Override
			public int compare(Item item1, Item item2) {
				return Integer.compare(item2.getDefinitions().getValue(), item1.getDefinitions().getValue());
			}
		});
		Collections.sort(safeItems, new Comparator<Item>() {
			@Override
			public int compare(Item item1, Item item2) {
				return Integer.compare(item2.getDefinitions().getValue(), item1.getDefinitions().getValue());
			}
		});
		int[] itemsKept = new int[keptSlots];
		for (int i = 0; i < keptSlots; i++) {
			if (i >= itemsLost.size())
				break;
			itemsKept[i] = itemsLost.get(i).getId();
		}
		boolean[] kept = new boolean[4];
		loop : for (int i = 0; i < itemsLost.size(); i++) {
			for (int x = 0; x < itemsKept.length; x++) {
				if (itemsLost.get(i).getId() == itemsKept[x] && !kept[x]) {
					itemsLost.get(i).setAmount(itemsLost.get(i).getAmount() - 1);
					safeItems.add(new Item(itemsLost.get(i).getId(), 1, itemsLost.get(i).getCharges()).setAttributes(itemsLost.get(i).getAttributes()));
					kept[x] = true;
					continue loop;
				}	
			}
		}
		player.getInventory().reset();
		player.getEquipment().reset();
		player.getAppearence().generateAppearenceData();
		for (Item safe : safeItems) {
			if (safe.getAmount() <= 0)
				continue;
			player.getInventory().addItem(safe);
		}
		int coins = 0;
		for (Item lost : itemsLost) {
			if (lost == null)
				continue;
			if (lost.getAmount() <= 0)
				continue;
			if (ItemConstants.isKeptOnDeath(lost))
				continue;
			else if (!ItemConstants.isTradeable(lost) && !player.equals(killer))
				coins += lost.getDefinitions().getValue();
//			else if (ItemConstants.getItemComposition(lost) != null && !player.equals(killer)) {
//				lost.setId(ItemConstants.getItemComposition(lost)[0]);
//				if (ItemConstants.getItemComposition(lost) != null && ItemConstants.getItemComposition(lost).length > 1)
//					itemsLost.add(new Item(ItemConstants.getItemComposition(lost)[1], lost.getAmount()));
//			}
		}
		//Needs to be done separately to avoid issues.
		if (coins > 0)
			World.addGroundItem(new Item(995, coins), deathTile, killer, true, 180);
		for (Item lost : itemsLost) {
			if (lost.getAmount() <= 0)
				continue;
			if (ItemConstants.isKeptOnDeath(lost)) {
				player.getInventory().addItem(lost);
				continue;
			}
			if (!ItemConstants.isTradeable(lost) && !player.equals(killer))
				continue;
			if(!killer.isIronMan() || 
	        		!killer.isNoviceIronMan() || !killer.isExpertIronMan() || !killer.isIntermediateIronMan() || killer.isIronMan() && !player.isIronMan())
				World.addGroundItem(lost, deathTile, lost.getAttributes() != null ? player : killer, true, 180);
		}
		World.addGroundItem(new Item(526, 1), deathTile, 60);
		itemsKept = null;
		itemsLost.clear();
		safeItems.clear();
		skulled = false;
		protectItem = false;
		keptSlots = 3;
		cost = 0;
		deathTile = null;
		safeDeath = false;
	}
	
	public final boolean wasSafeDeath() {
		return safeDeath;
	}
	
	public final void handleDeath() {
		boolean[] lost = new boolean[4];
		for (Item i : itemsLost) {
			loop : for (int x = 0; x < itemsKept.length; x++) {
				if (i.getId() == itemsKept[x] && !lost[x]) {
					i.setAmount(i.getAmount() - 1);
					lost[x] = true;
					break loop;
				}
			}
		}
		new Gravestone(player, deathTile, itemsLost.toArray(new Item[itemsLost.size()]));
		World.addGroundItem(new Item(526, 1), deathTile, 60);
		player.getInventory().reset();
		player.getEquipment().reset();
		player.getAppearence().generateIconsData();
		player.getAppearence().generateAppearenceData();
		for (int i = 0; i < itemsKept.length; i++) {
			if (itemsKept[i] <= 0)
				continue;
			player.getInventory().addItem(new Item(itemsKept[i], 1));
		}
		for (Item i : safeItems)
			player.getInventory().addItem(i);
		safeItems.clear();
		itemsLost.clear();
		skulled = false;
		protectItem = false;
		keptSlots = 3;
		cost = 0;
		safeDeath = false;
		itemsKept = null;
	}
	
	public final void finish() {
		safeItems.clear();
		itemsLost.clear();
		skulled = false;
		protectItem = false;
		keptSlots = 3;
		cost = 0;
		safeDeath = false;
		itemsKept = null;
	}
	
	public final int getCostOfItemsLost() {
		return cost;
	}
	
	public final void sendInterface() {
		player.getInterfaceManager().sendInterface(18);
		player.getPackets().sendConfigByFile(9227, keptSlots);
		if (itemsKept != null)
			for (int i = 0; i < itemsKept.length; i++)
				player.getPackets().sendConfigByFile(9222 + i, getSlot(player, itemsKept[i]));
		player.getInterfaceManager().sendInterface(18);
		player.getPackets().sendUnlockIComponentOptionSlots(18, 17, 0, 55, 0, 9);
		player.getPackets().sendUnlockIComponentOptionSlots(18, 25, 0, 55, 9);
		player.getPackets().sendUnlockIComponentOptionSlots(18, 9, 0, 4, 0, 9);
	}
	
	public final boolean handleInterface(int interfaceId, int componentId, int packetId, int slotId, int slotId2) {
		if (itemsKept == null)
			return false;
		if (interfaceId == 18)  {
			if (componentId == 17) {
				if (packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
					boolean full = true;
					for (int i = 0; i < keptSlots; i++) {
						if (itemsKept[i] == 0) {
							full = false;
							player.getPackets().sendConfigByFile(9222 + i, getSlot(player, slotId2));
							itemsKept[i] = slotId2;
							break;
						}
					}
					if (full) {
						player.sendMessage("You are unable to protect any more items, unprotect something if you wish to do so.");
						return true;
					}
				} else {
					player.sendMessage(ItemExaminesDataParser.getExamine(new Item(slotId2)));
					player.sendMessage("Grand Exchange guide price: " + Utils.getFormattedNumber(GrandExchange.getPrice(new Item(slotId2))) + ".");
				}
			} else if (componentId == 9) {
				if (packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
					itemsKept[slotId] = 0;
					player.getPackets().sendConfigByFile(9222 + slotId, -1);
				}
				else
					player.sendMessage(ItemExaminesDataParser.getExamine(new Item(slotId2)));
			} else if (componentId == 25) {
				player.sendMessage(ItemExaminesDataParser.getExamine(new Item(slotId2)));
			} else if (componentId == 36) {
				Magic.sendObjectTeleportSpell(player, true, player.getHomeTile());
				WorldTasksManager.schedule(new WorldTask() {
					@Override
					public void run() {
						handleDeath();
					}
				}, 2);
			}
			return true;
		}
		return false;
	}
	
	private static final int getSlot(Player player, int itemId) {
		if (player.getEquipment().containsOneItem(itemId))
			return player.getEquipment().getItems().getThisItemSlot(itemId) + 1;
		else
			return player.getInventory().getItems().getThisItemSlot(itemId) + 16;
	}

	public final static int getReclaimValue(Item item) {
		int price = GrandExchange.getPrice(item.getId());
		if (price >= 10000000)
			return (int) (70500 + price * 0.005D);
		else if (price >= 1000000)
			return (int) (20500 + price * 0.01D);
		else if (price >= 250000)
			return (int) (10500 + price * 0.02D);
		else if (price >= 50000)
			return (int) (3000 + price * 0.05D);
		else if (price >= 10000)
			return (int) (1000 + price * 0.1D);
		return (int) Math.max(price * 0.15D, 1);
	}

	public static int getSacrificeValue(Item item) {
		return 4 * getReclaimValue(item);
	}
	
	public static final void sendItemsKeptOnDeathInterface(Player player, boolean toggle, boolean inWilderness) {
		if (player.getInterfaceManager().containsScreenInter() || player.getInterfaceManager().containsInventoryInter()) {
			player.sendMessage("Please finish what you're doing before opening items kept upon death.");
			return;
		}
		player.getInterfaceManager().sendInterface(17);
		boolean safeZone = false;
		ArrayList<Item> itemsLost = new ArrayList<Item>(40), safeItems = new ArrayList<Item>(40);
		final boolean skulled = player.hasSkull();
		final boolean protectItem = player.getPrayer().isProtectingItem();
		Controller controler = player.getControlerManager().getControler();
		if (controler instanceof DungeonController || controler instanceof DuelArena || controler instanceof AreaController || controler instanceof GameController || controler instanceof PestControlGame)
			safeZone = true;
		final int keptSlots = !skulled && protectItem ? 4 : !skulled && !protectItem ? 3 : skulled && protectItem ? 1 : 0;
		Item item = null;
		for (int i = 0; i < 2; i++) {
			for (Item e : (i == 0 ? player.getEquipment().getItems().getItems() : player.getInventory().getItems().getItems())) {
				if (e == null)
					continue;
				item = new Item(e);
				if (e.getDefinitions().getCSOpcode(1397) == 1) {
					safeItems.add(e);
					continue;
				}
				if (item.getAmount() > 0)
					itemsLost.add(item);
			}
		}
		Collections.sort(itemsLost, new Comparator<Item>() {
			@Override
			public int compare(Item item1, Item item2) {
				return Integer.compare(item2.getDefinitions().getValue(), item1.getDefinitions().getValue());
			}
		});
		for (int i = 0; i < 4; i++)
			player.getPackets().sendConfigByFile(9222 + i, -1);
		int[] itemsKept = new int[keptSlots];
		for (int i = 0; i < keptSlots; i++) {
			if (i >= itemsLost.size())
				break;
			itemsKept[i] = itemsLost.get(i).getId();
		}
		boolean[] kept = new boolean[4];
		for (int i = 0; i < itemsLost.size(); i++) {
			for (int x = 0; x < itemsKept.length; x++) {
				if (itemsLost.get(i).getId() == itemsKept[x] && !kept[x]) {
					itemsLost.get(i).setAmount(itemsLost.get(i).getAmount() - 1);
					safeItems.add(new Item(itemsLost.get(i).getId(), 1, itemsLost.get(i).getCharges()));
					kept[x] = true;
				}	
			}
		}
		player.getPackets().sendUnlockIComponentOptionSlots(17, 18, 0, 11, 9);
		player.getPackets().sendUnlockIComponentOptionSlots(17, 17, 0, 50, 9);
		player.getPackets().sendUnlockIComponentOptionSlots(17, 20, 0, 50, 9);
		if (player.getFamiliar() != null && player.getFamiliar().getBob() != null)
			player.getPackets().sendItems(530, player.getFamiliar().getBob().getBeastItems());
		long carriedWealth = 0, riskedWealth = 0, saveCost = 0;
		for (Item i : safeItems)
			carriedWealth += GrandExchange.getPrice(i.getId()) * i.getAmount();
		for (Item i : itemsLost) {
			if (i.getAmount() <= 0)
				continue;
			carriedWealth += (long) GrandExchange.getPrice(i.getId()) * i.getAmount();
			riskedWealth += (long) GrandExchange.getPrice(i.getId()) * i.getAmount();
			saveCost += getReclaimValue(i) * i.getAmount();
		}
		StringBuilder builder = new StringBuilder();
		builder.append("<br><br>The normal amount of items kept is three.<br><br>");
		builder.append(keptSlots == 4 || keptSlots == 1 ? "You currently have <col=ff0000>Protect Item</col> prayer on, allowing you to keep one extra item." : keptSlots == 3 ? "You have no factors affecting the items you keep." : "You are skulled, therefore will not be able to protect any items.");
		builder.append("<br><br><br>Carried wealth:<br>" + Utils.formatNumber(carriedWealth));
		builder.append("<br><br>Risked wealth:<br>" + Utils.formatNumber(riskedWealth));
		builder.append("<br><br>Total cost to save:<br>" + Utils.formatNumber(saveCost));
		builder.append("<br><br>Current gravestone:<br>" + GRAVESTONES[player.getGravestone()][0] + "<br><br>Duration:<br>" + GRAVESTONES[player.getGravestone()][1]);
		player.getPackets().sendGlobalString(352, builder.toString());
		if (toggle)
			player.getTemporaryAttributtes().put("wildToggle", true);
		player.getPackets().sendConfigByFile(9226, safeZone ? 2 : toggle ? 1 : 0);
		player.getPackets().sendGlobalString(351, safeZone ? "You will not lose any items upon death within this area." : "");
		player.getPackets().sendConfigByFile(9229, inWilderness ? 1 : 0);
		player.getPackets().sendConfigByFile(9227, keptSlots);
		for (int i = 0; i < 4; i++)
			player.getPackets().sendConfigByFile(9222 + i, -1);
		if (itemsKept != null)
			for (int i = 0; i < itemsKept.length; i++)
				player.getPackets().sendConfigByFile(9222 + i, getSlot(player, itemsKept[i]));
		player.getPackets().sendRunScriptBlank(4592);
	}
}
