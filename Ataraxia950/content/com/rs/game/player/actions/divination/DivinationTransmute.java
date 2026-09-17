package com.rs.game.player.actions.divination;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.activites.halloween.ClueScrollDistributor;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.actions.Action;
import com.rs.game.player.content.skillingcontracts.impl.DivinationContractList;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.player.dialogue.impl.DivinationTransmuteD;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DivinationTransmute extends Action {

    public enum Transmutations {
        DIVINE_CRAYFISH_BUBBLE(new Item(31080, 1), 1, 1.0, new Item[] { new Item(29313, 5), new Item(13435, 20) }),

		PORTENT_OF_RESTORATION_I(new Item(29239, 1), 2, 1.0, new Item[] { new Item(29313, 30) }, new Item(13433, 1), new Item(319, 1), new Item(315, 1), new Item(325, 1), new Item(2140, 1), new Item(2142, 1)),

        DIVINE_BRONZE_ROCK(new Item(29294, 1), 3, 1.2, new Item[] { new Item(29313, 20) }, new Item(436, 20), new Item(438, 20)),

        DIVINE_KEBBIT_BURROW(new Item(29300, 1), 4, 1.3, new Item[] { new Item(29313, 25), new Item(9986, 20) }),

		ATTUNED_PORTENT_OF_RESTORATION_I(new Item(29259, 1), 5, 1.5, new Item[] { new Item(29313, 30) }, new Item(13433, 1), new Item(319, 1), new Item(315, 1), new Item(325, 1), new Item(2140, 1), new Item(2142, 1)),

		SIGN_OF_THE_PORTER_I(new Item(29275, 1), 6, 1.7, new Item[] { new Item(29313, 30), new Item(1656, 1) }),

        DIVINE_TREE(new Item(29304, 1), 7, 1.8, new Item[] { new Item(29313, 5), new Item(1511, 20) }),

		SIGN_OF_RESPITE_I(new Item(29269, 1), 8, 2.0, new Item[] { new Item(29313, 25), new Item(3211, 4) }),

        DIVINE_HERRING_BUBBLE(new Item(31081, 1), 11, 3.0, new Item[] { new Item(29314, 15), new Item(345, 20) }),

        DIVINE_HERB_PATCH_I(new Item(29310, 1), 12, 3.1, new Item[] { new Item(29314, 5), new Item(249, 10) }),

        OAK_LOGS(new Item(1521, 1), 13, 3.3, new Item[] { new Item(29314, 2), new Item(1511, 3) }),

		PORTENT_OF_RESTORATION_II(new Item(29241, 1), 15, 3.4, new Item[] { new Item(29314, 30), new Item(333, 1) }),

        RAW_TROUT(new Item(335, 1), 16, 3.6, new Item[] { new Item(29314, 2) }, new Item(327, 3), new Item(13435, 3), new Item(321, 3), new Item(317, 3)),

        IRON_ORE(new Item(440, 1), 17, 3.7, new Item[] { new Item(29314, 2) }, new Item(436, 3), new Item(438, 3)),

		ATTUNED_PORTENT_OF_RESTORATION_II(new Item(29260, 1), 18, 3.9, new Item[] { new Item(29314, 30), new Item(333, 1) }),

        DIVINE_IRON_ROCK(new Item(29295, 1), 19, 4.0, new Item[] { new Item(29314, 20), new Item(440, 15) }),

        DIVINE_TROUT_BUBBLE(new Item(31082, 1), 20, 5.0, new Item[] { new Item(29315, 15), new Item(335, 20) }),

        DIVINE_OAK_TREE(new Item(29305, 1), 21, 5.1, new Item[] { new Item(29315, 15), new Item(1521, 20) }),

        SILVER_ORE(new Item(442, 1), 22, 5.2, new Item[] { new Item(29315, 2), new Item(440, 3) }),

		SIGN_OF_RESPITE_II(new Item(29271, 1), 23, 5.3, new Item[] { new Item(29315, 30), new Item(3211, 4) }),

        DIVINE_BIRD_SNARE(new Item(29301, 1), 24, 5.4, new Item[] { new Item(29315, 30), new Item(9978, 20) }),

		PORTENT_OF_RESTORATION_III(new Item(29243, 1), 25, 5.6, new Item[] { new Item(29315, 30), new Item(329, 1) }),

        UNCUT_EMERALD(new Item(1621, 1), 26, 5.7, new Item[] { new Item(29315, 2), new Item(1623, 3) }),

		ATTUNED_PORTENT_OF_RESTORATION_III(new Item(29261, 1), 27, 5.8, new Item[] { new Item(29315, 30), new Item(329, 1) }),

		SIGN_OF_THE_PORTER_II(new Item(29277, 1), 28, 5.9, new Item[] { new Item(29315, 35), new Item(1656, 1) }),

        COAL(new Item(453, 1), 29, 6.0, new Item[] { new Item(29315, 2), new Item(440, 3) }),

        DIVINE_SALMON_BUBBLE(new Item(31083, 1), 30, 7.0, new Item[] { new Item(29316, 45), new Item(331, 20) }),

        DIVINE_WILLOW_TREE(new Item(29306, 1), 31, 7.2, new Item[] { new Item(29316, 20), new Item(1519, 20) }),

        DIVINE_COAL_ROCK(new Item(29296, 1), 31, 7.2, new Item[] { new Item(29316, 30), new Item(453, 20) }),

        WILLOW_LOGS(new Item(1519, 1), 32, 7.3, new Item[] { new Item(29316, 2), new Item(1521, 3) }),

        UNCUT_RUBY(new Item(1619, 1), 33, 7.4, new Item[] { new Item(29316, 2), new Item(1621, 3) }),

        DIVINE_DEADFALL_TRAP(new Item(29302, 1), 34, 7.5, new Item[] { new Item(29316, 45), new Item(10113, 24) }),

		PORTENT_OF_RESTORATION_IV(new Item(29245, 1), 35, 7.6, new Item[] { new Item(29316, 30), new Item(361, 1) }),

        RAW_TUNA(new Item(359, 1), 36, 7.7, new Item[] { new Item(29316, 2), new Item(335, 4) }),

		ATTUNED_PORTENT_OF_RESTORATION_IV(new Item(29262, 1), 37, 7.8, new Item[] { new Item(29316, 30), new Item(361, 1) }),

		SIGN_OF_RESPITE_III(new Item(29273, 1), 38, 7.9, new Item[] { new Item(29316, 35), new Item(3211, 4) }),

        GOLD_ORE(new Item(444, 1), 39, 8.0, new Item[] { new Item(29316, 2), new Item(442, 3) }),

        DIVINE_LOBSTER_BUBBLE(new Item(31084, 1), 41, 10.0, new Item[] { new Item(29317, 70), new Item(377, 20) }),

        UNCUT_DIAMOND(new Item(1617, 1), 42, 9.1, new Item[] { new Item(29317, 2), new Item(1619, 3) }),

        DIVINE_MAPLE_TREE(new Item(29307, 1), 44, 9.3, new Item[] { new Item(29317, 25), new Item(1517, 20) }),

		PORTENT_OF_RESTORATION_V(new Item(29247, 1), 45, 9.4, new Item[] { new Item(29317, 35), new Item(379, 1) }),

        RAW_BASS(new Item(363, 1), 46, 9.6, new Item[] { new Item(29317, 2), new Item(359, 3) }),

		ATTUNED_PORTENT_OF_RESTORATION_V(new Item(29263, 1), 47, 9.7, new Item[] { new Item(29317, 35), new Item(379, 1) }),

		SIGN_OF_THE_PORTER_III(new Item(29279, 1), 48, 9.9, new Item[] { new Item(29317, 40), new Item(1658, 1) }),

        MAPLE_LOGS(new Item(1517, 1), 49, 10.0, new Item[] { new Item(29317, 2), new Item(1519, 3) }),

        DIVINE_HERB_PATCH_II(new Item(29311, 1), 51, 11.3, new Item[] { new Item(29318, 20), new Item(259, 20) }),

        DIVINE_SWORDFISH_BUBBLE(new Item(31085, 1), 53, 13.0, new Item[] { new Item(29318, 70), new Item(371, 20) }),

		PORTENT_OF_RESTORATION_VI(new Item(29249, 1), 55, 11.5, new Item[] { new Item(29318, 40), new Item(373, 1) }),

		ATTUNED_PORTENT_OF_RESTORATION_VI(new Item(29264, 1), 57, 11.8, new Item[] { new Item(29318, 40), new Item(373, 1) }),

        UNCUT_DRAGONSTONE(new Item(1631, 1), 58, 12.0, new Item[] { new Item(29318, 2), new Item(1617, 5) }),

        DIVINE_MITHRIL_ROCK(new Item(29297, 1), 61, 13.1, new Item[] { new Item(29319, 30), new Item(447, 20) }),

        DIVINE_YEW_TREE(new Item(29308, 1), 62, 13.2, new Item[] { new Item(29319, 30), new Item(1515, 20) }),

        MITHRIL_ORE(new Item(447, 1), 63, 13.3, new Item[] { new Item(29319, 2), new Item(453, 3) }),

        GREEN_CHARM(new Item(12159, 10), 64, 13.4, new Item[] { new Item(29319, 25), new Item(12158, 20) }),

        DIVINE_BOX_TRAP(new Item(29303, 1), 64, 13.4, new Item[] { new Item(29319, 45), new Item(10033, 20) }),

		PORTENT_OF_RESTORATION_VII(new Item(29251, 1), 65, 13.6, new Item[] { new Item(29319, 45), new Item(7946, 1) }),

        RAW_MONKFISH(new Item(7944, 1), 66, 13.7, new Item[] { new Item(29319, 2), new Item(363, 3) }),

		ATTUNED_PORTENT_OF_RESTORATION_VII(new Item(29265, 1), 67, 13.8, new Item[] { new Item(29319, 45), new Item(7946, 1) }),

		SIGN_OF_THE_PORTER_IV(new Item(29281, 1), 68, 13.9, new Item[] { new Item(29319, 45), new Item(1658, 1) }),

		SIGN_OF_ITEM_PROTECTION(new Item(29287, 1), 69, 14.0, new Item[] { new Item(29319, 45), new Item(2434, 4) }),

        YEW_LOGS(new Item(1515, 1), 72, 15.2, new Item[] { new Item(29320, 2), new Item(1517, 3) }),

        DIVINE_ADAMANTITE_ROCK(new Item(29298, 1), 73, 15.3, new Item[] { new Item(29320, 40), new Item(449, 25) }),

        CRIMSON_CHARM(new Item(12160, 10), 74, 15.4, new Item[] { new Item(29320, 75), new Item(12159, 30) }),

		PORTENT_OF_RESTORATION_VIII(new Item(29253, 1), 75, 15.5, new Item[] { new Item(29320, 50), new Item(385, 1) }),

        ADAMANTITE_ORE(new Item(449, 1), 76, 15.7, new Item[] { new Item(29320, 2), new Item(447, 3) }),

		ATTUNED_PORTENT_OF_RESTORATION_VIII(new Item(29266, 1), 77, 15.8, new Item[] { new Item(29320, 50), new Item(385, 1) }),

		SIGN_OF_LIFE(new Item(29290, 1), 78, 16.0, new Item[] { new Item(29320, 100), new Item(1643, 1) }),

        DIVINE_SHARK_BUBBLE(new Item(31086, 1), 79, 16.0, new Item[] { new Item(29320, 60), new Item(383, 20) }),

		DIVINE_SIMULACRUM_I(new Item(31310, 1), 75, 16.0, new Item[] { new Item(31312, 100) }),

		PORTENT_OF_DEGRADATION_I(new Item(31313, 1), 75, 15.5, new Item[] { new Item(31312, 50), new Item(385, 1) }),

		ATTUNED_PORTENT_OF_DEGRADATION_I(new Item(31319, 1), 77, 15.8, new Item[] { new Item(31312, 50), new Item(385, 1) }),

		SIGN_OF_DEATH(new Item(31322, 1), 79, 16.0, new Item[] { new Item(31312, 100), new Item(1643, 1) }),

        AVIANSIE_TALONS(new Item(31421, 1), 81, 1.0, new Item[] { new Item(31312, 10) }, new Item(31422, 1), new Item(31418, 1), new Item(31419, 1), new Item(31420, 1)),

        DEMON_HORN(new Item(31420, 1), 81, 1.0, new Item[] { new Item(31312, 10) }, new Item(31422, 1), new Item(31419, 1), new Item(31418, 1), new Item(31421, 1)),

        ICYENE_FEATHER(new Item(31419, 1), 81, 1.0, new Item[] { new Item(31312, 10) }, new Item(31422, 1), new Item(31420, 1), new Item(31418, 1), new Item(31421, 1)),

        VAMPYRE_FANGS(new Item(31418, 1), 81, 1.0, new Item[] { new Item(31312, 10) }, new Item(31422, 1), new Item(31421, 1), new Item(31419, 1), new Item(31420, 1)),

		PORTENT_OF_DEGRADATION_II(new Item(31315, 1), 86, 19.3, new Item[] { new Item(31312, 50), new Item(15266, 1) }),

		ATTUNED_PORTENT_OF_DEGRADATION_II(new Item(31320, 1), 87, 19.7, new Item[] { new Item(31312, 50), new Item(15266, 1) }),

        DIVINE_SIMULACRUM_II(new Item(31311, 1), 92, 16.0, new Item[] { new Item(31312, 100) }),

		PORTENT_OF_DEGRADATION_III(new Item(31317, 1), 97, 23.4, new Item[] { new Item(31312, 60), new Item(15272, 1) }),

		ATTUNED_PORTENT_OF_DEGRADATION_III(new Item(31321, 1), 98, 23.6, new Item[] { new Item(31312, 60), new Item(15272, 1) }),

		PORTENT_OF_DEATH(new Item(31324, 1), 99, 24.0, new Item[] { new Item(31312, 100), new Item(1643, 1) }),

        DIVINE_HERB_PATCH_III(new Item(29312, 1), 82, 17.5, new Item[] { new Item(29321, 10), new Item(265, 5) }),

        DIVINE_MAGIC_TREE(new Item(29309, 1), 83, 18.0, new Item[] { new Item(29321, 40), new Item(1513, 5) }),

        BLUE_CHARM(new Item(12163, 10), 84, 17.4, new Item[] { new Item(29321, 150), new Item(12160, 20) }),

		PORTENT_OF_RESTORATION_IX(new Item(29255, 1), 86, 19.3, new Item[] { new Item(29322, 50), new Item(15266, 1) }),

		ATTUNED_PORTENT_OF_RESTORATION_IX(new Item(29267, 1), 87, 19.7, new Item[] { new Item(29322, 50), new Item(15266, 1) }),

		SIGN_OF_THE_PORTER_V(new Item(29283, 1), 88, 20.0, new Item[] { new Item(29322, 60), new Item(1660, 1) }),

        DIVINE_CAVEFISH_BUBBLE(new Item(31087, 1), 89, 20.0, new Item[] { new Item(29322, 70), new Item(15264, 15) }),

        DIVINE_ROCKTAIL_BUBBLE(new Item(31088, 1), 91, 21.0, new Item[] { new Item(29323, 80), new Item(15270, 10) }),

		PORTENT_OF_ITEM_PROTECTION(new Item(29289, 1), 92, 21.3, new Item[] { new Item(29323, 60), new Item(2434, 4) }),

        MAGIC_LOGS(new Item(1513, 1), 93, 21.7, new Item[] { new Item(29323, 2), new Item(1515, 3) }),

        DIVINE_RUNITE_ROCK(new Item(29299, 1), 94, 22.0, new Item[] { new Item(29323, 80), new Item(451, 6) }),

        RUNITE_ORE(new Item(451, 1), 96, 23.2, new Item[] { new Item(29324, 10), new Item(449, 6) }),

		PORTENT_OF_RESTORATION_X(new Item(29257, 1), 97, 23.4, new Item[] { new Item(29324, 60), new Item(15272, 1) }),

		ATTUNED_PORTENT_OF_RESTORATION_X(new Item(29268, 1), 98, 23.6, new Item[] { new Item(29324, 60), new Item(15272, 1) }),

		SIGN_OF_THE_PORTER_VI(new Item(29285, 1), 99, 24.0, new Item[] { new Item(29324, 80), new Item(1662, 1) }),

		PORTENT_OF_LIFE(new Item(29292, 1), 99, 24.0, new Item[] { new Item(29324, 100), new Item(1643, 1) }),

        PALE_ENERGY(new Item(29313, 150), 1, 1.0, new Item[] { new Item(37941, 100) }),

        FLICKERING_ENERGY(new Item(29314, 140), 10, 1.0, new Item[] { new Item(37941, 100) }),

        BRIGHT_ENERGY(new Item(29315, 130), 20, 1.0, new Item[] { new Item(37941, 100) }),

        GLOWING_ENERGY(new Item(29316, 120), 30, 1.0, new Item[] { new Item(37941, 100) }),

        SPARKLING_ENERGY(new Item(29317, 110), 40, 1.0, new Item[] { new Item(37941, 100) }),

        GLEAMING_ENERGY(new Item(29318, 100), 50, 1.0, new Item[] { new Item(37941, 100) }),

        VIBRANT_ENERGY(new Item(29319, 90), 60, 1.0, new Item[] { new Item(37941, 100) }),

        LUSTROUS_ENERGY(new Item(29320, 80), 70, 1.0, new Item[] { new Item(37941, 100) }),

        BRILLIANT_ENERGY(new Item(29321, 70), 80, 1.0, new Item[] { new Item(37941, 100) }),

        RADIANT_ENERGY(new Item(29322, 60), 85, 1.0, new Item[] { new Item(37941, 100) }),

        LUMINOUS_ENERGY(new Item(29323, 50), 90, 1.0, new Item[] { new Item(37941, 100) }),

        INCANDESCENT_ENERGY(new Item(29324, 40), 95, 1.0, new Item[] { new Item(37941, 100) }),

        RAW_SEERFISH(new Item(38591, 10), 90, 204.0, new Item[] { new Item(37762, 20), new Item(38622, 2) }),

        RAW_SILLAGO(new Item(38592, 10), 91, 204.0, new Item[] { new Item(37764, 10), new Item(38622, 10) }),

        RAW_WOBBEGONG(new Item(38593, 10), 92, 213.0, new Item[] { new Item(37766, 10), new Item(38622, 15) }),

        SHINY_SHELL_CHIPPINGS(new Item(38594, 10), 93, 217.0, new Item[] { new Item(37776, 10), new Item(38622, 5) }),

        ALAEA_SEA_SALT(new Item(38595, 10), 94, 220.0, new Item[] { new Item(37772, 10), new Item(38622, 5) }),

        SLICED_MUSHROOMS(new Item(38596, 1), 95, 22.6, new Item[] { new Item(37759, 2), new Item(38622, 2) }),

        WOBBEGONG_OIL(new Item(38597, 10), 96, 232.0, new Item[] { new Item(37773, 20), new Item(38622, 20) }),

        SHINY_TORTLE_SHELL_BOWL(new Item(38598, 10), 97, 234.0, new Item[] { new Item(37739, 10), new Item(38622, 10) }),

        UNCUT_ONYX(new Item(6571, 1), 97, 23.4, new Item[] { new Item(29324, 250), new Item(42954, 100) });

        private final Item product;
        private final int levelToMake;
        private final double xp;
        private final Item[] ingredients;
        private final Item[] replaceableIngredients;

        Transmutations(Item product, int levelToMake, double xp, Item[] ingredients, Item... replaceableIngredients) {
            this.product = product;
            this.levelToMake = levelToMake;
            this.xp = xp;
            this.ingredients = ingredients;
            this.replaceableIngredients = replaceableIngredients.length == 0 ? null : replaceableIngredients;
        }

        public Item getProduct() {
            return product;
        }

        public int getProductId() {
            return product.getId();
        }

        public int getLevelToMake() {
            return levelToMake;
        }

        public double getXp() {
            return xp;
        }

        public Item[] getIngredients() {
            return ingredients;
        }

        public Item[] getReplaceableIngredients() {
            return replaceableIngredients;
        }

        public static List<Transmutations> getDivinationTransmutationForEnergy(Item item) {
            return Arrays.asList(Transmutations.values()).stream().filter(t -> isDivinationTransmutationForEnergy(item, t)).collect(Collectors.toList());
        }

        public static boolean isDivinationTransmutationForEnergy(Item energy, Transmutations transmutation) {
            boolean hasEnergy = false;
            for (Item item : transmutation.ingredients)
                if (item.getId() == energy.getId())
                    hasEnergy = true;
            return hasEnergy;
        }

    }

    public static final Transmutations[] DIVINE_LOCATIONS = new Transmutations[] { Transmutations.DIVINE_ADAMANTITE_ROCK, Transmutations.DIVINE_BIRD_SNARE, Transmutations.DIVINE_BOX_TRAP, Transmutations.DIVINE_BRONZE_ROCK, Transmutations.DIVINE_CAVEFISH_BUBBLE, Transmutations.DIVINE_COAL_ROCK, Transmutations.DIVINE_CRAYFISH_BUBBLE, Transmutations.DIVINE_DEADFALL_TRAP, Transmutations.DIVINE_HERB_PATCH_I, Transmutations.DIVINE_HERB_PATCH_II, Transmutations.DIVINE_HERB_PATCH_III, Transmutations.DIVINE_HERRING_BUBBLE, Transmutations.DIVINE_IRON_ROCK, Transmutations.DIVINE_KEBBIT_BURROW, Transmutations.DIVINE_LOBSTER_BUBBLE, Transmutations.DIVINE_MAGIC_TREE, Transmutations.DIVINE_MAPLE_TREE, Transmutations.DIVINE_MITHRIL_ROCK, Transmutations.DIVINE_OAK_TREE, Transmutations.DIVINE_ROCKTAIL_BUBBLE, Transmutations.DIVINE_RUNITE_ROCK, Transmutations.DIVINE_SALMON_BUBBLE, Transmutations.DIVINE_SHARK_BUBBLE, Transmutations.DIVINE_SIMULACRUM_II, Transmutations.DIVINE_SWORDFISH_BUBBLE, Transmutations.DIVINE_TREE, Transmutations.DIVINE_TROUT_BUBBLE, Transmutations.DIVINE_WILLOW_TREE, Transmutations.DIVINE_YEW_TREE };

    public static boolean isDivinationTransmute(Player player, Item item) {
        if (!item.getDefinitions().containsInventoryOption(0, "Weave"))
            return false;
        List<Transmutations> availableTransmutations = Transmutations.getDivinationTransmutationForEnergy(item);
        if (availableTransmutations.isEmpty())
            return false;
        if (availableTransmutations.size() > 10) {
            player.getDialogueManager().startDialogue(new Dialogue() {
                @Override
                public void start() {
                    sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "First tab of options", "Second tab of options", "cancel");
                }

                @Override
                public void run(int interfaceId, int componentId) {
                    end();
                    if (componentId != OPTION_3) {
                        int option = componentId == OPTION_1 ? 0 : 1;
                        player.getDialogueManager().startDialogue(DivinationTransmuteD.class.getSimpleName(), availableTransmutations.subList(option == 0 ? 0 : 10, option == 0 ? 10 : availableTransmutations.size()));
                    }
                }

                @Override
                public void finish() {
                }
            });
            return true;
        }
        player.getDialogueManager().startDialogue(DivinationTransmuteD.class.getSimpleName(), availableTransmutations);
        return true;
    }

    private final Transmutations transmutation;
    private int ticks;
    private final boolean divineLocation;

    public DivinationTransmute(Transmutations transmutation, int ticks, boolean divineLocation) {
        this.transmutation = transmutation;
        this.ticks = ticks;
        this.divineLocation = divineLocation;
    }

    @Override
    public boolean start(Player player) {
        return checkAll(player);
    }

    private boolean checkAll(Player player) {
        if (transmutation == null)
            return false;
        if (player.getSkills().getLevel(Skills.DIVINATION) < transmutation.getLevelToMake()) {
            player.getPackets().sendGameMessage("You need a divination level of " + transmutation.getLevelToMake() + " to make " + transmutation.getProduct().getName() + ".");
            return false;
        }
        if (player.created && divineLocation) {
            player.sendMessage("You've already created " + ((player.getPerkManager().hasPerkActive(DonationPerk.DIVINE_DOUBLER) ? 2 : 1) == 1 ? "one divine location today." : "two divine locations today."));
            return false;
        }
        Item[] ingredients = transmutation.ingredients;
        if (!player.getInventory().containsItems(ingredients)) {
            String[] itemNames = new String[ingredients.length];
            for (int i = 0; i < itemNames.length; i++) {
                Item item = ingredients[i];
                if (item != null)
                    itemNames[i] = !player.getInventory().containsItem(item.getId(), item.getAmount()) ? item.getName() : "";
            }
            StringBuilder message = new StringBuilder();
            for (int i = 0; i < itemNames.length; i++) {
                String name = itemNames[i];
                message.append(itemNames[i]).append(name != "" && (i != (itemNames.length - 1)) ? " and " : "");
            }
            if (message.toString().endsWith(" and "))
                message.toString().replace(" and ", "");
            player.getPackets().sendGameMessage("You do not have enough " + message.toString().trim().toLowerCase() + " in your inventory.");
            return false;
        }
        Item[] replaceableIngredients = transmutation.replaceableIngredients;
        if (replaceableIngredients != null) {
            boolean hasOne = false;
            for (Item item : replaceableIngredients)
                if (player.getInventory().containsItem(item))
                    hasOne = true;
            if (!hasOne) {
                player.getPackets().sendGameMessage("You need to have any of the following items to make " + transmutation.getProduct().getName() + ":");
                int count = 1;
                for (Item item : replaceableIngredients) {
                    player.getPackets().sendGameMessage(count + ") " + item.getAmount() + " x " + item.getName() + ".");
                    count++;
                }
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean process(Player player) {
        return checkAll(player) && ticks > 0;
    }

    @Override
    public int processWithDelay(Player player) {
        if (divineLocation) {
            player.createdToday += 1;
            if (player.createdToday >= (player.getPerkManager().hasPerkActive(DonationPerk.DIVINE_DOUBLER) ? 2 : 1))
                player.created = true;
        }
        ticks--;
        for (Item item : transmutation.ingredients)
            player.getInventory().removeItemMoneyPouch(item);
        if (transmutation.replaceableIngredients != null)
            for (Item item : transmutation.replaceableIngredients)
                if (player.getInventory().containsItem(item)) {
                    player.getInventory().deleteItem(item.getId(), item.getAmount());
                    break;
                }
        player.setNextAnimation(new Animation(21248));
        player.setNextGraphics(new Graphics(4249));
        Item product = transmutation.getProduct();
        if (player.getContracts().hasContract()) {
            WispInfo wisp = WispInfo.forEnergyId(product.getId());
            if (wisp != null)
                DivinationContractList.listen(player, wisp);
        }
        player.getInventory().addItem(product);
        player.getSkills().addXp(Skills.DIVINATION, transmutation.getXp());
        player.sendMessage("You weave the energy into a " + product.getName().toLowerCase() + ".", true);
        ClueScrollDistributor.givePlayerClueScrollIfProbable(player, ClueScrollDistributor.SKILLING_PERCENT);
        if (ticks <= 0)
            return -1;
        return 1;
    }

    @Override
    public void stop(Player player) {
        setActionDelay(player, 3);
    }

}
