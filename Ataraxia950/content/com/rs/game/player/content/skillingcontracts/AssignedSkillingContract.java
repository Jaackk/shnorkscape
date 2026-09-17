package com.rs.game.player.content.skillingcontracts;

import com.rs.game.player.Player;

import java.io.Serializable;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class AssignedSkillingContract implements Serializable {
    private static final long serialVersionUID = -5604626726495930038L;
    public final int skillId;
    public final int contractId;
    public final int actionsNeeded;
    public int currentActions;

    public AssignedSkillingContract(Player player, int skillId, SkillingContract contract) {
        this.skillId = skillId;
        contractId = contract.id;
        actionsNeeded = contract.generateActions(player);
    }

    public AssignedSkillingContract(int skillId, SkillingContract contract, int actionsNeeded) {
        this.skillId  =skillId;
        this.actionsNeeded = actionsNeeded;
        contractId = contract.id;
    }

    public boolean same(AssignedSkillingContract other) {
        return skillId == other.skillId && contractId == other.contractId;
    }

    public int getActionsLeft() {
        return actionsNeeded - currentActions;
    }

    public ContractList getContractList() {
        return SkillingContractManager.ALL.get(skillId);
    }

    public SkillingContract getContract() {
        return getContractList().lookup(contractId);
    }
}