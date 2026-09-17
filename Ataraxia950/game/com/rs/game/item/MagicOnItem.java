package com.rs.game.item;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.activites.Sawmill;
import com.rs.game.activites.Sawmill.Plank;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.crafting.Enchanting;
import com.rs.game.player.actions.smithing.SuperHeating;
import com.rs.game.player.content.InterfaceManager;
import com.rs.game.player.content.ItemConstants;
import com.rs.game.player.content.Magic;
import com.rs.utils.EconomyPrices;

import java.util.Arrays;
import com.rs.utils.EconomyPrices.AlchTier;
/**
 * Handles Magic interface being used on inventory items.
 *
 * @author Noel.
 */
public class MagicOnItem {

    public static final int LOW_ALCHEMY = 38;
    public static final int HIGH_ALCHEMY = 59;
    public static final int SUPER_HEAT = 50;
    public static final int LV1_ENCHANT = 29;
    public static final int LV2_ENCHANT = 41;
    public static final int LV3_ENCHANT = 53;
    public static final int LV4_ENCHANT = 61;
    public static final int LV5_ENCHANT = 76;
    public static final int LV6_ENCHANT = 88;
    public static final int PLANK_MAKE = 33;

    private static int enchantLevel(final int itemId) {
        final int[] HYDRIX_ITEMS = {31857, 31859, 31863, 31865};
        return Arrays.stream(HYDRIX_ITEMS).anyMatch(i -> i == itemId) ? 7 : 6;
    }

    /**
     * Handles all of the available spell ID's on items.
     *
     * @param player The magician.
     * @param magicId The spell ID.
     * @param item The Item used on.
     */
    public static boolean handleMagic(Player player, int magicId, Item item) { 
        return handleMagic(player, magicId, item, false);
    }
    
    public static boolean handleMagic(Player player, int magicId, Item item, boolean usingBorrowed) {
        int itemId = item.getId();
        switch (magicId) {
            case 35:
                processDungeoneeringAlchemy(player, item, true);
                break;

            case 46:
                processDungeoneeringAlchemy(player, item, false);
                break;

            case LOW_ALCHEMY:
                return processAlchemy(player, item, true);

            case HIGH_ALCHEMY:
                return processAlchemy(player, item, false, usingBorrowed);

            case SUPER_HEAT:
                return SuperHeating.process(player, itemId, item);
                

            case LV1_ENCHANT:
                return Enchanting.startEnchant(player, itemId, 1);


            case LV2_ENCHANT:
                return  Enchanting.startEnchant(player, itemId, 2);


            case LV3_ENCHANT:
                return Enchanting.startEnchant(player, itemId, 3);


            case LV4_ENCHANT:
                return Enchanting.startEnchant(player, itemId, 4);


            case LV5_ENCHANT:
                return  Enchanting.startEnchant(player, itemId, 5);


            case LV6_ENCHANT:
                return  Enchanting.startEnchant(player, itemId, enchantLevel(itemId));
  

            case PLANK_MAKE: // plank make
                player.getInterfaceManager().openGameTab(InterfaceManager.MAGIC_BOOK_TAB);
                Plank plank = Sawmill.getPlankForLog(item.getId());
                if (plank == null) {
                    player.sendMessage("You can only convert plain, oak, teak and mahogany logs into planks.");
                    return false;
                }
                if (!Magic.checkRunes(player, true, 9075, 2, 557, 15, 561, 1))
                    return false;
                player.lock(2);
                player.getInventory().deleteItem(item);
                player.getInventory().addItem(new Item(plank.getId()));
                player.getSkills().addXp(Skills.MAGIC, 90);
                player.setNextAnimation(new Animation(4413));
                player.setNextGraphics(new Graphics(1063, 0, 100));
                return true;

            default:
                if (player.isDeveloper())
                    player.sendMessage("Invalid Magic Id: " + magicId + "; itemId: " + item.getId() + ".");
                return false;
        }
        return false;
    }

    /**
     * Processes Low & High Alchemy.
     *
     * @param player The magician.
     * @param item The item being used.
     * @param low if low alchemy.
     */
    
    public static boolean processAlchemy(Player player, Item item, boolean low) {
        return processAlchemy(player, item, low, false);
    }
    
    public static boolean processAlchemy(Player player, Item item, boolean low, boolean usingBorrowed) {
        int weaponId = player.getEquipment().getWeaponId();
        int shieldId = player.getEquipment().getShieldId();
        if (player.isLocked())
            return false;
        player.getInterfaceManager().openGameTab(7);
        if (player.getActionManager().getActionDelay() != 0)
            return false;
        boolean hasPerk = player.getPerkManager().hasPerkActive(DonationPerk.ARCANE_ALCHEMIST);
        if (hasPerk || ((!usingBorrowed && Magic.checkSpellRequirements(player, (low == true ? 21 : 55), (ItemConstants.isTradeable(item) && item.getId() != 995), 554, (low == true ? 3 : 5), 561, 1)))
                || ((usingBorrowed && Magic.checkRunes(player, Magic.getSpellData(157), false)))) {
            if (!ItemConstants.isTradeable(item) || item.getId() == 995) {
                player.sendMessage("You can't " + (low == true ? "low" : "high") + " alch this!");
                return false;
            }
            if (Magic.hasInfiniteRunes(554, weaponId, shieldId) && !player.getCombatDefinitions().isSheathe()) {
                player.setNextAnimation(new Animation(low == true ? 9625 : 9633));
                player.setNextGraphics(new Graphics(low == true ? 1692 : 1693));
                player.getPackets().sendSound(low ? 97 : 97, 0, 1);
            } else {
                player.setNextAnimation(new Animation(low == true ? 712 : 713));
                player.setNextGraphics(new Graphics(low == true ? 112 : 113));
                player.getPackets().sendSound(low ? 97 : 97, 0, 1);
            }
            player.getActionManager().setActionDelay(low == true ? 3 : 5);
            player.getInventory().deleteItem(item.getId(), 1);
            if (!hasPerk)
                player.getSkills().addXp(Skills.MAGIC, (low == true ? 31 : 65));
            if (item.getDefinitions().isNoted())
                item.setId(item.getDefinitions().certId);
            AlchTier tier = low ? AlchTier.LOW : AlchTier.HIGH;
            player.addMoney(EconomyPrices.getAlchCoins(item, tier));
            if (usingBorrowed)
                Magic.resetUsingBorrowedPowerSpell(player);
            return true;
        }
        return false;
    }

    public static void processDungeoneeringAlchemy(Player player, Item item, boolean low) {
//        int weaponId = player.getEquipment().getWeaponId();
//        int shieldId = player.getEquipment().getShieldId();
        if (player.isLocked())
            return;
        player.getInterfaceManager().openGameTab(7);
        if (player.getActionManager().getActionDelay() != 0)
            return;
//        if (Magic.checkCombatSpell(player, low ? 35 : 46, -1, false)) {
//            if (!ItemConstants.isTradeable(item)) {
//                player.sendMessage("You can't " + (low == true ? "low" : "high") + " alch this!");
//                return;
//            }
//            if (Magic.hasInfiniteRunes(17783, weaponId, shieldId) && !player.getCombatDefinitions().isSheathe()) {
//                player.setNextAnimation(new Animation(low == true ? 9625 : 9633));
//                player.setNextGraphics(new Graphics(low == true ? 1692 : 1693));
//                player.getPackets().sendSound(low ? 98 : 97, 0, 1);
//            } else {
//                player.setNextAnimation(new Animation(low == true ? 712 : 713));
//                player.setNextGraphics(new Graphics(low == true ? 112 : 113));
//                player.getPackets().sendSound(low ? 98 : 97, 0, 1);
//            }
//            Magic.checkCombatSpell(player, low ? 35 : 46, -1, true);
//            player.getActionManager().setActionDelay(low == true ? 3 : 5);
//            player.getInventory().deleteItem(item.getId(), 1);
//            player.getSkills().addXp(Skills.MAGIC, (low == true ? 31 : 65));
//            player.getInventory().addItem(new Item(18201, (int) ((item.getDefinitions().getValue() * item.getDefinitions().getDungShopValueMultiplier()) * (low ? 0.5D : 0.75D))));
//        }
    }
}