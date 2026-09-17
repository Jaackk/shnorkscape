package com.rs.game.player.content.jujupotions.vineherbpatch;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author lare96 <http://github.com/lare96>
 */
@AllArgsConstructor
@Getter
public enum VineHerbPatchState {
    WEEDED(56700),
    CLEAR(56697),
    SEEDED(56701),
    GROWING_1(56702),
    GROWING_2(56703),
    GROWING_3(56704),
    GROWN(56705);

    private final int objectId;
}