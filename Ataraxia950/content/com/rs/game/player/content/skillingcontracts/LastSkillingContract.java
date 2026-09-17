package com.rs.game.player.content.skillingcontracts;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@AllArgsConstructor
@Getter
public final class LastSkillingContract implements Serializable {
    private static final long serialVersionUID = 4779215652599207781L;
    private final int skillId;
    private final int contractId;

    public boolean matches(AssignedSkillingContract current) {
        return current.skillId == skillId && current.contractId == contractId;
    }
}
