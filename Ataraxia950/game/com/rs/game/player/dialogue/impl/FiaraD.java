package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;

/**
 * The dialogue for the fiara fog npc.
 * 
 * @author _Waterfiend <skype:alco-wahidi>
 *
 * Created in Apr 30, 2017 at 9:20:54 PM.
 */
public class FiaraD extends Dialogue {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rs.game.player.dialogue.Dialogue#start()
	 */
	@Override
	public void start() {
		sendNPCDialogue(7600, NORMAL, "What brings you here, " + player.getDisplayName() + "?");
		stage = 1;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rs.game.player.dialogue.Dialogue#run(int, int)
	 */
	@Override
	public void run(int interfaceId, int componentId) {
		switch (stage) {
		case 1:
			sendPlayerDialogue(9827, "I would like to learn more about this minigame!");
			stage = 2;
			break;
		case 2:
			sendNPCDialogue(7600, NORMAL, "You acquire two different types of runes upon entering. With those runes, you are able to cast any spell in your spellbook.");
			stage = 3;
			break;
		case 3:
			sendNPCDialogue(7600, NORMAL, "Proceeding, there are the prey and hunter roles. The prey has to escape without dying, and the hunter must chase the player with the magic stone.");
			stage = 4;
			break;
		case 4:
			sendNPCDialogue(7600, NORMAL, "Finally, you may enter the barriers to be teleported to a random base throughout the map. This is a good strategy to escape, when having a decent amount of hitpoints. Whilst having the stone wielded, it is advised to camp in the center for an increase in charge-count.");
			stage = 5;
			break;
		case 5:
			sendNPCDialogue(7600, NORMAL, "Good luck, " + player.getDisplayName() + "!");
			stage = 6;
			break;
		case 6:
			end();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rs.game.player.dialogue.Dialogue#finish()
	 */
	@Override
	public void finish() {
		// TODO Auto-generated method stub

	}

}