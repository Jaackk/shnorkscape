package com.rs.game.player.actions.magic.lunar.impl;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.magic.lunar.PlayerSpell;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

public class StatSpy implements PlayerSpell {

	@Override
	public int getId() {
		return 14842;
	}

	@Override
	public int getLevel() {
		return 75;
	}

	@Override
	public Item[] getRunes() {
		return new Item[] { new Item(ASTRAL_RUNE, 2), new Item(COSMIC_RUNE, 2), new Item(BODY_RUNE, 5) };
	}

	@Override
	public int getDelay() {
		return 3000;
	}
	
	private static final int[] SKILLS = {0, 3, 14, 2, 16, 13, 1, 15, 10, 4, 17, 7, 5, 12, 11, 6, 9, 8, 20, 18, 19, 22, 21, 23, 24, 25, 26};
	private static final int[] COMPONENTS = {1, 5, 9, 13, 17, 21, 25, 29, 33, 37, 41, 45, 49, 53, 57, 61, 65, 69, 73, 77, 81, 85, 89, 93, 98, 111, 115};
	public static void sendStatsOfOther(Player player, Player p2) {
	    player.getInterfaceManager().sendMagicAbilitesTab(523);
		player.getPackets().sendIComponentText(523, 1, p2.getSkills().getLevel(0));
		for (int i = 2; i <= 94; i+= 4)
			player.getPackets().sendIComponentText(523, i, "99");
		player.getPackets().sendIComponentText(523, 112, "99");
	    player.getPackets().sendIComponentText(523, 78, "120");
		player.getPackets().sendIComponentText(523, 99, "120");
	    player.getPackets().sendIComponentText(523, 116, "120");
		for (int i = 0; i < SKILLS.length; i++)
			player.getPackets().sendIComponentText(523, COMPONENTS[i], ""+p2.getSkills().getLevel(SKILLS[i]));
		player.getPackets().sendIComponentText(523, 106, p2.getDisplayName());
	}

	@Override
	public boolean spellEffect(Player player, Player target) {
		if (target == null || !target.isActive() || target.hasFinished())
			return false;
		
		WorldTasksManager.schedule(new WorldTask() {
			private int loop;
			@Override
			public void run() {
				if (player == null || !player.isActive() || player.hasFinished() || target == null
						|| target.hasFinished() || !target.isActive()) {
					this.stop();
					return;
				}
				if (loop == 0) {
					player.faceEntity((target));
					player.getSkills().addXp(Skills.MAGIC, 76);
					player.setNextGraphics(new Graphics(1060));
					player.setNextAnimation(new Animation(6293));
				} else if (loop == 1) {
					target.setNextGraphics(new Graphics(736, 0, 100));
					target.getPackets().sendGameMessage(player.getDisplayName() + " is spying upon your stats...", true);
				    player.getInterfaceManager().sendMagicAbilitesTab(523);
					sendStatsOfOther(player, target);
					this.stop();
				}
				loop++;
			}
		}, 0, 1);
		return true;
	}

}
