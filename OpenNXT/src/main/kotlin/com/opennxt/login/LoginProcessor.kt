package com.opennxt.login

import com.opennxt.Constants
import com.opennxt.model.proxy.PacketDumper
import com.opennxt.net.RSChannelAttributes
import com.opennxt.net.login.LoginPacket
import com.opennxt.net.proxy.ConnectedProxyClient
import com.opennxt.net.proxy.ProxyChannelAttributes
import com.opennxt.net.proxy.ProxyConnectionFactory
import com.opennxt.net.proxy.ProxyConnectionHandler
import com.opennxt.net.proxy.ProxyPlayer
import io.netty.channel.Channel
import mu.KotlinLogging
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun interface LoginProcessor {
    fun process(context: LoginContext)
}

object AuthoritativeLoginProcessor : LoginProcessor {
    private val logger = KotlinLogging.logger {}
    override fun process(context: LoginContext) {
        val local = com.opennxt.security.NativeLanAccess.loopback(context.channel.remoteAddress())
        val reason = if (context.build.major != 950 && !local) "unsupported-build"
            else com.opennxt.security.NativeLanAccess.authenticateReason(context.channel.remoteAddress(), context.username, context.password)
        context.result = if (reason == "accepted")
            LoginResult.SUCCESS else LoginResult.INVALID_USERNAME_PASS
        if (!local) logger.info { "LAN admission from ${context.channel.remoteAddress()}: build=${context.build.major}.${context.build.minor} result=$reason" }
        context.callback(context)
    }
}

class ProxyLoginProcessor(
    private val usernames: Set<String>,
    private val connectionFactory: ProxyConnectionFactory,
    private val connectionHandler: ProxyConnectionHandler
) : LoginProcessor {
    private val logger = KotlinLogging.logger { }

    override fun process(context: LoginContext) {
        if (usernames.isNotEmpty() && context.username.lowercase() !in usernames) {
            logger.warn { "Rejecting proxy login for ${context.username}: username is not allow-listed" }
            context.result = LoginResult.INVALID_USERNAME_PASS
            context.callback(context)
            return
        }

        connectionFactory.createLogin(context.packet) { channel, result ->
            if (channel != null) {
                bindProxyPair(context, channel)
            }

            context.result = result
            context.callback(context)
        }
    }

    private fun bindProxyPair(context: LoginContext, upstreamChannel: Channel) {
        val now = DateTimeFormatter.ISO_LOCAL_DATE_TIME
            .withLocale(Locale.getDefault())
            .format(LocalDateTime.now())
            .replace(':', '-')
        val type = if (context.packet is LoginPacket.LobbyLoginRequest) "lobby" else "game"
        val dumpBase = Constants.PROXY_DUMP_PATH.resolve("$now-$type-${context.username}")

        val clientSide = ConnectedProxyClient(
            context.channel.attr(RSChannelAttributes.CONNECTED_CLIENT).get(),
            PacketDumper(dumpBase.resolve("clientprot.bin"))
        )
        val serverSide = ConnectedProxyClient(
            upstreamChannel.attr(RSChannelAttributes.CONNECTED_CLIENT).get(),
            PacketDumper(dumpBase.resolve("serverprot.bin"))
        )

        val player = ProxyPlayer(clientSide)

        context.channel.attr(ProxyChannelAttributes.PROXY_PLAYER).set(player)
        upstreamChannel.attr(ProxyChannelAttributes.PROXY_PLAYER).set(player)

        clientSide.connection.processUnidentifiedPackets = true
        serverSide.connection.processUnidentifiedPackets = true

        clientSide.other = serverSide
        serverSide.other = clientSide

        context.channel.attr(ProxyChannelAttributes.PROXY_CLIENT).set(clientSide)
        upstreamChannel.attr(ProxyChannelAttributes.PROXY_CLIENT).set(serverSide)

        context.channel.attr(RSChannelAttributes.PASSTHROUGH_CHANNEL).set(upstreamChannel)
        upstreamChannel.attr(RSChannelAttributes.PASSTHROUGH_CHANNEL).set(context.channel)

        connectionHandler.registerProxyConnection(clientSide, serverSide)
    }
}
