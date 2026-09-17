package com.rs.utils;

import com.rs.game.player.Player;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;

public final class VoteHiscores implements Serializable {

	private static final long serialVersionUID = 5403480618483552509L;
	private static final String PATH = "data/hiscores/vote.vichy";
	private static VoteHiscores[] ranks;
	private final String username;
	private final int votes;

	public VoteHiscores(Player player) {
		this.username = player.getUsername();
		this.votes = player.getVotes();
	}

	public static void init() {
		File file = new File(PATH);
		if (file.exists())
			try {
				ranks = (VoteHiscores[]) SerializableFilesManager.loadSerializedFile(file);
				return;
			} catch (Throwable e) {
				Logger.getGlobal().catching(e);
			}
		ranks = new VoteHiscores[300];
	}

	public static final void save() {
		try {
			SerializableFilesManager.storeSerializableClass(ranks, new File(PATH));
		} catch (Throwable e) {
			Logger.getGlobal().catching(e);
		}
	}

	public static void showRanks(Player player) {
		player.getInterfaceManager().sendInterface(275);
		for (int i = 0; i <= 309; i++)
			player.getPackets().sendIComponentText(275, i, "");
		player.getPackets().sendIComponentText(275, 1, "<shad=000000>Voting Hiscores");
		int count = 0;
		for (VoteHiscores rank : ranks) {
			if (rank == null)
				return;
			player.getPackets().sendIComponentText(275, count + 10, "#" + (count + 1) + " - "
					+ Utils.formatPlayerNameForDisplay(rank.username) + " - times voted: " + rank.votes);
			count++;
		}
	}

	public static void sort() {
		Arrays.sort(ranks, new Comparator<VoteHiscores>() {
			@Override
			public int compare(VoteHiscores arg0, VoteHiscores arg1) {
				if (arg0 == null)
					return 1;
				if (arg1 == null)
					return -1;
				if (arg0.votes < arg1.votes)
					return 1;
				else if (arg0.votes > arg1.votes)
					return -1;
				else
					return 0;
			}
		});
	}

	public static void checkRank(Player player) {
		if (player.isDeveloper())
			return;
		int votes = player.getVotes();
		for (int i = 0; i < ranks.length; i++) {
			VoteHiscores rank = ranks[i];
			if (rank == null)
				break;
			if (rank.username.equalsIgnoreCase(player.getUsername())) {
				ranks[i] = new VoteHiscores(player);
				sort();
				return;
			}
		}
		for (int i = 0; i < ranks.length; i++) {
			VoteHiscores rank = ranks[i];
			if (rank == null) {
				ranks[i] = new VoteHiscores(player);
				sort();
				return;
			}
		}
		for (int i = 0; i < ranks.length; i++) {
			if (ranks[i].votes < votes) {
				ranks[i] = new VoteHiscores(player);
				sort();
				return;
			}
		}
	}

	/**
	 * Gets the Top 3 Voter ranks.
	 *
	 * @return the top 3 player names as String.
	 */
	public static String getVotersTop() {
		short count = 0;
		String top1 = null, top2 = null, top3 = null;
		for (VoteHiscores rank : ranks) {
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
		return "Top 3 all time voters  -  " + top1 + "  -  " + top2 + "  -  " + top3 + ".";
	}
}