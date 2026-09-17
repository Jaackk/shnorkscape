package com.rs.game.player.content.newlottery;

import com.rs.cores.CoresManager;
import com.rs.game.player.Player;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class LotteryBetManager {
    private final Map<String, LotteryBet> bets = new ConcurrentHashMap<>();
    private final Lottery lottery;

    LotteryBetManager(Lottery lottery) {
        this.lottery = lottery;
    }

    public boolean canAdd(LotteryBetType type) {
        int totalWinnings = getTotalWinnings() + type.amount;
        return totalWinnings > 0;
    }

    public void add(Path path, LotteryBet bet) {
        int totalWinnings = getTotalWinnings() + bet.type.amount;
        if (totalWinnings < 0) {
            // Overflow, don't let player bet.
            throw new RuntimeException();
        }
        saveOne(path, bet);
        bets.put(bet.username, bet);
        lottery.start(totalWinnings);
        lottery.startLotteryTask();
    }

    public boolean has(Player player) {
        for(LotteryBet bet : bets.values()) {
            if(bet.username.equals(player.getUsername()) ||
                    bet.mac.equals(player.getCurrentMac())) {
                return true;
            }
        }
        return false;
    }

    public int getTotalWinnings() {
        return bets.values().stream()
                .mapToInt(it -> it.type.amount)
                .sum();
    }

    public String formatTotalWinnings() {
        return formatWinnings(getTotalWinnings());
    }

    public String formatWinnings(int winnings) {
        return Colors.RED + Utils.formatNumber(winnings) + "</col>";
    }

    public LotteryBet getRandomBet() {
        List<LotteryBet> betList = new ArrayList<>(bets.values());
        int totalChance = 0;
        for (LotteryBet bet : betList) {
            totalChance += bet.type.chance;
        }
        Collections.shuffle(betList);
        int roll = ThreadLocalRandom.current().nextInt(totalChance) + 1;
        int mod = 0;
        for (LotteryBet bet : betList) {
            mod += bet.type.chance;
            if (roll <= mod) {
                return bet;
            }
        }
        throw new IllegalStateException("Invalid roll: " + roll);
    }

    public int getBetCount() {
        return bets.size();
    }

    public void load(Path path) {
        if (Files.exists(path)) {
            try (Scanner scanner = new Scanner(path)) {
                while (scanner.hasNextLine()) {
                    String[] tokens = scanner.nextLine().split("---");
                    String username = tokens[0];
                    String displayName = tokens[1];
                    LotteryBetType betLevel = LotteryBetType.valueOf(tokens[2]);
                    String mac = tokens[3];
                    bets.put(username, new LotteryBet(username, displayName, betLevel, mac));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void save(Path path) {
        CoresManager.getServiceProvider().executeNow(() -> {
            try (FileWriter writer = new FileWriter(path.toFile())) {
                for (LotteryBet bet : bets.values()) {
                    writer.write(bet.username + "---" + bet.displayName + "---" + bet.type + "---" + bet.mac + '\n');
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void saveOne(Path path, LotteryBet bet) {
        CoresManager.getServiceProvider().executeNow(() -> {
            try (FileWriter writer = new FileWriter(path.toFile(), true)) {
                writer.write(bet.username + "---" + bet.displayName + "---" + bet.type + "---" + bet.mac + '\n');
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void clear() {
        bets.clear();
    }
}
