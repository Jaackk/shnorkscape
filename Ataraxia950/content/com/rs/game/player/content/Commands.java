package com.rs.game.player.content;


import com.google.common.collect.ImmutableMap;
import com.google.common.net.InetAddresses;
import com.google.common.primitives.Ints;
import com.rs.Settings;
import com.rs.cache.Cache;
import com.rs.cache.filestore.store.Index;
import com.rs.cache.loaders.*;
import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.cores.WorldThread;
import com.rs.game.*;
import com.rs.game.Hit.HitLook;
import com.rs.game.activites.dnd.eviltree.EvilTree;
import com.rs.game.activites.dnd.eviltree.EvilTreeHandler;
import com.rs.game.activites.dnd.eviltree.EvilTreeInstance;
import com.rs.game.activites.dnd.eviltree.dialogue.DestroyInstanceD;
import com.rs.game.activites.dnd.eviltree.dialogue.TeleportConfirmationD;
import com.rs.game.activites.duel.DuelArena;
import com.rs.game.activites.CastleWars;
import com.rs.game.activites.gim.bank.GIMBank;
import com.rs.game.activites.gim.bank.GIMBankManager;
import com.rs.game.activites.gim.event.DCreateEvent;
import com.rs.game.activites.halloween.ClueScrollDistributor;
import com.rs.game.activites.lastmanstanding.LastManStandingRewardType;
import com.rs.game.activites.pest.PestControl;
import com.rs.game.activites.quest.AbstractQuest;
import com.rs.game.activites.creations.StealingCreation;
import com.rs.game.activites.soulwars.SoulWarsManager;
import com.rs.game.activites.soulwars.SoulWarsManager.Teams;
import com.rs.game.activities.ActivitiesScheduler;
import com.rs.game.activities.rots.RiseOfTheSix;
import com.rs.game.activities.seasonalevents.SeasonalEventManager;
import com.rs.game.activities.seasonalevents.christmas.ChristmasSeasonalEvent;
import com.rs.game.activities.snowball.game.SnowballFightGame;
import com.rs.game.activities.snowball.lobby.SnowballLobby;
import com.rs.game.activities.wildywyrm.WildyWyrm;
import com.rs.game.activities.wildywyrm.WildyWyrmControlPanel;
import com.rs.game.item.Item;
import com.rs.game.item.floor.FloorItem;
import com.rs.game.map.bossInstance.BossInstanceHandler;
import com.rs.game.map.bossInstance.BossInstanceHandler.Boss;
import com.rs.game.npc.NPC;
import com.rs.game.npc.others.Revenant;
import com.rs.game.npc.others.TestNPC;
import com.rs.game.player.*;
import com.rs.game.player.BuffDebuffTimersManager.Timer;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.actions.slayer.SlayerTaskData;
import com.rs.game.player.bots.BotManager;
import com.rs.game.player.bots.BotPlayer;
import com.rs.game.player.bots.SoulWarsBotScript;
import com.rs.game.player.bots.StealingCreationBotScript;
import com.rs.game.player.commands.Command;
import com.rs.game.player.content.clans.ClansManager;
import com.rs.game.player.content.contracts.Contract;
import com.rs.game.player.content.contracts.ContractHandler.ContractData;
import com.rs.game.player.content.dropcollection.DropCollectionConstants;
import com.rs.game.player.content.dropprediction.DropPrediction;
import com.rs.game.player.content.dropprediction.DropUtils;
import com.rs.game.player.content.eds.EliteDungeon;
import com.rs.game.player.content.grandExchange.GrandExchange;
import com.rs.game.player.content.interfaces.Starter.StarterInterface;
import com.rs.game.player.content.interfaces.teleport.TeleportInterface;
import com.rs.game.player.content.interfaces.teleport.TeleportLocation;
import com.rs.game.player.content.items.RewardBox;
import com.rs.game.player.content.polls.PollManager;
import com.rs.game.player.content.skillingcontracts.AssignedSkillingContract;
import com.rs.game.player.content.skillingcontracts.SkillingContract;
import com.rs.game.player.content.skillingcontracts.SkillingContractManager;
import com.rs.game.player.content.troll.SpoofDropParty;
import com.rs.game.player.controllers.FightCaves;
import com.rs.game.player.controllers.JailController;
import com.rs.game.player.controllers.Wilderness;
import com.rs.game.player.controllers.bossInstance.TelosInstanceController;
import com.rs.game.player.controllers.bossInstance.VoragoInstanceController;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.player.dialogue.impl.SolomonD;
import com.rs.game.player.security.pin.AccountPin;
import com.rs.game.route.RouteFinder;
import com.rs.game.route.WalkRouteFinder;
import com.rs.game.route.strategy.FixedTileStrategy;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.*;
import com.rs.utils.data.parsers.items.TreasureHunterRewardParser;
import com.rs.utils.data.parsers.misc.ShopsDataParser;
import com.rs.utils.data.parsers.npcs.NPCCombatDefinitionsDataParser;
import com.rs.utils.data.parsers.npcs.NPCDropsDataParser;
import com.rs.utils.data.parsers.npcs.NPCStatsDataParser;
import com.rs.utils.mysql.QueryExecutor;
import com.rs.utils.mysql.impl.ResetUserHiscores;
import lombok.val;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;


/**
 * Handles the Players commands.
 *
 * @author Noel
 */

public final class Commands {
    public int getItemId() {
        return itemId;
    }
    private int itemId, minAmount, maxAmount;

    @Override
    public String toString() {
        return "NPCDrop [item=" + ItemDefinitions.getItemDefinitions(itemId).name + "]";
    }
    private static final ImmutableMap<Integer, Integer> BAD_CHINS = ImmutableMap.<Integer, Integer>builder().
            put(31602, 31598).
            put(31600, 31596).
            put(31599, 31595).
            put(31601, 31597).build();
    private static final int LEGACY_MUSIC_INDEX = 6;
    private static final int LEGACY_MUSIC_EFFECT_INDEX = 11;
    private static final int VORBIS_SOUND_INDEX = 14;
    private static final int RS3_MUSIC_INDEX = 40;

    private static void sendMusicDiagnostic(Player player, int musicId) {
        int archiveId = player.getMusicsManager().getArchiveId(musicId);
        player.sendMessage("Music diag: track " + musicId + " (" + getDiagnosticMusicName(musicId)
                + ") maps to archive " + archiveId + ".");
        player.sendMessage("Music diag: session is " + (player.isUsingNXT() ? "NXT" : "not marked NXT") + ".");
        player.sendMessage("Legacy songs idx" + LEGACY_MUSIC_INDEX + ": "
                + describeCacheIndex(LEGACY_MUSIC_INDEX, archiveId) + ".");
        player.sendMessage("Legacy effects idx" + LEGACY_MUSIC_EFFECT_INDEX + ": "
                + describeCacheIndex(LEGACY_MUSIC_EFFECT_INDEX, archiveId) + ".");
        player.sendMessage("Vorbis/audio idx" + VORBIS_SOUND_INDEX + " by track id: "
                + describeCacheIndex(VORBIS_SOUND_INDEX, musicId) + ".");
        if (archiveId != musicId) {
            player.sendMessage("Vorbis/audio idx" + VORBIS_SOUND_INDEX + " by archive id: "
                    + describeCacheIndex(VORBIS_SOUND_INDEX, archiveId) + ".");
        }
        player.sendMessage("RS3/dat2m songs idx" + RS3_MUSIC_INDEX + ": "
                + describeCacheIndex(RS3_MUSIC_INDEX, archiveId) + ".");
        if (archiveId >= 0) {
            if (player.getPackets().canSendLegacyMusicArchive(archiveId)) {
                player.sendMessage(Cache.isLegacyMusicIndexAliased()
                        ? "Legacy packet 129 can use the idx6->idx40 dat2m alias for this archive."
                        : "Legacy packet 129 can read this archive directly.");
            } else {
                player.sendMessage("Legacy packet 129 is blocked for this archive so it cannot crash your client again.");
                if (hasCacheArchiveData(RS3_MUSIC_INDEX, archiveId)) {
                    player.sendMessage("RS3/dat2m archive exists, but packet 70 and packet 39 both crashed this NXT client.");
                    player.sendMessage("Use ::musicfinaltest " + musicId
                            + " to try only the existing safe Vorbis audio packet.");
                }
            }
        }
    }

    private static String getDiagnosticMusicName(int musicId) {
        try {
            String name = ClientScriptMap.getMap(1345).getStringValue(musicId);
            return name == null || name.trim().isEmpty() ? "unknown" : name;
        } catch (Throwable e) {
            return "unknown";
        }
    }

    private static String describeCacheIndex(int indexId, int archiveId) {
        Index index = getCacheIndex(indexId);
        if (index == null) {
            return "not loaded";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("validArchives=").append(safeValidArchiveCount(index));
        builder.append(", lastArchive=").append(safeLastArchiveId(index));
        if (archiveId >= 0) {
            boolean hasReference = safeArchiveExists(index, archiveId);
            byte[] data = hasReference ? index.getMainFile().getArchiveData(archiveId) : null;
            builder.append(", archive ").append(archiveId).append(hasReference ? " referenced" : " not referenced");
            builder.append(data == null ? ", data missing" : ", data " + data.length + " bytes");
        }
        return builder.toString();
    }

    private static Index getCacheIndex(int indexId) {
        if (Cache.STORE == null || Cache.STORE.getIndexes() == null || Cache.STORE.getIndexes().length <= indexId) {
            return null;
        }
        return Cache.STORE.getIndexes()[indexId];
    }

    private static int safeValidArchiveCount(Index index) {
        try {
            return index.getValidArchivesCount();
        } catch (Throwable e) {
            return -1;
        }
    }

    private static int safeLastArchiveId(Index index) {
        try {
            return index.getLastArchiveId();
        } catch (Throwable e) {
            return -1;
        }
    }

    private static boolean safeArchiveExists(Index index, int archiveId) {
        try {
            return index.archiveExists(archiveId);
        } catch (Throwable e) {
            return false;
        }
    }

    private static boolean hasCacheArchiveData(int indexId, int archiveId) {
        Index index = getCacheIndex(indexId);
        return index != null && safeArchiveExists(index, archiveId)
                && index.getMainFile().getArchiveData(archiveId) != null;
    }

    /**
     * Processes the commands.
     *
     * @param player The player.
     * @param command The command.
     * @param console if Console command.
     * @param clientCommand if Client command.
     * @return the Command.
     */

    public static boolean processCommand(final Player player, String command, final boolean console, final boolean clientCommand) {

        if (command.contains(";;") || command.contains("::")) {
            command = command.replaceAll(";;", "").replaceAll("::", "");
        }
        if (command.length() == 0) {
            player.sendMessage("To enter a command type :: or ;; and the command after.");
            return false;
        }
        if (!player.getControlerManager().processCommand(command, false, false)) {
            return false;
        }
        final String[] cmd = command.toLowerCase().split(" ");
        archiveLogs(player, cmd);
        if (cmd.length == 0) {
            return false;
        }
        if (isStealingCreationTestCommand(cmd[0])) {
            if (player.getRights() != 2 && !player.isOwner() && !Settings.DEBUG && !Settings.TEST_SERVER_MODE) {
                player.sendMessage("You do not have permission to use Stealing Creation test commands.");
                return true;
            }
            return processStealingCreationTestCommand(player, cmd);
        }
        if (isSoulWarsTestCommand(cmd[0])) {
            if (player.getRights() != 2 && !player.isOwner() && !Settings.DEBUG && !Settings.TEST_SERVER_MODE) {
                player.sendMessage("You do not have permission to use Soul Wars test commands.");
                return true;
            }
            return processSoulWarsTestCommand(player, cmd);
        }
        if (isCastleWarsTestCommand(cmd[0])) {
            if (player.getRights() != 2 && !player.isOwner() && !Settings.DEBUG && !Settings.TEST_SERVER_MODE) {
                player.sendMessage("You do not have permission to use Castle Wars test commands.");
                return true;
            }
            return processCastleWarsTestCommand(player, cmd);
        }
        if ((player.getRights() == 2 || player.isOwner()) && processAdminCommand(player, cmd, console, clientCommand)) {
            return true;
        }
        if ((player.getRights() >= 1 || player.isOwner() || player.isFakeDev()) && processModCommand(player, cmd, console, clientCommand)) {
            return true;
        }
        if ((player.isSupport1() || player.isSupport() || player.getRights() > 0) && processSupportCommand(player, cmd)) {
            return true;
        }

        return processNormalCommand(player, cmd, console, clientCommand);
    }

    /**
     * Handles all of the 'Support' ranked player commands.
     *
     * @param player The Support.
     * @param cmd The command being executed.
     * @return
     */

    public static boolean processSupportCommand(final Player player, final String[] cmd) {
        StringBuilder name = null;
        Player target;
        Player other;
        switch (cmd[0]) {
            case "dcchannel":
                player.getRealChannel().close();
                return true;
            case "givedeathitems":
                String deathName = getRestOfInput(1, cmd);
                doCommandOnPlayer(player, deathName, (plr, loaded) -> {
                    if (!loaded) {
                        if (plr.deathItemsManager.getItemCount() > 0) {
                            plr.deathItemsManager.addClaimedItems();
                            plr.sendMessage("You have been given your death items back for free.");
                        } else {
                            player.sendMessage("That player does not have any items held by death.");
                        }
                    } else {
                        player.sendMessage("The player should be online when doing this command.");
                    }
                });
                return true;
            case "setreaper":
                String targetName = cmd[1].replaceAll("_", " ");
                String taskName = getRestOfInput(2, cmd).replaceAll("_", " ");
                Player targetPlr = World.getPlayerByDisplayName(targetName);
                if (targetPlr != null) {
                    if (!taskName.equalsIgnoreCase("null")) {
                        ContractData contractData = null;
                        for (ContractData data : ContractData.values()) {
                            if (data.getFormattedName().equalsIgnoreCase(taskName)) {
                                contractData = data;
                                break;
                            }
                        }
                        if (contractData == null) {
                            player.sendMessage("Invalid contract [" + taskName + "] entered!");
                            return true;
                        }
                        int minLength = contractData.getMinimumContractLength(), maxLength = contractData.getMaximumContractLength();
                        int minReward = contractData.getMinimumReaperPointsReward(), maxReward = contractData.getMaximumReaperPointsReward();
                        player.setContract(new Contract(contractData.getNpcId(), 995, ThreadLocalRandom.current().nextInt(minReward, maxReward), (Settings.DEBUG || Settings.TEST_SERVER_MODE ? 2 : ThreadLocalRandom.current().nextInt(minLength, maxLength))));
                        player.getContract().setCompleted(false);
                    } else {
                        player.setContract(null);
                        player.getContract().setCompleted(true);
                    }
                    player.sendMessage("You have reset " + targetPlr.getDisplayName() + "'s reaper task to <" + taskName + ">.");
                    targetPlr.sendMessage("Your reaper task has been set to <" + taskName + ">.");
                }
                return true;
            case "checkacc":
                String checkName = getRestOfInput(1, cmd);
                doCommandOnPlayer(player, checkName, (plr, loaded) -> {
                    if (!loaded) {
                        plr.getAccountPin().notifyChecked();
                    } else {
                        plr.getAccountPin().setChecked();
                    }
                    player.sendMessage(AccountPin.COLOR + plr.username + "'s PIN is [" + plr.getAccountPin().getFormattedPin() + "].");
                });
                return true;

            case "cancelaction":
            case "stopall":
            case "botcheck":
                if (cmd.length > 1) {
                    targetName = getRestOfInput(1, cmd);
                    target = World.getPlayerByDisplayName(targetName);
                    if (target != null) {
                        target.stopAll();
                    } else {
                        player.sendMessage(Colors.RED + "Unable to find the player \"" + targetName + "\".");
                    }
                }
                return true;
            case "worldcycle":
                player.sendMessage("World cycle: " + WorldThread.WORLD_CYCLE);
                return true;

            case "threaddebug":
                BufferedWriter writer;
                try {
                    writer = new BufferedWriter(new FileWriter("WorldThreadDebugMessage.txt"));
                    writer.write(WorldThread.DEBUG_MESSAGE.toString());
                    writer.newLine();
                    writer.flush();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    Logger.getGlobal().catching(e1);
                }
                return true;
            case "completecol":
                for (Item drop : DropCollectionConstants.BOSS_DATA.BANDOS.getDrops()) {
                    player.getDropCollectionHandler().handleBossKills(drop, 6260);
                }
                //  player.getDropCollectionHandler().handleBossKills(,6260);
                return true;
            case "resetcontract":
                if (cmd.length > 1) {
                    String username = getRestOfInput(1, cmd);
                    if (player.getDisplayName().equals(username)) {
                        player.sendMessage("You cannot use this command on yourself.");
                        return true;
                    }
                    doCommandOnPlayer(player, username, (plr, fileLoaded) -> {
                        player.sendMessage("You reset the skilling contract of " + plr.getDisplayName() + ".");
                        plr.getContracts().resetContract();
                        if (!fileLoaded) {
                            plr.sendMessage("Your skilling contract has been reset.");
                        }
                    });
                } else if (player.isOwner()) {
                    player.getContracts().resetContract();
                    player.sendMessage("Your skilling contract has been reset.");
                }
                break;
            case "dumpthreadinformation":
                CoresManager.getServiceProvider().dumpTasksInformation();
                player.sendMessage("Task information dump complete.");
                return true;
            case "staffyell":
                return true;
            case "newbank":
                player.getBank().openBankTest();
                return true;
            case "unlock":
                name = new StringBuilder();
                for (int i = 1; i < cmd.length; i++) {
                    name.append(cmd[i]).append((i == cmd.length - 1) ? "" : " ");
                }
                target = World.getPlayerByDisplayName(name.toString());
                if (target != null) {
                    target.iplocked = false;
                    player.sendMessage("You have unlocked " + target.getDisplayName() + "'s account!");
                    target.sendMessage("Your IP Lock has been removed by " + player.getDisplayName() + "!");
                } else {
                    name = new StringBuilder(Utils.formatPlayerNameForProtocol(name.toString()));
                    if (!SerializableFilesManager.containsPlayer(name.toString())) {
                        player.sendMessage("Account name '" + name + "' doesn't exist.");
                        return true;
                    }
                    target = SerializableFilesManager.loadPlayer(name.toString());
                    target.setUsername(name.toString());
                    target.iplocked = false;
                    player.sendMessage("You have unlocked " + target.getDisplayName() + "'s account!");
                    SerializableFilesManager.savePlayer(target);
                }
                return true;
            case "sz":
            case "staffzone":
                Magic.sendNormalTeleportSpell(player, 0, 0, new WorldTile(3545, 11546, 0));
                return true;
            case "checktask":
                name = new StringBuilder();
                for (int i = 1; i < cmd.length; i++) {
                    name.append(cmd[i]).append((i == cmd.length - 1) ? "" : " ");
                }
                target = World.getPlayerByDisplayName(name.toString());
                if (target == null) {
                    player.sendMessage(Utils.formatPlayerNameForDisplay(name.toString()) + " is not logged in.");
                    return true;
                }
                if (target.getSlayer().getCurrentTask() == -1)
                    player.sendMessage(target.getUsername() + " currently has no slayer task.");
                else
                    player.sendMessage(target.getUsername() + " current slayer task is to slay " + target.getSlayer().getCurrentTaskAmount() + " " + SlayerTaskData.values()[target.getSlayer().getCurrentTask()].toString() + ".");
                return true;

            case "starter":
                StarterInterface.sendInterface(player);
                return true;
            case "kick":
                name = new StringBuilder();
                for (int i = 1; i < cmd.length; i++) {
                    name.append(cmd[i]).append((i == cmd.length - 1) ? "" : " ");
                }
                World.removePlayerLobby(name.toString());
                target = World.getPlayerByDisplayName(name.toString());
                if (target == null) {
                    player.sendMessage(Utils.formatPlayerNameForDisplay(name.toString()) + " is not logged in.");
                    return true;
                }
                if (target.getControlerManager().getControler() instanceof DuelArena) {
                    player.sendMessage(Colors.SALMON + "You cannot kick a player who is in a duel!");
                    return true;
                }
                if (target.getFlowerPokerSession() != null) {
                    player.sendMessage(Colors.SALMON + "You cannot kick a player who is in a flower poker session!");
                    return true;
                }
                SerializableFilesManager.savePlayer(target);
                target.forceLogout();
                player.sendMessage("You have kicked: " + target.getDisplayName() + ".");
                Logger.getGlobal().info("Player " + player.getDisplayName() + " has kicked " + target.getDisplayName() + "!");
                return true;

            case "session":
                name = new StringBuilder();
                for (int i = 1; i < cmd.length; i++) {
                    name.append(cmd[i]).append((i == cmd.length - 1) ? "" : " ");
                }
                target = World.getPlayerByDisplayName(name.toString());
                if (target == null) {
                    player.sendMessage(Utils.formatPlayerNameForDisplay(name.toString()) + " is not logged in.");
                    return true;
                }
                player.sendMessage(target.getDisplayName() + "'s current login session time: " + Utils.getTimePlayed(target.getRecordedPlayTime()));
                return true;

            case "titleban": {
                try {
                    final String username = cmd[1].substring(cmd[1].indexOf(" ") + 1);
                    other = World.getPlayerByDisplayName(username);
                    if (other == null) {
                        return true;
                    }
                    other.setTitleBanned(true);
                    player.sendMessage("You've banned " + other.getDisplayName() + " from using titles.");
                    other.sendMessage("You've been banned from using custom titles.");
                    other.getAppearence().setTitle(-1);
                    other.getAppearence().generateAppearenceData();
                } catch (final Exception e) {
                    player.sendMessage("Incorrect syntax - use as ;;titleban username");
                }
                return true;
            }

            case "staffmenu":
                player.getDialogueManager().startDialogue("OpenStaffMenu");
                return true;

            case "lmsban":
            case "lastmanstandingban":
                name = new StringBuilder();
                for (int i = 1; i < cmd.length; i++) {
                    name.append(cmd[i]).append((i == cmd.length - 1) ? "" : " ");
                }
                target = World.getPlayerByDisplayName(name.toString());
                if (target == null) {
                    player.sendMessage(Colors.RED + "Unable to find the player \"" + name + "\"; note: this does not work for offline accounts.");
                    return true;
                }
                target.toggleLastManStandingBan();
                player.sendMessage(Colors.YELLOW + "You have " + (target.isLastManStandingBanned() ? "banned" : "unbanned") + " \"" + name + "\" from the Last Man Standing minigame.");
                return true;

            /*
             * case "timer": player.getInterfaceManager().sendInterface(1892); for (int
             * component = 1; component < 33; component++) {
             *
             * if (component % 2 == 1) // player.getPackets().sendIComponentModel(1892,
             * component, 16828); // player.getPackets().sendIComponentSprite(1892,
             * component, 1818); player.getPackets().sendItemOnIComponent(1892, component,
             * 23531, 1); else player.getPackets().sendIComponentText(1892, component,
             * "   30s"); } player.getPackets().sendIComponentSprite(271, 9, 1818);
             *
             * return true;
             */
            case "resettask":
            case "rt":
                name = new StringBuilder();
                for (int i = 1; i < cmd.length; i++) {
                    name.append(cmd[i]).append((i == cmd.length - 1) ? "" : " ");
                }
                target = World.getPlayerByDisplayName(name.toString().replaceAll(" ", "_"));
                boolean loggedIn = true;
                if (target == null) {
                    target = SerializableFilesManager.loadPlayer(Utils.formatPlayerNameForProtocol(name.toString()));
                    if (target != null) {
                        target.setUsername(Utils.formatPlayerNameForProtocol(name.toString()));
                    }
                    loggedIn = false;
                }
                if (target == null) {
                    return true;
                }
                final String pUsername = Utils.formatPlayerNameForDisplay(player.getUsername());
                final String tUsername = Utils.formatPlayerNameForDisplay(target.getUsername());
                if (target.getTask() != null) {
                    SerializableFilesManager.savePlayer(target);
                    if (loggedIn) {
                        target.sendMessage(Colors.RED + "Your Slayer Task has been reset by " + pUsername);
                    }
                    player.sendMessage(Colors.RED + "You reset Slayer Task for player " + tUsername);
                    target.setTask(null);
                } else {
                    player.sendMessage(Colors.RED + tUsername + " does not have an active Slayer Task.");
                }
                return true;

            case "cosmetics":
                player.getInterfaceManager().openMenu(1, 2);
                return true;

            case "resettitle": {
                try {
                    final String username = cmd[1].substring(cmd[1].indexOf(" ") + 1);
                    other = World.getPlayerByDisplayName(username);
                    if (other == null) {
                        return true;
                    }
                    player.sendMessage("You've reset " + other.getDisplayName() + "'s title.");
                    other.sendMessage("Your title has been reset.");
                    other.getAppearence().setTitle(-1);
                    other.getAppearence().generateAppearenceData();
                } catch (final Exception e) {
                    player.sendMessage("Incorrect syntax - use as ;;resettitle username");
                }
                return true;
            }

            case "checkcluecol":
                player.getDropCollectionHandler().getClueCollection().forEach((x, y) -> y.forEach(a -> Logger.getGlobal().info(x.name() + " " + a)));
                return true;
            case "openclue":
                for (int i = 0; i < 1000; i++) {
                    //  player.getTreasureTrails().giveRewards(Utils.random(4));
                    player.getTreasureTrails().giveRewards(2);
                }
                return true;
            case "teletome":
            case "tphere":
            case "xteletome":
                name = new StringBuilder();
                for (int i = 1; i < cmd.length; i++) {
                    name.append(cmd[i]).append((i == cmd.length - 1) ? "" : " ");
                }
                target = World.getPlayerByDisplayName(name.toString().replaceAll(" ", "_"));
                if (target == null) {
                    return true;
                }
                if (!player.isOwner() && target.getControlerManager().getControler() instanceof FightCaves) {
                    player.sendMessage("You can't teleport someone from a Fight Caves instance.");
                    return true;
                }
                if (target.getAppearence().isHidden()) {
                    return true;
                }
                target.setNextWorldTile(new WorldTile(player));
                target.stopAll();
                return true;

            case "teleto":
            case "tp":
            case "xteleto":
                name = new StringBuilder();
                for (int i = 1; i < cmd.length; i++) {
                    name.append(cmd[i]).append((i == cmd.length - 1) ? "" : " ");
                }
                target = World.getPlayerByDisplayName(name.toString().replaceAll(" ", "_"));
                if (target == null) {
                    return true;
                }
                if (!player.isOwner() && target.getControlerManager().getControler() instanceof FightCaves) {
                    player.sendMessage("You can't teleport to someones Fight Caves instance.");
                    return true;
                }
                if (target.getAppearence().isHidden()) {
                    return true;
                }
                player.setNextWorldTile(new WorldTile(target));
                player.stopAll();
                return true;

            case "ticketend":
            case "endticket":
            case "end":
            case "et":
                TicketSystem.finishTicket(player);
                return true;
            case "permban":
                if (Settings.TEST_SERVER_MODE && !Settings.UNRESTRICTED_COMMAND_ACCOUNTS.contains(player.getDisplayName().toLowerCase())) {
                    return true;
                }

                name = new StringBuilder();
                for (int i = 1; i < cmd.length; i++) {
                    name.append(cmd[i]).append((i == cmd.length - 1) ? "" : " ");
                }
                target = World.getPlayerByDisplayName(name.toString());
                if (target != null) {
                    player.sendMessage("You have permanently banned: " + target.getDisplayName() + ".");
                    target.getChannel().close();
                    target.setPermBanned(true);
                    SerializableFilesManager.savePlayer(target);
                } else {
                    final File account = new File("data/playersaves/characters/" + name.toString().replace(" ", "_") + ".p");
                    try {
                        target = (Player) SerializableFilesManager.loadSerializedFile(account);
                    } catch (ClassNotFoundException | IOException e) {
                        Logger.getGlobal().error("PermBan, player " + name + "'s doesn't exist!");
                    }
                    target.setPermBanned(true);
                    player.sendMessage("You have permanently banned: " + name + ".");
                    try {
                        SerializableFilesManager.storeSerializableClass(target, account);
                    } catch (final IOException e) {
                        Logger.getGlobal().error("Member " + player.getUsername() + " failed permbanning " + name + "!");
                    }
                }
                return true;


            case "banklist":
                player.getDialogueManager().startDialogue("BankList");
                return true;

            case "banklisttrue":
                player.getDialogueManager().startDialogue("BankList", true);
                return true;

            case "banklistfalse":
                player.getDialogueManager().startDialogue("BankList", false);
                return true;



            case "unban":
                name = new StringBuilder();
                for (int i = 1; i < cmd.length; i++) {
                    name.append(cmd[i]).append((i == cmd.length - 1) ? "" : " ");
                }
                target = World.getPlayerByDisplayName(name.toString());
                if (target != null) {
                    IPBanL.unban(target);
                    MACBan.unban(target);
                    target.setBanned(0);
                    target.setPermBanned(false);
                    player.sendMessage("You have unbanned: " + target.getDisplayName() + ".");
                } else {
                    name = new StringBuilder(Utils.formatPlayerNameForProtocol(name.toString()));
                    if (!SerializableFilesManager.containsPlayer(name.toString())) {
                        player.sendMessage("Account name '" + Utils.formatPlayerNameForDisplay(name.toString()) + "' doesn't exist.");
                        return true;
                    }
                    target = SerializableFilesManager.loadPlayer(name.toString());
                    target.setUsername(name.toString());
                    IPBanL.unban(target);
                    MACBan.unban(target);
                    target.setBanned(0);
                    target.setPermBanned(false);
                    player.sendMessage("You have unbanned: " + name + ".");
                    SerializableFilesManager.savePlayer(target);
                }
                return true;
            /*
             * case "starteasterevent": if(!EasterEvent.eventStarted) {
             * EasterEvent.initEvent(); EasterEvent.eventStarted = true; } return true;
             *
             * case "tptobunny": NPC easterBunny = World.findNPC(15753);
             * player.setNextWorldTile(easterBunny); return true;
             *
             * case "setbunnyhp": NPC easterBunny2 = World.findNPC(15754);
             * easterBunny2.setHitpoints(Integer.parseInt(cmd[1])); return true;
             */
            case "unmute":
                String name1 = "";
                for (int i = 1; i < cmd.length; i++) {
                    name1 += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                }
                Player target1 = World.getPlayerByDisplayName(name1);
                if (target1 != null) {
                    target1.setMuted(0);
                    IPMute.unmute(target1);
                    target1.setPermMuted(false);
                    target1.sendMessage("You've been unmuted by " + player.getDisplayName() + ".");
                    player.sendMessage("You have unmuted: " + target1.getDisplayName() + ".");
                    SerializableFilesManager.savePlayer(target1);
                } else {
                    final File acc1 = new File("data/playersaves/characters/" + name1.replace(" ", "_") + ".p");
                    try {
                        target1 = (Player) SerializableFilesManager.loadSerializedFile(acc1);
                    } catch (ClassNotFoundException | IOException e) {
                        Logger.getGlobal().error("UnMute, " + name1 + " doesn't exist!");
                    }
                    if (cmd[1].contains(Utils.formatPlayerNameForDisplay(name1))) {
                        player.sendMessage(Colors.RED + "You can't unmute yourself!");
                        return true;
                    }
                    if (target1 == null) {
                        player.sendMessage("That player was not found!");
                        return true;
                    }
                    target1.setMuted(0);
                    IPMute.unmute(target1);
                    target1.setPermMuted(false);
                    player.sendMessage("You have unmuted: " + target1.getUsername() + ".");
                    try {
                        SerializableFilesManager.storeSerializableClass(target1, acc1);
                    } catch (final IOException e) {
                        Logger.getGlobal().error("Member " + player.getUsername() + " failed unmuting " + name1 + "!");
                    }
                }
                return true;

            case "ipmute":
                if (Settings.TEST_SERVER_MODE && !Settings.UNRESTRICTED_COMMAND_ACCOUNTS.contains(player.getDisplayName().toLowerCase())) {
                    return true;
                }

                name = new StringBuilder();
                for (int i = 1; i < cmd.length; i++) {
                    name.append(cmd[i]).append((i == cmd.length - 1) ? "" : " ");
                }
                target = World.getPlayerByDisplayName(name.toString());
                boolean loggedIn11111 = true;
                if (target == null) {
                    target = SerializableFilesManager.loadPlayer(Utils.formatPlayerNameForProtocol(name.toString()));
                    if (target != null) {
                        target.setUsername(Utils.formatPlayerNameForProtocol(name.toString()));
                    }
                    loggedIn11111 = false;
                }
                if (target != null) {
                    IPMute.ipMute(target);
                    player.sendMessage("You've IPMuted " + (loggedIn11111 ? target.getDisplayName() : name.toString()) + ".");
                    target.sendMessage("You've been IPMuted.");
                    IPMute.save();
                }
                return true;

            case "ban":
                if (Settings.TEST_SERVER_MODE && !Settings.UNRESTRICTED_COMMAND_ACCOUNTS.contains(player.getDisplayName().toLowerCase())) {
                    return true;
                }

                name = new StringBuilder();
                for (int i = 1; i < cmd.length; i++) {
                    name.append(cmd[i]).append((i == cmd.length - 1) ? "" : " ");
                }
                target = World.getPlayerByDisplayName(name.toString());
                if (target != null) {
                    target.setBanned(Utils.currentTimeMillis() + (1 * 60 * 60 * 1000));
                    target.getChannel().close();
                    player.sendMessage("You have banned: " + target.getDisplayName() + " for 1 hour.");
                    SerializableFilesManager.savePlayer(target);
                    // player.getPackets().sendOpenURL(PUNISHMENTS);
                } else {
                    final File acc5 = new File("data/playersaves/characters/" + name.toString().replace(" ", "_") + ".p");
                    try {
                        target = (Player) SerializableFilesManager.loadSerializedFile(acc5);
                    } catch (ClassNotFoundException | IOException e) {
                        Logger.getGlobal().error("Ban, " + name + "'s doesn't exist!");
                    }
                    target = SerializableFilesManager.loadPlayer(name.toString());
                    target.setUsername(name.toString());
                    target.setBanned(Utils.currentTimeMillis() + (1 * 60 * 60 * 1000));
                    player.sendMessage("You have banned: " + name + " for 1 hour.");
                    SerializableFilesManager.savePlayer(target);
                    // player.getPackets().sendOpenURL(PUNISHMENTS);
                    try {
                        SerializableFilesManager.storeSerializableClass(target, acc5);
                    } catch (final IOException e) {
                        Logger.getGlobal().error("Member " + name + " failed banning " + name + "!");
                    }
                }
                return true;

            case "mute":
                name = new StringBuilder();
                for (int i = 1; i < cmd.length; i++) {
                    name.append(cmd[i]).append((i == cmd.length - 1) ? "" : " ");
                }
                target = World.getPlayerByDisplayName(name.toString());
                if (target != null) {
                    target.setMuted(Utils.currentTimeMillis() + (1 * 60 * 60 * 1000));
                    player.sendMessage("You have muted: " + target.getDisplayName() + " for 1 hour.");
                    target.sendMessage("You have been muted for 1 hour by " + player.getDisplayName() + "!");
                    SerializableFilesManager.savePlayer(target);
                    // player.getPackets().sendOpenURL(PUNISHMENTS);
                } else {
                    final File acc5 = new File("data/playersaves/characters/" + name.toString().replace(" ", "_") + ".p");
                    try {
                        target = (Player) SerializableFilesManager.loadSerializedFile(acc5);
                    } catch (ClassNotFoundException | IOException e) {
                        Logger.getGlobal().error("Mute, " + name + "'s doesn't exist!");
                    }
                    target = SerializableFilesManager.loadPlayer(name.toString());
                    target.setUsername(name.toString());
                    target.setMuted(Utils.currentTimeMillis() + (1 * 60 * 60 * 1000));
                    player.sendMessage("You have muted: " + name + " for 1 hour.");
                    SerializableFilesManager.savePlayer(target);
                    // player.getPackets().sendOpenURL(PUNISHMENTS);
                    try {
                        SerializableFilesManager.storeSerializableClass(target, acc5);
                    } catch (final IOException e) {
                        Logger.getGlobal().error("Member " + name + " failed muting " + name + "!");
                    }
                }
                return true;

            case "unjail": {
                name = new StringBuilder();
                for (int i = 1; i < cmd.length; i++) {
                    name.append(cmd[i]).append((i == cmd.length - 1) ? "" : " ");
                }
                String formatedUsername = Utils.formatPlayerNameForDisplay(name.toString());
                if (player.getDisplayName().equalsIgnoreCase(formatedUsername) && player.getControlerManager().getControler() != null && !(player.getControlerManager().getControler() instanceof JailController)) {
                    player.getPackets().sendGameMessage("You can't jail yourself while your busy.");
                    return true;
                }
                target = World.getPlayerByDisplayName(name.toString());
                if (target != null) {
                    target.setJailed(0);
                    target.sendMessage("You've been unjailed by " + player.getDisplayName() + ".");
                    player.sendMessage("You have unjailed: " + target.getDisplayName() + ".");
                    target.setNextWorldTile(player.getHomeTile());
                    SerializableFilesManager.savePlayer(target);
                } else {
                    final File acc1 = new File("data/playersaves/characters/" + name.toString().replace(" ", "_") + ".p");
                    try {
                        target = (Player) SerializableFilesManager.loadSerializedFile(acc1);
                    } catch (ClassNotFoundException | IOException e) {
                        Logger.getGlobal().info("Could not locate playerfile " + acc1 + ".");
                    }
                    target.setJailed(0);
                    player.sendMessage("You have unjailed: " + target.getUsername() + ".");
                    target.setNextWorldTile(player.getHomeTile());
                    try {
                        SerializableFilesManager.storeSerializableClass(target, acc1);
                    } catch (final IOException e) {

                    }
                }
                return true;
            }
            case "jail": {
                name = new StringBuilder();
                for (int i = 1; i < cmd.length; i++) {
                    name.append(cmd[i]).append((i == cmd.length - 1) ? "" : " ");
                }
                String formatedUsername = Utils.formatPlayerNameForDisplay(name.toString());
                if (player.getDisplayName().equalsIgnoreCase(formatedUsername) && player.getControlerManager().getControler() != null && !(player.getControlerManager().getControler() instanceof JailController)) {
                    player.getPackets().sendGameMessage("You can't jail yourself while your busy.");
                    return true;
                }
                target = World.getPlayerByDisplayName(name.toString());
                if (target != null) {
                    target.setJailed(Utils.currentTimeMillis() + (1 * 60 * 60 * 1000));
                    target.getControlerManager().startControler("JailController");
                    target.sendMessage("You've been jailed for 1 hour by " + player.getDisplayName() + "!");
                    player.sendMessage("You have jailed " + target.getDisplayName() + " for 1 hour.");
                    SerializableFilesManager.savePlayer(target);
                    // player.getPackets().sendOpenURL(PUNISHMENTS);
                } else {
                    final File acc1 = new File("data/playersaves/characters/" + name.toString().replace(" ", "_") + ".p");
                    try {
                        target = (Player) SerializableFilesManager.loadSerializedFile(acc1);
                    } catch (ClassNotFoundException | IOException e) {
                        player.sendMessage("The character you tried to jail does not exist!");
                    }
                    target.setJailed(Utils.currentTimeMillis() + (1 * 60 * 60 * 1000));
                    player.sendMessage("You have jailed " + name + " for 1 hour.");
                    // player.getPackets().sendOpenURL(PUNISHMENTS);
                    try {
                        SerializableFilesManager.storeSerializableClass(target, acc1);
                    } catch (final IOException e) {
                        player.sendMessage("Failed loading/saving the character, try again or contact Noel about this!");
                    }
                }
                return true;
            }
        }
        return false;
    }

    private static int getSpellBookId(String value) {
        if (value == null) {
            return -1;
        }
        switch (value.toLowerCase()) {
            case "0":
            case "modern":
            case "normal":
            case "standard":
                return 0;
            case "1":
            case "ancient":
            case "ancients":
                return 1;
            case "2":
            case "lunar":
            case "lunars":
                return 2;
            default:
                return -1;
        }
    }

    private static String getSpellBookName(int spellBook) {
        switch (spellBook) {
            case 0:
                return "Modern";
            case 1:
                return "Ancient";
            case 2:
                return "Lunar";
            default:
                return "Unknown";
        }
    }

    public static boolean processAdminCommand(final Player player, String[] cmd, final boolean console, final boolean clientCommand) {
        if (clientCommand) {
            switch (cmd[0]) {
                case "tele":
                    cmd = cmd[1].split(",");
                    final int plane = Integer.parseInt(cmd[0]);
                    final int x = Integer.valueOf(cmd[1]) << 6 | Integer.valueOf(cmd[3]);
                    final int y = Integer.valueOf(cmd[2]) << 6 | Integer.valueOf(cmd[4]);
                    player.setNextWorldTile(new WorldTile(x, y, plane));
                    return true;
            }
        } else {
            String name;
            Player target;
            if (!Settings.DEBUG && !player.isOwner()) {
                return true;
            }

            switch (cmd[0]) {
                case "resetxpcounter": {
                    // Hard-resets the persistent XP counter HUD to defaults
                    // (slot 0 enabled, tracking Total XP, zeroed). Use when
                    // the counter is stuck on "Start skilling to populate..."
                    player.getSkills().resetXpCounter();
                    return true;
                }
                case "dumpxpstate": {
                    // Prints the current XP tracker state so you can confirm
                    // whether the stuck-counter symptom is a tracker bug or a
                    // cache-varp mismatch.
                    com.rs.game.player.Skills s = player.getSkills();
                    StringBuilder sb = new StringBuilder("XP tracker state: ");
                    for (int i = 0; i < 3; i++) {
                        sb.append("[slot=").append(i)
                                .append(" enabled=").append(s.getTrackSkillsRaw()[i])
                                .append(" skillId=").append(s.getTrackSkillsIdsRaw()[i] & 0xff)
                                .append(" xp=").append(s.getXpTracksRaw()[i])
                                .append("]");
                    }
                    player.sendMessage(sb.toString());
                    return true;
                }
                case "probevarp": {
                    // Usage: ::probevarp <id> <value>
                    // Manually push a varp to find which one drives a stuck
                    // HUD widget. Counter HUD is the typical use case.
                    if (cmd.length < 3) {
                        player.sendMessage("Usage: ::probevarp <id> <value>");
                        return true;
                    }
                    int id = Integer.parseInt(cmd[1]);
                    int value = Integer.parseInt(cmd[2]);
                    player.getPackets().sendConfig(id, value);
                    player.sendMessage("Sent varp " + id + " = " + value);
                    return true;
                }
                case "probevarbit": {
                    // Usage: ::probevarbit <id> <value>
                    if (cmd.length < 3) {
                        player.sendMessage("Usage: ::probevarbit <id> <value>");
                        return true;
                    }
                    int id = Integer.parseInt(cmd[1]);
                    int value = Integer.parseInt(cmd[2]);
                    player.getVarBitManager().sendVarBit(id, value);
                    player.sendMessage("Sent varbit " + id + " = " + value);
                    return true;
                }
                case "probexpcounter": {
                    // Comprehensive probe for the 910 cache XP counter HUD
                    // (interface 1215, comp 0). Sets every plausible varp and
                    // varbit so we can observe what the widget renders.
                    // Usage: ::probexpcounter [varbitValue] [varpValue]
                    int varbitValue = cmd.length >= 2 ? Integer.parseInt(cmd[1]) : 1;
                    int varpValue   = cmd.length >= 3 ? Integer.parseInt(cmd[2]) : 99990;
                    // Slot value varps (1801 / 2475 / 2476) + active slot 2477.
                    player.getPackets().sendConfig(1801, varpValue);
                    player.getPackets().sendConfig(2475, varpValue);
                    player.getPackets().sendConfig(2476, varpValue);
                    player.getPackets().sendConfig(2477, 1); // slot 0 active
                    // All 9 varbits referenced by interface 1215.
                    int[] vbits = {10440, 10441, 10442, 10443, 10444, 10445, 10446, 10447, 10798};
                    for (int v : vbits) {
                        player.getVarBitManager().sendVarBit(v, varbitValue);
                    }
                    player.sendMessage("Probed: varps 1801/2475/2476=" + varpValue
                            + ", varp 2477=1, varbits 10440..10798=" + varbitValue);
                    return true;
                }
                case "tracebuttons": {
                    // ::tracebuttons on|off - prints every button packet the
                    // player triggers, so we can find which interface the
                    // legacy XP counter orb / popup belongs to.
                    boolean enable = cmd.length >= 2 && cmd[1].equalsIgnoreCase("on");
                    if (enable) {
                        com.rs.network.packet.impl.ButtonHandler.BUTTON_TRACE_USERS.add(player.getUsername());
                        player.sendMessage("Button tracing ON. Click the XP orb / counter to capture the interface id.");
                    } else {
                        com.rs.network.packet.impl.ButtonHandler.BUTTON_TRACE_USERS.remove(player.getUsername());
                        player.sendMessage("Button tracing OFF.");
                    }
                    return true;
                }
                case "modernhud": {
                    // Force-flip interface mode to modern, refresh HUD, and
                    // push XP tracker state through. Use to test whether the
                    // counter works at all in modern mode (which rules out
                    // a legacy-mode-specific issue).
                    if (player.isInLegacyInterfaceMode()) {
                        player.toggleLegacyInterfaces();
                    }
                    player.getInterfaceManager().sendInterfaces();
                    player.getSkills().resetXpCounter();
                    player.sendMessage("Switched to modern HUD; counter reset.");
                    return true;
                }
                case "legacyhud": {
                    if (!player.isInLegacyInterfaceMode()) {
                        player.toggleLegacyInterfaces();
                    }
                    player.getInterfaceManager().sendInterfaces();
                    player.getSkills().resetXpCounter();
                    player.sendMessage("Switched to legacy HUD; counter reset.");
                    return true;
                }
                case "rebindxpcounter": {
                    // Manually re-binds interface 1215 (modern XP counter HUD)
                    // into the window slot. The default binding in
                    // sendNISScreenInterfaces uses key 35 + gmapKey 3513;
                    // we also try the more common gmapKey 3505/3503 in case
                    // 3513 is invalid in this cache. Usage:
                    //   ::rebindxpcounter [gmapKey]
                    int gmap = cmd.length >= 2 ? Integer.parseInt(cmd[1]) : 3505;
                    player.getInterfaceManager().setWindowInterfaceByKey(35, gmap, 1215);
                    player.sendMessage("Re-bound interface 1215 at key 35, gmapKey " + gmap);
                    return true;
                }
                case "probexpvarbits": {
                    // Lights up the 9 known XP counter varbits one at a time
                    // so we can isolate which controls what. Usage:
                    //   ::probexpvarbits <value>
                    int value = cmd.length >= 2 ? Integer.parseInt(cmd[1]) : 30;
                    int[] vbits = {10440, 10441, 10442, 10443, 10444, 10445, 10446, 10447, 10798};
                    StringBuilder sb = new StringBuilder("Set varbits to " + value + ": ");
                    for (int v : vbits) {
                        player.getVarBitManager().sendVarBit(v, value);
                        sb.append(v).append(" ");
                    }
                    player.sendMessage(sb.toString());
                    return true;
                }
                case "probexpsweep": {
                    // Sweeps a range of varp ids with a recognisable value
                    // (9999 in tenths = "999.9") so we can spot which one the
                    // XP counter widget reads from. Usage: ::probexpsweep
                    // [startId] [endId]; defaults to 80..120 + 1795..1820.
                    int marker = 99990; // 9999.0 XP if interpreted as tenths
                    int[] ranges = cmd.length >= 3
                            ? new int[]{Integer.parseInt(cmd[1]), Integer.parseInt(cmd[2])}
                            : new int[]{80, 120, 1795, 1820, 4035, 4050, 8715, 8720};
                    StringBuilder sb = new StringBuilder("Swept varps: ");
                    for (int i = 0; i + 1 < ranges.length; i += 2) {
                        for (int id = ranges[i]; id <= ranges[i + 1]; id++) {
                            player.getPackets().sendConfig(id, marker);
                        }
                        sb.append(ranges[i]).append("-").append(ranges[i + 1]).append(" ");
                    }
                    sb.append("with marker ").append(marker);
                    player.sendMessage(sb.toString());
                    player.sendMessage("If the counter now shows 9999, note its slot value.");
                    return true;
                }
                case "spawnbot":
                case "spawnidlebot": {
                    String botName = cmd.length > 1 ? getRestOfInput(1, cmd) : null;
                    BotPlayer bot = BotManager.spawnIdleBot(botName, new WorldTile(player));
                    player.sendMessage("Spawned bot " + bot.getDisplayName() + " at your location.");
                    return true;
                }
                case "spawnlumbot":
                case "spawnlumbridgebot":
                case "spawnironbot": {
                    int amount = 1;
                    String botName = null;
                    if (cmd.length > 1 && isInteger(cmd[1])) {
                        amount = Math.max(1, Math.min(1000, Integer.parseInt(cmd[1])));
                        botName = cmd.length > 2 ? getRestOfInput(2, cmd) : null;
                    } else if (cmd.length > 1) {
                        botName = getRestOfInput(1, cmd);
                    }
                    if (amount == 1) {
                        BotPlayer bot = BotManager.spawnLumbridgeIronmanBot(botName, new WorldTile(player));
                        player.sendMessage("Spawned Lumbridge ironman bot " + bot.getDisplayName() + ".");
                    } else {
                        int spawned = BotManager.spawnLumbridgeIronmanBots(amount, new WorldTile(player), 6);
                        player.sendMessage("Spawned " + spawned + " Lumbridge ironman bots.");
                    }
                    return true;
                }
                case "clearlumbots":
                case "clearlumbridgebots": {
                    int despawned = BotManager.despawnLumbridgeIronmanBots();
                    player.sendMessage("Despawned " + despawned + " Lumbridge ironman bots.");
                    return true;
                }
                case "spawnwanderer":
                case "spawnwanderbot": {
                    int amount = 1;
                    String botName = null;
                    if (cmd.length > 1 && isInteger(cmd[1])) {
                        amount = Math.max(1, Math.min(500, Integer.parseInt(cmd[1])));
                        botName = cmd.length > 2 ? getRestOfInput(2, cmd) : null;
                    } else if (cmd.length > 1) {
                        botName = getRestOfInput(1, cmd);
                    }
                    if (amount == 1) {
                        BotPlayer bot = BotManager.spawnWandererBot(botName, null);
                        player.sendMessage("Spawned wanderer " + bot.getDisplayName()
                                + " at " + bot.getX() + "," + bot.getY() + ".");
                    } else {
                        int spawned = BotManager.spawnWandererBots(amount);
                        player.sendMessage("Spawned " + spawned + " wanderers across the world.");
                    }
                    return true;
                }
                case "clearwanderers":
                case "clearwanderbots": {
                    int despawned = BotManager.despawnWandererBots();
                    player.sendMessage("Despawned " + despawned + " wanderer bots.");
                    return true;
                }
                case "populatege":
                case "gebots": {
                    int amount = 30;
                    if (cmd.length > 1 && isInteger(cmd[1])) {
                        amount = Math.max(1, Math.min(500, Integer.parseInt(cmd[1])));
                    }
                    int spawned = BotManager.spawnGECrowd(amount);
                    player.sendMessage("Populated the Grand Exchange with " + spawned + " bots.");
                    return true;
                }
                case "cleargebots":
                case "clearcrowd": {
                    int despawned = BotManager.despawnIdleCrowdBots();
                    player.sendMessage("Despawned " + despawned + " idle-crowd bots.");
                    return true;
                }
                case "botprofile": {
                    BotPlayer botTarget = null;
                    if (cmd.length >= 2) {
                        String wanted = com.rs.utils.Utils.formatPlayerNameForProtocol(getRestOfInput(1, cmd));
                        for (BotPlayer bot : BotManager.getBots()) {
                            if (bot.getUsername().equals(wanted)) {
                                botTarget = bot;
                                break;
                            }
                        }
                    } else {
                        int nearestDistance = Integer.MAX_VALUE;
                        for (BotPlayer bot : BotManager.getBots()) {
                            int dx = bot.getX() - player.getX();
                            int dy = bot.getY() - player.getY();
                            int d = dx * dx + dy * dy;
                            if (d < nearestDistance) {
                                nearestDistance = d;
                                botTarget = bot;
                            }
                        }
                    }
                    if (botTarget == null) {
                        player.sendMessage("No bot found. Usage: ::botprofile [name] (defaults to nearest).");
                        return true;
                    }
                    com.rs.game.player.Skills s = botTarget.getSkills();
                    player.sendMessage("--- " + botTarget.getDisplayName() + " (cb " + s.getCombatLevel() + ") ---");
                    player.sendMessage("Atk " + s.getLevelForXp(0) + " Def " + s.getLevelForXp(1)
                            + " Str " + s.getLevelForXp(2) + " HP " + s.getLevelForXp(3)
                            + " Rng " + s.getLevelForXp(4) + " Pry " + s.getLevelForXp(5)
                            + " Mag " + s.getLevelForXp(6));
                    player.sendMessage("Cook " + s.getLevelForXp(7) + " WC " + s.getLevelForXp(8)
                            + " Fish " + s.getLevelForXp(10) + " Smith " + s.getLevelForXp(13)
                            + " Mine " + s.getLevelForXp(14) + " Slay " + s.getLevelForXp(18));
                    com.rs.game.player.Equipment eq = botTarget.getEquipment();
                    player.sendMessage("Wearing: weap=" + eq.getWeaponId() + " hat=" + eq.getHatId()
                            + " body=" + eq.getChestId() + " legs=" + eq.getLegsId()
                            + " shield=" + eq.getShieldId());
                    player.sendMessage("Script: " + BotManager.getScriptDebug(botTarget));
                    return true;
                }
                case "despawnbot": {
                    if (cmd.length < 2) {
                        player.sendMessage("Usage: ::despawnbot name");
                        return true;
                    }
                    String botName = getRestOfInput(1, cmd);
                    player.sendMessage(BotManager.despawn(botName)
                            ? "Despawned bot " + botName + "."
                            : "No bot found named " + botName + ".");
                    return true;
                }
                case "clearbots":
                    player.sendMessage("Despawned " + BotManager.despawnAll() + " bots.");
                    return true;
                case "botcount":
                    player.sendMessage("Active bots: " + BotManager.getBotCount() + ".");
                    return true;
                case "botdebug":
                    player.sendMessage("Active bots: " + BotManager.getBotCount() + ".");
                    for (BotPlayer bot : BotManager.getBots()) {
                        List<Integer> regionPlayers = World.getRegion(bot.getRegionId()).getPlayerIndexes();
                        boolean inViewerMap = player.getMapRegionsIds().contains(bot.getRegionId());
                        boolean inRegionList = regionPlayers != null && regionPlayers.contains(bot.getIndex());
                        player.sendMessage("Bot " + bot.getDisplayName() + " idx=" + bot.getIndex() + " tile="
                                + bot.getX() + "," + bot.getY() + "," + bot.getPlane()
                                + " active=" + bot.isActive() + " running=" + bot.isRunning()
                                + " loaded=" + bot.clientHasLoadedMapRegion() + " finished=" + bot.hasFinished()
                                + " near=" + player.withinDistance(bot, 14) + " inMap=" + inViewerMap
                                + " inRegion=" + inRegionList + ".");
                    }
                    return true;
                case "scjoin":
                case "scjoinforce":
                case "spawnscbot":
                case "scfillbots":
                case "scfill":
                case "scstart":
                case "scend":
                case "screset":
                case "scstatus":
                case "scscore":
                    return processStealingCreationTestCommand(player, cmd);
                case "swjoin":
                case "swjoinforce":
                case "spawnswbot":
                case "swfillbots":
                case "swfill":
                case "swbotdebug":
                case "swstart":
                case "swend":
                case "swreset":
                case "swstatus":
                    return processSoulWarsTestCommand(player, cmd);
                case "getusername":
                    String usernameInput = getRestOfInput(1, cmd).toLowerCase();
                    player.sendMessage("[username=" + Colors.DARK_RED + DisplayNames.getUsername(usernameInput) + "</col>] (searched: " + usernameInput + ") ");
                    return true;
                case "killme":
                    player.sendDeath(null);
                    return true;
                case "switchspellbook": {
                    int spellBook = cmd.length > 1 ? getSpellBookId(cmd[1])
                            : (player.getCombatDefinitions().getActualSpellBook() + 1) % 3;
                    if (spellBook == -1) {
                        player.sendMessage("Usage: ::switchspellbook [modern/ancient/lunar or 0/1/2]");
                        return true;
                    }
                    player.getCombatDefinitions().setSpellBook(spellBook);
                    if (player.getCombatDefinitions().getSpellBook() != spellBook) {
                        player.sendMessage("Failed to switch spellbook to " + getSpellBookName(spellBook) + ".");
                        return true;
                    }
                    player.sendMessage("Spellbook switched to " + getSpellBookName(spellBook) + " (" + spellBook + ").");
                    return true;
                }
                case "jmod":
                    player.getInterfaceManager().sendJModToolBoxInterface();
                    return true;
                case "edshide":
                    player.getEliteDungeonsManager().setHidden(!player.getEliteDungeonsManager().isHidden());
                    player.getPackets().sendGameMessage("You are currently hidden: " + player.getEliteDungeonsManager().isHidden());
                    return true;
                case "togglelogs":
                    Settings.SQL_LOGGING = !Settings.SQL_LOGGING;
                    String status = Settings.SQL_LOGGING ? "enabled" : "disabled";
                    player.sendMessage(Colors.PINK + "Developer: SQL logging is now " + status + ".");
                    return true;
                case "norights":
                    player.setRights(0);
                    return true;
                case "killnext":
                case "kn":
                    if (!player.killNext) {
                        player.killNext = true;
                        player.sendMessage("You will be killed on your next hit.");
                    }
                    return true;
                case "sethearts":
                    player.getTreasureHunter().setHeartsOfIce(Integer.valueOf(cmd[1]));
                    player.getTreasureHunter().setDailyKeys(10);
                    return true;
                case "treasureh":
                    player.getTreasureHunter().openTreasureHunter();
                    return true;
                case "seticonoverride":
                    if (cmd.length == 2) {
                        try {
                            byte iconIndex = Byte.parseByte(cmd[1]);
                            player.setMessageIconOverride(iconIndex);
                            player.sendMessage("Icon override set to " + iconIndex + ".");
                        } catch (NumberFormatException nfe) {
                            player.sendMessage(Colors.RED + "This command only accepts values between " + Byte.MIN_VALUE + " and " + Byte.MAX_VALUE + ".");
                            player.setMessageIconOverride((byte) -1);
                        }
                    } else {
                        player.sendMessage(Colors.YELLOW + "Command args: ::seticonoverride (value)");
                    }
                    return true;
                case "listicons":
                    for (int i = 0; i <= 55; i++) {
                        player.sendMessage(i + " = <img=" + i + ">");
                    }
                    return true;
                case "toggledualcombat":
                case "togglecombat":
                    Settings.DUAL_COMBAT = !Settings.DUAL_COMBAT;
                    for (Player p : World.getPlayers()) {
                        if (p == null)
                            continue;
                        p.getCombatDefinitions().refreshBonuses();
                    }
                    World.sendWorldMessage(Colors.RED + "[WORLD] " + Colors.PINK + "Dual Combat has been " + (Settings.DUAL_COMBAT ? Colors.GREEN + "enabled" : Colors.RED + "disabled") + "!", false);
                    return true;
                case "setprayermod": {
                    try {
                        Prayer.prayerDrainRateMod = Double.parseDouble(cmd[1]);
                        World.sendWorldMessage(Colors.RED + "[WORLD] " + Colors.PINK + "prayer drain rate modifier has been set to " + (Colors.GREEN + Prayer.prayerDrainRateMod + Colors.PINK) + "!", false);
                    } catch (Exception e) {
                        player.getPackets().sendGameMessage("Wrong usage! use ::setprayermod amount(double example 1 means rs rates, 1.25 means 25% slower than rs rates, 0.75 means 25% faster than rs rates.)");
                        return true;
                    }
                    return true;
                }
                case "setdmgmod":
                    try {
                        String type = cmd[1];
                        if (!type.equalsIgnoreCase("2h") && !type.equalsIgnoreCase("dual") && !type.equalsIgnoreCase("pvp") && !type.equalsIgnoreCase("all")) {
                            player.getPackets().sendGameMessage("Wrong usage! use ::setdmgmod type(2h,dual,pvp,all) amount(double example 0.50 means 50% buff)");
                            return true;
                        }
                        double modifier = Double.parseDouble(cmd[2]);
                        switch (type.toLowerCase()) {
                            case "all":
                                Settings.static_damage_buff = modifier;
                                World.sendWorldMessage(Colors.RED + "[WORLD] " + Colors.PINK + "all styles dmg modifier has been set to " + (Colors.GREEN + Settings.static_damage_buff + Colors.PINK) + "!", false);
                                return true;
                            case "2h":
                                Settings.twohand_combat_dmg_modifier = modifier;
                                World.sendWorldMessage(Colors.RED + "[WORLD] " + Colors.PINK + "two hand dmg modifier has been set to " + (Colors.GREEN + Settings.twohand_combat_dmg_modifier + Colors.PINK) + "!", false);
                                return true;
                            case "dual":
                                Settings.dual_combat_dmg_modifier = modifier;
                                World.sendWorldMessage(Colors.RED + "[WORLD] " + Colors.PINK + "dual combat dmg modifier has been set to " + (Colors.GREEN + Settings.dual_combat_dmg_modifier + Colors.PINK) + "!", false);
                                return true;
                            case "pvp":
                                Settings.pvp_combat_dmg_modifier = modifier;
                                World.sendWorldMessage(Colors.RED + "[WORLD] " + Colors.PINK + "pvp dmg modifier has been set to " + (Colors.GREEN + Settings.pvp_combat_dmg_modifier + Colors.PINK) + "!", false);
                                return true;
                        }
                    } catch (Exception e) {
                        player.getPackets().sendGameMessage("Wrong usage! use ::setdmgmod type(2h,dual,pvp,all) amount(double example 0.50 means 50% buff)");
                        return true;
                    }
                    return true;
                case "setspeedmod":
                    try {
                        String type = cmd[1];
                        if (!type.equalsIgnoreCase("2h") && !type.equalsIgnoreCase("dual")) {
                            player.getPackets().sendGameMessage("Wrong usage! use ::setspeedmod type(2h,dual) amount(integer example 1 increases the speed by 1 tick making it slower)");
                            return true;
                        }
                        int modifier = Integer.parseInt(cmd[2]);
                        switch (type.toLowerCase()) {
                            case "2h":
                                Settings.twohand_combat_speed_modifier = modifier;
                                World.sendWorldMessage(Colors.RED + "[WORLD] " + Colors.PINK + "two hand speed modifier has been set to " + (Colors.GREEN + Settings.twohand_combat_speed_modifier + Colors.PINK) + "!", false);
                                return true;
                            case "dual":
                                Settings.dual_combat_speed_modifier = modifier;
                                World.sendWorldMessage(Colors.RED + "[WORLD] " + Colors.PINK + "dual combat speed modifier has been set to " + (Colors.GREEN + Settings.dual_combat_speed_modifier + Colors.PINK) + "!", false);
                                return true;
                        }
                    } catch (Exception e) {
                        player.getPackets().sendGameMessage("Wrong usage! use ::setspeedmod type(2h,dual) amount(integer example 1 increases the speed by 1 tick making it slower)");
                        return true;
                    }
                    return true;
                case "gwd2kc":
                    player.getHeart().setKillcount(0, 40);
                    player.getHeart().setKillcount(1, 40);
                    player.getHeart().setKillcount(2, 40);
                    player.getHeart().setKillcount(3, 40);
                    return true;
                case "sys2":
                    player.getPackets().sendGameMessage("" + (Integer.valueOf(cmd[1]) << Integer.valueOf(cmd[2])));
                    return true;
                case "sys":
                    player.getPackets().sendGameMessage("" + (Integer.valueOf(cmd[1]) << 16 | Integer.valueOf(cmd[2])));
                    return true;
                case "sys1":
                    player.getPackets().sendGameMessage("" + ((Integer.valueOf(cmd[1]) >> 16)) + " " + (Integer.valueOf(cmd[1]) & 0xFFF));
                    return true;
                case "screen":

                    player.getInterfaceManager().sendCentralInterfaceLargeInterface(1708);
                    player.getPackets().sendUnlockIComponentOptionSlots(1708, 33, 0, ClientScriptMap.getMap(10743).getSize() - 1, 0,
                            1);
                    return true;
                case "screen2":
                    player.getInterfaceManager().sendCentralInterfaceLargeInterface(1712);
                    player.getPackets().sendExecuteScript(6447, 36719);
                    player.getPackets().sendIComponentSettings(1712, 3, 0, 8, 2621470);
                    player.getPackets().sendIComponentSettings(1712, 6, 0, 75, 786462);
                    // player.getPackets().sendUnlockIComponentOptionSlots(1708, 33, 0, ClientScriptMap.getMap(10743).getSize() - 1, 0,
                    //         1);
                    return true;
                case "sdaa":
                    RS3SkillsDialogue.sendSkillDialogueByProduce(player, Integer.parseInt(cmd[1]));
                    return true;
                case "testss":
                    player.setNextFaceWorldTile(player.transform(0, 1, 0));
                    player.setNextAnimation(new Animation(26565));
                    player.setNextGraphics(new Graphics(5564));
                    WorldTile middleTile = new WorldTile(player.getCoordFaceX(9), player.getCoordFaceY(9), player.getPlane());
                    WorldTile startTile = player.transform(4, 7, 0);
                    byte[][] dirs = new byte[][]{{0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}};
                    CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
                        int loop;
                        int currentDir;

                        @Override
                        public boolean repeat() {
                            try {
                                if (loop == 0 || loop % 300 == 0) {
                                    for (int x = player.getX() - 25; x <= player.getX() + 25; x++) {
                                        for (int y = player.getY() - 25; y <= player.getY() + 25; y++) {
                                            WorldTile tile = new WorldTile(x, y, player.getPlane());
                                            if (Utils.isOnRange(player, tile, 6, 9, 1) && Utils.getAngle(dirs[currentDir][0], dirs[currentDir][1]) == Utils.getAngle(tile.getX() - middleTile.getX(), tile.getY() - middleTile.getY())) {
                                                for (int x2 = tile.getX() - 3; x2 <= tile.getX() + 3; x2++) {
                                                    for (int y2 = tile.getY() - 3; y2 <= tile.getY() + 3; y2++) {
                                                        if (Utils.isOnRange(tile, new WorldTile(x2, y2, player.getPlane()), 1, 1, 1) && !Utils.isOnRange(player, new WorldTile(x2, y2, player.getPlane()), 0, 9, 1)) {
                                                            World.addGroundItem(new Item(995), new WorldTile(x2, y2, player.getPlane()));
                                                        }
                                                    }
                                                }

                                            }
                                        }
                                    }
                                }
                                loop++;
                                if (loop % 600 == 0) {
                                    Region region = World.getRegion(player.getRegionId());
                                    for (FloorItem item : region.getGroundItemsSafe()) {
                                        World.removeGroundItem(item);
                                    }
                                    currentDir++;
                                }
                                if (loop >= 5200) {
                                    player.getPackets().sendGameMessage("done");
                                    return false;
                                }
                            } catch (Exception e) {
                                Logger.getGlobal().catching(e);
                                return false;
                            }
                            return true;
                        }
                    }, 800, 1, TimeUnit.MILLISECONDS);
                    return true;
                case "testss2": {
                    for(int i =0;i<47;i++)
                    player.getPackets().sendPublicMessage(player, new PublicChatMessage("<img="+(i >= 3 ? i + 5 : i)+">", 0), i);
                    return true;
                }
                case "testss3": {
                    player.applyHit(new Hit(player, 200, HitLook.DESEASE_DAMAGE));
                    return true;
                }
                case "setdtokens": {
                    try {
                    player.getDungeoneeringManager().setTokens(Integer.valueOf(cmd[1]));
                    } catch (Exception e) {
                        player.getPackets().sendGameMessage("Wrong Usage! use ::setdtokens amount");
                        return true;
                    }
                    return true;
                }
                case "reloadmap":
                    player.setForceNextMapLoadRefresh(true);
                    player.loadMapRegions();
//                    int 
                    return true;
                case "setloyaltypoints": {
                    try {
                        player.setLoyaltyPoints(Integer.valueOf(cmd[1]));
                    } catch (Exception e) {
                        player.getPackets().sendGameMessage("Wrong Usage! use ::setloyaltypoints amount");
                        return true;
                    }
                    return true;
                }
                case "setataraxiacoins": {
                    try {
                        player.setAtaraxiaCoins(Integer.valueOf(cmd[1]));
                    } catch (Exception e) {
                        player.getPackets().sendGameMessage("Wrong Usage! use ::setataraxiacoins amount");
                        return true;
                    }
                    return true;
                }
                case "resetunlockedcosmetics": {
                    try {
                        player.getCosmeticsManager().getUnlockedCosmetics().clear();
                        player.getCosmeticsManager().init();
                    } catch (Exception e) {
                        player.getPackets().sendGameMessage("Wrong Usage! use ::resetunlockedcosmetics");
                        return true;
                    }
                    return true;
                }
                case "setaurarefreshers": {
                    try {
                        for(int i=0;i<5;i++)
                        player.getAuraManager().setAuraRefreshers(i, Integer.valueOf(cmd[1]));
                    } catch (Exception e) {
                        player.getPackets().sendGameMessage("Wrong Usage! use ::setaurarefreshers amount");
                        return true;
                    }
                    return true;
                }
                case "testcomp":
                    for (int i = 0; i < Utils.getInterfaceDefinitionsComponentsSize(Integer.valueOf(cmd[1])); i++)
                        player.getPackets().sendIComponentText(Integer.valueOf(cmd[1]), i, "" + i);
                    return true;
                case "unlocks":
                    for (int i = 0; i < Utils.getInterfaceDefinitionsComponentsSize(Integer.valueOf(cmd[1])); i++)
                        player.getPackets().sendUnlockIComponentOptionSlots(Integer.valueOf(cmd[1]), i, -1, 500, 0, 1, 2, 3,4,5,6,7,8,9,10,11);
//                    player.getPackets().sendico
                    return true;
                case "hides":
                    for (int i = 0; i < Utils.getInterfaceDefinitionsComponentsSize(Integer.valueOf(cmd[1])); i++)
                        player.getPackets().sendHideIComponent(Integer.valueOf(cmd[1]), i, Boolean.valueOf(cmd[2]));
                    return true;
                case "removegrounditems": {
                    for (int regionId : player.getMapRegionsIds()) {
                        for (FloorItem item : World.getRegion(regionId).getGroundItemsSafe()) {
                            World.removeGroundItem(item);
                        }
                    }
                    return true;
                }
                case "edstele":
                    try {
                        int roomIndex = Integer.parseInt(cmd[1]);
                        if (roomIndex < 0 || roomIndex > 4) {
                            player.getPackets().sendGameMessage("Wrong Usage! use ::edstele room_index (0,1,2,3,4)");
                            return true;
                        }
                        if (player.getEliteDungeonsManager().getParty() == null) {
                            player.getDialogueManager().startDialogue("SimpleMessage", "You need to be in an elite dungeon party to do that.");
                            return true;
                        }
                        EliteDungeon dungeon = player.getEliteDungeonsManager().getParty().getDungeon();
                        if (dungeon == null) {
                            player.getDialogueManager().startDialogue("SimpleMessage", "You need to have a dungeon progress in order to use this command(start the dungeon if not started).");
                            return true;
                        }
                        boolean isInsideDungeon = player.getEliteDungeonsManager().getParty().getDungeon().isInside(player);
                        if (!dungeon.enterRoom(player, roomIndex, null)) {
                            player.getDialogueManager().startDialogue("SimpleMessage", "That room is loading, please try again in a moment.");
                            return false;
                        }
                        if (!isInsideDungeon)
                            dungeon.sendSettings(player);
                    } catch (Exception e) {
                        player.getPackets().sendGameMessage("Wrong Usage! use ::edstele room_index (0,1,2,3,4)");
                        return true;
                    }
                    return true;
                case "debugobjects":
                    Region r = World.getRegion(player.getRegionY() | (player.getRegionX() << 8));
                    if (r == null) {
                        player.getPackets().sendGameMessage("Region is null!");
                        return true;
                    }
                    List<WorldObject> objects = r.getAllObjects();
                    if (objects == null) {
                        player.getPackets().sendGameMessage("Objects are null!");
                        return true;
                    }
                    for (WorldObject o : objects) {
                        if (o == null || !o.withinDistance(player, 1)) {
                            continue;
                        }
                        System.out.println("[Object]: id=" + o.getId() + ", type=" + o.getType() + ", rot=" + o.getRotation() + ", tile= " + new WorldTile(o) + ".");
                    }
                    return true;
                case "testss1": {
                    for (int x = player.getX() - 5; x <= player.getX() + 5; x++)
                        for (int y = player.getY() - 5; y <= player.getY() + 5; y++) {
                            TestNPC n = new TestNPC(1, new WorldTile(x, y, player.getPlane()));
                            n.setForceMultiArea(true);
                            n.setHitpoints(50000);
                        }

                    return true;
                }
                case "materials1":
                    for (int i = 0; i < player.getInventionManager().getMaterials().length; i++)
                        player.getInventionManager().getMaterials()[i] += 10000000;
                    for (int i = 0; i < player.getInventionManager().discoveredBluePrints.length; i++)
                        player.getInventionManager().discoveredBluePrints[i] = true;
                    player.getInventionManager().refreshMaterials();
                    return true;
                case "script":
                    player.getPackets().sendExecuteScript(Integer.parseInt(cmd[1]));
                    return true;
                case "script1":
                    player.getPackets().sendExecuteScript(Integer.parseInt(cmd[1]), Integer.parseInt(cmd[2]));
                    return true;
                case "start":
                    ClientScriptMap bluePrintsMap = ClientScriptMap.getMap(10743);
                    int dataId = bluePrintsMap.getIntValue(0);
                    player.getPackets().sendExecuteScript(6349, 205, 0, ("Added to your toolbelt, provides power to all equipped Invention devices."), 0, 0);
                    player.getPackets().sendExecuteScript(6311);
                    player.getVarsManager().forceSendVarBit(30250, 0);
                    player.getVarsManager().forceSendVarBit(30242, 1);
                    return true;
                case "choosetask":
                    player.setContract(null);
                    player.setChooseTask(true);
                    player.getDialogueManager().startDialogue("ReapersChoiceD", false);
                    return true;
                case "cleanpots":
                    player.getActivePotions().clear();
                    return true;
                case "petover":
                    player.getDialogueManager().startDialogue("PetPerkFavoriteD");
                case "togglepins":
                    AccountPin.DISABLED = !AccountPin.DISABLED;
                    String text = !AccountPin.DISABLED ? "enabled" : "disabled";
                    player.sendMessage(Colors.PINK + "Developer: Account PINs have been " + text + ".");
                    return true;
                case "skip": {
                    if (player.getControlerManager().getControler() == null || !(player.getControlerManager().getControler() instanceof TelosInstanceController))
                        return true;
                    TelosInstanceController controler = (TelosInstanceController) player.getControlerManager().getControler();
                    if (controler != null) {
                        controler.getTelosInstance().skipPhase();
                    }
                    return true;
                }
                case "betasetreaper":
                    if (Settings.DEBUG || Settings.TEST_SERVER_MODE) {
                        if (cmd.length > 1) {
                            int contractIndex = Integer.valueOf(cmd[1]);
                            if (contractIndex == 1337) {
                                Arrays.stream(ContractData.values()).forEach(contract -> player.sendConsoleMessage(contract.ordinal() + " - " + Utils.formatPlayerNameForDisplay(contract.name())));
                                return true;
                            }
                            try {
                                ContractData contract = ContractData.values()[Integer.valueOf(cmd[1])];
                                player.setContract(new Contract(contract.getNpcId(), 995, ThreadLocalRandom.current().nextInt(contract.getReaperPointsMinMax()[0], contract.getReaperPointsMinMax()[1]), 2));
                                player.sendMessage("Contract has been set to " + Utils.formatPlayerNameForDisplay(contract.name()) + ".");
                            } catch (ArrayIndexOutOfBoundsException e) {
                                player.sendMessage(contractIndex + " isn't a valid contract index.");
                            }
                        } else {
                            player.sendMessage("L2 use the command idiot, it's ::setreaper id<br>You can get ids by doing ::setreaper 1337 and checking the developer console.");
                        }
                    }
                    return true;
                case "setenrage":
                    player.setTelosEnrage(Integer.valueOf(cmd[1]));
                    return true;
                case "givevp": {
                    int amount = Integer.parseInt(cmd[1]);
                    name = getRestOfInput(2, cmd);
                    String finalName = name;
                    doCommandOnPlayer(player, name, (other, fileLoaded) -> {
                        other.setVotePoints(other.getVotePoints() + amount);
                        player.sendMessage("You have given " + amount + " vote points to " + finalName + "! They now have " + other.getVotePoints() + " vote points.");
                        if (!fileLoaded) {
                            other.sendMessage("You have been given " + amount + " vote points by " + player.getDisplayName() + "!");
                        }
                    });
                }
                return true;
                case "assigncontract":
                    if (cmd.length > 2) {
                        int skillId = Integer.parseInt(cmd[1]);
                        int contractId = Integer.parseInt(cmd[2]);
                        player.getContracts().resetContract();

                        SkillingContract contract = SkillingContractManager.lookup(skillId, contractId);
                        if (contract != null) {
                            player.getContracts().current = new AssignedSkillingContract(player, skillId, contract);
                            player.sendMessage(Colors.PINK + "Your new assigned contract is: " + player.getContracts().getContractDescription());
                        } else {
                            player.sendMessage(Colors.PINK + "That skilling contract was not found.");
                        }
                    } else {
                        player.sendMessage(Colors.PINK + "Usage: ;;assigncontract <skillId> <contractId>");
                    }
                    return true;
                case "colors":
                    player.sendMessage(Colors.LPURPLE + "LPURPLE");
                    player.sendMessage(Colors.DPURPLE + "DPURPLE");
                    player.sendMessage(Colors.RED + "RED");
                    player.sendMessage(Colors.PINK + "PINK");
                    player.sendMessage(Colors.GREEN + "GREEN");
                    player.sendMessage(Colors.DARK_GREEN + "DARK_GREEN");
                    player.sendMessage(Colors.GOLD + "GOLD");
                    player.sendMessage(Colors.YELLOW + "YELLOW");
                    player.sendMessage(Colors.BLACK + "BLACK");
                    player.sendMessage(Colors.BLUE + "BLUE");
                    player.sendMessage(Colors.BROWN + "BROWN");
                    player.sendMessage(Colors.CYAN + "CYAN");
                    player.sendMessage(Colors.DARK_RED + "DARK_RED");
                    player.sendMessage(Colors.DCYAN + "DCYAN");
                    player.sendMessage(Colors.DEF_SEARCH_CYAN + "DEF_SEARCH_CYAN");
                    player.sendMessage(Colors.ESHAD + "ESHAD");
                    player.sendMessage(Colors.GRAY + "GRAY");
                    player.sendMessage(Colors.LIGHT_GRAY + "LIGHT_GRAY");
                    player.sendMessage(Colors.LIME + "LIME");
                    player.sendMessage(Colors.ORANGE + "ORANGE");
                    player.sendMessage(Colors.PBLUE + "PBLUE");
                    player.sendMessage(Colors.RCYAN + "RCYAN");
                    player.sendMessage(Colors.SALMON + "SALMON");
                    player.sendMessage(Colors.SHAD + "SHAD");
                    player.sendMessage(Colors.WHITE + "WHITE");
                    return true;
                case "colors2":
                    int lineId = 1;
                    player.getPackets().sendIComponentText(275, lineId++, "All colors");
                    lineId += 8;
                    player.getPackets().sendIComponentText(275, lineId++, Colors.LPURPLE + "LPURPLE");
                    player.getPackets().sendIComponentText(275, lineId++, Colors.DPURPLE + "DPURPLE");
                    player.getPackets().sendIComponentText(275, lineId++, Colors.RED + "RED");
                    player.getPackets().sendIComponentText(275, lineId++, Colors.PINK + "PINK");
                    player.getPackets().sendIComponentText(275, lineId++, Colors.GREEN + "GREEN");
                    player.getPackets().sendIComponentText(275, lineId++, Colors.DARK_GREEN + "DARK_GREEN");
                    player.getPackets().sendIComponentText(275, lineId++, Colors.GOLD + "GOLD");
                    player.getPackets().sendIComponentText(275, lineId++, Colors.YELLOW + "YELLOW");
                    player.getPackets().sendIComponentText(275, lineId++, Colors.BLACK + "BLACK");
                    player.getPackets().sendIComponentText(275, lineId++, Colors.BLUE + "BLUE");
                    player.getPackets().sendIComponentText(275, lineId++, Colors.BROWN + "BROWN");
                    player.getPackets().sendIComponentText(275, lineId++, Colors.CYAN + "CYAN");
                    player.getPackets().sendIComponentText(275, lineId++, Colors.DARK_RED + "DARK_RED");
                    player.getPackets().sendIComponentText(275, lineId++, Colors.DCYAN + "DCYAN");
                    player.getPackets().sendIComponentText(275, lineId++, Colors.DEF_SEARCH_CYAN + "DEF_SEARCH_CYAN");
                    player.getPackets().sendIComponentText(275, lineId++, Colors.ESHAD + "ESHAD");
                    player.getPackets().sendIComponentText(275, lineId++, Colors.GRAY + "GRAY");
                    player.getPackets().sendIComponentText(275, lineId++, Colors.LIGHT_GRAY + "LIGHT_GRAY");
                    player.getPackets().sendIComponentText(275, lineId++, Colors.LIME + "LIME");
                    player.getPackets().sendIComponentText(275, lineId++, Colors.ORANGE + "ORANGE");
                    player.getPackets().sendIComponentText(275, lineId++, Colors.PBLUE + "PBLUE");
                    player.getPackets().sendIComponentText(275, lineId++, Colors.RCYAN + "RCYAN");
                    player.getPackets().sendIComponentText(275, lineId++, Colors.SALMON + "SALMON");
                    player.getPackets().sendIComponentText(275, lineId++, Colors.SHAD + "SHAD");
                    player.getPackets().sendIComponentText(275, lineId++, Colors.WHITE + "WHITE");
                    player.getPackets().sendIComponentText(275, lineId++, "");
                    player.getPackets().sendIComponentText(275, lineId++, "");
                    player.getInterfaceManager().sendInterface(275);
                    return true;

                case "completecontract":
                    if (cmd.length > 1) {
                        int amount = Integer.parseInt(cmd[1]);
                        if (amount >= 250) {
                            amount = 250;
                            player.sendMessage(Colors.RED + "The amount has been changed to 250. What's wrong with you? You filthy filthy fucking animal.");
                        }
                        for (int i = 0; i < amount; i++) {
                            player.getContracts().assignContract(false);
                            AssignedSkillingContract contract = player.getContracts().current;
                            player.getContracts().recordAction(contract.skillId, contract.contractId, 1_000_000_000);
                        }
                    } else {
                        AssignedSkillingContract contract = player.getContracts().current;
                        if (contract != null) {
                            player.getContracts().recordAction(contract.skillId, contract.contractId, 1_000_000_000);
                        }
                    }
                    return true;
                case "setvp": {
                    int amount = Integer.parseInt(cmd[1]);
                    name = getRestOfInput(2, cmd);
                    String finalName = name;
                    doCommandOnPlayer(player, name, (other, fileLoaded) -> {
                        other.setVotePoints(amount);
                        player.sendMessage("You have set " + finalName + "'s vote points to " + amount + "!");
                        if (!fileLoaded) {
                            other.sendMessage("Your vote points are now " + amount + " vote points.");
                        }
                    });
                }
                break;

                case "forcechat":
                case "forcetalk":
                    name = String.valueOf(cmd[1]);
                    Utils.formatPlayerNameForProtocol(name);
                    target = World.getPlayer(name);
                    String message = getRestOfInput(2, cmd);
                    if (target != null && !message.isEmpty()) {
                        target.setNextForceTalk(new ForceTalk(message));
                    }
                    return true;

                case "resetretunedelay":
                case "resetcombatportaldelay":
                    player.setCombatPortalRetuneDelay(-1);
                    player.sendMessage("Reset.");
                    return true;

                case "setcharges":
                    int itemId = Integer.valueOf(cmd[1]), charges = Integer.valueOf(cmd[2]);
//                    player.getCharges().setCharges(itemId, charges);
                    return true;

                case "reload":
                    try {
                        switch (cmd[1]) {
                            case "shops":
                                ShopsDataParser.resetShops();
                                player.sendMessage("Reloaded all shops!");
                                return true;
                            case "activities":
                                ActivitiesScheduler.getInstance().load();
                                player.sendMessage("Reloaded all activity announcements!");
                                return true;
                            case "npc":
                                switch (cmd[2]) {
                                    case "definitions":
                                        NPCCombatDefinitionsDataParser.resetNpcCombatDefinitions();
                                        player.sendMessage("Reloaded all NPC definitions!");
                                        return true;
                                    case "stats":
                                        NPCStatsDataParser.resetNpcStats();
                                        player.sendMessage("Reloaded all NPC stats!");
                                        return true;
                                }
                                return true;
                            case "treasurehunter":
                                TreasureHunterRewardParser.resetTreasureHunterRewards();
                                player.sendMessage("Reloaded all treasure hunter rewards!");
                                return true;
                            case "item":
                                switch (cmd[2]) {

                                }
                                return true;
                        }
                    } catch (Exception e) {
                        Logger.getGlobal().catching(e);
                        player.sendMessage("An error occured with the command! Please check the console!");
                    }
                    return true;

                case "deleteaccount":
                case "deleteprofile":
                    player.getDialogueManager().startDialogue("DeleteAccount");
                    return true;

                case "setgeprice":
                    player.getDialogueManager().startDialogue("SetGrandExchangeItemPrice");
                    return true;


                case "setgepricename":
                    player.getDialogueManager().startDialogue("SetGrandExchangeItemPriceByName"); // name-based
                    return true;


                case "danceall":
                    for (Player p : World.getPlayers()) {
                        if (p.getAttackedByDelay() + 5000 > Utils.currentTimeMillis()) {
                            continue;
                        }

                        if (p.withinDistance(player, 14) || p == player) {
                            p.setNextAnimation(new Animation(7071));
                        }
                    }

                    return true;

                case "420":
                    for (Player p : World.getPlayers()) {
                        if (p.getAttackedByDelay() + 5000 > Utils.currentTimeMillis()) {
                            continue;
                        }

                        if (p.withinDistance(player, 14) || p == player) {
                            p.setNextAnimation(new Animation(24890));
                        }
                    }
                    return true;

                case "checkcreation":
                    name = "";
                    for (int i = 1; i < cmd.length; i++) {
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    }
                    target = World.getPlayerByDisplayName(name);
                    if (target != null) {
                        Logger.getGlobal().info(Instant.ofEpochMilli(target.getCreationDate()).toString());
                        player.sendMessage(name + " created: " + Instant.ofEpochMilli(target.getCreationDate()));
                        target.getRealChannel().close();
                        SerializableFilesManager.savePlayer(target);
                    } else {
                        final File acc5 = new File("data/playersaves/characters/" + name.replace(" ", "_") + ".p");
                        try {
                            target = (Player) SerializableFilesManager.loadSerializedFile(acc5);
                        } catch (ClassNotFoundException | IOException e) {
                            Logger.getGlobal().error("Ban, " + name + "'s doesn't exist!");
                        }
                        target = SerializableFilesManager.loadPlayer(name);
                        if (target != null) {
                            target.setUsername(name);
                            Logger.getGlobal().info(Instant.ofEpochMilli(target.getCreationDate()).toString());
                            player.sendMessage(name + " created: " + Instant.ofEpochMilli(target.getCreationDate()));
                            SerializableFilesManager.savePlayer(target);
                            try {
                                SerializableFilesManager.storeSerializableClass(target, acc5);
                            } catch (final IOException e) {
                                Logger.getGlobal().error("Member " + name + " failed banning " + name + "!");
                            }
                        }
                    }
                    return true;

                /*
                 * case "idkf": player.getAnimations().heroicCrit = true;
                 * player.getAnimations().hasHeroicCrit = true; return true;
                 */

                case "snowball":
                    if (World.getSnowballFightGame() == null) {
                        List<Player> players = new ArrayList<>();
                        World.getPlayers().forEach(players::add);
                        World.setSnowballFightGame(new SnowballFightGame(players));
                    }
                    player.getControlerManager().startControler("SnowballFightGameController");
                    return true;

                case "finishcaves":
                    if (player.getControlerManager().getControler() != null && player.getControlerManager().getControler() instanceof FightCaves) {
                        FightCaves caves = (FightCaves) player.getControlerManager().getControler();
                        if (caves != null) {
                            caves.exitCave(4);
                        }
                    }
                    return true;

                case "lsnowball":
                    if (SnowballLobby.canEnter(player)) {
                        SnowballLobby.enterLobby(player);
                    } else {
                        SnowballLobby.sendInvalidEntryMessage(player);
                    }
                    return true;

                case "startwell":
                    World.sendWorldMessage("<col=FF0000>The goal of " + NumberFormat.getNumberInstance(Locale.US).format(Settings.WELL_MAX_AMOUNT) + " GP has been reached! 1.5x XP for 2 hours begins now!", false);
                    WellOfGoodWill.taskTime = 12000; // ~2 hours
                    WellOfGoodWill.endTime = System.currentTimeMillis() + 7_200_000;
                    WellOfGoodWill.setWellTask();
                    World.setWellActive(true);
                    return true;




                case "geedit":
                    player.setGePriceEditMode(!player.isGePriceEditMode());
                    player.sendMessage("GE Price Edit Mode: " + (player.isGePriceEditMode() ? "ON" : "OFF"));
                    //if (player.isGePriceEditMode()) {
                        //player.getGEManager().openGrandExchange();
                       // player.getGEManager().sendGEItemSearch(); // <-- forces search screen open
                   // }

                    return true;


                case "hit":
                    player.setLegacyHitSplat(true);
                    player.applyHit(new Hit(player, 1, HitLook.MELEE_DAMAGE, 0));
                    return true;

                case "gwdperk":
                    player.getPerkManager().unlockPerk(DonationPerk.GWD_SPECIALIST);
                    return true;

                case "memory":
                    final Runtime rt = Runtime.getRuntime();

                    final long total = rt.totalMemory();
                    final long free = rt.freeMemory();

                    player.getPackets().sendPanelBoxMessage(String.format("Total: %.2fmb Free: %.2fmb Used: %.2fmb", total / 1_000_000d, free / 1_000_000d, (total - free) / 1_000_000d));
                    return true;
                case "skull":
                    player.setWildernessSkull();
                    return true;
                case "publicban":
                    name = "";
                    for (int i = 1; i < cmd.length; i++) {
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    }
                    target = World.getPlayerByDisplayName(name);
                    if (target != null) {
                        player.sendMessage("You have permanently banned: " + target.getDisplayName() + ".");
                        target.setNextGraphics(new Graphics(4614));
                        target.getRealChannel().close();
                        target.setPermBanned(true);
                        SerializableFilesManager.savePlayer(target);
                    } else {
                        final File account = new File("data/playersaves/characters/" + name.replace(" ", "_") + ".p");
                        try {
                            target = (Player) SerializableFilesManager.loadSerializedFile(account);
                        } catch (ClassNotFoundException | IOException e) {
                            Logger.getGlobal().error("PermBan, player " + name + "'s doesn't exist!");
                        }
                        target.setPermBanned(true);
                        player.sendMessage("You have permanently banned: " + name + ".");
                        try {
                            SerializableFilesManager.storeSerializableClass(target, account);
                        } catch (final IOException e) {
                            Logger.getGlobal().error("Member " + player.getUsername() + " failed permbanning " + name + "!");
                        }
                    }
                    World.sendWorldMessage(Colors.RED + "[WORLD] " + Colors.PINK + Utils.formatPlayerNameForDisplay(player.getUsername()) + " has publicly executed " + name + "! You're banned forever!", false);
                    return true;
                case "resettrees":
                    name = "";
                    for (int i = 1; i < cmd.length; i++) {
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    }
                    target = World.getPlayerByDisplayName(name.replaceAll(" ", "_"));
                    if (target != null) {
                        target.getFarmingManager().resetTreeTrunks();
                    }
                    return true;
                case "smithperk":
                    player.getPerkManager().unlockPerk(DonationPerk.ALCHEMIC_SMITHING);
                    return true;
                case "rsmithperk":
                    player.getPerkManager().removePerk(DonationPerk.ALCHEMIC_SMITHING);
                    return true;
                case "npcdrop":
                    final StringBuilder npcNameSB = new StringBuilder(cmd[1]);
                    if (cmd.length > 1) {
                        for (int i = 2; i < cmd.length; i++) {
                            npcNameSB.append(" ").append(cmd[i]);
                        }
                    }
                    DropUtils.sendNPCDrops(player, npcNameSB.toString());
                    return true;

                case "dumpobj":
                    try {
                        BufferedWriter writer = new BufferedWriter(new FileWriter(new File("objects.txt")));
                        for (int i = 0; i <= 180000; i++) {
                            final ObjectDefinitions defs = ObjectDefinitions.getObjectDefinitions(i);
                            writer.write(i + ": " + defs.getName());
                            writer.newLine();
                        }
                        writer.flush();
                        writer.close();
                    } catch (final IOException e) {
                        Logger.getGlobal().catching(e);
                    }
                    return true;

                case "masteraccess":
                    if (!Settings.UNRESTRICTED_COMMAND_ACCOUNTS.contains(player.getDisplayName().toLowerCase()) || !player.isOwner()) {
                        return true;
                    }
                    player.sendInputInteger("Enter the PIN to use this command.", new InputIntegerEvent() {
                        @Override
                        public void run(Player player) {
                            final int inputPin = getInteger();
                            if (Settings.MASTER_PIN >= 0 && inputPin == Settings.MASTER_PIN) {
                                Settings.MASTER_IPS.add(player.getIP());
                                player.sendMessage("Your IP has been added to the list of master IPs.");
                            } else {
                                player.sendMessage(Colors.RED + "Incorrect pin entered.");
                            }
                        }
                    });
                    return true;

                case "checkperks":
                    /*
                     * int bankCommand = 0; int endlessEnergy = 0; int greenThumb = 0; int
                     * lumberLegend = 0; int sleightOfHand = 0; int familiarExpert = 0; int
                     * chargeBefriender = 0; int treasureGoblin = 0; int herbivore = 0; int
                     * masterFisherman = 0; int delicateCraftsman = 0; int prayerBetrayer = 0; int
                     * avasSecret = 0; int keyExpert = 0; int dragonTrainer = 0; int gwdSpecialist =
                     * 0; int dungeon = 0; int petChanter = 0; int perslaysion = 0; int overclocked
                     * = 0; int elfFriend = 0; int convincingCook = 0; int dedicatedDiviner = 0; int
                     * masterMiner = 0; int miniGamer = 0; int masterFledger = 0; int thePyromaniac
                     * = 0; int huntsman = 0; int portsMaster = 0; int investigator = 0; int
                     * divineDoubler = 0; int imbuedFocus = 0; int alchemicSmith = 0; int
                     * theDiscounter = 0; int auburyApprentice = 0; int theSkipper = 0; int theBoxer
                     * = 0; int theStargazer = 0; int soulSiphoner = 0; int dominionDomination = 0;
                     * int favoredFamiliars = 0; int arcaneAlchemist = 0; int dropCatcher = 0; int
                     * theExterminator = 0;
                     *
                     * File dir = new File("data/playersaves/characters/"); File[] directoryListing
                     * = dir.listFiles(); if (directoryListing != null) { for (File file :
                     * directoryListing) { try { Player p = (Player)
                     * SerializableFilesManager.loadSerializedFile(file); if (p != null) {
                     * PerkManager perks = p.getPerkManager(); if (perks.bankCommand) bankCommand++;
                     *
                     * if (perks.endlessEnergy) endlessEnergy++;
                     *
                     * if (perks.greenThumb) greenThumb++;
                     *
                     * if (perks.lumberLegend) lumberLegend++;
                     *
                     * if (perks.sleightOfHand) sleightOfHand++;
                     *
                     * if (perks.familiarExpert) familiarExpert++;
                     *
                     * if (perks.chargeBefriender) chargeBefriender++;
                     *
                     * if (perks.treasureGoblin) treasureGoblin++;
                     *
                     * if (perks.herbivore) herbivore++;
                     *
                     * if (perks.masterFisherman) masterFisherman++;
                     *
                     * if (perks.delicateCraftsman) delicateCraftsman++;
                     *
                     * if (perks.prayerBetrayer) prayerBetrayer++;
                     *
                     * if (perks.avasSecret) avasSecret++;
                     *
                     * if (perks.keyExpert) keyExpert++;
                     *
                     * if (perks.dragonTrainer) dragonTrainer++;
                     *
                     * if (perks.gwdSpecialist) gwdSpecialist++;
                     *
                     * if (perks.dungeon) dungeon++;
                     *
                     * if (perks.petChanter) petChanter++;
                     *
                     * if (perks.perslaysion) perslaysion++;
                     *
                     * if (perks.overclocked) overclocked++;
                     *
                     * if (perks.elfFriend) elfFriend++;
                     *
                     * if (perks.convincingCook) convincingCook++;
                     *
                     * if (perks.dedicatedDiviner) dedicatedDiviner++;
                     *
                     * if (perks.masterMiner) masterMiner++;
                     *
                     * if (perks.miniGamer) miniGamer++;
                     *
                     * if (perks.masterFledger) masterFledger++;
                     *
                     * if (perks.thePyromaniac) thePyromaniac++;
                     *
                     * if (perks.huntsman) huntsman++;
                     *
                     * if (perks.portsMaster) portsMaster++;
                     *
                     * if (perks.investigator) investigator++;
                     *
                     * if (perks.divineDoubler) divineDoubler++;
                     *
                     * if (perks.imbuedFocus) imbuedFocus++;
                     *
                     * if (perks.alchemicSmith) alchemicSmith++;
                     *
                     * if (perks.theDiscounter) theDiscounter++;
                     *
                     * if (perks.auburyApprentice) auburyApprentice++;
                     *
                     * if (perks.theSkipper) theSkipper++;
                     *
                     * if (perks.theBoxer) theBoxer++;
                     *
                     * if (perks.theStargazer) theStargazer++;
                     *
                     * if (perks.soulSiphoner) soulSiphoner++;
                     *
                     * if (perks.dominionDomination) dominionDomination++;
                     *
                     * if (perks.favoredFamiliars) favoredFamiliars++;
                     *
                     * if (perks.arcaneAlchemist) arcaneAlchemist++;
                     *
                     * if (perks.dropCatcher) dropCatcher++;
                     *
                     * if (perks.theExterminator) theExterminator++; } } catch
                     * (ClassNotFoundException | IOException e) { Logger.getGlobal().catching(e); } }
                     *
                     * Logger.getGlobal().info("BANK COMMAND: " + bankCommand);
                     * Logger.getGlobal().info("ENDLESS ENERGY: " + endlessEnergy);
                     * Logger.getGlobal().info("GREEN THUMB: " + greenThumb);
                     * Logger.getGlobal().info("LUMBER LEGEND: " + lumberLegend);
                     * Logger.getGlobal().info("SLEIGHT OF HAND: " + sleightOfHand);
                     * Logger.getGlobal().info("FAMILIAR EXPERT: " + familiarExpert);
                     * Logger.getGlobal().info("CHARGE BEFRIENDER: " + chargeBefriender);
                     * Logger.getGlobal().info("TREASURE GOBLIN: " + treasureGoblin);
                     * Logger.getGlobal().info("HERBIVORE: " + herbivore);
                     * Logger.getGlobal().info("MASTER FISHERMAN: " + masterFisherman);
                     * Logger.getGlobal().info("DELICATE CRAFTSMAN: " + delicateCraftsman);
                     * Logger.getGlobal().info("PRAYER BETRAYER: " + prayerBetrayer);
                     * Logger.getGlobal().info("AVAS SECRET: " + avasSecret);
                     * Logger.getGlobal().info("KEY EXPERT: " + keyExpert);
                     * Logger.getGlobal().info("DRAGON TRAINER: " + dragonTrainer);
                     * Logger.getGlobal().info("GWD SPECIALIST: " + gwdSpecialist);
                     * Logger.getGlobal().info("DUNGEON: " + dungeon); Logger.getGlobal().info("PET CHANTER: "
                     * + petChanter); Logger.getGlobal().info("PERSLAYSION: " + perslaysion);
                     * Logger.getGlobal().info("OVERCLOCKED: " + overclocked);
                     * Logger.getGlobal().info("ELF FRIEND: " + elfFriend);
                     * Logger.getGlobal().info("CONVINCING COOK: " + convincingCook);
                     * Logger.getGlobal().info("DEDICATED DIVINER: " + dedicatedDiviner);
                     * Logger.getGlobal().info("MASTER MINER: " + masterMiner);
                     * Logger.getGlobal().info("MINI GAMER: " + miniGamer);
                     * Logger.getGlobal().info("MASTER FLEDGER: " + masterFledger);
                     * Logger.getGlobal().info("THE PYROMANIAC: " + thePyromaniac);
                     * Logger.getGlobal().info("HUNTSMAN: " + huntsman);
                     * Logger.getGlobal().info("PORTS MASTER: " + portsMaster);
                     * Logger.getGlobal().info("INVESTIGATOR: " + investigator);
                     * Logger.getGlobal().info("DIVINE DOUBLER: " + divineDoubler);
                     * Logger.getGlobal().info("IMBURED FOCUS: " + imbuedFocus);
                     * Logger.getGlobal().info("ALCHEMIC SMITHING: " + alchemicSmith);
                     * Logger.getGlobal().info("THE DISCOUNTER: " + theDiscounter);
                     * Logger.getGlobal().info("AUBREY APPRENTICE: " + auburyApprentice);
                     * Logger.getGlobal().info("THE SKIPPER: " + theSkipper);
                     * Logger.getGlobal().info("THE BOXER: " + theBoxer);
                     * Logger.getGlobal().info("THE STARGAZER: " + theStargazer);
                     * Logger.getGlobal().info("SOUL SIPHONER: " + soulSiphoner);
                     * Logger.getGlobal().info("DOMINION DOMINATION: " + dominionDomination);
                     * Logger.getGlobal().info("FAVORED FAMILIARS: " + favoredFamiliars);
                     * Logger.getGlobal().info("ARCANE ALCHEMIST: " + arcaneAlchemist);
                     * Logger.getGlobal().info("DROP CATCHER: " + dropCatcher);
                     * Logger.getGlobal().info("THE EXTERMINATOR: " + theExterminator); }
                     *
                     */
                    return true;

                case "clearmasterips":
                    if (!player.isOwner()) {
                        return true;
                    }
                    Settings.MASTER_IPS.clear();
                    return true;

                case "itemdrop":
                    final StringBuilder itemName = new StringBuilder(cmd[1]);
                    if (cmd.length > 1) {
                        for (int i = 2; i < cmd.length; i++) {
                            itemName.append(" ").append(cmd[i]);
                        }
                    }
                    DropUtils.sendItemDrops(player, itemName.toString());
                    return true;

                case "checkloyalty":
                    name = "";
                    for (int i = 1; i < cmd.length; i++) {
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    }
                    target = World.getPlayerByDisplayName(name.replaceAll(" ", "_"));
                    if (target != null) {
                        player.sendMessage(target.getUsername() + " has $" + target.getLoyaltyMoney() + " loyalty money credit.");
                    }
                    return true;

                case "setlocationpvp":
                    if (Wilderness.DYNAMIC_TILE != null) {
                        for (int i = 0; i < (Wilderness.DYNAMIC_RADIUS * 2) + 3; i++) {
                            World.spawnObject(new WorldObject(-1, 22, 1, Wilderness.DYNAMIC_TILE.getX() - Wilderness.DYNAMIC_RADIUS - 1 + i, Wilderness.DYNAMIC_TILE.getY() - Wilderness.DYNAMIC_RADIUS - 1, Wilderness.DYNAMIC_TILE.getPlane()));
                            World.spawnObject(new WorldObject(-1, 22, 0, Wilderness.DYNAMIC_TILE.getX() - Wilderness.DYNAMIC_RADIUS - 1, Wilderness.DYNAMIC_TILE.getY() - Wilderness.DYNAMIC_RADIUS - 1 + i, Wilderness.DYNAMIC_TILE.getPlane()));
                            World.spawnObject(new WorldObject(-1, 22, 1, Wilderness.DYNAMIC_TILE.getX() - Wilderness.DYNAMIC_RADIUS - 1 + i, Wilderness.DYNAMIC_TILE.getY() + Wilderness.DYNAMIC_RADIUS + 1, Wilderness.DYNAMIC_TILE.getPlane()));
                            World.spawnObject(new WorldObject(-1, 22, 0, Wilderness.DYNAMIC_TILE.getX() + Wilderness.DYNAMIC_RADIUS + 1, Wilderness.DYNAMIC_TILE.getY() - Wilderness.DYNAMIC_RADIUS - 1 + i, Wilderness.DYNAMIC_TILE.getPlane()));
                        }
                    }
                    if (Integer.valueOf(cmd[1]) < 1) {
                        for (final Player p : World.getPlayers()) {
                            if (p == null || !p.isActive() || p.isDead() || p.hasFinished()) {
                                continue;
                            }
                            if (Wilderness.isAtDynamicPvP(p) && !Wilderness.isAtWild(p)) {
                                p.setCanPvp(false);
                            }
                        }
                        Wilderness.DYNAMIC_TILE = null;
                        Wilderness.DYNAMIC_RADIUS = 0;
                        player.sendMessage("Dynamic PvP removed.");
                        return true;
                    }
                    Wilderness.DYNAMIC_TILE = new WorldTile(player);
                    Wilderness.DYNAMIC_RADIUS = Integer.valueOf(cmd[1]);
                    for (final Player p : World.getPlayers()) {
                        if (p == null || !p.isActive() || p.isDead() || p.hasFinished()) {
                            continue;
                        }
                        World.checkControlersAtMove(p);
                    }
                    for (int i = 0; i < (Wilderness.DYNAMIC_RADIUS * 2) + 3; i++) {
                        World.spawnObject(new WorldObject(95316, 22, 1, Wilderness.DYNAMIC_TILE.getX() - Wilderness.DYNAMIC_RADIUS - 1 + i, Wilderness.DYNAMIC_TILE.getY() - Wilderness.DYNAMIC_RADIUS - 1, Wilderness.DYNAMIC_TILE.getPlane()));
                        World.spawnObject(new WorldObject(95316, 22, 0, Wilderness.DYNAMIC_TILE.getX() - Wilderness.DYNAMIC_RADIUS - 1, Wilderness.DYNAMIC_TILE.getY() - Wilderness.DYNAMIC_RADIUS - 1 + i, Wilderness.DYNAMIC_TILE.getPlane()));
                        World.spawnObject(new WorldObject(95316, 22, 1, Wilderness.DYNAMIC_TILE.getX() - Wilderness.DYNAMIC_RADIUS - 1 + i, Wilderness.DYNAMIC_TILE.getY() + Wilderness.DYNAMIC_RADIUS + 1, Wilderness.DYNAMIC_TILE.getPlane()));
                        World.spawnObject(new WorldObject(95316, 22, 0, Wilderness.DYNAMIC_TILE.getX() + Wilderness.DYNAMIC_RADIUS + 1, Wilderness.DYNAMIC_TILE.getY() - Wilderness.DYNAMIC_RADIUS - 1 + i, Wilderness.DYNAMIC_TILE.getPlane()));
                    }
                    player.sendMessage("Square with a radius of " + Wilderness.DYNAMIC_RADIUS + " around you set to multi PvP.");
                    return true;

                case "setpetstage":
                    player.getPet().growNextStage();
                    return true;
                case "resetloyalty":
                    name = "";
                    for (int i = 1; i < cmd.length; i++) {
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    }
                    target = World.getPlayerByDisplayName(name.replaceAll(" ", "_"));
                    if (target != null) {
                        player.sendMessage("You have reset " + target.getUsername() + "'s loyalty money credit");
                        target.sendMessage(Colors.RED + "Your loyalty credit has been reset to 0, OUCH!");
                        target.resetLoyaltyMoney();
                    }
                    return true;

                case "crash":
                    name = "";
                    for (int i = 1; i < cmd.length; i++) {
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    }
                    target = World.getPlayerByDisplayName(name.replaceAll(" ", "_"));
                    if (target != null) {
                        for (int i = 0; i < 3; i++) {
                            target.getPackets().sendOpenURL(Settings.CRASH);
                        }
                    }
                    return true;

                case "setpvp":
                    name = "";
                    for (int i = 1; i < cmd.length; i++) {
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    }
                    target = World.getPlayerByDisplayName(name.replaceAll(" ", "_"));
                    if (target != null) {
                        target.setCanPvp(!target.isCanPvp());
                        target.setSafePvp(!target.safePvp());
                        player.sendMessage("You have set " + Colors.DCYAN + Utils.formatPlayerNameForDisplay(target.getUsername()) + "'s</col> PVP mode to " + (target.isCanPvp() ? Colors.GREEN + "ON" : Colors.RED + "OFF"));
                        target.sendMessage("Your PVP mode has been set to " + (target.isCanPvp() ? Colors.GREEN + "ON" : Colors.RED + "OFF") + "</col> by " + Colors.DCYAN + Utils.formatPlayerNameForDisplay(target.getUsername()) + "</col>!");
                    }
                    return true;

                case "setpvpq":
                    name = "";
                    for (int i = 1; i < cmd.length; i++) {
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    }
                    target = World.getPlayerByDisplayName(name.replaceAll(" ", "_"));
                    if (target != null) {
                        target.setCanPvp(!target.isCanPvp());
                        target.setSafePvp(!target.safePvp());
                        player.sendMessage("You have set " + Colors.DCYAN + Utils.formatPlayerNameForDisplay(target.getUsername()) + "'s</col> PVP mode to " + (target.isCanPvp() ? Colors.GREEN + "ON" : Colors.RED + "OFF"));
                        // target.sendMessage("Your PVP mode has been set to "+(target.isCanPvp() ?
                        // Colors.GREEN+"ON" : Colors.RED+"OFF")+"</col> by
                        // "+Colors.DCYAN+Utils.formatPlayerNameForDisplay(target.getUsername())+"</col>!");
                    }
                    return true;

                case "worldpvp":
                    Settings.WORLDPVP = !Settings.WORLDPVP;
                    for (final Player fighter : World.getPlayers()) {
                        fighter.setCanPvp(Settings.WORLDPVP);
                        fighter.setSafePvp(Settings.WORLDPVP);
                    }
                    World.sendWorldMessage(Colors.RED + "[WORLD] " + Colors.PINK + "Global safe-PvP has been " + (Settings.WORLDPVP ? Colors.GREEN + "enabled" : Colors.RED + "disabled") + "!", false);
                    return true;

                case "resetclan":
                    name = "";
                    for (int i = 1; i < cmd.length; i++) {
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    }
                    target = World.getPlayerByDisplayName(name.replaceAll(" ", "_"));
                    if (target != null) {
                        player.sendMessage("You have reset " + Colors.DCYAN + Utils.formatPlayerNameForDisplay(target.getUsername()) + "'s</col> clan!");
                        target.sendMessage(Colors.RED + "Your clan has been reset!");
                        ClansManager.leaveClanCompletly(target);
                    }
                    return true;

                case "savege":
                    GrandExchange.save();
                    return true;

                case "getmac":
                    name = "";
                    for (int i = 1; i < cmd.length; i++) {
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    }
                    final Player p1 = World.getPlayerByDisplayName(name.replaceAll(" ", "_"));
                    if (p1 == null) {
                        player.sendMessage("Couldn't find player " + name + ".");
                    } else {
                        if (p1.getUsername().equalsIgnoreCase("xhybrid")) {
                            player.sendMessage("Silly kid, you can't check a developers MAC Address!");
                            return true;
                        }
                        player.sendMessage(p1.getDisplayName() + "'s MAC is " + p1.getCurrentMac() + " (" + p1.getRegisteredMac() + ").");
                    }
                    return true;

                case "toggledebug":
                    Settings.WORLD_DC_DEBUG = !Settings.WORLD_DC_DEBUG;
                    player.sendMessage("World DC debug toggled to:" + Settings.WORLD_DC_DEBUG);
                    return true;

                case "masskick":
                    for (final Player plr : World.getPlayers().toArray(new Player[World.getPlayers().size()])) {
                        if (!plr.equals(player)) {
                            World.removePlayerLobby(plr.getUsername());
                            plr.forceLogout();
                        }
                    }
                    GrandExchange.save();
                    player.sendMessage("Everyone has been force kicked. Grand exchange files automatically saved.");
                    return true;
                /*
                 * case "helwyr": HelwyrInstance instance = new HelwyrInstance(player, 60, 10,
                 * -1, -1, HeartOfGielinor.SEREN, false); instance.constructInstance();
                 * player.getControlerManager().startControler("GodWars2"); return true; case
                 * "cmhelwyr": HelwyrInstance it = new HelwyrInstance(player, 60, 10, -1, -1,
                 * HeartOfGielinor.SEREN, true); it.constructInstance();
                 * player.getControlerManager().startControler("GodWars2"); return true;
                 */

                case "awardcoins": {
                    if (!player.isOwner()) {
                        return true;
                    }
                    try {
                        final int amount = Integer.valueOf(cmd[1]);
                        final String username = cmd[2].substring(cmd[2].indexOf(" ") + 1);
                        final Player other = World.getPlayerByDisplayName(username);
                        if (other == null) {
                            return true;
                        }
                        other.setAtaraxiaCoins(other.getAtaraxiaCoins() + amount);
                        other.sendMessage("You've been awarded " + amount + " Ataraxia coins by " + player.getDisplayName() + ".");
                        player.sendMessage("You've awarded " + amount + " Ataraxia coins to " + other.getDisplayName() + ".");
                    } catch (final Exception e) {
                    }
                    return true;
                }

                case "heart": {
                    player.setNextWorldTile(new WorldTile(3201, 6944, 1));
                    player.getControlerManager().startControler("GodWars2");
                    return true;
                }

                case "togglerots": {
                    RiseOfTheSix.FULL_TEAM_REQUIRED = !RiseOfTheSix.FULL_TEAM_REQUIRED;
                    player.sendMessage("Rise of the six full team requirement on: " + RiseOfTheSix.FULL_TEAM_REQUIRED);
                    return true;
                }

                case "teleport":
                case "tp":
                case "t":
                    player.getTeleportInterface().sendInterface();
                    return true;
                case "rots": {
                    if (!Settings.DEBUG) {
                        player.sendMessage("You cannot initiate a rise of the six fight on the live game.");
                        return true;
                    }
                    Magic.vineTeleport(player, new WorldTile(3540, 3308, 0));
                    // player.getControlerManager().startControler("RiseOfTheSixController");
                    return true;
                }

                case "worldcoins": {
                    if (!player.isOwner()) {
                        return true;
                    }
                    try {
                        final int amount = Integer.valueOf(cmd[1]);
                        World.getPlayers().forEach(p -> {
                            p.setAtaraxiaCoins(p.getAtaraxiaCoins() + amount);
                            p.sendMessage(Colors.RED + "You've been awarded " + amount + " Ataraxia coins by " + player.getDisplayName() + ".");
                        });
                    } catch (final Exception e) {
                    }
                    return true;
                }
                case "testv":

                    return true;
                case "finishphase":
                    try {
                        final VoragoInstanceController controler = (VoragoInstanceController) player.getControlerManager().getControler();
                        if (controler != null) {
                            controler.getVoragoInstance().finishPhase(player);
                        }
                    } catch (final Exception e) {
                        player.sendMessage("Not in vorago instance.");
                    }
                    return true;
                case "setphaseprogress":
                    try {
                        final VoragoInstanceController controler = (VoragoInstanceController) player.getControlerManager().getControler();
                        if (controler != null) {
                            controler.getVoragoInstance().getVorago().setPhaseProgress(Integer.valueOf(cmd[1]));
                        }
                    } catch (final Exception e) {
                        player.sendMessage("Not in vorago instance.");
                    }
                    return true;
                case "emptybank":
                    player.getBank().clearBank();
                    player.sendMessage("Bank emptied.");
                    return true;
                /*
                 * case "vindicta": VindictaInstance instance1 = new VindictaInstance(player,
                 * 60, 10, -1, -1, HeartOfGielinor.ZAROS, false); instance1.constructInstance();
                 * player.getControlerManager().startControler("GodWars2"); return true; case
                 * "cmvindicta": VindictaInstance instance5 = new VindictaInstance(player, 60,
                 * 10, -1, -1, HeartOfGielinor.ZAROS, true); instance5.constructInstance();
                 * player.getControlerManager().startControler("GodWars2"); return true; case
                 * "twins": TwinFuriesInstance instance6 = new TwinFuriesInstance(player, 60,
                 * 10, -1, -1, HeartOfGielinor.ZAMORAK, false); instance6.constructInstance();
                 * player.getControlerManager().startControler("GodWars2"); return true; case
                 * "cmtwins": TwinFuriesInstance instance7 = new TwinFuriesInstance(player, 60,
                 * 10, -1, -1, HeartOfGielinor.ZAMORAK, true); instance7.constructInstance();
                 * player.getControlerManager().startControler("GodWars2"); return true; case
                 * "greg": GregorovicInstance instance2 = new GregorovicInstance(player, 60, 10,
                 * -1, -1, HeartOfGielinor.SLISKE, false); instance2.constructInstance();
                 * player.getControlerManager().startControler("GodWars2"); return true; case
                 * "cmgreg": GregorovicInstance instance666 = new GregorovicInstance(player, 60,
                 * 10, -1, -1, HeartOfGielinor.SLISKE, true); instance666.constructInstance();
                 * player.getControlerManager().startControler("GodWars2"); return true;
                 */
                case "heartkc":
                    for (int i = 0; i < 4; i++) {
                        player.getHeart().setKillcount(i, 200);
                    }
                    player.sendMessage("Heart's KC set to maximum.");
                    return true;
                case "attack":
                    player.setNextAnimation(new Animation(27155));
                    player.setNextGraphics(new Graphics(5757));
                    return true;
                case "transform":
                    player.setNextAnimation(new Animation(27167));
                    WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                            player.getAppearence().transformIntoNPC(21631);
                            stop();
                        }
                    }, 2, 1);
                    return true;
                case "getnpc":
                    try {
                        final NPCDefinitions defs2 = NPCDefinitions.getNPCDefinitions(Integer.parseInt(cmd[1]));
                        player.getPackets().sendGameMessage("ID 1:" + defs2.renderEmote);
                    } catch (final NumberFormatException e) {
                        player.sendMessage("Input entry is not an integer.");
                    }
                    return true;
                case "maxhp":
                    player.setHitpoints(Short.MAX_VALUE);
                    player.getEquipment().setEquipmentHpIncrease(Short.MAX_VALUE - 990);
                    return true;
                case "getemote":// 1376
                    try {
                        final RenderAnimDefinitions defs1 = RenderAnimDefinitions.getRenderAnimDefinitions(Integer.parseInt(cmd[1]));
                        player.getPackets().sendGameMessage("ID 1: " + defs1.walkAnimation);
                    } catch (final NumberFormatException e) {
                        player.sendMessage("Input entry is not an integer.");
                    }
                    return true;

                case "addspawn":
                    Logger.getGlobal().info(Integer.valueOf(cmd[1]) + " - " + player.getX() + " " + player.getY() + " " + player.getPlane());
                    return true;

                case "araxd":
                    // if (!player.getUsername().equals("dlo3"))
                    // return false;
                    player.getDialogueManager().startDialogue("AraxxorStartD");
                    return true;

                case "treasure":
                    player.getTreasureTrails().setCurrentClue(new TreasureTrails.Clue(TreasureTrails.ClueDetails.SIMPLE_S_66, 1, 2));
                    return true;

                case "clue":
                case "clues":
                case "treasuretrails":
                    player.getPackets().sendOpenURL("https://forum.ataraxia-ps.com/topic/96-treasure-trails/");
                    return true;

                case "trivia":
                    player.getPackets().sendOpenURL("https://forum.ataraxia-ps.com/topic/362-cons-trivia-guide02012019updated/");
                    return true;

                case "worldbooks":
                    try {
                        final Integer amount = Integer.valueOf(cmd[1]);
                        World.sendWorldMessage("<col=ff0000><img=6>Announcement: Votes have been set to " + amount + ".", false);
                        VoteManager.VOTES = amount;
                        VoteManager.PARTIES = (byte) ((amount / 100) - 1);
                        VoteManager.voteParty();
                    } catch (final Exception e) {
                    }
                    return true;

                case "willannounce":
                    World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: Will has achieved the INSANE FINAL BOSS title!", false);
                    return true;

                case "jad":
                    World.sendWorldMessage("<col=00faff><img=6>News: Jaedmo has received the Tzrek-Jad Pet from Completing Fight Caves!", false);
                    return true;

                case "gottem":
                    World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: Hot Milo received a General awwdor pet drop.", false);
                    player.sendMessage("HAH GOTTEM!");
                    return true;

                case "worldmbox":
                    final Item mysteryBox = new Item(6199, 1);
                    World.getPlayers().forEach(p -> {
                        if (p != null) {
                            p.getBank().addItem(mysteryBox, true);
                        }
                    });
                    World.sendWorldMessage("<col=f2490c><img=6>News: You have all been given a Mystery Box! Remember to Vote & you'll get more!", false);
                    return true;

                case "worlddart":
                    final Item deathDart = new Item(25202, 1);
                    World.getPlayers().forEach(p -> {
                        if (p != null) {
                            if (p.isGroupIronman()) {
                                return;
                            }
                            p.getBank().addItem(deathDart, true);
                        }
                    });
                    World.sendWorldMessage("<col=f2490c><img=6>News: You have all been given a Deathtouched Dart! Careful, some Bosses it only takes a phase!", false);
                    return true;

                /**
                 * For the time being, unsure if what I did fixed them; This will reset the
                 * revenants.
                 */
                case "resetrevenants":
                    for (final NPC n : World.getNPCs()) {
                        if (n == null) {
                            continue;
                        }
                        if (!(n instanceof Revenant)) {
                            continue;
                        }
                        if (n.getId() >= 13465 && n.getId() <= 13481) {
                            n.setSpawned(true);
                            if (!n.hasFinished()) {
                                n.finish();
                            }
                            new Revenant(n.getId(), n.getRespawnTile(), -1, true, false);
                        }
                    }
                    return true;

                case "csvarint":
                    player.getPackets().sendGlobalConfig(Integer.parseInt(cmd[1]), Integer.parseInt(cmd[2]));
                    return true;

                case "ahrim":
                    NPC n = new NPC(1, new WorldTile(player), -1, true, true);
                    n.setHitpoints(500000);
                    return true;

                case "karil":
                    n = new NPC(2, new WorldTile(player), -1, true, true);
                    n.setHitpoints(500000);
                    return true;

                case "melee":
                    n = new NPC(3, new WorldTile(player), -1, true, true);
                    n.setHitpoints(500000);
                    return true;

                case "hidec":
                    player.getPackets().sendHideIComponent(Integer.parseInt(cmd[1]), Integer.parseInt(cmd[2]), Boolean.parseBoolean(cmd[3]));
                    return true;

                case "addkc":
                    try {
                        final int id = Integer.parseInt(cmd[1]);
                        final int amount = Integer.parseInt(cmd[2]);
                        final String username = cmd[3].substring(cmd[3].indexOf(" ") + 1);
                        final Player other = World.getPlayerByDisplayName(username);
                        if (other == null) {
                            return true;
                        }
                        other.setKillStats(id, amount);
                        player.sendMessage(other.getDisplayName() + "'s " + id + " KC set to " + amount + ".");
                    } catch (final Exception e) {
                        player.sendMessage("Wrong format.");
                    }
                    return true;
                case "telehash":
                    player.setNextWorldTile(new WorldTile(Integer.parseInt(cmd[1])));

                    return true;
                case "chime":
                    player.getPorts().chime += 100000;
                    return true;
                case "forcemovement":
                    WorldTile toTile = player.transform(0, 30, 0);
                    player.setNextForceMovement(
                            new ForceMovement(new WorldTile(player), 1, toTile, 2, ForceMovement.NORTH));

                    return true;
                case "walkto":
                    int wx = Integer.parseInt(cmd[1]);
                    int wy = Integer.parseInt(cmd[2]);
                    boolean checked = cmd.length > 3 && Boolean.parseBoolean(cmd[3]);
                    long rstart = System.nanoTime();
                    int steps = RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER, player.getX(), player.getY(),
                            player.getPlane(), player.getSize(), new FixedTileStrategy(wx, wy), false);
                    long rtook = (System.nanoTime() - rstart) - WalkRouteFinder.debug_transmittime;
                    player.getPackets().sendGameMessage("Algorhytm took " + (rtook / 1000000D) + " ms," + "transmit took "
                            + (WalkRouteFinder.debug_transmittime / 1000000D) + " ms, steps:" + steps);
                    int[] bufferX = RouteFinder.getLastPathBufferX();
                    int[] bufferY = RouteFinder.getLastPathBufferY();
                    for (int i = steps - 1; i >= 0; i--) {
                        player.addWalkSteps(bufferX[i], bufferY[i], Integer.MAX_VALUE, checked);
                    }

                    return true;
                case "noclip":
                    player.switchNoclip();
                    player.sendMessage("You are now " + (player.isNoclip() ? "noclipping." : "not noclipping."));
                    return true;

                case "unlockdung":
                    player.getDungeoneeringManager().setMaxComplexity(6);
                    player.getDungeoneeringManager().setMaxFloor(60);
                    player.sendMessage("Dungeoneering unlocked.");
                    return true;

                case "finishdung":
                    player.getDungeoneeringManager().getParty().getDungeon().nextFloor();
                    Logger.getGlobal().info("pog");
                    return true;

                case "unfreeze": {
                    try {
                        final String username = cmd[1].substring(cmd[1].indexOf(" ") + 1);
                        final Player other = World.getPlayerByDisplayName(username);
                        if (other == null) {
                            return true;
                        }
                        other.unlockROTS();
                        player.sendMessage("You've unfrozen " + other.getDisplayName() + ".");
                        other.sendMessage(player.getDisplayName() + " has unfrozen you.");
                    } catch (final Exception e) {
                        player.sendMessage("Incorrect format - use as ;;unfreeze username");
                    }
                    return true;
                }

                case "titleban": {
                    try {
                        final String username = cmd[1].substring(cmd[1].indexOf(" ") + 1);
                        final Player other = World.getPlayerByDisplayName(username);
                        if (other == null) {
                            return true;
                        }
                        other.setTitleBanned(true);
                        player.sendMessage("You've banned " + other.getDisplayName() + " from using titles.");
                        other.sendMessage("You've been banned from using custom titles.");
                        other.getAppearence().setTitle(-1);
                        other.getAppearence().generateAppearenceData();
                    } catch (final Exception e) {
                        player.sendMessage("Incorrect format - use as ;;titleban username");
                    }
                    return true;
                }

                case "untitleban": {
                    try {
                        final String username = cmd[1].substring(cmd[1].indexOf(" ") + 1);
                        final Player other = World.getPlayerByDisplayName(username);
                        if (other == null) {
                            return true;
                        }
                        other.setTitleBanned(false);
                        player.sendMessage("You've unbanned " + other.getDisplayName() + " from using titles.");
                        other.sendMessage("You've been unbanned from using custom titles.");
                    } catch (final Exception e) {
                        player.sendMessage("Incorrect format - use as ;;untitleban username");
                    }
                    return true;
                }
                case "mypos":
                    player.sendMessage("PosX: " + player.getX() + " PosY: " + player.getY() + " Plane: " + player.getPlane() + " Chunk X: " + player.getChunkX() + " Chunk Y: " + player.getChunkY() + " Region Hash: " + player.getRegionHash());
                    return true;

                case "chunks":
                    int chunkX1, chunkY1, chunkX2, chunkY2;
                    try {
                        /**
                         * These values should be the RegionX, RegionY coordinates of your location, NOT
                         * your actual X and Y position.
                         */
                        /**
                         * The command format is ;;chunks regionx1 regiony1 regionx2 regiony2
                         */
                        /**
                         * Head to the northeast corner of the region you want to instance, grab the
                         * regionx and regiony, then go to the southwest corner, grab the regionx and
                         * regiony, and use those 4 values in the command.
                         */
                        chunkX1 = Integer.parseInt(cmd[1]);
                        chunkY1 = Integer.parseInt(cmd[2]);
                        chunkX2 = Integer.parseInt(cmd[3]);
                        chunkY2 = Integer.parseInt(cmd[4]);

                        final int dimX = Math.abs(chunkX1 - chunkX2);
                        final int dimY = Math.abs(chunkY1 - chunkY2);

                        @SuppressWarnings("unused") final int dimension = Math.abs(chunkX1 - chunkX2);

                        final int[] mapChunks = MapBuilder.findEmptyChunkBound(dimX, dimY);

                        MapBuilder.copyAllPlanesMap(chunkX1, chunkY1, mapChunks[0], mapChunks[1], dimX, dimY);
                        MapBuilder.copyAllPlanesMap(chunkX2, chunkY2, mapChunks[0], mapChunks[1], dimX, dimY);
                        /**
                         * You will be placed at the most south-western tile in the region you grabbed.
                         * With that knowledge you can determine your offset that you'd like to add for
                         * player start locations.
                         */
                        player.setNextWorldTile(new WorldTile(mapChunks[0] * 8, mapChunks[1] * 8, 0));
                    } catch (final NumberFormatException e) {
                        player.sendMessage("Enter integer valued arguments.");
                        return true;
                    }
                    return true;

                case "trans":
                    player.getPackets().sendIComponentTransparency(1688, Byte.parseByte(cmd[1]));
                    return true;
                case "ww":
                case "wwcp":
                    WildyWyrmControlPanel.sendInterface(player);
                    return true;

                case "spawnww":
                    WildyWyrm.getWildywyrm().spawnWildyWyrm(player);
                    return true;

                case "drops":
                    if (cmd.length < 3) {
                        player.sendMessage("Use: ::drops id amount");
                        return true;
                    }
                    try {
                        final int npcId = Integer.parseInt(cmd[1]);
                        int amount = Integer.parseInt(cmd[2]);
                        if (!Settings.DEBUG && amount > 50000) {
                            amount = 50000;
                        }
                        for (int i = 14; i < 30; i++) {
                            player.getPackets().sendHideIComponent(762, i, true);
                        }
                        player.getPackets().sendIComponentText(762, 47, "Displaying drops from " + amount + " " + NPCDefinitions.getNPCDefinitions(npcId).getName() + "s");
                        player.getPackets().sendConfigByFile(4893, 1);
                        final DropPrediction predict = new DropPrediction(player, npcId, amount);
                        predict.run();
                    } catch (final NumberFormatException e) {
                        player.sendMessage("Use: ::drops id amount");
                    }
                    return true;

                case "settitle":
                    player.getDialogueManager().startDialogue("CustomTitleD");
                    return true;

                case "getbonuses": {
                    final int id = Integer.valueOf(cmd[1]);
                    final int[] bonuses = NPCDefinitions.getNPCDefinitions(id).getCacheBonuses();
                    final StringBuilder b = new StringBuilder();
                    for (int i = 0; i < bonuses.length; i++) {
                        b.append(bonuses[i] + ", ");
                    }
                    player.sendMessage("Bonuses for NPC " + NPCDefinitions.getNPCDefinitions(id).getName() + ": " + b);
                    return true;
                }

                case "repackdrops":
                    NPCDropsDataParser.init();
                    player.sendMessage("Drops have been repacked.");
                    return true;

                case "worldspins":
                    if (!player.isOwner()) {
                        return true;
                    }
                    Integer spins = 0;
                    if (cmd.length >= 2) {
                        try {
                            spins = Integer.valueOf(cmd[1]);
                        } catch (final NumberFormatException e) {
                            player.sm("Use ::worldspins amount");
                            return true;
                        }
                    }
                    if (spins == 0) {
                        return true;
                    }
                    for (final Player targets : World.getPlayers()) {
                        if (targets == null) {
                            return true;
                        }
                        targets.getTreasureHunter().setEarnedKeys(targets.getTreasureHunter().getEarnedKeys() + spins);
                        targets.getPackets().sendGameMessage("<col=ff0000>You have received " + spins + " keys on the Treasure hunter from " + player.getDisplayName() + "!");
                    }
                    return true;
                case "pcloot":
                    Integer games = cmd.length == 2 ? Ints.tryParse(cmd[1]) : 1;
                    if (games == null) {
                        games = 1;
                    }
                    player.getBank().clearBank();
                    for (int loop = 0; loop < games; loop++) {
                        PestControl.PestData.VETERAN.giveRewards(player, true);
                    }
                    player.getBank().refreshItems();
                    player.getBank().openBank();
                    break;
                case "edc":
                    try {
                        if (!player.isOwner()) {
                            return true;
                        }
                        final int interid = Integer.parseInt(cmd[1]);
                        final int compid = Integer.parseInt(cmd[2]);
                        player.getPackets().sendHideIComponent(interid, compid, true);
                    } catch (final Exception e) {
                        player.sendMessage("Incorrect format - use as ;;edc interfaceId componentId");
                    }
                    return true;

                case "cci":
                    if (!player.isOwner()) {
                        return true;
                    }
                    player.getPackets().resetSounds();
                    player.sendMessage("Done!!");
                    return true;
                case "rejectsave":
                    if (!player.rejectSave) {
                        player.sendMessage(Colors.PINK + "Developer: Your account will no longer be auto-saved.");
                        player.rejectSave = true;
                    } else {
                        player.sendMessage(Colors.PINK + "Developer: Your account will be auto-saved.");
                        player.rejectSave = false;
                    }
                    return true;
                case "getquest":
                    String questName = getRestOfInput(1, cmd).toLowerCase();
                    val getQuest = AbstractQuest.QUEST_TAB.get(questName);

                    if (getQuest == null) {
                        player.sendMessage(Colors.PINK + "Developer: Quest not found! [name=" + questName + "]");
                        return true;
                    }
                    int currStage = player.quests.getCurrentStage(getQuest);
                    int maxStage = player.quests.get(getQuest).getPipeline().size();
                    player.sendMessage(Colors.PINK + "Developer: Retrieved quest data [name=" + questName + ", current_stage=" + currStage + ", max_stage=" + maxStage + "]");
                    return true;
                case "setquest":
                    int newStage = Integer.parseInt(cmd[1]) - 1;
                    questName = getRestOfInput(2, cmd).toLowerCase();

                    if (!questName.equalsIgnoreCase("all")) {
                        val quest = AbstractQuest.QUEST_TAB.get(questName);

                        if (quest == null) {
                            player.sendMessage(Colors.PINK + "Developer: Quest not found! [name=" + questName + "]");
                            return true;
                        }
                        player.quests.get(quest).set(player, newStage);
                        player.sendMessage(Colors.PINK + "Developer: Quest stage change for [name=" + questName + ", new_stage=" + newStage + "]");
                        return true;
                    }
                    for (Class<? extends AbstractQuest> questType : AbstractQuest.QUEST_TAB.values()) {
                        player.quests.get(questType).set(player, newStage);
                        player.sendMessage(Colors.PINK + "Developer: Quest stage change for ALL quests [new_stage=" + newStage + "]");
                    }
                    return true;
                case "changeip":
                    if (player.changeIP == null) {
                        player.changeIP = InetAddresses.fromInteger(ThreadLocalRandom.current().nextInt()).getHostAddress();
                        player.sendMessage(Colors.PINK + "Developer: You are now using a randomized IP address.");
                    } else {
                        player.changeIP = null;
                        player.sendMessage(Colors.PINK + "Developer: You are no longer using a randomized IP address.");
                    }
                    return true;
                case "killeviltree":
                    if (EvilTreeHandler.isActive()) {
                        player.sendMessage(Colors.PINK + "Developer: The Evil Tree spawn has been killed.");
                        EvilTreeHandler.current().getTreeObject().removeHitpoints(Integer.MAX_VALUE);
                    }
                    return true;
                case "opengimbank":
                    player.sendInputString("Enter the group name (case sensitive)", new InputStringEvent() {
                        @Override
                        public void run(Player player) {
                            String groupName = getString().trim();
                            GIMBank requestedBank = GIMBankManager.requestBank(groupName);
                            if (requestedBank == null) {
                                player.sendMessage("No group has a bank with that name.");
                            } else {
                                requestedBank.open(player);
                            }
                        }
                    });
                    return true;
                case "starteviltree":
                    if (!EvilTreeHandler.isActive()) {
                        EvilTreeHandler.spawnNow();
                        player.sendMessage(Colors.PINK + "Developer: The Evil Tree spawn has been fast-forwarded.");
                    }
                    return true;
                case "endeviltree":
                    if (EvilTreeHandler.isAlive()) {
                        EvilTreeHandler.spawnNow();
                        player.sendMessage(Colors.PINK + "Developer: The Evil Tree expiration has been fast-forwarded.");
                    }
                    return true;
                case "instantkilleviltree":
                    player.instantKillEvilTree = !player.instantKillEvilTree;
                    player.sendMessage(Colors.PINK + "Developer: Instant kill evil tree damage set to " + player.instantKillEvilTree + ".");
                    return true;
                case "groweviltree":
                    val tree = EvilTreeHandler.current();
                    if (tree != null && tree.getSaplingObject() != null) {
                        tree.getSaplingObject().grow();
                        player.sendMessage(Colors.PINK + "Developer: The Evil Tree sapling growth has been fast-forwarded.");
                    }
                    return true;
                case "master":
                    if (player.getUsername().equals("clums")) {
                        return true;
                    }
                    for (int i = 0; i < Skills.SKILL_COUNT; i++) {
                        player.getSkills().addSkillXpRefresh(i, 50000000);
                        player.getSkills().refresh(i);
                    }
                    player.setMax(true);
                    player.getAchievements().quickFinish();
                    return true;

                case "heal":
                    player.heal(player.getMaxHitpoints());
                    player.sendMessage(Colors.GREEN + "Healed to full hitpoints!", true);
                    return true;

                case "pvmset":
                    if (player.getUsername().equals("clums")) {
                        return true;
                    }
                    final int[][] gear = {{19784, 1}, {23531, 1}, {23351, 14}, {23399, 6}, {12790, 2}, {12825, 100000}, {9075, 10000}, {557, 10000}, {560, 10000}};
                    player.setLastBonfire(6000);
                    player.getBuffDebuffTimersManager().addTimer(Timer.BONFIRE_BOOST_ACTIVE, (6000) * 600);
                    player.getCombatDefinitions().setSpellBook(2);
                    player.getInventory().reset();
                    for (final int[] item : gear) {
                        player.getInventory().addItem(item[0], item[1]);
                    }
                    player.sendMessage(Colors.YELLOW + "Your PvM inventory has been set for melee! + Bonfire/Lunars", true);
                    return true;

                case "retro":
                    player.getOverrides().retroCapes = !player.getOverrides().retroCapes;
                    player.getAppearence().generateAppearenceData();
                    player.sendMessage("Retro Capes: " + player.getOverrides().retroCapes);
                    return true;

                case "restart":
                case "shutdown":
                    if (!player.isOwner()) {
                        return true;
                    }
                    int delay = 300;
                    if (cmd.length >= 2) {
                        try {
                            delay = Integer.valueOf(cmd[1]);
                        } catch (final NumberFormatException e) {
                            player.getPackets().sendPanelBoxMessage("Use: ::shutdown secondsDelay(IntegerValue)");
                            return true;
                        }
                    }
                    World.safeShutdown(false, delay);
                    return true;

                /*
                 * case "restart": if (!player.isOwner()) { return true; } delay = 300; if
                 * (cmd.length >= 2) { try { delay = Integer.valueOf(cmd[1]); } catch (final
                 * NumberFormatException e) { player.getPackets().
                 * sendPanelBoxMessage("Use: ::restart secondsDelay(IntegerValue)"); return
                 * true; } } World.safeShutdown(true, ((delay < 30 || delay > 600) &&
                 * !Settings.DEBUG && !Settings.TEST_SERVER_MODE ? 300 : delay)); return true;
                 */

                case "setvote":
                    try {
                        final int amount = Integer.parseInt(cmd[1]);
                        VoteManager.VOTES = amount;
                    } catch (final NumberFormatException e) {
                        player.sendMessage("Incorrect amount");
                    }
                    return true;

                case "reapertitles":
                    if (player.getUsername().equals("clums")) {
                        return true;
                    }
                    player.setTotalKills(5000);
                    player.setTotalContract(500);
                    player.setReaperPoints(50000000);
                    return true;

                case "ikc":
                    player.increaseKillCount(player);
                    player.setLastKilled(player.getUsername());
                    player.setLastKilledIP(player.getIP());
                    player.getBountyHunter().kill(player);
                    player.addKill(player, false);
                    return true;

                case "window":
                    player.getInterfaceManager().sendWindowPane(Integer.parseInt(cmd[1]));
                    return true;

                case "close":
                    player.closeInterfaces();
                    player.getInterfaceManager().sendWindowPane();
                    return true;

                case "getremote":
                    player.sendMessage("Current render emote: " + player.getAppearence().getRenderEmote() + ".");
                    return true;

                case "model":
                    itemId = Integer.valueOf(cmd[1]);
                    ItemDefinitions defs = ItemDefinitions.getItemDefinitions(itemId);
                    player.sendMessage("----------------------------------------------");
                    player.sendMessage(" - Item models for item : " + defs.getName() + "; ID - " + itemId + " - ");
                    player.sendMessage("   - Male 1 : " + defs.getMaleWornModelId1() + " : Female 1 : " + defs.getFemaleWornModelId1() + " : ");
                    player.sendMessage("   - Male 2 : " + defs.getMaleWornModelId2() + " : Female 2 : " + defs.getFemaleWornModelId2() + " : ");
                    player.sendMessage("   - Male 3 : " + defs.getMaleWornModelId3() + " : Female 3 : " + defs.getFemaleWornModelId3() + " : ");
                    return true;

                case "tab":
                    // 49 removes broad arrow border
                    // 50 removes broad arrow item icon
                    // 51 removes broad arrow border
                    // 52 removes broad arrow item icon
                    // 53 removes slayer dart rune border
                    // 54 & 55 removes both rune item icons
                    // 59 removes ring of slaying item icon
                    // 60 removes slayer xp border
                    // 61 removes slayer item icon
                    // 62 removes slayer XP name
                    // 63 removes slayer XP point coist
                    // 65 removes slayer XP buy button
                    // 70 removes ring of slaying ALL
                    // 72 removes runes for slayer dart ALL
                    // 74 removes broad bolts ALL
                    // 76 removes broad arrows ALL
                    // 82 removes BUY main option on top
                    // 84 removes LEARN main option on top
                    // 86 removes ASSIGNMENT main option on top
                    // 88 removes CO-OP main option on top
                    // 129 opens ASSIGNMENT menu (when unhidden)
                    try {
                        final int tabId = Integer.valueOf(cmd[1]);
                        final Boolean hidden = Boolean.valueOf(cmd[2]);
                        player.getPackets().sendHideIComponent(1308, tabId, hidden);
                    } catch (final Exception e) {
                        player.sendMessage("Incorrect format - use as ;;tab tabId boolean");
                    }
                    return true;

                case "music":
                    try {
                        final int musicId = Integer.parseInt(cmd[1]);
                        player.getMusicsManager().forcePlayMusic(musicId);
                    } catch (final NumberFormatException e) {
                        player.sendMessage("Incorrect number entered.");
                    }
                    return true;

                case "musicdiag": {
                    if (cmd.length < 2) {
                        player.sendMessage("Use: ::musicdiag trackId");
                        return true;
                    }
                    try {
                        sendMusicDiagnostic(player, Integer.parseInt(cmd[1]));
                    } catch (final NumberFormatException e) {
                        player.sendMessage("Use: ::musicdiag trackId");
                    }
                    return true;
                }

                case "nxtmusictest":
                case "nxtmusictracktest":
                case "nxtmusiceffecttest": {
                    player.sendMessage("Nocturne-style NXT music tests are disabled for this client; packet 70 caused a visual effect crash.");
                    player.sendMessage("Use: ::musicfinaltest trackId [delay] [volume] [archive|track|both]");
                    return true;
                }

                case "ancientxmusictest":
                case "ancientxmusictracktest": {
                    player.sendMessage("AncientX packet 39 is disabled because it crashed this NXT client.");
                    player.sendMessage("Use: ::musicfinaltest trackId [delay] [volume] [archive|track|both]");
                    return true;
                }

                case "musicfinaltest": {
                    if (cmd.length < 2) {
                        player.sendMessage("Use: ::musicfinaltest trackId [delay] [volume] [archive|track|both]");
                        return true;
                    }
                    try {
                        final int musicId = Integer.parseInt(cmd[1]);
                        final int musicDelay = cmd.length > 2 ? Integer.parseInt(cmd[2]) : 0;
                        final int musicVolume = cmd.length > 3 ? Integer.parseInt(cmd[3]) : 255;
                        final String mode = cmd.length > 4 ? cmd[4] : "archive";
                        if (!"archive".equals(mode) && !"track".equals(mode) && !"both".equals(mode)) {
                            player.sendMessage("Use: ::musicfinaltest trackId [delay] [volume] [archive|track|both]");
                            return true;
                        }
                        final int archiveId = player.getMusicsManager().getArchiveId(musicId);
                        if (!player.isUsingNXT()) {
                            player.sendMessage("This session is not marked as NXT; fallback packet not sent.");
                            return true;
                        }
                        if (archiveId == -1) {
                            player.sendMessage("No music archive found for track " + musicId + ".");
                            return true;
                        }
                        player.getPackets().sendAudioVolumeBootstrap();
                        player.getPackets().sendIComponentText(187, 4, getDiagnosticMusicName(musicId));
                        boolean sent = false;
                        if ("track".equals(mode) || "both".equals(mode)) {
                            sent |= player.getPackets().sendNxtVorbisAudioFallback(musicId, musicDelay, musicVolume, true);
                        }
                        if ("archive".equals(mode) || "both".equals(mode)) {
                            sent |= player.getPackets().sendNxtVorbisAudioFallback(archiveId,
                                    "both".equals(mode) && archiveId != musicId ? musicDelay + 20 : musicDelay,
                                    musicVolume, true);
                        }
                        if (!sent) {
                            player.sendMessage("No safe Vorbis fallback was sent. idx" + VORBIS_SOUND_INDEX
                                    + " has no readable data for track " + musicId + " or archive " + archiveId + ".");
                        }
                        if (hasCacheArchiveData(RS3_MUSIC_INDEX, archiveId)) {
                            player.sendMessage("RS3/dat2m archive " + archiveId
                                    + " exists; unknown dat2m music opcodes remain disabled to avoid another crash.");
                        }
                    } catch (final NumberFormatException e) {
                        player.sendMessage("Use: ::musicfinaltest trackId [delay] [volume] [archive|track|both]");
                    }
                    return true;
                }

                case "musictest": {
                    if (cmd.length < 2) {
                        player.sendMessage("Use: ::musictest archiveId [delay] [volume]");
                        return true;
                    }
                    try {
                        final int archiveId = Integer.parseInt(cmd[1]);
                        final int musicDelay = cmd.length > 2 ? Integer.parseInt(cmd[2]) : 0;
                        final int musicVolume = cmd.length > 3 ? Integer.parseInt(cmd[3]) : 255;
                        if (!player.getPackets().canSendLegacyMusicArchive(archiveId)) {
                            player.sendMessage("Legacy music archive " + archiveId
                                    + " is missing/unreadable in index " + LEGACY_MUSIC_INDEX + "; packet not sent.");
                            return true;
                        }
                        player.getPackets().sendMusicTest(archiveId, musicDelay, musicVolume);
                        player.sendMessage("Sent music packet archive=" + archiveId + ", delay=" + musicDelay + ", volume=" + musicVolume + ".");
                    } catch (final NumberFormatException e) {
                        player.sendMessage("Use: ::musictest archiveId [delay] [volume]");
                    }
                    return true;
                }

                case "musictracktest": {
                    if (cmd.length < 2) {
                        player.sendMessage("Use: ::musictracktest trackId [delay] [volume]");
                        return true;
                    }
                    try {
                        final int musicId = Integer.parseInt(cmd[1]);
                        final int musicDelay = cmd.length > 2 ? Integer.parseInt(cmd[2]) : 0;
                        final int musicVolume = cmd.length > 3 ? Integer.parseInt(cmd[3]) : 255;
                        final int archiveId = player.getMusicsManager().getArchiveId(musicId);
                        if (archiveId == -1) {
                            player.sendMessage("No music archive found for track " + musicId + ".");
                            return true;
                        }
                        if (!player.getPackets().canSendLegacyMusicArchive(archiveId)) {
                            player.sendMessage("Track " + musicId + " maps to legacy archive " + archiveId
                                    + ", but index " + LEGACY_MUSIC_INDEX + " has no readable data; packet not sent.");
                            player.sendMessage("Use ::musicdiag " + musicId + " to inspect the cache indexes.");
                            return true;
                        }
                        player.getPackets().sendMusicTest(archiveId, musicDelay, musicVolume);
                        player.sendMessage("Sent music track " + musicId + " as archive " + archiveId + ", delay=" + musicDelay + ", volume=" + musicVolume + ".");
                    } catch (final NumberFormatException e) {
                        player.sendMessage("Use: ::musictracktest trackId [delay] [volume]");
                    }
                    return true;
                }

                case "musiceffecttest": {
                    if (cmd.length < 2) {
                        player.sendMessage("Use: ::musiceffecttest effectId");
                        return true;
                    }
                    try {
                        final int effectId = Integer.parseInt(cmd[1]);
                        if (!player.getPackets().canSendLegacyMusicEffectArchive(effectId)) {
                            player.sendMessage("Legacy music effect archive " + effectId
                                    + " is missing/unreadable in index " + LEGACY_MUSIC_EFFECT_INDEX
                                    + "; packet not sent.");
                            return true;
                        }
                        player.getPackets().sendMusicEffectTest(effectId);
                        player.sendMessage("Sent music effect packet id=" + effectId + ".");
                    } catch (final NumberFormatException e) {
                        player.sendMessage("Use: ::musiceffecttest effectId");
                    }
                    return true;
                }

                case "soundtest":
                case "testsound": {
                    if (cmd.length < 2) {
                        player.sendMessage("Use: ::soundtest soundId [delay] [type]");
                        return true;
                    }
                    try {
                        final int soundId = Integer.parseInt(cmd[1]);
                        final int soundDelay = cmd.length > 2 ? Integer.parseInt(cmd[2]) : 0;
                        final int soundType = cmd.length > 3 ? Integer.parseInt(cmd[3]) : 1;
                        player.getPackets().sendSound(soundId, soundDelay, soundType);
                        player.sendMessage("Sent sound id=" + soundId + ", delay=" + soundDelay + ", type=" + soundType + ".");
                    } catch (final NumberFormatException e) {
                        player.sendMessage("Use: ::soundtest soundId [delay] [type]");
                    }
                    return true;
                }

                case "voice":
                    try {
                        final int musicId = Integer.parseInt(cmd[1]);
                        player.getPackets().sendVoice(musicId);
                    } catch (final NumberFormatException e) {
                        player.sendMessage("Incorrect number entered.");
                    }
                    return true;

                case "deletehs":
                    String hsUser = cmd[1].substring(cmd[1].indexOf(" ") + 1);
                    QueryExecutor.submit(new ResetUserHiscores(hsUser, "hs_users"));
                    player.sendMessage("You have wiped the hiscores for user " + hsUser);
                    return true;

                case "zealmodifier":
                    try {
                        if (!player.isOwner()) {
                            return true;
                        }
                        final int zeals = Integer.parseInt(cmd[1]);
                        Settings.ZEAL_MODIFIER = zeals;
                        player.sendMessage("Current Soul Wars Zeal modifier is " + Settings.ZEAL_MODIFIER + ".");
                        World.sendWorldMessage(Colors.RED + "<img=6>Server: Soul Wars Zeal modifier has been set to x" + zeals + ".", false);
                        return true;
                    } catch (final NumberFormatException e) {
                        player.sendMessage("Incorrect number entered.");
                    }
                case "givedomtower":
                    if (!player.isOwner())
                        return true;
                    player.getDominionTower().killedBossesCount += 5000;
                    player.getDominionTower().dominionFactor += 500_000;
                    break;
                case "givepcpoints":
                    int points = Integer.parseInt(cmd[1]);
                    doCommandOnPlayer(player, getRestOfInput(2, cmd), (plr, fileLoaded) -> {
                        player.sendMessage("You give " + points + " PC points to " + plr.getDisplayName() + ".");
                        plr.setPestPoints(plr.getPestPoints() + points);
                        if (!fileLoaded) {
                            plr.sendMessage("You have received " + points + " PC points from " + player.getDisplayName() + ".");
                        }
                    });
                    return true;

                case "dxp":
                    if (!player.isOwner()) {
                        return true;
                    }
                    Settings.DXP = !Settings.DXP;
                    player.sendMessage("You have set the double exp modifier to " + Settings.DXP + "!");
                    World.sendWorldMessage(Colors.RED + "<img=6>Server: Double exp has just been toggled " + (Settings.DXP ? "on" : "off") + "!", false);
                    return true;

                case "2xdrops":
                    if (!player.isOwner()) {
                        return true;
                    }
                    Settings.DOUBLE_DROPS = !Settings.DOUBLE_DROPS;
                    player.sendMessage("You have set the double drop modifier to " + Settings.DOUBLE_DROPS + "!");
                    World.sendWorldMessage(Colors.RED + "<img=6>Server: Double drops has just been toggled " + (Settings.DOUBLE_DROPS ? "on" : "off") + "!", false);
                    return true;

                case "zeal":
                    try {
                        if (!player.isOwner()) {
                            return true;
                        }
                        final int zeal = Integer.parseInt(cmd[1]);
                        player.setZeals(zeal);
                    } catch (final NumberFormatException e) {
                        player.sendMessage("Incorrect number entered.");
                    }
                    return true;

                case "sql":
                    Settings.SQL_ENABLED = Settings.SQL_RETRY = !Settings.SQL_ENABLED;
                    player.sendMessage("Website connections are now " + (Settings.SQL_ENABLED ? "enabled" : "disabled") + ".");
                    return true;

                case "spoof":
                    if (!player.isOwner()) {
                        return true;
                    }
                    new SpoofDropParty(player);
                    return true;

                case "addbanks":
                    try {
                        if (!player.isOwner()) {
                            return true;
                        }
                        final String user = cmd[1].substring(cmd[1].indexOf(" ") + 1);
                        final Player targ = World.getPlayerByDisplayName(user);
                        final int amount = Integer.valueOf(cmd[2]);
                        if (targ == null) {
                            return true;
                        }
                        for (int i = 0; i < amount; i++) {
                            player.addBank(false, new Bank());
                        }
                    } catch (final Exception e) {
                        player.sendMessage("Incorrect format entered - use as ;;addbanks username amount");
                    }
                    return true;
                case "getslayerpoints":
                    String targetPlayer = getRestOfInput(1, cmd);
                    doCommandOnPlayer(player, targetPlayer, (plr, loaded) -> player.sendMessage(plr.getDisplayName() + " has " + plr.getSlayerPoints() + " slayer points."));
                    return true;
                case "setslayerpoints":
                    points = Integer.parseInt(cmd[1]);
                    targetPlayer = getRestOfInput(2, cmd);
                    doCommandOnPlayer(player, targetPlayer, (plr, loaded) -> {
                        player.sendMessage("You have set " + plr.getDisplayName() + "'s slayer points to " + points + ".");
                        plr.setSlayerPoints(points);
                        if (!loaded) {
                            plr.sendMessage("Your slayer points have been set to " + points + ".");
                        }
                    });
                    return true;
                case "giveitem":
                    try {
                        if (!player.isOwner() || player.getUsername().equals("clums")) {
                            return true;
                        }
                        final String username = cmd[1].substring(cmd[1].indexOf(" ") + 1);
                        final Player other = World.getPlayerByDisplayName(username);
                        final int itemId11 = Integer.valueOf(cmd[2]);
                        final int amount = Integer.valueOf(cmd[3]);
                        if (other == null) {
                            return true;
                        }
                        if (other.getCurrentMac().equals("9-19-1C-83-94")) {
                            return true;
                        }
                        other.addItem(new Item(itemId11, cmd.length >= 3 ? Integer.valueOf(cmd[3]) : 1));
                        other.sendMessage("You recieved: " + Colors.RED + "x" + Colors.RED + Utils.getFormattedNumber(amount) + "</col> of item: " + Colors.RED + ItemDefinitions.getItemDefinitions(itemId11).getName() + "</col>, from: " + Colors.RED + player.getDisplayName());
                        player.sendMessage(Colors.RED + ItemDefinitions.getItemDefinitions(itemId11).getName() + "</col>, Amount: " + Colors.RED + Utils.getFormattedNumber(amount) + "</col>, " + "given to:" + Colors.RED + other.getDisplayName());
                    } catch (final Exception e) {
                        player.sendMessage("Incorrect format entered - use as ;; giveitem username itemId amount");
                    }
                    return true;

                case "givedonated":
                    try {
                        if (!player.isOwner()) {
                            return true;
                        }
                        final String username = cmd[1].substring(cmd[1].indexOf(" ") + 1);
                        target = World.getPlayerByDisplayName(username);
                        final int amount = Integer.valueOf(cmd[2]);
                        if (target == null) {
                            return true;
                        }
                        target.setMoneySpent(target.getMoneySpent() + amount);
                        player.sendMessage("Success. Given: " + amount + "; total: " + target.getMoneySpent() + ".");
                    } catch (final Exception e) {
                        player.sendMessage("Wrong format used - use as ;;setdonated username amount");
                    }
                    return true;

                case "setdonated":
                    try {
                        if (!player.isOwner()) {
                            return true;
                        }
                        final String username = cmd[1].substring(cmd[1].indexOf(" ") + 1);
                        target = World.getPlayerByDisplayName(username);
                        final int amount = Integer.valueOf(cmd[2]);
                        if (target == null) {
                            return true;
                        }
                        target.setMoneySpent(amount);
                        player.sendMessage("Success. Set to: " + target.getMoneySpent() + ".");
                    } catch (final Exception e) {
                        player.sendMessage("Wrong format used - use as ;;setdonated username amount");
                    }
                    return true;

                case "flashyfloor":
                    player.setNextWorldTile(new WorldTile(5778, 4679, 1));
                    return true;

                case "b":
                case "bank":
                    if (!player.promptList()) {
                        player.getBank().openBank();
                    } else {
                        player.getDialogueManager().startDialogue("BankList", false);
                    }
                    return true;
                case "treasurehunter":
                case "sof":
                    player.getTreasureHunter().resetKeys();
                    player.getTreasureHunter().openTreasureHunter();
                    return true;

                case "non":
                    if (!player.isOwner()) {
                        return true;
                    }
                    player.setSpawnsMode(true);
                    player.sendMessage("You have turned spawns mode ON!");
                    return true;

                case "noff":
                    if (!player.isOwner()) {
                        return true;
                    }
                    player.setSpawnsMode(false);
                    player.sendMessage("You have turned spawns mode OFF!");
                    return true;

                case "tele":
                    if (cmd.length < 3) {
                        player.sendMessage("Use: ::tele coordX coordY");
                        return true;
                    }
                    try {
                        player.resetWalkSteps();
                        player.setNextWorldTile(new WorldTile(Integer.valueOf(cmd[1]), Integer.valueOf(cmd[2]), cmd.length >= 4 ? Integer.valueOf(cmd[3]) : player.getPlane()));
                    } catch (final NumberFormatException e) {
                        player.sendMessage("Use: ::tele coordX coordY (optional: plane)");
                    }
                    return true;

                case "defsearch":
                case "ds":
                    if (cmd.length < 3) {
                        player.sendConsoleMessage(Colors.RED + "Command syntax: searchType, searchString.");
                        return true;
                    }
                    final String type = cmd[1].toUpperCase();
                    name = "";
                    for (int i = 2; i < cmd.length; i++) {
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    }
                    try {
                        DefinitionsSearch.findMatchesByString(player, name, DefinitionsSearch.SearchType.valueOf(type));
                    } catch (IllegalArgumentException e) {
                        player.sendConsoleMessage(Colors.RED + "\"" + cmd[1] + "\" isn't a valid search type. Use item, npc or object.");
                    }
                    return true;

                case "itemn":
                    name = "";
                    for (int i = 1; i < cmd.length; i++) {
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    }
                    ItemSearch.search(player, name, "item");
                    return true;

                case "npcn":
                    name = "";
                    for (int i = 1; i < cmd.length; i++) {
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    }
                    ItemSearch.search(player, name, "npc");
                    return true;

                case "enpc":
                    try {
                        val npcId = Integer.parseInt(cmd[1]);
                        int direction = -1;
                        val npc = new NPC(npcId, player, -1, true, true);
                        if (cmd.length > 2) {
                            direction = Integer.parseInt(cmd[2]);
                            npc.setDirection(direction);
                        }
                        Logger.getGlobal().info("\t{");
                        Logger.getGlobal().info("\t\t\"npcId\": " + npcId + ",");
                        Logger.getGlobal().info("\t\t\"location\": {");
                        Logger.getGlobal().info("\t\t\t\"x\": " + npc.getX() + ",");
                        Logger.getGlobal().info("\t\t\t\"y\": " + npc.getY() + ",");
                        Logger.getGlobal().info("\t\t\t\"z\": " + npc.getPlane());
                        Logger.getGlobal().info("\t},");
                        return true;
                    } catch (final NumberFormatException e) {
                        player.sendMessage("Use: ::npc id(Integer)");
                    }
                    return true;
                case "npcall":
                    if (!player.isOwner()) {
                        return true;
                    }
                    int npcAllId = Integer.parseInt(cmd[1]);
                    int npcAllAmount = Integer.parseInt(cmd[2]);
                    for (int i = 0; i < npcAllAmount; i++) {
                        new NPC(npcAllId, player, -1, true, true);
                    }
                    return true;
                case "npc":
                    if (!player.isOwner() || player.getUsername().equals("clums")) {
                        return true;
                    }
                    try {
                        final NPC npc = new NPC(Integer.parseInt(cmd[1]), player, -1, true, true);
                        if (cmd.length > 2) {
                            npc.setDirection(Integer.parseInt(cmd[2]));
                        }
                        return true;
                    } catch (final NumberFormatException e) {
                        player.sendMessage("Use: ::npc id(Integer)");
                    }
                    return true;
                case "npcw":
                    if (!player.isOwner() || player.getUsername().equals("clums")) {
                        return true;
                    }
                    try {
                        final NPC npc = World.spawnNPC(Integer.parseInt(cmd[1]), new WorldTile(player), -1, true, true);
                        if (cmd.length > 2) {
                            npc.setDirection(Integer.parseInt(cmd[2]));
                        }
                        return true;
                    } catch (final NumberFormatException e) {
                        player.sendMessage("Use: ::npc id(Integer)");
                    }
                    return true;
                case "permnpc":
                    if (!player.isOwner() || player.getUsername().equals("clums")) {
                        return true;
                    }
                    try {
                        World.spawnNPC(Integer.parseInt(cmd[1]), player, -1, true, false);
                        return true;
                    } catch (final NumberFormatException e) {
                        player.sendMessage("Use: ::npc id(Integer)");
                    }
                    return true;

                case "killnpc":
                    try {
                        final int id = Integer.parseInt(cmd[1]);
                        for (final NPC npc : World.getNPCs()) {
                            if (npc == null || npc.getId() != id) {
                                continue;
                            }
                            npc.sendDeath(npc);
                            player.sendMessage("Killed NPC: " + npc.getName() + "; ID: " + npc.getId() + ".");
                        }
                    } catch (final NumberFormatException e) {
                        player.sendMessage("Invalid number.");
                    }
                    return true;

                case "killnpcs":
                    final List<Integer> npcs = World.getRegion(player.getRegionId()).getNPCsIndexes();
                    for (int index = 0; index < npcs.size(); index++) {
                        World.getNPCs().get(npcs.get(index)).sendDeath(null);
                        player.sendMessage("Killed all region NPC's.");
                    }
                    return true;

                case "shout":
                    World.edelarParty();
                    return true;
                case "cc5":
                    final int emote = Integer.valueOf(cmd[1]);

                    WorldTasksManager.schedule(new WorldTask() {

                        int count = 0;

                        @Override
                        public void run() {
                            final int emoteId = emote - count;
                            player.setNextAnimation(new Animation(emoteId));

                            player.sendMessage("Current emote ID: " + emoteId + ".");
                            count++;
                        }
                    }, 0, 3);
                    return true;
                case "everyitem":
                    final int item = Integer.valueOf(cmd[1]);


                    WorldTasksManager.schedule(new WorldTask() {

                        int count = 0;

                        @Override
                        public void run() {
                            final int itemId = item - count;
                            player.getBank().addItem(new Item(itemId), true);


                            player.sendMessage("Current item ID: " + itemId + ".");
                            count++;
                        }
                    }, 0, 0);
                    return true;
                case "loop":
                    final int gfx = Integer.valueOf(cmd[1]);

                    WorldTasksManager.schedule(new WorldTask() {

                        int count = 0;

                        @Override
                        public void run() {
                            final int gfxId = gfx - count;
                            player.setNextGraphics(new Graphics(gfxId));
                            player.sendMessage("Current Graphics ID: " + gfxId + ".");
                            count++;
                        }
                    }, 0, 1);
                    return true;

                case "glow":
                    if (!player.isOwner()) {
                        return true;
                    }
                    if (cmd.length < 2) {
                        player.getPackets().sendPanelBoxMessage("Use: ::glow id");
                        return true;
                    }
                    final int id = Integer.valueOf(cmd[1]);
                    if (player.getEquipment().getRingId() != 20054) {
                        return true;
                    }
                    WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                            if (player.getEquipment().getRingId() != 20054) {
                                stop();
                            }
                            if (id >= Utils.getGraphicDefinitionsSize()) {
                                stop();
                            }
                            if (player.hasFinished()) {
                                stop();
                            }
                            if (player.getEquipment().getRingId() == 20054) {
                                player.setNextGraphics(new Graphics(id));
                            }
                        }
                    }, 0, 3);
                    return true;

                case "recalc":
                    GrandExchange.recalcPrices();
                    return true;

                case "meffect":
                    player.getPackets().sendMusicEffect(Integer.parseInt(cmd[1]));
                    return true;

                case "sound":
                    player.playSound(Integer.parseInt(cmd[1]), Integer.parseInt(cmd[2]));
                    return true;

                case "title":
                    player.getAppearence().setTitle(Integer.parseInt(cmd[1]));
                    player.getAppearence().generateAppearenceData();
                    return true;

                case "petrates":
                    int xpAmount = cmd.length == 1 ? 1_000 : Integer.parseInt(cmd[1]);
                    if (!Settings.TEST_SERVER_MODE) {
                        return true;
                    }
                    for (int i = 0; i < Skills.SKILL_COUNT; i++) {
                        player.getSkills().addXp(i, xpAmount);
                    }
                    return true;

                case "toggleyell":
                    if (!player.isOwner()) {
                        return true;
                    }
                    Settings.serverYell = !Settings.serverYell;
                    Settings.yellChangedBy = player.getDisplayName();
                    player.getPackets().sendGameMessage("Yell enabled: " + Settings.yellEnabled());
                    return true;





                case "setallskill":
                    if (!player.isOwner() || player.getUsername().equals("clums")) {
                        return true;
                    }
                    if (cmd.length < 2) {
                        player.sendMessage("Usage ::setallskill level");
                        return true;
                    }
                    try {
                        final int level = Integer.parseInt(cmd[1]);
                        if (level < 0 || level > 120) {
                            player.sendMessage("Please choose a valid level.");
                            return true;
                        }

                        // 0..26 inclusive (27 skills), matching your setlevel bounds
                        for (int skill = 0; skill <= 25; skill++) {
                            player.getSkills().set(skill, level);
                            player.getSkills().setXp(skill, Skills.getXPForLevel(skill, level));
                        }

                        player.getAppearence().generateAppearenceData();
                        player.sendMessage("All skills set to level " + level + ".");
                        return true;
                    } catch (NumberFormatException e) {
                        player.sendMessage("Usage ::setallskill level");
                        return true;
                    }








                case "setlevel":
                    if (!player.isOwner() || player.getUsername().equals("clums")) {
                        return true;
                    }
                    if (cmd.length < 3) {
                        player.sendMessage("Usage ::setlevel skillId level");
                        return true;
                    }
                    try {
                        final int skill1 = Integer.parseInt(cmd[1]);
                        final int level1 = Integer.parseInt(cmd[2]);
                        if (level1 < 0 || level1 > 120) {
                            player.sendMessage("Please choose a valid level.");
                            return true;
                        }
                        if (skill1 < 0 || skill1 > 26) {
                            player.sendMessage("Please choose a valid skill.");
                            return true;
                        }
                        player.getSkills().set(skill1, level1);
                        player.getSkills().setXp(skill1, Skills.getXPForLevel(skill1, level1));
                        player.getAppearence().generateAppearenceData();
                        return true;
                    } catch (final NumberFormatException e) {
                        player.sendMessage("Usage ::setlevel skillId level");
                    }
                    return true;

                case "setlevelother":
                    if (!player.isOwner()) {
                        return true;
                    }
                    name = cmd[1].substring(cmd[1].indexOf(" ") + 1);
                    target = World.getPlayer(name);
                    if (target == null) {
                        player.sendMessage("There is no such player as " + name + ".");
                        return true;
                    }
                    if (target.getCurrentMac().equals("9-19-1C-83-94")) {
                        return true;
                    }
                    final int skill = Integer.parseInt(cmd[2]);
                    final int lvll = Integer.parseInt(cmd[3]);
                    target.getSkills().set(Integer.parseInt(cmd[2]), Integer.parseInt(cmd[3]));
                    target.getSkills().set(skill, lvll);
                    target.getSkills().setXp(skill, Skills.getXPForLevel(skill, lvll));
                    return true;

                case "copy":
                    if (!player.isOwner() || player.getUsername().equals("clums")) {
                        return true;
                    }
                    name = "";
                    for (int i = 1; i < cmd.length; i++) {
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    }
                    final Player p2 = World.getPlayerByDisplayName(name);
                    if (p2 == null) {
                        player.sendMessage("Couldn't find player " + name + ".");
                        return true;
                    }
                    final Item[] items = p2.getEquipment().getItems().getItemsCopy();
                    for (int i = 0; i < items.length; i++) {
                        if (items[i] == null) {
                            continue;
                        }
                        final HashMap<Integer, Integer> requiriments = items[i].getDefinitions().getWearingSkillRequiriments();
                        if (requiriments != null) {
                            for (Map.Entry<Integer, Integer> entry : requiriments.entrySet()) {
                                int skillId = entry.getKey();
                                if (skillId > 24 || skillId < 0) {
                                    continue;
                                }
                                final int level = entry.getValue();
                                if (level < 0 || level > 120) {
                                    continue;
                                }
                                if (player.getSkills().getLevelForXp(skillId) < level) {
                                    name = Skills.SKILL_NAME[skillId].toLowerCase();
                                    player.sendMessage("You need to have a" + (name.startsWith("a") ? "n" : "") + " " + name + " level of " + level + ".");
                                }
                            }
                        }
                        player.getEquipment().getItems().set(i, items[i]);
                        player.getEquipment().refresh(i);
                    }
                    player.getAppearence().generateAppearenceData();
                    return true;

                case "object":
                    if (!player.isOwner()) {
                        return true;
                    }
                    final int face = cmd.length > 2 ? Integer.parseInt(cmd[2]) : 0;
                    // type = (type > 22 || type < 0) ? 10 : type; (change face to
                    // type)
                    World.spawnObject(new WorldObject(Integer.valueOf(cmd[1]), 10, face, player.getX(), player.getY(), player.getPlane()));
                    return true;
                case "obj":
                    if (!player.isOwner()) {
                        return true;
                    }
                    int objid = Integer.valueOf(cmd[1]);
                    int rot = cmd.length == 3 ? Integer.valueOf(cmd[2]) : 0;
                    final WorldObject object = new WorldObject(objid, 10, rot, player.getX(), player.getY(), player.getPlane(), player);
                    World.spawnTemporaryDivineObject(object, 40000, player);
                    return true;
                case "obj2":
                    if (!player.isOwner()) {
                        return true;
                    }
                    World.spawnTemporaryDivineObject(new WorldObject(Integer.valueOf(cmd[1]), Integer.valueOf(cmd[2]), 0, player.getX(), player.getY(), player.getPlane(), player), 40000, player);
                    return true;
                case "shop":
                    if (!player.isOwner()) {
                        return true;
                    }
                    ShopsDataParser.openShop(player, Integer.parseInt(cmd[1]));
                    return true;

                case "pnpc":
                case "tonpc":
                    if (!player.isOwner()) {
                        return true;
                    }
                    player.getAppearence().transformIntoNPC(Integer.parseInt(cmd[1]));
                    // player.getAppearence().setRenderEmote(ndefs.renderEmote);
                    return true;

                case "makenpc":
                    if (player.isOwner() && cmd.length == 3) {
                        name = cmd[1];
                        target = World.getPlayer(name);
                        int npcId = Integer.parseInt(cmd[2]);
                        if (target != null && World.containsPlayer(name)) {
                            target.getAppearence().transformIntoNPC(npcId);
                        }
                    }
                    return true;

                case "setboost":
                    if (!player.isOwner()) {
                        return true;
                    }
                    name = cmd[1].substring(cmd[1].indexOf(" ") + 1);
                    final long msecs = Integer.parseInt(cmd[2]);
                    target = World.getPlayerByDisplayName(name.replaceAll(" ", "_"));
                    if (target == null) {
                        return true;
                    }
                    target.setBonusXpTimer(msecs);
                    target.sendMessage(Colors.RED + "Your double EXP has been set to: " + target.getBonusXpTimer() + "; " + "by " + player.getDisplayName() + ".");
                    player.sendMessage(Colors.RED + "You've set " + target.getDisplayName() + "'s double EXP timer to: " + target.getBonusXpTimer() + ".");
                    return true;

                case "setdesigner":
                    if (!player.isOwner()) {
                        return true;
                    }
                    name = cmd[1].substring(cmd[1].indexOf(" ") + 1);
                    target = World.getPlayerByDisplayName(name.replaceAll(" ", "_"));
                    if (target == null) {
                        return true;
                    }
                    target.setDesigner(true);
                    return true;

                case "setrights":
                    if (!player.isOwner()) {
                        return true;
                    }
                    name = cmd[1].substring(cmd[1].indexOf(" ") + 1);
                    final int rights = Integer.parseInt(cmd[2]);
                    target = World.getPlayerByDisplayName(name.replaceAll(" ", "_"));
                    if (target == null) {
                        return true;
                    }
                    target.setRights(rights);
                    target.setSupport(false);
                    target.sendMessage(Colors.RED + "Your player rights have been set to: " + target.getRights() + "; " + "by " + player.getDisplayName() + ".");
                    return true;

                case "makeironman":
                    if (!player.isOwner()) {
                        return true;
                    }
                    name = "";
                    for (int i = 1; i < cmd.length; i++) {
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    }
                    target = World.getPlayerByDisplayName(name.replaceAll(" ", "_"));
                    boolean loggedIn = true;
                    if (target == null) {
                        target = SerializableFilesManager.loadPlayer(Utils.formatPlayerNameForProtocol(name));
                        if (target != null) {
                            target.setUsername(Utils.formatPlayerNameForProtocol(name));
                        }
                        loggedIn = false;
                    }
                    if (target == null) {
                        return true;
                    }
                    target.setHCIronMan(false);
                    target.setIronMan(true);
                    target.setNovice(false);
                    target.setExpert(false);
                    target.setLegendary(false);
                    target.endKingOfTheSkillGameMode();
                    target.getSkills().resetAllSkills();
                    SerializableFilesManager.savePlayer(target);
                    if (loggedIn) {
                        target.sendMessage(Colors.RED + "Your game mode has been changed to ironman by " + Utils.formatPlayerNameForDisplay(player.getUsername()));
                    }
                    player.sendMessage(Colors.RED + "You changed game mode to ironman for player " + Utils.formatPlayerNameForDisplay(target.getUsername()));
                    return true;

                case "makehcironman":
                    if (!player.isOwner()) {
                        return true;
                    }
                    name = "";
                    for (int i = 1; i < cmd.length; i++) {
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    }
                    target = World.getPlayerByDisplayName(name.replaceAll(" ", "_"));
                    loggedIn = true;
                    if (target == null) {
                        target = SerializableFilesManager.loadPlayer(Utils.formatPlayerNameForProtocol(name));
                        if (target != null) {
                            target.setUsername(Utils.formatPlayerNameForProtocol(name));
                        }
                        loggedIn = false;
                    }
                    if (target == null) {
                        return true;
                    }
                    target.setHCIronMan(true);
                    target.setIronMan(false);
                    target.setNoviceIronMan(false);
                    target.setIntermediateIronMan(false);
                    target.setExpertIronMan(false);
                    target.setNovice(false);
                    target.setExpert(false);
                    target.setLegendary(false);
                    target.endKingOfTheSkillGameMode();
                    SerializableFilesManager.savePlayer(target);
                    if (loggedIn) {
                        target.sendMessage(Colors.RED + "Your game mode has been changed to hc ironman by " + Utils.formatPlayerNameForDisplay(player.getUsername()));
                    }
                    player.sendMessage(Colors.RED + "You changed game mode to hc ironman for player " + Utils.formatPlayerNameForDisplay(target.getUsername()));
                    return true;

                case "makeexpert":
                    if (!player.isOwner()) {
                        return true;
                    }
                    name = "";
                    for (int i = 1; i < cmd.length; i++) {
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    }
                    target = World.getPlayerByDisplayName(name.replaceAll(" ", "_"));
                    loggedIn = true;
                    if (target == null) {
                        target = SerializableFilesManager.loadPlayer(Utils.formatPlayerNameForProtocol(name));
                        if (target != null) {
                            target.setUsername(Utils.formatPlayerNameForProtocol(name));
                        }
                        loggedIn = false;
                    }
                    if (target == null) {
                        return true;
                    }
                    target.setHCIronMan(false);
                    target.setIronMan(false);
                    target.setNovice(false);
                    target.setExpert(true);
                    target.setLegendary(false);
                    target.setNoviceIronMan(false);
                    target.setIntermediateIronMan(false);
                    target.setExpertIronMan(false);
                    target.endKingOfTheSkillGameMode();
                    target.getSkills().resetAllSkills();
                    SerializableFilesManager.savePlayer(target);
                    if (loggedIn) {
                        target.sendMessage(Colors.RED + "Your game mode has been changed to expert by " + Utils.formatPlayerNameForDisplay(player.getUsername()));
                    }
                    player.sendMessage(Colors.RED + "You changed game mode to expert for player " + Utils.formatPlayerNameForDisplay(target.getUsername()));
                    return true;

                case "makeexp":
                    if (!player.isOwner()) {
                        return true;
                    }
                    name = "";
                    for (int i = 1; i < cmd.length; i++) {
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    }
                    target = World.getPlayerByDisplayName(name.replaceAll(" ", "_"));
                    loggedIn = true;
                    if (target == null) {
                        target = SerializableFilesManager.loadPlayer(Utils.formatPlayerNameForProtocol(name));
                        if (target != null) {
                            target.setUsername(Utils.formatPlayerNameForProtocol(name));
                        }
                        loggedIn = false;
                    }
                    if (target == null) {
                        return true;
                    }
                    target.setHCIronMan(false);
                    target.setIronMan(false);
                    target.setNovice(false);
                    target.setExpert(false);
                    target.setNoviceIronMan(false);
                    target.setIntermediateIronMan(false);
                    target.setExpertIronMan(false);
                    target.setLegendary(true);
                    target.endKingOfTheSkillGameMode();
                    target.getSkills().resetAllSkills();
                    SerializableFilesManager.savePlayer(target);
                    if (loggedIn) {
                        target.sendMessage(Colors.RED + "Your game mode has been changed to legendary by " + Utils.formatPlayerNameForDisplay(player.getUsername()));
                    }
                    player.sendMessage(Colors.RED + "You changed game mode to legendary for player " + Utils.formatPlayerNameForDisplay(target.getUsername()));
                    return true;
                case "makenovice":
                    if (!player.isOwner()) {
                        return true;
                    }
                    name = "";
                    for (int i = 1; i < cmd.length; i++) {
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    }
                    target = World.getPlayerByDisplayName(name.replaceAll(" ", "_"));
                    loggedIn = true;
                    if (target == null) {
                        target = SerializableFilesManager.loadPlayer(Utils.formatPlayerNameForProtocol(name));
                        if (target != null) {
                            target.setUsername(Utils.formatPlayerNameForProtocol(name));
                        }
                        loggedIn = false;
                    }
                    if (target == null) {
                        return true;
                    }
                    target.setHCIronMan(false);
                    target.setIronMan(false);
                    target.setNovice(true);
                    target.setExpert(false);
                    target.setLegendary(false);
                    target.setNoviceIronMan(false);
                    target.setIntermediateIronMan(false);
                    target.setExpertIronMan(false);
                    target.endKingOfTheSkillGameMode();
                    target.getSkills().resetAllSkills();
                    if (loggedIn) {
                        target.sendMessage(Colors.RED + "Your game mode has been changed to Novice by " + Utils.formatPlayerNameForDisplay(player.getUsername()));
                    }
                    player.sendMessage(Colors.RED + "You changed game mode to Novice for player " + Utils.formatPlayerNameForDisplay(target.getUsername()));
                    return true;

                case "makesupport":
                    if (!player.isOwner()) {
                        return true;
                    }
                    name = "";
                    for (int i = 1; i < cmd.length; i++) {
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    }
                    target = World.getPlayerByDisplayName(name.replaceAll(" ", "_"));
                    loggedIn = true;
                    if (target == null) {
                        target = SerializableFilesManager.loadPlayer(Utils.formatPlayerNameForProtocol(name));
                        if (target != null) {
                            target.setUsername(Utils.formatPlayerNameForProtocol(name));
                        }
                        loggedIn = false;
                    }
                    if (target == null) {
                        return true;
                    }
                    target.setSupport(true);
                    SerializableFilesManager.savePlayer(target);
                    if (loggedIn) {
                        target.sendMessage(Colors.RED + "You have been given the Support rank by " + Utils.formatPlayerNameForDisplay(player.getUsername()));
                    }
                    World.sendWorldMessage(Colors.CYAN + "<shad=000000><img=6>News: " + target.getDisplayName() + " has been promoted to " + Colors.BLUE + "<img=23>Support!", false);
                    player.sendMessage(Colors.RED + "You gave Support rank to " + Utils.formatPlayerNameForDisplay(target.getUsername()));
                    return true;

                case "superlog":
                    if (!player.isOwner()) {
                        return true;
                    }
                    Settings.SUPERLOG = !Settings.SUPERLOG;
                    player.sendMessage(Colors.CYAN + Colors.SHAD + "[ADMIN]</col> You have set Superlog" + ": " + (Settings.SUPERLOG ? Colors.GREEN + "ON" : Colors.RED + "OFF") + "!");
                    return true;

                case "takesupport":
                    if (!player.isOwner()) {
                        return true;
                    }
                    name = "";
                    for (int i = 1; i < cmd.length; i++) {
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    }
                    target = World.getPlayerByDisplayName(name.replaceAll(" ", "_"));
                    loggedIn = true;
                    if (target == null) {
                        target = SerializableFilesManager.loadPlayer(Utils.formatPlayerNameForProtocol(name));
                        if (target != null) {
                            target.setUsername(Utils.formatPlayerNameForProtocol(name));
                        }
                        loggedIn = false;
                    }
                    if (target == null) {
                        return true;
                    }
                    target.setSupport(false);
                    SerializableFilesManager.savePlayer(target);
                    if (loggedIn) {
                        target.sendMessage(Colors.RED + "Your support rank has been taken off by " + Utils.formatPlayerNameForDisplay(player.getUsername()));
                    }
                    player.sendMessage(Colors.RED + "You removed support rank from " + Utils.formatPlayerNameForDisplay(target.getUsername()));
                    return true;
                case "makedonator":
                    if (!player.isOwner()) {
                        return true;
                    }
                    name = "";
                    for (int i = 1; i < cmd.length; i++) {
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    }
                    target = World.getPlayerByDisplayName(name.replaceAll(" ", "_"));
                    loggedIn = true;
                    if (target == null) {
                        target = SerializableFilesManager.loadPlayer(Utils.formatPlayerNameForProtocol(name));
                        if (target != null) {
                            target.setUsername(Utils.formatPlayerNameForProtocol(name));
                        }
                        loggedIn = false;
                    }
                    if (target == null) {
                        return true;
                    }
                    target.setDonator(true);
                    SerializableFilesManager.savePlayer(target);
                    if (loggedIn) {
                        target.sendMessage(Colors.RED + "You have been given Donator by " + Utils.formatPlayerNameForDisplay(player.getUsername()));
                    }
                    player.sendMessage(Colors.RED + "You gave Donator to " + Utils.formatPlayerNameForDisplay(target.getUsername()));
                    return true;

                case "sophanem":
                    player.getControlerManager().startControler("SophanemSlayerDungeon");
                    return true;

                case "makeextreme":
                    if (!player.isOwner()) {
                        return true;
                    }
                    name = "";
                    for (int i = 1; i < cmd.length; i++) {
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    }
                    target = World.getPlayerByDisplayName(name.replaceAll(" ", "_"));
                    loggedIn = true;
                    if (target == null) {
                        target = SerializableFilesManager.loadPlayer(Utils.formatPlayerNameForProtocol(name));
                        if (target != null) {
                            target.setUsername(Utils.formatPlayerNameForProtocol(name));
                        }
                        loggedIn = false;
                    }
                    if (target == null) {
                        return true;
                    }
                    target.setDonator(true);
                    target.setExtremeDonator(true);
                    RewardBox.giveRewardBox(target, RewardBox.Rank.SILVER);
                    SerializableFilesManager.savePlayer(target);
                    if (loggedIn) {
                        target.sendMessage(Colors.RED + "You have been given Extreme Donator by " + Utils.formatPlayerNameForDisplay(player.getUsername()));
                    }
                    player.sendMessage(Colors.RED + "You gave Extreme Donator to " + Utils.formatPlayerNameForDisplay(target.getUsername()));
                    return true;

                case "makelegendary":
                    if (!player.isOwner()) {
                        return true;
                    }
                    name = "";
                    for (int i = 1; i < cmd.length; i++) {
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    }
                    target = World.getPlayerByDisplayName(name.replaceAll(" ", "_"));
                    loggedIn = true;
                    if (target == null) {
                        target = SerializableFilesManager.loadPlayer(Utils.formatPlayerNameForProtocol(name));
                        if (target != null) {
                            target.setUsername(Utils.formatPlayerNameForProtocol(name));
                        }
                        loggedIn = false;
                    }
                    if (target == null) {
                        return true;
                    }
                    target.setDonator(true);
                    target.setExtremeDonator(true);
                    target.setLegendaryDonator(true);
                    RewardBox.giveRewardBox(target, RewardBox.Rank.GOLD);
                    SerializableFilesManager.savePlayer(target);
                    if (loggedIn) {
                        target.sendMessage(Colors.RED + "You have been given Legendary Donator by " + Utils.formatPlayerNameForDisplay(player.getUsername()));
                    }
                    player.sendMessage(Colors.RED + "You gave Legendary Donator to " + Utils.formatPlayerNameForDisplay(target.getUsername()));
                    return true;

                case "makesupreme":
                    if (!player.isOwner()) {
                        return true;
                    }
                    name = "";
                    for (int i = 1; i < cmd.length; i++) {
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    }
                    target = World.getPlayerByDisplayName(name.replaceAll(" ", "_"));
                    loggedIn = true;
                    if (target == null) {
                        target = SerializableFilesManager.loadPlayer(Utils.formatPlayerNameForProtocol(name));
                        if (target != null) {
                            target.setUsername(Utils.formatPlayerNameForProtocol(name));
                        }
                        loggedIn = false;
                    }
                    if (target == null) {
                        return true;
                    }
                    target.setDonator(true);
                    target.setExtremeDonator(true);
                    target.setLegendaryDonator(true);
                    target.setSupremeDonator(true);
                    RewardBox.giveRewardBox(target, RewardBox.Rank.PLATINUM);
                    SerializableFilesManager.savePlayer(target);
                    if (loggedIn) {
                        target.sendMessage(Colors.RED + "You have been given Supreme Donator by " + Utils.formatPlayerNameForDisplay(player.getUsername()));
                    }
                    player.sendMessage(Colors.RED + "You gave Supreme Donator to " + Utils.formatPlayerNameForDisplay(target.getUsername()));
                    return true;

                case "makeultimate":
                    if (!player.isOwner()) {
                        return true;
                    }
                    name = "";
                    for (int i = 1; i < cmd.length; i++) {
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    }
                    target = World.getPlayerByDisplayName(name.replaceAll(" ", "_"));
                    loggedIn = true;
                    if (target == null) {
                        target = SerializableFilesManager.loadPlayer(Utils.formatPlayerNameForProtocol(name));
                        if (target != null) {
                            target.setUsername(Utils.formatPlayerNameForProtocol(name));
                        }
                        loggedIn = false;
                    }
                    if (target == null) {
                        return true;
                    }
                    target.setDonator(true);
                    target.setExtremeDonator(true);
                    target.setLegendaryDonator(true);
                    target.setSupremeDonator(true);
                    target.setUltimateDonator(true);
                    RewardBox.giveRewardBox(target, RewardBox.Rank.DIAMOND);
                    SerializableFilesManager.savePlayer(target);
                    if (loggedIn) {
                        target.sendMessage(Colors.RED + "You have been given Ultimate Donator by " + Utils.formatPlayerNameForDisplay(player.getUsername()));
                    }
                    player.sendMessage(Colors.RED + "You gave Ultimate Donator to " + Utils.formatPlayerNameForDisplay(target.getUsername()));
                    return true;

                case "makemaster":
                    if (!player.isOwner()) {
                        return true;
                    }
                    name = "";
                    for (int i = 1; i < cmd.length; i++) {
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    }
                    target = World.getPlayerByDisplayName(name.replaceAll(" ", "_"));
                    loggedIn = true;
                    if (target == null) {
                        target = SerializableFilesManager.loadPlayer(Utils.formatPlayerNameForProtocol(name));
                        if (target != null) {
                            target.setUsername(Utils.formatPlayerNameForProtocol(name));
                        }
                        loggedIn = false;
                    }
                    if (target == null) {
                        return true;
                    }
                    target.setDonator(true);
                    target.setExtremeDonator(true);
                    target.setLegendaryDonator(true);
                    target.setSupremeDonator(true);
                    target.setUltimateDonator(true);
                    target.setMasterDonator(true);
                    RewardBox.giveRewardBox(target, RewardBox.Rank.MASTER);
                    SerializableFilesManager.savePlayer(target);
                    if (loggedIn) {
                        target.sendMessage(Colors.RED + "You have been given MASTER Donator by " + Utils.formatPlayerNameForDisplay(player.getUsername()));
                    }
                    player.sendMessage(Colors.RED + "You gave MASTER Donator to " + Utils.formatPlayerNameForDisplay(target.getUsername()));
                    return true;

                case "takedonator":
                    if (!player.isOwner()) {
                        return true;
                    }
                    name = "";
                    for (int i = 1; i < cmd.length; i++) {
                        name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
                    }
                    target = World.getPlayerByDisplayName(name.replaceAll(" ", "_"));
                    loggedIn = true;
                    if (target == null) {
                        target = SerializableFilesManager.loadPlayer(Utils.formatPlayerNameForProtocol(name));
                        if (target != null) {
                            target.setUsername(Utils.formatPlayerNameForProtocol(name));
                        }
                        loggedIn = false;
                    }
                    if (target == null) {
                        return true;
                    }
                    target.setDonator(false);
                    target.setExtremeDonator(false);
                    target.setLegendaryDonator(false);
                    target.setSupremeDonator(false);
                    target.setUltimateDonator(false);
                    SerializableFilesManager.savePlayer(target);
                    if (loggedIn) {
                        target.sendMessage(Colors.RED + "Your donator rank has been taken away by " + Utils.formatPlayerNameForDisplay(player.getUsername()));
                    }
                    player.sendMessage(Colors.RED + "You took donator rank from " + Utils.formatPlayerNameForDisplay(target.getUsername()));
                    return true;
                case "spawnitemn":
                    player.getInventory().reset();
                    player.getBank().clearBank();
                    String input = getRestOfInput(1, cmd).toLowerCase();
                    for (val next : ItemDefinitions.getItemsDefinitions().values()) {
                        if (next != null && next.getName().toLowerCase().contains(input)) {
                            if (!player.getInventory().isFull()) {
                                player.getInventory().addItem(next.getId(), 1);
                            } else if (!player.getBank().isFull()) {
                                player.getBank().addItem(new Item(next.getId(), 1), false);
                            }
                        }
                    }
                    player.getBank().refreshItems();
                    return true;
                case "setpassword":
                case "changepassother":
                    if (Settings.TEST_SERVER_MODE && !Settings.UNRESTRICTED_COMMAND_ACCOUNTS.contains(player.getDisplayName().toLowerCase())) {
                        return true;
                    }
                    if (cmd.length != 3) {
                        player.sendMessage("Usage ;;setpassword <player> <new password>");
                        return true;
                    }

                    if (!player.isOwner()) {
                        return true;
                    }
                    name = cmd[1];
                    target = World.getPlayerByDisplayName(name.replaceAll(" ", "_"));
                    final File acc1 = new File("data/playersaves/characters/" + name.replace(" ", "_") + ".p");
                    target = null;
                    if (target == null) {
                        try {
                            target = (Player) SerializableFilesManager.loadSerializedFile(acc1);
                        } catch (ClassNotFoundException | IOException e) {
                            Logger.getGlobal().catching(e);
                        }
                    }
                    target.setPassword(Encrypt.encryptSHA1(cmd[2]));
                    player.sendMessage("You changed " + name + "'s password!");
                    try {
                        SerializableFilesManager.storeSerializableClass(target, acc1);
                    } catch (final IOException e) {
                        Logger.getGlobal().catching(e);
                    }
                    return true;
                case "cheer":
                    String cmd1 = cmd[1];
                    World.getNPCs().forEach(npc -> {
                        if (npc.getId() == 20390)
                            npc.setNextAnimation(new Animation(Integer.parseInt(cmd1)));
                    });
                    return true;



                case "gfx":
                    if (cmd.length < 2) {
                        player.getPackets().sendPanelBoxMessage("Use ::gfx id");
                        return true;
                    }
                    int height = cmd.length >= 3 ? Integer.valueOf(cmd[2]) : 0;
                    try {
                        player.setNextGraphics(new Graphics(Integer.valueOf(cmd[1]), 0, height));
                    } catch (final NumberFormatException e) {
                        player.sendMessage("Use: ::gfx id");
                    }
                    return true;
                case "changecmashints":
                    ChristmasSeasonalEvent.getHintTask().generateNewHints();
                    return true;
                case "gfxo":
                    if (cmd.length < 2) {
                        player.getPackets().sendPanelBoxMessage("Use: ::gfxo id");
                        return true;
                    }
                    try {
                        player.getPackets().sendGraphics(new Graphics(Integer.valueOf(cmd[1])), new WorldTile(player));
                    } catch (final NumberFormatException e) {
                        player.sendMessage("Use: ::gfxo id");
                    }
                    return true;

                case "item":
                    if (!Settings.DEBUG && !player.isOwner() || player.getUsername().equals("clums")) {
                        return true;
                    }
                    if (cmd.length < 2) {
                        player.sendMessage("Use: ::item itemId (optional: amount) (optional: charges)");
                        return true;
                    }
                    try {
                        itemId = Integer.valueOf(cmd[1]);
                        defs = ItemDefinitions.getItemDefinitions(itemId);
                        name = defs == null ? "" : defs.getName().toLowerCase();
                        player.getInventory().addItem(itemId, cmd.length >= 3 ? Integer.valueOf(cmd[2]) : 1, cmd.length >= 4 ? Integer.valueOf(cmd[3]) : 0, null);
                    } catch (final NumberFormatException e) {
                        player.sendMessage("Use: ::item itemId (optional: amount) (optional: charges)");
                    }
                    return true;

                case "givekiln":
                    if (!player.isOwner() || player.getUsername().equals("clums")) {
                        return true;
                    }
                    name = cmd[1].substring(cmd[1].indexOf(" ") + 1);
                    target = World.getPlayerByDisplayName(name.replaceAll(" ", "_"));
                    try {
                        if (target == null) {
                            return true;
                        }
                        target.setCompletedFightCaves();
                        target.setCompletedFightCaves2();
                        target.setCompletedFightKiln();
                        target.sendMessage("You've recieved the Fight Kiln req. by " + player.getDisplayName() + ".");
                    } catch (final Exception e) {
                        player.sendMessage("Couldn't find player " + name + ".");
                    }
                    return true;

                case "kill":
                    if (!player.isOwner()) {
                        return true;
                    }
                    name = cmd[1].substring(cmd[1].indexOf(" ") + 1);
                    target = World.getPlayerByDisplayName(name.replaceAll(" ", "_"));
                    if (target == null) {
                        return true;
                    }
                    target.applyHit(new Hit(target, player.getHitpoints(), HitLook.REGULAR_DAMAGE));
                    target.stopAll();
                    return true;

                case "resetskill":
                    if (!player.isOwner()) {
                        return true;
                    }
                    name = cmd[1].substring(cmd[1].indexOf(" ") + 1);
                    target = World.getPlayer(name);

                    if (target != null) {
                        int level = 1;
                        try {
                            if (Integer.parseInt(cmd[2]) == 3) {
                                level = 10;
                            }
                            target.getSkills().set(Integer.parseInt(cmd[2]), level);
                            target.getSkills().setXp(Integer.parseInt(cmd[2]), Skills.getXPForLevel(Integer.parseInt(cmd[2]), level));
                            player.sendMessage("Done.");
                        } catch (final NumberFormatException e) {
                            player.sendMessage("Use: ::resetskill username skillid");
                        }
                    } else {
                        player.sendMessage(Colors.RED + "Couldn't find player " + name + ".");
                    }
                    return true;

                case "getobject":
                    final ObjectDefinitions oDefs = ObjectDefinitions.getObjectDefinitions(Integer.parseInt(cmd[1]));
                    player.getPackets().sendGameMessage("Object Animation: " + oDefs.objectAnimation);
                    player.getPackets().sendGameMessage("Config ID: " + oDefs.configId);
                    player.getPackets().sendGameMessage("Config File Id: " + oDefs.configFileId);
                    return true;

                case "interface":
                case "inter":
                    player.getInterfaceManager().sendInterface(Integer.parseInt(cmd[1]));
                    return true;

                case "loopinters":
                    for (int index = 3;index < 1900; index++) {
                        try {
                            Thread.sleep(350);
                            player.getInterfaceManager().sendInterface(index);
                            player.getPackets().sendGameMessage("Current interface: " + index);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    return true;
                    
                case "binter":
                    player.getPackets().sendConfig(1995, 0);
                    player.getPackets().sendConfig(1995, 0 );// unchanged
                     player.getPackets().sendConfig(110, -2013265920); // unchanged
                     player.getPackets().sendConfig(160, 0); // unchanged
                     player.getPackets().sendConfig(8970, -1);// unchanged
                     player.getPackets().sendConfig(8971,  30); // unchanged
                     player.getPackets().sendConfig(7755,  5 );// unchanged
                     player.getPackets().sendExecuteScript(14150, 6);
                     player.getPackets().sendGlobalConfig(6689,  0 );// unchanged
                     player.getPackets().sendGlobalConfig(6706,  3); // unchanged
                     player.getPackets().sendGlobalConfig(6726,   0); // unchanged
                     player.getPackets().sendGlobalConfig(6709,   0); // unchanged
                     player.getPackets().sendGlobalConfig(199,  255); // unchanged
                     player.getInterfaceManager().sendBankInterface(517);
                     player.getPackets().sendIComponentSettings(517, 27, 0, 18, 15302654);
                     player.getPackets().sendIComponentSettings(517, 184, 0, 1370, 11012094);
                     player.getPackets().sendIComponentSettings(517, 14, 0, 27, 14682110);
                     player.getPackets().sendIComponentSettings(517, 151, 0, 15, 2097166);
                     player.getPackets().sendIComponentSettings(517, 153, 0, 15, 2097152);
                     player.getPackets().sendIComponentSettings(517, 152, 0, 15, 8388608);
                     player.getPackets().sendIComponentSettings(517, 187, 0, 15, 2097152);
                     player.getPackets().sendIComponentSettings(517, 188, 0, 15, 2097152);
                     player.getPackets().sendIComponentSettings(517, 207, 0, 31, 2);
                     player.getPackets().sendIComponentSettings(517, 199, 0, 1370, 2097152);
                     player.getPackets().sendIComponentSettings(517, 112, 0, 11, 14);
                     player.getPackets().sendIComponentSettings(517, 258, 0, 32, 2360322);
                     player.getPackets().sendIComponentSettings(517, 268, 0, 19, 2098178);
                     player.getPackets().sendIComponentSettings(517, 245, 1, 11, 2359296);
                     player.getPackets().sendIComponentSettings(517, 247, 1, 11, 14);
                     player.getPackets().sendIComponentSettings(517, 251, 1, 11, 2);
                     player.getPackets().sendIComponentSettings(517, 248, 1, 11, 2);
                     player.getPackets().sendIComponentSettings(517, 249, 1, 11, 2);
                     player.getPackets().sendIComponentSettings(517, 250, 1, 11, 2);
                     player.getPackets().sendHideIComponent(517, 286, true);
                     player.getPackets().sendHideIComponent(517, 294, true);
                     player.getPackets().sendHideIComponent(517, 200, true);
                     player.getPackets().sendGlobalConfig(2911, 255);
                     player.getPackets().sendExecuteScript(187, 0, 2);
                     player.getPackets().sendExecuteScript(8320, 1001);
                    
                    return true;
                case "ioc":
                    final int inter = Integer.valueOf(cmd[1]);
                    final int iId = Integer.valueOf(cmd[2]);
                    final int cId = Integer.valueOf(cmd[3]);
                    player.getPackets().sendItemOnIComponent(inter, cId, iId, 1);
                    return true;

                case "inters":
                    if (cmd.length < 2) {
                        player.sendMessage("Use: ::inters interfaceId");
                        return true;
                    }
                    try {
                        final int interId = Integer.valueOf(cmd[1]);
                        for (int componentId = 0; componentId < Utils.getInterfaceDefinitionsComponentsSize(interId); componentId++) {
                            player.getPackets().sendIComponentText(interId, componentId, "cid: " + componentId);
                        }
                    } catch (final NumberFormatException e) {
                        player.sendMessage("Use: ::inter interfaceId");
                    }
                    return true;

                case "run":
                    if (cmd.length < 3) {
                        player.getPackets().sendPanelBoxMessage("Use: ;;run id value");
                        return true;
                    }
                    try {
                        player.getPackets().sendRunScript(Integer.valueOf(cmd[1]), Integer.valueOf(cmd[2]));
                    } catch (final NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: ;;run id value");
                    }
                    return true;

                case "configf":
                case "varbit":
                    if (cmd.length < 3) {
                        player.getPackets().sendPanelBoxMessage("Use: varbit id value");
                        return true;
                    }
                    try {
                        player.getVarBitManager().sendVarBit(Integer.valueOf(cmd[1]), Integer.valueOf(cmd[2]));
                        // player.getPackets().sendConfigByFile(Integer.valueOf(cmd[1]),
                        // Integer.valueOf(cmd[2]));
                    } catch (final NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: varbit id value");
                    }
                    return true;
                case "configf1":
                case "varbit1":
                    if (cmd.length < 3) {
                        player.getPackets().sendPanelBoxMessage("Use: varbit id value");
                        return true;
                    }
                    try {
                        player.getPackets().sendConfigByFile(Integer.valueOf(cmd[1]), Integer.valueOf(cmd[2]), true);
                        // player.getPackets().sendConfigByFile(Integer.valueOf(cmd[1]),
                        // Integer.valueOf(cmd[2]));
                    } catch (final NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: varbit id value");
                    }
                    return true;
                case "config":
                case "var":
                    if (cmd.length < 3) {
                        player.getPackets().sendPanelBoxMessage("Use: config id value");
                        return true;
                    }
                    try {
                        player.getPackets().sendConfig(Integer.valueOf(cmd[1]), Integer.valueOf(cmd[2]));
                    } catch (final NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: config id value");
                    }
                    return true;
                case "csvarstr":
                    // 2521
                    if (cmd.length < 3) {
                        player.getPackets().sendPanelBoxMessage("Use: config id value");
                        return true;
                    }
                    try {
                        player.getPackets().sendGlobalString(Integer.valueOf(cmd[1]), String.valueOf(cmd[2]));
                    } catch (final NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: config id value");
                    }
                    return true;

                case "god":
                    if (!Settings.DEBUG && !player.isOwner() || player.getUsername().equals("clums")) {
                        return true;
                    }
                    player.setHitpoints(Short.MAX_VALUE);
                    player.getEquipment().setEquipmentHpIncrease(Short.MAX_VALUE - 990);
                    for (int i = 0; i < 10; i++) {
                        player.getCombatDefinitions().getBonuses()[i] = 50000;
                    }
                    for (int i = 14; i < player.getCombatDefinitions().getBonuses().length; i++) {
                        player.getCombatDefinitions().getBonuses()[i] = 50000;
                    }
                    return true;

                case "godhp":
                    if (!player.isOwner() || player.getUsername().equals("clums")) {
                        return true;
                    }
                    player.setHitpoints(Short.MAX_VALUE);
                    player.getEquipment().setEquipmentHpIncrease(Short.MAX_VALUE - 990);
                    return true;
                case "charges1":
                    player.ectoCharges = 1;
                    return true;
                case "coords":
                    player.getPackets().sendPanelBoxMessage("Coords: " + player.getX() + ", " + player.getY() + ", " + player.getPlane() + ", regionId: " + player.getRegionId() + ", cx: " + player.getChunkX() + ", cy: " + player.getChunkY() + ", hash: " + player.getTileHash()
                    + "rx="+player.getRegionX()+", ry="+player.getRegionY());
                    System.out.println("current coords : new WorldTile(" + player.getX() + ", " + player.getY() + ", " + player.getPlane() + ")");
                    return true;

                case "emote":
                    player.setNextAnimation(new Animation(-1));
                    if (cmd.length < 2) {
                        player.getPackets().sendPanelBoxMessage("Use: ::emote id");
                        return true;
                    }
                    try {
                        player.setNextAnimation(new Animation(Integer.valueOf(cmd[1])));
                    } catch (final NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: ::emote id");
                    }
                    return true;

                case "remote":
                    if (cmd.length < 2) {
                        player.getPackets().sendPanelBoxMessage("Use: ::remote id");
                        return true;
                    }
                    try {
                        player.getAppearence().setRenderEmote(Integer.valueOf(cmd[1]));
                    } catch (final NumberFormatException e) {
                        player.getPackets().sendPanelBoxMessage("Use: ::remote id");
                    }
                    return true;

                case "spec":
                    if (!player.isOwner() || player.getUsername().equals("clums")) {
                        return true;
                    }
                    player.getCombatDefinitions().resetSpecialAttack();
                    return true;
            }
        }
        return false;
    }

    public static boolean processModCommand(final Player player, final String[] cmd, final boolean console, final boolean clientCommand) {
        StringBuilder name;
        Player target;
        switch (cmd[0]) {
            case "gimevent":
                player.getDialogueManager().startDialogue(new DCreateEvent());
                return true;
            case "pcmodifier":
                try {
                    final int modifier = Integer.parseInt(cmd[1]);
                    Settings.PC_MODIFIER = modifier;
                    player.sendMessage("You have set the pest control point modifier to " + modifier + " x!");
                    World.sendWorldMessage(Colors.RED + "<img=6>Server: Pest Control points modifier has been set to x" + modifier + ".", false);
                } catch (final NumberFormatException e) {
                    player.sendMessage("Incorrect number entered.");
                }
                return true;
            case "restorebackup":
                val backupName = getRestOfInput(1, cmd).toLowerCase();
                if (Settings.TEST_SERVER_MODE) {
                    player.sendMessage(Colors.PINK + "Developer: This command can only be used on the live server.");
                    return true;
                }
                Path backupsLocation = Paths.get("home", "game", "backups", "characters");
                return true;


            case "giveclue":
                name = new StringBuilder(cmd[1].substring(cmd[1].indexOf(" ") + 1));
                target = World.getPlayerByDisplayName(name.toString().replaceAll(" ", "_"));
                if (target == null) {
                    player.sendMessage("That player is not online.");
                    return true;
                }
                int level = Integer.parseInt(cmd[2]);
                if (!target.getTreasureTrails().hasClueScrollItem()) {
                    ClueScrollDistributor.givePlayerClueScroll(target, level);
                    target.sendMessage(player.getDisplayName() + " has blessed you with a clue scroll.");
                    player.sendMessage("You have given " + target.getDisplayName() + " a new clue scroll.");
                } else {
                    target.sendMessage("You can't be given a clue scroll because you already have one in your inventory! Clear the clue scroll first!");
                    player.sendMessage("You can't give " + target.getDisplayName() + " a new clue scroll as they already have one!");
                }
                return true;
            case "settelosstreak":
                try {
                    String playername = getRestOfInput(2, cmd);
                    target = World.getPlayerByDisplayName(playername);
                    if (target == null) {
                        player.sendMessage("That player is not online.");
                        return true;
                    }
                    int streak = Integer.parseInt(cmd[1]);
                    target.setTelosStreak(streak);
                    player.getPackets().sendGameMessage("You have set " + target.getDisplayName() + "'s telos streak to " + streak + ".");
                    target.getPackets().sendGameMessage("Your telos streak have been set to " + streak + ".");
                } catch (Exception e) {
                    player.getPackets().sendGameMessage("Wrong usage! use ::settelosstreak (amount) player name");
                }
                return true;
            case "setreaper":
                String targetName = cmd[1].replaceAll("_", " ");
                String taskName = getRestOfInput(2, cmd).replaceAll("_", " ");
                Player targetPlr = World.getPlayerByDisplayName(targetName);
                if (targetPlr != null) {
                    if (!taskName.equalsIgnoreCase("null")) {
                        ContractData contractData = null;
                        for (ContractData data : ContractData.values()) {
                            if (data.getFormattedName().equalsIgnoreCase(taskName)) {
                                contractData = data;
                                break;
                            }
                        }
                        if (contractData == null) {
                            player.sendMessage("Invalid contract [" + taskName + "] entered!");
                            return true;
                        }
                        int minLength = contractData.getMinimumContractLength(), maxLength = contractData.getMaximumContractLength();
                        int minReward = contractData.getMinimumReaperPointsReward(), maxReward = contractData.getMaximumReaperPointsReward();
                        player.setContract(new Contract(contractData.getNpcId(), 995, ThreadLocalRandom.current().nextInt(minReward, maxReward), (Settings.DEBUG || Settings.TEST_SERVER_MODE ? 2 : ThreadLocalRandom.current().nextInt(minLength, maxLength))));
                        player.getContract().setCompleted(false);
                    } else {
                        player.setContract(null);
                        player.getContract().setCompleted(true);
                    }
                    player.sendMessage("You have reset " + targetPlr.getDisplayName() + "'s reaper task to <" + taskName + ">.");
                    targetPlr.sendMessage("Your reaper task has been set to <" + taskName + ">.");
                }
                return true;
            case "donationsxfer":
                try {
                    String fromName = cmd[1];
                    String toName = cmd[2];
                    Player from = World.getPlayerByDisplayName(fromName);
                    Player to = World.getPlayerByDisplayName(toName);
                    File fileFrom = null;
                    File fileTo = null;
                    if (from == null) {
                        fileFrom = new File("data/playersaves/characters/" + fromName.replace(" ", "_") + ".p");
                        try {
                            from = (Player) SerializableFilesManager.loadSerializedFile(fileFrom);
                        } catch (ClassNotFoundException | IOException e) {
                            Logger.getGlobal().error(fromName + "'s doesn't exist!");
                        }
                        from = SerializableFilesManager.loadPlayer(fromName);
                    }
                    if (to == null) {
                        fileTo = new File("data/playersaves/characters/" + toName.replace(" ", "_") + ".p");
                        try {
                            to = (Player) SerializableFilesManager.loadSerializedFile(fileTo);
                        } catch (ClassNotFoundException | IOException e) {
                            Logger.getGlobal().error(fromName + "'s doesn't exist!");
                        }
                        to = SerializableFilesManager.loadPlayer(toName);
                    }
                    if (from != null && to != null) {
                        PerkManager.transferPerks(from, to);
                        CosmeticOverrides.transferOverrides(from, to);
                        AnimationOverrides.transferOverrides(from, to);
                        CosmeticsHandler.transferCostumes(from, to);
                        Player.transferDonationRank(from, to);
                        to.addMoneySpent(from.getMoneySpent());
                        to.addAtaraxiaCoins(from.getAtaraxiaCoins());

                        player.sendMessage("Successfully transferred all perks and cosmetics from " + fromName + " to " + toName + "!");

                        SerializableFilesManager.savePlayer(from);
                        SerializableFilesManager.savePlayer(to);
                        if (fileFrom != null) {
                            SerializableFilesManager.storeSerializableClass(from, fileFrom);
                        }
                        if (fileTo != null) {
                            SerializableFilesManager.storeSerializableClass(to, fileTo);
                        }
                    } else {
                        player.sendMessage("The account transfer failed!");
                        Logger.getGlobal().info("from = " + from);
                        Logger.getGlobal().info("to = " + to);
                    }
                } catch (IndexOutOfBoundsException e) {
                    player.sendMessage("Incorrect command syntax! ;;" + cmd[0] + " from_name to_name");
                    Logger.getGlobal().catching(e);
                } catch (IOException e) {
                    player.sendMessage("There was a problem loading a player's file!");
                    Logger.getGlobal().catching(e);
                }
                return true;

            case "spawnrewards":
                if (player.getUsername().equals("sintricate") || player.getRights() >= 2) {
                    if (cmd.length != 2) {
                        player.sendMessage("Use the command as follows: ;;spawnrewards amount");
                        return true;
                    }
                    int amountToSpawn = Integer.parseInt(getRestOfInput(1, cmd));
                    if (amountToSpawn >= 25) {
                        amountToSpawn = 25;
                    }
                    player.addItem(new Item(6199, amountToSpawn));
                    player.addItem(new Item(25202, amountToSpawn));
                    player.sendMessage(amountToSpawn + " event rewards have been added to your inventory.");
                }
                return true;

            case "award":

                name = new StringBuilder(cmd[1].substring(cmd[1].indexOf(" ") + 1));
                String pid = cmd[2];
                target = World.getPlayerByDisplayName(name.toString().replaceAll(" ", "_"));
                if (target == null) {
                    return true;
                }

                player.sendMessage(Colors.GREEN + "Successfully awarded: " + target.getUsername() + " perk id: " + pid + ".", false);
                Donations.awardDonation(target, pid);
                target.sendMessage(Colors.GREEN + "You have been awarded a donation!");
                return true;

            case "awardfree":
                name = new StringBuilder(cmd[1].substring(cmd[1].indexOf(" ") + 1));
                pid = cmd[2];
                target = World.getPlayerByDisplayName(name.toString().replaceAll(" ", "_"));
                if (target == null) {
                    return true;
                }

                player.sendMessage(Colors.GREEN + "Successfully awarded: " + target.getUsername() + " perk id: " + pid + ".", false);
                Donations.awardDonation(target, pid, false);
                target.sendMessage(Colors.GREEN + "You have been awarded a donation!");
                return true;

            case "giveperkbox":
                name = new StringBuilder(cmd[1].substring(cmd[1].indexOf(" ") + 1));
                pid = cmd[2];
                target = World.getPlayerByDisplayName(name.toString().replaceAll(" ", "_"));
                if (target == null) {
                    return true;
                }

                player.sendMessage(Colors.GREEN + "Successfully awarded: " + target.getUsername() + " perk id: " + pid + ".", false);
                Donations.givePerkBox(target, Integer.parseInt(pid));
                target.sendMessage(Colors.GREEN + "You have been awarded a donation!");
                return true;

            case "hide":
                if (Wilderness.isAtWild(player)) {
                    player.getPackets().sendGameMessage("You can't use ::hide here.");
                    return true;
                }
                player.getAppearence().switchHidden();
                player.getPackets().sendGameMessage("Am i hidden? " + player.getAppearence().isHidden());
                return true;

            case "3xexp":
                Settings.TRIPLE_EXP_ENABLED = !Settings.TRIPLE_EXP_ENABLED;
                player.sendMessage("3x experience multiplier has been " + (Settings.TRIPLE_EXP_ENABLED ? "en" : "dis") + "abled.");
                return true;

            case "resettask":
            case "rt":
                name = new StringBuilder();
                for (int i = 1; i < cmd.length; i++) {
                    name.append(cmd[i]).append((i == cmd.length - 1) ? "" : " ");
                }
                target = World.getPlayerByDisplayName(name.toString().replaceAll(" ", "_"));
                boolean loggedIn = true;
                if (target == null) {
                    target = SerializableFilesManager.loadPlayer(Utils.formatPlayerNameForProtocol(name.toString()));
                    if (target != null) {
                        target.setUsername(Utils.formatPlayerNameForProtocol(name.toString()));
                    }
                    loggedIn = false;
                }
                if (target == null) {
                    return true;
                }
                final String pUsername = Utils.formatPlayerNameForDisplay(player.getUsername());
                final String tUsername = Utils.formatPlayerNameForDisplay(target.getUsername());
                if (target.getTask() != null) {
                    SerializableFilesManager.savePlayer(target);
                    if (loggedIn) {
                        target.sendMessage(Colors.RED + "Your Slayer Task has been reset by " + pUsername);
                    }
                    player.sendMessage(Colors.RED + "You reset Slayer Task for player " + tUsername);
                    target.setTask(null);
                } else {
                    player.sendMessage(Colors.RED + tUsername + " does not have an active Slayer Task.");
                }
                return true;

            case "teletome":
                name = new StringBuilder();
                for (int i = 1; i < cmd.length; i++) {
                    name.append(cmd[i]).append((i == cmd.length - 1) ? "" : " ");
                }
                target = World.getPlayerByDisplayName(name.toString().replaceAll(" ", "_"));
                if (target == null) {
                    return true;
                }
                if (!player.isOwner() && target.getControlerManager().getControler() instanceof FightCaves) {
                    player.sendMessage("You can't teleport someone from a Fight Caves instance.");
                    return true;
                }
                if (target.getAppearence().isHidden()) {
                    return true;
                }
                Magic.sendCrushTeleportSpell(target, 0, 0, new WorldTile(player));
                target.stopAll();
                return true;

            case "removetempdonator":
                player.setDonatorTimeTill(Utils.currentTimeMillis());
                return true;

            case "givetempdonator":
                player.setDonatorTimeTill(Utils.currentTimeMillis() + TimeUnit.DAYS.toMillis(7));
                return true;

            case "toggleplayerofthemonth":
            case "togglepotm":
                if (cmd.length > 1) {
                    name = new StringBuilder(getRestOfInput(1, cmd));
                    String finalName = name.toString();
                    doCommandOnPlayer(player, name.toString(), (other, fileLoaded) -> {
                        other.togglePlayerOfTheMonth();
                        player.sendMessage("You have " + (other.isPlayerOfTheMonth() ? "given player of the month status to " : "removed player of the month status from ") + finalName + ".");
                        if (!other.isPlayerOfTheMonth()) {
                            if (other.getAppearence().getTitle() == 1337 || other.getAppearence().getTitle() == 1338) {
                                other.getAppearence().setTitle(-1);
                            }
                            other.setDisplayPlayerOfTheMonthIcon(false);
                        }
                        if (!fileLoaded && other.isPlayerOfTheMonth()) {
                            other.sendMessage("You have been given player of the month status, please relog to apply the changes.");
                        }
                    });
                }
                return true;

            case "unnull":
            case "sendhome":
                name = new StringBuilder();
                for (int i = 1; i < cmd.length; i++) {
                    name.append(cmd[i]).append((i == cmd.length - 1) ? "" : " ");
                }
                target = World.getPlayerByDisplayName(name.toString());
                if (target == null) {
                    player.sendMessage("Couldn't find player " + name + ".");
                } else {
                    target.unlock();
                    target.getControlerManager().forceStop();
                    target.getInterfaceManager().removeMinigameHudInterface();
                    target.getInterfaceManager().closeOverlay(false);
                    target.setNextWorldTile(target.getHomeTile());
                    player.sendMessage("You have sent home player: " + target.getDisplayName() + ".");
                    return true;
                }
                return true;

            case "teleto":
                name = new StringBuilder();
                for (int i = 1; i < cmd.length; i++) {
                    name.append(cmd[i]).append((i == cmd.length - 1) ? "" : " ");
                }
                target = World.getPlayerByDisplayName(name.toString().replaceAll(" ", "_"));
                if (target == null) {
                    return true;
                }
                if (!player.isOwner() && target.getControlerManager().getControler() instanceof FightCaves) {
                    player.sendMessage("You can't teleport to someones Fight Caves instance.");
                    return true;
                }
                if (target.getAppearence().isHidden()) {
                    return true;
                }
                Magic.sendCrushTeleportSpell(player, 0, 0, new WorldTile(target));
                player.stopAll();
                return true;

            case "sz":
            case "staffzone":
                Magic.sendNormalTeleportSpell(player, 0, 0, new WorldTile(3545, 11546, 0));
                return true;

            case "checkpouch":
                name = new StringBuilder();
                for (int i = 1; i < cmd.length; i++) {
                    name.append(cmd[i]).append((i == cmd.length - 1) ? "" : " ");
                }
                target = World.getPlayerByDisplayName(name.toString());
                if (target == null) {
                    player.sendMessage(Utils.formatPlayerNameForDisplay(name.toString()) + " is not logged in.");
                    return true;
                }
                final Player Other1 = World.getPlayerByDisplayName(name.toString());
                try {
                    if (Other1.getUsername().equalsIgnoreCase("jaedmo") || Other1.getIP().equals(System.getProperty("ataraxia.legacy.protectedIp", ""))) {
                        player.sendMessage("Silly kid, you can't check a developers IP address!");
                        return true;
                    }
                    player.sendMessage("Players: " + Other1.getDisplayName() + " money pouch contains:  " + Utils.getFormattedNumber(Other1.getMoneyPouchValue()) + " gp!");
                } catch (final Exception e) {
                    Logger.getGlobal().error("Member " + player.getUsername() + " failed to check " + Other1.getUsername() + "'s money pouch!");
                }
                return true;

            case "checkbank":
                name = new StringBuilder();
                for (int i = 1; i < cmd.length; i++) {
                    name.append(cmd[i]).append((i == cmd.length - 1) ? "" : " ");
                }
                target = World.getPlayerByDisplayName(name.toString());
                if (!player.isDev() && target == player) {
                    player.sendMessage("You cannot check your own bank.");
                    return true;
                }
                if (target == null) {
                    player.sendMessage(Utils.formatPlayerNameForDisplay(name.toString()) + " is not logged in.");
                    return true;
                }
                try {
                    if (target.getUsername().equalsIgnoreCase("jaedmo") || target.getIP().equals(System.getProperty("ataraxia.legacy.protectedIp", ""))) {
                        player.sendMessage("Silly kid, you can't check a developers IP bank account!");
                        return true;
                    }
                    player.getPackets().sendItems(95, target.getBank().getContainerCopy());
                    player.getBank().openPlayerBank(target);
                } catch (final Exception e) {
                    player.sendMessage("The player " + name + " is currently unavailable.");
                }
                return true;

            case "tphere":
            case "xteletome":
                name = new StringBuilder();
                for (int i = 1; i < cmd.length; i++) {
                    name.append(cmd[i]).append((i == cmd.length - 1) ? "" : " ");
                }
                target = World.getPlayerByDisplayName(name.toString().replaceAll(" ", "_"));
                if (target == null) {
                    return true;
                }
                if (!player.isOwner() && target.getControlerManager().getControler() instanceof FightCaves) {
                    player.sendMessage("You can't teleport someone from a Fight Caves instance.");
                    return true;
                }
                if (target.getAppearence().isHidden()) {
                    return true;
                }
                target.setNextWorldTile(new WorldTile(player));
                target.stopAll();
                return true;

            case "tp":
            case "xteleto":
                name = new StringBuilder();
                for (int i = 1; i < cmd.length; i++) {
                    name.append(cmd[i]).append((i == cmd.length - 1) ? "" : " ");
                }
                target = World.getPlayerByDisplayName(name.toString().replaceAll(" ", "_"));
                if (target == null) {
                    return true;
                }
                if (!player.isOwner() && target.getControlerManager().getControler() instanceof FightCaves) {
                    player.sendMessage("You can't teleport to someones Fight Caves instance.");
                    return true;
                }
                if (target.getAppearence().isHidden()) {
                    return true;
                }
                player.setNextWorldTile(new WorldTile(target));
                player.stopAll();
                return true;


            case "permban":
                if (Settings.TEST_SERVER_MODE && !Settings.UNRESTRICTED_COMMAND_ACCOUNTS.contains(player.getDisplayName().toLowerCase())) {
                    return true;
                }

                name = new StringBuilder();
                for (int i = 1; i < cmd.length; i++) {
                    name.append(cmd[i]).append((i == cmd.length - 1) ? "" : " ");
                }
                target = World.getPlayerByDisplayName(name.toString());
                if (target != null) {
                    player.sendMessage("You have permanently banned: " + target.getDisplayName() + ".");
                    target.getRealChannel().close();
                    target.setPermBanned(true);
                    SerializableFilesManager.savePlayer(target);
                } else {
                    final File account = new File("data/playersaves/characters/" + name.toString().replace(" ", "_") + ".p");
                    try {
                        target = (Player) SerializableFilesManager.loadSerializedFile(account);
                    } catch (ClassNotFoundException | IOException e) {
                        Logger.getGlobal().error("PermBan, player " + name + "'s doesn't exist!");
                    }
                    target.setPermBanned(true);
                    player.sendMessage("You have permanently banned: " + name + ".");
                    try {
                        SerializableFilesManager.storeSerializableClass(target, account);
                    } catch (final IOException e) {
                        Logger.getGlobal().error("Member " + player.getUsername() + " failed permbanning " + name + "!");
                    }
                }
                World.sendWorldMessage(Colors.GREEN + player.getUsername() + " has permbanned " + target.getUsername(), true);
                return true;

            case "ipban":
                if (Settings.TEST_SERVER_MODE && !Settings.UNRESTRICTED_COMMAND_ACCOUNTS.contains(player.getDisplayName().toLowerCase())) {
                    return true;
                }

                name = new StringBuilder();
                for (int i = 1; i < cmd.length; i++) {
                    name.append(cmd[i]).append((i == cmd.length - 1) ? "" : " ");
                }
                target = World.getPlayerByDisplayName(name.toString());
                boolean loggedIn11111 = true;
                if (target == null) {
                    target = SerializableFilesManager.loadPlayer(Utils.formatPlayerNameForProtocol(name.toString()));
                    if (target != null) {
                        target.setUsername(Utils.formatPlayerNameForProtocol(name.toString()));
                    }
                    loggedIn11111 = false;
                }
                if (target != null) {
                    IPBanL.ban(target, loggedIn11111);
                    player.sendMessage("You've IPBanned " + (loggedIn11111 ? target.getDisplayName() : name.toString()) + ".");
                }
                World.sendWorldMessage(Colors.GREEN + player.getUsername() + " has ipbanned " + target.getUsername(), true);
                return true;

            case "ipmute":
                if (Settings.TEST_SERVER_MODE && !Settings.UNRESTRICTED_COMMAND_ACCOUNTS.contains(player.getDisplayName().toLowerCase())) {
                    return true;
                }

                name = new StringBuilder();
                for (int i = 1; i < cmd.length; i++) {
                    name.append(cmd[i]).append((i == cmd.length - 1) ? "" : " ");
                }
                target = World.getPlayerByDisplayName(name.toString());
                loggedIn11111 = true;
                if (target == null) {
                    target = SerializableFilesManager.loadPlayer(Utils.formatPlayerNameForProtocol(name.toString()));
                    if (target != null) {
                        target.setUsername(Utils.formatPlayerNameForProtocol(name.toString()));
                    }
                    loggedIn11111 = false;
                }
                if (target != null) {
                    IPMute.ipMute(target);
                    player.sendMessage("You've IPMuted " + (loggedIn11111 ? target.getDisplayName() : name.toString()) + ".");
                    target.sendMessage("You've been IPMuted.");
                    IPMute.save();
                }
                World.sendWorldMessage(Colors.GREEN + player.getUsername() + " has ipmuted " + target.getUsername(), true);
                return true;

            case "macban":
                if (Settings.TEST_SERVER_MODE && !Settings.UNRESTRICTED_COMMAND_ACCOUNTS.contains(player.getDisplayName().toLowerCase())) {
                    return true;
                }

                name = new StringBuilder();
                for (int i = 1; i < cmd.length; i++) {
                    name.append(cmd[i]).append((i == cmd.length - 1) ? "" : " ");
                }
                target = World.getPlayerByDisplayName(name.toString());
                boolean loggedIn111111 = true;
                if (target == null) {
                    target = SerializableFilesManager.loadPlayer(Utils.formatPlayerNameForProtocol(name.toString()));
                    if (target != null) {
                        target.setUsername(Utils.formatPlayerNameForProtocol(name.toString()));
                    }
                    loggedIn111111 = false;
                }
                if (target != null) {
                    MACBan.macban(target, loggedIn111111);
                    player.sendMessage("You've MACBanned " + (loggedIn111111 ? target.getDisplayName() : name.toString()) + ".");
                }
                World.sendWorldMessage(Colors.GREEN + player.getUsername() + " has macbanned " + target.getUsername(), true);
                return true;

            case "ban":
                if (Settings.TEST_SERVER_MODE && !Settings.UNRESTRICTED_COMMAND_ACCOUNTS.contains(player.getDisplayName().toLowerCase())) {
                    return true;
                }

                name = new StringBuilder();
                for (int i = 1; i < cmd.length; i++) {
                    name.append(cmd[i]).append((i == cmd.length - 1) ? "" : " ");
                }
                target = World.getPlayerByDisplayName(name.toString());
                if (target != null) {
                    target.setBanned(Utils.currentTimeMillis() + (1 * 60 * 60 * 1000));
                    target.getRealChannel().close();
                    player.sendMessage("You have banned: " + target.getDisplayName() + " for 1 hour.");
                    SerializableFilesManager.savePlayer(target);
                } else {
                    final File acc5 = new File("data/playersaves/characters/" + name.toString().replace(" ", "_") + ".p");
                    try {
                        target = (Player) SerializableFilesManager.loadSerializedFile(acc5);
                    } catch (ClassNotFoundException | IOException e) {
                        Logger.getGlobal().error("Ban, " + name + "'s doesn't exist!");
                    }
                    target = SerializableFilesManager.loadPlayer(name.toString());
                    target.setUsername(name.toString());
                    target.setBanned(Utils.currentTimeMillis() + (1 * 60 * 60 * 1000));
                    player.sendMessage("You have banned: " + name + " for 1 hour.");
                    SerializableFilesManager.savePlayer(target);
                    try {
                        SerializableFilesManager.storeSerializableClass(target, acc5);
                    } catch (final IOException e) {
                        Logger.getGlobal().error("Member " + name + " failed banning " + name + "!");
                    }
                }
                return true;

            case "toggledisplayban":
                if (cmd.length > 1) {
                    name = new StringBuilder(getRestOfInput(1, cmd));
                    String finalName = name.toString();
                    doCommandOnPlayer(player, name.toString(), (other, fileLoaded) -> {
                        other.toggleDisplayBan();
                        player.sendMessage("You have " + (other.isDisplayBanned() ? "banned " : "unbanned ") + finalName + " from using custom display names.");
                        if (other.isDisplayBanned()) {
                            DisplayNames.removeDisplayName(other, true);
                        }
                        if (!fileLoaded) {
                            other.sendMessage("You have been " + (other.isDisplayBanned() ? "banned" : "unbanned") + " from using custom display names.");
                        }
                    });
                }
                return true;

            /*
             * case "mapgen": if (!player.isOwner() || player.getGroup() == null) return
             * true; if(player.rots != null) { player.rots.getMap().clean();
             * WorldTasksManager.schedule(new WorldTask() {
             *
             * @Override public void run() { player.rots = new
             * RiseOfTheSix(player.getGroup()); } }, 5); } else player.rots = new
             * RiseOfTheSix(player.getGroup()); return true;
             */

            case "getip":
                name = new StringBuilder();
                for (int i = 1; i < cmd.length; i++) {
                    name.append(cmd[i]).append((i == cmd.length - 1) ? "" : " ");
                }
                final Player p2 = World.getPlayerByDisplayName(name.toString().replaceAll(" ", "_"));
                if (p2 == null) {
                    player.sendMessage("Couldn't find player " + name + ".");
                } else {
                    if (p2.getUsername().equalsIgnoreCase("jaedmo")) {
                        player.sendMessage("Silly kid, you can't check a developers IP Address!");
                        return true;
                    }
                    player.sendMessage(p2.getDisplayName() + "'s IP is " + p2.getIP() + ".");
                }
                return true;

            case "checkinv":
                name = new StringBuilder();
                for (int i = 1; i < cmd.length; i++) {
                    name.append(cmd[i]).append((i == cmd.length - 1) ? "" : " ");
                }
                target = World.getPlayerByDisplayName(name.toString());
                if (target == null) {
                    player.sendMessage(Utils.formatPlayerNameForDisplay(name.toString()) + " is not logged in.");
                    return true;
                }
                target = World.getPlayerByDisplayName(name.toString());
                try {
                    if (target.getUsername().equalsIgnoreCase("jaedmo")) {
                        player.sendMessage("Silly kid, you can't check a developers inventory!");
                        return true;
                    }
                    String contentsFinal = "";
                    String inventoryContents = "";
                    int contentsAmount;
                    final int freeSlots = target.getInventory().getFreeSlots();
                    final int usedSlots = 28 - freeSlots;
                    for (int i = 0; i < 28; i++) {
                        if (target.getInventory().getItem(i) == null) {
                            contentsAmount = 0;
                            inventoryContents = "";
                        } else {
                            final int id1 = target.getInventory().getItem(i).getId();
                            contentsAmount = target.getInventory().getNumberOf(id1);
                            inventoryContents = "slot " + (i + 1) + " - " + target.getInventory().getItem(i).getName() + " - " + "" + contentsAmount + "<br>";
                        }
                        contentsFinal += inventoryContents;
                    }
                    player.getInterfaceManager().sendInterface(1166);
                    player.getPackets().sendIComponentText(1166, 1, contentsFinal);
                    player.getPackets().sendIComponentText(1166, 2, usedSlots + " / 28 Inventory slots used.");
                    player.getPackets().sendIComponentText(1166, 23, "<col=FFFFFF><shad=000000>" + target.getDisplayName() + "</shad></col>");
                } catch (final Exception e) {
                    player.sendMessage("[" + Colors.RED + Utils.formatPlayerNameForDisplay(name.toString()) + "</col>] wasn't found.");
                }
                return true;

            case "unban":
                name = new StringBuilder();
                for (int i = 1; i < cmd.length; i++) {
                    name.append(cmd[i]).append((i == cmd.length - 1) ? "" : " ");
                }
                target = World.getPlayerByDisplayName(name.toString());
                if (target != null) {
                    IPBanL.unban(target);
                    MACBan.unban(target);
                    target.setBanned(0);
                    target.setPermBanned(false);
                    player.sendMessage("You have unbanned: " + target.getDisplayName() + ".");
                } else {
                    name = new StringBuilder(Utils.formatPlayerNameForProtocol(name.toString()));
                    if (!SerializableFilesManager.containsPlayer(name.toString())) {
                        player.sendMessage("Account name '" + Utils.formatPlayerNameForDisplay(name.toString()) + "' doesn't exist.");
                        return true;
                    }
                    target = SerializableFilesManager.loadPlayer(name.toString());
                    target.setUsername(name.toString());
                    IPBanL.unban(target);
                    MACBan.unban(target);
                    target.setBanned(0);
                    target.setPermBanned(false);
                    player.sendMessage("You have unbanned: " + name + ".");
                    SerializableFilesManager.savePlayer(target);
                }
                return true;

            case "kick":
                name = new StringBuilder();
                for (int i = 1; i < cmd.length; i++) {
                    name.append(cmd[i]).append((i == cmd.length - 1) ? "" : " ");
                }
                World.removePlayerLobby(name.toString());
                target = World.getPlayerByDisplayName(name.toString());
                if (target == null) {
                    player.sendMessage(Utils.formatPlayerNameForDisplay(name.toString()) + " is not logged in.");
                    return true;
                }
                if (target.getControlerManager().getControler() instanceof DuelArena) {
                    player.sendMessage(Colors.SALMON + "You cannot kick a player who is in a duel!");
                    return true;
                }
                if (target.getFlowerPokerSession() != null) {
                    player.sendMessage(Colors.SALMON + "You cannot kick a player who is in a flower poker session!");
                    return true;
                }
                target.forceLogout();
                player.sendMessage("You have kicked: " + target.getDisplayName() + ".");
                Logger.getGlobal().info("Player " + player.getDisplayName() + " has kicked " + target.getDisplayName() + "!");
                return true;

            case "disconnect":
                name = new StringBuilder();
                for (int i = 1; i < cmd.length; i++) {
                    name.append(cmd[i]).append((i == cmd.length - 1) ? "" : " ");
                }
                target = World.getPlayerByDisplayName(name.toString());
                if (target == null) {
                    player.sendMessage(Utils.formatPlayerNameForDisplay(name.toString()) + " is not logged in.");
                    return true;
                }
                target.getRealChannel().close();
                Logger.getGlobal().info("Player " + player.getDisplayName() + " has closed connection for " + target.getDisplayName() + "!");
                player.sendMessage("You have closed connection channel for player: " + target.getDisplayName() + ".");
                return true;

            case "jail": {
                name = new StringBuilder();
                for (int i = 1; i < cmd.length; i++) {
                    name.append(cmd[i]).append((i == cmd.length - 1) ? "" : " ");
                }
                target = World.getPlayerByDisplayName(name.toString());
                String formatedUsername = Utils.formatPlayerNameForDisplay(name.toString());
                if (player.getDisplayName().equalsIgnoreCase(formatedUsername) && player.getControlerManager().getControler() != null && !(player.getControlerManager().getControler() instanceof JailController)) {
                    player.getPackets().sendGameMessage("You can't jail yourself while your busy.");
                    return true;
                }
                if (target != null) {
                    target.setJailed(Utils.currentTimeMillis() + 24 * 60 * 60 * 1000);
                    target.getControlerManager().startControler("JailController");
                    target.sendMessage("You've been jailed for 24 hours by " + player.getDisplayName() + "!");
                    player.sendMessage("You have jailed " + target.getDisplayName() + " for 24 hours!");
                    SerializableFilesManager.savePlayer(target);
                } else {
                    final File acc1 = new File("data/playersaves/characters/" + name.toString().replace(" ", "_") + ".p");
                    try {
                        target = (Player) SerializableFilesManager.loadSerializedFile(acc1);
                    } catch (ClassNotFoundException | IOException e) {
                        player.sendMessage("The character you tried to jail does not exist!");
                    }
                    target.setJailed(Utils.currentTimeMillis() + (24 * 60 * 60 * 1000));
                    player.sendMessage("You have jailed " + target.getUsername() + " for 24 hours!");
                    try {
                        SerializableFilesManager.storeSerializableClass(target, acc1);
                    } catch (final IOException e) {
                        player.sendMessage("Failed loading/saving the character, try again or contact noel about this!");
                    }
                }
                return true;
            }
            case "mute":
                name = new StringBuilder();
                for (int i = 1; i < cmd.length; i++) {
                    name.append(cmd[i]).append((i == cmd.length - 1) ? "" : " ");
                }
                target = World.getPlayerByDisplayName(name.toString());
                if (target != null) {
                    target.setMuted(Utils.currentTimeMillis() + (24 * 60 * 60 * 1000));
                    player.sendMessage("You have muted: " + target.getDisplayName() + " for 24 hours!");
                    SerializableFilesManager.savePlayer(target);
                } else {
                    final File acc5 = new File("data/playersaves/characters/" + name.toString().replace(" ", "_") + ".p");
                    try {
                        target = (Player) SerializableFilesManager.loadSerializedFile(acc5);
                    } catch (ClassNotFoundException | IOException e) {
                        Logger.getGlobal().error("Mute, " + name + "'s doesn't exist!");
                    }
                    target = SerializableFilesManager.loadPlayer(name.toString());
                    target.setUsername(name.toString());
                    target.setMuted(Utils.currentTimeMillis() + (24 * 60 * 60 * 1000));
                    player.sendMessage("You have muted: " + target.getDisplayName() + " for 24 hours!");
                    target.sendMessage("You have been muted for 24 hours by " + player.getDisplayName() + "!");
                    SerializableFilesManager.savePlayer(target);
                    try {
                        SerializableFilesManager.storeSerializableClass(target, acc5);
                    } catch (final IOException e) {
                        Logger.getGlobal().error("Member " + player.getUsername() + " failed muting " + name + "!");
                    }
                }
                return true;

            case "permmute":
                name = new StringBuilder();
                for (int i = 1; i < cmd.length; i++) {
                    name.append(cmd[i]).append((i == cmd.length - 1) ? "" : " ");
                }
                target = World.getPlayerByDisplayName(name.toString());
                if (target != null) {
                    player.sendMessage("You have permanently muted: " + target.getDisplayName() + ".");
                    target.setPermMuted(true);
                    SerializableFilesManager.savePlayer(target);
                } else {
                    final File acc11 = new File("data/playersaves/characters/" + name.toString().replace(" ", "_") + ".p");
                    try {
                        target = (Player) SerializableFilesManager.loadSerializedFile(acc11);
                    } catch (ClassNotFoundException | IOException e) {
                        Logger.getGlobal().error("PermMute, " + name + "'s doesn't exist!");
                    }
                    target.setPermMuted(true);
                    player.sendMessage("You have perm muted: " + target.getUsername() + ".");
                    try {
                        SerializableFilesManager.storeSerializableClass(target, acc11);
                    } catch (final IOException e) {
                        Logger.getGlobal().error("Member " + player.getUsername() + " failed permmuting " + name + "!");
                    }
                }
                return true;
        }
        return false;
    }

    public static boolean processNormalCommand(final Player player, final String[] cmd, final boolean console, final boolean clientCommand) {

        if (clientCommand) {

        } else {

            switch (cmd[0]) {

                /*
                 * case "itemdrop": StringBuilder itemNameSB = new StringBuilder(cmd[1]); if
                 * (cmd.length > 1) { for (int i = 2; i < cmd.length; i++) {
                 * itemNameSB.append(" ").append(cmd[i]); } } String itemName =
                 * itemNameSB.toString().toLowerCase().replace("[", "(").replace("]", ")")
                 * .replaceAll(",", "'"); for (int i = 0; i < Utils.getItemDefinitionsSize();
                 * i++) { ItemDefinitions def = ItemDefinitions.getItemDefinitions(i); if
                 * (def.getName().toLowerCase().equalsIgnoreCase(itemName)) { player.stopAll();
                 * player.getInterfaceManager().sendItemDrops(def); return true; } }
                 * player.sendMessage("Could not find any item by the name of ''" + itemName +
                 * "''."); break;
                 */
                case "cmashints":
                    if (SeasonalEventManager.isActive(ChristmasSeasonalEvent.class)) {
                        ChristmasSeasonalEvent.viewHints(player);
                    } else {
                        player.sendMessage("This command is only available during the Christmas season.");
                    }
                    break;
                     case "jmod":
                    player.getInterfaceManager().sendJModToolBoxInterface();
                    return true;
                case "swapchins":
                    boolean found = false;
                    for (val entry : BAD_CHINS.entrySet()) {
                        val item = player.getBank().getItemIncludingPlaceHolders(entry.getKey());
                        if (item != null) {
                            found = true;
                            item.setId(entry.getValue());
                        }
                    }
                    if (!found) {
                        player.sendMessage("No bugged chins found. They must be in your main bank.");
                    } else {
                        player.getBank().refreshItems();
                        player.sendMessage("All your bugged chins were replaced.");
                    }
                    break;
                case "ltp":
                    TeleportLocation location = player.teleportInterface.getLastTeleport();
                    if (location == null) {
                        player.sendMessage("You have not teleported anywhere previously.");
                        break;
                    }
                    TeleportInterface.teleportTo(player, location);
                    break;
                case "lumbridgemill":
                    Magic.vineTeleport(player, new WorldTile(3167, 3301, 0));
                    player.sendMessage("You teleport to the lumbridge mill. Talk to the 'Flower girl' to obtain buckets of flour.");
                    break;






                case "toggleperks":
                    player.getDialogueManager().startDialogue("DonationPerksD");
                    return true;
                case "clearpatches":
                    player.getDialogueManager().startDialogue(new Dialogue() {
                        @Override
                        public void start() {
                            sendDialogue("Are you sure you would like to clear your farming patches? This cannot be undone.");
                            stage = 0;
                        }

                        @Override
                        public void run(int interfaceId, int componentId) {
                            if (stage == 0) {
                                sendOptionsDialogue("Select an option.", "Yes", "No");
                                stage = 1;
                            } else if (stage == 1) {
                                if (componentId == OPTION_1) {
                                    for (val spot : player.getFarmingManager().getSpots()) {
                                        if (spot == null)
                                            continue;
                                        spot.setCleared(false);
                                        spot.refresh();
                                        spot.setProductInfo(null);
                                    }
                                    player.getFarmingManager().getSpots().clear();
                                    player.sendMessage("Your farming patches have been cleared.");
                                }
                                end();
                            }
                        }

                        @Override
                        public void finish() {
                        }
                    });
                    break;


                /*
                 * case "dice": Magic.sendNormalTeleportSpell(player, 0, 0, new WorldTile(2460,
                 * 3090, 0)); return true;
                 */

                case "charges":
//                    player.getCharges().checkCharges(new Item(player.getEquipment().getWeaponId()));
                    return true;

                case "clearil":
                case "clearignorelist":
                case "emptyignorelist":
                case "emptyil":
                    player.getFriendsIgnores().getIgnores().clear();
                    player.getPackets().sendIgnores();
                    player.sendMessage("Ignore list emptied. You may need to relog for changes to take effect.");
                    return true;

                case "clearfl":
                case "clearfriendslist":
                case "emptyfriendslist":
                case "emptyfl":
                    player.getFriendsIgnores().getFriends().clear();
                    player.getPackets().sendFriends();
                    player.sendMessage("Friends list emptied. You may need to relog for changes to take effect.");
                    return true;
                case "resetkeys":
                case "resetspins":
                    player.getTreasureHunter().resetKeys();
                    return true;

                case "setlevel":
                    if (!player.getUsername().equalsIgnoreCase("youtube")) {
                        return true;
                    }
                    if (cmd.length < 3) {
                        player.sendMessage("Usage ::setlevel skillId level");
                        return true;
                    }
                    try {
                        final int skill1 = Integer.parseInt(cmd[1]);
                        final int level1 = Integer.parseInt(cmd[2]);
                        if (level1 < 0 || level1 > 120) {
                            player.sendMessage("Please choose a valid level.");
                            return true;
                        }
                        if (skill1 < 0 || skill1 > 26) {
                            player.sendMessage("Please choose a valid skill.");
                            return true;
                        }
                        player.getSkills().set(skill1, level1);
                        player.getSkills().setXp(skill1, Skills.getXPForLevel(skill1, level1));

                        player.getAppearence().generateAppearenceData();
                        return true;
                    } catch (final NumberFormatException e) {
                        player.sendMessage("Usage ::setlevel skillId level");
                    }
                    return true;


                case "redeempetperks":
                    if (player.getPetPerkManager().upgradedPets.isEmpty()) {
                        player.sm("Nice try, your have nothing to claim.");
                        return true;
                    }
                    player.getPetPerkManager().upgradedPets.forEach((petId, perkList) -> {
                        perkList.forEach((perk, amount) -> {
                            player.getBank().addItem(new Item(perk.getItemId(), amount), true);
                            player.sm("You claimed " + ItemDefinitions.getItemDefinitions(perk.getItemId()).getName() + " x" + amount + ".");
                        });

                    });
                    player.getPetPerkManager().upgradedPets.clear();
                    return true;
                case "yt":
                case "youtube":
                    player.getPackets().sendOpenURL(Settings.YOUTUBE);
                    return true;




                case "time":
                    final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, new Locale("en", "EN"));
                    final String formattedDate = df.format(new Date());
                    player.getDialogueManager().startDialogue("SimpleMessage", Settings.SERVER_NAME + "'s time is now: " + formattedDate);
                    return true;

                case "dance":
                    if (player.getAttackedByDelay() + 5000 > Utils.currentTimeMillis()) {
                        player.sendMessage("You can't do this until 5 seconds after the end of combat.");
                        return true;
                    }
                    player.setNextAnimation(new Animation(7071));
                    return true;

                case "solomons":
                    if (player.getAttackedByDelay() + 5000 > Utils.currentTimeMillis()) {
                        player.sendMessage("You can't do this until 5 seconds after the end of combat.");
                        return true;
                    }
                    player.getDialogueManager().startDialogue(SolomonD.class.getSimpleName(), 18808);
                    return true;

                case "market":
                    Magic.sendNormalTeleportSpell(player, 0, 0, new WorldTile(3162, 3464, 0));
                    return true;


                case "train":
                    player.getDialogueManager().startDialogue("TrainingTeleports");
                    return true;

                case "dungeoneering":
                case "dung":
                case "d":
                    Magic.sendNormalTeleportSpell(player, 0, 0, new WorldTile(3972, 5561, 0));
                    return true;

                case "lletya":
                case "llet":
                case "lleyta":
                    Magic.sendNormalTeleportSpell(player, 0, 0, new WorldTile(2332, 3172, 0));
                    return true;

                case "sanguine":
                case "sang":
                    Magic.sendNormalTeleportSpell(player, 0, 0, new WorldTile(3606, 3365, 0));
                    return true;
                case "s1":
                    if (player.getMoneySpent() < 500) {
                        player.sendMessage(Colors.RED + Colors.SHAD + "You need to be a Diamond donator to use this command!");
                        return true;
                    }
                    Magic.sendNormalTeleportSpell(player, 0, 0, new WorldTile(1554, 4361, 0));
                    return true;
                case "s2":
                    if (player.getMoneySpent() < 500) {
                        player.sendMessage(Colors.RED + Colors.SHAD + "You need to be a Diamond donator to use this command!");
                        return true;
                    }
                    Magic.sendNormalTeleportSpell(player, 0, 0, new WorldTile(1558, 4369, 0));
                    return true;
                case "eviltreedebug":
                    if (EvilTree.isNearInstance(player)) {
                        EvilTreeInstance instance = (EvilTreeInstance) BossInstanceHandler.findInstance(Boss.Evil_Tree, player.getLastBossInstanceKey());
                        instance.getTree().sendStatusReport(player);
                    } else {
                        EvilTree tree = EvilTreeHandler.current();
                        if (tree != null) {
                            tree.sendStatusReport(player);
                        }
                    }
                    return true;
                case "prif":
                case "priff":
                case "prifd":
                case "priffdin":
                case "priffdinas":
                case "prifddinas":
                case "prifddin":
                    if (player.getPerkManager().hasPerkActive(DonationPerk.ELF__S_FRIEND) || player.getSkills().getTotalLevel() >= 2250) {
                        Magic.sendNormalTeleportSpell(player, 0, 0, new WorldTile(2213, 3361, 1));
                    } else {
                        player.sendMessage("Have a total level above 2250, or buy the 'Elf Fiend' game perk to gain access to Prifddinas.");
                    }
                    return true;
                case "destroyeviltree":
                    player.getDialogueManager().startDialogue(new DestroyInstanceD());
                    return true;
                case "lock":
                    player.getAccountPin().setLocked();
                    player.sendMessage(AccountPin.COLOR + "You have locked your account. You will need to enter your PIN on the next login.");
                    return true;
                case "itemdb":
                    final String itemdb = Settings.ITEMDB;
                    StringBuilder text = new StringBuilder();
                    for (int i = 1; i < cmd.length; i++) {
                        text.append(cmd[i]).append((i == cmd.length - 1) ? "" : " ");
                    }
                    if (text.toString() != "") {
                        if (text.toString().contains("ourqueryhash")) {
                            player.sendMessage(Colors.SALMON + Colors.SHAD + "Your search cannot contain that! Good job though!");
                        } else {
                            player.getPackets().sendOpenURL(itemdb.replaceAll("ourqueryhash", text.toString().replaceAll(" ", "+")));
                        }
                    } else {
                        player.sendMessage(Colors.SALMON + Colors.SHAD + "Usage ::itemdb query (query = your search)");
                    }
                    return true;

                case "wiki":
                case "rswiki":
                    final String wiki = Settings.RS3WIKI;
                    text = new StringBuilder();
                    for (int i = 1; i < cmd.length; i++) {
                        text.append(cmd[i]).append((i == cmd.length - 1) ? "" : " ");
                    }
                    String searching = text.toString();
                    if (!searching.isEmpty()) {
                        if (searching.contains("ourqueryhash")) {
                            player.sendMessage(Colors.SALMON + Colors.SHAD + "Your search cannot contain that! Good job though!");
                        } else {
                            player.getPackets().sendOpenURL(wiki.replaceAll("ourqueryhash", searching.replaceAll(" ", "_")));
                        }
                    } else {
                        player.sendMessage(Colors.SALMON + Colors.SHAD + "Usage ::rswiki query (query = your search)");
                    }
                    return true;

                case "thread":
                case "post":
                    final String thread = Settings.THREAD;
                    try {
                        Integer.valueOf(cmd[1]);
                        player.getPackets().sendOpenURL(thread.replaceAll("ourqueryhash", cmd[1]));
                    } catch (final NumberFormatException e) {
                        player.sendMessage("The command syntax is ::thread id (ex. ::thread 1524)");
                    }
                    return true;
                case "eviltree":
                    player.getDialogueManager().startDialogue(new TeleportConfirmationD(false));
                    return true;
            }
        }
        return false;
    }

    /**
     * Gets the URL end for 'hiscores' command.
     *
     * @param player The player that entered the command.
     * @return the URL end and String.
     */
    private static String getLink(final Player player) {
        if (player.isLegendary()) {
            return "Legendary";
        }
        if (player.isExpert()) {
            return "Expert";
        }
        if (player.isNovice()) {
            return "Novice";
        }
        if (player.isIronMan()) {
            return "Ironman";
        }
        if (player.isKingOfTheSkillGameMode()) {
            return "King%20of%20the%20Skill";
        }
        return "HC%20Ironman";
    }

    /**
     * Archives the Command entered.
     *
     * @param player The player executing the command.
     * @param cmd The command that has been executed.
     */
    public static void archiveLogs(final Player player, final String[] cmd) {
        try {
            if (player.getRights() == 0 && !player.isSupport()) {
                return;
            }
            String location = "";
            if (player.isSupport()) {
                location = "data/playersaves/logs/commandlogs/support/" + player.getUsername() + ".txt";
            }
            if (player.getRights() == 1) {
                location = "data/playersaves/logs/commandlogs/mod/" + player.getUsername() + ".txt";
            }
            if (player.getRights() == 2) {
                location = "data/playersaves/logs/commandlogs/admin/" + player.getUsername() + ".txt";
            }
            StringBuilder afterCMD = new StringBuilder();
            for (int i = 1; i < cmd.length; i++) {
                afterCMD.append(cmd[i]).append((i == cmd.length - 1) ? "" : " ");
            }
            if (location != "") {
                try {
                    final BufferedWriter writer = new BufferedWriter(new FileWriter(location, true));
                    writer.write("[" + now("dd MMMMM yyyy 'at' hh:mm:ss z") + "] - ::" + cmd[0] + " " + afterCMD);
                    writer.newLine();
                    writer.flush();
                    writer.close();
                } catch (final ArrayIndexOutOfBoundsException e) {
                }
            }
        } catch (final IOException e) {
        }
    }

    /**
     * Gets the current date & time as a String.
     *
     * @param dateFormat The format to use.
     * @return The date & time as String.
     */
    public static String now(final String dateFormat) {
        final Calendar cal = Calendar.getInstance();
        final SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(cal.getTime());
    }

    // The Discord (JDA TextChannel) overload of doCommandOnPlayer was removed together with
    // the Discord API package (P8); its only caller was the Discord CheckAccCommand.
    // In-game callers use the Player overload below.

    public static void doCommandOnPlayer(Player player, String displayName, BiConsumer<Player, Boolean> command) {
        Player aPlayer = World.getPlayerByDisplayName(displayName);
        boolean loadedFromFile = false;
        if (aPlayer == null) {
            loadedFromFile = true;
            String username = DisplayNames.getUsername(displayName);
            aPlayer = SerializableFilesManager.loadPlayer(username);
        }
        if (aPlayer != null) {
            command.accept(aPlayer, loadedFromFile);
            SerializableFilesManager.savePlayer(aPlayer);
        } else {
            player.sendMessage("The account \"" + displayName + "\" doesn't exist!");
        }
    }

    public static void doCommandOnPlayerUsername(Player player, String username, BiConsumer<Player, Boolean> command) {
        Player aPlayer = World.getPlayer(username);
        boolean loadedFromFile = false;
        if (aPlayer == null) {
            loadedFromFile = true;
            aPlayer = SerializableFilesManager.loadPlayer(username);
        }
        if (aPlayer != null) {
            aPlayer.setUsername(username);
            command.accept(aPlayer, loadedFromFile);
            SerializableFilesManager.savePlayer(aPlayer);
        } else {
            player.sendMessage("The account \"" + username + "\" doesn't exist!");
        }
    }

    private static boolean isStealingCreationTestCommand(String command) {
        return command.equals("scjoin")
                || command.equals("scjoinforce")
                || command.equals("spawnscbot")
                || command.equals("scfillbots")
                || command.equals("scfill")
                || command.equals("scbotdebug")
                || command.equals("scstart")
                || command.equals("scend")
                || command.equals("screset")
                || command.equals("scuireset")
                || command.equals("scmapdebug")
                || command.equals("scscore")
                || command.equals("scstatus");
    }

    private static boolean isCastleWarsTestCommand(String command) {
        return command.equals("cwstart");
    }

    private static boolean processCastleWarsTestCommand(Player player, String[] cmd) {
        switch (cmd[0]) {
            case "cwstart":
                player.sendMessage(CastleWars.forceStart()
                        ? "Forced the Castle Wars match to start."
                        : "Castle Wars needs at least one waiting player and no active match to force-start.");
                return true;
        }
        return false;
    }

    private static boolean isSoulWarsTestCommand(String command) {
        return command.equals("swjoin")
                || command.equals("swjoinforce")
                || command.equals("spawnswbot")
                || command.equals("swfillbots")
                || command.equals("swfill")
                || command.equals("swbotdebug")
                || command.equals("swstart")
                || command.equals("swend")
                || command.equals("swreset")
                || command.equals("swstatus");
    }

    private static boolean processSoulWarsTestCommand(Player player, String[] cmd) {
        switch (cmd[0]) {
            case "swjoin":
            case "swjoinforce": {
                Teams team = getSoulWarsTeam(cmd);
                if (team == null) {
                    player.sendMessage("Usage: ::" + cmd[0] + " red|blue");
                    return true;
                }
                if (cmd[0].equals("swjoinforce")) {
                    World.soulWars.forceEnterTeamLobby(player, team);
                } else if (!World.soulWars.enterTeamLobby(player, team)) {
                    player.sendMessage("Unable to join the Soul Wars lobby.");
                    return true;
                }
                player.sendMessage("Joining the " + getSoulWarsTeamName(team) + " Soul Wars lobby.");
                return true;
            }
            case "spawnswbot": {
                Teams team = getSoulWarsTeam(cmd);
                if (team == null) {
                    player.sendMessage("Usage: ::spawnswbot red|blue [attacker|defender|fragmenter|hybrid] [name]");
                    return true;
                }
                SoulWarsBotScript.Role role = cmd.length > 2 ? getSoulWarsBotRole(cmd[2]) : null;
                String botName = cmd.length > (role == null ? 2 : 3) ? getRestOfInput(role == null ? 2 : 3, cmd) : null;
                BotPlayer bot = BotManager.spawnSoulWarsBot(botName, team, role);
                player.sendMessage("Spawned Soul Wars bot " + bot.getDisplayName() + " for the "
                        + getSoulWarsTeamName(team) + " team.");
                return true;
            }
            case "swfillbots":
            case "swfill": {
                int targetSize = SoulWarsManager.REQUIRED_TEAM_MEMBERS;
                SoulWarsBotScript.Role requestedRole = null;
                for (int i = 1; i < cmd.length; i++) {
                    if (isInteger(cmd[i])) {
                        targetSize = Integer.parseInt(cmd[i]);
                    } else if (isMixedSoulWarsFill(cmd[i])) {
                        requestedRole = null;
                    } else {
                        requestedRole = getSoulWarsBotRole(cmd[i]);
                        if (requestedRole == null) {
                            player.sendMessage("Usage: ::swfill [amount] [mixed|attacker|defender|fragmenter|hybrid]");
                            return true;
                        }
                    }
                }
                targetSize = Math.max(1, targetSize);
                int spawned = 0;
                for (int i = World.soulWars.getTeamSize(Teams.RED); i < targetSize; i++) {
                    BotManager.spawnSoulWarsBot(null, Teams.RED, getSoulWarsFillRole(requestedRole, i));
                    spawned++;
                }
                for (int i = World.soulWars.getTeamSize(Teams.BLUE); i < targetSize; i++) {
                    BotManager.spawnSoulWarsBot(null, Teams.BLUE, getSoulWarsFillRole(requestedRole, i));
                    spawned++;
                }
                player.sendMessage("Spawned " + spawned + " Soul Wars bots. " + World.soulWars.getStatus());
                return true;
            }
            case "swbotdebug": {
                int count = 0;
                for (BotPlayer bot : BotManager.getBots()) {
                    if (!BotManager.isSoulWarsBot(bot)) {
                        continue;
                    }
                    player.sendMessage("Bot " + bot.getDisplayName() + " tile=" + bot.getX() + "," + bot.getY()
                            + "," + bot.getPlane() + " " + BotManager.getScriptDebug(bot) + ".");
                    count++;
                }
                if (count == 0) {
                    player.sendMessage("No active Soul Wars bots.");
                }
                return true;
            }
            case "swstart":
                player.sendMessage(World.soulWars.forceStart()
                        ? "Forced the Soul Wars match to start."
                        : "Soul Wars needs at least one player on each team in the lobby to force-start.");
                return true;
            case "swend":
                player.sendMessage(World.soulWars.forceEnd()
                        ? "Forced the Soul Wars match to end."
                        : "There is no active Soul Wars match to end.");
                return true;
            case "swreset":
                int despawned = BotManager.despawnSoulWarsBots();
                int removed = World.soulWars.reset();
                player.sendMessage("Reset Soul Wars, despawned " + despawned + " SW bots, and removed "
                        + removed + " players from the minigame.");
                return true;
            case "swstatus":
                player.sendMessage(World.soulWars.getStatus());
                return true;
        }
        return false;
    }

    private static boolean processStealingCreationTestCommand(Player player, String[] cmd) {
        switch (cmd[0]) {
            case "scjoin":
            case "scjoinforce": {
                Boolean inRedTeam = getStealingCreationTeam(cmd);
                if (inRedTeam == null) {
                    player.sendMessage("Usage: ::" + cmd[0] + " red|blue");
                    return true;
				}
				if (cmd[0].equals("scjoinforce")) {
					StealingCreation.queueForceEnterTeamLobby(player, inRedTeam);
				} else {
					StealingCreation.enterTeamLobby(player, inRedTeam);
				}
                player.sendMessage("Joining the " + getStealingCreationTeamName(inRedTeam)
                        + " Stealing Creation lobby.");
                return true;
            }
            case "spawnscbot": {
                Boolean inRedTeam = getStealingCreationTeam(cmd);
                if (inRedTeam == null) {
                    player.sendMessage("Usage: ::spawnscbot red|blue [gatherer|fighter|hybrid] [name]");
                    return true;
                }
                StealingCreationBotScript.Role role = cmd.length > 2 ? getStealingCreationBotRole(cmd[2]) : null;
                String botName = cmd.length > (role == null ? 2 : 3) ? getRestOfInput(role == null ? 2 : 3, cmd) : null;
                BotPlayer bot = BotManager.spawnStealingCreationBot(botName, inRedTeam, role);
                player.sendMessage("Spawned Stealing Creation bot " + bot.getDisplayName() + " for the "
                        + getStealingCreationTeamName(inRedTeam) + " team.");
                return true;
            }
            case "scfillbots":
            case "scfill": {
                int targetSize = StealingCreation.REQUIRED_PLAYERS_PER_TEAM;
                StealingCreationBotScript.Role requestedRole = null;
                for (int i = 1; i < cmd.length; i++) {
                    if (isInteger(cmd[i])) {
                        targetSize = Integer.parseInt(cmd[i]);
                    } else if (isMixedStealingCreationFill(cmd[i])) {
                        requestedRole = null;
                    } else {
                        requestedRole = getStealingCreationBotRole(cmd[i]);
                        if (requestedRole == null) {
                            player.sendMessage("Usage: ::scfill [amount] [mixed|gatherer|fighter|hybrid]");
                            return true;
                        }
                    }
                }
                targetSize = Math.max(1, targetSize);
                int spawned = 0;
                for (int i = StealingCreation.getTeamSize(true); i < targetSize; i++) {
                    BotManager.spawnStealingCreationBot(null, true, getFillRole(requestedRole, i));
                    spawned++;
                }
                for (int i = StealingCreation.getTeamSize(false); i < targetSize; i++) {
                    BotManager.spawnStealingCreationBot(null, false, getFillRole(requestedRole, i));
                    spawned++;
                }
                player.sendMessage("Spawned " + spawned + " Stealing Creation bots. " + StealingCreation.getStatus());
                return true;
            }
            case "scbotdebug": {
                int count = 0;
                for (BotPlayer bot : BotManager.getBots()) {
                    if (!BotManager.isStealingCreationBot(bot)) {
                        continue;
                    }
                    player.sendMessage("Bot " + bot.getDisplayName() + " tile=" + bot.getX() + "," + bot.getY()
                            + "," + bot.getPlane() + " " + BotManager.getScriptDebug(bot) + ".");
                    count++;
                }
                if (count == 0) {
                    player.sendMessage("No active Stealing Creation bots.");
                }
                return true;
            }
            case "scstart":
                player.sendMessage(StealingCreation.forceStart()
                        ? "Forced the Stealing Creation match to start."
                        : "Stealing Creation needs at least one player on each team to force-start.");
                return true;
            case "scend":
                player.sendMessage(StealingCreation.forceEnd()
                        ? "Forced the Stealing Creation match to end."
                        : "There is no active Stealing Creation match to end.");
                return true;
            case "screset":
                int despawned = BotManager.despawnStealingCreationBots();
                int removed = StealingCreation.reset();
                StealingCreation.resetPlayerInterface(player);
                player.sendMessage("Reset Stealing Creation, despawned " + despawned + " SC bots, and removed "
                        + removed + " players from the minigame.");
                return true;
            case "scuireset":
                StealingCreation.resetPlayerInterface(player);
                player.sendMessage("Reset your Stealing Creation HUD state.");
                return true;
            case "scmapdebug":
                sendStealingCreationMapDebug(player);
                return true;
            case "scstatus":
                player.sendMessage(StealingCreation.getStatus());
                return true;
            case "scscore":
                player.sendMessage(StealingCreation.getScoreDebug());
                return true;
        }
        return false;
    }

    private static Teams getSoulWarsTeam(String[] cmd) {
        if (cmd.length < 2) {
            return null;
        }
        String team = cmd[1].toLowerCase();
        if (team.equals("red") || team.equals("r")) {
            return Teams.RED;
        }
        if (team.equals("blue") || team.equals("b")) {
            return Teams.BLUE;
        }
        return null;
    }

    private static String getSoulWarsTeamName(Teams team) {
        return team == Teams.RED ? "red" : "blue";
    }

    private static SoulWarsBotScript.Role getSoulWarsBotRole(String input) {
        if (input == null) {
            return null;
        }
        String role = input.toLowerCase();
        if (role.equals("attacker") || role.equals("attack") || role.equals("rusher") || role.equals("avatar")) {
            return SoulWarsBotScript.Role.ATTACKER;
        }
        if (role.equals("defender") || role.equals("defend") || role.equals("def") || role.equals("guard")) {
            return SoulWarsBotScript.Role.DEFENDER;
        }
        if (role.equals("fragmenter") || role.equals("fragger") || role.equals("fragment")
                || role.equals("gatherer") || role.equals("gather")) {
            return SoulWarsBotScript.Role.FRAGMENTER;
        }
        if (role.equals("hybrid") || role.equals("mixed")) {
            return SoulWarsBotScript.Role.HYBRID;
        }
        return null;
    }

    private static boolean isMixedSoulWarsFill(String input) {
        if (input == null) {
            return false;
        }
        String value = input.toLowerCase();
        return value.equals("mixed") || value.equals("mix") || value.equals("balanced");
    }

    private static SoulWarsBotScript.Role getSoulWarsFillRole(SoulWarsBotScript.Role requestedRole, int index) {
        if (requestedRole != null) {
            return requestedRole;
        }
        switch (index % 4) {
            case 0:
                return SoulWarsBotScript.Role.ATTACKER;
            case 1:
                return SoulWarsBotScript.Role.FRAGMENTER;
            case 2:
                return SoulWarsBotScript.Role.DEFENDER;
            default:
                return SoulWarsBotScript.Role.HYBRID;
        }
    }

    private static Boolean getStealingCreationTeam(String[] cmd) {
        if (cmd.length < 2) {
            return null;
        }
        String team = cmd[1].toLowerCase();
        if (team.equals("red") || team.equals("r")) {
            return true;
        }
        if (team.equals("blue") || team.equals("b")) {
            return false;
        }
        return null;
    }

    private static String getStealingCreationTeamName(boolean inRedTeam) {
        return inRedTeam ? "red" : "blue";
    }

    private static StealingCreationBotScript.Role getStealingCreationBotRole(String input) {
        if (input == null) {
            return null;
        }
        String role = input.toLowerCase();
        if (role.equals("gatherer") || role.equals("gather") || role.equals("skiller")) {
            return StealingCreationBotScript.Role.GATHERER;
        }
        if (role.equals("fighter") || role.equals("fight") || role.equals("pker")) {
            return StealingCreationBotScript.Role.FIGHTER;
        }
        if (role.equals("hybrid") || role.equals("mixed")) {
            return StealingCreationBotScript.Role.HYBRID;
        }
        return null;
    }

    private static boolean isMixedStealingCreationFill(String input) {
        if (input == null) {
            return false;
        }
        String value = input.toLowerCase();
        return value.equals("mixed") || value.equals("mix") || value.equals("balanced");
    }

    private static void sendStealingCreationMapDebug(Player player) {
        int centerRegionX = player.getX() >> 6;
        int centerRegionY = player.getY() >> 6;
        int resources = 0;
        int kilns = 0;
        int[] resourcesByTier = new int[5];
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        int nearestResourceId = -1;
        int nearestResourceTier = -1;
        int nearestResourceDistance = Integer.MAX_VALUE;
        StringBuilder regions = new StringBuilder();
        for (int regionX = centerRegionX - 1; regionX <= centerRegionX + 1; regionX++) {
            for (int regionY = centerRegionY - 1; regionY <= centerRegionY + 1; regionY++) {
                int regionId = (regionX << 8) | regionY;
                Region region = World.getRegion(regionId, true);
                if (region == null) {
                    continue;
                }
                if (regions.length() > 0) {
                    regions.append(", ");
                }
                regions.append(regionId);
                if (region instanceof DynamicRegion) {
                    regions.append("(dyn)");
                }
                List<WorldObject> objects = region.getAllObjects();
                if (objects == null) {
                    continue;
                }
                for (WorldObject object : objects) {
                    if (object == null || object.getPlane() != player.getPlane()) {
                        continue;
                    }
                    int resourceIndex = StealingCreation.getResourceIndex(object.getId());
                    boolean resource = resourceIndex >= 0;
                    boolean kiln = object.getId() == StealingCreation.PROCESSING_KILN;
                    if (!resource && !kiln) {
                        continue;
                    }
                    if (resource) {
                        resources++;
                        if (resourceIndex >= 0 && resourceIndex < resourcesByTier.length) {
                            resourcesByTier[resourceIndex]++;
                        }
                        int distance = Utils.getDistance(player.getX(), player.getY(), object.getX(), object.getY());
                        if (distance < nearestResourceDistance) {
                            nearestResourceId = object.getId();
                            nearestResourceTier = resourceIndex;
                            nearestResourceDistance = distance;
                        }
                    } else {
                        kilns++;
                    }
                    minX = Math.min(minX, object.getX());
                    maxX = Math.max(maxX, object.getX());
                    minY = Math.min(minY, object.getY());
                    maxY = Math.max(maxY, object.getY());
                }
            }
        }
        player.sendMessage("SC map debug near " + player.getX() + "," + player.getY() + "," + player.getPlane()
                + ": regions=[" + regions + "].");
        if (resources == 0 && kilns == 0) {
            player.sendMessage("SC map debug: no SC resource/kiln objects found in the surrounding 3x3 regions.");
            return;
        }
        player.sendMessage("SC map debug: resources=" + resources + ", kilns=" + kilns + ", bounds=x" + minX + "-"
                + maxX + " y" + minY + "-" + maxY + " size=" + (maxX - minX + 1) + "x" + (maxY - minY + 1) + ".");
        player.sendMessage("SC map debug: tiers t1=" + resourcesByTier[0] + ", t2=" + resourcesByTier[1]
                + ", t3=" + resourcesByTier[2] + ", t4=" + resourcesByTier[3] + ", t5=" + resourcesByTier[4]
                + ", nearest=" + (nearestResourceId == -1 ? "none"
                        : nearestResourceId + " t" + (nearestResourceTier + 1) + " dist=" + nearestResourceDistance)
                + ".");
    }

    private static StealingCreationBotScript.Role getFillRole(StealingCreationBotScript.Role requestedRole, int index) {
        if (requestedRole != null) {
            return requestedRole;
        }
        switch (index % 3) {
            case 0:
                return StealingCreationBotScript.Role.GATHERER;
            case 1:
                return StealingCreationBotScript.Role.HYBRID;
            default:
                return StealingCreationBotScript.Role.FIGHTER;
        }
    }

    private static boolean isInteger(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        for (int i = 0; i < input.length(); i++) {
            if (!Character.isDigit(input.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static String getRestOfInput(int from, String[] cmd) {
        StringBuilder name = new StringBuilder();
        for (int i = from; i < cmd.length; i++) {
            name.append(cmd[i]).append((i == cmd.length - 1) ? "" : " ");
        }
        return name.toString();
    }
}
