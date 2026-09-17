package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.data.parsers.misc.ShopsDataParser;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * 
 * Created on Oct 9, 2018.
 */
public class WizardFinixHomeD extends Dialogue {
	
	private int npcId;

	@Override
	public void start() {
		stage = 0;
		npcId = (int) parameters[0];
		sendNPCDialogue(npcId, NORMAL, "Hello! How may I help you?");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case 0:
			sendPlayerDialogue(NORMAL, "I would like to view your shop.");
			stage = 1;
			break;
		case 1:
			sendNPCDialogue(npcId, NORMAL, "Yes, of course! Here you go.");
			stage = 2;
			break;
		case 2:
			ShopsDataParser.openShop(player, 170);
			end();
			break;
		}
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		
	}

}
