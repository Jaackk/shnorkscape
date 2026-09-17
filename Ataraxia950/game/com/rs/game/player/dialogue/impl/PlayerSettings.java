package com.rs.game.player.dialogue.impl;

import com.rs.game.player.content.PlayerLook;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

import static com.rs.game.player.content.titles.PlayerTitle.PLAYER_OF_THE_MONTH;

/**
 * Used for handling the Player Settings dialogue.
 *
 * @author Noel
 */
public class PlayerSettings extends Dialogue {

	@Override
	public void start() {
		mainMenu();
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			switch (componentId) {
			case OPTION_1:
				finish();
				PlayerLook.openMageMakeOver(player);
				break;
			case OPTION_2:
				finish();
				PlayerLook.openCharacterCustomizing(player);
				break;
			case OPTION_3:
				finish();
				PlayerLook.openThessaliasMakeOver(player);
				break;
			case OPTION_4:
				if (!player.isDonator()) {
					player.getDialogueManager().startDialogue("SimpleMessage",
							"If you'd like to edit your skin tone you must contribute at least $20.");
					return;
				}
				player.getDialogueManager().startDialogue("Skincolor");
				break;
			case OPTION_5:
				if (!player.isPlayerOfTheMonth()) {
					end();
	                player.getInterfaceManager().openMenu(1, 2);
				} else {
					sendOptionsDialogue("Choose an Option", "Player of the Month settings" , (Colors.GREEN) + "Cosmetic Overrides", "Back...");
					stage = 110;
				}
				break;
			}
			break;
		case 0:
			stage = 0;
			switch (componentId) {
			case OPTION_1:
				player.getOverrides().showHelm = !player.getOverrides().showHelm;
				player.getOverrides().setOutfit(player.getOverrides().outfit);
				player.getAppearence().generateAppearenceData();
				sendOptionsDialogue("Cosmetic Override settings",
						(!player.getOverrides().showHelm ? Colors.GREEN : Colors.RED) + "Show Cosmetic helm",
						(!player.getOverrides().showBody ? Colors.GREEN : Colors.RED) + "Show Cosmetic body",
						(!player.getOverrides().showLegs ? Colors.GREEN : Colors.RED) + "Show Cosmetic legs",
						(!player.getOverrides().showCape ? Colors.GREEN : Colors.RED) + "Show Cosmetic cape",
						"More options..");
				break;
			case OPTION_2:
				player.getOverrides().showBody = !player.getOverrides().showBody;
				player.getOverrides().setOutfit(player.getOverrides().outfit);
				player.getAppearence().generateAppearenceData();
				sendOptionsDialogue("Cosmetic Override settings",
						(!player.getOverrides().showHelm ? Colors.GREEN : Colors.RED) + "Show Cosmetic helm",
						(!player.getOverrides().showBody ? Colors.GREEN : Colors.RED) + "Show Cosmetic body",
						(!player.getOverrides().showLegs ? Colors.GREEN : Colors.RED) + "Show Cosmetic legs",
						(!player.getOverrides().showCape ? Colors.GREEN : Colors.RED) + "Show Cosmetic cape",
						"More options..");
				break;
			case OPTION_3:
				player.getOverrides().showLegs = !player.getOverrides().showLegs;
				player.getOverrides().setOutfit(player.getOverrides().outfit);
				player.getAppearence().generateAppearenceData();
				sendOptionsDialogue("Cosmetic Override settings",
						(!player.getOverrides().showHelm ? Colors.GREEN : Colors.RED) + "Show Cosmetic helm",
						(!player.getOverrides().showBody ? Colors.GREEN : Colors.RED) + "Show Cosmetic body",
						(!player.getOverrides().showLegs ? Colors.GREEN : Colors.RED) + "Show Cosmetic legs",
						(!player.getOverrides().showCape ? Colors.GREEN : Colors.RED) + "Show Cosmetic cape",
						"More options..");
				break;
			case OPTION_4:
				player.getOverrides().showCape = !player.getOverrides().showCape;
				player.getOverrides().setOutfit(player.getOverrides().outfit);
				player.getAppearence().generateAppearenceData();
				sendOptionsDialogue("Cosmetic Override settings",
						(!player.getOverrides().showHelm ? Colors.GREEN : Colors.RED) + "Show Cosmetic helm",
						(!player.getOverrides().showBody ? Colors.GREEN : Colors.RED) + "Show Cosmetic body",
						(!player.getOverrides().showLegs ? Colors.GREEN : Colors.RED) + "Show Cosmetic legs",
						(!player.getOverrides().showCape ? Colors.GREEN : Colors.RED) + "Show Cosmetic cape",
						"More options..");
				break;
			case OPTION_5:
				stage = 1;
				sendOptionsDialogue("Cosmetic Override settings",
						(!player.getOverrides().showBoots ? Colors.GREEN : Colors.RED) + "Show Cosmetic boots",
						(!player.getOverrides().showGloves ? Colors.GREEN : Colors.RED) + "Show Cosmetic gloves",
						(!player.getOverrides().showWeapon ? Colors.GREEN : Colors.RED) + "Show Cosmetic weapon",
						(!player.getOverrides().showShield ? Colors.GREEN : Colors.RED) + "Show Cosmetic shield",
						"More options..");
				break;
			}
			break;
		case 1:
			stage = 1;
			switch (componentId) {
			case OPTION_1:
				player.getOverrides().showBoots = !player.getOverrides().showBoots;
				player.getOverrides().setOutfit(player.getOverrides().outfit);
				player.getAppearence().generateAppearenceData();
				sendOptionsDialogue("Cosmetic Override settings",
						(!player.getOverrides().showBoots ? Colors.GREEN : Colors.RED) + "Show Cosmetic boots",
						(!player.getOverrides().showGloves ? Colors.GREEN : Colors.RED) + "Show Cosmetic gloves",
						(!player.getOverrides().showWeapon ? Colors.GREEN : Colors.RED) + "Show Cosmetic weapon",
						(!player.getOverrides().showShield ? Colors.GREEN : Colors.RED) + "Show Cosmetic shield",
						"More options..");
				break;
			case OPTION_2:
				player.getOverrides().showGloves = !player.getOverrides().showGloves;
				player.getOverrides().setOutfit(player.getOverrides().outfit);
				player.getAppearence().generateAppearenceData();
				sendOptionsDialogue("Cosmetic Override settings",
						(!player.getOverrides().showBoots ? Colors.GREEN : Colors.RED) + "Show Cosmetic boots",
						(!player.getOverrides().showGloves ? Colors.GREEN : Colors.RED) + "Show Cosmetic gloves",
						(!player.getOverrides().showWeapon ? Colors.GREEN : Colors.RED) + "Show Cosmetic weapon",
						(!player.getOverrides().showShield ? Colors.GREEN : Colors.RED) + "Show Cosmetic shield",
						"More options..");
				break;
			case OPTION_3:
				player.getOverrides().showWeapon = !player.getOverrides().showWeapon;
				player.getOverrides().setOutfit(player.getOverrides().outfit);
				player.getAppearence().generateAppearenceData();
				sendOptionsDialogue("Cosmetic Override settings",
						(!player.getOverrides().showBoots ? Colors.GREEN : Colors.RED) + "Show Cosmetic boots",
						(!player.getOverrides().showGloves ? Colors.GREEN : Colors.RED) + "Show Cosmetic gloves",
						(!player.getOverrides().showWeapon ? Colors.GREEN : Colors.RED) + "Show Cosmetic weapon",
						(!player.getOverrides().showShield ? Colors.GREEN : Colors.RED) + "Show Cosmetic shield",
						"More options..");
				break;
			case OPTION_4:
				player.getOverrides().showShield = !player.getOverrides().showShield;
				player.getOverrides().setOutfit(player.getOverrides().outfit);
				player.getAppearence().generateAppearenceData();
				sendOptionsDialogue("Cosmetic Override settings",
						(!player.getOverrides().showBoots ? Colors.GREEN : Colors.RED) + "Show Cosmetic boots",
						(!player.getOverrides().showGloves ? Colors.GREEN : Colors.RED) + "Show Cosmetic gloves",
						(!player.getOverrides().showWeapon ? Colors.GREEN : Colors.RED) + "Show Cosmetic weapon",
						(!player.getOverrides().showShield ? Colors.GREEN : Colors.RED) + "Show Cosmetic shield",
						"More options..");
				break;
			case OPTION_5:
				stage = 0;
				sendOptionsDialogue("Cosmetic Override settings",
						(!player.getOverrides().showHelm ? Colors.GREEN : Colors.RED) + "Show Cosmetic helm",
						(!player.getOverrides().showBody ? Colors.GREEN : Colors.RED) + "Show Cosmetic body",
						(!player.getOverrides().showLegs ? Colors.GREEN : Colors.RED) + "Show Cosmetic legs",
						(!player.getOverrides().showCape ? Colors.GREEN : Colors.RED) + "Show Cosmetic cape",
						"More options..");
				break;
			}
			break;
		case 99:
			finish();
			break;
			case 110:
				switch (componentId) {
					case OPTION_1:
						sendOptionsDialogue("Choose an Option", "Toggle player of the Month icon" , "Set player of the Month title", "Back...");
						stage = 111;
						break;
					case OPTION_2:
						end();
		                player.getInterfaceManager().openMenu(1, 2);
						break;
					case OPTION_3:
						mainMenu();
						stage = -1;
						break;
				}
				break;
			case 111:
				switch (componentId) {
					case OPTION_1:
						player.togglePlayerOfTheMonthIcon();
						player.sendMessage("Your player of the month icon has been " + (player.isDisplayPlayerOfTheMonthIcon() ? "enabled." : "disabled."));
						end();
						break;
					case OPTION_2:
						sendOptionsDialogue("Would you like the long or short version of the title?", "Long", "Short", "Back...");
						stage = 112;
						break;
					case OPTION_3:
						sendOptionsDialogue("Choose an Option", "Player of the Month settings" , (Colors.GREEN) + "Cosmetic Overrides", "Back...");
						stage = 110;
						break;
				}
				break;
			case 112:
				switch (componentId) {
					case OPTION_1:
					case OPTION_2:
						player.getAppearence().setTitle(PLAYER_OF_THE_MONTH.getTitleId());
						player.sendMessage("Your player of the Month title has been set!");
						end();
						break;
					case OPTION_3:
						sendOptionsDialogue("Choose an Option", "Toggle player of the Month icon" , "Set player of the Month title", "Back...");
						stage = 111;
						break;
				}
				break;
		}
	}

	@Override
	public void finish() {
		player.getInterfaceManager().closeChatBoxInterface();
	}

	private void mainMenu() {
		if (!player.isPlayerOfTheMonth()) {
			sendOptionsDialogue(Colors.CYAN + "Appearence Settings", "Make-over Mage", "Player customization", "Thessalia's Clothes", "Skin Color", (Colors.GREEN) + "Cosmetic Overrides");
		} else {
			sendOptionsDialogue(Colors.CYAN + "Appearence Settings", "Make-over Mage", "Player customization", "Thessalia's Clothes", "Skin Color", (Colors.RED) + "More options...");
		}
	}

}