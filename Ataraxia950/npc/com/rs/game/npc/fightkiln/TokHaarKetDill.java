package com.rs.game.npc.fightkiln;

import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.FightKiln;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("serial")
public class TokHaarKetDill extends FightKilnNPC {

    private int receivedHits;

    public TokHaarKetDill(int id, WorldTile tile, FightKiln controler) {
        super(id, tile, controler);
    }

    @Override
    public void handleIngoingHit(final Hit hit) {
        if (hit.getLook() != HitLook.MELEE_DAMAGE && hit.getLook() != HitLook.RANGE_DAMAGE && hit.getLook() != HitLook.MAGIC_DAMAGE)
            return;
        if (receivedHits != -1) {
            Entity source = hit.getSource();
            if (source == null || !(source instanceof Player))
                return;
            hit.setDamage(0);
            Player playerSource = (Player) source;
            if (playerHasPickaxe(playerSource)) {
                receivedHits++;
            }
            if ((playerHasPickaxe(playerSource) && receivedHits >= 5) || (playerHasPickaxe(playerSource) && receivedHits >= 3)) {
                receivedHits = -1;
                transformIntoNPC(getId() + 1);
                playerSource.sendMessage("Your pickaxe breaks the TokHaar-Ket-Dill's thick armour!");
            } else {
                playerSource.sendMessage("Your pickaxe slowly cracks its way through the TokHaar-Ket-Dill's armour.");
            }
        }
        super.handlePrayers(hit);
    }

    public boolean playerHasPickaxe(Player player) {
        List<Integer> pickaxeIds = new ArrayList<>(Arrays.asList(1275, 1265, 1267, 1269, 1271, 1273, 13661, 15259));
        int weaponId = player.getEquipment().getWeaponId();

        if (pickaxeIds.contains(weaponId)) {
            return true;
        }
        for (int id : pickaxeIds) {
            Item pickaxe = new Item(id);
            if (player.getInventory().containsItem(pickaxe) || player.getToolBelt().contains(id)) {
                return true;
            }
        }
        return false;
    }
}