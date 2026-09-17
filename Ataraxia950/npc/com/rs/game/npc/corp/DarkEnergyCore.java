package com.rs.game.npc.corp;

import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.Projectile;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class DarkEnergyCore extends NPC {

	private final CorporealBeast beast;
	private final DarkEnergyCore core = this;
	private Entity target = null;
	private Projectile projectile;
	private int changeTarget;
	WorldTile toTile;

	private int delay;

	public DarkEnergyCore(CorporealBeast beast) {
		super(8127, beast, -1, true, true);
		setForceMultiArea(true);
		this.beast = beast;
		changeTarget = 2;
	}

	@Override
	public double getMagePrayerMultiplier() {
		return 0.67;
	}

	@Override
	public void processNPC() {
		if (isDead() || hasFinished())
			return;
		if (delay > 0) {
			delay--;
			return;
		}
		ArrayList<Entity> possibleTargets = beast.getPossibleTargets();

		if (!possibleTargets.isEmpty())
			target = possibleTargets.get(Utils.getRandom(possibleTargets.size() - 1));
		if (changeTarget > 0) {
			if (changeTarget == 1) {
				if (possibleTargets.isEmpty()) {
					finish();
					beast.removeDarkEnergyCore();
					return;
				}
				projectile = new Projectile(core, target, false, false, 0, 1828, 0, 0, 0, 40, 20, 0);
				setNextWorldTile(new WorldTile(core.getX() + 50, core.getY() + 50, core.getPlane()));
				World.sendProjectile(core, core, target, 1828, 0, 0, 40, 40, 20, 0);
				toTile = new WorldTile(target);
				WorldTasksManager.schedule(new WorldTask() {

					@Override
					public void run() {
						setNextWorldTile(toTile);
					}

				}, projectile.getDuration() / 335);

			}
			changeTarget--;
		}
		if (possibleTargets.isEmpty()) {
			finish();
			beast.removeDarkEnergyCore();
			return;
		}
		if (target == null || !target.withinDistance(core, 1)) {
			if (changeTarget == 0)
				changeTarget = 5;
		}
		int damage = Utils.random(15, 170);
		possibleTargets.forEach(t -> {
			if (t.withinDistance(this, 1)) {
				t.applyHit(new Hit(this, damage, HitLook.MAGIC_DAMAGE));
				if (t instanceof Player) {
					Player player = (Player) t;
					player.sendMessage("The dark core creature steals some life from you for its master.", true);
				}
			}
		});
		beast.heal(damage);
		delay = getPoison().isPoisoned() ? 10 : 3;
	}

	@Override
	public void sendDeath(Entity source) {
		super.sendDeath(source);
		beast.removeDarkEnergyCore();
	}
}