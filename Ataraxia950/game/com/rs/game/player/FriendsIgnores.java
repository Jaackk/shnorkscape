package com.rs.game.player;

import com.rs.game.World;
import com.rs.game.player.TemporaryAttributes.Key;
import com.rs.game.player.content.FriendChatsManager;
import com.rs.game.player.content.QuickChatMessage;
import com.rs.network.packet.PacketRepository;
import com.rs.utils.InputNameEvent;
import com.rs.utils.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FriendsIgnores implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 39693097250367467L;

	// friends chat
	private String chatName;
	private HashMap<String, Integer> friendsChatRanks;
	private byte whoCanEnterChat;
	private byte whoCanTalkOnChat;
	private byte whoCanKickOnChat;
	private byte whoCanShareloot;
	@SuppressWarnings("unused")
	private boolean coinshare;
	private byte friendsChatStatus;

	// friends list
	private final List<String> friends;
	private final List<String> ignores;
	private List<String> tillLogoutIgnores;

	private byte privateStatus;

	private transient Player player;

	public FriendsIgnores() {
		friends = new ArrayList<String>(200);
		ignores = new ArrayList<String>(100);
		friendsChatRanks = new HashMap<String, Integer>(200);
		whoCanKickOnChat = 7;
		whoCanShareloot = -1;
	}

	public void addFriend(String username) {
		if (friends.size() >= 200 && !player.isOwner()) {
			player.getPackets().sendGameMessage("Your friends list is full.");
			return;
		}
		if (username.equals(player.getUsername())) {
			player.getPackets().sendGameMessage("You can't add yourself.");
			return;
		}
		if (ignores.contains(player.getUsername()) || tillLogoutIgnores.contains(player.getUsername())) {
			player.getPackets().sendGameMessage("Please remove " + username + " from your ignore list first.");
			return;
		}
		String displayName;
		Player p2 = World.getPlayerByDisplayName(username);
		if (p2 != null) {
			displayName = p2.getDisplayName();
		} else {
			displayName = Utils.formatPlayerNameForDisplay(username);
		}
		String formatedUsername = Utils.formatPlayerNameForProtocol(username);
		if (friends.contains(formatedUsername)) {
			player.getPackets().sendGameMessage(username + " is already on your friends list.");
			return;
		}
		friends.add(formatedUsername);
		getFriendsChatRanks().put(formatedUsername, 0);
		FriendChatsManager.refreshChat(player);
		boolean online = p2 != null && isOnline(p2);
		player.getPackets().sendFriend(Utils.formatPlayerNameForDisplay(username), displayName, 1, online, online);
		if (privateStatus == 1 && p2 != null) {
			p2.getFriendsIgnores().changeFriendStatus(player, true);
		}
	}

	public void addIgnore(String username, boolean tillLogout) {
		if (ignores.size() + tillLogoutIgnores.size() >= 100) {
			player.getPackets().sendGameMessage("Your ignores list is full.");
			return;
		}
		if (username.equals(player.getUsername())) {
			player.getPackets().sendGameMessage("You can't add yourself.");
			return;
		}
		if (friends.contains(player.getUsername())) {
			player.getPackets().sendGameMessage("Please remove " + username + " from your friends list first.");
			return;
		}
		Player p2 = World.getPlayerByDisplayName(username);
		String formatedUsername = p2 != null ? p2.getUsername() : Utils.formatPlayerNameForProtocol(username);
		if (ignores.contains(formatedUsername) || tillLogoutIgnores.contains(formatedUsername)) {
			player.getPackets().sendGameMessage(formatedUsername + " is already on your ignores list.");
			return;
		}
		if (tillLogout) {
			tillLogoutIgnores.add(formatedUsername);
		} else {
			ignores.add(formatedUsername);
		}
		player.getPackets().sendIgnore(Utils.formatPlayerNameForDisplay(p2 != null ? p2.getUsername() : username), p2 != null ? p2.getDisplayName() : Utils.formatPlayerNameForDisplay(username), false);
	}

	public boolean canTalk(Player player) {
		return getRank(player.getUsername()) >= whoCanTalkOnChat;
	}

	public void changeFriendStatus(Player p2, boolean online) {
		if (p2 == null || player == null) {
			return;
		}
		if (!friends.contains(p2.getUsername())) {
			return;
		}
		if (online && !isOnline(p2)) {
			online = false;
		}
		if (player.getFriendsIgnores().privateStatus == 2) {
			online = false;
		}
		player.getPackets().sendFriend(Utils.formatPlayerNameForDisplay(p2.getUsername()), p2.getDisplayName(), 1, online, true);
	}

	public void changeRank(String username, int rank) {
		String formatedUsername = Utils.formatPlayerNameForProtocol(username);
		if (!friends.contains(formatedUsername)) {
			return;
		}
		getFriendsChatRanks().put(formatedUsername, rank);
		String displayName;
		Player p2 = World.getPlayerByDisplayName(username);
		if (p2 != null) {
			displayName = p2.getDisplayName();
		} else {
			displayName = Utils.formatPlayerNameForDisplay(username);
		}
		boolean online = p2 != null && isOnline(p2);
		player.getPackets().sendFriend(Utils.formatPlayerNameForDisplay(username), displayName, 1, online, true);
		FriendChatsManager.refreshChat(player);
	}

	public String getChatName() {
		return chatName == null ? "" : chatName;
	}

	public List<String> getFriends() {
		return friends;
	}

	public HashMap<String, Integer> getFriendsChatRanks() {
		if (friendsChatRanks == null) {// temporary
			whoCanKickOnChat = 7;
			whoCanShareloot = -1;
			friendsChatRanks = new HashMap<String, Integer>(200);
			for (String friend : friends) {
				friendsChatRanks.put(friend, 0);
			}
		}
		return friendsChatRanks;
	}

	public int getFriendsChatStatus() {
		return friendsChatStatus;
	}

	public void setFriendsChatStatus(int friendsChatStatus) {
		this.friendsChatStatus = (byte) friendsChatStatus;
		player.getPackets().sendGameBarStages();
	}

	public List<String> getIgnores() {
		return ignores;
	}

	public byte getPrivateStatus() {
		return privateStatus;
	}

	public void setPrivateStatus(int privateStatus) {
		this.privateStatus = (byte) privateStatus;
		player.getPackets().sendGameBarStages();
		sendFriendsMyStatus(true);
	}

	public int getRank(String username) {
		Integer rank = getFriendsChatRanks().get(username);
		if (rank == null) {
			return -1;
		}
		return rank;
	}

	public int getWhoCanKickOnChat() {
		return whoCanKickOnChat;
	}

	public boolean hasRankToLootShare(String username) {
		return getRank(username) >= whoCanShareloot;
	}

	public void handleFriendChatButtons(int interfaceId, int componentId, int packetId) {
		if (interfaceId == 1109 || interfaceId == 1427) {
			if ((interfaceId == 1109 && componentId == 41) || (interfaceId == 1427 && componentId == 36))
				if (player.getCurrentFriendChat() != null)
					player.getCurrentFriendChat().toogleLootShare(player);
			if ((interfaceId == 1109 && componentId == 13) || (interfaceId == 1427 && componentId == 12)) {
				if (player.getCurrentFriendChat() != null) {
					player.getCurrentFriendChat().leaveChat(player, false);
				} else {
				    openInputFriendChat();
				}
			} else if ((interfaceId == 1109 && componentId == 7) || (interfaceId == 1427 && componentId == 6)) {
				if (player.getInterfaceManager().containsScreenInter()) {
					player.getPackets().sendGameMessage("Please close the interface you have opened before using Friends Chat setup.");
					return;
				}
				player.stopAll();
				openFriendChatSetup();
			} else if ((interfaceId == 1109 && componentId == 47) || (interfaceId == 1427 && componentId == 44)) {
			    if (player.getCurrentFriendChat() != null)
			        openKickFriendChat();
            } else if ((interfaceId == 1427 && componentId == 51)) {// expand
//                player.getInterfaceManager().sendExpandOptionsInterface(235, interfaceId, componentId, 64, 40);
            }
		} else if (interfaceId == 1108) {
			if (componentId == 9) {
				if (packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
					player.sendInputName("Enter chat prefix:", new InputNameEvent() {
						@Override
						public void run(Player player) {
							player.getFriendsIgnores().setChatPrefix(getString());
						}
					});
				} else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET) {
					if (chatName != null) {
						chatName = null;
						refreshChatName();
						FriendChatsManager.destroyChat(player);
					}
				}
			} else if (componentId == 11) {
				if (packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
					whoCanEnterChat = -1;
				} else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET) {
					whoCanEnterChat = 0;
				} else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET) {
					whoCanEnterChat = 1;
				} else if (packetId == PacketRepository.ACTION_BUTTON4_PACKET) {
					whoCanEnterChat = 2;
				} else if (packetId == PacketRepository.ACTION_BUTTON5_PACKET) {
					whoCanEnterChat = 3;
				} else if (packetId == PacketRepository.ACTION_BUTTON9_PACKET) {
					whoCanEnterChat = 4;
				} else if (packetId == PacketRepository.ACTION_BUTTON6_PACKET) {
					whoCanEnterChat = 5;
				} else if (packetId == PacketRepository.ACTION_BUTTON8_PACKET) {
					whoCanEnterChat = 6;
				} else if (packetId == PacketRepository.ACTION_BUTTON10_PACKET) {
					whoCanEnterChat = 7;
				}
				refreshWhoCanEnterChat();
			} else if (componentId == 13) {
				if (packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
					whoCanTalkOnChat = -1;
				} else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET) {
					whoCanTalkOnChat = 0;
				} else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET) {
					whoCanTalkOnChat = 1;
				} else if (packetId == PacketRepository.ACTION_BUTTON4_PACKET) {
					whoCanTalkOnChat = 2;
				} else if (packetId == PacketRepository.ACTION_BUTTON5_PACKET) {
					whoCanTalkOnChat = 3;
				} else if (packetId == PacketRepository.ACTION_BUTTON9_PACKET) {
					whoCanTalkOnChat = 4;
				} else if (packetId == PacketRepository.ACTION_BUTTON6_PACKET) {
					whoCanTalkOnChat = 5;
				} else if (packetId == PacketRepository.ACTION_BUTTON7_PACKET) {
					whoCanTalkOnChat = 6;
				} else if (packetId == PacketRepository.ACTION_BUTTON10_PACKET) {
					whoCanTalkOnChat = 7;
				}
				refreshWhoCanTalkOnChat();
			} else if (componentId == 15) {
				if (packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
					whoCanKickOnChat = -1;
				} else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET) {
					whoCanKickOnChat = 0;
				} else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET) {
					whoCanKickOnChat = 1;
				} else if (packetId == PacketRepository.ACTION_BUTTON4_PACKET) {
					whoCanKickOnChat = 2;
				} else if (packetId == PacketRepository.ACTION_BUTTON5_PACKET) {
					whoCanKickOnChat = 3;
				} else if (packetId == PacketRepository.ACTION_BUTTON9_PACKET) {
					whoCanKickOnChat = 4;
				} else if (packetId == PacketRepository.ACTION_BUTTON6_PACKET) {
					whoCanKickOnChat = 5;
				} else if (packetId == PacketRepository.ACTION_BUTTON7_PACKET) {
					whoCanKickOnChat = 6;
				} else if (packetId == PacketRepository.ACTION_BUTTON10_PACKET) {
					whoCanKickOnChat = 7;
				}
				refreshWhoCanKickOnChat();
				FriendChatsManager.refreshChat(player);
			} else if (componentId == 17) {
				if (packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
					whoCanShareloot = -1;
				} else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET) {
					whoCanShareloot = 0;
				} else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET) {
					whoCanShareloot = 1;
				} else if (packetId == PacketRepository.ACTION_BUTTON4_PACKET) {
					whoCanShareloot = 2;
				} else if (packetId == PacketRepository.ACTION_BUTTON5_PACKET) {
					whoCanShareloot = 3;
				} else if (packetId == PacketRepository.ACTION_BUTTON9_PACKET) {
					whoCanShareloot = 4;
				} else if (packetId == PacketRepository.ACTION_BUTTON6_PACKET) {
					whoCanShareloot = 5;
				} else if (packetId == PacketRepository.ACTION_BUTTON7_PACKET) {
					whoCanShareloot = 6;
				}
				refreshWhoCanShareloot();
			}
		}
	}

	public boolean hasFriendChat() {
		return chatName != null;
	}

	public boolean hasRankToJoin(String username) {
		return getRank(username) >= whoCanEnterChat;
	}

	/**
	 * Just a simple boolean to clear players' ignore and friends lists from invalid names that were sent when the networking was shit and sent random
	 * packets.
	 */
	private boolean clearedInvalidResults;

	public void init() {
		tillLogoutIgnores = new ArrayList<String>(100);
		if (!clearedInvalidResults) {
			clearedInvalidResults = true;
			final List<String> toRemove = new ArrayList<String>();
			for (String s : friends) {
				if (!s.matches("[A-Za-z0-9 ]+") || s.length() < 3)
					toRemove.add(s);
			}
			friends.removeAll(toRemove);
			toRemove.clear();
			for (String s : ignores) {
				if (!s.matches("[A-Za-z0-9 ]+") || s.length() < 3)
					toRemove.add(s);
			}
			ignores.removeAll(toRemove);
			toRemove.clear();
		}
		player.getPackets().sendFriends();
		player.getPackets().sendIgnores();

		if (privateStatus != 2) {
			sendFriendsMyStatus(true);
		}
		if (hasFriendChat()) {
			FriendChatsManager.linkSettings(player);
		}
		refreshFriendsListSorting();
	}

	public boolean isOnline(Player p2) {
		if (p2 == null) {
			return false;
		}
		if (p2.getFriendsIgnores().privateStatus == 2) {
			return false;
		}
		if (p2.getFriendsIgnores().privateStatus == 1 && !p2.getFriendsIgnores().friends.contains(player.getUsername())) {
			return false;
		}
		if (player.getFriendsIgnores().privateStatus == 1 && !player.getFriendsIgnores().friends.contains(player.getUsername())) {
			return false;
		}
		return !player.getFriendsIgnores().getIgnores().contains(p2.getUsername());
	}

	public void openFriendChatSetup() {
		player.getInterfaceManager().sendInterface(1108);
		refreshChatName();
		refreshWhoCanEnterChat();
		refreshWhoCanTalkOnChat();
		refreshWhoCanKickOnChat();
		refreshWhoCanShareloot();
	}

	public void refreshChatName() {
		player.getPackets().sendIComponentText(1108, 9, chatName == null ? "Chat disabled" : chatName);
	}

	public void refreshWhoCanEnterChat() {
		String text;
		if (whoCanEnterChat == 0) {
			text = "Any friends";
		} else if (whoCanEnterChat == 1) {
			text = "Recruit+";
		} else if (whoCanEnterChat == 2) {
			text = "Corporal+";
		} else if (whoCanEnterChat == 3) {
			text = "Sergeant+";
		} else if (whoCanEnterChat == 4) {
			text = "Lieutenant+";
		} else if (whoCanEnterChat == 5) {
			text = "Captain+";
		} else if (whoCanEnterChat == 6) {
			text = "General+";
		} else if (whoCanEnterChat == 7) {
			text = "Only Me";
		} else {
			text = "Anyone";
		}
		player.getPackets().sendIComponentText(1108, 11, text);
	}

	public void refreshWhoCanKickOnChat() {
		String text;
		if (whoCanKickOnChat == 0) {
			text = "Any friends";
		} else if (whoCanKickOnChat == 1) {
			text = "Recruit+";
		} else if (whoCanKickOnChat == 2) {
			text = "Corporal+";
		} else if (whoCanKickOnChat == 3) {
			text = "Sergeant+";
		} else if (whoCanKickOnChat == 4) {
			text = "Lieutenant+";
		} else if (whoCanKickOnChat == 5) {
			text = "Captain+";
		} else if (whoCanKickOnChat == 6) {
			text = "General+";
		} else if (whoCanKickOnChat == 7) {
			text = "Only Me";
		} else {
			text = "Anyone";
		}
		player.getPackets().sendIComponentText(1108, 15, text);
	}

	public void refreshWhoCanShareloot() {
		String text;
		if (whoCanShareloot == 0) {
			text = "Any friends";
		} else if (whoCanShareloot == 1) {
			text = "Recruit+";
		} else if (whoCanShareloot == 2) {
			text = "Corporal+";
		} else if (whoCanShareloot == 3) {
			text = "Sergeant+";
		} else if (whoCanShareloot == 4) {
			text = "Lieutenant+";
		} else if (whoCanShareloot == 5) {
			text = "Captain+";
		} else if (whoCanShareloot == 6) {
			text = "General+";
		} else {
			text = "No-one";
		}
		player.getPackets().sendIComponentText(1108, 17, text);
	}

	public void refreshWhoCanTalkOnChat() {
		String text;
		if (whoCanTalkOnChat == 0) {
			text = "Any friends";
		} else if (whoCanTalkOnChat == 1) {
			text = "Recruit+";
		} else if (whoCanTalkOnChat == 2) {
			text = "Corporal+";
		} else if (whoCanTalkOnChat == 3) {
			text = "Sergeant+";
		} else if (whoCanTalkOnChat == 4) {
			text = "Lieutenant+";
		} else if (whoCanTalkOnChat == 5) {
			text = "Captain+";
		} else if (whoCanTalkOnChat == 6) {
			text = "General+";
		} else if (whoCanTalkOnChat == 7) {
			text = "Only Me";
		} else {
			text = "Anyone";
		}
		player.getPackets().sendIComponentText(1108, 13, text);
	}

	public void removeFriend(String username) {
		String formatedUsername = Utils.formatPlayerNameForProtocol(username);
		Player p2 = World.getPlayerByDisplayName(username);
		if (!friends.remove(formatedUsername)) {
			if (p2 == null) {
				return;
			}
			friends.remove(p2.getUsername());
			getFriendsChatRanks().remove(p2.getUsername());
			FriendChatsManager.refreshChat(player);
		} else {
			getFriendsChatRanks().remove(formatedUsername);
			FriendChatsManager.refreshChat(player);
		}
		if (privateStatus == 1 && p2 != null) {
			p2.getFriendsIgnores().changeFriendStatus(player, true);
		}
	}

	public void removeIgnore(String username) {
		String formatedUsername = Utils.formatPlayerNameForProtocol(username);
		Player p2 = World.getPlayerByDisplayName(username);
		if (!ignores.remove(formatedUsername) && !tillLogoutIgnores.remove(formatedUsername)) {
			if (p2 == null) {
				return;
			}
			if (!ignores.remove(p2.getUsername())) {
				tillLogoutIgnores.remove(p2.getUsername());
			}
		}
	}

	public void sendFriendsMyStatus(boolean online) {
		for (Player p2 : World.getPlayers()) {
			if (p2 == null || !p2.isActive() || p2.hasFinished()) {
				continue;
			}
			p2.getFriendsIgnores().changeFriendStatus(player, online);
		}
	}

	public void sendMessage(Player p2, String message) {
		if (privateStatus == 2) {// off
			privateStatus = 0;
			sendFriendsMyStatus(true);
			player.getPackets().sendPrivateGameBarStage();
		}
		if (p2.getFriendsIgnores().getIgnores().contains(player.getUsername())) {
			player.sendMessage("The targeted player is ignoring you.");
			return;
		}
		if (p2.isAFK()) {
			player.sendMessage("Please note that the targeted player is AFK currently.");
		}
		player.getPackets().sendPrivateMessage(p2.getDisplayName(), message);
		p2.getPackets().receivePrivateMessage(Utils.formatPlayerNameForDisplay(player.getDisplayName()), player.getDisplayName(), player.getMessageIcon() >= 8 ? player.getMessageIcon() - 5 : player.getMessageIcon(), message);
	}

	public void sendQuickChatMessage(Player p2, QuickChatMessage quickChatMessage) {
		player.getPackets().sendPrivateQuickMessageMessage(p2.getDisplayName(), quickChatMessage);
		p2.getPackets().receivePrivateChatQuickMessage(Utils.formatPlayerNameForDisplay(player.getUsername()), player.getDisplayName(), player.getMessageIcon() >= 8 ? player.getMessageIcon() - 5 : player.getMessageIcon(), quickChatMessage);

	}

	public void setChatPrefix(String name) {
		if (name.length() < 1 || name.length() > 20) {
			return;
		}
		chatName = name;
		refreshChatName();
		FriendChatsManager.refreshChat(player);
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public boolean isFriend(String displayName) {
		String formatedUsername = Utils.formatPlayerNameForProtocol(displayName);
		return friends.contains(formatedUsername);
	}
	
	   public void handleFriendListButtons(int interfaceId, int componentId, int packetId) {
	        if ((interfaceId == 1441 && componentId == 23) || (interfaceId == 550 && componentId == 32)
	                || (interfaceId == 235 && componentId == 8))
	            openInputFriend(false);
	        else if ((interfaceId == 1441 && componentId == 30) || (interfaceId == 550 && componentId == 40)
	                || (interfaceId == 235 && componentId == 16))
	            openInputFriend(true);
	        else if ((interfaceId == 1441 && componentId == 62) || (interfaceId == 550 && componentId == 74))
	            openInputIgnore(false);
	        else if ((interfaceId == 1441 && componentId == 70) || (interfaceId == 550 && componentId == 82))
	            openInputIgnore(true);
	        else if ((interfaceId == 1441 && componentId == 38) || (interfaceId == 550 && componentId == 49)
	                || (interfaceId == 235 && componentId == 25)) {
	            // TODO recruit a friend. opens website
	        } else if (interfaceId == 550 && componentId == 23) {
	            player.getInterfaceManager().sendExpandOptionsInterface(235, interfaceId, componentId, 64, 40);
	        } else if ((interfaceId == 550 && (componentId == 56 || componentId == 90)) || (interfaceId == 235 && componentId == 32)
	                || (interfaceId == 1441 && (componentId == 44 || componentId == 78))) {
	            if (packetId == PacketRepository.ACTION_BUTTON1_PACKET)
	                setNextFriendsListSorting();
	            else if(packetId == PacketRepository.ACTION_BUTTON2_PACKET)
	                setFriendsListSorting(0);
	            else if(packetId == PacketRepository.ACTION_BUTTON3_PACKET)
	                setFriendsListSorting(1);
	            else if(packetId == PacketRepository.ACTION_BUTTON4_PACKET)
	                setFriendsListSorting(2);
            }
	    }
	
	public void openInputFriend(boolean remove) {
        player.getTemporaryAttributtes().put(Key.REMOVE_INPUT_INTER, Boolean.TRUE);
	    player.getInterfaceManager().sendInputTextInterface();
	    player.getPackets().sendExecuteScript(remove ? 104 : 103);
	}

	public void openInputIgnore(boolean remove) {
        player.getTemporaryAttributtes().put(Key.REMOVE_INPUT_INTER, Boolean.TRUE);
	    player.getInterfaceManager().sendInputTextInterface();
	    player.getPackets().sendExecuteScript(remove ? 1419 : 105);
	}
	
	private int friendsListSorting;
	
	private void setNextFriendsListSorting() {
	    friendsListSorting = (friendsListSorting + 1) % 3;
	    refreshFriendsListSorting();
	}
	
    private void setFriendsListSorting(int friendsListSorting) {
        this.friendsListSorting = friendsListSorting;
        refreshFriendsListSorting();
    }
    
    private void refreshFriendsListSorting() {
        player.getPackets().sendConfigByFile(30173, friendsListSorting);
    }
    
    public void openInputFriendChat() {
        player.getTemporaryAttributtes().put(Key.REMOVE_INPUT_INTER, Boolean.TRUE);
        player.getInterfaceManager().sendInputTextInterface();
        player.getPackets().sendExecuteScript(8537);
    }
    
    public void openKickFriendChat() {
        player.getTemporaryAttributtes().put(Key.REMOVE_INPUT_INTER, Boolean.TRUE);
        player.getInterfaceManager().sendInputTextInterface();
        player.getPackets().sendExecuteScript(2688);
    }

}