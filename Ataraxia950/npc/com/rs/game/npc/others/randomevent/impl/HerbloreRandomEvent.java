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
public class HerbloreRandomEvent extends RandomEventNPC {

	private static final long serialVersionUID = -5489422431143955721L;

	/**
	 * Constructs a new class.
	 * @param tile
	 * @param target
	 */
	public HerbloreRandomEvent(WorldTile tile, Player target) {
		super(17347, tile, target);
	}

	@Override
	public void giveReward(Player player) {
		player.stopAll(true, false, true);
		player.setNextAnimation(new Animation(-1));
		if (player != randomEventTarget || player.isLocked()) {
			player.getDialogueManager().startDialogue("SimpleNPCMessage", 17347, "I don't have time for chit-chats.");
			return;
		}
		player.lock();
		if (player.getPotionsMade() >= 1000) {
			if (!player.hasItem(new Item(25194))) {
				player.addItem(new Item(25194, 1));
				player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
			}
		}
		if (player.getPotionsMade() >= 2000) {
			if (!player.hasItem(new Item(25193))) {
				player.addItem(new Item(25193, 1));
				player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
			}
		}
		if (player.getPotionsMade() >= 3000) {
			if (!player.hasItem(new Item(25190)) && !player.hasItem(new Item(34923))) {
				player.addItem(new Item(25190, 1));
				player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
			}
		}
		if (player.getPotionsMade() >= 4000) {
			if (!player.hasItem(new Item(25192))) {
				player.addItem(new Item(25192, 1));
				player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
			}
		}
		if (player.getPotionsMade() >= 5000) {
			if (!player.hasItem(new Item(25191))) {
				player.addItem(new Item(25191, 1));
				player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
			}
		}
		if (player.getPotionsMade() >= 25000) {
			if (!player.hasItem(new Item(34923)) && !player.hasItem(new Item(34919))) {
				player.addItem(new Item(34919, 1));
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
		player.sendMessage("<col=ff0000>Eli gives you a reward before leaving.", true);
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
