package com.opennxt.net.login

import com.opennxt.model.Build
import com.opennxt.net.RSChannelAttributes
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.handler.codec.ByteToMessageDecoder
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class Ataraxia947HandoffTest {
    @Test
    fun `game continuation cannot create a world session before authentication succeeds`() {
        val channel = EmbeddedChannel()
        try {
            channel.attr(RSChannelAttributes.LOGIN_TYPE).set(LoginType.GAME)
            Ataraxia947Handoff.complete(channel)
            assertFalse(channel.isActive)
            assertNull(channel.readOutbound<Any>())
        } finally {
            channel.finishAndReleaseAll()
        }
    }

    @Test
    fun `unpinned client revision is rejected before creating the world`() {
        assertFalse(Ataraxia947Handoff.supportsClientBuild(Build(947, 2)))
        assertFalse(Ataraxia947Handoff.supportsClientBuild(Build(947, 3)))
        assertFalse(Ataraxia947Handoff.supportsClientBuild(Build(910, 1)))
    }

    @Test
    fun `native executable 947-3 uses login wire build 947 dot 1`() {
        assertTrue(Ataraxia947Handoff.supportsClientBuild(Build(947, 1)))
    }

    @Test
    fun `persistent login requires an explicit absolute modern save directory`() {
        for (invalid in listOf(null, "", " ", "modern947/players")) {
            assertFailsWith<IllegalArgumentException> { Ataraxia947Handoff.playerSavePath(invalid) }
        }
        val workspace = Paths.get(".").toAbsolutePath().normalize()
        for (invalid in listOf(workspace, workspace.resolve("data/playersaves"), workspace.resolve("data/modern947"))) {
            assertFailsWith<IllegalArgumentException> { Ataraxia947Handoff.playerSavePath(invalid.toString()) }
        }
    }

    @Test
    fun `modern save path is normalized before store construction`() {
        val workspace = Paths.get(".").toAbsolutePath().normalize()
        assertEquals(workspace.resolve("data/modern947/players"), Ataraxia947Handoff.playerSavePath(
            workspace.resolve("data/unused/../modern947/players").toString()
        ))
    }

    @Test
    fun `continuation sharing a TCP read with game bytes survives decoder removal`() {
        val gate = Ataraxia947Handoff.PendingGameInput()
        val channel = EmbeddedChannel()
        channel.pipeline().addLast("login-decoder", object : ByteToMessageDecoder() {
            override fun decode(ctx: ChannelHandlerContext, input: ByteBuf, out: MutableList<Any>) {
                assertEquals(LoginType.GAMELOGIN_CONTINUE.id, input.readUnsignedByte().toInt())
                ctx.pipeline().replace("login-handler", "pending-game", gate)
                ctx.pipeline().remove(this)
            }
        })
        channel.pipeline().addLast("login-handler", ChannelInboundHandlerAdapter())
        try {
            assertFalse(channel.writeInbound(Unpooled.wrappedBuffer(byteArrayOf(26, 0x22, 0x33))))
            assertFalse(channel.writeInbound(Unpooled.wrappedBuffer(byteArrayOf(0x44, 0x55))))
            assertNull(channel.readInbound<Any>(), "Game data cannot reach a decoder until attachment succeeds")
            channel.pipeline().addLast("game-transport", ChannelInboundHandlerAdapter())
            gate.forwardWhenRemoved = true
            channel.pipeline().remove(gate)

            assertContentEquals(byteArrayOf(0x22, 0x33), readBytesAndRelease(channel))
            assertContentEquals(byteArrayOf(0x44, 0x55), readBytesAndRelease(channel))
            assertNull(channel.readInbound<Any>())
            assertTrue(channel.isActive)
        } finally {
            channel.finishAndReleaseAll()
        }
    }

    @Test
    fun `closing during attachment releases every buffered message`() {
        val gate = Ataraxia947Handoff.PendingGameInput()
        val channel = EmbeddedChannel(gate)
        val first = Unpooled.wrappedBuffer(byteArrayOf(1, 2))
        val second = Unpooled.wrappedBuffer(byteArrayOf(3, 4))
        try {
            channel.writeInbound(first, second)
            assertEquals(1, first.refCnt())
            channel.close()
            assertEquals(0, first.refCnt())
            assertEquals(0, second.refCnt())
            assertNull(channel.readInbound<Any>())
        } finally {
            channel.finishAndReleaseAll()
        }
    }

    @Test
    fun `failed attachment removal discards buffered bytes`() {
        val gate = Ataraxia947Handoff.PendingGameInput()
        val channel = EmbeddedChannel(gate)
        val bytes = Unpooled.wrappedBuffer(byteArrayOf(1, 2, 3))
        try {
            channel.writeInbound(bytes)
            channel.pipeline().remove(gate)
            assertEquals(0, bytes.refCnt())
            assertNull(channel.readInbound<Any>())
        } finally {
            channel.finishAndReleaseAll()
        }
    }

    @Test
    fun `pending input limit closes connection and releases retained and incoming data`() {
        val gate = Ataraxia947Handoff.PendingGameInput()
        val channel = EmbeddedChannel(gate)
        val retained = Unpooled.buffer(65536).writeZero(65536)
        val overflow = Unpooled.wrappedBuffer(byteArrayOf(1))
        try {
            channel.writeInbound(retained)
            assertTrue(channel.isActive)
            channel.writeInbound(overflow)
            assertFalse(channel.isActive)
            assertEquals(0, retained.refCnt())
            assertEquals(0, overflow.refCnt())
        } finally {
            channel.finishAndReleaseAll()
        }
    }

    private fun readBytesAndRelease(channel: EmbeddedChannel): ByteArray {
        val buffer = channel.readInbound<ByteBuf>()
        try {
            return ByteArray(buffer.readableBytes()).also { buffer.readBytes(it) }
        } finally {
            buffer.release()
        }
    }
}
