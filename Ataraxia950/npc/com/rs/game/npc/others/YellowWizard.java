package com.rs.game.npc.others;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.RuneCrafting;
import com.rs.game.player.content.RunespanData;
import com.rs.game.player.controllers.RunespanController;
import com.rs.utils.Utils;

@SuppressWarnings("serial")
public class YellowWizard extends NPC {

	private final RunespanController controler;
	private final long spawnTime;
	private final int requestedRune;

	public YellowWizard(WorldTile tile, RunespanController controler, int requestedRune) {
		super(15430, tile, -1, true, true);
		spawnTime = Utils.currentTimeMillis();
		this.controler = controler;
		this.requestedRune = requestedRune;
	}

	public void tellRequest(Player player) {
		player.getPackets().sendGameMessage("The yellow wizard needs " + ItemDefinitions.getItemDefinitions(requestedRune).getName().toLowerCase() + "s. Use a Runespan rune stack on him to help.");
	}

	public void giveReward(Player player, int runeId) {
		if (!RunespanData.isRunespanRune(runeId)) {
			tellRequest(player);
			return;
		}
		int amount = Math.min(10, player.getInventory().getAmountOf(runeId));
		if (amount <= 0) {
			tellRequest(player);
			return;
		}
		player.getInventory().deleteItem(runeId, amount);
		int level = player.getSkills().getLevel(Skills.RUNECRAFTING);
		double multiplier = runeId == requestedRune ? 3.0 : Math.max(0.5, RunespanData.getRunePointValue(runeId));
		double xp = Math.max(4, level * amount * multiplier) * RuneCrafting.getRunecraftingXpModifier(player);
		player.getSkills().addXp(Skills.RUNECRAFTING, xp);
		player.getPackets().sendGameMessage("The yellow wizard takes " + amount + " rune" + (amount == 1 ? "" : "s") + " and rewards you with Runecrafting experience.");
		finish();
	}

	@Override
	public void finish() {
		controler.removeWizard();
		super.finish();
	}

	@Override
	public void processNPC() {
		if (spawnTime + 300000 < Utils.currentTimeMillis())
			finish();
	}

	@Override
	public boolean withinDistance(Player tile, int distance) {
		return tile == controler.getPlayer() && super.withinDistance(tile, distance);
	}
}
