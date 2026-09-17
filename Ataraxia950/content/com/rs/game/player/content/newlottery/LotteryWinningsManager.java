package com.rs.game.player.content.newlottery;

import com.rs.cores.CoresManager;
import com.rs.game.Graphics;
import com.rs.game.item.Item;
import com.rs.game.player.Bank;
import com.rs.game.player.Player;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class LotteryWinningsManager {

    private final Map<String, LotteryWinnings> winnings = new ConcurrentHashMap<>();

    private final Lottery lottery;

    LotteryWinningsManager(Lottery lottery) {
        this.lottery = lottery;
    }

    public void add(Path path, LotteryWinnings reward) {
        saveOne(path, reward);
        winnings.put(reward.getUsername(), reward);
    }

    public boolean has(Player player) {
        return winnings.containsKey(player.getUsername());
    }

    public boolean give(Player player, int winningsAmt) {
        String strWinnings = lottery.bets.formatWinnings(winningsAmt);
        Bank bank = player.getBank();
        player.setNextGraphics(new Graphics(1765));
        player.sendMessage("Congratulations, you've won the lottery! You have been awarded " + strWinnings + "gp.");
        boolean overflow = bank.getNumberOf(995) + winningsAmt < 0;
        if (bank.hasBankSpace() && !overflow) {
            bank.addItem(new Item(995, winningsAmt), true);
            player.sendMessage("It has been deposited to your bank account.");
            return true;
        }
        player.sendMessage("You can collect it from the Ataraxia lottery coordinator.");
        return false;
    }

    public void giveAfter(Player player) {
        LotteryWinnings plrWinnings = winnings.remove(player.getUsername());
        player.addMoney(plrWinnings.getWinnings());
        save(lottery.winningsFile);
    }

    public void load(Path path) {
        if (Files.exists(path)) {
            try (Scanner scanner = new Scanner(path)) {
                while (scanner.hasNextLine()) {
                    String[] tokens = scanner.nextLine().split(":");
                    String username = tokens[0];
                    int amount = Integer.parseInt(tokens[1]);
                    LocalDate date = LocalDate.parse(tokens[2]);
                    winnings.put(username, new LotteryWinnings(username, amount, date));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void save(Path path) {
        CoresManager.getServiceProvider().executeNow(() -> {
            try (FileWriter writer = new FileWriter(path.toFile())) {
                for (LotteryWinnings reward : winnings.values()) {
                    writer.write(reward.getUsername() + ":" + reward.getWinnings() + ":" + reward.getDate() + '\n');
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

    }

    public void saveOne(Path path, LotteryWinnings lw) {
        if (winnings.containsKey(lw.getUsername())) {
            return;
        }
        CoresManager.getServiceProvider().executeNow(() -> {
            try (FileWriter writer = new FileWriter(path.toFile(), true)) {
                writer.write(lw.getUsername() + ":" + lw.getWinnings() + ":" + lw.getDate() + '\n');
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
