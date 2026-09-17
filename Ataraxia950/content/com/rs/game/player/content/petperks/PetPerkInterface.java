package com.rs.game.player.content.petperks;

import com.rs.cache.loaders.NPCDefinitions;
import com.rs.cache.loaders.RenderAnimDefinitions;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.content.pet.Pets;
import com.rs.utils.Colors;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * ataraxia-server
 * paolo 09/07/2019
 * #Shnek6969
 */
public class PetPerkInterface {

    public static final int INTERFACE_ID = 1918;
    private static final int[] COMPONENT_LIST_IDS = {101,105,109,113,117,121,125,129,133,137,141,145,149,153};
    private static final int PERK_DESCRIPTION_COMPONENT = 171;
    private static final int PET_DESCRIPTION_COMPONENT = 77;
    private static final int PET_MODEL_CONTAINER = 173;
    private static final int BASE_ANIMATION = 9772;

    /**
     * sends the interface to the player
     * @param player
     */
    public static void sendInterface(Player player){
        cleanPetList(player);
        sendPets(player,0, PetPerkConstants.SkillingPets);
        player.getInterfaceManager().sendInterface(INTERFACE_ID);
        player.getTemporaryAttributtes().put("PetPerkReset", false);
        sendFavoriteInfo(player);
    }

    private static void sendFavoriteInfo(Player player){
        player.getPackets().sendText(INTERFACE_ID, 177,(player.getFavoritePet() == null) ? "No favorite pet selected.": "Favorite: "+NPCDefinitions.getNPCDefinitions(player.getFavoritePet().getBabyNpcId()).getName() );
    }
    /**
     * cleans the list on the interface
     * @param player
     */
    private static void cleanPetList(Player player){
        player.getPackets().sendEmptyTextToComponents(INTERFACE_ID, COMPONENT_LIST_IDS);
        player.getPackets().sendEmptyTextToComponents(INTERFACE_ID, PET_DESCRIPTION_COMPONENT,PERK_DESCRIPTION_COMPONENT);
    }

    /**
     * displaying the selected information of a pet.
     * @param player
     * @param pet
     */
    public static void sendPetInformation(Player player, Pets pet){
        NPCDefinitions npcDefinition = NPCDefinitions.getNPCDefinitions(pet.getBabyNpcId());
        RenderAnimDefinitions renderAnimDefinitions = RenderAnimDefinitions.getRenderAnimDefinitions(npcDefinition.getRenderAnimation());
        player.getPackets().sendIComponentModel(INTERFACE_ID, PET_MODEL_CONTAINER,npcDefinition.models[0]);
        player.getPackets().sendIComponentAnimation(renderAnimDefinitions.walkAnimation, INTERFACE_ID,PET_MODEL_CONTAINER);
        player.getPackets().sendText(INTERFACE_ID, PET_DESCRIPTION_COMPONENT, NPCDefinitions.getNPCDefinitions(pet.getBabyNpcId()).getName());
        ObtainedPet obtainedPet = PetPerkUtils.getObtainedPetByItem(player, pet.getBabyItemId());
        if(obtainedPet == null) {
            player.getPackets().sendText(INTERFACE_ID,PERK_DESCRIPTION_COMPONENT,"No perks added to this pet.");
        } else {
            String information = "";
            List<PetPerk> uniquePerks =obtainedPet.getPerks().stream().filter(distinctByKey(p -> p.getName())).collect(Collectors.toList()); //only display uniques
            for(PetPerk perk : uniquePerks){
                int tier = obtainedPet.getPerks().stream().filter(item -> item.equals(perk)).collect(Collectors.toList()).size();
                information+= Colors.WHITE+"<b>"+perk.getName()+"(Tier:"+tier+") </b><br>+ "+perk.getProDescription()+".<br>- "+perk.getConDescription()+".<br>";
            }
            if(information == "")
                information = "No perks added to this pet.";
            player.getPackets().sendText(INTERFACE_ID, PERK_DESCRIPTION_COMPONENT, information);
        }

    }

    /**
     * since java streams don't have a distinct function, please LINQ :'(
     * @param keyExtractor
     * @param <T>
     * @return
     */
    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    /**
     * sends the list of pets to the interface
     * @param player
     * @param pets
     */
    public static void sendPets(Player player,int page, Pets[] pets){
        player.getTemporaryAttributtes().put("PetPerkPage", page);
        player.getTemporaryAttributtes().put("PetPerkList", pets);
        cleanPetList(player);
        int pageIndex = 14 * page;
        for(int i = 0; i < 14; i++){
            if(i + pageIndex > pets.length -1) {
                player.getTemporaryAttributtes().put("PetPerkReset", true);
                return;
            }
            player.getPackets().sendText(INTERFACE_ID,COMPONENT_LIST_IDS[i],
                    ((PetPerkUtils.getObtainedPetByItem(player, pets[i+ pageIndex].getBabyItemId()) != null) ? Colors.GREEN : Colors.RED) +
                            NPCDefinitions.getNPCDefinitions(pets[i +pageIndex].getBabyNpcId()).getName());
        }
    }


    /**
     * handles button clicks
     * @param player
     * @param componentId
     */
    public static void handleButtons(Player player, int componentId){
        switch(componentId){
            case 20:
                sendPets(player,0, PetPerkConstants.BossPets);
                return;
            case 22:
                sendPets(player,0, PetPerkConstants.LegendaryPets);
                return;
            case 24:
                sendPets(player,0, PetPerkConstants.SkillingPets);
                return;
            case 26:
                sendPets(player,0, PetPerkConstants.FollowerPets);
                return;
            case 184:
                sendPets(player,0, PetPerkConstants.CombatPets);
                return;
            case 186:
                sendPets(player,0, PetPerkConstants.TreasureHunterPets);
                return;
            case 30:
                PetPerkUtils.summonPet(player, (Pets) player.getTemporaryAttributtes().get("SelectedPet"));
                return;
            case 160:
                boolean endPage = (boolean) player.getTemporaryAttributtes().get("PetPerkReset");
                int page = endPage ? 0 : (int) player.getTemporaryAttributtes().get("PetPerkPage") + 1;
                if(endPage) //disabling it again
                    player.getTemporaryAttributtes().put("PetPerkReset", false);
                Pets[] petList = (Pets[]) player.getTemporaryAttributtes().get("PetPerkList");
                sendPets(player,page,petList);
                return;
            case 178:
                if(player.isGroupIronman()) {
                    player.getDialogueManager().startDialogue("SimpleMessage", "Group Ironmen cannot use pet perks.");
                    return;
                }
                Pets pet = (Pets) player.getTemporaryAttributtes().get("SelectedPet");
                 petList = (Pets[]) player.getTemporaryAttributtes().get("PetPerkList");
                if((petList.equals(PetPerkConstants.SkillingPets) || petList.equals(PetPerkConstants.BossPets)) && !player.getPerkManager().hasPerkActive(DonationPerk.PET_TRAINER)){
                    player.getDialogueManager().startDialogue("SimpleMessage", "You need to unlock the Pet Trainer perk before you can add Pet Perks to bossing and skilling pets.");
                    return;
                }
                if(pet != null)
                    player.getDialogueManager().startDialogue("PetPerkAddD", PetPerkUtils.getObtainedPetByItem(player, pet.getBabyItemId()));
                else
                    player.getDialogueManager().startDialogue("SimpleMessage", "Please select a pet first.");
                return;
            case 172:
                pet = (Pets) player.getTemporaryAttributtes().get("SelectedPet");
                if(pet != null ) {
                   if(PetPerkUtils.getObtainedPetByItem(player,pet.getBabyItemId()) == null){
                       player.getDialogueManager().startDialogue("SimpleMessage", "You need to unlock a pet before you can favorite it.");
                       return;
                   }
                    player.setFavoritePet(pet);
                    player.getDialogueManager().startDialogue("SimpleMessage", "You have set "+NPCDefinitions.getNPCDefinitions(pet.getBabyNpcId()).getName()+" as your favorite pet. You can now spawn him quicker.");
                    sendFavoriteInfo(player);
                }
                else
                    player.getDialogueManager().startDialogue("SimpleMessage", "Please select a pet first.");
                return;
            case 176:
                if(player.getFavoritePet() == null){
                    player.getDialogueManager().startDialogue("SimpleMessage", "You have not selected a favorite pet.");
                    return;
                }
                PetPerkUtils.summonPet(player, player.getFavoritePet());
                return;
            case 179:
                 pet = (Pets) player.getTemporaryAttributtes().get("SelectedPet");
                if(pet != null) {
                    ObtainedPet obtainedPet = PetPerkUtils.getObtainedPetByItem(player, pet.getBabyItemId());
                    if(obtainedPet == null || obtainedPet.getPerks() == null){
                        player.getDialogueManager().startDialogue("SimpleMessage", "You have no perks on this pet to remove.");
                        return;
                    }
                    player.getDialogueManager().startDialogue("PetPerkRemoveD", PetPerkUtils.getObtainedPetByItem(player, pet.getBabyItemId()));
                }else
                    player.getDialogueManager().startDialogue("SimpleMessage", "Please select a pet first.");
                return;
            case 180:
                if(player.getFavoritePet() == null){
                    player.getDialogueManager().startDialogue("SimpleMessage", "You have not selected a favorite pet.");
                    return;
                }
                player.getDialogueManager().startDialogue("PetPerkFavoriteD");
                return;
        }
        for(int i = 0; i < COMPONENT_LIST_IDS.length; i++){
            if(COMPONENT_LIST_IDS[i] == componentId){
                int currentPage = (int) player.getTemporaryAttributtes().get("PetPerkPage");
                int pageIndex = 14* currentPage;
                Pets[] petList = (Pets[]) player.getTemporaryAttributtes().get("PetPerkList");
                if(i+pageIndex > petList.length -1)
                    return;
                player.getTemporaryAttributtes().put("SelectedPet", petList[i + pageIndex]);
                sendPetInformation(player, petList[i+ pageIndex]);
                return;
            }
        }
    }
}
