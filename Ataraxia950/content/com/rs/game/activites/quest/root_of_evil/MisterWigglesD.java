package com.rs.game.activites.quest.root_of_evil;

import com.rs.game.MapInstance;
import com.rs.game.player.Skills;
import com.rs.game.player.content.FadingScreen;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import lombok.val;

public final class MisterWigglesD extends Dialogue {

    private int currentStage;

    @Override
    public void start() {
        currentStage = player.quests.getCurrentStage(RootOfEvil.class);
        if (currentStage == 1) {
            sendNPCDialogue(2371, ANGRY, "Who are you and why are you up here!?");
            stage = 0;
        } else if (currentStage == 2) {
            sendNPCDialogue(2371, NORMAL, "Can you guess what Bryan the Great did?");
            stage = 0;
        } else if (currentStage == 3) {
            sendNPCDialogue(2371, NORMAL, "Come back when you have a suspect.");
        } else if (currentStage == 4) {
            sendPlayerDialogue(CALM, "I've questioned everyone at ;;home. The lottery coordinator seems incredibly suspicious, but he won't tell me anything.");
            stage = 0;
        } else if (player.quests.isCompleted(RootOfEvil.class)) {
            sendNPCDialogue(2371, HAPPY, "I heard what you did. I'm very proud of you, " + player.getDisplayName() + ".F");
        } else {
            sendNPCDialogue(2371, ANGRY, "Get out of here, right now!");
        }
    }

    @Override
    public void run(int interfaceId, int componentId) {
        if (currentStage == 1) {
            switch (stage) {
                case -1:
                    end();
                    break;
                case 0:
                    sendPlayerDialogue(NORMAL,
                            "I'm " + player.getDisplayName() + ", and the Evil tree hunter sent me.",
                            "There have been a large amount of Evil Tree sightings as of late.");
                    stage++;
                    break;
                case 1:
                    sendNPCDialogue(2371, SAD, "As I predicted...");
                    stage++;
                    break;
                case 2:
                    sendPlayerDialogue(NORMAL,
                            "I also found a strange Evil root when looting its carcass.",
                            "Do you have any idea of what it could be?");
                    stage++;
                    break;
                case 3:
                    if (player.getInventory().containsItem(9919, 1)) {
                        String gender = player.getAppearence().isMale() ? "boy" : "girl";
                        sendNPCDialogue(2371, NORMAL, "Yes. Listen closely, " + gender + ".");
                        stage++;
                    } else {
                        sendNPCDialogue(2371, ANGRY,
                                "Where is this root you speak of? I don't see it.");
                        stage = -1;
                    }
                    break;
                case 4:
                    sendNPCDialogue(2371, SAD, "Centuries ago, King Jaedmo I ruled Ataraxia with Saradomin's guidance.",
                            "Zamorak worshippers hated this, and tried to incite their God's violence and chaos over the lands...");
                    stage++;
                    break;
                case 5:
                    sendPlayerDialogue(NORMAL, "Damn, that sucks.");
                    stage++;
                    break;
                case 6:
                    sendNPCDialogue(2371, SAD, "Indeed it does. A civil war broke out shortly after... but this is where things get interesting");
                    stage++;
                    break;
                case 7:
                    player.getInterfaceManager().closeChatBoxInterface();
                    player.lock();
                    FadingScreen.fade(player, 750, () -> {
                        val instance = new MapInstance(312, 403, 1, 1);
                        instance.load(() -> {
                            player.getAppearence().switchHidden();
                            player.setNextWorldTile(instance.getInstanceTile(2500, 3225, 0));
                            WorldTasksManager.schedule(new WorldTask() {
                                @Override
                                public void run() {
                                    player.getCutscenesManager().play(new MisterWigglesCutscene(instance));
                                }
                            }, 2);
                        });
                    });
                    break;
            }
        } else if (currentStage == 2) {
            switch (stage) {
                case 0:
                    sendPlayerDialogue(NORMAL, "Err.. no... what did he do?");
                    stage++;
                    break;
                case 1:
                    sendNPCDialogue(2371, NORMAL, "He did not kill them. He merely stored their life essence in magic roots.",
                            "This greatly weakened them, effectively turning their spirits into that of a tree.");
                    stage++;
                    break;
                case 2:
                    sendNPCDialogue(2371, NORMAL, "But someone... someone with a lot of wealth is trying to set them free...",
                            "These 'Evil trees' are a deliberate attack on Ataraxians!");
                    stage++;
                    break;
                case 3:
                    sendPlayerDialogue(NORMAL, "Huh?? What does wealth have to do with this?");
                    stage++;
                    break;
                case 4:
                    sendNPCDialogue(2371, NORMAL, "Someone is using the power within the roots to create Evil Tree avatars.",
                            "Who would be rich enough to get their hands on these sacred objects?",
                            "The magical equipment needed to create avatars costs a fortune in itself!");
                    stage++;
                    break;
                case 5:
                    sendNPCDialogue(2371, CONFUSED, "I haven't seen the outside world in decades...",
                            player.getDisplayName() + ", you must have some idea of who could do this?");
                    stage++;
                    break;
                case 6:
                    sendPlayerDialogue(UNSURE, "Well, I don't really.",
                            "I'll do some questioning around ;;home and see if I find anything suspicious.");
                    stage++;
                    break;
                case 7:
                    sendNPCDialogue(2371, CONFUSED, "Come back here if you find out anything.");
                    player.quests.advanceStage(RootOfEvil.class);
                    stage++;
                    break;
                case 8:
                    end();
                    break;
            }
        } else if (currentStage == 4) {
            switch (stage) {
                case -1:
                    end();
                    break;
                case 0:
                    sendNPCDialogue(2371, NORMAL, "That's not a problem at all. I just happen to know a truth serum recipe...");
                    stage++;
                    break;
                case 1:
                    int currentLevel = player.getSkills().getLevelForXp(Skills.HERBLORE);
                    if (currentLevel >= RootOfEvil.HERBLORE_REQ) {
                        sendNPCDialogue(2371, NORMAL, "You seem to have adequate enough skills to use it.",
                                "The recipe is simple: Use one Ranarr weed on a vial of water.",
                                "Then add one Evil Dust to the unfinished mixture to create the serum.");
                        stage++;
                    } else {
                        int diff = RootOfEvil.HERBLORE_REQ - currentLevel;
                        sendNPCDialogue(2371, NORMAL, "But you don't seem to be skilled enough to use it.",
                                "Come back when you've gained " + diff + " more Herblore levels.");
                        stage = -1;
                    }
                    break;
                case 2:
                    sendPlayerDialogue(NORMAL, "Okay. He said he wanted beer... could I drug him with the serum through that?");
                    stage++;
                    break;
                case 3:
                    sendNPCDialogue(2371, NORMAL, "Great idea! Mix it with some beer from Varrock's pub and give it to him.",
                            "From that point on you're on your own. I can't leave this attic so I won't be able to investigate with you.");
                    player.quests.advanceStage(RootOfEvil.class);
                    stage++;
                    break;
                case 4:
                    sendPlayerDialogue(NORMAL, "Understood. Thank you for the help!");
                    stage++;
                    break;
                case 5:
                    sendNPCDialogue(2371, NORMAL, "Good luck.");
                    stage = -1;
                    break;
            }
        } else {
            end();
        }
    }

    @Override
    public void finish() {

    }
}
