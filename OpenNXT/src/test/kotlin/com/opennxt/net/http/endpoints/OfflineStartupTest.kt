package com.opennxt.net.http.endpoints

import com.opennxt.OpenNXT
import com.opennxt.config.ServerConfig
import com.opennxt.model.files.BinaryType
import com.opennxt.model.files.ClientConfig
import io.netty.handler.codec.http.DefaultFullHttpRequest
import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.HttpVersion
import io.netty.handler.codec.http.QueryStringDecoder
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OfflineStartupTest {
    @BeforeTest
    fun initialize() {
        OpenNXT.config = ServerConfig().apply { build = 947 }
        JavConfigWsEndpoint.resetLiveConfigCacheForTests()
        Js5MsEndpoint.resetForTests()
    }

    @AfterTest
    fun resetFetchers() {
        JavConfigWsEndpoint.resetLiveConfigCacheForTests()
        Js5MsEndpoint.resetForTests()
    }

    @Test
    fun `offline mode requires an explicit enabled value and blocks retail passthrough`() {
        for (value in listOf(null, "", "0", "false", "yes", "unexpected")) {
            assertFalse(OpenNXT.offlineModeEnabled(value))
        }
        for (value in listOf("1", "true", " TRUE ")) {
            assertTrue(OpenNXT.offlineModeEnabled(value))
        }
        assertFalse(OpenNXT.retailRawChecksumPassthroughEnabled(947, "1", offline = true))
        assertFalse(OpenNXT.retailLoggedOutJs5PassthroughEnabled(947, "1", offline = true))
        assertFalse(OpenNXT.retailLoggedOutJs5ProxyEnabled(947, "1", "0", offline = true))
        assertTrue(OpenNXT.retailRawChecksumPassthroughEnabled(947, "1", offline = false))
        assertTrue(OpenNXT.retailLoggedOutJs5ProxyEnabled(947, "1", "0", offline = false))
    }

    @Test
    fun `offline startup reads the bundled patched config without a live session request`() {
        var calls = 0
        JavConfigWsEndpoint.liveConfigResponseFetcher = { _, _ ->
            calls++
            error("Offline startup must not fetch a live session")
        }
        val query = QueryStringDecoder("/jav_config.ws?binaryType=2&baseConfigSource=patched" +
            "&localRewrite=1&hostRewrite=1&lobbyHostRewrite=1&gameHostOverride=127.0.0.1" +
            "&gamePortOverride=43594&contentRouteRewrite=1&worldUrlRewrite=1&codebaseRewrite=1" +
            "&downloadMetadataSource=patched")
        val prepared = JavConfigWsEndpoint.prepareConfig(BinaryType.WIN64, query, offline = true)
        assertEquals("patched", prepared.source)
        assertEquals("947", prepared.config["server_version"])
        assertEquals("127.0.0.1", prepared.gameHostOverride)
        assertTrue(prepared.config["codebase"]!!.startsWith("http://"))
        assertEquals(0, calls)
        val fallback = JavConfigWsEndpoint.loadBaseConfig(BinaryType.WIN64, "live", false, offline = true)
        assertEquals("patched", fallback.second)
        assertEquals("947", fallback.first["server_version"])
        assertEquals(0, calls)
    }

    @Test
    fun `offline mode bypasses the live fetcher while online lookup remains available`() {
        var calls = 0
        JavConfigWsEndpoint.liveConfigResponseFetcher = { _, _ ->
            calls++
            JavConfigWsEndpoint.LiveConfigResponse(ClientConfig.parse("server_version=947\nparam=32=local-test"), null)
        }
        assertNull(JavConfigWsEndpoint.loadLiveConfig(BinaryType.WIN64, false, offline = true))
        assertEquals(0, calls)
        val online = JavConfigWsEndpoint.loadLiveConfig(BinaryType.WIN64, false, offline = false)
        assertEquals("local-test", online!!.first.getParam(32))
        assertEquals(1, calls)
    }

    @Test
    fun `offline response cookies are generated locally without upstream lookups`() {
        var calls = 0
        RetailUpstreamCookie.cookieFetcher = { calls++; listOf("JXADDINFO=online-test") }
        val localCookie = RetailSessionCookie.current()
        assertEquals(localCookie, RetailUpstreamCookie.resolveJavConfigCookie(
            "https://example.invalid/jav_config.ws", BinaryType.WIN64, offline = true))
        assertEquals(localCookie, RetailUpstreamCookie.resolveMsCookie(offline = true))
        assertEquals(0, calls)
        assertEquals("JXADDINFO=online-test", RetailUpstreamCookie.resolveJavConfigCookie(
            "https://example.invalid/jav_config.ws", BinaryType.WIN64, offline = false))
        assertEquals(1, calls)
    }

    @Test
    fun `missing offline assets remain missing instead of being fetched from another cache`() {
        var calls = 0
        Js5MsEndpoint.upstreamPayloadFetcher = { _, _ ->
            calls++
            Js5MsEndpoint.UpstreamMsResponse(byteArrayOf(1, 2, 3))
        }
        RetailSessionCookie.noteCurrent("JXADDINFO=local-test")
        val request = DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/ms?a=40&g=7&k=947")
        try {
            assertNull(Js5MsEndpoint.resolveRetailFallbackPayload(request, 40, 7, offline = true))
            assertEquals(0, calls)
            val online = Js5MsEndpoint.resolveRetailFallbackPayload(request, 40, 7, offline = false)
            assertEquals("retail-upstream", online!!.kind)
            assertEquals(1, calls)
        } finally {
            request.release()
        }
    }
}
