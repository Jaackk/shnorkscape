package com.rs.game.player.dialogue.impl;

import java.util.HashMap;
import java.util.Map;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.ChargesManagerNew.ChargesData;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

import lombok.Getter;

public class ClueScrollDyes {

    public enum DyesData {
        DRYGORE_RAPIER(26579, new int[] { 33306, 33372, 33438, 36309, 42034 }),

        OFF_HAND_DRYGORE_RAPIER(26583, new int[] { 33309, 33375, 33441, 36312, 42037 }),

        DRYGORE_LONGSWORD(26587, new int[] { 33312, 33378, 33444, 36315, 42022 }),

        OFF_HAND_DRYGORE_LONGSWORD(26591, new int[] { 33315, 33381, 33447, 36318, 42025 }),

        DRYGORE_MACE(26595, new int[] { 33300, 33366, 33432, 36303, 42028 }),

        OFF_HAND_DRYGORE_MACE(26599, new int[] { 33303, 33369, 33435, 36306, 42031 }),

        ASCENSION_CROSSBOW(28437, new int[] { 33318, 33384, 33450, 36321, 42016 }),

        OFF_HAND_ASCENSION_CROSSBOW(28441, new int[] { 33321, 33387, 33453, 36324, 42019 }),

        TECTONIC_MASK(28608, new int[] { 33339, 33405, 33471, 36276, 42082 }),

        TECTONIC_ROBE_TOP(28611, new int[] { 33342, 33408, 33474, 36279, 42085 }),

        TECTONIC_ROBE_BOTTOM(28614, new int[] { 33345, 33411, 33477, 36282, 42088 }),

        SEISMIC_WAND(28617, new int[] { 33324, 33390, 33456, 36327, 42058 }),

        SEISMIC_SINGULARITY(28621, new int[] { 33327, 33393, 33459, 36330, 42061 }),

        SIRENIC_MASK(29854, new int[] { 33348, 33414, 33480, 36285, 42073 }),

        SIRENIC_HAUBERK(29857, new int[] { 33351, 33417, 33483, 36288, 42076 }),

        SIRENIC_CHAPS(29860, new int[] { 33354, 33420, 33486, 36291, 42079 }),

        MALEVOLENT_HELM(30005, new int[] { 33357, 33423, 33489, 36294, 42040 }),

        MALEVOLENT_CUIRASS(30008, new int[] { 33360, 33426, 33492, 36297, 42043 }),

        MALEVOLENT_GREAVES(30011, new int[] { 33363, 33429, 33495, 36300, 42046 }),

        NOXIOUS_SCYTHE(31725, new int[] { 33330, 33396, 33462, 36333, 42049 }),

        NOXIOUS_STAFF(31729, new int[] { 33333, 33399, 33465, 36336, 42052 }),

        NOXIOUS_LONGBOW(31733, new int[] { 33336, 33402, 33468, 36339, 42055 }),

        AUGMENTED_MALEVOLENT_CUIRASS(36519, new int[] { 38204, 38208, 38212, 38240, 42134 }),

        AUGMENTED_MALEVOLENT_GREAVES(36521, new int[] { 38206, 38210, 38214, 38242, 42136 }),

        AUGMENTED_TECTONIC_ROBE_TOP(36523, new int[] { 38216, 38220, 38224, 38244, 42142 }),

        AUGMENTED_TECTONIC_ROBE_BOTTOM(36525, new int[] { 38218, 38222, 38226, 38246, 42144 }),

        AUGMENTED_SIRENIC_HAUBERK(36527, new int[] { 38230, 38232, 38236, 38248, 42138 }),

        AUGMENTED_SIRENIC_CHAPS(36529, new int[] { 38228, 38234, 38238, 38250, 42140 }),

        AUGMENTED_DRYGORE_LONGSWORD(36693, new int[] { 38260, 38286, 38312, 38338, 42166 }),

        AUGMENTED_OFF_HAND_DRYGORE_LONGSWORD(36695, new int[] { 38262, 38288, 38314, 38340, 42168 }),

        AUGMENTED_DRYGORE_MACE(36697, new int[] { 38252, 38278, 38304, 38330, 42170 }),

        AUGMENTED_OFF_HAND_DRYGORE_MACE(36699, new int[] { 38254, 38280, 38306, 38332, 42172 }),

        AUGMENTED_DRYGORE_RAPIER(36701, new int[] { 38256, 38282, 38308, 38334, 42174 }),

        AUGMENTED_OFF_HAND_DRYGORE_RAPIER(36703, new int[] { 38258, 38284, 38310, 38336, 42176 }),

        AUGMENTED_ASCENSION_CROSSBOW(36705, new int[] { 38264, 38290, 38316, 38342, 42162 }),

        AUGMENTED_OFF_HAND_ASCENSION_CROSSBOW(36707, new int[] { 38266, 38292, 38318, 38344, 42164 }),

        AUGMENTED_SEISMIC_WAND(36709, new int[] { 38268, 38294, 38320, 38346, 42184 }),

        AUGMENTED_SEISMIC_SINGULARITY(36711, new int[] { 38270, 38296, 38322, 38348, 42186 }),

        AUGMENTED_NOXIOUS_STAFF(36713, new int[] { 38274, 38300, 38326, 38352, 42180 }),

        AUGMENTED_NOXIOUS_SCYTHE(36715, new int[] { 38272, 38298, 38324, 38350, 42178 }),

        SEREN_GODBOW(37632, new int[] { 40713, 40716, 40719, 40710, 42070 }),

        STAFF_OF_SLISKE(37636, new int[] { 40689, 40692, 40695, 40686, 42064 }),

        ZAROS_GODSWORD(37640, new int[] { 40701, 40704, 40707, 40698, 42067 }),

        AUGMENTED_SEREN_GODBOW(37673, new int[] { 40758, 40760, 40762, 40756, 42192 }),

        AUGMENTED_ZAROS_GODSWORD(37675, new int[] { 40750, 40752, 40754, 40748, 42190 }),

        AUGMENTED_STAFF_OF_SLISKE(37677, new int[] { 40742, 40744, 40746, 40740, 42188 }),

        WAND_OF_THE_PRAESUL(39574, new int[] { 42548, 42551, 42554, 42557, 42100 }),

        IMPERIUM_CORE(39579, new int[] { 42560, 42563, 42566, 42569, 42097 }),

        AUGMENTED_WAND_OF_THE_PRAESUL(39620, new int[] { 42572, 42574, 42576, 42578, 42200 }),

        AUGMENTED_IMPERIUM_CORE(39622, new int[] { 42580, 42582, 42584, 42586, 42198 }),

        KHOPESH_OF_TUMEKEN(40655, new int[] { 42524, 42527, 42530, 42533, 42091 }),

        KHOPESH_OF_ELIDINIS(40659, new int[] { 42536, 42539, 42542, 42545, 42094 }),

        AUGMENTED_KHOPESH_OF_TUMEKEN(40672, new int[] { 42588, 42590, 42592, 42594, 42194 }),

        AUGMENTED_KHOPESH_OF_ELIDINIS(40674, new int[] { 42596, 42598, 42600, 42602, 42196 }),

        BLIGHTBOUND_CROSSBOW(42770, new int[] { 42788, 42794, 42812, 42800, 42806 }),

        OFF_HAND_BLIGHTBOUND_CROSSBOW(42774, new int[] { 42791, 42797, 42815, 42803, 42809 }),

        AUGMENTED_BLIGHTBOUND_CROSSBOW(42819, new int[] { 42823, 42827, 42839, 42831, 42835 }),

        AUGMENTED_OFF_HAND_BLIGHTBOUND_CROSSBOW(42821, new int[] { 42825, 42829, 42841, 42833, 42837 }),

        ELITE_SIRENIC_MASK(43155, new int[] { 42982, 43000, 43018, 42955, 43027 }),

        ELITE_SIRENIC_HAUBERK(43158, new int[] { 42985, 43003, 43021, 42958, 43030 }),

        ELITE_SIRENIC_CHAPS(43161, new int[] { 42988, 43006, 43024, 42961, 43033 }),

        ELITE_TECTONIC_MASK(43166, new int[] { 42973, 42991, 43009, 42964, 43036 }),

        ELITE_TECTONIC_ROBE_TOP(43169, new int[] { 42976, 42994, 43012, 42967, 43039 }),

        ELITE_TECTONIC_ROBE_BOTTOM(43172, new int[] { 42979, 42997, 43015, 42970, 43042 }),

        AUGMENTED_ELITE_SIRENIC_HAUBERK(43181, new int[] { 43129, 43131, 43135, 43143, 43147 }),

        AUGMENTED_ELITE_SIRENIC_CHAPS(43183, new int[] { 43127, 43133, 43137, 43145, 43149 }),

        AUGMENTED_ELITE_TECTONIC_ROBE_TOP(43185, new int[] { 43115, 43119, 43123, 43139, 43151 }),

        AUGMENTED_ELITE_TECTONIC_ROBE_BOTTOM(43187, new int[] { 43117, 43121, 43125, 43141, 43153 }),

        ELDRITCH_CROSSBOW(47470, new int[] { 47475, 47487, 47481, 47478, 47484 }),

        AUGMENTED_ELDRITCH_CROSSBOW(47569, new int[] { 47565, 47561, 47563, 47559, 47567 }),
        /**
         * imbued variants
         */
        OFF_HAND_DRYGORE_RAPIER_I(41397, new int[] { 41398, 41399, 41400, 41401, -1 }),

        OFF_HAND_DRYGORE_LONGSWORD_I(41392, new int[] { 41393, 41394, 41395, 41396, -1 }),

        OFF_HAND_DRYGORE_MACE_I(41402, new int[] { 41403, 41404, 41405, 41406, -1 }),

        SEISMIC_SINGULARITY_I(41408, new int[] { 41409, 41410, 41411, 41412, -1 }),

        OFF_HAND_ASCENSION_CROSSBOW_I(41413, new int[] { 41414, 41415, 41416, 41417, -1 }),

        CHRISTMAS_SCYTHE(31725, new int[] { 33625 });

        private static final Map<Integer, DyesData> dyeables = new HashMap<Integer, DyesData>();
        private static final Map<Integer, DyesData> undyeables = new HashMap<Integer, DyesData>();
        static {
            for (DyesData dyedata : DyesData.values()) {
                if (dyedata.ordinal() != CHRISTMAS_SCYTHE.ordinal())
                    dyeables.put(dyedata.getItemId(), dyedata);
            }
            for (DyesData dyedata : DyesData.values()) {
                for (int dyed : dyedata.dyes)
                    if (dyed != -1)
                        undyeables.put(dyed, dyedata);
            }
        }

        @Getter
        private final int itemId;
        @Getter
        private final int[] dyes;// barrows,shadow,3rd age, blood, ice

        DyesData(int itemId, int[] dyes) {
            this.itemId = itemId;
            this.dyes = dyes;
        }

        public static DyesData forItem(Item item) {
            if (dyeables.containsKey(item.getId()))
                return dyeables.get(item.getId());
            Integer[] repairData = item.getDefinitions().getRepairData();
            if (repairData != null && repairData.length == 1) {
                int originalId = repairData[0];
                Integer[] degradationData = ItemDefinitions.getItemDefinitions(originalId).getItemDegradeData();
                if (degradationData != null && degradationData.length == 2 && (item.getId() == degradationData[0] || item.getId() == degradationData[1]) && dyeables.containsKey(repairData[0]))
                    return dyeables.get(repairData[0]);
            }
            if (item.getDefinitions().getDegradeToDustOriginalItemId() != -1 && dyeables.containsKey(item.getDefinitions().getDegradeToDustOriginalItemId()))
                return dyeables.get(item.getDefinitions().getDegradeToDustOriginalItemId());
            if (item.getDefinitions().getId() == item.getDefinitions().getUnchargedItemId() && dyeables.containsKey(item.getDefinitions().getChargedItemId()))
                return dyeables.get(item.getDefinitions().getChargedItemId());
            return null;
        }

        public static DyesData forDyedItem(Item item) {
            if (undyeables.containsKey(item.getId()))
                return undyeables.get(item.getId());
            Integer[] repairData = item.getDefinitions().getRepairData();
            if (repairData != null && repairData.length == 1) {
                int originalId = repairData[0];
                Integer[] degradationData = ItemDefinitions.getItemDefinitions(originalId).getItemDegradeData();
                if (degradationData != null && degradationData.length == 2 && (item.getId() == degradationData[0] || item.getId() == degradationData[1]) && undyeables.containsKey(repairData[0]))
                    return undyeables.get(repairData[0]);
            }
            if (item.getDefinitions().getDegradeToDustOriginalItemId() != -1 && undyeables.containsKey(item.getDefinitions().getDegradeToDustOriginalItemId()))
                return undyeables.get(item.getDefinitions().getDegradeToDustOriginalItemId());
            if (item.getDefinitions().getId() == item.getDefinitions().getUnchargedItemId() && undyeables.containsKey(item.getDefinitions().getChargedItemId()))
                return undyeables.get(item.getDefinitions().getChargedItemId());
            return null;
        }

    }
    
    public static final int[] DYES = new int[] { 33294, 33296, 33298, 36274, 41887 };

    public static int getDyeIndex(int itemId) {
        for (int i = 0; i < DYES.length; i++) {
            if (itemId == DYES[i])
                return i;
        }
        return -1;
    }
    
    public static boolean dyeItem(Player player, Item itemUsed, Item itemUsedWith) {
        Item dye = getDyeIndex(itemUsed.getId()) != -1 ? itemUsed : getDyeIndex(itemUsedWith.getId()) != -1 ? itemUsedWith : null;
        if (dye == null)
            return false;
        int dyeIndex = getDyeIndex(dye.getId());
        Item item = dye == itemUsed ? itemUsedWith : itemUsed;
        if (item == null || getDyeIndex(item.getId()) != -1)
            return false;
        DyesData data = DyesData.forItem(item);
        if (data == null) {
            player.getPackets().sendGameMessage("You can't dye that item.");
            return true;
        }
        int dyedItemId = data.getDyes()[dyeIndex];
        if (dyedItemId == -1) {
            player.getPackets().sendGameMessage(item.getName() + " doesn't have " + Utils.aorAn(dye.getName()) + " " + dye.getName() + "d version.");
            return true;
        }
        player.getDialogueManager().startDialogue(new Dialogue() {

            @Override
            public void start() {
                final int interfaceId = 1183;
                player.getInterfaceManager().sendChatBoxInterface(interfaceId);
                player.getPackets().sendItemOnIComponent(interfaceId, 8, item.getId(), 1);
                player.getPackets().sendIComponentText(interfaceId, 3, "Dye your " + item.getName() + " with the " + dye.getName() + "?");
                player.getPackets().sendIComponentText(interfaceId, 1, Colors.wrap(Colors.RED + Colors.SHAD, "WARNING: ") + "Dyeing the " + item.getName() + " will make the it permanently untradable!");
            }

            @Override
            public void run(int interfaceId, int componentId) {
                switch (stage) {
                case -1:
                    if (!player.getInventory().containsItem(item)) {
                        end();
                        return;
                    }
                    int slotId = player.getInventory().getItemSlot(item);
                    if (slotId == -1) {
                        end();
                        return;
                    }
                    if (interfaceId == 1183 && componentId == 5) {
                        int originalId = item.getId();
                        if (item.getAttributes() == null) {
                            Integer[] repairData = item.getDefinitions().getRepairData();
                            if (repairData != null && repairData.length == 1) {
                                int fullchargedId = repairData[0];
                                Integer[] degradationData = ItemDefinitions.getItemDefinitions(fullchargedId).getItemDegradeData();
                                if (degradationData != null && degradationData.length == 2 && item.getId() == degradationData[1]) {
                                    item.setId(dyedItemId);
                                    degradationData = ItemDefinitions.getItemDefinitions(dyedItemId).getItemDegradeData();
                                    if (degradationData != null && degradationData.length == 2)
                                        item.setId(degradationData[1]);
                                    else
                                        item.setId(dyedItemId);
                                } else
                                    item.setId(dyedItemId);
                            } else
                                item.setId(dyedItemId);
                            player.getInventory().deleteItem(dye.getId(), 1);
                            sendSuccessfullItemMessage(item.getId(), originalId);
                            player.getInventory().refresh();
                            return;
                        }
                        Item toGive = new Item(originalId);
                        toGive.setId(dyedItemId);
                        if (item.getInventionData() != null) {
                            toGive.setInventionData(item.getInventionData());
                            DyesData unaugmentedDyedata = DyesData.forItem(new Item(item.getInventionData().getOriginalItemId(), 1));
                            toGive.getInventionData().setOriginalItemId(unaugmentedDyedata.getDyes()[dyeIndex]);
                            if (player.getInventionManager().getDivineCharges() <= 0 && !toGive.getDefinitions().usesChargesInside())
                                toGive.setId(toGive.getDefinitions().getUnchargedItemId());
                        }
                        if (item.getChargesData() != null) {
                            Integer[] degradationData = toGive.getDefinitions().getItemDegradeData();
                            int maxCharges = toGive.getDefinitions().getMaxCharges();
                            if (degradationData == null || maxCharges <= 0) {
                                player.getPackets().sendGameMessage("Couldn't dye this item, please report to staff.");
                                return;
                            }
                            ChargesData data = new ChargesData(toGive.getId(), degradationData.length < 2 ? toGive.getId() : degradationData[0], degradationData.length < 2 ? degradationData[0] : degradationData[1], maxCharges);
                            toGive.setId(data.getWornId());
                            data.setChargesLeft(item.getChargesData().getChargesLeft());
                            toGive.setChargesData(data);
                        }
                        player.getInventory().deleteItem(dye.getId(), 1);
                        player.getInventory().set(slotId, toGive);
                        sendSuccessfullItemMessage(toGive.getId(), originalId);
                        player.getInventory().refresh();
                        return;
                    }
                    end();
                    break;
                case 0:
                    end();
                    break;
                }
            }

            private void sendSuccessfullItemMessage(int itemId, int originalItemId) {
                stage = 0;
                sendItemDialogue(itemId, 1, "You've successfully " + dye.getName() + "d your " + ItemDefinitions.getItemDefinitions(originalItemId).getName() + "!");
            }

            @Override
            public void finish() {
            }
        });
        return true;
    }

    public static boolean undyeItem(Player player, Item itemUsed, Item itemUsedWith) {
        Item cleaningCloth = itemUsed.getId() == 3188 ? itemUsed : itemUsedWith.getId() == 3188 ? itemUsedWith : null;
        if (cleaningCloth == null)
            return false;
        Item item = cleaningCloth == itemUsed ? itemUsedWith : itemUsed;
        if (item == null)
            return false;
        DyesData data = DyesData.forDyedItem(item);
        if (data == null)
            return false;
        int undyedItemId = data.getItemId();
        if (item.getAttributes() == null) {
            Integer[] repairData = item.getDefinitions().getRepairData();
            if (repairData != null && repairData.length == 1) {
                int fullchargedId = repairData[0];
                Integer[] degradationData = ItemDefinitions.getItemDefinitions(fullchargedId).getItemDegradeData();
                if (degradationData != null && degradationData.length == 2 && item.getId() == degradationData[1]) {
                    degradationData = ItemDefinitions.getItemDefinitions(undyedItemId).getItemDegradeData();
                    if (degradationData != null && degradationData.length == 2 && degradationData[1] == -1) {
                        player.getPackets().sendGameMessage(item.getName() + " has no undyed broken version, its undyed version degrades to dust.");
                        return true;
                    }
                }
            }
        }
        player.getDialogueManager().startDialogue(new Dialogue() {

            @Override
            public void start() {
                sendItemDialogue(item.getId(), 1, "This action will destroy your cleaning cloth and you will lose the dye!");
            }

            @Override
            public void run(int interfaceId, int componentId) {
                switch (stage) {
                case -1:
                    sendOptionsDialogue("Do you want to undye this item?", "Yes", "No");
                    stage = 0;
                    break;
                case 0:
                    end();
                    if (componentId == OPTION_1) {
                        if (!player.getInventory().containsItem(item))
                            return;
                        int slotId = player.getInventory().getItemSlot(item);
                        if (slotId == -1)
                            return;
                        int originalId = item.getId();
                        if (item.getAttributes() == null) {
                            Integer[] repairData = item.getDefinitions().getRepairData();
                            if (repairData != null && repairData.length == 1) {
                                int fullchargedId = repairData[0];
                                Integer[] degradationData = ItemDefinitions.getItemDefinitions(fullchargedId).getItemDegradeData();
                                if (degradationData != null && degradationData.length == 2 && item.getId() == degradationData[1]) {
                                    item.setId(undyedItemId);
                                    degradationData = ItemDefinitions.getItemDefinitions(undyedItemId).getItemDegradeData();
                                    if (degradationData != null && degradationData.length == 2)
                                        item.setId(degradationData[1]);
                                    else
                                        item.setId(undyedItemId);
                                } else
                                    item.setId(undyedItemId);
                            } else
                                item.setId(undyedItemId);
                            player.getInventory().deleteItem(cleaningCloth.getId(), 1);
                            player.sendMessage(Colors.GREEN + "You have undyed your " + ItemDefinitions.getItemDefinitions(originalId).getName() + "! Your cleansing cloth disappears!", true);
                            player.getInventory().refresh();
                            return;
                        }
                        Item toGive = new Item(originalId);
                        toGive.setId(undyedItemId);
                        if (item.getInventionData() != null) {
                            toGive.setInventionData(item.getInventionData());
                            DyesData unaugmentedDyedata = DyesData.forDyedItem(new Item(item.getInventionData().getOriginalItemId(), 1));
                            toGive.getInventionData().setOriginalItemId(unaugmentedDyedata.getItemId());
                            if (player.getInventionManager().getDivineCharges() <= 0 && !toGive.getDefinitions().usesChargesInside())
                                toGive.setId(toGive.getDefinitions().getUnchargedItemId());
                        }
                        if (item.getChargesData() != null) {
                            Integer[] degradationData = toGive.getDefinitions().getItemDegradeData();
                            int maxCharges = toGive.getDefinitions().getMaxCharges();
                            if (degradationData == null || maxCharges <= 0) {
                                player.getPackets().sendGameMessage("Couldn't dye this item, please report to staff.");
                                return;
                            }
                            ChargesData data = new ChargesData(toGive.getId(), degradationData.length < 2 ? toGive.getId() : degradationData[0], degradationData.length < 2 ? degradationData[0] : degradationData[1], maxCharges);
                            toGive.setId(data.getWornId());
                            data.setChargesLeft(item.getChargesData().getChargesLeft());
                            toGive.setChargesData(data);
                        }
                        player.getInventory().deleteItem(cleaningCloth.getId(), 1);
                        player.getInventory().set(slotId, toGive);
                        player.sendMessage(Colors.GREEN + "You have undyed your " + ItemDefinitions.getItemDefinitions(originalId).getName() + "! Your cleansing cloth disappears!", true);
                        player.getInventory().refresh();
                        return;

                    }
                    break;
                }
            }

            @Override
            public void finish() {
            }

        });
        return true;
    }


}