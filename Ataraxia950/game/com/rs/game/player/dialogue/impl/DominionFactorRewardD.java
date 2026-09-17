package com.rs.game.player.dialogue.impl;

import com.rs.game.player.Skills;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Utils;

import java.util.concurrent.TimeUnit;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * 
 * Created on Jan 16, 2019.
 */
public class DominionFactorRewardD extends Dialogue {
	
	private int skill;

	@Override
	public void start() {
		sendDialogue("You have a Dominion Factor of " + Utils.formatNumber(player.getDominionTower().getDominionFactor()) + ".");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (player.getDominionTower().getDominionFactor() <= 0) {
			end();
			return;
		}
		
		if (stage == -1) {
			sendDialogue("Choose a skill to spend your Dominion Factor on. Your factor will be reset to 0 and the amount of experience you gain is that of your factor.");
			stage = 0;
		} else if (stage == 0) {
			sendOptionsDialogue("Choose a skill.", "Attack", "Strength", "Defence", "Constitution", "Next...");
			stage = 1;
		} else if (stage == 1) {
			if (componentId == OPTION_1) {
				skill = Skills.ATTACK;
				sendOptionsDialogue("Are you sure you chose " + player.getSkills().getSkillName(skill) + "?", "Yes.", "No.");
				stage = 10;
			} else if (componentId == OPTION_2) {
				skill = Skills.STRENGTH;
				sendOptionsDialogue("Are you sure you chose " + player.getSkills().getSkillName(skill) + "?", "Yes.", "No.");
				stage = 10;
			} else if (componentId == OPTION_3) {
				skill = Skills.DEFENCE;
				sendOptionsDialogue("Are you sure you chose " + player.getSkills().getSkillName(skill) + "?", "Yes.", "No.");
				stage = 10;
			} else if (componentId == OPTION_4) {
				skill = Skills.HITPOINTS;
				sendOptionsDialogue("Are you sure you chose " + player.getSkills().getSkillName(skill) + "?", "Yes.", "No.");
				stage = 10;
			} else if (componentId == OPTION_5) {
				sendOptionsDialogue("Choose a skill", "Ranged", "Magic", "Prayer");
				stage = 3;
			}
		} else if (stage == 3) {
			if (componentId == OPTION_1)
				skill = Skills.RANGE;
			else if (componentId == OPTION_2)
				skill = Skills.MAGIC;
			else if (componentId == OPTION_3)
				skill = Skills.PRAYER;
			
			sendOptionsDialogue("Are you sure you chose " + player.getSkills().getSkillName(skill) + "?", "Yes.", "No.");
			stage = 10;
		} else if (stage == 10) {
			if (componentId == OPTION_1) {
				int experience = player.getDominionTower().getDominionFactor();
				int hours = experience / 1000000;
				if (hours < 1) {
					sendOptionsDialogue("You need atleast 1,000,000 factor to recieve any bonus experience<br>and you have " + Utils.formatNumber(player.getDominionTower().dominionFactor) + ". Do you wish to continue?", "Yes.", "No.");
					stage = 11;
				} else {
					doBonusExperience();
					doExpReward();
					end();
				}
			} else if (componentId == OPTION_2)
				end();
		} else if (stage == 11) {
			if (componentId == OPTION_1) {
				doBonusExperience();
				doExpReward();
			}
			end();
		}
	}
	
	private void doBonusExperience() {
		int experience = player.getDominionTower().getDominionFactor();
		if (player.getPerkManager().hasPerkActive(DonationPerk.DOMINION_DOMINATION))
			experience = (int) (experience + (experience * 0.25));
		
		if (experience > 0) {
			int hours = experience / 1000000;
			if (hours > 0) {
				long millis = TimeUnit.HOURS.toMillis(hours);
				player.setBonusXpTimer(player.getBonusXpTimer() + (millis / 600));
				player.sendMessage("You are granted " + hours + (hours > 1 ? "hours" : "hour") + " worth of bonus experience.");
			}
		}
	}
	
	private void doExpReward() {
		int experience = player.getDominionTower().getDominionFactor();
		if (player.getPerkManager().hasPerkActive(DonationPerk.DOMINION_DOMINATION))
			experience = (int) (experience + (experience * 0.25));
		
		player.getDominionTower().dominionFactor = 0;
		
		player.getSkills().addSkillXpRefresh(skill, experience);
		player.getSkills().refresh(skill);
		player.sendMessage("You are granted " + Utils.formatNumber(experience) + " experience in the " + player.getSkills().getSkillName(skill) + " skill.");
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		
	}

}
