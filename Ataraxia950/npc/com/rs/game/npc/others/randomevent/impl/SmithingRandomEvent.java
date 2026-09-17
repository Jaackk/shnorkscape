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
public class SmithingRandomEvent extends RandomEventNPC {

	private static final long serialVersionUID = 5964500170727703197L;

	/**
	 * Constructs a new class.
	 * @param tile
	 * @param target
	 */
	public SmithingRandomEvent(WorldTile tile, Player target) {
		super(2551, new WorldTile(tile), target);
	}

	@Override
	public void giveReward(Player player) {
		player.stopAll(true, false, true);
		player.setNextAnimation(new Animation(-1));
		if (player != randomEventTarget || player.isLocked()) {
			player.getDialogueManager().startDialogue("SimpleNPCMessage", 2551, "I don't have time for chit-chats.");
			return;
		}
		player.lock();
		if (player.getSmithingActions() >= 1000) {
			if (!player.hasItem(new Item(25195)) && !player.hasItem(new Item(32280))) {
				player.addItem(new Item(25195, 1));
				player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
			}
		}
		if (player.getSmithingActions() >= 2000) {
			if (!player.hasItem(new Item(25196))) {
				player.addItem(new Item(25196, 1));
				player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
			}
		}
		if (player.getSmithingActions() >= 3000) {
			if (!player.hasItem(new Item(25197))) {
				player.addItem(new Item(25197, 1));
				player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
			}
		}
		if (player.getSmithingActions() >= 4000) {
			if (!player.hasItem(new Item(25198))) {
				player.addItem(new Item(25198, 1));
				player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
			}
		}
		if (player.getSmithingActions() >= 5000) {
			if (!player.hasItem(new Item(25199))) {
				player.addItem(new Item(25199, 1));
				player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
			}
		}
		if (player.getSmithingActions() >= 10000) {
			if (!player.hasItem(new Item(32276)) && !player.hasItem(new Item(32280))) {
				player.addItem(new Item(32276, 1));
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
		player.sendMessage("<col=ff0000>The Dwarven Miner gives you a reward before leaving.");
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
