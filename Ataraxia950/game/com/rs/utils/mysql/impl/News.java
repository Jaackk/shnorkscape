package com.rs.utils.mysql.impl;
import com.rs.game.player.Player;
import com.rs.utils.Utils;
import com.rs.utils.mysql.DatabaseCredential;
import com.rs.utils.mysql.SQLRunnable;

import java.awt.Color;

public class News extends SQLRunnable {

    private final String query = "INSERT INTO message (username, news) VALUES (?, ?)";

    private final Player player;
    private final String message;

    public News(final Player player, final String message) {
        this.player = player;
        this.message = message;
    }

    @Override
    public void execute(final DatabaseCredential auth) {
        if(player == null || player.isDeveloper() && !player.getIP().equals(System.getProperty("ataraxia.legacy.newsIp", "")))
            return;

        final String name = Utils.formatPlayerNameForDisplay(player.getUsername());
        final String rawMessage = message.substring(message.indexOf(player.getDisplayName()));
        final float hue = Utils.RANDOM.nextFloat();
        final float saturation = (Utils.RANDOM.nextInt(2000) + 1000) / 10000f;
        final float luminance = 0.9f;


//        try(final Connection con = Pool.getConnection(auth, "admin_news");
//            final PreparedStatement pst = con.prepareStatement(query)) {
//
//            pst.setString(1, name);
//            pst.setString(2, message);
//            pst.execute();
//
//        } catch(final Exception ex) {
//            Logger.getGlobal().info("Could not submit news query; player: "+name+" message: "+message);
//            Logger.getGlobal().catching(ex);
//        }
    }

}
