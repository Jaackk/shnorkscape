package com.rs.game.player.dialogue.impl;

import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.activities.instances.DagannothKingsInstance;
import com.rs.game.activities.instances.Instance;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.InputIntegerEvent;
import com.rs.utils.InputNameEvent;
import com.rs.utils.Utils;

import java.util.ArrayList;

public class DagannothKingsInstanceD extends Dialogue {

    private Instance instance;
    private int duration, speed, limit, password = -1, bossId;

    @Override
    public void start() {
        this.bossId = (int) parameters[0];
        if (parameters.length > 1) {
            this.speed = (int) parameters[1];
            if (parameters.length > 2)
                this.limit = (int) parameters[2];
            if (parameters.length > 3)
                this.password = (int) parameters[3];
        }
        if (parameters.length > 3) {
            sendOptionsDialogue("Select the duration of the instance.", "Half an hour.", "One hour.", "An hour and a half.", "Two hours.");
            stage = 13;
            return;
        } else if (parameters.length > 2) {
            sendOptionsDialogue("Protect instance with a password?", "Yes, add a password.", "No, don't add a password.");
            stage = 12;
            return;
        }
        for (int i = 0; i < World.getInstances().size(); i++) {
            if (World.getInstances().get(i) != null && World.getInstances().get(i).getBoss() >= 2880 && World.getInstances().get(i).getBoss() <= 2883 && World.getInstances().get(i).getOwner().getDisplayName().equalsIgnoreCase(player.getDisplayName())) {
                instance = World.getInstances().get(i);
                break;
            }
        }
        if (instance == null)
            sendOptionsDialogue("What would you like to do?", "Climb the ladder", "Start an instance", "Join an instance");
        else
            sendOptionsDialogue("What would you like to do?", "Climb the ladder", "Destroy my instance", "Join an instance", "Enter my instance");
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
        case -1:
            switch (componentId) {
            case OPTION_1:
                // player.setNextWorldTile(new WorldTile(1912, 4367, 0));
                player.setNextWorldTile(new WorldTile(2900, 4446, 0));
                end();
                break;
            case OPTION_2:
                if (instance != null) {
                    if (instance != null) {
                        instance.getPlayers().removeIf(p -> {
                            if (p.withinDistance(instance.getWorldTile(20, 20), 100))
                                p.setNextWorldTile(instance.getOutsideCoordinates());
                            return true;
                        });
                        instance.destroyInstance();
                    }
                    player.sendMessage("You've destroyed your instance.");
                    end();
                    return;
                }
                stage = 9;
                sendOptionsDialogue("Select the respawn speed", "Slow", "Medium", "Fast", "Very fast");
                break;
            case OPTION_3:
                end();
                player.sendInputName("Whose instance would you like to enter?", new InputNameEvent() {
                    @Override
                    public void run(Player player) {
                        final String value = getString();
                        player.getPackets().sendGlobalString(356, Utils.formatPlayerNameForDisplay(value));
                        Instance instance = null, hardInstance = null;
                        if (bossId >= 2880 && bossId <= 2883) {
                            ArrayList<Instance> ins = new ArrayList<Instance>();
                            for (int i = 0; i < World.getInstances().size(); i++) {
                                if (World.getInstances().get(i) != null && World.getInstances().get(i).getBoss() >= 2880 && World.getInstances().get(i).getBoss() <= 2883 && World.getInstances().get(i).getOwner().getDisplayName().equalsIgnoreCase(value)) {
                                    ins.add(World.getInstances().get(i));
                                }
                            }
                            if (ins.size() > 1) {
                                player.getDialogueManager().startDialogue("DagannothInstanceJoiningD", ins);
                                return;
                            }
                            if (ins.size() > 0 && ins.get(0) != null) {
                                ins.get(0).enterInstance(player);
                            }
                            return;
                        }
                        for (int i = 0; i < World.getInstances().size(); i++) {
                            if (World.getInstances().get(i) != null && World.getInstances().get(i).getBoss() == bossId && World.getInstances().get(i).getOwner().getDisplayName().equalsIgnoreCase(value)) {
                                if (World.getInstances().get(i).isHardMode()) {
                                    hardInstance = World.getInstances().get(i);
                                } else {
                                    instance = World.getInstances().get(i);
                                }
                            }
                        }
                        if (instance == null && hardInstance == null) {
                            player.getDialogueManager().startDialogue("SimpleMessage", Utils.formatPlayerNameForDisplay(value) + " does not currently have an active instance.");
                            return;
                        } else if (instance != null && hardInstance != null) {
                            player.getDialogueManager().startDialogue("InstanceJoiningD", instance, hardInstance);
                            return;
                        }
                        if (instance != null) {
                            instance.enterInstance(player);
                        } else {
                            hardInstance.enterInstance(player);
                        }
                    }
                });
                break;
            case OPTION_4:
                ArrayList<Instance> ins = new ArrayList<Instance>();
                for (int i = 0; i < World.getInstances().size(); i++) {
                    if (World.getInstances().get(i) != null && World.getInstances().get(i).getBoss() >= 2880 && World.getInstances().get(i).getBoss() <= 2883 && World.getInstances().get(i).getOwner().getDisplayName().equalsIgnoreCase(player.getDisplayName()))
                        ins.add(World.getInstances().get(i));
                }
                if (ins.size() > 1) {
                    player.getDialogueManager().startDialogue("DagannothInstanceJoiningD", ins);
                    return;
                }
                if (ins.size() > 0 && ins.get(0) != null)
                    ins.get(0).enterInstance(player);
                end();
                break;
            }
            break;
        case 10:
            switch (componentId) {
            case OPTION_1:
                speed = 120;
                break;
            case OPTION_2:
                speed = 60;
                break;
            case OPTION_3:
                speed = 30;
                break;
            case OPTION_4:
                speed = 5;
                break;
            }
            sendOptionsDialogue("Select the players limit", "No limit", "3", "5", "10", "Custom amount");
            break;
        case 11:
            switch (componentId) {
            case OPTION_1:
                limit = 0;
                break;
            case OPTION_2:
                limit = 3;
                break;
            case OPTION_3:
                limit = 5;
                break;
            case OPTION_4:
                limit = 10;
                break;
            case OPTION_5:
                player.sendInputInteger("How many players will you limit the instance to?", new InputIntegerEvent() {
                    @Override
                    public void run(Player player) {
                        player.getDialogueManager().startDialogue("DagannothKingsInstanceD", bossId, speed, getInteger());
                    }
                });
                end();
                break;
            }
            if (componentId != OPTION_5) {
                sendOptionsDialogue("Protect instance with a password?", "Yes, add a password.", "No, don't add a password.");
            }
            break;
        case 12:
            if (componentId == OPTION_1) {
                player.sendInputInteger("Select a password in digits.", new InputIntegerEvent() {
                    @Override
                    public void run(Player player) {
                        player.getDialogueManager().startDialogue("DagannothKingsInstanceD", bossId, speed, limit, getInteger());
                    }
                });
                end();
                return;
            }
            password = -1;
            if (componentId != OPTION_1) {
                sendOptionsDialogue("Select the duration of the instance.", "Half an hour.", "One hour.", "An hour and a half.", "Two hours.");
            }
            break;
        case 13:
            switch (componentId) {
            case OPTION_1:
                duration = 30;
                break;
            case OPTION_2:
                duration = 60;
                break;
            case OPTION_3:
                duration = 90;
                break;
            case OPTION_4:
                duration = 120;
                break;
            }
            sendOptionsDialogue("Which Dagannoth kings would you like to instance?", "All three of the kings", "Dagannoth Supreme", "Dagannoth Rex", "Dagannoth Prime");
            break;
        case 14:
            bossId = componentId == OPTION_1 ? 2880 : componentId == OPTION_2 ? 2881 : componentId == OPTION_3 ? 2883 : 2882;
            instance = null;
            for (int i = 0; i < World.getInstances().size(); i++) {
                if (World.getInstances().get(i) != null && World.getInstances().get(i).getBoss() == bossId && World.getInstances().get(i).getOwner().getDisplayName().equalsIgnoreCase(player.getDisplayName()))
                    instance = World.getInstances().get(i);
            }
            if (instance != null) {
                player.sendMessage("You already have an active instance to this boss.");
                end();
                return;
            }

            sendOptionsDialogue("The instance will cost " + Utils.formatNumber((player.getPerkManager().hasPerkActive(DonationPerk.THE_DISCOUNTER) ? 25_000 : 50_000) * duration) + " coins to build.<br>Are you sure you wish to create this instance?", "Yes, create the instance.", "No, I've changed my mind.");
            break;
        case 15:
            if (componentId == OPTION_1) {
                if (player.getMoneyPouch().removeAmount((player.getPerkManager().hasPerkActive(DonationPerk.THE_DISCOUNTER) ? 25_000 : 50_000) * duration)) {
                    instance = new DagannothKingsInstance(player, duration, speed, limit, password, bossId, false);
                    instance.constructInstance();
                } else {
                    player.sendMessage("You don't have enough coins to build this instance.");
                }
            }
            end();
            break;
        }
        stage++;
    }

    @Override
    public void finish() {

    }

}
