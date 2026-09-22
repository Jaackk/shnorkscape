package com.opennxt.model.entity

import com.opennxt.api.stat.StatContainer
import com.opennxt.model.commands.CommandSender
import com.opennxt.model.entity.player.InterfaceManager
import com.opennxt.model.messages.Message
import com.opennxt.model.tick.Tickable
import com.opennxt.net.ConnectedClient
import com.opennxt.net.game.GamePacket
import mu.KotlinLogging

abstract class BasePlayer(var client: ConnectedClient, val name: String): CommandSender, Tickable {
    abstract val interfaces: InterfaceManager
    abstract val stats: StatContainer

    var noTimeouts = 0
    private val logger = KotlinLogging.logger { }

    override fun message(message: Message) {
        client.write(message.createPacket())
    }

    override fun message(message: String) {
        client.write(Message.ConsoleMessage(message).createPacket())
    }

    override fun console(message: String) {
        client.write(Message.ConsoleMessage(message).createPacket())
    }

    override fun error(message: String) {
        client.write(Message.ConsoleError(message).createPacket())
    }

    override fun hasPermissions(node: String): Boolean {
        if (com.opennxt.security.NativeLanAccess.loopback(client.channel.remoteAddress())) return true
        val authenticated = client.channel.attr(io.netty.util.AttributeKey.valueOf<String>("opennxt.authenticated-lan-account")).get()
        return authenticated != null && authenticated.equals(name, true) &&
            com.opennxt.security.NativeLanAccess.allowedPeer(client.channel.remoteAddress()) &&
            System.getProperty("ataraxia950.lanDevAccounts", "").split(',').any { it.trim().equals(authenticated, true) }
    }

    override fun tick() {

    }

    fun write(message: GamePacket) {
        client.write(message)
    }
}
