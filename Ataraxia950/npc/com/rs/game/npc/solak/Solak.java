
package com.rs.game.npc.solak;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.*;
import com.rs.game.hitbar.HitBar;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.SolakController;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;
public class Solak extends NPC {
	private int phase;
	private long flameHitCycle;
	private boolean sentDeath;

	private transient Solak current;
	private transient List<Solak.Flame> flames;
	private Solak solak;
	public Solak(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		flames = new ArrayList<Solak.Flame>();
		this.setLureDelay(0);
		this.setCapDamage(2200);
		phase = 1;
		this.setRun(true);
		this.setForceMultiAttacked(true);
		this.setForceAgressive(false);
		this.setForceTargetDistance(14);
		this.setHitpoints(50000);
		this.getCombatDefinitions().setHitpoints(50000);
	}
	@Override
	public int getMaxHitpoints() {
		return getCombatDefinitions().getHitpoints();
	}
	@Override
	public void processNPC() {
		super.processNPC();
		if (flameHitCycle == 0 || Utils.currentTimeMillis() >= flameHitCycle) {
			for (Solak.Flame flame : flames) {
				if (flame != null && flame.getDamage() > 0) {
					for (Entity e : getPossibleTargets()) {
						if (e == null || e.hasFinished() || e.isDead() || !Utils.isOnRange(flame.tile, e, 1, 1, 1))
							continue;
						e.applyHit(new Hit(this, flame.getDamage(), Hit.HitLook.REGULAR_DAMAGE));
					}
				}
			}
			flameHitCycle = Utils.currentTimeMillis() + 600;
		}
		if ((getHPPercentage() < 75 && spawnCount < 1)
				|| (getHPPercentage() < 25 && spawnCount < 2))
			if (isDead())
				return;
		checkReset();
	}
	public int getBossMapId() {

		return 25513;
	}
	public int getBoss1MapId() {

		return 25529;
	}
	public int getNpcId() {
		return 25508;
	}


	@Override
	public void processHit(Hit hit) {
		super.processHit(hit);

		for (Player p : World.getPlayers()) { // lets just loop
			if (p == null || p.isDead() || p.hasFinished() || !(p.getControlerManager().getControler() instanceof SolakController))
				continue;
			SolakController c = (SolakController) p.getControlerManager().getControler();

			c.updateInterface();
		}
	}

	@Override
	public boolean restoreHitPoints() {
		boolean restore = super.restoreHitPoints();

		for (Player p : World.getPlayers()) { // lets just loop
			if (p == null || p.isDead() || p.hasFinished() || !(p.getControlerManager().getControler() instanceof SolakController))
				continue;
			SolakController c = (SolakController) p.getControlerManager().getControler();


			c.updateInterface();
			return restore;

		}
		return restore;
	}



	public void addFlame(WorldTile location) {
		Solak.Flame f = new Solak.Flame(location);
		flames.add(f);
		World.sendGraphics(this, new Graphics(6984), location);
		final Solak thisNPC = this;
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				if (isDead() || hasFinished() || !flames.contains(f)) {
					stop();
					return;
				}
				World.sendGraphics(thisNPC, new Graphics(6984), location);
			}
		}, 40, 40);
	}
	public static class PurpleFlameHitBar extends HitBar {
		private long totalTime = 0;
		private final long timeToExplode;
		public PurpleFlameHitBar(long totalTime, long timeToExplode) {
			this.totalTime = totalTime;
			this.timeToExplode = timeToExplode;
		}
		@Override
		public int getType() {
			return 9;
		}
		@Override
		public int getPercentage() {
			if (Utils.currentTimeMillis() > timeToExplode)
				return 0;
			long timeRemaining = timeToExplode - Utils.currentTimeMillis();
			int percentage = (int) (((((double) timeRemaining / (double) totalTime) * 100) * 255) / 100);
			return percentage;
		}
		@Override
		public boolean display(Player player) {
			return getPercentage() != 0;
		}
	}
	public static class Flame {
		private final WorldTile tile;
		private final long lifeCycle;
		private final int startDamage;
		public Flame(WorldTile tile) {
			this.tile = tile;
			this.lifeCycle = Utils.currentTimeMillis();
			startDamage = Utils.random(50, 90);
		}
		public int getDamage() {
			long time = Utils.currentTimeMillis() - lifeCycle;
			int ticksPassed = (int) (time / 600);
			if (ticksPassed <= 0)
				return 0;
			int damage = startDamage + ((ticksPassed - 1) * 30);
			return damage >= 20 ? 30 : damage;
		}
		public void remove(Solak boss) {

			World.sendGraphics(boss, new Graphics(-1), tile);
		}
	}
	public int getHPPercentage() {
		return getHitpoints() * 100 / getMaxHitpoints();
	}
	@Override
	public double getMeleePrayerMultiplier() {
		return 0.50;
	}
	@Override
	public double getMagePrayerMultiplier() {
		return 0.275;
	}
	private int spawnCount;
	@Override
	public double getRangePrayerMultiplier() {
		return 0.275;
	}
	public void checkReset() {
		int maxhp = getMaxHitpoints();
		if (maxhp > getHitpoints() && !isUnderCombat() && getPossibleTargets().isEmpty())
			setHitpoints(maxhp);
	}

	public List<Flame> clearArea() {

		for (Solak.Flame flame : flames) {
			if (flame != null)
				flame.remove(this);
			if (solak == null || solak.hasFinished() || solak.isDead())
				continue;
			flames.clear();
		}
		flames.clear();
		return flames;
	}
	public void setDefinitions() {
		setHitpoints(getMaxHitpoints());
		setBonuses();
	}
	public void melee1st() {
		phase = 1;
	}
	public void melee2nd() {
		phase = 2;
	}
	public void ringoffire() {
		sendExtraHeal();
		phase = 3;
	}
	public void shadowJump1() {
		phase = 4;
	}
	public void sendIcePrison1() {
		phase = 5;
	}
	public void range() {
		phase = 6;
	}
	public void poison1() {
		phase = 7;
	}
	public void melee2nd1() {
		phase = 8;
	}
	public int getPhase() {
		return phase;
	}
	public void nextPhase() {
		phase++;
	}
	public void setPhase(int phase) {
		this.phase = phase;
	}
	public void sendExtraHeal() {
		if (getHitpoints() < getMaxHitpoints() / 1.5){
			return;
		}
		if (getHitpoints() < getMaxHitpoints() / 1.3 && Utils.random(5) == 0) {
			setNextGraphics(new Graphics(5004));
			heal(184, 0, 1, true);
			heal(172, 0, 1, true);
			return;
		}
		WorldTile from = this.getMiddleWorldTile().transform((Utils.random(2) == 0 ? -1 : 1) * 7, (Utils.random(2) == 0 ? -1 : 1) * 7, 0);
		Projectile projectile = World.sendProjectileCycles(from, getMiddleWorldTile(), 5003, 208, 37, 0, 120, 5 + Utils.random(5), 0);
		long projectileCycles = Utils.projectileTimeToMiliseconds(projectile.getStartTime() + projectile.getEndTime() + 20);
		CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
			@Override
			public boolean repeat() {
				try {
					if (getHitpoints() < getMaxHitpoints() / 1.3 && Utils.random(5) == 0) {
						heal(184, 0, 1, true);
						heal(167, 0, 1, true);
					}
					return false;
				} catch (Exception e) {
					return false;
				}
			}
		}, projectileCycles, 600, TimeUnit.MILLISECONDS);
	}
	public Solak getSolak() {
		return solak;
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


					setNextAnimation(new Animation(defs.getDeathEmote()));
				} else if (loop >= defs.getDeathDelay()) {

					if (getId() == 25513) {
						resetCombat();
						World.sendProjectileCycles(from, getMiddleWorldTile(), 6916, 208, 37, 0, 120, 5 + Utils.random(5), 0);
						Utils.projectileTimeToMiliseconds(projectile.getStartTime() + projectile.getEndTime() + 20);

						setCantInteract(true);
						setNextNPCTransformation(25529);
						transformIntoNPC(25529);
						setForceAgressive(true);

						WorldTasksManager.schedule(new WorldTask() {

							@Override
							public void run() {
								setNextWorldTile(new WorldTile(1376, 5669, 0));
								setNextGraphics(new Graphics(6920));
								setNextGraphics(new Graphics(6919));
								setForceAgressive(true);

								reset();
								setCantInteract(false);
								requestIconRefresh();


								setNextAnimation(new Animation(31773));

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
						setNextAnimation(new Animation(31773));
						transformIntoNPC(25513);
						setNextNPCTransformation(25513);
						setForceAgressive(false);
						World.sendProjectileCycles(from, getMiddleWorldTile(), 6916, 208, 37, 0, 120, 5 + Utils.random(5), 0);
						Utils.projectileTimeToMiliseconds(projectile.getStartTime() + projectile.getEndTime() + 20);
						setNextGraphics(new Graphics(6920));
						setNextGraphics(new Graphics(6919));
						setHitpoints(50000);
						getCombatDefinitions().setHitpoints(50000);
					}
					stop();
				}
				loop++;
			}
		}, 0, 5);
	}

	@Override
	public void finish() {
		clearArea();
		super.finish();
	}
	@Override
	public void spawn() {
		super.spawn();
		start();
		flames = new ArrayList<Solak.Flame>();
		phase = 1;
	}
	private void start() {
		setForceTargetDistance(14);
		spawnCount = 1;
	}
	public int getAttackDistance() {
		return 14;
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
}