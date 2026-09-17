package com.rs.game.activities.instances;

import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.MapBuilder;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.gwd2.twinfuries.Avaryss;
import com.rs.game.npc.gwd2.twinfuries.Nymora;
import com.rs.game.player.Player;
import com.rs.game.player.content.HeartOfGielinor;
import com.rs.utils.Utils;

/**
 * @author Tom
 * @date April 14, 2017
 * 
 *       TODO: Make a check for each attack to see if the target is still in
 *       boundaries.
 */

public class TwinFuriesInstance extends Instance {

    private NPC avaryss;
    private NPC nymora;
    private int phase;
    private long specialDelay;
    private boolean channelling;

    public TwinFuriesInstance(Player owner, int instanceDuration, int respawnSpeed, int playersLimit, int password, int bossId, boolean hardMode) {
        super(owner, instanceDuration, respawnSpeed, playersLimit, password, bossId, hardMode);
        chunksToBind = new int[] { 384, 880 };
        sizes = new int[] { 8, 10 };
        boundChunks = MapBuilder.findEmptyChunkBound(sizes[0], sizes[1]);
    }

    @Override
    public WorldTile getWorldTile(int x, int y) {
        return new WorldTile((boundChunks[0] * 8) + x, (boundChunks[1] * 8) + y, 1);
    }

    public int getX(int mapX) {
        return boundChunks[0] * 8 + mapX;
    }

    public int getY(int mapY) {
        return boundChunks[1] * 8 + mapY;
    }

    @Override
    public WorldTile getWaitingRoomCoords() {
        return getWorldTile(43, 19);
    }

    @Override
    public void addPlayer(Player player) {
        super.addPlayer(player);

    }

    @Override
    public void initiateSpawningSequence() {
        TwinFuriesInstance instance = this;
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
                    if (nymora != null)
                        nymora.finish();
                    if (avaryss != null)
                        avaryss.finish();
                    return false;
                }
                if (!isOwnerInstance()) {
                    if (nymora != null)
                        nymora.finish();
                    if (avaryss != null)
                        avaryss.finish();
                    return false;
                }
                if (seconds == 0 && !finished) {
                    resetSeconds = false;
                    if ((nymora == null && avaryss == null || nymora.hasFinished() && avaryss.hasFinished()) && !getPlayers().isEmpty()) {
                        nymora = new Nymora(22454, new WorldTile(getWorldTile(32, 36)), -1, true, true, instance);
                        avaryss = new Avaryss(22453, new WorldTile(getWorldTile(27, 36)), -1, true, true, instance);
                        instance.getPlayers().forEach(p -> {
                            if (p != null && p.getX() >= p.getCurrentInstance().getWorldTile(43, 20).getX())
                                HeartOfGielinor.switchInterfaces(p, instance, avaryss, true);
                        });
                        phase = 0;
                        setSpecialDelay(Utils.currentTimeMillis() + (5000 + Utils.random(7000)));
                    } else if (getPlayers().isEmpty()) {
                        nymora.finish();
                        avaryss.finish();
                    }
                }
                if (nymora.hasFinished() && avaryss.hasFinished() && !resetSeconds) {
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

    public long getSpecialDelay() {
        return specialDelay;
    }

    public void setSpecialDelay(long value) {
        this.specialDelay = value;
    }

    public NPC getNymora() {
        return nymora;
    }

    public NPC getAvaryss() {
        return avaryss;
    }

    public int getPhase() {
        return phase;
    }

    public void nextPhase() {
        phase++;
    }

    public void setPhase(int phase) {
        this.phase = phase;
    }

    public boolean isChannelling() {
        return channelling;
    }

    public void setChannelling(boolean val) {
        channelling = val;
    }

    @Override
    public WorldTile getOutsideCoordinates() {
        return new WorldTile(3115, 7060, 1);
    }

    @Override
    public NPC getBossNPC() {
        return null;
    }

    @Override
    public void performOnSpawn() {
    }
}
