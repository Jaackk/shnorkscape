package com.rs.game.npc.eds;

import java.util.ArrayList;
import java.util.List;

import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.content.eds.EliteDungeonsConstants;
import com.rs.game.player.content.eds.rooms.EliteDungeonHandledRoom;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.npcs.pojos.NPCDrop;

public class SimpleEliteDungeonBoss extends EliteDungeonBoss {

    private static final long serialVersionUID = 3204622146356539919L;

    private final int bossMapId;
    private final WorldTile fightCenter;
    private final int fightRadius;

    public SimpleEliteDungeonBoss(int id, WorldTile tile, EliteDungeonHandledRoom room, int bossMapId, WorldTile fightCenter, int fightRadius) {
        super(id, tile, room);
        this.bossMapId = bossMapId;
        this.fightCenter = fightCenter;
        this.fightRadius = fightRadius;
        setForceMultiAttacked(true);
        setForceAgressive(true);
        setIntelligentRouteFinder(true);
        setForceTargetDistance(16);
    }

    @Override
    public int getBossMapId() {
        return bossMapId;
    }

    public boolean isInsideFightArea(WorldTile tile) {
        if (tile == null || getRoom() == null)
            return false;
        WorldTile center = getRoom().getTile(fightCenter);
        return tile.getPlane() == center.getPlane() && Math.abs(tile.getX() - center.getX()) <= fightRadius && Math.abs(tile.getY() - center.getY()) <= fightRadius;
    }

    @Override
    public ArrayList<Entity> getPossibleTargets(boolean checkNPCs, boolean checkPlayers) {
        ArrayList<Entity> possibleTargets = new ArrayList<Entity>();
        if (getRoom() == null || getRoom().getRoom() == null)
            return possibleTargets;
        if (getCombat().getTarget() != null && isInsideFightArea(getCombat().getTarget()))
            possibleTargets.add(getCombat().getTarget());
        for (Player player : getRoom().getRoom().getPlayers()) {
            if (player == null || player.hasFinished() || player.isDead() || player.getEliteDungeonsManager().isHidden() || possibleTargets.contains(player) || !isInsideFightArea(player) || !Utils.isOnRange(this, player, 16))
                continue;
            possibleTargets.add(player);
        }
        return possibleTargets;
    }

    @Override
    public ArrayList<Entity> getPossibleTargets() {
        return getPossibleTargets(false, true);
    }

    @Override
    public void drop(Player killer) {
        List<Player> players = getDropPlayers();
        if (players.isEmpty() && killer != null)
            players.add(killer);
        for (Player player : players) {
            if (player == null || player.hasFinished())
                continue;
            player.getInventionManager().processScavengingPerk();
            increaseKillStatistics(player, getName());
            handleRingOfDeath(player);
            sendDrop(player, new NPCDrop(995, 100, 75000, 125000), false);
            int amountCharms = player.getInventory().getAmountOf(43066);
            NPCDrop luckyCharm = Math.random() <= (amountCharms == 0 ? 0.005 : 0.04) ? NPCDrop.selectRandomNPCDrop(EliteDungeonsConstants.luckyCharmDrops) : null;
            NPCDrop mobDrop = luckyCharm != null ? luckyCharm : NPCDrop.selectRandomNPCDrop(EliteDungeonsConstants.mobDrops);
            if (luckyCharm != null && amountCharms > 0)
                player.getInventory().deleteItem(43066, 1);
            if (mobDrop != null)
                sendDrop(player, mobDrop, false);
        }
    }
}
