package com.rs.game.player.content.artisansworkshop;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.artisansworkshop.ArtisansWorkShopConstants.CeremonialSword;
import com.rs.game.player.content.artisansworkshop.ArtisansWorkShopConstants.Ingot;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.io.Serializable;

public class ArtisansWorkShop implements Serializable {

	private static final long serialVersionUID = -4221033791357223136L;
	public static int currentInstructions = 0;
	public static int BRONZE = 0, IRON = 1, STEEL = 2, MITHRIL = 3, ADAMANT = 4, RUNE = 5;

	public static int getOreIndex(int itemId) {
		return itemId == 453 ? 0 : itemId == 440 ? 1 : itemId == 447 ? 2 : itemId == 449 ? 3 : itemId == 451 ? 4 : -1;
	}

	public static String getInstructionText() {
		return currentInstructions == 0 ? "Helmets" : currentInstructions == 1 ? "Boots" : currentInstructions == 2 ? "Chestplates" : "Gauntlets";
	}

	public static void processArtisansWorkShop() {
		int random = Utils.random(4);
		while (currentInstructions == random)
			random = Utils.random(4);
		currentInstructions = random;
	}

	private transient Player player;
	public int[] artisansWorkShopSupplies;
	private int artisansXPGained;
	private double respect;
	private CeremonialSwordPlan currentPlan;
	private boolean firstTime100, firstTime90;

	public ArtisansWorkShop() {
		artisansWorkShopSupplies = new int[5];
	}

	public void withdrawDepositOre(int itemId, int amount, boolean deposit) {
		int oreIndex = ArtisansWorkShop.getOreIndex(itemId);
		ItemDefinitions defs = ItemDefinitions.getItemDefinitions(itemId);
		if (deposit) {
			int invAmount = player.getInventory().getAmountOf(itemId) + player.getInventory().getAmountOf(defs.getCertId());
			if (amount > invAmount)
				amount = invAmount;
			if (artisansWorkShopSupplies[oreIndex] == (oreIndex == 0 ? 8000 : 4000)) {
				player.getPackets().sendGameMessage("The machine can't hold any more " + defs.getName() + ".");
				return;
			}
			if (artisansWorkShopSupplies[oreIndex] + amount > (oreIndex == 0 ? 8000 : 4000)) {
				amount = (oreIndex == 0 ? 8000 : 4000) - artisansWorkShopSupplies[oreIndex];
				player.getPackets().sendGameMessage("You couldn't deposit all of the ores you wanted.(machine is full)");
			}
			if (amount == 0) {
				player.getPackets().sendGameMessage("You don't have any " + defs.getName() + " left.");
				return;
			}
			artisansWorkShopSupplies[oreIndex] += amount;
			player.getPackets().sendGameMessage("You deposit " + amount + " " + defs.getName() + ".");
			if (player.getInventory().containsItem(itemId, 1)) {
				boolean removedAll = true;
				if (amount >= player.getInventory().getAmountOf(itemId)) {
					amount -= player.getInventory().getAmountOf(itemId);
					removedAll = false;
				}
				player.getInventory().deleteItem(itemId, removedAll ? amount : player.getInventory().getAmountOf(itemId));
				if (removedAll)
					amount = 0;
			}
			if (amount > 0)
				player.getInventory().deleteItem(defs.getCertId(), amount);
		} else {
			if (amount > artisansWorkShopSupplies[oreIndex])
				amount = artisansWorkShopSupplies[oreIndex];
			if (player.getInventory().getFreeSlots() == 0 && !player.getInventory().containsItem(defs.getCertId(), 1)) {
				player.getPackets().sendGameMessage("You don't have enough inventory space.");
				return;
			}
			if (amount == 0) {
				player.getPackets().sendGameMessage("You don't have any " + defs.getName() + " left.");
				return;
			}
			player.getInventory().addItem(defs.getCertId(), amount);
			artisansWorkShopSupplies[oreIndex] -= amount;
			player.getPackets().sendGameMessage("You withdraw " + amount + " " + defs.getName() + ".");
		}
	}

	public void openCeremonialSwordCreating() {
		if (currentPlan.isPerfect() || currentPlan.isShattered()) {
			player.getPackets().sendGameMessage(currentPlan.isPerfect() ? "The sword is now perfect and requires no more work." : "The sword has been broken and is no longer workable.");
			return;
		}
		player.getDialogueManager().startDialogue(new Dialogue() {
			private int hitType;

			@Override
			public void start() {
				int type = currentPlan.getType();
				int start = ArtisansWorkShopConstants.START_SPRITE + (type * 112);
				player.getInterfaceManager().sendInterface(1074);
				player.getInterfaceManager().sendInventoryInterface(1071);
				player.getPackets().sendRunScriptBlank(4192);
				player.getPackets().sendRunScriptBlank(4190);
				player.getPackets().sendRunScriptBlank(4215);
				int[][] planDesign = CeremonialSword.values()[currentPlan.getSwordOrdinal()].getDesign();
				for (int i = 0; i < 7; i++) {
					int upperSprite = (start + (i == 6 ? 49 : 0) + (planDesign[0][i] * (i == 6 ? 9 : 7)) + planDesign[0][i + 1]);
					int downerSprite = planDesign.length < 2 ? upperSprite : (start + (i == 6 ? 49 : 0) + (planDesign[1][i] * (i == 6 ? 9 : 7)) + planDesign[1][i + 1]);
					player.getPackets().sendIComponentSprite(1074, 73 + i, upperSprite);
					player.getPackets().sendIComponentSprite(1074, 80 + i, downerSprite);
				}
				for (int i = 0; i < 8; i++) {
					player.getPackets().sendIComponentText(1074, i == 0 ? 71 : 154 + i, planDesign[0][i]);
					player.getPackets().sendIComponentText(1074, 162 + i, planDesign.length < 2 ? planDesign[0][i] : planDesign[1][i]);
				}
				refresh();
			}

			@Override
			public void run(int interfaceId, int componentId) {
				if (componentId >= 62 && componentId <= 65) {
					hitType = componentId - 62;
					refreshHitType();
					return;
				}
				int[][] planDesign = currentPlan.getPlanProgress();
				for (int i = 0; i < planDesign.length; i++) {
					for (int j = 0; j < planDesign[i].length; j++) {
						int component = (i == 0 && j == 0) ? 72 : (100 + j + (i * 8));
						if (componentId == component && player.getTemporaryAttributtes().get("HITTING") == null) {
							// soft 42 med 40 hard 39 careful 37
							// 4702
							if (currentPlan.getCoolDown() < (hitType == 3 ? 2 : 1)) {
								player.getPackets().sendGameMessage("You need atleast " + (hitType == 3 ? 2 : 1) + " cooldown to do that.");
								return;
							}
							int varbitId = 8911 + j + (i * 8);
							int spriteComponent = 34 + j + (i * 8);
							player.getPackets().sendRunScript(4216, hitType == 0 ? 39 : hitType == 1 ? 40 : hitType == 2 ? 42 : 37);
							player.getPackets().sendConfigByFile(varbitId, 1);
							player.getPackets().sendIComponentSprite(1074, spriteComponent, 4702);
							player.getTemporaryAttributtes().put("HITTING", Boolean.TRUE);
							boolean typical = (player.getSkills().getLevel(Skills.SMITHING) >= 95 && hitType == 2) || Utils.random(100) <= (player.getSkills().getLevel(Skills.SMITHING) / 1.4);
							int hitAmount = hitType == 3 ? 1 : typical ? (hitType == 0 ? 3 : hitType == 1 ? 2 : 1) : Utils.random(Utils.random(3) == 0 ? 0 : 1, hitType == 0 ? 6 : hitType == 1 ? 4 : 3);
							currentPlan.getPlanProgress()[i][j] += hitAmount;
							currentPlan.setCoolDown(currentPlan.getCoolDown() - (hitType == 3 ? 2 : 1));
							WorldTasksManager.schedule(new WorldTask() {
								@Override
								public void run() {
									player.getPackets().sendRunScript(4216, 41);
									player.getPackets().sendConfigByFile(varbitId, 0);
									player.getPackets().sendIComponentSprite(1074, spriteComponent, 4700);
									player.getPackets().sendGameMessage("Your hit makes " + (hitAmount == 0 ? "no dent." : ("a " + (hitAmount == 1 ? "small" : hitAmount == 2 ? "medium" : hitAmount == 3 ? "large" : "extra large") + " dent.") + "(" + hitAmount + ")"));
									player.getTemporaryAttributtes().remove("HITTING");
									if (currentPlan.isPerfect() || currentPlan.isShattered()) {
										end();
										player.closeInterfaces();
										player.getPackets().sendGameMessage(currentPlan.isPerfect() ? "The sword is now perfect and requires no more work." : "The sword has been broken and is no longer workable.");
										return;
									}
									refresh();
								}
							}, 1);
							return;
						}
					}
				}
			}

			@Override
			public void finish() {

			}

			private void refresh() {
				int type = currentPlan.getType();
				int start = ArtisansWorkShopConstants.START_SPRITE + (type * 112);
				int[][] planDesign = currentPlan.getPlanProgress();
				for (int i = 0; i < 7; i++) {
					int upperSprite = (start + (i == 6 ? 49 : 0) + (planDesign[0][i] * (i == 6 ? 9 : 7)) + planDesign[0][i + 1]);
					int downerSprite = planDesign.length < 2 ? upperSprite : (start + (i == 6 ? 49 : 0) + (planDesign[1][i] * (i == 6 ? 9 : 7)) + planDesign[1][i + 1]);
					player.getPackets().sendIComponentSprite(1074, 87 + i, upperSprite);
					player.getPackets().sendIComponentSprite(1074, 94 + i, downerSprite);
				}
				for (int i = 0; i < 8; i++) {
					player.getPackets().sendIComponentText(1074, 170 + i, planDesign[0][i]);
					player.getPackets().sendIComponentText(1074, 178 + i, planDesign.length < 2 ? planDesign[0][i] : planDesign[1][i]);
				}
				refreshCoolDown();
				refreshHitType();
			}

			private void refreshCoolDown() {
				player.getPackets().sendIComponentText(1074, 16, "" + currentPlan.getCoolDown());
			}

			private void refreshHitType() {
				player.getPackets().sendMoveIComponent(1074, 18, 0, 4 + (hitType * 30));
			}
		});

	}

	public void setPlayer(Player player) {
		this.player = player;
		if (player.ArtisansWorkShopSupplies != null) {
			artisansWorkShopSupplies = player.ArtisansWorkShopSupplies;
			player.ArtisansWorkShopSupplies = null;
		}
	}

	public int[] getArtisansWorkShopSupplies() {
		return artisansWorkShopSupplies;
	}

	public void setArtisansWorkShopSupplies(int index, int amount) {
		this.artisansWorkShopSupplies[index] = amount;
	}

	public int getArtisansXPGained() {
		return artisansXPGained;
	}

	public void increaseArtisansXPGained(int artisansXPGained) {
		this.artisansXPGained += artisansXPGained;
		if (this.artisansXPGained >= 10000) {
			this.artisansXPGained -= 10000;
			respect += 1;
		}
	}

	public double getRespect() {
		return respect;
	}

	public void setRespect(double respect) {
		this.respect = respect;
	}

	public CeremonialSwordPlan getCurrentPlan() {
		return currentPlan;
	}

	public void setCurrentPlan(CeremonialSwordPlan currentPlan) {
		this.currentPlan = currentPlan;
	}

	public boolean isFirstTime100() {
		return firstTime100;
	}

	public void setFirstTime100(boolean firstTime100) {
		this.firstTime100 = firstTime100;
	}

	public boolean isFirstTime90() {
		return firstTime90;
	}

	public void setFirstTime90(boolean firstTime90) {
		this.firstTime90 = firstTime90;
	}

	public static class CeremonialSwordPlan implements Serializable {

		private static final long serialVersionUID = -9196124903588179516L;
		private final int type;
		private final int swordOrdinal;
		private int coolDown;
		private int[][] planProgress;

		public CeremonialSwordPlan(int type, int swordOrdinal, int coolDown) {
			this.type = type;
			this.swordOrdinal = swordOrdinal;
			this.coolDown = coolDown;
			planProgress = new int[2][8];
		}

		public int getType() {
			return type + 1;
		}

		public int getSwordOrdinal() {
			return swordOrdinal;
		}

		public int getCoolDown() {
			return coolDown;
		}

		public void setCoolDown(int coolDown) {
			this.coolDown = coolDown;
		}

		public int[][] getPlanProgress() {
			return planProgress;
		}

		public void setPlanProgress(int[][] planProgress) {
			this.planProgress = planProgress;
		}

		public boolean isPerfect() {
			return getPerformance() == 100;
		}

		public boolean isShattered() {
			for (int i = 0; i < planProgress.length; i++) {
				for (int j = 0; j < planProgress[i].length; j++) {
					if ((j == 7 && planProgress[i][j] > 8) || (j < 7 && planProgress[i][j] > 6))
						return true;
				}
			}
			return false;
		}

		public int getPerformance() {
			int[][] design = CeremonialSword.values()[swordOrdinal].getDesign();
			int[][] perfect = new int[2][8];
			perfect[0] = design[0];
			perfect[1] = design[design.length == 1 ? 0 : 1];
			int performance = 100;
			for (int i = 0; i < perfect.length; i++) {
				for (int j = 0; j < perfect[i].length; j++) {
					if (perfect[i][j] != planProgress[i][j]) {
						int diff = Math.abs(perfect[i][j] - planProgress[i][j]);
						performance -= diff * 2;
					}
				}
			}
			return performance;
		}

		public double getXp() {
			double xp = getType() == BRONZE ? 0 : getType() == IRON ? Ingot.IRON_IV.getXp() : getType() == STEEL ? Ingot.STEEL_IV.getXp() : getType() == MITHRIL ? Ingot.MITHRIL_IV.getXp() : getType() == ADAMANT ? Ingot.ADAMANT_IV.getXp() : Ingot.RUNE_IV.getXp();
			return xp;
		}
	}

}