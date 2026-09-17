package com.rs.game;

import java.util.concurrent.TimeUnit;

import com.rs.cores.CoresManager;
import com.rs.game.player.Player;
import com.rs.utils.Logger;

public class MapInstance {

    private volatile Stages stage;
    private int[] instancePos;
    private final int[] originalPos;
    private final int ratioX, ratioY;
    private boolean destroyed;

    public MapInstance(final int x, final int y) {
        this(x, y, 1, 1);
    }

    public MapInstance(final int x, final int y, final int ratioX, final int ratioY) {
        originalPos = new int[] { x, y };
        this.ratioX = ratioX;
        this.ratioY = ratioY;
    }

    public void loadSafe(final Runnable run) {
        load(() -> CoresManager.getServiceProvider().addGameTask(run));
    }

    // TODO make this thread safe, use the loadSafe method for all usages
    public void load(final Runnable run) {
        stage = Stages.LOADING;
        CoresManager.getServiceProvider().executeNow(() -> {
            try {
                // finds empty map bounds
                if (instancePos == null) {
                    instancePos = MapBuilder.findEmptyChunkBound(ratioX * 8, ratioY * 8);
                }
                // copys real map into the empty map
                MapBuilder.copyAllPlanesMap(originalPos[0], originalPos[1], instancePos[0], instancePos[1], ratioX * 8, ratioY * 8);
                if (run != null) {
                    run.run();
                }
                stage = Stages.RUNNING;
            } catch (final Throwable e) {
                Logger.getGlobal().catching(e);
            }
        });
    }

    public void destroy(final Runnable run) {
        stage = Stages.DESTROYING;
        if (destroyed)
            return;
        destroyed = true;
        final int[] pos = instancePos;
        final int rx = ratioX;
        final int ry = ratioY;
        CoresManager.getServiceProvider().executeWithDelay(() -> {
            try {
                destroyed = false;
                MapBuilder.destroyMap(pos[0], pos[1], rx * 8, ry * 8);
                if (run != null) {
                    run.run();
                }
            } catch (final Throwable e) {
                Logger.getGlobal().catching(e);
            }
        }, 1800, TimeUnit.MILLISECONDS);
    }

    public WorldTile getTile(final int x, final int y, int z) {
        return new WorldTile(instancePos[0] * 8 + x, instancePos[1] * 8 + y, z);
    }

    public WorldTile getTile(final int x, final int y) {
        return getTile(x, y, 0);
    }

    public WorldTile getTile(WorldTile tile) {
        return getTile(tile.getX(), tile.getY(), tile.getPlane());
    }

    public WorldTile getInstanceTile(WorldTile tile) {
        return getInstanceTile(tile.getX(), tile.getY(), tile.getPlane());
    }

    public WorldTile getInstanceTile(int x, int y, int plane) {
        int[] originalPos = getOriginalPos();
        WorldTile tile = getTile((x - originalPos[0] * 8) % (getRatioX() * 64), (y - originalPos[1] * 8) % (getRatioY() * 64));
        tile.moveLocation(0, 0, plane);
        return tile;
    }

    public int getCutsceneX(Player player, int x) {
        return getInstanceTile(new WorldTile(x, 0, 0)).getXInScene(player);
    }

    public int getCutsceneY(Player player, int y) {
        return getInstanceTile(new WorldTile(0, y, 0)).getYInScene(player);
    }

    public int[] getOriginalPos() {
        return originalPos;
    }

    public int getRatioX() {
        return ratioX;
    }

    public int getRatioY() {
        return ratioY;
    }

    public Stages getStage() {
        return stage;
    }

    public enum Stages {
        LOADING, RUNNING, DESTROYING
    }
}