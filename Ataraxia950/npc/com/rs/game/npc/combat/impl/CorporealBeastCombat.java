package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.npc.corp.CorporealBeast;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.Random;

public class CorporealBeastCombat extends CombatScript {

	public final Random random = new Random();
	
	@Override
	public int attack(final NPC npc, final Entity target) {
		final CorporealBeast corp = (CorporealBeast) npc;
		final NPCCombatDefinition defs = npc.getCombatDefinitions();



		int size = npc.getSize();
		final ArrayList<Entity> possibleTargets = corp.getPossibleTargets();
		if (Utils.getRandom(40) <= possibleTargets.size())
			corp.spawnDarkEnergyCore();

		// lets see if this fixes invalid index targeting
		if(possibleTargets.size() == 0)
			return defs.getAttackDelay();
		
		npc.switchTarget();

		boolean stomp = false;
		for (Entity t : possibleTargets) {
			int distanceX = t.getX() - npc.getX();
			int distanceY = t.getY() - npc.getY();
			if (distanceX < size && distanceX > -1 && distanceY < size && distanceY > -1) {
				stomp = true;
				delayHit(npc, 0, t,
						getRegularHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitionConstants.MELEE, t)));
			}
		}
		if (stomp) {
			npc.setNextAnimation(new Animation(10496));
			npc.setNextGraphics(new Graphics(1834));
			return defs.getAttackDelay();
		}
		int attackStyle = Utils.getRandom(4);
		if (attackStyle == 0 || attackStyle == 1) { // melee
			int distanceX = target.getX() - npc.getX();
			int distanceY = target.getY() - npc.getY();
			if (distanceX > size || distanceX < -3 || distanceY > size || distanceY < -3)
				attackStyle = 2 + Utils.getRandom(2); // set mage
			else {
				npc.setNextAnimation(new Animation(attackStyle == 0 ? defs.getAttackEmote() : 10058));
				delayHit(npc, 0, target,
						getMeleeHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitionConstants.MELEE, target)));
				return defs.getAttackDelay();
			}
		}
		if (attackStyle == 2) { // powerfull mage spiky ball
			npc.setNextAnimation(new Animation(10410));
			delayHit(npc, 1, target, getMagicHit(npc, getRandomMaxHit(npc, 650, NPCCombatDefinitionConstants.MAGE, target)));
			World.sendProjectile(npc, target, 1825, 41, 16, 41, 0, 16, 0);
			if (target instanceof Player) {
				Player t = (Player) target;
				if (t.getFamiliar() != null && !t.getFamiliar().isDead() && !t.getFamiliar().hasFinished()) {
					npc.heal(t.getFamiliar().getHitpoints() / 4);
					t.getFamiliar().sendDeath(npc);
					t.sendMessage("The corporeal beast devours your familiar with a powerful attack.", true);
				}
			}
		} else if (attackStyle == 3) { // translucent ball of energy
			npc.setNextAnimation(new Animation(10410));
			delayHit(npc, 1, target, getMagicHit(npc, getRandomMaxHit(npc, 550, NPCCombatDefinitionConstants.MAGE, target)));
			if (target instanceof Player) {
					Player t = (Player) target;
					if (t.getFamiliar() != null && !t.getFamiliar().isDead() && !t.getFamiliar().hasFinished()) {
						npc.heal(t.getFamiliar().getHitpoints() / 4);
						t.getFamiliar().sendDeath(npc);
						t.sendMessage("The corporeal beast devours your familiar with a powerful attack.", true);
					}
				WorldTasksManager.schedule(new WorldTask() {

					@Override
					public void run() {
						int skill = Utils.getRandom(2);
						skill = skill == 0 ? Skills.MAGIC : (skill == 1 ? Skills.SUMMONING : Skills.PRAYER);
						Player player = (Player) target;
						if (skill == Skills.PRAYER)
							player.getPrayer().drainPrayer(10 + Utils.getRandom(40));
						else {
							int lvl = player.getSkills().getLevel(skill);
							lvl -= 1 + Utils.getRandom(4);
							player.getSkills().set(skill, lvl < 0 ? 0 : lvl);
						}
						player.sendMessage("Your " + Skills.SKILL_NAME[skill] + " has been slighly drained!", true);
						this.stop();
					}
				}, 1);
				World.sendProjectile(npc, target, 1823, 41, 16, 41, 0, 16, 0);
			}
		} else {
			if (target instanceof Player) {
				Player t = (Player) target;
				if (t.getFamiliar() != null && !t.getFamiliar().isDead() && !t.getFamiliar().hasFinished()) {
					npc.heal(t.getFamiliar().getHitpoints() / 4);
					t.getFamiliar().sendDeath(npc);
					t.sendMessage("The corporeal beast devours your familiar with a powerful attack.", true);
				}
			}
			npc.setNextAnimation(new Animation(10410));
			final WorldTile tile = new WorldTile(target);
			World.sendProjectile(npc, tile, 1824, 41, 16, 30, 0, 16, 0);

			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					for (int i = 0; i < 6; i++) {
						final WorldTile newTile = new WorldTile(tile, 3);
						if (!World.canMoveNPC(newTile.getPlane(), newTile.getX(), newTile.getY(), 1))
							continue;
						World.sendProjectile(npc, tile, newTile, 1824, 0, 0, 25, 0, 30, 0);
						for (Entity t : possibleTargets) {
							if (Utils.getDistance(newTile.getX(), newTile.getY(), t.getX(), t.getY()) > 1
									|| !t.clipedProjectile(newTile, false))
								continue;
							delayHit(npc, 0, t,
									getMagicHit(npc, getRandomMaxHit(npc, 450, NPCCombatDefinitionConstants.MAGE, t)));
						}
						WorldTasksManager.schedule(new WorldTask() {
							@Override
							public void run() {
								World.sendGraphics(npc, new Graphics(1806), newTile);
								this.stop();
							}
						});
					}
					this.stop();
				}
			}, 1);
		}
		return defs.getAttackDelay();
	}

	@Override
	public Object[] getKeys() {
		return new Object[] { 8133 };
	}
}