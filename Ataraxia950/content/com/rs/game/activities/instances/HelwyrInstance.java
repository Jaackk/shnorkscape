package com.rs.game.activities.instances;

import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.MapBuilder;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.gwd2.helwyr.CMHelwyr;
import com.rs.game.npc.gwd2.helwyr.CywirAlpha;
import com.rs.game.npc.gwd2.helwyr.Helwyr;
import com.rs.game.player.Player;
import com.rs.game.player.content.HeartOfGielinor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tom
 * @date April 8, 2017
 */

public class HelwyrInstance extends Instance {

    public static final int[][] MUSHROOM_TILES = new int[][] { { 26, 46 }, { 31, 46 }, { 36, 45 }, { 40, 46 }, { 45, 45 }, { 26, 41 }, { 31, 41 }, { 36, 41 }, { 41, 41 }, { 46, 40 }, { 26, 36 }, { 31, 36 }, { 36, 36 }, { 41, 36 }, { 45, 35 }, { 26, 31 }, { 31, 31 }, { 36, 31 }, { 41, 31 }, { 46, 31 }, { 26, 26 }, { 30, 26 }, { 35, 26 }, { 40, 26 }, { 45, 26 } };
    private final List<CywirAlpha> wolves = new ArrayList<CywirAlpha>();
    private final List<WorldTile> tiles = new ArrayList<WorldTile>();
    private final List<WorldTile> availableTiles = new ArrayList<WorldTile>();

    public HelwyrInstance(Player owner, int instanceDuration, int respawnSpeed, int playersLimit, int password, int bossId, boolean hardMode) {
        super(owner, instanceDuration, respawnSpeed, playersLimit, password, bossId, hardMode);
        chunksToBind = new int[] { 407, 856 };
        sizes = new int[] { 8, 10 };
        boundChunks = MapBuilder.findEmptyChunkBound(sizes[0], sizes[1]);
        for (int[] tiles : HelwyrInstance.MUSHROOM_TILES)
            getAvailableTiles().add(getWorldTile(tiles[0], tiles[1]));
    }

    @Override
    public WorldTile getWorldTile(int x, int y) {
        return new WorldTile((boundChunks[0] * 8) + x, (boundChunks[1] * 8) + y, 1);
    }

    @Override
    public WorldTile getWaitingRoomCoords() {
        return getWorldTile(20, 51);
    }

    @Override
    public void initiateSpawningSequence() {
        CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
            private int seconds;
            private boolean resetSeconds;

            @Override
            public boolean repeat() {
                if (!isStable && players.size() == 0 || (totalSeconds % 60 == 0 && (totalSeconds / 60) == instanceDuration + 5)) {
                    if (players.size() > 0) {
                        players.forEach(player -> {
                            if (player != null && player.getCurrentInstance() == HelwyrInstance.this)
                                player.setNextWorldTile(getOutsideCoordinates());
                        });
                    }
                    destroyInstance();
                    if (boss != null)
                        boss.finish();
                    return false;
                }
                if (!isOwnerInstance()) {
                    if (boss != null)
                        boss.finish();
                    return false;
                }
                if (seconds == 0 && !finished) {
                    resetSeconds = false;
                    if ((boss == null || boss.hasFinished()) && !getPlayers().isEmpty()) {
                        boss = getBossNPC();
                        boss.setForceMultiArea(true);
                        performOnSpawn();
                    } else if (getPlayers().isEmpty())
                        boss.finish();
                }
                if (boss.hasFinished() && !resetSeconds) {
                    seconds = 0 - respawnSpeed;
                    resetSeconds = true;
                }
                if (totalSeconds % 60 == 0 && (totalSeconds / 60) == instanceDuration) {
                    finished = true;
                    players.forEach(player -> player.sendMessage("The instance has ended. No more monsters will be spawned in this instance."));
                }
                if (totalSeconds % 60 == 0 && (totalSeconds / 60) == instanceDuration - 2) {
                    players.forEach(player -> player.sendMessage("The instance will remain open for two more minutes."));
                    isStable = false;
                }
                seconds++;
                totalSeconds++;
                return true;
            }

        }, 0, 1);
    }

    @Override
    public void performOnSpawn() {
        boss.faceObject(new WorldObject(101898, 6487, 8047, 1, 11, 2));
        boss.setNextAnimation(new Animation(28200));
        boss.setNextGraphics(new Graphics(6120));
        boss.setNextGraphics(new Graphics(6085));
        getPlayers().forEach(p -> {
            if (p != null && p.getY() > p.getCurrentInstance().getWorldTile(25, 47).getY())
                HeartOfGielinor.switchInterfaces(p, this, getHelwyr(), true);
        });
    }

    public List<CywirAlpha> getWolves() {
        return wolves;
    }

    public int getAliveWolves() {
        int amount = 0;
        for (CywirAlpha w : wolves)
            if (w != null && !w.isDead() && !w.hasFinished())
                amount++;
        return amount;
    }

    public List<WorldTile> getTiles() {
        return tiles;
    }

    public List<WorldTile> getAvailableTiles() {
        return availableTiles;
    }

    public void addMushroom(WorldTile tile) {
        availableTiles.remove(tile);
        tiles.add(tile);
    }

    public void removeMushroom(WorldTile tile) {
        availableTiles.add(tile);
        tiles.remove(tile);
    }

    @Override
    public WorldTile getOutsideCoordinates() {
        return new WorldTile(3278, 6897, 1);
    }

    public NPC getHelwyr() {
        return boss;
    }

    @Override
    public NPC getBossNPC() {
        if (isHardMode())
            return new CMHelwyr(22440, new WorldTile(getWorldTile(36, 34)), -1, true, true, this);
        return new Helwyr(22438, new WorldTile(getWorldTile(36, 34)), -1, true, true, this);
    }

}
