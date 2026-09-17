package com.rs.game.player.actions.runecrafting;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.npc.NPC;
import com.rs.game.npc.others.DonatorRunesphere;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.content.RuneCrafting;
import com.rs.game.player.content.RunespanData;
import com.rs.game.player.controllers.RunespanController;
import com.rs.utils.Utils;

public class SiphonDonatorRunesphere extends Action {

	private static final Animation SIPHON = new Animation(16596);
	private static final Animation STOP = new Animation(16599);
	private static final Graphics SIPHON_GFX = new Graphics(3071);
	private static final int DUST_CAP = 1000;

	private final NPC npc;
	private Player player;

	public SiphonDonatorRunesphere(NPC npc) {
		this.npc = npc;
		if (npc instanceof DonatorRunesphere) {
			final DonatorRunesphere sphere = (DonatorRunesphere) npc;
			if (!sphere.isActive())
				sphere.setActive(true);
		}
	}

	@Override
	public boolean process(Player player) {
		return check();
	}

	@Override
	public int processWithDelay(Player player) {
		Layer layer = Layer.forLevel(player.getSkills().getLevel(Skills.RUNECRAFTING));
		player.setNextAnimation(SIPHON);
		if (Utils.random(100) < 85) {
			player.getInventory().deleteItem(RunespanData.RUNE_ESSENCE, 1);
			player.getInventory().addItem(layer.runeId, 1);
			player.getSkills().addXp(Skills.RUNECRAFTING, layer.xp * RuneCrafting.getRunecraftingXpModifier(player));
			giveDust(player, 1 + Utils.random(6));
			player.setNextGraphics(SIPHON_GFX);
		} else {
			player.getSkills().addXp(Skills.RUNECRAFTING, RuneCrafting.getRunespanFailureXp());
			giveDust(player, 1);
		}
		if (player.getControlerManager().getControler() instanceof RunespanController)
			((RunespanController) player.getControlerManager().getControler()).refreshInventoryPoints();
		return 3;
	}

	private void giveDust(Player player, int amount) {
		int dust = player.getInventory().getAmountOf(RunespanData.RUNE_DUST);
		if (dust >= DUST_CAP)
			return;
		player.getInventory().addItem(RunespanData.RUNE_DUST, Math.min(amount, DUST_CAP - dust));
	}

	@Override
	public boolean start(Player player) {
		this.player = player;
		return check();
	}

	@Override
	public void stop(Player player) {
		player.setNextAnimation(STOP);
		setActionDelay(player, 3);
	}

	public boolean check() {
		if (player.isLocked())
			return false;
		if (player.getMoneySpent() < 20) {
			player.sendMessage("You need to be at least a bronze donator to use this!", false);
			return false;
		}
		if (!player.withinDistance(npc, 2)) {
			player.calcFollow(npc, true);
			return true;
		}
		if (!player.getInventory().containsItem(RunespanData.RUNE_ESSENCE, 1)) {
			player.sendMessage("You don't have any rune essence to siphon from the runesphere.", false);
			return false;
		}
		Layer layer = Layer.forLevel(player.getSkills().getLevel(Skills.RUNECRAFTING));
		if (player.getSkills().getLevel(Skills.RUNECRAFTING) < layer.level) {
			player.sendMessage("You need a Runecrafting level of " + layer.level + " to siphon this layer.", false);
			return false;
		}
		if (!player.getInventory().hasFreeSlots()
				&& !player.getInventory().containsItem(layer.runeId, 1)
				&& !player.getInventory().containsItem(RunespanData.RUNE_DUST, 1)) {
			player.sendMessage("You don't have enough inventory space to siphon any runes.", false);
			return false;
		}
		return true;
	}

	private enum Layer {
		AIR(1, 19, RunespanData.AIR_RUNE),
		MIND(8, 20, RunespanData.MIND_RUNE),
		WATER(15, 25.3, RunespanData.WATER_RUNE),
		EARTH(22, 28.6, RunespanData.EARTH_RUNE),
		FIRE(29, 34.8, RunespanData.FIRE_RUNE),
		BODY(36, 46.2, RunespanData.BODY_RUNE),
		COSMIC(42, 53.2, RunespanData.COSMIC_RUNE),
		CHAOS(50, 61.5, RunespanData.CHAOS_RUNE),
		ASTRAL(57, 71.33, RunespanData.ASTRAL_RUNE),
		NATURE(64, 87, RunespanData.NATURE_RUNE),
		LAW(71, 107.5, RunespanData.LAW_RUNE),
		DEATH(78, 120, RunespanData.DEATH_RUNE),
		BLOOD(85, 146.3, RunespanData.BLOOD_RUNE),
		SOUL(92, 213, RunespanData.SOUL_RUNE);

		private final int level;
		private final double xp;
		private final int runeId;

		Layer(int level, double xp, int runeId) {
			this.level = level;
			this.xp = xp;
			this.runeId = runeId;
		}

		private static Layer forLevel(int level) {
			Layer best = AIR;
			for (Layer layer : values()) {
				if (level >= layer.level)
					best = layer;
			}
			return best;
		}
	}
}
