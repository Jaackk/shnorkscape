package com.rs.game.activities.aod.reward;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Entity;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.utils.Utils;
import lombok.val;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.rs.game.activities.aod.AngelOfDeath.MAINDROP;
import static com.rs.game.activities.aod.AngelOfDeath.SIDEDROP;

/**
 * A class that generates a set of rewards for the victorious players, up to 10 players.
 * 7 first players will receive the main drop, 3 remaining players, if applicable will
 * receive a side drop which has a lower chance of including a rare item.
 *
 * @author Kris | 30. sept 2017 : 17:25.14
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>}
 */
public final class AoDRewardGeneration {

    public AoDRewardGeneration(final List<Player> playersFighting, final Map<Entity, Integer> damageSet) {
        generateEligiblePlayers(playersFighting, damageSet);
    }

    private final List<Player> eligiblePlayers = new ArrayList<>();

    /**
     * Fills the arraylist of eligible players in a fixed order, based on their damage dealt to AoD.
     * Up to 10 entries allowed.
     *
     * @param playersFighting
     * @param damageSet
     */
    private void generateEligiblePlayers(final List<Player> playersFighting, final Map<Entity, Integer> damageSet) {
        for (Entry<Entity, Integer> entry : damageSet.entrySet()) {
            if (!(entry.getKey() instanceof Player)
                    || !playersFighting.contains(entry.getKey())) {
                continue;
            }

            eligiblePlayers.add((Player) entry.getKey());
        }

        eligiblePlayers.sort(Comparator.comparing(damageSet::get));
    }

    /**
     * Generates a random drop for every eligible player. Puts it in a map.
     *
     * @return map of players and their drop.
     */
    public Map<Player, Item> getRewardsMap() {
        boolean giveWand = Utils.random(0, 150) == 0;
        boolean giveCodex = false;

        if (!giveWand) {
            giveCodex = Utils.random(0, 100) == 0;
        }

        final Map<Player, Item> map = new HashMap<>();
        for (int i = 0; i < Math.min(eligiblePlayers.size(), 7); i++) {
            final Player p = eligiblePlayers.get(i);
            boolean givePet = Utils.random(p.getPerkManager().hasPerkActive(DonationPerk.PETSCHANTER) ? 2500 : 3000) == 0;
            final List<Item> rewardTable = new ArrayList<>();
            for (AoDRewards reward : AoDRewards.VALUES) {
                if (i < 6 && reward.getType() == SIDEDROP || i >= 6 && reward.getType() == MAINDROP) {
                    continue;
                }

                if (Utils.getRandomDouble(100) < reward.getRate())
                    rewardTable.add(new Item(reward.getId(), Utils.random(reward.getMinimumAmount(), reward.getMaximumAmount())));
            }
            if (givePet) {
                val petItemId = 39624;
                map.put(p, new Item(p.hasItem(petItemId) ? 4012 : petItemId));
            } else if (giveWand && Utils.randomBool()) {
                Item reward = new Item(Utils.randomBool() ? 39574 : 39579);
                if (p.isDiamondDonor() && p.isNotingDrops()) {
                    val definitions = ItemDefinitions.getItemDefinitions(reward.getId());
                    if (definitions != null && !definitions.isNoted() && definitions.getCertId() != -1) {
                        reward.setId(definitions.getCertId());
                    }
                }
                map.put(p, reward);
                giveWand = false;
            } else if (giveCodex && Utils.randomBool()) {
                Item reward = new Item(39584);
                if (p.isDiamondDonor() && p.isNotingDrops()) {
                    val definitions = ItemDefinitions.getItemDefinitions(reward.getId());
                    if (definitions != null && !definitions.isNoted() && definitions.getCertId() != -1) {
                        reward.setId(definitions.getCertId());
                    }
                }
                map.put(p, reward);
                giveCodex = false;
            } else {
                Item reward = rewardTable.get(Utils.random(rewardTable.size()));
                if (reward != null && p.isDiamondDonor() && p.isNotingDrops()) {
                    val definitions = ItemDefinitions.getItemDefinitions(reward.getId());
                    if (definitions != null && !definitions.isNoted() && definitions.getCertId() != -1) {
                        reward.setId(definitions.getCertId());
                    }
                }
                map.put(p, reward);
            }
            
        }
        return map;
    }
}
