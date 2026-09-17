package com.rs.game.player.commands.impl.regularplayercommands;

import com.rs.game.player.Player;
import com.rs.game.player.commands.Command;
import com.rs.game.player.commands.CommandHandler;
import com.rs.game.player.commands.CommandInfo;
import com.rs.game.player.commands.CommandRights;
import com.rs.utils.Colors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ataraxia-server
 * paolo 08/09/2019
 * #Shnek6969
 */
@CommandInfo(
        rank = CommandRights.NORMAL,
        possibleCommands = {"commands"},
        description = "Displays all the possible commands"
        )
public class DisplayCommands extends Command {



    @Override
    public void executeCommand(Player player, boolean isClientCommand, String command, String... args) {
        List<String> dupelicate = new ArrayList<String>();
       player.getInterfaceManager().sendInterface(275);
       player.getPackets().sendText(275,1 ,"Ataraxias commands list");
       int index = 10;
        for (Map.Entry<Object, Class<? extends Command>> entry : CommandHandler.possibleCommands.entrySet()) {
            CommandInfo metaData =  entry.getValue().getAnnotation(CommandInfo.class);
            if(metaData.rank() != CommandRights.NORMAL || dupelicate.contains(metaData.description()))
                continue;
            dupelicate.add(metaData.description());
            player.getPackets().sendText(275, index,getFormatedCommands(metaData.possibleCommands()));
            player.getPackets().sendText(275, index + 1, metaData.description());
            index+= 2;
      }
    }

    private String getFormatedCommands(String[] input){
        StringBuilder newString = new StringBuilder();
        newString.append("[ <u>"+ Colors.RED);
        for (String s : input){
            newString.append(s+",");
        }
        newString.deleteCharAt(newString.lastIndexOf(","));
        newString.append("</u> </col>]");
        return  newString.toString();
    }
}
