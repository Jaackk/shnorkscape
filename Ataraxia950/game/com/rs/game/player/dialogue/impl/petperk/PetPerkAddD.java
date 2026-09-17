package com.rs.game.player.dialogue.impl.petperk;

import com.rs.cache.loaders.NPCDefinitions;
import com.rs.game.player.content.pet.Pets;
import com.rs.game.player.content.petperks.ObtainedPet;
import com.rs.game.player.content.petperks.PetPerk;
import com.rs.game.player.content.petperks.PetPerkInterface;
import com.rs.game.player.content.petperks.PetPerkUtils;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

import java.util.Collections;

/**
 * ataraxia-server
 * paolo 11/07/2019
 * #Shnek6969
 */
public class PetPerkAddD extends Dialogue {
    /**
     * pet to add the perk to
     */
    private ObtainedPet pet;
    /**
     * represents the page the player is atm.
     */
    private int page = 0;

    @Override
    public void start() {
        pet = (ObtainedPet) parameters[0];
        if(pet == null) {
            sendDialogue("You have to own a pet before you can add perks to them.");
            stage = 10;
        } else{
            sendOptionsDialogue("Select a perk you would like to add",
                    getOptions(0,4));
            stage = 1;
        }
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage){
            case 1:
                if(componentId == OPTION_5){
                    sendOptionsDialogue("Select a perk you would like to add", getOptions(4,PetPerk.values().length));
                    page = 1;
                    break;
                }
                if(page == 1 && componentId == OPTION_5){
                    page = 0;
                    sendOptionsDialogue("Select a perk you would like to add",
                            getOptions(0,4));
                    stage = 1;
                    return;
                }
                if(enoughPerks(componentId)){

                    int index = getIndexByComponent(componentId);
                    PetPerk perk = PetPerk.values()[index + page *4];
                    if(PetPerkUtils.hasMaxTier(perk,pet)){
                        sendDialogue("Your pet already has max tier of this perk.");
                        stage = 10;
                        break;
                    }
                    player.getObtainedPetPerks().remove(perk);
                    pet.getPerks().add(perk);
                    PetPerkInterface.sendPetInformation(player, Pets.getBabyPets().get(pet.getPetItemId()));
                    sendDialogue("The "+perk.getName()+" perk has been added to "+ NPCDefinitions.getNPCDefinitions(Pets.getBabyPets().get(pet.getPetItemId()).getBabyNpcId()).getName()+".");
                    stage = 10;
                } else {
                    sendDialogue("You don't have credits of this perk.");
                    stage = 10;
                }
                break;
            case 10:
                end();
                break;
        }
    }

    /**
     * checks if the player has the perk in his collection
     * @param option
     * @return
     */
    private boolean enoughPerks(int option){
        int index = getIndexByComponent(option);
        if(index < 4){
            int amount = Collections.frequency(player.getObtainedPetPerks(), PetPerk.values()[index + page *4]);
            return amount > 0;
        }
        return false;
    }

    /**
     * returns an array of options with perk names and #
     * @param startIndex
     * @param endIndex
     * @return
     */
    private String[] getOptions(int startIndex, int endIndex){
        String[] options = new String[endIndex - startIndex + 1];
        for(int i = startIndex; i <endIndex; i++){
            PetPerk p = PetPerk.values()[i + page *4];
            int amount = Collections.frequency(player.getObtainedPetPerks(), p);
            options[i - startIndex] = p.getName()+", amount: "+(amount > 0 ? Colors.GREEN : Colors.RED)+amount;
        }
        options[options.length -1] = "More...";
        return options;
    }

    /**
     * returns the index based on the option that is clicked.
     * @param component
     * @return
     */
    private int getIndexByComponent(int component){
        switch (component){
            case OPTION_1:
                return 0;
            case OPTION_2:
                return 1;
            case OPTION_3:
                return 2;
            case OPTION_4:
                return 3;
            case OPTION_5:
                return 4;
        }
        return 0;
    }

    @Override
    public void finish() { }
}
