package com.rs.game.npc.others;

import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.content.barrows.Barrows;
import com.rs.utils.Utils;

public class BarrowsCreature extends NPC {

	private static final long serialVersionUID = 6565006751085268328L;

	private Barrows barrows;
	private final boolean isWight;
	@SuppressWarnings("unused")
	private final int[] damageTaken;

	public BarrowsCreature(int id, WorldTile tile, Barrows barrows, boolean wight) {
		super(id, tile, -1, true, true);
		this.barrows = barrows;
		this.isWight = wight;
		damageTaken = new int[4];
	}
	
	@Override
	public void handleIngoingHit(final Hit hit) {
		if (hit.getLook() == HitLook.MELEE_DAMAGE && getId() == 14298 || hit.getLook() == HitLook.MAGIC_DAMAGE && getId() == 14299 || hit.getLook() == HitLook.RANGE_DAMAGE && getId() == 14300)
			hit.setDamage(0);
		super.handleIngoingHit(hit);
		//damageTaken[hit.getLook() == HitLook.MELEE_DAMAGE ? 0 : hit.getLook() == HitLook.MAGIC_DAMAGE ? 1 : hit.getLook() == HitLook.RANGE_DAMAGE ? 2 : 3] += hit.getDamage();
		if (getId() == 22721 && hit.getDamage() > 0 && Utils.random(4) == 0) {
			if (hit.getSource() != null)
				hit.getSource().applyHit(new Hit(this, Utils.random(50, 80), HitLook.REFLECTED_DAMAGE));
		}
		//Disabled akrisae's effect until overheads are sorted.
		/*if (getId() >= 14297 && getId() <= 14300) {
			if (damageTaken[hit.getLook() == HitLook.MELEE_DAMAGE ? 0 : hit.getLook() == HitLook.MAGIC_DAMAGE ? 1 : hit.getLook() == HitLook.RANGE_DAMAGE ? 2 : 3] >= 150) {
				this.transformIntoNPC(14298 + (hit.getLook() == HitLook.MELEE_DAMAGE ? 0 : hit.getLook() == HitLook.MAGIC_DAMAGE ? 1 : 2));
				for (int i = 0; i < damageTaken.length; i++)
					damageTaken[i] = 0;
			}
		}*/
	}

	public void removeNPC() {
		barrows = null;
		finish();
	}
	
	public final boolean isWight() {
		return isWight;
	}

	@Override
	public void finish() {
		if (hasFinished())
			return;
		if (barrows != null) {
			barrows.resetTarget();
			barrows = null;
		}
		super.finish();
	}

	@Override
	public void sendDeath(Entity source) {
		if (barrows != null) {
			barrows.finishTarget();
			barrows = null;
		}
		super.sendDeath(source);
	}

}
