package com.rs.game.npc.others.randomevent.impl;

import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.others.randomevent.RandomEventNPC;
import com.rs.game.player.Player;
import com.rs.game.player.content.achievementsystem.AchievementList;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * 
 *         Created on Sep 16, 2018.
 */
public class FiremakingRandomEvent extends RandomEventNPC {

	private static final long serialVersionUID = 6025836132018690531L;
	private static final Item[] rewards = { new Item(439, Utils.random(1, 2)), new Item(437, Utils.random(1, 2)), new Item(441, Utils.random(1, 2)), new Item(454, 4), new Item(443, 1), new Item(445, Utils.random(3, 5)), new Item(448, Utils.random(2, 4)), new Item(450, 1), new Item(452, 1), new Item(556, Utils.random(5, 200)), new Item(554, Utils.random(2, 100)), new Item(562, Utils.random(4, 20)), new Item(559, Utils.random(20, 80)), new Item(562, Utils.random(2, 20)), new Item(563, Utils.random(6, 10)), new Item(560, Utils.random(2, 6)), new Item(558, Utils.random(20, 40)), new Item(1617, 1), new Item(1619, 1), new Item(1621, 1), new Item(1623, 1), new Item(1625, 1), new Item(1627, 1), new Item(1629, 1), new Item(1631, 1), new Item(995, Utils.random(50, 2000)), new Item(12158, Utils.random(1, 3)), new Item(12159, Utils.random(1, 7)), new Item(12160, Utils.random(1, 4)), new Item(12163, Utils.random(1, 2)), new Item(593, Utils.random(1, 6)), new Item(985, 1), new Item(987, 1) };

	/**
	 * Constructs a new class.
	 * @param tile
	 * @param target
	 */
	public FiremakingRandomEvent(WorldTile tile, Player target) {
		super(15451, tile, target);
	}

	@Override
	public void giveReward(Player player) {
		if (player != randomEventTarget || player.isLocked())
			return;
		
		Item reward = rewards[(Utils.random(rewards.length))];
		Item reward2 = rewards[(Utils.random(rewards.length))];
		Item reward3 = rewards[(Utils.random(rewards.length))];
		Item reward4 = rewards[(Utils.random(rewards.length))];
		
		player.stopAll(true, false, true);
		player.lock();
		player.setNextAnimation(new Animation(16705));
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				if (reward.getId() == 995)
					player.addMoney(reward.getAmount());
				else
					addItem(player, reward);
				if (reward2.getId() == 995)
					player.addMoney(reward2.getAmount());
				else
					addItem(player, reward2);
				if (Utils.random(5) >= 3) {
					if (reward3.getId() == 995)
						player.addMoney(reward3.getAmount());
					else
						addItem(player, reward3);
				}
				if (Utils.random(10) <= 2) {
					if (reward4.getId() == 995)
						player.addMoney(reward4.getAmount());
					else
						addItem(player, reward4);
				}
				if (player.getLogsBurned() >= 1000) {
					if (!player.hasItem(new Item(13659))) {
						player.addItem(new Item(13659));
						player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
					}
				}
				if (player.getLogsBurned() >= 2000) {
					if (!player.hasItem(new Item(13660))) {
						player.addItem(new Item(13660));
						player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
					}
				}
				if (player.getLogsBurned() >= 3000) {
					if (!player.hasItem(new Item(13661))) {
						player.addItem(new Item(13661));
						player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
					}
				}
				player.sendMessage("<col=ff0000>The fire spirit gives you a reward before disappearing.");
				player.unlock();
				randomEventTarget.setCurrentRandomEventNPC(null);
				finish();
				stop();
			}
		}, 2);
	}

	public void addItem(Player player, Item item) {
		if (!player.getInventory().addItem(item))
			World.updateGroundItem(item, player, player, 60, 0, false);
		else
			player.getInventory().addItem(item);
	}

}
