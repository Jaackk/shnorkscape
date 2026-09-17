package com.rs.game.player.content;

import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.World;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.content.achievementsystem.AchievementList;
import com.rs.utils.Colors;

/**
 * Handles the Loyalty points timer task.
 *
 * @author Noel
 */
public class LoyaltyManager {

	private final transient Player player;

	public LoyaltyManager(Player player) {
		this.player = player;
	}

	public void addReward(int lps) {
		player.setLoyaltyPoints(player.getLoyaltyPoints() + lps);
		player.getAchievements().updateProgress(lps, AchievementList.REACH_1000_LOYALTY);
	}

	public void startTimer() {
		player.setTimes(0);
		CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
			private int timer = 1800;
			private String gameplay;

			@Override
			public boolean repeat() {
				if (player.hasFinished() || player == null)
					return false;
				if (timer == 1) {
					player.setTimes(player.getTimes() + 1);
					timer = 1800;

					if (player.getTimes() > 0) {
						if (player.isAFK()) {
							player.sendMessage(Colors.GOLD + "<shad=333333>You don't receive any loyalty points for being afk.", false);
						} else {
							/** This is the cap on loyalty points */
							if (player.getTimes() > 20) {
								player.sendMessage(Colors.DCYAN + "<shad=333333>You've received 300 loyalty points for over 10 hours of gameplay, wow! :)</shad>", false);
								addReward(300);
							} else {
								/* do a little bit of math to figure out verbage */
								gameplay = player.getTimes() % 2 == 0 ? (player.getTimes() / 2) + " hour" + (player.getTimes() == 2 ? "" : "s") : (player.getTimes() == 1) ? "30 minutes" : ((player.getTimes() - 1) / 2) + " hour" + (player.getTimes() == 3 ? "" : "s") + " and 30 minutes";
								player.sendMessage("<col=32CD32><shad=333333>You've received " + (player.getTimes() + 90) + " loyalty points for " + gameplay + " of gameplay!");
								addReward(player.getTimes() + 200);
									player.sendMessage(Colors.GOLD + "<shad=333333>You've received 20 coins, 5 Treasure hunter keys & a Mystery Box for 8h of weekend gameplay!", false);
										player.getTreasureHunter().giveEarnedSpins(5);
										player.getBank().addItem(new Item(6199, 1), true);
										player.addAtaraxiaCoins(20);

								if (World.isWeekend()) {
									switch (player.getTimes()) {
									case 8:
										player.sendMessage(Colors.GOLD + "<shad=333333>You've received an extra 10 coins for 4 hours of weekend gameplay!", false);
										player.addAtaraxiaCoins(10);
										break;
									case 12:
										player.sendMessage(Colors.GOLD + "<shad=333333>You've received 20 coins for 6 hours of weekend gameplay!", false);
										player.addAtaraxiaCoins(20);
										break;
									case 16:
										player.sendMessage(Colors.GOLD + "<shad=333333>You've received 20 coins, 5 Treasure hunter keys & a Mystery Box for 8h of weekend gameplay!", false);
										player.getTreasureHunter().giveEarnedSpins(5);
										player.getBank().addItem(new Item(6199, 1), true);
										player.addAtaraxiaCoins(20);
										break;
									case 20:
										player.sendMessage(Colors.PINK + "<shad=333333>You've received $1 added to your total donation rank and 1 Mystery Box for 10h of weekend gameplay! <3", false);
										player.getBank().addItem(new Item(6199, 1), true);
										player.addLoyaltyMoney(1);
										break;
									}
								}
							}
						}
					}
				}
				if (timer > 0)
					timer--;
				return true;
			}

		}, 0, 1);
	}
}