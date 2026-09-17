package com.rs.game.player.dialogue.impl;

import com.rs.game.World;
import com.rs.game.item.Item;
import com.rs.game.map.bossInstance.BossInstance;
import com.rs.game.map.bossInstance.BossInstanceHandler;
import com.rs.game.map.bossInstance.BossInstanceHandler.Boss;
import com.rs.game.map.bossInstance.InstanceSettings;
import com.rs.game.map.bossInstance.impl.SpiderBossInstance;
import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.InputNameEvent;
import com.rs.utils.Utils;

public class SpiderBossInstanceD extends Dialogue {

    private Boss boss;
    private SpiderBossInstance instance;

    @Override
    public void start() {
        boss = (Boss) parameters[0];
        instance = (SpiderBossInstance) BossInstanceHandler.findInstance(Boss.Spider_Boss, "");
        sendDialogue("<col=8b0000>Beyond this point is the Araxyte hive.", "<col=8b0000>There is no way out other than death or Victory.", "<col=8b0000>Only those who can endure dangerous encounters should proceed.");
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -1:
                stage = 0;
                sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Start/Join (Global)", "Start/Join/Rejoin (Instanced)");
                break;
            case 0:
                switch (componentId) {
                    case OPTION_1:
                        stage = -3;
                        sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Start", "Join");
                        break;
                    case OPTION_2:
                        stage = -4;
                        int count = BossInstanceHandler.getCount(Boss.Spider_Boss) - 1;
                        sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Start", "Join (" + count + ")", "Rejoin");
                        break;
                }
                break;
            case -5:
                int maxPlayers = -1;
                if (componentId == OPTION_1) {
                    maxPlayers = 2;
                } else if (componentId == OPTION_2) {
                    maxPlayers = 1;
                }
                if (startInstance(maxPlayers)) {
                    if (maxPlayers == 2) {
                        player.sendMessage("Your friend can join your instance by selecting 'Join (Instanced)'.");
                    }
                    BossInstanceHandler.createInstance(player, player.getLastBossInstanceSettings());
                }
                break;
            case -4:
                if (componentId == OPTION_1) {
                    sendOptionsDialogue("Fight with a friend?", "Yes, allow a friend.", "No, I'd rather go alone.");
                    stage = -5;
                } else if (componentId == OPTION_2) {
                    end();
                    player.sendInputName("Enter the name of your partner.", new InputNameEvent() {
                        @Override
                        public void run(Player player) {
                            if (getString() == null || getString().isEmpty()) {
                                player.getPackets().sendGameMessage("No valid Araxxor instance found for that name.");
                                return;
                            }
                            Player friend = World.getPlayerByDisplayName(getString());
                            if(friend == null) {
                                player.getPackets().sendGameMessage("No valid Araxxor instance found for that name.");
                                return;
                            }
                            SpiderBossInstance existing = (SpiderBossInstance) BossInstanceHandler.findInstance(Boss.Spider_Boss, friend.getUsername());
                            if (existing == null) {
                                player.getPackets().sendGameMessage("No valid Araxxor instance found for that name.");
                                return;
                            }
                            friend.sendMessage("<col=00ff00>" + player.getUsername() + " has joined the fight.");
                            existing.enterInstance(player, false);
                        }
                    });
                } else if (componentId == OPTION_3) {
                    end();
                    String key = player.getLastBossInstanceKey();
                    if (key == null) {
                        player.sendMessage("You do not have a battle to rejoin.");
                        return;
                    }
                    if (BossInstanceHandler.findInstance(boss, key) == null) {

                        if (key.equals(player.getUsername()) && player.getLastBossInstanceSettings() != null && player.getLastBossInstanceSettings().getBoss() == boss && player.getLastBossInstanceSettings().hasTimeRemaining()) {
                            end();
                            // if the instance is null, and its my own player, use
                            // the settings to recreate it
                            BossInstanceHandler.createInstance(player, player.getLastBossInstanceSettings());
                            return;
                        }

                        player.sendMessage("You do not have a battle to rejoin.");
                        return;
                    }
                    BossInstanceHandler.joinInstance(player, boss, key, false);
                }
                break;
            case -3:
                if (componentId == OPTION_1) {
                    sendOptionsDialogue("Fight with a friend?", "Yes, allow a friend.", "No, I'd rather go alone.");
                    stage = 1;
                } else if (componentId == OPTION_2) {
                    if (instance.getPlayersInsideCount() == 0) {
                        player.getPackets().sendGameMessage("There is no fight taking place that you can join.");
                        end();
                        return;
                    }
                    if (instance.getAraxxor() != null &&
                            !player.getDisplayName().equalsIgnoreCase(instance.getAllowedFriendDisplayName()) ||
                            instance.getPlayersInsideCount() > 1) {
                        player.getPackets().sendGameMessage("The entrance is webbed over, blocked while there is a fight in progress.");
                        end();
                        return;
                    }
                    instance.enterInstance(player, false);
                    end();
                }
                break;
            case 1:
                switch (componentId) {
                    case OPTION_1:
                        end();
                        player.sendInputName("Enter the name of your partner.", new InputNameEvent() {

                            @Override
                            public void run(Player player) {
                                if (instance == null) {
                                    return;
                                }
                                Player friend = World.getPlayerByDisplayName(getString());
                                if (friend == null || !player.getFriendsIgnores().isFriend(friend.getUsername())) {
                                    player.getPackets().sendGameMessage("That player is offline, or has privacy mode enabled.");
                                    return;
                                }
                                if (instance.getAraxxor() != null || instance.getPlayersInsideCount() > 1) {
                                    player.getPackets().sendGameMessage("The entrance is webbed over, blocked while there is a fight in progress.");
                                    return;
                                }
                                player.getPackets().sendGameMessage("<col=00ff00>" + friend.getDisplayName() + " will be able to join the fight if he wants to.");
                                friend.getPackets().sendGameMessage("<col=00ff00>" + player.getDisplayName() + " has invited you to a [Global] Araxxor battle.");
                                instance.setAllowedFriendDisplayName(friend.getDisplayName());
                                instance.enterInstance(player, false);
                            }
                        });
                        break;
                    case OPTION_2:
                        if (instance.getAraxxor() != null) {
                            player.getPackets().sendGameMessage("The entrance is webbed over, blocked while there is a fight in progress.");
                            end();
                            return;
                        }
                        if (instance.getAllowedFriendDisplayName() != null && !instance.getAllowedFriendDisplayName().equalsIgnoreCase(player.getDisplayName())) {
                            player.getPackets().sendGameMessage("The entrance is webbed over, blocked while there is a fight in progress.");
                            end();
                            return;
                        }
                        if (instance.getAllowedFriendDisplayName() == null && instance.getPlayersInsideCount() > 1) {
                            player.getPackets().sendGameMessage("The entrance is webbed over, blocked while there is a fight in progress.");
                            end();
                            return;
                        }
                        instance.enterInstance(player, false);
                        end();
                        break;
                }
                break;
        }
    }

    private boolean startInstance(int maxPlayers) {
        InstanceSettings settings = new InstanceSettings(Boss.Spider_Boss);
        settings.setMaxPlayers(maxPlayers);
        settings.setMinCombat(1);
        settings.setSpawnSpeed(BossInstance.FASTEST);
        settings.setProtection(BossInstance.FRIENDS_ONLY);
        int initialCost = settings.getBoss().getInitialCost();
        if (player.getInventory().getCoinsAmount() < initialCost) {
            player.getPackets().sendGameMessage("You don't have enough coins to start this battle.");
            player.setLastBossInstanceSettings(null);
            return false;
        }
        if (initialCost > 0)
            player.getInventory().removeItemMoneyPouch(new Item(995, initialCost));
        settings.setCreationTime(Utils.currentTimeMillis());
        player.setLastBossInstanceSettings(settings);
        return true;
    }

    @Override
    public void finish() {

    }

}
