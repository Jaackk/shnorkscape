package com.rs.game.player.content.clans;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.player.Player;
import com.rs.utils.ClanRank;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Clan.java | 3:14:52 PM
 * 
 * @author Chryonic
 * @date Feb 21, 2017
 */
public class Clan implements Serializable {

	public static final int RECRUIT = 0, CORPORAL = 1, SERGEANT = 2, LIEUTENANT = 3, CAPTAIN = 4, GENERAL = 5,
			ADMIN = 100, ORGANISER = 101, COORDINATOR = 102, OVERSEER = 103, DEPUTY_OWNER = 125, LEADER = 126,
			MAX_MEMBERS = 500;

	/**
	 * 
=======

public class Clan implements Serializable {

	public static final int RECRUIT = 0, ADMIN = 100, DEPUTY_OWNER = 125, LEADER = 126, MAX_MEMBERS = 500;

	/**
	 *
>>>>>>> parent of e125924... Clan Rework  + Clan Citadels
	 */
	private static final long serialVersionUID = 4062231702422979939L;

	private String clanLeaderUsername; // index in list
	private final List<ClanMember> members;
	private final List<String> bannedUsers;
	private int timeZone;
	private boolean recruiting;
	private boolean isClanTime;
	private int worldId;
	private int clanFlag;
	private boolean guestsInChatCanEnter;
	private boolean guestsInChatCanTalk;
	private String threadId;
	private String motto;
	private ClanBank clanBank, clanBank2;
	private int gatheredResources;
	private int monsterKills;
	private int playerKills;
	private int clanLevel;
	private int clanEXP;
	private int clanResources;
	private boolean[] unlockedPerks;

	public final int EXPERIENCE = 0, RESOURCES = 1, REVENUE = 2, CAPTIVITY = 3, THICK_SKIN = 4, MOBILE_BANKING = 5,
			BARTERING = 6, EXPERIENCE2 = 7, RESOURCES2 = 8, REVENUE2 = 9, THICK_SKIN2 = 10;
	// motif
	private int mottifTop, mottifBottom;
	private int[] mottifColors;

	// channel
	private int minimumRankForKick;

	private transient String clanName; // its also file name so no need to save

	// infle

	public Clan(String clanName, Player leader) {
		setDefaults();
		this.members = new ArrayList<ClanMember>();
		this.bannedUsers = new ArrayList<String>();
		setClanLeaderUsername(addMember(leader, LEADER));
		init(clanName);
		this.clanBank = new ClanBank();
		this.clanBank2 = new ClanBank();
		this.clanBank2.setSecond(true);
	}

	public ClanMember addMember(Player player, int rank) {
		final ClanMember member = new ClanMember(player.getUsername(), rank);
		members.add(member);
		return member;
	}

	public List<String> getBannedUsers() {
		return bannedUsers;
	}

	/**
	 * @return the clanBank
	 */
	public ClanBank getClanBank() {
		return clanBank;
	}

	public ClanBank getClanBank(Player player) {
		return clanBank;
	}

	public int getClanFlag() {
		return clanFlag;
	}

	public String getClanLeaderUsername() {
		return clanLeaderUsername;
	}

	public String getClanName() {
		return clanName;
	}

	public int getGatheredResources() {
		return gatheredResources;
	}

	public int getLevel() {
		return clanLevel;
	}

	public ClanMember getMemberByName(String username) {
		for (final ClanMember member : members) {
			if (member.getUsername().equalsIgnoreCase(username)) {
				return member;
			}
		}
		return null;
	}

	public int getMemberId(ClanMember member) {
		return members.indexOf(member);
	}

	public List<ClanMember> getMembers() {
		return members;
	}

	public int getMinimumRankForKick() {
		return minimumRankForKick;
	}

	public int getMonsterKills() {
		return monsterKills;
	}

	public int getMottifBottom() {
		return mottifBottom;
	}

	public int[] getMottifColors() {
		return mottifColors;
	}

	public int getMottifTop() {
		return mottifTop;
	}

	public String getMotto() {
		return motto;
	}

	public int getPlayerKills() {
		return playerKills;
	}

	public int getResources() {
		return clanResources;
	}

	public String getThreadId() {
		return threadId;
	}

	public int getTimeZone() {
		return timeZone;
	}

	public int getWorldId() {
		return worldId;
	}

	public int getXP() {
		return clanEXP;
	}


	public void increaseMonsterKills() {
		ClanRank.checkRank(this, 1);
		this.monsterKills++;
	}
	
	public void increasePlayerKills() {
		ClanRank.checkRank(this, 2);
		this.playerKills++;
	}

	public void increaseResources(int resources) {
		clanResources += resources;
	}

	public void increaseXP(int xp) {
		clanEXP += xp;
	}

	public void init(String clanName) {
		this.clanName = clanName;
		if (this.clanBank == null)
			this.clanBank = new ClanBank();
		if (this.clanBank2 == null) {
			this.clanBank2 = new ClanBank();
			this.clanBank2.setSecond(true);
		}
	}

	public boolean isClanTime() {
		return isClanTime;
	}

	public boolean isGuestsInChatCanEnter() {
		return guestsInChatCanEnter;
	}

	public boolean isGuestsInChatCanTalk() {
		return guestsInChatCanTalk;
	}

	public boolean isRecruiting() {
		return recruiting;
	}

	/**
	 * @param clanBank
	 *            the clanBank to set
	 */
	public void setClanBank(ClanBank clanBank) {
		this.clanBank = clanBank;
	}

	public void setClanBank2(ClanBank clanBank2) {
		this.clanBank2 = clanBank2;
	}

	public void setClanFlag(int clanFlag) {
		this.clanFlag = clanFlag;
	}

	public void setClanLeaderUsername(ClanMember member) {
		clanLeaderUsername = member.getUsername();
	}

	public void setDefaults() {
		recruiting = true;
		guestsInChatCanEnter = true;
		guestsInChatCanTalk = true;
		worldId = 1;
		mottifColors = Arrays.copyOf(ItemDefinitions.getItemDefinitions(20709).originalModelColors, 4);
	}

	public void setLevel(int level) {
		clanLevel = level;
	}

	public void setMinimumRankForKick(int minimumRankForKick) {
		this.minimumRankForKick = minimumRankForKick;
	}

	public void setMottifBottom(int mottifBottom) {
		this.mottifBottom = mottifBottom;
	}

	public void setMottifColours(int[] mottifColors) {
		this.mottifColors = mottifColors;
	}

	public void setMottifTop(int mottifTop) {
		this.mottifTop = mottifTop;
	}


	public void setMotto(String motto) {
		this.motto = motto;
	}

	public void setThreadId(String threadId) {
		this.threadId = threadId;
	}

	public void setTimeZone(int gameTime) {
		this.timeZone = gameTime;
	}

	public void setWorldId(int worldId) {
		this.worldId = worldId;
	}

	public void switchClanTime() {
		isClanTime = !isClanTime;
	}

	public void switchGuestsInChatCanEnter() {
		this.guestsInChatCanEnter = !guestsInChatCanEnter;
	}

	public void switchGuestsInChatCanTalk() {
		guestsInChatCanTalk = !guestsInChatCanTalk;
	}

	public void switchRecruiting() {
		recruiting = !recruiting;
	}

	public boolean unlockedPerk(int perkId) {
		return unlockedPerks[perkId];
	}

	public void unlockPerk(int perkId) {
		unlockedPerks[perkId] = true;
	}
	
	public void increaseGatheredResources() {
		// TODO Auto-generated method stub
	}

}
