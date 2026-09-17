package com.rs.game.player.controllers.bossInstance;

import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.map.bossInstance.BossInstance;
import com.rs.game.map.bossInstance.BossInstanceHandler;
import com.rs.game.map.bossInstance.BossInstanceHandler.Boss;
import com.rs.game.player.controllers.Controller;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class BossInstanceController extends Controller {

    private BossInstance instance;

    @Override
    public void start() {
        instance = (BossInstance) getArguments()[0]; // cant save this as not
                                                     // serializable would
                                                     // null :L
        getArguments()[0] = instance.getBoss(); // gotta save reference for
                                                // which instance im at
    }

    @Override
    public void process() {
        if (instance != null && Utils.currentWorldCycle() % 120 == 0)
            instance.playMusic(player);
    }

    @Override
    public boolean login() {
        // shouldnt happen but better be safe
        if (getArguments() == null || getArguments().length == 0)
            return true;
        Boss boss = (Boss) getArguments()[0];
        instance = BossInstanceHandler.joinInstance(player, boss, "", true);
        return instance == null; // if failed. remove
    }

    public BossInstance getInstance() {
        return instance;
    }

    private static final WorldTile DEATH = new WorldTile(2952, 1635, 0);

    @Override
    public boolean sendDeath() {
        player.lock(8);
        player.stopAll();
        if (player.isGroupIronman()) {
            String name = instance == null ? "an" : "a "+Utils.capitalize(instance.getBoss().name().replaceAll("_", " ").toLowerCase());
            player.gimTracker.incrementDeaths("<#player> died while in "+name+" instance");
        }
        final boolean safe = player.getTemporaryAttributtes().remove("safedeath") != null;
        final BossInstanceController ctrl = this;
        removeControler();
        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                if (loop == 0) {
                    instance.leaveInstance(player, BossInstance.DIED);
                    player.setNextAnimation(player.getDeathAnimation());
                    player.deathItemsManager.handleDeath();
                } else if (loop == 1) {
                    player.getPackets().sendGameMessage("Oh dear, you have died.");
                } else if (loop == 2) {

                } else if (loop == 3) {
                    player.getDeathManager().reset();
                    if (ctrl instanceof KalphiteKingInstanceController) {
                        WorldTile death = DEATH;
                        for (int i = 0; i < 50; i++) {
                            death = new WorldTile(DEATH, 10);
                            if (World.canMoveNPC(death, 1))
                                break;
                            death = null;
                        }
                        player.getDeathManager().setDeathCoordinates(death == null ? DEATH : death);
                    }
                    player.reset();
                    player.setForceNextMapLoadRefresh(true);
                    player.loadMapRegions();
                    player.setNextAnimation(new Animation(-1));
                } else if (loop == 4) {
                    player.getPackets().sendMusicEffect(90);
                    stop();
                }
                loop++;
            }
        }, 0, 1);
        return false;
    }

    @Override
    public boolean logout() {
        instance.leaveInstance(player, BossInstance.LOGGED_OUT);
        if (!instance.isPublic())
            removeControler();
        return false; // false. it will remove script normaly if needed
    }

    // controller stopped for some reason or in purpose
    @Override
    public void forceClose() {
        if (instance != null)
            instance.leaveInstance(player, BossInstance.EXITED);
    }

    @Override
    public boolean processObjectClick1(WorldObject object) {
        if (object.getId() == 38811) {
            instance.leaveInstance(player, BossInstance.EXITED);
            return false;
        }
        return false;
    }

    @Override
    public void magicTeleported(int type) {
        instance.leaveInstance(player, BossInstance.TELEPORTED);
        removeControler();
    }
}