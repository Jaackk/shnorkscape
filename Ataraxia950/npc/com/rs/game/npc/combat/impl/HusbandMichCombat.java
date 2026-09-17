package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.activites.quest.deathsbounty.DeathsBounty;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;

import java.util.concurrent.ThreadLocalRandom;

public class HusbandMichCombat extends CombatScript {

    @Override
    public int attack(final NPC npc, final Entity target) {
        int index = ThreadLocalRandom.current().nextInt(7);
        int graphic = -1;
        double damageMod = 1.0;
        switch (index) {
            case 0:
            case 1:
                graphic = 369;
                target.setFreezeDelay(3);
                break;
            case 2:
            case 3:
            case 4:
                graphic = 377;
                if(npc.getHealthPercentage() < 25) {
                    npc.heal((int) (npc.getMaxHitpoints() * 0.07));
                } else {
                    npc.heal((int) (npc.getMaxHitpoints() * 0.04));
                }
                break;
            case 5:
            case 6:
                graphic = 1854;
                npc.setNextGraphics(new Graphics(1853));
                damageMod = 1.5;
                break;
        }
        final NPCCombatDefinition defs = npc.getCombatDefinitions();
        npc.setNextAnimation(new Animation(defs.getAttackEmote()));
        int finalGraphic = graphic;
        delayHit(npc, 2, target, () -> target.setNextGraphics(new Graphics(finalGraphic)), getMagicHit(npc, getMaxHit(npc, NPCCombatDefinitionConstants.MAGE, target, damageMod, ThreadLocalRandom.current().nextInt(3) == 0)));
        return defs.getAttackDelay();
    }

    @Override
    public Object[] getKeys() {
        return new Object[]{DeathsBounty.HUSBAND_MICH_ID};
    }
}
