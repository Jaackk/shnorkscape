package com.rs.game.player.actions.mining;

import com.rs.game.WorldObject;
import com.rs.game.item.Item;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.utils.Utils;

/**
 * A class containing the Lava Flow Mine.
 *
 * @author Savions Sw.
 */

public class LavaFlowMine extends MiningBase {

	/**
	 * The object.
	 */
	private final WorldObject object;

	/**
	 * The XP Boost.
	 */
	private final double XPBoost = 1.0;

	/**
	 * The Lava Flow Mine.
	 *
	 * @param object
	 *            The object.
	 */
	public LavaFlowMine(WorldObject object) {
		this.object = object;
	}

	/**
	 * Adds XP to the Player.
	 *
	 * @param player
	 *            The Player.
	 */

	private void addXP(Player player) {
		double xpBoost = XPBoost;
		double totalXp = Utils.random(65, 80) * xpBoost;
		if (hasMiningSuit(player))
			totalXp *= 1.056;
        Item weapon = player.getEquipment().getItem(Equipment.SLOT_WEAPON);
        boolean hasAugmentedTool = weapon != null && weapon.getInventionData() != null && (weapon.getName().toLowerCase().contains("dragon pickaxe") || weapon.getName().toLowerCase().contains("crystal pickaxe"));
        if (!hasAugmentedTool)
                weapon = null;
        player.getInventionManager().processSkillXp(Skills.MINING, totalXp, weapon);
		player.getSkills().addXp(Skills.MINING, totalXp);
		player.getPackets().sendGameMessage("You mine away some crust.", true);
	}

	/**
	 * Checks if the Player has all the requirments.
	 *
	 * @param player
	 *            The Player
	 * @return Return if the Player has all the requirments.
	 */

	private boolean checkAll(Player player) {
		if (!setPickaxe(player)) {
			player.getPackets().sendObjectMessage(0, 15263739, object,
					"You need a pickaxe to mine the " + object.getDefinitions().name + ".");
			return false;
		}
		if (!hasPickaxe(player)) {
			player.getPackets().sendGameMessage("You dont have the required level to use this pickaxe.");
			return false;
		}
        return hasMiningLevel(player);
    }

	/**
	 * Gets the Mining Delay.
	 *
	 * @param player
	 *            The Player.
	 * @return Return the Mining Delay.
	 */

	private int getMiningDelay(Player player) {
		int summoningBonus = 0;
		if (player.getFamiliar() != null) {
			if (player.getFamiliar().getId() == 7342 || player.getFamiliar().getId() == 7342)
				summoningBonus += 10;
			else if (player.getFamiliar().getId() == 6832 || player.getFamiliar().getId() == 6831)
				summoningBonus += 1;
		}
		int oreBaseTime = 50;
		int oreRandomTime = 20;
		int mineTimer = oreBaseTime - (player.getSkills().getLevel(Skills.MINING) + summoningBonus)
				- Utils.getRandom(pickaxeTime);
		if (mineTimer < 1 + oreRandomTime)
			mineTimer = 1 + Utils.getRandom(oreRandomTime);
		mineTimer /= player.getAuraManager().getMiningAccurayMultiplier();
		return mineTimer;
	}

	/**
	 * Checks if the player has the required Mining level.
	 *
	 * @param player
	 *            The Player.
	 * @return Return if the Player has the required Mining level.
	 */

	private boolean hasMiningLevel(Player player) {
		if (player.getSkills().getLevel(Skills.MINING) < 68) {
			player.getPackets().sendGameMessage("You need a mining level of 68 to mine this rock.");
			return false;
		}
		return true;
	}

	/**
	 * Checks if the Player has the Mining Suit.
	 *
	 * @param player
	 *            The Player.
	 * @return Return if the Player has the Mining Suit.
	 */

	private boolean hasMiningSuit(Player player) {
        return player.getEquipment().getHatId() == 20789 && player.getEquipment().getChestId() == 20791
                && player.getEquipment().getBootsId() == 20787 && player.getEquipment().getLegsId() == 20790
                && player.getEquipment().getBootsId() == 20788;
    }

	@Override
	public boolean process(Player player) {
		setAnimationAndGFX(player);
		player.faceObject(object);
		if (checkAll(player)) {
			if (Utils.getRandom(18) == 0) {
				addXP(player);
			}
			return true;
		}
		return false;
	}

	@Override
	public int processWithDelay(Player player) {
		addXP(player);
		return getMiningDelay(player);
	}

	@Override
	public boolean start(Player player) {
		if (!checkAll(player))
			return false;
		player.getPackets().sendGameMessage("You swing your pickaxe at the rock.", true);
		setActionDelay(player, getMiningDelay(player));
		return true;
	}

	@Override
	public void stop(Player player) {
		setActionDelay(player, 3);
	}

}
