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
public class SummoningRandomEvent extends RandomEventNPC {

	private static final long serialVersionUID = -481116540444790989L;

	/**
	 * Constructs a new class.
	 * @param tile
	 * @param target
	 */
	public SummoningRandomEvent(WorldTile tile, Player target) {
		super(16887, tile, target);
	}

	@Override
	public void giveReward(Player player) {
		player.stopAll(true, false, true);
		player.setNextAnimation(new Animation(-1));
		if (player != randomEventTarget || player.isLocked()) {
			player.getDialogueManager().startDialogue("SimpleNPCMessage", 16887, "I don't have time for chit-chats.");
			return;
		}
		player.lock();
		if (player.getPouchesMade() >= 500) {
			if (!player.hasItem(new Item(28998))) {
				player.addItem(new Item(28998, 1));
				player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
			}
		}
		if (player.getPouchesMade() >= 1000) {
			if (!player.hasItem(new Item(28999))) {
				player.addItem(new Item(28999, 1));
				player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
			}
		}
		if (player.getPouchesMade() >= 1500) {
			if (!player.hasItem(new Item(28995)) && !player.hasItem(new Item(32278))) {
				player.addItem(new Item(28995, 1));
				player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
			}
		}
		if (player.getPouchesMade() >= 2000) {
			if (!player.hasItem(new Item(28997))) {
				player.addItem(new Item(28997, 1));
				player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
			}
		}
		if (player.getPouchesMade() >= 2500) {
			if (!player.hasItem(new Item(28996))) {
				player.addItem(new Item(28996, 1));
				player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
			}
		}
		if (player.getPouchesMade() >= 5000) {
			if (!player.hasItem(new Item(32278)) && !player.hasItem(new Item(32274))) {
				player.addItem(new Item(32274));
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
		player.sendMessage("<col=ff0000>The Summoner gives you a reward before leaving.");
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
