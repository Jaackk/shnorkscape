package com.rs.game.player.actions.divination;

import com.rs.Settings;
import com.rs.game.Animation;
import com.rs.game.Region;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

/**
 * Handles everything related to Divine Locations.
 *
 * @author Noel
 */
public class DivineObject {

	/**
	 * The Player placing the Divine location.
	 */
	protected static Player player;

	public static boolean canUseDivineObj(Player player, Player owner) {
		if (!player.isOwner()) {
			if (player != owner && (player.isATypeOfIronman() || player.isKingOfTheSkillGameMode())) {
				if (DivineObject.withinPlatinumZone(player) && player == owner)
					return true;
				if(!player.canGimInteractWith(owner)) {
					player.getDialogueManager().startDialogue("SimpleMessage",
							"You cannot use other player placed Divine locations.");
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Check if we can place another location.
	 *
	 * @param owner
	 *            The player placing.
	 */
	public static void check(final Player owner) {
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				if (owner.divine > 1)
					return;
				if (owner.divine == 0)
					stop();
				return;
			}
		}, 0, 0);
	}

	public static boolean withinPlatinumZone(Player player) {
        return player.getX() >= 3869 && player.getX() <= 3880 && player.getY() >= 6795 && player.getY() <= 6834
                && player.getPlane() == 0;
    }
	
	public static boolean withinPlatinumZone(WorldObject object) {
        return object.getX() >= 3869 && object.getX() <= 3880 &&
                object.getY() >= 6795 && object.getY() <= 6834;
    }

	/**
	 * Checks if the Divine location can be placed.
	 *
	 * @param plane
	 *            The plane coord.
	 * @param x
	 *            The X coord.
	 * @param y
	 *            The Y coord.
	 * @return if can be placed.
	 */
	private static boolean canPlaceLocation(Player owner, int plane, int x, int y) {
		if (!World.canMoveNPC(plane, x, y, 2) || World.getObjectWithSlot(owner, Region.OBJECT_SLOT_FLOOR) != null || owner.getControlerManager().getControler() != null)
			return false;
        return World.getObject(new WorldTile(x, y, plane)) == null;
    }

	/**
	 * Handles the actual Divine location object placement.
	 *
	 * @param owner
	 *            The player placing.
	 * @param item
	 *            The divine location item ID.
	 * @param fobject
	 *            the object spawn ID.
	 * @param lobject
	 *            the actual divine object.
	 * @param lvl
	 *            divination LVL required.
	 */
	public static void placeDivine(final Player owner, final int item, final int fobject, final int lobject, int lvl,
			int skillId) {
		if (owner.getSkills().getLevel(skillId) < lvl) {
			owner.sendMessage("You need a " + owner.getSkills().getSkillName(skillId) + " level of " + lvl
					+ " to place this Divine location.");
			return;
		}
		WorldTile tile = new WorldTile(owner.getX() + 1, owner.getY(), owner.getPlane());
		if (!canPlaceLocation(owner, tile.getPlane(), tile.getX(), tile.getY())) {
			tile = new WorldTile(owner.getX() - 1, tile.getY(), tile.getPlane());
			if (!canPlaceLocation(owner, tile.getPlane(), tile.getX(), tile.getY())) {
				tile = new WorldTile(owner.getX(), tile.getY() + 1, tile.getPlane());
				if (!canPlaceLocation(owner, tile.getPlane(), tile.getX(), tile.getY())) {
					tile = new WorldTile(tile.getX(), owner.getY() - 1, tile.getPlane());
					if (!canPlaceLocation(owner, tile.getPlane(), tile.getX(), tile.getY())) {
						owner.sendMessage("You cannot place a Divine location here; try moving around.");
						return;
					}
				}
			}
		}
		final WorldTile finalTile = tile;
		WorldTasksManager.schedule(new WorldTask() {
			int ticks;

			@Override
			public void run() {
				ticks++;
				if (ticks == 1) {
					owner.stopAll(true);
					owner.lock(5);
					owner.getInventory().deleteItem(item, 1);
					owner.setNextAnimation(new Animation(21217));
					WorldObject divinefirststage = new WorldObject(fobject, 10, 0, finalTile.getX(), finalTile.getY(),
							finalTile.getPlane(), owner);
					owner.faceObject(divinefirststage);
					World.spawnObject(divinefirststage);
				}
				if (ticks == 5) {
					check(owner);
					WorldObject divinefinalstage = new WorldObject(lobject, 10, 0, finalTile.getX(), finalTile.getY(),
							finalTile.getPlane(), owner);
					World.spawnObjectTemporary(divinefinalstage, 40000);
					owner.divine = lobject;
					owner.divines = owner;
					owner.unlock();
					player = owner;
					stop();
				}
				return;
			}
		}, 0, 0);
	}

	/**
	 * Resets Divination Divine location creation limit.
	 *
	 * @param player
	 *            The player's limit to reset.
	 */
	private static void resetDivineCreation(Player player, boolean check) {
		long timeVariation = Utils.currentTimeMillis() - player.lastCreationTime;
		if (check && timeVariation < (24 * 60 * 60 * 1000)) // 24 hours
			return;
		player.lastCreationTime = Utils.currentTimeMillis();
		if (player.created)
			player.sendMessage(Colors.DARK_RED + "Your Divination Divine location creation limit has been reset.");
		player.createdToday = 0;
		player.created = false;
	}

	/**
	 * Resets Divination Divine location gather limit.
	 *
	 * @param player
	 *            The player's limit to reset.
	 */
	public static void resetGatherLimit(Player player, boolean check) {
		resetDivineCreation(player, check);
		if (check && (Utils.currentTimeMillis() - player.lastGatherLimit) < (24 * 60 * 60 * 1000)) // 24																	// hours
			return;
		player.lastGatherLimit = Utils.currentTimeMillis();
		if (player.gathered > 0)
			player.sendMessage(Colors.DARK_RED + "Your Divination Divine location gather limit has been reset.");
		player.gathered = 0;
		player.gatherLimitMessage = 0;
		player.getVisWaxManager().setResetDivineLocation(false);
	}

	/**
	 * Checks how much more produce can we gather.
	 *
	 * @param player
	 *            The Player.
	 * @return amount left we can acquire.
	 */
	public static int checkPercentage(Player player) {
		int charges = player.gathered;
		int limitModifier = player.getPerkManager().hasPerkActive(DonationPerk.DIVINE_DOUBLER) ? 2 : 1;
		int maxCharges = (player.getSkills().getLevelForXp(Skills.DIVINATION) + 16) * limitModifier;
		int percentage = charges * 100 / maxCharges;
		if (Settings.DEBUG)
			Logger.getGlobal().info("Gather acquired: " + percentage + "%; " + charges + " out of " + maxCharges + ".");
		return percentage;
	}

	/**
	 * XP modifier by wearing items.
	 *
	 * @param player
	 *            The player.
	 * @return the XP modifier.
	 */
	public static double divinationSuit(Player player) {
		int pieces = 0;
		if (player.getEquipment().getHatId() == 29865 || player.getEquipment().getHatId() == 32279)
			pieces++;
		if (player.getEquipment().getChestId() == 29866)
			pieces++;
		if (player.getEquipment().getLegsId() == 29867)
			pieces++;
		if (player.getEquipment().getBootsId() == 29868)
			pieces++;
		if (player.getEquipment().getGlovesId() == 29869)
			pieces++;
		return 1.0 + (pieces * 0.01) + (pieces == 5 ? 0.01 : 0.0);
	}

	/**
	 * Checks if we can harvest more resources..
	 *
	 * @param player
	 *            The player harvesting.
	 * @return if the player can harvest or not.
	 */
	public static boolean canHarvest(Player player) {
		if (checkPercentage(player) >= 100) {
			player.sendMessage(Colors.DARK_RED + "You have already gathered your limit from Divine locations today.");
			player.getActionManager().forceStop();
			return false;
		}
		return true;
	}

	/**
	 * Handles the actual resource harvesting.
	 *
	 * @param player
	 *            The player harvesting.
	 */
	public static void handleHarvest(Player player) {
		player.gathered++;
		int percentage = checkPercentage(player);
		if (percentage == 25 && player.gatherLimitMessage == 0 || percentage == 50 && player.gatherLimitMessage == 1 || percentage == 75 && player.gatherLimitMessage == 2) {
			player.gatherLimitMessage++;
			player.sendMessage(Colors.DARK_RED + "You have used " + percentage + "% of your daily gathering limit for Divine locations.");
		}
	}
}
