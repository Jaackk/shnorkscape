package com.opennxt.net.login

import com.opennxt.OpenNXT
import com.opennxt.model.Build
import com.opennxt.net.GenericResponse
import com.opennxt.net.RSChannelAttributes
import com.opennxt.resources.FilesystemResources
import com.opennxt.resources.config.enums.EnumDefinition
import com.opennxt.resources.config.structs.StructDefinition
import com.opennxt.resources.defaults.wearpos.WearposDefaults
import com.rs.game.player.client.Native950World
import com.rs.game.player.client.Native950SaveStore
import com.rs.game.player.client.Native950Save
import com.rs.game.player.client.Native950Appearance
import com.rs.cache.Cache
import com.rs.network.protocol.modern950.Native950Packets
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.util.AttributeKey
import io.netty.util.ReferenceCountUtil
import mu.KotlinLogging
import java.util.ArrayDeque
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import java.util.function.IntSupplier

/** Keeps OpenNXT's lobby/asset service while giving game traffic one Ataraxia world owner. */
internal object Ataraxia950Handoff {
    private val logger = KotlinLogging.logger { }
    private val authenticated = AttributeKey.valueOf<String>("ataraxia950-authenticated-game")
    private const val GATE_NAME = "ataraxia950-handoff-gate"
    private val saveStore: Native950SaveStore by lazy {
        Native950SaveStore(playerSavePath(System.getenv("OPENNXT_PLAYER_SAVE_PATH")))
    }

    val enabled: Boolean
        get() = System.getenv("OPENNXT_GAME_BACKEND")?.equals("ataraxia950", ignoreCase = true) == true

    /** True only after this channel has passed the native 950 admission gate. */
    internal fun ownsAuthenticatedNativeSession(channel: Channel): Boolean =
        channel.attr(authenticated).get() != null

    /** Called only after the configured login processor accepts the game request. */
    fun authorize(channel: Channel, build: Build, username: String): GenericResponse? {
        if (!supportsClientBuild(build) || OpenNXT.config.build != 950) {
            logger.warn {
                "Rejected Ataraxia game login wire build ${build.major}.${build.minor}; " +
                    "expected 950.1 (native executable version 950-1), configured major ${OpenNXT.config.build}"
            }
            return GenericResponse.OUT_OF_DATE
        }
        try {
            initializeWorldCache()
            // A configured store is required before accepting a persistent local character.
            // The store creates and validates its directory; legacy player saves are never opened.
            saveStore
        } catch (failure: Exception) {
            logger.error(failure) { "Cannot initialize Ataraxia's 950 world cache or modern player store" }
            return GenericResponse.SERVICE_UNAVAILABLE
        }
        // One account holds exactly one slot. Two sessions for one username would load the same
        // profile and checkpoint it independently, so two whole-profile writers would interleave
        // over one save file. The world refuses the reservation as well; this branch only makes
        // the client see the right answer instead of "world full".
        if (Native950World.getInstance().isOnline(username)) return GenericResponse.LOGGED_IN
        if (!Native950World.getInstance().canReserve()) return GenericResponse.WORLD_FULL
        if (!channel.attr(authenticated).compareAndSet(null, username)) return GenericResponse.BAD_SESSION
        return null
    }

    // Captured from the native 947-3 executable's login header. Its wire minor
    // is 1; the executable's patch version is a different version field.
    internal fun supportsClientBuild(build: Build): Boolean = build.major == 950 && build.minor == 1

    internal fun playerSavePath(configured: String?): Path {
        require(!configured.isNullOrBlank()) {
            "OPENNXT_PLAYER_SAVE_PATH must identify the dedicated modern950/players directory"
        }
        val path = Paths.get(configured)
        require(path.isAbsolute) { "OPENNXT_PLAYER_SAVE_PATH must be an absolute directory path" }
        val normalized = path.normalize()
        require(normalized.fileName?.toString()?.equals("players", ignoreCase = true) == true &&
            normalized.parent?.fileName?.toString()?.equals("modern950", ignoreCase = true) == true) {
            "OPENNXT_PLAYER_SAVE_PATH must end in modern950/players; legacy saves cannot be used"
        }
        return normalized
    }

    @Synchronized
    private fun initializeWorldCache() {
        if (Cache.isFlatReadOnly()) return
        check(Cache.STORE == null) { "The native world cannot replace an active legacy cache" }
        val path = requireNotNull(System.getenv("OPENNXT_CACHE_PATH")) {
            "OPENNXT_CACHE_PATH must identify the selected flat 950 cache"
        }
        Cache.initFlatReadOnly(Paths.get(path))
    }

    /** Runs on the channel event loop after the final server-permanent-variable acknowledgement. */
    fun complete(channel: Channel) {
        check(channel.eventLoop().inEventLoop()) { "950 handoff must run on the channel event loop" }
        val username = channel.attr(authenticated).getAndSet(null)
        if (username == null || channel.attr(RSChannelAttributes.LOGIN_TYPE).get() !in
            setOf(LoginType.GAME, LoginType.GAME_ALT)
        ) {
            logger.warn { "Rejected unauthenticated or duplicate Ataraxia game continuation" }
            channel.close()
            return
        }

        // P6: GameLoginResponse carries the player index, and it is written long before the
        // world thread can create the Player, so the slot is reserved here first. Every
        // branch that does not end in an attached session releases it again - a leaked
        // reservation is permanent capacity loss, not a transient error.
        val world = Native950World.getInstance()
        val playerIndex = world.reserve(username)
        if (playerIndex == 0) {
            logger.warn {
                "Ataraxia 950 world refused a slot for $username (world full, or the account is " +
                    "already in the world); refusing the game continuation"
            }
            channel.close()
            return
        }
        // A channel that dies before the world claims the slot still reaches a release:
        // Netty always completes the GameLoginResponse write promise (with a failure, or
        // with !isActive), and the attach future always completes, so each of the four
        // branches below runs exactly one release. Nothing releases by channel-close
        // listener, which could otherwise return a slot a later login already reserved.
        // Every release names the username, so a branch that fires after the world already
        // freed the slot cannot take a concurrent login's fresh reservation with it.
        try {
            val incoming = requireNotNull(channel.attr(RSChannelAttributes.INCOMING_ISAAC).get())
            val outgoing = requireNotNull(channel.attr(RSChannelAttributes.OUTGOING_ISAAC).get())
            val appearance = appearance(username)
            val interfaces = interfaceBootstrap()
            val content = Native950CacheContent.content()
            val config = OpenNXT.config.lobbyBootstrap
            val scene = Native950World.SceneConfig(
                3222, 3222, 0, playerIndex, 7,
                config.rebuildNormalAreaType, config.rebuildNormalHash1, config.rebuildNormalHash2
            )
            val pipeline = channel.pipeline()
            val gate = PendingGameInput()

            // Replacing the handler first ensures a removed decoder's unread cumulation is retained.
            // The gate also covers bytes arriving while the world thread creates the player.
            pipeline.replace("login-handler", GATE_NAME, gate)
            pipeline.remove("login-decoder")
            channel.write(Unpooled.buffer(1).writeByte(GenericResponse.SUCCESSFUL.id))
            channel.writeAndFlush(
                LoginPacket.GameLoginResponse(
                    byte0 = 0, rights = 0, byte2 = 0, byte3 = 0, byte4 = 0, byte5 = 0,
                    byte6 = 0, playerIndex = playerIndex, byte8 = 1, medium9 = 0, isMember = 1,
                    username = username, short12 = 0, int13 = 0
                )
            ).addListener { response ->
                if (!response.isSuccess || !channel.isActive) {
                    logger.warn(response.cause()) { "Failed to complete Ataraxia game login response" }
                    world.release(playerIndex, username)
                    channel.close()
                } else {
                    try {
                        pipeline.remove("login-encoder")
                        // These are the SAME live cipher objects used during login. Re-seeding here
                        // would lose any words already consumed by an earlier login stage.
                        world.attach(
                            channel, username, IntSupplier { incoming.nextValue }, IntSupplier { outgoing.nextValue },
                            appearance, scene, interfaces, content, saveStore
                        ).whenComplete { _, failure ->
                            channel.eventLoop().execute {
                                if (failure != null || !channel.isActive) {
                                    logger.error(failure) { "Ataraxia 950 world attachment failed for $username" }
                                    world.release(playerIndex, username)
                                    channel.close()
                                } else {
                                    gate.forwardWhenRemoved = true
                                    pipeline.remove(gate)
                                    logger.info {
                                        "Ataraxia 950 now owns game session for $username at player index " +
                                            "$playerIndex; OpenNXT world entry skipped"
                                    }
                                }
                            }
                        }
                    } catch (failure: Exception) {
                        logger.error(failure) { "Ataraxia 950 transport installation failed for $username" }
                        world.release(playerIndex, username)
                        channel.close()
                    }
                }
            }
        } catch (failure: Exception) {
            logger.error(failure) { "Ataraxia 950 handoff failed for $username" }
            world.release(playerIndex, username)
            channel.close()
        }
    }

    /** The world replaces this initial body with saved equipment before sending it. */
    private fun appearance(username: String): ByteArray {
        val wearPositions = FilesystemResources.instance.defaults.get<WearposDefaults>().slots
        return Native950Appearance(wearPositions).encode(
            Native950Save.canonicalUsername(username), IntArray(Native950Appearance.SLOT_COUNT) { -1 }
        )
    }

    /** Resolve native UI slots against the actual selected cache, as the working 947 frontend did. */
    private fun interfaceBootstrap(): List<Native950Packets.Packet> {
        Native950CacheContent.verifyInterfaceBindings()
        Native950RunOrb.verify()
        com.rs.game.player.client.Native950Dialogues.verify()
        com.rs.game.player.client.Native950QuantityInput.verify()
        if (System.getProperty("ataraxia950.worldMap", "false").toBoolean()) {
            com.rs.game.player.client.Native950WorldMap.verify()
        }
        verifyChatInitialization()
        val inventory = requireNotNull(OpenNXT.resources.get<EnumDefinition>(7716))
        val slots = inventory.values.mapValues { (key, value) ->
            val structId = value as? Int ?: error("Missing native slot struct $key")
            val struct = requireNotNull(OpenNXT.resources.get<StructDefinition>(structId))
            Native950InterfaceBootstrap.Slot(
                struct.getInt(3505, default = -1), struct.getInt(3503, default = -1)
            )
        }
        val ribbon = Native950Ribbon.enabled()
        if (ribbon) Native950Ribbon.verify()
        return Native950InterfaceBootstrap.packets(slots, ribbonEnabled = ribbon) +
            if (ribbon) Native950Ribbon.initialization() else emptyList()
    }

    private fun verifyChatInitialization() {
        check(Cache.isFlatReadOnly()) { "Modern chat requires the selected read-only cache" }
        val expected = mapOf(
            1362 to "8c1dd80480365074a78744de4ca397bb7d809936b0b87811dab26b9398705ac1",
            1558 to "bb571b57ba93fd37e27f20b418d93279457682472ca71b73458c211e96aaf74c",
            8491 to "eeee0e42b2c151112acdcfd2ef3cbbfc20b556a2de1a0dd0a31a2bdd1d7892ac",
            8492 to "8da41c6be77653e91faa8dc250e6a4d600d6e88a2086fa4da442ca030ac58277",
            84 to "09e5d656bf988df1e9895ac513c7816c162f9c03491d58531f18b2363130fa31",
            8546 to "b8c65bb68f50cec1533e6b21d161301addae175295ea60a9da976618dea2ec07",
            655 to "fb91b21750e20467e8b6c94314813cd8d078847da3b196cbd9a2d77e7d5996e1",
            8508 to "db41244ffb47e865b03534c2fae07e00fd197bdc915ac1c63905069064bad145"
        )
        for ((script, hash) in expected) {
            val data = requireNotNull(Cache.STORE.indexes[12].getFile(script, 0)) {
                "Missing modern chat script $script"
            }
            val actual = MessageDigest.getInstance("SHA-256").digest(data)
                .joinToString("") { "%02x".format(it.toInt() and 255) }
            Native950CacheContent.requirePin("Modern chat script $script", hash, actual)
        }
    }

    /** Bounds memory and lifetime while preserving TCP bytes across asynchronous ownership transfer. */
    internal class PendingGameInput : ChannelInboundHandlerAdapter() {
        private val pending = ArrayDeque<ByteBuf>()
        private var bytes = 0
        private var timeout: io.netty.util.concurrent.ScheduledFuture<*>? = null
        var forwardWhenRemoved = false

        override fun handlerAdded(ctx: ChannelHandlerContext) {
            timeout = ctx.executor().schedule(Runnable {
                logger.warn { "Ataraxia 950 game attachment exceeded 15 seconds; closing pending connection" }
                ctx.close()
            }, 15, TimeUnit.SECONDS)
        }

        override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
            if (msg !is ByteBuf || bytes + msg.readableBytes() > 65536) {
                ReferenceCountUtil.release(msg)
                ctx.close()
                return
            }
            bytes += msg.readableBytes()
            pending.addLast(msg)
        }

        override fun channelInactive(ctx: ChannelHandlerContext) {
            timeout?.cancel(false)
            releasePending()
            ctx.fireChannelInactive()
        }

        override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
            logger.error(cause) { "Ataraxia 950 handoff pipeline failed" }
            ctx.close()
        }

        override fun handlerRemoved(ctx: ChannelHandlerContext) {
            timeout?.cancel(false)
            if (forwardWhenRemoved && ctx.channel().isActive) {
                while (pending.isNotEmpty()) ctx.fireChannelRead(pending.removeFirst())
                ctx.fireChannelReadComplete()
                bytes = 0
            } else releasePending()
        }

        private fun releasePending() {
            while (pending.isNotEmpty()) pending.removeFirst().release()
            bytes = 0
        }
    }
}
