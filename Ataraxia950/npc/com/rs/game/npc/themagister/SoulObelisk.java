package com.rs.game.npc.themagister;

import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class SoulObelisk extends NPC {

    private final TheMagister theMagister;
    private int hitCounter;
    private long drainSoulDelay;

    public SoulObelisk(int id, WorldTile tile, TheMagister theMagister) {
        super(id, tile, -1, true, true);
        setForceMultiArea(true);
        setCantFollowUnderCombat(true);
        this.theMagister = theMagister;
        drainSoulDelay = Utils.currentTimeMillis() + 4800;
    }

    @Override
    public boolean checkAgressivity() {
        return false;
    }

    @Override
    public ArrayList<Entity> getPossibleTargets(boolean checkNPCs, boolean checkPlayers) {
        ArrayList<Entity> possibleTarget = new ArrayList<Entity>();
        return possibleTarget;
    }

    @Override
    public ArrayList<Entity> getPossibleTargets() {
        return getPossibleTargets(false, true);
    }

    @Override
    public void processNPC() {
        if (isDead() || isLocked() || hasFinished()) {
            return;
        }
        if (theMagister == null || theMagister.hasFinished()) {
            finish();
            return;
        }
        if (Utils.currentTimeMillis() > drainSoulDelay && theMagister != null && !theMagister.hasFinished() && theMagister.getInstance().getPlayer() != null && !theMagister.getInstance().getPlayer().hasFinished()) {
            Player player = theMagister.getInstance().getPlayer();
            player.addHPReduction(3);
            theMagister.getInstance().updateInterface(false);
            World.sendProjectileCycles(player, transform(1, 1, 0), 6726, 25, 0, 0, 100, 30, 0);
            drainSoulDelay = Utils.currentTimeMillis() + 10000;
        }
    }

    @Override
    public void processHit(Hit hit) {
        if (!theMagister.hasFinished() && theMagister.getInstance().getPlayer() != null && !theMagister.getInstance().getPlayer().hasFinished()) {
            hitCounter += hit.getDamage();
            if (hitCounter >= 100) {
                int toRemove = (hitCounter / 100);
                theMagister.getInstance().getPlayer().removeHPReduction(toRemove);
                hitCounter -= toRemove * 100;
                theMagister.getInstance().updateInterface(false);
            }
        }
        getNextHits().add(hit);
        if (nextHitBars.isEmpty()) {
            addHitBars();
        }
    }

    public TheMagister getTheMagister() {
        return theMagister;
    }

}
