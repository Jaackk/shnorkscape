package com.rs.game.player.dialogue.impl.heartofgielinor;

import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.activities.instances.GregorovicInstance;
import com.rs.game.activities.instances.HelwyrInstance;
import com.rs.game.activities.instances.Instance;
import com.rs.game.activities.instances.TwinFuriesInstance;
import com.rs.game.activities.instances.VindictaInstance;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.content.HeartOfGielinor;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.InputIntegerEvent;
import com.rs.utils.InputNameEvent;
import com.rs.utils.Utils;

public class HeartOfGielinorInstanceD extends Dialogue {

    public static final WorldTile CENTER = new WorldTile(3199, 6970, 1);
    private Instance instance, hardInstance;
    private int duration, speed, limit, password = -1, bossId;
    private boolean hardMode, isInInstance;

    @Override
    public void start() {
        this.bossId = (int) parameters[0];
        if (!player.withinDistance(CENTER, 200)) {
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
            sendOptionsDialogue("Which mode would you like to enter?", "Normal mode", "<col=ff0000>Hard mode");
            duration = 60;
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
            sendOptionsDialogue("What would you like to do?", "Start an instance", "Join an instance");
        else
            sendOptionsDialogue("What would you like to do?", "Destroy my instance", "Join an instance", "Enter my instance");
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
        case -1:
            switch (componentId) {
            case OPTION_1:
                if (isInInstance) {
                    if (player.getCurrentInstance() != null) {
                        if (player.getCurrentInstance() instanceof HelwyrInstance) {
                            if (player.getY() <= player.getCurrentInstance().getWorldTile(25, 47).getY()) {
                                player.setNextWorldTile(player.getCurrentInstance().getWorldTile(22, 49));
                                end();
                                return;
                            } else {
                                HeartOfGielinor.switchInterfaces(player, player.getCurrentInstance(), ((HelwyrInstance) player.getCurrentInstance()).getHelwyr(), true);
                                player.setNextWorldTile(player.getCurrentInstance().getWorldTile(25, 46));
                                end();
                                return;
                            }
                        } else if (player.getCurrentInstance() instanceof VindictaInstance) {
                            if (player.getX() <= player.getCurrentInstance().getWorldTile(39, 38).getX()) {
                                player.setNextWorldTile(player.getCurrentInstance().getWorldTile(41, 41));
                                end();
                                return;
                            } else {
                                HeartOfGielinor.switchInterfaces(player, player.getCurrentInstance(), ((VindictaInstance) player.getCurrentInstance()).getVindicta(), true);
                                player.setNextWorldTile(player.getCurrentInstance().getWorldTile(38, 38));
                                end();
                                return;
                            }
                        } else if (player.getCurrentInstance() instanceof TwinFuriesInstance) {
                            if (player.getX() < player.getCurrentInstance().getWorldTile(43, 20).getX()) {
                                HeartOfGielinor.switchInterfaces(player, player.getCurrentInstance(), ((TwinFuriesInstance) player.getCurrentInstance()).getAvaryss(), false);
                                player.setNextWorldTile(player.getCurrentInstance().getWorldTile(43, 20));
                                end();
                                return;
                            } else {
                                HeartOfGielinor.switchInterfaces(player, player.getCurrentInstance(), ((TwinFuriesInstance) player.getCurrentInstance()).getAvaryss(), true);
                                player.setNextWorldTile(player.getCurrentInstance().getWorldTile(40, 21));
                                end();
                                return;
                            }
                        } else if (player.getCurrentInstance() instanceof GregorovicInstance) {
                            if (player.getX() > player.getCurrentInstance().getWorldTile(31, 30).getX()) {
                                player.setNextWorldTile(player.getCurrentInstance().getWorldTile(30, 30));
                                end();
                                return;
                            } else {
                                HeartOfGielinor.switchInterfaces(player, player.getCurrentInstance(), ((GregorovicInstance) player.getCurrentInstance()).getGregorovic(), true);
                                player.setNextWorldTile(player.getCurrentInstance().getWorldTile(33, 33));
                                end();
                                return;
                            }
                        }
                    }
                    end();
                    return;
                }
                if (instance == null && hardInstance == null) {
                    stage = 9;
                    sendOptionsDialogue("Select the respawn speed", "Slow", "Medium", "Fast", "Very fast");
                } else {
                    stage = 20;
                    sendOptionsDialogue("Are you sure you wish to destroy it?", "Yes, destroy the instance.", "No. Keep the instance.");
                    return;
                }
                break;
            case OPTION_2:
                end();
                player.sendInputName("Whose instance would you like to enter?", new InputNameEvent() {
                    @Override
                    public void run(Player player) {
                        String username = getString().toLowerCase();
                        player.getPackets().sendGlobalString(356, Utils.formatPlayerNameForDisplay(username));

                        for (int i = 0; i < World.getInstances().size(); i++) {
                            if (World.getInstances().get(i) != null && World.getInstances().get(i).getBoss() == bossId && World.getInstances().get(i).getOwner().getDisplayName().equalsIgnoreCase(username)) {
                                if (World.getInstances().get(i).isHardMode()) {
                                    hardInstance = World.getInstances().get(i);
                                } else {
                                    instance = World.getInstances().get(i);
                                }
                            }
                        }
                        if (instance == null && hardInstance == null) {
                            player.getDialogueManager().startDialogue("SimpleMessage", Utils.formatPlayerNameForDisplay(username) + " does not currently have an active instance.");
                            return;
                        } else if (instance != null && hardInstance != null) {
                            player.getDialogueManager().startDialogue("InstanceJoiningD", instance, hardInstance);
                            return;
                        }
                        if (instance != null)
                            instance.enterInstance(player);
                        else
                            hardInstance.enterInstance(player);
                    }
                });
                break;
            case OPTION_3:
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
                end();
                player.sendInputInteger("How many players will you limit the instance to?", new InputIntegerEvent() {
                    @Override
                    public void run(Player player) {
                        player.getDialogueManager().startDialogue("HeartOfGielinorInstanceD", bossId, speed, getInteger());
                    }
                });

                break;
            }
            if (componentId != OPTION_5) {
                sendOptionsDialogue("Protect instance with a password?", "Yes, add a password.", "No, don't add a password.");
            }
            break;
        case 12:
            if (componentId == OPTION_1) {
                end();
                player.sendInputInteger("Select a password in digits.", new InputIntegerEvent() {
                    @Override
                    public void run(Player player) {
                        player.getDialogueManager().startDialogue("HeartOfGielinorInstanceD", bossId, speed, limit, getInteger());
                    }
                });
                return;
            }
            password = -1;
            duration = 60;
            if (componentId != OPTION_1)
                sendOptionsDialogue("Which mode would you like to enter?", "Normal mode", "<col=ff0000>Hard mode");
            break;
        case 13:
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

            if (player.getRights() != 2 && !player.getPerkManager().hasPerkActive(DonationPerk.GWD_SPECIALIST) && player.getHeart().getKillcount(bossId) < (player.getHeart().getReputation(bossId) >= 4500 ? 20 : 40)) {
                player.sendMessage("You need at least " + (player.getHeart().getReputation(bossId) >= 4500 ? 20 : 40) + " " + HeartOfGielinor.getGod(bossId) + " kills to start an instance.");
                end();
                return;
            }
            sendOptionsDialogue("Create the instance?", "Yes, create the instance.", "No, I've changed my mind.");
            break;
        case 14:
            if (componentId == OPTION_1) {
                if (bossId == HeartOfGielinor.ZAMORAK)
                    instance = new TwinFuriesInstance(player, duration, speed, limit, password, bossId, hardMode);
                else if (bossId == HeartOfGielinor.SLISKE)
                    instance = new GregorovicInstance(player, duration, speed, limit, password, bossId, hardMode);
                else if (bossId == HeartOfGielinor.ZAROS)
                    instance = new VindictaInstance(player, duration, speed, limit, password, bossId, hardMode);
                else
                    instance = new HelwyrInstance(player, duration, speed, limit, password, bossId, hardMode);
                if (!player.getPerkManager().hasPerkActive(DonationPerk.GWD_SPECIALIST))
                    player.getHeart().setKillcount(bossId, player.getHeart().getKillcount(bossId) - (player.getHeart().getReputation(bossId) >= 4500 ? 20 : 40));
                instance.constructInstance();
            }
            end();
            break;
        case 20:
            if (componentId == OPTION_1) {
                if (instance != null) {
                    instance.getPlayers().forEach(p -> {
                        if (p.withinDistance(instance.getWorldTile(20, 20), 100))
                            p.setNextWorldTile(instance.getOutsideCoordinates());
                    });
                    instance.destroyInstance();
                }
                if (hardInstance != null) {
                    hardInstance.getPlayers().forEach(p -> {
                        if (p.withinDistance(hardInstance.getWorldTile(20, 20), 100))
                            p.setNextWorldTile(hardInstance.getOutsideCoordinates());
                    });
                    hardInstance.destroyInstance();
                }
                player.sendMessage("You've destroyed your instance.");
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
