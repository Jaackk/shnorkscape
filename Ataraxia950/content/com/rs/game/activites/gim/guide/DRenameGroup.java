package com.rs.game.activites.gim.guide;

import com.google.common.collect.Sets;
import com.rs.cores.CoresManager;
import com.rs.game.World;
import com.rs.game.activites.gim.GIM;
import com.rs.game.activites.gim.GIMCheckNameSql;
import com.rs.game.activites.gim.GIMGroup;
import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.InputStringEvent;
import com.rs.utils.Logger;
import com.rs.utils.SerializableFilesManager;
import com.rs.utils.mysql.DatabaseCredential;
import com.rs.utils.mysql.Pool;
import com.rs.utils.mysql.SQLRunnable;
import org.apache.logging.log4j.message.ParameterizedMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * A class representing the group renaming dialogue.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class DRenameGroup extends Dialogue {

    /**
     * The Ataraxia Dollars cost to rename a group.
     */
    public static final int RENAME_COST = 10;

    /**
     * Redirects the dialogue back to the main settings menu.
     */
    private final Consumer<Dialogue> settingsLoop =
            dialogue -> player.getDialogueManager().startDialogue(new DGroupSettings());

    /**
     * The GIM group being renamed.
     */
    private final GIMGroup group;

    /**
     * Creates a new {@link DRenameGroup}.
     */
    public DRenameGroup(GIMGroup group) {
        this.group = group;
    }

    @Override
    public void start() {
        LocalDateTime lastRename = GIM.getLastRenames().get(group.getGroupKey());
        if(lastRename != null && LocalDateTime.now().isBefore(lastRename.plusHours(24))) {
            Dialogue.sendSingleNPCDialogue(player, 12320, NORMAL, settingsLoop,
                    "You can only rename your group every 24h.");
        } else if (GIM.getPendingJoinRequests().containsKey(group.getGroupKey())) {
            Dialogue.sendSingleNPCDialogue(player, 12320, NORMAL, settingsLoop,
                    "Please cancel your pending reservation before renaming your group.");
        } else {
            if(lastRename != null) {
                GIM.getLastRenames().remove(group.getGroupKey());
            }
            sendNPCDialogue(12320, Dialogue.NORMAL,
                    "A group rename will cost  " + RENAME_COST + " Ataraxia dollars.",
                    "Are you okay with that?");
            stage = 0;
        }
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -1:
                promptEnterName();
                break;
            case 0:
                sendOptionsDialogue("Select an option.", "Yes", "No");
                stage = 1;
                break;
            case 1:
                if (componentId == OPTION_1) {
                    if (player.getInventory().containsItem(41430, RENAME_COST)) {
                        promptEnterName();
                    } else {
                        Dialogue.sendSingleNPCDialogue(player, 12320, NORMAL, settingsLoop,
                                "You need " + RENAME_COST + " Ataraxia dollars to rename your group.");
                    }
                } else if (componentId == OPTION_2) {
                    player.getDialogueManager().startDialogue(new DGroupSettings());
                }
                break;
            case 2:
                GIM.startRenameGroup(player, group, this);
                break;
        }
    }

    @Override
    public void finish() {

    }

    /**
     * Opens a prompt to enter the new name.
     */
    private void promptEnterName() {
        sendNPCDialogue(12320, Dialogue.NORMAL, "Please enter the new name for your group.");
        stage = 2;
    }

    public GIMGroup getGroup() {
        return group;
    }
}
