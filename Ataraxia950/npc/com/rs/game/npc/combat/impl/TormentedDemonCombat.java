package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.npc.others.TormentedDemon;
import com.rs.game.player.Player;
import com.rs.game.player.combat.PlayerCombat;
import com.rs.utils.Utils;

public class TormentedDemonCombat extends CombatScript {

	@Override
	public int attack(NPC npc, Entity target) {
		TormentedDemon demon = (TormentedDemon) npc;
		boolean isDistanced = !Utils.isOnRange(npc, target, 0);
		int style = demon.getFixedCombatType();

		if (isDistanced && demon.getFixedCombatType() == 0)
			demon.setFixedAmount(0);
		if (demon.getFixedAmount() == 0) {
			style = Utils.random(!isDistanced ? 0 : 1, 3);
			demon.setFixedCombatType(style);
			demon.setFixedAmount(Utils.random(3, 7));
		}
		if (style == 0) {
			npc.setNextAnimation(new Animation(10922));
			npc.setNextGraphics(new Graphics(1886));
			delayHit(npc, target, NPCCombatDefinitionConstants.MELEE, 0);
		} else if (style == 1) {
			npc.setNextAnimation(new Animation(10918));
			npc.setNextGraphics(new Graphics(1883, 0, 96 << 16));
			World.sendProjectile(npc, target, 1884, 34, 16, 30, 35, 16, 0);
			if (delayHit(npc, target, NPCCombatDefinitionConstants.MAGE, 1))
				target.setNextGraphics(new Graphics(1883, 1, 100));
		} else if (style == 2) {
			npc.setNextAnimation(new Animation(10919));
			npc.setNextGraphics(new Graphics(1888));
			World.sendProjectile(npc, target, 1887, 34, 16, 30, 35, 16, 0);
			delayHit(npc, target, NPCCombatDefinitionConstants.RANGE, 1);
		}
		demon.setFixedAmount(demon.getFixedAmount() - 1);
		return npc.getCombatDefinitions().getAttackDelay();
	}

	private boolean delayHit(NPC npc, Entity target, int style, int endTime) {
		int damage = Utils.random(210);
		if (target instanceof Player) {
			boolean negateDamage = false;
			Player player = (Player) target;
			negateDamage |= player.getPrayer().isMeleeProtecting() && style == 0;
			negateDamage |= player.getPrayer().isRangeProtecting() && style == 1;
			negateDamage |= player.getPrayer().isMageProtecting() && style == 2;
			if (negateDamage)
				damage = 0;
		}
		target.applyHit(new Hit(npc, damage,
				style == 0 ? HitLook.MELEE_DAMAGE : style == 1 ? HitLook.RANGE_DAMAGE : HitLook.MAGIC_DAMAGE, endTime));
		if (target instanceof Player) {
			Player p2 = (Player) target;
			if (p2.getCombatDefinitions().isAutoRetaliate() && !p2.getActionManager().hasSkillWorking() && !p2.hasWalkSteps()) {
				p2.getActionManager().setAction(new PlayerCombat(npc));
			}
		}
		return damage != 0;
	}

	@Override
	public Object[] getKeys() {
		return new Object[] { "Tormented demon" };
	}
}