package com.rs.game.npc.combat.impl;

import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.*;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit.HitLook;
import com.rs.game.npc.NPC;
import com.rs.game.Hit;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.npc.solak.Solak;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Logger;
import com.rs.utils.Utils;
import java.util.ArrayList;

import java.util.concurrent.TimeUnit;

public class SolakCombat extends CombatScript {
	private Solak solak;
	@Override
	public int attack(NPC npc, Entity target) {
		final NPCCombatDefinition defs = npc.getCombatDefinitions();
		solak = (Solak) npc;
		final Solak solak = (Solak) npc;
		if (npc.getId() == 25513 || npc.getId() == 25529) {
			if (npc.getHitpoints() < npc.getMaxHitpoints() / 2
					&& Utils.random(6) == 0) {
				WorldTasksManager.schedule(new WorldTask() {
					int count = 0;
					@Override
					public void run() {
						for (Player player : World.getPlayers()) { // lets just loop
							if (player == null || player.isDead()
									|| player.hasFinished())
								continue;
							if (npc.getHitpoints() < npc.getMaxHitpoints() / 3) {
								return;
							}
							if (npc.getHitpoints() <= 25_000) {
								npc.heal(220, 0, 2, true);
								World.sendGraphics(npc, new Graphics(5004), solak);
								Projectile projectile = World.sendProjectileCycles(solak, npc, 5003, 208, 37, 0, 120, 5 + Utils.random(5), 0);
								long projectileCycles = Utils.projectileTimeToMiliseconds(projectile.getStartTime() + projectile.getEndTime() + 20);
								World.sendGraphics(npc, new Graphics(6914), npc);
							}
						}
						if (count++ == 5) {
							stop();
							return;
						}
					}
				}, 0, 0);
			}
		}
		if (npc.getId() == 25513) {
			if (npc.getHitpoints() < npc.getMaxHitpoints() / 1.1) {
				if (npc.withinDistance(target, npc.getSize())) {
					solak.melee2nd();
				}
			}
			if (npc.getHitpoints() < npc.getMaxHitpoints() / 1.3) {
				if (npc.withinDistance(target, npc.getSize())) {
					solak.ringoffire();
				}
			}
			if (npc.getHitpoints() < npc.getMaxHitpoints() / 1.5) {
				if (npc.withinDistance(target, npc.getSize())) {
					solak.shadowJump1();
				}
			}
			if (npc.getHitpoints() < npc.getMaxHitpoints() / 1.7) {
				if (npc.withinDistance(target, npc.getSize())) {
					solak.sendIcePrison1();
				}
			}
			if (npc.getHitpoints() < npc.getMaxHitpoints() / 2) {
				if (npc.withinDistance(target, npc.getSize())) {
					solak.range();
				}
			}
			if (npc.getHitpoints() < npc.getMaxHitpoints() / 2.5) {
				if (npc.withinDistance(target, npc.getSize())) {
					solak.melee1st();
				}
			}
			if (npc.getHitpoints() < npc.getMaxHitpoints() / 3.5) {
				if (npc.withinDistance(target, npc.getSize())) {
					solak.melee2nd1();
					for (final Entity t : npc.getPossibleTargets()) {
						boolean drainPrayer = (t instanceof Player) && (Math.random() <= 0.40);
						if (drainPrayer)
							WorldTasksManager.schedule(new WorldTask() {
								@Override
								public void run() {
									if (t.hasFinished() || t.isDead())
										return;
									((Player) t).getPrayer().drainPrayer(100, true);

								}
							}, 0);
						World.sendProjectile(npc, t, 6922, 10, 10, 10, 2, 16, 0);
					}
				}
			}
			if (npc.getHitpoints() < npc.getMaxHitpoints() / 4.5) {
				if (npc.withinDistance(target, npc.getSize())) {
					solak.melee2nd();
				}
			}
		}
		if (npc.getId() == 25529) {
			if (npc.getHitpoints() <= 25_000) {
				if (npc.withinDistance(target, npc.getSize())) {
					solak.ringoffire();
				}
			}
			if (npc.getHitpoints() <= 22_000) {
				if (npc.withinDistance(target, npc.getSize())) {
					solak.melee1st();
				}
			}
			if (npc.getHitpoints() <= 18_000) {
				if (npc.withinDistance(target, npc.getSize())) {
					solak.melee2nd();
				}
			}
			if (npc.getHitpoints() <= 15_000) {
				if (npc.withinDistance(target, npc.getSize())) {
					solak.poison1();
				}
			}
			if (npc.getHitpoints() <= 10_000) {
				if (npc.withinDistance(target, npc.getSize())) {
					solak.melee1st();
				}
			}
		}
			switch (solak.getPhase()) {
			case 1:
				melee1stAttack(npc, target);

				return 7;
			case 2:
				melee2ndAttack(npc, target); //				meleeAttack(npc, target);

				return 7;
			case 3:
				ringoffireAttack(npc, target);
				return 7;
			case 4:
				shadowJump(npc, target);
				return 7;
			case 5:
				sendIcePrison(solak, true);
				return 15;
				case 6:
					rangeAttack(npc, target);
					return 8;
				case 7:
					poisonAttack(npc, target);
					return 7;
				case 8:
					melee2nd1Attack(solak, npc, target);
					return 15;
		}
		return defs.getAttackDelay();
	}




	private void sendIcePrison(final Solak solak, boolean start) {// this attack is from normal NEX boss
		if (start) {
			solak.setNextWorldTile(new WorldTile(1376, 5669, 0));
			solak.setNextForceTalk(new ForceTalk("<col=ff0000> Die now, in a prison of ice!"));
			solak.playSoundEffect(3308);
			solak.setNextAnimation(new Animation(31861));
			solak.setNextGraphics(new Graphics(6903));
			World.sendGraphics(solak, new Graphics(6931), solak);
		}
		ArrayList<Player> players = new ArrayList<Player>();
		for (Player player : World.getPlayers()) {
			if (!player.withinDistance(solak, 14))
				continue;
			players.add(player);
		}
		if (players.size() > 0) {
			final Player player = players.get(Utils.random(players.size()));
			if (player == null || player.isDead()) {
				sendIcePrison(solak, false);
				return;
			}
			player.getTemporaryAttributtes().remove("insideIcePrison");
			World.sendProjectile(solak, player, 6902, 20, 20, 20, 2, 10, 0);
			final WorldTile base = player;
			player.resetWalkSteps();
			player.getPrayer().closeProtectionPrayers();
			player.getTemporaryAttributtes().put("insideIcePrison", Boolean.TRUE);
			for (int x = -1; x <= 1; x++) {
				for (int y = -1; y <= 1; y++) {
					final WorldTile tile = base.transform(x, y, 0);
					final WorldObject object = new WorldObject(57263, 10, 0, tile);
					if (!tile.matches(player) && World.canMoveNPC(tile, 1))
						World.spawnObject(object);
					WorldTasksManager.schedule(new WorldTask() {
						boolean remove = false;
						@Override
						public void run() {
							if (remove) {
								if (World.containsObjectWithId(object, object.getId()))
									World.removeObject(object);
								stop();
								return;
							}
							remove = true;
							if (player.getTemporaryAttributtes().get("insideIcePrison") != null && player.getX() == tile.getX() && player.getY() == tile.getY()) {
								player.getPackets().sendPlayerMessage(1, 15263739, "<col=ff0000>" +"The centre of the ice prison freezes you to the bone!", true);
								player.resetWalkSteps();
								player.getTemporaryAttributtes().remove("insideIcePrison");
								player.applyHit(new Hit(solak, Utils.random(150, 250), HitLook.UNBLOCKABLE_REGULAR_DAMAGE));
							}
						}
					}, 8, 0);
				}
			}
		} else {
			return;
		}
	}
	public void shadowJump(NPC npc, Entity target) {
		final WorldTile center = new WorldTile(target);
		npc.setNextAnimation(new Animation(31861));
		target.getTemporaryAttributtes().put("cantMove", Boolean.TRUE);
		WorldTasksManager.schedule(new WorldTask() {
			int count = 0;
			@Override
			public void run() {
				for (Player player : World.getPlayers()) { // lets just loop
					if (player == null || player.isDead()
							|| player.hasFinished())
						continue;
					if (player.withinDistance(center, 1)) {
						delayHit(npc, 1, player,
								new Hit(npc, Utils.random(25),
										HitLook.REFLECTED_DAMAGE));
						World.sendGraphics(target, new Graphics(3802), center);//6925
					}
				}
				if (count++ == 10) {
					stop();
					return;
				}
			}
		}, 0, 0);
		for (Entity t : npc.getPossibleTargets()) {
			WorldTasksManager.schedule(new WorldTask() {
				int loop;
				public void run() {
					if (loop == 1) {
						int[][] tileTransforms = {{1, 1}, {-1, -1}, {1, -1}, {-1, 1}};
						for (int i = 0; i < tileTransforms.length; i++) {
							WorldTile tile = target.transform(tileTransforms[i][0], tileTransforms[i][1], 0);
							if (World.canMoveNPC(tile, 1))
								World.spawnObjectTemporary(new WorldObject(57262, 10, 0, tile), 4000);//57262
							target.setNextGraphics(new Graphics(6955));
						}
					}else if (loop == 0) {
						World.sendProjectile(npc, target, 6902, 41, 41, 10, 1, 1, 0);
						npc.setNextWorldTile(new WorldTile(1376, 5669, 0));
						target.setNextGraphics(new Graphics(6927));
						t.setNextGraphics(new Graphics(6901));
						npc.setNextForceTalk(new ForceTalk("<col=ff0000>Here Comes My Power From AnicentX"));

						int damage = getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitionConstants.MAGE, target);
						damage += Utils.random(80, 150);
						delayHit(npc, 0, t, getMagicHit(npc, damage));
					} else if (loop == 2) { //change to a lower number if the wait time is too long
						//put your code here
						target.getTemporaryAttributtes().remove("cantMove");
						stop();
					}
					loop++;
				}
			}, 0, 0);
		}
		ArrayList<Player> players = new ArrayList<Player>();
		for (Player player : World.getPlayers()) {
			if (!player.withinDistance(solak, 14))
				continue;
			players.add(player);
		}
		if (players.size() > 0) {
			final Player player = players.get(Utils.random(players.size()));


			if (npc.getHitpoints() <= 31_000) {
				return;
			}
			World.sendProjectile(solak, player, 7002, 20, 20, 20, 5, 0, 0);

			player.resetWalkSteps();
			player.getPrayer().closeProtectionPrayers();

		}

	}

	public void melee1stAttack(NPC npc, Entity target) {
		if (Utils.random(2) == 0) {
			npc.setNextForceTalk(new ForceTalk("<col=ff0000>Power From Lost Grove Makes Me Stronger"));
		}

		for (Entity t : npc.getPossibleTargets()) {
		npc.setNextAnimation(new Animation(31763));
			final WorldTile center = new WorldTile(target);
			World.sendGraphics(npc, new Graphics(3232), center);
			World.sendGraphics(npc, new Graphics(6914), npc);
		World.sendGraphics(npc, new Graphics(6865), target);
			int damage = getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitionConstants.MELEE, target);
			damage += Utils.random(75, 185);
			delayHit(npc, 0, t, getMeleeHit(npc, damage));
	}
	}
	public void ringoffireAttack(NPC npc, Entity target) {
		final WorldTile center = new WorldTile(target);
		target.getTemporaryAttributtes().put("cantMove", Boolean.TRUE);
		npc.setNextForceTalk(new ForceTalk("<col=ff0000>Ring Of Power Will Kill You"));

		WorldTasksManager.schedule(new WorldTask() {
			int count = 0;
			@Override
			public void run() {
				for (Player player : World.getPlayers()) { // lets just loop
					if (player == null || player.isDead()
							|| player.hasFinished())
						continue;

					if (player.withinDistance(center, 1)) {

						delayHit(npc, 1, player,
								new Hit(npc, Utils.random(55),
										HitLook.CRITICAL_DAMAGE));
						World.sendGraphics(target, new Graphics(6929), center);
					}
				}
				if (count++ == 10) {
					stop();
					return;
				}
			}
		}, 0, 0);
		for (Entity t : npc.getPossibleTargets()) {
			WorldTasksManager.schedule(new WorldTask() {
				int loop;
				public void run() {
					if (loop == 1) {

						int[][] tileTransforms = {{1, 1}, {-1, -1}, {1, -1}, {-1, 1}};
						for (int i = 0; i < tileTransforms.length; i++) {
							WorldTile tile = target.transform(tileTransforms[i][0], tileTransforms[i][1], 0);
							if (World.canMoveNPC(tile, 1))
								World.spawnObjectTemporary(new WorldObject(57262, 10, 0, tile), 4000);//57262
							target.setNextGraphics(new Graphics(6897));
						}
					}else if (loop == 0) {

						npc.setNextWorldTile(new WorldTile(1376, 5669, 0));
						target.setNextGraphics(new Graphics(5516));
						t.setNextGraphics(new Graphics(6888));
						npc.setNextAnimation(new Animation(31766));

						int damage = getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitionConstants.MAGE, target);
						damage += Utils.random(75, 155);
						delayHit(npc, 0, t, getMagicHit(npc, damage));
					} else if (loop == 2) { //change to a lower number if the wait time is too long
						//put your code here
						target.getTemporaryAttributtes().remove("cantMove");
						stop();
					}
					loop++;
				}
			}, 0, 0);
		}
    }
	public void rangeAttack(NPC npc, Entity target) {


			npc.setNextForceTalk(new ForceTalk("<col=ff0000>My Range Power Is Getting Stronger From AnicentX"));
		for (Entity t : npc.getPossibleTargets()) {
			npc.setNextAnimation(new Animation(31815));
			World.sendProjectile(npc, t, 6903, 31, 8, 20, 5, 20, 0);//39, 36, 41, 50, 0, 100);
			int damage = getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitionConstants.RANGE, target);
			damage += Utils.random(100, 180);
			delayHit(npc, 0, t, getRangeHit(npc, damage));

	}
	}
	public void poisonAttack(NPC npc, Entity target) {


			npc.setNextForceTalk(new ForceTalk("<col=ff0000>Poison Power From Lost Grove"));
		for (Entity t : npc.getPossibleTargets()) {

			npc.setNextAnimation(new Animation(31861));

			delayHit(npc, 0, t,
					getMagicHit(npc, getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitionConstants.MAGE, target)
							+ Utils.random(120, 220)));
			target.setNextGraphics(new Graphics(6898, 50, 0));
			target.getPoison().makePoisoned(100);

		}
		}

	public void melee2ndAttack(NPC npc, Entity target) {
		npc.setNextForceTalk(new ForceTalk("<col=ff0000>Power From Lost Grove Makes Me Stronger2"));
		for (Entity t : npc.getPossibleTargets()) {
		npc.setNextAnimation(new Animation(31764));
		int damage = getRandomMaxHit(npc, npc.getMaxHit(), NPCCombatDefinitionConstants.MELEE, target);
		World.sendGraphics(npc, new Graphics(5516), t);//6530
			World.sendGraphics(npc, new Graphics(6530), npc);

			damage += Utils.random(80, 155);
		delayHit(npc, 0, t, getMeleeHit(npc, damage));
	}
	}
	public static void melee2nd1Attack(Solak boss, NPC npc, Entity target) {


		boss.getCombat().removeTarget();
		npc.setNextFaceEntity(null);
		npc.setNextFaceWorldTile(boss.transform(4, boss.getSize() + 1, 0));
		npc.getTemporaryAttributtes().remove("insideTorrent");
		boss.setNextForceTalk(new ForceTalk("<col=ff0000>MELEE 2ND 1 ATTACK TESTMODE"));

		npc.setNextAnimation(new Animation(31797));
		npc.setNextGraphics(new Graphics(5564));
		npc.getTemporaryAttributtes().put("insideTorrent", Boolean.TRUE);
		WorldTile middleTile = npc.getMiddleWorldTile();
		byte[][] dirs = new byte[][] { { 0, 1 }, { -1, 1 }, { -1, 0 }, { -1, -1 }, { 0, -1 }, { 1, -1 }, { 1, 0 }, { 1, 1 }, { 0, 1 } };
		for (Entity e : npc.getPossibleTargets()) {
			if (e == null || e.hasFinished() || e.isDead())
				continue;
			e.getTemporaryAttributtes().remove("skiphitcurrent");
		}
		CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
			int loop;
			int currentDir;

			@Override
			public boolean repeat() {
				try {
					if (boss.isDead() || boss.hasFinished()) {
						boss.setNextFaceWorldTile(boss.transform(4, boss.getSize() + 1, 0));
						boss.getTemporaryAttributtes().remove("insideTorrent");
						boss.setNextFaceEntity(null);
						boss.setNextAnimation(new Animation(-1));
						boss.setNextGraphics(new Graphics(-1));
						return false;
					}
					if (loop == 0 || loop % 300 == 0) {
						for (int x = middleTile.getX() - 20; x <= middleTile.getX() + 20; x++) {
							for (int y = middleTile.getY() - 20; y <= middleTile.getY() + 20; y++) {
								WorldTile tile = new WorldTile(x, y, boss.getPlane());
								if (Utils.isOnRange(boss, tile, 7, 8, 1) && Utils.getAngle(dirs[currentDir][0], dirs[currentDir][1]) == Utils.getAngle(tile.getX() - middleTile.getX(), tile.getY() - middleTile.getY())) {
									for (Entity e : boss.getPossibleTargets()) {
										if (e == null || e.hasFinished() || e.isDead())
											continue;
										if (Utils.isOnRange(tile, e, 1, 2, 0) && e.getTemporaryAttributtes().get("skiphitcurrent") == null) {
											e.getTemporaryAttributtes().put("skiphitcurrent", Boolean.TRUE);
											e.applyHit(new Hit(boss, Utils.random(75, 110), HitLook.ABSORB_DAMAGE));
										}
									}
								}
							}
						}
					}
					loop++;
					if (loop % 600 == 0) {
						for (Entity e : boss.getPossibleTargets()) {
							if (e == null || e.hasFinished() || e.isDead())
								continue;
							e.getTemporaryAttributtes().remove("skiphitcurrent");
						}
						currentDir++;
					}
					if (loop >= 5200) {
						boss.getTemporaryAttributtes().remove("insideTorrent");
						if ((target != null && !target.isDead() && !target.hasFinished())) {
							boss.getCombat().setTarget(target);
							boss.setNextFaceEntity(target);
						} else if (!boss.getPossibleTargets().isEmpty()) {
							boss.getCombat().setTarget(boss.getPossibleTargets().get(Utils.random(boss.getPossibleTargets().size())));
							boss.setNextFaceEntity(boss.getCombat().getTarget());
						}
						return false;
					}
				} catch (Exception e) {
					Logger.getGlobal().catching(e);
					return false;
				}
				return true;
			}
		}, 800, 1, TimeUnit.MILLISECONDS);





			CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
				int loop = 0;
				Solak.PurpleFlameHitBar bar;
				long cycle;

				@Override
				public boolean repeat() {
					try {
						if (boss.isDead() || boss.hasFinished())
							return false;
						if (loop == 0) {
							target.setNextGraphics(new Graphics(5582));
							long totalTime = 6000;
							cycle = totalTime + Utils.currentTimeMillis();
							bar = new Solak.PurpleFlameHitBar(totalTime, cycle);
						}
						if (loop == 0 || loop % 50 == 0)
							target.getNextHitBars().add(bar);
						if (Utils.currentTimeMillis() > cycle) {
							if (boss.isDead() || boss.hasFinished())
								return false;
							boss.addFlame(new WorldTile(target));
							return false;
						}
						loop++;
					} catch (Exception e) {
						Logger.getGlobal().catching(e);
						return false;
					}
					return true;
				}
			}, 1200, 1, TimeUnit.MILLISECONDS);

	}
	@Override
	public Object[] getKeys() {
		return new Object[] { 25513, 25529 };
	}
}