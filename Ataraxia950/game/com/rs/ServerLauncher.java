package com.rs;


import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.rs.game.player.security.pin.AccountPin;
import org.apache.logging.log4j.message.ParameterizedMessage;
import com.google.common.base.Stopwatch;
import com.rs.cache.Cache;
import com.rs.cache.filestore.store.Index;
import com.rs.cache.loaders.BodyDefinitions;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.ItemsEquipIds;
import com.rs.cache.loaders.ObjectDefinitions;
import com.rs.cache.loaders.WorldAreaTypeDefinitions;
import com.rs.cores.CoresManager;
import com.rs.game.MapBuilder;
import com.rs.game.World;
import com.rs.game.activites.dnd.eviltree.EvilTreeHandler;
import com.rs.game.activites.gim.GIM;
import com.rs.game.activities.ActivitiesScheduler;
import com.rs.game.activities.seasonalevents.SeasonalEventManager;
import com.rs.game.map.bossInstance.BossInstanceHandler;
import com.rs.game.npc.combat.CombatScriptsHandler;
import com.rs.game.npc.gwd2.faction.FactionManager;
import com.rs.game.player.ChargesDatabase;
import com.rs.game.player.CosmeticsManager;
import com.rs.game.player.LendingManager;
import com.rs.game.player.Player;
import com.rs.game.player.actions.runecrafting.SiphonActionNodes;
import com.rs.game.player.bots.BotManager;
import com.rs.game.player.content.FishingSpotsHandler;
import com.rs.game.player.content.FriendChatsManager;
import com.rs.game.player.content.TelosEnrageRanks;
import com.rs.game.player.content.TriviaBot;
import com.rs.game.player.content.WellOfGoodWill;
import com.rs.game.player.content.agility.AgilityManager;
import com.rs.game.player.content.araxxor.AraxxorManager;
import com.rs.game.player.content.bank_highscores.GetRichestBanksSql;
import com.rs.game.player.content.clans.ClansManager;
import com.rs.game.player.content.death.DeathStatistics;
import com.rs.game.player.content.eds.EliteDungeon;
import com.rs.game.player.content.grandExchange.GrandExchange;
import com.rs.game.player.content.jujupotions.jadinkos.JadinkoManager;
import com.rs.game.player.content.newlottery.Lottery;
import com.rs.game.player.content.skillingcontracts.SkillingContractManager;
import com.rs.game.player.content.skillingcontracts.SkillingContractTracker;
import com.rs.game.player.controllers.ControllerHandler;
import com.rs.game.player.cutscenes.CutscenesHandler;
import com.rs.game.player.dialogue.impl.FlowerGirlD;
import com.rs.game.player.tt.TTAllRewards;
import com.rs.game.worldlist.WorldRepository;
import com.rs.network.NetworkBootstrapper;
import com.rs.network.codec.ProtocolSet;
import com.rs.utils.AutoBackup;
import com.rs.utils.DisplayNames;
import com.rs.utils.IPBanL;
import com.rs.utils.IPMute;
import com.rs.utils.Logger;
import com.rs.utils.LoggingSqlManager;
import com.rs.utils.MACBan;
import com.rs.utils.NXTClientsManager;
import com.rs.utils.ObjectSpawns;
import com.rs.utils.PkRank;
import com.rs.utils.SerializableFilesManager;
import com.rs.utils.StarterMap;
import com.rs.utils.WorldInformation;
import com.rs.utils.data.parsers.items.ItemDisassembleDataParser;
import com.rs.utils.data.parsers.items.ItemExaminesDataParser;
import com.rs.utils.data.parsers.items.ItemWeightsDataParser;
import com.rs.utils.data.parsers.items.TreasureHunterRewardParser;
import com.rs.utils.data.parsers.maps.CustomObjectSpawnsDataParser;
import com.rs.utils.data.parsers.misc.MusicHintsDataParser;
import com.rs.utils.data.parsers.misc.PerkGenerationDataParser;
import com.rs.utils.data.parsers.misc.ShopsDataParser;
import com.rs.utils.data.parsers.npcs.NPCCombatDefinitionsDataParser;
import com.rs.utils.data.parsers.npcs.NPCDropsDataParser;
import com.rs.utils.data.parsers.npcs.NPCExaminesDataParser;
import com.rs.utils.data.parsers.npcs.NPCSpawnsDataParser;
import com.rs.utils.data.parsers.npcs.NPCStatsDataParser;
import com.rs.utils.data.parsers.npcs.NPCWeaknessesDataParser;
import com.rs.utils.huffman.Huffman;
import com.rs.utils.mysql.SQLThread;

import lombok.val;

/**
 * The Main class for those who cannot read.
 *
 * @author Noel
 */
public final class ServerLauncher {

    static {
        try {
            System.setProperty("log4j.skipJansi", "true");
            System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
            System.setProperty("log4j.configurationFile", "./resources/log4j2.xml");
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static boolean verbose = false;
    private static boolean autoBackupEnabled = true;

    /**
     * The Main server launch method.
     *
     * @param args The launch arguments.
     * @throws Exception The exception to throw.
     */
    public static void main(final String[] args) {
        try {
            /** Run Arguments **/
            if (args.length >= 1) {
                Settings.GUI_MODE = Boolean.parseBoolean(args[0]);
            }
            if (args.length >= 2) {
                Settings.DEBUG = Boolean.parseBoolean(args[1]);
            }
            if (args.length >= 3) {
                Settings.SQL_ENABLED = Boolean.parseBoolean(args[2]);
            }
            if (args.length >= 4) {
                Settings.SERVER_PORT = Integer.parseInt(args[3]);
            }
            if (args.length >= 5) {
                Settings.WORLD_ID = Integer.parseInt(args[4]);
            }
            if (args.length >= 6) {
                Settings.BOT_ENABLED = Boolean.parseBoolean(args[5]);
            }
            if (args.length >= 7) {
                Settings.TEST_SERVER_MODE = Boolean.parseBoolean(args[6]);
            }
            if (args.length >= 8) {
                autoBackupEnabled = Boolean.parseBoolean(args[7]);
            }
            if (args.length >= 9) {
                Settings.CACHE_PATH = args[8];
            }
            if (Settings.DEBUG)
                Settings.WORLDS_INFORMATION = new WorldInformation[] { new WorldInformation(1, 0, "World 1", 0, 0x2 | 0x8, Settings.SERVER_NAME+" | LIVE", "127.0.0.1") };
            init();
        } catch (Throwable e) {
            Logger.getGlobal().fatal("Server could not be started!", e);
        }
    }

    private static void init() throws IOException {

        AccountPin.DISABLED = true;
        if (autoBackupEnabled && !Settings.DEBUG && !Settings.TEST_SERVER_MODE) {
            Logger.getGlobal().info("Preparing Auto-Backup...");
            AutoBackup.init();
        }

        if (Settings.SQL_ENABLED) {
            CoresManager.sqlThread = new SQLThread();
            CoresManager.sqlThread.start();
            Logger.getGlobal().info("SQL Database thread has been started..");
        }

        if (verbose) {
            Logger.getGlobal().info("Preparing Cache...");
        }
        Cache.init();
        ItemsEquipIds.init();
        Huffman.init();
        if (verbose) {
            Logger.getGlobal().info("Compressing nxt clients...");
        }
        NXTClientsManager.init();
        WorldAreaTypeDefinitions.init();



        if (verbose) {
            Logger.getGlobal().info("Preparing Player Data...");
        }

        ChargesDatabase.load();
        DisplayNames.init();
        BodyDefinitions.init();
        CosmeticsManager.setVars();


        IPBanL.init();
        IPMute.init();
        TelosEnrageRanks.init();

        TTAllRewards.init();

        if (verbose) {
            Logger.getGlobal().info("Preparing Mapdata...");
        }
        CustomObjectSpawnsDataParser.init();


        if (verbose) {
            Logger.getGlobal().info("Preparing NPC Data...");
        }
        NPCSpawnsDataParser.init();
        NPCWeaknessesDataParser.init();
        NPCCombatDefinitionsDataParser.init();
        NPCDropsDataParser.init();
        NPCExaminesDataParser.init();
        ItemDisassembleDataParser.init();
        PerkGenerationDataParser.init();
        NPCStatsDataParser.init();
        TreasureHunterRewardParser.init();
        CombatScriptsHandler.init();
        LoggingSqlManager.getInstance().start();
        FishingSpotsHandler.init();



        if (verbose) {
            Logger.getGlobal().info("Loading the Grand Exchange...");
        }
        GrandExchange.init();


        if (verbose) {
            Logger.getGlobal().info("Preparing Item Data...");
        }
        ItemExaminesDataParser.init();
        ItemWeightsDataParser.init();

        /* Initializes music hints */
        if (verbose) {
            Logger.getGlobal().info("Preparing Music Hints...");
        }
        MusicHintsDataParser.init();

        /* Initializes shops data */
        if (verbose) {
            Logger.getGlobal().info("Preparing Shops Handler...");
        }
        ShopsDataParser.init();

        /* Initializes lobby world data */
        if (verbose) {
            Logger.getGlobal().info("Preparing Lobby...");
        }
        WorldRepository.startWorlds();

        /* Initializes clans manager */
        if (verbose) {
            Logger.getGlobal().info("Preparing Clans Manager...");
        }
        ClansManager.init();

        /* Initializes In-game Scripts */
        // DialogueHandler.init();
        Scanner.scan();
        TriviaBot.loadWinners();
        AgilityManager.init();

        if (verbose) {
            Logger.getGlobal().info("Preparing Controller Handler...");
        }
        ControllerHandler.init();
        if (verbose) {
            Logger.getGlobal().info("Preparing Cutscenes Handler...");
        }
        CutscenesHandler.init();
        if (verbose) {
            Logger.getGlobal().info("Preparing Friend Handler...");
        }
        FriendChatsManager.init();

        /* Initializes World thread */
        if (verbose) {
            Logger.getGlobal().info("Preparing World Engine...");
        }
        CoresManager.init();
        World.init();
        EliteDungeon.initialize();
        WellOfGoodWill.load();

        if (verbose) {
            Logger.getGlobal().info("Preparing activities announcement...");
        }
        ActivitiesScheduler.getInstance().load();

        /* Initializes Region Builder */
        if (verbose) {
            Logger.getGlobal().info("Preparing Map Builder...");
        }
        MapBuilder.init();

        /* Initializes Networking */
        Logger.getGlobal().info("Preparing Netty...");
//		GameBootstrap.bind();
        NetworkBootstrapper.initAndStart();

        /* Initializes Lending Manager */
        if (verbose) {
            Logger.getGlobal().info("Preparing Lending Manager...");
        }
        LendingManager.init();

        /* Initializes Vote queue reward task */
        if (verbose) {
            Logger.getGlobal().info("Preparing scheduled game tasks...");
        }
        addAccountsSavingTask();

        if (!Settings.DEBUG && !Settings.TEST_SERVER_MODE) {
            addCleanMemoryTask();
            addRecalculatePricesTask();
        }
        BossInstanceHandler.init();
        AraxxorManager.init(Settings.cal);
        StarterMap.getSingleton().init();
        GetRichestBanksSql.load();
        SiphonActionNodes.init();
        ObjectSpawns.addCustomSpawns();
        FactionManager.init();
        try {
        Lottery.getSingleton().load();
        } catch(Exception e) {
            Logger.getGlobal().info("Couldn't load lottery");
        }
        GIM.start();
        EvilTreeHandler.start();
        SkillingContractTracker.getSingleton().init();
        SkillingContractManager.loadNpc();
        FlowerGirlD.spawn();

        SeasonalEventManager.init();
        JadinkoManager.start();
        // Bots are spawned in-memory only (see BotManager) and do not write
        // any .p files, so they don't bloat the AutoBackup zip. If bot
        // persistence is ever re-enabled (e.g. LumbridgeIronmanPersistence),
        // SerializableFilesManager routes BotPlayer saves to
        // data/playersaves/bots/ which AutoBackup ignores.
        // Gated: 600 bot Players violate the native 947 single-slot gates and
        // flood PLAYER_INFO, so they only spawn when explicitly enabled (args[5]).
        if (Settings.BOT_ENABLED) {
            populateStartupBots();
        } else {
            Logger.getGlobal().info("Startup bots disabled (Settings.BOT_ENABLED=false).");
        }
        /** Server successfully launched :] **/
        Logger.getGlobal().info("Server launched in " + (Settings.DEBUG ? "DEVELOPMENT" : "ECONOMY") + " mode; " + "data: " + ProtocolSet.REVISION + "/" + ProtocolSet.UPDATE + "/" + Settings.SERVER_PORT + ". " + (verbose ? " -verbose " : ""));

        Logger.getGlobal().info("SQL Database pools are " + (Settings.SQL_ENABLED ? "ENABLED" : "DISABLED") + "!");

        /**
         * For memory testing purposes. CoresManager.getServiceProvider().executeNow(()
         * -> { int count = 0; loop : for (int x = 0; x <= 16383; x += 64) { for (int y
         * = 0; y <= 16383; y += 64) { final int rx = x >> 6; final int ry = y >> 6;
         * final int regionId = rx * 256 + ry;
         *
         * final int archiveId = Utils.getMapArchiveId(rx, ry);
         *
         * final byte[] mapSettingsData = Cache.STORE.getIndexes()[5].getFile(archiveId,
         * 3); if (mapSettingsData != null) { World.getRegion(regionId, true); count++;
         * } if (count > 4000) { break loop; }
         *
         * } } System.err.println("Preloaded " + count + " regions!"); });
         */
    }

    private static void populateStartupBots() {
        try {
            int geBots = BotManager.spawnGECrowd(500);
            int wandererBots = BotManager.spawnWandererBots(100);
            Logger.getGlobal().info("Spawned startup bots: " + geBots + " Grand Exchange, "
                    + wandererBots + " wanderers.");
        } catch (Throwable e) {
            Logger.getGlobal().warn("Failed to spawn startup bots.", e);
        }
    }

    /**
     * The scheduled data saving task.
     */
    private static void addAccountsSavingTask() {
        CoresManager.getServiceProvider().scheduleRepeatingTask(new Runnable() {
            private final Stopwatch timer = Stopwatch.createUnstarted();

            @Override
            public void run() {
                timer.start();
                try {
                    saveFiles();
                    long millis = timer.elapsed().toMillis();
                    val msg = new ParameterizedMessage("Asynchronous file data saved, took {}ms (players online: {})", millis, World.getPlayersOnline());
                    if (millis > 5_000) {
                        Logger.getGlobal().warn(msg);
                    } else {
                        Logger.getGlobal().info(msg);
                    }
                } finally {
                    timer.reset();
                }
            }
        }, 1, 2, TimeUnit.MINUTES);
    }

    private static void addRecalculatePricesTask() {

        CoresManager.getServiceProvider().scheduleRepeatingTask(() -> {
            try {
                GrandExchange.recalcPrices();
            } catch (final Throwable e) {
                Logger.getGlobal().catching(e);
            }

        }, 1, 3, TimeUnit.HOURS);

    }

    /**
     * The scheduled Garbage collector task.
     */
    private static void addCleanMemoryTask() {
        CoresManager.getServiceProvider().scheduleRepeatingTask(() -> {
            try {
                for (final Index index : Cache.STORE.getIndexes()) {
                    index.resetCachedFiles();
                }
                System.gc();
                if (!Settings.SQL_ENABLED && Settings.SQL_RETRY) {
                    Settings.SQL_ENABLED = true;
                    Logger.getGlobal().info("Executing clean memory task; turning SQL back on.");
                }
            } catch (final Throwable e) {
                Logger.getGlobal().info("Launcher", "Failed executing clean memory task...");
                cleanMemory(Runtime.getRuntime().freeMemory() < Settings.MINIMUM_RAM_ALLOCATED);
            }
        }, 60, 60, TimeUnit.MINUTES);
    }

    /**
     * Cleans out the useless shit.
     *
     * @param force If force clean.
     */
    public static void cleanMemory(final boolean force) {
        if (force) {
            ItemDefinitions.clearItemsDefinitions();
            ObjectDefinitions.clearObjectDefinitions();
        }
        for (final Index index : Cache.STORE.getIndexes()) {
            index.resetCachedFiles();
        }
        System.gc();
    }

    /**
     * Shuts down networking.
     */
    public static void closeServices() {
        CoresManager.shutdown();
    }

    /**
     * Saves all data.
     *
     * @throws Exception if Failed.
     */
    private static void saveFiles() {
        for (final Player player : World.getPlayers()) {
            if (player == null || !player.isActive() || player.hasFinished() || player.rejectSave) {
                continue;
            }
            SerializableFilesManager.savePlayer(player);
        }
        if (!Settings.DEBUG && !Settings.TEST_SERVER_MODE) {
            GrandExchange.save();
            IPBanL.save();
            DeathStatistics.getInstance().save();
            IPMute.save();
            DisplayNames.save();
            Lottery.getSingleton().save();
            SkillingContractTracker.getSingleton().save();
            TelosEnrageRanks.save();
            EliteDungeon.save();
        }
    }

    /**
     * Restarts the server.
     */
    public static void restartEmulator() {
        closeServices();
        System.gc();
        try {
            // Runtime.getRuntime().exec("java -XX:-OmitStackTraceInFastThrow
            // -XX:+AggressiveHeap -cp
            // bin;data/libs/netty-3.5.2.Final.jar;data/libs/FileStore.jar;data/libs/com.rs.utils.mysql-connector-java-5.1.18-bin.jar;data/libs/gson-2.2.4.jar;data/libs/json-simple-1.1.1.jar;data/libs/commons-io-2.4.jar
            // com.rs.ServerLauncher true false true 43595 1");
            Runtime.getRuntime().exec("rsps");
            System.exit(0);
        } catch (final Throwable e) {
            Logger.getGlobal().catching(e);
        }
    }

    /**
     * Shuts down the server.
     */
    public static void shutdown() {
        try {
            closeServices();
        } finally {
            System.exit(0);
        }
    }
}
