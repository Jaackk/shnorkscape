package com.rs.game.player.content.agility;

import com.rs.cores.CoresManager;
import com.rs.game.Animation;
import com.rs.game.ForceMovement;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

import java.util.concurrent.TimeUnit;

/**
 * A class containing the management of the {@link AgilityPyramid}.
 * 
 * @author _Waterfiend <skype:alco-wahidi>
 *         Created in Apr 29, 2017 at 2:31:35 PM.
 */
public class AgilityPyramid {

	/**
	 * Verifies the level of the {@link Player} to be able to do the pyramid.
	 * @param player  The player.
	 * @param objectId The id of the object.
	 * @return the level
	 */
	private static final boolean verifyLevel(Player player, int objectId) {
		if (getLevelRequired(objectId) > player.getSkills().getLevel(Skills.AGILITY)) {
			player.sendMessage("You require a level of " + getLevelRequired(objectId) + " agility to do this.");
			return true;
		}
		return false;
	}

	/**
	 * Gets the level required (50) for the {@link AgilityPyramid}
	 * @param objectId The id of the object
	 * @return the level
	 */
	private static final int getLevelRequired(int objectId) {
		switch (objectId) {
		case 10865: // Wall object
		case 10857: // pyramid stairs
		case 10868:// the wooden plank to pass via
		case 10863: // gap object (jump)
			return 50;
		}
		return -1;
	}

	/**
	 * Fetches the experience earned by the obstacles.
	 * @param objectId The id of the object.
	 * @return the objectId
	 */
	private static final int fetchExp(int objectId) {
		switch (objectId) {
		case 10860: // ledge plank objects
		case 10868:
		case 10886:
		case 10888:
			return 300;
		case 10861:
		case 10863:
		case 10864:
		case 10865:
		case 10869:
			return 150;
		}
		return -1;
	}

	/**
	 * The hot-spot.
	 * @param player The player.
	 * @param hotX The X hot-spot.
	 * @param hotY The Y hot-spot.
	 * @return the hotSpot
	 */
	private static final boolean hotSpot(Player player, int hotX, int hotY) {
		return player.getX() == hotX && player.getY() == hotY;
	}

	/**
	 * Handles the objects in the {@link AgilityPyramid}.
	 * @param player The player to cross the objects.
	 * @param object The objects to be crossed.
	 * @return the pyramidObjects
	 */
	public static final boolean handlePyramidObjects(final Player player, final WorldObject object) {
		final int objectId = object.getId();
		switch (objectId) {
		case 10858:
		case 10867:
			player.sendMessage("You can't reach that.");
			break;
		case 10857:
			if (verifyLevel(player, objectId))
				return false;
			if (object.getX() == 3360 && object.getY() == 2837)
				player.useStairs(-1, new WorldTile(3041, 4695, 2), 0, 0);
			else if (object.getX() == 3042 && object.getY() == 4695)
				player.useStairs(-1, new WorldTile(3043, 4697, 3), 0, 0);
			else if (player.getPlane() == 0) {
				player.pyramidReward = false;
				player.useStairs(-1, new WorldTile(player.getX(), player.getY() + 3, 1), 0, 0);
			} else
				player.useStairs(-1, new WorldTile(player.getX(), player.getY() + 3, player.getPlane() + 1), 0, 0);
			break;
		case 10855:
			player.sendMessage("You journey back to the bottom of the pyramid.", true);
			player.getSkills().addXp(Skills.AGILITY, 700);
			player.getContracts().recordAction(Skills.AGILITY, 1);
			player.setNextWorldTile(new WorldTile(3364, 2830, 0));
			break;
		case 10851:
			player.setNextFaceWorldTile(new WorldTile(3044, player.getY(), 3));
			if (!player.pyramidReward) {
				player.setNextAnimation(new Animation(2146));
				player.getInventory().addItem(995, 10000);
				player.sendMessage("You receive a reward for reaching the top of the pyramid.", true);
				player.pyramidReward = true;
			} else
				player.sendMessage("You have already claimed your reward, complete the pyramid again.", true);
			break;
		case 10860:
		case 10888:
		case 10886:
			if (verifyLevel(player, objectId))
				return false;
			if (hotSpot(player, 3363, 2851) || hotSpot(player, 3363, 2852)) {
				if (hotSpot(player, 3363, 2852))
					player.addWalkSteps(3363, 2851, 1, false);
				handleLedge(player, object, new WorldTile(player.getX() + 5, 2851, player.getPlane()), hotSpot(player, 3363, 2852));
				return true;
			} else if (hotSpot(player, 3372, 2841) || hotSpot(player, 3373, 2841)) {
				if (hotSpot(player, 3373, 2841))
					player.addWalkSteps(3372, 2841, 1, false);
				handleLedge(player, object, new WorldTile(3372, player.getY() - 5, player.getPlane()), hotSpot(player, 3373, 2841));
				return true;
			} else if (hotSpot(player, 3359, 2842) || hotSpot(player, 3358, 2842)) {
				if (hotSpot(player, 3358, 2842))
					player.addWalkSteps(3359, 2842, 1, false);
				handleLedge(player, object, new WorldTile(3359, player.getY() + 5, player.getPlane()), hotSpot(player, 3358, 2842));
				return true;
			} else if (hotSpot(player, 3364, 2832) || hotSpot(player, 3364, 2831)) {
				if (hotSpot(player, 3364, 2831))
					player.addWalkSteps(3364, 2832, 1, false);
				handleLedge(player, object, new WorldTile(player.getX() - 5, 2831, player.getPlane()), hotSpot(player, 3364, 2831));
				return true;
			}
			player.sendMessage("You cannot cross the ledge from this side.", true);
			break;

		case 10865:
			if (verifyLevel(player, objectId))
				return false;
			if (hotSpot(player, 3354, 2848) || hotSpot(player, 3355, 2848) || hotSpot(player, 3359, 2838) || hotSpot(player, 3358, 2838))
				handleWall(player, object, new WorldTile(player.getX(), player.getY() + 2, player.getPlane()), ForceMovement.NORTH);
			else if (hotSpot(player, 3371, 2834) || hotSpot(player, 3371, 2833))
				handleWall(player, object, new WorldTile(player.getX() - 2, player.getY(), player.getPlane()), ForceMovement.WEST);
			else if (hotSpot(player, 3041, 4701) || hotSpot(player, 3041, 4702))
				handleWall(player, object, new WorldTile(player.getX() + 2, player.getY(), player.getPlane()), ForceMovement.EAST);
			else if (hotSpot(player, 3048, 4694) || hotSpot(player, 3048, 4693))
				handleWall(player, object, new WorldTile(player.getX() - 2, player.getY(), player.getPlane()), ForceMovement.WEST);
			if (!player.isLocked())
				player.sendMessage("You cannot climb the rocks from this side.");
			break;
		case 10883:
			player.sendMessage("You can't reach that.");
			break;
		case 10882:
		case 10863:
		case 10884:
		case 10859:
		case 10861:
			if (verifyLevel(player, objectId))
				return false;
			if (hotSpot(player, 3357, 2836) || hotSpot(player, 3356, 2836))
				handleGap(player, object, new WorldTile(player.getX(), 2841, player.getPlane()), ForceMovement.NORTH);
			else if (hotSpot(player, 3046, 4699) || hotSpot(player, 3047, 4699))
				handleGap(player, object, new WorldTile(player.getX(), 4696, player.getPlane()), ForceMovement.SOUTH);
			else if (hotSpot(player, 3048, 4697) || hotSpot(player, 3049, 4697))
				handleGap(player, object, new WorldTile(player.getX(), 4694, player.getPlane()), ForceMovement.SOUTH);
			else if (hotSpot(player, 3041, 4696) || hotSpot(player, 3040, 4696))
				handleGap(player, object, new WorldTile(player.getX(), 4699, player.getPlane()), ForceMovement.NORTH);
			else if (hotSpot(player, 3357, 2846) || hotSpot(player, 3356, 2846))
				handleGap(player, object, new WorldTile(player.getX(), 2849, player.getPlane()), ForceMovement.NORTH);
			else if (hotSpot(player, 3359, 2849) || hotSpot(player, 3359, 2850))
				handleGap(player, object, new WorldTile(3364, player.getY(), player.getPlane()), ForceMovement.EAST);
			else if (hotSpot(player, 3372, 2832) || hotSpot(player, 3372, 2831))
				handleGap(player, object, new WorldTile(3367, player.getY(), player.getPlane()), ForceMovement.WEST);
			else if (hotSpot(player, 3366, 2834) || hotSpot(player, 3366, 2833))
				handleGap(player, object, new WorldTile(3363, player.getY(), player.getPlane()), ForceMovement.WEST);
			else if (hotSpot(player, 3370, 2843) || hotSpot(player, 3371, 2843))
				handleGap(player, object, new WorldTile(player.getX(), 2840, player.getPlane()), ForceMovement.SOUTH);
			if (!player.isLocked())
				player.sendMessage("You cannot cross the obstacle from this side.");
			break;
		case 10868:
			if (verifyLevel(player, objectId))
				return false;
			player.lock();
			if (player.getPlane() == 1) {
				if (hotSpot(player, 3374, 2845) || hotSpot(player, 3374, 2845) || hotSpot(player, 3375, 2846))
					player.addWalkSteps(3375, 2845, 2, false);
				handlePlank(player, object, new WorldTile(3375, 2840, player.getPlane()), (hotSpot(player, 3374, 2845) || hotSpot(player, 3374, 2845) || hotSpot(player, 3375, 2846)));
				return true;
			} else if (player.getPlane() == 3) {
				if (hotSpot(player, 3370, 2836) || hotSpot(player, 3371, 2836) || hotSpot(player, 3371, 2835))
					player.addWalkSteps(3370, 2835, 2, false);
				handlePlank(player, object, new WorldTile(3365, 2835, player.getPlane()), (hotSpot(player, 3370, 2836) || hotSpot(player, 3371, 2836) || hotSpot(player, 3371, 2835)));
				return true;
			}
			break;
		}
		return true;
	}

	private static final void handleLedge(Player player, WorldObject object, WorldTile destination, boolean delay) {
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				player.lock();
				player.setRun(true);
				player.addWalkSteps(destination.getX(), destination.getY(), 5, false);
				player.getAppearence().setRenderEmote(1427);
				player.sendMessage("You attempt to cross the ledge...", true);
				CoresManager.getServiceProvider().executeWithDelay(new Runnable() {

					@Override
					public void run() {
						player.getSkills().addXp(Skills.AGILITY, fetchExp(object.getId()));
						player.getAppearence().setRenderEmote(-1);
						player.sendMessage("... and make it safely to the other side.", true);
						player.unlock();
					}
					
				}, 1500, TimeUnit.MILLISECONDS);
			}
		}, delay ? 1 : 0);
	}

	private static final void handleWall(Player player, WorldObject object, WorldTile destination, int direction) {
		player.lock();
		player.setNextAnimation(new Animation(839));
		player.setNextFaceWorldTile(destination);
		player.sendMessage("You attempt to climb the wall...", true);
		player.setNextForceMovement(new ForceMovement(player, 0, destination, 2, direction));
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				player.unlock();
				player.getSkills().addXp(Skills.AGILITY, fetchExp(object.getId()));
				player.getAppearence().setRenderEmote(-1);
				player.sendMessage("... and make it safely to the other side.", true);
				player.setNextWorldTile(destination);
				stop();
			}
		}, 1);
	}

	private static final void handleGap(Player player, WorldObject object, WorldTile destination, int direction) {
		player.lock(2);
		player.setNextAnimation(new Animation(3067));
		player.setNextForceMovement(new ForceMovement(player, 0, destination, 2, direction));
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				player.setNextWorldTile(destination);
				player.sendMessage("You successfully made it to the other side!", true);
				stop();
			}

		}, 1);
	}

	private static final void handlePlank(Player player, WorldObject object, WorldTile destination, boolean delay) {
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				player.lock();
				player.setRunHidden(false);
				player.addWalkSteps(destination.getX(), destination.getY(), -1, false);
				player.getAppearence().setRenderEmote(1637);
				player.sendMessage("You attempt to cross the plank...", true);
				CoresManager.getServiceProvider().executeWithDelay(new Runnable() {
					@Override
					public void run() {
						player.getSkills().addXp(Skills.AGILITY, fetchExp(object.getId()));
						player.getAppearence().setRenderEmote(-1);
						player.setRunHidden(true);
						player.unlock();
						player.sendMessage("... and make it safely to the other side.", true);
					}
				}, 2800, TimeUnit.MILLISECONDS);
			}
		}, delay ? 1 : 0);
	}
}