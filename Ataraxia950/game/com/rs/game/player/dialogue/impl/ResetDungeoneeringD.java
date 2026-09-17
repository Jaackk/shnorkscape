package com.rs.game.player.dialogue.impl;

import com.rs.Settings;
import com.rs.game.player.Skills;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Utils;

public class ResetDungeoneeringD extends Dialogue {

	@Override
	public void start() {
		sendDialogue("Before you continue your dungeoneering journey, you have the possiblity to reset your current dungeoneering progress completely. "
				+ "The new dungeoneering is absolutely uncomparable to the old one, "
				+ "so we advise you to reset your progress in dungeoneering completely. "
				+ "By doing so, you will receive " + Utils.formatNumber((int)((player.getSkills().getXp(Skills.DUNGEONEERING) / Settings.getExperienceMultiplier(player)) / 10)) + 
				" dungeoneering tokens in exchange for all your experience.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1)
			sendDialogue("If you choose not to reset your dungeoneering progress, and continue with your previous one, you will no longer be able to reset it in this specific method.");
		else if (stage == 0) 
			sendOptionsDialogue("Reset your dungeoneering for " + Utils.formatNumber((int)((player.getSkills().getXp(Skills.DUNGEONEERING) / Settings.getExperienceMultiplier(player)) / 10)) + " tokens?", "Reset my dungeoneering.", "Stop, don't reset my dungeoneering.");
		else if (stage == 1) {
			if (componentId == OPTION_1) {
				player.sendMessage("You've completely reset your dungeoneering experience for " + Utils.formatNumber((int)((player.getSkills().getXp(Skills.DUNGEONEERING) / Settings.getExperienceMultiplier(player)) / 10)) + " dungeoneering tokens!");
				player.getDungeoneeringManager().addTokens((int)((player.getSkills().getXp(Skills.DUNGEONEERING) / Settings.getExperienceMultiplier(player)) / 10));
				player.getSkills().set(Skills.DUNGEONEERING, 1);
				player.getSkills().setXp(Skills.DUNGEONEERING, 0);
				player.resetDg = true;
				player.getSkills().refresh(Skills.DUNGEONEERING);
			}
			end();
			player.getDungeoneeringManager().enterDungeon(true, true, false);
		}
		stage++;
	}

	@Override
	public void finish() {
		
	}

}
