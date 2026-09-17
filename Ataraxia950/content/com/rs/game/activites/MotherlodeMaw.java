package com.rs.game.activites;

import com.rs.game.Animation;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * A class used to handle the Motherlode Maw.
 *
 * @author Noel
 */
public class MotherlodeMaw {

	/**
	 * Item array holding all the rewards.
	 */
	private static Item[] rewards;

	/**
	 * Item rewards.
	 */
	private static final Item[] COMMON = { new Item(995, Utils.random(10000, 30000)) };

	private static final Item[] UNCOMMON = { new Item(32848, Utils.random(1, 6)), new Item(5316), new Item(5289),
			new Item(5288), new Item(5315), new Item(11238), new Item(11240), new Item(11242), new Item(11244),
			new Item(11246), new Item(11248), new Item(15513), new Item(11250), new Item(11252), new Item(11254),
			new Item(15517), new Item(11256), new Item(989), new Item(985), new Item(987) };

	private static final Item[] RARE = { new Item(11212, 30), new Item(32230), new Item(25932), new Item(1377),
			new Item(14484), new Item(6739), new Item(1305), new Item(1434), new Item(15259), new Item(25555),
			new Item(1213), new Item(11732), new Item(3140), new Item(11335), new Item(24365), new Item(14479),
			new Item(4087), new Item(1187), new Item(32262, 100) };

	private static final Item[] VERY_RARE = { new Item(18778) };

	/**
	 * Handles the reward.
	 *
	 * @param player
	 *            The player.
	 */
	private static void addReward(Player player) {
		int commonRewards = Utils.random(50), uncommonRewards = Utils.random(100), rareRewards = Utils.random(200);
		if (uncommonRewards > 65 && rareRewards > 100)
			rewards = new Item[] { RARE[Utils.random(RARE.length)] };
		else if (commonRewards > 15 && uncommonRewards > 50)
			rewards = new Item[] { UNCOMMON[Utils.random(UNCOMMON.length)] };
		else if (commonRewards < 40 && uncommonRewards > 40)
			rewards = new Item[] { COMMON[Utils.random(COMMON.length)] };
		else
			rewards = new Item[] { COMMON[Utils.random(COMMON.length)] };
		if (Utils.random(300) > 275)
			rewards = new Item[] { VERY_RARE[Utils.random(VERY_RARE.length)] };
		for (Item item : rewards) {
			if (item.getId() == 995) {
				player.addMoney(item.getAmount());
				return;
			}
			player.addItem(item);
		}
	}

	/**
	 * Handles the Motherlode Maw object.
	 *
	 * @param player
	 *            The player.
	 */
	public static void handleMotherlodeMaw(final Player player) {
		long timeVariation = Utils.currentTimeMillis() - player.motherlodeMaw;
		if (timeVariation < (24 * 60 * 60 * 1000)) {
			player.getDialogueManager().startDialogue("SimpleMessage",
					"You have already used the maw today and it refuses you.");
			return;
		}
		if (player.getSkills().getTotalLevel() < 2585) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You need the required total level to access.");
			player.sendMessage("You must have a total level of at least 2,585 to use the Motherlode Maw.");
			return;
		}
		player.lock();
		player.setNextAnimation(new Animation(25028));
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				player.motherlodeMaw = Utils.currentTimeMillis(); // set timer
				addReward(player);
				player.unlock();
			}
		}, 2);
	}
}