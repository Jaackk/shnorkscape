package com.rs.game.npc.kalphiteking;

import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.map.bossInstance.impl.KalphiteKingInstance;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class ExiledKalphiteMarauder extends NPC {
	private final transient KalphiteKingInstance instance;

	public ExiledKalphiteMarauder(int id, WorldTile tile, KalphiteKingInstance instance) {
		super(id, tile, -1, true, true);
		this.instance = instance;
		setForceMultiArea(true);
		setForceAgressive(true);
		setForceTargetDistance(24);
		setIntelligentRouteFinder(true);
	}

	@Override
	public void processNPC() {
		super.processNPC();
		if (getKKInstance() == null || getKKInstance().isFinished()) {
			finish();
		}
	}

	@Override
	public ArrayList<Entity> getPossibleTargets(boolean checkNPCs, boolean checkPlayers) {
		ArrayList<Entity> possibleTarget = new ArrayList<Entity>();
		for (Player player : instance.getPlayers()) {
			if (player == null || player.isDead() || player.hasFinished() || !player.isRunning()
					|| player.getAppearence().isHidden())
				continue;
			possibleTarget.add(player);
		}
		return possibleTarget;
	}

	@Override
	public boolean checkAgressivity() {
		ArrayList<Entity> possibleTarget = getPossibleTargets();
		if (!possibleTarget.isEmpty()) {
			Entity target = possibleTarget.get(Utils.random(possibleTarget.size()));
			setTarget(target);
			target.setAttackedBy(target);
			target.setFindTargetDelay(Utils.currentTimeMillis() + 10000);
			return true;
		}
		return false;
	}

	public KalphiteKingInstance getKKInstance() {
		return instance;
	}
}
