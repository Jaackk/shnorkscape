package com.rs.game.activites.warriorguild;

import java.util.HashMap;
import java.util.Map;

public enum WarriorsArmour {

	/**
	 * The enum for the armours
	 */
	BRONZE(new int[] { 1075, 1117, 1155 }, 4278, 10), IRON(new int[] { 1153, 1115, 1067 }, 4279, 20), STEEL(new int[] { 1157, 1119, 1069 }, 4280, 30), BLACK(new int[] { 1165, 1125, 1077 }, 4281, 40), MITRHIL(new int[] { 1159, 1121, 1071 }, 4282, 50), ADAMANT(new int[] { 1161, 1123, 1073 }, 4283, 60), RUNE(new int[] { 1127, 1079, 1163 }, 4284, 80),;

	private static final Map<Integer, WarriorsArmour> rocks = new HashMap<Integer, WarriorsArmour>();

	static {
		for (WarriorsArmour rock : WarriorsArmour.values()) {
			for (int object : rock.armour) {
				rocks.put(object, rock);
			}
		}
	}

	private final int reward;
	private final int npcId;
	private final int[] armour;

	/**
	 * @param armour
	 *            The armour to use
	 * @param npcId
	 *            The NPC to spawn
	 * @param reward
	 *            The token reward
	 */
	WarriorsArmour(int[] armour, int npcId, int reward) {
		this.armour = armour;
		this.npcId = npcId;
		this.reward = reward;
	}

	public static WarriorsArmour forId(int object) {
		return rocks.get(object);
	}

	/**
	 * @return The armour
	 */

	public int[] getArmour() {
		return armour;
	}

	/**
	 * @return The npc id
	 */

	public int getNpcId() {
		return npcId;
	}

	/**
	 * @return the reward
	 */

	public int getReward() {
		return reward;
	}

}