package com.rs.game.npc.fightcaves;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

@SuppressWarnings("serial")
public class TzKekCaves extends FightCavesNPC {

	public TzKekCaves(int id, WorldTile tile) {
		super(id, tile);
	}

	@Override
	public void sendDeath(Entity source) {
		final NPCCombatDefinition defs = getCombatDefinitions();
		resetWalkSteps();
		Entity target = getCombat().getTarget();
		getCombat().removeTarget();
		setNextAnimation(null);
		final WorldTile tile = this;
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					setNextAnimation(new Animation(defs.getDeathEmote()));
					setNextGraphics(new Graphics(2924 + getSize()));
				} else if (loop >= defs.getDeathDelay()) {
					reset();
					NPC npc1 = new FightCavesNPC(2738, tile);
					if (World.canMoveNPC(getPlane(), tile.getX() + 1, tile.getY(), 1))
						tile.moveLocation(1, 0, 0);
					else if (World.canMoveNPC(getPlane(), tile.getX() - 1, tile.getY(), 1))
						tile.moveLocation(-1, 0, 0);
					else if (World.canMoveNPC(getPlane(), tile.getX(), tile.getY() - 1, 1))
						tile.moveLocation(0, -1, 0);
					else if (World.canMoveNPC(getPlane(), tile.getX(), tile.getY() + 1, 1))
						tile.moveLocation(0, 1, 0);
					NPC npc2 = new FightCavesNPC(2738, tile);
					npc1.setTarget(target);
					npc2.setTarget(target);
					finish();
					stop();
				}
				loop++;
			}
		}, 0, 1);
	}

	@Override
	public void removeHitpoints(Hit hit) {
		super.removeHitpoints(hit);
		if (hit.getLook() != HitLook.MELEE_DAMAGE || hit.getSource() == null)
			return;
		hit.getSource().applyHit(new Hit(this, 10, HitLook.REGULAR_DAMAGE));
	}
}
