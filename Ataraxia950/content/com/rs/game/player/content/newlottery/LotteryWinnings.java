package com.rs.game.player.content.newlottery;

import java.time.LocalDate;

/**
 * @author lare96 <http://github.com/lare96>
 */
public class LotteryWinnings {

	private final String username;
	private final int winnings;
	private final LocalDate date;

	public LotteryWinnings(String username, int winnings, LocalDate date) {
		this.username = username;
		this.winnings = winnings;
		this.date = date;
	}

	public String getUsername() {
		return username;
	}

	public int getWinnings() {
		return winnings;
	}

	public LocalDate getDate() {
		return date;
	}
}
