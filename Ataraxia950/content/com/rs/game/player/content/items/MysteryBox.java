package com.rs.game.player.content.items;

import com.rs.game.World;
import com.rs.game.activities.seasonalevents.SeasonalEventManager;
import com.rs.game.activities.seasonalevents.christmas.ChristmasSeasonalEvent;
import com.rs.game.activities.seasonalevents.christmas.PresentHandler;
import com.rs.game.item.Item;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.utils.Colors;
import com.rs.utils.Utils;
import com.rs.utils.mysql.QueryExecutor;
import com.rs.utils.mysql.impl.News;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.google.common.base.Preconditions.checkState;

public class MysteryBox {

    public static final int TICKS_TO_REWARD = 60000;

    // Regular Mystery Box
    public enum RewardSet {
        COMMON(new Item(995, 1000000), new Item(1513, 112), new Item(37971, 15), new Item(2363, 100), new Item(6685, 35), new Item(15270, 112), new Item(536, 112), new Item(30037, 50), new Item(31350, 50), new Item(32337, 50), new Item(33740, 30), new Item(34528, 50), new Item(12158, 200), new Item(12159, 20) ,new Item(36390, 5), new Item(36730, 1), new Item(36719, 3), new Item(36721, 3)), // 65
        UNCOMMON(new Item(4720, 1), new Item(34023, 1), new Item(4722, 1), new Item(37972, 10), new Item(4718, 1), new Item(32262, 200), new Item(995, 2500000), new Item(6199, 5), new Item(34027, 2), (new Item(4755, 1)), new Item(4757, 1), new Item(4716, 1), new Item(4759, 1), new Item(4151, 1), new Item(4751, 1), new Item(4749, 1), new Item(4747, 1), new Item(4745, 1), new Item(4753, 1), new Item(4736, 1), new Item(4738, 1), new Item(4734, 1), new Item(4732, 1), new Item(4730, 1), new Item(4728, 1), new Item(4726, 1), new Item(4724, 1), new Item(4714), new Item(4712, 1), new Item(4710, 1), new Item(4708), new Item(12160, 200), new Item(36390, 10), new Item(36725, 1), new Item(36730, 3)), // 25
        RARE(new Item(34024, 1), new Item(6571, 1), new Item(995, 50000000), new Item(18830, 84), new Item(33246, 70), new Item(37971, 40), new Item(12163, 150), new Item(19784, 1), new Item(36725, 3)), // 9
        LEGENDARY(new Item(10723, 1), new Item(34006, 1), new Item(33683, 1), new Item(24437, 1), new Item(10318, 1), new Item(26493, 1), new Item(10840, 1), new Item(4012, 1), new Item(995, 200000000), new Item(22534, 1), new Item(6199, 5), new Item(29638, 1), new Item(13905, 1), new Item(33508, 1), new Item(33510, 1)),
        ;
        public static final RewardSet[] VALUES = values();

        @Getter
        private final Item[] rewards;

        RewardSet(final Item... rewards) {
            checkState(rewards.length >= 2, "Reward sets must have at least 2 items.");
            this.rewards = rewards;
        }

        public List<Item> getSet() {
            return Arrays.asList(this.rewards);
        }
    }

    public static void give10HourReward(Player player) {
        if (player.getTimeToNextMysterybox() > 0)
            return;

        if (player.getTimeToNextMysterybox() == 0) {
            player.setTimeToNextMysterybox(TICKS_TO_REWARD);
            boolean inventory = true;
            if (inventory = !player.getInventory().addItem(6199, 1))
                player.getBank().addItem(new Item(6199, 1), true);

            player.sendMessage("Congratulations! You are awarded a free mystery box for playing " + ((Utils.getHoursPlayed(player.getRecordedPlayTime()) > 10 ? " another 10 hours!" : " 10 hours!") + " It has been added to your " + (inventory ? " inventory." : " bank.")));
        }
    }

    public static void roll(final Player player) {

        RewardSet rewards;
        final int roll = Utils.random(100);
        if (roll == 99)
            rewards = RewardSet.LEGENDARY;
        else if (roll >= 95)
            rewards = RewardSet.RARE;
        else if (roll >= 70)
            rewards = RewardSet.UNCOMMON;
        else
            rewards = RewardSet.COMMON;

        LinkedList<Item> rewardsCopy = new LinkedList<>(rewards.getSet());
        Collections.shuffle(rewardsCopy);
        final Item reward = rewardsCopy.removeFirst();
        final Item bonusReward = rewardsCopy.removeFirst();

        if (player.isOwner())
            player.sendMessage("You have won a " + reward.getName() + " with a roll of: " + roll + " and a size of: " + rewards.getSet().size());

        if (rewards == RewardSet.RARE || rewards == RewardSet.LEGENDARY) {
            World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + player.getDisplayName() + " has received " + Utils.formatNumber(reward.getAmount()) + "x " + reward.getName() + " from a Mystery Box", false);
            QueryExecutor.submit(new News(player, "<b><img src=\"../bin/images/news/drop.png\" height=17> " + player.getDisplayName() + " has received a " + Utils.formatNumber(reward.getAmount()) + "x " + reward.getName() + " from a Mystery Box"));
            if (player.getPerkManager().hasPerkActive(DonationPerk.THE_BOXER)) {
                World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + player.getDisplayName() + " has received " + Utils.formatNumber(bonusReward.getAmount()) + "x " + bonusReward.getName() + " from a Mystery Box as a bonus reward", false);
                QueryExecutor.submit(new News(player, "<b><img src=\"../bin/images/news/drop.png\" height=17> " + player.getDisplayName() + " has received a " + Utils.formatNumber(bonusReward.getAmount()) + "x " + bonusReward.getName() + " from a Mystery Box as a bonus reward"));
            }
        }

        player.getInventory().deleteItem(6199, 1);
        if (reward.getId() == 995) {
            player.addMoney(reward.getAmount());
        } else {
            player.getBank().addItem(reward, true);
            player.sendMessage("Your reward: " + Utils.formatNumber(reward.getAmount()) + "x " + reward.getName() + " has been added to your bank");
        }
        if (player.getPerkManager().hasPerkActive(DonationPerk.THE_BOXER)) {
            if (bonusReward.getId() == 995) {
                player.addMoney(bonusReward.getAmount());
            } else {
                player.getBank().addItem(bonusReward, true);
                player.sendMessage("Your reward: " + Utils.formatNumber(bonusReward.getAmount()) + "x " + bonusReward.getName() + " has been added to your bank");
            }
        }
        if (SeasonalEventManager.isActive(ChristmasSeasonalEvent.class) && ThreadLocalRandom.current().nextInt(3) == 0) {
            Item item = PresentHandler.givePresentReward(player, false);
            if(item != null) {
                String itemName = item.getName();
                player.sendMessage(Colors.DEF_SEARCH_CYAN + "You find " + Utils.getAorAn(itemName) + " " + itemName + "(x" + item.getAmount() + ") in the box as well. Happy holidays!");
            }
        }
    }
}
