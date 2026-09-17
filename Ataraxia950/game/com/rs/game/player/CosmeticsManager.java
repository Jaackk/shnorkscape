package com.rs.game.player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.rs.Settings;
import com.rs.cache.loaders.BodyDefinitions;
import com.rs.cache.loaders.ClientScriptMap;
import com.rs.cache.loaders.GeneralRequirementMap;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.cache.loaders.VarBitDefinitions;
import com.rs.game.item.Item;
import com.rs.game.player.TemporaryAttributes.Key;
import com.rs.game.player.content.CosmeticsHandler;
import com.rs.game.player.content.CosmeticsHandler.CosmeticType;
import com.rs.game.player.content.InterfaceManager;
import com.rs.game.player.content.pet.Pets;
import com.rs.game.player.content.petperks.PetPerkUtils;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.network.packet.PacketRepository;
import com.rs.utils.DropDownMenuEvent;
import com.rs.utils.InputStringEvent;

import lombok.Getter;
import lombok.Setter;

public class CosmeticsManager implements Serializable {

    private static final long serialVersionUID = -6960190414485444043L;

    public static final int COSMETIC_TYPE_MENU_VARBIT = 673;
    private static final String SELECTED_CUSTOMISATION_SUBMENU = "selectedCustomisationSubMenu";
    private static final int CUSTOMISATION_CATEGORY_COMPONENT_START = 136;
    private static final int CUSTOMISATION_LIST_COMPONENT_START = 159;
    private static final int CUSTOMISATION_COMPONENT_COUNT = 22;


    // autounlocked, no var. blame rs XD, book of faces, the stuffed title, hide
    // all(invisbile item)
    // private static final int[] FREE_COSMETICS = { 14289, 14538, 5692, 19139,
    // 19140, 21095, 21096, 21097, 21103, 21102 };

    private transient Player player;
    private transient Item[] cosmeticPreview;

    private boolean showingAllItems;
    @Getter
    private List<Integer> unlockedCosmetics;
    private transient PurchaseItem[] purshaseItems;
    private int[] activeAnimations;

    public CosmeticsManager() {
        resetUnlockedCosmetics();
        showingAllItems = true;
        activeAnimations = new int[CosmeticsAnimation.values().length];

    }

    public void init() {
        if (cosmeticPresets == null) {
            cosmeticPresets = new CosmeticPreset[4];
            for (int i = 0; i < cosmeticPresets.length; i++)
                cosmeticPresets[i] = new CosmeticPreset();
        }
        checkAnimationSlots();
        CosmeticsHandler.checkRequirementUnlockedCosmetics(player);
        // script_8284
        player.getVarBitManager().sendVarBit(29610, 1);// removes extra tab
        if (unlockedCosmetics == null)
            resetUnlockedCosmetics();
        refreshVars();
        refreshShowingAllItems();
        player.getPackets().sendItems(675, player.getEquipment().getKeepSakeItems().toArray(new Item[player.getEquipment().getKeepSakeItems().size()]));
        for(int itemid : player.getUnlockedCostumesIds())
            unlockItem(itemid);
    }

    public void setPlayer(Player player) {
        this.player = player;
        purshaseItems = new PurchaseItem[0];
        if (unlockedCosmetics == null) // temporary
            resetUnlockedCosmetics();
        checkAnimationSlots();
    }

    private void checkAnimationSlots() {
        if (activeAnimations == null || activeAnimations.length != CosmeticsAnimation.values().length)
            activeAnimations = new int[CosmeticsAnimation.values().length];
    }

    private void resetUnlockedCosmetics() {
        unlockedCosmetics = new ArrayList<Integer>();
        for (int data : FREE_COSMETICS)
            unlockedCosmetics.add(data);
    }

    // save unlocked cosmetics and so on here.
    public void open(CosmeticsHandler.CosmeticType type) {
        open(type, false);
    }
    public enum CosmeticsAnimation {
        SKILL_ANIMATIONS_MINING(0, "mining"),

        SKILL_ANIMATIONS_WOODCUTTING(0, "woodcutting"),

        SKILL_ANIMATIONS_FISHING(0, "fishing"),

        SKILL_ANIMATIONS_FIREMAKING(0, "firemaking"),

        SKILL_ANIMATIONS_POTION_MAKING(0, "potion making"),

        SKILL_ANIMATIONS_SLAYER_BATTLE_CRY(0, "slayer battle cry"),

        SKILL_ANIMATIONS_SMITHING(0, "smithing"),

        SKILL_ANIMATIONS_SMELTING(0, "smelting"),

        SKILL_ANIMATIONS_COOKING(0, "cooking"),

        SKILL_ANIMATIONS_FLETCHING(0, "fletching"),

        SKILL_ANIMATIONS_PRAYER(0, "prayer", "burial"),

        SKILL_ANIMATIONS_DIVINATION(0, "divination"),

        SKILL_ANIMATIONS_CONVERSION(0, "conversion"),

        SKILL_ANIMATIONS_RUNECRAFTING(0, "runecrafting"),

        SKILL_ANIMATIONS_SUMMONING(0, "summoning"),

        SKILL_ANIMATIONS_INSPIRATION(0, "inspiration"),

        SKILL_ANIMATIONS_HEROIC_CRIT(0, "heroic crit"),

        SKILL_ANIMATIONS_HEROIC_HITSPARKS(0, "heroic hitsparks"),

        SPELL_ANIMATIONS_TELEPORT(1, "teleport"),

        SPELL_ANIMATIONS_ALCHEMY(1, "alchemy"),

        WALK_ANIMATIONS(5),

        PVP_VICTIM_DEATH_ANIMATIONS(4),



        REST_ANIMATIONS(6);
        private final int subMenuId;
        private final String[] keywords;

        CosmeticsAnimation(int subMenuId, String... keywords) {
            this.subMenuId = subMenuId;
            this.keywords = keywords;
        }

        public int getSubMenuId() {
            return subMenuId;
        }

        public String[] getKeywords() {
            return keywords;
        }

    }
    private void deActivateAnimation(int dataId) {
        int index = getAnimationIndex(dataId);
        if (index == -1)
            return;
        checkAnimationSlots();
        activeAnimations[index] = 0;
        if (index == CosmeticsAnimation.WALK_ANIMATIONS.ordinal()) {
            player.getVarsManager().sendVarBit(26893, 0);
            return;
        }
    }

    public void open(CosmeticsHandler.CosmeticType type, boolean resetPurchaseItems) {
        checkAnimationSlots();
        player.getVarBitManager().setVarBit(COSMETIC_TYPE_MENU_VARBIT, type.type);
        player.getPackets().sendExecuteScript(6460, type.type);
        CosmeticsHandler.checkRequirementUnlockedCosmetics(player);
        if (resetPurchaseItems)
            purshaseItems = new PurchaseItem[0];
        if (purshaseItems.length == 0) {
            for (int i = 0; i < 22; i++)
                player.getPackets().sendUnlockIComponentOptionSlots(1311, 159 + i, 0, 3069, 0, 1);
            if (type == CosmeticsHandler.CosmeticType.WARDROBE) {
                player.getInterfaceManager().setMenuInterface(0, 1843);
                player.getInterfaceManager().setInterface(true, 1843, 264, 1841);
                player.getInterfaceManager().setInterface(true, 1841, 3, 1840);
                for (int i = 0; i < 8; i++)
                    player.getInterfaceManager().setInterface(true, 1843, 252 + i, 1832 + i);
                if (resetPurchaseItems || cosmeticPreview == null) {
                    cosmeticPreview = player.getEquipment().getCosmeticItems().getItemsCopy();
                    setSelectedPreset(-1);
                }
                refreshCosmeticsPreview();
                unlockButtons();
                refreshCosmeticPresets();
                player.getPackets().sendHideIComponent(1311, (ClientScriptMap.getMap(5961).getIntValue(1) & 0xFFF), false);
                sendRenderAnimation();
            } else {
                unlockCustomizationButtons();
                if (type == CosmeticsHandler.CosmeticType.ANIMATION)
                    sendPreviewAnimation(-1);
                else if (type == CosmeticsHandler.CosmeticType.PET)
                    refreshPetPreview(null);
            }
        } else {
            openPurchaseMenu();
        }
    }

    private void unlockCustomizationButtons() {
        for (int i = 0; i < CUSTOMISATION_COMPONENT_COUNT; i++) {
            player.getPackets().sendIComponentSettings(1311, CUSTOMISATION_CATEGORY_COMPONENT_START + i, 0, 1, 2);
            player.getPackets().sendIComponentSettings(1311, CUSTOMISATION_LIST_COMPONENT_START + i, 0, 3069, 6);
        }
        player.getPackets().sendIComponentSettings(1311, 470, 0, 3, 2);
        player.getPackets().sendIComponentSettings(1311, 498, 0, 3069, 6);
    }

    public void unlockButtons() {
        player.getPackets().sendIComponentSettings(1844, 0, -1, -1, 2);
        player.getPackets().sendIComponentSettings(1844, 1, -1, -1, 2);
        player.getPackets().sendIComponentSettings(1841, 9, 0, 18, 2046);
        player.getPackets().sendIComponentSettings(1843, 183, 0, 5000, 2046);
        player.getPackets().sendIComponentSettings(1843, 184, -1, -1, 2);
        player.getPackets().sendIComponentSettings(1843, 184, 0, 19, 60);
        player.getPackets().sendIComponentSettings(1843, 185, 0, 28, 2);
        player.getInterfaceManager().unlockDropDownMenu(0, 200);
    }

    public void setSelectedPreset(int selectedPreset) {
        this.selectedPreset = selectedPreset;
        refreshSelectedPreset();
    }

    public void refreshSelectedPreset() {
        player.getVarBitManager().sendVar(6843, selectedPreset);
        player.getPackets().sendExecuteScript(13187, 1, selectedPreset);

    }

    private void sendRenderAnimation() {
        Item weapon = cosmeticPreview == null ? null : cosmeticPreview[Equipment.SLOT_WEAPON];
        if (weapon == null)
            weapon = player.getEquipment().getItem(Equipment.SLOT_WEAPON);

        player.getPackets().sendGlobalConfig(779, weapon == null ? 2702 : weapon.getDefinitions().getRenderAnimId());
        player.getVarBitManager().sendVar(6867, weapon == null ? 2702 : weapon.getDefinitions().getRenderAnimId());
    }

    private void revert() {
        revert(-1);
    }

    private void revert(int slotId) {
        CosmeticsHandler.CosmeticType type = getCurrentCosmeticType();
        if (type == CosmeticsHandler.CosmeticType.WARDROBE) {
            if (slotId != -1)
                cosmeticPreview[slotId] = player.getEquipment().getCosmeticItems().get(slotId);
            else
                cosmeticPreview = player.getEquipment().getCosmeticItems().getItemsCopy();
            setSelectedPreset(-1);
            refreshCosmeticsPreview();
        } else if (type == CosmeticsHandler.CosmeticType.TITLE) {
//			sendPreviewTitle(player.getAppearence().getTitleId());
        } else if (type == CosmeticsHandler.CosmeticType.ANIMATION) {
            sendPreviewAnimation(-1);
        }
        sendPreviewOptions(-1, true);
    }

    public void close() {
        if (getCurrentCosmeticType() == CosmeticsHandler.CosmeticType.WARDROBE) {
            player.getPackets().sendAppearenceLook();
            player.getAppearence().generateAppearenceData();
            cosmeticPreview = null;
            if (player.getTemporaryAttributtes().get(Key.COSMETICS_RECOLOR) != null)
                closeRecolorInterface();
        }
        player.getVarBitManager().sendVarBit(COSMETIC_TYPE_MENU_VARBIT, 0);
    }

    private void setCosmetic(ClientScriptMap map, int slot, boolean preview) {
        int itemId = map.getIntValue(slot);
        if (itemId == -1)
            return;
        Item item = new Item(itemId);
        // no model <.<
        if (item.getDefinitions().equipSlot != -1 && (player.getAppearence().isMale() ? item.getDefinitions().getMaleWornModelId1() == -1 : item.getDefinitions().getFemaleWornModelId1() == -1)) {
            return;
        }
        cosmeticPreview[slot] = item;
        if (!preview) {
            Item oldItem = player.getEquipment().getCosmeticItems().get(slot);
            boolean remove = oldItem != null && oldItem.getId() == item.getId();
            player.getEquipment().getCosmeticItems().set(slot, remove ? null : item);
        }
    }

    private void sendPreviewOptions(int id, boolean hide) {
        if (id == -1 || hide)
            player.getTemporaryAttributtes().remove(Key.CURRENT_COSMETIC);
        else
            player.getTemporaryAttributtes().put(Key.CURRENT_COSMETIC, id);
    }

    private void setCosmetic(GeneralRequirementMap data, boolean preview) {
        ClientScriptMap map = ClientScriptMap.getMap(data.getIntValue(2542));
        if (data.getIntValue(2532) == 20) {
            if (data.getId() >= 35654 && data.getId() <= 35657) {
                CosmeticPreset preset = cosmeticPresets[data.getId() - 35654];
                if (preset.hasItems()) {
                    cosmeticPreview = preset.getPresetItems();
                    refreshCosmeticsPreview();
                    sendRenderAnimation();
                    return;
                }
            }
            if (map == null || map.getValues() == null) {
                setSelectedPreset(-1);
                return;
            }
            cosmeticPreview = new Item[BodyDefinitions.getEquipmentContainerSize()];
            if (!preview)
                player.getEquipment().getCosmeticItems().reset();
            for (Long slot : map.getValues().keySet())
                setCosmetic(map, slot.intValue(), preview);
        } else {
            int slot = data.getIntValue(2532);
            cosmeticPreview[slot] = null;
            setCosmetic(map, slot, preview);
        }
        refreshCosmeticsPreview();
        sendRenderAnimation();
        if (preview)
            sendPreviewOptions(data.getId(), !preview);
    }

    private void refreshCosmeticsPreview() {
        player.getPackets().sendCustomPlayerOnIComponent(1840, 3, 1);
        player.getPackets().sendCustomPlayerAppearanceLook(cosmeticRecolorPreview != null ? cosmeticRecolorPreview : cosmeticPreview, player.getAppearence().getBodyStyle(), 1);
        player.getPackets().sendItems(817, player.getEquipment().getCosmeticItems());
        player.getPackets().sendItems(818, cosmeticPreview);
    }

    private static boolean isAutoUnlocked(int id) {
        for (int i : FREE_COSMETICS)
            if (i == id)
                return true;
        return false;
    }

    private void unlockOutfit(int outfitId, int secondOutfitId) {
        for (int menuId = 0; menuId < BodyDefinitions.getEquipmentContainerSize(); menuId++) {
            int mapId = ClientScriptMap.getMap(8464).getIntValue(menuId);
            if (mapId == -1)
                continue;
            ClientScriptMap map = ClientScriptMap.getMap(mapId);
            for (Object id : map.getValues().values()) {
                GeneralRequirementMap data = GeneralRequirementMap.getMap((int) id);
                if (data.getIntValue(4419) == outfitId || (secondOutfitId != -1 && data.getIntValue(4419) == secondOutfitId)) {
                    if(!unlockedCosmetics.contains(data.getId()))
                        unlockedCosmetics.add(data.getId());
                }
            }
        }
        if(!unlockedCosmetics.contains(outfitId))
            unlockedCosmetics.add(outfitId);
        if (secondOutfitId != -1) {
            if(!unlockedCosmetics.contains(secondOutfitId))
                unlockedCosmetics.add(secondOutfitId);
        }
    }

    public void unlockItem(int itemId) {
        int dataId = ItemDefinitions.getItemDefinitions(itemId).getCSOpcode(4414);
        if (dataId <= 0)
            return;
        GeneralRequirementMap data = GeneralRequirementMap.getMap(dataId);
        if (!(isAutoUnlocked(data.getId()) || hasCosmeticVar(data.getId(), data.getIntValue(2546) == 1)) || unlockedCosmetics.contains(data.getId()))
            return;
        int realDataId = data.getIntValue(4419) != 0 ? data.getIntValue(4419) : dataId;
        unlockItem(GeneralRequirementMap.getMap(realDataId));
    }

    /*
     * unlocks whole outfit items if its outfit or part of one, else jut item itself
     */
    public void unlockItem(GeneralRequirementMap data) {
        unlockItem(data, -1);
    }

    public void unlockItem(GeneralRequirementMap data, int secondOutfitId) {
        if (data == null || data.getValues() == null)
            return;
        if (data.getValues().containsKey((long) 4717)) {
            for (Object o : ClientScriptMap.getMap(data.getIntValue(4717)).getValues().values()) {
                if ((int) o != data.getId())
                    unlockItem(GeneralRequirementMap.getMap((int) o), data.getId());
            }
        }
        if (data.getId() == 28360 || data.getId() == 28266) {
            if(!unlockedCosmetics.contains(28360))
                unlockedCosmetics.add(28360);
            if(!unlockedCosmetics.contains(28266))
                unlockedCosmetics.add(28266);
            return;
        }
        if (data.getIntValue(2532) == 20 || data.getValues().containsKey((long) 4717))
            unlockOutfit(data.getId(), secondOutfitId);
        else {
            int outfitId = data.getIntValue(4419);
            if (outfitId > 0)
                unlockOutfit(outfitId, secondOutfitId);
            else {
                if(!unlockedCosmetics.contains(data.getId()))
                    unlockedCosmetics.add(data.getId());
            }
        }
        if (data.getId() == 30793 || data.getId() >= 30796 && data.getId() <= 30800 || data.getId() == 30794) {
            boolean hasAll = unlockedCosmetics.contains(30793) && unlockedCosmetics.contains(30794);
            for (int i = 30796; i <= 30800; i++)
                if (!unlockedCosmetics.contains(i)) {
                    hasAll = false;
                    break;
                }
            if (hasAll) {
                for (int i = 30773; i <= 30782; i++)
                    if(!unlockedCosmetics.contains(i))
                        unlockedCosmetics.add(i);
            }
        }
        if (data.getId() == 29760 || data.getIntValue(4419) == 29760 || data.getId() == 29765)
            if(!unlockedCosmetics.contains(29760))
                unlockedCosmetics.add(29760);
    }

    /*
     * -1 all
     */
    private void clearCosmetic(boolean preview, int slot) {
        if (preview) {
            if (slot == -1) {
                cosmeticPreview = new Item[BodyDefinitions.getEquipmentContainerSize()];
                for (int i = 0; i < 19; i++)
                    player.getEquipment().getCosmeticItems().set(slot, null);
            } else {
                cosmeticPreview[slot] = null;
                player.getEquipment().getCosmeticItems().set(slot, null);
            }
            player.getPackets().sendAppearenceLook();
            player.getAppearence().generateAppearenceData();

        } else {
            if (slot == -1)
                player.getEquipment().getCosmeticItems().reset();
            else
                player.getEquipment().getCosmeticItems().set(slot, null);
        }

        refreshCosmeticsPreview();

    }

    public int getKeepSakeItemsCount() {
        int count = 0;
        for (int i = 0; i < player.getEquipment().getKeepSakeItems().size(); i++) {
            Item keepSaked = player.getEquipment().getKeepSakeItems().get(i);
            if (keepSaked == null)
                continue;
            count++;
        }
        return count;
    }

    private void setCosmetic(Item item, boolean preview) {
        int slot = item.getDefinitions().getEquipSlot();
        cosmeticPreview[slot] = null;
        if (item.getDefinitions().equipSlot != -1 && (player.getAppearence().isMale() ? item.getDefinitions().getMaleWornModelId1() == -1 : item.getDefinitions().getFemaleWornModelId1() == -1)) {
            return;
        }
        cosmeticPreview[slot] = item;
        if (!preview) {
            Item oldItem = player.getEquipment().getCosmeticItems().get(slot);
            boolean remove = oldItem != null && oldItem.getId() == item.getId();
            player.getEquipment().getCosmeticItems().set(slot, remove ? null : item);
        }
        refreshCosmeticsPreview();
        sendRenderAnimation();
    }

    public void openReclaimKeepInterface(int index) {
        if (player.getInterfaceManager().containsScreenInter()) {
            player.getPackets().sendGameMessage("Please finish what you're doing before opening the reclaim Keepsake interface.");
            return;
        }
        player.getDialogueManager().startDialogue(new Dialogue() {
            @Override
            public void start() {
                player.getInterfaceManager().sendInterface(1048);
                player.getPackets().sendExecuteScript(9727, "Reclaim Keepsake", "Are you sure you want to reclaim this item? It will be placed in your inventory or bank.<br><br>You will not be able to reclaim your Keepsake Key.", "OK", "Cancel", "", -1, 48);
            }

            @Override
            public void run(int interfaceId, int componentId) {
                if (componentId == 19) {
                    Item item = player.getEquipment().getKeepSakeItems().get(index);
                    if (item == null)
                        return;
                    int equipSlot = item.getDefinitions().getEquipSlot();
                    Item cosmetic = player.getEquipment().getCosmeticItems().get(equipSlot);
                    if (cosmetic != null && cosmetic.getId() == item.getId())
                        clearCosmetic(false, equipSlot);
                    if (cosmeticPreview[equipSlot] != null && cosmeticPreview[equipSlot].getId() == item.getId())
                        cosmeticPreview[equipSlot] = null;
                    player.getEquipment().getKeepSakeItems().remove(item);
                    player.getPackets().sendItems(675, player.getEquipment().getKeepSakeItems().toArray(new Item[player.getEquipment().getKeepSakeItems().size()]));
                    player.addItem(item);
                    refreshCosmeticsPreview();
                }
                end();
            }

            @Override
            public void finish() {
                player.getInterfaceManager().closeScreenInterface();
            }
        });
    }

    public int getNextIndex() {
        for (int i = 0; i < player.getEquipment().getKeepSakeItems().size(); i++) {
            Item keepSaked = player.getEquipment().getKeepSakeItems().get(i);
            if (keepSaked == null)
                return i;
        }
        return -1;
    }

    public void handleButtons(int interfaceId, int componentId, int slotId, int slotId2, int packetId) {
        if (interfaceId == 1843) {
            if (componentId == 183) {
                if (packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
                    if (slotId >= 2333 && slotId <= 2432) {
                        int index = slotId - 2333;
                        if (index >= getKeepSakeItemsCount())
                            return;
                        Item item = this.player.getEquipment().getKeepSakeItems().get(index);
                        if (item == null)
                            return;
                        setSelectedPreset(slotId);
                        setCosmetic(item, true);
                        return;
                    }
                    int dataId = getDataId(slotId);
                    if (dataId == -1)
                        return;
                    GeneralRequirementMap data = GeneralRequirementMap.getMap(dataId);
                    if (data.getId() >= 35654 && data.getId() <= 35657) {// presets
                        setSelectedPreset(slotId);
                        setCosmetic(data, true); // preview cosmetic
                        return;
                    }
                    if (!(isAutoUnlocked(data.getId()) || hasCosmeticVar(data.getId(), data.getIntValue(2546) == 1)) && player.getRights() == 2)
                        player.getPackets().sendGameMessage("This cosmetic var is not currently added. Name: " + data.getValue(2533) + ", dataId=" + data.getId());
                    // System.out.print("VAR " + data.getId() + " " +
                    // data.toString());
                    setSelectedPreset(slotId);
                    setCosmetic(data, true); // preview cosmetic
                } else if (packetId == PacketRepository.ACTION_BUTTON8_PACKET) {
                    if (slotId >= 2333 && slotId <= 2432) {
                        int index = slotId - 2333;
                        if (index >= getKeepSakeItemsCount())
                            return;
                        Item item = player.getEquipment().getKeepSakeItems().get(index);
                        if (item == null)
                            return;
                        openReclaimKeepInterface(index);
                        return;
                    }
                    openRenamePresetInterface(slotId);
                } else {
                    int customIndex = packetId == PacketRepository.ACTION_BUTTON2_PACKET ? 2 : packetId == PacketRepository.ACTION_BUTTON3_PACKET ? 3 : packetId == PacketRepository.ACTION_BUTTON4_PACKET ? 4 : packetId == PacketRepository.ACTION_BUTTON5_PACKET ? 5 : packetId == PacketRepository.ACTION_BUTTON9_PACKET ? 6 : packetId == PacketRepository.ACTION_BUTTON6_PACKET ? 7 : packetId == PacketRepository.ACTION_BUTTON7_PACKET ? 8 : 9;
                    setCosmeticsPreview(customIndex, slotId);
                }
            } else if (componentId == 309) {
                apply();
            } else if (componentId == 279)
                revert();
            else if (componentId == 323) {
                clearCosmetic(true, -1);
                setSelectedPreset(-2);
            } else if (componentId == 302) {
                player.sendDropDownMenuEvent(new DropDownMenuEvent() {

                    @Override
                    public void run(Player player) {
                        player.getCosmeticsManager().openSavePresetInterface(getSlotId());
                    }
                });
            } else if (componentId == 271) {
                openRecolorInterface();
            } else if (componentId == 286) {
                GeneralRequirementMap data = GeneralRequirementMap.getMap(5692);

                if (!(isAutoUnlocked(data.getId()) || hasCosmeticVar(data.getId(), data.getIntValue(2546) == 1)) && player.getRights() == 2)
                    player.getPackets().sendGameMessage("This cosmetic var is not currently added. Name: " + data.getValue(2533));
                // System.out.print("VAR " + data.getId() + " " +
                // data.toString());
                setSelectedPreset(-3);
                setCosmetic(data, true); // preview cosmetic
            } else if (componentId == 184) {
                if (packetId == PacketRepository.ACTION_BUTTON2_PACKET) {// undo
                    // change
                    revert(slotId);
                    setSelectedPreset(0);
                } else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET) {// clear
                    // slot
                    clearCosmetic(true, slotId);
                    setSelectedPreset(0);
                } else if (packetId == PacketRepository.ACTION_BUTTON4_PACKET) {// hide
                    // slot
                    ClientScriptMap map = ClientScriptMap.getMap(8043);
                    if (map == null || map.getValues() == null)
                        return;
                    setCosmetic(map, slotId, true);
                    refreshCosmeticsPreview();
                    sendRenderAnimation();
                    setSelectedPreset(0);
                } else if (packetId == PacketRepository.ACTION_BUTTON5_PACKET) {// recolor
                    openRecolorInterface(slotId);
                }
            }
        } else if (interfaceId == 1627) {
            Integer recolorSlot = (Integer) player.getTemporaryAttributtes().get(Key.COSMETICS_RECOLOR);
            if (recolorSlot == null) {
                player.getPackets().sendHideIComponent(1843, 313, true);
                player.getPackets().closeInterface(InterfaceManager.getComponentUId(1843, 315));
                return;
            }
            if (componentId == 39 || componentId == 53) {// close/cancel
                closeRecolorInterface();
                setSelectedPreset(-5);
                refreshCosmeticsPreview();
            } else if (componentId == 45) {
                applyRecolor();
            } else if (componentId == 12) {
                recolor(slotId);
            }
        } else if (interfaceId == 1830) {
            if (componentId == 15) {
                player.getPackets().sendIComponentInputText(1830, 12, 80);
                player.sendIComponentInputString(1830, 12, 80, new InputStringEvent() {

                    @Override
                    public void run(Player player) {
                        Integer index = (Integer) player.getTemporaryAttributtes().get(Key.RENAME_PRESET_INDEX);
                        if (index == null)
                            return;
                        player.getCosmeticsManager().updatePresetName(index, getString());
                    }
                });
            } else if (componentId == 32) {
                Integer index = (Integer) player.getTemporaryAttributtes().get(Key.RENAME_PRESET_INDEX);
                if (index == null)
                    return;
                updatePresetName(index, "");
            }
        } else if (interfaceId == 1845) {
            if (componentId == 137) {
                closePurchaseMenu();
            } else if (componentId == 211) {
                applyPurchase();
            } else if (componentId >= 145 && componentId <= 205) {
                undoChange((componentId - 145) / 6);
            } else if (componentId >= 38 && componentId <= 128) {
                purchase((componentId - 38) / 9);
            }
        } else if (interfaceId == 1311) {
            handleCustomizationButtons(componentId, slotId, packetId);
        }
    }

    private void handleCustomizationButtons(int componentId, int slotId, int packetId) {
        CosmeticsHandler.CosmeticType type = getCurrentCosmeticType();
        if (type == null || type == CosmeticsHandler.CosmeticType.WARDROBE)
            return;
        if (isCustomisationCategoryComponent(componentId)) {
            player.getTemporaryAttributtes().put(SELECTED_CUSTOMISATION_SUBMENU, componentId - CUSTOMISATION_CATEGORY_COMPONENT_START);
            sendPreviewOptions(-1, true);
            return;
        }
        if (isCustomisationListComponent(componentId)) {
            int subMenu = componentId - CUSTOMISATION_LIST_COMPONENT_START;
            player.getTemporaryAttributtes().put(SELECTED_CUSTOMISATION_SUBMENU, subMenu);
            GeneralRequirementMap data = getCosmeticData(type, slotId, subMenu);
            if (data == null || data.getValues() == null)
                return;
            previewCustomization(type, data);
            if (packetId == PacketRepository.ACTION_BUTTON1_PACKET || packetId == PacketRepository.ACTION_BUTTON2_PACKET)
                activateCustomization(type, data);
        } else if (componentId == 498) {
            GeneralRequirementMap data = getCosmeticData(type, slotId, getSelectedCustomisationSubMenu());
            if (data == null || data.getValues() == null)
                return;
            previewCustomization(type, data);
            if (packetId == PacketRepository.ACTION_BUTTON1_PACKET || packetId == PacketRepository.ACTION_BUTTON2_PACKET)
                activateCustomization(type, data);
        } else if (componentId == 461 || componentId == 454) {
            Integer dataId = (Integer) player.getTemporaryAttributtes().get(Key.CURRENT_COSMETIC);
            if (dataId != null)
                activateCustomization(type, GeneralRequirementMap.getMap(dataId));
        }
    }

    private boolean isCustomisationCategoryComponent(int componentId) {
        return componentId >= CUSTOMISATION_CATEGORY_COMPONENT_START
                && componentId < CUSTOMISATION_CATEGORY_COMPONENT_START + CUSTOMISATION_COMPONENT_COUNT;
    }

    private boolean isCustomisationListComponent(int componentId) {
        return componentId >= CUSTOMISATION_LIST_COMPONENT_START
                && componentId < CUSTOMISATION_LIST_COMPONENT_START + CUSTOMISATION_COMPONENT_COUNT;
    }

    private int getSelectedCustomisationSubMenu() {
        Integer subMenu = (Integer) player.getTemporaryAttributtes().get(SELECTED_CUSTOMISATION_SUBMENU);
        return subMenu == null ? -1 : subMenu;
    }

    private void previewCustomization(CosmeticsHandler.CosmeticType type, GeneralRequirementMap data) {
        if (data == null || data.getValues() == null)
            return;
        sendPreviewOptions(data.getId(), false);
        if (type == CosmeticsHandler.CosmeticType.ANIMATION) {
            sendPreviewAnimation(data.getId());
        } else if (type == CosmeticsHandler.CosmeticType.TITLE) {
            sendPreviewTitle(data);
        } else if (type == CosmeticsHandler.CosmeticType.PET) {
            refreshPetPreview(data);
        }
    }

    private void activateCustomization(CosmeticsHandler.CosmeticType type, GeneralRequirementMap data) {
        if (data == null || data.getValues() == null)
            return;
        if (!isUnlockedForUse(data)) {
            player.getPackets().sendGameMessage("You have not unlocked that customisation.");
            return;
        }
        if (type == CosmeticsHandler.CosmeticType.ANIMATION) {
            activateAnimation(data);
        } else if (type == CosmeticsHandler.CosmeticType.TITLE) {
            activateTitle(data);
        } else if (type == CosmeticsHandler.CosmeticType.PET) {
            activatePet(data);
        } else if (type == CosmeticsHandler.CosmeticType.APPEARENCE) {
            activateAppearance(data);
        }
    }

    private boolean isUnlockedForUse(GeneralRequirementMap data) {
        return data.getId() == 11176 || data.getIntValue(2534) == -1 || isAutoUnlocked(data.getId()) || unlockedCosmetics.contains(data.getId()) || hasCosmeticVar(data.getId(), data.getIntValue(2546) == 1);
    }

    private void activateAnimation(GeneralRequirementMap data) {
        int index = getAnimationIndex(data.getId());
        if (index == -1) {
            player.getPackets().sendGameMessage("That animation customisation is not supported yet.");
            return;
        }
        checkAnimationSlots();
        if (activeAnimations[index] == data.getId()) {
            deActivateAnimation(data.getId());
            sendPreviewAnimation(-1);
            player.getAppearence().generateAppearenceData();
            return;
        }
        activeAnimations[index] = data.getId();
        if (index == CosmeticsAnimation.WALK_ANIMATIONS.ordinal())
            player.getVarsManager().sendVarBit(26893, data.getIntValue(5249));
        sendPreviewAnimation(data.getId());
        player.getAppearence().generateAppearenceData();
    }

    private void activateTitle(GeneralRequirementMap data) {
        int titleId = data.getIntValue(2543);
        player.getAppearence().setTitle(titleId);
        player.getAppearence().generateAppearenceData();
        sendPreviewTitle(data);
    }

    private void activateAppearance(GeneralRequirementMap data) {
        int style = data.getIntValue(2772);
        int subMenu = data.getIntValue(2532);
        if (subMenu == 1 || subMenu == 3 || subMenu == 4) {
            player.getAppearence().setHairStyle(style);
        } else if (subMenu == 2 || subMenu == 5) {
            if (!player.getAppearence().isMale()) {
                player.getPackets().sendGameMessage("Beard customisations can only be used on male characters.");
                return;
            }
            player.getAppearence().setBeardStyle(style);
        } else {
            player.getPackets().sendGameMessage("That appearance customisation is not supported yet.");
            return;
        }
        player.getAppearence().generateAppearenceData();
        player.getPackets().sendAppearenceLook();
    }

    private void activatePet(GeneralRequirementMap data) {
        Pets pet = findPet(data);
        if (pet == null) {
            player.getPackets().sendGameMessage("That pet is not mapped to the server pet system yet.");
            refreshPetPreview(data);
            return;
        }
        PetPerkUtils.summonPet(player, pet);
    }


    public void apply() {
        CosmeticsHandler.checkRequirementUnlockedCosmetics(player);
        List<PurchaseItem> items = new ArrayList<PurchaseItem>();
        for (int i = 0; i < cosmeticPreview.length; i++) {
            String message = cosmeticPreview[i] == null ? null : CosmeticsHandler.getEarnedMessageRequirement(player, cosmeticPreview[i].getId());
            if (message != null) {
                int dataId = cosmeticPreview[i].getDefinitions().getCSOpcode(4414);
                GeneralRequirementMap data = GeneralRequirementMap.getMap(dataId);
                player.getPackets().sendGameMessage(message.replace("this", data.getStringValue(2533)));
                continue;
            }
            if (cosmeticPreview[i] != null) {
                if (player.getEquipment().getKeepSakeItems().contains(cosmeticPreview[i]))
                    continue;
                int dataId = cosmeticPreview[i].getDefinitions().getCSOpcode(4414);
                if (dataId <= 0)
                    continue;
                GeneralRequirementMap data = GeneralRequirementMap.getMap(dataId);
                if (!(isAutoUnlocked(data.getId()) || hasCosmeticVar(data.getId(), data.getIntValue(2546) == 1)) || unlockedCosmetics.contains(data.getId()))
                    continue;
                PurchaseItem item = new PurchaseItem(data.getIntValue(4419) != 0 ? data.getIntValue(4419) : dataId);
                if (!items.contains(item))
                    items.add(item);
            }
//            player.getEquipment().getCosmeticItems().set(i, cosmeticPreview[i]);
        }
        if (!items.isEmpty()) {
            purshaseItems = items.toArray(new PurchaseItem[items.size()]);
            openPurchaseMenu();
            return;
        }
        for (int i = 0; i < cosmeticPreview.length; i++) {
            String message = cosmeticPreview[i] == null ? null : CosmeticsHandler.getEarnedMessageRequirement(player, cosmeticPreview[i].getId());
            if (message != null) {
//                player.getPackets().sendGameMessage(message);
                continue;
            }
            if (cosmeticPreview[i] == null) {
                player.getEquipment().getCosmeticItems().set(i, cosmeticPreview[i]);
                continue;
            }
            if (cosmeticPreview[i] != null && player.getEquipment().getKeepSakeItems().contains(cosmeticPreview[i])) {
                player.getEquipment().getCosmeticItems().set(i, cosmeticPreview[i]);
                continue;
            }
            int dataId = cosmeticPreview[i].getDefinitions().getCSOpcode(4414);
            if (dataId <= 0) {
                player.getEquipment().getCosmeticItems().set(i, null);
                continue;
            }
            GeneralRequirementMap data = GeneralRequirementMap.getMap(dataId);
            if (!(isAutoUnlocked(data.getId()) || hasCosmeticVar(data.getId(), data.getIntValue(2546) == 1)) && !unlockedCosmetics.contains(data.getId())) {
                player.getEquipment().getCosmeticItems().set(i, null);
                continue;
            }
            player.getEquipment().getCosmeticItems().set(i, cosmeticPreview[i]);
        }
        for (int i = 0; i < 4; i++)
            player.getPackets().sendExecuteScript(13185, 35654 + i);
        player.getAppearence().generateAppearenceData();
        refreshVars();
        revert();
    }

    public static GeneralRequirementMap getCosmeticData(CosmeticsHandler.CosmeticType type, int cosmeticId) {
        return getCosmeticData(type, cosmeticId, -1);
    }

    public static GeneralRequirementMap getCosmeticData(CosmeticsHandler.CosmeticType type, int cosmeticId, int subMenu) {
        GeneralRequirementMap direct = getCosmeticDataByKey(type, cosmeticId);
        GeneralRequirementMap divided = cosmeticId >= 0 ? getCosmeticDataByKey(type, cosmeticId / 3) : null;
        if (matchesCosmeticData(type, direct, subMenu))
            return direct;
        if (matchesCosmeticData(type, divided, subMenu))
            return divided;
        if (isValidCosmeticData(type, direct))
            return direct;
        return isValidCosmeticData(type, divided) ? divided : null;
    }

    private static GeneralRequirementMap getCosmeticDataByKey(CosmeticsHandler.CosmeticType type, int key) {
        if (key < 0)
            return null;
        ClientScriptMap ids = getCosmeticsIds(type);
        if (ids == null || ids.getValues() == null)
            return null;
        int dataId = ids.getIntValue(key);
        if (dataId <= 0)
            return null;
        return GeneralRequirementMap.getMap(dataId);
    }

    private static boolean matchesCosmeticData(CosmeticsHandler.CosmeticType type, GeneralRequirementMap data, int subMenu) {
        return isValidCosmeticData(type, data) && (subMenu == -1 || data.getIntValue(2532) == subMenu);
    }

    private static boolean isValidCosmeticData(CosmeticsHandler.CosmeticType type, GeneralRequirementMap data) {
        return data != null && data.getValues() != null && data.getIntValue(2531) == type.type;
    }

    public void refreshShowingAllItems() {
        player.getVarBitManager().sendVarBit(678, showingAllItems ? 1 : 0);
    }

    public void switchShowingAllItems() {
        showingAllItems = !showingAllItems;
        refreshShowingAllItems();
    }

    public CosmeticsHandler.CosmeticType getCurrentCosmeticType() {
        int id = player.getVarBitManager().getBitValue(COSMETIC_TYPE_MENU_VARBIT);
        for (CosmeticsHandler.CosmeticType type : CosmeticsHandler.CosmeticType.values())
            if (type.type == id) {
                return type;
            }
        return null;
    }

    public static ClientScriptMap getCosmeticsIds(CosmeticsHandler.CosmeticType type) {
        return ClientScriptMap.getMap(ClientScriptMap.getMap(5958).getIntValue(type.type));
    }

    /*
     * animation and so on
     */
    public void sendPreviewAnimation(int id) {
        player.getPackets().sendExecuteScript(2716, id);
    }

    private void sendPreviewTitle(GeneralRequirementMap data) {
        player.getPackets().sendExecuteScript(7609, data == null ? -1 : data.getId());
    }

    private void refreshPetPreview(GeneralRequirementMap data) {
        if (data == null) {
            player.getPackets().sendExecuteScript(7422, -1, -1, 0, 0, 0);
            player.getPackets().sendExecuteScript(7425, "");
            return;
        }
        Pets pet = findPet(data);
        String name = data.getStringValue(2533);
        player.getPackets().sendExecuteScript(7424, "Baby", name);
        player.getPackets().sendExecuteScript(7425, name);
        if (pet == null) {
            player.getPackets().sendExecuteScript(7422, -1, -1, 0, data.getIntValue(2540), data.getIntValue(2541));
            return;
        }
        player.getPackets().sendExecuteScript(7422, pet.getBabyNpcId(), pet.getBabyItemId(), 0, data.getIntValue(2540), data.getIntValue(2541));
    }

    private Pets findPet(GeneralRequirementMap data) {
        String cosmeticName = normalizeName(data.getStringValue(2533));
        if (cosmeticName.isEmpty())
            return null;
        for (Pets pet : Pets.values()) {
            if (normalizeName(pet.name()).equals(cosmeticName))
                return pet;
            if (matchesPetNpcName(cosmeticName, pet.getBabyNpcId())
                    || matchesPetNpcName(cosmeticName, pet.getGrownNpcId())
                    || matchesPetNpcName(cosmeticName, pet.getOvergrownNpcId()))
                return pet;
        }
        return null;
    }

    private boolean matchesPetNpcName(String cosmeticName, int npcId) {
        if (npcId <= 0)
            return false;
        return normalizeName(NPCDefinitions.getNPCDefinitions(npcId).getName()).equals(cosmeticName);
    }

    private static String normalizeName(String name) {
        return name == null ? "" : name.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    private transient int selectedPreset = -1;

    public int getDataId(int slotId) {
        int start = 0;
        int end = 0;
        for (int i = 0; i < 21; i++) {
            ClientScriptMap map = ClientScriptMap.getMap(ClientScriptMap.getMap(8464).getIntValue(i));
            if (map == null || map.getValues() == null)
                continue;
            start = end + (i == 0 ? 0 : 1);
            end += map.getSize() - (i == 0 ? 1 : 0);
            if (slotId >= start && slotId <= end)
                return map.getIntValue(slotId - start);
        }
        return -1;
    }

    private void setCosmeticsPreview(int customIndex, int slotId) {
        Item[] items = new Item[BodyDefinitions.getEquipmentContainerSize()];
        if (slotId >= 2333 && slotId <= (2333 + player.getEquipment().getKeepSakeItems().size() - 1)) {
            Item item = player.getEquipment().getKeepSakeItems().get(slotId - 2333);
            if (item == null)
                return;
            ClientScriptMap map = ClientScriptMap.getMap(12221);
            if (map == null || map.getValues() == null)
                return;
            for (Long slot : map.getValues().keySet()) {
                int itemId = map.getIntValue(slot);
                if (itemId == -1)
                    continue;
                items[slot.intValue()] = new Item(itemId);
            }
            int equipSlot = item.getDefinitions().getEquipSlot();
            items[equipSlot] = item;
        } else {
            int dataId = getDataId(slotId);
            GeneralRequirementMap data = GeneralRequirementMap.getMap(dataId);
            ClientScriptMap map = ClientScriptMap.getMap(data.getIntValue(2542));
            if (data.getIntValue(2532) == 20) {
                if (data.getId() >= 35654 && data.getId() <= 35657) {
                    CosmeticPreset preset = cosmeticPresets[data.getId() - 35654];
                    if (preset.hasItems()) {
                        player.getPackets().sendCustomPlayerAppearanceLook(preset.getPresetItems(), player.getAppearence().getBodyStyle(), customIndex);
                        player.getPackets().sendCustomPlayerOnIComponent(1830 + customIndex, 5, customIndex);
                        player.getPackets().sendExecuteScript(13187, customIndex, slotId);
                        return;
                    }
                }
                if (map == null || map.getValues() == null) {
                    map = ClientScriptMap.getMap(12221);
                }
                for (Long slot : map.getValues().keySet()) {
                    int itemId = map.getIntValue(slot);
                    if (itemId == -1)
                        continue;
                    items[slot.intValue()] = new Item(itemId);
                }
            } else {
                int slot = data.getIntValue(2532);
                int itemId = map.getIntValue(slot);
                if (itemId == -1)
                    return;
                items[slot] = new Item(itemId);
            }
        }
        player.getPackets().sendCustomPlayerAppearanceLook(items, player.getAppearence().getBodyStyle(), customIndex);
        player.getPackets().sendCustomPlayerOnIComponent(1830 + customIndex, 5, customIndex);
        player.getPackets().sendExecuteScript(13187, customIndex, slotId);
    }

    private transient Item[] cosmeticRecolorPreview;

    public void openRecolorInterface() {
        openRecolorInterface(-1);
    }

    public void openRecolorInterface(int slotId) {
        cosmeticRecolorPreview = new Item[BodyDefinitions.getEquipmentContainerSize()];
        boolean allNull = true;
        Item[] temp = cosmeticPreview == null ? player.getEquipment().getCosmeticItems().getItemsCopy() : cosmeticPreview;
        for (int i = 0; i < temp.length; i++) {
            if (temp[i] == null)
                continue;
            cosmeticRecolorPreview[i] = new Item(temp[i].getId(), temp[i].getAmount());
            if (cosmeticRecolorPreview[i].getId() >= 47885 && cosmeticRecolorPreview[i].getId() <= 47890) {
                cosmeticRecolorPreview[i] = null;
                continue;
            }
            if (cosmeticRecolorPreview[i] != null)
                allNull = false;
            if (temp[i].getCustomColor() != null)
                cosmeticRecolorPreview[i].setCustomColor(temp[i].getCustomColor());
        }
        if (allNull) {
            player.getPackets().sendMainInterfaceMessage(1, "You can't recolour this from here.", true);
            return;
        }
        setSelectedPreset(-5);
        player.getPackets().sendGlobalConfig(1967, 0);
        player.getPackets().sendHideIComponent(1843, 313, false);
        player.getInterfaceManager().setInterface(true, 1843, 315, 1627);
        player.getPackets().sendExecuteScript(11509, 29563, 11063, 0, 0);
        player.getPackets().sendIComponentSettings(1627, 12, 0, 15, 2);
        player.getPackets().sendIComponentSettings(1627, 21, 0, 1023, 6);
        player.getPackets().sendIComponentSettings(1627, 34, 0, 63, 2);
        player.getPackets().sendIComponentSettings(1627, 36, 0, 127, 2);
        player.getTemporaryAttributtes().put(Key.COSMETICS_RECOLOR, slotId);
    }

    public void closeRecolorInterface() {
        cosmeticRecolorPreview = null;
        player.getTemporaryAttributtes().remove(Key.COSMETICS_RECOLOR);
        player.getPackets().sendHideIComponent(1843, 313, true);
        player.getPackets().closeInterface(InterfaceManager.getComponentUId(1843, 315));
    }

    public void recolor(int slotId) {
        Integer slot = (Integer) player.getTemporaryAttributtes().get(Key.COSMETICS_RECOLOR);
        if (slot == null) {
            closeRecolorInterface();
            setSelectedPreset(-5);
            return;
        }

        GeneralRequirementMap colorMap = GeneralRequirementMap.getMap(11064 + slotId);
        int[] color = new int[4];
        for (int i = 0; i < color.length; i++)
            color[i] = colorMap.getIntValue(2502 + i);
        if (slot == -1) {
            for (Item item : cosmeticRecolorPreview)
                if (item != null)
                    item.setCustomColor(color);
        } else {
            cosmeticRecolorPreview[slot].setCustomColor(color);
        }
        // player.getPackets().sendAppearenceLook();
        // player.getAppearence().generateAppearenceData();
        player.getPackets().sendGlobalConfig(4888, slotId);
        refreshCosmeticsPreview();
    }

    public void applyRecolor() {
        this.cosmeticPreview = cosmeticRecolorPreview;
        closeRecolorInterface();
        refreshCosmeticsPreview();
        setSelectedPreset(-5);
    }

    private CosmeticPreset[] cosmeticPresets;

    public CosmeticPreset[] getCosmeticPresets() {
        return cosmeticPresets;
    }

    public void updatePresetItems(int index) {
        cosmeticPresets[index].setPresetItems(cosmeticPreview);
        player.getPackets().sendExecuteScript(9729);
        player.getPackets().sendExecuteScript(8840, 48, 0);
        player.getPackets().sendExecuteScript(13185, 35654 + index);
    }

    public void updatePresetName(int index, String name) {
        cosmeticPresets[index].setName(name);
        player.getPackets().sendGlobalString(5944, name);
        refreshCosmeticPresets();
    }

    public void openSavePresetInterface(int index) {
        if (player.getInterfaceManager().containsScreenInter()) {
            player.getPackets().sendGameMessage("Please finish what you're doing before opening the save preset interface.");
            return;
        }
        boolean hasAll = true;
        for (int i = 0; i < cosmeticPreview.length; i++) {
            String message = cosmeticPreview[i] == null ? null : CosmeticsHandler.getEarnedMessageRequirement(player, cosmeticPreview[i].getId());
            if (message != null) {
                player.getPackets().sendGameMessage(message);
                hasAll = false;
                break;
            }
            if (cosmeticPreview[i] != null) {
                if (player.getEquipment().getKeepSakeItems().contains(cosmeticPreview[i]))
                    continue;
                int dataId = cosmeticPreview[i].getDefinitions().getCSOpcode(4414);
                if (dataId <= 0) {
                    hasAll = false;
                    break;
                }
                if (isAutoUnlocked(dataId) || unlockedCosmetics.contains(dataId))
                    continue;
                hasAll = false;
                break;
            }
        }
        if (!hasAll) {
            player.getPackets().sendGameMessage("You don't own all of the items to set them as a preset.");
            return;
        }
        CosmeticPreset preset = cosmeticPresets[index];
        player.getPackets().sendExecuteScript(8178);
        String name = preset.getName();
        player.getDialogueManager().startDialogue(new Dialogue() {
            @Override
            public void start() {
                player.getInterfaceManager().sendInterface(1048);
                player.getPackets().sendExecuteScript(9727, "Save Preset", "This will overwrite the previously saved preset (" + (name.equals("") ? ("Preset " + (index + 1)) : name) + ").<br>Are you sure you wish to continue?", "OK", "Cancel", "", -1, 48);
            }

            @Override
            public void run(int interfaceId, int componentId) {
                if (componentId == 19)
                    updatePresetItems(index);
                end();
            }

            @Override
            public void finish() {
                player.getInterfaceManager().closeScreenInterface();
            }
        });
    }

    public void openRenamePresetInterface(int slotId) {
        if (player.getInterfaceManager().containsScreenInter()) {
            player.getPackets().sendGameMessage("Please finish what you're doing before opening the rename preset interface.");
            return;
        }
        int dataId = getDataId(slotId);
        if (dataId == -1)
            return;
        if (dataId < 35654 || dataId > 35657)
            return;
        int index = dataId - 35654;
        CosmeticPreset preset = cosmeticPresets[index];
        player.getVarBitManager().sendVar(6803, 2 + index);
        player.getPackets().sendGlobalConfig(199, -1);
        player.getPackets().sendGlobalConfig(3678, -1);
        player.getPackets().sendExecuteScript(8178);
        player.getInterfaceManager().sendInterface(1830);
        player.getPackets().sendGlobalString(5944, preset.getName());
        player.getTemporaryAttributtes().put(Key.RENAME_PRESET_INDEX, index);
        player.setCloseInterfacesEvent(new Runnable() {
            @Override
            public void run() {
                player.getTemporaryAttributtes().remove(Key.RENAME_PRESET_INDEX);
            }
        });
    }

    private void refreshCosmeticPresets() {
        for (int i = 0; i < cosmeticPresets.length; i++)
            player.getPackets().sendGlobalString(5940 + i, cosmeticPresets[i].getName());
    }

    private transient Item[] purchasePreview;
    private transient List<Integer> buyingDataIds;

    public void openPurchaseMenu() {
        purchasePreview = cosmeticPreview.clone();
        buyingDataIds = new ArrayList<Integer>();
        player.getInterfaceManager().setMenuInterface(0, 1845);
        refreshItemsToBuy();
    }

    public boolean doesntUseBuyInterface(int dataId) {
        return dataId == 34014 || dataId == 31820;
    }

    public void purchase(int index) {
        PurchaseItem item = index >= purshaseItems.length ? null : purshaseItems[index];
        if (item == null)
            return;
        int status = item == null ? -1 : hasItem(item.getDataId(), purchasePreview) ? 1 : !hasItem(item.getDataId(), purchasePreview) ? 2 : buyingDataIds.contains(item.getDataId()) ? 3 : buyingDataIds.contains(item.getDataId()) && unlockedCosmetics.contains(item.getDataId()) ? 4 : 5;
        if (status != 1)
            return;
        buyingDataIds.add(item.getDataId());
        GeneralRequirementMap data = GeneralRequirementMap.getMap(item.getDataId());
        final int price = data.getIntValue(4717) != 0 || data.getIntValue(2532) == 20 ? CosmeticsHandler.DEFAULT_PRICE_FULL_OUTFIT : CosmeticsHandler.DEFAULT_PRICE_SINGLE_PIECE;
        if (doesntUseBuyInterface(item.getDataId())) {
            player.getInterfaceManager().closeScreenInterface();
            player.getDialogueManager().startDialogue(new Dialogue() {

                @Override
                public void start() {
                    sendDialogue("This outfit doesnt use the regular buy interface, check its contents on wiki before confirming.");
                }

                @Override
                public void run(int interfaceId, int componentId) {
                    if (interfaceId != 1186 && interfaceId != 1188) {
                        end();
                        player.getDialogueManager().startDialogue("SimpleMessage", "Something went wrong, please try again.");
                        return;
                    }
                    switch(stage) {
                        case -1:
                            stage = 0;
                            sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Buy for "+price+" ataraxia coins.", "Nevermind.");
                            break;
                        case 0:
                            if (componentId == OPTION_2) {
                                end();
                                return;
                            }
                            stage = 1;
                            sendDialogue(true, "Confirming order, please wait.");
                            player.setAtaraxiaCoins(player.getAtaraxiaCoins() - price);
                            unlockItem(data);
                            WorldTasksManager.schedule(new WorldTask() {

                                @Override
                                public void run() {
                                    stage = 2;
                                    sendDialogue("Thank you, Your order was successfull.");
                                }
                            }, 1);
                            break;
                        case 1:
                            break;
                        case 2:
                            end();
                            break;
                    }
                }

                @Override
                public void finish() {
                    player.getInterfaceManager().closeScreenInterface();
                    if (buyingDataIds.contains(item.getDataId()) && !unlockedCosmetics.contains(item.getDataId()))
                        buyingDataIds.removeIf(i -> i == item.getDataId());
                    refreshItemsToBuy();
                }

            });
            refreshItemsToBuy();
            return;
        }
        player.getDialogueManager().startDialogue(new Dialogue() {
            private int status;

            @Override
            public void start() {
                player.getVarBitManager().sendVarBit(26175, price);
                player.getVarBitManager().sendVarBit(26176, 1048575);
                player.getVarBitManager().sendVar(5006, data.getId());
                player.getPackets().sendGlobalConfig(4661, status);
                player.refreshAtaraxiaCoins();
                player.getInterfaceManager().sendInterface(1562);
            }

            @Override
            public void run(int interfaceId, int componentId) {
                if (interfaceId != 1562) {
                    end();
                    player.getDialogueManager().startDialogue("SimpleMessage", "Something went wrong, please try again.");
                    return;
                }
                if (componentId == 34)
                    end();
                switch (status) {
                    case 0:
                        if (componentId == 77)
                            player.getPackets().sendOpenURL(Settings.DONATE);
                        else if (componentId == 69)
                            player.getPackets().sendGameMessage("That option is not available at the moment.");
                        else if (componentId == 61) {
                            status = 1;
                            player.getPackets().sendGlobalConfig(4661, status);
                        }
                        break;
                    case 1:
                        if (componentId == 93) {
                            status = 0;
                            player.getPackets().sendGlobalConfig(4661, status);
                        } else if (componentId == 85) {
                            status = 2;
                            player.getPackets().sendGlobalConfig(4661, status);
                            player.setAtaraxiaCoins(player.getAtaraxiaCoins() - price);
                            unlockItem(data);
                            WorldTasksManager.schedule(new WorldTask() {

                                @Override
                                public void run() {
                                    status = 3;
                                    player.getPackets().sendGlobalConfig(4663, 2);
                                    player.getPackets().sendGlobalConfig(4662, 3);
                                    player.getPackets().sendGlobalConfig(4661, status);
                                }
                            });
                        }
                        break;
                    case 3:
                        end();
                        break;
                }
            }

            @Override
            public void finish() {
                player.getInterfaceManager().closeScreenInterface();
                if (buyingDataIds.contains(item.getDataId()) && !unlockedCosmetics.contains(item.getDataId()))
                    buyingDataIds.removeIf(i -> i == item.getDataId());
                refreshItemsToBuy();
            }
        });
        refreshItemsToBuy();
    }

    public void undoChange(int index) {
        PurchaseItem item = index >= purshaseItems.length ? null : purshaseItems[index];
        if (item == null)
            return;
        int status = item == null ? -1 : hasItem(item.getDataId(), purchasePreview) ? 1 : !hasItem(item.getDataId(), purchasePreview) ? 2 : buyingDataIds.contains(item.getDataId()) ? 3 : buyingDataIds.contains(item.getDataId()) && unlockedCosmetics.contains(item.getDataId()) ? 4 : 5;
        if (status != 1 && status != 2)
            return;
        if (status == 1) {
            removeItem(item.getDataId(), purchasePreview);
        } else {
            revertItem(item.getDataId(), purchasePreview, cosmeticPreview);
        }
        refreshItemsToBuy();
    }

    public void applyPurchase() {
        cosmeticPreview = purchasePreview.clone();
        purchasePreview = null;
        closePurchaseMenu();
        apply();
    }

    public void closePurchaseMenu() {
        purshaseItems = new PurchaseItem[0];
        open(CosmeticType.WARDROBE);
        refreshSelectedPreset();
        player.getPackets().sendHideIComponent(1843, 310, true);
        player.getPackets().sendGlobalConfig(2235, 33882328);
    }

    public void refreshItemsToBuy() {
        player.getPackets().sendConfig(6809, purshaseItems.length);
        for (int i = 0; i < 11; i++) {
            PurchaseItem item = i >= purshaseItems.length ? null : purshaseItems[i];
            int status = item == null ? -1 : buyingDataIds.contains(item.getDataId()) && unlockedCosmetics.contains(item.getDataId()) ? 4 : buyingDataIds.contains(item.getDataId()) ? 3 : unlockedCosmetics.contains(item.getDataId()) ? 5 : hasItem(item.getDataId(), purchasePreview) ? 1 : !hasItem(item.getDataId(), purchasePreview) ? 2 : -1;
            player.getPackets().sendConfig(6810 + i, item == null ? -1 : item.getDataId());
            player.getPackets().sendConfig(6821 + i, item == null ? -1 : item.getSlotId());
            player.getPackets().sendConfig(6832 + i, item == null ? -1 : status);
        }
    }

    public boolean hasItem(int dataId, Item[] items) {
        GeneralRequirementMap data = GeneralRequirementMap.getMap(dataId);
        int actualDataId = data.getIntValue(4419) != 0 ? data.getIntValue(4419) : dataId;
        for (Item item : items) {
            if (item == null)
                continue;
            GeneralRequirementMap map = GeneralRequirementMap.getMap(item.getDefinitions().getCSOpcode(4414));
            if (map.getIntValue(4419) == actualDataId || map.getId() == actualDataId)
                return true;

        }
        return false;
    }

    public void removeItem(int dataId, Item[] items) {
        GeneralRequirementMap data = GeneralRequirementMap.getMap(dataId);
        int actualDataId = data.getIntValue(4419) != 0 ? data.getIntValue(4419) : dataId;
        for (int i = 0; i < items.length; i++) {
            if (items[i] == null)
                continue;
            GeneralRequirementMap map = GeneralRequirementMap.getMap(items[i].getDefinitions().getCSOpcode(4414));
            if (map.getIntValue(4419) == actualDataId || map.getId() == actualDataId)
                items[i] = null;
        }
    }

    public void revertItem(int dataId, Item[] items, Item[] original) {
        GeneralRequirementMap data = GeneralRequirementMap.getMap(dataId);
        int actualDataId = data.getIntValue(4419) != 0 ? data.getIntValue(4419) : dataId;
        for (int i = 0; i < items.length; i++) {
            if (original[i] == null)
                continue;
            GeneralRequirementMap map = GeneralRequirementMap.getMap(original[i].getDefinitions().getCSOpcode(4414));
            if (map.getIntValue(4419) == actualDataId || map.getId() == actualDataId)
                items[i] = original[i].clone();
        }
    }

    public int getRenderEmote() {
        int animationData = getActiveAnimation(CosmeticsAnimation.WALK_ANIMATIONS);
        if (animationData == 0 || animationData == -1)
            return 2699;
        GeneralRequirementMap map = GeneralRequirementMap.getMap(animationData);
        int renderEmote = map == null ? 0 : map.getIntValue(2954);
        return renderEmote == 0 ? 2699 : renderEmote;
    }

    public int getActiveAnimation(CosmeticsAnimation animation) {
        checkAnimationSlots();
        return activeAnimations[animation.ordinal()];
    }

    public static int getAnimationIndex(int dataId) {
        GeneralRequirementMap map = GeneralRequirementMap.getMap(dataId);
        if (map == null || map.getValues() == null)
            return -1;
        String name = map.getStringValue(2533);
        int subMenu = map.getIntValue(2532);
        for (CosmeticsAnimation animation : CosmeticsAnimation.values()) {
            if (animation.getSubMenuId() != subMenu)
                continue;
            if (animation.keywords == null || animation.keywords.length == 0)
                return animation.ordinal();
            for (String keyword : animation.keywords) {
                if (name.toLowerCase().contains(keyword.toLowerCase()))
                    return animation.ordinal();
            }
        }
        return -1;
    }


    public static class PurchaseItem {
        @Getter
        @Setter
        private int dataId;

        public PurchaseItem(int dataId) {
            this.dataId = dataId;
        }

        public int getSlotId() {
            return GeneralRequirementMap.getMap(dataId).getIntValue(2532) == 20 ? 15 : GeneralRequirementMap.getMap(dataId).getIntValue(2532);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + dataId;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            PurchaseItem other = (PurchaseItem) obj;
            if (dataId != other.dataId)
                return false;
            return true;
        }

    }

    public static final class CosmeticPreset implements Serializable {

        private static final long serialVersionUID = 2479949279427475293L;
        private String name;
        private Item[] presetItems;

        public CosmeticPreset() {
            this.name = "";
            this.presetItems = new Item[BodyDefinitions.getEquipmentContainerSize()];
        }

        public Item[] getPresetItems() {
            return presetItems;
        }

        public void setPresetItems(Item[] presetItems) {
            this.presetItems = presetItems;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean hasItems() {
            if (presetItems == null)
                return false;
            for (Item item : presetItems)
                if (item != null)
                    return true;
            return false;
        }

    }

    // enum 10156 bank boosters (uses script 6488 for unlock varbit)
    // script 6489 active varbit
    // script 6438 if is wearing the outfit then its active
    // script 6882 is active appearance
    // script 6883 is active appearance
    // script_9966 keepsake
    // script_7609 is active title

    // legendary pets script_11005
    // for walk animations
    // domain_0['cfg248'].bitcfg26893 == _struct_param(a0, 5249)
    // componentId 454 false show apply/revert
    // componentId 461 false disable apply button
    // componentId 470 false disable revert button
    public static Map<Integer, Object> cosmeticVars;

    static {
        if (cosmeticVars == null) {
            cosmeticVars = new HashMap<Integer, Object>();
            setVars();
        }
    }

    private static final int[] FREE_COSMETICS = { 26616, 26619, 26620, 26621, 26622, 26623, 26624, 26627, 26628, 26629, 26630, 26631, 26632, 26635, 26636, 26637, 26638, 7270, 7271, 7272, 7273, 16590, 19272, 26509, 26511, 26521, 26582, 26583, 26594, 26510, 26595, 26596, 26597, 26598, 26599, 26639, 26640

            , 32553, 32554, 24407, 19139, 19140, 21095, 21096, 21097, 21103, 21102, 5692 };

    public void refreshVars() {
        player.getVarBitManager().sendVar(3079, 80000);
        for (Entry<Integer, Object> e : cosmeticVars.entrySet()) {
            if (e == null || e.getValue() == null)
                continue;
            Object value = e.getValue();
            boolean unlocked = unlockedCosmetics.contains(e.getKey());
            if (value instanceof Integer)
                player.getVarBitManager().sendVarBit((Integer) value, unlocked ? 1 : 0);
            else
                for (int j = 0; j < ((Integer[]) value).length; j++) {
                    if (((Integer[]) value)[j] == null)
                        continue;
                    player.getVarBitManager().sendVarBit(((Integer[]) value)[j], unlocked ? 1 : 0);
                }
        }
        int v1 = 0;
        v1 |= ((unlockedCosmetics.contains(22571) ? 1 : 0) << 0);
        v1 |= ((unlockedCosmetics.contains(23008) ? 1 : 0) << 1);
        v1 |= ((unlockedCosmetics.contains(23013) ? 1 : 0) << 2);
        v1 |= ((unlockedCosmetics.contains(23018) ? 1 : 0) << 3);
        v1 |= ((unlockedCosmetics.contains(23023) ? 1 : 0) << 4);
        v1 |= ((unlockedCosmetics.contains(23028) ? 1 : 0) << 5);
        v1 |= ((unlockedCosmetics.contains(23033) ? 1 : 0) << 6);
        v1 |= ((unlockedCosmetics.contains(23038) ? 1 : 0) << 7);
        v1 |= ((unlockedCosmetics.contains(23043) ? 1 : 0) << 8);
        v1 |= ((unlockedCosmetics.contains(23048) ? 1 : 0) << 9);
        v1 |= ((unlockedCosmetics.contains(23053) ? 1 : 0) << 10);
        v1 |= ((unlockedCosmetics.contains(23058) ? 1 : 0) << 11);
        v1 |= ((unlockedCosmetics.contains(23063) ? 1 : 0) << 12);
        v1 |= ((unlockedCosmetics.contains(23068) ? 1 : 0) << 13);
        player.getVarBitManager().sendVar(1246, v1);
        int v2 = 0;
        v2 |= ((unlockedCosmetics.contains(23074) ? 1 : 0) << 0);
        v2 |= ((unlockedCosmetics.contains(23075) ? 1 : 0) << 1);
        v2 |= ((unlockedCosmetics.contains(23076) ? 1 : 0) << 2);
        v2 |= ((unlockedCosmetics.contains(23077) ? 1 : 0) << 3);
        v2 |= ((unlockedCosmetics.contains(23081) ? 1 : 0) << 4);
        v2 |= ((unlockedCosmetics.contains(23080) ? 1 : 0) << 5);
        v2 |= ((unlockedCosmetics.contains(23079) ? 1 : 0) << 6);
        v2 |= ((unlockedCosmetics.contains(23078) ? 1 : 0) << 7);
        player.getVarBitManager().sendVar(1244, v2);
        player.getVarBitManager().sendVarBit(5490, 5);
        player.getVarBitManager().sendVarBit(5491, 5);
        player.getVarBitManager().sendVarBit(5492, 4);
        player.getVarBitManager().sendVarBit(5493, 12);
        player.getVarBitManager().sendVarBit(5494, 2);
        player.getVarBitManager().sendVarBit(4291, unlockedCosmetics.contains(28266) ? 200 : 0);
        player.getVarBitManager().sendVarBit(25811, 5);
        player.getVarBitManager().sendVarBit(25810, 5);
        player.getVarBitManager().sendVarBit(27178, 1);
        player.getVarBitManager().sendVarBit(29424, unlockedCosmetics.contains(32374) ? 250 : 0);
        player.getVarBitManager().sendVarBit(29692, 10);
        player.getVarBitManager().sendVarBit(29682, 1);
        player.getVarBitManager().sendVarBit(26362, unlockedCosmetics.contains(29760) ? 1 : 0);
    }

    private boolean hasCosmeticVar(int id, boolean loyality) {
        if (id == 35592)
            return false;
        if (id == 22571 || id >= 23004 && id <= 23073)
            return true;
        if (id >= 23074 && id <= 23081)
            return true;
        if (id == 28266 || id == 28360)
            return true;
        if (id >= 30773 && id <= 30782)
            return true;
        if (id == 32374)
            return true;
        if (id == 29760)
            return true;
        if (cosmeticVars.containsKey(id))
            return true;
        GeneralRequirementMap data = GeneralRequirementMap.getMap(id);
        if (data.getIntValue(2532) == 20) {
            ClientScriptMap map = ClientScriptMap.getMap(data.getIntValue(2542));
            boolean hasAllVars = true;
            for (Long slot : map.getValues().keySet()) {
                int itemId = map.getIntValue(slot);
                ItemDefinitions defs = ItemDefinitions.getItemDefinitions(itemId);

                if (defs.getCSOpcode(4414) <= 0 || !cosmeticVars.containsKey(defs.getCSOpcode(4414))) {
                    hasAllVars = false;
                    break;
                }
            }
            return hasAllVars;
        }
        return false;
    }

    public static void setVars() {
        cosmeticVars.put(11113, 860);
        cosmeticVars.put(11114, 861);
        cosmeticVars.put(11115, 862);
        cosmeticVars.put(11116, 863);
        cosmeticVars.put(11117, 864);
        cosmeticVars.put(11118, 865);
        cosmeticVars.put(11119, 866);
        cosmeticVars.put(11120, 867);
        cosmeticVars.put(11121, 868);
        cosmeticVars.put(11122, 869);
        cosmeticVars.put(11123, 870);
        cosmeticVars.put(11124, 871);
        cosmeticVars.put(11125, 872);
        cosmeticVars.put(11126, 873);
        cosmeticVars.put(11127, 874);
        cosmeticVars.put(11128, 875);
        cosmeticVars.put(11129, 876);
        cosmeticVars.put(11130, 877);
        cosmeticVars.put(11131, 878);
        cosmeticVars.put(11132, 879);
        cosmeticVars.put(11133, 880);
        cosmeticVars.put(11134, 881);
        cosmeticVars.put(11135, 882);
        cosmeticVars.put(11136, 883);
        cosmeticVars.put(11137, 884);
        cosmeticVars.put(11138, 885);
        cosmeticVars.put(11139, 886);
        cosmeticVars.put(11140, 887);
        cosmeticVars.put(31880, 888);
        cosmeticVars.put(11141, 888);
        cosmeticVars.put(11142, 889);
        cosmeticVars.put(11143, 890);
        cosmeticVars.put(11144, 891);
        cosmeticVars.put(11145, 892);
        cosmeticVars.put(11146, 893);
        cosmeticVars.put(11147, 894);
        cosmeticVars.put(11148, 895);
        cosmeticVars.put(9918, 895);
        cosmeticVars.put(29717, 896);
        cosmeticVars.put(11149, 896);
        cosmeticVars.put(11150, 897);
        cosmeticVars.put(11151, 898);
        cosmeticVars.put(11152, 899);
        cosmeticVars.put(11153, 900);
        cosmeticVars.put(9919, 899);
        cosmeticVars.put(9920, 900);
        cosmeticVars.put(11154, 901);
        cosmeticVars.put(11167, 915);
        cosmeticVars.put(11168, 917);
        cosmeticVars.put(11169, 919);
        cosmeticVars.put(11170, 921);
        cosmeticVars.put(11171, 923);
        cosmeticVars.put(11172, 925);
        cosmeticVars.put(11173, 927);
        cosmeticVars.put(11174, 929);
        cosmeticVars.put(11175, 931);
        cosmeticVars.put(31856, 943);
        cosmeticVars.put(31857, 944);
        cosmeticVars.put(31858, 945);
        cosmeticVars.put(31859, 946);
        cosmeticVars.put(31860, 947);
        cosmeticVars.put(31861, 17882);
        cosmeticVars.put(11398, 780);
        cosmeticVars.put(11399, 781);
        cosmeticVars.put(11400, 782);
        cosmeticVars.put(11401, 783);
        cosmeticVars.put(11402, 784);
        cosmeticVars.put(11403, 786);
        cosmeticVars.put(11404, 785);
        cosmeticVars.put(11407, 789);
        cosmeticVars.put(11390, 765);
        cosmeticVars.put(11391, 766);
        cosmeticVars.put(11392, 767);
        cosmeticVars.put(11393, 768);
        cosmeticVars.put(11394, 769);
        cosmeticVars.put(11395, 770);
        cosmeticVars.put(11396, 771);
        cosmeticVars.put(11397, 772);
        cosmeticVars.put(11419, 722);
        cosmeticVars.put(11420, 723);
        cosmeticVars.put(11421, 724);
        cosmeticVars.put(11422, 725);
        cosmeticVars.put(11423, 726);
        cosmeticVars.put(11424, 727);
        cosmeticVars.put(11425, 728);
        cosmeticVars.put(9922, 728);
        cosmeticVars.put(11426, 729);
        cosmeticVars.put(9923, 729);
        cosmeticVars.put(11427, 730);
        cosmeticVars.put(11428, 731);
        cosmeticVars.put(9924, 731);
        cosmeticVars.put(11456, 948);
        cosmeticVars.put(11457, 950);
        cosmeticVars.put(11458, 952);
        cosmeticVars.put(11459, 954);
        cosmeticVars.put(11460, 956);
        cosmeticVars.put(11461, 958);
        cosmeticVars.put(11462, 960);
        cosmeticVars.put(7231, 962);
        cosmeticVars.put(7233, 963);
        cosmeticVars.put(7234, 964);
        cosmeticVars.put(7237, 966);
        cosmeticVars.put(7238, 967);
        cosmeticVars.put(7235, 965);
        cosmeticVars.put(7239, 968);
        cosmeticVars.put(7240, 969);
        cosmeticVars.put(7241, 970);
        cosmeticVars.put(7245, 972);
        cosmeticVars.put(7246, 973);
        cosmeticVars.put(7244, 971);
        cosmeticVars.put(7247, 974);
        cosmeticVars.put(7249, 975);
        cosmeticVars.put(7250, 976);
        cosmeticVars.put(12326, 977);
        cosmeticVars.put(12328, 979);
        cosmeticVars.put(12329, 980);
        cosmeticVars.put(12327, 978);
        cosmeticVars.put(29732, 981);
        cosmeticVars.put(12330, 981);
        cosmeticVars.put(12331, 982);
        cosmeticVars.put(9921, 982);
        cosmeticVars.put(31877, 792);
        cosmeticVars.put(13674, 792);
        cosmeticVars.put(13675, 793);
        cosmeticVars.put(13676, 794);
        cosmeticVars.put(13677, 795);
        cosmeticVars.put(13678, 796);
        cosmeticVars.put(13679, 797);
        cosmeticVars.put(31878, 798);
        cosmeticVars.put(13680, 798);
        cosmeticVars.put(13681, 799);
        cosmeticVars.put(13682, 800);
        cosmeticVars.put(13683, 801);
        cosmeticVars.put(13684, 802);
        cosmeticVars.put(13685, 803);
        cosmeticVars.put(31879, 804);
        cosmeticVars.put(13686, 804);
        cosmeticVars.put(13687, 805);
        cosmeticVars.put(13688, 806);
        cosmeticVars.put(13689, 807);
        cosmeticVars.put(13690, 808);
        cosmeticVars.put(13691, 809);
        cosmeticVars.put(13692, 810);
        cosmeticVars.put(13693, 811);
        cosmeticVars.put(13694, 812);
        cosmeticVars.put(13836, 738);
        cosmeticVars.put(13837, 739);
        cosmeticVars.put(13838, 740);
        cosmeticVars.put(13839, 741);
        cosmeticVars.put(13840, 742);
        cosmeticVars.put(13841, 743);
        cosmeticVars.put(13842, 744);
        cosmeticVars.put(13843, 745);
        cosmeticVars.put(13844, 746);
        cosmeticVars.put(13845, 747);
        cosmeticVars.put(13846, 748);
        cosmeticVars.put(13847, 749);
        cosmeticVars.put(13848, 750);
        cosmeticVars.put(13849, 751);
        cosmeticVars.put(13850, 752);
        cosmeticVars.put(13851, 753);
        cosmeticVars.put(13852, 754);
        cosmeticVars.put(13853, 755);
        cosmeticVars.put(13854, 756);
        cosmeticVars.put(13855, 757);
        cosmeticVars.put(13856, 758);
        cosmeticVars.put(13857, 759);
        cosmeticVars.put(13858, 760);
        cosmeticVars.put(13859, 761);
        cosmeticVars.put(13860, 762);
        cosmeticVars.put(13861, 763);
        cosmeticVars.put(29718, 815);
        cosmeticVars.put(13863, 815);
        cosmeticVars.put(13864, 816);
        cosmeticVars.put(13865, 817);
        cosmeticVars.put(13866, 818);
        cosmeticVars.put(13867, 819);
        cosmeticVars.put(13868, 820);
        cosmeticVars.put(13869, 821);
        cosmeticVars.put(9904, 821);
        cosmeticVars.put(13870, 822);
        cosmeticVars.put(9905, 822);
        cosmeticVars.put(13871, 823);
        cosmeticVars.put(13872, 824);
        cosmeticVars.put(14490, 692);
        cosmeticVars.put(14491, 693);
        cosmeticVars.put(14492, 694);
        cosmeticVars.put(14493, 695);
        cosmeticVars.put(14494, 696);
        cosmeticVars.put(14495, 697);
        cosmeticVars.put(14496, 698);
        cosmeticVars.put(14497, 699);
        cosmeticVars.put(14498, 700);
        cosmeticVars.put(14499, 701);
        cosmeticVars.put(14500, 702);
        cosmeticVars.put(14501, 703);
        cosmeticVars.put(14502, 704);
        cosmeticVars.put(9917, 705);
        cosmeticVars.put(14503, 705);
        cosmeticVars.put(35590, 705);
        cosmeticVars.put(14505, 706);
        cosmeticVars.put(14504, 707);
        cosmeticVars.put(14507, 708);
        cosmeticVars.put(14506, 709);
        cosmeticVars.put(31892, 222);
        cosmeticVars.put(14536, 222);
        cosmeticVars.put(14537, 222);
        cosmeticVars.put(14530, 710);
        cosmeticVars.put(31881, 712);
        cosmeticVars.put(14531, 712);
        cosmeticVars.put(14532, 714);
        cosmeticVars.put(14533, 716);
        cosmeticVars.put(14534, 718);
        cosmeticVars.put(14535, 720);
        cosmeticVars.put(9906, 774);
        cosmeticVars.put(9907, 774);
        cosmeticVars.put(9908, 775);
        cosmeticVars.put(9909, 776);
        cosmeticVars.put(9910, 776);
        cosmeticVars.put(9911, 777);
        cosmeticVars.put(9912, 777);
        cosmeticVars.put(9913, 778);
        cosmeticVars.put(9914, 778);
        cosmeticVars.put(9915, 779);
        cosmeticVars.put(9916, 779);
        cosmeticVars.put(29719, 16812);
        cosmeticVars.put(975, 16812);
        cosmeticVars.put(3109, 16813);
        cosmeticVars.put(1534, 16813);
        cosmeticVars.put(3110, 16814);
        cosmeticVars.put(3119, 16815);
        cosmeticVars.put(3120, 16816);
        cosmeticVars.put(3121, 16817);
        cosmeticVars.put(3122, 16818);
        cosmeticVars.put(3123, 16819);
        cosmeticVars.put(3124, 16820);
        cosmeticVars.put(29720, 16822);
        cosmeticVars.put(3128, 16822);
        cosmeticVars.put(3126, 16823);
        cosmeticVars.put(3125, 16823);
        cosmeticVars.put(3131, 16824);
        cosmeticVars.put(3132, 16825);
        cosmeticVars.put(3134, 16826);
        cosmeticVars.put(3138, 16827);
        cosmeticVars.put(5524, 16828);
        cosmeticVars.put(5544, 16829);
        cosmeticVars.put(5552, 16830);
        cosmeticVars.put(16548, 16938);
        cosmeticVars.put(16549, 16939);
        cosmeticVars.put(16550, 16940);
        cosmeticVars.put(16551, 16941);
        cosmeticVars.put(16552, 16942);
        cosmeticVars.put(16553, 16943);
        cosmeticVars.put(16554, 16944);
        cosmeticVars.put(16555, 16945);
        cosmeticVars.put(16556, 16946);
        cosmeticVars.put(16557, 16947);
        cosmeticVars.put(16558, 16948);
        cosmeticVars.put(16559, 16949);
        cosmeticVars.put(16560, 16950);
        cosmeticVars.put(16561, 16951);
        cosmeticVars.put(16562, 16952);
        cosmeticVars.put(16563, 16953);
        cosmeticVars.put(16564, 16954);
        cosmeticVars.put(16565, 16955);
        cosmeticVars.put(17330, 16890);
        cosmeticVars.put(17331, 16891);
        cosmeticVars.put(17332, 16892);
        cosmeticVars.put(17333, 16893);
        cosmeticVars.put(17334, 16894);
        cosmeticVars.put(18792, 16895);
        cosmeticVars.put(18793, 16896);
        cosmeticVars.put(18794, 16897);
        cosmeticVars.put(18795, 16898);
        cosmeticVars.put(18796, 16899);
        cosmeticVars.put(17294, 17507);
        cosmeticVars.put(17295, 17508);
        cosmeticVars.put(17296, 17509);
        cosmeticVars.put(17297, 17510);
        cosmeticVars.put(17298, 17511);
        cosmeticVars.put(17299, 17512);
        cosmeticVars.put(17306, 17519);
        cosmeticVars.put(17307, 17520);
        cosmeticVars.put(17308, 17521);
        cosmeticVars.put(17309, 17522);
        cosmeticVars.put(17310, 17523);
        cosmeticVars.put(17311, 17524);
        cosmeticVars.put(17300, 17513);
        cosmeticVars.put(17301, 17514);
        cosmeticVars.put(17302, 17515);
        cosmeticVars.put(17303, 17516);
        cosmeticVars.put(17304, 17517);
        cosmeticVars.put(17305, 17518);
        cosmeticVars.put(17312, 17525);
        cosmeticVars.put(17313, 17526);
        cosmeticVars.put(17314, 17527);
        cosmeticVars.put(17315, 17528);
        cosmeticVars.put(17316, 17529);
        cosmeticVars.put(17317, 17530);
        cosmeticVars.put(17318, 17531);
        cosmeticVars.put(17319, 17532);
        cosmeticVars.put(17320, 17533);
        cosmeticVars.put(17321, 17534);
        cosmeticVars.put(17322, 17535);
        cosmeticVars.put(17323, 17536);
        cosmeticVars.put(19019, 17628);
        cosmeticVars.put(19022, 17629);
        cosmeticVars.put(19025, 17630);
        cosmeticVars.put(17324, 17537);
        cosmeticVars.put(17325, 17537);
        cosmeticVars.put(17326, 17537);
        cosmeticVars.put(17327, 17537);
        cosmeticVars.put(17328, 17537);
        cosmeticVars.put(17329, 17537);
        cosmeticVars.put(383, 17617);
        cosmeticVars.put(19042, 17617);
        cosmeticVars.put(19043, 17617);
        cosmeticVars.put(19044, 17617);
        cosmeticVars.put(19046, 17617);
        cosmeticVars.put(19047, 17617);
        cosmeticVars.put(19045, 17617);
        cosmeticVars.put(19048, 17742);
        cosmeticVars.put(19049, 17742);
        cosmeticVars.put(19050, 17742);
        cosmeticVars.put(19051, 17742);
        cosmeticVars.put(19053, 17742);
        cosmeticVars.put(19054, 17742);
        cosmeticVars.put(19052, 17742);
        cosmeticVars.put(19121, 17805);
        cosmeticVars.put(19122, 17806);
        cosmeticVars.put(19123, 17807);
        cosmeticVars.put(19124, 17808);
        cosmeticVars.put(13697, 17873);
        cosmeticVars.put(14961, 17873);
        cosmeticVars.put(14962, 17873);
        cosmeticVars.put(14963, 17873);
        cosmeticVars.put(14964, 17873);
        cosmeticVars.put(19147, 17873);
        cosmeticVars.put(19148, 17871);
        cosmeticVars.put(19149, 17871);
        cosmeticVars.put(19150, 17871);
        cosmeticVars.put(19151, 17871);
        cosmeticVars.put(19152, 17871);
        cosmeticVars.put(19153, 17871);
        cosmeticVars.put(19154, 17871);
        cosmeticVars.put(19155, 17870);
        cosmeticVars.put(19156, 17870);
        cosmeticVars.put(19157, 17870);
        cosmeticVars.put(19158, 17870);
        cosmeticVars.put(19159, 17870);
        cosmeticVars.put(19160, 17870);
        cosmeticVars.put(19167, 17872);
        cosmeticVars.put(19168, 17872);
        cosmeticVars.put(19169, 17872);
        cosmeticVars.put(19170, 17872);
        cosmeticVars.put(19171, 17872);
        cosmeticVars.put(19172, 17872);
        cosmeticVars.put(19173, 17872);
        cosmeticVars.put(19161, 17874);
        cosmeticVars.put(19162, 17874);
        cosmeticVars.put(19163, 17874);
        cosmeticVars.put(19164, 17874);
        cosmeticVars.put(19165, 17874);
        cosmeticVars.put(19166, 17874);
        cosmeticVars.put(19125, 17816);
        cosmeticVars.put(19126, 17817);
        cosmeticVars.put(19127, 17818);
        cosmeticVars.put(19128, 17819);
        cosmeticVars.put(19129, 17820);
        cosmeticVars.put(19130, 17821);
        cosmeticVars.put(19131, 17822);
        cosmeticVars.put(19132, 17823);
        cosmeticVars.put(19133, 17824);
        cosmeticVars.put(19134, 17825);
        cosmeticVars.put(19135, 17826);
        cosmeticVars.put(19136, 17827);
        cosmeticVars.put(19137, 17828);
        cosmeticVars.put(19138, 17829);
        cosmeticVars.put(9891, 17876);
        cosmeticVars.put(14509, 17876);
        cosmeticVars.put(14510, 17876);
        cosmeticVars.put(14511, 17876);
        cosmeticVars.put(19059, 17876);
        cosmeticVars.put(19060, 17876);
        cosmeticVars.put(19061, 17880);
        cosmeticVars.put(19196, 18036);
        cosmeticVars.put(19213, 18036);
        cosmeticVars.put(19214, 18036);
        cosmeticVars.put(19215, 18036);
        cosmeticVars.put(19216, 18036);
        cosmeticVars.put(19217, 18036);
        cosmeticVars.put(19218, 18037);
        cosmeticVars.put(19219, 18037);
        cosmeticVars.put(19220, 18037);
        cosmeticVars.put(19221, 18037);
        cosmeticVars.put(19222, 18037);
        cosmeticVars.put(19223, 18037);
        cosmeticVars.put(19224, 18038);
        cosmeticVars.put(19225, 18038);
        cosmeticVars.put(19226, 18038);
        cosmeticVars.put(19227, 18038);
        cosmeticVars.put(19228, 18038);
        cosmeticVars.put(19229, 18038);
        cosmeticVars.put(19230, 18039);
        cosmeticVars.put(19231, 18039);
        cosmeticVars.put(19232, 18039);
        cosmeticVars.put(19233, 18039);
        cosmeticVars.put(19234, 18039);
        cosmeticVars.put(19235, 18039);
        cosmeticVars.put(19236, 18040);
        cosmeticVars.put(19237, 18041);
        cosmeticVars.put(19238, 18042);
        cosmeticVars.put(19239, 18043);
        cosmeticVars.put(6334, 18198);
        cosmeticVars.put(19195, 18168);
        cosmeticVars.put(19197, 18168);
        cosmeticVars.put(19209, 18168);
        cosmeticVars.put(19210, 18168);
        cosmeticVars.put(19306, 18168);
        cosmeticVars.put(19307, 18168);
        cosmeticVars.put(19208, 18168);
        cosmeticVars.put(31882, 18169);
        cosmeticVars.put(19308, 18169);
        cosmeticVars.put(19309, 18169);
        cosmeticVars.put(19310, 18169);
        cosmeticVars.put(19312, 18169);
        cosmeticVars.put(19311, 18169);
        cosmeticVars.put(19313, 18169);
        cosmeticVars.put(19314, 18169);
        cosmeticVars.put(19315, 18170);
        cosmeticVars.put(19316, 18170);
        cosmeticVars.put(19317, 18170);
        cosmeticVars.put(19319, 18170);
        cosmeticVars.put(19318, 18170);
        cosmeticVars.put(19320, 18170);
        cosmeticVars.put(31883, 18171);
        cosmeticVars.put(19321, 18171);
        cosmeticVars.put(19322, 18171);
        cosmeticVars.put(19323, 18171);
        cosmeticVars.put(19325, 18171);
        cosmeticVars.put(19324, 18171);
        cosmeticVars.put(19326, 18171);
        cosmeticVars.put(19327, 18171);
        cosmeticVars.put(19328, 18172);
        cosmeticVars.put(19329, 18172);
        cosmeticVars.put(19330, 18172);
        cosmeticVars.put(19332, 18172);
        cosmeticVars.put(19331, 18172);
        cosmeticVars.put(19333, 18172);
        cosmeticVars.put(19417, 18227);
        cosmeticVars.put(19418, 18228);
        cosmeticVars.put(29721, 18229);
        cosmeticVars.put(19419, 18229);
        cosmeticVars.put(19421, 18229);
        cosmeticVars.put(19420, 18229);
        cosmeticVars.put(19422, 18229);
        cosmeticVars.put(19423, 18230);
        cosmeticVars.put(19534, 18285);
        cosmeticVars.put(19535, 18285);
        cosmeticVars.put(19536, 18285);
        cosmeticVars.put(19538, 18285);
        cosmeticVars.put(19537, 18285);
        cosmeticVars.put(19539, 18285);
        cosmeticVars.put(19540, 18286);
        cosmeticVars.put(19561, 18322);
        cosmeticVars.put(19562, 18322);
        cosmeticVars.put(19563, 18322);
        cosmeticVars.put(19565, 18322);
        cosmeticVars.put(19564, 18322);
        cosmeticVars.put(19566, 18322);
        cosmeticVars.put(19567, 18323);
        cosmeticVars.put(19568, 18323);
        cosmeticVars.put(19569, 18323);
        cosmeticVars.put(19571, 18323);
        cosmeticVars.put(19570, 18323);
        cosmeticVars.put(19572, 18323);
        cosmeticVars.put(19573, 18324);
        cosmeticVars.put(19574, 18324);
        cosmeticVars.put(19575, 18324);
        cosmeticVars.put(19577, 18324);
        cosmeticVars.put(19576, 18324);
        cosmeticVars.put(19578, 18324);
        cosmeticVars.put(19579, 18325);
        cosmeticVars.put(19580, 18325);
        cosmeticVars.put(19581, 18325);
        cosmeticVars.put(19583, 18325);
        cosmeticVars.put(19582, 18325);
        cosmeticVars.put(19584, 18325);
        cosmeticVars.put(19605, 18389);
        cosmeticVars.put(19606, 18390);
        cosmeticVars.put(19607, 18391);
        cosmeticVars.put(19608, 18392);
        cosmeticVars.put(31885, 18373);
        cosmeticVars.put(19614, 18478);
        cosmeticVars.put(19644, 18478);
        cosmeticVars.put(19645, 18478);
        cosmeticVars.put(19646, 18478);
        cosmeticVars.put(19647, 18478);
        cosmeticVars.put(19648, 18478);
        cosmeticVars.put(19649, 18478);
        cosmeticVars.put(19650, 18478);
        cosmeticVars.put(18808, 18483);
        cosmeticVars.put(19411, 18484);
        cosmeticVars.put(19412, 18499);
        cosmeticVars.put(19413, 18500);
        cosmeticVars.put(19674, 18501);
        cosmeticVars.put(19675, 18502);
        cosmeticVars.put(19676, 18503);
        cosmeticVars.put(19677, 18504);
        cosmeticVars.put(19414, 18531);
        cosmeticVars.put(19424, 18532);
        cosmeticVars.put(21064, 18756);
        cosmeticVars.put(21065, 18757);
        cosmeticVars.put(20281, 18605);
        cosmeticVars.put(29722, 20052);
        cosmeticVars.put(22576, 20052);
        cosmeticVars.put(22577, 20074);
        cosmeticVars.put(22578, 20074);
        cosmeticVars.put(22579, 20053);
        cosmeticVars.put(22580, 20054);
        cosmeticVars.put(22581, 20055);
        cosmeticVars.put(22582, 20056);
        cosmeticVars.put(22583, 20057);
        cosmeticVars.put(22584, 20057);
        cosmeticVars.put(22585, 20058);
        cosmeticVars.put(29723, 20061);
        cosmeticVars.put(22586, 20061);
        cosmeticVars.put(22587, 20073);
        cosmeticVars.put(22588, 20073);
        cosmeticVars.put(22589, 20062);
        cosmeticVars.put(22590, 20063);
        cosmeticVars.put(22591, 20064);
        cosmeticVars.put(22592, 20065);
        cosmeticVars.put(22593, 20066);
        cosmeticVars.put(22594, 20066);
        cosmeticVars.put(22595, 20067);
        cosmeticVars.put(22596, 20067);
        cosmeticVars.put(22597, 20068);
        cosmeticVars.put(22598, 20069);
        cosmeticVars.put(22599, 20070);
        cosmeticVars.put(20301, 1891);
        cosmeticVars.put(21375, 1891);
        cosmeticVars.put(21376, 1891);
        cosmeticVars.put(21377, 1891);
        cosmeticVars.put(21378, 1891);
        cosmeticVars.put(21379, 1891);
        cosmeticVars.put(21380, 1891);
        cosmeticVars.put(21381, 5993);
        cosmeticVars.put(21382, 5993);
        cosmeticVars.put(21383, 5993);
        cosmeticVars.put(21384, 5993);
        cosmeticVars.put(21385, 5993);
        cosmeticVars.put(21386, 5993);
        cosmeticVars.put(21387, 5993);
        cosmeticVars.put(21388, 19941);
        cosmeticVars.put(21389, 19941);
        cosmeticVars.put(21390, 19941);
        cosmeticVars.put(21391, 19941);
        cosmeticVars.put(21392, 19941);
        cosmeticVars.put(21393, 19941);
        cosmeticVars.put(21394, 19942);
        cosmeticVars.put(21395, 19942);
        cosmeticVars.put(21396, 19942);
        cosmeticVars.put(21397, 19942);
        cosmeticVars.put(21398, 19942);
        cosmeticVars.put(21399, 19942);
        cosmeticVars.put(21400, 19943);
        cosmeticVars.put(21401, 19943);
        cosmeticVars.put(21402, 19943);
        cosmeticVars.put(21403, 19943);
        cosmeticVars.put(21404, 19943);
        cosmeticVars.put(21405, 19943);
        cosmeticVars.put(21406, 19943);
        cosmeticVars.put(21407, 19944);
        cosmeticVars.put(21408, 19944);
        cosmeticVars.put(21409, 19944);
        cosmeticVars.put(21410, 19944);
        cosmeticVars.put(21411, 19944);
        cosmeticVars.put(21412, 19944);
        cosmeticVars.put(21413, 19944);
        cosmeticVars.put(21414, 19945);
        cosmeticVars.put(21415, 19945);
        cosmeticVars.put(21416, 19945);
        cosmeticVars.put(21417, 19945);
        cosmeticVars.put(21418, 19945);
        cosmeticVars.put(21419, 19945);
        cosmeticVars.put(21420, 19946);
        cosmeticVars.put(21421, 19946);
        cosmeticVars.put(21422, 19946);
        cosmeticVars.put(21423, 19946);
        cosmeticVars.put(21424, 19946);
        cosmeticVars.put(21425, 19946);
        cosmeticVars.put(20338, 18671);
        cosmeticVars.put(20340, 18672);
        cosmeticVars.put(20342, 18673);
        cosmeticVars.put(20339, 18668);
        cosmeticVars.put(20341, 18669);
        cosmeticVars.put(20343, 18670);
        cosmeticVars.put(21088, 18778);
        cosmeticVars.put(21091, 18779);
        cosmeticVars.put(23111, 20370);
        cosmeticVars.put(23112, 20370);
        cosmeticVars.put(23113, 20370);
        cosmeticVars.put(23114, 20370);
        cosmeticVars.put(23115, 20370);
        cosmeticVars.put(23119, 20373);
        cosmeticVars.put(23120, 20373);
        cosmeticVars.put(23116, 20374);
        cosmeticVars.put(23117, 20374);
        cosmeticVars.put(23118, 20375);
        cosmeticVars.put(23121, 20411);
        cosmeticVars.put(23122, 20411);
        cosmeticVars.put(23123, 20411);
        cosmeticVars.put(23124, 20411);
        cosmeticVars.put(23125, 20411);
        cosmeticVars.put(11052, 20412);
        cosmeticVars.put(11053, 20413);
        cosmeticVars.put(11054, 20414);
        cosmeticVars.put(11055, 20415);
        cosmeticVars.put(11056, 20416);
        cosmeticVars.put(21210, 20547);
        cosmeticVars.put(21211, 20548);
        cosmeticVars.put(31886, 20551);
        cosmeticVars.put(21212, 20551);
        cosmeticVars.put(23185, 20553);
        cosmeticVars.put(23186, 20586);
        cosmeticVars.put(23197, 20616);
        cosmeticVars.put(23198, 20616);
        cosmeticVars.put(23199, 20616);
        cosmeticVars.put(23200, 20616);
        cosmeticVars.put(23201, 20616);
        cosmeticVars.put(23202, 20616);
        cosmeticVars.put(23203, 20616);
        cosmeticVars.put(23204, 20617);
        cosmeticVars.put(23205, 20617);
        cosmeticVars.put(23206, 20617);
        cosmeticVars.put(23207, 20617);
        cosmeticVars.put(23208, 20617);
        cosmeticVars.put(23209, 20617);
        cosmeticVars.put(23210, 20618);
        cosmeticVars.put(23211, 20618);
        cosmeticVars.put(23212, 20618);
        cosmeticVars.put(23213, 20618);
        cosmeticVars.put(23214, 20618);
        cosmeticVars.put(23215, 20618);
        cosmeticVars.put(23216, 20618);
        cosmeticVars.put(23217, 20619);
        cosmeticVars.put(23218, 20619);
        cosmeticVars.put(23219, 20619);
        cosmeticVars.put(23220, 20619);
        cosmeticVars.put(23221, 20619);
        cosmeticVars.put(23222, 20619);
        cosmeticVars.put(6975, 21297);
        cosmeticVars.put(25026, 21297);
        cosmeticVars.put(25027, 21297);
        cosmeticVars.put(25677, 21297);
        cosmeticVars.put(25678, 21297);
        cosmeticVars.put(25679, 21297);
        cosmeticVars.put(25680, 21297);
        cosmeticVars.put(25681, 21298);
        cosmeticVars.put(25682, 21298);
        cosmeticVars.put(25683, 21298);
        cosmeticVars.put(25684, 21298);
        cosmeticVars.put(25765, 21298);
        cosmeticVars.put(25766, 21298);
        cosmeticVars.put(25767, 21299);
        cosmeticVars.put(25772, 21299);
        cosmeticVars.put(25773, 21299);
        cosmeticVars.put(25774, 21299);
        cosmeticVars.put(25775, 21299);
        cosmeticVars.put(25780, 21299);
        cosmeticVars.put(25781, 21299);
        cosmeticVars.put(25782, 21300);
        cosmeticVars.put(25783, 21300);
        cosmeticVars.put(25784, 21300);
        cosmeticVars.put(25785, 21300);
        cosmeticVars.put(25786, 21300);
        cosmeticVars.put(25787, 21300);
        cosmeticVars.put(25788, 21301);
        cosmeticVars.put(25789, 21302);
        cosmeticVars.put(24043, 20678);
        cosmeticVars.put(24044, 20678);
        cosmeticVars.put(24045, 20678);
        cosmeticVars.put(24046, 20678);
        cosmeticVars.put(24047, 20678);
        cosmeticVars.put(24048, 20678);
        cosmeticVars.put(24049, 20678);
        cosmeticVars.put(24050, 20678);
        cosmeticVars.put(24051, 20679);
        cosmeticVars.put(24052, 20679);
        cosmeticVars.put(24053, 20679);
        cosmeticVars.put(24054, 20679);
        cosmeticVars.put(24055, 20679);
        cosmeticVars.put(24056, 20679);
        cosmeticVars.put(24057, 20679);
        cosmeticVars.put(24058, 20679);
        cosmeticVars.put(24059, 20679);
        cosmeticVars.put(24042, 20680);
        cosmeticVars.put(24038, 20725);
        cosmeticVars.put(24039, 20727);
        cosmeticVars.put(24040, 20726);
        cosmeticVars.put(29098, 25233);
        cosmeticVars.put(24041, 14429);
        cosmeticVars.put(24166, 20798);
        cosmeticVars.put(24167, 20798);
        cosmeticVars.put(24168, 20798);
        cosmeticVars.put(24169, 20798);
        cosmeticVars.put(24170, 20798);
        cosmeticVars.put(24171, 20798);
        cosmeticVars.put(24172, 20798);
        cosmeticVars.put(24173, 20799);
        cosmeticVars.put(24174, 20799);
        cosmeticVars.put(24175, 20799);
        cosmeticVars.put(24176, 20799);
        cosmeticVars.put(24177, 20799);
        cosmeticVars.put(24178, 20799);
        cosmeticVars.put(24179, 20799);
        cosmeticVars.put(24180, 20799);
        cosmeticVars.put(24125, 20789);
        cosmeticVars.put(24136, 20789);
        cosmeticVars.put(24137, 20789);
        cosmeticVars.put(24138, 20789);
        cosmeticVars.put(24139, 20789);
        cosmeticVars.put(24140, 20789);
        cosmeticVars.put(24196, 20886);
        cosmeticVars.put(24199, 20889);
        cosmeticVars.put(21104, 20858);
        cosmeticVars.put(21105, 20859);
        cosmeticVars.put(21106, 20860);
        cosmeticVars.put(21107, 20861);
        cosmeticVars.put(14993, 21015);
        cosmeticVars.put(21109, 21017);
        cosmeticVars.put(29724, 21001);
        cosmeticVars.put(24191, 21001);
        cosmeticVars.put(24192, 21001);
        cosmeticVars.put(24193, 21001);
        cosmeticVars.put(24194, 21001);
        cosmeticVars.put(24980, 21001);
        cosmeticVars.put(24981, 21001);
        cosmeticVars.put(24982, 21001);
        cosmeticVars.put(24983, 21001);
        cosmeticVars.put(24984, 21001);
        cosmeticVars.put(24985, 21001);
        cosmeticVars.put(24986, 21001);
        cosmeticVars.put(24987, 21001);
        cosmeticVars.put(24988, 21001);
        cosmeticVars.put(24989, 21001);
        cosmeticVars.put(24990, 21001);
        cosmeticVars.put(24991, 21001);
        cosmeticVars.put(24992, 21001);
        cosmeticVars.put(24993, 21001);
        cosmeticVars.put(24994, 21001);
        cosmeticVars.put(24995, 21001);
        cosmeticVars.put(24996, 21001);
        cosmeticVars.put(24997, 21001);
        cosmeticVars.put(29725, 21002);
        cosmeticVars.put(24998, 21002);
        cosmeticVars.put(24999, 21002);
        cosmeticVars.put(25000, 21002);
        cosmeticVars.put(26469, 21002);
        cosmeticVars.put(25001, 21002);
        cosmeticVars.put(26470, 21002);
        cosmeticVars.put(25002, 21002);
        cosmeticVars.put(25003, 21002);
        cosmeticVars.put(25004, 21002);
        cosmeticVars.put(25005, 21002);
        cosmeticVars.put(25006, 21002);
        cosmeticVars.put(25007, 21002);
        cosmeticVars.put(26471, 21002);
        cosmeticVars.put(25008, 21002);
        cosmeticVars.put(25009, 21002);
        cosmeticVars.put(25010, 21002);
        cosmeticVars.put(25011, 21002);
        cosmeticVars.put(26472, 21002);
        cosmeticVars.put(7013, 21249);
        cosmeticVars.put(14992, 21243);
        cosmeticVars.put(25697, 21226);
        cosmeticVars.put(25698, 21227);
        cosmeticVars.put(25055, 21044);
        cosmeticVars.put(25056, 21045);
        cosmeticVars.put(25057, 21038);
        cosmeticVars.put(25058, 21038);
        cosmeticVars.put(25059, 21039);
        cosmeticVars.put(25060, 21039);
        cosmeticVars.put(25061, 21041);
        cosmeticVars.put(25062, 21041);
        cosmeticVars.put(25063, 21042);
        cosmeticVars.put(25064, 21042);
        cosmeticVars.put(25065, 21047);
        cosmeticVars.put(25066, 21048);
        cosmeticVars.put(25745, 21291);
        cosmeticVars.put(25746, 21291);
        cosmeticVars.put(25747, 21291);
        cosmeticVars.put(25748, 21291);
        cosmeticVars.put(25749, 21291);
        cosmeticVars.put(25750, 21291);
        cosmeticVars.put(25751, 21291);
        cosmeticVars.put(25752, 21292);
        cosmeticVars.put(25755, 21292);
        cosmeticVars.put(25756, 21292);
        cosmeticVars.put(25757, 21292);
        cosmeticVars.put(25758, 21292);
        cosmeticVars.put(25759, 21292);
        cosmeticVars.put(25760, 21292);
        cosmeticVars.put(25761, 21292);
        cosmeticVars.put(26394, 21365);
        cosmeticVars.put(26457, 21365);
        cosmeticVars.put(26458, 21365);
        cosmeticVars.put(26459, 21365);
        cosmeticVars.put(26460, 21366);
        cosmeticVars.put(26461, 21366);
        cosmeticVars.put(26462, 21366);
        cosmeticVars.put(26463, 21366);
        cosmeticVars.put(26464, 21362);
        cosmeticVars.put(26465, 21362);
        cosmeticVars.put(26466, 21362);
        cosmeticVars.put(26467, 21362);
        cosmeticVars.put(26468, 21363);
        cosmeticVars.put(26399, 21354);
        cosmeticVars.put(26400, 21354);
        cosmeticVars.put(26401, 21354);
        cosmeticVars.put(26402, 21354);
        cosmeticVars.put(26404, 21354);
        cosmeticVars.put(26403, 21354);
        cosmeticVars.put(26405, 21354);
        cosmeticVars.put(26406, 21354);
        cosmeticVars.put(26407, 21355);
        cosmeticVars.put(26408, 21355);
        cosmeticVars.put(26409, 21355);
        cosmeticVars.put(26410, 21355);
        cosmeticVars.put(26412, 21355);
        cosmeticVars.put(26411, 21355);
        cosmeticVars.put(26413, 21355);
        cosmeticVars.put(26414, 21355);
        cosmeticVars.put(26532, 21471);
        cosmeticVars.put(26590, 21708);
        cosmeticVars.put(31862, 24922);
        cosmeticVars.put(31863, 24923);
        cosmeticVars.put(31864, 24924);
        cosmeticVars.put(31865, 24925);
        cosmeticVars.put(31866, 24926);
        cosmeticVars.put(31867, 24927);
        cosmeticVars.put(31868, 24928);
        cosmeticVars.put(31869, 24929);
        cosmeticVars.put(31870, 24930);
        cosmeticVars.put(31871, 24931);
        cosmeticVars.put(31872, 24932);
        cosmeticVars.put(31873, 24933);
        cosmeticVars.put(31874, 24934);
        cosmeticVars.put(31875, 24935);
        cosmeticVars.put(31876, 24936);
        cosmeticVars.put(26576, 21670);
        cosmeticVars.put(26577, 21672);
        cosmeticVars.put(26578, 21674);
        cosmeticVars.put(26508, 21681);
        cosmeticVars.put(30398, 26917);
        cosmeticVars.put(30399, 26917);
        cosmeticVars.put(30400, 26917);
        cosmeticVars.put(30401, 26917);
        cosmeticVars.put(30402, 26917);
        cosmeticVars.put(30403, 26917);
        cosmeticVars.put(30404, 26917);
        cosmeticVars.put(30405, 26917);
        cosmeticVars.put(19592, 30601);
        cosmeticVars.put(21214, 30601);
        cosmeticVars.put(19593, 30601);
        cosmeticVars.put(24277, 6000);
        cosmeticVars.put(24329, 21700);
        cosmeticVars.put(24330, 21701);
        cosmeticVars.put(26581, 21702);
        cosmeticVars.put(31887, 21824);
        cosmeticVars.put(21187, 21824);
        cosmeticVars.put(21321, 21826);
        cosmeticVars.put(31888, 21828);
        cosmeticVars.put(21322, 21828);
        cosmeticVars.put(21323, 21830);
        cosmeticVars.put(34724, 33877);
        cosmeticVars.put(34725, 33879);
        cosmeticVars.put(34726, 33881);
        cosmeticVars.put(21324, 21847);
        cosmeticVars.put(21325, 21848);
        cosmeticVars.put(26659, 21762);
        cosmeticVars.put(26661, 21762);
        cosmeticVars.put(29728, 22132);
        cosmeticVars.put(27753, 22132);
        cosmeticVars.put(27754, 22132);
        cosmeticVars.put(27755, 22132);
        cosmeticVars.put(27756, 22132);
        cosmeticVars.put(27757, 22132);
        cosmeticVars.put(27758, 22132);
        cosmeticVars.put(27759, 22132);
        cosmeticVars.put(27760, 22132);
        cosmeticVars.put(27761, 22132);
        cosmeticVars.put(29729, 22133);
        cosmeticVars.put(27762, 22133);
        cosmeticVars.put(27763, 22133);
        cosmeticVars.put(27764, 22133);
        cosmeticVars.put(27765, 22133);
        cosmeticVars.put(27766, 22133);
        cosmeticVars.put(27767, 22133);
        cosmeticVars.put(27768, 22133);
        cosmeticVars.put(27769, 22133);
        cosmeticVars.put(27770, 22133);
        cosmeticVars.put(27771, 22133);
        cosmeticVars.put(27772, 22133);
        cosmeticVars.put(27773, 22133);
        cosmeticVars.put(27774, 22133);
        cosmeticVars.put(29730, 22135);
        cosmeticVars.put(27775, 22135);
        cosmeticVars.put(27777, 22135);
        cosmeticVars.put(27776, 22135);
        cosmeticVars.put(27778, 22135);
        cosmeticVars.put(27779, 22135);
        cosmeticVars.put(27780, 22135);
        cosmeticVars.put(27781, 22135);
        cosmeticVars.put(27782, 22135);
        cosmeticVars.put(27783, 22135);
        cosmeticVars.put(29731, 22134);
        cosmeticVars.put(27786, 22134);
        cosmeticVars.put(27785, 22134);
        cosmeticVars.put(27784, 22134);
        cosmeticVars.put(27787, 22134);
        cosmeticVars.put(27788, 22134);
        cosmeticVars.put(27789, 22134);
        cosmeticVars.put(27790, 22134);
        cosmeticVars.put(27791, 22134);
        cosmeticVars.put(27792, 22134);
        cosmeticVars.put(29726, 22264);
        cosmeticVars.put(28070, 22264);
        cosmeticVars.put(28071, 22264);
        cosmeticVars.put(28072, 22264);
        cosmeticVars.put(28073, 22264);
        cosmeticVars.put(28074, 22264);
        cosmeticVars.put(28075, 22264);
        cosmeticVars.put(28076, 22264);
        cosmeticVars.put(28077, 22264);
        cosmeticVars.put(28078, 22264);
        cosmeticVars.put(28079, 22264);
        cosmeticVars.put(28080, 22264);
        cosmeticVars.put(28081, 22264);
        cosmeticVars.put(28082, 22264);
        cosmeticVars.put(28083, 22264);
        cosmeticVars.put(28084, 22264);
        cosmeticVars.put(28085, 22264);
        cosmeticVars.put(28086, 22264);
        cosmeticVars.put(28087, 22264);
        cosmeticVars.put(28088, 22264);
        cosmeticVars.put(28089, 22264);
        cosmeticVars.put(28090, 22267);
        cosmeticVars.put(28091, 22267);
        cosmeticVars.put(28092, 22267);
        cosmeticVars.put(28093, 22267);
        cosmeticVars.put(28094, 22265);
        cosmeticVars.put(28095, 22265);
        cosmeticVars.put(28096, 22265);
        cosmeticVars.put(28097, 22265);
        cosmeticVars.put(29727, 22266);
        cosmeticVars.put(28098, 22266);
        cosmeticVars.put(28099, 22266);
        cosmeticVars.put(28100, 22266);
        cosmeticVars.put(28101, 22266);
        cosmeticVars.put(28031, 22218);
        cosmeticVars.put(28032, 22219);
        cosmeticVars.put(28033, 22220);
        cosmeticVars.put(28165, 18920);
        cosmeticVars.put(28168, 22403);
        cosmeticVars.put(28061, 22361);
        cosmeticVars.put(28156, 22391);
        cosmeticVars.put(28157, 22392);
        cosmeticVars.put(28158, 22393);
        cosmeticVars.put(28159, 22394);
        cosmeticVars.put(28160, 22395);
        cosmeticVars.put(28162, 22399);
        cosmeticVars.put(28161, 22398);
        cosmeticVars.put(28163, 22396);
        cosmeticVars.put(28251, 22449);
        cosmeticVars.put(28252, 22450);
        cosmeticVars.put(28253, 22451);
        cosmeticVars.put(28254, 22452);
        cosmeticVars.put(28255, 22452);
        cosmeticVars.put(28256, 22453);
        cosmeticVars.put(28257, 22453);
        cosmeticVars.put(28258, 22454);
        cosmeticVars.put(28297, 22549);
        cosmeticVars.put(28298, 22550);
        cosmeticVars.put(28299, 22551);
        cosmeticVars.put(28321, 22586);
        cosmeticVars.put(28323, 22588);
        cosmeticVars.put(28325, 22590);
        cosmeticVars.put(28171, 22666);
        cosmeticVars.put(28172, 22666);
        cosmeticVars.put(28200, 22666);
        cosmeticVars.put(28201, 22666);
        cosmeticVars.put(28202, 22666);
        cosmeticVars.put(28376, 22666);
        cosmeticVars.put(28377, 22666);
        cosmeticVars.put(28378, 22666);
        cosmeticVars.put(28379, 22667);
        cosmeticVars.put(28380, 22667);
        cosmeticVars.put(28381, 22667);
        cosmeticVars.put(28382, 22667);
        cosmeticVars.put(28383, 22667);
        cosmeticVars.put(28384, 22667);
        cosmeticVars.put(28385, 22667);
        cosmeticVars.put(28386, 22667);
        cosmeticVars.put(28418, 22708);
        cosmeticVars.put(28419, 22710);
        cosmeticVars.put(28420, 22712);
        cosmeticVars.put(28417, 22717);
        cosmeticVars.put(28444, 22828);
        cosmeticVars.put(28357, 38933);
        cosmeticVars.put(28447, 22823);
        cosmeticVars.put(28449, 22824);
        cosmeticVars.put(28787, 23047);
        cosmeticVars.put(28789, 23049);
        cosmeticVars.put(28791, 23048);
        cosmeticVars.put(28793, 23050);
        cosmeticVars.put(28795, 23052);
        cosmeticVars.put(28797, 23051);
        cosmeticVars.put(28625, 22870);
        cosmeticVars.put(28626, 22870);
        cosmeticVars.put(28627, 22870);
        cosmeticVars.put(28628, 22897);
        cosmeticVars.put(28629, 22897);
        cosmeticVars.put(28630, 22897);
        cosmeticVars.put(29733, 22898);
        cosmeticVars.put(28631, 22898);
        cosmeticVars.put(28632, 22898);
        cosmeticVars.put(28633, 22898);
        cosmeticVars.put(28634, 22898);
        cosmeticVars.put(28635, 22898);
        cosmeticVars.put(28636, 22898);
        cosmeticVars.put(28637, 22898);
        cosmeticVars.put(24284, 23275);
        cosmeticVars.put(24285, 23276);
        cosmeticVars.put(24286, 23272);
        cosmeticVars.put(24287, 23273);
        cosmeticVars.put(29761, 26363);
        cosmeticVars.put(29762, 26364);
        cosmeticVars.put(29763, 26365);
        cosmeticVars.put(29764, 26366);
        cosmeticVars.put(29765, 26367);
        cosmeticVars.put(24353, 23334);
        cosmeticVars.put(24356, 23337);
        cosmeticVars.put(24359, 23340);
        cosmeticVars.put(28893, 24772);
        cosmeticVars.put(28894, 24772);
        cosmeticVars.put(28895, 24772);
        cosmeticVars.put(28896, 24772);
        cosmeticVars.put(28897, 24772);
        cosmeticVars.put(28898, 24772);
        cosmeticVars.put(28899, 24773);
        cosmeticVars.put(28900, 24773);
        cosmeticVars.put(28901, 24773);
        cosmeticVars.put(28902, 24773);
        cosmeticVars.put(28903, 24773);
        cosmeticVars.put(28904, 24773);
        cosmeticVars.put(28905, 24774);
        cosmeticVars.put(28906, 24771);
        cosmeticVars.put(28957, 24919);
        cosmeticVars.put(28945, 25272);
        cosmeticVars.put(28946, 25272);
        cosmeticVars.put(28947, 25272);
        cosmeticVars.put(28960, 25272);
        cosmeticVars.put(28961, 25272);
        cosmeticVars.put(29085, 25272);
        cosmeticVars.put(29086, 25273);
        cosmeticVars.put(29087, 25273);
        cosmeticVars.put(29088, 25273);
        cosmeticVars.put(29089, 25273);
        cosmeticVars.put(29090, 25273);
        cosmeticVars.put(29091, 25273);
        cosmeticVars.put(29092, 25274);
        cosmeticVars.put(29093, 25274);
        cosmeticVars.put(29094, 25274);
        cosmeticVars.put(29095, 25274);
        cosmeticVars.put(29096, 25274);
        cosmeticVars.put(29097, 25274);
        cosmeticVars.put(6565, 25266);
        cosmeticVars.put(28944, 25268);
        cosmeticVars.put(28919, 25270);
        cosmeticVars.put(31889, 25270);
        cosmeticVars.put(29008, 25478);
        cosmeticVars.put(29155, 25480);
        cosmeticVars.put(29156, 25482);
        cosmeticVars.put(29344, 25799);
        cosmeticVars.put(29345, 25800);
        cosmeticVars.put(29346, 25801);
        cosmeticVars.put(29148, 25520);
        cosmeticVars.put(29149, 25520);
        cosmeticVars.put(29150, 25520);
        cosmeticVars.put(29151, 25520);
        cosmeticVars.put(29152, 25520);
        cosmeticVars.put(29153, 25520);
        cosmeticVars.put(29338, 25778);
        cosmeticVars.put(29735, 25808);
        cosmeticVars.put(29365, 25808);
        cosmeticVars.put(29366, 25808);
        cosmeticVars.put(29367, 25808);
        cosmeticVars.put(29368, 25808);
        cosmeticVars.put(29369, 25808);
        cosmeticVars.put(29370, 25808);
        cosmeticVars.put(29371, 25808);
        cosmeticVars.put(29372, 25808);
        cosmeticVars.put(29736, 25809);
        cosmeticVars.put(29381, 25809);
        cosmeticVars.put(29382, 25809);
        cosmeticVars.put(29383, 25809);
        cosmeticVars.put(29384, 25809);
        cosmeticVars.put(29385, 25809);
        cosmeticVars.put(29386, 25809);
        cosmeticVars.put(29387, 25809);
        cosmeticVars.put(29388, 25809);
        cosmeticVars.put(29158, 26015);
        cosmeticVars.put(28962, 26013);
        cosmeticVars.put(29347, 26210);
        cosmeticVars.put(29699, 26258);
        cosmeticVars.put(29786, 26421);
        cosmeticVars.put(29789, 26428);
        cosmeticVars.put(29790, 26429);
        cosmeticVars.put(29791, 26424);
        cosmeticVars.put(29767, 26405);
        cosmeticVars.put(28923, 26353);
        cosmeticVars.put(29842, 26491);
        cosmeticVars.put(29843, 26486);
        cosmeticVars.put(29844, 26485);
        cosmeticVars.put(29845, 26487);
        cosmeticVars.put(29846, 26488);
        cosmeticVars.put(29847, 26490);
        cosmeticVars.put(29848, 26489);
        cosmeticVars.put(29840, 26492);
        cosmeticVars.put(29808, 26478);
        cosmeticVars.put(29809, 26478);
        cosmeticVars.put(29810, 26478);
        cosmeticVars.put(29811, 26478);
        cosmeticVars.put(29812, 26478);
        cosmeticVars.put(29813, 26479);
        cosmeticVars.put(29814, 26479);
        cosmeticVars.put(29815, 26479);
        cosmeticVars.put(29816, 26479);
        cosmeticVars.put(29817, 26479);
        cosmeticVars.put(29818, 26480);
        cosmeticVars.put(29819, 26480);
        cosmeticVars.put(29820, 26480);
        cosmeticVars.put(29821, 26480);
        cosmeticVars.put(29822, 26480);
        cosmeticVars.put(29823, 26481);
        cosmeticVars.put(29824, 26481);
        cosmeticVars.put(29825, 26481);
        cosmeticVars.put(29826, 26481);
        cosmeticVars.put(29827, 26481);
        cosmeticVars.put(29828, 26482);
        cosmeticVars.put(29829, 26482);
        cosmeticVars.put(29830, 26482);
        cosmeticVars.put(29831, 26482);
        cosmeticVars.put(29832, 26482);
        cosmeticVars.put(29833, 26482);
        cosmeticVars.put(29834, 26483);
        cosmeticVars.put(29835, 26483);
        cosmeticVars.put(29836, 26483);
        cosmeticVars.put(29837, 26483);
        cosmeticVars.put(29838, 26483);
        cosmeticVars.put(29919, 26607);
        cosmeticVars.put(29920, 26608);
        cosmeticVars.put(29921, 26609);
        cosmeticVars.put(29851, 26638);
        cosmeticVars.put(29929, 26640);
        cosmeticVars.put(29931, 26642);
        cosmeticVars.put(29933, 26644);
        cosmeticVars.put(29935, 26646);
        cosmeticVars.put(29937, 26648);
        cosmeticVars.put(29939, 26650);
        cosmeticVars.put(29941, 26652);
        cosmeticVars.put(29943, 26654);
        cosmeticVars.put(29945, 26656);
        cosmeticVars.put(29947, 26658);
        cosmeticVars.put(29949, 26660);
        cosmeticVars.put(29951, 26662);
        cosmeticVars.put(29953, 26664);
        cosmeticVars.put(29955, 26666);
        cosmeticVars.put(29957, 26668);
        cosmeticVars.put(29959, 26670);
        cosmeticVars.put(29961, 26672);
        cosmeticVars.put(29963, 26674);
        cosmeticVars.put(29965, 26676);
        cosmeticVars.put(29967, 26678);
        cosmeticVars.put(29969, 26680);
        cosmeticVars.put(29971, 26682);
        cosmeticVars.put(29973, 26684);
        cosmeticVars.put(29975, 26686);
        cosmeticVars.put(29977, 26688);
        cosmeticVars.put(29979, 26690);
        cosmeticVars.put(30395, 26918);
        cosmeticVars.put(30537, 27082);
        cosmeticVars.put(30736, 27155);
        cosmeticVars.put(30738, 27154);
        cosmeticVars.put(30783, 27184);
        cosmeticVars.put(30784, 27184);
        cosmeticVars.put(30785, 27184);
        cosmeticVars.put(30786, 27184);
        cosmeticVars.put(30787, 27183);
        cosmeticVars.put(30788, 27183);
        cosmeticVars.put(30789, 27183);
        cosmeticVars.put(30790, 27183);
        cosmeticVars.put(30791, 27183);
        cosmeticVars.put(30792, 27183);
        cosmeticVars.put(30796, 27193);
        cosmeticVars.put(30797, 27193);
        cosmeticVars.put(30798, 27193);
        cosmeticVars.put(30799, 27193);
        cosmeticVars.put(30800, 27193);
        cosmeticVars.put(30794, 27194);
        cosmeticVars.put(30793, 27182);
        cosmeticVars.put(30795, 27195);
        cosmeticVars.put(30801, 27185);
        cosmeticVars.put(30835, 27386);
        cosmeticVars.put(30836, 27386);
        cosmeticVars.put(30837, 27386);
        cosmeticVars.put(30838, 27386);
        cosmeticVars.put(30839, 27386);
        cosmeticVars.put(30840, 27386);
        cosmeticVars.put(30541, 27356);
        cosmeticVars.put(30552, 27359);
        cosmeticVars.put(31257, 27654);
        cosmeticVars.put(31225, 27649);
        cosmeticVars.put(31226, 27649);
        cosmeticVars.put(31227, 27649);
        cosmeticVars.put(31228, 27649);
        cosmeticVars.put(31229, 27649);
        cosmeticVars.put(31230, 27649);
        cosmeticVars.put(31231, 27649);
        cosmeticVars.put(31232, 27649);
        cosmeticVars.put(6230, 27705);
        cosmeticVars.put(31574, 28123);
        cosmeticVars.put(31651, 28197);
        cosmeticVars.put(31653, 28200);
        cosmeticVars.put(31655, 28203);
        cosmeticVars.put(31657, 28206);
        cosmeticVars.put(31602, 28149);
        cosmeticVars.put(31603, 28150);
        cosmeticVars.put(31604, 28151);
        cosmeticVars.put(31605, 28152);
        cosmeticVars.put(31647, 28170);
        cosmeticVars.put(31666, 28246);
        cosmeticVars.put(31664, 28361);
        cosmeticVars.put(31665, 28362);
        cosmeticVars.put(18709, 28378);
        cosmeticVars.put(26381, 28379);
        cosmeticVars.put(26382, 28380);
        cosmeticVars.put(31698, 28381);
        cosmeticVars.put(31699, 28382);
        cosmeticVars.put(31966, 28748);
        cosmeticVars.put(31968, 28749);
        cosmeticVars.put(31970, 28750);
        cosmeticVars.put(31818, 28501);
        cosmeticVars.put(31819, 28500);
        cosmeticVars.put(31383, 28662);
        cosmeticVars.put(32042, 28822);
        cosmeticVars.put(32044, 28823);
        cosmeticVars.put(32046, 28824);
        cosmeticVars.put(32048, 28825);
        cosmeticVars.put(32093, 28960);
        cosmeticVars.put(32094, 28960);
        cosmeticVars.put(32096, 28961);
        cosmeticVars.put(32097, 28961);
        cosmeticVars.put(32095, 28962);
        cosmeticVars.put(32098, 28963);
        cosmeticVars.put(32099, 28948);
        cosmeticVars.put(32308, 1741);
        cosmeticVars.put(32309, 1742);
        cosmeticVars.put(32310, 1744);
        cosmeticVars.put(32311, 1743);
        cosmeticVars.put(32312, 1745);
        cosmeticVars.put(30503, 29517);
        cosmeticVars.put(28833, 29628);
        cosmeticVars.put(35692, 29628);
        cosmeticVars.put(35693, 29628);
        cosmeticVars.put(35694, 29628);
        cosmeticVars.put(27587, 29628);
        cosmeticVars.put(27818, 29628);
        cosmeticVars.put(27819, 29628);
        cosmeticVars.put(27820, 29628);
        cosmeticVars.put(27821, 29628);
        cosmeticVars.put(27822, 29628);
        cosmeticVars.put(27823, 29628);
        cosmeticVars.put(27824, 29628);
        cosmeticVars.put(24410, 29628);
        cosmeticVars.put(32541, 29668);
        cosmeticVars.put(32517, 29702);
        cosmeticVars.put(31926, 29683);
        cosmeticVars.put(31928, 29683);
        cosmeticVars.put(32677, 29927);
        cosmeticVars.put(32678, 29927);
        cosmeticVars.put(32713, 30043);
        cosmeticVars.put(32768, 30090);
        cosmeticVars.put(32766, 30070);
        cosmeticVars.put(32816, 30187);
        cosmeticVars.put(33034, 30472);
        cosmeticVars.put(33038, 30473);
        cosmeticVars.put(33216, 30801);
        cosmeticVars.put(33300, 30991);
        cosmeticVars.put(33302, 30992);
        cosmeticVars.put(33308, 30993);
        cosmeticVars.put(33310, 30994);
        cosmeticVars.put(33304, 30996);
        cosmeticVars.put(33306, 30995);
        cosmeticVars.put(33244, 30895);
        cosmeticVars.put(33245, 30921);
        cosmeticVars.put(33246, 30921);
        cosmeticVars.put(33247, 30883);
        cosmeticVars.put(33248, 30883);
        cosmeticVars.put(33249, 30909);
        cosmeticVars.put(33250, 30909);
        cosmeticVars.put(33326, 31054);
        cosmeticVars.put(33340, 31019);
        cosmeticVars.put(33341, 31020);
        cosmeticVars.put(33342, 31021);
        cosmeticVars.put(33344, 31022);
        cosmeticVars.put(33343, 31023);
        cosmeticVars.put(33345, 31026);
        cosmeticVars.put(33346, 31025);
        cosmeticVars.put(33357, 31090);
        cosmeticVars.put(33420, 31217);
        cosmeticVars.put(33422, 31218);
        cosmeticVars.put(33423, 31218);
        cosmeticVars.put(33424, 31218);
        cosmeticVars.put(33425, 31218);
        cosmeticVars.put(32934, 18292);
        cosmeticVars.put(33492, 18294);
        cosmeticVars.put(33494, 18293);
        cosmeticVars.put(33496, 30973);
        cosmeticVars.put(33659, 32673);
        cosmeticVars.put(11413, 33065);
        cosmeticVars.put(11414, 33066);
        cosmeticVars.put(33793, 33067);
        cosmeticVars.put(33794, 33068);
        cosmeticVars.put(33796, 33088);
        cosmeticVars.put(33798, 33088);
        cosmeticVars.put(33800, 33142);
        cosmeticVars.put(34852, 34249);
        cosmeticVars.put(34853, 34249);
        cosmeticVars.put(34854, 34249);
        cosmeticVars.put(34893, 34249);
        cosmeticVars.put(33900, 33151);
        cosmeticVars.put(33901, 33151);
        cosmeticVars.put(33902, 33151);
        cosmeticVars.put(33903, 33151);
        cosmeticVars.put(34727, 33906);
        cosmeticVars.put(34728, 33906);
        cosmeticVars.put(34729, 33906);
        cosmeticVars.put(34730, 33906);
        cosmeticVars.put(33890, 33361);
        cosmeticVars.put(34070, 33552);
        cosmeticVars.put(34032, 33536);
        cosmeticVars.put(34033, 33537);
        cosmeticVars.put(34184, 33701);
        cosmeticVars.put(34213, 33733);
        cosmeticVars.put(34438, 33788);
        cosmeticVars.put(34603, 33861);
        cosmeticVars.put(34548, 33870);
        cosmeticVars.put(34712, 33972);
        cosmeticVars.put(34757, 20806);
        cosmeticVars.put(34758, 20806);
        cosmeticVars.put(34759, 20806);
        cosmeticVars.put(6327, 34122);
        cosmeticVars.put(15406, 34123);
        cosmeticVars.put(34774, 34124);
        cosmeticVars.put(34776, 34125);
        cosmeticVars.put(34778, 34126);
        cosmeticVars.put(34780, 34127);
        cosmeticVars.put(35311, 34855);
        cosmeticVars.put(34876, 34202);
        cosmeticVars.put(35010, 34202);
        cosmeticVars.put(34830, 34203);
        cosmeticVars.put(34913, 34288);
        cosmeticVars.put(34914, 34289);
        cosmeticVars.put(34915, 34290);
        cosmeticVars.put(34916, 34291);
        cosmeticVars.put(34917, 34292);
        cosmeticVars.put(34918, 34293);
        cosmeticVars.put(35095, 34827);
        cosmeticVars.put(35096, 34828);
        cosmeticVars.put(35097, 34829);
        cosmeticVars.put(35098, 34830);
        cosmeticVars.put(35099, 34831);
        cosmeticVars.put(34955, 34335);
        cosmeticVars.put(34966, 34331);
        cosmeticVars.put(34946, 33756);
        cosmeticVars.put(35223, 34448);
        cosmeticVars.put(35226, 34506);
        cosmeticVars.put(35365, 34900);
        cosmeticVars.put(35296, 34923);
        cosmeticVars.put(35370, 34932);
        cosmeticVars.put(35371, 34933);
        cosmeticVars.put(34539, 35202);
        cosmeticVars.put(34541, 35203);
        cosmeticVars.put(34950, 35204);
        cosmeticVars.put(35322, 35206);
        cosmeticVars.put(34954, 35205);
        cosmeticVars.put(35368, 35207);
        cosmeticVars.put(1198, 35681);
        cosmeticVars.put(1199, 35531);
        cosmeticVars.put(1200, 35532);
        cosmeticVars.put(1202, 35533);
        cosmeticVars.put(1201, 35534);
        cosmeticVars.put(1204, 35582);
        cosmeticVars.put(1205, 35583);
        cosmeticVars.put(1206, 35584);
        cosmeticVars.put(1208, 35585);
        cosmeticVars.put(1207, 35586);
        cosmeticVars.put(1210, 35539);
        cosmeticVars.put(1211, 35540);
        cosmeticVars.put(1212, 35541);
        cosmeticVars.put(1214, 35542);
        cosmeticVars.put(1213, 35543);
        cosmeticVars.put(1216, 35544);
        cosmeticVars.put(1217, 35545);
        cosmeticVars.put(1218, 35546);
        cosmeticVars.put(1220, 35547);
        cosmeticVars.put(1219, 35548);
        cosmeticVars.put(1424, 35821);
        cosmeticVars.put(1349, 35849);
        cosmeticVars.put(1350, 35850);
        cosmeticVars.put(1351, 35851);
        cosmeticVars.put(1352, 35852);
        cosmeticVars.put(1353, 35853);
        cosmeticVars.put(1354, 35854);
        cosmeticVars.put(1509, 35884);
        cosmeticVars.put(1355, 35855);
        cosmeticVars.put(1514, 35880);
        cosmeticVars.put(5283, 41399);
        cosmeticVars.put(42124, 42516);
        cosmeticVars.put(6724, 9439);
        cosmeticVars.put(6725, 9440);
        cosmeticVars.put(6726, 9438);
        cosmeticVars.put(6727, 9441);
        cosmeticVars.put(6735, 36155);
        cosmeticVars.put(6728, 9439);
        cosmeticVars.put(6729, 9440);
        cosmeticVars.put(6730, 9438);
        cosmeticVars.put(6731, 9441);
        cosmeticVars.put(6734, 36154);
        cosmeticVars.put(6749, 35987);
        cosmeticVars.put(6750, 35988);
        cosmeticVars.put(6751, 35989);
        cosmeticVars.put(6753, 35990);
        cosmeticVars.put(6754, 35991);
        cosmeticVars.put(6755, 35992);
        cosmeticVars.put(6757, 35993);
        cosmeticVars.put(6758, 35994);
        cosmeticVars.put(6760, 35995);
        cosmeticVars.put(7125, 35996);
        cosmeticVars.put(7126, 35997);
        cosmeticVars.put(7127, 35998);
        cosmeticVars.put(7129, 36053);
        cosmeticVars.put(7130, 36047);
        cosmeticVars.put(7131, 36054);
        cosmeticVars.put(7196, 35984);
        cosmeticVars.put(1320, 36396);
        cosmeticVars.put(1321, 36397);
        cosmeticVars.put(1605, 36404);
        cosmeticVars.put(37604, 36794);
        cosmeticVars.put(37662, 36866);
        cosmeticVars.put(37663, 36867);
        cosmeticVars.put(37664, 36868);
        cosmeticVars.put(37665, 36869);
        cosmeticVars.put(37666, 36870);
        cosmeticVars.put(37696, 36880);
        cosmeticVars.put(37697, 36882);
        cosmeticVars.put(37703, 36884);
        cosmeticVars.put(37832, 36958);
        cosmeticVars.put(37835, 36959);
        cosmeticVars.put(40851, 41347);
        cosmeticVars.put(40852, 41348);
        cosmeticVars.put(40848, 41345);
        cosmeticVars.put(40849, 41346);
        cosmeticVars.put(40850, 41349);
        cosmeticVars.put(1573, 30305);
        cosmeticVars.put(1574, 30306);
        cosmeticVars.put(1575, 30307);
        cosmeticVars.put(1729, 30308);
        cosmeticVars.put(1752, 30309);
        cosmeticVars.put(1753, 30310);
        cosmeticVars.put(38634, 38535);
        cosmeticVars.put(38637, 38534);
        cosmeticVars.put(7041, 38652);
        cosmeticVars.put(27592, 38865);
        cosmeticVars.put(39124, 39039);
        cosmeticVars.put(39125, 39040);
        cosmeticVars.put(39126, 39041);
        cosmeticVars.put(39127, 39042);
        cosmeticVars.put(39128, 39043);
        cosmeticVars.put(39129, 39044);
        cosmeticVars.put(39153, 39212);
        cosmeticVars.put(39154, 39213);
        cosmeticVars.put(39155, 39214);
        cosmeticVars.put(39156, 39215);
        cosmeticVars.put(39157, 39216);
        cosmeticVars.put(39158, 39217);
        cosmeticVars.put(39160, 39218);
        cosmeticVars.put(39161, 39219);
        cosmeticVars.put(39162, 39220);
        cosmeticVars.put(39163, 39221);
        cosmeticVars.put(39164, 39222);
        cosmeticVars.put(39165, 39223);
        cosmeticVars.put(39167, 39224);
        cosmeticVars.put(39168, 39225);
        cosmeticVars.put(39169, 39226);
        cosmeticVars.put(39170, 39227);
        cosmeticVars.put(39171, 39228);
        cosmeticVars.put(39172, 39229);
        cosmeticVars.put(39086, 39011);
        cosmeticVars.put(39087, 39011);
        cosmeticVars.put(39088, 39011);
        cosmeticVars.put(39089, 39011);
        cosmeticVars.put(39090, 39011);
        cosmeticVars.put(39091, 39011);
        cosmeticVars.put(39565, 39989);
        cosmeticVars.put(39566, 39990);
        cosmeticVars.put(39567, 39990);
        cosmeticVars.put(39608, 40052);
        cosmeticVars.put(39607, 40051);
        cosmeticVars.put(39640, 15841);
        cosmeticVars.put(39641, 15858);
        cosmeticVars.put(39642, 15876);
        cosmeticVars.put(39643, 15883);
        cosmeticVars.put(39644, 17794);
        cosmeticVars.put(39645, 17796);
        cosmeticVars.put(39646, 17798);
        cosmeticVars.put(39647, 17800);
        cosmeticVars.put(39648, 15762);
        cosmeticVars.put(39649, 15763);
        cosmeticVars.put(39650, 15764);
        cosmeticVars.put(39651, 15807);
        cosmeticVars.put(39659, 15981);
        cosmeticVars.put(39660, 15683);
        cosmeticVars.put(39661, 15685);
        cosmeticVars.put(39662, 15687);
        cosmeticVars.put(39663, 15681);
        cosmeticVars.put(39664, 15894);
        cosmeticVars.put(39665, 15895);
        cosmeticVars.put(39666, 15896);
        cosmeticVars.put(39667, 15959);
        cosmeticVars.put(39668, 15554);
        cosmeticVars.put(39669, 15555);
        cosmeticVars.put(39670, 15556);
        cosmeticVars.put(39671, 15629);
        cosmeticVars.put(40030, 40505);
        cosmeticVars.put(40510, 41067);
        cosmeticVars.put(40511, 41068);
        cosmeticVars.put(40512, 41069);
        cosmeticVars.put(40513, 41070);
        cosmeticVars.put(40514, 41071);
        cosmeticVars.put(40504, 41072);
        cosmeticVars.put(40505, 41073);
        cosmeticVars.put(40506, 41074);
        cosmeticVars.put(40507, 41075);
        cosmeticVars.put(40508, 41076);
        cosmeticVars.put(416, 41955);
        cosmeticVars.put(454, 41956);
        cosmeticVars.put(22753, 41958);
        cosmeticVars.put(467, 41957);
        cosmeticVars.put(40627, 41235);
        cosmeticVars.put(41380, 41922);
        cosmeticVars.put(41884, 42339);
        cosmeticVars.put(41883, 42338);
        cosmeticVars.put(41885, 42337);
        cosmeticVars.put(4119, 27186);
        cosmeticVars.put(42651, 42871);
        cosmeticVars.put(36743, 34043);
        cosmeticVars.put(41467, 42092);
        cosmeticVars.put(41468, 42092);
        cosmeticVars.put(40963, 14647);
        cosmeticVars.put(40965, 14488);
        cosmeticVars.put(42644, 42867);
        cosmeticVars.put(42654, 42875);
        cosmeticVars.put(40961, 15004);
        cosmeticVars.put(42699, 42914);
        cosmeticVars.put(669, 5164);
        cosmeticVars.put(4117, 27187);
        cosmeticVars.put(4191, 43884);
        cosmeticVars.put(11485, 44184);
        cosmeticVars.put(11486, 44185);
        cosmeticVars.put(4388, 44563);
        cosmeticVars.put(37128, 34060);
        cosmeticVars.put(37129, 34060);
        cosmeticVars.put(34206, 45280);
        cosmeticVars.put(11061, 20530);
        cosmeticVars.put(11062, 20530);
        cosmeticVars.put(26415, 34812);
        cosmeticVars.put(26416, 34812);
        cosmeticVars.put(6732, 36442);
        cosmeticVars.put(6733, 36441);
        for (int i = 0; i < 50000; i++) {
            GeneralRequirementMap map = GeneralRequirementMap.getMap(i);
            if (map == null || map.getValues() == null)
                continue;
            int mapId2 = map.getIntValue(4419);
            if (mapId2 != 0)
                map = GeneralRequirementMap.getMap(mapId2);
            if (!map.getValues().containsKey((long) 4744))
                continue;
            int bitValue = map.getIntValue(4744);
            if (bitValue < 0)
                continue;

            int bitShift = bitValue % 32;
            int[] varIds = { 5050, 5051, 5052, 5053, 5054, 6537, 6538, 6762, 6763, 7105, 7106, 7645, 7646, 7849, 7850, 7851, 7852, 8170, 8228, 8353, 8550, 8692, 9014 };
            if (bitValue / 32 >= varIds.length) {
                if (Settings.DEBUG)
                    System.out.println("Unable to get unlocked parent struct " + map.getStringValue(2533) + " with ID " + bitValue + ", index=" + (bitValue / 32));
                continue;
            }
            if (map.getId() == 27598 || map.getId() == 27597)
                continue;
            int varId = varIds[bitValue / 32];
            int varbitId = VarBitDefinitions.getVarbitsForVar(varId)[bitShift];
            if (cosmeticVars.get(i) == null)
                cosmeticVars.put(i, varbitId);
            else {
                List<Integer> vars = new ArrayList<Integer>();
                Object o = cosmeticVars.get(i);
                if (o instanceof Integer)
                    vars.add((Integer) o);
                else {
                    for (Integer var : ((Integer[]) o)) {
                        if (var != null)
                            vars.add(var);
                    }
                }
                if (!vars.contains(varbitId))
                    vars.add(varbitId);
                cosmeticVars.put(i, vars.stream().toArray(Integer[]::new));
            }
        }
    }

}
