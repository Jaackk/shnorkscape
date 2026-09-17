package com.rs.game.player.content;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.rs.cache.loaders.ClientScriptMap;
import com.rs.cache.loaders.GeneralRequirementMap;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.actions.herblore.herbicide.Herbicide;
import com.rs.game.player.content.items.Bonecrusher;
import com.rs.network.packet.PacketRepository;

/**
 * Handles a {@link Player}'s toolbelt.
 *
 * @author Noel.
 */
public class Toolbelt implements Serializable {

    private static final long serialVersionUID = 7706200801855080675L;
    private transient Player player;
    private Map<Integer, Boolean> items;
    private Item[][] newItems;
    private transient boolean dungeoneeringToolbelt;

    public static final int[][][] TOOL_BELT_ITEMS = { { { 1265, 20780, 1267, 20781, 1269, 20782, 1273, 20784, 1271, 20783, 1275, 20785, 29662, 29654, 15259, 20786, 32646, 29522 }, { 1351, 1349, 1353, 1355, 1357, 1359, 6739, 32645 }, { 2347 }, { 1755 }, { 946 }, { 590 }, { 8794 }, { 233 }, { 975, 6313, 6315, 6317 }, { 2575 }, { 2576 }, { 2574 }, { 1735 }, { 10150 }, { 20565 }, { 42453 }, { 42617 }, { 42618 }, { 28 }, { 4446 }, { 47718 }, { 307 }, { 13431 }, { 303 }, { 305 }, { 309 }, { 311 }, { 301 }, { 11323 }, { 1733 }, { 1785 }, { 1595 }, { 11065 }, { 1597 }, { 1592 }, { 5523 }, { 4 }, { 9434 }, { 1599 }, { 1594 }, { 2976 }, { 13153 }, { 7649 }, { 5341 }, { 5343 }, { 952 }, { 5325 }, { 5329, 7409 }, { 18682 }, { 4162 }, { 34960 }, { 34961 }, { 34962 }, { 34963, 34964 }, { 10952 }, { 32644 }, { 21451 }, { 18337 }, { 31188 }, { 27996 }, { 19675 }, { 41375 }, { 36367 }, { 36368 }, { 36389 } },

            { { 16295, 16297, 16299, 16301, 16303, 16305, 16307, 16309, 16311, 16313, 16315 }, { 16361, 16363, 16365, 16367, 16369, 16371, 16373, 16375, 16377, 16379, 16381 }, { 17754 }, { 17883 }, { 17444 }, { 17794 }, { 17446 }, { 17678 } } };

    public Toolbelt(Player player) {
        this.player = player;
        newItems = new Item[2][];
        newItems[0] = new Item[TOOL_BELT_ITEMS[0].length];
        newItems[1] = new Item[TOOL_BELT_ITEMS[1].length];
        addDefaultItems();
    }

    public void setPlayer(Player player) {
        this.player = player;
        if (newItems == null) {
            newItems = new Item[2][];
            newItems[0] = new Item[TOOL_BELT_ITEMS[0].length];
            newItems[1] = new Item[TOOL_BELT_ITEMS[1].length];
            addDefaultItems();
        }
    }

    public void init() {
        refresh();
        addOldItems();
    }

    /** Logged once per JVM when the 947 cache cannot seed the default belt. */
    private static boolean nativeSeedWarned;
    /** Logged once per JVM when an unseeded belt refuses an item instead of eating it. */
    private static boolean nativeAddWarned;

    /**
     * True when the default-tool chain this class needs is usable on the loaded
     * cache: enum 13730 -> struct params 6979/6980 -> enum 2433. It is
     * <b>unverified on the flat 947 cache</b> (M8) and simply absent when no
     * cache is loaded at all (every JUnit run), which is why the check is not
     * {@code Cache.isFlatReadOnly()} alone: a cache-free JVM used to skip the
     * guard entirely and left {@code Player.toolBelt} null, and
     * {@code Inventory.containsItem} / {@code Inventory.containsOneItem}
     * dereference that field with no guard on the first {@code stopAll()}.
     *
     * <p>A legacy 910 cache answers true and every path below is byte-for-byte
     * the behaviour it always had.
     */
    public static boolean defaultToolChainAvailable() {
        if (com.rs.cache.Cache.isFlatReadOnly())
            return false;
        ClientScriptMap map = ClientScriptMap.getMap(13730);
        return map != null && map.getSize() > 0;
    }

    private void addDefaultItems() {
        // The default-tool seeding walks enum 13730 -> struct param 6979 -> struct
        // param 6980 of the loaded cache. That chain is not verified on the flat
        // 947 cache (it is M8 work), so on that cache the belt is left EMPTY
        // instead of throwing: contains() then answers "no tool", which is the
        // fail-closed answer, and every getToolBelt() call site keeps a live
        // object instead of the null this constructor used to leave behind
        // (Inventory.containsItem dereferences it on every stopAll()).
        // The legacy 910 cache takes the original path untouched.
        if (!defaultToolChainAvailable()) {
            if (!nativeSeedWarned) {
                nativeSeedWarned = true;
                System.out.println("[Ataraxia947] Toolbelt default tools are unverified without the legacy"
                        + " enum chain (13730 -> 6979/6980 -> 2433); starting every belt on this cache empty");
            }
            return;
        }
        for (int i = 0; i <= 3; i++) {
            for (Object e : ClientScriptMap.getMap(GeneralRequirementMap.getMap(ClientScriptMap.getMap(13730).getIntValue(i)).getIntValue(6979)).getValues().values()) {
                int baseItemId = GeneralRequirementMap.getMap((int) e).getIntValue(6980);
                int slotId = getSlotId(baseItemId, false);
                newItems[0][slotId] = new Item(baseItemId, 1);
            }
        }
        for (Object e : ClientScriptMap.getMap(GeneralRequirementMap.getMap(5355).getIntValue(6979)).getValues().values()) {
            int baseItemId = GeneralRequirementMap.getMap((int) e).getIntValue(6980);
            int slotId = getSlotId(baseItemId, true);
            newItems[1][slotId] = new Item(baseItemId, 1);
        }
    }

    private void refresh() {
        for (int dung = 0; dung <= 1; dung++) {
            for (int i = 0; i < TOOL_BELT_ITEMS[dung].length; i++) {
                int varbitId = getVarbitId(TOOL_BELT_ITEMS[dung][i][0]);
                if (varbitId == -1)
                    continue;
                player.getPackets().sendConfigByFile(varbitId, newItems[dung][i] != null ? 1 : 0);
                if (TOOL_BELT_ITEMS[dung][i].length > 1 && newItems[dung][i] != null) {
                    int itemId = newItems[dung][i].getId();
                    switch (TOOL_BELT_ITEMS[dung][i][0]) {
                    case 1351:
                        int v = itemId == 32645 ? 7 : itemId == 6739 ? 6 : itemId == 1359 ? 5 : itemId == 1357 ? 4 : itemId == 1355 ? 3 : itemId == 1353 ? 2 : itemId == 1349 ? 1 : 0;
                        player.getPackets().sendConfigByFile(18522, v);
                        break;
                    case 975:
                        v = itemId == 6317 ? 3 : itemId == 6315 ? 2 : itemId == 6313 ? 1 : 0;
                        player.getPackets().sendConfigByFile(4935, v);
                        break;
                    case 5329:
                        player.getPackets().sendConfigByFile(27430, itemId == 7409 ? 1 : 0);
                        break;
                    case 34963:
                        player.getPackets().sendConfigByFile(28225, itemId == 34964 ? 2 : 1);
                        break;
                    case 16295:
                        player.getPackets().sendConfigByFile(3008, ((itemId - 16295) / 2) + 1);
                        break;
                    case 16361:
                        player.getPackets().sendConfigByFile(3009, ((itemId - 16361) / 2) + 1);
                        break;
                    }
                }
            }
        }
        // Slot 0 (pickaxe) is always seeded by addDefaultItems on the legacy cache;
        // on a cache where that chain is unavailable the belt is empty, and this
        // read used to NPE for every belt item except the pickaxe itself.
        if (newItems[0][0] != null) {
            int v = (int) ClientScriptMap.getMap(2433).getKeyForValue(newItems[0][0].getId());
            player.getPackets().sendConfigByFile(43044, v);
        }
        if (player.getInterfaceManager().containsCentralOverlayInterface(1944)) {
            player.getInterfaceManager().removeCentralOverlayInterface();
            openToolBelt();
        }
    }

    public void openToolBelt() {
        dungeoneeringToolbelt = player.getDungeoneeringManager().isInside();
        player.getInterfaceManager().sendCentralOverlayInterface(1944);
        player.getPackets().sendExecuteScript(14097, dungeoneeringToolbelt ? 1 : 0);
        player.getPackets().sendUnlockIComponentOptionSlots(1944, 7, 0, 500, 0, 1, 2);
    }

    public boolean addItem(Item item) {
        return addItem(item, false);
    }

    public boolean addItem(Item item, boolean silent) {
        if (item == null)
            return false;
        // FAIL CLOSED: a belt whose default-tool chain could not be read holds
        // nothing, so it may accept nothing. Every accepting branch below deletes
        // the item from the inventory BEFORE refresh(); on an unseeded belt that
        // destroyed the item and then threw, and the caller
        // (InventoryOptionsHandler.handleItemOption2 -> the router) downgraded the
        // throw to a counted rejection. Returning false instead makes the caller
        // fall through to its normal Wear/Wield handling with the item intact.
        if (!defaultToolChainAvailable()) {
            if (!nativeAddWarned) {
                nativeAddWarned = true;
                System.out.println("[Ataraxia947] Toolbelt refuses every item on this cache:"
                        + " the default-tool chain (enum 13730) is unavailable, so the belt cannot be refreshed");
            }
            return false;
        }
        if (getSlotId(item.getId(), false) == -1 && getSlotId(item.getId(), true) == -1)
            return false;
        if (item.getAttributes() != null) {
            if (!silent)
                player.sendMessage("You can't add that item to your toolbelt.");
            return true;
        }
        int slotId = getSlotId(item.getId(), false);
        boolean dungeoneering = false;
        if (slotId == -1) {
            slotId = getSlotId(item.getId(), true);
            dungeoneering = true;
        }
        if (newItems[dungeoneering ? 1 : 0][slotId] == null) {
            player.getInventory().deleteItem(item);
            newItems[dungeoneering ? 1 : 0][slotId] = new Item(item.getId(), 1);
            if (!silent)
                player.sendMessage("You add the " + item.getName() + " to your toolbelt.");
            refresh();
            return true;
        }
        if (TOOL_BELT_ITEMS[dungeoneering ? 1 : 0][slotId].length == 1) {
            if (!silent)
                player.sendMessage("The " + item.getName() + " is already in your toolbelt.");
            return true;
        }
        if (!dungeoneering && slotId == 0) {
            if (newItems[dungeoneering ? 1 : 0][slotId].getId() != TOOL_BELT_ITEMS[dungeoneering ? 1 : 0][slotId][0]) {
                if (!silent)
                    player.sendMessage("You already have a pickaxe in your toolbelt, You need to remove it from your toolbelt before you can change it.");
                return true;
            }
            player.getInventory().deleteItem(item);
            newItems[dungeoneering ? 1 : 0][slotId] = new Item(item.getId(), 1);
            if (!silent)
                player.sendMessage("You add the " + item.getName() + " to your toolbelt.");
            refresh();
            return true;
        }
        int itemTier = getSlotId(item.getId(), dungeoneering, true);
        int currentTier = getSlotId(newItems[dungeoneering ? 1 : 0][slotId].getId(), dungeoneering, true);
        if (currentTier == itemTier) {
            if (!silent)
                player.sendMessage("The " + item.getName() + " is already in your toolbelt.");
            return true;
        }
        if (currentTier > itemTier) {
            if (!silent)
                player.sendMessage("You already have a better version of " + item.getName() + " in your toolbelt.");
            return true;
        }
        player.getInventory().deleteItem(item);
        newItems[dungeoneering ? 1 : 0][slotId] = new Item(item.getId(), 1);
        if (!silent)
            player.sendMessage("You add the " + item.getName() + " to your toolbelt.");
        refresh();
        return true;
    }

    public boolean contains(int itemId) {
        if (getSlotId(itemId, false) == -1 && getSlotId(itemId, true) == -1)
            return false;
        int slotId = getSlotId(itemId, false);
        boolean dungeoneering = false;
        if (slotId == -1) {
            slotId = getSlotId(itemId, true);
            dungeoneering = true;
        }
        if (newItems[dungeoneering ? 1 : 0][slotId] == null)
            return false;
        return newItems[dungeoneering ? 1 : 0][slotId].getId() == itemId;
    }

    public int getItemsSize(boolean dungeoneering) {
        int size = 0;
        int mapId = ClientScriptMap.getMap(13729).getIntValue(dungeoneering ? 1 : 0);
        for (Object e : ClientScriptMap.getMap(mapId).getValues().values())
            size += ClientScriptMap.getMap(GeneralRequirementMap.getMap((int) e).getIntValue(6979)).getSize();
        return size;
    }

    public int getItemId(int slotId, boolean dungeoneering) {
        return newItems[dungeoneering ? 1 : 0][slotId] != null ? newItems[dungeoneering ? 1 : 0][slotId].getId() : TOOL_BELT_ITEMS[dungeoneering ? 1 : 0][slotId][0];
    }

    public int getSlotId(int itemId, boolean dungeoneering) {
        return getSlotId(itemId, dungeoneering, false);
    }

    public int getSlotId(int itemId, boolean dungeoneering, boolean tier) {
        for (int i = 0; i < TOOL_BELT_ITEMS[dungeoneering ? 1 : 0].length; i++) {
            for (int j = 0; j < TOOL_BELT_ITEMS[dungeoneering ? 1 : 0][i].length; j++) {
                if (TOOL_BELT_ITEMS[dungeoneering ? 1 : 0][i][j] == itemId)
                    return tier ? j : i;
            }
        }
        return -1;
    }

    private void addOldItems() {
        if (items != null && !items.isEmpty()) {
            Iterator<Entry<Integer, Boolean>> itr = items.entrySet().iterator();
            while (itr.hasNext()) {
                Entry<Integer, Boolean> e = itr.next();
                if (e == null || e.getKey() == null || e.getValue() == null) {
                    itr.remove();
                    continue;
                }
                int itemId = e.getKey();
                boolean contains = e.getValue();
                if (!contains || contains(itemId)) {
                    itr.remove();
                    continue;
                }
                player.addItem(new Item(itemId, 1));
                itr.remove();
            }
        }
        if (player.getToolBeltNew() != null && player.getToolBeltNew().getItems() != null && !player.getToolBeltNew().getItems().isEmpty()) {
            Iterator<Entry<Integer, Boolean>> itr = player.getToolBeltNew().getItems().entrySet().iterator();
            while (itr.hasNext()) {
                Entry<Integer, Boolean> e = itr.next();
                if (e == null || e.getKey() == null || e.getValue() == null) {
                    itr.remove();
                    continue;
                }
                int itemId = e.getKey();
                boolean contains = e.getValue();
                if (!contains || contains(itemId)) {
                    itr.remove();
                    continue;
                }
                player.addItem(new Item(itemId, 1));
                itr.remove();
            }
        }
    }

    public static int getVarbitId(int itemId) {
        switch (itemId) {
        case 1351:
            return 2985;
        case 946:
            return 2968;
        case 1735:
            return 2969;
        case 8794:
            return 2988;
        case 4:
            return 2989;
        case 1595:
            return 2970;
        case 9434:
            return 2990;
        case 11065:
            return 2991;
        case 1755:
            return 2971;
        case 1785:
            return 2992;
        case 1599:
            return 2972;
        case 1597:
            return 2973;
        case 1733:
            return 2974;
        case 1592:
            return 2975;
        case 2976:
            return 2993;
        case 5523:
            return 2976;
        case 1594:
            return 2994;
        case 5343:
            return 2995;
        case 5325:
            return 2996;
        case 5341:
            return 2997;
        case 5329:
            return 2998;
        case 18682:
            return 27431;
        case 13431:
            return 2977;
        case 307:
            return 2978;
        case 309:
            return 2979;
        case 311:
            return 2980;
        case 305:
            return 3001;
        case 301:
            return 2981;
        case 303:
            return 2982;
        case 233:
            return 2999;
        case 2347:
            return 2984;
        case 590:
            return 2986;
        case 952:
            return 3000;
        case 975:
            return 3002;
        case 11323:
            return 3003;
        case 2575:
            return 3004;
        case 2576:
            return 3005;
        case 2574:
            return 685;
        case 13153:
            return 3006;
        case 7649:
            return 30998;
        case 10150:
            return 3007;
        case 20565:
            return 28410;
        case 42453:
            return 40075;
        case 42617:
            return 40234;
        case 42618:
            return 40235;
        case 4446:
            return 40074;
        case 28:
            return 42126;
        case 17883:
            return 3010;
        case 17678:
            return 3011;
        case 17794:
            return 3012;
        case 17754:
            return 3013;
        case 17446:
            return 3014;
        case 17444:
            return 3015;
        case 16295:
            return 3008;
        case 16361:
            return 3009;
        case 4162:
            return 28219;
        case 34960:
            return 28220;
        case 34961:
            return 28221;
        case 34962:
            return 28222;
        case 10952:
            return 28223;
        case 32644:
            return 28224;
        case 34963:
            return 28225;
        case 18337:
            return 28226;
        case 31188:
            return 28227;
        case 27996:
            return 28228;
        case 19675:
            return 28229;
        case 41375:
            return 38660;
        case 21451:
            return 28230;
        }
        return -1;
    }

    public void handleButtons(int componentId, int slotId, int slotId2, int packetId) {
        if (componentId == 17)
            player.getPackets().sendExecuteScript(11193);
        else if (componentId == 43) {
            Herbicide.openHerbicide(player);
        }else if (componentId == 73) {
            Bonecrusher.openBonecrusher(player);
        } else if (componentId == 7) {
            if (packetId == PacketRepository.ACTION_BUTTON1_PACKET)
                refreshSelectedTool(slotId);
            else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET) {
                if (slotId != 0 || dungeoneeringToolbelt)
                    return;
                Item pickaxe = newItems[0][0];
                if (pickaxe.getId() == TOOL_BELT_ITEMS[0][slotId][0]) {
                    player.getPackets().sendMainInterfaceMessage(1, "You can't remove the base item.", true);
                    return;
                }
                player.addItem(new Item(pickaxe.getId(), 1));
                newItems[0][0] = new Item(TOOL_BELT_ITEMS[0][slotId][0], 1);
                refresh();
            }
        }
    }

    private void refreshSelectedTool(int slotId) {
        int itemId = getItemId(slotId, dungeoneeringToolbelt);
        boolean upgradeable = TOOL_BELT_ITEMS[dungeoneeringToolbelt ? 1 : 0][slotId].length > 1;
        if (upgradeable) {
            int itemTier = getSlotId(itemId, dungeoneeringToolbelt, true);
            int maxTier = TOOL_BELT_ITEMS[dungeoneeringToolbelt ? 1 : 0][slotId].length - 1;
            if (itemTier == maxTier)
                upgradeable = false;
        }
        int actualItemId = newItems[dungeoneeringToolbelt ? 1 : 0][slotId] != null ? newItems[dungeoneeringToolbelt ? 1 : 0][slotId].getId() : itemId;
        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(actualItemId);
        String vowels = "aeiou";
        boolean vowel = vowels.indexOf(Character.toLowerCase(defs.getName().toLowerCase().charAt(0))) != -1;
        player.getPackets().sendIComponentText(1944, 27, (vowel ? "An" : "A") + " " + defs.getName() + "." + (upgradeable ? ("<br><br>This can be upgraded by right-clicking 'Add to toolbelt' on a better version.") : ""));
    }

}