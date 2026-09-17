package com.rs.game.player.content.ectofuntus;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.ObjectDefinitions;
import com.rs.game.Animation;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.SlimeBucket;
import com.rs.game.player.content.Burying;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

/**
 * A class handling the components of the {@link Ectofuntus} prayer training method.
 * 
 * @author _Waterfiend <skype:alco-wahidi>
 *
 * Created in Apr 29, 2017 at 10:45:55 AM.
 */
public class Ectofuntus {

	/**
	 * An array containing the items involved in the {@link Ectofuntus} activity.
	 * 1925 = bucket; 1931 = pot; slime_bucket = 4286; ecto_token = 4278, ectophial = 4251;
	 */
	@SuppressWarnings("unused")
	private static final int[][] ECTO_ITEMS = { { 1925, 1931, 4286, 4278, 4251} };
	
	/**
	 * Manages the worshipping process of the {@link Ectofuntus} activity.
	 * @param worshipper The player using bone-meals to worship the {@link Ectofuntus}.
	 */
	public static void manageWorship(Player worshipper, int itemId) {
		ItemDefinitions itemDefs = ItemDefinitions.getItemDefinitions(itemId);
		if (!itemDefs.getName().toLowerCase().contains("bonemeal") || (!worshipper.getInventory().containsItem(4286, 1))) {
			worshipper.sendMessage("You must have a bonemeal and bucket of slime in your inventory to worship the Ectofuntus.");
			return;
		}
		for (Item ectoItem : worshipper.getInventory().getItems().getItems()) {
			if (ectoItem == null)
				continue;
			Bonemeal meal = Bonemeal.fetchMealId(ectoItem.getId());
			if (meal != null) {
				Burying.Bone boneInfo = Burying.Bone.forId(meal.getBoneId());
				if (boneInfo == null) {
					worshipper.sendMessage("You cannot use this bone to worship the Ectofuntus.");
					return;
				}
				worshipper.setNextAnimation(new Animation(1651));
				worshipper.getInventory().deleteItem(meal.getMealId(), 1);
				worshipper.getInventory().addItem(1931, 1);
				worshipper.getInventory().deleteItem(4286, 1);
				worshipper.getInventory().addItem(1925, 1);
				worshipper.getSkills().addXp(Skills.PRAYER, boneInfo.getExperience() * 4);
				worshipper.tokensUnclaimed += 5;
				return;
			}
		}
	}
	
	/**
	 * Manages the item on object process.
	 * @param player The player to use the item.
	 * @param itemId The id of the item to use.
	 * @param objectId The id of the object.
	 * @return the option
	 */
	public static boolean manageIOB(Player player, int itemId, int objectId) {
		ObjectDefinitions objectDefs = ObjectDefinitions.getObjectDefinitions(objectId);
		ItemDefinitions itemDefs = ItemDefinitions.getItemDefinitions(itemId);
		if (itemId == 1925 && objectDefs.name.equals("Pool of Slime")) {
			player.getActionManager().setAction(new SlimeBucket());
			return true;
		}

		if (itemDefs.getName().toLowerCase().contains("bone") && objectId == 11162) {
			if (player.boneType != -1) {
				player.sm("You already have some bones in the hopper.");
				return true;
			}
			Bonemeal meal = Bonemeal.fetchBoneId(itemId);
			if (meal != null) {
				player.boneType = meal.getBoneId();
				player.sm("You put the bones in the hopper.");
				player.setNextAnimation(new Animation(1649));
				player.getInventory().deleteItem(meal.getBoneId(), 1);
			} else {
		         player.sm("Use your bones on the hopper to fill it.");
				player.boneType = -1;
			}
		}
		return false;
	}
	
	/**
	 * Manages the use of the {@link Ectofuntus} objects.
	 * @param player The player that is worshipping the {@link Ectofuntus}.
	 * @param objectId The object in the {@link Ectofuntus} to interact with.
	 * @return the object
	 */
	public static boolean manageObjects(Player player, int objectId) {	
		switch (objectId) {
		case 11162:
	         player.sm("Use your bones on the hopper to fill it.");
			return true;
		case 5268: {
			player.setNextAnimation(new Animation(828));
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					player.setNextWorldTile(new WorldTile(3669, 9888, 3));
				}
			}, 0);
		}
			return true;

		case 17119:
			if(!player.getInventory().containsItem(1925, 1)) {
				player.sendMessage("You must have an empty bucket in your inventory to fill it up.");
			} else	
		player.getActionManager().setAction(new SlimeBucket());
		return true;
		case 5264: {
			player.setNextAnimation(new Animation(828));
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					player.setNextWorldTile(new WorldTile(3654, 3519, 0));
				}
			}, 0);
		}
			return true;

		case 9308: {
			if (player.getSkills().getLevel(Skills.AGILITY) < 53) {
				player.sendMessage("You require an agility level of 53 to pass via this obstacle.");
				return true;
			}
			player.setNextAnimation(new Animation(828));
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					player.setNextWorldTile(new WorldTile(3671, 9888, 2));
				}
			}, 1);
		}
			return true;

		case 9307: {
			if (player.getSkills().getLevel(Skills.AGILITY) < 53) {
				player.sendMessage("You require an agility level of 53 to pass via this obstacle.");
				return true;
			}
			player.setNextAnimation(new Animation(828));
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					player.setNextWorldTile(new WorldTile(3670, 9888, 3));
				}
			}, 1);
		}
			return true;

		case 5263:
			if (player.getPlane() == 3)
				player.setNextWorldTile(new WorldTile(3688, 9888, 2));
			if (player.getPlane() == 2)
				player.setNextWorldTile(new WorldTile(3675, 9887, 1));
			if (player.getPlane() == 1)
				player.setNextWorldTile(new WorldTile(3683, 9888, 0));
			return true;

		case 5262:
			if (player.getPlane() == 2)
				player.setNextWorldTile(new WorldTile(3692, 9888, 3));
			if (player.getPlane() == 1)
				player.setNextWorldTile(new WorldTile(3671, 9888, 2));
			if (player.getPlane() == 0)
				player.setNextWorldTile(new WorldTile(3687, 9888, 1));
			return true;

		case 5282:
			manageWorship(player, objectId);
			return true;

		case 11163:
			if (player.boneType != -1 && !player.grindedBones) {
				player.sm("You turn the grinder, some crushed bones fall into the bin.");
				player.setNextAnimation(new Animation(1648));
				player.grindedBones = true;
			} else {
				player.setNextAnimation(new Animation(1648));
			}
			return true;

		case 11164:
			if (player.boneType == -1) {
				player.sm("You need to put some bones in the hopper and grind them first.");
				return true;
			}
			if (!player.grindedBones) {
				player.sm("You need to grind the bones by turning the grinder first.");
				return true;
			}
			if (!player.getInventory().containsItem(1931, 1)) {
				player.sm("You need an empty pot to fill with the crushed bones.");
				return true;
			}
			if (player.boneType != -1 && player.grindedBones) {
				Bonemeal meal = Bonemeal.fetchBoneId(player.boneType);
				if (meal != null) {
					player.sm("You fill an empty pot with bones.");
					player.setNextAnimation(new Animation(1650));
					player.getInventory().deleteItem(1931, 1);
					player.getInventory().addItem(meal.getMealId(), 1);
					player.boneType = -1;
					player.grindedBones = false;
				} else {
					player.boneType = -1;
				}
			}
			return true;
		}
		return false;
	}
}
