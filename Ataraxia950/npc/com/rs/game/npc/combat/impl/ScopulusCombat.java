package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.vorago.Scopulus;
import com.rs.game.player.content.Combat;
import com.rs.utils.Utils;

import java.util.ArrayList;

public class ScopulusCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { 17185 };
	}

	@Override
	public int attack(NPC npc, Entity target) {
		Scopulus scop = (Scopulus) npc;
		NPCCombatDefinition def = scop.getCombatDefinitions();
		if (!Utils.isOnRange(npc, target, 0))
			return 0;
		if (Math.random() <= 0.1) {
			scop.switchTarget(true);
			return 0;
		}
		scop.setNextAnimation(new Animation(def.getAttackEmote()));
		ArrayList<Entity> possibleTargets = new ArrayList<Entity>();
		for (Entity e : scop.getPossibleTargets()) {
			if (Utils.isOnRange(scop, e, 0))
				possibleTargets.add(e);
		}
		for (Entity e : scop.getPossibleTargets()) {
			if (Utils.isOnRange(target, e, scop.isEnraged() ? 1 : 0) && !possibleTargets.contains(e))
				possibleTargets.add(e);
		}
		if (possibleTargets.isEmpty())
			return 0;
		for (Entity e : possibleTargets) {
			if (e == null || e.hasFinished() || e.isDead())
				continue;
			int damage = getMaxHit(scop, 250, Combat.MELEE_TYPE, e);
			damage *= scop.isEnraged() ? 1.2 : 1;
			delayHit(scop, 0, e, getMeleeHit(npc, damage));
		}
		return scop.isEnraged() ? 2 : 4;
	}
}
