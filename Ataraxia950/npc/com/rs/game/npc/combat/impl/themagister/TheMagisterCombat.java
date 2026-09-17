package com.rs.game.npc.combat.impl.themagister;

import com.rs.game.Entity;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.themagister.TheMagister;
import com.rs.game.npc.themagister.TheMagister.TheMagisterAttacks;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

public class TheMagisterCombat extends CombatScript {

    @Override
    public int attack(NPC npc, Entity target) {
        if (!(npc instanceof TheMagister))
            return 0;
        TheMagister theMagister = (TheMagister) npc;
        if (!(target instanceof Player)) {
            theMagister.switchTarget(true);
            return 0;
        }
        if (!Utils.isOnRange(theMagister, target, 6))
            return 0;
        return TheMagisterAttacks.ATTACK.sendAttack(theMagister, (Player) target);
    }

    @Override
    public Object[] getKeys() {
        return new Object[] { 24765 };
    }

}
