package com.rs.game.player;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.rs.cache.Cache;
import com.rs.cache.loaders.BodyDefinitions;
import com.rs.cache.loaders.ClientScriptMap;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.game.HeadIcon;
import com.rs.game.World;
import com.rs.game.activites.creations.StealingCreation;
import com.rs.game.activities.seasonalevents.SeasonalEventManager;
import com.rs.game.activities.seasonalevents.christmas.ChristmasSeasonalEvent;
import com.rs.game.item.Item;
import com.rs.game.player.client.Native950Appearance;
import com.rs.game.player.client.Native950EquipmentTypes;
import com.rs.game.player.client.Native950EquipmentAnimations;
import com.rs.game.player.content.SkillCapeCustomizer;
import com.rs.game.player.content.TaskTab;
import com.rs.game.player.content.clans.ClansManager;
import com.rs.game.player.content.dungeoneering.GorajanTrailblazer;
import com.rs.game.player.content.titles.PlayerTitle;
import com.rs.game.player.controllers.DungeonController;
import com.rs.network.io.OutputStream;
import com.rs.utils.Utils;

import lombok.Getter;

public class GlobalPlayerUpdater implements Serializable {

    private static final long serialVersionUID = 7655608569741626586L;
    /**
     * The cosmetic items
     */
    public Item[] cosmeticItems;
    private transient int renderEmote;
    private int title;

    @Getter
    private int[] bodyStyle;
    private byte[] colour;
    private boolean male;
    private transient boolean glowRed;
    private transient byte[] appeareanceData;
    private transient byte[] md5AppeareanceDataHash;
    private transient short transformedNpcId;
    private transient boolean native950LivingDeath;
    public boolean isNative950LivingDeath(){return native950LivingDeath;}
    public void setNative950LivingDeath(boolean active){
        if(active&&!com.rs.game.player.client.Native950LivingDeathAppearance.verified())return;
        if(native950LivingDeath==active)return;
        native950LivingDeath=active;generateAppearenceData();
    }
    private transient short transformedItemId;
    private transient boolean hidePlayer;
    private transient Player player;
    private boolean resetAppearanceForRS3;

    public GlobalPlayerUpdater() {
        male = true;
        renderEmote = -1;
        title = -1;

        resetAppearence();
    }

    public static String getTitle(final boolean male, final int title) {
        return title == 0 ? null : ClientScriptMap.getMap(male ? 1093 : 3872).getStringValue(title);
    }

    public boolean isTitleAfterName() {
        return title == 164 || title == 296 || title == 335 || title == 337 || title == 188 || title == 75
                || title == 78 || title == 84 || title == 88 || title == 95 || title == 162 || title == 177
                || title == 260 || title == 362 || title == 365 || title == 367 || title >= 369 && title <= 373
                || title == 375 || title >= 377 && title <= 378 || title == 380 || title >= 32 && title <= 37
                || title >= 102 && title <= 110 || title >= 121 && title <= 125 || title >= 134 && title <= 142
                || title >= 247 && title <= 248 || title >= 169 && title <= 173 || title >= 211 && title <= 219
                || title >= 270 && title <= 279 || title >= 300 && title <= 301 || title >= 303 && title <= 313
                || title >= 352 && title <= 353 || title >= 326 && title <= 327 || title >= 330 && title <= 332
                || title >= 59 && title <= 63 || title >= 72 && title <= 73 || title >= 126 && title <= 129
                || title >= 359 && title <= 360;

        // return isTitleAfterName(title);
    }

    public static boolean isTitleAfterName(int title) {
        return title >= 32 && title <= 37;
    }

    public boolean isNPC() {
        return transformedNpcId != -1;
    }

    public void copyColors(final short[] colors) {
        for (byte i = 0; i < colour.length; i = (byte) (i + 1)) {
            if (colors[i] != -1) {
                colour[i] = (byte) colors[i];
            }
        }
    }



    public void generateAppearenceData() {
        if (player != null && player.isNative950()) {
            // P4: native players get the verified 947 body with no TaskTab / varc /
            // prayer side effects; an unverifiable body keeps the previous one.
            final byte[] nativeBody = buildNative950AppearanceData(native947Name());
            if (nativeBody == null)
                return;
            appeareanceData = nativeBody;
            md5AppeareanceDataHash = Utils.encryptUsingMD5(nativeBody);
            return;
        }
        final byte[] appeareanceData = buildAppearenceData(player.getDisplayName(), true);
        final byte[] md5Hash = Utils.encryptUsingMD5(appeareanceData);
        this.appeareanceData = appeareanceData;
        md5AppeareanceDataHash = md5Hash;
        TaskTab.sendTab(player);
    }

    // ------------------------------------------------------------ native 947 body

    /** Logical wear slots in the verified 947-3 appearance body (16 of them are transmitted). */
    public static final int NATIVE947_SLOT_COUNT = 19;
    /** Unarmed animation set (BAS) verified for the selected 947 cache ({@code 2/32/2699}). */
    public static final int NATIVE947_UNARMED_BAS = 2699;
    /** Item parameter carrying the worn-weapon animation set in the 947 item definitions. */
    public static final int NATIVE947_BAS_PARAM = 644;
    /** Slot that holds the main-hand weapon in both the 910 container and the 947 body. */
    private static final int NATIVE947_WEAPON_SLOT = 3;

    private transient long native947WithheldBodies;
    private transient String native947LastWithheldReason;

    /** Bodies that generateAppearenceData refused to replace because a worn item or the cache was unverified. */
    public long getNative950WithheldBodies() {
        return native947WithheldBodies;
    }

    public String getNative950LastWithheldReason() {
        return native947LastWithheldReason;
    }

    private byte[] withholdNative950Body(String reason) {
        native947WithheldBodies++;
        native947LastWithheldReason = reason;
        if (native947WithheldBodies == 1 || native947WithheldBodies % 500 == 0)
            System.out.println("[Ataraxia950] Appearance body withheld for " + player.getUsername() + ": " + reason
                    + " (" + native947WithheldBodies + " so far)");
        return null;
    }

    /**
     * Wear positions the 947 body is keyed on. They come from {@code 28/6/0} of the
     * selected cache through {@link BodyDefinitions#disabledSlots} (the same file
     * the Kotlin handoff pins by SHA-256 and {@code Native950Appearance} checks
     * literally); a slot whose default is 1 is not transmitted. Loaded lazily on
     * the flat cache because {@code BodyDefinitions.init} is a ServerLauncher step
     * that the 947 bootstrap excludes; tests assign the array directly.
     */
    static int[] native947WearPositions() {
        int[] slots = BodyDefinitions.disabledSlots;
        if (slots == null && Cache.isFlatReadOnly()) {
            synchronized (BodyDefinitions.class) {
                if (BodyDefinitions.disabledSlots == null)
                    BodyDefinitions.init();
                slots = BodyDefinitions.disabledSlots;
            }
        }
        return slots;
    }

    /**
     * Builds native950 appearance from the real equipment container. Equipment metadata,
     * model dependencies, hidden slots and normal/combat BAS profiles come from the selected
     * cache. The existing 19-slot LEB128 layout, gender flag, colours and zero customization
     * blocks remain unchanged. Unsupported/malformed assets or conflicting slots withhold
     * the new body instead of sending an invalid definition to the client.
     */
    /** 950 reads each wearpos slot as an unsigned LEB128 varint; see Native950Appearance. */
    private static void writeWearposSlot(OutputStream stream, int value) {
        for (byte encoded : Native950Appearance.wearposSlot(value))
            stream.writeByte(encoded & 0xFF);
    }

    byte[] buildNative950AppearanceData(String displayName) {
        int[] wearPositions = native947WearPositions();
        if (wearPositions == null || wearPositions.length != NATIVE947_SLOT_COUNT)
            return withholdNative950Body("wear positions unavailable or not the 19-slot950 table");
        if (transformedNpcId >= 0)
            return withholdNative950Body("NPC transform " + transformedNpcId + " has no verified950 encoding");
        if (displayName == null || displayName.isEmpty() || displayName.length() > 12)
            return withholdNative950Body("name must be 1..12 characters");
        for (int i = 0; i < displayName.length(); i++) {
            char character = displayName.charAt(i);
            if (character < 32 || character > 126)
                return withholdNative950Body("name must be printable ASCII");
        }
        final Item[] worn = player.getEquipment().getItems().getItems();
        final int[] items = new int[NATIVE947_SLOT_COUNT];
        final boolean[] hidden = new boolean[NATIVE947_SLOT_COUNT];
        java.util.Arrays.fill(items, -1);
        int bas = NATIVE947_UNARMED_BAS;
        final boolean cacheBacked = Cache.isFlatReadOnly();
        final Native950EquipmentTypes.Type[] types = new Native950EquipmentTypes.Type[NATIVE947_SLOT_COUNT];
        for (int slot = 0; slot < NATIVE947_SLOT_COUNT && slot < worn.length; slot++) {
            final Item item = worn[slot];
            if (item == null) continue;
            int hide1, hide2;
            if (cacheBacked) {
                Native950EquipmentTypes.Type type = Native950EquipmentTypes.resolve(item.getId());
                if (type == null || type.slot != slot || !type.genderSupported(male))
                    return withholdNative950Body("invalid950 worn definition/assets for item " + item.getId() + " in slot " + slot);
                types[slot] = type;
                hide1 = type.hide1; hide2 = type.hide2;
            } else {
                // Cache-free protocol fixtures retain their explicitly supplied strict definitions.
                final ItemDefinitions defs = ItemDefinitions.getItemDefinitions(item.getId());
                if (defs == null || !defs.loaded || defs.decodeFailure != null || defs.equipSlot != slot)
                    return withholdNative950Body("invalid worn fixture for item " + item.getId() + " in slot " + slot);
                hide1 = defs.getEquipType(); hide2 = defs.getEquipType2();
                if (slot == NATIVE947_WEAPON_SLOT && defs.clientScriptData != null) {
                    Object animSet = defs.clientScriptData.get(NATIVE947_BAS_PARAM);
                    if (animSet instanceof Integer && (Integer) animSet >= 0 && (Integer) animSet < 65535)
                        bas = (Integer) animSet;
                }
            }
            items[slot] = item.getId();
            if (hide1 >= 0 && hide1 < NATIVE947_SLOT_COUNT) hidden[hide1] = true;
            if (hide2 >= 0 && hide2 < NATIVE947_SLOT_COUNT) hidden[hide2] = true;
        }
        if (cacheBacked) {
            for (int slot = 0; slot < NATIVE947_SLOT_COUNT; slot++)
                if (hidden[slot] && items[slot] >= 0)
                    return withholdNative950Body("conflicting950 equipped slot " + slot);
            boolean combat = player.getCombatDefinitions().isCombatStance();
            Native950EquipmentTypes.Type hand = types[3] != null ? types[3] : types[5];
            bas = hand == null ? (combat ? 2688 : NATIVE947_UNARMED_BAS) : combat ? hand.combatBas : hand.bas;
            if (!Native950EquipmentAnimations.validateBas(bas))
                return withholdNative950Body("missing or invalid950 movement set " + bas);
        }
        if (com.rs.game.player.client.Native950Agility.renderOverride(renderEmote)) bas = renderEmote;
        final OutputStream stream = new OutputStream();
        int flag = 0;
        flag |= !male ? 0x1 : 0;
        // 910 sets 0x4 (show total level instead of combat level) from the sheathe
        // toggle. The verified 947 template always uses the combat-level form, and
        // the sheathe/combat-stance settings are only bound in M10, so the native
        // body keeps 0x4 clear until then.
        final boolean showSkillLevel = false;
        flag |= (player.getSize() - 1) << 3;
        if (title != -1)
            flag |= 0x40;
        stream.writeByte(flag);
        if (title != -1)
            stream.writeSmart(title);
        stream.writeByte(hidePlayer ? 1 : 0);
        // Exact950 reader0x140131cb8 / 0x140131eba: slot0 sentinel1, big-smart NPC,
        // render type byte; morphs skip remaining slots AND item-customisation mask.
        if(native950LivingDeath){
            writeWearposSlot(stream,1);stream.writeBigSmart(30268);stream.writeByte(0);
        }else {
        for (int slot = 0; slot < NATIVE947_SLOT_COUNT; slot++) {
            if (wearPositions[slot] == 1)
                continue;
            if (items[slot] >= 0) {
                writeWearposSlot(stream, Native950Appearance.ITEM_BASE + items[slot]);
                continue;
            }
            int kit = hidden[slot] ? -1 : native947BodyKit(slot);
            if (kit >= 0)
                writeWearposSlot(stream, Native950Appearance.KIT_BASE + kit);
            else
                writeWearposSlot(stream, 0);
        }
        stream.writeShort(0); // no item model / recolour customization blocks
        }
        for (int index = 0; index < colour.length; index++)
            stream.writeByte(colour[index]);
        for (int index = 0; index < 10; index++)
            stream.writeByte(0);
        stream.writeShort(bas);
        stream.writeString(displayName);
        final boolean pvpArea = World.isPvpArea(player);
        stream.writeByte(pvpArea ? player.getSkills().getCombatLevel() : player.getSkills().getCombatLevelWithSummoning());
        if (showSkillLevel)
            stream.writeShort(player.getSkills().getTotalLevel());
        else {
            stream.writeByte(pvpArea ? player.getSkills().getCombatLevelWithSummoning() : 0);
            stream.writeByte(-1);
        }
        stream.writeByte(0); // no NPC-details tail
        final byte[] data = new byte[stream.getOffset()];
        System.arraycopy(stream.getBuffer(), 0, data, 0, data.length);
        return data;
    }

    /**
     * Name carried in the native body: an explicitly set display name, otherwise the
     * raw (canonical) username exactly as the verified bypass template sent it. The
     * 910 serializer capitalises through getDisplayName(); that cosmetic difference
     * is deferred to the IDENTITY section owner (M13) so existing smokes stay valid.
     */
    String native947Name() {
        return player.hasDisplayName() ? player.getDisplayName() : player.getUsername();
    }

    /** Same slot -> bodyStyle index table as the 910 serializer (torso 4, arms 6, legs 7, hair 8, wrists 9, feet 10, beard 11). */
    private int native947BodyKit(int slot) {
        int styleIndex;
        switch (slot) {
        case 4: styleIndex = 2; break;
        case 6: styleIndex = 3; break;
        case 7: styleIndex = 5; break;
        case 8: styleIndex = 0; break;
        case 9: styleIndex = 4; break;
        case 10: styleIndex = 6; break;
        case 11: styleIndex = 1; break;
        default: return -1;
        }
        return bodyStyle[styleIndex] > -1 ? bodyStyle[styleIndex] : -1;
    }

    /** Persistence view (P4 APPEARANCE section): copies, never the live arrays. */
    public int[] getBodyStyleCopy() {
        return bodyStyle.clone();
    }

    public int[] getColoursCopy() {
        int[] copy = new int[colour.length];
        for (int i = 0; i < copy.length; i++)
            copy[i] = colour[i] & 0xff;
        return copy;
    }

    /** Restores gender, colours and body kits without regenerating the body or sending anything. */
    public void restoreLook(boolean male, int[] colours, int[] kits) {
        this.male = male;
        for (int i = 0; i < colour.length && i < colours.length; i++)
            colour[i] = (byte) colours[i];
        for (int i = 0; i < bodyStyle.length && i < kits.length; i++)
            bodyStyle[i] = kits[i];
    }

    private byte[] buildAppearenceData(String displayName, boolean sendRenderConfig) {
        final OutputStream stream = new OutputStream();
        @SuppressWarnings("unused")
        final PlayerTitle playerTitle = PlayerTitle.getTitlesById().get(getTitle());
        int flag = 0;
        flag |= !male ? 0x1 : 0;
        boolean showSkillLevel = !player.getCombatDefinitions().isCombatStance() && player.getCombatDefinitions().isSheathe();
        if (showSkillLevel)
            flag |= 0x4;
        flag |= (player.getSize() - 1) << 3;
        if (title != -1)
            flag |= 0x40; // after/before
        stream.writeByte(flag);
        if (title != -1)
            stream.writeSmart(title);
        stream.writeByte(hidePlayer ? 1 : 0);
        stream.writeBytes(getAppearenceLook(sendRenderConfig));
        stream.writeString(displayName);
        final boolean pvpArea = World.isPvpArea(player);
        stream.writeByte(pvpArea ? player.getSkills().getCombatLevel() : player.getSkills().getCombatLevelWithSummoning());
        if (showSkillLevel)
            stream.writeShort(player.getSkills().getTotalLevel());
        else {
            stream.writeByte(pvpArea ? player.getSkills().getCombatLevelWithSummoning() : 0);
            stream.writeByte(-1);
        }
        boolean useNPCDetails = transformedNpcId >= 0;
        if (transformedNpcId >= 0) {
            NPCDefinitions defs = NPCDefinitions.getNPCDefinitions(transformedNpcId);
            HashMap<Integer, Object> data = defs.clientScriptData;
            if (data != null)
                useNPCDetails = !data.containsKey(2805);
        }
        stream.writeByte(useNPCDetails ? 1 : 0); // to end here else id
        if (useNPCDetails) {
            final NPCDefinitions defs = NPCDefinitions.getNPCDefinitions(transformedNpcId);
            stream.writeShort(defs.anInt3029);
            stream.writeShort(defs.anInt3065);
            stream.writeShort(defs.anInt3050);
            stream.writeShort(defs.anInt3042);
            stream.writeByte(defs.anInt3068);
        }
        final byte[] appeareanceData = new byte[stream.getOffset()];
        System.arraycopy(stream.getBuffer(), 0, appeareanceData, 0, appeareanceData.length);
        return appeareanceData;
    }

    public byte[] getAppearenceLook() {
        return getAppearenceLook(true);
    }

    private byte[] getAppearenceLook(boolean sendRenderConfig) {
        return getAppearenceLook(
                                  player.getEquipment().getCosmeticPreviewItems() != null ?
                                  player.getEquipment().getCosmeticPreviewItems().getItems() :
                                  player.getEquipment().getCosmeticItems().getItems(), bodyStyle, sendRenderConfig);
    }

    public byte[] getAppearenceLook(final Item[] cosmetics, final int[] look) {
        return getAppearenceLook(cosmetics, look, true);
    }

    private byte[] getAppearenceLook(final Item[] cosmetics, final int[] look, boolean sendRenderConfig) {
        final OutputStream stream = new OutputStream();

        if (transformedNpcId >= 0) {
            stream.writeShort(-1); // 65535 tells it a npc
            stream.writeBigSmart(transformedNpcId);
            Item cape = player.getEquipment().getItem(Equipment.SLOT_CAPE);
            stream.writeByte(cape != null ? cape.getDefinitions().getTeamId() : 0); // team
        } else {
            final Item[] items = new Item[BodyDefinitions.getEquipmentContainerSize()];
            final boolean[] skipLook = new boolean[items.length];
            for (int index = 0; index < items.length; index++) {
                Item item = player.getEquipment().isCanDisplayCosmetic() ? cosmetics[index] : null;
                if (index == Equipment.SLOT_AURA && player.getEquipment().isCanDisplayCosmetic() && cosmetics[index] != null && player.getAuraManager().isActivated()) {
                    item = null;
                }
                if (player.getControlerManager().getControler() instanceof DungeonController) {
                    switch (index) {
                    case Equipment.SLOT_HAT:
                        if (player.getGorajanTrailblazer().getActivePiece(GorajanTrailblazer.HEAD) != 0) {
                            item = new Item(player.getGorajanTrailblazer().getActivePiece(GorajanTrailblazer.HEAD));
                        }
                        break;
                    case Equipment.SLOT_CHEST:
                        if (player.getGorajanTrailblazer().getActivePiece(GorajanTrailblazer.BODY) != 0) {
                            item = new Item(player.getGorajanTrailblazer().getActivePiece(GorajanTrailblazer.BODY));
                        }
                        break;
                    case Equipment.SLOT_LEGS:
                        if (player.getGorajanTrailblazer().getActivePiece(GorajanTrailblazer.LEGS) != 0) {
                            item = new Item(player.getGorajanTrailblazer().getActivePiece(GorajanTrailblazer.LEGS));
                        }
                        break;
                    case Equipment.SLOT_HANDS:
                        if (player.getGorajanTrailblazer().getActivePiece(GorajanTrailblazer.GLOVES) != 0) {
                            item = new Item(player.getGorajanTrailblazer().getActivePiece(GorajanTrailblazer.GLOVES));
                        }
                        break;
                    case Equipment.SLOT_FEET:
                        if (player.getGorajanTrailblazer().getActivePiece(GorajanTrailblazer.BOOTS) != 0) {
                            item = new Item(player.getGorajanTrailblazer().getActivePiece(GorajanTrailblazer.BOOTS));
                        }
                        break;
                    }
                }
                if ((index == 3 || index == 5) && item != null && player.getEquipment().getCosmeticItems().getItems() == cosmetics) {
                    final Item originalWeapon = player.getEquipment().getItem(index);
                    if (originalWeapon == null) {
                        item = null;
                    } else {

                        if (index == 5 && originalWeapon.getDefinitions().isShield() != item.getDefinitions().isShield())
                            item = null;
                        if (item != null && !originalWeapon.getDefinitions().canBeOverridedBy(item)) {
                            item = null;
                        }
                    }
                }
                if (item == null && player.getEquipment().getCosmeticPreviewItems() == null) {
                    item = player.getEquipment().getItems().get(index);
                }
                if (item != null && player.getOverrides() != null && player.getOverrides().retroCapes && player.getOverrides().getRetroCapeId(item.getId()) != -1) {
                    final int retroId = player.getOverrides().getRetroCapeId(item.getId());
                    item = new Item(retroId);
                }
                if (!(player.getControlerManager().getControler() instanceof DungeonController) && player.getEquipment().isCanDisplayCosmetic() && index == Equipment.SLOT_SHIELD) {
                    final Item weapon = items[Equipment.SLOT_WEAPON] != null ? items[Equipment.SLOT_WEAPON] : player.getEquipment().getItems().get(Equipment.SLOT_WEAPON);
                    if (weapon != null && Equipment.isTwoHandedWeapon(weapon)) {
                        item = null;
                    }
                }
                if (index == 15 && player.getEquipment().isCanDisplayCosmetic() && cosmetics[Equipment.SLOT_AURA] != null && player.getAuraManager().isActivated()) {
                    items[index] = cosmetics[Equipment.SLOT_AURA];
                }
                if (index == Equipment.SLOT_HAT && SeasonalEventManager.isActive(ChristmasSeasonalEvent.class)) {
                    LocalDate now = LocalDate.now();
                    int day = now.getDayOfMonth();
                    if (now.getMonth() == Month.DECEMBER && (day == 24 || day == 25)) {
                        item = new Item(1050);
                    }
                }
                if (item != null) {
                    items[index] = item;
                    final int skipSlotLook = item.getDefinitions().getEquipType();
                    if (skipSlotLook != -1) {
                        skipLook[skipSlotLook] = true;
                    }
                    final int skipSlotLook2 = item.getDefinitions().getEquipType2();
                    if (skipSlotLook2 != -1) {
                        skipLook[skipSlotLook2] = true;
                    }
                }
            }
            for (int index = 0; index < items.length; index++) {
                if (BodyDefinitions.disabledSlots[index] != 0) {
                    continue;
                }
                if (glowRed) {
                    if (index == 0) {
                        stream.writeShort(2048 + 2910);
                        continue;
                    }
                    if (index == 1) {
                        stream.writeShort(2048 + 14641);
                        continue;
                    }
                    if (index == Equipment.SLOT_LEGS) {
                        stream.writeShort(2048 + 2908);
                        continue;
                    }
                    if (index == Equipment.SLOT_HANDS) {
                        stream.writeShort(2048 + 2912);
                        continue;
                    }
                    if (index == Equipment.SLOT_FEET) {
                        stream.writeShort(2048 + 2904);
                        continue;
                    }
                }
                if (items[index] != null && items[index].getDefinitions().equipSlot != -1) {
                    stream.writeShort(2048 + items[index].getId());
                    continue;
                }
                if (!skipLook[index]) {
                    int bodyStylendex = -1;

                    switch (index) {
                    case 4:
                        bodyStylendex = 2;
                        break;
                    case 6:
                        bodyStylendex = 3;
                        break;
                    case 7:
                        bodyStylendex = 5;
                        break;
                    case 8:
                        bodyStylendex = 0;
                        break;
                    case 9:
                        bodyStylendex = 4;
                        break;
                    case 10:
                        bodyStylendex = 6;
                        break;
                    case 11:
                        bodyStylendex = 1;
                        break;
                    }
                    if (bodyStylendex != -1 && bodyStyle[bodyStylendex] > -1) {
                        stream.writeShort(0x100 + bodyStyle[bodyStylendex]);
                        continue;
                    }
                }
                stream.writeByte(0);
            }

            final OutputStream streamModify = new OutputStream();
            int modifyFlag = 0;
            int slotIndex = -1;
            final ItemModify[] modify = generateItemModify(items, cosmetics);
            for (int index = 0; index < modify.length; index++) {
                if (BodyDefinitions.disabledSlots[index] != 0) {
                    continue;
                }
                slotIndex++;
                final ItemModify im = modify[index];
                if (im == null) {
                    continue;
                }
                modifyFlag |= 1 << slotIndex;
                int itemFlag = 0;
                final OutputStream streamItem = new OutputStream();
                if (im.maleModelId1 != -1 || im.femaleModelId1 != -1) {
                    itemFlag |= 0x1;
                    streamItem.writeBigSmart(im.maleModelId1);
                    streamItem.writeBigSmart(im.femaleModelId1);
                    if (im.maleModelId2 != -2 || im.femaleModelId2 != -2) {
                        streamItem.writeBigSmart(im.maleModelId2);
                        streamItem.writeBigSmart(im.femaleModelId2);
                    }
                    if (im.maleModelId3 != -2 || im.femaleModelId3 != -2) {
                        streamItem.writeBigSmart(im.maleModelId3);
                        streamItem.writeBigSmart(im.femaleModelId3);
                    }
                }
                if (im.colors != null) {
                    itemFlag |= 0x4;
                    streamItem.writeShort(0 | 1 << 4 | 2 << 8 | 3 << 12);
                    for (int i = 0; i < 4; i++) {
                        streamItem.writeShort(im.colors[i]);
                    }
                }
                if (im.textures != null) {
                    itemFlag |= 0x8;
                    streamItem.writeByte(0 | 1 << 4);
                    for (int i = 0; i < 2; i++) {
                        streamItem.writeShort(im.textures[i]);
                    }
                }
                streamModify.writeByte(itemFlag);
                streamModify.writeBytes(streamItem.getBuffer(), 0, streamItem.getOffset());
            }
            stream.writeShort(modifyFlag);
            stream.writeBytes(streamModify.getBuffer(), 0, streamModify.getOffset());
//            final OutputStream streamDyes = new OutputStream();
//            modifyFlag = 0;
//            slotIndex = -1;
//            final ItemDye[] itemDyes = getItemDyes(items);
//            for (int index = 0; index < modify.length; index++) {
//                if (Equipment.DISABLED_SLOTS[index] != 0) {
//                    continue;
//                }
//                slotIndex++;
//                final ItemDye im = itemDyes[index];
//                if (im == null) {
//                    continue;
//                }
//                modifyFlag |= 1 << slotIndex;
//                int itemFlag = 0;
//                final OutputStream streamItem = new OutputStream();
//                if (im.equipOriginalColours != null) {
//                    itemFlag |= 0x4;
//                    streamItem.writeByte(im.equipOriginalColours.length);
//                    for (int i = 0; i < im.equipOriginalColours.length; i++) {
//                        streamItem.writeShort(im.equipOriginalColours[i]);
//                        streamItem.writeShort(im.equipReplacementColours[i]);
//                    }
//                }
//                if (im.equipOriginalTextures != null) {
//                    itemFlag |= 0x8;
//                    streamItem.writeByte(im.equipOriginalTextures.length);
//                    for (int i = 0; i < im.equipOriginalTextures.length; i++) {
//                        streamItem.writeShort(im.equipOriginalTextures[i]);
//                        streamItem.writeShort(im.equipReplacementTextures[i]);
//                    }
//                }
//                if (im.suffix != null) {
//                    itemFlag |= 0x10;
//                    streamItem.writeString(im.suffix);
//                }
//                streamDyes.writeByte(itemFlag);
//                streamDyes.writeBytes(streamItem.getBuffer(), 0, streamItem.getOffset());
//            }
//            stream.writeShort(modifyFlag);
//            stream.writeBytes(streamDyes.getBuffer(), 0, streamDyes.getOffset());
        }
        for (int index = 0; index < colour.length; index++) {
            stream.writeByte(colour[index]);
        }
        for (int index = 0; index < 10; index++)
            stream.writeByte(0);
        int renderEmote = getRenderEmote();
        stream.writeShort(renderEmote);
        if (sendRenderConfig) {
            player.getPackets().sendGlobalConfig(779, renderEmote);
        }
        final byte[] data = new byte[stream.getOffset()];
        System.arraycopy(stream.getBuffer(), 0, data, 0, data.length);
        return data;
    }

//    private ItemDye[] getItemDyes(Item[] items) {
//        ItemDye[] itemDyes = new ItemDye[items.length];
//        for (int i = 0; i < itemDyes.length; i++) {
//            if (items[i] == null || items[i].getDyeData() == null)
//                continue;
//            itemDyes[i] = items[i].getDyeData();
//        }
//        return itemDyes;
//    }

    public byte[] getAppeareanceData() {
        return appeareanceData;
    }

    /**
     * Per-viewer body. The legacy branch re-serialises with the viewer-specific
     * name; for a native 947 player it must NOT, because
     * {@link #buildAppearenceData} writes the 910 slot membership and ends with
     * {@code getRenderEmote()} instead of the 947 BAS - a guessed body on the
     * wire. The native branch rebuilds through {@link #buildNative950AppearanceData}
     * with the same name and, when that body cannot be verified, keeps the
     * already-verified {@link #appeareanceData} (counted by
     * {@link #getNative950WithheldBodies()}) rather than falling through.
     */
    public byte[] getAppeareanceData(Player viewer) {
        String viewerDisplayName = getViewerDisplayName(viewer);
        if (viewerDisplayName == null)
            return appeareanceData;
        if (player != null && player.isNative950()) {
            byte[] nativeBody = buildNative950AppearanceData(viewerDisplayName);
            return nativeBody == null ? appeareanceData : nativeBody;
        }
        return buildAppearenceData(viewerDisplayName, false);
    }

    public int getBeardStyle() {
        return bodyStyle[1];
    }

    public void setBeardStyle(final int i) {
        bodyStyle[1] = i;
    }

    public int getFacialHair() {
        return bodyStyle[1];
    }

    public void setFacialHair(final int i) {
        bodyStyle[1] = i;
    }

    public int getHairColor() {
        return colour[0];
    }

    public void setHairColor(final int color) {
        colour[0] = (byte) color;
    }

    public int getHairStyle() {
        return bodyStyle[0];
    }

    public void setHairStyle(final int i) {
        bodyStyle[0] = i;
        if (!male)
            bodyStyle[1] = -1;
    }

    public byte[] getMD5AppeareanceDataHash() {
        return md5AppeareanceDataHash;
    }

    /** Hash of exactly what {@link #getAppeareanceData(Player)} would return for this viewer. */
    public byte[] getMD5AppeareanceDataHash(Player viewer) {
        String viewerDisplayName = getViewerDisplayName(viewer);
        if (viewerDisplayName == null)
            return md5AppeareanceDataHash;
        if (player != null && player.isNative950()) {
            byte[] nativeBody = buildNative950AppearanceData(viewerDisplayName);
            return nativeBody == null ? md5AppeareanceDataHash : Utils.encryptUsingMD5(nativeBody);
        }
        return Utils.encryptUsingMD5(buildAppearenceData(viewerDisplayName, false));
    }

    private String getViewerDisplayName(Player viewer) {
        return StealingCreation.getRightClickDisplayName(viewer, player);
    }

    public int getRenderEmote() {
        if (renderEmote >= 0)
            return renderEmote;
        if (transformedNpcId >= 0) {
            NPCDefinitions defs = NPCDefinitions.getNPCDefinitions(transformedNpcId);
            HashMap<Integer, Object> data = defs.clientScriptData;
            if (data != null && !data.containsKey(2805))
                return defs.renderEmote;
        }
        if (player.getCombatDefinitions().isSheathe() && !player.getCombatDefinitions().isCombatStance())
            return player.getCosmeticsManager().getRenderEmote();
        return player.getEquipment().getWeaponStance();
    }
    public void setRenderEmote(final int id) {
        renderEmote = id;
        generateAppearenceData();
    }

    public int getSize() {
        if (transformedNpcId >= 0) {
            return NPCDefinitions.getNPCDefinitions(transformedNpcId).size;
        }
        return 1;
    }

    public int getSkinColor() {
        return colour[4];
    }

    public void setSkinColor(final int color) {
        colour[4] = (byte) color;
    }

    public int getTopStyle() {
        return bodyStyle[2];
    }

    public void setTopStyle(final int i) {
        bodyStyle[2] = i;
    }

    public boolean isFemale() {
        return !male;
    }

    public boolean isGlowRed() {
        return glowRed;
    }

    public void setGlowRed(final boolean glowRed) {
        this.glowRed = glowRed;
        generateAppearenceData();
    }

    public boolean isHidden() {
        return hidePlayer;
    }

    public void setHidden(boolean hidePlayer) {
        this.hidePlayer = hidePlayer;
        generateAppearenceData();
    }

    public boolean isMale() {
        return male;
    }

    public void setMale(final boolean male) {
        this.male = male;
    }
    
    public void female() {
        bodyStyle[0] = 48; // Hair
        bodyStyle[1] = -1; // Beard
        bodyStyle[2] = 57; // Torso
        bodyStyle[3] = 65; // Arms
        bodyStyle[4] = 68; // Bracelets
        bodyStyle[5] = 77; // Legs
        bodyStyle[6] = 80; // Shoes

        colour[2] = 16;
        colour[1] = 16;
        colour[0] = 3;
        male = false;
        if (player != null)
            player.getEquipment().getCosmeticItems().reset();
    }
    
    public void male() {
        bodyStyle[0] = 3; // Hair
        bodyStyle[1] = 14; // Beard
        bodyStyle[2] = 18; // Torso
        bodyStyle[3] = 26; // Arms
        bodyStyle[4] = 34; // Bracelets
        bodyStyle[5] = 38; // Legs
        bodyStyle[6] = 42; // Shoes~

        colour[2] = 16;
        colour[1] = 16;
        colour[0] = 3;
        male = true;
        if (player != null)
            player.getEquipment().getCosmeticItems().reset();
    }

    public void resetAppearence() {
        bodyStyle = new int[7];
        colour = new byte[10];
        if (cosmeticItems == null) {
            cosmeticItems = new Item[14];
        }
        male();
    }

    public void setArmsStyle(final int i) {
        bodyStyle[3] = i;
    }
    
    public void setHandsStyle(int i) {
        bodyStyle[4] = i;
    }
    
    public void setBootsStyle(int i) {
        bodyStyle[6] = i;
    }
    
    public void setColor(final int i, final int i2) {
        colour[i] = (byte) i2;
    }

    public void setLegsColor(final int color) {
        colour[2] = (byte) color;
    }

    public void setLegsStyle(final int i) {
        bodyStyle[5] = i;
    }

    public void setLook(final int i, final int i2) {
        bodyStyle[i] = i2;
    }

    public void setLooks(final short[] look) {
        for (byte i = 0; i < bodyStyle.length; i = (byte) (i + 1)) {
            if (look[i] != -1) {
                bodyStyle[i] = look[i];
            }
        }
    }

    public void setPlayer(final Player player) {
        this.player = player;
        transformedNpcId = -1;
        renderEmote = -1;
        if (bodyStyle == null || cosmeticItems == null || !resetAppearanceForRS3) {
            resetAppearence();
            if (!resetAppearanceForRS3) {
                title = -1;
                resetAppearanceForRS3 = true;
            }
        }
    }

    public int getTitle() {
        return title;
    }

    public void setTitle(final int title) {
        this.title = title;
        generateAppearenceData();
    }

    public void setTopColor(final int color) {
        colour[1] = (byte) color;
    }

    public void setWristsStyle(final int i) {
        bodyStyle[4] = i;
    }

    public void switchHidden() {
        hidePlayer = !hidePlayer;
        generateAppearenceData();
    }

    public void transformIntoNPC(final int id) {
        transformedNpcId = (short) id;
        if (NPCDefinitions.getNPCDefinitions(transformedNpcId) == null)
            transformedNpcId = -1;
        generateAppearenceData();
    }

    public int getTransformedNpcId() {
        return transformedNpcId;
    }
    public int getTransformedItemId() {
        return transformedItemId;
    }

    public void transformIntoItem(int id) {
        transformedItemId = (short) id;
        generateAppearenceData();
    }
    private ItemModify[] generateItemModify(final Item[] items, final Item[] cosmetics) {
        final ItemModify[] modify = new ItemModify[19];
        for (int slotId = 0; slotId < modify.length; slotId++) {
            if ((slotId == Equipment.SLOT_WEAPON || slotId == Equipment.SLOT_SHIELD) && player.getCombatDefinitions().isSheathe() && player.getEquipment().getCosmeticItems().getItems() == cosmetics) {
                final Item item = items[slotId];
                if (item != null) {
                    final int modelId = items[slotId].getDefinitions().getSheatheModelId();
                    setItemModifyModel(items[slotId], slotId, modify, modelId, modelId, -1, -1, -1, -1);
                }
            }
            if (player.getAuraManager().isActivated() && slotId == Equipment.SLOT_AURA) {
                final int auraId = player.getEquipment().getAuraId();
                if (auraId == -1) {
                    continue;
                }
                int equip1 = isFemale() ? ItemDefinitions.getItemDefinitions(auraId).femaleEquip1 : ItemDefinitions.getItemDefinitions(auraId).maleEquip1;
                final int modelId = player.isHideAuraGlow() ? equip1 : player.getAuraManager().getAuraModelId();
                final int modelId2 = player.getAuraManager().getAuraModelId2();
                setItemModifyModel(items[slotId], slotId, modify, modelId, modelId, modelId2, modelId2, -1, -1);
            }
            if (items[slotId] != null && items[slotId] == cosmetics[slotId]) {
                final int id = cosmetics[slotId] == null ? -1 : cosmetics[slotId].getId();
                if (id == -1) {
                    continue;
                }
                final ItemDefinitions def = ItemDefinitions.getItemDefinitions(id);
                if (SkillCapeCustomizer.isCustomizable(id)) {
                    int type = def.getCSOpcode(7896);
                    setItemModifyColor(items[slotId], slotId, modify, type == 1 ? player.getMaxedCapeCustomized() : type == 2 ? player.getCompletionistCapeCustomized() : player.getTrimmedCompletionistCapeCustomized());
                } else {
                    int[] colors = items[slotId].getCustomColor();
                    if (colors == null) {
                        colors = new int[4];
                        colors[0] = 0;
                        colors[1] = colors[0] + 12;
                        colors[2] = colors[1] + 12;
                        colors[3] = colors[2] + 12;
                    }
                    setItemModifyColor(items[slotId], slotId, modify, colors);
                }
            } else {
                final int id = items[slotId] == null ? -1 : items[slotId].getId();
                final ItemDefinitions def = ItemDefinitions.getItemDefinitions(id);
                if (SkillCapeCustomizer.isCustomizable(id)) {
                    int type = def.getCSOpcode(7896);
                    setItemModifyColor(items[slotId], slotId, modify, type == 1 ? player.getMaxedCapeCustomized() : type == 2 ? player.getCompletionistCapeCustomized() : player.getTrimmedCompletionistCapeCustomized());
                } else if (id == 20708 || id == 20709) {
                    final ClansManager manager = player.getClanManager();
                    if (manager == null) {
                        continue;
                    }
                    final int[] colors = manager.getClan().getMottifColors();
                    setItemModifyColor(items[slotId], slotId, modify, colors);
                    setItemModifyTexture(items[slotId], slotId, modify, new short[] { (short) ClansManager.getMottifTexture(manager.getClan().getMottifTop()), (short) ClansManager.getMottifTexture(manager.getClan().getMottifBottom()) });
                }
            }
        }
        return modify;
    }

    private void setItemModifyModel(final Item item, final int slotId, final ItemModify[] modify, final int maleModelId1, final int femaleModelId1, final int maleModelId2, final int femaleModelId2, final int maleModelId3, final int femaleModelId3) {
        if (item == null) {
            return;
        }
        final ItemDefinitions defs = item.getDefinitions();
        if (defs.getMaleWornModelId1() == -1 || defs.getFemaleWornModelId1() == -1) {
            return;
        }
        if (modify[slotId] == null) {
            modify[slotId] = new ItemModify();
        }
        modify[slotId].maleModelId1 = maleModelId1;
        modify[slotId].femaleModelId1 = femaleModelId1;
        if (defs.getMaleWornModelId2() != -1 || defs.getFemaleWornModelId2() != -1) {
            modify[slotId].maleModelId2 = maleModelId2;
            modify[slotId].femaleModelId2 = femaleModelId2;
        }
        if (defs.getMaleWornModelId3() != -1 || defs.getFemaleWornModelId3() != -1) {
            modify[slotId].maleModelId2 = maleModelId3;
            modify[slotId].femaleModelId2 = femaleModelId3;
        }
    }

    private void setItemModifyTexture(final Item item, final int slotId, final ItemModify[] modify, final short[] textures) {
        final ItemDefinitions defs = item.getDefinitions();
        if (defs.originalTextureColors == null || defs.originalTextureColors.length != textures.length) {
            return;
        }
        if (Arrays.equals(textures, defs.originalTextureColors)) {
            return;
        }
        if (modify[slotId] == null) {
            modify[slotId] = new ItemModify();
        }
        modify[slotId].textures = textures;
    }

    private void setItemModifyColor(final Item item, final int slotId, final ItemModify[] modify, final int[] colors) {
        final ItemDefinitions defs = item.getDefinitions();
        if (defs.originalModelColors == null || defs.originalModelColors.length != colors.length) {
            return;
        }
        if (Arrays.equals(colors, defs.originalModelColors)) {
            return;
        }
        if (modify[slotId] == null) {
            modify[slotId] = new ItemModify();
        }
        modify[slotId].colors = colors;
    }

    public int getTitleId() {
        return title;
    }




    private static class ItemModify {

        private final int maleModelId3;
        private int[] colors;
        private short[] textures;
        private int maleModelId1;
        private int femaleModelId1;
        private int maleModelId2;
        private int femaleModelId2;
        private final int femaleModelId3;

        private ItemModify() {
            maleModelId1 = femaleModelId1 = -1;
            maleModelId2 = femaleModelId2 = -2;
            maleModelId3 = femaleModelId3 = -2;
        }
    }

    @SuppressWarnings("unused")
    public static class ItemDye implements Serializable {
        private static final long serialVersionUID = 2146987297565732980L;
        public String suffix;
        public short[] invOriginalColours;
        public short[] invReplacementColours;
        public short[] invOriginalTextures;
        public short[] invReplacementTextures;

        public short[] equipOriginalColours;
        public short[] equipReplacementColours;
        public short[] equipOriginalTextures;
        public short[] equipReplacementTextures;

        public ItemDye() {

        }
    }

    @Getter
    private transient byte[] md5IconsDataHash;
    @Getter
    private transient byte[] iconsData;
    public HeadIcon[] getIcons() {
        List<HeadIcon> icons = new ArrayList<HeadIcon>();

        if (player.hasSkull()) {
            icons.add(new HeadIcon(439, player.getSkullId()));
        }


           // icons.add(new HeadIcon(1455, 34));


        int prayerIcon = player.getPrayer().getPrayerHeadIcon();
        if (prayerIcon >= 0)
            icons.add(new HeadIcon(440, prayerIcon));

        return icons.toArray(new HeadIcon[icons.size()]);
    }


    public void generateIconsData() {
        OutputStream stream = new OutputStream();
        HeadIcon[] icons = getIcons();
        int mask = 0;
        for (int i = 0; i < icons.length; i++)
            mask |= 1 << i;
        stream.writeByte(mask);
        for (HeadIcon icon : icons) {
            stream.writeByte(icon.getFileId());
            stream.writeShort(icon.getSpriteId());
        }
        byte[] iconsData = new byte[stream.getOffset()];
        System.arraycopy(stream.getBuffer(), 0, iconsData, 0, iconsData.length);
        byte[] md5Hash = Utils.encryptUsingMD5(iconsData);
        this.iconsData = iconsData;
        md5IconsDataHash = md5Hash;
    }

    public void setBootsColor(int color) {
        colour[3] = (byte) color;
    }

}
