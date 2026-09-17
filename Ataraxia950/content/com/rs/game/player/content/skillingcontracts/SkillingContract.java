package com.rs.game.player.content.skillingcontracts;

import com.rs.Settings;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.skillingcontracts.impl.RunecraftingContractList;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author lare96 <http://github.com/lare96>
 */
public class SkillingContract {

    public final int id;
    public final String description;
    public final int levelRequired;
    public final int minimumActions;
    public final int maximumActions;
    public final int bonusExp;
    public final boolean advanced;

    public SkillingContract(int id, String description, int levelRequired, int minimumActions, int maximumActions) {
        this(id, description, levelRequired, minimumActions, maximumActions, levelRequired >= 70);
    }
    public SkillingContract(int id, String description, int levelRequired, RunecraftingContractList.CraftAmount amount) {
        this(id, description, levelRequired, amount.getMin(), amount.getMax(), levelRequired >= 70);
    }
    public SkillingContract(int id, String description, int levelRequired, int minimumActions, int maximumActions, boolean advanced) {
        this.id = id;
        this.description = description;
        this.levelRequired = levelRequired;
        this.minimumActions = minimumActions;
        this.maximumActions = maximumActions;
        this.advanced = advanced;
        bonusExp = levelRequired * SkillingContractManager.BONUS_XP_MULTIPLIER;
    }

    public boolean canAssign(Player player) {
        return true;
    }

    public int generateActions(Player player) {
        if (minimumActions == 1 && maximumActions == 1)
            return 1;
        int amount = ThreadLocalRandom.current().nextInt(minimumActions, maximumActions + 1);
        if (player.getContracts().getEffects().contains(TempContractEffect.REDUCE_TASK_AMOUNT)) {
            amount /= 2;
        }
        if (amount <= 1) {
            amount = 2;
        }
        return amount;
    }

    public int generateActionsCoOp(Player player, Player other, CoOpRequest request, int skillId) {
        if (minimumActions == 1 && maximumActions == 1)
            return 1;
        int min = minimumActions;
        int otherMin = minimumActions;
        if (player.getContracts().getEffects().contains(TempContractEffect.REDUCE_TASK_AMOUNT)) {
            min /= 2;
        }
        if (other.getContracts().getEffects().contains(TempContractEffect.REDUCE_TASK_AMOUNT)) {
            otherMin /= 2;
        }
        if (min < 1) {
            min = 1;
        }
        if (otherMin < 1) {
            otherMin = 1;
        }
        int playerAmount = ThreadLocalRandom.current().nextInt(min, maximumActions + 1);
        int otherAmount = ThreadLocalRandom.current().nextInt(otherMin, maximumActions + 1);
        int totalAmount = playerAmount + otherAmount;
        if (totalAmount < 20) {
            totalAmount = 20;
        }
        if (skillId == Skills.DIVINATION && totalAmount > 300) {
            totalAmount = 300;
        }
        if (Settings.TEST_SERVER_MODE) {
            String str = "DEBUG: amount(plr: " + playerAmount + ", other: " + otherAmount + ", max: " + maximumActions + ", total: " + totalAmount + ")";
            player.getContracts().coOpMessage(str);
            other.getContracts().coOpMessage(str);
        }
        totalAmount = (int) Math.floor(request.isShort ? totalAmount * 1.10 : totalAmount * 1.75);
        return totalAmount;
    }
}