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
public class GenericChatLog {

    public static List<GenericChatLog> list = new ArrayList<>();

    @Getter
    private transient final Player player;

    @Getter private final String message;

    @Getter private final Timestamp date = new Timestamp(System.currentTimeMillis());

    public GenericChatLog(final Player player, final String message) {
        this.player = player;
        this.message = message;
    }
}
