package com.rs.game.player.dialogue.impl;

import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.content.contracts.Contract;
import com.rs.game.player.content.contracts.ContractHandler;
import com.rs.game.player.content.interfaces.ReaperBenefitsInterface;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.misc.ShopsDataParser;

import lombok.val;

public class ContractDialogue extends Dialogue {

	public static final int idNo = 14386;

	@Override
	public void start() {
		sendNPCDialogue(idNo, CROOKED_HEAD, "Ahh.. " + Utils.formatPlayerNameForDisplay(player.getUsername()) + " the only soul ",
				"I cannot claim. You're here about my", "special tasks, no?");
		stage = 1;
	}

	@Override
	public void run(int interfaceId, int componentId) {
		String npcName = "";
		val resetCost = player.getPerkManager().hasPerkActive(DonationPerk.THE_DISCOUNTER) ? 2500000 : 5000000;

		if (player.getContract() != null) {
			npcName = ContractHandler.getFormattedContractName(player);
		}

		if (stage == 1) {
			sendOptionsDialogue("Select an Option", "I need another contract.", "I wish to check on my progress.",
					"I want to reset my task.", "I'd like to see your shop");
			stage = 2;
		} else if (stage == 2) {
			if (componentId == OPTION_1) {
				if (player.getContract() != null) {
					if (player.getContract().hasCompleted()) {
						player.setReaperPoints(player.getReaperPoints() + Contract.givePoints(player));
						{
							player.setContract(null);
							sendNPCDialogue(idNo, CROOKED_HEAD,
									"It looks like you haven't collected your reward yet.<br>",
									"Here you are, " + player.getUsername() + ". Talk to me ", "for another contract.");
							stage = 1;
						}

					} else {
						sendNPCDialogue(idNo, CROOKED_HEAD, "You already have an active contract.<br>",
								"Complete this one or get a new one if you want.<br>",
								"Current Contract: <col=FF0000>" + npcName + "</col><br>");
						stage = 1;
					}
				} else {
	                if (player.isChooseTask()) {
	                    end();
	                    player.getDialogueManager().startDialogue("ReapersChoiceD", true);
	                    return;
	                }
					ContractHandler.assignPlayerNewContract(player);
					npcName = ContractHandler.getFormattedContractName(player);
					sendNPCDialogue(idNo, CROOKED_HEAD, "Your new contract is to kill:<br>",
							"<col=FF0000>" + player.getContract().getKillAmount() + "x " + npcName + "<br>",
							"Reward: <col=0000FF>" + player.getContract().getRewardAmount() + " Reaper Points");
					stage = 1;
				}
			}

			if (componentId == OPTION_2) {
				if (player.getContract() == null) {
					sendNPCDialogue(idNo, CROOKED_HEAD, "You don't have an active contract, ",
							"collect a new one from me when you're ready.");
					stage = 1;
				} else {
					if (player.getContract().hasCompleted()) {
						sendNPCDialogue(idNo, CROOKED_HEAD, "You have a completed contract to turn in, ",
								"you must turn it in before starting a new one.");
						stage = 1;
					} else {
						sendNPCDialogue(idNo, CROOKED_HEAD, "You still have an active contract!<br>",
								"Current: <col=FF0000>" + player.getContract().getKillAmount() + "x " + npcName
										+ "<br>",
								"Reward: <col=0000FF>" + player.getContract().getRewardAmount() + " Reaper Points ");
						stage = 1;
					}
				}
			}

			if (componentId == OPTION_3) {
			    if(player.isSkipTask()) 
			        sendNPCDialogue(idNo, CROOKED_HEAD, "You have the ability to skip your task for fee thanks to your take two perk, Do you want to skip your task?");
			    else
				sendNPCDialogue(idNo, CROOKED_HEAD, "Choosing this will cost you <col=FF0000><shad=000000>" + Utils.getFormattedNumber(resetCost) + "</col></shad> coins,<br>",
						"do you wish to proceed?");
				stage = 3;
			}

			if (componentId == OPTION_4) {
				sendNPCDialogue(idNo, CROOKED_HEAD, "Of course...");
				stage = 5;
			}
			if (componentId == OPTION_5) {
				end();
			}

		} else if (stage == 3) {
			sendOptionsDialogue("Select an Option", "Yes, I wish to change my contract",
					"No, I do not want to change my contract");
			stage = 4;
		} else if (stage == 4) {
			if (componentId == OPTION_1) {
				if (player.getContract() == null) {
					sendNPCDialogue(idNo, CROOKED_HEAD, "You don't have an active contract, ",
							"collect a new one from me when you're ready.");
					stage = 1;
				} else {
					if (player.isSkipTask() || player.hasMoney(resetCost)) {
					    if(!player.isSkipTask())
					        player.takeMoney(resetCost);
						player.setContract(null);
						ContractHandler.assignPlayerNewContract(player);
						npcName = ContractHandler.getFormattedContractName(player);
						sendNPCDialogue(idNo, CROOKED_HEAD, "Your new contract is to kill:<br>",
								"<col=FF0000>" + player.getContract().getKillAmount() + "x " + npcName + "<br>",
								"Reward: <col=0000FF>" + player.getContract().getRewardAmount() + " Reaper Points");
						if (!player.isSkipTask() && player.getPerkManager().hasPerkActive(DonationPerk.THE_DISCOUNTER)) {
							player.sendMessage("The reset cost was halved thanks to your The Discounter perk.",true);
						}
                        if(player.isSkipTask())
                            player.setSkipTask(false);
						stage = 1;
					} else {
						sendNPCDialogue(idNo, CROOKED_HEAD, "Come back when you have more coins.",
								"You need at least " + Utils.getFormattedNumber(resetCost) + " coins to reset your Contract.");
						stage = 1;
					}
				}
			} else {
				end();
			}
		} else if (stage == 5) {
		    stage = 6;
		    sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Reaper Perks" , "Reaper Points Shop");
		} else if(stage == 6) {
            end();
		    if (componentId == OPTION_1) 
		       // player.getDialogueManager().startDialogue("ReaperPerksD");
				ReaperBenefitsInterface.sendInterface(player);
		    else
	            ShopsDataParser.openShop(player, 58);
		}
	}

	@Override
	public void finish() {
	}
}