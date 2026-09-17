package com.rs.game.player.dialogue.impl;

import com.rs.game.Animation;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class ThreeStatuesStoneD extends Dialogue {

	@Override
	public void start() {
		sendOptionsDialogue("What'd you like to carve?", "Stone sword.", "Stone bow.", "Stone staff.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		craftWeapon(player, componentId == OPTION_1 ? 2 : componentId == OPTION_2 ? 3 : 4);
		end();
	}
	
	public static final void craftWeapon(Player player, int option) {
		if (!player.getInventory().containsItem(17444, 1) && !player.getDungeoneeringToolbelt().containsTool(17444)) {
			player.sendMessage("You need a chisel to carve the stone block.");
			return;
		}
		player.setNextAnimation(new Animation(13740));
		player.sendMessage("You attempt to carve the stone block..");
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				if (Utils.random(100) < 25) {
					player.applyHit(new Hit(null, Utils.random(player.getSkills().getLevel(Skills.HITPOINTS) * 5), HitLook.REGULAR_DAMAGE));
					player.sendMessage("You have a hilarious crafting accident that you'll one day tell your grandchildren about.");
					player.getInventory().deleteItem(17415, 1);
					return;
				}
				player.sendMessage("You successfully carve the stone block into a weapon.");
				player.getInventory().deleteItem(17415, 1);
				player.getInventory().addItem(option == 2 ? 17416 : option == 3 ? 17418 : 17420, 1);
			}
		});
	}

	@Override
	public void finish() {}

}
