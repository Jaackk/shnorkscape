package com.rs.utils;

import com.rs.game.item.Item;

import java.io.Serializable;

public class Lend implements Serializable {

	private static final long serialVersionUID = -1131979864451286325L;
	private final String lender;
	private final String lendee;
	private final Item item;
	private final long timeTill;

	public Lend(String lender, String lendee, Item item, long timeTill) {
		this.lender = lender;
		this.lendee = lendee;
		this.item = item;
		this.timeTill = timeTill;
	}

	public String getLender() {
		return lender;
	}

	public String getLendee() {
		return lendee;
	}

	public Item getItem() {
		return item;
	}

	public long getTime() {
		return timeTill;
	}
}