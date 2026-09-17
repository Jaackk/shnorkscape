package com.rs.game.player.actions.fletching;

import com.rs.game.Animation;
import com.rs.game.activites.halloween.ClueScrollDistributor;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.utils.Colors;

public class Bamboo extends Action {
	
	// player.sendMessage(String) to debug
	private final Player player;
	
	// constructor for new action, aka new Bamboo()
	public Bamboo(Player player) {
		this.player = player;
	}

	// this code is run as a regular check
	@Override
	public boolean process(Player player) {
		return check(player);
	}

	// this code is run on a loop, return the amount of time to wait before next loop, each 1 = 600ms
	@Override
	public int processWithDelay(Player player) {
		player.setNextAnimation(new Animation(4438));
		player.getInventory().deleteItem(37770, 5);
		player.getInventory().addItem(37744, 1);
		player.getSkills().addXp(Skills.FLETCHING, 100);
		player.addItemsFletched();
		player.sendMessage("You bundle the sticks of bamboo. Total items fletched: " + Colors.RED + player.getItemsFletched() + "</col>", true);
		ClueScrollDistributor.givePlayerClueScrollIfProbable(player, ClueScrollDistributor.SKILLING_PERCENT);
		return 3;
	}

	
	// this is run when a Bamboo object is created w/ new Bamboo()
	// returns false automatically, must return true to start
	@Override
	public boolean start(Player player) {
		return check(player);
	}

	// this is run when the action is over or to stop it
	@Override
	public void stop(Player player) {
		setActionDelay(player, 3);
	}
	
	// this should be used to check conditions if a player can do this, if inventory is full, has enough, etc
	public boolean check(Player player) {
		if (player.getSkills().getLevel(Skills.FLETCHING) < 96) {
			player.sendMessage("You need a fletching level of 96 to fletch bundles of bamboo");
			return false;
		}
		if (!player.getInventory().containsItem(37770, 5)) {
			player.sendMessage("You don't have enough bamboo.");
			return false;
		}
		return true;
	}

	
	
	
}
