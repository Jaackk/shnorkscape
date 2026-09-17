package com.rs.game.activities.snowball.game;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.activites.CastleWars;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

import lombok.Getter;
import lombok.val;

public class SnowballFightGame {
    static final String TEAM_ATTRIBUTE_KEY = "snowball_fight_team";

    @Getter
    private final SnowballFightTeam teamZamorak;
    @Getter
    private final SnowballFightTeam teamSaradomin;



    public SnowballFightGame(List<Player> players) {
        teamZamorak = new SnowballFightTeam(SnowballFightTeamType.ZAMORAKIAN);
        teamSaradomin = new SnowballFightTeam(SnowballFightTeamType.SARADOMIN);
        distributeTeams(players);
    }

    private void distributeTeams(List<Player> players) {
        Collections.shuffle(players);

        for (int i = 0; i < players.size() / 2; i++) {
            val player = players.get(i);
            addPlayerToTeam(player, teamZamorak);
        }
        for (int i = players.size() / 2; i < players.size(); i++) {
            val player = players.get(i);
            addPlayerToTeam(player, teamSaradomin);
        }

        forEachPlayer(this::sendInterfaceInformation);
    }

    private void addPlayerToTeam(Player player, SnowballFightTeam team) {
        team.getPlayers().add(player);
        player.getTemporaryAttributtes().put(TEAM_ATTRIBUTE_KEY, team);
        player.sendMessage(team.getType().getMessageColor() + "You are apart of the " + team.getType().toString() + " team.");
        addCloak(player);
    }

    private void forEachTeamMember(SnowballFightTeam team, Consumer<Player> action) {
        team.getPlayers().forEach(action);
    }

    private void forEachPlayer(Consumer<Player> action) {
        teamZamorak.getPlayers().forEach(action);
        teamSaradomin.getPlayers().forEach(action);
    }

    void leaveGame(Player player) {
        val team = (SnowballFightTeam) player.getTemporaryAttributtes().get(TEAM_ATTRIBUTE_KEY);
        team.getPlayers().remove(player);
        removeCloak(player);
        forEachPlayer(this::sendInterfaceInformation);
        clearInterfaces(player);
        handlePossibleWinner(team);
    }

    private void clearInterfaces(Player player) {
        player.getPackets().sendPlayerOption("null", 1, true);
        player.getInterfaceManager().closeOverlay(false);
    }

    private void handlePossibleWinner(SnowballFightTeam team) {
        if (team.getPlayers().size() == 0) {
            val winningTeam = team == teamZamorak ? teamSaradomin : teamZamorak;
            handleWinningTeam(winningTeam);
            handleLosingTeam(team);
            forEachPlayer(player -> {
                clearInterfaces(player);
                player.getControlerManager().startControler("XmasController", true);
            });
            stopGame();
        }
    }

    private void stopGame() {
        World.setSnowballFightGame(null);
    }

    private void handleWinningTeam(SnowballFightTeam team) {
        forEachTeamMember(team, player -> {
            val victoryAnimationId = 14288;
            player.setNextAnimation(new Animation(victoryAnimationId));
            givePlayerReward(player, team.getHighestDamagingPlayer() == player, true);
            removeCloak(player);
            player.sendMessage(team.getType().getMessageColor() + "Your team wins!");
            healPlayer(player);
            player.getXmas().timesWon++;
        });
    }

    private void givePlayerReward(Player player, boolean mvp, boolean winner) {
        if (mvp) {
            val rollReward = handleRewardRoll();
            player.getXmas().timesMostDamage++;
            if(rollReward != null) {
                player.getInventory().addItem(rollReward);
                // add message for news after sql rework
            }
        }
        val energy = mvp ? 140 : 100;
        if (winner) {
            player.sendMessage(Colors.wrap(Colors.DCYAN, "You have received "+energy+" snow energy for winning the snowball fight!"));
        } else {
            player.sendMessage(Colors.wrap(Colors.DCYAN, "You have received "+energy+" snow energy for being the MVP on your team!"));
        }
    }

    private Item handleRewardRoll() {
        final int roll = Utils.random(1, 1000);

        if(roll == 1000)
            return new Item(26517, 1);

        if(roll >= 1 && roll <= 4)
            return new Item(1050, 1);

        if(roll == 5 || roll == 6)
            return new Item(30412, 1);

        return null;
    }

    private void handleLosingTeam(SnowballFightTeam team) {
        forEachTeamMember(team, player -> {
            val cryingAnimationId = 14149;
            player.setNextAnimation(new Animation(cryingAnimationId));
            removeCloak(player);
            player.sendMessage(team.getType().getMessageColor() + "Your team, unfortunately, has lost.");
            healPlayer(player);

            if(player == team.getHighestDamagingPlayer())
                givePlayerReward(player, true, false);
        });
    }

    private void healPlayer(Player player) {
        player.heal(player.getMaxHitpoints());
    }

    private void addCloak(Player player) {
        val team = (SnowballFightTeam) player.getTemporaryAttributtes().get(TEAM_ATTRIBUTE_KEY);
        val teamCapeId = team.getType().getItemId();

        CastleWars.setCape(player, new Item(teamCapeId));
    }

    private void removeCloak(Player player) {
        CastleWars.setCape(player, null);
    }

    public void handleDeath(Player player) {
        CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
            int loop;

            @Override
            public boolean repeat() {
                if (loop == 0) {
                    val dramaticDeathAnimationId = 12544;
                    player.setNextAnimation(new Animation(dramaticDeathAnimationId));
                } else if (loop == 1) {
                    player.getPackets().sendGameMessage("Oh dear, you have died.");
                } else if (loop == 5) {
                    val dramaticDeathPostAnimationId = 12475;

                    player.reset(false);
                    player.resetReceivedDamage();
                    player.setNextAnimation(new Animation(dramaticDeathPostAnimationId));
                    leaveGame(player);

                    player.getControlerManager().startControler("XmasController", true);
                } else if (loop == 6) {
                    player.getPackets().sendMusicEffect(90);
                    return false;
                }
                loop++;
                return true;
            }
        }, 0 ,1);
    }

    void sendInterfaceInformation(Player player) {
        val saradominComponentId = 5;
        val zamorakComponentId = 10;

        player.getPackets().sendIComponentText(SnowballFightGameController.INTERFACE_ID, saradominComponentId,
                teamSaradomin.getType().getMessageColor() + "Players: " + teamSaradomin.getPlayers().size());
        player.getPackets().sendIComponentText(SnowballFightGameController.INTERFACE_ID, zamorakComponentId,
                teamZamorak.getType().getMessageColor() + "Players: " + teamZamorak.getPlayers().size());
    }

    public boolean containsPlayer(Player player) {
        return teamZamorak.getPlayers().contains(player) || teamSaradomin.getPlayers().contains(player);
    }

    void addDamage(Player player, double damage) {
        val team = (SnowballFightTeam) player.getTemporaryAttributtes().get(TEAM_ATTRIBUTE_KEY);
        team.getDamageDealt().put(player, team.getDamageDealt().getOrDefault(player, 0) + (int) damage);
    }
}
