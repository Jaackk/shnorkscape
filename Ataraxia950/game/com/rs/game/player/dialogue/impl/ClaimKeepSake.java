package com.rs.game.player.dialogue.impl;

import com.rs.game.item.Item;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Utils;

import java.util.ArrayList;

public class ClaimKeepSake extends Dialogue {

	private int[][] pages;
	private int currentPage;
	private int maxPagesNeeded;
	private Item choosenItem;
	private ArrayList<Integer> availableCostumes;

	@Override
	public void start() {
		if (player.getEquipment().getKeepSakeItems().isEmpty()) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You don't have any keepsake items.");
			return;
		}
		availableCostumes = new ArrayList<>();
		for (Item item : player.getEquipment().getKeepSakeItems()) {
			if (item == null)
				continue;
			availableCostumes.add(item.getId());
		}
		stage = -1;
		currentPage = 0;
		sendOptionsDialogue("CHOOSE THE KEEPSAKE YOU WANT TO RECLAIM", getDialogueOptions());
	}

	private String[] getDialogueOptions() {
		maxPagesNeeded = ((int) Math.ceil(availableCostumes.size() / (currentPage == 0 ? 4.00 : 3.00)));
		maxPagesNeeded = maxPagesNeeded == 0 ? 1 : maxPagesNeeded;
		choosenItem = null;
		int index = 0;
		pages = new int[maxPagesNeeded][4];
		for (int i = 0; i < pages.length; i++) {
			for (int j = 0; j < (i > 0 ? (pages[i].length - 1) : pages[i].length); j++) {
				if (index > (availableCostumes.size() - 1))
					continue;
				pages[i][j] = availableCostumes.get(index);
				index++;
			}
		}
		ArrayList<String> dialogueOptions = new ArrayList<>(5);
		dialogueOptions.add(currentPage == 0
				? (Utils.formatPlayerNameForDisplay(new Item(pages[currentPage][0]).getName())) : "Back");
		int itemsCount = getItemsCount();
		if (currentPage == 0)
			itemsCount--;
		for (int i = 0; i < itemsCount; i++) {
			Item item = new Item(pages[currentPage][currentPage == 0 ? (i + 1) : i]);
			if (item.getId() != 0) {
				String name = Utils.formatPlayerNameForDisplay(item.getName());
				dialogueOptions.add(name);
			}
		}

		if (currentPage < (maxPagesNeeded - 1))
			dialogueOptions.add("More");
		else
			dialogueOptions.add("Cancel");
		String[] options = new String[dialogueOptions.size()];
		for (int i = 0; i < options.length; i++) {
			String option = dialogueOptions.get(i);
			if (option == null)
				continue;
			options[i] = option;
		}
		return options;
	}

	private int getItemsCount() {
		int itemsCount = 0;
		for (int i = 0; i < (currentPage == 0 ? pages[currentPage].length : (pages[currentPage].length - 1)); i++) {
			Item item = new Item(pages[currentPage][i]);
			if (item.getId() != 0)
				itemsCount++;
		}
		return itemsCount;
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			int itemsCount = getItemsCount();
			switch (componentId) {
			case OPTION_1:
				if (currentPage == 0) {
					choosenItem = new Item(pages[currentPage][0]);
					sendConfirmation();
				} else {// back
					currentPage--;
					sendOptionsDialogue("CHOOSE THE KEEPSAKE YOU WANT TO RECLAIM", getDialogueOptions());
				}
				break;
			case OPTION_2:
				if (currentPage == 0) {
					if (itemsCount > 1)
						choosenItem = new Item(pages[currentPage][1]);
				} else {
					if (itemsCount > 0)
						choosenItem = new Item(pages[currentPage][0]);
				}
				if (choosenItem != null)
					sendConfirmation();
				else
					end();
				break;
			case OPTION_3:
				if (currentPage == 0) {
					if (itemsCount > 2)
						choosenItem = new Item(pages[currentPage][2]);
				} else {
					if (itemsCount > 1)
						choosenItem = new Item(pages[currentPage][1]);
				}
				if (choosenItem != null)
					sendConfirmation();
				else
					end();
				break;
			case OPTION_4:
				if (currentPage == 0) {
					if (itemsCount > 3)
						choosenItem = new Item(pages[currentPage][3]);
				} else {
					if (itemsCount > 2)
						choosenItem = new Item(pages[currentPage][2]);
				}
				if (choosenItem != null)
					sendConfirmation();
				else
					end();
				break;
			case OPTION_5:
				if (currentPage < (maxPagesNeeded - 1)) {
					currentPage++;
					sendOptionsDialogue("CHOOSE THE KEEPSAKE YOU WANT TO RECLAIM", getDialogueOptions());
				} else
					end();
				break;
			}
			break;
		case 0:
			switch (componentId) {
			case OPTION_1:
				int index = getIndex();
				int equipSlot = choosenItem.getDefinitions().getEquipSlot();
				if (index == -1) {
					end();
					return;
				}
				Item item = player.getEquipment().getCosmeticItems().get(equipSlot);
				if (item != null && item.getId() == choosenItem.getId()) {
					player.getEquipment().getCosmeticItems().set(equipSlot, null);
					player.getAppearence().generateAppearenceData();
				}
				player.getEquipment().getKeepSakeItems().remove(index);
				if (!player.getInventory().addItem(choosenItem)) {
					player.getBank().addItem(choosenItem, true);
				}
				player.getPackets().sendGameMessage("You have re-claimed your item back from the keep sake box.");
				end();
				break;
			case OPTION_2:
				stage = -1;
				sendOptionsDialogue("CHOOSE THE KEEPSAKE YOU WANT TO RECLAIM", getDialogueOptions());
				break;
			case OPTION_3:
				end();
				break;
			}
			break;
		}
	}

	private int getIndex() {
		for (int i = 0; i < player.getEquipment().getKeepSakeItems().size(); i++) {
			Item item = player.getEquipment().getKeepSakeItems().get(i);
			if (item == null)
				continue;
			if (item.getId() == choosenItem.getId())
				return i;
		}
		return -1;
	}

	private void sendConfirmation() {
		stage = 0;
		sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Re-Claim item.", "Back.", "Cancel.");
	}

	@Override
	public void finish() {
	}

}
