package com.rs.game.activities.snowball.lobby;

import com.rs.Settings;
import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.World;
import com.rs.game.activities.snowball.game.SnowballFightGame;
import com.rs.game.player.Player;
import com.rs.game.player.content.xmas.XmasController;
import com.rs.utils.Colors;
import com.rs.utils.TimeUtils;
import lombok.Getter;
import lombok.val;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class SnowballLobby {

    private static final int LOBBY_OVERLAY = 57;
    private static final int MINIMUM_PLAYERS = 2;

    @Getter
    private final List<Player> players = new ArrayList<>();
    private LocalDateTime nextPossibleEndTime;

    public SnowballLobby() {
        initializeTransferTask();
    }

    public static void enterLobby(final Player player) {
        player.closeInterfaces();
        CoresManager.getServiceProvider().executeWithDelay(player::closeInterfaces, 2);
        player.getControlerManager().startControler("SnowballLobbyController");
    }

    public static boolean canEnter(final Player player) {
        return !player.isUnderCombat() &&
                player.getControlerManager().getControler() instanceof XmasController &&
                player.getCurrentInstance() == null &&
                !World.getSnowballLobby().getPlayers().contains(player) &&
                player.getFamiliar() == null &&
                player.getEquipment().getCapeId() == -1 &&
                player.getInventory().getFreeSlots() >= 1 &&
                !World.getSnowballLobby().isSameMacInLobby(player);
    }

    public static void sendInvalidEntryMessage(final Player player) {
        String message = null;
        if (World.getSnowballLobby().getPlayers().contains(player)) {
            message = "You're already waiting for a game to start!";
        } else if (player.isUnderCombat()) {
            message = "Please exit combat before trying to join the snowball fight!";
        } else if (player.getFamiliar() != null) {
            message = "Your familiar must be dismissed to join the snowball fight";
        } else if (player.getEquipment().getCapeId() != -1) {
            message = "You can't wear capes to this minigame!";
        } else if (player.getInventory().getFreeSlots() < 1) {
            message = "You must have at least 1 inventory slot empty to enter!";
        } else if (World.getSnowballLobby().isSameMacInLobby(player)) {
            message = "You cannot bring more than one account into the snowball fight!";
        } else if (!(player.getControlerManager().getControler() instanceof XmasController)) {
            message = "Please try to do this from the main Christmas area!";
        }
        if (message != null) {
            player.sendMessage(Colors.wrap(Colors.DARK_RED, message));
        }
    }

    void initializeTimeLeftComponentUpdateTask() {
        val timeLeftComponentUpdateTask = new FixedLengthRunnable() {
            @Override
            public boolean repeat() {
                if (gameCurrentlyRunning()) {
                    forEachPlayer(player -> sendGameCurrentlyRunningComponentText(player));
                } else {
                    forEachPlayer(player -> sendTimeLeftComponentText(player));
                }
                return !players.isEmpty();
            }
        };
        CoresManager.getServiceProvider().scheduleFixedLengthTask(timeLeftComponentUpdateTask, 1, 1, TimeUnit.SECONDS);
    }

    private void forEachPlayer(Consumer<Player> playerConsumer) {
        players.forEach(playerConsumer);
    }

    void sendLobbyInterface(final Player player) {
        player.getInterfaceManager().sendOverlay(LOBBY_OVERLAY, false);
    }

    private void sendTimeLeftComponentText(Player player) {
        val timeLeftComponentId = 1;
        val timeLeft = TimeUtils.getMMSSTime(LocalDateTime.now(), nextPossibleEndTime);
        player.getPackets().sendIComponentText(LOBBY_OVERLAY, timeLeftComponentId, Colors.BLACK + "Time left: " + timeLeft);
    }

    private void sendGameCurrentlyRunningComponentText(Player player) {
        val timeLeftComponentId = 1;
        player.getPackets().sendIComponentText(LOBBY_OVERLAY, timeLeftComponentId, "Game is currently running!");
    }

    public void removePlayer(final Player player) {
        players.remove(player);
        sendPlayerCount(player, false);
    }

    private boolean gameCurrentlyRunning() {
        return World.getSnowballFightGame() != null &&
                World.getSnowballFightGame().getTeamSaradomin().getPlayers().size() > 0 &&
                World.getSnowballFightGame().getTeamZamorak().getPlayers().size() > 0;
    }

    private void initializeTransferTask() {
        val timePerTask = Settings.DEBUG ? 10 : Settings.TEST_SERVER_MODE ? 1 : 2;
        val timeUnit = Settings.DEBUG ? TimeUnit.SECONDS : TimeUnit.MINUTES;
        val transferTask = new FixedLengthRunnable() {
            @Override
            public boolean repeat() {
                if (!gameCurrentlyRunning() && players.size() >= MINIMUM_PLAYERS) {
                    suspendLobbySession();
                } else if (gameCurrentlyRunning()) {
                    return true;
                }
                val nextPossibleEndTimeMillis = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(timePerTask, timeUnit);
                nextPossibleEndTime = Instant.ofEpochMilli(nextPossibleEndTimeMillis).atZone(ZoneId.systemDefault()).toLocalDateTime();
                return true;
            }
        };
        CoresManager.getServiceProvider().scheduleFixedLengthTask(transferTask, 0, timePerTask, timeUnit);
    }

    private void suspendLobbySession() {
        transferPlayers();
        clearPlayers();
    }

    private void clearPlayers() {
        players.clear();
    }

    private void transferPlayers() {
        setNewSnowballFightGame();
        healPlayers();
        setPlayersControllersToGame();
    }

    private void healPlayers() {
        forEachPlayer(player -> player.heal(player.getMaxHitpoints()));
    }

    private void setPlayersControllersToGame() {
        forEachPlayer(player -> player.getControlerManager().startControler("SnowballFightGameController"));
    }

    private void setNewSnowballFightGame() {
        World.setSnowballFightGame(new SnowballFightGame(players));
    }

    private boolean isSameMacInLobby(final Player player) {
        if (Settings.DEBUG || Settings.TEST_SERVER_MODE) {
            return false;
        }
        for(Player entry : players) {
            if(player.getCurrentMac().equals(entry.getCurrentMac())) {
                return true;
            }
        }
        return false;
    }

    void sendPlayerCount(Player player, boolean joined) {
        forEachPlayer(lobbyPlayer -> {
            val isOrAre = players.size() == 1 ? "is" : "are";
            val joinedOrLeaved = joined ? "joined" : "left";
            val playerOrPlayers = players.size() == 1 ? "player" : "players";
            lobbyPlayer.sendMessage(player.getDisplayName() + " has " + joinedOrLeaved + ". There " +
                    isOrAre + " now " + players.size() + " " + playerOrPlayers + " waiting for the snowball fight.");
        });
    }
}