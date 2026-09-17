package com.rs.game.player.actions.mining;

import com.rs.game.Animation;
import com.rs.game.activites.halloween.ClueScrollDistributor;
import com.rs.game.item.Item;
import com.rs.game.npc.others.LivingRock;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.utils.Utils;

public class LivingMineralMining extends MiningBase {

	private final LivingRock rock;

	public LivingMineralMining(LivingRock rock) {
		this.rock = rock;
	}

	private void addOre(Player player) {
		double xp = 1.0;
		xp *= miningSuit(player);
        Item weapon = player.getEquipment().getItem(Equipment.SLOT_WEAPON);
        boolean hasAugmentedTool = weapon != null && weapon.getInventionData() != null && (weapon.getName().toLowerCase().contains("dragon pickaxe") || weapon.getName().toLowerCase().contains("crystal pickaxe"));
        if (!hasAugmentedTool)
                weapon = null;

        player.getInventionManager().processSkillXp(Skills.MINING, xp, weapon);
		player.getSkills().addXp(Skills.MINING, xp);
		if (player.getInventionManager().procGathering(weapon, new Item(15263, Utils.random(5, 40)), Skills.MINING, xp))
		player.getInventory().addItem(15263, Utils.random(5, 40));
		player.getPackets().sendGameMessage("You manage to mine some living minerals.", true);
		ClueScrollDistributor.givePlayerClueScrollIfProbable(player, ClueScrollDistributor.SKILLING_PERCENT);
	}

	private boolean checkAll(Player player) {
		if (!setPickaxe(player)) {
			player.getPackets().sendNPCMessage(0, 15263739, rock,
					"You need a pickaxe to mine from the " + rock.getDefinitions().name + ".");
			return false;
		}
		if (!hasPickaxe(player)) {
			player.getPackets().sendGameMessage("You dont have the required level to use this pickaxe.");
			return false;
		}
		if (!hasMiningLevel(player))
			return false;
		if (!player.getInventory().hasFreeSlots()) {
			player.sendMessage("Inventory full. To make more room, sell, drop or bank something.");
			return false;
		}
		if (!rock.canMine(player)) {
			player.getPackets().sendGameMessage(
					"You must wait at least one minute before you can mine a living rock creature that someone else defeated.");
			return false;
		}
		return true;
	}

	private boolean checkRock(Player player) {
		return !rock.hasFinished();
	}

	private int getMiningDelay(Player player) {
		int oreBaseTime = 50;
		int oreRandomTime = 20;
		int mineTimer = oreBaseTime - player.getSkills().getLevel(Skills.MINING) - Utils.getRandom(pickaxeTime);
		if (mineTimer < 1 + oreRandomTime)
			mineTimer = 1 + Utils.getRandom(oreRandomTime);
		mineTimer /= player.getAuraManager().getMiningAccurayMultiplier();
		if (player.getPerkManager().hasPerkActive(DonationPerk.MASTER_MINER))
			mineTimer /= 1.33;
		return mineTimer;
	}

	private boolean hasMiningLevel(Player player) {
		if (73 > player.getSkills().getLevel(Skills.MINING)) {
			player.getPackets().sendGameMessage("You need a mining level of 73 to mine this rock.");
			return false;
		}
		return true;
	}

	@Override
	public boolean process(Player player) {
		setAnimationAndGFX(player);
		return checkRock(player);
	}

	@Override
	public int processWithDelay(Player player) {
		addOre(player);
		rock.takeRemains();
		player.setNextAnimation(new Animation(-1));
		return -1;
	}

	@Override
	public boolean start(Player player) {
		if (!checkAll(player))
			return false;
		setActionDelay(player, getMiningDelay(player));
		return true;
	}

	/**
	 * XP modifier by wearing items.
	 *
	 * @param player
	 *            The player.
	 * @return the XP modifier.
	 */
	private double miningSuit(Player player) {
		double xpBoost = 1.0;
		if (player.getEquipment().getHatId() == 20789)
			xpBoost *= 1.01;
		if (player.getEquipment().getChestId() == 20791)
			xpBoost *= 1.01;
		if (player.getEquipment().getLegsId() == 20790)
			xpBoost *= 1.01;
		if (player.getEquipment().getBootsId() == 20788)
			xpBoost *= 1.01;
		if (player.getEquipment().getHatId() == 20789 && player.getEquipment().getChestId() == 20791
				&& player.getEquipment().getLegsId() == 20790 && player.getEquipment().getBootsId() == 20788)
			xpBoost *= 1.01;
		return xpBoost;
	}
}
