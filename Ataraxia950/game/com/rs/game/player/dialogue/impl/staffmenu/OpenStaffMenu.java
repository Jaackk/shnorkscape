package com.rs.game.player.dialogue.impl.staffmenu;

import com.rs.game.World;
import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.InputNameEvent;
import com.rs.utils.SerializableFilesManager;
import com.rs.utils.Utils;

import java.io.File;
import java.io.IOException;

/**
 * @author Xenthium
 */

public class OpenStaffMenu extends Dialogue {

    @Override
    public void start() {
        if (player.isStaff()) {
            sendOptionsDialogue("Is the player online or offline?", "Online", "Offline", "Nevermind...");
        }
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -1:
            switch (componentId) {
                case OPTION_1:
                case OPTION_2:
                    end();
                    sendInputDialogue(componentId != OPTION_1);
                    break;
                case OPTION_3:
                    end();
                    break;
            }
            break;

            case 0:
                end();
                break;
        }
    }

    @Override
    public void finish() {

    }

    private void sendInputDialogue(final boolean loadPlayerSave) {
        player.sendInputName((loadPlayerSave ? "Enter the username of the player:<br>Note: offline mode has limited functionality." : "Enter the username or display name of the player:"), new InputNameEvent() {
            @Override
            public void run(Player player) {
                Player target;
                String name = getString();
                if (name.equalsIgnoreCase(player.getDisplayName()) || name.equalsIgnoreCase(player.getUsername())) {
                    sendErrorDialogue("You cannot use the staff menu on yourself.");
                    return;
                }
                if (!loadPlayerSave) {
                    target = World.getPlayerByDisplayName(name);
                    if (target != null) {
                        player.getDialogueManager().startDialogue("OnlineStaffMenu", target);
                    } else {
                        sendErrorDialogue("Unable to find any online player with the username/display name \"" + getString() + "\".");
                    }
                } else {
                    if (!World.containsPlayer(name)) {
                        File save = new File("data/playersaves/characters/" + name.replace(" ", "_") + ".p");
                        try {
                            target = (Player) SerializableFilesManager.loadSerializedFile(save);
                        } catch (IOException | ClassNotFoundException e) {
                            sendErrorDialogue("There was an error when attempting to load that player's character file.");
                            return;
                        }
                        if (target != null) {
                            target.setUsername(Utils.formatPlayerNameForProtocol(name));
                            player.getDialogueManager().startDialogue("OfflineStaffMenu", target, save);
                        } else {
                            sendErrorDialogue("Unable to locate the character file \"" + getString() + "\",<br> remember to use their username for offline mode, not display name.");
                        }
                    } else {
                        sendErrorDialogue("The player's character file you're attempting to load is currently online, try using the online option when opening the staff menu for this player.");
                    }
                }
            }
        });
    }


    private void sendErrorDialogue(final String errorMessage) {
        player.getDialogueManager().startDialogue(new Dialogue() {
            @Override
            public void start() {
                sendDialogue(errorMessage);
            }

            @Override
            public void run(int interfaceId, int componentId) {
                end();
            }

            @Override
            public void finish() {
            }
        });
    }

}
