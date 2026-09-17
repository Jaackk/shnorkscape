package com.rs.game.player.content.jujupotions.jadinkos;

import com.rs.game.WorldTile;
import com.rs.game.player.controllers.Controller;

/**
 * @author lare96 <http://github.com/lare96>
 */
public class JadinkoInstanceController extends Controller {

    private final JadinkoInstance instance;

    public JadinkoInstanceController(JadinkoInstance jadinkoInstance) {
        instance = jadinkoInstance;
    }

    @Override
    public boolean processMagicTeleport(WorldTile toTile) {
        player.getControlerManager().forceStop();
        return true;
    }

    @Override
    public void forceClose() {
        instance.getPlayers().remove(player);
    }

    @Override
    public boolean login() {
        instance.kick(player);
        return true;
    }

    @Override
    public boolean logout() {
        try {
            instance.kick(player, true);
        } catch (Exception e ) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void start() {
        instance.getPlayers().add(player);
    }
}
