package com.rs.game.npc.eds;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.content.eds.rooms.EliteDungeonHandledRoom;
import com.rs.game.player.controllers.EliteDungeonController;

public abstract class EliteDungeonBoss extends EliteDungeonNPC {

    private static final long serialVersionUID = -7348803461753341813L;

    public EliteDungeonBoss(int id, WorldTile tile, EliteDungeonHandledRoom room) {
        super(id, tile, room);
    }

    public abstract int getBossMapId();

    @Override
    public void processHit(Hit hit) {
        super.processHit(hit);
        if (getRoom() == null)
            return;
        for (Player p : getRoom().getRoom().getPlayers()) {
            if (p == null || p.isDead() || p.hasFinished() || !(p.getControlerManager().getControler() instanceof EliteDungeonController))
                continue;
            EliteDungeonController c = (EliteDungeonController) p.getControlerManager().getControler();
            if (c.getCurrent() != this)
                continue;
            c.updateInterface();
        }
    }

    @Override
    public boolean restoreHitPoints() {
        boolean restore = super.restoreHitPoints();
        if (getRoom() == null)
            return restore;
        for (Player p : getRoom().getRoom().getPlayers()) {
            if (p == null || p.isDead() || p.hasFinished() || !(p.getControlerManager().getControler() instanceof EliteDungeonController))
                continue;
            EliteDungeonController c = (EliteDungeonController) p.getControlerManager().getControler();
            if (c.getCurrent() != this)
                continue;
            c.updateInterface();
        }
        return restore;
    }

    @Override
    public double getMagePrayerMultiplier() {
        return 0.40;
    }

    @Override
    public double getMeleePrayerMultiplier() {
        return 0.40;
    }

    @Override
    public double getRangePrayerMultiplier() {
        return 0.40;
    }

    public List<Player> getDropPlayers() {
        List<Player> players = new ArrayList<Player>();
        if (getReceivedDamage() == null)
            return players;
        for (Map.Entry<Entity, Integer> entry : getReceivedDamage().entrySet()) {
            Entity source = entry.getKey();
            if (!(source instanceof Player) || !getPossibleTargets().contains(source)) {
                continue;
            }
            final Integer d = entry.getValue();
            if (d == null || source.hasFinished())
                continue;
            players.add((Player) source);
        }
        return players;
    }

    @Override
    public void processRemoveAgro() {

    }

    @Override
    public boolean isFreezeImmune() {
        return true;
    }

    @Override
    public boolean isStunImmune() {
        return true;
    }
    
}
