package com.rs.game.player.dialogue.impl;


import com.rs.game.player.content.Magic;
import com.rs.game.player.content.WorldLocation.Locations;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.Logger;

import java.util.ArrayList;
import java.util.List;

public class SentinelTeleports extends Dialogue {

	String outfit;	
	List<String> options = new ArrayList<String>();
	
	String COLOR = Colors.shade(Colors.LIME);
	String HEADER = COLOR+"Choose your destination";
	String[] baseOptions = { "Basic trees", "Willow trees", "Yew trees", "Magic trees" };

	@Override
	public void start() {
		if(!(parameters[0] instanceof String)) {
			finish();
			return;
		}
		outfit = (String) parameters[0];
		Logger.getGlobal().info(outfit);
		for(String option : baseOptions)
			options.add(option);
		if(outfit.equals("nature's"))
			options.add(Colors.GREEN+"Next page");
		sendOptionsDialogue(HEADER, options.toArray(new String[options.size()-1]));
		stage = 0;
	}
	
	@Override
	public void run(int interfaceId, int componentId) {
		switch(stage) {
		case 0:
			switch(componentId) {
			case OPTION_1:
				sendOptionsDialogue(HEADER, "West Varrock", "East Varrock");
				stage = 1;
				break;
			case OPTION_2:
				sendOptionsDialogue(COLOR+"Willow tree teleports", "Draynor Village", "Catherby", "Barbarian Outpost");
				stage = 2;
				break;
			case OPTION_3:
				sendOptionsDialogue(COLOR+"Yew tree teleports", "Seers' Village", "Edgeville", "Varrock palace", "Catherby");
				stage = 3;
				break;
			case OPTION_4:
				sendOptionsDialogue(COLOR+"Magic tree teleports", "Sorcerer's tower", "Ranging guild",  "Mage training arena");
				stage = 4;
				break;
			case OPTION_5:
				if(!outfit.equals("nature's"))
					finish();
				else {
					sendOptionsDialogue(HEADER, "Choking ivy", "Teak trees", "Mahogany trees", "Elder trees", Colors.SALMON+"Previous page");
					stage = 5;
				}
				break;
			}
			break;
			
		case 1: // basic tree teleports
			switch(componentId) {
			case OPTION_1:
				Magic.vineTeleport(player, Locations.getLocation("Varrock west wc").getTile());
				finish();
				break;
			case OPTION_2:
				Magic.vineTeleport(player, Locations.getLocation("Varrock east wc").getTile());
				finish();
				break;
			}
			break;
		case 2: // willow tree teleports
			switch(componentId) {
			case OPTION_1:
				Magic.vineTeleport(player, Locations.getLocation("Draynor willows").getTile());
				finish();
				break;
			case OPTION_2:
				Magic.vineTeleport(player, Locations.getLocation("Catherby").getTile());
				finish();
				break;
			case OPTION_3:
				Magic.vineTeleport(player, Locations.getLocation("Barbarian willows").getTile());
				finish();
				break;
			}
			break;
		case 3: // yew tree teleports
			switch(componentId) {
			case OPTION_1:
				Magic.vineTeleport(player, Locations.getLocation("Seers yews").getTile());
				finish();
				break;
			case OPTION_2:
				Magic.vineTeleport(player, Locations.getLocation("Edgeville yews").getTile());
				finish();
				break;
			case OPTION_3:
				Magic.vineTeleport(player, Locations.getLocation("Varrock yews").getTile());
				finish();
				break;
			case OPTION_4:
				Magic.vineTeleport(player, Locations.getLocation("Catherby").getTile());
				finish();
				break;
			}
			break;
		case 4: // magic tree teleports
			switch(componentId) {
			case OPTION_1:
				Magic.vineTeleport(player, Locations.getLocation("Sorcerers tower").getTile());
				finish();
				break;
			case OPTION_2:
				Magic.vineTeleport(player, Locations.getLocation("Ranging guild").getTile());
				finish();
				break;
			case OPTION_3:
				Magic.vineTeleport(player, Locations.getLocation("Mage training arena").getTile());
				break;
			}
			break;
		case 5: // extended page options
			switch(componentId) {
			case OPTION_1:
				sendOptionsDialogue(COLOR+"Choking ivy teleports", "South Varrock palace", "North Varrock palace", "Taverly ivy", "Yanille ivy", "Castle wars ivy");
				stage = 6;
				break;
			case OPTION_2:
				sendOptionsDialogue(COLOR+"Teak tree teleports", "Tai Bwo Wannai", "Ape Atoll", "South-west Castle Wars");
				stage = 7;
				break;
			case OPTION_3:
				sendOptionsDialogue(COLOR+"Mahogany tree teleports", "Tai Bwo Wannai", "Ape Atoll");
				stage = 8;
				break;
			case OPTION_4:
				//sendOptionsDialogue(COLOR+"Elder tree teleports", "", "", "");
				player.sendMessage(Colors.SALMON+"These will be implemented soon, thank you :)");
				finish();
				break;
			case OPTION_5:
				sendOptionsDialogue(HEADER, options.toArray(new String[options.size()-1]));
				stage = 0;
				break;
			}
			break;
		case 6:
			switch(componentId) {
			case OPTION_1:
				Magic.vineTeleport(player, Locations.getLocation("Varrock south ivy").getTile());
				finish();
				break;
			case OPTION_2:
				Magic.vineTeleport(player, Locations.getLocation("Varrock north ivy").getTile());
				finish();
				break;
			case OPTION_3:
				Magic.vineTeleport(player, Locations.getLocation("Taverly ivy").getTile());
				break;
			case OPTION_4:
				Magic.vineTeleport(player, Locations.getLocation("Yanille ivy").getTile());
				break;
			case OPTION_5:
				Magic.vineTeleport(player, Locations.getLocation("Castle wars ivy").getTile());
				break;
			}
			break;
		case 7:
			switch(componentId) {
			case OPTION_1:
				Magic.vineTeleport(player, Locations.getLocation("Tai bwo wannai").getTile());
				finish();
				break;
			case OPTION_2:
				Magic.vineTeleport(player, Locations.getLocation("Ape atoll teak").getTile());
				finish();
				break;
			case OPTION_3:
				Magic.vineTeleport(player, Locations.getLocation("Castle wars teak").getTile());
				break;
			}
			break;
		case 8:
			switch(componentId) {
			case OPTION_1:
				Magic.vineTeleport(player, Locations.getLocation("Tai bwo wannai").getTile());
				finish();
				break;
			case OPTION_2:
				Magic.vineTeleport(player, Locations.getLocation("Ape atoll mahogany").getTile());
				finish();
				break;
			}
			break;
		}
	}
	
	@Override
	public void finish() {}
}
