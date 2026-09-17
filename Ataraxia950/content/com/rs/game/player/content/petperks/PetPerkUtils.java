package com.rs.game.player.content.petperks;

import com.rs.game.player.Player;
import com.rs.game.player.content.pet.Pets;
import com.rs.utils.Colors;

import java.util.stream.Collectors;

/**
 * ataraxia-server paolo 10/07/2019 #Shnek6969
 */
public class PetPerkUtils {
    /**
     * checks if the item is indeed a perk item
     * 
     * @param itemId
     * @return
     */
    private static PetPerk isPerkItem(int itemId) {
        for (PetPerk perk : PetPerk.values()) {
            if (perk == null)
                continue;

            if (perk.getItemId() == itemId)
                return perk;
        }
        return null;
    }

    /**
     * summons the pet
     * 
     * @param player
     * @param pet
     */
    public static void summonPet(Player player, Pets pet) {
        if (pet == null) {
            player.getDialogueManager().startDialogue("SimpleMessage", "You have not selected a pet.");
            return;
        }
        if (getObtainedPetByItem(player, pet.getBabyItemId()) == null) {
            player.getDialogueManager().startDialogue("SimpleMessage", "You have not obtained this pet yet.");
            return;
        }
        if (!player.getControlerManager().canSummonFamiliar())
            return;
        player.closeInterfaces();
        player.setCurrentPet(getObtainedPetByItem(player, pet.getBabyItemId()));
        player.getPetManager().spawnPet(pet.getBabyItemId(), true);
    }

    /**
     * checks if the player already has a certain pet by spawnitem
     * 
     * @param player
     * @param itemId
     * @return
     */
    public static ObtainedPet getObtainedPetByItem(Player player, int itemId) {
        for (ObtainedPet pet : player.getObtainedPets())
            if (pet.getPetItemId() == itemId)
                return pet;
        return null;
    }

    /**
     * Checks if the player has obtained a pet by it's id.
     * @param player
     * @param itemId
     * @return
     */
    public static boolean hasObtainedPetById(Player player, int itemId) {
        for (ObtainedPet pet : player.getObtainedPets())
            if (pet.getPetItemId() == itemId)
                return true;
        return false;
    }

    /**
     * adds the pet the the obtained collection.
     * 
     * @param player
     * @param pet
     */
    public static void redeemPet(Player player, Pets pet) {
        if (hasObtainedPetById(player, pet.getBabyItemId())) {
             player.getDialogueManager().startDialogue("SimpleMessage", "You already have this pet obtained. Use the command ;;pets to spawn your pets.");
            return;
        }
        if(pet == Pets.FREEZY_ICE || pet  == Pets.FREEZY_JUNGLE || pet == Pets.FREEZY_LAVA || pet == Pets.FREEZY_SAND) {
            player.getObtainedPets().add(new ObtainedPet(Pets.FREEZY_ICE.getBabyItemId()));
            player.getObtainedPets().add(new ObtainedPet(Pets.FREEZY_JUNGLE.getBabyItemId()));
            player.getObtainedPets().add(new ObtainedPet(Pets.FREEZY_LAVA.getBabyItemId()));
            player.getObtainedPets().add(new ObtainedPet(Pets.FREEZY_SAND.getBabyItemId()));
            player.sm(Colors.RED+"All 4 versions of freezy have been added to your pets list.");
        }
        player.getInventory().deleteItem(pet.getBabyItemId(), 1);
        player.getObtainedPets().add(new ObtainedPet(pet.getBabyItemId()));
        player.sm(Colors.RED + "Your pet was obtained, you can spawn it by doing the ;;pets command.");
    }

    /**
     * returns the current tier of the player
     * 
     * @param perk
     * @param pet
     * @return
     */
    public static int getPerkTier(PetPerk perk, ObtainedPet pet) {
        if(pet == null)
            return 0;
        int tier = pet.getPerks().stream().filter(item -> item.equals(perk)).collect(Collectors.toList()).size();
        return tier;
    }

    /**
     * checks if the player already has maxtier
     * 
     * @param perk
     * @param pet
     * @return
     */
    public static boolean hasMaxTier(PetPerk perk, ObtainedPet pet) {
        int tier = getPerkTier(perk, pet);
        return tier >= 3;
    }

    /**
     *
     * @param player
     * @param itemId
     */
    public static void redeemPerkItem(Player player, int itemId) {
        PetPerk perk = isPerkItem(itemId);
        if (perk == null)
            return;
        if (!player.getInventory().containsItem(itemId, 1))
            return;
        player.getInventory().deleteItem(itemId, 1);
        player.handleDonation(5, "Pet Perk");
        player.getObtainedPetPerks().add(perk);
        player.sm(Colors.RED + perk.getName() + " has been added to your obtainable perk list. You can use it with the ;;pets command.");

    }

    /**
     * returns the pro modifier of the perk
     * 
     * @param player
     * @param perk
     * @return
     */
    public static double getProModifierForPerk(Player player, PetPerk perk) {
        int tier = getPerkTier(perk, player.getCurrentPet());
        double rate = (perk == null ? 0 : Math.max(0.01, perk.getProMultiplier() * tier));
        return rate;
    }

    /**
     * returns the con modifier of a perk, based on tier
     * 
     * @param player
     * @param perk
     * @return
     */
    public static double getConModifierForPerk(Player player, PetPerk perk) {
        int tier = getPerkTier(perk, player.getCurrentPet());
        double rate = (perk == null ? 0 : Math.max(0.01, perk.getConMultiplier() * tier));
        return rate;
    }

    /**
     * checks if the pet has a certain pet
     * 
     * @param pet
     * @param perk
     * @return
     */
    public static boolean petHasPerk(ObtainedPet pet, PetPerk perk) {
        if (pet == null)
            return false;
        return pet.getPerks().contains(perk);
    }

}
