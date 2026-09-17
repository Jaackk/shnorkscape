package com.rs.game.player.dialogue.impl;

import com.rs.cores.CoresManager;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.controllers.FightKiln;
import com.rs.game.player.cutscenes.Cutscene;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import java.util.concurrent.TimeUnit;

public class TokHaarHok extends Dialogue {

	private int npcId;
	private int type;
	private FightKiln fightKiln;

	@Override
	public void start() {
		type = (Integer) parameters[0];
		npcId = (Integer) parameters[1];
		fightKiln = (FightKiln) parameters[2];
		if (type == 0)
			sendNPCDialogue(npcId, 9827, "Let us talk...");
		else if (type == 1)
			sendNPCDialogue(npcId, 9827, "So.. you accept our challenge. Let our sport be glorious. Xill - attack!");
		else if (type == 2)
			sendNPCDialogue(npcId, 9827, "Well fought, " + player.getDisplayName()
					+ ". You are ferocious, but you must fight faster.. the lava is rising.");
		else if (type == 3)
			sendNPCDialogue(npcId, 9827,
					"You must be carved from a rock inpervious to magic.. you are quite the worthy foe.");
		else if (type == 4)
			sendNPCDialogue(npcId, 9827,
					"Hurry, " + player.getDisplayName() + ".. kill my brothers before the lava consumes you.");
		else if (type == 7)
			sendNPCDialogue(npcId, 9827,
					"Amazing! We haven't had such fun in such a long time. But now, the real challenge begins..");
		else if (type == 5)
			sendNPCDialogue(npcId, 9827,
					"We have thrown many waves at you.. you have handled yourself like a true Tokhaar. You have earned our respect.");
		else if (type == 6)
			sendNPCDialogue(npcId, 9827,
					"You are a Tokhaar.. born in a human's body. Truly, we have not seen such skill from anyone out of our kiln.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (type) {
		case 0:
			switch (stage) {
			case -1:
				stage = 0;
				sendPlayerDialogue(CALM, "Let's fight!");
				break;
			case 0:
				stage = 1;
				sendNPCDialogue(npcId, 9827, "Do you have any questions on the rules of our engagement?");
				break;
			case 1:
				stage = 2;
				sendPlayerDialogue(CALM, "No, I came here to fight, not chit-chat!");
				break;
			case 2:
			case 3:
				fightKiln.removeTokHaarTok();
				fightKiln.nextWave();
				end();
				break;
			}
			break;
		case 1:
			end();
			break;
		case 2:
			switch (stage) {
			case -1:
				stage = 0;
				player.getInterfaceManager().closeChatBoxInterface();
				WorldTile lookTo = fightKiln.getWorldTile(37, 50);
				player.getPackets().sendCameraLook(Cutscene.getX(player, lookTo.getX()),
						Cutscene.getY(player, lookTo.getY()), 1000);
				WorldTile posTile = fightKiln.getWorldTile(37, 45);
				player.getPackets().sendCameraPos(Cutscene.getX(player, posTile.getX()),
						Cutscene.getY(player, posTile.getY()), 3000);
				CoresManager.getServiceProvider().executeWithDelay(new Runnable() {
					@Override
					public void run() {
						try {
							sendNPCDialogue(npcId, 9827, "Our Mej wish to test you..");
						} catch (Throwable e) {
							Logger.getGlobal().catching(e);
						}
					}
				}, 3, TimeUnit.SECONDS);
				break;
			case 0:
				end();
				break;
			}
			break;
		case 3:
			switch (stage) {
			case -1:
				stage = 0;
				sendNPCDialogue(npcId, 9827,
						"Ah, the platform is crumbling. Be quick little one - our Ket are comming.");
				break;
			case 0:
				end();
				break;
			}
			break;
		case 4:
			end();
			break;
		case 7:
			switch (stage) {
			case -1:
				stage = 0;
				sendPlayerDialogue(9810, "The real challenge?");
				break;
			case 0:
				stage = 1;
				sendNPCDialogue(npcId, 9827,
						"Many creatures have entered the kiln over the ages. We remember their shapes.");
				break;
			case 1:
				end();
				break;
			}
			break;
		case 5:
			switch (stage) {
			case -1:
				stage = 0;
				sendNPCDialogue(npcId, 9827, "Take this cape as a symbol of our -");
				break;
			case 0:
				stage = 1;
				fightKiln.showHarAken();
				player.getInterfaceManager().closeChatBoxInterface();
				CoresManager.getServiceProvider().executeWithDelay(new Runnable() {
					@Override
					public void run() {
						try {
							sendNPCDialogue(npcId, 9827, "Ah - yes, there is one final challenge...");
						} catch (Throwable e) {
							Logger.getGlobal().catching(e);
						}
					}
				}, 3, TimeUnit.SECONDS);
				break;
			case 1:
				end();
				fightKiln.hideHarAken();
				CoresManager.getServiceProvider().executeWithDelay(new Runnable() {
					@Override
					public void run() {
						try {
							fightKiln.removeScene();
						} catch (Throwable e) {
							Logger.getGlobal().catching(e);
						}
					}
				}, 3, TimeUnit.SECONDS);
				break;
			}
			break;

		case 6:
			switch (stage) {
			case -1:
				stage = 0;
				sendNPCDialogue(npcId, 9827,
						"You have done very well. To mark your triumph, accept a trophy from our home.");
				break;
			case 0:
				stage = 1;
				sendOptionsDialogue("Choose your reward:", "The TokHaar-Kal-Ket (melee)", "The TokHaar-Kal-Xil (range)",
						"The TokHaar-Kal-Mej (mage)", "x2 Uncut Onyx's");
				break;
			case 1:
				if (componentId == OPTION_1) {
					stage = 2;
					sendNPCDialogue(npcId, 9827,
							"The TokHaar-Kal-Ket is a powerful cape that will let others see that you have mastered the Fight Kiln. "
									+ "In addition to this, it provides several melee stat bonuses.");
				}
				if (componentId == OPTION_2) {
					stage = 4;
					sendNPCDialogue(npcId, 9827,
							"The TokHaar-Kal-Xil is a powerful cape that will let others see that you have mastered the Fight Kiln. "
									+ "In addition to this, it provides several ranged stat bonuses.");
				}
				if (componentId == OPTION_3) {
					stage = 6;
					sendNPCDialogue(npcId, 9827,
							"The TokHaar-Kal-Mej is a powerful cape that will let others see that you have mastered the Fight Kiln. "
									+ "In addition to this, it provides several magic stat bonuses.");
				}
				if (componentId == OPTION_4) {
					stage = 8;
					sendNPCDialogue(npcId, 9827,
							"Onyx is a precious and rare gem that can be crafted into one of several powerful objects, "
									+ "including the coveted Amulet of Fury.");
				}
				break;
			case 2:
				stage = 3;
				sendOptionsDialogue("Accept the TokHaar-Kal-Ket?", "Yes.", "No.");
				break;
			case 3:
				if (componentId == OPTION_1) {
					player.getTemporaryAttributtes().put("FightKilnReward", new Item(23659));
					sendNPCDialogue(npcId, 9827, "Let us test our strength again.. soon.");
					stage = 10;
				} else {
					stage = 1;
					sendOptionsDialogue("Choose your reward:", "The TokHaar-Kal-Ket (melee)",
							"The TokHaar-Kal-Xil (range)", "The TokHaar-Kal-Mej (mage)", "x2 Uncut Onyx's");
				}
				break;
			case 4:
				stage = 5;
				sendOptionsDialogue("Accept the TokHaar-Kal-Xil?", "Yes.", "No.");
				break;
			case 5:
				if (componentId == OPTION_1) {
					player.getTemporaryAttributtes().put("FightKilnReward", new Item(31610));
					sendNPCDialogue(npcId, 9827, "Let us test our strength again.. soon.");
					stage = 10;
				} else {
					stage = 1;
					sendOptionsDialogue("Choose your reward:", "The TokHaar-Kal-Ket (melee)",
							"The TokHaar-Kal-Xil (range)", "The TokHaar-Kal-Mej (mage)", "x2 Uncut Onyx's");
				}
				break;
			case 6:
				stage = 7;
				sendOptionsDialogue("Accept the TokHaar-Kal-Mej?", "Yes.", "No.");
				break;
			case 7:
				if (componentId == OPTION_1) {
					player.getTemporaryAttributtes().put("FightKilnReward", new Item(31611));
					sendNPCDialogue(npcId, 9827, "Let us test our strength again.. soon.");
					stage = 10;
				} else {
					stage = 1;
					sendOptionsDialogue("Choose your reward:", "The TokHaar-Kal-Ket (melee)",
							"The TokHaar-Kal-Xil (range)", "The TokHaar-Kal-Mej (mage)", "x2 Uncut Onyx's");
				}
				break;
			case 8:
				stage = 9;
				sendOptionsDialogue("Accept the x2 Uncut Onyx's?", "Yes.", "No.");
				break;
			case 9:
				if (componentId == OPTION_1) {
					player.getTemporaryAttributtes().put("FightKilnReward", new Item(6572, Utils.random(2, 3)));
					sendNPCDialogue(npcId, 9827, "Let us test our strength again.. soon.");
					stage = 10;
				} else {
					stage = 1;
					sendOptionsDialogue("Choose your reward:", "The TokHaar-Kal-Ket (melee)",
							"The TokHaar-Kal-Xil (range)", "The TokHaar-Kal-Mej (mage)", "An uncut Onyx");
				}
				break;
			case 10:
				stage = 11;
				sendNPCDialogue(npcId, 9827, "Now, leave.. before the lava consumes you.");
				break;
			case 11:
				end();
				break;
			}
			break;
		}
	}

	@Override
	public void finish() {
		player.closeInterfaceLocked = false;
		if (type == 5)
			fightKiln.unlockPlayer();
		else if (type != 0)
			fightKiln.removeScene();
	}
}