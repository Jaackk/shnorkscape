package com.rs.game.player.controllers;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.SkillsDialogue;
import com.rs.game.player.content.SkillsDialogue.ItemNameFilter;
import com.rs.game.player.content.artisansworkshop.ArtisansWorkShop;
import com.rs.game.player.content.artisansworkshop.ArtisansWorkShop.CeremonialSwordPlan;
import com.rs.game.player.content.artisansworkshop.ArtisansWorkShopAction;
import com.rs.game.player.content.artisansworkshop.ArtisansWorkShopConstants.CeremonialSword;
import com.rs.game.player.content.artisansworkshop.ArtisansWorkShopConstants.Ingot;
import com.rs.game.player.content.artisansworkshop.ArtisansWorkShopConstants.Track;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.InputIntegerEvent;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class ArtisansWorkShopControler extends Controller {

    private int currentInstruction = -1;
    private int selectedIngotType;
    private int amountToMake;

    @Override
    public void start() {
        sendInterfaces();
    }

    @Override
    public void process() {
        if (currentInstruction != ArtisansWorkShop.currentInstructions) {
            currentInstruction = ArtisansWorkShop.currentInstructions;
            player.getPackets().sendIComponentText(1073, 1, ArtisansWorkShop.getInstructionText());
        }
    }

    @Override
    public void sendInterfaces() {
        if (isInsideArtisansBurial(player)) {
            player.getInterfaceManager().sendMinigameHudInterface(1073);
            player.getPackets().sendHideIComponent(1073, 9, false);
            player.getPackets().sendIComponentText(1073, 1, ArtisansWorkShop.getInstructionText());
        }
    }

    @Override
    public void forceClose() {
        player.getInterfaceManager().removeMinigameHudInterface();
    }

    @Override
    public void moved() {
        if (!isInsideArtisansShop(player)) {
            forceClose();
            removeControler();
        } else {
            if (!isInsideArtisansBurial(player)) {
                player.getInterfaceManager().removeMinigameHudInterface();
            } else {
                player.getInterfaceManager().sendMinigameHudInterface(1073);
                player.getPackets().sendHideIComponent(1073, 9, false);
                player.getPackets().sendIComponentText(1073, 1, ArtisansWorkShop.getInstructionText());
            }
        }
    }

    @Override
    public boolean processButtonClick(int interfaceId, int componentId, int slotId, int slotId2, int packetId) {
        if (interfaceId == 1072) {
            switch (componentId) {
            case 138:
            case 150:
            case 162:
            case 174:
                selectedIngotType = (componentId - 138) / 12;
                refreshIngotInterface();
                break;
            case 31:
            case 32:
                boolean add = componentId == 31;
                amountToMake = amountToMake + (add ? (amountToMake == 28 ? 0 : 1) : (amountToMake == 1 ? 0 : -1));
                refreshIngotInterface();
                break;
            case 201:
            case 213:
            case 225:
            case 237:
            case 249:
                int barType = (componentId - 201) / 12;
                Ingot ignot = Ingot.values()[barType * 4 + selectedIngotType];
                if (player.getSkills().getLevelForXp(Skills.SMITHING) < ignot.getRequiredLevel()) {
                    player.getPackets().sendGameMessage("You need a smithing level of " + ignot.getRequiredLevel() + " to make this ingot.");
                    return false;
                }
                Item[] requiredItems = ignot.getRequiredItems();
                int availableAmount = 0;
                int ore1Index = ArtisansWorkShop.getOreIndex(requiredItems[0].getId());
                int ore2Index = requiredItems.length == 1 ? -1 : ArtisansWorkShop.getOreIndex(requiredItems[1].getId());
                if (requiredItems.length == 1) {
                    availableAmount = player.getArtisansWorkShop().getArtisansWorkShopSupplies()[ore1Index] / requiredItems[0].getAmount();
                } else {
                    availableAmount = Math.min(player.getArtisansWorkShop().getArtisansWorkShopSupplies()[ore1Index] / requiredItems[0].getAmount(), player.getArtisansWorkShop().getArtisansWorkShopSupplies()[ore2Index] / requiredItems[1].getAmount());
                }
                if (player.getInventory().getFreeSlots() == 0) {
                    player.getPackets().sendGameMessage("You don't have enough inventory space to withdraw this ingot!");
                    player.closeInterfaces();
                    return false;
                }
                if (amountToMake > player.getInventory().getFreeSlots()) {
                    amountToMake = player.getInventory().getFreeSlots();
                    player.getPackets().sendGameMessage("You didn't have enough inventory space to withdraw all of the ingots.");
                }

                if (amountToMake > availableAmount) {
                    amountToMake = availableAmount;
                    if (amountToMake != 0)
                        player.getPackets().sendGameMessage("You didn't have enough ores to withdraw all of the ingots.");
                }
                if (amountToMake == 0) {
                    player.getPackets().sendGameMessage("You don't have enough ores to withdraw this ingot!");
                    player.closeInterfaces();
                    return false;
                }
                player.getArtisansWorkShop().setArtisansWorkShopSupplies(ore1Index, player.getArtisansWorkShop().getArtisansWorkShopSupplies()[ore1Index] - (requiredItems[0].getAmount() * amountToMake));
                if (requiredItems.length > 1)
                    player.getArtisansWorkShop().setArtisansWorkShopSupplies(ore2Index, player.getArtisansWorkShop().getArtisansWorkShopSupplies()[ore2Index] - (requiredItems[1].getAmount() * amountToMake));
                player.getInventory().addItem(ignot.getItemId(), amountToMake);
                player.getPackets().sendGameMessage("You withdraw " + amountToMake + " " + ItemDefinitions.getItemDefinitions(ignot.getItemId()).getName() + ".");
                player.closeInterfaces();
                break;
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean processObjectClick1(WorldObject object) {
        if (object.getId() == 114209 || object.getId() == 114208) {
            openWithdrawInterface(object.getId() == 114208);
            return false;
        } else if (object.getId() == 114175) {
            sendSmeltFurnaceSkillsDialogue(null);
            return false;
        } else if (object.getId() == 114171 || object.getId() == 114169 || object.getId() == 114127 || object.getId() == 114174 || object.getId() == 114173 || object.getId() == 114172) {
            CeremonialSwordPlan currentPlan = player.getArtisansWorkShop().getCurrentPlan();
            if (currentPlan == null) {
                int type = getPlanType();
                if (type == -1) {
                    player.getPackets().sendGameMessage("You don't have a sword design to start working.");
                    return false;
                }
                int heatedId = 20567 + type;
                if (!player.getInventory().containsItem(heatedId, 1)) {
                    player.getPackets().sendGameMessage("You don't have any heated ingots that matches your sword design.");
                    return false;
                }
                player.lock(1);
                player.getInventory().deleteItem(heatedId, 1);
                player.getInventory().deleteItem(20560 + type, 1);
                player.setNextAnimation(new Animation(21838));
                player.getPackets().sendGameMessage("You place the ingot and the plans on the anvil.");
                CeremonialSword randomSword = CeremonialSword.values()[Utils.random(CeremonialSword.values().length)];
                player.getArtisansWorkShop().setCurrentPlan(new CeremonialSwordPlan(type, randomSword.ordinal(), Math.max(Math.min(randomSword.getCoolDown() + Utils.random(2), 39), 22)));
                return false;
            }
            player.getArtisansWorkShop().openCeremonialSwordCreating();
            return false;
        } else if (object.getId() == 114207) {
            player.getDialogueManager().startDialogue(new Dialogue() {

                @Override
                public void start() {
                    this.sendOptionsDialogue("SELECT THE TOOL YOU WOULD LIKE TO TAKE", "Take Hammer", "Take Tongs", "Nevermind");
                }

                @Override
                public void run(int interfaceId, int componentId) {
                    end();
                    if (componentId != OPTION_3) {
                        if (player.getInventory().getFreeSlots() == 0)
                            player.getDialogueManager().startDialogue("SimpleMessage", "You don't have enough space in your inventory");
                        else
                            player.getInventory().addItem(componentId == OPTION_1 ? 2347 : 20565, 1);
                    }
                }

                @Override
                public void finish() {
                }
            });
            return false;
        } else if (object.getId() == 114126) {
            sendBurialArmourSkillsDialogue(object, null);
            return false;
        } else if (object.getId() == 114210) {
            boolean successful = false;
            for (Ingot ingot : Ingot.values()) {
                if (ingot == null || ingot.getProducts() == null || ingot.getProducts().length == 1)
                    continue;
                for (int itemId : ingot.getProducts()) {
                    if (player.getInventory().containsItem(itemId, 1)) {
                        player.getInventory().deleteItem(itemId, player.getInventory().getAmountOf(itemId));
                        successful = true;
                    }
                }
            }
            player.getPackets().sendGameMessage(successful ? "You deposit the armour into the machine." : "You don't have anything to deposit.");
            return false;
        } else if (object.getId() == 114204 || object.getId() == 114203) {
            player.useStairs(-1, new WorldTile(3067, 9710, 0), 1, 2);
            return false;
        } else if (object.getId() == 114523) {
            player.useStairs(-1, new WorldTile(3061, 3335, 0), 1, 2);
            leaveArtisansUnderGround();
            return false;
        } else if (object.getId() == 114205) {
            player.useStairs(-1, new WorldTile(3035, 9713, 0), 1, 2);
            return false;
        } else if (object.getId() == 114522) {
            player.useStairs(-1, new WorldTile(3037, 3342, 0), 1, 2);
            leaveArtisansUnderGround();
            return false;
        } else if (object.getId() == 114494 || object.getId() == 114493 || object.getId() == 114492) {
            player.getDialogueManager().startDialogue(new Dialogue() {
                int productId;

                @Override
                public void start() {
                    productId = object.getId() == 114494 ? 20504 : object.getId() == 114493 ? 20503 : 20502;
                    SkillsDialogue.sendSkillsDialogue(player, SkillsDialogue.MAKE, "How many would you like to make?<br>Choose a number, then click the item to begin.", 28, new int[] { productId }, new ItemNameFilter() {
                        @Override
                        public String rename(String name) {
                            int requiredLevel = (productId == 20504 ? 39 : productId == 20503 ? 15 : 1);
                            if (player.getSkills().getLevel(Skills.SMITHING) < requiredLevel)
                                name = "<col=ff0000>" + name + "<br><col=ff0000>Level " + requiredLevel;
                            return name;

                        }
                    });
                }

                @Override
                public void run(int interfaceId, int componentId) {
                    int amount = SkillsDialogue.getQuantity(player);
                    if (player.getInventory().getFreeSlots() == 0) {
                        player.getPackets().sendGameMessage("You don't have enough inventory space.");
                        end();
                        return;
                    }
                    if (amount > player.getInventory().getFreeSlots()) {
                        amount = player.getInventory().getFreeSlots();
                        player.getPackets().sendGameMessage("You didn't have enough inventory space to take all of the ingots.");
                    }
                    if (amount == 0) {
                        end();
                        return;
                    }
                    player.getInventory().addItem(productId, amount);
                    player.getPackets().sendGameMessage("You take " + amount + " " + ItemDefinitions.getItemDefinitions(productId).getName().toLowerCase() + ".");
                    end();
                }

                @Override
                public void finish() {

                }
            });
            return false;
        } else if (object.getId() == 114491) {
            sendTrackSkillsDialogue(object, null);
            return false;
        } else if (object.getId() == 114495) {
            boolean sucessful = false;
            for (Track track : Track.values()) {
                if (track == null || track.toString().contains("100"))
                    continue;
                if (player.getInventory().containsItem(track.getItemId(), 1)) {
                    int amount = player.getInventory().getAmountOf(track.getItemId());
                    player.getInventory().deleteItem(track.getItemId(), amount);
                    if (track.getRequiredItems().length > 1) {
                        player.getSkills().addXp(Skills.SMITHING, track.getXp() * amount);
                        player.getPackets().sendGameMessage("You get some xp for depositing " + ItemDefinitions.getItemDefinitions(track.getItemId()).getName().toLowerCase() + ".");
                    }
                    sucessful = true;
                }
            }
            player.getPackets().sendGameMessage(sucessful ? "You deposit components" : "You don't have any component that you can deposit.");
            return false;
        } else if (object.getId() == 114496 || object.getId() == 114497 || object.getId() == 114498) {
            List<Track> tracks = new ArrayList<Track>();
            for (Track track : Track.values()) {
                if (track == null || !track.toString().contains("100"))
                    continue;
                if (player.getInventory().containsItem(track.getItemId(), 1))
                    tracks.add(track);
            }
            if (tracks.isEmpty()) {
                player.getPackets().sendGameMessage("You don't have any tracks to lay down.");
                return false;
            }
            WorldTile tile = new WorldTile(player);
            WorldTile toTile = new WorldTile(object.getRotation() == 0 ? object.getX() + 1 : object.getX(), object.getRotation() == 3 ? object.getY() + 1 : object.getY(), object.getPlane());
            player.lock();
            player.addWalkSteps(toTile.getX(), toTile.getY(), 5, false);
            WorldTasksManager.schedule(new WorldTask() {
                int count = 0;

                @Override
                public void run() {
                    if (count == 2) {
                        for (int i = 0; i < tracks.size(); i++) {
                            Track track = tracks.get(i);
                            if (track == null)
                                continue;
                            int amount = player.getInventory().getAmountOf(track.getItemId());
                            player.getInventory().deleteItem(track.getItemId(), amount);
                            player.getSkills().addXp(Skills.SMITHING, track.getXp() * amount);
                        }
                        player.getPackets().sendGameMessage("You lay the tracks down.");
                    } else if (count == 4) {
                        player.addWalkSteps(tile.getX(), tile.getY(), 5, false);
                    } else if (count == 6) {
                        player.unlock();
                        stop();
                    }
                    count++;
                }

            }, 0, 1);
            return false;
        }
        return true;
    }

    @Override
    public boolean processObjectClick2(WorldObject object) {
        if (object.getId() == 114209 || object.getId() == 114208) {
            boolean successful = false;
            for (Ingot ingot : Ingot.values()) {
                if (ingot == null)
                    continue;
                int itemId = ingot.getItemId();
                if (player.getInventory().containsItem(itemId, 1)) {
                    int amount = player.getInventory().getAmountOf(itemId);
                    player.getInventory().deleteItem(itemId, amount);
                    player.getPackets().sendGameMessage("You return " + amount + " " + ItemDefinitions.getItemDefinitions(itemId).getName() + ".");
                    for (Item item : ingot.getRequiredItems()) {
                        int oreIndex = ArtisansWorkShop.getOreIndex(item.getId());
                        player.getArtisansWorkShop().getArtisansWorkShopSupplies()[oreIndex] += item.getAmount() * amount;
                    }
                    successful = true;
                }
            }
            if (!successful)
                player.getPackets().sendGameMessage("You don't have any ingots to return to the machine.");
            return false;
        } else if (object.getId() == 114494 || object.getId() == 114493 || object.getId() == 114492) {
            int itemId = object.getId() == 114494 ? 20504 : object.getId() == 114493 ? 20503 : 20502;
            if (player.getInventory().containsItem(itemId, 1)) {
                int amount = player.getInventory().getAmountOf(itemId);
                player.getInventory().deleteItem(itemId, amount);
                player.getPackets().sendGameMessage("You return " + amount + " " + ItemDefinitions.getItemDefinitions(itemId).getName().toLowerCase() + ".");
                return false;
            }
            player.getPackets().sendGameMessage("You don't have any ingots to return.");
            return false;
        }
        return true;
    }

    @Override
    public boolean processObjectClick3(WorldObject object) {
        if (object.getId() == 114209 || object.getId() == 114208) {
            sendDepositWithdrawDialogue(true);
            return false;
        }
        return true;
    }

    @Override
    public boolean processObjectClick4(WorldObject object) {
        if (object.getId() == 114209 || object.getId() == 114208) {
            sendDepositWithdrawDialogue(false);
            return false;
        }
        return true;
    }

    @Override
    public boolean processObjectClick5(WorldObject object) {
        return super.processObjectClick5(object);
    }

    @Override
    public boolean handleItemOnObject(WorldObject object, Item item) {
        if (object.getId() == 114126) {
            if (item != null && item.getName().contains("IV")) {
                player.sendMessage("You cannot smith this tier bars.");
                return false;
            }
            sendBurialArmourSkillsDialogue(object, item);
            return false;
        } else if (object.getId() == 114491) {
            sendTrackSkillsDialogue(object, item);
            return false;
        } else if (object.getId() == 114175) {
            sendSmeltFurnaceSkillsDialogue(item);
            return false;
        } else if (object.getId() == 114176) {
            for (Ingot ingot : Ingot.values()) {
                if (ingot.getProducts().length != 1 || ingot.getProducts()[0] != item.getId())
                    continue;
                player.getInventory().deleteItem(item.getId(), 1);
                player.getInventory().addItem(ingot.getItemId(), 1);
                player.getPackets().sendGameMessage("You cool the ingot.");
                return false;
            }
            player.getPackets().sendGameMessage("You can't use this item on water cooler.");
            return false;
        } else if (object.getId() == 114208 || object.getId() == 114209) {
            boolean noted = item.getDefinitions().isNoted();
            int itemId = noted ? (item.getDefinitions().getCertId()) : item.getId();
            int oreIndex = ArtisansWorkShop.getOreIndex(itemId);
            if (oreIndex == -1) {
                player.getPackets().sendGameMessage("You can't deposit this item.");
                return false;
            }
            player.getArtisansWorkShop().withdrawDepositOre(itemId, item.getAmount(), true);
            return false;
        }
        return true;
    }

    public void sendSmeltFurnaceSkillsDialogue(Item item) {
        List<Ingot> availableIngots = new ArrayList<Ingot>();
        if (item != null) {
            Ingot ingot = Ingot.forId(item.getId());
            if (ingot != null && ingot.getProducts().length == 1)
                availableIngots.add(ingot);
        } else
            for (Ingot ingot : Ingot.values()) {
                int[] products = ingot.getProducts();
                if (products.length != 1)
                    continue;
                if (player.getInventory().containsItem(ingot.getItemId(), 1))
                    availableIngots.add(ingot);
            }
        if (availableIngots.isEmpty()) {
            player.getPackets().sendGameMessage(item != null ? "You cant use this item on furnace." : "You don't have any ingots to use on furnace.");
            return;
        }
        player.getDialogueManager().startDialogue(new Dialogue() {
            @Override
            public void start() {
                int[] ids = new int[availableIngots.size()];
                for (int i = 0; i < ids.length; i++)
                    ids[i] = availableIngots.get(i).getProducts()[0];
                SkillsDialogue.sendSkillsDialogue(player, SkillsDialogue.MAKE, "How many would you like to make?<br>Choose a number, then click the item to begin.", 28, ids, new ItemNameFilter() {
                    int count = 0;

                    @Override
                    public String rename(String name) {
                        Ingot ingot = availableIngots.get(count++);
                        if (player.getSkills().getLevel(Skills.SMITHING) < ingot.getRequiredLevel())
                            name = "<col=ff0000>" + name + "<br><col=ff0000>Level " + ingot.getRequiredLevel();
                        return name;

                    }
                });
            }

            @Override
            public void run(int interfaceId, int componentId) {
                end();
                int productIndex = SkillsDialogue.getItemSlot(componentId);
                if (productIndex < 0) {
                    end();
                    return;
                }
                int quantity = SkillsDialogue.getQuantity(player);
                Ingot ingot = availableIngots.get(productIndex);
                if (ingot == null) {
                    end();
                    return;
                }
                if (quantity > player.getInventory().getNumberOf(ingot.getItemId()))
                    quantity = player.getInventory().getNumberOf(ingot.getItemId());
                if (quantity <= 0)
                    return;
                if (!player.getInventory().containsItem(20565, 1)) {
                    player.getPackets().sendGameMessage("You need tongs to use this ingot on furnace.");
                    return;
                }
                if (player.getSkills().getLevel(Skills.SMITHING) < ingot.getRequiredLevel()) {
                    player.getPackets().sendGameMessage("You need a Smithing level of at least " + ingot.getRequiredLevel() + " to smelt " + ItemDefinitions.getItemDefinitions(ingot.getItemId()) + ".");
                    return;
                }
                player.lock();
                player.setNextAnimation(new Animation(32626));
                final int amount = quantity;
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        player.getInventory().deleteItem(ingot.getItemId(), amount);
                        player.getInventory().addItem(ingot.getProducts()[0], amount);
                        player.getPackets().sendGameMessage("You heat all the ingots at once so that they'er soft and malleable.");
                        player.unlock();
                    }
                }, 1);
            }

            @Override
            public void finish() {

            }
        });
    }

    public void sendBurialArmourSkillsDialogue(WorldObject object, Item item) {
        Ingot ingot = null;
        if (item != null) {
            ingot = Ingot.forId(item.getId());
        } else
            for (int i = Ingot.values().length - 1; i >= 0; i--) {
                Ingot cIngot = Ingot.values()[i];
                if (cIngot == null || cIngot.getProducts() == null || !player.getInventory().containsItem(cIngot.getItemId(), 1))
                    continue;
                if (object.getId() == 114126 && cIngot.getItemId() >= 20648)
                    continue;
                ingot = cIngot;
                break;
            }
        if (ingot == null || ingot.getProducts() == null) {
            player.getPackets().sendGameMessage(item != null ? "You cant use this item on anvil." : "You don't have any ingots to use on anvil.");
            return;
        }
        player.getDialogueManager().startDialogue(new Dialogue() {
            Ingot ingot;

            @Override
            public void start() {
                ingot = (Ingot) parameters[0];
                SkillsDialogue.sendSkillsDialogue(player, SkillsDialogue.MAKE, "How many would you like to make?<br>Choose a number, then click the item to begin.", 28, ingot.getProducts(), new ItemNameFilter() {
                    @Override
                    public String rename(String name) {
                        if (player.getSkills().getLevel(Skills.SMITHING) < ingot.getRequiredLevel())
                            name = "<col=ff0000>" + name + "<br><col=ff0000>Level " + ingot.getRequiredLevel();
                        return name;

                    }
                });
            }

            @Override
            public void run(int interfaceId, int componentId) {
                end();
                int slot = SkillsDialogue.getItemSlot(componentId);
                if (slot > ingot.getProducts().length || slot < 0) {
                    end();
                    return;
                }
                player.getActionManager().setAction(new ArtisansWorkShopAction(object, ingot, slot, SkillsDialogue.getQuantity(player)));
            }

            @Override
            public void finish() {

            }
        }, ingot);
    }

    public void sendTrackSkillsDialogue(WorldObject object, Item item) {
        final List<Track> availableTracks = new ArrayList<Track>();
        int itemId = item == null ? (player.getInventory().containsItem(20504, 1) ? 20504 : player.getInventory().containsItem(20503, 1) ? 20503 : player.getInventory().containsItem(20502, 1) ? 20502 : -1) : item.getId();
        for (Track track : Track.values()) {
            if (track == null || (itemId != -1 ? (itemId >= 20502 && itemId <= 20504 ? (track.getRequiredItems().length > 1 || track.getRequiredItems()[0] != itemId) : (track.getRequiredItems().length == 1 || (track.getRequiredItems()[0] != itemId && track.getRequiredItems()[1] != itemId))) : (!player.getInventory().containsItem(track.getRequiredItems()[0], 1) || !player.getInventory().containsItem(track.getRequiredItems()[1], 1))))
                continue;
            availableTracks.add(track);
        }
        if (availableTracks.isEmpty()) {
            player.getPackets().sendGameMessage(item == null ? "You don't enough bars to make anything." : "You can't use this item on anvil.");
            return;
        }
        player.getDialogueManager().startDialogue(new Dialogue() {
            @Override
            public void start() {
                int[] products = new int[availableTracks.size()];
                for (int i = 0; i < products.length; i++)
                    products[i] = availableTracks.get(i).getItemId();
                SkillsDialogue.sendSkillsDialogue(player, SkillsDialogue.MAKE, "How many would you like to make?<br>Choose a number, then click the item to begin.", 28, products, new ItemNameFilter() {
                    int count = 0;

                    @Override
                    public String rename(String name) {
                        if (player.getSkills().getLevel(Skills.SMITHING) < availableTracks.get(count).getRequiredLevel())
                            name = "<col=ff0000>" + name + "<br><col=ff0000>Level " + availableTracks.get(count).getRequiredLevel();
                        count++;
                        return name;

                    }
                });
            }

            @Override
            public void run(int interfaceId, int componentId) {
                end();
                int slot = SkillsDialogue.getItemSlot(componentId);
                if (availableTracks.get(slot) == null || slot < 0) {
                    end();
                    return;
                }
                player.getActionManager().setAction(new ArtisansWorkShopAction(object, availableTracks.get(slot), SkillsDialogue.getQuantity(player)));
            }

            @Override
            public void finish() {

            }
        });
    }

    public void sendDepositWithdrawDialogue(final boolean deposit) {
        player.closeInterfaces();
        player.getDialogueManager().startDialogue(new Dialogue() {
            int itemId;

            @Override
            public void start() {
                sendOptionsDialogue("CHOOSE ORE YOU WANT TO " + (deposit ? "DEPOSIT" : "WITHDRAW"), (deposit ? "Deposit " : "Withdraw ") + "Coal", (deposit ? "Deposit " : "Withdraw ") + "Iron ore", (deposit ? "Deposit " : "Withdraw ") + "Mithril ore", (deposit ? "Deposit " : "Withdraw ") + "Adamantite ore", (deposit ? "Deposit " : "Withdraw ") + "Runite ore");
            }

            @Override
            public void run(int interfaceId, int componentId) {
                switch (stage) {
                case -1:
                    itemId = componentId == OPTION_1 ? 453 : componentId == OPTION_2 ? 440 : componentId == OPTION_3 ? 447 : componentId == OPTION_4 ? 449 : 451;
                    String option = (deposit ? "Deposit" : "Withdraw");
                    stage = 0;
                    sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, option + " 1", option + " 5", option + " 10", option + " X", option + " All");
                    break;
                case 0:
                    end();
                    switch (componentId) {
                    case OPTION_1:
                    case OPTION_2:
                    case OPTION_3:
                        player.getArtisansWorkShop().withdrawDepositOre(itemId, componentId == OPTION_1 ? 1 : componentId == OPTION_2 ? 5 : 10, deposit);
                        break;
                    case OPTION_4:
                        player.sendInputInteger("Choose the amount you want to " + (deposit ? "deposit" : "withdraw") + ":", new InputIntegerEvent() {
                            @Override
                            public void run(Player player) {
                                player.getArtisansWorkShop().withdrawDepositOre(itemId, getInteger(), deposit);
                            }
                        });
                        break;
                    case OPTION_5:
                        player.getArtisansWorkShop().withdrawDepositOre(itemId, Integer.MAX_VALUE, deposit);
                        break;
                    }
                    break;
                }

            }

            @Override
            public void finish() {

            }
        });
    }

    public void leaveArtisansUnderGround() {
        for (Track track : Track.values()) {
            if (track == null || !player.getInventory().containsItem(track.getItemId(), 1))
                continue;
            int itemId = track.getItemId();
            int amount = player.getInventory().getAmountOf(itemId);
            player.getInventory().deleteItem(itemId, amount);
            if (track.getRequiredItems().length > 1)
                player.getSkills().addXp(Skills.SMITHING, track.getXp() * amount);
        }
        if (player.getInventory().containsItem(20502, 1))
            player.getInventory().deleteItem(20502, player.getInventory().getAmountOf(20502));
        if (player.getInventory().containsItem(20503, 1))
            player.getInventory().deleteItem(20503, player.getInventory().getAmountOf(20503));
        if (player.getInventory().containsItem(20504, 1))
            player.getInventory().deleteItem(20504, player.getInventory().getAmountOf(20504));
    }

    @Override
    public void magicTeleported(int type) {
        if (isInsideArtisansUnderGround(player))
            leaveArtisansUnderGround();
        forceClose();
        removeControler();
    }

    public static boolean isInsideArtisansShop(Player player) {
        WorldTile loc = new WorldTile(player);
        return (loc.getX() >= 3035 && loc.getX() <= 3040 && loc.getY() >= 3335 && loc.getY() <= 3342) || (loc.getX() >= 3041 && loc.getX() <= 3050 && loc.getY() >= 3332 && loc.getY() <= 3345) || (loc.getX() >= 3051 && loc.getX() <= 3061 && loc.getY() >= 3333 && loc.getY() <= 3344) || (loc.getX() >= 3014 && loc.getX() <= 3071 && loc.getY() >= 9668 && loc.getY() <= 9723);
    }

    public static boolean isInsideArtisansUnderGround(Player player) {
        WorldTile loc = new WorldTile(player);
        return (loc.getX() >= 3014 && loc.getX() <= 3071 && loc.getY() >= 9668 && loc.getY() <= 9723);
    }

    public static boolean isInsideArtisansBurial(Player player) {
        WorldTile loc = new WorldTile(player);
        return (loc.getX() >= 3051 && loc.getX() <= 3061 && loc.getY() >= 3333 && loc.getY() <= 3344);
    }

    @Override
    public boolean processNPCClick1(NPC npc) {
        if (npc.getId() == 6642 || npc.getId() == 6647) {
            player.getDialogueManager().startDialogue("EgilAbelD", npc.getId());
            return false;
        }
        return true;
    }

    public void openWithdrawInterface(boolean IVIngot) {
        player.getInterfaceManager().sendInterface(1072);
        selectedIngotType = IVIngot ? 3 : selectedIngotType == 3 ? 0 : selectedIngotType;
        amountToMake = player.getInventory().getFreeSlots() == 0 ? 1 : player.getInventory().getFreeSlots();
        for (int i = 0; i < player.getArtisansWorkShop().getArtisansWorkShopSupplies().length; i++) {
            player.getPackets().sendIComponentText(1072, 49 + (4 * i), player.getArtisansWorkShop().getArtisansWorkShopSupplies()[i] + (i == 0 ? "/8000" : "/4000"));
        }
        if (IVIngot) {
            player.getPackets().sendHideIComponent(1072, 140, true);
            player.getPackets().sendHideIComponent(1072, 152, true);
            player.getPackets().sendHideIComponent(1072, 129, true);
            player.getPackets().sendHideIComponent(1072, 133, true);
            player.getPackets().sendHideIComponent(1072, 134, true);
            player.getPackets().sendHideIComponent(1072, 136, true);
            player.getPackets().sendHideIComponent(1072, 138, true);
        } else {
            player.getPackets().sendHideIComponent(1072, 165, true);
            player.getPackets().sendHideIComponent(1072, 169, true);
            player.getPackets().sendHideIComponent(1072, 170, true);
            player.getPackets().sendHideIComponent(1072, 172, true);
            player.getPackets().sendHideIComponent(1072, 174, true);
        }
        refreshIngotInterface();
    }

    public void refreshIngotInterface() {
        player.getPackets().sendRunScript(4183, selectedIngotType == 0 ? 70254730 : selectedIngotType == 1 ? 70254742 : selectedIngotType == 2 ? 70254754 : 70254766);
        player.getPackets().sendIComponentText(1072, 33, amountToMake + "");
        player.getPackets().sendConfigByFile(8877, amountToMake);
        player.getPackets().sendConfigByFile(8857, player.getArtisansWorkShop().getArtisansWorkShopSupplies()[1] + 1);
        player.getPackets().sendConfigByFile(8859, player.getArtisansWorkShop().getArtisansWorkShopSupplies()[2] + 1);
        player.getPackets().sendConfigByFile(8862, player.getArtisansWorkShop().getArtisansWorkShopSupplies()[3] + 1);
        player.getPackets().sendConfigByFile(8863, player.getArtisansWorkShop().getArtisansWorkShopSupplies()[4] + 1);
        player.getPackets().sendConfigByFile(8865, player.getArtisansWorkShop().getArtisansWorkShopSupplies()[0] + 1);
    }

    private int getPlanType() {
        for (int i = 0; i < 5; i++)
            if (player.getInventory().containsItem(20560 + i, 1))
                return i;
        return -1;
    }
}
