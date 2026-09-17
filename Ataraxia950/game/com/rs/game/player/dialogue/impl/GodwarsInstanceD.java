package com.rs.game.player.dialogue.impl;

import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.activites.GodWarsBosses;
import com.rs.game.activites.ZarosGodwars;
import com.rs.game.activities.instances.GodwarsInstance;
import com.rs.game.activities.instances.Instance;
import com.rs.game.activities.instances.NexInstance;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.ZGDController;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.InputIntegerEvent;
import com.rs.utils.InputNameEvent;
import com.rs.utils.Utils;

public class GodwarsInstanceD extends Dialogue {

    private Instance instance, hardInstance;
    private int duration, speed, limit, password = -1, bossId;
    private boolean hardMode, isInInstance;

    @Override
    public void start() {
        this.bossId = (int) parameters[0];
        if (!GodWarsBosses.isAtGodwars(player)) {
            sendOptionsDialogue("What would you like to do?", "Enter the room", "Join a different instance");
            isInInstance = true;
            return;
        }
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
            if (World.getInstances().get(i) != null && World.getInstances().get(i).getBoss() == bossId && World.getInstances().get(i).getOwner().getDisplayName().equalsIgnoreCase(player.getDisplayName())) {
                if (World.getInstances().get(i).isHardMode())
                    hardInstance = World.getInstances().get(i);
                else
                    instance = World.getInstances().get(i);
                break;
            }
        }
        if (instance == null && hardInstance == null)
            sendOptionsDialogue("What would you like to do?", "Enter the room", "Start an instance", "Join an instance");
        else 
            sendOptionsDialogue("What would you like to do?", "Enter the room", "Start an instance", "Join an instance", "Enter my instance", "Destroy my instance");
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
        case -1:
            switch (componentId) {
            case OPTION_1:
                switch (bossId) {
                case 13447:
                    player.setNextWorldTile(new WorldTile(player.getX() + 2, player.getY(), player.getPlane()));
                    player.getControlerManager().startControler(ZGDController.class.getSimpleName());
                    ZarosGodwars.addPlayer(player);
                    break;
                case 6260:
                    player.setNextWorldTile(new WorldTile(player.getX() + 1, player.getY(), player.getPlane()));
                    break;
                case 6203:
                    player.setNextWorldTile(new WorldTile(player.getX(), player.getY() - 1, player.getPlane()));
                    break;
                case 6247:
                    player.setNextWorldTile(new WorldTile(player.getX(), player.getY() - 1, player.getPlane()));
                    break;
                case 6222:
                    player.setNextWorldTile(new WorldTile(player.getX(), player.getY() + 1, player.getPlane()));
                    break;
                }
                end();
                break;
            case OPTION_2:
                if (isInInstance) {
                    end();
                    player.sendInputName("Whose instance would you like to enter?", new InputNameEvent() {
                        @Override
                        public void run(Player player) {
                            final String value = getString();
                            player.getPackets().sendGlobalString(356, Utils.formatPlayerNameForDisplay(value));
                            Instance instance = null, hardInstance = null;
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
            case OPTION_4: {
                Instance instance = null, hardInstance = null;
                for (int i = 0; i < World.getInstances().size(); i++) {
                    if (World.getInstances().get(i) != null && World.getInstances().get(i).getBoss() == bossId && World.getInstances().get(i).getOwner().getDisplayName().equalsIgnoreCase(player.getDisplayName())) {
                        if (World.getInstances().get(i).isHardMode())
                            hardInstance = World.getInstances().get(i);
                        else
                            instance = World.getInstances().get(i);
                    }
                }
                if (instance != null && hardInstance != null) {
                    player.getDialogueManager().startDialogue("InstanceJoiningD", instance, hardInstance);
                    return;
                }
                if (instance != null)
                    instance.enterInstance(player);
                else
                    hardInstance.enterInstance(player);
                end();
                break;
            }
            case OPTION_5:{
                Instance instance = null, hardInstance = null;
                for (int i = 0; i < World.getInstances().size(); i++) {
                    if (World.getInstances().get(i) != null && World.getInstances().get(i).getBoss() == bossId && World.getInstances().get(i).getOwner().getDisplayName().equalsIgnoreCase(player.getDisplayName())) {
                        if (World.getInstances().get(i).isHardMode())
                            hardInstance = World.getInstances().get(i);
                        else
                            instance = World.getInstances().get(i);
                    }
                }
                if (instance != null) {
                    final Instance normalInstance = instance;
                    normalInstance.getPlayers().forEach(p -> {
                            p.setNextWorldTile(normalInstance.getOutsideCoordinates());
                    });
                    instance.destroyInstance();
                }
                if (hardInstance != null) {
                    final Instance hinstance = hardInstance;
                    hinstance.getPlayers().forEach(p -> {
                            p.setNextWorldTile(hinstance.getOutsideCoordinates());
                    });
                    hardInstance.destroyInstance();
                }
                player.sendMessage("You've destroyed your instance.");
                end();
                break;
            }
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
                        player.getDialogueManager().startDialogue("GodwarsInstanceD", bossId, speed, getInteger());
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
                        player.getDialogueManager().startDialogue("GodwardInstanceD", bossId, speed, limit, getInteger());
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
            sendOptionsDialogue("Which mode would you like to enter?", "Normal mode", "<col=ff0000>Hard mode");
            break;
        case 14:
            hardMode = componentId != OPTION_1;
            instance = null;
            for (int i = 0; i < World.getInstances().size(); i++) {
                if (World.getInstances().get(i) != null && World.getInstances().get(i).getBoss() == bossId && World.getInstances().get(i).getOwner().getDisplayName().equalsIgnoreCase(player.getDisplayName())) {
                    if (World.getInstances().get(i).isHardMode() == hardMode)
                        instance = World.getInstances().get(i);
                }
            }
            if (instance != null) {
                player.sendMessage("You already have an active instance to this boss.");
                end();
                return;
            }
            sendOptionsDialogue("The instance will cost " + (player.getPerkManager().hasPerkActive(DonationPerk.THE_DISCOUNTER) ? 25000 : 37500) * duration * (hardMode ? 2 : 1) + " coins to build.<br>Are you sure you wish to create this instance?", "Yes, create the instance.", "No, I've changed my mind.");
            break;
        case 15:
            if (componentId == OPTION_1) {
                if (player.getMoneyPouch().removeAmount((player.getPerkManager().hasPerkActive(DonationPerk.THE_DISCOUNTER) ? 25000 : 37500) * duration * (hardMode ? 2 : 1))) {
                    if (bossId == 13447) {
                        instance = new NexInstance(player, duration, speed, limit, password, bossId, hardMode, false);
                        player.getControlerManager().startControler(ZGDController.class.getSimpleName());
                    } else
                        instance = new GodwarsInstance(player, duration, speed, limit, password, bossId, hardMode);
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
