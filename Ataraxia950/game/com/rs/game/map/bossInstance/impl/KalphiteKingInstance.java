package com.rs.game.map.bossInstance.impl;

import com.rs.game.Animation;
import com.rs.game.WorldTile;
import com.rs.game.map.bossInstance.BossInstance;
import com.rs.game.map.bossInstance.BossInstanceHandler;
import com.rs.game.map.bossInstance.InstanceSettings;
import com.rs.game.npc.kalphiteking.KalphiteKing;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class KalphiteKingInstance extends BossInstance {

    public static final WorldTile KALPHITE_KING_SPAWN_TILE = new WorldTile(2978, 1760, 0);

    private transient KalphiteKing kalphiteKing;

    public KalphiteKingInstance(Player owner, InstanceSettings settings) {
        super(owner, settings);
    }

    @Override
    public void loadMapInstance() {
        int respawnDelay = 60;
        respawnDelay /= getSettings().getSpawnSpeed();
        WorldTasksManager.schedule(new WorldTask() {
            int count = 0;

            @Override
            public void run() {
                if (isFinished()) {
                    stop();
                    return;
                }
                if (count == 0) {
                    int random = Utils.random(3);
                    int id = 16697 + random;
                    kalphiteKing = new KalphiteKing(id, getTile(KALPHITE_KING_SPAWN_TILE), KalphiteKingInstance.this);
                    kalphiteKing.setNextAnimation(new Animation(19451));
                    kalphiteKing.setForceFollowClose(id == 16697);
                    kalphiteKing.setDirection(Utils.getAngle(0, -1));
                    kalphiteKing.setCantInteract(true);
                } else if (count >= 3) {
                    kalphiteKing.setCantInteract(false);
                    kalphiteKing.setNextAnimation(new Animation(-1));
                    stop();
                }
                count++;
            }

        }, respawnDelay, 1);

    }

    @Override
    public void enterInstance(Player player, boolean login) {
        synchronized (BossInstanceHandler.LOCK) {
            if (!login) {
                player.lock();
                player.setNextFaceWorldTile(new WorldTile(2971, 1657, 0));
                player.setNextAnimation(new Animation(19498));
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        player.unlock();
                        player.setNextWorldTile(getTile(getSettings().getBoss().getInsideTile()));
                        player.setNextAnimation(new Animation(-1));

                    }
                }, 5);
            }
            getPlayers().add(player);
            player.setForceMultiArea(true);
            playMusic(player);
            player.getPackets().sendGameMessage("Welcome to this session against <col=00FFFF>" + getInstanceName() + "</col>. This arena will expire in " + (getSettings().getTimeRemaining() / 60000) + " minutes.");
            if (!login)
                player.getControlerManager().startControler(getSettings().getBoss().getControllerName(), this);
            player.setLastBossInstanceKey(getOwnerUsername());
        }
    }

    @Override
    public void leaveInstance(Player player, int type) {
        synchronized (BossInstanceHandler.LOCK) {
            getPlayers().remove(player);
            player.setForceMultiArea(false);
            player.getInterfaceManager().closeOverlay(false);
            player.getMusicsManager().reset();
            if (type == EXITED) {
                player.lock();
                player.setNextAnimation(new Animation(19499));
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        player.unlock();
                        player.setNextWorldTile(getSettings().getBoss().getOutsideTile());
                        player.setNextAnimation(new Animation(-1));
                        player.resetReceivedDamage();
                        if (getPlayers().isEmpty())
                            finish();
                    }
                }, 3);
                return;
            } else if (type == LOGGED_OUT && !isPublic())
                player.setLocation(getSettings().getBoss().getOutsideTile());
            if (getPlayers().isEmpty())
                finish();
        }
    }

    public void finish() {
        super.finish();
        if (kalphiteKing != null) {
            kalphiteKing.finish();
            kalphiteKing = null;
        }
    }

    @Override
    public int[] getMapPos() {
        return new int[] { 369, 216 };
    }

    @Override
    public int[] getMapSize() {
        return new int[] { 1, 1 };
    }

    public KalphiteKing getKalphiteKing() {
        return kalphiteKing;
    }
}
