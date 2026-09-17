package com.rs.game.npc.combat.impl.eds;

import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.npc.eds.EliteDungeonNPC;
import com.rs.game.player.Player;

public class DefencePylon extends CombatScript {

    @Override
    public int attack(NPC npc, Entity target) {
        if (!(target instanceof Player))
            return 0;
        if (!(npc instanceof EliteDungeonNPC))
            return 0;
        World.sendProjectileCycles(npc, target, 6981, 35, 35, 5, 60, 30, 0);
        delayHit(npc, 2, target, getMagicHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitionConstants.MAGE, target)));
        return 2;
    }

    @Override
    public Object[] getKeys() {
        return new Object[] { 25596, 25597 };
    }

}
