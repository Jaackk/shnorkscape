package com.rs.game.activites.gim.guide;

import com.rs.cores.CoresManager;
import com.rs.game.activites.gim.GIM;
import com.rs.game.activites.gim.GIMGroup;
import com.rs.game.activites.gim.GIMGroupKey;
import com.rs.game.activites.gim.GIMJoinGroupSql;
import com.rs.game.activites.gim.GIMPendingGroup;
import com.rs.game.player.Player;
import com.rs.game.player.content.interfaces.Starter.StarterInterface;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.InputStringEvent;
import com.rs.utils.Logger;
import com.rs.utils.SerializableFilesManager;

import java.util.concurrent.Future;

/**
 * The dialogue for helping kicked GIM players join another group.
 *
 * @author lare96 <http://github.com/lare96>
 */
public class DUnregisteredGIM extends Dialogue {

    @Override
    public void start() {
        if (player.pendingGimKey != null) {
            sendNPCDialogue(12320, NORMAL, "You are already awaiting the completion of a pending group. Would you like to clear it?");
            stage = -2;
        } else {
            sendNPCDialogue(12320, NORMAL, "Err, it seems like you aren't in a group at the moment. Would you like to join one?");
            stage = 0;
        }
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -3:
                if (componentId == OPTION_1) {
                    sendNPCDialogue(12320, NORMAL, "You are now free to join another group.");
                    stage = -1;
                    GIM.removeFromPendingGroup(player);
                } else if (componentId == OPTION_2) {
                    end();
                }
                break;
            case -2:
                sendOptionsDialogue("Select an option.", "Yes", "No");
                stage = -3;
                break;
            case -1:
                end();
                break;
            case 0:
                sendOptionsDialogue("Select an option.", "Yes", "No");
                stage = 1;
                break;
            case 1:
                if (componentId == OPTION_1) {
                    sendNPCDialogue(12320, NORMAL, "Please enter the name of the group you wish to join. They must have a reservation for you.");
                    stage = 2;
                } else if (componentId == OPTION_2) {
                    sendNPCDialogue(12320, NORMAL, "Okay. You won't be able to participate in anything GIM related until you do.");
                    stage = -1;
                }
                break;
            case 2:
                player.getInterfaceManager().closeChatBoxInterface();
                player.sendInputString("Enter the group name", new InputStringEvent() {
                    @Override
                    public void run(Player player) {
                        String groupKey = getString().toLowerCase().trim();
                        String reservationUsername = GIM.getPendingJoinRequests().get(groupKey);
                        if (reservationUsername != null) {
                            if (player.getUsername().equals(reservationUsername)) {
                                GIMGroup group = GIM.getGroupData().get(groupKey);
                                if (group == null) {
                                    sendNPCDialogue(12320, NORMAL, "No record of that group could be found. Please try again in a few minutes.");
                                    stage = -1;
                                    return;
                                }
                                int groupId = group.getGroupId();
                                player.lock();
                                Dialogue.sendNPCDialogueNoContinue(player, 12320, NORMAL, "Group found! Joining...");
                                Future<?> joinGroupFuture = CoresManager.getServiceProvider().executeNow(new GIMJoinGroupSql(groupId, reservationUsername));
                                WorldTasksManager.schedule(new WorldTask() {
                                    @Override
                                    public void run() {
                                        if (joinGroupFuture.isDone()) {
                                            String groupName = group.getGroupName();
                                            stop();
                                            stage = -1;
                                            player.unlock();
                                            GIM.getPendingJoinRequests().remove(groupKey);
                                            Dialogue.closeNoContinueDialogue(player);
                                            boolean failed = false;
                                            try {
                                                joinGroupFuture.get();
                                            } catch (Exception e) {
                                                Logger.getGlobal().catching(e);
                                                failed = true;
                                            }
                                            if (failed || joinGroupFuture.isCancelled()) {
                                                sendNPCDialogue(12320, NORMAL, "The server could not add you to the group. Please try again.");
                                                return;
                                            }
                                            sendNPCDialogue(12320, NORMAL, "You have successfully joined " + Colors.DARK_RED + groupName + "</col>!");
                                            player.gimName = groupName;
                                            player.gimKey = new GIMGroupKey(groupId, groupKey);
                                            SerializableFilesManager.savePlayer(player);
                                        }
                                    }
                                }, 4, 1);
                            } else {
                                sendNPCDialogue(12320, NORMAL, "That group does not have a reservation for you.");
                                stage = -1;
                            }
                            return;
                        }
                        GIMPendingGroup pendingGroup = GIM.getPendingGroups().get(groupKey);
                        if (pendingGroup != null) {
                            pendingGroup.addMember(player);
                            if (pendingGroup.isDone()) {
                                sendNPCDialogue(12320, NORMAL, "You have joined the newly created group " + Colors.DARK_RED + pendingGroup.getGroupName() + "</col>!");
                            } else {
                                sendNPCDialogue(12320, NORMAL, "You have joined the pending group " + Colors.DARK_RED + pendingGroup.getGroupName() + "</col>.",
                                        "This group needs " + pendingGroup.remaining() + " more member(s) until it can be created.",
                                        "Logging out before its creation will remove you from the pending group.");
                            }
                            stage = -1;
                        } else {
                            sendNPCDialogue(12320, NORMAL, "That group does not have a reservation for you.");
                            stage = -1;
                        }
                    }
                });
                break;
        }
    }

    @Override
    public void finish() {

    }
}
