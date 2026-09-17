package com.opennxt.net.login

import com.rs.cache.Cache
import com.rs.network.protocol.modern950.Native950Packets
import java.security.MessageDigest

/** The native event mask gates packets; op labels need their own cache-side update. */
internal object Native950RunOrb {
    fun initialization(): Native950Packets.Packet =
        // Paired-cache 3916 case 0 clears op text 2..4 only. The orb has no ops 3/4.
        // Unlike 10370 with empty text, this leaves the run prediction hook intact.
        Native950Packets.runClientScript(3916, 0, (1465 shl 16) or 15)

    fun verify(readFile: (Int, Int, Int) -> ByteArray = { index, group, file ->
        check(Cache.isFlatReadOnly())
        requireNotNull(Cache.STORE.indexes[index].getFile(group, file))
    }) {
        val expected = mapOf(
            Triple(12, 3916, 0) to "9a6975c58a1acc99ef261618add01ff50a6a9784baff8c7c6eb40e15bd52dc8e",
            Triple(3, 1465, 15) to "041aad3239ded0d8591080ddeb53eb98f7fd8292321a7efc4baa408e1a70b6d7",
            Triple(12, 1315, 0) to "b955e492eecc7da593b4090563810860c91856a92b1de79492823c9b4a740ce8",
            Triple(12, 1741, 0) to "470bacbaeb3a2b62250fe6fc8cdf637c9f1ffccae8e7d49924472f48efe15e52"
        )
        for ((path, hash) in expected) {
            val actual = MessageDigest.getInstance("SHA-256")
                .digest(readFile(path.first, path.second, path.third))
                .joinToString("") { "%02x".format(it.toInt() and 255) }
            Native950CacheContent.requirePin("Native run-orb menu file $path", hash, actual)
        }
    }
}
