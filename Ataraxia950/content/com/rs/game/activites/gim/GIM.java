package com.rs.game.activites.gim;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Futures;
import com.rs.Settings;
import com.rs.cores.CoresManager;
import com.rs.game.World;
import com.rs.game.activites.gim.bank.GIMBank;
import com.rs.game.activites.gim.bank.GIMBankManager;
import com.rs.game.activites.gim.event.GIMEventManager;
import com.rs.game.activites.gim.guide.DGroupSettings;
import com.rs.game.activites.gim.guide.DRenameGroup;
import com.rs.game.activites.gim.highscores.GIMHighscores;
import com.rs.game.activites.gim.season.GIMDiscordManager;
import com.rs.game.activites.gim.season.GIMFetchWinnersSql;
import com.rs.game.activites.gim.season.GIMHighlightsManager;
import com.rs.game.activites.gim.season.GIMRewardsManager;
import com.rs.game.activites.gim.season.GIMSeasonData;
import com.rs.game.activites.gim.season.GIMSeasonDataSql;
import com.rs.game.activites.gim.season.GIMSeasonTask;
import com.rs.game.activites.gim.season.GIMSeasonWinner;
import com.rs.game.player.Player;
import com.rs.game.player.content.interfaces.Starter.StarterInterface;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.InputStringEvent;
import com.rs.utils.Logger;
import com.rs.utils.SerializableFilesManager;
import org.apache.logging.log4j.message.ParameterizedMessage;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static com.rs.game.player.dialogue.Dialogue.CALM;

/**
 * Controls all the main functions of the Group Ironman game mode.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class GIM {

    /**
     * If GIM is active.
     */
    public static boolean ACTIVE = !Settings.TEST_SERVER_MODE;

    /**
     * If GIM is in Beta mode.
     */
    public static final boolean BETA_MODE = Settings.TEST_SERVER_MODE;

    /**
     * The maximum amount of group members.
     */
    public static final int MAX_MEMBERS = 4;

    /**
     * Regex for checking validity of group names.
     */
    private static final Pattern VALID_NAME = Pattern.compile("^[A-Za-z0-9 ]+$");

    /**
     * A map of pending groups. These groups are waiting for more members to join them.
     */
    private static final Map<String, GIMPendingGroup> pendingGroups = new ConcurrentHashMap<>();

    /**
     * The discord manager.
     */
    private static final GIMDiscordManager discordManager = new GIMDiscordManager();

    /**
     * The season task.
     */
    private static final GIMSeasonTask seasonTask = new GIMSeasonTask();

    /**
     * The highscores.
     */
    private static final GIMHighscores highscores = new GIMHighscores();

    /**
     * The highlights.
     */
    private static final GIMHighlightsManager highlights = new GIMHighlightsManager();

    /**
     * The rewards.
     */
    private static final GIMRewardsManager rewards = new GIMRewardsManager();

    /**
     * The event manager.
     */
    private static final GIMEventManager eventManager = new GIMEventManager();

    /**
     * A group being deleted or renamed, login blocked.
     */
    private static final Set<String> loginBlocked = Sets.newConcurrentHashSet();

    /**
     * The pending join requests (group key -> username).
     */
    private static final Map<String, String> pendingJoinRequests = new ConcurrentHashMap<>();

    /**
     * The map of groups that renamed their groups recently (group key -> last rename date & time).
     */
    private static final Map<String, LocalDateTime> lastRenames = new HashMap<>();

    /**
     * The map of kick requests (group key -> requested member to kick).
     */
    private static final Map<String, String> kickRequests = new HashMap<>();

    /**
     * The set of leave request (group key -> request usernames leaving).
     */
    private static final SetMultimap<String, String> leaveRequests = HashMultimap.create();

    /**
     * The latest seasonal data. Updated by the {@link GIMSeasonTask} every hour.
     */
    private static volatile GIMSeasonData seasonData;

    /**
     * The latest group data (group name -> group data).
     */
    private static volatile ImmutableMap<String, GIMGroup> groupData;

    /**
     * The latest winner data.
     */
    private static volatile ImmutableList<GIMSeasonWinner> winnerData;

    /**
     * The latest rankings.
     */
    private static volatile ImmutableList<GIMGroup> rankings;

    /**
     * The latest group data (group member name -> group data).
     */
    private static volatile ImmutableMap<String, GIMGroup> groupMemberData;

    /**
     * Starts the GIM processes.
     */
    public static void start() {
        try {
            if (ACTIVE) {
                eventManager.load();
                highlights.load();
                rewards.load();
                refreshSeasonData();
                seasonTask.start();
                highscores.start();
                refreshWinners();
            }
        } catch (Exception e) {
            ACTIVE = false;
            Logger.getGlobal().catching(e);
        }
    }

    /**
     * Sends a GIM themed message to the world.
     */
    public static void sendWorldMsg(String msg) {
        World.sendWorldMessage(Colors.DEF_SEARCH_CYAN + "<img=33>GIM: " + msg, false);
    }

    /**
     * Sends a GIM themed message to a player.
     */
    public static void sendPlayerMsg(Player player, String msg) {
        if (player.isGroupIronman()) {
            player.sendMessage(Colors.DEF_SEARCH_CYAN + "<img=33> " + msg);
        }
    }

    /**
     * Adds a pending group to the backing map. Does not check for duplicates.
     */
    static void addPending(String groupName, String groupKey, Player leader, int membersNeeded) {
        pendingGroups.put(groupKey, new GIMPendingGroup(groupKey, groupName, leader, membersNeeded));
    }

    /**
     * Determines if a group with {@code key} exists.
     */
    static Future<Boolean> groupExists(String key) {
        GIMPendingGroup group = pendingGroups.get(key);
        if (group != null) {
            if (group.getLeader().hasFinished() || group.getMembers().isEmpty()) {
                pendingGroups.remove(key);
            } else {
                return Futures.immediateFuture(true);
            }
        }
        return CoresManager.getServiceProvider().submitNow(new GIMCheckNameSql(key));
    }

    /**
     * Determines if a group name is valid.
     */
    public static boolean isGroupNameValid(String name) {
        return name.length() >= 3 && name.length() <= 20 && VALID_NAME.matcher(name).matches();
    }

    /**
     * Starts the group renaming dialogue for staff and players. In the argued dialogue, Stage=-1 must end/loop the dialogue.
     */
    public static void startRenameGroup(Player player, GIMGroup oldGroup, Dialogue dialogue) {
        boolean realDialogue = dialogue instanceof DRenameGroup;
        player.sendInputString("Enter the new name", new InputStringEvent() {
            @Override
            public void run(Player player) {
                String enteredName = getString().trim();
                if (oldGroup.getGroupName().equalsIgnoreCase(enteredName)) {
                    dialogue.sendNPCDialogue(12320, CALM, "The new name must be different than the old name.");
                    dialogue.setStage(-1);
                } else if (GIM.isGroupNameValid(enteredName)) {
                    checkNameAvailability(player, oldGroup, enteredName, enteredName.toLowerCase(), dialogue, realDialogue);
                } else {
                    dialogue.sendNPCDialogue(12320, CALM, "Group names must be between 3 and 20 characters.",
                            "They must also only contain characters a-z, 0-9, and spaces.");
                    dialogue.setStage(-1);
                }
            }
        });
    }

    /**
     * Checks the availability of the chosen name. Will either proceed to renaming. or notify the player that the chosen
     * name is in use.
     */
    private static void checkNameAvailability(Player player, GIMGroup oldGroup, String newName, String newKey, Dialogue dialogue, boolean realDialogue) {
        Consumer<Dialogue> loop = realDialogue ?
                d -> player.getDialogueManager().startDialogue(new DGroupSettings()) :
                Dialogue::end;
        player.getInterfaceManager().closeChatBoxInterface();
        Future<Boolean> nameResult = CoresManager.getServiceProvider().submitNow(new GIMCheckNameSql(newKey));
        Dialogue.sendNPCDialogueNoContinue(player, 12320, Dialogue.NORMAL, "Checking name availability...");
        player.lock();
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                // Use this task to force the player to wait until the asynchronous task is done.
                if (nameResult.isDone()) {
                    stop();
                    Boolean foundName = null;
                    try {
                        // Get the result of the asynchronous task.
                        foundName = nameResult.get();
                    } catch (InterruptedException | ExecutionException e) {
                        Logger.getGlobal().catching(e);
                    }

                    // An error occurred while checking for the name.
                    if (foundName == null) {
                        Dialogue.closeNoContinueDialogue(player);
                        Dialogue.sendSingleNPCDialogue(player, 12320, CALM, loop,
                                "Error checking for name availibility. Please try again later.");
                        player.unlock();
                        return;
                    }

                    // The name they chose is in-use already.
                    if (foundName) {
                        Dialogue.closeNoContinueDialogue(player);
                        dialogue.sendNPCDialogue(12320, CALM, "The name you selected is not available. Please choose a different name.");
                        dialogue.setStage(-1);
                        player.unlock();
                        return;
                    }

                    // They don't have enough atar dollars.
                    if (realDialogue && !player.getInventory().containsItem(41430, DRenameGroup.RENAME_COST)) {
                        Dialogue.closeNoContinueDialogue(player);
                        Dialogue.sendSingleNPCDialogue(player, 12320, CALM, loop,
                                "You need " + DRenameGroup.RENAME_COST + " dollars to rename your group.");
                        player.unlock();
                        return;
                    }

                    // All checks passed, rename group.
                    if (realDialogue) {
                        player.getInventory().deleteItem(41430, DRenameGroup.RENAME_COST);
                    }
                    Dialogue.sendNPCDialogueNoContinue(player, 12320, Dialogue.NORMAL, "Name available! Renaming group...");
                    renameGroup(player, oldGroup, newName, newKey, realDialogue);
                }
            }
        }, 5, 2);
    }

    /**
     * Renames the group.
     */
    private static void renameGroup(Player player, GIMGroup oldGroup, String newName, String newKey, boolean realDialogue) {

        // First rename the bank.
        renameBank(oldGroup.getGroupName(), newName);

        // Prevent group members from logging in during this process.
        GIM.getLoginBlocked().addAll(oldGroup.getMembers());

        Future<?> renameFuture = CoresManager.getServiceProvider().runNow(() -> {
            // Rename the group in SQL tables.
            GIMRenameGroupSql renameGroupSql = new GIMRenameGroupSql(newName, newKey, oldGroup.getGroupId());
            renameGroupSql.prepare();

            // Rename the group data in the members' profiles.
            for (String member : oldGroup.getMembers()) {
                Player foundPlayer = World.playerMap.get(member);
                if (foundPlayer != null) {
                    CoresManager.getServiceProvider().addGameTask(() -> {
                        foundPlayer.gimName = newName;
                        foundPlayer.gimKey = foundPlayer.gimKey.withNewKey(newKey);
                        SerializableFilesManager.savePlayer(foundPlayer);
                    });
                } else {
                    Player loadedPlayer = SerializableFilesManager.loadPlayer(member);
                    if (loadedPlayer != null && loadedPlayer.isGroupIronman()) {
                        loadedPlayer.gimName = newName;
                        loadedPlayer.gimKey = foundPlayer.gimKey.withNewKey(newKey);
                        SerializableFilesManager.savePlayer(member, loadedPlayer);
                    } else {
                        throw new IllegalStateException("GIM player '" + member + "' could not be found!");
                    }
                }
            }
        });

        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {

                // Wait until task is done, then notify player it completed.
                if (renameFuture.isDone()) {
                    stop();
                    Dialogue.closeNoContinueDialogue(player);
                    player.unlock();
                    boolean hasErrored = false;
                    try {
                        renameFuture.get();
                    } catch (Exception e) {
                        hasErrored = true;
                        Logger.getGlobal().error(new ParameterizedMessage("Error while renaming group '{}'.", oldGroup.getGroupName()), e);
                    }
                    if (renameFuture.isCancelled() || hasErrored) {
                        if (realDialogue) {
                            Dialogue.sendSingleNPCDialogue(player, 12320, CALM, "Name change failed. Please tell all your group members to logout and report this message to an admin.");
                        } else {
                            Dialogue.sendSingleNPCDialogue(player, 12320, CALM, "Name change failed. Please tell the team to logout, and report this message to lare96!");
                        }
                    } else {
                        GIM.getHighscores().requestRefresh();
                        GIM.getLoginBlocked().removeAll(oldGroup.getMembers());
                        Dialogue.sendSingleNPCDialogue(player, 12320, CALM,
                                "Your group name has successfully been changed to " + Colors.RED + newName + "</col>!");
                        if (realDialogue) {
                            GIM.getLastRenames().put(newKey, LocalDateTime.now());
                        }
                    }
                }
            }
        }, 4, 1);
    }

    /**
     * Renames the GIM bank.
     */
    private static void renameBank(String oldName, String newName) {
        GIMBank loadedBank = GIMBankManager.requestBank(oldName);
        CoresManager.getServiceProvider().executeNow(() -> {
            try {
                Files.deleteIfExists(GIMBankManager.getBankPath(oldName));
            } catch (IOException e) {
                Logger.getGlobal().catching(e);
            }
        });
        loadedBank.rename(oldName, newName);
    }

    /**
     * Determines if leadership can be transferred to {@code toMember}. Returns {@code null} if yes.
     */
    public static String canTransferLeadership(GIMGroup group, String toMember, boolean staff) {
        String groupKey = group.getGroupKey();
        if (!group.getMembers().contains(toMember)) {
            return Colors.DARK_RED + toMember + "</col> is not a member of " + Colors.DARK_RED + group.getGroupName() + "</col>.";
        } else if (pendingJoinRequests.containsKey(groupKey)) {
            return staff ? "This cannot be done while the group has an active join reservation." :
                    "Please clear your active join reservation before doing this.";
        } else if (kickRequests.containsKey(groupKey)) {
            return staff ? "This cannot be done while the group has an active kick request." :
                    "Please clear your active kick request before doing this.";
        }
        return null;
    }

    /**
     * Creates a groupless GIM account.
     */
    public static void createGrouplessAccount(Player newPlayer) {
        newPlayer.gimName = "";
        newPlayer.gimKey = new GIMGroupKey(-1, "");
        newPlayer.getInterfaceManager().closeChatBoxInterface();
        newPlayer.setIronMan(true);
        sendPlayerMsg(newPlayer, "You are currently groupless! Speak to the GIM guide to join another group.");
        StarterInterface.completeTutorial(newPlayer);
        SerializableFilesManager.savePlayer(newPlayer);
    }

    /**
     * Adds {@code member} to a pending group, starting the group afterwards if necessary.
     */
    static boolean addMember(String groupKey, Player member) {
        GIMPendingGroup pending = pendingGroups.get(groupKey);
        if (pending == null) {
            // Add member to an existing GIM group.
            String joinRequest = pendingJoinRequests.get(groupKey);
            if (!Objects.equals(joinRequest, member.getUsername())) {
                return false;
            }
            GIMGroup group = getGroupData().get(groupKey);
            Future<?> joinGroupFuture = CoresManager.getServiceProvider().executeNow(new GIMJoinGroupSql(group.getGroupId(), member.getUsername()));
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    if (joinGroupFuture.isDone()) {
                        stop();
                        pendingJoinRequests.remove(group.getGroupKey());
                        member.unlock();
                        if (joinGroupFuture.isCancelled()) {
                            Logger.getGlobal().warn("{} could not join existing GIM group '{}'.", member.getUsername(), group.getGroupName());
                            Dialogue.sendNPCDialogue(member, 6139, CALM, "The server could not add you to the group. Please try again.");
                            return;
                        }
                        member.getInterfaceManager().closeChatBoxInterface();
                        member.setIronMan(true);
                        member.gimName = group.getGroupName();
                        member.gimKey = new GIMGroupKey(group.getGroupId(), group.getGroupKey());
                        StarterInterface.completeTutorial(member);
                        SerializableFilesManager.savePlayer(member);
                    }
                }
            }, 1, 1);
            return true;
        }
        pending.addMember(member);
        return true;
    }

    /**
     * Sends the "{@code x} players remaining" dialogue.
     */
    static void updateWaitForPartners(Player player) {
        GIMPendingGroup group = pendingGroups.get(player.pendingGimKey);
        if (group == null)
            return;
        group.updateWaitForPartners(player);
    }

    /**
     * Asynchronously saves the group to the database.
     */
    static void saveNewGroup(GIMPendingGroup pending) {

        // Check if someone logged out or chose another game mode, if so dissolve the group.
        boolean cancelled = false;
        for (Player player : pending.getMembers()) {
            if(player.isUnregisteredGIM()) {
                continue;
            }
            if (player.hasFinished() || player.hasCompleted()) {
                cancelled = true;
                break;
            }
            Dialogue.sendNPCDialogueNoContinue(player, 6139, CALM, "Registering group...");
        }
        if (cancelled) {
            dissolvePendingGroup(pending, "someone left");
            return;
        }

        // Group is okay, save players and add them to the world.
        Future<Integer> createGroupFuture = CoresManager.getServiceProvider().submitNow(new GIMCreateGroupSql(pending));
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                if (createGroupFuture.isDone()) {
                    if (GIM.getRankings().isEmpty()) {
                        highscores.requestRefresh();
                    }
                    stop();
                    Integer groupId;
                    try {
                        groupId = createGroupFuture.get();
                    } catch (InterruptedException | ExecutionException e) {
                        Logger.getGlobal().catching(e);
                        groupId = null;
                    }
                    if (groupId == null || groupId == -1) {
                        Logger.getGlobal().warn("Could not create GIM group '" + pending.getGroupName() + "'.");
                        dissolvePendingGroup(pending, "the server could not save your group. Please try again.");
                        return;
                    }
                    for (Player player : pending.getMembers()) {
                        if (!player.isUnregisteredGIM()) {
                            player.unlock();
                            player.getInterfaceManager().closeChatBoxInterface();
                            player.setIronMan(true);
                            StarterInterface.completeTutorial(player);
                        } else {
                            sendPlayerMsg(player, "Your pending group has been created! You are now apart of group '" + pending.getGroupName() + "'.");
                        }
                        player.gimName = pending.getGroupName();
                        player.gimKey = new GIMGroupKey(groupId, pending.getGroupKey());
                        SerializableFilesManager.savePlayer(player);
                    }
                }
            }
        }, 1, 1);
    }

    /**
     * Displays the dialogue for dissolving a pending group, for all group members. Will also unlock them.
     */
    public static void dissolvePendingGroup(GIMPendingGroup pending, String reason) {
        for (Player player : pending.getMembers()) {
            player.pendingGimKey = null;
            if (player.isUnregisteredGIM()) {
                sendPlayerMsg(player, "Your pending group has been dissolved. This is because " + reason + ".");
                continue;
            }

            if (!player.hasFinished()) {
                player.getDialogueManager().startDialogue(new DGroupDissolve(reason));
                player.unlock();
            }
        }
        pendingGroups.remove(pending.getGroupKey());
    }

    /**
     * Removes the player from their pending group on logout.
     */
    public static void removeFromPendingGroup(Player player) {
        if (player.pendingGimKey != null) {
            GIMPendingGroup pending = pendingGroups.get(player.pendingGimKey);
            if (pending == null)
                return;
            pending.removeMember(player);
        }
    }

    /**
     * Renames this player's group.
     */
    public static void renameGroup(Player player) {
        GIMGroup group = getGroupData().get(player.gimKey.getGroupKey());
        if (group == null) {
            Dialogue.sendNPCDialogueNoContinue(player, 12320, Dialogue.NORMAL, "Looking for a record of your group...");
            GIM.getHighscores().requestRefresh();
            player.lock();
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    stop();
                    player.unlock();
                    Dialogue.closeNoContinueDialogue(player);
                    Dialogue.sendSingleNPCDialogue(player, 12320, Dialogue.NORMAL,
                            dialogue -> player.getDialogueManager().startDialogue(new DGroupSettings()),
                            "You have only just created your group. Please wait a moment and try again.");
                }
            }, 5);
        } else {
            if (group.getLeaderName().equals(player.getUsername())) {
                player.getDialogueManager().startDialogue(new DRenameGroup(group));
            } else {
                Dialogue.sendSingleNPCDialogue(player, 12320, Dialogue.NORMAL,
                        "Only the group leader can rename the group.");
            }
        }
    }

    /**
     * Gets the group at {@code rank}.
     */
    public static GIMGroup getGroupForRank(int rank) {
        int index = rank - 1;
        if (rankings == null || index >= rankings.size()) {
            return null;
        }
        return rankings.get(index);
    }

    /**
     * Gets the group for the {@code member}.
     */
    public static GIMGroup getGroupForMember(String member) {
        if (groupMemberData == null)
            return null;
        return groupMemberData.get(member);
    }

    /**
     * Refreshes the previous winners.
     */
    private static void refreshWinners() {
        GIMFetchWinnersSql fetchWinnersSql = new GIMFetchWinnersSql();
        fetchWinnersSql.prepare();
    }

    /**
     * Refreshes the season data.
     */
    private static void refreshSeasonData() {
        GIMSeasonDataSql seasonDataSql = new GIMSeasonDataSql();
        seasonDataSql.prepare();
    }

    public static Map<String, GIMPendingGroup> getPendingGroups() {
        return pendingGroups;
    }

    public static GIMDiscordManager getDiscordManager() {
        return discordManager;
    }

    public static GIMHighscores getHighscores() {
        return highscores;
    }

    public static GIMHighlightsManager getHighlights() {
        return highlights;
    }

    public static GIMRewardsManager getRewards() {
        return rewards;
    }

    public static GIMEventManager getEventManager() {
        return eventManager;
    }

    public static Set<String> getLoginBlocked() {
        return loginBlocked;
    }

    public static Map<String, String> getPendingJoinRequests() {
        return pendingJoinRequests;
    }

    public static Map<String, LocalDateTime> getLastRenames() {
        return lastRenames;
    }

    public static Map<String, String> getKickRequests() {
        return kickRequests;
    }

    public static SetMultimap<String, String> getLeaveRequests() {
        return leaveRequests;
    }

    public static GIMSeasonData getSeasonData() {
        return seasonData;
    }

    public static void setSeasonData(GIMSeasonData seasonData) {
        GIM.seasonData = seasonData;
    }

    public static void setGroupData(ImmutableMap<String, GIMGroup> groupData) {
        GIM.groupData = groupData;
    }

    public static ImmutableMap<String, GIMGroup> getGroupData() {
        if (groupData == null)
            return ImmutableMap.of();
        return groupData;
    }

    public static void setGroupMemberData(ImmutableMap<String, GIMGroup> groupMemberData) {
        GIM.groupMemberData = groupMemberData;
    }

    public static ImmutableMap<String, GIMGroup> getGroupMemberData() {
        if (groupMemberData == null)
            return ImmutableMap.of();
        return groupMemberData;
    }

    public static void setWinnerData(ImmutableList<GIMSeasonWinner> winnerData) {
        GIM.winnerData = winnerData;
    }

    public static ImmutableList<GIMSeasonWinner> getWinnerData() {
        if (winnerData == null)
            return ImmutableList.of();
        return winnerData;
    }

    public static void setRankings(ImmutableList<GIMGroup> rankings) {
        GIM.rankings = rankings;
        CoresManager.getServiceProvider().addGameTask(highscores::resetLastRefresh);
    }

    public static ImmutableList<GIMGroup> getRankings() {
        if (rankings == null)
            return ImmutableList.of();
        return rankings;
    }
}