package com.rs.game.player.commands.impl.modcommands;

import com.rs.game.activites.gim.GIM;
import com.rs.game.activites.gim.GIMGroup;
import com.rs.game.player.Player;
import com.rs.game.player.commands.Command;
import com.rs.game.player.commands.CommandInfo;
import com.rs.game.player.commands.CommandRights;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.InputStringEvent;

/**
 * @author lare96 <http://github.com/lare96>
 */
@CommandInfo(
        rank = CommandRights.MODERATOR,
        possibleCommands = {"renamegimteam"},
        description = "renames a GIM team"
)
public final class RenameGIMTeamCommand extends Command {

    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        player.getDialogueManager().startDialogue(new Dialogue() {
            GIMGroup group;

            @Override
            public void start() {
                sendNPCDialogue(12320, CALM, "Please enter the name of the group you want to rename.");
                stage = 0;
            }

            @Override
            public void run(int interfaceId, int componentId) {
                switch (stage) {
                    case -1:
                        end();
                        break;
                    case 0:
                        player.sendInputString("Enter the old name", new InputStringEvent() {
                            @Override
                            public void run(Player player) {
                                String name = getString().toLowerCase().trim();
                                group = GIM.getGroupData().get(name);
                                if (group == null) {
                                    sendNPCDialogue(12320, CALM, "No group matching " + name + " was found!");
                                } else {
                                    sendNPCDialogue(12320, CALM, "Now enter the new name for the group.");
                                }
                                stage = 1;
                            }
                        });
                        break;
                    case 1:
                        if (group == null) {
                            end();
                        } else {
                            GIM.startRenameGroup(player, group, this);
                        }
                        break;
                }
            }

            @Override
            public void finish() {

            }
        });
    }
}
