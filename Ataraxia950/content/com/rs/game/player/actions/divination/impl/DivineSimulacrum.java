package com.rs.game.player.actions.divination.impl;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activites.quest.deathsbounty.DeathsBounty;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.actions.divination.DivineObject;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles Divine location Hunting.
 *
 * @author Noel
 */
public class DivineSimulacrum extends Action {

	private final DivineSimulacrumSpots spot;
	private final WorldObject spott;
	private final WorldTile tile;
	private int itemId;

	public DivineSimulacrum(DivineSimulacrumSpots spot, int x, int y, int plane, WorldObject spott) {
		this.spot = spot;
		this.spott = spott;
		tile = new WorldTile(x, y, plane);
	}

	@Override
	public boolean start(Player player) {
		if (!checkAll(player))
			return false;
		itemId = getRandomItem(player);
		return true;
	}

	@Override
	public boolean process(Player player) {
		player.setNextAnimation(spot.getAnimation());
		player.faceObject(spott);
		if (!DivineObject.canHarvest(player)) {
			player.setNextAnimation(new Animation(21229));
			return false;
		}
		return checkAll(player);
	}

	private int getRandomItem(Player player) {
		int random = Utils.random(spot.getRewards().length);
		int difference = player.getSkills().getLevel(Skills.DIVINATION) - spot.getRewards()[random].getLevel();
		if (difference < 0)
			return random = 0;
		if (random < -1)
			return random = 0;
		return random;
	}

	@Override
	public int processWithDelay(Player player) {
		addItem(player);
		return Utils.random(1, 3);
	}

	private void addItem(Player player) {
		itemId = getRandomItem(player);
		Item hItem = new Item(spot.getRewards()[itemId].getId(), Utils.random(3, spot.getId() == 66531 ? 18 : 9));
		int roll = Utils.random(100);
		Player owner = spott.getOwner();
		String name = hItem.getDefinitions().getName().toLowerCase();
		player.getInventory().addItem(hItem);
		player.addMemoriesCollected();
		player.sendMessage(getMessage(player, hItem), true);
		player.setNextGraphics(new Graphics(4235));
		double totalXp = spot.getRewards()[itemId].getXp();
		player.getSkills().addXp(Skills.DIVINATION, totalXp);
		DivineObject.handleHarvest(player);
		if (roll >= 75 && owner != null && player != owner && DivineObject.checkPercentage(owner) < 100) {
			if (Utils.random(100) >= 75)
				owner.gathered++;
			if (!hItem.getDefinitions().isStackable())
				hItem.setId(hItem.getDefinitions().getCertId());
			owner.getInventory().addItem(hItem);
			owner.sendMessage(player.getDisplayName() + " harvested some " + name + " for you.", true);
		}
		DeathsBounty.dropUrn(player);
	}

	private String getMessage(Player player, Item item) {
		return "You've harvested some " + item.getDefinitions().getName().toLowerCase() + "; memories harvested: "
				+ Colors.RED + Utils.getFormattedNumber(player.getMemoriesCollected()) + "</col>.";
	}

	private boolean checkAll(Player player) {
		Player owner = spott.getOwner();
		if (owner == null)
			owner = player;
		if (!DivineObject.canUseDivineObj(player, owner)) {
			return false;
		}
		if (player.getSkills().getLevel(Skills.DIVINATION) < spot.getRewards()[itemId].getLevel()) {
			player.setNextAnimation(new Animation(-1));
			player.sendMessage("You need a Divination level of at least " + spot.getRewards()[itemId].getLevel()
					+ " to use this Divine location.");
			return false;
		}
		if (!player.getInventory().hasFreeSlots()) {
			player.setNextAnimation(new Animation(21229));
			player.sendMessage("Inventory full. To make room, sell, drop or bank something.", true);
			return false;
		}
		if (!World.containsObjectWithId(tile, spot.getId())) {
			player.setNextAnimation(new Animation(21229));
			return false;
		}
		return true;
	}

	@Override
	public void stop(final Player player) {
		setActionDelay(player, 3);
	}

	public enum Rewards {

		lustrous_energy(29320, 70, 100), brilliant_energy(29321, 80, 115), luminous_energy(29323, 90,
				130), incandescent_energy(29324, 90, 137.5),;

		private final int id, level;
		private final double xp;

		Rewards(int id, int level, double xp) {
			this.id = id;
			this.level = level;
			this.xp = xp;
		}

		public int getId() {
			return id;
		}

		public int getLevel() {
			return level;
		}

		public double getXp() {
			return xp;
		}
	}

	public enum DivineSimulacrumSpots {

		DIVINE_SIMULACRUM_I(66528, 1, new Animation(21231), Rewards.brilliant_energy, Rewards.incandescent_energy,
				Rewards.luminous_energy, Rewards.lustrous_energy),

		DIVINE_SIMULACRUM_II(66531, 1, new Animation(21231), Rewards.brilliant_energy, Rewards.incandescent_energy,
				Rewards.luminous_energy);

		static final Map<Integer, DivineSimulacrumSpots> spot = new HashMap<Integer, DivineSimulacrumSpots>();

		static {
			for (DivineSimulacrumSpots spots : DivineSimulacrumSpots.values())
				spot.put(spots.id | spots.option << 24, spots);
		}

		private final Rewards[] rewards;
		private final int id, option;
		private final Animation animation;

		DivineSimulacrumSpots(int id, int option, Animation animation, Rewards... rewards) {
			this.id = id;
			this.animation = animation;
			this.rewards = rewards;
			this.option = option;
		}

		public static DivineSimulacrumSpots forId(int id) {
			return spot.get(id);
		}

		public Rewards[] getRewards() {
			return rewards;
		}

		public int getId() {
			return id;
		}

		public int getOption() {
			return option;
		}

		public Animation getAnimation() {
			return animation;
		}
	}
}