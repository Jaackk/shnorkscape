package com.rs.game.player.content.grandExchange;
import com.rs.utils.InputIntegerEvent;

import java.io.Serializable;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.player.LendingManager;
import com.rs.game.player.Player;
import com.rs.game.player.content.InterfaceManager;
import com.rs.game.player.content.ItemConstants;
import com.rs.game.player.content.shops.ShopViewer;
import com.rs.network.packet.PacketRepository;
import com.rs.utils.Colors;
import com.rs.utils.InputIntegerComponentEvent;
import com.rs.utils.Lend;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.items.ItemExaminesDataParser;

/**
 * Handles the Grand Exchange.
 *
 * @author Noel
 */
public class GrandExchangeManager implements Serializable {

	private static final long serialVersionUID = -866326987352331696L;

	private transient Player player;

	private long[] offerUIds;
	private OfferHistory[] history;

	public GrandExchangeManager() {
		offerUIds = new long[8];
		history = new OfferHistory[10];
	}

	public void setPlayer(Player player) {
	   if (history.length < 10) {
	       OfferHistory[] historyN = new OfferHistory[10];
	        for (int i = 0; i < history.length; i++)
	                historyN[i] = history[i];
	            history = historyN;
	    }
        if (offerUIds.length < 8) {
               long[] offerUIDsN = new long[8];
               for (int i = 0; i < offerUIds.length; i++)
                   offerUIDsN[i] = offerUIds[i];
               offerUIds = offerUIDsN;
        }
		this.player = player;
	}

	public void init() {
		GrandExchange.linkOffers(player);
	}

	public void stop() {
		GrandExchange.unlinkOffers(player);
	}

	public long[] getOfferUIds() {
		return offerUIds;
	}

	public boolean isSlotFree(int slot) {
		return offerUIds[slot] == 0;
	}

	public void addOfferHistory(OfferHistory o) {
		OfferHistory[] dest = new OfferHistory[history.length];
		dest[0] = o;
		System.arraycopy(history, 0, dest, 1, history.length - 1);
		history = dest;
	}
	
    public void openHistory() { 
        openHistory(false);
    }
    
	public void openHistory(boolean fromMenu) {
		if (!canUse()) {
			player.sendMessage("Ironmen cannot use the Grand Exchange.");
//			player.getPackets().sendSound(4039, 1, 1);
			return;
		}
        if (!fromMenu)
            player.getInterfaceManager().openMenu(5, 2);
        player.getInterfaceManager().sendLockGameTab(InterfaceManager.INVENTORY_TAB, false, false);
        for (int i = 9; i >= 0; i--) {
            OfferHistory o = history[i];
            player.getVarsManager().sendVar(i <= 4 ? (145 + i) : (5712 + (i - 5)), o == null ? -1 : o.getId());
            player.getVarsManager().sendVarBit(i <= 4 ? (439 + i) : (28363 + (i - 5)),
                    o == null ? -1 : o.isBought() ? 0 : 1);
            player.getVarsManager().sendVar(i <= 4 ? (155 + i) : (5722 + (i - 5)), o == null ? -1 : o.getQuantity());
            player.getVarsManager().sendVar(i <= 4 ? (150 + i) : (5717 + (i - 5)), o == null ? -1 : o.getPrice());
        }
	}
	
    public void openGrandExchange() { 
        openGrandExchange(false);
    }
    
	public void openGrandExchange(boolean fromMenu) {
		if (!canUse()) {
			player.sendMessage("Ironmen cannot use the Grand Exchange.");
//			player.getPackets().sendSound(4039, 1, 1);
			return;
		}
	    if (!fromMenu)
	       player.getInterfaceManager().openMenu(5, 1);
        player.getPackets().sendUnlockIComponentOptionSlots(105, 282, -1, 0, 0, 1);
        player.getPackets().sendUnlockIComponentOptionSlots(105, 284, -1, 0, 0, 1);
        player.getInterfaceManager().sendLockGameTab(InterfaceManager.INVENTORY_TAB, true, false);
        player.getPackets().sendUnlockIComponentOptionSlots(107, 7, 0, 27, 0, 1);
        player.getMoneyPouch().refresh();
        cancelOffer();
        player.setCloseInterfacesEvent(new Runnable() {
            @Override
            public void run() {
                removeGEItemSearch();
                player.getInterfaceManager().sendLockGameTab(InterfaceManager.INVENTORY_TAB, false, false);
                player.getInterfaceManager().closeMenu();
            }
        });
	}

	public void openCollectionBox() {
		if (!canUse()) {
			player.sendMessage("Ironmen cannot use the Grand Exchange.");
//			player.getPackets().sendSound(4039, 1, 1);
			return;
		}
		Lend hasLendedOut = LendingManager.getHasLendedItemsOut(player);
		Item item = null;
		if (player.getBank().getCollectableItem() != 0) {
			for (int i = 540; i < 541; i++) {
				for (int j = player.getBank().getCollectableItem(); j < player.getBank().getCollectableItem() + 1; j++) {
					Item[] default_ = new Item[] { new Item(player.getBank().getCollectableItem(), 1) };
					player.getPackets().sendItems(i, default_);
				}
			}
			if (LendingManager.getHasLendedItemsOut(player) == null) {
				player.getPackets().sendIComponentText(109, 20, "<col=00ff00>Available");
			}
		} else if (hasLendedOut != null) {
			for (Lend lend : LendingManager.list) {
				if (lend.getLender().equals(player.getUsername()))
					item = lend.getItem();
			}
			int id2 = item.getId();
			for (int i = 540; i < 541; i++) {
				for (int j = id2; j < id2 + 1; j++) {
					Item[] default_ = new Item[] { new Item(id2, 1) };
					player.getPackets().sendItems(i, default_);
				}
			}
			player.getPackets().sendIComponentText(109, 20, "<col=FF0000>Still on loan");
		} else {
			player.getPackets().sendHideIComponent(109, 17, true);
			for (int i = 540; i < 541; i++) {
				for (int j = -1; j < 0; j++) {
					Item[] default_ = new Item[] { new Item(-1, 1) };
					player.getPackets().sendItems(i, default_);
				}
			}
			player.getPackets().sendIComponentText(109, 20, "<col=FF0000>Nothing!");
		}
        player.getInterfaceManager().sendInterface(109);
        player.getPackets().sendUnlockIComponentOptionSlots(109, 14, 0, 2, 0, 1);
        player.getPackets().sendUnlockIComponentOptionSlots(109, 12, 0, 2, 0, 1);
        player.getPackets().sendUnlockIComponentOptionSlots(109, 10, 0, 2, 0, 1);
        player.getPackets().sendUnlockIComponentOptionSlots(109, 7, 0, 2, 0, 1);
        player.getPackets().sendUnlockIComponentOptionSlots(109, 4, 0, 2, 0, 1);
        player.getPackets().sendUnlockIComponentOptionSlots(109, 1, 0, 2, 0, 1);
        player.getPackets().sendUnlockIComponentOptionSlots(109, 62, 0, 2, 0, 1);
        player.getPackets().sendUnlockIComponentOptionSlots(109, 67, 0, 2, 0, 1);
	}

	public void setSlot(int slot) {
		player.getVarBitManager().sendVar(138, slot);
	}
	
	public int getMarketPrice() {
	    return player.getVarBitManager().getValue(140);
	}
	
	public void setMarketPrice(int price) {
		player.getVarBitManager().sendVar(140, price);
		//player.getPackets().sendSound(4041, 1, 1);
	}

	public int getPricePerItem() {
		return player.getVarBitManager().getValue(137);
	}

	public void setPricePerItem(int price) {
		player.getVarBitManager().sendVar(137, price);
		//player.getPackets().sendSound(4041, 1, 1);
	}

	public int getCurrentSlot() {
		return player.getVarBitManager().getValue(138);
	}

	public int getItemId() {
		return player.getVarBitManager().getValue(135);
	}

	public void setItemId(int id) {
		//player.getPackets().sendSound(4041, 1, 1);
		player.getVarBitManager().sendVar(135, id);
	}

	public int getAmount() {
		return player.getVarBitManager().getValue(136);
	}

	public void setAmount(int amount) {
		//player.getPackets().sendSound(4041, 1, 1);
		player.getVarBitManager().sendVar(136, amount);
	}

	public int getType() {
		return player.getVarBitManager().getValue(139);
	}

	public void setType(int amount) {
		player.getVarBitManager().sendVar(139, amount);
	}

	private void promptPriceEdit(final int itemId) {
		final String itemName = ItemDefinitions.getItemDefinitions(itemId).getName();
		final int currentPrice = GrandExchange.getPrice(itemId);

		player.sendInputInteger(
				"Enter new GE price for " + itemName + " (Current: " + Utils.formatNumber(currentPrice) + "):",
				new InputIntegerEvent() {
					@Override
					public void run(Player player) {
						int value = getInteger();
						if (value < 1) {
							player.sendMessage("Invalid price.");
							return;
						}
						if (GrandExchange.usesCachePrices()) {
							player.sendMessage(Colors.RED + "Grand Exchange prices now come from the item cache. Edit the item value in your cache editor instead.");
							return;
						}
						GrandExchange.setPrice(itemId, value);
						GrandExchange.savePrices();
						player.sendMessage(Colors.GREEN + "Set price of " + itemName + " to " +
								Utils.formatNumber(value) + " gp.");
					}
				}
		);
	}



	public void handleButtons(int interfaceId, int componentId, int slotId, int packetId) {


		//System.out.println("DEBUG: interfaceId=" + interfaceId + ", componentId=" + componentId + ", slotId=" + slotId + ", packetId=" + packetId);


		// ------------------------------
		// ADMIN PRICE EDIT MODE HOOKS
		// ------------------------------
		if (player.isGePriceEditMode()) {
			if (interfaceId == 105) {
				switch (componentId) {
					case 31: case 37: case 46: case 52:
					case 61: case 67: case 76: case 82:
					case 94: case 100: case 112: case 118:
					case 130: case 136: case 148: case 154: {

						// Ensure GE is in a valid "Buy" state before opening search
						int free = getFreeSlot();
						if (free == -1) {
							player.sendMessage("All Grand Exchange slots are in use.");
							return;
						}

						// Reset any prior offer context
						cancelOffer();

						// Set up a buy offer context so the search results will be accepted
						setType(0);            // 0 = BUY
						setSlot(free);         // pick a free slot
						setItemId(-1);
						setAmount(0);
						setPricePerItem(1);
						setMarketPrice(0);

						// Now open search; client will route search clicks properly
						player.getGEManager().sendGEItemSearch();
						return;
					}
				}
			}
		}


		// ------------------------------
		// NORMAL GE & OTHER INTERFACE HANDLING
		// ------------------------------
		if (interfaceId == 105) {
			switch (componentId) {
				case 171:
					back();
					break;
				case 24:
					if (packetId == PacketRepository.ACTION_BUTTON1_PACKET)
						viewOffer(0);
					else
						abortOffer(0);
					break;
				case 39:
					if (packetId == PacketRepository.ACTION_BUTTON1_PACKET)
						viewOffer(1);
					else
						abortOffer(1);
					break;
				case 54:
					if (packetId == PacketRepository.ACTION_BUTTON1_PACKET)
						viewOffer(2);
					else
						abortOffer(2);
					break;
				case 69:
					if (packetId == PacketRepository.ACTION_BUTTON1_PACKET)
						viewOffer(3);
					else
						abortOffer(3);
					break;
				case 87:
					if (packetId == PacketRepository.ACTION_BUTTON1_PACKET)
						viewOffer(4);
					else
						abortOffer(4);
					break;
				case 105:
					if (packetId == PacketRepository.ACTION_BUTTON1_PACKET)
						viewOffer(5);
					else
						abortOffer(5);
					break;
				case 123:
					if (packetId == PacketRepository.ACTION_BUTTON1_PACKET)
						viewOffer(6);
					else
						abortOffer(6);
					break;
				case 141: // converted
					if (packetId == PacketRepository.ACTION_BUTTON1_PACKET)
						viewOffer(7);
					else
						abortOffer(7);
					break;
				case 279:
					abortCurrentOffer();
					break;
				case 31:
					makeOffer(0, false);
					break;
				case 37:
					makeOffer(0, true);
					break;
				case 46:
					makeOffer(1, false);
					break;
				case 52:
					makeOffer(1, true);
					break;
				case 61:
					makeOffer(2, false);
					break;
				case 67:
					makeOffer(2, true);
					break;
				case 76:
					makeOffer(3, false);
					break;
				case 82:
					makeOffer(3, true);
					break;
				case 94:
					makeOffer(4, false);
					break;
				case 100:
					makeOffer(4, true);
					break;
				case 112:
					makeOffer(5, false);
					break;
				case 118:
					makeOffer(5, true);
					break;
				case 130:
					makeOffer(6, false);
					break;
				case 136:
					makeOffer(6, true);
					break;
				case 148:
					makeOffer(7, false);
					break;
				case 154:
					makeOffer(7, true);
					break;
				case 5:
					cancelOffer();
					break;
				case 207:
					modifyAmount(getAmount() - 1);
					break;
				case 206:
					modifyAmount(getAmount() + 1);
					break;
				case 212:
					modifyAmount(getAmount() + 1);
					break;
				case 219:
					modifyAmount(getAmount() + 10);
					break;
				case 226:
					modifyAmount(getAmount() + 100);
					break;
				case 233:
					modifyAmount(getType() == 0 ? getAmount() + 1000 : getItemAmount(new Item(getItemId())));
					break;
				case 203:
					editAmount();
					break;
				case 247:
					modifyPricePerItem(getPricePerItem() - 1);
					break;
				case 246:
					modifyPricePerItem(getPricePerItem() + 1);
					break;
				case 259:
					modifyPricePerItem(GrandExchange.getPrice(getItemId()));
					break;
				case 243:
					editPrice();
					break;
				case 265:
					modifyPricePerItem((int) (Math.ceil(getPricePerItem() * 1.05)));
					break;
				case 252:
					modifyPricePerItem((int) (getPricePerItem() * 0.95));
					break;
				case 302:
					confirmOffer();
					break;
				case 14:
					chooseItem();
					break;
				case 280:
					collectItems(getCurrentSlot(), 0, 0);
					collectItems(getCurrentSlot(), 1, 0);
					break;
				case 282:
					collectItems(getCurrentSlot(), 0, packetId == PacketRepository.ACTION_BUTTON1_PACKET ? 0 : 1);
					break;
				case 284:
					collectItems(getCurrentSlot(), 1, packetId == PacketRepository.ACTION_BUTTON1_PACKET ? 0 : 1);
					break;
			}
		}
		else if (interfaceId == 107 && componentId == 7) {
			if (packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
				offer(slotId);
			} else if (player.getInventory().getItem(slotId) != null)
				player.getPackets().sendInterfaceMessage(107, 7, 0, slotId,
						ItemExaminesDataParser.getExamine(player.getInventory().getItem(slotId)));
		}
		else if (interfaceId == 389 && componentId == 8) {
			removeGEItemSearch();
		}
		else if (interfaceId == 109) {
			switch (componentId) {
				case 14:
					collectItems(0, slotId == 0 ? 0 : 1,
							packetId == PacketRepository.ACTION_BUTTON1_PACKET ? 0 : 1);
					break;
				case 12:
					collectItems(1, slotId == 0 ? 0 : 1,
							packetId == PacketRepository.ACTION_BUTTON1_PACKET ? 0 : 1);
					break;
				case 10:
					collectItems(2, slotId == 0 ? 0 : 1,
							packetId == PacketRepository.ACTION_BUTTON1_PACKET ? 0 : 1);
					break;
				case 7:
					collectItems(3, slotId == 0 ? 0 : 1,
							packetId == PacketRepository.ACTION_BUTTON1_PACKET ? 0 : 1);
					break;
				case 4:
					collectItems(4, slotId == 0 ? 0 : 1,
							packetId == PacketRepository.ACTION_BUTTON1_PACKET ? 0 : 1);
					break;
				case 1:
					collectItems(5, slotId == 0 ? 0 : 1,
							packetId == PacketRepository.ACTION_BUTTON1_PACKET ? 0 : 1);
					break;
				case 62:
					collectItems(6, slotId == 0 ? 0 : 1,
							packetId == PacketRepository.ACTION_BUTTON1_PACKET ? 0 : 1);
					break;
				case 67:
					collectItems(7, slotId == 0 ? 0 : 1,
							packetId == PacketRepository.ACTION_BUTTON1_PACKET ? 0 : 1);
					break;
				case 47:
				case 55: // added
					collectAll(componentId == 47);
					break;
			}
		}
		else if (interfaceId == 651) {
			if (componentId == 6 || componentId == 14)
				collectAll(componentId == 14);
		}
	}


	public void cancelOffer() {
		setItemId(-1);
		setAmount(0);
		setPricePerItem(1);
		setMarketPrice(0);
		setSlot(-1);
        removeGEItemSearch();
		setType(-1);
	}

	public void editAmount() {
		if (getType() == -1)
			return;
		player.sendIComponentInputInteger(105, 200, 10, new InputIntegerComponentEvent() {
            @Override
            public void run(Player player) {
                final int value = getInteger();
                if (value < 0)
                    return;
                player.getGEManager().modifyAmount(value);
            }
        });
	}

	public void editPrice() {
		if (getType() == -1)
			return;
		if (getItemId() == -1) {
			player.sendMessage("You must choose an item first.");
			//player.getPackets().sendSound(4039, 1, 1);
			return;
		}
        player.sendIComponentInputInteger(105, 240, 10, new InputIntegerComponentEvent() {
			@Override
			public void run(Player player) {
				final int value = getInteger();
				if (value < 0)
					return;
				player.getGEManager().modifyPricePerItem(value);
			}
		});
	}

	public void modifyPricePerItem(int value) {
		if (getType() == -1)
			return;
		if (getItemId() == -1) {
			player.sendMessage("You must choose an item first.");
//			player.getPackets().sendSound(4039, 1, 1);
			return;
		}
		if (value < 1)
			value = 1;
		setPricePerItem(value);
	}

	public void modifyAmount(int value) {
		if (getType() == -1)
			return;
		if (value < 0)
			value = 0;
		setAmount(value);
	}

	public void abortCurrentOffer() {
		int slot = getCurrentSlot();
		if (slot == -1)
			return;
		abortOffer(slot);
	}

	public void abortOffer(int slot) {
		if (isSlotFree(slot))
			return;
		GrandExchange.abortOffer(player, slot);
		player.sendMessage("Please be aware that your offer may have already been completed.");
	}

	public void collectItems(int slot, int invSlot, int option) {
		if (slot == -1 || isSlotFree(slot))
			return;
		GrandExchange.collectItems(player, slot, invSlot, option);
	}

	public void viewOffer(int slot) {
		if (isSlotFree(slot) || getCurrentSlot() != -1)
			return;
		Offer offer = GrandExchange.getOffer(player, slot);
		if (offer == null)
			return;
		setSlot(slot);
		setExtraDetails(offer.getId());
	}

	/*
	 * includes noted
	 */
	public int getItemAmount(Item item) {
		int notedId = item.getDefinitions().certId;
		return player.getInventory().getNumberOf(item.getId()) + player.getInventory().getNumberOf(notedId);
	}

	public void chooseItem(int id) {
		if (!player.getInterfaceManager().containsInterface(105))
			return;

		if (player.isGePriceEditMode()) {
			promptPriceEdit(id);
			return;
		}

		setItem(new Item(id), false);
	}


	public void chooseItem() {
		if (getType() != 0)
			return;
        sendGEItemSearch();
	}

	public void offer(int slot) {
        int freeSlot = getFreeSlot();
        if (freeSlot == -1 && getCurrentSlot() == -1)
            return;
        Item item = player.getInventory().getItem(slot);
        if (item == null)
            return;
        if (item.getId() == ShopViewer.COINS || !ItemConstants.isTradeable(item) || item.getName().contains("charm") || item.getId() == 11640 || item.getId() == 37753) {// to add items here when u dont want them selling on ge
            player.sendMessage("This item cannot be traded on the Grand Exchange.");
//            player.getPackets().sendSound(4039, 1, 1);
            return;
        }
        setSlot(getCurrentSlot() != -1 ? getCurrentSlot() : freeSlot);
        setType(getType() == -1 ? 1 : getType());
        setItem(item, getType() == 1);
    }

	public void setExtraDetails(int id) {
		setItemId(id);
		setMarketPrice(GrandExchange.getPrice(id));
		int totalsellamount = GrandExchange.getTotalSellQuantity(id);
		int totalbuyamount = GrandExchange.getTotalBuyQuantity(id);
		int sellprice = GrandExchange.getCheapestSellPrice(id);
		int sellamount = GrandExchange.getSellQuantity(id);
		int buyprice = GrandExchange.getBestBuyPrice(id);
		int buyamount = GrandExchange.getBuyQuantity(id);
		player.getPackets().sendIComponentText(105, 185, ItemExaminesDataParser.getGEExamine(new Item(id)));
		if (getType() == 1) { // Selling
			if (totalbuyamount > 0) {
				player.getPackets().sendIComponentText(105, 185,
						ItemExaminesDataParser.getGEExamine(new Item(id)) +
								"<br><br>Quantity: " + Utils.getFormattedNumber(buyamount, ',') +
								"<br>Best sell price: " + Utils.getFormattedNumber(buyprice, ','));
			} else {
				player.getPackets().sendIComponentText(105, 185,
						ItemExaminesDataParser.getGEExamine(new Item(id)) +
								"<br><br>Grand Exchange: No current buyers.");
			}
		} else if (getType() == 0) { // Buying
			if (totalsellamount > 0) {
				player.getPackets().sendIComponentText(105, 185,
						ItemExaminesDataParser.getGEExamine(new Item(id)) +
								"<br><br>Quantity: " + Utils.getFormattedNumber(sellamount, ',') +
								"<br>Lowest sell price: " + Utils.getFormattedNumber(sellprice, ','));
			} else {
				player.getPackets().sendIComponentText(105, 185,
						ItemExaminesDataParser.getGEExamine(new Item(id)) +
								"<br><br>Grand Exchange: No current sellers.");
			}
		}

	}

	public void setItem(Item item, boolean sell) {
		if (item.getId() == ShopViewer.COINS || !ItemConstants.isTradeable(item) || item.getName().contains("charm") || item.getId() == 11640 || item.getId() == 37753) {
			player.sendMessage("This item cannot be traded on the Grand Exchange.");
//			player.getPackets().sendSound(4039, 1, 1);
			return;
		}
		if (item.getId() == 18832)
			item.setId(18830);
		if (item.getDefinitions().isNoted() && item.getDefinitions().getCertId() != -1)
			item = new Item(item.getDefinitions().getCertId(), item.getAmount());
		int price = GrandExchange.getPrice(item.getId());
		setPricePerItem(price);
		setAmount(item.getAmount());
		setExtraDetails(item.getId());
	}

	public void confirmOffer() {
		int type = getType();
		if (type == -1)
			return;
		int slot = getCurrentSlot();
		if (slot == -1 || !isSlotFree(slot))
			return;
		boolean buy = type == 0;
		int itemId = getItemId();

		System.out.println("DEBUG ConfirmOffer:");
		System.out.println("ItemId: " + getItemId());
		System.out.println("Amount: " + getAmount());
		System.out.println("PricePerItem: " + getPricePerItem());
		System.out.println("Calculated Total Price: " + (getPricePerItem() * getAmount()));
		System.out.println("Inventory coins: " + player.getInventory().getCoinsAmount());
		System.out.println("Pouch coins: " + player.getMoneyPouch().getTotal());



		if (itemId == -1) {
			player.sendMessage("You must choose an item to " + (buy ? "buy" : "sell") + "!");
//			player.getPackets().sendSound(4039, 1, 1);
			return;
		}
		int amount = getAmount();
		if (amount == 0) {
			player.sendMessage("You must choose the quantity you wish to " + (buy ? "buy" : "sell") + "!");
//			player.getPackets().sendSound(4039, 1, 1);
			return;
		}
		int pricePerItem = getPricePerItem();
		if (pricePerItem != 0) {
			if (amount > 2147483647 / pricePerItem) { // TOO HIGH
				player.sendMessage("You do not have enough coins to cover the offer.");
//				player.getPackets().sendSound(4039, 1, 1);
				return;
			}
		}
		/*
		 * if ((GrandExchange.getPrice(getItemId()) / 2 < GrandExchange.getPrice(getItemId()) - pricePerItem)) { player.getPackets().sendConfig(1113, 1);
		 * 
		 * return; }
		 */
		if (buy) {
			int price = pricePerItem * amount;


			if (pricePerItem > 1000000000) {
				player.sendMessage("You cannot buy items for more than 1000000000 coins each on the Grand Exchange.");
				return;
			}

			if (!player.hasMoney(price)) {
				player.sendMessage("You do not have enough coins to cover the offer.");
//				player.getPackets().sendSound(4039, 1, 1);
				return;
			} else
				player.takeMoney(price);
		} else {

			if (pricePerItem > 0) {
				player.sendMessage("You cannot sell items on the Grand Exchange.");
				return;
			}

			int inventoryAmount = getItemAmount(new Item(itemId));
			if (amount > inventoryAmount) {
				player.sendMessage("You do not have enough of this item in your inventory.");
				return;
			}
			int notedId = ItemDefinitions.getItemDefinitions(itemId).certId;
			int notedAmount = player.getInventory().getNumberOf(notedId);
			if (notedAmount < amount) {
				player.getInventory().deleteItem(notedId, notedAmount);
				player.getInventory().deleteItem(itemId, amount - notedAmount);
			} else
				player.getInventory().deleteItem(notedId, amount);
		}
		GrandExchange.sendOffer(player, slot, itemId, amount, pricePerItem, buy);
		cancelOffer();
	}

	public void makeOffer(int slot, boolean sell) {
		if (!isSlotFree(slot) || getCurrentSlot() != -1)
			return;
		if (sell && player.getSkills().getTotalLevel() < 150) {
			player.sendMessage("You need a total level of at least 150 to sell items on the Grand Exchange.", false);
			return;
		}
		setType(sell ? 1 : 0);
		setSlot(slot);
        if (!sell)
            sendGEItemSearch();
	}
	
    public void repeatOffer(int itemId, int amount, int pricePerItem, int marketPrice, int slotId, int type) {
        if (!isSlotFree(slotId))
            return;
        setItemId(itemId);
        setAmount(amount);
        setPricePerItem(pricePerItem);
        setMarketPrice(marketPrice);
        setSlot(slotId);
        removeGEItemSearch();
        setType(type);
    }
    
	/**
	 * Checks if the player can use the G.E.
	 *
	 * @return true if The player can use it.
	 */
	public boolean canUse() {
		return !player.isIronMan() && !player.isHCIronMan() &&
        		!player.isNoviceIronMan() && !player.isExpertIronMan() && !player.isIntermediateIronMan() && !player.isKingOfTheSkillGameMode();
	}

	/**
	 * Opens the item sets interface.
	 */
	
	public void openItemSets() {
	    openItemSets(false);
	}
	
	public void openItemSets(boolean fromMenu) {
	    if (!fromMenu)
	        player.getInterfaceManager().openMenu(5, 3);
        player.getInterfaceManager().sendLockGameTab(InterfaceManager.INVENTORY_TAB, true, false);
        player.getPackets().sendIComponentSettings(1719, 6, 0, 119, 14);
        player.getPackets().sendUnlockIComponentOptionSlots(1721, 7, 0, 27, 0, 1);
	}
	
    public void removeGEItemSearch() {
        player.getPackets().sendGlobalConfig(2235, -1);
    }
    
    private void back() {
        player.getPackets().sendUnlockIComponentOptionSlots(105, 282, -1, 0, 0, 1);
        player.getPackets().sendUnlockIComponentOptionSlots(105, 284, -1, 0, 0, 1);
        player.getInterfaceManager().sendLockGameTab(InterfaceManager.INVENTORY_TAB, true, false);
        player.getPackets().sendUnlockIComponentOptionSlots(107, 7, 0, 27, 0, 1);
        cancelOffer();
        player.setCloseInterfacesEvent(new Runnable() {
            @Override
            public void run() {
                removeGEItemSearch();
                player.getInterfaceManager().closeMenu();
            }
        });
    }
    
    public void collectAll(boolean bank) {
        for (int i = 0; i < 8; i++) {
            Offer offer = GrandExchange.getOffer(player, i);
            if (offer == null)
                continue;
            if (!player.getInventory().hasFreeSlots()) {
                player.getPackets().sendGameMessage("Not enough space in your inventory.");
                return;
            }
            for (int i2 = 0; i2 < 2; i2++) {
                Item item = offer.getReceivedItems().get(i2);
                if (item == null)
                    continue;
                collectItems(i, i2, 0); // tries to collect everything
                if (bank) {
                    int slot = player.getInventory().getItems().getThisItemSlot(item);
                    if (slot == -1)
                        continue;
                    player.getBank().depositItem(slot, item.getAmount(), false);
                }
            }
        }
    }
    
    public void sendGEItemSearch() {
        player.getPackets().sendGEItemSearch();
    }
    
    public int getFreeSlot() {
        for (int i = 0; i < offerUIds.length; i++) {
            if (isSlotFree(i))
                return i;
        }
        return -1;
    }
}
