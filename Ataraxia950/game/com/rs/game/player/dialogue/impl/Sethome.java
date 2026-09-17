package com.rs.game.player.dialogue.impl;

import com.rs.game.WorldTile;
import com.rs.game.player.dialogue.Dialogue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Noele
 * Written as a release / improval of an "interchangeable homes" show-off thread    
 *
 */
public class Sethome extends Dialogue {
	
	int page = 0; 
	int next = OPTION_5;
	
	@Override
	public void start() {
		displayPage(true); 
		stage = 0;
	}
	
	@Override
	public void run(int interfaceId, int componentId) {
		switch(stage) {
		case 0:
			if(componentId == next)
				displayPage(false); 
			else
				handleChoice(componentId); 
			break;
			
		case 1: 
			displayPage(true); 
			stage = 0; 
			break;
		}
	}
	
	@Override
	public void finish() { 
		player.getInterfaceManager().closeChatBoxInterface();
	}
	
	public void displayPage(boolean start) {
		List<String> options = new ArrayList<String>();
		
		if(!start)
			page += 1;
		
		if(!HomeLocation.checkIndex(4*page))
			page = 0;
		
		for(int i=0; i < 5; i++) {
			options.add(i, HomeLocation.checkIndex(i+(4*page)) ?
					(i == 4 ? "Next page" : HomeLocation.getHome(i+(4*page)).getName()) : "First page");

			if(options.get(i).equals("First page")) {
				if(page == 0)
					options.remove(i);
				else	
					next = i == 0 ? OPTION_1 : i+12;
				break;
			}
			
			if(options.get(i).equals("Next page")) {
				if(i == 4 && !HomeLocation.checkIndex(i+(4*page)) || !HomeLocation.checkIndex((4*(page+1))))
					options.remove(i);
				else
					next = i == 0 ? OPTION_1 : i+12;
				break;
			}
		}
		sendOptionsDialogue("Choose a home to set", options.toArray(new String[options.size()-1]));
	}
	
	public void handleChoice(int component) {
		component = component == OPTION_1 ? 0 : component-12;
		if(HomeLocation.checkIndex(component+(4*page))) {
			HomeLocation home = HomeLocation.getHome(component+(4*page));
			finish();
			if(home.getName().equals("Members' zone") && !player.isDonator()) {
				player.sendMessage("You must be a donator to set your home to this area!");
				return;
			}
			player.setHome(home.getTile(), home.getName());
			player.sendMessage("You have set your home area to: "+home.getName()+"!");
		} else {
			sendDialogue("There is not a home in this spot, suggest something!");
			stage = 1;
		}
	}
	
	public enum HomeLocation {
		EDGEVILLE("Edgeville", new WorldTile(3087, 3496, 0)),
		KARAMJA("Karamja", new WorldTile(2908, 3152, 0)),
		DRAYNOR("Draynor", new WorldTile(3105, 3251, 0)),
		ALKHARID("Al Kharid", new WorldTile(3274, 3165,0)),
		LUMBY("Lumbridge", new WorldTile(3222, 3218, 0)),
		VARROCK("Varrock", new WorldTile(3217, 3426, 0)),
		FALADOR("Falador", new WorldTile(2965, 3379, 0)),
		CAMELOT("Camelot", new WorldTile(2758, 3478, 0)),
		ARDOUGNE("Ardougne", new WorldTile(2660, 3306, 0));		
		
		private static final LinkedHashMap<String, HomeLocation> homes = new LinkedHashMap<String, HomeLocation>();
	
		static {
			for (HomeLocation location : HomeLocation.values())
				homes.put(location.getName(), location);
		}

		private final String name; // name attribute of HomeLocation, the home name
		private final WorldTile tile; // tile attribute of HomeLocation, the home base tile

		HomeLocation(String name, WorldTile tile) {
			this.name = name;
			this.tile = tile;
		}
		
		public static HomeLocation getHome(int index) {
			return checkIndex(index) ? homes.get(homes.keySet().toArray()[index]) : homes.get("Default");
		}

		public static boolean checkIndex(int index) {
			return index < homes.keySet().toArray().length;
		}
		
		public String getName() {
			return this.name;
		}
		
		public WorldTile getTile() {
			return this.tile;
		}
	}
}