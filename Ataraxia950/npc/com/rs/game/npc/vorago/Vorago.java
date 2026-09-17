package com.rs.game.npc.vorago;

import com.rs.Settings;
import com.rs.cache.loaders.AnimationDefinitions;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.Animation;
import com.rs.game.Colour;
import com.rs.game.Entity;
import com.rs.game.ForceMovement;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.Projectile;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activities.instances.GodwarsInstance;
import com.rs.game.item.Item;
import com.rs.game.item.floor.FloorItem;
import com.rs.game.map.bossInstance.impl.VoragoInstance;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.herblore.HerbCleaning;
import com.rs.game.player.actions.herblore.HerbCleaning.Herbs;
import com.rs.game.player.combat.PlayerCombat;
import com.rs.game.player.content.Burying.Bone;
import com.rs.game.player.content.Combat;
import com.rs.game.player.content.FriendChatsManager;
import com.rs.game.player.content.HybridTokenDistributor;
import com.rs.game.player.content.contracts.ContractHandler;
import com.rs.game.player.content.dropcollection.DropCollectionConstants;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Logger;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.npcs.NPCDropsDataParser;
import com.rs.utils.data.parsers.npcs.pojos.NPCDrop;
import lombok.val;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("serial")
public class Vorago extends NPC {// 89150

    private final transient VoragoInstance instance;
    private int phase, phaseProgress, attackProgress, damageWhileDown, bringHimPoints;
    private final double voragoPushRatio;
    private transient Player targetedPlayer;
    private final transient CopyOnWriteArrayList<Player> phaseKillers;
    private WorldObject gravityField;
    public FloorItem[] weaponPieces;
    private List<List<NPCDrop>> phaseDrops;
    private long damageTime;
    private int losenWeaponPieceStage;
    private final WorldObject[] ceilingCollapses;
    private final WorldObject[] mists;
    private final WorldObject[] teamSplitSquares;
    private int greenBombBounces;
    private transient Player greenBombP;
    private final Scopulus[] scopuli;
    private final StoneClone[] stoneClones;
    private final Vitalis[] vitali;
    private final TheEndBomb[] theEndBombs;
    private WorldTile theEndFaceTile;
    private int theEndStartIndex, theEndCycles, damageReduction;
    private final List<WorldTile> unWalkAbleSpots;
    private WorldObject waterFall;
    private int damageCheckCycles;
    private final int groupSize;

    public static int[] DEFENSIVE_ABILITY_IDS = {4, 7, 10, 12, 14};

    public static int[] VORAGOES = {17182, 17183, 17184};
    public WorldTile[] locations = {new WorldTile(3095, 6120, 0), new WorldTile(3104, 6120, 0), new WorldTile(3112, 6120, 0), new WorldTile(3095, 6111, 0), new WorldTile(3104, 6111, 0), new WorldTile(3112, 6111, 0), new WorldTile(3095, 6103, 0), new WorldTile(3104, 6103, 0), new WorldTile(3112, 6103, 0)};

    public Vorago(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, VoragoInstance instance) {
        super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
        this.instance = instance;
        getTemporaryAttributtes().put("VoragoType", 1);
        setCantFollowUnderCombat(true);
        setForceFollowClose(false);
        setIntelligentRouteFinder(true);
        setNoDistanceCheck(true);
        setCapDamage(1500);
        generateAttackList();
        phase = 1;
        phaseProgress = 0;
        attackProgress = 0;
        phaseDrops = new ArrayList<List<NPCDrop>>(10);
        phaseKillers = new CopyOnWriteArrayList<Player>();
        weaponPieces = new FloorItem[4];
        ceilingCollapses = new WorldObject[6];
        mists = new WorldObject[9];
        teamSplitSquares = new WorldObject[16];
        unWalkAbleSpots = new ArrayList<WorldTile>();
        theEndBombs = new TheEndBomb[3];
        scopuli = new Scopulus[3];
        stoneClones = new StoneClone[8];
        vitali = new Vitalis[25];
        @SuppressWarnings("unused")
        int startingSize = getPlayerOnBattleCount();
        @SuppressWarnings("unused")
        boolean hardMode = instance.getSettings().isHardMode();
        voragoPushRatio = 1;
        groupSize = instance.getPlayersOnBattle().size();
        setForceMultiArea(true);
    }

    @Override
    public boolean canWalkNPC(int toX, int toY) {
        boolean hardMode = instance.getSettings().isHardMode();
        if ((!hardMode && phase == 5) || (hardMode && phase >= 10))
            return false;
        if (getTemporaryAttributtes().get("BringHimDownClick") != null)
            return false;
        if (getTemporaryAttributtes().get("CantBeAttacked") != null || getTemporaryAttributtes().get("TheEnd") != null)
            return false;
        return super.canWalkNPC(toX, toY);
    }

    @Override
    public void processEntity() {
        if (instance.checkEndBattle())
            return;
        boolean hardMode = instance.getSettings().isHardMode();
        if (getTemporaryAttributtes().get("BringHimDownClick") != null)
            processBringHimDown();
        else if ((isAtLastPhase()) && !sentDeath) {
            WorldTile voragoSpawnLocation = instance.getVoragoSpawnLocation(hardMode ? 10 : 5);
            if (damageCheckCycles == 8) {
                int damage = getTemporaryAttributtes().get("damage") == null ? 0 : (int) getTemporaryAttributtes().get("damage");
                if (damage != 0) {
                    if (damage > 0) {// push players
                        int nextX = (hardMode && phase == 11) ? (getX() + 1) : (getX() - 1);
                        WorldTile nextVoragoTile = new WorldTile(nextX, getY(), getPlane());
                        setNextForceMovement(new ForceMovement(nextVoragoTile, 1, phase == 11 ? ForceMovement.WEST : ForceMovement.EAST));
                        WorldTasksManager.schedule(new WorldTask() {

                            @Override
                            public void run() {
                                setNextWorldTile(nextVoragoTile);
                            }

                        });
                        for (Player player : instance.getPlayersOnBattle()) {
                            if (player == null || player.isDead())
                                continue;
                            if (Utils.colides(new WorldTile(nextX, getY(), getPlane()), player, getSize(), player.getSize())) {
                                if (nextX == ((hardMode && phase == 11) ? (voragoSpawnLocation.getX() + 8) : (voragoSpawnLocation.getX() - 11))) {
                                    player.getTemporaryAttributtes().put("InstantDeath", Boolean.TRUE);
                                    World.sendGraphics(this, new Graphics(4040, 1, 0), new WorldTile(nextX, player.getY(), player.getPlane()));
                                    player.sendDeath(this);
                                } else {
                                    int dir = (hardMode && phase == 11) ? ForceMovement.EAST : ForceMovement.WEST;
                                    WorldTile nextPlayerLocation = new WorldTile((hardMode && phase == 11) ? (player.getX() + 1) : (player.getX() - 1), player.getY(), player.getPlane());
                                    player.setNextForceMovement(new ForceMovement(nextPlayerLocation, 1, dir));
                                    player.lock();
                                    WorldTasksManager.schedule(new WorldTask() {

                                        @Override
                                        public void run() {
                                            player.setNextWorldTile(nextPlayerLocation);
                                            player.unlock();
                                        }

                                    });
                                }
                            }
                        }
                    } else if (damage < 0) {// push vorago
                        int nextX = (hardMode && phase == 11) ? (getX() - 1) : (getX() + 1);
                        WorldTile nextVoragoTile = new WorldTile(nextX, getY(), getPlane());
                        if (nextX == ((hardMode && phase == 11) ? (voragoSpawnLocation.getX() - 12) : (voragoSpawnLocation.getX() + 9))) {
                            for (Player player : instance.getPlayersOnBattle()) {
                                if (player == null || player.isDead())
                                    continue;
                                player.getPackets().sendGameMessage("Only the maul of omens will be enough to finish vorago.");
                            }
                        } else {
                            int dir = (hardMode && phase == 11) ? ForceMovement.WEST : ForceMovement.EAST;
                            setNextForceMovement(new ForceMovement(nextVoragoTile, 1, dir));
                            WorldTasksManager.schedule(new WorldTask() {

                                @Override
                                public void run() {
                                    setNextWorldTile(nextVoragoTile);
                                }

                            });
                        }
                    }
                    getTemporaryAttributtes().put("damage", 0);
                }
                damageCheckCycles = 0;
            }
            damageCheckCycles++;
        }
        super.processEntity();
    }

    // win coords = 3110 5982 0 (spawnlocation + 8 in x) / death player coords =
    // 3091 5982 0(spawnlocation - 11 in x)

    @Override
    public void handleIngoingHit(Hit hit) {
        if (getCapDamage() != -1 && hit.getDamage() > getCapDamage()) {
            hit.setDamage(getCapDamage());
        }
        boolean hardMode = instance.getSettings().isHardMode();
        if (targetedPlayer != null) {
            Hit newHit = new Hit(this, hit.getDamage(), hit.getLook());
            hit.setDamage(0);
            if (hit.isCriticalHit())
                newHit.setCriticalMark();
            int damage = newHit.getDamage();
            if (damage != 0) {
                targetedPlayer.applyHit(newHit);
            }
            if ((!hardMode && phase == 5) || (hardMode && phase >= 10)) {
                int damageR = getTemporaryAttributtes().get("damage") == null ? 0 : (int) getTemporaryAttributtes().get("damage");
                getTemporaryAttributtes().put("damage", damageR + (newHit.getDamage()));
            }
        }
        if (getTemporaryAttributtes().get("BringHimDownClick") != null) {
            damageWhileDown += hit.getDamage();
        }
        if (getTemporaryAttributtes().get("ReducedDamage") != null) {
            int damage = hit.getDamage() * 1 / 8;
            hit.setDamage(damage);
        }
        if (getTemporaryAttributtes().get("TheEnd") != null) {
            int damage = hit.getDamage() * 1 / damageReduction;
            hit.setDamage(damage);
        }
        if (((getHitpoints() - hit.getDamage()) <= 20) && canFinishPhase()) {
            hit.setDamage(0);
            sendDeath(hit.getSource());
        }
        if (((!hardMode && phase == 5) || (hardMode && phase >= 10)) && targetedPlayer == null) {
            int damage = getTemporaryAttributtes().get("damage") == null ? 0 : (int) getTemporaryAttributtes().get("damage");
            getTemporaryAttributtes().put("damage", (damage - ((int) (hit.getDamage() / voragoPushRatio))));
        }
        super.handleIngoingHit(hit);
    }

    @Override
    public Hit handleOutgoingHit(Hit hit, Entity target) {
        boolean hardMode = instance.getSettings().isHardMode();
        if ((!hardMode && phase == 5) || (hardMode && phase >= 10)) {
            int damage = getTemporaryAttributtes().get("damage") == null ? 0 : (int) getTemporaryAttributtes().get("damage");
            if (hit.getLook() != HitLook.VORAGO_SPECIAL_DAMAGE)
                getTemporaryAttributtes().put("damage", (damage + ((int) (hit.getDamage() * voragoPushRatio))));
        }
        return hit;
    }

    @Override
    public void processHit(Hit hit) {
        if (isDead() || getTemporaryAttributtes().get("BringHimDownClick") == Boolean.TRUE)
            return;
        if (((double) (getHitpoints() - hit.getDamage()) < ((double) getMaxHitpoints() * 0.05)) && !canFinishPhase()) {
            setHitpoints((int) ((double) getMaxHitpoints() * 0.3));
            instance.updateInterface(false);
        }
        if (getTemporaryAttributtes().get("BringHimDownClick") == null)
            removeHitpoints(hit);
        getNextHits().add(hit);
        instance.updateInterface(false);
    }

    public boolean sentDeath;

    @Override
    public void sendDeath(Entity source) {
        boolean hardMode = instance.getSettings().isHardMode();
        if ((!hardMode && phase != 5) || (hardMode && phase < 10)) {
            if (!canFinishPhase())
                return;
            if (sentDeath)
                return;
            sentDeath = true;
            setHitpoints(0);
            instance.removeInterfaces();
            setCantInteract(true);
            resetWalkSteps();
            setNextFaceEntity(null);
            for (Player players : instance.getPlayersOnBattle()) {
                if (players == null || players.isDead())
                    continue;
                players.resetCombat();
                players.resetWalkSteps();
                players.setAttackedBy(null);
                players.setFindTargetDelay(0);
                if (players.isGroupIronman()) {
                    players.gimTracker.incrementBpGained(7);
                }
            }
            generatePhaseKiller();
            reset(true);
            setHitpoints(0);
            getCombat().reset();
            getTemporaryAttributtes().put("cantMovePlayers", Boolean.TRUE);
            finishPhase();
        }
    }

    public void sendRealDeath(Player player) {
        if (sentDeath)
            return;
        sentDeath = true;
        instance.removeInterfaces();
        getTemporaryAttributtes().put("cantMovePlayers", Boolean.TRUE);
        boolean hardMode = instance.getSettings().isHardMode();
        if (phase != 11)
            generatePhaseKiller();
        for (Player players : instance.getPlayersOnBattle()) {
            if (players == null || players.isDead() || players == player)
                continue;
            players.lock();
            players.resetCombat();
            players.resetWalkSteps();
            WorldTile sendTile = new WorldTile((hardMode && phase == 10) ? (getX() + 1) : (getX() - 1), getY(), getPlane());
            players.setNextWorldTile(new WorldTile((hardMode && phase == 10) ? (sendTile.getX() + Utils.random(5)) : (sendTile.getX() - Utils.random(5)), sendTile.getY(), sendTile.getPlane()));
        }
        player.lock();
        player.resetCombat();
        player.resetWalkSteps();
        player.setNextWorldTile((hardMode && phase == 11) ? getTile(new WorldTile(3096, 5984, 0)) : getTile(new WorldTile(3109, 5984, 0)));
        player.faceEntity(this);
        setCantInteract(true);
        resetWalkSteps();
        resetCombat();
        setHitpoints(0);
        setNextAnimation(null);
        if (hardMode && phase == 10) {
            final int animDelay = (AnimationDefinitions.getAnimationDefinitions(25432).getEmoteClientCycles() / 30) - 8;
            WorldTasksManager.schedule(new WorldTask() {
                int loop;

                @Override
                public void run() {
                    if (loop == 0) {
                        setNextAnimation(new Animation(25432));
                        setNextGraphics(new Graphics(4036));
                        player.setNextAnimation(new Animation(25433));
                    } else if (loop == 4) {
                        for (Player players : instance.getPlayersOnBattle()) {
                            if (players == null || players.isDead())
                                continue;
                            Dialogue.sendNPCDialogueNoContinue(player, getId(), Dialogue.ANGERY, "Enough!", "If you truly are the Defeater, Roon-show me!");
                        }
                    } else if (loop == 6) {
                        for (Player players : instance.getPlayersOnBattle()) {
                            if (players == null || players.isDead() || players == player)
                                continue;
                            players.unlock();
                            players.getTemporaryAttributtes().put("ENDING_PHASE_START", Boolean.TRUE);
                        }
                        player.getTemporaryAttributtes().put("ENDING_PHASE_START", Boolean.TRUE);
                    } else if (loop == animDelay) {
                        player.unlock();
                        for (Player players : instance.getPlayersOnBattle()) {
                            if (players == null || players.isDead())
                                continue;
                            Dialogue.closeNoContinueDialogue(players);
                        }
                        jump();
                    } else if (loop == animDelay + 3) {
                        for (Player players : instance.getPlayersOnBattle()) {
                            if (players == null || players.isDead())
                                continue;
                            players.unlock();
                        }
                        sentDeath = false;
                        startNextPhase();
                        stop();
                    }
                    loop++;
                }
            }, 0, 1);
        } else {
            final NPCCombatDefinition defs = getCombatDefinitions();
            final int deathDelay = defs.getDeathDelay() - 4;
            WorldTasksManager.schedule(new WorldTask() {
                int loop;

                @Override
                public void run() {
                    if (loop == 0) {
                        setNextAnimation(new Animation(defs.getDeathEmote()));
                        setNextGraphics(new Graphics(4036));
                        player.setNextAnimation(new Animation(20387));
                        if (hardMode)
                            player.setHasMauledWeekHM(Settings.VORAGO_ROTATION);
                        else
                            player.setHasMauledWeekNM(Settings.VORAGO_ROTATION);
                        player.getInventory().deleteItem(28606, 1);
                    } else if (loop >= deathDelay) {
                        sendDrops();
                        instance.spawnExitSphere();
                        reset();
                        finish();
                        for (Player players : instance.getPlayersOnBattle()) {
                            if (players == null || players.isDead())
                                continue;
                            players.increaseKillStatistics("vorago", true);
                            ContractHandler.updateContract(players, Vorago.this);
                            HybridTokenDistributor.rollForToken(players, HybridTokenDistributor.Activity.VORAGO);
                            players.unlock();
                        }
                        stop();
                    }
                    loop++;
                }

            }, 0, 1);
        }
    }

    public static final int[] CHARMS = {12158, 12159, 12160, 12163};

    public void sendDrops() {
        boolean hardMode = instance.getSettings().isHardMode();
        int dropsCount = hardMode ? 10 : 5;
        NPCDrop[] drops = GodwarsInstance.getDrops(this) != null ? GodwarsInstance.getDrops(this) : NPCDropsDataParser.getDrops(id);
        for (int i = 0; i < dropsCount; i++) {
            Player phaseKiller = getPhaseKiller(i);
            List<NPCDrop> dropL = new ArrayList<NPCDrop>();
            final NPCDrop[] possibleDrops = new NPCDrop[drops.length];
            int possibleDropsCount = 0;
            for (final NPCDrop drop : drops) {
                if (drop == null)
                    continue;
                if (drop.getRate() == 100) {
                    dropL.add(drop);
                } else {
                    double rate = drop.getRate();
                    final double random = Utils.getRandomDouble(ContractHandler.isContractNpc(phaseKiller, this) ? 95 : 100);
                    if (rate < 30) {
                        rate *= Settings.getDropQuantityRate(phaseKiller);
                    }
                    if (random <= rate && random != 100 && random != 0) {
                        possibleDrops[possibleDropsCount++] = drop;
                    }
                }
            }
            if (possibleDropsCount > 0) {
                dropL.add(possibleDrops[Utils.getRandom(possibleDropsCount - 1)]);
            }
            if (!dropL.isEmpty() && Utils.random(8 / getSize()) == 0) {
                dropL.add(new NPCDrop(CHARMS[Utils.random(CHARMS.length)], 100, 1, getSize()));
            }
            phaseDrops.add(i, dropL);
        }
        phaseDrops = checkDrops(phaseDrops);
        WorldTile[] normalModeLocations = {getTile(new WorldTile(3111, 5985, 0)), getTile(new WorldTile(3113, 5985, 0)), getTile(new WorldTile(3112, 5984, 0)), getTile(new WorldTile(3111, 5983, 0)), getTile(new WorldTile(3113, 5983, 0))};
        WorldTile[] hardModeLocations = {getTile(new WorldTile(3092, 5985, 0)), getTile(new WorldTile(3094, 5985, 0)), getTile(new WorldTile(3093, 5984, 0)), getTile(new WorldTile(3092, 5983, 0)), getTile(new WorldTile(3094, 5983, 0)), getTile(new WorldTile(3096, 5985, 0)), getTile(new WorldTile(3098, 5985, 0)), getTile(new WorldTile(3097, 5984, 0)), getTile(new WorldTile(3096, 5983, 0)), getTile(new WorldTile(3098, 5983, 0))};
        for (int i = 0; i < dropsCount; i++) {
            WorldTile loc = hardMode ? hardModeLocations[i] : normalModeLocations[i];
            List<Player> players = FriendChatsManager.getLootSharingPeople(getPhaseKiller(i), this);
            List<NPCDrop> dropL = phaseDrops.get(i);
            if (players == null || players.size() == 1) {
                sendDrops(getPhaseKiller(i), dropL, loc);
            } else {
                skip:
                for (NPCDrop drop : dropL) {
                    Player luckyPlayer = players.get(Utils.random(players.size()));
                    while (true) {
                        if (luckyPlayer != null && luckyPlayer.getRights() < 2)
                            break;
                        luckyPlayer = players.get(Utils.random(players.size()));
                    }
                    boolean hasBonecrusher = luckyPlayer.getInventory().containsOneItem(18337);
                    boolean hasHerbicide = luckyPlayer.getInventory().containsOneItem(19675);
                    if (hasBonecrusher) {
                        Bone bone = Bone.forId(drop.getItemId());
                        Item item = new Item(drop.getItemId(), (drop.getMinAmount()) + Utils.random(drop.getExtraAmount()));
                        if (bone != null) {
                            luckyPlayer.getSkills().addXp(Skills.PRAYER, bone.getExperience() * item.getAmount());
                            luckyPlayer.getPackets().sendGameMessage("<col=00FF00>You received: " + item.getAmount() + " " + item.getName() + ".");
                            for (Player p2 : players) {
                                if (p2 == luckyPlayer)
                                    continue;
                                p2.getPackets().sendGameMessage("<col=66FFCC>" + luckyPlayer.getDisplayName() + "</col> received: " + item.getAmount() + " " + item.getName() + ".");
                                p2.getPackets().sendGameMessage("Your chance of receiving loot has improved.");
                            }
                            continue skip;
                        }
                    }
                    if (hasHerbicide) {
                        final Herbs herb = HerbCleaning.getHerb(drop.getItemId());
                        Item item = new Item(drop.getItemId(), (drop.getMinAmount()) + Utils.random(drop.getExtraAmount()));
                        if (herb != null && luckyPlayer.getSkills().getLevel(Skills.HERBLORE) >= herb.getLevel()) {
                            luckyPlayer.getSkills().addXp(Skills.HERBLORE, herb.getExperience() * 2 * item.getAmount());
                            luckyPlayer.getPackets().sendGameMessage("<col=00FF00>You received: " + item.getAmount() + " " + item.getName() + ".");
                            for (Player p2 : players) {
                                if (p2 == luckyPlayer)
                                    continue;
                                p2.getPackets().sendGameMessage("<col=66FFCC>" + luckyPlayer.getDisplayName() + "</col> received: " + item.getAmount() + " " + item.getName() + ".");
                                p2.getPackets().sendGameMessage("Your chance of receiving loot has improved.");
                            }
                            continue skip;
                        }
                    }
                    if (luckyPlayer.getTreasureTrails().isScroll(drop.getItemId())) {
                        if (luckyPlayer.getTreasureTrails().hasClueScrollItem())
                            continue skip;
                        luckyPlayer.getTreasureTrails().resetCurrentClue();
                    }
                    Item item = sendSingleDrop(luckyPlayer, drop, loc);
                    if (item == null)
                        continue skip;
                    luckyPlayer.getPackets().sendGameMessage("<col=00FF00>You received: " + item.getAmount() + " " + item.getName() + ".");
                    for (Player p2 : players) {
                        if (p2 == luckyPlayer)
                            continue;
                        p2.getPackets().sendGameMessage("<col=66FFCC>" + luckyPlayer.getDisplayName() + "</col> received: " + item.getAmount() + " " + item.getName() + ".");
                        p2.getPackets().sendGameMessage("Your chance of receiving loot has improved.");
                    }
                }
            }
        }
    }

    public List<List<NPCDrop>> checkDrops(List<List<NPCDrop>> phaseDrops) {
        boolean hardMode = instance.getSettings().isHardMode();
        int dropsCount = hardMode ? 10 : 5;
        List<List<NPCDrop>> d = new ArrayList<List<NPCDrop>>();
        int seismicCount = 0;
        for (int i = 0; i < dropsCount; i++) {
            List<NPCDrop> drops = phaseDrops.get(i);
            if (Utils.random(2) == 0)
                drops.add(new NPCDrop(28627, 100, 2, 2));
            for (int j = 0; j < drops.size(); j++) {
                NPCDrop drop = drops.get(j);
                if (drop == null)
                    continue;
                String dropName = ItemDefinitions.getItemDefinitions(drop.getItemId()).getName().toLowerCase();
                if (dropName.contains("seismic")) {
                    if (seismicCount > 0)
                        drops.set(j, new NPCDrop(1748, 100, 35, 55));
                    else
                        seismicCount++;
                }
                if (drop.getItemId() == 33716 && !hardMode)
                    drops.set(j, new NPCDrop(1748, 100, 35, 55));
            }
            d.add(drops);
        }
        return d;
    }

    public void sendDrops(Player player, List<NPCDrop> dropL, WorldTile location) {
        boolean hasBonecrusher = player.getInventory().containsOneItem(18337);
        boolean hasHerbicide = player.getInventory().containsOneItem(19675);
        for (NPCDrop drop : dropL) {
            if (hasBonecrusher) {
                Bone bone = Bone.forId(drop.getItemId());
                if (bone != null) {
                    player.getSkills().addXp(Skills.PRAYER, bone.getExperience());
                    continue;
                }
            }
            if (hasHerbicide) {
                final Herbs herb = HerbCleaning.getHerb(drop.getItemId());
                if (herb != null && player.getSkills().getLevel(Skills.HERBLORE) >= herb.getLevel()) {
                    player.getSkills().addXp(Skills.HERBLORE, herb.getExperience() * 2);
                    continue;
                }
            }
            if (player.getTreasureTrails().isScroll(drop.getItemId())) {
                if (player.getTreasureTrails().hasClueScrollItem())
                    continue;
                player.getTreasureTrails().resetCurrentClue();
            }
            sendSingleDrop(player, drop, location);
        }
    }

    private Item sendSingleDrop(Player player, NPCDrop drop, WorldTile location) {
        String name = ItemDefinitions.getItemDefinitions(drop.getItemId()).getName().toLowerCase();

        if (name.contains("seismic") || name.contains("stone")) {
            World.sendNews(player, player.getDisplayName() + " has received " + ItemDefinitions.getItemDefinitions(drop.getItemId()).getName() + " drop!", World.WORLD_NEWS);
        }
        boolean stackable = ItemDefinitions.getItemDefinitions(drop.getItemId()).isStackable();
        Item item = stackable ? new Item(drop.getItemId(), (drop.getMinAmount()) + Utils.random(drop.getExtraAmount())) : new Item(drop.getItemId(), drop.getMinAmount() + Utils.random(drop.getExtraAmount()));
        player.getDropCollectionHandler().handleBossKills(item, DropCollectionConstants.RAGO_ID);
        if (player.isDiamondDonor() && player.isNotingDrops()) {
            val definitions = ItemDefinitions.getItemDefinitions(item.getId());
            if (definitions != null && !definitions.isNoted() && definitions.getCertId() != -1) {
                item.setId(definitions.getCertId());
            }
        }
        if (!stackable && item.getAmount() > 1) {
            for (int i = 0; i < item.getAmount(); i++)
                World.addGroundItem(new Item(item.getId(), 1), location, player, true, 60);
        } else
            World.addGroundItem(item, location, player, true, 60);
        return item;
    }

    private Player getPhaseKiller(int index) {
        Player phaseKiller = phaseKillers.get(index);
        if (phaseKiller == null || !instance.playerIsOnBattle(phaseKiller)) {
            ArrayList<Player> available = new ArrayList<>();
            for (int i = 0; i < instance.getPlayersOnBattle().size(); i++) {
                Player player = instance.getPlayersOnBattle().get(i);
                if (player == null || player.isDead())
                    continue;
                available.add(player);
            }
            Collections.shuffle(available);
            return available.get(Utils.random(available.size()));
        }
        return phaseKiller;
    }

    private void generatePhaseKiller() {
        int phase = getPhase() - 1;
        int luckyType = Utils.random(4);
        Player player = null;
        switch (luckyType) {
            case 0:// By last agro
                player = (Player) getCombat().getTarget();
                break;
            case 1:// By DPS
                int lowestDamage = 0;
                int index = 0;
                for (int i = 0; i < instance.getPlayersOnBattle().size(); i++) {
                    Player DPS = instance.getPlayersOnBattle().get(i);
                    if (DPS == null || DPS.isDead())
                        continue;
                    int TotalDamage = DPS.getTemporaryAttributtes().get("TotalDamage") == null ? 0 : (int) DPS.getTemporaryAttributtes().get("TotalDamage");
                    if (TotalDamage > lowestDamage) {
                        lowestDamage = TotalDamage;
                        index = i;
                    }
                }
                player = instance.getPlayersOnBattle().get(index);
                break;
            case 2:// By blue bombs
            case 3:
                int lowestAmount = 0;
                index = 0;
                for (int i = 0; i < instance.getPlayersOnBattle().size(); i++) {
                    Player bombTank = instance.getPlayersOnBattle().get(i);
                    if (bombTank == null || bombTank.isDead())
                        continue;
                    int blues = bombTank.getTemporaryAttributtes().get("BlueBombs") == null ? 0 : (int) bombTank.getTemporaryAttributtes().get("BlueBombs");
                    if (blues > lowestAmount) {
                        lowestAmount = blues;
                        index = i;
                    }
                }
                player = instance.getPlayersOnBattle().get(index);
                break;
        }
        if (player == null) {// shouldn't happen but okay random
            ArrayList<Player> available = new ArrayList<>();
            for (int i = 0; i < instance.getPlayersOnBattle().size(); i++) {
                Player p = instance.getPlayersOnBattle().get(i);
                if (p == null || p.isDead())
                    continue;
                available.add(p);
            }
            Collections.shuffle(available);
            player = available.get(Utils.random(available.size()));
        }
        phaseKillers.add(phase, player);
        for (Player reset : instance.getPlayersOnBattle()) {
            if (reset == null || reset.isDead())
                continue;
            reset.getTemporaryAttributtes().remove("BlueBombs");
            reset.getTemporaryAttributtes().remove("TotalDamage");
        }
    }

    @Override
    public boolean isDead() {// TODO put a method in phase 5 to set dead
        return false;
    }

    @Override
    public boolean restoreHitPoints() {
        int maxHp = getMaxHitpoints();
        if (getHitpoints() < maxHp && getHitpoints() != 0) {
            setHitpoints(getHitpoints() + 1);
            instance.updateInterface(false);
            return true;
        }
        return false;
    }

    @Override
    public boolean checkAgressivity() {
        boolean hardMode = instance.getSettings().isHardMode();
        if (getTemporaryAttributtes().get("BringHimDownClick") != null || getTemporaryAttributtes().get("TheEnd") != null) {
            if (getNextFaceEntity() != -1)
                setNextFaceEntity(null);
            return false;
        }
        if (sentDeath)
            return false;
        if (getTemporaryAttributtes().get("CantBeAttacked") != null) {
            if ((Settings.VORAGO_ROTATION == 1 && phase == 3 && !hardMode) || (hardMode && phase == 4)) {
                ArrayList<Entity> possibleTarget = getPossibleTargets();
                if (!possibleTarget.isEmpty()) {
                    Entity target = possibleTarget.get(Utils.random(possibleTarget.size()));
                    setTarget(target);
                    target.setAttackedBy(target);
                    target.setFindTargetDelay(Utils.currentTimeMillis() + 10000);
                    return true;
                }
                return false;
            }
            if (getNextFaceEntity() != -1)
                setNextFaceEntity(null);
            return false;
        }
        ArrayList<Entity> possibleTarget = getPossibleTargets();
        if (!possibleTarget.isEmpty()) {
            Entity target = possibleTarget.get(Utils.random(possibleTarget.size()));
            setTarget(target);
            target.setAttackedBy(target);
            target.setFindTargetDelay(Utils.currentTimeMillis() + 10000);
            return true;
        }
        return false;
    }

    @Override
    public ArrayList<Entity> getPossibleTargets() {
        return getPossibleTargets(false, true);
    }

    public ArrayList<Entity> getPossibleTargets(boolean checkNPCs, boolean checkPlayers) {
        ArrayList<Entity> possibleTarget = new ArrayList<Entity>();
        if (instance == null || instance.getPlayersOnBattle() == null)
            return possibleTarget;
        for (Player player : instance.getPlayersOnBattle()) {
            if (player == null || player.isDead() || player.hasFinished())
                continue;
            possibleTarget.add(player);
        }
        return possibleTarget;
    }

    @Override
    public void setTarget(Entity entity) {
        boolean hardMode = instance.getSettings().isHardMode();
        if (getTemporaryAttributtes().get("TheEnd") != null)
            return;
        if (sentDeath) {
            getCombat().removeTarget();
            getCombat().reset();
            return;
        }
        if (getTemporaryAttributtes().get("CantBeAttacked") != null) {
            if ((Settings.VORAGO_ROTATION == 1 && phase == 3 && !hardMode) || (hardMode && phase == 4)) {
                super.setTarget(entity);
                return;
            }
            return;
        }
        super.setTarget(entity);
    }

    public void finishPhase() {
        for (Player player : instance.getPlayersOnBattle()) {
            if (player == null || player.isDead())
                continue;
            player.getTemporaryAttributtes().remove("Suffocating");
        }
        WorldTasksManager.schedule(new WorldTask() {
            int count = 0;

            @Override
            public void run() {
                switch (count) {
                    case 0:
                    case 5:
                    case 11:
                        jump();
                        break;
                    case 17:
                        setNextAnimation(new Animation(20365));
                        removeMist();
                        sendCrackGround();
                        break;
                    case 18:
                        startNextPhase();
                        sentDeath = false;
                        stop();
                        break;
                }
                count++;
            }
        }, 0, 1);
    }

    public void startNextPhase() {
        boolean hardMode = instance.getSettings().isHardMode();
        phase++;
        generateAttackList();
        removeLeftOvers();
        if (hardMode && phase == 11) {
            resetOnPhaseStart();
            for (Player player : instance.getPlayersOnBattle()) {
                if (player == null || player.isDead())
                    continue;
                WorldTile tile = instance.getSimilarCoords(player, phase);
                if (tile != null) {
                    player.setNextWorldTile(tile);
                }
                player.getTemporaryAttributtes().remove("ENDING_PHASE_START");
            }
        } else {
            for (Player player : instance.getPlayersOnBattle()) {
                if (player != null)
                    player.setNextAnimation(new Animation(20402));
            }
            WorldTasksManager.schedule(new WorldTask() {
                int count = 0;

                @Override
                public void run() {
                    switch (count) {
                        case 1:
                            for (Player player : instance.getPlayersOnBattle()) {
                                if (player == null)
                                    continue;
                                WorldTile tile = instance.getSimilarCoords(player, phase);
                                if (tile != null) {
                                    player.setNextAnimation(new Animation(20401));
                                    player.setNextWorldTile(tile);
                                }
                                if (phase == 5 || phase == 10)
                                    player.getTemporaryAttributtes().put("ENDING_PHASE_START", Boolean.TRUE);
                            }
                            break;
                        case 4:
                            if (phase == 4 && !hardMode)
                                for (WorldObject ceiling : ceilingCollapses) {
                                    if (ceiling == null)
                                        continue;
                                    WorldTile tile = instance.getSimilarCoords(new WorldTile(ceiling.getX(), ceiling.getY(), ceiling.getPlane()), phase);
                                    ceiling.setLocation(tile);
                                    spawnCeilingCollapse(tile, ceiling);
                                }
                            break;
                        case 6:
                            moveWeaponPieces();
                            break;
                        case 10:
                            WorldTile phaseStartLocation = instance.getVoragoSpawnLocation(phase);
                            if (phase == 4 && !hardMode)
                                removeMiddleCeilingObject();
                            sendCrashDownAttack(phaseStartLocation);
                            if (phase == 5 || phase == 10) {
                                for (Player player : instance.getPlayersOnBattle()) {
                                    if (player == null || player.isDead())
                                        continue;
                                    player.getTemporaryAttributtes().remove("ENDING_PHASE_START");
                                }
                            }
                            break;
                        case 11:
                            stop();
                            break;
                    }
                    count++;
                }
            }, 0, 1);
        }
    }

    public void removeMist() {
        for (int i = 0; i < mists.length; i++) {
            WorldObject mist = mists[i];
            if (mist == null)
                continue;
            if (World.containsObjectWithId(mist, mist.getId()))
                World.removeObject(mist);
            mists[i] = null;
        }
    }

    public void removeLeftOvers() {
        removeLeftOvers(false);
    }

    public void removeLeftOvers(boolean finished) {
        boolean hardMode = instance.getSettings().isHardMode();
        if (gravityField != null) {
            if (World.containsObjectWithId(gravityField, gravityField.getId()))
                World.removeObject(gravityField);
            gravityField = null;
        }
        for (int i = 0; i < ceilingCollapses.length; i++) {
            WorldObject ceiling = ceilingCollapses[i];
            if (ceiling == null)
                continue;
            if (World.containsObjectWithId(ceiling, ceiling.getId()))
                World.removeObject(ceiling);
            if ((phase == 5 && !finished) || hardMode)
                ceilingCollapses[i] = null;
        }
        for (int i = 0; i < teamSplitSquares.length; i++) {
            WorldObject square = teamSplitSquares[i];
            if (square == null)
                continue;
            if (World.containsObjectWithId(square, square.getId()))
                World.removeObject(square);
            teamSplitSquares[i] = null;
        }
        if (waterFall != null) {
            if (World.containsObjectWithId(waterFall, waterFall.getId()))
                World.removeObject(waterFall);
            waterFall = null;
        }
        removeMist();
        for (int i = 0; i < theEndBombs.length; i++) {
            TheEndBomb bomb = theEndBombs[i];
            if (bomb == null)
                continue;
            if (World.containsObjectWithId(bomb, bomb.getId()))
                World.removeObject(bomb);
            theEndBombs[i] = null;
        }
        for (int i = 0; i < stoneClones.length; i++) {
            StoneClone clone = stoneClones[i];
            if (clone == null || clone.isDead())
                continue;
            clone.sendDeath(null);
            stoneClones[i] = null;
        }
        for (int i = 0; i < vitali.length; i++) {
            Vitalis vitalis = vitali[i];
            if (vitalis == null || vitalis.isDead())
                continue;
            vitalis.sendDeath(null);
            vitali[i] = null;
        }
        for (int i = 0; i < scopuli.length; i++) {
            Scopulus scop = scopuli[i];
            if (scop == null || scop.isDead())
                continue;
            scop.sendDeath(null);
            scopuli[i] = null;
        }
        unWalkAbleSpots.clear();
    }

    public void resetOnPhaseStart() {
        boolean hardMode = instance.getSettings().isHardMode();
        setHitpoints(getMaxHitpoints());
        reset();
        setCantInteract(false);
        setForceFollowClose(false);
        setTargetedPlayer(null);
        getCombat().setCombatDelay(0);
        phaseProgress = 0;
        if (phase != 11)
            attackProgress = 0;
        switch (phase) {
            case 2:
                getTemporaryAttributtes().put("VoragoType", 1);
                transform();
                break;
            case 3:
                if (hardMode) {
                    getTemporaryAttributtes().put("VoragoType", 1);
                    transform();
                    break;
                }
                switch (Settings.VORAGO_ROTATION) {
                    case 1:// Scopulus
                        getTemporaryAttributtes().put("VoragoType", 1);
                        getTemporaryAttributtes().put("CantBeAttacked", "Magical Force prevents you from attacking vorago, Try killing his minions instead.");
                        transform();
                        break;
                    case 0:// ceiling
                    case 2:// Vitalis
                    case 3:// Green Bomb
                        getTemporaryAttributtes().put("VoragoType", 1);
                        transform();
                        break;
                    case 4:// Team Split
                    case 5:// The End
                        getTemporaryAttributtes().put("CantBeAttackedOnPhaseStart", Boolean.TRUE);
                        getTemporaryAttributtes().put("VoragoType", 1);
                        transform();
                        break;
                }
                break;
            case 4:
                if (hardMode) {// scop
                    getTemporaryAttributtes().put("VoragoType", 1);
                    getTemporaryAttributtes().put("CantBeAttacked", "Magical Force prevents you from attacking vorago, Try killing his minions instead.");
                    transform();
                    break;
                }
                getTemporaryAttributtes().put("CantBeAttackedOnPhaseStart", Boolean.TRUE);
                getTemporaryAttributtes().put("VoragoType", 1);
                transform();
                break;
            case 5:
                if (hardMode) {// vitalis
                    getTemporaryAttributtes().put("VoragoType", 1);
                    transform();
                    break;
                }
                if (Settings.VORAGO_ROTATION == 4)
                    getTemporaryAttributtes().put("CantBeAttackedOnPhaseStart", Boolean.TRUE);
                getTemporaryAttributtes().put("VoragoType", 1);
                transform();
                break;
            case 6:// green bomb (hm)
                getTemporaryAttributtes().put("VoragoType", 1);
                transform();
                break;
            case 7:// teamsplit
                getTemporaryAttributtes().put("CantBeAttackedOnPhaseStart", Boolean.TRUE);
                getTemporaryAttributtes().put("VoragoType", 1);
                transform();
                break;
            case 8:// The End
                getTemporaryAttributtes().put("CantBeAttackedOnPhaseStart", Boolean.TRUE);
                getTemporaryAttributtes().put("VoragoType", 1);
                transform();
                break;
            case 9:
                getTemporaryAttributtes().put("CantBeAttackedOnPhaseStart", Boolean.TRUE);
                getTemporaryAttributtes().put("VoragoType", 1);
                transform();
                break;
            case 10:
                if (Settings.VORAGO_ROTATION == 4 || Settings.VORAGO_ROTATION == 0)
                    getTemporaryAttributtes().put("CantBeAttackedOnPhaseStart", Boolean.TRUE);
                getTemporaryAttributtes().put("VoragoType", 1);
                transform();
                break;
        }
        instance.updateInterface(false);
    }

    public void moveWeaponPieces() {
        for (int i = 0; i < weaponPieces.length; i++) {
            FloorItem weaponPiece = weaponPieces[i];
            if (weaponPiece == null)
                continue;
            World.removeGroundItem(weaponPiece);
            weaponPieces[i] = World.addWeaponPiece(new Item(weaponPiece.getId()), instance.getSimilarCoords(weaponPiece.getTile(), phase));
        }
    }

    public void transform() {
        int type = (int) getTemporaryAttributtes().get("VoragoType");
        switch (type) {
            case 0:
                setCantFollowUnderCombat(false);
                break;
            case 1:
                setCantFollowUnderCombat(true);
                break;
            case 2:
                setCantFollowUnderCombat(true);
                break;
        }
        if (getId() != VORAGOES[type]) {
            setNextNPCTransformation(VORAGOES[type]);
            getWalkSteps().clear();
        }
    }

    public void sendCrashDownAttack(WorldTile crashDownTile) {
        resetOnPhaseStart();
        setNextWorldTile(crashDownTile);
        setNextAnimation(new Animation(20367, 20367, 20367, -1, 0));
        for (Player player : instance.getPlayersOnBattle()) {
            if (player == null || player.isDead() || !Utils.colides(player.getX(), player.getY(), player.getSize(), crashDownTile.getX(), crashDownTile.getY(), getSize()))
                continue;
            player.lock();
            player.setNextAnimation(new Animation(20338));
            WorldTile toTile = getPushedBackWorldTile(player, crashDownTile, getMoveDirection(player));
            player.setNextForceMovement(new ForceMovement(toTile, 1, getMoveDirection(player)));
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    player.unlock();
                    player.setNextWorldTile(toTile);
                }
            }, (AnimationDefinitions.getAnimationDefinitions(20338).getEmoteClientCycles() / 30));
        }
        instance.updateInterface(true);
    }

    public WorldTile getPushedBackWorldTile(WorldTile startLocation, WorldTile voragoLocation, int playerDirection) {
        int moveX = (voragoLocation.getX() - startLocation.getX()) + 5;
        int moveY = (voragoLocation.getY() - startLocation.getY()) + 5;
        switch (playerDirection) {
            case ForceMovement.NORTH_EAST:// north-east
                return new WorldTile(startLocation.getX() - moveX, startLocation.getY() - moveY, startLocation.getPlane());
            case ForceMovement.NORTH_WEST:// north-west
                return new WorldTile(startLocation.getX() + moveX, startLocation.getY() - moveY, startLocation.getPlane());
            case ForceMovement.SOUTH_WEST:// south-west
                return new WorldTile(startLocation.getX() + moveX, startLocation.getY() + moveY, startLocation.getPlane());
            case ForceMovement.SOUTH_EAST:// south-east
                return new WorldTile(startLocation.getX() - moveX, startLocation.getY() + moveY, startLocation.getPlane());
            case ForceMovement.WEST:// west
                return new WorldTile(startLocation.getX() + moveX, startLocation.getY(), startLocation.getPlane());
            case ForceMovement.EAST:// east
                return new WorldTile(startLocation.getX() - moveX, startLocation.getY(), startLocation.getPlane());
            case ForceMovement.NORTH:// north
                return new WorldTile(startLocation.getX(), startLocation.getY() - moveY, startLocation.getPlane());
            case ForceMovement.SOUTH:// south
            default:
                return new WorldTile(startLocation.getX(), startLocation.getY() + moveY, startLocation.getPlane());
        }
    }

    // north-west 0, south-east 0, north-east 1, south-west 1
    // Center phase 4 3038, 5982, 0

    // { -10, 10 } west east
    // { 10, -10 } north south

    public void sendWaterFallAttack() {
        if (getTemporaryAttributtes().get("CantBeAttackedOnPhaseStart") != null)
            getTemporaryAttributtes().remove("CantBeAttackedOnPhaseStart");
        boolean hardMode = instance.getSettings().isHardMode();
        WorldTile center = instance.getVoragoSpawnLocation(hardMode ? 9 : 4);
        WorldTile waterFallLocation = new WorldTile((Utils.random(3) == 0 ? (center.getX() - 10) : (center.getX() + 10)), (Utils.random(3) == 0 ? (center.getY() + 10) : (center.getY() - 10)), center.getPlane());
        boolean northWest = ((waterFallLocation.getX() == (center.getX() - 10)) && (waterFallLocation.getY() == (center.getY() + 10)));
        boolean southEast = ((waterFallLocation.getX() == (center.getX() + 10)) && (waterFallLocation.getY() == (center.getY() - 10)));
        boolean northEast = ((waterFallLocation.getX() == (center.getX() + 10)) && (waterFallLocation.getY() == (center.getY() + 10)));
        int rotation = northWest ? 0 : northEast ? 1 : southEast ? 2 : 3;// south-west
        waterFall = new WorldObject(84967, 11, rotation, waterFallLocation);
        List<WorldTile> safeLocations = new ArrayList<WorldTile>();
        int rot = rotation;
        int xstart = (rot == 2 || rot == 1) ? waterFallLocation.getX() : (waterFallLocation.getX() + 3);
        int ystart = (rot == 0 || rot == 1) ? (waterFallLocation.getY() + 3) : waterFallLocation.getY();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < (4 - i); j++) {
                WorldTile tile = new WorldTile((rot == 2 || rot == 1) ? (xstart + j) : (xstart - j), (rot == 0 || rot == 1) ? (ystart - j) : (ystart + j), 1);
                safeLocations.add(tile);
            }
            xstart = (rot == 2 || rot == 1) ? (xstart + 1) : (xstart - 1);
        }
        if (Settings.VORAGO_ROTATION == 0 && !hardMode)
            removeMiddleCeilingObject();
        setNextWorldTile(center);
        canBeAttackedByAutoRelatie();
        setNextAnimation(new Animation(20322));
        getTemporaryAttributtes().put("CantBeAttacked", "Vorago is invulnerable as he charges a massive fire attack!");
        getTemporaryAttributtes().put("waterfall", Boolean.TRUE);
        World.spawnObject(waterFall);
        for (Player player : instance.getPlayersOnBattle()) {
            if (player == null || player.isDead())
                continue;
            player.getPackets().sendGameMessage("Vorago's raw power disrupts your defensive abilities!");
            getVoragoInstance().sendMessage(player, 1, "<col=ff0000>Vorago starts to charge all his power into a massive fire attack!");
//			player.getInterfaceManager().setFadingInterface(1492);
        }
        for (int i = 0; i < stoneClones.length; i++) {
            StoneClone clone = stoneClones[i];
            if (clone == null)
                continue;
            clone.sendDeath(clone.getTarget());
        }
        Entity lastTarget = getCombat().getTarget();
        getCombat().removeTarget();
        cancelFaceEntityNoCheck();
        Vorago thisNPC = this;
        getTemporaryAttributtes().put("canMoveUnderRago", Boolean.TRUE);
        WorldTasksManager.schedule(new WorldTask() {
            int count = 0;

            @Override
            public void run() {
                if (sentDeath) {
                    getTemporaryAttributtes().remove("waterfall");
                    World.removeObject(waterFall);
                    stop();
                    getTemporaryAttributtes().remove("canMoveUnderRago");
                    return;
                }
                switch (count) {
                    case 7:
                        setNextAnimation(new Animation(20323));
                        break;
                    case 8:
                        setNextGraphics(new Graphics(4013));
                        setNextAnimation(new Animation(20323));
                        getTemporaryAttributtes().remove("CantBeAttacked");
                        getTemporaryAttributtes().remove("waterfall");
                        if (waterFall != null && World.containsObjectWithId(waterFall, waterFall.getId()))
                            World.removeObject(waterFall);
                        phaseProgress = phaseProgress + 1 >= 3 ? 3 : phaseProgress + 1;
                        instance.updateInterface(false);
                        for (Player player : instance.getPlayersOnBattle()) {
                            if (player == null || player.isDead())
                                continue;
                            if (!isSafe(new WorldTile(player.getX(), player.getY(), player.getPlane()), safeLocations))
                                player.applyHit(new Hit(thisNPC, (hardMode ? 1000 : 900), HitLook.REGULAR_DAMAGE));
                            // player.getInterfaceManager().closeFadingInterface();
                        }
                        if (phaseProgress == 3) {
                            WorldTile tile = instance.randomSpawnTile(null, instance.getVoragoSpawnLocation(phase), 5);
                            weaponPieces[2] = World.addWeaponPiece(new Item(28604), tile);
                        }
                        getTemporaryAttributtes().remove("canMoveUnderRago");
                        waterFall = null;
                        getCombat().setTarget(lastTarget);
                        stop();
                        break;
                }
                count++;
            }
        }, 0, 1);
    }

    public boolean isSafe(WorldTile player, List<WorldTile> safeLocations) {
        for (WorldTile checkTile : safeLocations) {
            if (checkTile == null)
                continue;
            if (checkTile.getX() == player.getX() && checkTile.getY() == player.getY())
                return true;
        }
        return false;
    }

    public boolean cantBeAutoRetaliated() {// TODO in playerCombatNew
        return getTemporaryAttributtes().get("BringHimDownClick") != null || getTemporaryAttributtes().get("CantBeAttackedOnPhaseStart") != null || getTemporaryAttributtes().get("TheEnd") != null || getTemporaryAttributtes().get("CantBeAttacked") != null;
    }

    public void spawnStoneClones() {
        boolean hardMode = instance.getSettings().isHardMode();
        double maxStoneClones = Math.ceil((double) getPlayerOnBattleCount() / 7.00);
        if (hardMode)
            maxStoneClones *= 2;
        if (maxStoneClones > stoneClones.length)
            maxStoneClones = stoneClones.length;
        for (int i = 0; i < maxStoneClones; i++) {
            Player target = getRandomCloneTarget();
            if (target == null)
                continue;
            int combatType = target.getCombatDefinitions().getType(Equipment.SLOT_WEAPON);
            int id = combatType == Combat.MAGIC_TYPE ? 17160 : combatType == Combat.RANGE_TYPE ? 17159 : 17158;
            WorldTile spawnTile = instance.randomSpawnTile(null, new WorldTile(getX(), getY(), getPlane()), 4);
            stoneClones[i] = new StoneClone(id, spawnTile, -1, true, this, target);
            stoneClones[i].setTarget(target);
            target.getHintIconsManager().addHintIcon(stoneClones[i], 0, -1, false);
        }
    }

    public void sendCloneDeath(Player player) {
        for (int i = 0; i < stoneClones.length; i++) {
            StoneClone clone = stoneClones[i];
            if (clone == null)
                continue;
            if (clone.getTarget() == player)
                stoneClones[i] = null;
        }
    }

    public Player getRandomCloneTarget() {
        List<Player> availableIndexes = new ArrayList<Player>();
        for (int i = 0; i < getVoragoInstance().getPlayersOnBattle().size(); i++) {
            Player player = getVoragoInstance().getPlayersOnBattle().get(i);
            if (player == null || player.isDead() || (player == getCombat().getTarget() && getPlayerOnBattleCount() > 1) || hasClone(player))
                continue;
            availableIndexes.add(player);
        }
        return availableIndexes.isEmpty() ? null : availableIndexes.get(Utils.random(availableIndexes.size()));
    }

    public boolean hasClone(Player player) {
        for (StoneClone stoneClone : stoneClones) {
            if (stoneClone == null || stoneClone.isDead())
                continue;
            if (stoneClone.getTarget() == player)
                return true;
        }
        return false;
    }

    public void sendBringHimDown() {
        getTemporaryAttributtes().put("BringHimDownClick", Boolean.TRUE);
        setCantDoDefenceEmote(true);
        setNextAnimation(new Animation(20382));
        instance.removeHealthBar();
        damageTime = Utils.currentTimeMillis() + 180000;// 3mins
        for (Player player : instance.getPlayersOnBattle()) {
            if (player == null || player.isDead())
                continue;
            player.stopAll();
            player.faceEntity(this);
//			player.getPackets().sendCSVarInteger(2770, getBringHimPoints());
            player.getTemporaryAttributtes().put("BRINGHIM", Boolean.TRUE);
            player.setNextAnimation(getBringHimDownAnimation());
//			player.getInterfaceManager().setFadingInterface(1413);
        }
    }

    public void processBringHimDown() {
        if (getBringHimPoints() >= 200 && getTemporaryAttributtes().get("BringHimDownClick") == Boolean.TRUE) {
            getTemporaryAttributtes().put("BringHimDownClick", Boolean.FALSE);
            setNextAnimation(new Animation(20383));
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    getTemporaryAttributtes().put("VoragoType", 2);
                    transform();
                    finishBringHimDown();
                }
            }, (AnimationDefinitions.getAnimationDefinitions(20383).getEmoteClientCycles() / 30));
            for (Player player : getVoragoInstance().getPlayersOnBattle()) {
                if (player == null)
                    continue;
                Vorago thisNPC = this;
                player.getTemporaryAttributtes().remove("BRINGHIM");
                player.lock();
                player.setNextAnimation(new Animation(20400));
                // player.getInterfaceManager().closeFadingInterface();
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        if (damageTime >= Utils.currentTimeMillis())
                            player.applyHit(new Hit(thisNPC, 200, HitLook.REGULAR_DAMAGE));
                        player.unlock();
                    }
                }, (AnimationDefinitions.getAnimationDefinitions(20400).getEmoteClientCycles() / 30) + 1);
            }
        }
    }

    public void finishBringHimDown() {
        for (Player player : instance.getPlayersOnBattle()) {
            if (player == null || player.isDead())
                continue;
            player.getPackets().sendOverlayMessage(1, "<col=00ff00>Vorago stumbles! Deal as much damage as possible to loosen the next weapon piece!", true);
        }
        int damageRequired = 1500 * getPlayerOnBattleCount();
        boolean hardMode = instance.getSettings().isHardMode();
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                setBringHimPoints(0);
                setNextAnimation(new Animation(20362));
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        getTemporaryAttributtes().remove("BringHimDownClick");
                        getTemporaryAttributtes().put("VoragoType", 1);
                        transform();
                        setCantSetTargetAutoRelatio(false);
                        setCantDoDefenceEmote(false);
                    }
                }, (AnimationDefinitions.getAnimationDefinitions(20362).getEmoteClientCycles() / 30));
                int finishType = (losenWeaponPieceStage >= 2 || damageWhileDown >= damageRequired) ? 2 : (damageWhileDown < (damageRequired / 2)) ? 1 : 0;
                for (Player player : instance.getPlayersOnBattle()) {
                    if (player == null || player.isDead())
                        continue;
                    // player.getPackets().sendCSVarInteger(2770, getBringHimPoints());
                    player.getPackets().sendOverlayMessage(1, (finishType == 2 ? "<col=00ff00>The weapon piece falls from Vorago's body!" : finishType == 1 ? "<col=ff0000>You didn't do enough damage in time. The weapon piece still looks secure." : "<col=FF6600>Your strong attacks helps loosen the weapon piece a little more."), true);
                }
                phaseProgress = finishType == 2 ? 5 : 2;
                instance.addHealthBar();
                instance.updateInterface(false);
                if (finishType == 2) {
                    WorldTile tile = instance.randomSpawnTile(null, instance.getVoragoSpawnLocation(phase), 5);
                    weaponPieces[1] = World.addWeaponPiece(new Item(28602), tile);
                } else if (finishType == 0) {
                    losenWeaponPieceStage++;
                }
                damageWhileDown = 0;
                stop();
            }
        }, hardMode ? 22 : 30);
    }

    public Animation getBringHimDownAnimation() {
        int points = getBringHimPoints();
        int index = 0;
        if (points >= 167 && points < 200)
            index = 5;
        else if (points >= 134 && points < 167)
            index = 4;
        else if (points >= 101 && points < 134)
            index = 3;
        else if (points >= 68 && points < 101)
            index = 2;
        else if (points >= 35 && points < 68)
            index = 1;
        return new Animation(20394 + index);
    }

    public int getDamageWhileDown() {
        return damageWhileDown;
    }

    public int getPlayerOnBattleCount() {
        int count = 0;
        for (Player player : instance.getPlayersOnBattle()) {
            if (player == null || player.isDead())
                continue;
            count++;
        }
        return count;
    }

    public void jump() {
        boolean hardMode = instance.getSettings().isHardMode();
        if (hardMode && phase == 10) {
            WorldTile jumpLocation = instance.getVoragoSpawnLocation(10);
            WorldTasksManager.schedule(new WorldTask() {
                int count = 0;

                @Override
                public void run() {
                    switch (count) {
                        case 0:
                            setNextFaceWorldTile(jumpLocation);
                            setNextAnimation(new Animation(20365, 20365, 20365, -1, 0));
                            setNextGraphics(new Graphics(4019));
                            break;
                        case 2:
                            setNextAnimation(new Animation(20367));
                            setNextWorldTile(new WorldTile(jumpLocation.getX(), jumpLocation.getY(), 0));
                            stop();
                            break;
                    }
                    count++;
                }
            }, 0, 1);
        } else {
            WorldTile jumpLocation = getRandomUsableLocation(null, true);
            NPC thisNPC = this;
            WorldTasksManager.schedule(new WorldTask() {
                int count = 0;

                @Override
                public void run() {
                    switch (count) {
                        case 1:
                            setNextFaceWorldTile(jumpLocation);
                            setNextAnimation(new Animation(20365));
                            break;
                        case 2:
                            World.sendGraphics(thisNPC, new Graphics(4037), jumpLocation);
                            break;
                        case 4:
                            setNextAnimation(new Animation(20367));
                            setNextWorldTile(new WorldTile(jumpLocation.getX() - 2, jumpLocation.getY() - 2, 0));
                            for (Player player : instance.getPlayersOnBattle()) {
                                if (player == null || player.isDead())
                                    continue;
                                boolean underRago = Utils.colides(jumpLocation.getX() - 2, jumpLocation.getY() - 2, thisNPC.getSize(), player.getX(), player.getY(), player.getSize());
                                int distance = Utils.getDistance(player, jumpLocation);
                                int damage = underRago ? (distance == 0 ? 550 : (550 - (distance * 51))) : (distance > 5 ? 0 : (550 * 1 / distance == 0 ? 1 : distance));
                                if (damage != 0) {
                                    if (damage < 150)
                                        damage = Utils.random(150, 301);
                                    player.applyHit(new Hit(thisNPC, damage, HitLook.REGULAR_DAMAGE));
                                }
                            }
                            stop();
                            break;
                    }
                    count++;
                }

            }, 0, 1);
        }
    }

    // northEast +6 +6 northwest -6+6 southwest -6-6 southeast +6-6

    public void startTheEnd() {
        if (getTemporaryAttributtes().get("CantBeAttackedOnPhaseStart") != null)
            getTemporaryAttributtes().remove("CantBeAttackedOnPhaseStart");
        getTemporaryAttributtes().put("CantBeAttacked", "Vorago is unvulnerable as he charges a massive smash attack!");
        boolean hardMode = instance.getSettings().isHardMode();
        for (Player player : instance.getPlayersOnBattle()) {
            if (player == null || player.isDead())
                continue;
            player.resetCombat();
            player.getTemporaryAttributtes().remove("bomb95042");
            player.getTemporaryAttributtes().remove("bomb95043");
            getVoragoInstance().sendMessage(player, 1, "<col=ff0000>Vorago prepares to suffocate all those who aren't contained!");
        }
        WorldTile voragoLocation = instance.getVoragoSpawnLocation(phase);
        WorldTile center = new WorldTile(voragoLocation.getX() + 2, voragoLocation.getY() + 2, voragoLocation.getPlane());
        WorldTile[] locations = {new WorldTile(voragoLocation.getX() + 7, voragoLocation.getY() + 7, voragoLocation.getPlane()), new WorldTile(voragoLocation.getX() - 6, voragoLocation.getY() + 6, voragoLocation.getPlane()), new WorldTile(voragoLocation.getX() - 6, voragoLocation.getY() - 6, voragoLocation.getPlane()), new WorldTile(voragoLocation.getX() + 6, voragoLocation.getY() - 6, voragoLocation.getPlane())};
        theEndStartIndex = Utils.random(locations.length);
        int objectStartIndex = (theEndStartIndex + 3) > (locations.length - 1) ? (theEndStartIndex - 1) : (theEndStartIndex + 3);
        int objectId = Utils.random(3) == 0 ? 95042 : 95043;
        int blueBombsAmount = (int) (hardMode ? ((getPlayerOnBattleCount() + 1) < 7 ? 7 : (getPlayerOnBattleCount() + 1)) : ((getPlayerOnBattleCount() * 0.7) < 5 ? 5 : (getPlayerOnBattleCount() * 0.7)));
        int redBombsAmount = (int) (hardMode ? ((getPlayerOnBattleCount() * 0.60) < 5 ? 5 : (getPlayerOnBattleCount() * 0.60)) : ((getPlayerOnBattleCount() * 0.42) < 3 ? 3 : (getPlayerOnBattleCount() * 0.42)));
        for (int i = 0; i < 3; i++) {
            int index = (objectStartIndex - i) < 0 ? ((objectStartIndex - i) == -1 ? 3 : 2) : (objectStartIndex - i);
            int id = i == 0 ? 95044 : i == 1 ? objectId : objectId == 95043 ? 95042 : 95043;
            WorldTile loc = new WorldTile(locations[index].getX(), locations[index].getY(), locations[index].getPlane());
            theEndBombs[i] = new TheEndBomb(id, 10, 0, loc, this, id == 95044 ? 1 : id == 95043 ? blueBombsAmount : redBombsAmount);
        }
        WorldTile faceTile = new WorldTile(theEndStartIndex == 0 ? locations[theEndStartIndex].getX() + 6 : theEndStartIndex == 1 || theEndStartIndex == 2 ? locations[theEndStartIndex].getX() - 4 : locations[theEndStartIndex].getX() + 7, theEndStartIndex == 0 ? locations[theEndStartIndex].getY() + 6 : theEndStartIndex == 1 ? locations[theEndStartIndex].getY() + 7 : locations[theEndStartIndex].getY() - 4, locations[theEndStartIndex].getPlane());
        setNextWorldTile(voragoLocation);
        cancelFaceEntityNoCheck();
        getCombat().removeTarget();
        theEndFaceTile = faceTile;
        setNextFaceWorldTile(theEndFaceTile);
        getTemporaryAttributtes().put("canMoveUnderRago", Boolean.TRUE);
        getTemporaryAttributtes().put("TheEndAnim", Boolean.TRUE);
        setNextAnimation(new Animation(25434, 25434, 25434, -1, 0));
        unWalkAbleSpots.clear();
        Vorago thisNPC = this;
        theEndCycles = 3;
        WorldTasksManager.schedule(new WorldTask() {
            int count = 0;

            @Override
            public void run() {
                if (sentDeath || instance.getPlayersOnBattle().isEmpty()) {
                    stop();
                    getTemporaryAttributtes().remove("TheEnd");
                    getTemporaryAttributtes().remove("TheEndAnim");
                    getTemporaryAttributtes().remove("CantBeAttacked");
                    for (Player player : instance.getPlayersOnBattle()) {
                        if (player == null || player.isDead())
                            continue;
                        player.getTemporaryAttributtes().remove("Suffocating");
                    }
                    setCantDoDefenceEmote(false);
                    return;
                }
                if (getCombat().getTarget() != null && theEndCycles != 0) {
                    getCombat().removeTarget();
                    getCombat().reset();
                    cancelFaceEntityNoCheck();
                    setNextFaceWorldTile(theEndFaceTile);
                }
                if (count == 10) {
                    getTemporaryAttributtes().remove("canMoveUnderRago");
                    for (int i = 0; i < theEndBombs.length; i++) {
                        theEndBombs[i].setActive(false);
                        World.spawnObject(theEndBombs[i]);
                    }
                    setNextForceTalk(new ForceTalk("...Za End!"));
                    setNextAnimation(new Animation(25435));
                    setCantDoDefenceEmote(true);
                    getTemporaryAttributtes().remove("CantBeAttacked");
                    getTemporaryAttributtes().put("TheEnd", Boolean.TRUE);
                    damageReduction = 1;
                    for (Player player : instance.getPlayersOnBattle()) {
                        if (player == null || player.isDead())
                            continue;
                        if (isBetweenVoragoHands(player))
                            player.getTemporaryAttributtes().remove("Suffocating");
                        else
                            player.getTemporaryAttributtes().put("Suffocating", 10);
                    }
                    unWalkAbleSpots.clear();
                    for (int i = 0; i < 2; i++) {
                        for (int j = 0; j < 15; j++) {
                            WorldTile unWalkable = new WorldTile(i == 0 ? ((theEndStartIndex == 0 || theEndStartIndex == 2) ? (theEndStartIndex == 0 ? (center.getX() + j) : (center.getX() - j)) : center.getX()) : ((theEndStartIndex == 0 || theEndStartIndex == 2) ? center.getX() : (theEndStartIndex == 1 ? (center.getX() - j) : (center.getX() + j))), i == 0 ? ((theEndStartIndex == 0 || theEndStartIndex == 2) ? center.getY() : (theEndStartIndex == 1 ? (center.getY() + j) : (center.getY() - j))) : ((theEndStartIndex == 0 || theEndStartIndex == 2) ? (theEndStartIndex == 0 ? (center.getY() + j) : (center.getY() - j)) : center.getY()), getPlane());
                            unWalkAbleSpots.add(unWalkable);
                        }
                    }
                } else if (count == 16)
                    setNextAnimation(new Animation(25436, 25436, 25436, -1, 0));
                else if (count == 19) {
                    setNextAnimation(new Animation(25437));
                    unWalkAbleSpots.clear();
                    for (int i = 0; i < 2; i++) {
                        for (int j = 0; j < 15; j++) {
                            WorldTile unWalkable = new WorldTile(i == 0 ? ((theEndStartIndex == 0 || theEndStartIndex == 2) ? (theEndStartIndex == 0 ? (center.getX() + j) : (center.getX() - j)) : center.getX()) : ((theEndStartIndex == 0 || theEndStartIndex == 2) ? (theEndStartIndex == 0 ? (center.getX() - j) : (center.getX() + j)) : (center.getX())), i == 0 ? ((theEndStartIndex == 0 || theEndStartIndex == 2) ? center.getY() : (theEndStartIndex == 1 ? (center.getY() + j) : (center.getY() - j))) : ((theEndStartIndex == 0 || theEndStartIndex == 2) ? (center.getY()) : (theEndStartIndex == 1 ? (center.getY() - j) : (center.getY() + j))), getPlane());
                            unWalkAbleSpots.add(unWalkable);
                        }
                    }
                    TheEndBomb bomb = getBombBetweenVoragoHands();
                    if (bomb != null)
                        bomb.setActive(false);
                } else if (count == 27) {
                    int nextFaceIndex = (theEndStartIndex + 1) > (locations.length - 1) ? (theEndStartIndex - 3) : (theEndStartIndex + 1);
                    WorldTile nextFaceTile = new WorldTile(nextFaceIndex == 0 ? locations[nextFaceIndex].getX() + 6 : nextFaceIndex == 1 || nextFaceIndex == 2 ? locations[nextFaceIndex].getX() - 4 : locations[nextFaceIndex].getX() + 7, nextFaceIndex == 0 ? locations[nextFaceIndex].getY() + 6 : nextFaceIndex == 1 ? locations[nextFaceIndex].getY() + 7 : locations[nextFaceIndex].getY() - 4, locations[nextFaceIndex].getPlane());
                    setNextAnimation(new Animation(25438, 25438, 25438, -1, 0));
                    for (Player player : instance.getPlayersOnBattle()) {
                        if (player == null || player.isDead() || !isBetweenVoragoHands(player))
                            continue;
                        player.lock(2);
                        player.setNextAnimation(new Animation(20338));
                        WorldTile pushTile = new WorldTile(nextFaceIndex == 0 || nextFaceIndex == 2 ? player.getX() : nextFaceTile.getX(), nextFaceIndex == 0 || nextFaceIndex == 2 ? nextFaceTile.getY() : player.getY(), player.getPlane());
                        player.setNextWorldTile(pushTile);
                        player.applyHit(new Hit(thisNPC, (hardMode ? 500 : 250), HitLook.REGULAR_DAMAGE));
                    }
                    setNextFaceWorldTile(nextFaceTile);
                    theEndFaceTile = nextFaceTile;
                    unWalkAbleSpots.clear();
                    for (int i = 0; i < 2; i++) {
                        for (int j = 0; j < 15; j++) {
                            WorldTile unWalkable = new WorldTile(i == 0 ? ((nextFaceIndex == 0 || nextFaceIndex == 2) ? (nextFaceIndex == 0 ? (center.getX() + j) : (center.getX() - j)) : center.getX()) : ((nextFaceIndex == 0 || nextFaceIndex == 2) ? center.getX() : (nextFaceIndex == 1 ? (center.getX() - j) : (center.getX() + j))), i == 0 ? ((nextFaceIndex == 0 || nextFaceIndex == 2) ? center.getY() : (nextFaceIndex == 1 ? (center.getY() + j) : (center.getY() - j))) : ((nextFaceIndex == 0 || nextFaceIndex == 2) ? (nextFaceIndex == 0 ? (center.getY() + j) : (center.getY() - j)) : center.getY()), getPlane());
                            unWalkAbleSpots.add(unWalkable);
                        }
                    }
                } else if (count == 28) {
                    setNextAnimation(new Animation(25439));
                    for (Player player : instance.getPlayersOnBattle()) {
                        if (player == null || player.isDead())
                            continue;
                        if (isBetweenVoragoHands(player))
                            player.getTemporaryAttributtes().remove("Suffocating");
                        else if (player.getTemporaryAttributtes().get("Suffocating") == null)
                            player.getTemporaryAttributtes().put("Suffocating", 10);
                    }
                    TheEndBomb bomb = getBombBetweenVoragoHands();
                    if (bomb != null) {
                        bomb.setActive(true);
                        damageReduction = bomb.getId() == 95044 ? 4 : bomb.getId() == 95043 ? 3 : 2;
                    }
                } else if (count == 34) {
                    theEndCycles--;
                    theEndStartIndex = theEndStartIndex + 1 >= 4 ? 0 : theEndStartIndex + 1;
                    if (theEndCycles == 0) {
                        setNextAnimation(new Animation(25440));
                        getTemporaryAttributtes().remove("TheEnd");
                        getTemporaryAttributtes().remove("TheEndAnim");
                        for (Player player : instance.getPlayersOnBattle()) {
                            if (player == null || player.isDead())
                                continue;
                            player.getTemporaryAttributtes().remove("Suffocating");
                        }
                        setCantDoDefenceEmote(false);
                        theEndFaceTile = null;
                        damageReduction = 1;
                        unWalkAbleSpots.clear();
                        count = 36;
                        return;
                    }
                    count = 16;
                    return;
                } else if (count == 38) {
                    launchRemainingBombs();
                    stop();
                }
                count++;
            }
        }, 0, 0);
    }

    @Override
    public void setNextAnimation(Animation nextAnimation) {
        if (getTemporaryAttributtes().get("TheEndAnim") != null) {
            int id = nextAnimation.getIds()[0];
            if (id > 25440 || id < 25434)
                return;
        }
        super.setNextAnimation(nextAnimation);
    }

    @Override
    public void setNextAnimationNoPriority(Animation nextAnimation) {
        if (getTemporaryAttributtes().get("TheEndAnim") != null) {
            int id = nextAnimation.getIds()[0];
            if (id > 25440 || id < 25434)
                return;
        }
        super.setNextAnimationNoPriority(nextAnimation);
    }

    public boolean canMove(Player player, int nextX, int nextY) {
        if (unWalkAbleSpots == null || sentDeath)
            return true;
        for (WorldTile checkTile : unWalkAbleSpots) {
            if (checkTile.getX() == nextX && checkTile.getY() == nextY)
                return false;
        }
        return true;
    }

    public TheEndBomb getBombById(int objectId) {
        for (int i = 0; i < theEndBombs.length; i++) {
            if (theEndBombs[i] == null)
                continue;
            if (theEndBombs[i].getId() == objectId)
                return theEndBombs[i];
        }
        return null;
    }

    public TheEndBomb removeBombById(int objectId) {
        for (int i = 0; i < theEndBombs.length; i++) {
            if (theEndBombs[i] == null)
                continue;
            if (theEndBombs[i].getId() == objectId) {
                if (World.containsObjectWithId(theEndBombs[i], theEndBombs[i].getId()))
                    World.removeObject(theEndBombs[i]);
                theEndBombs[i] = null;
            }
        }
        return null;
    }

    public boolean skipBomb(Player player, int bombId) {
        return player.getTemporaryAttributtes().get("bomb" + bombId) != null;
    }

    public void launchRemainingBombs() {
        if (instance.getPlayersOnBattle().isEmpty())
            return;
        TheEndBomb redBomb = getBombById(95042);
        TheEndBomb blueBomb = getBombById(95043);
        int redBombs = redBomb.getCharges();
        int blueBombs = blueBomb.getCharges();
        for (int i = 0; i < blueBombs; i++) {
            Collections.shuffle(instance.getPlayersOnBattle());
            for (Player player : instance.getPlayersOnBattle()) {
                if (player == null || player.hasFinished() || player.isDead() || skipBomb(player, blueBomb.getId()))
                    continue;
                if (blueBomb.decreaseCharges())
                    sendBlueBombAttack(player, blueBomb, false);
                break;
            }
        }
        for (int i = 0; i < redBombs; i++) {
            Collections.shuffle(instance.getPlayersOnBattle());
            for (Player player : instance.getPlayersOnBattle()) {
                if (player == null || player.hasFinished() || player.isDead() || skipBomb(player, redBomb.getId()))
                    continue;
                if (redBomb.decreaseCharges())
                    sendRedBombAttack(player, redBomb);
                break;
            }
        }
        removeBombById(95042);
        removeBombById(95043);
        launchPurpleBomb();
    }

    public void launchPurpleBomb() {
        TheEndBomb purpleBomb = getBombById(95044);
        if (purpleBomb == null)
            return;
        if (!purpleBomb.isActive()) {
            World.removeObject(purpleBomb);
            return;
        }
        purpleBomb.setCharges(0);
        World.removeObject(purpleBomb);
        for (Player player : instance.getPlayersOnBattle()) {
            if (player == null || player.isDead())
                continue;
            player.getPackets().sendStillProjectileNew(purpleBomb, 1, 1, purpleBomb, 1, 1, null, 5326, 20, 150, 0, 50);
        }
        long projectileCycles = Utils.projectileTimeToMiliseconds(45);
        // Vorago turns everyone into explosive Vitali!
        CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
            @Override
            public boolean repeat() {
                try {
                    sendPurpleBombAttack();
                    removeBombById(95044);
                    return false;
                } catch (Exception e) {
                    Logger.getGlobal().catching(e);
                    return false;
                }
            }
        }, projectileCycles, 600, TimeUnit.MILLISECONDS);
    }

    public void sendPurpleBombAttack() {
        boolean hardMode = instance.getSettings().isHardMode();
        TheEndBomb purpleBomb = ((!hardMode && phase == 5) || (hardMode && phase >= 10)) ? null : getBombById(95044);
        Vorago thisNPC = this;
        for (Player player : instance.getPlayersOnBattle()) {
            if (player == null || player.hasFinished() || player.isDead())
                continue;
            player.getPackets().sendOverlayMessage(1, "<col=ff0000>Vorago turns everyone into explosive Vitali!", true);
        }
        Collections.shuffle(instance.getPlayersOnBattle());
        int count = 0;
        for (int i = 0; i < instance.getPlayersOnBattle().size(); i++) {
            if (count >= 10)
                break;
            Player player = instance.getPlayersOnBattle().get(i);
            if (player == null || player.hasFinished() || player.isDead())
                continue;
            WorldTile fromLocation = purpleBomb != null ? purpleBomb : new WorldTile(player.getX() - 4, player.getY(), player.getPlane());
            Projectile projectile = World.sendProjectileCycles(fromLocation, player, 5326, 150, 23, 0, 350 + (count * 100), 0, 0);
            long projectileCycles = Utils.projectileTimeToMiliseconds1(projectile.getStartTime() + projectile.getEndTime() + 1);
            // PurpleBombHitBar bar = new PurpleBombHitBar(player);
            player.getAppearence().transformIntoNPC(17158);
            CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
                final long startTime = Utils.currentTimeMillis() + projectileCycles;

                @Override
                public boolean repeat() {
                    try {
                        if (checkStopAttack(player))
                            return false;
                        if (Utils.currentTimeMillis() >= startTime) {
                            player.getAppearence().transformIntoNPC(-1);
                            player.setNextGraphics(new Graphics(3522));
                            int damage = hardMode ? 400 : 200;
                            for (Player p2 : instance.getPlayersOnBattle()) {
                                if (p2 == null || p2.hasFinished() || p2.isDead() || !Utils.isOnRange(player, p2, 1))
                                    continue;
                                CombatScript.delayHit(thisNPC, 0, p2, new Hit(thisNPC, damage, HitLook.REGULAR_DAMAGE));
                            }
                            return false;
                        }
                        return true;
                    } catch (Exception e) {
                        Logger.getGlobal().catching(e);
                        return false;
                    }
                }
            }, 0, 1, TimeUnit.MILLISECONDS);
            CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
                int loop = 0;
                int count = ((projectile.getEndTime() - 50) / 50) * 2;

                @Override
                public boolean repeat() {
                    try {
                        if (checkStopAttack(player))
                            return false;
                        int perc = (int) (((double) loop / ((double) ((projectile.getEndTime() - 50) / 50) * 2)) * 100);
                        player.getTemporaryAttributtes().put("PurpleBomb", perc);
                        if (count == 0)
                            return false;
                        if (loop % 2 == 0) {
                            player.getPackets().sendOverlayMessage(1, ("<col=ff0000>Detonation in: " + (count / 2)), false);
                        }
                        count--;
                        loop++;
                        return true;
                    } catch (Exception e) {
                        Logger.getGlobal().catching(e);
                        return false;
                    }
                }
            }, 1000, 500, TimeUnit.MILLISECONDS);
            count++;
        }
    }

    public void handleBombClick(Player player, WorldObject object) {
        if (sentDeath)
            return;
        TheEndBomb bomb = (TheEndBomb) object;
        if (bomb == null)
            return;
        if (skipBomb(player, object.getId()))
            return;
        if (getTemporaryAttributtes().get("TheEnd") == null || !bomb.isActive()) {
            getVoragoInstance().sendMessage(player, 1, "Vorago isn't focusing on making that bomb active right now.");
            return;
        }
        switch (bomb.getId()) {
            case 95042:// red bomb
                if (bomb.decreaseCharges())
                    sendRedBombAttack(player, bomb);
                if (bomb.getCharges() == 0)
                    removeBombById(bomb.getId());
                break;
            case 95043:// blue bomb
                if (bomb.decreaseCharges())
                    sendBlueBombAttack(player, bomb, true);
                if (bomb.getCharges() == 0)
                    removeBombById(bomb.getId());
                break;
            case 95044:
                launchPurpleBomb();
                break;
        }
    }

    public boolean checkStopAttack(Player player) {
        if (instance == null || instance.getPlayersOnBattle() == null)
            return true;
        return player == null || player.hasFinished() || player.isDead() || hasFinished() || isDead() || !instance.getPlayersOnBattle().contains(player);
    }

    public boolean isBetweenVoragoHands(WorldTile tile) {
        if (theEndFaceTile == null)
            return false;
        WorldTile cornerLocation = new WorldTile(theEndFaceTile.getX(), theEndFaceTile.getY(), theEndFaceTile.getPlane());
        WorldTile center = new WorldTile(getX() + 2, getY() + 2, getPlane());
        int smallestX = center.getX() > cornerLocation.getX() ? cornerLocation.getX() : center.getX();
        int largestX = center.getX() < cornerLocation.getX() ? cornerLocation.getX() : center.getX();
        int smallestY = center.getY() > cornerLocation.getY() ? cornerLocation.getY() : center.getY();
        int largestY = center.getY() < cornerLocation.getY() ? cornerLocation.getY() : center.getY();
        return isInSquare(tile, smallestX, largestX, smallestY, largestY);
    }

    public TheEndBomb getBombBetweenVoragoHands() {
        if (theEndFaceTile == null)
            return null;
        WorldTile cornerLocation = new WorldTile(theEndFaceTile.getX(), theEndFaceTile.getY(), theEndFaceTile.getPlane());
        WorldTile center = new WorldTile(getX() + 2, getY() + 2, getPlane());
        int smallestX = center.getX() > cornerLocation.getX() ? cornerLocation.getX() : center.getX();
        int largestX = center.getX() < cornerLocation.getX() ? cornerLocation.getX() : center.getX();
        int smallestY = center.getY() > cornerLocation.getY() ? cornerLocation.getY() : center.getY();
        int largestY = center.getY() < cornerLocation.getY() ? cornerLocation.getY() : center.getY();
        int[] bombObjectIds = {95042, 95043, 95044};
        for (int i = 0; i < bombObjectIds.length; i++) {
            for (int x = smallestX; x < largestX; x++) {
                for (int y = smallestY; y < largestY; y++) {
                    TheEndBomb bomb = (TheEndBomb) World.getObjectWithId(new WorldTile(x, y, cornerLocation.getPlane()), bombObjectIds[i]);
                    if (bomb == null)
                        continue;
                    return bomb;
                }
            }
        }
        return null;
    }

    public void sendBlueBombAttack(Player player, WorldTile fromLocation, boolean resetDefinsive) {
        player.getTemporaryAttributtes().put("bomb" + 95043, Boolean.TRUE);
        Vorago thisNPC = this;
        Projectile projectile = World.sendProjectileCycles(fromLocation, player, 4016, 20, 23, 0, 160, 45, 0);
        long projectileCycles = Utils.projectileTimeToMiliseconds1(projectile.getStartTime() + projectile.getEndTime() + 10);
        CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
            @Override
            public boolean repeat() {
                try {
                    if (checkStopAttack(player))
                        return false;
                    player.setNextGraphics(new Graphics(4017));
                    for (Entity t : getPossibleTargets()) {
                        if (t == null || t.hasFinished() || t.isDead() || !Utils.isOnRange(player, t, 2))
                            continue;
                        int damage = CombatScript.getMaxHit(thisNPC, 500, NPCCombatDefinitionConstants.MAGE, t);
                        CombatScript.delayHit(thisNPC, 0, t, CombatScript.getMagicHit(thisNPC, damage));
                    }
                    return false;
                } catch (Exception e) {
                    Logger.getGlobal().catching(e);
                    return false;
                }
            }
        }, projectileCycles, 600, TimeUnit.MILLISECONDS);
    }

    public void sendRedBombAttack(Player player, WorldTile fromLocation) {
        boolean hardMode = instance.getSettings().isHardMode();
        player.getTemporaryAttributtes().put("bomb" + 95042, Boolean.TRUE);
        Vorago thisNPC = this;
        Projectile projectile = World.sendProjectileCycles(fromLocation, player, 4023, 20, 23, 0, 275, 40, 0);
        long projectileCycles = Utils.projectileTimeToMiliseconds1(projectile.getStartTime() + projectile.getEndTime() + 10);
        CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
            @Override
            public boolean repeat() {
                try {
                    if (checkStopAttack(player))
                        return false;
                    int damage = 200 + (hardMode ? 100 : 0);
                    for (Entity e : getPossibleTargets()) {
                        if (e == null || e.hasFinished() || e.isDead() || e == player || !Utils.isOnRange(player, e, 3))
                            continue;
                        damage += 100 + (hardMode ? 50 : 0);
                    }
                    if (damage > (hardMode ? 1050 : 700))
                        damage = hardMode ? 1050 : 700;
                    player.setNextGraphics(new Graphics(4024));
                    World.sendGraphics(thisNPC, new Graphics(3522), new WorldTile(player));
                    for (Entity e : getPossibleTargets()) {
                        if (e == null || e.hasFinished() || e.isDead() || !Utils.isOnRange(player, e, 1))
                            continue;

                        CombatScript.delayHit(thisNPC, 0, e, new Hit(thisNPC, damage, HitLook.REGULAR_DAMAGE));
                    }
                    player.getCombatDefinitions().decreaseSpecialAttack(5);
                    return false;
                } catch (Exception e) {
                    Logger.getGlobal().catching(e);
                    return false;
                }
            }
        }, projectileCycles, 600, TimeUnit.MILLISECONDS);
    }

    // north 0 - east - 1 south - 2 west - 3

    public void sendTeamSplit() {
        if (getTemporaryAttributtes().get("CantBeAttackedOnPhaseStart") != null)
            getTemporaryAttributtes().remove("CantBeAttackedOnPhaseStart");
        boolean hardMode = instance.getSettings().isHardMode();
        setNextAnimation(new Animation(20322));
        getTemporaryAttributtes().put("CantBeAttacked", "Vorago is invulnerable as he charges a massive fire attack!");
        WorldTile greenSpawnLocation = getRandomUsableLocation();
        WorldTile redSpawnLocation = getRandomUsableLocation(greenSpawnLocation);
        ArrayList<WorldTile> redSafeLocations = new ArrayList<WorldTile>();
        ArrayList<WorldTile> greenSafeLocations = new ArrayList<WorldTile>();
        if (hardMode) {
            greenSafeLocations.add(greenSpawnLocation);
            redSafeLocations.add(redSpawnLocation);
            for (int i = 0; i < 2; i++) {
                int index = (i == 0) ? 0 : 4;
                int objectId = (i == 0) ? 87328 : 88882;
                WorldTile loc = (i == 0) ? greenSpawnLocation : redSpawnLocation;
                teamSplitSquares[index] = new WorldObject(objectId, 22, 1, new WorldTile(loc.getX() - 1, loc.getY(), loc.getPlane()));
                teamSplitSquares[1 + index] = new WorldObject(objectId, 22, 3, new WorldTile(loc.getX() + 1, loc.getY(), loc.getPlane()));
                teamSplitSquares[2 + index] = new WorldObject(objectId, 22, 0, new WorldTile(loc.getX(), loc.getY() - 1, loc.getPlane()));
                teamSplitSquares[3 + index] = new WorldObject(objectId, 22, 2, new WorldTile(loc.getX(), loc.getY() + 1, loc.getPlane()));
            }
            for (WorldObject teamSplitSquare : teamSplitSquares) {
                if (teamSplitSquare != null)
                    World.spawnObject(teamSplitSquare);
            }
        } else {
            int index = 0;
            int startYGreen = greenSpawnLocation.getY() - 2;
            int startYRed = redSpawnLocation.getY() - 2;
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    WorldTile loc = (index >= 0 && index < 8) ? new WorldTile(redSpawnLocation.getX() + i, startYRed + j, redSpawnLocation.getPlane()) : new WorldTile(greenSpawnLocation.getX() + ((i >= 2) ? (i - 2) : i), startYGreen + j, greenSpawnLocation.getPlane());
                    int rot = (i == 0 || i == 2) ? ((j == 0) ? 0 : (j == 1 || j == 2) ? 3 : 2) : ((j == 0) ? 0 : (j == 1 || j == 2) ? 1 : 2);
                    if ((j == 1 || j == 2) && (index >= 0 && index < 8))
                        redSafeLocations.add(loc);
                    else if ((j == 1 || j == 2) && (index >= 8))
                        greenSafeLocations.add(loc);
                    teamSplitSquares[index] = new WorldObject((index >= 0 && index < 8) ? 88882 : 87328, 22, rot, loc);
                    World.spawnObject(teamSplitSquares[index]);
                    index++;
                }
            }
        }
        ArrayList<Player> players = new ArrayList<Player>();
        int playersCount = 0;
        for (Player player : instance.getPlayersOnBattle()) {
            if (player == null || player.isDead())
                continue;
            player.getTemporaryAttributtes().remove("TeamSplitRed");
            players.add(player);
            playersCount++;
        }
        Collections.shuffle(players);
        int playersInTeam = 0;
        boolean redTeam = Utils.random(2) == 0;
        Colour red = new Colour(0, 510, 50, 110, 70, 130);
        Colour green = new Colour(0, 510, 70, 110, 50, 130);
        for (int i = 0; i < playersCount; i += 2) {
            Player player = players.get(i);
            if (player == null || player.isDead())
                continue;
            player.getTemporaryAttributtes().put("TeamSplitRed", redTeam);
            player.setNextColour(redTeam ? red : green);
            playersInTeam++;
        }
        int playersLeft = (playersCount - playersInTeam);
        for (int i = playersLeft; i > 0; i--) {
            for (Player player : players) {
                if (player == null || player.isDead() || player.getTemporaryAttributtes().get("TeamSplitRed") != null)
                    continue;
                player.getTemporaryAttributtes().put("TeamSplitRed", !redTeam);
                player.setNextColour(redTeam ? green : red);
                break;
            }
        }
        for (Player player : instance.getPlayersOnBattle()) {
            if (player == null || player.isDead())
                continue;
            player.getPackets().sendGameMessage("Vorago's raw power disrupts your defensive abilities!");
            getVoragoInstance().sendMessage(player, 1, "<col=ff0000>Vorago starts to charge all his power into a massive fire attack!");
        }
        Entity lastTarget = getCombat().getTarget();
        getCombat().removeTarget();
        cancelFaceEntityNoCheck();
        getTemporaryAttributtes().put("canMoveUnderRago", Boolean.TRUE);
        Vorago thisNPC = this;
        WorldTasksManager.schedule(new WorldTask() {
            int count = 0;

            @Override
            public void run() {
                if (sentDeath) {
                    for (int i = 0; i < teamSplitSquares.length; i++) {
                        WorldObject square = teamSplitSquares[i];
                        if (square == null)
                            continue;
                        if (World.containsObjectWithId(new WorldTile(square), 88882) || World.containsObjectWithId(new WorldTile(square), 87328))
                            World.removeObject(square);
                        teamSplitSquares[i] = null;
                    }
                    getTemporaryAttributtes().remove("canMoveUnderRago");
                    stop();
                    return;
                }
                switch (count) {
                    case 12:
                        setNextAnimation(new Animation(20323));
                        break;
                    case 13:
                        setNextAnimation(new Animation(20323));
                        setNextGraphics(new Graphics(4013));
                        setNextFaceEntity(lastTarget == null ? getRandomTarget(new WorldTile(thisNPC), null, 24) : lastTarget);
                        getTemporaryAttributtes().remove("CantBeAttacked");
                        for (Player player : instance.getPlayersOnBattle()) {
                            if (player == null || player.isDead())
                                continue;
                            boolean isInRedTeam = (boolean) player.getTemporaryAttributtes().get("TeamSplitRed");
                            if ((isInRedTeam && isSafe(new WorldTile(player.getX(), player.getY(), player.getPlane()), redSafeLocations)) || (!isInRedTeam && isSafe(new WorldTile(player.getX(), player.getY(), player.getPlane()), greenSafeLocations)))
                                continue;
                            player.applyHit(new Hit(thisNPC, (hardMode ? 900 : 800), HitLook.VORAGO_SPECIAL_DAMAGE));
                            player.getTemporaryAttributtes().remove("TeamSplitRed");
                        }
                        getWalkSteps().clear();
                        getCombat().setTarget(lastTarget);
                        break;
                    case 14:
                        for (int i = 0; i < teamSplitSquares.length; i++) {
                            WorldObject square = teamSplitSquares[i];
                            if (square == null)
                                continue;
                            if (World.containsObjectWithId(new WorldTile(square), 88882) || World.containsObjectWithId(new WorldTile(square), 87328))
                                World.removeObject(square);
                            teamSplitSquares[i] = null;
                        }
                        getTemporaryAttributtes().remove("canMoveUnderRago");
                        stop();
                        break;
                }
                count++;
            }
        }, 0, 0);
    }

    public void sendGreenBomb() {
        boolean hardMode = instance.getSettings().isHardMode();
        setNextAnimation(new Animation(20371));
        setNextGraphics(new Graphics(4268));
        WorldTile mistLocation = ((!hardMode && phase == 5) || (hardMode && phase == 10)) ? getTile(new WorldTile(3092, 5984, 0)) : (hardMode && phase == 11) ? getTile(new WorldTile(3113, 5984, 0)) : getRandomUsableLocation();
        if (mistLocation == null)
            return;
        Projectile projectile = World.sendProjectileCycles(this, mistLocation, 4269, 130, 5, 45, 220, Utils.random(5), 50);
        Vorago thisNPC = this;
        greenBombP = null;
        long projectileCycles = Utils.projectileTimeToMiliseconds(projectile.getStartTime() + projectile.getEndTime() - 1);
        CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {

            @Override
            public boolean repeat() {
                try {
                    if (!isAtLastPhase()) {
                        int index = getAvailableMistIndex();
                        if (index != -1) {
                            WorldTile spawnLoc = new WorldTile(mistLocation.getX() - 5, mistLocation.getY() - 3, mistLocation.getPlane());
                            if (World.getStandartObject(spawnLoc) != null)
                                spawnLoc = spawnLoc.transform(1, 0, 0);
                            mists[index] = new WorldObject(87322, 10, 0, spawnLoc);
                            World.spawnObject(mists[index]);
                        }
                    }
                    for (Player player : instance.getPlayersOnBattle()) {
                        if (player == null || player.hasFinished() || player.isDead())
                            continue;
                        player.getTemporaryAttributtes().remove("GreenBombed");
                    }
                    greenBombBounces = ((!hardMode && phase == 3) || (hardMode && phase == 6)) ? 5 : 4;
                    greenBombP = (Player) getPossibleTargets().get(Utils.random(getPossibleTargets().size()));
                    getVoragoInstance().sendMessage(greenBombP, 1, "<col=ff0000>Vorago has sent a green bomb after you. Run!</col>");
                    final Projectile projectile = World.sendProjectileCycles(mistLocation, greenBombP, 4269, 5, 30, 0, 240, 35, 0);
                    final long projectileCycles = Utils.projectileTimeToMiliseconds(projectile.getStartTime() + projectile.getEndTime() + 85);
                    CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
                        long startTime = Utils.currentTimeMillis() + projectileCycles;

                        @Override
                        public boolean repeat() {
                            try {
                                if (greenBombBounces == 0 || sentDeath) {
                                    greenBombP = null;
                                    for (Player player : instance.getPlayersOnBattle()) {
                                        if (player == null || player.hasFinished() || player.isDead())
                                            continue;
                                        player.getTemporaryAttributtes().remove("GreenBombed");
                                    }
                                    return false;
                                }
                                if (Utils.currentTimeMillis() >= startTime) {
                                    if (getNearbyPlayers(greenBombP) == 0 || (greenBombP.getTemporaryAttributtes().get("GreenBombed") != null && greenBombBounces != 1)) {
                                        greenBombP.setNextGraphics(new Graphics(4270));
                                        greenBombP.applyHit(new Hit(thisNPC, 1000, HitLook.VORAGO_SPECIAL_DAMAGE));
                                        if (greenBombP.getCombatDefinitions().isAutoRetaliate() && !greenBombP.hasWalkSteps())
                                            greenBombP.getActionManager().setAction(new PlayerCombat(thisNPC));
                                        if ((!hardMode && phase == 3) || (hardMode && phase == 6))
                                            attackProgress = attackProgress < 10 ? 6 : 16;
                                        else if (thisNPC.isAtLastPhase()) {
                                            attackProgress = 6;
                                        }
                                        greenBombBounces = 0;
                                        greenBombP.getTemporaryAttributtes().remove("GreenBombed");
                                        greenBombP = null;
                                        return false;
                                    }
                                    for (Player player : instance.getPlayersOnBattle()) {
                                        if (player == null || player.isDead() || !Utils.isOnRange(player, greenBombP, 0) || greenBombBounces == 1)
                                            continue;
                                        player.setNextGraphics(new Graphics(4270));
                                        player.applyHit(new Hit(thisNPC, 100, HitLook.REGULAR_DAMAGE));
                                        int perc = player.getCombatDefinitions().getSpecialAttackPercentage();
                                        if (perc != 0)
                                            player.getCombatDefinitions().decreaseSpecialAttack(perc < 10 ? (10 - perc) : 10);
                                    }
                                    Player p2 = getRandomTarget(new WorldTile(greenBombP), greenBombP, 2);
                                    Projectile projectile = World.sendProjectileCycles(new WorldTile(greenBombP), p2, 4269, 30, 30, 0, 180, 40, 0);
                                    long projectileCycles = Utils.projectileTimeToMiliseconds(projectile.getStartTime() + projectile.getEndTime() + 82);
                                    startTime = Utils.currentTimeMillis() + projectileCycles;
                                    greenBombP.getTemporaryAttributtes().put("GreenBombed", Boolean.TRUE);
                                    greenBombP = p2;
                                    greenBombBounces--;
                                    return true;
                                }
                                return true;
                            } catch (Exception e) {
                                Logger.getGlobal().catching(e);
                                return false;
                            }
                        }

                        public int getNearbyPlayers(Entity player) {
                            int count = 0;
                            for (Player n : instance.getPlayersOnBattle()) {
                                if (n == player || n == null || n.isDead() || n.hasFinished() || !Utils.isOnRange(player, n, 1))
                                    continue;
                                count++;
                            }
                            return count;
                        }

                    }, 0, 1, TimeUnit.MILLISECONDS);
                    return false;
                } catch (Exception e) {
                    Logger.getGlobal().catching(e);
                    return false;
                }
            }
        }, projectileCycles, 1, TimeUnit.MILLISECONDS);
    }

    public boolean isUnderMist(Player player) {
        for (WorldObject mist : mists) {
            if (mist == null)
                continue;
            WorldTile centerM = new WorldTile(mist.getX() + 5, mist.getY() + 3, mist.getY());
            if (player.getX() >= (centerM.getX() - 4) && player.getX() <= (centerM.getX() + 4) && player.getY() >= (centerM.getY() - 4) && player.getY() <= (centerM.getY() + 4))
                return true;
        }
        return false;
    }

    public int getPlayersNearby(Player target, int withinDistance) {
        int count = 0;
        for (Player player : instance.getPlayersOnBattle()) {
            if (player == null || player.hasFinished() || player.isDead() || Utils.getDistance(target, player) > withinDistance || player == target)
                continue;
            count++;
        }
        return count > 5 ? 5 : count;
    }

    public Player getRandomTarget(WorldTile tile, Player exception, int maxDistance) {
        List<Player> availablePlayers = new ArrayList<Player>();
        for (int i = 0; i < instance.getPlayersOnBattle().size(); i++) {
            Player player = instance.getPlayersOnBattle().get(i);
            if (player == null || player.hasFinished() || player.isDead() || (exception != null && player == exception) || !Utils.isOnRange(tile, player, maxDistance - 1, 1, 1))
                continue;
            availablePlayers.add(player);
        }
        return availablePlayers.isEmpty() ? null : availablePlayers.get(Utils.random(availablePlayers.size()));
    }

    public int getAvailableMistIndex() {
        for (int i = 0; i < mists.length; i++) {
            if (mists[i] == null)
                return i;
        }
        return -1;
    }

    public boolean cantSendVitalisOrb() {
        if (!isAtLastPhase())
            return false;
        boolean hardMode = instance.getSettings().isHardMode();
        int checkX = hardMode && phase == 11 ? getX() + 4 : getX() - 4;
        WorldTile vs = instance.getVoragoSpawnLocation(hardMode ? 10 : 5);
        return checkX >= (vs.getX() + 7) || checkX <= (vs.getX() - 10);
    }

    public void sendVitalisOrb() {
        boolean hardMode = instance.getSettings().isHardMode();
        if (cantSendVitalisOrb())
            return;
        setNextAnimation(new Animation(20328));
        setNextGraphics(new Graphics(4026));
        WorldTile closestVitalisSpawn = (((!hardMode && phase == 5) || (hardMode && phase == 10)) ? new WorldTile(getX() - 4, getY() + 2, getPlane()) : (hardMode && phase == 11) ? new WorldTile(getX() + 4, getY() + 2, getPlane()) : getVitalisSpawnLocation());
        Projectile projectile = World.sendProjectileNew(this, closestVitalisSpawn, 4027, 0, 0, 10, 0.3, 0, 0);
        int cycleTime = Utils.projectileTimeToCycles(projectile.getEndTime()) - 1;
        Vorago thisNPC = this;
        WorldTasksManager.schedule(new WorldTask() {
            int vitalisCount = hardMode ? (phase >= 10 ? Utils.random(3, 6) : 8) : (phase == 5 ? Utils.random(2, 5) : 5);

            @Override
            public void run() {
                if (hardMode && phase >= 10 && sentDeath)
                    return;
                World.sendGraphics(thisNPC, new Graphics(4047), closestVitalisSpawn);
                for (Player player : instance.getPlayersOnBattle()) {
                    if (player == null || player.hasFinished() || player.isDead() || !isInSquare(player, closestVitalisSpawn.getX() - 2, closestVitalisSpawn.getX() + 2, closestVitalisSpawn.getY() - 2, closestVitalisSpawn.getY() + 2))
                        continue;
                    vitalisCount--;
                    CombatScript.delayHit(thisNPC, 0, player, new Hit(thisNPC, (hardMode ? 300 : 200), HitLook.REGULAR_DAMAGE));
                    player.setNextGraphics(new Graphics(4028));
                }
                for (int i = 0; i < vitalisCount; i++) {
                    WorldTile spawnTile = instance.randomSpawnTile(null, closestVitalisSpawn, ((!hardMode && phase == 5) || (hardMode && phase >= 10)) ? 1 : 2);
                    int index = getAvailableIndex();
                    if (index == -1)
                        continue;
                    vitali[index] = new Vitalis(17157, spawnTile, -1, true, thisNPC);
                    vitali[index].setNextAnimation(new Animation(20381));
                }
            }
        }, cycleTime);
        return;
    }

    public void sendVitalisDeath(Vitalis vitalis) {
        for (int i = 0; i < vitali.length; i++) {
            if (vitali[i] == vitalis)
                vitali[i] = null;
        }
    }

    public int getVitalisAliveCount() {
        int count = 0;
        for (int i = 0; i < vitali.length; i++) {
            Vitalis vitalis = vitali[i];
            if (vitalis != null && !vitalis.isDead())
                count++;
        }
        return count;
    }

    public int getAvailableIndex() {
        boolean hardMode = instance.getSettings().isHardMode();
        if (getVitalisAliveCount() >= (hardMode ? 25 : 15))
            return -1;
        for (int i = 0; i < vitali.length; i++) {
            if (vitali[i] == null)
                return i;
        }
        return -1;
    }

    private boolean isInSquare(WorldTile tile, int smallestX, int largestX, int smallestY, int largestY) {
        return (tile.getX() >= smallestX && tile.getX() <= largestX && tile.getY() >= smallestY && tile.getY() <= largestY);
    }

    private WorldTile getVitalisSpawnLocation() {
        WorldTile[] jumpLocationsPhase = new WorldTile[locations.length];
        boolean hardMode = instance.getSettings().isHardMode();
        if (hardMode) {
            for (int i = 0; i < jumpLocationsPhase.length; i++) {
                jumpLocationsPhase[i] = getTile(new WorldTile(locations[i].getX(), locations[i].getY() - 64, locations[i].getPlane()));
            }
        } else {
            for (int i = 0; i < jumpLocationsPhase.length; i++) {
                switch (phase) {
                    case 3:
                        jumpLocationsPhase[i] = getTile(new WorldTile(locations[i].getX(), locations[i].getY() - 64, locations[i].getPlane()));
                        break;
                    case 4:
                        jumpLocationsPhase[i] = getTile(new WorldTile(locations[i].getX() - 64, locations[i].getY() - 128, locations[i].getPlane()));
                        break;
                }
            }
        }
        WorldTile voragoSquare = getVoragoSquare();
        if(voragoSquare != null) {
            List<WorldTile> closestLocations = new ArrayList<WorldTile>();
            if (voragoSquare.getX() == jumpLocationsPhase[4].getX() && voragoSquare.getY() == jumpLocationsPhase[4].getY()) {
                closestLocations.add(jumpLocationsPhase[1]);
                closestLocations.add(jumpLocationsPhase[2]);
                closestLocations.add(jumpLocationsPhase[5]);
            } else {
                for (int i = 0; i < jumpLocationsPhase.length; i++) {
                    WorldTile close = jumpLocationsPhase[i];
                    if (close.getX() == voragoSquare.getX() && close.getY() == voragoSquare.getY())
                        continue;
                    if (Utils.getDistance(close, voragoSquare) > 12)
                        continue;
                    closestLocations.add(close);
                }
            }
            return closestLocations.get(Utils.random(closestLocations.size()));
        }
        // TODO Might have to change this vitalis orb spawn location.
        return this;
    }

    public WorldTile getVoragoSquare() {
        WorldTile[] jumpLocationsPhase = new WorldTile[locations.length];
        boolean hardMode = instance.getSettings().isHardMode();
        if (hardMode) {
            for (int i = 0; i < jumpLocationsPhase.length; i++) {
                switch (phase) {
                    case 3:
                        jumpLocationsPhase[i] = getTile(new WorldTile(locations[i].getX(), locations[i].getY() - 64, locations[i].getPlane()));
                        break;
                    case 4:
                        jumpLocationsPhase[i] = getTile(new WorldTile(locations[i].getX() - 64, locations[i].getY() - 64, locations[i].getPlane()));
                        break;
                    case 5:
                        jumpLocationsPhase[i] = getTile(new WorldTile(locations[i].getX(), locations[i].getY() - 64, locations[i].getPlane()));
                        break;
                    case 6:
                        jumpLocationsPhase[i] = getTile(new WorldTile(locations[i].getX() - 64, locations[i].getY() - 64, locations[i].getPlane()));
                        break;
                    case 7:
                        jumpLocationsPhase[i] = getTile(new WorldTile(locations[i].getX(), locations[i].getY() - 64, locations[i].getPlane()));
                        break;
                    case 8:
                        jumpLocationsPhase[i] = getTile(new WorldTile(locations[i].getX() - 64, locations[i].getY() - 64, locations[i].getPlane()));
                        break;
                    case 9:
                        jumpLocationsPhase[i] = getTile(new WorldTile(locations[i].getX() - 64, locations[i].getY() - 128, locations[i].getPlane()));
                        break;
                }
            }
        } else {
            for (int i = 0; i < jumpLocationsPhase.length; i++) {
                switch (phase) {
                    case 3:
                        jumpLocationsPhase[i] = getTile(new WorldTile(locations[i].getX(), locations[i].getY() - 64, locations[i].getPlane()));
                        break;
                    case 4:
                        jumpLocationsPhase[i] = getTile(new WorldTile(locations[i].getX() - 64, locations[i].getY() - 128, locations[i].getPlane()));
                        break;
                }
            }
        }
        for (int i = 0; i < jumpLocationsPhase.length; i++) {
            WorldTile center = jumpLocationsPhase[i];
            if (getX() >= (center.getX() - 3) && getX() <= (center.getX() + 3) && getY() >= (center.getY() - 3) && getY() <= (center.getY() + 3))
                return center;
        }
        return null;
    }

    public void spawnScopuli() {
        boolean hardMode = instance.getSettings().isHardMode();
        WorldTile center = instance.getVoragoSpawnLocation(phase).transform(1, 1, 0);
        WorldTile[] scopuliLocations = {new WorldTile(center.getX(), center.getY() + 6, center.getPlane()), new WorldTile(center.getX() - 4, center.getY() - 6, center.getPlane()), new WorldTile(center.getX() + 4, center.getY() - 6, center.getPlane())};
        for (int i = (hardMode ? 0 : 1); i < 3; i++) {
            scopuli[i] = new Scopulus(17185, scopuliLocations[i], -1, true, this);
        }
    }

    public void sendScopulusDeath(Scopulus scop) {
        boolean hardMode = instance.getSettings().isHardMode();
        for (int i = 0; i < scopuli.length; i++) {
            Scopulus SCOP = scopuli[i];
            if (SCOP == null)
                continue;
            if (SCOP == scop) {
                scopuli[i] = null;
                int lastScopIndex = i == 0 ? 2 : i == 1 ? (!hardMode ? 2 : 0) : 1;
                Scopulus aliveScop = scopuli[lastScopIndex];
                if (aliveScop != null && !aliveScop.hasFinished() && !aliveScop.isDead()) {
                    aliveScop.setNextForceTalk(new ForceTalk("RAAAAAAAAGGGGHHH"));
                    aliveScop.setEnraged(true);
                    aliveScop.heal(15000);
                    break;
                }
            }
        }
        if (getScopuliCount() == 0)
            sendDeath(null);
    }

    public int getScopuliCount() {
        int count = 0;
        for (int i = 0; i < scopuli.length; i++) {
            if (scopuli[i] == null || scopuli[i].isDead())
                continue;
            count++;
        }
        return count;
    }

    public boolean canSendCeilingCollapse() {
        int count = 0;
        for (WorldObject ceiling : ceilingCollapses) {
            if (ceiling == null)
                continue;
            count++;
        }
        return count < 6;
    }

    public boolean sendCeilingCollapse() {
        if (!canSendCeilingCollapse())
            return false;
        setNextAnimation(new Animation(20369));
        WorldTile usableLocation = getRandomUsableLocation();
        WorldTile jumpLocation = getRandomUsableLocation(usableLocation);
        WorldTile ceilingLocation = new WorldTile(usableLocation.getX() - 5, usableLocation.getY() - 3, usableLocation.getPlane());
        Vorago thisNPC = this;
        WorldTasksManager.schedule(new WorldTask() {
            int count = 0;

            @Override
            public void run() {
                if (sentDeath) {
                    for (Player player : instance.getPlayersOnBattle()) {
                        if (player == null || player.isDead() || !isStuckUnderRock(player, null))
                            continue;
                        player.lock(1);
                        player.setNextWorldTile(new WorldTile(getX(), getY(), getPlane()));
                    }
                    stop();
                    return;
                }
                switch (count) {
                    case 3:
                        World.sendGraphics(thisNPC, new Graphics(4025), usableLocation);
                        break;
                    case 5:
                        setNextFaceWorldTile(jumpLocation);
                        setNextAnimation(new Animation(20365));
                        break;
                    case 7:
                        spawnCeilingCollapse(ceilingLocation);
                        setNextAnimation(new Animation(20367));
                        setNextWorldTile(new WorldTile(jumpLocation.getX() - 2, jumpLocation.getY() - 2, 0));
                        break;
                    case 9:
                        for (Player player : instance.getPlayersOnBattle()) {
                            if (player == null || player.isDead() || !isStuckUnderRock(player, null))
                                continue;
                            player.lock(1);
                            player.setNextWorldTile(new WorldTile(getX(), getY(), getPlane()));
                        }
                        stop();
                        break;
                }
                count++;
            }

        }, 0, 1);
        return true;
    }

    private void spawnCeilingCollapse(WorldTile ceilingLocation) {
        spawnCeilingCollapse(ceilingLocation, null);
    }

    public void spawnCeilingCollapse(WorldTile ceilingLocation, WorldObject ceiling) {
        World.spawnObject(new WorldObject(84963, 10, 3, ceilingLocation));
        Vorago thisNPC = this;
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                World.removeObject(World.getObjectWithId(ceilingLocation, 84963));
                if (ceiling == null)
                    for (int i = 0; i < ceilingCollapses.length; i++) {
                        if (ceilingCollapses[i] == null) {
                            ceilingCollapses[i] = new WorldObject(84962, 10, 3, ceilingLocation);
                            World.spawnObject(ceilingCollapses[i]);
                            break;
                        }
                    }
                else
                    World.spawnObject(ceiling);
                int underRock = getPlayersUnderRock(ceilingLocation);
                boolean hardMode = instance.getSettings().isHardMode();
                int damage = hardMode ? 600 : ((underRock == 0) ? 0 : (800 / underRock));
                if (damage == 0)
                    return;
                for (Player player : instance.getPlayersOnBattle()) {
                    if (player == null || player.isDead() || !isStuckUnderRock(player, ceilingLocation))
                        continue;
                    player.setNextAnimation(new Animation(20338));
                    WorldTile toTile = findBestLocation(new WorldTile(player), ceilingLocation);
                    if (toTile != null) {
                        player.setNextForceMovement(new ForceMovement(toTile, 1, getMoveDirection(player)));
                        player.setNextWorldTile(toTile);
                    }
                    player.applyHit(new Hit(thisNPC, damage, HitLook.REGULAR_DAMAGE));
                }
            }
        }, 0);
    }

    public WorldTile findBestLocation(WorldTile playerL, WorldTile ceilingL) {
        ArrayList<WorldTile> unCheckedTeleTiles = new ArrayList<WorldTile>();
        for (int x = (ceilingL.getX() - 1); x <= ceilingL.getX() + 10; x++) {
            for (int y = (ceilingL.getY() - 1); y <= ceilingL.getY() + 10; y++) {
                unCheckedTeleTiles.add(new WorldTile(x, y, ceilingL.getPlane()));
            }
        }
        ArrayList<WorldTile> checkedTeleTiles = new ArrayList<WorldTile>();
        for (WorldTile checkTile : unCheckedTeleTiles) {
            if (instance.ignoreTile(checkTile) || !World.isTileFree(checkTile.getPlane(), checkTile.getX(), checkTile.getY(), 1))
                continue;
            checkedTeleTiles.add(checkTile);
        }
        int farestDistance = Integer.MAX_VALUE;
        int index = 0;
        for (int i = 0; i < checkedTeleTiles.size(); i++) {
            WorldTile checkedTile = checkedTeleTiles.get(i);
            int distance = Utils.getDistance(checkedTile, playerL);
            if (distance < farestDistance) {
                index = i;
                farestDistance = distance;
            }
        }
        return checkedTeleTiles.isEmpty() ? null : checkedTeleTiles.get(index);
    }

    // (3104, 6111, 0)

    public void removeMiddleCeilingObject() {
        WorldTile middleCeilingLocation = getTile(new WorldTile(3035, 5980, 0));
        if (getCeilingIndex(middleCeilingLocation) == -1)
            return;
        WorldObject middleCeiling = ceilingCollapses[getCeilingIndex(middleCeilingLocation)];
        if (middleCeiling == null)
            return;
        World.removeObject(middleCeiling);
        ceilingCollapses[getCeilingIndex(middleCeilingLocation)] = null;
    }

    public boolean isStuckUnderRock(Player player, WorldTile location) {
        if (location != null) {
            return player.getX() >= (location.getX() + 1) && player.getX() <= (location.getX() + 8) && player.getY() >= (location.getY() + 1) && player.getY() <= (location.getY() + 8);
        }
        for (WorldObject ceiling : ceilingCollapses) {
            if (ceiling == null)
                continue;
            if (player.getX() >= (ceiling.getX() + 1) && player.getX() <= (ceiling.getX() + 8) && player.getY() >= (ceiling.getY() + 1) && player.getY() <= (ceiling.getY() + 8))
                return true;
        }
        return false;
    }

    public int getPlayersUnderRock(WorldTile ceilingLocation) {
        int count = 0;
        for (Player player : instance.getPlayersOnBattle()) {
            if (player == null || player.isDead() || !isStuckUnderRock(player, ceilingLocation))
                continue;
            count++;
        }
        return count;
    }

    public int getCeilingIndex(WorldTile tile) {
        for (int i = 0; i < ceilingCollapses.length; i++) {
            WorldObject ceiling = ceilingCollapses[i];
            if (ceiling == null)
                continue;
            if (ceiling.getX() == tile.getX() && ceiling.getY() == tile.getY() && ceiling.getPlane() == tile.getPlane())
                return i;
        }
        return -1;
    }

    public void sendCrackGround() {
        boolean hardMode = instance.getSettings().isHardMode();
        int[] xstart = new int[hardMode ? 9 : 4];
        int[] ystart = new int[hardMode ? 9 : 4];
        if (hardMode) {
            xstart[0] = 3090;
            ystart[0] = 6098;
            xstart[1] = 3026;
            ystart[1] = 6034;
            xstart[2] = 3090;
            ystart[2] = 6034;
            xstart[3] = 3026;
            ystart[3] = 6034;
            xstart[4] = 3090;
            ystart[4] = 6034;
            xstart[5] = 3026;
            ystart[5] = 6034;
            xstart[6] = 3090;
            ystart[6] = 6034;
            xstart[7] = 3026;
            ystart[7] = 6034;
            xstart[8] = 3026;
            ystart[8] = 5970;
            if ((phase - 1) == 9 || (phase - 1) == 10)
                return;
        } else {
            xstart[0] = 3090;
            ystart[0] = 6098;
            xstart[1] = 3026;
            ystart[1] = 6034;
            xstart[2] = 3090;
            ystart[2] = 6034;
            xstart[3] = 3026;
            ystart[3] = 5970;
            if ((phase - 1) == 4)
                return;
        }
        World.spawnObjectTemporary(new WorldObject(84873, 10, 1, getTile(new WorldTile(xstart[phase - 1], ystart[phase - 1], 0))), 3000, true, true);
        World.spawnObjectTemporary(new WorldObject(84871, 10, 1, getTile(new WorldTile(xstart[phase - 1], ystart[phase - 1] + 9, 0))), 3000, true, true);
        World.spawnObjectTemporary(new WorldObject(84873, 10, 2, getTile(new WorldTile(xstart[phase - 1], ystart[phase - 1] + 18, 0))), 3000, true, true);
        World.spawnObjectTemporary(new WorldObject(84871, 10, 0, getTile(new WorldTile(xstart[phase - 1] + 9, ystart[phase - 1], 0))), 3000, true, true);
        World.spawnObjectTemporary(new WorldObject(84869, 10, 0, getTile(new WorldTile(xstart[phase - 1] + 9, ystart[phase - 1] + 9, 0))), 3000, true, true);
        World.spawnObjectTemporary(new WorldObject(84871, 10, 2, getTile(new WorldTile(xstart[phase - 1] + 9, ystart[phase - 1] + 18, 0))), 3000, true, true);
        World.spawnObjectTemporary(new WorldObject(84873, 10, 0, getTile(new WorldTile(xstart[phase - 1] + 18, ystart[phase - 1], 0))), 3000, true, true);
        World.spawnObjectTemporary(new WorldObject(84871, 10, 3, getTile(new WorldTile(xstart[phase - 1] + 18, ystart[phase - 1] + 9, 0))), 3000, true, true);
        World.spawnObjectTemporary(new WorldObject(84873, 10, 3, getTile(new WorldTile(xstart[phase - 1] + 18, ystart[phase - 1] + 18, 0))), 3000, true, true);
    }

    public void spawnGravityField() {
        if (phaseProgress == 5)
            return;
        getTemporaryAttributtes().remove("FiringGravityField");
        if (gravityField != null) {
            if (World.containsObjectWithId(gravityField, gravityField.getId()))
                World.removeObject(gravityField);
        }
        WorldTile wrongLocation = locations[Utils.random(8)];
        WorldTile spawnTile = instance.getTile(new WorldTile(wrongLocation.getX() - 64, wrongLocation.getY() - 64, wrongLocation.getPlane()));
        gravityField = new WorldObject(84959, 10, 1, spawnTile);
        World.spawnObject(gravityField);
    }

    public void fireGravityField(Player player) {
        if (player.isLocked())
            return;
        if (getTemporaryAttributtes().get("FiringGravityField") != null)
            return;
        if (!Utils.isOnRange(gravityField.transform(1, 1, 0), this, 8, 1, getSize())) {
            instance.sendMessage(player, 1, "<col=ff0000>Vorago is not close enough!</col>");
            return;
        }
        getTemporaryAttributtes().put("FiringGravityField", Boolean.TRUE);
        instance.sendMessage(player, 1, "<col=ffffff>Vorago feels the pull of the gravity field , which destablises him.</col>");
        player.lock(1);
        Projectile projectile = World.sendProjectileCycles(gravityField.transform(1, 1, 0), this, 4029, 10, 50, 0, 90, 0, 0);
        long projectileCycles = Utils.projectileTimeToMiliseconds(projectile.getStartTime() + projectile.getEndTime() - 1);
        World.removeObject(gravityField);
        phaseProgress++;
        instance.updateInterface(false);
        gravityField = null;
        CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
            @Override
            public boolean repeat() {
                try {
                    setNextGraphics(new Graphics(4030));
                    getTemporaryAttributtes().remove("FiringGravityField");
                    if (phaseProgress == 4) {
                        sendBringHimDown();
                    }
                    return false;
                } catch (Exception e) {
                    Logger.getGlobal().catching(e);
                    return false;
                }
            }
        }, projectileCycles, 600, TimeUnit.MILLISECONDS);

    }

    @Override
    public void setFinished(boolean finished) {
        removeLeftOvers();
        super.setFinished(finished);
    }

    public WorldTile getRandomUsableLocation() {
        return getRandomUsableLocation(null);
    }

    public WorldTile getRandomUsableLocation(WorldTile exception) {
        return getRandomUsableLocation(exception, false);
    }

    public WorldTile getRandomUsableLocation(WorldTile exception, boolean atPhaseFinish) {
        boolean hardMode = instance.getSettings().isHardMode();
        WorldTile[] locs = new WorldTile[9];
        if (hardMode) {
            for (int i = 0; i < locations.length; i++) {
                switch (phase) {
                    case 1:
                        locs[i] = getTile(locations[i]);
                        break;
                    case 2:
                        locs[i] = getTile(new WorldTile(locations[i].getX() - 64, locations[i].getY() - 64, locations[i].getPlane()));
                        break;
                    case 3:
                        locs[i] = getTile(new WorldTile(locations[i].getX(), locations[i].getY() - 64, locations[i].getPlane()));
                        break;
                    case 4:
                        locs[i] = getTile(new WorldTile(locations[i].getX() - 64, locations[i].getY() - 64, locations[i].getPlane()));
                        break;
                    case 5:
                        locs[i] = getTile(new WorldTile(locations[i].getX(), locations[i].getY() - 64, locations[i].getPlane()));
                        break;
                    case 6:
                        locs[i] = getTile(new WorldTile(locations[i].getX() - 64, locations[i].getY() - 64, locations[i].getPlane()));
                        break;
                    case 7:
                        locs[i] = getTile(new WorldTile(locations[i].getX(), locations[i].getY() - 64, locations[i].getPlane()));
                        break;
                    case 8:
                        locs[i] = getTile(new WorldTile(locations[i].getX() - 64, locations[i].getY() - 64, locations[i].getPlane()));
                        break;
                    case 9:
                        locs[i] = getTile(new WorldTile(locations[i].getX() - 64, locations[i].getY() - 128, locations[i].getPlane()));
                        break;
                }
            }
        } else {
            for (int i = 0; i < locations.length; i++) {
                switch (phase) {
                    case 1:
                        locs[i] = getTile(locations[i]);
                        break;
                    case 2:
                        locs[i] = getTile(new WorldTile(locations[i].getX() - 64, locations[i].getY() - 64, locations[i].getPlane()));
                        break;
                    case 3:
                        locs[i] = getTile(new WorldTile(locations[i].getX(), locations[i].getY() - 64, locations[i].getPlane()));
                        break;
                    case 4:
                        locs[i] = getTile(new WorldTile(locations[i].getX() - 64, locations[i].getY() - 128, locations[i].getPlane()));
                        break;
                }
            }
        }
        List<WorldTile> availableLocations = new ArrayList<WorldTile>();
        if ((!hardMode && phase == 5) || (hardMode && phase == 10)) {
            WorldTile[] locationsPhase510 = {getTile(new WorldTile(3092, 5985, 0)), getTile(new WorldTile(3094, 5985, 0)), getTile(new WorldTile(3104, 5985, 0)), getTile(new WorldTile(3106, 5985, 0))};
            for (int i = 0; i < locationsPhase510.length; i++) {
                WorldTile checkLocation = locationsPhase510[i];
                if ((exception != null && exception.matches(checkLocation)) || ((getX() - 2) < checkLocation.getX()))
                    continue;
                availableLocations.add(checkLocation);
            }
            if (availableLocations.isEmpty()) {
                availableLocations.add(locationsPhase510[0]);
                availableLocations.add(locationsPhase510[1]);
            }
        } else if (hardMode && phase == 11) {
            WorldTile[] locationsPhase11 = {getTile(new WorldTile(3112, 5985, 0)), getTile(new WorldTile(3110, 5985, 0)), getTile(new WorldTile(3100, 5985, 0)), getTile(new WorldTile(3102, 5985, 0))};
            for (int i = 0; i < locationsPhase11.length; i++) {
                WorldTile checkLocation = locationsPhase11[i];
                if ((exception != null && exception.matches(checkLocation)) || ((getX() + 2) > checkLocation.getX()))
                    continue;
                availableLocations.add(checkLocation);
            }
            if (availableLocations.isEmpty()) {
                availableLocations.add(locationsPhase11[0]);
                availableLocations.add(locationsPhase11[1]);
            }
        } else {
            for (int i = 0; i < locs.length; i++) {
                WorldTile checkLocation = locs[i];
                if (!atPhaseFinish && ((World.containsObjectWithId(new WorldTile(checkLocation.getX() - 5, checkLocation.getY() - 3, checkLocation.getPlane()), 84962) || World.containsObjectWithId(new WorldTile(checkLocation.getX() - 5, checkLocation.getY() - 3, checkLocation.getPlane()), 87322)) || (exception != null && checkLocation.matches(exception))))
                    continue;
                availableLocations.add(checkLocation);
            }
        }
        if (availableLocations.isEmpty())
            return locs[Utils.random(locs.length)];
        return availableLocations.get(Utils.random(availableLocations.size()));
    }

    public int getMoveDirection(Player player) {
        switch (player.getDirection()) {
            case 10240:// north-east
                return ForceMovement.NORTH_EAST;
            case 6144:// north-west
                return ForceMovement.NORTH_WEST;
            case 2048:// south-west
                return ForceMovement.SOUTH_WEST;
            case 14336:// south-east
                return ForceMovement.SOUTH_EAST;
            case 4096:// west
                return ForceMovement.WEST;
            case 12288:// east
                return ForceMovement.EAST;
            case 8192:// north
                return ForceMovement.NORTH;
            case 0:// south
            default:
                return ForceMovement.SOUTH;
        }
    }

    public boolean canFinishPhase() {
        boolean hardMode = instance.getSettings().isHardMode();
        if (hardMode) {
            switch (phase) {
                case 1:
                    return phaseProgress == (Settings.VORAGO_JUMP_DISABLED ? 0 : 1);
                case 2:
                    return phaseProgress == 5;
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                    return phaseProgress == 0;
                case 9:
                    return phaseProgress == 3;
                case 10:
                case 11:
                    return false;
            }
        } else {
            switch (phase) {
                case 1:
                    return phaseProgress == (Settings.VORAGO_JUMP_DISABLED ? 0 : 1);
                case 2:
                    return phaseProgress == 5;
                case 3:
                    return phaseProgress == 0;
                case 4:
                    return phaseProgress == 3;
                case 5:
                    return false;
            }
        }
        return false;
    }

    public int getPhase() {
        return phase;
    }

    public void setPhase(int phase) {
        this.phase = phase;
    }

    public int getPhaseProgress() {
        return phaseProgress;
    }

    public void setPhaseProgress(int phaseProgress) {
        this.phaseProgress = phaseProgress;
        instance.updateInterface(false);
    }

    public int getAttackProgress() {
        return attackProgress;
    }

    public void setAttackProgress(int attackProgress) {
        this.attackProgress = attackProgress;
    }

    public WorldObject[] getCeilingColapses() {
        return ceilingCollapses;
    }

    public WorldObject getGravityField() {
        return gravityField;
    }

    public Player getTargetedPlayer() {
        return targetedPlayer;
    }

    public void setTargetedPlayer(Player targetedPlayer) {
        this.targetedPlayer = targetedPlayer;
    }

    public int getBringHimPoints() {
        return bringHimPoints;
    }

    public void setBringHimPoints(int bringHimPoints) {
        this.bringHimPoints = bringHimPoints;
        instance.updateInterface(false);
    }

    public WorldTile getTile(WorldTile tile) {
        return instance.getTile(tile);
    }

    public VoragoInstance getVoragoInstance() {
        return instance;
    }

    @Override
    public int getMaxHitpoints() {
        return 25000;
    }

    public void giveFirstWeaponPiece() {
        WorldTile tile = instance.randomSpawnTile(null, instance.getVoragoSpawnLocation(phase), 5);
        weaponPieces[0] = World.addWeaponPiece(new Item(28600), tile);
    }

    public VoragoInstance getInstance() {
        return instance;
    }

    public int getGroupSize() {
        return groupSize;
    }

    public boolean isAtLastPhase() {
        boolean hardMode = instance.getSettings().isHardMode();
        return !hardMode && phase == 5 || hardMode && phase >= 10;
    }

    public static List<VoragoAttacks> repeatedAttack(VoragoAttacks attack, int repeatCount) {
        VoragoAttacks[] attacks = new VoragoAttacks[repeatCount];
        for (int i = 0; i < attacks.length; i++)
            attacks[i] = attack;
        return Arrays.asList(attacks);
    }

    public static List<VoragoAttacks> defaultPhase3(VoragoAttacks specialAttack) {
        return Arrays.asList(specialAttack, VoragoAttacks.AUTO_ATTACK, VoragoAttacks.BLUE_BOMB_ATTACK_NO_ANIM, VoragoAttacks.AUTO_ATTACK, VoragoAttacks.REFLECT_ATTACK, VoragoAttacks.AUTO_ATTACK, VoragoAttacks.AUTO_ATTACK, VoragoAttacks.AUTO_ATTACK, specialAttack, VoragoAttacks.AUTO_ATTACK, VoragoAttacks.BLUE_BOMB_ATTACK_NO_ANIM, VoragoAttacks.AUTO_ATTACK, VoragoAttacks.RED_BOMB, VoragoAttacks.AUTO_ATTACK, VoragoAttacks.AUTO_ATTACK, VoragoAttacks.AUTO_ATTACK, VoragoAttacks.AUTO_ATTACK);
    }

    public static class MultibleAttack {

        private final int delay;
        private final VoragoAttacks[] attacks;

        public MultibleAttack(int delay, VoragoAttacks... attacks) {
            this.delay = delay;
            this.attacks = attacks;
        }

        public boolean contains(VoragoAttacks attack) {
            for (VoragoAttacks att : attacks)
                if (att == attack)
                    return true;
            return false;
        }

        public int getDelay() {
            return delay;
        }

        public VoragoAttacks[] getAttacks() {
            return attacks;
        }

    }

    private List<Object> attacks;

    public void generateAttackList() {
        boolean hardMode = instance.getSettings().isHardMode();
        int phase = hardMode ? ((this.phase >= 3 && this.phase <= 8) ? 3 : this.phase == 9 ? 4 : this.phase) : this.phase;
        int rot = hardMode && phase == 3 ? ((this.phase >= 3 && this.phase <= 8) ? (this.phase - 3) : Settings.VORAGO_ROTATION) : Settings.VORAGO_ROTATION;
        attacks = new ArrayList<Object>();
        switch (phase) {
            case 1:
                attacks.add(VoragoAttacks.RED_BOMB);
                attacks.addAll(repeatedAttack(VoragoAttacks.AUTO_ATTACK, 4));
                break;
            case 2:
                attacks.addAll(smashesAttack());
                attacks.addAll(repeatedAttack(VoragoAttacks.AUTO_ATTACK, 3));
                attacks.add(new MultibleAttack(4, VoragoAttacks.REFLECT_ATTACK, VoragoAttacks.GRAVITY_FIELD_ATTACK));
                attacks.addAll(repeatedAttack(VoragoAttacks.AUTO_ATTACK, 3));
                attacks.add(VoragoAttacks.RED_BOMB);
                attacks.addAll(repeatedAttack(VoragoAttacks.AUTO_ATTACK, 4));
                break;
            case 3:
                switch (rot) {
                    case 0:
                    case 4:
                    case 5:
                        attacks.addAll(defaultPhase3(rot == 0 ? VoragoAttacks.CEILING_COLLAPSE_ATTACK : rot == 4 ? VoragoAttacks.TEAM_SPLIT_ATTACK : VoragoAttacks.THE_END_ATTACK));
                        break;
                    case 1:
                        attacks.add(VoragoAttacks.SCOPULI_ATTACK);
                        attacks.add(VoragoAttacks.BLUE_BOMB_ATTACK);
                        break;
                    case 2:
                        attacks.add(VoragoAttacks.VITALIS_ORB_ATTACK);
                        attacks.addAll(repeatedAttack(VoragoAttacks.AUTO_ATTACK, 3));
                        attacks.add(VoragoAttacks.REFLECT_ATTACK);
                        attacks.addAll(repeatedAttack(VoragoAttacks.AUTO_ATTACK, 3));
                        attacks.add(VoragoAttacks.VITALIS_ORB_ATTACK);
                        attacks.addAll(repeatedAttack(VoragoAttacks.AUTO_ATTACK, 3));
                        attacks.addAll(smashesAttack());
                        attacks.addAll(repeatedAttack(VoragoAttacks.AUTO_ATTACK, 3));
                        break;
                    case 3:
                        attacks.add(VoragoAttacks.GREEN_BOMB_ATTACK);
                        attacks.addAll(repeatedAttack(VoragoAttacks.AUTO_ATTACK, 5));
                        attacks.add(VoragoAttacks.REFLECT_ATTACK);
                        attacks.addAll(repeatedAttack(VoragoAttacks.AUTO_ATTACK, 3));
                        attacks.add(VoragoAttacks.GREEN_BOMB_ATTACK);
                        attacks.addAll(repeatedAttack(VoragoAttacks.AUTO_ATTACK, 5));
                        attacks.add(VoragoAttacks.RED_BOMB);
                        attacks.addAll(repeatedAttack(VoragoAttacks.AUTO_ATTACK, 4));
                        break;
                }
                break;
            case 4:
                attacks.add(VoragoAttacks.WATER_FALL_ATTACK);
                attacks.addAll(repeatedAttack(VoragoAttacks.AUTO_ATTACK, 3));
                attacks.add(VoragoAttacks.STONE_CLONE_ATTACK);
                attacks.addAll(repeatedAttack(VoragoAttacks.AUTO_ATTACK, 7));
                attacks.add(rot == 0 ? VoragoAttacks.CEILING_COLLAPSE_ATTACK : rot == 1 ? VoragoAttacks.RED_BOMB : rot == 2 ? VoragoAttacks.VITALIS_ORB_ATTACK : rot == 3 ? VoragoAttacks.GREEN_BOMB_ATTACK : rot == 4 ? VoragoAttacks.TEAM_SPLIT_ATTACK : VoragoAttacks.THE_END_ATTACK);
                attacks.addAll(repeatedAttack(VoragoAttacks.AUTO_ATTACK, rot == 1 ? 4 : rot == 3 ? 5 : 3));
                attacks.addAll(smashesAttack());
                attacks.addAll(repeatedAttack(VoragoAttacks.AUTO_ATTACK, rot == 5 ? 4 : 3));
                attacks.add(VoragoAttacks.REFLECT_ATTACK);
                attacks.addAll(repeatedAttack(VoragoAttacks.AUTO_ATTACK, 3));
                break;
            case 5:
                attacks.add(rot == 0 ? VoragoAttacks.TEAM_SPLIT_ATTACK : rot == 1 ? VoragoAttacks.PURPLE_BOMB_ATTACK : rot == 2 ? VoragoAttacks.VITALIS_ORB_ATTACK : rot == 3 ? VoragoAttacks.GREEN_BOMB_ATTACK : rot == 4 ? VoragoAttacks.TEAM_SPLIT_ATTACK : VoragoAttacks.PURPLE_BOMB_ATTACK);
                attacks.addAll(repeatedAttack(VoragoAttacks.AUTO_ATTACK, rot == 1 || rot == 3 || rot == 5 ? 5 : 3));
                attacks.add(VoragoAttacks.REFLECT_ATTACK);
                attacks.addAll(repeatedAttack(VoragoAttacks.AUTO_ATTACK, 3));
                attacks.addAll(repeatedAttack(VoragoAttacks.SMASH_ATTACK, 3));
                attacks.addAll(repeatedAttack(VoragoAttacks.AUTO_ATTACK, 3));
                break;
            case 10:
                attacks.add(rot == 0 ? VoragoAttacks.TEAM_SPLIT_ATTACK : rot == 1 ? VoragoAttacks.PURPLE_BOMB_ATTACK : rot == 2 ? VoragoAttacks.VITALIS_ORB_ATTACK : rot == 3 ? VoragoAttacks.GREEN_BOMB_ATTACK : rot == 4 ? VoragoAttacks.TEAM_SPLIT_ATTACK : VoragoAttacks.PURPLE_BOMB_ATTACK);
                attacks.addAll(repeatedAttack(VoragoAttacks.AUTO_ATTACK, rot == 1 || rot == 3 || rot == 5 ? 5 : 3));
                attacks.add(VoragoAttacks.REFLECT_ATTACK);
                if (rot == 5)
                    attacks.addAll(repeatedAttack(VoragoAttacks.SMASH_ATTACK, 3));
                else
                    attacks.add(rot == 0 ? VoragoAttacks.GREEN_BOMB_ATTACK : rot == 1 ? VoragoAttacks.TEAM_SPLIT_ATTACK : rot == 2 ? VoragoAttacks.PURPLE_BOMB_ATTACK : rot == 3 ? VoragoAttacks.VITALIS_ORB_ATTACK : VoragoAttacks.TEAM_SPLIT_ATTACK);

                attacks.addAll(repeatedAttack(VoragoAttacks.AUTO_ATTACK, rot == 0 || rot == 2 ? 5 : 3));
                break;
            case 11:
                attacks.add(rot == 0 ? VoragoAttacks.TEAM_SPLIT_ATTACK : rot == 1 ? VoragoAttacks.PURPLE_BOMB_ATTACK : rot == 2 ? VoragoAttacks.VITALIS_ORB_ATTACK : rot == 3 ? VoragoAttacks.GREEN_BOMB_ATTACK : rot == 4 ? VoragoAttacks.TEAM_SPLIT_ATTACK : VoragoAttacks.PURPLE_BOMB_ATTACK);
                attacks.addAll(repeatedAttack(VoragoAttacks.AUTO_ATTACK, rot == 1 || rot == 3 || rot == 5 ? 5 : 3));
                attacks.add(VoragoAttacks.REFLECT_ATTACK);
                if (rot == 2)
                    attacks.addAll(repeatedAttack(VoragoAttacks.SMASH_ATTACK, 3));
                else
                    attacks.add(rot == 0 ? VoragoAttacks.VITALIS_ORB_ATTACK : rot == 1 ? VoragoAttacks.VITALIS_ORB_ATTACK : rot == 3 ? VoragoAttacks.TEAM_SPLIT_ATTACK : rot == 4 ? VoragoAttacks.PURPLE_BOMB_ATTACK : VoragoAttacks.VITALIS_ORB_ATTACK);
                attacks.addAll(repeatedAttack(VoragoAttacks.AUTO_ATTACK, rot == 4 ? 5 : 3));
                break;
        }
    }

    public List<Object> getAttacks() {
        return attacks;
    }

    public static List<Object> smashesAttack() {
        List<Object> attacks = new ArrayList<Object>();
        attacks.addAll(repeatedAttack(VoragoAttacks.SMASH_ATTACK, 2));
        attacks.add(new MultibleAttack(4, VoragoAttacks.BLUE_BOMB_ATTACK_NO_ANIM, VoragoAttacks.SMASH_ATTACK));
        attacks.addAll(repeatedAttack(VoragoAttacks.SMASH_ATTACK, 2));
        return attacks;
    }

    public enum VoragoAttacks {
        ATTACK() {
            @Override
            public int sendAttack(Vorago vorago, Player target) {
                if (vorago.sentDeath) {
                    vorago.getCombat().removeTarget();
                    vorago.setNextFaceEntity(null);
                    return 0;
                }
                vorago.getTemporaryAttributtes().remove("cantMovePlayers");
                boolean skip = vorago.getTemporaryAttributtes().get("skippedVitalis") != null && vorago.getAttackProgress() == 4;
                int attackProgress = skip ? 7 : vorago.getAttackProgress();
                if (skip)
                    vorago.getTemporaryAttributtes().remove("skippedVitalis");
                if (vorago.getAttacks() == null || vorago.getAttacks().isEmpty())
                    vorago.generateAttackList();
                Object attack = vorago.getAttacks().get(attackProgress);
                if (attack instanceof MultibleAttack) {
                    MultibleAttack att = (MultibleAttack) attack;
                    for (VoragoAttacks t : att.getAttacks())
                        t.sendAttack(vorago, target);
                    vorago.setAttackProgress(attackProgress + 1 >= vorago.getAttacks().size() ? 0 : attackProgress + 1);
                    return att.getDelay();
                }
                VoragoAttacks att = (VoragoAttacks) attack;
                if (att == VoragoAttacks.CEILING_COLLAPSE_ATTACK && !vorago.canSendCeilingCollapse())
                    att = VoragoAttacks.AUTO_ATTACK;
                if (att == VoragoAttacks.VITALIS_ORB_ATTACK && vorago.cantSendVitalisOrb()) {
                    att = VoragoAttacks.RED_BOMB;
                    vorago.getTemporaryAttributtes().put("skippedVitalis", Boolean.TRUE);
                }
                if (att == VoragoAttacks.SCOPULI_ATTACK)
                    vorago.getAttacks().removeIf(VoragoAttacks.SCOPULI_ATTACK::equals);

                int delay = att.sendAttack(vorago, target);
                if (vorago.isAtLastPhase() && att == VoragoAttacks.AUTO_ATTACK) {
                    if (delay == 7)
                        vorago.setAttackProgress(attackProgress + 1 >= vorago.getAttacks().size() ? 0 : attackProgress + 1);
                } else
                    vorago.setAttackProgress(attackProgress + 1 >= vorago.getAttacks().size() ? 0 : attackProgress + 1);
                Object nextAtt = vorago.getAttacks().get(vorago.getAttackProgress());
                if (nextAtt instanceof MultibleAttack && ((MultibleAttack) nextAtt).contains(VoragoAttacks.SMASH_ATTACK))
                    nextAtt = VoragoAttacks.SMASH_ATTACK;
                if (nextAtt != VoragoAttacks.SMASH_ATTACK)
                    vorago.setForceFollowClose(false);
                else
                    vorago.setForceFollowClose(!vorago.isAtLastPhase());
                return delay;
            }
        },

        MELEE_ATTACK() {
            @Override
            public int sendAttack(Vorago vorago, Player target) {
                ArrayList<Entity> possibleTargets = new ArrayList<Entity>();
                if (vorago.getPossibleTargets().isEmpty())
                    return 0;
                for (Entity e : vorago.getPossibleTargets()) {
                    if (Utils.isOnRange(vorago, e, 0))
                        possibleTargets.add(e);
                }
                for (Entity e : vorago.getPossibleTargets()) {
                    if (!possibleTargets.contains(e) && Utils.isOnRange(target, e, 1))
                        possibleTargets.add(e);
                }
                if (possibleTargets.isEmpty())
                    return 0;
                vorago.setNextAnimation(new Animation(20355));
                for (Entity e : possibleTargets) {
                    if (e == null || e.hasFinished() || e.isDead())
                        continue;
                    CombatScript.delayHit(vorago, 0, e, CombatScript.getMeleeHit(vorago, CombatScript.getMaxHit(vorago, 500, NPCCombatDefinitionConstants.MELEE, e)));
                }
                return 6;
            }

        },
        BLUE_BOMB_ATTACK() {
            @Override
            public int sendAttack(Vorago vorago, Player target) {
                vorago.sendBlueBombAttack();
                return 7;
            }
        },
        BLUE_BOMB_ATTACK_NO_ANIM() {// just like araxxor return delay in ticks.

            @Override
            public int sendAttack(Vorago vorago, Player target) {
                vorago.sendBlueBombAttack(false);
                return 7;
            }
        },

        AUTO_ATTACK() {
            @Override
            public int sendAttack(Vorago vorago, Player target) {
                vorago.setForceFollowClose(false);
                VoragoAttacks attack = VoragoAttacks.MELEE_ATTACK;
                double blueBombChance = 0.5;
                if (!Utils.isOnRange(vorago, target, 0) || Math.random() <= blueBombChance)
                    attack = VoragoAttacks.BLUE_BOMB_ATTACK;
                int delay = attack.sendAttack(vorago, target);
                return delay;
            }
        },

        RED_BOMB() {
            @Override
            public int sendAttack(Vorago vorago, Player target) {
                if (vorago.getPossibleTargets().isEmpty())
                    return 0;
                double randomPlayerChance = 0.015;
                Entity t = Math.random() <= randomPlayerChance ? vorago.getPossibleTargets().get(Utils.random(vorago.getPossibleTargets().size())) : getFarestEntity(vorago.getPossibleTargets(), vorago, null);
                boolean hardMode = vorago.getInstance().getSettings().isHardMode();
                vorago.getInstance().sendMessage(target, 1, "<col=ff0000>Vorago has sent a bomb after you. Run!</col>");
                vorago.setNextAnimation(new Animation(20371));
                vorago.setNextGraphics(new Graphics(4022));
                vorago.sendGroundBlueBomb(new WorldTile(t));
                Projectile projectile = World.sendProjectileCycles(vorago, t, 4023, 130, 20, 45, 320, Utils.random(5), 270);
                long projectileCycles = Utils.projectileTimeToMiliseconds(projectile.getStartTime() + projectile.getEndTime() - 1);
                CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
                    @Override
                    public boolean repeat() {
                        try {
                            if (!(t instanceof Player) || vorago.checkStopAttack((Player) t))
                                return false;
                            int damage = 200 + (hardMode ? 100 : 0);
                            for (Entity e : vorago.getPossibleTargets()) {
                                if (e == null || e.hasFinished() || e.isDead() || e == t || !Utils.isOnRange(t, e, 3))
                                    continue;
                                damage += 100 + (hardMode ? 50 : 0);
                            }
                            if (damage > (hardMode ? 1050 : 700))
                                damage = hardMode ? 1050 : 700;
                            t.setNextGraphics(new Graphics(4024));
                            World.sendGraphics(vorago, new Graphics(3522), new WorldTile(t.getX(), t.getY(), t.getPlane()));
                            for (Entity e : vorago.getPossibleTargets()) {
                                if (e == null || e.hasFinished() || e.isDead() || !Utils.isOnRange(t, e, 1))
                                    continue;
                                CombatScript.delayHit(vorago, 0, e, new Hit(vorago, damage, HitLook.REGULAR_DAMAGE));
                            }
                            ((Player) t).getCombatDefinitions().decreaseSpecialAttack(5);
                            return false;
                        } catch (Exception e) {
                            Logger.getGlobal().catching(e);
                            return false;
                        }
                    }
                }, projectileCycles, 600, TimeUnit.MILLISECONDS);
                return 6;
            }
        },

        SMASH_ATTACK() {
            public int sendAttack(Vorago vorago, Player target) {
                boolean hardMode = vorago.getVoragoInstance().getSettings().isHardMode();
                Entity t = ((!hardMode && vorago.getPhase() == 5) || (hardMode && vorago.getPhase() >= 10)) ? vorago.getPossibleTargets().get(Utils.random(vorago.getPossibleTargets().size())) : target;
                int damage = hardMode ? 450 : 300;
                // int smashDamage = hardMode ? (vorago.getPhase() >= 10 ? 750 : 1000) :
                // (vorago.getPhase() == 5 ? 450 : 600);
                if ((!hardMode && vorago.getPhase() != 5) || (hardMode && vorago.getPhase() < 10)) {
                    vorago.getTemporaryAttributtes().put("VoragoType", 0);
                    vorago.transform();
                }
                vorago.setNextAnimation(new Animation(20363));
                vorago.setNextGraphics(new Graphics(4018));
                t.setNextGraphics(new Graphics(4019));
                CombatScript.delayHit(vorago, 0, t, new Hit(vorago, damage, HitLook.REGULAR_DAMAGE));
                return 6;
            }
        },
        REFLECT_ATTACK() {
            @Override
            public int sendAttack(Vorago vorago, Player target) {
                vorago.setNextAnimation(new Animation(20319));
                vorago.setNextGraphics(new Graphics(4011));
                vorago.setTargetedPlayer((Player) vorago.getPossibleTargets().get(Utils.random(vorago.getPossibleTargets().size())));
                Player targetedPlayer = vorago.getTargetedPlayer();
                for (Player player : vorago.getVoragoInstance().getPlayersOnBattle()) {
                    if (player == null || player.isDead())
                        continue;
                    boolean isTargetedPlayer = player == targetedPlayer;
                    vorago.getInstance().sendMessage(player, 1, isTargetedPlayer ? "<col=ff0000>Vorago channels incoming damage to you. Beware!</col>" : "<col=FFFFFF>Vorago reflects incoming damage to surrounding foes!</col>");
                }
                targetedPlayer.setNextGraphics(new Graphics(4012));
                CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
                    @Override
                    public boolean repeat() {
                        try {
                            if (!vorago.isAtLastPhase()) {
                                vorago.getTemporaryAttributtes().put("VoragoType", 1);
                                vorago.transform();
                            }
                            vorago.setTargetedPlayer(null);
                            if (vorago.getTemporaryAttributtes().get("BringHimDownClick") != null) {
                                targetedPlayer.getPackets().sendGameMessage("<col=00FF00>Vorago releases his mental link on you.");
                            } else
                                vorago.getInstance().sendMessage(targetedPlayer, 0, "<col=00FF00>Vorago releases his mental link on you.");
                            return false;
                        } catch (Exception e) {
                            Logger.getGlobal().catching(e);
                            return false;
                        }
                    }
                }, 9600, 600, TimeUnit.MILLISECONDS);
                return 4;
            }
        },
        GRAVITY_FIELD_ATTACK() {
            public int sendAttack(Vorago vorago, Player target) {
                vorago.spawnGravityField();
                return 0;
            }
        },

        CEILING_COLLAPSE_ATTACK() {
            @Override
            public int sendAttack(Vorago vorago, Player target) {
                vorago.sendCeilingCollapse();
                return 4;
            }

        },

        TEAM_SPLIT_ATTACK() {
            @Override
            public int sendAttack(Vorago vorago, Player target) {
                vorago.sendTeamSplit();
                return 15;
            }

        },

        THE_END_ATTACK() {
            @Override
            public int sendAttack(Vorago vorago, Player target) {
                vorago.startTheEnd();
                return 78;
            }
        },

        PURPLE_BOMB_ATTACK() {
            @Override
            public int sendAttack(Vorago vorago, Player target) {
                vorago.sendPurpleBombAttack();
                return 4;
            }
        },

        SCOPULI_ATTACK() {
            @Override
            public int sendAttack(Vorago vorago, Player target) {
                vorago.spawnScopuli();
                return 0;
            }
        },

        VITALIS_ORB_ATTACK() {
            @Override
            public int sendAttack(Vorago vorago, Player target) {
                vorago.sendVitalisOrb();
                return 4;
            }
        },

        GREEN_BOMB_ATTACK() {
            @Override
            public int sendAttack(Vorago vorago, Player target) {
                vorago.sendGreenBomb();
                return 4;
            }
        },

        WATER_FALL_ATTACK() {
            public int sendAttack(Vorago vorago, Player target) {
                vorago.sendWaterFallAttack();
                return 19;
            }
        },

        STONE_CLONE_ATTACK() {
            public int sendAttack(Vorago vorago, Player target) {
                vorago.getTemporaryAttributtes().put("ReducedDamage", Boolean.TRUE);
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        vorago.getTemporaryAttributtes().remove("ReducedDamage");
                    }
                }, 33);
                for (Player player : vorago.getVoragoInstance().getPlayersOnBattle()) {
                    if (player == null || player.isDead())
                        continue;
                    player.getTemporaryAttributtes().remove("RecentlyKilledClone");
                }
                vorago.spawnStoneClones();
                return 4;
            }
        };

        public int sendAttack(Vorago vorago, Player target) {
            return 0;
        }

    }

    public static Entity getFarestEntity(ArrayList<Entity> possibleTargets, Entity from, Entity exception) {
        int farestDistance = 0;
        int index = 0;
        for (int i = 0; i < possibleTargets.size(); i++) {
            Entity player = possibleTargets.get(i);
            if (player == null || player.isDead() || !Utils.isOnRange(from, player, 14) || (exception != null && player == exception))
                continue;
            int distance = Utils.getDistance(from, player);
            if (distance > farestDistance) {
                index = i;
                farestDistance = distance;
            }
        }
        return possibleTargets.get(index);
    }

    public void sendBlueBombAttack() {
        sendBlueBombAttack(true);
    }

    public void sendBlueBombAttack(boolean sendAnimation) {
        ArrayList<Entity> targets = new ArrayList<Entity>();
        if (getPossibleTargets().isEmpty())
            return;
        Entity firstTarget = getFarestEntity(getPossibleTargets(), this, null);
        if (firstTarget == null)
            return;
        targets.add(firstTarget);
        boolean hardMode = getInstance().getSettings().isHardMode();
        boolean overwrite = true;
        if ((hardMode || getGroupSize() >= 11) && (getPossibleTargets().size() > (overwrite ? 0 : 1))) {
            double randomPlayerChance = 0.015 + (0.015 * (getGroupSize() - 11)) >= 0.1 ? 0.1 : 0.015 + (0.015 * (getGroupSize() - 11));
            if (Math.random() <= randomPlayerChance) {
                Entity randomTarget = null;
                while (randomTarget == null || (!overwrite && randomTarget == firstTarget))
                    randomTarget = getPossibleTargets().get(Utils.random(getPossibleTargets().size()));
                targets.add(randomTarget);
            } else
                targets.add(getFarestEntity(getPossibleTargets(), this, overwrite ? null : firstTarget));
        }
        if (targets.isEmpty())
            return;
        if (sendAnimation)
            setNextAnimation(new Animation(20356));
        setNextGraphics(new Graphics(4015));
        for (int i = 0; i < targets.size(); i++) {
            Entity e = targets.get(i);
            if (e == null || e.hasFinished() || e.isDead())
                continue;
            Projectile projectile = World.sendProjectileCycles(this, e, 4016, 70 + i * 15, 23, 30, 190, Utils.random(5), 200);
            long projectileCycles = Utils.projectileTimeToMiliseconds(projectile.getStartTime() + projectile.getEndTime() - 1);
            Vorago thisVorago = this;
            CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
                @Override
                public boolean repeat() {
                    try {
                        if (!(e instanceof Player) || checkStopAttack((Player) e))
                            return false;
                        e.setNextGraphics(new Graphics(4017));
                        for (Entity t : getPossibleTargets()) {
                            if (t == null || t.hasFinished() || t.isDead() || !Utils.isOnRange(e, t, 2))
                                continue;
                            int damage = CombatScript.getMaxHit(thisVorago, 500, NPCCombatDefinitionConstants.MAGE, t);
                            CombatScript.delayHit(thisVorago, 0, t, CombatScript.getMagicHit(thisVorago, damage));
                        }
                        return false;
                    } catch (Exception e) {
                        Logger.getGlobal().catching(e);
                        return false;
                    }
                }
            }, projectileCycles, 600, TimeUnit.MILLISECONDS);
        }
    }

    public void sendGroundBlueBomb(WorldTile tile) {
        Projectile projectile = World.sendProjectileCycles(this, tile, 4016, 140, 5, 5, 120, -15, 40);
        long projectileCycles = Utils.projectileTimeToMiliseconds(projectile.getStartTime() + projectile.getEndTime() - 1);
        Vorago thisVorago = this;
        CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
            @Override
            public boolean repeat() {
                try {
                    for (Entity t : getPossibleTargets()) {
                        if (t == null || t.hasFinished() || t.isDead() || !Utils.isOnRange(tile, t, 2, 1, 1))
                            continue;
                        int damage = CombatScript.getMaxHit(thisVorago, 500, NPCCombatDefinitionConstants.MAGE, t);
                        CombatScript.delayHit(thisVorago, 0, t, CombatScript.getMagicHit(thisVorago, damage));
                    }
                    return false;
                } catch (Exception e) {
                    Logger.getGlobal().catching(e);
                    return false;
                }
            }
        }, projectileCycles, 600, TimeUnit.MILLISECONDS);
    }

    @Override
    public double getMeleePrayerMultiplier() {
        return 0.25;
    }

    @Override
    public double getMagePrayerMultiplier() {
        return 0.25;
    }

    @Override
    public double getRangePrayerMultiplier() {
        return 0.25;
    }
    
    @Override
    public boolean isFreezeImmune() {
        return true;
    }

    @Override
    public boolean isStunImmune() {
        return true;
    }


    
}
