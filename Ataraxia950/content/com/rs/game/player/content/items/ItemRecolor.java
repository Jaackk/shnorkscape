package com.rs.game.player.content.items;

import com.rs.game.player.Player;

/**
 * Handles Item on Item re-colouring.
 *
 * @author Noel
 */
public class ItemRecolor {

	/**
	 * Re-color the item.
	 *
	 * @param player
	 *            The player.
	 * @param itemId
	 *            The default item.
	 * @param itemId2
	 *            The dye.
	 * @return if Successful re-color.
	 */
	public static boolean itemRecolor(final Player player, final int itemId, final int itemId2) {
		/**
		 * Robin hood hats.
		 */
		if (itemId == 1763 && itemId2 == 2581 || itemId2 == 1763 && itemId == 2581) {
			recolor(player, itemId, itemId2, 20949); /** Red **/
			return true;
		}
		if (itemId == 1765 && itemId2 == 2581 || itemId2 == 1765 && itemId == 2581) {
			recolor(player, itemId, itemId2, 20950); /** Yellow **/
			return true;
		}
		if (itemId == 1767 && itemId2 == 2581 || itemId2 == 1767 && itemId == 2581) {
			recolor(player, itemId, itemId2, 20951); /** Blue **/
			return true;
		}
		if (itemId == 1769 && itemId2 == 2581 || itemId2 == 1769 && itemId == 2581) {
			recolor(player, itemId, itemId2, 20952); /** White **/
			return true;
		}
		/**
		 * Staff of Lights.
		 */
		if (itemId == 1763 && itemId2 == 15486 || itemId2 == 1763 && itemId == 15486) {
			recolor(player, itemId, itemId2, 22207); /** Red **/
			return true;
		}
		if (itemId == 1765 && itemId2 == 15486 || itemId2 == 1765 && itemId == 15486) {
			recolor(player, itemId, itemId2, 22209); /** Yellow **/
			return true;
		}
		if (itemId == 1767 && itemId2 == 15486 || itemId2 == 1767 && itemId == 15486) {
			recolor(player, itemId, itemId2, 22211); /** Blue **/
			return true;
		}
		if (itemId == 1771 && itemId2 == 15486 || itemId2 == 1771 && itemId == 15486) {
			recolor(player, itemId, itemId2, 22213); /** Green **/
			return true;
		}
		/**
		 * Gnome Scarfs.
		 */
		if (itemId == 1763 && itemId2 == 9470 || itemId2 == 1763 && itemId == 9470) {
			recolor(player, itemId, itemId2, 22215); /** Red **/
			return true;
		}
		if (itemId == 1765 && itemId2 == 9470 || itemId2 == 1765 && itemId == 9470) {
			recolor(player, itemId, itemId2, 22216); /** Yellow **/
			return true;
		}
		if (itemId == 1767 && itemId2 == 9470 || itemId2 == 1767 && itemId == 9470) {
			recolor(player, itemId, itemId2, 22217); /** Blue **/
			return true;
		}
		if (itemId == 1771 && itemId2 == 9470 || itemId2 == 1771 && itemId == 9470) {
			recolor(player, itemId, itemId2, 22218); /** Green **/
			return true;
		}
		
		/**
		 * Ranger boots.
		 */
		if (itemId == 1763 && itemId2 == 2577 || itemId2 == 1763 && itemId == 2577) {
			recolor(player, itemId, itemId2, 22552); /** Red **/
			return true;
		}
		if (itemId == 1765 && itemId2 == 2577 || itemId2 == 1765 && itemId == 2577) {
			recolor(player, itemId, itemId2, 22558); /** Yellow **/
			return true;
		}
		if (itemId == 1767 && itemId2 == 2577 || itemId2 == 1767 && itemId == 2577) {
			recolor(player, itemId, itemId2, 22554); /** Blue **/
			return true;
		}
		if (itemId == 1769 && itemId2 == 2577 || itemId2 == 1769 && itemId == 2577) {
			recolor(player, itemId, itemId2, 22556); /** White **/
			return true;
		}
		/**
		 * Ancient staffs.
		 */
		if (itemId == 1763 && itemId2 == 4675 || itemId2 == 1763 && itemId == 4675) {
			recolor(player, itemId, itemId2, 24092); /** Red **/
			return true;
		}
		if (itemId == 1765 && itemId2 == 4675 || itemId2 == 1765 && itemId == 4675) {
			recolor(player, itemId, itemId2, 24098); /** Yellow **/
			return true;
		}
		if (itemId == 1767 && itemId2 == 4675 || itemId2 == 1767 && itemId == 4675) {
			recolor(player, itemId, itemId2, 24094); /** Blue **/
			return true;
		}
		if (itemId == 1771 && itemId2 == 4675 || itemId2 == 1771 && itemId == 4675) {
			recolor(player, itemId, itemId2, 24096); /** Green **/
			return true;
		}
		/**
		 * Top hats.
		 */
		if (itemId == 1763 && itemId2 == 13101 || itemId2 == 1763 && itemId == 13101) {
			recolor(player, itemId, itemId2, 24108); /** Red **/
			return true;
		}
		if (itemId == 1771 && itemId2 == 13101 || itemId2 == 1771 && itemId == 13101) {
			recolor(player, itemId, itemId2, 24110); /** Green **/
			return true;
		}
		if (itemId == 1767 && itemId2 == 13101 || itemId2 == 1767 && itemId == 13101) {
			recolor(player, itemId, itemId2, 24112); /** Blue **/
			return true;
		}
		if (itemId == 1769 && itemId2 == 13101 || itemId2 == 1769 && itemId == 13101) {
			recolor(player, itemId, itemId2, 24114); /** White **/
			return true;
		}
		/**
		 * Mage's books.
		 */
		if (itemId == 1767 && itemId2 == 6889 || itemId2 == 1767 && itemId == 6889) {
			recolor(player, itemId, itemId2, 24100); /** Blue **/
			return true;
		}
		if (itemId == 1771 && itemId2 == 6889 || itemId2 == 1771 && itemId == 6889) {
			recolor(player, itemId, itemId2, 24102); /** Green **/
			return true;
		}
		if (itemId == 1765 && itemId2 == 6889 || itemId2 == 1765 && itemId == 6889) {
			recolor(player, itemId, itemId2, 24104); /** Yellow **/
			return true;
		}
		if (itemId == 1769 && itemId2 == 6889 || itemId2 == 1769 && itemId == 6889) {
			recolor(player, itemId, itemId2, 24106); /** White **/
			return true;
		}
		/**
		 * Infinity hats.
		 */
		if (itemId == 1769 && itemId2 == 6918 || itemId2 == 1769 && itemId == 6918) {
			recolor(player, itemId, itemId2, 15602); /** White **/
			return true;
		}
		if (itemId == 1767 && itemId2 == 6918 || itemId2 == 1767 && itemId == 6918) {
			recolor(player, itemId, itemId2, 15608); /** Blue **/
			return true;
		}
		if (itemId == 1763 && itemId2 == 6918 || itemId2 == 1763 && itemId == 6918) {
			recolor(player, itemId, itemId2, 15614); /** Red **/
			return true;
		}
		if (itemId == 1771 && itemId2 == 6918 || itemId2 == 1771 && itemId == 6918) {
			recolor(player, itemId, itemId2, 15620); /** Green **/
			return true;
		}
		/**
		 * Infinity Robe tops.
		 */
		if (itemId == 1769 && itemId2 == 6916 || itemId2 == 1769 && itemId == 6916) {
			recolor(player, itemId, itemId2, 15600); /** White **/
			return true;
		}
		if (itemId == 1767 && itemId2 == 6916 || itemId2 == 1767 && itemId == 6916) {
			recolor(player, itemId, itemId2, 15606); /** Blue **/
			return true;
		}
		if (itemId == 1763 && itemId2 == 6916 || itemId2 == 1763 && itemId == 6916) {
			recolor(player, itemId, itemId2, 15612); /** Red **/
			return true;
		}
		if (itemId == 1771 && itemId2 == 6916 || itemId2 == 1771 && itemId == 6916) {
			recolor(player, itemId, itemId2, 15618); /** Green **/
			return true;
		}
		/**
		 * Infinity Robe bottoms.
		 */
		if (itemId == 1769 && itemId2 == 6924 || itemId2 == 1769 && itemId == 6924) {
			recolor(player, itemId, itemId2, 15604); /** White **/
			return true;
		}
		if (itemId == 1767 && itemId2 == 6924 || itemId2 == 1767 && itemId == 6924) {
			recolor(player, itemId, itemId2, 15610); /** Blue **/
			return true;
		}
		if (itemId == 1763 && itemId2 == 6924 || itemId2 == 1763 && itemId == 6924) {
			recolor(player, itemId, itemId2, 15616); /** Red **/
			return true;
		}
		if (itemId == 1771 && itemId2 == 6924 || itemId2 == 1771 && itemId == 6924) {
			recolor(player, itemId, itemId2, 15622); /** Green **/
			return true;
		}
		/**
		 * Abyssal whip.
		 */
		if (itemId == 1765 && itemId2 == 4151 || itemId2 == 1765 && itemId == 4151) {
			recolor(player, itemId, itemId2, 15441); /** Yellow **/
			return true;
		}
		if (itemId == 1767 && itemId2 == 4151 || itemId2 == 1767 && itemId == 4151) {
			recolor(player, itemId, itemId2, 15442); /** Blue **/
			return true;
		}
		if (itemId == 1769 && itemId2 == 4151 || itemId2 == 1769 && itemId == 4151) {
			recolor(player, itemId, itemId2, 15443); /** White **/
			return true;
		}
		if (itemId == 1771 && itemId2 == 4151 || itemId2 == 1771 && itemId == 4151) {
			recolor(player, itemId, itemId2, 15444); /** Green **/
			return true;
		}
		/**
		 * First tower armor red (hat, top, bottom)
		 */
		if (itemId == 1763 && itemId2 == 26119 || itemId2 == 1763 && itemId == 26119) {
			recolor(player, itemId, itemId2, 26118);
			return true;
		}
		if (itemId == 1763 && itemId2 == 26108 || itemId2 == 1763 && itemId == 26108) {
			recolor(player, itemId, itemId2, 26104);
			return true;
		}
		if (itemId == 1763 && itemId2 == 26107 || itemId2 == 1763 && itemId == 26107) {
			recolor(player, itemId, itemId2, 26103);
			return true;
		}
		/**
		 * First tower armor blue (hat, top, bottom)
		 */
		if (itemId == 1767 && itemId2 == 26119 || itemId2 == 1767 && itemId == 26119) {
			recolor(player, itemId, itemId2, 26116);
			return true;
		}
		if (itemId == 1767 && itemId2 == 26108 || itemId2 == 1767 && itemId == 26108) {
			recolor(player, itemId, itemId2, 26102);
			return true;
		}
		if (itemId == 1767 && itemId2 == 26107 || itemId2 == 1767 && itemId == 26107) {
			recolor(player, itemId, itemId2, 26101);
			return true;
		}
		/**
		 * First tower armor green (hat, top, bottom)
		 */
		if (itemId == 1771 && itemId2 == 26119 || itemId2 == 1771 && itemId == 26119) {
			recolor(player, itemId, itemId2, 26117);
			return true;
		}
		if (itemId == 1771 && itemId2 == 26108 || itemId2 == 1771 && itemId == 26108) {
			recolor(player, itemId, itemId2, 26106);
			return true;
		}
		if (itemId == 1771 && itemId2 == 26107 || itemId2 == 1771 && itemId == 26107) {
			recolor(player, itemId, itemId2, 26105);
			return true;
		}
		return false;
	}

	private static void recolor(final Player player, final int itemId, final int itemId2, final int productId) {
		player.getInventory().deleteItem(itemId, 1);
		player.getInventory().deleteItem(itemId2, 1);
		player.getInventory().addItem(productId, 1);
		player.sendMessage("You've successfully re-colored your item.");
	}
}