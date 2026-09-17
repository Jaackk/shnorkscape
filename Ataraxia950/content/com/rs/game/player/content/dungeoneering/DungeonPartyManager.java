package com.rs.game.player.content.dungeoneering;

import com.rs.Settings;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;

import java.util.concurrent.CopyOnWriteArrayList;

public final class DungeonPartyManager {

	private String leader; // username
	private int floor;
	private int complexity;
	private int size;
	private int dificulty;
	private boolean guideMode;
	private int keyType;


	private DungeonManager dungeon;
	public void setDungeon(DungeonManager dungeon) { this.dungeon = dungeon; }



	private final CopyOnWriteArrayList<Player> team;


	public DungeonPartyManager() {
		team = new CopyOnWriteArrayList<Player>();
	}

	public void setDefaults() {
		floor = 1;
		complexity = 6;
		dificulty = team.size();
		guideMode = true;
	}

	public void leaveParty(Player player, boolean logout) {
		if (dungeon != null)
			dungeon.exitDungeon(player, logout);
		else {
			if (player.isCantWalk())
				return;
			player.setForceMultiArea(false);
			player.stopAll();
			remove(player, logout);
		}
		player.setMapSize(Settings.DEFAULT_MAP_SIZE);
	}

	public void remove(Player player, boolean logout) {
		synchronized (this) {
			team.remove(player);
			player.getDungeoneeringManager().setParty(null);
			player.getDungeoneeringManager().expireInvitation();
			player.getDungeoneeringManager().refreshPartyDetailsComponents();
			player.getCombatDefinitions().setSpellBook(player.getCombatDefinitions().getSpellBookBefore());
			player.getPackets().sendGameMessage("You leave the party.");
			player.getDungeoneeringManager().refreshNames();
			if (dungeon != null && team.size() == 0) {
				if (dungeon.hasLoadedNoRewardScreen() && logout) //destroy timer cant exist with a party member on anyway, team must be 0
					dungeon.setDestroyTimer();
				else
					dungeon.destroy();
			} else {
				for (Player p2 : team)
					p2.getPackets().sendGameMessage(player.getDisplayName() + " has left the party.");
				if (isLeader(player) && team.size() > 0)
					setLeader(team.get(0));
				for (Player p2 : team) 
					refreshPartyDetails(p2);
			}
		}
	}

	public void refreshPartyDetails(Player player) {
		player.getDungeoneeringManager().refreshPartyDetailsComponents();
		player.getDungeoneeringManager().refreshNames();
		player.getDungeoneeringManager().refreshPartyGuideModeComponent();
	}

	public void add(Player player) {
		synchronized (this) {
			for (Player p2 : team)
				p2.getPackets().sendGameMessage(player.getDisplayName() + " has joined the party.");
			team.add(player);
			player.getDungeoneeringManager().setParty(this);
			if (team.size() == 1) {
				setLeader(player);
				setFloor(0);
				setComplexity(0);
				if (dungeon != null)
					dungeon.endDestroyTimer();
			} else
				player.getPackets().sendGameMessage("You join the regular dungeon party.");
			for (Player p2 : team)
				refreshPartyDetails(p2);
		}
	}

	public boolean isLeader(Player player) {
		return player.getUsername().equals(leader);
	}

	public void setLeader(Player player) {
		leader = player.getUsername();
		if (team.size() > 1) {
			Player positionZero = team.get(0);
			team.set(0, player);
			team.remove(player);
			team.add(positionZero);
		}
		player.getPackets().sendGameMessage("You have been set as the party leader.");
	}

	public void lockParty() {
		for (Player player : team) {
			player.stopAll();
			player.lock();
		}
	}

	public void start() {
		synchronized (this) {
			if (dungeon != null)
				return;
			dungeon = new DungeonManager(this);
		}
	}

	public int getComplexity() {
		return complexity;
	}

	public void setComplexity(int complexity) {
		this.complexity = complexity;
		for (Player player : team)
			player.getDungeoneeringManager().refreshComplexity();
	}

	public void setSize(int size) {
		this.size = size;
	}

	public void setDifficulty(int dificulty) {
		if (dificulty > team.size())
			dificulty = team.size();
		this.dificulty = dificulty;
	}

	public int getFloor() {
		return floor;
	}

	public String getLeader() {
		return leader;
	}

	public Player getLeaderPlayer() {
		for (Player player : team)
			if (player.getUsername().equals(leader))
				return player;
		return null;
	}

	public Player getGateStonePlayer() {
		for (Player player : team)
			if (player.getInventory().containsItem(DungeonConstants.GROUP_GATESTONE, 1))
				return player;
		return null;
	}

	public void setFloor(int floor) {
		this.floor = floor;
		for (Player player : team) {
			player.getDungeoneeringManager().refreshFloor();
			if (floor >= 50 && !player.hasReachedFloor50())
				player.setHasReachedFloor50();
		}
	}

	public int getFloorType() {
		return DungeonUtils.getFloorType(floor);
	}

	public int getDungeoneeringLevel() {
		int level = 120;
		for (Player player : team) {
			int playerLevel = player.getSkills().getLevelForXp(Skills.DUNGEONEERING);
			if (playerLevel < level)
				level = playerLevel;
		}
		return level;
	}

	public double getLevelDiferencePenalty(Player player) {
		int average = getAverageCombatLevel();
		int cb = player.getSkills().getCombatLevelWithSummoning();
		double diff = Math.abs(cb - average);
		return  (diff > 50 ? ((diff - 50)*0.01) : 0);
		
	}
	
	public int getMaxLevelDiference() {
		if (team.size() <= 1)
			return 0;
		int maxLevel = 0;
		int minLevel = 138;
		for (Player player : team) {
			int level = player.getSkills().getCombatLevelWithSummoning();
			if (maxLevel < level)
				maxLevel = level;
			if (minLevel > level)
				minLevel = level;
		}
		return Math.abs(maxLevel - minLevel);
	}

	public DungeonManager getDungeon() {
		return dungeon;
	}

	public int getMaxFloor() {
		int floor = 60;
		for (Player player : team) {
			if (player.getDungeoneeringManager().getMaxFloor() < floor)
				floor = player.getDungeoneeringManager().getMaxFloor();
		}
		return floor;
	}

	public int getMaxComplexity() {
		int complexity = 6;
		for (Player player : team) {
			if (player.getDungeoneeringManager().getMaxComplexity() < complexity)
				complexity = player.getDungeoneeringManager().getMaxComplexity();
		}
		return complexity;
	}

	public int getCombatLevel() {
		int level = 0;
		for (Player player : team)
			level += player.getSkills().getCombatLevelWithSummoning();
		return team.size() == 0 ? 138 : level;
	}

	public int getAverageCombatLevel() {
		if (team.size() == 0)
			return 138;
		int level = 0;
		for (Player player : team)
			level += player.getSkills().getCombatLevelWithSummoning();
		return level / team.size();
	}

	public int getDefenceLevel() {
		if (team.size() == 0)
			return 99;
		int level = 0;
		for (Player player : team)
			level += player.getSkills().getLevelForXp(Skills.DEFENCE);
		return level / team.size();
	}

	public double getDificultyRatio() {
		if (dificulty > team.size())
			return 1;
		return 0.7 + (((double) dificulty / (double) team.size()) * 0.3);
	}

	public int getMaxLevel(int skill) {
		if (team.size() == 0)
			return 1;
		int level = 0;
		for (Player player : team) {
			int lvl = player.getSkills().getLevelForXp(skill);
			if (lvl > level)
				level = lvl;
		}
		return level;
	}

	public int getAttackLevel() {
		if (team.size() == 0)
			return 99;
		int level = 0;
		for (Player player : team)
			level += player.getSkills().getLevelForXp(Skills.ATTACK);
		return level / team.size();
	}

	public int getMagicLevel() {
		if (team.size() == 0)
			return 99;
		int level = 0;
		for (Player player : team)
			level += player.getSkills().getLevelForXp(Skills.MAGIC);
		return level / team.size();
	}

	public int getRangeLevel() {
		if (team.size() == 0)
			return 99;
		int level = 0;
		for (Player player : team)
			level += player.getSkills().getLevelForXp(Skills.RANGE);
		return level / team.size();
	}

	public CopyOnWriteArrayList<Player> getTeam() {
		return team;
	}

	public int getSize() {
		return size;
	}

	public int getIndex(Player player) {
		int index = 0;
		for (Player p2 : team) {
			if (p2 == player)
				return index;
			index++;
		}
		return 0;
	}

	public int getDifficulty() {
		if (dificulty > team.size())
			dificulty = team.size();
		return dificulty;
	}

	public boolean isGuideMode() {
		return guideMode || complexity <= 4;
	}

	/*
	 * dont use for dung itself
	 */

	public boolean getGuideMode() {
		return guideMode;
	}

	public void setGuideMode(boolean guideMode) {
		this.guideMode = guideMode;
	}

	public boolean isKeyShare() {
		return keyType == 1;
	}

	public void setKeyShare(boolean isKeyShare) {
		this.keyType = isKeyShare ? 1 : 2;
	}

	public int getKeyType() {
		return keyType;
	}
}
