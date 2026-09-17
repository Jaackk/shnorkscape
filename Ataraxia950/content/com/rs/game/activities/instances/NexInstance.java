package com.rs.game.activities.instances;

import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.MapBuilder;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.godwars.zaros.Nex;
import com.rs.game.npc.godwars.zaros.Nex.NexPhase;
import com.rs.game.npc.godwars.zaros.NexMinion;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * 
 *         Created on Nov 14, 2018.
 */
public class NexInstance extends Instance {

    private NPC nex;
    private NexPhase currentPhase;
    private boolean solo;
    private final List<Player> inArena = new ArrayList<Player>();

    public NexInstance(Player owner, int instanceDuration, int respawnSpeed, int playersLimit, int password, int bossId, boolean hardMode, boolean solo) {
        super(owner, instanceDuration, respawnSpeed, playersLimit, password, bossId, hardMode);
        chunksToBind = new int[] { 362, 648 };
        sizes = new int[] { 10, 10 };
        boundChunks = MapBuilder.findEmptyChunkBound(sizes[0], sizes[1]);
    }

    @Override
    public NPC getBossNPC() {
        nex = new Nex(13447, getSpawnTile(), -1, true, NexInstance.this);
        return nex;
    }

    @Override
    public WorldTile getWaitingRoomCoords() {
        return solo ? getWorldTile(15, 19) : getWorldTile(8, 19);
    }

    @Override
    public WorldTile getOutsideCoordinates() {
        return new WorldTile(2904, 5203, 0);
    }

    @Override
    public void initiateSpawningSequence() {
        CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
            private int seconds;
            private boolean resetSeconds;

            @Override
            public boolean repeat() {
                if (!nexSpawned) {
                    return true;
                }
                if (!isStable && players.size() == 0 || (totalSeconds % 60 == 0 && (totalSeconds / 60) == instanceDuration + 5)) {
                    if (players.size() > 0) {
                        players.forEach(player -> {
                            if (player != null && player.getCurrentInstance() == NexInstance.this)
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
                if (boss != null && boss.hasFinished() && !resetSeconds) {
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
        final Graphics[] MINION_GRAPHICS = { new Graphics(3359), new Graphics(3361), new Graphics(3358), new Graphics(3360) };
        WorldTasksManager.schedule(new WorldTask() {
            int stage = 0;

            @Override
            public void run() {
                if (players.isEmpty()) {
                    stop();
                    return;
                }
                if (stage == 0) {
                    if (hardMode) {
                        int[] bonuses = new int[nex.getBonuses().length];
                        for (int i = 0; i < bonuses.length; i++)
                            bonuses[i] = (int) (nex.getBonus(i) * 1.2);
                        nex.setBonuses(bonuses);
                    }
                    sendBeginningAction(NexPhase.values()[stage]);
                } else if (stage < 5) {
                    WorldTile minion = boss;
                    if (stage == 1)
                        minion = new WorldTile(boss.getX() - 12, boss.getY() + 12, 0);
                    else if (stage == 2)
                        minion = new WorldTile(boss.getX() + 12, boss.getY() + 12, 0);
                    else if (stage == 3)
                        minion = new WorldTile(boss.getX() + 12, boss.getY() - 12, 0);
                    else if (stage == 4)
                        minion = new WorldTile(boss.getX() - 12, boss.getY() - 12, 0);
                    final NexMinion nexMinion = (NexMinion) World.spawnNPC(13450 + stage, minion, 0, true, true);
                    if (nex != null) {
                        ((Nex) nex).setMinion(stage - 1, nexMinion);
                    }
                    nexMinion.setInstance(NexInstance.this);
                    nexMinion.setNextAnimation(new Animation(17403));
                    nexMinion.setNextGraphics(MINION_GRAPHICS[stage - 1]);
                    sendBeginningAction(NexPhase.values()[stage]);
                } else if (stage == 5) {
                    incrementStage(NexPhase.START);
                } else if (stage == 6) {
                    stop();
                    if (nex != null) {
                        ((Nex) nex).start();
                    }
                }
                stage++;
            }
        }, 4, 3);
    }

    public volatile boolean nexSpawned;

    public void incrementStage(final NexPhase lastPhase) {
        final NexPhase phase = NexPhase.values()[lastPhase.getPhaseValue() + 1];
        setCurrentPhase(phase);
        ((Nex) nex).setFirstStageAttack(true);
        nex.setNextForceTalk(phase.getMessage());
        nex.playSoundEffect(phase.getSecondSound());
        nex.setCapDamage(Utils.random(801, 999));
        if (lastPhase == NexPhase.SMOKE) {
            ((Nex) nex).removeInfectedPlayers();
        } else if (lastPhase == NexPhase.SHADOW) {
            ((Nex) nex).removeShadow();
        } else if (lastPhase == NexPhase.BLOOD) {
            ((Nex) nex).killBloodReavers();
        }
        if (phase == NexPhase.ZAROS) {
            ((Nex) nex).sendFinalStage();
        }
        if (phase != NexPhase.ZAROS) {
            World.sendProjectile(((Nex) nex).getMinion(phase.getPhaseValue() - 1), nex, 2244, 18, 18, 60, 30, 0, 0);
        }
    }

    public void sendBeginningAction(final NexPhase phase) {
        final Animation START = new Animation(17412), SECOND_START = new Animation(17413), THIRD_START = new Animation(17414);
        nex.setNextForceTalk(new ForceTalk(phase.getMinionName()));
        nex.setNextAnimation((phase == NexPhase.SHADOW || phase == NexPhase.ICE) ? THIRD_START : phase == NexPhase.START ? START : SECOND_START);
        if (phase == NexPhase.START) {
            nex.setNextGraphics(new Graphics(3353));
        }
        nex.playSoundEffect(phase.getFirstSound());
        if (phase.getPhaseValue() == 0) {
            return;
        }
        final NexMinion nexMinion = ((Nex) nex).getMinion(phase.getPhaseValue() - 1);
        if (nexMinion != null) {
            World.sendProjectile(nex, nexMinion, 2244, 18, 18, 60, 30, 0, 0);
            nexMinion.faceEntity(nex);
            nexMinion.setCantInteract(false);
            nex.faceEntity(nexMinion);
        }
    }

    public WorldTile getSpawnTile() {
        return getWorldTile(29, 19);
    }

    public Entity getRandomNexTarget() {
        synchronized (players) {
            if (players.isEmpty() || inArena.isEmpty()) {
                return null;
            }
            return inArena.get(Utils.random(inArena.size()));
        }
    }

    public NexPhase getCurrentPhase() {
        return currentPhase;
    }

    public void setCurrentPhase(NexPhase currentPhase) {
        this.currentPhase = currentPhase;
    }

    public void addPlayerToArena(Player player) {
        synchronized (inArena) {
            inArena.add(player);
        }
    }

    public void removePlayerFromArena(Player player) {
        synchronized (inArena) {
            inArena.remove(player);
            if (inArena.size() < 1)
                end();
        }
    }

    private void end() {
        synchronized (inArena) {
            if (nex != null) {
                for (final NexMinion minion : ((Nex) nex).nexMinions) {
                    if (minion == null || minion.isDead()) {
                        continue;
                    }
                    minion.finish();
                }
                nex.finish();
                nex = null;
            }
        }
    }

    public List<Player> getInArena() {
        return inArena;
    }

}
