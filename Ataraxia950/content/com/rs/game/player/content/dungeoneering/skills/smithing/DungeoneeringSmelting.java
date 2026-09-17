package com.rs.game.player.content.dungeoneering.skills.smithing;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

public class DungeoneeringSmelting extends Action {

	private final DungeoneeringSmeltingData data;
	private int amount;
	
	public DungeoneeringSmelting(DungeoneeringSmeltingData data, int amount) {
		this.data = data;
		this.amount = amount;
	}

	@Override
	public boolean process(Player player) {
		if (amount <= 0)
			return false;
		if (player.getSkills().getLevel(Skills.SMITHING) < data.getLevel()) {
			player.sendMessage("You need at least level " + data.getLevel() + " to smelt a bar of " + ItemDefinitions.getItemDefinitions(data.getBarId()).getName().replace(" bar", "."));
			return false;
		}
		if (!player.getInventory().containsItem(data.getOreId(), 1)) {
			player.sendMessage("You need some " + ItemDefinitions.getItemDefinitions(data.getOreId()).getName().toLowerCase() + " to smelt this.");
			return false;
		}
		return true;
	}

	@Override
	public int processWithDelay(Player player) {
		amount--;
		if (player.getAnimations().hasArcaneSmelt && player.getAnimations().arcaneSmelt) {
			player.setNextAnimation(new Animation(20292));
			player.setNextGraphics(new Graphics(4000));
		} else
			player.setNextAnimation(new Animation(32626));
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				player.getInventory().addItem(data.getBarId(), 1);
				player.getSkills().addXp(Skills.SMITHING, data.getExperience());
			}
		}, 3);
		player.getInventory().deleteItem(data.getOreId(), 1);
		return 5;
	}

	@Override
	public boolean start(Player player) {
        return process(player);
    }

	@Override
	public void stop(Player player) {

	}

}
