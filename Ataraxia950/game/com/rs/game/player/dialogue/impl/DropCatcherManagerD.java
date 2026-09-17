package com.rs.game.player.dialogue.impl;

import com.rs.game.player.DataInterface;
import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.InputStringEvent;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class DropCatcherManagerD extends Dialogue {

    @Override
    public void start() {
        sendMainMenu();
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -1:
                sendMainMenu();
                break;
            case 0:
                if (componentId == OPTION_1) {
                    DataInterface inter = new DataInterface("Drop Catcher ~ Blocked drops");
                    for (String str : player.dropCatcherFilter) {
                        inter.add(str);
                    }
                    if (inter.size() == 0) {
                        inter.blankLines(3);
                        inter.add(Colors.RCYAN + "You haven't blocked any drops yet.");
                    }
                    inter.show(player);
                    player.sendInputString("Enter a key to return to the main menu", new InputStringEvent() {
                        @Override
                        public void run(Player player) {
                            player.getInterfaceManager().closeScreenInterface();
                            sendMainMenu();
                        }
                    });
                } else if (componentId == OPTION_2) {
                    if (player.dropCatcherFilter.size() < 600) {
                        player.sendInputString("Enter the name of the drop to block", new InputStringEvent() {
                            @Override
                            public void run(Player player) {
                                String drop = getString().toLowerCase().trim();
                                if (player.dropCatcherFilter.add(drop)) {
                                    sendDialogue("You have " + Colors.RED + "blocked</col> '" + drop + "'.");
                                } else {
                                    sendDialogue("That drop has already been blocked!");
                                }
                            }
                        });
                    } else {
                        sendDialogue("You have too many blocked drops! You'll need to unblock one first.");
                    }
                    stage = -1;
                } else if (componentId == OPTION_3) {
                    if (player.dropCatcherFilter.size() > 0) {
                        player.sendInputString("Enter the name of the drop to unblock", new InputStringEvent() {
                            @Override
                            public void run(Player player) {
                                String drop = getString().toLowerCase().trim();
                                if (player.dropCatcherFilter.remove(drop)) {
                                    sendDialogue("You have " + Colors.DARK_GREEN + "unblocked</col> '" + drop + "'.");
                                } else {
                                    sendDialogue("That drop is not currently blocked!");
                                }
                            }
                        });
                    } else {
                        sendDialogue("You don't have any blocked drops.");
                    }
                    stage = -1;
                } else if(componentId == OPTION_4) {
                    sendDialogue("Are you sure you would like to unblock all drops? This cannot be undone.");
                    stage = 1;
                }
                break;
            case 1:
                sendOptionsDialogue("Select an option.", "Yes", "No");
                stage = 2;
                break;
            case 2:
                if(componentId == OPTION_1) {
                    player.dropCatcherFilter.clear();
                }
                sendMainMenu();
                break;
        }
    }

    @Override
    public void finish() {

    }

    private void sendMainMenu() {
        player.getInterfaceManager().closeScreenInterface();
        sendOptionsDialogue("Select an option.",
                "View blocked drops (" + player.dropCatcherFilter.size() + ")",
                "Add blocked drop",
                "Remove blocked drop",
                "Clear all blocked drops");
        stage = 0;
    }
}