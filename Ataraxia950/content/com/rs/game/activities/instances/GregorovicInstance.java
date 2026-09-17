package com.rs.game.activities.instances;

import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.Animation;
import com.rs.game.MapBuilder;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.gwd2.gregorovic.CMGregorovic;
import com.rs.game.npc.gwd2.gregorovic.Gregorovic;
import com.rs.game.player.Player;
import com.rs.game.player.content.HeartOfGielinor;

/**
 * @author Tom
 * @date April 9, 2017
 */

public class GregorovicInstance extends Instance {

    private final WorldTile[] masks;

    public GregorovicInstance(Player owner, int instanceDuration, int respawnSpeed, int playersLimit, int password, int bossId, boolean hardMode) {
        super(owner, instanceDuration, respawnSpeed, playersLimit, password, bossId, hardMode);
        chunksToBind = new int[] { 407, 879 };
        sizes = new int[] { 8, 10 };
        boundChunks = MapBuilder.findEmptyChunkBound(sizes[0], sizes[1]);
        masks = new WorldTile[] { getWorldTile(32, 37), getWorldTile(43, 55), getWorldTile(55, 37) };
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
                            if (player != null && player.getCurrentInstance() == GregorovicInstance.this)
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
    public WorldTile getWorldTile(int x, int y) {
        return new WorldTile((boundChunks[0] * 8) + x, (boundChunks[1] * 8) + y, 1);
    }

    @Override
    public WorldTile getWaitingRoomCoords() {
        return getWorldTile(30, 30);
    }

    @Override
    public void performOnSpawn() {
        boss.setNextAnimation(new Animation(28223));
        getPlayers().forEach(p -> {
            if (p != null && p.getX() > p.getCurrentInstance().getWorldTile(30, 30).getX())
                HeartOfGielinor.switchInterfaces(p, this, getGregorovic(), true);
        });
    }

    public WorldTile[] getMasks() {
        return masks;
    }

    public NPC getGregorovic() {
        return boss;
    }

    @Override
    public WorldTile getOutsideCoordinates() {
        return new WorldTile(3286, 7062, 1);
    }

    @Override
    public NPC getBossNPC() {
        if (isHardMode())
            return new CMGregorovic(22443, new WorldTile(getWorldTile(43, 44)), -1, true, true, this);
        return new Gregorovic(22442, new WorldTile(getWorldTile(43, 44)), -1, true, true, this);
    }

}
