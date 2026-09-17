package com.rs.game;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.rs.Settings;
import com.rs.cache.loaders.AnimationDefinitions;
import com.rs.cache.loaders.ObjectDefinitions;
import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.Hit.HitLook;
import com.rs.game.activities.dfm.DemonFlashMobs;
import com.rs.game.hitbar.HitBar;
import com.rs.game.hitbar.impl.EntityHitBar;
import com.rs.game.npc.NPC;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.npc.godwars.zaros.Nex;
import com.rs.game.npc.kalphiteking.KalphiteKing;
import com.rs.game.npc.others.Ascended;
import com.rs.game.npc.others.DreadNip;
import com.rs.game.npc.others.Legios;
import com.rs.game.npc.pet.Pet;
import com.rs.game.npc.spiderboss.Araxxor;
import com.rs.game.npc.spiderboss.AraxxorMinion;
import com.rs.game.npc.telos.Telos;
import com.rs.game.player.BuffDebuffTimersManager.Timer;
import com.rs.game.player.GlobalPlayerUpdater;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.invention.InventionConstants.Perks;
import com.rs.game.player.actions.invention.InventionData.Perk;
import com.rs.game.player.content.Magic;
import com.rs.game.player.controllers.DTController;
import com.rs.game.player.dialogue.impl.Transportation;
import com.rs.game.route.RouteFinder;
import com.rs.game.route.strategy.EntityStrategy;
import com.rs.game.route.strategy.FixedTileStrategy;
import com.rs.game.route.strategy.ObjectStrategy;
import com.rs.utils.Logger;
import com.rs.utils.MapUtils;
import com.rs.utils.MapUtils.Structure;
import com.rs.utils.Utils;

import lombok.Getter;

public abstract class Entity extends WorldTile {

    private static final long serialVersionUID = -3372926325008880753L;
    private final static AtomicInteger hashCodeGenerator = new AtomicInteger();
    // transient stuff
    private transient int index;
    private transient int lastRegionId; // the last region the entity was at
    private transient WorldTile lastLoadedMapRegionTile;

    private transient CopyOnWriteArrayList<Integer> mapRegionsIds; // called by
    // more than
    // 1thread
    // so
    // concurent
    private transient int direction;
    private transient WorldTile lastWorldTile;
    private transient WorldTile nextWorldTile;
    private transient int nextWalkDirection;
    private transient int nextRunDirection;
    private transient Rectangle nextFaceWorldTile;
    private transient boolean teleported;
    private transient ConcurrentLinkedQueue<Object[]> walkSteps;// called by
    // more than
    // 1thread so
    // concurent
    private transient ConcurrentLinkedQueue<Hit> receivedHits;
    private transient Map<Entity, Integer> receivedDamage;
    private transient boolean finished; // if removed
    private transient long freezeDelay;
    // entity masks
    private transient Animation nextAnimation;
    private transient Graphics nextGraphics1;
    private transient Graphics nextGraphics2;
    private transient Graphics nextGraphics3;
    private transient Graphics nextGraphics4;
    private transient ArrayList<Hit> nextHits;
    protected transient ArrayList<HitBar> nextHitBars;
    private transient ForceMovement nextForceMovement;
    private transient com.rs.game.player.client.Native950ForceMovement.Plan nextNative950ForceMovement;
    private transient long native950ForceMovementGeneration;
    private transient long nextNative950ForceMaskGeneration;
    private transient long native950ForceArrivalGeneration;
    private transient boolean native950ForceMovementActive;
    private transient ForceTalk nextForceTalk;
    private transient int nextFaceEntity;
    private transient int lastFaceEntity;
    private transient Entity attacking;
    private transient Entity attackedBy; // whos attacking you, used for single
    private transient long attackedByDelay; // delay till someone else can
    // attack you
    private transient long attackingDelay; // tells youre attacking someone
    private transient boolean multiArea;
    private transient boolean isAtDynamicRegion;
    private transient long lastAnimationEnd;
    private transient boolean forceMultiArea;
    private transient long frozenBlocked;
    private transient int immunity;
    private transient long stunDelay;
    private transient long findTargetDelay;
    private transient ConcurrentHashMap<Object, Object> temporaryAttributes;
    private transient int hashCode;
    private transient ArrayList<Entity> aggressiveOnYou;
    // saving stuff
    protected int hitpoints;
    private int mapSize; // default 0, can be setted other value usefull on
    // static maps
    private boolean run;
    private final Poison poison;
    /*
     * Freeze immunity boolean, setter, and getter ~ used to set immunity options
     */
    private boolean freezeImmune;
    /**
     * All custom crowd control effects Effects:
     * <p>
     * + Stun: +
     */
    private boolean stunImmune;
    @Getter
    private transient TemporaryModifiersManager temporaryModifiersManager;

    // creates Entity and saved classes
    public Entity(final WorldTile tile) {
        super(tile);
        poison = new Poison();
        effectsManager = new EffectsManager();
        temporaryModifiersManager = new TemporaryModifiersManager();
    }

    public Map<Entity, Integer> getReceivedDamage() {
        return receivedDamage;
    }

    public static boolean findBasicRoute(final Entity src, final WorldTile dest, int maxStepsCount, final boolean calculate) {
        final int[] srcPos = src.getLastWalkTile();
        final int[] destPos = {dest.getX(), dest.getY()};
        final int srcSize = src.getSize();
        // set destSize to 0 to walk under it else follows
        final int destSize = dest instanceof Entity ? ((Entity) dest).getSize() : 1;
        final int[] destScenePos = {destPos[0] + destSize - 1, destPos[1] + destSize - 1};// Arrays.copyOf(destPos,
        while (maxStepsCount-- != 0) {
            final int[] srcScenePos = {srcPos[0] + srcSize - 1, srcPos[1] + srcSize - 1};// srcSize
            if (!Utils.isOnRange(srcPos[0], srcPos[1], srcSize, destPos[0], destPos[1], destSize, 0)) {
                if (srcScenePos[0] < destScenePos[0] && srcScenePos[1] < destScenePos[1] && (!(src instanceof NPC) || src.canWalkNPC(srcPos[0] + 1, srcPos[1] + 1)) && src.addWalkStep(srcPos[0] + 1, srcPos[1] + 1, srcPos[0], srcPos[1], true)) {
                    srcPos[0]++;
                    srcPos[1]++;
                    continue;
                }
                if (srcScenePos[0] > destScenePos[0] && srcScenePos[1] > destScenePos[1] && (!(src instanceof NPC) || src.canWalkNPC(srcPos[0] - 1, srcPos[1] - 1)) && src.addWalkStep(srcPos[0] - 1, srcPos[1] - 1, srcPos[0], srcPos[1], true)) {
                    srcPos[0]--;
                    srcPos[1]--;
                    continue;
                }
                if (srcScenePos[0] < destScenePos[0] && srcScenePos[1] > destScenePos[1] && (!(src instanceof NPC) || src.canWalkNPC(srcPos[0] + 1, srcPos[1] - 1)) && src.addWalkStep(srcPos[0] + 1, srcPos[1] - 1, srcPos[0], srcPos[1], true)) {
                    srcPos[0]++;
                    srcPos[1]--;
                    continue;
                }
                if (srcScenePos[0] > destScenePos[0] && srcScenePos[1] < destScenePos[1] && (!(src instanceof NPC) || src.canWalkNPC(srcPos[0] - 1, srcPos[1] + 1)) && src.addWalkStep(srcPos[0] - 1, srcPos[1] + 1, srcPos[0], srcPos[1], true)) {
                    srcPos[0]--;
                    srcPos[1]++;
                    continue;
                }
                if (srcScenePos[0] < destScenePos[0] && (!(src instanceof NPC) || src.canWalkNPC(srcPos[0] + 1, srcPos[1])) && src.addWalkStep(srcPos[0] + 1, srcPos[1], srcPos[0], srcPos[1], true)) {
                    srcPos[0]++;
                    continue;
                }
                if (srcScenePos[0] > destScenePos[0] && (!(src instanceof NPC) || src.canWalkNPC(srcPos[0] - 1, srcPos[1])) && src.addWalkStep(srcPos[0] - 1, srcPos[1], srcPos[0], srcPos[1], true)) {
                    srcPos[0]--;
                    continue;
                }
                if (srcScenePos[1] < destScenePos[1] && (!(src instanceof NPC) || src.canWalkNPC(srcPos[0], srcPos[1] + 1)) && src.addWalkStep(srcPos[0], srcPos[1] + 1, srcPos[0], srcPos[1], true)) {
                    srcPos[1]++;
                    continue;
                }
                if (srcScenePos[1] > destScenePos[1] && (!(src instanceof NPC) || src.canWalkNPC(srcPos[0], srcPos[1] - 1)) && src.addWalkStep(srcPos[0], srcPos[1] - 1, srcPos[0], srcPos[1], true)) {
                    srcPos[1]--;
                    continue;
                }
                return false;
            }
            break; // for now nothing between break and return
        }
        return true;
    }

    public Set<Entity> getReceivedDamageSources() {
        return receivedDamage.keySet();
    }

    public boolean calcFollow(final WorldTile target, final boolean inteligent) {
        return calcFollow(target, -1, true, inteligent);
    }

    /*
     * Set immunity to being frozen by ice barrage for a bit ~ generally we want 2-4
     * seconds of non-freeze after a full freeze.
     */

    public boolean calcDungFollow(final WorldTile target, final int maxStepsCount, final boolean calculate, final boolean inteligent) {
        if (inteligent) {
            final int steps = RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER, getX(), getY(), getPlane(), getSize(), target instanceof WorldObject ? new ObjectStrategy((WorldObject) target) : new EntityStrategy((Entity) target), true);
            if (steps == -1) {
                return false;
            }
            if (steps == 0) {
                return true;
            }
            final int[] bufferX = RouteFinder.getLastPathBufferX();
            final int[] bufferY = RouteFinder.getLastPathBufferY();
            for (int step = steps - 1; step >= 0; step--) {
                if (!addWalkSteps(bufferX[step], bufferY[step], 25, true)) {
                    break;
                }
            }
            return true;
        }
        return findBasicRoute(this, target, maxStepsCount, true);
    }

    public boolean calcFollow(final WorldTile target, final int maxStepsCount, final boolean calculate, final boolean inteligent) {
        if (inteligent) {
            final int steps = RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER, getX(), getY(), getPlane(), getSize(), target instanceof WorldObject ? new ObjectStrategy((WorldObject) target) : target instanceof Entity ? new EntityStrategy((Entity) target) : new FixedTileStrategy(target.getX(), target.getY()), true);
            if (steps == -1) {
                return false;
            }
            if (steps == 0) {
                return true;
            }
            final int[] bufferX = RouteFinder.getLastPathBufferX();
            final int[] bufferY = RouteFinder.getLastPathBufferY();
            for (int step = steps - 1; step >= 0; step--) {
                if (!addWalkSteps(bufferX[step], bufferY[step], 25, true)) {
                    break;
                }
            }
            return true;
        }
        return findBasicRoute(this, target, maxStepsCount, true);
    }

    public boolean calcFollow(WorldTile target, int maxStepsCount, boolean calculate, boolean inteligent, int attackRange, boolean followClose) {
        if (inteligent) {
            int steps = RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER, getX(), getY(), getPlane(), getSize(), target instanceof WorldObject ? new ObjectStrategy((WorldObject) target) : target instanceof Entity ? new EntityStrategy((Entity) target) : new FixedTileStrategy(target.getX(), target.getY()), true);
            if (steps == -1)
                return false;
            if (steps == 0)
                return true;
            int[] bufferX = RouteFinder.getLastPathBufferX();
            int[] bufferY = RouteFinder.getLastPathBufferY();
            for (int step = steps - 1; step >= 0; step--) {
                int destX = bufferX[step];
                int destY = bufferY[step];
                if ((target instanceof Entity && Utils.isOnRange(this, (Entity) target, attackRange) && clipedProjectile(target, followClose))
                        
                        || (!(target instanceof Entity) && Utils.isOnRange(this, target, attackRange, 1, 1) && clipedProjectile(target, followClose)))
                    break;
                if (!addWalkSteps(destX, destY, 25, true))
                    break;
            }
            return true;
        }
        return findBasicRoute(this, target, maxStepsCount, true);
    }

    public void addFreezeDelay(final long time) {
        addFreezeDelay(time, false);
    }

    public int getImmunity() {
        return immunity;
    }

    public void setImmunity(final int charges) {
        immunity = charges;
    }

    public boolean isFreezeImmune() {
        return freezeImmune;
    }

    public void setFreezeImmune(final boolean immune) {
        freezeImmune = immune;
    }

    public void addFreezeDelay(final long time, final boolean entangleMessage) {
        final long currentTime = Utils.currentTimeMillis();
        if (currentTime > freezeDelay && !isFreezeImmune()) {
            resetWalkSteps();
            freezeDelay = time + currentTime;
            if (this instanceof Player) {
                final Player p = (Player) this;
                if (!entangleMessage) {
                    p.sendMessage("You have been frozen.", true);
                }
            }
        }
    }

    public void addFrozenBlockedDelay(final int time) {
        frozenBlocked = time + Utils.currentTimeMillis();
    }

    public void addReceivedDamage(Entity source, final int amount) {
        if (source == null) {
            return;
        }
        Integer damage = receivedDamage.get(source);
        damage = damage == null ? amount : damage + amount;
        if (damage < 0) {
            receivedDamage.remove(source);
        } else {
            receivedDamage.put(source, damage);
        }
    }

    // return cliped step
    public boolean addWalkStep(final int nextX, final int nextY, final int lastX, final int lastY, final boolean check) {
        final int dir = Utils.getMoveDirection(nextX - lastX, nextY - lastY);
        if (dir == -1) {
            return false;
        }
        if (check && !World.checkWalkStep(getPlane(), lastX, lastY, dir, getSize())) {
            return false;
        }
        if (this instanceof Player) {
            if (!((Player) this).getControlerManager().addWalkStep(lastX, lastY, nextX, nextY)) {
                return false;
            }
        }
        walkSteps.add(new Object[]{dir, nextX, nextY, check});
        return true;
    }

    public boolean addWalkSteps(final int destX, final int destY) {
        return addWalkSteps(destX, destY, -1);
    }

    /*
     * return added all steps
     */
    public boolean addWalkSteps(final int destX, final int destY, final int maxStepsCount) {
        return addWalkSteps(destX, destY, -1, true);
    }

    /*
     * return added all steps
     */
    public boolean addWalkSteps(final int destX, final int destY, final int maxStepsCount, final boolean check) {
        /* Disable movement if user is frozen */
        if (this instanceof Player) {
            final Player walk = (Player) this;
            if (walk.isFrozen() || walk.isCantWalk()) {
                return false;
            }
            walk.setKeyAction(null);
        }

        final int[] lastTile = getLastWalkTile();
        int myX = lastTile[0];
        int myY = lastTile[1];
        int stepCount = 0;
        while (true) {
            stepCount++;
            if (myX < destX) {
                myX++;
            } else if (myX > destX) {
                myX--;
            }
            if (myY < destY) {
                myY++;
            } else if (myY > destY) {
                myY--;
            }
            if (!addWalkStep(myX, myY, lastTile[0], lastTile[1], check)) {
                return false;
            }
            if (stepCount == maxStepsCount) {
                return true;
            }
            lastTile[0] = myX;
            lastTile[1] = myY;
            if (lastTile[0] == destX && lastTile[1] == destY) {
                return true;
            }
        }
    }

    public boolean addWalkStepsInteract(final int destX, final int destY, final int maxStepsCount, final int size, final boolean calculate) {
        return addWalkStepsInteract(destX, destY, maxStepsCount, size, size, calculate);
    }

    /*
     * return added all steps
     */
    public boolean addWalkStepsInteract(final int destX, final int destY, final int maxStepsCount, final int sizeX, final int sizeY, final boolean calculate) {
        final int[] lastTile = getLastWalkTile();
        int myX = lastTile[0];
        int myY = lastTile[1];
        int stepCount = 0;
        while (true) {
            stepCount++;
            final int myRealX = myX;
            final int myRealY = myY;

            if (myX < destX) {
                myX++;
            } else if (myX > destX) {
                myX--;
            }
            if (myY < destY) {
                myY++;
            } else if (myY > destY) {
                myY--;
            }
            if ((this instanceof NPC && !canWalkNPC(myX, myY)) || !addWalkStep(myX, myY, lastTile[0], lastTile[1], true)) {
                if (!calculate) {
                    return false;
                }
                myX = myRealX;
                myY = myRealY;
                final int[] myT = calculatedStep(myRealX, myRealY, destX, destY, lastTile[0], lastTile[1], sizeX, sizeY);
                if (myT == null) {
                    return false;
                }
                myX = myT[0];
                myY = myT[1];
            }
            final int distanceX = myX - destX;
            final int distanceY = myY - destY;
            if (!(distanceX > sizeX || distanceX < -1 || distanceY > sizeY || distanceY < -1)) {
                return true;
            }
            if (stepCount == maxStepsCount) {
                return true;
            }
            lastTile[0] = myX;
            lastTile[1] = myY;
            if (lastTile[0] == destX && lastTile[1] == destY) {
                return true;
            }
        }
    }

    public void applyHit(Hit hit) {
        try {
            if (isDead()) {
                return;
            }
            final Entity source = hit.getSource();
            if (source != null) {
                hit = source.handleOutgoingHit(hit, this);
            }
            // todo damage for who gets drop
            getReceivedHits().add(hit); // added hit first because, soaking added after,
            // if applyhit used right there shouldnt be any problem
            handleIngoingHit(hit);
        } catch (final Exception e) {
            Logger.getGlobal().catching(e);
        }
    }

    /**
     * Where the hit came from.
     *
     * @param hit The hit.
     */
    @Deprecated
    public Hit handleOutgoingHit(final Hit hit, final Entity target) {
        // EMPTY
        return hit;
    }

    public int[] calculatedStep(int myX, int myY, final int destX, final int destY, final int lastX, final int lastY, final int sizeX, final int sizeY) {
        if (myX < destX) {
            myX++;
            if ((this instanceof NPC && !canWalkNPC(myX, myY)) || !addWalkStep(myX, myY, lastX, lastY, true)) {
                myX--;
            } else if (!(myX - destX > sizeX || myX - destX < -1 || myY - destY > sizeY || myY - destY < -1)) {
                if (myX == lastX || myY == lastY) {
                    return null;
                }

                return new int[]{myX, myY};
            }
        } else if (myX > destX) {
            myX--;
            if ((this instanceof NPC && !canWalkNPC(myX, myY)) || !addWalkStep(myX, myY, lastX, lastY, true)) {
                myX++;
            } else if (!(myX - destX > sizeX || myX - destX < -1 || myY - destY > sizeY || myY - destY < -1)) {
                if (myX == lastX || myY == lastY) {
                    return null;
                }
                return new int[]{myX, myY};
            }
        }
        if (myY < destY) {
            myY++;
            if ((this instanceof NPC && !canWalkNPC(myX, myY)) || !addWalkStep(myX, myY, lastX, lastY, true)) {
                myY--;
            } else if (!(myX - destX > sizeX || myX - destX < -1 || myY - destY > sizeY || myY - destY < -1)) {
                if (myX == lastX || myY == lastY) {
                    return null;
                }
                return new int[]{myX, myY};
            }
        } else if (myY > destY) {
            myY--;
            if ((this instanceof NPC && !canWalkNPC(myX, myY)) || !addWalkStep(myX, myY, lastX, lastY, true)) {
                myY++;
            } else if (!(myX - destX > sizeX || myX - destX < -1 || myY - destY > sizeY || myY - destY < -1)) {
                if (myX == lastX || myY == lastY) {
                    return null;
                }
                return new int[]{myX, myY};
            }
        }
        if (myX == lastX || myY == lastY) {
            return null;
        }
        return new int[]{myX, myY};
    }

    public void cancelFaceEntityNoCheck() {
        nextFaceEntity = -2;
        lastFaceEntity = -1;
    }

    /**
     * Checks if the NPC can walk to the tile or not. Used to STOP safe-spotting.
     *
     * @param toX The X coordinate.
     * @param toY The Y coordinate.
     * @return if can walk.
     */
    public boolean canWalkNPC(final int toX, final int toY) {
        final int id = ((NPC) this).getId();
        return (id >= 2881 && id <= 2883) || id == 14301 || id == 6260 || id == 22438 || id == 22439 || id == 22440 || id == 22441 || this instanceof Familiar || id == 6222 || id == 17182 || id >= 22453 && id <= 22456 || id == 6203 || id == 6247 || id == 8133 || this instanceof KalphiteKing || this instanceof Araxxor || this instanceof Telos || this instanceof AraxxorMinion || this instanceof DreadNip || this instanceof Legios || this instanceof Ascended || canWalkNPC(toX, toY, false);
    }

    // checks collisions
    public boolean canWalkNPC(final int toX, final int toY, final boolean checkUnder) {
        if (!isAtMultiArea()) {
            return true;
        }
        if (getFreezeDelay() >= Utils.currentTimeMillis()) {
            return false;
        }
        final int size = getSize();
        for (final int regionId : getMapRegionsIds()) {
            final List<Integer> npcIndexes = World.getRegion(regionId).getNPCsIndexes();
            if (npcIndexes != null && npcIndexes.size() < 50) {
                for (final int npcIndex : npcIndexes) {
                    final NPC target = World.getNPCs().get(npcIndex);
                    if (target == null || target == this || target.isDead() || target.hasFinished() || target.getPlane() != getPlane() || !target.isAtMultiArea() || (!(this instanceof Familiar) && target instanceof Familiar)) {
                        continue;
                    }
                    final int targetSize = target.getSize();
                    if (!checkUnder && target.getNextWalkDirection() == -1) {
                        final int previewDir = getPreviewNextWalkStep();
                        if (previewDir != -1) {
                            final int tileX = target.getX() + Utils.DIRECTION_DELTA_X[previewDir];
                            final int tileY = target.getY() + Utils.DIRECTION_DELTA_Y[previewDir];

                            if (Utils.colides(tileX, tileY, targetSize, getX(), getY(), size)) {
                                continue;
                            }

                            if (Utils.colides(tileX, tileY, targetSize, toX, toY, size)) {
                                return false;
                            }
                        }
                    }
                    if (Utils.colides(target.getX(), target.getY(), targetSize, getX(), getY(), size)) {
                        continue;
                    }
                    if (Utils.colides(target.getX(), target.getY(), targetSize, toX, toY, size)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public int[] checkcalculatedStep(int myX, int myY, final int destX, final int destY, final int lastX, final int lastY, final int size) {
        if (myX < destX) {
            myX++;
            if (!checkWalkStep(myX, myY, lastX, lastY, true)) {
                myX--;
            } else if (!(myX - destX > size || myX - destX < -1 || myY - destY > size || myY - destY < -1)) {
                if (myX == lastX || myY == lastY) {
                    return null;
                }
                return new int[]{myX, myY};
            }
        } else if (myX > destX) {
            myX--;
            if (!checkWalkStep(myX, myY, lastX, lastY, true)) {
                myX++;
            } else if (!(myX - destX > size || myX - destX < -1 || myY - destY > size || myY - destY < -1)) {
                if (myX == lastX || myY == lastY) {
                    return null;
                }
                return new int[]{myX, myY};
            }
        }
        if (myY < destY) {
            myY++;
            if (!checkWalkStep(myX, myY, lastX, lastY, true)) {
                myY--;
            } else if (!(myX - destX > size || myX - destX < -1 || myY - destY > size || myY - destY < -1)) {
                if (myX == lastX || myY == lastY) {
                    return null;
                }
                return new int[]{myX, myY};
            }
        } else if (myY > destY) {
            myY--;
            if (!checkWalkStep(myX, myY, lastX, lastY, true)) {
                myY++;
            } else if (!(myX - destX > size || myX - destX < -1 || myY - destY > size || myY - destY < -1)) {
                if (myX == lastX || myY == lastY) {
                    return null;
                }
                return new int[]{myX, myY};
            }
        }
        if (myX == lastX || myY == lastY) {
            return null;
        }
        return new int[]{myX, myY};
    }

    public void checkMultiArea() {
        multiArea = forceMultiArea || DemonFlashMobs.getDemonFlashMobs() != null && DemonFlashMobs.getDemonFlashMobs().isMultiArea(this) || World.isMultiArea(this);
    }

    // return cliped step
    public boolean checkWalkStep(final int nextX, final int nextY, final int lastX, final int lastY, final boolean check) {
        final int dir = Utils.getMoveDirection(nextX - lastX, nextY - lastY);
        if (dir == -1) {
            return false;
        }

        return !check || World.checkWalkStep(getPlane(), lastX, lastY, dir, getSize());
    }

    /*
     * return added all steps
     */
    public boolean checkWalkStepsInteract(final int fromX, final int fromY, final int destX, final int destY, final int maxStepsCount, final int size, final boolean calculate) {
        final int[] lastTile = new int[]{fromX, fromY};
        int myX = lastTile[0];
        int myY = lastTile[1];
        int stepCount = 0;
        while (true) {
            stepCount++;
            final int myRealX = myX;
            final int myRealY = myY;

            if (myX < destX) {
                myX++;
            } else if (myX > destX) {
                myX--;
            }
            if (myY < destY) {
                myY++;
            } else if (myY > destY) {
                myY--;
            }
            if (!checkWalkStep(myX, myY, lastTile[0], lastTile[1], true)) {
                if (!calculate) {
                    return false;
                }
                myX = myRealX;
                myY = myRealY;
                final int[] myT = checkcalculatedStep(myRealX, myRealY, destX, destY, lastTile[0], lastTile[1], size);
                if (myT == null) {
                    return false;
                }
                myX = myT[0];
                myY = myT[1];
            }
            final int distanceX = myX - destX;
            final int distanceY = myY - destY;
            if (!(distanceX > size || distanceX < -1 || distanceY > size || distanceY < -1)) {
                return true;
            }
            if (stepCount == maxStepsCount) {
                return true;
            }
            lastTile[0] = myX;
            lastTile[1] = myY;
            if (lastTile[0] == destX && lastTile[1] == destY) {
                return true;
            }
        }
    }

    /*
     * returns if cliped
     */
    public boolean clipedProjectile(WorldTile tile, final boolean checkClose) {
        if (tile instanceof NPC) {
            if (this instanceof Nex && tile instanceof Pet) {
                return false;
            }
            final NPC n = (NPC) tile;
            if (this instanceof Player) {
                return n.clipedProjectile(this, checkClose);
            }
            tile = n.getMiddleWorldTile();
        } else if (tile instanceof Player && this instanceof Player) {
            final Player p = (Player) tile;
            return clipedProjectile(tile, checkClose, 1) || p.clipedProjectile(this, checkClose, 1);
        }
        return clipedProjectile(tile, checkClose, 1); // size 1 thats arrow
        // size, the tile has to
        // be target center
        // coord not base
    }

    /*
     * returns if cliped
     */
    public boolean clipedProjectile(final WorldTile tile, final boolean checkClose, final int size) {
        int myX = getX();
        int myY = getY();
        if (this instanceof NPC) {
            NPC n = (NPC) this;
            WorldTile thist = n.getMiddleWorldTile();
            myX = thist.getX();
            myY = thist.getY();
        }
        int destX = tile.getX();
        int destY = tile.getY();
        if (myX == destX && destY == myY)
            return true;
        int lastTileX = myX;
        int lastTileY = myY;
        while (true) {
            if (myX < destX)
                myX++;
            else if (myX > destX)
                myX--;
            if (myY < destY)
                myY++;
            else if (myY > destY)
                myY--;
            int dir = Utils.getMoveDirection(myX - lastTileX, myY - lastTileY);
            if (dir == -1)
                return false;
            if (checkClose) {
                if (!World.checkWalkStep(getPlane(), lastTileX, lastTileY, dir, size))
                    return false;
            } else if (!World.checkProjectileStep(getPlane(), lastTileX, lastTileY, dir, size))
                return false;
            lastTileX = myX;
            lastTileY = myY;
            if (lastTileX == destX && lastTileY == destY)
                return true;
        }
    }

    public void faceEntity(final Entity target) {
        setNextFaceRectanglePrecise(new WorldTile(target.getX(), target.getY(), target.getPlane()), target.getSize(),
                target.getSize());
    }

    public void faceObject(final WorldObject object) {
        ObjectDefinitions def = object.getDefinitions();
        int x = -1, y = -1;
        int sizeX = 1, sizeY = 1;
        if (object.getType() == 0) { // wall
            if (object.getRotation() == 0) { // west
                x = object.getX() - 1;
                y = object.getY();
            } else if (object.getRotation() == 1) { // north
                x = object.getX();
                y = object.getY() + 1;
            } else if (object.getRotation() == 2) { // east
                x = object.getX() + 1;
                y = object.getY();
            } else if (object.getRotation() == 3) { // south
                x = object.getX();
                y = object.getY() - 1;
            }
        } else if (object.getType() == 1 || object.getType() == 2) { // corner
            // and
            // cornerwall
            if (object.getRotation() == 0) { // nw
                x = object.getX() - 1;
                y = object.getY() + 1;
            } else if (object.getRotation() == 1) { // ne
                x = object.getX() + 1;
                y = object.getY() + 1;
            } else if (object.getRotation() == 2) { // se
                x = object.getX() + 1;
                y = object.getY() - 1;
            } else if (object.getRotation() == 3) { // sw
                x = object.getX() - 1;
                y = object.getY() - 1;
            }
        } else if (object.getType() == 3) { // inverted corner
            if (object.getRotation() == 0) { // se
                x = object.getX() + 1;
                y = object.getY() - 1;
            } else if (object.getRotation() == 1) { // sw
                x = object.getX() - 1;
                y = object.getY() - 1;
            } else if (object.getRotation() == 2) { // nw
                x = object.getX() - 1;
                y = object.getY() + 1;
            } else if (object.getRotation() == 3) { // ne
                x = object.getX() + 1;
                y = object.getY() + 1;
            }
        } else if (object.getType() < 10) { // walldeco's
            if (object.getRotation() == 0) { // west
                x = object.getX() - 1;
                y = object.getY();
            } else if (object.getRotation() == 1) { // north
                x = object.getX();
                y = object.getY() + 1;
            } else if (object.getRotation() == 2) { // east
                x = object.getX() + 1;
                y = object.getY();
            } else if (object.getRotation() == 3) { // south
                x = object.getX();
                y = object.getY() - 1;
            }
        } else if (object.getType() == 10 || object.getType() == 11 || object.getType() == 22) { // multisized
            // rect
            // objs
            if (object.getRotation() == 0 || object.getRotation() == 2) {
                x = object.getX();
                y = object.getY();
                sizeX = def.getSizeX();
                sizeY = def.getSizeY();
            } else {
                x = object.getX();
                y = object.getY();
                sizeX = def.getSizeY();
                sizeY = def.getSizeX();
            }
        } else {
            // rest
            x = object.getX();
            y = object.getY();
        }

        setNextFaceRectanglePrecise(new WorldTile(x, y, getPlane()), sizeX, sizeY);
    }

    public abstract void finish();

    /**
     * Combat and attack definitions
     */

    public Entity getTarget() {
        return attacking;
    }

    public void setTarget(final Entity target) {
        attacking = target;
    }

    public Entity getAttackedBy() {
        return attackedBy;
    }

    public void setAttackedBy(final Entity attackedBy) {
        this.attackedBy = attackedBy;
    }

    public long getAttackedByDelay() {
        return attackedByDelay;
    }

    public void setAttackedByDelay(final long attackedByDelay) {
        this.attackedByDelay = attackedByDelay;
    }

    public int getClientIndex() {
        return index + (this instanceof Player ? 32768 : 0);
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(final int direction) {
        this.direction = direction;
    }

    public long getFindTargetDelay() {
        return findTargetDelay;
    }

    public void setFindTargetDelay(final long findTargetDelay) {
        this.findTargetDelay = findTargetDelay;
    }

    public long getFreezeDelay() {
        return freezeDelay; // 2500 delay
    }

    public void setFreezeDelay(final int seconds) {
        freezeDelay = Utils.currentTimeMillis() + (seconds * 1000);
    }

    public void unFreeze() {
        freezeDelay = Utils.currentTimeMillis();
    }

    public long getFrozenBlockedDelay() {
        return frozenBlocked;
    }

    public long getStunDelay() {
        return stunDelay;
    }

    public boolean isStunned() {
        return stunDelay >= Utils.currentTimeMillis();
    }

    public boolean isStunImmune() {
        return stunImmune;
    }

    public void setStunImmune(final boolean immune) {
        stunImmune = immune;
    }

    public void setStunDelay(long delay) {
        this.stunDelay = delay;
    }

    public void addStunDelay(final long time, final boolean entangleMessage) {
        final long currentTime = Utils.currentTimeMillis();
        if (currentTime > stunDelay && !isStunImmune()) {
            resetWalkSteps();
            stunDelay = time + currentTime;
            if (this instanceof Player) {
                final Player p = (Player) this;
                if (!entangleMessage) {
                    p.sendMessage("You have been stunned.", true);
                }
            }
        }
    }

    public boolean isFrozen() {
        return freezeDelay >= Utils.currentTimeMillis();
    }

    public int getHitpoints() {
        return hitpoints;
    }

    public int getHealthPercentage() {
        int maxHitpoints = getMaxHitpoints();
        if(maxHitpoints <= 0) {
            maxHitpoints = 1;
        }
        return (int) (((double) hitpoints / maxHitpoints) * 100);
    }

    public void setHitpoints(final int hitpoints) {
        this.hitpoints = hitpoints;
        needTargetInformationUpdate = true;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(final int index) {
        this.index = index;
    }

    public long getLastAnimationEnd() {
        return lastAnimationEnd;
    }

    public int getLastFaceEntity() {
        return lastFaceEntity;
    }

    public WorldTile getLastLoadedMapRegionTile() {
        return lastLoadedMapRegionTile;
    }

    public int getLastRegionId() {
        return lastRegionId;
    }

    public void setLastRegionId(final int lastRegionId) {
        this.lastRegionId = lastRegionId;
    }

    private int[] getLastWalkTile() {
        final Object[] objects = walkSteps.toArray();
        if (objects.length == 0) {
            return new int[]{getX(), getY()};
        }
        final Object[] step = (Object[]) objects[objects.length - 1];
        return new int[]{(int) step[1], (int) step[2]};
    }

    public WorldTile getLastWorldTile() {
        return lastWorldTile;
    }

    public abstract double getMagePrayerMultiplier();

    public CopyOnWriteArrayList<Integer> getMapRegionsIds() {
        return mapRegionsIds;
    }

    public int getMapSize() {
        return (this instanceof Player)  ? (((Player)this).isUsingNXT() ? 5 : mapSize) : mapSize;
    }

    public void setMapSize(final int size) {
        mapSize = size;
        loadMapRegions();
    }

    public abstract int getMaxHitpoints();

    public abstract double getMeleePrayerMultiplier();

    public Player getMostDamageReceivedSourcePlayer() {
        Player player = null;
        int damage = -1;
        if (receivedDamage == null)
            return null;
        for (Map.Entry<Entity, Integer> entry : receivedDamage.entrySet()) {
            Entity source = entry.getKey();
            if (!(source instanceof Player)) {
                continue;
            }
            final Integer d = entry.getValue();
            if (d == null || source.hasFinished()) {
                receivedDamage.remove(source);
                continue;
            }
            if (d > damage) {
                player = (Player) source;
                damage = d;
            }
        }
        return player;
    }

    public Animation getNextAnimation() {
        return nextAnimation;
    }

    public void setNextAnimation(final Animation nextAnimation) {
        if (nextAnimation != null && nextAnimation.getIds()[0] >= 0) {
            // Safe-load the definition
            AnimationDefinitions defs = AnimationDefinitions.getAnimationDefinitions(nextAnimation.getIds()[0]);

            // Only calculate the end time if the definition actually exists in the cache
            if (defs != null) {
                lastAnimationEnd = Utils.currentTimeMillis() + defs.getEmoteTime();
            } else {
                // Fallback: If cache is missing the anim, just set a default 1-second delay
                lastAnimationEnd = Utils.currentTimeMillis() + 1000;
            }
        }
        this.nextAnimation = nextAnimation;
    }

    public int getNextFaceEntity() {
        return nextFaceEntity;
    }

    public void setNextFaceEntity(final Entity entity) {
        if (entity == null) {
            nextFaceEntity = -1;
            lastFaceEntity = -1;
        } else {
            nextFaceEntity = entity.getClientIndex();
            lastFaceEntity = nextFaceEntity;
        }
    }

    public Rectangle getNextFaceWorldTile() {
        return nextFaceWorldTile;
    }

    // @Deprecated used to face simply a tiel
    // "use setNextFaceRectanglePrecise(tile, 1, 1)
    public void setNextFaceWorldTile(WorldTile nextFaceWorldTile) {
        // who the hell made setNextFaceworldTile() ??!!! fucking incorrect
        // calcs.....
        // also the thing you guys call direction is actually angle
        setNextFaceRectanglePrecise(nextFaceWorldTile, 1, 1);
    }

    public void setNextFaceRectanglePrecise(WorldTile base, int sizeX, int sizeY) {
        if (nextFaceWorldTile != null && nextFaceWorldTile.getX() == base.getX()
                && nextFaceWorldTile.getY() == base.getY() && nextFaceWorldTile.getSizeX() == sizeX
                && nextFaceWorldTile.getSizeY() == sizeY)
            return;
        nextFaceWorldTile = new Rectangle(base.getX(), base.getY(), sizeX, sizeY);
        updateAngle(base, sizeX, sizeY);
    }
    
    /*
     * avoid using it :p
     */
    @Deprecated
    public void updateAngle(WorldTile base, int sizeX, int sizeY) {
        WorldTile from = nextWorldTile != null ? nextWorldTile : this;
        int srcX = (from.getX() * 512) + (getSize() * 256);
        int srcY = (from.getY() * 512) + (getSize() * 256);
        int dstX = (base.getX() * 512) + (sizeX * 256);
        int dstY = (base.getY() * 512) + (sizeY * 256);
        int deltaX = srcX - dstX;
        int deltaY = srcY - dstY;
        direction = deltaX != 0 || deltaY != 0 ? (int) (Math.atan2(deltaX, deltaY) * 2607.5945876176133) & 0x3FFF : 0;
    }
    
    public ForceMovement getNextForceMovement() {
        return nextForceMovement;
    }

    /** Per-mask-tick snapshot; scheduling survives resetMasks, explicit cancellation does not. */
    public com.rs.game.player.client.Native950ForceMovement.Plan getNextNative950ForceMovement() {
        return nextNative950ForceMovement;
    }

    /** Persistent engine state: the one-tick mask snapshot may already have been reset. */
    public boolean isNative950ForceMovementActive() {
        return native950ForceMovementActive;
    }

    /** Nonzero only while the original interpolation mask is available for this frame. */
    public long getNative950ForceMaskGeneration() {
        return nextNative950ForceMaskGeneration;
    }

    /** Nonzero for a scheduled server waypoint whose client interpolation already owns XY. */
    public long getNative950ForceArrivalGeneration() {
        return native950ForceArrivalGeneration;
    }

    private void cancelNative950ForceMovement(final WorldTile destination) {
        final boolean clientMayStillMove = native950ForceMovementActive || nextNative950ForceMovement != null;
        native950ForceMovementGeneration++;
        nextNative950ForceMaskGeneration = 0;
        native950ForceMovementActive = false;
        nextForceMovement = null;
        nextNative950ForceMovement = clientMayStillMove
                ? com.rs.game.player.client.Native950ForceMovement.stationary(destination, getDirection() & 0x3fff)
                : null;
        if (clientMayStillMove) {
            // The stationary mask uses the client's rendered position as its base. Reposition
            // first, using the viewer's tracked logical endpoint, so cancellation cannot freeze
            // the player at an unknown interpolated position between server waypoints.
            this.nextWorldTile = new WorldTile(destination);
            native950ForceArrivalGeneration = 0;
        }
    }

    /** Scheduled force arrivals are not external teleports and must preserve stage two. */
    private void setNative950ForceArrival(final WorldTile tile, final long generation) {
        this.nextWorldTile = tile;
        native950ForceArrivalGeneration = generation;
    }

    public void setNextForceMovement(final ForceMovement nextForceMovements) {
        if (this instanceof Player && ((Player)this).getClientProfile()
                == com.rs.game.player.client.ClientProfile.NATIVE_950) {
            cancelNative950ForceMovement(nextWorldTile == null ? this : nextWorldTile);
            final long generation = native950ForceMovementGeneration;
            if (nextForceMovements == null) return;
            final com.rs.game.player.client.Native950ForceMovement.Plan plan =
                    com.rs.game.player.client.Native950ForceMovement.adapt(this, nextForceMovements);
            if (plan == null) return; // invalid content remains counted, never partially scheduled
            com.rs.cores.Native950TickScheduler scheduler = CoresManager.getNative950Scheduler();
            if (scheduler == null) throw new IllegalStateException("950 force movement requires the native world tick scheduler");
            nextForceMovement = nextForceMovements;
            nextNative950ForceMovement = plan;
            nextNative950ForceMaskGeneration = generation;
            native950ForceMovementActive = true;
            resetWalkSteps();
            com.rs.game.player.client.Native950ForceMovement.schedule(plan, scheduler,
                    () -> native950ForceMovementGeneration == generation && !hasFinished(),
                    tile -> setNative950ForceArrival(tile, generation),
                    () -> native950ForceMovementActive = false);
            return;
        }
        this.nextForceMovement = nextForceMovements;
        if (nextForceMovement == null)
            return;
        if (this instanceof Player) {
            WorldTile firstTile = new WorldTile(nextForceMovement.getToFirstTile());
            WorldTile secondTile = nextForceMovement.getToSecondTile() == null ? null : new WorldTile(nextForceMovement.getToSecondTile());
            long firstTileSetTime = Utils.currentTimeMillis() + nextForceMovement.getFirstTickTime();
            long secondTileSetTime = secondTile == null ? 0 : Utils.currentTimeMillis() + nextForceMovement.getSecondTickTime();
            CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
                boolean setFirst = false;
                boolean setSecond = secondTileSetTime == 0;

                @Override
                public boolean repeat() {
                    try {
                        if (setFirst && setSecond)
                            return false;
                        if (!setFirst && Utils.currentTimeMillis() >= firstTileSetTime) {
                            setNextWorldTile(new WorldTile(firstTile));
                            setFirst = true;
                            return true;
                        }
                        if (setFirst && !setSecond && Utils.currentTimeMillis() >= secondTileSetTime) {
                            setNextWorldTile(new WorldTile(secondTile));
                            setSecond = true;
                            return true;
                        }
                        return true;
                    } catch (Exception e) {
                        Logger.getGlobal().catching(e);
                        return false;
                    }
                }
            }, 1, 1, TimeUnit.MILLISECONDS);
        }
    }

    public ForceTalk getNextForceTalk() {
        return nextForceTalk;
    }

    public void setNextForceTalk(final ForceTalk nextForceTalk) {
        this.nextForceTalk = nextForceTalk;
    }

    public Graphics getNextGraphics1() {
        return nextGraphics1;
    }

    public Graphics getNextGraphics2() {
        return nextGraphics2;
    }

    public Graphics getNextGraphics3() {
        return nextGraphics3;
    }

    public Graphics getNextGraphics4() {
        return nextGraphics4;
    }

    public ArrayList<Hit> getNextHits() {
        return nextHits;
    }

    public List<HitBar> getNextHitBars() {
        return nextHitBars;
    }

    public int getNextRunDirection() {
        return nextRunDirection;
    }

    public int getNextWalkDirection() {
        return nextWalkDirection;
    }

    private Object[] getNextWalkStep() {
        final Object[] step = walkSteps.poll();
        return step;
    }

    public WorldTile getNextWorldTile() {
        return nextWorldTile;
    }

    public void setNextWorldTile(final WorldTile nextWorldTile) {
        native950ForceArrivalGeneration = 0; // this request replaces any pending forced arrival
        if (nextWorldTile != null && this instanceof Player && ((Player)this).getClientProfile()
                == com.rs.game.player.client.ClientProfile.NATIVE_950)
            cancelNative950ForceMovement(nextWorldTile); // cancel both native interpolation and old server endpoints
        this.nextWorldTile = nextWorldTile;
    }

    public Poison getPoison() {
        return poison;
    }

    private int getPreviewNextWalkStep() {
        final Object[] step = walkSteps.peek();
        if (step == null) {
            return -1;
        }
        return (int) step[0];
    }

    public abstract double getRangePrayerMultiplier();

    public boolean getRun() {
        return run;
    }

    public void setRun(final boolean run) {
        this.run = run;
    }

    public abstract int getSize();

    public ConcurrentHashMap<Object, Object> getTemporaryAttributtes() {
        return temporaryAttributes;
    }

    public ConcurrentLinkedQueue<Object[]> getWalkSteps() {
        return walkSteps;
    }

    public abstract void handleIngoingHit(Hit hit);

    public boolean hasFinished() {
        return finished;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    public boolean hasTeleported() {
        return teleported;
    }

    public boolean hasWalkSteps() {
        return !walkSteps.isEmpty();
    }

    public void heal(int amount) {
        heal(amount, 0);
    }

    public void heal(int amount, int extra) {
        heal(amount, extra, 0);
    }

    public void heal(int amount, int extra, int delay) {
        heal(amount, extra, delay, false);
    }

    public void heal(int amount, int extra, int delay, boolean displayMark) {
        if (isDead())
            return;
        boolean aboveMaxHP = hitpoints + amount >= getMaxHitpoints() + extra;
        int hp = aboveMaxHP ? getMaxHitpoints() + extra : hitpoints + amount;
        if (hitpoints > hp)
            return;
        if (displayMark) {
            int damage = hp - hitpoints;
            if (damage > 0)
                applyHit(new Hit(this, damage, HitLook.HEALED_DAMAGE, delay));
        }
        setHitpoints(hp);
    }

    public boolean inArea(final int a, final int b, final int c, final int d) {
        return getX() >= a && getY() >= b && getX() <= c && getY() <= d;
    }

    public final void initEntity() {
        hashCode = hashCodeGenerator.getAndIncrement();
        mapRegionsIds = new CopyOnWriteArrayList<Integer>();
        walkSteps = new ConcurrentLinkedQueue<Object[]>();
        setReceivedHits(new ConcurrentLinkedQueue<Hit>());
        receivedDamage = new ConcurrentHashMap<Entity, Integer>();
        temporaryAttributes = new ConcurrentHashMap<Object, Object>();
        nextHits = new ArrayList<Hit>();
        nextHitBars = new ArrayList<>();
        aggressiveOnYou = new ArrayList<Entity>();
        nextWalkDirection = nextRunDirection - 1;
        lastFaceEntity = -1;
        nextFaceEntity = -2;
        poison.setEntity(this);
        if (effectsManager == null)
            effectsManager = new EffectsManager();
        temporaryModifiersManager = new TemporaryModifiersManager();
        prayerDebuffs = new ConcurrentHashMap<Entity, Integer[]>();
        effectsManager.setEntity(this);
        mapSize = Settings.DEFAULT_MAP_SIZE;
    }

    public boolean isAtDynamicRegion() {
        return isAtDynamicRegion;
    }

    public boolean isAtMultiArea() {
        return multiArea;
    }

    public void setAtMultiArea(final boolean multiArea) {
        this.multiArea = multiArea;
    }

    public boolean isDead() {
        return hitpoints == 0;
    }

    public boolean isForceMultiArea() {
        return forceMultiArea;
    }

    public void setForceMultiArea(final boolean forceMultiArea) {
        this.forceMultiArea = forceMultiArea;
        checkMultiArea();
    }

    public void loadMapRegions() {
        mapRegionsIds.clear();
        isAtDynamicRegion = false;
        final int chunkX = getChunkX();
        final int chunkY = getChunkY();
        final int mapHash = Settings.MAP_SIZES[getMapSize()] >> 4;
        final int minRegionX = (chunkX - mapHash) / 8;
        final int minRegionY = (chunkY - mapHash) / 8;
        for (int xCalc = minRegionX < 0 ? 0 : minRegionX; xCalc <= ((chunkX + mapHash) / 8); xCalc++) {
            for (int yCalc = minRegionY < 0 ? 0 : minRegionY; yCalc <= ((chunkY + mapHash) / 8); yCalc++) {
                final int regionId = yCalc + (xCalc << 8);
                if (World.getRegion(regionId, this instanceof Player) instanceof DynamicRegion) {
                    isAtDynamicRegion = true;
                }
                mapRegionsIds.add(regionId);
            }
        }
        if (this instanceof Player && DynamicArea.getDynamicArea(getRegionId()) != null)
            isAtDynamicRegion = true;
        lastLoadedMapRegionTile = new WorldTile(this); // creates a immutable
        // copy of this
        int sceneChunksRadio = Settings.MAP_SIZES[getMapSize()] / 16; // 16 cuz a
        // chunk is 8.
        // radio is
        // half
        int sceneBaseChunkX = (chunkX - sceneChunksRadio);
        int sceneBaseChunkY = (chunkY - sceneChunksRadio);
        if (sceneBaseChunkX < 0)
            sceneBaseChunkX = 0;
        if (sceneBaseChunkY < 0)
            sceneBaseChunkY = 0;
        sceneBaseChunkId = MapUtils.encode(Structure.CHUNK, sceneBaseChunkX, sceneBaseChunkY);
    }

    public boolean needMasksUpdate() {
        return nextColour != null || nextFaceEntity != -2 || nextAnimation != null || nextGraphics1 != null
                || nextGraphics2 != null || nextGraphics3 != null || nextGraphics4 != null
                || (nextWalkDirection == -1 && nextRunDirection == -1 && nextFaceWorldTile != null)
                || !nextHits.isEmpty() || !nextHitBars.isEmpty() || nextForceMovement != null || nextForceTalk != null
                || needTargetInformationUpdate;
    }

    public void playSound(final int soundId, final int type) {
        for (final int regionId : getMapRegionsIds()) {
            final List<Integer> playerIndexes = World.getRegion(regionId).getPlayerIndexes();
            if (playerIndexes != null) {
                for (final int playerIndex : playerIndexes) {
                    final Player player = World.getPlayers().get(playerIndex);
                    if (player == null || !player.isRunning() || !withinDistance(player)) {
                        continue;
                    }
                    player.getPackets().sendSound(soundId, 0, type);
                }
            }
        }
    }

    public void processEntityUpdate() {
        processMovement();
        processReceivedHits();
        processReceivedDamage();
    }

    public void processEntity() {
        if (getHitpoints() >= getMaxHitpoints())
            extraHitPoints = getHitpoints() - getMaxHitpoints();
        else {
            extraHitPoints = 0;
        }
        poison.processPoison();
        effectsManager.processEffects();
        temporaryModifiersManager.process();
    }

    public void processHit(final Hit hit) {
        if (isDead()) {
            return;
        }
        removeHitpoints(hit);
        nextHits.add(hit);
        if (this instanceof Player)
            ((Player) this).getChargesManagerNew().useChargeOnHit(false);
        if (nextHitBars.isEmpty()) {
            addHitBars();
        }
    }

    public void addHitBars() {
        nextHitBars.add(new EntityHitBar(this));
    }

    public abstract boolean canMove(int dir);

    public void processMovement() {
        if (this instanceof Player) {
            Player player = (Player) this;
            if (player.getXmas().isSnowman == true) {
                int randomWalkStep = Utils.random(8);
                if (randomWalkStep == 0) {
                    player.getXmas().isSnowman = false;
                    player.getAppearence().transformIntoNPC(-1);
                    player.sendMessage("The snow has melted and you have turned back to normal.", true);
                }
            }
        }
        boolean wasAtDT = this instanceof Player && DTController.isInsideDominionTower((Player) this);
        lastWorldTile = new WorldTile(this);
        if (lastFaceEntity >= 0) {
            Entity target = lastFaceEntity >= 32768 ? World.getPlayers().get(lastFaceEntity - 32768)
                    : World.getNPCs().get(lastFaceEntity);
            if (target != null) {
                int size = target.getSize();
                updateAngle(target, size, size);
            }
        }
        nextWalkDirection = nextRunDirection = -1;
        if (nextWorldTile != null) {
            lastWorldTile = new WorldTile(this);
            final int lastPlane = getPlane();
            setLocation(nextWorldTile);
            if (this instanceof Player && wasAtDT != (this instanceof Player && DTController.isInsideDominionTower((Player) this)))
                ((Player) this).getCombatDefinitions().refreshBonuses();
            nextWorldTile = null;
            teleported = true;
            World.updateEntityRegion(this);
            if (needMapUpdate()) {
                loadMapRegions();
            } else if (this instanceof Player && lastPlane != getPlane()) {
                ((Player) this).setClientHasntLoadedMapRegion();
            }
            resetWalkSteps();
            return;
        }
        teleported = false;
        if (walkSteps.isEmpty()) {
            return;
        }
        if (this instanceof Player) { // emotes are special on rs, when using
            // one u will walk once emote done
            if (((Player) this).getEmotesManager().getNextEmoteEnd() >= Utils.currentTimeMillis()) {
                return;
            }
        }
        /*
         * if (this instanceof TorturedSoul) { // waste of process power personaly //
         * but meh. if (((TorturedSoul) this).switchWalkStep()) { return; } }
         */
        if (this instanceof Player && ((Player) this).getRunEnergy() <= 0) {
            setRun(false);
        }
        for (int stepCount = 0; stepCount < (run ? 2 : 1); stepCount++) {
            final Object[] nextStep = getNextWalkStep();
            if (nextStep == null)
                break;
            final int dir = (int) nextStep[0];
            if (((boolean) nextStep[3] && !World.checkWalkStep(getPlane(), getX(), getY(), dir, getSize())) || (this instanceof NPC && !canWalkNPC(getX() + Utils.DIRECTION_DELTA_X[dir], getY() + Utils.DIRECTION_DELTA_Y[dir])) || !canMove(dir) || (this instanceof Player && !((Player) this).getControlerManager().canMove(dir))) {
                resetWalkSteps();
                break;
            }
            if (stepCount == 0) {
                nextWalkDirection = dir;
            } else {
                nextRunDirection = dir;
                if (this instanceof Player) {
                    ((Player) this).drainRunEnergy();
                }
            }
            moveLocation(Utils.DIRECTION_DELTA_X[dir], Utils.DIRECTION_DELTA_Y[dir], 0);
            if (run && stepCount == 0) { // fixes impossible steps
                Object[] previewStep = previewNextWalkStep();
                if (previewStep == null)
                    break;
                int previewDir = (int) previewStep[0];
                if (Utils.getPlayerRunningDirection(Utils.DIRECTION_DELTA_X[dir] + Utils.DIRECTION_DELTA_X[previewDir],
                        Utils.DIRECTION_DELTA_Y[dir] + Utils.DIRECTION_DELTA_Y[previewDir]) == -1)
                    break;
            }
        }
        World.updateEntityRegion(this);
        if (needMapUpdate()) {
            loadMapRegions();
        }
    }
    
    private Object[] previewNextWalkStep() {
        Object[] step = walkSteps.peek();
        return step;
    }

    @Override
    public void moveLocation(final int xOffset, final int yOffset, final int planeOffset) {
        super.moveLocation(xOffset, yOffset, planeOffset);
        direction = Utils.getFaceDirection(xOffset, yOffset);
    }

    public boolean needMapUpdate() {
        return needMapUpdate(lastLoadedMapRegionTile) || needMapUpdate(this)/* || needMapUpdate2(this)*/;
    }

    private boolean needMapUpdate(WorldTile tile) {
        int lastMapRegionX = tile.getChunkX();
        int lastMapRegionY = tile.getChunkY();
        int regionX = getChunkX();
        int regionY = getChunkY();
        int size = ((Settings.MAP_SIZES[getMapSize()] >> 3) / 2) - 1;
        return Math.abs(lastMapRegionX - regionX) >= size || Math.abs(lastMapRegionY - regionY) >= size;
    }
    
    private boolean needMapUpdate2(WorldTile tile) {
        int[] baseChunk = MapUtils.decode(Structure.CHUNK, sceneBaseChunkId);
        // chunks length - offset. if within 16 tiles of border it updates map
        int limit = Settings.MAP_SIZES[getMapSize()] / 8 - 2;

        int offsetX = tile.getChunkX() - baseChunk[0];
        int offsetY = tile.getChunkY() - baseChunk[1];

        return offsetX < 2 || offsetX >= limit || offsetY < 2 || offsetY >= limit;
    }
    
    public void processReceivedDamage() {
        for (Map.Entry<Entity, Integer> entry : receivedDamage.entrySet()) {
            Entity source = entry.getKey();
            Integer damage = entry.getValue();
            if (damage == null || source.hasFinished()) {
                receivedDamage.remove(source);
                continue;
            }
            damage--;
            if (damage == 0) {
                receivedDamage.remove(source);
                continue;
            }
            receivedDamage.put(source, damage);
        }
    }

    public void processReceivedHits() {
        if (this instanceof Player) {
            if (((Player) this).getEmotesManager().getNextEmoteEnd() >= Utils.currentTimeMillis()) {
                return;
            }
        }
        Hit hit;
        int count = 0;
        while ((hit = getReceivedHits().poll()) != null && count++ < 10) {
            processHit(hit);
        }
    }

    public void removeDamage(final Entity entity) {
        receivedDamage.remove(entity);
    }

    public void removeHitpoints(final Hit hit) {
        if (isDead() || hit.getLook() == HitLook.ABSORB_DAMAGE) {
            return;
        }
        if ((boolean) temporaryAttributes.getOrDefault("teleporting", false)) {
            hit.setDamage(0);
        }
        if (hit.getLook() == HitLook.HEALED_DAMAGE) {
            heal(hit.getDamage());
            return;
        }
        if (hit.getDamage() > hitpoints) {
            hit.setDamage(hitpoints);
        }
        Entity source = hit.getSource();
        if (source != null) {
            if (source instanceof Familiar) {
                if (source != null && ((Familiar) source).getOwner() != null) {
                    source = ((Familiar) source).getOwner();
                }
            }
            if (source instanceof Pet && ((Pet) source).getOwner() != null)
                source = ((Pet) source).getOwner();
            if (source instanceof DreadNip && ((DreadNip) source).getOwner() != null)
                source = ((DreadNip) source).getOwner();
            if (this instanceof NPC && source == this) {
                Entity mostDamage = this.getMostDamageReceivedSourcePlayer();
                source = mostDamage != null && mostDamage != this ? mostDamage : source;
            }
        }
        addReceivedDamage(source, hit.getDamage());
        setHitpoints(hitpoints - hit.getDamage());
        if (hitpoints <= 0) {
            sendDeath(source);
        } else if (this instanceof Player) {
            final Player player = (Player) this;
            if (player.getEquipment().getRingId() == 2550 && source != null && source != player && hit.getDamage() > 0) {
                hit.getSource().applyHit(new Hit(player, (int) (hit.getDamage() * 0.1), HitLook.REFLECTED_DAMAGE));
            }
            if (player.getEquipment().getAmuletId() != 11090 && player.getEquipment().getRingId() == 2570 && player.getHitpoints() <= player.getMaxHitpoints() * 0.1) {
                Magic.sendItemTeleportSpell(player, true, Transportation.EMOTE, Transportation.GFX, 4, player.getHomeTile());
                player.getEquipment().deleteItem(2570, 1);
                player.getPackets().sendGameMessage("Your ring of life saves you, but is destroyed in the process.");
            }
            if (player.getPrayer().hasPrayersOn()) {
                if ((hitpoints < player.getMaxHitpoints() * 0.1) && player.getPrayer().usingPrayer(0, 15)) {
                    setNextGraphics(new Graphics(436));
                    setHitpoints((int) (hitpoints + player.getSkills().getLevelForXp(Skills.PRAYER) * 2.5));
                    player.getSkills().set(Skills.PRAYER, 0);
                    player.getPrayer().setPrayerpoints(0);
                } else if (player.getEquipment().getAmuletId() != 11090 && player.getEquipment().getRingId() == 2570 && player.getHitpoints() <= player.getMaxHitpoints() * 0.1) {
                    Magic.sendItemTeleportSpell(player, true, Transportation.EMOTE, Transportation.GFX, 4, player.getHomeTile());
                    player.getEquipment().deleteItem(2570, 1);
                    player.getPackets().sendGameMessage("Your ring of life saves you, but is destroyed in the process.");
                }
            }
            if (player.getEquipment().getAmuletId() == 11090 && player.getHitpoints() <= player.getMaxHitpoints() * 0.2) {
                player.heal((int) (player.getMaxHitpoints() * 0.3));
                player.getEquipment().deleteItem(11090, 1);
                player.getPackets().sendGameMessage("Your pheonix necklace heals you, but is destroyed in the process.");
            }
        }
    }

    public void reset() {
        reset(true);
    }

    public void reset(final boolean attributes) {
        setHitpoints(getMaxHitpoints());
        getReceivedHits().clear();
        resetCombat();
        walkSteps.clear();
        poison.reset();
        resetReceivedDamage();
        setAttackedBy(null);
        setAttackedByDelay(0);
        setAttackingDelay(0);
        effectsManager.resetEffects();
        if (attributes) {
            temporaryAttributes.clear();
        }
        temporaryModifiersManager.clear();
        prayerDebuffs.clear();
    }

    public void resetCombat() {
        attackedBy = null;
        attackedByDelay = 0;
        freezeDelay = 0;
        prayerDebuffs.clear();
    }

    public void deathResetCombat() {
        attackedBy = null;
        attackedByDelay = 0;
        if (this instanceof Player) {
            Player player = (Player) this;
            Perk taunting = player.getInventionManager().hasPerk(Perks.TAUNTING);
            if (taunting != null && Math.random() <= 0.10 && player.getAggressiveDelay() <= 0) {
                player.setAggressiveDelay(50);
                player.getBuffDebuffTimersManager().addTimer(Timer.AGGRESSION_POTION_ACTIVE, 50 * 600);
                player.sendMessage("You receive Aggression potion effects for 30 seconds thanks to your taunting perk.", true);
            }
            Perk invigorating = player.getInventionManager().hasPerk(Perks.INVIGORATING);
            if (invigorating != null && Math.random() <= 0.05) 
                player.getPrayer().restorePrayer(15 * invigorating.getRank());

        }
    }

    public void resetMasks() {
        nextAnimation = null;
        nextGraphics1 = null;
        nextGraphics2 = null;
        nextGraphics3 = null;
        nextGraphics4 = null;
        if (nextWalkDirection == -1) {
            nextFaceWorldTile = null;
        }
        nextForceMovement = null;
        nextNative950ForceMovement = null; // only the mask snapshot; scheduled endpoints remain live
        nextNative950ForceMaskGeneration = 0;
        native950ForceArrivalGeneration = 0;
        nextForceTalk = null;
        nextFaceEntity = -2;
        nextColour = null;
        needTargetInformationUpdate = false;
        nextHits.clear();
        nextHitBars.clear();
    }

    public void resetReceivedDamage() {
        receivedDamage.clear();
    }

    public void resetWalkSteps() {
        walkSteps.clear();
    }

    public boolean restoreHitPoints() {
        final int maxHp = getMaxHitpoints();
        if (hitpoints > maxHp) {
            setHitpoints(hitpoints - 1);
            return true;
        } else if (hitpoints < maxHp) {
            setHitpoints(hitpoints + 1);
            if (this instanceof Player) {
                final Player player = (Player) this;
                if (player.getPrayer().usingPrayer(0, 8) && hitpoints < maxHp) {
                    setHitpoints(hitpoints + 1);
                } else if (player.getPrayer().usingPrayer(0, 18) && hitpoints < maxHp) {
                    setHitpoints(hitpoints + (hitpoints + 4 > maxHp ? maxHp - hitpoints : 4));
                }
            }
            return true;
        }
        return false;
    }

    public abstract void sendDeath(Entity source);

    public void setFinished(final boolean finished) {
        this.finished = finished;
    }

    public void setFrozeBlocked(final int time) {
        frozenBlocked = time;
    }

    public void setNextAnimationForce(final Animation nextAnimation) {
        this.nextAnimation = nextAnimation;
    }

    public void setNextAnimationNoPriority(final Animation nextAnimation) {
        if (lastAnimationEnd > Utils.currentTimeMillis()) {
            return;
        }
        setNextAnimation(nextAnimation);
    }

    public void setNextGraphics(final Graphics nextGraphics) {
        if (nextGraphics == null) {
            if (nextGraphics4 != null) {
                nextGraphics4 = null;
            } else if (nextGraphics3 != null) {
                nextGraphics3 = null;
            } else if (nextGraphics2 != null) {
                nextGraphics2 = null;
            } else {
                nextGraphics1 = null;
            }
        } else {
            if (nextGraphics.equals(nextGraphics1) || nextGraphics.equals(nextGraphics2) || nextGraphics.equals(nextGraphics3) || nextGraphics.equals(nextGraphics4)) {
                return;
            }
            if (nextGraphics1 == null) {
                nextGraphics1 = nextGraphics;
            } else if (nextGraphics2 == null) {
                nextGraphics2 = nextGraphics;
            } else if (nextGraphics3 == null) {
                nextGraphics3 = nextGraphics;
            } else {
                nextGraphics4 = nextGraphics;
            }
        }
    }

    public void forceSetNextGraphics(final Graphics nextGraphics) {
        if (nextGraphics == null) {
            if (nextGraphics4 != null) {
                nextGraphics4 = null;
            } else if (nextGraphics3 != null) {
                nextGraphics3 = null;
            } else if (nextGraphics2 != null) {
                nextGraphics2 = null;
            } else {
                nextGraphics1 = null;
            }
        } else {
            if (nextGraphics.equals(nextGraphics1) || nextGraphics.equals(nextGraphics2) || nextGraphics.equals(nextGraphics3) || nextGraphics.equals(nextGraphics4)) {
                return;
            }
            if (nextGraphics1 == null) {
                nextGraphics1 = nextGraphics;
            } else if (nextGraphics2 == null) {
                nextGraphics2 = nextGraphics;
            } else if (nextGraphics3 == null) {
                nextGraphics3 = nextGraphics;
            } else {
                nextGraphics4 = nextGraphics;
            }
        }
    }


    public long getAttackingDelay() {
        return attackingDelay;
    }

    public void setAttackingDelay(final long attackingDelay) {
        this.attackingDelay = attackingDelay;
    }

    public boolean isUnderCombat() {
        return getAttackingDelay() + 6000 >= Utils.currentTimeMillis() || getAttackedByDelay() + 6000 >= Utils.currentTimeMillis();
    }

    public void resetReceivedHits() {
        nextHits.clear();
        getReceivedHits().clear();
    }

    private boolean cantDoDefenceEmote;

    public boolean isCantDoDefenceEmote() {
        return cantDoDefenceEmote;
    }

    public void setCantDoDefenceEmote(final boolean cantDoDefenceEmote) {
        this.cantDoDefenceEmote = cantDoDefenceEmote;
    }

    public ConcurrentLinkedQueue<Hit> getReceivedHits() {
        return receivedHits;
    }

    public void setReceivedHits(final ConcurrentLinkedQueue<Hit> receivedHits) {
        this.receivedHits = receivedHits;
    }

    /**
     * Gets the aggressiveOnYou.
     *
     * @return the aggressiveOnYou
     */
    public ArrayList<Entity> getAggressiveOnYou() {
        return aggressiveOnYou;
    }

    /**
     * Sets the aggressiveOnYou.
     *
     * @param aggressiveOnYou the aggressiveOnYou to set
     */
    public void setAggressiveOnYou(ArrayList<Entity> aggressiveOnYou) {
        this.aggressiveOnYou = aggressiveOnYou;
    }

    private EffectsManager effectsManager;

    public EffectsManager getEffectsManager() {
        return effectsManager;
    }

    private transient Colour nextColour;

    public Colour getNextColour() {
        return nextColour;
    }

    public void setNextColour(Colour nextColour) {
        this.nextColour = nextColour;
    }

    public WorldTile getMiddleWorldTile() {
        int size = getSize();
        return size == 1 ? this : new WorldTile(getCoordFaceX(size), getCoordFaceY(size), getPlane());
    }

    private transient int sceneBaseChunkId;

    public int getSceneBaseChunkId() {
        return sceneBaseChunkId;
    }

    private transient Map<Entity, Integer[]> prayerDebuffs;

    public boolean increasePrayerDebuff(Entity source, int debuffIndex, int intialValue, int maxValue) {
        if (source == null)
            return false;
        if (!prayerDebuffs.containsKey(source)) {
            Integer[] debuffs = new Integer[7];
            debuffs[debuffIndex] = intialValue;
            prayerDebuffs.put(source, debuffs);
            return true;
        }
        boolean increased = true;
        Integer[] debuffs = prayerDebuffs.remove(source);
        if (debuffs[debuffIndex] == null)
            debuffs[debuffIndex] = intialValue;
        else {
            int increament = 1;
            if (source instanceof Player) {
                Perk flanking = ((Player) source).getInventionManager().hasPerk(Perks.FLANKING);
                if (flanking != null)
                    increament = 2;
            }
            increased = debuffs[debuffIndex] + increament < maxValue;
            debuffs[debuffIndex] = debuffs[debuffIndex] + increament >= maxValue ? maxValue : debuffs[debuffIndex] + increament;
        }
        prayerDebuffs.put(source, debuffs);
        return increased;
    }

    public int getPrayerDebuff(int debuffIndex) {
        if (prayerDebuffs.isEmpty())
            return 0;
        int totalDebuff = 0;
        Iterator<Integer[]> iter = prayerDebuffs.values().iterator();
        while (iter.hasNext()) {
            Integer[] debuffs = iter.next();
            totalDebuff += debuffs[debuffIndex] == null ? 0 : debuffs[debuffIndex];
        }
        return totalDebuff;
    }

    private int extraHitPoints = 0;

    public int getExtraHitPoints() {
        return extraHitPoints;
    }

    public void setExtraHitPoints(int extraHitPoints) {
        this.extraHitPoints = extraHitPoints;
    }
    
    public abstract String getName();

    public abstract int getCombatLevel();
    
    private transient boolean needTargetInformationUpdate;
    
    public boolean isNeedTargetInformationUpdate() {
        return needTargetInformationUpdate;
    }


}