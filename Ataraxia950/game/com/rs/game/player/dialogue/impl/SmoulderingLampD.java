package com.rs.game.player.dialogue.impl;

import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.InputStringEvent;
import com.rs.utils.Utils;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class SmoulderingLampD extends Dialogue {

    Skills skills;
    int level;
    double redeemXp;
    double rawConvertXp;
    double convertXp;
    String formattedRedeem;
    String formattedConvert;
    int lampId;

    @Override
    public void start() {
        skills = player.getSkills();
        level = (int) parameters[0];
        rawConvertXp = skills.getRawPrismaticLampXp(level);
        convertXp = skills.getPrismaticLampXp(level);
        redeemXp = convertXp * 1.15;
        lampId = getLampForLevel();

        double prismaticXp = skills.getBonusPrismaticXp();
        if (prismaticXp > 0) {
            if (convertXp > prismaticXp) {
                convertXp = prismaticXp;
            }
            formattedConvert = Utils.formatNumber((long) convertXp);
            sendDialogue("Would you like to instantly convert " + Colors.RED + formattedConvert + "</col> prismatic XP into real XP?");
            stage = 0;
        } else {
            formattedRedeem = Utils.formatNumber((long) redeemXp);
            sendDialogue("You do not have any prismatic XP to convert. Would you like to redeem this lamp for " + Colors.RED + formattedRedeem + "</col> prismatic XP instead?");
            stage = -2;
        }
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -3:
                end();
                break;
            case -2:
                sendOptionsDialogue("Select an option.", "Yes", "No");
                stage = -1;
                break;
            case -1:
                if (componentId == OPTION_1 && player.getInventory().containsItem(lampId, 1)) {
                    player.getInventory().deleteItem(lampId, 1);
                    player.getSkills().addBonusPrismaticXp(redeemXp);
                    player.sendMessage("You have redeemed your smouldering lamp for " + Colors.RED + formattedRedeem + "</col> prismatic XP.");
                }
                end();
                break;
            case 0:
                sendOptionsDialogue("Select an option.", "Yes", "No");
                stage = 1;
                break;
            case 1:
                if (componentId == OPTION_1) {
                    player.sendInputString("Enter the name of the skill", new InputStringEvent() {
                        @Override
                        public void run(Player player) {
                            String skillName = getString().toLowerCase().trim();
                            Integer skill = Skills.getSkill(skillName);
                            if (skill == null) {
                                sendDialogue("Skill '" + skillName + "' not found. Please write the full name of the skill.");
                                stage = -3;
                            } else if (Skills.isCombatSkill(skill)) {
                                sendDialogue("The skill must be a non-combat skill.");
                                stage = -3;
                            } else if (player.getInventory().containsItem(lampId, 1)) {
                                player.getInventory().deleteItem(lampId, 1);
                                player.getSkills().addBonusPrismaticXp(-convertXp);
                                player.getSkills().silentAddXp(skill, rawConvertXp);
                                player.sendMessage("You have used the smouldering lamp to convert " + Colors.RED + formattedConvert + "</col> prismatic XP into real XP.");
                                end();
                            }
                        }
                    });
                } else {
                    end();
                }
                break;
        }
    }

    @Override
    public void finish() {

    }

    private int getLampForLevel() {
        switch (level) {
            case 0:
                return 33527;
            case 1:
                return 33528;
            case 2:
                return 33529;
            case 3:
                return 33530;
        }
        throw new IllegalStateException("invalid level " + level);
    }
}