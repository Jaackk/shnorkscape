package com.rs.game.player.client;

import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.item.floor.FloorItem;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.content.ItemConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The native transport seam over the existing NPC tables and World floor-item lifecycle. */
public final class Native950Loot {
    private Native950Loot() { }
    public static final int PRIVATE_SECONDS = 60;
    public static final int PUBLIC_SECONDS = 60;

    /** Invoked once on NPC death by the world owner, with the existing damage-credit winner. */
    public static List<FloorItem> createDeathDrops(NPC npc, Player owner) {
        List<Item> selected = Native950NpcDrops.roll(npc, owner);
        if (selected.isEmpty()) return Collections.emptyList();
        WorldTile tile = new WorldTile(npc.getCoordFaceX(npc.getSize()), npc.getCoordFaceY(npc.getSize()), npc.getPlane());
        List<FloorItem> created = new ArrayList<FloorItem>(selected.size());
        for (Item item : selected) {
            // This exact legacy rule is read only after cache/bootstrap initialization. GIM's
            // referenced managers have inert constructors; no start/load/service method runs.
            boolean transferable = ItemConstants.isTradeable(item);
            FloorItem floor = World.addGroundItem(item, tile, owner, true, PRIVATE_SECONDS, 2, PUBLIC_SECONDS, false);
            if (floor != null) { floor.setPublicTransferAllowed(transferable); created.add(floor); }
        }
        return Collections.unmodifiableList(created);
    }
}
