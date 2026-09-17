package com.rs.game.player.actions.thieving.def;

/**
 * Stalls.java | 12:10:48 PM
 * 
 * @author Chryonic
 * @date Apr 15, 2017
 */
public enum Stalls {
	VEGETABAL(0, 1, new int[] { 1957, 1965, 1942, 1982, 1550 }, 1, 2, 8, 34381),
	CAKE(34384, 1, new int[] { 1891, 1897, 2309 }, 1, 2.5, 10, 34381),
	CRAFTING(0, 5, new int[] { 1755, 1592, 1597 }, 1, 7, 12, 34381),
	MONKEY_FOOD(0, 5, new int[] { 1963 }, 1, 7, 12, 34381),
	MONKEY_GENERAL(0, 5, new int[] { 1931, 2347, 590 }, 1, 7, 12, 34381),
	TEA_STALL(635, 5, new int[] { 712 }, 1, 7, 12, 634),
	SILK_STALL(34383, 20, new int[] { 950 }, 1, 8, 16, 34381),
	WINE_STALL(14011, 22, new int[] { 1937, 1993, 1987, 1935, 7919 }, 1, 16, 20, 2046),
	SEED_STALL(7053, 27, new int[] { 5096, 5097, 5098, 5099, 5100, 5101, 5102, 5103, 5105 }, 30, 11, 14, 2047),
	FUR_STALL(34387, 35, new int[] { 6814, 958 }, 1, 15, 30, 34381),
	FISH_STALL(0, 42, new int[] { 331, 359, 377 }, 1, 16, 40, 34381),
	CROSSBOW_STALL(0, 49, new int[] { 877, 9420, 9440 }, 1, 11, 52, 34381), 
	SILVER_STALL(0, 50, new int[] { 442 }, 1, 30, 54, 34381),
	SPICE_STALL(34386, 65, new int[] { 2007 }, 1, 80, 81, 34381), 
	MAGIC_STALL(0, 65, new int[] { 556, 557, 554, 555, 563 }, 30, 80, 100, 34381),
	SCIMITAR_STALL(0, 65, new int[] { 1323 }, 1, 80, 100, 34381);

	private final int[] item;
	private final int level;
	private final int amount;
	private final int objectId;
	private final int replaceObject;
	private final double experience;
	private final double seconds;

	Stalls(int objectId, int level, int[] item, int amount, double seconds, double experience, int replaceObject) {
		this.objectId = objectId;
		this.level = level;
		this.item = item;
		this.amount = amount;
		this.seconds = seconds;
		this.experience = experience;
		this.replaceObject = replaceObject;
	}

	public int getAmount() {
		return amount;
	}

	public double getExperience() {
		return experience;
	}

	public int getItem(int count) {
		return item[count];
	}

	public int getLevel() {
		return level;
	}

	public int getObjectId() {
		return objectId;
	}

	public int getReplaceObject() {
		return replaceObject;
	}

	public double getTime() {
		return seconds;
	}

	public int[] getItem() {
		return item;
	}
}