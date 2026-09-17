package com.rs.game.player.content.agility;

import com.rs.game.Animation;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.Pots;
import com.rs.game.player.content.achievementsystem.AchievementList;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Handles the Prifddinas Agility course.
 *
 * @author Noel
 */
public class PrifddinasAgilityCourse {

	/**
	 * Handles Leaping across walk way object.
	 *
	 * @param player
	 *            The player using the object.
	 */
	public static void leapAcrossWalkway(final Player player, WorldObject object) {
		if ((Utils.currentTimeMillis() - player.getLastAnimationEnd()) <= 2000)
			return;
		if (!Agility.hasLevel(player, 77))
			return;
		if (player.getY() > 3403)
			return;
		player.lock();
		player.setNextFaceWorldTile(new WorldTile(2178, 3410, 1));
		player.setNextAnimation(new Animation(24587));

		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				player.setNextWorldTile(new WorldTile(2180, 3419, 1));
				addExp(player, false);
			}
		}, 6);
	}

	/**
	 * Handles Leaping across walk way object.
	 *
	 * @param player
	 *            The player using the object.
	 */
	public static void traverseCliff(final Player player, WorldObject object) {
		if ((Utils.currentTimeMillis() - player.getLastAnimationEnd()) <= 2100)
			return;
		if (player.getX() >= 2184)
			return;
		if (!Agility.hasLevel(player, 77))
			return;
		if (!player.withinDistance(new WorldTile(2180, 3419, 1), 1))
			return;
		player.lock();
		player.setNextFaceWorldTile(new WorldTile(2180, 3423, 1));
		player.setNextAnimation(new Animation(25011));

		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				player.setNextWorldTile(new WorldTile(2171, 3437, 1));
				addExp(player, false);
			}
		}, 7);
	}

	/**
	 * Handles Scaling Cathedral object.
	 *
	 * @param player
	 *            The player using the object.
	 */
	public static void scaleCathedral(final Player player, WorldObject object) {
		if ((Utils.currentTimeMillis() - player.getLastAnimationEnd()) <= 2100)
			return;
		if (!Agility.hasLevel(player, 77))
			return;
		if (!(player.getX() == 2171 && player.getY() == 3437))
			return;
		player.lock();
		player.setNextFaceWorldTile(new WorldTile(2170, 3440, 1));
		player.setNextAnimation(new Animation(25014));

		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				player.setNextWorldTile(new WorldTile(2177, 3448, 2));
				addExp(player, false);
			}
		}, 6);
	}

	/**
	 * Handles Vault Roof object.
	 *
	 * @param player
	 *            The player using the object.
	 */
	public static void vaultRoof(final Player player, WorldObject object) {
		if ((Utils.currentTimeMillis() - player.getLastAnimationEnd()) <= 1000)
			return;
		if (!Agility.hasLevel(player, 77))
			return;
		if (!(player.getX() == 2177 && player.getY() == 3448)) {
			return;
		}
		player.lock();
		player.faceObject(object);
		player.setNextAnimation(new Animation(25015));

		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				player.setNextWorldTile(new WorldTile(2187, 3443, 2));
				addExp(player, false);
			}
		}, 6);
	}

	/**
	 * Handles Vaulting the Roof object.
	 *
	 * @param player
	 *            The player using the object.
	 */
	public static void slideDownZipLine(final Player player, WorldObject object) {
		if ((Utils.currentTimeMillis() - player.getLastAnimationEnd()) <= 1850)
			return;
		if (!Agility.hasLevel(player, 77))
			return;
		if (!(player.getX() == 2187 && player.getY() == 3443)) {
			return;
		}
		player.lock();
		player.setNextFaceWorldTile(new WorldTile(2187, 3455, 2));
		player.setNextAnimation(new Animation(25016));

		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				player.setNextWorldTile(new WorldTile(2176, 3400, 1));
				addExp(player, true);
				player.getContracts().recordAction(Skills.AGILITY, 5);
			}
		}, 7);
	}

	/**
	 * Handles adding Experience depending on your Agility level + other assets.
	 *
	 * @param player The player to reward.
	 */
	private static void addExp(Player player, boolean last) {
		int level = player.getSkills().getLevelForXp(Skills.AGILITY);
		int exp = (level < 82 ? 250 : (level < 87 ? 300 : (level < 92 ? 350 : (level < 97 ? 400 : 450))));
		if (last) {
			exp *= 10;
			if (player.jujuPotions.isActive(Pots.Effects.PERFECT_AGILITY_JUJU) && ThreadLocalRandom.current().nextInt(4) == 0) {
				player.addItem(new Item(39922, Utils.rand(4, 12)));
				player.sendMessage("Through the power of juju you receive some skilling tickets!");
			}
			player.addLapsRan();
			Agility.checkAgilityRandom(player);
			player.hefinLapReward = true;
			if (player.getHefinLaps() < 500)
				player.addHefinLaps();
			if (player.getHefinLaps() == 200)
				player.sendMessage("Congratulations! You've unlocked '<col=964F03>of the Hefin</col>' Loyalty Title.");
			player.getDialogueManager().startDialogue("LightCreature");
			player.sendMessage("You've completed the Agility course; laps ran: " + Colors.RED + Utils.getFormattedNumber(player.getLapsRan()) + "</col>.", true);
			player.getAchievements().updateProgress(1, AchievementList.RUN_100_TOTAL_AGILITY_LAPS, AchievementList.RUN_250_AGILITY_LAPS, AchievementList.RUN_1000_AGIL_LAPS, AchievementList.RUN_2500_AGIL_LAPS);
		}
		player.getSkills().addXp(Skills.AGILITY, exp);
		player.setNextAnimation(new Animation(-1));
		player.unlock();
	}

	/**
	 * Getting to the fishing platform - obstacles.
	 */
	public static void jumpToOutcrop(final Player player, WorldObject object) {
		if (!canTraverse(player))
			return;
		player.lock();
		player.faceObject(object);
		player.setNextAnimation(new Animation(27447));

		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				handleTraverse(player, new WorldTile(2268, 3400, 1));
			}
		}, 2);
	}

	public static void jumpToBridge(final Player player, WorldObject object) {
		if (!canTraverse(player))
			return;
		if (player.getX() != 2268 && player.getY() != 3400) {
			player.sendMessage("You can't reach that.", true);
			return;
		}
		player.lock();
		player.faceObject(object);
		player.setNextAnimation(new Animation(27447));

		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				handleTraverse(player, new WorldTile(2266, 3402, 1));
			}
		}, 2);
	}

	public static void climbFirstHandholds(final Player player, WorldObject object) {
		if (!canTraverse(player))
			return;
		boolean up = player.getX() == 2269 && player.getY() == 3396;
		boolean down = player.getX() == 2271 && player.getY() == 3396;
		if (!up && !down) {
			player.sendMessage("You can't reach that.", true);
			return;
		}
		player.lock();
		player.faceObject(object);
		player.setNextAnimation(new Animation(up ? 27449 : 27450));

		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				handleTraverse(player, new WorldTile((up ? 2271 : 2269), 3396, 1));
			}
		}, up ? 4 : 2);
	}

	public static void climbSecondHandholds(final Player player, WorldObject object) {
		if (!canTraverse(player))
			return;
		boolean up = player.getX() == 2274 && player.getY() == 3395;
		boolean down = player.getX() == 2276 && player.getY() == 3395;
		if (!up && !down) {
			player.sendMessage("You can't reach that.", true);
			return;
		}
		player.lock();
		player.faceObject(object);
		player.setNextAnimation(new Animation(up ? 27451 : 27452));

		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				handleTraverse(player, new WorldTile((up ? 2276 : 2274), 3395, 1));
			}
		}, up ? 6 : 2);
	}

	public static void climbThirdHandholds(final Player player, WorldObject object) {
		if (!canTraverse(player))
			return;
		boolean up = player.getX() == 2281 && player.getY() == 3394;
		boolean down = player.getX() == 2285 && player.getY() == 3394;
		if (!up && !down) {
			player.sendMessage("You can't reach that.", true);
			return;
		}
		player.lock();
		player.faceObject(object);
		player.setNextAnimation(new Animation(up ? 3381 : 3382));

		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				handleTraverse(player, new WorldTile((up ? 2285 : 2281), 3394, 1));
			}
		}, up ? 5 : 4);
	}

	public static void grappleCliffFace(final Player player, WorldObject object) {
		if (!canTraverse(player))
			return;
		if (player.getX() != 2287 && player.getY() != 3396) {
			player.sendMessage("You can't reach that.", true);
			return;
		}
		player.lock();
		player.setNextAnimation(new Animation(27454));
		player.setNextFaceWorldTile(new WorldTile(2287, 3397, 1));

		WorldTasksManager.schedule(new WorldTask() {

			int count = 0;

			@Override
			public void run() {
				if (count == 1)
					player.setNextAnimation(new Animation(27455));
				else if (count == 5) {
					handleTraverse(player, new WorldTile(2288, 3398, 2));
					stop();
				}
				count++;
			}
		}, 0, 1);
	}

	public static void descendCliff(final Player player, WorldObject object) {
		if (!canTraverse(player))
			return;
		if (player.getX() != 2287 && player.getY() != 3398) {
			player.sendMessage("You can't reach that.", true);
			return;
		}
		player.lock();
		player.setNextAnimation(new Animation(27457));
		player.setNextFaceWorldTile(new WorldTile(2287, 3397, 2));

		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				handleTraverse(player, new WorldTile(2288, 3396, 1));
			}
		}, 3);
	}

	public static void climbFourthHandholds(final Player player, WorldObject object) {
		if (!canTraverse(player))
			return;
		boolean up = player.getX() == 2289 && player.getY() == 3402;
		boolean down = player.getX() == 2289 && player.getY() == 3404;
		if (!up && !down) {
			player.sendMessage("You can't reach that.", true);
			return;
		}
		player.lock();
		player.faceObject(object);
		player.setNextAnimation(new Animation(up ? 27449 : 27450));

		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				handleTraverse(player, new WorldTile(2289, (up ? 3404 : 3402), 2));
			}
		}, up ? 4 : 2);
	}

	/**
	 * Checks if the player can start traversing to Waterfall fishing area.
	 *
	 * @param player
	 *            the Player to check.
	 * @return if can traverse.
	 */
	private static boolean canTraverse(Player player) {
		if (player.getSkills().getLevel(Skills.AGILITY) >= 90 && player.getSkills().getLevel(Skills.STRENGTH) >= 90
				&& player.getSkills().getLevel(Skills.RANGE) >= 90)
			return true;
		player.sendMessage("You need to have level 90 in Agility, Strength and Ranged to do this.");
		return false;
	}

	/**
	 * Handles the actual object traversing.
	 *
	 * @param player
	 *            The player traversing.
	 * @param tile
	 *            the WorldTile to go to.
	 */
	private static void handleTraverse(Player player, WorldTile tile) {
		player.getSkills().addXp(Skills.AGILITY, 1);
		player.setNextAnimation(new Animation(-1));
		player.setNextWorldTile(tile);
		player.unlock();
	}
}