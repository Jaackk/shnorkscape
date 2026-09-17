package com.rs.game.player.content.dungeoneering.skills.construction;

import com.rs.game.item.Item;

public enum DungeoneeringConstructionData {

	GROUP_GATESTONE(1, new int[] { 53124, 53125, 53126, 55606, 56146 }, 0),
	PHOTO_BOOTH(1, new int[] { 53128, 53130, 53132, 53700, 55607 }, 0),
	FARMING_PATCH(25, new int[] { 53136, 53173, 53210, 53702, 55609 }, 9, new Item(17684, 5), new Item(17652, 5)),
	COOKING_RANGE(50, new int[] { 53284, 53305, 53326, 53347, 55646 }, 13.5, new Item(17688, 5), new Item(17656, 5)),
	PRAYER_ALTAR(75, new int[] { 53368, 53376, 53384, 53739, 55667 }, 29.1, new Item(17692, 5), new Item(17660, 5)),
	SKILLCAPE_STAND(99, new int[] { 53400 }, 0, new Item(17682));

	private final int level;
	private final int[] objectIds;
	private final double xp;
	private final Item[] materials;

	DungeoneeringConstructionData(int level, int[] objectIds, double xp, Item... materials) {
		this.level = level;
		this.objectIds = objectIds;
		this.xp = xp;
		this.materials = materials;
	}
	
	public int getLevel() {
		return level;
	}
	
	public int[] getObjectIds() {
		return objectIds;
	}
	
	public double getExperience() {
		return xp;
	}
	
	public Item[] getMaterials() {
		return materials;
	}
}
