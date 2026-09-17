package com.rs.game.npc.combat.impl.eds;

import com.rs.game.Entity;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.eds.SeiryuTheAzureSerpent;
import com.rs.game.npc.eds.SeiryuTheAzureSerpent.SeiryuTheAzureSerpentAttacks;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

public class SeiryuTheAzureSerpentCombat extends CombatScript {

    @Override
    public int attack(NPC npc, Entity target) {
        if (!(npc instanceof SeiryuTheAzureSerpent))
            return 0;
        if (!(target instanceof Player))
            return 0;
        SeiryuTheAzureSerpent boss = (SeiryuTheAzureSerpent) npc;
        if (!Utils.isOnRange(boss, target, 30))
            return 0;
        return SeiryuTheAzureSerpentAttacks.ATTACK.sendAttack(boss, (Player) target);
    }

    @Override
    public Object[] getKeys() {
        return new Object[] { 25593 };
    }

}
