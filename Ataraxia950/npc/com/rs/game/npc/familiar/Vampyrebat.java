package com.rs.game.npc.familiar;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.actions.summoning.Summoning.Pouches;

public class Vampyrebat extends Familiar {

    /**
     *
     */
    private static final long serialVersionUID = 586089784797828590L;

    public Vampyrebat(Player owner, Pouches pouch, WorldTile tile,
                      int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
        super(owner, pouch, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
    }

    @Override
    public int getBOBSize() {
        return 0;
    }

    @Override
    public int getSpecialAmount() {
        return 4;
    }

    @Override
    public SpecialAttack getSpecialAttack() {
        return SpecialAttack.ENTITY;
    }

    @Override
    public String getSpecialDescription() {
        return "Deals damage to your opponents, with a maximum hit of 120. It also has a chance of healing your lifepoints by 20. ";
    }

    @Override
    public String getSpecialName() {
        return "Vampyre Touch";
    }

    @Override
    public boolean submitSpecial(Object object) {
        return false;
    }
}
