package com.rs.game.activites.gim.guide;

import com.rs.game.activites.gim.GIM;
import com.rs.game.activites.gim.GIMGroup;
import com.rs.game.activites.gim.event.DActiveEvents;
import com.rs.game.player.dialogue.Dialogue;

/**
 * The main GIM guide dialogue.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class DGIMGuide extends Dialogue {
    private boolean isLeader;
    private boolean leaveRequest;

    @Override
    public void start() {
        if (player.isUnregisteredGIM()) {
            player.getDialogueManager().startDialogue(new DUnregisteredGIM());
            return;
        }

        if (player.isGroupIronman()) {
            GIMGroup group = GIM.getGroupData().get(player.gimKey.getGroupKey());
            if (group == null) {
                sendNPCDialogue(12320, NORMAL, "There was an issue fetching your group data. Please try again in a few minutes.");
                stage = -1;
                return;
            }
            isLeader = group.getLeaderName().equals(player.getUsername());
            if (isLeader) {
                sendOptionsDialogue("Select an option.",
                        "How does Group Ironman work?",
                        "View events",
                        "View group stats",
                        "View rewards",
                        "View settings");
            } else {
                leaveRequest = GIM.getLeaveRequests().containsEntry(group.getGroupKey(), player.getUsername());
                sendOptionsDialogue("Select an option.",
                        "How does Group Ironman work?",
                        "View events",
                        "View group stats",
                        "View rewards",
                        leaveRequest ? "I don't want to leave my group anymore." : "Can I leave my group?");
            }
            stage = 0;
        } else {
            sendPlayerDialogue(NORMAL, "Who are you? What is Group Ironman?");
            stage = -2;
        }
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -3:
                player.getDialogueManager().startDialogue(new DGIMIntro(false));
                break;
            case -2:
                sendNPCDialogue(12320, NORMAL, "I am the GIM guide. I manage rewards, events, and settings for Group Ironmen.");
                stage = -3;
                break;
            case -1:
                end();
                break;
            case 0:
                if (componentId == OPTION_1) {
                    player.getDialogueManager().startDialogue(new DGIMIntro(true));
                } else if (componentId == OPTION_2) {
                    player.getDialogueManager().startDialogue(new DActiveEvents());
                } else if (componentId == OPTION_3) {
                    GIM.getHighscores().displayGroup(player, player.gimKey.getGroupKey());
                } else if (componentId == OPTION_4) {
                    GIM.getRewards().claim(player);
                } else if (componentId == OPTION_5) {
                    if (isLeader) {
                        player.getDialogueManager().startDialogue(new DGroupSettings());
                    } else {
                        if (leaveRequest) {
                            GIM.getLeaveRequests().remove(player.gimKey.getGroupKey(), player.getUsername());
                            sendNPCDialogue(12320, NORMAL, "Okay, I have cancelled your leave request.");
                            stage = -1;
                            return;
                        }
                        sendNPCDialogue(12320, NORMAL,
                                "Are you absolutely sure you want to do this? It must be verified by a staff member.",
                                "Once its accepted, all your progress towards this season will be lost.",
                                "The request will expire when the server goes offline.");
                        stage = 1;
                    }
                }
                break;
            case 1:
                sendOptionsDialogue("Select an option.", "Yes", "No");
                stage = 2;
                break;
            case 2:
                if (componentId == OPTION_1) {
                    GIM.getLeaveRequests().put(player.gimKey.getGroupKey(), player.getUsername());
                    sendNPCDialogue(12320, NORMAL, "Okay, I have submitted a leave request for you. Now just ask any staff member to verify it.");
                    stage = -1;
                } else if (componentId == OPTION_2) {
                    end();
                }
                break;
        }
    }

    @Override
    public void finish() {

    }
}
