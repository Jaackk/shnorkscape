package com.rs.game.player.content.group;

import com.rs.game.World;
import com.rs.game.player.Player;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Group implements Serializable {

	private static final long serialVersionUID = -3165243136109630261L;
	
	public boolean disband;
	private final String leader;
	private final Map<String, String> team = new HashMap<String, String>();
	
	/* Wrapper for player groups */
	public Group(Player leader) {
		this.leader = leader.getUsername();
		this.team.put(leader.getUsername(), "[no role]");
	}
	
	public Group recruit(Player recruit) {
		this.team.put(recruit.getUsername(), "[no role]");
		recruit.group = this;
		GroupInterface.joinInterface(recruit);
		return this;
	}
	
	public void leave(Player player) {
		player.closeInterfaces();
		this.team.remove(player.getUsername(), this.team.get(player.getUsername()));
		player.group = null;
	}
	
	public Player getLeader() {
		return World.getPlayer(this.leader);
	}
	
	public Map<String, String> getTeam() {
		return this.team;
	}
	
	public Player getPlayer(int index) {
		int cheapi = 0;
		for(Map.Entry<String, String> entry : team.entrySet()) {
			if(cheapi == index)
				return World.getPlayer(entry.getKey());
			else
				cheapi += 1;
		}
		return null;
	}
	
	/* Alias for getting a player's role with Player object */
	public String getRole(Player player) {
		return getRole(player.getUsername());
	}
	
	public String getRole(String username) {
		return !team.containsKey(username) ? "Invalid!" : team.get(username);
	}
}
