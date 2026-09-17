package com.rs.game.activites.gim.guide;

import com.rs.game.activites.gim.GIM;
import com.rs.game.activites.gim.GIMGroup;
import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.DisplayNames;
import com.rs.utils.InputStringEvent;
import com.rs.utils.Utils;

import java.util.Objects;

/**
 * The dialogue for adding new GIM members.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class DAddGroupMember extends Dialogue {

    /**
     * The GIM group adding new members.
     */
    private final GIMGroup group;

    /**
     * Creates a new {@link DAddGroupMember}.
     */
    public DAddGroupMember(GIMGroup group) {
        this.group = group;
    }

    @Override
    public void start() {
        if (group.getMembers().size() == 4) {
            sendNPCDialogue(12320, NORMAL, "Your group is already full.");
            stage = -1;
        } else {
            sendNPCDialogue(12320, NORMAL, "Please enter the username of the expected player.");
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
                        if(group.getMembers().contains(username)) {
                            sendNPCDialogue(12320, NORMAL, "You cannot make a join request for someone already in your group.");
                        } else {
                            GIM.getPendingJoinRequests().put(group.getGroupKey(), username);
                            sendNPCDialogue(12320, NORMAL, "You have reserved a spot for '" + displayName + "', they can now join your group.",
                                    "This reservation will expire if the server goes offline.");
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
