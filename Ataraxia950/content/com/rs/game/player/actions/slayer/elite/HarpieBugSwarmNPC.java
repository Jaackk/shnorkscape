package com.rs.game.player.actions.slayer.elite;

import com.rs.game.Hit;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;

/**
 * @author Kris | 3. okt 2018 : 15:07:50
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 * @see <a href="https://rune-status.net/members/kris.354/">Rune-Status profile</a>
 */
public class HarpieBugSwarmNPC extends EliteNPC {

	private static final long serialVersionUID = 3312747580585237202L;

	protected HarpieBugSwarmNPC(final Player owner, final int id, final WorldTile tile) {
		super(owner, id, tile);
	}

	public static boolean handleLitBugLantern(final Player player, final Item item) {
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
			final Player player = (Player) hit.getSource();
			if (player.getEquipment().getShieldId() != 7053) {
				if (player.getEquipment().getShieldId() != 7051) {
					player.sendMessage("Maybe you should wield something to distract the bugs..", true);
				} else {
					player.sendMessage("You oughta lit the lantern to distract the bugs.", true);
				}
			}
		}
		super.handleIngoingHit(hit);
	}
	
	public static final boolean canDamage(final Player player) {
		return player.getEquipment().getShieldId() == 7053;
	}

	
}
