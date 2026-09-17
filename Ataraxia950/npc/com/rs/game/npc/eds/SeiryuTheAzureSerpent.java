package com.rs.game.npc.eds;

import com.rs.Settings;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.Projectile;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activites.gim.GIM;
import com.rs.game.hitbar.HitBar;
import com.rs.game.item.Item;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.actions.herblore.herbicide.Herbicide;
import com.rs.game.player.content.HybridTokenDistributor;
import com.rs.game.player.content.achievementsystem.AchievementList;
import com.rs.game.player.content.contracts.ContractHandler;
import com.rs.game.player.content.eds.EliteDungeonsConstants;
import com.rs.game.player.content.eds.rooms.EliteDungeonHandledRoom;
import com.rs.game.player.content.eds.rooms.ed1.Room5;
import com.rs.game.player.content.hcim_news.HcimNewsManager;
import com.rs.game.player.content.items.Bonecrusher;
import com.rs.game.player.content.items.CoinAccumulator;
import com.rs.game.player.content.items.Ectoplasmator;
import com.rs.game.player.content.petperks.PetPerk;
import com.rs.game.player.content.petperks.PetPerkUtils;
import com.rs.game.player.controllers.EliteDungeonController;
import com.rs.game.route.RouteFinder;
import com.rs.game.route.strategy.FixedTileStrategy;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Logger;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.npcs.pojos.NPCDrop;
import com.rs.utils.mysql.QueryExecutor;
import com.rs.utils.mysql.impl.News;
import lombok.Getter;
import lombok.val;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

public class SeiryuTheAzureSerpent extends EliteDungeonBoss {

    private static final long serialVersionUID = -8662168895243703461L;
    private transient WorldObject shadowTail;
    private transient List<BlackHole> blackHoles;
    private int attackProgress;
    private transient Crystal[] crystals;// west/mid/east
    private transient ShadowPortal portal;
    @Getter
    private boolean knockedOut;
    private long downCycle;
    @Getter
    private boolean sentDeath;
    private transient List<ShadowEnigma> enigmas;
    private int enigmaWaveIndex;
    @Getter
    private boolean nevermindChecks;
    private transient int talkIndex;
    private transient long talkCooldown;
    private transient String[] currentForceTalk;

    public static String[][] FirstEncounter = { { "YOU ARRIVE EXACTLY AS FORSEEN.", "INCAPABLE OF AFFECTING ANYTHING.", "A CATACLYSMIC INFECTION HAS ALREADY BEGUN.", "YOU WILL SUCCUMB TOO." }, { "YOU ARE JUST ANOTHER PUPPET.", "THE UNYIELDING DARKNESS IS INEVITABLE.", "YOUR ACTIONS WILL PURGE THE LIGHT.", "ALL OF YOUR CHOICES LEAD YOU DOWN THE SAME PATH." } };

    public static String[][] LosingControl = { { "Is...this...Aminishi?", "Am I...still...here?" }, { "Who are...you?", "Why...are...you..." }, { "What is...this?", "I..." } };

    public static String[][] RegainingControl = { { "THE BLACK CRYSTALS GIVE ME A REASON TO BE!", "LEAVE THIS TEMPLE. THE OUTCOME WILL NOT CHANGE." }, { "NO! I SERVE A PURPOSE GREATER THAN MY OWN BEING.", "I WILL PURIFY IT ALL.", "DO YOU REALLY THINK YOU CAN SAVE ME!?" }, { "ALL HISTORY. ALL THE PAST, PRESENT AND FUTURE.", "IT HAS ALREADY BEEN DECIDED.", "IT IS THE STONE THROWN INTO THE WATER. THE RIPPLES ARE INEVITABLE.", "THIS WORLD WILL SUCCUMB TOO." } };

    public static String[][] FreeingSeiryu = { { "The pools of anguish overflow...", "Solemn... Silent...", "All my fears converged inside one sempiternal moment.", "You were beautiful lights on the horizon...", "Blotted out by a dark storm.", "Thank you.", "I felt...others...sharing my torment.", "The exact same veiled confusion.", "One hides it well, though it pains her. The other.. so alone.", "Would...", "Would you help them too...?", "Be wary. Do not wander to your death." }, { "The pools of anguish overflow...", "Solemn... Silent...", "All my fears converged inside one sempiternal moment.", "You were beautiful lights on the horizon...", "Blotted out by a dark storm.", "Thank you.", "I felt...another...sharing my torment.", "The exact same veiled confusion.", "Under the obliterated crust of Forinthry...", "Would...", "Would you help them too...?", "Be wary. Do not wander to your death." } };

    public SeiryuTheAzureSerpent(int id, WorldTile tile, EliteDungeonHandledRoom room) {
        super(id, tile, room);
        blackHoles = new ArrayList<BlackHole>();
        setCantFollowUnderCombat(true);
        shadowTail = new WorldObject(111555, 11, 1, room.getTile(new WorldTile(4759, 8990, 1)));
        crystals = new Crystal[3];
        for (int i = 0; i < crystals.length; i++)
            crystals[i] = new Crystal(i, room, this);
        enigmas = new ArrayList<ShadowEnigma>();
        attackProgress = 0;
        enigmaWaveIndex = 0;
        nevermindChecks = false;
    }

    @Override
    public void spawn() {
        super.spawn();
        if (crystals == null) {
            crystals = new Crystal[3];
            for (int i = 0; i < crystals.length; i++) {
                crystals[i] = new Crystal(i, getRoom(), this);
                crystals[i].spawn();
            }
        } else {
            for (int i = 0; i < crystals.length; i++) {
                if (crystals[i] == null) {
                    crystals[i] = new Crystal(i, getRoom(), this);
                    crystals[i].spawn();
                } else {
                    try {
                        crystals[i].reset();
                        crystals[i].spawn();
                    } catch (Exception e) {
                        crystals[i].spawn();
                        crystals[i].reset();
                    }
                }
            }
        }
        blackHoles = new ArrayList<BlackHole>();
        setCantFollowUnderCombat(true);
        shadowTail = new WorldObject(111555, 11, 1, getRoom().getTile(new WorldTile(4759, 8990, 1)));
        enigmas = new ArrayList<ShadowEnigma>();
        attackProgress = 0;
        enigmaWaveIndex = 0;
        sentDeath = false;
        nevermindChecks = false;
        knockedOut = false;
        talkIndex = 0;
        talkCooldown = 0;
        currentForceTalk = null;
        getRoom().refreshNPCs();
    }

    @Override
    public void reset() {
        super.reset();
        if (portal != null) {
            portal.finish();
            portal = null;
        }
        if (crystals == null) {
            crystals = new Crystal[3];
            for (int i = 0; i < crystals.length; i++) {
                crystals[i] = new Crystal(i, getRoom(), this);
                crystals[i].spawn();
            }
        } else {
            for (int i = 0; i < crystals.length; i++) {
                if (crystals[i] == null) {
                    crystals[i] = new Crystal(i, getRoom(), this);
                    crystals[i].spawn();
                } else {
                    crystals[i].spawned = false;
                    try {
                        crystals[i].reset();
                        crystals[i].spawn();
                    } catch (Exception e) {
                        crystals[i].spawn();
                        crystals[i].reset();
                    }
                }
            }
        }
        setCantFollowUnderCombat(true);
        attackProgress = 0;
        enigmaWaveIndex = 0;
        nevermindChecks = false;
        talkIndex = 0;
        talkCooldown = 0;
        currentForceTalk = null;
        shadowTail.setId(111555);
        World.spawnObject(shadowTail);
        getRoom().refreshNPCs();
    }

    @Override
    public void processNPC() {
        if (currentForceTalk != null && talkCooldown != 0 && Utils.currentTimeMillis() > talkCooldown) {
            setNextForceTalk(new ForceTalk(currentForceTalk[talkIndex]));
            if (talkIndex + 1 >= currentForceTalk.length) {
                talkIndex = 0;
                talkCooldown = 0;
                currentForceTalk = new String[0];
            } else {
                talkIndex++;
                talkCooldown = Utils.currentTimeMillis() + 6000;
            }
        }
        if (isDead() || hasFinished() || sentDeath) {
            Room5 room = (Room5) getRoom();
            if (room == null || room.getRoom() == null || !room.getRoom().isLoaded())
                return;
            if (room.getBossBlock1().isSpawned()) {
                room.getBossBlock1().getLinkedNPCs().remove(this);
            }
            if (room.getBossBlock2().isSpawned()) {
                room.getBossBlock2().getLinkedNPCs().remove(this);
            }
            if (room.getBossBlock1().isSpawned() && room.isUnlocked(room.getBossBlock1()))
                room.getBossBlock1().remove();
            if (room.getBossBlock2().isSpawned() && room.isUnlocked(room.getBossBlock2()))
                room.getBossBlock2().remove();
            return;
        }
        super.processNPC();
        processBossBlock();
        processBlackHoles();
        processDownCycle();
    }

    public void processBossBlock() {
        if (getRoom() == null || !(getRoom() instanceof Room5))
            return;
        List<Entity> targets = getPossibleTargets();
        Room5 room = (Room5) getRoom();
        if (room.getBossBlock1().isSpawned()) {
            if (((nevermindChecks || getCombat().getTarget() == null) && targets.isEmpty()) || (getCombat().getTarget() != null && !isInsideFightArea(getCombat().getTarget()))) {
                if (!targets.isEmpty()) {
                    getCombat().setTarget(targets.get(Utils.random(targets.size())));
                    room.getBossBlock1().addNPC(this);
                    room.getBossBlock1().spawn(true);
                } else if (room.getBossBlock1().getLinkedNPCs().contains(this)) {
                    room.getBossBlock1().getLinkedNPCs().remove(this);
                    getCombat().removeTarget();
                    clearArea();
                    reset();
                    setNextRenderAnimation(-1);
                    setCantInteract(false);
                    setNextFaceEntity(null);
                    setNextFaceWorldTile(getRespawnTile().transform(2, getSize() + 5, 0));
                    setNextAnimation(new Animation(-1));
                    setNextGraphics(new Graphics(-1));
                    setNextWorldTile(getRespawnTile());
                    room.checkRemoveBlocks();
                }
            } else if (getCombat().getTarget() != null && isInsideFightArea(getCombat().getTarget())) {
                room.getBossBlock1().addNPC(this);
                room.getBossBlock1().spawn(true);
            }
            if (room.isUnlocked(room.getBossBlock1()))
                room.getBossBlock1().remove();
        } else {
            if (getCombat().getTarget() != null && isInsideFightArea(getCombat().getTarget())) {
                room.getBossBlock1().addNPC(this);
                room.getBossBlock1().spawn(true);
            }
        }
        if (room.getBossBlock2().isSpawned()) {
            if (((nevermindChecks || getCombat().getTarget() == null) && targets.isEmpty()) || (getCombat().getTarget() != null && !isInsideFightArea(getCombat().getTarget()))) {
                if (!targets.isEmpty()) {
                    getCombat().setTarget(targets.get(Utils.random(targets.size())));
                    room.getBossBlock2().addNPC(this);
                    room.getBossBlock2().spawn(true);
                } else if (room.getBossBlock2().getLinkedNPCs().contains(this)) {
                    room.getBossBlock2().getLinkedNPCs().remove(this);
                    getCombat().removeTarget();
                    clearArea();
                    reset();
                    setNextRenderAnimation(-1);
                    setCantInteract(false);
                    setNextFaceEntity(null);
                    setNextFaceWorldTile(getRespawnTile().transform(2, getSize() + 5, 0));
                    setNextAnimation(new Animation(-1));
                    setNextGraphics(new Graphics(-1));
                    setNextWorldTile(getRespawnTile());
                    room.checkRemoveBlocks();
                }
            } else if (getCombat().getTarget() != null && isInsideFightArea(getCombat().getTarget())) {
                room.getBossBlock2().addNPC(this);
                room.getBossBlock2().spawn(true);
            }
            if (room.isUnlocked(room.getBossBlock2()))
                room.getBossBlock2().remove();
        } else {
            if (getCombat().getTarget() != null && isInsideFightArea(getCombat().getTarget())) {
                room.getBossBlock2().addNPC(this);
                room.getBossBlock2().spawn(true);
            }
        }
    }

    @Override
    public void setNextForceTalk(ForceTalk nextForceTalk) {
        super.setNextForceTalk(nextForceTalk);
        if (nextForceTalk == null || getRoom() == null || getRoom().getRoom() == null)
            return;
        for (Player player : getRoom().getRoom().getPlayers()) {
            if (player == null || player.hasFinished() || player.isDead())
                continue;
            player.getPackets().sendGameMessage("<col=33CCFF>Seiryu The Azure Serpent </col><col=99FF99>" + nextForceTalk.getText() + "</col>");
        }
    }

    @Override
    public boolean checkAgressivity() {
        return false;
    }

    @Override
    public ArrayList<Entity> getPossibleTargets(boolean checkNPCs, boolean checkPlayers) {
        final ArrayList<Entity> possibleTarget = new ArrayList<Entity>();
        if (getRoom() == null)
            return possibleTarget;
        if (getCombat().getTarget() != null && isInsideFightArea(getCombat().getTarget()))
            possibleTarget.add(getCombat().getTarget());
        for (Player player : getRoom().getRoom().getPlayers()) {
            if (player == null || player.hasFinished() || player.isDead() || !Utils.isOnRange(this, player, 30) || player.getEliteDungeonsManager().isHidden() || possibleTarget.contains(player) || !isInsideFightArea(player))
                continue;
            possibleTarget.add(player);
        }
        return possibleTarget;

    }

    @Override
    public void handleIngoingHit(Hit hit) {
        super.handleIngoingHit(hit);
        if (getCombat().getTarget() == null) {
            List<Entity> targets = getPossibleTargets();
            if (!targets.isEmpty())
                getCombat().setTarget(targets.get(Utils.random(targets.size())));
        }
    }

    @Override
    public void processHit(Hit hit) {
        if (getHitpoints() - hit.getDamage() <= 720000) {
            hit.setDamage(getHitpoints() - 720000);
        }
        super.processHit(hit);
        handleKnockOut();
    }

    @Override
    public boolean restoreHitPoints() {
        if (getRoom() == null)
            return false;
        for (Player p : getRoom().getRoom().getPlayers()) {
            if (p == null || p.isDead() || p.hasFinished() || !(p.getControlerManager().getControler() instanceof EliteDungeonController))
                continue;
            EliteDungeonController c = (EliteDungeonController) p.getControlerManager().getControler();
            if (c.getCurrent() != this)
                continue;
            c.updateInterface();
        }
        return false;
    }

    @Override
    public ArrayList<Entity> getPossibleTargets() {
        return getPossibleTargets(false, true);
    }

    public boolean isInsideFightArea(WorldTile tile) {
        if (getRoom() == null)
            return false;
        if (isOnSeiryuNeck(tile))
            return true;
        WorldTile min = getRoom().getTile(new WorldTile(4724, 8979, 1));
        WorldTile max = getRoom().getTile(new WorldTile(4763, 9021, 1));
        WorldTile[] exceptions = { new WorldTile(4735, 9021, 1), new WorldTile(4734, 9020, 1), new WorldTile(4733, 9019, 1), new WorldTile(4733, 9020, 1), new WorldTile(4736, 9021, 1), new WorldTile(4751, 9022, 1), new WorldTile(4754, 9020, 1), new WorldTile(4753, 9020, 1), new WorldTile(4752, 9020, 1), new WorldTile(4735, 9021, 1), new WorldTile(4734, 9021, 1), new WorldTile(4733, 9021, 1), new WorldTile(4732, 9021, 1), new WorldTile(4751, 9021, 1), new WorldTile(4752, 9021, 1), new WorldTile(4754, 9021, 1), new WorldTile(4753, 9021, 1) };
        for (int i = 0; i < exceptions.length; i++) {
            if (getRoom().getTile(exceptions[i]).matches(tile))
                return false;
        }
        return tile.getX() >= min.getX() && tile.getY() >= min.getY() && tile.getX() <= max.getX() && tile.getY() <= max.getY();
    }

    private boolean isOnSeiryuNeck(WorldTile tile) {
        if (getRoom() == null)
            return false;
        WorldTile min = getRoom().getTile(new WorldTile(4732, 8985, 1));
        WorldTile max = getRoom().getTile(new WorldTile(4755, 8985, 1));
        return tile.getX() >= min.getX() && tile.getY() >= min.getY() && tile.getX() <= max.getX() && tile.getY() <= max.getY();
    }

    @Override
    public int getBossMapId() {
        return 40396;
    }

    @Override
    public void setNextFaceEntity(Entity entity) {
    }

    @Override
    public void setNextFaceWorldTile(WorldTile nextFaceWorldTile) {
        super.setNextFaceWorldTile(getRespawnTile().transform(2, getSize() + 5, 0));
    }

    public void sendShadowTailAttack() {
        for (Player p : this.getRoom().getRoom().getPlayers()) {
            if (p == null || p.hasFinished() || p.isDead())
                continue;
            p.getPackets().sendObjectAnimation(shadowTail, new Animation(31944));
        }
        World.sendGraphics(this, new Graphics(6979), getRoom().getTile(new WorldTile(4755, 8998, 1)));
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                if (isDead() || hasFinished() || stopAttack())
                    return;
                for (Entity e : getPossibleTargets()) {
                    if (e == null || e.isDead() || e.hasFinished())
                        continue;
                    addBlackHole(new WorldTile(e));
                }
            }

        }, 2);
    }

    public void processBlackHoles() {
        Iterator<BlackHole> iter = blackHoles.iterator();
        while (iter.hasNext()) {
            BlackHole blackHole = iter.next();
            if (blackHole != null)
                blackHole.process();
        }
    }

    public void addBlackHole(WorldTile tile) {
        BlackHole hole = new BlackHole(this, tile);
        blackHoles.add(hole);
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                if (isDead() || hasFinished() || blackHoles == null || !blackHoles.contains(hole) || stopAttack())
                    return;
                Iterator<BlackHole> iter = blackHoles.iterator();
                while (iter.hasNext()) {
                    if (iter.next() == hole)
                        iter.remove();
                }
            }

        }, 20);
    }

    @Override
    public void finish() {
        clearArea();
        super.finish();
    }

    public void clearArea() {
        if (portal != null) {
            portal.finish();
            portal = null;
        }
        if (blackHoles != null)
            blackHoles.clear();
        if (shadowTail != null) {
            shadowTail.setId(111554);
            World.spawnObject(shadowTail);
        }
        if (crystals != null) {
            for (int i = 0; i < crystals.length; i++)
                if (crystals[i] != null)
                    crystals[i].finish();
        }
        if (enigmas != null) {
            for (ShadowEnigma enigma : enigmas) {
                if (enigma == null || enigma.hasFinished() || enigma.isDead() || enigma.dead)
                    continue;
                enigma.sendDeath(null);
            }
            enigmas.clear();
        }
    }

    public void addShadowHand(WorldTile tile) {
        SeiryuTheAzureSerpent boss = this;
        World.sendGraphics(this, new Graphics(6974), tile);
        World.sendGraphics(this, new Graphics(6985), tile);
        WorldTasksManager.schedule(new WorldTask() {
            private int ticks;

            @Override
            public void run() {
                if (isDead() || hasFinished() || stopAttack() || ticks >= 2) {
                    stop();
                    return;
                }
                for (Entity e : getPossibleTargets()) {
                    if (e == null || e.isDead() || e.hasFinished() || !Utils.isOnRange(tile, e, 0, 1, 1))
                        continue;
                    e.applyHit(new Hit(boss, Utils.random(250, 451), HitLook.REGULAR_DAMAGE));
                }
                ticks++;
            }

        }, 0, 0);
    }

    public void handleKnockOut() {
        if (getHitpoints() > 720000 || isCantInteract())
            return;
        for (int i = 0; i < 3; i++)
            World.sendGraphics(this, new Graphics(7173, 0, 0, 4), getRoom().getTile(new WorldTile(4743, 8995 + (i * 7), 1)));
        nevermindChecks = true;
        setCantInteract(true);
        getTemporaryAttributtes().remove("cantDoAnimationOrGFX");
        setNextAnimation(new Animation(31936));
        final SeiryuTheAzureSerpent thisNPC = this;
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                if (isDead() || hasFinished() || sentDeath)
                    return;
                knockedOut = true;
                setNextRenderAnimation(4256);
                long totalTime = 90000;
                downCycle = Utils.currentTimeMillis() + totalTime;
                if (portal != null && portal.hasFinished())
                    portal.finish();
                portal = new ShadowPortal(totalTime, downCycle, thisNPC);
                portal.spawn();
                enigmaWaveIndex = 0;
                for (Crystal crystal : crystals) {
                    if (crystal == null || crystal.hasFinished() || crystal.dead)
                        continue;
                    crystal.setCantInteract(false);
                }
                talkIndex = 0;
                talkCooldown = Utils.currentTimeMillis() + 200;
                currentForceTalk = LosingControl[Utils.random(LosingControl.length)];
                getRoom().refreshNPCs();
            }

        }, 6);
    }

    public void processDownCycle() {
        if (downCycle == 0 || !knockedOut || !isCantInteract() || hasFinished() || isDead() || stopAttack())
            return;
        getRoom().refreshNPCs();
        for (Crystal crystal : crystals) {
            if (crystal == null || crystal.hasFinished())
                continue;
            crystal.setCantInteract(false);
        }
        long timePassed = downCycle - Utils.currentTimeMillis();
        if (timePassed <= 0) {// finish
            downCycle = 0;
            if (portal != null)
                portal.finish();
            getTemporaryAttributtes().remove("cantDoAnimationOrGFX");
            for (int i = 0; i < 3; i++)
                World.sendGraphics(this, new Graphics(7173, 0, 0, 4), getRoom().getTile(new WorldTile(4743, 8995 + (i * 7), 1)));
            setNextAnimation(new Animation(31940));
            knockedOut = false;
            for (Player player : getRoom().getRoom().getPlayers()) {
                if (player == null || player.hasFinished() || player.isDead() || !isOnSeiryuNeck(player))
                    continue;
                player.stopAll(false, false, true);
                player.resetWalkSteps();
                player.setNextWorldTile(getRoom().getTile(new WorldTile(4743, 9008, 1)));
                player.addFreezeDelay(2000, true);
            }
            WorldTasksManager.schedule(new WorldTask() {

                @Override
                public void run() {
                    if (isDead() || hasFinished() || stopAttack())
                        return;
                    for (Crystal crystal : crystals) {
                        if (crystal == null || crystal.hasFinished() || crystal.dead)
                            continue;
                        crystal.setCantInteract(true);
                        crystal.resetReceivedHits();
                    }
                    enigmaWaveIndex = 0;
                    setNextRenderAnimation(-1);
                    setCantInteract(false);
                    setHitpoints(getMaxHitpoints());
                    restoreHitPoints();
                    List<Entity> targets = getPossibleTargets();
                    if (!targets.isEmpty())
                        getCombat().setTarget(targets.get(Utils.random(targets.size())));
                    nevermindChecks = false;
                    talkIndex = 0;
                    talkCooldown = Utils.currentTimeMillis() + 200;
                    currentForceTalk = RegainingControl[Utils.random(RegainingControl.length)];
                    portal = null;
                }
            }, 6);
            return;
        }
        if ((timePassed <= 60000 && enigmaWaveIndex == 0) || (timePassed <= 20000 && enigmaWaveIndex == 1)) {
            enigmaWaveIndex++;
            sendShadowEnigmasWave();
        }
    }

    @Override
    public void sendDeath(Entity source) {
        if (sentDeath)
            return;
        if (isMarked()) {
            if (getRoom() != null && getRoom().getRoom() != null && getRoom().getRoom().getDungeon() != null)
                getRoom().getRoom().getDungeon().removeMark();
            setMarked(false);
        }
        for (Crystal crystal : crystals)
            if (crystal != null && !crystal.dead)
                return;
        sentDeath = true;
        getTemporaryAttributtes().remove("cantDoAnimationOrGFX");
        setNextAnimation(null);
        setNextGraphics(null);
        clearArea();
        setNextNPCTransformation(25594);
        final SeiryuTheAzureSerpent thisNPC = this;
        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                if (loop == 0) {
                    setNextAnimation(new Animation(31940));
                    setNextGraphics(new Graphics(-1));
                } else if (loop >= 6) {
                    setNextRenderAnimation(-1);
                    if (source instanceof Player) {
                        Player plr = (Player) source;
                        plr.getControlerManager().processNPCDeath(thisNPC);
                    }
                    talkIndex = 0;
                    talkCooldown = Utils.currentTimeMillis() + 200;
                    currentForceTalk = FreeingSeiryu[Utils.random(FreeingSeiryu.length)];
                    setCantInteract(false);
                    getRoom().handleDeath(thisNPC);
                    setNextNPCTransformation(25595);
                    getRoom().refreshNPCs();
                    drop(source instanceof Player ? (Player) source : null);
                    stop();
                }
                loop++;
            }
        }, 0, 1);
    }

    public void applyDeath() {
        if (sentDeath)
            return;
        sentDeath = true;
        setNextAnimation(new Animation(-1));
        setNextNPCTransformation(25595);
        clearArea();
        getRoom().refreshNPCs();
        nevermindChecks = false;
    }

    public void sendShadowEnigmasWave() {
        final SeiryuTheAzureSerpent thisNPC = this;
        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                if (isDead() || hasFinished() || stopAttack() || loop >= 3) {
                    stop();
                    return;
                }
                for (int i = 0; i < 2; i++) {
                    ShadowEnigma enigma = new ShadowEnigma(getRoom().getTile(new WorldTile(4742 + (i * 2), 9012, 1)), thisNPC);
                    enigma.spawn();
                    enigmas.add(enigma);
                }
                if (thisNPC.getRoom() != null)
                    thisNPC.getRoom().refreshNPCs();
                loop++;
            }

        }, 0, 3);
    }

    public void healCrystals(WorldTile from) {
        Crystal crystal = crystals[Utils.random(crystals.length)];
        int lowestHealth = Integer.MAX_VALUE;
        for (int i = 0; i < crystals.length; i++) {
            if (crystals[i] == null || crystals[i].isDead() || crystals[i].hasFinished() || crystals[i].dead || (crystal != null && crystals[i].getHitpoints() >= crystals[i].getMaxHitpoints()))
                continue;
            if (crystals[i].getHitpoints() < lowestHealth) {
                crystal = crystals[i];
                lowestHealth = crystals[i].getHitpoints();
            }
        }
        boolean allDead = true;
        for (int i = 0; i < crystals.length; i++) {
            if (crystals[i] == null || crystals[i].isDead() || crystals[i].hasFinished() || crystals[i].dead)
                continue;
            allDead = false;
            break;
        }
        if (crystal == null || allDead)
            return;
        Projectile projectile = World.sendProjectileCycles(from, crystal, 6981, 5, 60, 10, 300, 30, 0);
        long projectileCycles = Utils.projectileTimeToMiliseconds(projectile);
        final Crystal c = crystal;
        CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
            @Override
            public boolean repeat() {
                try {
                    if (isDead() || hasFinished() || stopAttack() || c == null || c.hasFinished() || c.isDead() || c.dead || c.getHitpoints() >= c.getMaxHitpoints())
                        return false;
                    c.heal(5000, 0, 0, true);
                    return false;
                } catch (Exception e) {
                    Logger.getGlobal().catching(e);
                    return false;
                }
            }
        }, projectileCycles, 600, TimeUnit.MILLISECONDS);
    }

    public boolean stopAttack() {
        Entity target = getCombat().getTarget();
        if (nevermindChecks)
            return false;
        return (target == null && getPossibleTargets().isEmpty()) || (target != null && !isInsideFightArea(target) && getPossibleTargets().isEmpty()) || sentDeath;
    }

    public static class ShadowEnigma extends EliteDungeonNPC {

        private static final long serialVersionUID = 3123248836620660978L;

        private final SeiryuTheAzureSerpent boss;
        private long walkDelay, resetWalkDelay;
        private boolean dead;

        public ShadowEnigma(WorldTile tile, SeiryuTheAzureSerpent boss) {
            super(25602, tile, boss.getRoom());
            this.setIntelligentRouteFinder(true);
            this.boss = boss;
            setRun(false);
            walkDelay = Utils.currentTimeMillis();

        }

        @Override
        public void spawn() {
            super.spawn();
            setIntelligentRouteFinder(true);
            setRun(false);
            walkDelay = Utils.currentTimeMillis();
        }

        @Override
        public boolean canWalkNPC(int toX, int toY, boolean checkUnder) {
            return true;
        }

        @Override
        public void processNPC() {
            if (isDead() || hasFinished() || boss == null || boss.sentDeath || boss.isDead() || boss.hasFinished()) {
                if (boss == null || boss.sentDeath || boss.isDead() || boss.hasFinished())
                    finish();
                return;
            }
            if (forceWalk != null && (resetWalkDelay == 0 || Utils.currentTimeMillis() > resetWalkDelay)) {
                resetWalkSteps();
                resetWalkDelay = Utils.currentTimeMillis() + 900;
                return;
            }
            if (!getCombat().process()) {
                if (!checkAgressivity()) {
                    if (isForceWalking()) {
                        if (getFreezeDelay() < Utils.currentTimeMillis()) {
                            if (getX() != getForceWalk().getX() || getY() != getForceWalk().getY()) {
                                if (!hasWalkSteps()) {
                                    int steps = RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER, getX(), getY(), getPlane(), getSize(), new FixedTileStrategy(forceWalk.getX(), forceWalk.getY()), true);
                                    int[] bufferX = RouteFinder.getLastPathBufferX();
                                    int[] bufferY = RouteFinder.getLastPathBufferY();
                                    for (int i = steps - 1; i >= 0; i--) {
                                        if (!addWalkSteps(bufferX[i], bufferY[i], 25, true)) {
                                            break;
                                        }
                                    }
                                }
                                if (!hasWalkSteps()) {
                                    setNextWorldTile(new WorldTile(getForceWalk()));
                                    forceWalk = null;
                                }
                            } else
                                forceWalk = null;
                        }
                        return;
                    }
                    if (walkDelay == 0 || Utils.currentTimeMillis() >= walkDelay) {
                        if (forceWalk == null) {
                            WorldTile toTile = getRespawnTile().transform(0, -14, 0);
                            if (!matches(toTile)) {
                                setForceWalk(toTile);
                            } else {
                                sendDeath(boss);
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void handleIngoingHit(Hit hit) {
            super.handleIngoingHit(hit);
            if (hit.getSource() != null && hit.getSource() instanceof Player) {
                forceWalk = null;
                resetWalkSteps();
                getCombat().setTarget(hit.getSource());
            }
        }

        @Override
        public boolean checkAgressivity() {
            return false;
        }

        @Override
        public ArrayList<Entity> getPossibleTargets(boolean checkNPCs, boolean checkPlayers) {
            if (boss != null)
                return boss.getPossibleTargets(checkNPCs, checkPlayers);
            return super.getPossibleTargets(checkNPCs, checkPlayers);
        }

        @Override
        public ArrayList<Entity> getPossibleTargets() {
            if (boss != null)
                return boss.getPossibleTargets();
            return super.getPossibleTargets();
        }

        @Override
        public void sendDeath(Entity source) {
            if (dead)
                return;
            if (isMarked()) {
                if (getRoom() != null && getRoom().getRoom() != null && getRoom().getRoom().getDungeon() != null)
                    getRoom().getRoom().getDungeon().removeMark();
                setMarked(false);
            }
            dead = true;
            final NPCCombatDefinition defs = getCombatDefinitions();
            resetWalkSteps();
            getCombat().removeTarget();
            setNextAnimation(null);
            WorldTasksManager.schedule(new WorldTask() {
                int loop;

                @Override
                public void run() {
                    if (loop == 0) {
                        setNextAnimation(new Animation(defs.getDeathEmote()));
                    } else if (loop >= defs.getDeathDelay()) {
                        if (source == boss && boss != null && !boss.hasFinished() && !boss.stopAttack() && !boss.isDead())
                            boss.healCrystals(getRespawnTile().transform(0, -14, 0));
                        reset();
                        setLocation(getRespawnTile());
                        finish();
                        stop();
                    }
                    loop++;
                }
            }, 0, 1);
        }

    }

    public static class BlackHole {
        private final SeiryuTheAzureSerpent boss;
        private final WorldTile tile;
        private long cycle;
        private int damage;

        public BlackHole(SeiryuTheAzureSerpent boss, WorldTile tile) {
            this.boss = boss;
            this.tile = tile;
            damage = 180;
            cycle = -1;
            World.sendGraphics(boss, new Graphics(6977), tile);
            WorldTasksManager.schedule(new WorldTask() {
                boolean started;

                @Override
                public void run() {
                    try {
                        if (boss == null || boss.isDead() || boss.hasFinished()) {
                            stop();
                            return;
                        }
                        if (!started) {
                            started = true;
                            World.sendGraphics(boss, new Graphics(6978), tile);
                        } else {
                            cycle = 0;
                            World.sendGraphics(boss, new Graphics(4661), tile);
                            stop();
                            return;
                        }
                    } catch (Exception e) {
                        stop();
                        return;
                    }
                }
            }, 2, 0);
        }

        public void process() {
            if (boss == null || boss.isDead() || boss.hasFinished() || cycle == -1)
                return;
            if (cycle == 0 || Utils.currentTimeMillis() > cycle) {
                for (Entity e : boss.getPossibleTargets()) {
                    if (e == null || e.isDead() || e.hasFinished() || !e.matches(tile))
                        continue;
                    e.applyHit(new Hit(boss, damage, HitLook.REGULAR_DAMAGE));
                }
                damage += Utils.random(15, 26);
                if (damage >= 400)
                    damage = 400;
                cycle = Utils.currentTimeMillis() + 600;
            }
        }

    }

    public static class Crystal extends EliteDungeonNPC {

        private static final long serialVersionUID = -6329716886421905788L;

        public static final WorldTile[] objectTiles = { new WorldTile(4743, 8985, 1), new WorldTile(4739, 8985, 1), new WorldTile(4747, 8985, 1) };

        private WorldObject object;
        private final SeiryuTheAzureSerpent boss;
        private boolean dead;
        private int maxHitPoints = 10000;

        public Crystal(int index, EliteDungeonHandledRoom room, SeiryuTheAzureSerpent boss) {
            super(25598 + index, room.getTile(objectTiles[index]), room);
            setCantInteract(true);
            this.boss = boss;
            object = new WorldObject(111557 + index, 10, index == 2 ? 0 : 2, room.getTile(objectTiles[index]));
            World.spawnObject(object);
            World.getRegion(object.getRegionId(), true);
            setCantFollowUnderCombat(true);
            maxHitPoints = 10000 + (5000 * room.getRoom().getDungeon().getPartySize());
            setHitpoints(getMaxHitpoints());
        }

        @Override
        public void spawn() {
            super.spawn();
            setCantInteract(true);
            int index = id - 25598;
            object = new WorldObject(111557 + index, 10, index == 2 ? 0 : 2, getRoom().getTile(objectTiles[index]));
            World.spawnObject(object);
            World.getRegion(object.getRegionId(), true);
            setHitpoints(10000 + (5000 * boss.getRoom().getRoom().getDungeon().getPartySize()));
            setCantFollowUnderCombat(true);
        }

        @Override
        public void processNPC() {
            if (boss != null) {
                if (boss.crystals == null)
                    finish();
                else {
                    boolean anyOfThem = false;
                    for (Crystal crystal : boss.crystals) {
                        if (crystal == this) {
                            anyOfThem = true;
                            break;
                        }
                    }
                    if (!anyOfThem)
                        finish();
                }
            }
            if (isDead() || hasFinished() || boss == null || boss.isDead() || boss.hasFinished() || boss.stopAttack())
                return;
            if (World.getObjectWithId(object, object.getId()) == null)
                World.spawnObject(object);
//            addHitBars();
        }

        @Override
        public void setNextAnimationForce(Animation nextAnimation) {
        }

        @Override
        public void setNextAnimation(Animation animation) {
        }

        @Override
        public void setNextAnimationNoPriority(Animation nextAnimation) {
        }

        @Override
        public void setNextFaceEntity(Entity entity) {
        }

        @Override
        public void setNextFaceWorldTile(WorldTile nextFaceWorldTile) {
        }

        @Override
        public int getMaxHitpoints() {
            return maxHitPoints;
        }

        @Override
        public void finish() {
            object.setId(-1);
            World.spawnObject(object);
            super.finish();
        }

        @Override
        public void sendDeath(Entity source) {
            if (dead)
                return;
            if (isMarked()) {
                if (getRoom() != null && getRoom().getRoom() != null && getRoom().getRoom().getDungeon() != null)
                    getRoom().getRoom().getDungeon().removeMark();
                setMarked(false);
            }
            dead = true;
            boss.sendDeath(source);
            int index = id - 25598;
            setNextGraphics(new Graphics(6970 + index));
            reset();
            setLocation(getRespawnTile());
            finish();
        }

        @Override
        public boolean restoreHitPoints() {
            return false;
        }

    }

    public static class ShadowPortal extends EliteDungeonNPC {

        private static final long serialVersionUID = 512628053647721048L;
        private final SeiryuTheAzureSerpent boss;
        private final PortalHitBar bar;

        public ShadowPortal(long totalTime, long timeToRemove, SeiryuTheAzureSerpent boss) {
            super(25601, boss.getRoom().getTile(new WorldTile(4743, 9014, 1)), boss.getRoom());
            this.boss = boss;
            bar = new PortalHitBar(totalTime, timeToRemove);
            setCantInteract(true);
            setCantFollowUnderCombat(true);
        }

        @Override
        public void spawn() {
            super.spawn();
            setCantInteract(true);
            setCantFollowUnderCombat(true);
        }

        @Override
        public void processNPC() {
            if (isDead() || hasFinished() || boss == null || boss.isDead() || boss.hasFinished() || boss.sentDeath || boss.stopAttack()) {
                finish();
                return;
            }
            if (bar != null && !getNextHitBars().contains(bar))
                getNextHitBars().add(bar);
        }

        @Override
        public void setNextAnimationForce(Animation nextAnimation) {
        }

        @Override
        public void setNextAnimation(Animation animation) {
        }

        @Override
        public void setNextAnimationNoPriority(Animation nextAnimation) {
        }

        @Override
        public void setNextFaceEntity(Entity entity) {
        }

        @Override
        public void setNextFaceWorldTile(WorldTile nextFaceWorldTile) {
        }

        @Override
        public void finish() {
            super.finish();
        }

        public static class PortalHitBar extends HitBar {
            private long totalTime = 0;
            private final long timeToRemove;

            public PortalHitBar(long totalTime, long timeToRemove) {
                this.totalTime = totalTime;
                this.timeToRemove = timeToRemove;
            }

            @Override
            public int getType() {
                return 9;
            }

            @Override
            public int getPercentage() {
                if (Utils.currentTimeMillis() > timeToRemove)
                    return 0;
                long timeRemaining = timeToRemove - Utils.currentTimeMillis();
                int percentage = (int) (((((double) timeRemaining / (double) totalTime) * 100) * 255) / 100);
                return percentage;
            }

            @Override
            public boolean display(Player player) {
                return getPercentage() != 0;
            }

        }

    }

    public enum SeiryuTheAzureSerpentAttacks {
        ATTACK() {
            public int sendAttack(SeiryuTheAzureSerpent boss, Player target) {
                SeiryuTheAzureSerpentAttacks[] attacks = { AUTO_ATTACK, AUTO_ATTACK, AUTO_ATTACK, AUTO_ATTACK, HUGE_BREATH, AUTO_ATTACK, AUTO_ATTACK, SHADOW_ARROW, AUTO_ATTACK, AUTO_ATTACK, HUGE_BREATH, AUTO_ATTACK, AUTO_ATTACK, SHADOW_HANDS, AUTO_ATTACK, AUTO_ATTACK, HUGE_BREATH, SHADOW_HANDS, AUTO_ATTACK, AUTO_ATTACK, SHADOW_ARROW, AUTO_ATTACK, SHADOW_HANDS, AUTO_ATTACK, HUGE_BREATH, AUTO_ATTACK, SHADOW_HANDS, AUTO_ATTACK };
                int attackProgress = boss.attackProgress;
                SeiryuTheAzureSerpentAttacks attack = attacks[attackProgress];
                boss.attackProgress = boss.attackProgress + 1 >= attacks.length ? 5 : boss.attackProgress + 1;
                if (boss.currentForceTalk == null) {
                    boss.talkIndex = 0;
                    boss.talkCooldown = Utils.currentTimeMillis() + 200;
                    boss.currentForceTalk = FirstEncounter[Utils.random(FirstEncounter.length)];
                }
                return attack.sendAttack(boss, target);
            }
        },

        AUTO_ATTACK() {
            public int sendAttack(SeiryuTheAzureSerpent boss, Player target) {
                SeiryuTheAzureSerpentAttacks attack = Utils.random(2) == 0 ? REGULAR_ATTACK : SHADOW_TAIL;
                return attack.sendAttack(boss, target);
            }
        },

        REGULAR_ATTACK() {
            public int sendAttack(SeiryuTheAzureSerpent boss, Player target) {
                boss.setNextAnimation(new Animation(31935));
                WorldTile hitTile = boss.getRoom().getTile(new WorldTile(4743, 9008, 1));
                int fromHeight = 150;
                int startTime = 30;
                World.sendProjectileCycles(boss.transform(-1, 0, 0), hitTile, 6986, fromHeight, 0, startTime, 80, 30, 0);
                World.sendProjectileCycles(boss.transform(4, 0, 0), hitTile, 6986, fromHeight, 0, startTime, 80, 30, 0);
                CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {

                    @Override
                    public boolean repeat() {
                        try {
                            if (boss.isDead() || boss.hasFinished() || boss.stopAttack())
                                return false;
                            for (Entity e : boss.getPossibleTargets()) {
                                if (e == null || e.isDead() || e.hasFinished())
                                    continue;
                                Projectile projectile = World.sendProjectileCycles(hitTile, e, 6986, 0, 35, 0, 60, 30, 0);
                                long projectileCycles = Utils.projectileTimeToMiliseconds(projectile);
                                CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
                                    @Override
                                    public boolean repeat() {
                                        try {
                                            if (boss.isDead() || boss.hasFinished() || boss.stopAttack() || e == null || e.hasFinished() || e.isDead())
                                                return false;
                                            int damage = CombatScript.getMaxHit(boss, boss.getMaxHit(), NPCCombatDefinitionConstants.MAGE, e);
                                            CombatScript.delayHit(boss, 0, e, CombatScript.getMagicHit(boss, damage));
                                            return false;
                                        } catch (Exception e) {
                                            Logger.getGlobal().catching(e);
                                            return false;
                                        }
                                    }
                                }, projectileCycles, 600, TimeUnit.MILLISECONDS);
                            }
                            World.sendProjectileCycles(hitTile, hitTile.transform(-3, 10, 0), 6987, 0, 0, 0, 60, 30, 0);
                            World.sendProjectileCycles(hitTile, hitTile.transform(3, 10, 0), 6987, 0, 0, 0, 60, 30, 0);
                        } catch (Exception e) {
                            Logger.getGlobal().catching(e);
                            return false;
                        }
                        return false;
                    }
                }, 1500, 1, TimeUnit.MILLISECONDS);
                return boss.getAttackSpeed();
            }
        },

        HUGE_BREATH() {
            public int sendAttack(SeiryuTheAzureSerpent boss, Player target) {
                for (Entity e : boss.getPossibleTargets()) {
                    if (e == null || e.isDead() || e.hasFinished() || !(e instanceof Player))
                        continue;
                    ((Player) e).getPackets().sendExecuteScript(1211, "Seiryu takes a huge breath.", 0, -120, 1);
                    ((Player) e).getPackets().sendGameMessage("<col=ff0000>Seiryu takes a huge breath.");
                }
                boss.setNextAnimation(new Animation(31934));
                boss.setNextGraphics(new Graphics(6967));
                boss.getTemporaryAttributtes().put("cantDoAnimationOrGFX", Boolean.TRUE);
                WorldTasksManager.schedule(new WorldTask() {
                    boolean sentAttack;

                    @Override
                    public void run() {
                        if (boss.isDead() || boss.hasFinished() || boss.stopAttack()) {
                            stop();
                            return;
                        }
                        if (!sentAttack) {
                            for (Entity e : boss.getPossibleTargets()) {
                                if (e == null || e.isDead() || e.hasFinished() || Utils.isOnRange(boss, e, 1))
                                    continue;
                                int damage = CombatScript.getRandomMaxHit(boss, 800, NPCCombatDefinitionConstants.MAGE, e, 1, Utils.random(4) == 0);
                                CombatScript.delayHit(boss, 0, e, CombatScript.getMagicHit(boss, damage));
                            }
                            sentAttack = true;
                        } else {
                            boss.getTemporaryAttributtes().remove("cantDoAnimationOrGFX");
                            boss.setNextGraphics(new Graphics(-1));
                            boss.setNextAnimation(new Animation(-1));
                            stop();
                        }
                    }
                }, 6, 1);
                return 10;
            }
        },
        SHADOW_TAIL() {
            public int sendAttack(SeiryuTheAzureSerpent boss, Player target) {
                boss.setNextAnimation(new Animation(31935));
                boss.sendShadowTailAttack();
                return 4;
            }
        },
        SHADOW_ARROW() {
            public int sendAttack(SeiryuTheAzureSerpent boss, Player target) {
                boss.setNextAnimation(new Animation(31935));
                WorldTile min = boss.getRespawnTile().transform(-8, 4, 0);
                WorldTile max = boss.getRespawnTile().transform(8, 16, 0);
                WorldTile tile = new WorldTile(Utils.random(min.getX(), max.getX() + 1), Utils.random(min.getY(), max.getY() + 1), boss.getPlane());
                for (int i = 0; i < 5; i++)
                    World.sendProjectileCycles(boss, tile, 6981, 30 + (i * 25), 0, 0, 180, 0, 0);
                for (Entity e : boss.getPossibleTargets()) {
                    if (e == null || e.isDead() || e.hasFinished() || !(e instanceof Player))
                        continue;
                    ((Player) e).getHintIconsManager().addHintIcon(tile.getX(), tile.getY(), tile.getPlane(), 0, 0, 0, -1, false);
                    ((Player) e).getPackets().sendExecuteScript(1211, "Shadow energy bursts forth from Seiryu.", 0, -120, 1);
                    ((Player) e).getPackets().sendGameMessage("<col=ff0000>Shadow energy bursts forth from Seiryu.");
                }
                CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {

                    @Override
                    public boolean repeat() {
                        try {
                            if (boss.isDead() || boss.hasFinished() || boss.stopAttack())
                                return false;
                            World.sendGraphics(boss, new Graphics(6975), tile);
                            for (Entity e : boss.getPossibleTargets()) {
                                if (e == null || e.isDead() || e.hasFinished() || !(e instanceof Player))
                                    continue;
                                ((Player) e).getHintIconsManager().removeUnsavedHintIcon();
                                boolean matches = e.matches(tile);
                                WorldTasksManager.schedule(new WorldTask() {
                                    WorldTile currentTile = tile;
                                    WorldTile toTile = null;
                                    int steps = 0;

                                    @Override
                                    public void run() {
                                        if (boss.isDead() || boss.hasFinished() || boss.stopAttack()) {
                                            stop();
                                            return;
                                        }
                                        if (steps >= 8 || (toTile != null && currentTile != null && currentTile.matches(toTile))) {
                                            stop();
                                            return;
                                        }
                                        if (toTile == null) {
                                            World.sendGraphics(boss, new Graphics(6976), tile);
                                            if (matches) {
                                                stop();
                                                return;
                                            }
                                            toTile = new WorldTile(e);
                                            return;
                                        }
                                        int v = Utils.getAngle(toTile.getX() - currentTile.getX(), toTile.getY() - currentTile.getY());
                                        byte[] dir = Utils.getDirection(v);
                                        currentTile = currentTile.transform(dir[0], dir[1], 0);
                                        boss.addShadowHand(currentTile);
                                        steps++;
                                    }
                                }, 3, 0);
                            }
                        } catch (Exception e) {
                            Logger.getGlobal().catching(e);
                            return false;
                        }
                        return false;
                    }
                }, 3600, 1, TimeUnit.MILLISECONDS);
                return 7;
            }
        },
        SHADOW_HANDS() {
            public int sendAttack(SeiryuTheAzureSerpent boss, Player target) {
                boss.setNextAnimation(new Animation(31935));
                WorldTile[] locations = { new WorldTile(4736, 8995, 1), new WorldTile(4740, 8995, 1), new WorldTile(4746, 8995, 1), new WorldTile(4750, 8995, 1) };
                WorldTile tile = boss.getRoom().getTile(locations[Utils.random(locations.length)]);
                WorldTasksManager.schedule(new WorldTask() {
                    final List<WorldTile> hands = new ArrayList<WorldTile>();
                    final HashMap<WorldTile, Long> cooldowns = new HashMap<WorldTile, Long>();
                    int currentY;
                    int width;

                    @Override
                    public void run() {
                        if (currentY >= 50 || boss.isDead() || boss.hasFinished() || boss.stopAttack()) {
                            stop();
                            return;
                        }
                        if (currentY % 4 != 0)
                            for (int i = 0; i < hands.size(); i++) {
                                if (hands.get(i) == null)
                                    continue;
                                WorldTile newTile = hands.get(i).transform(0, 1, 0);
                                hands.set(i, newTile);
                            }
                        if (currentY <= 15) {
                            if (width == 0)
                                hands.add(tile.transform(0, 0, 0));
                            else
                                for (int i = 0; i < width; i++) {
                                    for (int j = 0; j < 2; j++) {
                                        WorldTile hand = tile.transform(j == 0 ? width : -width, 0, 0);
                                        if (hands.contains(hand))
                                            continue;
                                        hands.add(hand);
                                    }
                                }
                        }
                        currentY++;
                        if (currentY % 4 == 0) {
                            width = width + 1 >= 4 ? 4 : width + 1;
                        }
                        for (WorldTile h : hands) {
                            if (h == null || !World.canMoveNPC(h, 1) || !boss.isInsideFightArea(h))
                                continue;
                            for (Entity e : boss.getPossibleTargets()) {
                                if (e == null || e.isDead() || e.hasFinished() || !e.matches(h))
                                    continue;
                                e.applyHit(new Hit(boss, Utils.random(250, 451), HitLook.REGULAR_DAMAGE));
                            }
                            long cooldown = getCooldown(h);
                            if (cooldown == 0 || Utils.currentTimeMillis() > cooldown) {
                                World.sendGraphics(boss, new Graphics(6974), new WorldTile(h));
                                World.sendGraphics(boss, new Graphics(6985, 0, 0, 2), new WorldTile(h));
//                                World.sendGraphics(boss, new Graphics(7181), new WorldTile(h));
                                cooldowns.put(h, Utils.currentTimeMillis() + 4200L);
                            }
                        }
                    }

                    public long getCooldown(WorldTile tile) {
                        for (Entry<WorldTile, Long> e : cooldowns.entrySet()) {
                            if (e.getKey().matches(tile))
                                return e.getValue();
                        }
                        return 0;
                    }

                }, 0, 0);
                return 4;
            }
        };
        public int sendAttack(SeiryuTheAzureSerpent boss, Player target) {
            return 0;
        }
    }

    @Override
    public void drop(Player killer) {
        String name = getName().toLowerCase();
        if (name == null)
            return;
        List<Player> players = getDropPlayers();
        if (killer != null && !players.contains(killer))
            players.add(killer);
        for (Player player : players) {
            if (player == null || player.hasFinished() || player.isDead())
                continue;
            int currentId = getId();
            ContractHandler.updateContract(player, this);
            HybridTokenDistributor.rollForToken(player, HybridTokenDistributor.Activity.TEMPLE_OF_AMINISHI);
            setId(currentId);
            int extra = player.getPerkManager().hasPerkActive(DonationPerk.DUNGEONS_MASTER) ? 1250 : 0;
            player.getDungeoneeringManager().setTokens(player.getDungeoneeringManager().getTokens() + 5000 + extra);
            player.getPackets().sendGameMessage(!player.getPerkManager().hasPerkActive(DonationPerk.DUNGEONS_MASTER) ? "You received 5,000 dungeoneering tokens." : "You received 6,250 dungeoneering tokens, An extra 1250 tokens for having the dungeons master perk.");
            player.getInventionManager().processScavengingPerk();
            increaseKillStatistics(player, getName());
            handleRingOfDeath(player);
            if (Math.random() <= 0.001) {
                sendDrop(player, new NPCDrop(43050, 0.001, 1, 1), false, false);
                World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a " + ItemDefinitions.getItemDefinitions(43050).getName() + " drop.", false);
                HcimNewsManager.getInstance().addNews(player, "<#player> got a " + ItemDefinitions.getItemDefinitions(43050).getName() + " drop", 2);
                        player.getAchievements().updateProgress(1, AchievementList.OBTAIN_BOSS_PET);
                QueryExecutor.submit(new News(player, "<b> " + player.getDisplayName() + " received a " + ItemDefinitions.getItemDefinitions(43050).getName() + " drop."));
            } else
                sendDrop(player, Math.random() <= 0.5 ? new NPCDrop(43164, 0.05, 1, Math.random() <= 0.05 ? 110 : 50) : new NPCDrop(42954, 40, 5, 34), false);
            if (getRoom() != null && getRoom().getRoom() != null && getRoom().getRoom().getDungeon() != null && getRoom().getRoom().getDungeon().getPartySize() == 1)
                sendDrop(player, new NPCDrop(43164, 0.05, 4, 8), false);
            final NPCDrop[] possibleDrops = EliteDungeonsConstants.mobDrops;
            for (final NPCDrop drop : possibleDrops) {
                if (drop == null || drop.getRate() < 100)
                    continue;
                if (!Ectoplasmator.scatterAshes(player, this, drop.getItemId()))
                    sendDrop(player, drop, false);
            }
            int amountCharms = player.getInventory().getAmountOf(43066);
            NPCDrop luckyCharm = Math.random() <= (amountCharms == 0 ? 0.005 : 0.04) ? NPCDrop.selectRandomNPCDrop(EliteDungeonsConstants.luckyCharmDrops) : null;
            NPCDrop mobDrop = luckyCharm != null ? luckyCharm : NPCDrop.selectRandomNPCDrop(possibleDrops);
            if (luckyCharm != null && amountCharms > 0)
                player.getInventory().deleteItem(43066, 1);
            sendDrop(player, mobDrop, false);
            if (Math.random() <= 0.005)
                sendDrop(player, new NPCDrop(18778, 5, 1, 1), false);
        }
    }

    @Override
    protected void sendDrop(Player player, NPCDrop drop, boolean lootbeam) {
        sendDrop(player, drop, lootbeam, true);
    }

    public void sendDrop(Player player, NPCDrop drop, boolean lootbeam, boolean canBeDoubled) {
        WorldTile tile = getRespawnTile();
        final Item item = new Item(drop.getItemId());
        if (player.getInventory().containsItem(19675, 1)) {
            if (Herbicide.handleDrop(player, item)) {
                return;
            }
        }
        if (player.getInventory().containsItem(18337, 1)) {
            if (Bonecrusher.handleDrop(player, item)) {
                return;
            }
        }
        if (!(drop.getItemId() == 995 && CoinAccumulator.handleCoinAccumulator(player, this, drop.getMinAmount() + Utils.getRandom(drop.getExtraAmount())))) {
            int id = drop.getItemId();
            int amount = drop.getMinAmount() + Utils.getRandom(drop.getExtraAmount());
            if (canBeDoubled && Settings.DOUBLE_DROPS) {
                amount *= 2;
            }
            if (canBeDoubled && (player.getEquipment().getRingId() == 773 || player.getEquipment().getRingId() == 41069 || player.getEquipment().getRingId() == 48483)) {
                if (Utils.randomDouble() <= Settings.PERFECT_RING_DROP_DUPLICATION_RATE) {
                    amount *= 2;
                    player.sendMessage(Colors.DCYAN + "<shad=000000>Your drop has been doubled thanks to your ring!");
                }
            }
            if (canBeDoubled && player.getCurrentPet() != null) {
                if (player.getCurrentPet().getPerks().contains(PetPerk.DOUBLE_TROUBLE)) {
                    if (Utils.random(100) <= PetPerkUtils.getConModifierForPerk(player, PetPerk.DOUBLE_TROUBLE) * 10) {
                        player.sendMessage(Colors.DCYAN + "<shad=000000>Your drop suddenly vanishes because of your pet perk!");
                        return;
                    } else if (Utils.random(100) <= PetPerkUtils.getProModifierForPerk(player, PetPerk.DOUBLE_TROUBLE) * 10) {
                        amount *= 2;
                        player.sendMessage(Colors.DCYAN + "<shad=000000>Your drop has been doubled thanks to your pet perk!");
                    }
                }
            }
            if (canBeDoubled && !player.getEliteDungeonsManager().isAutoLoot() && Math.random() <= 0.05) {
                amount *= 2;
                player.getPackets().sendGameMessage("You received 2x " + item.getName() + ".");
            }
            val loot = new Item(id, amount);
            if (!player.getEliteDungeonsManager().isAutoLoot() || !player.getEliteDungeonsManager().addReward(loot)) {
                if (item.getAmount() > 1 && !item.getDefinitions().isStackable() && !item.getDefinitions().isNoted()) {
                    for (int i = 0; i < item.getAmount(); i++)
                        World.updateGroundItem(new Item(loot.getId(), 1), tile, player, 60, 0, false);
                } else
                    World.updateGroundItem(loot, tile, player, 60, 0, false);
            }
        }
        player.getDropCollectionHandler().handleBossKills(new Item(drop.getItemId()), id);
    }

}
