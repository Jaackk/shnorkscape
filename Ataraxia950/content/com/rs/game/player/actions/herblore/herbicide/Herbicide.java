package com.rs.game.player.actions.herblore.herbicide;

import java.util.Arrays;

import com.rs.cache.loaders.ClientScriptMap;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.InterfaceManager;

public class Herbicide {
	
	private static final String[] HERB_NAMES = new String[] {"Guam leave", "Marrentill", "Tarromin", "Harralander", "Ranarr", "Toadflax", "Spirit weed", "Irit", "Wergali", "Avantoe", 
			"Kwuarm", "Snapdragon", "Cadantine", "Lantadyme", "Dwarf weed", "Fellstalk", "Torstol"};
	
	private static final int[] HERBS = new int[] { 249, 251, 253, 255, 257, 2998, 12172, 259, 14854, 261, 263, 3000, 265, 2481, 267, 21624, 269 };

	public static void openHerbicide(Player player) {
	    refreshSettings(player);
	       for (int i = 0; i < player.herbicideSettings.length; i++) {
	            if (i == 16)
	                player.getPackets().sendGlobalConfig(1605, ItemDefinitions.getItemDefinitions(HERBS[15]).getValue());
	            else 
	                player.getPackets().sendGlobalConfig(1336 + i, ItemDefinitions.getItemDefinitions(HERBS[i]).getValue());
	        }
		player.getInterfaceManager().sendCentralOverlayInterface(1006);
	}
	
	public static void handleHerbicide(Player player, int componentId) {
	    if (componentId == 67) {
	        boolean[] allTrue = new boolean[player.herbicideSettings.length];
	        Arrays.fill(allTrue, true);
	        boolean disableAll = Arrays.equals(player.herbicideSettings, allTrue);
	        Arrays.fill(player.herbicideSettings, !disableAll);
	        refreshSettings(player);
	    } else if (componentId == 75) {
            player.getInterfaceManager().removeCentralOverlayInterface();
        } else if (componentId >= 9 && componentId <= 147) {
			int id = (int) ClientScriptMap.getMap(13739).getKeyForValue(InterfaceManager.getComponentUId(1006, componentId + 1));
			player.herbicideSettings[id] = !player.herbicideSettings[id];
			player.sendMessage(HERB_NAMES[id] + (player.herbicideSettings[id] ? "s are now being burnt for 2x cleaning experience." : "s are no longer being burnt for 2x cleaning experience."));
			refreshSettings(player);
		}
	}
	
	public static boolean handleDrop(Player player, Item item) {
		int i = 0;
		for (HerbicideSettings settings : HerbicideSettings.values()) {
			if (settings.isHerb(item.getId()) && player.herbicideSettings[i]) {
				player.getSkills().addXp(Skills.HERBLORE, settings.getExperience() * item.getAmount());
				player.getPackets().sendGameMessage("The herbicide instantly incinerates the" + item.getName().replaceAll("Grimy", "").replaceAll("Clean", "") + ".", true);
				return true;
			}
			i++;
		}
		return false;
	}
	
	public static void refreshSettings(Player player) {
	    int v = 0;
	    for(int i = 0; i < player.herbicideSettings.length; i++)
	        v |= (player.herbicideSettings[i] ? 1 : 0) << i;
	    player.getPackets().sendConfig(1086, v);
	}
	
}
