package com.rs.game.player.content.skillingcontracts;

import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class TempEffectsD extends Dialogue {

    TempContractEffect selected;

    @Override
    public void start() {
        loadMainMenu();
    }

    @Override
    public void run(int interfaceId, int componentId) {

        switch (stage) {
            case -1:
                loadMainMenu();
                stage = 0;
                break;
            case 0:
                int option = componentToOption(componentId);
                selected = TempContractEffect.OPTIONS_EFFECT.get(option);
                if (selected == null)
                    throw new IllegalStateException("Could not retrieve contract modification. Option " + option);
                int count = player.getContracts().getEffects().count(selected);
                if (count > 1) {
                    sendNPCDialogue(943, ANGRY, "You have " + count + " contracts remaining until this effect wears off.");
                    stage = -1;
                } else if (count == 1) {
                    sendNPCDialogue(943, ANGRY, "You have 1 contract remaining until this effect wears off.");
                    stage = -1;
                } else {
                    sendNPCDialogue(943, ANGRY, "This will cost you " + selected.cost + " Skilling tickets, and will last for " + TempContractEffect.DURATION + " contracts.",
                            "Are you sure you would like to purchase this?");
                    stage = 1;
                }
                break;
            case 1:
                sendOptionsDialogue("Select an option.", "Yes", "No");
                stage = 2;
                break;
            case 2:
                if (componentId == OPTION_1) {
                    if (player.getContracts().getTotalTickets() < selected.cost) {
                        sendNPCDialogue(943, ANGRY, "Are you kidding me? You don't even have enough Skilling tickets for this!");
                    } else if (player.getContracts().getEffects().contains(selected)) {
                        sendNPCDialogue(943, ANGRY, "This effect is already active.");
                    } else {
                        int duration = TempContractEffect.DURATION;
                        if (player.getPerkManager().hasPerkActive(DonationPerk.SKILLING_ADDICT)) {
                            duration *= 2;
                        }
                        player.getContracts().removeTickets(selected.cost);
                        player.getContracts().getEffects().add(selected, duration);
                        sendNPCDialogue(943, ANGRY, "Done. Are you finished bothering me now?");
                    }
                    stage = -1;
                } else if (componentId == OPTION_2) {
                    loadMainMenu();
                }
                break;
        }
    }

    @Override
    public void finish() {
        selected = null;
        player.getInterfaceManager().closeChatBoxInterface();
    }

    public void loadMainMenu() {
        sendOptionsDialogue("Select an option.",
                TempContractEffect.ALL.stream().map(e ->
                        player.getContracts().getEffects().contains(e) ?
                                Colors.GREEN + e.description : Colors.RED + e.description).
                        toArray(String[]::new));
        stage = 0;
    }
}
