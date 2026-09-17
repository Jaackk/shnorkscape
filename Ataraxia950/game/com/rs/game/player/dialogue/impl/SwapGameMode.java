package com.rs.game.player.dialogue.impl;

import com.rs.game.player.content.Toolbelt;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.SerializableFilesManager;
import com.rs.utils.mysql.QueryExecutor;
import com.rs.utils.mysql.impl.Hiscores;
import com.rs.utils.mysql.impl.ResetUserHiscores;

public class SwapGameMode extends Dialogue {

    private String choice;
    private final String[] modes = {
            "Novice", "Intermediate", "Expert", "Legendary"
    };
    
    @Override
    public void start() {
        sendOptionsDialogue("What game mode would you like?",
                "Novice - 100x XP - +0% drop rate",
                "Intermediate - 50x XP - +5% drop rate",
                "Expert - 25x XP - +10% drop rate",
                "Legendary - 5x XP - +15% drop rate",
                "Nevermind");
        stage = 0;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch(stage) {
        case 0:
            if (componentId == OPTION_5)
                end();
            else {
                if (!player.getEquipment().wearingArmour()) {
                    int option = getOrdinal(componentId);
                    sendDialogue(Colors.RED+Colors.SHAD+"WARNING: To become "+
                            (Colors.GREEN+modes[option])+" mode"+Colors.RED+
                            " you will lose ALL of your skill levels AND prismatic XP!");
                    choice = modes[option];
                    stage = 1;
                } else {
                    sendDialogue(Colors.RED+"You cannot be wearing armor when doing this!");
                    stage = 3;
                }
            }
            break;
        case 1:
            sendOptionsDialogue(Colors.WHITE+"Are you sure you want to reset your skills?",
                    Colors.GREEN+Colors.SHAD+"Yes", Colors.RED+Colors.SHAD+"No");
            stage = 2;
            break;
        case 2:
            if (componentId == OPTION_1)
                switchMode();
            end();
            break;
        case 3:
            end();
            break;
        }
    }

    @Override
    public void finish() {
        player.getInterfaceManager().closeChatBoxInterface();
    }
    
    public void switchMode() {
        Toolbelt toolbelt = new Toolbelt(player);
        QueryExecutor.submit(new ResetUserHiscores(player.getUsername(), player.getTable()));
        switch(choice) {
        case "Novice":
            player.setNovice(true);
            player.setExpert(false);
            player.setLegendary(false);
            player.setIntermediate(false);
            break;
        case "Intermediate":
            player.setNovice(false);
            player.setLegendary(false);
            player.setExpert(false);
            player.setIntermediate(true);
            break;
        case "Expert":
            player.setNovice(false);
            player.setExpert(true);
            player.setLegendary(false);
            player.setIntermediate(false);
            break;
        case "Legendary":
            player.setNovice(false);
            player.setIntermediate(false);
            player.setExpert(false);
            player.setLegendary(true);
            break;
        }
        player.setHCIronMan(false);
        player.setIronMan(false);
        player.setNoviceIronMan(false);
        player.setIntermediateIronMan(false);
        player.setExpertIronMan(false);
        player.endKingOfTheSkillGameMode();
        player.getSkills().setBonusPrismaticXp(0);
        player.getSkills().resetAllSkills();
        QueryExecutor.submit(new Hiscores(player, player.getTable()));
        SerializableFilesManager.savePlayer(player);
        player.sendMessage(Colors.GREEN+"You have switched your game mode to: "+
                (Colors.GREEN+Colors.SHAD+choice)+"</col></shad>!");
    }
}
