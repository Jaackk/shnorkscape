package com.rs.game.npc.others.randomevent.impl;

import com.rs.game.Animation;
import com.rs.game.ForceTalk;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.others.randomevent.RandomEventNPC;
import com.rs.game.player.Player;
import com.rs.game.player.content.RuneCrafting;
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
public class RunecraftingRandomEvent extends RandomEventNPC {

	private static final long serialVersionUID = -7887305997320661839L;

	/**
	 * Constructs a new class.
	 * @param tile
	 * @param target
	 */
	public RunecraftingRandomEvent(WorldTile tile, Player target) {
		super(15419, tile, target);
	}

	@Override
	public void giveReward(Player player) {
		player.stopAll(true, false, true);
		player.setNextAnimation(new Animation(-1));
		if (player != randomEventTarget || player.isLocked()) {
			player.getDialogueManager().startDialogue("SimpleNPCMessage", 15419, "I don't have time for chit-chats.");
			return;
		}
		player.lock();
		int airMultiplier =  RuneCrafting.getRuneMultiplier("air rune");
		int waterMultiplier =  RuneCrafting.getRuneMultiplier("water rune");
		int earthMultiplier =  RuneCrafting.getRuneMultiplier("earth rune");
		int mindMultiplier =  RuneCrafting.getRuneMultiplier("mind rune");
		int fireMultiplier =  RuneCrafting.getRuneMultiplier("fire rune");
		int bodyMultiplier =  RuneCrafting.getRuneMultiplier("body rune");
		int cosmicMultiplier =  RuneCrafting.getRuneMultiplier("cosmic rune");
		int chaosMultiplier =  RuneCrafting.getRuneMultiplier("chaos rune");
		int natureMultiplier =  RuneCrafting.getRuneMultiplier("nature rune");
		int lawMultiplier =  RuneCrafting.getRuneMultiplier("law rune");
		int deathMultiplier =  RuneCrafting.getRuneMultiplier("death rune");
		int bloodMultiplier =  RuneCrafting.getRuneMultiplier("blood rune");
		if (player.getRunesMade() >= 1000 * RuneCrafting.BASE_RC_MULTIPLIER) {
			if (!player.hasItem(new Item(21487))) {
				player.addItem(new Item(21487, 1));
				player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
			}
		}
		if (player.getRunesMade() >= 2000* RuneCrafting.BASE_RC_MULTIPLIER) {
			if (!player.hasItem(new Item(21485))) {
				player.addItem(new Item(21485, 1));
				player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
			}
		}
		if (player.getAirRunesMade() >= 2500 *airMultiplier) {
			if (!player.hasItem(new Item(13631))) {
				player.addItem(new Item(13631, 1));
				player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
			}
		}
		if (player.getWaterRunesMade() >= 2500 * waterMultiplier) {
			if (!player.hasItem(new Item(13632))) {
				player.addItem(new Item(13632, 1));
				player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
			}
		}
		if (player.getEarthRunesMade() >= 2500 * earthMultiplier) {
			if (!player.hasItem(new Item(13633))) {
				player.addItem(new Item(13633, 1));
				player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
			}
		}
		if (player.getMindRunesMade() >= 2500  * mindMultiplier) {
			if (!player.hasItem(new Item(13630))) {
				player.addItem(new Item(13630, 1));
				player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
			}
		}
		if (player.getFireRunesMade() >= 2500  * fireMultiplier) {
			if (!player.hasItem(new Item(13634))) {
				player.addItem(new Item(13634, 1));
				player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
			}
		}
		if (player.getBodyRunesMade() >= 2500 * bodyMultiplier) {
			if (!player.hasItem(new Item(13635))) {
				player.addItem(new Item(13635, 1));
				player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
			}
		}
		if (player.getCosmicRunesMade() >= 2500 * cosmicMultiplier) {
			if (!player.hasItem(new Item(13636))) {
				player.addItem(new Item(13636, 1));
				player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
			}
		}
		if (player.getChaosRunesMade() >= 2500 * chaosMultiplier) {
			if (!player.hasItem(new Item(13637))) {
				player.addItem(new Item(13637, 1));
				player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
			}
		}
		if (player.getNatureRunesMade() >= 2500 * natureMultiplier) {
			if (!player.hasItem(new Item(13638))) {
				player.addItem(new Item(13638, 1));
				player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
			}
		}
		if (player.getLawRunesMade() >= 2500 * lawMultiplier) {
			if (!player.hasItem(new Item(13639))) {
				player.addItem(new Item(13639, 1));
				player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
			}
		}
		if (player.getDeathRunesMade() >= 2500 * deathMultiplier) {
			if (!player.hasItem(new Item(13640))) {
				player.addItem(new Item(13640, 1));
				player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
			}
		}
		if (player.getBloodRunesMade() >= 2500 * bloodMultiplier) {
			if (!player.hasItem(new Item(13641))) {
				player.addItem(new Item(13641, 1));
				player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
			}
		}
		if (player.getAirRunesMade() >= 2500 * airMultiplier && player.getMindRunesMade() >= 2500 * mindMultiplier && player.getWaterRunesMade() >= 2500 * waterMultiplier &&
				player.getEarthRunesMade() >= 2500 * earthMultiplier && player.getFireRunesMade() >= 2500 * fireMultiplier && player.getBodyRunesMade() >= 2500 * bodyMultiplier
				&& player.getCosmicRunesMade() >= 2500 * cosmicMultiplier && player.getChaosRunesMade() >= 2500 * chaosMultiplier && player.getNatureRunesMade() >= 2500 * natureMultiplier &&
				player.getLawRunesMade() >= 2500 * lawMultiplier && player.getDeathRunesMade() >= 2500 * deathMultiplier && player.getBloodRunesMade() >= 2500 * bloodMultiplier) {
			if (!player.hasItem(new Item(13642))) {
				player.addItem(new Item(13642, 1));
				player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
			}
		}
		if (player.getRunesMade() >= 3000 * RuneCrafting.BASE_RC_MULTIPLIER) {
			if (!player.hasItem(new Item(21486))) {
				player.addItem(new Item(21486, 1));
				player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_OUTFITS);
			}
		}
		if (player.getRunesMade() >= 4000 * RuneCrafting.BASE_RC_MULTIPLIER) {
			if (!player.hasItem(new Item(21484))) {
				player.addItem(new Item(21484, 1));
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
		player.sendMessage("<col=ff0000>The Wizard gives you a reward before leaving.", true);
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				player.unlock();
				stop = false;
				randomEventTarget.setCurrentRandomEventNPC(null);
				finish();
				stop();
			}
		}, 1);
	}

}
