package com.rs.game.player.dialogue.impl;

import com.rs.game.player.content.achievementsystem.AchievementList;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

/**
 * Handles The Partner's (John Strum) dialogue.
 */
public class JohnStrumD extends Dialogue {

	// The NPC ID.
	int npcId;

	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		if (player.getPorts().firstTimer) {
			player.getHintIconsManager().removeUnsavedHintIcon();
			Dialogue.closeNoContinueDialogue(player);
			sendNPCDialogue(npcId, NORMAL, "Welcome to your new port, adventurer. My name is John Strum.");
		} else {
			sendNPCDialogue(npcId, NORMAL, "Hello there!");
			stage = 11;
		}
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			sendPlayerDialogue(GLANCE_DOWN, "My port..?");
			stage = 0;
			break;
		case 0:
			sendNPCDialogue(npcId, NORMAL, "That's right, this is your port now.");
			stage = 1;
			break;
		case 1:
			sendNPCDialogue(npcId, CALM, "Now hear me out.. as an ex-port master I will be your manager. "
					+ "I will help with managing your ships, voyages, resources and crew members.");
			stage = 2;
			break;
		case 2:
			sendNPCDialogue(npcId, CALM, "This is the noticeboard at which you will be able to see all of your "
					+ "gathered resources as well as your ship and voyage progress.");
			stage = 3;
			break;
		case 3:
			sendNPCDialogue(npcId, CALM,
					"There are 3 types of resources you will be able to get "
							+ "from successful voyages: Plate; Chi Globe; Lacquer. These resources can be turned into "
							+ "extremely strong armor and weapons at the smithery.");
			stage = 4;
			break;
		case 4:
			sendNPCDialogue(npcId, CALM,
					"To go on voyages you'll need a ship. You may have up to "
							+ "5 ships which you will have to purchase from me in exchange for chime. Chime"
							+ " is the main currency used in the port.");
			stage = 5;
			break;
		case 5:
			sendItemDialogue(995, 200, "John has given you 200 Chime to start off with.");
			stage = 6;
			break;
		case 6:
			sendNPCDialogue(npcId, CALM,
					"That was all I had.. To earn more Chime you will have to successfully send "
							+ "your ships on voyages. Unsuccessful voyages will only earn you one third of the "
							+ "possible Chime reward.");
			player.getPorts().firstTimer = false;
			player.getPorts().chime += 200;
			stage = 7;
			break;
		case 7:
			sendPlayerDialogue(NORMAL, "Thank you, John. Could I start managing my ships " + "right now?");
			stage = 8;
			break;
		case 8:
			sendNPCDialogue(npcId, NORMAL, "Certainly!");
			stage = 11;
			break;

		case 11:
			if (!player.getPorts().meetRequirements()) {
				sendNPCDialogue(npcId, SAD,
						"I'm sorry, but you do not meet the requirements to start your " + "own port.");
				stage = 99;
				break;
			}
			sendOptionsDialogue(Colors.RED + "Ship Management",
					"Ship 'Alpha' - " + (!player.getPorts().hasFirstShip ? "purchase for " + player.getPorts().getShipCost("alpha", true) + " Chime."
							: (!player.getPorts().hasFirstShipReturned()
									? Colors.RED + "currently in a voyage - minutes left: "
											+ player.getPorts().getFirstVoyageTimeLeft() + "</col>."
									: (!player.getPorts().firstShipReward
											? Colors.GREEN + "voyage time ended - check for rewards</col>."
											: Colors.GREEN + "Send on a Voyage"))),
					"Ship 'Beta' - " + (!player.getPorts().hasSecondShip ? "purchase for " + player.getPorts().getShipCost("beta", true) + " Chime."
							: (!player.getPorts().hasSecondShipReturned()
									? Colors.RED + "currently in a voyage - minutes left: "
											+ player.getPorts().getSecondVoyageTimeLeft() + "</col>."
									: (!player.getPorts().secondShipReward
											? Colors.GREEN + "voyage time ended - check for rewards</col>."
											: Colors.GREEN + "Send on a Voyage"))),
					"Ship 'Gamma' - " + (!player.getPorts().hasThirdShip ? "purchase for " + player.getPorts().getShipCost("gamma", true) + " Chime."
							: (!player.getPorts().hasThirdShipReturned()
									? Colors.RED + "currently in a voyage - minutes left: "
											+ player.getPorts().getThirdVoyageTimeLeft() + "</col>."
									: (!player.getPorts().thirdShipReward
											? Colors.GREEN + "voyage time ended - check for rewards</col>."
											: Colors.GREEN + "Send on a Voyage"))),
					"Ship 'Delta' - " + (!player.getPorts().hasFourthShip ? "purchase for " + player.getPorts().getShipCost("delta", true) + " Chime."
							: (!player.getPorts().hasFourthShipReturned()
									? Colors.RED + "currently in a voyage - minutes left: "
											+ player.getPorts().getFourthVoyageTimeLeft() + "</col>."
									: (!player.getPorts().fourthShipReward
											? Colors.GREEN + "voyage time ended - check for rewards</col>."
											: Colors.GREEN + "Send on a Voyage"))),
					"Ship 'Epsilon' - " + (!player.getPorts().hasFifthShip ? "purchase for " + player.getPorts().getShipCost("epsilon", true) + " Chime."
							: (!player.getPorts().hasFifthShipReturned()
									? Colors.RED + "currently in a voyage - minutes left: "
											+ player.getPorts().getFifthVoyageTimeLeft() + "</col>."
									: (!player.getPorts().fifthShipReward
											? Colors.GREEN + "voyage time ended - check for rewards</col>."
											: Colors.GREEN + "Send on a Voyage"))));
			stage = 12;
			break;
		case 12:
			stage = 98;
			switch (componentId) {
			case OPTION_1:
				/** Purchase **/
				if (!player.getPorts().hasFirstShip) {
					sendOptionsDialogue("Purchase ship 'Alpha' for " + player.getPorts().getShipCost("alpha", true) + " Chime? " + "You have: "
							+ Utils.getFormattedNumber(player.getPorts().chime) + ".", "Yes.", "No.");
					stage = 13;
					break;
				}

				/** Reward if done with voyage **/
				if (player.getPorts().hasFirstShipReturned()) {
					if (!player.getPorts().firstShipReward) {
						if (player.getPorts().handleFirstShipReward()) {
							sendNPCDialogue(npcId, NORMAL, "Congratulations! Your ship returned safe and sound. "
									+ "You can view the noticeboard to see all your available resources.");
							break;
						}
						sendNPCDialogue(npcId, CALM, "Sadly your ship returned from an unsuccessful voyage, though, "
								+ "we should try again.");
						break;
					}

					/** Send on voyage **/
					sendOptionsDialogue(Colors.RED + "Ship Management</col> - Send ship 'Alpha' on a voyage",
							"30 minutes ( 90% success and 5% loot ).", "2 hours ( 80% success and 20% loot ).",
							"5 hours ( 60% success and 50% loot ).", "12 hours ( 40% success and 125% loot ).",
							"24 hours ( 20% success and 260% loot ).");
					stage = 14;
					break;
				}
				sendNPCDialogue(npcId, CALM, "Your ship has not yet returned.");
				break;
			case OPTION_2:
				/** Purchase **/
				if (!player.getPorts().hasSecondShip) {
					sendOptionsDialogue("Purchase ship 'Beta' for " + player.getPorts().getShipCost("beta", true) + " Chime? " + "You have: "
							+ Utils.getFormattedNumber(player.getPorts().chime) + ".", "Yes.", "No.");
					stage = 15;
					break;
				}

				/** Reward if done with voyage **/
				if (player.getPorts().hasSecondShipReturned()) {
					if (!player.getPorts().secondShipReward) {
						if (player.getPorts().handleSecondShipReward()) {
							sendNPCDialogue(npcId, NORMAL, "Congratulations! Your ship returned safe and sound. "
									+ "You can view the noticeboard to see all your available resources.");
							break;
						}
						sendNPCDialogue(npcId, CALM, "Sadly your ship returned from an unsuccessful voyage, though, "
								+ "we should try again.");
						break;
					}

					/** Send on voyage **/
					sendOptionsDialogue(Colors.RED + "Ship Management</col> - Send ship 'Beta' on a voyage",
							"30 minutes ( 90% success and 5% loot ).", "2 hours ( 80% success and 20% loot ).",
							"5 hours ( 60% success and 50% loot ).", "12 hours ( 40% success and 125% loot ).",
							"24 hours ( 20% success and 260% loot ).");
					stage = 16;
					break;
				}
				sendNPCDialogue(npcId, CALM, "Your ship has not yet returned.");
				break;
			case OPTION_3:
				/** Purchase **/
				if (!player.getPorts().hasThirdShip) {
					sendOptionsDialogue("Purchase ship 'Gamma' for " + player.getPorts().getShipCost("gamma", true) + " Chime? " + "You have: "
							+ Utils.getFormattedNumber(player.getPorts().chime) + ".", "Yes.", "No.");
					stage = 17;
					break;
				}

				/** Reward if done with voyage **/
				if (player.getPorts().hasThirdShipReturned()) {
					if (!player.getPorts().thirdShipReward) {
						if (player.getPorts().handleThirdShipReward()) {
							sendNPCDialogue(npcId, NORMAL, "Congratulations! Your ship returned safe and sound. "
									+ "You can view the noticeboard to see all your available resources.");
							break;
						}
						sendNPCDialogue(npcId, CALM, "Sadly your ship returned from an unsuccessful voyage, though, "
								+ "we should try again.");
						break;
					}

					/** Send on voyage **/
					sendOptionsDialogue(Colors.RED + "Ship Management</col> - Send ship 'Gamma' on a voyage",
							"30 minutes ( 90% success and 5% loot ).", "2 hours ( 80% success and 20% loot ).",
							"5 hours ( 60% success and 50% loot ).", "12 hours ( 40% success and 125% loot ).",
							"24 hours ( 20% success and 260% loot ).");
					stage = 18;
					break;
				}
				sendNPCDialogue(npcId, CALM, "Your ship has not yet returned.");
				break;
			case OPTION_4:
				/** Purchase **/
				if (!player.getPorts().hasFourthShip) {
					sendOptionsDialogue("Purchase ship 'Delta' for " + player.getPorts().getShipCost("delta", true) + " Chime? " + "You have: "
							+ Utils.getFormattedNumber(player.getPorts().chime) + ".", "Yes.", "No.");
					stage = 19;
					break;
				}

				/** Reward if done with voyage **/
				if (player.getPorts().hasFourthShipReturned()) {
					if (!player.getPorts().fourthShipReward) {
						if (player.getPorts().handleFourthShipReward()) {
							sendNPCDialogue(npcId, NORMAL, "Congratulations! Your ship returned safe and sound. "
									+ "You can view the noticeboard to see all your available resources.");
							break;
						}
						sendNPCDialogue(npcId, CALM, "Sadly your ship returned from an unsuccessful voyage, though, "
								+ "we should try again.");
						break;
					}

					/** Send on voyage **/
					sendOptionsDialogue(Colors.RED + "Ship Management</col> - Send ship 'Delta' on a voyage",
							"30 minutes ( 90% success and 5% loot ).", "2 hours ( 80% success and 20% loot ).",
							"5 hours ( 60% success and 50% loot ).", "12 hours ( 40% success and 125% loot ).",
							"24 hours ( 20% success and 260% loot ).");
					stage = 20;
					break;
				}
				sendNPCDialogue(npcId, CALM, "Your ship has not yet returned.");
				break;
			case OPTION_5:
				/** Purchase **/
				if (!player.getPorts().hasFifthShip) {
					sendOptionsDialogue("Purchase ship 'Epsilon' for " + player.getPorts().getShipCost("epsilon", true) + " Chime? " + "You have: "
							+ Utils.getFormattedNumber(player.getPorts().chime) + ".", "Yes.", "No.");
					stage = 21;
					break;
				}

				/** Reward if done with voyage **/
				if (player.getPorts().hasFifthShipReturned()) {
					if (!player.getPorts().fifthShipReward) {
						if (player.getPorts().handleFifthShipReward()) {
							sendNPCDialogue(npcId, NORMAL, "Congratulations! Your ship returned safe and sound. "
									+ "You can view the noticeboard to see all your available resources.");
							break;
						}
						sendNPCDialogue(npcId, CALM, "Sadly your ship returned from an unsuccessful voyage, though, "
								+ "we should try again.");
						break;
					}

					/** Send on voyage **/
					sendOptionsDialogue(Colors.RED + "Ship Management</col> - Send ship 'Epsilon' on a voyage",
							"30 minutes ( 90% success and 5% loot ).", "2 hours ( 80% success and 20% loot ).",
							"5 hours ( 60% success and 50% loot ).", "12 hours ( 40% success and 125% loot ).",
							"24 hours ( 20% success and 260% loot ).");
					stage = 22;
					break;
				}
				sendNPCDialogue(npcId, CALM, "Your ship has not yet returned.");
				break;
			}
			break;
		case 13: // purchase ship alpha
			switch (componentId) {
			case OPTION_1:
				if (player.getPorts().chime >=  Integer.parseInt(player.getPorts().getShipCost("alpha", false))) {
					sendNPCDialogue(npcId, NORMAL, "Congratulations on your first ship, port master. You "
							+ "can now send it on its first voyage!");
					player.getPorts().hasFirstShip = true;
					player.getAchievements().updateProgress(1, AchievementList.UNLOCK_ALL_PORT_BOATS);
					player.getPorts().firstShipReward = true;
					player.getPorts().chime -= Integer.parseInt(player.getPorts().getShipCost("alpha", false));
					stage = 98;
					break;
				}
				sendNPCDialogue(npcId, SAD, "I'm sorry, but you do not have enough Chime. To get more "
						+ "Chime send another ship on a voyage!");
				stage = 98;
				break;
			case OPTION_2:
				sendNPCDialogue(npcId, NORMAL, "As you wish!");
				stage = 98;
				break;
			}
			break;
		case 14: // first ship voyage time select
			player.getPorts().firstShipTime = Utils.currentTimeMillis();
			player.getPorts().firstShipReward = false;
			stage = 98;
			switch (componentId) {
			case OPTION_1:
				sendNPCDialogue(npcId, NORMAL, "I've sent ship 'Alpha' on a voyage; check back in 30 minutes!");
				player.getPorts().firstShipTimerOption = 1;
				player.getPorts().firstShipVoyage = 30 * 60 * 1000;
				break;
			case OPTION_2:
				sendNPCDialogue(npcId, NORMAL, "I've sent ship 'Alpha' on a voyage; check back in 2 hours!");
				player.getPorts().firstShipTimerOption = 2;
				player.getPorts().firstShipVoyage = 2 * 60 * 60 * 1000;
				break;
			case OPTION_3:
				sendNPCDialogue(npcId, NORMAL, "I've sent ship 'Alpha' on a voyage; check back in 5 hours!");
				player.getPorts().firstShipTimerOption = 3;
				player.getPorts().firstShipVoyage = 5 * 60 * 60 * 1000;
				break;
			case OPTION_4:
				sendNPCDialogue(npcId, NORMAL, "I've sent ship 'Alpha' on a voyage; check back in 12 hours!");
				player.getPorts().firstShipTimerOption = 4;
				player.getPorts().firstShipVoyage = 12 * 60 * 60 * 1000;
				break;
			case OPTION_5:
				sendNPCDialogue(npcId, NORMAL, "I've sent ship 'Alpha' on a voyage; check back in 24 hours!");
				player.getPorts().firstShipTimerOption = 5;
				player.getPorts().firstShipVoyage = 24 * 60 * 60 * 1000;
				break;
			}
			break;
		case 15: // purchase ship beta
			switch (componentId) {
			case OPTION_1:
				if (player.getPorts().chime >= Integer.parseInt(player.getPorts().getShipCost("beta", false))) {
					sendNPCDialogue(npcId, NORMAL, "Congratulations on your second ship, port master. You "
							+ "can now send it on its first voyage!");
					player.getPorts().hasSecondShip = true;
					player.getAchievements().updateProgress(1, AchievementList.UNLOCK_BETA_SHIP);
					player.getAchievements().updateProgress(1, AchievementList.UNLOCK_ALL_PORT_BOATS);
					player.getPorts().secondShipReward = true;
					player.getPorts().chime -= Integer.parseInt(player.getPorts().getShipCost("beta", false));
					stage = 98;
					break;
				}
				sendNPCDialogue(npcId, SAD, "I'm sorry, but you do not have enough Chime. To get more "
						+ "Chime send another ship on a voyage!");
				stage = 98;
				break;
			case OPTION_2:
				sendNPCDialogue(npcId, NORMAL, "As you wish!");
				stage = 98;
				break;
			}
			break;
		case 16: // second ship voyage time select
			player.getPorts().secondShipTime = Utils.currentTimeMillis();
			player.getPorts().secondShipReward = false;
			stage = 98;
			switch (componentId) {
			case OPTION_1:
				sendNPCDialogue(npcId, NORMAL, "I've sent ship 'Beta' on a voyage; check back in 30 minutes!");
				player.getPorts().secondShipTimerOption = 1;
				player.getPorts().secondShipVoyage = 30 * 60 * 1000;
				break;
			case OPTION_2:
				sendNPCDialogue(npcId, NORMAL, "I've sent ship 'Beta' on a voyage; check back in 2 hours!");
				player.getPorts().secondShipTimerOption = 2;
				player.getPorts().secondShipVoyage = 2 * 60 * 60 * 1000;
				break;
			case OPTION_3:
				sendNPCDialogue(npcId, NORMAL, "I've sent ship 'Beta' on a voyage; check back in 5 hours!");
				player.getPorts().secondShipTimerOption = 3;
				player.getPorts().secondShipVoyage = 5 * 60 * 60 * 1000;
				break;
			case OPTION_4:
				sendNPCDialogue(npcId, NORMAL, "I've sent ship 'Beta' on a voyage; check back in 12 hours!");
				player.getPorts().secondShipTimerOption = 4;
				player.getPorts().secondShipVoyage = 12 * 60 * 60 * 1000;
				break;
			case OPTION_5:
				sendNPCDialogue(npcId, NORMAL, "I've sent ship 'Beta' on a voyage; check back in 24 hours!");
				player.getPorts().secondShipTimerOption = 5;
				player.getPorts().secondShipVoyage = 24 * 60 * 60 * 1000;
				break;
			}
			break;
		case 17: // purchase ship gamma
			switch (componentId) {
			case OPTION_1:
				if (player.getPorts().chime >= Integer.parseInt(player.getPorts().getShipCost("gamma", false))) {
					sendNPCDialogue(npcId, NORMAL, "Congratulations on your third ship, port master. You "
							+ "can now send it on its first voyage!");
					player.getPorts().hasThirdShip = true;
					player.getAchievements().updateProgress(1, AchievementList.UNLOCK_ALL_PORT_BOATS);
					player.getPorts().thirdShipReward = true;
					player.getPorts().chime -= Integer.parseInt(player.getPorts().getShipCost("gamma", false));
					stage = 98;
					break;
				}
				sendNPCDialogue(npcId, SAD, "I'm sorry, but you do not have enough Chime. To get more "
						+ "Chime send another ship on a voyage!");
				stage = 98;
				break;
			case OPTION_2:
				sendNPCDialogue(npcId, NORMAL, "As you wish!");
				stage = 98;
				break;
			}
			break;
		case 18: // third ship voyage time select
			player.getPorts().thirdShipTime = Utils.currentTimeMillis();
			player.getPorts().thirdShipReward = false;
			stage = 98;
			switch (componentId) {
			case OPTION_1:
				sendNPCDialogue(npcId, NORMAL, "I've sent ship 'Gamma' on a voyage; check back in 30 minutes!");
				player.getPorts().thirdShipTimerOption = 1;
				player.getPorts().thirdShipVoyage = 30 * 60 * 1000;
				break;
			case OPTION_2:
				sendNPCDialogue(npcId, NORMAL, "I've sent ship 'Gamma' on a voyage; check back in 2 hours!");
				player.getPorts().thirdShipTimerOption = 2;
				player.getPorts().thirdShipVoyage = 2 * 60 * 60 * 1000;
				break;
			case OPTION_3:
				sendNPCDialogue(npcId, NORMAL, "I've sent ship 'Gamma' on a voyage; check back in 5 hours!");
				player.getPorts().thirdShipTimerOption = 3;
				player.getPorts().thirdShipVoyage = 5 * 60 * 60 * 1000;
				break;
			case OPTION_4:
				sendNPCDialogue(npcId, NORMAL, "I've sent ship 'Gamma' on a voyage; check back in 12 hours!");
				player.getPorts().thirdShipTimerOption = 4;
				player.getPorts().thirdShipVoyage = 12 * 60 * 60 * 1000;
				break;
			case OPTION_5:
				sendNPCDialogue(npcId, NORMAL, "I've sent ship 'Gamma' on a voyage; check back in 24 hours!");
				player.getPorts().thirdShipTimerOption = 5;
				player.getPorts().thirdShipVoyage = 24 * 60 * 60 * 1000;
				break;
			}
			break;

		case 19: // purchase ship delta
			switch (componentId) {
			case OPTION_1:
				if (player.getPorts().chime >= Integer.parseInt(player.getPorts().getShipCost("delta", false))) {
					sendNPCDialogue(npcId, NORMAL, "Congratulations on your fourth ship, port master. You "
							+ "can now send it on its first voyage!");
					player.getPorts().hasFourthShip = true;
					player.getAchievements().updateProgress(1, AchievementList.UNLOCK_ALL_PORT_BOATS);
					player.getPorts().fourthShipReward = true;
					player.getPorts().chime -= Integer.parseInt(player.getPorts().getShipCost("delta", false));
					stage = 98;
					break;
				}
				sendNPCDialogue(npcId, SAD, "I'm sorry, but you do not have enough Chime. To get more "
						+ "Chime send another ship on a voyage!");
				stage = 98;
				break;
			case OPTION_2:
				sendNPCDialogue(npcId, NORMAL, "As you wish!");
				stage = 98;
				break;
			}
			break;
		case 20: // fourth ship voyage time select
			player.getPorts().fourthShipTime = Utils.currentTimeMillis();
			player.getPorts().fourthShipReward = false;
			stage = 98;
			switch (componentId) {
			case OPTION_1:
				sendNPCDialogue(npcId, NORMAL, "I've sent ship 'Delta' on a voyage; check back in 30 minutes!");
				player.getPorts().fourthShipTimerOption = 1;
				player.getPorts().fourthShipVoyage = 30 * 60 * 1000;
				break;
			case OPTION_2:
				sendNPCDialogue(npcId, NORMAL, "I've sent ship 'Delta' on a voyage; check back in 2 hours!");
				player.getPorts().fourthShipTimerOption = 2;
				player.getPorts().fourthShipVoyage = 2 * 60 * 60 * 1000;
				break;
			case OPTION_3:
				sendNPCDialogue(npcId, NORMAL, "I've sent ship 'Delta' on a voyage; check back in 5 hours!");
				player.getPorts().fourthShipTimerOption = 3;
				player.getPorts().fourthShipVoyage = 5 * 60 * 60 * 1000;
				break;
			case OPTION_4:
				sendNPCDialogue(npcId, NORMAL, "I've sent ship 'Delta' on a voyage; check back in 12 hours!");
				player.getPorts().fourthShipTimerOption = 4;
				player.getPorts().fourthShipVoyage = 12 * 60 * 60 * 1000;
				break;
			case OPTION_5:
				sendNPCDialogue(npcId, NORMAL, "I've sent ship 'Delta' on a voyage; check back in 24 hours!");
				player.getPorts().fourthShipTimerOption = 5;
				player.getPorts().fourthShipVoyage = 24 * 60 * 60 * 1000;
				break;
			}
			break;

		case 21: // purchase ship epsilon
			switch (componentId) {
			case OPTION_1:
				if (player.getPorts().chime >= Integer.parseInt(player.getPorts().getShipCost("epsilon", false))) {
					sendNPCDialogue(npcId, NORMAL, "Congratulations on your fifth ship, port master. You "
							+ "can now send it on its first voyage!");
					player.getPorts().hasFifthShip = true;
					player.getAchievements().updateProgress(1, AchievementList.UNLOCK_ALL_PORT_BOATS);
					player.getPorts().fifthShipReward = true;
					player.getPorts().chime -= Integer.parseInt(player.getPorts().getShipCost("epsilon", false));
					stage = 98;
					break;
				}
				sendNPCDialogue(npcId, SAD, "I'm sorry, but you do not have enough Chime. To get more "
						+ "Chime send another ship on a voyage!");
				stage = 98;
				break;
			case OPTION_2:
				sendNPCDialogue(npcId, NORMAL, "As you wish!");
				stage = 98;
				break;
			}
			break;
		case 22: // fifth ship voyage time select
			player.getPorts().fifthShipTime = Utils.currentTimeMillis();
			player.getPorts().fifthShipReward = false;
			stage = 98;
			switch (componentId) {
			case OPTION_1:
				sendNPCDialogue(npcId, NORMAL, "I've sent ship 'Epsilon' on a voyage; check back in 30 minutes!");
				player.getPorts().fifthShipTimerOption = 1;
				player.getPorts().fifthShipVoyage = 30 * 60 * 1000;
				break;
			case OPTION_2:
				sendNPCDialogue(npcId, NORMAL, "I've sent ship 'Epsilon' on a voyage; check back in 2 hours!");
				player.getPorts().fifthShipTimerOption = 2;
				player.getPorts().fifthShipVoyage = 2 * 60 * 60 * 1000;
				break;
			case OPTION_3:
				sendNPCDialogue(npcId, NORMAL, "I've sent ship 'Epsilon' on a voyage; check back in 5 hours!");
				player.getPorts().fifthShipTimerOption = 3;
				player.getPorts().fifthShipVoyage = 5 * 60 * 60 * 1000;
				break;
			case OPTION_4:
				sendNPCDialogue(npcId, NORMAL, "I've sent ship 'Epsilon' on a voyage; check back in 12 hours!");
				player.getPorts().fifthShipTimerOption = 4;
				player.getPorts().fifthShipVoyage = 12 * 60 * 60 * 1000;
				break;
			case OPTION_5:
				sendNPCDialogue(npcId, NORMAL, "I've sent ship 'Epsilon' on a voyage; check back in 24 hours!");
				player.getPorts().fifthShipTimerOption = 5;
				player.getPorts().fifthShipVoyage = 24 * 60 * 60 * 1000;
				break;
			}
			break;

		case 98:
			sendOptionsDialogue(Colors.RED + "Ship Management",
					"Ship 'Alpha' - " + (!player.getPorts().hasFirstShip ? "purchase for " + player.getPorts().getShipCost("alpha", true) + " Chime."
							: (!player.getPorts().hasFirstShipReturned()
									? Colors.RED + "currently in a voyage - minutes left: "
											+ player.getPorts().getFirstVoyageTimeLeft() + "</col>."
									: (!player.getPorts().firstShipReward
											? Colors.GREEN + "voyage time ended - check for rewards</col>."
											: Colors.GREEN + "Send on a Voyage"))),
					"Ship 'Beta' - " + (!player.getPorts().hasSecondShip ? "purchase for " + player.getPorts().getShipCost("beta", true) + " Chime."
							: (!player.getPorts().hasSecondShipReturned()
									? Colors.RED + "currently in a voyage - minutes left: "
											+ player.getPorts().getSecondVoyageTimeLeft() + "</col>."
									: (!player.getPorts().secondShipReward
											? Colors.GREEN + "voyage time ended - check for rewards</col>."
											: Colors.GREEN + "Send on a Voyage"))),
					"Ship 'Gamma' - " + (!player.getPorts().hasThirdShip ? "purchase for " + player.getPorts().getShipCost("gamma", true) + " Chime."
							: (!player.getPorts().hasThirdShipReturned()
									? Colors.RED + "currently in a voyage - minutes left: "
											+ player.getPorts().getThirdVoyageTimeLeft() + "</col>."
									: (!player.getPorts().thirdShipReward
											? Colors.GREEN + "voyage time ended - check for rewards</col>."
											: Colors.GREEN + "Send on a Voyage"))),
					"Ship 'Delta' - " + (!player.getPorts().hasFourthShip ? "purchase for " + player.getPorts().getShipCost("delta", true) + " Chime."
							: (!player.getPorts().hasFourthShipReturned()
									? Colors.RED + "currently in a voyage - minutes left: "
											+ player.getPorts().getFourthVoyageTimeLeft() + "</col>."
									: (!player.getPorts().fourthShipReward
											? Colors.GREEN + "voyage time ended - check for rewards</col>."
											: Colors.GREEN + "Send on a Voyage"))),
					"Ship 'Epsilon' - " + (!player.getPorts().hasFifthShip ? "purchase for " + player.getPorts().getShipCost("epsilon", true) + " Chime."
							: (!player.getPorts().hasFifthShipReturned()
									? Colors.RED + "currently in a voyage - minutes left: "
											+ player.getPorts().getFifthVoyageTimeLeft() + "</col>."
									: (!player.getPorts().fifthShipReward
											? Colors.GREEN + "voyage time ended - check for rewards</col>."
											: Colors.GREEN + "Send on a Voyage"))));
			stage = 12;
			break;
		case 99:
			end();
			break;
		}
	}

	@Override
	public void finish() {
	}
}