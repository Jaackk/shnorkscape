package com.rs.game;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.ImmutableSet;
import com.rs.ServerLauncher;
import com.rs.Settings;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.ObjectDefinitions;
import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.activites.GodWarsBosses;
import com.rs.game.activites.PuroPuro;
import com.rs.game.activites.ZarosGodwars;
import com.rs.game.activites.clanwars.FfaZone;
import com.rs.game.activites.clanwars.RequestController;
import com.rs.game.activites.duel.DuelArena;
import com.rs.game.activites.soulwars.SoulWarsManager;
import com.rs.game.activites.worldevents.ShootingStar;
import com.rs.game.activities.dfm.DemonFlashMobs;
import com.rs.game.activities.instances.Instance;
import com.rs.game.activities.snowball.game.SnowballFightGame;
import com.rs.game.activities.snowball.lobby.SnowballLobby;
import com.rs.game.activities.wildywyrm.WildyWyrm;
import com.rs.game.item.Item;
import com.rs.game.item.floor.FloorItem;
import com.rs.game.map.bossInstance.impl.SpiderBossInstance;
import com.rs.game.map.bossInstance.impl.VoragoInstance;
import com.rs.game.npc.Ed3boss1.Crassianscout;
import com.rs.game.npc.Ed3boss1.Ed3boss1;
import com.rs.game.npc.NPC;
import com.rs.game.npc.Trex.Trex;
import com.rs.game.npc.airut.Airut;
import com.rs.game.npc.camelwarrior.CamelWarrior;
import com.rs.game.npc.corp.CorporealBeast;
import com.rs.game.npc.solak.Solak;
import com.rs.game.npc.vinecrawler.Vinecrawler;
import com.rs.game.npc.mossgolem.Mossgolem;
import com.rs.game.npc.bulbouscrawler.Bulbouscrawler;
import com.rs.game.npc.dragons.CelestialDragonB;
import com.rs.game.npc.dragons.KingBlackDragon;
import com.rs.game.npc.dragons.RuneDragon;
import com.rs.game.npc.eds.EliteDungeonBoss;
import com.rs.game.npc.glacor.Glacor;
import com.rs.game.npc.godwars.GodWarMinion;
import com.rs.game.npc.godwars.armadyl.GodwarsArmadylFaction;
import com.rs.game.npc.godwars.armadyl.KreeArra;
import com.rs.game.npc.godwars.bandos.GeneralGraardor;
import com.rs.game.npc.godwars.bandos.GodwarsBandosFaction;
import com.rs.game.npc.godwars.saradomin.CommanderZilyana;
import com.rs.game.npc.godwars.saradomin.GodwarsSaradominFaction;
import com.rs.game.npc.godwars.zammorak.GodwarsZammorakFaction;
import com.rs.game.npc.godwars.zammorak.KrilTsutsaroth;
import com.rs.game.npc.godwars.zaros.GodwarsZarosFaction;
import com.rs.game.npc.godwars.zaros.Nex;
import com.rs.game.npc.godwars.zaros.NexMinion;
import com.rs.game.npc.gwd2.HoGChosen;
import com.rs.game.npc.gwd2.SerenFaction;
import com.rs.game.npc.gwd2.SliskeFaction;
import com.rs.game.npc.gwd2.ZamorakFaction;
import com.rs.game.npc.gwd2.ZarosFaction;
import com.rs.game.npc.gwd2.helwyr.CywirAlpha;
import com.rs.game.npc.gwd2.helwyr.Helwyr;
import com.rs.game.npc.gwd2.twinfuries.Avaryss;
import com.rs.game.npc.gwd2.twinfuries.Nymora;
import com.rs.game.npc.gwd2.vindicta.Gorvek;
import com.rs.game.npc.gwd2.vindicta.Vindicta;
import com.rs.game.npc.kalgerion.Kalgerion;
import com.rs.game.npc.kalphite.KalphiteQueen;
import com.rs.game.npc.nomad.FlameVortex;
import com.rs.game.npc.nomad.Nomad;
import com.rs.game.npc.others.AcheronMammoth;
import com.rs.game.npc.others.Automaton;
import com.rs.game.npc.others.BarrowsCryptNPC;
import com.rs.game.npc.others.Bork;
import com.rs.game.npc.others.CrystalShapeShifter;
import com.rs.game.npc.others.DagannothKing;
import com.rs.game.npc.others.DonatorRunesphere;
import com.rs.game.npc.others.GemstoneDragon;
import com.rs.game.npc.others.HunterTrapNPC;
import com.rs.game.npc.others.Legios;
import com.rs.game.npc.others.LivingRock;
import com.rs.game.npc.others.Lucien;
import com.rs.game.npc.others.Revenant;
import com.rs.game.npc.others.Sheep;
import com.rs.game.npc.others.StarSprite;
import com.rs.game.npc.others.TormentedDemon;
import com.rs.game.npc.others.WildyWyrmNPC;
import com.rs.game.npc.slayer.RipperDemon;
import com.rs.game.npc.slayer.Strykewyrm;
import com.rs.game.npc.sorgar.Elemental;
import com.rs.game.npc.telos.Telos;
import com.rs.game.npc.xmas.SmallSnowman;
import com.rs.game.player.ActionBar;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.agility.SerenityPostsHandler;
import com.rs.game.player.actions.divination.Wisp;
import com.rs.game.player.actions.divination.WispInfo;
import com.rs.game.player.actions.hunter.TrapAction.HunterNPC;
import com.rs.game.player.actions.slayer.sophanemdungeon.SophanemSlayerNPC;
import com.rs.game.player.content.ItemConstants;
import com.rs.game.player.content.LivingRockCavern;
import com.rs.game.player.content.OwnedObjectManager;
import com.rs.game.player.content.StaffActivityMonitor;
import com.rs.game.player.content.TaskTab;
import com.rs.game.player.content.TriviaBot;
import com.rs.game.player.content.VoteManager;
import com.rs.game.player.content.WellOfGoodWill;
import com.rs.game.player.content.artisansworkshop.ArtisansWorkShop;
import com.rs.game.player.content.barrows.Barrows;
import com.rs.game.player.content.dungeoneering.DungeonPartyManager;
import com.rs.game.player.content.easter.EasterBunnyNPC;
import com.rs.game.player.content.easter.SickEasterBunnyNPC;
import com.rs.game.player.content.eds.EliteDungeon;
import com.rs.game.player.content.fistofguthix.FOGManager;
import com.rs.game.player.content.grandExchange.GrandExchange;
import com.rs.game.player.content.maxguild.combatportal.BossPortal;
import com.rs.game.player.content.ports.JohnStrum;
import com.rs.game.player.controllers.AraxxorHiveControler;
import com.rs.game.player.controllers.ArtisansWorkShopControler;
import com.rs.game.player.controllers.EliteDungeonsLobby;
import com.rs.game.player.controllers.Kalaboss;
import com.rs.game.player.controllers.WarriorsGuild;
import com.rs.game.player.controllers.Wilderness;
import com.rs.game.player.controllers.bossInstance.SpiderBossInstanceController;
import com.rs.game.player.dialogue.impl.StarterTutorialD;
import com.rs.game.route.Flags;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.network.packet.impl.ButtonHandler;
import com.rs.utils.AntiFlood;
import com.rs.utils.Colors;
import com.rs.utils.DTRank;
import com.rs.utils.IPBanL;
import com.rs.utils.IPMute;
import com.rs.utils.LmsKillsHiscores;
import com.rs.utils.LmsVictoriesHiscores;
import com.rs.utils.Logger;
import com.rs.utils.LoggingSystem;
import com.rs.utils.MACBan;
import com.rs.utils.PkRank;
import com.rs.utils.Utils;
import com.rs.utils.VoteHiscores;
import com.rs.utils.data.parsers.misc.ShopsDataParser;

import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps.SynchronizedMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

/**
 * A class which handles every World event & interactions.
 *
 * @author Noel
 */
public final class World {

    /**
     * A list holding all Player-Entities online.
     */
    private static final EntityList<Player> players = new EntityList<Player>(Settings.SV_PLAYERS_LIMIT, true);

    /**
     * A list holding all NPC-Entities online.
     */
    private static final EntityList<NPC> npcs = new EntityList<NPC>(Settings.SV_NPCS_LIMIT, false);

    /**
     * A map containing all Regions as Integers.
     */
    private static final SynchronizedMap<Region> regions = (SynchronizedMap<Region>) Int2ObjectMaps.synchronize(new Int2ObjectOpenHashMap<Region>(1000));
    public static int exiting_delay;
    public static long exiting_start;
    /**
     * Long representing the current World time in ticks.
     */
    public static long currentTime;
    /**
     * A String[] containing all Colors used for Server messages.
     */
    public static String[] colors = {"3399FF", "00CC99", "003399", "0099CC", "993300", "993333", "9900FF", "7D1616", "CC3300", "00FF00", "3399FF"};
    /**
     * We represent the Soul Wars mini-game as static.
     */
    public static SoulWarsManager soulWars;
    public static boolean pcGame;

    public static boolean activeStar = false;

  
    /**
     * An arraylist containing all available godwars instances
     */
    private static final List<Instance> INSTANCES = Collections.synchronizedList(new ArrayList<Instance>());



    /**
     * This should reeeaaaaaallllllyyyyy be changed, as said by previous comment.
     * #theAtaraxiaWay
     */
    @Getter
    @Setter
    private static SnowballFightGame snowballFightGame;

    @Getter
    private static SnowballLobby snowballLobby;

    public static List<Instance> getInstances() {
        return INSTANCES;
    }

    public static void addInstance(final Instance instance) {
        synchronized (INSTANCES) {
            INSTANCES.add(instance);
        }
    }

    public static boolean removeInstance(final Instance instance) {
        synchronized (INSTANCES) {
            return INSTANCES.remove(instance);
        }
    }

    /**
     * A String[] containing all World random messages.
     */
    private static final String[] messageString = {"You can do co-op skilling contracts by using a skilling backpack/gem on another player!",
            "Please report all bugs; glitches & missing content on our forum.",
            "Type ::ticket in the chatbox if you need assistance from staff.",
            "Trade in some of your items at ;;market to receive powerful equipment!",
            "Keep your account secure! Use ;;lock to require your be PIN required for account access.",
            "Keep your account secure! Use a PIN that's hard to guess.",
            "You can talk to the Skilling master at ;;home to get a Skilling contract!",
            "After every 24h of server time, the player who completes the most skilling contracts will be rewarded!",
            "Talk to the Skilling master at ;;home to get a Skilling contract for experience and rewards!",
            "You can spend your Trivia points by talking to the Wise Old Man.",
            "Use the Grand Exchange if you can't find an item you're looking for.",
            "You can spend your Vote points by trading Party Pete.", "Join our ::discord channel for voice/text chat; music & events.",
            "Confused? Type ;;starterguide for a comprehensive starter guide!",
            "Type ;;events to check the Event schedule for the Month!",
            "Remember to ::Vote every 12 hours - receive awesome rewards.",
            "You can add tools to your toolbelt to save inventory space.",
            "You can toggle World/Yell messages in your Account Manager.",
            "You can change your Loyalty Title in your Account Manager.",
            "You can use the Altar at home to restore your Prayer and switch Spellbook or Prayer book!",
            "You can use the Altar at home to restore your Prayer and switch Spellbook or Prayer book!",
            "You can use ;;rswiki queryhere to automatically search for a piece of content on the Wiki!",
            "You can use ;;rswiki queryhere to automatically search for a piece of content on the Wiki!",
            "Type ;;settings to access a wide range of options to change your gameplay experience!",
            "Type ;;settings to access a wide range of options to change your gameplay experience!",
            "Type ;;settings to access a wide range of options to change your gameplay experience!",
            "Ataraxia coins can be spent by typing ;;cosmetics and purchasing cosmetic overrides!",
            "Ataraxia coins can be spent by typing ;;cosmetics and purchasing cosmetic overrides!",
            "Join our ::discord channel for voice/text chat; music & events.",
            "Donation ranks are given when spent over 20$ in total.",
            "Type ::commands to see a list of all available commands.",
            "Read up on some quick basic help by typing ::help.",
            "You can reset your Slayer tasks from Kuradal's reward shop.",
            "Trade in some of your items at ;;market to receive amazing items!",
            "Every weekend is double xp and minigame weekend.",
            "You can quickly open the Titles manager interface by typing ::titles.",
            "Type ::ticket in the chatbox if you need assistance from staff.",
            "Join our ::discord channel for voice/text chat; music & events.",
            "You can zoom in and out by scrolling your mouse wheel while holding ctrl.",
            "You can change your Loot Beam settings in your Account Manager.",
            "Type ::achievements to view your Achievement progress & rewards.",
            "Type ::achievements to see all you need to achieve the Master Quest Cape!",
            "Use the Grand Exchange if you can't find an item you're looking for.",
            "Type ::voted to claim your vote rewards (if you voted successfully).",
            "You can access the Prifddinas city with a total level of at least 2250.",
            "Not sure what Hybrid tokens are? Check out ::thread 823.",
            "Not sure what Hybrid tokens are? Check out ::thread 823.",
            "Check out ::sp, easy way to access Skilling Teleports!",
            "Check out ::sp, easy way to access Skilling Teleports!",
            "Most items can be auto-bought on the Grand Exchange with a +5% price.",
            "You can use coins on the well at home for 2 hours of 1.5x experience.",
            "You can view all your currently active game perks by typing '::perks'.",
            "The QBD has been made easier and more profitable for mid-tier players. Try it out!",
            "Join our ::discord channel for voice/text chat; music & events.",
            "There's a Vote Party every 100 total server votes. You must vote to receive rewards!",
            "Report a bug in game with ;;bug [MESSAGE]!", "Suggest a new feature for the server with ;;suggest [MESSAGE]!"};

    /**
     * Well of Goodwill.
     */
    private static int wellAmount;
    private static boolean wellActive;

    /**
     * Last player who has successfully voted.
     */
    private static String lastVoter;

    /**
     * Starts all World activites.
     */
    public static final void init() {
        addDrainPrayerTask();
        addRestoreHitPointsTask();
        addRestoreSkillsTask();
        addRestoreSpecialAttackTask();
        addSummoningEffectTask();
        addOwnedObjectsTask();
        addRotaionChangeTask();
        LivingRockCavern.init();
        addRestoreShopItemsTask();
        addArtisansWorkShopProcessTask();
        messageEvent();
        WarriorsGuild.init();
        DemonFlashMobs.init();
        ShootingStar.init();
        WildyWyrm.InitiateWyrmSpawningSequence();
        soulWars = new SoulWarsManager();
        soulWars.start();
        snowballLobby = new SnowballLobby();
        addTriviaBotTask();
        PuroPuro.initPuroImplings();
        SerenityPostsHandler.initSerenPosts();
        ActionBar.addActionBarTask();
        addRefreshTargetBuffsTask();
    }
    
    /**
     * Queues only the six {@link WorldTasksManager}-based core tasks of
     * {@link #init()} (prayer drain, hitpoint / skill / special-attack restore,
     * owned objects, shop restock) and nothing else: no activity initialisers,
     * no {@code CoresManager}-scheduled task. This is the subset the native 947
     * bootstrap ({@code Native950Bootstrap}) may run, exposed so it no longer
     * has to reach the private {@code add*Task} methods reflectively. The
     * legacy launcher keeps calling {@link #init()}; behaviour is unchanged.
     *
     * <p>The bootstrap gates the two prayer-touching tasks per milestone, so it
     * calls the individual {@code queue*Task} wrappers rather than this method.
     */
    public static void initCoreTasks() {
        addDrainPrayerTask();
        addRestoreHitPointsTask();
        addRestoreSkillsTask();
        addRestoreSpecialAttackTask();
        addOwnedObjectsTask();
        addRestoreShopItemsTask();
    }

    /** Public wrapper for {@link #addDrainPrayerTask()}; dereferences {@code player.getPrayer()} every tick. */
    public static void queueDrainPrayerTask() { addDrainPrayerTask(); }

    /** Public wrapper for {@link #addRestoreHitPointsTask()}. */
    public static void queueRestoreHitPointsTask() { addRestoreHitPointsTask(); }

    /** Public wrapper for {@link #addRestoreSkillsTask()}; dereferences {@code player.getPrayer()} every 100 ticks. */
    public static void queueRestoreSkillsTask() { addRestoreSkillsTask(); }

    /** Public wrapper for {@link #addRestoreSpecialAttackTask()}. */
    public static void queueRestoreSpecialAttackTask() { addRestoreSpecialAttackTask(); }

    /** Public wrapper for {@link #addOwnedObjectsTask()}. */
    public static void queueOwnedObjectsTask() { addOwnedObjectsTask(); }

    /** Public wrapper for {@link #addRestoreShopItemsTask()}. */
    public static void queueRestoreShopItemsTask() { addRestoreShopItemsTask(); }

    public static void addRefreshTargetBuffsTask() {
        CoresManager.getServiceProvider().scheduleRepeatingTask(new Runnable() {

            @Override
            public void run() {
                try {
                    for (Player player : getPlayers()) {
                        if (player == null || !player.isActive() || player.hasFinished())
                            continue;
                        player.getCombatDefinitions().refreshTargetBuffs();
                        TaskTab.sendTab(player);
                    }
                } catch (Throwable e) {
                    Logger.getGlobal().catching(e);
                }
            }

        }, 0, 600, TimeUnit.MILLISECONDS);
    }
    

    private static void addArtisansWorkShopProcessTask() {
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                ArtisansWorkShop.processArtisansWorkShop();
            }
        }, 100);
    }

    /**
     * Handles informative random world messages.
     */
    private static void messageEvent() {

    }

    private static void addTriviaBotTask() {
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                try {
                    TriviaBot.run();
                } catch (final Throwable e) {
                    Logger.getGlobal().catching(e);
                }
            }
        }, 1000, Utils.random(500, 1000));

    }

    private static void addOwnedObjectsTask() {
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                try {
                    OwnedObjectManager.processAll();
                    if (isWellActive() && WellOfGoodWill.taskTime > 0 && !Settings.DEBUG) {
                        WellOfGoodWill.taskTime--;
                    }
                } catch (final Throwable e) {
                    Logger.getGlobal().catching(e);
                }
            }
        }, 1, 2);
    }

    private static void addRestoreShopItemsTask() {
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                try {
                    ShopsDataParser.restoreShops();
                } catch (final Throwable e) {
                    Logger.getGlobal().catching(e);
                }
            }
        }, 1, 50);
    }

    private static final void addSummoningEffectTask() {
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                try {
                    for (final Player player : getPlayers()) {
                        if (player == null || player.getFamiliar() == null || player.isDead() || !player.hasFinished()) {
                            continue;
                        }
                        if (player.getFamiliar().getOriginalId() == 6814) {
                            player.heal(20);
                            player.setNextGraphics(new Graphics(1507));
                        }
                    }
                } catch (final Throwable e) {
                    Logger.getGlobal().catching(e);
                }
            }
        }, 1, 25);
    }

    private static final void addRestoreSpecialAttackTask() {
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                try {
                    for (final Player player : getPlayers()) {
                        if (player == null || player.isDead() || !player.isRunning()) {
                            continue;
                        }
                        player.getCombatDefinitions().restoreSpecialAttack();
                    }

                } catch (final Throwable e) {
                    Logger.getGlobal().catching(e);
                }
            }
        }, 1, 50);
    }

    private static void addDrainPrayerTask() {
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                try {
                    for (final Player player : getPlayers()) {
                        if (player == null || player.isDead() || !player.isRunning()) {
                            continue;
                        }
                        player.getPrayer().processPrayerDrain();
                    }
                } catch (final Throwable e) {
                    Logger.getGlobal().catching(e);
                }
            }

        }, 0, 0);
    }

    private static void addRestoreHitPointsTask() {
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                try {
                    for (final Player player : getPlayers()) {
                        if (player == null || player.isDead() || !player.isRunning() || player.getControlerManager().getControler() instanceof DuelArena || !player.isAcceptingAid()) {
                            continue;
                        }
                        player.restoreHitPoints();
                    }
                    for (final NPC npc : npcs) {
                        if (npc == null || npc.isDead() || npc.hasFinished() || npc.getId() >= 16726 && npc.getId() <= 16732) {
                            continue;
                        }
                        npc.restoreHitPoints();
                    }
                } catch (final Throwable e) {
                    Logger.getGlobal().catching(e);
                }
            }
        }, 1, 10);
    }

    private static void addRestoreSkillsTask() {
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                try {
                    for (final Player player : getPlayers()) {
                        if (player == null || !player.isRunning()) {
                            continue;
                        }
                        int ammountTimes = player.getPrayer().usingPrayer(0, 7) ? 2 : 1;
                        if (player.isResting()) {
                            ammountTimes += 1;
                        }
                        final boolean berserker = player.getPrayer().usingPrayer(1, 9);
                        b:
                        // The bound was a literal 26, so it excluded Invention and, once the
                        // 947 cache widened the model to 29 stats, Archaeology and Necromancy
                        // too - a boost or drain on those never decayed. It now walks the whole
                        // model. Invention keeps its old exclusion explicitly so the behaviour
                        // of stats 0..26 is unchanged; only 27 and 28 gain restoration.
                        for (int skill = 0; skill < Skills.SKILL_COUNT; skill++) {
                            if (skill == Skills.SUMMONING || skill == Skills.INVENTION) {
                                continue b;
                            }
                            c:
                            for (int time = 0; time < ammountTimes; time++) {
                                final int currentLevel = player.getSkills().getLevel(skill);
                                final int normalLevel = player.getSkills().getLevelForXp(skill);
                                if (currentLevel > normalLevel && time == 0) {
                                    if (skill == Skills.ATTACK || skill == Skills.STRENGTH || skill == Skills.DEFENCE || skill == Skills.RANGE || skill == Skills.MAGIC) {
                                        if (berserker && Utils.random(100) <= 15) {
                                            continue c;
                                        }
                                    }
                                    player.getSkills().set(skill, currentLevel - 1);
                                } else if (currentLevel < normalLevel) {
                                    player.getSkills().set(skill, currentLevel + 1);
                                } else {
                                    break c;
                                }
                            }
                        }
                    }
                } catch (final Throwable e) {
                    Logger.getGlobal().catching(e);
                }
            }
        }, 1, 100);
    }

    public static final Map<Integer, Region> getRegions() {
        return regions;
    }

    public static final Region getRegion(final int id) {
        return getRegion(id, false);
    }

    public static final Region getRegion(final int id, final boolean load) {
        Region region = regions.get(id);
        if (region == null) {
            region = new Region(id);
            regions.put(id, region);
        }
        if (load) {
            region.checkLoadMap();
        }
        return region;
    }

    public static final void addNPC(final NPC npc) {
        // Legacy registration also attaches dynamic-area ownership. Refuse before either
        // mutation: a failed legacy encounter must not poison the next native login.
        if (CoresManager.isNative950())
            throw new IllegalStateException("Legacy NPC registration is unavailable in the native 950 world");
        npcs.add(npc);
        if(npc != null) {
        DynamicArea area =  DynamicArea.getDynamicArea(npc.getRegionId());
        if (area != null)
            area.addNPC(npc);
        }
    }

    public static final void removeNPC(final NPC npc) {

        npcs.remove(npc);
    }

    /**
     * P6: registers one world-owned native 947 NPC. The list may already hold other
     * native NPCs; it may never hold a legacy one, because legacy NPCs bring the
     * respawn, combat and region tasks the native JVM does not run.
     *
     * <p>{@code EntityList} is the single index authority: the assigned index is what
     * {@code NPC_INFO} reports, and its 16-bit field reserves 65535 as the addition
     * terminator (verified/NPC_INFO.md, "Adding one stationary NPC").
     */
    public static void addNative950Npc(final NPC npc) {
        if (npc == null || !npc.isNative950())
            throw new IllegalArgumentException("Expected a native 947 NPC");
        if (npc.getIndex() != 0 || npc.hasFinished())
            throw new IllegalStateException("Native 947 NPCs enter the world exactly once, as fresh unregistered entities");
        for (final NPC other : npcs)
            if (other != null && !other.isNative950())
                throw new IllegalStateException("The modern world must not share a legacy NPC loop");
        if (npcs.size() >= NATIVE_947_MAX_NPC_INDEX)
            throw new IllegalStateException("Native 947 NPC_INFO indices must stay below the 65535 addition terminator");
        npcs.add(npc);
        if (npc.getIndex() < 1 || npc.getIndex() > NATIVE_947_MAX_NPC_INDEX) {
            npcs.remove(npc);
            npc.setIndex(0);
            throw new IllegalStateException("Native 947 NPC index does not fit the verified 16-bit NPC_INFO field");
        }
    }

    /** Highest NPC index NPC_INFO can carry; 65535 is its addition terminator. */
    public static final int NATIVE_947_MAX_NPC_INDEX = 65534;

    /** Highest player index PLAYER_INFO can carry (slots 1..2047). */
    public static final int NATIVE_947_MAX_PLAYER_INDEX = 2047;

    /** Removes local NPC state without legacy respawn, combat or region hooks. */
    public static void removeNative950Npc(final NPC npc) {
        if (npc == null || !npc.isNative950())
            throw new IllegalArgumentException("Expected a native 947 NPC");
        if (npcs.contains(npc)) {
            if (npc.getLastRegionId() >= 0)
                getRegion(npc.getLastRegionId()).removeNPCIndex(npc.getIndex());
            npcs.remove(npc);
        }
        npc.setLastRegionId(-1);
        npc.setIndex(0);
        npc.setFinished(true);
        npc.resetWalkSteps();
    }

    public static boolean containsNPC(final NPC npc) {
        return npcs.contains(npc);
    }

    public static final NPC spawnNPC(final int id, final WorldTile tile, final int mapAreaNameHash, final boolean canBeAttackFromOutOfArea, final boolean spawned) {
        NPC n = null;

        HunterNPC hunterNPC = HunterNPC.ENTITIES.get(id);
        if (hunterNPC != null) {
            n = new HunterTrapNPC(hunterNPC, id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
            return n;
        }

        if (id >= 2031 && id <= 2037) {
            n = new BarrowsCryptNPC(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        } else if (id >= 24170 && id <= 24172) {
            n = new GemstoneDragon(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
        } else if (id >= 22433 && id <= 22436) {
            n = new HoGChosen(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
        } else if (id == 340) {
            n = new NPC(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        } else if (id == 3334) {
            n = new WildyWyrmNPC(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        } else if (id >= 5533 && id <= 5558) {
            n = new Elemental(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        } else if (id == 43 || id >= 5156 && id <= 5164 || id == 1765) {
            n = new Sheep(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
        } else if (id == 14301) {
            n = new Glacor(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
        } else if (id == 22001) {
            n = new CamelWarrior(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
        } else if (id == 9441) {
            n = new FlameVortex(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        } else if (id >= 8832 && id <= 8834) {
            n = new LivingRock(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        } else if (id >= 13465 && id <= 13481) {
            n = new Revenant(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        } else if (id == 1158 || id == 1160) {
            n = new KalphiteQueen(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        } else if (id >= 8528 && id <= 8532) {
            n = new Nomad(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        } else if (id == 15753) {
            n = new EasterBunnyNPC(id, tile);
        } else if (id == 15754) {
            n = new SickEasterBunnyNPC(id, tile);
        } else if (id == 8091) {
            n = new StarSprite();
        } else if (id == 14973 || id == 14974 || id == 14975 || id == 14976 || id == 14977) {
            n = new Kalgerion(id, tile);
        } else if (id == 19109) {
            n = new CelestialDragonB(id, tile);
        } else if (id == 16907 || id == 16906 || id == 16905) {
            n = new Automaton(id, tile);
        } else if (id == 21994) {
            n = new RipperDemon(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        } else if (id >= 21630 && id <= 21632) {
            n = new CrystalShapeShifter(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        } else if (id == 22007) {
            n = new AcheronMammoth(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        } else if (id == 22455) {
            n = new Avaryss(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned, null);
        } else if (id == 22456) {
            n = new Nymora(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned, null);
        } else if (id == 22459 || id == 22460) {
            n = new Vindicta(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned, null);
        } else if (id == 22463) {
            n = new Gorvek(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        } else if (id == 22438) {
            n = new Helwyr(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned, null);
        } else if (id == 22439) {
            n = new CywirAlpha(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        } else if (id >= 22470 && id <= 22475) {
            n = new SerenFaction(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        } else if (id >= 22478 && id <= 22484) {
            n = new SliskeFaction(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        } else if (id >= 22486 && id <= 22494) {
            n = new ZamorakFaction(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        } else if (id >= 22496 && id <= 22501) {
            n = new ZarosFaction(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        } else if (id >= 6210 && id <= 6221) {
            n = new GodwarsZammorakFaction(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        } else if (id >= 6254 && id <= 6259) {
            n = new GodwarsSaradominFaction(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        } else if (id >= 6229 && id <= 6246) {
            n = new GodwarsArmadylFaction(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        } else if (id >= 13456 && id <= 13459) {
            n = new GodwarsZarosFaction(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        } else if (id >= 6268 && id <= 6283) {
            n = new GodwarsBandosFaction(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        } else if (id == 6261 || id == 6263 || id == 6265) {
            n = GodWarsBosses.graardorMinions[(id - 6261) / 2] = new GodWarMinion(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        } else if (id == 6260) {
            n = new GeneralGraardor(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned, false);

        } else if (id == 2269 || id == 2271 || id == 2272) {
            n = new SmallSnowman(id, tile);
        } else if (id == 6222) {
            n = new KreeArra(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned, false);
        } else if (id == 6223 || id == 6225 || id == 6227) {
            n = GodWarsBosses.armadylMinions[(id - 6223) / 2] = new GodWarMinion(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        } else if (id == 6203) {
            n = new KrilTsutsaroth(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned, false);
        } else if (id == 6204 || id == 6206 || id == 6208) {
            n = GodWarsBosses.zamorakMinions[(id - 6204) / 2] = new GodWarMinion(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        } else if (id >= 24592 && id <= 24606) {
            n = new SophanemSlayerNPC(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
        } else if (id == 50 || id == 2642) {
            n = new KingBlackDragon(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        } else if (id >= 9462 && id <= 9467 || id == 2417 || id == 20630) {
            n = new Strykewyrm(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
        } else if (id == 6248 || id == 6250 || id == 6252) {
            n = GodWarsBosses.commanderMinions[(id - 6248) / 2] = new GodWarMinion(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        } else if (id == 6247) {
            n = new CommanderZilyana(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned, false);
        } else if (id == 8133) {
            n = new CorporealBeast(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        }
        else if (id == 25513) {
            n = new Solak(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        }
        else if (id == 26435) {
            n = new Trex(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        }
        else if (id == 26050) {
            n = new Ed3boss1(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        }
         else if (id == 26149 || id == 26157 ||  id == 26154 || id == 26144 || id == 26175 ||  id == 26158 ||  id == 26163
        || id == 26165 || id == 26170 || id == 26164) {
            n = new Crassianscout(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        }

         else if (id == 24830) {
            n = new Vinecrawler(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        }
         else if (id == 24832) {
            n = new Mossgolem(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        }
        else if (id == 24831) {
            n = new Bulbouscrawler(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        }
         else if (id == 13447) {
            n = ZarosGodwars.nex = new Nex(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, null);
        } else if (id == 13451) {
            n = new NexMinion(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        } else if (id == 13452) {
            n = new NexMinion(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        } else if (id == 13453) {
            n = new NexMinion(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        } else if (id == 13454) {
            n = new NexMinion(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        } else if (id == 14256) {
            n = new Lucien(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        } else if (id == 8349 || id == 8351 || id == 8450 || id == 8451) {
            n = new TormentedDemon(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        } else if (WispInfo.forNpcId(id) != null) {
            n = new Wisp(id, tile);
        } else if (id >= 2881 && id <= 2883) {
            n = new DagannothKing(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned, null);
        } else if (id == 21136) {
            n = new RuneDragon(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        } else if (id == 16554) {
            n = new JohnStrum(id, tile);
        } else if (id == 18621 || id == 18622) {
            n = new Airut(id, tile);
        } else if (id == 15440) {
            n = new DonatorRunesphere(id, tile);
        } else {
            n = new NPC(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        }
        return n;
    }

    public static final void spawnTemporaryObjectWithReplacement(final WorldObject primary, final WorldObject replacement, final int time) {
        spawnObject(primary);
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                try {
                    spawnObject(replacement);
                } catch (final Throwable e) {
                    Logger.getGlobal().catching(e);
                }
            }
        }, time);
    }

    public static final NPC spawnNPC(final NPC npc) {
        return spawnNPC(npc.getId(), npc, npc.getMapAreaNameHash(), npc.canBeAttackFromOutOfArea(), false);
    }

    public static final NPC spawnNPC(final int id, final WorldTile tile, final int mapAreaNameHash, final boolean canBeAttackFromOutOfArea) {
        return spawnNPC(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, false);
    }

    /*
     * check if the entity region changed because moved or teled then we update it
     */
    public static final void updateEntityRegion(final Entity entity) {
        final int regionId = entity.getRegionId();
        if (entity instanceof NPC && ((NPC) entity).isNative950()) {
            if (!npcs.contains((NPC) entity) || entity.hasFinished())
                throw new IllegalStateException("Native 947 NPC must be registered before updating its region");
            if (entity.getLastRegionId() != regionId) {
                if (entity.getLastRegionId() >= 0)
                    getRegion(entity.getLastRegionId()).removeNPCIndex(entity.getIndex());
                getRegion(regionId).addNPCIndex(entity.getIndex());
                entity.setLastRegionId(regionId);
            }
            return;
        }
        if (entity instanceof Player && ((Player) entity).isNative950()) {
            if (entity.getLastRegionId() != regionId) {
                if (entity.getLastRegionId() > 0)
                    getRegion(entity.getLastRegionId()).removePlayerIndex(entity.getIndex());
                getRegion(regionId, true).addPlayerIndex(entity.getIndex());
                entity.setLastRegionId(regionId);
            }
            return;
        }
        if (entity.getLastRegionId() != regionId) { // map region entity at
            // changed
            if (entity instanceof Player) {
                if (entity.getLastRegionId() > 0) {
                    getRegion(entity.getLastRegionId()).removePlayerIndex(entity.getIndex());
                }
                final Region region = getRegion(regionId);
                region.addPlayerIndex(entity.getIndex());
                final Player player = (Player) entity;
                final int musicId = region.getRandomMusicId();
                if (musicId != -1) {
                    player.getMusicsManager().checkMusic(musicId);
                }
                if (regionId == BossPortal.REGION_ID) { // Spawns the combat portal as something always removes it
                    WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                            BossPortal.spawn(player);
                            stop();
                        }
                    });
                }
                player.getControlerManager().moved();
                StarterTutorialD.checkStarterArea(player);
                if (player.isActive()) {
                    checkControlersAtMove(player);
                }
            } else {
                if (entity.getLastRegionId() > 0) {
                    getRegion(entity.getLastRegionId()).removeNPCIndex(entity.getIndex());
                }
                getRegion(regionId).addNPCIndex(entity.getIndex());
            }
            entity.checkMultiArea();
            entity.setLastRegionId(regionId);
        } else {
            if (entity instanceof Player) {
                final Player player = (Player) entity;
                if ((FOGManager.get().getFOGInstance().isAtCave(player) || FOGManager.get().getFOGInstance().isInLobby(player) || FOGManager.get().getFOGInstance().getTeam(player) != null)) {
                    return;
                }
                player.getControlerManager().moved();
                StarterTutorialD.checkStarterArea(player);
                if (player.isActive()) {
                    checkControlersAtMove(player);
                }
            }
            entity.checkMultiArea();
        }
    }

    public static void checkControlersAtMove(final Player player) {
        if (Wilderness.isAtDynamicPvP(player)) {
            player.setCanPvp(true);
        } else if (Wilderness.isAtDynamicPvP(player.getLastWorldTile())) {
            player.setCanPvp(false);
        }
        if (!(player.getControlerManager().getControler() instanceof RequestController) && RequestController.inWarRequest(player)) {
            player.getControlerManager().startControler("clan_wars_request");
        } else if (player.getControlerManager().getControler() == null && ArtisansWorkShopControler.isInsideArtisansShop(player)) {
            player.getControlerManager().startControler("ArtisansWorkShopControler");
        } else if (!(player.getControlerManager().getControler() instanceof Barrows) && Barrows.isAtBarrows(player)) {
            player.getControlerManager().startControler("Barrows");
        } else if (!(player.getControlerManager().getControler() instanceof Kalaboss) && Kalaboss.isAtKalaboss(player)) {
            player.getControlerManager().startControler("Kalaboss");
        } else if (!(player.getControlerManager().getControler() instanceof EliteDungeonsLobby) && EliteDungeonsLobby.isAtEliteDungeonLobbyAreas(player)) {
            player.getControlerManager().startControler("EliteDungeonsLobby");
        } else if (!(player.getControlerManager().getControler() instanceof AraxxorHiveControler) && !(player.getControlerManager().getControler() instanceof SpiderBossInstanceController) && AraxxorHiveControler.isAtAraxxorHive(player)) {
            player.getControlerManager().startControler("AraxxorHiveControler");
        } else if (FfaZone.inArea(player)) {
            player.getControlerManager().startControler("clan_wars_ffa");
        } else if (!(player.getControlerManager().getControler() instanceof Wilderness) && player.getX() > 2968 && player.getX() < 3369 && player.getY() > 3547 && player.getY() < 3989) {
            player.getControlerManager().startControler("Wilderness");
        } else if (!player.isApeAtoll()) {
            if (player.getEquipment().getWeaponId() == 4024) {
                ButtonHandler.sendRemove2(player, 3);
            }
        }
    }

    /*
     * checks clip
     */
    public static boolean canMoveNPC(final int plane, final int x, final int y, final int size) {
        for (int tileX = x; tileX < x + size; tileX++) {
            for (int tileY = y; tileY < y + size; tileY++) {
                if (getMask(plane, tileX, tileY) != 0) {
                    return false;
                }
            }
        }
        if (x == 2497 && y == 2716 && plane == 2) {
            return false;
        }
        return x != 2504 || y != 2678 || plane != 2;
    }

    public static boolean hasAnyInhabitance(final int plane, final int x, final int y, final int size) {
        for (Player player : getPlayers()) {
            if (player.getX() == x && player.getY() == y && player.getPlane() == plane) {
                return true;
            }
        }
        return !canMoveNPC(plane, x, y, size);
    }

    public static boolean hasAnyInhabitance(final WorldTile worldTile, final int size) {
        return hasAnyInhabitance(worldTile.getPlane(), worldTile.getX(), worldTile.getY(), size);
    }

    public static boolean canMoveNPC(final WorldTile tile, final int size) {
        final int x = tile.getX();
        final int y = tile.getY();
        final int plane = tile.getPlane();
        for (int tileX = x; tileX < x + size; tileX++) {
            for (int tileY = y; tileY < y + size; tileY++) {
                if (getMask(plane, tileX, tileY) != 0) {
                    return false;
                }
            }
        }
        return !(x == 2497 && y == 2716 && plane == 2) && !(x == 2504 && y == 2678 && plane == 2);
    }

    /**
     * Used in dungeoneering.
     */
    public static final FloorItem permanentlyAddGroundItem(final Item item, final WorldTile tile) {
        final FloorItem floorItem = new FloorItem(item, tile, null, false, false);
        final Region region = getRegion(tile.getRegionId());
        region.getGroundItemsSafe().add(floorItem);
        final int regionId = tile.getRegionId();
        for (final Player player : getPlayers()) {
            if (player == null || player.hasFinished() || player.getPlane() != tile.getPlane() || !player.getMapRegionsIds().contains(regionId) || !ItemConstants.isTradeable(item)) {
                continue;
            }
            player.getPackets().sendGroundItem(floorItem);
        }
        return floorItem;
    }

    public static void executeAfterLoadRegion(final int regionId, final Runnable event) {
        executeAfterLoadRegion(regionId, 0, event);
    }

    public static void executeAfterLoadRegion(final int regionId, final long startTime, final Runnable event) {
        executeAfterLoadRegion(regionId, startTime, 10000, event);
    }

    public static void executeAfterLoadRegion(final int regionId, final long startTime, final long expireTime, final Runnable event) {
        final long start = Utils.currentTimeMillis();
        World.getRegion(regionId, true); // forces check load if not loaded
        CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {

            @Override
            public boolean repeat() {
                try {
                    if (!World.isRegionLoaded(regionId) && Utils.currentTimeMillis() - start < expireTime) {
                        return true;
                    }
                    event.run();
                    return false;
                } catch (final Throwable e) {
                    Logger.getGlobal().catching(e);
                }
                return false;
            }

        }, startTime, 600, TimeUnit.MILLISECONDS);
    }


    public static final boolean removeGroundItem(final FloorItem floorItem) {
        final int regionId = floorItem.getTile().getRegionId();
        final Region region = getRegion(regionId);
        if (!removeStoredGroundItem(region, floorItem)) {
            return false;
        }
        for (final Player player : World.getPlayers()) {
            if (player == null || !player.isActive() || player.hasFinished() || !player.getMapRegionsIds().contains(regionId)) {
                continue;
            }
            player.getPackets().sendRemoveGroundItem(floorItem);
        }
        return true;
    }

    /*
     * checks clip
     */
    public static boolean isNotCliped(final int plane, final int x, final int y, final int size) {
        for (int tileX = x; tileX < x + size; tileX++) {
            for (int tileY = y; tileY < y + size; tileY++) {
                if ((getMask(plane, tileX, tileY) & 2097152) != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isNotCliped(WorldTile tile) {
        return isNotCliped(tile.getPlane(), tile.getX(), tile.getY(), 1);
    }

    public static void setMask(final int plane, final int x, final int y, final int mask) {
        final WorldTile tile = new WorldTile(x, y, plane);
        final int regionId = tile.getRegionId();
        final Region region = getRegion(regionId);
        if (region == null) {
            return;
        }
        final int baseLocalX = x - ((regionId >> 8) * 64);
        final int baseLocalY = y - ((regionId & 0xff) * 64);
        region.setMask(tile.getPlane(), baseLocalX, baseLocalY, mask);
    }

    public static void forceSetMask(int plane, int x, int y, int mask) {
        WorldTile tile = new WorldTile(x, y, plane);
        World.getRegion(tile.getRegionId()).forceSetMask(tile.getPlane(), tile.getXInRegion(), tile.getYInRegion(), mask);
    }

    public static int getRotation(final int plane, final int x, final int y) {
        final WorldTile tile = new WorldTile(x, y, plane);
        final int regionId = tile.getRegionId();
        final Region region = getRegion(regionId);
        if (region == null) {
            return 0;
        }
        // int baseLocalX = x - ((regionId >> 8) * 64);
        // int baseLocalY = y - ((regionId & 0xff) * 64);
        // return region.getRotation(tile.getPlane(), baseLocalX, baseLocalY);
        return 0;
    }

    /*
     * checks clip
     */
    public static boolean isRegionLoaded(final int regionId) {
        final Region region = getRegion(regionId);
        if (region == null) {
            return false;
        }
        return region.getLoadMapStage() == 2;
    }

    public static boolean isTileFree(final int plane, final int x, final int y, final int size) {
        for (int tileX = x; tileX < x + size; tileX++) {
            for (int tileY = y; tileY < y + size; tileY++) {
                if (!isFloorFree(plane, tileX, tileY) || !isWallsFree(plane, tileX, tileY)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isTileFree(final WorldTile tile, final int size) {
        for (int tileX = tile.getX(); tileX < tile.getX() + size; tileX++) {
            for (int tileY = tile.getPlane(); tileY < tile.getPlane() + size; tileY++) {
                if (!isFloorFree(tile.getPlane(), tileX, tileY) || !isWallsFree(tile.getPlane(), tileX, tileY)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isFloorFree(final int plane, final int x, final int y, final int size) {
        for (int tileX = x; tileX < x + size; tileX++) {
            for (int tileY = y; tileY < y + size; tileY++) {
                if (!isFloorFree(plane, tileX, tileY)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isFloorFree(final int plane, final int x, final int y) {
        return (getMask(plane, x, y) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ)) == 0;
    }

    public static boolean isWallsFree(final int plane, final int x, final int y) {
        return (getMask(plane, x, y) & (Flags.CORNEROBJ_NORTHEAST | Flags.CORNEROBJ_NORTHWEST | Flags.CORNEROBJ_SOUTHEAST | Flags.CORNEROBJ_SOUTHWEST | Flags.WALLOBJ_EAST | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST)) == 0;
    }

    public static int getMask(final int plane, final int x, final int y) {
        final Region region = getRegion(WorldTile.getRegionId(x, y));
        if (region == null) {
            return -1;
        }
        return region.getMask(plane, x & 0x3F, y & 0x3F);
    }

    private static int getClipedOnlyMask(final int plane, final int x, final int y) {
        final WorldTile tile = new WorldTile(x, y, plane);
        final Region region = getRegion(tile.getRegionId());
        if (region == null) {
            return -1;
        }
        return region.getMaskClipedOnly(tile.getPlane(), tile.getXInRegion(), tile.getYInRegion());
    }

    public static final boolean checkProjectileStep(final int plane, final int x, final int y, final int dir, final int size) {
        final int xOffset = Utils.DIRECTION_DELTA_X[dir];
        final int yOffset = Utils.DIRECTION_DELTA_Y[dir];
        /*
         * int rotation = getRotation(plane,x+xOffset,y+yOffset); if(rotation != 0) {
         * dir += rotation; if(dir >= Utils.DIRECTION_DELTA_X.length) dir = dir -
         * (Utils.DIRECTION_DELTA_X.length-1); xOffset = Utils.DIRECTION_DELTA_X[dir];
         * yOffset = Utils.DIRECTION_DELTA_Y[dir]; }
         */
        if (size == 1) {
            final int mask = getClipedOnlyMask(plane, x + Utils.DIRECTION_DELTA_X[dir], y + Utils.DIRECTION_DELTA_Y[dir]);
            if (xOffset == -1 && yOffset == 0) {
                return (mask & 0x42240000) == 0;
            }
            if (xOffset == 1 && yOffset == 0) {
                return (mask & 0x60240000) == 0;
            }
            if (xOffset == 0 && yOffset == -1) {
                return (mask & 0x40a40000) == 0;
            }
            if (xOffset == 0 && yOffset == 1) {
                return (mask & 0x48240000) == 0;
            }
            if (xOffset == -1 && yOffset == -1) {
                return (mask & 0x43a40000) == 0 && (getClipedOnlyMask(plane, x - 1, y) & 0x42240000) == 0 && (getClipedOnlyMask(plane, x, y - 1) & 0x40a40000) == 0;
            }
            if (xOffset == 1 && yOffset == -1) {
                return (mask & 0x60e40000) == 0 && (getClipedOnlyMask(plane, x + 1, y) & 0x60240000) == 0 && (getClipedOnlyMask(plane, x, y - 1) & 0x40a40000) == 0;
            }
            if (xOffset == -1 && yOffset == 1) {
                return (mask & 0x4e240000) == 0 && (getClipedOnlyMask(plane, x - 1, y) & 0x42240000) == 0 && (getClipedOnlyMask(plane, x, y + 1) & 0x48240000) == 0;
            }
            if (xOffset == 1 && yOffset == 1) {
                return (mask & 0x78240000) == 0 && (getClipedOnlyMask(plane, x + 1, y) & 0x60240000) == 0 && (getClipedOnlyMask(plane, x, y + 1) & 0x48240000) == 0;
            }
        } else if (size == 2) {
            if (xOffset == -1 && yOffset == 0) {
                return (getClipedOnlyMask(plane, x - 1, y) & 0x43a40000) == 0 && (getClipedOnlyMask(plane, x - 1, y + 1) & 0x4e240000) == 0;
            }
            if (xOffset == 1 && yOffset == 0) {
                return (getClipedOnlyMask(plane, x + 2, y) & 0x60e40000) == 0 && (getClipedOnlyMask(plane, x + 2, y + 1) & 0x78240000) == 0;
            }
            if (xOffset == 0 && yOffset == -1) {
                return (getClipedOnlyMask(plane, x, y - 1) & 0x43a40000) == 0 && (getClipedOnlyMask(plane, x + 1, y - 1) & 0x60e40000) == 0;
            }
            if (xOffset == 0 && yOffset == 1) {
                return (getClipedOnlyMask(plane, x, y + 2) & 0x4e240000) == 0 && (getClipedOnlyMask(plane, x + 1, y + 2) & 0x78240000) == 0;
            }
            if (xOffset == -1 && yOffset == -1) {
                return (getClipedOnlyMask(plane, x - 1, y) & 0x4fa40000) == 0 && (getClipedOnlyMask(plane, x - 1, y - 1) & 0x43a40000) == 0 && (getClipedOnlyMask(plane, x, y - 1) & 0x63e40000) == 0;
            }
            if (xOffset == 1 && yOffset == -1) {
                return (getClipedOnlyMask(plane, x + 1, y - 1) & 0x63e40000) == 0 && (getClipedOnlyMask(plane, x + 2, y - 1) & 0x60e40000) == 0 && (getClipedOnlyMask(plane, x + 2, y) & 0x78e40000) == 0;
            }
            if (xOffset == -1 && yOffset == 1) {
                return (getClipedOnlyMask(plane, x - 1, y + 1) & 0x4fa40000) == 0 && (getClipedOnlyMask(plane, x - 1, y + 1) & 0x4e240000) == 0 && (getClipedOnlyMask(plane, x, y + 2) & 0x7e240000) == 0;
            }
            if (xOffset == 1 && yOffset == 1) {
                return (getClipedOnlyMask(plane, x + 1, y + 2) & 0x7e240000) == 0 && (getClipedOnlyMask(plane, x + 2, y + 2) & 0x78240000) == 0 && (getClipedOnlyMask(plane, x + 1, y + 1) & 0x78e40000) == 0;
            }
        } else {
            if (xOffset == -1 && yOffset == 0) {
                if ((getClipedOnlyMask(plane, x - 1, y) & 0x43a40000) != 0 || (getClipedOnlyMask(plane, x - 1, -1 + (y + size)) & 0x4e240000) != 0) {
                    return false;
                }
                for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++) {
                    if ((getClipedOnlyMask(plane, x - 1, y + sizeOffset) & 0x4fa40000) != 0) {
                        return false;
                    }
                }
            } else if (xOffset == 1 && yOffset == 0) {
                if ((getClipedOnlyMask(plane, x + size, y) & 0x60e40000) != 0 || (getClipedOnlyMask(plane, x + size, y - (-size + 1)) & 0x78240000) != 0) {
                    return false;
                }
                for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++) {
                    if ((getClipedOnlyMask(plane, x + size, y + sizeOffset) & 0x78e40000) != 0) {
                        return false;
                    }
                }
            } else if (xOffset == 0 && yOffset == -1) {
                if ((getClipedOnlyMask(plane, x, y - 1) & 0x43a40000) != 0 || (getClipedOnlyMask(plane, x + size - 1, y - 1) & 0x60e40000) != 0) {
                    return false;
                }
                for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++) {
                    if ((getClipedOnlyMask(plane, x + sizeOffset, y - 1) & 0x63e40000) != 0) {
                        return false;
                    }
                }
            } else if (xOffset == 0 && yOffset == 1) {
                if ((getClipedOnlyMask(plane, x, y + size) & 0x4e240000) != 0 || (getClipedOnlyMask(plane, x + (size - 1), y + size) & 0x78240000) != 0) {
                    return false;
                }
                for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++) {
                    if ((getClipedOnlyMask(plane, x + sizeOffset, y + size) & 0x7e240000) != 0) {
                        return false;
                    }
                }
            } else if (xOffset == -1 && yOffset == -1) {
                if ((getClipedOnlyMask(plane, x - 1, y - 1) & 0x43a40000) != 0) {
                    return false;
                }
                for (int sizeOffset = 1; sizeOffset < size; sizeOffset++) {
                    if ((getClipedOnlyMask(plane, x - 1, y + (-1 + sizeOffset)) & 0x4fa40000) != 0 || (getClipedOnlyMask(plane, sizeOffset - 1 + x, y - 1) & 0x63e40000) != 0) {
                        return false;
                    }
                }
            } else if (xOffset == 1 && yOffset == -1) {
                if ((getClipedOnlyMask(plane, x + size, y - 1) & 0x60e40000) != 0) {
                    return false;
                }
                for (int sizeOffset = 1; sizeOffset < size; sizeOffset++) {
                    if ((getClipedOnlyMask(plane, x + size, sizeOffset + (-1 + y)) & 0x78e40000) != 0 || (getClipedOnlyMask(plane, x + sizeOffset, y - 1) & 0x63e40000) != 0) {
                        return false;
                    }
                }
            } else if (xOffset == -1 && yOffset == 1) {
                if ((getClipedOnlyMask(plane, x - 1, y + size) & 0x4e240000) != 0) {
                    return false;
                }
                for (int sizeOffset = 1; sizeOffset < size; sizeOffset++) {
                    if ((getClipedOnlyMask(plane, x - 1, y + sizeOffset) & 0x4fa40000) != 0 || (getClipedOnlyMask(plane, -1 + (x + sizeOffset), y + size) & 0x7e240000) != 0) {
                        return false;
                    }
                }
            } else if (xOffset == 1 && yOffset == 1) {
                if ((getClipedOnlyMask(plane, x + size, y + size) & 0x78240000) != 0) {
                    return false;
                }
                for (int sizeOffset = 1; sizeOffset < size; sizeOffset++) {
                    if ((getClipedOnlyMask(plane, x + sizeOffset, y + size) & 0x7e240000) != 0 || (getClipedOnlyMask(plane, x + size, y + sizeOffset) & 0x78e40000) != 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static final boolean checkWalkStep(final int plane, final int x, final int y, final int dir, final int size) {
        return checkWalkStep(plane, x, y, Utils.DIRECTION_DELTA_X[dir], Utils.DIRECTION_DELTA_Y[dir], size);
    }

    public static final boolean checkWalkStep(final int plane, final int x, final int y, final int xOffset, final int yOffset, final int size) {
        if (size == 1) {
            final int mask = getMask(plane, x + xOffset, y + yOffset);
            if (xOffset == -1 && yOffset == 0) {
                return (mask & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST)) == 0;
            }
            if (xOffset == 1 && yOffset == 0) {
                return (mask & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_WEST)) == 0;
            }
            if (xOffset == 0 && yOffset == -1) {
                return (mask & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH)) == 0;
            }
            if (xOffset == 0 && yOffset == 1) {
                return (mask & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_SOUTH)) == 0;
            }
            if (xOffset == -1 && yOffset == -1) {
                return (mask & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.CORNEROBJ_NORTHEAST)) == 0 && (getMask(plane, x - 1, y) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST)) == 0 && (getMask(plane, x, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH)) == 0;
            }
            if (xOffset == 1 && yOffset == -1) {
                return (mask & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST)) == 0 && (getMask(plane, x + 1, y) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_WEST)) == 0 && (getMask(plane, x, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH)) == 0;
            }
            if (xOffset == -1 && yOffset == 1) {
                return (mask & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.CORNEROBJ_SOUTHEAST)) == 0 && (getMask(plane, x - 1, y) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST)) == 0 && (getMask(plane, x, y + 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_SOUTH)) == 0;
            }
            if (xOffset == 1 && yOffset == 1) {
                return (mask & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_SOUTHWEST)) == 0 && (getMask(plane, x + 1, y) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_WEST)) == 0 && (getMask(plane, x, y + 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_SOUTH)) == 0;
            }
        } else if (size == 2) {
            if (xOffset == -1 && yOffset == 0) {
                return (getMask(plane, x - 1, y) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.CORNEROBJ_NORTHEAST)) == 0 && (getMask(plane, x - 1, y + 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.CORNEROBJ_SOUTHEAST)) == 0;
            }
            if (xOffset == 1 && yOffset == 0) {
                return (getMask(plane, x + 2, y) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST)) == 0 && (getMask(plane, x + 2, y + 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_SOUTHWEST)) == 0;
            }
            if (xOffset == 0 && yOffset == -1) {
                return (getMask(plane, x, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.CORNEROBJ_NORTHEAST)) == 0 && (getMask(plane, x + 1, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST)) == 0;
            }
            if (xOffset == 0 && yOffset == 1) {
                return (getMask(plane, x, y + 2) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.CORNEROBJ_SOUTHEAST)) == 0 && (getMask(plane, x + 1, y + 2) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_SOUTHWEST)) == 0;
            }
            if (xOffset == -1 && yOffset == -1) {
                return (getMask(plane, x - 1, y) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.CORNEROBJ_NORTHEAST | Flags.CORNEROBJ_SOUTHEAST)) == 0 && (getMask(plane, x - 1, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.CORNEROBJ_NORTHEAST)) == 0 && (getMask(plane, x, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST | Flags.CORNEROBJ_NORTHEAST)) == 0;
            }
            if (xOffset == 1 && yOffset == -1) {
                return (getMask(plane, x + 1, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST | Flags.CORNEROBJ_NORTHEAST)) == 0 && (getMask(plane, x + 2, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST)) == 0 && (getMask(plane, x + 2, y) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST | Flags.CORNEROBJ_SOUTHWEST)) == 0;
            }
            if (xOffset == -1 && yOffset == 1) {
                return (getMask(plane, x - 1, y + 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.CORNEROBJ_NORTHEAST | Flags.CORNEROBJ_SOUTHEAST)) == 0 && (getMask(plane, x - 1, y + 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.CORNEROBJ_SOUTHEAST)) == 0 && (getMask(plane, x, y + 2) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_SOUTHEAST | Flags.CORNEROBJ_SOUTHWEST)) == 0;
            }
            if (xOffset == 1 && yOffset == 1) {
                return (getMask(plane, x + 1, y + 2) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_SOUTHEAST | Flags.CORNEROBJ_SOUTHWEST)) == 0 && (getMask(plane, x + 2, y + 2) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_SOUTHWEST)) == 0 && (getMask(plane, x + 1, y + 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST | Flags.CORNEROBJ_SOUTHWEST)) == 0;
            }
        } else {
            if (xOffset == -1 && yOffset == 0) {
                if ((getMask(plane, x - 1, y) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.CORNEROBJ_NORTHEAST)) != 0 || (getMask(plane, x - 1, -1 + (y + size)) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.CORNEROBJ_SOUTHEAST)) != 0) {
                    return false;
                }
                for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++) {
                    if ((getMask(plane, x - 1, y + sizeOffset) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.CORNEROBJ_NORTHEAST | Flags.CORNEROBJ_SOUTHEAST)) != 0) {
                        return false;
                    }
                }
            } else if (xOffset == 1 && yOffset == 0) {
                if ((getMask(plane, x + size, y) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST)) != 0 || (getMask(plane, x + size, y - (-size + 1)) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_SOUTHWEST)) != 0) {
                    return false;
                }
                for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++) {
                    if ((getMask(plane, x + size, y + sizeOffset) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST | Flags.CORNEROBJ_SOUTHWEST)) != 0) {
                        return false;
                    }
                }
            } else if (xOffset == 0 && yOffset == -1) {
                if ((getMask(plane, x, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.CORNEROBJ_NORTHEAST)) != 0 || (getMask(plane, x + size - 1, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST)) != 0) {
                    return false;
                }
                for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++) {
                    if ((getMask(plane, x + sizeOffset, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST | Flags.CORNEROBJ_NORTHEAST)) != 0) {
                        return false;
                    }
                }
            } else if (xOffset == 0 && yOffset == 1) {
                if ((getMask(plane, x, y + size) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.CORNEROBJ_SOUTHEAST)) != 0 || (getMask(plane, x + (size - 1), y + size) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_SOUTHWEST)) != 0) {
                    return false;
                }
                for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++) {
                    if ((getMask(plane, x + sizeOffset, y + size) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_SOUTHEAST | Flags.CORNEROBJ_SOUTHWEST)) != 0) {
                        return false;
                    }
                }
            } else if (xOffset == -1 && yOffset == -1) {
                if ((getMask(plane, x - 1, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.CORNEROBJ_NORTHEAST)) != 0) {
                    return false;
                }
                for (int sizeOffset = 1; sizeOffset < size; sizeOffset++) {
                    if ((getMask(plane, x - 1, y + (-1 + sizeOffset)) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.CORNEROBJ_NORTHEAST | Flags.CORNEROBJ_SOUTHEAST)) != 0 || (getMask(plane, sizeOffset - 1 + x, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST | Flags.CORNEROBJ_NORTHEAST)) != 0) {
                        return false;
                    }
                }
            } else if (xOffset == 1 && yOffset == -1) {
                if ((getMask(plane, x + size, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST)) != 0) {
                    return false;
                }
                for (int sizeOffset = 1; sizeOffset < size; sizeOffset++) {
                    if ((getMask(plane, x + size, sizeOffset + (-1 + y)) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST | Flags.CORNEROBJ_SOUTHWEST)) != 0 || (getMask(plane, x + sizeOffset, y - 1) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST | Flags.CORNEROBJ_NORTHEAST)) != 0) {
                        return false;
                    }
                }
            } else if (xOffset == -1 && yOffset == 1) {
                if ((getMask(plane, x - 1, y + size) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.CORNEROBJ_SOUTHEAST)) != 0) {
                    return false;
                }
                for (int sizeOffset = 1; sizeOffset < size; sizeOffset++) {
                    if ((getMask(plane, x - 1, y + sizeOffset) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.CORNEROBJ_NORTHEAST | Flags.CORNEROBJ_SOUTHEAST)) != 0 || (getMask(plane, -1 + (x + sizeOffset), y + size) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_SOUTHEAST | Flags.CORNEROBJ_SOUTHWEST)) != 0) {
                        return false;
                    }
                }
            } else if (xOffset == 1 && yOffset == 1) {
                if ((getMask(plane, x + size, y + size) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_SOUTHWEST)) != 0) {
                    return false;
                }
                for (int sizeOffset = 1; sizeOffset < size; sizeOffset++) {
                    if ((getMask(plane, x + sizeOffset, y + size) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_EAST | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_SOUTHEAST | Flags.CORNEROBJ_SOUTHWEST)) != 0 || (getMask(plane, x + size, y + sizeOffset) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ | Flags.WALLOBJ_NORTH | Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST | Flags.CORNEROBJ_NORTHWEST | Flags.CORNEROBJ_SOUTHWEST)) != 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static final Map<String, Player> playerMap = new ConcurrentHashMap<>();
    public static final List<String> playerLobbyMap = new ArrayList<String>();
    
    public static boolean containsPlayer(String username) {
        if (username == null)
            return false;
        username = Utils.formatPlayerNameForProtocol(username);
        boolean containsPlayer = playerMap.containsKey(username) && getPlayer(username) != null;
        return containsPlayer;
    }

    public static boolean containsPlayerLobby(String username) {
        if (username == null)
            return false;
        username = Utils.formatPlayerNameForProtocol(username);
        boolean containsPlayer = playerLobbyMap.contains(username);
        return containsPlayer;
    }
    
    public static final void addPlayerLobby(String username) {
        username = Utils.formatPlayerNameForProtocol(username);
        if(!playerLobbyMap.contains(username))
            playerLobbyMap.add(username);
        final String userName = username;
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                if (!playerLobbyMap.contains(userName)) {
                    stop();
                    return;
                }
                playerLobbyMap.remove(userName);
            }
            
        }, 10, 1);
    }
    
    public static final void removePlayerLobby(String username) {
        username = Utils.formatPlayerNameForProtocol(username);
        playerLobbyMap.remove(username);
    }
    
    public static Player getPlayer(String username) {
        if (username == null)
            return null;
        username = Utils.formatPlayerNameForProtocol(username);
        Player player = playerMap.get(username);
        if (player == null) {
            playerMap.remove(username);
            return null;
        }
        return player;
    }

    public static Player getPlayerByDisplayName(String displayName) {
        final String formattedDisplayName = Utils.formatPlayerNameForDisplay(displayName);
        final String formattedProtDisplayName = Utils.formatPlayerNameForProtocol(displayName);
        Iterator<Entry<String, Player>> itrPlayerMap = playerMap.entrySet().iterator();
        while (itrPlayerMap.hasNext()) {
            Entry<String, Player> e = itrPlayerMap.next();
            if (e.getValue() == null) {
                itrPlayerMap.remove();
                continue;
            }
            if (e.getValue().getUsername().equalsIgnoreCase(formattedDisplayName) ||
                    e.getValue().getDisplayName().equalsIgnoreCase(formattedDisplayName)) {
                return e.getValue();
            }
            if (e.getValue().getUsername().equalsIgnoreCase(formattedProtDisplayName) ||
                    e.getValue().getDisplayName().equalsIgnoreCase(formattedProtDisplayName)) {
                return e.getValue();
            }
        }
        return null;
    }

    public static Optional<Player> computePlayerByDisplayName(String displayName) {
        if (displayName == null)
            return Optional.empty();
        return Optional.ofNullable(getPlayerByDisplayName(displayName));
    }

    public static final EntityList<Player> getPlayers() {
        return players;
    }

    public static final EntityList<NPC> getNPCs() {
        return npcs;
    }

    public static final void safeShutdown(final boolean restart, final int delay) {
        if (exiting_start != 0) {
            Logger.getGlobal().warn("You cannot launch another restart if there's already one running!");
            return;
        }
        exiting_start = Utils.currentTimeMillis();
        exiting_delay = delay;
        for (final Player player : World.getPlayers()) {
            if (player == null || !player.isActive() || player.hasFinished()) {
                continue;
            }
            player.getPackets().sendSystemUpdate(delay);
        }
        CoresManager.getServiceProvider().executeWithDelay(() -> {
            try {
                for (final Player player : World.getPlayers()) {
                    if (player == null || !player.isActive()) {
                        continue;
                    }
                    player.realFinish();
                }
                IPBanL.save();
                IPMute.save();
                GrandExchange.save();
                EliteDungeon.save();
                StaffActivityMonitor.getInstance().save();
                if (restart) {
                    ServerLauncher.restartEmulator();
                } else {
                    ServerLauncher.shutdown();
                }
            } catch (final Throwable e) {
                Logger.getGlobal().catching(e);
            }
        }, delay, TimeUnit.SECONDS);
    }


    public static final boolean isSpawnedObject(final WorldObject object) {
        return getRegion(object.getRegionId()).getSpawnedObjects().contains(object);
    }

    public static final boolean isSpawnedObject(final Player player, final WorldObject object) {
        return getRegion(player.getRegionId()).getSpawnedObjects().contains(object);
    }

    public static final void spawnObject(final WorldObject object) {
        getRegion(object.getRegionId()).spawnObject(object, object.getPlane(), object.getXInRegion(), object.getYInRegion(), false);
    }

    public static final void spawnOriginalObject(final WorldObject object) {
        getRegion(object.getRegionId()).spawnObject(object, object.getPlane(), object.getXInRegion(), object.getYInRegion(), true);
    }

    public static final void spawnObject(final Player player, final WorldObject object) {
        getRegion(player.getRegionId()).spawnObject(object, object.getPlane(), object.getXInRegion(), object.getYInRegion(), false);
    }

    public static final void unclipTile(final WorldTile tile) {
        getRegion(tile.getRegionId()).unclip(tile.getPlane(), tile.getXInRegion(), tile.getYInRegion());
    }

    public static final void clipTile(final WorldTile tile) {
        getRegion(tile.getRegionId()).clip(tile.getPlane(), tile.getXInRegion(), tile.getYInRegion());
    }

    public static final void removeObject(final WorldObject object, final boolean removeClip) {
        getRegion(object.getRegionId()).removeObject(object, object.getPlane(), object.getXInRegion(), object.getYInRegion(), removeClip);
    }

    public static final void removeObject(final WorldObject object) {
        getRegion(object.getRegionId()).removeObject(object, object.getPlane(), object.getXInRegion(), object.getYInRegion());
    }

    public static void removeObject(WorldTile tile) {
        removeObject(World.getObject(tile));
    }

    public static final void removeObject(final Player player, final WorldObject object, final boolean removeClip) {
        getRegion(player.getRegionId()).removeObject(player, object, object.getPlane(), object.getXInRegion(), object.getYInRegion(), removeClip);
    }

    public static final void removeObject(final Player player, final WorldObject object) {
        getRegion(player.getRegionId()).removeObject(player, object, object.getPlane(), object.getXInRegion(), object.getYInRegion());
    }

    public static final void spawnObjectTemporary(final WorldObject object, final long time) {
        spawnObject(object);
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                try {
                    if (!World.isSpawnedObject(object)) {
                        return;
                    }
                    removeObject(object);
                } catch (final Throwable e) {
                    Logger.getGlobal().catching(e);
                }
            }
        }, Utils.msToTicks(time));
    }

    public static final void spawnObjectTemporary(final Player player, final WorldObject object, final long time) {
        spawnObject(object);
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                try {
                    if (!World.isSpawnedObject(player, object)) {
                        return;
                    }
                    removeObject(object);
                } catch (final Throwable e) {
                    Logger.getGlobal().catching(e);
                }
            }
        }, Utils.msToTicks(time));
    }

    public static final boolean removeObjectTemporary(final WorldObject object, final long time, final boolean removeClip) {
        removeObject(object, removeClip);
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                try {
                    spawnObject(object);
                } catch (final Throwable e) {
                    Logger.getGlobal().catching(e);
                }
            }
        }, Utils.msToTicks(time));
        return true;
    }

    public static final void spawnTempGroundObject(final WorldObject object, final int replaceId, final long time, final boolean removeClip) {
        final boolean native950 = CoresManager.isNative950();
        spawnObject(object);
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                try {
                    // A stale fire timer must not remove a replacement object or create duplicate ashes.
                    if (native950 && getObjectWithSlot(object, Region.OBJECT_SLOTS[object.getType()]) != object) return;
                    removeObject(object, removeClip);
                    FloorItem ashes = addGroundItem(new Item(replaceId), object, null, false, 180, 2, 60, false);
                    if (native950) ashes.markNative950();
                } catch (final Throwable e) {
                    Logger.getGlobal().catching(e);
                }
            }
        }, Utils.msToTicks(time));
    }

    public static final WorldObject getStandartObject(final WorldTile tile) {
        return getRegion(tile.getRegionId()).getStandartObject(tile.getPlane(), tile.getXInRegion(), tile.getYInRegion());
    }

    public static final WorldObject getObjectWithType(final WorldTile tile, final int type) {
        return getRegion(tile.getRegionId(), true).getObjectWithType(tile.getPlane(), tile.getXInRegion(), tile.getYInRegion(), type);
    }

    public static final WorldObject getObjectWithSlot(final WorldTile tile, final int slot) {
        return getRegion(tile.getRegionId()).getObjectWithSlot(tile.getPlane(), tile.getXInRegion(), tile.getYInRegion(), slot);
    }

    public static final boolean containsObjectWithId(final WorldTile tile, final int id) {
        return getRegion(tile.getRegionId()).containsObjectWithId(tile.getPlane(), tile.getXInRegion(), tile.getYInRegion(), id);
    }

    public static final WorldObject getObjectWithId(final WorldTile tile, final int id) {
        return getRegion(tile.getRegionId()).getObjectWithId(tile.getPlane(), tile.getXInRegion(), tile.getYInRegion(), id);
    }

    public static final FloorItem addGroundItem(final Item item, final WorldTile tile) {
        return addGroundItem(item, tile, null, false, -1, 2, -1, false);
    }

    public static final void addDungeoneeringGroundItem(final DungeonPartyManager manager, final Item item, final WorldTile tile, final boolean lootbeam) {
        addGroundItem(item, tile, manager.getLeaderPlayer(), false, -1, 2, -1, lootbeam);
    }

    public static final void addGroundItem(final Item item, final WorldTile tile, final int publicTime) {
        addGroundItem(item, tile, null, false, -1, 2, publicTime, false);
    }

    public static final void addGroundItem(final Item item, final WorldTile tile, final Player owner, final boolean invisible, final long hiddenTime) {
        addGroundItem(item, tile, owner, invisible, hiddenTime, 2, 60, false);
    }

    public static final FloorItem addGroundItem(final Item item, final WorldTile tile, final Player owner, final boolean invisible, final long hiddenTime, final int type, final boolean lootbeam) {
        return addGroundItem(item, tile, owner, invisible, hiddenTime, type, 60, lootbeam);
    }

    public static final void turnPublic(final FloorItem floorItem, final int publicTime) {
        if (!floorItem.isInvisible()) {
            return;
        }
        final int regionId = floorItem.getTile().getRegionId();
        final Region region = getRegion(regionId);
        if (!containsStoredGroundItem(region, floorItem)) {
            return;
        }
        final Player realOwner = floorItem.hasOwner() ? World.getPlayer(floorItem.getOwner()) : null;
        floorItem.setInvisible(false);
        for (final Player player : getPlayers()) {
            if (player == null || player == realOwner || player.hasFinished() || player.getPlane() != floorItem.getTile().getPlane() || !player.getMapRegionsIds().contains(regionId) || !ItemConstants.isTradeable(floorItem)) {
                continue;
            }
            if (realOwner != null && player.isGroupIronman() && !player.canGimInteractWith(realOwner)) {
                continue;
            }
            player.getPackets().sendGroundItem(floorItem);
        }
        if (publicTime != -1) {
            removeGroundItem(floorItem, publicTime);
        }
    }

    @Deprecated
    public static final void addGroundItemForever(final Item item, final WorldTile tile) {
        final int regionId = tile.getRegionId();
        final FloorItem floorItem = new FloorItem(item, tile, true);
        final Region region = getRegion(tile.getRegionId());
        region.getGroundItemsSafe().add(floorItem);
        for (final Player player : getPlayers()) {
            if (player == null || player.hasFinished() || player.getPlane() != floorItem.getTile().getPlane() || !player.getMapRegionsIds().contains(regionId)) {
                continue;
            }
            player.getPackets().sendGroundItem(floorItem);
        }
    }

    private static final ImmutableSet<Integer> DIAMOND_ITEMS = ImmutableSet.of(11286, 989, 985, 987);

    /*
     * type 0 - if not tradeable type 1 - if destroyable type 2 - no
     */
    public static final FloorItem addGroundItem(final Item item, final WorldTile tile, final Player owner, final boolean invisible, final long hiddenTime, final int type, final int publicTime, final boolean lootbeam) {
        if (owner != null && !owner.isNative950() && DIAMOND_ITEMS.contains(item.getId()) && owner.isDiamondDonor() && owner.isNotingDrops()) {
            val definitions = ItemDefinitions.getItemDefinitions(item.getId());
            if (definitions != null && !definitions.isNoted() && definitions.getCertId() != -1) {
                item.setId(definitions.getCertId());
            }
        }
        final FloorItem floorItem = new FloorItem(item, tile, owner, false, invisible);
        floorItem.setLootbeam(lootbeam);
        final Region region = getRegion(tile.getRegionId());
        if (type == 1) {
            if (ItemConstants.isTradeable(item) || ItemConstants.turnCoins(item)) {
                region.getGroundItemsSafe().add(floorItem);
            }
            if (invisible) {
                if (owner != null) {
                    if (ItemConstants.isTradeable(item) || ItemConstants.turnCoins(item)) {
                        owner.getPackets().sendGroundItem(floorItem);
                    }
                }
                if (hiddenTime != -1) {
                    WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                            try {
                                stop();
                                turnPublic(floorItem, publicTime);
                            } catch (final Throwable e) {
                                Logger.getGlobal().catching(e);
                            }
                        }
                    }, Utils.secondsToTicks(hiddenTime));
                }
            } else {
                final int regionId = tile.getRegionId();
                for (final Player player : getPlayers()) {
                    if (player == null || player.hasFinished() || player.getPlane() != tile.getPlane() || !player.getMapRegionsIds().contains(regionId)) {
                        continue;
                    }
                    player.getPackets().sendGroundItem(floorItem);
                }
                if (publicTime != -1) {
                    removeGroundItem(floorItem, publicTime);
                }
            }
        } else {
            region.getGroundItemsSafe().add(floorItem);
            if (invisible) {
                if (owner != null) {
                    owner.getPackets().sendGroundItem(floorItem);
                }
                if (hiddenTime != -1) {
                    WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                            try {
                                stop();
                                turnPublic(floorItem, publicTime);
                            } catch (final Throwable e) {
                                Logger.getGlobal().catching(e);
                            }
                        }
                    }, Utils.secondsToTicks(hiddenTime));
                }
            } else {
                final int regionId = tile.getRegionId();
                for (final Player player : getPlayers()) {
                    if (player == null || player.hasFinished() || player.getPlane() != tile.getPlane() || !player.getMapRegionsIds().contains(regionId) || !ItemConstants.isTradeable(item)) {
                        continue;
                    }
                    player.getPackets().sendGroundItem(floorItem);
                }
                if (publicTime != -1) {
                    removeGroundItem(floorItem, publicTime);
                }
            }
        }
        return floorItem;
    }

    public static final void updateGroundItem(final Item item, final WorldTile tile, final Player owner, final boolean lootbeam) {
        final FloorItem floorItem = World.getRegion(tile.getRegionId()).getGroundItem(item.getId(), tile, owner);
        if (floorItem == null) {
            updateGroundItem(item, tile, owner, 60, 0, lootbeam);
            return;
        }
        floorItem.setLootbeam(lootbeam);
        floorItem.setAmount(floorItem.getAmount() + item.getAmount());
    }

    public static final void updateGroundItem(final Item item, final WorldTile tile, final Player owner, final int time, final int type, final boolean lootbeam) {
        final FloorItem floorItem = World.getRegion(tile.getRegionId()).getGroundItem(item.getId(), tile, owner);
        int floorAmount = floorItem == null ? 0 : floorItem.getAmount();
        long overflow = item.getAmount() + floorAmount;
        if (overflow > Integer.MAX_VALUE) {
            if (owner != null)
                owner.sendMessage("You can't drop that here.");
            return;
        }
        if (floorItem == null || item.getCharges() > 0 || item.getAttributes() != null) {
            addGroundItem(item, tile, owner, true, time, type, lootbeam);
            return;
        }
        if (lootbeam) {
            floorItem.setLootbeam(true);
        }
        if (floorItem.getAttributes() == null && floorItem.getDefinitions().isStackable()) {
            if (floorItem.getAmount() + item.getAmount() < 0) {
                final int totalAmount = Integer.MAX_VALUE - floorItem.getAmount();
                floorItem.setAmount(Integer.MAX_VALUE);
                item.setAmount(item.getAmount() - totalAmount);
                addGroundItem(item, tile, owner, true, time, type, lootbeam);
                owner.getPackets().sendRemoveGroundItem(floorItem);
                owner.getPackets().sendGroundItem(floorItem);
            } else {
                floorItem.setAmount(floorItem.getAmount() + item.getAmount());
            }
            owner.getPackets().sendRemoveGroundItem(floorItem);
            owner.getPackets().sendGroundItem(floorItem);
        } else {
            addGroundItem(item, tile, owner, true, time, type, lootbeam);
        }
    }

    /** A picked-up native pile's old task must never publish or remove an equal replacement pile. */
    private static boolean containsStoredGroundItem(final Region region, final FloorItem floorItem) {
        if (!floorItem.isNative950()) return region.getGroundItemsSafe().contains(floorItem);
        for (FloorItem candidate : region.getGroundItemsSafe()) if (candidate == floorItem) return true;
        return false;
    }

    private static boolean removeStoredGroundItem(final Region region, final FloorItem floorItem) {
        if (!floorItem.isNative950()) return region.getGroundItemsSafe().remove(floorItem);
        return region.getGroundItemsSafe().removeIf(candidate -> candidate == floorItem);
    }

    private static final void removeGroundItem(final FloorItem floorItem, final long publicTime) {
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                try {
                    final int regionId = floorItem.getTile().getRegionId();
                    final Region region = getRegion(regionId);
                    if (!removeStoredGroundItem(region, floorItem)) {
                        return;
                    }
                    for (final Player player : World.getPlayers()) {
                        if (player == null

                                || player.hasFinished() || player.getPlane() != floorItem.getTile().getPlane() || !player.getMapRegionsIds().contains(regionId)) {
                            continue;
                        }
                        player.getPackets().sendRemoveGroundItem(floorItem);
                    }
                } catch (final Throwable e) {
                    Logger.getGlobal().catching(e);
                }
            }
        }, Utils.secondsToTicks(publicTime));
    }

    public static final boolean removeGroundItem(final Player player, final FloorItem floorItem) {
        return removeGroundItem(player, floorItem, true);
    }

    public static final void spawnTemporaryObject(final WorldObject object, final long time) {
        spawnTemporaryObject(object, time, false);
    }

    public static final void spawnTemporaryObject(final WorldObject object, final long time, final boolean clip) {
        spawnObject(object);
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                try {
                    if (!World.isSpawnedObject(object)) {
                        return;
                    }
                    removeObject(object);
                } catch (final Throwable e) {
                    Logger.getGlobal().catching(e);
                }
            }
        }, Utils.msToTicks(time));
    }

    public static final boolean removeGroundItem(final Player player, final FloorItem floorItem, final boolean add) {
        final int regionId = floorItem.getTile().getRegionId();
        final Region region = getRegion(regionId);
        final WorldTile playerTile = new WorldTile(player.getX(), player.getY(), player.getPlane());
        if (World.soulWars.getTasks().get(SoulWarsManager.PlayerType.IN_GAME) != null) {
            if (floorItem.getId() == SoulWarsManager.BANDAGE_ID && player.isUnderCombat()) {
                player.sendMessage("You can't pick this while in combat!");
                return false;
            }
        }
        if (!region.getGroundItemsSafe().contains(floorItem)) {
            return false;
        }
        int amount = floorItem.getAmount();
        LoggingSystem.logItemPickup(player, floorItem, playerTile);
        if (floorItem.getId() == 995) {
            amount = floorItem.getAmount();
            int leftOver = 0;
            int inventoryLeftOver = 0;
            if (player.getMoneyPouch().getTotal() + amount > Integer.MAX_VALUE || player.getMoneyPouch().getTotal() + amount < 0) {
                player.getPackets().sendGameMessage("Your money pouch is not big enough to hold that much cash.");
                leftOver = Integer.MAX_VALUE - player.getMoneyPouch().getTotal();
                amount = amount - leftOver;
                if (player.getMoneyPouch().getTotal() != Integer.MAX_VALUE) {
                    player.getPackets().sendRunScript(5561, 1, leftOver);
                    player.getMoneyPouch().setTotal(Integer.MAX_VALUE);
                    player.getPackets().sendGameMessage(Utils.getFormattedNumber(leftOver, ',') + " coins have been added to your money pouch.");
                    player.getMoneyPouch().refresh();
                    floorItem.setAmount(Integer.MAX_VALUE - leftOver);
                }
                if (player.getInventory().getNumberOf(995) + amount > Integer.MAX_VALUE || player.getInventory().getNumberOf(995) + amount < 0) {
                    if (!player.getInventory().hasFreeSlots()) {
                        player.getPackets().sendGameMessage("You don't have enough inventory space.");
                        return false;
                    }
                    inventoryLeftOver = Integer.MAX_VALUE - player.getInventory().getNumberOf(995);
                    amount = amount - inventoryLeftOver;
                    if (player.getInventory().getNumberOf(995) != Integer.MAX_VALUE) {
                        player.getInventory().deleteItem(995, Integer.MAX_VALUE);
                        player.getInventory().addItem(995, Integer.MAX_VALUE);
                    }
                    floorItem.setAmount(amount);
                    player.getPackets().sendRemoveGroundItem(floorItem);
                    player.getPackets().sendGroundItem(floorItem);
                    return false;
                } else {
                    if (!player.getInventory().hasFreeSlots()) {
                        player.getPackets().sendGameMessage("You don't have enough inventory space.");
                        return false;
                    }
                    player.getInventory().addItem(995, amount);
                }
                region.getGroundItemsSafe().remove(floorItem);
                if (floorItem.isInvisible()) {
                    player.getPackets().sendRemoveGroundItem(floorItem);
                    return true;
                } else {
                    for (final Player p2 : World.getPlayers()) {
                        if (p2 == null || p2.hasFinished() || p2.getPlane() != floorItem.getTile().getPlane() || !p2.getMapRegionsIds().contains(regionId)) {
                            continue;
                        }
                        p2.getPackets().sendRemoveGroundItem(floorItem);
                    }
                    if (floorItem.isForever()) {
                        WorldTasksManager.schedule(new WorldTask() {
                            @Override
                            public void run() {
                                try {
                                    addGroundItemForever(floorItem, floorItem.getTile());
                                } catch (final Throwable e) {
                                    Logger.getGlobal().catching(e);
                                }
                            }
                        }, 100);
                    }
                    return true;
                }
            }
            if (player.getMoneyPouch().getTotal() == Integer.MAX_VALUE) {
                return false;
            }
            if (amount > 1) {
                player.getPackets().sendGameMessage(Utils.getFormattedNumber(amount, ',') + " coins have been added to your money pouch.");
            } else {
                player.getPackets().sendGameMessage("One coin has been added to your money pouch.");
            }
            player.getPackets().sendRunScript(5561, 1, amount);
            player.getMoneyPouch().setTotal(player.getMoneyPouch().getTotal() + amount);
            player.getMoneyPouch().refresh();
            region.getGroundItemsSafe().remove(floorItem);
            if (floorItem.isInvisible()) {
                player.getPackets().sendRemoveGroundItem(floorItem);
                return true;
            } else {
                for (final Player p2 : World.getPlayers()) {
                    if (p2 == null || p2.hasFinished() || p2.getPlane() != floorItem.getTile().getPlane() || !p2.getMapRegionsIds().contains(regionId)) {
                        continue;
                    }
                    p2.getPackets().sendRemoveGroundItem(floorItem);
                }
                if (floorItem.isForever()) {
                    WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                            try {
                                addGroundItemForever(floorItem, floorItem.getTile());
                            } catch (final Throwable e) {
                                Logger.getGlobal().catching(e);
                            }
                        }
                    }, 100);
                }
                return true;
            }
        } else if (floorItem.getId() != 995) {
            if (player.getInventory().getNumberOf(floorItem.getId()) + floorItem.getAmount() > Integer.MAX_VALUE || player.getInventory().getNumberOf(floorItem.getId()) + floorItem.getAmount() < 0) {
                amount = floorItem.getAmount();
                amount = Integer.MAX_VALUE - player.getInventory().getNumberOf(floorItem.getAmount());
                floorItem.setAmount(Integer.MAX_VALUE - amount);
                player.getInventory().deleteItem(floorItem.getId(), Integer.MAX_VALUE);
                player.getInventory().addItem(floorItem.getId(), Integer.MAX_VALUE);
                player.sendMessage("Inventory full. To make more room, sell, drop or bank something.");
                return false;
            } else if (player.getInventory().getFreeSlots() == 0 && (floorItem.getAttributes() == null && floorItem.getDefinitions().isStackable() && !player.getInventory().containsItem(floorItem.getId(), 1))) {
                player.sendMessage("Inventory full. To make more room, sell, drop or bank something.");
                return false;
            } else if (player.getInventory().getFreeSlots() == 0 && (!floorItem.getDefinitions().isStackable())) {
                player.sendMessage("Inventory full. To make more room, sell, drop or bank something.");
                return false;
            }
            if (player.getFreezeDelay() >= Utils.currentTimeMillis()) {
                if (!floorItem.getTile().matches(playerTile)) {
                    player.setNextAnimation(new Animation(537));
                }
            }
            region.getGroundItemsSafe().remove(floorItem);

            if (add) {
                player.getInventory().addItem(new Item(floorItem.getId(), floorItem.getAmount(), floorItem.getCharges()).setAttributes(floorItem.getAttributes()));
            }

            if (floorItem.isInvisible()) {
                player.getPackets().sendRemoveGroundItem(floorItem);
                return true;
            } else {
                for (final Player p2 : World.getPlayers()) {
                    if (p2 == null || p2.hasFinished() || p2.getPlane() != floorItem.getTile().getPlane() || !p2.getMapRegionsIds().contains(regionId)) {
                        continue;
                    }
                    p2.getPackets().sendRemoveGroundItem(floorItem);
                }
                if (floorItem.isForever()) {
                    WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                            try {
                                addGroundItemForever(floorItem, floorItem.getTile());
                            } catch (final Throwable e) {
                                Logger.getGlobal().catching(e);
                            }
                        }
                    }, 100);
                }
            }
        }
        return true;
    }

    public static final void sendObjectAnimation(final WorldObject object, final Animation animation) {
        sendObjectAnimation(null, object, animation);
    }

    public static final void sendObjectAnimation(final Entity creator, final WorldObject object, final Animation animation) {
        if (creator == null) {
            for (final Player player : World.getPlayers()) {
                if (player == null || player.hasFinished() || object == null || !player.withinDistance(object)) {
                    continue;
                }
                player.getPackets().sendObjectAnimation(object, animation);
            }
        } else {
            for (final int regionId : creator.getMapRegionsIds()) {
                final List<Integer> playersIndexes = getRegion(regionId).getPlayerIndexes();
                if (playersIndexes == null) {
                    continue;
                }
                for (final Integer playerIndex : playersIndexes) {
                    final Player player = getPlayers().get(playerIndex);
                    if (player == null || player.hasFinished() || !player.withinDistance(object)) {
                        continue;
                    }
                    player.getPackets().sendObjectAnimation(object, animation);
                }
            }
        }
    }

    public static final void sendGraphics(final Entity creator, final Graphics graphics, final WorldTile tile) {
        if (creator == null) {
            for (final Player player : World.getPlayers()) {
                if (player == null || tile == null || player.hasFinished() || !player.withinDistance(tile)) {
                    continue;
                }
                player.getPackets().sendGraphics(graphics, tile);
            }
        } else {
            for (final int regionId : creator.getMapRegionsIds()) {
                final List<Integer> playersIndexes = getRegion(regionId).getPlayerIndexes();
                if (playersIndexes == null) {
                    continue;
                } // its fixed now
                for (final Integer playerIndex : playersIndexes) {
                    final Player player = getPlayers().get(playerIndex);
                    if (player == null || player.hasFinished() || (!(creator instanceof Legios) && !(creator instanceof Telos) && !(creator instanceof EliteDungeonBoss) && !player.withinDistance(tile))) {
                        continue;
                    }
                    player.getPackets().sendGraphics(graphics, tile);
                }
            }
        }
    }



    public static final void sendProjectile(final Entity shooter, final Entity receiver, final int gfxId, final int startHeight, final int endHeight, final int speed, final int delay, final int curve) {
        for (final int regionId : shooter.getMapRegionsIds()) {
            final List<Integer> playersIndexes = getRegion(regionId).getPlayerIndexes();
            if (playersIndexes == null) {
                continue;
            }
            for (final Integer playerIndex : playersIndexes) {
                final Player player = getPlayers().get(playerIndex);
                if (player == null

                        || player.hasFinished() || (!player.withinDistance(shooter) && !player.withinDistance(receiver))) {
                    continue;
                }
                final int size = shooter.getSize();
                final int distance = Utils.getDistance(shooter, receiver);
                final int startOffsetDistance = distance > 2 ? 0 : 11;
                final int startOffsetDistance2 = 0;
                player.getPackets().sendProjectile(receiver, new WorldTile(shooter.getCoordFaceX(size), shooter.getCoordFaceY(size), shooter.getPlane()), receiver, gfxId, startHeight, endHeight, speed, delay, curve, receiver instanceof Player ? startOffsetDistance : startOffsetDistance2, size);
            }
        }
    }

    public static final void sendProjectile(final Entity shooter, final WorldTile startTile, final WorldTile receiver, final int gfxId, final int startHeight, final int endHeight, final int speed, final int delay, final int curve, final int startDistanceOffset) {
        for (final int regionId : shooter.getMapRegionsIds()) {
            final List<Integer> playersIndexes = getRegion(regionId).getPlayerIndexes();
            if (playersIndexes == null) {
                continue;
            }
            for (final Integer playerIndex : playersIndexes) {
                final Player player = getPlayers().get(playerIndex);
                if (player == null

                        || player.hasFinished() || (!player.withinDistance(shooter) && !player.withinDistance(receiver))) {
                    continue;
                }
                player.getPackets().sendProjectile(null, startTile, receiver, gfxId, startHeight, endHeight, speed, delay, curve, startDistanceOffset, shooter.getSize());
            }
        }
    }

    public static final void sendProjectile(final WorldTile shooter, final Entity receiver, final int gfxId, final int startHeight, final int endHeight, final int speed, final int delay, final int curve, final int startDistanceOffset) {
        for (final int regionId : receiver.getMapRegionsIds()) {
            final List<Integer> playersIndexes = getRegion(regionId).getPlayerIndexes();
            if (playersIndexes == null) {
                continue;
            }
            for (final Integer playerIndex : playersIndexes) {
                final Player player = getPlayers().get(playerIndex);
                if (player == null

                        || player.hasFinished() || (!player.withinDistance(shooter) && !player.withinDistance(receiver))) {
                    continue;
                }
                player.getPackets().sendProjectile(receiver, shooter, receiver, gfxId, startHeight, endHeight, speed, delay, curve, startDistanceOffset, 1);
            }
        }
    }

    public static final void sendStillProjectile(final WorldTile shooter, final WorldTile receiver, final int gfxId, final int startHeight, final int endHeight, final int speed, final int delay, final int curve, final int startDistanceOffset) {
        for (final Player pl : getPlayers()) {
            if (pl == null || !pl.withinDistance(receiver, 20)) {
                continue;
            }
            pl.getPackets().sendStillProjectile(receiver, shooter, shooter, gfxId, startHeight, endHeight, speed, delay, curve, startDistanceOffset, 1);
        }
    }

    public static final void sendProjectile(final Entity shooter, final WorldTile receiver, final int gfxId, final int startHeight, final int endHeight, final int speed, final int delay, final int curve, final int startDistanceOffset) {
        for (final int regionId : shooter.getMapRegionsIds()) {
            final List<Integer> playersIndexes = getRegion(regionId).getPlayerIndexes();
            if (playersIndexes == null) {
                continue;
            }
            for (final Integer playerIndex : playersIndexes) {
                final Player player = getPlayers().get(playerIndex);
                if (player == null

                        || player.hasFinished() || (!player.withinDistance(shooter) && !player.withinDistance(receiver))) {
                    continue;
                }
                player.getPackets().sendProjectile(null, shooter, receiver, gfxId, startHeight, endHeight, speed, delay, curve, startDistanceOffset, shooter.getSize());
            }
        }
    }

    public static final void sendProjectile(final Entity shooter, final Entity receiver, final int gfxId, final int startHeight, final int endHeight, final int speed, final int delay, final int curve, final int startDistanceOffset) {
        for (final int regionId : shooter.getMapRegionsIds()) {
            final List<Integer> playersIndexes = getRegion(regionId).getPlayerIndexes();
            if (playersIndexes == null) {
                continue;
            }
            for (final Integer playerIndex : playersIndexes) {
                final Player player = getPlayers().get(playerIndex);
                if (player == null || player.hasFinished() || (!player.withinDistance(shooter) && !player.withinDistance(receiver))) {
                    continue;
                }
                final int size = shooter.getSize();
                player.getPackets().sendProjectile(receiver, shooter, receiver, gfxId, startHeight, endHeight, speed, delay, curve, startDistanceOffset, size);
            }
        }
    }

    public static final boolean isMultiArea(final WorldTile tile) {
        if (Wilderness.isAtDynamicPvP(tile)) {
            return true;
        }
        /**
         * full regions multi.
         */
        /*
         * switch (tile.getRegionId()){ // case 16729: // return true; }
         */
        final int destX = tile.getX();
        final int destY = tile.getY();
        return (destX >= 3462 && destX <= 3511 && destY >= 9481 && destY <= 9521 && tile.getPlane() == 0) // kalphite
                // lair
                || (destX >= 4606 && destX <= 4799 && destY >= 5052 && destY <= 5183 && tile.getPlane() == 0) // thzaar
                // ripper demons
                || (destX >= 5134 && destX <= 5168 && destY >= 7572 && destY <= 7596)
                // city
                || (destX >= 1721 && destX <= 1791 && destY >= 5123 && destY <= 5249) || (destX >= 2250 && destX <= 2280 && destY >= 4670 && destY <= 4720) || (destX >= 2987 && destX <= 3006 && destY >= 3912 && destY <= 3937) || (destX >= 2895 && destX <= 2937 && destY >= 4430 && destY <= 4472) || (destX >= 2245 && destX <= 2295 && destY >= 4675 && destY <= 4720) || (destX >= 2450 && destX <= 3520 && destY >= 9450 && destY <= 9550) || (destX >= 3006 && destX <= 3071 && destY >= 3602 && destY <= 3710) || (destX >= 3061 && destX <= 3328 && destY >= 6854 && destY <= 7100) || (destX >= 3134 && destX <= 3192 && destY >= 3519 && destY <= 3646) || (destX >= 2815 && destX <= 2966 && destY >= 5240 && destY <= 5375)// wild
                || (destX >= 1790 && destX <= 1987 && destY >= 3194 && destY <= 3273) // soul
                // wars
                || (destX >= 2840 && destX <= 2950 && destY >= 5190 && destY <= 5230) // godwars
                || (destX >= 3547 && destX <= 3555 && destY >= 9690 && destY <= 9699) || (destX >= 3136 && destX <= 3327 && destY >= 3519 && destY <= 3607) // WILDY
                || (destX >= 3190 && destX <= 3327 && destY >= 3648 && destY <= 3839) || (destX >= 3200 && destX <= 3390 && destY >= 3840 && destY <= 3967)

                || (destX >= 2363 && destX <= 2425 && destY >= 6781 && destY <= 6858)

                || (destX >= 2992 && destX <= 3007 && destY >= 3912 && destY <= 3967) || (destX >= 2946 && destX <= 2959 && destY >= 3816 && destY <= 3831) || (destX >= 3008 && destX <= 3199 && destY >= 3856 && destY <= 3903) || (destX >= 3008 && destX <= 3071 && destY >= 3600 && destY <= 3711) || (destX >= 3072 && destX <= 3327 && destY >= 3608 && destY <= 3647) || (destX >= 2624 && destX <= 2690 && destY >= 2550 && destY <= 2619) || (destX >= 2371 && destX <= 2422 && destY >= 5062 && destY <= 5117) || (destX >= 2896 && destX <= 2927 && destY >= 3595 && destY <= 3630) || (destX >= 2892 && destX <= 2932 && destY >= 4435 && destY <= 4464) || (destX >= 2256 && destX <= 2287 && destY >= 4680 && destY <= 4711) || (destX >= 2863 && destX <= 2878 && destY >= 5350 && destY <= 5372) || KingBlackDragon.atKBD(tile) // KBD
                || TormentedDemon.atTD(tile) // Tormented demon's area
                || Bork.atBork(tile) // Bork's area
                || (destX >= 2970 && destX <= 3000 && destY >= 4365 && destY <= 4400)// corp
                || (destX >= 3195 && destX <= 3327 && destY >= 3520 && destY <= 3970 || (destX >= 2376 && 5127 >= destY && destX <= 2422 & 5168 <= destY)) || (destX >= 2622 && destY >= 5696 && destX <= 2573 && destY <= 5752) // torms
                || (destX >= 2368 && destY >= 3072 && destX <= 2431 && destY <= 3135) // castlewars
                || (destX >= 2365 && destY >= 9470 && destX <= 2436 && destY <= 9532) // castlewars
                || (destX >= 2948 && destY >= 5537 && destX <= 3071 && destY <= 5631) // Risk

                || (destX >= 3356 && destY >= 3917 && destX <= 3376 && destY <= 3937) // Risk
                || (destX >= 3274 && destY >= 3837 && destX <= 3294 && destY <= 3857) // Risk
                || (destX >= 2949 && destY >= 3811 && destX <= 2969 && destY <= 3831) // Risk

                // ffa.
                || (destX >= 2756 && destY >= 5537 && destX <= 2879 && destY <= 5631) // Safe

                || /* South West */(destX >= 2654 && destY >= 3706 && // rock
                // crabs
                /* North East */destX <= 2723 && destY <= 3754) || /*
         * South West
         */(destX >= 4481 && destY >= 6207 && // araxyve
                // cave
                /* North East */destX <= 4607 && destY <= 6333) || /*
         * South West
         */(destX >= 2691 && destY >= 9410 && // brimhaven
                // dungeon
                // -
                // dragons
                /* North East */destX <= 2748 && destY <= 9538) || /*
         * South West
         */(destX >= 999 && destY >= 558 && // ascension
                // dungeon
                /* North East */destX <= 1209 && destY <= 765) || /*
         * South West
         */(destX >= 2955 && destY >= 1735 && // kalphite
                // king
                // lair
                /* North East */destX <= 2997 && destY <= 1783) || /*
         * South West
         */(destX >= 3009 && destY >= 5955 && // vorago
                // borehole
                /* North East */destX <= 3135 && destY <= 6136) || /*
         * South West
         */(destX >= 3218 && destY >= 5079 && // duel
                // arena
                // -
                // summoning
                /* North East */destX <= 3246 && destY <= 5159)
         || (destX >= 2845 && destY >= 9831 && destX <= 2877 && destY <= 9857) // taverly hellhounds
         || (destX >= 1349 && destY >= 4547 && destX <= 1399 && destY <= 4605) // taverly hellhounds resource dung
         || (destX >= 4161 && destY >= 5696 && destX <= 4223 && destY <= 5758) // glacors cave
         || (destX >= 1793 && destY >= 5953 && destX <= 1854 && destY <= 6014) // guthix cave
         || (destX >= 3396 && destY >= 3536 && destX <= 3453 && destY <= 3579) // slayer tower
//         || (destX >= 2368 && destY >= 6784 && destX <= 2489 && destY <= 6906) // soph dung1
//         || (destX >= 2446 && destY >= 6721 && destX <= 2484 && destY <= 6783) // soph dung2
//         || (destX >= 1588 && destY >= 5237 && destX <= 1674 && destY <= 5339) // kura dung
                || (destX >= 2240 && destY >= 5953 && destX <= 2302 && destY <= 6019) // cele drags
                || (destX >= 3969 && destY >= 6465 && destX <= 4159 && destY <= 6718) // crystal shapshifters
                || (destX >= 1281 && destY >= 1282 && destX <= 1342 && destY <= 1342) // crystal shapshifters
                || (destX >= 3520 && destY >= 9663 && destX <= 3583 && destY <= 9725) // barrows tunnel
                || (tile.getX() >= 3011 && tile.getX() <= 3132 && tile.getY() >= 10052 && tile.getY() <= 10175 && (tile.getY() >= 10066 || tile.getX() >= 3094)); // forin
        // dung
    }

    public static final boolean isPvpArea(final WorldTile tile) {
        return Wilderness.isAtWild(tile);
    }

    public static final void addPlayer(final Player player) {
        removePlayerLobby(player.getUsername());
        if (players.add(player)) {
            playerMap.put(player.getUsername(), player);
        }
//		AntiFlood.add(player.getIP());
    }

    public static void removePlayer(final Player player) {
        removePlayerLobby(player.getUsername());
        Iterator<Player> itr = players.iterator();
        while (itr.hasNext()) {
            Player p = itr.next();
            if (p == null || (player != null && p.equals(player)))
                itr.remove();
        }
        Iterator<Entry<String, Player>> itrPlayerMap = playerMap.entrySet().iterator();
        while (itrPlayerMap.hasNext()) {
            Entry<String, Player> e = itrPlayerMap.next();
            if (e.getValue() == null || (player != null &&e.getValue().equals(player)))
                itrPlayerMap.remove();
        }
        AntiFlood.remove(player.getIP());
    }

    public static void forceRemovePlayer(Player player) {
        removePlayerLobby(player.getUsername());
        Iterator<Player> itr = players.iterator();
        while (itr.hasNext()) {
            Player p = itr.next();
            if ( p == null || (player != null && p.equals(player)))
                itr.remove();
        }
        Iterator<Entry<String, Player>> itrPlayerMap = playerMap.entrySet().iterator();
        while (itrPlayerMap.hasNext()) {
            Entry<String, Player> e = itrPlayerMap.next();
            if (e.getValue() == null || (player != null &&e.getValue().equals(player)))
                itrPlayerMap.remove();
        }
        AntiFlood.remove(player.getIP());
    }

    public static void sendWorldMessage(final String message, final boolean forStaff) {
        for (final Player p : World.getPlayers()) {
            if (p == null || !p.isRunning() || p.isYellOff() || (forStaff && !p.isStaff())) {
                continue;
            }
            p.getPackets().sendGameMessage(message);
        }
    }

    public static void addItemsAll(final Item item) {
        for (final Player p : World.getPlayers()) {
            if (p == null) {
                continue;
            }
            p.getInventory().addItemDrop(item.getId(), item.getAmount());
        }
    }

    public static final void sendProjectile(final WorldObject object, final WorldTile startTile, final WorldTile endTile, final int gfxId, final int startHeight, final int endHeight, final int speed, final int delay, final int curve, final int startOffset) {
        for (final Player pl : getPlayers()) {
            if (pl == null || !pl.withinDistance(object, 20)) {
                continue;
            }
            pl.getPackets().sendProjectile(null, startTile, endTile, gfxId, startHeight, endHeight, speed, delay, curve, startOffset, 1);
        }
    }

    /**
     * Gets the Players Online amount.
     *
     * @return players online as Integer.
     */
    public static final int getPlayersOnline() {
        int count = 0;
        for (Player player : getPlayers()) {
            if (player != null && !player.isBot()) {
                count++;
            }
        }
        return count;
    }

    /**
     * P6: registers one native 947 character at the index {@code Native950World}
     * reserved before the login response was written. The world may already hold
     * other native characters; it may never hold a legacy one, because a legacy
     * player brings the 910 login/logout, network and world-thread hooks this JVM
     * does not run.
     *
     * <p>The index is supplied rather than assigned because {@code Ataraxia947Handoff}
     * puts it into {@code GameLoginResponse} before the Player exists, so
     * {@link EntityList#add} (which assigns {@code lowestFreeIndex + 1} at insert
     * time) cannot be the authority here.
     */
    public static void addNative950Player(final Player player, final int index) {
        if (player == null || !player.isNative950())
            throw new IllegalArgumentException("Expected a native 947 character");
        if (player.hasFinished())
            throw new IllegalStateException("Native 947 characters enter the world exactly once");
        if (index < 1 || index > NATIVE_947_MAX_PLAYER_INDEX)
            throw new IllegalArgumentException("Native 947 player index must fit the verified PLAYER_INFO range 1.."
                    + NATIVE_947_MAX_PLAYER_INDEX);
        for (final Player other : players)
            if (other != null && !other.isNative950())
                throw new IllegalStateException("The modern world must not share a legacy player loop");
        if (!players.addAt(player, index))
            throw new IllegalStateException("Native 947 player slot " + index + " is already occupied");
        playerMap.put(player.getUsername(), player);
    }

    /** Native characters are ephemeral; no 910 logout/save or network hooks run. */
    public static void removeNative950Player(final Player player) {
        if (!player.isNative950())
            throw new IllegalArgumentException("Expected a native 947 character");
        if (player.getLastRegionId() > 0)
            getRegion(player.getLastRegionId()).removePlayerIndex(player.getIndex());
        if (players.contains(player))
            players.remove(player);
        playerMap.remove(player.getUsername(), player);
        player.setRunning(false);
        player.setActive(false);
        player.setFinished(true);
        player.resetWalkSteps();
    }

    /**
     * Gets the Players Online amount.
     *
     * @return players online as Integer.
     */
    public static final int getAfkPlayersOnline() {
        int count = 0;
        for (Player player : getPlayers()) {
            if (player != null && player.isAFK()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Checks for the day of the week.
     *
     * @return dayOfWeek the weekday to return.
     */
    private static int dayOfWeek() {
        final Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.DAY_OF_WEEK);
    }

    /**
     * Checks if it's the weekend.
     *
     * @return theDayOfWeek.
     */
    public static boolean isWeekend() {
        return dayOfWeek() == 1 || dayOfWeek() == 6 || dayOfWeek() == 7;
    }

    /**
     * Finds an NPC in the world by its NPC ID.
     *
     * @param id The ID to find.
     * @return The NPC.
     */
    public static NPC findNPC(final int id) {
        NPC npc = null;
        for (final NPC n : getNPCs()) {
            if (n == null) {
                continue;
            }
            if (n.getId() == id) {
                npc = n;
            }
        }
        return npc;
    }

    /**
     * Finds the NPC by it's id.
     *
     * @param player The player searching.
     * @param id The NPC ID to search for.
     * @return If NPC found.
     */
    public static NPC findNPC(final Player player, final int id) {
        for (final NPC npc : World.getNPCs()) {
            if (npc == null || npc.getId() != id) {
                continue;
            }
            if (npc.getRegionId() == player.getRegionId()) {
                return npc;
            }
        }
        return null;
    }

    /**
     * Gets the real object by tile and slot.
     *
     * @param tile The worldtile.
     * @param slot The object slot.
     * @return the Object to return.
     */
    public static final WorldObject getRealObject(final WorldTile tile, final int slot) {
        return getRegion(tile.getRegionId()).getRealObject(tile.getPlane(), tile.getXInRegion(), tile.getYInRegion(), slot);
    }

    /**
     * Spawns a temporary world object.
     *
     * @param object The object to spawn.
     * @param time The time for it to stay.
     * @param checkObjectInstance checks if the object is in an instance.
     * @param checkObjectBefore checks before adding.
     */
    public static final void spawnObjectTemporary(final WorldObject object, final long time, final boolean checkObjectInstance, final boolean checkObjectBefore) {
        final WorldObject before = checkObjectBefore ? World.getObjectWithType(object, object.getType()) : null;
        spawnObject(object);
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                try {
                    if (checkObjectInstance && World.getObjectWithId(object, object.getId()) != object) {
                        return;
                    }
                    if (before != null) {
                        spawnObject(before);
                    } else {
                        removeObject(object); // this method allows to remove
                        // object with
                        // just tile and type actualy so
                        // the removing
                        // object may be diferent and
                        // still gets removed
                    }
                } catch (final Throwable e) {
                    Logger.getGlobal().catching(e);
                }
            }
        }, Utils.msToTicks(time));
    }

    /**
     * Sends the players online list on an interface.
     *
     * @param player The player to send the list to.
     */
    public static void playersList(final Player player, boolean staffOnly) {
        if (getPlayers().size() >= 308) {
            return;
        }
        player.getInterfaceManager().sendInterface(1245);
        for (int i = 0; i < 309; i++) {
            player.getPackets().sendIComponentText(1245, i, "");
        }
        int number = 0;
        for (final Player p5 : World.getPlayers()) {
            if (p5 == null || (staffOnly && p5.getRights() == 0 && !p5.isSupport())) {
                continue;
            }
            number++;
            String titles = "[<col=000000>Player</col>] ";
            final String color = (p5.getYellColor().equals("000000") || p5.getYellColor() == null ? "" : "<col=" + p5.getYellColor() + ">");
            if (p5.isLegendary()) {
                titles = "[<col=000000>Legendary</col>]";
            }
            if (p5.isKingOfTheSkillGameMode()) {
                titles = "[" + Colors.wrap(Colors.GREEN, "King of the Skill") + "]";
            }
            if (p5.isIronMan()) {
                titles = "[<col=000000>Legendary Ironman</col>] <img=24>";
            }
            if (p5.isHCIronMan()) {
                titles = "[<col=FF0000>HC Ironman</col>] <img=18>";
            }
            if (p5.isNoviceIronMan()) {
                titles = "[<col=000000>Novice Ironman</col>] <img=17>";
            }
            if (p5.isIntermediateIronMan()) {
                titles = "[<col=FF0000>Intermediate Ironman</col>] <img=22>";
            }
            if (p5.isExpertIronMan()) {
                titles = "[<col=000000>Expert Ironman</col>] <img=23>";
            }
            if (p5.isDonator()) {
                titles = "[" + color + "Bronze</col>] <img=15>";
            }
            if (p5.isExtremeDonator()) {
                titles = "[" + color + "Silver</col>] <img=16>";
            }
            if (p5.isLegendaryDonator()) {
                titles = "[" + color + "Gold</col>] <img=19>";
            }
            if (p5.isSupremeDonator()) {
                titles = "[" + color + "Platinum</col>] <img=20>";
            }
            if (p5.isUltimateDonator()) {
                titles = "[" + color + "Diamond</col>] <img=21>";
            }
            if (p5.isDisplayPlayerOfTheMonthIcon() && p5.isPlayerOfTheMonth()) {
                titles = "[" + color + "Player of the Month</col>] <img=25>";
            }
            if (p5.isSupport()) {
                titles = "[" + color + "Support</col>] <img=23>";
            }
            if (p5.getRights() == 1) {
                titles = "[" + color + "Moderator</col>] <img=0>";
            }
            if (p5.getRights() == 2) {
                titles = "[" + color + "Administrator</col>] <img=1>";
            }
            if (p5.getUsername().equalsIgnoreCase("zcdot")) {
                titles = "[" + color + "Co-Owner</col>] <img=34>";
            }
            if (p5.isDev()) {
                titles = "[" + color + "Developer</col>] <img=25>";
            }
            if (p5.getUsername().equalsIgnoreCase("mordris")) {
                titles = " <col=000000>Owner</col>] <img=9>";
            }
            if (p5.getUsername().equalsIgnoreCase("ponix_killer")) {
                    titles = " <col=000000>Owner</col>] <img=9>";
            }
            if (p5.getUsername().equalsIgnoreCase("xhybrid")) {
                titles = "[<col=000000>Owner</col>] <img=1>";
            }
            if (p5.isGroupIronman()) {
                titles = "[" + Colors.DEF_SEARCH_CYAN + "Group Ironman</col>] <img=33>";
            }
            player.getPackets().sendIComponentText(1245, (12 + number), titles + p5.getDisplayName() + "</col></shad> " + "[Lv. " + p5.getSkills().getCombatLevelWithSummoning() + "]");
        }
        player.getPackets().sendRunScript(17037, World.getPlayersOnline() + 2);
        player.getPackets().sendIComponentText(1245, 330, "<shade=B00000><u=000080>Ataraxia " + (staffOnly ? "players" : "staff") + "</u>");
        player.getPackets().sendIComponentText(1245, 10, staffOnly ? "" : "Players online: " + World.getPlayersOnline() + " [" + World.getPlayers().size() + "].");
    }

    /**
     * Sends a Message to all Players online.
     *
     * @param message The message to send.
     * @param me The Player sending the Message.
     */
    public static void sendWorldYellMessage(final String message, final Player me) {
        for (final Player player : World.getPlayers()) {
            if (player == null || !player.isRunning() || !Settings.yellEnabled()) {
                continue;
            }
            if (!me.isStaff()) {
                if (player.getFriendsIgnores().getIgnores().contains(me.getUsername()) || (player != me && player.isYellOff())) {
                    continue;
                }
            }
            player.getPackets().sendGameMessage(message);
        }
    }

    public static int getWellAmount() {
        return wellAmount;
    }

    public static void setWellAmount(final int amount) {
        wellAmount = amount;
    }

    public static void addWellAmount(final String displayName, final int amount) {
        wellAmount += amount;
        if (wellAmount < Settings.WELL_MAX_AMOUNT && amount >= 10000000) {
            sendWorldMessage("<col=FF0000>" + displayName + " " + "has contributed " + NumberFormat.getNumberInstance(Locale.US).format(amount) + " GP to the Globe of Goodwill!", false);
        }
    }

    public static void resetWell() {
        wellAmount = 0;
        sendWorldMessage(Colors.SHAD + Colors.LPURPLE + "The Globe of Goodwill has been reset.", false);
    }

    public static boolean isWellActive() {
        return wellActive;
    }

    public static void setWellActive(final boolean wellActive) {
        World.wellActive = wellActive;
    }

    public static final void spawnTemporaryDivineObject(final WorldObject object, final long time, final Player player) {
        spawnObject(object);
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                try {
                    if (!World.isSpawnedObject(object)) {
                        return;
                    }
                    removeObject(object);
                } catch (final Throwable e) {
                    Logger.getGlobal().catching(e);
                }
            }
        }, Utils.msToTicks(time));
    }

    public static final WorldObject getObject(final WorldTile tile) {
        return getRegion(tile.getRegionId()).getStandartObject(tile.getPlane(), tile.getXInRegion(), tile.getYInRegion());
    }

    public static final boolean removeObjectTemporary(final WorldObject object, final long time) {
        removeObject(object);
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                try {
                    spawnObject(object);
                } catch (final Throwable e) {
                    Logger.getGlobal().catching(e);
                }
            }
        }, Utils.msToTicks(time));
        return true;
    }

    /**
     * Checks if the player is at the Ascensiopns Dungeon.
     *
     * @param player The player to check for.
     * @return if is in Monastery of Ascension.
     */
    public static boolean isAtAscensionDungeon(final Player player) {
        final int destX = player.getX(), destY = player.getY();
        return /* South West */(destX >= 999 && destY >= 558 &&
                /* North East */destX <= 1209 && destY <= 765);
    }

    /**
     * Checks if the player is at the Kuradal's Dungeon.
     *
     * @param player The player to check for.
     * @return if is in Kuradal's Dungeon.
     */
    public static boolean isAtKuradalsDungeon(final Player player) {
        final int destX = player.getX(), destY = player.getY();
        return /* South West */(destX >= 1591 && destY >= 5241 &&
                /* North East */destX <= 1672 && destY <= 5337);
    }

    public static final void spawnObject(final WorldObject object, final boolean clip) {
        getRegion(object.getRegionId()).spawnObject(object, object.getPlane(), object.getXInRegion(), object.getYInRegion(), false);
    }

    public static void removeProjectiles() {
        synchronized (getRegions()) {
            for (final Region region : getRegions().values()) {
                region.removeProjectiles();
            }
        }
    }

    public static final Projectile sendProjectileNew(final WorldTile from, final WorldTile to, final int graphicId, final int startHeight, final int endHeight, final int startTime, final double speed, final int angle, final int slope) {
        return sendProjectile(from, to, false, false, 0, graphicId, startHeight, endHeight, startTime, speed, angle, slope);
    }

    public static final Projectile sendProjectile(final WorldTile from, final WorldTile to, final boolean adjustFlyingHeight, final boolean adjustSenderHeight, final int senderBodyPartId, final int graphicId, final int startHeight, final int endHeight, final int startTime, final double speed, final int angle, final int slope) {
        int fromSizeX, fromSizeY;
        if (from instanceof Entity) {
            fromSizeX = fromSizeY = ((Entity) from).getSize();
        } else if (from instanceof WorldObject) {
            final ObjectDefinitions defs = ((WorldObject) from).getDefinitions();
            fromSizeX = defs.getSizeX();
            fromSizeY = defs.getSizeY();
        } else {
            fromSizeX = fromSizeY = 1;
        }
        int toSizeX, toSizeY;
        if (to instanceof Entity) {
            toSizeX = toSizeY = ((Entity) to).getSize();
        } else if (to instanceof WorldObject) {
            final ObjectDefinitions defs = ((WorldObject) to).getDefinitions();
            toSizeX = defs.getSizeX();
            toSizeY = defs.getSizeY();
        } else {
            toSizeX = toSizeY = 1;
        }

        final Projectile projectile = new Projectile(from, to, adjustFlyingHeight, adjustSenderHeight, senderBodyPartId, graphicId, startHeight, endHeight, startTime, startTime + (speed == -1 ? Utils.getProjectileTimeSoulsplit(from, fromSizeX, fromSizeY, to, toSizeX, toSizeY) : Utils.getProjectileTimeNew(from, fromSizeX, fromSizeY, to, toSizeX, toSizeY, speed)), slope, angle);
        if (CoresManager.isNative950() && !adjustFlyingHeight && !adjustSenderHeight && senderBodyPartId == 0
                && !(from instanceof WorldObject) && !(to instanceof WorldObject))
            com.rs.game.player.client.Native950World.getInstance().queueProjectile(projectile);
        else getRegion(from.getRegionId()).addProjectile(projectile);
        return projectile;
    }

    /**
     * Throws a party!
     */
    public static void edelarParty() {
        final String message = "<col=00CCCC>#" + Settings.SERVER_NAME;
        final int total = isWeekend() ? 700 : 500;
        for (final Player players : World.getPlayers()) {
            if (players == null) {
                continue;
            }
            players.setNextAnimation(new Animation(9098));
            players.setNextGraphics(new Graphics(92));
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    players.setNextForceTalk(new ForceTalk(message));
                    stop();
                }
            }, 4);
        }
        for (final NPC npc : World.getNPCs()) {
            if (npc == null) {
                continue;
            }
            npc.setNextGraphics(new Graphics(92));
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    npc.setNextForceTalk(new ForceTalk(message));

                    /** Reset votes & Parties at 700 **/
                    if (VoteManager.VOTES >= total) {
                        VoteManager.VOTES = 0;
                        VoteManager.PARTIES = 0;
                    }

                    stop();
                }
            }, 4);
        }
    }

    public static String getLastVoter() {
        return lastVoter;
    }

    public static void setLastVoter(final String voter) {
        lastVoter = voter;
    }

    public static final Player get(final int index) {
        for (final Player player : getPlayers()) {
            if (player == null) {
                continue;
            }
            if (player.getIndex() == index) {
                return player;
            }
        }
        return null;
    }

    public static void setLoginServerConnected(final boolean b) {
        // TODO Auto-generated method stub

    }

    public static final void sendProjectile(final NewProjectile projectile) {
        for (final Player p : getPlayers()) {
            if (p == null || !p.withinDistance(projectile.getFrom(), 20)) {
                continue;
            }
            p.getPackets().sendTestProjectile(projectile);
        }
    }

    public static final FloorItem addWeaponPiece(final Item item, final WorldTile tile) {
        final int regionId = tile.getRegionId();
        final FloorItem floorItem = new FloorItem(item, tile, null, false, false);
        final Region region = getRegion(tile.getRegionId());
        region.getGroundItemsSafe().add(floorItem);
        for (final Player player : getPlayers()) {
            if (player == null || !player.isActive() || player.hasFinished() || !player.getMapRegionsIds().contains(regionId)) {
                continue;
            }
            player.getPackets().sendGroundItem(floorItem);
        }
        return floorItem;
    }

    public static final int WORLD_NEWS = 0, SERVER_NEWS = 1, FRIEND_NEWS = 2, GAME_NEWS = 3;

    public static void sendNews(String message, int type) {
        sendNews(null, message, type); // dont use type 2(FRIEND_NEWS) with this
        // one
    }

    /*
     * 0 - all worlds 1 - just this world 2 - friend 3 - game news
     */
    public static void sendNews(Player from, String message, int type) {
        String m = "<shad=000>News: " + message + "</shad></col>";
        if (type == 0)
            m = "<img=6><col=D80000>" + m;
        else if (type == 1)
            m = "<img=5><col=ff8c38>" + m;
        else if (type == 2)
            m = "<img=5><col=45b247>" + m;
        else if (type == 3)
            m = "<img=6><col=FFFF00>" + m;

        for (Player p : World.getPlayers()) {
            if (p == null || !p.isRunning() || (type == 2 && p != from && !p.getFriendsIgnores().getFriends().contains(from.getUsername())))
                continue;
            p.getPackets().sendGameMessage(m, false);
        }
    }

    public static final Projectile sendProjectileCycles(WorldTile from, WorldTile to, int graphicId, int startHeight, int endHeight, int startTime, int endTime, int angle, int slope) {
        Projectile projectile = new Projectile(from, to, false, false, 0, graphicId, startHeight, endHeight, startTime, endTime, slope, angle);
        projectile.setNewProjectile(true);
        getRegion(from.getRegionId()).addProjectile(projectile);
        return projectile;
    }

    public static void sendStillProjectile(WorldTile from, WorldTile to, Entity fromE, Entity toE, int graphicId, int startHeight, int endHeight, int startTime, int endTime) {
        for (int playerIndex : getRegion(from.getRegionId()).getPlayerIndexes()) {
            Player player = players.get(playerIndex);
            if (player == null || player.isDead() || !player.withinDistance(from, 25))
                continue;
            player.getPackets().sendStillProjectileNew(from, 1, 1, to, 1, 1, toE, graphicId, startHeight, endHeight, startTime, endTime);
        }
    }

    private static void addRotaionChangeTask() {
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                try {
                    VoragoInstance.checkChangeRotation();
                    SpiderBossInstance.checkChangeRotation();
                } catch (final Throwable e) {
                    Logger.getGlobal().catching(e);
                }
            }
        }, 2, 2);
    }
}
