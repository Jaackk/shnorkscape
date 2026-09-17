package com.rs.game.npc.qbd;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.combat.PlayerCombat;
import com.rs.game.player.content.Combat;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * Handles the super dragonfire attack.
 *
 * @author Emperor
 */
public final class SuperFireAttack implements QueenAttack {

	/**
	 * The animation.
	 */
	private static final Animation ANIMATION = new Animation(16745);

	/**
	 * The graphics.
	 */
	private static final Graphics GRAPHIC = new Graphics(3152);

	@Override
	public int attack(final QueenBlackDragon npc, final Player victim) {
		npc.setNextAnimation(ANIMATION);
		npc.setNextGraphics(GRAPHIC);
		victim.sendMessage(
				"<col=FFCC00>The Queen Black Dragon gathers her strength to breath extremely hot flames.</col>");
		WorldTasksManager.schedule(new WorldTask() {
			int count = 0;

			@Override
			public void run() {
				int hit = 195 + Utils.random(195);
				if (victim.getInventory().containsItem(24337, 1)) {
					if (victim.getSkills().getLevelForXp(Skills.SMITHING) >= 70) {
						victim.getInventory().deleteItem(24337, 1);
						Item crossbow = new Item(24338);
						victim.getChargesManagerNew().initChargesData(crossbow, false);
						victim.getInventory().addItem(crossbow);
						victim.sendMessage("<col=FFCC00>The Dragon's breath forges your Royal Crossbow!</col>");
					} else
						victim.sendMessage("You need at least a level of 70 smithing to forge your Royal Crossbow.");
				}
				int distance = Utils.getDistance(npc.getBase().transform(33, 31, 0), victim);
				hit /= (distance / 3) + 1;
				victim.setNextAnimation(new Animation(Combat.getDefenceEmote(victim)));
				victim.applyHit(new Hit(npc, hit, HitLook.REGULAR_DAMAGE));
				if (victim.getCombatDefinitions().isAutoRetaliate() && !victim.getActionManager().hasSkillWorking() && !victim.hasWalkSteps())
						victim.getActionManager().setAction(new PlayerCombat(npc));
				if (++count == 3) {
					stop();
				}
			}
		}, 4, 1);
		return Utils.random(8, 15);
	}

	@Override
	public boolean canAttack(QueenBlackDragon npc, Player victim) {
		return true;
	}
}