package com.rs.game.activites.dnd.eviltree;

import com.rs.game.WorldTile;
import com.rs.utils.Utils;

/**
 * An enumerated type describing all evil tree locations.
 *
 * @author lare96 <http://github.com/lare96>
 */
public enum EvilTreeLocation {
    SEERS_VILLAGE("close to a collection of yew trees", new WorldTile(2757, 3422, 0)),
    TREE_GNOME_VILLAGE("to the south of a tree gnome settlement", new WorldTile(2520, 3104, 0)),
    SEERS_VILLAGE_2("north as the crow flies from Seers' Village", new WorldTile(2705, 3506, 0)),
    CASTLE_WARS("in a location with rare trees and ogres nearby", new WorldTile(2375, 3052, 0)),
    DRAYNOR_VILLAGE("due west of the town you call lumbridge", new WorldTile(3097, 3226, 0)),
    LEGENDS_GUILD("close to the home of 'Legends'", new WorldTile(2737, 3329, 0));

    public final String description;
    public final String formattedDescription;
    public final WorldTile treeTile;
    public final WorldTile hunterTile;
    public final WorldTile weedsTile;

    EvilTreeLocation(String description, WorldTile treeTile) {
        this.description = description;
        this.treeTile = treeTile;
        formattedDescription = Utils.capitalize(description) + ".";
        hunterTile = treeTile.transform(6, 3, 0);
        weedsTile = hunterTile.transform(0, -2, 0);
    }

    public static EvilTreeLocation getRandom() {
   return Utils.randomFrom(values());
    }
}