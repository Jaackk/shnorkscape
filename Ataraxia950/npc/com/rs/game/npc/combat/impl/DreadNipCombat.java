package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.npc.others.DreadNip;
import com.rs.utils.Utils;

public class DreadNipCombat extends CombatScript {

    private final String[] DREADNIP_ATTACK_MESSAGE = { "Your dreadnip stunned its target!", "Your dreadnip poisoned its target!" };

    @Override
    public int attack(NPC npc, Entity target) {
        DreadNip dreadNip = (DreadNip) npc;
        if (dreadNip.getTicks() <= 3)
            return 0;
        npc.setNextAnimation(new Animation(14485));
        int attackStyle = Utils.random(3);
        switch (attackStyle) {
        case 0:
            break;
        case 1:
            int secondsDelay = 6 + Utils.getRandom(3);
            target.addFreezeDelay(secondsDelay * 1000);
            dreadNip.setNextForceTalk(new ForceTalk("Sstk!"));
            break;
        case 2:
            target.getPoison().makePoisoned(108);
            break;
        }
        if (attackStyle != 0)
            dreadNip.getOwner().getPackets().sendGameMessage(DREADNIP_ATTACK_MESSAGE[attackStyle - 1]);
        if (dreadNip.getId() == 20458)
            delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, 300, dreadNip.getId() == 20458 ? NPCCombatDefinitionConstants.MELEE : dreadNip.getId() == 20459 ? NPCCombatDefinitionConstants.RANGE : dreadNip.getId() == 20460 ? NPCCombatDefinitionConstants.MAGE : NPCCombatDefinitionConstants.MELEE, target)));
        else if (dreadNip.getId() == 20459)
            delayHit(npc, 0, target, getRangeHit(npc, getRandomMaxHit(npc, 300, dreadNip.getId() == 20458 ? NPCCombatDefinitionConstants.MELEE : dreadNip.getId() == 20459 ? NPCCombatDefinitionConstants.RANGE : dreadNip.getId() == 20460 ? NPCCombatDefinitionConstants.MAGE : NPCCombatDefinitionConstants.MELEE, target)));
        else if (dreadNip.getId() == 20460)
            delayHit(npc, 0, target, getMagicHit(npc, getRandomMaxHit(npc, 300, dreadNip.getId() == 20458 ? NPCCombatDefinitionConstants.MELEE : dreadNip.getId() == 20459 ? NPCCombatDefinitionConstants.RANGE : dreadNip.getId() == 20460 ? NPCCombatDefinitionConstants.MAGE : NPCCombatDefinitionConstants.MELEE, target)));
        dreadNip.setTimesAttacked(dreadNip.getTimesAttacked() + 1);
        return 6;
    }

    @Override
    public Object[] getKeys() {
        return new Object[] { 20458, 20459, 20460 };
    }
}
