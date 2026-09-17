package com.rs.game.player.dialogue.impl;

import com.rs.game.item.Item;
import com.rs.game.player.dialogue.Dialogue;

import java.util.ArrayList;

public class SunglassesD extends Dialogue {

	private int itemId;
	private final String[] options = { "Normal", "Dark blue", "Green", "Light blue", "Red", "Orange", "Black" };
	private final ArrayList<String> options_ = new ArrayList<String>();

	@Override
	public void start() {
		if(parameters[0] instanceof Integer)
			itemId = (int) parameters[0];
		for (int i = 0; i < options.length; i++) {
			if (i + 34030 != itemId)
				options_.add(options[i]);
		}
		sendOptionsDialogue("Choose a color", options_.get(0), options_.get(1), options_.get(2),
				options_.get(3), "More options..");
		stage = 0;
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case 0:
			int glassesGet = getOrdinal(componentId);
			switch (componentId) {
			case OPTION_1:
			case OPTION_2:
			case OPTION_3:
			case OPTION_4:
				for (int i = 0; i <= options.length - 1; i++) {
					if (options[i] == options_.get(glassesGet)) {sendItemDialogue(34030 + i, 1, "You recolored your sunglasses " + options[i] + "!");
						player.getEquipment().set(0, new Item(34030 + i));
						player.getEquipment().refresh(0);
						player.getAppearence().generateAppearenceData();
						stage = 2;
					}
				}
				break;
			case OPTION_5:
				sendOptionsDialogue("Choose a color", options_.get(4), options_.get(5), "Previous options..");
				stage = 1;
				break;
			}
			break;
		case 1:
			switch (componentId) {
			case OPTION_1:
			case OPTION_2:
				glassesGet = componentId == 11 ? 4 : 5;
				for (int i = 0; i <= options.length - 1; i++) {
					if (options[i] == options_.get(glassesGet)) {
						sendItemDialogue(34030 + i, 1,"You recolored your sunglasses " + options[i] + "!");
						player.getEquipment().set(0, new Item(34030 + i));
						player.getEquipment().refresh(0);
						player.getAppearence().generateAppearenceData();
						stage = 2;
					}
				}
			break;
			case OPTION_3:
				sendOptionsDialogue("Choose a color", options_.get(0), options_.get(1), options_.get(2), options_.get(3), "More options..");
				stage = 0;
				break;
			}
			break;
			
		case 2:
			finish();
			break;
		}
	}

	@Override
	public void finish() {
		player.getInterfaceManager().closeChatBoxInterface();
	}
}
