package com.rs.game.activites.gim;

import com.rs.game.player.Player;
import com.rs.game.player.Titles;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.InputIntegerEvent;
import com.rs.utils.InputStringEvent;
import com.rs.utils.Logger;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * A dialogue that facilitates the creation of GIM groups.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class DCreateGroup extends Dialogue {

    /**
     * The cached entered group name.
     */
    String groupName;

    /**
     * If the dialogue is finished.
     */
    boolean finished;

    @Override
    public void start() {
        player.getInterfaceManager().closeScreenInterface();
        sendDialogue("Please enter the desired name of your group.",
                "It must be between 3 and 20 characters.",
                "It must also only contain characters a-z, 0-9, and spaces.");
        stage = 0;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 0:
                createNewGroup();
                break;
            case 1:
                enterDesiredPlayers();
                break;
        }
    }

    @Override
    public void finish() {
        finished = true;
    }

    /**
     * Dialogue to enter the name of your group.
     */
    private void createNewGroup() {
        player.sendInputString("Enter the name", new InputStringEvent() {
            @Override
            public void run(Player player) {
                if (finished)
                    return;
                String enteredName = getString().trim();
                if (GIM.isGroupNameValid(enteredName)) {
                    groupName = enteredName;
                    enterDesiredPlayers();
                } else {
                    sendNPCDialogue(6139, CALM, "Group names must be between 3 and 20 characters.",
                            "They must also only contain characters a-z, 0-9, and spaces.");
                    stage = 0;
                }
            }
        });
    }

    /**
     * Dialogue to enter the amount of desired players.
     */
    private void enterDesiredPlayers() {
        end();
        String name = groupName;
        String key = groupName.toLowerCase().trim();
        int maxMembers = GIM.MAX_MEMBERS - 1;
        player.sendInputInteger("Select the amount of additional members to have (1-" + maxMembers + ")", new InputIntegerEvent() {
            @Override
            public void run(Player player) {
                int enteredPlayers = getInteger();
                if (enteredPlayers >= 1 && enteredPlayers <= maxMembers) {
                    for (String profanity : Titles.UNALLOWED_TITLES) {
                        if (key.equalsIgnoreCase(profanity) || key.length() > 3 && key.contains(profanity)) {
                            Dialogue.sendSingleNPCDialogue(player, 6139, CALM,
                                    dialogue -> player.getDialogueManager().startDialogue(new DCreateGroup()),
                                    "You cannot use profanity in your group name.");
                           return;
                        }
                    }
                    checkIfNameAvailable(name, key, enteredPlayers);
                } else {
                    Dialogue.sendSingleNPCDialogue(player, 6139, CALM,
                            dialogue -> player.getDialogueManager().startDialogue(new DCreateGroup()),
                            "The amount of members must be between 1 and " + maxMembers + ".");
                }
            }
        });
    }

    /**
     * Asynchronously check if the group name is available.
     */
    private void checkIfNameAvailable(String name, String key, int enteredPlayers) {
        Dialogue.sendNPCDialogueNoContinue(player, 6139, CALM, "Checking name availability...");
        Future<Boolean> groupExistsFuture = GIM.groupExists(key);
        player.lock();
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                if (player.hasFinished()) {
                    stop();
                    return;
                }
                if (groupExistsFuture.isDone()) {
                    stop();
                    Boolean groupExists;
                    try {
                        groupExists = groupExistsFuture.get();
                    } catch (InterruptedException | ExecutionException e) {
                        Logger.getGlobal().catching(e);
                        groupExists = null;
                    }
                    if (groupExists == null) {
                        Dialogue.sendSingleNPCDialogue(player, 6139, CALM,
                                dialogue -> player.getDialogueManager().startDialogue(new DCreateGroup()),
                                "Could not complete name check. Please try again.");
                        return;
                    } else if (groupExists) {
                        Dialogue.sendSingleNPCDialogue(player, 6139, CALM,
                                dialogue -> player.getDialogueManager().startDialogue(new DCreateGroup()),
                                "A group with the name '" + name + "' already exists.");
                        return;
                    }
                    GIM.addPending(name, key, player, enteredPlayers);
                    player.getInterfaceManager().closeScreenInterface();
                    player.pendingGimKey = key;
                    Dialogue.sendSingleNPCDialogue(player, 6139, CALM,
                            dialogue -> GIM.updateWaitForPartners(player),
                            "Please tell whomever you would like to join to select 'Join existing group' and enter '" + key + "'.",
                            "Once all expected players have joined, you will all be added to the game. Logging out will reset the process.");
                }
            }
        }, 1, 1);
    }
}