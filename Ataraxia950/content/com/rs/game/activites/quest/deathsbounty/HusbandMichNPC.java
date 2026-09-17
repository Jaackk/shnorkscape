package com.rs.game.activites.quest.deathsbounty;

import com.rs.game.npc.NPC;

public class HusbandMichNPC extends NPC {
    private final SoulFightInstanceController controller;
    private final SoulFightInstance instance;
    public HusbandMichNPC(SoulFightInstanceController controller) {
        super(DeathsBounty.HUSBAND_MICH_ID, controller.getInstance().getTile(3419, 5277, 0), -1, true);
        this.controller = controller;
        instance = (SoulFightInstance) controller.getInstance();
        setRandomWalk(0);
    }
}
