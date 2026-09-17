package com.rs.game.player.dialogue.impl;

import com.rs.game.item.Item;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.content.SlayerTask;
import com.rs.game.player.content.SlayerTask.Master;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.InputStringEvent;

/**
 * Get-Task option for Kuradal.
 *
 * @author Noel
 */
public class KuradalGetTask extends Dialogue {

    public static final long PERSLAYSION_PERK_THROTTLE = 10;

    int npcId;

    @Override
    public void start() {
        npcId = (Integer) parameters[0];
        if (!player.hasItem(new Item(4155))) {
            sendNPCDialogue(npcId, 9827, "It seems you don't have a Slayer gem.");
            stage = 80;
        } else if (player.getTask() == null) {
            if (player.getPerkManager().hasPerkActive(DonationPerk.PERSLAYSION)) {
                sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE,
                        "Can I choose my assignment?",
                        "Can you pick an assignment for me?",
                        "Show me all the tasks I can choose from!");
            } else {
                sendPlayerDialogue(9827, "I need another assignment.");
            }
            stage = -1;
        } else {
            if (!player.getPerkManager().hasPerkActive(DonationPerk.PERSLAYSION)) {
                sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE,
                        "Re-ask for a new task (will cost 10 slayer points)",
                        "Ask current task");
            } else {
                sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE,
                        "Quit current task (will cost no slayer points)",
                        "Ask current task");
            }
            stage = -1;
        }

    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -2:
            case 100:
                end();
                break;
            case 80:
                sendItemDialogue(4155, 1, "You have received a Slayer gem from the Slayer Master.");
                player.addItem(new Item(4155));
                stage = 81;
                break;
            case 81:
                if (player.getTask() == null) {
                    if (player.getPerkManager().hasPerkActive(DonationPerk.PERSLAYSION)) {
                        SlayerTask.SlayerTaskData lastChosen = Master.KURADAL_TASK_IDS.get(player.lastTaskId);
                        String lastChosenStr = lastChosen != null ? "(Last: " + lastChosen.name + ")" : "";
                        sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE,
                                "Can I choose my assignment? " + lastChosenStr,
                                "Can you pick an assignment for me?",
                                "Show me all the tasks I can choose from!");
                    } else {
                        sendPlayerDialogue(9827, "I need another assignment.");
                    }
                } else {
                    sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Re-ask for a new task", "Ask current task");
                }
                stage = -1;
                break;
            case -1:
                if (player.getTask() == null) {
                    if (player.getPerkManager().hasPerkActive(DonationPerk.PERSLAYSION)) {
                        switch (componentId) {
                            case OPTION_1:
                                long minutesPassed = player.pickTaskThrottle.elapsed().toMinutes();
                                if (player.pickTaskThrottle.isRunning() && minutesPassed < PERSLAYSION_PERK_THROTTLE) {
                                    long minutesLeft = PERSLAYSION_PERK_THROTTLE - minutesPassed;
                                    String minsStr = minutesLeft > 0 ? minutesLeft + " minutes" : "a few seconds";
                                    sendNPCDialogue(npcId, 9827, "Please wait "+minsStr+" before choosing your own task.");
                                    stage = -2;
                                } else {
                                    enterOption();
                                }
                                break;
                            case OPTION_2:
                                assignAssignment();
                                break;
                            case OPTION_3:
                                SlayerTask.displayOptions(player);
                                break;
                        }
                    } else {
                        assignAssignment();
                    }
                } else {
                    switch (componentId) {
                        case OPTION_1:
                            if (!player.getPerkManager().hasPerkActive(DonationPerk.PERSLAYSION)) {
                                if (player.getSlayerPoints() >= 10) {
                                    player.setSlayerPoints(player.getSlayerPoints() - 10);
                                } else {
                                    sendNPCDialogue(npcId, 9827, "You don't have enough points for this!");
                                    stage = -2;
                                    return;
                                }
                                player.setTask(null);
                                assignAssignment();
                            } else {
                                player.setTask(null);
                                sendNPCDialogue(npcId, 9827, "Done!");
                                stage = 100;
                                return;
                            }
                            break;
                        case OPTION_2:
                            sendNPCDialogue(npcId, 9827, "You're still hunting " + player.getTask().getName(player).toLowerCase() + "s; come back when you've finished your task.");
                            stage = 100;
                            break;
                    }
                }
                break;
        }
    }

    private void assignAssignment() {
        SlayerTask.random(player, Master.KURADAL, false);
        if (player.getTask() != null)
            sendNPCDialogue(npcId, 9827, "Excellent, you're doing great. Your new task is to kill " + player.getTask().getTaskAmount() + " " + player.getTask().getName(player).toLowerCase() + "s.");
        else {
            end();
            return;
        }
        stage = 100;
    }

    @Override
    public void finish() {
    }

    private void assignTask(SlayerTask.SlayerTaskData task) {
        SlayerTask.assignTask(player, Master.KURADAL, false, task.taskId, null);
        if (player.getTask() != null) {
            sendNPCDialogue(npcId, 9827, "Excellent, you're doing great. Your new task is to kill " + player.getTask().getTaskAmount() + " " + player.getTask().getName(player).toLowerCase() + "s.");
        } else {
            end();
            return;
        }
        stage = 100;
    }

    private void enterOption() {
        player.sendInputString("Enter the slayer monster's name", new InputStringEvent() {
            @Override
            public void run(Player player) {
                String key = getString().toLowerCase().trim();
                SlayerTask.SlayerTaskData data = Master.KURADAL_KEYS.get(key);

                if (data == null) {
                    sendNPCDialogue(npcId, 9827, "No slayer monster named " + Colors.RED + key + "</col> was found!");
                    stage = -2;
                } else if (player.lastTaskId == data.taskId) {
                    sendNPCDialogue(npcId, 9827, "You cannot pick the same task twice in a row!");
                    stage = -2;
                } else {
                    player.pickTaskThrottle.reset().start();
                    player.lastTaskId = data.taskId;
                    assignTask(data);
                }
            }
        });
    }
}