package com.rs.game.player.content.lottery;

import com.rs.game.player.Player;
import com.rs.utils.Utils;

import java.io.Serializable;

public class Lottery implements Serializable {

	private static final long serialVersionUID = 7177183090967520328L;
	
	private transient Player player;
	private long winnings;
	private LotteryReward lastReward;
	
	public void setPlayer(final Player player) {
		this.player = player;
	}
	
	public void addReward(final long amount) {
		
		this.winnings += amount;
	}
	
	public void addReward(final LotteryReward reward) {
		this.winnings += reward.getAmount();
		this.lastReward = reward;
	}
	
	public void checkLogin() {
		if (lastReward != null) {
			player.sendMessage("<col=ff0000>Congratulations! You won the " + getSpot(lastReward.getSpot()) + " place on the lottery held on " + lastReward.getDate().getTime() + " for a prize of " + Utils.formatNumber(lastReward.getAmount()) + "!");
			lastReward = null;
		}
	}
	
	public static final String getSpot(byte spot) {
		switch(spot) {
		case 1:
			return "first";
		case 2:
			return "second";
		default:
			return "third";
		}
	}
	
	public long getWinnings() {
		return winnings;
	}
	
}
