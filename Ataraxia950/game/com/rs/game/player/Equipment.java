package com.rs.game.player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.rs.Settings;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.WorldTile;
import com.rs.game.activites.duel.DuelControler;
import com.rs.game.activities.seasonalevents.christmas.ChristmasSeasonalEvent;
import com.rs.game.item.Item;
import com.rs.game.item.ItemsContainer;
import com.rs.game.npc.NPC;
import com.rs.game.npc.others.Revenant;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.actions.firemaking.Bonfire;
import com.rs.game.player.combat.PlayerCombat;
import com.rs.game.player.content.ItemConstants;
import com.rs.game.player.content.Magic;
import com.rs.game.player.content.SkillCapeCustomizer;
import com.rs.game.player.content.grandExchange.GrandExchange;
import com.rs.game.player.content.items.Ectoplasmator;
import com.rs.game.player.content.items.EliteOutfits;
import com.rs.game.player.content.items.PrayerBooks;
import com.rs.game.player.content.items.RunePouch;
import com.rs.game.player.dialogue.impl.Transportation;
import com.rs.network.packet.PacketRepository;
import com.rs.network.packet.impl.ButtonHandler;
import com.rs.utils.Colors;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.items.ItemExaminesDataParser;
import com.rs.utils.data.parsers.items.ItemWeightsDataParser;

import lombok.val;

public final class Equipment implements Serializable {

    public static final byte SLOT_HAT = 0, SLOT_CAPE = 1, SLOT_AMULET = 2, SLOT_WEAPON = 3, SLOT_CHEST = 4, SLOT_SHIELD = 5, SLOT_LEGS = 7, SLOT_HANDS = 9, SLOT_FEET = 10, SLOT_RING = 12, SLOT_ARROWS = 13, SLOT_AURA = 14, SLOT_POCKET = 17, SLOT_WINGS = 18;
    // static final int[] DISABLED_SLOTS = new int[] { 0, 0, 0, 0, 0, 0, 0, 0,
    // 0, 0, 0, 0, 1, 1, 0 };
    static final int[] DISABLED_SLOTS = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 1, 0 };

    private static final long serialVersionUID = -4147163237095647617L;
    private ItemsContainer<Item> items;
    private transient double equipmentWeight;
    private transient Player player;
    private transient int equipmentHpIncrease;
    // cosmetics
    private ItemsContainer<Item> cosmeticItems;
    private transient ItemsContainer<Item> cosmeticPreviewItems;
    private List<Item> keepSakeItems;
    private List<SavedCosmetic> savedCosmetics;
    @SuppressWarnings("unused")
    private int costumeColor;

    public Equipment() {
        items = new ItemsContainer<Item>(19, false);
        cosmeticItems = new ItemsContainer<>(19, false);
        keepSakeItems = new ArrayList<>(100);
        savedCosmetics = new ArrayList<>();
    }

    public int getAmountOf(int itemId) {
        return items.getNumberOf(itemId);
    }

    public static boolean hideArms(Item item) {
        return item.getDefinitions().getEquipType() == 6;
    }

    public static boolean hideHair(Item item) {
        String name = item.getName().toLowerCase();
        return item.getDefinitions().getEquipType() == 8 || name.contains("full helm") || (name.startsWith("void") && name.contains("ranger")) || name.contains("dragon mask") || name.contains("ween mask") || name.contains("fox mask");
    }

    public static boolean showBear(Item item) {
        String name = item.getName().toLowerCase();
        if (name.contains("sirenic") || name.contains("tectonic"))
            return false;
        return !hideHair(item) || name.contains("horns") || name.contains("hat") || name.contains("afro") || name.contains("bronze full") || name.contains("cowl") || name.contains("tattoo") || name.contains("headdress") || name.contains("hood") || name.contains("bearhead") || name.equals("santa hat") || name.contains("partyhat") || name.contains("sleeping") || name.contains("coif") || name.contains("wig") || name.contains("bandana") || name.contains("mitre") || (name.contains("mask") && !name.contains("ween") && !name.contains("sirenic")) || name.contains("med helm") || name.contains("chicken head") || (name.contains("helm") && !name.contains("full") || name.contains("headwear") || item.getId() == 32386);
    }

    // try
    public static int getItemSlot(int itemId) {
        if (itemId == 14632)
            return Equipment.SLOT_SHIELD;
        if (itemId == 1917 || itemId == 1963 || itemId == 8794 || itemId == 288)
            return -1;
        return ItemDefinitions.getItemDefinitions(itemId).getEquipSlot();
    }

    public void resetEquipmentSlots() {
        ItemsContainer<Item> i = items.asItemContainer();
        items = new ItemsContainer<Item>(19, false);
        for (int x = 0; x < 19; x++)
            items.set(x, i.get(x));
    }

    public static boolean isTwoHandedWeapon(Item item) {
        return item.getDefinitions().getEquipType() == 5;
    }

    public void checkItems() {
        if (cosmeticItems == null)
            cosmeticItems = new ItemsContainer<>(19, false);
        if (keepSakeItems == null)
            keepSakeItems = new ArrayList<>(100);
        if (savedCosmetics == null)
            savedCosmetics = new ArrayList<>();
        int size = items.getSize();
        int newSize = 19;
        if (size != newSize) {
            Item[] copy = new Item[newSize];
            for (int i = 0; i < items.getSize(); i++)
                copy[i] = items.getItems()[i];
            items = new ItemsContainer<Item>(newSize, false);
            for (int i = 0; i < copy.length; i++)
                items.set(i, copy[i]);
        }
        for (int i = 0; i < size; i++) {
            Item item = items.get(i);
            if (item == null)
                continue;
            if (!ItemConstants.canWear(item, player)) {
                items.set(i, null);
                player.getInventory().addItemDrop(item.getId(), item.getAmount());
            }
        }
        if (keepSakeItems.size() != 100) {
            ArrayList<Item> keepSakeCopy = new ArrayList<Item>(100);
            for (int i = 0; i < keepSakeItems.size(); i++)
                if (keepSakeItems.get(i) != null)
                    keepSakeCopy.add(keepSakeItems.get(i));
            this.keepSakeItems = keepSakeCopy;
        }
    }


    public int getWeaponStance() {
        boolean combatStance = player.getCombatDefinitions().isCombatStance();
        Item weapon = items.get(3);
        if (weapon == null) {
            Item offhand = items.get(SLOT_SHIELD);
            if (offhand == null)
                return combatStance ? 2688 : 2699;
            int emote = offhand.getDefinitions().getCombatOpcode(combatStance ? 2955 : 2954);
            return emote == 0 ? combatStance ? 2688 : 2699 : emote;
        }
        int emote = weapon.getDefinitions().getCombatOpcode(combatStance ? 2955 : 2954);
        if (weapon.getId() == 4084) // sled exception
            return 1119;
        return emote == 0 ? combatStance ? 2688 : 2699 : emote;
    }

    public void deleteItem(int itemId, int amount) {
        Item[] itemsBefore = items.getItemsCopy();
        items.remove(new Item(itemId, amount));
        refreshItems(itemsBefore);
    }

    public int getAmmoAmount() {
        Item item = items.get(SLOT_ARROWS);
        if (item == null)
            return -1;
        return item.getAmount();
    }

    public int getAmmoId() {
        Item item = items.get(SLOT_ARROWS);
        if (item == null)
            return -1;
        return item.getId();
    }

    public int getAmuletId() {
        Item item = items.get(SLOT_AMULET);
        if (item == null)
            return -1;
        return item.getId();
    }

    public int getAuraId() {
        Item item = items.get(SLOT_AURA);
        if (item == null)
            return -1;
        return item.getId();
    }

    public int getBootsId() {
        Item item = items.get(SLOT_FEET);
        if (item == null)
            return -1;
        return item.getId();
    }

    public int getCapeId() {
        Item item = items.get(SLOT_CAPE);
        if (item == null)
            return -1;
        return item.getId();
    }

    public int getChestId() {
        Item item = items.get(SLOT_CHEST);
        if (item == null)
            return -1;
        return item.getId();
    }

    public int getEquipmentHpIncrease() {
        return equipmentHpIncrease;
    }

    public void setEquipmentHpIncrease(int hp) {
        this.equipmentHpIncrease = hp;
    }

    public int getGlovesId() {
        Item item = items.get(SLOT_HANDS);
        if (item == null)
            return -1;
        return item.getId();
    }

    public int getHatId() {
        Item item = items.get(SLOT_HAT);
        if (item == null)
            return -1;
        return item.getId();
    }

    public Item getItem(int slot) {
        return items.get(slot);
    }

    public ItemsContainer<Item> getItems() {
        return items;
    }

    public int getLegsId() {
        Item item = items.get(SLOT_LEGS);
        if (item == null)
            return -1;
        return item.getId();
    }

    public int getRingId() {
        Item item = items.get(SLOT_RING);
        if (item == null)
            return -1;
        return item.getId();
    }

    public int getShieldId() {
        Item item = items.get(SLOT_SHIELD);
        if (item == null)
            return -1;
        return item.getId();
    }

    public int getWeaponId() {
        Item item = items.get(SLOT_WEAPON);
        if (item == null)
            return -1;
        return item.getId();
    }
    
    public int getPocketId() {
        Item item = items.get(SLOT_POCKET);
        if (item == null)
            return -1;
        return item.getId();
    }

    public List<Integer> getEquipmentIds() {
        List<Integer> equipmentIds = new ArrayList<>();
        Arrays.stream(items.toArray()).forEach(item -> {
            if (item != null && item.getId() != -1) {
                equipmentIds.add(item.getId());
            }
        });
        return equipmentIds;
    }

    public boolean hasShield() {
        return items.get(5) != null;
    }

    public boolean hasTwoHandedWeapon() {
        Item weapon = items.get(SLOT_WEAPON);
        return weapon != null && isTwoHandedWeapon(weapon);
    }

    public void init() {
        if (cosmeticItems == null)
            cosmeticItems = new ItemsContainer<>(19, false);
        if (keepSakeItems == null)
            keepSakeItems = new ArrayList<>(100);
        if (savedCosmetics == null)
            savedCosmetics = new ArrayList<>();
        player.getPackets().sendItems(94, items);
        refresh(null);
    }

    public void refresh(int... slots) {
        if (slots != null) {
            if (player.getTemporaryAttributtes().get("Cosmetics") != null) {
                Item[] cosmetics = items.getItemsCopy();
                for (int i = 0; i < cosmetics.length; i++) {
                    Item item = cosmetics[i];
                    if (item == null)
                        cosmetics[i] = new Item(0);
                }
                player.getPackets().sendUpdateItems(94, cosmetics, slots);
            } else
                player.getPackets().sendUpdateItems(94, items, slots);
//            player.getCombatDefinitions().checkAttackStyle();
        }
        player.getCombatDefinitions().refreshBonuses();
        player.getInventionManager().refreshEquipedItemsDrainRate();
        refreshConfigs(slots == null);
    }

    public void refreshItemContainer() {
        player.getPackets().sendItems(94, items);
        refresh();
    }

    public void refreshConfigs(boolean init) {
        double hpIncrease = player.getCombatDefinitions().getBonuses()[CombatDefinitions.LIFE_B];
        if (player.getLastBonfire() > 0) {
            int maxhp = player.getSkills().getLevel(Skills.HITPOINTS) * 10;
            hpIncrease += (maxhp * (Bonfire.getBonfireBoostMultiplier(player) * (player.getPerkManager().hasPerkActive(DonationPerk.THE_PYROMANIAC) && !player.isCanPvp() ? 1.25 : 1))) - maxhp;
        }
        if (player.getHpBoostMultiplier() != 0) {
            int maxhp = player.getSkills().getLevel(Skills.HITPOINTS) * 10;
            hpIncrease += maxhp * player.getHpBoostMultiplier();
        }
        if (hpIncrease != equipmentHpIncrease) {
            equipmentHpIncrease = (int) hpIncrease;
            if (!init)
                player.refreshHitPoints();
        }
        double w = 0;
        for (Item item : items.getItems()) {
            if (item == null)
                continue;
            w += ItemWeightsDataParser.getWeight(item, true);
        }
        equipmentWeight = w;
        player.getPackets().refreshWeight();
    }

    public double getEquipmentWeight() {
        return equipmentWeight;
    }

    public boolean cantWearWidowsWail() {
        val skills = player.getSkills();
        return skills.getLevelForXp(Skills.ATTACK) > 70 || skills.getLevelForXp(Skills.STRENGTH) > 70 || skills.getLevelForXp(Skills.DEFENCE) > 70;
    }
//    public static final byte SLOT_HAT = 0, SLOT_CAPE = 1, SLOT_AMULET = 2, SLOT_WEAPON = 3, SLOT_CHEST = 4, SLOT_SHIELD = 5, SLOT_LEGS = 7, SLOT_HANDS = 9, SLOT_FEET = 10, SLOT_RING = 12, SLOT_ARROWS = 13, SLOT_AURA = 14, SLOT_POCKET = 17, SLOT_WINGS = 18;

    public void handleEquipment(int interfaceId, int componentId, int slotId, int itemId, int packetId) {
        player.closeInterfaces();
        if (player.getInterfaceManager().containsInventoryInter())
            return;
        if (slotId == Equipment.SLOT_AURA) {
            if (player.getEquipment().getItem(slotId) != null && !player.getAuraManager().ignoreDuelIsActivated()) {
                player.addItem(player.getEquipment().getItem(slotId));
                player.getEquipment().set(slotId, null);
                player.getEquipment().refreshItemContainer();
                return;
            }
            switch (packetId) {
            default:
                player.getAuraManager().handleEquipmentOptions(true, packetId);
                break;
            }
            return;
        }
        int option = packetId == PacketRepository.ACTION_BUTTON1_PACKET ? 0 : packetId == PacketRepository.ACTION_BUTTON2_PACKET ? 1 : packetId == PacketRepository.ACTION_BUTTON3_PACKET ? 2 : packetId == PacketRepository.ACTION_BUTTON4_PACKET ? 3 : packetId == PacketRepository.ACTION_BUTTON5_PACKET ? 4 : packetId == PacketRepository.ACTION_BUTTON9_PACKET ? 5 : packetId == PacketRepository.ACTION_BUTTON8_PACKET ? 6 : -1;
        int equipmentSlot = slotId;
        if (option == -1)
            return;
        ItemDefinitions def = ItemDefinitions.getItemDefinitions(itemId);
        if (Settings.DEBUG)
            player.getPackets().sendConsoleMessage(def.name + " equipment options : " + Arrays.toString(def.getEquipmentOptions()) + " , option=" + option);
        if (itemId >= 39893 && itemId <= 39901) {
            if (packetId == PacketRepository.ACTION_BUTTON2_PACKET) {
                player.getDialogueManager().startDialogue("AttuneGemstoneArmourD");
                return;
            } else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET) {
                player.getDialogueManager().startDialogue("GemstoneChargesD");
                return;
            }
        }

        // if(option == )
        String clickedOption = def.getEquipmentOptions()[option];
        if (slotId == Equipment.SLOT_CAPE && SkillCapeCustomizer.isCustomizable(itemId)) {
            switch (clickedOption) {
            case "Remove":
                ButtonHandler.sendRemove(player, Equipment.SLOT_CAPE);
                break;
            case "Max Guild":
                Magic.compCapeTeleport(player, 2276, 3315, 1);
                break;
            case "Kandarin Monastery":
                Magic.compCapeTeleport(player, 2606, 3222, 0);
                break;
            case "Summoning restore":
                Long sumRestore = (Long) player.getTemporaryAttributtes().get("sum_restore");
                if (sumRestore != null && sumRestore + 1500000 > Utils.currentTimeMillis()) {
                    player.sendMessage("You can only restore your summoning points once every 15 minutes.");
                    return;
                }
                player.getSkills().set(Skills.SUMMONING, 99);
                player.getTemporaryAttributtes().put("sum_restore", Utils.currentTimeMillis());
                player.sendMessage("Summoning points restored, you can do this again in 15 minutes.");
                break;
            case "Customise":
                SkillCapeCustomizer.startCustomizing(player, itemId);
                break;
            }
            return;
        }
        if (clickedOption != null && equipmentSlot != -1) {
            Item item = player.getEquipment().getItem(equipmentSlot);
            if (item != null) {
                if (item.getId() == 26492 && clickedOption.equalsIgnoreCase("check")) {
                    ChristmasSeasonalEvent.checkCmasAmulet(player, item);
                    return;
                }
                if (item.getId() == 47592) {
                    if (clickedOption.equalsIgnoreCase("remove")) {
                        ButtonHandler.sendRemove(player, Equipment.SLOT_CAPE);
                    } else if (clickedOption.equalsIgnoreCase("add-to")) {
                        ChristmasSeasonalEvent.addToPresentSack(player, item);
                    } else if (clickedOption.equalsIgnoreCase("check")) {
                        ChristmasSeasonalEvent.checkPresentSack(player, item);
                    }
                    return;
                }
                if (item.getLowercaseName().contains("slayer helmet")) {
                    if (clickedOption.equalsIgnoreCase("kills left")) {
                        if (player.getTask() == null) {
                            player.sendMessage("You currently have no Slayer task. Speak to one of the Slayer masters to get one.");
                        } else {
                            player.sendMessage("You have to kill " + Colors.RED + player.getTask().getAmountLeft() + "</col> more " + Colors.RED + player.getTask().getName(player) + "(s)</col>.");
                        }
                    } else if (clickedOption.equalsIgnoreCase("activate")) {
                        player.getDialogueManager().startDialogue("Kuradal", 9085);
                    } else if (clickedOption.equalsIgnoreCase("co-op options")) {
                        player.getInterfaceManager().sendInterface(1309);
                    }
                }
                if (clickedOption.equalsIgnoreCase("check charges") || clickedOption.equalsIgnoreCase("check") || clickedOption.equalsIgnoreCase("check-charges") || clickedOption.equalsIgnoreCase("check state")) {
                    if (player.getInventionManager().checkAugmentedItem(equipmentSlot, item, true) || player.getChargesManagerNew().checkCharges(item)/* || player.getCharges().checkCharges(item) */)
                        return;
                }
                if (clickedOption.equals("Communicate")) {
                    if (item.getId() == 37694) {
                        player.getDialogueManager().startDialogue("SkillingMasterD", false);
                        return;
                    } else if (item.getId() == 25450) {
                        if (player.getContracts().canTalkToAdvancedMaster(true)) {
                            player.getDialogueManager().startDialogue("SkillingMasterD", true);
                        }
                        return;
                    }
                }
                if (clickedOption.equalsIgnoreCase("freedom")) {
                    PlayerCombat.pressFreedom(player);
                    return;
                }
                if (item.getId() == 41083) {
                    switch (clickedOption.toLowerCase()) {
                    case "check storage":
                        player.getInventionManager().checkVaccumCharges();
                        return;
                    case "withdraw":
                        player.getInventionManager().withdrawDivineCharges();
                        return;
                    case "configure":
                        player.getInventionManager().configureVaccum();
                        return;
                    }
                }
            }
        }
        switch (slotId) {
        case Equipment.SLOT_HAT:// HAT
            if (packetId == PacketRepository.ACTION_BUTTON2_PACKET) {
                if (itemId == 34030 || itemId == 34031 || itemId == 34032 || itemId == 34033 || itemId == 34034 || itemId == 34035 || itemId == 34036) {
                    player.getDialogueManager().startDialogue("SunglassesD", itemId);
                    return;
                }

                if (EliteOutfits.isComponent(itemId)) {
                    if (EliteOutfits.wearingSentinel(player))
                        player.getDialogueManager().startDialogue("SentinelTeleports", EliteOutfits.getPrefix(def.getName()));
                    else
                        player.getDialogueManager().startDialogue("SimpleItemMessage", 39745, 1, Colors.shade(Colors.SALMON) + "You need to be wearing a full set of matching Sentinel outfit!");
                    return;
                }

                if (itemId == 24431) {
                    player.setNextGraphics(new Graphics(3187));
                    return;
                }
                if (itemId == 35963 || itemId == 35968 || itemId == 35973 || itemId == 35978) {
                    Magic.sendNormalTeleportSpell(player, 1, 0, new WorldTile(3134, 3228, 0));
                    return;
                }
                if (itemId >= 15493 && itemId <= 15494) {
                    player.getDialogueManager().startDialogue("Kuradal");
                    return;
                }
                if (itemId == 10507) {
                    player.setNextGraphics(new Graphics(263));
                    player.setNextAnimation(new Animation(7531));
                    player.sendMessage("You act like a reindeer.. I think.");
                    player.lock(2);
                    return;
                }
            }
            break;
        case Equipment.SLOT_CAPE:// CAPE
            switch (packetId) {
            case PacketRepository.ACTION_BUTTON2_PACKET:
                if (itemId == 10498 || itemId == 10499 || itemId == 20068)
                    player.sendMessage("You can not operate that.");
                if (itemId == 12645) {
                    player.setNextAnimation(new Animation(8903));
                    player.setNextGraphics(new Graphics(1566));
                    player.lock(6);
                }
                break;
            }
            break;
        case Equipment.SLOT_AMULET:// AMULET
            switch (packetId) {
            case PacketRepository.ACTION_BUTTON2_PACKET:
                int amuletId = player.getEquipment().getAmuletId();
                if (amuletId == 37130 || amuletId >= 39298 && amuletId <= 39307) {
                    player.getDialogueManager().startDialogue("ChicScarfD", amuletId, true);
                    return;
                } else if (amuletId == 21514) {
                    player.sendMessage("Your arcane capacitor has " + player.getEquipment().getItem(Equipment.SLOT_AMULET).getCharges() + " charges left in it.");
                    return;
                }
                if (amuletId <= 1712 && amuletId >= 1706) {
                    if (Magic.sendItemTeleportSpell(player, true, Transportation.EMOTE, Transportation.GFX, 4, new WorldTile(3087, 3496, 0))) {
                        Item amulet = player.getEquipment().getItem(Equipment.SLOT_AMULET);
                        if (amulet != null) {
                            amulet.setId(amulet.getId() - 2);
                            player.getEquipment().refresh(Equipment.SLOT_AMULET);
                        }
                    }
                } else if (amuletId >= 11105 && amuletId <= 11111) {
                    if (Magic.sendItemTeleportSpell(player, true, Transportation.EMOTE, Transportation.GFX, 4, new WorldTile(2614, 3382, 0))) {
                        Item amulet = player.getEquipment().getItem(Equipment.SLOT_AMULET);
                        if (amulet != null) {
                            amulet.setId(amulet.getId() + 2);
                            player.getEquipment().refresh(Equipment.SLOT_AMULET);
                        }
                    }
                } else if (amuletId == 1704 || amuletId == 10352) {
                    player.sendMessage("The amulet has ran out of charges.");
                    return;
                } else if (amuletId == 11113) {
                    player.sendMessage("The amulet has ran out of charges.");
                    return;
                } else if (amuletId >= 3853 && amuletId <= 3867) {
                    player.getDialogueManager().startDialogue("Transportation2", "Burthrope Games Room", new WorldTile(2880, 3559, 0), "Barbarian Outpost", new WorldTile(2519, 3571, 0), "Gamers' Grotto", new WorldTile(2970, 9679, 0), "Corporeal Beast", new WorldTile(2886, 4377, 0), amuletId);
                    return;
                }
                break;
            case PacketRepository.ACTION_BUTTON3_PACKET:
                if (itemId <= 1712 && itemId >= 1706) {
                    if (Magic.sendItemTeleportSpell(player, true, Transportation.EMOTE, Transportation.GFX, 4, new WorldTile(2918, 3176, 0))) {
                        Item amulet = player.getEquipment().getItem(Equipment.SLOT_AMULET);
                        if (amulet != null) {
                            amulet.setId(amulet.getId() - 2);
                            player.getEquipment().refresh(Equipment.SLOT_AMULET);
                        }
                    }
                } else if (itemId >= 11105 && itemId <= 11111) {
                    if (Magic.sendItemTeleportSpell(player, true, Transportation.EMOTE, Transportation.GFX, 4, new WorldTile(3021, 3339, 0))) {
                        Item amulet = player.getEquipment().getItem(Equipment.SLOT_AMULET);
                        if (amulet != null) {
                            amulet.setId(amulet.getId() + 2);
                            player.getEquipment().refresh(Equipment.SLOT_AMULET);
                        }
                    }
                }
                break;
            case PacketRepository.ACTION_BUTTON4_PACKET:
                if (itemId <= 1712 && itemId >= 1706) {
                    if (Magic.sendItemTeleportSpell(player, true, Transportation.EMOTE, Transportation.GFX, 4, new WorldTile(3105, 3251, 0))) {
                        Item amulet = player.getEquipment().getItem(Equipment.SLOT_AMULET);
                        if (amulet != null) {
                            amulet.setId(amulet.getId() - 2);
                            player.getEquipment().refresh(Equipment.SLOT_AMULET);
                        }
                    }
                } else if (itemId >= 11105 && itemId <= 11111) {
                    if (Magic.sendItemTeleportSpell(player, true, Transportation.EMOTE, Transportation.GFX, 4, new WorldTile(2933, 3296, 0))) {
                        Item amulet = player.getEquipment().getItem(Equipment.SLOT_AMULET);
                        if (amulet != null) {
                            amulet.setId(amulet.getId() + 2);
                            player.getEquipment().refresh(Equipment.SLOT_AMULET);
                        }
                    }
                }
                break;
            case PacketRepository.ACTION_BUTTON5_PACKET:
                if (itemId <= 1712 && itemId >= 1706) {
                    if (Magic.sendItemTeleportSpell(player, true, Transportation.EMOTE, Transportation.GFX, 4, new WorldTile(3293, 3163, 0))) {
                        Item amulet = player.getEquipment().getItem(Equipment.SLOT_AMULET);
                        if (amulet != null) {
                            amulet.setId(amulet.getId() - 2);
                            player.getEquipment().refresh(Equipment.SLOT_AMULET);
                        }
                    }
                } else if (itemId >= 11105 && itemId <= 11111) {
                    if (Magic.sendItemTeleportSpell(player, true, Transportation.EMOTE, Transportation.GFX, 4, new WorldTile(3142, 3440, 0))) {
                        Item amulet = player.getEquipment().getItem(Equipment.SLOT_AMULET);
                        if (amulet != null) {
                            amulet.setId(amulet.getId() + 2);
                            player.getEquipment().refresh(Equipment.SLOT_AMULET);
                        }
                    }
                }
                break;
            }
            break;
        case Equipment.SLOT_WEAPON:// WEAPON
            switch (packetId) {
            case PacketRepository.ACTION_BUTTON2_PACKET:
                if (itemId == 15426)
                    player.setNextAnimation(new Animation(12664));
                if (itemId == 20084)
                    player.setNextAnimation(new Animation(15150));
                break;
            case PacketRepository.ACTION_BUTTON3_PACKET:
                if (itemId == 20084) {
                    player.setNextGraphics(new Graphics(2953));
                    player.setNextAnimation(new Animation(15149));
                }
                break;
            }
            break;
        case Equipment.SLOT_SHIELD:// SHIELD
            switch (packetId) {
            case PacketRepository.ACTION_BUTTON2_PACKET:
                final Item idam5 = player.getEquipment().getItem(Equipment.SLOT_SHIELD);
                if (idam5 != null) {
                    if (PrayerBooks.isGodBook(idam5.getId(), true)) {
                        PrayerBooks.sermanize(player, idam5.getId());
                        return;
                    }
                }
                int shieldId = player.getEquipment().getShieldId();
                if (shieldId == 11283 || shieldId == 25558 || shieldId == 25561) {
                    if (player.getDFSDelay() >= Utils.currentTimeMillis()) {
                        player.sendMessage("You must wait two minutes before performing this attack once more.");
                        return;
                    }
                    player.getTemporaryAttributtes().put("dfs_shield_active", true);
                } else if (shieldId == 11284 || shieldId == 25559 || shieldId == 25562)
                    player.sendMessage("You don't have any charges left in your shield.");
                break;
            case PacketRepository.ACTION_BUTTON3_PACKET:
                final Item idam6 = player.getEquipment().getItem(Equipment.SLOT_SHIELD);
                if (idam6 != null) {
                    if (idam6.getId() == 30014 || idam6.getId() == 30018 || idam6.getId() == 30022) {
                        Entity target = (Entity) player.getTemporaryAttributtes().get("last_target");
                        if (target != null && !target.isDead() && !target.hasFinished()) {
                            if (target instanceof Player) {
                                player.sendMessage("You cannot provoke players.");
                                return;
                            } else if (target instanceof NPC) {
                                long lastProvoke = player.getTemporaryAttributtes().get("provoke") == null ? 0 : (long) player.getTemporaryAttributtes().get("provoke");
                                if (lastProvoke > Utils.currentTimeMillis()) {
                                    final int seconds = (int) (((lastProvoke - Utils.currentTimeMillis()) / 1000) + 1);
                                    player.sendMessage("You need to wait another " + seconds + " second" + (seconds == 1 ? "" : "s") + " to use provoke.");
                                    return;
                                }
                                player.getTemporaryAttributtes().put("provoke", Utils.currentTimeMillis() + 30000);
                                target.setTarget(player);
                                player.setNextAnimation(new Animation(18130));
                            }
                        } else
                            player.sendMessage("You need to be engaged in combat to use provoke.");
                        return;
                    }
                }
                break;
            }
            break;
        case Equipment.SLOT_LEGS:// LEGS

            break;
        case Equipment.SLOT_HANDS:// HANDS
            switch (packetId) {
            case PacketRepository.ACTION_BUTTON2_PACKET:
                if (itemId == Ectoplasmator.ATTUNED_ECTOPLASMATOR_ID) {
                    player.sendMessage("Your attuned ectoplasmator has " + Colors.RED + player.ectoCharges + "</col> charges left.");
                    return;
                }
                if (itemId == Ectoplasmator.DEGRADED_ATTUNED_ECTOPLASMATOR_ID) {
                    player.sendMessage("This ectoplasmator has degraded.");
                    return;
                }
                if (itemId >= 11118 && itemId <= 11124) {
                    if (Magic.sendItemTeleportSpell(player, true, Transportation.EMOTE, Transportation.GFX, 4, new WorldTile(2880, 3542, 0))) {
                        Item gloves = player.getEquipment().getItem(Equipment.SLOT_HANDS);
                        if (gloves != null) {
                            gloves.setId(gloves.getId() + 2);
                            player.getEquipment().refresh(Equipment.SLOT_HANDS);
                        }
                    }
                } else if (itemId == 11126) {
                    player.sendMessage("The bracelet has ran out of charges.");
                    return;
                }
                final Item idam8 = player.getEquipment().getItem(Equipment.SLOT_HANDS);
                if (itemId >= 13845 && itemId <= 13857) {
                    player.sendMessage("Your " + idam8.getName() + " have " + (int) Math.ceil((double) idam8.getCharges() / Revenant.getBrawlersCharges(itemId) * 100) + "% of its charges left.");
                    return;
                }
                break;
            case PacketRepository.ACTION_BUTTON3_PACKET:
                if (itemId >= 11118 && itemId <= 11124) {
                    if (Magic.sendItemTeleportSpell(player, true, Transportation.EMOTE, Transportation.GFX, 4, new WorldTile(3191, 3367, 0))) {
                        Item gloves = player.getEquipment().getItem(Equipment.SLOT_HANDS);
                        if (gloves != null) {
                            gloves.setId(gloves.getId() + 2);
                            player.getEquipment().refresh(Equipment.SLOT_HANDS);
                        }
                    }
                }
                break;
            case PacketRepository.ACTION_BUTTON4_PACKET:
                if (itemId >= 11118 && itemId <= 11124) {
                    if (Magic.sendItemTeleportSpell(player, true, Transportation.EMOTE, Transportation.GFX, 4, new WorldTile(3051, 3491, 0))) {
                        Item gloves = player.getEquipment().getItem(Equipment.SLOT_HANDS);
                        if (gloves != null) {
                            gloves.setId(gloves.getId() + 2);
                            player.getEquipment().refresh(Equipment.SLOT_HANDS);
                        }
                    }
                }
                break;
            case PacketRepository.ACTION_BUTTON5_PACKET:
                if (itemId >= 11118 && itemId <= 11124) {
                    if (Magic.sendItemTeleportSpell(player, true, Transportation.EMOTE, Transportation.GFX, 4, new WorldTile(2655, 3441, 0))) {
                        Item gloves = player.getEquipment().getItem(Equipment.SLOT_HANDS);
                        if (gloves != null) {
                            gloves.setId(gloves.getId() + 2);
                            player.getEquipment().refresh(Equipment.SLOT_HANDS);
                        }
                    }
                }
                break;
            }
            break;
        case Equipment.SLOT_FEET:// BOOTS
            if (packetId == PacketRepository.ACTION_BUTTON2_PACKET) {
                final Item idam9 = player.getEquipment().getItem(Equipment.SLOT_FEET);
                if (idam9 != null) {
                    if (idam9.getId() >= 30920 && idam9.getId() <= 30924) {
                        if (idam9.getCharges() == 0)
                            player.sendMessage("There are no charges left in your Silverhawk boots.");
                        else
                            player.sendMessage("There " + (idam9.getCharges() == 1 ? "is " : "are ") + idam9.getCharges() + " charges left in your Silverhawk boots.");
                        return;
                    }
                }
            } else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET) {
                if (player.isUnderCombat()) {
                    player.sendMessage("You need to be out of combat to do this emote.");
                    return;
                }
                player.lock(15);
                player.stopAll();
                player.setNextAnimation(new Animation(22794));
                player.setNextGraphics(new Graphics(4604));
                player.setNextGraphics(new Graphics(4603));
                return;
            }
            break;
        case Equipment.SLOT_RING:// RING
            switch (packetId) {
            case PacketRepository.ACTION_BUTTON2_PACKET:
                if (itemId == 15707 || ItemDefinitions.getItemDefinitions(itemId).isRingOfKinship()) {
                    player.getDungeoneeringManager().openPartyInterface();
                    return;
                }
                if (itemId >= 13281 && itemId <= 13288) {
                    player.getDialogueManager().startDialogue("Kuradal", 9085);
                    return;
                }
                if (itemId == 2572) {
                    player.getInterfaceManager().sendBossKillLog();
                    return;
                }
                if (itemId >= 34987 && itemId <= 34995) {
                    player.getDialogueManager().startDialogue("KethsiRing", itemId, false);
                    return;
                }
                if (itemId >= 2552 && itemId <= 2566) {
                    if (Magic.sendItemTeleportSpell(player, true, Transportation.EMOTE, Transportation.GFX, 4, new WorldTile(3315, 3234, 0))) {
                        Item ring = player.getEquipment().getItem(Equipment.SLOT_RING);
                        if (ring != null) {
                            if (ring.getId() == 2566)
                                ring.setId(1635);
                            else
                                ring.setId(ring.getId() + 2);
                            player.getEquipment().refresh(Equipment.SLOT_RING);
                        }
                    }
                    return;
                }
                if (itemId == 2568) {
                    int left = 140 - player.ironOres;
                    player.sendMessage("Your ring of forging has " + Colors.RED + left + "</col> charges left.");
                    return;
                }
                if (itemId >= 20655 && itemId <= 20659) {
                    Magic.sendItemTeleportSpell(player, true, 9603, 1684, 4, new WorldTile(2581, 3845, 0));
                    Item ring = player.getEquipment().getItem(Equipment.SLOT_RING);
                    ring.setId(ring.getId() - 2);
                    player.getEquipment().refresh(Equipment.SLOT_RING);
                    return;
                }
                if (itemId == 20653) {
                    Magic.sendItemTeleportSpell(player, true, 9603, 1684, 4, new WorldTile(2581, 3845, 0));
                    Item ring = player.getEquipment().getItem(Equipment.SLOT_RING);
                    player.getEquipment().refresh(Equipment.SLOT_RING);
                    ring.setId(2572);
                    return;
                }
                break;
            case PacketRepository.ACTION_BUTTON3_PACKET:
                if (itemId == 15707) {
                    Magic.daemonheimTeleport(player, new WorldTile(3448, 3699, 0));
                    return;
                } else if (ItemDefinitions.getItemDefinitions(itemId).isRingOfKinship()) {
                    player.getRingOfKinship().quickSwitch(new Item(itemId));
                    return;
                }
                if (itemId >= 13281 && itemId <= 13288) {
                    if (player.getTask() == null) {
                        player.sendMessage("You currently do not have an active slayer task.");
                        return;
                    } else {
                        player.sendMessage("Your task is to kill <col=ff0000><shad=000000>" + player.getTask().getTaskAmount() + "</col></shad> x <col=ff0000><shad=000000>" + player.getTask().getName(player).toLowerCase() + "s</col></shad>.");
                        return;
                    }
                }
                if (itemId >= 2552 && itemId <= 2566) {
                    if (Magic.sendItemTeleportSpell(player, true, Transportation.EMOTE, Transportation.GFX, 4, new WorldTile(2442, 3088, 0))) {
                        Item ring = player.getEquipment().getItem(Equipment.SLOT_RING);
                        if (ring != null) {
                            if (ring.getId() == 2566)
                                ring.setId(1635);
                            else
                                ring.setId(ring.getId() + 2);
                            player.getEquipment().refresh(Equipment.SLOT_RING);
                        }
                    }
                }
                if (itemId >= 20655 && itemId <= 20659) {
                    Magic.sendItemTeleportSpell(player, true, 9603, 1684, 4, new WorldTile(3164, 3468, 0));
                    Item ring = player.getEquipment().getItem(Equipment.SLOT_RING);
                    ring.setId(ring.getId() - 2);
                    player.getEquipment().refresh(Equipment.SLOT_RING);
                    return;
                }
                if (itemId == 20653) {
                    Magic.sendItemTeleportSpell(player, true, 9603, 1684, 4, new WorldTile(3164, 3468, 0));
                    Item ring = player.getEquipment().getItem(Equipment.SLOT_RING);
                    ring.setId(2572);
                    player.getEquipment().refresh(Equipment.SLOT_RING);
                    return;
                }
                break;
            case PacketRepository.ACTION_BUTTON4_PACKET:
                /*
                 * if (itemId == 15707) { player.sendMessage("You cannot customise your ring.");
                 * return; }
                 */
                if (itemId == 15707 || ItemDefinitions.getItemDefinitions(itemId).isRingOfKinship()) {
                    player.getRingOfKinship().openInterface(new Item(itemId, 1));
                    // Logger.getGlobal().info(packetId);
                    // Magic.daemonheimTeleport(player, new WorldTile(3448,
                    // 3699, 0));
                    return;
                }
                if (itemId >= 13281 && itemId <= 13288) {
                    player.getInterfaceManager().sendInterface(1309);
                    return;
                }
                if (itemId >= 2552 && itemId <= 2566) {
                    if (Magic.sendItemTeleportSpell(player, true, Transportation.EMOTE, Transportation.GFX, 4, new WorldTile(2413, 2848, 0))) {
                        Item ring = player.getEquipment().getItem(Equipment.SLOT_RING);
                        if (ring != null) {
                            if (ring.getId() == 2566)
                                ring.setId(1635);
                            else
                                ring.setId(ring.getId() + 2);
                            player.getEquipment().refresh(Equipment.SLOT_RING);
                        }
                    }
                }
                break;
            case PacketRepository.ACTION_BUTTON5_PACKET:
                if (itemId >= 2552 && itemId <= 2566) {
                    if (Magic.sendItemTeleportSpell(player, true, Transportation.EMOTE, Transportation.GFX, 4, new WorldTile(1679, 5599, 0))) {
                        Item ring = player.getEquipment().getItem(Equipment.SLOT_RING);
                        if (ring != null) {
                            if (ring.getId() == 2566)
                                ring.setId(1635);
                            else
                                ring.setId(ring.getId() + 2);
                            player.getEquipment().refresh(Equipment.SLOT_RING);
                        }
                    }
                }
                break;
            }
            break;
        case Equipment.SLOT_ARROWS:// ARROWS
            if (itemId == 38453) {
                if (packetId == PacketRepository.ACTION_BUTTON1_PACKET)
                    ButtonHandler.sendRemove(player, Equipment.SLOT_ARROWS);
                if (packetId == PacketRepository.ACTION_BUTTON2_PACKET)
                    RunePouch.check(player, false);
                else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET)
                    RunePouch.empty(player, false);
                return;
            }
            break;
        }
        switch (packetId) {
        case PacketRepository.ACTION_BUTTON1_PACKET:
            ButtonHandler.sendRemove(player, slotId);
            break;
        case PacketRepository.ACTION_BUTTON3_PACKET:
            if (itemId == 34200 || itemId == 34205 || itemId == 34210 || itemId == 34215) {
                player.getDialogueManager().startDialogue("SharkConsumeOption");
                return;
            }
            break;
        case PacketRepository.ACTION_BUTTON8_PACKET:
            player.getEquipment().sendExamine(interfaceId, componentId, slotId);
            break;
        }
    }

    public void refreshItems(Item[] itemsBefore) {
        int[] changedSlots = new int[itemsBefore.length];
        int count = 0;
        for (int index = 0; index < itemsBefore.length; index++) {
            if (itemsBefore[index] != items.getItems()[index])
                changedSlots[count++] = index;
        }
        int[] finalChangedSlots = new int[count];
        System.arraycopy(changedSlots, 0, finalChangedSlots, 0, count);
        refresh(finalChangedSlots);
    }

    public void removeAmmo(int ammoId, int amount, boolean primary) {
        if (ammoId == -1 || ammoId == -2) {
            int slot = primary ? Equipment.SLOT_WEAPON : Equipment.SLOT_SHIELD;
            Item item = items.get(slot);
            if (item != null && item.getAmount() > 1) {
                items.set(slot, new Item(item.getId(), item.getAmount() - 1));
            } else {
                items.set(slot, null); // fully consumed
            }
            refresh(slot);
            player.getAppearence().generateAppearenceData();
        } else {
            items.remove(Equipment.SLOT_ARROWS, new Item(ammoId, amount));
            refresh(Equipment.SLOT_ARROWS);
        }
    }


    public void setGear(byte gear) {
        int slot = gear;
        items.remove(slot, new Item(items.get(slot)));
        refresh(gear);
    }

    public void reset() {
        items.reset();
        init();
    }

    public void resetExcept(int slot) {
        for (byte i = 0; i <= SLOT_ARROWS; i++) {
            if (i == slot) {
                continue;
            }
            if (items.get(i) != null) {
                setGear(i);
            }
        }
    }

    public void sendExamine(int interfaceId, int componentId, int slotId) {
        Item item = items.get(slotId);
        if (item == null)
            return;
        player.getPackets().sendInterfaceMessage(interfaceId, componentId, 0, slotId, ItemExaminesDataParser.getExamine(item));
        player.getPackets().sendGameMessage("Grand Exchange guide price: " + Utils.getFormattedNumber(GrandExchange.getPrice(item)) + ".");
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public boolean has(int slot, int itemId) {
        Item item = items.get(slot);
        if (item == null)
            return false;
        return item.getId() == itemId;
    }

    public boolean wearingArmour() {
        return wearingArmourExceptAura();
    }

    public boolean wearingArmourExceptAura() {
        return getItem(SLOT_HAT) != null || getItem(SLOT_CAPE) != null || getItem(SLOT_AMULET) != null || getItem(SLOT_WEAPON) != null || getItem(SLOT_CHEST) != null || getItem(SLOT_SHIELD) != null || getItem(SLOT_LEGS) != null || getItem(SLOT_HANDS) != null || getItem(SLOT_FEET) != null || getItem(SLOT_ARROWS) != null || getItem(SLOT_RING) != null;
    }

    public boolean wearingArmourExcept(int slot) {
        for (byte i = 0; i <= SLOT_WINGS; i++) {
            if (i != slot && getItem(i) != null) {
                return false;
            }
        }
        return true;
    }

    public boolean wearingArmorExcept(int slot, int id) {
        if (getItem(slot) != null && getItem(slot).getId() != id)
            return true;

        for (int i = 0; i <= 14; i++) {
            if (slot == i || getItem(i) == null)
                continue;

            if (getItem(i) != null)
                return true;
        }

        return !(getItem(slot) == null || getItem(slot).getId() == id);
    }

    public ItemsContainer<Item> getItemsContainer() {
        return items;
    }

    public void set(int i, Item item) {
        items.set(i, item);
        refresh();
    }

    public boolean containsOneItem(int... itemIds) {
        for (int itemId : itemIds) {
            if (items.containsOne(new Item(itemId, 1)))
                return true;
        }
        return false;
    }


    @SuppressWarnings("unused")
    private boolean refreshItems(Item item) {
        int defenceLvl = player.getSkills().getLevelForXp(Skills.DEFENCE);
        int[] items50 = { 30306, 30309, 30312, 30315, 30318, 30321, 28773, 28776, 28779, 28782, 28785, 28788, 28755, 28758, 28761, 28764, 28767, 28770, 30288, 30291, 30294, 30297, 30300, 30303 };
        int[] items75 = { 30307, 30310, 30313, 30316, 30319, 30322, 28774, 28777, 28780, 28783, 28786, 28789, 28756, 28759, 28762, 28765, 28768, 28771, 30289, 30292, 30295, 30298, 30301, 30304 };
        if (defenceLvl >= 50 && defenceLvl < 75) {
            for (int itemId : items50) {
                if (item.getId() == itemId) {
                    player.sendMessage("Your " + item.getName() + " upgraded to a higher tier.");
                    item.setId(item.getId() + 1);
                    return true;
                }
            }
        }
        if (defenceLvl >= 75) {
            for (int itemId : items75) {
                if (item.getId() == itemId) {
                    player.sendMessage("Your " + item.getName() + " upgraded to a higher tier.");
                    item.setId(item.getId() + 1);
                    return true;
                }
            }
            for (int itemId : items50) {
                if (item.getId() == itemId) {
                    player.sendMessage("Your " + item.getName() + " upgraded to a higher tier.");
                    item.setId(item.getId() + 2);
                    return true;
                }
            }
        }
        return false;
    }

    public int getWeaponEndCombatEmote() {
        Item weapon = items.get(3);
        if (weapon == null) {
            Item offhand = items.get(SLOT_SHIELD);
            if (offhand == null)
                return -1;
            int emote = offhand.getDefinitions().getCombatOpcode(2918);
            return emote == 0 ? 18025 : emote;
        }
        int emote = weapon.getDefinitions().getCombatOpcode(2918);
        return emote == 0 ? 18025 : emote;
    }

    public static boolean isTwoHandedWeapon(ItemDefinitions defs) {
        return defs.getEquipType() == 5;
    }

    public void removeAmmo(int ammoId, int ammount) {
        if (ammount == -1) {
            val superiorMorrigansJavelinItemId = 39139;
            val superiorMorrigansThrowingAxeItemId = 39143;
            if (ammoId == superiorMorrigansJavelinItemId || ammoId == superiorMorrigansThrowingAxeItemId) {
                return;
            }
            items.remove(SLOT_WEAPON, new Item(ammoId, 1));
            refresh(SLOT_WEAPON);
        } else {
            items.remove(SLOT_ARROWS, new Item(ammoId, ammount));
            refresh(SLOT_ARROWS);
        }
        player.getAppearence().generateAppearenceData();
    }

    public ItemsContainer<Item> getCosmeticItems() {
        if (cosmeticItems == null)
            cosmeticItems = new ItemsContainer<>(19, false);
        return cosmeticItems;
    }

    public List<Item> getKeepSakeItems() {
        if (keepSakeItems == null)
            keepSakeItems = new ArrayList<>(100);
        return keepSakeItems;
    }

    public boolean containsKeepSakeItem(int itemId) {
        for (Item item : keepSakeItems) {
            if (item == null)
                continue;
            if (item.getId() == itemId)
                return true;
        }
        return false;
    }

    public List<SavedCosmetic> getSavedCosmetics() {
        if (savedCosmetics == null)
            savedCosmetics = new ArrayList<>();
        return savedCosmetics;
    }

    public boolean isCanDisplayCosmetic() {
        if (player.getControlerManager().getControler() != null && player.getControlerManager().getControler() instanceof DuelControler)
            return false;

        if (player.isOwner())
            return true;
        return !player.isCanPvp();
    }

    public void resetCosmetics() {
        cosmeticItems.reset();
    }

    public ItemsContainer<Item> getCosmeticPreviewItems() {
        return cosmeticPreviewItems;
    }

    public void setCosmeticPreviewItems(ItemsContainer<Item> cosmeticPreviewItems) {
        this.cosmeticPreviewItems = cosmeticPreviewItems;
    }

    public boolean isEmpty() {
        return getItems().getUsedSlots() == 0;
    }

    public static final class SavedCosmetic implements Serializable {

        /**
         *
         */
        private static final long serialVersionUID = 3243926269985535095L;
        private final ItemsContainer<Item> cosmeticItems;
        private final String cosmeticName;

        public SavedCosmetic(String cosmeticName, ItemsContainer<Item> cosmeticItems) {
            this.cosmeticName = cosmeticName;
            this.cosmeticItems = cosmeticItems;
        }

        public ItemsContainer<Item> getCosmeticItems() {
            return cosmeticItems;
        }

        public String getCosmeticName() {
            return cosmeticName;
        }

    }

    public boolean hasOffHand() {
        Item item = items.get(SLOT_SHIELD);
        return items.get(5) != null && item.getDefinitions().getCombatMap() != null;
    }

    public void unlockEquipment(boolean menu) {
        player.getPackets().sendIComponentSettings(menu ? 1462 : 1464, menu ? 31 : 15, 0, 18, 10749950);
        player.getPackets().sendIComponentSettings(menu ? 1462 : 1464, menu ? 40 : 24, 0, 5, 2046);
        player.getPackets().sendIComponentSettings(menu ? 1462 : 1464, menu ? 40 : 19, 0, 5, 2046);
    }

    public void refreshEquipmentInterfaceBonuses() {
        player.getPackets().sendConfig(1037, player.getCombatDefinitions().getBonuses()[CombatDefinitions.MELEE_ACCURACY_PENALTY]);
        player.getPackets().sendConfig(1038, player.getCombatDefinitions().getBonuses()[CombatDefinitions.RANGE_ACCURACY_PENALTY]);
        player.getPackets().sendConfig(1039, player.getCombatDefinitions().getBonuses()[CombatDefinitions.MAGE_ACCURACY_PENALTY]);
        // main hand
        player.getPackets().sendConfig(715, player.getCombatDefinitions().getHandDamage(false) * 10);
        player.getPackets().sendConfig(3561, player.getCombatDefinitions().getSkillAccuracy(false));
        player.getPackets().sendConfig(717, player.getCombatDefinitions().getStyle(false));
        // offhand
        player.getPackets().sendConfig(716, player.getCombatDefinitions().getHandDamage(true) * 2 * 10);
        player.getPackets().sendConfig(3562, player.getCombatDefinitions().getSkillAccuracy(true));
        player.getPackets().sendConfig(718, player.getCombatDefinitions().getStyle(true));
        // armor
        player.getPackets().sendConfig(711, player.getCombatDefinitions().getBonuses()[CombatDefinitions.WORN_ARMOUR]);
        player.getPackets().sendConfig(3563, player.getCombatDefinitions().getDefenceArmor());
        player.getPackets().sendConfig(3596, player.getCombatDefinitions().getBonuses()[CombatDefinitions.LIFE_B] * 10);
        player.getPackets().sendConfig(712, player.getCombatDefinitions().getBonuses()[CombatDefinitions.MELEE_AFF]);
        player.getPackets().sendConfig(713, player.getCombatDefinitions().getBonuses()[CombatDefinitions.RANGE_AFF]);
        player.getPackets().sendConfig(714, player.getCombatDefinitions().getBonuses()[CombatDefinitions.MAGIC_AFF]);
//        VARBIT[22841];PVM reduction tank armour
//        VARBIT[22842];PVM reduction shield
//        VAR_0[4500]PVP reduction armour
        player.getPackets().sendGlobalConfig(779, 2702); // sets equip inter
        // render anim
        player.getPackets().sendExecuteScript(6992);
    }

}