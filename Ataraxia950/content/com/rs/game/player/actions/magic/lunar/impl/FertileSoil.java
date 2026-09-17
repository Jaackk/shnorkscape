package com.rs.game.player.actions.magic.lunar.impl;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.item.Item;
import com.rs.game.player.FarmingManager.FarmingSpot;
import com.rs.game.player.FarmingManager.SpotInfo;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.magic.lunar.ObjectSpell;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

public class FertileSoil implements ObjectSpell {

	@Override
	public int getId() {
		return 14855;
	}

	@Override
	public int getLevel() {
		return 83;
	}

	@Override
	public Item[] getRunes() {
		return new Item[] { new Item(ASTRAL_RUNE, 3), new Item(NATURE_RUNE, 2), new Item(EARTH_RUNE, 15) };
	}

	@Override
	public int getDelay() {
		return 5000;
	}

	@Override
	public boolean spellEffect(Player player, WorldObject object) {
		SpotInfo info = SpotInfo.getInfo(object.getId());
		if (info == null) {
			player.getPackets().sendGameMessage("Um...I don't want to fertilise that!");
			return false;
		} else if (info != null) {
			FarmingSpot spot = player.getFarmingManager().getSpot(info);
			if (spot == null || spot.productInfo == null) {
				player.sendMessage("What's the point in fertilising that?");
				return false;
			}
			if (spot.isDead() || spot.productInfo != null && spot.reachedMaxStage() || spot.isDiseased()) {
				player.getPackets().sendGameMessage("Composting it isn't going to make it get any bigger.");
				return false;
			} else if (spot.hasCompost()) {
				player.getPackets().sendGameMessage("The patch has already been saturated with compost.");
				return false;
			}
			WorldTasksManager.schedule(new WorldTask() {
				private int loop;

				@Override
				public void run() {
					if (loop == 0) {
						player.lock();
						player.faceObject(object);
						World.sendGraphics(player, new Graphics(724), object);
						player.setNextAnimation(new Animation(4413));
					} else if (loop == 1) {
						player.getSkills().addXp(Skills.MAGIC, 87);
						player.getSkills().addXp(Skills.FARMING, 18);
						spot.setSuperCompost(true);
						spot.refresh();
						player.unlock();
						player.getPackets().sendGameMessage("You saturate the patch with supercompost.");
						stop();
					}
					loop++;
				}
			}, 0, 6);
		}
		return true;
	}

}
