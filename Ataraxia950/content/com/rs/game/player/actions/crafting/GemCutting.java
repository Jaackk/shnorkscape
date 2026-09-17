package com.rs.game.player.actions.crafting;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.activites.halloween.ClueScrollDistributor;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.content.packs.portable.PortableType;
import com.rs.game.player.content.petperks.PetPerkHandler;
import com.rs.game.player.content.skillingcontracts.impl.CraftingContractList;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

import java.util.ArrayList;

public class GemCutting extends Action {

    private final Gem gem;
    private final boolean portable;

    private int quantity;

    public GemCutting(Gem gem, int quantity, boolean portable) {
        this.gem = gem;
        this.quantity = quantity;
        this.portable = portable;
    }

    public static void cut(Player player, Gem gem) {
        CraftingRs3Dialogue.sendGemCuttingInterface(player, gem, false);
    }

    public static void performPortableAction(Player player, WorldObject object) {
        ArrayList<Integer> possibilities = new ArrayList<Integer>();
        for (Gem gem : Gem.values()) {
            if (player.getInventory().containsItem(gem.getUncut(), 1))
                possibilities.add(gem.getUncut());
        }

        if (possibilities.isEmpty()) {
            player.sendMessage("You do not have any gems to cut.");
            return;
        }

        Item item = getPreferredItemToUse(player, possibilities);
        if (item == null) {
            player.sendMessage("You do not have any gems to cut.");
            return;
        }

        Gem gem = Gem.forId(item.getId());
        if (gem == null)
            return;

        CraftingRs3Dialogue.sendGemCuttingInterface(player, gem, PortableType.isPortableObject(object.getId()));
    }

    private static Item getPreferredItemToUse(Player player, ArrayList<Integer> ints) {
        Item temp = null;
        for (int i : ints) {
            if (temp == null || player.getInventory().getNumberOf(i) > temp.getAmount())
                temp = new Item(i, player.getInventory().getNumberOf(i));
        }
        return temp;
    }

    public boolean checkAll(Player player) {
        if (player.getInterfaceManager().containsScreenInter() || player.getInterfaceManager().containsInventoryInter()) {
            player.getPackets().sendGameMessage("Please finish what you're doing before doing this action.");
            return false;
        }
        if (player.getSkills().getLevel(Skills.CRAFTING) < gem.getLevelRequired()) {
            player.getDialogueManager().startDialogue("SimpleMessage", "You need a crafting level of " + gem.getLevelRequired() + " to cut that gem.");
            return false;
        }
        if (!player.getInventory().containsOneItem(gem.getUncut())) {
          //  player.getDialogueManager().startDialogue("SimpleMessage", "You don't have any " + ItemDefinitions.getItemDefinitions(gem.getUncut()).getName().toLowerCase() + " to cut.");
            return false;
        }
        if (!player.getInventory().containsItem(1755, 1) && !player.getToolBelt().contains(1755)) {
            player.sendMessage("You don't have a chisel to cut the " + ItemDefinitions.getItemDefinitions(gem.getUncut()).getName().toLowerCase() + ".");
            return false;
        }
        if (player.clickedObject != null) {
            return World.containsObjectWithId(player.clickedObject, player.clickedObject.getId());
        }
        return true;
    }

    @Override
    public boolean process(Player player) {
        return checkAll(player);
    }

    @Override
    public int processWithDelay(Player player) {
        if (PetPerkHandler.handleEfficiencyExpert(player, new Item(gem.uncut))) {
            player.addItem(gem.getCut(), 1);
        } else {
            player.getInventory().deleteItem(gem.getUncut(), 1);
            player.getInventory().addItem(gem.getCut(), 1);
        }
        CraftingContractList.listenGem(player, gem);
        player.getSkills().addXp(Skills.CRAFTING, gem.getExperience());
        player.addItemsMade();
        if (portable && Utils.random(9) == 4 && gem != Gem.ONYX && gem != Gem.HYDRIX) {
            player.getBank().addItem(new Item(gem.getUncut(), 1), true);
            player.sendMessage(Colors.GOLD + "<shad=000000>The portable crafter saves you some resources. They have been sent to your bank.", true);
        }
        player.sendMessage("You cut the " + ItemDefinitions.getItemDefinitions(gem.getUncut()).getName().toLowerCase() + "; items crafted: " + Colors.RED + Utils.getFormattedNumber(player.getItemsMade()) + "</col>.", true);
        ClueScrollDistributor.givePlayerClueScrollIfProbable(player, ClueScrollDistributor.SKILLING_PERCENT);
        quantity--;
        if (quantity <= 0)
            return -1;
        player.setNextAnimation(new Animation(gem.getEmote()));
        return 0;
    }

    @Override
    public boolean start(Player player) {
        if (checkAll(player)) {
            setActionDelay(player, 1);
            player.setNextAnimation(new Animation(gem.getEmote()));
            return true;
        }
        return false;
    }

    @Override
    public void stop(final Player player) {
        setActionDelay(player, 3);
        player.clickedObject = null;
    }

    /**
     * Enum for gems
     *
     * @author Raghav
     * @author Noel
     */
    public enum Gem {

        OPAL(1625, 1609, 15.0, 1, 22778),

        JADE(1627, 1611, 20, 13, 22779),

        RED_TOPAZ(1629, 1613, 25, 16, 22780),

        SAPPHIRE(1623, 1607, 40, 20, 22774),

        EMERALD(1621, 1605, 57, 27, 22775),

        RUBY(1619, 1603, 65, 34, 22776),

        DIAMOND(1617, 1601, 85, 43, 22777),

        DRAGONSTONE(1631, 1615, 107.5, 55, 22781),

        ONYX(6571, 6573, 200, 67, 22782),

        HYDRIX(31853, 31855, 300, 79, 22783);

        private final double experience;
        private final int levelRequired;
        private final int uncut;
        private final int cut;
        private final int emote;

        Gem(int uncut, int cut, double experience, int levelRequired, int emote) {
            this.uncut = uncut;
            this.cut = cut;
            this.experience = experience;
            this.levelRequired = levelRequired;
            this.emote = emote;
        }

        public static Gem forId(int itemId) {
            for (Gem gem : Gem.values()) {
                if (gem.getUncut() == itemId)
                    return gem;
            }

            return null;
        }

        public int getCut() {
            return cut;
        }

        public int getEmote() {
            return emote;
        }

        public double getExperience() {
            return experience;
        }

        public int getLevelRequired() {
            return levelRequired;
        }

        public int getUncut() {
            return uncut;
        }
    }
}
