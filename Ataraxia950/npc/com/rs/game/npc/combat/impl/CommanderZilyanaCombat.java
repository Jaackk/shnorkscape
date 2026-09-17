package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.npc.godwars.saradomin.CommanderZilyana;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class CommanderZilyanaCombat extends CombatScript {

	@SuppressWarnings("unused")
	@Override
	public int attack(final NPC npc, final Entity target) {
		final NPCCombatDefinition defs = npc.getCombatDefinitions();
		boolean hardMode = ((CommanderZilyana) npc).isHardMode();
		if (Utils.getRandom(4) == 0) {
			switch (Utils.getRandom(9)) {
			case 0:
				npc.setNextForceTalk(new ForceTalk("Death to the enemies of the light!"));
				npc.playSound(3247, 2);
				break;
			case 1:
				npc.setNextForceTalk(new ForceTalk("Slay the evil ones!"));
				npc.playSound(3242, 2);
				break;
			case 2:
				npc.setNextForceTalk(new ForceTalk("Saradomin lend me strength!"));
				npc.playSound(3263, 2);
				break;
			case 3:
				npc.setNextForceTalk(new ForceTalk("By the power of Saradomin!"));
				npc.playSound(3262, 2);
				break;
			case 4:
				npc.setNextForceTalk(new ForceTalk("May Saradomin be my sword."));
				npc.playSound(3251, 2);
				break;
			case 5:
				npc.setNextForceTalk(new ForceTalk("Good will always triumph!"));
				npc.playSound(3260, 2);
				break;
			case 6:
				npc.setNextForceTalk(new ForceTalk("Forward! Our allies are with us!"));
				npc.playSound(3245, 2);
				break;
			case 7:
				npc.setNextForceTalk(new ForceTalk("Saradomin is with us!"));
				npc.playSound(3266, 2);
				break;
			case 8:
				npc.setNextForceTalk(new ForceTalk("In the name of Saradomin!"));
				npc.playSound(3250, 2);
				break;
			case 9:
				npc.setNextForceTalk(new ForceTalk("Attack! Find the Godsword!"));
				npc.playSound(3258, 2);
				break;
			}
		}
		if (((CommanderZilyana) npc).isHardMode() && ((CommanderZilyana) npc).isSecondHalf()) {
			if (Utils.getRandom(2) == 0) {
				WorldTasksManager.schedule(new WorldTask() {
					int ticks;
					WorldTile tile = null;

					@Override
					public void run() {
						if (ticks == 2)
							stop();
						for (int i = 0; i < 10; i++) {
							tile = new WorldTile(npc, 5);
							if (World.isTileFree(tile.getPlane(), tile.getX(), tile.getY(), 1))
								break;
						}
						if (tile != null) {
							World.sendGraphics(null, new Graphics(5019), tile);
							npc.getPossibleTargets().forEach(t -> {
								if (t.getHash() == tile.getHash())
									t.applyHit(new Hit(npc, Utils.random(200), HitLook.REGULAR_DAMAGE));
							});
						}
						ticks++;
					}
				}, 0, 1);
			}
		}
		if (Utils.getRandom(1) == 0) { // mage magical attack
			npc.setNextAnimation(new Animation(6967));
			for (Entity t : npc.getPossibleTargets()) {
				if (!t.withinDistance(npc, 3))
					continue;
				int damage = getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitionConstants.MAGE, t);
				if (damage > 0) {
					delayHit(npc, 1, t, getMagicHit(npc, damage));
					t.setNextGraphics(new Graphics(1194));
				}
			}

		} else { // melee attack
			npc.setNextAnimation(new Animation(defs.getAttackEmote()));
			delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitionConstants.MELEE, target)));
		}
		return defs.getAttackDelay();
	}

	@Override
	public Object[] getKeys() {
		return new Object[] { 6247 };
	}
}
