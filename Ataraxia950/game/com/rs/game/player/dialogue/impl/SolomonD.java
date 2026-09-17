package com.rs.game.player.dialogue.impl;

import com.rs.Settings;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

/**
 * Handles Solomons dialogue and all shop things.
 *
 * @author Noel
 */
public class SolomonD extends Dialogue {

	/**
	 * Integer representing Solomon's ID.
	 */
	private int npcId;

	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		sendNPCDialogue(npcId, NORMAL, "Welcome to Solomon's General store! <br>How may I be of assistance?");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			if (player.getOverrides().outfit == null)
				sendOptionsDialogue("Select an Option", "What is this place?", "How do I buy things here?",
						"I'd like to buy something.", "I'd like to see all available items.");
			else
				sendOptionsDialogue("Select an Option", "What is this place?", "How do I buy things here?",
						"I'd like to buy something.", "I'd like to see all available items.",
						"Could you remove my cosmetic override, please?");
			stage = 0;
			break;
		case 0:
			switch (componentId) {
			case OPTION_1:
				sendNPCDialogue(npcId, JUST_LISTEN,
						"This is Solomons General store, the finest retailer " + "in the corporeal realm.");
				stage = 1;
				break;
			case OPTION_2:
				sendNPCDialogue(npcId, NORMAL, "Simply enter the store, select the items you want, and "
						+ "complete your transaction using loyalty points or money!");
				stage = 2;
				break;
			case OPTION_3:
				sendOptionsDialogue("Visit the online store?", "Yes.", "No.");
				stage = 3;
				break;
			case OPTION_4:
				sendNPCDialogue(npcId, NORMAL,
						"Certainly! If the item is marked in " + Colors.GREEN
								+ "green</col> - it means you can use it. If it's marked in " + Colors.RED
								+ "red</col> - it means you have to unlock it by " + "visiting our ::store page!");
				stage = 11;
				break;
			case OPTION_5:
				sendNPCDialogue(npcId, NORMAL, "As you wish!");
				player.getOverrides().resetCosmetics();
				stage = 10;
				break;
			}
			break;
		case 1:
			sendNPCDialogue(npcId, NORMAL, "Here you can buy a selection of fine cosmetic items.");
			stage = -1;
			break;
		case 2:
			sendNPCDialogue(npcId, NORMAL, "It couldn't be simpler.");
			stage = -1;
			break;
		case 3:
			switch (componentId) {
			case OPTION_1:
				sendDialogue("Opening the online store website..");
				player.getPackets().sendOpenURL(Settings.DONATE + "/?page=1&category=4");
				stage = 127;
				break;
			case OPTION_2:
				sendOptionsDialogue("Select an Option", "What is this place?", "How do I buy things here?",
						"I'd like to buy something.", "I'd like to see all available items.");
				stage = 0;
				break;
			}
			break;
		case 4:
			sendOptionsDialogue("Select an Override to use",
					(player.getOverrides().paladin ? Colors.GREEN : Colors.RED) + "Paladin Cosmetical Override",
					(player.getOverrides().warlord ? Colors.GREEN : Colors.RED) + "Warlord Cosmetical Override",
					(player.getOverrides().obsidian ? Colors.GREEN : Colors.RED) + "Obsidian Cosmetical Override",
					(player.getOverrides().kalphite ? Colors.GREEN : Colors.RED) + "Kalphite Cosmetical Override",
					"More options..");
			stage = 5;
			break;
		case 5:
			switch (componentId) {
			case OPTION_1:
				if (!player.isOwner() && !player.getOverrides().paladin) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item " + "in order to unlock it.");
					stage = 4;
					return;
				}
				player.getOverrides().setPaladinOutfit();
				stage = 99;
				break;
			case OPTION_2:
				if (!player.isOwner() && !player.getOverrides().warlord) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item " + "in order to unlock it.");
					stage = 4;
					return;
				}
				player.getOverrides().setWarlordOutfit();
				stage = 99;
				break;
			case OPTION_3:
				if (!player.isOwner() && !player.getOverrides().obsidian) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item " + "in order to unlock it.");
					stage = 4;
					return;
				}
				player.getOverrides().setObsidianOutfit();
				stage = 99;
				break;
			case OPTION_4:
				if (!player.isOwner() && !player.getOverrides().kalphite) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item " + "in order to unlock it.");
					stage = 4;
					return;
				}
				player.getOverrides().setKalphiteOutfit();
				stage = 99;
				break;
			case OPTION_5:
				sendOptionsDialogue("Select an Override to use",
						(player.getOverrides().demonflesh ? Colors.GREEN : Colors.RED)
								+ "Demonflesh Cosmetical Override",
						(player.getOverrides().remokee ? Colors.GREEN : Colors.RED) + "Remokee Cosmetical Override",
						(player.getOverrides().assassin ? Colors.GREEN : Colors.RED) + "Assassin Cosmetical Override",
						(player.getOverrides().skeleton ? Colors.GREEN : Colors.RED) + "Skeleton Cosmetical Override",
						"More options..");
				stage = 6;
				break;
			}
			break;
		case 6:
			switch (componentId) {
			case OPTION_1:
				if (!player.isOwner() && !player.getOverrides().demonflesh) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item " + "in order to unlock it.");
					stage = 4;
					return;
				}
				player.getOverrides().setDemonfleshOutfit();
				stage = 99;
				break;
			case OPTION_2:
				if (!player.isOwner() && !player.getOverrides().remokee) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item " + "in order to unlock it.");
					stage = 4;
					return;
				}
				player.getOverrides().setRemokeeOutfit();
				stage = 99;
				break;
			case OPTION_3:
				if (!player.isOwner() && !player.getOverrides().assassin) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item " + "in order to unlock it.");
					stage = 4;
					return;
				}
				player.getOverrides().setAssassinOutfit();
				stage = 99;
				break;
			case OPTION_4:
				if (!player.isOwner() && !player.getOverrides().skeleton) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item in order to unlock it.");
					stage = 4;
					return;
				}
				player.getOverrides().setSkeletonOutfit();
				stage = 99;
				break;
			case OPTION_5:
				sendOptionsDialogue("Select an Override to use",
						(player.getOverrides().goth ? Colors.GREEN : Colors.RED) + "Goth Cosmetical Override",
						(player.getOverrides().mummy ? Colors.GREEN : Colors.RED) + "Mummy Cosmetical Override",
						(player.getOverrides().replicaDragon ? Colors.GREEN : Colors.RED)
								+ "Replica Dragon Cosmetical Override",
						(player.getOverrides().sentinel ? Colors.GREEN : Colors.RED) + "Sentinel Cosmetical Override",
						"More options..");
				stage = 7;
				break;
			}
			break;
		case 7:
			switch (componentId) {
			case OPTION_1:
				if (!player.isOwner() && !player.getOverrides().goth) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item " + "in order to unlock it.");
					stage = 4;
					return;
				}
				player.getOverrides().setGothOutfit();
				stage = 99;
				break;
			case OPTION_2:
				if (!player.isOwner() && !player.getOverrides().mummy) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item " + "in order to unlock it.");
					stage = 4;
					return;
				}
				player.getOverrides().setMummyOutfit();
				stage = 99;
				break;
			case OPTION_3:
				if (!player.isOwner() && !player.getOverrides().replicaDragon) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item " + "in order to unlock it.");
					stage = 4;
					return;
				}
				player.getOverrides().setReplicaDragonOutfit();
				stage = 99;
				break;
			case OPTION_4:
				if (!player.isOwner() && !player.getOverrides().sentinel) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item " + "in order to unlock it.");
					stage = 4;
					return;
				}
				player.getOverrides().setSentinelOutfit();
				stage = 99;
				break;
			case OPTION_5:
				sendOptionsDialogue("Select an Override to use",
						(player.getOverrides().reaver ? Colors.GREEN : Colors.RED) + "Reaver Cosmetical Override",
						(player.getOverrides().hiker ? Colors.GREEN : Colors.RED) + "Hiker Cosmetical Override",
						(player.getOverrides().skyguard ? Colors.GREEN : Colors.RED) + "Skyguard Cosmetical Override",
						(player.getOverrides().vyrewatch ? Colors.GREEN : Colors.RED) + "Vyrewatch Cosmetical Override",
						"More options..");
				stage = 8;
				break;
			}
			break;
		case 8:
			switch (componentId) {
			case OPTION_1:
				if (!player.isOwner() && !player.getOverrides().reaver) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item " + "in order to unlock it.");
					stage = 4;
					return;
				}
				player.getOverrides().setReaverOutfit();
				stage = 99;
				break;
			case OPTION_2:
				if (!player.isOwner() && !player.getOverrides().hiker) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item " + "in order to unlock it.");
					stage = 4;
					return;
				}
				player.getOverrides().setHikerOutfit();
				stage = 99;
				break;
			case OPTION_3:
				if (!player.isOwner() && !player.getOverrides().skyguard) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item " + "in order to unlock it.");
					stage = 4;
					return;
				}
				player.getOverrides().setSkyguardOutfit();
				stage = 99;
				break;
			case OPTION_4:
				if (!player.isOwner() && !player.getOverrides().vyrewatch) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item " + "in order to unlock it.");
					stage = 4;
					return;
				}
				player.getOverrides().setVyrewatchOutfit();
				stage = 99;
				break;
			case OPTION_5:
				sendOptionsDialogue("Select an Override to use",
						(player.getOverrides().snowman ? Colors.GREEN : Colors.RED) + "Snowman Cosmetical Override",
						(player.getOverrides().samurai ? Colors.GREEN : Colors.RED) + "Samurai Cosmetical Override",
						(player.getOverrides().warmWinter ? Colors.GREEN : Colors.RED)
								+ "Warm Winter Cosmetical Override",
						(player.getOverrides().darkLord ? Colors.GREEN : Colors.RED) + "Dark Lord Cosmetical Override",
						"More options..");
				stage = 9;
				break;
			}
			break;
		case 9:
			switch (componentId) {
			case OPTION_1:
				if (!player.isOwner() && !player.getOverrides().snowman) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item " + "in order to unlock it.");
					stage = 4;
					return;
				}
				player.getOverrides().setSnowmanOutfit();
				stage = 99;
				break;
			case OPTION_2:
				if (!player.isOwner() && !player.getOverrides().samurai) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item " + "in order to unlock it.");
					stage = 4;
					return;
				}
				player.getOverrides().setSamuraiOutfit();
				stage = 99;
				break;
			case OPTION_3:
				if (!player.isOwner() && !player.getOverrides().warmWinter) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item " + "in order to unlock it.");
					stage = 4;
					return;
				}
				player.getOverrides().setWarmWinterOutfit();
				stage = 99;
				break;
			case OPTION_4:
				if (!player.isOwner() && !player.getOverrides().darkLord) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item " + "in order to unlock it.");
					stage = 4;
					return;
				}
				player.getOverrides().setDarkLordOutfit();
				stage = 99;
				break;
			case OPTION_5:
				sendOptionsDialogue("Select an Override to use",
						(player.getOverrides().paladin ? Colors.GREEN : Colors.RED) + "Paladin Cosmetical Override",
						(player.getOverrides().warlord ? Colors.GREEN : Colors.RED) + "Warlord Cosmetical Override",
						(player.getOverrides().obsidian ? Colors.GREEN : Colors.RED) + "Obsidian Cosmetical Override",
						(player.getOverrides().kalphite ? Colors.GREEN : Colors.RED) + "Kalphite Cosmetical Override",
						"More options..");
				stage = 5;
				break;
			}
			break;
		case 10:
			sendDialogue("Solomon has removed your Cosmetic Overrides.");
			stage = 99;
			break;

		case 11:
			sendOptionsDialogue("Select an Option", "Cosmetic Overrides", "Animation Overrides");
			stage = 12;
			break;
		case 12:
			switch (componentId) {
			case OPTION_1:
				sendPlayerDialogue(NORMAL, "I'd like to view all of your available Cosmetic Overrides!");
				stage = 4;
				break;
			case OPTION_2:
				sendPlayerDialogue(NORMAL, "I'd like to view all of your available Animation Overrides!");
				stage = 13;
				break;
			}
			break;
		case 13:
			sendOptionsDialogue("Select an Override to use",
					(player.getAnimations().hasBattleCry
							? Colors.GREEN + "Slayer Battle Cry - unlocked - "
									+ (player.getAnimations().battleCry ? "enabled" : Colors.RED + "disabled")
							: Colors.RED + "Slayer Battle Cry - LOCKED"),
					(player.getAnimations().hasEnhancedPotion
							? Colors.GREEN + "Enhanced Potion Making - unlocked - "
									+ (player.getAnimations().enhancedPotion ? "enabled" : Colors.RED + "disabled")
							: Colors.RED + "Enhanced Potion Making - LOCKED"),
					(player.getAnimations().hasLumberjackWc
							? Colors.GREEN + "Lumberjack Woodcutting - unlocked - "
									+ (player.getAnimations().lumberjackWc ? "enabled" : Colors.RED + "disabled")
							: Colors.RED + "Lumberjack Woodcutting - LOCKED"),
					(player.getAnimations().hasDeepFishing
							? Colors.GREEN + "Deep-Sea Fishing - unlocked - "
									+ (player.getAnimations().deepFishing ? "enabled" : Colors.RED + "disabled")
							: Colors.RED + "Deep-Sea Fishing - LOCKED"),
					"More options..");
			stage = 14;
			break;
		case 14:
			stage = 13;
			switch (componentId) {
			case OPTION_1:
				if (!player.isOwner() && !player.getAnimations().hasBattleCry) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
					return;
				}
				player.getAnimations().battleCry = !player.getAnimations().battleCry;
				sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().battleCry ? "enabled" : "disabled")
						+ " the 'Slayer Battle Cry' Animation Override for you!");
				player.getAppearence().generateAppearenceData();
				break;
			case OPTION_2:
				if (!player.isOwner() && !player.getAnimations().hasEnhancedPotion) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
					return;
				}
				player.getAnimations().enhancedPotion = !player.getAnimations().enhancedPotion;
				sendNPCDialogue(npcId, NORMAL,
						"I've " + (player.getAnimations().enhancedPotion ? "enabled" : "disabled")
								+ " the 'Enhanced Potion Making' Animation Override for you!");
				player.getAppearence().generateAppearenceData();
				break;
			case OPTION_3:
				if (!player.isOwner() && !player.getAnimations().hasLumberjackWc) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
					return;
				}
				player.getAnimations().lumberjackWc = !player.getAnimations().lumberjackWc;
				sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().lumberjackWc ? "enabled" : "disabled")
						+ " the 'Lumberjack Woodcutting' Animation Override for you!");
				player.getAppearence().generateAppearenceData();
				break;
			case OPTION_4:
				if (!player.isOwner() && !player.getAnimations().hasDeepFishing) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
					return;
				}
				player.getAnimations().deepFishing = !player.getAnimations().deepFishing;
				sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().deepFishing ? "enabled" : "disabled")
						+ " the 'Deep-Sea Fishing' Animation Override for you!");
				player.getAppearence().generateAppearenceData();
				break;
			case OPTION_5:
				sendOptionsDialogue("Select an Override to use",
						(player.getAnimations().hasZenResting
								? Colors.GREEN + "Zen Resting - unlocked - "
										+ (player.getAnimations().zenResting ? "enabled" : Colors.RED + "disabled")
								: Colors.RED + "Zen Resting - LOCKED"),
						(player.getAnimations().hasKarateFletch
								? Colors.GREEN + "Karate-Chop Fletching - unlocked - "
										+ (player.getAnimations().karateFletch ? "enabled" : Colors.RED + "disabled")
								: Colors.RED + "Karate-Chop Fletching - LOCKED"),
						(player.getAnimations().hasIronSmith
								? Colors.GREEN + "Iron-Fist Smithing - unlocked - "
										+ (player.getAnimations().ironSmith ? "enabled" : Colors.RED + "disabled")
								: Colors.RED + "Iron-Fist Smithing - LOCKED"),
						(player.getAnimations().hasChiMining
								? Colors.GREEN + "Chi-Blast Mining - unlocked - "
										+ (player.getAnimations().chiMining ? "enabled" : Colors.RED + "disabled")
								: Colors.RED + "Chi-Blast Mining - LOCKED"),
						"More options..");
				stage = 15;
				break;
			}
			break;
		case 15:
			stage = 90;
			switch (componentId) {
			case OPTION_1:
				if (!player.isOwner() && !player.getAnimations().hasZenResting) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
					return;
				}
				player.getAnimations().zenResting = !player.getAnimations().zenResting;
				sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().zenResting ? "enabled" : "disabled")
						+ " the 'Zen Resting' Animation Override for you!");
				player.getAppearence().generateAppearenceData();
				break;
			case OPTION_2:
				if (!player.isOwner() && !player.getAnimations().hasKarateFletch) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
					return;
				}
				player.getAnimations().karateFletch = !player.getAnimations().karateFletch;
				sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().karateFletch ? "enabled" : "disabled")
						+ " the 'Karate-Chop Fletching' Animation Override for you!");
				player.getAppearence().generateAppearenceData();
				break;
			case OPTION_3:
				if (!player.isOwner() && !player.getAnimations().hasIronSmith) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
					return;
				}
				player.getAnimations().ironSmith = !player.getAnimations().ironSmith;
				sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().ironSmith ? "enabled" : "disabled")
						+ " the 'Iron-Fist Smithing' Animation Override for you!");
				player.getAppearence().generateAppearenceData();
				break;
			case OPTION_4:
				if (!player.isOwner() && !player.getAnimations().hasChiMining) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
					return;
				}
				player.getAnimations().chiMining = !player.getAnimations().chiMining;
				sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().chiMining ? "enabled" : "disabled")
						+ " the 'Chi-Blast Mining' Animation Override for you!");
				player.getAppearence().generateAppearenceData();
				break;
			case OPTION_5:
				sendOptionsDialogue("Select an Override to use",
						(player.getAnimations().hasSamuraiCook
								? Colors.GREEN + "Samurai Cooking - unlocked - "
										+ (player.getAnimations().samuraiCook ? "enabled" : Colors.RED + "disabled")
								: Colors.RED + "Samurai Cooking - LOCKED"),
						(player.getAnimations().hasRoundHouseWc
								? Colors.GREEN + "Roundhouse Woodcutting - unlocked - "
										+ (player.getAnimations().roundHouseWc ? "enabled" : Colors.RED + "disabled")
								: Colors.RED + "Roundhouse Woodcutting - LOCKED"),
						(player.getAnimations().hasBlastMining
								? Colors.GREEN + "Blast Mining - unlocked - "
										+ (player.getAnimations().blastMining ? "enabled" : Colors.RED + "disabled")
								: Colors.RED + "Blast Mining - LOCKED"),
						(player.getAnimations().hasStrongResting
								? Colors.GREEN + "Strongarm Resting - unlocked - "
										+ (player.getAnimations().strongResting ? "enabled" : Colors.RED + "disabled")
								: Colors.RED + "Strongarm Resting - LOCKED"),
						"More options..");
				stage = 16;
				break;
			}
			break;

		case 16:
			stage = 91;
			switch (componentId) {
			case OPTION_1:
				if (!player.isOwner() && !player.getAnimations().hasSamuraiCook) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
					return;
				}
				player.getAnimations().samuraiCook = !player.getAnimations().samuraiCook;
				sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().samuraiCook ? "enabled" : "disabled")
						+ " the 'Samurai Cooking' Animation Override for you!");
				player.getAppearence().generateAppearenceData();
				break;
			case OPTION_2:
				if (!player.isOwner() && !player.getAnimations().hasRoundHouseWc) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
					return;
				}
				player.getAnimations().roundHouseWc = !player.getAnimations().roundHouseWc;
				sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().roundHouseWc ? "enabled" : "disabled")
						+ " the 'Roundhouse Woodcutting' Animation Override for you!");
				player.getAppearence().generateAppearenceData();
				break;
			case OPTION_3:
				if (!player.isOwner() && !player.getAnimations().hasBlastMining) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
					return;
				}
				player.getAnimations().blastMining = !player.getAnimations().blastMining;
				sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().blastMining ? "enabled" : "disabled")
						+ " the 'Blast Mining' Animation Override for you!");
				player.getAppearence().generateAppearenceData();
				break;
			case OPTION_4:
				if (!player.isOwner() && !player.getAnimations().hasStrongResting) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
					return;
				}
				player.getAnimations().strongResting = !player.getAnimations().strongResting;
				sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().strongResting ? "enabled" : "disabled")
						+ " the 'Strongarm Resting' Animation Override for you!");
				player.getAppearence().generateAppearenceData();
				break;
			case OPTION_5:
				sendOptionsDialogue("Select an Override to use",
						(player.getAnimations().hasArcaneSmelt
								? Colors.GREEN + "Arcane Smelting - unlocked - "
										+ (player.getAnimations().arcaneSmelt ? "enabled" : Colors.RED + "disabled")
								: Colors.RED + "Arcane Smelting - LOCKED"),
						(player.getAnimations().hasArcaneResting
								? Colors.GREEN + "Arcane Resting - unlocked - "
										+ (player.getAnimations().arcaneResting ? "enabled" : Colors.RED + "disabled")
								: Colors.RED + "Arcane Resting - LOCKED"),
						(player.getAnimations().hasStrongWc
								? Colors.GREEN + "Strongarm Woodcutting - unlocked - "
										+ (player.getAnimations().strongWc ? "enabled" : Colors.RED + "disabled")
								: Colors.RED + "Strongarm Woodcutting - LOCKED"),
						(player.getAnimations().hasStrongMining
								? Colors.GREEN + "Strongarm Mining - unlocked - "
										+ (player.getAnimations().strongMining ? "enabled" : Colors.RED + "disabled")
								: Colors.RED + "Strongarm Mining - LOCKED"),
						"More options..");
				stage = 17;
				break;
			}
			break;

		case 17:
			stage = 92;
			switch (componentId) {
			case OPTION_1:
				if (!player.isOwner() && !player.getAnimations().hasArcaneSmelt) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
					return;
				}
				player.getAnimations().arcaneSmelt = !player.getAnimations().arcaneSmelt;
				sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().arcaneSmelt ? "enabled" : "disabled")
						+ " the 'Arcane Smelting' Animation Override for you!");
				player.getAppearence().generateAppearenceData();
				break;
			case OPTION_2:
				if (!player.isOwner() && !player.getAnimations().hasArcaneResting) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
					return;
				}
				player.getAnimations().arcaneResting = !player.getAnimations().arcaneResting;
				sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().arcaneResting ? "enabled" : "disabled")
						+ " the 'Arcane Resting' Animation Override for you!");
				player.getAppearence().generateAppearenceData();
				break;
			case OPTION_3:
				if (!player.isOwner() && !player.getAnimations().hasStrongWc) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
					return;
				}
				player.getAnimations().strongWc = !player.getAnimations().strongWc;
				sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().strongWc ? "enabled" : "disabled")
						+ " the 'Strongarm Woodcutting' Animation Override for you!");
				player.getAppearence().generateAppearenceData();
				break;
			case OPTION_4:
				if (!player.isOwner() && !player.getAnimations().hasStrongMining) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
					return;
				}
				player.getAnimations().strongMining = !player.getAnimations().strongMining;
				sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().strongMining ? "enabled" : "disabled")
						+ " the 'Strongarm Mining' Animation Override for you!");
				player.getAppearence().generateAppearenceData();
				break;
			case OPTION_5:
				sendOptionsDialogue("Select an Override to use",
						(player.getAnimations().hasArcaneFishing
								? Colors.GREEN + "Arcane Fishing - unlocked - "
										+ (player.getAnimations().arcaneFishing ? "enabled" : Colors.RED + "disabled")
								: Colors.RED + "Arcane Fishing - LOCKED"),
						(player.getAnimations().hasStrongBurial
								? Colors.GREEN + "Strongarm Burial - unlocked - "
										+ (player.getAnimations().strongBurial ? "enabled" : Colors.RED + "disabled")
								: Colors.RED + "Strongarm Burial - LOCKED"),
						(player.getAnimations().hasArcaneCook
								? Colors.GREEN + "Arcane Cooking - unlocked - "
										+ (player.getAnimations().arcaneCook ? "enabled" : Colors.RED + "disabled")
								: Colors.RED + "Arcane Cooking - LOCKED"),
						(player.getAnimations().hasPowerDivination
								? Colors.GREEN + "Powerful Divination - unlocked - "
										+ (player.getAnimations().powerDivination ? "enabled" : Colors.RED + "disabled")
								: Colors.RED + "Powerful Divination - LOCKED"),
						"More options..");
				stage = 18;
				break;
			}
			break;

		case 18:
			stage = 93;
			switch (componentId) {
			case OPTION_1:
				if (!player.isOwner() && !player.getAnimations().hasArcaneFishing) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
					return;
				}
				player.getAnimations().arcaneFishing = !player.getAnimations().arcaneFishing;
				sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().arcaneFishing ? "enabled" : "disabled")
						+ " the 'Arcane Fishing' Animation Override for you!");
				player.getAppearence().generateAppearenceData();
				break;
			case OPTION_2:
				if (!player.isOwner() && !player.getAnimations().hasStrongBurial) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
					return;
				}
				player.getAnimations().strongBurial = !player.getAnimations().strongBurial;
				sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().strongBurial ? "enabled" : "disabled")
						+ " the 'Strongarm Burial' Animation Override for you!");
				player.getAppearence().generateAppearenceData();
				break;
			case OPTION_3:
				if (!player.isOwner() && !player.getAnimations().hasArcaneCook) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
					return;
				}
				player.getAnimations().arcaneCook = !player.getAnimations().arcaneCook;
				sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().arcaneCook ? "enabled" : "disabled")
						+ " the 'Arcane Cooking' Animation Override for you!");
				player.getAppearence().generateAppearenceData();
				break;
			case OPTION_4:
				if (!player.isOwner() && !player.getAnimations().hasPowerDivination) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
					return;
				}
				player.getAnimations().powerDivination = !player.getAnimations().powerDivination;
				sendNPCDialogue(npcId, NORMAL,
						"I've " + (player.getAnimations().powerDivination ? "enabled" : "disabled")
								+ " the 'Powerful Divination' Animation Override for you!");
				player.getAppearence().generateAppearenceData();
				break;
			case OPTION_5:
				sendOptionsDialogue("Select an Override to use",
						(player.getAnimations().hasPowerConversion
								? Colors.GREEN + "Powerful Conversion - unlocked - "
										+ (player.getAnimations().powerConversion ? "enabled" : Colors.RED + "disabled")
								: Colors.RED + "Powerful Conversion - LOCKED"),
						(player.getAnimations().hasAgileDivination
								? Colors.GREEN + "Agile Divination - unlocked - "
										+ (player.getAnimations().agileDivination ? "enabled" : Colors.RED + "disabled")
								: Colors.RED + "Agile Divination - LOCKED"),
						(player.getAnimations().hasAgileConversion
								? Colors.GREEN + "Agile Conversion - unlocked - "
										+ (player.getAnimations().agileConversion ? "enabled" : Colors.RED + "disabled")
								: Colors.RED + "Agile Conversion - LOCKED"),
						(player.getAnimations().hasSinisterSlumber
								? Colors.GREEN + "Sinister Slumber - unlocked - "
										+ (player.getAnimations().sinisterSlumber ? "enabled" : Colors.RED + "disabled")
								: Colors.RED + "Sinister Slumber - LOCKED"),
						"More options..");
				stage = 19;
				break;
			}
			break;

		case 19:
			stage = 94;
			switch (componentId) {
			case OPTION_1:
				if (!player.isOwner() && !player.getAnimations().hasPowerConversion) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
					return;
				}
				player.getAnimations().powerConversion = !player.getAnimations().powerConversion;
				sendNPCDialogue(npcId, NORMAL,
						"I've " + (player.getAnimations().powerConversion ? "enabled" : "disabled")
								+ " the 'Powerful Conversion' Animation Override for you!");
				player.getAppearence().generateAppearenceData();
				break;
			case OPTION_2:
				if (!player.isOwner() && !player.getAnimations().hasAgileDivination) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
					return;
				}
				player.getAnimations().agileDivination = !player.getAnimations().agileDivination;
				sendNPCDialogue(npcId, NORMAL,
						"I've " + (player.getAnimations().agileDivination ? "enabled" : "disabled")
								+ " the 'Agile Divination' Animation Override for you!");
				player.getAppearence().generateAppearenceData();
				break;
			case OPTION_3:
				if (!player.isOwner() && !player.getAnimations().hasAgileConversion) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
					return;
				}
				player.getAnimations().agileConversion = !player.getAnimations().agileConversion;
				sendNPCDialogue(npcId, NORMAL,
						"I've " + (player.getAnimations().agileConversion ? "enabled" : "disabled")
								+ " the 'Agile Conversion' Animation Override for you!");
				player.getAppearence().generateAppearenceData();
				break;
			case OPTION_4:
				if (!player.isOwner() && !player.getAnimations().hasSinisterSlumber) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
					return;
				}
				player.getAnimations().sinisterSlumber = !player.getAnimations().sinisterSlumber;
				sendNPCDialogue(npcId, NORMAL,
						"I've " + (player.getAnimations().sinisterSlumber ? "enabled" : "disabled")
								+ " the 'Sinister Slumber' Animation Override for you!");
				player.getAppearence().generateAppearenceData();
				break;
			case OPTION_5:
				sendOptionsDialogue("Select an Override to use",
						(player.getAnimations().hasArmWarrior
								? Colors.GREEN + "Armchair Warrior - unlocked - "
										+ (player.getAnimations().armWarrior ? "enabled" : Colors.RED + "disabled")
								: Colors.RED + "Armchair Warrior - LOCKED"),
						(player.getAnimations().hasEneResting
								? Colors.GREEN + "Energy Drain Resting - unlocked - "
										+ (player.getAnimations().eneResting ? "enabled" : Colors.RED + "disabled")
								: Colors.RED + "Energy Drain Resting - LOCKED"),
						(player.getAnimations().hasCrystalResting
								? Colors.GREEN + "Crystal Impling Resting - unlocked - "
										+ (player.getAnimations().crystalResting ? "enabled" : Colors.RED + "disabled")
								: Colors.RED + "Crystal Impling Resting - LOCKED"),
						(player.getAnimations().hasHeadMining
								? Colors.GREEN + "Headbutt Mining - unlocked - "
										+ (player.getAnimations().headMining ? "enabled" : Colors.RED + "disabled")
								: Colors.RED + "Headbutt Mining - LOCKED"),
						"More options..");
				stage = 20;
				break;
			}
			break;

		case 20:
			stage = 95;
			switch (componentId) {
			case OPTION_1:
				if (!player.isOwner() && !player.getAnimations().hasArmWarrior) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
					return;
				}
				player.getAnimations().armWarrior = !player.getAnimations().armWarrior;
				sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().armWarrior ? "enabled" : "disabled")
						+ " the 'Armchair Warrior' Animation Override for you!");
				player.getAppearence().generateAppearenceData();
				break;
			case OPTION_2:
				if (!player.isOwner() && !player.getAnimations().hasEneResting) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
					return;
				}
				player.getAnimations().eneResting = !player.getAnimations().eneResting;
				sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().eneResting ? "enabled" : "disabled")
						+ " the 'Energy Drain Resting' Animation Override for you!");
				player.getAppearence().generateAppearenceData();
				break;
			case OPTION_3:
				if (!player.isOwner() && !player.getAnimations().hasCrystalResting) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
					return;
				}
				player.getAnimations().crystalResting = !player.getAnimations().crystalResting;
				sendNPCDialogue(npcId, NORMAL,
						"I've " + (player.getAnimations().crystalResting ? "enabled" : "disabled")
								+ " the 'Crystal Impling Resting' Animation Override for you!");
				player.getAppearence().generateAppearenceData();
				break;
			case OPTION_4:
				if (!player.isOwner() && !player.getAnimations().hasHeadMining) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
					return;
				}
				player.getAnimations().headMining = !player.getAnimations().headMining;
				sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().headMining ? "enabled" : "disabled")
						+ " the 'Headbutt Mining' Animation Override for you!");
				player.getAppearence().generateAppearenceData();
				break;
			case OPTION_5:
				sendOptionsDialogue("Select an Override to use",
						(player.getAnimations().hasSandWalk
								? Colors.GREEN + "Sandstorm Walk - unlocked - "
										+ (player.getAnimations().sandWalk ? "enabled" : Colors.RED + "disabled")
								: Colors.RED + "Sandstorm Walk - LOCKED"),
						(player.getAnimations().hasSadWalk
								? Colors.GREEN + "Sad Walk - unlocked - "
										+ (player.getAnimations().sadWalk ? "enabled" : Colors.RED + "disabled")
								: Colors.RED + "Sad Walk - LOCKED"),
						(player.getAnimations().hasAngryWalk
								? Colors.GREEN + "Angry Walk - unlocked - "
										+ (player.getAnimations().angryWalk ? "enabled" : Colors.RED + "disabled")
								: Colors.RED + "Angry Walk - LOCKED"),
						(player.getAnimations().hasProudWalk
								? Colors.GREEN + "Proud Walk - unlocked - "
										+ (player.getAnimations().proudWalk ? "enabled" : Colors.RED + "disabled")
								: Colors.RED + "Proud Walk - LOCKED"),
						"More options..");
				stage = 21;
				break;
			}
			break;

		case 21:
			stage = 96;
			switch (componentId) {
			case OPTION_1:
				if (!player.isOwner() && !player.getAnimations().hasSandWalk) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
					return;
				}
				player.getAnimations().sandWalk = !player.getAnimations().sandWalk;
				sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().sandWalk ? "enabled" : "disabled")
						+ " the 'Sandstorm Walk' Animation Override for you!");
				player.getAppearence().generateAppearenceData();
				break;
			case OPTION_2:
				if (!player.isOwner() && !player.getAnimations().hasSadWalk) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
					return;
				}
				player.getAnimations().sadWalk = !player.getAnimations().sadWalk;
				sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().sadWalk ? "enabled" : "disabled")
						+ " the 'Sad Walk' Animation Override for you!");
				player.getAppearence().generateAppearenceData();
				break;
			case OPTION_3:
				if (!player.isOwner() && !player.getAnimations().hasAngryWalk) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
					return;
				}
				player.getAnimations().angryWalk = !player.getAnimations().angryWalk;
				sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().angryWalk ? "enabled" : "disabled")
						+ " the 'Angry Walk' Animation Override for you!");
				player.getAppearence().generateAppearenceData();
				break;
			case OPTION_4:
				if (!player.isOwner() && !player.getAnimations().hasProudWalk) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
					return;
				}
				player.getAnimations().proudWalk = !player.getAnimations().proudWalk;
				sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().proudWalk ? "enabled" : "disabled")
						+ " the 'Proud Walk' Animation Override for you!");
				player.getAppearence().generateAppearenceData();
				break;
			case OPTION_5:
				sendOptionsDialogue("Select an Override to use",
						(player.getAnimations().hasHappyWalk
								? Colors.GREEN + "Happy Walk - unlocked - "
										+ (player.getAnimations().happyWalk ? "enabled" : Colors.RED + "disabled")
								: Colors.RED + "Happy Walk - LOCKED"),
						(player.getAnimations().hasBarbarianWalk
								? Colors.GREEN + "Barbarian Walk - unlocked - "
										+ (player.getAnimations().barbarianWalk ? "enabled" : Colors.RED + "disabled")
								: Colors.RED + "Barbarian Walk - LOCKED"),
						(player.getAnimations().hasRevenantWalk
								? Colors.GREEN + "Revenant Walk - unlocked - "
										+ (player.getAnimations().revenantWalk ? "enabled" : Colors.RED + "disabled")
								: Colors.RED + "Revenant Walk - LOCKED"),
						(player.getAnimations().hasSingerMining
								? Colors.GREEN + "Crystal Singer Mining - unlocked - "
										+ (player.getAnimations().singerMining ? " enabled" : Colors.RED + "disabled")
								: Colors.RED + "Crystal Singer Mining - LOCKED"),
						"More options..");
				stage = 22;
				break;
			}
			break;
		case 22:
			stage = 97;
			switch (componentId) {
			case OPTION_1:
				if (!player.isOwner() && !player.getAnimations().hasHappyWalk) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
					return;
				}
				player.getAnimations().happyWalk = !player.getAnimations().happyWalk;
				sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().happyWalk ? "enabled" : "disabled")
						+ " the 'Happy Walk' Animation Override for you!");
				player.getAppearence().generateAppearenceData();
				break;
			case OPTION_2:
				if (!player.isOwner() && !player.getAnimations().hasBarbarianWalk) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
					return;
				}
				player.getAnimations().barbarianWalk = !player.getAnimations().barbarianWalk;
				sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().barbarianWalk ? "enabled" : "disabled")
						+ " the 'Barbarian Walk' Animation Override for you!");
				player.getAppearence().generateAppearenceData();
				break;
			case OPTION_3:
				if (!player.isOwner() && !player.getAnimations().hasRevenantWalk) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
					return;
				}
				player.getAnimations().revenantWalk = !player.getAnimations().revenantWalk;
				sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().revenantWalk ? "enabled" : "disabled")
						+ " the 'Revenant Walk' Animation Override for you!");
				player.getAppearence().generateAppearenceData();
				break;
			case OPTION_4:
				if (!player.isOwner() && !player.getAnimations().hasSingerMining) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
					return;
				}
				player.getAnimations().singerMining = !player.getAnimations().singerMining;
				sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().singerMining ? "enabled" : "disabled")
						+ " the 'Crystal Singer Mining' Animation Override for you!");
				break;
			case OPTION_5:
				sendOptionsDialogue("Select an Override to use",
						(player.getAnimations().hasExplosiveWc
								? Colors.GREEN + "Explosive Woodcutting - unlocked - "
										+ (player.getAnimations().explosiveWc ? "enabled" : Colors.RED + "disabled")
								: Colors.RED + "Explosive Woodcutting - LOCKED"),
						(player.getAnimations().hasSingerWc
								? Colors.GREEN + "Crystal Singer Woodcutting - unlocked - "
										+ (player.getAnimations().singerWc ? "enabled" : Colors.RED + "disabled")
								: Colors.RED + "Crystal Singer Woodcutting - LOCKED"),
						(player.getAnimations().hasBombFiremaking
								? Colors.GREEN + "Bomb Firemaking - unlocked - "
										+ (player.getAnimations().bombFiremaking ? "enabled" : Colors.RED + "disabled")
								: Colors.RED + "Bomb Firemaking - LOCKED"),
						(player.getAnimations().hasPartyhatFiremaking
								? Colors.GREEN + "Partyhat Firemaking - unlocked - "
										+ (player.getAnimations().partyhatFiremaking ? " enabled" : Colors.RED + "disabled")
								: Colors.RED + "Partyhat Firemaking - LOCKED"),
						"More options..");
				//stage = 14;
				stage = 23;
				break;
			}
			break;
			
		case 23:
			stage = 98;
			switch (componentId) {
			case OPTION_1:
				if (!player.isOwner() && !player.getAnimations().hasExplosiveWc) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
					return;
				}
				player.getAnimations().explosiveWc = !player.getAnimations().explosiveWc;
				sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().explosiveWc ? "enabled" : "disabled")
						+ " the 'Explosive Woodcutting' Animation Override for you!");
				break;
			case OPTION_2:
				if (!player.isOwner() && !player.getAnimations().hasSingerWc) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
					return;
				}
				player.getAnimations().singerWc = !player.getAnimations().singerWc;
				sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().singerWc ? "enabled" : "disabled")
						+ " the 'Crystal Singer Woodcutting' Animation Override for you!");
				break;
			case OPTION_3:
				if (!player.isOwner() && !player.getAnimations().hasBombFiremaking) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
					return;
				}
				player.getAnimations().bombFiremaking = !player.getAnimations().bombFiremaking;
				sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().bombFiremaking ? "enabled" : "disabled")
						+ " the 'Bomb Firemaking' Animation Override for you!");
				break;
			case OPTION_4:
				if (!player.isOwner() && !player.getAnimations().hasPartyhatFiremaking) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
					return;
				}
				player.getAnimations().partyhatFiremaking = !player.getAnimations().partyhatFiremaking;
				sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().partyhatFiremaking ? "enabled" : "disabled")
						+ " the 'Partyhat Firemaking' Animation Override for you!");
				break;
			case OPTION_5:
				sendOptionsDialogue("Select an Override to use",
						(player.getAnimations().hasAncestralDivination1
								? Colors.GREEN + "Ancestral Divination I - unlocked - "
										+ (player.getAnimations().ancestralDivination1 ? "enabled" : Colors.RED + "disabled")
								: Colors.RED + "Ancestral Divination I - LOCKED"),
						(player.getAnimations().hasAncestralDivination2
								? Colors.GREEN + "Ancestral Divination II - unlocked - "
										+ (player.getAnimations().ancestralDivination2 ? "enabled" : Colors.RED + "disabled")
								: Colors.RED + "Ancestral Divination II - LOCKED"),
						(player.getAnimations().hasAncestralConversion
								? Colors.GREEN + "Ancestral Conversion - unlocked - "
										+ (player.getAnimations().ancestralConversion ? "enabled" : Colors.RED + "disabled")
								: Colors.RED + "Ancestral Conversion - LOCKED"),
						(player.getAnimations().hasBlinkWalk
								? Colors.GREEN + "Blink Walk - unlocked - "
										+ (player.getAnimations().blinkWalk ? "enabled" : Colors.RED + "disabled")
								: Colors.RED + "Blink Walk - LOCKED"),
						"More options..");
				stage = 24;
				break;
			}
			break;
			
		case 24:
			stage = 99;
			switch (componentId) {
			case OPTION_1:
				if (!player.isOwner() && !player.getAnimations().hasAncestralDivination1) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
					return;
				}
				player.getAnimations().ancestralDivination1 = !player.getAnimations().ancestralDivination1;
				sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().ancestralDivination1 ? "enabled" : "disabled")
						+ " the 'Ancestral Divination I' Animation Override for you!");
				break;
			case OPTION_2:
				if (!player.isOwner() && !player.getAnimations().hasAncestralDivination2) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
					return;
				}
				player.getAnimations().ancestralDivination2 = !player.getAnimations().ancestralDivination2;
				sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().ancestralDivination2 ? "enabled" : "disabled")
						+ " the 'Ancestral Divination II' Animation Override for you!");
				break;
			case OPTION_3:
				if (!player.isOwner() && !player.getAnimations().hasAncestralConversion) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
					return;
				}
				player.getAnimations().ancestralConversion = !player.getAnimations().ancestralConversion;
				sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().ancestralConversion ? "enabled" : "disabled")
						+ " the 'Ancestral Conversion' Animation Override for you!");
				break;
			case OPTION_4:
				if (!player.isOwner() && !player.getAnimations().hasBlinkWalk) {
					sendNPCDialogue(npcId, SAD,
							"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
					return;
				}
				player.getAnimations().blinkWalk = !player.getAnimations().blinkWalk;
				sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().blinkWalk ? "enabled" : "disabled")
						+ " the 'Blink Walk' Animation Override for you!");
				break;
			case OPTION_5:
				sendOptionsDialogue("Select an Override to use",
						(player.getAnimations().hasCatWalk
								? Colors.GREEN + "Cat walk - unlocked - "
								+ (player.getAnimations().catWalk ? "enabled" : Colors.RED + "disabled")
								: Colors.RED + "Cat walk - LOCKED"),
						(player.getAnimations().hasZombieWalk
								? Colors.GREEN + "Zombie walk - unlocked - "
								+ (player.getAnimations().zombieWalk ? "enabled" : Colors.RED + "disabled")
								: Colors.RED + "Zombie walk - LOCKED"),
						(player.getAnimations().hasCongaWalk
								? Colors.GREEN + "Conga walk - unlocked - "
								+ (player.getAnimations().congaWalk ? "enabled" : Colors.RED + "disabled")
								: Colors.RED + "Conga walk - LOCKED"),
						(player.getAnimations().hasLovedUpWalk
								? Colors.GREEN + "Loved up walk - unlocked - "
								+ (player.getAnimations().lovedUpWalk ? "enabled" : Colors.RED + "disabled")
								: Colors.RED + "Loved up walk - LOCKED"),
						"More options..");
				stage = 25;
				break;
			}
			break;

			case 25:
				stage = 100;
				switch (componentId) {
					case OPTION_1:
						if (!player.isOwner() && !player.getAnimations().hasCatWalk) {
							sendNPCDialogue(npcId, SAD,
									"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
							return;
						}
						player.getAnimations().catWalk = !player.getAnimations().catWalk;
						sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().catWalk ? "enabled" : "disabled")
								+ " the 'CatWalk' Animation Override for you!");
						break;
					case OPTION_2:
						if (!player.isOwner() && !player.getAnimations().hasZombieWalk) {
							sendNPCDialogue(npcId, SAD,
									"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
							return;
						}
						player.getAnimations().zombieWalk = !player.getAnimations().zombieWalk;
						sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().zombieWalk ? "enabled" : "disabled")
								+ " the 'Zombie walk' Animation Override for you!");
						break;
					case OPTION_3:
						if (!player.isOwner() && !player.getAnimations().hasCongaWalk) {
							sendNPCDialogue(npcId, SAD,
									"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
							return;
						}
						player.getAnimations().congaWalk = !player.getAnimations().congaWalk;
						sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().congaWalk ? "enabled" : "disabled")
								+ " the 'Conga walk' Animation Override for you!");
						break;
					case OPTION_4:
						if (!player.isOwner() && !player.getAnimations().hasLovedUpWalk) {
							sendNPCDialogue(npcId, SAD,
									"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
							return;
						}
						player.getAnimations().lovedUpWalk = !player.getAnimations().lovedUpWalk;
						sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().lovedUpWalk ? "enabled" : "disabled")
								+ " the 'Loved up walk' Animation Override for you!");
						break;
					case OPTION_5:
						sendOptionsDialogue("Select an Override to use",
								(player.getAnimations().hasSlowmoWalk
										? Colors.GREEN + "Slowmo walk - unlocked - "
										+ (player.getAnimations().slowmoWalk ? "enabled" : Colors.RED + "disabled")
										: Colors.RED + "Slowmo walk - LOCKED"),
								(player.getAnimations().hasPartyFeverWalk
										? Colors.GREEN + "Party fever walk - unlocked - "
										+ (player.getAnimations().partyFeverWalk ? "enabled" : Colors.RED + "disabled")
										: Colors.RED + "Party fever walk - LOCKED"),
								(player.getAnimations().hasAssassinWalk
										? Colors.GREEN + "Assassin walk - unlocked - "
										+ (player.getAnimations().assassinWalk ? "enabled" : Colors.RED + "disabled")
										: Colors.RED + "Assassin walk - LOCKED"),
								(player.getAnimations().hasTwistedJesterWalk
										? Colors.GREEN + "Twisted jester walk - unlocked - "
										+ (player.getAnimations().twistedJesterWalk ? "enabled" : Colors.RED + "disabled")
										: Colors.RED + "Twisted jester walk - LOCKED"),
								"More options..");
						stage = 26;
						break;
				}
				break;

			case 26:
				switch (componentId) {
					case OPTION_1:
						if (!player.isOwner() && !player.getAnimations().hasSlowmoWalk) {
							sendNPCDialogue(npcId, SAD,
									"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
							return;
						}
						player.getAnimations().slowmoWalk = !player.getAnimations().slowmoWalk;
						sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().slowmoWalk ? "enabled" : "disabled")
								+ " the 'Slowmo walk' Animation Override for you!");
						break;
					case OPTION_2:
						if (!player.isOwner() && !player.getAnimations().hasPartyFeverWalk) {
							sendNPCDialogue(npcId, SAD,
									"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
							return;
						}
						player.getAnimations().partyFeverWalk = !player.getAnimations().partyFeverWalk;
						sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().partyFeverWalk ? "enabled" : "disabled")
								+ " the 'Party fever walk' Animation Override for you!");
						break;
					case OPTION_3:
						if (!player.isOwner() && !player.getAnimations().hasAssassinWalk) {
							sendNPCDialogue(npcId, SAD,
									"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
							return;
						}
						player.getAnimations().assassinWalk = !player.getAnimations().assassinWalk;
						sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().assassinWalk ? "enabled" : "disabled")
								+ " the 'Assassin walk' Animation Override for you!");
						break;
					case OPTION_4:
						if (!player.isOwner() && !player.getAnimations().hasTwistedJesterWalk) {
							sendNPCDialogue(npcId, SAD,
									"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
							return;
						}
						player.getAnimations().twistedJesterWalk = !player.getAnimations().twistedJesterWalk;
						sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().twistedJesterWalk ? "enabled" : "disabled")
								+ " the 'Twisted jester walk' Animation Override for you!");
						break;
					case OPTION_5:
						sendOptionsDialogue("Select an Override to use",
								(player.getAnimations().hasHotSandWalk
										? Colors.GREEN + "Hot sand walk - unlocked - "
										+ (player.getAnimations().hotSandWalk ? "enabled" : Colors.RED + "disabled")
										: Colors.RED + "Hot sand walk - LOCKED"),
								(player.getAnimations().hasPlagueDoctorWalk
										? Colors.GREEN + "Plague doctor walk - unlocked - "
										+ (player.getAnimations().plagueDoctorWalk ? "enabled" : Colors.RED + "disabled")
										: Colors.RED + "Plague doctor walk - LOCKED"),
								"More options..");
						stage = 27;
						break;
				}
				break;

			case 27:
				switch (componentId) {
					case OPTION_1:
						if (!player.isOwner() && !player.getAnimations().hasHotSandWalk) {
							sendNPCDialogue(npcId, SAD,
									"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
							return;
						}
						player.getAnimations().hotSandWalk = !player.getAnimations().hotSandWalk;
						sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().hotSandWalk ? "enabled" : "disabled")
								+ " the 'Hot sand walk' Animation Override for you!");
						break;
					case OPTION_2:
						if (!player.isOwner() && !player.getAnimations().hasPlagueDoctorWalk) {
							sendNPCDialogue(npcId, SAD,
									"I'm sorry, but you will have to purchase this item first " + "in order to unlock it.");
							return;
						}
						player.getAnimations().plagueDoctorWalk = !player.getAnimations().plagueDoctorWalk;
						sendNPCDialogue(npcId, NORMAL, "I've " + (player.getAnimations().plagueDoctorWalk ? "enabled" : "disabled")
								+ " the 'Plague doctor walk' Animation Override for you!");
						break;
					case OPTION_3:
						sendOptionsDialogue("Select an Override to use",
								(player.getAnimations().hasBattleCry
										? Colors.GREEN + "Slayer Battle Cry - unlocked - "
										+ (player.getAnimations().battleCry ? "enabled" : Colors.RED + "disabled")
										: Colors.RED + "Slayer Battle Cry - LOCKED"),
								(player.getAnimations().hasEnhancedPotion
										? Colors.GREEN + "Enhanced Potion Making - unlocked - "
										+ (player.getAnimations().enhancedPotion ? "enabled" : Colors.RED + "disabled")
										: Colors.RED + "Enhanced Potion Making - LOCKED"),
								(player.getAnimations().hasLumberjackWc
										? Colors.GREEN + "Lumberjack Woodcutting - unlocked - "
										+ (player.getAnimations().lumberjackWc ? "enabled" : Colors.RED + "disabled")
										: Colors.RED + "Lumberjack Woodcutting - LOCKED"),
								(player.getAnimations().hasDeepFishing
										? Colors.GREEN + "Deep-Sea Fishing - unlocked - "
										+ (player.getAnimations().deepFishing ? "enabled" : Colors.RED + "disabled")
										: Colors.RED + "Deep-Sea Fishing - LOCKED"),
								"More options..");
						stage = 14;
						break;
				}
				break;

			
		//dialogue options
		case 90:
			sendOptionsDialogue("Select an Override to use",
					(player.getAnimations().hasZenResting
							? Colors.GREEN + "Zen Resting - unlocked - "
									+ (player.getAnimations().zenResting ? "enabled" : Colors.RED + "disabled")
							: Colors.RED + "Zen Resting - LOCKED"),
					(player.getAnimations().hasKarateFletch
							? Colors.GREEN + "Karate-Chop Fletching - unlocked - "
									+ (player.getAnimations().karateFletch ? "enabled" : Colors.RED + "disabled")
							: Colors.RED + "Karate-Chop Fletching - LOCKED"),
					(player.getAnimations().hasIronSmith
							? Colors.GREEN + "Iron-Fist Smithing - unlocked - "
									+ (player.getAnimations().ironSmith ? "enabled" : Colors.RED + "disabled")
							: Colors.RED + "Iron-Fist Smithing - LOCKED"),
					(player.getAnimations().hasChiMining
							? Colors.GREEN + "Chi-Blast Mining - unlocked - "
									+ (player.getAnimations().chiMining ? "enabled" : Colors.RED + "disabled")
							: Colors.RED + "Chi-Blast Mining - LOCKED"),
					"More options..");
			stage = 15;
			break;
		case 91:
			sendOptionsDialogue("Select an Override to use",
					(player.getAnimations().hasSamuraiCook
							? Colors.GREEN + "Samurai Cooking - unlocked - "
									+ (player.getAnimations().samuraiCook ? "enabled" : Colors.RED + "disabled")
							: Colors.RED + "Samurai Cooking - LOCKED"),
					(player.getAnimations().hasRoundHouseWc
							? Colors.GREEN + "Roundhouse Woodcutting - unlocked - "
									+ (player.getAnimations().roundHouseWc ? "enabled" : Colors.RED + "disabled")
							: Colors.RED + "Roundhouse Woodcutting - LOCKED"),
					(player.getAnimations().hasBlastMining
							? Colors.GREEN + "Blast Mining - unlocked - "
									+ (player.getAnimations().blastMining ? "enabled" : Colors.RED + "disabled")
							: Colors.RED + "Blast Mining - LOCKED"),
					(player.getAnimations().hasStrongResting
							? Colors.GREEN + "Strongarm Resting - unlocked - "
									+ (player.getAnimations().strongResting ? "enabled" : Colors.RED + "disabled")
							: Colors.RED + "Strongarm Resting - LOCKED"),
					"More options..");
			stage = 16;
			break;
		case 92:
			sendOptionsDialogue("Select an Override to use",
					(player.getAnimations().hasArcaneSmelt
							? Colors.GREEN + "Arcane Smelting - unlocked - "
									+ (player.getAnimations().arcaneSmelt ? "enabled" : Colors.RED + "disabled")
							: Colors.RED + "Arcane Smelting - LOCKED"),
					(player.getAnimations().hasArcaneResting
							? Colors.GREEN + "Arcane Resting - unlocked - "
									+ (player.getAnimations().arcaneResting ? "enabled" : Colors.RED + "disabled")
							: Colors.RED + "Arcane Resting - LOCKED"),
					(player.getAnimations().hasStrongWc
							? Colors.GREEN + "Strongarm Woodcutting - unlocked - "
									+ (player.getAnimations().strongWc ? "enabled" : Colors.RED + "disabled")
							: Colors.RED + "Strongarm Woodcutting - LOCKED"),
					(player.getAnimations().hasStrongMining
							? Colors.GREEN + "Strongarm Mining - unlocked - "
									+ (player.getAnimations().strongMining ? "enabled" : Colors.RED + "disabled")
							: Colors.RED + "Strongarm Mining - LOCKED"),
					"More options..");
			stage = 17;
			break;
		case 93:
			sendOptionsDialogue("Select an Override to use",
					(player.getAnimations().hasArcaneFishing
							? Colors.GREEN + "Arcane Fishing - unlocked - "
									+ (player.getAnimations().arcaneFishing ? "enabled" : Colors.RED + "disabled")
							: Colors.RED + "Arcane Fishing - LOCKED"),
					(player.getAnimations().hasStrongBurial
							? Colors.GREEN + "Strongarm Burial - unlocked - "
									+ (player.getAnimations().strongBurial ? "enabled" : Colors.RED + "disabled")
							: Colors.RED + "Strongarm Burial - LOCKED"),
					(player.getAnimations().hasArcaneCook
							? Colors.GREEN + "Arcane Cooking - unlocked - "
									+ (player.getAnimations().arcaneCook ? "enabled" : Colors.RED + "disabled")
							: Colors.RED + "Arcane Cooking - LOCKED"),
					(player.getAnimations().hasPowerDivination
							? Colors.GREEN + "Powerful Divination - unlocked - "
									+ (player.getAnimations().powerDivination ? "enabled" : Colors.RED + "disabled")
							: Colors.RED + "Powerful Divination - LOCKED"),
					"More options..");
			stage = 18;
			break;
		case 94:
			sendOptionsDialogue("Select an Override to use",
					(player.getAnimations().hasPowerConversion
							? Colors.GREEN + "Powerful Conversion - unlocked - "
									+ (player.getAnimations().powerConversion ? "enabled" : Colors.RED + "disabled")
							: Colors.RED + "Powerful Conversion - LOCKED"),
					(player.getAnimations().hasAgileDivination
							? Colors.GREEN + "Agile Divination - unlocked - "
									+ (player.getAnimations().agileDivination ? "enabled" : Colors.RED + "disabled")
							: Colors.RED + "Agile Divination - LOCKED"),
					(player.getAnimations().hasAgileConversion
							? Colors.GREEN + "Agile Conversion - unlocked - "
									+ (player.getAnimations().agileConversion ? "enabled" : Colors.RED + "disabled")
							: Colors.RED + "Agile Conversion - LOCKED"),
					(player.getAnimations().hasSinisterSlumber
							? Colors.GREEN + "Sinister Slumber - unlocked - "
									+ (player.getAnimations().sinisterSlumber ? "enabled" : Colors.RED + "disabled")
							: Colors.RED + "Sinister Slumber - LOCKED"),
					"More options..");
			stage = 19;
			break;
		case 95:
			sendOptionsDialogue("Select an Override to use",
					(player.getAnimations().hasArmWarrior
							? Colors.GREEN + "Armchair Warrior - unlocked - "
									+ (player.getAnimations().armWarrior ? "enabled" : Colors.RED + "disabled")
							: Colors.RED + "Armchair Warrior - LOCKED"),
					(player.getAnimations().hasEneResting
							? Colors.GREEN + "Energy Drain Resting - unlocked - "
									+ (player.getAnimations().eneResting ? "enabled" : Colors.RED + "disabled")
							: Colors.RED + "Energy Drain Resting - LOCKED"),
					(player.getAnimations().hasCrystalResting
							? Colors.GREEN + "Crystal Impling Resting - unlocked - "
									+ (player.getAnimations().crystalResting ? "enabled" : Colors.RED + "disabled")
							: Colors.RED + "Crystal Impling Resting - LOCKED"),
					(player.getAnimations().hasHeadMining
							? Colors.GREEN + "Headbutt Mining - unlocked - "
									+ (player.getAnimations().headMining ? "enabled" : Colors.RED + "disabled")
							: Colors.RED + "Headbutt Mining - LOCKED"),
					"More options..");
			stage = 20;
			break;
		case 96:
			sendOptionsDialogue("Select an Override to use",
					(player.getAnimations().hasSandWalk
							? Colors.GREEN + "Sandstorm Walk - unlocked - "
									+ (player.getAnimations().sandWalk ? "enabled" : Colors.RED + "disabled")
							: Colors.RED + "Sandstorm Walk - LOCKED"),
					(player.getAnimations().hasSadWalk
							? Colors.GREEN + "Sad Walk - unlocked - "
									+ (player.getAnimations().sadWalk ? "enabled" : Colors.RED + "disabled")
							: Colors.RED + "Sad Walk - LOCKED"),
					(player.getAnimations().hasAngryWalk
							? Colors.GREEN + "Angry Walk - unlocked - "
									+ (player.getAnimations().angryWalk ? "enabled" : Colors.RED + "disabled")
							: Colors.RED + "Angry Walk - LOCKED"),
					(player.getAnimations().hasProudWalk
							? Colors.GREEN + "Proud Walk - unlocked - "
									+ (player.getAnimations().proudWalk ? "enabled" : Colors.RED + "disabled")
							: Colors.RED + "Proud Walk - LOCKED"),
					"More options..");
			stage = 21;
			break;
		case 97:
			sendOptionsDialogue("Select an Override to use",
					(player.getAnimations().hasHappyWalk
							? Colors.GREEN + "Happy Walk - unlocked - "
									+ (player.getAnimations().happyWalk ? "enabled" : Colors.RED + "disabled")
							: Colors.RED + "Happy Walk - LOCKED"),
					(player.getAnimations().hasBarbarianWalk
							? Colors.GREEN + "Barbarian Walk - unlocked - "
									+ (player.getAnimations().barbarianWalk ? "enabled" : Colors.RED + "disabled")
							: Colors.RED + "Barbarian Walk - LOCKED"),
					(player.getAnimations().hasRevenantWalk
							? Colors.GREEN + "Revenant Walk - unlocked - "
									+ (player.getAnimations().revenantWalk ? "enabled" : Colors.RED + "disabled")
							: Colors.RED + "Revenant Walk - LOCKED"),
					(player.getAnimations().hasSingerMining
							? Colors.GREEN + "Crystal Singer Mining - unlocked - "
									+ (player.getAnimations().singerMining ? " enabled" : Colors.RED + "disabled")
							: Colors.RED + "Crystal Singer Mining - LOCKED"),
					"More options..");
			stage = 22;
			break;
			
		case 98:
			sendOptionsDialogue("Select an Override to use",
					(player.getAnimations().hasExplosiveWc
							? Colors.GREEN + "Explosive Woodcutting - unlocked - "
									+ (player.getAnimations().explosiveWc ? "enabled" : Colors.RED + "disabled")
							: Colors.RED + "Explosive Woodcutting - LOCKED"),
					(player.getAnimations().hasSingerWc
							? Colors.GREEN + "Crystal Singer Woodcutting - unlocked - "
									+ (player.getAnimations().singerWc ? "enabled" : Colors.RED + "disabled")
							: Colors.RED + "Crystal Singer Woodcutting - LOCKED"),
					(player.getAnimations().hasBombFiremaking
							? Colors.GREEN + "Bomb Firemaking - unlocked - "
									+ (player.getAnimations().bombFiremaking ? "enabled" : Colors.RED + "disabled")
							: Colors.RED + "Bomb Firemaking - LOCKED"),
					(player.getAnimations().hasPartyhatFiremaking
							? Colors.GREEN + "Partyhat Firemaking - unlocked - "
									+ (player.getAnimations().partyhatFiremaking ? " enabled" : Colors.RED + "disabled")
							: Colors.RED + "Partyhat Firemaking - LOCKED"),
					"More options..");
			stage = 23;
			break;
		
		case 99:
			sendOptionsDialogue("Select an Override to use",
					(player.getAnimations().hasAncestralDivination1
							? Colors.GREEN + "Ancestral Divination I - unlocked - "
									+ (player.getAnimations().ancestralDivination1 ? "enabled" : Colors.RED + "disabled")
							: Colors.RED + "Ancestral Divination I - LOCKED"),
					(player.getAnimations().hasAncestralDivination2
							? Colors.GREEN + "Ancestral Divination II - unlocked - "
									+ (player.getAnimations().ancestralDivination2 ? "enabled" : Colors.RED + "disabled")
							: Colors.RED + "Ancestral Divination II - LOCKED"),
					(player.getAnimations().hasAncestralConversion
							? Colors.GREEN + "Ancestral Conversion - unlocked - "
									+ (player.getAnimations().ancestralConversion ? "enabled" : Colors.RED + "disabled")
							: Colors.RED + "Ancestral Conversion - LOCKED"),
					(player.getAnimations().hasBlinkWalk
							? Colors.GREEN + "Blink walk - unlocked - "
									+ (player.getAnimations().blinkWalk ? "enabled" : Colors.RED + "disabled")
							: Colors.RED + "Blink walk - LOCKED"),
					"More options..");
			stage = 24;
			break;

		case 127:
			end();
			break;
		}
	}

	@Override
	public void finish() {
	}

}