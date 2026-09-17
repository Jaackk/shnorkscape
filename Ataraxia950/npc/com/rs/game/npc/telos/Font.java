package com.rs.game.npc.telos;

import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

@SuppressWarnings("serial")
public class Font extends NPC {

	private final Telos telos;
	private long chargeCycle;
	private int charge;
	private boolean shieldActive;

	public Font(int id, WorldTile tile, Telos telos) {
		super(id, tile, 0, true, true);
		this.telos = telos;
		setHitpoints(getMaxHitpoints());
	}

	public Telos getTelos() {
		return telos;
	}

	@Override
	public void processNPC() {
		if (telos.getPhase() == 3 && chargeCycle != 0 && !shieldActive && Utils.currentTimeMillis() >= chargeCycle
				&& telos.getPlayer() != null && Utils.isOnRange(telos.getPlayer(), this, 1) && getHitpoints() != 0) {
			chargeCycle = Utils.currentTimeMillis() + 1200;
			int damage = (int) (this.getMaxHitpoints() * 0.20);
			telos.getPlayer().applyHit(new Hit(this, damage, HitLook.REGULAR_DAMAGE));
			if (getHitpoints() >= getMaxHitpoints()) {
				telos.getPlayer().getPackets().sendEntityMessage(1, 15263739, telos.getPlayer(),
						"You draw anima from the font, but it damages you in the process.", true);
			}
			applyHit(new Hit(this, damage, HitLook.REGULAR_DAMAGE));
		}
		super.processNPC();
	}

	@Override
	public int getMaxHitpoints() {
		if (telos == null || telos.getPlayer() == null)
			return super.getMaxHitpoints();
		int enrage = telos.getPlayer().getTelosEnrage() >= 500 ? 500 : telos.getPlayer().getTelosEnrage();
		return (int) (500 + (10 * Math.floor((double) enrage / 10.00)));
	}

	public void startRegularChargeCycle() {
		chargeCycle = Utils.currentTimeMillis() + 1200;
	}

	@Override
	public void sendDeath(Entity source) {
		if (source instanceof Telos) {
			chargeCycle = 0;
			shieldActive = false;
			setNextGraphics(new Graphics(-1));
			setNextNPCTransformation(getId() + 9);
			return;
		}
		shieldActive = true;
		setNextGraphics(new Graphics(3810));
		// super.sendDeath(source);
	}

	public boolean isShieldActive() {
		return shieldActive;
	}

	public void setShieldActive(boolean shieldActive) {
		this.shieldActive = shieldActive;
	}

	public int getCharge() {
		return charge;
	}

	public void setCharge(int charge) {
		this.charge = charge;
		int newId = getId() <= 22914 && charge >= 2 ? getId() + 3
				: getId() >= 22915 && charge < 2 ? getId() - 3 : getId();
		if (newId != getId())
			setNextNPCTransformation(newId);
	}

	@Override
	public boolean restoreHitPoints() {
		return false;
	}

	public void processOnGolemDeath(ColoredAnimaGolem golem) {
		if (telos.getPhase() != 4)
			return;
		int golemColor = golem.getId() == 22905 ? 0 : golem.getId() == 22906 ? 2 : 1;
		int fontColor = getId() == 22912 ? 0 : getId() == 22913 ? 2 : 1;
		if (golemColor == fontColor && golem.withinArea(getX() - 4, getY() - 4, getX() + 4, getY() + 4)) {
			int delay = Utils.projectileTimeToCycles(
					World.sendProjectileNew(golem, this, 6266 + golemColor, 40, 5, 24, 2, 16, 5).getEndTime()) - 1;
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					setCharge(charge + 1);
					addAnimaBar();
				}

			}, delay);
		}
	}

	public void addAnimaBar() {
		// getNextHitBars().add(new AnimaHitBar(this));
	}

	@Override
	public boolean isDead() {
		return false;
	}

}
