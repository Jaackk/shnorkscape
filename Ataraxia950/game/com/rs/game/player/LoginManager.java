package com.rs.game.player;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.rs.Settings;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.activites.BountyHunter;
import com.rs.game.activites.gim.GIM;
import com.rs.game.activites.gim.GIMPrestigeManager;
import com.rs.game.activites.gim.bank.GIMBankManager;
import com.rs.game.activites.gim.highscores.GIMScoreTracker;
import com.rs.game.activites.multiboss.Multiboss;
import com.rs.game.activites.quest.QuestHandler;
import com.rs.game.activities.seasonalevents.SeasonalEventManager;
import com.rs.game.activities.seasonalevents.christmas.ChristmasSeasonalEvent;
import com.rs.game.item.Item;
import com.rs.game.item.floor.LootBeamManager;
import com.rs.game.player.actions.ActionManager;
import com.rs.game.player.actions.hunter.JadinkoCatch;
import com.rs.game.player.client.ClientProfile;
import com.rs.game.player.client.ClientSession;
import com.rs.game.player.content.*;
import com.rs.game.player.content.achievementsystem.AchievementList;
import com.rs.game.player.content.achievementsystem.Achievements;
import com.rs.game.player.content.artisansworkshop.ArtisansWorkShop;
import com.rs.game.player.content.clans.ClansManager;
import com.rs.game.player.content.construction.House;
import com.rs.game.player.content.contracts.ContractHandler;
import com.rs.game.player.content.contracts.ReaperPerks;
import com.rs.game.player.content.death.DeathItemsManager;
import com.rs.game.player.content.death.DeathManager;
import com.rs.game.player.content.death.Gravestone;
import com.rs.game.player.content.distinctioncape.DistinctionCape;
import com.rs.game.player.content.dropcollection.DropCollectionHandler;
import com.rs.game.player.content.dungeoneering.DungManager;
import com.rs.game.player.content.dungeoneering.DungeonManager;
import com.rs.game.player.content.dungeoneering.DungeoneeringToolbelt;
import com.rs.game.player.content.dungeoneering.RingOfKinship;
import com.rs.game.player.content.dungeoneering.journals.DungeoneeringJournals;
import com.rs.game.player.content.ectofuntus.Ectophial;
import com.rs.game.player.content.eds.EliteDungeon;
import com.rs.game.player.content.eds.EliteDungeonsManager;
import com.rs.game.player.content.grandExchange.GrandExchangeManager;
import com.rs.game.player.content.interfaces.Starter.ExpMode;
import com.rs.game.player.content.interfaces.Starter.GameMode;
import com.rs.game.player.content.interfaces.clientsettings.PlayerGameSettings;
import com.rs.game.player.content.interfaces.keybinds.KeyBindManager;
import com.rs.game.player.content.interfaces.teleport.TeleportInterface;
import com.rs.game.player.content.items.MysteryBox;
import com.rs.game.player.content.items.PrayerBooks;
import com.rs.game.player.content.items.ShardsBag;
import com.rs.game.player.content.jujupotions.JujuPotionManager;
import com.rs.game.player.content.jujupotions.harmonypillar.HarmonyPillarManager;
import com.rs.game.player.content.jujupotions.vineherbpatch.VineHerbPatchManager;
import com.rs.game.player.content.maxguild.combatportal.BossPortal;
import com.rs.game.player.content.maxguild.combatportal.BossPortalType;
import com.rs.game.player.content.pet.PetManager;
import com.rs.game.player.content.polls.PollManager;
import com.rs.game.player.content.presets.PresetHandler;
import com.rs.game.player.content.skillingcontracts.SkillingContractManager;
import com.rs.game.player.content.skillingcontracts.SkillingContractTracker;
import com.rs.game.player.content.slayer.CooperativeSlayer;
import com.rs.game.player.content.xmas.XmasEvent;
import com.rs.game.player.cutscenes.CutscenesManager;
import com.rs.game.player.dialogue.DialogueManager;
import com.rs.game.player.dialogue.impl.BankList;
import com.rs.game.player.dialogue.impl.StarterTutorialD;
import com.rs.game.player.security.pin.AccountPin;
import com.rs.game.player.skills.SkillsListener;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.network.packet.PacketDispatcher;
import com.rs.network.packet.PacketRepository;
import com.rs.utils.*;
import com.rs.utils.mysql.QueryExecutor;
import com.rs.utils.mysql.impl.AdReferralCheck;
import com.rs.utils.web.PlayersOnlineManager;
import io.netty.channel.Channel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.rs.game.player.content.interfaces.Starter.StarterInterface;



/**
 * Handles player login.
 *
 * @author Noel
 */
public class LoginManager {

    private static final SkillsListener SKILLS_LISTENER = new SkillsListener();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, uuuu");

    public static void initLobby(final Player player, final Channel channel, final String username, final String mac, final int displayMode, final boolean usingNXT, final IsaacKeyPair isaacKeyPair) {
        initLobby(player, channel, username, mac, displayMode, usingNXT, isaacKeyPair, ClientProfile.configured());
    }

    public static void initLobby(final Player player, final Channel channel, final String username, final String mac, final int displayMode, final boolean usingNXT, final IsaacKeyPair isaacKeyPair, final ClientProfile profile) {
        player.bindClientSession(ClientSession.beginLegacyGameplay(profile));
        World.addPlayerLobby(username);
        player.setLastPacketReceivedTime(Utils.currentTimeMillis());
        player.setRealChannel(channel);
        player.username = username;
        player.displayMode = displayMode;
        player.setCurrentMac(mac);
        player.setUsingNXT(usingNXT);
        player.isaacKeyPair = isaacKeyPair;
        player.interfaceManager = new InterfaceManager(player);
        player.dispatcher = new PacketDispatcher(player);
        player.repository = new PacketRepository(player);
    }

    public static void init(final Player player, final Channel channel, final String username, final String mac, final int displayMode, final boolean usingNXT, final IsaacKeyPair isaacKeyPair) {
        init(player, channel, username, mac, displayMode, usingNXT, isaacKeyPair, ClientProfile.configured());
    }

    public static void init(final Player player, final Channel channel, final String username, final String mac, final int displayMode, final boolean usingNXT, final IsaacKeyPair isaacKeyPair, final ClientProfile profile) {
        player.bindClientSession(ClientSession.beginLegacyGameplay(profile));
        World.removePlayerLobby(username);
        player.setLastPacketReceivedTime(Utils.currentTimeMillis());
        player.setRealChannel(channel);
        player.username = username;
        player.displayMode = displayMode;
        player.setCurrentMac(mac);
        player.setUsingNXT(usingNXT);
        player.isaacKeyPair = isaacKeyPair;
        if (player.getObtainedPets() == null)
            player.setObtainedPets(new ArrayList<>());
        if (player.getObtainedPetPerks() == null)
            player.setObtainedPetPerks(new ArrayList<>());
        if (player.getReaperPerks() == null)
            player.setReaperPerks(new ArrayList<ReaperPerks>());
        if (player.getDropCollectionHandler() == null)
            player.setDropCollectionHandler(new DropCollectionHandler(player));
        // only for development
        if (player.getPresetHandler() == null)
            player.setPresetHandler(new PresetHandler((player)));
        if (player.getTeleportInterface() == null)
            player.setTeleportInterface(new TeleportInterface(player));
        if (player.dayOfWeekManager == null)
            player.dayOfWeekManager = new DayOfWeekManager();
        if (player.getNotes() == null) {
            player.notesL = new Notes();
        }
        if (player.hunterCatch == null)
            player.hunterCatch = new HashMap<>(5);
        if (player.getGameSettings() == null)
            player.setGameSettings(new HashMap<PlayerGameSettings, Boolean>());
        if (player.getContracts() == null) {
            player.contracts = new SkillingContractManager(player);
        } else {
            player.contracts.player = player;
        }
        if (player.geManager == null) {
            player.geManager = new GrandExchangeManager();
        }
        if (player.squealOfFortune == null) {
            player.squealOfFortune = new SquealOfFortune();
        }
        if (player.prayerBook == null) {
            player.prayerBook = new boolean[PrayerBooks.BOOKS.length];
        }
        if (player.boons == null) {
            player.boons = new boolean[12];
        }
        if (player.accountPin == null) {
            player.accountPin = new AccountPin(player);
        } else {
            player.accountPin.setPlayer(player);
        }
        if (player.prestigeManager == null) {
            player.prestigeManager = new GIMPrestigeManager(player);
        } else {
            player.prestigeManager.setPlayer(player);
        }
        if (player.quests == null) {
            player.quests = new QuestHandler();
        }
        if(player.onlineTracker == null) {
            player.onlineTracker = Stopwatch.createStarted();
        }
        player.quests.init(player);
        if (player.questionedNpcs == null) {
            player.questionedNpcs = new HashSet<>();
        }
        if (player.overrides == null) {
            player.overrides = new CosmeticOverrides();
        }
        if (player.mauledWeeksNM == null) {
            player.mauledWeeksNM = new boolean[6];
        }
        if (player.mauledWeeksHM == null) {
            player.mauledWeeksHM = new boolean[6];
        }
        if (player.animations == null) {
            player.animations = new AnimationOverrides();
        }
        if (player.getGemstoneArmour() == null) {
            player.setGemstoneArmour();
        }
        if (player.unlockedCostumesIds == null) {
            player.unlockedCostumesIds = new ArrayList<Integer>();
        }
        if (player.jujuPotions == null) {
            player.jujuPotions = new JujuPotionManager();
        }
        player.jujuPotions.setPlayer(player);
        if (player.harmonyPillars == null) {
            player.harmonyPillars = new HarmonyPillarManager();
        }
        player.harmonyPillars.setPlayer(player);

        if (player.vineHerbPatches == null) {
            player.vineHerbPatches = new VineHerbPatchManager();
        }
        player.vineHerbPatches.setPlayer(player);
        if (player.ports == null) {
            player.ports = new PlayerOwnedPort();
        }

        if (player.xmas == null) {
            player.xmas = new XmasEvent();
        }
        if (player.multiboss == null) {
            player.multiboss = new Multiboss();
        }
        if (player.artisansWorkShop == null) {
            player.artisansWorkShop = new ArtisansWorkShop();
        }
        if (player.getPlayerExamineManager() == null)
            player.setPlayerExamineManager(new PlayerExamineManager());
        if (player.getDungeoneeringBinds() == null) {
            player.setDungeoneeringBinds();
        }
        if (player.getGorajanTrailblazer() == null) {
            player.resetGorajanTrailblazer();
        }
        if (player.getSophanemChestLoot() == null) {
            player.setSophanemChest();
        }
        if (player.getInteractedNoncombatPets() == null) {
            player.setInteractedNoncombatPets();
        }
        if (!player.resetheart) {
            player.setHeartofGielinor();
            player.resetheart = true;
        }
        if (player.getInventionManager() == null)
            player.setInventionManager(new InventionManager());
        //if (player.getTreasureHunter() == null)
           // player.setTreasureHunter(new TreasureHunter());
        if (player.getFavoriteSkillingTeleports() == null) {
            player.setFavoriteSkillingTeleports(new ArrayList<>());
        }
        player.gimTracker = new GIMScoreTracker(player);
        player.interfaceManager = new InterfaceManager(player);
        player.dialogueManager = new DialogueManager(player);
        player.hintIconsManager = new HintIconsManager(player);
        player.priceCheckManager = new PriceCheckManager(player);
        player.localPlayerUpdate = new LocalPlayerUpdate(player);
        player.localNPCUpdate = new LocalNPCUpdate(player);
        player.actionManager = new ActionManager(player);
        player.gimBank = new GIMBankManager(player);
        player.cutscenesManager = new CutscenesManager(player);
        player.dispatcher = new PacketDispatcher(player);
        player.repository = new PacketRepository(player);
        player.shSkillingCooldown = Stopwatch.createUnstarted();
        player.shCombatCooldown = Stopwatch.createUnstarted();
        player.pollAnswers = new ArrayList<>();
        player.groupSearchThrottle = Stopwatch.createUnstarted();
        player.pickTaskThrottle = Stopwatch.createUnstarted();
        if (player.pouch == null) {
            player.pouch = new MoneyPouch();
        }

        if (player.ectophial == null) {
            player.ectophial = new Ectophial(player);
        }

        if (player.deathItemsManager == null) {
            player.deathItemsManager = new DeathItemsManager(player);
        } else {
            player.deathItemsManager.setPlayer(player);
        }

        if (player.getBossPortal() == null) {
            player.setBossPortal(BossPortalType.UNDIRECTED);
        }

        if (player.dailyManager == null) {
            player.dailyManager = new DailyManager();
        }

        if (player.skills == null) {
            player.skills = new Skills();
        }
        if (player.petManager == null) {
            player.petManager = new PetManager();
        }
        if (player.auraManager == null) {
            player.auraManager = new AuraManager();
        }
        if (player.getActivityTimersManager() == null)
            player.setActivityTimersManager(new ActivityTimersManager());
        if (player.dropCatcherFilter == null) {
            player.dropCatcherFilter = new HashSet<>();
        }
        if (player.recordedVotes == null) {
            player.recordedVotes = ArrayListMultimap.create();
        }
        if (player.getPetPerkManager() == null) {
            player.setPetPerkManager(new PetPerkManager());
        }
        if (player.VBM == null) {
            player.VBM = new VarBitManager(player);
        }
        if (player.house == null) {
            player.house = new House();
        }
        player.coOpSlayer = new CooperativeSlayer();
        if (player.dominionTower == null) {
            player.dominionTower = new DominionTower();
        }
        player.loyaltyManager = new LoyaltyManager(player);
        if (player.getHeart() == null) {
            player.setHeartofGielinor();
        }
        if (player.getAppearence() == null) {
            player.setGlobalPlayerUpdater(new GlobalPlayerUpdater());
        }
        if (player.farmingManager == null) {
            player.farmingManager = new FarmingManager();
        }
        if (player.getToolBelt() == null) {
            player.toolBelt = new Toolbelt(player);
        }
        if (player.getToolBeltNew() == null) {
            player.toolBeltNew = new ToolbeltNew(player);
        }
//        if (player.depreciatedAchievementManager == null) {
//			player.depreciatedAchievementManager = new DepreciatedAchievement(player);
//		}
        if (player.getTitles() == null) {
            player.titles = new Titles();
        }
        if (player.perkManager == null) {
            player.perkManager = new PerkManager();
        }
        if (player.getLootBeamManager() == null) {
            player.setLootBeamManager(new LootBeamManager());
        }
        if (player.treasureTrails == null) {
            player.treasureTrails = new TreasureTrails();
        }
        if (player.bountyHunter == null) {
            player.bountyHunter = new BountyHunter();
        }

        if (player.getGemBag() == null) {
            player.setGemBag();
        } else {
            player.getGemBag().setPlayer(player);
        }

        if (player.getVisWaxManager() == null) {
            player.setVisWaxManager(new VisWaxManager());
        }

        if (player.getLottery() == null) {
            player.setLottery();
        }

        if (player.getBanks() == null) {
            player.setBanks(new ArrayList<Bank>());
        }




        if (player.getClaimedDonationAwardBoxes() == null) {
            player.setClaimedDonationAwardBoxes(new boolean[6]);
        }
        if (player.getAchievements() == null)
            player.setAchievements(new Achievements());

        if (player.dicingManager == null)
            player.dicingManager = new DicingManager();

        /*
         * if (player.getSlayer() == null) player.setSlayer(new Slayer());
         */

        if (player.getShardsList() == null) {
            player.setShardsList(new HashMap<Integer, Boolean>());
            ShardsBag.shardMapInit(player);
        }

        if (player.getSmallRunePouch() == null) {
            player.setSmallRunePouch(new HashMap<Integer, Item>());
        }

        if (player.getBigRunePouch() == null) {
            player.setBigRunePouch(new HashMap<Integer, Item>());
        }

        if (player.getBannedTasks() == null) {
            player.setBannedTasks(new ArrayList<Integer>());
        }

        if (player.getHome() == null || player.getHomeName() == null) {
            player.setHome(Settings.RESPAWN_PLAYER_LOCATION, "Home");
        }

        if (player.uniqueItems == null) {
            player.uniqueItems = new HashMap<Integer, Integer>();
        }
        if (player.getActivePotions() == null) {
            player.setActivePotions(new ArrayList<>());
            player.setShowPotionTimers(true);
        }
        if (player.getChargesManagerNew() == null)
            player.setChargesManagerNew(new ChargesManagerNew());
        if (player.getEliteDungeonsManager() == null)
            player.setEliteDungeonsManager(new EliteDungeonsManager());
        if (player.getActionbar() == null)
            player.setActionbar(new ActionBar());
        if (player.getSubMenus() == null)
            player.setSubMenus(new int[12]);
        if (player.getDoomsayerManager() == null)
            player.setDoomsayerManager(new DoomsayerManager());
        if (player.getTrimmedCompletionistCapeCustomized() == null)
            player.setTrimmedCompletionistCapeCustomized(Arrays.copyOf(ItemDefinitions.getItemDefinitions(20771).originalModelColors, 4));
        if (player.getCapeCustomizationPresets() == null) {
            player.setCapeCustomizationPresets(new int[3][4]);
            for(int i=0;i<player.getCapeCustomizationPresets().length;i++)
                player.getCapeCustomizationPresets()[i] = Arrays.copyOf(ItemDefinitions.getItemDefinitions(20771).originalModelColors, 4);
        }
        if (player.getCosmeticsManager() == null)
            player.setCosmeticsManager(new CosmeticsManager());
        if (player.getBuffDebuffTimersManager() == null)
            player.setBuffDebuffTimersManager(new BuffDebuffTimersManager());
        player.getBuffDebuffTimersManager().setPlayer(player);
        player.getCosmeticsManager().setPlayer(player);
        player.getActionbar().setPlayer(player);
        player.getDoomsayerManager().setPlayer(player);
        player.getEliteDungeonsManager().setPlayer(player);
        player.getChargesManagerNew().setPlayer(player);
        player.getInventionManager().setPlayer(player);
        KeyBindManager.setDefaultBinding(player);
        player.getAppearence().setPlayer(player);
        player.getVarBitManager().setPlayer(player);
        player.getInventory().setPlayer(player);
        //player.getTreasureHunter().setPlayer(player);
        player.getEquipment().setPlayer(player);
        player.getFarmingManager().setPlayer(player);
        player.getToolBelt().setPlayer(player);
        player.getGemstoneArmour().setPlayer(player);
        player.getToolBeltNew().setPlayer(player);
        player.getHouse().setPlayer(player);
        player.getSquealOfFortune().setPlayer(player);
        player.getSkills().setPlayer(player);
        player.getCombatDefinitions().setPlayer(player);
        player.getPrayer().setPlayer(player);
        player.getBank().setPlayer(player);
        player.getDungeoneeringBinds().setPlayer(player);
        player.getControlerManager().setPlayer(player);
        player.getTitles().setPlayer(player);
        //player.getSlayer().setPlayer(player);
        player.getOverrides().setPlayer(player);
        player.getAnimations().setPlayer(player);
        player.getDailyManager().setPlayer(player);
        player.getArtisansWorkShop().setPlayer(player);
        player.getGEManager().setPlayer(player);
        player.getHeart().setPlayer(player);
        player.getLottery().setPlayer(player);
        player.getMusicsManager().setPlayer(player);
        player.getDayOfWeekManager().setPlayer(player);
        player.getGorajanTrailblazer().setPlayer(player);
        player.getEmotesManager().setPlayer(player);
        player.getPorts().setPlayer(player);
        player.getXmas().setPlayer(player);
        player.getMulti().setPlayer(player);
        player.getFriendsIgnores().setPlayer(player);
        player.getDominionTower().setPlayer(player);
        player.getAuraManager().setPlayer(player);
        player.getActivityTimersManager().setPlayer(player);
        player.getPetPerkManager().setPlayer(player);
        player.getTreasureTrails().setPlayer(player);
        player.getPerkManager().setPlayer(player);
        player.getNotes().setPlayer(player);
        player.getCharges().setPlayer(player);
        player.getQuestManager().setPlayer(player);
        player.getVisWaxManager().setPlayer(player);
        player.getPetManager().setPlayer(player);
        player.getBountyHunter().setPlayer(player);
        player.getMoneyPouch().setPlayer(player);
        player.getTeleportInterface().setPlayer(player);
        player.getDropCollectionHandler().setPlayer(player);
        player.getPlayerExamineManager().setPlayer(player);
        if (player.isKingOfTheSkillGameMode()) {
            player.initializeKingOfTheSkillGameModeVariables();
        }
        if (player.getDungeoneeringToolbelt() == null) {
            player.setDungeoneeringToolbelt(new DungeoneeringToolbelt(player));
        }
        player.getDungeoneeringToolbelt().setPlayer(player);
        if (player.getDungeoneeringJournals() == null) {
            player.setDungeoneeringJournals(new DungeoneeringJournals(player));
        }
        player.getDungeoneeringJournals().setPlayer(player);
        if (player.getRingOfKinship() == null) {
            player.setRingOfKinship(new RingOfKinship(player));
        }
        player.getRingOfKinship().setPlayer(player);
        player.getAchievements().setPlayer(player);
        player.dicingManager.setPlayer(player);
        player.setDirection(Utils.getFaceDirection(0, -1));
        player.setDefaultVariables();
        player.fairyRingCombination = new int[3];
        player.warriorCheck();
        player.logicPackets = new ConcurrentLinkedQueue<>();
        player.setSwitchItemCache(Collections.synchronizedList(new ArrayList<>()));
        player.initEntity();
        World.addPlayer(player);
        player.setPacketsDecoderPing(Utils.currentTimeMillis());
        player.increaseAFKTimer();

        // checkSmuggles(player);
    }

    public static void initBot(final Player player, final String username) {
        init(player, null, username, "bot", 0, true, new IsaacKeyPair(new int[4]));
        player.getAccountPin().resetLocked();
        player.getAccountPin().setPinEntered();
        if (player.getDeathManager() == null) {
            player.setDeathManager(new DeathManager());
        }
        player.getDeathManager().setPlayer(player);
    }

    @SuppressWarnings("unused")
    private static final void checkSmuggles(final Player player) {
        for (final Bank bank : player.getBanks()) {
            for (final Item items : bank.getContainerCopy()) {
                if (items.getDefinitions().isBindItem()) {
                    bank.removeItem(items);
                }
            }
        }
    }

    private static void sendGameData(final Player player) {
        LoggingSystem.logIP(player);
        LoggingSystem.logAddress(player);
        player.farmingManager.init();
        // friend chat connect
        FriendChatsManager.joinChat("xhybrid1", player);
        player.getBank().init();
        // connect to current clan
        if (player.getClanName() != null) {
            if (!ClansManager.connectToClan(player, player.getClanName(), false)) {
                player.setClanName(null);
            }
        }

         player.getDailyManager().giveDailyTask();

        // respawn familiar
        if (player.getFamiliar() != null) {
            player.getFamiliar().respawnFamiliar(player);
        } else {
            player.getPetManager().init();
        }
        if (player.getDeathManager() == null) {
            player.setDeathManager(new DeathManager());
        }
        player.getDayOfWeekManager().init();
        player.getDeathManager().setPlayer(player);

        // Vecna timer
        player.vecnaTimer(player.getVecnaTimer());
        player.getBuffDebuffTimersManager().init();
        player.distinctionCape = new DistinctionCape(player);

        if (Settings.WORLDPVP) {
            player.setCanPvp(true);
            player.setSafePvp(true);
            player.sendMessage(Colors.RED + "[WORLD] " + Colors.GREEN + "Global safe-PvP is currently enabled!");
        }

        if (player.isOwner()) {
            player.setRights(2);
        }

        /**
         * Inits G.E. for the player.
         */
        player.getGEManager().init();

        /**
         * Refreshes the TOP Donation ranks.
         */






        // Refresh the money pouch.
        player.getMoneyPouch().login();
        player.getMoneyPouch().refresh();

        player.setTimeToNextMysterybox(MysteryBox.TICKS_TO_REWARD);

        /**
         * Refreshes the Notes.
         */
        player.getNotes().refresh();

        player.getActivityTimersManager().resetTimers();

        /**
         * Constructs the killstats.
         */
        if (player.killStats == null) {
            player.killStats = new int[512];
        }

        /**
         * Player-owned house.
         */
        player.house.init();
        if (!player.hasHouse) {
            player.hasHouse = true;
        }

        /**
         * Loyalty Manager.
         */
        player.getLoyaltyManager().startTimer();

        /**
         * Play time.
         */
        player.setRecordedPlayTime(Utils.currentTimeMillis());

        if (player.getBanks().size() == 0) {
            player.addBank(false, player.getBank());
        }

        /**
         * Reset thieving delay.
         */
        player.setThievingDelay(0);

        if (!player.addedEquipmentSlot) {
            player.getEquipment().resetEquipmentSlots();
        }


        if (SeasonalEventManager.isActive(ChristmasSeasonalEvent.class)) {
            player.sendMessage(Colors.DEF_SEARCH_CYAN + "Santa needs your help! You can find him at ;;home's upper level. Happy holidays!");
        }

        player.getActionbar().init();
        player.getAuraManager().init();
        player.getDoomsayerManager().init();
        player.unlockLodeStoneTeleportLocations();
        player.getVisWaxManager().refreshQuickTeleportCharges();
        player.getPlayerExamineManager().init();
        player.getCosmeticsManager().init();
        player.updateSlayerCounterInformation();
        player.refreshUnlockedRecipes();
        player.getToolBelt().init();
        player.getAchievements().init();
        player.getAchievements().getAchievements().put(AchievementList.REACH_10_HRS_PLAYTIME, 0);
        player.getAchievements().getAchievements().put(AchievementList.REACH_100_HRS_OF_PLAYTIME, 0);
        player.getAchievements().getAchievements().put(AchievementList.REACH_250_HRS_OF_PLAYTIME, 0);
        player.getAchievements().getAchievements().put(AchievementList.REACH_1000_HRS_PLAYTIME, 0);
        player.getAchievements().updateProgress(Utils.getHoursPlayed(player.getTimePlayed()), AchievementList.REACH_10_HRS_PLAYTIME, AchievementList.REACH_100_HRS_OF_PLAYTIME, AchievementList.REACH_250_HRS_OF_PLAYTIME, AchievementList.REACH_1000_HRS_PLAYTIME);

        /**
         * Temporary disable for invention / divination components
         */

        // player.getPackets().sendHideIComponent(320, 203, true);
        // player.getPackets().sendHideIComponent(320, 211, true);
    }

    public static void sendLogin(final Player player) {
        player.getClientProfile().requireLegacyGameplay("910 interface bootstrap");
        sendLoginVars(player);
        player.resetPlayerObjects();
        if (World.exiting_start != 0) {
            final int delayPassed = (int) ((Utils.currentTimeMillis() - World.exiting_start) / 1000);
            player.getPackets().sendSystemUpdate(World.exiting_delay - delayPassed);
        }
        if (player.getKillStatistics(93) > 0) {
            player.setSpokenToVorago(true);
        }
        player.sendMessage("Welcome to " + Settings.SERVER_NAME + ".");
        if (player.isKingOfTheSkillGameMode() || player.isGroupIronman()) {
            if (player.donorObjectBypassTime > 0) {
                player.donorObjectBypassTime = 0;
            }
        }
        player.resetImbuedGear();
        player.jujuPotions.onLogin();
        player.resetOldBarrowsGear();
        player.resetItemsAttributes();
        player.resetDupedItems();
        player.resetMinigameGear();
        /* Run an AdReferralCheck on every login */
        if (!player.isReferralChecked()) {
            QueryExecutor.submit(new AdReferralCheck(player));
        }

        /* Hardcoded check for lodestones, grants player all loadstones */
        if (!player.hasLodestones) {
            player.grantLodestones();
        }
        if (player.getContract() != null && !ContractHandler.ContractData.CONTRACT_FOR_ID.containsKey(player.getContract().getNpcId())) {
            player.setContract(null);
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    player.sendMessage(Colors.RED + "There was an update to reaper contracts recently, and as such your current contract has been reset because it is not supported by the new system.");
                }
            }, 5);
        }
//        if (player.getSquealOfFortune().getTotalSpins() < 0) {
//            player.sendMessage("Your squeal of fortune spins have been set from " + player.getSquealOfFortune().getTotalSpins() + " to zero.");
//            player.getSquealOfFortune().setBoughtSpins(0);
//            player.getSquealOfFortune().setDailySpins(0);
//            player.getSquealOfFortune().setEarnedSpins(0);
//        }

        /**
         * Barrows reload for sister
         */
        if (player.getKilledBarrowBrothers().length != 8) {
            player.resetBarrows();
            player.setHiddenBrother(Utils.random(8));
        }
        sendStaticConfigs(player);
        sendGameData(player);
        listenForSkills(player);

        if (World.isWeekend()) {
            //player.sendMessage("<img=7>Double experience and minigame weekend is currently: <shad=000000>" + Colors.GREEN + "Activated</col></shad>.");
        }

        //player.sendMessage(Colors.RED + "<shad=000000><img=6>Latest updates</col></shad>: " + Settings.LATEST_UPDATE);
        if (player.lsp <= 0) {
            player.lsp = 1;
        }
        if (player.DFMScroll == null) {
            player.DFMScroll = new boolean[14];
        }
        if (player.herbicideSettings == null) {
            player.herbicideSettings = new boolean[17];
        }
        if (player.bonecrusherSettings == null) {
            player.bonecrusherSettings = new boolean[17];
        }
        if (player.meilyrShopSettings == null) {
            player.meilyrShopSettings = new boolean[16];
        }
        if (player.meilyrShopSettings2 == null) {
            player.meilyrShopSettings2 = new boolean[10];
        }
        if (player.getSophanemChestLoot() == null) {
            player.setSophanemChest();
        }
        if (player.getDungeoneeringManager() == null) {
            player.setDungeoneeringManager(new DungManager(player));
        }
        if (player.getDungeoneeringJournals() == null) {
            player.setDungeoneeringJournals(new DungeoneeringJournals(player));
        }

        if (player.getRingOfKinship() == null) {
            player.setRingOfKinship(new RingOfKinship(player));
        }
        BossPortal.spawn(player);
        if (player.hasAttachedPyramidRope()) {
            player.getPackets().addSpawnedObject(new WorldObject(28488, 10, 1, 3382, 2824, 1));
            player.getPackets().addSpawnedObject(new WorldObject(28490, 10, 1, 3382, 2825, 0));
        }
        if (player.hasRepairedBurthorpePegs()) {
            player.getPackets().addSpawnedObject(new WorldObject(34904, 10, 0, 2935, 3558, 0));
        }
        if (player.getGemBag() == null) {
            player.setGemBag();
        } else {
            player.getGemBag().setPlayer(player);
        }

        if (!player.hasUpgradedBarrowsDefender() && (player.hasItem(new Item(36153)) || player.hasItem(new Item(36154)) || player.hasItem(new Item(36155)))) {
            player.setHasUpgradedBarrowsDefender();
        }
        player.getContracts().removeVirtualPoints();
        boolean isChampion = SkillingContractTracker.getSingleton().isChampion(player);
        if (player.getAppearence().getTitle() == 300 && !isChampion) {
            // player.sendMessage("Your " + Colors.RED + "Skilling Champion</col> title has
            // been removed.");
            player.getAppearence().setTitle(-1);
        }

        player.getDungeoneeringManager().setPlayer(player);

        if (player.getDungeoneeringToolbelt() == null) {
            player.setDungeoneeringToolbelt(new DungeoneeringToolbelt(player));
        }

        if (Settings.DEBUG || Settings.TEST_SERVER_MODE) {
            player.sendMessage("AnicentX Server Will Be Released May 21st");
        }
        if (player.isUnregisteredGIM()) {
            GIM.sendPlayerMsg(player, "You are currently groupless! Speak to the GIM guide to join another group.");
        }
        sendWorldLoginMessage(player);

        /**
         * Checks if player's wielding comp/max etc and unequips if requirements aren't
         * met.
         */
        player.getLottery().checkLogin();
        DistinctionCape.checkRequirementsUponLogin(player);

        /**
         * Items to do on first login or concurrently for every one after tags:
         * doonlogin login uponlogin do-on-login
         *
         */
        player.harmonyPillars.init();
        player.vineHerbPatches.init();

        if (!player.hasCompleted()) {
            player.getTemporaryAttributtes().put("ExpMode", ExpMode.NOVICE);
            player.getTemporaryAttributtes().put("GameMode", GameMode.NORMALE);

            StarterInterface.completeTutorial(player); // Marks tutorial complete
        }



        //player.getAccountPin().lock();
        /* Check and clear duo slayer configs */
        player.coOpSlayer.loginCheck(player);

        Gravestone.login(player);

        // if (player.getDailyManager().getTask() != null) {
        // player.getDailyManager().displayTask();
        // }

        if (com.rs.game.player.content.newlottery.Lottery.getSingleton().winnings.has(player)) {
            player.sendMessage(Colors.RED + "You won the lottery while you were logged out! See the lottery coordinator to claim your winnings.");
        }


        if (GIM.getEventManager().isRunning()) {
            GIM.sendPlayerMsg(player, "A '" + GIM.getEventManager().getEventDescription() + "' event is currently running until " + GIM.getEventManager().getFormattedEndDate() + ".");
        }
        SkillingContractTracker.getSingleton().login(player);
        Logger.getGlobal().info("Player {} has logged in, there are {} players on.", player.getUsername(), World.getPlayers().size());

        if (!player.hunterCatch.isEmpty()) {
            player.hunterCatch.values().removeIf(JadinkoCatch::expired);
        }
        PlayersOnlineManager.updatePlayersOnline();
        player.lootshareDelay = -1;
        DungeonManager.checkRejoin(player);
        EliteDungeon.checkRejoin(player);
        if (!player.hasUnlockedEFC()) {
            if (player.hasItem(new Item(31603, 1))) {
                player.setUnlockedEFC(true);
            }
        }
        player.getHeart().checkQuota();
        if (player.hasAuguryActivated()) {
            player.setAugury(false);
            player.getDungeoneeringManager().addTokens(153000);
            player.sendMessage("Your augury scroll has been transformed into 153,000 dungeoneering tokens.");
        }
        if (player.hasRigourActivated()) {
            player.setRigour(false);
            player.getDungeoneeringManager().addTokens(140000);
            player.sendMessage("Your rigour scroll has been transformed into 140,000 dungeoneering tokens.");
        }
        if (player.hasTormentActivated()) {
            player.setTorment(false);
            player.getDungeoneeringManager().addTokens(153000);
            player.sendMessage("Your torment scroll has been transformed into 153,000 dungeoneering tokens.");
        }
        if (player.hasAnguishActivated()) {
            player.setAnguish(false);
            player.getDungeoneeringManager().addTokens(140000);
            player.sendMessage("Your anguish scroll has been transformed into 140,000 dungeoneering tokens.");
        }
        player.unlockOldCosmetics();
        player.getTreasureTrails().replaceDisabledClue();
        if (!player.halfedTheMagisterKC) {
            player.killStats[136] = player.killStats[136] / 2;
            player.halfedTheMagisterKC = true;
        }
        try {
            if (!Settings.TEST_SERVER_MODE) {
                //World.getDiscordBot().getDiscordLinkage().updateRankInGame(player);
            }
        } catch (Exception e) {
            Logger.getGlobal().catching(e);
        }
        /*
         * if (player.getContributerTill() != 0) { if (player.hasContributerStatus()) {
         * player.getPackets().
         * sendGameMessage("<col=00ff00>Your contributor status expires " +
         * player.getContributerDaysLeftMessage()); } else { player.getPackets().
         * sendGameMessage("<col=FF0000>Your contributor status expired.");
         * player.getPackets().sendGameMessage("<col=FF0000>You have donated $"+
         * player.getContributionAmount() +
         * "/$"+Player.CONTRIBUTER_STATUS_THRESSHOLD+" ("+new
         * DecimalFormat("##.##").format((double)player.getContributionAmount() /
         * (double) Player.CONTRIBUTER_STATUS_THRESSHOLD)+"%)."); } } else {
         * player.getPackets().
         * sendGameMessage("<col=FF7F00>You can get contributor status after donating $"
         * +Player.CONTRIBUTER_STATUS_THRESSHOLD+". It will last 30 days.");
         * player.getPackets().sendGameMessage("<col=FF7F00>You have donated $"+
         * player.getContributionAmount() +
         * "/$"+Player.CONTRIBUTER_STATUS_THRESSHOLD+" ("+new
         * DecimalFormat("##.##").format(((double)player.getContributionAmount() *
         * 100.00) / (double) Player.CONTRIBUTER_STATUS_THRESSHOLD)+"%)."); }
         */
    }

    private static void listenForSkills(Player player) {
        SKILLS_LISTENER.listenForLogin(player);
    }

    private static void sendStaticConfigs(final Player player) {
        player.getPackets().sendConfig(1295, 1000);
        player.getPackets().sendConfigByFile(20940, 120);
        player.getPackets().sendConfigByFile(382, 11);
        player.getPackets().sendConfigByFile(9513, 1);
        player.getPackets().sendConfigByFile(14041, 200);
        player.getPackets().sendConfigByFile(39917, 98);
        player.getPackets().sendConfig(425, 1);
        player.getPackets().sendConfig(8569, 1);
        player.getPackets().sendConfig(8571, 1);
        player.getInventionManager().init();
        //player.getTreasureHunter().init();
        player.getInterfaceManager().sendInterfaces();
        player.getPackets().sendRunEnergy();
        player.refreshAllowChatEffects();
        player.refreshMouseButtons();
        player.refreshReportOption();
        player.sendRunButtonConfig();
        player.sendDefaultPlayersOptions();
        player.checkMultiArea();
        player.refreshAcceptAid();
        player.refreshProfanityFilter();
        player.getInventory().init();
        player.getEquipment().checkItems();
        player.getEquipment().init();
        player.getSkills().init();
        player.getCombatDefinitions().init();
        player.getPrayer().init();
        player.getFriendsIgnores().init();
        player.refreshHitPoints();
        player.getNotes().init();
        player.getEmotesManager().init();
        player.getPrayer().refreshPrayerPoints();
        player.getPoison().refresh();
        player.getPackets().sendGameBarStages();

        // Hide Treasure Hunter button
        // Hide Treasure Hunter chest icon
        player.getInterfaceManager().closeTreasureHunterOverlay();



        player.getQuestManager().init();
        player.getMusicsManager().init();
        player.sendUnlockedObjectConfigs();
        player.refreshLegacyInterfacesSkin();
        player.refreshGoldTrim99();
        // GPI
        player.setRunning(true);
        player.getAppearence().generateAppearenceData();
        player.getControlerManager().login();
        player.refreshToggleBuffTimer(true);
        OwnedObjectManager.linkKeys(player);
    }

    private static final ImmutableSet<String> LOGIN_NOTIFICATIONS_BLOCKED = ImmutableSet.of("lare96");

    private static void sendWorldLoginMessage(final Player player) {
        if (player.getRights() == 0 && !player.isSupport()) {
            return;
        }
        if (LOGIN_NOTIFICATIONS_BLOCKED.contains(player.getUsername())) {
            return;
        }
        World.sendWorldMessage(Colors.GOLD + player.getIcon() + player.getDisplayName() + " has logged on!", false);
    }

    private static void sendLoginVars(Player player) {
        player.getPackets().sendGlobalConfig(3698, 1);
        player.getPackets().sendGlobalConfig(987, 1);
        player.getPackets().sendGlobalConfig(1701, 1);
        player.getPackets().sendGlobalConfig(6042, 0);
        player.getPackets().sendGlobalConfig(5187, 1);
        player.getPackets().sendGlobalConfig(6503, 1);
        player.getPackets().sendGlobalConfig(1240, 4);
        player.getPackets().sendGlobalConfig(1277, 0);
        player.getPackets().sendGlobalConfig(3684, 0);
        player.getPackets().sendGlobalConfig(3708, 0);
        player.getPackets().sendGlobalConfig(3681, 1);
        player.getPackets().sendGlobalConfig(2771, 56495751);
        player.getPackets().sendGlobalConfig(178, 0);
        player.getPackets().sendGlobalConfig(6341, 1073741313);
        player.getPackets().sendGlobalConfig(6348, 0);
        player.getPackets().sendGlobalConfig(4738, 0);
        player.getPackets().sendGlobalConfig(1787, 1);
        player.getPackets().sendGlobalConfig(6040, 30);
        player.getPackets().sendGlobalConfig(6041, 13);
        player.getPackets().sendGlobalString(3683, player.getDisplayName());
        player.getPackets().sendGlobalConfig(1882, 0);
        player.getPackets().sendGlobalConfig(1274, 102);
        player.getPackets().sendGlobalConfig(1414, 1);
        player.getPackets().sendGlobalConfig(1788, -37);
        player.getPackets().sendGlobalConfig(1971, 5725);
        player.refreshToggleBuffTimer(true);
    }
}
