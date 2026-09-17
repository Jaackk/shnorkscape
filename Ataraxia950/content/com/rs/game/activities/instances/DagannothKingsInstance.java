package com.rs.game.activities.instances;

import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.MapBuilder;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.others.DagannothKing;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

/**
 * @author Kris {@link https://www.rune-server.ee/members/kris/ }
 */
public class DagannothKingsInstance extends Instance {

    public DagannothKingsInstance(Player owner, int instanceDuration, int respawnSpeed, int playersLimit, int password, int bossId, boolean hardMode) {
        super(owner, instanceDuration, respawnSpeed, playersLimit, password, bossId, hardMode);
        kings = new NPC[bossId == 2880 ? 3 : 1];
        chunksToBind = new int[]{360, 553};
        sizes = new int[]{7, 7};
        boundChunks = MapBuilder.findEmptyChunkBound(sizes[0], sizes[1]);
    }

    private final NPC[] kings;
    private int regionHash;

    private WorldTile getBossLocation(int bossId) {
        switch (bossId) {
            case 2883:
                return getWorldTile(38, 19);
            case 2881:
                return getWorldTile(28, 24);
            default:
                return getWorldTile(36, 26);
        }
    }

    @Override
    public WorldTile getWaitingRoomCoords() {
        return getWorldTile(20, 22);
    }

    @Override
    public void initiateSpawningSequence() {
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                regionHash = getWaitingRoomCoords().getRegionHash();
            }
        }, 2);
        if (bossId == 2880) {
            kings[0] = new DagannothKing(2881, getBossLocation(2881), -1, true, false, this);
            kings[1] = new DagannothKing(2882, getBossLocation(2882), -1, true, false, this);
            kings[2] = new DagannothKing(2883, getBossLocation(2883), -1, true, false, this);
        } else
            kings[0] = new DagannothKing(bossId, getWorldTile(34, 24), -1, true, false, this);
        for (int i = 0; i < kings.length; i++)
            kings[i].setForceMultiArea(true);
        Instance instance = this;
        CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {

            @Override
            public boolean repeat() {
                if (!isStable && players.size() == 0 || (totalSeconds % 60 == 0 && (totalSeconds / 60) == instanceDuration + 5)) {
                    players.removeIf(plr -> {
                        if (plr != null && plr.getCurrentInstance() == instance) {
                            plr.setCurrentInstance(null);
                            if(plr.getRegionHash() == regionHash) {
                                plr.setNextWorldTile(getOutsideCoordinates());
                            }
                        }
                        return true;
                    });
                    destroyInstance();
                    for (int i = 0; i < kings.length; i++) {
                        if (kings[i] != null)
                            kings[i].finish();
                    }
                    return false;
                }
                if (!isOwnerInstance()) {
                    for (int i = 0; i < kings.length; i++) {
                        if (kings[i] != null)
                            kings[i].finish();
                    }
                    return false;
                }
                if (totalSeconds % 60 == 0 && (totalSeconds / 60) == instanceDuration) {
                    finished = true;
                    players.forEach(player -> player.sendMessage("The instance has ended. No more monsters will be spawned in this instance."));
                }
                if (totalSeconds % 60 == 0 && (totalSeconds / 60) == instanceDuration - 2) {
                    players.forEach(player -> player.sendMessage("The instance will remain open for two more minutes."));
                    isStable = false;
                }
                totalSeconds++;
                return true;
            }

        }, 0, 1);
    }

    @Override
    public WorldTile getOutsideCoordinates() {
        return new WorldTile(1912, 4367, 0);
    }

    @Override
    public NPC getBossNPC() {
        return null;
    }

    @Override
    public void performOnSpawn() {
    }


}
