package com.rs.game.player.actions.magic.lunar.impl;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.BuffDebuffTimersManager.Timer;
import com.rs.game.player.actions.magic.lunar.ItemSpell;
import com.rs.game.player.content.Foods.Food;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class SpiritualiseFood implements ItemSpell {

	@Override
	public int getId() {
		return 14850;
	}

	@Override
	public int getLevel() {
		return 80;
	}

	@Override
	public Item[] getRunes() {
		return new Item[] { new Item(ASTRAL_RUNE, 2), new Item(COSMIC_RUNE, 3), new Item(BODY_RUNE, 5) };
	}

	@Override
	public int getDelay() {
		return 3000;
	}

	@Override
	public boolean spellEffect(Player player, Item item) {
		if (player.getSpiritualiseDelay() > Utils.currentTimeMillis()) {
			final int seconds = (int) (((player.getSpiritualiseDelay() - Utils.currentTimeMillis()) / 1000) + 1);
			player.sendMessage("You need to wait another " + seconds + " second" + (seconds == 1 ? "" : "s") + " to cast spiritualise food.");
			return false;
		}
		if (player.getFamiliar() == null) {
			player.sendMessage("You need to have a follower to cast this spell.");
			return false;
		}
		final Food food = Food.forId(item.getId());
		if (food == null) {
			player.sendMessage("You cannot cast this spell on that item.");
			return false;
		}
		int heal = food.getHeal(player);
		if (heal < 12) {
			player.sendMessage("You can only cast this spell on high-healing food.");
			return false;
		}
		if (player.getFamiliar().isSpiritualised()) {
			player.sendMessage("Your familiar has already been spiritualised with food.");
			return false;
		}
		WorldTasksManager.schedule(new WorldTask() {
			private int ticks;
			@Override
			public void run() {
				if (player.getFamiliar() == null || player.getFamiliar().isDead() || player.getFamiliar().hasFinished()) {
					stop();
					return;
				}
				if (ticks == 0) {
					player.faceEntity(player.getFamiliar());
					player.setNextAnimation(new Animation(7700));
					player.setNextGraphics(new Graphics(1309));
					player.getInventory().deleteItem(item);
					player.getFamiliar().setNextGraphics(new Graphics(player.getFamiliar().getSize() == 1 ? 1314 : 1315));
				} else if (ticks == 1) {
					int heal = food.getHeal(player);
					player.getFamiliar().applyHit(new Hit(null, heal * 10, HitLook.HEALED_DAMAGE));
					player.getFamiliar().setTicks((int) (player.getFamiliar().getTicks() * 1.5));
					player.getBuffDebuffTimersManager().addTimer(Timer.FAMILIAR_SUMMONED, player.getFamiliar().getTicks() * 600);
					player.getFamiliar().sendTimeRemaining();
					player.getFamiliar().setSpiritualised(true);
					player.setSpiritualiseDelay(120000);
					final int[] bonuses = player.getFamiliar().getBonuses();
					if (bonuses != null) {
						final int[] renewedBonuses = new int[bonuses.length];
						for (int i = 0; i < bonuses.length; i++)
							renewedBonuses[i] = (int) (bonuses[i] * 1.2);
						player.getFamiliar().setBonuses(renewedBonuses);
					}
					stop();
				}
				ticks++;
			}
			
		}, 0, 1);
		return true;
	}

}
