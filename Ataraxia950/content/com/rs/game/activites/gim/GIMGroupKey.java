package com.rs.game.activites.gim;

import java.io.Serializable;

/**
 * A class representing data that can be used to access GIM group information.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class GIMGroupKey implements Serializable {

    /**
     * The serial version UID.
     */
    private static final long serialVersionUID = 5477071111832168395L;

    /**
     * The SQL group ID.
     */
    private final int groupId;

    /**
     * The group key.
     */
    private final String groupKey;

    /**
     * Creates a new {@link GIMGroupKey}.
     */
    public GIMGroupKey(int groupId, String groupKey) {
        this.groupId = groupId;
        this.groupKey = groupKey;
    }

    public GIMGroupKey withNewKey(String newGroupKey) {
        return new GIMGroupKey(groupId, newGroupKey);
    }
    public GIMGroupKey withNewId(int newGroupId) {
        return new GIMGroupKey(newGroupId, groupKey);
    }

    public int getGroupId() {
        return groupId;
    }

    public String getGroupKey() {
        return groupKey;
    }
}
