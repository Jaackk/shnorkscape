package com.rs.game.player;

import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.player.actions.slayer.sophanemdungeon.SophanemSlayerDungeon;
import com.rs.game.player.content.Magic;
import com.rs.game.player.content.RouteEvent;
import com.rs.game.player.controllers.GodWars;
import com.rs.game.player.controllers.bossInstance.gwd2.HeartOfGielinorController;
import lombok.val;

import java.util.HashMap;

/**
 * @author Xenthium/Toby.
 */

public class DiamondZonePortalHandler {

    public static boolean processObjectClick(final Player player, final WorldObject object) {
        if (!Portal.getIds().containsKey(object.getId())) {
            return false;
        }
        if (player.getRegionId() != 8261) {
            return false; // The portal objects used for these exist elsewhere in the game, causing issues.
        }
        player.stopAll(true);
        player.setRouteEvent(new RouteEvent(object, () -> {
            val portal = getPortalById(object.getId());
            player.faceObject(object);
            Magic.sendDiamondZonePortalTeleport(player, portal.getWorldTile(), portal.getController());
        }, false));
        return true;
    }

    private static Portal getPortalById(final int objectId) {
        return Portal.getIds().get(objectId);
    }

    /**
     * @Jaedmo if you ever need to add more it goes as follows.
     *
     * The portal's ID,
     * The required controller for the destination (can be null if one isn't required),
     * The portal's destination.
     */
    enum Portal {
        LEGIO_SEXTUS(114199, null, new WorldTile(1183, 624, 1)),
        LEGIO_PRIMUS(114194, null, new WorldTile(1026, 632, 1)),
        LEGIO_TERTIUS(114196, null, new WorldTile(1099, 666, 1)),
        LEGIO_QUARTUS(114197, null, new WorldTile(1177, 634, 1)),
        LEGIO_QUINTUS(114198, null, new WorldTile(1190, 634, 1)),
        LEGIO_SECUNDUS(114195, null, new WorldTile(1106, 670, 1)),
        NEX(114193, GodWars.class.getSimpleName(), new WorldTile(2904, 5203, 0)),
        BANDOS(114191, GodWars.class.getSimpleName(), new WorldTile(2860, 5357, 0)),
        ARMADYL(114192, GodWars.class.getSimpleName(), new WorldTile(2835, 5292, 0)),
        ZAMORAK(114189, GodWars.class.getSimpleName(), new WorldTile(2925, 5335, 0)),
        SARADOMIN(114190, GodWars.class.getSimpleName(), new WorldTile(2923, 5259, 0)),
        TELOS(114205, HeartOfGielinorController.class.getSimpleName(), new WorldTile(3200, 6961, 1)),
        HELWYR(114202, HeartOfGielinorController.class.getSimpleName(), new WorldTile(3278, 6897, 1)),
        VINDICTA(114201, HeartOfGielinorController.class.getSimpleName(), new WorldTile(3113, 6897, 1)),
        GREGOROVIC(114203, HeartOfGielinorController.class.getSimpleName(), new WorldTile(3286, 7062, 1)),
        TWIN_FURIES(114204, HeartOfGielinorController.class.getSimpleName(), new WorldTile(3115, 7060, 1)),
        THE_MAGISTER(114200, SophanemSlayerDungeon.class.getSimpleName(), new WorldTile(2464, 6729, 1));
        
        private static final HashMap<Integer, Portal> PORTAL_IDS = new HashMap<>();

        static {
            for (Portal portal : values()) {
                PORTAL_IDS.put(portal.id, portal);
            }
        }

        private final int id;
        private final String controller;
        private final WorldTile worldTile;

        Portal(int id, String controller, WorldTile worldtile) {
            this.id = id;
            this.controller = controller;
            this.worldTile = worldtile;
        }

        private static HashMap<Integer, Portal> getIds() {
            return PORTAL_IDS;
        }

        private int getId() {
            return id;
        }

        private WorldTile getWorldTile() {
            return worldTile;
        }

        private String getController() {
            return controller;
        }

    }

}
