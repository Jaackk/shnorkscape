package com.rs.game.activites.quest.deathsbounty;

import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

public final class DeathD extends Dialogue {

    @Override
    public void start() {
        int questStage = (int) parameters[0];
        if (questStage == -1) {
            sendNPCDialogue(14386, CROOKED_HEAD, "Oh dear... there's something terribly wrong.",
                    "I've noticed strange energy surrounding Ataraxia for the past while...");
            stage = 1;
        } else if (questStage == 3) {
            sendPlayerDialogue(NORMAL, "I've done it! I've found the lost soul and returned it to you!");
            stage = -1;
        } else if (questStage == 0) {
            if (player.getInventory().containsItem(DeathsBounty.SOUL_URN_ID, 1)) {
                sendPlayerDialogue(NORMAL, "I think I've found it...");
                stage = 5;
            } else {
                sendNPCDialogue(14386, CROOKED_HEAD,
                        "Talk to me when you've found the " + Colors.DARK_RED + "Soul urn</col>.");
                stage = 0;
            }
        } else {
            throw new IllegalStateException("invalid quest stage(" + stage + ")");
        }
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            // COMPLETED THE QUEST (3)
            case -2:
                player.quests.advanceStage(DeathsBounty.class);
                break;
            case -1:
                sendNPCDialogue(14386, CROOKED_HEAD,
                        "Well done "+ player.getDisplayName()+", very good. Take this as compensation for your time...");
                stage = -2;
                break;

            // END DIALOGUE
            case 0:
                end();
                break;

            // HAS NOT STARTED QUEST (-1)
            case 1:
                sendOptionsDialogue("Select an option.",
                        "How can I help?",
                        "Sorry, don't really care.");
                stage = 2;
                break;
            case 2:
                if (componentId == OPTION_1) {
                    sendNPCDialogue(14386, CROOKED_HEAD,
                            "The energy is most likely a soul in limbo... a soul that has cheated death.",
                            "If true, their " + Colors.DARK_RED + "Soul urn</col> might be within divine locations...");
                    stage = 3;
                } else if (componentId == OPTION_2) {
                    end();
                }
                break;
            case 3:
                sendNPCDialogue(14386, CROOKED_HEAD,
                        "You should be able to retrieve the urn by gathering from them.",
                        "Let me know when you find it.");
                stage = 4;
                player.quests.advanceStage(DeathsBounty.class);
                break;
            case 4:
                sendPlayerDialogue(NORMAL, "Okay, I'll try my best!");
                stage = 0;
                break;

            // FOUND SOUL URN (0)
            case 5:
                sendNPCDialogue(14386, CROOKED_HEAD,
                        "Splendid! Let me take a look...");
                stage = 6;
                break;
            case 6:
                sendDialogue("Death channels his vision through your eyes.");
                stage = 7;
                break;
            case 7:
                sendPlayerDialogue(NORMAL, "Dude... this feels weird...");
                stage = 8;
                break;
            case 8:
                sendNPCDialogue(14386, CROOKED_HEAD,
                        "Hmm... there's a name imprinted on the urn, and I can't seem to read it.",
                        "I need you to take this to Guthix for me. He'll be able to figure it out.");
                stage = 9;
                break;
            case 9:
                sendPlayerDialogue(ANGRY, "Why don't you do it?");
                stage = 10;
                break;
            case 10:
                sendNPCDialogue(14386, CROOKED_HEAD, "I have dead people to collect and tax. These Ataraxians sure die a lot...");
                stage = 11;
                break;
            case 11:
                sendPlayerDialogue(ANGRY, "*SIGH* Fine. Where can I find Guthix?");
                stage = 12;
                break;
            case 12:
                sendNPCDialogue(14386, CROOKED_HEAD,
                        "You can find him in " + Colors.DARK_RED + "Guthix's cave</col>. Good luck.");
                player.quests.advanceStage(DeathsBounty.class);
                stage = 0;
                break;
        }
    }

    @Override
    public void finish() {

    }
}
