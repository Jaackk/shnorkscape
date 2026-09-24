package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.player.Player;
import java.util.Arrays;

/** Backpack menu policy derived from the paired950 client and its authored CS2 scripts. */
public final class Native950InventoryMenu {
    private Native950InventoryMenu() { }
    public static final int INTERFACE = 1473, ITEMS_COMPONENT = 5;
    // Captions remain authored by native CS2; an event bit alone never creates a menu caption.
    public static final int OPERATIONS = 0x7fe; // operation1 through10
    public static final int USE_TARGETS = 2 | 4 | 32; // NPC, object, component item
    public static final int EVENT_MASK = OPERATIONS | (USE_TARGETS << 11)
            | (3 << 18) | (1 << 21) | (1 << 22); // grid -> panel -> backpack root, drag target, Use target

    static int eventsForItem(int id){return EVENT_MASK|(id==962?(8<<11):0);}

    // Script2833's two switch tables bypass2410 for these IDs/categories. Never interpret
    // their operation8 as Drop without deriving the selected specialized script first.
    private static final int[] SPECIAL_IDS = {35,5509,5510,5511,5512,5513,5514,5515,6099,6100,6101,6102,13561,13562,14632,19040,19042,19760,19865,19866,19867,19868,20709,21581,24199,24200,24202,24203,24205,27616,27618,27620,27622,27624,27996,28575,28686,28688,28690,28692,28694,29970,31089,31091,31093,31095,31097,31099,31101,35277,35279,35281,35283,35285,35287,35289,36619,36620,39784,39786,39788,39790,39792,41808,42679,42682,44155,51275,51276,51309,58451};
    private static final int[] SPECIAL_CATEGORIES = {1825,3464,4040,4355,4568,5096,5248,5363};

    /** Reject special native menus before translating operations into destructive actions. */
    public static boolean usesOrdinaryOperations(int itemId) {
        if (itemId < 0 || itemId > 0xffffff || Cache.STORE == null || !Cache.isFlatReadOnly()) return false;
        try {
            byte[] bytes = Cache.STORE.getIndexes()[19].getFile(itemId >>> 8, itemId & 255);
            if (bytes == null) return false;
            ItemDefinitions d = ItemDefinitions.decodeStrict947(itemId, bytes, null);
            if (!ordinary(d)) return false;
            // Note generation may inherit base params. Only ordinary one-level certificates are
            // admitted here; bound/lent/shard templates require their own native menu derivation.
            if (d.lendTemplateId != -1 || d.bindTemplateId != -1 || d.shardTemplateId != -1) return false;
            if (d.certTemplateId == -1) return true;
            int baseId = d.certId;
            if (baseId < 0 || baseId > 0xffffff || baseId == itemId) return false;
            byte[] baseBytes = Cache.STORE.getIndexes()[19].getFile(baseId >>> 8, baseId & 255);
            if (baseBytes == null) return false;
            ItemDefinitions base = ItemDefinitions.decodeStrict947(baseId, baseBytes, null);
            return base.certTemplateId == -1 && base.lendTemplateId == -1
                    && base.bindTemplateId == -1 && base.shardTemplateId == -1 && ordinary(base);
        } catch (RuntimeException invalid) { return false; }
    }

    private static boolean ordinary(ItemDefinitions definition) {
        return usesOrdinaryOperations(definition.getId(), definition.itemCategory,
                definition.getCSOpcode(6799), definition.getCSOpcode(4840));
    }

    /** Pure metadata gate, separately testable without opening the live cache. */
    public static boolean usesOrdinaryOperations(int itemId, int category, int flag6799, int flag4840) {
        return itemId >= 0 && itemId <= 0xffffff && Arrays.binarySearch(SPECIAL_IDS, itemId) < 0
                && Arrays.binarySearch(SPECIAL_CATEGORIES, category) < 0 && flag6799 != 1 && flag4840 != 1;
    }

    /**
     * Equipment-only translation for the actual backpack captions. This must never be used
     * for Drop or other special-menu actions. The selected cache label must itself be Wear,
     * Wield or Equip and the complete current-cache equipment contract must validate.
     */
    public static int equipmentCacheOption(Player player, int itemId, int uiOperation) {
        if (player == null || Cache.STORE == null || !Cache.isFlatReadOnly()) return 0;
        try {
            Native950EquipmentTypes.Type type = Native950EquipmentTypes.resolve(itemId);
            if (type == null) return 0;
            ItemDefinitions definition = Native950CacheItems.definition(itemId);
            // Backpack12090 passes -1 to2833, then18401. With that argument the only
            // Excalibur caption swap is varbit54934 (current-cache varp2180 bit4).
            boolean swapExcalibur = (itemId == 35 || itemId == 14632 || itemId == 36619 || itemId == 36620)
                    && player.getVarsManager().getBitValue(54934) == 1;
            int option = equipmentCacheOption(definition, uiOperation, swapExcalibur);
            return type.isWearOption(option) ? option : 0;
        } catch (RuntimeException malformed) { return 0; }
    }

    /** Pure caption seam: no mutation, no ID/name-based equipment admission. */
    static int equipmentCacheOption(ItemDefinitions definition, int uiOperation, boolean swapExcalibur) {
        if (definition == null || definition.inventoryOptions == null) return 0;
        int script = equipmentMenuScript(definition);
        int option = ordinaryCacheOption(uiOperation);
        switch (script) {
            case 6468: // Cache option2 is the first caption; option1 is the second.
                if (option == 1) option = 2; else if (option == 2) option = 1;
                break;
            case 18401:
                if (swapExcalibur) {
                    if (option == 1) option = 2; else if (option == 2) option = 1;
                }
                break;
            case 7031: // Both quest-state branches preserve only cache2/3/5 here.
                if (option != 2 && option != 3 && option != 5) option = 0;
                break;
            case 2383: case 1520: // Quest/event branches always preserve cache1/2/5.
                if (option != 1 && option != 2 && option != 5) option = 0;
                break;
            case 12405:
                if (option == 4) option = 0;
                break;
            case 13505: // Other captions are dynamic skill names, never cache Wear.
                if (option != 1 && option != 5) option = 0;
                break;
            case 2410: case 16473: //16473 hides all only with argument1; backpack passes-1.
                break;
            default: return 0; // No inferred action for an unreviewed specialized caption.
        }
        return option > 0 && option <= definition.inventoryOptions.length
                && Native950EquipmentTypes.isWearLabel(definition.inventoryOptions[option - 1]) ? option : 0;
    }

    private static int equipmentMenuScript(ItemDefinitions d) {
        // These are native2833 menu branches, not equipment ID allow-lists. Actual equipment
        // capability, models, slots and requirements are supplied by Native950EquipmentTypes.
        switch (d.getId()) {
            case 35: case 14632: case 36619: case 36620: return 18401;
            case 13561: case 13562: case 19760: case 24200: case 24203: case 24199: case 24202:
            case 19867: case 19865: case 19868: case 19866: case 31091: case 31089: case 31101:
            case 31099: case 31095: case 31097: case 31093: case 27624: case 27620: case 27618:
            case 27616: case 27622: case 28686: case 28690: case 28692: case 28694: case 28688:
            case 29970: case 35277: case 35279: case 35281: case 35283: case 35285: case 35287:
            case 35289: case 27996: case 20709: return 6468;
            case 21581: case 28575: return 7031;
            case 42682: return 2383;
            case 44155: return 1520;
            default: if (Arrays.binarySearch(SPECIAL_IDS, d.getId()) >= 0) return 0;
        }
        // Native dispatch precedence is ID switch,6799,4840,category switch,ordinary.
        if (d.getCSOpcode(6799) == 1) return 12405;
        if (d.getCSOpcode(4840) == 1) return 6468;
        switch (d.itemCategory) {
            case 3464: return 6468;
            case 4040: case 4355: case 5248: case 5363: case 5096: return 12405;
            case 1825: return 13505;
            case 4568: return 16473;
            default: return 2410;
        }
    }

    /** Script2410 routes ordinary item cache options to UI operations1,2,3,7,8. */
    public static int ordinaryCacheOption(int uiOperation) {
        switch (uiOperation) {
            case 1: case 2: case 3: return uiOperation;
            case 7: return 4;
            case 8: return 5;
            default: return 0;
        }
    }
}
