package com.rs.game.item.floor;

import com.rs.game.player.Player;
import com.rs.game.player.content.grandExchange.GrandExchange;
import com.rs.utils.Colors;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;

public class LootBeamManager implements Serializable {

    private static final long serialVersionUID = -6454356751078830705L;

    private final HashMap<LootBeamType, Boolean> unlockedLootBeamTypes;
    @Getter
    private boolean lootBeamsEnabled;
    @Getter @Setter
    private LootBeamType currentLootBeamType;
    @Getter @Setter
    private int lootBeamMinimumValue;

    public LootBeamManager() {
        unlockedLootBeamTypes = new HashMap<>();
        unlockLootBeam(LootBeamType.NORMAL);
        currentLootBeamType = LootBeamType.NORMAL;
    }

    public boolean hasUnlockedLootBeam(LootBeamType lootBeamType) {
        return unlockedLootBeamTypes.getOrDefault(lootBeamType, false);
    }

    public void unlockLootBeam(LootBeamType lootBeamType) {
        unlockedLootBeamTypes.put(lootBeamType, true);
    }

    public void toggleLootBeam() {
        lootBeamsEnabled = !lootBeamsEnabled;
    }

    public boolean isViableFloorItem(int itemId) {
        return GrandExchange.getPrice(itemId) >= lootBeamMinimumValue && lootBeamsEnabled;
    }

    public static boolean handleItemOption(Player player, int itemId) {
        for (LootBeamType lootBeamType : LootBeamType.VALUES) {
            if (lootBeamType.getItemId() == itemId) {
                if (!player.getLootBeamManager().hasUnlockedLootBeam(lootBeamType)) {
                    player.getLootBeamManager().unlockLootBeam(lootBeamType);
                    player.getInventory().deleteItem(itemId, 1);
                    player.sendMessage("You have gained the power to shine a new beacon of light on your drops.");
                } else {
                    player.sendMessage("You don't need to unlock two of these!");
                }
                return true;
            }
        }
        return false;
    }

    public static void sendLootBeamMessage(Player player, LootBeamType lootBeamType) {
        player.sendMessage(Colors.ORANGE + "<shad=000000>" + lootBeamType.getMessage());
    }
}
