package com.rs.game.player.actions.magic.lunar.impl;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.activites.duel.DuelArena;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.magic.lunar.DefaultSpell;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HealGroup implements DefaultSpell {

	@Override
	public int getId() {
		return 14872;
	}

	@Override
	public int getLevel() {
		return 95;
	}

	@Override
	public Item[] getRunes() {
		return new Item[] { new Item(ASTRAL_RUNE, 4), new Item(BLOOD_RUNE, 2), new Item(LAW_RUNE, 3) };
	}

	@Override
	public int getDelay() {
		return 4500;
	}

	@Override
	public boolean spellEffect(Player player) {
		if (player.getHitpoints() <= player.getMaxHitpoints() * 0.11) {
			player.getPackets().sendGameMessage("You need atleast 11% of your hitpoints to use this spell.");
			return false;
		} else if (player.getHealDelay() > Utils.currentTimeMillis()) {
			final int seconds = (int) (((player.getHealDelay() - Utils.currentTimeMillis()) / 1000) + 1);
			player.sendMessage("You need to wait another " + seconds + " second" + (seconds == 1 ? "" : "s") + " to cast heal group.");
			return false;
		}
		final int currentHp = (int) ((player.getHitpoints() * 0.75) > player.getMaxHitpoints() ? player.getMaxHitpoints() * 0.75 : player.getHitpoints() * 0.75);
		WorldTasksManager.schedule(new WorldTask() {
			private int loop;

			@Override
			public void run() {
				if (loop == 0) {
					player.getSkills().addXp(Skills.MAGIC, 124);
					player.setNextAnimation(new Animation(4409));
					player.applyHit(new Hit(player, currentHp, HitLook.REGULAR_DAMAGE));
					player.setHealDelay(14000);
				} else if (loop == 1) {
					int affected = 0;
					ArrayList<Player> players = new ArrayList<Player>();
					for (int regionId : player.getMapRegionsIds()) {
						List<Integer> playerIndexes = World.getRegion(regionId).getPlayerIndexes();
						if (playerIndexes == null)
							continue;
						for (int playerIndex : playerIndexes) {
							Player p2 = World.getPlayers().get(playerIndex);
							if (p2 == null || p2 == player || !p2.isAcceptingAid() || p2.isDead() || !p2.isActive() || p2.hasFinished() || !p2.withinDistance(player, 2) || !player.getControlerManager().canHit(p2))
								continue;
							else if (p2.getControlerManager().getControler() != null && p2.getControlerManager().getControler() instanceof DuelArena)
								continue;
							affected++;
							if (!players.contains(p2))
								players.add(p2);
							if (affected >= 10)
								break;
						}
					}
					if (affected <= 0) {
						this.stop();
						return;
					}
					int heal = currentHp / affected;
					if (heal < 0)
						heal = 0;
					for (Iterator<Player> it = players.iterator(); it.hasNext();) {
						Player p2 = it.next();
						if (p2 == null || p2.hasFinished() || !p2.isActive())
							continue;
						if (player == null || player.hasFinished() || !player.isActive())
							break;
						p2.setNextGraphics(new Graphics(736, 0, 100));
						p2.heal(heal);
					}
					players.clear();
					this.stop();
				}
				loop++;
			}
		}, 0, 1);
		return true;
	}

}
