package com.rs.game.npc.telos;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class ColoredAnimaGolem extends NPC {

	private final Telos telos;
	private long damageIncreaseCycle;
	private double damageFactor;
	private boolean dead;

	public ColoredAnimaGolem(int id, WorldTile tile, Telos telos) {
		super(id, tile, -1, true, true);
		this.telos = telos;
		setHitpoints(getMaxHitpoints());
		setNextAnimation(new Animation(29000));
		damageFactor = 1.00;
		damageIncreaseCycle = Utils.currentTimeMillis() + 3000;
		setIntelligentRouteFinder(true);
		setForceTargetDistance(50);
	}

	@Override
	public boolean checkAgressivity() {
		if (getHitpoints() == 0)
			return false;
		ArrayList<Entity> possibleTarget = getPossibleTargets();
		if (!possibleTarget.isEmpty()) {
			Entity target = possibleTarget.get(Utils.random(possibleTarget.size()));
			setTarget(target);
			target.setAttackedBy(target);
			target.setFindTargetDelay(Utils.currentTimeMillis() + 10000);
			return true;
		}
		return false;
	}

	public ArrayList<Entity> getPossibleTargets() {
		return getPossibleTargets(false, true);
	}

	@Override
	public ArrayList<Entity> getPossibleTargets(boolean checkNPCs, boolean checkPlayers) {
		ArrayList<Entity> possibleTarget = new ArrayList<Entity>();
		if (telos.getInstance() != null)
			for (Player player : telos.getInstance().getPlayers()) {
				if (player == null || player.isDead())
					continue;
				possibleTarget.add(player);
			}
		return possibleTarget;
	}

	@Override
	public void processNPC() {
		if (damageIncreaseCycle != 0 && Utils.currentTimeMillis() >= damageIncreaseCycle) {
			damageFactor += 0.1;
			damageIncreaseCycle = Utils.currentTimeMillis() + 3000;
		}
		if (!isDead() && (telos.isDead() || telos.isDieing())) {
			sendDeath(telos);
		}
		if (isDead() || isLocked()) {
			return;
		}
		loadNPCSettings();
		getCombat().process();
	}

	@Override
	public boolean canWalkNPC(int toX, int toY) {
		return true;//this will make the golems able to walk under each other and under telos sweet
	}

	@Override
	public Hit handleOutgoingHit(Hit hit, Entity target) {
		if (getId() == 22905)
			hit.setDamage((int) (hit.getDamage() * damageFactor));
		super.handleOutgoingHit(hit, target);
		if (hit.getDamage() > 0 && Utils.random(100) <= 4 && target != null && !target.isDead()) {
			switch (getId()) {
			case 22907:
				// u can remove the stun from coloured golems here
				target.addStunDelay(4 * 600, true);
				break;
			case 22906:
				// target.getEffectsManager().startEffect(new
				// Effect(EffectType.ARMOUR_BREAK, 5000, 0));
				break;
			}
		}
		return hit;
	}

	@Override
	public int getAttackStyle() {
		return NPCCombatDefinitionConstants.MELEE;
	}

	@Override
	public int getAttackSpeed() {
		return 4;
	}

	@Override
	public int getMaxHitpoints() {
		if (telos == null || telos.getPlayer() == null)
			return super.getMaxHitpoints();
		int enrage = telos.getPlayer().getTelosEnrage();
		// thats how it should be was incorrect sorry
		return (int) (telos.getPhase() == 4 ? (450 + (2.5 * (enrage - 100))) : (800 + (2 * enrage)));
	}

	@Override
	public void sendDeath(Entity source) {
		if (dead)
			return;
		dead = true;
		if (!(source instanceof Telos) && telos != null && !telos.isDead() && !telos.hasFinished()) {
			telos.chargeFont(this);
		}
		super.sendDeath(source);
	}

	@Override
	public int getMaxDistance() {
		return 35;
	}

}
