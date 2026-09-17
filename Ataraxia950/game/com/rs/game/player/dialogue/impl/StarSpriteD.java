package com.rs.game.player.dialogue.impl;

import com.rs.Settings;
import com.rs.game.activites.worldevents.ShootingStar;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Utils;

/**
 * Handles the Star Sprite dialogue.
 *
 * @author Noel
 */
public class StarSpriteD extends Dialogue {

	@Override
	public void start() {
		if (player.getInventory().containsOneItem(ShootingStar.STARDUST)
				&& (Utils.currentTimeMillis() - player.getLastStarSprite()) > (1 * 60 * 60 * 1000)) {
			sendNPCDialogue(ShootingStar.SPRITE, NORMAL, "Thank you for helping me out of here.");
			stage = 0;
		} else {
			sendNPCDialogue(ShootingStar.SPRITE, NORMAL,
					"I'm a star sprite! "
							+ "I was in my star in the sky, when it lost control and crashed into the ground. "
							+ "With half my star sticking into the ground, I became stuck. Fortunately, "
							+ "I was mined out by the kind creatures of");
			stage = 1;
		}
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case 0:
			int stardust = player.getInventory().getAmountOf(ShootingStar.STARDUST);
			if (stardust > 200)
				stardust = 200;
			doRewards(player.getInventory().getAmountOf(ShootingStar.STARDUST));
			player.getInventory().deleteItem(ShootingStar.STARDUST, stardust);
			int amtCoins = (int) (stardust * 50002 / 200 * Settings.getDropQuantityRate(player));
			player.addMoney(amtCoins);
			player.setLastStarSprite(Utils.currentTimeMillis());
			sendNPCDialogue(ShootingStar.SPRITE, NORMAL, "I have rewarded you with the ability to"
					+ " mine extra ore for 15 minutes!" + " Please enjoy the resources as well, adventurer!");
			stage = -2;
			break;
		case 1:
			sendNPCDialogue(ShootingStar.SPRITE, NORMAL, "your race.");
			stage = 2;
			break;
		case 2:
			sendPlayerDialogue(NORMAL, "Well, I'm glad you're okay.");
			stage = -2;
			break;
		default:
			end();
			break;
		}
	}

	public void doRewards(int dust) {
		int[][] resources = {
				/* Assorted ores */
				{ 441, 500 }, { 445, 250 }, { 443, 300 },
				/* Assorted logs */
				{ 1512, 1000 }, { 1522, 1000 }, { 1520, 750 }, { 1518, 500 },
				/* Rune essence and herblore supplies */
				{ 7937, 1500 }, { 6694, 200 }, { 220, 200 }, { 12539, 200 } };
		if (dust >= 200) {
			for (int i = 0; i < 2; i++) {
				int index = Utils.random(0, resources.length - 1);
				player.getInventory().addItemDrop(resources[index][0],
						Utils.random(resources[index][1] / 2, resources[index][1]));
			}
			int index = Utils.random(0, resources.length - 1);
			player.getInventory().addItemDrop(resources[index][0],
					Utils.random(resources[index][1] / 4, resources[index][1] / 2));
			player.getInventory().addItemDrop(35886, 1);
			return;
		}
		if (dust >= 100) {
			for (int i = 0; i < 2; i++) {
				int index = Utils.random(0, resources.length - 1);
				player.getInventory().addItemDrop(resources[index][0],
						Utils.random(resources[index][1] / 4, resources[index][1] / 2));
			}
			int index = Utils.random(0, resources.length - 1);
			player.getInventory().addItemDrop(resources[index][0], Utils.random(1, resources[index][1] / 4));
			return;
		}
		for (int i = 0; i < 2; i++) {
			int index = Utils.random(1, resources.length - 1);
			player.getInventory().addItemDrop(resources[index][0], Utils.random(1, resources[index][1] / 4));
		}
		return;
	}

	@Override
	public void finish() {
	}
}