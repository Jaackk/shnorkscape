package com.rs.game.player.dialogue.impl;

import com.rs.game.player.content.ectofuntus.Ectofuntus;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.data.parsers.misc.ShopsDataParser;

/**
 * A class containing the dialogue of the ghost disciple npc of the {@link Ectofuntus} activity.
 * 
 * @author _Waterfiend <skype:alco-wahidi>
 *
 * Created in Apr 29, 2017 at 11:30:49 AM.
 */
public class GhostDisciple extends Dialogue {
	
	/**
	 * The id of the {@link GhostDisciple} npc.
	 */
	public int npcId;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rs.game.player.dialogue.Dialogue#start()
	 */
	@Override
	public void start() {
		npcId = (Integer) parameters[0];
		sendNPCDialogue(npcId, 9827, "Wooo wooo woooooo.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rs.game.player.dialogue.Dialogue#run(int, int)
	 */
	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case -1:
			stage = 0;
			sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Are you selling anything?", "Can I claim my ectotokens?", "Nevermind.");
			break;
		case 0:
			if (componentId == OPTION_1) {
				stage = -1;
				ShopsDataParser.openShop(player, 198);
				end();
			} else if (componentId == OPTION_2) {
				stage = -1;
				if (player.getInventory().hasFreeSlots() && player.tokensUnclaimed > 0) {
					 player.getInventory().addItem(4278, player.tokensUnclaimed);
					player.tokensUnclaimed = 0;
				}
				sendNPCDialogue(npcId, 9827, "Woo wooooo woo woo woooo woo woo wooo!");
			} else if (componentId == OPTION_3) {
				end();
			}
			break;
		default:
			end();
			break;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rs.game.player.dialogue.Dialogue#finish()
	 */
	@Override
	public void finish() {
		/* empty */
		
	}

}
