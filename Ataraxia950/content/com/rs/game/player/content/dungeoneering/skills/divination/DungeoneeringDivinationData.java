package com.rs.game.player.content.dungeoneering.skills.divination;

import com.rs.game.item.Item;

public enum DungeoneeringDivinationData {

	POR_I(29209, 2, 8, new Item(29313, 30), 18159, 10, 20),
	POR_II(29210, 15, 15, new Item(29314, 30), 18161, 20, 20),
	POR_III(29211, 25, 20, new Item(29315, 30), 18163, 30, 40),
	POR_IV(29212, 35, 30, new Item(29316, 30), 18165, 40, 60),
	POR_V(29213, 45, 40, new Item(29317, 35), 18167, 50, 80),
	POR_VI(29214, 55, 50, new Item(29318, 40), 18169, 60, 100),
	POR_VII(29215, 65, 60, new Item(29319, 45), 18171, 70, 120),
	POR_VIII(29216, 75, 70, new Item(29320, 50), 18173, 80, 140),
	POR_IX(29217, 86, 80, new Item(29321, 50), 18175, 90, 160),
	POR_X(29218, 97, 90, new Item(29322, 60), 18177, 100, 180),
	POS_I(29219, 2, 0, new Item(29313, 30), 0, 10, 2),
	POS_II(29221, 15, 0, new Item(29314, 30), 0, 20, 4),
	POS_III(29223, 25, 0, new Item(29315, 30), 0, 30, 6),
	POS_IV(29225, 35, 0, new Item(29316, 30), 0, 40, 8),
	POS_V(29227, 45, 0, new Item(29317, 35), 0, 50, 10),
	POS_VI(29229, 55, 0, new Item(29318, 40), 0, 60, 12),
	POS_VII(29231, 65, 0, new Item(29319, 45), 0, 70, 14),
	POS_VIII(29233, 75, 0, new Item(29320, 50), 0, 80, 16),
	POS_IX(29235, 86, 0, new Item(29321, 50), 0, 90, 18),
	POS_X(29237, 97, 0, new Item(29322, 60), 0, 100, 20);
	
	private final int itemId;
    private final int divLevel;
    private final int hpLevel;
    private final int secondaryItemId;
    private final int experience;
    private final int tertiaryBoost;
	private final Item energies;
	
	DungeoneeringDivinationData(int itemId, int divLevel, int hpLevel, Item energy, int secondaryItemId, int experience, int tertiaryBoost) {
		this.itemId = itemId;
		this.divLevel = divLevel;
		this.hpLevel = hpLevel;
		this.energies = energy;
		this.secondaryItemId = secondaryItemId;
		this.experience = experience;
		this.tertiaryBoost = tertiaryBoost;
	}
	
	public int getItemId() {
		return itemId;
	}
	
	public int getDivinationLevel() {
		return divLevel;
	}
	
	public int getConstitutionLevel() {
		return hpLevel;
	}
	
	public Item getEnergies() {
		return energies;
	}
	
	public int getSecondaryItemId() {
		return secondaryItemId;
	}
	
	public int getExperience() {
		return experience;
	}
	
	public int getTertiaryBoost() {
		return tertiaryBoost;
	}
	
}
