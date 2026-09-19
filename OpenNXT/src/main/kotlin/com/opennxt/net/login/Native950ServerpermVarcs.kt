package com.opennxt.net.login

import com.rs.utils.ILayoutDefaults

/**
 * Prevents the legacy workspace seed from replacing the native client's saved layout.
 *
 * The login service still provides its complete server-permanent variable bootstrap.  A
 * native 950 session is the sole exception: the entries shared with the legacy interface
 * layout map describe positions, dimensions, tabs, and visibility, so the client must keep
 * its own persisted values for them.
 */
internal object Native950ServerpermVarcs {
    private val legacyWorkspaceKeys: Set<Int> = ILayoutDefaults.INTERFACE_LAYOUT_VARS.keys.toSet()

    internal fun removeLegacyWorkspaceDefaults(values: MutableMap<Int, Int>): Set<Int> {
        val removed = values.keys.filterTo(linkedSetOf()) { it in legacyWorkspaceKeys }
        removed.forEach(values::remove)
        return removed
    }

    internal fun isLegacyWorkspaceKey(id: Int): Boolean = id in legacyWorkspaceKeys
}
