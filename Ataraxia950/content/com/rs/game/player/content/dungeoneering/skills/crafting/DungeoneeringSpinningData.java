package com.rs.game.player.content.dungeoneering.skills.crafting;

public enum DungeoneeringSpinningData {

	SALVE(17448, 17468, 1, 2.5),
	WILDERCRESS(17450, 17470, 10, 3),
	BLIGHTLEAF(17452, 17472, 20, 3.6),
	ROSEBLOOD(17454, 17474, 30, 4.3),
	BRYLL(17456, 17476, 40, 5.2),
	DUSKWEED(17458, 17478, 50, 6.4),
	SOULBELL(17460, 17480, 60, 7.8),
	ECTOCLOTH(17462, 17482, 70, 9.5),
	RUNIC(17464, 17484, 80, 11.6),
	SPIRITBLOOM(17466, 17486, 90, 14.2);
	
	private final int ingredient;
    private final int product;
    private final int level;
	private final double xp;
	
	DungeoneeringSpinningData(int ingredient, int product, int level, double xp) {
		this.ingredient = ingredient;
		this.product = product;
		this.level = level;
		this.xp = xp;
	}
	
	public int getIngredient() {
		return ingredient;
	}
	
	public int getProduct() {
		return product;
	}
	
	public int getLevel() {
		return level;
	}
	
	public double getExperience() {
		return xp;
	}
	
}
