package com.rs.game.activities.instances;

import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.Animation;
import com.rs.game.MapBuilder;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.gwd2.vindicta.CMVindicta;
import com.rs.game.npc.gwd2.vindicta.Gorvek;
import com.rs.game.npc.gwd2.vindicta.Vindicta;
import com.rs.game.player.Player;
import com.rs.game.player.content.HeartOfGielinor;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

/**
 * @author Tom
 * @date April 9, 2017
 */

public class VindictaInstance extends Instance {

    private NPC vindicta;
    private NPC gorvek;
    private boolean finished;

    public VindictaInstance(Player owner, int instanceDuration, int respawnSpeed, int playersLimit, int password, int bossId, boolean hardMode) {
        super(owner, instanceDuration, respawnSpeed, playersLimit, password, bossId, hardMode);
        chunksToBind = new int[] { 384, 857 };
        sizes = new int[] { 8, 10 };
        boundChunks = MapBuilder.findEmptyChunkBound(sizes[0], sizes[1]);
    }

    @Override
    public WorldTile getWorldTile(int x, int y) {
        return new WorldTile((boundChunks[0] * 8) + x, (boundChunks[1] * 8) + y, 1);
    }

    @Override
    public WorldTile getWaitingRoomCoords() {
        return getWorldTile(42, 42);
    }

    @Override
    public void initiateSpawningSequence() {
        VindictaInstance instance = this;
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
                    if (vindicta != null)
                        vindicta.finish();
                    if (gorvek != null)
                        gorvek.finish();
                    return false;
                }
                if (!isOwnerInstance()) {
                    if (vindicta != null)
                        vindicta.finish();
                    if (gorvek != null)
                        gorvek.finish();
                    return false;
                }
                if (seconds == 0 && !finished) {
                    resetSeconds = false;
                    if ((vindicta == null || vindicta.hasFinished()) && !getPlayers().isEmpty()) {
                        vindicta = isHardMode() ? new CMVindicta(22461, new WorldTile(getWorldTile(27, 27)), -1, true, true, instance) : new Vindicta(22459, new WorldTile(getWorldTile(27, 27)), -1, true, true, instance);
                        gorvek = new Gorvek(22463, new WorldTile(getWorldTile(27, 26)), -1, true, false);
                        instance.getPlayers().forEach(p -> {
                            if (p != null && p.getCurrentInstance() != null && p.getX() < p.getCurrentInstance().getWorldTile(39, 38).getX())
                                HeartOfGielinor.switchInterfaces(p, instance, vindicta, true);
                        });
                        vindicta.setForceMultiArea(true);
                        gorvek.setForceMultiArea(true);
                        vindicta.faceObject(new WorldObject(101898, 6487, 8047, 1, 11, 2));
                        vindicta.setNextAnimation(new Animation(28257));
                        gorvek.setNextAnimation(new Animation(28264));
                        WorldTasksManager.schedule(new WorldTask() {
                            @Override
                            public void run() {
                                gorvek.setNextWorldTile(new WorldTile(getWorldTile(63, 62)));
                                gorvek.setCantFollowUnderCombat(true);
                                gorvek.isCantSetTargetAutoRelatio();
                            }
                        }, 1);
                    } else if (getPlayers().isEmpty()) {
                        vindicta.finish();
                        gorvek.finish();
                    }
                }
                if (vindicta.hasFinished() && !resetSeconds) {
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

    public NPC getGorvek() {
        return gorvek;
    }

    public NPC getVindicta() {
        return vindicta;
    }

    @Override
    public WorldTile getOutsideCoordinates() {
        return new WorldTile(3113, 6897, 1);
    }

    @Override
    public NPC getBossNPC() {
        return null;
    }

    @Override
    public void performOnSpawn() {
    }

}
