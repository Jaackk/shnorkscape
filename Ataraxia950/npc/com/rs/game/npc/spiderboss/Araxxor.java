package com.rs.game.npc.spiderboss;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceMovement;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.Projectile;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.item.ItemsContainer;
import com.rs.game.map.bossInstance.impl.SpiderBossInstance;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.player.Player;
import com.rs.game.player.content.Combat;
import com.rs.game.player.content.contracts.ContractHandler;
import com.rs.game.player.content.contracts.ReaperPerks;
import com.rs.game.player.content.dropcollection.DropCollectionConstants;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

@SuppressWarnings("serial")
public class Araxxor extends NPC {

    private final SpiderBossInstance instance;
    private int phase;
    private int autoAttacksLeft;
    private int lastSpecialAttackIndex = -1;
    private boolean duoFight;
    private int fightEnrage;
    private final long startDelay;
    private int extraHP;
    private long extraHPCycles;
    private final List<Integer> spidersLeft;
    private int choosenPath = -1;
    private final List<WorldObject> eggs;
    private final List<AraxxorMinion> minions;
    private long burnWebCycles;
    private final long burnWebDelay;
    private int acidInPool;
    private int absorpedAcid;
    private long darknessCycles;
    private WorldObject sunlight;
    private long sunlightDuration;
    private int wallHealth;
    private long wallChargeDelay;
    private boolean sentWarningMessage;
    private final Map<Entity, ItemsContainer<Item>> rewards;
    private final Map<Entity, boolean[]> challenges;
    private int combatStyle;
    private int baseEnrage;

    public Araxxor(int id, WorldTile tile, SpiderBossInstance instance) {
        super(id, tile, 0, true, true);
        this.instance = instance;
        setForceMultiArea(true);
        setCantFollowUnderCombat(false);
        setIntelligentRouteFinder(true);
        setNoDistanceCheck(true);
        setCapDamage(1200);
        setPhase(0);
        setRun(true);
        setAutoAttacksLeft(Utils.random(1, 6));
        duoFight = instance.getPlayersInsideCount() == 2;
        for (int i = 0; i < instance.getPlayersInsideCount(); i++) {
            Player player = instance.getPlayersInside().get(i);
            if (player == null || player.isDead())
                continue;
            baseEnrage += player.getSpiderBossEnrage();
        }
        fightEnrage += baseEnrage;
        startDelay = Utils.currentTimeMillis() + 6000;
        getCombat().removeTarget();
        setNextFaceEntity(null);
        setNextFaceWorldTile(instance.getTile(new WorldTile(4495, 6264, 1)));
        eggs = new ArrayList<WorldObject>();
        minions = new ArrayList<AraxxorMinion>();
        burnWebDelay = Utils.currentTimeMillis() + 2000;
        spidersLeft = new ArrayList<Integer>();
        rewards = new HashMap<Entity, ItemsContainer<Item>>();
        challenges = new HashMap<Entity, boolean[]>();
        setBonuses();
    }

    @Override
    public boolean isIntelligentRouteFinder() {
        return true;
    }

    public static int[][] araxxorIds = { { 19457, 19462, 19463 }, { 19465, 19466, 19467 } };

    @Override
    public void processNPC() {
        if (instance == null || !hasFinished() && (getPossibleTargets().isEmpty() || instance.getAraxxor() != this)) {
            finish();
            return;
        }
        if (phase >= 2 && Utils.currentTimeMillis() >= extraHPCycles) {
            sendExtraHeal(false);
            extraHPCycles = Utils.currentTimeMillis() + 4000;
        }
        if (getId() != 19464) {
            int araxxorType = getAbsorpedAcid() == 0 ? 0 : 1;
            int nextId = -1;
            for (int i = 0; i < araxxorIds[araxxorType].length; i++) {
                int id = araxxorIds[araxxorType][i];
                if (getId() == araxxorIds[araxxorType == 1 ? 0 : 1][i]) {
                    nextId = id;
                    break;
                }
            }
            if (nextId != -1)
                setNextNPCTransformation(nextId);
        }
        if (burnWebCycles != 0 && Utils.currentTimeMillis() > burnWebCycles) {
            for (WorldObject o : instance.getObjects()) {
                if (o == null)
                    continue;
                if (o.getId() == SpiderBossInstance.PATH_SPECIFIC_OBJECT_IDS[choosenPath][1] && World.containsObjectWithId(o, o.getId())) {
                    World.removeObject(o);
                    WorldTasksManager.schedule(new WorldTask() {
                        private int loop;

                        @Override
                        public void run() {
                            if (loop == 10) {
                                stop();
                            }
                            for (WorldTile tile : SpiderBossInstance.blockedPaths[choosenPath]) {
                                WorldTile instancedTile = instance.getTile(tile);
                                World.forceSetMask(instancedTile.getPlane(), instancedTile.getX(), instancedTile.getY(), 0);
                            }
                            loop++;
                        }

                    }, 0, 1);
                    instance.sendMessage("The web burns down, allowing access!");
                    instance.removeBurnCountDown();
                }
            }
            burnWebCycles = 0;
        }
        processWallCharge();
        processAcid();
        processSunlight();
        processDarkNess();
        if (getTemporaryAttributtes().get("CantMove") != null || getTemporaryAttributtes().get("HealingWeb") != null)
            return;
        if (!getCombat().process())
            checkAgressivity();
        super.processNPC();
    }

    public void processWallCharge() {
        if (wallChargeDelay == 0 || getTemporaryAttributtes().get("charging") != null || getTemporaryAttributtes().get("phase4cutscene") != null)
            return;
        if (wallChargeDelay > Utils.currentTimeMillis()) {
            long timeLeft = wallChargeDelay - Utils.currentTimeMillis();
            if (timeLeft <= 15000 && !sentWarningMessage) {
                instance.sendMessage("Araxxor prepares to come down from the ceiling and charge!");
                sentWarningMessage = true;
            }
            return;
        }
        getTemporaryAttributtes().put("charging", Boolean.TRUE);
        setNextFaceEntity(null);
        sentWarningMessage = false;
        setNextWorldTile(instance.getTile(new WorldTile(4529, 6247, 1)));
        WorldTile playerTile = instance.getTile(new WorldTile(4543, 6249, 1));
        setNextFaceWorldTile(playerTile);
        WorldTile totile = instance.getTile(new WorldTile(4536, 6247, 1));
        int index = 0;
        for (Player player : instance.getPlayersInside()) {
            if (player == null || player.hasFinished() || player.isDead())
                continue;
            player.getTemporaryAttributtes().put("undercutscene", Boolean.TRUE);
            player.resetWalkSteps();
            player.setNextWorldTile(playerTile.transform(0, -index, 0));
            player.setNextFaceWorldTile(player.transform(-2, 0, 0));
            instance.removeDarkness(player);
            index++;
        }
        int[] araxxorEmotes = { 24111, 24099, 24101, 24113 };
        int[] playerEmotes = { 24112, 24100, 24102, 24114 };
        int emoteIndex = Utils.random(araxxorEmotes.length);
        int insideCount = instance.getPlayersInsideCount();
        Araxxor thisSpider = this;
        WorldTasksManager.schedule(new WorldTask() {
            int loop = 0;
            int totalWallDamage = 0;

            @Override
            public void run() {
                if (hasFinished() || (getPossibleTargets().isEmpty())) {
                    stop();
                    return;
                }
                if (loop == 0) {
                    for (Player player : instance.getPlayersInside()) {
                        if (player == null || player.hasFinished() || player.isDead())
                            continue;
                        player.getPackets().sendCameraLook(instance.getTile(new WorldTile(4535, 0, 0)).getXInScene(player), instance.getTile(new WorldTile(0, 6249, 0)).getYInScene(player), 1);
                        player.getPackets().sendCameraPos(instance.getTile(new WorldTile(4545, 0, 0)).getXInScene(player), instance.getTile(new WorldTile(0, 6247, 0)).getYInScene(player), 3500);
                    }
                } else if (loop == 1) {
                    for (Player player : instance.getPlayersInside()) {
                        if (player == null || player.hasFinished() || player.isDead())
                            continue;
                        player.getPackets().sendCameraLook(instance.getTile(new WorldTile(4535, 0, 0)).getXInScene(player), instance.getTile(new WorldTile(0, 6249, 0)).getYInScene(player), 1, 400, 0);
                        player.getPackets().sendCameraPos(instance.getTile(new WorldTile(4551, 0, 0)).getXInScene(player), instance.getTile(new WorldTile(0, 6249, 0)).getYInScene(player), 3200, 400, 0);
                    }
                }
                if (loop == 2) {
                    setNextAnimation(new Animation(24098));
                    setNextForceMovement(new ForceMovement(totile, 2, ForceMovement.EAST));
                } else if (loop == 3) {
                    setNextWorldTile(totile);
                    setNextAnimation(new Animation(araxxorEmotes[emoteIndex]));
                    for (Player player : instance.getPlayersInside()) {
                        if (player == null || player.hasFinished() || player.isDead())
                            continue;
                        player.getTemporaryAttributtes().remove("DodgeEmote");
                        player.getPackets().sendExecuteScript(10098, 0);
                        instance.sendRedScreen(player, 150);
                    }
                } else if (loop == 4) {
                    for (Player player : instance.getPlayersInside()) {
                        if (player == null || player.hasFinished() || player.isDead())
                            continue;
                        instance.sendRedScreen(player, 120);
                    }
                } else if (loop == 5) {
                    for (Player player : instance.getPlayersInside()) {
                        if (player == null || player.hasFinished() || player.isDead())
                            continue;
                        player.getPackets().sendExecuteScript(10098, 1);
                        Integer choosenEmoteIndex = (Integer) player.getTemporaryAttributtes().get("DodgeEmote");
                        if (choosenEmoteIndex == null) {
                            player.applyHit(new Hit(thisSpider, 500, HitLook.UNBLOCKABLE_REGULAR_DAMAGE));
                            continue;
                        }
                        player.setNextAnimation(new Animation(playerEmotes[choosenEmoteIndex]));
                        totalWallDamage += choosenEmoteIndex != emoteIndex ? 250 : 500;
                        if (choosenEmoteIndex != emoteIndex)
                            player.applyHit(new Hit(thisSpider, 250, HitLook.UNBLOCKABLE_REGULAR_DAMAGE));
                    }
                } else if (loop == 8) {
                    showWallHealth = true;
                    instance.updateInterface(false);
                    for (Player player : instance.getPlayersInside()) {
                        if (player == null || player.hasFinished() || player.isDead())
                            continue;
                        instance.sendRedScreen(player, 150);
                        player.getPackets().sendCameraLook(instance.getTile(new WorldTile(4550, 0, 0)).getXInScene(player), instance.getTile(new WorldTile(0, 6250, 0)).getYInScene(player), 1);
                        player.getPackets().sendCameraPos(instance.getTile(new WorldTile(4535, 0, 0)).getXInScene(player), instance.getTile(new WorldTile(0, 6233, 0)).getYInScene(player), 7000);
                    }
                    setNextWorldTile(playerTile.transform(2, -2, 0));
                    setNextAnimation(new Animation(24103));
                } else if (loop == 10) {
                    wallHealth = wallHealth - totalWallDamage <= 0 ? 0 : wallHealth - totalWallDamage;
                    if (wallHealth == 0) {
                        World.removeObject(instance.getDarknessWall());
                        instance.getDarknessWall().setId(91515);
                        World.spawnObject(instance.getDarknessWall());
                    }
                    for (Player player : instance.getPlayersInside()) {
                        if (player == null || player.hasFinished() || player.isDead())
                            continue;
                        instance.removeDarkness(player);
                        player.getTemporaryAttributtes().remove("underDarkness");
                        player.getTemporaryAttributtes().remove("darknessHitCycles");
                    }
                    instance.updateInterface(false);
                    for (Player player : instance.getPlayersInside()) {
                        if (player == null || player.hasFinished() || player.isDead())
                            continue;
                        Integer choosenEmoteIndex = (Integer) player.getTemporaryAttributtes().get("DodgeEmote");
                        player.getTemporaryAttributtes().remove("undercutscene");
                        if (choosenEmoteIndex == null) {
                            instance.sendMessage(player, "Araxxor doesn't damage the wall after hitting you" + (insideCount == 2 && totalWallDamage == 0 ? " both" : "") + " so much.");
                            continue;
                        }
                        if (choosenEmoteIndex != emoteIndex)
                            instance.sendMessage(player, "Your dodge attempt caused Araxxor to smash the wall a bit.");
                        else {
                            if (insideCount == 1)
                                instance.sendMessage(player, "Dodging the attack fully enrages Araxxor, who damages the wall considerably!");
                            else if (insideCount == 2 && totalWallDamage == 1000)
                                instance.sendMessage(player, "You both dodge fully, causing Araxxor to smash hard into the wall!");
                        }

                    }
                } else if (loop == 12) {
                    if (instance.getDarknessWall().getId() == 91515) {
                        World.removeObject(instance.getDarknessWall());
                        instance.getDarknessWall().setId(91516);
                        World.spawnObject(instance.getDarknessWall());
                    }
                    setNextAnimation(new Animation(24056));
                    setNextGraphics(new Graphics(4985));
                } else if (loop == 17) {
                    for (Player player : instance.getPlayersInside()) {
                        if (player == null || player.hasFinished() || player.isDead())
                            continue;
                        player.getPackets().sendResetCamera();
                    }
                    WorldTile nextTile = wallHealth == 0 ? instance.getTile(new WorldTile(4563, 6262, 1)) : new WorldTile(0, 0, 1);
                    setNextWorldTile(nextTile);
                    wallChargeDelay = wallHealth == 0 ? 0 : Utils.currentTimeMillis() + 75000;
                    darknessCycles = wallHealth == 0 ? 0 : -1;
                    getTemporaryAttributtes().remove("charging");
                    if (wallHealth > 0) {
                        setCantInteract(false);
                        stop();
                        return;
                    }
                    phase++;
                    setHitpoints(getMaxHitpoints());
                    onPhaseChange();
                    setBonuses();
                    getInstance().updateInterface(false);
                    setNextAnimation(new Animation(24076));
                    setNextGraphics(new Graphics(4984));
                    WorldTile baseCoord = new WorldTile(4550, 6245, 1);
                    for (int i = 0; i < 11; i++) {
                        WorldTile tile = baseCoord.transform(0, i, 0);
                        WorldTile instancedTile = instance.getTile(tile);
                        World.forceSetMask(instancedTile.getPlane(), instancedTile.getX(), instancedTile.getY(), 0);
                    }
                } else if (loop == 19) {
                    setCantInteract(false);
                    checkAgressivity();
                    setNextFaceEntity(getCombat().getTarget());
                    stop();
                }
                loop++;
            }
        }, 0, 0);
    }

    public void processDarkNess() {
        if (getTemporaryAttributtes().get("charging") != null || getTemporaryAttributtes().get("phase4cutscene") != null)
            return;
        if (darknessCycles != 0 && (darknessCycles == -1 || darknessCycles > Utils.currentTimeMillis())) {
            for (Player player : instance.getPlayersInside()) {
                if (player == null || player.isDead() || player.hasFinished())
                    continue;
                Integer underDarkness = (Integer) player.getTemporaryAttributtes().get("underDarkness");
                if (underDarkness == null && (sunlight == null || !player.withinArea(sunlight.getX(), sunlight.getY(), sunlight.getX() + 2, sunlight.getY() + 2))) {
                    instance.sendDarkness(player);
                    player.getTemporaryAttributtes().put("underDarkness", 1);
                    underDarkness = (Integer) player.getTemporaryAttributtes().get("underDarkness");
                }
                if (underDarkness != null) {
                    Long darknessHitCycles = (Long) player.getTemporaryAttributtes().get("darknessHitCycles");
                    if (darknessHitCycles == null) {
                        player.getTemporaryAttributtes().put("darknessHitCycles", Utils.currentTimeMillis());
                        darknessHitCycles = (Long) player.getTemporaryAttributtes().get("darknessHitCycles");
                    }
                    if (Utils.currentTimeMillis() >= darknessHitCycles) {
                        int playersEnrage = 0;
                        if (instance != null)
                            for (int i = 0; i < instance.getPlayersInsideCount(); i++) {
                                Player p = instance.getPlayersInside().get(i);
                                if (p == null || p.isDead())
                                    continue;
                                playersEnrage += p.getSpiderBossEnrage();
                            }
                        if (playersEnrage > 300)
                            playersEnrage = 300;
                        int maxIncrease = darknessCycles == -1 ? 9 : 7;
                        if (underDarkness > maxIncrease)
                            underDarkness = maxIncrease;
                        int damage = (5 + ((playersEnrage / 20))) * underDarkness;
                        if (damage > 100)
                            damage = 100;
                        if (sunlight != null && player.withinArea(sunlight.getX(), sunlight.getY(), sunlight.getX() + 2, sunlight.getY() + 2)) {
                            player.getTemporaryAttributtes().remove("underDarkness");
                            player.getTemporaryAttributtes().remove("darknessHitCycles");
                            continue;
                        }
                        CombatScript.delayHit(this, 0, player, new Hit(this, damage, HitLook.UNBLOCKABLE_REGULAR_DAMAGE));
                        player.getTemporaryAttributtes().put("darknessHitCycles", Utils.currentTimeMillis() + 900);
                        player.getTemporaryAttributtes().put("underDarkness", underDarkness + 1);
                    }
                }
            }
        }
        if (darknessCycles != 0 && darknessCycles != -1 && Utils.currentTimeMillis() >= darknessCycles)
            darknessCycles = 0;
    }

    public void processSunlight() {
        if (darknessCycles == 0) {
            removeSunlight();
            for (Player player : instance.getPlayersInside()) {
                if (player == null || player.isDead() || player.hasFinished() || player.getTemporaryAttributtes().get("underDarkness") == null || player.getTemporaryAttributtes().get("darknessHitCycles") == null)
                    continue;
                instance.removeDarkness(player);
                player.getTemporaryAttributtes().remove("underDarkness");
                player.getTemporaryAttributtes().remove("darknessHitCycles");
            }
            return;
        }
        if (sunlightDuration != 0 && Utils.currentTimeMillis() >= sunlightDuration) {
            if (phase == 1 && choosenPath == 2)
                sendRandomSunlight();
        } else {
            if (sunlight != null && World.containsObjectWithId(sunlight, sunlight.getId())) {
                for (Player player : instance.getPlayersInside()) {
                    if (player == null || player.isDead() || player.hasFinished() || !player.withinArea(sunlight.getX(), sunlight.getY(), sunlight.getX() + 2, sunlight.getY() + 2) || player.getTemporaryAttributtes().get("underDarkness") == null || player.getTemporaryAttributtes().get("darknessHitCycles") == null)
                        continue;
                    instance.removeDarkness(player);
                    player.getTemporaryAttributtes().remove("underDarkness");
                    player.getTemporaryAttributtes().remove("darknessHitCycles");
                }
            }
        }
    }

    public void sendRandomSunlight() {
        WorldTile spawnTile = getRandomSunLightLocation(sunlight);
        removeSunlight();
        WorldObject sun = new WorldObject(91664, 10, 0, spawnTile.transform(1, 0, 0));
        sunlight = new WorldObject(91665, 10, 0, spawnTile);
        World.spawnObject(sun);
        World.spawnObject(sunlight);
        sunlightDuration = Utils.currentTimeMillis() + (isDuoFight() ? 10000 : 20000);
    }

    public void sendTemporaryDarkness() {
        removeSunlight();
        WorldTile spawnTile = instance.getRandomWorldTile(this, 3, 7, false);
        WorldObject sun = new WorldObject(91664, 10, 0, spawnTile.transform(1, 0, 0));
        sunlight = new WorldObject(91665, 10, 0, spawnTile);
        World.spawnObject(sun);
        World.spawnObject(sunlight);
        sunlightDuration = Utils.currentTimeMillis() + 10000;
        darknessCycles = Utils.currentTimeMillis() + 10000;
    }

    public void removeSunlight() {
        if (sunlight != null && World.containsObjectWithId(sunlight, sunlight.getId())) {
            WorldObject sun = World.getObjectWithId(sunlight.transform(1, 0, 0), sunlight.getId() - 1);
            if (sun != null && World.containsObjectWithId(sun, sun.getId()))
                World.removeObject(sun);
            World.removeObject(sunlight);
            sunlightDuration = 0;
            sunlight = null;
        }
    }

    public WorldTile getRandomSunLightLocation(WorldObject object) {
        int[] area = SpiderBossInstance.darkness_path_sunlite_area;
        List<WorldTile> possibleTiles = new ArrayList<WorldTile>();
        WorldTile minTile = instance.getTile(area[0], area[1], 1);
        WorldTile maxTile = instance.getTile(area[2], area[3], 1);
        WorldTile checkTile = object;
        for (int x = minTile.getX(); x <= maxTile.getX(); x++) {
            for (int y = minTile.getY(); y <= maxTile.getY(); y++) {
                WorldTile tile = new WorldTile(x, y, 1);
                if (!World.canMoveNPC(1, x, y, 1))
                    continue;
                if (checkTile != null && (Utils.getDistance(checkTile, tile) > 20 || Utils.getDistance(checkTile, tile) < 7))
                    continue;
                possibleTiles.add(tile);
            }
        }
        return possibleTiles.get(Utils.random(possibleTiles.size()));
    }

    @Override
    public boolean canMove(int dir) {
        if (getTemporaryAttributtes().get("CantMove") != null || getTemporaryAttributtes().get("HealingWeb") != null || getTemporaryAttributtes().get("cocoonAttack") != null || getTemporaryAttributtes().get("phase4cutscene") != null)
            return false;
        return super.canMove(dir);
    }

    @Override
    public boolean canWalkNPC(int toX, int toY) {
        if (getTemporaryAttributtes().get("CantMove") != null || getTemporaryAttributtes().get("HealingWeb") != null || getTemporaryAttributtes().get("cocoonAttack") != null || getTemporaryAttributtes().get("phase4cutscene") != null)
            return false;
        return super.canWalkNPC(toX, toY);
    }

    public List<Entity> getCleaveTargets() {
        List<Entity> targets = new ArrayList<Entity>();
        targets.addAll(getPossibleTargets());
        targets.addAll(getAttackAbleMinions());
        for (Entity e : getPossibleTargets()) {
            if (e == null || e.isDead() || e.hasFinished() || !(e instanceof Player))
                continue;
            Player player = (Player) e;
            if (player.getFamiliar() != null && !player.getFamiliar().isDead() && !player.getFamiliar().hasFinished())
                targets.add(player.getFamiliar());
        }
        return targets;
    }

    private List<AraxxorMinion> getAttackAbleMinions() {
        List<AraxxorMinion> attackAbleMinions = new ArrayList<AraxxorMinion>();
        for (AraxxorMinion minion : minions) {
            if (minion == null || minion.isDead() || minion.hasFinished() || minion.getId() == 19470 || minion.getId() == 19471)
                continue;
            attackAbleMinions.add(minion);
        }
        return attackAbleMinions;
    }

    public List<Object> getEggBombTargets(Entity target) {
        List<Object> targets = new ArrayList<Object>();
        for (WorldObject egg : eggs) {
            if (egg == null || !World.containsObjectWithId(egg, egg.getId()) || !Utils.isOnRange(egg, target, 0, 1, 1) || targets.size() >= 5)
                continue;
            targets.add(egg);
        }
        for (AraxxorMinion minion : getAttackAbleMinions()) {
            if (minion == null || minion.isDead() || minion.hasFinished() || !Utils.isOnRange(minion, target, 0, 1, 1) || targets.size() >= 5)
                continue;
            targets.add(minion);
        }
        return targets;
    }

    @Override
    public ArrayList<Entity> getPossibleTargets() {
        return getPossibleTargets(false, true);
    }

    @Override
    public ArrayList<Entity> getPossibleTargets(boolean checkNPCs, boolean checkPlayers) {
        ArrayList<Entity> possibleTarget = new ArrayList<Entity>();
        if (instance == null || instance.getPlayersInside() == null)
            return possibleTarget;
        for (Player player : instance.getPlayersInside()) {
            if (player == null || player.isDead() || player.hasFinished())
                continue;
            possibleTarget.add(player);
        }
        return possibleTarget;
    }

    @Override
    public boolean checkAgressivity() {
        if (startDelay >= Utils.currentTimeMillis())
            return false;
        if (getHitpoints() == 0)
            return false;
        if (getTemporaryAttributtes().get("charging") != null || getTemporaryAttributtes().get("phase4cutscene") != null || isCantInteract())
            return false;
        ArrayList<Entity> possibleTarget = getPossibleTargets();
        if (!possibleTarget.isEmpty()) {
            Entity target = possibleTarget.get(Utils.random(possibleTarget.size()));
            setTarget(target);
            target.setAttackedBy(target);
            target.setFindTargetDelay(Utils.currentTimeMillis() + 10000);
            return true;
        }
        return !possibleTarget.isEmpty();
    }

    public boolean switchTarget() {
        if (startDelay >= Utils.currentTimeMillis())
            return false;
        if (getHitpoints() == 0)
            return false;
        if (getTemporaryAttributtes().get("charging") != null || getTemporaryAttributtes().get("phase4cutscene") != null || isCantInteract())
            return false;
        ArrayList<Entity> possibleTarget = getPossibleTargets();
        if (possibleTarget.size() == 1)
            return false;
        if (!possibleTarget.isEmpty()) {
            Entity target = getCombat().getTarget();
            if (target == null)
                target = possibleTarget.get(Utils.random(possibleTarget.size()));
            else {
                possibleTarget.removeIf(getCombat().getTarget()::equals);
                if (possibleTarget.isEmpty())
                    return false;
                target = possibleTarget.get(Utils.random(possibleTarget.size()));
            }
            setTarget(target);
            target.setAttackedBy(target);
            target.setFindTargetDelay(Utils.currentTimeMillis() + 10000);
            return true;
        }
        return false;
    }

    public void processHit(Hit hit) {
        if (hit.getSource() != null && hit.getSource() != this && hit.getSource() instanceof Player) {
            for (AraxxorMinion minion : minions) {
                if (minion != null && !minion.isDead() && !minion.hasFinished() && minion.getId() == 19468) {
                    CombatScript.delayHit(this, 0, hit.getSource(), new Hit(this, hit.getDamage(), HitLook.REFLECTED_DAMAGE));
                    hit.setDamage(0);
                    super.processHit(hit);
                    unleashSludge();
                    instance.updateInterface(false);
                    return;
                }
            }
        }
        if (hit.getSource() != null && hit.getSource() != this && hit.getLook() != HitLook.HEALED_DAMAGE && hit.getDamage() > 0 && getTemporaryAttributtes().get("HealingWeb") != null) {
            if (hit.getSource() instanceof NPC && !(hit.getSource() instanceof Familiar)) {
                if (hit.getDamage() > getHitpoints()) {
                    sendExtraHeal(true);
                }
                super.processHit(hit);
                if (getTemporaryAttributtes().get("phase4cutscene") == null && getHitpoints() <= 5000 && phase == 3 && this.getTemporaryAttributtes().get("sentEnrageMessage") == null) {
                    getTemporaryAttributtes().put("sentEnrageMessage", Boolean.TRUE);
                    instance.sendMessage("Araxxi starts to get enraged.");
                }
                unleashSludge();
                instance.updateInterface(false);
                return;
            }
            Entity source = hit.getSource() instanceof Familiar ? ((Familiar) hit.getSource()).getOwner() : hit.getSource();
            int damage = (int) (hit.getDamage() * 0.5);
            double reflectedDamageMultiplier = ((double) getFightEnrage() / 900);
            hit.setDamage(damage);
            damage += (int) ((double) damage * reflectedDamageMultiplier);
            if (source != null) {
                CombatScript.delayHit(this, 0, source, new Hit(this, Utils.random(damage - 10, damage + 10), HitLook.REFLECTED_DAMAGE));// are u sure this doesnt crash your client? mayb
            }
            getTemporaryAttributtes().put("SkipHeal", Boolean.TRUE);
        }
        if (hit.getDamage() > getHitpoints()) {
            sendExtraHeal(true);
        }
        super.processHit(hit);
        if (getTemporaryAttributtes().get("phase4cutscene") == null && getHitpoints() <= 5000 && phase == 3 && getTemporaryAttributtes().get("sentEnrageMessage") == null) {
            getTemporaryAttributtes().put("sentEnrageMessage", Boolean.TRUE);
            instance.sendMessage("Araxxi starts to get enraged.");
        }
        unleashSludge();
        instance.updateInterface(false);
    }

    public void unleashSludge() {
        if (getTemporaryAttributtes().get("phase4cutscene") != null || getTemporaryAttributtes().get("unleashedSludge") != null || phase != 3 || getHitpoints() > (isDuoFight() ? 3500 : 2500))
            return;
        getTemporaryAttributtes().put("unleashedSludge", Boolean.TRUE);
        increaseCurrentEnrage(20);
        instance.sendMessage("Araxxi unleashes a putrid acid wave!");
        WorldTile middelTile = getMiddleWorldTile();
        for (Player target : instance.getPlayersInside()) {
            if (target == null || target.isDead() || target.hasFinished())
                continue;
            target.getCombatDefinitions().decreaseSpecialAttack(isDuoFight() ? 100 : 50, false);
            boolean reverse = Utils.random(2) == 0;
            WorldTasksManager.schedule(new WorldTask() {
                WorldTile[] to;
                WorldTile[] nextTo = getSludgeWorldTile(middelTile, target, 6, reverse);
                final int startTime = 4;
                int currentBounceIndex = 0;
                int loop;

                @Override
                public void run() {
                    if (hasFinished() || (getPossibleTargets().isEmpty()) || getTemporaryAttributtes().get("dead") != null || target == null || target.isDead() || target.hasFinished() || !instance.getPlayersInside().contains(target)) {
                        stop();
                        return;
                    }
                    int damage = (int) (100 + (Math.min((double) getFightEnrage() / 300.00, 1) * 300.00));
                    if (loop == 0) {
                        WorldTile[] tempNextTo = null;
                        if (to == null)
                            tempNextTo = getSludgeWorldTile(middelTile, target, 6, reverse);
                        World.sendProjectileCycles(to == null ? middelTile : to[to.length - 1], tempNextTo != null ? tempNextTo[0] : nextTo[0], 5012, 0, 0, 0, 120, 20 + Utils.random(5), 0);
                        to = nextTo;
                        currentBounceIndex = 0;
                    }
                    if (startTime != -1 && loop == startTime) {
                        nextTo = getSludgeWorldTile(middelTile, target, 6, reverse);
                        World.sendProjectileCycles(to[currentBounceIndex], to[currentBounceIndex + 1], 5012, 0, 0, 0, 60, 30 + Utils.random(5), 0);
                        to = nextTo;
                        currentBounceIndex++;
                    } else if (loop > startTime && (loop - startTime) % 2 == 0) {
                        if (currentBounceIndex == 7) {
                            if (target.matches(to[currentBounceIndex])) {
                                target.applyHit(new Hit(Araxxor.this, damage, HitLook.UNBLOCKABLE_REGULAR_DAMAGE));
                                target.getTemporaryAttributtes().put("sludgeunderplayer", Boolean.TRUE);
                                World.sendProjectileCycles(new WorldTile(target), new WorldTile(target), 5012, 0, 0, 0, 80, 30 + Utils.random(5), 0);
                                loop++;
                                return;
                            } else {
                                if (target.getTemporaryAttributtes().remove("sludgeunderplayer") != null) {
                                    loop = 0;
                                    return;
                                }
                                nextTo = getSludgeWorldTile(middelTile, target, 6, reverse);
                                World.sendProjectileCycles(to[currentBounceIndex], nextTo[currentBounceIndex], 5012, 0, 0, 0, 60, 30 + Utils.random(5), 0);
                                to = nextTo;
                                loop++;
                            }
                            return;
                        }
                        if (target.matches(to[currentBounceIndex])) {
                            target.applyHit(new Hit(Araxxor.this, damage, HitLook.UNBLOCKABLE_REGULAR_DAMAGE));
                            target.getTemporaryAttributtes().put("sludgeunderplayer", Boolean.TRUE);
                            World.sendProjectileCycles(new WorldTile(target), new WorldTile(target), 5012, 0, 0, 0, 80, 30 + Utils.random(5), 0);
                            loop++;
                            return;
                        } else {
                            if (target.getTemporaryAttributtes().remove("sludgeunderplayer") != null) {
                                loop = 0;
                                return;
                            }
                        }
                        nextTo = getSludgeWorldTile(middelTile, target, 6, reverse);
                        World.sendProjectileCycles(to[currentBounceIndex], nextTo[currentBounceIndex + 1], 5012, 0, 0, 0, 60, 30 + Utils.random(5), 0);
                        to = nextTo;
                        currentBounceIndex++;
                    }
                    loop++;
                }
            }, 0, 0);
        }
    }

    public AraxxorMinion spawnMinion(int id, WorldTile tile, int spawnAnimation) {
        AraxxorMinion minion = new AraxxorMinion(id, tile, this);
        if (spawnAnimation == -1) {
            spawnAnimation = Utils.random(2) == 0 ? 24054 : 24078;
        }
        minion.setNextAnimation(new Animation(spawnAnimation));
        if (spawnAnimation != 24135 && id != 19470 && id != 19471)
            minion.setNextGraphics(new Graphics(spawnAnimation == 24078 ? 5005 : 4982));
        minions.add(minion);
        return minion;
    }

    private void removeMinions() {
        for (AraxxorMinion minion : minions) {
            if (minion != null && !minion.hasFinished())
                minion.finish();
        }
        minions.clear();
    }

    private void killSpiders() {
        for (AraxxorMinion minion : minions) {
            if (minion != null && !minion.hasFinished()) {
                if (minion.getAcidAmount() > 0)
                    acidInPool += minion.getAcidAmount();
                if (minion.isFromMinionAssist())
                    spidersLeft.add(minion.getId());
                minion.finish();
            }
        }
        minions.clear();
    }

    public void spawnEgg(WorldTile tile) {
        WorldObject egg = new WorldObject(91641, 22, 0, tile);
        eggs.add(egg);
        World.sendGraphics(null, new Graphics(5009), tile);
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                World.spawnObject(egg);
            }
        }, 1);
    }

    public void bombEgg(WorldTile tile) {
        int eggIndex = -1;
        for (int i = 0; i < eggs.size(); i++) {
            WorldObject egg = eggs.get(i);
            if (egg == null || !egg.matches(tile) || !World.containsObjectWithId(egg, egg.getId()))
                continue;
            eggIndex = i;
            break;
        }
        if (eggIndex != -1) {
            WorldObject egg = eggs.get(eggIndex);
            World.removeObject(egg);
            World.sendGraphics(null, new Graphics(5010), tile);
            eggs.remove(eggIndex);
        }
    }

    // time to spawn 20 ticks
    public void spawnSpiderEgg(WorldTile tile) {
        int eggIndex = -1;
        for (int i = 0; i < eggs.size(); i++) {
            WorldObject egg = eggs.get(i);
            if (egg == null || !egg.matches(tile) || !World.containsObjectWithId(egg, egg.getId()))
                continue;
            eggIndex = i;
            break;
        }
        if (eggIndex != -1) {
            WorldObject egg = eggs.get(eggIndex);
            World.removeObject(egg);
            World.sendGraphics(null, new Graphics(5008), tile);
            spawnMinion(19458 + Utils.random(3), tile, 24054);
            eggs.remove(eggIndex);
        }
    }

    private void clearEggs() {
        for (WorldObject egg : eggs)
            if (egg != null && World.containsObjectWithId(egg, egg.getId()))
                World.removeObject(egg);
        eggs.clear();
    }

    public void unlockChallenge(Player player, int index) {
        if (challenges.get(player) == null)
            challenges.put(player, new boolean[6]);
        boolean[] challenge = challenges.get(player);
        challenge[index] = true;
    }

    private void clearArea() {
        removeMinions();
        clearEggs();
        removeSunlight();
    }

    private boolean refuseToDie() {
        if (getPhase() <= 1) {
            setHitpoints(500);
            instance.updateInterface(false);
            instance.sendMessage("The spiders above in the cavern refuse to allow Araxxor to die!");
            World.sendStillProjectile(getMiddleWorldTile(), getMiddleWorldTile(), null, null, 5003, 500, 60, 0, 50);
            return true;
        }
        if (phase == 2) {
            if (getTemporaryAttributtes().get("phase4cutscene") != null)
                return true;
            getTemporaryAttributtes().put("phase4cutscene", Boolean.TRUE);
            setCantInteract(true);
            resetWalkSteps();
            setNextAnimation(new Animation(-1));
            darknessCycles = 0;
            setNextFaceEntity(null);
            phase++;
            onPhaseChange();
            setNextNPCTransformation(19462);
            setNextWorldTile(instance.getTile(new WorldTile(4567, 6263, 1)));
            WorldTile playerTile = instance.getTile(new WorldTile(4569, 6262, 1));
            setNextFaceWorldTile(playerTile);
            instance.removeHealthBar();
            removeSunlight();
            for (Player player : instance.getPlayersInside()) {
                if (player == null || player.isDead() || player.hasFinished())
                    continue;
                player.stopAll(true, false, true);
                player.resetWalkSteps();
                player.setNextAnimation(new Animation(-1));
                instance.removeDarkness(player);
                player.getTemporaryAttributtes().remove("underDarkness");
                player.getTemporaryAttributtes().remove("darknessHitCycles");
                player.setNextWorldTile(playerTile);
                player.setNextFaceEntity(this);
                player.lock();
                player.getTemporaryAttributtes().put("undercutscene", Boolean.TRUE);
                player.getPackets().sendCameraLook(instance.getTile(new WorldTile(4567, 0, 0)).getXInScene(player), instance.getTile(new WorldTile(0, 6265, 0)).getYInScene(player), 1);
                player.getPackets().sendCameraPos(instance.getTile(new WorldTile(4556, 0, 0)).getXInScene(player), instance.getTile(new WorldTile(0, 6265, 0)).getYInScene(player), 3500);
            }
            instance.updateInterface(false);
            WorldTasksManager.schedule(new WorldTask() {
                int loop;

                @Override
                public void run() {
                    if (hasFinished() || getPossibleTargets().isEmpty()) {
                        stop();
                        return;
                    }
                    if (loop == 1) {
                        for (Player player : instance.getPlayersInside()) {
                            if (player == null || player.isDead() || player.hasFinished())
                                continue;
                            player.setForceNextMapLoadRefresh(true);
                            player.loadMapRegions();
                        }
                        setNextFaceWorldTile(playerTile);
                    } else if (loop == 2) {
                        for (Player player : instance.getPlayersInside()) {
                            if (player == null || player.isDead() || player.hasFinished())
                                continue;
                            player.getPackets().sendCameraLook(instance.getTile(new WorldTile(4569, 0, 0)).getXInScene(player), instance.getTile(new WorldTile(0, 6265, 0)).getYInScene(player), 1, 10, 0);
                            player.getPackets().sendCameraPos(instance.getTile(new WorldTile(4557, 0, 0)).getXInScene(player), instance.getTile(new WorldTile(0, 6265, 0)).getYInScene(player), 3000, 10, 0);
                        }
                        setNextAnimation(new Animation(24042));
                        resetWalkSteps();
                        setNextForceMovement(new ForceMovement(instance.getTile(new WorldTile(4589, 6263, 1)), 20, ForceMovement.EAST));
                    } else if (loop == 6) {
                        for (Player player : instance.getPlayersInside()) {
                            if (player == null || player.isDead() || player.hasFinished())
                                continue;
                            player.setNextAnimation(new Animation(11785));
                            player.resetWalkSteps();
                            player.setNextForceMovement(new ForceMovement(instance.getTile(new WorldTile(4573, 6263, 1)), 4, ForceMovement.EAST));
                        }
                    } else if (loop == 7) {
                        for (Player player : instance.getPlayersInside()) {
                            if (player == null || player.isDead() || player.hasFinished())
                                continue;
                            player.getPackets().sendCameraLook(instance.getTile(new WorldTile(4578, 0, 0)).getXInScene(player), instance.getTile(new WorldTile(0, 6265, 0)).getYInScene(player), 1);
                            player.getPackets().sendCameraPos(instance.getTile(new WorldTile(4572, 0, 0)).getXInScene(player), instance.getTile(new WorldTile(0, 6265, 0)).getYInScene(player), 4000);
                        }
                    } else if (loop == 12) {
                        for (int i = 0; i < instance.getPlayersInside().size(); i++) {
                            Player player = instance.getPlayersInside().get(i);
                            if (player == null || player.isDead() || player.hasFinished())
                                continue;
                            WorldTile firstPlayer = instance.getTile(new WorldTile(4591, 6254, 1));
                            player.setNextWorldTile(firstPlayer.transform(-i, 0, 0));
                            player.setForceNextMapLoadRefresh(true);
                            player.loadMapRegions();
                        }
                    } else if (loop == 13) {
                        for (Player player : instance.getPlayersInside()) {
                            if (player == null || player.isDead() || player.hasFinished())
                                continue;
                            player.setNextFaceEntity(null);
                            player.unlock();
                            player.getTemporaryAttributtes().remove("undercutscene");
                            player.getPackets().sendResetCamera();
                        }
                    } else if (loop == 22) {
                        for (int i = 0; i < instance.getPlayersInside().size(); i++) {
                            Player player = instance.getPlayersInside().get(i);
                            if (player == null || player.isDead() || player.hasFinished())
                                continue;
                            player.getLocalNPCUpdate().reset();
                        }
                        setNextAnimation(new Animation(-1));
                        setNextWorldTile(instance.getTile(new WorldTile(4589, 6263, 1)));
                        setNextFaceWorldTile(instance.getTile(new WorldTile(4590, 6254, 1)));
                    } else if (loop == 23) {
                        setNextFaceWorldTile(instance.getTile(new WorldTile(4590, 6254, 1)));
                        setNextAnimation(new Animation(24062));
                        setNextGraphics(new Graphics(5000));
                    } else if (loop == 39) {
                        setNextNPCTransformation(19464);
                        setHitpoints(getMaxHitpoints());
                        setCantInteract(false);
                        setNextFaceEntity(getCombat().getTarget());
                        instance.updateInterface(true);
                        getTemporaryAttributtes().remove("phase4cutscene");
                    } else if (loop == 43) {
                        sendRemainingAcid();
                        stop();
                    }
                    loop++;
                }
            }, 0, 0);
            return true;
        }
        return false;
    }

    @Override
    public boolean isDead() {
        if (getTemporaryAttributtes().get("phase4cutscene") != null)
            return false;
        return super.isDead();
    }

    @Override
    public void sendDeath(Entity source) {
        if (refuseToDie())
            return;
        if (getTemporaryAttributtes().get("phase4cutscene") != null)
            return;
        getTemporaryAttributtes().put("phase4cutscene", Boolean.TRUE);
        getTemporaryAttributtes().put("dead", Boolean.TRUE);
        clearArea();
        resetWalkSteps();
        getCombat().removeTarget();
        setNextAnimation(null);
        if (!isDead())
            setHitpoints(0);
        final int deathDelay = 3;
        spidersLeft.clear();
        for (Player player : instance.getPlayersInside()) {
            if (player == null || player.isDead() || player.hasFinished())
                continue;
            instance.removeDarkness(player);
            player.stopAll();
            player.getTemporaryAttributtes().clear();
            player.setSpiderBossEnrage(player.getSpiderBossEnrage() >= 300 ? 300 : player.getSpiderBossEnrage() + 20);
            // player.getMonstersKillCountManager().increaseMonsterKills(Monsters.ARAXXI);
        }
        instance.updateInterface(false);
        setNextFaceWorldTile(instance.getTile(new WorldTile(4590, 6254, 1)));
        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                if (loop == 0) {
                    setNextAnimation(new Animation(24106));
                } else if (loop >= deathDelay) {
                    Player killer = getMostDamageReceivedSourcePlayer();
                    if (killer == null) {
                        killer = source instanceof Player ? (Player) source : null;
                        if (killer == null) {
                            if (source instanceof Familiar) {
                                killer = ((Familiar) source).getOwner();
                            }
                        }
                    }
                    if (killer == null) {
                        killer = instance == null || instance.getPlayersInside() == null || instance.getPlayersInside().isEmpty() ? null : instance.getPlayersInside().get(0);
                    }
                    if (source instanceof Player) {

                        ((Player) source).getControlerManager().processNPCDeath(Araxxor.this);
                    }
                    getTemporaryAttributtes().clear();
                    reset();
                    getTemporaryAttributtes().put("dead", Boolean.TRUE);
                    finish();
                    if (killer != null) {
                        if (killer.isGroupIronman()) {
                            killer.gimTracker.incrementBpGained(6);
                        }
                        WorldObject AraxxorBody = new WorldObject(91673, 10, 0, getX(), getY(), getPlane());
                        instance.spawnArraxorBody(AraxxorBody);
                        if (instance.getPlayersInside() != null)
                            for (Player player : instance.getPlayersInside()) {
                                if (player == null || player.isDead() || player.hasFinished())
                                    continue;
                                prepareRewards(player);
                                ContractHandler.updateContract(player, Araxxor.this);
                            }
                        instance.removeHealthBar();
                    }
                    stop();
                }
                loop++;
            }
        }, 0, 1);
    }

    public static final int COMMOM = 1, UNCOMMON = 2;

    private static final int[][][] REWARDS = { { { 33870, 1, -1 }// Araxytepheromone
            , { 31737, 70, 90, COMMOM } // Araxyte arrows
            }, { { 26750, 2, 5, UNCOMMON }, // overload flasks (6)
                    { 23400, 2, 5, COMMOM }, // full restore flasks(6)
                    { 23352, 9, 15, COMMOM }, // saradomin brew flasks
                    { 15273, 8, 11, COMMOM }, // rocktails
            }, { { 1514, 125, 300, COMMOM }, // magiclogs
                    { 1516, 300, 600, COMMOM }, // yew logs
                    { 454, 300, 600, COMMOM }, // coal
                    { 29863, 2, 3, UNCOMMON }, // sirenic scale
                    { 6572, 2, 2, UNCOMMON }, // uncut onyx
                    { 450, 100, 100, COMMOM }, // addy ore
                    { 1748, 70, 90, COMMOM }, // black d hide
                    { 452, 50, 50, UNCOMMON }, // rune ore
                    { 9245, 100, 251, UNCOMMON }, // onyx bolt(e)
                    { 1392, 50, 70, UNCOMMON }, // battlestaff
                    { 5316, 5, 7, UNCOMMON }, // magic seed
                    { 5303, 10, 10, UNCOMMON }, // dwarf weed seed
                    { 212, 45, 45, COMMOM }, // grimy avatoe
                    { 218, 45, 45, COMMOM }, // grimy dwarfweed
                    { 2486, 40, 55, COMMOM } // Grimy lantadyme
            } };
    public static final int[] CHARMS = { 12158, 12159, 12160, 12163 };
    public static final int[] RARE_REWARDS = { 31722, 31724, 31723 };

    public static final int[] LEG_REWARDS = { 31718, 31719, 31720 };

    public void prepareRewards(Player player) {
        ItemsContainer<Item> playerRewards = new ItemsContainer<>(20, true);
        double random = Math.random();
        if (player.reaperPerkActivated(ReaperPerks.REAPERS_BLESSING) && ContractHandler.isContractNpc(player, this))
            random -= random * 0.05;
        double pheromoneChance = (double) 1 / ((double) 50 - ((double) player.getSpiderBossEnrage() / 20.00));
        Item alwaysReward = random <= pheromoneChance ? new Item(33870, 1) : new Item(31737, Utils.random(70, 91));
        playerRewards.add(alwaysReward);
        random = Math.random();
        int charmsIndex = random <= 0.07 ? -1 : random <= 0.08 ? 0 : random <= 0.09 ? 1 : random <= 42 ? 2 : 3;
        if (charmsIndex != -1)
            playerRewards.add(new Item(CHARMS[charmsIndex], 15));
        for (int i = 1; i < REWARDS.length; i++) {
            List<int[]> possibleDrops = new ArrayList<int[]>();
            for (int j = 0; j < REWARDS[i].length; j++) {
                random = Math.random();
                if (random <= DROP_RATES[REWARDS[i][j][3]]) {
                    possibleDrops.add(REWARDS[i][j]);
                }
            }
            if (!possibleDrops.isEmpty()) {
                int[] randomDrop = possibleDrops.get(Utils.random(possibleDrops.size()));
                playerRewards.add(new Item(randomDrop[0], Utils.random(randomDrop[1], randomDrop[2] + 1)));
            }
        }
        if (Utils.random(3) == 0) {
            for (int i = 2; i < REWARDS.length; i++) {
                List<int[]> possibleDrops = new ArrayList<int[]>();
                for (int j = 0; j < REWARDS[i].length; j++) {
                    random = Math.random();
                    if (random <= DROP_RATES[REWARDS[i][j][3]]) {
                        possibleDrops.add(REWARDS[i][j]);
                    }
                }
                if (!possibleDrops.isEmpty()) {
                    int[] randomDrop = possibleDrops.get(Utils.random(possibleDrops.size()));
                    playerRewards.add(new Item(randomDrop[0], Utils.random(randomDrop[1], randomDrop[2] + 1)));
                }
            }
        }
        random = Math.random();
        if (player.reaperPerkActivated(ReaperPerks.REAPERS_BLESSING) && ContractHandler.isContractNpc(player, this))
            random -= random * 0.05;
        double legChance = ((double) 1 / (double) (isDuoFight() ? 70 : 40));
        if (random <= legChance) {
            Item leg = new Item(LEG_REWARDS[getChoosenPath()], 1);
            playerRewards.add(leg);
            player.getDropCollectionHandler().handleBossKills(new Item(LEG_REWARDS[getChoosenPath()], 1), DropCollectionConstants.ARAXXOR_ID);
            World.sendNews(player, player.getDisplayName() + " has received " + leg.getName() + " drop!", World.WORLD_NEWS);
        }
        random = Math.random();
        if (player.reaperPerkActivated(ReaperPerks.REAPERS_BLESSING) && ContractHandler.isContractNpc(player, this))
            random -= random * 0.05;
        double hiltChance = ((double) 1 / (double) (isDuoFight() ? 200 : 120));
        if (random <= hiltChance) {
            random = Math.random();
            int combatStyleHilt = RARE_REWARDS[getCombatStyle()];
            int hiltId = random <= 0.50 ? combatStyleHilt : -1;
            if (hiltId == -1) {
                while (hiltId == -1 && hiltId != combatStyleHilt)
                    hiltId = RARE_REWARDS[Utils.random(RARE_REWARDS.length)];
                if (hiltId == -1)
                    hiltId = RARE_REWARDS[Utils.random(RARE_REWARDS.length)];
            }
            Item hilt = new Item(hiltId, 1);
            playerRewards.add(hilt);
            player.getDropCollectionHandler().handleBossKills(hilt, DropCollectionConstants.ARAXXOR_ID);
            World.sendNews(player, player.getDisplayName() + " has received " + hilt.getName() + " drop!", World.WORLD_NEWS);
        }
        boolean hasAllPets = true;
        for (int i = 0; i < ARAXXOR_PETS.length; i++) {
            if (!player.hasItem(ARAXXOR_PETS[i])) {
                hasAllPets = false;
                break;
            }
        }
        boolean hasAnyPets = false;
        for (int i = 0; i < ARAXXOR_PETS.length; i++) {
            if (player.hasItem(ARAXXOR_PETS[i])) {
                hasAnyPets = true;
                break;
            }
        }
        int currentEnrageIncrease = fightEnrage - baseEnrage;
        random = Math.random();
        if (player.reaperPerkActivated(ReaperPerks.REAPERS_BLESSING) && ContractHandler.isContractNpc(player, this))
            random -= random * 0.05;
        double petChance = (double) 1 / (1000.00 - (double) currentEnrageIncrease);
        if (random <= petChance && !hasAnyPets) {
            int index = Utils.random(ARAXXOR_PETS.length);
            playerRewards.add(new Item(ARAXXOR_PETS[index], 1));
            player.getDropCollectionHandler().handleBossKills(new Item(ARAXXOR_PETS[index], 1), DropCollectionConstants.ARAXXOR_ID);
            World.sendNews(player, player.getDisplayName() + " has received " + ARAXXOR_PETS_NAME[index] + " drop!", World.WORLD_NEWS);
        }
        random = Math.random();
        if (player.reaperPerkActivated(ReaperPerks.REAPERS_BLESSING) && ContractHandler.isContractNpc(player, this))
            random -= random * 0.05;
        double araxyteEggChance = (double) 1 / 500.00;
        boolean hasAllEggPets = player.hasItem(ARAXXOR_EGG) || (player.hasItem(33809) && player.hasItem(33810));
        if (random <= araxyteEggChance && hasAllPets && !hasAllEggPets) {
            int id = player.hasItem(33809) ? 33810 : player.hasItem(33810) ? 33809 : ARAXXOR_EGG;
            Item pet = new Item(id, 1);
            playerRewards.add(pet);
            player.getDropCollectionHandler().handleBossKills(pet, DropCollectionConstants.ARAXXOR_ID);
            World.sendNews(player, player.getDisplayName() + " has received " + pet.getName() + " drop!", World.WORLD_NEWS);
        }
        if (hasAnyPets) {
            boolean[] challenges = this.challenges.get(player);
            if (challenges != null) {
                for (int i = 0; i < ARAXXOR_PETS.length; i++) {
                    if (!player.hasItem(ARAXXOR_PETS[i]) && challenges[i]) {
                        playerRewards.add(new Item(ARAXXOR_PETS[i], 1));
                        player.getPackets().sendGameMessage("<col=00FF00>Congratulations - you've unlocked the " + ARAXXOR_PETS_NAME[i] + " pet!");
                    }
                }
            }
        }
        for (Item i : playerRewards.getItemsCopy()) {
            player.getDropCollectionHandler().handleBossKills(i, DropCollectionConstants.ARAXXOR_ID);
        }
        rewards.put(player, playerRewards);
        player.getActivityTimersManager().finishBossTimer(Araxxor.this);
        player.addRaxKill();
    }

    public static final int ARAXXOR_EGG = 33835;

    public static final int[] ARAXXOR_PETS = { 31753, 31748, 31751, 31752, 31750, 31749 };
    public static final String[] ARAXXOR_PETS_NAME = { "Acidic spider", "Bladed spider", "Mirrorback spider", "Pulsing spider", "Imbued spider", "Spitting spider" };
    public static final double[] DROP_RATES = { 100.0, 90.0, 70.0, 2.0, 0.8 };

    public void increaseCurrentEnrage(int amount) {
        int currentEnrageIncrease = getFightEnrage() - baseEnrage;
        currentEnrageIncrease = currentEnrageIncrease + amount >= 500 ? 500 : currentEnrageIncrease + amount;
        fightEnrage = baseEnrage + currentEnrageIncrease;
        instance.updateInterface(false);
        setBonuses();
    }

    @Override
    public void handleIngoingHit(Hit hit) {
        if (getCapDamage() != -1 && hit.getDamage() > 15000) {
            hit.setDamage(15000);
        }
        super.handleIngoingHit(hit);
    }

    public void checkPhaseChangeByWorldTile(int lastX, int lastY) {
        if (choosenPath == -1)
            return;
        if (phase == 0) {
            boolean startNextPhase = false;
            WorldTile maxTile = instance.getTile(SpiderBossInstance.PHASE_CHANGE_BY_WORLDTILE[choosenPath][SpiderBossInstance.PHASE_CHANGE_BY_WORLDTILE[choosenPath].length - 2]);
            WorldTile minTile = instance.getTile(SpiderBossInstance.PHASE_CHANGE_BY_WORLDTILE[choosenPath][SpiderBossInstance.PHASE_CHANGE_BY_WORLDTILE[choosenPath].length - 1]);
            startNextPhase = lastX >= minTile.getX() && lastY >= minTile.getY() && lastY <= maxTile.getY();
            if (!startNextPhase)
                for (WorldTile tile : SpiderBossInstance.PHASE_CHANGE_BY_WORLDTILE[choosenPath]) {
                    WorldTile instancedTile = instance.getTile(tile);
                    if (lastX == instancedTile.getX() && lastY == instancedTile.getY()) {
                        startNextPhase = true;
                        break;
                    }
                }
            if (startNextPhase) {
                phase++;
                extraHP += getHitpoints();
                setHitpoints(getMaxHitpoints());
                onPhaseChange();
                instance.sendMessage(choosenPath == 2 ? "The spiders above are watching Araxxor as he retreats upwards, blocking out the light!" : "The spiders above are watching Araxxor...");
                instance.updateInterface(false);
            }
        }
        if (phase == 1 && choosenPath == 0) {
            WorldTile tile = instance.getTile(new WorldTile(4552, 6275, 1));
            boolean startNextPhase = lastX >= tile.getX();
            if (startNextPhase) {
                phase++;
                extraHP += getHitpoints();
                setHitpoints(getMaxHitpoints());
                onPhaseChange();
                instance.sendMessage("The spiders above take note of Araxxor's health and prepare to heal!");
                instance.updateInterface(false);
            }
        }
    }

    @Override
    public boolean restoreHitPoints() {
        if (getTemporaryAttributtes().get("phase4cutscene") != null)
            return false;
        boolean restore = super.restoreHitPoints();
        instance.updateInterface(false);
        return restore;
    }

    public void onPhaseChange() {
        if (choosenPath == 0 && phase == 1) {
            for (int i = 0; i < (isDuoFight() ? 40 : 20); i++) {
                int checkIndex = i >= 20 ? i - 20 : i;
                boolean specialMinion = checkIndex == 2 || checkIndex == 6 || checkIndex == 10;
                int spiderId = specialMinion ? (19468 + Utils.random(2)) : (19458 + Utils.random(3));
                spidersLeft.add(spiderId);
            }
        }
        if (choosenPath == 1 && phase == 1) {
            acidInPool = isDuoFight() ? 200 : 100;
            acidProcessCycle = Utils.currentTimeMillis();
        }
        if (choosenPath == 2 && phase == 1) {
            wallHealth = isDuoFight() ? 2000 : 1000;
            setCantInteract(true);
            setCantFollowUnderCombat(true);
            setNextAnimation(new Animation(24056));
            setNextGraphics(new Graphics(4985));
            WorldTasksManager.schedule(new WorldTask() {
                boolean sendSunLight = false;

                @Override
                public void run() {
                    if (hasFinished() || (getPossibleTargets().isEmpty())) {
                        stop();
                        return;
                    }
                    if (!sendSunLight) {
                        setCantInteract(false);
                        setNextWorldTile(new WorldTile(0, 0, 1));
                        darknessCycles = -1;
                        wallChargeDelay = Utils.currentTimeMillis() + 75000;// 1min
                                                                            // 15sec
                        sendSunLight = true;
                    } else {
                        sendRandomSunlight();
                        stop();
                    }
                }
            }, 5, 3);
        }
        if (phase == 2) {
            setCantFollowUnderCombat(false);
            double roundedExtra = Math.floor(((double) extraHP * 0.80) / 100.00);
            extraHP = (int) (roundedExtra * 100);
            if (extraHP > 0)
                extraHPCycles = Utils.currentTimeMillis() + 4000;
            if (extraHP >= 16000)
                for (Player player : instance.getPlayersInside()) {
                    if (player == null || player.isDead() || player.hasFinished())
                        continue;
                    unlockChallenge(player, 3);
                }
            if (choosenPath != 1 && instance.getFightRotation() != 2)
                acidInPool = isDuoFight() ? 200 : 100;
        }
        if (phase == 3) {
            absorpedAcid = 0;
            setCantFollowUnderCombat(true);
        }
    }

    public void sendExtraHeal(boolean die) {
        if (extraHP <= 0 || phase < 2)
            return;
        if (getHitpoints() >= 30_000)
            return;
        int amountToHeal = (int) (Math.floor((double) (getMaxHitpoints() - getHitpoints()) / 100.00) * 100);
        if (amountToHeal == 0)
            return;
        if (amountToHeal > extraHP)
            amountToHeal = extraHP;
        if (die) {
            setNextGraphics(new Graphics(5004));
            heal(amountToHeal, 0, 0, true);
            extraHP -= amountToHeal;
            if (extraHP <= 0)
                instance.sendMessage("The spiders above run out of energy to heal Araxxor.");
            instance.updateInterface(false);
            return;
        }
        WorldTile from = this.getMiddleWorldTile().transform((Utils.random(2) == 0 ? -1 : 1) * 7, (Utils.random(2) == 0 ? -1 : 1) * 7, 0);
        Projectile projectile = World.sendProjectileCycles(from, getMiddleWorldTile(), 5003, 208, 37, 0, 120, 5 + Utils.random(5), 0);
        long projectileCycles = Utils.projectileTimeToMiliseconds(projectile.getStartTime() + projectile.getEndTime() + 20);
        CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {

            @Override
            public boolean repeat() {
                try {
                    if (hasFinished() || getPossibleTargets().isEmpty())
                        return false;
                    if (extraHP <= 0)
                        return false;
                    int amountToHeal = (int) (Math.floor((double) (getMaxHitpoints() - getHitpoints()) / 100.00) * 100);
                    if (amountToHeal == 0)
                        return false;
                    if (amountToHeal > extraHP)
                        amountToHeal = extraHP;
                    heal(amountToHeal, 0, 0, true);
                    extraHP -= amountToHeal;
                    if (extraHP <= 0)
                        instance.sendMessage("The spiders above run out of energy to heal Araxxor.");
                    instance.updateInterface(false);
                    return false;
                } catch (Exception e) {
                    return false;
                }
            }
        }, projectileCycles, 600, TimeUnit.MILLISECONDS);
    }

    public void sendRemainingAcid() {
        killSpiders();
        instance.sendMessage("Araxxi absorbs some of the left over acid.");
        int[][] possibleDirs = { { 0, -1 }, { 0, 1 }, { -1, 0 }, { 1, 0 } };
        int[] dir = possibleDirs[Utils.random(possibleDirs.length)];
        WorldTile from = getMiddleWorldTile().transform(dir[0] * 5, dir[1] * 5, 0);
        int amount = acidInPool > 75 ? 75 : acidInPool > 50 ? 50 : acidInPool > 25 ? 25 : acidInPool;
        for (int i = 0; i < 3; i++) {
            if (acidInPool == 0)
                break;
            int multiplier = i == 2 ? -1 : i;
            World.sendProjectileCycles(from.transform(multiplier * dir[1], multiplier * dir[0], 0), getMiddleWorldTile(), 4979, 208, 37, 0, 120, 5 + Utils.random(5), 0);
            acidInPool -= amount > 25 ? 25 : amount;
            absorpedAcid += amount > 25 ? 25 : amount;
            if (acidInPool < 0)
                acidInPool = 0;
        }
        instance.updateInterface(false);
    }

    @Override
    public int getMaxHit() {
        int maxHit = 110;
        int maxHitIncreaseWithEnrage = 300;
        return (int) ((double) maxHit + ((double) maxHitIncreaseWithEnrage * ((double) getFightEnrage() / 800.0)));
    }

    @Override
    public void forceWalkRespawnTile() {

    }

    public int[] getCacheBonuses() {
        int[] bonuses = new int[8];
        if (instance == null)
            return bonuses;
        Map<Integer, Object> data = getDefinitions().clientScriptData;
        if (data != null) {
            Integer meleeDamage = (Integer) data.get(641);
            bonuses[0] = meleeDamage == null ? 0 : meleeDamage;
            Integer rangeDamage = (Integer) data.get(643);
            bonuses[1] = rangeDamage == null ? 0 : rangeDamage;
            Integer mageDamage = (Integer) data.get(965);
            bonuses[2] = mageDamage == null ? 0 : mageDamage;
            Integer meleeAccuracy = (Integer) data.get(29);
            bonuses[3] = meleeAccuracy == null ? 1 : meleeAccuracy;
            bonuses[3] += (int) ((double) bonuses[3] * ((double) getFightEnrage() / 100.00));
            Integer rangeAccuracy = (Integer) data.get(4);
            bonuses[4] = rangeAccuracy == null ? 1 : rangeAccuracy;
            bonuses[4] += (int) ((double) bonuses[4] * ((double) getFightEnrage() / 100.00));
            Integer magicAccuracy = (Integer) data.get(3);
            bonuses[5] = magicAccuracy == null ? 1 : magicAccuracy;
            bonuses[5] += (int) ((double) bonuses[5] * ((double) getFightEnrage() / 100.00));
            Integer armourBonus = (Integer) data.get(2865);
            bonuses[6] = armourBonus == null ? 1 : armourBonus;
            Integer critBonus = (Integer) data.get(2864);
            bonuses[7] = critBonus == null ? 1 : critBonus;
        } else
            for (int idx = 0; idx < bonuses.length; idx++)
                bonuses[idx] = 1;
        return bonuses;
    }

    @Override
    public void setNextAnimationNoPriority(Animation nextAnimation) {
        if (getTemporaryAttributtes().get("HealingWeb") != null || getTemporaryAttributtes().get("cocoonAttack") != null || getTemporaryAttributtes().get("changingPhase") != null)
            return;
        super.setNextAnimationNoPriority(nextAnimation);
    }

    @Override
    public void setNextAnimationForce(Animation nextAnimation) {
        if (getTemporaryAttributtes().get("HealingWeb") != null || getTemporaryAttributtes().get("cocoonAttack") != null || getTemporaryAttributtes().get("changingPhase") != null)
            return;
        super.setNextAnimationForce(nextAnimation);
    }

    @Override
    public void setNextAnimation(Animation nextAnimation) {
        if (getTemporaryAttributtes().get("HealingWeb") != null || getTemporaryAttributtes().get("cocoonAttack") != null || getTemporaryAttributtes().get("changingPhase") != null)
            return;
        super.setNextAnimation(nextAnimation);
    }

    @Override
    public void setNextGraphics(Graphics nextGraphics) {
        if (getTemporaryAttributtes().get("HealingWeb") != null || getTemporaryAttributtes().get("cocoonAttack") != null || getTemporaryAttributtes().get("changingPhase") != null)
            return;
        super.setNextGraphics(nextGraphics);
    }

    public int getFightEnrage() {
        int currentEnrageIncrease = fightEnrage - baseEnrage;
        int absorbedAcidEnrage = (int) ((double) absorpedAcid / 2.5);
        if ((absorbedAcidEnrage + currentEnrageIncrease) > 500)
            absorbedAcidEnrage = 500 - currentEnrageIncrease;
        return fightEnrage + absorbedAcidEnrage;
    }

    @Override
    public void finish() {
        clearArea();
        super.finish();
    }

    public void choosePath(int i) {
        if (choosenPath != -1)
            return;
        choosenPath = i;
        instance.sendBurnCountDown();
        for (WorldObject o : instance.getObjects()) {
            if (o == null)
                continue;
            if (o.getId() == SpiderBossInstance.PATH_SPECIFIC_OBJECT_IDS[i][0] && World.containsObjectWithId(o, o.getId())) {
                World.removeObject(o);
                o.setId(SpiderBossInstance.PATH_SPECIFIC_OBJECT_IDS[i][1]);
                World.spawnObject(o);
                burnWebCycles = Utils.currentTimeMillis() + 59700;
            }
        }
        for (int j = 0; j < SpiderBossInstance.PATH_SPECIFIC_OBJECT_IDS.length; j++) {
            for (WorldObject o : instance.getObjects()) {
                if (o != null && World.containsObjectWithId(o, o.getId()) && o.getId() == SpiderBossInstance.PATH_SPECIFIC_OBJECT_IDS[j][2])
                    World.removeObject(o);
            }
        }
    }

    public SpiderBossInstance getInstance() {
        return instance;
    }

    public int getPhase() {
        return phase;
    }

    public int getChoosenPath() {
        return choosenPath;
    }

    public void setPhase(int phase) {
        this.phase = phase;
    }

    public int getAutoAttacksLeft() {
        return autoAttacksLeft;
    }

    public void setAutoAttacksLeft(int autoAttacksLeft) {
        this.autoAttacksLeft = autoAttacksLeft;
    }

    public int getLastSpecialAttackIndex() {
        return lastSpecialAttackIndex;
    }

    public void setLastSpecialAttackIndex(int lastSpecialAttackIndex) {
        this.lastSpecialAttackIndex = lastSpecialAttackIndex;
    }

    public boolean isDuoFight() {
        return duoFight;
    }

    public void setDuoFight(boolean duoFight) {
        this.duoFight = duoFight;
    }

    public int getAttackDistance() {
        if (getId() == 19464)// araxxi
            return 30;
        if (phase == 1 && choosenPath == 1)
            return 2;
        int combatStyle = (getId() == 19457 || getId() == 19465) ? Combat.MELEE_TYPE : (getId() == 19462 || getId() == 19466) ? Combat.MAGIC_TYPE : Combat.RANGE_TYPE;
        return combatStyle == Combat.MELEE_TYPE ? 1 : 7;
    }

    public List<Integer> getSpidersLeft() {
        return spidersLeft;
    }

    public long getBurnWebDelay() {
        return burnWebDelay;
    }

    public int getExtraHP() {
        return extraHP;
    }

    public void setExtraHP(int extraHP) {
        this.extraHP = extraHP;
    }

    public int getAcidInPool() {
        return acidInPool;
    }

    public void setAcidInPool(int acidInPool) {
        this.acidInPool = acidInPool;
    }

    public int getAbsorpedAcid() {
        return absorpedAcid;
    }

    public void setAbsorpedAcid(int absorpedAcid) {
        this.absorpedAcid = absorpedAcid;
    }

    public int getWallHealth() {
        return wallHealth;
    }

    public long getWallChargeDelay() {
        return wallChargeDelay;
    }

    private boolean showWallHealth;

    public boolean isShowWallHealth() {
        return showWallHealth;
    }

    public boolean isSentWarningMessage() {
        return sentWarningMessage;
    }

    public long getDarknessCycles() {
        return darknessCycles;
    }

    public void setDarknessCycles(long darknessCycles) {
        this.darknessCycles = darknessCycles;
    }

    public List<AraxxorMinion> getMinions() {
        return minions;
    }

    public Entity getPartner(Entity currentTarget) {
        for (Player p : instance.getPlayersInside()) {
            if (p == null || p.isDead() || p.hasFinished() || p == currentTarget || !Utils.isOnRange(currentTarget, p, 3))
                continue;
            return p;
        }
        return null;
    }

    public static WorldTile[] getCocoonSpidersWorldTiles(Entity source, Entity target) {
        WorldTile[] tiles = new WorldTile[5];
        byte[] dir = Utils.getDirection(Utils.getAngle(source.getX() - target.getX(), source.getY() - target.getY()));
        tiles[0] = target.transform((-3 * (dir[1] != 0 ? -dir[1] : dir[1])) + (2 * dir[0]), (-3 * (dir[0] != 0 ? dir[0] : dir[0])) + (2 * dir[1]), 0);
        tiles[1] = target.transform((-2 * (dir[1] != 0 ? -dir[1] : dir[1])), (-2 * (dir[0] != 0 ? dir[0] : dir[0])), 0);
        tiles[2] = target.transform((2 * (dir[1] != 0 ? -dir[1] : dir[1])), (2 * (dir[0] != 0 ? dir[0] : dir[0])), 0);
        tiles[3] = target.transform((2 * (dir[1] != 0 ? -dir[1] : dir[1])) + (2 * dir[0]), (2 * (dir[0] != 0 ? dir[0] : dir[0])) + (2 * dir[1]), 0);
        tiles[4] = target.transform((3 * (dir[1] != 0 ? -dir[1] : dir[1])) + (dir[0]), (3 * (dir[0] != 0 ? dir[0] : dir[0])) + (dir[1]), 0);
        return tiles;
    }

    public WorldTile[] getMinionsAssistWorldTiles() {
        WorldTile[] tiles = new WorldTile[5];
        byte[] dir = Utils.getDirection(getDirection());
        WorldTile tile = getMiddleWorldTile();
        tiles[0] = tile.transform(((6) * dir[0]), ((6) * dir[1]), 0);
        tiles[1] = tile.transform(((-6) * (dir[1] != 0 ? -dir[1] : dir[1])), ((-6) * (dir[0] != 0 ? dir[0] : dir[0])), 0);
        tiles[2] = tile.transform(((3) * dir[0]), ((3) * dir[1]), 0);
        tiles[3] = tile.transform(((-3) * (dir[1] != 0 ? -dir[1] : dir[1])), ((-3) * (dir[0] != 0 ? dir[0] : dir[0])), 0);
        tiles[4] = tiles[0];
        return tiles;
    }

    public WorldTile[] getSludgeWorldTile(WorldTile source, Entity target, int startDistance, boolean reverse) {
        byte[] dir = Utils.getDirection(Utils.getAngle(source.getX() - target.getX(), source.getY() - target.getY()));
        WorldTile[] tiles = new WorldTile[8];
        tiles[0] = target.transform((((reverse ? -1 : 1) * -startDistance) * (dir[1] != 0 ? -dir[1] : dir[1])), (((reverse ? -1 : 1) * -startDistance) * (dir[0] != 0 ? dir[0] : dir[0])), 0);
        tiles[1] = target.transform((((reverse ? -1 : 1) * (startDistance / 2)) * (dir[1] != 0 ? -dir[1] : dir[1])), (((reverse ? -1 : 1) * (startDistance / 2)) * (dir[0] != 0 ? dir[0] : dir[0])), 0);
        tiles[2] = tiles[0];
        tiles[3] = target.transform(((startDistance) * dir[0]), ((startDistance) * dir[1]), 0);
        tiles[4] = target.transform((((reverse ? -1 : 1) * -2) * (dir[1] != 0 ? -dir[1] : dir[1])), (((reverse ? -1 : 1) * -2) * (dir[0] != 0 ? dir[0] : dir[0])), 0);
        tiles[5] = target.transform(((reverse ? -1 : 1) * (dir[1] != 0 ? -dir[1] : dir[1])), ((reverse ? -1 : 1) * (dir[0] != 0 ? dir[0] : dir[0])), 0);
        tiles[6] = target.transform((((reverse ? -1 : 1) * -1) * (dir[1] != 0 ? -dir[1] : dir[1])), (((reverse ? -1 : 1) * -1) * (dir[0] != 0 ? dir[0] : dir[0])), 0);
        for (int i = 1; i < 7; i++) {
            int random = Utils.random(-1, 2);
            if (i == 3) {
                random = Utils.random(-3, 0);
                tiles[i] = tiles[i].transform(((random) * (dir[1] != 0 ? -dir[1] : dir[1])), ((random) * (dir[0] != 0 ? dir[0] : dir[0])), 0);
                continue;
            }
            tiles[i] = tiles[i].transform(((random) * dir[0]), ((random) * dir[1]), 0);
        }
        for (int i = 0; i < 7; i++) {
            if (i == 3)
                continue;
            boolean change = !World.canMoveNPC(tiles[i].getPlane(), tiles[i].getX(), tiles[i].getY(), 1) || !World.isFloorFree(tiles[i].getPlane(), tiles[i].getX(), tiles[i].getY()) || !instance.isInsidePhase4Area(tiles[i]);
            if (change) {
                for (int j = 0; j <= 10; j++) {
                    WorldTile checkTile = tiles[i].transform(((j) * dir[0]), ((j) * dir[1]), 0);
                    if (World.canMoveNPC(checkTile.getPlane(), checkTile.getX(), checkTile.getY(), 1) && instance.isInsidePhase4Area(tiles[i])) {
                        tiles[i] = checkTile;
                        break;
                    }
                }
            }
        }
        tiles[7] = new WorldTile(target);
        return tiles;
    }

    private long acidProcessCycle;

    private void processAcid() {
        if (phase != 1 && choosenPath != 1)
            return;
        if (acidProcessCycle == 0 || acidProcessCycle > Utils.currentTimeMillis() || instance.getAcidGround() == null)
            return;
        WorldTile minArea = instance.getTile(SpiderBossInstance.ACID_POOL_AREA[0]);
        WorldTile maxArea = instance.getTile(SpiderBossInstance.ACID_POOL_AREA[1]);
        boolean inPool = getX() >= minArea.getX() && getY() >= minArea.getY() && getX() <= maxArea.getX() && getY() <= maxArea.getY();
        minArea = instance.getTile(SpiderBossInstance.ACID_POOL_AREA[2]);
        maxArea = instance.getTile(SpiderBossInstance.ACID_POOL_AREA[3]);
        boolean inDrain = getX() >= minArea.getX() && getY() >= minArea.getY() && getX() <= maxArea.getX() && getY() <= maxArea.getY();
        acidProcessCycle = Utils.currentTimeMillis() + 600;
        if (inPool) {
            int amount = acidInPool - 1 < 0 ? 0 : 1;
            acidInPool -= amount;
            absorpedAcid += amount;
        } else if (inDrain) {
            int percentageLeaked = (isDuoFight() ? 200 : 100) - (acidInPool + absorpedAcid);
            if (percentageLeaked < (isDuoFight() ? 100 : 50)) {
                int amount = absorpedAcid - 1 < 0 ? 0 : 1;
                absorpedAcid -= amount;
            }
        } else {

        }
        if (acidInPool == 0)
            for (Player player : instance.getPlayersInside()) {
                if (player == null || player.isDead() || player.hasFinished())
                    continue;
                unlockChallenge(player, 0);
            }
        setBonuses();
        instance.updateInterface(false);
        int percentageLeaked = (isDuoFight() ? 200 : 100) - (acidInPool + absorpedAcid);
        int newGroundId = percentageLeaked >= (isDuoFight() ? 100 : 50) ? 91522 : percentageLeaked >= 1 ? 91521 : 91520;
        if (instance.getAcidGround().getId() != newGroundId) {
            if (newGroundId == 91521)
                instance.sendMessage("The acid on Araxxor drops and starts to degrade the platform below.");
            World.removeObject(instance.getAcidGround());
            instance.getAcidGround().setId(newGroundId);
            World.spawnObject(instance.getAcidGround());
            if (newGroundId >= 91522) {
                acidProcessCycle = 0;
            }
        }
    }

    @Override
    public boolean clipedProjectile(WorldTile tile, boolean checkClose) {
        if (phase == 1 && choosenPath == 2)
            return true;
        return super.clipedProjectile(tile, checkClose);
    }

    @Override
    public boolean clipedProjectile(WorldTile tile, boolean checkClose, int size) {
        if (phase == 1 && choosenPath == 2)
            return true;
        return super.clipedProjectile(tile, checkClose, size);
    }

    public int getCombatStyle() {
        return combatStyle;
    }

    public void setCombatStyle(int combatStyle) {
        this.combatStyle = combatStyle;
        for (Player player : instance.getPlayersInside()) {
            if (player == null || player.isDead() || player.hasFinished())
                continue;
            unlockChallenge(player, combatStyle == NPCCombatDefinitionConstants.MELEE ? 1 : combatStyle == NPCCombatDefinitionConstants.MAGE ? 4 : 5);
        }
    }

    public Map<Entity, ItemsContainer<Item>> getRewards() {
        return rewards;
    }

    public int getBaseEnrage() {
        return baseEnrage;
    }

    public enum AraxxorAttacks {
        ATTACK() {
            @Override
            public int sendAttack(Araxxor spider, Player target) {
                if (spider.getInstance().getAcidGround().getId() == 91522) {
                    spider.setNextAnimation(new Animation(24056));
                    spider.setNextGraphics(new Graphics(4985));
                    spider.getTemporaryAttributtes().put("changingPhase", Boolean.TRUE);
                    spider.setNextFaceEntity(null);
                    spider.setNextFaceWorldTile(target);
                    spider.setCantInteract(true);
                    World.removeObject(spider.getInstance().getAcidGround());
                    spider.getInstance().getAcidGround().setId(91526);
                    World.spawnObject(spider.getInstance().getAcidGround());
                    WorldTile brokenRampTile = spider.getInstance().getTile(SpiderBossInstance.BROKEN_RAMP_WORLDTILE);
                    WorldObject brokenRamp = new WorldObject(SpiderBossInstance.BROKEN_RAMP_ID, 10, 0, brokenRampTile);
                    World.spawnObject(brokenRamp);
                    spider.getInstance().getObjects().add(brokenRamp);
                    spider.getInstance().sendMessage("As the platform degrades away. Araxxor flees to the next area calling on the spiders above to keep him alive!");
                    for (int i = 0; i < spider.getInstance().getPlayersInside().size(); i++) {
                        Player player = spider.getInstance().getPlayersInside().get(i);
                        if (player == null || player.isDead() || player.hasFinished())
                            continue;
                        player.getLocalNPCUpdate().reset();
                    }
                    WorldTasksManager.schedule(new WorldTask() {
                        int loop = 0;

                        @Override
                        public void run() {
                            if (spider.hasFinished() || (spider.getPossibleTargets().isEmpty() || spider.getInstance().getAraxxor() != spider) || spider.getTemporaryAttributtes().get("phase4cutscene") != null) {
                                spider.getTemporaryAttributtes().remove("changingPhase");
                                stop();
                                return;
                            }
                            if (loop == 0) {
                                World.removeObject(spider.getInstance().getAcidGround());
                                spider.getInstance().getAcidGround().setId(91527);
                                World.spawnObject(spider.getInstance().getAcidGround());
                            } else if (loop == 4) {
                                spider.setNextWorldTile(spider.getInstance().getTile(new WorldTile(4563, 6262, 1)));
                                spider.getTemporaryAttributtes().remove("changingPhase");
                                spider.setExtraHP(spider.getExtraHP() + spider.getHitpoints());
                                spider.setPhase(spider.getPhase() + 1);
                                spider.setHitpoints(spider.getMaxHitpoints());
                                spider.onPhaseChange();
                                spider.killSpiders();
                                spider.setAbsorpedAcid(0);
                                spider.setBonuses();
                                spider.getInstance().updateInterface(false);
                                spider.setNextAnimation(new Animation(24076));
                                spider.setNextGraphics(new Graphics(4984));
                                spider.setNextFaceEntity(target);
                            } else if (loop == 8) {
                                spider.resetWalkSteps();
                                spider.setCantInteract(false);
                                stop();
                            }
                            loop++;
                        }
                    }, 1, 0);
                    return 12;
                }
                if (Utils.random(5) == 0 && spider.switchTarget())
                    return 0;
                if (!(spider.getPhase() == 1 && spider.getChoosenPath() == 2) && !Utils.isOnRange(spider, target, spider.getAttackDistance()))
                    return 0;
                if (!spider.getSpidersLeft().isEmpty() && spider.getPhase() == 3 && spider.getHitpoints() <= 5000) {
                    return MINIONS_ASSIST.sendAttack(spider, target);
                }
                AraxxorAttacks attack = spider.getAutoAttacksLeft() == 0 ? AraxxorAttacks.SPECIAL_ATTACK : AraxxorAttacks.AUTO_ATTACK;
                if (attack != SPECIAL_ATTACK)
                    spider.setAutoAttacksLeft(spider.getAutoAttacksLeft() - 1 <= 0 ? 0 : spider.getAutoAttacksLeft() - 1);
                if (spider.getPhase() == 3 && spider.getHitpoints() <= 5000) {
                    attack = AraxxorAttacks.AUTO_ATTACK;
                    if (spider.getHitpoints() <= (spider.isDuoFight() ? 3500 : 2500))
                        spider.increaseCurrentEnrage(5);
                    attack.sendAttack(spider, target);
                    return 3;
                }
                if (spider.getPhase() == 1 && spider.getChoosenPath() == 2 && spider.isSentWarningMessage())
                    attack = AraxxorAttacks.AUTO_ATTACK;
                return attack.sendAttack(spider, target);
            }

        },
        AUTO_ATTACK() {
            @Override
            public int sendAttack(Araxxor spider, Player target) {
                if (spider.getPhase() == 1 && spider.getChoosenPath() == 2)
                    return AraxxorAttacks.RANGE_MAGE_ATTACK.sendAttack(spider, target);
                if (!Utils.isOnRange(spider, target, spider.getAttackDistance()))
                    return 0;
                if (spider.getPhase() == 3) {
                    if (Utils.isOnRange(spider, target, 0)) {
                        int randomAttack = Utils.random(3);
                        AraxxorAttacks attack = randomAttack == 0 ? AraxxorAttacks.MELEE_ATTACK : AraxxorAttacks.MAGE_ATTACK;
                        int delay = attack.sendAttack(spider, target);
                        return delay;
                    }
                    boolean hasRangeProtection = target.getPrayer().isRangeProtecting();
                    boolean hasMageProtection = target.getPrayer().isMageProtecting();
                    AraxxorAttacks attack = hasRangeProtection ? AraxxorAttacks.MAGE_ATTACK : hasMageProtection ? AraxxorAttacks.RANGE_ATTACK : Utils.random(2) == 0 ? AraxxorAttacks.MAGE_ATTACK : AraxxorAttacks.RANGE_ATTACK;
                    int delay = attack.sendAttack(spider, target);
                    return delay;
                }
                int combatStyle = (spider.getId() == 19457 || spider.getId() == 19465) ? NPCCombatDefinitionConstants.MELEE : (spider.getId() == 19462 || spider.getId() == 19466) ? NPCCombatDefinitionConstants.MAGE : NPCCombatDefinitionConstants.RANGE;
                if (combatStyle == NPCCombatDefinitionConstants.MELEE || Utils.isOnRange(spider, target, 0)) {
                    int randomAttack = Utils.random(4);
                    AraxxorAttacks attack = combatStyle == NPCCombatDefinitionConstants.MELEE || randomAttack == 0 ? AraxxorAttacks.MELEE_ATTACK : (combatStyle == NPCCombatDefinitionConstants.MAGE ? AraxxorAttacks.MAGE_ATTACK : AraxxorAttacks.RANGE_ATTACK);
                    int delay = attack.sendAttack(spider, target);
                    return delay;
                }
                AraxxorAttacks attack = combatStyle == NPCCombatDefinitionConstants.MAGE ? AraxxorAttacks.MAGE_ATTACK : AraxxorAttacks.RANGE_ATTACK;
                int delay = attack.sendAttack(spider, target);
                return delay;
            }
        },
        SPECIAL_ATTACK() {
            @Override
            public int sendAttack(Araxxor spider, Player target) {
                List<AraxxorAttacks> possibleAttacks = new ArrayList<AraxxorAttacks>();
                switch (spider.getPhase()) {
                case 0:
                    possibleAttacks.addAll(asList(CLEAVE_ATTACK, CLEAVE_ATTACK, SPIDER_WEB, COCOON_ATTACK));
                    break;
                case 1:
                    switch (spider.getChoosenPath()) {
                    case 0:
                        possibleAttacks.addAll(asList(CLEAVE_ATTACK, SPIDER_WEB, COCOON_ATTACK, EGG_BOMB, EGG_BOMB, MINIONS_ASSIST, MINIONS_ASSIST, MINIONS_ASSIST, MINIONS_ASSIST, MINIONS_ASSIST));
                        break;
                    case 1:
                        possibleAttacks.addAll(asList(CLEAVE_ATTACK, SPIDER_WEB, EGG_BOMB, ACIDIC_SPIDER_ATTACK));
                        break;
                    case 2:
                        possibleAttacks.addAll(asList(EGG_BOMB));
                        break;
                    }
                    break;
                case 2:

                    switch (spider.getInstance().getFightRotation()) {
                    case 0:
                        possibleAttacks.addAll(asList(CLEAVE_ATTACK, SPIDER_WEB, COCOON_ATTACK, EGG_BOMB, MINIONS_ASSIST, MINIONS_ASSIST, ACIDIC_SPIDER_ATTACK, HIGHLY_ACIDIC_SPIDER_ATTACK));
                        break;
                    case 1:
                        possibleAttacks.addAll(asList(CLEAVE_ATTACK, SPIDER_WEB, COCOON_ATTACK, EGG_BOMB, TEMPORARY_DARKNESS, ACIDIC_SPIDER_ATTACK, HIGHLY_ACIDIC_SPIDER_ATTACK));
                        break;
                    case 2:
                        possibleAttacks.addAll(asList(CLEAVE_ATTACK, SPIDER_WEB, COCOON_ATTACK, EGG_BOMB, TEMPORARY_DARKNESS, MINIONS_ASSIST, MINIONS_ASSIST));
                        break;
                    }
                    break;
                case 3:
                    switch (spider.getInstance().getFightRotation()) {
                    case 0:
                        possibleAttacks.addAll(asList(CLEAVE_ATTACK, SPIDER_WEB, COCOON_ATTACK, MINIONS_ASSIST, MINIONS_ASSIST));
                        break;
                    case 1:
                        possibleAttacks.addAll(asList(CLEAVE_ATTACK, SPIDER_WEB, COCOON_ATTACK, TEMPORARY_DARKNESS));
                        break;
                    case 2:
                        possibleAttacks.addAll(asList(CLEAVE_ATTACK, SPIDER_WEB, COCOON_ATTACK, TEMPORARY_DARKNESS, MINIONS_ASSIST, MINIONS_ASSIST));
                        break;
                    }
                    break;
                }
                if (spider.getPhase() == 0 || (spider.getPhase() == 1 && spider.getChoosenPath() != 2)) {
                    int lastSpecIndex = spider.getLastSpecialAttackIndex();
                    if (lastSpecIndex != -1)
                        possibleAttacks.removeIf(values()[lastSpecIndex]::equals);
                }
                if (spider.getSpidersLeft().isEmpty())
                    possibleAttacks.removeIf(MINIONS_ASSIST::equals);
                if (spider.getAcidInPool() <= 0)
                    possibleAttacks.removeIf(HIGHLY_ACIDIC_SPIDER_ATTACK::equals);
                AraxxorAttacks attack = possibleAttacks.get(Utils.random(possibleAttacks.size()));
                spider.setAutoAttacksLeft(attack == ACIDIC_SPIDER_ATTACK ? 7 : attack == COCOON_ATTACK ? 4 : 5);
                spider.setLastSpecialAttackIndex(attack.ordinal());
                return attack.sendAttack(spider, target);
            }
        },

        RANGE_ATTACK() {
            @Override
            public int sendAttack(Araxxor spider, Player target) {
                spider.setNextAnimation(new Animation(24047));
                Projectile projectile = World.sendProjectileCycles(spider, target, 4997, 54, 30, 15, 55, Utils.random(5), 160);
                long projectileCycles = Utils.projectileTimeToMiliseconds(projectile);
                CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
                    Entity secondTarget = null;

                    @Override
                    public boolean repeat() {
                        try {
                            if (spider.hasFinished() || (spider.getPossibleTargets().isEmpty() || spider.getInstance().getAraxxor() != spider) || spider.getTemporaryAttributtes().get("phase4cutscene") != null) {
                                return false;
                            }
                            Entity partner = spider.getPartner(target);
                            Player t = secondTarget != null ? (Player) secondTarget : target;
                            t.setNextGraphics(new Graphics(4998));
                            int damage = CombatScript.getMaxHit(spider, spider.getMaxHit(), NPCCombatDefinitionConstants.RANGE, target);
                            boolean doubleDamage = secondTarget == null && spider.isDuoFight() && partner == null;
                            if (doubleDamage)
                                damage *= 2;
                            CombatScript.delayHit(spider, 0, t, new Hit(spider, damage, HitLook.RANGE_DAMAGE));
                            if (damage > 0 && Utils.random(5) == 0) {
                                t.getPoison().makePoisoned(Utils.random(10, 201));
                            }
                            if (secondTarget == null) {
                                if (spider.isDuoFight() && partner != null) {
                                    World.sendProjectileCycles(t, partner, 4997, 30, 30, 0, 40, Utils.random(5), 0);
                                    secondTarget = partner;
                                    return true;
                                }
                            }
                            return false;
                        } catch (Exception e) {
                            Logger.getGlobal().catching(e);
                            return false;
                        }
                    }

                }, projectileCycles, Utils.projectileTimeToMiliseconds(40), TimeUnit.MILLISECONDS);
                return 12;
            }
        },
        MELEE_ATTACK() {
            @Override
            public int sendAttack(Araxxor spider, Player target) {
                spider.setNextAnimation(new Animation(24046));
                CombatScript.delayHit(spider, 0, target, new Hit(spider, CombatScript.getMaxHit(spider, spider.getMaxHit(), NPCCombatDefinitionConstants.MELEE, target), HitLook.MELEE_DAMAGE));
                return 7;
            }
        },
        RANGE_MAGE_ATTACK() {
            @Override
            public int sendAttack(Araxxor spider, Player target) {
                boolean hasRangeProtection = target.getPrayer().isRangeProtecting();
                boolean hasMageProtection = target.getPrayer().isMageProtecting();
                int combatStyle = hasRangeProtection ? NPCCombatDefinitionConstants.MAGE : hasMageProtection ? NPCCombatDefinitionConstants.RANGE : Utils.random(2) == 0 ? NPCCombatDefinitionConstants.MAGE : NPCCombatDefinitionConstants.RANGE;
                WorldTile from = target.transform((Utils.random(2) == 0 ? 1 : -1) * 7, (Utils.random(2) == 0 ? 1 : -1) * 2, 0);
                Projectile projectile = World.sendProjectileCycles(from, target, combatStyle == NPCCombatDefinitionConstants.MAGE ? 4979 : 4997, 196, 37, 0, combatStyle == NPCCombatDefinitionConstants.MAGE ? 175 : 129, 5 + Utils.random(5), 0);
                long projectileCycles = Utils.projectileTimeToMiliseconds(projectile.getStartTime() + projectile.getEndTime() + 20);
                CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
                    Entity secondTarget = null;

                    @Override
                    public boolean repeat() {
                        try {
                            if (spider.hasFinished() || (spider.getPossibleTargets().isEmpty() || spider.getInstance().getAraxxor() != spider) || spider.getTemporaryAttributtes().get("phase4cutscene") != null) {
                                return false;
                            }
                            Entity partner = spider.getPartner(target);
                            Player t = secondTarget != null ? (Player) secondTarget : target;
                            if (combatStyle == NPCCombatDefinitionConstants.MAGE) {
                                int damage = CombatScript.getMaxHit(spider, spider.getMaxHit(), NPCCombatDefinitionConstants.MAGE, t);
                                boolean doubleDamage = secondTarget == null && spider.isDuoFight() && partner == null;
                                if (doubleDamage)
                                    damage *= 2;
                                t.setNextGraphics(new Graphics(4980));
                                CombatScript.delayHit(spider, 0, t, new Hit(spider, damage, HitLook.MAGIC_DAMAGE));
                            } else {
                                t.setNextGraphics(new Graphics(4998));
                                int damage = CombatScript.getMaxHit(spider, spider.getMaxHit(), NPCCombatDefinitionConstants.RANGE, t);
                                boolean doubleDamage = secondTarget == null && spider.isDuoFight() && partner == null;
                                if (doubleDamage)
                                    damage *= 2;
                                CombatScript.delayHit(spider, 0, t, new Hit(spider, damage, HitLook.RANGE_DAMAGE));
                                if (damage > 0 && Utils.random(5) == 0) {
                                    t.getPoison().makePoisoned(Utils.random(10, 201));
                                }
                            }
                            if (secondTarget == null) {
                                if (spider.isDuoFight() && partner != null) {
                                    World.sendProjectileCycles(t, partner, combatStyle == NPCCombatDefinitionConstants.MAGE ? 4979 : 4997, 30, 30, 0, combatStyle == NPCCombatDefinitionConstants.MAGE ? 50 : 40, Utils.random(5), 0);
                                    secondTarget = partner;
                                    return true;
                                }
                            }
                            return false;
                        } catch (Exception e) {
                            return false;
                        }
                    }
                }, projectileCycles, Utils.projectileTimeToMiliseconds(combatStyle == NPCCombatDefinitionConstants.MAGE ? 50 : 40), TimeUnit.MILLISECONDS);
                return 12;
            }

        },
        MAGE_ATTACK() {
            @Override
            public int sendAttack(Araxxor spider, Player target) {
                spider.setNextAnimation(new Animation(24047));
                spider.setNextGraphics(new Graphics(4978));
                Projectile projectile = World.sendProjectileCycles(spider, target, 4979, 54, 30, 30, 80, Utils.random(5), 157);
                long projectileCycles = Utils.projectileTimeToMiliseconds(projectile);
                CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
                    Entity secondTarget = null;

                    @Override
                    public boolean repeat() {
                        try {
                            if (spider.hasFinished() || (spider.getPossibleTargets().isEmpty() || spider.getInstance().getAraxxor() != spider) || spider.getTemporaryAttributtes().get("phase4cutscene") != null) {
                                return false;
                            }
                            Entity partner = spider.getPartner(target);
                            Player t = secondTarget != null ? (Player) secondTarget : target;
                            int damage = CombatScript.getMaxHit(spider, spider.getMaxHit(), NPCCombatDefinitionConstants.MAGE, target);
                            boolean doubleDamage = secondTarget == null && spider.isDuoFight() && partner == null;
                            if (doubleDamage)
                                damage *= 2;
                            t.setNextGraphics(new Graphics(4980));
                            CombatScript.delayHit(spider, 0, t, new Hit(spider, damage, HitLook.MAGIC_DAMAGE));
                            if (secondTarget == null) {
                                if (spider.isDuoFight() && partner != null) {
                                    World.sendProjectileCycles(t, partner, 4979, 30, 30, 0, 50, Utils.random(5), 0);
                                    secondTarget = partner;
                                    return true;
                                }
                            }
                            return false;
                        } catch (Exception e) {
                            Logger.getGlobal().catching(e);
                            return false;
                        }
                    }

                }, projectileCycles, Utils.projectileTimeToMiliseconds(50), TimeUnit.MILLISECONDS);
                return 12;
            }

        },

        CLEAVE_ATTACK() {

            @Override
            public int sendAttack(Araxxor spider, Player target) {
                spider.setNextAnimation(new Animation(24050));
                spider.setNextGraphics(new Graphics(4986));
                spider.setNextFaceEntity(null);
                spider.setNextFaceWorldTile(target);
                spider.getTemporaryAttributtes().put("CantMove", Boolean.TRUE);
                boolean dragTarget = !(spider.getChoosenPath() == 1 && spider.getPhase() == 1) && !Utils.isOnRange(spider, target, 1);
                if (dragTarget) {
                    byte[] dir = Utils.getDirection(spider.getDirection());
                    WorldTile araxxor = spider.transform((int) (2 * (dir[0] < 0 ? 0 : dir[0] == 0 && dir[1] != 0 ? 1 : dir[0] == 0 ? 0.5 : dir[0] == 1 ? 2 : dir[0])), (int) (4 * (dir[1] < 0 ? 0 : dir[1] == 0 ? 0.5 : dir[1])), 0);
                    WorldTile toTile = araxxor.transform(dir[0], dir[1], 0);
                    target.resetWalkSteps();
                    target.setNextForceMovement(new ForceMovement(toTile, 1, Utils.getAngle(dir[0], dir[1])));
                    WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                            target.setNextWorldTile(toTile);
                        }
                    }, 0);
                }
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        if (spider.hasFinished() || (spider.getPossibleTargets().isEmpty() || spider.getInstance().getAraxxor() != spider) || spider.getTemporaryAttributtes().get("phase4cutscene") != null) {
                            spider.getTemporaryAttributtes().remove("CantMove");
                            stop();
                            return;
                        }
                        int enrageIncrease = 0;
                        for (Entity t : spider.getCleaveTargets()) {
                            if (t == null || t.isDead() || (!Utils.isOnRange(spider, t, t instanceof Familiar ? 3 : t instanceof NPC ? 2 : 1)))
                                continue;
                            int baseDamage = t instanceof Player ? (400 * (spider.isDuoFight() ? 2 : 1)) : t instanceof Familiar ? 120 : 300;
                            int maxIncrease = t instanceof Familiar ? 80 : 600;
                            int damage = (int) ((double) baseDamage + ((double) maxIncrease * Math.min(((double) spider.getFightEnrage() / 400.00), 1.00)));

                            if (t instanceof Familiar)
                                for (int i = 1; i < 5; i++)
                                    if (Utils.isOnRange(spider, t, i)) {
                                        damage -= 20 * i;
                                        break;
                                    }
                            if (t instanceof AraxxorMinion)
                                enrageIncrease += enrageIncrease == 0 ? 10 : 5;
                            CombatScript.delayHit(spider, 0, t, new Hit(spider, damage, t instanceof NPC ? HitLook.MELEE_DAMAGE : HitLook.REGULAR_DAMAGE));
                        }
                        spider.increaseCurrentEnrage(enrageIncrease);
                        spider.setNextFaceEntity(target);
                        spider.getTemporaryAttributtes().remove("CantMove");
                    }

                }, 4);
                return 6;
            }

        },

        SPIDER_WEB() {

            @Override
            public int sendAttack(Araxxor spider, Player target) {
                spider.setNextGraphics(new Graphics(-1));
                spider.setNextAnimation(new Animation(24075));
                spider.setNextGraphics(new Graphics(4987));
                spider.setNextFaceEntity(null);
                // spider.setNextFaceWorldTile(target);//most likely from this line cuz its not
                // correct on your source.
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        spider.setNextAnimation(new Animation(24075));
                        spider.getTemporaryAttributtes().put("HealingWeb", Boolean.TRUE);
                    }
                }, 2);
                WorldTasksManager.schedule(new WorldTask() {
                    private int loop = 0;

                    @Override
                    public void run() {
                        if (spider.hasFinished() || (spider.getPossibleTargets().isEmpty() || spider.getInstance().getAraxxor() != spider) || spider.getTemporaryAttributtes().get("phase4cutscene") != null) {
                            spider.getTemporaryAttributtes().remove("HealingWeb");
                            spider.getTemporaryAttributtes().remove("SkipHeal");
                            stop();
                            return;
                        }
                        if (loop % 2 == 0) {
                            if (spider.getTemporaryAttributtes().remove("SkipHeal") == null)
                                spider.heal(spider.isDuoFight() ? 200 : 100, 0, 0, false);
                        }
                        if (loop >= 11) {
                            spider.getTemporaryAttributtes().remove("SkipHeal");
                            spider.getTemporaryAttributtes().remove("HealingWeb");
                            spider.setNextFaceEntity(target);
                            stop();
                            return;
                        }
                        loop++;
                    }
                }, 3, 0);
                return 5;
            }

        },

        COCOON_ATTACK() {

            public int sendAttack(Araxxor spider, Player target) {
                spider.setNextAnimation(new Animation(24083));
                spider.setNextGraphics(new Graphics(4983));
                spider.getTemporaryAttributtes().put("cocoonAttack", Boolean.TRUE);
                spider.setNextFaceEntity(null);
                spider.setNextFaceWorldTile(target);
                for (Entity t : spider.getPossibleTargets()) {
                    for (WorldTile from : Araxxor.getCocoonSpidersWorldTiles(spider, t)) {
                        World.sendProjectileCycles(from, t, 2839, 0, 0, 20, 80, 0, 0);
                    }
                }
                CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
                    int loop = 0;

                    @Override
                    public boolean repeat() {
                        try {
                            if (spider.hasFinished() || (spider.getPossibleTargets().isEmpty() || spider.getInstance().getAraxxor() != spider) || spider.getTemporaryAttributtes().get("phase4cutscene") != null) {
                                spider.getTemporaryAttributtes().remove("CantMove");
                                spider.getTemporaryAttributtes().remove("cocoonAttack");
                                for (Player t : spider.getInstance().getPlayersInside()) {
                                    if (t.getTemporaryAttributtes().get("cocoonAttacks") == null)
                                        continue;
                                    t.getTemporaryAttributtes().remove("cantMove");
                                    t.getTemporaryAttributtes().remove("cocoonAttacks");
                                    t.getAppearence().transformIntoNPC(-1);
                                    t.getAppearence().setRenderEmote(-1);
                                }
                                return false;
                            }
                            if (loop == 7) {
                                spider.setNextFaceEntity(target);
                                spider.getTemporaryAttributtes().remove("cocoonAttack");
                                for (Player t : spider.getInstance().getPlayersInside()) {
                                    if (t.getTemporaryAttributtes().get("cocoonAttacks") == null)
                                        continue;
                                    t.getTemporaryAttributtes().put("cantMove", Utils.currentTimeMillis() + (long) 1000);
                                    t.getTemporaryAttributtes().remove("cocoonAttacks");
                                    t.resetWalkSteps();
                                    t.getAppearence().transformIntoNPC(-1);
                                    t.getAppearence().setRenderEmote(-1);
                                    t.setNextGraphics(new Graphics(4993));
                                }
                                return false;
                            }
                            if (loop == 0) {
                                for (Player t : spider.getInstance().getPlayersInside()) {
                                    if (t == null || t.isDead() || t.hasFinished())
                                        continue;
                                    int amountToFree = 3;
                                    t.getTemporaryAttributtes().put("cocoonAttacks", amountToFree);
                                    t.resetWalkSteps();
                                    t.setNextAnimation(new Animation(-1));
                                    t.getAppearence().transformIntoNPC(19472);
                                    t.getAppearence().setRenderEmote(3199);
                                }
                            } else if (loop >= 1) {
                                int damage = 20;
                                damage += (int) (Math.min((double) spider.getFightEnrage() / 300.00, 1) * 60.00);
                                for (Player t : spider.getInstance().getPlayersInside()) {
                                    if (t == null || t.isDead() || t.hasFinished())
                                        continue;
                                    if (t.getTemporaryAttributtes().get("cocoonAttacks") != null)
                                        CombatScript.delayHit(spider, 0, t, new Hit(spider, damage, HitLook.REGULAR_DAMAGE));
                                }
                            }
                            if (loop != 0 && loop % 2 == 0) {
                                for (Player t : spider.getInstance().getPlayersInside()) {
                                    if (t == null || t.isDead() || t.hasFinished())
                                        continue;
                                    if (t.getTemporaryAttributtes().get("cocoonAttacks") != null) {
                                        Projectile projectile = World.sendProjectileCycles(spider, t, 4991, 66, 30, 30, 80, Utils.random(5), 100);
                                        long projectileCycles = Utils.projectileTimeToMiliseconds(projectile);
                                        CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
                                            @Override
                                            public boolean repeat() {
                                                try {
                                                    t.setNextGraphics(new Graphics(4992));
                                                    return false;
                                                } catch (Exception e) {
                                                    Logger.getGlobal().catching(e);
                                                    return false;
                                                }
                                            }

                                        }, projectileCycles, 600, TimeUnit.MILLISECONDS);
                                    }
                                }
                            }
                            loop++;
                            return true;
                        } catch (Exception e) {
                            Logger.getGlobal().catching(e);
                            return false;
                        }
                    }

                }, 1000, 600, TimeUnit.MILLISECONDS);
                return 12;
            }

        },

        EGG_BOMB() {

            public int sendAttack(Araxxor spider, Player target) {
                boolean fromCeiling = spider.getPhase() == 1 && spider.getChoosenPath() == 2;
                if (!fromCeiling) {
                    spider.setNextAnimation(new Animation(24085));
                    spider.setNextGraphics(new Graphics(4994));
                }
                WorldTile from = !fromCeiling ? spider : target.transform((Utils.random(2) == 0 ? 1 : -1) * 7, (Utils.random(2) == 0 ? 1 : -1) * 2, 0);
                Projectile projectile = World.sendProjectileCycles(from, target, 4995, fromCeiling ? 196 : 27, fromCeiling ? 37 : 0, fromCeiling ? 0 : 39, fromCeiling ? 500 : 300, (fromCeiling ? 5 : 40) + Utils.random(5), fromCeiling ? 0 : 406);
                long projectileCycles = Utils.projectileTimeToMiliseconds(projectile.getStartTime() + projectile.getEndTime() + (fromCeiling ? 100 : 39));
                WorldTile eggSpawnLocation = spider.getInstance().getRandomWorldTile(fromCeiling ? target : spider, fromCeiling ? 3 : 5, fromCeiling ? 7 : 10, fromCeiling);
                int eggsCount = Utils.random(10) == 0 ? 2 : 3;
                int spawnedCount = 0;
                WorldTile[] spawnedLocations = new WorldTile[eggsCount];
                for (int i = 0; i < eggsCount; i++) {
                    if (i == 0) {
                        spider.spawnEgg(eggSpawnLocation);
                        spawnedLocations[spawnedCount] = eggSpawnLocation;
                        spawnedCount++;
                    } else {
                        for (int x = eggSpawnLocation.getX() - 1; x <= eggSpawnLocation.getX() + 1; x++) {
                            for (int y = eggSpawnLocation.getY() + 1; y >= eggSpawnLocation.getY() - 1; y--) {
                                WorldTile checkTile = new WorldTile(x, y, spider.getPlane());
                                if (World.containsObjectWithId(checkTile, 91641) || !World.canMoveNPC(spider.getPlane(), x, y, 1) || spawnedCount == eggsCount)
                                    continue;
                                spider.spawnEgg(checkTile);
                                spawnedLocations[spawnedCount] = checkTile;
                                spawnedCount++;
                            }
                        }
                    }
                }
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        for (WorldTile egg : spawnedLocations)
                            spider.spawnSpiderEgg(egg);
                    }
                }, (int) ((projectileCycles + 4800) / 600));
                CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {

                    @Override
                    public boolean repeat() {
                        try {
                            if (spider.hasFinished() || (spider.getPossibleTargets().isEmpty() || spider.getInstance().getAraxxor() != spider) || spider.getTemporaryAttributtes().get("phase4cutscene") != null) {
                                return false;
                            }
                            List<Object> targets = spider.getEggBombTargets(target);
                            int playerDamage = 300 - (targets.size() * 90);
                            if (playerDamage > 0) {
                                target.applyHit(new Hit(spider, playerDamage, HitLook.REGULAR_DAMAGE));
                            }
                            int enrageIncrease = 0;
                            for (Object t : targets) {
                                if (t == null)
                                    continue;
                                if (t instanceof WorldObject) {
                                    spider.bombEgg((WorldObject) t);
                                    enrageIncrease += enrageIncrease == 0 ? 2 : 1;
                                } else
                                    ((Entity) t).applyHit(new Hit(spider, 300, HitLook.UNBLOCKABLE_REGULAR_DAMAGE));
                            }
                            spider.increaseCurrentEnrage(enrageIncrease);
                            return false;
                        } catch (Exception e) {
                            Logger.getGlobal().catching(e);
                            return false;
                        }
                    }

                }, projectileCycles, 600, TimeUnit.MILLISECONDS);
                return fromCeiling ? 16 : 4;
            }

        },

        MINIONS_ASSIST() {

            public int sendAttack(Araxxor spider, Player target) {
                spider.setNextAnimation(new Animation(24047));
                int reduceAmount = (spider.getSpidersLeft().size() < (spider.isDuoFight() ? 10 : 5)) ? spider.getSpidersLeft().size() : (spider.isDuoFight() ? 10 : 5);
                int[] spiderIds = new int[reduceAmount];
                ListIterator<Integer> listIterator = spider.getSpidersLeft().listIterator();
                for (int i = 0; i < reduceAmount; i++) {
                    spiderIds[i] = listIterator.next();
                    listIterator.remove();
                }
                WorldTile[] tiles = spider.getMinionsAssistWorldTiles();
                for (int i = 0; i < tiles.length; i++) {
                    WorldTile from = tiles[i];
                    World.sendProjectileCycles(from, spider, 2839, 0, 0, 0, 15 - (i * 3), 0, 0);
                }
                spider.getInstance().updateInterface(false);
                WorldTasksManager.schedule(new WorldTask() {
                    int loop = 0;
                    final List<WorldTile> spawnTiles = new ArrayList<WorldTile>();

                    @Override
                    public void run() {
                        if (spider.hasFinished() || (spider.getPossibleTargets().isEmpty() || spider.getInstance().getAraxxor() != spider) || spider.getTemporaryAttributtes().get("phase4cutscene") != null) {
                            stop();
                            return;
                        }
                        if (loop >= reduceAmount) {
                            stop();
                            return;
                        }
                        WorldTile spawnTile = spider.getInstance().getRandomWorldTile(spider, 0, 10, true, false, spawnTiles);
                        AraxxorMinion minion = spider.spawnMinion(spiderIds[loop], spawnTile, -1);
                        minion.setFromMinionAssist(true);
                        loop++;
                    }
                }, 2, 2);
                return 4;
            }

        },

        ACIDIC_SPIDER_ATTACK() {

            public int sendAttack(Araxxor spider, Player target) {
                spider.setNextAnimation(new Animation(24046));
                WorldTile spawnTile = spider.getInstance().getRandomWorldTile(target, 1, 10, false);
                Projectile projectile = World.sendProjectileCycles(spider, spawnTile, 4997, 66, 0, 30, 120, 15 + Utils.random(5), 100);
                long projectileCycles = Utils.projectileTimeToMiliseconds(projectile);
                target.getPackets().sendExecuteScript(1211, "<col=ff0000>Araxxor has sent an instant kill spider after you BE CAREFULL!", 5, -120, 1);
                target.getPackets().sendGameMessage("<col=ff0000>Araxxor has sent an instant kill spider after you BE CAREFULL!", true);
                CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
                    WorldObject object;

                    @Override
                    public boolean repeat() {
                        try {
                            if (spider.hasFinished() || (spider.getPossibleTargets().isEmpty() || spider.getInstance().getAraxxor() != spider) || spider.getTemporaryAttributtes().get("phase4cutscene") != null) {
                                return false;
                            }
                            if (object == null) {
                                World.sendGraphics(null, new Graphics(5006), spawnTile);
                                object = new WorldObject(91671, 10, 0, spawnTile);
                                spider.getInstance().getObjects().add(object);
                                WorldTasksManager.schedule(new WorldTask() {
                                    @Override
                                    public void run() {
                                        World.spawnObject(object);
                                    }
                                }, 1);
                                return true;
                            } else {
                                if (World.containsObjectWithId(object, object.getId()))
                                    World.removeObject(object);
                                AraxxorMinion minion = spider.spawnMinion(19470, spawnTile, 24054);
                                minion.setFollowTarget(target);
                                return false;
                            }
                        } catch (Exception e) {
                            return false;
                        }
                    }

                }, projectileCycles, 5000, TimeUnit.MILLISECONDS);
                return 0;
            }

        },

        HIGHLY_ACIDIC_SPIDER_ATTACK() {

            public int sendAttack(Araxxor spider, Player target) {
                spider.setNextAnimation(new Animation(24046));
                WorldTile spawnTile = spider.getInstance().getRandomWorldTile(spider, 3, 7, false);
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        if (spider.hasFinished() || (spider.getPossibleTargets().isEmpty() || spider.getInstance().getAraxxor() != spider) || spider.getTemporaryAttributtes().get("phase4cutscene") != null) {
                            stop();
                            return;
                        }
                        AraxxorMinion minion = spider.spawnMinion(19471, spawnTile, 24054);
                        minion.setRandomWalk(NPC.NORMAL_WALK);
                        int amountAcid = spider.getAcidInPool() - 25 <= 0 ? spider.getAcidInPool() : 25;
                        minion.setAcidAmount(amountAcid);
                        spider.setAcidInPool(spider.getAcidInPool() - amountAcid);
                        spider.getInstance().sendMessage("A highly acidic spider appears nearby!");
                        spider.getInstance().updateInterface(false);
                    }
                }, 2);
                return 4;
            }

        },

        TEMPORARY_DARKNESS() {

            @Override
            public int sendAttack(Araxxor spider, Player target) {
                spider.getInstance().sendMessage((spider.getId() == 19464 ? "Araxxi" : "Araxxor") + " plunges the area into temporary darkness!");
                spider.sendTemporaryDarkness();
                return 4;
            }

        };

        public int sendAttack(Araxxor spider, Player target) {
            return 0;
        }

        public static ArrayList<AraxxorAttacks> asList(AraxxorAttacks... attacks) {
            return new ArrayList<AraxxorAttacks>(Arrays.asList(attacks));
        }

        public String toString() {
            return this.name();
        }
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
    public int getMaxDistance() {
        return 1000000;
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
