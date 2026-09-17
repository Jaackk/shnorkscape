package com.rs.game.activites.quest.deathsbounty;

import com.rs.game.map.bossInstance.BossInstance;
import com.rs.game.map.bossInstance.BossInstanceHandler;
import com.rs.game.map.bossInstance.InstanceSettings;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public final class GuthixD extends Dialogue {

    @Override
    public void start() {
        int questStage = player.quests.getCurrentStage(DeathsBounty.class);
        if (questStage == 1) {
            sendPlayerDialogue(NORMAL, "Death told me to come see you. I was wondering if you could read the name on this urn.");
            stage = 1;
        } else if (questStage == 2) {
            sendNPCDialogue(16971, NORMAL,
                    "Are you ready to fight Husband Mich?",
                    "Remember, he uses magic attacks so prepare accordingly.");
            stage = -1;
        } else if (questStage == 3) {
            sendNPCDialogue(16971, NORMAL,
                    "I saw your fight... great job!",
                    "You should go tell Death the good news.");
            stage = 0;
        } else if (player.quests.isCompleted(DeathsBounty.class)) {
            LocalDateTime now = LocalDateTime.now();
            if (player.lastDeathSoul != null && now.isBefore(player.lastDeathSoul)) {
                long hoursBetween = now.until(player.lastDeathSoul, ChronoUnit.HOURS);
                if (hoursBetween > 0) {
                    sendNPCDialogue(16971, NORMAL, "Your 10% damage boost while on reaper tasks is active for another "+hoursBetween+" hour(s).");
                } else {
                    sendNPCDialogue(16971, NORMAL, "Your 10% damage boost while on reaper tasks will go inactive soon!");
                }
                stage = 0;
                return;
            }
            sendNPCDialogue(16971, NORMAL, "Would you like to fight another soul?",
                    "Death will reward you with an extra 10% damage boost on reaper tasks for 24h.");
            stage = -1;
        }
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -2:
                if (componentId == OPTION_1) {
                    InstanceSettings settings = new InstanceSettings(BossInstanceHandler.Boss.Husband_Mich);
                    settings.setMaxPlayers(1);
                    settings.setProtection(BossInstance.FFA);
                    settings.setCreationTime(Utils.currentTimeMillis());
                    BossInstanceHandler.createInstance(player, settings);
                } else if (componentId == OPTION_2) {
                    end();
                }
                break;
            case -1:
                sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE,
                        "Yes, I'm ready!",
                        "No.");
                stage = -2;
                break;
            case 0:
                end();
                break;
            case 1:
                if (player.getInventory().containsItem(DeathsBounty.SOUL_URN_ID, 1)) {
                    sendNPCDialogue(16971, NORMAL,
                            "Ah yes, I can read this.",
                            "Give it here.");
                    stage = 2;
                } else {
                    sendNPCDialogue(16971, ANGRY, "I don't see an urn in your inventory!");
                    stage = 0;
                }
                break;
            case 2:
                sendDialogue("Guthix examines the urn with a stern glare.");
                stage = 3;
                break;
            case 3:
                sendNPCDialogue(16971, NORMAL, "The name on the urn is... " + Colors.DARK_RED + "Husband Mich</col>!",
                        "It seems that he created the urn to intentionally avoid Death.");
                stage = 4;
                break;
            case 4:
                sendPlayerDialogue(NORMAL, "Surely we can stop him somehow?");
                stage = 5;
                break;
            case 5:
                sendNPCDialogue(16971, NORMAL, "Yes... I will have to transport you inside the urn.",
                        "Once inside, you must kill his soul and return it to Death.");
                stage = 6;
                break;
            case 6:
                sendPlayerDialogue(NORMAL, "Sounds dangerous... is there anything I should know before fighting him?");
                stage = 7;
                break;
            case 7:
                sendNPCDialogue(16971, NORMAL, "Well, aside from the fact that he's very skilled with magic...");
                stage = 8;
                break;
            case 8:
                sendNPCDialogue(16971, NORMAL, "The urn has been bound to his soul.",
                        "He has a lot of control over the physics of his realm... don't be surprised if you see unusual things.");
                stage = 9;
                break;
            case 9:
                if (player.getInventory().containsItem(DeathsBounty.SOUL_URN_ID, 1)) {
                    sendNPCDialogue(16971, NORMAL,
                            "I'll take the urn from you now so that I can channel the energy from it.",
                            "Let me know when you want to fight Husband Mich.");
                    player.getInventory().deleteItem(DeathsBounty.SOUL_URN_ID, 1);
                    player.quests.advanceStage(DeathsBounty.class);
                } else {
                    sendNPCDialogue(16971, ANGRY, "I don't see an urn in your inventory!");
                }
                stage = 0;
                break;
        }
    }

    @Override
    public void finish() {

    }
}
