package com.rs.game.player.actions.ancientartefacts;

public enum Artefact {

	BROKEN_STATUE_HEADDRESS(14892, 50_000),
	THIRD_AGE_CARAFE(14891, 100_000),
	BRONZED_DRAGON_CLAW(14890, 200_000),
	ANCIENT_PSALTERY_BRIDGE(14889, 300_000),
	SARADOMIN_AMPHORA(14888, 400_000),
	BANDOS_SCRIMSHAW(14887, 500_000),
	SARADOMIN_CARVING(14886, 750_000),
	ZAMORAK_MEDALLION(14885, 1_000_000),
	ARMADYL_TOTEM(14884, 1_500_000),
	GUTHIXIAN_BRAZIER(14883, 2_000_000),
	RUBY_CHALICE(14882, 2_500_000),
	BANDOS_STATUETTE(14881, 3_000_000),
	SARADOMIN_STATUETTE(14880, 4_000_000),
	ZAMORAK_STATUETTE(14879, 5_000_000),
	ARMADYL_STATUETTE(14878, 7_500_000),
	SEREN_STATUETTE(14877, 10_000_000),
	ANCIENT_STATUETTE(14876, 50_000_000);
	
	private final int id, price;
	
	Artefact(final int id, final int price) {
		this.id = id;
		this.price = price;
	}
	
	public int getId() {
		return id;
	}
	
	public int getPrice() {
		return price;
	}
	
}
