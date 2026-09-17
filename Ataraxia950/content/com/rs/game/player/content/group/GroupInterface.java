package com.rs.game.player.content.group;

import com.rs.game.World;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

import java.util.Map;

public class GroupInterface {
	private static final int RECRUIT = 492;
	private static final int JOIN = 493;
	private static final int OVERLAY = 256;
	private static final String[] modes = { "Melee", "Ranged", "Magic", "Tank" };
	
	public static void groupOverlay(Player player) {
		int[] hide = { 10, 15 };
		for(int component : hide)
			player.getPackets().sendHideIComponent(OVERLAY, component, true);
		player.getInterfaceManager().sendOverlay(OVERLAY, false);
		groupRefresh(player);
	}
	
	public static void groupRefresh(Player player) {
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				player.getPackets().sendIComponentText(OVERLAY, 6,
						Utils.formatPlayerNameForDisplay(
						player.getGroup().getLeader().getUsername()));
				
				for(Map.Entry<String, String> entry : player.getGroup().getTeam().entrySet()) {
					Player current = World.getPlayer(entry.getKey());
					if(current == null ||
							entry.getKey().equals(player.getGroup().getLeader().getUsername()))
						continue;
					for(int i=0; i < 3; i++) {
						player.getPackets().sendText(OVERLAY, 7+i, 
							(player == current ? Colors.WHITE : "")+
							Utils.formatPlayerNameForDisplay(entry.getKey()));
					}
				}
			}
			
		});
	}
	
	public static void recruitInterface(Player player) {
		player.closeInterfaces();
		int[] hide = { 1, 25, 26, 31, 32, 33, 34, 5, 6, 14, 19, 24, 29 };
		for(int component : hide)
			player.getPackets().sendHideIComponent(RECRUIT, component, true);
		player.getPackets().sendIComponentText(RECRUIT, 2, "Disband");
		player.getPackets().sendIComponentText(RECRUIT, 4, "Close");
		player.getPackets().sendIComponentText(RECRUIT, 7, "Group recruitment interface");
		player.getPackets().sendIComponentText(RECRUIT, 45, "Disband group?");
		player.getPackets().sendIComponentText(RECRUIT, 46,
				Colors.WHITE+"( hit x to disband )");
		player.getInterfaceManager().sendInterface(RECRUIT);
		if(player.getUsername().equals(player.getGroup().getLeader()))
			player.getGroup().disband = false;
		recruitRefresh(player);
	}
	
	public static void recruitRefresh(Player player) {
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				
				player.getPackets().sendIComponentText(RECRUIT, 15, Colors.GOLD+
						Utils.formatPlayerNameForDisplay(player.getGroup().getLeader().getUsername()));
				player.getPackets().sendIComponentText(RECRUIT, 20,
						player.getGroup().getTeam().get(player.getGroup().getLeader().getUsername()));
				
				int cheapi = 0;
				for(Map.Entry<String, String> entry : player.getGroup().getTeam().entrySet()) {
					Player current = World.getPlayerByDisplayName(entry.getKey());
					if (current == null ||
							entry.getKey().equals(player.getGroup().getLeader().getUsername()))
						continue;
					player.getPackets().sendIComponentText(RECRUIT, 16+cheapi,
						(current == player ? Colors.WHITE : "")+Utils.
						formatPlayerNameForDisplay(entry.getKey()));
					player.getPackets().sendIComponentText(RECRUIT, 21+cheapi,
							entry.getValue());
					cheapi++;
				}
			}
		}, 1);
	}
	
	public static void recruitButtons(Player player, int component) {
		if(player.getGroup().disband && component != 4 && component != 35) {
			player.getPackets().sendIComponentText(RECRUIT, 2, "Disband");
			player.getPackets().sendIComponentText(RECRUIT, 4, "Close");
			player.getPackets().sendIComponentText(RECRUIT, 45,
					"Disband group?");
			player.getPackets().sendIComponentText(RECRUIT, 46,
					Colors.WHITE+"( hit x to disband )");
		}
		
		switch(component) {
		case 47: // kick 1st person
			Player kicked = player.getGroup().getPlayer(0);
			if(kicked == null) {
				player.sendMessage(Colors.SALMON+"There is not a player in this position!");
				break;
			}
			if(player.getUsername().equals(player.getGroup().getLeader().getUsername())) {
				if(player.getGroup().getTeam().size() > 1) {
					player.sendMessage(Colors.GREEN+"You have removed "+Utils.
						formatPlayerNameForDisplay(kicked.getUsername())+" from your group!");
					kicked.sendMessage(Colors.SALMON+"You have been kicked from your current group!");
					kicked.getInterfaceManager().closeScreenInterface();
					player.getGroup().leave(kicked);
					break;
				}
			}		
			break;
			
		case 27: // kick 2nd person
			kicked = player.getGroup().getPlayer(2);
			if(kicked == null) {
				player.sendMessage(Colors.SALMON+"There is not a player in this position!");
				break;
			}
			if(player.getUsername().equals(player.getGroup().getLeader().getUsername())) {
				if(player.getGroup().getTeam().size() > 2) {
					player.sendMessage(Colors.GREEN+"You have removed "+Utils.
						formatPlayerNameForDisplay(kicked.getUsername())+" from your group!");
					kicked.sendMessage(Colors.SALMON+"You have been kicked from your current group!");
					kicked.getInterfaceManager().closeScreenInterface();
					player.getGroup().leave(kicked);
					break;
				}
			}		
			break;
			
		case 28: // kick 3rd person
			kicked = player.getGroup().getPlayer(3);
			if(kicked == null) {
				player.sendMessage(Colors.SALMON+"There is not a player in this position!");
				break;
			}
			if(player.getUsername().equals(player.getGroup().getLeader().getUsername())) {
				if(player.getGroup().getTeam().size() > 3) {
					player.sendMessage(Colors.GREEN+"You have removed "+Utils.
						formatPlayerNameForDisplay(kicked.getUsername())+" from your group!");
					kicked.sendMessage(Colors.SALMON+"You have been kicked from your current group!");
					kicked.getInterfaceManager().closeScreenInterface();
					player.getGroup().leave(kicked);
					break;
				}
			}		
			break;
			
		case 35: 
		case 4: // accept / close
			player.getInterfaceManager().closeScreenInterface();
			if(!player.getUsername().equals(player.getGroup().getLeader().getUsername()))
				return;
			if(player.getGroup().disband) {
				for(Map.Entry<String, String> entry : player.getGroup().getTeam().entrySet()) {
					Player current = World.getPlayer(entry.getKey());
					if(current == null)
						continue;
					if(!player.getUsername().equals(player.getGroup().getLeader().getUsername()))
						current.sendMessage(Colors.GOLD+Colors.SHAD+
								"Your group leader has disbanded the group!");
					current.getGroup().leave(current);
				}
				player.sendMessage(Colors.GOLD+Colors.SHAD+"You have disbanded"
						+ " your group! You are free to start another one!");
			}
			break;
			
		case 2:
		case 3: // disband
			if(!player.getUsername().equals(player.getGroup().getLeader().getUsername())) {
				player.sendMessage(Colors.RED+"Only the leader can disband the group!");
				return;
			}
			if(!player.getGroup().disband) {
				player.getGroup().disband = true;
				player.getPackets().sendIComponentText(RECRUIT, 45,
						Colors.RED+"Are you sure?");
				player.getPackets().sendIComponentText(RECRUIT, 46,
						Colors.WHITE+"( confirm to disband )");
				player.getPackets().sendIComponentText(RECRUIT, 2, "Cancel");
				player.getPackets().sendIComponentText(RECRUIT, 4, "Confirm");
			} else
				player.getGroup().disband = false;
		}
		if(player.getGroup() == null) {
			player.getInterfaceManager().closeScreenInterface();
			return;
		}
		if(!player.getGroup().disband && component != 4 && component != 35)
			recruitRefresh(player);
	}
	
	public static void joinInterface(Player player) {
		player.closeInterfaces();
		int[] hide = { 22, 37, 38, 39, 40, 41, 42, 5, 50, 51, 31, 36, 21};
		
		for(int component : hide)
			player.getPackets().sendHideIComponent(JOIN, component, true);
		player.getPackets().sendIComponentText(JOIN, 23, "Name: ");
		player.getPackets().sendIComponentText(JOIN, 24, "Role: ");
		player.getPackets().sendIComponentText(JOIN, 18, 
				player.getGroup().getLeader().getUsername()
				== player.getUsername() ? "Create a group?" : "Join "
				+Colors.GOLD+Utils.formatPlayerNameForDisplay(
						player.getGroup().getLeader().getUsername())
				+"</col>'s group?");
		
		for(int i = 0; i < 4; i++) {
			player.getPackets().sendIComponentText(JOIN, 44+(i*2), "Name: ");
			player.getPackets().sendIComponentText(JOIN, 45+(i*2), "Role: ");
		}
		player.getInterfaceManager().sendInterface(JOIN);
		joinRefresh(player);
	}
	
	public static void joinRefresh(Player player) {
		int[][] items = { { 7, 4151 }, { 9, 861  }, { 12, 4675 }, {13, 23351 } };
		int[] modes1 = { 8, 10, 11, 14 };
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				//users = 27, 32 +1 each time
				player.getPackets().sendIComponentText(JOIN, 43, "Current role: "
						+player.getGroup().getTeam().get(player));
				
				player.getPackets().sendIComponentText(JOIN, 27, Colors.GOLD+
						Utils.formatPlayerNameForDisplay(player.getGroup().getLeader().getUsername()));
				player.getPackets().sendIComponentText(JOIN, 32, 
						player.getGroup().getTeam().get(player.getGroup().getLeader().getUsername()));
				
				int cheapi = 0;
				for(Map.Entry<String, String> entry : player.getGroup().getTeam().entrySet()) {
					Player current = World.getPlayer(entry.getKey());
					if(current == null || entry.getKey().equals(player.getGroup().getLeader().getUsername()))
						continue;
					player.getPackets().sendIComponentText(JOIN, 28+cheapi,
						(current == player ? Colors.WHITE : "")+Utils.
						formatPlayerNameForDisplay(entry.getKey()));
					player.getPackets().sendIComponentText(JOIN, 33+cheapi,
							entry.getValue());
					cheapi++;
				}
					
				for(int i = 0; i < items.length; i++) {
					player.getPackets().sendIComponentText(JOIN, modes1[i], modes[i]);
					player.getPackets().sendItemOnIComponent(JOIN, items[i][0], items[i][1], 1);
				}
			}
		}, 0);
	}
	
	public static void joinButtons(Player player, int component) {
		switch(component) {
		case 7:
		case 8: //melee switch
			if(!player.getGroup().getTeam().get(player.getUsername()).contains(Colors.GREEN))
				player.getGroup().getTeam().put(player.getUsername(), Colors.SALMON+modes[0]);
			else
				player.sendMessage(Colors.RED+"You cannot change your role until"
							+" the leader approves or declines it.");
			break;
			
		case 9:
		case 10: //range switch
			if(!player.getGroup().getTeam().get(player.getUsername()).contains(Colors.GREEN))
				player.getGroup().getTeam().put(player.getUsername(), Colors.SALMON+modes[1]);
			else
				player.sendMessage(Colors.RED+"You cannot change your role until"
							+" the leader approves or declines it.");
			break;
			
		case 11:
		case 12: //mage switch
			if(!player.getGroup().getTeam().get(player.getUsername()).contains(Colors.GREEN))
				player.getGroup().getTeam().put(player.getUsername(), Colors.SALMON+modes[2]);
			else
				player.sendMessage(Colors.RED+"You cannot change your role until"
							+" the leader approves or declines it.");
			break;
			
		case 13:
		case 14: //tank switch
			if(!player.getGroup().getTeam().get(player.getUsername()).contains(Colors.GREEN))
				player.getGroup().getTeam().put(player.getUsername(), Colors.SALMON+modes[3]);
			else
				player.sendMessage(Colors.RED+"You cannot change your role until"
							+" the leader approves or declines it.");
			break;
		
		case 15:
		case 16: //accept button
			if(player.getGroup().getTeam().get(player.getUsername()).equals("[no role]")) {
				player.sendMessage(Colors.RED+"You don't have a role set!");
				break;
			}
			player.getGroup().getTeam().put(player.getUsername(),
					player.getGroup().getTeam().get(player.getUsername()).replaceAll(Colors.SALMON, Colors.GREEN));
			player.getInterfaceManager().closeScreenInterface();
			if(player.getUsername().equals(player.getGroup().getLeader().getUsername()))	
				recruitInterface(player);
			break;
		
		case 6:
		case 17: //decline button
			if(player.getUsername().equals(player.getGroup().getLeader().getUsername())) {
				if(player.getGroup().getTeam().size() <= 1) {
					player.getGroup().getTeam().remove(player.getUsername(),
							player.getGroup().getTeam().get(player.getUsername()));
					player.getGroup().leave(player);
					player.sendMessage(Colors.RED+"Your group was empty and it has been disbanded.");
				}
			} else
				player.getGroup().leave(player);
			player.getInterfaceManager().closeScreenInterface();
			break;
		}
		if(component != 6 && component != 17)
			joinRefresh(player);
	}
}
