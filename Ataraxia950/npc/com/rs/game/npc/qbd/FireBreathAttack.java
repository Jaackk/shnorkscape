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
 * Represents a default fire breath attack.
 *
 * @author Emperor
 */
public final class FireBreathAttack implements QueenAttack {

	/**
	 * The animation of the attack.
	 */
	private static final Animation ANIMATION = new Animation(16721);

	/**
	 * The graphic of the attack.
	 */
	private static final Graphics GRAPHIC = new Graphics(3143);

	@Override
	public int attack(final QueenBlackDragon npc, final Player victim) {
		npc.setNextAnimation(ANIMATION);
		npc.setNextGraphics(GRAPHIC);
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				super.stop();

				String message = Combat.getProtectMessage(victim);
				int hit = Utils.random(400, 710);
				if (message != null) {
					if (message != null) {
						victim.sendMessage(message, true);
						if (message.contains("fully")) {
							hit *= 0.1;
						}
						if (message.contains("most")) {
							hit *= 0.12;
						}
						if (message.contains("some")) {
							hit *= 0.5;
						}
					}
				} else {
					victim.sendMessage("You are hit by the dragon's fiery breath!", true);
				}
                if(!(npc.isDead() || npc.hasFinished())) {
				victim.setNextAnimation(new Animation(Combat.getDefenceEmote(victim)));
				victim.applyHit(new Hit(npc, hit, HitLook.REGULAR_DAMAGE));
				if (victim.getCombatDefinitions().isAutoRetaliate() && !victim.getActionManager().hasSkillWorking() && !victim.hasWalkSteps())
					victim.getActionManager().setAction(new PlayerCombat(npc));
                }
			}
		}, 1);
		return Utils.random(4, 15); // Attack delay seems to be random a lot.
	}

	@Override
	public boolean canAttack(QueenBlackDragon npc, Player victim) {
		return true;
	}
}