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
public class CraftingRandomEvent extends RandomEventNPC {

	private static final long serialVersionUID = -5705838548456037396L;

	/**
	 * Constructs a new class.
	 * @param tile
	 * @param target
	 */
	public CraftingRandomEvent(WorldTile tile, Player target) {
		super(7954, tile, target);
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
		if (player.getItemsMade() >= 1000) {
			if (!player.hasItem(new Item(25185))) {
				player.addItem(new Item(25185, 1));
				player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
			}
		}
		if (player.getItemsMade() >= 2000) {
			if (!player.hasItem(new Item(25186))) {
				player.addItem(new Item(25186, 1));
				player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
			}
		}
		if (player.getItemsMade() >= 3000) {
			if (!player.hasItem(new Item(25187))) {
				player.addItem(new Item(25187, 1));
				player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
			}
		}
		if (player.getItemsMade() >= 4000) {
			if (!player.hasItem(new Item(25188))) {
				player.addItem(new Item(25188, 1));
				player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
			}
		}
		if (player.getItemsMade() >= 5000) {
			if (!player.hasItem(new Item(25189))) {
				player.addItem(new Item(25189, 1));
				player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
			}
		}
		if (player.getItemsMade() >= 25000) {
			if (!player.hasItem(new Item(32277)) && !player.hasItem(new Item(32281))) {
				player.addItem(new Item(32277, 1));
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
		player.sendMessage("<col=ff0000>The Artisan gives you a reward before leaving.");
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
