package com.rs.tools;

import com.rs.cache.Cache;
import com.rs.game.player.Player;
import com.rs.utils.Logger;
import com.rs.utils.SerializableFilesManager;

import java.io.File;
import java.io.IOException;

public class CharacterFixer {

	// For individual item remover.
	// private static String[] ITEMS = { "knowledge" };

	private static long playersReset, playersResetFail;

	/**
	 * Runs the Tool. #CAUTION
	 *
	 * @param args
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public static void main(String[] args) throws ClassNotFoundException, IOException {
		Cache.init();
		File[] chars = new File("data/playersaves/characters/tofix").listFiles();
		for (File acc : chars) {
			try {
				Player player = (Player) SerializableFilesManager.loadSerializedFile(acc);

				// Full eco reset
				/**
				 * for (int i = 0; i < 34235; i++) {
				 * player.getBank().removeItem(i); int[] BankSlot =
				 * player.getBank().getItemSlot(i);
				 * 
				 * player.getBank().removeItem(BankSlot, Integer.MAX_VALUE,
				 * false, true); if (player.getBank().bankTabs != null) { for
				 * (int i1 = 0; i < player.getBank().bankTabs.length; i++) { for
				 * (int i2 = 0; i2 < player.getBank().bankTabs[i].length; i2++)
				 * { player.getBank().bankTabs[i1][i2].setId(0);
				 * player.getBank().bankTabs[i1][i2].setAmount(0);
				 * player.getBank().bankTabs = new Item[1][0]; } } } } for (int
				 * i = 0; i < 34235; i++)
				 * player.getInventory().getItems().removeAll(new Item(i,
				 * Integer.MAX_VALUE));
				 * 
				 * for (int i = 0; i < 34235; i++)
				 * player.getEquipment().getItems().removeAll(new Item(i,
				 * Integer.MAX_VALUE));
				 */

				// Individual item reset.
				/**
				 * for (int id = 0; id < Utils.getItemDefinitionsSize(); id++) {
				 * Item item = player.getBank().getItem(id); if (item == null)
				 * continue; String name =
				 * item.getDefinitions().getName().toLowerCase(); for (String
				 * string : ITEMS) { if (name.contains(string)) {
				 * player.getBank().removeItem(
				 * player.getBank().getItemSlot(item.getId()), item.getAmount(),
				 * false, false); } } } for (Item item :
				 * player.getInventory().getItems().getItems()) { if (item ==
				 * null) continue; if (item != null) { String name =
				 * item.getDefinitions().getName().toLowerCase(); for (String
				 * string : ITEMS) { if (name.contains(string)) {
				 * player.getInventory().getItems().remove(item); } } } } for
				 * (Item item : player.getEquipment().getItems().getItems()) {
				 * if (item == null) continue; if (item != null) { String name =
				 * item.getDefinitions().getName().toLowerCase(); for (String
				 * string : ITEMS) { if (name.contains(string)) {
				 * player.getEquipment().getItems().remove(item); } } } }
				 */

				player.treasureTrails = null;

				playersReset++;
				SerializableFilesManager.storeSerializableClass(player, acc);
			} catch (Throwable e) {
				Logger.getGlobal().error("failed: " + acc.getName(),e);
				// acc.delete();
				playersResetFail++;
			}
		}
		Logger.getGlobal().info("Done. Accounts affected: " + playersReset + "; failed: " + playersResetFail + ".");
	}
}