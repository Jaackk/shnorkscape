package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.utils.Utils;

public class GanodermicCombat extends CombatScript {

    @Override
    public int attack(NPC n, Entity target) {
        NPCCombatDefinition defs = n.getCombatDefinitions();
        n.setNextAnimation(new Animation(15470));
        n.setNextGraphics(new Graphics(2034));
        World.sendProjectile(n, target, 2034, 10, 18, 50, 50, 0, 0);
        delayHit(n, 1, target, getMagicHit(n, Utils.random(400)));
        return defs.getAttackDelay();
    }

    @Override
    public Object[] getKeys() {
        return new Object[] { 14696, 14697 };
    }
}