package com.rs.game.npc.giantmole;

import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.MapBuilder;
import com.rs.game.WorldTile;
import com.rs.game.activities.instances.Instance;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;

public final class GiantMoleInstance extends Instance {

    public GiantMoleInstance(Player owner, int instanceDuration, int respawnSpeed, int playersLimit, int password, int bossId, boolean hardMode) {
        super(owner, instanceDuration, respawnSpeed, playersLimit, password, bossId, hardMode);
        chunksToBind = new int[] { 215, 647 };
        sizes = new int[] { 11, 10 };
        boundChunks = MapBuilder.findEmptyChunkBound(sizes[0], sizes[1]);
    }

    @Override
    public WorldTile getWaitingRoomCoords() {
        return getWorldTile(29, 67);
    }

    @Override
    public void initiateSpawningSequence() {
        GiantMoleInstance instance = this;
        CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
            private int seconds;
            private boolean resetSeconds;

            @Override
            public boolean repeat() {
                if (!isStable && players.size() == 0 || (totalSeconds % 60 == 0 && (totalSeconds / 60) == instanceDuration + 5)) {
                    if (players.size() > 0) {
                        players.forEach(player -> {
                            if (player != null && player.getCurrentInstance() == instance)
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
    public WorldTile getOutsideCoordinates() {
        return new WorldTile(2985, 3382, 0);
    }

    @Override
    public NPC getBossNPC() {
        return new GiantMole(18932, getWorldTile(38, 51), -1, true, true, this);
    }

    @Override
    public void performOnSpawn() {
    }

}
