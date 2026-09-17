package com.rs.game.player.content;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import com.rs.game.World;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

public class MoneyPouch implements Serializable {

	private static final long serialVersionUID = -3847090682601697992L;

	private transient Player player;
	private boolean open;

	public void addMoney(int amount, boolean delete) {
		if (!player.getControlerManager().processMoneyPouch())
			return;
		if (amount > Integer.MAX_VALUE - getTotal())
			amount = Integer.MAX_VALUE - getTotal();
		if (getTotal() + amount > Integer.MAX_VALUE || getTotal() + amount < 0) {
			player.sendMessage("Your money pouch is not big enough to hold that much cash.");
			if (delete) {
				World.updateGroundItem(new Item(995, amount), player, player, 60, 0, false);
				player.getInventory().deleteItem(995, amount);
			} else
				World.updateGroundItem(new Item(995, amount), player, player, 60, 0, false);
			return;
		}
		if (getTotal() == Integer.MAX_VALUE)
			return;
		if (amount > 1)
			player.sendMessage(Utils.getFormattedNumber(amount) + " coins have been added to your money pouch.", true);
		else
			player.sendMessage("One coin has been added to your money pouch.");
		player.getPackets().sendRunScript(5561, 1, amount);
		player.getMoneyPouch().setTotal(player.getMoneyPouch().getTotal() + amount);
		if (delete)
			player.getInventory().deleteItem(new Item(995, amount));
		refresh();
	}

	public void addMoneyMisc(int amount) {
		if (getTotal() + amount > Integer.MAX_VALUE || getTotal() + amount < 0) {
			player.sendMessage("Your money pouch is not big enough to hold that much cash.");
			if (player.getInventory().getAmountOf(995) != Integer.MAX_VALUE && (player.getInventory().containsItem(995, 1) || player.getInventory().hasFreeSlots())) {
				if ((long) (player.getInventory().getAmountOf(995) + amount) <= Integer.MAX_VALUE) {
					player.getInventory().addItem(new Item(995, amount));
					return;
				}
				final int addedAmount = Integer.MAX_VALUE - player.getInventory().getAmountOf(995);
				amount -= addedAmount;
				player.getInventory().addItem(new Item(995, addedAmount));
			}
			World.updateGroundItem(new Item(995, amount), player, player, 60, 0, false);
			return;
		}
		if (amount > 1) {
			player.sendMessage(Utils.getFormattedNumber(amount) + " coins have been added to your money pouch.", true);
		} else {
			player.sendMessage("One coin has been added to your money pouch.", true);
		}
		player.getPackets().sendRunScript(5561, 1, amount);
		player.getMoneyPouch().setTotal(player.getMoneyPouch().getTotal() + amount);
		refresh();
	}

	public void addOverFlowMoney(int amount) {
		if (getTotal() + amount > Integer.MAX_VALUE || getTotal() + amount < 0) {
			player.sendMessage("Your money pouch is not big enough to hold that much cash.");
			World.updateGroundItem(new Item(995, amount), player, player, 60, 0, false);
			return;
		}
		if (getTotal() != Integer.MAX_VALUE) {
			if (amount + getTotal() < 0) {
				amount = Integer.MAX_VALUE - getTotal();
			}
			if (amount > 1) {
				player.sendMessage(Utils.getFormattedNumber(amount) + " coins have been added to your money pouch.", true);
			} else {
				player.sendMessage("One coin has been added to your money pouch.", true);
			}
			player.getPackets().sendRunScript(5561, 1, amount);
			player.getMoneyPouch().setTotal(player.getMoneyPouch().getTotal() + amount);
			refresh();
		}
	}

	private String getFormattedNumber(int amount) {
		return new DecimalFormat("#,###,##0").format(amount);
	}

	public int getTotal() {
		return player.getMoneyPouchValue();
	}

	public void setTotal(int amount) {
		player.setMoneyPouchValue(amount);
	}
	
	public void login() {
		if (open)
			player.getPackets().sendRunScript(5557, 1);
	}

	public void refresh() {
	     player.getPackets().sendExecuteScript(5559, player.getMoneyPouchValue());
	     player.getPackets().sendItems(623,
	                new Item[] { new Item(995, player.getMoneyPouchValue()) });
	}

	public boolean removeMoneyMisc(int amount) {
		if (amount <= 0 || getTotal() == 0)
			return false;
		if (getTotal() < amount)
			amount = getTotal();
		if (amount > 1)
			player.sendMessage(Utils.getFormattedNumber(amount) + " coins have been removed from your money pouch.", true);
		else
			player.sendMessage(Utils.getFormattedNumber(amount) + " coin has been removed from your money pouch.", true);
		player.getMoneyPouch().setTotal(player.getMoneyPouch().getTotal() - amount);
		player.getPackets().sendRunScript(5561, 0, amount);
		refresh();
		return true;
	}

	public boolean removeAmount(int amount) {
		if (amount <= 0 || getTotal() == 0)
			return false;
		if (getTotal() < amount)
			return false;
		if (amount > 1)
			player.sendMessage(Utils.getFormattedNumber(amount) + " coins have been removed from your money pouch.", true);
		else
			player.sendMessage(Utils.getFormattedNumber(amount) + " coin has been removed from your money pouch.", true);
		player.getMoneyPouch().setTotal(player.getMoneyPouch().getTotal() - amount);
		player.getPackets().sendRunScript(5561, 0, amount);
		refresh();
		return true;
	}

	public boolean takeMoneyFromPouch(int amount) {
		if (getTotal() - amount < 0)
			amount = getTotal();
		if (amount == 0)
			return false;
		setTotal(getTotal() - amount);
		player.getPackets().sendRunScript(5561, 0, amount);
		player.sendMessage((amount == 1 ? "One" : NumberFormat.getIntegerInstance().format(amount)) + " coin" + (amount == 1 ? "" : "s") + " " + (amount == 1 ? "has" : "have") + " been withdrawn from your money pouch.", true);
		refresh();
		return true;
	}

	public void sendExamine() {
		player.sendMessage("Your money pouch currently contains " + getFormattedNumber(player.getMoneyPouchValue()) + " coins.");
	}

	public void withdrawPouch(int amount) {
		if (player.getInventory().getNumberOf(995) == Integer.MAX_VALUE || amount <= 0 || getTotal() <= 0 || player.getInventory().getFreeSlots() == 0) {
			player.sendMessage("Inventory full. To make more room, sell, drop or bank something.");
			return;
		}
		if (player.getInventory().getNumberOf(995) > Integer.MAX_VALUE - amount)
			amount = Integer.MAX_VALUE - player.getInventory().getNumberOf(995);
		if (amount > getTotal())
			amount = getTotal();
		if (amount > 1)
			player.sendMessage(Utils.getFormattedNumber(amount) + " coins have been removed from your money pouch.", true);
		else
			player.sendMessage("One coin has been removed from your money pouch.", true);
		player.getMoneyPouch().setTotal(player.getMoneyPouch().getTotal() - amount);
		player.getInventory().addItem(new Item(995, amount));
		player.getPackets().sendRunScript(5561, 0, amount);
		refresh();
	}

	public boolean cantAdd() {
		return getTotal() == Integer.MAX_VALUE;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}

}