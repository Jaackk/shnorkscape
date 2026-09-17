package com.rs.game.activites.soulwars;

import com.rs.cores.FixedLengthRunnable;
import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.activites.soulwars.SoulWarsManager.PlayerType;
import com.rs.game.npc.NPC;

/**
 * @author Savions Sw
 */
public class Barricade extends NPC {

    private static final long serialVersionUID = -8831815086522267318L;

    private final int corruptionx;

    public Barricade(WorldTile tile, int team) {
        super(1532, tile, -1, true, true);
        corruptionx = team;
        setCantFollowUnderCombat(true);
    }

    @Override
    public void processNPC() {
        if (isDead())
            return;
        cancelFaceEntityNoCheck();
    }

    public void litFire() {
        transformIntoNPC(1533);
        sendDeath(this);
    }

    public void explode() {
        sendDeath(this);
    }

    @Override
    public void sendDeath(Entity killer) {
        resetWalkSteps();
        getCombat().removeTarget();
        if (this.getId() != 1533) {
            setNextAnimation(null);
            reset();
            setLocation(getRespawnTile());
            finish();
        } else {
            super.sendDeath(killer);
        }
        FixedLengthRunnable runnable = World.soulWars.getTasks().get(PlayerType.IN_GAME);
        if (runnable instanceof GameTask) {
            ((GameTask) runnable).removeBarricade(this, corruptionx);
        }
    }
}