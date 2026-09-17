package com.rs.game.player.actions.magic.lunar.impl;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.magic.lunar.DefaultSpell;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

public class NPCContact implements DefaultSpell {

	private static final Item[] RUNES = new Item[] { new Item(ASTRAL_RUNE, 1), new Item(COSMIC_RUNE, 1), new Item(AIR_RUNE, 2) };
	
	@Override
	public int getId() {
		return 14828;
	}

	@Override
	public int getLevel() {
		return 67;
	}

	@Override
	public Item[] getRunes() {
		return RUNES;
	}

	@Override
	public int getDelay() {
		return 6000;
	}
	
	public static final void handleInterface(final Player player, final int slotId) {
		player.setLunarDelay(4000);
		player.sendMessage("You attempt to create a contact..");
		for (Item i : RUNES)
			player.getInventory().deleteItem(i);
		player.setNextAnimation(new Animation(4413));
		player.setNextGraphics(new Graphics(728));
		player.getSkills().addXp(Skills.MAGIC, 63);
		player.getInterfaceManager().closeScreenInterface();
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				switch (slotId) {
				case 5:
					player.getDialogueManager().startDialogue("HonestJimmyD");
					return;
				case 4:
					player.getDialogueManager().startDialogue("LanthusD");
					return;
				case 3:
					player.getDialogueManager().startDialogue("Kuradal", 8273);
					return;
				case 6:
					player.getDialogueManager().startDialogue("Kuradal", 8274);
					return;
				case 7:
					player.getDialogueManager().startDialogue("Kuradal", 8275);
					return;
				case 8:
					player.getDialogueManager().startDialogue("Kuradal", 1597);
					return;
				case 9:
					player.getDialogueManager().startDialogue("MurphyD");
					return;
				case 10:
					player.getDialogueManager().startDialogue("Kuradal", 1598);
					return;
				case 13:
					player.getDialogueManager().startDialogue("Kuradal", 9085);
					return;
				case 15:
					player.getDialogueManager().startDialogue("PikkupstixD");
					return;
				case 17:
					player.getDialogueManager().startDialogue("OneiromancerD");
					return;
				}
			}
		}, 7);
	}

	@Override
	public boolean spellEffect(Player player) {
		player.getInterfaceManager().sendInterface(88);
		player.getPackets().sendUnlockIComponentOptionSlots(88, 1, 0, 50, 0, 1, 2, 3);
		return false;
	}

}
