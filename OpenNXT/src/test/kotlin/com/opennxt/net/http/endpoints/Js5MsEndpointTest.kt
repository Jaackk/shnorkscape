package com.opennxt.net.http.endpoints

import com.opennxt.net.http.HttpRequestHandler
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.handler.codec.http.DefaultFullHttpRequest
import io.netty.handler.codec.http.FullHttpResponse
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpVersion
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.CRC32
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class Js5MsEndpointTest {
    @AfterTest fun reset() {
        Js5MsEndpoint.resetForTests()
        RetailSessionCookie.resetForTests()
    }

    @Test fun `archive responses exclude only the stored version for every compression type`() {
        for (compression in 0..3) {
            val wire = container(compression)
            val stored = wire + byteArrayOf(0, 15)
            val result = Js5MsEndpoint.archiveHttpBody(ByteBuffer.wrap(stored)).bytes()
            assertContentEquals(wire, result)
            assertEquals(crc(wire), crc(result))
            assertNotEquals(crc(stored), crc(result))
            assertContentEquals(stored, wire + byteArrayOf(0, 15))
        }
    }

    @Test fun `already normalized wire archives keep every payload byte`() {
        for (compression in 0..3) {
            // A payload ending in version-looking bytes must not be truncated.
            val wire = container(compression, byteArrayOf(74, 65, 71, 65, 0, 15))
            assertContentEquals(wire, Js5MsEndpoint.archiveHttpBody(ByteBuffer.wrap(wire)).bytes())
        }
        assertContentEquals(byteArrayOf(0, 0, 0, 0, 0),
            Js5MsEndpoint.archiveHttpBody(ByteBuffer.wrap(byteArrayOf(0, 0, 0, 0, 0, 0, 15))).bytes())
    }

    @Test fun `normalization respects buffer bounds without changing source state or bytes`() {
        val wire = container(0)
        val backing = byteArrayOf(91, 92) + wire + byteArrayOf(0, 15, 93)
        val original = backing.copyOf()
        val raw = ByteBuffer.wrap(backing).order(ByteOrder.LITTLE_ENDIAN)
        raw.position(2)
        raw.limit(backing.size - 1)
        val result = Js5MsEndpoint.archiveHttpBody(raw)
        assertContentEquals(wire, result.bytes())
        assertTrue(result.isReadOnly)
        assertEquals(2, raw.position())
        assertEquals(backing.size - 1, raw.limit())
        assertEquals(ByteOrder.LITTLE_ENDIAN, raw.order())
        assertContentEquals(original, backing)
    }

    @Test fun `invalid lengths fail instead of truncating arbitrary cache bytes`() {
        val bad = listOf(
            byteArrayOf(), byteArrayOf(0, 0, 0, 0),
            byteArrayOf(4, 0, 0, 0, 0),
            ByteBuffer.allocate(5).put(0).putInt(-1).array(),
            ByteBuffer.allocate(5).put(0).putInt(Int.MAX_VALUE).array(),
            byteArrayOf(2, 0, 0, 0, 0),
            ByteBuffer.allocate(9).put(2).putInt(0).putInt(-1).array(),
            container(0) + byteArrayOf(1), container(0) + byteArrayOf(1, 2, 3),
            container(0).copyOf(container(0).size - 1)
        )
        for (bytes in bad) assertFailsWith<IllegalArgumentException> {
            Js5MsEndpoint.archiveHttpBody(ByteBuffer.wrap(bytes))
        }
    }

    @Test fun `HTTP response length and CRC match the native request rather than the cache file`() {
        val wire = container(0)
        val stored = wire + byteArrayOf(0, 15)
        val requestedCrc = crc(wire).toInt()
        Js5MsEndpoint.payloadResolver = { index, archive ->
            assertEquals(40, index)
            assertEquals(36067, archive)
            Js5MsEndpoint.ResolvedPayload(Js5MsEndpoint.archiveHttpBody(ByteBuffer.wrap(stored)), "archive")
        }
        RetailSessionCookie.pinCurrent() // The test must not fetch an upstream cookie.
        val channel = EmbeddedChannel(HttpRequestHandler())
        try {
            val request = DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
                "/ms?m=0&a=40&k=947&g=36067&c=$requestedCrc&v=15")
            request.headers().set(HttpHeaderNames.HOST, "127.0.0.1:8080")
            channel.writeInbound(request)
            val response = channel.readOutbound<FullHttpResponse>()
            try {
                assertEquals(HttpResponseStatus.OK, response.status())
                assertEquals("application/octet-stream", response.headers()[HttpHeaderNames.CONTENT_TYPE])
                assertEquals(wire.size.toString(), response.headers()[HttpHeaderNames.CONTENT_LENGTH])
                val actual = ByteArray(response.content().readableBytes())
                response.content().readBytes(actual)
                assertContentEquals(wire, actual)
                assertEquals(requestedCrc, crc(actual).toInt())
            } finally { response.release() }
        } finally { channel.finishAndReleaseAll() }
    }

    private fun container(compression: Int, payload: ByteArray = byteArrayOf(74, 65, 71, 65)): ByteArray {
        val buffer = ByteBuffer.allocate((if (compression == 0) 5 else 9) + payload.size)
        buffer.put(compression.toByte()).putInt(payload.size)
        if (compression != 0) buffer.putInt(100)
        return buffer.put(payload).array()
    }
    private fun ByteBuffer.bytes(): ByteArray = ByteArray(remaining()).also { duplicate().get(it) }
    private fun crc(bytes: ByteArray): Long = CRC32().apply { update(bytes) }.value
}
