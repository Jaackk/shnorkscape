package com.rs.game.npc.fightkiln;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.player.Player;
import com.rs.game.player.content.contracts.ContractHandler;
import com.rs.game.player.controllers.FightKiln;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class FightKilnNPC extends NPC {

	private final FightKiln controler;

	public FightKilnNPC(int id, WorldTile tile, FightKiln controler) {
		super(id, tile, -1, true, true);
		setForceMultiArea(true);
		setNoDistanceCheck(true);
		this.controler = controler;
	}

	private int getDeathGfx() {
		switch (getId()) {
		case 15201:
			return 2926;
		case 15202:
			return 2927;
		case 15203:
			return 2957;
		case 15213:
		case 15214:
		case 15204:
			return 2928;
		case 15205:
			return 2959;
		case 15206:
		case 15207:
			return 2929;
		case 15208:
		case 15211:
		case 15212:
			return 2973;
		default:
			return 2926;
		}
	}

	@Override
	public double getMagePrayerMultiplier() {
		return 0.1;
	}

	@Override
	public double getMeleePrayerMultiplier() {
		return 0.1;
	}

	@Override
	public ArrayList<Entity> getPossibleTargets() {
		ArrayList<Entity> possibleTarget = new ArrayList<Entity>(1);
		List<Integer> playerIndexes = World.getRegion(getRegionId()).getPlayerIndexes();
		if (playerIndexes != null) {
			for (int npcIndex : playerIndexes) {
				Player player = World.getPlayers().get(npcIndex);
				if (player == null || player.isDead() || player.hasFinished() || !player.isRunning())
					continue;
				possibleTarget.add(player);
			}
		}
		return possibleTarget;
	}

	@Override
	public double getRangePrayerMultiplier() {
		return 0.1;
	}

	@Override
	public void sendDeath(Entity source) {
		final NPCCombatDefinition defs = getCombatDefinitions();
		resetWalkSteps();
		getCombat().removeTarget();
		setNextAnimation(null);
		controler.checkCrystal();
		setNextGraphics(new Graphics(getDeathGfx()));
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					setNextAnimation(new Animation(defs.getDeathEmote()));
				} else if (loop >= defs.getDeathDelay()) {
					if (source instanceof Player) {
						ContractHandler.updateContract(((Player) source), FightKilnNPC.this);
					}
					reset();
					finish();
					controler.removeNPC();
					stop();
				}
				loop++;
			}
		}, 0, 1);
	}

}
