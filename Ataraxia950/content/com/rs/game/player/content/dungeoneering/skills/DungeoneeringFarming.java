package com.rs.game.player.content.dungeoneering.skills;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.TemporaryAttributes.Key;
import com.rs.game.player.content.dungeoneering.RingOfKinship;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class DungeoneeringFarming {

	public enum Harvest {
		SALVE_NETTLES(1, 6.1, 17448),

		WILDERCRESS(10, 9.2, 17450),

		BLIGHTLEAF(20, 12.8, 17452),

		ROSEBLOOD(30, 17.4, 17454),

		BRYLL(40, 23.5, 17456),

		DUSKWEED(50, 31.6, 17458),

		SOULBELL(60, 42.2, 17460),

		ECTOGRASS(70, 55.8, 17462),

		RUNELEAF(80, 72.9, 17464),

		SPIRITBLOOM(90, 94, 17466);

		private final int lvl, product;
		private final double exp;

		Harvest(int lvl, double exp, int product) {
			this.lvl = lvl;
			this.exp = exp;
			this.product = product;
		}

		public int getLvl() {
			return lvl;
		}

		public double getExp() {
			return exp;
		}

		public int getProduct() {
			return product;
		}
	}

	public static int getHerbForLevel(int level) {
		for (int i = 10; i < Harvest.values().length; i++)
			if (Harvest.values()[i].lvl == level)
				return Harvest.values()[i].product;
		return 17448;
	}

	public static void initHarvest(final Player player, final Harvest harvest, final WorldObject object) {
		Integer harvestCount = (Integer) player.getTemporaryAttributtes().get(Key.HARVEST_COUNT);
		final String productName = ItemDefinitions.getItemDefinitions(harvest.product).getName().toLowerCase();

		if (player.getSkills().getLevel(Skills.FARMING) < harvest.lvl) {
			player.getPackets().sendGameMessage("You need a Farming level of " + harvest.lvl + " in order to pick " + productName + ".");
			return;
		}

		if (harvestCount == null)
			harvestCount = Utils.random(2, 5);
		if (Utils.random(100) > (player.getGorajanTrailblazer().getExperienceBoost() - 1) * 100)
			harvestCount--;
		if (harvestCount == 0) {
			player.getTemporaryAttributtes().remove(Key.HARVEST_COUNT);
			player.getPackets().sendGameMessage("You have depleted this resource.");
			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					World.spawnObject(new WorldObject(object.getId() + 1, object.getType(), object.getRotation(), object));
				}
			});
			return;
		}
		player.getTemporaryAttributtes().put(Key.HARVEST_COUNT, harvestCount);
		player.setNextAnimation(new Animation(3659));
		player.lock(2);
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				if (player.getInventory().addItemDrop(harvest.product, 1)) {
					player.getPackets().sendGameMessage("You pick a " + productName + ".");
					player.getSkills().addXp(Skills.FARMING, harvest.exp);
				}
				if (Utils.random(100) < player.getRingOfKinship().getBoost(RingOfKinship.GATHERER))
					if (player.getInventory().addItemDrop(harvest.product, 1))
						player.getPackets().sendGameMessage("You manage to grab another " + productName + ".");
			}
		}, 2);
	}
}
