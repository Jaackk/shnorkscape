package com.rs.game.player.dialogue.impl;

import com.rs.game.player.Equipment;
import com.rs.game.player.dialogue.Dialogue;

public class ChicScarfD extends Dialogue {

	private int itemId;
	private boolean wearing;
	private static final String[] OPTIONS = { "Red", "Black", "White", "Purple", "Blue", "Green", "Golden", "Orange", "Pink", "Grey", "Rainbow" };
	private static final int[] IDS = { 37130, 39298, 39299, 39300, 39301, 39302, 39303, 39304, 39305, 39306, 39307 };
	
	private final String[] options = new String[OPTIONS.length - 1];

	@Override
	public void start() {
		itemId = (int) parameters[0];
		wearing = (boolean) parameters[1];
		stage = 0;
		boolean skip = false;
		for (int i = 0; i < IDS.length; i++) {
			if (itemId != IDS[i])
				options[skip ? i - 1 : i] = OPTIONS[i];
			else
				skip = true;
		}
		sendOptionsDialogue("Select a Colour", getOptions());
	}
	
	private String[] getOptions() {
		if (stage == 2) {
			String[] o = new String[3];
			for (int i = 0; i < 2; i++) {
				o[i] = options[8 + i];
				o[2] = "First page";
			}
			return o;
		} else {
			String[] o = new String[5];
			for (int i = 0; i < 4; i++) {
				o[i] = options[(stage * 4) + i];
				o[4] = "Next page";
			}
			return o;
		}
	}
	
	private int getIdForString(String string) {
		for (int i = 0; i < OPTIONS.length; i++) {
			if (OPTIONS[i].equals(string))
				return IDS[i];
		}
		return -1;
	}

	@Override
	public void run(int interfaceId, int componentId) {
		final int index = componentAsIndex(componentId);
		if (index < (stage == 2 ? 2 : 4)) {
			final int id = getIdForString(options[(stage * 4) + index]);
			if (wearing) {
				player.getEquipment().set(Equipment.SLOT_AMULET, new com.rs.game.item.Item(id));
				player.getEquipment().refresh(Equipment.SLOT_AMULET);
				player.getAppearence().generateAppearenceData();
			} else {
				player.getInventory().deleteItem(itemId, 1);
				player.getInventory().addItem(id, 1);
			}
			end();
		} else {
			if (stage == 2)
				stage = 0;
			else
				stage++;
			sendOptionsDialogue("Select a Colour", getOptions());
		}
	}

	@Override
	public void finish() {}
}
