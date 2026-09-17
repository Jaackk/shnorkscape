package com.rs.game.player.content.lottery;

import com.rs.game.World;
import com.rs.game.player.Player;
import com.rs.utils.Logger;
import com.rs.utils.SerializableFilesManager;
import com.rs.utils.Utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class LotteryManager {

	private static long POT;
	private static final Map<String, Byte> TICKETS = new HashMap<String, Byte>();
	private static final List<String> ENTRIES = new ArrayList<String>();
	private static final String PATH = "data/lottery/Lottery.txt";

	public static final void init() {
		try {
			new File("data/lottery/").mkdirs();
			final File file = new File(PATH);
			if (!file.exists()) {
				Logger.getGlobal().info("Lottery initiated with no data.");
				return;
			}
			BufferedReader br = new BufferedReader(new FileReader(file));
			String s;
			if ((s = br.readLine()) != null)
				POT = Long.parseLong(s);
			while ((s = br.readLine()) != null) {
				if (s.isEmpty())
					break;
				String[] line = s.split(", ");
				
				TICKETS.put(line[0], Byte.valueOf(line[1]));
			}
			Logger.getGlobal().info("Lottery initiated " + TICKETS.size() + " entries and a total pot of " + Utils.formatNumber(POT) + ".");
			br.close();
		} catch (Exception e) {
			Logger.getGlobal().catching(e);
		}
	}
	
	public static final void save() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(PATH));
			writer.write(String.valueOf(POT));
			writer.newLine();
			TICKETS.forEach((name, type) -> {
				try {
					writer.write(name + ", " + type);
					writer.newLine();
				} catch (IOException e) {
					Logger.getGlobal().catching(e);
				}
			});
			writer.close();
		} catch (Exception e) {
			Logger.getGlobal().catching(e);
		}
	}
	
	public static void initLottery() {
		TICKETS.put("Kris", (byte) 1);
		for (int i = 0; i < 5; i++) {
			TICKETS.put("Kris" + Utils.random(5), (byte) Utils.random(2));
		}
		POT = 500000000;
		generateRandomWinner();
	}
	
	public static final long getPot() {
		return POT;
	}
	
	public static final boolean canPurchaseTicket(final Player player) {
		final int amount = getMaximumTicketsAmount(player);
		return getTickets(player) < amount;
	}
	
	public static final int getMaximumTicketsAmount(final Player player) {
		return player.isMasterDonator() ? 3 : player.isLegendaryDonator() ? 2 : 1;
	}
	
	public static final void purchaseTicket(final Player player, final byte type) {
		TICKETS.put(player.getUsername(), type);
		player.sendMessage("You've purchased a ticket for the lottery. You now have " + getTickets(player) + " to this lottery.");
	}
	
	public static final int getTickets(final Player player) {
		int tickets = 0;
		for (String ticket : TICKETS.keySet())
			if (ticket.equalsIgnoreCase(player.getUsername()))
				tickets++;
		return tickets;
	}
	
	private static final void generateRandomWinner() {
		if (TICKETS.size() == 0) {
			World.sendWorldMessage("<col=ff0000><img=6>Lottery: The lottery has closed with zero participants.", false);
			return;
		}
		TICKETS.forEach((name, type) -> ENTRIES.add(name));
		Collections.shuffle(ENTRIES);
		final String[] winnerNames = new String[ENTRIES.size() < 3 ? ENTRIES.size() : 3];
		final Player[] winners = new Player[winnerNames.length];
		final List<String> logged = new ArrayList<String>();
		for (int i = 0; i < winnerNames.length; i++) {
			winnerNames[i] = ENTRIES.remove(Utils.random(ENTRIES.size()));
			if (World.getPlayer(winnerNames[i]) == null) {
				winners[i] = SerializableFilesManager.loadPlayer(winnerNames[i]);
				if (winners[i].getUsername() == null)
					winners[i].setUsername(winnerNames[i]);
				logged.add(winners[i].getUsername());
			} else
			winners[i] = World.getPlayer(winnerNames[i]);
		}
		ENTRIES.clear();
		final Map<Player, Long> map = new HashMap<Player, Long>();
		for (int i = 0; i < winners.length; i++) {
			map.put(winners[i], map.get(winners[i]) == null ? getAmount(i) : map.get(winners[i]) + getAmount(i));
		} if (winnerNames.length == 1) {
			World.sendWorldMessage("<col=ff0000><img=6>Lottery: The winner of the lottery is " + winnerNames[0] + "!", false);
		} else {
			StringBuilder builder = new StringBuilder();
			builder.append("<col=ff0000><img=6>Lottery: The winners of the lottery are ");
			for (int i = 0; i < winnerNames.length; i++) {
				if (i != winnerNames.length - 1)
					builder.append(winnerNames[i] + ", ");
				else
					builder.append("and " + winnerNames[i] + "!");
			}
			World.sendWorldMessage(builder.toString(), false);
		}
		for (Entry<Player, Long> p : map.entrySet()) {
			if (logged.contains(p.getKey().getUsername())) {
				p.getKey().getLottery().addReward(new LotteryReward(p.getValue(), getSpot(p.getKey().getUsername(), winnerNames)));
				SerializableFilesManager.savePlayer(p.getKey());
			} else {
				p.getKey().getLottery().addReward(p.getValue());
				p.getKey().sendMessage("Congratulations! You've won the " + Lottery.getSpot(getSpot(p.getKey().getUsername(), winnerNames)) + " place on the lottery for a total prize of " + Utils.formatNumber(p.getValue()) + "!");
			}
		}
	}
	
	private static final byte getSpot(final String username, final String[] winners) {
		for (int i = 0; i < winners.length; i++) {
			if (winners[i].equalsIgnoreCase(username))
				return (byte) (i + 1);
		}
		return 0;
	}
	
	private static final long getAmount(int spot) {
		switch(spot) {
		case 0:
			if (ENTRIES.size() == 1)
				return (long) (POT * 0.85);
			else if (ENTRIES.size() == 2)
				return (long) (POT * 0.55);
			return (long) (POT * 0.5);
		case 1:
			if (ENTRIES.size() == 2)
				return (long) (POT * 0.3);
			return (long) (POT * 0.25);
			default:
				return (long) (POT * 0.1);
		}
	}
	
}
