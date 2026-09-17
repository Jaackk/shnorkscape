package com.rs.game.player.tt;

import com.rs.game.item.Item;

/**
 * @author Kris
 * Using 'amount' field as minimum amount and 'charges' field as maximum amount.
 * Up to 3 'TTH.ALL' rewards at a time.
 * Excluded heraldic armours - No one wants that shit.
 */
public enum TTMediumRewards {

	ADAMANT_CROSSBOW(new Item(9183, 1), 70),
	ADAMANT_LONGSWORD(new Item(1301, 1), 70),
	ADAMANT_2H_SWORD(new Item(1317, 1), 70),
	ADAMANT_FULL_HELM(new Item(1161, 1), 50),
	ADAMANT_PLATEBODY(new Item(1123, 1), 50),
	ADAMANT_PLATELEGS(new Item(1073, 1), 50),
	ADAMANT_KITESHIELD(new Item(1199, 1), 50),
	ADAMANT_ARROWS(new Item(890, 10, 80), 65),
	ADAMANT_BOLTS(new Item(9143, 10, 80), 65),
	GREEN_DRAGONHIDE_BODY(new Item(1135, 1), 75),
	GREEN_DRAGONHIDE_CHAPS(new Item(1099, 1), 75),
	AIR_BATTLESTAFF(new Item(1397, 1), 70),
	WATER_BATTLESTAFF(new Item(1395, 1), 70),
	EARTH_BATTLESTAFF(new Item(1399, 1), 70),
	FIRE_BATTLESTAFF(new Item(1393, 1), 70),
	AIR_RUNE(new Item(556, 300), 60),
	MIND_RUNE(new Item(558, 300), 60),
	WATER_RUNE(new Item(555, 300), 60),
	EARTH_RUNE(new Item(557, 300), 60),
	FIRE_RUNE(new Item(554, 300), 60),
	LAW_RUNE(new Item(563, 20, 75), 70),
	NATURE_RUNE(new Item(561, 20, 75), 70),
	PURE_ESSENCE(new Item(7937, 300), 80),
	TEAK_PLANKS(new Item(8781, 4), 80),
	GREEN_DRAGONHIDE(new Item(1754, 5), 80),
	WILLOW_COMPOSITE_BOW(new Item(10280, 1), 20),
	YEW_COMPOSITE_BOW(new Item(10282, 1), 20),
	MAGIC_COMPOSITE_BOW(new Item(10284, 1), 20), 
	ADAMANT_FULL_HELM_T(new Item(2605, 1), 10),
	ADAMANT_PLATEBODY_T(new Item(2599, 1), 10),
	ADAMANT_PLATELEGS_T(new Item(2601, 1), 10),
	ADAMANT_KITESHIELD_T(new Item(2603, 1), 10),
	ADAMANT_PLATESKIRT_T(new Item(3474, 1), 10),
	ADAMANT_FULL_HELM_G(new Item(2613, 1), 10),
	ADAMANT_PLATEBODY_G(new Item(2607, 1), 10),
	ADAMANT_PLATELEGS_G(new Item(2609, 1), 10),
	ADAMANT_KITESHIELD_G(new Item(2611, 1), 10),
	ADAMANT_PLATESKIRT_G(new Item(3475, 1), 10),
	ADAMANT_H_7334(new Item(7334, 1), 10),
	ADAMANT_H_7340(new Item(7340, 1), 10),
	ADAMANT_H_7346(new Item(7346, 1), 10),
	ADAMANT_H_7352(new Item(7352 ,1), 10),
	ADAMANT_H_7358(new Item(7358, 1), 10),
	ADAMANT_H_10296(new Item(10296, 1), 10),
	ADAMANT_H_10298(new Item(10298, 1), 10),
	ADAMANT_H_10300(new Item(10300, 1), 10),
	ADAMANT_H_10302(new Item(10302, 1), 10),
	ADAMANT_H_10304(new Item(10304, 1), 10),
	ADAMANT_H_19173(new Item(19173, 1), 10),
	ADAMANT_H_19175(new Item(19175, 1), 10),
	ADAMANT_H_19177(new Item(19177, 1), 10),
	ADAMANT_H_19194(new Item(19194, 1), 10),
	ADAMANT_H_19196(new Item(19196, 1), 10),
	ADAMANT_H_19198(new Item(19198, 1), 10),
	ADAMANT_H_19215(new Item(19215, 1), 10),
	ADAMANT_H_19217(new Item(19217, 1), 10),
	ADAMANT_H_19219(new Item(19219, 1), 10),
	ADAMANT_H_19236(new Item(19236, 1), 10),
	ADAMANT_H_19238(new Item(19238, 1), 10),
	ADAMANT_H_19240(new Item(19240, 1), 10),
	ADAMANT_H_10400(new Item(10400, 1), 10),
	ADAMANT_H_10402(new Item(10402, 1), 10),
	ADAMANT_H_10420(new Item(10420, 1), 10),
	ADAMANT_H_10422(new Item(10422, 1), 10),
	RANGER_BOOTS(new Item(2577, 1), 10),
	WIZARD_BOOTS(new Item(2579, 1), 10),
	BLACK_HEADBAND(new Item(2647, 1), 8),
	RED_HEADBAND(new Item(2645, 1), 8),
	BROWN_HEADBAND(new Item(2649, 1), 8),
	RED_BOATER(new Item(7319, 1), 8),
	ORANGE_BOATER(new Item(7321, 1), 8),
	BLUE_BOATER(new Item(7325, 1), 8),
	GREEN_BOATER(new Item(7323, 1), 8),
	BLACK_BOATER(new Item(7327, 1), 8),
	GREEN_DHIDE_BODY_T(new Item(7372, 1), 10),
	GREEN_DHIDE_CHAPS_T(new Item(7380, 1), 10),
	GREEN_DHIDE_BODY_G(new Item(7370, 1), 10),
	GREEN_DHIDE_CHAPS_G(new Item(7378, 1), 10),
	BLACK_ELEGANT_SHIRT(new Item(10400, 1), 5),
	BLACK_ELEGANT_LEGS(new Item(10402, 1), 5),
	WHITE_ELEGANT_BLOUSE(new Item(10420, 1), 5),
	WHITE_ELEGANT_SKIRT(new Item(10422, 1), 5),
	PURPLE_ELEGANT_SHIRT(new Item(10416, 1), 5),
	PURPLE_ELEGANT_LEGS(new Item(10418, 1), 5),
	PURPLE_ELEGANT_BLOUSE(new Item(10436, 1), 5),
	PURPLE_ELEGANT_SKIRT(new Item(10438, 1), 5),
	SARADOMIN_MITRE(new Item(10452, 1), 10),
	GUTHIX_MITRE(new Item(10454, 1), 10),
	ZAMORAK_MITRE(new Item(10456, 1), 10),
	SARADOMIN_CLOAK(new Item(10446, 1), 10),
	GUTHIX_CLOAK(new Item(10448, 1), 10),
	ZAMORAK_CLOAK(new Item(10450, 1), 10),
	ARMADYL_ROBE_TOP(new Item(19380, 1), 10),
	ARMADYL_ROBE_LEGS(new Item(19386, 1), 10),
	BANDOS_ROBE_TOP(new Item(19384, 1), 10),
	BANDOS_ROBE_LEGS(new Item(19338, 1), 10),
	ANCIENT_ROBE_TOP(new Item(19382, 1), 10),
	ANCIENT_ROBE_LEGS(new Item(19390, 1), 10),
	PENGUIN_MASK(new Item(13109, 1), 3),
	SHEEP_MASK(new Item(13107, 1), 3),
	BAT_MASK(new Item(13111, 1), 3),
	CAT_MASK(new Item(13113, 1), 3),
	WOLF_MASK(new Item(13115, 1), 3),
	STRENGTH_AMULET_T(new Item(10364, 1), 5),
	ADAMANT_CANE(new Item(13097, 1), 5),
	PITH_HELMET(new Item(13103, 1), 8),
	BRIEFCASE(new Item(33512), 10),
	OFFHAND_BRIEFCASE(new Item(33514), 10); 
	
	private final Item item;
	private final double rarity;
	
	public static final TTMediumRewards[] VALUES = values();
	
	TTMediumRewards(Item item, double rarity) {
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
