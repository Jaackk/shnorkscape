package com.rs.game.player.content.polls;

import com.rs.game.item.Item;
import com.rs.utils.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class PollRewardsDatabase {

    private final class PollReward {
        private final int id;
        private final int min;
        private final int max;
        private final int chance;
        private final boolean special;

        private PollReward(int id, int min, int max, int chance) {
            this(id, min, max, chance, false);
        }

        private PollReward(int id, int min, int max, int chance, boolean special) {
            this.id = id;
            this.min = min;
            this.max = max;
            this.chance = chance;
            this.special = special;
        }

        private boolean success() {
            return ThreadLocalRandom.current().nextInt(chance) == 0;
        }

        private Item toItem() {
            return new Item(id, ThreadLocalRandom.current().nextInt(min, max + 1));
        }
    }

    private final Path rewardsPath = Paths.get("data/pollbooth/rewards.txt");
    private final List<PollReward> rewards = new ArrayList<>();
    private boolean useDefaultRewards = true;

    public void load() {
        rewards.clear();
        if (Files.exists(rewardsPath)) {
            try (Scanner sc = new Scanner(rewardsPath)) {
                while (sc.hasNextLine()) {
                    String nextLine = sc.nextLine().trim();
                    if (nextLine.startsWith("#") || nextLine.isEmpty()) {
                        continue;
                    }
                    if (nextLine.startsWith("item:")) {
                        String[] tokens = nextLine.substring(5).split(",");
                        int id = Integer.parseInt(tokens[0].trim());
                        int min = Integer.parseInt(tokens[1].trim());
                        int max = Integer.parseInt(tokens[2].trim());
                        int chance = Integer.parseInt(tokens[3].trim());
                        boolean special = tokens.length > 4 && Boolean.parseBoolean(tokens[4].trim());
                        rewards.add(new PollReward(id, min, max, chance, special));
                    } else if (nextLine.startsWith("default_rewards:")) {
                        useDefaultRewards = Boolean.parseBoolean(nextLine.substring(16).trim());
                    } else {
                        throw new IllegalStateException("Unexpected token: " + nextLine);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            useDefaultRewards = true;
        }
        if (useDefaultRewards) {
            rewards.add(new PollReward(6572, 1, 3, 32)); // Onyx.
            rewards.add(new PollReward(37972, 5, 15, 16)); // Aggro pots.
            rewards.add(new PollReward(6199, 2, 5, 16)); // Mystery box.
            rewards.add(new PollReward(35886, 1, 2, 8)); // Supply cache.
            rewards.add(new PollReward(41451, 1, 2, 8)); // Supply boxes v.
            rewards.add(new PollReward(41452, 1, 2, 8));
            rewards.add(new PollReward(41453, 1, 2, 8));
            rewards.add(new PollReward(41454, 1, 2, 8));
            rewards.add(new PollReward(41455, 1, 2, 8));
        }
    }

    public Item computeReward() {
        if (rewards.isEmpty())
            return null;
        int rolls = 4;
        for (PollReward rw : rewards) {
            if (rw.special && rw.success()) {
                return rw.toItem();
            }
        }
        for (int loop = 0; loop < rolls; loop++) {
            PollReward rw = Utils.randomFrom(rewards);
            if (rw.success()) {
                return rw.toItem();
            }
        }
        return null;
    }
}