package com.opennxt.net.login

import com.opennxt.model.lobby.TODORefactorThisClass
import com.rs.utils.ILayoutDefaults
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Native950ServerpermVarcsTest {
    @Test
    fun `native 950 login retains non-layout serverperm values and removes only legacy workspace defaults`() {
        val values = Int2IntOpenHashMap()
        TODORefactorThisClass.populateServerpermVarcs(values)
        val original = values.toMap()

        val removed = Native950ServerpermVarcs.removeLegacyWorkspaceDefaults(values)
        val expected = original.keys.intersect(ILayoutDefaults.INTERFACE_LAYOUT_VARS.keys)

        assertEquals(expected, removed)
        assertEquals(92, removed.size, "The inspected native login blob has 92 legacy workspace entries")
        assertTrue(values.keys.none(Native950ServerpermVarcs::isLegacyWorkspaceKey))
        assertEquals(original.filterKeys { it !in expected }, values.toMap())
        assertEquals(123, values.size)

        // Representative non-layout values remain available to the native login bootstrap.
        assertEquals(original.getValue(5139), values.get(5139))
        assertEquals(original.getValue(6119), values.get(6119))
    }

    @Test
    fun `legacy sessions retain the original full serverperm bootstrap when no native filter is applied`() {
        val values = Int2IntOpenHashMap()
        TODORefactorThisClass.populateServerpermVarcs(values)

        assertEquals(215, values.size)
        assertTrue(values.keys.any(Native950ServerpermVarcs::isLegacyWorkspaceKey))
        assertFalse(values.isEmpty)
    }
}
