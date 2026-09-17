package com.rs.game.player.actions.smithing.defs;

import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ForgingBar.java | 12:30:27 PM
 * @author Chryonic
 * @date Apr 15, 2017
 */
public enum ForgingBar {

	ADAMANT(2361, 40,
			new Item[] { new Item(1211, 1), new Item(1357, 1), new Item(1430, 1), new Item(1145, 1),
					new Item(9380, 1), new Item(1287, 1), new Item(823, 1), new Item(4823, 1), new Item(-1, 1),
					new Item(-1, 1), new Item(-1, 1), new Item(43, 1), new Item(1331, 1), new Item(9429, 1),
					new Item(1301, 1), new Item(867, 1), new Item(1161, 1), new Item(1183, 1), new Item(-1, 1),
					new Item(-1, 1), new Item(1345, 1), new Item(1371, 1), new Item(1111, 1), new Item(1199, 1),
					new Item(3100, 1), new Item(1317, 1), new Item(1091, 1), new Item(1073, 1), new Item(1123, 1),
					new Item(1271, 1) },
			new double[] { 62.5, 125, 187.5, 312.5 }, new int[] { 66, 210, 267 }),

	DRACONIC_VISAGE(11286, 90, new Item[] { new Item(11283, 1) }, new double[] { 2000.0 },
			new int[] { 66, 210, 267 }),

	BRONZE(2349, 0,
			new Item[] { new Item(1205, 1), new Item(1351, 1), new Item(1422, 1), new Item(1139, 1),
					new Item(9375, 1), new Item(1277, 1), new Item(819, 1), new Item(4819, 1), new Item(1794, 1),
					new Item(-1, 1), new Item(-1, 1), new Item(39, 1), new Item(1321, 1), new Item(9420, 1),
					new Item(1291, 1), new Item(864, 1), new Item(1155, 1), new Item(1173, 1), new Item(-1, 1),
					new Item(-1, 1), new Item(1337, 1), new Item(1375, 1), new Item(1103, 1), new Item(1189, 1),
					new Item(3095, 1), new Item(1307, 1), new Item(1087, 1), new Item(1075, 1), new Item(1117, 1),
					new Item(1265, 1) },
			new double[] { 12.5, 25, 37.5, 62.5 }, new int[] { 66, 82, 210, 267 }),

	IRON(2351, 10,
			new Item[] { new Item(1203, 1), new Item(1349, 1), new Item(1420, 1), new Item(1137, 1),
					new Item(9377, 1), new Item(1279, 1), new Item(820, 1), new Item(4820, 1), new Item(-1, 1),
					new Item(7225, 1), new Item(-1, 1), new Item(40, 1), new Item(1323, 1), new Item(9423, 1),
					new Item(1293, 1), new Item(863, 1), new Item(1153, 1), new Item(1175, 1), new Item(4540, 1),
					new Item(-1, 1), new Item(1335, 1), new Item(1363, 1), new Item(1101, 1), new Item(1191, 1),
					new Item(3096, 1), new Item(1309, 1), new Item(1081, 1), new Item(1067, 1), new Item(1115, 1),
					new Item(1267, 1) },
			new double[] { 25, 50, 75, 125 }, new int[] { 66, 90, 162, 210, 267 }),

	MITHRIL(2359, 30,
			new Item[] { new Item(1209, 1), new Item(1355, 1), new Item(1428, 1), new Item(1143, 1),
					new Item(9379, 1), new Item(1285, 1), new Item(822, 1), new Item(4822, 1), new Item(-1, 1),
					new Item(-1, 1), new Item(-1, 1), new Item(42, 1), new Item(1329, 1), new Item(9427, 1),
					new Item(1299, 1), new Item(866, 1), new Item(1159, 1), new Item(1181, 1), new Item(-1, 1),
					new Item(9416, 1), new Item(1343, 1), new Item(1369, 1), new Item(1109, 1), new Item(1197, 1),
					new Item(3099, 1), new Item(1315, 1), new Item(1085, 1), new Item(1071, 1), new Item(1121, 1),
					new Item(1273, 1) },
			new double[] { 50, 100, 150, 250 }, new int[] { 66, 170, 210, 267 }),

	ELDER(44844, 90,
			new Item[] {
					new Item(45549), new Item(45554), new Item(45559), new Item(45564), new Item(45569), new Item(45574),
					new Item(45580), new Item(45585), new Item(45590), new Item(45595), new Item(45600), new Item(45605),
					new Item(45611), new Item(45616), new Item(45621), new Item(45626), new Item(45631), new Item(45636),
					new Item(44797), new Item(45642), new Item(45644), new Item(45646), new Item(45648), new Item(45650), new Item(45652),
					new Item(45655), new Item(45660), new Item(45665), new Item(45670), new Item(45675), new Item(45680),
					new Item(45686), new Item(45691), new Item(45696), new Item(45701), new Item(45706), new Item(45711),
					new Item(45717), new Item(45722), new Item(45727), new Item(45732), new Item(45737), new Item(45742),
					new Item(45748), new Item(45753), new Item(45758), new Item(45763), new Item(45768), new Item(45773),
					new Item(45779), new Item(45784), new Item(45789), new Item(45794), new Item(45799), new Item(45804),
					new Item(45810), new Item(45815), new Item(45820), new Item(45825), new Item(45830), new Item(45835)
			},
			new double[] { 500, 1000, 1500, 2500 }, new int[] { 66, 90, 162, 210, 267 }),

	ORIKALKUM(44838, 60,
			new Item[] {
					new Item(46539), new Item(46542), new Item(46545), new Item(46548),
					new Item(46552), new Item(46555), new Item(46558), new Item(46561),
					new Item(46565), new Item(46568), new Item(46571), new Item(46574),
					new Item(44791), new Item(46578), new Item(46581), new Item(46584), new Item(46587),
					new Item(46591), new Item(46594), new Item(46597), new Item(46600),
					new Item(46604), new Item(46607), new Item(46610), new Item(46613),
					new Item(46617), new Item(46620), new Item(46623), new Item(46626),
					new Item(46630), new Item(46633), new Item(46636), new Item(46639),
					new Item(46643), new Item(46646), new Item(46649), new Item(46652),
					new Item(46656), new Item(46659), new Item(46662), new Item(46665)
			},
			new double[] { 70, 140, 210, 350 }, new int[] { 66, 120, 180, 240 }),

	NECRONIUM(44840, 70,
			new Item[] {
					new Item(46294), new Item(46299), new Item(46304), new Item(46309), new Item(46314),
					new Item(46320), new Item(46325), new Item(46330), new Item(46335), new Item(46340),
					new Item(46346), new Item(46351), new Item(46356), new Item(46361), new Item(46366),
					new Item(44793), new Item(46372), new Item(46374), new Item(46376), new Item(46378), new Item(46380),
					new Item(46383), new Item(46388), new Item(46393), new Item(46398), new Item(46403),
					new Item(46409), new Item(46414), new Item(46419), new Item(46424), new Item(46429),
					new Item(46435), new Item(46440), new Item(46445), new Item(46450), new Item(46455),
					new Item(46461), new Item(46466), new Item(46471), new Item(46476), new Item(46481),
					new Item(46487), new Item(46492), new Item(46497), new Item(46502), new Item(46507),
					new Item(46513), new Item(46518), new Item(46523), new Item(46528), new Item(46533)
			},
			new double[] { 300, 600, 900, 1500 }, new int[] { 66, 135, 195, 255 }),

	BANE(44842, 80,
			new Item[] {
					new Item(45076), new Item(45081), new Item(45086), new Item(45091), new Item(45096),
					new Item(45102), new Item(45107), new Item(45112), new Item(45117), new Item(45122),
					new Item(45128), new Item(45133), new Item(45138), new Item(45143), new Item(45148),
					new Item(44795), new Item(45154), new Item(45156), new Item(45158), new Item(45160), new Item(45162),
					new Item(45165), new Item(45170), new Item(45175), new Item(45180), new Item(45185),
					new Item(45191), new Item(45196), new Item(45201), new Item(45206), new Item(45211),
					new Item(45217), new Item(45222), new Item(45227), new Item(45232), new Item(45237),
					new Item(45243), new Item(45248), new Item(45253), new Item(45258), new Item(45263),
					new Item(45269), new Item(45274), new Item(45279), new Item(45284), new Item(45289),
					new Item(45295), new Item(45300), new Item(45305), new Item(45310), new Item(45315)
			},
			new double[] { 350, 700, 1050, 1750 }, new int[] { 66, 150, 210, 270 }),

	MASTERWORK(45991, 90,
			new Item[] {
					new Item(45955), // Masterwork helm
					new Item(45960), // Masterwork gloves
					new Item(45965), // Masterwork boots
					new Item(45970), // Masterwork platebody
					new Item(45975)  // Masterwork platelegs
					// Masterwork is armor only
			},
			new double[] { 400, 800, 1200, 2000 }, new int[] { 66, 90, 162, 210, 267 }),
	// -------------------------------------------------------

	RUNE(2363, 50,
			new Item[] { new Item(1213, 1), new Item(1359, 1), new Item(1432, 1), new Item(1147, 1),
					new Item(9381, 1), new Item(1289, 1), new Item(824, 1), new Item(4824, 1), new Item(-1, 1),
					new Item(-1, 1), new Item(-1, 1), new Item(44, 1), new Item(1333, 1), new Item(9431, 1),
					new Item(1303, 1), new Item(868, 1), new Item(1163, 1), new Item(1185, 1), new Item(805, 1),
					new Item(25908, 1), new Item(1347, 1), new Item(1373, 1), new Item(1113, 1), new Item(1201, 1),
					new Item(3101, 1), new Item(1319, 1), new Item(1093, 1), new Item(1079, 1), new Item(1127, 1),
					new Item(1275, 1) },
			new double[] { 75, 150, 225, 375 }, new int[] { 66, 210, 267 }),

	STEEL(2353, 20,
			new Item[] { new Item(1207, 1), new Item(1353, 1), new Item(1424, 1), new Item(1141, 1),
					new Item(9378, 1), new Item(1281, 1), new Item(821, 1), new Item(1539, 1), new Item(-1, 1),
					new Item(-1, 1), new Item(2370, 1), new Item(41, 1), new Item(1325, 1), new Item(9425, 1),
					new Item(1295, 1), new Item(865, 1), new Item(1157, 1), new Item(1177, 1), new Item(4544, 1),
					new Item(-1, 1), new Item(1339, 1), new Item(1365, 1), new Item(1105, 1), new Item(1193, 1),
					new Item(3097, 1), new Item(1311, 1), new Item(1083, 1), new Item(1069, 1), new Item(1119, 1),
					new Item(1269, 1) },
			new double[] { 37.5, 75, 112.5, 187.5 }, new int[] { 66, 98, 162, 210, 267 }),

	DRAGONBANE(21783, 80,
			new Item[]{ new Item(-1, 1), new Item(-1, 1), new Item(-1, 1), new Item(-1, 1), new Item(21823, 1),
					new Item(-1, 1), new Item(-1, 1), new Item(-1, 1), new Item(-1, 1), new Item(-1, 1), new Item(-1, 1), new Item(21843, 1) }, new double[] { 62.5 }, new int[] {  });

	private static final Map<Integer, ForgingBar> bars = new HashMap<Integer, ForgingBar>();

	static {
		for (ForgingBar bar : ForgingBar.values())
			bars.put(bar.getBarId(), bar);
	}

	private final int barId;
	private final int[] componentChilds;
	private final double[] experience;
	private final Item[] items;
	private final int level;

	ForgingBar(int barId, int level, Item[] items, double[] experience, int[] componentChilds) {
		this.barId = barId;
		this.level = level;
		this.items = items;
		this.componentChilds = componentChilds;
		this.experience = experience;
	}

	public static ForgingBar forId(int id) {
		return bars.get(id);
	}

	public static ForgingBar getBar(Player player) {
		int smithLevel = player.getSkills().getLevel(Skills.SMITHING);
		ForgingBar bestInventoryBar = null;
		ForgingBar bestMetalBankBar = null;
		for (ForgingBar bar : ForgingBar.values()) {
			if (smithLevel < bar.getLevel())
				continue;
			if (player.getInventory().containsItem(bar.getBarId(), 1)) {
				if (bestInventoryBar == null || bar.getLevel() > bestInventoryBar.getLevel())
					bestInventoryBar = bar;
				continue;
			}
			if (player.getMetalBankAmount(bar.getBarId()) > 0 && (bestMetalBankBar == null || bar.getLevel() > bestMetalBankBar.getLevel()))
				bestMetalBankBar = bar;
		}
		return bestInventoryBar != null ? bestInventoryBar : bestMetalBankBar;
	}

	public static List<ForgingBar> getAvailableBars(Player player) {
		int smithLevel = player.getSkills().getLevel(Skills.SMITHING);
		List<ForgingBar> availableBars = new ArrayList<ForgingBar>();
		for (ForgingBar bar : ForgingBar.values()) {
			if (smithLevel < bar.getLevel() || !isSelectableBar(bar) || getAvailableAmount(player, bar) <= 0)
				continue;
			availableBars.add(bar);
		}
		return availableBars;
	}

	public static int getAvailableAmount(Player player, ForgingBar bar) {
		return player.getInventory().getAmountOf(bar.getBarId()) + player.getMetalBankAmount(bar.getBarId());
	}

	private static boolean isSelectableBar(ForgingBar bar) {
		String name = new Item(bar.getBarId()).getDefinitions().getName().toLowerCase();
		return name.endsWith(" bar");
	}

	public int getBarId() {
		return barId;
	}

	public int[] getComponentChilds() {
		return componentChilds;
	}

	public double[] getExperience() {
		return experience;
	}

	public Item[] getItems() {
		return items;
	}

	public int getLevel() {
		return level;
	}
}
