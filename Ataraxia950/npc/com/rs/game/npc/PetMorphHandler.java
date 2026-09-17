package com.rs.game.npc;

import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;

/**
 * ataraxia-server
 * paolo 18/09/2019
 * #Shnek6969
 */
public class PetMorphHandler {

    public enum Morphs {

        KALPHITE_QUEEN(new PetMorph(20542,"Kalphite Grublet (ground)"),new PetMorph(20543,"Kalphite Grublet (flying)"));

        PetMorph[] petMorph;

        Morphs(com.rs.game.npc.PetMorph... morps){
            this.petMorph =morps;
        }

        public static Morphs getMorphByNPCId(int npc){
            for (Morphs value : Morphs.values()) {
                for (PetMorph possibleOption : value.petMorph) {
                    if(npc == possibleOption.getNpcId())
                        return value;

                }
            }
            return null;
        }
    }



    public static void sendMorphOptions(Morphs morphs, Player player){

        player.getDialogueManager().startDialogue(new Dialogue() {

            @Override
            public void start() {
                sendOptionsDialogue("Select an option",generateOptions(morphs));
            }

            @Override
            public void run(int interfaceId, int componentId) {
                NPC npc = player.getPet();
                npc.setNextNPCTransformation(morphs.petMorph[getIndexByOption(componentId)].getNpcId());
                end();
            }

            @Override
            public void finish() {

            }
        });

    }

    private static int getIndexByOption(int componentId){
       switch (componentId){
           case 11:
               return 0;
           case 13:
               return 1;
           case 14:
               return 2;
           case 15:
               return 3;
           case 16:
               return 4;
       }
       return 0;
    }

    private static String[] generateOptions(Morphs morphs){
        String[] options = new String[morphs.petMorph.length];
        for (int i=0; i < options.length; i++){
            options[i] = morphs.petMorph[i].getDialogOption();
        }
        return options;
    }


}

