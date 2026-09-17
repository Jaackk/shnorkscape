package com.rs.game.player.content.skillingcontracts;

import com.rs.game.player.dialogue.Dialogue;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class AssignContractD extends Dialogue {
    boolean highLevel;
    int npc;

    @Override
    public void start() {
        highLevel = (boolean) parameters[0];
        npc = (int) parameters[1];
        if (player.getContracts().current != null) {
            sendNPCDialogue(npc, Dialogue.ANGRY, "You already have a contract:", player.getContracts().getContractDescription().toLowerCase());
        } else {
            if (player.getContracts().assignContract(highLevel)) {
                sendNPCDialogue(npc, Dialogue.ANGRY, player.getContracts().getContractDescription());
            } else {
                sendNPCDialogue(npc, Dialogue.ANGRY, "I can't assign you any contracts! Train your skills a bit more.");
            }
        }
        stage = 0;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 0:
                end();
                break;
        }
    }

    @Override
    public void finish() {

    }
}