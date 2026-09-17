package com.rs.game.npc.combat.impl;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

public class RevenantCombat extends CombatScript {

	@Override
	public int attack(final NPC npc, final Entity target) {
		final NPCCombatDefinition defs = npc.getCombatDefinitions();
		if (npc.getId() == 13481 && Utils.random(5) == 0)
			target.getPoison().makePoisoned(50);
		int attackStyle = Utils.random(3);
		if (target instanceof Player) {
			Player targetPlayer = (Player) target;
			if (attackStyle != 2)
				targetPlayer.getPackets().sendSound(202, 0, 1);
			if (Utils.random(2) == 0) {
				if (targetPlayer.getPrayer().isMageProtecting() && attackStyle == 0)
					attackStyle = Utils.random(1, 3);
				if (targetPlayer.getPrayer().isRangeProtecting() && attackStyle == 1)
					attackStyle = Utils.random(2) == 0 ? 0 : 1;
				if (targetPlayer.getPrayer().isMeleeProtecting() && attackStyle == 2)
					attackStyle = Utils.random(0, 2);
			}
		}
		if (attackStyle == 2) { // checks if can melee
			int distanceX = target.getX() - npc.getX();
			int distanceY = target.getY() - npc.getY();
			int size = npc.getSize();
			if ((distanceX > size || distanceX < -1 || distanceY > size || distanceY < -1))
				attackStyle = Utils.random(2);
		}

		switch (attackStyle) {
		case 0: // magic
			int damage = getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitionConstants.MAGE, target);
			delayHit(npc, 2, target, getMagicHit(npc, damage));
			World.sendProjectile(npc, target, 1276, 34, 16, 30, 35, 16, 0);
			npc.setNextAnimation(new Animation(getMagicAnimation(npc)));
			return 5;
		case 1: // range
			delayHit(npc, 2, target, getRangeHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitionConstants.RANGE, target)));
			World.sendProjectile(npc, target, 1278, 34, 16, 30, 35, 16, 0);
			npc.setNextAnimation(new Animation(getRangeAnimation(npc)));
			return 5;
		default: // melee
			delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitionConstants.MELEE, target)));
			npc.setNextAnimation(new Animation(defs.getAttackEmote()));
			return 5;
		}
	}

	@Override
	public Object[] getKeys() {
		return new Object[] { 13465, 13466, 13467, 13468, 13469, 13470, 13471, 13472, 13473, 13474, 13475, 13476, 13477,
				13478, 13479, 13480, 13481 };
	}

	public int getMagicAnimation(NPC npc) {
		switch (npc.getId()) {
		case 13465:
			return 7500;
		case 13466:
		case 13467:
		case 13468:
		case 13469:
			return 7499;
		case 13470:
		case 13471:
			return 7506;
		case 13472:
			return 7503;
		case 13473:
			return 7507;
		case 13474:
			return 7496;
		case 13475:
			return 7497;
		case 13476:
			return 7515;
		case 13477:
			return 7498;
		case 13478:
			return 7505;
		case 13479:
			return 7515;
		case 13480:
			return 7508;
		case 13481:
		default:
			// melee emote, better than 0
			return npc.getCombatDefinitions().getAttackEmote();
		}
	}

	public int getRangeAnimation(NPC npc) {
		switch (npc.getId()) {
		case 13465:
			return 7501;
		case 13466:
		case 13467:
		case 13468:
		case 13469:
			return 7513;
		case 13470:
		case 13471:
			return 7519;
		case 13472:
			return 7516;
		case 13473:
			return 7520;
		case 13474:
			return 7521;
		case 13475:
			return 7510;
		case 13476:
			return 7501;
		case 13477:
			return 7512;
		case 13478:
			return 7518;
		case 13479:
			return 7514;
		case 13480:
			return 7522;
		case 13481:
		default:
			// melee emote, better than 0
			return npc.getCombatDefinitions().getAttackEmote();
		}
	}
}
