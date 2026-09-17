/**
 * Copyrigh 2014 Imperial Development
 * <p>
 * This work is provided "AS IS" and WITHOUT WARRANTY of any kind, to
 * the utmost extent permitted by applicable law, neither express nor
 * implied; without malicious intent or gross negligence. In no event
 * may a licensor, author or contributor be held liable for indirect,
 * direct, other damage, loss, legal issues, or other issues arising
 * in any way out of dealing in the work, even if advised of the
 * possibility of such damage or existence of a defect, except proven
 * that it results out of said person's immediate fault when using the
 * work as intended. Any of the work within this project is for educational
 * purposes, and is not to be taken as a threat towards the original
 * authors of this emulator.
 * <p>
 * This work is not permitted to be sold by anyone except the legal
 * authors, or anyone else who has done excessive work in this project.
 * You have been warned.
 * <p>
 * ~ David B. Miller <chimerica@fulmination.org>
 */
package com.rs.game.player.content;

import com.rs.cache.loaders.NPCDefinitions;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.content.distinctioncape.DistinctionCape;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

import java.util.ArrayList;

public class ItemSearch {


	public static void search(Player player, String itemName, String identifier) {
		ArrayList<String> ITEMS = new ArrayList<String>();
		String name = "NULL";
		String id = "-1";

		// search prior to building the interface, why wouldn't you do this?
		if(identifier.equals("item")) {
			for (int i = 0; i < Utils.getItemDefinitionsSize(); i++) {
				Item item = new Item(i);
				if (item.getDefinitions().getName().toLowerCase().contains(itemName.toLowerCase())) {
					ITEMS.add(item.getName() + (item.getDefinitions().isNoted() ? "(noted)," : ",") + item.getId());
				}
			}
		}
		
		if(identifier.equals("npc")) {
			for(int i = 0; i < Utils.getNPCDefinitionsSize(); i++) {
				final NPCDefinitions defs = NPCDefinitions.getNPCDefinitions(i);
				if(defs == null)
					continue;
				
				if(defs.hasOption(itemName))
					ITEMS.add(defs.getName()+","+i);
			}
		}

		if (ITEMS.size() == 1) {
			if(identifier.equals("item")) {
				name = ITEMS.get(0).split(",")[0];
				id = ITEMS.get(0).split(",")[1];
				player.addItem(new Item(Integer.parseInt(id), 1));
				player.sendMessage(Colors.GREEN + "There was only one result for this item, so it was spawned for you!",
						true);
				return;
			}
		}

		player.getInterfaceManager().sendInterface(1082);
        for (int i = 0; i < Utils.getInterfaceDefinitionsComponentsSize(1082); i++) {
            player.getPackets().sendIComponentText(1082, i, "");
        }
		player.getPackets().sendExecuteScript(8420, 70910101, 70910103, 70910102, 70910104, Colors.CYAN + "<shad=000000>Item Search Results", 21218, 1007);

		player.getPackets().sendIComponentText(1082, 22, "Item Name");
		player.getPackets().sendIComponentText(1082, 23, "ItemId");
        for (int i = 0; i < DistinctionCape.LINES.length; i++) { 
            if(i >= ITEMS.size())
                continue;
            name = ITEMS.get(i).split(",")[0];
            id = ITEMS.get(i).split(",")[1];
            player.getPackets().sendIComponentText(1082, DistinctionCape.LINES[i][0], Utils.formatPlayerNameForDisplay(name));
            player.getPackets().sendIComponentText(1082, DistinctionCape.LINES[i][1], id);
        }
        player.getPackets().sendIComponentText(1082, 1, "Found " + ITEMS.size() + " results for the item "
                + Utils.formatPlayerNameForDisplay(itemName) + (ITEMS.size() > DistinctionCape.LINES.length ? ", Couldn't show all of results." :  "."));
	}

}
