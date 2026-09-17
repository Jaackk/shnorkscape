package com.rs.game.activites.dungeon_architect;

import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.map.bossInstance.BossInstance;
import com.rs.game.map.bossInstance.InstanceSettings;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;

import java.util.ArrayList;
import java.util.List;

public final class DungeonArchitectInstance extends BossInstance {

        private DungeonArchitectMonster type;
        public final List<NPC> spawnedMonsters = new ArrayList<>();

        public DungeonArchitectInstance(Player owner, InstanceSettings settings) {
            super(owner, settings);
        }

        @Override
        public String getInstanceName() {
            if (type == null) {
                return super.getInstanceName();
            }
            return "Dungeoneering Instance ~ " + type.getNpcName();
        }

        @Override
        public int[] getMapPos() {
            return new int[]{170, 738};
        }

        @Override
        public int[] getMapSize() {
            return new int[]{2, 2};
        }

        @Override
        public void loadMapInstance() {
            type = getOwner().monsterType;
            getOwner().monsterType = null;

            // Start spawning monsters!
            spawnNpcs();

            // A monitor to ensure that NPCs don't stop respawning...
            WorldTasksManager.schedule(new WorldTask() {
                int noNpcsTicks = 0;

                @Override
                public void run() {
                    if (isFinished()) {
                        stop();
                        return;
                    }
                    if (noNpcsTicks >= 10) {
                        spawnNpcs();
                        noNpcsTicks = 0;
                    } else if (spawnedMonsters.isEmpty()) {
                        noNpcsTicks++;
                    } else {
                        noNpcsTicks = 0;
                    }
                }
            }, 5, 5);
        }

        @Override
        public void onFinish() {
            for (NPC n : spawnedMonsters) {
                n.setNextWorldTile(new WorldTile(1, 1, 1));
            }
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    for (NPC n : spawnedMonsters) {
                        World.removeNPC(n);
                    }
                    spawnedMonsters.clear();
                }
            }, 2);
        }

        private void spawnNpcs() {
            WorldTile[] tiles = {new WorldTile(1381, 5913, 0),
                    new WorldTile(1375, 5913, 0),
                    new WorldTile(1370, 5914, 0),
                    new WorldTile(1370, 5919, 0),
                    new WorldTile(1370, 5926, 0),
                    new WorldTile(1376, 5926, 0),
                    new WorldTile(1381, 5925, 0),
                    new WorldTile(1376, 5920, 0),
                    new WorldTile(1374, 5918, 0)};
            for (WorldTile wt : tiles) {
                WorldTile instanceTile = getTile(wt);
                spawnedMonsters.add(new DungeonArchitectNPC(this, type, instanceTile));
            }
        }

        public void destroy() {
            if (getPlayers() != null) {
                for (Player player : getPlayers()) {
                    if (player == null) {
                        continue;
                    }
                    leaveInstance(player, BossInstance.EXITED);
                    player.getControlerManager().forceStop();
                    player.sendMessage(Colors.CYAN + "[Instance]: The instance was destroyed by the owner.");
                }
            }
            forceFinish();
        }
    }