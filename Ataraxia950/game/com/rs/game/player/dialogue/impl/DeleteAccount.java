package com.rs.game.player.dialogue.impl;

import com.rs.Settings;
import com.rs.game.World;
import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.InputNameEvent;
import com.rs.utils.SerializableFilesManager;
import com.rs.utils.Utils;

import java.io.File;

public class DeleteAccount extends Dialogue {

    private String USERNAME = "";

    @Override
    public void start() {
        if (Settings.DEBUG || Settings.TEST_SERVER_MODE || player.isOwner()) {
            player.sendInputName("Enter the username of the account to delete:", new InputNameEvent() {
                @Override
                public void run(Player player) {
                    USERNAME = getString();
                    int interfaceId = 1183;
                    player.getInterfaceManager().sendChatBoxInterface(interfaceId);
                    player.getPackets().sendItemOnIComponent(interfaceId, 8, 4012, 1);
                    player.getPackets().sendIComponentText(interfaceId, 13, "Are you sure you want to do this?");
                    player.getPackets().sendIComponentText(interfaceId, 3, "Delete the account \"" + USERNAME + "\"?");
                    player.getPackets().sendIComponentText(interfaceId, 1, Colors.wrap(Colors.RED + Colors.SHAD, "WARNING: ") + "Deleting an account is irreversible and should only be done if it is nulled/unfixable.");
                }
            });
        }
    }

    @Override
    public void run(int interfaceId, int componentId) {
        if (componentId == 9) {
            deleteAccounts(USERNAME);
        }
        end();
    }

    @Override
    public void finish() {}
    
    private void deleteAccounts(String username) {
        String formattedUsername = Utils.formatPlayerNameForProtocol(username);
        String primaryDirectory = "data/playersaves/characters/", backupDirectory = "data/playersaves/charactersBackup/";
        boolean primaryExists = SerializableFilesManager.containsPlayer(formattedUsername), backupExists = SerializableFilesManager.containsPlayerBackup(formattedUsername);
        File primary = new File(primaryDirectory + formattedUsername + ".p"), backup = new File(backupDirectory + formattedUsername + ".p");
        if (player.getUsername().equals(formattedUsername)) {
            player.sendMessage(Colors.RED + "You cannot delete your account whilst logged in to it.");
            return;
        }
        if (!World.containsPlayer(formattedUsername)) {
            player.sendMessage(primary.delete() ? "Primary account deleted." : (primaryExists ? "There was a problem when attempting to delete the primary account." : "There was no primary account found by the name of \"" + username + "\"."));
            player.sendMessage(backup.delete() ? "Backup account deleted." : (backupExists ? "There was a problem when attempting to delete the backup account." : "There was no backup account found by the name of \"" + username + "\"."));
        } else {
            player.sendMessage(Colors.RED + "The account must be offline when attempting to delete it.");
        }
    }
}
