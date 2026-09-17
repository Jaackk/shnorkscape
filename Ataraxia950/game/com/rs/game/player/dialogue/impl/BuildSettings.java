package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

public class BuildSettings extends Dialogue {

	@Override
	public void start() {
		if (player.getMoneySpent() >= 100) {
			sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, player.isLegacyHitSplat() ? "Toggle Eoc Hitsplats" : "Toggle Legacy Hitsplats", player.isHasDropTableEnabled() ? Colors.RED + "Toggle Drop Table Examine" : Colors.GREEN + "Toggle Drop Table Examine", (player.isNotingDrops() ? "<col=00ff00>" : "<col=ff0000>") + (player.isDiamondDonor() ? "Toggle drop noting" : "Toggle bones/ashes noting"), player.lockedShiftDrop ? "Unlock shift-drop." : "Lock shift-drop.");
		} else {
			sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, player.isLegacyHitSplat() ? "Toggle Eoc Hitsplats" : "Toggle Legacy Hitsplats", player.isHasDropTableEnabled() ? Colors.RED + "Toggle Drop Table Examine" : Colors.GREEN + "Toggle Drop Table Examine", player.lockedShiftDrop ? "Unlock shift-drop." : "Lock shift-drop.");
		}
	}

	@Override
	public void run(final int interfaceId, final int componentId) {
		if (componentId == OPTION_1) {
			player.setLegacyHitSplat(!player.isLegacyHitSplat());
			player.getDialogueManager().startDialogue("BuildSettings");
		} else if (componentId == OPTION_2) {
			player.setHasDropTableEnabled(!player.isHasDropTableEnabled());
			player.getDialogueManager().startDialogue("BuildSettings");
		} else if (componentId == OPTION_3) {
			if (player.getMoneySpent() < 100) {
				player.lockedShiftDrop = !player.lockedShiftDrop;
				player.getDialogueManager().startDialogue("BuildSettings");
			} else {
				player.setNotingDrops(!player.isNotingDrops());
				player.getDialogueManager().startDialogue("BuildSettings");
			}
		} else if (componentId == OPTION_4) {
			player.lockedShiftDrop = !player.lockedShiftDrop;
			player.getDialogueManager().startDialogue("BuildSettings");
		}
	}

	@Override
	public void finish() {

	}
}
