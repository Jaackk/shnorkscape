package com.rs.game.player.content.dropprediction;

import com.rs.Settings;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.game.player.Player;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.npcs.NPCDropsDataParser;
import com.rs.utils.data.parsers.npcs.pojos.NPCDrop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Kris
 * Improved drop rates displaying + formulas.
 */
public final class DropUtils {

	public static final void sendNPCDrops(final Player player, final String name) {
		for (int i = 0; i < Utils.getNPCDefinitionsSize(); i++) {
			NPCDefinitions def = NPCDefinitions.getNPCDefinitions(i);
			if (def.getName().toLowerCase().equalsIgnoreCase(name) && NPCDropsDataParser.getDrops(i) != null && NPCDropsDataParser.getDrops(i).length != 0) {
				player.stopAll();
				sendNPCDrops(player, i);
				return;
			}
		}
		player.sendMessage("Could not find any NPC by the name of '" + name + "'.");
	}

	private static final void sendNPCDrops(final Player player, final int id) {
		final NPCDrop[] drops = NPCDropsDataParser.getDrops(id);
		final List<NPCDrop> dropList = new ArrayList<NPCDrop>();
		player.getPackets().sendIComponentText(1245, 330, "<col=9900FF><shad=000000>" + NPCDefinitions.getNPCDefinitions(id).getName() + "</col></shad>");
		for (int i = 0; i < 316; i++)
			player.getPackets().sendIComponentText(1245, i, "");
		for (int i = 0; i < drops.length; i++) {
			if (i > 300)
				break;
			if (drops[i].getRate() == 100) {
				dropList.add(new NPCDrop(drops[i].getItemId(), 100, drops[i].getMinAmount(), drops[i].getMaxAmount()));
				continue;
			}
			dropList.add(new NPCDrop(drops[i].getItemId(), getPreciseDropRate(player, drops, id, drops[i].getItemId()), drops[i].getMinAmount(), drops[i].getMaxAmount()));
		}
		Collections.sort(dropList, new Comparator<NPCDrop>() {
		    @Override
		    public int compare(NPCDrop c1, NPCDrop c2) {
		        return Double.compare(c2.getRate(), c1.getRate());
		    }
		});
		player.getPackets().sendIComponentText(1245, 13, "Item:   Quantity   |   Rate");
		for (int i = 0; i < dropList.size(); i++) {
			NPCDrop drop = dropList.get(i);
			player.getPackets().sendIComponentText(1245, 14 + i, ItemDefinitions.getItemDefinitions(drop.getItemId()).getName() + ":   " + (drop.getMinAmount() == drop.getMaxAmount() ? drop.getMinAmount() : drop.getMinAmount() + "-" + drop.getMaxAmount()) + "   |   " + Utils.round(drop.getRate(), 3) + "%");
		}
		player.getPackets().sendRunScript(17037, dropList.size() + 1);
		player.getInterfaceManager().sendInterface(1245);
	}
	
	public static final void sendItemDrops(final Player player, final String name) {
		for (int i = 0; i < Utils.getItemDefinitionsSize(); i++) {
			ItemDefinitions def = ItemDefinitions.getItemDefinitions(i);
			if (def.getName().toLowerCase().equalsIgnoreCase(name)) {
				player.stopAll();
				sendItemDrops(player, i);
				return;
			}
		}
		player.sendMessage("Could not find any drops by the name of '" + name + "'.");
	}
	
	private static final void sendItemDrops(final Player player, final int id) {
		final Map<NPCDrop, String> dropList = new HashMap<NPCDrop, String>();
		loop : for (int i = 0; i < Utils.getNPCDefinitionsSize(); i++) {
			final NPCDrop[] loot = NPCDropsDataParser.getDrops(i);
			if (loot == null || loot.length == 0) 
				continue;
			for (NPCDrop drop : loot) {
				if (drop != null && drop.getItemId() == id) {
					final NPCDefinitions defs = NPCDefinitions.getNPCDefinitions(i);
					if(defs == null)
						continue;
					if (dropList.containsValue(defs.getName() + " (level: " + defs.combatLevel + ")"))
						continue;
					if (defs.combatLevel == 0)
						continue;
					if (defs.name.equals("null"))
						continue;
					dropList.put(new NPCDrop(id, getPreciseDropRate(player, loot, i, id), drop.getMinAmount(), drop.getMaxAmount()), defs.getName() + " (level: " + defs.combatLevel + ")");
					if (dropList.size() > 300)
						break loop;
				}
			}
		}
		List<Entry<NPCDrop, String>> listOfEntries = new ArrayList<Entry<NPCDrop, String>>(dropList.entrySet());
		Comparator<Entry<NPCDrop, String>> comparator = new Comparator<Entry<NPCDrop, String>>() {
			@Override 
			public int compare(Entry<NPCDrop, String> e1, Entry<NPCDrop, String> e2) {
				return Double.compare(e2.getKey().getRate(), e1.getKey().getRate());
			}
		};
		Collections.sort(listOfEntries, comparator);
		player.getPackets().sendIComponentText(1245, 330, "<col=9900FF><shad=000000>" + ItemDefinitions.getItemDefinitions(id).getName() + "</col></shad>");
		for (int i = 0; i < 316; i++)
			player.getPackets().sendIComponentText(1245, i, "");
		player.getPackets().sendIComponentText(1245, 13, "NPC:   Quantity   |   Rate");
		for (int i = 0; i < listOfEntries.size(); i++) {
			Entry<NPCDrop, String> drop = listOfEntries.get(i);
			player.getPackets().sendIComponentText(1245, 14 + i, drop.getValue() + ":   " + (drop.getKey().getMinAmount() == drop.getKey().getMaxAmount() ? drop.getKey().getMinAmount() : drop.getKey().getMinAmount() + "-" + drop.getKey().getMaxAmount()) + "   |   " + Utils.round(drop.getKey().getRate(), 3) + "%");
		}
		if (listOfEntries.size() > 11)
			player.getPackets().sendRunScript(17037, (listOfEntries.size() ) > 300 ? 300 : listOfEntries.size() + 1);
		player.getInterfaceManager().sendInterface(1245);
	}

	private static final double getPreciseDropRate(final Player player, final NPCDrop[] drops, final int npcId, final int dropId) {
		final double encodedRate = getEncodedDropRate(player, drops, npcId, dropId);
		if (encodedRate == 0)
			return 0;
		int totalRate = 0;
		for (NPCDrop drop : drops) {
			if (drop.getRate() == 100)
				continue;
			totalRate += drop.getRate();
		}
		/**
		 * at *100 the drop rates would all be slightly over it. 
		 * rate * 97 seems to result in the most promising data, although imperfect, 
		 * still best. This is due to being unable to calculate absolute randomness. 
		 * CBF explaining why that is.
		 */
		return encodedRate * 97 / totalRate;	
	}

	private static final double getEncodedDropRate(final Player player, final NPCDrop[] drops, final int npcId, final int dropId) {
		if (drops == null)
			return 0;
		for (NPCDrop drop : drops) {
			if (drop.getItemId() == dropId) {
				double rate = player.getHeart().getDropRate(npcId, drop);
				if (rate < 30)
					rate *= Settings.getDropQuantityRate(player);
				return rate;
			}
		}
		return 0;
	}

}
