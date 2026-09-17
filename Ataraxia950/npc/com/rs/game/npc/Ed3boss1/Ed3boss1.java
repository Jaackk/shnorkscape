
package com.rs.game.npc.Ed3boss1;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.*;
import com.rs.game.npc.NPC;

import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;

import com.rs.game.player.Player;
import com.rs.game.player.combat.PlayerCombat;
import com.rs.game.player.content.Combat;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import static com.rs.game.npc.combat.CombatScript.*;

public class Ed3boss1 extends NPC {
	private int attackProgress;
	public Ed3boss1(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		setLureDelay(0);
		setCapDamage(1750);
		setCantFollowUnderCombat(true);
		setRandomWalk(0);
		setForceFollowClose(false);
		setForceMultiArea(true);
		setForceTargetDistance(10);
		attackProgress = 0;

		setForceMultiAttacked(true);
		setForceAgressive(false);
		setIntelligentRouteFinder(true);
		setNoDistanceCheck(true);
		setHitpoints(12000);
		getCombatDefinitions().setHitpoints(12000);
	}


	public int getAutoAttacks() {
		return autoAttacks;
	}

	public void setAutoAttacks(int autoAttacks) {
		this.autoAttacks = autoAttacks;
	}
	private int phase;
	private int autoAttacks;
	private int attackRotation;
	public int getAttackRotation() {
		return attackRotation;
	}

	public void setAttackRotation(int attackRotation) {
		this.attackRotation = attackRotation;
	}

	public void increaseAttackRotation() {
		this.attackRotation = attackRotation + 1 >= SpecialAttacks[phase].length ? 0 : attackRotation + 1;
	}
	public int getPhase() {
		return phase;
	}

	public void setPhase(int phase) {
		this.phase = phase;
	}
	@Override
	public void setNextFaceEntity(Entity entity) {
	}

	@Override
	public void setNextFaceWorldTile(WorldTile nextFaceWorldTile) {
		super.setNextFaceWorldTile(getRespawnTile().transform(2, getSize() + 5, 0));
	}


	public static final Ed3boss1.Ed3boss1Attacks[][] SpecialAttacks = { { Ed3boss1.Ed3boss1Attacks.UPPERCUT, Ed3boss1Attacks.AUTO_ATTACK, null, null }, { Ed3boss1.Ed3boss1Attacks.UPPERCUT, Ed3boss1Attacks.AUTO_ATTACK, }, { Ed3boss1.Ed3boss1Attacks.UPPERCUT, Ed3boss1Attacks.AUTO_ATTACK, null, null }, { Ed3boss1.Ed3boss1Attacks.UPPERCUT, Ed3boss1Attacks.AUTO_ATTACK, null, null }, };
	public enum Ed3boss1Attacks {

		UPPERCUT() {
			@Override
			public int sendAttack(Ed3boss1 ed3boss1, Player target) {
				ed3boss1.resetWalkSteps();
				ed3boss1.setNextForceTalk(new ForceTalk("give me strength!"));
				ed3boss1.setNextAnimation(new Animation(1254));
				ed3boss1.setNextGraphics(new Graphics(7185));
				ed3boss1.setCantDoDefenceEmote(true);
				ed3boss1.setNextFaceEntity(null);
				ed3boss1.setNextFaceWorldTile(target);

				int phase = ed3boss1.getPhase();
				final WorldTile center = new WorldTile(target);

				WorldTasksManager.schedule(new WorldTask() {
					int count = 0;
					@Override
					public void run() {
						for (Player player : World.getPlayers()) { // lets just loop
							if (player == null || player.isDead()
									|| player.hasFinished())
								continue;

							if (player.withinDistance(center, 1)) {

								delayHit(ed3boss1, 0, player,
										new Hit(ed3boss1, Utils.random(55),
												Hit.HitLook.CRITICAL_DAMAGE));
							}
						}
						if (count++ == 5) {
							stop();
							return;
						}
					}
				}, 0, 0);
				WorldTasksManager.schedule(new WorldTask() {
					int loop = 0;


					@Override
					public void run() {
						for (Entity t : ed3boss1.getPossibleTargets()) {

							if (ed3boss1.getPhase() != phase) {
							ed3boss1.resetWalkSteps();
							ed3boss1.getCombat().addCombatDelay(ed3boss1.getAttackSpeed());
							ed3boss1.setCantDoDefenceEmote(true);
							stop();
							return;
						}
						if (target.isDead() || ed3boss1.isDead()) {
							ed3boss1.resetWalkSteps();
							ed3boss1.getCombat().addCombatDelay(ed3boss1.getAttackSpeed());
							ed3boss1.setCantDoDefenceEmote(true);
							stop();
							return;
						}
						if (loop == 1)
							ed3boss1.setNextWorldTile(new WorldTile(5471, 9117, 2));
						if (loop == 2) {
							if (!Utils.isOnRange(ed3boss1, target, 0)) {
								ed3boss1.resetWalkSteps();
								ed3boss1.setCantDoDefenceEmote(true);
								ed3boss1.setNextFaceEntity(null);
								ed3boss1.setNextFaceWorldTile(target);
								ed3boss1.setNextWorldTile(new WorldTile(5471, 9117, 2));
								ed3boss1.setDirection(Utils.getAngle(0, -1));

							}
						} else if (loop >= 3) {

							ed3boss1.resetWalkSteps();
							ed3boss1.getCombat().addCombatDelay(ed3boss1.getAttackSpeed());
							ed3boss1.setCantDoDefenceEmote(false);
							ed3boss1.setNextFaceEntity(target);
							stop();
						}
						}
						loop++;
					}
				}, 0, 0);
				return 12;
			}

		},
		AUTO_ATTACK() {
			public int sendAttack(Ed3boss1 ed3boss1, Player target) {
				ed3boss1.setNextForceTalk(new ForceTalk(""));

				boolean meleeAttack = Utils.getRandom(Utils.isOnRange(ed3boss1, target, 1) ? 2 : 10) == 0;
				ed3boss1.setNextAnimation(new Animation(meleeAttack ? 1316 : 626));
				ed3boss1.setCantDoDefenceEmote(true);
				if (meleeAttack) {
					for (Entity e : ed3boss1.getPossibleTargets()) {
						if (e == null || e.hasFinished() || e.isDead() || !Utils.isOnRange(ed3boss1, e, 1))
							continue;
						int damage = getRandomMaxHit(ed3boss1, ed3boss1.getMaxHit(), NPCCombatDefinitionConstants.MELEE, target);
						damage += Utils.random(75, 185);
						delayHit(ed3boss1, 0, e, getMeleeHit(ed3boss1, damage));
					}
				} else {
					WorldTile tile = new WorldTile(target);
					Projectile projectile = World.sendProjectileCycles(ed3boss1, tile, 6962, 54, 30, 20, 120, Utils.random(5), 350);
					long projectileCycles = Utils.projectileTimeToMiliseconds(projectile);
					CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
						@Override
						public boolean repeat() {
							try {
								if (ed3boss1.isDead() || ed3boss1.hasFinished())
									return false;
								for (Entity e : ed3boss1.getPossibleTargets()) {
									if (e == null || e.hasFinished() || e.isDead() || !Utils.isOnRange(tile, e, 0, 1, 1))
										continue;
									int damage = getRandomMaxHit(ed3boss1, ed3boss1.getMaxHit(), NPCCombatDefinitionConstants.MELEE, target);
									damage += Utils.random(75, 185);
									delayHit(ed3boss1, 0, e, getMeleeHit(ed3boss1, damage));
								}
								return false;
							} catch (Exception e) {
								Logger.getGlobal().catching(e);
								return false;
							}
						}
					}, projectileCycles, 600, TimeUnit.MILLISECONDS);
				}
				return 12;
			}
		};
		public int sendAttack(Ed3boss1 ed3boss1, Player target) {
			return 0;
		}
	}



	@Override
	public void processNPC() {
		super.processNPC();
		if (isDead())
			return;



	}
	@Override
	public void handleIngoingHit(Hit hit) {
		super.handleIngoingHit(hit);
	}
	@Override
    public double getMeleePrayerMultiplier() {
		return 0.50;
    }


	@Override
	public void sendDeath(Entity source) {
		final NPCCombatDefinition defs = getCombatDefinitions();
		setNextForceTalk(new ForceTalk("you think you can kill me that easy now my full power is unleashed"));
		resetWalkSteps();
		getCombat().removeTarget();
		setNextAnimation(null);
		setNextGraphics(new Graphics(5004));
		WorldTile from = this.getMiddleWorldTile().transform((Utils.random(2) == 0 ? -1 : 1) * 7, (Utils.random(2) == 0 ? -1 : 1) * 7, 0);
		Projectile projectile = World.sendProjectileCycles(from, getMiddleWorldTile(), 5003, 208, 37, 0, 120, 5 + Utils.random(5), 0);
		long projectileCycles = Utils.projectileTimeToMiliseconds(projectile.getStartTime() + projectile.getEndTime() + 50);
		World.sendProjectileCycles(from, getMiddleWorldTile(), 6916, 208, 37, 0, 120, 5 + Utils.random(5), 0);
		Utils.projectileTimeToMiliseconds(projectile.getStartTime() + projectile.getEndTime() + 20);
		WorldTasksManager.schedule(new WorldTask() {

			int loop;
			@Override
			public void run() {
				if (loop == 0) {


					setNextAnimation(new Animation(6752));
				} else if (loop >= defs.getDeathDelay()) {

					if (getId() == 26050) {
						resetCombat();
						World.sendProjectileCycles(from, getMiddleWorldTile(), 6916, 208, 37, 0, 120, 5 + Utils.random(5), 0);
						Utils.projectileTimeToMiliseconds(projectile.getStartTime() + projectile.getEndTime() + 20);

						setCantInteract(true);
						setNextNPCTransformation(26050);
						transformIntoNPC(26050);
						setForceAgressive(true);

						WorldTasksManager.schedule(new WorldTask() {

							@Override
							public void run() {
								setNextGraphics(new Graphics(6920));
								setNextGraphics(new Graphics(6919));
								setForceAgressive(true);

								reset();
								setCantInteract(false);
								requestIconRefresh();


								setNextAnimation(new Animation(624));

							}
						}, 6);
					} else {
						drop();
						reset();
						setLocation(getRespawnTile());
						finish();
						if (!isSpawned())
							setRespawnTask();
						setDirection(Utils.getAngle(0, -1));
						setNextAnimation(new Animation(6750));
						transformIntoNPC(26050);
						setNextNPCTransformation(26050);
						setForceAgressive(false);
						World.sendProjectileCycles(from, getMiddleWorldTile(), 6916, 208, 37, 0, 120, 5 + Utils.random(5), 0);
						Utils.projectileTimeToMiliseconds(projectile.getStartTime() + projectile.getEndTime() + 20);
						setNextGraphics(new Graphics(6920));
						setNextGraphics(new Graphics(6919));
						setHitpoints(5000);
						getCombatDefinitions().setHitpoints(5000);
					}
					stop();
				}
				loop++;
			}
		}, 0, 5);
	}

	private long resetCombatDelayCycle;

	public void addResetAttackDelayCycle() {
		resetCombatDelayCycle = Utils.currentTimeMillis() + 10000;
	}
	@Override
	public double getMagePrayerMultiplier() {
		return 0.275;
	}
	
	@Override
    public double getRangePrayerMultiplier() {
		return 0.275;
    }

	@Override
	public boolean isFreezeImmune() {
		return true;
	}
	@Override
	public boolean isStunImmune() {
		return true;
	}
	@Override
	public ArrayList<Entity> getPossibleTargets() {
		ArrayList<Entity> possibleTarget = new ArrayList<Entity>();
		for (int regionId : getMapRegionsIds()) {
			List<Integer> playerIndexes = World.getRegion(regionId).getPlayerIndexes();
			if (playerIndexes != null) {
				for (int npcIndex : playerIndexes) {
					Player player = World.getPlayers().get(npcIndex);
					if (player == null || player.isDead() || player.hasFinished() || !player.isRunning()
							|| !player.withinDistance(this, 64)
							|| ((!isAtMultiArea() || !player.isAtMultiArea()) && player.getAttackedBy() != this
							&& player.getAttackedByDelay() > System.currentTimeMillis())
							|| !clipedProjectile(player, false))
						continue;
					possibleTarget.add(player);
				}
			}
		}
		return possibleTarget;
	}
	//test

	
	//test

}
