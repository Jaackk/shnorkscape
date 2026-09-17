package com.rs.game.player.actions.magic.lunar.impl;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.WorldObject;
import com.rs.game.item.Item;
import com.rs.game.player.FarmingManager.FarmingSpot;
import com.rs.game.player.FarmingManager.SpotInfo;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.magic.lunar.ObjectSpell;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

public class CurePlant implements ObjectSpell {

	@Override
	public int getId() {
		return 14826;
	}

	@Override
	public int getLevel() {
		return 66;
	}

	@Override
	public Item[] getRunes() {
		return new Item[] { new Item(ASTRAL_RUNE, 1), new Item(EARTH_RUNE, 8) };
	}

	@Override
	public int getDelay() {
		return 6000;
	}

	@Override
	public boolean spellEffect(Player player, WorldObject object) {
		final SpotInfo info = SpotInfo.getInfo(object.getId());
		if (info == null) {
			player.getPackets().sendGameMessage("Umm... this spell won't cure that!");
			return false;
		} else if (info != null) {
			FarmingSpot spot = player.getFarmingManager().getSpot(info);
			if (spot == null) {
				player.sendMessage("I don't think I can cure that.");
				return false;
			}
			if (spot.isDead()) {
				player.getPackets().sendGameMessage("'Cure' not 'Resurrect'. Although death may arise from disease, it is not in itself a disease and hence cannot be cured. So there.");
				return false;
			}
			if (spot.isDiseased()) {
				WorldTasksManager.schedule(new WorldTask() {
					private int loop;

					@Override
					public void run() {
						if (loop == 0) {
							player.lock();
							player.faceObject(object);
							player.setNextGraphics(new Graphics(748, 0, 120));
							player.setNextAnimation(new Animation(4432));
						} else if (loop == 1) {
							player.getSkills().addXp(Skills.MAGIC, 91.5);
							spot.setDiseased(false);
							spot.refresh();
							player.unlock();
							player.getPackets().sendGameMessage("The produce in this patch has been restored to its natural health.");
							stop();
						}
						loop++;

					}
				}, 0, 8);
			} else {
				player.getPackets().sendGameMessage("The patch is not diseased.");
				return false;
			}
		}
		return true;
	}

}
