package com.rs.game.npc.gwd2;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.player.Player;
import com.rs.game.player.content.HeartOfGielinor;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class SerenReinforcement extends SerenFaction {

	private static final long serialVersionUID = 871504735211392833L;
	private final Player owner;
	
	public SerenReinforcement(int id, WorldTile tile, Player owner) {
		super(id, tile, -1, true, true);
		this.owner = owner;
	}
	
	public final Player getOwner() {
		return owner;
	}
	
	@Override
	public ArrayList<Entity> getPossibleTargets() {
		int size = getSize();
		ArrayList<Entity> possibleTarget = new ArrayList<Entity>();
		for (int regionId : getMapRegionsIds()) {
			List<Integer> playerIndexes = World.getRegion(regionId).getPlayerIndexes();
			if (playerIndexes != null) {
				for (int playerIndex : playerIndexes) {
					Player player = World.getPlayers().get(playerIndex);
					if (player == null || player.isDead() || player.equals(owner) || player.hasFinished() || !player.isRunning() || player.getAppearence().isHidden() || player.getHeart().getActiveInsignia() == HeartOfGielinor.SEREN || !Utils.isOnRange(getX(), getY(), size, player.getX(), player.getY(), player.getSize(), getForceTargetDistance() > 0 ? getForceTargetDistance() : 1) || !clipedProjectile(player, false))
						continue;
					possibleTarget.add(player);
				}
			}
			List<Integer> npcsIndexes = World.getRegion(regionId).getNPCsIndexes();
			if (npcsIndexes != null) {
				for (int npcIndex : npcsIndexes) {
					NPC npc = World.getNPCs().get(npcIndex);
					if (npc == null || npc == this || npc instanceof Familiar && ((Familiar) npc).getOwner().equals(owner) || npc.getId() == 16980 || npc.isDead() || npc.hasFinished() || npc instanceof SerenFaction || !Utils.isOnRange(getX(), getY(), size, npc.getX(), npc.getY(), npc.getSize(), getForceTargetDistance() > 0 ? getForceTargetDistance() : 1) || !npc.getDefinitions().hasAttackOption() || !clipedProjectile(npc, false))
						continue;
					possibleTarget.add(npc);
				}
			}
		}
		return possibleTarget;
	}
	
	@Override
	public void sendDeath(final Entity source) {
		final NPCCombatDefinition defs = getCombatDefinitions();
		resetWalkSteps();
		getCombat().removeTarget();
		HeartOfGielinor.REINFORCEMENTS.remove(this);
		setNextAnimation(null);
		if (source != null && source instanceof Player) {
			Player p = (Player) source;
			if (p != owner) {
				if (p.getHeart().getBountyTargetFaction() == HeartOfGielinor.SEREN) {
					if (p.getHeart().getCurrentBounty() != 0)
						p.getHeart().decrementBounty();
				}
				p.getHeart().sendMultiplier();
			}
		}
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					setNextAnimation(new Animation(defs.getDeathEmote()));
				} else if (loop >= defs.getDeathDelay()) {
					if (source instanceof Player) {
						Player player = (Player) source;
						if (player != owner)
							player.getHeart().incrementKillcount(HeartOfGielinor.SEREN);
					}
					drop();
					reset();
					setLocation(getRespawnTile());
					finish();
					stop();
				}
				loop++;
			}
		}, 0, 0);
	}

}
