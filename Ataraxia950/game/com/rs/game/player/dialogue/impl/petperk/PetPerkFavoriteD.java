package com.rs.game.player.dialogue.impl.petperk;

import com.rs.game.player.dialogue.Dialogue;

/**
 * ataraxia-server
 * paolo 14/07/2019
 * #Shnek6969
 */
public class PetPerkFavoriteD extends Dialogue {
    @Override
    public void start() {
        sendOptionsDialogue("Favorite settings", "Favorite familiar as override for other pets: "+player.isUseFavoriteAsOverride(), "Close.");
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage){
            case -1:
                if(componentId == OPTION_1){
                    player.setUseFavoriteAsOverride(!player.isUseFavoriteAsOverride());
                    player.getLocalNPCUpdate().reset();
                    sendDialogue("Your favorite familair is now an override for other pets: "+player.isUseFavoriteAsOverride()+".");
                    stage = 10;
                } else {
                    end();
                }
                break;
            case 10:
                end();
                break;

        }

    }

    @Override
    public void finish() {

    }
}
