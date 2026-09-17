package com.rs.network;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Multiset;
import com.rs.Settings;
import com.rs.network.codec.ResultMessage;
import com.rs.utils.IPBanL;
import com.rs.utils.Logger;
import io.netty.channel.Channel;

/**
 * Filters {@link Channel}s by the amount of active connections they already have. A threshold is put on the amount of
 * successful connections allowed to be made in order to provide security from socket flooder attacks.
 *
 * Also handles IP bans.
 *
 * @author lare96 <http://github.org/lare96>
 */
public final class NetworkChannelFilter {

    /**
     * Limit connections.
     */
    private static final int CONNECTION_LIMIT = 3;

    /**
     * A concurrent multiset containing active connection counts.
     */
    private static final Multiset<String> connections = ConcurrentHashMultiset.create();

    public static ResultMessage accept(Channel channel, String address) throws Exception {
        if (Settings.TEST_SERVER_MODE && connections.count(address) >= CONNECTION_LIMIT) {

            // Reject if more than CONNECTION_LIMIT active connections.
            Logger.getGlobal().info("Rejected connection from {}, too many active sessions.", address);
            return ResultMessage.SESSION_LIMIT;
        }
        if(IPBanL.isBanned(address)) {
            return ResultMessage.CREDENTIALS_BLACKLISTED_BAN;
        }
        // Increment connection count by 1, and remove address once disconnected.
        connections.add(address);
        channel.closeFuture().addListener(it -> connections.remove(address));
        return ResultMessage.LOGIN_GAME_SUCCESS;
    }
}
