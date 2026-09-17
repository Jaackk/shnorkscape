package com.rs.game.npc.others.randomevent.impl;

import com.rs.game.Animation;
import com.rs.game.ForceTalk;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.others.randomevent.RandomEventNPC;
import com.rs.game.player.Player;
import com.rs.game.player.content.achievementsystem.AchievementList;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * 
 *         Created on Sep 16, 2018.
 */
public class FarmingRandomEvent extends RandomEventNPC {

	private static final long serialVersionUID = -1066713219026106613L;

	/**
	 * Constructs a new class.
	 * @param tile
	 * @param target
	 */
	public FarmingRandomEvent(WorldTile tile, Player target) {
		super(2170, tile, target);
	}

	@Override
	public void giveReward(Player player) {
		player.stopAll(true, false, true);
		player.setNextAnimation(new Animation(-1));
		if (player != randomEventTarget || player.isLocked()) {
			player.getDialogueManager().startDialogue("SimpleNPCMessage", 1051, "I don't have time for chit-chats.");
			return;
		}
		player.lock();
		if (player.getProduceGathered() >= 500) {
			if (!player.hasItem(new Item(7409, 1))) {
				player.addItem(new Item(7409, 1));
				player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
			}
		}
		if (player.getProduceGathered() >= 1000) {
			if (!player.hasItem(new Item(31347)) && !player.hasItem(new Item(34926))) {
				player.addItem(new Item(31347, 1));
				player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
			}
		}
		if (player.getProduceGathered() >= 2000) {
			if (!player.hasItem(new Item(31346))) {
				player.addItem(new Item(31346, 1));
				player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
			}
		}
		if (player.getProduceGathered() >= 3000) {
			if (!player.hasItem(new Item(31345))) {
				player.addItem(new Item(31345, 1));
				player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
			}
		}
		if (player.getProduceGathered() >= 4000) {
			if (!player.hasItem(new Item(31344))) {
				player.addItem(new Item(31344, 1));
				player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
			}
		}
		if (player.getProduceGathered() >= 5000) {
			if (!player.hasItem(new Item(31343))) {
				player.addItem(new Item(31343, 1));
				player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
			}
		}
		if (player.getProduceGathered() >= 25000) {
			if (!player.hasItem(new Item(34926)) && !player.hasItem(new Item(34922))) {
				player.addItem(new Item(34922, 1));
				player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
			}
		}
		stop = true;
		player.addMoney(Utils.random(5000));
		if (Utils.random(35) == 0) {
			player.addItem(new Item(48792));
			World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: has received a Ataraxia Dollar (Vote) Which is Super Rare!", false);

		}
		setNextForceTalk(new ForceTalk("See you later, " + randomEventTarget.getDisplayName() + "!"));
		player.sendMessage("<col=ff0000>The Farmer gives you a reward before leaving.");
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				player.unlock();
				stop = false;
				randomEventTarget.setCurrentRandomEventNPC(null);
				finish();
				stop();
			}
		}, 2);
	}

}
