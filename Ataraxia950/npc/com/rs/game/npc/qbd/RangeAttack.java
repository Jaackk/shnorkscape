package com.rs.game.npc.qbd;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.player.Player;
import com.rs.game.player.combat.PlayerCombat;
import com.rs.game.player.content.Combat;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * Handles the Queen Black Dragon's range attack.
 *
 * @author Emperor
 */
public final class RangeAttack implements QueenAttack {

	/**
	 * The animation.
	 */
	private static final Animation ANIMATION = new Animation(16718);

	@Override
	public int attack(final QueenBlackDragon npc, final Player victim) {
		npc.setNextAnimation(ANIMATION);
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				stop();
				int hit;
				if (victim.getPrayer().usingPrayer(1, 12)) {
					victim.setNextAnimation(new Animation(12573));
					victim.setNextGraphics(new Graphics(2229));
					victim.getPackets().sendGameMessage("You are unable to reflect damage back to this creature.");
					hit = 0;
				} else if (victim.getPrayer().usingPrayer(0, 12)) {
					victim.setNextAnimation(new Animation(Combat.getDefenceEmote(victim)));
					hit = 0;
				} else {
					hit = Utils.random(0 + Utils.random(150), 360);
					victim.setNextAnimation(new Animation(Combat.getDefenceEmote(victim)));
				}
                if(!(npc.isDead() || npc.hasFinished())) {
				victim.applyHit(new Hit(npc, hit, hit == 0 ? HitLook.MISSED : HitLook.RANGE_DAMAGE));
				if (victim.getCombatDefinitions().isAutoRetaliate() && !victim.getActionManager().hasSkillWorking() && !victim.hasWalkSteps())
					victim.getActionManager().setAction(new PlayerCombat(npc));
                }
			}
		}, 1);
		return Utils.random(4, 15);
	}

	@Override
	public boolean canAttack(QueenBlackDragon npc, Player victim) {
		return true;
	}

}