package com.rs.game.player.controllers;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceMovement;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activites.BountyHunter;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.thieving.Thieving;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class Wilderness extends Controller {

	public static WorldTile DYNAMIC_TILE;
	public static int DYNAMIC_RADIUS;

	private boolean showingSkull;

	public static void checkBoosts(Player player) {
		boolean changed = false;
		int level = player.getSkills().getLevelForXp(Skills.ATTACK);
		int maxLevel = (int) (level + 5 + (level * 0.15));
		if (maxLevel < player.getSkills().getLevel(Skills.ATTACK)) {
			player.getSkills().set(Skills.ATTACK, maxLevel);
			changed = true;
		}
		level = player.getSkills().getLevelForXp(Skills.STRENGTH);
		maxLevel = (int) (level + 5 + (level * 0.15));
		if (maxLevel < player.getSkills().getLevel(Skills.STRENGTH)) {
			player.getSkills().set(Skills.STRENGTH, maxLevel);
			changed = true;
		}
		level = player.getSkills().getLevelForXp(Skills.DEFENCE);
		maxLevel = (int) (level + 5 + (level * 0.15));
		if (maxLevel < player.getSkills().getLevel(Skills.DEFENCE)) {
			player.getSkills().set(Skills.DEFENCE, maxLevel);
			changed = true;
		}
		level = player.getSkills().getLevelForXp(Skills.RANGE);
		maxLevel = (int) (level + 5 + (level * 0.1));
		if (maxLevel < player.getSkills().getLevel(Skills.RANGE)) {
			player.getSkills().set(Skills.RANGE, maxLevel);
			changed = true;
		}
		level = player.getSkills().getLevelForXp(Skills.MAGIC);
		maxLevel = level + 5;
		if (maxLevel < player.getSkills().getLevel(Skills.MAGIC)) {
			player.getSkills().set(Skills.MAGIC, maxLevel);
			changed = true;
		}
		if (changed) {
			player.sendMessage("Your extreme potion bonuses have been reduced.");
			player.setPrayerRenewalDelay(0);
			player.getAppearence().generateAppearenceData();
		}

	}

	public static final boolean isAtDynamicPvP(final WorldTile tile) {
		if (DYNAMIC_TILE == null || tile == null)
			return false;
		return tile.withinArea(DYNAMIC_TILE.getX() - DYNAMIC_RADIUS, DYNAMIC_TILE.getY() - DYNAMIC_RADIUS, DYNAMIC_TILE.getX() + DYNAMIC_RADIUS, DYNAMIC_TILE.getY() + DYNAMIC_RADIUS);
	}

	public static boolean isDitch(int id) {
		return id >= 1440 && id <= 1444 || id >= 65076 && id <= 65087;
	}

	public static final boolean isAtWildBH(WorldTile tile) {
		return tile != null
				&& (tile.getX() >= 3011 && tile.getX() <= 3132 && tile.getY() >= 10052 && tile.getY() <= 10175)
				|| (tile.getX() >= 2940 && tile.getX() <= 3395 && tile.getY() >= 3525 && tile.getY() <= 4000)
				|| (tile.getX() >= 3264 && tile.getX() <= 3279 && tile.getY() >= 3279 && tile.getY() <= 3672)
				|| (tile.getX() >= 2756 && tile.getX() <= 2875 && tile.getY() >= 5512 && tile.getY() <= 5627)
				|| (tile.getX() >= 3158 && tile.getX() <= 3181 && tile.getY() >= 3679 && tile.getY() <= 3697)
				|| (tile.getX() >= 3280 && tile.getX() <= 3183 && tile.getY() >= 3885 && tile.getY() <= 3888)
				|| (tile.getX() >= 3012 && tile.getX() <= 3059 && tile.getY() >= 10303 && tile.getY() <= 10351);
	}

	public static final boolean isAtWild(WorldTile tile) {
		return (tile.getX() >= 3011 && tile.getX() <= 3132 && tile.getY() >= 10052 && tile.getY() <= 10175) // forinthry
				// dungeon
				|| (tile.getX() >= 2940 && tile.getX() <= 3395 && tile.getY() >= 3525 && tile.getY() <= 4000)
				|| (tile.getX() >= 3264 && tile.getX() <= 3279 && tile.getY() >= 3279 && tile.getY() <= 3672)
				|| (tile.getX() >= 2756 && tile.getX() <= 2875 && tile.getY() >= 5512 && tile.getY() <= 5627)
				|| (tile.getX() >= 3158 && tile.getX() <= 3181 && tile.getY() >= 3679 && tile.getY() <= 3697)
				|| (tile.getX() >= 3280 && tile.getX() <= 3183 && tile.getY() >= 3885 && tile.getY() <= 3888)
				|| (tile.getX() >= 3012 && tile.getX() <= 3059 && tile.getY() >= 10303 && tile.getY() <= 10351);
	}

	public static final boolean isAtDitch(WorldTile tile) {
		if (tile.getRegionId() == 10057)
			return true;
		return (tile.getX() >= 2940 && tile.getX() <= 3395 && tile.getY() <= 3524 && tile.getY() >= 3523
				|| tile.getX() >= 3084 && tile.getX() <= 3092 && tile.getY() >= 3929 && tile.getY() <= 3937);
	}

	@Override
	public void start() {
		checkBoosts(player);
		player.getBountyHunter().sendInterface();
	}

	public void process() {
		player.getBountyHunter().process();
	}

	@Override
	public boolean login() {
		moved();
		return false;
	}

	@Override
	public boolean keepCombating(boolean mainHand, Entity target) {
		if (target instanceof NPC)
			return true;
		if (!processPlayerOption1(target))
			return false;
		if (target.getAttackedBy() != player && player.getAttackedBy() != target)
			player.setWildernessSkull();
		if (player.getCombatDefinitions().getSpellId(!mainHand) <= 0
				&& Utils.inCircle(new WorldTile(3105, 3933, 0), target, 24)) {
			player.getPackets().sendGameMessage("You can only use magic in the arena.");
			return false;
		}
		return true;
	}

	@Override
	public boolean processPlayerOption1(Entity target) {
		if (target instanceof Player) {
			Player p2 = (Player) target;

			if (isAtWild(player) || isAtWild(p2))
				return false;

			if (player.isCanPvp() && (!isAtWild(p2) || !p2.isCanPvp())) {
				player.sendMessage("That player is not in the wilderness.");
				return false;
			}

			return canHit(target);
		}
		return true;
	}

	@Override
	public boolean canHit(Entity target) {
		if (target instanceof NPC)
			return true;
		Player p2 = (Player) target;
		if (Math.abs(player.getSkills().getCombatLevel() - p2.getSkills().getCombatLevel()) > getWildLevel(player))
			return false;
		return !player.isFrozen() || player.withinDistance(target, 1);
	}

	@Override
	public boolean processMagicTeleport(WorldTile toTile) {
		if (getWildLevel(player) > 20) {
			player.sendMessage("A mysterious force prevents you from teleporting.");
			player.getInterfaceManager().closeChatBoxInterface();
			return false;
		}
		if (player.getTeleBlockDelay() > Utils.currentTimeMillis()) {
			player.sendMessage("A mysterious force prevents you from teleporting.");
			player.getInterfaceManager().closeChatBoxInterface();
			return false;
		}
		return true;
	}

	@Override
	public boolean processItemTeleport(WorldTile toTile) {
		if (getWildLevel(player) > 20) {
			player.sendMessage("A mysterious force prevents you from teleporting.");
			player.getInterfaceManager().closeChatBoxInterface();
			return false;
		}
		if (player.getTeleBlockDelay() > Utils.currentTimeMillis()) {
			player.sendMessage("A mysterious force prevents you from teleporting.");
			player.getInterfaceManager().closeChatBoxInterface();
			return false;
		}
		return true;
	}

	@Override
	public boolean processObjectTeleport(WorldTile toTile) {
		if (player.getTeleBlockDelay() > Utils.currentTimeMillis()) {
			player.sendMessage("A mysterious force prevents you from teleporting.");
			player.getInterfaceManager().closeChatBoxInterface();
			return false;
		}
		return true;
	}

	public void showSkull() {
		player.getBountyHunter().sendInterface();
		player.getInterfaceManager().sendMinigameHudInterface(381);
	}

	@Override
	public boolean processObjectClick1(final WorldObject object) {
		if (isDitch(object.getId())) {
			player.lock();
			player.setNextAnimation(new Animation(6132));
			final WorldTile toTile = new WorldTile(
					object.getRotation() == 1 || object.getRotation() == 3 ? object.getX() + 2 : player.getX(),
					object.getRotation() == 0 || object.getRotation() == 2 ? object.getY() - 1 : player.getY(),
					object.getPlane());

			player.setNextForceMovement(new ForceMovement(new WorldTile(player), 1, toTile, 2,
					object.getRotation() == 0 || object.getRotation() == 2 ? ForceMovement.SOUTH : ForceMovement.EAST));
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					player.setNextWorldTile(toTile);
					player.faceObject(object);
					removeIcon(true, true);
					removeControler();
					player.getBountyHunter().removeInterface();
					player.resetReceivedDamage();
					player.unlock();
				}
			}, 1);
			return false;
		} else if (object.getId() == 2557 || object.getId() == 65717) {
			player.sendMessage("It seems it is locked, maybe you should try something else.");
			return false;
		}
		if (object.getId() == 65386 && object.getY() != 3896) {
			if (player.getY() >= 3904) {
				player.setNextWorldTile(new WorldTile(object.getX(), object.getY() - 1, 0));
			} else {
				player.setNextWorldTile(new WorldTile(object.getX(), object.getY() + 1, 0));
			}
			return false;
		}
		if (object.getId() == 65386 && object.getY() == 3896) {
			if (player.getY() >= 3896) {
				player.setNextWorldTile(new WorldTile(object.getX(), object.getY() - 1, 0));
			} else {
				player.setNextWorldTile(new WorldTile(object.getX(), object.getY() + 1, 0));
			}
			return false;
		}
		return true;
	}

	@Override
	public boolean processObjectClick2(final WorldObject object) {
		if (object.getId() == 2557 || object.getId() == 65717) {
			Thieving.pickDoor(player, object);
			return false;
		}
		return true;
	}

	@Override
	public void sendInterfaces() {
		// if (isAtWild(player));
		showSkull();
	}

	@Override
	public boolean sendDeath() {
		if (player.isGroupIronman()) {
			player.gimTracker.incrementDeaths("<#player> died while in the Wilderness");
		}

		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					player.setNextAnimation(new Animation(836));
				} else if (loop == 1) {
					player.sendMessage("Oh dear, you have died.");
				} else if (loop == 3) {
					Player killer = player.getMostDamageReceivedSourcePlayer();
					if (killer != null) {
						killer.removeDamage(player);
						killer.increaseKillCount(player);
						killer.addKill(player, false);
						killer.getBountyHunter().kill(player);
						killer.getBountyHunter().setTarget(null);
						player.getBountyHunter().setTarget(null);
					}
					player.killStreak = 0;
					player.getDeathManager().handleDeathInPvP(killer);
					//player.sendItemsOnDeath(killer, true);
					player.getEquipment().init();
					player.getInventory().init();
					player.reset();
					removeIcon(true, true);
					removeControler();
					player.setNextWorldTile(player.getHomeTile());
					player.setNextAnimation(new Animation(-1));
				} else if (loop == 4) {
					stop();
				}
				loop++;
			}
		}, 0, 1);
		return false;
	}

	@Override
	public void moved() {
		final boolean isAtWild = isAtWild(player);
		final boolean isAtWildSafe = isAtWildSafe();
		if (!showingSkull && isAtWild && !isAtWildSafe) {
			showingSkull = true;
			player.setCanPvp(true);
			showSkull();
			player.getAppearence().generateAppearenceData();
		} else if (showingSkull && (isAtWildSafe || !isAtWild)) {
			removeIcon(false, false);
		} else if (!isAtWildSafe && !isAtWild) {
			removeIcon(true, false);
			removeControler();
			player.getBountyHunter().removeTarget(true);
		} else if (Kalaboss.isAtKalaboss(player)) {
			removeIcon(true, false);
			player.getControlerManager().startControler("Kalaboss");
			removeControler();
			player.getBountyHunter().removeTarget(true);
		}
		if (!player.getBountyHunter().processWithMovement())
			return;
	}

	public void removeIcon(boolean removeControler, boolean dead) {
		player.getBountyHunter().decreaseRunAwayTimer();
		if (!dead && player.getBountyHunter().getRunAwayTimer() != BountyHunter.READY_TO_START) {
			player.getBountyHunter().updateCombatTimer(true);
			return;
		}
		if (removeControler) {
			player.getBountyHunter().removeInterface();
			player.getInterfaceManager().removeMinigameHudInterface();
			removeControler();
		}
		if (showingSkull) {
			showingSkull = false;
			player.setCanPvp(false);
            player.getInterfaceManager().removeMinigameHudInterface();
			player.getAppearence().generateAppearenceData();
			player.getEquipment().refresh(null);
		}
	}

	public boolean logout() {
		return false; // so doesnt remove script
	}

	@Override
	public void forceClose() {
		removeIcon(true, true);
	}

	public boolean isAtWildSafe() {
		return (player.getX() >= 2940 && player.getX() <= 3395 && player.getY() <= 3524 && player.getY() >= 3523);
	}

	public static final int getWildLevel(Player player) {
		if (player.getY() > 9900)
			return (player.getY() - 9912) / 8 + 1;
		return (player.getY() - 3520) / 8 + 1;
	}
}