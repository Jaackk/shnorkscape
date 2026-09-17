package com.rs.game.player.actions.mining;

import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.activites.halloween.ClueScrollDistributor;
import com.rs.game.activites.worldevents.ShootingStar;
import com.rs.game.item.Item;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.utils.Utils;

public class ShootingStarMining extends MiningBase {

	private final WorldObject rock;

	public ShootingStarMining(WorldObject rock) {
		this.rock = rock;
	}

	@Override
	public boolean start(Player player) {
		if (!checkAll(player))
			return false;
		player.getPackets().sendGameMessage("You swing your pickaxe at the rock.");
		setActionDelay(player, getMiningDelay());
		return true;
	}

	private int getMiningDelay() {
		return ShootingStar.getStarSize() * 2;
	}

	private boolean checkAll(Player player) {
		player.closeInterfaces();
		if (!hasPickaxe(player)) {
			player.getPackets().sendObjectMessage(0, 15263739, rock,
					"You need a pickaxe to mine the " + rock.getDefinitions().name + ".");
			return false;
		}
		if (!setPickaxe(player)) {
			player.sendMessage("You dont have the required level to use this pickaxe.");
			return false;
		}
		if (!hasMiningLevel(player))
			return false;
		if (!player.getInventory().hasFreeSlots()) {
			player.sendMessage("Inventory full. To make more room, sell, drop or bank something.");
			return false;
		}
		return true;
	}

	private boolean hasMiningLevel(Player player) {
		int level = ShootingStar.getLevel();
		if (level > player.getSkills().getLevel(Skills.MINING)) {
			player.getPackets().sendGameMessage("You need a mining level of " + level + " to mine this rock.");
			return false;
		}
		return true;
	}

	@Override
	public boolean process(Player player) {
		setAnimationAndGFX(player);
		player.faceObject(rock);
		return checkRock(player);
	}

	@Override
	public int processWithDelay(Player player) {
		addOre(player);
		if (!player.getInventory().hasFreeSlots() && !player.getInventory().containsItem(ShootingStar.STARDUST, 1)) {
			player.setNextAnimation(new Animation(-1));
			player.sendMessage("Inventory full. To make more room, sell, drop or bank something.");
			return -1;
		}
		return getMiningDelay();
	}

	private void addOre(Player player) {
        Item weapon = player.getEquipment().getItem(Equipment.SLOT_WEAPON);
        boolean hasAugmentedTool = weapon != null && weapon.getInventionData() != null && (weapon.getName().toLowerCase().contains("dragon pickaxe") || weapon.getName().toLowerCase().contains("crystal pickaxe"));
        if (!hasAugmentedTool)
                weapon = null;
        player.getInventionManager().processSkillXp(Skills.MINING, ShootingStar.getXP(), weapon);
		player.getSkills().addXp(Skills.MINING, ShootingStar.getXP());
		int heldAmount = player.getInventory().getAmountOf(ShootingStar.STARDUST);
		heldAmount += player.getBank().getNumberOf(ShootingStar.STARDUST);
		if (heldAmount < 1000)
			player.getInventory().addItem(ShootingStar.STARDUST, 1);
		player.getPackets().sendGameMessage("You mine some stardust.", true);
		ClueScrollDistributor.givePlayerClueScrollIfProbable(player, ClueScrollDistributor.SKILLING_PERCENT);
		ShootingStar.reduceStarLife();
	}

	private boolean checkRock(Player player) {
		return World.containsObjectWithId(rock, rock.getId());
	}
}
