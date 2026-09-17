package com.rs.game.player.content.death;

import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.map.bossInstance.BossInstance;
import com.rs.game.map.bossInstance.InstanceSettings;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.data.parsers.npcs.pojos.NPCDirection;

/**
 * @author lare96
 */
public final class DeathInstance extends BossInstance {

    private NPC deathNpc;
    private WorldObject portalCover;
    private WorldObject exitPortal;

    public DeathInstance(Player owner, InstanceSettings settings) {
        super(owner, settings);
    }

    @Override
    public String getInstanceName() {
        throw new IllegalStateException("This instance does not have a name.");
    }

    @Override
    public int[] getMapPos() {
        return new int[] { 424, 657 };
    }

    @Override
    public int[] getMapSize() {
        return new int[] { 1, 1 };
    }

    @Override
    public void loadMapInstance() {
        deathNpc = new NPC(14386, getTile(3419, 5272, 0), -1, false, true);
        portalCover = new WorldObject(-1, 10, 0, getTile(3420, 5272, 0));
        deathNpc.setDirection(NPCDirection.SOUTH.getValue());
        World.spawnObject(portalCover);
        exitPortal = new WorldObject(45803, 10, 0, deathNpc.transform(0, -5, 0));
        World.spawnObject(exitPortal);
    }

    @Override
    public void enterInstance(Player player, boolean login) {
        super.enterInstance(player, login);
        player.setNextAnimation(new Animation(-1));
    }

    @Override
    public void leaveInstance(Player player, int type) {
        player.heardDeathsWarning = true;
        player.rodSaved = false;
        super.leaveInstance(player, type);
    }

    @Override
    public void onFinish() {
        if (deathNpc != null && !deathNpc.hasFinished())
            deathNpc.finish();
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                if (World.containsObjectWithId(portalCover, portalCover.getId()))
                    World.removeObject(portalCover);
            }
        }, 3);
        if (World.containsObjectWithId(exitPortal, exitPortal.getId()))
            World.removeObject(exitPortal);
    }
}
