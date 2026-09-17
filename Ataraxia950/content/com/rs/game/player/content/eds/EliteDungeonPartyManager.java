package com.rs.game.player.content.eds;

import java.util.concurrent.CopyOnWriteArrayList;

import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;

import lombok.Getter;
import lombok.Setter;

public class EliteDungeonPartyManager {

    private String leader;
    private final transient CopyOnWriteArrayList<Player> team;
    @Getter
    @Setter
    private transient EliteDungeon dungeon;

    public EliteDungeonPartyManager() {
        team = new CopyOnWriteArrayList<Player>();
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

    public void add(Player player) {
        synchronized (this) {
            for (Player p2 : team)
                p2.getPackets().sendGameMessage(player.getDisplayName() + " has joined the party.");
            team.add(player);
            player.getEliteDungeonsManager().setParty(this);
            if (team.size() == 1) {
                setLeader(player);
//                if (dungeon != null)
//                    dungeon.endDestroyTimer();
            } else
                player.getPackets().sendGameMessage("You join the elite dungeon party.");
            for (Player p2 : team)
                refreshPartyDetails(p2);
        }
    }

    public void sendMessage(String message) {
        for (Player p2 : team)
            p2.getPackets().sendGameMessage(message);
    }

    public void remove(Player player, boolean logout) {
        synchronized (this) {
            team.remove(player);
            player.getEliteDungeonsManager().setParty(null);
            player.getEliteDungeonsManager().expireInvitation();
            player.getEliteDungeonsManager().refreshPartyDetailsComponents();
            player.getPackets().sendGameMessage("You leave the party.");
            if (dungeon != null) {
                dungeon.removePlayer(player);
                if (team.size() == 0)
                    dungeon.destroyRooms();
                if (!logout) {
                    if (team.size() == 0)
                        dungeon.removeDungeon();
                }
            }
            for (Player p2 : team)
                p2.getPackets().sendGameMessage(player.getDisplayName() + " has left the party.");
            if (isLeader(player) && team.size() > 0)
                setLeader(team.get(0));
            for (Player p2 : team)
                refreshPartyDetails(p2);
        }
    }

    public void leaveParty(Player player, boolean logout) {
        player.stopAll();
        remove(player, logout);
    }

    public void refreshPartyDetails(Player player) {
        player.getEliteDungeonsManager().refreshPartyDetailsComponents();
        player.getEliteDungeonsManager().refreshNames();
    }

    public CopyOnWriteArrayList<Player> getTeam() {
        return team;
    }

    public int getSize() {
        return team.size();
    }

    public static void enterDungeon(Player player, int type) {
        EliteDungeonPartyManager p = player.getEliteDungeonsManager().getParty();
        final String dungeonName = EliteDungeonsConstants.getDungeonName(type);
        if (p != null && !p.isLeader(player) && p.getDungeon() == null) {
            player.getDialogueManager().startDialogue("SimpleMessage", "Only party leader can start the dungeon.");
            return;
        }

        player.getDialogueManager().startDialogue(new Dialogue() {
            EliteDungeonPartyManager party;

            @Override
            public void start() {
                this.party = p;
                if (party == null) {
                    if (player.isInsideAnyDungParty()) {
                        end();
                        player.getDialogueManager().startDialogue("SimpleMessage", "You need to be in an elite dungeon party to enter.");
                        return;
                    }
                    stage = 1;
                    sendDialogue("You need to be in an elite dungeon party to enter.");
                    return;
                }
                if (party.isLeader(player) && party.getDungeon() != null) {
                    stage = 0;
                    sendOptionsDialogue("DO YOU WANT TO CONTINUE TO WHERE YOUR GROUP LEFT OFF?", "Yes.", "No.");
                    return;
                }
                sendOptionsDialogue("WOULD YOU LIKE TO ENTER THE " + dungeonName.toUpperCase() + "?", "Yes", "No, Not now");
            }

            @Override
            public void run(int interfaceId, int componentId) {
                switch (stage) {
                case -1:
                    end();
                    if (componentId == OPTION_1) {
                        if (party.isLeader(player) && party.getDungeon() != null) {
                            party.getDungeon().destroyRooms();
                            party.getDungeon().removeDungeon();
                            party.setDungeon(null);
                        }
                        player.getEliteDungeonsManager().getParty().start(player, type);
                    } else if (componentId == OPTION_2) {
                        if (party.isLeader(player) && party.getDungeon() != null) {
                            party.getDungeon().destroyRooms();
                            party.getDungeon().removeDungeon();
                            party.setDungeon(null);
                            party.sendMessage("Your party dungeon progress has been reset.");
                        }
                    }
                    break;
                case 0:
                    if (componentId == OPTION_1) {
                        end();
                        player.getEliteDungeonsManager().getParty().start(player, type);
                    } else if (componentId == OPTION_2) {
                        if (party.getDungeon().containsPlayers()) {
                            player.getDialogueManager().startDialogue("SimpleMessage", "Some of your party members are still in the dungeon, You can't create a new dungeon while they are inside.");
                            return;
                        }
                        stage = -1;
                        sendOptionsDialogue("WOULD YOU LIKE TO ENTER THE " + dungeonName.toUpperCase() + "?", "Yes, start a new dungeon progress.", "No, and reset my dungeon progress.", "No, not right now.");
                    }
                    break;
                case 1:
                    stage = 2;
                    sendOptionsDialogue("Would you like to form an elite dungeon party?", "Yes.", "No, not right now.");
                    break;
                case 2:
                    if (componentId == OPTION_2) {
                        end();
                        return;
                    }
                    this.party = new EliteDungeonPartyManager();
                    party.add(player);
                    stage = -1;
                    sendOptionsDialogue("WOULD YOU LIKE TO ENTER THE " + dungeonName.toUpperCase() + "?", "Yes", "No, Not now");
                    break;
                }
            }

            @Override
            public void finish() {

            }
        });
    }

    public void start(Player player, int type) {
        synchronized (this) {
            if (dungeon == null)
                dungeon = new EliteDungeon(this, type);
            if (!dungeon.enterDungeon(player))
                player.getDialogueManager().startDialogue("Dungeon is loading, please try again in a moment.");
        }
    }
}
