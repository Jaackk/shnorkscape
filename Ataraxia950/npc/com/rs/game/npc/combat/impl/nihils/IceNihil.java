package com.rs.game.npc.combat.impl.nihils;

import com.rs.cores.CoresManager;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.others.Nihils;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

import java.util.concurrent.TimeUnit;

public class IceNihil extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { 19147 };
	}

	private Player t;
	private boolean start, frozen;

	@Override
	public int attack(final NPC npc, final Entity target) {
		if (!(npc instanceof Nihils)) {
			return 0;
		}
		if (Utils.random(10) == 0) {
			if (target instanceof Player) {
				if (!((Player) target).isROTSLocked()) {
					npc.setNextAnimation(new Animation(23017));
					npc.setNextGraphics(new Graphics(4686));
					npc.setNextForceTalk(new ForceTalk("Hiss."));
					start = true;
					return 2;
				}
			}
		}
		if (start && !frozen) {
			if (target instanceof Player) {
				t = ((Player) target);
				t.lockROTS();
				t.setTarget(null);
				frozen = true;
				CoresManager.getServiceProvider().executeWithDelay(() -> {
					t.unlockROTS();
					t.setTarget(npc);
					frozen = false;
				}, 2500, TimeUnit.MILLISECONDS);
			}
			start = false;
		}
		npc.setNextAnimation(new Animation(23037));
		World.sendProjectile(npc, npc, target, 4697, 5, 15, 3, 0, 0, 0);
		delayHit(npc, 1, target, getMagicHit(npc, Utils.random(300)));
		return 6;
	}

}
