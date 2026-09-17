package com.rs.game.player.tt;

import com.rs.game.item.Item;

/**
 * @author Kris
 * Using 'amount' field as minimum amount and 'charges' field as maximum amount.
 * Up to 3 'TTH.ALL' rewards at a time.
 * Excluded heraldic armours - No one wants that shit.
 */
public enum TTEasyRewards {

	AIR_RUNE(new Item(556, 150), 85),
	EARTH_RUNE(new Item(557, 150), 85),
	WATER_RUNE(new Item(555, 150), 85),
	FIRE_RUNE(new Item(554, 150), 85),
	BLACK_CROSSBOW(new Item(13081, 1), 60),
	STAFF_OF_AIR(new Item(1381, 1), 50),
	STAFF_OF_WATER(new Item(1383, 1), 50),
	STAFF_OF_EARTH(new Item(1385, 1), 50),
	STAFF_OF_FIRE(new Item(1387,1 ), 50),
	MITHRIL_ARROW(new Item(888, 50), 80),
	BLACK_BOLTS(new Item(13083, 33), 75),
	AIR_TALISMAN(new Item(1438, 1), 50),
	EARTH_TALISMAN(new Item(1440, 1), 50),
	FIRE_TALISMAN(new Item(1442, 1), 50),
	WATER_TALISMAN(new Item(1444, 1), 50),
	MITHRIL_LONGSWORD(new Item(1299, 1), 60), 
	MITHRIL_2H_SWORD(new Item(1315, 1), 60),
	MITHRIL_FULL_HELM(new Item(1159, 1), 65),
	MITHRIL_PLATEBODY(new Item(1121, 1), 65),
	MITHRIL_PLATELEGS(new Item(1121, 1), 65),
	MITHRIL_KITESHIELD(new Item(1197, 1), 65),
	OAK_PLANK(new Item(8779, 20), 80),
	LAW_RUNE(new Item(563, 20), 70),
	NATURE_RUNE(new Item(561, 20), 70),
	HARD_LEATHER(new Item(1744, 10), 70),
	WILLOW_COMPOSITE_BOW(new Item(10280, 1), 20),
	YEW_COMPOSITE_BOW(new Item(10282, 1), 20),
	MAGIC_COMPOSITE_BOW(new Item(10284, 1), 20),
	BLACK_FULL_HELM_T(new Item(2587, 1), 10),
	BLACK_PLATEBODY_T(new Item(2583, 1), 10),
	BLACK_PLATELEGS_T(new Item(2585, 1), 10),
	BLACK_PLATESKIRT_T(new Item(3472, 1), 10),
	BLACK_KITESHIELD_T(new Item(2589, 1), 10),
	BLACK_FULL_HELM_G(new Item(2595, 1), 10),
	BLACK_PLATEBODY_G(new Item(2591, 1), 10),
	BLACK_PLATELEGS_G(new Item(2593, 1), 10), 
	BLACK_PLATESKIRT_G(new Item(3473, 1), 10),
	BLACK_KITESHIELD_G(new Item(2597, 1), 10),
	BLACK_7332(new Item(7332,1),10),
	BLACK_7338(new Item(7338,1),10),
	BLACK_7344(new Item(7344,1),10),
	BLACK_7350(new Item(7350,1),10),
	BLACK_7356(new Item(7356,1),10),
	BLACK_10306(new Item(10306,1),10),
	BLACK_10308(new Item(10308,1),10),
	BLACK_10310(new Item(10310,1),10),
	BLACK_10312(new Item(10312,1),10),
	BLACK_10314(new Item(10314,1),10),
	BLACK_19167(new Item(19167,1),10),
	BLACK_19169(new Item(19169,1),10),
	BLACK_19171(new Item(19171,1),10),
	BLACK_19188(new Item(19188,1),10),
	BLACK_19190(new Item(19190,1),10),
	BLACK_19192(new Item(19192,1),10),
	BLACK_119209(new Item(19209,1),10),
	BLACK_19211(new Item(19211,1),10),
	BLACK_19213(new Item(19213,1),10),
	BLACK_19230(new Item(19230,1),10),
	BLACK_19232(new Item(19232,1),10),
	BLACK_19234(new Item(19234,1),10),
	BLACK_19251(new Item(19251,1),10),
	BLACK_19253(new Item(19253,1),10),
	BLACK_19255(new Item(19255,1),10),
	BLACK_10404(new Item(10404,1),10),
	BLACK_10406(new Item(10406,1),10),
	BLACK_10424(new Item(10424,1),10),
	BLACK_10426(new Item(10426,1),10),
	BLUE_BERET(new Item(2633, 1), 5),
	BLACK_BERET(new Item(2635, 1), 5),
	WHITE_BERET(new Item(2637, 1), 5),
	HIGHWAYMAN_MASK(new Item(2631, 1), 3),
	WIZARD_HAT_T(new Item(7396, 1), 10),
	WIZARD_ROBE_TOP_T(new Item(7392, 1), 10),
	WIZARD_ROBE_SKIRT_T(new Item(7388, 1), 10),
	WIZARD_HAT_G(new Item(7394, 1), 10),
	WIZARD_ROBE_TOP_G(new Item(7390, 1), 10),
	WIZARD_ROBE_SKIRT_G(new Item(7386, 1), 10),
	STUDDED_BODY_T(new Item(7364, 1), 10),
	STUDDED_CHAPS_T(new Item(7368, 1), 10),
	STUDDED_BODY_G(new Item(7362, 1), 10),
	STUDDED_CHAPS_G(new Item(7366, 1), 10),
	BLUE_ELEGANT_SHIRT(new Item(10408, 1), 15),
	BLUE_ELEGANT_LEGS(new Item(10410, 1), 15),
	BLUE_ELEGANT_BLOUSE(new Item(10428, 1), 15),
	BLUE_ELEGANT_SKIRT(new Item(10430, 1), 15),
	GREEN_ELEGANT_SHIRT(new Item(10412, 1), 15),
	GREEN_ELEGANT_LEGS(new Item(10414, 1), 15),
	GREEN_ELEGANT_BLOUSE(new Item(10432, 1), 15),
	GREEN_ELEGANT_SKIRT(new Item(10434, 1), 15),
	RED_ELEGANT_SHIRT(new Item(10404, 1), 15),
	RED_ELEGANT_LEGS(new Item(10406, 1), 15),
	RED_ELEGANT_BLOUSE(new Item(10424, 1), 15),
	RED_ELEGANT_SKIRT(new Item(10426, 1), 15),
	BOB_SHIRT_1(new Item(10316, 1), 5),
	BOB_SHIRT_2(new Item(10318, 1), 5),
	BOB_SHIRT_3(new Item(10320, 1), 5),
	BOB_SHIRT_4(new Item(10322, 1), 5),
	BOB_SHIRT_5(new Item(10324, 1), 5 ),
	FLARED_TROUSERS(new Item(10394, 1), 10),
	SLEEPING_CAP(new Item(10398, 1), 10),
	A_POWDERED_WIG(new Item(10392, 1), 10),
	PANTALOONS(new Item(10396, 1), 10),
	SARADOMIN_ROBE_TOP(new Item(10458, 1), 8),
	SARADOMIN_ROBE_LEGS(new Item(10464, 1), 8),
	GUTHIX_ROBE_TOP(new Item(10462, 1), 8),
	GUTHIX_ROBE_LEGS(new Item(10466, 1), 8),
	ZAMORAK_ROBE_TOP(new Item(10460, 1), 8),
	ZAMORAK_ROBE_LEGS(new Item(10468, 1), 8),
	AMULET_OF_MAGIC_T(new Item(10366, 1), 3),
	BLACK_CANE(new Item(13095, 1), 10),
	SPIKED_HELMET(new Item(13105, 1), 15),
	SUITCASE(new Item(33508, 1), 10),
	OFFHAND_SUITCASE(new Item(33510), 10);
	
	private final Item item;
	private final double rarity;
	
	public static final TTEasyRewards[] VALUES = values();
	
	TTEasyRewards(Item item, double rarity) {
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
