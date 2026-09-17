package com.rs.game.player.actions;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.DungeonController;

public class WaterFilling extends Action {

	private final Fill fill;
	private int quantity;

	public WaterFilling(Fill fill, int quantity) {
		this.fill = fill;
		this.quantity = quantity;
	}

	public static Fill getFillByProduce(int produce) {
		for (Fill fill : Fill.values()) {
			if (fill.full == produce)
				return fill;
		}
		return null;
	}

	public static boolean isFilling(Player player, int empty, boolean isSpot) {
		for (Fill fill : Fill.values()) {
			if (fill.empty == empty) {
				if (isSpot && fill.ordinal() <= 4)
					return false;
				fill(player, fill);
				return true;
			}
		}
		return false;
	}

	private static void fill(Player player, Fill fill) {
		if (player.getInventory().getItems().getNumberOf(new Item(fill.empty, 1)) == 1)
			player.getActionManager().setAction(new WaterFilling(fill, 1));
		else {
			if (player.getControlerManager().getControler() instanceof DungeonController)
				player.getDialogueManager().startDialogue("DungeoneeringWaterFillingD", fill);
			else
				player.getDialogueManager().startDialogue("WaterFillingD", fill);
		}
	}

	public boolean checkAll(Player player) {
		if (!player.getInventory().containsOneItem(fill.empty)) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You don't have any "
					+ ItemDefinitions.getItemDefinitions(fill.empty).getName().toLowerCase() + "s to fill.");
			return false;
		}
		return true;
	}

	@Override
	public boolean start(Player player) {
		if (checkAll(player)) {
			setActionDelay(player, 1);
			player.setNextAnimation(new Animation(832));
			String name = ItemDefinitions.getItemDefinitions(fill.full).getName();
			if (name.contains(" ("))
				name = name.substring(0, name.indexOf(" ("));
			if (player.getControlerManager().getControler() instanceof DungeonController)
				player.getPackets().sendGameMessage("You fill the vial with water.", true);
			else
				player.getPackets().sendGameMessage("You fill the " + name + ".", true);
			return true;
		}
		return false;
	}

	@Override
	public boolean process(Player player) {
		return checkAll(player);
	}

	@Override
	public int processWithDelay(Player player) {
		player.getInventory().deleteItem(fill.empty, 1);
		player.getInventory().addItem(fill.full, 1);
		quantity--;
		if (quantity <= 0)
			return -1;
		player.setNextAnimation(new Animation(832));
		return 0;
	}

	@Override
	public void stop(final Player player) {
		setActionDelay(player, 3);
	}

	public enum Fill {
		VIAL(229, 227),
		BOWL(1923, 1921),
		BUCKET(1925, 1929),
		BUCKETB(3727, 2953),
		JUG(1937, 1935),
		VASE(3734, 3735),
		PLANT_POT(5350, 5354),
		WATERSKIN1(1825, 1823),
		WATERSKIN2(1827, 1823),
		WATERSKIN3(1829, 1823),
		WATERSKIN4(1831, 1823),
		CUP(7728, 4458),
		CLAY(434, 1761),
		WATERING_CAN1(5333, 5340),
		WATERING_CAN2(5331, 5340),
		WATERING_CAN3(5334, 5340),
		WATERING_CAN4(5335, 5340),
		WATERING_CAN5(5336, 5340),
		WATERING_CAN6(5337, 5340),
		WATERING_CAN7(5338, 5340),
		WATERING_CAN8(5339, 5340),
		DUNGEONEERING_VIAL(17490, 17492);

		private final int empty;
        private final int full;

		Fill(int empty, int full) {
			this.empty = empty;
			this.full = full;
		}

		public int getEmpty() {
			return empty;
		}

		public int getFull() {
			return full;
		}

	}
}