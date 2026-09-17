package com.rs.game.npc.eds;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.content.eds.EliteDungeonsConstants;
import com.rs.game.player.content.eds.rooms.EliteDungeonHandledRoom;
import com.rs.utils.data.parsers.npcs.pojos.NPCDrop;

public class SimpleEliteDungeonNPC extends EliteDungeonNPC {

    private static final long serialVersionUID = 4682437915234233617L;

    public SimpleEliteDungeonNPC(int id, WorldTile tile, EliteDungeonHandledRoom room) {
        super(id, tile, room);
        setForceMultiAttacked(true);
        setForceAgressive(true);
        setIntelligentRouteFinder(true);
        setForceTargetDistance(10);
    }

    @Override
    public void drop(Player player) {
        Player killer = getMostDamageReceivedSourcePlayer();
        if (killer == null)
            killer = player;
        if (killer == null)
            return;
        killer.getInventionManager().processScavengingPerk();
        increaseKillStatistics(killer, getName());
        handleRingOfDeath(killer);
        int amountCharms = killer.getInventory().getAmountOf(43066);
        NPCDrop luckyCharm = Math.random() <= (amountCharms == 0 ? 0.005 : 0.02) ? NPCDrop.selectRandomNPCDrop(EliteDungeonsConstants.luckyCharmDrops) : null;
        NPCDrop mobDrop = luckyCharm != null ? luckyCharm : NPCDrop.selectRandomNPCDrop(EliteDungeonsConstants.mobDrops);
        if (luckyCharm != null && amountCharms > 0)
            killer.getInventory().deleteItem(43066, 1);
        if (mobDrop != null)
            sendDrop(killer, mobDrop, false);
    }
}
