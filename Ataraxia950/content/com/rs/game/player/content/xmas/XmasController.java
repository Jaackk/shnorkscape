package com.rs.game.player.content.xmas;

import com.rs.game.Animation;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.content.xmas.XmasRiddles.Riddle;
import com.rs.game.player.controllers.Controller;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Utils;
import lombok.var;

public class XmasController extends Controller {

    /**
     * @author Noel Constants to be used throughout the event!
     */

    private final int helper = 9398;
    private final int snowballAnim = 2286;
    private final String[] welcome = {Colors.SALMON + "You step on the madness-ridden frozen tundra..",
            Colors.RCYAN + "You plop onto the snow, there is much to be done..",
            Colors.GREEN + "You re-emerge in a winter wonderland..."};

    private void remove() {
        removeControler();
    }

    @Override
    public boolean login() {
        start();
        return false;
    }

    @Override
    public boolean logout() {
        player.getXmas().inPresentBox = false;
        player.getXmas().isSnowman = false;
        player.getXmas().inThrow = false;
        return false;
    }

    @Override
    public void magicTeleported(int type) {
        remove();
        player.getAppearence().transformIntoNPC(-1);
        player.getXmas().isSnowman = false;
        player.getXmas().inPresentBox = false;
        player.getXmas().inThrow = false;
        player.getXmas().inXmas = false;
    }

    @Override
    public boolean sendDeath() {
        remove();
        player.getAppearence().transformIntoNPC(-1);
        player.getXmas().isSnowman = false;
        player.getXmas().inPresentBox = false;
        player.getXmas().inThrow = false;
        player.getXmas().inXmas = false;
        return true;
    }

    @Override
    public boolean handleItemOnObject(WorldObject object, Item item) {
        switch (object.getId()) {
            case 28318: // stag
            case 41723: // reindeer
                if (item.getId() != 30381 || player.getXmas().riddle == null || player.getXmas().riddle.getIndex() != 4) {
                    break;
                } else {
                    String name = (object.getId() == 41723) ? "reindeer" : "stag";
                    Riddle ours = player.getXmas().riddle;
                    if (ours.getIndex() == 4) {
                        player.faceObject(object);
                        player.setNextAnimation(new Animation(811));
                        player.getInventory().deleteItem(30381, 1);
                        player.getDialogueManager().startDialogue(new Dialogue() {
                            @Override
                            public void start() {
                                player.getInventory().deleteItem(30381, 1);
                                player.getXmas().riddle4 = true;
                                sendItemDialogue(30381, 1,
                                        Colors.GREEN + Colors.SHAD + "You feed a carrot to the " + name + "!</shad></col>");
                            }

                            @Override
                            public void run(int interfaceId, int componentId) {
                                XmasRiddles.finishRiddle(player);
                                finish();
                            }

                            @Override
                            public void finish() {
                                player.getInterfaceManager().closeChatBoxInterface();
                            }
                        });
                    }
                }
                break;

            case 47748: // throw snowball at tree
                if (item.getId() != 33590 || player.getXmas().riddle == null || player.getXmas().riddle.getIndex() != 3)
                    break;
                else {
                    Riddle ours = player.getXmas().riddle;
                    if (ours.getIndex() == 3) {
                        player.getXmas().riddle3 = true;
                        player.getXmas().snowballObject(object);
                        XmasRiddles.riddleReward(player);
                    }
                }
                break;

            case 47786:
                if (item.getId() == 33590) {
                    if (player.getXmas().freedSanta) {
                        player.sendMessage("You've already freed Santa!");
                        break;
                    }
                    player.getXmas().throwSnowball(object, null, false, () -> player.getDialogueManager().startDialogue("SantaFreedDialogue"));
                }
                break;
        }
        return true;
    }

    @Override
    public boolean canSummonFamiliar() {
        return false;
    }

    @Override
    public boolean processNPCClick1(final NPC npc) {
        switch (npc.getId()) {

            case 9398:
                player.getDialogueManager().startDialogue("XmasDialogue", npc.getId());
                return false;

            default:
                return true;
        }
    }

    @Override
    public boolean processCommand(String s, boolean b, boolean c) {
        if (s.contains("vote") || s.contains("claim") || s.contains("forums") || s.contains("discord")
                || s.contains("yell") || s.contains("store") || s.contains("donate") || s.contains("live")
                || s.contains("stream") || s.contains("twitch") || s.contains("rules") | s.contains("hiscores")
                || s.contains("update") || s.contains("answer") || player.canBan())
            return true;
        player.sendMessage(
                Colors.SALMON + Colors.SHAD
                        + "Commands are disabled in the event area for your safety! You may leave via the exit portal!",
                false);
        return false;
    }

    @Override
    public boolean processObjectClick1(final WorldObject object) {
        switch (object.getId()) {

            case 11899:
                player.getXmas().inPresentBox = false;
                player.getXmas().isSnowman = false;
                player.getXmas().inThrow = false;
                player.getXmas().bossTick = 0;
                player.setNextWorldTile(new WorldTile(2593, 5595, 0));
                return false;

            case 95001:
                if (!player.getXmas().inPresentBox) {
                    player.faceObject(object);
                    player.setNextWorldTile(new WorldTile(object.getX(), object.getY(), object.getPlane()));
                    player.addFreezeDelay(5000);
                    player.getXmas().inPresentBox = true;
                    Dialogue.closeNoContinueDialogue(player);
                }
                return false;

            case 65958:
                remove();
                player.getXmas().leave();
                return false;

            case 47766:
                if (!player.getXmas().riddle4 && !player.getInventory().containsItem(30381, 1)) {
                    if (!player.getInventory().hasFreeSlots()) {
                        player.sendMessage(Colors.RED + "You need a free inventory space to do this!");
                        return false;
                    }
                    player.getDialogueManager().startDialogue(new Dialogue() {

                        @Override
                        public void start() {
                            player.getInventory().addItem(new Item(30381));
                            sendItemDialogue(30381, 1, "You've found a carrot stowed away in the cupboard!");
                            stage = 1;
                        }

                        @Override
                        public void run(int interfaceId, int componentId) {
                            if (stage == 1)
                                finish();
                        }

                        @Override
                        public void finish() {
                            player.getInterfaceManager().closeChatBoxInterface();
                        }
                    });
                } else {
                    if (player.getXmas().riddle4)
                        player.sendMessage("You have no use for this, you have already completed this riddle!");
                    if (player.getInventory().containsItem(30381, 1))
                        player.sendMessage("You already have a carrot in your inventory!");
                }
                return false;

            case 47786:
                if (!(player.getEquipment().getWeaponId() == 33590 || player.getInventory().containsOneItem(33590))) {
                    player.sendMessage("You require a snowball in order to make Santa wake up!");
                    return false;
                } else if (player.getXmas().freedSanta) {
                    player.sendMessage("You've already freed Santa!");
                    return false;
                }
                player.getXmas().throwSnowball(object, null, false, () -> player.getDialogueManager().startDialogue("SantaFreedDialogue"));
                return false;

            case 47774: // gate to mansion
                if (player.getXmas().snowmenKilled >= 30 && player.getXmas().finishedRiddles()) {
                    if (!player.withinDistance(new WorldTile(object.getX(), object.getY(), 0), 2))
                        player.addWalkSteps(2674, 5661);
                    player.faceObject(object);
                    player.getXmas().traverse(true);
                    player.sendMessage(Colors.RCYAN + "You wander into the winter woodlands and stumble upon a mansion.",
                            true);
                } else
                    player.getDialogueManager().startDialogue("SimpleMessage",
                            "You'll need to kill at least 30 snowmen and do all of the riddles!");
                return false;

            case 47775: // gate to dining area
                if (!player.withinDistance(new WorldTile(object.getX(), object.getY(), 0), 2))
                    player.addWalkSteps(2593, 5577);
                player.faceObject(object);
                player.getXmas().traverse(false);
                player.sendMessage(Colors.RCYAN + "You walk through the snowy woods into a town bustling with festivities!",
                        true);
                return false;

		/*
			For the memories :)

			case 47776:
			if (player.addWalkSteps(2593, 5595))
				player.faceObject(object);
			if (!player.hasItem(new Item(744))) {
				player.getInventory().addItem(new Item(744));
				player.sendMessage("You receive a heart crystal, to help you in your endeavors! Merry Christmas. Thanks for everything -- Ataraxia Management", false);
			}
			player.getXmas().bossTick = Utils.currentTimeMillis();
			player.setNextWorldTile(new WorldTile(2593, 5600, 1));
			return false;
		*/

            case 28296:
                if (Utils.currentTimeMillis() - player.getXmas().snowballTick < 2)
                    return false;
                if (!player.getInventory().hasFreeSlots()) {
                    player.sendMessage("You do not have enough inventory space!");
                    return false;
                }
                if (player.withinDistance(new WorldTile(object.getX(), object.getY(), 0))) {
                    player.faceObject(object);
                    player.setNextAnimation(new Animation(snowballAnim));
                    WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                            player.setNextAnimation(new Animation(-1));
                            player.getInventory().addItem(new Item(33590));
                            player.sendMessage("You pick up a snowball from the pile!", true);
                            player.getXmas().snowballTick = Utils.currentTimeMillis();
                        }
                    }, 0);
                } else
                    player.addWalkSteps(object.getX(), object.getY());
                return false;

            default:
                return true;
        }
    }

    @Override
    public void start() {
        var softStart = false;
        if (getArguments().length > 0) {
            softStart = (boolean) getArguments()[0];
        }
        if (!softStart) {
            String gender = player.getAppearence().isMale() ? "sir" : "ma'am";
            player.getXmas().enter();
            if (!player.getXmas().intro) {

                /** Snowball initialization vectors */
                player.getXmas().damage = 1;
                player.getXmas().speed = false;
                player.getXmas().snowmenKilled = 0;
                player.getXmas().snowEnergy = 0;
                player.getXmas().bossKilled = 0;

                /** Intro to talk to the scared snow queen! */
                player.getHintIconsManager().addHintIcon(2655, 5679, 0, 120, 0, 0, -1, false);
                Dialogue.sendNPCDialogueNoContinue(player, helper, Dialogue.SCARED,
                        "Please, " + gender + " help me in my perils!");
            } else
                player.sendMessage(Colors.SHAD + welcome[Utils.random(2)] + Colors.ESHAD);
            player.setNextFaceWorldTile(new WorldTile(2593, 5581, 0));
        }
    }
}
