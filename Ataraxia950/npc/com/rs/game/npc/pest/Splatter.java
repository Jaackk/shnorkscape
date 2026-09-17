package com.rs.game.npc.pest;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.activites.pest.PestControl;
import com.rs.game.activites.pest.PestControlObject;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("serial")
public class Splatter extends PestMonsters {

	public Splatter(int id, WorldTile tile, int mapAreaNameHash, int index, PestControl manager) {
		super(id, tile, mapAreaNameHash, index, manager);
		manager.addNPC(this);
		objects = new ArrayList<PestControlObject>();
		for (int i = 0; i < getBonuses().length; i++)
			setBonus(i, i == 8 ? getCombatLevel() : getCombatLevel() * 2);
	}
	
	private List<PestControlObject> objects;

	@Override
	public void processNPC() {
		if (!isDead() && manager.getPestControlObjects() != null && !manager.getPestControlObjects().isEmpty()) {
			objects.addAll(manager.getPestControlObjects());
			objects = objects.stream().filter(o -> Utils.getDistance(this, o) <= 15 && (o.getDefinitions().getName().toLowerCase().contains("gate") && o.getId() != 91335 && o.getId() != 91339) || (o.getDefinitions().getName().toLowerCase().contains("barricade") && o.getId() >= 14224 && o.getId() <= 14226)).sorted(new Comparator<PestControlObject>() {
				@Override
				public int compare(PestControlObject o1, PestControlObject o2) {
					int dist1 = Utils.getDistance(Splatter.this, o1);
					int dist2 = Utils.getDistance(Splatter.this, o2);
					if (dist1 > dist2)
						return 1;
					else if (dist1 < dist2)
						return -1;
					else
						return 0;
				}
			}).collect(Collectors.toList());
			if (!objects.isEmpty()) {
				resetCombat();
				getCombat().removeTarget();
				for (PestControlObject o : objects) {
					if (o == null) {
						objects.clear();
						return;
					}
					if (!Utils.isOnRange(this, o, 1, getSize(), 1)) {
						resetWalkSteps();
						calcFollow(o, 4, true, isIntelligentRouteFinder());
					} else
						applyHit(new Hit(null, getHitpoints(), HitLook.REGULAR_DAMAGE));
					objects.clear();
					return;
				}
				objects.clear();
			}

		}

		super.processNPC();
	}
	
	public boolean canWalkNPC(int toX, int toY) {
		return true;
	}

	@Override
	public void sendDeath(Entity source) {
		final NPCCombatDefinition defs = getCombatDefinitions();
		resetWalkSteps();
		getCombat().removeTarget();
		setNextAnimation(null);
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0)
					sendExplosion();
				else if (loop >= defs.getDeathDelay()) {
					reset();
					stop();
				}
				loop++;
			}
		}, 0, 1);
	}

	private void sendExplosion() {
		final Splatter splatter = this;
		setNextAnimation(new Animation(3888));
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				setNextAnimation(new Animation(3889));
				setNextGraphics(new Graphics(649 + (getId() - 3727)));
				WorldTasksManager.schedule(new WorldTask() {

					@Override
					public void run() {
						finish();
						for (Entity e : getPossibleTargets())
							e.applyHit(new Hit(splatter, Utils.getRandom(400), HitLook.REGULAR_DAMAGE));
						if (manager.getPestControlObjects() != null && !manager.getPestControlObjects().isEmpty()) {
							manager.getPestControlObjects().forEach(o -> {
								if (o != null && o.withinDistance(splatter, 2))
									o.destroyObject(splatter);
							});
						}
					}
				});
			}
		});
	}

	@Override
	public ArrayList<Entity> getPossibleTargets() {
		ArrayList<Entity> possibleTarget = new ArrayList<Entity>();
		for (Player player : manager.getPlayers()) {
			if (player == null || player.isDead() || player.hasFinished() || !player.isRunning() || !player.withinDistance(this, 2))
				continue;
			possibleTarget.add(player);
		}
		List<Integer> npcsIndexes = World.getRegion(getRegionId()).getNPCsIndexes();
		if (npcsIndexes != null) {
			for (int npcIndex : npcsIndexes) {
				NPC npc = World.getNPCs().get(npcIndex);
				if (npc == null || npc == this || npc.isDead() || npc.hasFinished() || !npc.withinDistance(this, 2))
					continue;
				possibleTarget.add(npc);
			}
		}
		return possibleTarget;
	}
}
