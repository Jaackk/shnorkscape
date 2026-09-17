package com.rs.game.player.content.grandExchange;

import com.rs.cache.loaders.ClientScriptMap;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.item.ItemsContainer;
import com.rs.game.player.Player;
import com.rs.utils.LoggingSystem;
import com.rs.utils.Utils;

public class Offer extends Item {

	private static final long serialVersionUID = -4065899425889989474L;
	private transient Player owner;
	private transient int slot;
	private final int price; // price per item selling or buying
	private int totalPriceSoFar; // total profit received so far or spent
	private int totalAmountSoFar; // amt of items sold or bought
	private final ItemsContainer<Item> receivedItems;
	private boolean canceled;
	private final boolean buying;
	private long time;

	public Offer(int price, int totalPriceSoFar, int totalAmountSoFar, boolean cancelled, boolean buying, long time, int id, int amount, int receivedItem, int receivedItemAmount, int leftoverCoinsAmount) {
		super(id, amount);
		this.price = price;
		this.totalPriceSoFar = totalPriceSoFar;
		this.totalAmountSoFar = totalAmountSoFar;
		this.canceled = cancelled;
		this.buying = buying;
		this.time = time;
		receivedItems = new ItemsContainer<Item>(2, true);
		if (receivedItemAmount > 0 && receivedItem > 0)
			receivedItems.add(new Item(receivedItem, receivedItemAmount));
		if (leftoverCoinsAmount > 0)
			receivedItems.add(new Item(995, leftoverCoinsAmount));
	}

	public Offer(int id, int Amount, int price, boolean buy) {
		super(id, Amount);
		this.price = price;
		buying = buy;
		receivedItems = new ItemsContainer<Item>(2, true);
	}

	public void link(int slot, Player owner, long time) {
		this.slot = slot;
		this.owner = owner;
		this.setTime(Utils.currentTimeMillis());
	}

	public ItemsContainer<Item> getReceivedItems() {
		return receivedItems;
	}

	public void unlink() {
		owner = null;
	}

	public void update() {
		if (owner == null)
			return;
		owner.getPackets().sendGrandExchangeOffer(this);
		sendItems();
	}

	public void sendItems() {
		if (owner == null)
			return;
		owner.getPackets().sendItems(ClientScriptMap.getMap(1079).getIntValue(slot), receivedItems);
	}

	public int getPrice() {
		return price;
	}

	public boolean forceRemove() {
		return isCompleted() && !hasItemsWaiting();
	}

	protected boolean isCompleted() {
		return canceled || totalAmountSoFar >= getAmount();
	}

	public int getPercentage() {
		return totalAmountSoFar * getAmount() / 100;
	}

	public int getTotalAmountSoFar() {
		return totalAmountSoFar;
	}

	public int getTotalPriceSoFar() {
		return totalPriceSoFar;
	}

	public int getSlot() {
		return slot;
	}

	public int getStage() {
		if (forceRemove())
			return 0;
		if (isCompleted())
			return buying ? 5 : 13;
		return buying ? 2 : 10;
	}

	public boolean isBuying() {
		return buying;
	}

	public boolean isCancelled() {
		return canceled;
	}

	public boolean cancel() {
		if (isCompleted())
			return false;
		canceled = true;
		if (buying)
			receivedItems.add(new Item(995, (getAmount() - totalAmountSoFar) * price));
		else
			receivedItems.add(new Item(getId(), getAmount() - totalAmountSoFar));
		update();
		return true;
	}

	public void sendUpdateWarning() {
		if (owner == null)
			return;
		if (isCompleted()) {
			if (buying)
				owner.sendMessage("<col=00BFFF><shad=000000>Grand Exchange: Finished buying " + Utils.getFormattedNumber(amount) + " x " + getName() + ".");
			else
				owner.sendMessage("<col=00BFFF><shad=000000>Grand Exchange: Finished selling " + Utils.getFormattedNumber(amount) + " x " + getName() + ".");
		} else {
			if (buying)
				owner.sendMessage("<col=00BFFF><shad=000000>Grand Exchange: Bought " + Utils.getFormattedNumber(totalAmountSoFar) + "/" + Utils.getFormattedNumber(amount) + " x " + getName() + ".");
			else
				owner.sendMessage("<col=00BFFF><shad=000000>Grand Exchange: Sold " + Utils.getFormattedNumber(totalAmountSoFar) + "/" + Utils.getFormattedNumber(amount) + " x " + getName() + ".");
		}
		owner.getPackets().sendMusicEffect(284);
		update();
	}

	public boolean isOfferTooHigh(Offer fromOffer) {
		int left = getAmount() - totalAmountSoFar;
		int leftFrom = fromOffer.getAmount() - fromOffer.totalAmountSoFar;
		int exchangeAmt = left > leftFrom ? leftFrom : left;
		int totalPrice = exchangeAmt * fromOffer.price;
		int amtCoins = receivedItems.getNumberOf(995);

		if (buying) {
			if (fromOffer.receivedItems.getNumberOf(995) + totalPrice <= 0)
				return true;
			int leftcoins = exchangeAmt * price - totalPrice;
			return leftcoins > 0 && amtCoins + leftcoins <= 0;
		} else {
			return amtCoins + totalPrice <= 0;
		}

	}

	public void updateOffer(Offer fromOffer) {
		int left = getAmount() - totalAmountSoFar;
		int leftFrom = fromOffer.getAmount() - fromOffer.totalAmountSoFar;
		int exchangeAmt = left > leftFrom ? leftFrom : left;
		int totalPrice = exchangeAmt * fromOffer.price;
		if (buying) {
			int leftcoins = exchangeAmt * price - totalPrice;
			if (leftcoins > 0)
				receivedItems.add(new Item(995, leftcoins));
			receivedItems.add(buying ? new Item(getId(), exchangeAmt) : new Item(getId(), exchangeAmt));
			fromOffer.receivedItems.add(new Item(995, totalPrice));
		} else {
			fromOffer.receivedItems.add(new Item(getId(), exchangeAmt));
			receivedItems.add(new Item(995, totalPrice));
		}
		totalAmountSoFar += exchangeAmt;
		fromOffer.totalAmountSoFar += exchangeAmt;
		totalPriceSoFar += totalPrice;
		fromOffer.totalPriceSoFar += totalPrice;
		fromOffer.sendUpdateWarning();
	}

	public boolean collectItems(int slot, int option) {
		if (owner == null)
			return false;
		int freeSlots = owner.getInventory().getFreeSlots();
		if (freeSlots < 1) {
			owner.sendMessage("You need more inventory space to be able to do this!");
//			owner.getPackets().sendSound(4039, 1, 10);
			return false;
		}
		Item item = receivedItems.get(slot);
		if (item == null)
			return false;
		ItemDefinitions defs = item.getDefinitions();
		if (!defs.isStackable() && option == (item.getAmount() == 1 ? 0 : 1)) {
			Item add = new Item(item.getId(), item.getAmount() > freeSlots ? freeSlots : item.getAmount());
			owner.getInventory().addItemMoneyPouch(add);
			receivedItems.remove(add);
		} else {
			owner.getInventory().addItemMoneyPouch(new Item(defs.certId != -1 ? defs.certId : item.getId(), item.getAmount()));
			receivedItems.remove(item);
		}
		update();
		LoggingSystem.logGrandExchange(owner, item);
		return true;
	}







	public boolean hasItemsWaiting() {
		return receivedItems.getFreeSlots() != 2;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public void sellOffer() {
		int exchangeAmt = getAmount();
		int actualOfferPrice = getPrice(); // This is the actual price the player set
		int totalPrice = actualOfferPrice * exchangeAmt;

		receivedItems.add(new Item(995, totalPrice));
		totalAmountSoFar += exchangeAmt;
		totalPriceSoFar += totalPrice;
		sendUpdateWarning();
	}


	public void buyOffer(Offer fromOffer) {
		int left = getAmount() - totalAmountSoFar;
		int leftFrom = fromOffer.getAmount() - fromOffer.totalAmountSoFar;
		int exchangeAmt = left > leftFrom ? leftFrom : left;
		int totalPrice = exchangeAmt * (int) ((Math.round(GrandExchange.getPrice(fromOffer.getId())) * 1.25));
		int leftcoins = exchangeAmt * price - totalPrice;
		if (leftcoins > 0)
			receivedItems.add(new Item(995, leftcoins));
		receivedItems.add(buying ? new Item(getId(), exchangeAmt) : new Item(getId(), exchangeAmt));
		totalAmountSoFar += exchangeAmt;
		totalPriceSoFar += totalPrice;
		sendUpdateWarning();
	}

	public Player getOwner() {
		return owner;
	}

	public void setOwner(Player owner) {
		this.owner = owner;
	}
}