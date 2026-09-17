package com.rs.game.npc;

import lombok.Getter;

/**
 * ataraxia-server
 * paolo 21/09/2019
 * #Shnek6969
 */
public class PetMorph {
    @Getter
    public int npcId;
    @Getter
    public String dialogOption;

    public PetMorph(int npcId, String dialogOption){
        this.npcId = npcId;
        this.dialogOption = dialogOption;
    }
}
