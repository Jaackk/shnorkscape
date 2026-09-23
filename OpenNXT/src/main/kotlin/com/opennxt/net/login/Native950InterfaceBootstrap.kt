package com.opennxt.net.login

import com.rs.network.protocol.modern950.Native950Packets
import com.rs.game.player.client.Native950InventoryMenu

/** Packet policy for the Ataraxia backend, independent of cache lookup and the old slot enum. */
internal object Native950InterfaceBootstrap {
    const val ROOT = 1477
    private const val FORCE_OPEN_PANELS_PROPERTY = "ataraxia950.workspace.forceOpenPanels"

    data class Slot(val attach: Int, val wrapper: Int)

    // Keep this order: the baseline golden fixture pins the pre-extraction login burst.
    // A slot becoming addressable must never implicitly change its visibility policy.
    val baselineHiddenSlots: List<Int> = listOf(
        0, 4, 5, 6, 7, 8, 9, 10, 11, 12, 14, 15, 16, 17, 19, 20, 21, 22, 23, 24,
        25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 39, 40, 41,
        1002, 1003, 1005, 1006, 1007, 1009, 1010, 1012, 1013, 1014, 1015, 1016,
        1017, 1018, 1019, 1021, 1023, 1024, 1025, 1026, 1027, 1028, 1029, 1030,
        1031, 1032, 1033, 1034, 1035, 1036, 1037, 1038, 1040, 1041, 1045, 1047, 2008
    )

    // UI-DECISIONS.md Tier 2: these root wrappers were missing from InterfaceSlot.VALUES.
    // In particular 1049 and 1051 are visible by default in the paired cache.
    val inertHiddenSlots: List<Int> = listOf(45, 46, 1049, 1050, 1051, 1052, 1053)
    val hiddenSlots: List<Int> = baselineHiddenSlots + inertHiddenSlots

    /**
     * Native content still has to be mounted for its saved workspace slot to render. Visibility,
     * however, is workspace state and must not be overwritten at every login.
     */
    private data class Panel(val slot: Int, val interfaceId: Int, val canForceShow: Boolean)
    private val panels = listOf(
        Panel(1000, 1482, false), Panel(1004, 1465, false), Panel(2, 1473, true),
        Panel(3, 1462, true), Panel(4, 1458, true), Panel(5, 1461, true),
        // These cache slot entries own the populated ability-book windows.  Leaving
        // them unattached creates empty Melee/Ranged/Defensive frames while Magic loads.
        Panel(6, 1460, true), Panel(7, 1452, true), Panel(39, 1880, true),
        Panel(33, 1884, true), Panel(34, 1885, true), Panel(35, 1887, true), Panel(36, 1886, true),
        // Exact950 CS8423 maps these native books to slots42/43/44. Their
        // version6 onLoad hooks call8422 with categories4/14/15 (enum16973).
        // 1887 is Teleport Spells (category10), not a Necromancy book.
        Panel(42, 1219, true), Panel(43, 1220, true), Panel(44, 1221, true),
        // Exact950 enum7716: slots1009/1038 attach284/291. Native scripts own icons/timers.
        Panel(1009, 284, true), Panel(1038, 291, true),
        Panel(18, 137, true)
    )

    /** [ribbonEnabled] stays false until the ribbon's component and click evidence is admitted. */
    fun packets(
        slots: Map<Int, Slot>,
        hiddenSlotKeys: List<Int> = hiddenSlots,
        assertModernMode: Boolean = true,
        ribbonEnabled: Boolean = false
    ): List<Native950Packets.Packet> {
        val forceOpenPanels = System.getProperty(FORCE_OPEN_PANELS_PROPERTY, "false").toBoolean()
        val opened = if (ribbonEnabled) panels + Panel(1002, 1431, true) else panels
        val retained = mutableSetOf(hash(0), hash(27), hash(60))
        val output = mutableListOf(Native950Packets.openTop(ROOT))
        if (assertModernMode) {
            // UI-DECISIONS.md: modern combat, modern interfaces and modern skin.
            // Emit before the components whose onLoad hooks inspect these mode bits.
            output += Native950Packets.varbitSmall(27168, 0)
            output += Native950Packets.varbitSmall(27169, 0)
            output += Native950Packets.varbitSmall(22875, 0)
            // This server enters the ordinary world directly. Native 15532(0)
            // otherwise treats zero-valued onboarding state as an active tutorial
            // and rejects the ribbon's own custom-mode checkbox (2755).
            output += Native950Packets.varbitSmall(39917, 98)
            output += Native950Packets.varbitSmall(49044, 100)
            output += Native950Packets.varbitSmall(60098, 1)
        }
        for (panel in opened) {
            val slot = requireNotNull(slots[panel.slot]) { "Missing native interface slot ${panel.slot}" }
            requireRoot(slot.attach, "slot ${panel.slot} attachment")
            requireRoot(slot.wrapper, "slot ${panel.slot} wrapper")
            retained += slot.wrapper
            output += Native950Packets.openSub(ROOT, slot.attach and 65535, panel.interfaceId, true)
            if (forceOpenPanels && panel.canForceShow) {
                output += Native950Packets.hideInterface(ROOT, slot.wrapper and 65535, false)
            }
        }

        val hidden = mutableSetOf<Int>()
        for (key in hiddenSlotKeys) {
            val slot = requireNotNull(slots[key]) { "Missing native hidden interface slot $key" }
            requireRoot(slot.wrapper, "slot $key hidden wrapper")
            if (slot.wrapper !in retained && hidden.add(slot.wrapper)) {
                output += Native950Packets.hideInterface(ROOT, slot.wrapper and 65535, true)
            }
        }

        // Script8682 resolves the backpack actor layer; script2410 puts Drop at operation8.
        // Selection also needs source target bits11..17 and target component bit22.
        output += Native950Packets.interfaceEvents(Native950InventoryMenu.INTERFACE,
            Native950InventoryMenu.ITEMS_COMPONENT, 0, 27, Native950InventoryMenu.EVENT_MASK)
        if (assertModernMode) {
            // The native run orb supports Toggle Run (operation 1). Rest is not implemented.
            output += Native950Packets.interfaceEvents(1465, 15, -1, -1, 2)
            output += Native950RunOrb.initialization()
        }
        // These are required even when no optional HUD panel is open.
        output += Native950Packets.varbitSmall(18797, 1)
        output += Native950Packets.runClientScript(1362, 18)
        output += Native950Packets.runClientScript(8491, 18)
        return output
    }

    private fun hash(component: Int): Int = (ROOT shl 16) or component

    private fun requireRoot(value: Int, what: String) {
        require(value >= 0 && value ushr 16 == ROOT && value and 65535 != 65535) {
            "Unverified native $what: $value"
        }
    }
}
