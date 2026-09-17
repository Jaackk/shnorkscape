package com.rs.game.activities.rots.effects;

import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.activities.rots.npcs.RiseOfTheSixNPC;
import com.rs.game.hitbar.impl.HitBarTimer;
import com.rs.game.npc.NPC;
import com.rs.game.tasks.WorldTask;

/**
 * @author Kris | 3. sept 2017 : 23:34.51
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public class Healing extends WorldTask {

	private final RiseOfTheSixNPC npc;
	private int loop;
	private boolean reset;

	public Healing(RiseOfTheSixNPC npc, boolean reset) {
		this.npc = npc;
		this.reset = reset;
	}

	@Override
	public void run() {
		if (loop < 51) {
			for (RiseOfTheSixNPC wights : npc.getInstance().getWights()) {
				if (wights != null && wights.isDead()) {
					if (reset && wights.getHealing() != null)
						wights.getHealing().loop = 0;
					wights.getNextHitBars().clear();
					wights.getNextHitBars().add(new HitBarTimer(wights.getHealing() == null ? 0 : (wights.getHealing().loop * 2)));
					npc.setNextRenderAnimation(2982);
				} else if (reset) {
					wights.heal(1);
					if (wights.getHitpoints() < wights.getMaxHitpoints())
						wights.applyHit(new Hit(null, wights.getMaxHitpoints() - wights.getHitpoints() < 500 ? (wights.getMaxHitpoints() - wights.getHitpoints()) : 500, HitLook.HEALED_DAMAGE));
				}
			}
			if (reset)
				reset = false;
		} else if (loop == 53) {
			for (NPC wights : npc.getInstance().getWights()) {
				if (wights.isDead()) {
					wights.setHitpoints(1);
					HitBarTimer timer = new HitBarTimer(100);
					timer.setDisplay(true);
					wights.getNextHitBars().add(timer);
					wights.applyHit(new Hit(null, 2500, HitLook.HEALED_DAMAGE));
					wights.setNextRenderAnimation(2689);
				}
			}
			stop();
		}
		loop++;
	}

}
