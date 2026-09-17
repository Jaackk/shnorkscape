package com.rs.game.activites.gim.guide;

import com.rs.cores.CoresManager;
import com.rs.game.activites.gim.GIM;
import com.rs.game.activites.gim.GIMGroup;
import com.rs.game.activites.gim.GIMTransferLeadershipSql;
import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.DisplayNames;
import com.rs.utils.InputStringEvent;
import com.rs.utils.Logger;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * The GIM settings dialogue.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class DGroupSettings extends Dialogue {

    /**
     * The pending join username.
     */
    private String pendingJoin;

    /**
     * The pending kick username.
     */
    private String pendingKick;

    /**
     * The group pending a leadership change.
     */
    private GIMGroup pendingLeadership;

    @Override
    public void start() {
        if (player.isUnregisteredGIM())
            return;
        sendMenu();
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -1:
                sendMenu();
                break;
            case 0:
                if (componentId == OPTION_1) {
                    GIM.renameGroup(player);
                } else if (componentId == OPTION_2) {
                    GIMGroup group = GIM.getGroupData().get(player.gimKey.getGroupKey());
                    if (group == null) {
                        lookupRecord();
                        return;
                    }
                    boolean notGroupLeader = !player.getUsername().equals(group.getLeaderName());
                    if (pendingJoin == null) {
                        // Add new members.
                        if (notGroupLeader) {
                            sendNPCDialogue(12320, NORMAL, "Only the group leader can add new members.");
                            stage = -1;
                            return;
                        }
                        player.getDialogueManager().startDialogue(new DAddGroupMember(group));
                    } else {
                        // Disable join request.
                        if (notGroupLeader) {
                            sendNPCDialogue(12320, NORMAL, "Only the group leader can disable join requests.");
                        } else {
                            sendNPCDialogue(12320, NORMAL, "You have cancelled your join request for '" + pendingJoin + "'.");
                            GIM.getPendingJoinRequests().remove(player.gimKey.getGroupKey());
                        }
                        stage = -1;
                    }
                } else if (componentId == OPTION_3) {
                    GIMGroup group = GIM.getGroupData().get(player.gimKey.getGroupKey());
                    if (group == null) {
                        lookupRecord();
                        return;
                    }
                    boolean notGroupLeader = !player.getUsername().equals(group.getLeaderName());
                    if (pendingKick == null) {
                        // Kick member.
                        if (notGroupLeader) {
                            sendNPCDialogue(12320, NORMAL, "Only the group leader can kick members.");
                            stage = -1;
                            return;
                        }
                        player.getDialogueManager().startDialogue(new DKickGroupMember(group));
                    } else {
                        // Disable kick request.
                        if (notGroupLeader) {
                            sendNPCDialogue(12320, NORMAL, "Only the group leader can disable kick requests.");
                        } else {
                            sendNPCDialogue(12320, NORMAL, "You have cancelled your kick request for '" + pendingKick + "'.");
                            GIM.getKickRequests().remove(group.getGroupKey());
                        }
                        stage = -1;
                    }
                } else if (componentId == OPTION_4) {
                    pendingLeadership = GIM.getGroupData().get(player.gimKey.getGroupKey());
                    if (pendingLeadership == null) {
                        lookupRecord();
                        return;
                    }
                    if (!pendingLeadership.getLeaderName().equals(player.getUsername())) {
                        sendNPCDialogue(12320, NORMAL, "Only the group leader can transfer their leadership.");
                        stage = -1;
                        return;
                    }
                    sendNPCDialogue(12320, NORMAL, "Are you sure you would like to transfer your leadership? This cannot be undone.");
                    stage = 2;
                }
                break;
            case 2:
                sendOptionsDialogue("Select an option.", "Yes", "No");
                stage = 3;
                break;
            case 3:
                if (componentId == OPTION_1) {
                    player.sendInputString("Enter the new leader's name", new InputStringEvent() {
                        @Override
                        public void run(Player player) {
                            String newLeader = DisplayNames.getUsername(getString().toLowerCase().trim());
                            String response = GIM.canTransferLeadership(pendingLeadership, newLeader, false);
                            if (response != null) {
                                sendNPCDialogue(12320, NORMAL, response);
                                stage = -1;
                                return;
                            }
                            sendLeadershipChange(player, pendingLeadership.getGroupId(), newLeader);
                        }
                    });
                } else if (componentId == OPTION_2) {
                    sendMenu();
                }
                break;

        }
    }

    @Override
    public void finish() {

    }

    /**
     * Sends the leadership change request.
     */
    private void sendLeadershipChange(Player player, int groupId, String newLeader) {
        player.lock();
        player.getInterfaceManager().closeChatBoxInterface();
        Dialogue.sendNPCDialogueNoContinue(player, 12320, NORMAL, "Sending leadership transfer request...");
        Future<?> transferTask = CoresManager.getServiceProvider().runNow(new GIMTransferLeadershipSql(groupId, newLeader));
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                if (transferTask.isDone()) {
                    stop();
                    player.unlock();
                    Dialogue.closeNoContinueDialogue(player);
                    boolean failed;
                    try {
                        transferTask.get();
                        failed = false;
                    } catch (InterruptedException | ExecutionException e) {
                        failed = true;
                        Logger.getGlobal().error("Error while kicking player from GIM group!", e);
                    }
                    if (failed) {
                        Dialogue.sendSingleNPCDialogue(player, 12320, NORMAL, "Leadership transfer failed. Please report this message to a staff member.");
                    } else {
                        Dialogue.sendSingleNPCDialogue(player, 12320, NORMAL, "Successfully transferred group leadership to " + Colors.DARK_RED + newLeader + "</col>!");
                    }
                }
            }
        }, 4, 1);
    }

    /**
     * Make player wait and refresh highscores.
     */
    private void lookupRecord() {
        Dialogue.sendNPCDialogueNoContinue(player, 12320, NORMAL, "Looking for a record of your group...");
        GIM.getHighscores().requestRefresh();
        player.lock();
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                stop();
                player.unlock();
                Dialogue.closeNoContinueDialogue(player);
                sendNPCDialogue(12320, NORMAL, "You have only just created your group. Please wait a moment and try again.");
                stage = -1;
            }
        }, 5);
    }

    /**
     * Send the main menu.
     */
    private void sendMenu() {
        pendingJoin = GIM.getPendingJoinRequests().get(player.gimKey.getGroupKey());
        pendingKick = GIM.getKickRequests().get(player.gimKey.getGroupKey());
        sendOptionsDialogue("Select an option.",
                "Rename my group",
                pendingJoin != null ? "Please cancel my reservation for '" + pendingJoin + "'" :
                        "Can I add another member to my group?",
                pendingKick != null ? "Please cancel my kick request for '" + pendingKick + "'" :
                        "Can I kick a member from my group?",
                "Can I transfer leadership to another member?");
        stage = 0;
    }
}
