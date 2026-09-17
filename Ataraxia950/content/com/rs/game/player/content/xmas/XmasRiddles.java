package com.rs.game.player.content.xmas;

import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.utils.Colors;

import java.util.HashMap;
import java.util.Map;

public class XmasRiddles {

	public static void setRiddle(Player player, int index, boolean done) {
		if (done) {
			player.getXmas().riddle = null;
			switch (index) {
			case 1:
				player.getXmas().riddle1 = true;
				break;
			case 2:
				player.getXmas().riddle2 = true;
				break;
			case 3:
				player.getXmas().riddle3 = true;
				break;
			case 4:
				player.getXmas().riddle4 = true;
				break;
			}
		} else
			player.getXmas().riddle = Riddle.forIndex(index);
	}

	public static void setRiddles(Player player) {
		player.getXmas().riddle = null;
		player.getXmas().riddle1 = true;
		player.getXmas().riddle2 = true;
		player.getXmas().riddle3 = true;
		player.getXmas().riddle4 = true;
	}

	public static void writeInterface(Player player) {
		int fuck = 345;
		int currentIndex = 1;
		String[] emptyIndexes = { "", "", "", "", "", "", "", "" };
		Riddle ours = player.getXmas().riddle;
		if (ours == null)
			return;

		player.getInterfaceManager().sendInterface(fuck);
		for (int i = 0; i < player.getXmas().riddle.getLines().length; i++) {
			if (ours.getLines()[i].length() > 40) {
				int lastIndex = ours.getLines()[i].indexOf(" ", 36);
				emptyIndexes[(i != 0) ? currentIndex + 1 : 0] = ours.getLines()[i].substring(0, lastIndex);
				emptyIndexes[(i != 0) ? currentIndex + 2 : 1] = ours.getLines()[i].substring(lastIndex + 1);
				currentIndex = (i != 0) ? currentIndex + 2 : 1;
			} else {
				emptyIndexes[(i != 0) ? currentIndex : 0] = ours.getLines()[i];
				currentIndex = (i != 0) ? i + 1 : currentIndex;
			}
		}
		for (int i = 0; i < emptyIndexes.length; i++)
			player.getPackets().sendIComponentText(fuck, i + 1, emptyIndexes[i]);
	}

	public static void riddleReward(Player player) {
		if (player.getXmas().riddle == null)
			return;
		Riddle ours = player.getXmas().riddle;
		if (player.getInventory().getFreeSlots() < 2) {
			player.sendMessage(Colors.RED + "You need at least two free inventory slots!", false);
			return;
		} else {
			for (int i = 0; i < ours.getRewards().length; i++)
				player.getInventory().addItem(ours.getRewards()[i]);
			player.getXmas().snowEnergy += 300;
			setRiddle(player, ours.getIndex() - 1, true);
		}
	}

	public static void finishRiddle(Player player) {
		if (player.getXmas().riddle == null)
			return;
		riddleReward(player);
	}

	public enum Riddle {
		RIDDLE_A(
				new String[] { "Between the horn and dragon's roar,", "Not one north and not one south,",
						"Dig upon the spot by the frozen mouth." },
				new int[] { 6858, 6859 }, new int[] { 2666, 5672 }, 1),

		RIDDLE_B(
				new String[] { "In the land of the frozen, looking for seasonal adventure,",
						"Next to the clanging of bells and festive border,",
						"South is the spot, of the pool that defies nature." },
				new int[] { 6860, 6861 }, new int[] { 2655, 5650 }, 2),

		RIDDLE_C(
				new String[] { "Multicoloured globes spiral up towards the night,",
						"Layered green foliage gives the star atop some height,",
						"Use your own pale sphere, to reveal hidden delight." },
				new int[] { 6856, 6857 }, new int[] { 2668, 5647 }, 3),

		RIDDLE_D(
				new String[] { "A vegetable most peculiar and pointy, it's definitely been mislaid,",
						"Pick it up, dust it off, no reason to be afraid,",
						"Feed the most seasonal mammal, and you may just be repayed." },
				new int[] { 6862, 6863 }, new int[] { 28318, 41723 }, 4);

		private static final Map<Integer, Riddle> riddles = new HashMap<Integer, Riddle>();

		static {
			for (Riddle riddle : Riddle.values()) {
				riddles.put(riddle.getIndex(), riddle);
			}
		}

		private final String[] lines;
		private final int[] rewards;
		private final int[] coords;
		private final int index;

		Riddle(String[] lines, int[] rewards, int[] coords, int index) {
			this.lines = lines;
			this.rewards = rewards;
			this.coords = coords;
			this.index = index;
		}

		public static Riddle forIndex(int index) {
			return riddles.get(index);
		}

		public String[] getLines() {
			return lines;
		}

		public int[] rewardArray() {
			return rewards;
		}

		public Item[] getRewards() {
			return new Item[] { new Item(rewards[0]), new Item(rewards[1]) };
		}

		public int[] getCoords() {
			return coords;
		}

		public int[] getObjectives() {
			return coords;
		}

		public int getIndex() {
			return index;
		}
	}
}
