package com.rs.game.player.actions.magic.lunar.impl;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.magic.lunar.NPCSpell;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

public class MonsterExamine implements NPCSpell {

	@Override
	public int getId() {
		return 14827;
	}

	@Override
	public int getLevel() {
		return 66;
	}

	@Override
	public Item[] getRunes() {
		return new Item[] { new Item(ASTRAL_RUNE, 1), new Item(COSMIC_RUNE, 1), new Item(MIND_RUNE, 1) };
	}

	@Override
	public int getDelay() {
		return 3000;
	}

	@Override
	public boolean spellEffect(Player player, NPC npc) {
		if (npc == null || npc.isDead() || npc.hasFinished())
			return false;
		WorldTasksManager.schedule(new WorldTask() {
			private int loop;

			@Override
			public void run() {
				if (npc == null || npc.isDead() || npc.hasFinished() || player == null || !player.isActive() || player.hasFinished()) {
					this.stop();
					return;
				}
				if (loop == 0) {
					player.getSkills().addXp(Skills.MAGIC, 61);
					player.setNextFaceEntity(npc);
					player.setNextGraphics(new Graphics(1059));
					player.setNextAnimation(new Animation(6293));
				} else if (loop == 1) {
					npc.setNextGraphics(new Graphics(736));
					player.getInterfaceManager().sendMagicAbilitesTab(522);
					player.getPackets().sendIComponentText(522, 12, "Monster Name:" + npc.getDefinitions().getName());
	                player.getPackets().sendIComponentText(522, 4, "Hitpoints: " + npc.getHitpoints() + "/" + npc.getMaxHitpoints());
					player.getPackets().sendIComponentText(522, 5, "Attack level: " + npc.getStats().getAttackLevel());
                    player.getPackets().sendIComponentText(522, 6, "Defence level: " + npc.getStats().getDefenceLevel());
                    player.getPackets().sendIComponentText(522, 7, "Magic level: " + npc.getStats().getMagicLevel());
                    player.getPackets().sendIComponentText(522, 8, "Range level: " + npc.getStats().getRangeLevel());
                    player.getPackets().sendIComponentText(522, 9, "Attack rate: " + npc.getAttackSpeed());
					player.setNextFaceEntity(null);
					this.stop();
				}
				loop++;
			}
		}, 0, 1);
		return true;
	}

}
