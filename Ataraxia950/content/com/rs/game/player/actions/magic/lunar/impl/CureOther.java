package com.rs.game.player.actions.magic.lunar.impl;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.magic.lunar.PlayerSpell;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

public class CureOther implements PlayerSpell {

	@Override
	public int getId() {
		return 14829;
	}

	@Override
	public int getLevel() {
		return 68;
	}

	@Override
	public Item[] getRunes() {
		return new Item[] { new Item(ASTRAL_RUNE, 1), new Item(EARTH_RUNE, 10) };
	}

	@Override
	public int getDelay() {
		return 3000;
	}

	@Override
	public boolean spellEffect(Player player, Player target) {
		player.faceEntity(target);
		if (!target.getPoison().isPoisoned()) {
			player.getPackets().sendGameMessage(target.getDisplayName() + " is not poisoned.");
			return false;
		} else if (!target.isAcceptingAid()) {
			player.sendMessage("The targeted player is not accepting aid.");
			return false;
		}
		WorldTasksManager.schedule(new WorldTask() {
			private int loop;
			@Override
			public void run() {
				if (loop == 0) {
					player.faceEntity(target);
					player.getSkills().addXp(Skills.MAGIC, 65);
					player.setNextAnimation(new Animation(4411));
				} else if (loop == 1) {
					target.setNextGraphics(new Graphics(744, 0, 120));
					target.getPoison().reset();
					target.getPackets().sendGameMessage("Your afflictions have been cured by " + player.getDisplayName() + ".");
					player.getPackets().sendGameMessage("You have cured the afflictions of " + target.getDisplayName() + ".");
					stop();
				}
				loop++;
			}
		}, 0, 1);
		return true;
	}

}
