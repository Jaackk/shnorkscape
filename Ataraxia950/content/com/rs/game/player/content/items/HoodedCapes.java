package com.rs.game.player.content.items;

import com.rs.game.player.Player;

/**
 * Handles Hooding capes of accomplishment.
 *
 * @author Noel
 */
public class HoodedCapes {

	/**
	 * Handles adding the hoods to their respective capes.
	 *
	 * @param player
	 *            The player.
	 * @param itemId
	 *            The hood.
	 * @param itemId2
	 *            The cape.
	 * @return if can be hooded.
	 */
	public static boolean handleHooding(Player player, int hoodId, int capeId) {
		// Skillcapes TODO product ID's (hooded capes)
		if (hoodId == 9749 && capeId == 9747 || capeId == 9747 && hoodId == 9747) {
			attachHood(player, hoodId, capeId, 34246); /** Attack **/
			return true;
		}
		if (hoodId == 9749 && capeId == 9748 || capeId == 9748 && hoodId == 9749) {
			attachHood(player, hoodId, capeId, 34247); /** Attack T **/
			return true;
		}
		if (hoodId == 9752 && capeId == 9750 || capeId == 9750 && hoodId == 9752) {
			attachHood(player, hoodId, capeId, 34248); /** Strength **/
			return true;
		}
		if (hoodId == 9752 && capeId == 9751 || capeId == 9751 && hoodId == 9752) {
			attachHood(player, hoodId, capeId, 34249); /** Strength T **/
			return true;
		}
		if (hoodId == 9755 && capeId == 9753 || capeId == 9753 && hoodId == 9755) {
			attachHood(player, hoodId, capeId, 34250); /** Defence **/
			return true;
		}
		if (hoodId == 9755 && capeId == 9754 || capeId == 9754 && hoodId == 9755) {
			attachHood(player, hoodId, capeId, 34251); /** Defence T **/
			return true;
		}
		if (hoodId == 9758 && capeId == 9756 || capeId == 9756 && hoodId == 9758) {
			attachHood(player, hoodId, capeId, 34252); /** Ranging **/
			return true;
		}
		if (hoodId == 9758 && capeId == 9757 || capeId == 9757 && hoodId == 9758) {
			attachHood(player, hoodId, capeId, 34253); /** Ranging T **/
			return true;
		}
		if (hoodId == 9761 && capeId == 9759 || capeId == 9759 && hoodId == 9761) {
			attachHood(player, hoodId, capeId, 34254); /** Prayer **/
			return true;
		}
		if (hoodId == 9761 && capeId == 9760 || capeId == 9760 && hoodId == 9761) {
			attachHood(player, hoodId, capeId, 34255); /** Prayer T **/
			return true;
		}
		if (hoodId == 9764 && capeId == 9762 || capeId == 9762 && hoodId == 9764) {
			attachHood(player, hoodId, capeId, 34256); /** Magic **/
			return true;
		}
		if (hoodId == 9764 && capeId == 9763 || capeId == 9763 && hoodId == 9764) {
			attachHood(player, hoodId, capeId, 34257); /** Magic T **/
			return true;
		}
		if (hoodId == 9767 && capeId == 9765 || capeId == 9765 && hoodId == 9767) {
			attachHood(player, hoodId, capeId, 34258); /** Runecrafting **/
			return true;
		}
		if (hoodId == 9767 && capeId == 9766 || capeId == 9766 && hoodId == 9767) {
			attachHood(player, hoodId, capeId, 34259); /** Runecrafting T **/
			return true;
		}
		if (hoodId == 9770 && capeId == 9768 || capeId == 9768 && hoodId == 9770) {
			attachHood(player, hoodId, capeId, 34262); /** Constitution **/
			return true;
		}
		if (hoodId == 9770 && capeId == 9769 || capeId == 9769 && hoodId == 9770) {
			attachHood(player, hoodId, capeId, 34263); /** Constitution T **/
			return true;
		}
		if (hoodId == 9773 && capeId == 9771 || capeId == 9771 && hoodId == 9773) {
			attachHood(player, hoodId, capeId, 34264); /** Agility **/
			return true;
		}
		if (hoodId == 9773 && capeId == 9772 || capeId == 9772 && hoodId == 9773) {
			attachHood(player, hoodId, capeId, 34265); /** Agility T **/
			return true;
		}
		if (hoodId == 9776 && capeId == 9774 || capeId == 9774 && hoodId == 9776) {
			attachHood(player, hoodId, capeId, 34266); /** Herblore **/
			return true;
		}
		if (hoodId == 9776 && capeId == 9775 || capeId == 9775 && hoodId == 9776) {
			attachHood(player, hoodId, capeId, 34267); /** Herblore T **/
			return true;
		}
		if (hoodId == 9779 && capeId == 9777 || capeId == 9777 && hoodId == 9779) {
			attachHood(player, hoodId, capeId, 34268); /** Thieving **/
			return true;
		}
		if (hoodId == 9779 && capeId == 9778 || capeId == 9778 && hoodId == 9779) {
			attachHood(player, hoodId, capeId, 34269); /** Thieving T **/
			return true;
		}
		if (hoodId == 9782 && capeId == 9780 || capeId == 9780 && hoodId == 9782) {
			attachHood(player, hoodId, capeId, 34270); /** Crafting **/
			return true;
		}
		if (hoodId == 9782 && capeId == 9781 || capeId == 9781 && hoodId == 9782) {
			attachHood(player, hoodId, capeId, 34271); /** Crafting T **/
			return true;
		}
		if (hoodId == 9785 && capeId == 9783 || capeId == 9783 && hoodId == 9785) {
			attachHood(player, hoodId, capeId, 34272); /** Fletching **/
			return true;
		}
		if (hoodId == 9785 && capeId == 9784 || capeId == 9784 && hoodId == 9785) {
			attachHood(player, hoodId, capeId, 34273); /** Fletching T **/
			return true;
		}
		if (hoodId == 9788 && capeId == 9786 || capeId == 9786 && hoodId == 9788) {
			attachHood(player, hoodId, capeId, 34274); /** Slayer **/
			return true;
		}
		if (hoodId == 9788 && capeId == 9787 || capeId == 9787 && hoodId == 9788) {
			attachHood(player, hoodId, capeId, 34275); /** Slayer T **/
			return true;
		}
		if (hoodId == 9791 && capeId == 9789 || capeId == 9789 && hoodId == 9791) {
			attachHood(player, hoodId, capeId, 34276); /** Construction **/
			return true;
		}
		if (hoodId == 9791 && capeId == 9790 || capeId == 9790 && hoodId == 9791) {
			attachHood(player, hoodId, capeId, 34277); /** Construction T **/
			return true;
		}
		if (hoodId == 9794 && capeId == 9792 || capeId == 9792 && hoodId == 9794) {
			attachHood(player, hoodId, capeId, 34278); /** Mining **/
			return true;
		}
		if (hoodId == 9794 && capeId == 9793 || capeId == 9793 && hoodId == 9794) {
			attachHood(player, hoodId, capeId, 34279); /** Mining T **/
			return true;
		}
		if (hoodId == 9797 && capeId == 9795 || capeId == 9795 && hoodId == 9797) {
			attachHood(player, hoodId, capeId, 34280); /** Smithing **/
			return true;
		}
		if (hoodId == 9797 && capeId == 9796 || capeId == 9796 && hoodId == 9797) {
			attachHood(player, hoodId, capeId, 34281); /** Smithing T **/
			return true;
		}
		if (hoodId == 9800 && capeId == 9798 || capeId == 9798 && hoodId == 9800) {
			attachHood(player, hoodId, capeId, 34282); /** Fishing **/
			return true;
		}
		if (hoodId == 9800 && capeId == 9799 || capeId == 9799 && hoodId == 9800) {
			attachHood(player, hoodId, capeId, 34283); /** Fishing T */
			return true;
		}
		if (hoodId == 9803 && capeId == 9801 || capeId == 9801 && hoodId == 9803) {
			attachHood(player, hoodId, capeId, 34284); /** Cooking */
			return true;
		}
		if (hoodId == 9803 && capeId == 9802 || capeId == 9802 && hoodId == 9803) {
			attachHood(player, hoodId, capeId, 34285); /** Cooking T */
			return true;
		}
		if (hoodId == 9806 && capeId == 9804 || capeId == 9804 && hoodId == 9806) {
			attachHood(player, hoodId, capeId, 34286); /** Firemaking */
			return true;
		}
		if (hoodId == 9806 && capeId == 9805 || capeId == 9805 && hoodId == 9806) {
			attachHood(player, hoodId, capeId, 34287); /** Firemaking T */
			return true;
		}
		if (hoodId == 9809 && capeId == 9807 || capeId == 9807 && hoodId == 9809) {
			attachHood(player, hoodId, capeId, 34288); /** Woodcutting */
			return true;
		}
		if (hoodId == 9809 && capeId == 9808 || capeId == 9808 && hoodId == 9809) {
			attachHood(player, hoodId, capeId, 34289); /** Woodcutting T */
			return true;
		}
		if (hoodId == 9812 && capeId == 9810 || capeId == 9810 && hoodId == 9812) {
			attachHood(player, hoodId, capeId, 34290); /** Farming */
			return true;
		}
		if (hoodId == 9812 && capeId == 9811 || capeId == 9811 && hoodId == 9812) {
			attachHood(player, hoodId, capeId, 34291); /** Farming T */
			return true;
		}
		if (hoodId == 9950 && capeId == 9948 || capeId == 9948 && hoodId == 9950) {
			attachHood(player, hoodId, capeId, 34260); /** Hunter */
			return true;
		}
		if (hoodId == 9950 && capeId == 9949 || capeId == 9949 && hoodId == 9950) {
			attachHood(player, hoodId, capeId, 34261); /** Hunter T */
			return true;
		}
		if (hoodId == 12171 && capeId == 12169 || capeId == 12169 && hoodId == 12171) {
			attachHood(player, hoodId, capeId, 34296); /** Summoning */
			return true;
		}
		if (hoodId == 12171 && capeId == 12170 || capeId == 12170 && hoodId == 12171) {
			attachHood(player, hoodId, capeId, 34297); /** Summoning T */
			return true;
		}
		if (hoodId == 18510 && capeId == 18508 || capeId == 18508 && hoodId == 18510) {
			attachHood(player, hoodId, capeId, 34294); /** Dungeoneering */
			return true;
		}
		if (hoodId == 18510 && capeId == 18509 || capeId == 18509 && hoodId == 18510) {
			attachHood(player, hoodId, capeId, 34295); /** Dungeoneering T */
			return true;
		}
		if (hoodId == 20751 && capeId == 20767 || capeId == 20767 && hoodId == 20751) {
			attachHood(player, hoodId, capeId, 32151); /** Max cape */
			return true;
		}
		if (hoodId == 20770 && capeId == 20769 || capeId == 20769 && hoodId == 20770) {
			attachHood(player, hoodId, capeId, 32152); /** Completionist cape */
			return true;
		}
		if (hoodId == 20772 && capeId == 20771 || capeId == 20771 && hoodId == 20772) {
			attachHood(player, hoodId, capeId,
					32153); /** Completionist cape T */
			return true;
		}
		if (hoodId == 29187 && capeId == 29185 || capeId == 29185 && hoodId == 29187) {
			attachHood(player, hoodId, capeId, 34292); /** Divination */
			return true;
		}
		if (hoodId == 29187 && capeId == 29186 || capeId == 29186 && hoodId == 29187) {
			attachHood(player, hoodId, capeId, 34293); /** Divination T */
			return true;
		}
		if (hoodId == 36355 && capeId == 36351 || capeId == 36351 && hoodId == 36355) {
			attachHood(player, hoodId, capeId, 36353); /** Invention */
			return true;
		}
		if (hoodId == 36355 && capeId == 36352 || capeId == 36352 && hoodId == 36355) {
			attachHood(player, hoodId, capeId, 36354); /** Invention T */
			return true;
		}
		return false;
	}

	/**
	 * Hoods the cape.
	 *
	 * @param player
	 *            The player.
	 * @param hoodId
	 *            The Hood item ID.
	 * @param capeId
	 *            The Cape item ID.
	 * @param productId
	 *            The Hooded cape item ID.
	 */
	private static void attachHood(Player player, int itemId, int itemId2, int productId) {
		player.getInventory().deleteItem(itemId, 1);
		player.getInventory().deleteItem(itemId2, 1);
		player.getInventory().addItem(productId, 1);
		player.sendMessage("You've successfully hooded your cape.");
	}
}