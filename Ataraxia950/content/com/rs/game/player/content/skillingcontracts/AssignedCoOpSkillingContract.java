package com.rs.game.player.content.skillingcontracts;

import java.io.Serializable;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class AssignedCoOpSkillingContract implements Serializable {

    private static final long serialVersionUID = 6370278339461078534L;
    public final String partnerName;
    public final boolean advanced;
    public final boolean isShort;

    public AssignedCoOpSkillingContract(String partnerName, boolean advanced, boolean isShort) {
        this.partnerName = partnerName;
        this.advanced = advanced;
        this.isShort = isShort;
    }
}