package com.rs.game.npc.combat.impl.eds;

import com.rs.game.Entity;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.eds.MasutaTheAscended;
import com.rs.game.npc.eds.MasutaTheAscended.MasutaTheAscendedAttacks;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

public class MasutaTheAscendedCombat extends CombatScript {

    @Override
    public int attack(NPC npc, Entity target) {
        if (!(npc instanceof MasutaTheAscended))
            return 0;
        if (!(target instanceof Player))
            return 0;
        MasutaTheAscended boss = (MasutaTheAscended) npc;
        if (!Utils.isOnRange(boss, target, 7))
            return 0;
        if (!boss.isForceFollowClose())
            boss.resetWalkSteps();
        return MasutaTheAscendedAttacks.ATTACK.sendAttack(boss, (Player) target);
    }

    @Override
    public Object[] getKeys() {
        return new Object[] { "Masuta the Ascended" };
    }

}
