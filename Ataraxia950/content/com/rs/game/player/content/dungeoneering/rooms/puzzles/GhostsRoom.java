package com.rs.game.player.content.dungeoneering.rooms.puzzles;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.NewProjectile;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.dungeonnering.DungeonNPC;
import com.rs.game.player.Player;
import com.rs.game.player.content.dungeoneering.DungeonManager;
import com.rs.game.player.content.dungeoneering.rooms.PuzzleRoom;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class GhostsRoom extends PuzzleRoom {

	private Ghost[] ghosts;
	private Ghost spiritGhost;
	
	@Override
	public void openRoom() {
		ghosts = new Ghost[3];
		for (int i = 0; i < 3; i++) {
			loop: while (true) {
				WorldTile tile = new WorldTile(manager.getRotatedTile(reference, Utils.random(16), Utils.random(16)));
				if (World.canMoveNPC(0, tile.getX(), tile.getY(), 1)) {
					ghosts[i] = new Ghost(tile, manager, 0.0, this);
					break loop;
				}
			}
		}
		spiritGhost = ghosts[Utils.random(3)];
	}
	
	private static class Ghost extends DungeonNPC {

		private static final long serialVersionUID = -5481570086922572546L;

		private final GhostsRoom room;

		public Ghost(WorldTile tile, DungeonManager manager, double multiplier, GhostsRoom room) {
			super(10821, tile, manager, multiplier);
			this.room = room;
			int value = manager.getParty().getSize() * 50;
			setCombatLevel((int) Utils.random(value * 0.7, value * 1.3));
			getCombatDefinitions().setHitpoints(value * 20);
			setHitpoints(this.getCombatDefinitions().getHitpoints());
		}
		
		private int ticks;
		
		private void sendGhost() {
			List<Ghost> aliveGhosts = new ArrayList<Ghost>();
			for (Ghost ghost : room.ghosts) {
				if (ghost.isDead())
					continue;
				if (ghost.hasFinished())
					continue;
				if (ghost != this)
					aliveGhosts.add(this);
			}
			if (aliveGhosts.size() == 0)
				return;
			final Ghost t = aliveGhosts.get(Utils.random(aliveGhosts.size()));
			NewProjectile projectile = new NewProjectile(this, t, 2383, 0, 0, 0, 30, 5, 0);
			World.sendProjectile(projectile);
			room.spiritGhost = t;
		}

		@Override
		public void processNPC() {
			super.processNPC();
			if (this.isDead())
				return;
			if (ticks % 15 == 0 && this.equals(room.spiritGhost) && ticks != 0)
				sendGhost();
			ticks++;
		}
		
		@Override
		public void handleIngoingHit(Hit hit) {
			if (!this.equals(room.spiritGhost))
				hit.setDamage(0);
			super.handleIngoingHit(hit);
		}
		
		@Override
		public void sendDeath(Entity source) {
			resetWalkSteps();
			getCombat().removeTarget();
			if (source instanceof Player)
				source.deathResetCombat();
			setNextAnimation(null);
			if (isMarked()) {
				getManager().removeMark();
				setMarked(false);
			}
			if (room.spiritGhost == this)
				sendGhost();
			WorldTasksManager.schedule(new WorldTask() {
				int loop;

				@Override
				public void run() {
					if (loop == 0)
						setNextAnimation(new Animation(5542));
					else if (loop >= 1) {
						reset();
						finish();
						stop();
						boolean allDead = true;
						for (Ghost ghost : room.ghosts) {
							if (!ghost.isDead() && !ghost.hasFinished())
								allDead = false;
						}
						if (allDead)
							room.setComplete();
					}
					loop++;
				}
			}, 0, 1);

		}
	}
}
