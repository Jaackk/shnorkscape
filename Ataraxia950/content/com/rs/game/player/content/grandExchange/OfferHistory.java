package com.rs.game.player.content.grandExchange;

import java.io.Serializable;

public class OfferHistory implements Serializable {

	private static final long serialVersionUID = 7322642705393018764L;

	private final int id;
    private final int quantity;
    private final int price;
	private final boolean bought;

	public OfferHistory(int id, int quantity, int price, boolean bought) {
		this.id = id;
		this.quantity = quantity;
		this.price = price;
		this.bought = bought;
	}

	public int getId() {
		return id;
	}

	public int getQuantity() {
		return quantity;
	}

	public int getPrice() {
		return price;
	}

	public boolean isBought() {
		return bought;
	}
}
