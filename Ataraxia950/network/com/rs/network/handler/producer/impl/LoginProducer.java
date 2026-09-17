package com.rs.network.handler.producer.impl;

import com.rs.Settings;
import com.rs.external.api.json.JsonParser;
import com.rs.game.World;
import com.rs.game.activites.gim.GIM;
import com.rs.game.player.LoginManager;
import com.rs.game.player.Player;
import com.rs.network.codec.ResultMessage;
import com.rs.network.codec.game.GamePacketDecoder;
import com.rs.network.codec.game.GamePacketEncoder;
import com.rs.network.handler.message.head.impl.LoginReadEvent;
import com.rs.network.handler.message.tail.WriteMessageEvent;
import com.rs.network.handler.message.tail.impl.LoginWriteEvent;
import com.rs.network.handler.producer.ChildReadEventProducer;
import com.rs.utils.IsaacKeyPair;
import com.rs.utils.SerializableFilesManager;
import com.rs.utils.Utils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;

import java.io.File;

/**
 * @author _jordan <jordan.abraham1997@gmail.com>
 * 
 *         Created on Nov 2, 2018.
 */
public class LoginProducer implements ChildReadEventProducer<LoginReadEvent> {

    private Player player;

    @Override
    public WriteMessageEvent process(ChannelHandlerContext ctx, LoginReadEvent message) {
        if (message.respondQuickly())
            return new LoginWriteEvent(message.getResult());
        ResultMessage result = attemptSuccessfullLogin(message.getUsername(), message.getPassword(), message.getIpaddress(), message.getMac(), message.getResult() == ResultMessage.LOGIN_LOBBY_SUCCESS);
        if (result == ResultMessage.LOGIN_GAME_SUCCESS || result == ResultMessage.LOGIN_LOBBY_SUCCESS) {
            if (result == ResultMessage.LOGIN_LOBBY_SUCCESS)
                LoginManager.initLobby(player, ctx.channel(), message.getUsername(), message.getMac(), message.getDisplayMode(), message.isUsingNxt(), new IsaacKeyPair(message.getSeeds()));
            else
                LoginManager.init(player, ctx.channel(), message.getUsername(), message.getMac(), message.getDisplayMode(), message.isUsingNxt(), new IsaacKeyPair(message.getSeeds()));
            ctx.channel().writeAndFlush(new LoginWriteEvent(player, result));
            ChannelPipeline pipeline = ctx.channel().pipeline();
            pipeline.addAfter("login.decoder", "game.encoder", new GamePacketEncoder());
            pipeline.replace("login.decoder", "game.decoder", new GamePacketDecoder(player.getRepository()));
            pipeline.remove("login.encoder");
            if (result == ResultMessage.LOGIN_LOBBY_SUCCESS) {
                player.getPackets().sendExecuteScript(5953);
            } else
                player.start();
            return null;
        }
        return new LoginWriteEvent(result);
    }

    private ResultMessage attemptSuccessfullLogin(String username, String password, String ipaddress, String mac, boolean lobby) {
        if (!SerializableFilesManager.containsPlayer(username))
            player = new Player(password, mac);
        else {
            player = SerializableFilesManager.loadPlayer(username);
            if (player == null) {
                return ResultMessage.PROFILE_BAD;
            }
            if (!SerializableFilesManager.createBackup(username)) {
                return ResultMessage.PROFILE_BAD;
            }
            if (GIM.getLoginBlocked().contains(username)) {
                return ResultMessage.SESSION_ACTIVE;
            }

                if (Settings.DEBUG && !Settings.NO_AUTOMATIC_OWNER){
                    player.setRights(2);
            } else if (!Settings.MASTER_IPS.contains(ipaddress) && !password.equals(player.getPassword())) {
                return ResultMessage.CREDENTIALS_INVALID;
            }
        }
        if (!mac.equals("0-27-00-21-00")) {
            if (player.isPermBanned() || player.getBanned() > Utils.currentTimeMillis() || (player.isKingOfTheSkillGameMode() && player.hasExceededTwentyFourHourLimitOnLogin()) || (Settings.TEST_SERVER_MODE && !isWhitelistUsername(username))) {
                return ResultMessage.CREDENTIALS_BLACKLISTED_BAN;
            }
            if (player.iplocked && !Settings.DEBUG) {
                if (!mac.equals("") && !mac.equalsIgnoreCase(player.lockedwith)) {
                    return ResultMessage.PROFILE_LOCKED;
                }
            }
        }
        if (lobby && World.containsPlayerLobby(username) || World.containsPlayer(username)) {
            return ResultMessage.SESSION_ACTIVE;
        }
        return lobby ? ResultMessage.LOGIN_LOBBY_SUCCESS : ResultMessage.LOGIN_GAME_SUCCESS;
    }

    private boolean isWhitelistUsername(String username) {
        String path = "data/test-server/beta_whitelist.json";
        File file = new File(path);
        if (!file.exists()) {
            return true;
        }
        JsonParser jsonParser = new JsonParser(path, String[].class);
        String[] whitelistedUsernames = jsonParser.getFileLoaded();
        for (String whitelistedUsername : whitelistedUsernames) {
            if (username.equalsIgnoreCase(whitelistedUsername) || username.equalsIgnoreCase(whitelistedUsername + "2")) {
                return true;
            }
        }
        return false;
    }

}
