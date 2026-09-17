package com.rs.game.activities.seasonalevents.christmas;

import com.rs.cores.CoresManager;
import com.rs.game.activities.seasonalevents.SeasonalEventManager;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.JsonSerializable;

import static com.rs.game.activities.seasonalevents.christmas.ChristmasSeasonalEvent.READ_INTRODUCTION_PATH;
import static com.rs.game.activities.seasonalevents.christmas.ChristmasSeasonalEvent.SANTA_NPC;
import static com.rs.game.activities.seasonalevents.christmas.ChristmasSeasonalEvent.readIntroduction;

/**
 * @author lare96
 */
public final class SantaDialogue extends Dialogue {

    @Override
    public void start() {
        if (!SeasonalEventManager.isActive(ChristmasSeasonalEvent.class)) {
            Dialogue.sendSingleNPCDialogue(player, SANTA_NPC, NORMAL, "I can't talk right now! I have to get back to my factory...");
            return;
        }
        if (readIntroduction.contains(player.getUsername())) {
            sendOptionsDialogue("Select an option.",
                    "What should I be doing again?",
                    "I found some presents!");
            stage = -1;
        } else {
            sendNPCDialogue(SANTA_NPC, NORMAL, "This is terrible, just terrible!",
                    "Christmas is ruined and it's all thanks to those blasted Khazard troops and gnomes with their constant fighting.",
                    "What ever will I do?!");
            stage = 1;
        }
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -1:
                if (componentId == OPTION_1) {
                    sendPlayerDialogue(HAPPY, "Happy holidays Santa! What am I supposed to be doing again?");
                    stage = 6;
                } else if (componentId == OPTION_2) {
                    end();
                    ChristmasSeasonalEvent.giveSantaPresents(player);
                }
                break;
            case 0:
                end();
                break;
            case 1:
                sendPlayerDialogue(NORMAL, "Excuse me mister, is something wrong?");
                stage = 2;
                break;
            case 2:
                sendNPCDialogue(SANTA_NPC, NORMAL, "Wait, are you the hero that King Jaedmo IV's adviser told me he'd send?",
                        "What was that advisor's name... err.. Kirita!");
                stage = 3;
                break;
            case 3:
                sendOptionsDialogue("Select an option.",
                        "Yes",
                        "No");
                stage = 4;
                break;
            case 4:
                if (componentId == OPTION_1) {
                    sendNPCDialogue(SANTA_NPC, NORMAL, "I didn't think he'd actually get around to it!",
                            "This is wonderful, there's still hope!",
                            "You must help me save Christmas for the sake of all Ataraxians.");
                    stage = 5;
                } else if (componentId == OPTION_2) {
                    sendNPCDialogue(SANTA_NPC, NORMAL, "Ahh, carry on then. If you see them, tell them to come here.");
                    stage = 0;
                }
                break;
            case 5:
                sendPlayerDialogue(NORMAL, "How can I help?");
                readIntroduction.add(player.getUsername());
                CoresManager.getServiceProvider().executeNow(() -> JsonSerializable.save(READ_INTRODUCTION_PATH, readIntroduction));
                stage = 6;
                break;
            case 6:
                sendNPCDialogue(SANTA_NPC, NORMAL, "The gnome and the Khazard troops shot my slay out of the sky, scattering presents all throughout Ataraxia.",
                        "I have my reindeer looking for them but they're not very intelligent you see.");
                stage = 7;
                break;
            case 7:
                sendPlayerDialogue(NORMAL, "Yeah, hard to pick up presents without thumbs I'm sure.");
                stage = 8;
                break;
            case 8:
                sendNPCDialogue(SANTA_NPC, NORMAL, "Err... exactly! That's why I need you to help them retrieve the presents.",
                        "They will tell me their locations when they find presents, and I'll update the list beside me with them.");
                stage = 9;
                break;
            case 9:
                sendNPCDialogue(SANTA_NPC, NORMAL, "You may also find presents while doing your daily errands, those should be returned as well.",
                        "I'll be keeping track of how many you bring back to me!");
                stage = 10;
                break;
            case 10:
                sendPlayerDialogue(GLANCE_DOWN, "What's stopping me from just stealing them all?");
                stage = 11;
                break;
            case 11:
                sendNPCDialogue(SANTA_NPC, NORMAL, "... Good faith and your love for Christmas?");
                stage = 12;
                break;
            case 12:
                sendPlayerDialogue(NORMAL, "Uh... Yeah sure, I'll help.");
                stage = 13;
                break;
            case 13:
                if (!player.hasItem(47592)) {
                    sendNPCDialogue(SANTA_NPC, HAPPY, "Great to hear! This should help you on your journeys. Happy holidays!");
                    player.addItem(47592, 1);
                    stage = 14;
                } else {
                    sendNPCDialogue(SANTA_NPC, HAPPY, "Great to hear! Happy holidays!");
                    stage = 0;
                }
                break;
            case 14:
                sendItemDialogue(47592, 1, "He hands you a large sack that can be used to store presents.");
                stage = 0;
                break;
        }
    }

    @Override
    public void finish() {
    }
}