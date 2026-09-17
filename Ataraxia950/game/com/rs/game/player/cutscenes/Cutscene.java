package com.rs.game.player.cutscenes;

import com.rs.Settings;
import com.rs.cores.CoresManager;
import com.rs.game.MapBuilder;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.content.InterfaceManager;
import com.rs.game.player.cutscenes.actions.CutsceneAction;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Logger;

import lombok.val;

public abstract class Cutscene {

    private int stage;
    private Object[] cache;
    private CutsceneAction[] actions;
    private int delay;
    private boolean constructingRegion;
    private int[] currentMapData;
    private WorldTile endTile;
    private volatile int originalBaseX;
    private volatile int originalBaseY;
    private volatile int ratioX;
    private volatile int ratioY;
    private volatile int instanceX;
    private volatile int instanceY;
    private boolean finished;

    public Cutscene() {

    }

    public static int getX(Player player, int x) {
        return new WorldTile(x, 0, 0).getXInScene(player);
    }

    public static int getY(Player player, int y) {
        return new WorldTile(0, y, 0).getYInScene(player);
    }

    public WorldTile getTile(WorldTile tile) {
        return getTile(tile.getX(), tile.getY(), tile.getPlane());
    }

    // gets the instanced coords of the world tile(notice if the area instanced
    // differs from coords it will give wrong tile,
    // the ratio part is to make sure the coords are inside map
    public WorldTile getTile(int x, int y, int plane) {
        int computedX = (x - originalBaseX * 8) % (ratioX * 64);
        int computedY = (y - originalBaseY * 8) % (ratioY * 64);
        val tile = new WorldTile(instanceX * 8 + computedX, instanceY * 8 + computedY, 0);
        tile.moveLocation(0, 0, plane);
        return tile;
    }//

    public void constructArea(final Player player, final int baseChunkX, final int baseChunkY, final int widthChunks,
                              final int heightChunks) {
        constructingRegion = true;
        player.getPackets().sendWindowsPane(56, 0);
        CoresManager.getServiceProvider().executeNow(new Runnable() {
            @Override
            public void run() {
                try {
                    final int[] oldData = currentMapData;
                    final int[] mapBaseChunks = MapBuilder.findEmptyChunkBound(widthChunks, heightChunks);
                    MapBuilder.copyAllPlanesMap(baseChunkX, baseChunkY, mapBaseChunks[0], mapBaseChunks[1], widthChunks,
                            heightChunks);
                    originalBaseX = baseChunkX;
                    originalBaseY = baseChunkY;
                    ratioX = widthChunks;
                    ratioY = heightChunks;
                    instanceX = mapBaseChunks[0];
                    instanceY = mapBaseChunks[1];
                    currentMapData = new int[]{mapBaseChunks[0], mapBaseChunks[1], widthChunks, heightChunks};
                    player.setNextWorldTile(
                            new WorldTile(getBaseX() + widthChunks * 4, +getBaseY() + heightChunks * 4, 0));
                    constructingRegion = false;
                    if (Settings.DEBUG) {
                        Logger.getGlobal().info("Bases: " + getBaseX() + ", " + getBaseY());
                    }
                    WorldTasksManager.schedule(new WorldTask() {

                        @Override
                        public void run() {

                            CoresManager.getServiceProvider().executeNow(() -> {
                                player.getPackets()
                                        .sendWindowsPane(player.getInterfaceManager().hasRezizableScreen()
                                                ? InterfaceManager.RESIZABLE_WINDOW_ID
                                                : InterfaceManager.FIXED_WINDOW_ID, 0);
                                if (oldData != null) {
                                    MapBuilder.destroyMap(oldData[0], oldData[1], oldData[1], oldData[2]);
                                }
                            });
                        }

                    }, 1);
                } catch (final Throwable e) {
                    Logger.getGlobal().catching(e);
                }
            }
        });
    }

    public final void createCache(final Player player) {
        actions = getActions(player);
        endTile = new WorldTile(player);
        int lastIndex = 0;
        for (final CutsceneAction action : actions) {
            if (action.getCachedObjectIndex() > lastIndex) {
                lastIndex = action.getCachedObjectIndex();
            }
        }
        cache = new Object[lastIndex + 1];
        cache[0] = this;
    }

    public void start(Player player) {

    }

    public void deleteCache() {
        for (final Object object : cache) {
            destroyCache(object);
        }
    }

    public void destroyCache(final Object object) {
        if (object instanceof NPC) {
            final NPC n = (NPC) object;
            n.finish();
        }
    }

    public abstract CutsceneAction[] getActions(Player player);

    public int getBaseX() {
        return currentMapData == null ? 0 : currentMapData[0] << 3;
    }

    public int getBaseY() {
        return currentMapData == null ? 0 : currentMapData[1] << 3;
    }

    public int getLocalX(Player player, int x) {
        return getX(player, (currentMapData == null ? 0 : getBaseX()) + x);
    }

    public int getLocalY(Player player, int y) {
        return getY(player, (currentMapData == null ? 0 : getBaseY()) + y);
    }

    public abstract boolean hiddenMinimap();

    public final void logout(final Player player) {
        stopCutscene(player);
    }

    public final boolean process(final Player player) {
        if (delay > 0) {
            delay--;
            return true;
        }
        while (true) {
            if (constructingRegion) {
                return true;
            }
            if (stage == actions.length) {
                stopCutscene(player);
                return false;
            } else if (stage == 0) {
                startCutscene(player);
            }
            final CutsceneAction action = actions[stage++];
            action.process(player, cache);
            final int delay = action.getActionDelay();
            if (delay == -1) {
                continue;
            }
            this.delay = delay;
            return true;
        }
    }

    public final void startCutscene(final Player player) {
        if (hiddenMinimap()) {
            player.getPackets().sendMiniMapStatus(2); // minimap
        }
        player.getPackets().sendConfigByFile(3028, 1);
        player.setLargeSceneView(true);
        player.lock();
        player.stopAll(true, false);
        start(player);
    }

    public void stopCutscene(final Player player) {
        if (player.getX() != endTile.getX() || player.getY() != endTile.getY()
                || player.getPlane() != endTile.getPlane()) {
            player.setNextWorldTile(endTile);
        }
        if (hiddenMinimap()) {
            player.getPackets().sendMiniMapStatus(0); // unblack
        }
        player.getPackets().sendConfigByFile(3028, 0);
        player.getPackets().sendResetCamera();
        player.setLargeSceneView(false);
        player.unlock();
        deleteCache();
        finished = true;
        if (currentMapData != null) {
            CoresManager.getServiceProvider().executeNow(() -> {
                try {
                    if (currentMapData != null) {
                        MapBuilder.destroyMap(currentMapData[0], currentMapData[1], currentMapData[1],
                                currentMapData[2]);
                    }
                } catch (final Throwable e) {
                    Logger.getGlobal().catching(e);
                }
            });
        }
    }

    protected final void stopWithoutReplacement(final Player player) {
        if (hiddenMinimap()) {
            player.getPackets().sendMiniMapStatus(0); // unblack
        }
        player.getPackets().sendConfigByFile(3028, 0);
        player.getPackets().sendResetCamera();
        player.setLargeSceneView(false);
        player.unlock();
        deleteCache();
        finished = true;
        if (currentMapData != null) {
            CoresManager.getServiceProvider().executeNow(() -> {
                try {
                    if (currentMapData != null) {
                        MapBuilder.destroyMap(currentMapData[0], currentMapData[1], currentMapData[1],
                                currentMapData[2]);
                    }
                } catch (final Throwable e) {
                    Logger.getGlobal().catching(e);
                }
            });
        }
    }
}
