package com.rs.game.npc.combat.impl.gwd2;

import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.NewProjectile;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.npc.gwd2.gregorovic.Gregorovic;
import com.rs.game.npc.gwd2.gregorovic.Spirit;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GregorovicCombat extends CombatScript {
	
	private static final ForceTalk RISE_FTALK = new ForceTalk("RISE, CHILD!");
	public static final int POISON_CAP = 300;
	private static final int SHURIKEN = 28228, 
			SHURIKEN_PROJ = 6132, 
			TRICK_KNIFE = 28229,
			TRICK_KNIFE_PROJ = 6135, 
			MANIA = 22450, 
			RISE_PROJ = 2263, 
			GLAIVE_THROW = 28494, 
			GLAIVE_THROW_GFX = 6139;
	
	
	@Override
	public Object[] getKeys() {
		return new Object[] { 22442, 22443 };
	}

	/**
	 * Sends the basic glaive attack.
	 */
	private final int shurikenAttack(Gregorovic npc, Entity target) {
		npc.setNextAnimation(new Animation(SHURIKEN));
		final NewProjectile projectile = new NewProjectile(npc, target, SHURIKEN_PROJ, 35, 40, 25, 10, 35, 0);
		World.sendProjectile(projectile);
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				delayHit(npc, 0, target, getRangeHit(npc, getRandomMaxHit(npc, (int) ((npc.getInstance().isHardMode() ? 345 : 230) * npc.getDamageBoost()), NPCCombatDefinitionConstants.RANGE, target)));
			}
		}, 1);
		return 3;
	}

	/**
	 * Sends the trick knife attack:
	 * NPC -> Target -> NPC -> Random possible target -> NPC -> Random possible target.
	 */
	private final int trickKnife(Gregorovic npc, Entity t) {
		npc.setNextAnimation(new Animation(TRICK_KNIFE));
		if (t != null) {
			try {
				CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
					private int stage, time, offset;
					private Entity target = t;
					private NewProjectile projectile = new NewProjectile(npc, target, TRICK_KNIFE_PROJ, 35, 40, 70, 10, 35, 0);

					@Override
					public boolean repeat() {
						try {
							if (time == 0)
								World.sendProjectile(projectile);
							if (time == projectile.getTime() + offset) {
								boolean even = stage % 2 == 0;
								if (even)
									target.applyHit(new Hit(npc, (int) (Utils.random(npc.getInstance().isHardMode() ? 460 : 230) * npc.getDamageBoost()), HitLook.RANGE_DAMAGE));
								if (stage != 4) {
									List<Entity> targets = npc.getPossibleTargets(true, true);
									if (targets.size() == 0) {
										return false;
									}
									target = targets.get(Utils.random(targets.size()));
									offset += projectile.getTime();
									if (even)
										projectile = new NewProjectile(target, npc, TRICK_KNIFE_PROJ, 35, 40, 25, 10, 35, -2);
									else
										projectile = new NewProjectile(new WorldTile(npc.getCoordFaceX(npc.getSize()), npc.getCoordFaceY(npc.getSize()), npc.getPlane()), target, 6135, 35, 40, 25, 10, 35, 0);
									World.sendProjectile(projectile);
								}
								if (stage == 4)
									return false;
								stage++;
							}
							time++;
							} catch (Exception e) {
								Logger.getGlobal().catching(e);
							}
						return true;
					}
					
				}, 0, 1, TimeUnit.MILLISECONDS);

			} catch (Exception e) {}
		}
		return 4;
	}

	/**
	 * Summons the spirit closest to him.
	 */
	private final int summonSpirit(Gregorovic gregorovic, Entity target) {
		final int type = getMaskType(gregorovic, true);
		gregorovic.setNextForceTalk(RISE_FTALK);
		final NewProjectile projectile = new NewProjectile(gregorovic.getFrom()[type], gregorovic.getTo()[type], RISE_PROJ, 50, 50, 0, 20, 30, 0);
		World.sendProjectile(projectile);
		try {
			CoresManager.getServiceProvider().executeWithDelay(new Runnable() {

				@Override
				public void run() {
					new Spirit(MANIA + type, gregorovic.getTo()[type], -1, true, true, gregorovic.getInstance());
				}
				
			}, projectile.getTime(), TimeUnit.MILLISECONDS);
		} catch (Exception e) {
		}
		return 4;
	}
	
	/**
	 * Summons challenge mode spirits
	 */
	private final int summonChallengeSpirits(Gregorovic gregorovic, Entity target) {
		final int type = getMaskType(gregorovic, false);
		gregorovic.setNextForceTalk(RISE_FTALK);
		for (int i = 0; i < 3; i++) {
			if (i == type)
				continue;
			final int spiritType = i;
			final NewProjectile projectile = new NewProjectile(gregorovic.getFrom()[type], gregorovic.getTo()[type], RISE_PROJ, 50, 50, 0, 20, 30, 0);
			World.sendProjectile(projectile);
			try {
				CoresManager.getServiceProvider().executeWithDelay(new Runnable() {

					@Override
					public void run() {
						new Spirit(MANIA + spiritType, gregorovic.getTo()[spiritType], -1, true, true, gregorovic.getInstance());
					}

				}, projectile.getTime(), TimeUnit.MILLISECONDS);

			} catch (Exception e) {}
		}
		return 4;
	}

	/**
	 * Gets the type of the closest mask.
	 */
	private final int getMaskType(Gregorovic gregorovic, boolean closest) {
		int type = 0;
		for (int i = 1; i < 3; i++)
			if (closest ? gregorovic.getDistance(gregorovic.getInstance().getMasks()[i]) < gregorovic.getDistance(gregorovic.getInstance().getMasks()[type]) : gregorovic.getDistance(gregorovic.getInstance().getMasks()[i]) > gregorovic.getDistance(gregorovic.getInstance().getMasks()[type]))
				type = i;
		return type;
	}

	/**
	 * Sends the glaives up in the sky which will fall down into random spots after about 8 seconds.
	 */
	private final int glaiveThrow(Gregorovic gregorovic, Entity target) {
		gregorovic.setNextAnimation(new Animation(GLAIVE_THROW));
		loop : for (int x = 0; x < 50; x++) {
			final WorldTile t = gregorovic.getInstance().getWorldTile(Utils.random(32, 55), Utils.random(32, 55));
			for (WorldTile tile : gregorovic.getTiles()) {
				if (tile.getTileHash() == t.getTileHash())
					continue loop;
				if (!World.canMoveNPC(tile.getPlane(), tile.getX(), tile.getY(), 1))
					continue;
			}
			gregorovic.getTiles().add(t);
		}
		WorldTasksManager.schedule(new WorldTask() {
			private int ticks = 0;
			private final List<WorldTile> t = new ArrayList<WorldTile>();
			private final List<WorldTile> t2 = new ArrayList<WorldTile>();

			@Override
			public void run() {
				switch (ticks) {
				case 0:
					gregorovic.getTiles().forEach(tile -> {
						if (Utils.random(100) > 50) {
							t.add(tile);
							for (Player p : gregorovic.getInstance().getPlayers())
								p.getPackets().sendGraphics(new Graphics(GLAIVE_THROW_GFX), tile);
						}
					});
					break;
				case 1:
					gregorovic.getTiles().forEach(tile -> {
						if (!t.contains(tile)) {
							t2.add(tile);
							for (Player p : gregorovic.getInstance().getPlayers())
								p.getPackets().sendGraphics(new Graphics(GLAIVE_THROW_GFX), tile);
						}
					});
					break;
				case 7:
					for (WorldTile tile : t)
						if (target.withinDistance(tile, 1))
							target.applyHit(new Hit(target, (int) (gregorovic.getInstance().isHardMode() ? Utils.random(200, 350) : Utils.random(100, 170) * gregorovic.getDamageBoost()), HitLook.MAGIC_DAMAGE));
					break;
				case 8:
					for (WorldTile tile : t2)
						if (target.withinDistance(tile, 1))
							target.applyHit(new Hit(target, (int) (gregorovic.getInstance().isHardMode() ? Utils.random(200, 350) : Utils.random(100, 170) * gregorovic.getDamageBoost()), HitLook.MAGIC_DAMAGE));
					break;
				case 9:
					gregorovic.getTiles().clear();
					stop();
					break;
				}
				ticks++;
			}
		}, 1, 0);
		return 6;
	}

	@Override
	public int attack(NPC npc, Entity target) {
		Gregorovic gregorovic = (Gregorovic) npc;
		if (Utils.random(10) < gregorovic.getManiaBuff())
			gregorovic.skipBasicAttacks();
		final int phase = gregorovic.getPhase();
		gregorovic.nextPhase();
		if (gregorovic.getPhase() < 0 || gregorovic.getPhase() > 12)
			gregorovic.setPhase(0);
		if (Utils.random(5) == 1) {
			int damage = (int) (gregorovic.getInstance().isHardMode() ? Utils.random(20, 80) : Utils.random(10, 40) * gregorovic.getDamageBoost()) + (target.getPoison().getPoisonDamage() > 150 ? 150 : target.getPoison().getPoisonDamage());
			if (damage > POISON_CAP)
				damage = POISON_CAP + (Utils.random(50));
			target.getPoison().makePoisoned(damage);
		}
		switch (phase) {
		case 3:
			return trickKnife(gregorovic, target);
		case 7:
			return gregorovic.getInstance().isHardMode() ? summonChallengeSpirits(gregorovic, target) : summonSpirit(gregorovic, target);
		case 11:
			return glaiveThrow(gregorovic, target);
		default:
			return shurikenAttack(gregorovic, target);
		}
	}

}