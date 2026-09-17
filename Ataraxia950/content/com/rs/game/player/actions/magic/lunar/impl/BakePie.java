package com.rs.game.player.actions.magic.lunar.impl;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Cooking.Cookables;
import com.rs.game.player.actions.magic.lunar.DefaultSpell;
import com.rs.game.player.content.achievementsystem.AchievementList;

public class BakePie implements DefaultSpell {
	
	@Override
	public int getId() {
		return 14825;
	}
	
	@Override
	public int getLevel() {
		return 65;
	}
	
	@Override
	public Item[] getRunes() {
		return new Item[] { new Item(ASTRAL_RUNE, 1), new Item(FIRE_RUNE, 5), new Item(WATER_RUNE, 4) };
	}
	
	@Override
	public int getDelay() {
		return 4000;
	}

	@Override
	public boolean spellEffect(Player player) {
		for (Cookables food : Cookables.values()) {
			if (food.toString().toLowerCase().contains("_pie")) {
				if (player.getSkills().getLevel(Skills.COOKING) < food.getLvl())
					continue;
				Item item = food.getRawItem();
				if (player.getInventory().containsItem(item.getId(), 1)) {
					for (int i = 0; i < player.getInventory().getAmountOf(item.getId());) {
						player.lock(2);
						player.setLunarDelay(4000);
						player.getInventory().replaceItem(food.getProduct().getId(), item.getAmount(), player.getInventory().getItems().getThisItemSlot(item.getId()));
						if (food.getProduct().getId() == 7218) {
							player.getAchievements().updateProgress(1, AchievementList.COOK_10_SUMMER_PIES);
						}
						player.getSkills().addXp(Skills.MAGIC, 60);
						player.getSkills().addXp(Skills.COOKING, food.getXp());
						player.setNextAnimation(new Animation(4413));
						player.setNextGraphics(new Graphics(746, 0, 100));
						return true;
					}
				}
			}
		}
		player.getPackets().sendGameMessage("You do not have any pie in your inventory.");
		return false;
	}
}
