package com.rs.game.activites.quest.root_of_evil;

import com.google.common.collect.ImmutableList;
import com.rs.game.Animation;
import com.rs.game.MapInstance;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activites.dnd.eviltree.entity.EvilRootObject;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.npc.StillNPC;
import com.rs.game.player.Skills;
import com.rs.game.player.content.FadingScreen;
import com.rs.game.player.controllers.Controller;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.player.dialogue.impl.SinglePlayerDialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Logger;
import com.rs.utils.Utils;
import lombok.val;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class VaultController extends Controller {

    private static final ImmutableList<String> INSPECT_MUTANT_TREES = ImmutableList.of(
            "Why does it look so weird?",
            "I wonder what happened to it...",
            "It looks mutated..."
    );
    private final Queue<WorldObject> objectSpawns = new ConcurrentLinkedQueue<>();
    private WorldObject carcassObject;
    private volatile NPC evilMage;
    final MapInstance instance = new MapInstance(297, 1225, 1, 1);
    int vaultStage = 0;
    int logsLoaded = 0;
    boolean lootedChest = false;
    boolean finished = false;

    @Override
    public boolean processMagicTeleport(WorldTile toTile) {
        player.sendMessage("A magical force blocks your teleport.");
        return false;
    }

    @Override
    public void magicTeleported(int type) {
        player.getControlerManager().forceStop();
        super.magicTeleported(type);
    }

    @Override
    public void forceClose() {
        if (!finished) {
            player.getInventory().deleteItem(RootOfEvil.EVIL_WATERING_CAN_ID, 1);
        }
        player.getInventory().deleteItem(RootOfEvil.SELF_DESTRUCT_KEY_ID, 1);
        player.getHintIconsManager().removeUnsavedHintIcon();
        player.setNextWorldTile(new WorldTile(3005, 3203, 0));
        player.getPackets().sendMiniMapStatus(0);
        if (player.getAppearence().isHidden()) {
            player.getAppearence().switchHidden();
        }
        player.getAppearence().setRenderEmote(-1);
        player.unlock();
        player.getInterfaceManager().closeChatBoxInterface();
        vaultStage = 0;
        if (evilMage != null) {
            World.removeNPC(evilMage);
            evilMage = null;
        }
        for (; ; ) {
            WorldObject next = objectSpawns.poll();
            if (next == null)
                break;
            try {
                World.removeObject(next);
            } catch (Exception e) {
                Logger.getGlobal().catching(e);
            }
        }
        instance.destroy(null);
    }

    @Override
    public void start() {
        player.lock();
        FadingScreen.fade(player, 600, this::setup);
    }

    @Override
    public boolean processObjectClick1(WorldObject object) {
        if (object.getId() == 17223) {
            player.sendMessage("You fail to climb back up...");
        } else if (object.getId() == 17235) {
            if (vaultStage == 0) {
                player.lock();
                player.getAppearence().switchHidden();
            } else {
                player.sendMessage("I don't need to hide right now.");
            }
        } else if (object.getId() == 17272) {
            Dialogue.sendSingleDialogue(player, "The sign reads:",
                    "\"POWER THE ROOT BY LOADING " + RootOfEvil.EVIL_LOGS_REQ + " EVIL LOGS INTO THE GENERATOR.",
                    "THEN PROGRAM THE GENERATOR WITH ONE OF THE FOLLOWING CODES:",
                    "CODE A175Z ~ START",
                    "CODE Z542H ~ THROTTLE",
                    "CODE 1LEB4 ~ SELF-DESTRUCT\"");
            player.sendMessage("That self-destruct program option looks interesting...");
            if (vaultStage == 3) {
                player.getHintIconsManager().removeUnsavedHintIcon();
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        val hintIconPos = instance.getInstanceTile(2383, 9822, 0);
                        player.getHintIconsManager().addHintIcon(hintIconPos.getX(), hintIconPos.getY(), hintIconPos.getPlane(), 85, 5, 0, -1, false);
                    }
                });
            }
        } else if (object.getId() == 17283) {
            player.getDialogueManager().startDialogue(new GeneratorProgramD(this));
        } else if (object.getId() == 17282) {
            loadMachine();
        } else if (object.getId() == 14) {
            player.getActionManager().setAction(new ChopCarcassAction(this, carcassObject));
        } else if (object.getId() == 2403) {
            searchChest();
        } else if (object.getId() == 106592) {
            Dialogue.sendSinglePlayerDialogue(player, Dialogue.SCARED, Utils.randomFrom(INSPECT_MUTANT_TREES));
        } else if (object.getId() == 9398) {
            player.getBank().openDepositBox();
        }
        return false;
    }

    @Override
    public boolean canAddInventoryItem(int itemId, int amount) {
        if (itemId == 18682) {
            player.sendMessage("A magical force prevents the can from entering your inventory.");
            return false;
        }
        return true;
    }

    @Override
    public boolean checkWalkStep(int lastX, int lastY, int nextX, int nextY) {
        int safeTileX = instance.getInstanceTile(2401, 9828, 0).getX();
        if (lastX <= safeTileX && nextX <= safeTileX && lastX != nextX && vaultStage == 2) {
            player.getAppearence().setRenderEmote(-1);
            player.resetWalkSteps();
            player.getDialogueManager().startDialogue(new SinglePlayerDialogue(Dialogue.NORMAL, "I think I'm safe... Now I should figure out how to destroy the machinery."));
            vaultStage = 3;
            val hintIconPos = instance.getInstanceTile(2387, 9826, 0);
            player.getHintIconsManager().addHintIcon(hintIconPos.getX(), hintIconPos.getY(), hintIconPos.getPlane(), 85, 5, 0, -1, false);
            return false;
        }
        val tile1 = instance.getInstanceTile(2409, 9826, 0);
        val tile2 = instance.getInstanceTile(2408, 9826, 0);
        if ((lastX == tile1.getX() && lastY == tile1.getY() ||
                lastX == tile2.getX() && lastY == tile2.getY()) && vaultStage == 1) {
            vaultStage = 2;
            return true;
        }
        if (vaultStage == 0) {
            int nextTileY = instance.getInstanceTile(2409, 9821, 0).getY();
            if (lastY >= nextTileY && nextY >= nextTileY && lastY != nextY) {
                player.getDialogueManager().startDialogue(new SinglePlayerDialogue(Dialogue.SCARED, "I need to hide!"));
                return false;
            }
        } else if (vaultStage == 1) {
            int nextTileX = instance.getInstanceTile(2411, 9824, 0).getX();
            int nextTileY = instance.getInstanceTile(2405, 9819, 0).getY();
            if (lastX >= nextTileX && nextX >= nextTileX && lastX != nextX ||
                    lastY <= nextTileY && nextY <= nextTileY && lastY != nextY) {
                player.getDialogueManager().startDialogue(new SinglePlayerDialogue(Dialogue.SCARED, "I need to explore the cave further."));
                return false;
            }
        } else if (vaultStage >= 2) {
            int nextTileX = instance.getInstanceTile(2404, 9828, 0).getX();
            int nextTileY = instance.getInstanceTile(2409, 9826, 0).getY();
            if (lastX >= nextTileX && nextX >= nextTileX && lastY <= nextTileY && nextY <= nextTileY && lastY >= nextY) {
                player.getDialogueManager().startDialogue(new SinglePlayerDialogue(Dialogue.SCARED, "I'd rather not confront the mage."));
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canDropItem(Item item) {
        if (item.getId() != RootOfEvil.EVIL_LOGS_ID &&
                item.getId() != RootOfEvil.DIARY_ID &&
                item.getId() != RootOfEvil.SELF_DESTRUCT_KEY_ID &&
                item.getId() != RootOfEvil.EVIL_WATERING_CAN_ID) {
            return true;
        }
        player.sendMessage("A magical force prevents this item from being destroyed.");
        return false;
    }


    private void setup() {
        player.sendMessage("You fall into the cave...");
        // makes the machine "broken" 1 or unbroken 0
        // player.getPackets().sendConfigByFile(12818, 0, true);
        instance.load(() -> {
            evilMage = new StillNPC(3245, instance.getInstanceTile(2390, 9824, 0));
            evilMage.setNextFaceWorldTile(evilMage.transform(0, 1, 0));
            player.lock();
            player.getPackets().sendMiniMapStatus(2);
            carcassObject = new WorldObject(14, 10, 3, instance.getInstanceTile(2383, 9822, 0));
            objectSpawns.add(carcassObject);
            objectSpawns.add(new WorldObject(100790, 10, 1, instance.getInstanceTile(2387, 9817, 0)));
            objectSpawns.add(new WorldObject(9398, 10, 0, instance.getInstanceTile(2384, 9825, 0)));
            objectSpawns.add(new WorldObject(-1, 10, 0, instance.getInstanceTile(2388, 9813, 0)));
            objectSpawns.add(new WorldObject(-1, 10, 0, instance.getInstanceTile(2385, 9808, 0)));
            objectSpawns.add(new WorldObject(-1, 10, 0, instance.getInstanceTile(2392, 9825, 0)));
            objectSpawns.add(new WorldObject(-1, 10, 0, instance.getInstanceTile(2384, 9809, 0)));
            objectSpawns.add(new WorldObject(-1, 10, 0, instance.getInstanceTile(2384, 9809, 0)));
            objectSpawns.add(new WorldObject(2403, 10, 2, instance.getInstanceTile(2393, 9825, 0)));
            objectSpawns.add(new WorldObject(106592, 10, 2, instance.getInstanceTile(2390, 9817, 0)));
            objectSpawns.add(new WorldObject(106592, 10, 2, instance.getInstanceTile(2384, 9817, 0)));
            player.setNextWorldTile(instance.getInstanceTile(2409, 9812, 0));
            player.setNextFaceWorldTile(player.transform(1, 0, 0));
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    player.getCutscenesManager().play(new EvilMageIntroductionCutscene(instance, VaultController.this, evilMage));
                }
            }, 2);
            for (WorldObject next : objectSpawns) {
                World.spawnObject(next);
            }
        });
    }

    private void searchChest() {
        if (lootedChest) {
            Dialogue.sendSinglePlayerDialogue(player, Dialogue.NORMAL, "I've already looted this chest.");
            return;
        }
        if (vaultStage != 5) {
            Dialogue.sendSinglePlayerDialogue(player, Dialogue.NORMAL, "I'll leave this alone for now...");
            return;
        }
        player.lock();
        player.setNextAnimation(new Animation(2246));
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                player.unlock();
                Dialogue.sendSingleDialogue(player, "You find the self-destruct key, an evil watering can, and the wizard's diary.");
                if (!player.getInventory().containsItem(RootOfEvil.DIARY_ID, 1)) {
                    player.getInventory().addItem(RootOfEvil.DIARY_ID, 1);
                }
                if (!player.getInventory().containsItem(RootOfEvil.SELF_DESTRUCT_KEY_ID, 1)) {
                    player.getInventory().addItem(RootOfEvil.SELF_DESTRUCT_KEY_ID, 1);
                }
                if (!player.getInventory().containsItem(RootOfEvil.EVIL_WATERING_CAN_ID, 1)) {
                    player.getInventory().addItem(RootOfEvil.EVIL_WATERING_CAN_ID, 1);
                }
                lootedChest = true;
                player.getHintIconsManager().removeUnsavedHintIcon();
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        val hintIconPos = instance.getInstanceTile(2391, 9826, 0);
                        player.getHintIconsManager().addHintIcon(hintIconPos.getX(), hintIconPos.getY(), hintIconPos.getPlane(), 85, 5, 0, -1, false);
                    }
                }, 1);
            }
        });
    }

    private void loadMachine() {
        if (vaultStage == 4) {
            Dialogue.sendSinglePlayerDialogue(player, Dialogue.NORMAL, "The generator is already powered. I should now enter the self-destruct code in the program module.");
            return;
        }
        if (vaultStage == 5) {
            Dialogue.sendSinglePlayerDialogue(player, Dialogue.NORMAL, "I need to find the self-destruct key!");
            return;
        }
        int amount = player.getInventory().getAmountOf(RootOfEvil.EVIL_LOGS_ID);
        if (amount == 0) {
            Dialogue.sendSinglePlayerDialogue(player, Dialogue.NORMAL, "I don't have any logs to load into the generator.");
            return;
        }
        player.lock();
        player.resetWalkSteps();
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                player.setNextAnimation(new Animation(32626));
                player.unlock();
                player.getInventory().deleteItem(RootOfEvil.EVIL_LOGS_ID, amount);
                logsLoaded += amount;
                if (logsLoaded >= RootOfEvil.EVIL_LOGS_REQ) {
                    player.getHintIconsManager().removeUnsavedHintIcon();
                    WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                            val hintIconPos = instance.getInstanceTile(2391, 9826, 0);
                            player.getHintIconsManager().addHintIcon(hintIconPos.getX(), hintIconPos.getY(), hintIconPos.getPlane(), 85, 5, 0, -1, false);
                        }
                    }, 1);
                    Dialogue.sendSingleDialogue(player,
                            "You load " + amount + " log(s) into the generator.",
                            "The sound of machinery emanates from the program module as it turns on.");
                    vaultStage = 4;
                } else {
                    Dialogue.sendSingleDialogue(player, "You load " + amount + " log(s) into the generator.");
                }
            }
        }, 2);
    }
}
