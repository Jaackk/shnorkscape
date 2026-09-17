package com.rs.game.npc.combat.impl.spiderboss;

import com.rs.game.Entity;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.spiderboss.Araxxor;
import com.rs.game.npc.spiderboss.Araxxor.AraxxorAttacks;
import com.rs.game.player.Player;

public class AraxxorCombat extends CombatScript {

    @Override
    public Object[] getKeys() {
        return new Object[] { "Araxxor", "Araxxi", 19465, 19466, 19467 };
    }

    @Override
    public int attack(NPC npc, Entity target) {
        if (!(npc instanceof Araxxor))
            return 0;
        if (!(target instanceof Player)) {
            npc.checkAgressivity();
            return 0;
        }
        Araxxor spider = (Araxxor) npc;
        return AraxxorAttacks.ATTACK.sendAttack(spider, (Player) target);
    }

}
