package com.rs.game.activities.rots.effects;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.activities.rots.npcs.GuthanNPC;
import com.rs.game.activities.rots.npcs.RiseOfTheSixNPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kris | 3. sept 2017 : 23:35.04
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public class Impale extends RoTSEffect {

	public Impale(int duration, RiseOfTheSixNPC npc, Player player) {
		super(duration, npc, player);
	}
	
	/**
	 * Generates a random player on the same side as guthan himself while trying to avoid his primary target.
	 * If no other targets besides the primary exist, returns the primary.
	 * @return target.
	 */
	private final Player generatePlayer() {
		if (instance.getPlayers().size() == 0)
			return null;
		final WorldTile center = instance.getWorldTile(34, 20);
		final List<Player> targets = new ArrayList<Player>();
		for (Player p : instance.getPlayers()) {
			if (p.equals(npc.getTarget()))
				continue;
			if (npc.getX() > center.getX() && p.getX() > center.getX() || npc.getX() < center.getX() && p.getX() < center.getX())
				targets.add(p);
		}
		if (targets.size() == 0)
			return player;
		return targets.get(Utils.random(targets.size()));
	}
	
	@Override
	public void start() {
		for (Player players : instance.getPlayers()) 
			players.sendMessage("Guthan prepares to throw his spear!");
		final Player target = generatePlayer();
		if (target == null) {
			npc.finishEffect();
			return;
		}
		npc.faceEntity(target);
		WorldTasksManager.schedule(new WorldTask() {
			private int tick;
			private final WorldTile center = instance.getWorldTile(34, 20);
			@Override
			public void run() {
				if (cancel()) {
					npc.finishEffect();
					((GuthanNPC) npc).setImpaledPlayer(null);
					npc.transformIntoNPC(18541);
					stop();
					return;
				}
				if (tick == 0) {
					target.sendMessage("Guthan throws his spear at you!");
					((GuthanNPC) npc).setImpaledPlayer(target);
					npc.setNextAnimation(new Animation(21944));
					World.sendProjectile(npc, new WorldTile(npc), new WorldTile(target), 4411, 40, 40, 30, 20, 0, 0);
					npc.transformIntoNPC(npc.getId() + 1);
				} else if (npc.withinDistance(target, 1) || target.isDead() || target.getX() > center.getX() && npc.getX() < center.getX() || target.getX() < center.getX() && npc.getX() > center.getX()) {
					npc.faceEntity(target);
					target.applyHit(new Hit(npc, instance.withinShadowRealm() ? 200 : 100, HitLook.REGULAR_DAMAGE));
					target.setNextAnimation(new Animation(21945));
					npc.setNextAnimation(new Animation(21947));
					((GuthanNPC) npc).setImpaledPlayer(null);
					npc.finishEffect();
					npc.refreshSpecialDelay();
					npc.transformIntoNPC(npc.getId() - 1);
					npc.getCombat().addCombatDelay(3);
					stop();
					return;
				} else { 
					target.applyHit(new Hit(npc, instance.withinShadowRealm() ? Utils.random(80, 100) : Utils.random(40, 50), HitLook.REGULAR_DAMAGE));
				    target.setNextGraphics(new Graphics(4407, 0, 100));
				}
				tick++;
			}
		}, 0, 0);

	}

}
