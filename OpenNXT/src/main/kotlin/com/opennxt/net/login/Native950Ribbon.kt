package com.opennxt.net.login

import com.rs.cache.Cache
import com.rs.network.protocol.modern950.Native950Packets
import java.security.MessageDigest

/** Native management menus plus the existing direct HUD-panel shortcuts. */
internal object Native950Ribbon {
    private const val FORCE_OPEN_PANELS_PROPERTY = "ataraxia950.workspace.forceOpenPanels"
    fun enabled(): Boolean = System.getProperty("ataraxia947.ribbon", "true").toBoolean()

    fun initialization(): List<Native950Packets.Packet> {
        val recoverySeed = System.getProperty(FORCE_OPEN_PANELS_PROPERTY, "false").toBoolean()
        val wrapper = (1477 shl 16) or 61
        // Enum13319 actor IDs stay fixed even when their display order changes.
        // Eight management destinations and four direct HUD toggles; zero ends the list.
        val buttons = listOf(128, 129, 130, 131, 132, 135, 136, 137, 0, 2, 3, 18)
        val selection = (buttons + -1).mapIndexed { index, button ->
            val value = button + 1
            if (value > 127) Native950Packets.varcBitLarge(21788 + index, value)
            else Native950Packets.varcBitSmall(21788 + index, value)
        }
        val ribbonLayout = if (recoverySeed) listOf(
            // These scripts establish the old fallback location and save preset 8. They must
            // not run during an ordinary login because the client owns a saved workspace.
            Native950Packets.runClientScript(11145, buttons.size * 40 + 24, 48, 0, 0, wrapper),
            Native950Packets.runClientScript(13268, 8, 8, 2, 2, wrapper),
            Native950Packets.runClientScript(2330, wrapper),
            Native950Packets.runClientScript(8707, 1002),
            Native950Packets.runClientScript(8708, 1002, 8)
        ) else emptyList()
        return selection + listOf(
            // Native checkbox 2755 toggles this preference, then copies it to 42113.
            // Keep the forced login selection and its next-toggle source in sync.
            Native950Packets.varcBitSmall(21816, 1),
            Native950Packets.varcBitSmall(42113, 1)
        ) + ribbonLayout + if (recoverySeed) standalonePanels() else emptyList<Native950Packets.Packet>() + listOf(
            Native950Packets.runClientScript(13833, 1431 shl 16, (1431 shl 16) or 12),
            // All eight management actors notify the server. Direct HUD toggles retain
            // their own cache scripts and do not need duplicate server operations.
            Native950Packets.interfaceEvents(1431, 0, 0, 7, 2)
        )
    }

    /**
     * Legacy recovery seed for an empty layout only. It clears tab links and writes preset 8,
     * so ordinary login must never call it: doing so destroys a player's saved tab groups.
     */
    private fun standalonePanels(): List<Native950Packets.Packet> {
        // 8701 treats a hidden rectangle of zero size/position with no neighbors as
        // uninitialized and restores preset 1, including its unwanted chat links.
        // These inactive slots share minimum-size struct 21320 (100 by 135). Keep
        // them hidden, but give their saved layout an unambiguous nonzero size.
        val inactive = mapOf(9 to 407, 19 to 429, 20 to 439, 21 to 449,
            22 to 459, 23 to 469, 25 to 479, 46 to 489)
        val hiddenGeometry = inactive.values.map { Native950Packets.hideInterface(1477, it, true) } +
            inactive.values.map { Native950Packets.runClientScript(11145, 100, 135, 0, 0, (1477 shl 16) or it) }
        // Native close promotes another actor in the LIVE tab bar even if its wrapper was
        // hidden by the server. Reset both ends of the implemented and inactive chat links.
        // 8361 rebuilds only the slot's own tab; it preserves wrapper geometry/visibility.
        val slots = listOf(0, 2, 3, 18) + inactive.keys
        return hiddenGeometry + slots.map { Native950Packets.runClientScript(8361, it) } + slots.flatMap {
            // 8707 now reads no previous/next tab (-1/-1) into working layout 9.
            // Copy it to preset 8 too, otherwise resize reconstructs the old tab chain.
            listOf(Native950Packets.runClientScript(8707, it), Native950Packets.runClientScript(8708, it, 8))
        }
    }

    fun verify(readFile: (Int, Int, Int) -> ByteArray = { index, group, file ->
        check(Cache.isFlatReadOnly())
        requireNotNull(Cache.STORE.indexes[index].getFile(group, file))
    }) {
        val components = mapOf(
            0 to "e6244517899a81adb8143e85c7c4504535144683faa93cbb62945a9709879b99",
            5 to "622c126febc691d93e58bcaabca9f1839824f3d4e44a447bcb0dab1317d55f2c",
            8 to "f1080fd0496ef9b65a122dddfb78565b65606db2e6e5f7fe035623558a4d1157",
            12 to "e6244517899a81adb8143e85c7c4504535144683faa93cbb62945a9709879b99"
        )
        val bits = mapOf(
            21788 to "933710ce8ad820a3472a9f269465223beed16154e53921be45a506e8cec10d26",
            21789 to "ed6cdaf8a62719f2e874b9ba7cb10c128e45c7fe11cd6270d1aba8799908898f",
            21790 to "0ad2fa4437a50d6baee08ace5669524d7e39c572acbc54932c8aa7ad67a44c0b",
            21791 to "75a0210d2731f9931fd7d0b9c75ae14f49397cd54538466ff22265e3928abdd6",
            21792 to "0d6da01a4c197c3f56265f0ce5ea724188918e1108dc0da4e04222bb1c6bd1d1",
            21793 to "bf9294a364ab88b46a83af87aee4ad31e7d45ffe031aefafe49191623f33647e",
            21794 to "dadbb2214ee1dd24f1850b9e9988c9bac86657a81b13153602a1ee46c8e8972b",
            21795 to "5cf3a4d58f4e665b6ebb2362f7a15917274782c6f06aedc3c317263533b629b7",
            21796 to "86a0afc843ab362643361dcf7f08d674b6ef393c07ab72804c925f93f23a486a",
            21797 to "39df68dfea425a8c3df68e5f341cd6c1f5958ae2a4b163cc9c8a2905fc71c500",
            21798 to "badba67a97c5f4b8d348ba1aa45634f665ea24862fac7610182c6d5064344cc6",
            21799 to "b115c5f4f5e0a58ef06103547a993fda1108dccae49c75bad04d0a6c50f9a8e8",
            21800 to "5cd7e6ad149568a44f65ad91dcb160c9e625049c7b1e1833838f819dd181b1fa",
            21816 to "4e37e79c000bc9f05d42428d918695824ffbfab6cd08d53ab7baf4585e138e45",
            42113 to "8bfff555ecc3b93ddc79c920468a94252e333fca81ad555ca91a1626e0a42431"
        )
        val scripts = mapOf(
            11145 to "3785303ee6b2c09c260e718b853c8ef7773bc080d07d0dc3d4a6d150b0350575",
            13268 to "f1f865db2310bb0fb19337a693f97d60b89b5949c1d9b1f94fcbb3308295f18a",
            8707 to "c40ff90931165d958d9995914b4c55627f7b9a6d2cd11c21eeb0e20a47e17cd6",
            8708 to "f2cdca500c552a91ba4ff11ea5b22cbd9b91c6602c3ba1be4d4264f9efa30039",
            13845 to "210ebcc916d1cf7cbdd8a2840419ab90fd35b1a30ec687cf18bb7f245f920d8b",
            10405 to "a14d6490b6b8fbf6c1d56fcf7be903edbae2420a2cec0ab81de395e9bfc79242",
            5588 to "2eff8418dafb4216c4f66257a3753065c44c6a31202ae9ea8f90d1339f92f30e",
            13833 to "1648986531d3555209a5c1fea7a2f16829500f02bd13f4a904bbf525408968ae",
            13843 to "85a18e5d67dca31404f45d318459d8be680b997065623ac67402b99ebb8f9f23",
            8144 to "4048c52db63fd081c8909a6253721389ce329fb2821ad6288aed606f9692ab30",
            8145 to "acb09c62b546ce35b583cc78afe40276c871e7d2c3ef111ac41001d910b31359",
            8146 to "7e04f05c851aea9b670382529eb507b2d1a9be45152a91c87c1d874f8d0fc031",
            8159 to "5289f83365e2fae735ed1ea16611c9c8ee7034005e892253b2b5b440504b3a7c",
            8361 to "4eabc1a216b542fded7ad97f85a6a8f2effea6dbbaeb4b184da66239c989c198",
            8362 to "82af57548db6b21618f696deba3bd27ab5555ef321d7d603a8871f3c5673a07a",
            8363 to "d6079a86070ac650dab6ff849e2d26977eef502f8e87325732526e7cbc9d60ab",
            2384 to "adbbae391a7e0457d0765e4bc9599c4b724cc1b33625a0f5d923f698ceda25d7",
            2387 to "72d4495b367b15fec6d14d81d850e34190e0d6a3262c7c207f0f2469f09bb1dd",
            2755 to "6bb83530169cf83a78ca40e1a8836c59f3ce84eaf0cc0180aaa412020185fe1e"
        )
        fun checkFile(index: Int, group: Int, file: Int, expected: String) {
            val actual = MessageDigest.getInstance("SHA-256").digest(readFile(index, group, file))
                .joinToString("") { "%02x".format(it.toInt() and 255) }
            Native950CacheContent.requirePin("Native ribbon file $index/$group/$file", expected, actual)
        }
        components.forEach { (id, sha) -> checkFile(3, 1431, id, sha) }
        checkFile(3, 1477, 61, "038b8e31eade4d7f1dd179a644069fb3cb56174699dbf1729dbd4a212d6e1a2c")
        checkFile(17, 52, 7, "a0c76027b1ba4c203e68c6cfb80227a57b54fbfd051834fd1ef826a1a1be48df") // enum13319 actors
        checkFile(17, 52, 9, "6f0cc7447a757a727188f869a8bb48264f69671bfe1e2a3893cd9e5d4c1f39c6") // enum13321 management IDs
        checkFile(22, 1215, 3, "c3373fd34f32cbb990b7db4cbed57bc32dfd926c42fec537b9bd03c00ff6f338") // struct38883 minimum48x48
        checkFile(17, 30, 36, "63a614bce39c6953c96821e6fda3ad88ea42b5d3a6d0b8feb82819b2ce84694a") // enum7716
        bits.forEach { (id, sha) -> checkFile(2, 69, id, sha) }
        scripts.forEach { (id, sha) -> checkFile(12, id, 0, sha) }
        val inactiveStructs = mapOf(
            21284 to "bef83b3197fe587320dea043cfd033b138159a5ed87d8b99cc48e3c16c0d3e58",
            21280 to "99d1061ba790cfc3e4068a42c98c838cbf6ef3b15546463b437e165d57f5def0",
            21281 to "9c98cb0cf22ea4cc1d165f3e18b040ea8146a3925aed22f950db7d3e625c064a",
            21282 to "b30f544a552d107453f5a858f1ee8ee95e742ca549a8be3e33c67cd28644efcb",
            21283 to "e0ef07cc68f4956813737dfd9f839a58bde0448ed08f8a97d23da73e0ff13126",
            3280 to "e99a887d258a383ded78072df1afa8bec4d59bfc77c2b7e49bf0fef9332fbc1f",
            28883 to "3054212e51b50ada13865dae11f9f767d57c3a5c798942b850e160e79e832048",
            50664 to "50406a3fcedafb621c1362634bf772f014ea8e5a7048f058a523d0565951e8c7",
            21320 to "3aceac9f3cd7b9db1aa919f464f21f626fef3ec1efb1ead4e6498c9f4c423e7f"
        )
        inactiveStructs.forEach { (id, sha) -> checkFile(22, id ushr 5, id and 31, sha) }
    }
}
