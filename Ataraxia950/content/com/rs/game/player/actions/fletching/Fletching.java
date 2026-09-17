package com.rs.game.player.actions.fletching;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.activites.halloween.ClueScrollDistributor;
import com.rs.game.item.Item;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.actions.fletching.defs.Fletchables;
import com.rs.game.player.content.packs.portable.PortableType;
import com.rs.game.player.content.petperks.PetPerkHandler;
import com.rs.game.player.content.skillingcontracts.impl.FletchingContractList;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

import java.util.ArrayList;

/**
 * Fletching.java | 11:44:44 AM
 *
 * @author Chryonic
 * @date Apr 15, 2017
 */
public class Fletching extends Action {

    public static final int KNIFE = 946, DUNGEONEERING_KNIFE = 17754, CHISLE = 1755;
    public static final int CROSSBOW_STRING = 9438;

    private final Fletchables fletchables;
    private final int option;
    private final boolean portable;

    private int ticks;

    public Fletching(Fletchables fletchables, int option, int ticks, boolean portable) {
        this.fletchables = fletchables;
        this.option = option;
        this.ticks = ticks;
        this.portable = portable;
    }

    public static Fletchables isFletching(Item first, Item second) {
        Fletchables fletchables = Fletchables.forId(first.getId());
        int selected;
        if (fletchables != null)
            selected = second.getId();
        else {
            fletchables = Fletchables.forId(second.getId());
            selected = first.getId();
        }
        return fletchables != null && fletchables.getSelected() == selected ? fletchables : null;
    }

    public static boolean isFletching(Player player, int logId) {
        for (Fletchables fletchables : Fletchables.values()) {
            if (fletchables.getId() == logId) {
                FletchingRs3Dialogue.sendFletchingInterface(player, fletchables, false);
                return true;
            }
        }
        return false;
    }

    public static void performPortableAction(Player player, WorldObject object, int ordinal) {
        ArrayList<Integer> possibleFletchables = new ArrayList<Integer>();
        for (Fletchables fletch : Fletchables.values()) {
            if (player.getInventory().containsItem(fletch.getId(), 1))
                possibleFletchables.add(fletch.getId());
        }

        if (possibleFletchables.isEmpty()) {
            player.sendMessage("You do not have anything to fletch.");
            return;
        }

        Item preferredItem = getPreferredItemToUse(player, possibleFletchables);
        if (preferredItem == null) {
            player.sendMessage("You do not have anything to fletch.");
            return;
        }

        Fletchables preferredFletchable = Fletchables.forId(preferredItem.getId());
        if (preferredFletchable == null)
            return;

        ArrayList<Integer> possibleSelected = new ArrayList<Integer>();
        for (Fletchables fletch : Fletchables.values()) {
            if ((fletch.getSelected() == 314 || player.getInventory().containsItem(fletch.getSelected(), 1)) &&
                    fletch.getId() == preferredFletchable.getId())
                possibleSelected.add(fletch.getSelected());
        }
        if (player.getPerkManager().hasPerkActive(DonationPerk.MASTER_FLEDGER) && !possibleSelected.contains(1777)) {
            possibleSelected.add(1777);
        }

        if (possibleSelected.isEmpty()) {
            player.sendMessage("You do not have the appropriate items to fletch.");
            return;
        }

        Item preferredSelected = getPreferredItemToUse(player, possibleSelected);
        if (preferredSelected == null)
            return;

        Fletchables fletchable = isFletching(preferredItem, preferredSelected);
        if (fletchable != null) {
            switch (ordinal) {
                case 1:
                    FletchingRs3Dialogue.sendFletchingInterface(player, fletchable, PortableType.isPortableObject(object.getId()));
                    break;
                case 2:
                    if (!isHeadlessArrow(fletchable) && !isUnfinishedBolt(fletchable)) {
                        player.sendMessage("You have no ammo to fletch.");
                        return;
                    }
                    FletchingRs3Dialogue.sendFletchingInterface(player, fletchable, PortableType.isPortableObject(object.getId()));
                    break;
                case 3:
                    if (!isStrungBow(fletchable)) {
                        player.sendMessage("You have no items that need stringing.");
                        return;
                    }
                    FletchingRs3Dialogue.sendFletchingInterface(player, fletchable, PortableType.isPortableObject(object.getId()));
                    break;
            }
        }
    }

    private static Item getPreferredItemToUse(Player player, ArrayList<Integer> ints) {
        Item temp = null;
        for (int i : ints) {
            if (temp == null || player.getInventory().getNumberOf(i) > temp.getAmount())
                temp = new Item(i, player.getInventory().getNumberOf(i));
        }
        return temp;
    }

    @Override
    public boolean start(Player player) {
        if (option >= fletchables.getProduct().length)
            return false;
        return process(player);
    }

    private static boolean isLogItem(int itemId) {
        return itemId == 1511 || itemId == 1521 || itemId == 1519 || itemId == 1517 || itemId == 1515 || itemId == 1513 || itemId == 29556;
    }

    public static boolean requiresTool(Fletchables fletchables) {
        return fletchables.getSelected() == KNIFE || fletchables.getSelected() == DUNGEONEERING_KNIFE
                || fletchables.getSelected() == CHISLE;
    }

    public static int getPrimaryAmountRequired(Fletchables fletchables, int option) {
        int productId = fletchables.getProduct()[option];
        if (productId == 52 && isLogItem(fletchables.getId()))
            return 1;
        return getAmountProduced(fletchables, option);
    }

    public static int getSecondaryAmountRequired(Player player, Fletchables fletchables, int option) {
        if (requiresTool(fletchables))
            return 0;
        if ((fletchables.getSelected() == 1777 || fletchables.getSelected() == 314)
                && player.getPerkManager().hasPerkActive(DonationPerk.MASTER_FLEDGER))
            return 0;
        return getAmountProduced(fletchables, option);
    }

    public static int getAmountProduced(Fletchables fletchables, int option) {
        int productId = fletchables.getProduct()[option];
        if (productId == 52 && isLogItem(fletchables.getId())) {
            switch (fletchables.getId()) {
                case 1511:  return 15;
                case 1521:  return 30;
                case 1519:  return 45;
                case 1517:  return 60;
                case 1515:  return 75;
                case 1513:  return 90;
                case 29556: return 105;
                default:    return 15;
            }
        }
        if (productId == 53)
            return 15;
        if (isHeadlessArrow(fletchables))
            return 15;
        if (isUnfinishedBolt(fletchables))
            return 10;
        if (isDartFletching(fletchables))
            return 10;
        return 1;
    }

    @Override
    public boolean process(Player player) {
        if (ticks <= 0)
            return false;
        if (player.clickedObject != null) {
            if (!World.containsObjectWithId(player.clickedObject, player.clickedObject.getId()))
                return false;
        }
        if (player.getSkills().getLevel(Skills.FLETCHING) < fletchables.getLevel()[option]) {
            player.sendMessage("You need a fletching level of " + fletchables.getLevel()[option] + " to fletch this.");
            return false;
        }
        if (requiresTool(fletchables) && !player.getInventory().containsOneItem(fletchables.getSelected())) {
            player.sendMessage("You will need a "
                    + ItemDefinitions.getItemDefinitions(fletchables.getSelected()).getName().toLowerCase()
                    + " in order to fletch this.");
            return false;
        }
        int primaryRequired = getPrimaryAmountRequired(fletchables, option);
        int secondaryRequired = getSecondaryAmountRequired(player, fletchables, option);
        if (primaryRequired > 0 && !player.getInventory().containsOneItem(fletchables.getId(), primaryRequired))
            return false;
        if (secondaryRequired > 0 && !player.getInventory().containsOneItem(fletchables.getSelected(), secondaryRequired))
            return false;
        if (fletchables.getId() == 28436 || fletchables.getSelected() == 28436) {
            if (player.getInventory().getAmountOf(28436) < 15) {
                player.sendMessage(Colors.RED + "You will need at least 15 of these to craft ascension bolts!", false);
                return false;
            }
        }
        return true;
    }

    @Override
    public int processWithDelay(Player player) {
        if (player.getAnimations().hasKarateFletch && player.getAnimations().karateFletch) {
            player.setNextAnimation(new Animation(17299));
            player.setNextGraphics(new Graphics(3300));
        } else
            player.setNextAnimation(fletchables.getAnim());

        int productId = fletchables.getProduct()[option];
        int amountProduced = getAmountProduced(fletchables, option);

        // inventory space check before doing anything
        if (!ItemDefinitions.getItemDefinitions(productId).isStackable()) {
            if (!player.getInventory().hasFreeSlots()) {
                player.sendMessage("You don't have enough inventory space to fletch more items.");
                stop(player);
                return -1; // Stop the action immediately
            }
        }


        // Handle perks
        boolean efficiencyPerkActive = PetPerkHandler.handleEfficiencyExpert(player, new Item(fletchables.getId(), 1));

        // Consume main resource if perk not active
        if (!efficiencyPerkActive) {
            // Arrow shaft from logs: only use 1 log per action
            if (productId == 52 && isLogItem(fletchables.getId())) {
                player.getInventory().deleteItem(fletchables.getId(), 1);
            }
            // Headless arrows: consume 15 shafts per batch (or whatever amountProduced is)
            else if (productId == 53) {
                player.getInventory().deleteItem(fletchables.getId(), amountProduced);
            }
            // Other fletching (like bolts/arrows): batch size might differ
            else {
                player.getInventory().deleteItem(fletchables.getId(), amountProduced);
            }
        }



        // Add produced items to inventory
        player.getInventory().addItem(productId, amountProduced);

        // Handle secondary resource deletion (feathers, arrow tips, etc.)
        if (fletchables.getSelected() != KNIFE && fletchables.getSelected() != DUNGEONEERING_KNIFE && fletchables.getSelected() != CHISLE &&
                !((fletchables.getSelected() == 1777 || fletchables.getSelected() == 314) && player.getPerkManager().hasPerkActive(DonationPerk.MASTER_FLEDGER))) {
            boolean saveFeather = fletchables.getSelected() == 314 && portable;
            if (!saveFeather) {
                player.getInventory().deleteItem(fletchables.getSelected(), amountProduced);
            }
        }

        player.addItemsFletched();

        // Portable fletcher random event
        if (portable && Utils.random(9) == 4) {
            if (isUnstrungBowOrStock(fletchables, option)) {
                player.getBank().addItem(new Item(fletchables.getId(), 1), true);
                player.sendMessage(Colors.GOLD + "<shad=000000>The portable fletcher saves you some resources. They have been sent to your bank.", true);
            }
        }

        // Final success message and XP reward
        String name = new Item(productId).getDefinitions().getName().replace("(u)", "");
        FletchingContractList.listen(player, productId);
        player.sendMessage("You successfully create " +
                (amountProduced > 1 ? "some " : Utils.getAorAn(name)) + name + "; " +
                "items fletched: " + Colors.RED + Utils.getFormattedNumber(player.getItemsFletched()) + "</col>.", true);
        ClueScrollDistributor.givePlayerClueScrollIfProbable(player, ClueScrollDistributor.SKILLING_PERCENT);
        player.getSkills().addXp(Skills.FLETCHING, (fletchables.getXp()[option] * amountProduced) * (portable ? 1.1 : 1));

        ticks--;
        return player.getPerkManager().hasPerkActive(DonationPerk.MASTER_FLEDGER) ? 1 : 1;
    }



    @Override
    public void stop(final Player player) {
        player.setNextAnimation(new Animation(-1));
        setActionDelay(player, 3);
        player.clickedObject = null;
    }

    private static boolean isUnstrungBowOrStock(Fletchables f, int option) {
        return ((f == Fletchables.REGULAR_BOW && f.getProduct()[option] != 52) || f == Fletchables.OAK_BOW || f == Fletchables.MAHOGANY_STOCK || f == Fletchables.WILLOW_BOW || f == Fletchables.MAPLE_BOW || f == Fletchables.YEW_BOW || f == Fletchables.MAGIC_BOW || f == Fletchables.ELDER_BOW);
    }

    private static boolean isHeadlessArrow(Fletchables f) {
        return (f == Fletchables.BRONZE_ARROWS || f == Fletchables.IRON_ARROWS || f == Fletchables.STEEL_ARROWS || f == Fletchables.MITHRIL_ARROWS || f == Fletchables.ADAMANT_ARROWS || f == Fletchables.FRAGMENT_ARROWS || f == Fletchables.RUNITE_ARROWS || f == Fletchables.DRAGON_ARROWS || f == Fletchables.BROAD_ARROWS || f == Fletchables.DRAGONBANE_ARROWS || f == Fletchables.FRAGMENT_ARROWS);
    }

    private static boolean isUnfinishedBolt(Fletchables f) {
        return (f == Fletchables.BRONZE_BOLT || f == Fletchables.IRON_BOLT || f == Fletchables.STEEL_BOLT || f == Fletchables.MITHRIL_BOLT || f == Fletchables.ADAMANT_BOLT || f == Fletchables.RUNITE_BOLT || f == Fletchables.OPAL_BOLTS || f == Fletchables.BLURITE_BOLTS || f == Fletchables.JADE_BOLTS || f == Fletchables.PEARL_BOLTS || f == Fletchables.SILVER_BOLTS || f == Fletchables.RED_TOPAZ_BOLTS || f == Fletchables.BARBED_BOLTS || f == Fletchables.SAPPHIRE_BOLTS || f == Fletchables.EMERALD_BOLTS || f == Fletchables.FRAGMENT_BOLTS || f == Fletchables.RUBY_BOLTS || f == Fletchables.DIAMOND_BOLTS || f == Fletchables.DRAGON_BOLTS || f == Fletchables.ONYX_BOLTS || f == Fletchables.DRAGONBANE_BOLTS || f == Fletchables.ASCENDRI_BOLTS || f == Fletchables.FRAGMENT_BOLTS);
    }

    private static boolean isStrungBow(Fletchables f) {
        return (f == Fletchables.STRUNG_SHORT_BOW || f == Fletchables.STRUNG_LONG_BOW || f == Fletchables.STRUNG_OAK_SHORT_BOW || f == Fletchables.STRUNG_OAK_LONG_BOW || f == Fletchables.STRUNG_WILLOW_SHORT_BOW || f == Fletchables.STRUNG_WILLOW_LONG_BOW || f == Fletchables.STRUNG_MAPLE_SHORT_BOW || f == Fletchables.STRUNG_MAPLE_LONG_BOW || f == Fletchables.STRUNG_YEW_SHORT_BOW || f == Fletchables.STRUNG_YEW_LONG_BOW || f == Fletchables.STRUNG_MAGIC_SHORT_BOW || f == Fletchables.STRUNG_MAGIC_LONG_BOW || f == Fletchables.STRUNG_ELDER_SHORT_BOW || f == Fletchables.STRUNG_ELDER_LONG_BOW || f == Fletchables.BRONZE_CBOW || f == Fletchables.IRON_CBOW || f == Fletchables.STEEL_CBOW || f == Fletchables.BLURITE_CBOW || f == Fletchables.MITHRIL_CBOW || f == Fletchables.ADAMANT_CBOW || f == Fletchables.RUNITE_CBOW || f == Fletchables.DRAGON_CBOW);
    }

    private static boolean isDartFletching(Fletchables f) {
        return (f == Fletchables.BRONZE_DART ||
                f == Fletchables.IRON_DART ||
                f == Fletchables.STEEL_DART ||
                f == Fletchables.MITHRIL_DART ||
                f == Fletchables.ADAMANT_DART ||
                f == Fletchables.RUNITE_DART ||
                f == Fletchables.DRAGON_DART);
                //f == Fletchables.ACB_DART; // Include any custom dart enums youve defined
    }


}
