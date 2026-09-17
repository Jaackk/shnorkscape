package com.rs.game.player.client;

import com.rs.game.WorldObject;

/** The loc-shape rule shared by the cache-placed scenery the skills harvest. */
final class Native950Scenery {
    private Native950Scenery() { }

    /**
     * Loc shapes 10 and 11 are both "centrepiece" scenery: the object owns its tile, and 11
     * differs only in being drawn diagonally. The cache uses them interchangeably for the same
     * object id - ids 113140..113160, the Varrock-area rocks, are placed 130 times as shape 11
     * against 73 times as shape 10, and tree 1308 is shape 11 everywhere it appears.
     *
     * <p>Accepting only 10 therefore refused roughly two thirds of rocks, per placement rather
     * than per id, so a "Copper rock" would work in one spot and answer "That object action is
     * not available yet" in another. Shape is a rendering detail here and must not decide whether
     * something is harvestable; the name and options checks do that.
     */
    static boolean isHarvestableShape(WorldObject object) {
        return object != null && (object.getType() == 10 || object.getType() == 11);
    }
}
