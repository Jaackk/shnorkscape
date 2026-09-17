package com.opennxt.net.login

import com.rs.cache.Cache
import com.rs.game.player.client.Native950ItemCatalog
import com.rs.game.player.client.Native950Content
import com.rs.game.player.client.Native950Appearance
import com.rs.network.protocol.modern950.Native950Packets
import com.opennxt.OpenNXT
import com.opennxt.resources.config.enums.EnumDefinition
import com.opennxt.resources.config.structs.StructDefinition
import java.security.MessageDigest

/** Small, fail-closed catalog for the modern definitions and UI bindings inspected locally. */
internal object Native950CacheContent {
    fun content(): Native950Content {
        verifyInterfaceBindings()
        val slots = requireNotNull(OpenNXT.resources.get<EnumDefinition>(7716))
        val bank = requireNotNull(OpenNXT.resources.get<StructDefinition>(slots.values[1017] as Int))
        val parent = bank.getInt(3505, default = -1)
        val wrapper = bank.getInt(3503, default = -1)
        check(parent >= 0 && wrapper >= 0) { "Missing native bank attachment" }
        val equipment = requireNotNull(OpenNXT.resources.get<StructDefinition>(slots.values[3] as Int))
        // Nonzero3494 selects the bank modal anchor (3503). CS10906 tests517 there;
        // using the nested3505 host skips refresh and triggers immediate actor compaction.
        val mount = bankMount(parent, wrapper, bank.getInt(3494, default = 0))
        return Native950Content(itemCatalog().withLegacyDrops(), bankUi(mount, wrapper), banker(),
            equipmentUi(equipment.getInt(3505, default = -1), equipment.getInt(3503, default = -1)),
            appearance())
    }

    fun banker(readFile: (Int, Int, Int) -> ByteArray = ::readCacheFile): Native950Content.BankerNpc {
        // Native cache option 1 is Bank; Talk to is option 3, with an absent option 2.
        verifyFile(18, 3, 110, "b599c1067d13cc4858663551fcaef4461a62d34ec5eb64326658492007cd29fe", readFile)
        verifyFile(2, 32, 3180, "41c95eacbf2f7f7d36757b1cb026aa16d820135f093c4104c1ab5d4c061f214a", readFile)
        return Native950Content.BankerNpc(494, "Banker", 3217, 3257, 0, 1, 1, 3, 4)
    }

    internal fun equipmentUi(parent: Int, wrapper: Int): Native950Content.EquipmentUi {
        check(parent == ((1477 shl 16) or 114) && wrapper == ((1477 shl 16) or 112)) {
            "The selected cache has an unverified equipment attachment"
        }
        return Native950Content.EquipmentUi(1462, 31, 94, listOf(
            // The initial interface phase attaches the panel before its container arrives.
            // Initialize its geometry explicitly: unused native layout slots begin at zero size.
            Native950Packets.runClientScript(11145, 224, 360, 0, 0, wrapper),
            Native950Packets.runClientScript(13268, 232, 80, 2, 2, wrapper),
            Native950Packets.runClientScript(2330, wrapper),
            Native950Packets.runClientScript(8471, (1462 shl 16) or 3, 94),
            // Save this slot to both the current layout and the selected custom preset.
            // Otherwise a window resize restores preset 8's hidden, zero-size slot.
            Native950Packets.runClientScript(8707, 3),
            Native950Packets.runClientScript(8708, 3, 8),
            Native950Packets.interfaceEvents(1462, 31, 0, 18, 2 or 1024)
        ), listOf(Native950Packets.runClientScript(8471, (1462 shl 16) or 3, 94)))
    }

    internal fun appearance(readFile: (Int, Int, Int) -> ByteArray = ::readCacheFile): Native950Appearance {
        verifyFile(28, 6, 0, "e600212f895943b3dcfc853bf4ae5a11a16e3a95e6c52b405d7b5b3ac300c1f2", readFile)
        verifyFile(2, 32, 2699, "170691c58f90a79167e134825a17d41fbb224133f469a5b03e63bdf8b5954fb8", readFile)
        verifyFile(2, 32, 2584, "e036638807022ce4dd345a18153482dcfc55935f384fda5f84d50e0ae3e83b83", readFile)
        return Native950Appearance(intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 1, 0))
    }

    internal fun bankMount(contentHost: Int, wrapper: Int, layoutStruct: Int): Int =
        if (layoutStruct != 0) wrapper else contentHost

    internal fun bankUi(parent: Int, wrapper: Int): Native950Content.BankUi {
        val deposits = IntArray(11).apply {
            this[1] = 1; this[2] = 1; this[3] = 5; this[4] = 10; this[7] = Int.MAX_VALUE
        }
        val withdrawals = IntArray(11).apply {
            this[1] = 1; this[2] = 1; this[3] = 5; this[4] = 10; this[7] = Int.MAX_VALUE
        }
        val open = listOf(
            Native950Packets.varbitSmall(45141, 1), // All Items tab
            Native950Packets.varbitSmall(45158, 1), // coins to backpack
            Native950Packets.openSub(parent ushr 16, parent and 65535, 517, false),
            Native950Packets.hideInterface(wrapper ushr 16, wrapper and 65535, false)
        ) + com.rs.game.player.client.Native950BankUi.openControls()
        val close = listOf(
            Native950Packets.closeSub(parent ushr 16, parent and 65535),
            Native950Packets.hideInterface(wrapper ushr 16, wrapper and 65535, true)
        )
        return Native950Content.BankUi(
            517, 201, 15, 317, 39, deposits, withdrawals, open, close, 6
        )
    }

    fun itemCatalog(readFile: (Int, Int, Int) -> ByteArray = ::readCacheFile): Native950ItemCatalog {
        val definitions = listOf(
            Definition(995, "Coins", true,
                arrayOf("Add to pouch", "Add X to pouch", null, null, "Drop"),
                "628c2fd1154e10ed7eb2cda75f31d9736d12636269323e6adab292bbffac45ff"),
            Definition(1511, "Logs", false,
                arrayOf("Craft", "Light", null, null, "Drop"),
                "29257b9164a4e328cc2d6c3117695a386da8e41f1b9cea023bda905a1f45ba00"),
            Definition(315, "Shrimps", false,
                arrayOf("Eat", null, null, null, "Drop"),
                "3d364d02ca06a4e048ad1a4cb69f7eb51ce584babee5191a42deb3f5f7f30372"),
            Definition(1277, "Bronze sword", false,
                arrayOf(null, "Wield", null, null, "Drop"),
                "6433fee53de4734fc83e785825ad1219a2591d3134e33e5abba3491fffc5d95b", 3, 2),
            Definition(1173, "Bronze square shield", false,
                arrayOf(null, "Wield", null, null, "Drop"),
                "97af6e94f54e0289f9b4c6ee7d6ad0430ff34025df9215bf87f383fe3da9f184", 5, 2),
            Definition(1139, "Bronze med helm", false,
                arrayOf(null, "Wear", null, null, "Drop"),
                "df1f9d31a32f0dac7ba94adccf8d0e01c9515c1681a351c41877a879562ca3a0", 0, 2)
        )
        return Native950ItemCatalog(definitions.map { definition ->
            verifyFile(19, definition.id ushr 8, definition.id and 255, definition.sha256, readFile)
            Native950ItemCatalog.Entry(definition.id, definition.name,
                definition.stackable, definition.options, definition.equipSlot, definition.equipOption)
        })
    }

    /** UI components are created by these scripts; a changed cache needs fresh bindings. */
    fun verifyInterfaceBindings() {
        com.rs.game.player.client.Native950BankUi.verify()
        verifyModernModeBindings()
        verifyEquipmentBindings()
        verifyFile(3, 1473, 0, "4a6a3bfb42122da83492c2fad4f2f70cb4c6a4eb196b927a73667d4fb5164d5b")
        verifyFile(12, 8682, 0, "875561f065b16f74615745963e589278ab007fb94fe1bce4b2e535427b3d0254")
        verifyFile(12, 8678, 0, "727bc18626541d39da8dd739fe87dbb292ca37766782e83e85c7e6ef9b9e384f")
        // Inventory-menu call chain and nonconsecutive ordinary option mapping.
        verifyFile(12, 8677, 0, "61ce759acd890ac63405e11de88d80581b6f72e1d384eb900cdd714cf3333eb6")
        verifyFile(12, 8679, 0, "6519901216960540530774f3d0d27d763fe65608950006786ec7893343d6ca13")
        verifyFile(12, 8680, 0, "0b52ef101d691c4e3f2c5e04c454af6e3ceca2bc705c1a3b8760c37b248008d7")
        verifyFile(12, 12090, 0, "aae4aa14c3992f92c4dfcc3741171daa5236458f4ca07049ae3db2ba87241d54")
        verifyFile(12, 2833, 0, "3277400face2411e85602eececb21520fd33a3003969678356d8cc20eeed36f8")
        verifyFile(12, 2410, 0, "7da996882e2804da8b357bf812a52916cdd15af966664058710fa7995f75adc3")
        verifyFile(12, 1620, 0, "e695aad1827f75c2a7f4a0a1c634cd1812bebff8b30aedd83b84a554059235e2")
        verifyFile(3, 517, 0, "0693acb772e6db8cd24787c393e3831316d21614b7cae484b1e8aea0c0547ecd")
        verifyFile(12, 13353, 0, "7a6fbf2e3cf3dc6fcbfffea3714aa42b68944a600259fffb3ccd879b94ba7f78")
        verifyFile(12, 13798, 0, "6c82ce1618e069e0e2a5e3946e867b7fee8f454119badcd275abed001aae60ed")
        verifyFile(12, 9239, 0, "d3edd2f2b22ea6cfbcd01ec4c57eea1832f65d3404156bae6d91d126ed521274")
        verifyFile(12, 13687, 0, "0879ef07407b1ae2429a56cef2448021661f09b60902843ecca372e374f63839")
        verifyFile(12, 14419, 0, "98af497f34ca6ce5e661b6b06e413e3687e4409176eed0c5d9cd735343b6be27")
        verifyFile(12, 6962, 0, "b21de7c4ef11b16da34ab8289ba768df6ad001c894846a4419366b5107aa3769")
        verifyFile(12, 9324, 0, "f038d900938eec0a32efe437e25aa1a4ec96d234c2a279c5d176e7c4b8ae7898")
        verifyFile(12, 14337, 0, "38b3b6e78d2306d3f99d3f4a9c4a46e6df7d086cce25aaf10ea1f4567fa29d2b")
        verifyFile(12, 14288, 0, "237431cb4468d962b6298092a6b256f5e453cba5e3db22d5ddc5d14cdd290e10")
        verifyFile(12, 6794, 0, "f940a8d5dd744eadbb6e5e1fc4fe710c38a6e8d9eb08d0f460fa23fe4af2cc17")
        verifyFile(12, 14073, 0, "852442b1b62d14524454bd0943422e016f8034646a8d4ae9e756fa09b55141e0")
        verifyFile(12, 6793, 0, "e27c0ddee4502d816c836a1c47ac5aa5806cdd0a882958f84daf3baa0eeb4f6a")
        verifyFile(12, 14351, 0, "9ef8dfbfecaacc85069f476c880b310d97ed07fb1f716d5921f347d90397eb06")
        verifyFile(12, 5175, 0, "2ddae4c65ab0573e92a48d4a063915694447daaa3be2ae5cf6ab930a7dfb918f")
        // These onOp scripts replace exhausted item actors before the native click is sent.
        verifyFile(12, 5823, 0, "3b62c3587a3ff3dc3fa4cded6b988eb2e63dd79f0fac4532a863b13983472a06")
        verifyFile(12, 8911, 0, "ff4953f27071c48d2ef8c4e0b7b26e77f202feff73fafb975fae73e66b77fcf8")
        verifyFile(12, 2293, 0, "561c1e1e37090d55495a402e531fa1d521d6b248992baf63cfeba60fa448832d")
        verifyFile(12, 2295, 0, "dec8435a7c66d23191d018d21ca7aa20178ed4f124c422a774dd16a67004cca1")
        verifyFile(12, 2347, 0, "646b4b32ad6054af02ac5e13b7a392f727ec0dd3fd0b9807c115b57c326eca27")
        verifyFile(12, 14363, 0, "a4c90529a9d2860c6e65289d2c858c6a92dbac6649ad2734c917582d81e32fe2")
        verifyFile(12, 14367, 0, "81d03c038b588510b52469a691047bb2647bc6a76169a24e6301a5e8f4aae80e")
        verifyFile(12, 9240, 0, "91517674cb243888ab4ee000fcc3958d9b7124193d2e8d3c7655b0c3d3ed39af")
        verifyFile(12, 12092, 0, "94536134db1feae61e94cbfda273d220fca8a126fcf6f039bbfa502f79b73f2d")
        verifyFile(12, 14362, 0, "696a77a7fe316cd65f6b32a43c5a83461852c6306a967f1abb240681be76668e")
        verifyFile(12, 13796, 0, "668fa3ad8504f85a6102d020f12598fc094e2e462c6f8dbc7eceec6f7d37cf5d")
    }

    /** Modern interfaces, skin, combat scale and the normal-world onboarding state. */
    internal fun verifyModernModeBindings(readFile: (Int, Int, Int) -> ByteArray = ::readCacheFile) {
        verifyFile(2, 69, 27168, "736a664c00e894e67d201a9a79e3c6a9db4e099f515dc648afbceb43acac3f68", readFile)
        verifyFile(2, 69, 27169, "f800f1c4dcb2ad5a6a7d19f13a44443474ad344048688ecf77e4df208ffed58d", readFile)
        verifyFile(2, 69, 22875, "3a7e8dc7c91fa7d940d669c93555740e61e3fa4804d22415e54638e8643ee65d", readFile)
        verifyFile(2, 69, 39917, "2f60675e65eddff2492bb74b49b2b2bc61830142563cdec08ee020644116e2d4", readFile)
        verifyFile(2, 69, 49044, "08f88fc4270fc2937e57d84efd92add73c84bdb97b9f9a5a073943377afdc094", readFile)
        verifyFile(2, 69, 60098, "fecaf8a54c06f32beb8d26da8a5be2f3a825dac85fd41bc1e3523acddc6a29ac", readFile)
        verifyFile(12, 15532, 0, "0dbb099a8795041974b0f6c1e4668b5a3a42f7f2f9be151f7a40b4689e5dc689", readFile)
        verifyFile(12, 15534, 0, "fc5f7a7318f3dd154c7da001cda85ac95b587fe989a713d2ac8cff6652daa0be", readFile)
        verifyFile(12, 734, 0, "f3aecf194ba03b515add550b5bed1448fcb84a06abfb1628bc5ca657d6da4cdd", readFile)
    }

    internal fun verifyEquipmentBindings(readFile: (Int, Int, Int) -> ByteArray = ::readCacheFile) {
        verifyFile(2, 5, 94, "e011f566d047121caf5f8b608cf65b7be022289d3519dcc10b1eaf876582a96e", readFile)
        verifyFile(3, 1462, 0, "076f92b58a47fa090e1bf24a24b1c92bbe18f31262ea2546374f4623280b53ef", readFile)
        verifyFile(3, 1462, 3, "7bbe9121c015a14ae479de36c7aa0c84b7d67feb960a31a4d40c9e7225998c8e", readFile)
        verifyFile(3, 1462, 31, "ea269f99f0943958a993a2cfc766f23cd0ea0d1b0a36a5dce2e49c16682966a2", readFile)
        verifyFile(2, 69, 54934, "d69de0566daef31e14043cbc9b57fb482d906d785339d21c2f7b49672812a385", readFile)
        val scripts = mapOf(
            6468 to "0dbc3243e4e9489ef614964ae147cb5c3e25c278f8c061b111c93224d13ab0ce",
            18401 to "38fd18dc137a7e6f002fce3183eb6fdc9ca1452fa0d4b0546b47c48f8efbc4cb",
            7031 to "4f0ad753f0173d5548408618c600bb43eda125e74ecd94c73221725dd14d1000",
            2383 to "a55d33a800b3bc79f3b2b8bc27476923a081ed3551240516b9855130c05449f5",
            1520 to "787753d4ed547e5b3afefeb8860d39fff8bc423eac9fee1abbc95feaea07c05b",
            12405 to "2a25cf015df73d3363d125bec82ab79e7a8902fe374edcb735f8425ceb66b434",
            13505 to "88e27e08b0fa5a86a4b74ea85fdf09f69abaf40be05e6f4855ac398b37dc7f46",
            16473 to "eba13e5e5deaa58b2a729809204688d7d22ccee0561d16e7646697ddbe10d645",
            8471 to "a6b1f837eea30ef9877befb81f8888a2424598955f39a7961570c7feac829522",
            8472 to "5925fd9b910ff6bfc0b3cf3589f13e4a774c76cd7f1493436e4b60c01870ae56",
            8469 to "afd52026e32aca40c5475f07b0dca772b8a7c6e0a7cbbe656d63a4b171077c03",
            12112 to "7908fe06d86d9a353e81bab661120c2c9bf0f376c46107bd2b2fa9edef89be84",
            8680 to "0b52ef101d691c4e3f2c5e04c454af6e3ceca2bc705c1a3b8760c37b248008d7",
            12090 to "aae4aa14c3992f92c4dfcc3741171daa5236458f4ca07049ae3db2ba87241d54",
            2833 to "3277400face2411e85602eececb21520fd33a3003969678356d8cc20eeed36f8",
            2410 to "7da996882e2804da8b357bf812a52916cdd15af966664058710fa7995f75adc3",
            1620 to "e695aad1827f75c2a7f4a0a1c634cd1812bebff8b30aedd83b84a554059235e2",
            1621 to "bca3cf0dacab40508f43037b8cf68cad20f1f487ac8960e099187b77260a3534",
            13268 to "f1f865db2310bb0fb19337a693f97d60b89b5949c1d9b1f94fcbb3308295f18a",
            11145 to "3785303ee6b2c09c260e718b853c8ef7773bc080d07d0dc3d4a6d150b0350575",
            2330 to "29dd977d6dc6d576c6bad1af429e48201462a0d06fcdb070ae9d08edf61f4197",
            8468 to "477d3f228191c1b8e0d4f18f57c5b0240eb46d766079c8092396af12eab68455",
            8707 to "c40ff90931165d958d9995914b4c55627f7b9a6d2cd11c21eeb0e20a47e17cd6",
            8709 to "7765d05d5196ccb39c515f00ba81ab4e0fb2ad240a51533f39ea244624544677",
            15999 to "7e364d2f7d4c5dcaf06311530566ffd1a1a9b3b1d3fd9219c825e5426c1b3cee",
            8708 to "f2cdca500c552a91ba4ff11ea5b22cbd9b91c6602c3ba1be4d4264f9efa30039",
            8701 to "bbb7de2a77b39b189b6701d34451304f1bf1f0c9bb9ed4376e25cfdde761bea2",
            8710 to "89630526da0623c02fa3306a08390209e13567851ac1933b2c81b740840adf6d",
            8712 to "34013a19f6cfa7866a5432bddfb466e221dfe34a3b8cef8b682ee5c4cee679e7",
            1886 to "4108aed062c1581e7a8f571f83cba1e051c9423e16f52e6196acb9e17cdba52f"
        )
        scripts.forEach { (script, hash) -> verifyFile(12, script, 0, hash, readFile) }
    }

    private fun readCacheFile(index: Int, group: Int, file: Int): ByteArray {
        check(Cache.isFlatReadOnly()) { "Modern content requires the selected read-only cache" }
        return requireNotNull(Cache.STORE.indexes[index].getFile(group, file)) {
            "Missing modern content file $index/$group/$file"
        }
    }

    /**
     * Whether a failed pin aborts the session or is reported and carried past.
     *
     * This reads the SAME property as the engine's `NativeCacheVerification`, deliberately. There
     * are two pin checkers - this Kotlin one over content and interface files, and the Java one
     * over the engine's own bindings - and a kill switch that silences only one of them is worse
     * than none: it looks like it worked right up until the other aborts the login for the same
     * underlying reason. That is exactly what happened on the first 950 engine run.
     *
     * Enforcement stays ON by default. Turned off, a stale pin means the binding behind it is
     * UNVERIFIED against this cache, so anything it guards is unproven until it is re-pinned.
     */
    private fun pinsEnforced(): Boolean =
        System.getProperty("ataraxia.native.verifyCache", "true").toBoolean()

    private val reportedPins = java.util.Collections.synchronizedSet(LinkedHashSet<String>())

    /** Pins seen to differ from their recorded hash this run, for a summary after a shakeout. */
    fun unverifiedPins(): Set<String> = synchronized(reportedPins) { LinkedHashSet(reportedPins) }

    /**
     * The one gate every cache pin goes through, wherever the pin lives.
     *
     * @param what a human-readable name for the pinned thing, used in both messages
     */
    fun requirePin(what: String, expected: String, actual: String) {
        if (actual == expected) return
        val detail = "$what changed; revalidate its binding (expected $expected, found $actual)"
        check(!pinsEnforced()) {
            "$detail. Re-pin it against this cache, or set -Dataraxia.native.verifyCache=false to" +
                " continue with the binding treated as unverified."
        }
        if (reportedPins.add(what)) println("[native-cache] UNVERIFIED $detail")
    }

    /** SHA-256 of a byte array as lowercase hex, the form every pin in this port is recorded in. */
    fun sha256(data: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(data)
            .joinToString("") { "%02x".format(it.toInt() and 255) }

    private fun verifyFile(index: Int, group: Int, file: Int, expected: String,
                           readFile: (Int, Int, Int) -> ByteArray = ::readCacheFile) {
        val data = readFile(index, group, file)
        val actual = MessageDigest.getInstance("SHA-256").digest(data)
            .joinToString("") { "%02x".format(it.toInt() and 255) }
        if (actual == expected) return
        val detail = "Modern content file $index/$group/$file changed; revalidate its bindings" +
            " (expected $expected, found $actual)"
        check(!pinsEnforced()) {
            "$detail. Re-pin it against this cache, or set -Dataraxia.native.verifyCache=false to" +
                " continue with the binding treated as unverified."
        }
        if (reportedPins.add("$index/$group/$file")) println("[native-cache] UNVERIFIED $detail")
    }

    private data class Definition(val id: Int, val name: String, val stackable: Boolean,
                                  val options: Array<String?>, val sha256: String,
                                  val equipSlot: Int = -1, val equipOption: Int = 0)
}
