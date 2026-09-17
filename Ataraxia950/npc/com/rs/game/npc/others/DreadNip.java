package com.rs.game.npc.others;

import com.rs.game.ForceTalk;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

@SuppressWarnings("serial")
public class DreadNip extends NPC {

    public static final String[] DREADNIP_MESSAGES = { "Your dreadnip couldn't attack so it left.", "The dreadnip gave up as you were too far away.", "Your dreadnip served its purpose and fled." };

    private final Player owner;
    private int ticks;
    private int timesAttacked;

    public DreadNip(Player owner, int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
        super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
        this.owner = owner;
        setIntelligentRouteFinder(true);
        if (id == 20458)
            setNextForceTalk(new ForceTalk("Zrrrtk!"));
        else if (id == 20459)
            setNextForceTalk(new ForceTalk("Pssstk!"));
        else if (id == 20460)
            setNextForceTalk(new ForceTalk("Krrrrrtk!"));
        if (owner.getPerkManager().hasPerkActive(DonationPerk.DOMINION_DOMINATION)) {
            setAttackBonuses(600);
            setRangedBonuses(600);
            setMagicBonuses(600);
        }
    }

    private void finish(int index) {
        if (index != -1) {
            owner.getPackets().sendGameMessage(DREADNIP_MESSAGES[index]);
            owner.getTemporaryAttributtes().remove("hasDN");
        }
        this.finish();
    }

    public Player getOwner() {
        return owner;
    }

    public int getTicks() {
        return ticks;
    }

    @Override
    public void processNPC() {
        super.processNPC();
        if (owner == null || owner.hasFinished()) {
            finish(-1);
            return;
        } else if (getCombat().getTarget() == null || getCombat().getTarget().isDead()) {
            finish(2);
            return;
        } else if (Utils.getDistance(owner, this) >= 14) {
            finish(1);
            return;
        } else if (ticks++ == 75 || timesAttacked == 18) {
            finish(2);
            return;
        }
    }

    public int getTimesAttacked() {
        return timesAttacked;
    }

    public void setTimesAttacked(int timesAttacked) {
        this.timesAttacked = timesAttacked;
    }

    @Override
    public boolean canMove(int dir) {
        return true;
    }
    
    @Override
    public boolean canWalkNPC(int toX, int toY, boolean checkUnder) {
        return true;
    }
    
}
