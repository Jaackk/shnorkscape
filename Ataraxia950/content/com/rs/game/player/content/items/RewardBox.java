package com.rs.game.player.content.items;

import com.rs.Settings;
import com.rs.game.World;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.utils.Colors;
import com.rs.utils.Logger;
import com.rs.utils.Utils;
import com.rs.utils.mysql.QueryExecutor;
import com.rs.utils.mysql.impl.News;
import lombok.AllArgsConstructor;
import lombok.val;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;


/**
 * @author Kris | 30. sept 2018 : 17:44:54
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 * @see <a href="https://rune-status.net/members/kris.354/">Rune-Status profile</a>
 */
public enum RewardBox {
    // mystery box drops
	// Logs
	REGULAR_LOGS(new Item(1511, 1), false,
			new Weight(Rank.SILVER, 30),
			new Weight(Rank.GOLD, 20),
			new Weight(Rank.PLATINUM, 10),
			new Weight(Rank.DIAMOND, 5),
			new Weight(Rank.MASTER, 2)),
	OAK_LOGS(new Item(1521, 1), false,
			new Weight(Rank.SILVER, 25),
			new Weight(Rank.GOLD, 20),
			new Weight(Rank.PLATINUM, 15),
			new Weight(Rank.DIAMOND, 8),
			new Weight(Rank.MASTER, 4)),
	WILLOW_LOGS(new Item(1519, 1), false,
			new Weight(Rank.SILVER, 20),
			new Weight(Rank.GOLD, 25),
			new Weight(Rank.PLATINUM, 20),
			new Weight(Rank.DIAMOND, 10),
			new Weight(Rank.MASTER, 6)),
	MAPLE_LOGS(new Item(1517, 1), false,
			new Weight(Rank.SILVER, 10),
			new Weight(Rank.GOLD, 15),
			new Weight(Rank.PLATINUM, 20),
			new Weight(Rank.DIAMOND, 15),
			new Weight(Rank.MASTER, 8)),
	YEW_LOGS(new Item(1515, 1), false,
			new Weight(Rank.SILVER, 5),
			new Weight(Rank.GOLD, 10),
			new Weight(Rank.PLATINUM, 15),
			new Weight(Rank.DIAMOND, 20),
			new Weight(Rank.MASTER, 10)),
	MAGIC_LOGS(new Item(1513, 1), false,
			new Weight(Rank.SILVER, 2),
			new Weight(Rank.GOLD, 5),
			new Weight(Rank.PLATINUM, 10),
			new Weight(Rank.DIAMOND, 15),
			new Weight(Rank.MASTER, 20)),

	// Ores
	COPPER_ORE(new Item(436, 1), false,
			new Weight(Rank.SILVER, 30),
			new Weight(Rank.GOLD, 20),
			new Weight(Rank.PLATINUM, 10),
			new Weight(Rank.DIAMOND, 5),
			new Weight(Rank.MASTER, 2)),
	TIN_ORE(new Item(438, 1), false,
			new Weight(Rank.SILVER, 30),
			new Weight(Rank.GOLD, 20),
			new Weight(Rank.PLATINUM, 10),
			new Weight(Rank.DIAMOND, 5),
			new Weight(Rank.MASTER, 2)),
	IRON_ORE(new Item(440, 1), false,
			new Weight(Rank.SILVER, 25),
			new Weight(Rank.GOLD, 25),
			new Weight(Rank.PLATINUM, 15),
			new Weight(Rank.DIAMOND, 8),
			new Weight(Rank.MASTER, 4)),
	COAL(new Item(453, 1), false,
			new Weight(Rank.SILVER, 15),
			new Weight(Rank.GOLD, 20),
			new Weight(Rank.PLATINUM, 20),
			new Weight(Rank.DIAMOND, 10),
			new Weight(Rank.MASTER, 6)),
	MITHRIL_ORE(new Item(447, 1), false,
			new Weight(Rank.SILVER, 5),
			new Weight(Rank.GOLD, 10),
			new Weight(Rank.PLATINUM, 15),
			new Weight(Rank.DIAMOND, 20),
			new Weight(Rank.MASTER, 10)),
	ADAMANTITE_ORE(new Item(449, 1), false,
			new Weight(Rank.SILVER, 2),
			new Weight(Rank.GOLD, 5),
			new Weight(Rank.PLATINUM, 10),
			new Weight(Rank.DIAMOND, 15),
			new Weight(Rank.MASTER, 20)),
	RUNITE_ORE(new Item(451, 1), false,
			new Weight(Rank.SILVER, 1),
			new Weight(Rank.GOLD, 3),
			new Weight(Rank.PLATINUM, 8),
			new Weight(Rank.DIAMOND, 12),
			new Weight(Rank.MASTER, 25)),

	// Raw Fish
	RAW_SHRIMP(new Item(317, 1), false,
			new Weight(Rank.SILVER, 30),
			new Weight(Rank.GOLD, 20),
			new Weight(Rank.PLATINUM, 10),
			new Weight(Rank.DIAMOND, 5),
			new Weight(Rank.MASTER, 2)),
	RAW_SARDINE(new Item(325, 1), false,
			new Weight(Rank.SILVER, 25),
			new Weight(Rank.GOLD, 20),
			new Weight(Rank.PLATINUM, 15),
			new Weight(Rank.DIAMOND, 8),
			new Weight(Rank.MASTER, 4)),
	RAW_TROUT(new Item(335, 1), false,
			new Weight(Rank.SILVER, 25),
			new Weight(Rank.GOLD, 20),
			new Weight(Rank.PLATINUM, 10),
			new Weight(Rank.DIAMOND, 5),
			new Weight(Rank.MASTER, 5)),
	RAW_SALMON(new Item(331, 1), false,
			new Weight(Rank.SILVER, 15),
			new Weight(Rank.GOLD, 20),
			new Weight(Rank.PLATINUM, 20),
			new Weight(Rank.DIAMOND, 10),
			new Weight(Rank.MASTER, 6)),
	RAW_TUNA(new Item(359, 1), false,
			new Weight(Rank.SILVER, 10),
			new Weight(Rank.GOLD, 15),
			new Weight(Rank.PLATINUM, 20),
			new Weight(Rank.DIAMOND, 15),
			new Weight(Rank.MASTER, 8)),
	RAW_LOBSTER(new Item(377, 1), false,
			new Weight(Rank.SILVER, 5),
			new Weight(Rank.GOLD, 10),
			new Weight(Rank.PLATINUM, 15),
			new Weight(Rank.DIAMOND, 20),
			new Weight(Rank.MASTER, 10)),
	RAW_SHARK(new Item(383, 1), false,
			new Weight(Rank.SILVER, 2),
			new Weight(Rank.GOLD, 5),
			new Weight(Rank.PLATINUM, 10),
			new Weight(Rank.DIAMOND, 15),
			new Weight(Rank.MASTER, 20));




	private final Item item;
	private final boolean announced;
	private final Weight[] weights;

	private static final RewardBox[] VALUES = values();
	private static final Map<Rank, Integer> ACCUMULATIVE_SIZES = new EnumMap<>(Rank.class);

	static {
		for (int i = VALUES.length - 1; i >= 0; i--) {
			val box = VALUES[i];
			for (int j = box.weights.length - 1; j >= 0; j--) {
				val weight = box.weights[j];
				if (weight.weight <= 0) {
					continue;
				}
				ACCUMULATIVE_SIZES.put(weight.rank, ACCUMULATIVE_SIZES.getOrDefault(weight.rank, 0) + weight.weight);
			}
		}
	}

	RewardBox(final Item item, final boolean announced, final Weight... weights) {
		this.item = item;
		this.announced = announced;
		this.weights = weights;
	}

	/**
	 * Rolls for a random loot on the respective box's loot table.
	 *
	 * @param player the player rolling for the loot.
	 * @param item   the box item.
	 */
	public static final void roll(final Player player, final Item item) {
		if (!player.getInventory().containsItem(item)) {
			throw new NoSuchElementException("Couldn't find the " + item.getName() + " in player \"" + player.getDisplayName() + "\" inventory.");
		}
		val rank = Utils.findMatching(Rank.VALUES, r -> r.boxId == item.getId());
		if (rank == null) {
			throw new RuntimeException("That item hasn't got a loot table attached to it.");
		}

		val totalWeight = (int) ACCUMULATIVE_SIZES.getOrDefault(rank, 0);
		if (totalWeight == 0) {
			player.sendMessage(item.getName() + " has no loot table attached to it.");
			return;
		}

		// Remove the box once before rolling for 10 items
		player.getInventory().deleteItem(item.getId(), 1);

		// Roll 10 items
		for (int roll = 0; roll < 10; roll++) {
			val random = Utils.random(totalWeight + 1);
			int current = 0;
			for (int i = VALUES.length - 1; i >= 0; i--) {
				val box = VALUES[i];
				val weight = Utils.findMatching(box.weights, v -> v.rank == rank);
				if (weight == null || weight.weight == 0) {
					continue;
				}
				if ((current += weight.weight) >= random) {
					if (player.isOwner()) {
						player.sendMessage("You have won a " + box.item.getName() + ". Drop rate: " + ((((float) weight.weight) / totalWeight) * 100) + "%");
					}

					if (box.announced) {
						World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + player.getDisplayName() + " has received " + Utils.getFormattedNumber(box.item.getAmount()) + "x " + box.item.getName() + " from a Mystery Box", false);
						if (!Settings.DEBUG) {
							QueryExecutor.submit(new News(player, "<b><img src=\"../bin/images/news/drop.png\" height=17> " + player.getDisplayName() + " has received a " + box.item.getAmount() + "x " + box.item.getName() + " from a Mystery Box"));
						}
					}

					player.getInventory().addItem(box.item);
					player.sendMessage("You open the box and find " + Utils.getFormattedNumber(box.item.getAmount()) + " x " + box.item.getName() + "! - Mystery boxes opened: " + (player.getMysteryBoxesOpened() + 1));
					break; // Exit inner loop after selecting an item
				}
			}
		}

		// Increment mystery boxes opened counter once per box
		player.incrementMysteryBoxesOpened();
	}

	public static void giveRewardBox(Player player, Rank max) {
		boolean[] boxes = player.getClaimedDonationAwardBoxes();
		for (Rank rank : Rank.VALUES) {
			if (rank.ordinal() > max.ordinal())
				break;

			if (rank == Rank.BRONZE)
				continue;

			boolean collected = boxes[rank.ordinal()];
			if (!collected && rank.ordinal() <= max.ordinal()) {
				boolean inventory = true;
				if (!player.getInventory().addItem(rank.boxId, 1)) {
					inventory = false;
					player.getBank().addItem(new Item(rank.boxId, 1), true);
				}
				boxes[rank.ordinal()] = true;
				player.sendMessage("Congratulations! You are awarded a free " + rank.name().toLowerCase() + " mystery box. It has been added to your" + (inventory ? " inventory." : " bank."));
			}
		}

		player.setClaimedDonationAwardBoxes(boxes);
	}

	/**
	 * Prints all the rates of the loots.
	 */
	public static final void print() {
		val map = new EnumMap<Rank, List<RewardBox>>(Rank.class);
		for (int i = Rank.VALUES.length - 1; i >= 0; i--) {
			map.put(Rank.VALUES[i], new ArrayList<RewardBox>());
		}
		for (int i = VALUES.length - 1; i >= 0; i--) {
			val box = VALUES[i];
			for (int j = box.weights.length - 1; j >= 0; j--) {
				val weight = box.weights[j];
				val list = map.get(weight.rank);
				list.add(box);
			}
		}
		val builder = new StringBuilder();
		for (int i = Rank.VALUES.length - 1; i >= 0; i--) {
			val rank = Rank.VALUES[i];
			val list = map.get(rank);
			val totalWeight = (float) ACCUMULATIVE_SIZES.getOrDefault(rank, -1);
			if (totalWeight == -1) {
				continue;
			}
			builder.append("Rates for rank: " + rank.toString() + "\n");
			for (int j = list.size() - 1; j >= 0; j--) {
				val entry = list.get(j);
				val weight = Utils.findMatching(entry.weights, r -> r.rank == rank);
				builder.append("Chance of " + entry.name() + " is " + (((float) weight.weight) / totalWeight) * 100 + "%\n");
			}
			builder.append("\n");
		}
		Logger.getGlobal().info(builder.toString());
	}

	@AllArgsConstructor
	private static final class Weight {
		private final Rank rank;
		private final int weight;
	}

	@AllArgsConstructor
	public enum Rank {
		//TODO DO NOT CHANGE THE ORDER OF THESE.
		BRONZE(-1), SILVER(13713), GOLD(13714), PLATINUM(13715), DIAMOND(13716), MASTER(13717);
		private static final Rank[] VALUES = values();
		private final int boxId;


		public int getBoxId() {
			return boxId;
		}
	}

}
