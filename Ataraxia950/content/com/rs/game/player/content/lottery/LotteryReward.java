package com.rs.game.player.content.lottery;

import java.io.Serializable;
import java.util.Calendar;

public class LotteryReward implements Serializable {

	private static final long serialVersionUID = -2244140958736783408L;
	
	private final byte spot;
	private final long amount;
	private final Calendar date;

	public LotteryReward(final long amount, final byte spot) {
		this.amount = amount;
		this.date = Calendar.getInstance();
		this.spot = spot;
	}
	
	public long getAmount() {
		return amount;
	}
	
	public Calendar getDate() {
		return date;
	}
	
	public byte getSpot() {
		return spot;
	}
	
}
