package com.rs.game.player.actions.magic.lunar.impl;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.magic.lunar.ItemSpell;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

public class MakeLeather implements ItemSpell {

	private static final int[][] LEATHER = new int[][] {
		{ 1739, 1741 },
		{ 1739, 1743 },
		{ 6287, 6289 },
		{ 7801, 6289 },
		{ 1753, 1745 },
		{ 1751, 2505 },
		{ 1749, 2507 },
		{ 1747, 2509 },
		{ 24372, 24374 }
	};
	
	@Override
	public int getId() {
		return 14854;
	}

	@Override
	public int getLevel() {
		return 83;
	}

	@Override
	public Item[] getRunes() {
		return new Item[] { new Item(ASTRAL_RUNE, 2), new Item(FIRE_RUNE, 2), new Item(BODY_RUNE, 2) };
	}

	@Override
	public int getDelay() {
		return 2000;
	}
	
	@Override
	public boolean spellEffect(Player player, Item item) {
		int index = 0;
		if (player.getTemporaryAttributtes().get("makeLeather") == null) {
			boolean isLeather = false;
			for (int[] items : LEATHER) {
				if (items[0] == item.getId()) {
					isLeather = true;
					break;
				}
				index++;
			}
			if (!isLeather) {
				player.sendMessage("You can only cast this spell on leather.");
				return false;
			} else if (player.getInventory().getAmountOf(item.getId()) < 5) {
				player.sendMessage("You need at least five " + item.getName().toLowerCase() + "s to cast this spell.");
				return false;
			}
			if (index < 2) {
				player.getDialogueManager().startDialogue("MakeLeatherOptionD", this);
				return false;
			}
		} else 
			index = (int) player.getTemporaryAttributtes().remove("makeLeather");
		final int productIndex = index;
		player.lock();
		WorldTasksManager.schedule(new WorldTask() {
			private int ticks;
			@Override
			public void run() {
				if (ticks == 0) {
					player.setNextGraphics(new Graphics(1319));
					player.setNextAnimation(new Animation(7756));
					player.getSkills().addXp(Skills.MAGIC, 87);
				} else if (ticks == 1) {
					player.getInventory().deleteItem(new Item(item.getId(), 5));
					player.getInventory().addItem(new Item(LEATHER[productIndex][1], 5));
					player.unlock();
					stop();
				}
				ticks++;
			}
		}, 0, 2);
		return true;
	}

}
