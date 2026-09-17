package com.rs.game.player.content.dungeoneering.skills.divination;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;

/**
 * Handles Weaving Divination energy.
 *
 * @author Noel.
 */
public class DungeoneeringWeaving extends Action {

	public DungeoneeringDivinationData data;
	public int ticks;
	public int limit;

	public DungeoneeringWeaving(DungeoneeringDivinationData bar, int ticks) {
		this.data = bar;
		this.ticks = ticks;
	}

	@Override
	public boolean start(Player player) {
        return process(player);
    }

	@Override
	public boolean process(Player player) {
		limit = player.getPerkManager().hasPerkActive(DonationPerk.DIVINE_DOUBLER) ? 2 : 1;
		if (data == null || player == null)
			return false;
		if (ticks <= 0)
			return false;
		if (player.getSkills().getLevel(Skills.DIVINATION) < data.getDivinationLevel()) {
			player.sendMessage("You need a Divination level of at least " + data.getDivinationLevel() + " to create a " + ItemDefinitions.getItemDefinitions(data.getItemId()).getName() + ".");
			return false;
		}
		if (player.getSkills().getLevelForXp(Skills.HITPOINTS) < data.getConstitutionLevel()) {
			player.sendMessage("You need a Constitution level of at least " + data.getConstitutionLevel() + " to create a " + ItemDefinitions.getItemDefinitions(data.getItemId()).getName() + ".");
			return false;
		}
		if (!player.getInventory().containsItem(data.getEnergies())) {
			player.sendMessage("You need " + data.getEnergies().getAmount() + " " + data.getEnergies().getName() + " to create a " + ItemDefinitions.getItemDefinitions(data.getItemId()).getName() + ".");
			return false;
		}
		if (data.getSecondaryItemId() != 0) {
			if (!player.getInventory().containsItem(data.getSecondaryItemId(), 1)) {
				player.sendMessage("You need one " + ItemDefinitions.getItemDefinitions(data.getSecondaryItemId()).getName() + " " + "to create a " + ItemDefinitions.getItemDefinitions(data.getItemId()).getName() + ".");
				return false;
			}
		}
		return true;
	}

	@Override
	public int processWithDelay(Player player) {
		ticks--;
		player.setNextAnimation(new Animation(21225));
		player.setNextGraphics(new Graphics(4249));
		double xp = data.getExperience();
		player.getSkills().addXp(Skills.DIVINATION, xp);
		player.getInventory().deleteItem(data.getEnergies());
		if (data.getSecondaryItemId() != 0)
			player.getInventory().deleteItem(data.getSecondaryItemId(), 1);
		player.getInventory().addItem(data.getItemId(), 1);
		player.sendMessage("You weave the energy into a " + ItemDefinitions.getItemDefinitions(data.getItemId()).getName().toLowerCase() + ".", true);
		if (ticks > 0)
			return 1;
		return -1;
	}

	@Override
	public void stop(Player player) {
		setActionDelay(player, 3);
	}
}