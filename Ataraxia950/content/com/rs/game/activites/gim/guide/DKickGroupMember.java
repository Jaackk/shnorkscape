package com.rs.game.activites.gim.guide;

import com.rs.game.activites.gim.GIM;
import com.rs.game.activites.gim.GIMGroup;
import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.DisplayNames;
import com.rs.utils.InputStringEvent;

/**
 * The dialogue for kicking GIM members.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class DKickGroupMember extends Dialogue {

    /**
     * The GIM group adding new members.
     */
    private final GIMGroup group;

    /**
     * Creates a new {@link DAddGroupMember}.
     */
    public DKickGroupMember(GIMGroup group) {
        this.group = group;
    }

    @Override
    public void start() {
        if (group.getMembers().size() <= 2) {
            sendNPCDialogue(12320, NORMAL, "You cannot kick anymore members.");
            stage = -1;
        } else {
            sendNPCDialogue(12320, NORMAL, "Please enter the username of the player to request a kick for.");
            stage = 0;
        }
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -1:
                loop();
                break;
            case 0:
                player.sendInputString("Enter the name", new InputStringEvent() {
                    @Override
                    public void run(Player player) {
                        String displayName = getString().toLowerCase().trim();
                        String username = DisplayNames.getUsername(displayName);
                        if(!group.getMembers().contains(username)) {
                            sendNPCDialogue(12320, NORMAL, "You cannot make a kick request for someone not in your group.");
                        } else if(group.getLeaderName().equalsIgnoreCase(username)) {
                            sendNPCDialogue(12320, NORMAL, "You cannot kick yourself. Transfer your leadership to another player first.");
                        } else {
                            GIM.getKickRequests().put(group.getGroupKey(), username);
                            sendNPCDialogue(12320, NORMAL, "You have requested to kick "+displayName+".",
                                    "You will need to speak to a Moderator to have them confirm it.",
                                    "This request will expire if the server goes offline.");
                        }
                        stage = -1;
                    }
                });
                break;
        }
    }

    @Override
    public void finish() {

    }

    /**
     * Loops back to the main dialogue.
     */
    private void loop() {
        player.getDialogueManager().startDialogue(new DGIMGuide());
    }
}
