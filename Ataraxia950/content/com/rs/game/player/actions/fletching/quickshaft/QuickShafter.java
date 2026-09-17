package com.rs.game.player.actions.fletching.quickshaft;

import com.rs.game.player.Player;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class QuickShafter {
    public static final int ARROW_SHAFT = 52;
    public static final int ID = 36273;

    public static void execute(Player player) {
        player.getActionManager().setAction(new QuickShafterAction());
    }
}