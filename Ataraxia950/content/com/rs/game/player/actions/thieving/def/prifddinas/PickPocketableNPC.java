package com.rs.game.player.actions.thieving.def.prifddinas;

import com.rs.game.item.Item;
import com.rs.utils.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * An enum containing all Prifddinas Clan district workers.
 *
 * @author Noel
 */
public enum PickPocketableNPC {

	IORWERTH_WORKER(new short[] { 20113, 20114, 20115, 20116 }, 91, 125, "Clan Iorwerth",
			new Item(995, Utils.random(135, 346)), new Item(385), new Item(450), new Item(150), new Item(168),
			new Item(162), new Item(32732), new Item(32733), new Item(32734), new Item(32735)),

	ITHELL_WORKER(new short[] { 20312, 20313, 20314, 20315, 20316 }, 92, 130, "Clan Ithell",
			new Item(995, Utils.random(135, 346)), new Item(8779), new Item(1782), new Item(1761), new Item(8781),
			new Item(32732), new Item(32733), new Item(32734), new Item(32735), new Item(995, Utils.random(135, 346))),

	CADARN_WORKER(new short[] { 20121, 20122, 20123, 20124 }, 93, 135, "Clan Cadarn",
			new Item(995, Utils.random(135, 346)), new Item(385), new Item(568), new Item(3047), new Item(174),
			new Item(1778), new Item(32732), new Item(32733), new Item(32734), new Item(32735)),

	AMLODD_WORKER(new short[] { 20316, 20317 }, 94, 140, "Clan Amlodd", new Item(995, Utils.random(135, 346)),
			new Item(12158), new Item(12159), new Item(12160), new Item(12163), new Item(29324, Utils.random(2, 6)),
			new Item(29323, Utils.random(2, 6)), new Item(29322, Utils.random(2, 6)), new Item(32732), new Item(32733),
			new Item(32734), new Item(32735)),

	TRAHAEARN_WORKER(new short[] { 20125, 20126, 20127, 20128 }, 95, 145, "Clan Trahaearn",
			new Item(995, Utils.random(135, 346)), new Item(2354), new Item(454), new Item(445), new Item(450),
			new Item(32262), new Item(32732), new Item(32733), new Item(32734), new Item(32735)),

	HEFIN_WORKER(new short[] { 20320, 20321, 20322, 20323 }, 96, 150, "Clan Hefin",
			new Item(995, Utils.random(135, 346)), new Item(20267), new Item(7218), new Item(536), new Item(32616),
			new Item(32615), new Item(3039), new Item(3023), new Item(32732), new Item(32733), new Item(32734),
			new Item(32735)),

	CRWYS_WORKER(new short[] { 20117, 20118, 20119, 20120 }, 97, 155, "Clan Crwys",
			new Item(995, Utils.random(135, 346)), new Item(1780), new Item(6035), new Item(5372), new Item(5500),
			new Item(1516), new Item(1513), new Item(32732), new Item(32733), new Item(32734), new Item(32735)),

	MEILYR_WORKER(new short[] { 20324, 20325, 20326, 20327 }, 98, 170, "Clan Meilyr",
			new Item(995, Utils.random(135, 346)), new Item(241), new Item(32747), new Item(232), new Item(6694),
			new Item(32732), new Item(32733), new Item(32734), new Item(32735));

	/**
	 * A hashmap containing all the NPC pickpocketing data.
	 */
	private static final Map<Short, PickPocketableNPC> NPCS = new HashMap<Short, PickPocketableNPC>();

	/**
	 * Populate the mapping.
	 */
	static {
		for (PickPocketableNPC data : PickPocketableNPC.values()) {
			for (short id : data.npcIds)
				NPCS.put(id, data);
		}
	}

	/**
	 * The npc ids.
	 */
	private final short[] npcIds;
	/**
	 * The thieving levels required (slot 0 = normal loot, 1 = double, 2 = 3x
	 * loot, 4 = 4x loot).
	 */
	private final int thievingLevel;
	/**
	 * The experience gained.
	 */
	private final double experience;
	/**
	 * The message sent when caught.
	 */
	private final String message;
	/**
	 * The possible loot.
	 */
	private final Item[] loot;

	/**
	 * Constructs a new {@code PickpocketableNPC} {@code Object}.
	 *
	 * @param npcIds
	 *            The array containing all the npc ids of the npcs using this
	 *            pickpocket data.
	 * @param thievingLevel
	 *            The thieving level required.
	 * @param agilityLevel
	 *            The agility level required.
	 * @param experience
	 *            The experience gained.
	 * @param loot
	 *            The possible loot.
	 */
	PickPocketableNPC(short[] npcIds, int thievingLevel, double experience, String message, Item... loot) {
		this.npcIds = npcIds;
		this.thievingLevel = thievingLevel;
		this.experience = experience;
		this.message = message;
		this.loot = loot;
	}

	/**
	 * Gets the pickpocketing data from the mapping, depending on the NPC id.
	 *
	 * @param id
	 *            The npc id.
	 * @return The {@code PickpocketableNPC} {@code Object}, or {@code null} if
	 *         the data was non-existant.
	 */
	public static PickPocketableNPC get(int id) {
		return NPCS.get((short) id);
	}

	/**
	 * @return the experience
	 */
	public double getExperience() {
		return experience;
	}

	/**
	 * @return the loot
	 */
	public Item[] getLoot() {
		return loot;
	}

	/**
	 * @return the npcIds
	 */
	public short[] getNpcIds() {
		return npcIds;
	}

	/**
	 * @return the thievingLevel
	 */
	public int getThievingLevel() {
		return thievingLevel;
	}

	/**
	 * @return the message.
	 */
	public String getMessage() {
		return message;
	}
}