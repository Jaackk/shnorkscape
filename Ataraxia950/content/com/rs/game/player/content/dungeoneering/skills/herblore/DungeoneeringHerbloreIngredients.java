package com.rs.game.player.content.dungeoneering.skills.herblore;

public enum DungeoneeringHerbloreIngredients {
	SAGEWORT_POTION(1, 0, 17492, 17512, 17538),
	VALERIAN_POTION(1, 0, 17492, 17514, 17540),
	ALOE_POTION(1, 0, 17492, 17516, 17542),
	WORMWOOD_POTION(1, 0, 17492, 17518, 17544),
	MAGEBANE_POTION(1, 0, 17492, 17520, 17546),
	FEATHERFOIL_POTION(1, 0, 17492, 17522, 17548),
	WINTERS_GRIP_POTION(1, 0, 17492, 17524, 17550),
	LYCOPUS_POTION(1, 0, 17492, 17526, 17552),
	BUCKTHORN_POTION(1, 0, 17492, 17528, 17554),
	WEAK_MAGIC_POTION(3, 21, DungeoneeringHerblore.SAGEWORT, DungeoneeringHerblore.VOID_DUST, 17556),
	WEAK_RANGED_POTION(5, 34, DungeoneeringHerblore.VALERIAN, DungeoneeringHerblore.VOID_DUST, 17558),
	WEAK_MELEE_POTION(7, 37.5, DungeoneeringHerblore.VALERIAN, DungeoneeringHerblore.MISSHAPEN_CLAW, 17560),
	WEAK_DEFENCE_POTION(9, 41, DungeoneeringHerblore.ALOE, DungeoneeringHerblore.VOID_DUST, 17562),
	WEAK_STAT_RESTORE_POTION(12, 47, DungeoneeringHerblore.ALOE, DungeoneeringHerblore.RED_MOSS, 17564),
	WEAK_CURE_POTION(15, 53.5, DungeoneeringHerblore.ALOE, DungeoneeringHerblore.FIREBREATH_WHISKEY, 17568),
	WEAK_REJUVENATION_POTION(18, 57, DungeoneeringHerblore.ALOE, DungeoneeringHerblore.MISSHAPEN_CLAW, 17570),
	WEAK_POISON(21, 61, DungeoneeringHerblore.SAGEWORT, DungeoneeringHerblore.FIREBREATH_WHISKEY, 17572),
	WEAK_GATHERERS_POTION(24, 65, DungeoneeringHerblore.SAGEWORT, DungeoneeringHerblore.RED_MOSS, 17574),
	WEAK_ARTISANS_POTION(27, 68.5, DungeoneeringHerblore.VALERIAN, DungeoneeringHerblore.RED_MOSS, 17576),
	WEAK_NATURALISTS_POTION(30, 72, DungeoneeringHerblore.SAGEWORT, DungeoneeringHerblore.MISSHAPEN_CLAW, 17578),
	WEAK_SURVIVALISTS_POTION(33, 75, DungeoneeringHerblore.VALERIAN, DungeoneeringHerblore.FIREBREATH_WHISKEY, 17580),
	MAGIC_POTION(36, 79.5, DungeoneeringHerblore.WORMWOOD, DungeoneeringHerblore.VOID_DUST, 17582),
	RANGED_POTION(38, 83, DungeoneeringHerblore.MAGEBANE, DungeoneeringHerblore.VOID_DUST, 17584),
	MELEE_POTION(40, 86.5, DungeoneeringHerblore.MAGEBANE, DungeoneeringHerblore.MISSHAPEN_CLAW, 17586),
	DEFENCE_POTION(42, 89, DungeoneeringHerblore.FEATHERFOIL, DungeoneeringHerblore.VOID_DUST, 17588),
	STAT_RESTORE_POTION(45, 93, DungeoneeringHerblore.FEATHERFOIL, DungeoneeringHerblore.RED_MOSS, 17590),
	CURE_POTION(48, 98.5, DungeoneeringHerblore.FEATHERFOIL, DungeoneeringHerblore.FIREBREATH_WHISKEY, 17592),
	REJUVENATION_POTION(51, 105.5, DungeoneeringHerblore.FEATHERFOIL, DungeoneeringHerblore.MISSHAPEN_CLAW, 17594),
	WEAPON_POISON(54, 114, DungeoneeringHerblore.WORMWOOD, DungeoneeringHerblore.FIREBREATH_WHISKEY, 17596),
	GATHERERS_POTION(57, 123.5, DungeoneeringHerblore.WORMWOOD, DungeoneeringHerblore.RED_MOSS, 17598),
	ARTISANS_POTION(60, 131, DungeoneeringHerblore.MAGEBANE, DungeoneeringHerblore.RED_MOSS, 17600),
	NATURALISTS_POTION(63, 139.5, DungeoneeringHerblore.WORMWOOD, DungeoneeringHerblore.MISSHAPEN_CLAW, 17602),
	SURVIVALISTS_POTION(66, 147, DungeoneeringHerblore.MAGEBANE, DungeoneeringHerblore.FIREBREATH_WHISKEY, 17604),
	STRONG_MAGIC_POTION(69, 155.5, DungeoneeringHerblore.WINTERS_GRIP, DungeoneeringHerblore.VOID_DUST, 17606),
	STRONG_RANGED_POTION(71, 160, DungeoneeringHerblore.LYCOPUS, DungeoneeringHerblore.VOID_DUST, 17608),
	STRONG_MELEE_POTION(73, 164, DungeoneeringHerblore.LYCOPUS, DungeoneeringHerblore.MISSHAPEN_CLAW, 17610),
	STRONG_DEFENCE_POTION(75, 170.5, DungeoneeringHerblore.BUCKTHORN, DungeoneeringHerblore.VOID_DUST, 17612),
	STRONG_STAT_RESTORE_POTION(78, 173.5, DungeoneeringHerblore.BUCKTHORN, DungeoneeringHerblore.RED_MOSS, 17614),
	STRONG_CURE_POTION(81, 178, DungeoneeringHerblore.BUCKTHORN, DungeoneeringHerblore.FIREBREATH_WHISKEY, 17616),
	STRONG_REJUVENATION_POTION(84, 189.5, DungeoneeringHerblore.BUCKTHORN, DungeoneeringHerblore.MISSHAPEN_CLAW, 17618),
	STRONG_POISON(87, 205.5, DungeoneeringHerblore.WINTERS_GRIP, DungeoneeringHerblore.FIREBREATH_WHISKEY, 17620),
	STRONG_GATHERERS_POTION(90, 234, DungeoneeringHerblore.WINTERS_GRIP, DungeoneeringHerblore.RED_MOSS, 17622),
	STRONG_ARTISANS_POTION(93, 253, DungeoneeringHerblore.LYCOPUS, DungeoneeringHerblore.RED_MOSS, 17624),
	STRONG_NATURALISTS_POTION(96, 279, DungeoneeringHerblore.WINTERS_GRIP, DungeoneeringHerblore.MISSHAPEN_CLAW, 17626),
	STRONG_SURVIVALISTS_POTION(99, 315.5, DungeoneeringHerblore.LYCOPUS, DungeoneeringHerblore.FIREBREATH_WHISKEY, 17628);

	private final int level;
	private final double experience;
	private final int[] items;

	DungeoneeringHerbloreIngredients(int level, double experience, int... items) {
		this.level = level;
		this.experience = experience;
		this.items = items;
	}

	public int getLevel() {
		return level;
	}

	public double getExperience() {
		return experience;
	}

	public int[] getItems() {
		return items;
	}
}
