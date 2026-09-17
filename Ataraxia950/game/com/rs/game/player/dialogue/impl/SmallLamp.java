package com.rs.game.player.dialogue.impl;

import com.rs.game.player.Skills;
import com.rs.game.player.dialogue.Dialogue;

/**
 * Handles the Small XP lamp's primitive dialogue <.<
 *
 * @author Noel
 */
public class SmallLamp extends Dialogue {

	/**
	 * Int representing the base EXP gain.
	 */
	int XP = 250;

	/**
	 * Int representing the Lamp ID.
	 */
	int LAMP = 23713;

	@Override
	public void start() {
		sendOptionsDialogue("What skill would you like to gain experience in?", "Attack", "Strength", "Defence",
				"Ranged", "More options...");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			if (componentId == OPTION_1) {
				player.getSkills().addXp(Skills.ATTACK, XP + player.getSkills().getLevelForXp(Skills.ATTACK));
				destroyLamp();
			} else if (componentId == OPTION_2) {
				player.getSkills().addXp(Skills.STRENGTH, XP + player.getSkills().getLevelForXp(Skills.STRENGTH));
				destroyLamp();
			} else if (componentId == OPTION_3) {
				player.getSkills().addXp(Skills.DEFENCE, XP + player.getSkills().getLevelForXp(Skills.DEFENCE));
				destroyLamp();
			} else if (componentId == OPTION_4) {
				player.getSkills().addXp(Skills.RANGE, XP + player.getSkills().getLevelForXp(Skills.RANGE));
				destroyLamp();
			} else {
				sendOptionsDialogue("What skill would you like to gain experience in?", "Prayer", "Magic",
						"Constitution", "Summoning", "More options...");
				stage = 1;
			}
			break;
		case 1:
			if (componentId == OPTION_1) {
				player.getSkills().addXp(Skills.PRAYER, XP + player.getSkills().getLevelForXp(Skills.PRAYER));
				destroyLamp();
			} else if (componentId == OPTION_2) {
				player.getSkills().addXp(Skills.MAGIC, XP + player.getSkills().getLevelForXp(Skills.MAGIC));
				destroyLamp();
			} else if (componentId == OPTION_3) {
				player.getSkills().addXp(Skills.HITPOINTS, XP + player.getSkills().getLevelForXp(Skills.HITPOINTS));
				destroyLamp();
			} else if (componentId == OPTION_4) {
				player.getSkills().addXp(Skills.SUMMONING, XP + player.getSkills().getLevelForXp(Skills.SUMMONING));
				destroyLamp();
			} else {
				sendOptionsDialogue("What skill would you like to gain experience in?", "Agility", "Herblore",
						"Thieving", "Crafting", "More options...");
				stage = 2;
			}
			break;
		case 2:
			if (componentId == OPTION_1) {
				player.getSkills().addXp(Skills.AGILITY, XP + player.getSkills().getLevelForXp(Skills.AGILITY));
				destroyLamp();
			} else if (componentId == OPTION_2) {
				player.getSkills().addXp(Skills.HERBLORE, XP + player.getSkills().getLevelForXp(Skills.HERBLORE));
				destroyLamp();
			} else if (componentId == OPTION_3) {
				player.getSkills().addXp(Skills.THIEVING, XP + player.getSkills().getLevelForXp(Skills.THIEVING));
				destroyLamp();
			} else if (componentId == OPTION_4) {
				player.getSkills().addXp(Skills.CRAFTING, XP + player.getSkills().getLevelForXp(Skills.CRAFTING));
				destroyLamp();
			} else {
				sendOptionsDialogue("What skill would you like to gain experience in?", "Fletching", "Runecrafting",
						"Construction", "Slayer", "More options...");
				stage = 3;
			}
			break;
		case 3:
			if (componentId == OPTION_1) {
				player.getSkills().addXp(Skills.FLETCHING, XP + player.getSkills().getLevelForXp(Skills.FLETCHING));
				destroyLamp();
			} else if (componentId == OPTION_2) {
				player.getSkills().addXp(Skills.RUNECRAFTING,
						XP + player.getSkills().getLevelForXp(Skills.RUNECRAFTING));
				destroyLamp();
			} else if (componentId == OPTION_3) {
				player.getSkills().addXp(Skills.CONSTRUCTION,
						XP + player.getSkills().getLevelForXp(Skills.CONSTRUCTION));
				destroyLamp();
			} else if (componentId == OPTION_4) {
				player.getSkills().addXp(Skills.SLAYER, XP + player.getSkills().getLevelForXp(Skills.SLAYER));
				destroyLamp();
			} else {
				sendOptionsDialogue("What skill would you like to gain experience in?", "Hunter", "Mining", "Smithing",
						"Fishing", "More options...");
				stage = 4;
			}
			break;
		case 4:
			if (componentId == OPTION_1) {
				player.getSkills().addXp(Skills.HUNTER, XP + player.getSkills().getLevelForXp(Skills.HUNTER));
				destroyLamp();
			} else if (componentId == OPTION_2) {
				player.getSkills().addXp(Skills.MINING, XP + player.getSkills().getLevelForXp(Skills.MINING));
				destroyLamp();
			} else if (componentId == OPTION_3) {
				player.getSkills().addXp(Skills.SMITHING, XP + player.getSkills().getLevelForXp(Skills.SMITHING));
				destroyLamp();
			} else if (componentId == OPTION_4) {
				player.getSkills().addXp(Skills.FISHING, XP + player.getSkills().getLevelForXp(Skills.FISHING));
				destroyLamp();
			} else {
				sendOptionsDialogue("What skill would you like to gain experience in?", "Cooking", "Firemaking",
						"Woodcutting", "Farming", "Divination");
				stage = 5;
			}
			break;
		case 5:
			if (componentId == OPTION_1) {
				player.getSkills().addXp(Skills.COOKING, XP + player.getSkills().getLevelForXp(Skills.COOKING));
				destroyLamp();
			} else if (componentId == OPTION_2) {
				player.getSkills().addXp(Skills.FIREMAKING, XP + player.getSkills().getLevelForXp(Skills.FIREMAKING));
				destroyLamp();
			} else if (componentId == OPTION_3) {
				player.getSkills().addXp(Skills.WOODCUTTING, XP + player.getSkills().getLevelForXp(Skills.WOODCUTTING));
				destroyLamp();
			} else if (componentId == OPTION_4) {
				player.getSkills().addXp(Skills.FARMING, XP + player.getSkills().getLevelForXp(Skills.FARMING));
				destroyLamp();
			} else if (componentId == OPTION_5) {
				player.getSkills().addXp(Skills.DIVINATION, XP + player.getSkills().getLevelForXp(Skills.DIVINATION));
				destroyLamp();
			}
			break;
		}
	}

	/**
	 * Gets rid of the Lamp.
	 */
	public void destroyLamp() {
		player.getInventory().deleteItem(LAMP, 1);
		finish();
	}

	@Override
	public void finish() {
		player.getInterfaceManager().closeChatBoxInterface();
	}
}