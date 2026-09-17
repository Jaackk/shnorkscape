package com.opennxt.filesystem

import java.nio.ByteBuffer
import java.nio.ByteOrder

/** Selects the wire/CRC bytes of a normal archive without decoding its compressed payload. */
object Js5ContainerPayload {
    /**
     * Returns a read-only view without the optional, exactly two-byte on-disk version.
     * Header lengths are big-endian regardless of [raw]'s byte order. Its position,
     * limit, mark, order and bytes remain untouched. Reference tables (index 255)
     * use a separate transport path and must not pass through this normalizer.
     */
    fun archiveBody(raw: ByteBuffer): ByteBuffer {
        val data = raw.duplicate().order(ByteOrder.BIG_ENDIAN)
        val start = data.position()
        val available = data.remaining()
        require(available >= 5) { "Truncated cache container" }
        val compression = data.get(start).toInt() and 255
        require(compression in 0..3) { "Unknown cache compression: $compression" }
        val size = data.getInt(start + 1)
        require(size >= 0) { "Negative cache container length" }
        val header = if (compression == 0) 5 else 9
        require(available >= header) { "Truncated compressed cache header" }
        if (compression != 0) require(data.getInt(start + 5) >= 0) { "Negative expanded cache length" }
        // Use Long before addition so malformed Int-sized declarations cannot wrap.
        val length = header.toLong() + size
        require(length == available.toLong() || length + 2 == available.toLong()) {
            "Cache container length does not match stored bytes"
        }
        // Validation bounds length by remaining(), making this addition safe too.
        data.limit(start + length.toInt())
        return data.slice().asReadOnlyBuffer()
    }
}
