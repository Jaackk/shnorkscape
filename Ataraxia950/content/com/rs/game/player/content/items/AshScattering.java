package com.rs.game.player.content.items;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles the Scattering of various prayer Ashes.
 *
 * @author Noel.
 */
public class AshScattering {

	/**
	 * Handles the scattering of the ashes
	 *
	 * @param player
	 *            - the player
	 * @param slot
	 *            - the inventory slot
	 * @return
	 */
	public static boolean scatter(final Player player, final int slot) {
		final Item item = player.getInventory().getItem(slot);
		if(item == null)
			return false;
		final AshesData ashesData = AshesData.forId(item.getId());
		if ( ashesData == null || player.isLocked())
			return false;
		player.lock(1);
		player.setNextAnimation(new Animation(445));
		player.setNextGraphics(new Graphics(ashesData.getGFX()));
		player.addBonesOffered();
		player.sendMessage("You scatter the ashes in the wind; bones offered: " + Colors.wrap(Colors.RED, Utils.formatNumber(player.getBonesOffered())) + ".", true);
		player.getSkills().addXp(Skills.PRAYER, ashesData.getExp());
		player.getInventory().deleteItem(item.getId(), 1);
        int restoration = 0;
        boolean aura = false;
        boolean amulet = false;
        if (player.getAuraManager().getPrayerRestoration() != 0) {
            restoration = ((int) ((int) (Math.floor(player.getSkills().getLevelForXp(Skills.PRAYER))) * player.getAuraManager().getPrayerRestoration()));
            aura = true;
            /** Twisted Bird Skull necklace **/
        }
        if (player.getEquipment().getAmuletId() == 19886) {
            amulet = true;
            restoration = restoration + ((int) ((int) ashesData.getExp() * 0.5));
            /** Split dragontooth necklace **/
        } else if (player.getEquipment().getAmuletId() == 19887) {
            amulet = true;
            restoration = restoration + ((int) ((int) ashesData.getExp() * 0.75));
            /** Demon horn necklace **/
        } else if (player.getEquipment().getAmuletId() == 19888) {
            amulet = true;
            restoration = restoration + ((int) ashesData.getExp());
        }
        if (restoration + player.getPrayer().getPrayerpoints() > player.getSkills().getLevelForXp(Skills.PRAYER) * 10)
            restoration = (player.getSkills().getLevelForXp(Skills.PRAYER) * 10) - player.getPrayer().getPrayerpoints();
        if (restoration > 0) {
            player.getPrayer().restorePrayer(restoration);
            if (aura && amulet)
                player.sendMessage("Your aura along with the amulet restore " + restoration + " prayer points.", true);
            else if (aura)
                player.sendMessage("Your aura restores " + restoration + " prayer points.", true);
            else if (amulet)
                player.sendMessage("Your amulet restores " + restoration + " prayer points.", true);
        }
		return true;
	}

	public enum AshesData {

		IMPIOUS(20264, 25, 56),
		ACCURSED(20266, 50, 47),
		INFERNAL(20268, 130, 40),
		INFERNALA(22312, 130, 40),
		TORTURED(32945, 160, 40),
		SEARING(34159, 230, 40),
		EVIL_DUST(3325, 350, 40);

		private static final Map<Integer, AshesData> ashes = new HashMap<Integer, AshesData>();

		static {
			for (AshesData ash : AshesData.values()) {
				ashes.put(ash.getItemId(), ash);
			}
		}

		private final int itemId;
		private final int gfx;
		private final double exp;

		AshesData(int itemId, double exp, int gfx) {
			this.itemId = itemId;
			this.exp = exp;
			this.gfx = gfx;
		}

		public static AshesData forId(int id) {
			return ashes.get(id);
		}

		public int getItemId() {
			return itemId;
		}

		public double getExp() {
			return exp;
		}

		public int getGFX() {
			return gfx;
		}
	}

}