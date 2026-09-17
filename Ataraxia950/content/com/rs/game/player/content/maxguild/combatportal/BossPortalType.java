package com.rs.game.player.content.maxguild.combatportal;

import com.rs.game.WorldTile;
import com.rs.game.player.actions.slayer.sophanemdungeon.SophanemSlayerDungeon;
import com.rs.game.player.controllers.GodWars;
import com.rs.game.player.controllers.bossInstance.gwd2.HeartOfGielinorController;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * @author Xenthium.
 */

@AllArgsConstructor
public enum BossPortalType {
    UNDIRECTED(92256, -1, -1, null, null),
    UNDIRECTED1(92257, -1, -1, null, null),
    BORK(92258, 1, 38, null, new WorldTile(3143, 5545, 0)),
    GLACORS(92259, 1, 46, null, new WorldTile(4181, 5726, 0)),
    TORMENTED_DEMONS(92260, 1, 54, null, new WorldTile(2571, 5735, 0)),
    AIRUTS(92261, 1, 62, null, new WorldTile(2280, 3617, 0)),
    GIANT_MOLE(92262, 1, 70, null, new WorldTile(2985, 3381, 0)),
    BARROWS(92263, 1, 78, null, new WorldTile(3564, 3289, 0)),
    DAGANNOTH_KINGS(92264, 1, 86, null, new WorldTile(1912, 4367, 0)),
    CORPOREAL_BEAST(92265, 1, 94, null, new WorldTile(2968, 4384, 2)),
    KING_BLACK_DRAGON(92266, 2, 38, null, new WorldTile(3052, 3517, 0)),
    QUEEN_BLACK_DRAGON(92267, 2, 46, null, new WorldTile(1195, 6499, 0)),
    KALPHITE_QUEEN(92268, 2, 54, null, new WorldTile(3479, 9488, 0)),
    KALPHITE_KING(92269, 2, 62, null, new WorldTile(2973, 1655, 0)),
    COMMANDER_ZILYANA(92270, 2, 70, GodWars.class.getSimpleName(), new WorldTile(2923, 5259, 0)),
    GENERAL_GRAARDOR(92271, 2, 78, GodWars.class.getSimpleName(), new WorldTile(2860, 5357, 0)),
    KREE_ARRA(92272, 2, 86, GodWars.class.getSimpleName(), new WorldTile(2835, 5292, 0)),
    KRIL_TSUTSAROTH(92273, 2, 94, GodWars.class.getSimpleName(), new WorldTile(2925, 5335, 0)),
    NEX(92274, 3, 38, GodWars.class.getSimpleName(), new WorldTile(2904, 5203, 0)),
    LEGIONES(92275, 3, 46, null, new WorldTile(1109, 592, 1)),
    ARAXXOR(92276, 3, 54, null, new WorldTile(4512, 6289, 1)),
    VORAGO(92277, 3, 62, null, new WorldTile(2972, 3430, 0)),
    GREGOROVIC(101720, 3, 70, HeartOfGielinorController.class.getSimpleName(), new WorldTile(3286, 7062, 1)),
    HELWYR(101721, 3, 78, HeartOfGielinorController.class.getSimpleName(), new WorldTile(3278, 6897, 1)),
    TWIN_FURIES(101722, 3, 86, HeartOfGielinorController.class.getSimpleName(), new WorldTile(3115, 7060, 1)),
    VINDICTA_AND_GORVEK(101723, 3, 94, HeartOfGielinorController.class.getSimpleName(), new WorldTile(3113, 6897, 1)),
    TELOS(103489, 4, 38, HeartOfGielinorController.class.getSimpleName(), new WorldTile(3200, 6961, 1)),
    THE_MAGISTER(109485, 4, 46, SophanemSlayerDungeon.class.getSimpleName(), new WorldTile(2464, 6729, 1));
    //104719 ??

    private static final HashMap<Integer, BossPortalType> PORTAL_FOR_ID = new HashMap<>();

    static {
        for (BossPortalType portal : values()) {
            PORTAL_FOR_ID.put(portal.getObjectId(), portal);
        }
    }

    @Getter
    public final int objectId;
    @Getter
    public final int interfacePage;
    @Getter
    public final int interfaceTextComponentId;
    @Getter
    public final String controller;
    @Getter
    public final WorldTile destinationTile;

    public static HashMap<Integer, BossPortalType> getPortalForId() {
        return PORTAL_FOR_ID;
    }

    public static int getLastPage() {
        return Collections.max(Arrays.stream(values()).map(BossPortalType::getInterfacePage).collect(Collectors.toList()));
    }

    public int getInterfaceButtonComponentId() {
        return interfaceTextComponentId - 3;
    }

}
