package com.rs.game.activities.rots.npcs;

import com.rs.game.Hit;
import com.rs.game.WorldTile;
import com.rs.game.activities.rots.RiseOfTheSix;
import com.rs.game.activities.rots.effects.DeathCopter;
import com.rs.game.activities.rots.effects.Hurricane;
import com.rs.game.activities.rots.effects.RoTSEffect;
import com.rs.game.activities.rots.effects.SoulBind;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kris | 3. sept 2017 : 23:36.39
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public class VeracNPC extends RiseOfTheSixNPC {

	private static final long serialVersionUID = 1426379276384849758L;

	public VeracNPC(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned, RiseOfTheSix instance) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned, instance);
	}
	
	private boolean requestingAssistance;

	public void setRequest(final boolean value) {
		requestingAssistance = value;
	}

	public boolean isRequestingAssistance() {
		return requestingAssistance;
	}
	
	@Override
	public void processNPC() {
		super.processNPC();
		if (requestingAssistance && Utils.random(100) == 0) {
			final List<RiseOfTheSixNPC> possiblePartners = new ArrayList<RiseOfTheSixNPC>();
			final WorldTile center = instance.getWorldTile(34, 20);
			for (RiseOfTheSixNPC npc : instance.getWights()) {
				if (npc == null || npc.equals(this) || npc.getEffect() != null || npc.isDead() || npc.hasFinished() || npc instanceof AhrimNPC || npc instanceof KarilNPC)
					continue;
				if (getX() > center.getX() && npc.getX() > center.getX() || getX() < center.getX() && npc.getX() < center.getX())
					possiblePartners.add(npc);
			}
			if (possiblePartners.size() == 0)
				return;
			if (!(getEffect() instanceof DeathCopter))
				return;
			requestingAssistance = false;
			final DeathCopter copter = (DeathCopter) getEffect();
			copter.answerRequest(possiblePartners.get(Utils.random(possiblePartners.size())));
		}
	}

	@Override
	public void handleIngoingHit(final Hit hit) {
		super.handleIngoingHit(hit);
	}
	
	@Override
	public double getMeleePrayerMultiplier() {
		return 0.5;
	}

	@Override
	public final RoTSEffect generateEffect(final Player target) {
		final int random = Utils.random(6);
		switch(random) {
		case 0:
			return new DeathCopter(20, this, null);
		case 1:
		case 2:
			return new Hurricane(10, this, null);
			default:
				return new SoulBind(1, this, target);
		}
	}

}
