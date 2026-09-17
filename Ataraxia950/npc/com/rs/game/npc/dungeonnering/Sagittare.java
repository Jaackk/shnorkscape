package com.rs.game.npc.dungeonnering;

import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.content.dungeoneering.DungeonManager;
import com.rs.game.player.content.dungeoneering.DungeonUtils;
import com.rs.game.player.content.dungeoneering.RoomReference;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.data.parsers.npcs.pojos.NPCDrop;

@SuppressWarnings("serial")
public class Sagittare extends DungeonBoss {

	private int stage;
	private boolean special;

	public Sagittare(int id, WorldTile tile, DungeonManager manager, RoomReference reference) {
		super(id, tile, manager, reference);
		setCantFollowUnderCombat(true);
		stage = 3;
	}

	@Override
	public void processNPC() {
		super.processNPC();
		int max_hp = getMaxHitpoints();
		int current_hp = getHitpoints();

		if ((current_hp == 1 || current_hp < max_hp * (.25 * stage)) && !special) {
			special = true;
			stage--;
			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					special = false;
				}
				
			}, 8);
		}
	}

	@Override
	public void handleIngoingHit(Hit hit) {
		int damage = hit.getDamage();
		if (damage > 0) {
			if (hit.getLook() == HitLook.RANGE_DAMAGE)
				hit.setDamage((int) (damage * .4));
		}
		super.handleIngoingHit(hit);
	}

	public boolean isUsingSpecial() {
		return special;
	}

	public void setUsingSpecial(boolean special) {
		this.special = special;
	}

	public int getStage() {
		return stage;
	}

	@Override
	public void sendDeath(final Entity source) {
		if (stage != -1) {
			setHitpoints(1);
			return;
		}
		super.sendDeath(source);
	}

	@Override
	public void sendDrop(Player player, NPCDrop drop, boolean lootbeam) {
		if (drop.getItemId() >= 16317 && drop.getItemId() <= 16337) {
			int tier = (drop.getItemId() - 16317) / 2 + 1;
			player.getInventory().addItemDrop(DungeonUtils.getArrows(tier), 125);
		}
		super.sendDrop(player, drop, false);
	}
}
