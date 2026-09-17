package com.rs.game.activites.pest;

import com.google.common.collect.Sets;
import com.rs.Settings;
import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.WorldTile;
import com.rs.game.activites.pest.PestControl.PestData;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.pestcontrol.PestControlGame;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Lander {

    private static final int TIME = Settings.DEBUG || Settings.TEST_SERVER_MODE ? 5 : 60;
    public static Lander[] landers = new Lander[3];
    private static final int AUTO_GAME = 25;

    static {
        for (int i = 0; i < landers.length; i++)
            landers[i] = new Lander(i, LanderRequirement.forId(i));
    }

    private final List<Player> lobby = Collections.synchronizedList(new LinkedList<Player>());
    private LobbyTimer timer;
    private final LanderRequirement landerRequirement;
    public Set<Player> confirmations = Sets.newConcurrentHashSet();
    public boolean confirmationPeriod = false;
    private final int index;

    public Lander(int index, LanderRequirement landerRequirement) {
        this.index = index;
        this.landerRequirement = landerRequirement;
    }

    public static boolean canEnter(Player player, int landerIndex) {
        Lander lander = landers[landerIndex];
        if (player.getSkills().getCombatLevelWithSummoning() < lander.getLanderRequirement().requirement) {
            player.getDialogueManager().startDialogue("SimpleMessage", "You need a combat level of " + lander.getLanderRequirement().getRequirement() + " or more to enter in boat.");
            return false;
        }else if (/*player.getPet() != null || */player.getFamiliar() != null) {
            player.getPackets().sendGameMessage("You can't take a follower into the lander, there isn't enough room!");
            return false;
        }
        lander.enterLander(player);
        return true;
    }

    public static Lander[] getLanders() {
        return landers;
    }

    public void add(Player player) {
        lobby.add(player);
        refreshLanderInterface();
        reject(null);
    }

    public void confirm(Player player) {
        if (confirmationPeriod) {
            confirmations.removeIf(it -> it.hasFinished() || !it.isActive() || !lobby.contains(it));
            confirmations.remove(player);
            if (confirmations.size() == 0) {
                passPlayersToGame();
                confirmationPeriod = false;
            } else if (confirmations.size() == 1) {
                player.sendMessage("Waiting for 1 more player.");
            } else {
                player.sendMessage("Waiting for " + confirmations.size() + " more players.");
            }
        }
    }

    public void reject(Player player) {
        if (confirmationPeriod) {
            confirmationPeriod = false;
            timer.seconds = TIME;
            synchronized (lobby) {
                for (Player inside : lobby) {
                    inside.closeInterfaces();
                    if (player != null) {
                        inside.sendMessage(player.getDisplayName() + " has rejected the pest control game.");
                    } else {
                        inside.sendMessage("Someone entered the lander.");
                    }
                }
            }
        }
    }

    public void enterLander(Player player) {
        if (lobby.size() == 0)
            CoresManager.getServiceProvider().scheduleFixedLengthTask(timer = new LobbyTimer(), 1, 1);
        player.getControlerManager().startControler("PestControlLobby", landerRequirement.getId());
        add(player);
        player.useStairs(-1, landerRequirement.getWorldTile(), 1, 2, "You board the lander.");
    }

    public void exitLander(Player player) {
        player.useStairs(-1, landerRequirement.getExitTile(), 1, 2, "You leave the lander.");
        remove(player);
    }

    public LanderRequirement getLanderRequirement() {
        return landerRequirement;
    }

    public List<Player> getPlayers() {
        return lobby;
    }

    public LobbyTimer getTimer() {
        return timer;
    }

    private void passPlayersToGame() {
        final List<Player> playerList = new LinkedList<Player>();
        playerList.addAll(Collections.synchronizedList(lobby));
        lobby.clear();
        if (playerList.size() > AUTO_GAME) {
            for (int index = AUTO_GAME; index < playerList.size(); index++) {
                Player player = playerList.get(index);
                if (player == null) {
                    playerList.remove(index);
                    continue;
                }
                player.getPackets().sendGameMessage("You have received priority over other players.");
                playerList.remove(index);
                lobby.add(player);
            }
        }
        new PestControl(playerList, PestData.valueOf(landerRequirement.name())).create();
    }

    private void refreshLanderInterface() {
        synchronized (lobby) {
            for (Player teamPlayer : lobby) {
                if(teamPlayer.getControlerManager().getControler() instanceof PestControlGame) {
                    teamPlayer.getControlerManager().getControler().sendInterfaces();
                }
            }
        }
    }

    public void remove(Player player) {
        lobby.remove(player);
        refreshLanderInterface();
    }

    @Override
    public String toString() {
        return landerRequirement.name().toLowerCase();
    }

    public enum LanderRequirement {

        NOVICE(0, 20, new WorldTile(2653, 2634,
                0), new WorldTile(2650, 2634, 0)),

        INTERMEDIATE(1, 50, new WorldTile(2640, 2631,
                0), new WorldTile(2637, 2631, 0)),

        VETERAN(2, 80, new WorldTile(2629, 2645,
                0), new WorldTile(2632, 2645, 0));

        private static final Map<Integer, LanderRequirement> landers = new HashMap<Integer, LanderRequirement>();

        static {
            for (LanderRequirement lander : LanderRequirement.values())
                landers.put(lander.getId(), lander);
        }

        int id, requirement, reward;
        int[] pests;
        WorldTile tile, exit;

        LanderRequirement(int id, int requirement, WorldTile tile, WorldTile exit) {
            this.id = id;
            this.requirement = requirement;
            this.tile = tile;
            this.exit = exit;
        }

        public static LanderRequirement forId(int id) {
            return landers.get(id);
        }

        public WorldTile getExitTile() {
            return exit;
        }

        public int getId() {
            return id;
        }

        public int getRequirement() {
            return requirement;
        }

        public WorldTile getWorldTile() {
            return tile;
        }
    }

    public class LobbyTimer extends FixedLengthRunnable {

        private int seconds = TIME;

        public int getSeconds() {
            return seconds;
        }

        @Override
        public boolean repeat() {
            if (lobby.size() == 0) {
                confirmationPeriod = false;
                return false;
            }
            if (confirmationPeriod && lobby.size() > 4) {
                passPlayersToGame();
                return true;
            }
            if (confirmationPeriod) {
                synchronized (lobby) {
                    for (Player player : lobby) {
                        if (player == null || !player.isActive() || player.hasFinished())
                            continue;
                        if (confirmations.contains(player) && !player.getInterfaceManager().containsChatBoxInter()) {
                            player.getWalkSteps().clear();
                            player.dialogueManager.startDialogue("PestControlConfirmationD", index);
                        }
                    }
                }
            }
            if (Settings.DEBUG && seconds == 30 || seconds == 0 && !confirmationPeriod) {
                if (lobby.size() < 5) {
                    confirmationPeriod = true;
                    confirmations.clear();

                    synchronized (lobby) {
                        for (Player player : lobby) {
                            if (player == null || !player.isActive() || player.hasFinished())
                                continue;
                            player.getWalkSteps().clear();
                            player.dialogueManager.startDialogue("PestControlConfirmationD", index);
                            confirmations.add(player);
                        }
                    }
                } else {
                    passPlayersToGame();
                }
            }
            if (seconds == 0) {
                seconds = TIME;
                refreshLanderInterface();
                return true;
            }
            seconds--;
            refreshLanderInterface();
            return true;
        }
    }
}
