package com.rs.game.player.content.skillingcontracts;

import com.google.common.collect.ImmutableList;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.utils.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author lare96 <http://github.com/lare96>
 */
public abstract class ContractList {

    public final boolean higherRate;
    public final int skillId;
    public int maxLevel;
    /**
     * Never call this field directly, use {@link #getContracts()} instead.
     */
    private ImmutableList<SkillingContract> contracts;

    public ContractList(int skillId, boolean higherRate) {
        this.skillId = skillId;
        this.higherRate = higherRate;
    }

    protected abstract ImmutableList<SkillingContract> buildContracts();

    private void validateContracts(ImmutableList<SkillingContract> list) {
        for (int index = 0; index < list.size(); index++) {
            SkillingContract next = list.get(index);
            if (next.id != index) {
                throw new IllegalStateException("Contracts must be ordered by index!");
            }
        }
    }

    public final SkillingContract generate(Player player, boolean advanced) {
        int level = player.getSkills().getLevelForXp(skillId);
        List<SkillingContract> possible = new ArrayList<>();
        for (SkillingContract next : getContracts()) {
            if (player.getContracts().getBlocks().containsEntry(skillId, next.id)) {
                continue;
            }
            if (!next.canAssign(player)) {
                continue;
            }
            if (advanced && !next.advanced) {
                continue;
            }

            if (level >= next.levelRequired) {
                possible.add(next);
            }
        }

        int size = possible.size();
        if (size == 0) {
            throw new IllegalStateException("No task generated: " + level);
        } else if (size == 1) {
            return possible.get(0);
        } else {
            int totalRate = 0;
            for (SkillingContract next : possible) {
                totalRate += getAssignChance(next, advanced);
            }
            Collections.shuffle(possible);
            int roll = ThreadLocalRandom.current().nextInt(totalRate) + 1;
            int mod = 0;
            for (SkillingContract next : possible) {
                mod += getAssignChance(next, advanced);
                if (roll <= mod) {
                    return next;
                }
            }
            throw new IllegalStateException("Error ~ this code block should not be reachable!");
        }
    }

    private int getAssignChance(SkillingContract contract, boolean advanced) {
        int levelRequired = contract.levelRequired;
        int chance = levelRequired;
        if (advanced) {
            if (levelRequired < 30) {
                chance /= 2;
            }
        } else {
            if (levelRequired < 10) {
                chance = 10;
            }
        }
        if (chance <= 0)
            chance = 1;
        return chance;
    }

    public final SkillingContract generateCoOp(Player player, Player other, boolean advanced, boolean countBlocks) {
        List<SkillingContract> possible = new ArrayList<>();
        for (SkillingContract next : getContracts()) {
            if (!next.canAssign(player) || !next.canAssign(other)) {
                continue;
            }
            if (advanced && !next.advanced) {
                continue;
            }
            if (countBlocks) {
                if (other.getContracts().getBlocks().containsEntry(skillId, next.id) ||
                        player.getContracts().getBlocks().containsEntry(skillId, next.id)) {
                    continue;
                }
            }
            if (player.getSkills().getLevelForXp(skillId) >= next.levelRequired &&
                    other.getSkills().getLevelForXp(skillId) >= next.levelRequired) {
                possible.add(next);
            }
        }
        int size = possible.size();
        if (size == 0) {
            throw new IllegalStateException("No task generated.");
        } else if (size == 1) {
            return possible.get(0);
        } else {
            int totalRate = 0;
            for (SkillingContract next : possible) {
                totalRate += getAssignChance(next, advanced);
            }
            Collections.shuffle(possible);
            int roll = ThreadLocalRandom.current().nextInt(totalRate) + 1;
            int mod = 0;
            for (SkillingContract next : possible) {
                mod += getAssignChance(next, advanced);
                if (roll <= mod) {
                    return next;
                }
            }
            throw new IllegalStateException("Error ~ this code block should not be reachable!");
        }

    }

    public boolean canGenerate(Player player, boolean advanced) {
        int level = player.getSkills().getLevelForXp(skillId);
        for (SkillingContract next : getContracts()) {
            if (player.getContracts().getBlocks().containsEntry(skillId, next.id)) {
                continue;
            }
            if (!next.canAssign(player)) {
                continue;
            }
            if (advanced && !next.advanced) {
                continue;
            }

            if (level >= next.levelRequired) {
                return true;
            }
        }
        return false;
    }

    public boolean canGenerateCoOp(Player player, Player other, boolean advanced, boolean countBlocks) {
        for (SkillingContract next : getContracts()) {
            if (!next.canAssign(player) || !next.canAssign(other)) {
                continue;
            }
            if (advanced && !next.advanced) {
                continue;
            }
            if (countBlocks) {
                if (other.getContracts().getBlocks().containsEntry(skillId, next.id) ||
                        player.getContracts().getBlocks().containsEntry(skillId, next.id)) {
                    continue;
                }
            }
            if (player.getSkills().getLevelForXp(skillId) >= next.levelRequired &&
                    other.getSkills().getLevelForXp(skillId) >= next.levelRequired) {
                return true;
            }
        }
        return false;
    }

    public final int getRate(Player player) {
        // Values must return the exact same results within milliseconds of subsequent calls!
        // There can't be any form of randomness here, or errors will be thrown.
        if (player.getContracts().totalContracts < 10 && (skillId == Skills.WOODCUTTING ||
                skillId == Skills.RUNECRAFTING ||
                skillId == Skills.FISHING ||
                skillId == Skills.MINING ||
                skillId == Skills.AGILITY ||
                skillId == Skills.THIEVING)) {
            return 10;
        }
        if (player.getSkills().getXp(skillId) >= 1_000_000_000) {
            return 1;
        }
        if (skillId == Skills.FARMING) {
            return 3;
        }
        return higherRate ? 5 : 4;
    }

    public final int getCoOpRate(Player player, Player other) {
        // Values must return the exact same results within milliseconds of subsequent calls!
        // There can't be any form of randomness here, or errors will be thrown.
        if (player.getSkills().getXp(skillId) >= 1_000_000_000 || other.getSkills().getXp(skillId) >= 1_000_000_000) {
            return 1;
        }
        if (skillId == Skills.FARMING) {
            return 3;
        }
        return higherRate ? 5 : 4;
    }

    public final SkillingContract lookup(int contractId) {
        try {
            return getContracts().get(contractId);
        } catch (IndexOutOfBoundsException e) {
            Logger.getGlobal().info("Invalid contract id " + contractId + " in list " + skillId + ".");
            return null;
        }
    }

    public List<SkillingContract> getDisplayableContracts(boolean advanced) {
        if (advanced) {
            List<SkillingContract> list = new ArrayList<>(getContracts());
            list.removeIf(contract -> !contract.advanced);
            return list;
        }
        return getContracts();
    }

    public ImmutableList<SkillingContract> getContracts() {
        if (contracts == null) {
            ImmutableList<SkillingContract> allContracts = buildContracts();
            validateContracts(allContracts);
            contracts = allContracts;
        }
        return contracts;

    }

}