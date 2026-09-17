package com.rs.game.player.content.newlottery;

import com.rs.cores.CoresManager;
import com.rs.game.player.Player;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class LotteryStatistics {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, uuuu");
    private LocalDate since = LocalDate.now();
    public volatile String lastWinner;
    public volatile int lastJackpot = -1;
    public volatile long totalGpWon;
    public volatile long totalGpTaxed;
    public volatile long totalLottos;
    public volatile long totalBets;

    public volatile boolean available;

    private final Lottery lottery;

    public LotteryStatistics(Lottery lottery) {
        this.lottery = lottery;
    }

    public void display(Player player) {
        if (player.getInterfaceManager().containsScreenInter()) {
            player.getInterfaceManager().closeScreenInterface();
        }

        if (player.getInterfaceManager().containsChatBoxInter()) {
            player.getInterfaceManager().closeChatBoxInterface();
        }

        if (player.getInterfaceManager().containsInventoryInter()) {
            player.getInterfaceManager().closeInventoryInterface();
        }

        player.getInterfaceManager().sendInterface(1245);
        int lineId = 13;
        player.getPackets().sendIComponentText(1245, 330, Colors.YELLOW + "Lottery statistics</col>");
        player.getPackets().sendIComponentText(1245, lineId++, Colors.YELLOW + "Since:</col> " + formatter.format(since));
        player.getPackets().sendIComponentText(1245, lineId++, "");
        player.getPackets().sendIComponentText(1245, lineId++, Colors.YELLOW + Colors.SHAD + "Last lottery's results</col>");
        player.getPackets().sendIComponentText(1245, lineId++, Colors.YELLOW + "Last jackpot:</col> " + Utils.formatNumber(lastJackpot) + " gp");
        player.getPackets().sendIComponentText(1245, lineId++, Colors.YELLOW + "Last winner:</col> " + lastWinner);
        player.getPackets().sendIComponentText(1245, lineId++, "");
        player.getPackets().sendIComponentText(1245, lineId++, Colors.YELLOW + Colors.SHAD + "Previous lottery statistics");
        player.getPackets().sendIComponentText(1245, lineId++, Colors.YELLOW + "Total gp won:</col> " + Utils.formatNumber(totalGpWon) + " gp");
        player.getPackets().sendIComponentText(1245, lineId++, Colors.YELLOW + "Total gp taxed:</col> " + Utils.formatNumber(totalGpTaxed) + " gp");
        player.getPackets().sendIComponentText(1245, lineId++, Colors.YELLOW + "Total lottery games:</col> " + Utils.formatNumber(totalLottos));
        player.getPackets().sendIComponentText(1245, lineId, Colors.YELLOW + "Total bets placed:</col> " + Utils.formatNumber(totalBets));
    }

    public void load(Path path) {
        if (Files.exists(path)) {
            try (Scanner scanner = new Scanner(path)) {
                since = LocalDate.parse(scanner.nextLine());
                totalGpWon = Long.parseLong(scanner.nextLine());
                totalGpTaxed = Long.parseLong(scanner.nextLine());
                totalLottos = Long.parseLong(scanner.nextLine());
                totalBets = Long.parseLong(scanner.nextLine());
                lastWinner = scanner.nextLine();
                lastJackpot = Integer.parseInt(scanner.nextLine());
                available = true;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void save(Path path) {
        CoresManager.getServiceProvider().executeNow(() -> {
            StringBuilder sb = new StringBuilder()
                    .append(since).append('\n')
                    .append(totalGpWon).append('\n')
                    .append(totalGpTaxed).append('\n')
                    .append(totalLottos).append('\n')
                    .append(totalBets).append('\n')
                    .append(lastWinner).append('\n')
                    .append(lastJackpot);
            try (FileWriter writer = new FileWriter(path.toFile())) {
                writer.write(sb.toString());
                available = true;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

    }
}
