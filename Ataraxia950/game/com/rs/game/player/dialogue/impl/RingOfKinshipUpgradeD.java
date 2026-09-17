package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;

public class RingOfKinshipUpgradeD extends Dialogue {

	private int classId, cost;
	private String className;
	
	@Override
	public void start() {
		this.classId = (int) parameters[0];
		this.cost = (int) parameters[1];
		this.className = player.getRingOfKinship().getClassName(classId);
		sendDialogue("Upgrading " + className + " class is an irreversible process. You will not be able to retrieve the tokens spent on the upgrade in any way. The class effect is only usable inside of Daemonheim, as long as the given class is active. This upgrade will cost you " + cost + " dungeoneering tokens. Are you sure you wish to upgrade " + className + " class?");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch(stage) {
		case -1:
			sendOptionsDialogue("Select an Option", "Yes, upgrade " + className + " class.", "No, don't upgrade " + className + " class.");
			break;
		case 0:
			if (componentId == OPTION_1) 
				player.getRingOfKinship().upgradeTier(classId, false);
			end();
		}
		stage++;
	}

	@Override
	public void finish() {
		
	}

}
