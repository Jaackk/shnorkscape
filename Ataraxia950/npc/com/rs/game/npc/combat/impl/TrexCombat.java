package com.rs.game.npc.combat.impl;

import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.*;
import com.rs.game.npc.NPC;
import com.rs.game.npc.Trex.Trex;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.npc.dragons.RuneDragon;
import com.rs.game.npc.solak.Solak;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import java.util.concurrent.TimeUnit;

public class TrexCombat extends CombatScript {
	private Trex trex;

	@Override
	public int attack(NPC npc, Entity target) {

		final NPCCombatDefinition defs = npc.getCombatDefinitions();
		trex = (Trex) npc;
		final Trex trex = (Trex) npc;

		if (npc.getId() == 26435) {
			if (npc.getHitpoints() <= 8_000) {
				if (npc.withinDistance(target, npc.getSize())) {
					trex.magic1st();
				}
			}
		}

		switch (trex.getPhase()) {
			case 1:


					melee1stAttack(npc, target);



				return 5;

			case 2:


					melee2nd1Attack(trex, npc, target);



				return 12;

		}
		return defs.getAttackDelay();
	}
	public static void melee2nd1Attack(Trex boss, NPC npc, Entity target) {







			npc.setNextForceTalk(new ForceTalk("<col=ff0000>Bite1 Power Coming"));
		boss.setNextAnimation(new Animation(32698));


		for (Entity t : npc.getPossibleTargets()) {
			final WorldTile center = new WorldTile(target);
			World.sendProjectile(npc, target, 5064, 45, 10, 1, 5, 0, 0);
			int damage = getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitionConstants.MAGE, target);
			damage += Utils.random(150, 350);
			delayHit(npc, 1, t, getMagicHit(npc, damage));


		}
		CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
			int loop = 0;
			Trex.PurpleFlameHitBar bar;
			long cycle;

			@Override
			public boolean repeat() {
				try {
					if (boss.isDead() || boss.hasFinished())
						return false;
					if (loop == 0) {
						target.setNextGraphics(new Graphics(4675));
						long totalTime = 3000;
						cycle = totalTime + Utils.currentTimeMillis();
						bar = new Trex.PurpleFlameHitBar(totalTime, cycle);
					}
					if (loop == 0 || loop % 50 == 0)
						target.getNextHitBars().add(bar);
					if (Utils.currentTimeMillis() > cycle) {
						if (boss.isDead() || boss.hasFinished())
							return false;
						boss.addFlame(new WorldTile(target));
						return false;
					}
					loop++;
				} catch (Exception e) {
					Logger.getGlobal().catching(e);
					return false;
				}
				return true;
			}
		}, 1200, 1, TimeUnit.MILLISECONDS);

	}
	public void melee1stAttack(NPC npc, Entity target) {

			npc.setNextForceTalk(new ForceTalk("<col=ff0000>Bite Power"));

		npc.setNextAnimation(new Animation(32695));

		for (Entity t : npc.getPossibleTargets()) {
			final WorldTile center = new WorldTile(target);
			World.sendGraphics(npc, new Graphics(3232), center);
			World.sendGraphics(npc, new Graphics(7048), npc);
			int damage = getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitionConstants.MELEE, target);
			damage += Utils.random(75, 185);
			delayHit(npc, 1, t, getMeleeHit(npc, damage));
		}
	}
	@Override
	public Object[] getKeys() {
		return new Object[] { 26435 };
	}
}