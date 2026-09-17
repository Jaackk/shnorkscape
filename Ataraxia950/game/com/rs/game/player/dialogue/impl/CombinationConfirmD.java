package com.rs.game.player.dialogue.impl;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.player.content.interfaces.combinations.CombinationData;
import com.rs.game.player.content.interfaces.combinations.CombinationsInterface;
import com.rs.game.player.dialogue.Dialogue;

/**
 * ataraxia-server
 * paolo 15/08/2019
 * #Shnek6969
 */
public class CombinationConfirmD extends Dialogue {



    @Override
    public void start() {
        CombinationData combinationData = (CombinationData) player.getTemporaryAttributtes().get("CombinationData");
        sendOptionsDialogue("Are you sure you want to create "+ ItemDefinitions.getItemDefinitions(combinationData.getProductId()).getName()+"?", "Yes", "No");
        stage = 1;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        if(stage == 1){
            if(componentId == OPTION_1)
                CombinationsInterface.combine(player);
            end();
        }
    }

    @Override
    public void finish() {

    }
}
