package com.rs.cache.loaders;
//original 910 server itemdefinitions file
import com.rs.cache.Cache;
import com.rs.cache.filestore.io.InputStream;
import com.rs.cache.filestore.io.OutputStream;
import com.rs.cache.filestore.utils.Constants;
import com.rs.game.item.Item;
import com.rs.game.player.Bank.TabDetails;
import com.rs.game.player.ChargesManagerNew;
import com.rs.game.player.CosmeticsManager;
import com.rs.game.player.Equipment;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.invention.Manufacture;
import com.rs.game.player.actions.invention.Manufacture.ManufactureData;
import com.rs.game.player.content.Combat;
import com.rs.game.player.content.SkillCapeCustomizer;
import com.rs.game.player.content.titles.PlayerTitle;
import com.rs.utils.Logger;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.items.ItemExaminesDataParser;
import lombok.val;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public final class ItemDefinitions {

    private static final ConcurrentHashMap<Integer, ItemDefinitions> itemsDefinitions = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<Integer, ItemDefinitions> getItemsDefinitions() {
        return itemsDefinitions;
    }

    public static void main(String[] args) throws IOException {
        Cache.init();
        System.out.println(ItemDefinitions.getItemDefinitions(48375).bindId);
        int[] colors = ItemDefinitions.getItemDefinitions(20767).originalModelColors;
        System.out.println(Arrays.toString(colors));
        int hslColor = colors[0] & 0xffff;
        int hue = hslColor >> 10 & 63;
        int sat = hslColor >> 7 & 7;
        int lum = hslColor & 127;
        System.out.println(hue + " " + sat + " " + lum);
        System.out.println(colors[0]);
        System.out.println((hue << 10 | sat << 7 | lum) & 0xffff);
        System.out.println(-1019005077 * 2000000000);

    }

    public static int getItemsLength(Item[] items) {
        int count = 0;
        for (Item item : items)
            if (item != null)
                count++;
        return count;
    }

    public static int[] getItemDyes(int itemId) {
        int[] dyes = new int[5];// barrows,shadow,3rd age, blood, ice
        boolean found = false;
        ItemDefinitions odefs = ItemDefinitions.getItemDefinitions(itemId);
        String oname = odefs.getName().toLowerCase();
        if (oname.toLowerCase().contains("(blood") || oname.toLowerCase().contains("(barrows") || oname.toLowerCase().contains("(shadow") || oname.toLowerCase().contains("(third age") || oname.toLowerCase().contains("(ice") || !odefs.isWearItem() || oname.equals("") || odefs.getRepairData() != null || itemId == odefs.getUnchargedItemId() || odefs.getDegradeToDustOriginalItemId() != -1)
            return null;
        for (int i = 33294; i < Utils.getItemDefinitionsSize(); i++) {
            ItemDefinitions defs = ItemDefinitions.getItemDefinitions(i);
            if (!defs.isWearItem() || i == itemId || !defs.getName().contains(" (") || i == defs.getUnchargedItemId())
                continue;
            if (defs.isNoted() || defs.getName().contains("(i)"))
                continue;
            String name = defs.getName().toLowerCase().replace(defs.getName().toLowerCase().substring(defs.getName().toLowerCase().indexOf(" (")), "");
            if (name.equalsIgnoreCase(oname) && defs.getRepairData() == null) {
                int dyeIndex = defs.getName().toLowerCase().contains("(barrows") ? 0 : defs.getName().toLowerCase().contains("(shadow") ? 1 : defs.getName().toLowerCase().contains("(third age") ? 2 : defs.getName().toLowerCase().contains("(blood") ? 3 : defs.getName().toLowerCase().contains("(ice") ? 4 : -1;
                if (dyeIndex == -1)
                    continue;
                dyes[dyeIndex] = i;
                found = true;
            }
        }
        if (found)
            for (int dye : dyes) {
                if (dye == 0) {
//                    System.out.println("couldnt find dyes data for "+odefs.getName());
                    return null;
                }
            }
        return found ? dyes : null;
    }

    public int id;
    public boolean loaded;

    public int baseModel;
    public String name;

    // model size information
    public int modelScale;
    public int modelPitch;
    public int modelRoll;
    public int modelTranslateX;
    public int modelTranslateY;

    // extra information
    public int stackable;
    public int value;
    public boolean membersOnly;

    // wearing model information
    public int maleEquip1;
    public int femaleEquip1;
    public int maleEquip2;
    public int femaleEquip2;

    // options
    public String[] groundOptions;
    public String[] inventoryOptions;

    // model information
    public int[] originalModelColors;
    public int[] modifiedModelColors;
    public short[] originalTextureColors;
    public short[] modifiedTextureColors;
    public byte[] unknownArray1;
    public byte[] unknownArray3;
    public int[] unknownArray2;
    // extra information, not used for newer items
    public boolean unnoted;

    public int maleEquipModelId3;
    public int femaleEquipModelId3;
    public int unknownInt1;
    public int unknownInt2;
    public int unknownInt3;
    public int unknownInt4;
    public int unknownInt5;
    public int unknownInt6;
    public int certId;
    public int certTemplateId;
    public int[] stackIds;
    public int[] stackAmounts;
    public int unknownInt7;
    public int unknownInt8;
    public int unknownInt9;
    public int unknownInt10;
    public int unknownInt11;
    public int teamId;
    public int lendId;
    public int lendTemplateId;
    public int unknownInt12;
    public int unknownInt13;
    public int unknownInt14;
    public int unknownInt15;
    public int unknownInt16;
    public int unknownInt17;
    public int unknownInt18;
    public int unknownInt19;
    public int unknownInt20;
    public int unknownInt21;
    public int unknownInt22;
    public int unknownInt23;
    public int itemCategory;
    public int equipSlot;
    public int equipType;

    // extra added
    public boolean noted;
    public boolean lended;

    public HashMap<Integer, Object> clientScriptData;
    public HashMap<Integer, Integer> itemRequiriments;
    public int[] unknownArray5;
    public int[] unknownArray4;
    public byte[] unknownArray6;

    public byte[] data;
    public int bindTemplateId;
    public int bindId;

    public boolean aBool129;

    public int shardId = -1;
    public int shardTemplateId = -1;
    public int shardCombineRequirement = 0;
    String shardName = "null";
    /** Current-cache shard label used when resolving a shard template. */
    public String getShardName() { return shardName; }

    /*
     * 947 (2024 RS3) item opcodes the 910 table lacks. Widths were confirmed by
     * DefinitionScanProbe against the real cache (see notes/P3-definition-scan.md):
     *   181 = int64. It replaces the 32-bit opcode 12 value (opcode 12 no longer
     *         occurs in the 947 cache; coins/cannonballs carry their shop value here).
     *   3   = string. Examine-style description text (only ~130 items carry it).
     *   69  = int32. Meaning not established; stored verbatim.
     *   178 = flag with no payload. Meaning not established.
     * These fields are only written on the strict 947 path (Cache.isFlatReadOnly()).
     */
    /** Raw 64-bit value from 947 opcode 181; -1 when absent. {@link #value} receives the clamped int. */
    public long value64 = -1L;
    /** Raw int32 payload of 947 opcode 69 (unknown meaning). */
    public int unknownInt69;
    /** Description string from 947 opcode 3; null when absent. */
    public String description;
    /** True when the payload-less 947 opcode 178 is present. */
    public boolean flag178;

    /*
     * 950 ObjType fields that neither the 910 table nor the 947 additions above carry. Every one
     * of them was derived from the 950 client's own ObjType decoder - see the javadoc on
     * readModern950Values() for the address and the method. Written only on the strict modern
     * path (Cache.isFlatReadOnly()).
     */
    /**
     * 950 opcode 9 (and the per-index overrides 46-56): the item's inventory model list.
     * The 950 client keeps ONE model vector; opcode 1 clears it and pushes a single id at index 0,
     * opcode 9 sizes it and fills every entry, and 46-56 overwrite entry 1..11. Index 0 is
     * mirrored into {@link #baseModel} because opcode 1 does not occur anywhere in the 950 cache -
     * every item's model now arrives through opcode 9, so a parser that only knows opcode 1 leaves
     * baseModel at 0 for all 63,414 items.
     */
    public int[] inventoryModels;
    /** 950 opcode 43: int32 payload, meaning undetermined; stored verbatim. 0 when absent. */
    public int unknownInt43;
    /** 950 opcode 131: string payload, meaning undetermined; null when absent. */
    public String unknownString131;
    /** 950 opcode 182: 24-bit payload, meaning undetermined; -1 when absent. */
    public int unknownInt182 = -1;
    /** True when the payload-less 950 opcode 167 is present. */
    public boolean flag167;
    /** Non-null when the strict 947 decode failed; the definition then only holds defaults. */
    public String decodeFailure;

    /** Ids whose 947 cache file exists but could not be decoded strictly (id -> reason). */
    private static final ConcurrentHashMap<Integer, String> DECODE_FAILURES = new ConcurrentHashMap<Integer, String>();

    /** Every 947 item that failed strict decoding so far; never served silently as a normal item. */
    public static Map<Integer, String> getDecodeFailures() {
        return Collections.unmodifiableMap(DECODE_FAILURES);
    }

    /** Reason the given item failed strict 947 decoding, or null if it decoded (or was never requested). */
    public static String getDecodeFailure(int itemId) {
        return DECODE_FAILURES.get(itemId);
    }

    public ItemDefinitions(int id) {
        this(id, true);
    }

    /** load=false builds a defaults-only definition without touching Cache.STORE (probe/test decoding). */
    private ItemDefinitions(int id, boolean load) {
        this.id = id;
        setDefaultsVariableValues();
        setDefaultOptions();
        if (load)
            loadItemDefinitions();
    }

    /**
     * Strict 947 decode of a raw index-19 file: every opcode must be known and the
     * terminator must be the last byte, otherwise an IllegalArgumentException is
     * thrown ("... opcode N at offset" / "Trailing bytes ..."). Note/lend/bind
     * templates are not resolved here because that needs other items from the cache.
     * When trace is non-null every opcode is appended as "opcode@offset ".
     */
    public static ItemDefinitions decodeStrict947(int id, byte[] data, StringBuilder trace) {
        ItemDefinitions def = new ItemDefinitions(id, false);
        def.readModernOpcodeValues(new com.rs.network.io.InputStream(data, true), trace);
        def.loaded = true;
        return def;
    }

    /** Legacy 910 decode of raw bytes through the tolerant filestore stream (tests only). */
    static ItemDefinitions decodeLegacy(int id, byte[] data) {
        ItemDefinitions def = new ItemDefinitions(id, false);
        def.readOpcodeValues(new InputStream(data));
        def.loaded = true;
        return def;
    }

    private static final Comparator<Item> comparator = (o1, o2) -> Integer.compare(o2.getDefinitions().value, o1.getDefinitions().value);

    public static Comparator<Item> compartor() {
        return comparator;
    }

    public static final ItemDefinitions getItemDefinitions(int itemId) {
        ItemDefinitions def = itemsDefinitions.get(itemId);
        if (def == null)
            itemsDefinitions.put(itemId, def = new ItemDefinitions(itemId));
        return def;
    }

    public static final void clearItemsDefinitions() {
        itemsDefinitions.clear();
        DECODE_FAILURES.clear();
    }

    public static int getEquipType(String name) {
        if (name.contains("sword") || name.contains("dagger") || name.contains("scimitar") || name.contains("whip") || name.contains("spear") || name.contains("mace") || name.contains("battleaxe") || name.contains("staff") || name.contains("Staff") || name.contains("battleaxe") || name.contains("hatchet") || name.contains("pickaxe") || name.contains("axe") || name.contains("wand") || name.contains("katana") || name.contains("Katana") || name.contains("widow") || name.contains("scythe") || name.contains("maul")) {
            return 11;
        }
        if (name.contains("plate") || name.contains("body") || name.contains("apron") || name.contains("chest") || name.contains("top")) {
            return 3;
        }
        if (name.contains("gloves")) {
            return 5;
        }
        if (name.contains("boots")) {
            return 6;
        }
        if (name.contains("skirt") || name.contains("legs") || name.contains("bottom")) {
            return 4;
        }
        if (name.contains("helm") || name.contains("hat") || name.contains("hood")) {
            return 0;
        }
        if (name.contains("shield") || name.contains("ket-xil") || name.equalsIgnoreCase("book") || name.contains("defender") || name.contains("teddy") || name.contains("vyre'lector")) {
            return 9;
        }
        if (name.contains("cape") || name.contains("Cape")) {
            return 1;
        }
        if (name.contains("neck")) {
            return 2;
        }
        if (name.contains("arrow") || name.contains("bolt")) {
            return 7;
        }
        if (name.contains("bow")) {
            return 12;
        }
        return -1;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public final void loadItemDefinitions() {
        com.rs.cache.filestore.store.Index index = Cache.STORE.getIndexes()[Constants.ITEM_DEFINITIONS_INDEX];
        byte[] data = null;

        // 1. Safely check if the index exists before requesting the file
        if (index != null && index.getTable() != null && index.getTable().getArchives() != null) {
            try {
                data = index.getFile(getArchiveId(), getFileId());
            } catch (Exception e) {
                data = null;
            }
        }

        // 2. Read the data if we have it, otherwise apply a safe fallback
        if (data != null) {
            if (Cache.isFlatReadOnly()) {
                // 947: strict stream, unknown opcodes and trailing bytes abort the definition
                // instead of decoding through EOF zeroes as silent garbage.
                try {
                    readModernOpcodeValues(new com.rs.network.io.InputStream(data, true), null);
                } catch (RuntimeException failure) {
                    markDecodeFailure(failure);
                }
            } else {
                readOpcodeValues(new InputStream(data));
            }
        } else {
            // Prevent shop/drop NPEs by ensuring the name is never truly null
            if (this.name == null) {
                this.name = "Missing Item (" + id + ")";
            }
        }

        if (this.name == null) {
            this.name = "null";
        }

        // === FORCE STACKABLE ITEMS ===
        /*if (id == 1511 || id == 1521 || id == 1519 || id == 1517 || id == 1515 || id == 1513 || id == 29556 ||
                id == 434 || id == 436 || id == 438 || id == 440 || id == 442 || id == 444 || id == 447 || id == 449 || id == 451 || id == 453 ||
                id == 2349 || id == 2351 || id == 2353 || id == 2355 || id == 2357 || id == 2359 || id == 2361 || id == 2363 ||
                id == 1761 || id == 1737 || id == 1739 || id == 1741 || id == 1743 || id == 1759 ||
                id == 1745 || id == 1747 || id == 1749 || id == 1751 || id == 1753 || id == 2505 || id == 2507 || id == 2509 ||
                id == 1617 || id == 1619 || id == 1621 || id == 1623 || id == 1625 || id == 1627 || id == 1629 || id == 1631 ||
                id == 317 || id == 321 || id == 327 || id == 331 || id == 335 || id == 341 || id == 345 || id == 349 || id == 353 ||
                id == 359 || id == 361 || id == 363 || id == 371 || id == 377 || id == 7944 || id == 383 || id == 389 || id == 395 ||
                id == 15270 || id == 407 ||
                id == 199 || id == 201 || id == 203 || id == 205 || id == 207 || id == 209 || id == 211 || id == 213 || id == 215 ||
                id == 217 || id == 219 || id == 2485 || id == 3051 ||
                id == 249 || id == 251 || id == 253 || id == 255 || id == 257 || id == 259 || id == 261 || id == 263 || id == 265 ||
                id == 267 || id == 269 || id == 3000 || id == 3002 ||
                id == 526 || id == 528 || id == 530 || id == 532 || id == 534 || id == 536 || id == 3125 || id == 3123 || id == 4830 ||
                id == 6729 || id == 6812 || id == 10976 || id == 13713 || id == 13714 || id == 13715 || id == 13716 || id == 13717 || id == 44844 || id == 1777) {

            setStackable(true);
        }*/
        // === END FORCE STACKABLE ITEMS ===

        if (certTemplateId != -1)
            toNote();
        if (lendTemplateId != -1)
            toLend();
        if (bindTemplateId != -1)
            toBind();
        if (shardTemplateId != -1)
            toShard();
        loaded = decodeFailure == null;
    }

    /**
     * Records a strict 947 decode failure: the partially decoded fields are thrown
     * away (no misaligned garbage survives), the name marks the item as undecodable
     * and the id is kept in the static registry for DefinitionScanProbe / callers.
     */
    private void markDecodeFailure(RuntimeException failure) {
        String reason = failure.getMessage() == null ? failure.toString() : failure.getMessage();
        resetDecodedFields();
        name = "Undecodable Item (" + id + ")";
        decodeFailure = reason;
        if (DECODE_FAILURES.put(id, reason) == null)
            Logger.getGlobal().warn("947 item " + id + " rejected by strict decoder: " + reason);
    }

    /** Restores every field a partial decode may have touched to its constructor default. */
    private void resetDecodedFields() {
        setDefaultsVariableValues();
        setDefaultOptions();
        baseModel = 0;
        modelPitch = 0;
        modelRoll = 0;
        modelTranslateX = 0;
        modelTranslateY = 0;
        stackable = 0;
        membersOnly = false;
        unnoted = false;
        itemCategory = 0;
        originalModelColors = null;
        modifiedModelColors = null;
        originalTextureColors = null;
        modifiedTextureColors = null;
        unknownArray1 = null;
        unknownArray2 = null;
        unknownArray3 = null;
        unknownArray4 = null;
        unknownArray5 = null;
        unknownArray6 = null;
        stackIds = null;
        stackAmounts = null;
        clientScriptData = null;
        itemRequiriments = null;
        unknownInt1 = unknownInt2 = unknownInt3 = unknownInt4 = unknownInt5 = unknownInt6 = 0;
        unknownInt7 = unknownInt8 = unknownInt10 = unknownInt11 = 0;
        unknownInt12 = unknownInt13 = unknownInt14 = unknownInt15 = unknownInt16 = unknownInt17 = 0;
        unknownInt18 = unknownInt19 = unknownInt20 = unknownInt21 = unknownInt22 = unknownInt23 = 0;
        shardId = -1;
        shardTemplateId = -1;
        shardCombineRequirement = 0;
        shardName = "null";
        value64 = -1L;
        unknownInt69 = 0;
        description = null;
        flag178 = false;
        inventoryModels = null;
        unknownInt43 = 0;
        unknownString131 = null;
        unknownInt182 = -1;
        flag167 = false;
    }



    public byte[] getData() {
        return data;
    }

    public void toNote() {
        // ItemDefinitions noteItem; //certTemplateId
        ItemDefinitions realItem = getItemDefinitions(certId);
        membersOnly = realItem.membersOnly;
        value = realItem.value;
        name = realItem.name;
        stackable = 1;
        noted = true;
        clientScriptData = realItem.clientScriptData;
    }

    public void toBind() {
        // ItemDefinitions lendItem; //lendTemplateId
        ItemDefinitions realItem = getItemDefinitions(bindId);
        originalModelColors = realItem.originalModelColors;
        maleEquipModelId3 = realItem.maleEquipModelId3;
        femaleEquipModelId3 = realItem.femaleEquipModelId3;
        teamId = realItem.teamId;
        value = 0;
        membersOnly = realItem.membersOnly;
        name = realItem.name;
        inventoryOptions = new String[5];
        groundOptions = realItem.groundOptions;
        if (realItem.inventoryOptions != null)
            for (int optionIndex = 0; optionIndex < 4; optionIndex++)
                inventoryOptions[optionIndex] = realItem.inventoryOptions[optionIndex];
        inventoryOptions[4] = "Destroy";
        maleEquip1 = realItem.maleEquip1;
        maleEquip2 = realItem.maleEquip2;
        femaleEquip1 = realItem.femaleEquip1;
        femaleEquip2 = realItem.femaleEquip2;
        clientScriptData = realItem.clientScriptData;
        equipSlot = realItem.equipSlot;
        equipType = realItem.equipType;
        equipLookHideSlot2 = realItem.equipLookHideSlot2;
    }

    public void toLend() {
        // ItemDefinitions lendItem; //lendTemplateId
        ItemDefinitions realItem = getItemDefinitions(lendId);
        originalModelColors = realItem.originalModelColors;
        maleEquipModelId3 = realItem.maleEquipModelId3;
        femaleEquipModelId3 = realItem.femaleEquipModelId3;
        teamId = realItem.teamId;
        value = 0;
        membersOnly = realItem.membersOnly;
        name = realItem.name;
        inventoryOptions = new String[5];
        groundOptions = realItem.groundOptions;
        if (realItem.inventoryOptions != null)
            for (int optionIndex = 0; optionIndex < 4; optionIndex++)
                inventoryOptions[optionIndex] = realItem.inventoryOptions[optionIndex];
        inventoryOptions[4] = "Discard";
        maleEquip1 = realItem.maleEquip1;
        maleEquip2 = realItem.maleEquip2;
        femaleEquip1 = realItem.femaleEquip1;
        femaleEquip2 = realItem.femaleEquip2;
        clientScriptData = realItem.clientScriptData;
        equipSlot = realItem.equipSlot;
        equipType = realItem.equipType;
        lended = true;
    }

    public void toShard() {
        ItemDefinitions realItem = getItemDefinitions(shardId);
        membersOnly = realItem.membersOnly;
        value = (int) Math.floor(realItem.value / realItem.shardCombineRequirement);
        name = realItem.shardName;
        stackable = 1;
        inventoryOptions = new String[5];
        inventoryOptions[0] = "Combine";
        inventoryOptions[4] = "Drop";
    }

    public int getArchiveId() {
        return getId() >>> 8;
    }

    public int getFileId() {
        return 0xff & getId();
    }

    public boolean isDestroyItem() {
        if (inventoryOptions == null)
            return false;
        for (String option : inventoryOptions) {
            if (option == null)
                continue;
            if (option.equalsIgnoreCase("destroy"))
                return true;
        }
        return false;
    }

    public boolean containsOption(int i, String option) {
        if (inventoryOptions == null || inventoryOptions.length <= i || inventoryOptions[i] == null)
            return false;
        return inventoryOptions[i].equals(option);
    }

    public boolean containsOption(String option) {
        if (inventoryOptions == null)
            return false;
        for (String o : inventoryOptions) {
            if (o == null || !o.equals(option))
                continue;
            return true;
        }
        return false;
    }

    public boolean isWearItem() {
        return equipSlot != -1;
    }

    public boolean isWearItem(boolean male) {
        if (equipSlot < Equipment.SLOT_RING && (male ? getMaleWornModelId1() == -1 : getFemaleWornModelId1() == -1))
            return false;
        return equipSlot != -1;
    }

    public boolean containsInventoryOption(int i, String option) {
        if (inventoryOptions == null || inventoryOptions[i] == null || inventoryOptions.length <= i)
            return false;
        return inventoryOptions[i].equals(option);
    }

    public int getStageOnDeath() {
        if (clientScriptData == null)
            return 0;
        Object protectedOnDeath = clientScriptData.get(1397);
        if (protectedOnDeath != null && protectedOnDeath instanceof Integer)
            return (Integer) protectedOnDeath;
        return 0;
    }

    public boolean hasSpecialBar() {
        if (clientScriptData == null)
            return false;
        Object specialBar = clientScriptData.get(4334);
        return specialBar != null && specialBar instanceof String;
    }

    public int getAttackSpeed() {
        return getCSOpcode(14, 4);
    }

    public int getPrayerBonus() {
        if (id == 41069)
            return 4;
        if (id == 48482)
            return 2;
        if (id == 48483)
            return 4;
        if (id == 48484)
            return 3;
        if (id == 36027)
            return 1;
        if (id == 36028)
            return 1;
        if (id == 31603)
            return 4;
        if (getName().equalsIgnoreCase("grim reaper hood"))
            return 5;
        if (clientScriptData == null)
            return 0;
        Object value = clientScriptData.get(2946);
        if (value != null && value instanceof Integer)
            return (int) value;
        return 0;
    }

    public int getRenderAnimId() {
        if (id == 1419)
            return 1284;
        if (clientScriptData == null)
            return 1284;
        Object animId = clientScriptData.get(644);
        if (animId != null && animId instanceof Integer)
            return (Integer) animId;
        return 1284;
    }

    public int getModelZoom() {
        return modelScale;
    }

    public int getModelOffset1() {
        return modelTranslateX;
    }

    public int getModelOffset2() {
        return modelTranslateY;
    }

    private void setItemNoRequirements() {
        for (int skill = 0; skill <= Skills.INVENTION; skill++) {
            itemRequiriments.put(skill, 0);
        }

    }

    public HashMap<Integer, Integer> getWearingSkillRequiriments() {
        if (clientScriptData == null)
            return null;
        if (itemRequiriments == null) {
            HashMap<Integer, Integer> skills = new HashMap<Integer, Integer>();
            for (int i = 0; i < 10; i++) {
                Integer skill = (Integer) clientScriptData.get(749 + (i * 2));
                if (skill != null) {
                    Integer level = (Integer) clientScriptData.get(750 + (i * 2));
                    if (level != null)
                        skills.put(skill, level);
                }
            }
            Integer maxedSkill = (Integer) clientScriptData.get(277);
            if (maxedSkill != null)
                skills.put(maxedSkill, getId() == 19709 ? 120 : 99);
            itemRequiriments = skills;

            if (name.toLowerCase().contains("berserker helm"))
                itemRequiriments.put(Skills.DEFENCE, 45);
            else if (name.toLowerCase().contains("widow's"))
                setItemNoRequirements();
            else if (name.toLowerCase().contains("helm of neitiznot"))
                itemRequiriments.put(Skills.DEFENCE, 55);
            else if (name.toLowerCase().startsWith("body "))
                itemRequiriments.put(Skills.DEFENCE, 33);
            else if (name.toLowerCase().startsWith("cosmic "))
                itemRequiriments.put(Skills.DEFENCE, 40);
            else if (name.toLowerCase().startsWith("chaos "))
                itemRequiriments.put(Skills.DEFENCE, 50);
            else if (name.toLowerCase().startsWith("iron plate"))
                itemRequiriments.put(Skills.DEFENCE, 1);
            else if (name.toLowerCase().startsWith("iron chain"))
                itemRequiriments.put(Skills.DEFENCE, 1);
            else if (name.toLowerCase().startsWith("iron kite"))
                itemRequiriments.put(Skills.DEFENCE, 1);
            else if (name.toLowerCase().startsWith("iron sq"))
                itemRequiriments.put(Skills.DEFENCE, 1);
            else if (name.toLowerCase().startsWith("steel plate"))
                itemRequiriments.put(Skills.DEFENCE, 5);
            else if (name.toLowerCase().startsWith("steel chain"))
                itemRequiriments.put(Skills.DEFENCE, 5);
            else if (name.toLowerCase().startsWith("steel kite"))
                itemRequiriments.put(Skills.DEFENCE, 5);
            else if (name.toLowerCase().startsWith("steel sq"))
                itemRequiriments.put(Skills.DEFENCE, 5);
            else if (name.toLowerCase().startsWith("black plate"))
                itemRequiriments.put(Skills.DEFENCE, 10);
            else if (name.toLowerCase().startsWith("black chain"))
                itemRequiriments.put(Skills.DEFENCE, 10);
            else if (name.toLowerCase().startsWith("black kite"))
                itemRequiriments.put(Skills.DEFENCE, 10);
            else if (name.toLowerCase().startsWith("black sq"))
                itemRequiriments.put(Skills.DEFENCE, 10);
            else if (name.toLowerCase().startsWith("mithril plate"))
                itemRequiriments.put(Skills.DEFENCE, 20);
            else if (name.toLowerCase().startsWith("mithril chain"))
                itemRequiriments.put(Skills.DEFENCE, 20);
            else if (name.toLowerCase().startsWith("mithril kite"))
                itemRequiriments.put(Skills.DEFENCE, 20);
            else if (name.toLowerCase().startsWith("mithril sq"))
                itemRequiriments.put(Skills.DEFENCE, 20);
            else if (name.toLowerCase().startsWith("adamant plate"))
                itemRequiriments.put(Skills.DEFENCE, 30);
            else if (name.toLowerCase().startsWith("adamant chain"))
                itemRequiriments.put(Skills.DEFENCE, 30);
            else if (name.toLowerCase().startsWith("adamant kite"))
                itemRequiriments.put(Skills.DEFENCE, 30);
            else if (name.toLowerCase().startsWith("adamant sq"))
                itemRequiriments.put(Skills.DEFENCE, 30);
            else if (name.toLowerCase().startsWith("rune plate"))
                itemRequiriments.put(Skills.DEFENCE, 40);
            else if (name.toLowerCase().startsWith("rune chain"))
                itemRequiriments.put(Skills.DEFENCE, 40);
            else if (name.toLowerCase().startsWith("rune kite"))
                itemRequiriments.put(Skills.DEFENCE, 40);
            else if (name.toLowerCase().startsWith("rune sq"))
                itemRequiriments.put(Skills.DEFENCE, 40);
            else if (name.toLowerCase().startsWith("green dragonh"))
                itemRequiriments.put(Skills.RANGE, 40);
            else if (name.toLowerCase().startsWith("blue dragonh"))
                itemRequiriments.put(Skills.RANGE, 50);
            else if (name.toLowerCase().startsWith("red dragonh"))
                itemRequiriments.put(Skills.RANGE, 60);
            else if (name.toLowerCase().startsWith("black dragonh"))
                itemRequiriments.put(Skills.RANGE, 70);
            else if (name.toLowerCase().startsWith("royal dragonh"))
                itemRequiriments.put(Skills.RANGE, 80);
            else if (name.toLowerCase().startsWith("pernix"))
                itemRequiriments.put(Skills.RANGE, 80);
            else if (name.toLowerCase().startsWith("superior morrigan"))
                itemRequiriments.put(Skills.RANGE, 88);
            else if (name.toLowerCase().startsWith("sirenic hau"))
                itemRequiriments.put(Skills.RANGE, 90);
            else if (name.toLowerCase().startsWith("karil's"))
                itemRequiriments.put(Skills.RANGE, 70);
            else if (name.equalsIgnoreCase("dominion sword"))
                itemRequiriments.put(Skills.ATTACK, 90);
            else if (name.equalsIgnoreCase("dominion crossbow"))
                itemRequiriments.put(Skills.RANGE, 90);
            else if (name.equalsIgnoreCase("dominion staff"))
                itemRequiriments.put(Skills.MAGIC, 90);
            else if (name.equalsIgnoreCase("retro divination cape"))
                itemRequiriments.put(Skills.DIVINATION, 99);

            switch (getId()) {
            case 1161:
                itemRequiriments.put(Skills.DEFENCE, 30);
                break;
            case 21371:
            case 21372:
            case 21373:
            case 21374:
            case 21375:
                itemRequiriments.put(Skills.ATTACK, 75);
                break;
            case 10887:
                itemRequiriments.put(Skills.PRAYER, 51);
                break;
            case 7460:
                itemRequiriments.put(Skills.DEFENCE, 13);
                break;
            case 7461:
                itemRequiriments.put(Skills.DEFENCE, 35);
                break;
            case 7462:
                itemRequiriments.put(Skills.DEFENCE, 35);
                break;
            case 12674:
            case 12675:
                itemRequiriments.put(Skills.DEFENCE, 45);
                break;
            case 12680:
            case 12681:
                itemRequiriments.put(Skills.DEFENCE, 55);
                break;
            case 10828:
                itemRequiriments.put(Skills.CONSTRUCTION, 20);
                itemRequiriments.put(Skills.WOODCUTTING, 54);
                itemRequiriments.put(Skills.CRAFTING, 46);
                itemRequiriments.put(Skills.AGILITY, 40);
                break;
            case 2412:
            case 2413:
            case 2414:
                itemRequiriments.put(Skills.MAGIC, 60);
                break;
            case 19784:
            case 22401:
            case 19780: // Korasi
                itemRequiriments.put(Skills.ATTACK, 78);
                itemRequiriments.put(Skills.STRENGTH, 78);
                itemRequiriments.put(Skills.MAGIC, 80);
                itemRequiriments.put(Skills.DEFENCE, 10);
                itemRequiriments.put(Skills.SUMMONING, 55);
                break;
            case 20822:
            case 20823:
            case 20824:
            case 20825:
            case 20826:
                itemRequiriments.put(Skills.DEFENCE, 99);
                break;
            case 1377:
            case 1434:
                itemRequiriments.put(Skills.DEFENCE, 28);
                break;
            case 8846:
                itemRequiriments.put(0, 5);
                itemRequiriments.put(1, 5);
                break;
            case 8847:
                itemRequiriments.put(Skills.ATTACK, 10);
                itemRequiriments.put(Skills.DEFENCE, 10);
                break;
            case 8848:
                itemRequiriments.put(Skills.ATTACK, 20);
                itemRequiriments.put(Skills.DEFENCE, 20);
                break;
            case 8849:
                itemRequiriments.put(Skills.ATTACK, 30);
                itemRequiriments.put(Skills.DEFENCE, 30);
                break;
            case 8850:
                itemRequiriments.put(Skills.ATTACK, 40);
                itemRequiriments.put(Skills.DEFENCE, 40);
                break;
            case 20072:
                itemRequiriments.put(Skills.ATTACK, 60);
                itemRequiriments.put(Skills.DEFENCE, 60);
                break;
            case 8839:
            case 8840:
            case 8841:
            case 8842:
            case 11663:
            case 11664:
            case 11665:
            case 11674:
            case 11675:
            case 11676:
                itemRequiriments.put(Skills.DEFENCE, 42);
                itemRequiriments.put(Skills.HITPOINTS, 42);
                itemRequiriments.put(Skills.RANGE, 42);
                itemRequiriments.put(Skills.ATTACK, 42);
                itemRequiriments.put(Skills.MAGIC, 42);
                itemRequiriments.put(Skills.STRENGTH, 42);
                break;
            case 19785:
            case 19786:
            case 19787:
            case 19788:
            case 19789:
            case 19790:
                itemRequiriments.put(Skills.ATTACK, 78);
                itemRequiriments.put(Skills.STRENGTH, 78);
                itemRequiriments.put(Skills.MAGIC, 80);
                itemRequiriments.put(Skills.HITPOINTS, 42);
                itemRequiriments.put(Skills.RANGE, 42);
                itemRequiriments.put(Skills.PRAYER, 22);
                break;
            }
        }

        return itemRequiriments;
    }

    public void setDefaultOptions() {
        groundOptions = new String[] { null, null, "take", null, null };
        inventoryOptions = new String[] { null, null, null, null, "drop" };
    }

    public void setDefaultsVariableValues() {
        name = "null";
        maleEquip1 = -1;
        maleEquip2 = -1;
        femaleEquip1 = -1;
        femaleEquip2 = -1;
        modelScale = 2000;
        lendId = -1;
        lendTemplateId = -1;
        certId = -1;
        certTemplateId = -1;
        unknownInt9 = 128;
        value = 1;
        maleEquipModelId3 = -1;
        femaleEquipModelId3 = -1;
        bindTemplateId = -1;
        bindId = -1;
        teamId = -1;
        equipSlot = -1;
        equipType = -1;
        equipLookHideSlot2 = -1;
    }

    public OutputStream writeValues() {
        OutputStream stream = new OutputStream();

        if (baseModel != 0) {
            stream.writeByte(1);
            stream.writeBigSmart(baseModel);
        }

        if (!name.equals("null") && certTemplateId == -1) {
            stream.writeByte(2);
            stream.writeString(name);
        }

        if (modelScale != 2000) {
            stream.writeByte(4);
            stream.writeShort(modelScale);
        }

        if (modelPitch != 0) {
            stream.writeByte(5);
            stream.writeShort(modelPitch);
        }

        if (modelRoll != 0) {
            stream.writeByte(6);
            stream.writeShort(modelRoll);
        }

        if (modelTranslateX != 0) {
            stream.writeByte(7);
            stream.writeShort(modelTranslateX);
            if (modelTranslateX < 0)
                modelTranslateX += 65536;
        }

        if (modelTranslateX != 0) {
            stream.writeByte(8);
            stream.writeShort(modelTranslateY);
            if (modelTranslateY < 0)
                modelTranslateY += 65536;
        }

        if (stackable > 0 && certId == -1) {
            stream.writeByte(11);
        }

        if (value != 1 && lendId == -1) {
            stream.writeByte(12);
            stream.writeInt(value);
        }

        if (equipSlot != -1) {
            stream.writeByte(13);
            stream.writeByte(equipSlot);
        }

        if (equipType != -1) {
            stream.writeByte(14);
            stream.writeByte(equipType);
        }

        if (membersOnly && certId == -1) {
            stream.writeByte(16);
        }

        if (maleEquip1 != -1) {
            stream.writeByte(23);
            stream.writeBigSmart(maleEquip1);
        }

        if (maleEquip2 != -1) {
            stream.writeByte(24);
            stream.writeBigSmart(maleEquip2);
        }

        if (femaleEquip1 != -1) {
            stream.writeByte(25);
            stream.writeBigSmart(femaleEquip1);
        }

        if (femaleEquip2 != -1) {
            stream.writeByte(26);
            stream.writeBigSmart(femaleEquip2);
        }

        if (equipLookHideSlot2 != -1) {
            stream.writeByte(27);
            stream.writeByte(equipLookHideSlot2);
        }

        for (int index = 0; index < groundOptions.length; index++) {
            if (groundOptions[index] == null || (index == 2 && groundOptions[index].equals("take")))
                continue;
            stream.writeByte(30 + index);
            stream.writeString(groundOptions[index]);
        }

        for (int index = 0; index < inventoryOptions.length; index++) {
            if (inventoryOptions[index] == null || (index == 4 && inventoryOptions[index].equals("drop")))
                continue;
            stream.writeByte(35 + index);
            stream.writeString(inventoryOptions[index]);
        }

        if (originalModelColors != null && modifiedModelColors != null) {
            stream.writeByte(40);
            stream.writeByte(originalModelColors.length);
            for (int index = 0; index < originalModelColors.length; index++) {
                stream.writeShort(originalModelColors[index]);
                stream.writeShort(modifiedModelColors[index]);
            }
        }

        if (originalTextureColors != null && modifiedTextureColors != null) {
            stream.writeByte(41);
            stream.writeByte(originalTextureColors.length);
            for (int index = 0; index < originalTextureColors.length; index++) {
                stream.writeShort(originalTextureColors[index]);
                stream.writeShort(modifiedTextureColors[index]);
            }
        }

        if (unknownArray1 != null) {
            stream.writeByte(42);
            stream.writeByte(unknownArray1.length);
            for (int index = 0; index < unknownArray1.length; index++) {
                stream.writeByte(unknownArray1[index]);
            }
        }

        if (unknownArray3 != null) {
            stream.writeByte(44);
            stream.writeByte(unknownArray3.length);
        }

        if (unknownArray6 != null) {
            stream.writeByte(45);
            stream.writeByte(unknownArray6.length);
        }

        if (unnoted)
            stream.writeByte(65);

        if (maleEquipModelId3 != -1) {
            stream.writeByte(78);
            stream.writeBigSmart(maleEquipModelId3);
        }

        if (femaleEquipModelId3 != -1) {
            stream.writeByte(79);
            stream.writeBigSmart(femaleEquipModelId3);
        }

        if (unknownInt1 != 0) {
            stream.writeByte(90);
            stream.writeBigSmart(unknownInt1);
        }

        if (unknownInt2 != 0) {
            stream.writeByte(91);
            stream.writeBigSmart(unknownInt2);
        }

        if (unknownInt3 != 0) {
            stream.writeByte(92);
            stream.writeBigSmart(unknownInt3);
        }

        if (unknownInt4 != 0) {
            stream.writeByte(93);
            stream.writeBigSmart(unknownInt4);
        }

        if (itemCategory != 0) {
            stream.writeByte(94);
            stream.writeShort(itemCategory);
        }

        if (unknownInt5 != 0) {
            stream.writeByte(95);
            stream.writeShort(unknownInt5);
        }

        if (unknownInt6 != 0) {
            stream.writeByte(96);
            stream.writeByte(unknownInt6);
        }

        if (certId != -1) {
            stream.writeByte(97);
            stream.writeShort(certId);
        }

        if (certTemplateId != -1) {
            stream.writeByte(98);
            stream.writeShort(certTemplateId);
        }

        if (stackIds != null && stackAmounts != null) {
            for (int index = 0; index < stackIds.length; index++) {
                if (stackIds[index] == 0 && stackAmounts[index] == 0)
                    continue;
                stream.writeByte(100 + index);
                stream.writeShort(stackIds[index]);
                stream.writeShort(stackAmounts[index]);
            }
        }

        if (unknownInt7 != 0) {
            stream.writeByte(110);
            stream.writeShort(unknownInt7);
        }

        if (unknownInt8 != 0) {
            stream.writeByte(111);
            stream.writeShort(unknownInt8);
        }

        if (unknownInt9 != 0) {
            stream.writeByte(112);
            stream.writeShort(unknownInt9);
        }

        if (unknownInt10 != 0) {
            stream.writeByte(113);
            stream.writeByte(unknownInt10);
        }

        if (unknownInt11 != 0) {
            stream.writeByte(114);
            stream.writeByte(unknownInt11 / 5);
        }

        if (teamId != -1) {
            stream.writeByte(115);
            stream.writeByte(teamId);
        }

        if (lendId != -1) {
            stream.writeByte(121);
            stream.writeShort(lendId);
        }

        if (lendTemplateId != -1) {
            stream.writeByte(122);
            stream.writeShort(lendTemplateId);
        }

        if (unknownInt12 != 0 && unknownInt13 != 0 && unknownInt14 != 0) {
            stream.writeByte(125);
            stream.writeByte(unknownInt12 >> 2);
            stream.writeByte(unknownInt13 >> 2);
            stream.writeByte(unknownInt14 >> 2);
        }

        if (unknownInt15 != 0 && unknownInt16 != 0 && unknownInt17 != 0) {
            stream.writeByte(126);
            stream.writeByte(unknownInt15 >> 2);
            stream.writeByte(unknownInt16 >> 2);
            stream.writeByte(unknownInt17 >> 2);
        }

        if (unknownInt18 != 0 && unknownInt19 != 0) {
            stream.writeByte(127);
            stream.writeByte(unknownInt18);
            stream.writeShort(unknownInt19);
        }

        if (unknownInt20 != 0 && unknownInt21 != 0) {
            stream.writeByte(128);
            stream.writeByte(unknownInt20);
            stream.writeShort(unknownInt21);
        }

        if (unknownInt20 != 0 && unknownInt21 != 0) {
            stream.writeByte(129);
            stream.writeByte(unknownInt20);
            stream.writeShort(unknownInt21);
        }

        if (unknownInt22 != 0 && unknownInt23 != 0) {
            stream.writeByte(130);
            stream.writeByte(unknownInt22);
            stream.writeShort(unknownInt23);
        }

        if (unknownArray2 != null) {
            stream.writeByte(132);
            stream.writeByte(unknownArray2.length);
            for (int index = 0; index < unknownArray2.length; index++) {
                stream.writeShort(unknownArray2[index]);
            }
        }

        // skip 134

        if (bindId != -1) {
            stream.writeByte(139);
            stream.writeShort(bindId);
        }

        if (bindTemplateId != -1) {
            stream.writeByte(140);
            stream.writeShort(bindTemplateId);
        }

        if (unknownArray4 != null) {
            for (int index = 0; index < unknownArray4.length; index++) {
                if (unknownArray4[index] == 0)
                    continue;
                stream.writeByte(142 + index);
                stream.writeShort(unknownArray4[index]);
            }
        }

        if (unknownArray5 != null) {
            for (int index = 0; index < unknownArray5.length; index++) {
                if (unknownArray5[index] == 0)
                    continue;
                stream.writeByte(150 + index);
                stream.writeShort(unknownArray5[index]);
            }
        }

        // skip 156-164

        if (stackable > 1 && certId == -1) {
            stream.writeByte(165);
        }

        // skip 168-252

        if (clientScriptData != null) {
            stream.writeByte(249);
            stream.writeByte(clientScriptData.size());
            for (Map.Entry<Integer, Object> entry : clientScriptData.entrySet()) {
                Object value = entry.getValue();
                stream.writeByte((value instanceof String ? 1 : 0));
                stream.write24BitInt(entry.getKey());
                if (value instanceof String) {
                    stream.writeString((String) value);
                } else {
                    stream.writeInt((Integer) value);
                }
            }
        }

        // end
        stream.writeByte(0);

        return stream;
    }

    /** Legacy 910 entry point: tolerant filestore stream, unknown opcodes are ignored exactly as before. */
    public final void readValues(InputStream stream, int opcode) {
        readValues(new LegacyReader(stream), opcode);
    }

    /**
     * Read surface for the tolerant 910 filestore stream. It used to be shared with the strict
     * modern path as well; it no longer is (see {@link #readModern950Values}). Only the methods
     * the 910 table actually uses are exposed.
     */
    private interface OpcodeReader {
        int readByte();
        int readUnsignedByte();
        int readUnsignedShort();
        int read24BitInt();
        int readInt();
        int readBigSmart();
        String readString();
    }

    private static final class LegacyReader implements OpcodeReader {
        private final InputStream stream;
        LegacyReader(InputStream stream) { this.stream = stream; }
        public int readByte() { return stream.readByte(); }
        public int readUnsignedByte() { return stream.readUnsignedByte(); }
        public int readUnsignedShort() { return stream.readUnsignedShort(); }
        public int read24BitInt() { return stream.read24BitInt(); }
        public int readInt() { return stream.readInt(); }
        public int readBigSmart() { return stream.readBigSmart(); }
        public String readString() { return stream.readString(); }
    }

    // The modern path no longer shares the 910 table, so the ModernReader adapter that used to sit
    // here is gone; readModern950Values() reads the strict network stream directly.

    /**
     * Strict modern opcode loop. Every opcode is decoded by {@link #readModern950Values}; the
     * tolerant 910 table is NOT consulted on this path any more, because the two revisions
     * disagree about the width of several opcodes (18, 127-130, 242-248, 251, 252 - see that
     * method's javadoc). Anything the 950 table does not know aborts the decode (fail closed)
     * instead of being skipped.
     */
    private void readModernOpcodeValues(com.rs.network.io.InputStream stream, StringBuilder trace) {
        for (;;) {
            int offset = stream.getOffset();
            int opcode = stream.readUnsignedByte();
            if (opcode == 0)
                break;
            if (trace != null)
                trace.append(opcode).append('@').append(offset).append(' ');
            if (!readModern950Values(stream, opcode))
                throw new IllegalArgumentException("Unsupported modern item " + id + " opcode " + opcode + " at " + offset);
        }
        if (stream.getRemaining() != 0)
            throw new IllegalArgumentException("Trailing bytes in modern item " + id + " at " + stream.getOffset());
    }

    /**
     * ObjType (cache index 19) field decoder for the 950 client. Returns false for an opcode the
     * 950 client has no handler for.
     *
     * <p>Derived from the 950 client's own ObjType decoder. Unlike LocType's cmp/jne ladder this
     * one IS a jump table: the virtual method at <b>950 0x14036eb30</b> (rcx = ObjType,
     * rdx = buffer, r8d = opcode; its vtable slot is 950 0x140b79fd8, one slot family away from
     * LocType's 0x140b79cf8) bounds-checks {@code opcode - 1 <= 0xf8}, indexes the byte table at
     * <b>950 0x14037039c</b> with {@code opcode - 1}, and jumps through the 81-entry dword table at
     * <b>950 0x140370258</b>. Decoding those two tables gives the complete accepted opcode set and
     * the exact handler address for each; the handler bodies live in 950 0x14036eb58..0x140370243.
     * The buffer's read position is the qword at buffer+0x18, so every "lea rax, [rcx + n];
     * mov [rdx+0x18], rax" in a handler is an n-byte field. NOTE: 947 addresses do not carry over -
     * they disassemble cleanly in the 950 image but land on unrelated code, so every address quoted
     * here was derived against the 950 binary.
     *
     * <p>Shared helpers, same ones LocType uses: 950 0x1400feea0 readBigSmart (one byte &lt;= 0x7f
     * means a 2-byte value, otherwise 4 bytes with bit 31 cleared), 950 0x1400feb20 int64,
     * 950 0x1400b4db0 / 0x140100d50 NUL-terminated string, 950 0x1403f13e0 the params record.
     *
     * <p>The complete opcode set the 950 client accepts is:
     * <pre>
     *   1 2 3 4 5 6 7 8 9 11 12 13 14 15 16 23 24 25 26 27 30-34 35-39 40 41 42 43 44 45 46-56
     *   65 69 78 79 90 91 92 93 94 95 96 97 98 100-109 110 111 112 113 114 115 121 122 125 126
     *   131 132 134 139 140 142-146 150-154 156 157 161 162 163 164 165 167 168 178 181 182
     *   190-199 201 202 203 204 205 206 207 208 249
     * </pre>
     * Every other opcode in 1..249 shares the default target 950 0x140370226, which is the
     * function epilogue: the client reads nothing and moves on. Opcodes 0 and 250-255 never reach
     * the table at all (0 terminates the loop, 250-255 fail the bounds check at 950 0x14036eb4d).
     * This method still returns false for all of them ON PURPOSE. Reading nothing would keep the
     * stream aligned, but it would also silently swallow a misaligned stream, and the whole point
     * of the strict path is that a desync surfaces as an exception rather than as a wrong item.
     * If a future cache legitimately carries one of them, add it here as an explicit no-op.
     *
     * <p>Of the accepted set, the shipped 950 cache uses all except 1, 12, 31, 42, 43, 46-56, 97,
     * 98, 100-109, 121, 122, 131, 139, 140, 143, 156, 161, 162 and 199; those are implemented from
     * the client only. In particular the narrow id opcodes 97/98/121/122/139/140/161/162 have been
     * fully replaced by their 24-bit forms 201-208, and opcodes 100-109 by 190-199 - no item in
     * the cache carries both forms of a pair. Opcode 1 is likewise fully replaced by opcode 9.
     *
     * <p>Verification: all 63,414 ObjType definitions in the 950 cache were parsed with this exact
     * width table; every one terminates on opcode 0 at exactly its record length, with no unknown
     * opcode and no over- or under-read.
     */
    private boolean readModern950Values(com.rs.network.io.InputStream stream, int opcode) {
        if (opcode == 1) {
            // 950 0x14036eb85: bigSmart. Clears the model vector and pushes one entry, so this is
            // model list index 0. No item in the 950 cache uses it; opcode 9 carries the models.
            baseModel = stream.readBigSmart();
            inventoryModels = new int[] { baseModel };
        } else if (opcode == 2) {
            name = stream.readString(); // 950 0x14036ebb0: NUL-terminated string.
        } else if (opcode == 3) {
            description = stream.readString(); // 950 0x14036ebde.
        } else if (opcode == 4) {
            modelScale = stream.readUnsignedShort(); // 950 0x14036ec05: two bytes.
        } else if (opcode == 5) {
            modelPitch = stream.readUnsignedShort(); // 950 0x14036ec36.
        } else if (opcode == 6) {
            modelRoll = stream.readUnsignedShort(); // 950 0x14036ec67.
        } else if (opcode == 7) {
            // 950 0x14036ec98: two bytes, sign-extended at 0x14036ecc6 (>0x7fff subtracts 0x10000).
            modelTranslateX = stream.readUnsignedShort();
            if (modelTranslateX > 32767)
                modelTranslateX -= 65536;
        } else if (opcode == 8) {
            modelTranslateY = stream.readUnsignedShort(); // 950 0x14036ece2, same shape as 7.
            if (modelTranslateY > 32767)
                modelTranslateY -= 65536;
        } else if (opcode == 9) {
            // 950 0x14036ed2c: one byte count, then that many bigSmarts into the model vector at
            // ObjType+0x230 - the same vector opcode 1 pushes to and 46-56 overwrite entries of.
            int length = stream.readUnsignedByte();
            inventoryModels = new int[length];
            for (int index = 0; index < length; index++)
                inventoryModels[index] = stream.readBigSmart();
            if (length > 0)
                baseModel = inventoryModels[0];
        } else if (opcode == 11) {
            stackable = 1; // 950 0x14036edab: no payload, writes 1 to the stack mode byte.
        } else if (opcode == 12) {
            // 950 0x14036edb8: four bytes, sign-extended into the same qword opcode 181 writes.
            value = stream.readInt();
            value64 = value;
        } else if (opcode == 13) {
            equipSlot = stream.readUnsignedByte(); // 950 0x14036ee0a: one byte (ObjType+0x2ec).
        } else if (opcode == 14) {
            equipType = stream.readUnsignedByte(); // 950 0x14036ee2a: one byte (ObjType+0x2ed).
        } else if (opcode == 15) {
            // 950 0x14036ee4a: no payload; clears a boolean at ObjType+0x220. Meaning undetermined.
        } else if (opcode == 16) {
            membersOnly = true; // 950 0x14036ee57: no payload.
        } else if (opcode == 23) {
            maleEquip1 = stream.readBigSmart(); // 950 0x14036ee64.
        } else if (opcode == 24) {
            maleEquip2 = stream.readBigSmart(); // 950 0x14036ee78.
        } else if (opcode == 25) {
            femaleEquip1 = stream.readBigSmart(); // 950 0x14036ee8c.
        } else if (opcode == 26) {
            femaleEquip2 = stream.readBigSmart(); // 950 0x14036eea0.
        } else if (opcode == 27) {
            equipLookHideSlot2 = stream.readUnsignedByte(); // 950 0x14036eeb4: one byte.
        } else if (opcode >= 30 && opcode < 35) {
            groundOptions[opcode - 30] = stream.readString(); // 950 0x14036eed4: string.
        } else if (opcode >= 35 && opcode < 40) {
            inventoryOptions[opcode - 35] = stream.readString(); // 950 0x14036ef0b: string.
        } else if (opcode == 40) {
            // 950 0x14036ef7c: one byte count, then count * (2-byte src, 2-byte dst) - the read
            // loop at 950 0x14036f080 advances 2+2 per iteration.
            int length = stream.readUnsignedByte();
            originalModelColors = new int[length];
            modifiedModelColors = new int[length];
            for (int index = 0; index < length; index++) {
                originalModelColors[index] = stream.readUnsignedShort();
                modifiedModelColors[index] = stream.readUnsignedShort();
            }
        } else if (opcode == 41) {
            // 950 0x14036f0e9, loop at 950 0x14036f1f0: identical shape to opcode 40.
            int length = stream.readUnsignedByte();
            originalTextureColors = new short[length];
            modifiedTextureColors = new short[length];
            for (int index = 0; index < length; index++) {
                originalTextureColors[index] = (short) stream.readUnsignedShort();
                modifiedTextureColors[index] = (short) stream.readUnsignedShort();
            }
        } else if (opcode == 42) {
            // 950 0x14036f259: one byte count, then the client ADDS count to the read position
            // without decoding it. Kept verbatim so the bytes are at least preserved.
            int length = stream.readUnsignedByte();
            unknownArray1 = new byte[length];
            for (int index = 0; index < length; index++)
                unknownArray1[index] = (byte) stream.readByte();
        } else if (opcode == 43) {
            // 950 0x14036f279: four bytes into ObjType+0x224, plus a flag byte at +0x223.
            // Meaning undetermined; stored verbatim to keep the stream aligned.
            unknownInt43 = stream.readInt();
        } else if (opcode == 44) {
            // 950 0x14036f2ca: two bytes. A bitmask that re-indexes the opcode 40 recolour lists;
            // no further bytes are read, all the remaining work is in memory.
            stream.readUnsignedShort();
        } else if (opcode == 45) {
            // 950 0x14036f3bf: two bytes, the opcode 41 retexture equivalent of opcode 44.
            stream.readUnsignedShort();
        } else if (opcode >= 46 && opcode < 57) {
            // 950 0x14036f4af: bigSmart written to model vector entry (opcode - 45), i.e. 1..11.
            // The client assumes opcode 9 already sized the vector; grow defensively here.
            int index = opcode - 45;
            if (inventoryModels == null)
                inventoryModels = new int[index + 1];
            else if (inventoryModels.length <= index)
                inventoryModels = Arrays.copyOf(inventoryModels, index + 1);
            inventoryModels[index] = stream.readBigSmart();
        } else if (opcode == 65) {
            unnoted = true; // 950 0x14036f4ca: no payload.
        } else if (opcode == 69) {
            unknownInt69 = stream.readInt(); // 950 0x14036f4d7: four bytes (ObjType+0x21c).
        } else if (opcode == 78) {
            maleEquipModelId3 = stream.readBigSmart(); // 950 0x14036f504.
        } else if (opcode == 79) {
            femaleEquipModelId3 = stream.readBigSmart(); // 950 0x14036f518.
        } else if (opcode == 90) {
            unknownInt1 = stream.readBigSmart(); // 950 0x14036f52c.
        } else if (opcode == 91) {
            unknownInt2 = stream.readBigSmart(); // 950 0x14036f540.
        } else if (opcode == 92) {
            unknownInt3 = stream.readBigSmart(); // 950 0x14036f554.
        } else if (opcode == 93) {
            unknownInt4 = stream.readBigSmart(); // 950 0x14036f568.
        } else if (opcode == 94) {
            itemCategory = stream.readUnsignedShort(); // 950 0x14036f57c: two bytes.
        } else if (opcode == 95) {
            unknownInt5 = stream.readUnsignedShort(); // 950 0x14036f5ad: two bytes.
        } else if (opcode == 96) {
            unknownInt6 = stream.readUnsignedByte(); // 950 0x14036f5de: one byte.
        } else if (opcode == 97) {
            certId = stream.readUnsignedShort(); // 950 0x14036f5fe: two bytes (ObjType+0x330).
        } else if (opcode == 98) {
            certTemplateId = stream.readUnsignedShort(); // 950 0x14036f631: two bytes (+0x334).
        } else if (opcode >= 100 && opcode < 110) {
            // 950 0x14036f664, read at 950 0x14036f767: two bytes id then two bytes amount.
            if (stackIds == null) {
                stackIds = new int[10];
                stackAmounts = new int[10];
            }
            stackIds[opcode - 100] = stream.readUnsignedShort();
            stackAmounts[opcode - 100] = stream.readUnsignedShort();
        } else if (opcode == 110) {
            unknownInt7 = stream.readUnsignedShort(); // 950 0x14036f7d3: two bytes.
        } else if (opcode == 111) {
            unknownInt8 = stream.readUnsignedShort(); // 950 0x14036f7d3, same handler as 110/112.
        } else if (opcode == 112) {
            unknownInt9 = stream.readUnsignedShort(); // 950 0x14036f7d3.
        } else if (opcode == 113) {
            unknownInt10 = stream.readByte(); // 950 0x14036f84f: one SIGNED byte (movsx).
        } else if (opcode == 114) {
            unknownInt11 = stream.readByte() * 5; // 950 0x14036f870: one signed byte.
        } else if (opcode == 115) {
            teamId = stream.readUnsignedByte(); // 950 0x14036f89b: one byte.
        } else if (opcode == 121) {
            lendId = stream.readUnsignedShort(); // 950 0x14036f8bb: two bytes (ObjType+0x338).
        } else if (opcode == 122) {
            lendTemplateId = stream.readUnsignedShort(); // 950 0x14036f8ee: two bytes (+0x33c).
        } else if (opcode == 125) {
            // 950 0x14036f921: three SIGNED bytes. The client converts each straight to float with
            // no scaling; the << 2 below is the server's own long-standing convention and is what
            // writeValues() undoes with >> 2. It never reaches the client, so it cannot desync.
            unknownInt12 = stream.readByte() << 2;
            unknownInt13 = stream.readByte() << 2;
            unknownInt14 = stream.readByte() << 2;
        } else if (opcode == 126) {
            unknownInt15 = stream.readByte() << 2; // 950 0x14036f993: three signed bytes, as 125.
            unknownInt16 = stream.readByte() << 2;
            unknownInt17 = stream.readByte() << 2;
        } else if (opcode == 131) {
            // 950 0x14036fa05: NUL-terminated string into ObjType+0x170, a third string field
            // distinct from name (+0x140), description (+0x158) and shardName (+0x358).
            // Meaning undetermined; stored verbatim.
            unknownString131 = stream.readString();
        } else if (opcode == 132) {
            // 950 0x14036fa58, loop at 950 0x14036faf0: one byte count then count * two bytes.
            int length = stream.readUnsignedByte();
            unknownArray2 = new int[length];
            for (int index = 0; index < length; index++)
                unknownArray2[index] = stream.readUnsignedShort();
        } else if (opcode == 134) {
            stream.readUnsignedByte(); // 950 0x14036fb2e: one byte (ObjType+0x2c4). Not kept.
        } else if (opcode == 139) {
            bindId = stream.readUnsignedShort(); // 950 0x14036fb4e: two bytes (ObjType+0x340).
        } else if (opcode == 140) {
            bindTemplateId = stream.readUnsignedShort(); // 950 0x14036fb81: two bytes (+0x344).
        } else if (opcode >= 142 && opcode < 147) {
            // 950 0x14036fbb4, read at 950 0x14036fc65: two bytes into slot (opcode - 142) of a
            // six-entry array the client pre-fills with -1.
            if (unknownArray4 == null) {
                unknownArray4 = new int[6];
                Arrays.fill(unknownArray4, -1);
            }
            unknownArray4[opcode - 142] = stream.readUnsignedShort();
        } else if (opcode >= 150 && opcode < 155) {
            // 950 0x14036fc98, read at 950 0x14036fd3d: two bytes into slot (opcode - 150) of a
            // five-entry array.
            if (unknownArray5 == null) {
                unknownArray5 = new int[5];
                Arrays.fill(unknownArray5, -1);
            }
            unknownArray5[opcode - 150] = stream.readUnsignedShort();
        } else if (opcode == 156) {
            // 950 0x14036fd70: no payload; clears a boolean at ObjType+0x1d9. Meaning undetermined.
        } else if (opcode == 157) {
            // 950 0x14036fd7d: no payload; sets a boolean at ObjType+0x1da. Meaning undetermined.
        } else if (opcode == 161) {
            shardId = stream.readUnsignedShort(); // 950 0x14036fd8a: two bytes (ObjType+0x348).
        } else if (opcode == 162) {
            shardTemplateId = stream.readUnsignedShort(); // 950 0x14036fdbd: two bytes (+0x34c).
        } else if (opcode == 163) {
            shardCombineRequirement = stream.readUnsignedShort(); // 950 0x14036fdf0: two bytes.
        } else if (opcode == 164) {
            shardName = stream.readString(); // 950 0x14036fe23: NUL-terminated string.
        } else if (opcode == 165) {
            stackable = 2; // 950 0x14036fe8a: no payload, writes 2 to the stack mode byte.
        } else if (opcode == 167) {
            flag167 = true; // 950 0x14036fe97: no payload, sets ObjType+0x222. Meaning undetermined.
        } else if (opcode == 168) {
            // 950 0x14036fea4: no payload; clears a boolean at ObjType+0x22a. Meaning undetermined.
        } else if (opcode == 178) {
            // 950 0x14036feb1: no payload. It writes 0 to the SAME stack mode byte opcode 11 sets
            // to 1 and opcode 165 sets to 2, so it explicitly means "not stackable". No item in
            // the 950 cache carries 178 together with 11 or 165, so this cannot silently undo one.
            stackable = 0;
            flag178 = true;
        } else if (opcode == 181) {
            // 950 0x14036febe -> 950 0x1400feb20: eight bytes, into the same qword as opcode 12.
            value64 = stream.readLong();
            value = value64 > Integer.MAX_VALUE ? Integer.MAX_VALUE
                    : value64 < Integer.MIN_VALUE ? Integer.MIN_VALUE : (int) value64;
        } else if (opcode == 182) {
            // 950 0x14036fed2: three bytes, big-endian, into ObjType+0x370 - a field nothing else
            // in the decoder touches. Meaning undetermined; stored verbatim.
            unknownInt182 = stream.read24BitInt();
        } else if (opcode >= 190 && opcode < 200) {
            // 950 0x14036ff07, read at 950 0x14037000a: THREE bytes of id then TWO bytes of
            // amount, into the same pair of arrays opcodes 100-109 fill, at slot (opcode - 190).
            // This is the wide form of 100-109 and is what the 950 cache actually ships.
            if (stackIds == null) {
                stackIds = new int[10];
                stackAmounts = new int[10];
            }
            stackIds[opcode - 190] = stream.read24BitInt();
            stackAmounts[opcode - 190] = stream.readUnsignedShort();
        } else if (opcode == 201) {
            certId = stream.read24BitInt(); // 950 0x14037007f: three bytes, opcode 97's field.
        } else if (opcode == 202) {
            certTemplateId = stream.read24BitInt(); // 950 0x1403700b4: opcode 98's field (+0x334).
        } else if (opcode == 203) {
            lendId = stream.read24BitInt(); // 950 0x1403700e9: opcode 121's field (+0x338).
        } else if (opcode == 204) {
            lendTemplateId = stream.read24BitInt(); // 950 0x14037011e: opcode 122's field (+0x33c).
        } else if (opcode == 205) {
            bindId = stream.read24BitInt(); // 950 0x140370153: opcode 139's field (+0x340).
        } else if (opcode == 206) {
            bindTemplateId = stream.read24BitInt(); // 950 0x140370188: opcode 140's field (+0x344).
        } else if (opcode == 207) {
            shardId = stream.read24BitInt(); // 950 0x1403701ba: opcode 161's field (+0x348).
        } else if (opcode == 208) {
            shardTemplateId = stream.read24BitInt(); // 950 0x1403701ec: opcode 162's field (+0x34c).
        } else if (opcode == 249) {
            // 950 0x14037021e -> the shared params record at 950 0x1403f13e0: one byte count, then
            // per entry one byte "is string" (compared against 1 at 950 0x1403f152b), a 3-byte key
            // and either a NUL-terminated string or a 4-byte int.
            int length = stream.readUnsignedByte();
            if (clientScriptData == null)
                clientScriptData = new HashMap<Integer, Object>(length);
            for (int index = 0; index < length; index++) {
                boolean stringInstance = stream.readUnsignedByte() == 1;
                int key = stream.read24BitInt();
                Object value = stringInstance ? stream.readString() : (Object) Integer.valueOf(stream.readInt());
                clientScriptData.put(key, value);
            }
        } else {
            return false;
        }
        return true;
    }

    /**
     * Legacy 910 opcode table; returns false for an opcode it does not know.
     *
     * <p>LEGACY CACHE ONLY. Do not route a modern (flat) record through this - use
     * {@link #readModern950Values}. Fourteen of the widths here contradict the 950 client's own
     * ObjType decoder, and until this commit the modern path went through this table, so each of
     * them was a live desync waiting for a record that used it:
     * <ul>
     *   <li>18 - read here as 2 bytes; 950 0x14036eb4d..0x140370226 dispatches 18 to the default
     *       target, so the 950 client reads nothing.</li>
     *   <li>127, 128, 129, 130 - read here as 1 + 2 bytes each; all four are default targets in
     *       950, i.e. no payload.</li>
     *   <li>242, 243, 244, 245, 246, 247, 248 - read here as a bigSmart (2 or 4 bytes); all seven
     *       are default targets in 950.</li>
     *   <li>251, 252 - read here as 1 + count*4 bytes; in 950 they are above the decoder's bounds
     *       check (950 0x14036eb4d accepts 1..249 only), so the client reads nothing.</li>
     * </ul>
     * None of them occurs in the shipped 950 cache, which is why the sweep never caught them; a
     * single hand-edited definition would have been enough to mis-parse every field after it.
     */
    private boolean readValues(OpcodeReader stream, int opcode) {
        if (opcode == 1)
            baseModel = stream.readBigSmart();
        else if (opcode == 2)
            name = stream.readString();
        else if (opcode == 4)
            modelScale = stream.readUnsignedShort();
        else if (opcode == 5)
            modelPitch = stream.readUnsignedShort();
        else if (opcode == 6)
            modelRoll = stream.readUnsignedShort();
        else if (opcode == 7) {
            modelTranslateX = stream.readUnsignedShort();
            if (modelTranslateX > 32767)
                modelTranslateX -= 65536;
            modelTranslateX <<= 0;
        } else if (opcode == 8) {
            modelTranslateY = stream.readUnsignedShort();
            if (modelTranslateY > 32767)
                modelTranslateY -= 65536;
            modelTranslateY <<= 0;
        } else if (opcode == 11)
            stackable = 1;
        else if (opcode == 12)
            value = stream.readInt();
        else if (opcode == 13) {
            equipSlot = stream.readUnsignedByte();
        } else if (opcode == 14) {
            equipType = stream.readUnsignedByte();
        } else if (opcode == 15) {

        } else if (opcode == 16)
            membersOnly = true;
        else if (opcode == 18) { // added
            stream.readUnsignedShort();
        } else if (opcode == 23)
            maleEquip1 = stream.readBigSmart();
        else if (opcode == 24)
            maleEquip2 = stream.readBigSmart();
        else if (opcode == 25)
            femaleEquip1 = stream.readBigSmart();
        else if (opcode == 26)
            femaleEquip2 = stream.readBigSmart();
        else if (opcode == 27)
            equipLookHideSlot2 = stream.readUnsignedByte();
        else if (opcode >= 30 && opcode < 35)
            groundOptions[opcode - 30] = stream.readString();
        else if (opcode >= 35 && opcode < 40)
            inventoryOptions[opcode - 35] = stream.readString();
        else if (opcode == 40) {
            int length = stream.readUnsignedByte();
            originalModelColors = new int[length];
            modifiedModelColors = new int[length];
            for (int index = 0; index < length; index++) {
                originalModelColors[index] = stream.readUnsignedShort();
                modifiedModelColors[index] = stream.readUnsignedShort();
            }
        } else if (opcode == 41) {
            int length = stream.readUnsignedByte();
            originalTextureColors = new short[length];
            modifiedTextureColors = new short[length];
            for (int index = 0; index < length; index++) {
                originalTextureColors[index] = (short) stream.readUnsignedShort();
                modifiedTextureColors[index] = (short) stream.readUnsignedShort();
            }
        } else if (opcode == 42) {
            int length = stream.readUnsignedByte();
            unknownArray1 = new byte[length];
            for (int index = 0; index < length; index++)
                unknownArray1[index] = (byte) stream.readByte();
        } else if (opcode == 44) {
            int length = stream.readUnsignedShort();
            int arraySize = 0;
            for (int modifier = 0; modifier > 0; modifier++) {
                arraySize++;
                unknownArray3 = new byte[arraySize];
                byte offset = 0;
                for (int index = 0; index < arraySize; index++) {
                    if ((length & 1 << index) > 0) {
                        unknownArray3[index] = offset;
                    } else {
                        unknownArray3[index] = -1;
                    }
                }
            }
        } else if (45 == opcode) {
            int length = (short) stream.readUnsignedShort();
            int i_98_ = 0;
            for (int i_99_ = length; i_99_ > 0; i_99_ >>= 1)
                i_98_++;
            unknownArray6 = new byte[i_98_];
            byte i_100_ = 0;
            for (int i_101_ = 0; i_101_ < i_98_; i_101_++) {
                if ((length & 1 << i_101_) > 0) {
                    unknownArray6[i_101_] = i_100_;
                    i_100_++;
                } else
                    unknownArray6[i_101_] = (byte) -1;
            }
        } else if (opcode == 65)
            unnoted = true;
        else if (opcode == 78)
            maleEquipModelId3 = stream.readBigSmart();
        else if (opcode == 79)
            femaleEquipModelId3 = stream.readBigSmart();
        else if (opcode == 90)
            unknownInt1 = stream.readBigSmart();
        else if (opcode == 91)
            unknownInt2 = stream.readBigSmart();
        else if (opcode == 92)
            unknownInt3 = stream.readBigSmart();
        else if (opcode == 93)
            unknownInt4 = stream.readBigSmart();
        else if (opcode == 94) {// new
            itemCategory = stream.readUnsignedShort();
        } else if (opcode == 95)
            unknownInt5 = stream.readUnsignedShort();
        else if (opcode == 96)
            unknownInt6 = stream.readUnsignedByte();
        else if (opcode == 97)
            certId = stream.readUnsignedShort();
        else if (opcode == 98)
            certTemplateId = stream.readUnsignedShort();
        else if (opcode >= 100 && opcode < 110) {
            if (stackIds == null) {
                stackIds = new int[10];
                stackAmounts = new int[10];
            }
            stackIds[opcode - 100] = stream.readUnsignedShort();
            stackAmounts[opcode - 100] = stream.readUnsignedShort();
        } else if (opcode == 110)
            unknownInt7 = stream.readUnsignedShort();
        else if (opcode == 111)
            unknownInt8 = stream.readUnsignedShort();
        else if (opcode == 112)
            unknownInt9 = stream.readUnsignedShort();
        else if (opcode == 113)
            unknownInt10 = stream.readByte();
        else if (opcode == 114)
            unknownInt11 = stream.readByte() * 5;
        else if (opcode == 115)
            teamId = stream.readUnsignedByte();
        else if (opcode == 121)
            lendId = stream.readUnsignedShort();
        else if (opcode == 122)
            lendTemplateId = stream.readUnsignedShort();
        else if (opcode == 125) {
            unknownInt12 = stream.readByte() << 2;
            unknownInt13 = stream.readByte() << 2;
            unknownInt14 = stream.readByte() << 2;
        } else if (opcode == 126) {
            unknownInt15 = stream.readByte() << 2;
            unknownInt16 = stream.readByte() << 2;
            unknownInt17 = stream.readByte() << 2;
        } else if (opcode == 127) {
            unknownInt18 = stream.readUnsignedByte();
            unknownInt19 = stream.readUnsignedShort();
        } else if (opcode == 128) {
            unknownInt20 = stream.readUnsignedByte();
            unknownInt21 = stream.readUnsignedShort();
        } else if (opcode == 129) {
            unknownInt20 = stream.readUnsignedByte();
            unknownInt21 = stream.readUnsignedShort();
        } else if (opcode == 130) {
            unknownInt22 = stream.readUnsignedByte();
            unknownInt23 = stream.readUnsignedShort();
        } else if (opcode == 132) {
            int length = stream.readUnsignedByte();
            unknownArray2 = new int[length];
            for (int index = 0; index < length; index++)
                unknownArray2[index] = stream.readUnsignedShort();
        } else if (opcode == 134) {
            int unknownValue = stream.readUnsignedByte();
        } else if (opcode == 139) {
            bindId = stream.readUnsignedShort();
        } else if (opcode == 140) {
            bindTemplateId = stream.readUnsignedShort();
        } else if (opcode >= 142 && opcode < 147) {
            if (unknownArray4 == null) {
                unknownArray4 = new int[6];
                Arrays.fill(unknownArray4, -1);
            }
            unknownArray4[opcode - 142] = stream.readUnsignedShort();
        } else if (opcode >= 150 && opcode < 155) {
            if (null == unknownArray5) {
                unknownArray5 = new int[5];
                Arrays.fill(unknownArray5, -1);
            }
            unknownArray5[opcode - 150] = stream.readUnsignedShort();
        } else if (opcode == 156) { // new

        } else if (157 == opcode) {// new
            boolean aBool7955 = true;
        } else if (opcode == 161) {
            shardId = stream.readUnsignedShort();
        } else if (162 == opcode) {
            shardTemplateId = stream.readUnsignedShort();
        } else if (163 == opcode) {
            shardCombineRequirement = stream.readUnsignedShort();
        } else if (opcode == 164) {
            shardName = stream.readString();
        } else if (opcode == 165) {// new
            stackable = 2;
        } else if (opcode == 168) {

        } else if (opcode == 242) {
            int oldInvModel = stream.readBigSmart();
        } else if (opcode == 243) {
            int oldMaleEquipModelId3 = stream.readBigSmart();
        } else if (opcode == 244) {
            int oldFemaleEquipModelId3 = stream.readBigSmart();
        } else if (opcode == 245) {
            int oldMaleEquipModelId2 = stream.readBigSmart();
        } else if (opcode == 246) {
            int oldFemaleEquipModelId2 = stream.readBigSmart();
        } else if (opcode == 247) {
            int oldMaleEquipModelId1 = stream.readBigSmart();
        } else if (opcode == 248) {
            int oldFemaleEquipModelId1 = stream.readBigSmart();
        } else if (opcode == 251) {
            int length = stream.readUnsignedByte();
            int[] oldoriginalModelColors = new int[length];
            int[] oldmodifiedModelColors = new int[length];
            for (int index = 0; index < length; index++) {
                oldoriginalModelColors[index] = stream.readUnsignedShort();
                oldmodifiedModelColors[index] = stream.readUnsignedShort();
            }
        } else if (opcode == 252) {
            int length = stream.readUnsignedByte();
            short[] oldoriginalTextureColors = new short[length];
            short[] oldmodifiedTextureColors = new short[length];
            for (int index = 0; index < length; index++) {
                oldoriginalTextureColors[index] = (short) stream.readUnsignedShort();
                oldmodifiedTextureColors[index] = (short) stream.readUnsignedShort();
            }
        } else if (opcode == 249) {
            int length = stream.readUnsignedByte();
            if (clientScriptData == null)
                clientScriptData = new HashMap<Integer, Object>(length);
            for (int index = 0; index < length; index++) {
                boolean stringInstance = stream.readUnsignedByte() == 1;
                int key = stream.read24BitInt();
                Object value = stringInstance ? stream.readString() : stream.readInt();
                clientScriptData.put(key, value);
            }
        } else {
            // if (Settings.DEBUG)
            // throw new RuntimeException("MISSING OPCODE " + opcode + " FOR
            // ITEM " + getId());
            return false;
        }
        return true;
    }

    public final void readOpcodeValues(InputStream stream) {
        while (true) {
            int opcode = stream.readUnsignedByte();
            if (opcode == 0)
                break;
            readValues(stream, opcode);
        }
    }

    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    public int getFemaleWornModelId1() {
        return femaleEquip1;
    }

    public int getFemaleWornModelId2() {
        return femaleEquip2;
    }

    public int getFemaleWornModelId3() {
        return femaleEquipModelId3;
    }

    public int getMaleWornModelId1() {
        return maleEquip1;
    }

    public int getMaleWornModelId2() {
        return maleEquip2;
    }

    public int getMaleWornModelId3() {
        return maleEquipModelId3;
    }

    public boolean isOverSized() {
        return modelScale > 5000;
    }

    public boolean isLended() {
        return lended;
    }

    public boolean isMembersOnly() {
        return membersOnly;
    }

    public boolean isStackable() {
        return stackable == 1 || id == 0;
    }

    private void setStackable(boolean stackable) {
        this.stackable = stackable ? 1 : 0;
    }

    public boolean isNoted() {
        return noted;
    }

    public int getLendId() {
        return lendId;
    }

    public int getCertId() {
        return certId;
    }

    public int getValue() {
        return value;
    }

    public int getDeathValue() {
        if (value <= 0)
            return 1;
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public int getEquipSlot() {
        return equipSlot;
    }

    public int getEquipType() {
        return equipType;
    }

    public String getExamine() {
        return ItemExaminesDataParser.getExamine(new Item(id));
    }

    public int getSheatheModelId() {
        if (clientScriptData == null)
            return -1;
        Object modelId = clientScriptData.get(2820);
        if (modelId != null && modelId instanceof Integer)
            return (Integer) modelId;
        return -1;
    }

    public GeneralRequirementMap getCombatMap() {
        if (name.equalsIgnoreCase("mud pie"))
            return GeneralRequirementMap.getMap(14944);
        int csMapOpcode = getCSOpcode(3000);
        if (csMapOpcode == 0)
            csMapOpcode = getCSOpcode(686);
        if (csMapOpcode != 0)
            return GeneralRequirementMap.getMap(csMapOpcode);
        return null;
    }

    public int getCSOpcode(int opcode, int magic) {
        if (clientScriptData != null) {
            Object value = clientScriptData.get(opcode);
            if (value != null && value instanceof Integer)
                return (int) value;
        }
        return magic;
    }

    public int getCSOpcode(int opcode) {
        return getCSOpcode(opcode, 0);
    }

    public String getStringOpcode(int opcode) {
        return getStringOpcode(opcode, null);
    }

    public String getStringOpcode(int opcode, String magic) {
        if (clientScriptData != null) {
            Object value = clientScriptData.get(opcode);
            if (value != null && value instanceof String)
                return (String) value;
        }
        return magic;
    }

    public int getCombatOpcode(int opcode) {
        Integer value;
        try {
            value = (Integer) clientScriptData.get(opcode);
        } catch (Exception e) {
            return 0;
        }
        if (value != null)
            return value;
        GeneralRequirementMap map = getCombatMap();
        return map == null ? 0 : map.getIntValue(opcode);
    }

    public int getCombatStyle() {
        return getCombatOpcode(2853);
    }

    public int getMainhandEmote(final boolean legacy) {
        if (MapInformationParser.getMap() == null || clientScriptData == null)
            return -1;
        final Integer index = (Integer) clientScriptData.get(686);
        if (index == null)
            return -1;
        final HashMap<Object, Object> map = MapInformationParser.getMap().get(index);
        final Object key = (legacy ? 4385 : 2914);// RS3 and legacy == 4385
        if (map != null) {
            if (map.get(key.toString()) != null) {
                Object value = map.get(key.toString());
                return Integer.parseInt(value.toString());
            }
        }
        return -1;
    }

    public int getOffhandEmote(final boolean legacy) {
        if (MapInformationParser.getMap() == null || clientScriptData == null)
            return -1;
        final Integer index = (Integer) clientScriptData.get(3000);
        if (index == null)
            return -1;
        final HashMap<Object, Object> map = MapInformationParser.getMap().get(index);
        final Object key = (legacy ? 4389 : 2831);// RS3 is 2831, legacy is 4389
        if (map != null) {
            if (map.get(key.toString()) != null) {
                Object value = map.get(key.toString());
                return Integer.parseInt(value.toString());
            } else
                return getMainhandEmote(legacy);
        }
        return -1;
    }

    public boolean isShield() {
        return getCSOpcode(2832) == 1;
    }

    public int getCombatDefenceEmote(final boolean legacy) {
        if (MapInformationParser.getMap() == null || clientScriptData == null)
            return 424;
        final Integer index = (Integer) clientScriptData.get(3000);
        if (index == null)
            return 424;
        final HashMap<Object, Object> map = MapInformationParser.getMap().get(index);
        final Object key = (legacy ? 4387 : 2917);// legacy == 4387
        if (map != null) {
            if (map.get(key.toString()) != null) {
                Object value = map.get(key.toString());
                return Integer.parseInt(value.toString());
            }
        }
        return 424;
    }

    public double getDungShopValueMultiplier() {
        if (clientScriptData == null)
            return 1;
        Object value = clientScriptData.get(1046);
        if (value != null && value instanceof Integer)
            return ((Integer) value).doubleValue() / 100;
        return 1;
    }

    public boolean isBindItem() {
        if (inventoryOptions == null)
            return false;
        for (String option : inventoryOptions) {
            if (option == null)
                continue;
            if (option.equalsIgnoreCase("bind"))
                return true;
        }
        return false;
    }

    public boolean isRingOfKinship() {
        if (inventoryOptions == null)
            return false;
        for (String option : inventoryOptions) {
            if (option == null)
                continue;
            if (option.equals("Open party interface"))
                return true;
        }
        return false;
    }

    public boolean isMeleeTypeGear() {
        return getCSOpcode(2821) == 1;
    }

    public boolean isRangeTypeGear() {
        return getCSOpcode(2822) == 1;
    }

    public boolean isMagicTypeGear() {
        return getCSOpcode(2823) == 1;
    }

    public boolean isMeleeTypeWeapon() {
        return getCSOpcode(2825) == 1;
    }

    public boolean isRangeTypeWeapon() {
        return getCSOpcode(2826) == 1;
    }

    public boolean isMagicTypeWeapon() {
        return getCSOpcode(2827) == 1;
    }

    public int getSpecialCost() {
        return (int) (getCSOpcode(4332, getParentDefinition().getCSOpcode(4332)) * 0.10D);
    }

    public ItemDefinitions getParentDefinition() {
        return getItemDefinitions(getCSOpcode(4338, id));
    }

    public int equipLookHideSlot2;

    public int getEquipType2() {
        return equipLookHideSlot2;
    }

    public boolean isCompletionistCape() {
        return getCSOpcode(7896) >= 2;
    }

    public boolean isMaxCape() {
        return getCSOpcode(7896) == 1;
    }

    public boolean containsEquipmentOption(String string) {
        for (String option : this.getEquipmentOptions()) {
            if (option != null && option.equalsIgnoreCase(string))
                return true;
        }
        return false;
    }

    public int getOverrideOpcode() {
        return getCSOpcode(686);
    }

    public int getOverrideOpcode2() {
        return getCSOpcode(3000);
    }

    public boolean isDefender() {
        return itemCategory == 494;
    }

    public int getDamage(int type) {
        if (type == Combat.MELEE_TYPE)
            return getMeleeDamage();
        if (type == Combat.RANGE_TYPE)
            return getRangeDamage();
        if (type == Combat.MAGIC_TYPE)
            return getMageDamage();
        return 0;
    }

    public int getMeleeDamage() {
        if (id == 41069)
            return 315;
        if (id == 48481)
            return 170;
        if (id == 48482)
            return 170;


        if (id == 48378) {
            return 1000000; // Hardcode stabAttack for item 48378
        } //testing stat override


        if (id == 48483)
            return 330;
        if (id == 48484)
            return 490;
        if (id == 36026)
            return 25;
        if (id == 36027)
            return 55;
        if (id == 36028)
            return 45;
        if (id == 36029)
            return 15;
        if (id == 36030)
            return 15;
        if (id == 31603)
            return 310;
        if (getName().equalsIgnoreCase("grim reaper hood"))
            return 210;
        return getCSOpcode(641);
    }

    public int getRangeDamage() {
        if (id == 41069)
            return 315;
        if (id == 48481)
            return 170;
        if (id == 48482)
            return 170;
        if (id == 48483)
            return 330;
        if (id == 48484)
            return 490;
        if (id == 36026)
            return 25;
        if (id == 36027)
            return 55;
        if (id == 36028)
            return 45;
        if (id == 36029)
            return 15;
        if (id == 36030)
            return 15;
        if (id == 31603)
            return 310;
        if (getName().equalsIgnoreCase("grim reaper hood"))
            return 210;
        return getCSOpcode(643);
    }

    public int getMageDamage() {
        if (id == 41069)
            return 315;
        if (id == 48481)
            return 170;
        if (id == 48482)
            return 170;
        if (id == 48483)
            return 330;
        if (id == 48484)
            return 490;
        if (id == 36026)
            return 25;
        if (id == 36027)
            return 55;
        if (id == 36028)
            return 45;
        if (id == 36029)
            return 15;
        if (id == 36030)
            return 15;
        if (id == 31603)
            return 310;
        if (getName().equalsIgnoreCase("grim reaper hood"))
            return 210;
        return getCSOpcode(965);
    }

    public int getRangedLevel() {
        if (getWearingSkillRequiriments() == null)
            return 1;
        Integer level = getWearingSkillRequiriments().get(Skills.RANGE);
        return level == null ? 1 : level;
    }

    public int getAccuracy(int type) {
        if (type == Combat.MELEE_TYPE)
            return getMeleeAccuracy();
        if (type == Combat.RANGE_TYPE)
            return getRangeAccuracy();
        if (type == Combat.MAGIC_TYPE)
            return getMageAccuracy();
        return 0;
    }

    public int getType() {
        if (isMeleeTypeWeapon() || isMeleeTypeGear())
            return Combat.MELEE_TYPE;
        if (isRangeTypeWeapon() || isRangeTypeGear())
            return Combat.RANGE_TYPE;
        if (isMagicTypeWeapon() || isMagicTypeGear())
            return Combat.MAGIC_TYPE;
        return Combat.ALL_TYPE;
    }

    public int getAccuracy() {
        if (getType() == Combat.MELEE_TYPE)
            return getMeleeAccuracy();
        if (getType() == Combat.RANGE_TYPE)
            return getRangeAccuracy();
        if (getType() == Combat.MAGIC_TYPE)
            return getMageAccuracy();
        return 0;
    }

    public int getMeleeAccuracy() {

        if (id == 48378) {
            return 1000000; // Hardcode stabAttack for item 48378
        } //testing stat override


        return getCSOpcode(3267);
    }

    public int getRangeAccuracy() {
        return getCSOpcode(4);
    }

    public int getMageAccuracy() {
        if (id == 15598)
            return 950;
        return getCSOpcode(3);
    }

    public int getHealth() {

        if (id == 48378) {
            return 1000000; // Hardcode stabAttack for item 48378
        } //testing stat override

        return getCSOpcode(1326);
    }

    public int getArmor() {
        if (id == 21538 || id == 21548 || id == 21558) {
            return 297;
        }
        if (id == 21540 || id == 21550 || id == 21560) {
            return 341;
        }
        if (id == 21542 || id == 21552 || id == 21562) {
            return 326;
        }
        if (id == 21544 || id == 21554 || id == 21564 || id == 21546 || id == 21556 || id == 21566) {
            return 68;
        }
        if (id == 36026)
            return 170;

        if (id == 48378) {
            return 1000000; // Hardcode stabAttack for item 48378
        } //testing stat override

        if (id == 36027)
            return 210;
        if (id == 36028)
            return 185;
        if (id == 36029)
            return 45;
        if (id == 36030)
            return 45;
        if (id == 31603)
            return 305;
        return getCSOpcode(2870) / 10;
    }

    public String[] getEquipmentOptions() {
        return new String[] { !(getCSOpcode(2091) == 1 || getCSOpcode(1430) != 0) ? "Remove" : null, getStringOpcode(528), getStringOpcode(529), getStringOpcode(530), getStringOpcode(531), getStringOpcode(1211), "Examine" };
    }

    public int getAugmentedItemId() {
        if (getCSOpcode(5551) != 0)
            return getCSOpcode(5551);
        if (id == 39652)
            return 39653;
        if (id == 39648)
            return 39649;
        if (id == 37546)
            return 36953;
        for (Object itemObj : ClientScriptMap.getMap(11269).getValues().values()) {
            int productId = (int) itemObj;
            ManufactureData data = Manufacture.getManufactureData(productId);
            for (int i = 0; i < data.getRequiredItems().length; i++) {
                int itemId = data.getRequiredItems()[i][0];
                if (itemId == this.id)
                    return productId;
            }
        }
        return 0;
    }

    public static final int IS_START_ITEM_OPCODE = 3384, NEXT_ID_OPCODE = 3382;

    public Integer[] getItemDegradeData() {
        List<Integer> ids = new ArrayList<Integer>();
        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(id);
        if (ChargesManagerNew.getStaticItemDegradeData(id) != null)
            return ChargesManagerNew.getStaticItemDegradeData(id);
        int nextId = defs.getCSOpcode(NEXT_ID_OPCODE);
        int usedOpcode = NEXT_ID_OPCODE;
        if (nextId == 0) {
            nextId = defs.getCSOpcode(1327);
            usedOpcode = 1327;
            if (nextId == 0) {
                nextId = defs.getCSOpcode(3792);
                usedOpcode = 3792;
                if (nextId == 0)
                    return null;
            }
        }
        if (defs.isNoted())
            return null;
        if (usedOpcode != NEXT_ID_OPCODE) {
            if (defs.getCSOpcode(2195) == 0)
                return null;
        } else {
            if ((defs.getCSOpcode(IS_START_ITEM_OPCODE) != 1 && defs.getCSOpcode(5449) != 1))
                return null;
        }
        if (defs.getCSOpcode(3110) != 0)
            return new Integer[] { defs.getCSOpcode(3110), -1 };
        int count = 0;

        while (count < 10) {
            defs = ItemDefinitions.getItemDefinitions(nextId);
            if (nextId != 0 && !ids.contains(nextId) && id != nextId) {
                ids.add(nextId);
                if (ItemDefinitions.getItemDefinitions(nextId).getCSOpcode(5527) == id)
                    nextId = id;
                if (nextId < 0 || nextId == id || ids.size() == 2)
                    break;
                nextId = defs.getCSOpcode(usedOpcode);
            }
            count++;
        }
        if (ids.isEmpty())
            return null;
        return ids.toArray(new Integer[ids.size()]);
    }

    public static String getName(int itemId) {
        val def = getItemDefinitions(itemId);
        if (def == null) {
            return "NULL";
        }
        return def.getName();
    }

    public Integer[] getRepairData() {
        if (ChargesManagerNew.getStaticItemRepairData(id) != null)
            return ChargesManagerNew.getStaticItemRepairData(id);
        List<Integer> ids = new ArrayList<Integer>();
        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(id);
        if (defs.isNoted() || defs.getName().toLowerCase().contains("augmented"))
            return null;
        int nextId = defs.getCSOpcode(NEXT_ID_OPCODE);
        boolean usedSecondOpcode = false;
        if (nextId == 0) {
            nextId = defs.getCSOpcode(1327);
            if (nextId == 0)
                return null;
            usedSecondOpcode = true;
        }
        if (usedSecondOpcode) {
            if (defs.getCSOpcode(2195) != 0)
                return null;
        } else {
            if (defs.getCSOpcode(IS_START_ITEM_OPCODE) == 1)
                return null;
        }
        int count = 0;
        while (count < 10) {
            defs = ItemDefinitions.getItemDefinitions(nextId);
            if (usedSecondOpcode) {
                if (defs.getCSOpcode(2195) != 0) {
                    ids.add(nextId);
                    break;
                }
            } else {
                if (defs.getCSOpcode(IS_START_ITEM_OPCODE) == 1) {
                    ids.add(nextId);
                    break;
                }
            }
            nextId = defs.getCSOpcode(usedSecondOpcode ? 1327 : NEXT_ID_OPCODE);
            count++;
        }
        if (ids.isEmpty())
            return null;
        return ids.toArray(new Integer[ids.size()]);
    }

    public int getCustomRepairItemId() {
        if (getRepairData() == null)
            return -1;
        int orignalItemId = getRepairData()[0];
        if (ItemDefinitions.getItemDefinitions(orignalItemId).getCSOpcode(2655) > 0 && (name.toLowerCase().contains("malevolent") || name.toLowerCase().contains("sirenic") || name.toLowerCase().contains("tectonic")) && !name.toLowerCase().contains("elite"))
            return ItemDefinitions.getItemDefinitions(orignalItemId).getCSOpcode(2655);
        else
            return -1;
    }

    public int getMaxCharges() {
        int itemId = getName().contains("Augmented") ? getChargedItemId() : id;
        if (((itemId >= 18349 && itemId <= 18374) || (itemId >= 25991 && itemId <= 25996) || (itemId >= 27069 && itemId <= 27072) || (itemId >= 31463 && itemId <= 31464) || (itemId >= 34069 && itemId <= 34096) || (itemId >= 36869 && itemId <= 36880) || (itemId >= 43416 && itemId <= 43437)))
            return 30000;
        if (itemId >= 22458 && itemId <= 22497)
            return name.toLowerCase().contains("polypore staff") ? 3000 : 30000;
        if (name.toLowerCase().contains("royal crossbow"))
            return 30000;
        if (name.toLowerCase().contains("ring of wealth (c)"))
            return 100000;
        int maxCharges = (ItemDefinitions.getItemDefinitions(itemId).getCSOpcode(3109) != 0 ? ItemDefinitions.getItemDefinitions(itemId).getCSOpcode(3109) : ItemDefinitions.getItemDefinitions(itemId).getCSOpcode(3385));
        if (maxCharges == 0 && getName().toLowerCase().contains("crystal "))
            maxCharges = 50000;
        if (ItemDefinitions.getItemDefinitions(itemId).getCSOpcode(1327) != 0 || ItemDefinitions.getItemDefinitions(itemId).getCSOpcode(3792) != 0)
            return 60000;

        return maxCharges;
    }

    public boolean usesDoubleCharges() {
        return getCSOpcode(1327) != 0 || getCSOpcode(3792) != 0;
    }

    public int getRepairPrice() {
        if ((name.toLowerCase().contains("sirenic") || name.toLowerCase().contains("tectonic")) && name.toLowerCase().contains("elite"))
            return 100000000;
        ItemDefinitions item2 = null;
        int int3 = 0;
        if (getCSOpcode(1324) == 0 && script_12070() == 1) {
            int3 = 200000;
        }
        if (getCSOpcode(3109) > 0) {
            int3 = getCSOpcode(3114);
            if (int3 < 1) {
                switch (equipSlot) {
                case 17:
                    int3 = 250000;
                    break;
                case 0:
                    int3 = ItemDefinitions.getItemDefinitions(26322).getCSOpcode(3114);
                    break;
                case 4:
                    int3 = ItemDefinitions.getItemDefinitions(26323).getCSOpcode(3114);
                    break;
                case 7:
                    int3 = ItemDefinitions.getItemDefinitions(26324).getCSOpcode(3114);
                    break;
                case 9:
                    int3 = ItemDefinitions.getItemDefinitions(38812).getCSOpcode(3114);
                    break;
                case 10:
                    int3 = ItemDefinitions.getItemDefinitions(38806).getCSOpcode(3114);
                    break;
                }
            }
            if (int3 < 1) {
                return 0;
            }
        }
        if (getCSOpcode(3793) == 0) {
            if (clientScriptData.get(1327) != null) {
                int3 = getCSOpcode(1328);
            } else if (getCSOpcode(1079) > 0) {
                int3 = getCSOpcode(1079);
            } else if (clientScriptData.get(3792) != null) {
                int3 = getCSOpcode(3230);
                if (int3 < 1) {
                    int3 = ItemDefinitions.getItemDefinitions(getCSOpcode(3792)).getCSOpcode(3230);
                }
            } else if (clientScriptData.get(88) != null) {
                switch (equipSlot) {
                case 0:
                    int3 = 180000;
                    break;
                case 4:
                    int3 = 270000;
                    break;
                case 7:
                    int3 = 240000;
                    break;
                case 3:
                    int3 = 300000;
                    break;
                }
            } else if (id == 24338) {
                int3 = 200000;
            } else if (getCSOpcode(5635) > 0) {
                int3 = 200000;
            } else if (getCSOpcode(5722) > 0) {
                int3 = 200000;
            } else if (getCSOpcode(3385) > 0 && getCSOpcode(4563) == 0) {
                if (getCSOpcode(4656) == 1) {
                    int3 = 1000000;// bigest 5253
                } else {
                    int3 = getCSOpcode(3383);
                    if (int3 == -2) {
                        int3 = 200000;
                    } else if (int3 < 1) {
                        item2 = ItemDefinitions.getItemDefinitions(getCSOpcode(3382));
                        if (item2 != null) {
                            int3 = item2.getCSOpcode(3383);
                            if (int3 < 1) {
                                item2 = ItemDefinitions.getItemDefinitions(getCSOpcode(3382));
                                if (item2 != null) {
                                    int3 = item2.getCSOpcode(3383);
                                }
                            }
                        }
                    }
                }
            }
        }
        if (int3 < 1) {
            return -1;
        }
        return int3;
    }

    public int script_12070() {
        if (getCSOpcode(5524) != 0 && stackable == 2) {
            return 1;
        }
        return 0;
    }

    public int getUnchargedItemId() {
        if (id == 39654)
            return id;
        if (id == 39653)
            return id + 1;
        if (id == 39650)
            return id;
        if (id == 39649)
            return id + 1;
        if (id == 36953)
            return id + 1;
        if (id == 36954)
            return id;
        if ((getName().toLowerCase().contains("augmented") && !getName().toLowerCase().contains("uncharged")) || id == 36399)
            return id + 1;
        if ((getName().toLowerCase().contains("augmented") && getName().toLowerCase().contains("uncharged")) || id == 36400)
            return id;
        return -1;
    }

    public int getChargedItemId() {
        if (id == 39653)
            return id;
        if (id == 39654)
            return id - 1;
        if (id == 39649)
            return id;
        if (id == 39650)
            return id - 1;
        if (id == 36953)
            return id;
        if (id == 36954)
            return id - 1;
        if ((getName().toLowerCase().contains("augmented") && !getName().toLowerCase().contains("uncharged")) || id == 36399)
            return id;
        if ((getName().toLowerCase().contains("augmented") && getName().toLowerCase().contains("uncharged")) || id == 36400)
            return id - 1;
        return -1;
    }

    public boolean usesChargesInside() {
        return getCSOpcode(5772) == 1;
    }

    public int getDegradeToDustOriginalItemId() {
        switch (id) {
        case 13860:
            return 13858;
        case 13863:
            return 13861;
        case 13866:
            return 13864;
        case 13869:
            return 13867;
        case 13872:
            return 13870;
        case 13875:
            return 13873;
        case 13878:
            return 13876;
        case 13886:
            return 13884;
        case 13889:
            return 13887;
        case 13892:
            return 13890;
        case 13895:
            return 13893;
        case 13898:
            return 13896;
        case 13901:
            return 13899;
        case 13904:
            return 13902;
        case 13907:
            return 13905;
        case 28610:
            return 28608;
        case 28613:
            return 28611;
        case 28616:
            return 28614;
        case 29856:
            return 29854;
        case 29859:
            return 29857;
        case 29862:
            return 29860;
        case 30007:
            return 30005;
        case 30010:
            return 30008;
        case 30013:
            return 30011;
        case 30215:
            return 30213;
        case 30570:
            return 30568;
        case 30578:
            return 30576;
        case 31191:
            return 31189;
        case 31205:
            return 31203;
        case 38365:
            return 38364;
        case 38367:
            return 38366;
        case 38369:
            return 38368;
        case 38371:
            return 38370;
        case 38373:
            return 38372;
        case 38375:
            return 38374;
        case 38377:
            return 38376;
        case 38379:
            return 38378;
        case 38381:
            return 38380;
        case 38383:
            return 38382;
        case 38385:
            return 38384;
        case 38387:
            return 38386;
        case 38389:
            return 38388;
        case 38391:
            return 38390;
        case 38393:
            return 38392;
        case 39086:
            return 39085;
        case 39090:
            return 39089;
        case 39094:
            return 39093;
        case 39096:
            return 39095;
        case 39100:
            return 39099;
        case 39104:
            return 39103;
        case 39108:
            return 39107;
        case 39112:
            return 39111;
        case 39116:
            return 39115;
        case 39118:
            return 39117;
        case 39122:
            return 39121;
        case 39126:
            return 39125;
        case 39130:
            return 39129;
        case 39134:
            return 39133;
        case 39138:
            return 39137;
        case 39140:
            return 39139;
        case 39144:
            return 39143;
        case 43157:
            return 43155;
        case 43160:
            return 43158;
        case 43163:
            return 43161;
        case 43168:
            return 43166;
        case 43171:
            return 43169;
        case 43174:
            return 43172;
        }
        return -1;
    }

    public int getTeamId() {
        return teamId;
    }

    public boolean canBeBanked() {
        return getCSOpcode(59) == 0 && getCSOpcode(1047) == 0;
    }

    public boolean hasCustomVarItem() {
        return this.stackable == 2 || getId() == 29492 || getId() == 29494;
    }

    public String getExtraBankActionOption() {
        if (getEquipSlot() != -1 && getCSOpcode(3845) == 0 && !ClientScriptMap.getMap(15589).getValues().containsKey((long) getId()))
            return inventoryOptions[1];
        return null;
    }

    public boolean hasSpecialAttack() {
        return getCSOpcode(4329) == 1;
    }

    public boolean canBeOverridedBy(Item cosmeticItem) {
        if (cosmeticItem == null)
            return false;
        int int17 = -1;
        int item20 = cosmeticItem.getDefinitions().getEquipSlot();
        int attrmap16 = -1;
        int attrmap15 = -1;
        int item14 = getId();
        if (item20 == 3) {
            attrmap16 = cosmeticItem.getDefinitions().getCombatMap() == null ? -1 : cosmeticItem.getDefinitions().getCombatMap().getId();
            int17 = ClientScriptMap.getMap(7365).getIntValue(attrmap16);
            attrmap15 = getCombatMap() == null ? -1 : getCombatMap().getId();
        } else if (item20 == 5) {
            attrmap16 = cosmeticItem.getDefinitions().getCombatMap() == null ? -1 : cosmeticItem.getDefinitions().getCombatMap().getId();
            int17 = ClientScriptMap.getMap(7366).getIntValue(attrmap16);
            attrmap15 = getCombatMap() == null ? -1 : getCombatMap().getId();
        }
        if (int17 != -1) {
            int int30 = ClientScriptMap.getMap(7365).getDuplicates(int17).length;
            int int13 = 0;
            int int29 = -1;
            while (int29 < int30) {
                int29 = int29 + 1;
                try {
                    attrmap16 = ClientScriptMap.getMap(7365).getDuplicates(int17)[int29];
                    if (attrmap16 == attrmap15) {
                        int13 = 1;
                    }
                } catch (Exception e) {
                    break;
                }
            }
            int30 = ClientScriptMap.getMap(7366).getDuplicates(int17).length;
            int29 = -1;
            while (int29 < int30) {
                int29 = int29 + 1;
                try {
                    attrmap16 = ClientScriptMap.getMap(7366).getDuplicates(int17)[int29];
                    if (attrmap16 == attrmap15) {
                        int13 = 1;
                    }
                } catch (Exception e) {
                    break;
                }
            }
            return int13 != 0;
        }
        return true;
    }
}