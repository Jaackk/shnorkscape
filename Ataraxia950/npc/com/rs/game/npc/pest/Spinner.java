package com.rs.game.npc.pest;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldTile;
import com.rs.game.activites.pest.PestControl;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class Spinner extends PestMonsters {

	private byte healTicks;

	public Spinner(int id, WorldTile tile, int mapAreaNameHash, int index, PestControl manager) {
		super(id, tile, mapAreaNameHash, index, manager);
		manager.addNPC(this);
		for (int i = 0; i < getBonuses().length; i++)
			setBonus(i, i == 8 ? getCombatLevel() : getCombatLevel() * 2);
	}
	
	public boolean canWalkNPC(int toX, int toY) {
		return true;
	}

	private void explode() {
		final NPC npc = this;
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				for (Player player : manager.getPlayers()) {
					if (!withinDistance(player, 7))
						continue;
					player.getPoison().makePoisoned(50);
					player.applyHit(new Hit(npc, 50, HitLook.REGULAR_DAMAGE));
					npc.reset();
					npc.finish();
				}
			}
		}, 1);
	}

	private void healPortal(final PestPortal portal) {
		setNextFaceEntity(portal);
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				setNextAnimation(new Animation(3911));
				setNextGraphics(new Graphics(658, 0, 96 << 16));
				if (portal.getHitpoints() != 0 && portal.getHitpoints() < portal.getMaxHitpoints())
					portal.heal((portal.getMaxHitpoints() / portal.getHitpoints()) * 45);
				healTicks = 0; /* Saves memory in the long run. Meh */
			}
		});
	}

	@Override
	public void processNPC() {
		PestPortal portal = manager.getPortals()[portalIndex];
		if (portal.isDead()) {
			explode();
			return;
		}
		if (!portal.isLocked && !portal.isDead() && !portal.hasFinished()) {
			healTicks++;
			if (!Utils.isOnRange(this, portal, 1))
				calcFollow(portal, 4, true, isIntelligentRouteFinder());
			else if (healTicks % 6 == 0)
				healPortal(portal);
		}
	}

	@Override
	public ArrayList<Entity> getPossibleTargets() {
		ArrayList<Entity> possibleTarget = new ArrayList<Entity>();
		for (Player player : manager.getPlayers()) {
			if (player == null || player.isDead() || player.hasFinished() || !player.isRunning()
					|| !player.withinDistance(this, 10))
				continue;
			possibleTarget.add(player);
		}
		return possibleTarget;
	}
}
