package com.rs.game.activities.rots.effects;

import com.rs.cache.loaders.NPCDefinitions;
import com.rs.game.Animation;
import com.rs.game.activities.rots.npcs.AhrimNPC;
import com.rs.game.activities.rots.npcs.RiseOfTheSixNPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * @author Kris | 3. sept 2017 : 23:34.42
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public class Flight extends RoTSEffect {

	public Flight(int duration, RiseOfTheSixNPC npc, Player player) {
		super(duration, npc, player);
	}

	@Override
	public void start() {
		npc.setNextAnimation(new Animation(21926));
		WorldTasksManager.schedule(new WorldTask() {
			private int tick;
			final AhrimNPC ahrims = (AhrimNPC) npc;
			@Override
			public void run() {
				if (cancel() || tick == duration - 1) {
					npc.finishEffect();
					npc.refreshSpecialDelay();
					npc.setNextAnimation(new Animation(21927));
					npc.transformIntoNPC(18538);
					npc.setNextRenderAnimation(NPCDefinitions.getNPCDefinitions(18538).getRenderAnimation());
					stop();
					return;
				} else if (tick == 0) {
					npc.transformIntoNPC(18539);
					npc.setNextRenderAnimation(NPCDefinitions.getNPCDefinitions(18539).getRenderAnimation());
				} 
				if (ahrims.getLastPit() < Utils.currentTimeMillis() && Utils.random(5) == 0)
					instance.addPit(ahrims);
				tick++;
			}
		}, 1, 0);

	}

}
