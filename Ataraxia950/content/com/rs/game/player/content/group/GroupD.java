package com.rs.game.player.content.group;

import com.rs.game.World;
import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

import java.util.Map;

public class GroupD extends Dialogue {

	@Override
	public void start() {
		sendOptionsDialogue("Please select an option",
				"Create a group", "Manage your group",
				"Leave current group", "Nevermind");
		stage = 0;
	}

	@Override
	public void run(int interfaceId, int componentId) {
		switch(stage) {
		case 0:
			finish();
			switch(componentId) {
			case OPTION_1: // create a group option
				if(player.getGroup() == null) {
					player.group = new Group(player);
					GroupInterface.joinInterface(player);
				} else
					player.sendMessage(Colors.RED+"You are already in a group!");
				break;
				
			case OPTION_2: // manage group option
				if(player.getGroup() == null) {
					player.group = new Group(player);
					GroupInterface.joinInterface(player);
				} else {
					if(!player.getGroup().getRole(player).contains(Colors.GREEN))
						GroupInterface.joinInterface(player);
					else
						GroupInterface.recruitInterface(player);
				}
				break;
				
			case OPTION_3: // leave group option
				if(player.getGroup() != null) {
					if(player.getGroup().getLeader() == player) {
						for(Map.Entry<String, String> entry : player.getGroup().getTeam().entrySet()) {
							Player current = World.getPlayer(entry.getKey());
							if(current == null)
								continue;
							if(!player.getUsername().equals(player.getGroup().getLeader().getUsername()))
								current.sendMessage(Colors.GOLD+Colors.SHAD+
										"Your group leader has disbanded the group!");
							current.getGroup().leave(current);
						}
					} else {
						player.getGroup().leave(player);
						player.sendMessage(Colors.GOLD+Colors.SHAD+"You have left your group!");
					}
				} else
					player.sendMessage(Colors.RED+"You are not currently in a group!");
				break;
				
			case OPTION_4: // never mind
				break;
			}
			break;
		}
	}

	@Override
	public void finish() { 
		player.getInterfaceManager().closeChatBoxInterface();
	}

	
	
}
