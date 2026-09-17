package com.rs.game.npc.camelwarrior;

import com.rs.cores.CoresManager;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.others.SecondaryBar;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/*
 * author Movee
 */

@SuppressWarnings("serial")
public class CamelWarrior extends Mirage {

	private List<Mirage> mirages;
	private boolean miragesCreated;
	private boolean camelReturned;
	private boolean stormAttackActive;
	public Entity savedTarget = null;
	private WorldTile targetTile = null;

	public CamelWarrior(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
		super(null, id, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
		setCamel(this);
	}

	public void createMirages() {
		miragesCreated = true;
		if (!isDead()) {
			for (int index = 0; index < 3; index++) {
				tileLoop: for (int tileAttempt = 0; tileAttempt < 10; tileAttempt++) {
					WorldTile tile = new WorldTile(this, 2);
					if (World.isTileFree(0, tile.getX(), tile.getY(), 1)) {
						World.sendGraphics(this, new Graphics(5923), tile);
						mirages.add(new Mirage(this, 22002 + index, tile, -1, true));
						break tileLoop;
					}
				}
			}
		}
	}

	public void removeMirage(Mirage mirage) {
		if (mirages != null) {
			if (mirages.size() > 0)
				mirages.remove(mirage);
		}
	}

	@Override
	public void handleIngoingHit(final Hit hit) {
		super.handleIngoingHit(hit);
	}

	@Override
	public void processNPC() {
		NPC npc = this;

		if (stormAttackActive && targetTile != null) {
			Entity target = getCombat().getTarget();

			World.sendGraphics(npc, new Graphics(5931), targetTile);

			if (target != null) {
				if (target.withinDistance(targetTile, 1)) {
					target.applyHit(new Hit(target, 200, HitLook.REGULAR_DAMAGE));
				}
			}

		}

		if (camelReturned & Utils.random(12) == 0 & !stormAttackActive && isUnderCombat()) {
			stormAttackActive = true;

			setNextSecondaryBar(new SecondaryBar(0, 1500, 1, false));
			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					Entity target = getCombat().getTarget();
					if (target != null)
						targetTile = new WorldTile(target);
					setNextAnimation(new Animation(27792));
					CoresManager.getServiceProvider().executeWithDelay(new Runnable() {

						@Override
						public void run() {
							targetTile = null;
							stormAttackActive = false;
						}
						
					}, 10, TimeUnit.SECONDS);
				}
				
			}, 49);
		}

		if (miragesCreated) {
			if (mirages != null) {
				if (mirages.size() == 0 && !camelReturned && !isDead()) {
					camelReturned = true;
					CoresManager.getServiceProvider().executeWithDelay(new Runnable() {

						@Override
						public void run() {
							npc.setNextWorldTile(new WorldTile(npc.getX(), npc.getY(), npc.getPlane() - 1));
							npc.setCantInteract(false);

							for (Entity target : getPossibleTargets(false, true)) {
								if (target.getIndex() == getTargetIndex()) {
									getCombat().setTarget(target);
									break;
								}
							}
						}
						
					}, 3500, TimeUnit.MILLISECONDS);

				}

			}
		}

		if (isUnderCombat()) {
			if (mirages == null && !miragesCreated && !camelReturned) {
				if (Utils.random(10) == 0) {
					mirages = new ArrayList<Mirage>(3);
					setNextGraphics(new Graphics(5921));
					CoresManager.getServiceProvider().executeWithDelay(new Runnable() {

						@Override
						public void run() {
							savedTarget = getCombat().getTarget();

							setNextWorldTile(new WorldTile(npc.getX(), npc.getY(), npc.getPlane() + 1));
							setCantInteract(true);
							createMirages();
						}
						
					}, 2, TimeUnit.SECONDS);

				}
			}
		}

		super.processNPC();
	}

	public WorldTile getBloodMirageTile() {
		WorldTile tile = null;

		if (mirages != null) {
			for (Mirage mirage : mirages) {
				if (mirage.getId() == 22003)
					tile = new WorldTile(mirage);
			}
		}

		return tile;
	}

	public NPC getBloodMirage() {
		NPC bloodMirage = null;

		if (mirages != null) {
			for (Mirage mirage : mirages) {
				if (mirage.getId() == 22003)
					bloodMirage = mirage;
			}
		}

		return bloodMirage;
	}

	public void healMirages() {
		WorldTile bloodTile = getBloodMirageTile();
		NPC bloodMirage = getBloodMirage();

		if (bloodTile != null && bloodMirage != null) {
			for (Mirage mirage : mirages) {

				if (mirage.getId() != 22003 && mirage.getId() != 22001) {
					World.sendProjectile(bloodMirage, bloodTile, mirage, 5003, 41, 30, 30, 25, 16, 0);
					CoresManager.getServiceProvider().executeWithDelay(new Runnable() {

						@Override
						public void run() {
							try {
								mirage.applyHit(new Hit(mirage, Utils.random(95), HitLook.HEALED_DAMAGE));
							} catch (Exception e) {
								Logger.getGlobal().catching(e);
							}
						}
						
					}, 2, TimeUnit.SECONDS);
				}
			}
		}
	}

	public boolean isHidden() {
		return getPlane() > 0;
	}

	public void resetCamel() {
		miragesCreated = false;
		camelReturned = false;
		stormAttackActive = false;
		setCantInteract(false);
		setCannotMove(false);
		getCombat().removeTarget();
		// reset();
		// finish();
	}

	public void resetMirages() {
		mirages = null;
	}

	@Override
	public void sendDeath(Entity source) {
		mirages = null;
		resetCamel();

		final NPCCombatDefinition defs = getCombatDefinitions();
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0)
					setNextAnimation(new Animation(defs.getDeathEmote()));
				else if (loop == 3) {
					// if(isHidden())
					// setNextWorldTile(new WorldTile(getX(), getY(), 0));
					setCantInteract(false);
					setCannotMove(false);
					drop();
					reset();
					getCombat().removeTarget();
					setLocation(getRespawnTile());
					finish();
					setRespawnTask();
					stop();
				}
				loop++;
			}
		}, 0, 1);

		super.sendDeath(source);
	}

}
