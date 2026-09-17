package com.rs.game.player.content.petperks;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * ataraxia-server
 * paolo 10/07/2019
 * #Shnek6969
 */
public class ObtainedPet implements Serializable {
    /**
     * Dev note:we don't want Serialisation to save a Pets object since that would cause a lot of problems later.
     * we can use the petItemId to get the pet object. Pets.get...
     */
    @Getter@Setter
    public int petItemId;
    @Getter@Setter
    public List<PetPerk> perks;

    public ObtainedPet(int petItemId){
        this.petItemId = petItemId;
        this.perks = new ArrayList<>();
    }

}
