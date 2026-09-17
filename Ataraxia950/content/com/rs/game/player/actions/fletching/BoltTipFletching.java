package com.rs.game.player.actions.fletching;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.activites.halloween.ClueScrollDistributor;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.actions.crafting.CraftingRs3Dialogue;
import com.rs.game.player.actions.fletching.defs.BoltTips;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

/**
 * BoltTipFletching.java | 11:45:24 AM
 * @author Chryonic
 * @date Apr 15, 2017
 */
public class BoltTipFletching extends Action {

	private final BoltTips tips;
	private int quantity;

	public BoltTipFletching(BoltTips tips, int quantity) {
		this.tips = tips;
		this.quantity = quantity;
	}

	public static void boltFletch(Player player, BoltTips tips) {
		CraftingRs3Dialogue.sendBoltTipsInterface(player, tips);
	}

	public boolean checkAll(Player player) {
		if (player.getSkills().getLevel(Skills.FLETCHING) < tips.getLevelRequired()) {
			player.sendMessage("You need a Fletching level of " + tips.getLevelRequired() + " to cut that gem into Bolt Tips.");
			return false;
		}
		if (!player.getInventory().containsOneItem(tips.getGemId())) {
			player.sendMessage("You don't have any " + ItemDefinitions.getItemDefinitions(tips.getGemId()).getName().toLowerCase() + " to cut into Bolt Tips.");
			return false;
		}
		return true;
	}

	@Override
	public boolean start(Player player) {
		if (checkAll(player)) {
			setActionDelay(player, 0);
			player.setNextAnimation(new Animation(tips.getEmote()));
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
		player.getInventory().deleteItem(tips.getGemId(), 1);
		player.getInventory().addItem(tips.gettipId(), tips.getAmount());
		player.getSkills().addXp(Skills.FLETCHING, tips.getExperience() / 4);
		player.addItemsFletched();
		player.sendMessage("You cut the " + ItemDefinitions.getItemDefinitions(tips.getGemId()).getName().toLowerCase() + " into " + ItemDefinitions.getItemDefinitions(tips.gettipId()).getName().toLowerCase() + "; " + "items fletched: " + Colors.RED + Utils.getFormattedNumber(player.getItemsFletched()) + "</col>.", true);
		ClueScrollDistributor.givePlayerClueScrollIfProbable(player, ClueScrollDistributor.SKILLING_PERCENT);
		quantity--;
		if (quantity <= 0)
			return -1;
		player.setNextAnimation(new Animation(tips.getEmote()));
		return 0;
	}

	@Override
	public void stop(final Player player) {
		setActionDelay(player, 2);
	}
}
