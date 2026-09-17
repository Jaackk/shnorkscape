package com.rs.game.player.content.bank_highscores;

import com.rs.Settings;
import com.rs.cores.CoresManager;
import com.rs.game.item.Item;
import com.rs.game.player.Bank;
import com.rs.game.player.Player;
import com.rs.game.player.content.grandExchange.GrandExchange;
import com.rs.utils.Logger;
import com.rs.utils.mysql.DatabaseCredential;
import com.rs.utils.mysql.Pool;
import com.rs.utils.mysql.SQLRunnable;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@RequiredArgsConstructor
public final class SaveBankSql extends SQLRunnable {

    public static void send(Player player) {
        if(Settings.TEST_SERVER_MODE)
            return;
        if (player.lastBankSave == null ||
                LocalDateTime.now().isAfter(player.lastBankSave.plusMinutes(10))) {
            int quantity = 0;
            long value = 0;
            for (Bank b : player.getBanks()) {
                for (Item item : b.getContainerCopy()) {
                    if (item == null) {
                        continue;
                    }
                    quantity++;
                    int itemValue = GrandExchange.getPrice(item);
                    if (itemValue > 0) {
                        value += itemValue * item.getAmount();
                    }
                }
            }
            String table;
            if (player.isGroupIronman() && !player.isUnregisteredGIM()) {
                table = "hs_banks_gim";
            } else if (player.isHCIronMan()) {
                table = "hs_banks_hciron";
            } else if (player.isIronMan()) {
                table = "hs_banks_iron";
            } else {
                table = "hs_banks";
            }
            CoresManager.getServiceProvider().executeNow(new SaveBankSql(table, player.getUsername(), value, quantity));
            player.lastBankSave = LocalDateTime.now();
        }
    }

    private final String table;
    private final String username;
    private final long value;
    private final int quantity;
    private final LocalDateTime timestamp = LocalDateTime.now();

    @Override
    public void execute(DatabaseCredential auth) {
        try (Connection c = Pool.getConnection(auth, "ataraxia");
             PreparedStatement pst = c.prepareStatement("INSERT INTO "+table+"(username, total_value, quantity, last_updated) "+
                             "VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE total_value=VALUES(total_value), quantity=VALUES(quantity), last_updated=VALUES(last_updated)")) {
            pst.setString(1, username);
            pst.setLong(2, value);
            pst.setInt(3,quantity);
            pst.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            pst.executeUpdate();
        } catch (Exception e) {
            Logger.getGlobal().catching(e);
        }
    }
}
