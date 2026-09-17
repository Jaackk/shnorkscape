package com.rs.game.player.actions.crafting;

import com.rs.game.Animation;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.content.achievementsystem.AchievementList;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

import java.util.HashMap;

/**
 * @author Tom
 * @date April 9, 2017
 */

public class Looming extends Action {

	public enum Loom {

		MILESTONE_CAPE_10(new Item[] { new Item(1759) }, new Item(20754), 10, 10),

		MILESTONE_CAPE_20(new Item[] { new Item(1759, 2) }, new Item(20755), 20, 20),

		MILESTONE_CAPE_30(new Item[] { new Item(1759, 3) }, new Item(20756), 30, 30),

		MILESTONE_CAPE_40(new Item[] { new Item(1759, 4) }, new Item(20757), 40, 40),

		MILESTONE_CAPE_50(new Item[] { new Item(1759, 5) }, new Item(20758), 50, 50),

		MILESTONE_CAPE_60(new Item[] { new Item(1759, 6) }, new Item(20759), 60, 60),

		MILESTONE_CAPE_70(new Item[] { new Item(1759, 7) }, new Item(20760), 70, 70),

		MILESTONE_CAPE_80(new Item[] { new Item(1759, 8) }, new Item(20761), 80, 80),

		MILESTONE_CAPE_90(new Item[] { new Item(1759, 9) }, new Item(20762), 90, 90);

		private final double experience;
		private final int levelRequired;
		private final Item[] material;
		private final Item product;

		Loom(Item[] material, Item product, double experience, int levelRequired) {
			this.material = material;
			this.product = product;
			this.experience = experience;
			this.levelRequired = levelRequired;
		}

		public Item getProduct() {
			return product;
		}

		public double getExperience() {
			return experience;
		}

		public int getLevelRequired() {
			return levelRequired;
		}

		public Item[] getMaterial() {
			return material;
		}

		public static final HashMap<Integer, Loom> VALUES = new HashMap<Integer, Loom>();

		static {
			for (Loom v : Loom.values())
				VALUES.put(v.ordinal(), v);
		}

		public static Loom getBar(Player player) {
			for (Loom bar : Loom.values()) {
				for (Item item : bar.getMaterial())
					if (player.getInventory().containsItem(new Item(item.getId())))
						return bar;
			}
			return null;
		}
	}

	private final Loom loom;
	@SuppressWarnings("unused")
	private final int quantity;

	public Looming(Loom gem, int quantity) {
		this.loom = gem;
		this.quantity = quantity;
	}

	public boolean checkRequirements(Player player) {
		if (player.getInterfaceManager().containsScreenInter() || player.getInterfaceManager().containsInventoryInter()) {
			player.getPackets().sendGameMessage("Please finish what you're doing before doing this action.");
			return false;
		}
		if (!player.getInventory().containsItem(loom.getMaterial()[0])) {
			player.getPackets().sendGameMessage("You don't have any " + loom.getMaterial()[0].getName().toLowerCase() + " to loom.");
			return false;
		}
		for (int x = 0; x < player.getSkills().getXp().length; x++)
			if (player.getSkills().getLevel(x) < loom.getLevelRequired()) {
				player.getPackets().sendGameMessage("You don't have " + loom.getLevelRequired() + " in all the skills to make this milestone cape.");
				return false;
			}
		return true;
	}

	@Override
	public boolean start(Player player) {
		if (checkRequirements(player)) {
			setActionDelay(player, 1);
			player.setNextAnimation(new Animation(883));
			return true;
		}
		return false;
	}

	@Override
	public boolean process(Player player) {
		return checkRequirements(player);
	}

	@Override
	public int processWithDelay(Player player) {
		player.getInventory().deleteItem(loom.getMaterial()[0]);
		player.getInventory().addItem(loom.getProduct());
		player.getSkills().addXp(Skills.CRAFTING, loom.getExperience());
		player.addItemsMade();
		player.sendMessage("You loom the " + loom.getMaterial()[0].getName().toLowerCase() + "; items crafted: " + Colors.RED + Utils.getFormattedNumber(player.getItemsMade()) + "</col>.", true);
		if (loom == Loom.MILESTONE_CAPE_70)
			player.getAchievements().updateProgress(1, AchievementList.CRAFT_70_MILESTONE_CAPE);
		if (loom == Loom.MILESTONE_CAPE_90)
			player.getAchievements().updateProgress(1, AchievementList.CRAFT_90_MILESTONE_CAPE);
		player.setNextAnimation(new Animation(883));
		return 2;
	}

	@Override
	public void stop(final Player player) {
		setActionDelay(player, 3);
	}

}
