package com.rs.game.activities.rots;

import com.rs.cores.CoresManager;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.MapBuilder;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activities.rots.effects.Empowerment;
import com.rs.game.activities.rots.effects.ShadowDrag;
import com.rs.game.activities.rots.npcs.AhrimNPC;
import com.rs.game.activities.rots.npcs.DharokNPC;
import com.rs.game.activities.rots.npcs.GuthanNPC;
import com.rs.game.activities.rots.npcs.KarilNPC;
import com.rs.game.activities.rots.npcs.RiseOfTheSixNPC;
import com.rs.game.activities.rots.npcs.ToragNPC;
import com.rs.game.activities.rots.npcs.VeracNPC;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/**
 * @author Kris | 3. sept 2017 : 23:33.06
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public final class RiseOfTheSix {

    public static final int MIN_TEAM_SIZE = 2;

    private final ArrayList<Player> players, soulBind;
    private final RiseOfTheSixNPC[] wights;
    private final transient RiseOfTheSixMap map;
    private boolean shadowRealm;
    private final WorldTile[] pressurePlates;
    private RiseOfTheSixNPC shadowWight;
    private int shadowDrag, portalDashTeleports, stage;
    private int[] shadowBoundChunks, allMapRegionIds;
    private ArrayList<WorldObject> smokeBuffs;
    private final List<ShadowPit> pits = new ArrayList<ShadowPit>(), removedPits = new ArrayList<ShadowPit>();
    private final Map<Long, WorldObject> obstacles = new HashMap<Long, WorldObject>();
    private final List<RiseOfTheSixNPC> desertedWights = new ArrayList<RiseOfTheSixNPC>();
    private final String password;

    public static boolean FULL_TEAM_REQUIRED = true;

    public RiseOfTheSix(final String password) {
        this.password = password;
        players = new ArrayList<Player>();
        soulBind = new ArrayList<Player>();
        wights = new RiseOfTheSixNPC[6];
        map = new RiseOfTheSixMap();
        pressurePlates = new WorldTile[]{getWorldTile(12, 11), getWorldTile(12, 10), getWorldTile(12, 5), getWorldTile(12, 4)};
        process();
    }

    public final String getPassword() {
        return password;
    }

    /**
     * A 2D array containing shadow pit object locations; numbers within are offsets from the arena's
     * south-western corner (0, 0 tile).
     * First row is the western side, second row is eastern side.
     */
    public static final int[][] SHADOW_PITS = new int[][]{
            {22, 14}, {22, 18}, {22, 22}, {22, 26}, {27, 14}, {27, 18}, {27, 22}, {27, 26},
            {39, 14}, {39, 18}, {39, 22}, {39, 26}, {44, 14}, {44, 18}, {44, 22}, {44, 26}
    };

    /**
     * A 2D array containing possible rotation combinations. Only contains compositions per side, not their
     * locations per side.
     * First three numbers are indexes to the correct brothers on the western side, the rest are eastern.
     */
    public static final int[][] ROTATIONS = new int[][]{
            {0, 4, 5, 3, 1, 2}, {0, 5, 3, 4, 1, 2}, {4, 1, 2, 0, 5, 3}, {1, 2, 3, 0, 4, 5},
            {4, 1, 3, 0, 2, 5}, {1, 4, 5, 0, 2, 3}, {1, 3, 5, 0, 2, 4}, {4, 3, 2, 0, 5, 1},
            {4, 5, 2, 0, 1, 3}, {3, 5, 2, 0, 4, 1}, {4, 3, 5, 0, 1, 2}, {0, 2, 1, 4, 5, 3},
            {0, 4, 1, 5, 2, 3}, {0, 3, 1, 4, 2, 5}, {0, 5, 1, 4, 3, 2}, {0, 3, 2, 4, 1, 5},
            {0, 3, 2, 4, 1, 5}, {0, 5, 2, 4, 1, 3}, {0, 4, 3, 1, 2, 5},
    };

    /**
     * Adds a new obstacle to the list when a player uses it
     *
     * @param time delay until the obstacle can be used again
     * @param obstacle object
     */
    public final void addObstacle(final long time, final WorldObject obstacle) {
        obstacles.put(time, obstacle);
    }

    /**
     * Whether the player can use the obstacle or it's being used by someone else at the given time.
     *
     * @param player attempting to use the obstacle.
     * @param obstacle object
     * @return true or false.
     */
    public final boolean canUseObstacle(final Player player, final WorldObject obstacle) {
        for (final Entry<Long, WorldObject> keyset : obstacles.entrySet()) {
            if (keyset.getValue().equals(obstacle) && keyset.getKey() > Utils.currentTimeMillis()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Adds a singular shadow pit to the side where ahrims currently is.
     *
     * @param npc ahrims
     */
    public final void addPit(final AhrimNPC npc) {
        final WorldTile center = getWorldTile(34, 20);
        final List<WorldTile> remainingTiles = new ArrayList<WorldTile>();
        loop:
        for (int i = (npc.getX() > center.getX() ? 8 : 0); i < (npc.getX() > center.getX() ? SHADOW_PITS.length : 8); i++) {
            final WorldTile tile = getWorldTile(SHADOW_PITS[i][0], SHADOW_PITS[i][1]);
            for (final ShadowPit pit : pits) {
                if (pit.getTileHash() == tile.getTileHash()) {
                    continue loop;
                }
            }
            remainingTiles.add(tile);
        }
        if (remainingTiles.isEmpty()) {
            return;
        }
        final WorldTile selected = remainingTiles.get(Utils.random(remainingTiles.size()));
        final WorldObject o = World.getObjectWithType(selected, 10) == null ? World.getObjectWithType(selected, 11) : World.getObjectWithType(selected, 10);
        npc.setLastPit(Utils.currentTimeMillis() + 25000);
        World.sendGraphics(npc, new Graphics(4405, 0, 0, 20), selected);
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                pits.add(new ShadowPit(o.getType() == 11 ? 88094 : 88093, o.getType(), o.getRotation(), selected));
            }
        }, 4);
    }

    /**
     * Inflates all opposite side shadow pits
     *
     * @param npc
     */
    public final void inflateOppositeSide(final WorldTile npc) {
        final WorldTile center = getWorldTile(33, 20);
        final List<WorldTile> remainingTiles = new ArrayList<WorldTile>();
        loop:
        for (int i = (npc.getX() < center.getX() ? 8 : 0); i < (npc.getX() < center.getX() ? SHADOW_PITS.length : 8); i++) {
            final WorldTile tile = getWorldTile(SHADOW_PITS[i][0], SHADOW_PITS[i][1]);
            for (final ShadowPit pit : pits) {
                if (pit.getTileHash() == tile.getTileHash()) {
                    continue loop;
                }
            }
            remainingTiles.add(tile);
        }
        if (remainingTiles.isEmpty()) {
            return;
        }
        for (final WorldTile t : remainingTiles) {
            final WorldObject o = World.getObjectWithType(t, 10) == null ? World.getObjectWithType(t, 11) : World.getObjectWithType(t, 10);
            pits.add(new ShadowPit(o.getType() == 11 ? 88094 : 88093, o.getType(), o.getRotation(), t));
        }
    }

    public final void addSmokePit(final WorldObject pit) {
        smokeBuffs.add(pit);
    }

    public final ArrayList<WorldObject> getSmokePits() {
        return smokeBuffs;
    }

    public final boolean initiatedFight() {
        return stage > 0;
    }

    public final ArrayList<Player> getSoulBoundPlayers() {
        return soulBind;
    }

    public final void addSoulBoundPlayer(final Player player) {
        soulBind.add(player);
    }

    public final void removeSoulBoundPlayer(final Player player) {
        soulBind.remove(player);
    }

    /**
     * Gets the direction of wight to face based on the existing direction and offset.
     * Increments/decrements at an angle of 22.5 degrees (16384 / 16 -> 1024 value)
     *
     * @param direction current direction.
     * @param offset direction offset to set the new direction to.
     * @return new direction.
     */
    public final int getDirection(final int direction, final int offset) {
        int dir = 0;
        if (direction + offset >= 0 && direction + offset < 16) {
            dir = direction + offset;
        } else if (direction + offset < 0) {
            dir = 16 - offset;
        } else if (direction + offset >= 16) {
            dir = offset - (16 - direction);
        }
        return dir;
    }

    public final boolean isCutsceneInitiated() {
        return stage > 2;
    }

    public final void setCutsceneInitiated() {
        stage++;
    }

    public final boolean withinShadowRealm() {
        return shadowRealm;
    }

    public final void setShadowRealm(final boolean value) {
        shadowRealm = value;
    }

    public final RiseOfTheSixMap getMap() {
        return map;
    }

    public final int getPortalDashTeleports() {
        return portalDashTeleports;
    }

    public final void incrementPortalDashTeleports() {
        portalDashTeleports++;
    }

    public final int getShadowDragTicks() {
        return shadowDrag / 50;
    }

    public final void resetPortalDashTeleports() {
        portalDashTeleports = 0;
    }

    public final ArrayList<Player> getPlayers() {
        return players;
    }

    public final void addPlayer(final Player player) {
        player.setForceMultiArea(true);
        players.add(player);
    }

    public final void removePlayer(final Player player) {
        player.setForceMultiArea(false);
        players.remove(player);
    }

    public final KarilNPC getKarils() {
        for (final RiseOfTheSixNPC n : wights) {
            if (n instanceof KarilNPC) {
                return (KarilNPC) n;
            }
        }
        return null;
    }

    public final AhrimNPC getAhrims() {
        for (final RiseOfTheSixNPC n : wights) {
            if (n instanceof AhrimNPC) {
                return (AhrimNPC) n;
            }
        }
        return null;
    }

    public final RiseOfTheSixNPC[] getWights() {
        return wights;
    }

    public final int getShadowDragHealth() {
        return shadowDrag;
    }

    public final NPC getShadowWight() {
        return shadowWight;
    }

    public final int[] getAllMapRegionIds() {
        return allMapRegionIds;
    }

    /**
     * Launches the fight - removes the fight start option, spawns all wights, sets the shadow wight at random,
     * and maps down the region ids of all the chunks used in the instance.
     */
    public final void initiateFight() {
        if (stage >= 2) {
            return;
        }
        stage++;
        final int day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        final int[] rotation = ROTATIONS[day - ((day / RiseOfTheSix.ROTATIONS.length)) * RiseOfTheSix.ROTATIONS.length];
        World.removeObject(World.getObject(getWorldTile(32, 18)));
        wights[rotation[0]] = new AhrimNPC(18538, getWorldTile(31, 18), -1, true, true, this);
        wights[rotation[1]] = new DharokNPC(18540, getWorldTile(31, 20), -1, true, true, this);
        wights[rotation[2]] = new GuthanNPC(18541, getWorldTile(31, 22), -1, true, true, this);
        wights[rotation[3]] = new ToragNPC(18544, getWorldTile(36, 18), -1, true, true, this);
        wights[rotation[4]] = new KarilNPC(18543, getWorldTile(36, 20), -1, true, true, this);
        wights[rotation[5]] = new VeracNPC(18545, getWorldTile(36, 22), -1, true, true, this);
        shadowWight = wights[Utils.random(6)];
        shadowWight.setNextForceTalk(new ForceTalk("You dare disturb the darkness!"));
        players.forEach(p -> p.sendMessage("<col=ff0000>" + shadowWight.getName() + "</col>: You dare disturb the darkness!"));
        shadowDrag = Utils.random(1000, 4000);
        allMapRegionIds = new int[map.getAllMapRooms().length];
        for (int i = 0; i < map.getAllMapRooms().length; i++) {
            allMapRegionIds[i] = new WorldTile(map.getAllMapRooms()[i][0], map.getAllMapRooms()[i][1], 0).getRegionId();
        }
    }


    /**
     * Builds the shadow realm when the team is thrown into the realm.
     */
    public final void buildShadowRealm() {
        shadowBoundChunks = MapBuilder.findEmptyChunkBound(6, 5);
        MapBuilder.copyAllPlanesMap(287, 752, shadowBoundChunks[0], shadowBoundChunks[1], 6, 5);
    }

    /**
     * Converts the location from normal realm to shadow realm.
     *
     * @param target whose location to convert.
     * @return shadow world tile.
     */
    public final WorldTile convertToShadowRealm(final Entity target) {
        final WorldTile normalRealm = getNormalRealmWorldTile(0, 0);
        final WorldTile shadowRealm = getShadowRealmWorldTile(0, 0);
        final int x = target.getX() - normalRealm.getX();
        final int y = target.getY() - normalRealm.getY();
        return new WorldTile(shadowRealm.getX() + x, shadowRealm.getY() + y, 1);
    }

    /**
     * Converts the location from shadow realm to normal realm.
     *
     * @param target whose location to convert.
     * @return regular world tile
     */
    public final WorldTile convertToRegularRealm(final Entity target) {
        if (target == null) {
            return null;
        }
        final WorldTile normalRealm = getNormalRealmWorldTile(0, 0);
        final WorldTile shadowRealm = getShadowRealmWorldTile(0, 0);
        final int x = target.getX() - shadowRealm.getX();
        final int y = target.getY() - shadowRealm.getY();
        return new WorldTile(normalRealm.getX() + x, normalRealm.getY() + y, 0);
    }

    private final WorldTile getNormalRealmWorldTile(final int x, final int y) {
        final int[] index = getMap().getIndex("entrance");
        return new WorldTile(index[0] + x, index[1] + y, 0);
    }

    private final WorldTile getShadowRealmWorldTile(final int x, final int y) {
        return new WorldTile((shadowBoundChunks[0] * 8) - 8 + x, (shadowBoundChunks[1] * 8) + y, 1);
    }

    /**
     * Gets a specific tile in the instance. Automatically converts to shadow realm if the players are in there.
     *
     * @param x offset
     * @param y offset
     * @return real tile.
     */
    public final WorldTile getWorldTile(final int x, final int y) {
        if (shadowRealm && shadowBoundChunks != null) {
            return getShadowRealmWorldTile(x, y);
        }
        final int[] index = getMap().getIndex("entrance");
        return new WorldTile(index[0] + x, index[1] + y, 0);
    }


    /**
     * Marks the direction offsets between each turn the NPC does. Array starts facing south.
     */
    public static final int[][] DIRS = new int[][]{
            {0, -10}, {-5, -10}, {-10, -10}, {-10, -5},
            {-10, 0}, {-10, 5}, {-10, 10}, {-5, 10},
            {0, 10}, {5, 10}, {10, 10}, {10, 5},
            {10, 0}, {10, -5}, {10, -10}, {5, -10}
    };

    /**
     * Generates a random possible player entity on the same side as the wight itself.
     *
     * @param npc wight.
     * @return new target.
     */
    public final Player generateRandomTarget(final RiseOfTheSixNPC npc) {
        final WorldTile center = getWorldTile(34, 20);
        final List<Player> plrs = new ArrayList<Player>();
        for (final Player p : players) {
            if (npc.getX() > center.getX() && p.getX() > center.getX() || npc.getX() < center.getX() && p.getX() < center.getX()) {
                plrs.add(p);
            }
        }
        if (plrs.size() == 0) {
            return null;
        }
        return plrs.get(Utils.random(plrs.size()));
    }

    /**
     * @param npc karils or ahrims.
     * @param left whether we check the left side of right.
     * @return whether the northern or southern tile, based on which is the closest.
     */
    public final WorldTile getNorthernTile(final RiseOfTheSixNPC npc, final boolean left) {
        return left ? getWorldTile(42, 24) : getWorldTile(25, 24);
    }

    /**
     * Processes all the events of the minigame.
     */
    private final void process() {
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                try {
                    if (checkFinish()) {
                        stop();
                        return;
                    } else if (stage == 0) {
                        checkPressurePlates();
                        return;
                    }
                    checkShadowPits();
                    checkShadowWight();
                    checkEmpowerment();
                    checkEscape();
                } catch (final Exception e) {
                    Logger.getGlobal().catching(e);
                }
            }
        }, 0, 0);
    }

    /**
     * Begins the final cutscene after defeating the wights. Transforms the area properly right before the
     * cutscene begins.
     *
     * @param name NPC name who was last to die.
     */
    public final void engulfWights(final String name) {
        setCutsceneInitiated();
        getPlayers().forEach(player -> {
            try {
                if (!player.isDead()) {
                    player.sendMessage("As you defeat " + name + ", the shadow engulfs the remaining wights!");
                    player.getCutscenesManager().play("RiseOfTheSixCutscene");
                }
            } catch (final Exception e) {
                Logger.getGlobal().error("Failure to play a cutscene:", e);
            }
        });
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                World.spawnObject(new WorldObject(88091, 10, 0, getWorldTile(32, 16)));
                World.removeObject(World.getObjectWithType(getWorldTile(18, 10), 10));
                World.removeObject(World.getObjectWithType(getWorldTile(41, 8), 10));
                World.removeObject(World.getObjectWithType(getWorldTile(13, 4), 10));
                World.removeObject(World.getObjectWithType(getWorldTile(13, 10), 10));
                World.spawnObject(new WorldObject(87996, 10, 1, getWorldTile(25, 20)));
                getPlayers().forEach(player -> {
                    if(player.isGroupIronman()) {
                        player.gimTracker.incrementBpGained(6);
                    }
                    increaseAndNotifyCompletedRuns(player);
                    if (player.withinDistance(getWorldTile(25, 20), 1)) {
                        if (!player.addWalkSteps(player.getX() - 1, player.getY(), 1)) {
                            if (!player.addWalkSteps(player.getX() + 1, player.getY(), 1)) {
                                if (!player.addWalkSteps(player.getX(), player.getY() + 1, 1)) {
                                    player.addWalkSteps(player.getX(), player.getY() - 1, 1);
                                }
                            }
                        }
                    }
                });
            }
        }, 1);
    }

    /**
     * Checks to see if the cutscene part has already initiated. If the condition is met and an escape isn't
     * already initiated, initiates a new escape sequence if any of the players are away from the main area.
     */
    private final void checkEscape() {
        if (stage != 3) {
            return;
        }
        for (final Player p : players) {
            if (p == null || p.isDead() || p.hasFinished()) {
                continue;
            }
            if (p.getX() < getWorldTile(16, 0).getX()) {
                initiateEscape();
                break;
            }
        }
    }

    /**
     * Checks to see if one side of the arena is empty of players. If this value returns true,
     * it'll continue to check if the empty side contains any NPCs. If that conditions also returns true,
     * all the wights on the empty side will initiate an empowering special which teleports them
     * over to the side with players.
     */
    private final void checkEmpowerment() {
        final WorldTile center = getWorldTile(34, 20);
        int westPlayerCount = 0;
        for (final Player p : players) {
            if (p == null || p.isDead() || p.hasFinished()) {
                continue;
            }
            if (p.getX() < center.getX()) {
                westPlayerCount++;
            }
        }
        if (westPlayerCount > 0 && westPlayerCount != players.size()) {
            return;
        }
        final boolean west = westPlayerCount == players.size();
        desertedWights.clear();
        for (final RiseOfTheSixNPC wight : wights) {
            if (wight == null || wight.isDead() || wight.hasFinished() || wight.getEffect() != null) {
                return;
            }
            if (west && wight.getX() > center.getX() || !west && wight.getX() < center.getX()) {
                desertedWights.add(wight);
            }
        }
        if (desertedWights.size() == 0) {
            return;
        }
        desertedWights.forEach(npc -> npc.setEffect(new Empowerment(4, npc, null)));
        players.forEach(p -> p.sendMessage(Colors.BROWN + "As there is no one on the other side of the portal, it empowers the Barrows Brothers to destroy everyone!"));
    }

    /**
     * Checks the shadow wight, whether its health has dropped below the trigger value of the shadow drag special.
     * If the health has dropped below the trigger, the wight will initiate a timer on the drag,
     * which upon running out will transport everyone over to the shadow realm in which the npcs deal
     * far higher damage.
     * Players are transported back into the regular realm after they're finished killing all wights in shadow.
     */
    private final void checkShadowWight() {
        if (shadowWight == null || shadowWight.isDead() || shadowWight.hasFinished() || shadowWight.getEffect() instanceof ShadowDrag
                || shadowWight.getHitpoints() > shadowDrag || players.size() == 0 || shadowRealm) {
            return;
        }
        if (shadowWight.getEffect() != null) {
            shadowWight.getEffect().setCancelled();
        }
        shadowWight.setEffect(new ShadowDrag(1, shadowWight, null));
    }

    /**
     * Checks if the arena is empty of players. It it's empty, all the wights are finishes instantly,
     * which also finishes all their pending effects if any exist (Effects all check if the npc has finishes and interrupts if so).
     * Map is destroyed six seconds after this occurs.
     *
     * @return whether finished or not.
     */
    private final boolean checkFinish() {
        if (players.size() > 0) {
            return false;
        }
        for (final RiseOfTheSixNPC n : wights) {
            if (n != null) {
                n.finish();
            }
        }
        map.clean();
        if (shadowBoundChunks != null) {
            CoresManager.getServiceProvider().executeWithDelay(() -> MapBuilder.destroyMap(shadowBoundChunks[0], shadowBoundChunks[1], 5, 6), 6000, TimeUnit.MILLISECONDS);
        }
        return true;
    }

    /**
     * Checks the pressure plates at the entrance of the arena.
     * All 4 players must be standing on one pressure plate each for it to activate. Once it does,
     * all players are teleported inside the arena itself where they may begin the minigame.
     */
    private final void checkPressurePlates() {
        if (FULL_TEAM_REQUIRED) {
            if (players.size() < MIN_TEAM_SIZE) {
                return;
            }
            int count = 0;
            for (final Player p : players) {
                    for (final WorldTile plate : pressurePlates) {
                        if (p.getTileHash() == plate.getTileHash()) {
                            count++;
                        }
                    }
            }
            if (count != players.size())
                return;
        }
        stage++;
        WorldTasksManager.schedule(new WorldTask() {
            private int ticks;

            @Override
            public void run() {
                if (ticks == 0) {
                    players.forEach(p -> {
                        p.lock(2);
                        p.setNextAnimation(new Animation(21917, -1, -1, -1, -1, 0));
                        p.setNextGraphics(new Graphics(4413));
                    });
                } else if (ticks == 1) {
                    players.forEach(p -> {
                        p.setNextAnimation(new Animation(21915));
                        p.setNextGraphics(new Graphics(4413));
                        p.setNextWorldTile(new WorldTile(p.getX() + 2, p.getY(), p.getPlane()));
                    });
                    stop();
                }
                ticks++;
            }
        }, 0, 0);
    }

    /**
     * Checks the shadow pits over the arena. Returns if none exists.
     * Purple shadow pits deal 50 tick damage to players standing on them,
     * red ones heal brothers when standing on them.
     * Saves finishes pits in a temporary list to avoid concurrency/looping issues.
     */
    private final void checkShadowPits() {
        if (pits.size() == 0) {
            return;
        }
        for (final ShadowPit pit : pits) {
            if (pit.process()) {
                if (pit.getId() == 88093) {
                    for (final Player p : players) {
                        if (p == null || p.isDead() || p.hasFinished()) {
                            continue;
                        }
                        if (p.withinDistance(pit, 1)) {
                            p.applyHit(new Hit(null, 50, HitLook.REGULAR_DAMAGE));
                        }
                    }
                } else if (pit.getId() == 88094) {
                    for (final RiseOfTheSixNPC wight : wights) {
                        if (wight == null || wight.isDead() || wight.hasFinished()) {
                            continue;
                        }
                        if (wight.withinDistance(pit, 1)) {
                            wight.applyHit(new Hit(null, 50, HitLook.HEALED_DAMAGE));
                        }
                    }
                }
            } else {
                removedPits.add(pit);
            }
        }
        pits.removeAll(removedPits);
        removedPits.clear();
    }

    /**
     * Initiates the escape sequence as soon as players go away from the center of the arena after
     * the end of the fight. Sequence consists of a 30 second run from the arena back to the entrance.
     * At the start of the sequence, all bridges break and different obstacles (vine, stepping stone, ledge)
     * are spawned next to the bridge. Only one person can go over an obstacle at a time and
     * vine and stepping stone disappear as soon as the person finishes going over them, ledge however is permanent.
     * Different size rocks also spawn in the area at random if there's enough space, blocking the path for
     * players. After all players leave (or die), the minigame finishes and the arena is destroyed.
     */
    public final void initiateEscape() {
        stage++;
        smokeBuffs = new ArrayList<WorldObject>();
        new RiseOfTheSixEscapeSequence(this).run();
    }

    private void increaseAndNotifyCompletedRuns(final Player player) {
        player.incrementRiseOfTheSixRunsCompleted();
        player.sendMessage("Barrows: Rise of the Six runs completed: " + Colors.wrap(Colors.RED, Utils.formatNumber(player.getRiseOfTheSixRunsCompleted())) + ".");
    }

}