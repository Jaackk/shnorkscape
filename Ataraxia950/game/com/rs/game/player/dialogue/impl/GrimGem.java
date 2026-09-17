package com.rs.game.player.dialogue.impl;

import com.rs.game.activites.quest.deathsbounty.DeathsBounty;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.content.achievementsystem.AchievementList;
import com.rs.game.player.content.contracts.ContractHandler;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.misc.ShopsDataParser;
import lombok.val;

import static com.rs.game.player.content.titles.PlayerTitle.FINAL_BOSS;
import static com.rs.game.player.content.titles.PlayerTitle.INSANE_FINAL_BOSS;
import static com.rs.game.player.content.titles.PlayerTitle.INSANE_REAPER;
import static com.rs.game.player.content.titles.PlayerTitle.Reaper;

/**
 * Grim Gem Dialogue.
 *
 * @author Nate
 * @edited Noel
 */
public class GrimGem extends Dialogue {
    private int questStage;

    @Override
    public void start() {
        questStage = player.quests.getCurrentStage(DeathsBounty.class);
        if (DeathsBounty.activateDeathDialogue(player, questStage)) {
            sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE,
                    "Talk about 'Death's Bounty'.",
                    "Talk about reaper tasks.");
            stage = 0;
        } else {
            sendNPCDialogue(14386, CROOKED_HEAD, "What can I do for you?");
            stage = 1;
        }
    }

    @Override
    public void run(int interfaceId, int componentId) {
        String npcName = "";
        val resetCost = player.getPerkManager().hasPerkActive(DonationPerk.THE_DISCOUNTER) ? 2500000 : 5000000;

        if (player.getContract() != null)
            npcName = ContractHandler.getFormattedContractName(player);

        switch (stage) {
            case 0:
                if (componentId == OPTION_1) {
                    player.getDialogueManager().startDialogue("DeathD", questStage);
                } else if (componentId == OPTION_2) {
                    sendNPCDialogue(14386, CROOKED_HEAD, "What can I do for you?");
                    stage = 1;
                }
                break;
            case 1:
                sendOptionsDialogue("Death", "I would like a new contract.", "I would like to see your shop.",
                        "I would like to see your titles.");
                stage = 2;
                break;
            case 2:
                if (componentId == OPTION_1) {
                    if (player.getContract() == null) {
                        if (player.isChooseTask()) {
                            end();
                            player.getDialogueManager().startDialogue("ReapersChoiceD", true);
                            return;
                        }
                        ContractHandler.assignPlayerNewContract(player);
                        npcName = ContractHandler.getFormattedContractName(player);
                        sendNPCDialogue(14386, CROOKED_HEAD, "Your new contract is to kill:<br>",
                                "<col=FF0000>" + player.getContract().getKillAmount() + "x " + npcName + "<br>",
                                "Reward: <col=0000FF>" + player.getContract().getRewardAmount() + " Reaper Points");
                        stage = 1;
                        break;
                    } else {
                        sendNPCDialogue(14386, CROOKED_HEAD, "You already have a task, would you like to reset it?",
                                "<br><col=FF0000>" + player.getContract().getKillAmount() + "x " + npcName + "");
                        stage = 3;
                        break;
                    }
                }
                if (componentId == OPTION_2) {
                    sendNPCDialogue(14386, CROOKED_HEAD, "Of course..");
                    stage = 4;
                    break;

                }
                if (componentId == OPTION_3) {
                    if (player.getTotalContract() >= 500 || player.getTotalKills() >= 5000) {
                        sendNPCDialogue(14386, CROOKED_HEAD, "Of course..");
                        stage = 6;
                        break;

                    } else {
                        sendNPCDialogue(14386, CROOKED_HEAD, "You are not worthy of my titles.",
                                "<br><col=FF0000>" + player.getTotalKills() + "/5000 total kills.",
                                "<br><col=FF0000>" + player.getTotalContract() + "/500 total contracts.");
                        stage = 1;
                        break;
                    }
                }
                if (componentId == OPTION_4) {
                    end();
                    break;
                }
            case 4:
                stage = 8;
                sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Reaper Perks", "Reaper Points Shop");
                break;

            case 3:
                sendOptionsDialogue("Reset current assignment?" + (player.isSkipTask() ? " this will be free thanks to your take two perk." : ""), "Yes", "No");
                stage = 5;
                break;

            case 6:
                sendOptionsDialogue("Death", "<col=8A0808>The Reaper", "<col=8A0808><shad=9D1309>The Insane Reaper",
                        "<col=8A0808>Final Boss", "<col=8A0808><shad=9D1309>Insane Final Boss", "Nevermind");
                stage = 7;
                break;

            case 5:
                if (componentId == OPTION_1) {
                    if (player.getContract() == null) {
                        sendNPCDialogue(14386, CROOKED_HEAD, "You don't have an active contract, ",
                                "collect a new one from me when you're ready.");
                        stage = 1;
                        break;
                    } else {
                        if (player.isSkipTask() || player.hasMoney(resetCost)) {
                            if (!player.isSkipTask())
                                player.takeMoney(resetCost);
                            player.setContract(null);
                            ContractHandler.assignPlayerNewContract(player);
                            npcName = ContractHandler.getFormattedContractName(player);
                            sendNPCDialogue(14386, CROOKED_HEAD, "Your new contract is to kill:<br>",
                                    "<col=FF0000>" + player.getContract().getKillAmount() + "x " + npcName + "<br>",
                                    "Reward: <col=0000FF>" + player.getContract().getRewardAmount() + " Reaper Points");
                            if (!player.isSkipTask() && player.getPerkManager().hasPerkActive(DonationPerk.THE_DISCOUNTER)) {
                                player.sendMessage("The reset cost was halved thanks to your The Discounter perk.");
                            }
                            if (player.isSkipTask())
                                player.setSkipTask(false);
                            stage = 1;
                            break;
                        } else {
                            sendNPCDialogue(14386, CROOKED_HEAD, "Come back when you have more coins.",
                                    "You need at least " + Utils.formatNumber(resetCost) + " coins in order to reset your assignment.");
                            stage = 1;
                            break;
                        }

                    }

                }
                if (componentId == OPTION_2) {
                    sendNPCDialogue(14386, CROOKED_HEAD, "As you wish.");
                    stage = 1;
                    break;

                }

            case 7:
                if (componentId == OPTION_1) {
                    if (player.getTotalContract() >= 500) {
                        setTitle(Reaper.getTitleId());
                        end();
                    } else {
                        sendNPCDialogue(14386, CROOKED_HEAD, "You need to complete 500 contracts to use this title.");
                        stage = 1;
                        break;
                    }
                }
                if (componentId == OPTION_2) {
                    if (player.getTotalContract() >= 1250) {
                        setTitle(INSANE_REAPER.getTitleId());
                        end();
                    } else {
                        sendNPCDialogue(14386, CROOKED_HEAD, "You need to complete 1,250 contracts to use this title.");
                        stage = 1;
                        break;
                    }
                }
                if (componentId == OPTION_3) {
                    if (player.getTotalKills() >= 5000) {
                        setTitle(FINAL_BOSS.getTitleId());
                        player.getAchievements().updateProgress(1, AchievementList.ACHIEVE_FINAL_BOSS_TITLE);
                        end();
                    } else {
                        sendNPCDialogue(14386, CROOKED_HEAD,
                                "You need to complete 5,000 contract kills to use this title.");
                        stage = 1;
                        break;
                    }
                }
                if (componentId == OPTION_4) {
                    if (player.getTotalKills() >= 15000) {
                        setTitle(INSANE_FINAL_BOSS.getTitleId());
                        end();
                    } else {
                        sendNPCDialogue(14386, CROOKED_HEAD,
                                "You need to complete 15,000 contract kills to use this title.");
                        stage = 1;
                        break;
                    }
                }
                if (componentId == OPTION_5) {
                    stage = 1;
                    end();
                    break;
                }
                break;
            case 8:
                end();
                if (componentId == OPTION_1)
                    player.getDialogueManager().startDialogue("ReaperPerksD");
                else
                    ShopsDataParser.openShop(player, 58);
                break;
        }
    }

    private void setTitle(int titleId) {
        player.getAppearence().setTitle(titleId);
        player.getAppearence().generateAppearenceData();
        player.sendMessage("Your title has been successfully changed.");
    }

    @Override
    public void finish() {
    }
}