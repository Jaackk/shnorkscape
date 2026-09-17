package com.rs.game.player.dialogue.impl;

import com.rs.game.player.Player;
import com.rs.game.player.Titles;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.InputStringEvent;

import java.util.regex.Pattern;

public class CustomTitleD extends Dialogue {

	private static final Pattern SPACES = Pattern.compile("[ ]{2,}");

	@Override
	public void start() {
		sendOptionsDialogue("Select an Option", "Switch occurrance", "Set colour", "Set title", "Use title");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1) {
			switch (componentId) {
			case OPTION_1:
				sendOptionsDialogue("Select an Occurrance", "Set the title before your name", "Set the title after your name");
				break;
			case OPTION_2:
				end();
				player.sendInputString("Enter the colour in hex:", new InputStringEvent() {
					@Override
					public void run(Player player) {
						try {
							final String value = getString();
							Long.parseLong(value, 16);
							player.colour = value;
							player.getAppearence().generateAppearenceData();
							player.getPackets().sendIComponentText(1157, 69,
							player.getCustomTitle().equalsIgnoreCase(" null") ? "Not set" : !player.beforeName ? player.getDisplayName() + player.getCustomTitle() : player.getCustomTitle() + "</col>" + player.getDisplayName());
						} catch (NumberFormatException nfe) {
							player.sendMessage("The colour has to be entered in hexadecimal.");
						}
					}
				});
				break;
			case OPTION_3:
				end();
				player.sendInputString("Enter the title:", new InputStringEvent() {
					@Override
					public void run(Player player) {
						final String value = getString().trim();
						if(SPACES.matcher(value).find()) {
							player.sendMessage("Custom titles cannot contain 2 consecutive spaces.");
							return;
						}
						for (String profanity : Titles.UNALLOWED_TITLES) {
							if (value.equalsIgnoreCase(profanity) || profanity.length() > 3 && value.contains(profanity)) {
								player.sendMessage("You cannot use " + value + " as a title.");
								return;
							}
						}
						for (String existing : Titles.PRE_EXISTING_TITLES) {
							if (value.equalsIgnoreCase(existing)) {
								player.sendMessage("You cannot use " + value + " as a title.");
								return;
							}
						}
						if (value.equalsIgnoreCase("support") || value.contains("support") || value.contains("Support")) {
							if (!player.isSupport()) {
								player.sendMessage("You need to be a support to use this title.");
								return;
							}
						}
						if (value.equalsIgnoreCase("moderator") || value.contains("mode") || value.equalsIgnoreCase("mod")) {
							if (player.getRights() < 1) {
								player.sendMessage("You need to be a moderator to use this title.");
								return;
							}
						}
						if (value.equalsIgnoreCase("admin") || value.contains("admin") || value.contains("Admin")) {
							if (player.getRights() < 2) {
								player.sendMessage("You need to be an administrator to use this title.");
								return;
							}
						}
						if (value.equalsIgnoreCase("developer") || value.contains("develo") || value.contains("Develo")) {
							if (!player.isDev()) {
								player.sendMessage("You need to be a developer to use this title.");
								return;
							}
						}
						if (value.equalsIgnoreCase("owner") || value.contains("owner") || value.contains("Owner")) {
							if (!player.isDev()) {
								player.sendMessage("You need to be the owner to use this title.");
								return;
							}
						}
						if (!value.matches("[A-Za-z0-9 ]+")) {
							player.sendMessage("You cannot use invalid characters in a title.");
							return;
						}
						if (value.length() > 16) {
							player.sendMessage("The length of the title you entered has exceeded the maximum 16 allowed characters.");
							return;
						}
						player.title = value;
						player.getAppearence().generateAppearenceData();
						player.getPackets().sendIComponentText(1157, 69,
						player.getCustomTitle().equalsIgnoreCase(" null") ? "Not set" : !player.beforeName ? player.getDisplayName() + player.getCustomTitle() : player.getCustomTitle() + "</col>" + player.getDisplayName());

					}
				});
				break;
			case OPTION_4:
				if (player.title == null) {
					player.sendMessage("You need to construct a title before you can set it.");
					return;
				}
				player.getAppearence().setTitle(1600);
				end();
				break;
			}
		} else {
			switch (componentId) {
			case OPTION_1:
				player.beforeName = true;
				break;
			case OPTION_2:
				player.beforeName = false;
				break;
			}
			player.getAppearence().generateAppearenceData();
			player.getPackets().sendIComponentText(1157, 69, player.getCustomTitle().equalsIgnoreCase(" null") ? "Not set" : (!player.beforeName ? (player.getDisplayName() + player.getCustomTitle()) : player.getCustomTitle() + "</col>" + player.getDisplayName()));
			end();
		}
		stage++;
	}

	@Override
	public void finish() {

	}

}
