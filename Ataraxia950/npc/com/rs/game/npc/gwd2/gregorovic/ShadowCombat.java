package com.rs.game.npc.gwd2.gregorovic;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.NewProjectile;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.content.Combat;
import com.rs.game.route.RouteFinder;
import com.rs.game.route.strategy.FixedTileStrategy;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class ShadowCombat extends Action {

	private final NPC target;

	public ShadowCombat(NPC target) {
		this.target = target;
	}

	private static void addAttackedByDelay(Entity player, Entity target) {
		target.setAttackedBy(player);
		target.setAttackedByDelay(Utils.currentTimeMillis() + 6000); // 8seconds
		player.setAttackingDelay(Utils.currentTimeMillis() + 6000);
	}

	public static void addAttackingDelay(Entity player) {
		player.setAttackingDelay(Utils.currentTimeMillis() + 6000);
	}

	@Override
	public boolean start(Player player) {
		player.setNextFaceEntity(target);
		if (checkAll(player))
			return true;
		player.setNextFaceEntity(null);
		return false;
	}

	@Override
	public boolean process(Player player) {
		return checkAll(player);
	}

	@Override
	public int processWithDelay(Player player) {
		if (target.isDead() || target.hasFinished() || target == null)
			return -1;
		int maxDistance = 7;
		final int distanceX = player.getX() - target.getX();
		final int distanceY = player.getY() - target.getY();
		final int size = target.getSize();
		if (player.hasWalkSteps())
			maxDistance += 1;
		if (distanceX > size + maxDistance || distanceX < -1 - maxDistance || distanceY > size + maxDistance || distanceY < -1 - maxDistance)
			return 0;
		addAttackedByDelay(player);
		return rangeAttack(player);
	}

	private void addAttackedByDelay(Entity player) {
		target.setAttackedBy(player);
		target.setAttackedByDelay(Utils.currentTimeMillis() + 8000);
	}

	private int rangeAttack(final Player player) {
		World.sendProjectile(new NewProjectile(player, target, 6133, 38, 35, 50, 5, 41, -1));
		delayHit(2, 37075, 1, new Hit(player, Utils.random(200, 400), HitLook.RANGE_DAMAGE));
		player.setNextAnimation(new Animation(28228));
		return 3;
	}

	private void delayHit(int delay, final int weaponId, final int attackStyle, final Hit... hits) {
		addAttackedByDelay(hits[0].getSource(), target);
		final NPC target = this.target;
		for (Hit hit : hits) {
			Player player = (Player) hit.getSource();
			final int damage = hit.getDamage() > target.getHitpoints() ? target.getHitpoints() : hit.getDamage();
			double combatXp = damage / 2.5;
			if (combatXp > 0)
				player.getSkills().addXp(Skills.RANGE, combatXp);
		}
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				for (Hit hit : hits) {
					Player player = (Player) hit.getSource();
					if (player.isDead() || player.hasFinished() || target.isDead() || target.hasFinished())
						return;
					doDefenceEmotes();
					if (!target.isUnderCombat() || target.canBeAttackedByAutoRelatie())
						target.setTarget(player);
					target.applyHit(hit);
				}
			}
		}, delay);
	}

	private void doDefenceEmotes() {
		if (target.isCantDoDefenceEmote())
			return;
		target.setNextAnimationNoPriority(new Animation(Combat.getDefenceEmote(target)));
	}

	@Override
	public void stop(final Player player) {
		player.setNextFaceEntity(null);
		if ((player.getAttackedByDelay() - Utils.currentTimeMillis()) <= 0) {
			player.setAttackedBy(null);
			player.setAttackedByDelay(0);
		}
	}

	private boolean checkAll(Player player) {
		if (player.isDead() || player.hasFinished() || player.isCantWalk())
			return false;
		if (target.isDead() || target.hasFinished()) {
			player.setNextAnimation(new Animation(-1));
			return false;
		}
		final int distanceX = player.getX() - target.getX();
		final int distanceY = player.getY() - target.getY();
		final int size = target.getSize();
		final int maxDistance = 5;
		if (distanceX < size && distanceX > -1 && distanceY < size && distanceY > -1 && !target.hasWalkSteps()) {
			player.resetWalkSteps();
			if (!player.addWalkSteps(target.getX() + size, target.getY())) {
				player.resetWalkSteps();
				if (!player.addWalkSteps(target.getX() - 1, target.getY())) {
					player.resetWalkSteps();
					if (!player.addWalkSteps(target.getX(), target.getY() + size)) {
						player.resetWalkSteps();
						return player.addWalkSteps(target.getX(), target.getY() - 1);
					}
				}
			}
			return true;
		}

		if ((!player.clipedProjectile(target, maxDistance == 0)) || distanceX > size + maxDistance || distanceX < -1 - maxDistance || distanceY > size + maxDistance || distanceY < -1 - maxDistance) {
			
			if (!player.hasWalkSteps()) {
				player.resetWalkSteps();
				int steps = RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER, player.getX(), player.getY(), player.getPlane(), player.getSize(), new FixedTileStrategy(target.getX(), target.getY()), true);
				int[] bufferX = RouteFinder.getLastPathBufferX();
				int[] bufferY = RouteFinder.getLastPathBufferY();
				for (int i = steps - 1; i >= 0; i--) {
					if (!player.addWalkSteps(bufferX[i], bufferY[i], 25, true))
						break;
				}
			}
			return true;
		} else
			player.resetWalkSteps();
		player.getTemporaryAttributtes().put("last_target", target);
		if (target != null)
			target.getTemporaryAttributtes().put("last_attacker", player);
		return true;
	}
}