package com.rs.game.npc.combat.impl.eds;

import com.rs.game.Entity;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.eds.TheSanctumGuardian;
import com.rs.game.npc.eds.TheSanctumGuardian.TheSanctumGuardianAttacks;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

public class TheSanctumGuardianCombat extends CombatScript {

    @Override
    public int attack(NPC npc, Entity target) {
        if (!(npc instanceof TheSanctumGuardian))
            return 0;
        if (!(target instanceof Player))
            return 0;
        TheSanctumGuardian boss = (TheSanctumGuardian) npc;
        if (!Utils.isOnRange(boss, target, 12))
            return 0;
        return TheSanctumGuardianAttacks.ATTACK.sendAttack(boss, (Player) target);
    }

    @Override
    public Object[] getKeys() {
        return new Object[] { 25587 };
    }

}
