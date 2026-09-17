package com.rs.game.player.content.dungeoneering.rooms.puzzles;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.dungeonnering.DungeonNPC;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.dungeoneering.DungeonManager;
import com.rs.game.player.content.dungeoneering.rooms.PuzzleRoom;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class MonolithRoom extends PuzzleRoom {

	
	private static final int[] UNCHARGED_MONOLITHS = { 10975, 10976, 10977, 10976, 12175 };
	private static final int[] CHARGED_MONOLITHS = { 10978, 10979, 10980, 10979, 12176 };
	private Monolith monolith;
	private int charges = -1, shadeCount;
	private WorldTask monolithTask;
	private long lastShade;
	private final List<MysteriousShade> shades = new ArrayList<MysteriousShade>();
	
	@Override
	public void openRoom() {
		monolith = new Monolith(UNCHARGED_MONOLITHS[type], manager.getRotatedTile(reference, 7, 7), manager, 0.0);
	}
	
	@Override
	public boolean processNPCClick1(Player player, NPC npc) {
		if (npc instanceof Monolith) {
			if (charges == -1) {
				monolithTask = new MonolithTask(this);
				monolith.transformIntoNPC(CHARGED_MONOLITHS[type]);
				WorldTasksManager.schedule(monolithTask, 0, 1);
				player.sendMessage("You activate the Monolith.");
				return false;
			} else {
				if (charges * 5 < 100)
					player.sendMessage("The Monolith is currently powered to " + (charges * 5) + "% charges.");
				else
					player.sendMessage("The Monolith is fully charged!");
				return false;
			}
		}
		return true;
	}
	
	private class MonolithTask extends WorldTask {

		private final MonolithRoom room;
		
		public MonolithTask(MonolithRoom room) {
			this.room = room;
		}
		
		@Override
		public void run() {
			if (monolithTask == null) {
				if (charges > 0) {
					manager.message(reference, "The monolith is fully powered to maximum charges.");
					manager.hideBar(reference);
					setComplete();
					stop();
				} else {
					manager.message(reference, "The monolith explodes with unstable energy.");
					shades.forEach(shade -> shade.sendDeath(null));
					manager.hideBar(reference);
					monolith.transformIntoNPC(UNCHARGED_MONOLITHS[type]);
					shadeCount = 0;
					for (Player p : manager.getParty().getTeam())
						if (p.withinDistance(monolith, 8))
							p.applyHit(new Hit(null, Utils.random(p.getSkills().getLevelForXp(Skills.HITPOINTS * 3)), HitLook.REGULAR_DAMAGE));
				stop();
				}
				return;
			}
			charges++;
			if ((charges > 8 && shadeCount == 0 || Utils.random(100) > 50) && charges > 2 && lastShade < Utils.currentTimeMillis()) {
				WorldTile spawnTile = null;
				for (int i = 0; i < 25; i++) {
					WorldTile tile = manager.getRotatedTile(reference, Utils.random(16), Utils.random(16));
					if (World.canMoveNPC(0, tile.getX(), tile.getY(), 1)) {
						spawnTile = tile;
						break;
					}
				}
				if (spawnTile != null) {
					lastShade = Utils.currentTimeMillis() + 3000;
					shadeCount++;
					shades.add(new MysteriousShade(spawnTile, manager, 0.0, room));
				}
			}
			if (charges >= 0) {
				manager.showBar(reference, "Monolith Charges", charges * 5);
				if (charges >= 19)
					monolithTask = null;
			} else {
				if (charges == -1) {
					monolithTask = null;
					charges = -1;
					manager.hideBar(reference);
					stop();
				}
			}
		}
	}
	
	private static class Monolith extends DungeonNPC {

		private static final long serialVersionUID = -5481570086922572546L;

		public Monolith(int id, WorldTile tile, DungeonManager manager, double multiplier) {
			super(id, tile, manager, multiplier);
		}

		@Override
		public int getDirection() {
			return 0;
		}
	}
	
	private static class MysteriousShade extends DungeonNPC {

		private static final long serialVersionUID = -5481570086922572546L;
		
		private final MonolithRoom room;

		public MysteriousShade(WorldTile tile, DungeonManager manager, double multiplier, MonolithRoom room) {
			super(10831, tile, manager, multiplier);
			setForceAgressive(false);
			int value = manager.getParty().getSize() * 50;
			setCombatLevel((int) Utils.random(value * 0.7, value * 1.3));
			getCombatDefinitions().setHitpoints(value * 5);
			setHitpoints(this.getCombatDefinitions().getHitpoints());
			this.room = room;
			faceEntity(room.monolith);
			setFreezeDelay(500);
		}
		
		private int ticks;

		@Override
		public void processNPC() {
			super.processNPC();
			if (!isUnderCombat()) {
				if (ticks % 5 == 0 && room.monolith != null) {
					faceEntity(room.monolith);
					setNextAnimation(new Animation(13398));
					room.charges -= room.manager.getParty().getSize();
					if (room.charges <= 0) {
						room.monolithTask = null;
						room.charges = -1;
					}
				}
			}
			ticks++;
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
					}
					loop++;
				}
			}, 0, 1);

		}
	}
}
