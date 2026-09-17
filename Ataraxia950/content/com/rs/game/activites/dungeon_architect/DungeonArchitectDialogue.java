package com.rs.game.activites.dungeon_architect;

import com.rs.Settings;
import com.rs.game.World;
import com.rs.game.map.bossInstance.BossInstance;
import com.rs.game.map.bossInstance.BossInstanceHandler;
import com.rs.game.map.bossInstance.InstanceSettings;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.InputNameEvent;
import com.rs.utils.Utils;

public final class DungeonArchitectDialogue extends Dialogue {

    private boolean paying;

    @Override
    public void start() {

        sendOptionsDialogue("Select an option.", "Start a new instance.", "Join an existing instance.");
        stage = 0;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -1:
                end();
                break;
            case 0:
                if (componentId == OPTION_1) {
                    if (player.getPerkManager().hasPerkActive(DonationPerk.DUNGEON_ARCHITECT) || Settings.TEST_SERVER_MODE) {
                        if (BossInstanceHandler.inInstance(BossInstanceHandler.Boss.Dungeon_Architect, player)) {
                            sendDialogue("You already have an active instance!");
                            stage = -1;
                            return;
                        }
                        sendOptions();
                    } else {
                        sendDialogue("You must have the 'Dungeon Architect' perk active in order to start instances.");
                        stage = -1;
                        return;
                    }
                } else if (componentId == OPTION_2) {
                    if (player.getPerkManager().hasPerkActive(DonationPerk.DUNGEON_ARCHITECT)) {
                        sendEnter();
                    } else {
                        sendDialogue("You do not have the 'Dungeon Architect' perk activated.",
                                "To join an instance, you will need to pay a fee of " + Utils.formatNumber(DungeonArchitect.COINS_COST) + " coins. Are you okay with this?");
                        stage = 2;
                    }
                }
                break;
            case 1:
                player.sendInputName("Enter the name", new InputNameEvent() {
                    @Override
                    public void run(Player player) {
                        Player found = World.getPlayerByDisplayName(getString());
                        boolean removeCoins = false;
                        if (paying) {
                            if (player.getInventory().containsCoins(DungeonArchitect.COINS_COST)) {
                                removeCoins = true;
                                paying = false;
                            } else {
                                sendDialogue("You do not have enough coins to pay the fee.");
                                stage = -1;
                                return;
                            }
                        }
                        if (found != null && player.isGroupIronman() && !player.canGimInteractWith(found)) {
                            sendDialogue("You can only join instances created by your group members.");
                            stage = -1;
                            return;
                        }
                        if (found != null && BossInstanceHandler.joinInstance(player, BossInstanceHandler.Boss.Dungeon_Architect, found.getUsername(), false) != null) {
                            if (removeCoins) {
                                player.getInventory().deleteCoins(DungeonArchitect.COINS_COST);
                            }
                            end();
                        } else {
                            sendDialogue("Could not join instance for '" + getString() + "'.");
                            stage = -1;
                        }
                    }
                });
                break;
            case 2:
                sendOptionsDialogue("Select an option.", "Yes", "No");
                stage = 3;
                break;
            case 3:
                if (componentId == OPTION_1) {
                    paying = true;
                    sendEnter();
                } else if (componentId == OPTION_2) {
                    end();
                }
                break;
        }
    }

    @Override
    public void finish() {

    }

    private void sendEnter() {
        sendDialogue("Enter the display name of the player's instance you would like to join.");
        stage = 1;
    }

    private void sendOptions() {
        player.getDialogueManager().startDialogue(new Dialogue() {

            @Override
            public void start() {
                sendOptions();
            }

            @Override
            public void run(int interfaceId, int componentId) {
                switch (stage) {
                    case -1:
                        sendOptions();
                        break;
                    case 0:
                        if (componentId == OPTION_1) {
                            start(DungeonArchitectMonster.SKELETAL_WARRIOR);
                        } else if (componentId == OPTION_2) {
                            start(DungeonArchitectMonster.ICE_ELEMENTAL);
                        } else if (componentId == OPTION_3) {
                            start(DungeonArchitectMonster.ICE_WARRIOR);
                        } else if (componentId == OPTION_4) {
                            start(DungeonArchitectMonster.ICEFIEND);
                        } else if (componentId == OPTION_5) {
                            sendOptionsDialogue("Select an option.",
                                    "Level 50 ~ Earth Warrior",
                                    "Level 40 ~ Ghost",
                                    "Level 30 ~ Armoured Zombie",
                                    "Level 20 ~ Skeleton",
                                    Colors.RED + "Next...");
                            stage = 1;
                        }
                        break;
                    case 1:
                        if (componentId == OPTION_1) {
                            start(DungeonArchitectMonster.EARTH_WARRIOR);
                        } else if (componentId == OPTION_2) {
                            start(DungeonArchitectMonster.GHOST);
                        } else if (componentId == OPTION_3) {
                            start(DungeonArchitectMonster.ARMOURED_ZOMBIE);
                        } else if (componentId == OPTION_4) {
                            start(DungeonArchitectMonster.SKELETON);
                        } else if (componentId == OPTION_5) {
                            sendOptionsDialogue("Select an option.",
                                    "Level 10 ~ Dungeon Spider",
                                    "Level 1 ~ Dungeon Rat",
                                    Colors.RED + "Home...");
                            stage = 2;
                        }
                        break;
                    case 2:
                        if (componentId == OPTION_1) {
                            start(DungeonArchitectMonster.DUNGEON_SPIDER);
                        } else if (componentId == OPTION_2) {
                            start(DungeonArchitectMonster.DUNGEON_RAT);
                        } else if (componentId == OPTION_3) {
                            sendOptions();
                        }
                        break;
                }
            }

            @Override
            public void finish() {

            }

            private void sendOptions() {
                sendOptionsDialogue("Select an option.",
                        "Level 90 ~ Skeletal Warrior",
                        "Level 80 ~ Ice Elemental",
                        "Level 70 ~ Ice Warrior",
                        "Level 60 ~  Icefiend",
                        Colors.RED + "Next...");
                stage = 0;
            }

            private void start(DungeonArchitectMonster monsters) {
                if (BossInstanceHandler.inInstance(BossInstanceHandler.Boss.Dungeon_Architect, player)) {
                    end();
                } else if (monsters.level > player.getSkills().getLevelForXp(Skills.DUNGEONEERING)) {
                    sendDialogue("You need a level of " + monsters.level + " to select this option.");
                    stage = -1;
                } else {
                    player.monsterType = monsters;
                    InstanceSettings settings = new InstanceSettings(BossInstanceHandler.Boss.Dungeon_Architect);
                    settings.setMaxPlayers(3);
                    settings.setProtection(BossInstance.FFA);
                    settings.setCreationTime(Utils.currentTimeMillis());
                    BossInstanceHandler.createInstance(player, settings);
                }
            }
        });
    }
}