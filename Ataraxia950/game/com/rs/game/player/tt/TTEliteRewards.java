package com.rs.game.player.tt;

import com.rs.game.item.Item;

/**
 * @author Kris
 * Using 'amount' field as minimum amount and 'charges' field as maximum amount.
 * Up to 3 'TTH.ALL' rewards at a time.
 * Excluded heraldic armours - No one wants that shit.
 */
public enum TTEliteRewards {

	PALM_TREE_SEED(new Item(5289, 1), 80),
	PAPAYA_TREE_SEED(new Item(5288, 1), 80),
	YEW_SEED(new Item(5315, 1), 80),
	DWARF_WEED_SEED(new Item(5303, 2), 80),
	LANTADYME_SEED(new Item(5302, 2), 80),
	SUPER_RESTORE(new Item(3025, 9), 70),
	ANTIFIRE(new Item(2453, 9), 70),
	PRAYER_POTION(new Item(2435, 9), 70),
	WATER_TALISMAN(new Item(1445, 8), 60),
	SWAMP_LIZARD(new Item(10163, 15), 50),
	RUNE_BAR(new Item(2364, 5), 55),
	UNICORN_HORN(new Item(238, 10), 65),
	ROYAL_DRAGONHIDE(new Item(24373, 15), 70),
	ONYX_BOLT_TIPS(new Item(9194, 12), 60),
	UNCUT_DRAGONSTONE(new Item(1632, 2), 60),
	BATTLE_STAFF(new Item(1392, 8), 70),
	MAHOGANY_PLANK(new Item(8783, 40), 60),
	DRAGON_HELM(new Item(1149, 1), 50),
	RUNE_PLATEBODY(new Item(1127, 1), 75),
	ARMADYL_FULL_HELM(new Item(19422, 1), 20),
	ARMADYL_PLATEBODY(new Item(19413, 1), 20),
	ARMADYL_PLATELEGS(new Item(19416, 1), 20),
	ARMADYL_PLATESKIRT(new Item(19419, 1), 20),
	ARMADYL_KITESHIELD(new Item(19425, 1), 20),
	BANDOS_FULL_HELM(new Item(19437, 1), 20),
	BANDOS_PLATEBODY(new Item(19428, 1), 20),
	BANDOS_PLATELEGS(new Item(19431, 1), 20),
	BANDOS_PLATESKIRT(new Item(19434, 1), 20),
	BANDOS_KITESHIELD(new Item(19440, 1), 20),
	ANCIENT_FULL_HELM(new Item(19407, 1), 20),
	ANCIENT_PLATEBODY(new Item(19398, 1), 20),
	ANCIENT_PLATELEGS(new Item(19401, 1), 20),
	ANCIENT_PLATESKIRT(new Item(19404, 1), 20),
	ANCIENT_KITESHIELD(new Item(19410, 1), 20),
	ARMADYL_COIF(new Item(19465, 1), 20),
	ARMADYL_BODY(new Item(19461, 1), 20),
	ARMADYL_CHAPS(new Item(19463, 1), 20),
	ARMADYL_VAMBRACES(new Item(19459, 1), 20),
	BANDOS_COIF(new Item(19457, 1), 20),
	BANDOS_BODY(new Item(19457, 1), 20),
	BANDOS_CHAPS(new Item(19445, 1), 20),
	BANDOS_VAMBRACES(new Item(19451, 1), 20),
	ANCIENT_COIF(new Item(19449, 1), 20),
	ANCIENT_BODY(new Item(19445, 1), 20),
	ANCIENT_CHAPS(new Item(19449, 1), 20),
	ANCIENT_VAMBRACES(new Item(19443, 1), 20),
	ARMADYL_STOLE(new Item(19392, 1), 20),
	BANDOS_STOLE(new Item(19394, 1), 20),
	ANCIENT_STOLE(new Item(19396, 1), 20),
	ARMADYL_CROZIER(new Item(19362, 1), 20),
	BANDOS_CROZIER(new Item(19364, 1), 20),
	ANCIENT_CROZIER(new Item(19366, 1), 20),
	FURY_ORNAMENT_KIT(new Item(19333, 1), 10),
	DRAGON_FULL_HELM_ORNAMENT_KIT_OR(new Item(19346, 1), 10),
	DRAGON_PLATELEGS_SKIRT_ORNAMENT_KIT_OR(new Item(19348, 1), 10),
	DRAGON_PLATEBODY_ORNAMENT_KIT_OR(new Item(19350, 1), 10),
	DRAGON_SQ_SHIELD_ORNAMENT_KIT_OR(new Item(19352, 1), 10),
	DRAGON_FULL_HELM_ORNAMENT_KIT_SP(new Item(19354, 1), 10),
	DRAGON_PLATELEGS_SKIRT_ORNAMENT_KIT_SP(new Item(19356, 1), 10),
	DRAGON_PLATEBODY_ORNAMENT_KIT_SP(new Item(19358, 1), 10),
	DRAGON_SQ_SHIELD_ORNAMENT_KIT_SP(new Item(19360, 1), 10),
	DRAGON_KITE_ORNAMENT_KIT_OR(new Item(25312, 1), 10),
	DRAGON_KITE_ORNAMENT_KIT_SP(new Item(25314, 1), 10),
	BAT_STAFF(new Item(19327, 1), 8),
	WOLF_STAFF(new Item(19329, 1), 8),
	DRAGON_STAFF(new Item(19323, 1), 8),
	CAT_STAFF(new Item(19331, 1), 8),
	PENGUIN_STAFF(new Item(19325, 1), 8),
	STARVED_ANCIENT_EFFIGY(new Item(18778, 1), 3),
	BLACK_DRAGON_MASK(new Item(19290, 1), 3),
	BRONZE_DRAGON_MASK(new Item(19296, 1), 3),
	IRON_DRAGON_MASK(new Item(19299, 1), 3),
	STEEL_DRAGON_MASK(new Item(19302, 1), 3),
	MITHRIL_DRAGON_MASK(new Item(19305, 1), 3),
	FROST_DRAGON_MASK(new Item(19293, 1), 3),
	GUTHIX_BOW(new Item(19146, 1), 6),
	SARADOMIN_BOW(new Item(19143, 1), 6),
	ZAMORAK_BOW(new Item(19149, 1), 6),
	BARROWS_DYE(new Item(33294, 1), 4),
	SHADOW_DYE(new Item(33296, 1), 4),
	THIRD_AGE_DYE(new Item(33298, 1), 4),
	BLOOD_DYE(new Item(36274), 4),
	ICE_DYE(new Item(41887), 4),
	BACKSTAB_CAPE(new Item(33520), 5),
	CRYSTAL_FRAGMENT1(new Item(28547), 5),
	CRYSTAL_FRAGMENT2(new Item(28548), 5),
	CRYSTAL_FRAGMENT3(new Item(28549), 5),
	THIRD_AGE_DRUIDIC_STAFF(new Item(19308, 1), 4),
	THIRD_AGE_CLOAK(new Item(19311, 1), 4),
	THIRD_AGE_WREATH(new Item(19314, 1), 4),
	THIRD_AGE_ROBE_TOP(new Item(19317, 1), 4),
	THIRD_AGE_ROBE_BOTTOMS(new Item(19320, 1), 4); 
	
	private final Item item;
	private final double rarity;
	
	public static final TTEliteRewards[] VALUES = values();
	
	TTEliteRewards(Item item, double rarity) {
		this.item = item;
		this.rarity = rarity;
	}
	
	public Item getItem() {
		return item;
	}
	
	public double getRarity() {
		return rarity;
	}
	
}
