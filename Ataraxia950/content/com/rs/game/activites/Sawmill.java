package com.rs.game.activites;

import com.rs.game.player.Player;
import com.rs.game.player.content.construction.HouseConstants;
import com.rs.utils.InputIntegerEvent;

public class Sawmill {

	/**public static final int OVERSEER = 8904;
	private static final int SAWMILL_OPERATOR = 4250;

	public static Plank getPlankForLog(int id) {
		for (Plank plank : Plank.values())
			if (plank.logId == id)
				return plank;
		return null;
	}

	public static Plank getPlank(int id) {
		for (Plank plank : Plank.values())
			if (plank.id == id)
				return plank;
		return null;
	}

	public static boolean hasPlanksOrLogs(Player player) {
		for (Item item : player.getInventory().getItems().getItems()) {
			if (item != null && (getPlankForLog(item.getId()) != null || getPlank(item.getId()) != null))
				return true;
		}
		return false;
	}

	public static void openPlanksConverter(Player player) {
		player.getInterfaceManager().sendInterface(403);
	}

	public static void handlePlanksConvertButtons(Player player, int componentId, int packetId) {
		/**
		 * Wood
		 */
		/**if (componentId == 41)
			convertPlanks(player, Plank.WOOD, 1);
		if (componentId == 47)
			convertPlanks(player, Plank.WOOD, 5);
		if (componentId == 53)
			convertPlanks(player, Plank.WOOD, 10);
		if (componentId == 59) {
			player.sendInputInteger("Enter amount:", new InputIntegerEvent() {
				@Override
				public void run(Player player) {
					convertPlanks(player, Plank.WOOD, getInteger());
				}
			});
			return;
		}
		if (componentId == 65)
			convertPlanks(player, Plank.WOOD, player.getInventory().getAmountOf(Plank.WOOD.logId));
		/**
		 * Oak
		 */
	/**	if (componentId == 103)
			convertPlanks(player, Plank.OAK, 1);
		if (componentId == 95)
			convertPlanks(player, Plank.OAK, 5);
		if (componentId == 87)
			convertPlanks(player, Plank.OAK, 10);
		if (componentId == 79) {
			player.sendInputInteger("Enter amount:", new InputIntegerEvent() {
				@Override
				public void run(Player player) {
					convertPlanks(player, Plank.OAK, getInteger());
				}
			});
			return;
		}
		if (componentId == 71)
			convertPlanks(player, Plank.values()[1], player.getInventory().getAmountOf(Plank.OAK.logId));
		/**
		 * Teak
		 */
		/**if (componentId == 141)
			convertPlanks(player, Plank.TEAK, 1);
		if (componentId == 133)
			convertPlanks(player, Plank.TEAK, 5);
		if (componentId == 125)
			convertPlanks(player, Plank.TEAK, 10);
		if (componentId == 117) {
			player.sendInputInteger("Enter amount:", new InputIntegerEvent() {
				@Override
				public void run(Player player) {
					convertPlanks(player, Plank.TEAK, getInteger());
				}
			});
			return;
		}
		if (componentId == 109)
			convertPlanks(player, Plank.TEAK, player.getInventory().getAmountOf(Plank.TEAK.logId));
		/**
		 * Mahogany
		 */
		/**if (componentId == 179)
			convertPlanks(player, Plank.MAHOGANY, 1);
		if (componentId == 171)
			convertPlanks(player, Plank.MAHOGANY, 5);
		if (componentId == 163)
			convertPlanks(player, Plank.MAHOGANY, 10);
		if (componentId == 155) {
			player.sendInputInteger("Enter amount:", new InputIntegerEvent() {
				@Override
				public void run(Player player) {
					convertPlanks(player, Plank.MAHOGANY, getInteger());
				}
			});
			return;
		}
		if (componentId == 147)
			convertPlanks(player, Plank.MAHOGANY, player.getInventory().getAmountOf(Plank.MAHOGANY.logId));

	}

	public static void convertPlanks(Player player, Plank type, int amount) {
		int warning = 0;
		int logsAmt = player.getInventory().getAmountOf(type.logId);
		if (amount > logsAmt) {
			amount = logsAmt;
			warning = 1;
		}
		int cost = amount * type.cost;
		int invCoins = player.getInventory().getNumberOf(995);
		if (cost > invCoins) {
			amount = invCoins / type.cost;
			cost = amount * type.cost;
			warning = 2;
		}
		if (warning != 0)
			player.getPackets().sendGameMessage("You've run out of " + (warning == 1 ? "logs" : "coins") + ".");
		if (amount > 0) {
			player.takeMoney(cost);
			player.getInventory().deleteItem(type.logId, amount);
			player.getInventory().addItem(type.id, amount);
		}
		player.closeInterfaces();
		player.getDialogueManager().startDialogue("SimpleNPCMessage", SAWMILL_OPERATOR, "Ive done as many as I could.");
	}

	public static void enter(Player player, WorldObject object) {
		if (player.getSkills().getLevelForXp(Skills.WOODCUTTING) < 80) {
			player.getDialogueManager().startDialogue("SimpleNPCMessage", OVERSEER,
					"Sorry, we don't need inexperienced woodcutters.");
			return;
		}
		if (hasPlanksOrLogs(player)) {
			player.getDialogueManager().startDialogue("SimpleNPCMessage", OVERSEER,
					"Sorry, you can't bring any planks or logs in with you. You might get them muddled with ours.");
			return;
		}
		player.lock(2);
		player.addWalkSteps(object.getX() + 1, object.getY(), 1, false);
		player.getControlerManager().startControler("SawmillController");
	}*/
	
	public static final void handleInterface(final Player player, final int componentId, final int packetId) {
	    int index = componentId >= 41 && componentId <= 65 ? 0 : componentId >= 71 & componentId <= 103 ? 1 : componentId >= 109 && componentId <= 141 ? 2 : 3;
	    int amountIndex = index == 0 ? ((componentId - 41)/ 6) : index == 1 ? (4 - ((componentId - 71) / 8)) : index == 2 ? (4 - ((componentId - 109) / 8)) : (4 - ((componentId - 147) / 8));
	    int amount = amountIndex == 0 ? 1 : amountIndex == 1 ? 5 : amountIndex == 2 ? 10 : amountIndex == 3 ? -1 : 28;
		if (amount == -1) {
			player.sendInputInteger("How many logs would you like to convert?", new InputIntegerEvent() {
				@Override
				public void run(Player player) {
					convert(player, Plank.VALUES[index], getInteger());
				}
			});
			return;
		}
		convert(player, Plank.VALUES[index], amount);
	}
	
	public static final void convert(final Player player, final Plank plank, int amount) {
		final int inInventory = player.getInventory().getAmountOf(plank.logId);
		if (inInventory < amount)
			amount = inInventory;
		if (amount == 0) {
			player.sendMessage("You haven't got any logs to convert this.");
			return;
		}
		final int cost = plank.cost * amount;
		final int coinsInInventory = player.getMoneyPouch().getTotal();
		if (coinsInInventory < cost) {
			amount = coinsInInventory / plank.cost;
		}
		if (amount == 0) {
			player.sendMessage("You haven't got enough coins to purchase this service.");
			return;
		}
		player.getInventory().deleteItem(plank.logId, amount);
		player.getMoneyPouch().removeMoneyMisc(amount * plank.cost);
		player.getInventory().addItem(plank.id, amount);
		player.sendMessage("You converted " + amount + " x " + plank.toString().toLowerCase() + " planks.");
		return;
	}

	public static Plank getPlankForLog(int id) {
		for (Plank plank : Plank.values())
			if (plank.logId == id)
				return plank;
		return null;
	}
	
	public enum Plank {
		WOOD(HouseConstants.PLANK, 1511, 100),
		OAK(HouseConstants.OAK_PLANK, 1521, 250),
		TEAK(HouseConstants.TEAK_PLANK, 6333, 500),
		MAHOGANY(HouseConstants.MAHOGANY_PLANK, 6332, 1500);
		
		private static final Plank[] VALUES = values();

		private final int id;
        private final int logId;
        private final int cost;

		Plank(int id, int logId, int cost) {
			this.id = id;
			this.logId = logId;
			this.cost = cost;
		}

		public int getCost() {
			return cost;
		}

		public int getId() {
			return id;
		}
	}
}