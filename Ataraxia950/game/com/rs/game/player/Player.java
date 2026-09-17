package com.rs.game.player;

import com.google.common.base.Stopwatch;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multiset;
import com.rs.Settings;
import com.rs.game.player.client.ClientProfile;
import com.rs.game.player.client.ClientSession;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.ObjectDefinitions;
import com.rs.cache.loaders.rs3.RS3GeneralRequirementMap;
import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.*;
import com.rs.game.Hit.HitLook;
import com.rs.game.activites.BountyHunter;
import com.rs.game.activites.clanwars.FfaZone;
import com.rs.game.activites.clanwars.WarControler;
import com.rs.game.activites.dnd.eviltree.EvilTree;
import com.rs.game.activites.dnd.eviltree.EvilTreeInstanceLevel;
import com.rs.game.activites.duel.DuelArena;
import com.rs.game.activites.duel.DuelRules;
import com.rs.game.activites.dungeon_architect.DungeonArchitectController;
import com.rs.game.activites.dungeon_architect.DungeonArchitectMonster;
import com.rs.game.activites.gambling.flowerpoker.FlowerPokerSession;
import com.rs.game.activites.gim.GIM;
import com.rs.game.activites.gim.GIMGroupKey;
import com.rs.game.activites.gim.GIMPrestigeManager;
import com.rs.game.activites.gim.bank.GIMBankManager;
import com.rs.game.activites.gim.highscores.GIMScoreTracker;
import com.rs.game.activites.multiboss.Multiboss;
import com.rs.game.activites.quest.QuestHandler;
import com.rs.game.activites.quest.deathsbounty.HusbandMichNPC;
import com.rs.game.activites.soulwars.AreaController;
import com.rs.game.activites.soulwars.GameController;
import com.rs.game.activites.soulwars.LobbyController;
import com.rs.game.activities.aod.npc.PraesulMinion;
import com.rs.game.activities.dfm.DemonFlashMobs;
import com.rs.game.activities.gambling.Gambling;
import com.rs.game.activities.instances.Instance;
import com.rs.game.activities.rots.RiseOfTheSixController;
import com.rs.game.activities.snowball.game.SnowballFightGameController;
import com.rs.game.activities.snowball.lobby.SnowballLobbyController;
import com.rs.game.item.Item;
import com.rs.game.item.floor.FloorItem;
import com.rs.game.item.floor.LootBeamManager;
import com.rs.game.map.bossInstance.InstanceSettings;
import com.rs.game.npc.NPC;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.npc.godwars.zaros.Nex;
import com.rs.game.npc.others.MirrorbackSpider;
import com.rs.game.npc.pest.PestPortal;
import com.rs.game.npc.pet.Pet;
import com.rs.game.npc.telos.Telos;
import com.rs.game.player.BuffDebuffTimersManager.Timer;
import com.rs.game.player.ChargesManagerNew.ChargesData;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.TemporaryAttributes.Key;
import com.rs.game.player.actions.ActionManager;
import com.rs.game.player.actions.divination.DivinationConvert.ConvertMode;
import com.rs.game.player.actions.divination.DivineObject;
import com.rs.game.player.actions.hunter.JadinkoCatch;
import com.rs.game.player.actions.invention.InventionConstants.Perks;
import com.rs.game.player.actions.invention.InventionData.Perk;
import com.rs.game.player.actions.slayer.Slayer;
import com.rs.game.player.actions.slayer.sophanemdungeon.SophanemSlayerDungeon;
import com.rs.game.player.combat.CombatUtils;
import com.rs.game.player.combat.PlayerCombat;
import com.rs.game.player.content.*;
import com.rs.game.player.content.achievements.Achievement;
import com.rs.game.player.content.achievementsystem.AchievementDifficulty;
import com.rs.game.player.content.achievementsystem.AchievementList;
import com.rs.game.player.content.achievementsystem.Achievements;
import com.rs.game.player.content.agility.courses.CourseManager;
import com.rs.game.player.content.araxxor.AraxxorController;
import com.rs.game.player.content.artisansworkshop.ArtisansWorkShop;
import com.rs.game.player.content.bank_highscores.SaveBankSql;
import com.rs.game.player.content.barrows.Barrows;
import com.rs.game.player.content.clans.ClansManager;
import com.rs.game.player.content.construction.House;
import com.rs.game.player.content.contracts.Contract;
import com.rs.game.player.content.contracts.ContractHandler;
import com.rs.game.player.content.contracts.ReaperPerks;
import com.rs.game.player.content.death.DeathController;
import com.rs.game.player.content.death.DeathItemsManager;
import com.rs.game.player.content.death.DeathManager;
import com.rs.game.player.content.distinctioncape.DistinctionCape;
import com.rs.game.player.content.distinctioncape.DistinctionCapeInterface.Cape;
import com.rs.game.player.content.dropcollection.DropCollectionHandler;
import com.rs.game.player.content.dungeoneering.*;
import com.rs.game.player.content.dungeoneering.journals.DungeoneeringJournals;
import com.rs.game.player.content.dungeoneering.skills.divination.DungeoneeringDivinationData;
import com.rs.game.player.content.ectofuntus.Ectofuntus;
import com.rs.game.player.content.ectofuntus.Ectophial;
import com.rs.game.player.content.eds.EliteDungeonsManager;
import com.rs.game.player.content.gamemode.KingOfTheSkillGameModeHandler;
import com.rs.game.player.content.grandExchange.GrandExchange;
import com.rs.game.player.content.grandExchange.GrandExchangeManager;
import com.rs.game.player.content.hcim_news.HcimNewsManager;
import com.rs.game.player.content.homearea.HomeAreaHandler;
import com.rs.game.player.content.interfaces.clientsettings.PlayerGameSettings;
import com.rs.game.player.content.interfaces.combinations.CombinationData;
import com.rs.game.player.content.interfaces.keybinds.KeyBindActions;
import com.rs.game.player.content.interfaces.keybinds.KeyCombination;
import com.rs.game.player.content.interfaces.potiontimers.PotionTimerInterface;
import com.rs.game.player.content.interfaces.teleport.TeleportInterface;
import com.rs.game.player.content.interfaces.teleport.TeleportLocation;
import com.rs.game.player.content.items.*;
import com.rs.game.player.content.jujupotions.JujuPotionManager;
import com.rs.game.player.content.jujupotions.harmonypillar.HarmonyPillarManager;
import com.rs.game.player.content.jujupotions.vineherbpatch.VineHerbPatchManager;
import com.rs.game.player.content.lottery.Lottery;
import com.rs.game.player.content.maxguild.combatportal.BossPortalType;
import com.rs.game.player.content.newlottery.LotteryBetType;
import com.rs.game.player.content.pet.PetManager;
import com.rs.game.player.content.pet.Pets;
import com.rs.game.player.content.petperks.ObtainedPet;
import com.rs.game.player.content.petperks.PetPerk;
import com.rs.game.player.content.petperks.PetPerkUtils;
import com.rs.game.player.content.polls.PollVotedDatabase;
import com.rs.game.player.content.presets.PresetHandler;
import com.rs.game.player.content.skillingcontracts.CoOpRequest;
import com.rs.game.player.content.skillingcontracts.LastSkillingContract;
import com.rs.game.player.content.skillingcontracts.SkillingContract;
import com.rs.game.player.content.skillingcontracts.SkillingContractManager;
import com.rs.game.player.content.slayer.CooperativeSlayer;
import com.rs.game.player.content.trade.ItemTransaction;
import com.rs.game.player.content.trade.ItemTransaction.CloseTransactionStage;
import com.rs.game.player.content.xmas.XmasEvent;
import com.rs.game.player.controllers.*;
import com.rs.game.player.controllers.Dungeoneering;
import com.rs.game.player.controllers.bossInstance.VoragoInstanceController;
import com.rs.game.player.controllers.castlewars.CastleWarsPlaying;
import com.rs.game.player.controllers.castlewars.CastleWarsWaiting;
import com.rs.game.player.controllers.fightpits.FightPitsArena;
import com.rs.game.player.controllers.pestcontrol.PestControlGame;
import com.rs.game.player.controllers.pestcontrol.PestControlLobby;
import com.rs.game.player.cutscenes.Cutscene;
import com.rs.game.player.cutscenes.CutscenesManager;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.player.dialogue.DialogueManager;
import com.rs.game.player.dialogue.impl.AfkD;
import com.rs.game.player.dialogue.impl.AllTeleportsD;
import com.rs.game.player.security.pin.AccountPin;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.network.packet.PacketDispatcher;
import com.rs.network.packet.PacketRepository;
import com.rs.network.packet.impl.ButtonHandler;
import com.rs.utils.*;
import com.rs.utils.mysql.QueryExecutor;
import com.rs.utils.mysql.impl.Hiscores;
import com.rs.utils.mysql.impl.News;
import com.rs.utils.mysql.impl.ResetUserHiscores;
import com.rs.utils.mysql.struct.GenericChatLog;
import com.rs.utils.web.StoreManager;
import io.netty.channel.Channel;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import lombok.Getter;
import lombok.Setter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Player extends Entity {

    public LocalDateTime lastDeathSoul;
    public AccountPin accountPin;
    public LocalDateTime lastBankSave;
    public transient Stopwatch onlineTracker;
    public transient int jujuGumbo;
    public transient int baronShark;
    public transient boolean woodSpirit;
    public int lastViewedScoreboard;
    public boolean setCorruptionCooldown;
    private transient Player player;
    public QuestHandler quests;
    public Set<Integer> questionedNpcs;
    public transient WorldTile presentHintArrow;
    public boolean heardDeathsWarning;
    public boolean stopEvilDustConfirmation;
    public static final int TELE_MOVE_TYPE = 127, WALK_MOVE_TYPE = 1, RUN_MOVE_TYPE = 2;
    private static final long serialVersionUID = 2011932556974180375L;
    public boolean barrowsAmuletActivated;
    public boolean evilTreeContracts;
    public transient boolean killNext;
    public transient Stopwatch pickTaskThrottle;
    public double awardBcPoint;
    public double awardDkPoint;
    public GIMPrestigeManager prestigeManager;
    public transient Stopwatch groupSearchThrottle;
    public transient boolean rodSaved;
    public transient BookInterface lastBookInterface;
    public boolean rejectSave;
    public LocalDateTime lastResetWisdomAura;
    public String lastSearchedOffer;
    public transient WorldTask stringJewelleryAction;
    public int lastTaskId = -1;
    @Getter
    @Setter
    private transient List<TeleportLocation> favoriteSkillingTeleports;
    @Getter
    @Setter
    private transient KeyActionMapper keyAction;
    public transient String changeIP;
    public String gimName;
    public GIMGroupKey gimKey;
    public transient GIMScoreTracker gimTracker;
    public transient String pendingGimKey;
    public transient ActionManager actionManager;
    public transient GIMBankManager gimBank;
    public transient LoyaltyManager loyaltyManager;
    public transient int xpBookOption = -1;
    public transient CoOpRequest coOpRequest;
    public transient boolean evilTreeInter;
    public transient boolean treeAttacking;
    public transient EvilTreeInstanceLevel etInstanceLvl;
    public transient DungeonArchitectMonster monsterType;
    public transient boolean instantKillEvilTree;
    public transient boolean unlockedSettings;
    public transient boolean instanceCreated;
    public int evilTreeKc;
    @Getter
    @Setter
    private transient int alcoholIntake = 0;
    protected transient Ectophial ectophial;
    public int entlingTransformId;
    public AuraManager auraManager;
    public int totalBoothVotes;
    public boolean clickToTeleport;
    private ActivityTimersManager activityTimersManager;
    private PetPerkManager petPerkManager;
    public ListMultimap<String, PollVotedDatabase.PlayerVote> recordedVotes;
    public Set<String> dropCatcherFilter;
    public LocalDateTime petNurture;
    @Getter
    @Setter
    public PresetHandler presetHandler;
    public int ectoCharges;
    public int sophanemKc;
    public transient boolean inPzInstance;
    @Getter
    @Setter
    public BossPortalType bossPortal;
    @Getter
    @Setter
    public TeleportInterface teleportInterface;
    @Getter
    @Setter
    public long combatPortalRetuneDelay;
    // Used for xenthium's easter eggs.
    @Getter
    @Setter
    public boolean discoveredEasterEgg, completedEasterEgg;
    @Getter
    @Setter
    public DropCollectionHandler dropCollectionHandler;
    @Getter
    @Setter
    private boolean givenBoxAutomatically2;
    public transient List<String> pollAnswers;
    // newpkstuff
    public int highestKillStreak, killStreak, killStreakPoints, totalkillStreakPoints;
    public int highestKill;
    // Ip Lock
    public transient WorldTile knightTile;
    public boolean iplocked; // Is player using iplock
    public String lockedwith; // which ip is account locked to
    public ChargesManager charges;
    public LocalDateTime lastVote;

    @Deprecated // Needs to be LocalDateTime, but not sure if its safe to change or delete.
    public LocalTime lastBug;
    @Deprecated // Needs to be LocalDateTime, but not sure if its safe to change or delete.
    public LocalTime lastSuggest;
    @Getter
    @Setter
    public boolean hidePets;
    public LocalDateTime lastBug2;
    public LocalDateTime lastSuggest2;
    public boolean unlockedSkillingShop;
    public boolean unlockedAdvancedSkillingShop;
    public transient boolean clientLoadedMapRegion;
    public transient FriendChatsManager currentFriendChat;
    public transient CutscenesManager cutscenesManager;
    public transient DialogueManager dialogueManager;
    public transient int displayMode;
    @Getter
    @Setter
    public List<Integer> activePotions;

    public int customBankAmount = 1;

    public AccountPin getAccountPin() {
        if (accountPin == null) accountPin = new AccountPin(this);
        return accountPin;
    }

    // add a setter if you don't have one
    public void setAccountPin(AccountPin pin) { this.accountPin = pin; }

    // 2) Helper: make sure the pin object exists and its transient player ref is set
    private void ensureAccountPin() {
        if (accountPin == null) accountPin = new AccountPin(this);
        else accountPin.setPlayer(this); // important after deserialization
    }

    // 3) Called automatically after Java deserialization
    private Object readResolve() {
        ensureAccountPin();
        return this;
    }

    @Getter
    @Setter
    private transient boolean grabbing;
    private transient boolean noclip;
    private transient Instance currentInstance;
    public DominionTower dominionTower;
    public TreasureTrails treasureTrails;
    public transient int fishingDelay;
    public BountyHunter bountHunter;
    public transient LotteryBetType confirmBet;
    public Equipment equipment;
    public int[] fairyRingCombination = new int[3];
    public Familiar familiar;
    public transient HintIconsManager hintIconsManager;
    public transient InterfaceManager interfaceManager;
    public transient IsaacKeyPair isaacKeyPair;
    public transient LocalNPCUpdate localNPCUpdate;
    public transient MirrorbackSpider mirrorback;
    public transient ZarosGodswordSpecialAttack zarosGodswordSpecialAttack;
    public transient DistinctionCape distinctionCape;
    // used for update
    public transient LocalPlayerUpdate localPlayerUpdate;
    // used for packets logic
    public transient ConcurrentLinkedQueue<LogicPacket> logicPackets;
    private transient long bloodNecklaceSpecial;
    public int money;
    public Notes notesL;
    public transient Pet pet;
    public SkillingContractManager contracts;
    public PetManager petManager = new PetManager();
    public transient long polDelay;
    public transient long bloodDelay;
    public MoneyPouch pouch;

    /**
     * Virtual shards are no longer used for Lootshare.
     */
    @Deprecated
    public Multiset<Integer> lootShareSet = HashMultiset.create();
    public Prayer prayer;
    public DicingManager dicingManager;
    public PriceCheckManager priceCheckManager;
    public QuestManager questManager;
    private VisWaxManager visWaxManager;
    @Setter
    @Getter
    private Map<PlayerGameSettings, Boolean> gameSettings;
    private Map<Integer, Integer> metalBank;
    private Map<Integer, Integer> oreBoxContents;
    /**
     * usage of getSkills() is encouraged
     */
    public Skills skills;
    @Setter
    private transient Skills fakeSkills;
    @Setter
    private transient boolean usingFakeSkills;
    public StoreManager store;
    public int infusedPouches;
    public Contract Rtask;
    public int tasksCompleted;
    @Getter
    @Setter
    private transient ItemTransaction itemTransaction;
    public int usedMacs;
    private int gravestone;
    private boolean hasEnhancedFireCape;
    // transient stuff
    public transient String username;
    public boolean xmasTitle1;
    public boolean xmasTitle2;
    public boolean xmasTitle3;
    public boolean xmasTitle4;
    public int feedPetCounter;
    public boolean isBleeding;
    public boolean isFixing;
    public boolean lockedShiftDrop;
    public transient boolean switchDropMode;
    private transient long lunarDelay;
    public Player target;
    public int lastTabFixed;
    public int lastTabResizable;
    public boolean killme;
    public boolean receivedSeed;
    public boolean wonVoteDonator;
    private Slayer slayer;
    private boolean lootedBarrowsChest;
    public boolean addedEquipmentSlot;
    private transient List<WorldObject> playerObjects;
    private HeartOfGielinor heart;
    private transient NPC currentRandomEventNPC;
    private transient AchievementDifficulty selectedAchivementsTab;
    private transient AchievementList selectedAchivement;
    private transient Cape selectedDistinctionCape;
    @Getter
    private IntOpenHashSet interactedNoncombatPets = new IntOpenHashSet(18);

    @Getter
    @Setter
    private boolean notingDrops;

    @Getter
    @Setter
    public boolean showPotionTimers = true;

    /**
     * Silverhawk boots.
     */
    public int ticksSpentRunning;
    public long coolDownForSilverhawks;
    public transient Stopwatch shSkillingCooldown;
    public transient Stopwatch shCombatCooldown;
    public transient Stopwatch offerSearchCooldown;
    public transient long shCombatDelay;
    public transient WorldTile ticketTile;
    public int alwaysUseSpecialSpeed;

    public transient boolean selectedMeleeDreadnip;
    public transient boolean selectedMagicDreadnip;
    public transient boolean selectedRangedDreadnip;

    /**
     * Custom title settings
     */
    public boolean beforeName;
    public String title;
    public String colour;

    // Used for light creature
    public long enlightenment;

    /**
     * PvP
     */
    public int totalpkPoints;
    public BountyHunter bountyHunter;
    public int lendMessage;

    public int demonFlashMobsKills;
    /**
     * Custom Game Mode ranks.
     */
    public boolean expert, novice, ironman, noviceironman, intermediateironman, intermediate, expertironman, hcironman, legendary;
    /**
     * New player starter stuff.
     */
    public boolean hasCompleted, hasLogedIn, hasClaimedStarterEquipment;
    /**
     * Varbit manager.
     */
    public transient VarBitManager VBM;
    /**
     * Farming.
     */
    public FarmingManager farmingManager;
    /**
     * Death manager
     */
    private DeathManager deathManager;
    /**
     * LodeStones.
     */
    public boolean hasLodestones;
    public boolean[] lodestone;
    /**
     * Grand Exchange.
     */
    public GrandExchangeManager geManager;
    public int produceGathered;
    public int pouchesMade;
    /**
     * Daily Tasks.
     */
    public transient PestPortal pcFirewall;
    public DailyManager.DailyTasks dailyTask;
    @Getter
    public DailyManager dailyManager;
    @Getter
    @Setter
    private int taskId;
    @Getter
    @Setter
    private int skillingTask;
    @Getter
    @Setter
    private int taskItemId;

    @Getter
    @Setter
    private int dailyAmount;
    public LastSkillingContract lastContract;
    public boolean skippedLastContract;
    /**
     * Toolbelt.
     */
    public Toolbelt toolBelt;
    public ToolbeltNew toolBeltNew;
    /**
     * Player-owned titles.
     */
    public Titles titles;
    public int dungKills;
    public boolean inDungeoneering;
    /**
     * Loot beam.
     */
    private LootBeamManager lootBeamManager;
    /**
     * Construction.
     */
    public boolean hasHouse, inRing;
    public House house;

    /**
     * Demon flash mobs
     */
    public boolean[] DFMScroll;
    /**
     * Co-Op Slayer.
     */
    public CooperativeSlayer coOpSlayer;
    public boolean hasInvited, hasHost, hasGroup, hasOngoingInvite;
    /**
     * Perk Management.
     */
    public PerkManager perkManager;
    public int ironOres;
    public boolean hasAnswered;
    public DeathItemsManager deathItemsManager;
    public boolean xpert_bonus;
    /**
     * Squeal of Fortune
     */
    public SquealOfFortune squealOfFortune;
    @Getter
    @Deprecated
    public Achievement achievementManager;
    /**
     * Prayer Books.
     */
    public boolean[] prayerBook;
    public long displayNameChange;
    /**
     * Well of Good Will.
     */
    public long donatedToWell;
    public long lastWellDonation;
    public int recentWellDonated;
    /**
     * Divination things.
     */
    public boolean[] boons;
    public int divine, gathered;
    public transient Player divines;
    public long lastGatherLimit, lastGatherLimit1, lastCreationTime;
    private long timeToNextMysterybox;
    public int gatherLimitMessage;
    public int createdToday;
    public boolean created;
    public boolean hefinLapReward;
    public long motherlodeMaw;

    /**
     * Easter stuff
     */

    public int easterPoints;
    public boolean startedEasterEvent;
    public boolean easterToggle;

    public boolean easterTitle1;
    public boolean easterTitle2;
    public boolean easterTitle3;
    public boolean easterTitle4;
    public boolean easterTitle5;
    public boolean easterTitle6;

    /**
     * Prifddinas thieving.
     */
    public int thievIorwerth, thievIthell, thievCadarn, thievAmlodd, thievTrahaearn, thievHefin, thievCrwys, thievMeilyr;
    public byte caughtIorwerth, caughtIthell, caughtCadarn, caughtAmlodd, caughtTrahaearn, caughtHefin, caughtCrwys, caughtMeilyr;
    /**
     * Cosmetic Overrides (Outfits)
     */
    public CosmeticOverrides overrides;
    private boolean usingTeleportInterface = true;
    /**
     * Shark outfit.
     */
    public boolean consumeFish;
    public int barrowsAmuletUses;
    /**
     * AFK auto-Kick.
     */
    public transient long afkTimer = Utils.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(25, TimeUnit.MINUTES);
    /**
     * Vorago
     */
    public boolean defeatedVorago, isSiphoning, firstTime;
    /**
     * Animation Overrides
     */
    public AnimationOverrides animations;
    /**
     * Player Owned Port.
     */
    public com.rs.game.player.content.group.Group group;
    public JujuPotionManager jujuPotions;
    public HarmonyPillarManager harmonyPillars;
    public VineHerbPatchManager vineHerbPatches;
    public PlayerOwnedPort ports;
    public XmasEvent xmas;
    public Multiboss multiboss;
    // Temporary (for portables)
    public transient WorldObject clickedObject;
    @Getter
    private boolean kingOfTheSkillGameMode;

    /**
     * Ad referral boolean
     */
    @Getter
    @Setter
    private boolean referralChecked;
    private transient boolean wasXpLocked;
    @Getter
    @Setter
    private int lmsVictories;
    @Getter
    @Setter
    private int lmsKills;

    /**
     * Kill-statistics.
     */
    public int[] killStats = new int[512];
    private transient Channel channel;
    private transient Channel realChannel;
    private boolean agrithNaNa;
    private boolean allowChatEffects;
    private GlobalPlayerUpdater globalPlayerUpdater;
    private Bank bank;
    private Bank metalBankStorage;
    private transient Bank previousBankBeforeMetalBank;
    private List<Bank> banks;
    private int barrowsKillCount;
    private int barrowsRunsDone;
    private int riseOfTheSixRunsCompleted;
    private transient long boneDelay;
    private transient boolean canPvp;
    private transient boolean safePvp;
    private transient boolean cantTrade;
    private transient boolean castedVeng;
    private int clanStatus;
    public boolean oldTomeActivated;
    private transient Runnable closeInterfacesEvent;
    private final CombatDefinitions combatDefinitions;
    // completionistcape reqs
    private boolean completedFightCaves;
    private boolean completedFightKiln;
    private boolean completedRfd;
    private int[] completionistCapeCustomized;
    private final ControlerManager controlerManager;
    private long creationDate;
    private int crucibleHighScore;
    private boolean culinaromancer;
    private transient ClansManager clanManager, guestClanManager;
    private String currentFriendChatOwner;
    private String clanName;
    private boolean connectedClanChannel;
    private boolean dessourt;
    private transient boolean disableEquip;
    private String displayName;
    private boolean donator;
    private long donatorTill;
    private DuelArena duelarena;
    private final EmotesManager emotesManager;
    private boolean extremeDonator;
    private long extremeDonatorTill;
    @SuppressWarnings("unused")
    private boolean filterGame;
    private transient boolean finishing;
    private long fireImmune;
    private long superAntiFire;
    private boolean flamBeed;
    private transient long foodDelay;
    private boolean forceNextMapLoadRefresh;
    private final FriendsIgnores friendsIgnores;
    private int hiddenBrother;
    private boolean hideWorldAnnouncements;
    private transient double hpBoostMultiplier;
    private boolean inAnimationRoom;
    private final Inventory inventory;
    private transient boolean invulnerable;
    private transient boolean developmentGodMode;
    private transient boolean infiniteRunEnergy, infiniteCombatRunes, infiniteAmmunition;
    public boolean isInfiniteRunEnergy() { return infiniteRunEnergy; }
    public void setInfiniteRunEnergy(boolean enabled) { infiniteRunEnergy = enabled; }
    public boolean isInfiniteCombatRunes() { return infiniteCombatRunes; }
    public void setInfiniteCombatRunes(boolean enabled) { infiniteCombatRunes = enabled; }
    public boolean isInfiniteAmmunition() { return infiniteAmmunition; }
    public void setInfiniteAmmunition(boolean enabled) { infiniteAmmunition = enabled; }

    public boolean isInvulnerable() { return invulnerable || developmentGodMode; }
    public boolean isDevelopmentGodMode() { return developmentGodMode; }
    public void setDevelopmentGodMode(boolean enabled) { developmentGodMode = enabled; }

    @Override
    public void setHitpoints(final int hitpoints) {
        // Native environmental damage may write HP without going through a Hit.
        if (isNative950() && developmentGodMode && hitpoints < getHitpoints()) return;
        super.setHitpoints(hitpoints);
    }
    private boolean isInDefenderRoom;
    private long jailed;
    private boolean karamel;
    // objects
    private boolean khalphiteLairEntranceSetted;
    private boolean khalphiteLairSetted;
    // honor
    private int killCount, deathCount;
    // barrows
    private boolean[] killedBarrowBrothers;
    private boolean killedBork;
    private boolean killedQueenBlackDragon;
    private transient boolean largeSceneView;
    private transient int shopLastViewX;
    private int lastBonfire;
    private transient DuelRules duelRules;
    public String lastPinIP;
    private String lastIP;
    @SuppressWarnings("unused")
    private String lastKillIP;
    private long lastLoggedIn;
    private transient long lastPublicMessage;
    private transient long lockDelay; // used for doors and stuff like that
    @Getter
    @Setter
    public long ticketDelay; // used for spam ticket delays
    public transient boolean toogleLootShare;
    // skill capes customizing
    private int[] maxedCapeCustomized;
    private boolean mouseButtons;
    private final MusicsManager musicsManager;
    private int overloadDelay;
    private int supremeOverloadDelay;
    public int naturesEssenceDelay;
    public int bloodSerumDelay;
    private int aggressiveDelay;
    private List<String> ownedObjectsManagerKeys;
    private transient long packetsDecoderPing;
    // saving stuff
    @Getter
    @Setter
    private String discordId;
    private String password;
    private boolean permBanned;
    private boolean permMuted;
    @Getter
    @Setter
    private boolean lastManStandingBanned;
    @Setter
    @Getter
    private transient Gambling gamblingSession;
    public transient int oresLeft;
    private int pestControlGames;
    private int pestPoints;
    private int pkPoints;
    private int pkPointReward;
    private int pointsHad;
    private long poisonImmune;
    private transient long potDelay;
    private final int[] pouches;
    private int prayerRenewalDelay;
    // game bar status
    private int publicStatus;
    // Extra bank space
    private int bankspace;
    @Getter
    private boolean donatorIcon;
    private String registeredMac, currentMac;
    private boolean reportOption;
    private transient boolean resting;
    private int rights;
    @Getter
    @Setter
    private byte messageIconOverride;
    private byte runEnergy;
    private int runeSpanPoints;
    private transient boolean running;
    private int skullDelay;
    private int skullId;
    private int slayerPoints;
    private transient boolean spawnsMode;
    private int specRestoreTimer;
    private int spins;
    // player stages
    private transient boolean active;
    private transient List<Integer> switchItemCache;
    private boolean talkedtoCook;
    // Slayer
    private SlayerTask task;
    private boolean talkedWithVannaka, talkedWithMarv;
    private int taskStreak;

    @Setter
    @Getter
    private boolean hasDropTableEnabled;
    @Getter
    private int dailyTasksCompleted;
    private boolean[] claimedDonationAwardBoxes;

    @Getter
    private transient KingOfTheSkillGameModeHandler kingOfTheSkillGameModeHandler;
    @Getter
    @Setter
    private transient FlowerPokerSession flowerPokerSession;
    private Achievements achievements;

    @Setter
    @Getter
    private boolean displayPlayerOfTheMonthIcon;

    public void togglePlayerOfTheMonthIcon() {
        displayPlayerOfTheMonthIcon = !displayPlayerOfTheMonthIcon;
    }

    @Getter
    @Setter
    private boolean playerOfTheMonth;

    @Getter
    @Setter
    private int mysteryBoxesOpened;

    public void incrementMysteryBoxesOpened() {
        if (mysteryBoxesOpened < 0) {
            mysteryBoxesOpened = 0;
        }
        mysteryBoxesOpened++;
    }

    public void togglePlayerOfTheMonth() {
        playerOfTheMonth = !playerOfTheMonth;
    }

    private transient long disruptionDelay, vengeanceDelay, spiritualiseDelay;
    private transient long chargeDelay;

    public long getChargeDelay() {
        return chargeDelay;
    }

    public void setChargeDelay(final int delay) {
        chargeDelay = Utils.currentTimeMillis() + delay;
    }

    private transient boolean disruption;

    public boolean hasDisruption() {
        return disruption;
    }

    public void setDisruption(final boolean value) {
        disruption = value;
    }

    public long getDisruptionDelay() {
        return disruptionDelay;
    }

    public void setDisruptionDelay(final int delay) {
        disruptionDelay = Utils.currentTimeMillis() + delay;
    }

    public long getSpiritualiseDelay() {
        return spiritualiseDelay;
    }

    public void setSpiritualiseDelay(final int delay) {
        spiritualiseDelay = Utils.currentTimeMillis() + delay;
    }

    private transient long healDelay;

    public long getHealDelay() {
        return healDelay;
    }

    public void setHealDelay(final int delay) {
        healDelay = Utils.currentTimeMillis() + delay;
    }

    private transient boolean magicImbue;

    public boolean hasMagicImbue() {
        return magicImbue;
    }

    public void setMagicImbue(final boolean value) {
        magicImbue = value;
    }

    public long getVengeanceDelay() {
        return vengeanceDelay;
    }

    public void setVengeanceDelay(final int delay) {
        vengeanceDelay = Utils.currentTimeMillis() + delay;
    }

    /**
     * New networking temp stuff.
     */
    transient PacketDispatcher dispatcher;
    transient PacketRepository repository;
    private transient ClientSession clientSession;

    void bindClientSession(ClientSession clientSession) {
        this.clientSession = java.util.Objects.requireNonNull(clientSession, "clientSession");
    }

    public ClientSession getClientSession() {
        return clientSession;
    }

    /** Bots and deserialized players retain the existing 910 behavior until login. */
    public ClientProfile getClientProfile() {
        return clientSession == null ? ClientProfile.LEGACY_910 : clientSession.getProfile();
    }

    private transient boolean native947MapRefresh;
    private transient com.rs.game.player.client.Native950Dialogues native947Dialogues;

    public com.rs.game.player.client.Native950Dialogues getNative950Dialogues() {
        return native947Dialogues;
    }

    public void setNative950Dialogues(com.rs.game.player.client.Native950Dialogues dialogues) {
        if (!isNative950()) throw new IllegalStateException("Native dialogues require a native client");
        native947Dialogues = dialogues;
    }

    public boolean isNative950() {
        // Every call site means "native client, do not take the 910 path", so this is deliberately
        // profile-agnostic rather than pinned to one revision.
        return getClientProfile().isNativeModern();
    }

    /** Consumed by the native world after the authoritative movement step. */
    public boolean consumeNative950MapRefresh() {
        boolean refresh = native947MapRefresh;
        native947MapRefresh = false;
        return refresh;
    }

    public void setPoisonImmune(final long poisonImmune) {
        this.poisonImmune = poisonImmune;
    }

    private Lottery lottery;

    public Lottery getLottery() {
        return lottery;
    }

    public void setLottery() {
        lottery = new Lottery();
    }

    // reaper
    private final ContractHandler cHandler;
    private Contract cContracts;
    @SuppressWarnings("unused")
    private int temporaryMovementType;
    private int tradeStatus;
    @SuppressWarnings("unused")
    private boolean updateMovementType;
    private transient long yellDelay;
    private int vecnaTimer;
    private int votePoints;
    private int weekendBooks;
    private boolean wonFightPits;
    private boolean xpLocked;
    private String yellColor = "ff0000";
    private boolean yellDisabled;
    private boolean yellOff;
    private long muted;
    private long banned;
    private DungeoneeringJournals dungJournals;
    private GemBag gemBag;
    private int coal;
    private int natureRunes;
    private int lawRunes;
    public boolean resetheart;

    public boolean infusedSpiritCapeEffect;

    private transient boolean isShadow;

    private boolean agilityPyramidRope, burthorpePegs;

    public boolean hasAttachedPyramidRope() {
        return agilityPyramidRope;
    }

    public void setAttachedAgilityPyramidRope() {
        agilityPyramidRope = true;
    }

    public boolean hasRepairedBurthorpePegs() {
        return burthorpePegs;
    }

    public void setHasRepairedBurthorpePegs() {
        burthorpePegs = true;
    }

    private GemstoneArmour gemstone;

    public GemstoneArmour getGemstoneArmour() {
        return gemstone;
    }

    public void setGemstoneArmour() {
        gemstone = new GemstoneArmour();
    }

    private int gemstoneKC;

    public int getGemstoneKC() {
        return gemstoneKC;
    }

    public void addGemstoneKC(final int amount) {
        gemstoneKC += amount;
    }

    public boolean isShadow() {
        return isShadow;
    }

    public void setShadow(final boolean val) {
        isShadow = val;
    }

    public boolean designer;

    public void setDesigner(final boolean input) {
        designer = input;
    }

    public boolean isDesigner() {
        return designer;
    }

    public HeartOfGielinor getHeart() {
        return heart;
    }

    public Instance getCurrentInstance() {
        return currentInstance;
    }

    public void setCurrentInstance(final Instance instance) {
        currentInstance = instance;
    }

    public void setHeartofGielinor() {
        heart = new HeartOfGielinor();
    }

    public void setInteractedNoncombatPets() {
        interactedNoncombatPets = new IntOpenHashSet(18);
    }

    public boolean hasLootedBarrowsChest() {
        return lootedBarrowsChest;
    }

    public void setLootedBarrowsChest(final boolean val) {
        lootedBarrowsChest = val;
    }

    public boolean hasUnlockedEFC() {
        return hasEnhancedFireCape;
    }

    public void setUnlockedEFC(final boolean state) {
        hasEnhancedFireCape = state;
    }

    public long getBloodNecklaceSpecial() {
        return bloodNecklaceSpecial;
    }

    public void setBloodNecklaceSpecial() {
        bloodNecklaceSpecial = Utils.currentTimeMillis() + 15000;
    }

    public int getNatureRunes() {
        return natureRunes;
    }

    public List<WorldObject> getPlayerObjects() {
        return playerObjects;
    }

    public void addPlayerObject(final WorldObject object) {
        playerObjects.add(object);
    }

    public boolean removePlayerObject(final WorldObject object) {
        return playerObjects.remove(object);
    }

    public void resetPlayerObjects() {
        playerObjects = new ArrayList<WorldObject>();
    }

    public boolean isMasterMember() {
        return masterDonator || moneySpent >= 1000;
    }
    public boolean isDiamondMember() {
        return ultimateDonator || moneySpent >= 500;
    }
    public boolean isPlatinumMember() {
        return supremeDonator || moneySpent >= 250;
    }
    public boolean isGoldMember() {
        return legendaryDonator || moneySpent >= 100;
    }
    public boolean isSilverMember() {
        return extremeDonator || moneySpent >= 50;
    }
    public boolean isBronzeMember() {
        return donator || moneySpent >= 20;
    }

    public final WorldObject getPlayerObjectWithId(final int id, final int x, final int y, final int z) {
        for (final WorldObject objects : playerObjects) {
            if (objects.getId() == id && objects.getX() == x && objects.getY() == y && objects.getPlane() == z) {
                return objects;
            }
        }
        return null;
    }

    public final WorldObject getPlayerObjectWithType(final int type, final int x, final int y, final int z) {
        for (final WorldObject objects : playerObjects) {
            if (objects.getType() == type && objects.getX() == x && objects.getY() == y && objects.getPlane() == z) {
                return objects;
            }
        }
        return null;
    }

    public SkillingContract skillingContract;

    public SkillingContract getSkillingContract() {
        return skillingContract;
    }

    public SkillingContractManager getContracts() {
        return contracts;
    }

    public int getLawRunes() {
        return lawRunes;
    }

    public void setNatureRunes(final int amount) {
        natureRunes = amount;
    }

    public void setLawRunes(final int amount) {
        lawRunes = amount;
    }

    public static final int MAX_COAL = 100;

    public int getCoal() {
        return coal;
    }

    public void addCoal(final int amount) {
        int newAmount = coal + amount;
        if (newAmount > MAX_COAL) {
            throw new IllegalStateException("Too much coal (" + newAmount + "). Please check before adding.");
        }
        coal = newAmount;
    }

    public void removeCoal(final int amount) {
        int newAmount = coal - amount;
        if (newAmount < 0) {
            throw new IllegalStateException("Too little coal (" + newAmount + "). Please check before removing.");
        }
        coal = newAmount;
    }

    public void setCoal(int newCoal) {
        if (newCoal > MAX_COAL) {
            throw new IllegalStateException("Too much coal (" + newCoal + "). Please check before adding.");
        } else if (newCoal < 0) {
            throw new IllegalStateException("Too little coal (" + newCoal + "). Please check before removing.");
        }
        coal = newCoal;
    }

    public Slayer getSlayer() {
        return slayer;
    }

    public DungeoneeringJournals getDungeoneeringJournals() {
        return dungJournals;
    }

    public GemBag getGemBag() {
        return gemBag;
    }

    public void setDungeoneeringJournals(final DungeoneeringJournals journal) {
        dungJournals = journal;
    }

    public void setGemBag() {
        gemBag = new GemBag(this);
    }

    /*
     * Custom titles
     */

    public String getCustomTitle() {
        if (colour != null) {
            return (beforeName ? "" : " ") + "<col=" + colour + ">" + title + "</col>" + (beforeName ? " " : "");
        } else {
            return (beforeName ? "" : " ") + title + (beforeName ? " " : "");
        }
    }

    /**
     * Home locations
     */
    private WorldTile homeLocation;
    private String homeName;

    public WorldTile getHome() {
        return homeLocation;
    }

    public void setHome(final WorldTile home, final String name) {
        homeLocation = home;
        homeName = name;
    }

    public String getHomeName() {
        return homeName;
    }

    /**
     * Banned slayer task information bannedtask preferred tasks
     */

    private List<Integer> bannedTasks;
    private Map<Integer, Boolean> expertShards;

    /*
     * Data for rune pouch
     */

    @Getter
    @Setter
    private Map<Integer, Item> smallRunePouch;

    @Getter
    @Setter
    private Map<Integer, Item> bigRunePouch;

    public void addRuneToPouch(final Item rune, final boolean small) {
        (small ? smallRunePouch : bigRunePouch).put(rune.getId(), rune);
        this.getCombatDefinitions().refreshRunes();
    }
    
    public boolean containsRuneInPouch(int runeId, final boolean small) {
        return (small ? smallRunePouch : bigRunePouch).containsKey(runeId);
    }
    
    public void removeRune(final Item rune, final boolean small) {
        if (!(small ? smallRunePouch : bigRunePouch).containsKey(rune.getId())) {
            return;
        }

        final int amount = (small ? smallRunePouch : bigRunePouch).get(rune.getId()).getAmount();
        if ((amount - rune.getAmount()) > 0) {
            (small ? smallRunePouch : bigRunePouch).put(rune.getId(), new Item(rune.getId(), amount - rune.getAmount()));
        } else {
            (small ? smallRunePouch : bigRunePouch).remove(rune.getId());
        }
        this.getCombatDefinitions().refreshRunes();
    }

    private String lastKilled;
    private String lastKilledIP;
    @SuppressWarnings("unused")
    private double dropRate;
    private transient RouteEvent routeEvent;
    private double[] warriorPoints;
    private long thievingDelay;
    /**
     * IRL Money spent.
     */
    private int moneySpent;
    private int loyaltyMoney;
    /**
     * Completionist Cape requirements.
     */
    public int triskKeyReedem;
    private int oresMined, smithingActions;
    private int logsChopped, logsBurned;
    private int lapsRan;
    private int bonesOffered;
    private int potionsMade;
    private int timesStolen;
    private int itemsMade;
    private int itemsFletched;
    private int furnitureCreated;
    private int creaturesCaught;
    private int fishCaught;
    private int foodCooked;
    private int memoriesCollected;
    private int runesMade;
    private boolean max, comp, compT;
    private boolean combinedCloaks;
    private boolean guthixTitle;
    private int swwins;
    private boolean reachedFloor50, hasUpgradedBarrowsDefence;
    public int lsp = 1;
    public transient int shardingDelay = -1;
    public transient int lootshareDelay = -1;
    @Getter
    @Setter
    public long flaggedWordsWarningDelay;

    @Getter
    private int necklaceOfOmnipotenceAttempts;
    @Getter
    private int ringOfOmnipotenceAttempts;
    @Getter
    private int glovesOfOmnipotenceAttempts;
    @Getter
    private int bootsOfOmnipotenceAttempts;

    public void incrementOmnipotenceRoll(CombinationData combinationData) {
        if (necklaceOfOmnipotenceAttempts < 0) {
            necklaceOfOmnipotenceAttempts = 0;
        }
        if (ringOfOmnipotenceAttempts < 0) {
            ringOfOmnipotenceAttempts = 0;
        }
        if (glovesOfOmnipotenceAttempts < 0) {
            glovesOfOmnipotenceAttempts = 0;
        }
        if (bootsOfOmnipotenceAttempts < 0) {
            bootsOfOmnipotenceAttempts = 0;
        }
        switch (combinationData) {
            case NECKLACE_OF_OMNIPOTENCE:
                necklaceOfOmnipotenceAttempts++;
                break;
            case RING_OF_OMNIPOTENCE:
                ringOfOmnipotenceAttempts++;
                break;
            case GLOVES_OF_OMNIPOTENCE:
                glovesOfOmnipotenceAttempts++;
                break;
            case BOOTS_OF_OMNIPOTENCE:
                bootsOfOmnipotenceAttempts++;
                break;
            case BOOTS_OF_OMNIPOTEN:
                bootsOfOmnipotenceAttempts++;
                break;
        }
    }





    public int getOmnipotenceAttempts(CombinationData combinationData) {
        switch (combinationData) {
            case NECKLACE_OF_OMNIPOTENCE:
                return necklaceOfOmnipotenceAttempts;
            case RING_OF_OMNIPOTENCE:
                return ringOfOmnipotenceAttempts;
            case GLOVES_OF_OMNIPOTENCE:
                return glovesOfOmnipotenceAttempts;
            case BOOTS_OF_OMNIPOTENCE:
                return bootsOfOmnipotenceAttempts;
        }
        return -1;
    }

    public boolean hasReachedFloor50() {
        return reachedFloor50;
    }

    public void setHasReachedFloor50() {
        reachedFloor50 = true;
    }

    public boolean hasUpgradedBarrowsDefender() {
        return hasUpgradedBarrowsDefence;
    }

    public void setHasUpgradedBarrowsDefender() {
        hasUpgradedBarrowsDefence = true;
    }

    public int getSoulWarsWins() {
        return swwins;
    }

    public void addSWWin() {
        swwins++;
    }

    public int getSlayerTasks() {
        return tasksCompleted;
    }

    /**
     * Donator Boxes.
     */
    private int boxesOpened;
    /**
     * Crystal chest.
     */
    private int chestsOpened;
    /**
     * Dungeoneering.
     */
    private int dungTokens;
    /**
     * Dungeoneering scrolls.
     */
    private boolean augury, renewal, rigour, efficiency, life, cleansing;
    private boolean anguish, torment;
    /**
     * Used to handle XP bonus. and tracking it on login/logout
     */
    private long doubleXpTimer;

    @Getter
    @Setter
    private long voteDoubleXpTimeRemaining;
    
    public long donorObjectBypassTime;
    
    public boolean canByPassDonorObjects() {
    	return donorObjectBypassTime > System.currentTimeMillis();
    }
    /**
     * Soul Wars.
     */
    private int zeals;
    private transient long karamDelay;
    private String slayerPartner = "";
    private String slayerHost = "";
    private String slayerInvite = "";
    private int ReaperPoints;
    private int totalkills;
    private int totalcontract;
    /**
     * Loyalty Program
     */
    private int loyaltyPoints;
    private int times;
    /**
     * Seasonal emotes.
     */
    private boolean halloweenEmotes, christmasEmotes, easterEmotes, thanksGiving;
    /**
     * Times this has voted.
     */
    private int votes;
    @Getter
    @Setter
    private transient int sitesSuccessfullyVotedOn;
    public void incrementSitesSuccessfullyVotedOn() {
        sitesSuccessfullyVotedOn++;
    }
    /**
     * Custom ranks.
     */
    private boolean legendaryDonator, supremeDonator, ultimateDonator;
    /**
     * Trivia
     */
    private int triviaPoints;
    /**
     * Play time.
     */
    private long totalPlayTime;
    private long recordedPlayTime;
    private transient boolean cantWalk;
    /**
     * Shooting Stars
     */

    private boolean foundShootingStar;
    private long lastStarSprite;
    private int starsFound;
    /**
     * Boss Instancing.
     */
    private String lastBossInstanceKey;
    private InstanceSettings lastBossInstanceSettings;
    /**
     * Clue Scrolls
     */
    private int completedClues;
    public int easyClues;
    public int mediumClues;
    public int hardClues;
    public int eliteClues;
    private boolean acceptAid, profanityFilter;
    private byte frozenKeyCharges;
    /**
     * Player-based home areas.
     */
    private boolean edgeville, market, dZone, prifddinas;
    /**
     * RuneCrafted runes; for staves/omni-staff.
     */
    private int air, mind, water, earth, fire, body, cosmic, chaos, nature, law, death, blood, astral, soul;
    /**
     * Custom 'Supporter' (helper) rank.
     */
    private boolean support;
    /**
     * Prifddinas City and etc.
     */
    private boolean receivedCracker;
    private byte serenStonesMined;
    private short hefinLaps;
    /**
     * Bork daily.
     */
    private long lastBork;
    /**
     * Chronicle Fragment offering.
     */
    private int chroniclesOffered;
    private int taskPoints;

    /**
     * AbstractQuest FW
     */
    private transient com.rs.game.player.questing.framework.quest.QuestManager qm;

    public com.rs.game.player.questing.framework.quest.QuestManager getQM() {
        return qm;
    }

    public void setQM(final com.rs.game.player.questing.framework.quest.QuestManager qm) {
        this.qm = qm;
    }

    /**
     * Araxxor
     */
    private int araxxorEnrage;
    private int raxKills;

    public void addRaxKill() {
        raxKills++;
    }

    public void resetRaxKills() {
        raxKills = 0;
    }

    public int getRaxKC() {
        return raxKills;
    }

    private long lastAraxxorUpdate = Utils.currentTimeMillis();

    public int getAraxxorEnrage() {
        return araxxorEnrage;
    }

    public void addAraxxorEnrage(final int amt) {
        araxxorEnrage += amt;
        if (araxxorEnrage > 300)
            araxxorEnrage = 300;
    }

    public void removeAraxxorEnrage(final int amt) {
        araxxorEnrage -= amt;
    }

    public void removeAllAraxxorEnrage() {
        araxxorEnrage = 0;
    }

    private void resetEnrage() {
        final long currentTime = Utils.currentTimeMillis();
        if ((currentTime - lastAraxxorUpdate) < (24 * 60 * 60 * 1000)) {
            // 24 hours
            return;
        }
        lastAraxxorUpdate = currentTime;
        araxxorEnrage = 0;
    }

    private GorajanTrailblazer gorajan;

    public GorajanTrailblazer getGorajanTrailblazer() {
        return gorajan;
    }

    public void resetGorajanTrailblazer() {
        gorajan = new GorajanTrailblazer();
    }

    private DungeoneeringToolbelt dungToolbelt = new DungeoneeringToolbelt(this);
    private DungManager dungManager;
    private RingOfKinship ringOfKinship;
    public long silkTime;
    private long portentDelay;
    public boolean resetDg;

    public DungeoneeringToolbelt getDungeoneeringToolbelt() {
        return dungToolbelt;
    }

    public RingOfKinship getRingOfKinship() {
        return ringOfKinship;
    }

    private transient CourseManager courseManager;

    public CourseManager getCourseManager() {
        return courseManager;
    }

    public void setRingOfKinship(final RingOfKinship ring) {
        ringOfKinship = ring;
    }

    private long weakWeaponPoison, mediumWeaponPoison, strongWeaponPoison;

    public int getGravestone() {
        return gravestone;
    }

    public void setGravestone(final int id) {
        gravestone = id;
    }

    public DeathManager getDeathManager() {
        return deathManager;
    }

    public boolean isNoclip() {
        return noclip;
    }

    public void switchNoclip() {
        noclip = !noclip;
    }

    public void setDeathManager(final DeathManager manager) {
        deathManager = manager;
    }

    public long getWeakWeaponPoison() {
        return weakWeaponPoison;
    }

    public long getMediumWeaponPoison() {
        return mediumWeaponPoison;
    }

    public long getStrongWeaponPoison() {
        return strongWeaponPoison;
    }

    public void addWeakWeaponPoison(final long time) {
        weakWeaponPoison = time + Utils.currentTimeMillis();
    }

    public void addMediumWeaponPoison(final long time) {
        mediumWeaponPoison = time + Utils.currentTimeMillis();
    }

    public void addStrongWeaponPoison(final long time) {
        strongWeaponPoison = time + Utils.currentTimeMillis();
    }

    public void setWeakWeaponPoison(long time) {
        weakWeaponPoison = time;
    }

    public void setMediumWeaponPoison(long time) {
        mediumWeaponPoison = time;
    }

    public void setStrongWeaponPoison(long time) {
        strongWeaponPoison = time;
    }

    @Override
    public void applyHit(final Hit hit) {


// Global NPC -> Player damage scaling (catches direct applyHit calls too)
        if (hit != null
                && hit.getDamage() > 0
                && hit.getSource() instanceof NPC
                && hit.getLook() != HitLook.HEALED_DAMAGE) { // don't nerf healing portents/etc


            int before = hit.getDamage();
            int after = (int) Math.round(before * 0.5); // or Settings.NPC_TO_PLAYER_DAMAGE_MULT


            hit.setDamage(after);


// Use sendMessage or packets—whatever you prefer in your base
               // getPackets().sendGameMessage(
                  //      "NPC dmg scaled: " + before + " -> " + after + " (x0.5) srcNpc=" + ((NPC) hit.getSource()).getId()
               // );

        }


        super.applyHit(hit);


        if (hit.getDamage() > 0 && hit.getSource() != null && !isDead()) {
            if (Utils.random(20) == 0 && getAuraManager().hasAncestorSpirits()) {
                if (hit.getSource() != null) {
                    World.sendGraphics(this, new Graphics(2194),
                            new WorldTile(hit.getSource().getX() + hit.getSource().getSize(),
                                    hit.getSource().getY() + hit.getSource().getSize(),
                                    hit.getSource().getPlane()));
                    hit.getSource().applyHit(new Hit(this, (int) (getSkills().getLevelForXp(Skills.PRAYER) * 2.5),
                            HitLook.REGULAR_DAMAGE));
                }
            }
            if (getControlerManager().getControler() instanceof DungeonController) {
                if ((getHitpoints() - hit.getDamage()) < (getMaxHitpoints() / 2) && (getHitpoints() - hit.getDamage() > 0)) {
                    if (getPortentUseDelay() < Utils.currentTimeMillis()) {
                        for (int i = 9; i > -1; i--) {
                            final DungeoneeringDivinationData data = DungeoneeringDivinationData.values()[i];
                            if (getInventory().containsItem(data.getItemId(), 1)) {
                                applyHit(new Hit(null, data.getTertiaryBoost(), HitLook.HEALED_DAMAGE));
                                this.sendMessage("Your " + ItemDefinitions.getItemDefinitions(data.getItemId()).getName()
                                        + " heals you for " + data.getTertiaryBoost() + " lifepoints.");
                                getInventory().deleteItem(data.getItemId(), 1);
                                addPortentUseDelay(1000);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    public long getPortentUseDelay() {
        return portentDelay;
    }

    public void addPortentUseDelay(final long delay) {
        portentDelay = Utils.currentTimeMillis() + delay;
    }

    @Override
    public boolean canMove(final int dir) {
        return true;
    }

    private DungeoneeringBinds binds;

    public DungeoneeringBinds getDungeoneeringBinds() {
        return binds;
    }

    public void setDungeoneeringBinds() {
        binds = new DungeoneeringBinds();
    }

    public DungManager getDungeoneeringManager() {
        return dungManager;
    }

    public void setDungeoneeringManager(final DungManager dungManager) {
        this.dungManager = dungManager;
    }

    public void setDungeoneeringToolbelt(final DungeoneeringToolbelt toolbelt) {
        dungToolbelt = toolbelt;
    }

    /**
     * Isolated, ephemeral native character. Initialize the real entity and its
     * movement dependencies without running 910 UI scripts, account migration,
     * remote integrations or the legacy appearance serializer.
     */
    private transient com.rs.game.player.client.Native950MeleeCombat native950Combat;
    private transient com.rs.game.player.client.Native950Slayer.State native950Slayer;
    public com.rs.game.player.client.Native950Slayer.State getNative950Slayer() {
        if(native950Slayer==null)native950Slayer=new com.rs.game.player.client.Native950Slayer.State();
        return native950Slayer;
    }
    public com.rs.game.player.client.Native950MeleeCombat getNative950Combat() { return native950Combat; }
    public void setNative950Combat(com.rs.game.player.client.Native950MeleeCombat combat) { native950Combat = combat; }

    public static Player createNative950(final String username, final WorldTile tile,
                                          final io.netty.channel.Channel channel) {
        Player player = new Player(tile, ClientSession.beginNative950Gameplay(),
                java.util.Objects.requireNonNull(username, "username"));
        player.realChannel = java.util.Objects.requireNonNull(channel, "channel");
        return player;
    }

    private Player(final WorldTile tile, final ClientSession nativeSession, final String nativeUsername) {
        super(tile);
        bindClientSession(nativeSession);
        username = nativeUsername; // before hydration so its diagnostics name the player
        // The facade replaces the legacy PacketDispatcher for this player only;
        // every getPackets().X(...) call site routes through verified 947 writers.
        dispatcher = new com.rs.game.player.client.Native950PacketDispatcher(this);
        combatDefinitions = new CombatDefinitions();
        friendsIgnores = new FriendsIgnores();
        musicsManager = new MusicsManager();
        pouches = new int[4];
        cHandler = new ContractHandler();
        inventory = new Inventory();
        inventory.setPlayer(this);
        equipment = new Equipment();
        equipment.setPlayer(this);
        skills = new Skills();
        skills.setPlayer(this);
        bank = new Bank();
        controlerManager = new ControlerManager();
        controlerManager.setPlayer(this);
        emotesManager = new EmotesManager();
        emotesManager.setPlayer(this);
        xmas = new XmasEvent();
        setGlobalPlayerUpdater(new GlobalPlayerUpdater());
        getAppearence().setPlayer(this);
        runEnergy = 100;
        usingNXT = true;
        initEntity();
        setRun(false);
        // P4: every manager the native tick dereferences exists before the first
        // processEntity(); the legacy Player(password, mac) constructor and
        // LoginManager.init are never run for this player.
        hydrateForNative950();
        // Start at the level-10 maximum (100) instead of the old 1000: World's
        // restore task drains anything above getMaxHitpoints() by one every ten
        // ticks and each drain would emit an HP varbit, so an above-max start
        // is pure noise. VITALS in the schema-3 profile overrides this on restore.
        setHitpoints(getMaxHitpoints());
        setLastPacketReceivedTime(Utils.currentTimeMillis());
    }

    // ------------------------------------------------------------ P4 persistence accessors (packet-free)

    /** Sets run energy without the sendRunEnergy refresh (schema-3 VITALS restore). */
    public void setRunEnergyWithoutRefresh(final int energy) {
        if (energy < 0 || energy > 100)
            throw new IllegalArgumentException("Run energy must be 0..100");
        runEnergy = (byte) energy;
    }

    public void setLastLoggedIn(final long lastLoggedIn) {
        this.lastLoggedIn = lastLoggedIn;
    }

    /** Keys of the small named settings map persisted in the schema-3 SETTINGS section. */
    public static final String SETTING_CHAT_EFFECTS = "chatEffects", SETTING_PROFANITY_FILTER = "profanityFilter",
            SETTING_MOUSE_BUTTONS = "mouseButtons", SETTING_ACCEPT_AID = "acceptAid",
            SETTING_BANK_WITHDRAW_NOTES = "bankWithdrawNotes", SETTING_BANK_LAST_X = "bankLastX",
            SETTING_BANK_QUANTITY_MODE = "bankQuantityMode";

    /** Packet-free snapshot of client preferences, including the bank's raw X selection. */
    public java.util.Map<String, Integer> nativeSettingsSnapshot() {
        java.util.Map<String, Integer> settings = new java.util.LinkedHashMap<String, Integer>();
        settings.put(SETTING_CHAT_EFFECTS, allowChatEffects ? 1 : 0);
        settings.put(SETTING_PROFANITY_FILTER, profanityFilter ? 1 : 0);
        settings.put(SETTING_MOUSE_BUTTONS, mouseButtons ? 1 : 0);
        settings.put(SETTING_ACCEPT_AID, acceptAid ? 1 : 0);
        settings.put(SETTING_BANK_WITHDRAW_NOTES, bank.getWithdrawNotes() ? 1 : 0);
        settings.put(SETTING_BANK_LAST_X, nativeBankLastX(bank.getLastX()));
        // getDefaultInteractionAmount() resolves X to its current count and loses the mode.
        settings.put(SETTING_BANK_QUANTITY_MODE, nativeBankQuantityMode(bank.getNativeDefaultInteractionAmount()));
        if(isNative950())getNative950Slayer().writeSettings(settings);
        return settings;
    }

    /** Applies a saved settings snapshot; absent bank keys use old-profile defaults, without packets. */
    public void applyNativeSettings(final java.util.Map<String, Integer> settings) {
        if(isNative950())getNative950Slayer().restore(settings);
        Integer value;
        if ((value = settings.get(SETTING_CHAT_EFFECTS)) != null) allowChatEffects = value != 0;
        if ((value = settings.get(SETTING_PROFANITY_FILTER)) != null) profanityFilter = value != 0;
        if ((value = settings.get(SETTING_MOUSE_BUTTONS)) != null) mouseButtons = value != 0;
        if ((value = settings.get(SETTING_ACCEPT_AID)) != null) acceptAid = value != 0;
        value = settings.get(SETTING_BANK_WITHDRAW_NOTES);
        bank.restoreNativePreferences(value != null && value == 1,
                nativeBankLastX(settings.get(SETTING_BANK_LAST_X)),
                nativeBankQuantityMode(settings.get(SETTING_BANK_QUANTITY_MODE)));
    }

    private static int nativeBankLastX(final Integer value) {
        return value != null && value > 0 ? value : 1;
    }

    private static int nativeBankQuantityMode(final Integer value) {
        return value != null && (value == 1 || value == 5 || value == 10 || value == 11
                || value == Integer.MAX_VALUE) ? value : 1;
    }

    /** Exceptions caught by processEntity's outer catch for this native player (P4 tick counter). */
    private transient long nativeTickFailures;
    /**
     * processEntity entries for this native player (M2b acceptance (e)). Compared
     * against Native950Session's tick count so "processEntity ran on every native
     * tick" is an observed counter rather than an argument about the call site.
     */
    private transient long nativeProcessEntityRuns;
    /** Refreshes that legacy code asked for but that have no verified 947 packet yet (counted, not sent). */
    private transient long nativeDeferredRefreshes;
    private transient boolean nativeHydrated;

    public long getNativeTickFailures() {
        return nativeTickFailures;
    }

    /** How many times processEntity was entered for this native player. */
    public long getNativeProcessEntityRuns() {
        return nativeProcessEntityRuns;
    }

    public long getNativeDeferredRefreshes() {
        return nativeDeferredRefreshes;
    }

    /**
     * Counts a refresh that legacy content requested for a native player but that
     * cannot be sent yet (its 947 packet or var id is unverified). Nothing reaches
     * the wire; the count is surfaced through Native950Session.Snapshot.
     */
    private void deferNativeRefresh(final String what) {
        nativeDeferredRefreshes++;
        if (nativeDeferredRefreshes == 1 || nativeDeferredRefreshes % 1000 == 0)
            Logger.getGlobal().info("[Ataraxia950] " + what + " deferred for native player " + username
                    + " (" + nativeDeferredRefreshes + " deferred refreshes so far)");
    }

    /**
     * P4 hydration for native 947 players: constructs, packet-free, every manager
     * that {@link #processEntity()} and the M2b/M3 code paths dereference and that
     * only the legacy {@code Player(password, mac)} constructor or
     * {@code LoginManager.init} create. The list deliberately duplicates the
     * relevant lines of {@code LoginManager.init} instead of extracting a shared
     * helper: {@code init} interleaves packet emitters ({@code setDefaultVariables},
     * {@code increaseAFKTimer}, {@code World.addPlayer}) with the null-guards, so a
     * shared helper could not be proven sequence-identical for legacy logins by
     * diff. Each manager's packet-freeness (constructor, setPlayer, process) and
     * CoresManager use is recorded in notes/P4-lifecycle.md.
     *
     * <p>Idempotent: managers that already exist are kept. Never emits a packet;
     * the only side effect beyond field assignment is marking the account pin as
     * entered, exactly as {@code LoginManager.initBot} does, so
     * {@code accountPin.forceLock()} in processEntity never fires.
     */
    public void hydrateForNative950() {
        if (!isNative950())
            throw new IllegalStateException("hydrateForNative950 is only for native 947 players");
        if (nativeHydrated) {
            // Idempotent, but never a no-op: re-checking is how a manager that
            // content cleared after admission is caught before the next tick.
            requireNative950TickManagers();
            return;
        }
        // -- managers processEntity dereferences every tick (Player.java processEntity body)
        if (cutscenesManager == null)
            cutscenesManager = new CutscenesManager(this);
        // AuraManager / InventionManager size their arrays from cache enums
        // (13430, 10742, 10743) in their constructors. Both now size through a
        // guarded helper that yields a usable, empty manager when cache index 17
        // is absent, so hydration succeeds with or without a cache and the tick
        // can never dereference a null. Legacy sizing is unchanged whenever the
        // enum is present; see notes/P4-lifecycle.md.
        if (auraManager == null)
            auraManager = new AuraManager();
        auraManager.setPlayer(this);
        if (actionManager == null)
            actionManager = new ActionManager(this);
        if (inventionManager == null)
            inventionManager = new InventionManager();
        inventionManager.setPlayer(this);
        if (prayer == null)
            prayer = new Prayer();
        prayer.setPlayer(this);
        if (dayOfWeekManager == null)
            dayOfWeekManager = new DayOfWeekManager();
        dayOfWeekManager.setPlayer(this);
        if (buffDebuffTimersManager == null)
            buffDebuffTimersManager = new BuffDebuffTimersManager();
        buffDebuffTimersManager.setPlayer(this);
        if (farmingManager == null)
            farmingManager = new FarmingManager();
        farmingManager.setPlayer(this);
        if (playerExamineManager == null)
            playerExamineManager = new PlayerExamineManager();
        playerExamineManager.setPlayer(this);
        if (accountPin == null)
            accountPin = new AccountPin(this);
        accountPin.setPlayer(this);
        accountPin.resetLocked();
        accountPin.setPinEntered(); // as LoginManager.initBot: no pin UI exists natively
        if (interfaceManager == null)
            interfaceManager = new InterfaceManager(this);
        if (activityTimersManager == null)
            activityTimersManager = new ActivityTimersManager();
        activityTimersManager.setPlayer(this); // CombatDefinitions.processCombatStance
        if (visWaxManager == null)
            visWaxManager = new VisWaxManager();
        visWaxManager.setPlayer(this); // DivineObject.resetGatherLimit
        if (activePotions == null)
            activePotions = new ArrayList<Integer>(); // PotionTimerInterface gate
        // -- managers the M2b/M3 paths (Skills.init, Bank, Equipment, dialogue) dereference
        if (questManager == null)
            questManager = new QuestManager();
        questManager.setPlayer(this);
        if (toolBelt == null)
            // ALWAYS constructed: Inventory.containsItem / containsOneItem
            // dereference getToolBelt() with no guard, and Player.stopAll() reaches
            // them through CombatDefinitions.resetSpells -> Magic.getTotalAmountOfRune
            // on the very first routed object click (ObjectHandler.dispatch calls
            // stopAll() before anything else). Seeding the default tools walks enum
            // 13730 -> struct params 6979/6980 -> enum 2433, which is unverified on
            // the 947 cache and absent with no cache at all; Toolbelt.addDefaultItems
            // detects that (Toolbelt.defaultToolChainAvailable) and leaves the belt
            // EMPTY rather than throwing, so the constructor is packet-free and
            // cache-free here and contains() answers "no tool" - the fail-closed
            // answer. The empty belt also refuses addItem(), so no inventory item can
            // be consumed into a belt that cannot be refreshed (M8 lifts both).
            toolBelt = new Toolbelt(this);
        toolBelt.setPlayer(this);
        if (toolBeltNew == null)
            toolBeltNew = new ToolbeltNew(this);
        toolBeltNew.setPlayer(this);
        if (perkManager == null)
            perkManager = new PerkManager();
        perkManager.setPlayer(this);
        if (deathManager == null)
            deathManager = new DeathManager();
        deathManager.setPlayer(this);
        if (gemBag == null)
            // InventoryOptionsHandler.handleItemOption2 (the M2b Wear path for
            // 1473:5 option 2) calls getGemBag().withdraw(id) before its own id
            // branches; the constructor stores the player and withdraw() returns
            // false at once for anything but item 18338.
            setGemBag();
        gemBag.setPlayer(this);
        if (binds == null)
            // ButtonHandler.handleButtons calls getDungeoneeringBinds().processButtonClick
            // before the 517/1473/1462 branches; the constructor only flips
            // activeLoadouts[0] and processButtonClick returns false for any
            // interface other than 116, so it is packet-free and cache-free.
            setDungeoneeringBinds();
        binds.setPlayer(this);
        if (treasureTrails == null)
            // ObjectHandler.handleOption2 asks getTreasureTrails().useObject(object)
            // before any of its id branches, so the M2b bank chest (79036 option 2)
            // NPEs without it. Constructor and useObject are packet-free and
            // cache-free; with no clue in progress useObject returns false at once.
            treasureTrails = new TreasureTrails();
        treasureTrails.setPlayer(this);
        if (dialogueManager == null)
            dialogueManager = new DialogueManager(this);
        if (hintIconsManager == null)
            hintIconsManager = new HintIconsManager(this);
        if (priceCheckManager == null)
            priceCheckManager = new PriceCheckManager(this);
        getVarBitManager(); // lazy VarBitManager over the shared VarsManager (P2)
        // -- setPlayer wiring LoginManager.init performs for managers this constructor already built
        combatDefinitions.setPlayer(this);
        bank.setPlayer(this);
        friendsIgnores.setPlayer(this);
        musicsManager.setPlayer(this); // Native regional playback is session-owned.
        if (switchItemCache == null)
            setSwitchItemCache(Collections.synchronizedList(new ArrayList<Integer>()));
        if (banks == null)
            // M3: Player.hasItem(Item) iterates getBanks() with no null guard and is
            // reached from Skills.handleSkillShards on roughly half of all experience
            // awards, so an unbuilt list NPEs the first time content awards xp. The
            // legacy Player(password, mac) constructor builds the same empty list;
            // the primary bank lives in the `bank` field, not in this list.
            banks = new ArrayList<Bank>();
        requireNative950TickManagers();
        nativeHydrated = true;
    }

    /**
     * P4 fail-closed admission gate. Every manager {@link #processEntity()}
     * dereferences without a null guard must exist before the player is allowed
     * on the native world; a silent null would NPE on the very first tick (which
     * lenient mode would swallow) instead of refusing admission. Called at the
     * end of {@link #hydrateForNative950()}, so a construction that could not be
     * completed aborts {@code Player.createNative950} and therefore
     * {@code Native950World.attachOnWorld}, whose caller reports the message.
     *
     * <p>Deliberately NOT on this list: the legacy-only fields
     * {@code logicPackets}, {@code bountyHunter}, {@code squealOfFortune} and
     * friends, which neither the native tick nor the M2b router path reads.
     * {@code toolBelt} used to be excluded on the premise that every native call
     * site goes through {@code toolBeltNew}; that was wrong -
     * {@code Inventory.containsItem} and {@code Inventory.containsOneItem}
     * dereference it unguarded and {@code Player.stopAll()} reaches them - so it
     * is required here and constructed unconditionally above.
     */
    private void requireNative950TickManagers() {
        requireNative950Manager("cutscenesManager", cutscenesManager);
        requireNative950Manager("auraManager", auraManager);
        requireNative950Manager("actionManager", actionManager);
        requireNative950Manager("inventionManager", inventionManager);
        requireNative950Manager("prayer", prayer);
        requireNative950Manager("dayOfWeekManager", dayOfWeekManager);
        requireNative950Manager("buffDebuffTimersManager", buffDebuffTimersManager);
        requireNative950Manager("controlerManager", controlerManager);
        requireNative950Manager("farmingManager", farmingManager);
        requireNative950Manager("playerExamineManager", playerExamineManager);
        requireNative950Manager("accountPin", accountPin);
        requireNative950Manager("interfaceManager", interfaceManager);
        requireNative950Manager("musicsManager", musicsManager);
        requireNative950Manager("combatDefinitions", combatDefinitions);
        requireNative950Manager("activityTimersManager", activityTimersManager);
        requireNative950Manager("visWaxManager", visWaxManager);
        requireNative950Manager("activePotions", activePotions);
        requireNative950Manager("questManager", questManager);
        requireNative950Manager("toolBeltNew", toolBeltNew);
        requireNative950Manager("toolBelt", toolBelt); // Inventory.containsItem/containsOneItem, reached by stopAll()
        requireNative950Manager("perkManager", perkManager);
        requireNative950Manager("deathManager", deathManager);
        requireNative950Manager("dialogueManager", dialogueManager);
        requireNative950Manager("hintIconsManager", hintIconsManager);
        requireNative950Manager("priceCheckManager", priceCheckManager);
        requireNative950Manager("varBitManager", getVarBitManager());
        requireNative950Manager("inventory", inventory);
        requireNative950Manager("equipment", equipment);
        requireNative950Manager("bank", bank);
        requireNative950Manager("banks", banks); // Player.hasItem(Item) iterates it unguarded
        requireNative950Manager("skills", skills);
        requireNative950Manager("appearence", getAppearence());
        requireNative950Manager("switchItemCache", switchItemCache);
        // M2b router path: ObjectHandler/NPCHandler/ButtonHandler dereference these
        // before any id branch, so a null is a landmine for the very first click.
        requireNative950Manager("treasureTrails", treasureTrails);
        requireNative950Manager("emotesManager", emotesManager);
        requireNative950Manager("dungeoneeringBinds", binds);
        requireNative950Manager("gemBag", gemBag);
    }

    private void requireNative950Manager(final String name, final Object manager) {
        if (manager != null)
            return;
        throw new IllegalStateException("Native 947 admission refused for " + username + ": manager '" + name
                + "' could not be constructed, and Player.processEntity dereferences it every tick."
                + " Initialize the 947 cache (Cache.initFlatReadOnly) and run Native950Bootstrap before"
                + " admitting a player, then retry the login.");
    }

    // creates Player and saved classes
    public Player(final String password, final String mac) {
        super(Settings.START_PLAYER_LOCATION);
        setHitpoints(Settings.START_PLAYER_HITPOINTS);
        this.password = password;
        registeredMac = mac;
        dayOfWeekManager = new DayOfWeekManager();
        setGlobalPlayerUpdater(new GlobalPlayerUpdater());
        inventory = new Inventory();
        equipment = new Equipment();
        skills = new Skills();
        bountyHunter = new BountyHunter();
        cHandler = new ContractHandler();
        reaperPerks = new ArrayList<ReaperPerks>();
        squealOfFortune = new SquealOfFortune();
        coOpSlayer = new CooperativeSlayer();
        combatDefinitions = new CombatDefinitions();
        unlockedCostumesIds = new ArrayList<Integer>();
        mauledWeeksNM = new boolean[6];
        mauledWeeksHM = new boolean[6];
        artisansWorkShop = new ArtisansWorkShop();
        prayer = new Prayer();
        bank = new Bank();
        metalBankStorage = new MetalBankStorage();
        metalBank = new HashMap<Integer, Integer>();
        oreBoxContents = new HashMap<Integer, Integer>();
        banks = new ArrayList<Bank>();
        overrides = new CosmeticOverrides();
        animations = new AnimationOverrides();
        controlerManager = new ControlerManager();
        treasureTrails = new TreasureTrails();
        prayerBook = new boolean[PrayerBooks.BOOKS.length];
        farmingManager = new FarmingManager();
        musicsManager = new MusicsManager();
        emotesManager = new EmotesManager();
        ports = new PlayerOwnedPort();
        xmas = new XmasEvent();
        friendsIgnores = new FriendsIgnores();
        dominionTower = new DominionTower();
        house = new House();
        charges = new ChargesManager();
        auraManager = new AuraManager();
        setActivityTimersManager(new ActivityTimersManager());
        questManager = new QuestManager();
        visWaxManager = new VisWaxManager();
        dungManager = new DungManager(this);
        petManager = new PetManager();
        geManager = new GrandExchangeManager();
        dicingManager = new DicingManager();
        toolBelt = new Toolbelt(this);
        toolBeltNew = new ToolbeltNew(this);
        perkManager = new PerkManager();
        lootBeamManager = new LootBeamManager();
        playerExamineManager = new PlayerExamineManager();
        titles = new Titles();
        runEnergy = 100;
        allowChatEffects = true;
        profanityFilter = true;
        mouseButtons = true;
        pouches = new int[4];
        warriorPoints = new double[6];
        resetBarrows();
        killStats = new int[512];
        boons = new boolean[12];
        claimedDonationAwardBoxes = new boolean[6];
        SkillCapeCustomizer.resetSkillCapes(this);
        ownedObjectsManagerKeys = new LinkedList<String>();
        setCreationDate(Utils.currentTimeMillis());
        currentFriendChatOwner = "xhybrid";
        uniqueItems = new HashMap<Integer, Integer>();
        bossPortal = BossPortalType.UNDIRECTED;
        inventionManager = new InventionManager();
        chargesManagerNew = new ChargesManagerNew();
        treasureHunter = new TreasureHunter();
        eliteDungeonsManager = new EliteDungeonsManager();
        subMenus = new int[12];
        actionbar = new ActionBar();
        doomsayerManager = new DoomsayerManager();
        buffDebuffTimersManager = new BuffDebuffTimersManager();
        setCapeCustomizationPresets(new int[3][4]);
        meilyrShopSettings = new boolean[16];
        meilyrShopSettings2 = new boolean[10];
        for(int i=0;i<getCapeCustomizationPresets().length;i++)
            getCapeCustomizationPresets()[i] = Arrays.copyOf(ItemDefinitions.getItemDefinitions(20771).originalModelColors, 4);
        cosmeticsManager = new CosmeticsManager();
    }

    public int getBankSpace() {
        return bankspace;
    }

    public void setBankSpace(final int amount) {
        bankspace = amount;
    }

    public int getTaskStreak() {
        return taskStreak;
    }

    public void setTaskStreak(final int amount) {
        taskStreak = amount;
        updateSlayerCounterInformation();
    }

    public boolean hasXmasTitleUnlocked() {
        return (xmasTitle1 || xmasTitle2 || xmasTitle3 || xmasTitle4);
    }

    public void addFireImmune(final long time) {
        fireImmune = time + Utils.currentTimeMillis();
    }

    public void addFoodDelay(final long time) {
        foodDelay = time + Utils.currentTimeMillis();
    }

    public void addLogicPacketToQueue(final LogicPacket toAdd) {
        final Iterator<LogicPacket> it$ = logicPackets.iterator();
        while (it$.hasNext()) {
            final LogicPacket packet = it$.next();
            if (packet.getId() == toAdd.getId()) {
                it$.remove();
                break;
            }
        }

        logicPackets.add(toAdd);
    }

    public void addPoisonImmune(final long time) {
        poisonImmune = time + Utils.currentTimeMillis();
//        getPoison().reset();
    }

    public void addPolDelay(final long delay) {
        polDelay = delay + Utils.currentTimeMillis();
    }

    public void addBloodDelay(final long time) {
        bloodDelay = time + Utils.currentTimeMillis();
    }

    public void addPotDelay(final long time) {
        potDelay = time + Utils.currentTimeMillis();
    }

    /**
     * Adds points
     *
     * @param points
     */
    public void addRunespanPoints(final int points) {
        runeSpanPoints += points;
    }

    public boolean canSpawn() {
        if (Wilderness.isAtWild(this) || getControlerManager().getControler() instanceof FightPitsArena || getControlerManager().getControler() instanceof CorpBeastController || getControlerManager().getControler() instanceof PestControlLobby || getControlerManager().getControler() instanceof PestControlGame || getControlerManager().getControler() instanceof ZGDController || getControlerManager().getControler() instanceof GodWars || getControlerManager().getControler() instanceof JailController || getControlerManager().getControler() instanceof DTController || getControlerManager().getControler() instanceof WarControler || getControlerManager().getControler() instanceof DeathController || getControlerManager().getControler() instanceof BarrelchestController || getControlerManager().getControler() instanceof DuelArena || getControlerManager().getControler() instanceof CastleWarsPlaying || getControlerManager().getControler() instanceof CastleWarsWaiting || getControlerManager().getControler() instanceof FightCaves || getControlerManager().getControler() instanceof FightKiln || getControlerManager().getControler() instanceof ImpossibleJad || getControlerManager().getControler() instanceof BarrelchestController || FfaZone.inPvpArea(this) || getControlerManager().getControler() instanceof NomadsRequiem || getControlerManager().getControler() instanceof QueenBlackDragonController || getControlerManager().getControler() instanceof VoragoInstanceController || World.isAtAscensionDungeon(this) || /* South West */(getX() >= 2955 && getY() >= 1735 && // kalphite
                // king
                // lair
                /* North East */getX() <= 2997 && getY() <= 1783)

                || /* South West */(getX() >= 3009 && getY() >= 5955 && // vorago
                // borehole
                /* North East */getX() <= 3135 && getY() <= 6136) || this.getControlerManager().getControler() instanceof AreaController || this.getControlerManager().getControler() instanceof LobbyController || this.getControlerManager().getControler() instanceof GameController
                || getControlerManager().getControler() instanceof EliteDungeonController) {
            return false;
        }

        if (getControlerManager().getControler() instanceof CrucibleController) {
            final CrucibleController controler = (CrucibleController) getControlerManager().getControler();
            return !controler.isInside();
        }

        return true;
    }

    public void checkMovement(final int x, final int y, final int plane) {
        Magic.teleControlersCheck(this, new WorldTile(x, y, plane));
    }

    @Override
    public void checkMultiArea() {
        if (!isActive()) {
            return;
        }
        final boolean isAtMultiArea = isForceMultiArea() || ((DemonFlashMobs.getDemonFlashMobs() != null && DemonFlashMobs.getDemonFlashMobs().isMultiArea(this)) || World.isMultiArea(this));
        if (isAtMultiArea && !isAtMultiArea()) {
            setAtMultiArea(isAtMultiArea);
            getPackets().sendGlobalConfig(616, 1);
        } else if (!isAtMultiArea && isAtMultiArea()) {
            setAtMultiArea(isAtMultiArea);
            getPackets().sendGlobalConfig(616, 0);
        }
    }



    public void setFinishedRoTS() {
        hasLootedROTS = true;
    }

    public transient boolean closeInterfaceLocked;

    /**
     * Closes all on-screen interfaces.
     */
    public void closeInterfaces() {
        if (closeInterfaceLocked) {
            return;
        }
        if (isNative950()) {
            interfaceManager.closeSettings();
            interfaceManager.closeNative950Lodestones();
            interfaceManager.closeNative950SkillGuide();
            interfaceManager.closeNative950ProductionMenu();
            interfaceManager.closeNative950ToolbeltUi();
            interfaceManager.closeNative950ForgeUi();
        }
        if (interfaceManager.containsScreenInter()) {
            interfaceManager.closeScreenInterface();
        }
        if (interfaceManager.containsInventoryInter()) {
            interfaceManager.closeInventoryInterface();
        }
        if (interfaceManager.containsBankInterface())
            interfaceManager.removeBankInterface();
        if (interfaceManager.containsInputTextInterface())
            interfaceManager.removeInputTextInterface();
        if (interfaceManager.containsWorldMapInterface())
            interfaceManager.removeWorldMapInterface();
        if (interfaceManager.containsCentralInterfaceLargeInterface())
            interfaceManager.removeCentralInterfaceLargeInterface();
        if (interfaceManager.containsPlayerInspectInterface())
            interfaceManager.removePlayerInspectInterface();
        dialogueManager.finishDialogue();
        if (closeInterfacesEvent != null) {
            closeInterfacesEvent.run();
            closeInterfacesEvent = null;
        }
        if (bank.isInSearchMode())
            bank.closeSearchMode();
        if (interfaceManager.containsChatBoxInter())
            getInterfaceManager().closeChatBoxInterface();
    }

    public void closeAllExceptScreenInterfaces() {
        if (interfaceManager.containsInventoryInter()) {
            interfaceManager.closeInventoryInterface();
        }
        dialogueManager.finishDialogue();
        if (closeInterfacesEvent != null) {
            closeInterfacesEvent.run();
            closeInterfacesEvent = null;
        }
        getInterfaceManager().closeChatBoxInterface();
    }

    private transient double extraDrain;

    public void drainRunEnergy() {
        if (getPerkManager().hasPerkActive(DonationPerk.ENDLESS_ENERGY)) {
            return;
        }
        final double rate = 0.3 + (getWeight() * 0.0027);
        extraDrain += rate;
        if (extraDrain >= 1) {
            setRunEnergy(runEnergy - 1);
            extraDrain--;
        }
    }

    @Override
    public void finish() {
        finish(0);
    }

    public void finish(final int tryCount) {
        if (finishing || hasFinished()) {
            return;
        }
        finishing = true;
        stopAll(false, true, !(actionManager.getAction() instanceof PlayerCombat));
        if ((getAttackedByDelay() + 10000 > Utils.currentTimeMillis() && tryCount < 6) || getEmotesManager().isDoingEmote() || isLocked() || isDead() || (flowerPokerSession != null && tryCount < 5)) {
            CoresManager.getServiceProvider().executeWithDelay(() -> {
                try {
                    packetsDecoderPing = Utils.currentTimeMillis();
                    finishing = false;
                    finish(tryCount + 1);
                } catch (final Throwable e) {
                    Logger.getGlobal().catching(e);
                }
            }, 10, TimeUnit.SECONDS);
            return;
        }
        realFinish();
    }

    public String lowestShip() {
        String finale = "";
        int minX = Integer.MAX_VALUE;
        final int[] ships = {getPorts().getFirstVoyageMinsLeft(), getPorts().getSecondVoyageMinsLeft(), getPorts().getThirdVoyageMinsLeft(), getPorts().getFourthVoyageMinsLeft(), getPorts().getFifthVoyageMinsLeft()};
        final String[] names = {"first", "second", "third", "fourth", "fifth"};
        for (int i = 0; i < ships.length; i++) {
            if (ships[i] > 0 && ships[i] < minX) {
                minX = ships[i];
            }
        }
        for (int i = 0; i < ships.length; i++) {
            if (minX == ships[i]) {
                finale += names[i] + " ship has " + minX + " minutes left! ";
            }
        }
        return finale;
    }

    public void checkPorts() {
        if (!getPorts().hasFirstShip || getPorts().firstShipVoyage == 0 && getPorts().secondShipVoyage == 0 && getPorts().thirdShipVoyage == 0 && getPorts().fourthShipVoyage == 0 && getPorts().fifthShipVoyage == 0) {
            return;
        }
        if (getPorts().hasFirstShipReturned() && getPorts().firstShipVoyage != 0 || getPorts().hasSecondShipReturned() && getPorts().secondShipVoyage != 0 || getPorts().hasThirdShipReturned() && getPorts().thirdShipVoyage != 0 || getPorts().hasFourthShipReturned() && getPorts().fourthShipVoyage != 0 || getPorts().hasFifthShipReturned() && getPorts().fifthShipVoyage != 0) {
            sendMessage("<img=7>" + Colors.SALMON + "[" + getDisplayName() + "'s Port]:" + Colors.GREEN + " One or more of your ships have returned!", false);
        } else {
            if (lowestShip() != "") {
                sendMessage("<img=7>" + Colors.SALMON + "[" + getDisplayName() + "'s Port]:" + Colors.ORANGE + " Your " + lowestShip());
            }
        }
    }

    public int bloodNeckHeal(int cap, final int heal) {
        if ((heal + cap) > 350) {
            return 350;
        }
        heal(heal);
        cap += heal;
        return cap;
    }

    public boolean isUnderCombat(final int tryCount) {
        return (getAttackedByDelay() + (tryCount * 1000) > Utils.currentTimeMillis());
    }

    public void forceLogout() {
        getPackets().sendLogout();
        setRunning(false);
        setFinished(false);
        realFinish();
        World.forceRemovePlayer(this);
    }

    public void forceSession() {
        setRunning(false);
        realFinish();
    }

    public ActionManager getActionManager() {
        return actionManager;
    }

    public AuraManager getAuraManager() {
        return auraManager;
    }

    /*
     * All of the data for new banking system
     */

    public Bank getBank() {
        return bank;
    }

    public Bank getMetalBankStorage() {
        if (!(metalBankStorage instanceof MetalBankStorage))
            metalBankStorage = new MetalBankStorage();
        metalBankStorage.setPlayer(this);
        migrateLegacyMetalBank();
        return metalBankStorage;
    }

    public void setMetalBankOpenBank(Bank previousBank) {
        if (previousBank instanceof MetalBankStorage)
            previousBank = banks != null && !banks.isEmpty() ? banks.get(0) : null;
        previousBankBeforeMetalBank = previousBank;
        setBank(getMetalBankStorage());
    }

    public void restoreBankAfterMetalBank() {
        if (!(bank instanceof MetalBankStorage))
            return;
        Bank restore = previousBankBeforeMetalBank;
        previousBankBeforeMetalBank = null;
        if (restore == null && banks != null && !banks.isEmpty())
            restore = banks.get(0);
        if (restore == null)
            restore = new Bank();
        setBank(restore);
    }

    public Map<Integer, Integer> getMetalBank() {
        if (metalBank == null)
            metalBank = new HashMap<Integer, Integer>();
        return metalBank;
    }

    public Map<Integer, Integer> getOreBoxContents() {
        if (oreBoxContents == null)
            oreBoxContents = new HashMap<Integer, Integer>();
        return oreBoxContents;
    }

    public int getMetalBankAmount(int itemId) {
        itemId = getUnnotedMetalBankItemId(itemId);
        Item item = getMetalBankStorage().getItem(itemId);
        return item == null ? 0 : item.getAmount();
    }

    public void addMetalBankItem(int itemId, int amount) {
        if (itemId <= 0 || amount <= 0)
            return;
        itemId = getUnnotedMetalBankItemId(itemId);
        int current = getMetalBankAmount(itemId);
        int safeAmount = (long) current + amount > Integer.MAX_VALUE ? Integer.MAX_VALUE - current : amount;
        if (safeAmount > 0) {
            Bank metalBank = getMetalBankStorage();
            metalBank.addItem(itemId, safeAmount, 0, null, false);
            metalBank.lastContainerCopy = null;
        }
    }

    public int removeMetalBankItem(int itemId, int amount) {
        if (itemId <= 0 || amount <= 0)
            return 0;
        itemId = getUnnotedMetalBankItemId(itemId);
        int current = getMetalBankAmount(itemId);
        int removed = Math.min(current, amount);
        if (removed <= 0)
            return 0;
        Bank metalBank = getMetalBankStorage();
        metalBank.removeItem(metalBank.getItemSlotCheckIdOnly(itemId), removed, false, Bank.DESTROY_ITEM);
        metalBank.lastContainerCopy = null;
        return removed;
    }

    private void migrateLegacyMetalBank() {
        if (metalBank == null || metalBank.isEmpty())
            return;
        Map<Integer, Integer> legacyMetalBank = new HashMap<Integer, Integer>(metalBank);
        metalBank.clear();
        for (Map.Entry<Integer, Integer> entry : legacyMetalBank.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null || entry.getValue() <= 0)
                continue;
            getMetalBankStorage().addItem(entry.getKey(), entry.getValue(), 0, null, false);
        }
    }

    private int getUnnotedMetalBankItemId(int itemId) {
        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(itemId);
        if (defs != null && defs.isNoted() && defs.getCertId() != -1)
            return defs.getCertId();
        return itemId;
    }

    public void setBank(final Bank bank) {
        this.bank = bank;
        this.bank.setPlayer(this);
    }

    public List<Bank> getBanks() {
        return banks;
    }

    public void setBanks(final List<Bank> banklist) {
        banks = banklist;
    }

    public void addBank(final boolean remove, final Bank bank) {
        if (!remove) {
            banks.add(bank);
        } else {
            banks.remove(bank);
        }
    }

    /* End new bank data */

    public long getBanned() {
        return banned;
    }

    public void setBanned(final long banned) {
        this.banned = banned;
    }

    public int getBarrowsKillCount() {
        return barrowsKillCount;
    }

    public int getBarrowsRunsDone() {
        return barrowsRunsDone;
    }

    public int getRiseOfTheSixRunsCompleted() {
        return riseOfTheSixRunsCompleted;
    }

    public void setRiseOfTheSixRunsCompleted(final int amount) {
        riseOfTheSixRunsCompleted = amount;
    }

    public void incrementRiseOfTheSixRunsCompleted() {
        riseOfTheSixRunsCompleted++;
    }

    public long getBoneDelay() {
        return boneDelay;
    }

    public ChargesManager getCharges() {
        return charges;
    }

    public int getClanStatus() {
        return clanStatus;
    }

    public void setClanStatus(final int clanStatus) {
        this.clanStatus = clanStatus;
        getPackets().sendGameBarStages();
    }

    public CombatDefinitions getCombatDefinitions() {
        return combatDefinitions;
    }

    public int[] getCompletionistCapeCustomized() {
        return completionistCapeCustomized;
    }

    public void setCompletionistCapeCustomized(final int[] skillcapeCustomized) {
        completionistCapeCustomized = skillcapeCustomized;
    }

    public ControlerManager getControlerManager() {
        return controlerManager;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(final long creationDate) {
        this.creationDate = creationDate;
    }

    public int getCrucibleHighScore() {
        return crucibleHighScore;
    }

    public FriendChatsManager getCurrentFriendChat() {
        return currentFriendChat;
    }

    public void setCurrentFriendChat(final FriendChatsManager currentFriendChat) {
        this.currentFriendChat = currentFriendChat;
    }

    public String getCurrentFriendChatOwner() {
        return currentFriendChatOwner;
    }

    public void setCurrentFriendChatOwner(final String currentFriendChatOwner) {
        this.currentFriendChatOwner = currentFriendChatOwner;
    }

    public String getCurrentMac() {
        return currentMac;
    }

    public void setCurrentMac(final String currentMac) {
        this.currentMac = currentMac;
    }

    public CutscenesManager getCutscenesManager() {
        return cutscenesManager;
    }

    public int getDeathCount() {
        return deathCount;
    }

    public DialogueManager getDialogueManager() {
        return dialogueManager;
    }

    public int getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(final int displayMode) {
        this.displayMode = displayMode;
    }

    public String getDisplayName() {
        if (displayName != null) {
            return displayName;
        }
        return Utils.formatPlayerNameForDisplay(username);
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    /**
     * Pet perk variables
     */
    /**
     * lists of the pets a player obtained, with the perks.
     */
    @Getter
    @Setter
    public List<ObtainedPet> obtainedPets = new ArrayList<>();
    /**
     * this list constains the perks that the player has redeemed but not yet used
     */
    @Getter
    @Setter
    public List<PetPerk> obtainedPetPerks = new ArrayList<>();
    /**
     * represents the current perkpet the player has spaxned
     */
    @Getter
    @Setter
    public ObtainedPet currentPet;
    /**
     * the pet the player set as his favorite
     */
    @Getter
    @Setter
    public Pets favoritePet;
    /**
     * represents if the players wants every familiar to look like his favorite pet
     */
    @Getter
    @Setter
    public boolean useFavoriteAsOverride;
    /**
     * end pet pet variables
     */
    /**
     * Misc account settings
     */

    private boolean warnEmpty;
    private boolean bankprompt;
    public boolean hasOffhandCombat = true;
    public boolean secondary;

    public boolean getWarnEmpty() {
        return warnEmpty;
    }

    public void setWarnEmpty(final boolean mode) {
        warnEmpty = mode;
    }

    public boolean showIcon() {
        return donatorIcon;
    }

    public void setIcon(final boolean donatorIcon) {
        this.donatorIcon = donatorIcon;
    }

    private boolean randomEventDisabled;

    public boolean hasRandomEvent() {
        return !randomEventDisabled;
    }

    public void setRandomEvent(final boolean val) {
        randomEventDisabled = val;
    }

    public boolean promptList() {
        return bankprompt;
    }

    public void setBankPrompt(final boolean bankprompt) {
        this.bankprompt = bankprompt;
    }

    /* end misc account settings */

    public DominionTower getDominionTower() {
        return dominionTower;
    }

    @SuppressWarnings("deprecation")
    public String getDonatorTill() {
        return (donator ? "never" : new Date(donatorTill).toGMTString()) + ".";
    }

    public DuelArena getDuelArena() {
        return duelarena;
    }

    public EmotesManager getEmotesManager() {
        return emotesManager;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    @SuppressWarnings("deprecation")
    public String getExtremeDonatorTill() {
        return (extremeDonator ? "never" : new Date(extremeDonatorTill).toGMTString()) + ".";
    }

    public Familiar getFamiliar() {
        return familiar;
    }

    public void setFamiliar(final Familiar familiar) {
        this.familiar = familiar;
    }

    public long getFireImmune() {
        return fireImmune;
    }

    public long getFoodDelay() {
        return foodDelay;
    }

    public FriendsIgnores getFriendsIgnores() {
        return friendsIgnores;
    }

    public int getHiddenBrother() {
        return hiddenBrother;
    }

    public void setHiddenBrother(final int hiddenBrother) {
        this.hiddenBrother = hiddenBrother;
    }

    public HintIconsManager getHintIconsManager() {
        return hintIconsManager;
    }

    public double getHpBoostMultiplier() {
        return hpBoostMultiplier;
    }

    public void setHpBoostMultiplier(final double hpBoostMultiplier) {
        this.hpBoostMultiplier = hpBoostMultiplier;
    }

    public InterfaceManager getInterfaceManager() {
        return interfaceManager;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public IsaacKeyPair getIsaacKeyPair() {
        return isaacKeyPair;
    }

    public long getJailed() {
        return jailed;
    }

    public void setJailed(final long jailed) {
        this.jailed = jailed;
    }

    public int getKillCount() {
        return killCount;
    }

    public int getKillStreak() {
        return killStreak;
    }

    public int getKillStreakPoints() {
        return killStreakPoints;
    }

    public int getTotalKillStreakPoints() {
        return totalkillStreakPoints;
    }

    public boolean[] getKilledBarrowBrothers() {
        return killedBarrowBrothers;
    }

    public int getLastBonfire() {
        return lastBonfire;
    }

    public void setLastBonfire(final int lastBonfire) {
        this.lastBonfire = lastBonfire;
    }

    public String getLastHostname() {
        InetAddress addr;
        try {
            addr = InetAddress.getByName(getLastIP());
            final String hostname = addr.getHostName();
            return hostname;
        } catch (final UnknownHostException e) {
            Logger.getGlobal().catching(e);
        }
        return null;
    }

    public String getLastIP() {
        return lastIP;
    }

    public void setLastIP(final String lastIP) {
        this.lastIP = lastIP;
    }

    public long getLastLoggedIn() {
        return lastLoggedIn;
    }

    public long getLastPublicMessage() {
        return lastPublicMessage;
    }

    public void setLastPublicMessage(final long lastPublicMessage) {
        this.lastPublicMessage = lastPublicMessage;
    }

    public LocalNPCUpdate getLocalNPCUpdate() {
        return localNPCUpdate;
    }

    public LocalPlayerUpdate getLocalPlayerUpdate() {
        return localPlayerUpdate;
    }

    public long getLockDelay() {
        return lockDelay;
    }

    @Override
    public double getMagePrayerMultiplier() {
        return 0.6;
    }

    private transient long lastReaperNecklaceEffect;
    private transient double reaperAccuracyBoost;

    public double getReaperHitChanceBoost() {
        return reaperAccuracyBoost;
    }

    public void setReaperAccuracyBoost(final double boost) {
        reaperAccuracyBoost = boost;
    }

    public long getLastReaperNecklaceEffect() {
        return lastReaperNecklaceEffect;
    }

    public void setLastReaperNecklaceEffect(final long time) {
        lastReaperNecklaceEffect = time;
    }

    @Override
    public int getMaxHitpoints() {
        return (getSkills().getLevel(Skills.HITPOINTS) - getHPReduction()) * 10 + equipment.getEquipmentHpIncrease();
    }

    @Override
    public double getMeleePrayerMultiplier() {
        if (Utils.random(5) == 0 && Combat.fullVeracsEquipped(this))
            return 1.0;
        return 0.6;
    }

    public int getMessageIcon() {
        if (getMessageIconOverride() > 0) {
            return getMessageIconOverride();
        }
        if (getUsername().equalsIgnoreCase("xhybrid")) {
            return Icons.ORANGE_CROWN;
        }
       // if (getUsername().equalsIgnoreCase("kirita")) {
       //     return Icons.CYAN_CROWN;
       // }
        if (isFakeDev()) {
            return Icons.DEVELOPER;
        }
       // if (getRights() == 2) {
        //    return Icons.ADMINISTRATOR + 1;
       // }
        if (getRights() == 1) {
            return Icons.MODERATOR + 1;
        }
        if (isSupport()) {
            return Icons.SUPPORT;
        }
        if (isPlayerOfTheMonth() && isDisplayPlayerOfTheMonthIcon()) {
            return Icons.PLAYER_OF_THE_MONTH;
        }
        if (isDonatorIcon()) {
            if (isMasterDonator()) {
                return Icons.MASTER_DONATOR;
            }
            if (isUltimateDonator()) {
                return Icons.DIAMOND_DONATOR;
            }
            if (isSupremeDonator()) {
                return Icons.PLATINUM_DONATOR;
            }
            if (isLegendaryDonator()) {
                return Icons.GOLD_DONATOR;
            }
            if (isExtremeDonator()) {
                return Icons.SILVER_DONATOR;
            }
            if (isDonator()) {
                return Icons.BRONZE_DONATOR;
            }
        } else {
            if (isKingOfTheSkillGameMode()) {
                return Icons.KING_OF_THE_SKILL;
            }
            if (isDesigner()) {
                return Icons.DESIGNER;
            }
            if (isGroupIronman()) {
                return Icons.GROUP_IRONMAN;
            }
            if (isHCIronMan()) {
                return Icons.HARDCORE_IRONMAN;
            }
            if (isIronMan()) {
                return Icons.LEGENDARY_IRONMAN;
            }
            if (isExpertIronMan()) {
                return Icons.EXPERT_IRONMAN;
            }
            if (isIntermediateIronMan()) {
                return Icons.INTERMEDIATE_IRONMAN;
            }
            if (isNoviceIronMan()) {
                return Icons.NOVICE_IRONMAN;
            }
        }
        return getRights();
    }
    
    public int getMessageIconForDiscordOnly() {
        if (getUsername().equalsIgnoreCase("xhybrid")) {
            return Icons.ORANGE_CROWN;
        }
       // if (getUsername().equalsIgnoreCase("kirita")) {
        //    return Icons.CYAN_CROWN;
       // }
        if (isFakeDev()) {
            return Icons.DEVELOPER;
        }
       // if (getRights() == 2) {
       //     return Icons.ADMINISTRATOR + 1;
       // }
        if (getRights() == 1) {
            return Icons.MODERATOR + 1;
        }
        if (isSupport()) {
            return Icons.SUPPORT;
        }
        if (isPlayerOfTheMonth() && isDisplayPlayerOfTheMonthIcon()) {
            return Icons.PLAYER_OF_THE_MONTH;
        }
        if (isDonatorIcon()) {
            if (isMasterDonator()) {
                return Icons.MASTER_DONATOR;
            }
            if (isUltimateDonator()) {
                return Icons.DIAMOND_DONATOR;
            }
            if (isSupremeDonator()) {
                return Icons.PLATINUM_DONATOR;
            }
            if (isLegendaryDonator()) {
                return Icons.GOLD_DONATOR;
            }
            if (isExtremeDonator()) {
                return Icons.SILVER_DONATOR;
            }
            if (isDonator()) {
                return Icons.BRONZE_DONATOR;
            }
        } else {
            if (isKingOfTheSkillGameMode()) {
                return Icons.KING_OF_THE_SKILL;
            }
            if (isDesigner()) {
                return Icons.DESIGNER;
            }
            if (isGroupIronman()) {
                return Icons.GROUP_IRONMAN;
            }
            if (isHCIronMan()) {
                return Icons.HARDCORE_IRONMAN;
            }
            if (isIronMan()) {
                return Icons.LEGENDARY_IRONMAN;
            }
            if (isExpertIronMan()) {
                return Icons.EXPERT_IRONMAN;
            }
            if (isIntermediateIronMan()) {
                return Icons.INTERMEDIATE_IRONMAN;
            }
            if (isNoviceIronMan()) {
                return Icons.NOVICE_IRONMAN;
            }
        }
        return getRights();
    }

    /**
     * Checks if @this is a staff member.
     *
     * @return if Staff.
     */
    public boolean isStaff() {
        return isSupport() || isOwner() || isDev() || isFakeDev() || getRights() == 2 || getRights() == 1 || isSupport1();
    }

    public MoneyPouch getMoneyPouch() {
        return pouch;
    }

    public int getMoneyPouchValue() {
        return money;
    }

    public void setMoneyPouchValue(final int money) {
        this.money = money;
    }

    public int getMovementType() {
        return (getNextRunDirection() != -1 || (getRun() && getWalkSteps().size() > 1) ? RUN_MOVE_TYPE
                : WALK_MOVE_TYPE) + 1;
    }

    @Override
    public void addHitBars() {
        super.addHitBars();
    }

    public MusicsManager getMusicsManager() {
        return musicsManager;
    }

    public long getMuted() {
        return muted;
    }

    public void setMuted(final long muted) {
        this.muted = muted;
    }

    public Notes getNotes() {
        return notesL;
    }

    public int getOverloadDelay() {
        return overloadDelay;
    }

    public int getSupremeOverloadDelay() {
        return supremeOverloadDelay;
    }

    public void setOverloadDelay(final int overloadDelay) {
        this.overloadDelay = overloadDelay;
    }

    public void setSupremeOverloadDelay(final int supremeOverlayDelay) {
        supremeOverloadDelay = supremeOverlayDelay;
    }

    public int getAggressiveDelay() {
        return aggressiveDelay;
    }

    public void setAggressiveDelay(final int delay) {
        aggressiveDelay = delay;
    }

    public List<String> getOwnedObjectManagerKeys() {
        if (ownedObjectsManagerKeys == null) {
            ownedObjectsManagerKeys = new LinkedList<String>();
        }
        return ownedObjectsManagerKeys;
    }

    public PacketDispatcher getPackets() {
        // Native players receive the 947 facade assigned by the native
        // constructor; it never emits legacy 910 bytes (see Native950PacketDispatcher).
        return dispatcher;
    }

    public PacketRepository getRepository() {
        return repository;
    }

    public long getPacketsDecoderPing() {
        return packetsDecoderPing;
    }

    public void setPacketsDecoderPing(final long packetsDecoderPing) {
        this.packetsDecoderPing = packetsDecoderPing;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public int getPestControlGames() {
        return pestControlGames;
    }

    public void setPestControlGames(final int pestControlGames) {
        this.pestControlGames = pestControlGames;
    }

    public int getPestPoints() {
        return pestPoints;
    }

    public void setPestPoints(final int pestPoints) {
        this.pestPoints = pestPoints;
    }

    /**
     * Gets the pet.
     *
     * @return The pet.
     */
    public Pet getPet() {
        return pet;
    }

    /**
     * Sets the pet.
     *
     * @param pet The pet to set.
     */
    public void setPet(final Pet pet) {
        this.pet = pet;
    }

    /**
     * Gets the petManager.
     *
     * @return The petManager.
     */
    public PetManager getPetManager() {
        return petManager;
    }

    /**
     * Sets the petManager.
     *
     * @param petManager The petManager to set.
     */
    public void setPetManager(final PetManager petManager) {
        this.petManager = petManager;
    }

    public int getPkPoints() {
        return pkPoints;
    }

    public void setPkPoints(final int pkPoints) {
        this.pkPoints = pkPoints;
    }

    public long getPoisonImmune() {
        return poisonImmune;
    }

    public long getPolDelay() {
        return polDelay;
    }

    public void setPolDelay(final long delay) {
        polDelay = delay;
    }

    public long getBloodDelay() {
        return bloodDelay;
    }

    public void setBloodDelay(final long delay) {
        bloodDelay = delay;
    }

    public long getPotDelay() {
        return potDelay;
    }

    public int[] getPouches() {
        return pouches;
    }

    public Prayer getPrayer() {
        return prayer;
    }

    public long getPrayerDelay() {
        final Long teleblock = (Long) getTemporaryAttributtes().get("PrayerBlocked");
        if (teleblock == null) {
            return 0;
        }
        return teleblock;
    }


    public void setPrayerDelay(final long teleDelay) {
        getTemporaryAttributtes().put("PrayerBlocked", teleDelay + Utils.currentTimeMillis());
        prayer.closeAllPrayers();
    }

    public PriceCheckManager getPriceCheckManager() {
        return priceCheckManager;
    }

    public int getPublicStatus() {
        return publicStatus;
    }

    public void setPublicStatus(final int publicStatus) {
        this.publicStatus = publicStatus;
        getPackets().sendOtherGameBarStages();
    }

    public QuestManager getQuestManager() {
        return questManager;
    }

    @Override
    public double getRangePrayerMultiplier() {
        return 0.6;
    }

    public String getRegisteredMac() {
        return registeredMac;
    }

    public void setRegisteredMac(final String registeredMac) {
        this.registeredMac = registeredMac;
    }

    public int getRights() {
        return rights;
    }

    public void setRights(final int rights) {
        this.rights = rights;
    }

    public byte getRunEnergy() {
        return runEnergy;
    }

    public void setRunEnergy(final int runEnergy) {
        if (infiniteRunEnergy && runEnergy < this.runEnergy) return;
        if (runEnergy + this.runEnergy >= 0) {
            this.runEnergy = (byte) runEnergy;
        }
        getPackets().sendRunEnergy();
    }

    @Getter
    @Setter
    public LinkedHashMap<KeyBindActions, KeyCombination> keyBindActionsMap;

    /**
     * @return the runeSpanPoint
     */
    public int getRuneSpanPoints() {
        return runeSpanPoints;
    }

    @Override
    public int getSize() {
        return getAppearence().getSize();
    }

    public Skills getSkills() {
        return usingFakeSkills ? fakeSkills : skills;
    }

    public StoreManager getStore() {
        return store;
    }

    public int getSkullId() {
        return skullId;
    }

    public void setSkullId(final int skullId) {
        this.skullId = skullId;
    }

    public int getSlayerPoints() {
        return slayerPoints;
    }

    public void setSlayerPoints(final int slayerPoints) {
        this.slayerPoints = slayerPoints;
        updateSlayerCounterInformation();
    }

    public int getSpecRestoreTimer() {
        return specRestoreTimer;
    }

    public void setSpecRestoreTimer(final int specRestoreTimer) {
        this.specRestoreTimer = specRestoreTimer;
    }

    public int getSpins() {
        return spins;
    }

    public void setSpins(final int spins) {
        this.spins = spins;
    }

    public List<Integer> getSwitchItemCache() {
        return switchItemCache;
    }

    public void setSwitchItemCache(final List<Integer> switchItemCache) {
        this.switchItemCache = switchItemCache;
    }

    /* Expert Skillcape Shards Bag data */

    // Returns expertShards map
    public Map<Integer, Boolean> getShardsList() {
        return expertShards;
    }

    public void setShardsList(final Map<Integer, Boolean> list) {
        expertShards = list;
    }

    // Returns whether or not the shard is stored
    public boolean shardStored(final int id) {
        return expertShards.get(id);
    }

    // Used to set whether or not a shard is stored
    public void setShard(final int id, final boolean store) {
        expertShards.put(id, store);
    }

    /* End of expertShards data */

    public List<Integer> getBannedTasks() {
        return bannedTasks;
    }

    /**
     * Used to instantiate tasklist in the case that it is null
     *
     * @param tasklist
     */
    public void setBannedTasks(final List<Integer> tasklist) {
        bannedTasks = tasklist;
    }

    /**
     * Adds or removes an entry from the bannedTasks list
     *
     * @param banned - use to ban / unban
     * @param taskId - can be used as the taskId or the index
     */
    public void banTask(final boolean banned, final int taskId) {
        if (banned) {
            bannedTasks.add(taskId);
        } else {
            bannedTasks.remove(taskId);
        }
    }

    /**
     * Checks whether or not the taskId is banned
     *
     * @param taskId
     * @return whether or not the taskId is in our list
     */
    public boolean checkBannedTask(final int taskId) {
        final Player partner = World.getPlayerByDisplayName(getSlayerPartner());
        final List<Integer> badTasks = new ArrayList<Integer>();
        badTasks.addAll(getBannedTasks());
        if (partner != null) {
            badTasks.addAll(partner.getBannedTasks());
        }
        for (final Integer task : badTasks) {
            if (task != null && task == taskId) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the task
     */
    public SlayerTask getTask() {
        return task;
    }

    /**
     * @param task the task to set
     */
    public void setTask(final SlayerTask task) {
        this.task = task;
        updateSlayerCounterInformation();
    }

    /**
     * Tracking for how many votes / trivia questions done total
     */
    @Getter
    @Setter
    private int totalVotes;
    @Getter
    @Setter
    private int totalTrivia;

    public long getTeleBlockDelay() {
        final Long teleblock = (Long) getTemporaryAttributtes().get("TeleBlocked");
        if (teleblock == null) {
            return 0;
        }
        return teleblock;
    }

    public void setTeleBlockDelay(final long teleDelay) {
        getTemporaryAttributtes().put("TeleBlocked", teleDelay + Utils.currentTimeMillis());
    }

    public int getTradeStatus() {
        return tradeStatus;
    }

    public void setTradeStatus(final int tradeStatus) {
        this.tradeStatus = tradeStatus;
        getPackets().sendGameBarStages();
    }

    public String getUsername() {
        return username;
    }

    /*
     * do not use this, only used by pm
     */
    public void setUsername(final String username) {
        this.username = username;
    }

    public int getVecnaTimer() {
        return vecnaTimer;
    }

    public void setVecnaTimer(final int vecnaTimer) {
        this.vecnaTimer = vecnaTimer;
    }

    public int getVotePoints() {
        return votePoints;
    }

    public void setVotePoints(final int votePoints) {
        this.votePoints = votePoints;
    }

    public String getYellColor() {
        return yellColor;
    }

    public void setYellColor(final String yellColor) {
        this.yellColor = yellColor;
    }

    public long getYellDelay() {
        return yellDelay;
    }

    public void setYellDelay(final long duration) {
        yellDelay = (System.currentTimeMillis() + duration);
    }

    @Override
    public Hit handleOutgoingHit(Hit hit, Entity target) {
        if (!accountPin.hasEnteredPin()) {
            hit.setDamage(0);
            return hit;
        }
        if (target instanceof NPC &&
                lastDeathSoul != null &&
                LocalDateTime.now().isBefore(lastDeathSoul) &&
                ContractHandler.isContractNpc(this, (NPC) target)) {
            hit.setDamage((int) (hit.getDamage() * 1.10));
        }
        if (controlerManager.getControler() instanceof DungeonArchitectController &&
                jujuPotions.isActive(Pots.Effects.PERFECT_DUNGEONEERING_JUJU)) {
            hit.setDamage((int) (hit.getDamage() * 1.15));
        }
        @SuppressWarnings("deprecation")
        Hit superhit = super.handleOutgoingHit(hit, target);
        return superhit;
    }

    @Override
    public void handleIngoingHit(final Hit hit) {
        if (!accountPin.hasEnteredPin()) {
            hit.setDamage(0);
            return;
        }
        if (killNext) {
            killNext = false;
            hit.setDamage(Integer.MAX_VALUE);
            return;
        }
        // if(getAttackedBy() == null)
        // return;
        // sendMessage("[Incoming hit] D-time:
        // "+(getAttackedByDelay()-Utils.currentTimeMillis())+" A-time:
        // "+(getAttackingDelay()-Utils.currentTimeMillis())+" from: "+((NPC)
        // getAttackedBy()).hashCode()+" "+isUnderCombat(), true);
        /** Blood necklaces */
        if (getControlerManager().getControler() instanceof Barrows) {
//            var barrows = (Barrows) getControlerManager().getControler();
            if (!interfaceManager.containsInterface(25)) {
                closeInterfaces();
            }
        }
        final Entity source = hit.getSource();
        if (getControlerManager().getControler() instanceof DungeonController) {
            if (ItemDefinitions.getItemDefinitions(getEquipment().getShieldId()).getName().contains("shield")) {
                if (getRingOfKinship().getBoost(RingOfKinship.TANK) > 0) {
                    hit.setDamage((int) (hit.getDamage() - (hit.getDamage() * (getRingOfKinship().getBoost(RingOfKinship.TANK) / 100.0f))));
                }
            }
        }
        if (source instanceof PraesulMinion) {
            hit.setDamage((int) (hit.getDamage() * 0.8));
        }
        if (CombatUtils.fullAkrisaesEquipped(this)) {
            if (Utils.random(4) == 0) {
                if (hit.getSource() != null) {
                    hit.getSource().applyHit(new Hit(this, Utils.random(50, 80), HitLook.REFLECTED_DAMAGE));
                }
            }
        }
        Perk brief = getInventionManager().hasPerk(Perks.BRIEF_RESPITE);
        boolean hasBriefEffect = brief != null && equipment.hasShield() && equipment.getItem(Equipment.SLOT_SHIELD).getDefinitions().getCombatMap() == null && (Math.random() <= (0.03 * (double)brief.getRank()));
        if (hasBriefEffect && source instanceof NPC &&(hit.getLook() == HitLook.MELEE_DAMAGE || hit.getLook() == HitLook.RANGE_DAMAGE || hit.getLook() == HitLook.MAGIC_DAMAGE) && hit.getDamage() > 0) {
            hit.setDamage(0);
            getPackets().sendGameMessage("You take no damage due to brief respite effect.", true);
        }
        if (hasSpearWallEffect() && hit.getSource() != null && hit.getDamage() > 0 && (hit.getLook() == HitLook.MELEE_DAMAGE || hit.getLook() == HitLook.RANGE_DAMAGE || hit.getLook() == HitLook.MAGIC_DAMAGE)) {
            hit.getSource().applyHit(new Hit(this, (int) Math.floor((double) hit.getDamage() * 0.5), HitLook.REFLECTED_DAMAGE));
        }
        if (mirrorback != null && !mirrorback.isDead() && !mirrorback.hasFinished() && !hit.isSpecialHit()) {
            hit.setDamage(hit.getDamage() / 2);
            mirrorback.applyHit(new Hit(hit.getSource(), hit.getDamage(), hit.getLook()));
            if (hit != null && hit.getSource() != null && hit.getSource() != this) {
                hit.getSource().applyHit(new Hit(this, hit.getDamage(), HitLook.REFLECTED_DAMAGE, true));
            }
        }
        String amuletName = getEquipment().getItem(Equipment.SLOT_AMULET) == null ? "" : getEquipment().getItem(Equipment.SLOT_AMULET).getName().toLowerCase();
        if ((amuletName.contains("blood necklace") || amuletName.contains("blood amulet")) && !amuletName.contains("(empty)") && bloodDelay == 0 && !inDungeoneering && hit.getLook() != HitLook.POISON_DAMAGE && hit.getLook() != HitLook.REFLECTED_DAMAGE) {
            int healCap = 0;
            if (source == null || source instanceof Player) {
                return;
            }
            final PlayerCombat pc = new PlayerCombat(source);
            final Entity[] targets = pc.getMultiAttackTargets(this, 2, 8);

            if (targets.length <= 1) {
                final int bloodRoll = Utils.random(50, 125);
                source.applyHit(new Hit(this, bloodRoll, HitLook.REFLECTED_DAMAGE));
                if (healCap < 350) {
                    healCap += bloodNeckHeal(healCap, bloodRoll);
                }
            } else {
                for (final Entity target : targets) {
                    if (target instanceof Player) {
                        return;
                    }
                    if (healCap < 350) {
                        final int bloodRoll = Utils.random(50, 125);
                        target.applyHit(new Hit(this, bloodRoll, HitLook.REFLECTED_DAMAGE));
                        healCap = bloodNeckHeal(healCap, bloodRoll);
                    }
                }
            }
            sendMessage(Colors.GREEN + "Your blood necklace heals you for " + healCap + " hitpoints!", true);
            setBloodDelay(28);
        }
        if (hit.getLook() != HitLook.MELEE_DAMAGE && hit.getLook() != HitLook.RANGE_DAMAGE && hit.getLook() != HitLook.MAGIC_DAMAGE) {
            return;
        }
        if (isInvulnerable()) {
            hit.setDamage(0);
            return;
        }
        if (source == null) {
            return;
        }
        Perk lucky = getInventionManager().hasPerk(Perks.LUCKY);
        if (hit.getDamage() > 1 && lucky != null && Math.random() <= (0.005 * (double) lucky.getRank())) {
            getPackets().sendGameMessage("<col=00FF00>Your Lucky perk reduces the damage of this attack by " + (hit.getDamage() - 1) + ".", hit.getDamage() < 500);
            hit.setDamage(1);
        }
        Perk absorbative = getInventionManager().hasPerk(Perks.ABSORBATIVE);
        if (source instanceof NPC && absorbative != null && Math.random() <= (0.2 + (absorbative.hasIncreasedChance() ? 0.02 : 0))) {
            int reduction = (int) Math.ceil((double) hit.getDamage() * 0.05 * (double) absorbative.getRank());
            hit.setDamage(hit.getDamage() - reduction <= 0 ? 0 : hit.getDamage() - reduction);
        }

        if (getEquipment().getGlovesId() == 31878 || getEquipment().getRingId() == 48481 || getEquipment().getGlovesId() == 31880) {
            if (Utils.random(5) == 0) {
                source.applyHit(new Hit(this, (int) (Utils.random(0.25, 0.5) * hit.getDamage()), HitLook.REFLECTED_DAMAGE));
            }
        }
        if (hasDisruption() && hit.getDamage() > 0) {
            hit.setDamage(0);
            if (getTemporaryAttributtes().get("disruption") == null) {
                if (source instanceof Player) {
                    final Player p2 = (Player) source;
                    p2.sendMessage(Colors.BLUE + getDisplayName() + "'s disruption shield has blocked your hits.");
                }
                sendMessage(Colors.BLUE + "The disruption shield blocks the incoming hits.");
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        setDisruption(false);
                        getTemporaryAttributtes().remove("disruption");
                    }
                });
            }
            getTemporaryAttributtes().put("disruption", true);
        }
        if (polDelay > Utils.currentTimeMillis()) {
            hit.setDamage((int) (hit.getDamage() * 0.5));
        }
        boolean isVerac = source instanceof NPC && ((NPC) source).getId() == 2030;
        if (prayer.hasPrayersOn() && hit.getDamage() != 0) {
            if (hit.getLook() == HitLook.MAGIC_DAMAGE) {
                if (prayer.usingPrayer(0, 11)) {
                    if (source instanceof HusbandMichNPC) {
                        hit.setDamage((int) (hit.getDamage() * 0.5));
                    } else {
                        hit.setDamage((int) (hit.getDamage() * source.getMagePrayerMultiplier()));
                    }
                } else if (prayer.usingPrayer(1, 11)) {
                    int deflectedDamage = source instanceof Nex ? 0 : (int) (hit.getDamage() * 0.1);
                    hit.setDamage((int) (hit.getDamage() * source.getMagePrayerMultiplier()));
                    if (deflectedDamage > 0) {
                        Perk preperation = getInventionManager().hasPerk(Perks.PREPARATION);
                        if (preperation != null)
                            deflectedDamage += (int) Math.ceil(deflectedDamage * 0.03 * preperation.getRank());
                        source.applyHit(new Hit(this, deflectedDamage, HitLook.REFLECTED_DAMAGE));
                        if (!isCantDoDefenceEmote()) {
                            setNextGraphics(new Graphics(2228));
                            setNextAnimation(new Animation(12573));
                        }
                    }
                }
            } else if (hit.getLook() == HitLook.RANGE_DAMAGE) {
                if (prayer.usingPrayer(0, 12)) {
                    hit.setDamage((int) (hit.getDamage() * source.getRangePrayerMultiplier()));
                } else if (prayer.usingPrayer(1, 12)) {
                    final int deflectedDamage = source instanceof Nex ? 0 : (int) (hit.getDamage() * 0.1);
                    hit.setDamage((int) (hit.getDamage() * source.getRangePrayerMultiplier()));
                    if (deflectedDamage > 0) {
                        source.applyHit(new Hit(this, deflectedDamage, HitLook.REFLECTED_DAMAGE));
                        if (!isCantDoDefenceEmote()) {
                            setNextGraphics(new Graphics(2229));
                            setNextAnimation(new Animation(12573));
                        }
                    }
                }
            } else if (hit.getLook() == HitLook.MELEE_DAMAGE && !isVerac) {
                if (prayer.usingPrayer(0, 13)) {
                    hit.setDamage((int) (hit.getDamage() * source.getMeleePrayerMultiplier()));
                } else if (prayer.usingPrayer(1, 13)) {
                    final int deflectedDamage = source instanceof Nex ? 0 : (int) (hit.getDamage() * 0.1);
                    hit.setDamage((int) (hit.getDamage() * source.getMeleePrayerMultiplier()));
                    if (deflectedDamage > 0) {
                        source.applyHit(new Hit(this, deflectedDamage, HitLook.REFLECTED_DAMAGE));
                        if (!isCantDoDefenceEmote()) {
                            setNextGraphics(new Graphics(2230));
                            setNextAnimation(new Animation(12573));
                        }
                    }
                }
            }
        }
//        if (hit.getDamage() > 200) {
//            if (hit.getLook() == HitLook.MELEE_DAMAGE) {
//                final int reducedDamage = (hit.getDamage() - 200) * combatDefinitions.getBonuses()[CombatDefinitions.ABSORVE_MELEE_BONUS] / 100;
//                if (reducedDamage > 0) {
//                    hit.setDamage(hit.getDamage() - reducedDamage);
//                    hit.setSoaking(new Hit(source, reducedDamage, HitLook.ABSORB_DAMAGE));
//                }
//            } else if (hit.getLook() == HitLook.RANGE_DAMAGE) {
//                final int reducedDamage = (hit.getDamage() - 200) * combatDefinitions.getBonuses()[CombatDefinitions.ABSORVE_RANGE_BONUS] / 100;
//                if (reducedDamage > 0) {
//                    hit.setDamage(hit.getDamage() - reducedDamage);
//                    hit.setSoaking(new Hit(source, reducedDamage, HitLook.ABSORB_DAMAGE));
//                }
//            } else if (hit.getLook() == HitLook.MAGIC_DAMAGE) {
//                final int reducedDamage = (hit.getDamage() - 200) * combatDefinitions.getBonuses()[CombatDefinitions.ABSORVE_MAGE_BONUS] / 100;
//                if (reducedDamage > 0) {
//                    hit.setDamage(hit.getDamage() - reducedDamage);
//                    hit.setSoaking(new Hit(source, reducedDamage, HitLook.ABSORB_DAMAGE));
//                }
//            }
//        }
        final int shieldId = equipment.getShieldId();
        /** Defenders passive */
        if (CombatUtils.hasDefenderOrEquivalentEffect(this) && Math.random() <= 0.06666666666666666666666666666667 && hit.getDamage() > 0) {
            double damageReduction = ThreadLocalRandom.current().nextDouble(0.5, 1.01);
            int damageDeflected = damageReduction >= 1 ? hit.getDamage() : (int) ((double) hit.getDamage() * damageReduction);
            hit.setDamage(hit.getDamage() - damageDeflected);
            getPackets().sendGameMessage("<col=00ff00> Your " + ItemDefinitions.getItemDefinitions(getEquipment().getShieldId()).getName() + " has deflected " + damageDeflected + " damage and increased your accuracy by 20% for your next hit.");
            getTemporaryAttributtes().put(Key.DEFENDERS_PASSIVE_ACCURACY, Boolean.TRUE);
        }
        Item shield = getEquipment().getItem(Equipment.SLOT_SHIELD);
        int realItemId = shield == null ? shieldId : shield.getInventionData() != null ? shield.getInventionData().getOriginalItemId() : shield.getChargesData() != null ? shield.getChargesData().getOrignalId() : shieldId;
        if (realItemId == 13742 || realItemId == 23699 || realItemId == 24884) { // elsyian
            if (Utils.getRandom(100) <= 70) {
                hit.setDamage((int) (hit.getDamage() * 0.75));
            }
        }
        if (realItemId == 13740 || realItemId == 23698 || realItemId == 24884) { // divine
            final int drain = (int) (Math.ceil(hit.getDamage() * 0.3) / 2);
            if (prayer.getPrayerpoints() >= drain) {
                hit.setDamage((int) (hit.getDamage() * 0.70));
                prayer.drainPrayer(drain);
            }
        }
        if ((equipment.getGlovesId() == 31878 || equipment.getGlovesId() == 48481) && Utils.random(100) >= 80) {
            double damage = hit.getDamage() * Utils.random(0.25, 0.50);
            if (damage >= 150) {
                damage = Utils.random(145.0, 151.9);
            }
            source.applyHit(new Hit(this, (int) damage, HitLook.REGULAR_DAMAGE));
        }
        if (castedVeng && hit.getDamage() >= 4) {
            castedVeng = false;
            setNextForceTalk(new ForceTalk("Taste vengeance!"));
            double dmg = (double) hit.getDamage() * 0.75;
            if (getInventionManager().hasPerk(Perks.REFLEXES) != null)
                dmg *= 2;
            source.applyHit(new Hit(this, (int) dmg, HitLook.REGULAR_DAMAGE));
        }
        hit.setDamage(getAuraManager().handleInGoingHit(hit.getDamage()));
        getControlerManager().processIngoingHit(hit);
        if (source instanceof Player) {
            final Player p2 = (Player) source;
            p2.getControlerManager().processIncomingHit(hit, this);
            p2.getPrayer().handleOutgoingHit(hit, this);
        }
        if (auraManager.usingPenance()) {
            final int amount = (int) (hit.getDamage() * 0.2);
            if (amount > 0) {
                prayer.restorePrayer(amount);
            }
        }
        if (hit.getDamage() > 0 && getControlerManager().getControler() instanceof DungeonController) {
            ((DungeonController) getControlerManager().getControler()).addDamageReceived(hit.getDamage());
        }
    }

    public boolean hasDisabledYell() {
        return yellDisabled;
    }

    public boolean hasDisplayName() {
        return displayName != null;
    }

    public boolean hasInstantSpecial(final int weaponId) {
        final ItemDefinitions definitions = ItemDefinitions.getItemDefinitions(weaponId);
        if (definitions.getName().toLowerCase().contains("noxious") || definitions.getName().toLowerCase().contains("zaros godsword")) {
            return true;
        }
        if (definitions.getName().toLowerCase().contains("decimation"))
            return true;
        Item wep = weaponId == -1 ? null : getEquipment().getItem(Equipment.SLOT_WEAPON);
        int realItemId = wep.getInventionData() != null ? wep.getInventionData().getOriginalItemId() : wep.getChargesData() != null ? wep.getChargesData().getOrignalId() : weaponId;
        switch (realItemId) {
            case 4153:
            case 15486:
            case 22207:
            case 22209:
            case 22211:
            case 22213:
            case 1377:
            case 13472:
            case 35:// Excalibur
            case 8280:
            case 14632:
            case 33625:
            case 33627:
                return true;
            default:
                return false;
        }
    }

    public boolean hasLargeSceneView() {
        return largeSceneView;
    }

    public boolean hasSkull() {
        return skullDelay > 0;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public boolean hasTalkedtoCook() {
        return talkedtoCook;
    }

    @Override
    public void heal(final int ammount, final int extra) {
        if (killmeActivated)
            return;
        super.heal(ammount, extra);
        refreshHitPoints();
    }

    public void increaseCrucibleHighScore() {
        crucibleHighScore++;
    }

    /**
     * Increases kill count and all adjustments to kill streaks.
     *
     * @param killed The Enemy player killed.
     */
    public void increaseKillCount(final Player killed) {
        if (killed == null || this == null) {
            return;
        }
        if (getLastKilled() == null || getLastKilledIP() == null) {
            setLastKilled("");
            setLastKilledIP("");
        }
        if (killed.getIP().equals(getIP()) || (killed.getUsername().equalsIgnoreCase(getLastKilled())) || (killed.getIP().equals(getLastKilledIP()))) {
            return;
        }
        killed.deathCount++;
        killStreak += 1;

        if (killStreak > highestKillStreak) {
            highestKillStreak = killStreak;
        }

        sendMessage(Colors.RED + "<img=17>You are now on a " + killStreak + " kill streak!");

        if (killStreak % 5 == 0 && killStreak > 0) {

            World.sendWorldMessage(Colors.RED + "<img=18>" + getDisplayName() + " is on a <col=ff0000>" + killStreak + "</col>" + Colors.RED + " killstreak. " + "Their highest streak is <col=ff0000>" + highestKillStreak + "</col>.", false);
        }

        if (killStreak >= 5) {
            int streakPoints = killStreakPoints;
            final int totalstreakPoints = totalkillStreakPoints;
            setTotalKillStreakPoints(getTotalKillStreakPoints() + 1);
            streakPoints = 1;
            if (isLegendaryDonator()) {
                setTotalKillStreakPoints(getTotalKillStreakPoints() + 1);
                streakPoints = 2;
            }

            sendMessage(Colors.BLUE + "You have reached a milestone killstreak and have been rewared with " + streakPoints + " killstreak Point(s)");
            sendMessage(Colors.BLUE + "You now have " + totalstreakPoints + " killstreak points!");
        }

        PkRank.checkRank(killed);

        killCount++;

        sendMessage(Colors.RED + "You have killed " + killed.getDisplayName() + ", " + "you now have " + killCount + " kills.");

        PkRank.checkRank(this);

        addPoints();

    }

    public int getPkPointReward() {
        return pkPointReward;
    }

    public int setPkPointReward(final int PkPointReward) {
        return pkPointReward = PkPointReward;
    }

    public int getPointsHad() {
        return pointsHad;
    }

    public int setPointsHad(final int PointsHad) {
        return pointsHad = PointsHad;
    }

    public void addPoints() {
        if (getPkPointReward() <= 500000) {
            setPointsHad(getPkPoints());
            setPkPoints(getPkPoints() + 10 + Utils.random(10));
            final int pointsgiven = getPkPoints() - pointsHad;
            sendMessage("You have received " + pointsgiven + " Pk Points");
        }
        if (getPkPointReward() >= 500000 && getPkPointReward() <= 999999) {
            setPointsHad(getPkPoints());
            setPkPoints(getPkPoints() + 10 + Utils.random(25));
            final int pointsgiven = getPkPoints() - pointsHad;
            sendMessage("You have received " + pointsgiven + " Pk Points");
        }
        if (getPkPointReward() >= 1000000 && getPkPointReward() <= 4999999) {
            setPkPoints(getPkPoints() + 10 + Utils.random(50));
            setPointsHad(getPkPoints());
            final int pointsgiven = getPkPoints() - pointsHad;
            sendMessage("You have received " + pointsgiven + " Pk Points");
        }
        if (getPkPointReward() >= 5000000) {
            setPointsHad(getPkPoints());
            setPkPoints(getPkPoints() + 10 + Utils.random(90));
            final int pointsgiven = getPkPoints() - pointsHad;
            sendMessage("You have received " + pointsgiven + " Pk Points");
        }
    }

    public void addKill(final Player dead, final boolean safe) {
        setLastKilled(dead.getUsername());
        setLastKilledIP(dead.getIP());
        if (dead.getControlerManager().getControler() != null) {
            return;
        }
        final int risk = safe ? 0 : checkHighestKill(dead);
        setPkPointReward(risk);
    }

    public String getIP() {
        if (changeIP != null) {
            return changeIP;
        }
        final String remote = realChannel.remoteAddress().toString();
        return remote.substring(1, remote.indexOf(":"));
    }

    public boolean isApeAtoll() {
        return (getX() >= 2693 && getX() <= 2821 && getY() >= 2693 && getY() <= 2817);
    }

    public BountyHunter getBountyHunter() {
        return bountyHunter;
    }

    public String getLastKilled() {
        return lastKilled;
    }

    public void setLastKilled(final String player) {
        lastKilled = player;
    }

    public String getLastKilledIP() {
        return lastKilledIP;
    }

    public void setLastKilledIP(final String ip) {
        lastKilledIP = ip;
    }

    public boolean isCanPvp() {
        return canPvp;
    }

    public void setCanPvp(final boolean canPvp) {
        if (canPvp && getHitpoints() > getMaxHitpoints()) {
            setHitpoints(getMaxHitpoints());
            sm(Colors.SALMON + Colors.SHAD + "[Warning]: " + Colors.WHITE + "Your bonfire bonus has been reset because you are in a PvP area!");
            refreshHitPoints();
        }
        this.canPvp = canPvp;
        getAppearence().generateAppearenceData();
        getPackets().sendPlayerOption(canPvp ? "Attack" : "null", 1, true);
        getPackets().sendPlayerUnderNPCPriority(canPvp);

    }

    public boolean safePvp() {
        return safePvp;
    }

    public void setSafePvp(final boolean safePvp) {
        this.safePvp = safePvp;
    }

    public boolean isCantTrade() {
        return cantTrade;
    }

    public void setCantTrade(final boolean canTrade) {
        cantTrade = canTrade;
    }

    public boolean isCastVeng() {
        return castedVeng;
    }

    public void setCastVeng(final boolean castVeng) {
        castedVeng = castVeng;
    }

    public boolean isCompletedFightCaves() {
        return completedFightCaves;
    }

    public boolean isCompletedFightKiln() {
        return completedFightKiln;
    }

    public boolean isCompletedRfd() {
        return completedRfd;
    }

    public boolean isDonator() {
        return donator || donatorTill > Utils.currentTimeMillis();
    }

    public void setDonator(final boolean donator) {
        this.donator = donator;
    }

    public boolean isATypeOfDonator() {
        return getMoneySpent() >= 20;
    }

    public void setDonatorTimeTill(long time) {
        this.donatorTill = time;
    }

    public boolean isEquipDisabled() {
        return disableEquip;
    }

    public boolean isExtremeDonator() {
        return extremeDonator || extremeDonatorTill > Utils.currentTimeMillis();
    }

    public void setExtremeDonator(final boolean extremeDonator) {
        this.extremeDonator = extremeDonator;
    }

    public boolean isExtremePermDonator() {
        return extremeDonator;
    }

    public boolean isForceNextMapLoadRefresh() {
        return forceNextMapLoadRefresh;
    }

    public void setForceNextMapLoadRefresh(final boolean forceNextMapLoadRefresh) {
        this.forceNextMapLoadRefresh = forceNextMapLoadRefresh;
    }

    public boolean isHidingWorldMessages() {
        return hideWorldAnnouncements;
    }

    public boolean isInAnimationRoom() {
        return inAnimationRoom;
    }

    public void setInAnimationRoom(final boolean inAnimationRoom) {
        this.inAnimationRoom = inAnimationRoom;
    }

    public boolean isInDefenderRoom() {
        return isInDefenderRoom;
    }

    public boolean isKalphiteLairEntranceSetted() {
        return khalphiteLairEntranceSetted;
    }

    public boolean isKalphiteLairSetted() {
        return khalphiteLairSetted;
    }

    public boolean isKilledAgrithNaNa() {
        return agrithNaNa;
    }

    public void setKilledAgrithNaNa(final boolean agrithNaNa) {
        this.agrithNaNa = agrithNaNa;
    }

    public boolean isKilledBork() {
        return killedBork;
    }

    public void setKilledBork(final boolean killedBork) {
        this.killedBork = killedBork;
    }

    /**
     * RFD
     */

    public boolean isKilledCulinaromancer() {
        return culinaromancer;
    }

    public void setKilledCulinaromancer(final boolean culinaromancer) {
        this.culinaromancer = culinaromancer;
    }

    public boolean isKilledDessourt() {
        return dessourt;
    }

    public void setKilledDessourt(final boolean dessourt) {
        this.dessourt = dessourt;
    }

    public boolean isKilledFlambeed() {
        return flamBeed;
    }

    public boolean isKilledKaramel() {
        return karamel;
    }

    public void setKilledKaramel(final boolean karamel) {
        this.karamel = karamel;
    }

    /**
     * Gets the killedQueenBlackDragon.
     *
     * @return The killedQueenBlackDragon.
     */
    public boolean isKilledQueenBlackDragon() {
        return killedQueenBlackDragon;
    }

    /**
     * Sets the killedQueenBlackDragon.
     *
     * @param killedQueenBlackDragon The killedQueenBlackDragon to set.
     */
    public void setKilledQueenBlackDragon(final boolean killedQueenBlackDragon) {
        this.killedQueenBlackDragon = killedQueenBlackDragon;
    }

    public boolean isLocked() {
        return lockDelay >= Utils.currentTimeMillis();
    }

    /**
     * Checks if @this username should have access to all commands.
     *
     * @return true if has access.
     */

    public boolean isOwner() {
        for (String accounts : Settings.ELEVATED_ACCESS_ACCOUNTS) {
            if (getUsername().equalsIgnoreCase(accounts)) {
                return true;
            }
        }
        if (Settings.NO_AUTOMATIC_OWNER) {
            return false;
        }
        return Settings.DEBUG || isDev();
    }

    public boolean isDev() {
        if (Settings.NO_AUTOMATIC_OWNER) {
            return false;
        }
        return Settings.TEST_SERVER_MODE;
    }

    public boolean isFakeDev() {
        for (String accounts : Settings.FAKE_DEV_ACCOUNTS) {
            if (getUsername().equalsIgnoreCase(accounts)) {
                return true;
            }
        }
        if (Settings.NO_AUTOMATIC_OWNER) {
            return false;
        }
        return false;
    }

    public boolean isSupport1() {
        return isOwner() || isDev();
    }

    public boolean canBan() {
        return isOwner() || isDev() || isSupport1() || isSupport();
    }

    public boolean isPermBanned() {
        return permBanned;
    }

    public void setPermBanned(final boolean permBanned) {
        this.permBanned = permBanned;
    }

    public void toggleLastManStandingBan() {
        lastManStandingBanned = !lastManStandingBanned;
    }

    public boolean isPermMuted() {
        return permMuted;
    }

    public void setPermMuted(final boolean permMuted) {
        this.permMuted = permMuted;
    }

    public boolean isResting() {
        return resting;
    }

    public void setResting(final boolean resting) {
        this.resting = resting;
        sendRunButtonConfig();
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(final boolean running) {
        this.running = running;
    }

    public boolean isSpawnsMode() {
        return spawnsMode;
    }

    public void setSpawnsMode(final boolean spawnsMode) {
        this.spawnsMode = spawnsMode;
    }

    public boolean isTalkedWithMarv() {
        return talkedWithMarv;
    }

    public boolean isTalkedWithVannaka() {
        return talkedWithVannaka;
    }

    public void setTalkedWithVannaka(final boolean talkedWithVannaka) {
        this.talkedWithVannaka = talkedWithVannaka;
    }

    public boolean isUsingReportOption() {
        return reportOption;
    }

    public boolean isWonFightPits() {
        return wonFightPits;
    }

    public boolean isXpLocked() {
        return xpLocked;
    }

    public void setXpLocked(final boolean locked) {
        xpLocked = locked;
    }

    public boolean isYellOff() {
        return yellOff;
    }

    public void setYellOff(final boolean yellOff) {
        this.yellOff = yellOff;
    }

    public void kickPlayerFromFriendsChannel(final String name) {
        if (currentFriendChat == null) {
            return;
        }
        currentFriendChat.kickPlayerFromChat(this, name);
    }

    @Override
    public int getMapSize() {
        // Keep the existing 104-tile server interest/collision window. The native950
        // client independently uses a fixed256-tile wire scene (Native950Packets.SCENE_SIZE);
        // zone packet coordinates must use that native origin, not this server radius.
        return isNative950() ? 0 : super.getMapSize();
    }

    @Override
    public void loadMapRegions() {
        if (isNative950()) {
            super.loadMapRegions();
            native947MapRefresh = true;
            return;
        }
        final boolean wasAtDynamicRegion = isAtDynamicRegion();
        super.loadMapRegions();
        lastRegionKey = getRegionKey();
        setClientHasntLoadedMapRegion();
        if (isAtDynamicRegion()) {
            getPackets().sendDynamicMapRegion(!isActive());
            if (!wasAtDynamicRegion) {
                localNPCUpdate.reset();
            }
        } else {
            getPackets().sendMapRegion();
            if (wasAtDynamicRegion) {
                localNPCUpdate.reset();
            }
        }
        forceNextMapLoadRefresh = false;
    }

    public transient boolean killmeActivated;

    public void killme() {
        lock();
        killmeActivated = true;
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                killmeActivated = false;
                applyHit(new Hit(Player.this, getHitpoints(), HitLook.REGULAR_DAMAGE));
                getTemporaryAttributtes().put("safedeath", Boolean.TRUE);
                unlock();
            }
        }, 1);
    }

    public void lock() {
        lockDelay = Long.MAX_VALUE;
    }

    public void lock(final long ticks) {
        lockDelay = Utils.currentTimeMillis() + (ticks * 600);
    }






    private boolean gePriceEditMode;

    public boolean isGePriceEditMode() {
        return gePriceEditMode;
    }

    public void setGePriceEditMode(boolean mode) {
        this.gePriceEditMode = mode;
    }

// ge price editor




    /**
     * Logs the player out.
     *
     * @param force If we're logging out to the lobby.
     */
    public void logout(final boolean force) {
        if (!force) {
            final long currentTime = Utils.currentTimeMillis();
            if (!isRunning()) {
                return;
            }
            if ((lockDelay >= currentTime && !isAFK() && pendingGimKey == null) || lunarDelay >= currentTime) {
                getPackets().sendGameMessage("You can't log out while performing an action.");
                return;
            }
            if (isUnderCombat(10)) {
                sendMessage("You can't log out until 10 seconds after the end of combat.");
                return;
            }
            if (getEmotesManager().isDoingEmote()) {
                sendMessage("You can't log out while performing an emote.");
                return;
            }
            if (isLocked() && !isAFK() && pendingGimKey == null) {
                sendMessage("You can't log out while performing an action.");
                return;
            }
            if (GIM.getLoginBlocked().contains(getUsername())) {
                GIM.sendPlayerMsg(this, "Your group is being renamed. Please wait until it finishes before logging out.");
                return;
            }
        }
        getPackets().sendLogout();
        setRunning(false);
    }

    public boolean hasVotedInLast24H() {
        if (lastVote == null) {
            return false;
        }
        LocalDateTime dayBefore = LocalDateTime.now().minusDays(1);
        return lastVote.isAfter(dayBefore);
    }

    public long getLunarDelay() {
        return lunarDelay;
    }

    public void setLunarDelay(final long time) {
        lunarDelay = time + Utils.currentTimeMillis();
    }

    @SuppressWarnings("deprecation")
    public void makeDonator(final int months) {
        if (donatorTill < Utils.currentTimeMillis()) {
            donatorTill = Utils.currentTimeMillis();
        }
        final Date date = new Date(donatorTill);
        date.setMonth(date.getMonth() + months);
        donatorTill = date.getTime();
    }

    @Override
    public boolean needMasksUpdate() {
        return super.needMasksUpdate() || refreshClanIcon;
    }

    public void performInstantSpecial(final int weaponId) {
        Item wep = weaponId == -1 ? null : getEquipment().getItem(Equipment.SLOT_WEAPON);
        int realItemId = wep.getInventionData() != null ? wep.getInventionData().getOriginalItemId() : wep.getChargesData() != null ? wep.getChargesData().getOrignalId() : weaponId;
        int specAmt = PlayerCombat.getSpecialCost(realItemId);
        final ItemDefinitions definitions = ItemDefinitions.getItemDefinitions(realItemId);
        if (combatDefinitions.hasRingOfVigour()) {
            specAmt *= 0.9;
        }
        if (combatDefinitions.getSpecialAttackPercentage() < specAmt && !definitions.getName().toLowerCase().contains("zaros godsword")) {
            sendMessage("You don't have enough power left.");
            combatDefinitions.decreaseSpecialAttack(0);
            return;
        }
        if (getSwitchItemCache().size() > 0) {
            ButtonHandler.submitSpecialRequest(this);
            return;
        }
        if (!isUnderCombat()) {
            PlayerCombat.addAttackingDelay(this);
        }
        if (definitions.getName().toLowerCase().contains("decimation")) {
            setNextAnimation(new Animation(18081));
            setNextGraphics(new Graphics(3623));
            setNextGraphics(new Graphics(3624));
            startDecimationEffect();
            combatDefinitions.decreaseSpecialAttack(specAmt);
            return;
        }
        if (definitions.getName().toLowerCase().contains("noxious") || realItemId == 33625 || realItemId == 33627) {
            if (mirrorback != null) {
                sendMessage("You cannot summon a mirrorback spider just yet.");
                return;
            }
            sendMessage("You summon a mirrorback spider to aid you.", true);
            setNextAnimation(new Animation(24015));
            setNextGraphics(new Graphics(5005));
            final Player t = this;
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    WorldTile freeTile = null;
                    for (int i = 0; i < 10; i++) {
                        freeTile = new WorldTile(t, 2);
                        if (World.isTileFree(freeTile, 1) && !freeTile.withinDistance(t, 1)) {
                            break;
                        }
                    }
                    final MirrorbackSpider spider = new MirrorbackSpider(19468, freeTile, -1, true, true, t);
                    mirrorback = spider;
                }
            });
            combatDefinitions.decreaseSpecialAttack(specAmt);
            return;
        }
        if (definitions.getName().toLowerCase().contains("zaros godsword") && ZarosGodswordSpecialAttack.canSummonBlackHole(this)) {
            zarosGodswordSpecialAttack = new ZarosGodswordSpecialAttack(this, getLastWorldTile());
            return;
        }
        switch (realItemId) {
            case 4153:
                combatDefinitions.setInstantAttack(true);
                combatDefinitions.switchUsingSpecialAttack();
                final Entity target = (Entity) getTemporaryAttributtes().get("last_target");
                if (target != null && target.getTemporaryAttributtes().get("last_attacker") == this) {
                    if (!(getActionManager().getAction() instanceof PlayerCombat) || ((PlayerCombat) getActionManager().getAction()).getTarget() != target) {
                        getActionManager().setAction(new PlayerCombat(target));
                    }
                }
                break;
            case 1377:
            case 13472:
                setNextAnimation(new Animation(1056));
                setNextGraphics(new Graphics(246));
                setNextForceTalk(new ForceTalk("Raarrrrrgggggghhhhhhh!"));
                final int defence = (int) (getSkills().getLevelForXp(Skills.DEFENCE) * 0.90D);
                final int attack = (int) (getSkills().getLevelForXp(Skills.ATTACK) * 0.90D);
                final int range = (int) (getSkills().getLevelForXp(Skills.RANGE) * 0.90D);
                final int magic = (int) (getSkills().getLevelForXp(Skills.MAGIC) * 0.90D);
                final int strength = (int) (getSkills().getLevelForXp(Skills.STRENGTH) * 1.2D);
                getSkills().set(Skills.DEFENCE, defence);
                getSkills().set(Skills.ATTACK, attack);
                getSkills().set(Skills.RANGE, range);
                getSkills().set(Skills.MAGIC, magic);
                getSkills().set(Skills.STRENGTH, strength);
                getTemporaryAttributtes().put(Key.D_BATTLEAXE_SPECIAL, Long.valueOf(Utils.currentTimeMillis() + 60000));
                combatDefinitions.decreaseSpecialAttack(specAmt);
                break;
            case 35:// Excalibur
            case 8280:
            case 14632:
                setNextAnimation(new Animation(1168));
                setNextGraphics(new Graphics(247));
                final boolean enhanced = realItemId == 14632;
                getSkills().set(Skills.DEFENCE, enhanced ? (int) (getSkills().getLevelForXp(Skills.DEFENCE) * 1.15D) : (getSkills().getLevel(Skills.DEFENCE) + 8));
                WorldTasksManager.schedule(new WorldTask() {
                    int count = 5;

                    @Override
                    public void run() {
                        if (isDead() || hasFinished() || getHitpoints() >= getMaxHitpoints()) {
                            stop();
                            return;
                        }
                        heal(enhanced ? 80 : 40);
                        if (count-- == 0) {
                            stop();
                            return;
                        }
                    }
                }, 4, 2);
                combatDefinitions.decreaseSpecialAttack(specAmt);
                break;
            case 15486:
            case 22207:
            case 22209:
            case 22211:
            case 22213:
                setNextAnimation(new Animation(12804));
                setNextGraphics(new Graphics(2319));// 2320
                setNextGraphics(new Graphics(2321));
                addPolDelay(60000);
                combatDefinitions.decreaseSpecialAttack(specAmt);
                break;
        }
    }

    @Override
    public void processEntity() {
        // P4/M2b acceptance (e): count the entry for native players only, before any
        // work, so the counter is a lower bound that cannot over-report. Legacy 910
        // players never touch this field.
        if (isNative950())
            nativeProcessEntityRuns++;
        try {
            processLogicPackets();
            if (routeEvent != null) {
                try {
                    if (routeEvent.processEvent(this)) {
                        routeEvent = null;
                    }
                } catch (final Exception e) {
                    Logger.getGlobal().catching(e);
                }
            }
            super.processEntity();
            cutscenesManager.process();
            Magic.checkRemoveSwapedSpellBook(this);
//            charges.process();
            if (pcFirewall != null)
                pcFirewall.checkFirewall(this);
            auraManager.process();
            try {
                actionManager.process();
            } catch (final Exception e) {
                Logger.getGlobal().catching(e);
                actionManager.forceStop();
            }
            inventionManager.process();
            prayer.processPrayer();
            dayOfWeekManager.process();
            buffDebuffTimersManager.process();
            try {
                controlerManager.process();
            } catch (final Exception e) {
                Logger.getGlobal().catching(e);
            }
            farmingManager.process();
            if (isDead()) {
                return;
            }
            playerExamineManager.process();
            if (lastReaperNecklaceEffect != 0 && lastReaperNecklaceEffect < Utils.currentTimeMillis() || !PlayerCombat.hasReaperNecklace(this)) {
                reaperAccuracyBoost = 0;
                lastReaperNecklaceEffect = 0;
            }
            if (!accountPin.hasEnteredPin() && !interfaceManager.containsChatBoxInter() && getTemporaryAttributtes().get("pluginInteger") == null) {
                accountPin.forceLock();
            }
            if (getJailed() > Utils.currentTimeMillis()) {
                if (!(getControlerManager().getControler() instanceof JailController) || getRegionId() != JailController.REGION_ID) {
                    WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                            if (getCurrentInstance() != null) {
                                getCurrentInstance().destroyInstance();
                            }
                            getControlerManager().startControler("JailController");
                        }
                    });
                }
            }
            if (!evilTreeInter && EvilTree.isNearGlobal(this)) {
                getControlerManager().startControler("GlobalEvilTreeController");
            } else if (evilTreeInter && !EvilTree.isNearInstanceOrGlobal(this)) {
                getInterfaceManager().closeOverlay(false);
            }
            if (musicsManager.musicEnded()) {
                musicsManager.replayMusic();
            }
            if (hasSkull()) {
                skullDelay--;
                if (!hasSkull()) {
                    getAppearence().generateAppearenceData();
                }
            }
            if (kingOfTheSkillGameMode && hasExceededTwentyFourHourLimitDuringPlayTime()) {
                kingOfTheSkillGameModeHandler.terminatePlayer();
            }
            if (polDelay != 0 && polDelay <= Utils.currentTimeMillis()) {
                sendMessage("The power of the light fades. Your resistance to melee attacks return to normal.");
                polDelay = 0;
            }
            if (getEquipment().getBootsId() >= 30920 && getEquipment().getBootsId() <= 30924) {
                if (getRun() && getNextWalkDirection() != -1) {
                    final Item boots = getEquipment().getItem(Equipment.SLOT_FEET);
                    ticksSpentRunning++;
                    if (ticksSpentRunning == 100 && boots.getCharges() > 0) {
                        boots.setCharges(boots.getCharges() - 1);
                        setNextGraphics(new Graphics(4605));
                        sendMessage("<col=b3b3b3>Your Silverhawk boots have granted you some experience.", true);
                        getSkills().addXp(Skills.AGILITY, XPLamps.getExp(getSkills().getLevelForXp(Skills.AGILITY), 0) * 0.10, false, true);
                        ticksSpentRunning = 0;
                    }
                }
            }
            if (getEquipment().getBootsId() >= 30920 && getEquipment().getBootsId() <= 30924 && isUnderCombat()) {
                if (!shCombatCooldown.isRunning()) {
                    shCombatCooldown.start();
                }
                final Item boots = getEquipment().getItem(Equipment.SLOT_FEET);
                if (shCombatDelay == 0) {
                    shCombatDelay = Utils.random(30, 60);
                }
                if (boots.getCharges() > 0 && shCombatCooldown.elapsed(TimeUnit.SECONDS) > shCombatDelay) {
                    boots.setCharges(boots.getCharges() - 1);
                    sendMessage("<col=b3b3b3>Your Silverhawk boots have granted you some experience.", true);
                    setNextGraphics(new Graphics(4605));
                    getSkills().addXp(Skills.AGILITY, XPLamps.getExp(getSkills().getLevelForXp(Skills.AGILITY), 0) * 0.10, false, true);
                    shCombatCooldown.reset().start();
                    shCombatDelay = 0;
                }
            }

            if (lootshareDelay >= 200) {
                if (getCurrentFriendChat() != null) {
                    getPackets().sendGameMessage(Colors.GREEN + "LootShare has been enabled.");
                    toogleLootShare = true;
                    refreshToogleLootShare();
                }
                lootshareDelay = -1;
            }
            if (lootshareDelay > -1) {
                lootshareDelay++;
            }
            if (bloodDelay > 0) {
                bloodDelay--;
            }

            if (doubleXpTimer > 0) {
                doubleXpTimer--;
                if (doubleXpTimer == 50) {
                    sendMessage("<col=930000>Your double EXP is about to run out...");
                } else if (doubleXpTimer == 1) {
                    sendMessage("<col=930000>Your double EXP has ran out!");
                }
            }
            if (getActivePotions().size() > 0 && isShowPotionTimers())
                PotionTimerInterface.updateInterface(this);
            if (overloadDelay > 0) {
                if (overloadDelay == 1 || isDead()) {
                    Pots.resetOverLoadEffect(this);
                    return;
                } else if ((overloadDelay - 1) % 25 == 0) {
                    Pots.applyOverLoadEffect(this);
                }
                overloadDelay--;
                if (overloadDelay == 50) {
                    sendMessage("<col=930000>Your overload is about to run out...");
                }
            }
            if (naturesEssenceDelay > 0) {
                naturesEssenceDelay--;
                if (naturesEssenceDelay <= 0) {
                    sendMessage(Colors.DARK_GREEN + "You no longer harbour the grace of Nature's essence.");
                }
            }
            if (bloodSerumDelay > 0) {
                if (!EvilTree.isNearInstanceOrGlobal(this) && !isDead()) {
                    bloodSerumDelay--;
                    if (bloodSerumDelay <= 0) {
                        sendMessage(Colors.RED + "The strange effects have worn off.");
                    }
                } else {
                    if (isDead()) {
                        bloodSerumDelay = 0;
                    } else if (bloodSerumDelay % 10 == 0) {
                        int damage = (int) (skills.getLevelForXp(Skills.HITPOINTS) * 0.10);
                        if (damage < 1)
                            damage = 1;
                        applyHit(new Hit(damage, HitLook.HEALED_DAMAGE));
                        setNextGraphics(new Graphics(2345));
                    }
                    bloodSerumDelay--;
                    if (bloodSerumDelay <= 0) {
                        sendMessage(Colors.RED + "The strange effects have worn off.");
                    }
                }
            }
            if (supremeOverloadDelay > 0) {

                if (supremeOverloadDelay == 1 || isDead()) {
                    Pots.resetSupremeOverLoadEffect(this);
                    return;
                } else if ((supremeOverloadDelay - 1) % 25 == 0) {
                    Pots.applySupremeOverLoadEffect(this);
                }
                supremeOverloadDelay--;
                if (supremeOverloadDelay == 50) {
                    sendMessage("<col=930000>Your supreme overload is about to run out...");
                }
            }
            if (prayerRenewalDelay > 0) {
                if (prayerRenewalDelay == 1 || isDead()) {
                    sendMessage(Colors.RED + "Your prayer renewal effect has run out.");
                    prayerRenewalDelay = 0;
                    return;
                } else {
                    if (prayerRenewalDelay == 50) {
                        sendMessage(Colors.RED + "Your prayer renewal effect will run out in 30 seconds..");
                    }
                    if (!prayer.hasFullPrayerpoints()) {
                        getPrayer().restorePrayer(1);
                        if ((prayerRenewalDelay - 1) % 25 == 0) {
                            setNextGraphics(new Graphics(1295));
                        }
                    }
                }
                prayerRenewalDelay--;
            }
            if (aggressiveDelay > 0) {
                if (aggressiveDelay == 1 || isDead()) {
                    sendMessage(Colors.RED + "The effects of your Aggression Potion have expired.");
                    aggressiveDelay = 0;

                    for (final int regionId : getMapRegionsIds()) {
                        final List<Integer> npcsIndexes = World.getRegion(regionId).getNPCsIndexes();
                        if (npcsIndexes != null) {
                            for (final int npcIndex : npcsIndexes) {
                                final NPC npc = World.getNPCs().get(npcIndex);
                                if (npc == null || npc.isDead() || npc.hasFinished()) {
                                    continue;
                                }

                                if (getAggressiveOnYou().contains(npc) && npc.isForceMultiAttacked()) {
                                    // removes the aggression.
                                    npc.setForceMultiAttacked(false);
                                    // if the npc was not attacked recently or has no attacker etc.
                                    if (npc.getAttackedBy() == null) {
                                        npc.removeTarget();
                                        npc.resetWalkSteps();
                                    }
                                }
                            }
                        }
                    }
                    // resets.
                    getAggressiveOnYou().clear();

                    return;
                }

                if (aggressiveDelay == 50) {
                    sendMessage(Colors.RED + "The effects of your Aggression Potion will expire in 30 seconds.");
                }

                aggressiveDelay--;
            }

            /**
             * Prifddinas Thiev timers. If you can think of a better way to handle this - do
             * tell.
             */
            if (thievIthell > 0) {
                if (thievIthell == 1 || isDead()) {
                    sendMessage("Clan Ithell has forgotten about your pickpocketing.");
                    thievIthell = 0;
                    return;
                }
                thievIthell--;
            }
            if (thievIorwerth > 0) {
                if (thievIorwerth == 1 || isDead()) {
                    sendMessage("Clan Iorwerth has forgotten about your pickpocketing.");
                    thievIorwerth = 0;
                    return;
                }
                thievIorwerth--;
            }
            if (thievCadarn > 0) {
                if (thievCadarn == 1 || isDead()) {
                    sendMessage("Clan Cadarn has forgotten about your pickpocketing.");
                    thievCadarn = 0;
                    return;
                }
                thievCadarn--;
            }
            if (thievAmlodd > 0) {
                if (thievAmlodd == 1 || isDead()) {
                    sendMessage("Clan Amlodd has forgotten about your pickpocketing.");
                    thievAmlodd = 0;
                    return;
                }
                thievAmlodd--;
            }
            if (thievTrahaearn > 0) {
                if (thievTrahaearn == 1 || isDead()) {
                    sendMessage("Clan Trahaearn has forgotten about your pickpocketing.");
                    thievTrahaearn = 0;
                    return;
                }
                thievTrahaearn--;
            }
            if (thievHefin > 0) {
                if (thievHefin == 1 || isDead()) {
                    sendMessage("Clan Hefin has forgotten about your pickpocketing.");
                    thievHefin = 0;
                    return;
                }
                thievHefin--;
            }
            if (thievCrwys > 0) {
                if (thievCrwys == 1 || isDead()) {
                    sendMessage("Clan Crwys has forgotten about your pickpocketing.");
                    thievCrwys = 0;
                    return;
                }
                thievCrwys--;
            }
            if (thievMeilyr > 0) {
                if (thievMeilyr == 1 || isDead()) {
                    sendMessage("Clan Meilyr has forgotten about your pickpocketing.");
                    thievMeilyr = 0;
                    return;
                }
                thievMeilyr--;
            }
            if (specRestoreTimer > 0) {
                specRestoreTimer--;
            }
            if (lastBonfire > 0) {
                lastBonfire--;
                if (lastBonfire == 500) {
                    sendMessage("<col=ffff00>The health boost from stoking a bonfire will run out in 5 minutes..", true);
                } else if (lastBonfire == 0) {
                    sendMessage("<col=ff0000>The health boost you received from stoking a bonfire has ran out.");
                    equipment.refreshConfigs(false);
                }
            }
            if (timeToNextMysterybox > 0) {
                timeToNextMysterybox--;
                if (timeToNextMysterybox == 0) {
                    MysteryBox.give10HourReward(this);
                }
            }
            restoreRunEnergy();

            /*
             * if (getInventory().containsItem(10501, 1) || getEquipment().getWeaponId() ==
             * 10501) getPackets().sendPlayerOption("Pelt", 6, true); else
             * getPackets().sendPlayerOption("Null", 6, true);
             */

            if (getGroup() != null) {
                if (getGroup().getLeader() == this && getGroup().getTeam().size() < 4) {
                    getPackets().sendPlayerOption("Invite", 6, false);
                }
            }

            // LendingManager.processs();

            DivineObject.resetGatherLimit(this, true);
            if (!(controlerManager.getControler() instanceof AraxxorController)) {
                resetEnrage();
            }

            // P4 guard: the AFK island teleport / staff realFinish never applies to a
            // native 947 player (isAFK() is also false for them, see below).
            if (!isBot() && !isNative950() && isAFK() && !isLocked()) {
                if (getControlerManager().getControler() != null && getControlerManager().getControler() instanceof JailController) {
                    return;
                }
                if (isStaff()) {
                    realFinish();
                    return;
                }
                setTotalPlayTime(getTotalPlayTime() + (getRecordedPlayTime() - Utils.currentTimeMillis()));
                stopAll();
                lock();
                getPoison().reset();
                this.beforeAfkLocation = (getControlerManager().getControler() != null || isAtDynamicRegion()) ? Settings.RESPAWN_PLAYER_LOCATION : getLastWorldTile();
                setNextWorldTile(getAFKSpawn());
                controlerManager.forceStop();
                Logger.getGlobal().info("Player [" + getDisplayName() + " (" + getUsername() + ")] has been sent to bob for being AFK.");
//                realFinish();
                String name = hasDisplayName() ? (getDisplayName() + " (" + getUsername() + ")") : getUsername();
                World.sendWorldMessage(name + " was sent to afk island.", true);
            }

            getCombatDefinitions().processCombatStance();
            // P4 guard: the AfkD warning dialogues (1/5/10 minutes) need the dialogue
            // plumbing of M7; nothing idle-related runs for native players.
            if (!isBot() && !isNative950()) {
                sendAfkNotification();
            }

        } catch (final Exception e) {
            Logger.getGlobal().catching(e);
            if (isNative950()) {
                // P4: legacy swallows every tick exception; natively that would make
                // "100 ticks without exceptions" unobservable, so count it and, in
                // strict mode (tests/smokes), fail the tick loudly.
                nativeTickFailures++;
                if (com.rs.game.player.client.Native950PacketDispatcher.isStrict())
                    throw new IllegalStateException("Native 947 tick failure #" + nativeTickFailures
                            + " for " + username + ": " + e, e);
            }
        }
    }


    private WorldTile getAFKSpawn() {
        WorldTile centerTile = new WorldTile(3420, 4776, 0);
        for (int trycount = 0; trycount < 5; trycount++) {
            centerTile = new WorldTile(centerTile, 4);
            if (World.canMoveNPC(centerTile.getPlane(), centerTile.getX(), centerTile.getY(), 1)) {
                break;
            }
        }
        return centerTile;
    }

    private transient int lastNotification = -1;
    public Map<WorldTile, JadinkoCatch> hunterCatch = new HashMap<>(5);

    private void sendAfkNotification() {
        if (isAFK())
            return;
        if (getControlerManager().getControler() != null && (getControlerManager().getControler() instanceof JailController || getControlerManager().getControler() instanceof EliteDungeonController)) {
            return;
        }
        long now = Utils.currentTimeMillis();
        long afk10 = afkTimer - TimeUnit.MILLISECONDS.convert(10, TimeUnit.MINUTES);
        long afk5 = afkTimer - TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES);
        long afk1 = afkTimer - TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES);
        if (now >= afk1) {
            if (lastNotification != 1) {
                dialogueManager.startDialogue("AfkD", "1 minute");
                lastNotification = 1;
            }
            return;
        }
        if (now >= afk5) {
            if (lastNotification != 5) {
                dialogueManager.startDialogue("AfkD", "5 minutes");
                lastNotification = 5;
            }
            return;
        }
        if (now >= afk10) {
            if (lastNotification != 10) {
                dialogueManager.startDialogue("AfkD", "10 minutes");
                lastNotification = 10;
            }
        }
    }

    public void processProjectiles() {// updated it to work for current
        // projecttiles i will remove it when i
        // stop using it in vorago
        for (final int regionId : getMapRegionsIds()) {
            final Region region = World.getRegion(regionId);
            for (final Projectile projectile : region.getProjectiles()) {
                if (projectile.isNewProjectile()) {
                    int fromSizeX, fromSizeY;
                    if (projectile.getFrom() instanceof Entity)
                        fromSizeX = fromSizeY = ((Entity) projectile.getFrom()).getSize();
                    else if (projectile.getFrom() instanceof WorldObject) {
                        ObjectDefinitions defs = ((WorldObject) projectile.getFrom()).getDefinitions();
                        fromSizeX = defs.getSizeX();
                        fromSizeY = defs.getSizeY();
                    } else
                        fromSizeX = fromSizeY = 1;
                    int toSizeX, toSizeY;
                    if (projectile.getTo() instanceof Entity)
                        toSizeX = toSizeY = ((Entity) projectile.getTo()).getSize();
                    else if (projectile.getTo() instanceof WorldObject) {
                        ObjectDefinitions defs = ((WorldObject) projectile.getTo()).getDefinitions();
                        toSizeX = defs.getSizeX();
                        toSizeY = defs.getSizeY();
                    } else
                        toSizeX = toSizeY = 1;
                    getPackets().sendProjectileProperNew(projectile.getFrom(), fromSizeX, fromSizeY, projectile.getTo(), toSizeX, toSizeY, projectile.getTo() instanceof Entity ? (Entity) projectile.getTo() : null, projectile.getGraphicId(), projectile.getStartHeight(), projectile.getEndHeight(), projectile.getStartTime(), projectile.getEndTime(), projectile.getSlope(), projectile.getAngle());
                    continue;
                }
                if (projectile.getFrom() instanceof Telos) {
                    getPackets().sendTestProjectile(projectile);
                    continue;
                }
                final int distance = Utils.getDistance(projectile.getFrom().getX(), projectile.getFrom().getY(), projectile.getTo().getX(), projectile.getTo().getY());
                double speedModifier = (distance * 0.65) / 10;
                if (speedModifier > 1) {
                    speedModifier = 1;
                }
                if (speedModifier < 0.76) {
                    speedModifier = 0.76;
                }
                getPackets().sendTestProjectile(new NewProjectile(projectile.getFrom(), projectile.getTo(), projectile.getGraphicId(), projectile.getStartHeight(), projectile.getEndHeight(), 0, projectile.getSlope(), (int) (projectile.getSpeed() * speedModifier), 2));
            }
        }
    }

    public void sendInputName(final String question, final InputNameEvent event) {
        getTemporaryAttributtes().put("pluginString", event);
        getPackets().sendInputNameScript(question);
    }

    public void sendInputString(final String question, final InputStringEvent event) {
        getTemporaryAttributtes().put("pluginLongString", event);
        getPackets().sendInputLongTextScript(question);
    }

    public void sendOptionsDialogue(final String question, final String[] options, final DialogueOptionEvent e) {
        getTemporaryAttributtes().put("pluginOption", e);
        Dialogue.sendOptionsDialogueStatic(this, question, options);
        setCloseInterfacesEvent(() -> getTemporaryAttributtes().remove("pluginOption"));
    }

    public void processLogicPackets() {
        // Native 947 players have no 910 logic-packet queue: their input arrives
        // through Native950GameTransport.drainActions on the world thread (P4).
        if (logicPackets == null && isNative950())
            return;
        getClientProfile().requireLegacyGameplay("910 gameplay input decoding");
        LogicPacket packet;
        while ((packet = logicPackets.poll()) != null) {
            try {
                PacketRepository.decodeLogicPacket(this, packet);
            } catch (final Exception e) {
                Logger.getGlobal().catching(e);
            }
        }
    }

    @Override
    public void processReceivedHits() {
        if (isLocked()) {
            return;
        }
        super.processReceivedHits();
    }

    private static BufferedWriter writer;

    public void realFinish() {
        if (hasFinished()) {
            return;
        }
        stopAll();
        if (accountPin.hasEnteredPin()) {
            lastPinIP = getIP();
        }
        StaffActivityMonitor.getInstance().saveTracker(this);
        cutscenesManager.logout();
        controlerManager.logout();
        if (flowerPokerSession != null) {
            flowerPokerSession.abort();
        }
        house.finish();
        coOpSlayer.handleLogout(this);
        setRunning(false);
        friendsIgnores.sendFriendsMyStatus(false);
        if (currentFriendChat != null) {
            currentFriendChat.leaveChat(this, true);
        }
        if (clanManager != null) {
            clanManager.disconnect(this, false);
        }
        if (guestClanManager != null) {
            guestClanManager.disconnect(this, true);
        }
        if (familiar != null && !familiar.isFinished()) {
            familiar.dissmissFamiliar(true);
        } else if (pet != null) {
            pet.finish();
        }
        GIM.removeFromPendingGroup(this);
        if (isGroupIronman()) {
            gimTracker.save();
            gimTracker.reset();
        }
        SaveBankSql.send(this);
        setFinished(true);
        realChannel.close();
        lastLoggedIn = System.currentTimeMillis();
        dungManager.finish();
        eliteDungeonsManager.finish();
        if (gamblingSession != null) {
            gamblingSession.refund(this);
        }
        if (!isAFK())
            setTotalPlayTime(getTotalPlayTime() + (getRecordedPlayTime() - Utils.currentTimeMillis()));
        // SerializableFilesManager.savePlayer(this);
        World.updateEntityRegion(this);
        World.removePlayer(this);
        SerializableFilesManager.savePlayer(this);
        if (isStaff()) {
            LoggingSystem.logStaffSession(this);
        }
        try {
            if (!Settings.TEST_SERVER_MODE) {
              //  World.getDiscordBot().getDiscordLinkage().updateRankInGame(this);
            }
        } catch (Exception e) {
            Logger.getGlobal().catching(e);
        }
        QueryExecutor.submit(new Hiscores(this, getTable()));
        if (Settings.DEBUG) {
            Logger.getGlobal().info("Finished Player: " + username);
        }
//        checkDC(this);
        Logger.getGlobal().info("Player " + getUsername() + " has logged out, " + "there are " + World.getPlayers().size() + " players on.");
//        PlayersOnlineManager.updatePlayersOnline();
    }

    @SuppressWarnings("unused")
    private static final void checkDC(final Player player) {
        if (Settings.WORLD_DC_DEBUG) {
            final Map<Thread, StackTraceElement[]> elements = Thread.getAllStackTraces();
            try {
                if (writer == null) {
                    writer = new BufferedWriter(new FileWriter("WorldDCs.txt"));
                }
                writer.flush();
                writer.write("-----------------------------------------------");
                writer.newLine();
                writer.write(player.getDisplayName() + ", date: " + new Date());
                writer.newLine();
                for (final Entry<Thread, StackTraceElement[]> element : elements.entrySet()) {
                    if (!element.getKey().getName().equals("World Thread")) {
                        continue;
                    }
                    writer.flush();
                    writer.write("Thread: " + element.getKey().getName());
                    writer.newLine();
                    for (final StackTraceElement e : element.getValue()) {
                        writer.write("Class: " + e.getClassName() + ", Method: " + e.getMethodName() + ", Line ID: " + e.getLineNumber());
                        writer.newLine();
                    }
                }
                writer.flush();
            } catch (final Exception e) {
                Logger.getGlobal().catching(e);
            }
        }
    }

    /**
     * Gets the Hiscores database table depending on the players gamemode.
     *
     * @return the Table name.
     */
    public String getTable() {
        return "hs_users";
    }

    public void refreshAllowChatEffects() {
        getPackets().sendConfig(171, allowChatEffects ? 0 : 1);
    }

    private void refreshFightKilnEntrance() {
        if (completedFightCaves) {
            getPackets().sendConfigByFile(10838, 1);
        }
    }

    public boolean followedByRandomEventNPC() {
        return (currentRandomEventNPC != null);
    }

    /**
     * The engine stores ten hitpoints per Constitution level. Both native caches
     * display one hundred life points per level (950 script 16856), but 950 moved
     * current life points from varbit 1668 to varp 13537 (script 8122).
     * See protocol-analysis/hitpoints-950.md for the 950 scale derivation.
     */
    public void refreshHitPoints() {
        if (getClientProfile() == ClientProfile.NATIVE_950) {
            // This is a full-width varp; the former 15-bit varbit ceiling no longer applies.
            long hp = (long) getHitpoints() * 10L;
            getPackets().sendConfig(13537, (int) Math.max(0L, Math.min(Integer.MAX_VALUE, hp)));
            return;
        }
        int hp = getHitpoints() * 10;
        if (hp >= Short.MAX_VALUE)
            hp = Short.MAX_VALUE;
        getPackets().sendConfigByFile(1668, hp);
    }

    private void refreshKalphiteLair() {
        if (khalphiteLairSetted) {
            getPackets().sendConfigByFile(16281, 1, true);
        }
    }

    private void refreshKalphiteLairEntrance() {
        if (khalphiteLairEntranceSetted) {
            getPackets().sendConfigByFile(16280, 1, true);
        }
    }

    private void refreshLodestoneNetwork() {
        if (lodestone == null || lodestone[9] != true) {
            lodestone = new boolean[]{false, false, false, false, false, false, false, false, false, true, false, false, false, false, false};
        }
        getPackets().sendConfigByFile(358, lodestone[0] ? 15 : 14);
        getPackets().sendConfigByFile(2448, lodestone[1] ? 190 : 189);
        for (int i = 10900; i < 10913; i++) {
            getPackets().sendConfigByFile(i, lodestone[(i - 10900) + 2] ? 1 : -1);
        }
    }

    public void refreshMoneyPouch() {
        getPackets().sendRunScript(5560, getMoneyPouch().getTotal());
    }

    public void refreshMouseButtons() {
        getPackets().sendConfig(455, mouseButtons ? 0 : 1);
    }

    @Getter
    @Setter
    private boolean displayBanned;

    public void toggleDisplayBan() {
        displayBanned = !displayBanned;
    }

    private boolean titleBanned;

    public boolean isTitleBanned() {
        return titleBanned;
    }

    public void setTitleBanned(final boolean value) {
        titleBanned = value;
    }

    private transient VarsManager varsManager;

    public VarsManager getVarsManager() {
        if (varsManager == null) {
            varsManager = new VarsManager(this);
        }
        return varsManager;
    }

    public void refreshSpawnedItems() {
        for (final int regionId : getMapRegionsIds()) {
            final List<FloorItem> floorItems = World.getRegion(regionId).getGroundItems();
            if (floorItems == null) {
                continue;
            }
            for (final FloorItem item : floorItems) {
                if (item.isInvisible() && (item.hasOwner() && !getUsername().equals(item.getOwner())) || item.getTile().getPlane() != getPlane() || !getUsername().equals(item.getOwner()) && !ItemConstants.isTradeable(item)) {
                    continue;
                }
                getPackets().sendRemoveGroundItem(item);
            }
        }
        for (final int regionId : getMapRegionsIds()) {
            final List<FloorItem> floorItems = World.getRegion(regionId).getGroundItems();
            if (floorItems == null) {
                continue;
            }
            for (final FloorItem item : floorItems) {
                if ((item.isInvisible()) && (item.hasOwner() && !getUsername().equals(item.getOwner())) || item.getTile().getPlane() != getPlane() || !getUsername().equals(item.getOwner()) && !ItemConstants.isTradeable(item)) {
                    continue;
                }
                getPackets().sendGroundItem(item);
            }
        }
    }

    public void refreshSpawnedObjects() {
        for (final int regionId : getMapRegionsIds()) {
            final List<WorldObject> spawnedObjects = World.getRegion(regionId).getSpawnedObjects();
            if (spawnedObjects != null) {
                for (final WorldObject object : spawnedObjects) {
                    if (object.getPlane() == getPlane()) {
                        getPackets().sendSpawnedObject(object);
                    }
                }
            }
            final List<WorldObject> removedObjects = World.getRegion(regionId).getRemovedOriginalObjects();
            if (removedObjects != null) {
                for (final WorldObject object : removedObjects) {
                    if (object.getPlane() == getPlane()) {
                        getPackets().sendDestroyObject(object);
                    }
                }
            }
        }
    }

    @Override
    public void removeHitpoints(final Hit hit) {
        super.removeHitpoints(hit);
        refreshHitPoints();
    }

    public void sendInputInteger(final String question, final InputIntegerEvent event) {
        getTemporaryAttributtes().put("pluginInteger", event);
        getPackets().sendInputIntegerScript(question);
    }
    
    public void sendIComponentInputInteger(int interfaceId, int componentId, int length, final InputIntegerComponentEvent event) {
        getTemporaryAttributtes().put("pluginInteger", event);
        getPackets().sendIComponentInputInteger(interfaceId, componentId, length);
    }
    
    public void sendIComponentInputString(int interfaceId, int componentId, int length, final InputStringEvent event) {
        getTemporaryAttributtes().put("pluginLongString", event);
        getPackets().sendIComponentInputText(interfaceId, componentId, length);
    }
    
    public void removeSkull() {
        skullDelay = -1;
        getAppearence().generateAppearenceData();
    }

    public void reset(final boolean attributes, final boolean pots) {
        reset(attributes, pots, true);
    }

    public void reset(final boolean attributes, final boolean pots, boolean resetSpells) {
        Boolean autoCast = (Boolean) getTemporaryAttributtes().get("autocast");
        super.reset(attributes);
        hintIconsManager.removeAll();
        getSkills().restoreSkills();
        combatDefinitions.resetSpecialAttack();
        prayer.reset();
        if (autoCast != null && !resetSpells)
            getTemporaryAttributtes().put("autocast", autoCast);
        if (resetSpells)
            if (getTemporaryAttributtes().remove("autocast") == null) {
                combatDefinitions.setAutoCast(-1);
            }
        resting = false;
        skullDelay = 0;
        foodDelay = 0;
        cantWalk = false;
        karamDelay = 0;
        lastBonfire = 0;
        getTemporaryAttributtes().remove("bleed");
        if (pots) {
            potDelay = 0;
            if (getTemporaryAttributtes().remove("dungOverload") == null) {
                if (getOverloadDelay() > 0) {
                    Pots.resetOverLoadEffect(this);
                }
                if (getSupremeOverloadDelay() > 0) {
                    Pots.resetSupremeOverLoadEffect(this);
                }
                poisonImmune = 0;
                fireImmune = 0;
                superAntiFire = 0;
                prayerRenewalDelay = 0;
                castedVeng = false;
            }
        }
        setRunEnergy(100);
        removeDamage(this);
        getEquipment().refreshConfigs(false);
        refreshHitPoints();
        getAppearence().generateAppearenceData();
    }

    @Override
    public void reset() {
        reset(true);
    }

    @Override
    public void reset(final boolean attributes) {
        reset(attributes, true);
    }

    // Only dungeoneering should use this, resets everything except bonfires.
    public void resetForDungeoneering() {
        super.reset(true);
        hintIconsManager.removeAll();
        getSkills().restoreSkills();
        combatDefinitions.resetSpecialAttack();
        prayer.reset();
        if (getTemporaryAttributtes().remove("autocast") == null) {
            combatDefinitions.setAutoCast(-1);
        }
        resting = false;
        skullDelay = 0;
        foodDelay = 0;
        cantWalk = false;
        potDelay = 0;
        karamDelay = 0;
        // lastBonfire = 0;
        getTemporaryAttributtes().remove("bleed");
        if (getTemporaryAttributtes().remove("dungOverload") == null) {
            if (getOverloadDelay() > 0) {
                Pots.resetOverLoadEffect(this);
            }
            if (getSupremeOverloadDelay() > 0) {
                Pots.resetSupremeOverLoadEffect(this);
            }
            poisonImmune = 0;
            fireImmune = 0;
            superAntiFire = 0;
            prayerRenewalDelay = 0;
            castedVeng = false;
        }
        setRunEnergy(100);
        removeDamage(this);
        getEquipment().refreshConfigs(false);
        refreshMaxHitpoints();
        getAppearence().generateAppearenceData();
    }

    public void resetDungeoneeringAttributes() {
        super.reset(false);
        hintIconsManager.removeAll();
        combatDefinitions.resetSpecialAttack();
        prayer.reset();
        if (getTemporaryAttributtes().remove("autocast") == null) {
            combatDefinitions.setAutoCast(-1);
        }
        resting = false;
        skullDelay = 0;
        foodDelay = 0;
        cantWalk = false;
        potDelay = 0;
        karamDelay = 0;
        // lastBonfire = 0;
        getTemporaryAttributtes().remove("bleed");
        setRunEnergy(100);
        removeDamage(this);
        getEquipment().refreshConfigs(false);
        refreshHitPoints();
        getAppearence().generateAppearenceData();
    }

    public void resetBarrows() {
        hiddenBrother = -1;
        killedBarrowBrothers = new boolean[8];
        barrowsKillCount = 0;
    }

    @Override
    public void resetMasks() {
        super.resetMasks();
        if (isNative950()) {
            // P4: the Entity mask reset only. refreshSpawnedObjects/Items would push
            // LOC_ADD_CHANGE/OBJ_ADD through the facade (STRICT until M6); the
            // native session marks the region loaded after each REBUILD flush.
            return;
        }
        if (!clientHasLoadedMapRegion()) {
            // load objects and items here
            // load new npcs spawn after update
            setClientHasLoadedMapRegion();
            refreshSpawnedObjects();
            refreshSpawnedItems();
        }
    }

    @Override
    public boolean restoreHitPoints() {
        final boolean update = super.restoreHitPoints();
        if (update) {
            if (resting) {
                super.restoreHitPoints();
            }
            if (getAuraManager().usingRegeneration()) {
                super.restoreHitPoints();
            }
            refreshHitPoints();
        }
        return update;
    }

    private transient double extraEnergy;

    public void restoreRunEnergy() {
        if (getNextRunDirection() != -1 || runEnergy >= 100) {
            return;
        }
        final double rate = 0.26877 + (getSkills().getLevel(Skills.AGILITY) * 0.00477);
        extraEnergy += rate;
        if (extraEnergy >= 1) {
            runEnergy++;
            if (runEnergy > 100) {
                runEnergy = 100;
            }
            getPackets().sendRunEnergy();
            extraEnergy--;
        }
    }

    private boolean enteredSophanemBefore;

    private List<Item> sophanemChestLoot;

    public boolean hasEnteredSophanemBefore() {
        return enteredSophanemBefore;
    }

    public void setEnteredSophanemBefore(boolean entered) {
        enteredSophanemBefore = entered;
    }

    public final List<Item> getSophanemChestLoot() {
        return sophanemChestLoot;
    }

    public final void setSophanemChest() {
        sophanemChestLoot = new ArrayList<Item>();
    }

    private byte sophanemCorruption;

    public byte getSophanemCorruption() {
        return sophanemCorruption;
    }

    public void setSophanemCorruption(final int value) {
        sophanemCorruption = (byte) value;
    }

    private boolean automaticLootCollection;

    public boolean isAutomaticSophanemLootCollection() {
        return automaticLootCollection;
    }

    public void setAutomaticLootCollection(final boolean val) {
        automaticLootCollection = val;
    }

    public void addSophanemChestLoot(final int id, int amount) {
        for (final Item i : sophanemChestLoot) {
            if (i.getId() == id) {
                amount += i.getAmount();
                sophanemChestLoot.remove(i);
                break;
            }
        }
        sophanemChestLoot.add(new Item(id, amount));
    }

    public void removeSophanemChestLoot(final int id, final int amount) {
        for (final Item i : sophanemChestLoot) {
            if (i.getId() == id) {
                if (amount >= i.getAmount()) {
                    sophanemChestLoot.remove(i);
                } else {
                    i.setAmount(i.getAmount() - amount);
                }
                break;
            }
        }
    }

    @Override
    public void sendDeath(final Entity source) {
        final boolean hcimSafe = getControlerManager().getControler() instanceof PestControlGame || getControlerManager().getControler() instanceof DTController || getControlerManager().getControler() instanceof Dungeoneering || getControlerManager().getControler() instanceof FightCaves || getControlerManager().getControler() instanceof FightKiln || getControlerManager().getControler() instanceof AreaController || getControlerManager().getControler() instanceof DungeonController || getControlerManager().getControler() instanceof SnowballFightGameController || getControlerManager().getControler() instanceof SnowballLobbyController || source instanceof Player;
        if (isHCIronMan() && !hcimSafe) {
            QueryExecutor.submit(new ResetUserHiscores(getUsername(), getTable()));
            if (getSkills().getTotalLevel(this) >= 500) {
                World.sendWorldMessage(Colors.RED + "<shad=000000><img=18>News: " + getDisplayName() + " just died in Hardcore Ironman mode with a skill total of " + getSkills().getTotalLevel(this) + "!", false);
            }
            setHCIronMan(false);
            setIronMan(true);
            QueryExecutor.submit(new Hiscores(this, getTable()));
            HcimNewsManager.getInstance().addDeath(this);
        }

        if (prayer.hasPrayersOn() && getTemporaryAttributtes().get("startedDuel") != Boolean.TRUE) {
            if (prayer.usingPrayer(0, 14)) {
                setNextGraphics(new Graphics(437));
                final Player target = this;
                if (isAtMultiArea()) {
                    for (final int regionId : getMapRegionsIds()) {
                        final List<Integer> playersIndexes = World.getRegion(regionId).getPlayerIndexes();
                        if (playersIndexes != null) {
                            for (final int playerIndex : playersIndexes) {
                                final Player player = World.getPlayers().get(playerIndex);
                                if (player == null || !player.isActive() || player.isDead() || player.hasFinished() || !player.withinDistance(this, 1) || !player.isCanPvp() || !target.getControlerManager().canHit(player)) {
                                    continue;
                                }
                                player.applyHit(new Hit(target, Utils.random((int) (getSkills().getLevelForXp(Skills.PRAYER) * 2.5)), HitLook.REGULAR_DAMAGE));
                            }
                        }
                        final List<Integer> npcsIndexes = World.getRegion(regionId).getNPCsIndexes();
                        if (npcsIndexes != null) {
                            for (final int npcIndex : npcsIndexes) {
                                final NPC npc = World.getNPCs().get(npcIndex);
                                if (npc == null || npc.isDead() || npc.hasFinished() || !npc.withinDistance(this, 1) || !npc.getDefinitions().hasAttackOption() || !target.getControlerManager().canHit(npc)) {
                                    continue;
                                }
                                npc.applyHit(new Hit(target, Utils.random((int) (getSkills().getLevelForXp(Skills.PRAYER) * 2.5)), HitLook.REGULAR_DAMAGE));
                            }
                        }
                    }
                } else {
                    if (source != null && source != this && !source.isDead() && !source.hasFinished() && source.withinDistance(this, 1)) {
                        source.applyHit(new Hit(target, Utils.random((int) (getSkills().getLevelForXp(Skills.PRAYER) * 2.5)), HitLook.REGULAR_DAMAGE));
                    }
                }
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        World.sendGraphics(target, new Graphics(438), new WorldTile(target.getX() - 1, target.getY(), target.getPlane()));
                        World.sendGraphics(target, new Graphics(438), new WorldTile(target.getX() + 1, target.getY(), target.getPlane()));
                        World.sendGraphics(target, new Graphics(438), new WorldTile(target.getX(), target.getY() - 1, target.getPlane()));
                        World.sendGraphics(target, new Graphics(438), new WorldTile(target.getX(), target.getY() + 1, target.getPlane()));
                        World.sendGraphics(target, new Graphics(438), new WorldTile(target.getX() - 1, target.getY() - 1, target.getPlane()));
                        World.sendGraphics(target, new Graphics(438), new WorldTile(target.getX() - 1, target.getY() + 1, target.getPlane()));
                        World.sendGraphics(target, new Graphics(438), new WorldTile(target.getX() + 1, target.getY() - 1, target.getPlane()));
                        World.sendGraphics(target, new Graphics(438), new WorldTile(target.getX() + 1, target.getY() + 1, target.getPlane()));
                    }
                });
            } else if (prayer.usingPrayer(1, 27)) {
                World.sendProjectile(this, new WorldTile(getX() + 2, getY() + 2, getPlane()), 2261, 24, 0, 41, 35, 30, 0);
                World.sendProjectile(this, new WorldTile(getX() + 2, getY(), getPlane()), 2261, 41, 0, 41, 35, 30, 0);
                World.sendProjectile(this, new WorldTile(getX() + 2, getY() - 2, getPlane()), 2261, 41, 0, 41, 35, 30, 0);

                World.sendProjectile(this, new WorldTile(getX() - 2, getY() + 2, getPlane()), 2261, 41, 0, 41, 35, 30, 0);
                World.sendProjectile(this, new WorldTile(getX() - 2, getY(), getPlane()), 2261, 41, 0, 41, 35, 30, 0);
                World.sendProjectile(this, new WorldTile(getX() - 2, getY() - 2, getPlane()), 2261, 41, 0, 41, 35, 30, 0);

                World.sendProjectile(this, new WorldTile(getX(), getY() + 2, getPlane()), 2261, 41, 0, 41, 35, 30, 0);
                World.sendProjectile(this, new WorldTile(getX(), getY() - 2, getPlane()), 2261, 41, 0, 41, 35, 30, 0);
                final Player target = this;
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        setNextGraphics(new Graphics(2259));

                        if (isAtMultiArea()) {
                            for (final int regionId : getMapRegionsIds()) {
                                final List<Integer> playersIndexes = World.getRegion(regionId).getPlayerIndexes();
                                if (playersIndexes != null) {
                                    for (final int playerIndex : playersIndexes) {
                                        final Player player = World.getPlayers().get(playerIndex);
                                        if (player == null || !player.isActive() || player.isDead() || player.hasFinished() || !player.isCanPvp() || !player.withinDistance(target, 2) || !target.getControlerManager().canHit(player)) {
                                            continue;
                                        }
                                        player.applyHit(new Hit(target, Utils.random((getSkills().getLevelForXp(Skills.PRAYER) * 3)), HitLook.REGULAR_DAMAGE));
                                    }
                                }
                                final List<Integer> npcsIndexes = World.getRegion(regionId).getNPCsIndexes();
                                if (npcsIndexes != null) {
                                    for (final int npcIndex : npcsIndexes) {
                                        final NPC npc = World.getNPCs().get(npcIndex);
                                        if (npc == null || npc.isDead() || npc.hasFinished() || !npc.withinDistance(target, 2) || !npc.getDefinitions().hasAttackOption() || !target.getControlerManager().canHit(npc)) {
                                            continue;
                                        }
                                        npc.applyHit(new Hit(target, Utils.random((getSkills().getLevelForXp(Skills.PRAYER) * 3)), HitLook.REGULAR_DAMAGE));
                                    }
                                }
                            }
                        } else {
                            if (source != null && source != target && !source.isDead() && !source.hasFinished() && source.withinDistance(target, 2)) {
                                source.applyHit(new Hit(target, Utils.random((getSkills().getLevelForXp(Skills.PRAYER) * 3)), HitLook.REGULAR_DAMAGE));
                            }
                        }

                        World.sendGraphics(target, new Graphics(2260), new WorldTile(getX() + 2, getY() + 2, getPlane()));
                        World.sendGraphics(target, new Graphics(2260), new WorldTile(getX() + 2, getY(), getPlane()));
                        World.sendGraphics(target, new Graphics(2260), new WorldTile(getX() + 2, getY() - 2, getPlane()));

                        World.sendGraphics(target, new Graphics(2260), new WorldTile(getX() - 2, getY() + 2, getPlane()));
                        World.sendGraphics(target, new Graphics(2260), new WorldTile(getX() - 2, getY(), getPlane()));
                        World.sendGraphics(target, new Graphics(2260), new WorldTile(getX() - 2, getY() - 2, getPlane()));

                        World.sendGraphics(target, new Graphics(2260), new WorldTile(getX(), getY() + 2, getPlane()));
                        World.sendGraphics(target, new Graphics(2260), new WorldTile(getX(), getY() - 2, getPlane()));

                        World.sendGraphics(target, new Graphics(2260), new WorldTile(getX() + 1, getY() + 1, getPlane()));
                        World.sendGraphics(target, new Graphics(2260), new WorldTile(getX() + 1, getY() - 1, getPlane()));
                        World.sendGraphics(target, new Graphics(2260), new WorldTile(getX() - 1, getY() + 1, getPlane()));
                        World.sendGraphics(target, new Graphics(2260), new WorldTile(getX() - 1, getY() - 1, getPlane()));
                    }
                });
            }
        }
        final boolean safe = getTemporaryAttributtes().remove("safedeath") != null;
        final WorldTile deathTile = getDeathTile(safe);
        if (currentInstance != null) {
            currentInstance.removePlayer(this);
        }
        setNextAnimation(new Animation(-1));
        if (!controlerManager.sendDeath()) {
            return;
        }
        if (isGroupIronman()) {
            gimTracker.incrementDeaths(null);
        }
        lock();
        stopAll();
        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                if (loop == 0) {
                    setAggressiveDelay(0);
                    getBuffDebuffTimersManager().removeTimer(Timer.AGGRESSION_POTION_ACTIVE);
                    setNextAnimation(new Animation(isShadow() ? 28225 : 836));
                    sendMessage("Oh dear, you have died.");
                    deathItemsManager.handleDeath();
                    if (source instanceof Player) {
                        if (familiar != null) {
                            familiar.sendDeath(source);
                        }
                        final Player killer = (Player) source;
                        killer.setAttackedByDelay(4);
                    }
                }
                if (loop == 3) {
                    if (isShadow()) {
                        setShadow(false);
                        getAppearence().transformIntoNPC(-1);
                    }
                    setNextAnimation(new Animation(-1));
                    getPackets().sendMusicEffect(90);
                    getDeathManager().reset();
                    if (deathTile != null) {
                        getDeathManager().setDeathCoordinates(deathTile);
                    }
                    setNextAnimation(new Animation(-1));
                    reset();
                    unlock();
                    stop();
                }
                loop++;
            }
        }, 0, 1);
    }

    private final WorldTile getDeathTile(boolean safe) {
        if (currentInstance != null) {
            return currentInstance.getOutsideCoordinates();
        }
        if (getControlerManager().getControler() instanceof QueenBlackDragonController) {
            return new WorldTile(1198, 6499, 0);
        }
        return null;
    }

    public Animation getDeathAnimation() {
        // setNextGraphics(new Graphics(Utils.random(2) == 0 ? 4399 : 4398));
        return new Animation(836);
    }

    public void sendDefaultPlayersOptions() {
        getPackets().sendPlayerOption("Follow", 2, false);
        getPackets().sendPlayerOption("Trade with", 4, false);
        getPackets().sendPlayerOption("Examine", 5, false);
        getPackets().sendPlayerOption("null", 6, false);
        getPackets().sendPlayerOption("null", 7, false);
        getPackets().sendPlayerOption("Request item", 9, false);
        /*
         * If this is enabled, do not use option 7 without handling the staff menu if
         * (!isIronMan() && !isHCIronMan()) getPackets().sendPlayerOption("Gamble with",
         * 7, false);
         */
    }

    public void sendFriendsChannelMessage(final String message) {
        if (currentFriendChat == null) {
            return;
        }
        currentFriendChat.sendMessage(this, message);
    }

    public void sendFriendsChannelQuickMessage(final QuickChatMessage message) {
        if (currentFriendChat == null) {
            return;
        }
        currentFriendChat.sendQuickMessage(this, message);
    }

    public void sendItemsOnDeath(final Player killer, final boolean dropItems) {
        final Integer[][] slots = ButtonHandler.getItemSlotsKeptOnDeath(this, true, hasSkull(), getPrayer().isProtectingItem());
        sendItemsOnDeath(killer, new WorldTile(this), new WorldTile(this), true, slots);
    }

    public void sendItemsOnDeath(final Player killer, final WorldTile deathTile, final WorldTile respawnTile, final boolean wilderness, final Integer[][] slots) {
        if (killer == null) {
            return;
        }
        if (safePvp) {
            return;
        }
        if (!isCanPvp()) {
            return;
        }
        if (getUsername().equalsIgnoreCase("youtube") || killer.getUsername().equalsIgnoreCase("youtube")) {
            return;
        }
        if (getUsername().equalsIgnoreCase("community") || killer.getUsername().equalsIgnoreCase("community")) {
            return;
        }
        /*
         * if (killer.isOwner()) {
         * sendMessage("You didn't loose your items on death due to a Developer killing you."
         * ); World.addGroundItem(new Item(526, 1), deathTile, 60); return; } if
         * (isOwner()) {
         * sendMessage("You didn't loose your items on death due to being an Administrator."
         * ); World.addGroundItem(new Item(526, 1), deathTile, 60); return; }
         */
        /*
         * if (killer.getCurrentMac().equalsIgnoreCase(getCurrentMac())) {
         * Logger.getGlobal().info("Killer: " + getUsername() + " killed " +
         * killer.getUsername() + " on same computer."); World.addGroundItem(new
         * Item(526, 1), deathTile, 60); return; } if
         * (killer.getIP().equalsIgnoreCase(getIP())) {
         * Logger.getGlobal().info("Killer: " + getUsername() + " killed " +
         * killer.getUsername() + " on same computer."); World.addGroundItem(new
         * Item(526, 1), deathTile, 60); return; }
         */
//        charges.die();
        auraManager.removeAura();
        final Item[][] items = ButtonHandler.getItemsKeptOnDeath(this, slots);
        inventory.reset();
        equipment.reset();
        getAppearence().generateAppearenceData();
        for (final Item item : items[0]) {
            if (!ItemConstants.keptOnDeath(item)) {
                World.addGroundItem(item, deathTile, this, true, 60);
            } else {
                inventory.addItem(item.getId(), item.getAmount());
            }
        }
        World.addGroundItem(new Item(526, 1), deathTile, killer == null ? this : (killer.isIronMan() || killer.isHCIronMan() || killer.isNoviceIronMan() || killer.isExpertIronMan() || killer.isIntermediateIronMan()) ? this : killer, true, 60);
        if (items[1].length != 0) {
            for (final Item item : items[1]) {
                if (ItemConstants.keptOnDeath(item)) {
                    getInventory().addItem(item);
                    continue;
                }
//                if (ItemConstants.degradeOnDrop(item)) {
//                    getCharges().degradeCompletely(item);
//                }
//                if (ItemConstants.getItemComposition(item) != null) {
//                    if (ItemConstants.getItemComposition(item).length == 2) {
//                        World.updateGroundItem(new Item(ItemConstants.getItemComposition(item)[1], 1), deathTile, killer == null ? this : (killer.isIronMan() || killer.isHCIronMan() || killer.isNoviceIronMan() || killer.isExpertIronMan() || killer.isIntermediateIronMan()) ? this : killer, 60, 1, false);
//                    }
//                    item.setId(ItemConstants.getItemComposition(item)[0]);
//                }
                if (ItemConstants.turnCoins(item) && (isAtWild() || FfaZone.inPvpArea(this))) {
                    final int price = GrandExchange.getPrice(item.getId()) / 4;
                    item.setId(995);
                    item.setAmount(price);
                }
                World.updateGroundItem(item, deathTile, killer == null ? this : (killer.isIronMan() || killer.isHCIronMan() || killer.isNoviceIronMan() || killer.isExpertIronMan() || killer.isIntermediateIronMan()) ? this : killer, 60, 1, false);
            }
        }
    }

    public final boolean isAtWild() {
        return (getX() >= 3011 && getX() <= 3132 && getY() >= 10052 && getY() <= 10175) || (getX() >= 2940 && getX() <= 3395 && getY() >= 3525 && getY() <= 4000) || (getX() >= 3264 && getX() <= 3279 && getY() >= 3279 && getY() <= 3672) || (getX() >= 3158 && getX() <= 3181 && getY() >= 3679 && getY() <= 3697) || (getX() >= 3280 && getX() <= 3183 && getY() >= 3885 && getY() <= 3888) || (getX() >= 3012 && getX() <= 3059 && getY() >= 10303 && getY() <= 10351) || (getX() >= 3060 && getX() <= 3072 && getY() >= 10251 && getY() <= 10263);
    }

    public void sendMessage(final String message, final boolean filter) {
        getPackets().sendGameMessage(message, filter);
    }

    public void sendFilteredMessage(String message) {
        sendMessage(message, true);
    }

    public void sendMessage(final String message) {
        getPackets().sendGameMessage(message);
    }

    public void sendConsoleMessage(String text) {
        getPackets().sendConsoleMessage(text);
    }

    public void sendPublicChatMessage(final PublicChatMessage message) {
        if (isBot()) {
            for (final Player player : World.getPlayers()) {
                if (player == null || player.isBot() || !player.isRunning() || !player.isActive()
                        || !player.clientHasLoadedMapRegion() || player.hasFinished()
                        || !player.withinDistance(this, 24)
                        || !isVisibleLocalPlayer(player, this)
                        || player.getFriendsIgnores().getIgnores().contains(username)) {
                    continue;
                }
                player.getPackets().sendPublicMessage(this, message);
            }

            /** Public chat logging */
            GenericChatLog.list.add(new GenericChatLog(this, message.getMessage()));
            return;
        }
        for (int i = getLocalPlayerUpdate().getLocalPlayersIndexesCount() - 1; i >= 0; i--) {
            final Player player = getLocalPlayerUpdate().getLocalPlayers()[getLocalPlayerUpdate().getLocalPlayersIndexes()[i]];
            if (player == null || !player.isRunning() || player.hasFinished() || player.getFriendsIgnores().getIgnores().contains(username)) {
                continue;
            }
            player.getPackets().sendPublicMessage(this, message);
        }

        /** Public chat logging */
        GenericChatLog.list.add(new GenericChatLog(this, message.getMessage()));
    }

    private static boolean isVisibleLocalPlayer(Player viewer, Player speaker) {
        if (viewer == null || speaker == null || viewer.getLocalPlayerUpdate() == null) {
            return false;
        }
        int index = speaker.getIndex();
        Player[] localPlayers = viewer.getLocalPlayerUpdate().getLocalPlayers();
        return index >= 0 && index < localPlayers.length && localPlayers[index] == speaker;
    }

    public void sendRunButtonConfig() {
        getPackets().sendConfig(463, resting ? 3 : getRun() ? 1 : 0);
    }

    public void sendUnlockedObjectConfigs() {
        refreshKalphiteLairEntrance();
        refreshKalphiteLair();
        refreshLodestoneNetwork();
        refreshFightKilnEntrance();
        refreshCurrentTelosReward();
    }

    public int setBarrowsKillCount(final int barrowsKillCount) {
        return this.barrowsKillCount = barrowsKillCount;
    }

    public void incrementBarrowsRunsDone() {
        barrowsRunsDone++;
    }

    public void setClientHasLoadedMapRegion() {
        clientLoadedMapRegion = true;
    }
    
    public boolean clientHasLoadedMapRegion() {
        return clientLoadedMapRegion;
    }
    
    public void setClientHasntLoadedMapRegion() {
        clientLoadedMapRegion = false;
    }

    public void setCloseInterfacesEvent(final Runnable closeInterfacesEvent) {
        this.closeInterfacesEvent = closeInterfacesEvent;
    }

    public void setClueReward(final int clueReward) {
    }

    public void setCompletedFightCaves() {
        if (!completedFightCaves) {
            completedFightCaves = true;
            refreshFightKilnEntrance();
        }
    }

    public void setCompletedFightCaves2() {
        completedFightCaves = true;
    }

    public void setCompletedFightKiln() {
        completedFightKiln = true;
    }

    public void setCompletedRfd() {
        completedRfd = true;
    }

    public int setDeathCount(final int deathCount) {
        return this.deathCount = deathCount;
    }

    public void setDefenderRoom(final boolean isInDefenderRoom) {
        this.isInDefenderRoom = isInDefenderRoom;
    }

    public void setDisableEquip(final boolean equip) {
        disableEquip = equip;
    }

    public void setEmailAttached(final String email) {
    }

    public void setFightPitsSkull() {
        skullDelay = Integer.MAX_VALUE;
        skullId = 1;
        getAppearence().generateAppearenceData();
    }

    public void setHideWorldMessages(final boolean hideWorldAnnouncements) {
        this.hideWorldAnnouncements = hideWorldAnnouncements;
    }

    public void setInvulnerable(final boolean invulnerable) {
        this.invulnerable = invulnerable;
    }

    public void setIsInLobby(final boolean isInLobby) {
    }

    private transient long ROTSLockDelay;

    public void lockROTS() {
        ROTSLockDelay = Long.MAX_VALUE;
    }

    public boolean isROTSLocked() {
        return ROTSLockDelay >= Utils.currentTimeMillis();
    }

    public void unlockROTS() {
        ROTSLockDelay = 0;
    }

    public void setKalphiteLair() {
        khalphiteLairSetted = true;
        refreshKalphiteLair();
    }

    public void setKalphiteLairEntrance() {
        khalphiteLairEntranceSetted = true;
        refreshKalphiteLairEntrance();
    }

    public int setKillCount(final int killCount) {
        return this.killCount = killCount;
    }

    public int setDropRate(final double d) {
        return (int) (dropRate = d);
    }

    public int setTotalKillStreakPoints(final int totalkillStreakPoints) {
        return this.totalkillStreakPoints = totalkillStreakPoints;
    }

    public int setKillStreakPoints(final int killStreakPoints) {
        return this.killStreakPoints = killStreakPoints;
    }

    public void setKilledFlamBeed(final boolean flamBeed) {
        this.flamBeed = flamBeed;
    }

    public void setLargeSceneView(final boolean largeSceneView) {
        this.largeSceneView = largeSceneView;
        setNPCViewDistanceBits(largeSceneView ? 8 : 5, true);
    }

    public void setPrayerRenewalDelay(final int delay) {
        prayerRenewalDelay = delay;
    }

    public int getPrayerRenewalDelay() {
        return prayerRenewalDelay;
    }

    @Override
    public void setRun(final boolean run) {
        if (run != getRun()) {
            super.setRun(run);
            sendRunButtonConfig();
        }
    }

    /**
     * @param runeSpanPoints the runeSpanPoint to set
     */
    public void setRuneSpanPoint(final int runeSpanPoints) {
        this.runeSpanPoints = runeSpanPoints;
    }

    public void setRunHidden(final boolean run) {
        super.setRun(run);
    }

    public int setSkullDelay(final int delay) {
        return skullDelay = delay;
    }

    public void setSkullInfiniteDelay(final int skullId) {
        skullDelay = Integer.MAX_VALUE;
        this.skullId = skullId;
        getAppearence().generateAppearenceData();
    }

    public void setTalkedToCook() {
        talkedtoCook = true;
    }

    public void setTalkedWithMarv() {
        talkedWithMarv = true;
    }

    public void setRTask(final Contract rtask) {
        Rtask = rtask;
    }

    public void setWildernessSkull() {
        skullDelay = 3000;
        skullId = 0;
        getAppearence().generateAppearenceData();
    }

    public void setWonFightPits() {
        wonFightPits = true;
    }

    public void setYellDisabled(final boolean yellDisabled) {
        this.yellDisabled = yellDisabled;
    }

    public DistinctionCape getDistinctionCapes() {
        return distinctionCape;
    }

    // now that we inited we can start showing game
    public void start() {


        ensureAccountPin();                 // < add this FIRST

        if (AccountPin.DISABLED) {          // optional, but nice in single-player
            this.lastPinIP = getIP();
            getAccountPin().resetLocked();
            getAccountPin().setPinEntered();
        }


        // send(new GameWorldResponse(getRights(), getIndex(), getDisplayName()));
        loadMapRegions();
        LoginManager.sendLogin(this);
        if (isDead() || getHitpoints() <= 0) {
            sendDeath(null);
        }
        setActive(true);
        World.updateEntityRegion(this);
    }








    public void setSlayer(final Slayer slayer) {
        this.slayer = slayer;
    }

    public void stopAll() {
        stopAll(true);
    }

    public void stopAll(final boolean stopWalk) {
        stopAll(stopWalk, true);
    }

    public void stopAll(final boolean stopWalk, final boolean stopInterface) {
        stopAll(stopWalk, stopInterface, true);
    }

    // as walk done clientsided
    public void stopAll(final boolean stopWalk, final boolean stopInterfaces, final boolean stopActions) {
        routeEvent = null;
        if (stopInterfaces) {
            closeInterfaces();
        }
        if (stopWalk && !cantWalk) {
            resetWalkSteps();
        }
        if (stopActions) {
            actionManager.forceStop();
        }
        combatDefinitions.resetSpells(false);
    }

    public void switchAllowChatEffects() {
        allowChatEffects = !allowChatEffects;
        refreshAllowChatEffects();
    }

    public void switchMouseButtons() {
        mouseButtons = !mouseButtons;
        refreshMouseButtons();
    }

    public void switchReportOption() {
        reportOption = !reportOption;
        refreshReportOption();
    }

    public void refreshReportOption() {
        getPackets().sendConfig(1056, isUsingReportOption() ? 2 : 0);
    }

    public boolean isToogleLootShare() {
        return toogleLootShare;
    }

    public void disableLootShare() {
        if (isToogleLootShare()) {
            toogleLootShare = false;
            refreshToogleLootShare();
        }
    }

    public void refreshToogleLootShare() {
        // need to force cuz autoactivates when u click on it even if no chat
        VBM.forceSendVarBit(3306, toogleLootShare ? 1 : 0);
    }

    public void toogleRun(final boolean update) {
        super.setRun(!getRun());
        if (update) {
            sendRunButtonConfig();
        }
    }

    public void unlock() {
        lockDelay = 0;
    }

    public void useStairs(final int emoteId, final WorldTile dest, final int useDelay, final int totalDelay) {
        useStairs(emoteId, dest, useDelay, totalDelay, null);
    }

    public void useStairs(final int emoteId, final WorldTile dest, final int useDelay, final int totalDelay, final String message) {
        useStairs(emoteId, dest, useDelay, totalDelay, message, false);
    }

    public void useStairs(final int emoteId, final WorldTile dest, final int useDelay, final int totalDelay, final String message, final boolean resetAnimation) {
        stopAll();
        lock(totalDelay);
        if (emoteId != -1) {
            setNextAnimation(new Animation(emoteId));
        }
        if (useDelay == 0) {
            setNextWorldTile(dest);
        } else {
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    if (isDead()) {
                        return;
                    }
                    if (resetAnimation) {
                        setNextAnimation(new Animation(-1));
                    }
                    setNextWorldTile(dest);
                    if (message != null) {
                        getPackets().sendGameMessage(message);
                    }
                }
            }, useDelay - 1);
        }
    }

    public void vecnaTimer(final int amount) {
        if (getVecnaTimer() > 0) {
            CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
                @Override
                public boolean repeat() {
                    if (hasFinished()) {
                        return false;
                    }
                    if (getVecnaTimer() > 0) {
                        setVecnaTimer(getVecnaTimer() - 1);
                    }
                    if (getVecnaTimer() == 0) {
                        getPackets().sendGameMessage("<col=FFCC00>Your skull of Vecna has regained its mysterious aura.");
                        return false;
                    }
                    return true;
                }
            }, 10, 1, TimeUnit.MILLISECONDS);
        }
    }

    public void setRouteEvent(final RouteEvent routeEvent) {
        this.routeEvent = routeEvent;
    }

    public ClansManager getClanManager() {
        return clanManager;
    }

    public void setClanManager(final ClansManager clanManager) {
        this.clanManager = clanManager;
    }

    public String getClanName() {
        return clanName;
    }

    public void setClanName(final String clanName) {
        this.clanName = clanName;
    }

    public boolean isConnectedClanChannel() {
        return connectedClanChannel;
    }

    public void setConnectedClanChannel(final boolean connectedClanChannel) {
        this.connectedClanChannel = connectedClanChannel;
    }

    public ClansManager getGuestClanManager() {
        return guestClanManager;
    }

    public void setGuestClanManager(final ClansManager guestClanManager) {
        this.guestClanManager = guestClanManager;
    }

    public void sendClanChannelMessage(final ChatMessage message) {
        if (clanManager == null) {
            return;
        }
        clanManager.sendMessage(this, message);
    }

    public void sendGuestClanChannelMessage(final ChatMessage message) {
        if (guestClanManager == null) {
            return;
        }
        guestClanManager.sendMessage(this, message);
    }

    public void sendClanChannelQuickMessage(final QuickChatMessage message) {
        if (clanManager == null) {
            return;
        }
        clanManager.sendQuickMessage(this, message);
    }

    public void sendGuestClanChannelQuickMessage(final QuickChatMessage message) {
        if (guestClanManager == null) {
            return;
        }
        guestClanManager.sendQuickMessage(this, message);
    }

    public double[] getWarriorPoints() {
        return warriorPoints;
    }

    public void setWarriorPoints(final int index, final double pointsDifference) {
        warriorPoints[index] += pointsDifference;
        if (warriorPoints[index] < 0) {
            final Controller controler = getControlerManager().getControler();
            if (controler == null || !(controler instanceof WarriorsGuild)) {
                return;
            }
            final WarriorsGuild guild = (WarriorsGuild) controler;
            guild.inCyclopse = false;
            setNextWorldTile(WarriorsGuild.CYCLOPS_LOBBY);
            warriorPoints[index] = 0;
        } else if (warriorPoints[index] > 65535) {
            warriorPoints[index] = 65535;
        }
        refreshWarriorPoints(index);
    }

    public void refreshWarriorPoints(final int index) {
        getPackets().sendConfigByFile(index + 15398, (int) warriorPoints[index]);
    }

    public void warriorCheck() {
        if (warriorPoints == null || warriorPoints.length != 6) {
            warriorPoints = new double[6];
        }
    }

    public GlobalPlayerUpdater getAppearence() {
        return globalPlayerUpdater;
    }

    public void setGlobalPlayerUpdater(final GlobalPlayerUpdater globalPlayerUpdater) {
        this.globalPlayerUpdater = globalPlayerUpdater;
    }

    public long getThievingDelay() {
        return thievingDelay;
    }

    public void setThievingDelay(final long thievingDelay) {
        this.thievingDelay = thievingDelay;
    }

    public Channel getChannel() {
        return channel;
    }

    public Channel getRealChannel() {
        return realChannel;
    }

    public boolean isBot() {
        return false;
    }

    public boolean isDeveloper() {
        return (isOwner() || isDev());
    }

    public boolean isExpert() {
        return expert;
    }

    public void setExpert(final boolean vet) {
        expert = vet;
    }

    public boolean isNovice() {
        return novice;
    }

    public void setNovice(final boolean novice) {
        this.novice = novice;
    }

    public boolean isIntermediate() {
        return intermediate;
    }

    public void setIntermediate(final boolean interm) {
        intermediate = interm;
    }

    public boolean isIronMan() {
        return ironman;
    }

    public boolean isATypeOfIronman() {
        return ironman || hcironman || noviceironman || intermediateironman || expertironman || isGroupIronman();
    }

    public boolean isGroupIronman() {
        return gimKey != null;
    }

    public boolean isUnregisteredGIM() {
        return isGroupIronman() && gimKey.getGroupId() == -1;
    }

    public boolean canGimInteractWith(Player other) {
        if (isUnregisteredGIM() || other.isUnregisteredGIM())
            return false;
        return isGroupIronman() && other.isGroupIronman() && Objects.equals(other.gimName, gimName);
    }

    public void sendGimCantInteract() {
        sendMessage("You cannot do this with players that aren't in your group.");
    }

    public void setIronMan(final boolean ironm) {
        ironman = ironm;
    }

    public boolean isNoviceIronMan() {
        return noviceironman;
    }

    public void setNoviceIronMan(final boolean nironm) {
        noviceironman = nironm;
    }

    public boolean isExpertIronMan() {
        return expertironman;
    }

    public void setExpertIronMan(final boolean eironm) {
        expertironman = eironm;

    }

    public boolean isIntermediateIronMan() {
        return intermediateironman;
    }

    public void setIntermediateIronMan(final boolean iironm) {
        intermediateironman = iironm;

    }

    public void sm(final String message) {
        this.sendMessage(message);
    }

    public boolean isHCIronMan() {
        return hcironman;
    }

    public void setHCIronMan(final boolean hardcoreim) {
        hcironman = hardcoreim;
    }

    public boolean isLegendary() {
        return legendary;
    }

    public void setLegendary(final boolean legendary) {
        this.legendary = legendary;
    }

    public Contract getContract() {
        return cContracts;
    }

    public void setContract(final Contract contract) {
        cContracts = contract;
        updateSlayerCounterInformation();
    }

    public void setCompleted() {
        getHintIconsManager().removeUnsavedHintIcon();
        hasCompleted = true;
    }

    public boolean hasCompleted() {
        return hasCompleted;
    }

    public void setLogedIn() {
        hasLogedIn = true;
    }

    public boolean hasLogedIn() {
        return hasLogedIn;
    }

    public static void transferDonationRank(Player from, Player to) {
        if (from.isDonator()) {
            to.setDonator(from.isDonator());
        }
        if (from.isExtremeDonator()) {
            to.setExtremeDonator(from.isExtremeDonator());
        }
        if (from.isLegendaryDonator()) {
            to.setLegendaryDonator(from.isLegendaryDonator());
        }
        if (from.isSupremeDonator()) {
            to.setSupremeDonator(from.isSupremeDonator());
        }
        if (from.isUltimateDonator()) {
            to.setUltimateDonator(from.isUltimateDonator());
        }
        if (from.isMasterDonator()) {
            to.setMasterDonator(from.isMasterDonator());
        }
    }

    /**
     * Handles a donation made.
     *
     * @param price The donation price.
     */
    public void handleDonation(final int price, final String perk) {

        try {
            final BufferedWriter bf = new BufferedWriter(new FileWriter("data/playersaves/logs/donationLogs.txt", true));
            bf.write("[Player: " + getDisplayName() + ", on " + DateFormat.getDateTimeInstance().format(new Date()) + "]: has donated: " + price + "$ for " + perk + ".");
            bf.newLine();
            bf.flush();
            bf.close();
        } catch (final IOException e) {
            Logger.getGlobal().error("Failed saving Donation Logs...", e);
        }
        processContributerProgress(price);
        getInterfaceManager().closeChatBoxInterface();
        setMoneySpent(getMoneySpent() + price);

        if (price >= 20 && price < 50 && !isDonator()) {
            setDonator(true);
            final int left = 59 - getMoneySpent();
            getDialogueManager().startDialogue("SimpleMessage", "You've been promoted to <img=15>Bronze member; " + "Only " + left + "$ left to donate until next rank!");
            return;
        }

        if (price >= 50 && price < 100 && !isExtremeDonator()) {
            setDonator(true);
            setExtremeDonator(true);
            RewardBox.giveRewardBox(this, RewardBox.Rank.SILVER);
            final int left = 100 - getMoneySpent();
            getDialogueManager().startDialogue("SimpleMessage", "You've been promoted to <img=16>Silver member; " + "Only " + left + "$ left to donate until next rank!");
            return;
        }

        if (price >= 100 && price < 250 && !isLegendaryDonator()) {
            setDonator(true);
            setExtremeDonator(true);
            setLegendaryDonator(true);
            RewardBox.giveRewardBox(this, RewardBox.Rank.GOLD);
            final int left = 250 - getMoneySpent();
            getDialogueManager().startDialogue("SimpleMessage", "You've been promoted to <img=19>Gold member; " + "Only " + left + "$ left to donate until next rank!");
            return;
        }

        if (price >= 250 && price < 500 && !isSupremeDonator()) {
            setDonator(true);
            setExtremeDonator(true);
            setLegendaryDonator(true);
            setSupremeDonator(true);
            RewardBox.giveRewardBox(this, RewardBox.Rank.PLATINUM);
            final int left = 500 - getMoneySpent();
            getDialogueManager().startDialogue("SimpleMessage", "You've been promoted to <img=20>Platinum member; " + "Only " + left + "$ left to donate until next rank!");
            return;
        }

        if (price >= 500 && !isUltimateDonator()) {
            setDonator(true);
            setExtremeDonator(true);
            setLegendaryDonator(true);
            setSupremeDonator(true);
            setUltimateDonator(true);
            RewardBox.giveRewardBox(this, RewardBox.Rank.DIAMOND);
            getDialogueManager().startDialogue("SimpleMessage", "You've been promoted to <img=21>Diamond member; " + "This is the highest rank available!");
            return;
        }

        if (getMoneySpent() < 20 && !isDonator()) {
            final int left = 20 - getMoneySpent();
            getDialogueManager().startDialogue("SimpleMessage", "Only " + left + "$ left " + "to donate until promotion to Bronze member!");
            return;
        }
        if (getMoneySpent() >= 20 && !isDonator()) {
            setDonator(true);
            final int left = 50 - getMoneySpent();
            getDialogueManager().startDialogue("SimpleMessage", "You've been promoted to <img=15>Bronze member; " + "Only " + left + "$ left to donate until next rank!");
            return;
        }
        if (getMoneySpent() >= 50 && !isExtremeDonator()) {
            setExtremeDonator(true);
            RewardBox.giveRewardBox(this, RewardBox.Rank.SILVER);
            final int left = 100 - getMoneySpent();
            getDialogueManager().startDialogue("SimpleMessage", "You've been promoted to <img=16>Silver member; " + "Only " + left + "$ left to donate until next rank!");
            return;
        }
        if (getMoneySpent() >= 100 && !isLegendaryDonator()) {
            setLegendaryDonator(true);
            RewardBox.giveRewardBox(this, RewardBox.Rank.GOLD);
            final int left = 250 - getMoneySpent();
            getDialogueManager().startDialogue("SimpleMessage", "You've been promoted to <img=19>Gold member; " + "Only " + left + "$ left to donate until next rank!");
            return;
        }
        if (getMoneySpent() >= 250 && !isSupremeDonator()) {
            setSupremeDonator(true);
            RewardBox.giveRewardBox(this, RewardBox.Rank.PLATINUM);
            final int left = 500 - getMoneySpent();
            getDialogueManager().startDialogue("SimpleMessage", "You've been promoted to <img=20>Platinum member; " + "Only " + left + "$ left to donate until next rank!");
            return;
        }
        if (isDiamondDonor() && !isUltimateDonator()) {
            setUltimateDonator(true);
            RewardBox.giveRewardBox(this, RewardBox.Rank.DIAMOND);
            final int left = 1000 - getMoneySpent();
            getDialogueManager().startDialogue("SimpleMessage", "You've been promoted to <img=21>Diamond member; " + "Only " + left + "$ left to donate until next rank!");
            return;
        }
        if (getMoneySpent() >= 1000 && !isMasterDonator()) {
            setMasterDonator(true);
            RewardBox.giveRewardBox(this, RewardBox.Rank.MASTER);
            getDialogueManager().startDialogue("SimpleMessage", "You've been promoted to <img=25>Master member; " + "This is the highest rank available!");
            return;
        }
    }

    public boolean isBronzeDonor() {
        return moneySpent >= 20;
    }

    public boolean isSilverDonor() {
        return moneySpent >= 50;
    }

    public boolean isGoldDonor() {
        return moneySpent >= 100;
    }

    public boolean isPlatinumDonor() {
        return moneySpent >= 250;
    }

    public boolean isDiamondDonor() {
        return moneySpent >= 500;
    }

    public boolean isMasterDonor() {
        return moneySpent >= 1000;
    }

    public int getMoneySpent() {
        return moneySpent;
    }

    public void setMoneySpent(final int money) {
        moneySpent = money;
    }

    public void addMoneySpent(final int money) {
        moneySpent += money;
    }

    public int getLoyaltyMoney() {
        return loyaltyMoney;
    }

    public void addLoyaltyMoney(final int money) {
        moneySpent += money;
        loyaltyMoney += money;
    }

    public void resetLoyaltyMoney() {
        moneySpent = moneySpent - loyaltyMoney;
        loyaltyMoney = 0;
    }

    public void addDailyTasksCompleted() {
        dailyTasksCompleted += 1;
    }

    /**
     * Used to take coins.
     *
     * @param amount the Amount to take.
     * @return if The money has been taken.
     */
    public boolean takeMoney(int amount) {
        if (!hasMoney(amount)) {
            return false;
        }
        if (amount < 0) {
            return false;
        }
        final int inPouch = getMoneyPouch().getTotal();
        final int inInventory = getInventory().getAmountOf(995);
        if (inPouch >= amount) {
            getMoneyPouch().removeMoneyMisc(amount);
            return true;
        }
        if (inInventory >= amount) {
            getInventory().deleteItem(new Item(995, amount));
            return true;
        }
        if (inPouch + inInventory >= amount) {
            amount = amount - inPouch;
            getMoneyPouch().removeMoneyMisc(inPouch);
            getInventory().deleteItem(new Item(995, amount));
            return true;
        }
        return false;
    }

    /**
     * Used to add coins.
     *
     * @param amount the Amount to add.
     * @return if The money has been added.
     */
    public void addMoney(int amount) {
        if (money + amount < 0) {
            final int amountPouch = Integer.MAX_VALUE - money;
            amount = amount - amountPouch;
            getMoneyPouch().addMoney(amountPouch, false);
            if (getInventory().hasFreeSlots() || getInventory().containsItem(995, 1)) {
                final int has = getInventory().getAmountOf(995);
                if (has + amount < 0) {
                    final int amountAdd = Integer.MAX_VALUE - has;
                    final int toDrop = amount - amountAdd;
                    if (amountAdd > 0) {
                        getInventory().addItem(995, amountAdd);
                    }
                    World.addGroundItem(new Item(995, toDrop), new WorldTile(this), this, true, 60);
                    sendMessage(Colors.RED + Utils.getFormattedNumber(toDrop) + " coins have been dropped due to insufficient coin inventory space.");
                    return;
                }
                getInventory().addItem(995, amount);
                return;
            }
            sendMessage(Colors.RED + Utils.getFormattedNumber(amount) + " coins have been dropped due to insufficient coin inventory space.");
            World.addGroundItem(new Item(995, amount), new WorldTile(this), this, true, 60);
            return;
        }
        getMoneyPouch().addMoney(amount, false);
    }

    /**
     * Used for checking if the player has money.
     *
     * @param amount the Amount to check for.
     * @return if the player has the required amount either in their money pouch or
     * their inventory.
     */
    public boolean hasMoney(final int amount) {
        final int money = getInventory().getAmountOf(995) + getMoneyPouch().getTotal();
        return money >= amount;
    }

    /**
     * Used for removing money when the player has more than {@code amount}. Will
     * not modify anything if the player doesn't have enough money.
     * <p>
     * Returns {@code true} if money was removed.
     */
    public boolean removeMoney(int amount) {
        int invCoins = getInventory().getAmountOf(995);
        int pouchCoins = getMoneyPouch().getTotal();
        if (invCoins + pouchCoins >= amount) {
            if (pouchCoins >= amount) {
                getMoneyPouch().removeAmount(amount);
            } else {
                amount -= getMoneyPouchValue();
                getMoneyPouch().setTotal(0);
                inventory.deleteCoins(amount);
            }
            return true;
        }
        return false;
    }

    public VarBitManager getVarBitManager() {
        // P2/P4: VarBitManager is a stateless delegate over getVarsManager(), so
        // creating it lazily is safe; legacy logins still get it from LoginManager.init.
        if (VBM == null)
            VBM = new VarBitManager(this);
        return VBM;
    }

    public FarmingManager getFarmingManager() {
        return farmingManager;
    }

    public void grantLodestones() {
        lodestone = new boolean[]{true, true, true, true, true, true, true, true, true, true, true, true, true, true, true};
        hasLodestones = true;
    }

    public void activateLodeStone(final WorldObject object, final Player p) {
        lock(5);
        WorldTasksManager.schedule(new WorldTask() {
            int count = 0;

            @Override
            public void run() {
                if (count == 0) {
                    getPackets().sendCameraPos(Cutscene.getX(p, p.getX() - 6), Cutscene.getY(p, p.getY()), 3000);
                    getPackets().sendCameraLook(Cutscene.getX(p, object.getX()), Cutscene.getY(p, object.getY()), 50);
                    getPackets().sendGraphics(new Graphics(3019), object);
                }
                if (count == 2) {
                    getPackets().sendResetCamera();
                    lodestone[object.getId() - 69827] = true;
                    refreshLodestoneNetwork();
                }
                if (count == 3) {
                    unlock();
                    stop();
                }
                count++;
            }
        }, 0, 1);
    }

    public GrandExchangeManager getGEManager() {
        return geManager;
    }

    /**
     * Gets the XP mode.
     *
     * @return the XP mode.
     */
    public String getXPMode() {
        if (isGroupIronman()) {
            return "Group Ironman";
        }
        if (isLegendary()) {
            return "Legendary";
        }
        if (isExpert()) {
            return "Expert";
        }
        if (isNovice()) {
            return "Novice";
        }
        if (isIntermediate()) {
            return "Intermediate";
        }
        if (isIronMan()) {
            return "Legendary Ironman";
        }
        if (isNoviceIronMan()) {
            return "Novice Ironman";
        }
        if (isIntermediateIronMan()) {
            return "Intermediate Ironman";
        }
        if (isExpertIronMan()) {
            return "Expert Ironman";
        }
        if (isHCIronMan()) {
            return "HC Ironman";
        }
        if (isKingOfTheSkillGameMode()) {
            return "King of the Skill";
        }
        return "HACKER";
    }

    /**
     * Gets the Drop rate.
     *
     * @return the Drop rate.
     */
    public double getDropRate() {
        if (isExpert()) {
            return Settings.VET_DROP;
        }
        if (isNovice()) {
            return Settings.INTERM_DROP;
        }
        if (isNoviceIronMan()) {
            return Settings.INTERM_DROP;
        }
        if (isIntermediate()) {
            return Settings.INTERMEDIATE_DROP;
        }
        if (isIntermediateIronMan()) {
            return Settings.INTERMEDIATE_DROP;
        }
        if (isExpertIronMan()) {
            return Settings.VET_DROP;
        }
        if (isIronMan()) {
            return Settings.IRONMAN_DROP;
        }
        if (isHCIronMan()) {
            return Settings.HCIRONMAN_DROP;
        }
        if (isKingOfTheSkillGameMode()) {
            return Settings.KING_OF_THE_SKILL_DROP;
        }
        return 1;
    }

    /**
     * Checks if the Player has the item.
     *
     * @param item The item to check.
     * @return if has item or not.
     */
    public boolean hasItem(final Item item) {
        if (getInventory().containsItem(item)) {
            return true;
        }
        if (getEquipment().getItemsContainer().contains(item)) {
            return true;
        }
        if (bank.containsItem(item)) {
            return true;
        }
        if (pet != null && pet.getItemId() == item.getId()) {
            return true;
        }
        if (PetPerkUtils.getObtainedPetByItem(this, item.getId()) != null)
            return true;
        if (Settings.SUPERLOG) {
            Logger.getGlobal().info("[HASITEM]: " + getDisplayName() + " banks size: " + getBanks().size());
        }
        return false;
    }

    public boolean hasItem(final int itemId) {
        return hasItem(new Item(itemId));
    }

    public boolean hasItems(final Item... items) {
        for (final Item item : items) {
            if (!hasItem(item)) {
                return false;
            }
        }
        return true;
    }

    public boolean hasItems(final int... items) {
        for (final int item : items) {
            if (!hasItem(new Item(item))) {
                return false;
            }
        }
        return true;
    }


    public void addItem(int id, int amount) {
        addItem(new Item(id, amount));
    }

    public boolean giveItem(int id, int amount) {
        return giveItem(new Item(id, amount));
    }

    public boolean giveItem(Item item) {
        if (!getInventory().hasFreeSlots() && !(item.getDefinitions().isStackable() && getInventory().containsOneItem(item.getId()))) {
            if (!getBank().hasBankSpace()) {
                return false;
            } else {
                if (item.getDefinitions().isNoted()) {
                    item.setId(item.getDefinitions().getCertId());
                }
                getBank().addItem(item.getId(), item.getAmount(), item.getCharges(), item.getAttributes(), true);
            }
        } else {
            getInventory().addItem(item);
        }
        return true;
    }

    public void catchDrop(Item item, Runnable dropAction) {


        dropAction.run();
        // Check if the chest is already full (32 items)
        //if (sophanemChestLoot.size() >= 28) {
          //  dropAction.run(); // Revert to normal drop logic
           // return;
        //}

        // Add to chest
        //addSophanemChestLoot(item.getId(), item.getAmount());
    }




    /**
     * Used to display players icon (if any).
     */
    public String getIcon() {
        int id = -1;
        if (getUsername().equalsIgnoreCase("xhybrid")) {
            id = Icons.ORANGE_CROWN;
        } //else if (getUsername().equalsIgnoreCase("kirita")) {
          //  id = Icons.CYAN_CROWN;
        //}

        else if (isFakeDev()) {
            id = Icons.DEVELOPER;
        } //else if (getRights() == 2) {
           // id = Icons.ADMINISTRATOR;
       // }

        else if (getRights() == 1) {
            id = Icons.MODERATOR;
        } else if (isSupport()) {
            id = Icons.SUPPORT;
        }
        return id != -1 ? ("<img=" + id + ">") : "";
    }

    public void addOresMined() {
        oresMined++;
    }

    public int getOresMined() {
        return oresMined;
    }

    public void addSmithingActions() {
        smithingActions++;
    }

    public int getSmithingActions() {
        return smithingActions;
    }

    public void addLogsChopped() {
        logsChopped++;
    }

    public int getLogsChopped() {
        return logsChopped;
    }

    public void addLogsBurned() {
        logsBurned++;
    }

    public int getLogsBurned() {
        return logsBurned;
    }

    public void addLapsRan() {
        lapsRan++;
    }

    public int getLapsRan() {
        return lapsRan;
    }

    public void addBonesOffered() {
        bonesOffered++;
    }

    public int getBonesOffered() {
        return bonesOffered;
    }

    public void addPotionsMade() {
        potionsMade++;
    }

    public int getPotionsMade() {
        return potionsMade;
    }

    public void addTimesStolen() {
        timesStolen++;
    }

    public int getTimesStolen() {
        return timesStolen;
    }

    public void addItemsMade() {
        itemsMade++;
    }

    public int getItemsMade() {
        return itemsMade;
    }

    public void addFurnitureCreated() {
        furnitureCreated++;
    }

    public int getFurnitureCreated() {
        return furnitureCreated;
    }

    public void addItemsFletched() {
        itemsFletched++;
    }

    public int getItemsFletched() {
        return itemsFletched;
    }

    public void addCreaturesCaught() {
        creaturesCaught++;
    }

    public int getCreaturesCaught() {
        return creaturesCaught;
    }

    public void addFishCaught(final int amount) {
        fishCaught += amount;
    }

    public int getFishCaught() {
        return fishCaught;
    }

    public void addFoodCooked() {
        foodCooked++;
    }

    public int getFoodCooked() {
        return foodCooked;
    }

    public void addProduceGathered() {
        produceGathered++;
    }

    public int getProduceGathered() {
        return produceGathered;
    }

    public int getPouchesMade() {
        return pouchesMade;
    }

    public void setPouchesMade(final int pouches) {
        pouchesMade = pouches;
    }

    public int getMemoriesCollected() {
        return memoriesCollected;
    }

    public void addMemoriesCollected() {
        memoriesCollected++;
    }

    public int getRunesMade() {
        return runesMade;
    }

    public boolean unlockedAllRecipes() {
        boolean unlocked = true;
        for (final boolean settings1 : meilyrShopSettings) {
            if (!settings1) {
                unlocked = false;
            }
        }
        for (final boolean settings2 : meilyrShopSettings2) {
            if (!settings2) {
                unlocked = false;
            }
        }
        return unlocked;
    }

    public void addRunesMade(final int runes) {
        runesMade += runes;
    }

    public boolean isMax() {
        return max;
    }

    public void setMax(final boolean max) {
        this.max = max;
    }

    public boolean isComp() {
        return comp;
    }

    public void setComp(final boolean comp) {
        this.comp = comp;
    }

    public boolean isCompT() {
        return compT;
    }

    public void setCompT(final boolean compT) {
        this.compT = compT;
    }

    public Toolbelt getToolBelt() {
        return toolBelt;
    }

    public ToolbeltNew getToolBeltNew() {
        return toolBeltNew;
    }

    public Titles getTitles() {
        return titles;
    }

    public void setCombinedCloaks() {
        combinedCloaks = true;
    }

    public boolean hasCombinedCloaks() {
        return combinedCloaks;
    }

    public void unlockGuthixTitle() {
        guthixTitle = true;
    }

    public boolean hasGuthixTitleUnlocked() {
        return guthixTitle;
    }

    public int getBoxesOpened() {
        return boxesOpened;
    }

    public void incrementBoxesOpened() {
        boxesOpened++;
    }

    public int getChestsOpened() {
        return chestsOpened;
    }

    public void incrementChestsOpened() {
        chestsOpened++;
    }

    public void announceKC(final String name, final int amount) {
        if (amount % 250 == 0 && amount != 0) {
            World.sendWorldMessage("<img=6><col=cc3737>News: " + getDisplayName() + " has reached " + amount + " " + Utils.formatPlayerNameForDisplay(name) + " kills milestone!", false);
            String img;
            if (amount < 500) {
                img = "bronze.png";
            } else if (amount < 1000) {
                img = "silver.png";
            } else {
                img = "gold.png";
            }
            QueryExecutor.submit(new News(this, "<b><img src=\"../bin/images/news/" + img + "\" width=17> " + getDisplayName() + " has reached " + amount + " " + Utils.formatPlayerNameForDisplay(name) + " kills milestone!"));
        }
    }

    public int getBossKillcount() {
        return evilTreeKc + getKillStatistics(3) + getKillStatistics(1) + getKillStatistics(2) + getKillStatistics(4) + getKillStatistics(6) + getKillStatistics(5) + getKillStatistics(7) + getKillStatistics(8) + getKillStatistics(10) + getKillStatistics(55) + getKillStatistics(54) + getKillStatistics(71) + getKillStatistics(79) + getKillStatistics(80) + getKillStatistics(81) + getKillStatistics(82) + getKillStatistics(83) + getKillStatistics(84) + getKillStatistics(85) + getKillStatistics(86) + getKillStatistics(87) + getKillStatistics(88) + getKillStatistics(90) + getKillStatistics(93) + getKillStatistics(111) + getKillStatistics(112) + getKillStatistics(113) + getKillStatistics(95) + getKillStatistics(96) + getKillStatistics(95);
    }

    public void setKillStats(final int id, final int amount) {
        killStats[id] = amount;
    }

    /**
     * Increases the statistics.
     *
     * @param name The NPC name.
     */
    public int increaseKillStatistics(final String name, final boolean add) {
        switch (name.toLowerCase()) {
            case "rock crab":
                if (add) {
                    killStats[0]++;
                }
                return getKillStatistics(0);
            case "general graardor":
                if (add) {
                    killStats[1]++;
                    announceKC(name, killStats[1]);
                }
                return getKillStatistics(1);
            case "k'ril tsutsaroth":
                if (add) {
                    killStats[2]++;
                    announceKC(name, killStats[2]);
                }
                return getKillStatistics(2);
            case "kree'arra":
                if (add) {
                    killStats[3]++;
                    announceKC(name, killStats[3]);
                }
                return getKillStatistics(3);
            case "commander zilyana":
                if (add) {
                    killStats[4]++;
                    announceKC(name, killStats[4]);
                }
                return getKillStatistics(4);
            case "nex":
                if (add) {
                    killStats[5]++;
                    announceKC(name, killStats[5]);
                }
                return getKillStatistics(5);
            case "corporeal beast":
                if (add) {
                    killStats[6]++;
                    announceKC(name, killStats[6]);
                }
                return getKillStatistics(6);
            case "queen black dragon":
                return getKillStatistics(7);
            case "king black dragon":
                if (add) {
                    killStats[8]++;
                    announceKC(name, killStats[8]);
                }
                return getKillStatistics(8);
            case "bork":
                if (add) {
                    killStats[9]++;
                    announceKC(name, killStats[9]);
                }
                return getKillStatistics(9);
            case "chaos elemental":
                if (add) {
                    killStats[10]++;
                    announceKC(name, killStats[10]);
                }
                return getKillStatistics(10);
            case "crawling Hand":
                if (add) {
                    killStats[11] += 1;
                }
                return getKillStatistics(11);
            case "abyssal demon":
                if (add) {
                    killStats[12] += 1;
                }
                return getKillStatistics(12);
            case "ice strykewyrm":
                if (add) {
                    killStats[13] += 1;
                }
                return getKillStatistics(13);
            case "jungle strykewyrm":
                if (add) {
                    killStats[14] += 1;
                }
                return getKillStatistics(14);
            case "desert strykewyrm":
                if (add) {
                    killStats[15] += 1;
                }
                return getKillStatistics(15);
            case "nechryael":
                if (add) {
                    killStats[16] += 1;
                }
                return getKillStatistics(16);
            case "aberrant spectre":
                if (add) {
                    killStats[17] += 1;
                }
                return getKillStatistics(17);
            case "hellhound":
                if (add) {
                    killStats[18] += 1;
                }
                return getKillStatistics(18);
            case "mature grotworm":
                if (add) {
                    killStats[19] += 1;
                }
                return getKillStatistics(19);
            case "tztok-jad":
                if (add) {
                    killStats[20] += 1;
                    announceKC(name, killStats[20]);
                }
                return getKillStatistics(20);
            case "greater demon":
                if (add) {
                    killStats[21] += 1;
                }
                return getKillStatistics(21);
            case "mutated jadinko baby":
                if (add) {
                    killStats[22] += 1;
                }
                return getKillStatistics(22);
            case "mutated jadinko male":
                if (add) {
                    killStats[23] += 1;
                }
                return getKillStatistics(23);
            case "mutated jadinko guard":
                if (add) {
                    killStats[24] += 1;
                }
                return getKillStatistics(24);
            case "blue dragon":
                if (add) {
                    killStats[25] += 1;
                }
                return getKillStatistics(25);
            case "iron dragon":
                if (add) {
                    killStats[26] += 1;
                }
                return getKillStatistics(26);
            case "steel dragon":
                if (add) {
                    killStats[27] += 1;
                }
                return getKillStatistics(27);
            case "frost dragon":
                if (add) {
                    killStats[28] += 1;
                }
                return getKillStatistics(28);
            case "glacor":
                if (add) {
                    killStats[29] += 1;
                    announceKC(name, killStats[29]);
                }
                return getKillStatistics(29);
            case "infernal mage":
                if (add) {
                    killStats[30] += 1;
                }
                return getKillStatistics(30);
            case "ganodermic beast":
                if (add) {
                    killStats[31] += 1;
                }
                return getKillStatistics(31);
            case "gargoyle":
                if (add) {
                    killStats[32] += 1;
                }
                return getKillStatistics(32);
            case "jelly":
                if (add) {
                    killStats[33] += 1;
                }
                return getKillStatistics(33);
            case "dark beast":
                if (add) {
                    killStats[34] += 1;
                }
                return getKillStatistics(34);
            case "bloodveld":
                if (add) {
                    killStats[35] += 1;
                }
                return getKillStatistics(35);
            case "black guard":
                if (add) {
                    killStats[36] += 1;
                }
                return getKillStatistics(36);
            case "chaos dwarf hand cannoneer":
                if (add) {
                    killStats[37] += 1;
                }
                return getKillStatistics(37);
            case "chaos dwogre":
                if (add) {
                    killStats[38] += 1;
                }
                return getKillStatistics(38);
            case "pyrefiend":
                if (add) {
                    killStats[39] += 1;
                }
                return getKillStatistics(39);
            case "cockatrice":
                if (add) {
                    killStats[40] += 1;
                }
                return getKillStatistics(40);
            case "brutal green dragon":
            case "green dragon":
                if (add) {
                    killStats[41] += 1;
                }
                return getKillStatistics(41);
            case "fungal rodent":
                if (add) {
                    killStats[42] += 1;
                }
                return getKillStatistics(42);
            case "grifolaroo":
                if (add) {
                    killStats[43] += 1;
                }
                return getKillStatistics(43);
            case "grifolapine":
                if (add) {
                    killStats[44] += 1;
                }
                return getKillStatistics(44);
            case "mithril dragon":
                if (add) {
                    killStats[45] += 1;
                }
                return getKillStatistics(45);
            case "bronze dragon":
                if (add) {
                    killStats[46] += 1;
                }
                return getKillStatistics(46);
            case "moss giant":
                if (add) {
                    killStats[47] += 1;
                }
                return getKillStatistics(47);
            case "fire giant":
                if (add) {
                    killStats[48] += 1;
                }
                return getKillStatistics(48);
            case "hill giant":
                if (add) {
                    killStats[49] += 1;
                }
                return getKillStatistics(49);
            case "turoth":
                if (add) {
                    killStats[50] += 1;
                }
                return getKillStatistics(50);
            case "basilisk":
                if (add) {
                    killStats[51] += 1;
                }
                return getKillStatistics(51);
            case "kurask":
                if (add) {
                    killStats[52] += 1;
                }
                return getKillStatistics(52);
            case "black demon":
                if (add) {
                    killStats[53] += 1;
                }
                return getKillStatistics(53);
            case "kalphite queen":
                if (add) {
                    killStats[54] += 1;
                    announceKC(name, killStats[54]);
                }
                return getKillStatistics(54);
            case "tormented demon":
                if (add) {
                    killStats[55] += 1;
                    announceKC(name, killStats[55]);
                }
                return getKillStatistics(55);
            case "baby blue dragon":
                if (add) {
                    killStats[56] += 1;
                }
                return getKillStatistics(56);
            case "lesser demon":
                if (add) {
                    killStats[57] += 1;
                }
                return getKillStatistics(57);
            case "skeleton":
                if (add) {
                    killStats[58] += 1;
                }
                return getKillStatistics(58);
            case "man":
            case "farmer":
            case "woman":
                if (add) {
                    killStats[59] += 1;
                }
                return getKillStatistics(59);
            case "waterfiend":
                if (add) {
                    killStats[60] += 1;
                }
                return getKillStatistics(60);
            case "banshee":
                if (add) {
                    killStats[61] += 1;
                }
                return getKillStatistics(61);
            case "dog":
            case "terror dog":
            case "wild dog":
                if (add) {
                    killStats[62] += 1;
                }
                return getKillStatistics(62);
            case "cave crawler":
                if (add) {
                    killStats[63] += 1;
                }
                return getKillStatistics(63);
            case "black dragon":
                if (add) {
                    killStats[64] += 1;
                }
                return getKillStatistics(64);
            case "chaos druid":
                if (add) {
                    killStats[65] += 1;
                }
                return getKillStatistics(65);
            case "black knight":
                if (add) {
                    killStats[66] += 1;
                }
                return getKillStatistics(66);
            case "barrelchest":
                if (add) {
                    killStats[67] += 1;
                }
                return getKillStatistics(67);
            case "dagannoth supreme":
                if (add) {
                    killStats[68] += 1;
                    announceKC(name, killStats[68]);
                }
                return getKillStatistics(68);
            case "dagannoth prime":
                if (add) {
                    killStats[69] += 1;
                    announceKC(name, killStats[69]);
                }
                return getKillStatistics(69);
            case "dagannoth rex":
                if (add) {
                    killStats[70] += 1;
                    announceKC(name, killStats[70]);
                }
                return getKillStatistics(70);
            case "araxxor":
                if (add) {
                    killStats[71] += 1;
                    announceKC(name, killStats[71]);
                }
                return getKillStatistics(71);
            case "vampyre":
                if (add) {
                    killStats[72] += 1;
                }
                return getKillStatistics(72);
            case "werewolf":
                if (add) {
                    killStats[73] += 1;
                }
                return getKillStatistics(73);
            case "goblin":
            case "hobgoblin":
                if (add) {
                    killStats[74] += 1;
                }
                return getKillStatistics(74);
            case "imp":
                if (add) {
                    killStats[75] += 1;
                }
                return getKillStatistics(75);
            case "icefiend":
                if (add) {
                    killStats[76] += 1;
                }
                return getKillStatistics(76);
            case "ogre":
                if (add) {
                    killStats[77] += 1;
                }
                return getKillStatistics(77);
            case "cyclops":
                if (add) {
                    killStats[78] += 1;
                }
                return getKillStatistics(78);
            case "rorarius":
                if (add) {
                    killStats[79] += 1;
                    // announceKC(name, killStats[79]);
                }
                return getKillStatistics(79);
            case "gladius":
                if (add) {
                    killStats[80] += 1;
                    // announceKC(name, killStats[80]);
                }
                return getKillStatistics(80);
            case "capsarius":
                if (add) {
                    killStats[81] += 1;
                    // announceKC(name, killStats[81]);
                }
                return getKillStatistics(81);
            case "scutarius":
                if (add) {
                    killStats[82] += 1;
                    // announceKC(name, killStats[82]);
                }
                return getKillStatistics(82);
            case "legio primus":
                if (add) {
                    killStats[83] += 1;
                    announceKC(name, killStats[83]);
                }
                return getKillStatistics(83);
            case "legio secundus":
                if (add) {
                    killStats[84] += 1;
                    announceKC(name, killStats[84]);
                }
                return getKillStatistics(84);
            case "legio tertius":
                if (add) {
                    killStats[85] += 1;
                    announceKC(name, killStats[85]);
                }
                return getKillStatistics(85);
            case "legio quartus":
                if (add) {
                    killStats[86] += 1;
                    announceKC(name, killStats[86]);
                }
                return getKillStatistics(86);
            case "legio quintus":
                if (add) {
                    killStats[87] += 1;
                    announceKC(name, killStats[87]);
                }
                return getKillStatistics(87);
            case "legio sextus":
                if (add) {
                    killStats[88] += 1;
                    announceKC(name, killStats[88]);
                }
                return getKillStatistics(88);
            case "giant mole":
                if (add) {
                    killStats[89] += 1;
                    announceKC(name, killStats[89]);
                }
                return getKillStatistics(89);
            case "kalphite king":
                if (add) {
                    killStats[90] += 1;
                    announceKC(name, killStats[90]);
                }
                return getKillStatistics(90);
            case "ork":
                if (add) {
                    killStats[91] += 1;
                }
                return getKillStatistics(91);
            case "aviansie":
                if (add) {
                    killStats[92] += 1;
                }
                return getKillStatistics(92);
            case "vorago":
                if (add) {
                    killStats[93] += 1;
                    announceKC(name, killStats[93]);
                }
                return getKillStatistics(93);
            case "adamant dragon":
                if (add) {
                    killStats[94] += 1;
                }
                return getKillStatistics(94);
            case "rune dragon":
                if (add) {
                    killStats[95] += 1;
                }
                return getKillStatistics(95);
            case "edimmu":
                if (add) {
                    killStats[96] += 1;
                }
                return getKillStatistics(96);
            case "lava strykewyrm":
                if (add) {
                    killStats[97] += 1;
                }
                return getKillStatistics(97);

            case "airut":
                if (add) {
                    killStats[98] += 1;
                }
                return getKillStatistics(98);

            case "celestial dragon":
                if (add) {
                    killStats[99] += 1;
                }
                return getKillStatistics(99);

            case "automaton guardian":
                if (add) {
                    killStats[100] += 1;
                }
                return getKillStatistics(100);

            case "automaton tracer":
                if (add) {
                    killStats[101] += 1;
                }
                return getKillStatistics(101);

            case "automaton generator":
                if (add) {
                    killStats[102] += 1;
                }
                return getKillStatistics(102);

            case "kal'gerion demon":
                if (add) {
                    killStats[103] += 1;
                }
                return getKillStatistics(103);

            case "blood nihil":
                if (add) {
                    killStats[104] += 1;
                }
                return getKillStatistics(104);

            case "ice nihil":
                if (add) {
                    killStats[105] += 1;
                }
                return getKillStatistics(105);

            case "shadow nihil":
                if (add) {
                    killStats[106] += 1;
                }
                return getKillStatistics(106);

            case "smoke nihil":
                if (add) {
                    killStats[107] += 1;
                }
                return getKillStatistics(107);

            case "camel warrior":
                if (add) {
                    killStats[108] += 1;
                }
                return getKillStatistics(108);

            case "acheron mammoth":
                if (add) {
                    killStats[109] += 1;
                }
                return getKillStatistics(109);

            case "ripper demon":
                if (add) {
                    killStats[110] += 1;
                }
                return getKillStatistics(110);

            case "wildywyrm":
            case "large mound":
                if (add) {
                    killStats[111]++;
                    announceKC(name, killStats[111]);
                }
                return getKillStatistics(111);
            case "voragoHardMode":
                if (add) {
                    killStats[112]++;
                    announceKC(name, killStats[112]);
                }
                return getKillStatistics(112);
            case "helwyr":
                if (add) {
                    killStats[113]++;
                    announceKC(name, killStats[113]);
                }
                return getKillStatistics(113);
            case "helwyr(cm)":
                if (add) {
                    killStats[114]++;
                    announceKC(name, killStats[114]);
                }
                return getKillStatistics(114);
            case "vindicta":
                if (add) {
                    killStats[115]++;
                    announceKC(name, killStats[115]);
                }
                return getKillStatistics(115);
            case "dragonstone dragon":
                if (add) {
                    killStats[116]++;
                }
                return getKillStatistics(116);
            case "onyx dragon":
                if (add) {
                    killStats[117]++;
                }
                return getKillStatistics(117);
            case "hydrix dragon":
                if (add) {
                    killStats[118]++;
                }
                return getKillStatistics(118);
            case "revenant":
                if (add) {
                    killStats[119]++;
                }
                return getKillStatistics(119);
            case "vindicta(cm)":
                if (add) {
                    killStats[120]++;
                    announceKC(name, killStats[120]);
                }
                return getKillStatistics(120);
            case "twin furies":
                if (add) {
                    killStats[121]++;
                    announceKC(name, killStats[121]);
                }
                return getKillStatistics(121);
            case "twin furies'(cm)":
                if (add) {
                    killStats[122]++;
                    announceKC(name, killStats[122]);
                }
                return getKillStatistics(122);
            case "gregorovic":
                if (add) {
                    killStats[123]++;
                    announceKC(name, killStats[123]);
                }
                return getKillStatistics(123);
            case "gregorovic(cm)":
                if (add) {
                    killStats[124]++;
                    announceKC(name, killStats[124]);
                }
                return getKillStatistics(124);
            case "har-aken":
                if (add) {
                    killStats[125]++;
                    announceKC(name, killStats[125]);
                }
                return getKillStatistics(125);
            case "red dragon":
                if (add) {
                    killStats[126]++;
                }
                return getKillStatistics(126);
            case "aquanite":
                if (add) {
                    killStats[127]++;
                }
                return getKillStatistics(127);
            case "crystal shapeshifter":
                if (add) {
                    killStats[128]++;
                }
                return getKillStatistics(128);
            case "bladed muspah":
                if (add) {
                    killStats[129]++;
                }
                return getKillStatistics(129);
            case "throwing muspah":
                if (add) {
                    killStats[130]++;
                }
                return getKillStatistics(130);
            case "force muspah":
                if (add) {
                    killStats[131]++;
                }
                return getKillStatistics(131);
            case "corpse spider":
                if (add) {
                    killStats[132]++;
                }
                return getKillStatistics(132);
            case "telos":
                if (add) {
                    killStats[133]++;
                }
                return getKillStatistics(133);
            case "Nex: Angel of Death":
            case "nex: angel of death":
                if (add) {
                    killStats[134]++;
                }
                return getKillStatistics(134);
            case "rise of the six":
                if (add) {
                    killStats[135]++;
                }
                return getKillStatistics(135);
            case "the magister":
                if (add) {
                    killStats[136]++;
                }
                return getKillStatistics(136);
            case "fight caves":
                if (add) {
                    killStats[137]++;
                }
                return getKillStatistics(137);
            case "the sanctum guardian":
                if (add) {
                    killStats[138]++;
                }
                return getKillStatistics(138);
            case "masuta the ascended":
                if (add) {
                    killStats[139]++;
                }
                return getKillStatistics(139);
            case "seiryu the azure serpent":
                if (add) {
                    killStats[140]++;
                }
                return getKillStatistics(140);
            case "crassian scout":
                if (add) {
                    killStats[141]++;
                }
                return getKillStatistics(141);
            case "crassian scuttler":
                if (add) {
                    killStats[142]++;
                }
                return getKillStatistics(142);
            case "crassian warrior":
                if (add) {
                    killStats[143]++;
                }
                return getKillStatistics(143);
            case "cloaked zealot":
                if (add) {
                    killStats[144]++;
                }
                return getKillStatistics(144);
            case "sea horror":
                if (add) {
                    killStats[145]++;
                }
                return getKillStatistics(145);
            case "zombie":
                if (add) {
                    killStats[146]++;
                }
                return getKillStatistics(146);
            case "armoured zombie":
                if (add) {
                    killStats[147]++;
                }
                return getKillStatistics(147);
            case "solak":
                if (add) {
                    killStats[148]++;
                }
                return getKillStatistics(148);
            case "vinecrawler":
                if (add) {
                    killStats[149]++;
                }
                return getKillStatistics(149);
            case "bulbous crawler":
                if (add) {
                    killStats[150]++;
                }
                return getKillStatistics(150);
            case "moss golem":
                if (add) {
                    killStats[151]++;
                }
                return getKillStatistics(151);
        }

        return -1;
    }

    public void openTeleportInterface() {
        if (isUsingTeleportInterface()) {
            getTeleportInterface().sendInterface();
        } else {
            getDialogueManager().startDialogue(new AllTeleportsD());
        }
    }

    /**
     * Gets the kill statistics.
     *
     * @param i The NPC id.
     * @return the Statistic.
     */
    public int getKillStatistics(final int i) {
        return killStats[i];
    }

    public int getDungeoneeringTokens() {
        return dungTokens;
    }

    public void setDungeoneeringTokens(final int tokens) {
        dungTokens = tokens;
    }

    public void setAnguish(final boolean ang) {
        anguish = ang;
    }

    public boolean hasAnguishActivated() {
        return anguish;
    }

    public void setTorment(final boolean torm) {
        torment = torm;
    }

    public boolean hasTormentActivated() {
        return torment;
    }

    public void setAugury(final boolean aug) {
        augury = aug;
    }

    public boolean hasAuguryActivated() {
        return augury;
    }

    public void setRenewal(final boolean ren) {
        renewal = ren;
    }

    public boolean hasRenewalActivated() {
        return renewal;
    }

    public void setRigour(final boolean rig) {
        rigour = rig;
    }

    public boolean hasRigourActivated() {
        return rigour;
    }

    public void setEfficiency(final boolean eff) {
        efficiency = eff;
    }

    public boolean hasEfficiencyActivated() {
        return efficiency;
    }

    public void setLife(final boolean life) {
        this.life = life;
    }

    public boolean hasLifeActivated() {
        return life;
    }

    public void setCleansing(final boolean cleanse) {
        cleansing = cleanse;
    }

    public boolean hasCleansingActivated() {
        return cleansing;
    }

    /**
     * "Double EXP" is really the exp boost the vote books give.
     *
     * @return
     */
    public boolean hasBonusEXP() {
        return getTimeLeft() > 1;
    }

    public long getTimeLeft() {
        return doubleXpTimer / 100;
    }

    private static transient final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("h:mm a");

    public String getTimeLeftString() {
        if (doubleXpTimer > 0) {
            LocalDateTime laterTime = LocalDateTime.now().plusMinutes(getTimeLeft());
            return Colors.GREEN + dateFormatter.format(laterTime);
        }
        return Colors.RED + "No Boost Active";
    }

    public long getBonusXpTimer() {
        return doubleXpTimer;
    }

    public void setBonusXpTimer(final long timer) {
        doubleXpTimer = timer;
    }

    public void addSuperAntiFire(final long time) {
        superAntiFire = time + Utils.currentTimeMillis();
    }

    public long getSuperAntiFire() {
        return superAntiFire;
    }

    public int getZeals() {
        return zeals;
    }

    public void setZeals(final int zeal) {
        zeals = zeal;
    }

    public long getKaramDelay() {
        return karamDelay;
    }

    public void addKaramDelay(final long time) {
        karamDelay = time + Utils.currentTimeMillis();
    }

    public House getHouse() {
        return house;
    }

    public void getPartner() {
        sendMessage("Your Slayer partner is: " + getSlayerPartner() + ".");
    }

    public String getSlayerPartner() {
        return slayerPartner;
    }

    public void setSlayerPartner(final String partner) {
        slayerPartner = partner;
    }

    public String getSlayerHost() {
        return slayerHost;
    }

    public void setSlayerHost(final String host) {
        slayerHost = host;
    }

    public String getSlayerInvite() {
        return slayerInvite;
    }

    public void setSlayerInvite(final String invite) {
        slayerInvite = invite;
    }

    public int getReaperPoints() {
        return ReaperPoints;
    }

    public void setReaperPoints(final int ReaperPoints) {
        this.ReaperPoints = ReaperPoints;
        updateSlayerCounterInformation();
    }

    public void incrementTotalReaperContractsCompleted() {
        totalcontract++;
    }

    /**
     * increments the total kills from all reaper contracts, not the current
     * contract
     **/
    public void incrementTotalReaperContractKills() {
        totalkills++;
    }

    public void addReaperPoints(int amount) {
        ReaperPoints += amount;
    }

    public int getTotalKills() {
        return totalkills;
    }

    public void setTotalKills(final int totalkills) {
        this.totalkills = totalkills;
    }

    public int getTotalContract() {
        return totalcontract;
    }

    public void setTotalContract(final int totalcontract) {
        this.totalcontract = totalcontract;
    }

    public int getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public void setLoyaltyPoints(final int lps) {
        loyaltyPoints = lps;
        auraManager.refreshLoyaltyPoints();
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(final int i) {
        times = i;
    }

    public LoyaltyManager getLoyaltyManager() {
        return loyaltyManager;
    }

    public boolean hasHWeenEmotes() {
        return halloweenEmotes;
    }

    public boolean hasChristmasEmotes() {
        return christmasEmotes;
    }

    public boolean hasModRights() {
        return rights >= 1;
    }

    public boolean hasAdminRights() {
        return rights >= 2;
    }

    public boolean hasEasterEmotes() {
        return easterEmotes;
    }

    public boolean hasThanksGivingEmotes() {
        return thanksGiving;
    }

    public void unlockHWeenEmotes() {
        sendMessage(Colors.RED + "You have unlocked all of the Halloween season Emotes.");
        getEmotesManager().refreshListConfigs();
        halloweenEmotes = true;
    }

    public void unlockChristmasEmotes() {
        sendMessage(Colors.RED + "You have unlocked all of the Christmas season Emotes.");
        getEmotesManager().refreshListConfigs();
        christmasEmotes = true;
    }

    public void unlockEasterEmotes() {
        sendMessage(Colors.RED + "You have unlocked all of the Easter season Emotes.");
        getEmotesManager().refreshListConfigs();
        easterEmotes = true;
    }

    public void unlockThanksGivingEmotes() {
        sendMessage(Colors.RED + "You have unlocked all of the Thanks giving season Emotes.");
        getEmotesManager().refreshListConfigs();
        thanksGiving = true;
    }

    public int getVotes() {
        return votes;
    }

    public void setVotes(final int votes) {
        this.votes = votes;
    }

    public boolean isLegendaryDonator() {
        return legendaryDonator;
    }

    public void setLegendaryDonator(final boolean legendary) {
        legendaryDonator = legendary;
    }

    public boolean isSupremeDonator() {
        return supremeDonator;
    }

    public void setSupremeDonator(final boolean supreme) {
        supremeDonator = supreme;
    }

    public boolean isUltimateDonator() {
        return ultimateDonator;
    }

    public void setUltimateDonator(final boolean ultimate) {
        ultimateDonator = ultimate;
    }

    private boolean masterDonator;
    public boolean hasLootedROTS;
    public boolean[] herbicideSettings;
    public boolean[] bonecrusherSettings;
    public boolean[] meilyrShopSettings;
    public boolean[] meilyrShopSettings2;
    /**
     * The {@link Ectofuntus} tokens that are yet to be claimed.
     */
    public int tokensUnclaimed;

    /**
     * The bones that are grinded.
     */
    public boolean grindedBones;

    /**
     * The type of bone to use.
     */
    public int boneType;
    public boolean pyramidReward;

    public void setMasterDonator(final boolean master) {
        masterDonator = master;
    }

    public PerkManager getPerkManager() {
        return perkManager;
    }

    public int getTriviaPoints() {
        return triviaPoints;
    }

    public void setTriviaPoints(final int triviaPts) {
        triviaPoints = triviaPts;
    }

    public long getTotalPlayTime() {
        return totalPlayTime;
    }

    public void setTotalPlayTime(final long amount) {
        totalPlayTime = amount;
    }

    public long getRecordedPlayTime() {
        return recordedPlayTime;
    }

    public void setRecordedPlayTime(final long amount) {
        recordedPlayTime = amount;
    }

    public boolean isCantWalk() {
        return cantWalk;
    }

    public void setCantWalk(final boolean cantWalk) {
        this.cantWalk = cantWalk;
    }

    public long getLastStarSprite() {
        return lastStarSprite;
    }

    public void setLastStarSprite(final long lastStarSprite) {
        this.lastStarSprite = lastStarSprite;
    }

    public boolean isFoundShootingStar() {
        return foundShootingStar;
    }

    public void setFoundShootingStar() {
        foundShootingStar = true;
    }

    public int getStarsFound() {
        return starsFound;
    }

    public void incrementStarsFound() {
        starsFound++;
    }

    public String getLastBossInstanceKey() {
        return lastBossInstanceKey;
    }

    public void setLastBossInstanceKey(final String lastBossInstanceKey) {
        this.lastBossInstanceKey = lastBossInstanceKey;
    }

    public InstanceSettings getLastBossInstanceSettings() {
        return lastBossInstanceSettings;
    }

    public void setLastBossInstanceSettings(final InstanceSettings lastBossInstanceSettings) {
        this.lastBossInstanceSettings = lastBossInstanceSettings;
    }

    public int getCompletedClues() {
        return completedClues;
    }

    public void incrementCompletedClues() {
        completedClues++;
    }

    public SquealOfFortune getSquealOfFortune() {
        return squealOfFortune;
    }

    public boolean[] getPrayerBook() {
        return prayerBook;
    }

    public boolean isAcceptingAid() {
        return acceptAid;
    }

    public boolean isFilteringProfanity() {
        return profanityFilter;
    }

    public void switchAcceptAid() {
        acceptAid = !acceptAid;
        refreshAcceptAid();
    }

    public void switchProfanityFilter() {
        profanityFilter = !profanityFilter;
        refreshProfanityFilter();
    }

    public void refreshAcceptAid() {
        getPackets().sendConfig(427, acceptAid ? 1 : 0);
    }

    public void refreshProfanityFilter() {
        getPackets().sendGlobalConfig(2834, profanityFilter ? 1 : 0);
    }

    public byte getFrozenKeyCharges() {
        return frozenKeyCharges;
    }

    public void setFrozenKeyCharges(final byte charges) {
        frozenKeyCharges = charges;
    }

    public boolean containsOneItem(final int... itemIds) {
        if (getInventory().containsOneItem(itemIds)) {
            return true;
        }
        if (getEquipment().containsOneItem(itemIds)) {
            return true;
        }
        final Familiar familiar = getFamiliar();
        return familiar != null && ((familiar.getBob() != null && familiar.getBob().containsOneItem(itemIds) || familiar.isFinished()));
    }

    public TreasureTrails getTreasureTrails() {
        return treasureTrails;
    }

    /**
     * Gets the players total time played.
     *
     * @return the play time.
     */
    public long getTimePlayed() {
        return getTotalPlayTime() + (isAFK() ? Utils.currentTimeMillis() : getRecordedPlayTime());
    }

    public boolean[] getBoons() {
        return boons;
    }

    public void setBoons(final boolean[] boons) {
        this.boons = boons;
    }

    public boolean getBoon(final int index) {
        return boons[index];
    }

    public long getDFSDelay() {
        final Long delay = (Long) getTemporaryAttributtes().get("dfs_delay");
        if (delay == null) {
            return 0;
        }
        return delay;
    }

    /**
     * Dragonfire special.
     */
    public void setDFSDelay(final long delay) {
        getTemporaryAttributtes().put("dfs_delay", delay + Utils.currentTimeMillis());
        getTemporaryAttributtes().remove("dfs_shield_active");
    }

    public boolean isEdgevilleHome() {
        return edgeville;
    }

    public boolean isMarketHome() {
        return market;
    }

    public boolean isMemberZoneHome() {
        return dZone;
    }

    public boolean isPrifddinasHome() {
        return prifddinas;
    }

    /**
     * Gets the @this home tile.
     *
     * @return The WorldTile.
     */
    public WorldTile getHomeTile() {
        return homeLocation;
    }

    public void addAirRunesMade(final int amount) {
        air += amount;
    }

    public void addMindRunesMade(final int amount) {
        mind += amount;
    }

    public void addWaterRunesMade(final int amount) {
        water += amount;
    }

    public void addEarthRunesMade(final int amount) {
        earth += amount;
    }

    public void addFireRunesMade(final int amount) {
        fire += amount;
    }

    public void addBodyRunesMade(final int amount) {
        body += amount;
    }

    public void addCosmicRunesMade(final int amount) {
        cosmic += amount;
    }

    public void addChaosRunesMade(final int amount) {
        chaos += amount;
    }

    public void addNatureRunesMade(final int amount) {
        nature += amount;
    }

    public void addLawRunesMade(final int amount) {
        law += amount;
    }

    public void incrementDeathRunesMade(final int amount) {
        death += amount;
    }

    public void addBloodRunesMade(final int amount) {
        blood += amount;
    }

    public void addAstralRunesMade(final int amount) {
        astral += amount;
    }

    public void addSoulRunesMade(final int amount) {
        soul += amount;
    }

    public int getAirRunesMade() {
        return air;
    }

    public int getMindRunesMade() {
        return mind;
    }

    public int getWaterRunesMade() {
        return water;
    }

    public int getEarthRunesMade() {
        return earth;
    }

    public int getFireRunesMade() {
        return fire;
    }

    public int getBodyRunesMade() {
        return body;
    }

    public int getCosmicRunesMade() {
        return cosmic;
    }

    public int getChaosRunesMade() {
        return chaos;
    }

    public int getNatureRunesMade() {
        return nature;
    }

    public int getLawRunesMade() {
        return law;
    }

    public int getDeathRunesMade() {
        return death;
    }

    public int getBloodRunesMade() {
        return blood;
    }

    public int getAstralRunesMade() {
        return astral;
    }

    public int getSoulRunesMade() {
        return soul;
    }

    public ContractHandler getCHandler() {
        return cHandler;
    }

    public boolean isSupport() {
        return support;
    }

    public void setSupport(final boolean support) {
        this.support = support;
    }

    public void setReceivedCracker() {
        receivedCracker = true;
    }

    public boolean hasReceivedCracker() {
        return receivedCracker;
    }

    public void addSerenStonesMined() {
        serenStonesMined++;
    }

    public byte getSerenStonesMined() {
        return serenStonesMined;
    }

    public void addHefinLaps() {
        hefinLaps++;
    }

    public short getHefinLaps() {
        return hefinLaps;
    }

    /**
     * Checks if the Player has access to Prifddinas.
     *
     * @return true if has access.
     */
    public boolean hasAccessToPrifddinas() {
        return getSkills().getTotalLevel(this) >= 2250 || getPerkManager().hasPerkActive(DonationPerk.ELF__S_FRIEND) || isDeveloper();
    }

    /**
     * Gets the total Drop Player's drop wealth.
     *
     * @return total wealth as Integer.
     */
    public int getDropWealth() {
        final ArrayList<Item> containedItems = new ArrayList<Item>();
        for (int i = 0; i < 14; i++) {
            final Item item = inventory.getItem(i);
            if (item != null) {
                containedItems.add(item);
            }
        }
        for (int i = 0; i < 28; i++) {
            final Item item = inventory.getItem(i);
            if (item != null) {
                containedItems.add(item);
            }
        }
        if (containedItems.isEmpty()) {
            return 0;
        }
        int keptAmount = 3;
        if (hasSkull()) {
            keptAmount = 0;
        }
        if (prayer.isProtectingItem()) {
            keptAmount++;
        }
        final ArrayList<Item> keptItems = new ArrayList<Item>();
        Item lastItem = new Item(1, 1);
        for (int i = 0; i < keptAmount; i++) {
            for (final Item item : containedItems) {
                final int price = GrandExchange.getPrice(item.getId());
                if (price >= GrandExchange.getPrice(lastItem.getId())) {
                    lastItem = item;
                }
            }
            keptItems.add(lastItem);
            containedItems.remove(lastItem);
            lastItem = new Item(1, 1);
        }

        int riskAmount = 0;
        for (final Item item : containedItems) {
            riskAmount += (GrandExchange.getPrice(item.getId()) * item.getAmount());
        }
        return riskAmount;
    }

    /**
     * Checks the highest total wealth.
     *
     * @param killed The opponent.
     * @return highest total wealth as Integer.
     */
    public int checkHighestKill(final Player killed) {
        if (killed != null) {
            final int riskAmount = killed.getDropWealth();
            final String riskAmount2 = Utils.moneyToString(riskAmount);
            if (riskAmount > highestKill) {
                highestKill = riskAmount;
                sendMessage("You have a new highest drop kill! Your opponent dropped " + riskAmount2 + " worth of items!");
            } else {
                sendMessage("Your opponent dropped " + riskAmount2 + " worth of items.");
            }
            return riskAmount;
        }
        return 0;
    }

    public long getLastBork() {
        return lastBork;
    }

    public void setLastBork(final long lastBork) {
        this.lastBork = lastBork;
    }

    public CosmeticOverrides getOverrides() {
        return overrides;
    }

    public void addChroniclesOffered(final int chronicles) {
        chroniclesOffered += chronicles;
    }

    public int getChroniclesOffered() {
        return chroniclesOffered;
    }

    public int getTaskPoints() {
        return taskPoints;
    }

    public void setTaskPoints(final int taskPoints) {
        this.taskPoints = taskPoints;
    }

    /**
     * Checks if the player is AFK.
     *
     * @return if Player is AFK.
     */
    public boolean isAFK() {
        // P4 guard: a native 947 player is never AFK. The 25-minute timer is only
        // reset by 910 packet decoding (increaseAFKTimer), which the native input
        // path does not run, so without this guard ActionManager.process would
        // force-stop every action and processEntity would teleport the player to
        // the AFK island 25 minutes after login regardless of activity.
        if (isNative950())
            return false;
        return Utils.currentTimeMillis() >= afkTimer;
    }

    private WorldTile beforeAfkLocation;

    /**
     * Resets the AFK timer.
     */
    public void increaseAFKTimer() {
        if (isAFK() && isActive() && accountPin.hasEnteredPin()) {
            unlock();
            setRecordedPlayTime(Utils.currentTimeMillis());
            closeInterfaces();
            setNextWorldTile(beforeAfkLocation);
            if (beforeAfkLocation == Settings.RESPAWN_PLAYER_LOCATION)
                sm(Colors.ORANGE + "We were not able to send you back to your previous location since it was instanced.");
        }
        if (!isAFK() && lastNotification != -1 && dialogueManager.getDialogue() != null && dialogueManager.getDialogue() instanceof AfkD) {
            dialogueManager.finishDialogue();
        }

        afkTimer = Utils.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(25, TimeUnit.MINUTES);
        lastNotification = -1;
    }

    /**
     * Total player weight.
     *
     * @return the weight as a Double Integer.
     */
    public double getWeight() {
        return inventory.getInventoryWeight() + equipment.getEquipmentWeight();
    }

    public boolean isFirstTime() {
        return firstTime;
    }

    public void setFirstTime(final boolean firstTime) {
        this.firstTime = firstTime;
    }

    public AnimationOverrides getAnimations() {
        return animations;
    }

    /*
     * XMAS EVENT controllers etc
     */

    /**
     * Duel Arena
     */
    public DuelRules getDuelRules() {
        return duelRules;
    }

    public void setLastDuelRules(final DuelRules duelRules) {
        this.duelRules = duelRules;
    }

    public com.rs.game.player.content.group.Group getGroup() {
        return group;
    }

    public PlayerOwnedPort getPorts() {
        return ports;
    }

    public XmasEvent getXmas() {
        return xmas;
    }

    public Multiboss getMulti() {
        return multiboss;
    }

    /**
     * Player interaction restriction check.
     */
    public boolean canTrade(final Player p2) {
        if (isOwner() || (p2 != null && p2.isOwner())) {
            return true;
        }
        if (!hasCompleted() || (p2 != null && !p2.hasCompleted())) {
            return false;
        }
        if (isGroupIronman()) {
            if (p2 != null && !canGimInteractWith(p2)) {
                sendGimCantInteract();
                return false;
            }
        } else if (isIronMan() || isHCIronMan() || isNoviceIronMan() || isExpertIronMan() || isIntermediateIronMan()) {
            sendMessage("You can't do this on an ironman account.");
            return false;
        }

        if (isKingOfTheSkillGameMode()) {
            kingOfTheSkillGameModeHandler.sendVagueNoMessageToPlayer();
            return false;
        }
        if (p2 != null && p2.isGroupIronman()) {
            if (!p2.canGimInteractWith(this)) {
                sendMessage(p2.getDisplayName() + " is on an ironman account and can't do this.");
                return false;
            }
        } else if (p2 != null && (p2.isIronMan() || p2.isHCIronMan() || p2.isNoviceIronMan() || p2.isExpertIronMan() || p2.isIntermediateIronMan())) {
            sendMessage(p2.getDisplayName() + " is on an ironman account and can't do this.");
            return false;
        }
        if (p2 != null && p2.isKingOfTheSkillGameMode()) {
            kingOfTheSkillGameModeHandler.sendVagueNoMessageToPlayerAboutOtherPlayer(p2);
            return false;
        }
        //if (getSkills().getTotalLevel(this) < 150 && !isATypeOfDonator() && !isOwner() && !isDev()) {
            //sendMessage("You need to be a donator or have at least a total level of 150 to do this.");
          //  return false;
        //}
        if (p2 != null && !p2.isBot() && p2.getSkills().getTotalLevel(p2) < 150 && !p2.isATypeOfDonator() && !p2.isOwner() && !p2.isDev()) {
            sendMessage(p2.getDisplayName() + " needs to be a donator or have at least a total level of 150 to do this.");
            return false;
        }
        if (itemTransaction != null && itemTransaction.getTarget() != null && itemTransaction.getTarget().getItemTransaction() != null && itemTransaction.getTarget() != p2) {
            itemTransaction.getTarget().getItemTransaction().closeTransaction(CloseTransactionStage.CANCEL);
            SerializableFilesManager.savePlayer(this);
            if (realChannel != null) {
                realChannel.close();
            }
            if (p2 != null) {
                p2.sendMessage(Colors.RED + "Your trading partner has been disconnected.");
            }
            return false;
        }
        if (p2 != null && !p2.isBot() && p2.getInterfaceManager().containsScreenInter()) {
            sendMessage("The other player is busy.");
            return false;
        }
        /*
         * if (p2 != null && p2.getCurrentMac().equals(getCurrentMac())) {
         * sendMessage("You can not do this on the same computer."); return false; }
         */
        return !getUsername().equalsIgnoreCase("youtube") && !getUsername().equalsIgnoreCase("community") && !getUsername().equalsIgnoreCase("xanax");
    }

    public boolean canGamble(final Player p2) {
        if (isIronMan() || isHCIronMan() || isNoviceIronMan() || isExpertIronMan() || isIntermediateIronMan()) {
            sendMessage("You can't do this on an ironman account.");
            return false;
        }
        if (p2 != null && (p2.isIronMan() || p2.isHCIronMan() || p2.isNoviceIronMan() || p2.isExpertIronMan() || p2.isIntermediateIronMan())) {
            sendMessage(p2.getDisplayName() + " is on an ironman account and can't do this.");
            return false;
        }
        if (getSkills().getTotalLevel(this) < 50) {
            sendMessage("You need at least a total level of 50 to do this.");
            return false;
        }
        if (p2 != null && (p2.getSkills().getTotalLevel(p2) < 150)) {
            sendMessage(p2.getDisplayName() + " needs at least a total level of 150 to do this.");
            return false;
        }
        if (p2 != null && p2.getInterfaceManager().containsScreenInter()) {
            sendMessage("The other player is busy.");
            return false;
        }
        return true;
    }

    public int gWeekendBooks() {
        return weekendBooks;
    }

    public void sWeekendBooks(final int weekendBooks) {
        this.weekendBooks += weekendBooks;
    }

    public boolean inMembersZone() {
        return getX() > 4369 && getX() < 4495 && getY() > 5901 && getY() < 5931 && isDonator();
    }

    public boolean inPlatMembersZone() {
        return getX() > 3865 && getX() < 3908 && getY() > 6800 && getY() < 6850 && isSupremeDonator();
    }

    public boolean inDiamondMembersZone() {
        return getX() > 3533 && getX() < 3569 && getY() > 6029 && getY() < 6067 && isUltimateDonator();
    }

    public boolean inDiamondHunterArea() {
        return getX() > 1547 && getX() < 1575 && getY() > 4352 && getY() < 4382 && isUltimateDonator();
    }

    public boolean isMasterDonator() {
        return masterDonator;
    }

    /**
     * Gets the {@link Ectophial}.
     *
     * @return the ectophial
     */
    public Ectophial getEctophial() {
        return ectophial;
    }

    // protected SlayerInter slayerInter;

    /*
     * public SlayerInter getSlayerInter() { return slayerInter; }
     */

    public boolean hasFamiliar() {
        return familiar != null;
    }

    // FOG
    protected int fogRating;
    protected transient int fogCharge;

    public int fogRating() {
        return fogRating;
    }

    public void fogRating(final int f) {
        fogRating = f;
    }

    public int fogCharge() {
        return fogCharge;
    }

    public void fogCharge(final int f) {
        fogCharge = f;
    }

    /*
     * Vorago
     */

    private boolean spokenToVorago;

    public boolean hasSpokenToVorago() {
        return spokenToVorago;
    }

    public void setSpokenToVorago(final boolean spokenToVorago) {
        this.spokenToVorago = spokenToVorago;
    }

    public boolean[] mauledWeeksNM;

    public boolean[] mauledWeeksHM;

    public void setHasMauledWeekNM(final int rotation) {
        mauledWeeksNM[rotation] = true;
    }

    public void setHasMauledWeekHM(final int rotation) {
        mauledWeeksHM[rotation] = true;
    }

    public boolean hasBombiChance() {
        for (final boolean mauled : mauledWeeksHM) {
            if (!mauled) {
                return false;
            }
        }
        return true;
    }

    public boolean[] getMauledWeeksNM() {
        return mauledWeeksNM;
    }

    public boolean[] getMauledWeeksHM() {
        return mauledWeeksHM;
    }

    public boolean isCanStartHardModeVorago(final boolean sendMessage) {
        if (rights == 2) {
            return true;
        }
        final List<String> neededWeeks = new ArrayList<String>();
        for (int i = 0; i < mauledWeeksNM.length; i++) {
            if (!mauledWeeksNM[i]) {
                neededWeeks.add(Settings.VORAGO_ROTATION_NAMES[i]);
            }
        }
        if (!neededWeeks.isEmpty() && sendMessage) {
            getPackets().sendGameMessage("<col=ff0000>You must maul these rotations before you can start hard mode instance:");
            for (final String neededWeek : neededWeeks) {
                if (neededWeek == null) {
                    continue;
                }
                getPackets().sendGameMessage(neededWeek);
            }
        }
        return neededWeeks.isEmpty();
    }

    private boolean showSearchOption;
    private boolean filterLocked;

    public boolean isShowSearchOption() {
        return showSearchOption;
    }

    public void setShowSearchOption(final boolean showSearchOption) {
        this.showSearchOption = showSearchOption;
    }

    public boolean isFilterLocked() {
        return filterLocked;
    }

    public void setFilterLocked(final boolean filterLocked) {
        this.filterLocked = filterLocked;
    }

    public ArrayList<Integer> unlockedCostumesIds;

    public boolean isLockedCostume(final int itemId) {
        return !unlockedCostumesIds.contains(itemId);
    }

    public ArrayList<Integer> getUnlockedCostumesIds() {
        return unlockedCostumesIds;
    }

    private int ataraxiaCoins;

    public int getAtaraxiaCoins() {
        return ataraxiaCoins;
    }

    public void setAtaraxiaCoins(final int ataraxiaCoins) {
        this.ataraxiaCoins = ataraxiaCoins;
        refreshAtaraxiaCoins();
    }

    public void refreshAtaraxiaCoins() {
        getPackets().sendGlobalConfig(4659, ataraxiaCoins);
    }

    public void addAtaraxiaCoins(final int amount) {
        ataraxiaCoins += amount;
        refreshAtaraxiaCoins();
    }

    public int[] ArtisansWorkShopSupplies;

    public ArtisansWorkShop artisansWorkShop;
    private transient int previousKeyPressed;
    private transient long previousKeyValidity;
    private boolean legacyHitSplat;

    public ArtisansWorkShop getArtisansWorkShop() {
        return artisansWorkShop;
    }

    public void setPreviousKeyPressed(final int previousKeyPressed) {
        addPreviousKeyValidity();
        this.previousKeyPressed = previousKeyPressed;
    }

    /**
     * Required but will be removed as soon as I can modify client to check for key
     * combinations.
     */
    private void addPreviousKeyValidity() {
        previousKeyValidity = Utils.currentTimeMillis() + 600;
    }

    public int getPreviousKeyPressed() {
        if (previousKeyValidity < Utils.currentTimeMillis()) {
            return -1;
        }
        return previousKeyPressed;
    }

    public void setRealChannel(final io.netty.channel.Channel realChannel) {
        this.realChannel = realChannel;
    }

    public boolean isLobby() {
        return false;
    }

    public boolean isLegacyHitSplat() {
        return legacyHitSplat;
    }

    public void setLegacyHitSplat(final boolean legacyHitSplat) {
        this.legacyHitSplat = legacyHitSplat;
    }

    /**
     * Gets the currentRandomEventNPC.
     *
     * @return the currentRandomEventNPC
     */
    public NPC getCurrentRandomEventNPC() {
        return currentRandomEventNPC;
    }

    /**
     * Sets the currentRandomEventNPC.
     *
     * @param currentRandomEventNPC the currentRandomEventNPC to set
     */
    public void setCurrentRandomEventNPC(final NPC currentRandomEventNPC) {
        this.currentRandomEventNPC = currentRandomEventNPC;
    }

    /**
     * Gets the visWaxManager.
     *
     * @return the visWaxManager
     */
    public VisWaxManager getVisWaxManager() {
        return visWaxManager;
    }

    /**
     * Sets the visWaxManager.
     *
     * @param visWaxManager the visWaxManager to set
     */
    public void setVisWaxManager(final VisWaxManager visWaxManager) {
        this.visWaxManager = visWaxManager;
    }

    /**
     * Gets the timeToNextMysterybox.
     *
     * @return the timeToNextMysterybox
     */
    public long getTimeToNextMysterybox() {
        return timeToNextMysterybox;
    }

    /**
     * Sets the timeToNextMysterybox.
     *
     * @param timeToNextMysterybox the timeToNextMysterybox to set
     */
    public void setTimeToNextMysterybox(final long timeToNextMysterybox) {
        this.timeToNextMysterybox = timeToNextMysterybox;
    }

    /**
     * Gets the claimedDonationAwardBoxes.
     *
     * @return the claimedDonationAwardBoxes
     */
    public boolean[] getClaimedDonationAwardBoxes() {
        return claimedDonationAwardBoxes;
    }

    /**
     * Sets the claimedDonationAwardBoxes.
     *
     * @param claimedDonationAwardBoxes the claimedDonationAwardBoxes to set
     */
    public void setClaimedDonationAwardBoxes(final boolean[] claimedDonationAwardBoxes) {
        this.claimedDonationAwardBoxes = claimedDonationAwardBoxes;
    }

    public void startKingOfTheSkillGameMode() {
        kingOfTheSkillGameMode = true;
        kingOfTheSkillGameModeHandler = new KingOfTheSkillGameModeHandler(this);
    }

    public void endKingOfTheSkillGameMode() {
        kingOfTheSkillGameMode = false;
        kingOfTheSkillGameModeHandler = null;
    }

    /**
     * Had to make this method over here because a verifier class was having
     * serialization issues. Sorry SRP
     */
    public boolean hasExceededTwentyFourHourLimitDuringPlayTime() {
        int hoursPlayed = Utils.getHoursPlayed(getTimePlayed());
        return hoursPlayed >= 24;
    }

    public boolean hasExceededTwentyFourHourLimitOnLogin() {
        long time = getTimePlayed() + Utils.currentTimeMillis();
        int hoursPlayed = Utils.getHoursPlayed(time);
        return hoursPlayed >= 24;
    }

    /**
     * This method implies that king of the skill game mode has been started, but is
     * having it's session continued. Essentially, it is initializing the transient
     * variables.
     *
     * @todo think of a better name for this method
     */
    public void initializeKingOfTheSkillGameModeVariables() {
        kingOfTheSkillGameModeHandler = new KingOfTheSkillGameModeHandler(this);
    }

    public boolean isUsingTeleportInterface() {
        return usingTeleportInterface;
    }

    public void setUsingTeleportInterface(boolean usingTeleportInterface) {
        this.usingTeleportInterface = usingTeleportInterface;
    }

    public Achievements getAchievements() {
        return achievements;
    }

    public void setAchievements(Achievements achievements) {
        this.achievements = achievements;
    }

    public AchievementDifficulty getSelectedAchivementsTab() {
        return selectedAchivementsTab;
    }

    public void setSelectedAchivementsTab(AchievementDifficulty selectedAchivementsTab) {
        this.selectedAchivementsTab = selectedAchivementsTab;
    }

    public AchievementList getSelectedAchivement() {
        return selectedAchivement;
    }

    public void setSelectedAchivement(AchievementList selectedAchivement) {
        this.selectedAchivement = selectedAchivement;
    }

    public Cape getSelectedDistinctionCape() {
        return selectedDistinctionCape;
    }

    public void setSelectedDistinctionCape(Cape selectedDistinctionCape) {
        this.selectedDistinctionCape = selectedDistinctionCape;
    }

    public LootBeamManager getLootBeamManager() {
        return lootBeamManager;
    }

    public void setLootBeamManager(LootBeamManager lootBeamManager) {
        this.lootBeamManager = lootBeamManager;
    }

    public int getShopLastViewX() {
        return shopLastViewX;
    }

    public void setShopLastViewX(int shopLastViewX) {
        this.shopLastViewX = shopLastViewX;
    }

    public ActivityTimersManager getActivityTimersManager() {
        return activityTimersManager;
    }

    public void setActivityTimersManager(ActivityTimersManager activityTimersManager) {
        this.activityTimersManager = activityTimersManager;
    }

    private int telosEnrage;

    public int getTelosEnrage() {
        return telosEnrage;
    }

    public void setTelosEnrage(int telosEnrage) {
        this.telosEnrage = telosEnrage;
    }

    private int telosStreak;
    private int maxEnrage;

    public int getMaxEnrage() {
        return maxEnrage;
    }

    public void setMaxEnrage(int maxEnrage) {
        this.maxEnrage = maxEnrage;
    }

    public int getTelosStreak() {
        return telosStreak;
    }

    public void setTelosStreak(int telosStreak) {
        this.telosStreak = telosStreak;
    }

    private Item currentTelosReward;
    private List<Item> telosRewards;

    public List<Item> getTelosRewards() {
        if (telosRewards == null)
            telosRewards = new ArrayList<Item>();
        return telosRewards;
    }

    public Item getCurrentTelosReward() {
        return currentTelosReward;
    }

    public void setCurrentTelosReward(Item currentTelosReward) {
        this.currentTelosReward = currentTelosReward;
        refreshCurrentTelosReward();
    }

    private void refreshCurrentTelosReward() {
        getPackets().sendConfigByFile(32629, currentTelosReward != null ? 1 : 0, true);
    }

    public void openTelosTrove() {
        if (currentTelosReward == null)
            return;
        getTemporaryAttributtes().remove("ViewDrops");
        getTemporaryAttributtes().put("telosTrove", Boolean.TRUE);
        stopAll();
        addReward();
        getPackets().sendItems(99, getTelosRewards().toArray(new Item[getTelosRewards().size()]));
        interfaceManager.sendInterface(1284);
        getPackets().sendInterSetItemsOptionsScript(1284, 7, 99, 8, 3, "Examine");
        getPackets().sendUnlockIComponentOptionSlots(1284, 7, 0, 24, 0);
        getPackets().sendHideIComponent(1284, 10, true);
        getPackets().sendIComponentText(1284, 42, "Continue");
        setCloseInterfacesEvent(new Runnable() {

            @Override
            public void run() {
                if (getTemporaryAttributtes().remove("chooseOption") == null)
                    continueChallenge();
                getTemporaryAttributtes().remove("chooseOption");
                getTemporaryAttributtes().remove("telosTrove");
            }
        });
    }

    private void addReward() {
        if (currentTelosReward == null)
            return;
        if (!getTelosRewards().isEmpty())
            for (int i = 0; i < getTelosRewards().size(); i++) {
                if (getTelosRewards().get(i).getId() == currentTelosReward.getId()) {
                    getTelosRewards().get(i).setAmount(getTelosRewards().get(i).getAmount() + currentTelosReward.getAmount());
                    currentTelosReward = null;
                    break;
                }
            }
        if (currentTelosReward != null)
            getTelosRewards().add(currentTelosReward);
        currentTelosReward = null;
        refreshCurrentTelosReward();
    }

    public void continueChallenge() {
        if (getControlerManager().getControler() instanceof Barrows || getControlerManager().getControler() instanceof SophanemSlayerDungeon || getControlerManager().getControler() instanceof RiseOfTheSixController) {

            interfaceManager.closeScreenInterface();
            getPackets().sendGameMessage("You choose to abandon the chest.");

        } else {
            interfaceManager.closeScreenInterface();
            int r = Utils.random(5, 21);
            int previousEnrage = telosEnrage;
            telosEnrage = telosEnrage + r >= 4000 ? 4000 : telosEnrage + r;
            if (maxEnrage < telosEnrage)
                maxEnrage = telosEnrage;
            telosStreak = telosStreak + 1 >= 200 ? 200 : telosStreak + 1;
            if (previousEnrage == 4000)
                getPackets().sendGameMessage("You have reached the max enrage cap.");
            else
                getPackets().sendGameMessage("You choose to continue the challenge your new enrage is: " + telosEnrage + "%.");
            getTemporaryAttributtes().put("chooseOption", Boolean.TRUE);
            TelosEnrageRanks.checkRank(this);
        }
    }

    public void claimTelosRewards() {
        interfaceManager.closeScreenInterface();
        if (getTelosRewards().isEmpty()) {
            getPackets().sendGameMessage("You don't have any rewards to claim.");
        } else {
            for (Item item : getTelosRewards())
                getBank().addItem(item.getId(), item.getAmount(), 0, null, true);
            getTelosRewards().clear();
            telosEnrage = 0;
            telosStreak = 0;
            getPackets().sendGameMessage("Your rewards have been added to your bank.");
            getTemporaryAttributtes().put("chooseOption", Boolean.TRUE);
        }
    }

    public String theWarden() {
        String colour = getMaxEnrage() < 2000 ? "84D477" : getMaxEnrage() < 4000 ? "BEBEBE" : "FAB402";
        return "<col=" + colour + ">the Warden</col>";
    }

    public void setWasXpLocked(boolean wasXpLocked) {
        this.wasXpLocked = wasXpLocked;
    }

    public boolean wasXpLocked() {
        return wasXpLocked;
    }

    public boolean hasCoalBag() {
        return coal > 0 && inventory.containsOneItem(18339);
    }

    public PetPerkManager getPetPerkManager() {
        return petPerkManager;
    }

    public void setPetPerkManager(PetPerkManager petPerkManager) {
        this.petPerkManager = petPerkManager;
    }

    private int spiderBossEnrage;

    public int getSpiderBossEnrage() {
        if (spiderBossEnrage >= 300)
            spiderBossEnrage = 300;
        return spiderBossEnrage;
    }

    public void setSpiderBossEnrage(int spiderBossEnrage) {
        this.spiderBossEnrage = spiderBossEnrage;
    }

    @Override
    public void setNextAnimation(Animation animation) {
        if (!getControlerManager().canDoAnimation(animation))
            return;
        super.setNextAnimation(animation);
    }

    @Override
    public void setNextAnimationNoPriority(Animation animation) {
        if (!getControlerManager().canDoAnimation(animation))
            return;
        super.setNextAnimationNoPriority(animation);
    }

    public boolean isCoOpSkillingRequesting() {
        return coOpRequest != null && !coOpRequest.isExpired();
    }

    public long getTotalItemCount(int itemId) {
        return getTotalItemCount(new Item(itemId, 1));
    }

    public long getTotalItemCount(Item item) {
        long count = inventory.getAmountOf(item) + bank.getNumberOf(item);
        if (familiar != null && familiar.getBob() != null) {
            count += familiar.getBob().getBeastItems().getNumberOf(item);
        }
        if (priceCheckManager != null) {
            count += priceCheckManager.getPcInv().getNumberOf(item);
        }
        if (item.getId() == 995) {
            count += getMoneyPouchValue();
        }
        return count;
    }

    public Map<Integer, Integer> uniqueItems = new HashMap<>();

    public void addUniqueItem(int itemId) {
        if (!containsUniqueItem(itemId)) {
            getPackets().sendGameMessage("Unique items: <col=f00000>" + new Item(itemId).getName() + "</col> was added to the collection log!", true);
        }
        incrementUniqueItem(uniqueItems, itemId);// show me code where it sends colours in ur tab
    }

    public void resetUniqueItems() {
        uniqueItems.clear();
    }

    private void incrementUniqueItem(Map<Integer, Integer> quantity, Integer itemId) {
        uniqueItems.put(itemId, uniqueItems.getOrDefault(itemId, 0) + 1);
    }

    public void addMultipleItem(int itemId, int quant) {
        Item item = new Item(itemId);
        if (!containsUniqueItem(itemId)) {
            getPackets().sendGameMessage("Unique items: <col=f00000>" + item.getDefinitions().getName() + "</col> x<col=f00000>" + quant + "</col> was added to the collection log!", true);
        }
        incrementMultipleItem(uniqueItems, itemId, quant);
    }

    private void incrementMultipleItem(Map<Integer, Integer> quantity, Integer itemId, int quant) {
        uniqueItems.put(itemId, uniqueItems.getOrDefault(itemId, 0) + quant);
    }

    public boolean containsUniqueItem(int itemId) {
        return uniqueItems.containsKey(itemId);
    }

    public int getUniqueItemQuantity(int item) {
        return uniqueItems.getOrDefault(item, 0);
    }

    @Override
    public String toString() {
        return getDisplayName() + " (" + getUsername() + ") @ " + super.toString();
    }

    public DayOfWeekManager dayOfWeekManager;

    public DayOfWeekManager getDayOfWeekManager() {
        return dayOfWeekManager;
    }

    public RouteEvent getRouteEvent() {
        return routeEvent;
    }

    private long spearWallDelay;

    public boolean hasSpearWallEffect() {
        return spearWallDelay >= Utils.currentTimeMillis();
    }

    public void activateSpearWallEffect() {
        spearWallDelay = Utils.currentTimeMillis() + 5000;// 5sec
    }

    public int getHPReduction() {
        return getTemporaryAttributtes().get("HPReduction") != null ? (int) getTemporaryAttributtes().get("HPReduction") : 0;
    }

    public void addHPReduction(int value) {
        getTemporaryAttributtes().put("HPReduction", getHPReduction() + value > 89 ? 89 : getHPReduction() + value);
        refreshMaxHitpoints();
    }

    public void removeHPReduction(int value) {
        if (getHPReduction() - value <= 0)
            getTemporaryAttributtes().remove("HPReduction");
        else
            addHPReduction(-value);
        refreshMaxHitpoints();
    }

    public void refreshMaxHitpoints() {
        if (getHitpoints() < 2000 && getHitpoints() > getMaxHitpoints()) {
            setHitpoints(getMaxHitpoints() + this.getExtraHitPoints());
            this.setExtraHitPoints(0);
        }
        refreshHitPoints();
    }

    private List<ReaperPerks> reaperPerks;
    private boolean[] inactivePerks;

    public boolean reaperPerkUnlocked(ReaperPerks perk) {
        return reaperPerks.contains(perk);
    }

    public List<ReaperPerks> getReaperPerks() {
        return reaperPerks;
    }

    public void setReaperPerks(List<ReaperPerks> reaperPerks) {
        this.reaperPerks = reaperPerks;
    }

    private boolean skipTask, chooseTask;

    public boolean isSkipTask() {
        return skipTask;
    }

    public void setSkipTask(boolean skipTask) {
        this.skipTask = skipTask;
    }

    public boolean isChooseTask() {
        return chooseTask;
    }

    public void setChooseTask(boolean chooseTask) {
        this.chooseTask = chooseTask;
    }

    public boolean reaperPerkActivated(ReaperPerks perk) {
        if (!reaperPerkUnlocked(perk))
            return false;
        if (inactivePerks == null) {
            inactivePerks = new boolean[ReaperPerks.values().length];
        }
        if (inactivePerks.length != ReaperPerks.values().length) {
            boolean[] temp = new boolean[ReaperPerks.values().length];
            for (int i = 0; i < (temp.length > inactivePerks.length ? inactivePerks.length : temp.length); i++)
                temp[i] = inactivePerks[i];
            inactivePerks = temp;
        }
        return !inactivePerks[perk.ordinal()];
    }

    public void togglePerkActivation(int index) {
        inactivePerks[index] = !inactivePerks[index];
        if (inactivePerks[index] && index == ReaperPerks.REAPERS_CHOICE.ordinal())
            chooseTask = false;
        if (inactivePerks[index] && index == ReaperPerks.TAKE_TWO.ordinal())
            skipTask = false;
    }

    public boolean halfedTheMagisterKC;

    @Getter
    @Setter
    private InventionManager inventionManager;

    public boolean hasSkillCapePerk(int skillId) {
        int capeId = equipment.getCapeId();
        if (capeId == -1)
            return false;
        String name = ItemDefinitions.getItemDefinitions(capeId).getName().toLowerCase();
        /*
         * if (name.contains("max cape") || name.contains("completionist cape")) { for
         * (int perkId : maxCompCapePerks) { if (perkId == skillId) return true; } }
         */
        return name.contains(Skills.SKILL_NAME[skillId].toLowerCase() + " cape") || name.contains(Skills.SKILL_NAME[skillId].toLowerCase() + " master cape") || name.contains((skillId == Skills.WOODCUTTING ? "woodcut." : Skills.SKILL_NAME[skillId].toLowerCase()) + " cape (t)");
    }

    @Getter
    @Setter
    private ChargesManagerNew chargesManagerNew;

    private transient boolean pouchFilter;

    public void setPouchFilter(boolean pouchFilter) {
        this.pouchFilter = pouchFilter;
    }

    public boolean isPouchFilter() {
        return pouchFilter;
    }

    private boolean disableDisassembleHighValueWarning;

    public boolean hasDisableDisassembleHighValueWarning() {
        return disableDisassembleHighValueWarning;
    }

    public void toggleDisableDisassembleHighValueWarning() {
        this.disableDisassembleHighValueWarning = !disableDisassembleHighValueWarning;
    }

    @SuppressWarnings("unused")
    private boolean resetedImbuedGear, resetedImbuedGear2;

    public void resetImbuedGear() {
        if (resetedImbuedGear2)
            return;
        int amountGearsToGive = 0;
        for (int i = 0; i < getInventory().getItems().getItems().length; i++) {
            Item item = getInventory().getItems().getItems()[i];
            if (item == null || (CombinationData.isImbuedGear(item.getId()) == null && item.getChargesData() == null) || item.getInventionData() != null)
                continue;
            if (item.getChargesData() != null && CombinationData.isImbuedGear(item.getChargesData().getOrignalId()) == null)
                continue;
            CombinationData data = item.getChargesData() != null ? CombinationData.isImbuedGear(item.getChargesData().getOrignalId()) : CombinationData.isImbuedGear(item.getId());
            if (item.getChargesData() != null) {
                item.getChargesData().setOrignalId(data.getRequiredItems()[0]);
            } else {
                item.setId(data.getRequiredItems()[0]);
            }
            amountGearsToGive++;
        }
        inventory.refresh();
        for (int i = 0; i < getEquipment().getItems().getItems().length; i++) {
            Item item = getEquipment().getItems().getItems()[i];
            if (item == null || (CombinationData.isImbuedGear(item.getId()) == null && item.getChargesData() == null) || item.getInventionData() != null)
                continue;
            if (item.getChargesData() != null && CombinationData.isImbuedGear(item.getChargesData().getOrignalId()) == null)
                continue;
            CombinationData data = item.getChargesData() != null ? CombinationData.isImbuedGear(item.getChargesData().getOrignalId()) : CombinationData.isImbuedGear(item.getId());
            if (item.getChargesData() != null) {
                item.getChargesData().setOrignalId(data.getRequiredItems()[0]);
            } else {
                item.setId(data.getRequiredItems()[0]);
            }
            amountGearsToGive++;
        }
        equipment.refreshItemContainer();
        for (Bank bank : getBanks()) {
            if (bank == null)
                continue;
            for (int i = 0; i < bank.bankTabs.length; i++) {
                for (Item item : bank.bankTabs[i]) {
                    if (item == null || (CombinationData.isImbuedGear(item.getId()) == null && item.getChargesData() == null) || item.getInventionData() != null)
                        continue;
                    if (item.getChargesData() != null && CombinationData.isImbuedGear(item.getChargesData().getOrignalId()) == null)
                        continue;
                    CombinationData data = item.getChargesData() != null ? CombinationData.isImbuedGear(item.getChargesData().getOrignalId()) : CombinationData.isImbuedGear(item.getId());
                    if (item.getChargesData() != null) {
                        item.getChargesData().setOrignalId(data.getRequiredItems()[0]);
                        amountGearsToGive++;
                    } else {
                        item.setId(data.getRequiredItems()[0]);
                        amountGearsToGive += item.getAmount();
                    }
                }
            }
        }
        if (amountGearsToGive > 0) {
            getBank().addItem(new Item(41407, amountGearsToGive), true);
            getPackets().sendGameMessage("<col=ff0000>All of your imbued gear has been reset to its original item, and " + amountGearsToGive + " x Imbued gear has been added to your bank.");
            getPackets().sendGameMessage("<col=ff0000>You can replace those for 50 vote points or 20k chimes each at Xuan at home.");
        }
        resetedImbuedGear2 = true;
    }

    private boolean resetBarrows;

    public void resetOldBarrowsGear() {
        if (resetBarrows)
            return;
        for (int i = 0; i < getInventory().getItems().getItems().length; i++) {
            Item item = getInventory().getItems().getItems()[i];
            if (item == null || item.getDefinitions().getCSOpcode(88) == 0)
                continue;
            String name = item.getName().toLowerCase();
            int newItemId = RS3GeneralRequirementMap.getMap(item.getDefinitions().getCSOpcode(2281)).getIntValue(590);
            item.setId(newItemId);
            if (!name.contains("100")) {
                ChargesData data = this.getChargesManagerNew().initChargesData(item, false);
                int chargesLeft = (int) ((double) data.getMaxCharges() * (name.contains("75") ? 0.75 : name.contains("50") ? 0.5 : 0.25));
                data.setChargesLeft(chargesLeft);
            }
        }
        inventory.refresh();
        for (int i = 0; i < getEquipment().getItems().getItems().length; i++) {
            Item item = getEquipment().getItems().getItems()[i];
            if (item == null || item.getDefinitions().getCSOpcode(88) == 0)
                continue;
            String name = item.getName().toLowerCase();
            int newItemId = RS3GeneralRequirementMap.getMap(item.getDefinitions().getCSOpcode(2281)).getIntValue(590);
            item.setId(newItemId);
            if (!name.contains("100")) {
                ChargesData data = this.getChargesManagerNew().initChargesData(item, false);
                int chargesLeft = (int) ((double) data.getMaxCharges() * (name.contains("75") ? 0.75 : name.contains("50") ? 0.5 : 0.25));
                data.setChargesLeft(chargesLeft);
            }
        }
        equipment.refreshItemContainer();
        for (Bank bank : getBanks()) {
            if (bank == null)
                continue;
            for (int i = 0; i < bank.bankTabs.length; i++) {
                for (Item item : bank.bankTabs[i]) {
                    if (item == null || item.getDefinitions().getCSOpcode(88) == 0)
                        continue;
                    int newItemId = RS3GeneralRequirementMap.getMap(item.getDefinitions().getCSOpcode(2281)).getIntValue(590);
                    item.setId(newItemId);
                }
            }
        }
        resetBarrows = true;
    }

    private boolean resetAttributes;

    public void resetItemsAttributes() {
        if (resetAttributes)
            return;
        for (int i = 0; i < getInventory().getItems().getItems().length; i++) {
            Item item = getInventory().getItems().getItems()[i];
            if (item == null || item.getAttributes() == null)
                continue;
            Item t = new Item(item.getId());
            t.setAttributes(item.getAttributes());
            getInventory().set(i, t);
        }
        inventory.refresh();
        for (int i = 0; i < getEquipment().getItems().getItems().length; i++) {
            Item item = getEquipment().getItems().getItems()[i];
            if (item == null || item.getAttributes() == null)
                continue;
            Item t = new Item(item.getId());
            t.setAttributes(item.getAttributes());
            getEquipment().set(i, t);
        }
        equipment.refreshItemContainer();
        for (Bank bank : getBanks()) {
            if (bank == null)
                continue;
            for (int i = 0; i < bank.bankTabs.length; i++) {
                for (int j = 0; j < bank.bankTabs[i].length; j++) {
                    Item item = bank.bankTabs[i][j];
                    if (item == null || item.getAttributes() == null)
                        continue;
                    Item t = new Item(item.getId());
                    t.setAttributes(item.getAttributes());
                    bank.bankTabs[i][j] = t;
                }
            }
        }
        getBank().refreshItems();
        resetAttributes = true;
    }

    private boolean resetDupedItems;

    public void resetDupedItems() {
        if (resetDupedItems)
            return;
//        List<Item> allItems = new ArrayList<Item>();
//        for (int i = 0; i < getInventory().getItems().getItems().length; i++) {
//            Item item = getInventory().getItems().getItems()[i];
//            if (item == null || item.getAttributes() == null)
//                continue;
//            allItems.add(item);
//        }
//        for (int i = 0; i < getEquipment().getItems().getItems().length; i++) {
//            Item item = getEquipment().getItems().getItems()[i];
//            if (item == null || item.getAttributes() == null)
//                continue;
//            allItems.add(item);
//        }
//        for (Bank bank : getBanks()) {
//            if (bank == null)
//                continue;
//            for (int i = 0; i < bank.bankTabs.length; i++) {
//                for (int j = 0; j < bank.bankTabs[i].length; j++) {
//                    Item item = bank.bankTabs[i][j];
//                    if (item == null || item.getAttributes() == null)
//                        continue;
//                    allItems.add(item);
//                }
//            }
//        }
//        List<Integer> dupedItems = new ArrayList<Integer>();
//        for (Item a : allItems) {
//            int count = 0;
//            for (int i = 0; i < getInventory().getItems().getItems().length; i++) {
//                Item item = getInventory().getItems().getItems()[i];
//                if (item == null || !item.equals(a))
//                    continue;
//                count++;
//                if (count > 1)
//                    getInventory().set(i, null);
//            }
//            inventory.refresh();
//            for (int i = 0; i < getEquipment().getItems().getItems().length; i++) {
//                Item item = getEquipment().getItems().getItems()[i];
//                if (item == null || !item.equals(a))
//                    continue;
//                count++;
//                if (count > 1)
//                    getEquipment().set(i, null);
//            }
//            equipment.refreshItemContainer();
//            for (Bank bank : getBanks()) {
//                if (bank == null)
//                    continue;
//                for (int i = 0; i < bank.bankTabs.length; i++) {
//                    for (int j = 0; i < bank.bankTabs.length && j < bank.bankTabs[i].length; j++) {
//                        Item item = bank.bankTabs[i][j];
//                        if (item == null || !item.equals(a))
//                            continue;
//                        count++;
//                        if (count > 1)
//                            bank.removeItem(bank.getItemSlot(item), item.getAmount(), false, true);
//                    }
//                }
//            }
//            if (count > 1)
//                dupedItems.add(a.getId());
//        }
//        if (!dupedItems.isEmpty()) {
//            World.sendWorldMessage(getDisplayName() + " had duped items:", true);
//            World.sendWorldMessage(dupedItems.toString(), true);
//            System.out.println(getDisplayName() + " had duped items:" + dupedItems);
//        }
//        dupedItems.clear();
//        allItems.clear();
//        resetDupedItems = true;
    }

    private TreasureHunter treasureHunter;

    public void setTreasureHunter(TreasureHunter treasureHunter) {
        this.treasureHunter = treasureHunter;
    }

    public TreasureHunter getTreasureHunter() {
        return treasureHunter;
    }


    private boolean goldTrim99;

    public boolean isGoldTrim99() {
        return goldTrim99;
    }

    public void toggleGoldTrim99() {
        goldTrim99 = !goldTrim99;
        refreshGoldTrim99();
    }

    public void refreshGoldTrim99() {
        VBM.sendVarBit(19009, goldTrim99 ? 1 : 0);
    }

    private long freedomCooldown;

    public void setFreedomCooldown() {
        freedomCooldown = Utils.currentTimeMillis() + 30000;
    }

    public boolean hasFreedomActive() {
        return freedomCooldown != 0 && Utils.currentTimeMillis() < freedomCooldown && (freedomCooldown - Utils.currentTimeMillis() >= 24000);
    }

    public boolean canUseFreedom() {
        return freedomCooldown == 0 || Utils.currentTimeMillis() >= freedomCooldown;
    }

    public long getFreedomCooldown() {
        return freedomCooldown - Utils.currentTimeMillis();
    }
    private long codexCooldown;

    public boolean setCodexCooldown() {
        codexCooldown = Utils.currentTimeMillis() + 30000;
        player.sendMessage(Colors.RED + "cool)" +
                "!");
        return true;
    }

    public boolean hasCodexActive() {
        return codexCooldown != 0 && Utils.currentTimeMillis() < codexCooldown && (codexCooldown - Utils.currentTimeMillis() >= 24000);

    }

    public boolean canUseCodex() {
        return codexCooldown == 0 || Utils.currentTimeMillis() >= codexCooldown;
    }

    public long getCodexCooldown() {
        return codexCooldown - Utils.currentTimeMillis();
    }






    private boolean hideAuraGlow;

    public boolean isHideAuraGlow() {
        return hideAuraGlow;
    }

    public void toggleHideAuraGlow() {
        this.hideAuraGlow = !hideAuraGlow;
    }

    @Getter
    @Setter
    private long contributerTill;

    private int contributionAmount;

    public static final int CONTRIBUTER_STATUS_THRESSHOLD = 100;

    public boolean hasContributerStatus() {
        return contributerTill != 0 && contributerTill >= Utils.currentTimeMillis();
    }

    @SuppressWarnings("deprecation")
    public void makeContributerDays(int days) {
        if (contributerTill < Utils.currentTimeMillis())
            contributerTill = Utils.currentTimeMillis();
        Date date = new Date(contributerTill);
        date.setDate(date.getDate() + days);
        contributerTill = date.getTime();
    }

    public int getContributionAmount() {
        return contributionAmount;
    }

    public void setContributionAmount(int contributionAmount) {
        this.contributionAmount = contributionAmount;
    }

    @SuppressWarnings("deprecation")
    public String getContributerDaysLeftMessage() {
        return new Date(contributerTill).toGMTString() + ".";
    }

    public void processContributerProgress(int price) {
     /*   if (hasContributerStatus())
            return;
        this.contributionAmount += price;
        if (contributionAmount >= CONTRIBUTER_STATUS_THRESSHOLD) {
            makeContributerDays(30);
            getPackets().sendGameMessage("<col=00ff00>You have received contributor status for 30 days, it will expire " + getContributerDaysLeftMessage());
            contributionAmount = 0;
        }*/
    }

    public double experienceModifier() {
        if (getMoneySpent() >= 1_000) {
            return 1.30;
        }
        if (getMoneySpent() >= 500) {
            return HomeAreaHandler.playerIsAtHome(this) ? 1.30 : 1.20;
        }
        if (getMoneySpent() >= 250) {
            return HomeAreaHandler.playerIsAtHome(this) ? 1.20 : 1.10;
        }
        if (getMoneySpent() >= 20 && HomeAreaHandler.playerIsAtHome(this)) {
            return 1.10;
        }
        return 1.00;
    }

    @Getter
    @Setter
    private transient long decimationSpecDelay;

    public boolean hasDecimationEffect() {
        return decimationSpecDelay != 0 && decimationSpecDelay >= Utils.currentTimeMillis();
    }

    public void startDecimationEffect() {
        decimationSpecDelay = Utils.currentTimeMillis() + 10000;
    }

    public void resetDecimationEffect() {
        decimationSpecDelay = 0;
    }

    @Getter
    @Setter
    private EliteDungeonsManager eliteDungeonsManager;

    public boolean isInsideAnyDungParty() {
        return getDungeoneeringManager().getParty() != null || eliteDungeonsManager.getParty() != null;
    }

    public void sendChoosePartyTypeDialogue() {
        if (getDungeoneeringManager().getParty() != null || eliteDungeonsManager.getParty() != null) {
            getPackets().sendGameMessage("You can only have one party active at a time, either elite dung party or regular dung party.");
            return;
        }
        getDialogueManager().startDialogue(new Dialogue() {

            @Override
            public void start() {
                sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Form regular dung party.", "Form elite dung party", "Nevermind");
            }

            @Override
            public void run(int interfaceId, int componentId) {
                end();
                if (componentId == OPTION_1) {
                    player.getDungeoneeringManager().formParty();
                } else if (componentId == OPTION_2) {
                    player.getEliteDungeonsManager().formParty();
                }
            }

            @Override
            public void finish() {

            }
        });
    }

    public void openPartyInterface() {
        getInterfaceManager().openDungTab();
        getInterfaceManager().sendDungPartyInterface();
        Object oManager = getEliteDungeonsManager().getParty() != null ? getEliteDungeonsManager() : getDungeoneeringManager();
        if (oManager instanceof DungManager) {
            DungManager manager = (DungManager) oManager;
            manager.refreshFloor();
            manager.refreshCurrentProgress();
            manager.refreshPreviousProgress();
            manager.refreshComplexity();
            manager.refreshPartyDetailsComponents();
            manager.refreshPartyGuideModeComponent();
            manager.refreshNames();
        } else {
            EliteDungeonsManager manager = (EliteDungeonsManager) oManager;
            manager.refreshPartyDetailsComponents();
            manager.refreshNames();
        }
    }


    private long surgeEscapeCooldown;

    public void setSurgeEscapeCooldown() {


        surgeEscapeCooldown = Utils.currentTimeMillis() + 10000;
    }


    public long getSurgeEscapeCooldown() {




        return surgeEscapeCooldown - Utils.currentTimeMillis();

    }


    public boolean canUseSurgeEscape() {

        return surgeEscapeCooldown == 0 || Utils.currentTimeMillis() >= surgeEscapeCooldown;
    }


    public void addItem(final Item item) {
        addItem(item, true);
    }

    public void addItem(final Item item, boolean sendMessage) {
        int amountLeft = item.getAmount();
        int amountAddedToInv = 0;
        int amountAddedToBank = 0;
        int amountAddedToGround = 0;
        if (!item.getDefinitions().isStackable() && !item.getDefinitions().isNoted()) {
            for (int i = 0; i < item.getAmount(); i++) {
                if (item.getAttributes() == null ? (!getInventory().addItemToInventory(item.getId(), 1, false)) : (!getInventory().addItemToInventory(new Item(item.getId(), 1, item.getCharges()).setAttributes(item.getAttributes()), false)))
                    break;
                amountAddedToInv += 1;
                if (amountLeft - 1 <= 0) {
                    amountLeft = 0;
                    break;
                } else
                    amountLeft--;
            }
            if (amountLeft > 0) {
                Item bankItem = item.getAttributes() == null ? getBank().getItemIncludingPlaceHolders(item.getDefinitions().isNoted() ? item.getDefinitions().certId : item.getId()) : getBank().getItemIncludingPlaceHolders(item);
                if (bankItem == null) {
                    if (getBank().isFull()) {
                        for (int i = 0; i < amountLeft; i++) {
                            World.updateGroundItem(new Item(item.getId(), 1).setAttributes(item.getAttributes()), new WorldTile(this), this, 60, 0, false);
                            amountAddedToGround += 1;
                        }
                        if (sendMessage)
                            sendAddedItemMessage(item, amountAddedToInv, amountAddedToBank, amountAddedToGround);
                        return;
                    }
                    getBank().addItem(item.getDefinitions().isNoted() ? item.getDefinitions().certId : item.getId(), amountLeft, item.getCharges(), item.getAttributes(), true);
                    amountAddedToBank += amountLeft;
                    if (sendMessage)
                        sendAddedItemMessage(item, amountAddedToInv, amountAddedToBank, amountAddedToGround);
                    return;
                }
                int amountToBank = amountLeft;
                if (((long) bankItem.getAmount() + (long) amountToBank) > Integer.MAX_VALUE)
                    amountToBank = Integer.MAX_VALUE - bankItem.getAmount();
                amountLeft -= amountToBank;
                if (amountToBank > 0) {
                    getBank().addItem(item.getDefinitions().isNoted() ? item.getDefinitions().certId : item.getId(), amountToBank, item.getCharges(), item.getAttributes(), true);
                    amountAddedToBank += amountToBank;
                }
                if (amountLeft > 0)
                    for (int i = 0; i < amountLeft; i++) {
                        World.updateGroundItem(new Item(item.getId(), 1, item.getCharges()).setAttributes(item.getAttributes()), new WorldTile(this), this, 60, 0, false);
                        amountAddedToGround += 1;
                    }
            }
            if (sendMessage)
                sendAddedItemMessage(item, amountAddedToInv, amountAddedToBank, amountAddedToGround);
            return;
        }
        long amountToTake = item.getAmount();
        int amountInInv = getInventory().getItems().getNumberOf(item.getId());
        if (item.getId() == 995) {
            int amountInPouch = getMoneyPouch().getTotal();
            if (amountToTake > ((long) (Integer.MAX_VALUE - (amountInInv == 0 && getInventory().getFreeSlots() == 0 ? Integer.MAX_VALUE : amountInInv)) + (long) (Integer.MAX_VALUE - amountInPouch)))
                amountToTake = (long) (Integer.MAX_VALUE - (amountInInv == 0 && getInventory().getFreeSlots() == 0 ? Integer.MAX_VALUE : amountInInv)) + (long) (Integer.MAX_VALUE - amountInPouch);
        } else {
            if (amountInInv == 0 && getInventory().getFreeSlots() == 0) {
                Item bankItem = item.getAttributes() == null ? getBank().getItemIncludingPlaceHolders(item.getDefinitions().isNoted() ? item.getDefinitions().certId : item.getId()) : getBank().getItemIncludingPlaceHolders(item);
                if (bankItem == null) {
                    if (getBank().isFull()) {
                        World.updateGroundItem(new Item(item.getId(), amountLeft, item.getCharges()).setAttributes(item.getAttributes()), new WorldTile(this), this, 60, 0, false);
                        amountAddedToGround += amountLeft;
                        if (sendMessage)
                            sendAddedItemMessage(item, amountAddedToInv, amountAddedToBank, amountAddedToGround);
                        return;
                    }
                    getBank().addItem(item.getDefinitions().isNoted() ? item.getDefinitions().certId : item.getId(), amountLeft, item.getCharges(), item.getAttributes(), true);
                    amountAddedToBank += amountLeft;
                    if (sendMessage)
                        sendAddedItemMessage(item, amountAddedToInv, amountAddedToBank, amountAddedToGround);
                    return;
                }
                int amountToBank = amountLeft;
                if (((long) bankItem.getAmount() + (long) amountToBank) > Integer.MAX_VALUE)
                    amountToBank = Integer.MAX_VALUE - bankItem.getAmount();
                amountLeft -= amountToBank;
                if (amountToBank > 0) {
                    getBank().addItem(item.getDefinitions().isNoted() ? item.getDefinitions().certId : item.getId(), amountToBank, item.getCharges(), item.getAttributes(), true);
                    amountAddedToBank += amountToBank;
                }
                if (amountLeft > 0) {
                    World.updateGroundItem(new Item(item.getId(), amountLeft, item.getCharges()).setAttributes(item.getAttributes()), new WorldTile(this), this, 60, 0, false);
                    amountAddedToGround += amountLeft;
                }
                if (sendMessage)
                    sendAddedItemMessage(item, amountAddedToInv, amountAddedToBank, amountAddedToGround);
                return;
            }
            if (amountToTake > (Integer.MAX_VALUE - amountInInv))
                amountToTake = (Integer.MAX_VALUE - amountInInv);
        }
        amountLeft -= amountToTake;
        if (item.getId() == 995) {
            int amountInPouch = getMoneyPouch().getTotal();
            int amountToPouch = (int) (amountToTake > (long) (Integer.MAX_VALUE - amountInPouch) ? (Integer.MAX_VALUE - amountInPouch) : amountToTake);
            if (amountToPouch > 0)
                getMoneyPouch().addMoneyMisc(amountToPouch);
            amountToTake -= amountToPouch;
        }
        if (amountToTake > 0) {
            getInventory().addItemToInventory(item.getId(), (int) amountToTake, false);
            amountAddedToInv += (int) amountToTake;
        }
        if (amountLeft > 0) {
            Item bankItem = item.getAttributes() == null ? getBank().getItemIncludingPlaceHolders(item.getDefinitions().isNoted() ? item.getDefinitions().certId : item.getId()) : getBank().getItemIncludingPlaceHolders(item);
            if (bankItem == null) {
                if (getBank().isFull()) {
                    World.updateGroundItem(new Item(item.getId(), amountLeft, item.getCharges()).setAttributes(item.getAttributes()), new WorldTile(this), this, 60, 0, false);
                    amountAddedToGround += amountLeft;
                    if (sendMessage)
                        sendAddedItemMessage(item, amountAddedToInv, amountAddedToBank, amountAddedToGround);
                    return;
                }
                getBank().addItem(item.getDefinitions().isNoted() ? item.getDefinitions().certId : item.getId(), amountLeft, item.getCharges(), item.getAttributes(), true);
                amountAddedToBank += amountLeft;
                if (sendMessage)
                    sendAddedItemMessage(item, amountAddedToInv, amountAddedToBank, amountAddedToGround);
                return;
            }
            int amountToBank = amountLeft;
            if (((long) bankItem.getAmount() + (long) amountToBank) > Integer.MAX_VALUE)
                amountToBank = Integer.MAX_VALUE - bankItem.getAmount();
            amountLeft -= amountToBank;
            if (amountToBank > 0) {
                getBank().addItem(item.getDefinitions().isNoted() ? item.getDefinitions().certId : item.getId(), amountToBank, item.getCharges(), item.getAttributes(), true);
                amountAddedToBank += amountToBank;
            }
            if (amountLeft > 0) {
                World.updateGroundItem(new Item(item.getId(), amountLeft, item.getCharges()).setAttributes(item.getAttributes()), new WorldTile(this), this, 60, 0, false);
                amountAddedToGround += amountLeft;
            }
        }
        if (sendMessage)
            sendAddedItemMessage(item, amountAddedToInv, amountAddedToBank, amountAddedToGround);
    }

    public void sendAddedItemMessage(Item item, int amountAddedToInv, int amountAddedToBank, int amountAddedToGround) {
        if (amountAddedToInv > 0)
            getPackets().sendGameMessage(amountAddedToInv + " x " + item.getName() + " has been added to your inventory.", true);
        if (amountAddedToBank > 0)
            getPackets().sendGameMessage(amountAddedToBank + " x " + item.getName() + " has been added to your bank.", true);
        if (amountAddedToGround > 0)
            getPackets().sendGameMessage("You did not have enough inventory/bank space so " + amountAddedToGround + " x " + item.getName() + " has been placed on the ground underneath you.", true);
    }

    private boolean hideFamiliarOptions;

    public void switchHideFamiliarOptions() {
        hideFamiliarOptions = !hideFamiliarOptions;
        localNPCUpdate.reset();
        refreshHideFamiliarOptions();
    }
    
    private void refreshHideFamiliarOptions() {
        VBM.sendVarBit(18564, hideFamiliarOptions ? 1 : 0);
    }

    public boolean isHideFamiliarOptions() {
        return hideFamiliarOptions;
    }
    
    private transient int npcViewDistanceBits = 5;

    public int getNPCViewDistanceBits() {
        if (npcViewDistanceBits < 5)
            npcViewDistanceBits = 5;
        return npcViewDistanceBits;
    }
    
    public void setNPCViewDistanceBits(int npcViewDistanceBits) { 
        setNPCViewDistanceBits(npcViewDistanceBits, true);
    }
    
    public void setNPCViewDistanceBits(int npcViewDistanceBits, boolean update) {
        if (npcViewDistanceBits < 5)
            npcViewDistanceBits = 5;
        this.npcViewDistanceBits = npcViewDistanceBits;
        if (update) {
            getLocalNPCUpdate().reset();
            loadMapRegions();
        }
    }
    
    private boolean legacyCombatMode;
    private boolean legacyInterfacesSkin;
    private boolean legacyInterfaceMode;
    
    public boolean isInLegacyCombatMode() {
        return legacyCombatMode;
    }
    
    public void setLegacyMode(boolean legacyCombatMode) {
        this.legacyCombatMode = legacyCombatMode;
        if(this.legacyCombatMode && this.legacyInterfaceMode)
            this.setAlwaysChatOnMode(true);
        refreshInterfaceVars();
        getInterfaceManager().sendAbilities();
    }
    
    public boolean isInLegacyInterfaceMode() {
        return legacyInterfaceMode;
    }


    public void toggleLegacyInterfacesSkin() {
        this.legacyInterfacesSkin = !legacyInterfacesSkin;
        if(this.legacyCombatMode && this.legacyInterfaceMode)
            this.setAlwaysChatOnMode(true);
        refreshLegacyInterfacesSkin();
    }

    public void refreshLegacyInterfacesSkin() {
        VBM.forceSendVarBit(22875, legacyInterfacesSkin || legacyInterfaceMode ? 1 : 0);
    }

    private void refreshLegacyInterfaces() {
        VBM.forceSendVarBit(27169, legacyInterfaceMode ? 1 : 0);
    }
    
    public boolean hasLegacyInterfacesSkin() {
        return legacyInterfacesSkin;
    }
    
    public void refreshInterfaceVars() {
        actionbar.refreshMultiActionBar();
        refreshLegacyInterfacesSkin();
        refreshLegacyInterfaces();
        refreshTargetReticules();
        refreshActionBarLocked();
       // refreshMeleeMenuLocked();
        //refreshRangeMenuLocked();
        //refreshDefensiveMenuLocked();
        refreshHideLogoutWarning();
        refreshLockInterfaceCustomization();
        refreshGameSettingsInteractionsCount();
        refreshGameSettingsMenuId();
        refreshCameraMode();
        refreshZoomSensitivity();
        refreshLockZoom();
        refreshDisableCameraShake();
        refreshKeyboardSensitivity();
        refreshMouseSensitivity();
        refreshHideMouseoverText();
        refreshHideExtraPotionInformation();
        refreshHideCogsOnAugmentedItems();
        refreshPlayerAttackOption();
        refreshNPCAttackOption();
        refreshHideFamiliarOptions();
        refreshHideDuelOptionOnPlayers();
        refreshClickThroughtChatBoxes();
        refreshHideUpperLeftHoverText();
        refreshDisableShortcutCloseWindow();
        refreshHideTitleBarsWhenLocked();
        refreshSlimHeaders();
        refreshTransparency();
        refreshGuidanceSystemHints();
        refreshTaskCompletePopups();
        refreshSkillTargetBasedXPPopup();
        refreshMakeXProgressWindow();
        refreshDisableHighlightBoostedSkills();
        refreshSlayerCounter();
        refreshUTCClock();
        refreshDisableDragToDropItems();
        refreshDynamicBackpackColumns();
        refreshDisableXpLampWarning();
        refreshChatButtonsInMenu();
        refreshChatButtonsCollapsed();
        refreshAlwaysChatOnMode();
        refreshLegacyMapIcons();
        refreshSummoningOrbOption();
        refreshToggleBuffTimer(true);
        refreshBlockPvPXP();
        refreshVerboseShopDisplayMode();
        refreshTargetInformationUnDocked();
        refreshConvertMemoryType();
        refreshAtaraxiaCoins();
        refreshShowReaperCounter();
        refreshVirtualCapIcon();
        refreshXpCapIcon();
        refreshYourHitSplats();
        refreshOtherHitSplats();
        refreshDisableEnterQuickChat();
        refreshTimeStamps();
        refreshHidePublicEffects();
        refreshChatPrefix();
        refreshSplitPrivateChat();
        refreshChatsSetup();
        refreshTooglePlayerNotification();
        refreshToogleAbilityCooldownTimer();
    }
    
    public void toggleLegacyInterfaces() {
        this.legacyInterfaceMode = !this.legacyInterfaceMode;
        if(this.legacyCombatMode && this.legacyInterfaceMode)
            this.setAlwaysChatOnMode(true);
        refreshLegacyInterfaces();
        refreshInterfaceVars();
        interfaceManager.sendAbilities();
        // The legacy/modern UI swap reloads HUD components on 1477, which
        // can leave the XP popup overlay hidden or its varbit re-evaluated.
        // Re-run the popup refresh so floating XP drops keep working after
        // a mode flip.
        if (getSkills() != null) {
            getSkills().refreshXPPopUp();
        }
    }
    // new nsi system
    private Map<Integer, Integer> iLayoutVars;
    
    public void resetILayoutVars() {
        iLayoutVars = new HashMap<Integer, Integer>(ILayoutDefaults.INTERFACE_LAYOUT_VARS);
    }
    
    public Map<Integer, Integer> getILayoutVars() {
        return iLayoutVars;
    }
    
    private transient boolean viewingGame;

    public boolean isViewingGame() {
        return viewingGame;
    }

    private boolean resetedAllMinigameGear;

    public static boolean isMinigameGear(int itemId) {
        int[] itemIds = {21462, 21463, 21464, 21465, 21466, 21467, 21468, 21469, 21470, 21471, 21472,
                21473, 21474, 21475, 21476, 21537, 21538, 21539, 21540, 21541, 21542, 21543, 21544,
                21545, 21546, 21547, 21548, 21549, 21550, 21551, 21552, 21553, 21554, 21555, 21556,
                21557, 21558, 21559, 21560, 21561, 21562, 21563, 21564, 21565, 21566};
        for (int id : itemIds)
            if (id == itemId)
                return true;
        return false;
    }

    public void resetMinigameGear() {
        if (resetedAllMinigameGear)
            return;
        int amountItemsFound = 0;
        for (int i = 0; i < getInventory().getItems().getItems().length; i++) {
            Item item = getInventory().getItems().getItems()[i];
            if (item == null || !isMinigameGear(item.getId()) || item.getAttributes() != null)
                continue;
            getInventory().set(i, null);
            amountItemsFound++;
        }
        inventory.refresh();
        for (int i = 0; i < getEquipment().getItems().getItems().length; i++) {
            Item item = getEquipment().getItems().getItems()[i];
            if (item == null || !isMinigameGear(item.getId()) || item.getAttributes() != null)
                continue;
            getEquipment().set(i, null);
            amountItemsFound++;
        }
        equipment.refreshItemContainer();
        for (Bank bank : getBanks()) {
            if (bank == null)
                continue;
            for (int tab = 0; tab < bank.bankTabs.length; tab++) {
                for (int slotId = 0; slotId < bank.bankTabs[tab].length; slotId++) {
                    Item item = bank.bankTabs[tab][slotId];
                    if (item == null || !isMinigameGear(item.getId()) || item.getAttributes() != null)
                        continue;
                    int[] slot = new int[]{tab, slotId};
                    amountItemsFound += item.getAmount();
                    bank.removeItem(slot, item.getAmount(), false, Bank.DESTROY_ITEM);
                }
            }
        }
        bank.refreshTabs();
        bank.refreshItems();
        Iterator<Item> itr = getEquipment().getKeepSakeItems().iterator();
        while (itr.hasNext()) {
            Item item = itr.next();
            if (item == null || !isMinigameGear(item.getId()) || item.getAttributes() != null)
                continue;
            int equipSlot = item.getDefinitions().getEquipSlot();
            Item equiItem = equipment.getCosmeticItems().get(equipSlot);
            if (equiItem != null && equiItem.getId() == item.getId()) {
                getEquipment().getCosmeticItems().set(equipSlot, null);
                getAppearence().generateAppearenceData();
            }
            amountItemsFound += item.getAmount();
            itr.remove();
        }
        if (amountItemsFound > 0) {
            getBank().addItem(new Item(32707, amountItemsFound * 5), true);
            getPackets().sendGameMessage("<col=ff0000>Your minigame gear has been removed, and " + (amountItemsFound * 5) + " x Tortured souls have been added to your bank.");
        }
        resetedAllMinigameGear = true;
    }


    public void setViewingGame(boolean viewingGame) {
        this.viewingGame = viewingGame;
    }

    private boolean targetReticules = true;

    public boolean isTargetReticule() {
        return !legacyInterfaceMode && targetReticules;
    }

    private void refreshTargetReticules() {
        VBM.sendVarBit(19929, !legacyInterfaceMode && targetReticules ? 0 : 1);
    }

    public void switchTargetReticules() {
        targetReticules = !targetReticules;
        refreshTargetReticules();
    }
    
    private transient boolean refreshClanIcon;
    
    public boolean isRefreshClanIcon() {
        return refreshClanIcon;
    }

    public void setRefreshClanIcon(boolean refreshClanIcon) {
        this.refreshClanIcon = refreshClanIcon;
    }

    private void refreshActionBarLocked() {
        VBM.sendVarBit(27168, legacyCombatMode ? 1 : 0);
    }

   // public void refreshMeleeMenuLocked() {
      //  VBM.sendVarBit(19014, legacyCombatMode ? 1 : 0);
   // }

    //public void refreshRangeMenuLocked() {
       // VBM.sendVarBit(19015, legacyCombatMode ? 1 : 0);
   // }

   // public void refreshDefensiveMenuLocked() {
       // VBM.sendVarBit(19017, legacyCombatMode ? 1 : 0);
   // }
    
    @Getter
    @Setter
    private transient boolean usingNXT;
    
    
    private transient int lastRegionKey;

    @Override
    public boolean needMapUpdate() {
        if (isNative950())
            return super.needMapUpdate();
        return super.needMapUpdate() || (isUsingNXT() && lastRegionKey != getRegionKey());
    }
    
    @Getter
    @Setter
    private int[] subMenus;

    private boolean hideLogoutWarning;

    public boolean isHideLogoutWarning() {
        return hideLogoutWarning;
    }

    public void switchHideLogoutWarning() {
        hideLogoutWarning = !hideLogoutWarning;
        refreshHideLogoutWarning();
    }

    public void setHideLogoutWarning(boolean hideLogoutWarning) {
        this.hideLogoutWarning = hideLogoutWarning;
        refreshHideLogoutWarning();
    }

    private void refreshHideLogoutWarning() {
        VBM.sendVarBit(42255, hideLogoutWarning ? 1 : 0);
    }
    
    private boolean lockInterfaceCustomization;
    
    public boolean isLockInterfaceCustomization() {
        return lockInterfaceCustomization;
    }

    public void switchLockInterfaceCustomization() {
        lockInterfaceCustomization = !lockInterfaceCustomization;
        refreshLockInterfaceCustomization();
    }
    
    private void refreshLockInterfaceCustomization() {
        VBM.sendVarBit(19925, legacyInterfaceMode || lockInterfaceCustomization ? 1 : 0);
    }
    @Getter
    private transient int gameSettingsInteractionsCount;
    
    public void increaseGameSettingsInteractions() {
        gameSettingsInteractionsCount++;
        refreshGameSettingsInteractionsCount();
    }
    
    public void resetGameSettingsInteractionsCount() {
        gameSettingsInteractionsCount = 0;
        refreshGameSettingsInteractionsCount();
    }
    
    public void refreshGameSettingsInteractionsCount() {
        getPackets().sendConfigByFile(42102, gameSettingsInteractionsCount);
    }
    
    @Getter
    private int gameSettingsMenuId;
    
    public void setGameSettingsMenuId(int slotId) {
        gameSettingsMenuId = slotId;
        refreshGameSettingsMenuId();
    }
    
    public void refreshGameSettingsMenuId() {
        getPackets().sendConfigByFile(42101, gameSettingsMenuId);
    }
    
    @Getter
    private transient boolean settingsTabClosed;
    
    public void toggleSettingsTabClosed() {
        settingsTabClosed = !settingsTabClosed;
        refreshSettingsTabClosed();
    }
    
    public void resetSettingsTabClosed() {
        settingsTabClosed = false;
        refreshSettingsTabClosed();
    }

    private void refreshSettingsTabClosed() {
        getPackets().sendGlobalConfig(6501, settingsTabClosed ? 1 : 0);
    }
    
    public void sendDropDownMenuEvent(final DropDownMenuEvent event) {
        interfaceManager.unlockDropDownMenu(0, 500);
        getTemporaryAttributtes().put("pluginDropdown", event);
    }
    
    private int cameraMode;
    
    public void setCameraMode(int cameraMode) {
        this.cameraMode = cameraMode;
        refreshCameraMode();
    }

    public void refreshCameraMode() {
        VBM.sendVarBit(19949, cameraMode);
    }
    
    private int zoomSensitivity;
    
    public void setZoomSensitivity(int zoomSensitivity) {
        this.zoomSensitivity = zoomSensitivity;
        refreshZoomSensitivity();
    }
    
    public void refreshZoomSensitivity() {
        getPackets().sendGlobalConfig(6565, zoomSensitivity);
    }
    
    private boolean lockZoom;

    public void switchLockZoom() {
        lockZoom = !lockZoom;
        refreshLockZoom();
    }
    
    private void refreshLockZoom() {
        VBM.sendVarBit(19926, lockZoom ? 1 : 0);
    }
    
    private boolean disableCameraShake;
    
    public void toggleDisableCameraShake() {
        disableCameraShake = !disableCameraShake;
        refreshDisableCameraShake();
    }
    
    public void refreshDisableCameraShake() {
        VBM.sendVarBit(34078, disableCameraShake ? 1 : 0);
    }
    
    private int keyboardSensitivityH = 175, keyboardSensitivityV = 135;
    
    public void setKeyboardSensitivity(boolean h, int sensitivity) {
        if (h)
            keyboardSensitivityH = sensitivity;
        else
            keyboardSensitivityV = sensitivity;
        refreshKeyboardSensitivity();
    }
    
    public void refreshKeyboardSensitivity() {
        getPackets().sendGlobalConfig(2827, keyboardSensitivityH);
        getPackets().sendGlobalConfig(2828, keyboardSensitivityV);
    }
    
    private int mouseSensitivityH = 14, mouseSensitivityV = 13;
    
    public void setMouseSensitivity(boolean h, int sensitivity) {
        if (h)
            mouseSensitivityH = sensitivity;
        else
            mouseSensitivityV = sensitivity;
        refreshMouseSensitivity();
    }
    
    public void refreshMouseSensitivity() {
        getPackets().sendGlobalConfig(2829, mouseSensitivityH);
        getPackets().sendGlobalConfig(2830, mouseSensitivityV);
    }
    
    private boolean hideMouseoverText;

    public void switchHideMouseoverText() {
        hideMouseoverText = !hideMouseoverText;
        refreshHideMouseoverText();
    }

    private void refreshHideMouseoverText() {
        VBM.sendVarBit(42094, hideMouseoverText ? 1 : 0);
    }
    
    private boolean hideExtraPotionInformation;

    public void switchHideExtraPotionInformation() {
        hideExtraPotionInformation = !hideExtraPotionInformation;
        refreshHideExtraPotionInformation();
    }

    private void refreshHideExtraPotionInformation() {
        VBM.sendVarBit(42534, hideExtraPotionInformation ? 1 : 0);
    }

    private boolean hideCogsOnAugmentedItems;

    public void switchHideCogsOnAugmentedItems() {
        hideCogsOnAugmentedItems = !hideCogsOnAugmentedItems;
        refreshHideCogsOnAugmentedItems();
    }

    private void refreshHideCogsOnAugmentedItems() {
        VBM.sendVarBit(37571, hideCogsOnAugmentedItems ? 1 : 0);
        VBM.sendVar(4012, hideCogsOnAugmentedItems ? 114370 : 114407);
        VBM.sendVar(4012, hideCogsOnAugmentedItems ? 114371 : 114408);
    }
    
    private int playerAttackOption;
    private int npcAttackOption;

    public void setPlayerAttackOption(int playerAttackOption) {
        this.playerAttackOption = playerAttackOption;
        refreshPlayerAttackOption();
    }

    public void setNPCAttackOption(int npcAttackOption) {
        this.npcAttackOption = npcAttackOption;
        refreshNPCAttackOption();
    }

    private void refreshPlayerAttackOption() {
        VBM.sendVarBit(35185, playerAttackOption);
    }

    private void refreshNPCAttackOption() {
        VBM.sendVarBit(35186, npcAttackOption);
    }
    
    private boolean hideDuelOptionOnPlayers;

    public void switchHideDuelOptionOnPlayers() {
        hideDuelOptionOnPlayers = !hideDuelOptionOnPlayers;
        refreshHideDuelOptionOnPlayers();
    }

    private void refreshHideDuelOptionOnPlayers() {
        VBM.sendVarBit(32404, hideDuelOptionOnPlayers ? 1 : 0);
    }
    
    private boolean clickThroughtChatboxes;

    public void switchClickThroughtChatBoxes() {
        clickThroughtChatboxes = !clickThroughtChatboxes;
        refreshClickThroughtChatBoxes();
    }

    private void refreshClickThroughtChatBoxes() {
        VBM.sendVarBit(20188, legacyInterfaceMode || clickThroughtChatboxes ? 1 : 0);
    }
    
    private boolean hideUpperLeftHoverText;

    public void switchHideUpperLeftHoverText() {
        hideUpperLeftHoverText = !hideUpperLeftHoverText;
        refreshHideUpperLeftHoverText();
    }

    private void refreshHideUpperLeftHoverText() {
        VBM.sendVarBit(30492, hideUpperLeftHoverText ? 1 : 0);
    }

    private boolean disableShortcutCloseWindow;

    public void switchDisableShortcutCloseWindow() {
        disableShortcutCloseWindow = !disableShortcutCloseWindow;
        refreshDisableShortcutCloseWindow();
    }

    private void refreshDisableShortcutCloseWindow() {
        VBM.sendVarBit(36885, disableShortcutCloseWindow ? 1 : 0);
    }
    
    private boolean hideTitleBarsWhenLocked;
    private boolean slimHeaders;
    
    public void switchSlimHeaders() {
        slimHeaders = !slimHeaders;
        refreshSlimHeaders();
    }

    public void switchHideTitleBarsWhenLocked() {
        hideTitleBarsWhenLocked = !hideTitleBarsWhenLocked;
        refreshHideTitleBarsWhenLocked();
    }
    
    private void refreshSlimHeaders() {
        VBM.sendVarBit(19924, legacyInterfaceMode || slimHeaders ? 1 : 0);
    }
    
    private void refreshHideTitleBarsWhenLocked() {
        VBM.sendVarBit(19928, legacyInterfaceMode || hideTitleBarsWhenLocked ? 1 : 0);
    }
    
    private int transparency;

    public void setTransparency(int transparency) {
        this.transparency = transparency;
        refreshTransparency();
    }

    private void refreshTransparency() {
        VBM.sendVar(7624, transparency);
    }
    
    private boolean guidanceSystemHints = true;
    
    public void switchGuidanceSystemHints() {
        guidanceSystemHints = !guidanceSystemHints;
        refreshGuidanceSystemHints();
    }
    
    private void refreshGuidanceSystemHints() {
        VBM.sendVarBit(20924, guidanceSystemHints ? 0 : 1);
    }

    private boolean taskCompletePopup;
    
    public void switchTaskCompletePopups() {
        taskCompletePopup = !taskCompletePopup;
        refreshTaskCompletePopups();
    }

    private void refreshTaskCompletePopups() {
        VBM.sendVarBit(42093, taskCompletePopup ? 1 : 0);
    }

    private boolean skillTargetBasedXPPopup;
    
    public void switchSkillTargetBasedXPPopup() {
        skillTargetBasedXPPopup = !skillTargetBasedXPPopup;
        refreshSkillTargetBasedXPPopup();
    }
    
    public void refreshSkillTargetBasedXPPopup() {
        VBM.sendVarBit(26632, skillTargetBasedXPPopup ? 1 : 0);
    }
    
    private boolean makeXProgressWindow = true;

    public void switchMakeXProgressWindow() {
        makeXProgressWindow = !makeXProgressWindow;
        refreshMakeXProgressWindow();
    }
    
    private void refreshMakeXProgressWindow() {
        VBM.sendVarBit(3034, makeXProgressWindow ? 0 : 1);
    }
    
    private boolean disableHighlightBoostedSkills;
    
    public void switchDisableHighlightBoostedSkills() {
        disableHighlightBoostedSkills = !disableHighlightBoostedSkills;
        refreshDisableHighlightBoostedSkills();
    }
    
    private void refreshDisableHighlightBoostedSkills() {
        VBM.sendVarBit(44074, disableHighlightBoostedSkills ? 1 : 0);
    }
    
    private boolean slayerCounter;

    public boolean hasSlayerCounter() {
        return slayerCounter;
    }

    public void setSlayerCounter(boolean slayerCounter) {
        this.slayerCounter = slayerCounter;
        refreshSlayerCounter();
    }

    public void switchSlayerCounter() {
        slayerCounter = !slayerCounter;
        refreshSlayerCounter();
    }

    private void refreshSlayerCounter() {
        VBM.sendVarBit(28385, slayerCounter ? 1 : 0);
        if (!slayerCounter && interfaceManager.containsSlayerCounterInterface())
            interfaceManager.removeSlayerCounterInterface();
        else if (slayerCounter && !interfaceManager.containsSlayerCounterInterface())
            interfaceManager.sendSlayerCounterInterface();
    }
    
    private byte utcClock = 1;

    public void setUTCClock(int type) {
        this.utcClock = (byte) type;
        refreshUTCClock();
    }

    private void refreshUTCClock() {
        VBM.sendVarBit(26696, utcClock);
        getPackets().sendHideIComponent(635, 0, utcClock == 0);
        getPackets().sendHideIComponent(635, 5, utcClock == 2);
    }
    @Getter
    private boolean disableDragToDropItems;

    public void switchDisableDragToDropItems() {
        disableDragToDropItems = !disableDragToDropItems;
        refreshDisableDragToDropItems();
    }

    private void refreshDisableDragToDropItems() {
        VBM.sendVarBit(27164, disableDragToDropItems ? 1 : 0);
    }

    private int dynamicBackpackColumns;

    public void setDynamicBackpackColumns(int dynamicBackpackColumns) {
        this.dynamicBackpackColumns = dynamicBackpackColumns;
        refreshDynamicBackpackColumns();
    }

    private void refreshDynamicBackpackColumns() {
        VBM.sendVarBit(31052, dynamicBackpackColumns);
    }
    
    private boolean disableXpLampWarning;
    
    public void switchDisableXpLampWarning() {
        disableXpLampWarning = !disableXpLampWarning;
        refreshDisableXpLampWarning();
    }

    private void refreshDisableXpLampWarning() {
        VBM.sendVarBit(34101, disableXpLampWarning ? 1 : 0);
    }
    
    private boolean chatButtonsInMenu;

    public boolean isChatButtonsInMenu() {
        return chatButtonsInMenu;
    }

    public void switchChatButtonsInMenu() {
        chatButtonsInMenu = !chatButtonsInMenu;
        refreshChatButtonsInMenu();
    }

    private void refreshChatButtonsInMenu() {
        VBM.sendVarBit(36975, chatButtonsInMenu ? 1 : 0);
        VBM.sendVarBit(18796, chatButtonsInMenu ? 1 : 0);
    }

    private boolean chatButtonsCollapsed;

    public void switchChatButtonsCollapsed() {
        chatButtonsCollapsed = !chatButtonsCollapsed;
        refreshChatButtonsCollapsed();
    }

    private void refreshChatButtonsCollapsed() {
        for (int i = 0; i < 7; i++)
            VBM.sendVarBit(36976 + i, chatButtonsCollapsed ? 1 : 0);
    }
    @Getter
    private boolean alwaysOnChatMode;
    
    public void setAlwaysChatOnMode(boolean alwaysOnChatMode) {
        this.alwaysOnChatMode = alwaysOnChatMode;
        refreshAlwaysChatOnMode();
    }

    public void switchAlwaysChatOnMode() {
        alwaysOnChatMode = !alwaysOnChatMode;
        refreshAlwaysChatOnMode();
    }
    
    public void refreshAlwaysChatOnMode() {
        getPackets().sendConfigByFile(22310, alwaysOnChatMode ? 1 : 0);
    }
    
    // game bar status
    private int gameStatus;
    private int assistStatus;
    private int chatBadgeStatus;
    private int localChatStatus;
    
    public int getGameStatus() {
        return gameStatus;
    }
    
    public void setGameStatus(int gameStatus) {
        this.gameStatus = gameStatus;
        getPackets().sendGameBarStages();
    }
    
    private boolean broadCastMessages;
    
    public void toggleBroadCastMessages() {
        broadCastMessages = !broadCastMessages;
        getPackets().sendGameBarStages();
    }
    
    public boolean isBroadCastMessages() {
        return broadCastMessages;
    }
    
    public int getAssistStatus() {
        return assistStatus;
    }

    public void setAssistStatus(int assistStatus) {
        if (assistStatus != this.assistStatus) {
            this.assistStatus = assistStatus;
        }
        getPackets().sendGameBarStages();
    }

    public int getChatBadgeStatus() {
        return chatBadgeStatus;
    }

    public void setChatBadgeStatus(int chatStatus) {
        this.chatBadgeStatus = chatStatus;
        getPackets().sendGameBarStages();
    }

    public int getLocalChatStatus() {
        return localChatStatus;
    }

    public void setLocalChatStatus(int localChatStatus) {
        this.localChatStatus = localChatStatus;
        getPackets().sendGameBarStages();
    }
    private int guestClanStatus;
    
    public int getGuestClanStatus() {
        return guestClanStatus;
    }

    public void setGuestClanStatus(int guestClanStatus) {
        this.guestClanStatus = guestClanStatus;
        getPackets().sendGameBarStages();
    }
    
    private int groupStatus;
    
    public int getGroupStatus() {
        return groupStatus;
    }

    public void setGroupStatus(int groupStatus) {
        this.groupStatus = groupStatus;
        getPackets().sendGameBarStages();
    }
    
    private boolean legacyMapIcons;
    
    public void switchLegacyMapIcons() {
        legacyMapIcons = !legacyMapIcons;
        refreshLegacyMapIcons();
    }
    
    private void refreshLegacyMapIcons() {
        VBM.sendVarBit(22874, legacyMapIcons ? 1 : 0);
    }

    private boolean displayFamilarInventory;

    public boolean isDisplayFamilarInventory() {
        return displayFamilarInventory;
    }

    public void setDisplayFamilarInventory(boolean displayFamilarInventory) {
        this.displayFamilarInventory = displayFamilarInventory;
    }
    
    public static final int[] LEFT_CLICK_OPTION_SLOTS = { 5, 1, 2, 4, 3, 8, 9, 7, 6 };


    private int summoningLeftClickOption;

    public int getSummoningLeftClickOption() {
        return summoningLeftClickOption;
    }

    public void setSummoningLeftClickOption(int slot) {
        this.summoningLeftClickOption = LEFT_CLICK_OPTION_SLOTS[slot];
        refreshSummoningLeftClickOption();
    }

    public void refreshSummoningLeftClickOption() {
        VBM.forceSendVarBit(6070, summoningLeftClickOption);
        refreshSummoningOrbOption();
    }

    public static final int[] PET_LEFT_CLICK_OPTION_SLOTS = { 5, 4, 3, 6 };

    private int petLeftClickOption;

    public int getPetLeftClickOption() {
        return petLeftClickOption;
    }

    public void setPetLeftClickOption(int slot) {
        this.petLeftClickOption = PET_LEFT_CLICK_OPTION_SLOTS[slot];
        refreshPetLeftClickOption();
    }

    public void refreshPetLeftClickOption() {
        VBM.forceSendVarBit(28967, petLeftClickOption);
        refreshSummoningOrbOption();
    }

    public static final int[] LEGENDARY_PET_LEFT_CLICK_OPTION_SLOTS = { 5, 4, 3, 8, 9, 6 };

    private int legendaryPetLeftClickOption;

    public int getLegendaryPetLeftClickOption() {
        return legendaryPetLeftClickOption;
    }

    public void setLegendaryPetLeftClickOption(int slot) {
        this.legendaryPetLeftClickOption = LEGENDARY_PET_LEFT_CLICK_OPTION_SLOTS[slot];
        refreshLegendaryPetLeftClickOption();
    }

    public void refreshLegendaryPetLeftClickOption() {
        VBM.forceSendVarBit(28968, legendaryPetLeftClickOption);
        refreshSummoningOrbOption();
    }

    public void refreshSummoningOrbOption() {
        if(getFamiliar() == null && pet == null)
            VBM.sendVar(1831, -1);
        VBM.forceSendVarBit(6068,
                getFamiliar() != null ? summoningLeftClickOption : pet != null ? petLeftClickOption : 0);
    }
    @Getter
    @Setter
    private ActionBar actionbar;

    public void updateBuffs() {
        VBM.sendVar(895, VBM.getValue(895) + 1);
    }
    
    private boolean hasLoggedInForFirstTimeOn910;
    
    public void setDefaultVariables() {
        if(hasLoggedInForFirstTimeOn910)
            return;
        hasLoggedInForFirstTimeOn910 = true;
        mouseSensitivityH = 14;
        mouseSensitivityV = 13;
        keyboardSensitivityH =175;
        keyboardSensitivityV =135;
        zoomSensitivity = 175;
        utcClock = 1;
        alwaysOnChatMode = true;
        targetReticules = true;
        // A native 947 character defaults to the MODERN interface, and the 910 default of legacy
        // is not merely a preference here - it is a trap with three edges. (1) The whole 947 slot
        // model is modern: no key of enum 7716 attaches to any of the legacy hosts, so a legacy
        // player has no evidenced way to open a panel at all. (2) Client script 7198 hides the
        // minimap, action bar, ribbon and HUD overlay wrappers when the legacy interface bit is
        // set, two of which this server force-opens at login, so the player would silently lose
        // panels the server believes it opened. (3) legacyCombatMode makes the client divide the
        // life-points bar by ten, and this port feeds hitpoints on the x100 scale, so it would
        // read as a tenth of the real value.
        //
        // This method is not on the native login path today. It is fenced now rather than later
        // because it sits inside InterfaceManager.sendInterfaces(), which is exactly the method
        // the ribbon work will make reachable - and it runs AFTER that method has already emitted
        // modern interface ids, so the first native login through it would emit modern and then
        // flip the model to legacy. See UI-DECISIONS.md, decision 1.
        boolean legacyDefaults = !isNative950();
        legacyCombatMode = legacyDefaults;
        legacyInterfacesSkin = legacyDefaults;
        legacyInterfaceMode = legacyDefaults;
        toggleBuffTimers = true;
        refreshInterfaceVars();
        getInterfaceManager().sendAbilities();
        refreshAlwaysChatOnMode();
    }
    
    private boolean toggleBuffTimers;
    
    public boolean hasBuffTimersEnabled() {
        return toggleBuffTimers;
    }

    public void refreshToggleBuffTimer(boolean login) {
        getPackets().sendConfigByFile(24832, toggleBuffTimers ? 1 : 0);
        getPackets().sendExecuteScript(364, toggleBuffTimers ? 1 : 0, login ? 0 : 1);
    }
    
    public void switchBuffTimer() {
        toggleBuffTimers = !toggleBuffTimers;
        refreshToggleBuffTimer(false);
    }
    
    private boolean blockPvPXP;

    public void switchBlockPvPXP() {
        blockPvPXP = !blockPvPXP;
        refreshBlockPvPXP();
    }

    public boolean isBlockPvPXP() {
        return blockPvPXP;
    }

    private void refreshBlockPvPXP() {
        getPackets().sendConfigByFile(27450, blockPvPXP ? 1 : 0);
    }
    
    private transient boolean runAfterLoad;
    
    public boolean isRunAfterLoad() {
        return runAfterLoad;
    }
    
    public void runAfterLoad() {
        interfaceManager.refreshInterface(true);
        skills.sendCombatLevel();
        runAfterLoad = true;
    }
    
    @Override
    public String getName() {
        return getDisplayName();
    }

    @Override
    public int getCombatLevel() {
        return skills.getCombatLevel();
    }
    
    @Getter
    private int borrowedSpellId, borrowedSpellChargesRemaining;

    public void setBorrowedSpellId(int borrowedSpellId) {
        this.borrowedSpellId = borrowedSpellId;
        if (this.borrowedSpellId != 0) 
            borrowedSpellChargesRemaining = 1000;
        combatDefinitions.refreshBorrowedSpell();
    }
    
    public void useBorrowedSpellCharge() {
        borrowedSpellChargesRemaining--;
        if (borrowedSpellChargesRemaining <= 0) {
            borrowedSpellChargesRemaining = 0;
            setBorrowedSpellId(0);
        }
    }

    private boolean verboseShopDisplayMode;
    
    public void setVerboseShopDisplayMode(boolean verboseShopDisplayMode) {
        this.verboseShopDisplayMode = verboseShopDisplayMode;
        refreshVerboseShopDisplayMode();
    }

    public void refreshVerboseShopDisplayMode() {
        VBM.sendVarBit(987, verboseShopDisplayMode ? 0 : 1);
    }
    
    @Getter
    private boolean targetInformationUnDocked;
    
    public void toggleTargetInfomationUnDocked() {
        targetInformationUnDocked = !targetInformationUnDocked;
        interfaceManager.sendCombatTargetInterface();
        refreshTargetInformationUnDocked();
    }

    public void refreshTargetInformationUnDocked() {
        getPackets().sendConfigByFile(22332, targetInformationUnDocked ? 1 : 0);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        Player other = (Player) obj;
        if (displayName != null && other.displayName == null)
            return false;
        if (displayName == null && other.displayName != null)
            return false;
        if (displayName != null && other.displayName != null && !displayName.equalsIgnoreCase(other.displayName))
            return false;
        if (username != null && other.username == null)
            return false;
        if (username == null && other.username != null)
            return false;
        return username == null || other.username == null || Utils.formatPlayerNameForDisplay(username).equalsIgnoreCase(Utils.formatPlayerNameForDisplay(other.username));
    }
    
    
    private int convertMemoryType;
    
    public void setConvertMemoryType(int convertMemoryType) {
        this.convertMemoryType = convertMemoryType;
        refreshConvertMemoryType();
    }

    private void refreshConvertMemoryType() {
        getPackets().sendConfigByFile(40524, convertMemoryType);
    }
    
    public ConvertMode getConvertMode() {
        return convertMemoryType == 0 ? ConvertMode.CONVERT_TO_XP : convertMemoryType == 1 ? ConvertMode.CONVERT_TO_MORE_XP : ConvertMode.CONVERT_TO_ENERGY;
    }
    
    @Getter
    @Setter
    private DoomsayerManager doomsayerManager;
    
    public void unlockLodeStoneTeleportLocations() {
        getPackets().sendConfigByFile(9482, 15);
        getPackets().sendConfigByFile(10236, 190);
        for(int i=28;i<=40;i++)
        getPackets().sendConfigByFile(i, 1);
        for(int i=18523;i<=18529;i++)
        getPackets().sendConfigByFile(i, 1);
        getPackets().sendConfigByFile(22430, 1);
        getPackets().sendConfigByFile(24967, 1);
        getPackets().sendConfigByFile(36173, 1);
        getPackets().sendConfigByFile(44270, 1);
    }
    
    @Getter
    private boolean quickTeleportByDefault;
    
    public void toggleQuickTeleportByDefault() {
        quickTeleportByDefault = !quickTeleportByDefault;
        refreshQuickTeleportByDefault();
    }

    private void refreshQuickTeleportByDefault() {
       getPackets().sendConfigByFile(28622, quickTeleportByDefault ? 1 : 0);
    }

    private PlayerExamineManager playerExamineManager;
    
    public PlayerExamineManager getPlayerExamineManager() {
        return playerExamineManager;
    }
    
    public void setPlayerExamineManager(PlayerExamineManager playerExamineManager) {
       this.playerExamineManager = playerExamineManager;
    }
    
    public int[] getMaxedCapeCustomized() {
        return maxedCapeCustomized;
    }

    public void setMaxedCapeCustomized(final int[] maxedCapeCustomized) {
        this.maxedCapeCustomized = maxedCapeCustomized;
    }
    
    private int[] trimmedCompletionistCapeCustomized;
    
    public int[] getTrimmedCompletionistCapeCustomized() {
        return trimmedCompletionistCapeCustomized;
    }

    public void setTrimmedCompletionistCapeCustomized(final int[] skillcapeCustomized) {
        trimmedCompletionistCapeCustomized = skillcapeCustomized;
    }
    
    @Getter
    @Setter
    private int[][] capeCustomizationPresets;
    
    
    public void setCapeCustomizationPresets(int presetIndex, int[] colors) {
        capeCustomizationPresets[presetIndex] = colors;
        refreshCapeCustomizationPresets();
    }

    public void refreshCapeCustomizationPresets() {
        for(int i=0;i<capeCustomizationPresets.length;i++)
            for(int j=0;j<capeCustomizationPresets[i].length;j++)
            VBM.sendVarBit(25814+(i*4)+j, capeCustomizationPresets[i][j]);
    }
    
    @Getter
    @Setter
    private CosmeticsManager cosmeticsManager;
    
    private boolean unlockedOldCosmetics;
    
    public void unlockOldCosmetics() {
        if (unlockedOldCosmetics)
            return;
        for (Integer itemId : unlockedCostumesIds) 
            if (itemId != null)
               // cosmeticsManager.unlockItem(itemId);
        cosmeticsManager.refreshVars();
        unlockedOldCosmetics = true;
    }

    @Override
    public boolean isAtDynamicRegion() {
        DynamicArea area = DynamicArea.getDynamicArea(getRegionId());
        if (isUsingNXT() && area == null)
            return false;
        return isUsingNXT() && area != null || super.isAtDynamicRegion();
    }
    
    private boolean showReaperCounter;
    
    public void toggleShowReaperCounter() {
        showReaperCounter = !showReaperCounter;
        refreshShowReaperCounter();
    }

    private void refreshShowReaperCounter() {
        getPackets().sendConfigByFile(40059, showReaperCounter ? 1 : 0);
    }
    
    public void updateSlayerCounterInformation() {
        SlayerTask sTask = getTask();
        getPackets().sendGlobalString(4967, sTask == null ? "No task." : sTask.getName(this).toLowerCase()+".");
        getPackets().sendConfig(183, sTask == null ? 0 : sTask.getTaskAmount());
        getPackets().sendConfigByFile(9071, getSlayerPoints());
        getPackets().sendConfigByFile(9074, getTaskStreak());
        Contract contract = getContract();
        getPackets().sendGlobalString(7532, contract == null ? "No task." : ContractHandler.getFormattedContractName(this)+".");
        getPackets().sendConfigByFile(22902, contract == null ? 0 : contract.getKillAmount());
        getPackets().sendConfigByFile(22905, getReaperPoints());
        getPackets().sendConfigByFile(23260, 0);// streak
    }
    
    public void refreshUnlockedRecipes() {
        if (meilyrShopSettings2[2])
            meilyrShopSettings2[8] = true;
        for(int i=99;i<=124;i++) {
            int slot1 = MeilyrRecipes.getIndexForSlot(i, false);
            if (slot1 != -1)
                getPackets().sendConfigByFile(MeilyrRecipes.getVarbitIdForSlot(i), meilyrShopSettings[slot1] ? 1 : 0);
            int slot2 = MeilyrRecipes.getIndexForSlot(i, true);
            if (slot2 != -1)
                getPackets().sendConfigByFile(MeilyrRecipes.getVarbitIdForSlot(i), meilyrShopSettings2[slot2] ? 1 : 0);
        }
    }
    
    private int virtualIconId;
    private int virtualIconId2;
    
    public void setVirtualCapIcon(int virtualCapIcon) {
        if (virtualCapIcon > 8)
            virtualCapIcon = 8;
        else if (virtualCapIcon <= 0)
            virtualCapIcon = 0;
        this.virtualIconId = virtualCapIcon;
        refreshVirtualCapIcon();
    }

    private void refreshVirtualCapIcon() {
        VBM.sendVarBit(19010, virtualIconId);
    }

    public void setXpCapIcon(int xpCapIcon) {
        if (xpCapIcon > 8)
            xpCapIcon = 8;
        else if (xpCapIcon <= 0)
            xpCapIcon = 0;
        this.virtualIconId2 = xpCapIcon;
        refreshXpCapIcon();
    }

    private void refreshXpCapIcon() {
        VBM.sendVarBit(19011, virtualIconId2);
    }
    
    private int[] yourHitSplats;
    private int[] otherHitSplats;

    public void setYourHitSplats(int index, int slotId) {
        if (yourHitSplats == null)
            yourHitSplats = new int[3];
        this.yourHitSplats[index] = slotId;
        refreshYourHitSplats();
    }
    
    private void refreshYourHitSplats() {
        if (yourHitSplats == null)
            yourHitSplats = new int[3];
        for (int i = 0; i < yourHitSplats.length; i++)
            VBM.sendVarBit(36892 + i, legacyCombatMode ? 6 : yourHitSplats[i]);
    }

    public void setOtherHitSplats(int index, int slotId) {
        if (otherHitSplats == null)
            otherHitSplats = new int[3];
        this.otherHitSplats[index] = slotId;
        refreshOtherHitSplats();
    }

    private void refreshOtherHitSplats() {
        if (otherHitSplats == null)
            otherHitSplats = new int[3];
        for (int i = 0; i < otherHitSplats.length; i++)
            VBM.sendVarBit(36895 + i, legacyCombatMode ? 6 : otherHitSplats[i]);
    }
    
    private boolean disableEnterQuickChat;
    
    public void toggleDisableEnterQuickChat() {
        disableEnterQuickChat = !disableEnterQuickChat;
        refreshDisableEnterQuickChat();
    }
    
    public void refreshDisableEnterQuickChat() {
        VBM.sendVarBit(21242, disableEnterQuickChat ? 1 : 0);
    }
    
    private boolean timeStamps;
    
    public void switchTimeStamps() {
        timeStamps = !timeStamps;
        refreshTimeStamps();
    }

    private void refreshTimeStamps() {
        VBM.sendVarBit(27452, timeStamps ? 1 : 0);
    }
    
    private boolean hidePublicChatEffects;
    
    public void switchHidePublicEffects() {
        hidePublicChatEffects = !hidePublicChatEffects;
        refreshHidePublicEffects();
    }

    private void refreshHidePublicEffects() {
        VBM.sendVar(456, hidePublicChatEffects ? 1 : 0);
    }
    
    private int chatPrefix;

    public void setChatPrefix(int chatPrefix) {
        this.chatPrefix = chatPrefix;
        refreshChatPrefix();
    }

    private void refreshChatPrefix() {
        VBM.forceSendVarBit(30172, chatPrefix);
    }
    
    private boolean splitPrivateChat;
    
    public void switchSplitPrivateChat() {
        splitPrivateChat = !splitPrivateChat;
        refreshSplitPrivateChat();
    }
    
    private void refreshSplitPrivateChat() {
        VBM.sendVarBit(20187, splitPrivateChat ? 1 : 0);
    }

    private int friendChatSetup;
    private int privateChatSetup;
    private int clanChatSetup;
    private int friendBroadCastChatSetup;
    private int guestChatSetup;
    private int publicChatSetup;
    private int groupChatSetup;
    private int groupTeamChatSetup;
    private int globalBroadCastChatSetup;
    private int worldBroadCastChatSetup;
    
    public void setPublicChatSetup(int publicChatSetup) {
        this.publicChatSetup = publicChatSetup;
        refreshPublicChatSetup();
    }

    public void setGroupChatSetup(int groupChatSetup) {
        this.groupChatSetup = groupChatSetup;
        refreshGroupChatSetup();
    }

    public void setGroupTeamChatSetup(int groupTeamChatSetup) {
        this.groupTeamChatSetup = groupTeamChatSetup;
        refreshGroupTeamChatSetup();
    }

    public void setClanChatSetup(int clanChatSetup) {
        this.clanChatSetup = clanChatSetup;
        refreshClanChatSetup();
    }

    public void setGuestChatSetup(int guestChatSetup) {
        this.guestChatSetup = guestChatSetup;
        refreshGuestChatSetup();
    }

    public void setFriendChatSetup(int friendChatSetup) {
        this.friendChatSetup = friendChatSetup;
        refreshFriendChatSetup();
    }
    
    public void setPrivateChatSetup(int privateChatSetup) {
        this.privateChatSetup = privateChatSetup;
        refreshPrivateChatSetup();
    }
    
    public void setGlobalBroadCastChatSetup(int globalBroadCastChatSetup) {
        this.globalBroadCastChatSetup = globalBroadCastChatSetup;
        refreshGlobalBroadCastChatSetup();
    }
    
    public void setWorldBroadCastChatSetup(int worldBroadCastChatSetup) {
        this.worldBroadCastChatSetup = worldBroadCastChatSetup;
        refreshWorldBroadCastChatSetup();
    }
    
    public void setFriendBroadCastChatSetup(int friendBroadCastChatSetup) {
        this.friendBroadCastChatSetup = friendBroadCastChatSetup;
        refreshFriendBroadCastChatSetup();
    }
    
    public void setChatSetup(int chatSetup) {
        Integer mapId = (Integer) getTemporaryAttributtes().remove(Key.CHAT_SETUP);
        if (mapId == null)
            return;
        if (mapId == 41589)
            setPublicChatSetup(chatSetup);
        else if (mapId == 41590)
            setFriendChatSetup(chatSetup);
        else if (mapId == 41591)
            setPrivateChatSetup(chatSetup);
        else if (mapId == 41592)
            setClanChatSetup(chatSetup);
        else if (mapId == 41593)
            setGuestChatSetup(chatSetup);
        else if (mapId == 41594)
            setGroupChatSetup(chatSetup);
        else if (mapId == 41595)
            setGroupTeamChatSetup(chatSetup);
        else if (mapId == 40950)
            setGlobalBroadCastChatSetup(chatSetup);
        else if (mapId == 40951)
            setWorldBroadCastChatSetup(chatSetup);
        else if (mapId == 40952)
            setFriendBroadCastChatSetup(chatSetup);
    }
    
    public void resetChatSetup() {
        setPublicChatSetup(0);
        setFriendChatSetup(0);
        setPrivateChatSetup(0);
        setClanChatSetup(0);
        setGuestChatSetup(0);
        setGroupChatSetup(0);
        setGroupTeamChatSetup(0);
        setGlobalBroadCastChatSetup(0);
        setWorldBroadCastChatSetup(0);
        setFriendBroadCastChatSetup(0);
    }
    
    private void refreshChatsSetup() {
        refreshFriendChatSetup();
        refreshClanChatSetup();
        refreshGuestChatSetup();
        refreshPrivateChatSetup();
        refreshPublicChatSetup();
        refreshGroupChatSetup();
        refreshGroupTeamChatSetup();
        refreshWorldBroadCastChatSetup();
        refreshFriendBroadCastChatSetup();
        refreshGlobalBroadCastChatSetup();
    }
    
    private void refreshFriendBroadCastChatSetup() {
        getPackets().sendConfigByFile(42243, friendBroadCastChatSetup);
    }

    private void refreshPrivateChatSetup() {
        getPackets().sendConfigByFile(7423, privateChatSetup);
    }

    private void refreshFriendChatSetup() {
        getPackets().sendConfigByFile(1190, friendChatSetup);
    }

    private void refreshClanChatSetup() {
        getPackets().sendConfigByFile(1188, clanChatSetup);
    }

    private void refreshGuestChatSetup() {
        getPackets().sendConfigByFile(1191, guestChatSetup);
    }

    public void refreshPublicChatSetup() {
        getPackets().sendConfigByFile(30165, publicChatSetup);
    }

    public void refreshGroupChatSetup() {
        getPackets().sendConfigByFile(24560, groupChatSetup);
    }

    public void refreshGroupTeamChatSetup() {
        getPackets().sendConfigByFile(24561, groupTeamChatSetup);
    }

    private void refreshWorldBroadCastChatSetup() {
        getPackets().sendConfigByFile(42242, worldBroadCastChatSetup);
    }

    private void refreshGlobalBroadCastChatSetup() {
        getPackets().sendConfigByFile(42241, globalBroadCastChatSetup);
    }
    
    private boolean tooglePlayerNotification;
    
    public void switchTooglePlayerNotification() {
        tooglePlayerNotification = !tooglePlayerNotification;
        refreshTooglePlayerNotification();
    }

    private void refreshTooglePlayerNotification() {
        VBM.sendVarBit(24940, tooglePlayerNotification ? 1 : 0);
    }
    

    
    private transient long lastPacketReceivedTime;
    
    public long getLastPacketReceivedTime() {
        return lastPacketReceivedTime;
    }

    public void setLastPacketReceivedTime(final long lastPacketReceivedTime) {
        this.lastPacketReceivedTime = lastPacketReceivedTime;
    }
    
    @Getter
    @Setter
    private BuffDebuffTimersManager buffDebuffTimersManager;
    
    private boolean toogleAbilityCooldownTimer;
    
    public void switchToogleAbilityCooldownTimer() {
        toogleAbilityCooldownTimer = !toogleAbilityCooldownTimer;
        refreshToogleAbilityCooldownTimer();
    }

    private void refreshToogleAbilityCooldownTimer() {
        VBM.sendVarBit(25401, toogleAbilityCooldownTimer ? 1 : 0);
    }
    
    @Getter
    private int[] otherChatsGameStatus;//private, friends, clan, guest clan, trade, group
    
    public void toggleOtherChatsGameStatus(int index, int status) {
        if (otherChatsGameStatus == null)
            otherChatsGameStatus = new int[6];
        otherChatsGameStatus[index] = status;
        getPackets().sendGameBarStages();
    }
    
}
