package com.rs.game.activites.quest.deathsbounty;

import com.rs.game.map.bossInstance.BossInstance;
import com.rs.game.map.bossInstance.InstanceSettings;
import com.rs.game.player.Player;

public final class SoulFightInstance extends BossInstance {

    public SoulFightInstance(Player owner, InstanceSettings settings) {
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

    }
}
