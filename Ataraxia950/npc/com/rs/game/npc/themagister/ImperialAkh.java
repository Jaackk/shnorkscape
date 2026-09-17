package com.rs.game.npc.themagister;

import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class ImperialAkh extends NPC {

    private final TheMagister theMagister;

    public ImperialAkh(int id, WorldTile tile, TheMagister theMagister) {
        super(id, tile, -1, true, true);
        setForceMultiArea(true);
        setIntelligentRouteFinder(true);
        setForceAgressive(true);
        setForceTargetDistance(64);
        this.theMagister = theMagister;
    }

    @Override
    public boolean checkAgressivity() {
        if (getHitpoints() == 0)
            return false;
        ArrayList<Entity> possibleTarget = getPossibleTargets();
        if (!possibleTarget.isEmpty()) {
            Entity target = possibleTarget.get(Utils.random(possibleTarget.size()));
            setTarget(target);
            target.setAttackedBy(target);
            target.setFindTargetDelay(Utils.currentTimeMillis() + 10000);
            return true;
        }
        return false;
    }

    public ArrayList<Entity> getPossibleTargets() {
        return getPossibleTargets(false, true);
    }

    @Override
    public ArrayList<Entity> getPossibleTargets(boolean checkNPCs, boolean checkPlayers) {
        ArrayList<Entity> possibleTarget = new ArrayList<Entity>();
        if (theMagister == null)
            return super.getPossibleTargets(false, true);
        for (Player player : theMagister.getInstance().getPlayers()) {
            if (player == null || player.isDead())
                continue;
            possibleTarget.add(player);
        }
        return possibleTarget;
    }

    @Override
    public double getMagePrayerMultiplier() {
        return 0.5;
    }

    @Override
    public double getMeleePrayerMultiplier() {
        return 0.5;
    }

    @Override
    public double getRangePrayerMultiplier() {
        return 0.5;
    }

    @Override
    public void drop() {

    }

}
