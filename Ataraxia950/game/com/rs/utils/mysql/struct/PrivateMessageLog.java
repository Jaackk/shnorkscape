package com.rs.utils.mysql.struct;

import com.rs.game.player.Player;
import lombok.Getter;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Noele
 * see https://noeles.life || noele@zenyte.com
 */
public class PrivateMessageLog {

    public static List<PrivateMessageLog> list = new ArrayList<>();

    @Getter
    private final Player player;

    @Getter private final String friend;

    @Getter private final String message;

    @Getter private final Timestamp date = new Timestamp(System.currentTimeMillis());

    public PrivateMessageLog(final Player player, final String friend, final String message) {
        this.player = player;
        this.friend = friend;
        this.message = message;
    }

}
