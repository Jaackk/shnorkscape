package com.rs.game.activites.soulwars;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.activites.soulwars.SoulWarsManager.PlayerType;
import com.rs.game.activites.soulwars.SoulWarsManager.Teams;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

import java.util.ArrayList;

/**
 * @author Savions Sw
 */
public class Avatar extends NPC {

	private static final long serialVersionUID = -7459713774773950181L;

	public Avatar(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, false);
		setLureDelay(0);
		setForceMultiAttacked(true);
		setCapDamage(500);
	}

	@Override
	public void handleIngoingHit(Hit hit) {
		if (hit.getSource() instanceof NPC || SoulWarsManager.MINUTES_BEFORE_NEXT_GAME.get() < 4)
			return;
		final Teams team = Teams.values()[getId() - SoulWarsManager.AVATAR_INDEX];
		if (((GameTask) World.soulWars.getTasks().get(PlayerType.IN_GAME))
				.getAvatarSlayerLevel(team) > ((Player) hit.getSource()).getSkills().getLevelForXp(Skills.SLAYER)) {
			((Player) hit.getSource()).getPackets()
					.sendGameMessage("Your slayer level is not high enough to damage this creature.");
			hit.setDamage(0);
		}
		super.handleIngoingHit(hit);
	}

	@Override
	public double getMeleePrayerMultiplier() {
		return 0.5;
	}

	@Override
	public ArrayList<Entity> getPossibleTargets() {
		ArrayList<Entity> targets = super.getPossibleTargets();
		final Teams team = Teams.values()[getId() - SoulWarsManager.AVATAR_INDEX];
		for (Player player : (((GameTask) World.soulWars.getTasks().get(PlayerType.IN_GAME)).getPlayers(team))) {
			if (player != null)
				targets.remove(player);
		}
		return targets;
	}

	@Override
	public void sendDeath(Entity source) {
		final NPCCombatDefinition defs = getCombatDefinitions();
		resetWalkSteps();
		getCombat().removeTarget();
		setNextAnimation(null);
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					setNextAnimation(new Animation(defs.getDeathEmote()));
				} else if (loop >= defs.getDeathDelay()) {
					if (SoulWarsManager.MINUTES_BEFORE_NEXT_GAME.get() > 3) {
						((GameTask) World.soulWars.getTasks().get(PlayerType.IN_GAME)).increaseKill(getId());
						reset();
						setLocation(getRespawnTile());
						finish();
						setRespawnTask();
						super.stop();
					}
				}
				loop++;
			}
		}, 0, 1);
	}
}