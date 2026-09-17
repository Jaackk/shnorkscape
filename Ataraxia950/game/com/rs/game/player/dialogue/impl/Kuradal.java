package com.rs.game.player.dialogue.impl;

import com.rs.game.item.Item;
import com.rs.game.player.PerkManager;
import com.rs.game.player.Player;
import com.rs.game.player.content.SlayerTask;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.InputStringEvent;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.misc.ShopsDataParser;

/**
 * Full Kuradal's Dialogue.
 *
 * @author Noel
 */
public class Kuradal extends Dialogue {

    int npcId;

    @Override
    public void start() {
        npcId = (Integer) parameters[0];
        if (!player.hasItem(new Item(4155))) {
            sendNPCDialogue(npcId, 9827, "It seems you don't have a Slayer gem.");
            stage = 80;
        } else
            sendNPCDialogue(npcId, 9827, "Hello, and what do you ask of me?");
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 80:
                sendItemDialogue(4155, 1, "You have received a Slayer gem from the Slayer Master.");
                player.addItem(new Item(4155));
                stage = 81;
                break;
            case 81:
                sendNPCDialogue(npcId, 9827, "Hello, and what do you ask of me?");
                stage = -1;
                break;
            case -1:
                sendOptionsDialogue("Select an Option", "I need another assignment.",
                        "Can we talk about items and incentives?", "Can I see my boss kill-log?", "Let's talk about you.");
                stage = 0;
                break;
            case 101:
                sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Yes", "No");
                stage = 102;
                break;
            case 102:
                switch (componentId) {
                    case OPTION_1:
                        sendNPCDialogue(npcId, 9827, "Done!");
                        player.setTask(null);
                        stage = 100;
                        break;
                    case OPTION_2:
						end();
                        break;
                }
                break;
            case 0:
                switch (componentId) {
                    case OPTION_1:
                        if (player.getPerkManager().hasPerkActive(PerkManager.DonationPerk.PERSLAYSION)) {
                            if (player.getTask() == null) {
                                sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE,
                                        "Can I choose my assignment?",
                                        "Can you pick an assignment for me?",
                                        "Show me all the tasks I can choose from!");
                            } else {
                                sendNPCDialogue(npcId, 9827, "You're still hunting " + player.getTask().getName(player).toLowerCase()
                                        + "s; Quit this task?");
                                stage = 101;
                                return;
                            }
                        } else {
                            sendPlayerDialogue(9827, "I need another assignment.");
                        }
                        stage = 1;
                        break;
                    case OPTION_2:
                        sendPlayerDialogue(9827, "Can we talk about items and incentives?");
                        stage = 5;
                        break;
                    case OPTION_3:
                        sendPlayerDialogue(9827, "Can I see my boss kill-log?");
                        stage = 17;
                        break;
                    case OPTION_4:
                        sendPlayerDialogue(9827, "Let's talk about you.");
                        stage = 18;
                        break;
                }
                break;
            case 1:
                if (player.getPerkManager().hasPerkActive(PerkManager.DonationPerk.PERSLAYSION)) {
                    switch (componentId) {
                        case OPTION_1:
                            long minutesPassed = player.pickTaskThrottle.elapsed().toMinutes();
                            if (player.pickTaskThrottle.isRunning() && minutesPassed < KuradalGetTask.PERSLAYSION_PERK_THROTTLE) {
                                long minutesLeft = KuradalGetTask.PERSLAYSION_PERK_THROTTLE - minutesPassed;
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
                    if (player.getTask() == null) {
                        SlayerTask.random(player, SlayerTask.Master.KURADAL, false);
                        if (player.getTask() != null)
                            sendNPCDialogue(npcId, 9827,
                                    "Excellent, you're doing great. Your new task is to kill "
                                            + player.getTask().getTaskAmount() + " "
                                            + player.getTask().getName(player).toLowerCase() + "s.");
                        else {
                            end();
                            break;
                        }
                        stage = 2;
                    } else {
                        sendNPCDialogue(npcId, 9827, "You're still hunting " + player.getTask().getName(player).toLowerCase()
                                + "s; " + "come back when you've finished your task.");
                        stage = 100;
                    }
                }
                break;
            case 2:
                sendOptionsDialogue("Select an Option", "Got any tips for me?", "Okay, great!");
                stage = 3;
                break;
            case 3:
                switch (componentId) {
                    case OPTION_1:
                        sendPlayerDialogue(9827, "Got any tips for me?");
                        stage = 4;
                        break;
                    case OPTION_2:
                        sendPlayerDialogue(9827, "Okay, great!");
                        stage = 100;
                        break;
                }
                break;
            case 4:
                sendNPCDialogue(npcId, 9827, "Not at this time, sorry.");
                stage = 100;
                break;
            case 5:
                sendNPCDialogue(npcId, 9827, "Of course, what would you like to discuss: items for sale or incentives?");
                stage = 6;
                break;
            case 6:
                sendOptionsDialogue("Select an Option", "Let's talk items.", "Let's talk incentives.");
                stage = 7;
                break;
            case 7:
                switch (componentId) {
                    case OPTION_1:
                        sendPlayerDialogue(9827, "Let's talk items.");
                        stage = 8;
                        break;
                    case OPTION_2:
                        sendOptionsDialogue("Select an Option", "View rewards",
                                "How do I earn co-op points to spend on rewards?",
                                "Where can I use the co-op food and potion rewards?");
                        stage = 10;
                        break;
                }
                break;
            case 8:
                sendNPCDialogue(npcId, 9827, "I have a wide selection of Slayer equipment - take a look!");
                stage = 9;
                break;
            case 9:
                ShopsDataParser.openShop(player, 5);
                end();
                break;
            case 10:
                switch (componentId) {
                    case OPTION_1:
                        end();
                        SlayerTask.openSlayerShop(player);
                        break;
                    case OPTION_2:
                        sendNPCDialogue(npcId, 9827,
                                "You'll gain co-op reward points if you complete at least half of your contribution to a task with a friend.");
                        stage = 11;
                        break;
                    case OPTION_3:
                        sendNPCDialogue(npcId, 9827,
                                "The rewards can be used in most of the areas we commonly send you to complete your tasks.");
                        stage = 15;
                        break;
                }
                break;
            case 11:
                sendNPCDialogue(npcId, 9827,
                        "That means you need to be in a group for most of the time you're working on your task, but it doesn't matter if your friend beats you to the majority of kill!");
                stage = 12;
                break;
            case 12:
                sendNPCDialogue(npcId, 9827,
                        "You'll still have been in a group for most of your contribution, so you're still entitled to a co-op reward point.");
                stage = 13;
                break;
            case 13:
                sendOptionsDialogue("Select an Options", "View rewards",
                        "Where can I use the co-op food and potion rewards?", "That's all, thanks.");
                stage = 14;
                break;
            case 14:
                switch (componentId) {
                    case OPTION_1:
                        end();
                        SlayerTask.openSlayerShop(player);
                        break;
                    case OPTION_2:
                        sendNPCDialogue(npcId, 9827,
                                "The rewards can be used in most of the areas we commonly send you to complete your tasks.");
                        stage = 15;
                        break;
                    case OPTION_3:
                        sendPlayerDialogue(9827, "That's all, thanks.");
                        stage = 100;
                        break;
                }
                break;
            case 15:
                sendOptionsDialogue("Select an Option", "View rewards", "How do I earn co-op points to spend on rewards?",
                        "That's all, thanks");
                stage = 16;
                break;
            case 16:
                switch (componentId) {
                    case OPTION_1:
                        end();
                        SlayerTask.openSlayerShop(player);
                        break;
                    case OPTION_2:
                        sendNPCDialogue(npcId, 9827,
                                "You'll gain co-op reward points if you complete at least half of your contribution to a task with a friend.");
                        stage = 11;
                        break;
                    case OPTION_3:
                        sendPlayerDialogue(9827, "That's all, thanks.");
                        stage = 100;
                        break;
                }
                break;
            case 17:
                /*
                 * if (player.getSkills().getLevelForXp(Skills.SLAYER) >= 99) {
                 * sendNPCDialogue(npcId, 9827,
                 * "Well you have performed well as a student. I guess you have earned the right to wear a prestigious cape now. That will be 99000 coins, please."
                 * ); stage = 77; } else { sendNPCDialogue(npcId, 9827,
                 * "This is a Slayer's Skillcape. Only a true Slayer master is permitted to wear one. You need more training before you earn that honour."
                 * ); stage = 100; }
                 */
                finish();
                player.getInterfaceManager().sendBossKillLog();
                break;
            case 18:
                sendNPCDialogue(npcId, 9827, "I'm Kuradal, daughter of Duradel. What more would you like to know?");
                stage = 19;
                break;
            case 19:
                sendOptionsDialogue("Select an Option", "Tell me more about your background.", "About your father...",
                        "So what's in this dungeon then?");
                stage = 20;
                break;
            case 20:
                switch (componentId) {
                    case OPTION_1:
                        sendPlayerDialogue(9827, "Tell me more about your background.");
                        stage = 21;
                        break;
                    case OPTION_2:
                        sendPlayerDialogue(9827, "About your father...");
                        stage = 31;
                        break;
                    case OPTION_3:
                        sendPlayerDialogue(9827, "So what's in this dungeon then?");
                        stage = 57;
                        break;
                }
                break;
            case 21:
                sendNPCDialogue(npcId, 9827,
                        "If you so desire. As I said, I am Kuradal, daughter of Duradel - another great master of Slayer.");
                stage = 22;
                break;
            case 22:
                sendNPCDialogue(npcId, 9827,
                        "Unlike the others, though, I ventured out not only to slay, but also to capture.");
                stage = 23;
                break;
            case 23:
                sendNPCDialogue(npcId, 9827,
                        "I've caught many different types of creatures and I hold them within this dungeon, reserved exclusively for my pupils to slay.");
                stage = 24;
                break;
            case 24:
                sendPlayerDialogue(9827, "So slaying runs in the family, then?");
                stage = 25;
                break;
            case 25:
                sendNPCDialogue(npcId, 9827,
                        "Of course. One cannot simply 'grow up' with someone like my father. My childhood was all combat training, taking on the beasts of this land side by side with my father.");
                stage = 26;
                break;
            case 26:
                sendOptionsDialogue("Select an Option", "So who is your mother?",
                        "Sounds like you had a hectic childhood!");
                stage = 27;
                break;
            case 27:
                switch (componentId) {
                    case OPTION_1:
                        sendPlayerDialogue(9827, "So who is your mother?");
                        stage = 28;
                        break;
                    case OPTION_2:
                        sendPlayerDialogue(9827, "Sounds like you had a hectic childhood!");
                        stage = 56;
                        break;
                }
                break;
            case 28:
                sendNPCDialogue(npcId, 9827, "I'd rather not say. Can we talk about something else?");
                stage = 29;
                break;
            case 29:
                sendOptionsDialogue("Select an Option", "About your father...", "That's all for now.");
                stage = 30;
                break;
            case 30:
                switch (componentId) {
                    case OPTION_1:
                        sendPlayerDialogue(9827, "About your father...");
                        stage = 31;
                        break;
                    case OPTION_2:
                        sendPlayerDialogue(9827, "That's all for now.");
                        stage = 55;
                        break;
                }
                break;
            case 31:
                sendPlayerDialogue(9827, "Lucien is dead!");
                stage = 32;
                break;
            case 32:
                sendNPCDialogue(npcId, 9827, "I know. I saw his corpse up at the ritual site.");
                stage = 33;
                break;
            case 33:
                sendPlayerDialogue(9827, "What? You were there? I didn't see you.");
                stage = 34;
                break;
            case 34:
                sendNPCDialogue(npcId, 9827,
                        "Nor I you, but I was there, fighting off Mahjarrat and their minions. None of them were Lucien, though.");
                stage = 35;
                break;
            case 35:
                sendNPCDialogue(npcId, 9827,
                        "I chased one of them into a cave, full of ice elementals called glacors. After killing a couple hundred of those, I managed to escape, but by then it was all over: everyone had gone.");
                stage = 36;
                break;
            case 36:
                sendPlayerDialogue(9827, "You sound angry. I'd have thought you'd be glad he's dead!");
                stage = 37;
                break;
            case 37:
                sendNPCDialogue(npcId, 9827,
                        "I am, but Lucien was my kill! Mine alone! No one had the right to take that from me! No one!");
                stage = 38;
                break;
            case 38:
                sendNPCDialogue(npcId, 9827, "...");
                stage = 39;
                break;
            case 39:
                sendNPCDialogue(npcId, 9827, "How will I honour my father's memory?");
                stage = 40;
                break;
            case 40:
                sendOptionsDialogue("Select an Option", "You honour him every day.",
                        "By helping to defeat what killed Lucien!");
                stage = 41;
                break;
            case 41:
                switch (componentId) {
                    case OPTION_1:
                        sendPlayerDialogue(9827, "You honour him every day.");
                        stage = 42;
                        break;
                    case OPTION_2:
                        sendPlayerDialogue(9827, "By helping to defeat what killed Lucien!");
                        stage = 50;
                        break;
                }
                break;
            case 42:
                sendNPCDialogue(npcId, 9827,
                        "Bah! Any more of that touchy-feely nonsense and I'll be forced to revoke your Slayer license...");
                stage = 43;
                break;
            case 43:
                sendPlayerDialogue(9827, "But, yo-");
                stage = 44;
                break;
            case 44:
                sendNPCDialogue(npcId, 9827, "Hahaha! The look on your face! That cheered me up some. Thanks, "
                        + Utils.formatPlayerNameForDisplay(player.getDisplayName()) + ".");
                stage = 45;
                break;
            case 45:
                sendNPCDialogue(npcId, 9827, "'You honour him every day.'");
                stage = 46;
                break;
            case 46:
                sendPlayerDialogue(9827, "I'm, er, glad I could help.");
                stage = 47;
                break;
            case 47:
                sendNPCDialogue(npcId, 9827, "Now, can I help you with anything else?");
                stage = 48;
                break;
            case 48:
                sendOptionsDialogue("Select an Option", "I need another assignment.",
                        "Can we talk about items and incentives?", "Tell me about your skillcape, please.",
                        "Er...nothing...");
                stage = 49;
                break;
            case 49:
                switch (componentId) {
                    case OPTION_1:
                        if (player.getPerkManager().hasPerkActive(PerkManager.DonationPerk.PERSLAYSION)) {
                            sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE,
                                    "Can I choose my assignment?",
                                    "Can you pick an assignment for me?",
                                    "Show me all the tasks I can choose from!");
                        } else {
                            sendPlayerDialogue(9827, "I need another assignment.");
                        }
                        stage = 1;
                        break;
                    case OPTION_2:
                        sendPlayerDialogue(9827, "Can we talk about items and incentives?");
                        stage = 5;
                        break;
                    case OPTION_3:
                        sendPlayerDialogue(9827, "Tell me about your skillcape, please.");
                        stage = 17;
                        break;
                    case OPTION_4:
                        sendPlayerDialogue(9827, "Er...nothing...");
                        stage = 100;
                        break;
                }
                break;
            case 50:
                sendNPCDialogue(npcId, 9827, "Which was what, exactly?");
                stage = 51;
                break;
            case 51:
                sendPlayerDialogue(9827, "A dragonkin. Three of them, in fact.");
                stage = 52;
                break;
            case 52:
                sendNPCDialogue(npcId, 9827, "Hmm, now that sounds like a challenge worth of a slayer master.");
                stage = 53;
                break;
            case 53:
                sendNPCDialogue(npcId, 9827,
                        "I've got some preparations to make. Collecting those for a dungeon could be tricky...");
                stage = 54;
                break;
            case 54:
                sendPlayerDialogue(9827, "Cellecting them? You're crazy!");
                stage = 47;
                break;
            case 55:
                sendNPCDialogue(npcId, 9827, "Fine. I'll be here if you need me.");
                stage = 100;
                break;
            case 56:
                sendNPCDialogue(npcId, 9827,
                        "You could say that, but I am thankful for it as it made me the person I am today.");
                stage = 29;
                break;
            case 57:
                sendNPCDialogue(npcId, 9827,
                        "Well, I've been all over and managed to capture hellhounds, greater demons, gargoyles, abyssal demons, dark beasts, blue, iron and steel dragons.");
                stage = 58;
                break;
            case 58:
                sendPlayerDialogue(9827, "You caught them single-handedly?");
                stage = 59;
                break;
            case 59:
                sendNPCDialogue(npcId, 9827, "You doubt my skills?");
                stage = 60;
                break;
            case 60:
                sendOptionsDialogue("Select an Option", "I didn't mean anything by it.",
                        "Well, come on. You are just a girl and I'm a tough warrior!");
                stage = 61;
                break;
            case 61:
                switch (componentId) {
                    case OPTION_1:
                        sendPlayerDialogue(9827, "I didn't mean anything by it.");
                        stage = 62;
                        break;
                    case OPTION_2:
                        sendPlayerDialogue(9827, "Well, come on. You are just a girl and I'm a tough warrior!");
                        stage = 75;
                        break;
                }
                break;
            case 62:
                sendNPCDialogue(npcId, 9827, "I'm glad to hear so.");
                stage = 63;
                break;
            case 63:
                sendPlayerDialogue(9827, "So is there anything else that's special about this dungeon?");
                stage = 64;
                break;
            case 64:
                sendNPCDialogue(npcId, 9827,
                        "I'm glad you asked. You might find creatures inside the dungeon drop something called a ferocious ring - they must have foraged them from remains in the area.");
                stage = 65;
                break;
            case 65:
                sendPlayerDialogue(9827, "Well, that's great and all, buy what exactly does it do?");
                stage = 66;
                break;
            case 66:
                sendNPCDialogue(npcId, 9827,
                        "Well, for one this, they are bound to these caves, so you'll find it has teleport charges that will return you to this place if you command it.");
                stage = 67;
                break;
            case 67:
                sendPlayerDialogue(9827, "Sounds good, but is that it?");
                stage = 68;
                break;
            case 68:
                sendNPCDialogue(npcId, 9827,
                        "Oh, no. As I said, it is bound to this area, so you'll find that you do more damage to creatures in the dungeon if you are wearing it. Also, if you happen to be near death in the dungeon, it'll save you in a similar manner to a ring of life,");
                stage = 69;
                break;
            case 69:
                sendNPCDialogue(npcId, 9827, "teleporting you back to the safety of my presence.");
                stage = 70;
                break;
            case 70:
                sendPlayerDialogue(9827,
                        "Even if I'm using ranged or magical combat? And what if I'm not in the dungeon when I die wearing it?");
                stage = 71;
                break;
            case 71:
                sendNPCDialogue(npcId, 9827,
                        "Calm down, one question at a time. First, it is a powerful artefact and will affect all combat styles.");
                stage = 72;
                break;
            case 72:
                sendNPCDialogue(npcId, 9827,
                        "Second, I would be careful about dying outside wearing it. I do not believe it will work anywhere but inside the dungeon here.");
                stage = 73;
                break;
            case 73:
                sendPlayerDialogue(9827, "Thanks for your help.");
                stage = 74;
                break;
            case 74:
                sendNPCDialogue(npcId, 9827, "No problem. Anything else?");
                stage = -1;
                break;
            case 75:
                sendNPCDialogue(npcId, 9827, "Just a girl, am I? Right!");
                stage = 76;
                break;
            case 76:
                // TODO: Knock player out and teleport somewhere randomly.
                end();
                break;
            case 77:
                sendOptionsDialogue("Select an Option", "I've changed my mind, I don't want it.",
                        "Great; I've always wanted one!");
                stage = 78;
                break;
            case 78:
                switch (componentId) {
                    case OPTION_1:
                        sendPlayerDialogue(9827, "I've changed my mind, I don't want it.");
                        stage = 100;
                        break;
                    case OPTION_2:
                        if (player.getInventory().getFreeSlots() < 2) {
                            sendPlayerDialogue(9827, "Sorry, but my inventory is currently full. I'll come back later!");
                            stage = 100;
                            return;
                        }
                        if (player.hasMoney(99000)) {
                            sendPlayerDialogue(9827, "Great; I've always wanted one!");
                            stage = 79;
                        } else {
                            sendPlayerDialogue(9827, "I don't have 99'000 coins.");
                            stage = 100;
                        }
                        break;
                }
                break;
            case 79:
                player.takeMoney(99000);
                player.getInventory().addItem(9787, 1);
                player.getInventory().addItem(9788, 1);
                sendNPCDialogue(npcId, 9827,
                        "Great hunting, " + Utils.formatPlayerNameForDisplay(player.getDisplayName()) + ".");
                stage = 100;
                break;
            case 100:
                end();
                break;
        }
    }

    @Override
    public void finish() {
    }

    private void assignAssignment() {
        SlayerTask.random(player, SlayerTask.Master.KURADAL, false);
        if (player.getTask() != null)
            sendNPCDialogue(npcId, 9827, "Excellent, you're doing great. Your new task is to kill " + player.getTask().getTaskAmount() + " " + player.getTask().getName(player).toLowerCase() + "s.");
        else {
            end();
            return;
        }
        stage = 100;
    }

    private void assignTask(SlayerTask.SlayerTaskData task) {
        SlayerTask.assignTask(player, SlayerTask.Master.KURADAL, false, task.taskId, null);
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
                SlayerTask.SlayerTaskData data = SlayerTask.Master.KURADAL_KEYS.get(key);
                if (data == null) {
                    sendNPCDialogue(npcId, 9827, "No slayer monster named " + Colors.RED + key + "</col> was found!");
                    stage = 100;
                } else if (player.lastTaskId == data.taskId) {
                    sendNPCDialogue(npcId, 9827, "You cannot pick the same task twice in a row!");
                    stage = 100;
                } else {
                    player.pickTaskThrottle.reset().start();
                    player.lastTaskId = data.taskId;
                    assignTask(data);
                }
            }
        });
    }
}