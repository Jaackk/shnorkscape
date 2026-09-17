package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.activities.dfm.DemonFlashMobs;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.ArrayList;

public class DemonFlashBossScript extends CombatScript {

	boolean targetsPraying;

	@Override
	public int attack(NPC npc, Entity target) {
		targetsPraying = false;
		npc.setNextAnimation(new Animation(16990));
		ArrayList<Entity> targets = DemonFlashMobs.getDemonFlashMobs().getBoss().getPossibleTargets();
		int attackStyle = Utils.random(2);
		if (!target.withinDistance(npc, 3))
			attackStyle = 1;
		if (DemonFlashMobs.getDemonFlashMobs().getPrefix().equals("Deacon"))
			attackStyle = 1;
		int damage = 230;
		if (DemonFlashMobs.getDemonFlashMobs().getPrefix().equals("Executioner"))
			damage *= 2;
		targets.forEach(t -> {
			if (t instanceof Player) {
				Player targ = (Player) t;
				if (targ.getPrayer().hasPrayersOn() && !targ.getPrayer().isProtectingItem())
					targetsPraying = true;
			}
		});
		if (targetsPraying)
			damage *= 2;
		if (Utils.random(5) == 0) {
			switch (DemonFlashMobs.getDemonFlashMobs().getSuffix()) {
			case "Blazing":
			case "Rending":
				target.applyHit(new Hit(npc, 5, HitLook.REGULAR_DAMAGE));
				WorldTasksManager.schedule(new WorldTask() {
					@Override
					public void run() {
						target.applyHit(new Hit(npc, 5, HitLook.REGULAR_DAMAGE));
					}
				}, 2);
				break;
			case "Corrupting":
				targets.forEach(t -> {
					if (t.withinDistance(npc, 3))
						t.getPoison().makePoisoned(Utils.random(10, 100));
				});
				break;
			case "Glorious":
			case "Frostborn":
				targets.forEach(t -> {
					if (t.withinDistance(npc, 3)) {
						if (t instanceof Player) {
							Player targ = (Player) t;
							targ.lockROTS();
							WorldTasksManager.schedule(new WorldTask() {
								@Override
								public void run() {
									targ.unlockROTS();
								}
							}, 5);
						}
					}
				});
				break;
			case "Infernal":
				targets.forEach(t -> {
					if (t.withinDistance(npc, 3)) {
						t.applyHit(new Hit(npc, 5, HitLook.REGULAR_DAMAGE));
						WorldTasksManager.schedule(new WorldTask() {
							@Override
							public void run() {
								t.applyHit(new Hit(npc, 5, HitLook.REGULAR_DAMAGE));
							}
						}, 2);
					}
				});
				break;
			case "Obscured":
				targets.forEach(t -> {
					if (t.withinDistance(npc, 3)) {
						if (t instanceof Player) {
							Player targ = (Player) t;
							for (int i = 0; i < 7; i++)
								targ.getSkills().drainLevel(i, Utils.random(4));
						}
					}
				});
				break;
			case "Pestilent":
				target.getPoison().makePoisoned(Utils.random(60, 110));
				break;
			case "Shattering":
				if (attackStyle == 0) {
					targets.forEach(t -> {
						int dmg = 230;
						if (DemonFlashMobs.getDemonFlashMobs().getPrefix().equals("Executioner"))
							dmg *= 2;
						if (targetsPraying)
							dmg *= 2;
						if (t.withinDistance(npc, 3))
							delayHit(npc, 1, t, getMeleeHit(npc, getRandomMaxHit(npc, dmg, NPCCombatDefinitionConstants.MELEE, t)));
					});
				} else
					delayHit(npc, 1, target, getMagicHit(npc, getRandomMaxHit(npc, damage, NPCCombatDefinitionConstants.MAGE, target)));
				return 10;
			}
		}
		if (attackStyle == 0)
			delayHit(npc, 1, target, getMeleeHit(npc, getRandomMaxHit(npc, damage, NPCCombatDefinitionConstants.MELEE, target)));
		else
			delayHit(npc, 1, target, getMagicHit(npc, getRandomMaxHit(npc, damage, NPCCombatDefinitionConstants.MAGE, target)));
		return 10;
	}

	@Override
	public Object[] getKeys() {
		return new Object[] { 16731, 16732 };
	}

}
