package com.rs.game.npc.slayer;

import com.rs.game.Hit;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;

public class HarpieBugSwarm extends NPC {

	private static final long serialVersionUID = 1337379286462996024L;

	public HarpieBugSwarm(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
	}

	public static boolean handleLitBugLantern(Player player, Item item) {
		if (item.getId() == 7051) {
			if (!player.getInventory().containsItem(new Item(590, 1)) && !player.getToolBelt().contains(590)) {
				player.sendMessage("You need a tinderbox to light the lantern.", true);
				return true;
			}
			item.setId(7053);
			player.sendMessage("You light the lantern.", true);
			player.getInventory().refresh();
			return true;
		} else if (item.getId() == 7053) {
			item.setId(7051);
			player.getInventory().refresh();
			player.sendMessage("You extinguish the lantern.", true);
			return true;
		}
		return false;
	}

	@Override
	public void handleIngoingHit(final Hit hit) {
		if (hit.getSource() instanceof Player) {
			Player player = (Player) hit.getSource();
			if (player.getEquipment().getShieldId() != 7053) {
				if (player.getEquipment().getShieldId() != 7051)
					player.sendMessage("Maybe you should wield something to distract the bugs..", true);
				else
					player.sendMessage("You oughta lit the lantern to distract the bugs.", true);
			}
		}
		super.handleIngoingHit(hit);
	}
	
	public static final boolean canDamage(Player player) {
		return player.getEquipment().getShieldId() == 7053;
	}

}