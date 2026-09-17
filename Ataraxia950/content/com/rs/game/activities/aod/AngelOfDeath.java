package com.rs.game.activities.aod;

import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.MapBuilder;
import com.rs.game.NewProjectile;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activites.gim.GIM;
import com.rs.game.activities.aod.ability.ElementsInstantKill;
import com.rs.game.activities.aod.ability.SmokeWall;
import com.rs.game.activities.aod.npc.AoDNex;
import com.rs.game.activities.aod.npc.Crystal;
import com.rs.game.activities.aod.reward.AoDRewardGeneration;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.content.dropcollection.DropCollectionConstants;
import com.rs.game.player.content.hcim_news.HcimNewsManager;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Logger;
import com.rs.utils.Utils;
import lombok.Getter;
import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Instance class for Nex: Angel of Death.
 *
 * @author Kris | 27. sept 2017 : 21:08.35
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server
 * profile</a>}
 */
public final class AngelOfDeath {

    public AngelOfDeath(final Player player, final String password) {
        owner = player.getUsername();
        this.password = password;
        chunks = MapBuilder.findEmptyChunkBound(SIZE, SIZE);
        MapBuilder.copyAllPlanesMap(REGIONX, REGIONY, chunks[0], chunks[1], SIZE, SIZE);
        CENTER = getWorldTile(2848, 1824);
        nex = new AoDNex(24003, CENTER, this);
        allNpcs.add(nex);
        World.spawnObject(new WorldObject(100804, 10, 0, getWorldTile(2848, 1795)));
        addPlayer(player);
        NORTH_WEST = getWorldTile(2829, 1843);
        NORTH_EAST = getWorldTile(2867, 1843);
        SOUTH_WEST = getWorldTile(2829, 1805);
        SOUTH_EAST = getWorldTile(2867, 1805);
        process();
        INSTANCES.add(this);
        order = new int[4];
        for (int i = 0; i < order.length; i++)
            order[i] = -1;
    }

    private static final int REGIONX = 350, REGIONY = 222, SIZE = 11;
    public static final int MAINDROP = 1, SIDEDROP = 2;
    public static final Queue<AngelOfDeath> INSTANCES = new ConcurrentLinkedQueue<AngelOfDeath>();
    private final String owner;
    private final String password;
    @Getter
    private boolean gimInside;
    private final int[] chunks;
    private final List<Player> players = new CopyOnWriteArrayList<Player>();
    private final List<Player> playersInFight = new CopyOnWriteArrayList<Player>();
    private final List<WorldTile[]> tileSets = new ArrayList<WorldTile[]>();
    private final List<ShadowOrb> shadowOrbs = new ArrayList<ShadowOrb>();
    private final List<Player> hitPlayers = new ArrayList<Player>();
    private final List<WorldTile> bombs = new CopyOnWriteArrayList<WorldTile>();
    private final List<NPC> allNpcs = new ArrayList<NPC>();
    private final Crystal[] crystals = new Crystal[4];
    public final WorldTile CENTER, NORTH_EAST, NORTH_WEST, SOUTH_EAST, SOUTH_WEST;
    public static final Graphics RESET_GFX = new Graphics(-1);
    private final int[] order;
    private final AoDNex nex;
    private int stage;

    /**
     * Generates a map of rewards for up to top 10 players of the fight. First 7
     * players will get a shot at the main drop table which has enhanced rewards.
     * The remaining 3 players (if eligible, of course) will get a shot at the side
     * drop table. Side drop table also contains the rare drops but the wand as well
     * as the core are at a much lower rate. Any players past 10 will not receive a
     * drop. The list of players eligible for a drop is based on their damage done
     * to Nex.
     *
     * @param damageSet map of entities who've done damage to Nex.
     */
    public void generateRewards(final Map<Entity, Integer> damageSet) {
        final AoDRewardGeneration rewards = new AoDRewardGeneration(playersInFight, damageSet);
        rewards.getRewardsMap().forEach((k, v) -> {
            k.getDropCollectionHandler().handleBossKills(v, DropCollectionConstants.AOD_ID);
            k.catchDrop(v, () -> World.addGroundItem(v, CENTER, k, true, 180));
            final String name = v.getName().toLowerCase();
            if (name.contains("praesul") || name.contains("intricate") || name.contains("imperium")) {
                val AorAn = name.contains("praesul") ? "a " : "an ";
                World.sendWorldMessage(Colors.ORANGE + "News: " + k.getDisplayName() + " received " + AorAn + v.getName() + " drop from Nex: Angel of Death!", false);
                HcimNewsManager.getInstance().addNews(k,"<#player> received " + AorAn + v.getName() + " drop from Nex: Angel of Death!");
            } else if (name.contains("reeve")) {
                World.sendWorldMessage("<img=6><col=008FB2> News: " + Colors.PINK + k.getDisplayName() + " has received Reeves, the Nex: Angel of Death pet!", false);
                HcimNewsManager.getInstance().addNews(k,"<#player> got Reeves, the Nex: Angel of Death pet");
            } else if (name.contains("monkey")) {
                World.sendWorldMessage("<img=6><col=008FB2> News: " + Colors.PINK + k.getDisplayName() + " has received Reeves, the... siike! Monkey nuts!", false);
            }
        });
    }

    /**
     * Gets a specific crystal based on the id from the list of crystals.
     *
     * @param id id of the crystal to get.
     * @return Crystal NPC.
     */
    public Crystal getCrystal(final int id) {
        for (int i = 0; i < 4; i++) {
            if (crystals[i] == null) {
                continue;
            }
            if (crystals[i].getId() == id) {
                return crystals[i];
            }
        }
        return null;
    }

    /**
     * Spawns a NPC and adds it to the list to ensure all NPCs infact are removed
     * from the game at the end of an instance.
     *
     * @param npc npc to add.
     */
    public void spawnNPC(final NPC npc) {
        allNpcs.add(npc);
    }

    /**
     * Adds a crystal to the array of crystals.
     *
     * @param crystal crystal to add.
     */
    public void addCrystal(final Crystal crystal) {
        for (int i = 0; i < 4; i++) {
            if (crystals[i] == null) {
                crystals[i] = crystal;
                return;
            }
        }
    }

    /**
     * Adds a player into the instance. Sets their view to large-scene which means
     * the viewport of NPCs is increased by 81 times. It's reset to default after
     * leaving the instance. By default, a player is not included within the fight,
     * but only in the list of players in the instance in general - can be either
     * within the fight or at the balcony watching the fight.
     *
     * @param player entity to add to the list of instance players.
     */
    public void addPlayer(final Player player) {
        if (player.isGroupIronman()) {
            gimInside = true;
        }
        player.getControlerManager().forceStop();
        player.getTemporaryAttributtes().put("aodinstance", this);
        player.getControlerManager().startControler("AoDController");
        players.add(player);
        player.setLargeSceneView(true);
        player.setForceMultiArea(true);
        player.setNextWorldTile(getWorldTile(2848, 1792));
    }

    /**
     * Removes a player from the instance completely. Resets their viewport to
     * normal.
     *
     * @param player entity to remove.
     */
    public void removePlayer(final Player player) {
        player.setLargeSceneView(false);
        player.setForceMultiArea(false);
        players.remove(player);
        playersInFight.remove(player);
        if (players.isEmpty() && playersInFight.isEmpty()) {
            stage++;
            INSTANCES.remove(this);
            CoresManager.getServiceProvider().executeWithDelay(() -> MapBuilder.destroyMap(chunks[0], chunks[1], 11, 11), 6, TimeUnit.SECONDS);
        } else if(gimInside) {
            gimInside = false;
            for (Player next : players) {
                if (next != null && next.isGroupIronman()) {
                    gimInside = true;
                    break;
                }
            }
        }
    }

    /**
     * Sets the order of the praesul as they die. Used to determine the order in
     * which the crystals within the pillars can be attacked.
     *
     * @param id Praesul npc id.
     */
    public void setOrder(final int id) {
        for (int i = 0; i < order.length; i++) {
            if (order[i] == -1) {
                order[i] = id;
                break;
            }
        }
    }

    /**
     * Clears the order of those crystals that have been defeated. Used to determine
     * which crystal can still be attacked.
     */
    public void finishOrder() {
        for (int i = 0; i < order.length; i++) {
            if (order[i] > 0) {
                order[i] = 0;
                break;
            }
        }
    }

    /**
     * Gets the id of the next vulnerable crystal, based on the order in which the
     * Praesul died.
     *
     * @return id of the crystal.
     */
    public int getAvailableCrystal() {
        boolean allZero = true;
        for (int i = 0; i < order.length; i++) {
            if (order[i] != 0) {
                allZero = false;
            }
            if (order[i] == 24010) {
                return 24017;
            } else if (order[i] == 24011) {
                return 24016;
            } else if (order[i] == 24012) {
                return 24018;
            } else if (order[i] == 24013) {
                return 24019;
            }
        }
        return allZero ? -1 : 0;
    }

    /**
     * Initiates a task throughout the fight which will process the arena in
     * general. Checks for spawned shadow orbs - if any players are stepping on
     * them, they will be damaged. Checks for smoke wall - if any players are inside
     * the smoke, they will be damaged. Checks for explosions occurring throughout
     * the arena that are left by the players. Task finishes once the instance has
     * been cleared of all players, after which the instance is completely wiped off
     * the map.
     */
    public void process() {
        WorldTasksManager.schedule(new WorldTask() {
            private final List<ShadowOrb> toRemove = new ArrayList<ShadowOrb>();
            private int ticks;

            @Override
            public void run() {
                if (stage >= 5) {
                    allNpcs.forEach(n -> {
                        if (n != null && !n.hasFinished()) {
                            n.finish();
                        }
                    });
                    stop();
                    return;
                }
                if (!shadowOrbs.isEmpty()) {
                    shadowOrbs.forEach(orb -> {
                        if (!orb.process()) {
                            toRemove.add(orb);
                        }
                    });
                    if (!toRemove.isEmpty()) {
                        shadowOrbs.removeAll(toRemove);
                        toRemove.clear();
                    }
                }
                hitPlayers.clear();
                if (!tileSets.isEmpty()) {
                    playersInFight.forEach(p -> {
                        for (int i = 0; i < tileSets.size(); i++) {
                            if (tileSets.get(i) == null) {
                                continue;
                            }
                            for (int x = 0; x < tileSets.get(i).length; x++) {
                                if (tileSets.get(i)[x] != null && p.withinDistance(tileSets.get(i)[x], 1)) {
                                    if (!hitPlayers.contains(p)) {
                                        hitPlayers.add(p);
                                    }
                                }
                            }
                        }
                    });
                    hitPlayers.forEach(p -> p.applyHit(new Hit(null, Utils.random(25, 50), HitLook.MAGIC_DAMAGE)));
                }
                if (!bombs.isEmpty() && ticks % 3 == 0 && !nex.isDead()) {
                    bombs.forEach(bomb -> {
                        sendGraphics(ElementsInstantKill.EXPLOSION, bomb);
                        playersInFight.forEach(p -> {
                            if (p.withinDistance(bomb, 3)) {
                                p.applyHit(new Hit(null, 90, HitLook.REGULAR_DAMAGE));
                            }
                        });
                    });
                }
                ticks++;
            }
        }, 0, 0);
    }

    /**
     * Adds a list of smoke/fire walls in the instance. Smoke is added with 15
     * millisecond intervals for smoothness.
     *
     * @param tiles an array of the tiles in which the graphics are sent.
     */
    public void addFires(final WorldTile[] tiles) {
        final List<WorldTile> t = new ArrayList<WorldTile>();
        try {
            for (int i = 0; i < tiles.length; i++) {
                final WorldTile dest = tiles[i];
                if (dest == null) {
                    continue;
                }
                loop:
                for (int x = 0; x < tileSets.size(); x++) {
                    for (int a = 0; a < tileSets.get(x).length; a++) {
                        if (tileSets.get(x)[a] != null && tileSets.get(x)[a].getHash() == dest.getHash()) {
                            playersInFight.forEach(p -> p.getPackets().sendGraphics(new Graphics(-1), dest));
                            break loop;
                        }
                    }
                }
                t.add(dest);
            }
            tileSets.add(tiles);
        } catch (final Exception e) {
        }
        CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
            @Override
            public boolean repeat() {
                try {
                    if (t.isEmpty()) {
                        return false;
                    }
                    sendGraphics(SmokeWall.SMOKE, t.remove(0));
                } catch (final Exception e) {
                    Logger.getGlobal().catching(e);
                }
                return true;
            }

        }, 0, 15, TimeUnit.MILLISECONDS);
    }

    /**
     * Adds a shadow bomb to the list of processed bombs.
     *
     * @param tile where the bomb is.
     */
    public void addBomb(final WorldTile tile) {
        bombs.add(tile);
    }

    /**
     * Removes an array of fires from the list of tilesets - used for Smoke wall.
     *
     * @param tiles array of tiles - same array that was added a minute prior.
     */
    public void removeFires(final WorldTile[] tiles) {
        tileSets.remove(tiles);
    }

    /**
     * Gets the main boss NPC - AoD: Nex.
     *
     * @return nex
     */
    public AoDNex getNex() {
        return nex;
    }

    /**
     * Gets the list of shadow orb objects.
     *
     * @return list of shadow orbs.
     */
    public List<ShadowOrb> getShadowOrbs() {
        return shadowOrbs;
    }

    /**
     * Sends a projectile to all players contained within the instance. Using this
     * method over World class methods due to the processing time as well as the
     * issue of the arena being too large, meaning players wouldn't be able to see
     * projectiles sent from one corner to another.
     *
     * @param projectile projectile to send.
     */
    public void sendProjectile(final NewProjectile projectile) {
        players.forEach(player -> player.getPackets().sendTestProjectile(projectile));
    }

    /**
     * Sends graphics to a tile, player or npc. Sent to all players in the instance
     * to reduce process time over World methods.
     *
     * @param graphics graphics to send.
     * @param target tile, player or npc object which will receive the graphics.
     */
    public void sendGraphics(final Graphics graphics, final Object target) {
        players.forEach(player -> player.getPackets().sendGraphics(graphics, target));
    }

    /**
     * Sends a message to all players in the instance.
     *
     * @param message message to send.
     */
    public void sendMessage(final String message) {
        players.forEach(player -> player.sendMessage(message));
    }

    /**
     * Gets a list of all players in the instance, including those watching.
     *
     * @return array list of players in instance.
     */
    public List<Player> getAllPlayers() {
        return players;
    }

    /**
     * Gets a list of players that have entered the fight (gone off the balcony)
     *
     * @return arraylist of players in fight.
     */
    public List<Player> getPlayers() {
        return playersInFight;
    }

    /**
     * Gets the username of the owner of the instance. Used for joining instanced.
     *
     * @return username of instance's owner.
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Gets the password to the instance. All passwords are in lower case letters.
     *
     * @return password for instance.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the stage of the fight - used to determine certain points in it.
     *
     * @param stage integer describing instance's stage.
     */
    public void setStage(final int stage) {
        this.stage = stage;
    }

    /**
     * Gets the stage of the fight.
     *
     * @return stage of the fight.
     */
    public int getStage() {
        return stage;
    }

    /**
     * Gets an instanced worldtile based on the coordinates of the static area in
     * the map. Height is always set to two.
     *
     * @param x coordinate to the static map location.
     * @param y coordinate to the static map location.
     * @return instanced worldtile.
     */
    public WorldTile getWorldTile(final int x, final int y) {
        final int offsetX = x - (REGIONX * 8);
        final int offsetY = y - (REGIONY * 8);
        return new WorldTile((chunks[0] * 8) + offsetX, (chunks[1] * 8) + offsetY, 2);
    }

    /**
     * Gets an instanced worldtile based on the coordinates of the static area in
     * the map. Height is always set to two.
     *
     * @param tile coordinates to the static map location.
     * @return instanced worldtile.
     */
    public WorldTile getWorldTile(final WorldTile tile) {
        final int offsetX = tile.getX() - (REGIONX * 8);
        final int offsetY = tile.getY() - (REGIONY * 8);
        return new WorldTile((chunks[0] * 8) + offsetX, (chunks[1] * 8) + offsetY, tile.getPlane());
    }

    public void clearBombs() {
        bombs.clear();
    }
}
