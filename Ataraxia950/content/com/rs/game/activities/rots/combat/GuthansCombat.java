package com.rs.game.activities.rots.combat;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.activities.rots.RiseOfTheSix;
import com.rs.game.activities.rots.effects.Impale;
import com.rs.game.activities.rots.effects.RoTSEffect;
import com.rs.game.activities.rots.effects.ShadowDrag;
import com.rs.game.activities.rots.npcs.GuthanNPC;
import com.rs.game.activities.rots.npcs.RiseOfTheSixNPC;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

/**
 * @author Kris | 3. sept 2017 : 23:39.32
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public class GuthansCombat extends CombatScript {

    @Override
    public Object[] getKeys() {
        return new Object[]{18541, 18542};
    }

    @Override
    public int attack(NPC npc, Entity target) {
        final RiseOfTheSix instance = ((RiseOfTheSixNPC) npc).getInstance();
        final GuthanNPC n = (GuthanNPC) npc;
        int damage = getRandomMaxHit(npc, instance.withinShadowRealm() ? 260 : 130, NPCCombatDefinitionConstants.MELEE, target);
        boolean isBlocked = target instanceof Player && ((Player) target).getPrayer().isMeleeProtecting();
        if (!isBlocked && damage > 0 && Utils.random(3) == 0) {
            npc.heal(damage);
            target.setNextGraphics(new Graphics(398));
        }
        if (n.getEffect() != null) {
            if (n.getEffect() instanceof Impale) {
                npc.setNextAnimation(new Animation(18224));
                delayHit(npc, 0, target, getMeleeHit(npc, damage));
                return 3;
            }
            if (!(n.getEffect() instanceof ShadowDrag))
                return 1;
        }
        if (Utils.random(7) == 1 && n.canPerformEffect() && n.getEffect() == null) {
            final RoTSEffect effect = n.generateEffect((Player) target);
            n.setEffect(effect);
            n.setLastEffect(effect);
            return 1;
        }
        npc.setNextAnimation(new Animation(18223));
        delayHit(npc, 0, target, getMeleeHit(npc, damage));
        return 5;
    }

}
