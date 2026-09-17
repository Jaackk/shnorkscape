package com.rs.game.activites.gim;

import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.rs.game.player.dialogue.Dialogue.CALM;

/**
 * A class representing a pending GIM group. In this state, the group leader is waiting for their members to join.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class GIMPendingGroup {

    /**
     * The group's key.
     */
    private final String groupKey;

    /**
     * The group's name.
     */
    private final String groupName;

    /**
     * The group leader.
     */
    private final Player leader;

    /**
     * The amount of members needed before this group can be created.
     */
    private final int membersNeeded;

    /**
     * The current set of members that joined.
     */
    private final Set<Player> members;

    /**
     * Creates a new {@link GIMPendingGroup}.
     */
    GIMPendingGroup(String groupKey, String groupName, Player leader, int membersNeeded) {
        this.groupKey = groupKey;
        this.groupName = groupName;
        this.leader = leader;
        this.membersNeeded = membersNeeded;
        members = Collections.newSetFromMap(new ConcurrentHashMap<>(membersNeeded + 1));
        members.add(leader);
    }

    /**
     * Adds a member to this pending group.
     */
    public void addMember(Player member) {
        if (members.size() >= GIM.MAX_MEMBERS) {
            throw new IllegalStateException("Memberset too large!");
        }
        member.pendingGimKey = groupKey;
        members.add(member);
        if (!isDone()) {
            for (Player user : members) {
                if (user.hasFinished() || user.isUnregisteredGIM())
                    continue;
                updateWaitForPartners(user);
            }
        } else {
            GIM.saveNewGroup(this);
        }
    }

    /**
     * Removes a member from this pending group.
     */
    public void removeMember(Player member) {
        members.remove(member);
        member.pendingGimKey = null;
        if (leader.equals(member) || members.isEmpty()) {
            GIM.dissolvePendingGroup(this, "the leader left");
        } else {
            for (Player user : members) {
                updateWaitForPartners(user);
            }
        }
    }

    /**
     * Displays the dialogue shown when waiting for more members to join this group.
     */
    public void updateWaitForPartners(Player player) {
        if (player == null)
            throw new IllegalStateException("Player should not be null at this point");
        if(player.isUnregisteredGIM())
            return;
        player.getInterfaceManager().closeChatBoxInterface();
        player.lock();
        int remaining = remaining();
        if (remaining == 1) {
            Dialogue.sendNPCDialogueNoContinue(player, 6139, CALM, "Waiting for 1 more member to join...");
        } else {
            Dialogue.sendNPCDialogueNoContinue(player, 6139, CALM, "Waiting for " + remaining + " more members to join...");
        }
    }

    /**
     * If all members have joined this group.
     */
    public boolean isDone() {
        return remaining() == 0;
    }

    /**
     * The amount of members required to join this group.
     */
    public int remaining() {
        return membersNeeded - (members.size() - 1);
    }

    public String getGroupKey() {
        return groupKey;
    }

    public String getGroupName() {
        return groupName;
    }

    public Player getLeader() {
        return leader;
    }

    public int getMembersNeeded() {
        return membersNeeded;
    }

    public Set<Player> getMembers() {
        return members;
    }
}