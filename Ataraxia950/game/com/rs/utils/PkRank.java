package com.rs.utils;

import com.rs.game.player.Player;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;

public final class PkRank implements Serializable {

	private static final long serialVersionUID = 5403480618483552509L;
	private static final String PATH = "data/hiscores/pk.vichy";
	private static PkRank[] ranks;
	private final String username;
	private final int kills;
	private final int deaths;

	public PkRank(Player player) {
		this.username = player.getUsername();
		this.kills = player.getKillCount();
		this.deaths = player.getDeathCount();
	}

	public static void init() {
		File file = new File(PATH);
		if (file.exists())
			try {
				ranks = (PkRank[]) SerializableFilesManager.loadSerializedFile(file);
				return;
			} catch (Throwable e) {
				Logger.getGlobal().catching(e);
			}
		ranks = new PkRank[10];
	}

	public static final void save() {
		try {
			SerializableFilesManager.storeSerializableClass(ranks, new File(PATH));
		} catch (Throwable e) {
			Logger.getGlobal().catching(e);
		}
	}

	public static void showRanks(Player player) {
		player.getInterfaceManager().sendInterface(1158);
		player.getPackets().sendIComponentText(1158, 74, Colors.WHITE + "Top 10 Serial Killers</col>");
		int count = 0;
		for (PkRank rank : ranks) {
			if (rank == null)
				return;
			player.getPackets().sendIComponentText(1158, 9 + count * 5,
					Utils.formatPlayerNameForDisplay(rank.username));
			player.getPackets().sendIComponentText(1158, 10 + count * 5,
					"Players killed: " + Utils.getFormattedNumber(rank.kills) + "; Times died: "
							+ Utils.getFormattedNumber(rank.deaths) + ".");
			player.getPackets().sendIComponentText(1158, 11 + count * 5, "");
			count++;
		}
	}

	public static void sort() {
		Arrays.sort(ranks, new Comparator<PkRank>() {
			@Override
			public int compare(PkRank arg0, PkRank arg1) {
				if (arg0 == null)
					return 1;
				if (arg1 == null)
					return -1;
				if (arg0.kills < arg1.kills)
					return 1;
				else if (arg0.kills > arg1.kills)
					return -1;
				else
					return 0;
			}

		});
	}

	public static void checkRank(Player player) {
		if (player.isDeveloper()) {
			return;
		}
		int kills = player.getKillCount();
		for (int i = 0; i < ranks.length; i++) {
			PkRank rank = ranks[i];
			if (rank == null)
				break;
			if (rank.username.equalsIgnoreCase(player.getUsername())) {
				ranks[i] = new PkRank(player);
				sort();
				return;
			}
		}
		for (int i = 0; i < ranks.length; i++) {
			PkRank rank = ranks[i];
			if (rank == null) {
				ranks[i] = new PkRank(player);
				sort();
				return;
			}
		}
		for (int i = 0; i < ranks.length; i++) {
			if (ranks[i].kills < kills) {
				ranks[i] = new PkRank(player);
				sort();
				return;
			}
		}
	}

	/**
	 * Gets the Top 3 PK ranks.
	 *
	 * @return the top 3 player names as String.
	 */
	public static String getPkerTop() {
		short count = 0;
		String top1 = null, top2 = null, top3 = null;
		for (PkRank rank : ranks) {
			if (rank == null)
				return null;
			switch (count) {
			case 0:
				top1 = Utils.formatPlayerNameForDisplay(rank.username);
				break;
			case 1:
				top2 = Utils.formatPlayerNameForDisplay(rank.username);
				break;
			case 2:
				top3 = Utils.formatPlayerNameForDisplay(rank.username);
				break;
			}
			count++;
		}
		return "Top 3 serial killers (PvP)  -  " + top1 + "  -  " + top2 + "  -  " + top3 + ".";
	}
}