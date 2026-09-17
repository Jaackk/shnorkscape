package com.rs.game.player.content.agility;

import com.rs.game.Animation;
import com.rs.game.ForceMovement;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.Pots;
import com.rs.game.player.content.achievementsystem.AchievementList;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.concurrent.ThreadLocalRandom;

public class BurthopeAgility {

	public static void walkLog(final Player player) {
		if (player.getX() != 2919 || player.getY() != (player.getY() >= 3558 ? 3558 : 3552))
			return;
		final boolean running = player.getRun();
		player.lock();
		player.setRunHidden(false);
		player.setNextAnimation(new Animation(15811 ));
		//player.addWalkSteps(2919, player.getY() < 3558 ? 3558 : 3552, -1, false);
		player.setNextForceMovement(new ForceMovement(new WorldTile(2919, player.getY() < 3558 ? 3558 : 3552,0), 5 , ForceMovement.NORTH));
		player.sendMessage("You walk across the log beam...");
		WorldTasksManager.schedule(new WorldTask() {
			boolean secondloop;
			@Override
			public void run() {
				if (!secondloop) {
					secondloop = true;
					player.getAppearence().setRenderEmote(155);
				} else {
					player.getAppearence().setRenderEmote(-1);
					player.setRunHidden(running);
					setBurthorpeStage(player, 0);
					player.getSkills().addXp(Skills.AGILITY, 5);
					player.sendMessage("... and make it safely to the other side.");
					player.unlock();
					stop();
				}
			}
		}, 0, 5);
	}

	public static void climbWall(final Player player) {
		if (player.getY() != 3561)
			player.addWalkSteps(player.getX(), 3561);
		player.lock();
		player.sendMessage("You climb the wall.");
		player.setNextFaceWorldTile( new WorldTile(player.getX(), 3562, 0));
		player.setNextAnimation(new Animation(15765));
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				if (getBurthorpeStage(player) == 0)
					setBurthorpeStage(player, 1);
				player.setNextAnimation(new Animation(-1));
				player.setNextWorldTile( new WorldTile(player.getX(), 3562, 1));
				player.getSkills().addXp(Skills.AGILITY, 2);
				player.unlock();
			}
		}, 7);
	}

	public static void walkAcrossBalancingLedge(final Player player, final WorldObject object) {
		if (player.getY() != 3564 || player.getX() < 2916) {
			player.sendMessage("You cannot walk across from here!");
			return;
		}
		player.sendMessage("You put your foot on the ledge and try to edge across...");
		player.lock();
		player.setNextAnimation(new Animation(752));
		player.getAppearence().setRenderEmote(156);
		final WorldTile toTile = new WorldTile((player.getX() >= 2916 ? 2912 : 2916), object.getY(), object.getPlane());
		player.setNextForceMovement(new ForceMovement(new WorldTile((player.getX() >= 2916 ? 2912 : 2916), object.getY(), object.getPlane()), 4, ForceMovement.WEST));
		player.setRun(true);
		player.addWalkSteps(toTile.getX(), toTile.getY(), -1, false);
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				player.setNextAnimation(new Animation(758));
				player.getAppearence().setRenderEmote(-1);
				player.getSkills().addXp(Skills.AGILITY, 5);
				player.sendMessage("You skillfully edge across the gap.");
				player.setNextWorldTile(new WorldTile((player.getX() >= 2916 ? 2912 : 2916), object.getY(), object.getPlane()));
				player.unlock();
				if (getBurthorpeStage(player) == 1)
					setBurthorpeStage(player, 2);
			}
		}, 3);
	}
	
	public static void climbOverObstacleWall(final Player player, WorldObject object) {
		if (player.getX() >= object.getX()) {
			player.getPackets().sendGameMessage("You cannot climb that from this side.");
			return;
		}
		if (player.getY() == 3562) {
			player.getPackets().sendGameMessage("You climb the low wall...");
			player.lock();
			player.setNextAnimation(new Animation(15782 ));
			final WorldTile toTile = new WorldTile(object.getX() + 1, 3562, object.getPlane());
			player.setNextForceMovement(new ForceMovement(player, 0, toTile, 2, ForceMovement.EAST));
			WorldTasksManager.schedule(new WorldTask() {
	
				@Override
				public void run() {
					player.setNextWorldTile(toTile);
					player.getSkills().addXp(Skills.AGILITY, 3);
					player.unlock();
					if (getBurthorpeStage(player) == 2)
						setBurthorpeStage(player, 3);
				}
	
			}, 1);
		} else
			player.addWalkSteps(2910, 3562);
	}
	
	public static void swingOnRopeSwing(final Player player, WorldObject object) {
		if (player.getX() > object.getX()) {
			player.getPackets().sendGameMessage("You cannot swing on that from this side.");
			return;
		}
		player.lock();
		player.setNextAnimation(new Animation(751));
		World.sendObjectAnimation(player, object, new Animation(497));
		final WorldTile toTile = new WorldTile(2916, 3562, object.getPlane());
		player.setNextForceMovement(new ForceMovement(player, 1, toTile, 2, ForceMovement.EAST));
		player.sendMessage("You skillfully swing on the rope.");
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				player.setNextWorldTile(toTile);
				player.getSkills().addXp(Skills.AGILITY, 10);
				player.unlock();
				if (getBurthorpeStage(player) == 3)
					setBurthorpeStage(player, 4);
			}

		}, 1);
	}
	
	public static void swingAcrossMonkeyBars(final Player player, WorldObject object) {
		if (player.getY() <= 3554) {
			player.sendMessage("You cannot swing across those from this side.");
			return;
		}
		if (player.getX() == 2917) {
			player.lock();
			final boolean running = player.getRun();
			player.setRunHidden(false);
			player.setNextAnimation(new Animation(16069));
			player.addWalkSteps(2917, 3554, -1, false);
			player.sendMessage("You swing across the monkey bars...");
			WorldTasksManager.schedule(new WorldTask() {
				boolean secondloop;
				@Override
				public void run() {
					if (!secondloop) {
						secondloop = true;
						player.getAppearence().setRenderEmote(662);
					} else {
						player.getAppearence().setRenderEmote(-1);
						player.setRunHidden(running);
						player.getSkills().addXp(Skills.AGILITY, 20);
						if (getBurthorpeStage(player) == 4)
							setBurthorpeStage(player, 5);
						player.sendMessage("... and make it safely to the other side.");
						player.setNextAnimation(new Animation(16070));
						player.unlock();
						stop();
					}
				}
			}, 0, 6);
		} else
			player.addWalkSteps(2917, 3561);
	}
	
	public static void jumpDownLedge(final Player player, WorldObject object) {
		if (player.getY() <= 3552) {
			player.sendMessage("You cannot jump down that ledge from this side.");
			return;
		}
		if (player.getY() >= 3553) {
			player.sendMessage("You jump down...");
			player.setNextAnimation(new Animation(7269));
			final WorldTile toTile = new WorldTile(player.getX(), 3552, 0);
			player.setNextForceMovement(new ForceMovement(player, 0, new WorldTile(toTile.getX(), toTile.getY() - 1, 1), 1, ForceMovement.SOUTH));
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					player.setNextWorldTile(toTile);
					player.sendMessage("...to the ground below.");
					if (getBurthorpeStage(player) == 5) {
						player.getAchievements().updateProgress(1, AchievementList.RUN_100_TOTAL_AGILITY_LAPS, AchievementList.RUN_250_AGILITY_LAPS, AchievementList.RUN_1000_AGIL_LAPS, AchievementList.RUN_2500_AGIL_LAPS);
						player.addLapsRan();
						player.sendMessage("You have completed a lap at the Burthorpe Agility Course!");
					}
					if (player.jujuPotions.isActive(Pots.Effects.PERFECT_AGILITY_JUJU) && ThreadLocalRandom.current().nextInt(4) == 0) {
						player.addItem(new Item(39922, Utils.rand(1, 3)));
						player.sendMessage("Through the power of juju you receive some skilling tickets!");
					}
					player.getSkills().addXp(Skills.AGILITY, 20);
					setBurthorpeStage(player, 0);
				}
			}, 0);
		}
	}

	public static void removeBurthorpeStage(Player player) {
		player.getTemporaryAttributtes().remove("BurthorpeCourse");
	}

	public static void setBurthorpeStage(Player player, int stage) {
		player.getTemporaryAttributtes().put("BurthorpeCourse", stage);
	}

	public static int getBurthorpeStage(Player player) {
		Integer stage = (Integer) player.getTemporaryAttributtes().get("BurthorpeCourse");
		if (stage == null)
			return -1;
		return stage;
	}
}