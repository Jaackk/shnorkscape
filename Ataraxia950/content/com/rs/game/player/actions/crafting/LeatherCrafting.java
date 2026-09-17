package com.rs.game.player.actions.crafting;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.activites.halloween.ClueScrollDistributor;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.actions.Action;
import com.rs.game.player.content.packs.portable.PortableType;
import com.rs.game.player.content.petperks.PetPerkHandler;
import com.rs.game.player.content.skillingcontracts.impl.CraftingContractList;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Fuck you, I made this. Used to handle Leather crafting.
 *
 * @author Noel
 */
public class LeatherCrafting extends Action {


	public static final int STRIP_OF_CLOTH = 3224;

	// Wizard set (RS3 ids you gave)
	public static final int WIZARD_HAT = 579;
	public static final int WIZARD_ROBE_TOP = 577;
	public static final int WIZARD_ROBE_BOTTOM = 1011;
	public static final int WIZARD_BOOTS = 41225;
	public static final int WIZARD_GLOVES = 25873;

	public static final Item NEEDLE = new Item(1733);
	public static final Item THREAD = new Item(1734);
	public static final int[] LEATHER = {
			1741, 1745, 2505, 2507, 2509, 6289, 24374,
			25545, // Imphide
			25547, // Spider Silk
			25551, // Carapace
			25549, //batwing
			3224
	};


	public static final int[][] PRODUCTS = {
			{ 1169, 1129, 1095, 1059, 1061 }, // Leather
			{ 1065, 1099, 1135 },             // Green d'hide
			{ 2487, 2493, 2499 },             // Blue d'hide
			{ 2489, 2495, 2501 },             // Red d'hide
			{ 2491, 2497, 2503 },             // Black d'hide
			{ 6326, 6322, 6324, 6328, 6330 }, // Snakeskin
			{ 24376, 24379, 24382 },          // Royal d'hide

			{ 25845, 25847, 25849, 25851, 25853 }, // Imphide
			{ 25835, 25837, 25839, 25841, 25843 }, // Spider Silk
			{ 25857, 25859, 25861, 25863, 25865 }, // Carapace
			{ 25825, 25827, 25831, 25833, 25829 },  // Batwing (note: gloves/boots swapped order)
			{ WIZARD_HAT, WIZARD_ROBE_TOP, WIZARD_ROBE_BOTTOM, WIZARD_GLOVES, WIZARD_BOOTS }
	};


	public final Animation CRAFT_ANIMATION = new Animation(25594);

	private final LeatherData data;
	private final boolean portable;

	private int quantity;
	private int removeThread = 5;

	public LeatherCrafting(LeatherData data, int quantity, boolean portable) {
		this.data = data;
		this.quantity = quantity;
		this.portable = portable;
	}

	public static int getIndex(Player player) {
		int leather = (Integer) player.getTemporaryAttributtes().get("leatherType");
		for (int i = 0; i < LEATHER.length; i++) {
			if (leather == LEATHER[i]) {
				return i;
			}
		}
		return -1;
	}


	public static boolean handleItemOnItem(Player player, Item itemUsed, Item usedWith) {
		for (int i = 0; i < LEATHER.length; i++) {
			if (itemUsed.getId() == LEATHER[i] || usedWith.getId() == LEATHER[i]) {


				player.getTemporaryAttributtes().put("leatherType", LEATHER[i]);
				int index = getIndex(player);
				if (index == -1)
					return true;

				CraftingRs3Dialogue.sendLeatherInterface(player, LEATHER[i], false);
				return true;
			}
		}
		return false;
	}

	public static void performPortableAction(Player player, WorldObject object) {
		ArrayList<Integer> possibilities = new ArrayList<Integer>();
		for (LeatherData leather : LeatherData.values()) {
			if (player.getInventory().containsItem(leather.getLeatherId(), 1))
				possibilities.add(leather.getLeatherId());
		}

		if (possibilities.isEmpty()) {
			player.sendMessage("You do not have any leather to craft.");
			return;
		}

		Item item = getPreferredItemToUse(player, possibilities);
		if (item == null) {
			player.sendMessage("You do not have any leather to craft.");
			return;
		}

		if (handlePortable(player, item, NEEDLE, object))
			return;

		player.sendMessage("You do not have any leather to craft.");
	}

	private static Item getPreferredItemToUse(Player player, ArrayList<Integer> ints) {
		Item temp = null;
		for (int i : ints) {
			if (temp == null || player.getInventory().getNumberOf(i) > temp.getAmount())
				temp = new Item(i, player.getInventory().getNumberOf(i));
		}
		return temp;
	}

	public static boolean handlePortable(Player player, Item itemUsed, Item usedWith, WorldObject object) {
		for (int i = 0; i < LEATHER.length; i++) {
			if (itemUsed.getId() == LEATHER[i] || usedWith.getId() == LEATHER[i]) {


				player.getTemporaryAttributtes().put("leatherType", LEATHER[i]);
				int index = getIndex(player);
				if (index == -1)
					return true;


				boolean isPortable = PortableType.isPortableObject(object.getId());

				CraftingRs3Dialogue.sendLeatherInterface(player, LEATHER[i], isPortable);
				return true;
			}
		}
		return false;
	}

	private boolean checkAll(Player player) {
		if (player.getInterfaceManager().containsScreenInter() || player.getInterfaceManager().containsInventoryInter()) {
			player.sendMessage("Please finish what you're doing before doing this action.");
			return false;
		}
		if (data.getRequiredLevel() > player.getSkills().getLevel(Skills.CRAFTING)) {
			player.sendMessage("You need a crafting level of " + data.getRequiredLevel() + " to craft this hide.");
			return false;
		}
		if (player.getInventory().getItems().getNumberOf(data.getLeatherId()) < data.getLeatherAmount()) {
			player.sendMessage("You don't have enough amount of hides in your inventory.");
			return false;
		}
		if (!player.getInventory().getItems().containsOne(THREAD) && !player.getPerkManager().hasPerkActive(DonationPerk.DELICATE_CRAFTSMAN)) {
			player.sendMessage("You need some thread to do this.");
			return false;
		}
		if (!player.getInventory().containsOneItem(NEEDLE.getId())) {
			player.sendMessage("You need a needle to craft leather.");
			return false;
		}
		if (!player.getInventory().containsOneItem(data.getLeatherId())) {
			player.sendMessage("You've ran out of " + ItemDefinitions.getItemDefinitions(data.getLeatherId()).getName().toLowerCase() + ".");
			return false;
		}
		if (player.clickedObject != null) {
			return World.containsObjectWithId(player.clickedObject, player.clickedObject.getId());
		}
		return true;
	}

	@Override
	public boolean process(Player player) {
		return checkAll(player);
	}

	@Override
	public int processWithDelay(Player player) {
		if(PetPerkHandler.handleEfficiencyExpert(player, new Item(data.getLeatherId(), data.getLeatherAmount()))) {
		      player.addItem(data.getFinalProduct(), 1);
		} else {
	          player.getInventory().deleteItem(data.getLeatherId(), data.getLeatherAmount());
	          player.getInventory().addItem(data.getFinalProduct(), 1);
		}
		player.getSkills().addXp(Skills.CRAFTING, data.getExperience() * (player.getPerkManager().hasPerkActive(DonationPerk.DELICATE_CRAFTSMAN) ? 1.25 : 1) * (portable ? 1.1 : 1));
		player.addItemsMade();
		if (portable && Utils.random(9) == 4) {
			player.getBank().addItem(new Item(data.getLeatherId(), data.getLeatherAmount()), true);
			player.sendMessage(Colors.GOLD + "<shad=000000>The portable crafter saves you some resources. They have been sent to your bank.", true);
		}
		player.sendMessage("You make a " + data.getName().toLowerCase() + "; " + "items crafted: " + Colors.RED + Utils.getFormattedNumber(player.getItemsMade()) + "</col>.", true);
		ClueScrollDistributor.givePlayerClueScrollIfProbable(player, ClueScrollDistributor.SKILLING_PERCENT);
		CraftingContractList.listenArmor(player, data);
		quantity--;
		if (!player.getPerkManager().hasPerkActive(DonationPerk.DELICATE_CRAFTSMAN)) {
			removeThread--;
			if (removeThread == 0) {
				removeThread = Utils.random(2, 6);
				player.getInventory().removeItems(THREAD);
				player.sendMessage("You use up a reel of your thread.", true);
			}
		}
		if (quantity <= 0)
			return -1;
		player.setNextAnimation(CRAFT_ANIMATION);
		return 3;
	}

	@Override
	public boolean start(Player player) {
		if (!checkAll(player))
			return false;
		setActionDelay(player, 1);
		player.setNextAnimation(CRAFT_ANIMATION);
		return true;
	}

	@Override
	public void stop(final Player player) {
		setActionDelay(player, 3);
		player.clickedObject = null;
	}

	public enum LeatherData {

		LEATHER_COIF(1741, 3, 1169, 38, 61),

		LEATHER_BOOTS(1741, 1, 1061, 7, 16.25),

		LEATHER_VAMBS(1741, 1, 1063, 11, 13.75),

		LEATHER_COWL(1741, 2, 1167, 11, 22),

		LEATHER_GLOVES(1741, 1, 1059, 1, 13.75),

		LEATHER_CHAPS(1741, 2, 1095, 18, 27),

		LEATHER_BODY(1741, 3, 1129, 14, 25),

		GREEN_D_HIDE_VAMBS(1745, 1, 1065, 57, 62),

		GREEN_D_HIDE_CHAPS(1745, 2, 1099, 60, 124),

		GREEN_D_HIDE_BODY(1745, 3, 1135, 63, 186),

		BLUE_D_HIDE_VAMBS(2505, 1, 2487, 66, 70),

		BLUE_D_HIDE_CHAPS(2505, 2, 2493, 68, 140),

		BLUE_D_HIDE_BODY(2505, 3, 2499, 71, 210),

		RED_D_HIDE_VAMBS(2507, 1, 2489, 73, 78),

		RED_D_HIDE_CHAPS(2507, 2, 2495, 75, 156),

		RED_D_HIDE_BODY(2507, 3, 2501, 77, 234),

		BLACK_D_HIDE_VAMBS(2509, 1, 2491, 79, 86),

		BLACK_D_HIDE_CHAPS(2509, 2, 2497, 82, 172),

		BLACK_D_HIDE_BODY(2509, 3, 2503, 84, 258),

		SNAKESKIN_BANDANA(6289, 5, 6326, 48, 45),

		SNAKESKIN_BODY(6289, 15, 6322, 53, 55),

		SNAKESKIN_CHAPS(6289, 12, 6324, 51, 50),

		SNAKESKIN_BOOTS(6289, 6, 6328, 45, 30),

		SNAKESKIN_VAMBRACES(6289, 8, 6330, 47, 35),

		ROYAL_D_HIDE_BODY(24374, 3, 24382, 93, 282),

		ROYAL_D_HIDE_VAMBS(24374, 1, 24376, 87, 94),

		ROYAL_D_HIDE_CHAPS(24374, 2, 24379, 89, 188),

		IMPHIDE_HOOD(25545, 1, 25845, 10, 30),
		IMPHIDE_ROBE_TOP(25545, 3, 25847, 12, 50),
		IMPHIDE_ROBE_BOTTOM(25545, 2, 25849, 11, 40),
		IMPHIDE_GLOVES(25545, 1, 25851, 8, 20),
		IMPHIDE_BOOTS(25545, 1, 25853, 9, 20),

		SPIDER_SILK_HOOD(25547, 1, 25835, 20, 40),
		SPIDER_SILK_TOP(25547, 3, 25837, 22, 60),
		SPIDER_SILK_BOTTOM(25547, 2, 25839, 21, 50),
		SPIDER_SILK_BOOTS(25547, 1, 25841, 18, 25),
		SPIDER_SILK_GLOVES(25547, 1, 25843, 19, 25),

		CARAPACE_HELM(25551, 1, 25857, 30, 60),
		CARAPACE_TORSO(25551, 3, 25859, 32, 90),
		CARAPACE_LEGS(25551, 2, 25861, 31, 70),
		CARAPACE_BOOTS(25551, 1, 25863, 28, 30),
		CARAPACE_GLOVES(25551, 1, 25865, 29, 30),

		BATWING_HOOD(25549, 1, 25825, 40, 70),
		BATWING_TORSO(25549, 3, 25827, 42, 100),
		BATWING_LEGS(25549, 2, 25831, 41, 80),
		BATWING_BOOTS(25549, 1, 25833, 38, 35),
		BATWING_GLOVES(25549, 1, 25829, 39, 35),

		WIZARD_HAT_CLOTH(STRIP_OF_CLOTH, 2, WIZARD_HAT, 7, 30),
		WIZARD_ROBE_TOP_CLOTH(STRIP_OF_CLOTH, 4, WIZARD_ROBE_TOP, 8, 50),
		WIZARD_ROBE_BOTTOM_CLOTH(STRIP_OF_CLOTH, 3, WIZARD_ROBE_BOTTOM, 6, 40),
		WIZARD_GLOVES_CLOTH(STRIP_OF_CLOTH, 1, WIZARD_GLOVES, 5, 20),
		WIZARD_BOOTS_CLOTH(STRIP_OF_CLOTH, 1, WIZARD_BOOTS, 4, 20),
				;

		private static final Map<Integer, LeatherData> leatherItems = new HashMap<Integer, LeatherData>();

		static {
			for (LeatherData leather : LeatherData.values()) {
				leatherItems.put(leather.finalProduct, leather);
			}
		}

		private final int leatherId;
		private final int leatherAmount;
		private final int finalProduct;
		private final int requiredLevel;
		private final double experience;
		private final String name;

		LeatherData(int leatherId, int leatherAmount, int finalProduct, int requiredLevel, double experience) {
			this.leatherId = leatherId;
			this.leatherAmount = leatherAmount;
			this.finalProduct = finalProduct;
			this.requiredLevel = requiredLevel;
			this.experience = experience;
			this.name = ItemDefinitions.getItemDefinitions(getFinalProduct()).getName().replace("d'hide", "");
		}

		public static LeatherData forId(int id) {
			return leatherItems.get(id);
		}

		public double getExperience() {
			return experience;
		}

		public int getFinalProduct() {
			return finalProduct;
		}

		public int getLeatherAmount() {
			return leatherAmount;
		}

		public int getLeatherId() {
			return leatherId;
		}

		public String getName() {
			return name;
		}

		public int getRequiredLevel() {
			return requiredLevel;
		}
	}
}
