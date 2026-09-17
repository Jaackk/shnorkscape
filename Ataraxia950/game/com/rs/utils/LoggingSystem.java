package com.rs.utils;

import com.google.common.collect.ImmutableList;
import com.rs.Settings;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cores.CoresManager;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.content.grandExchange.GrandExchange;
import com.rs.game.player.dialogue.impl.AtaraxiaDollarsTransferD;
import com.rs.utils.LoggingSqlManager.DropPickupLogSql;
import com.rs.utils.LoggingSqlManager.GrandExchangeLogSql;
import com.rs.utils.LoggingSqlManager.TradeLogSql;
import com.rs.utils.mysql.struct.PrivateMessageLog;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A class used to write Logs about various player actions.
 *
 * @author Noel
 */
public class LoggingSystem {

    private static BufferedWriter newLogWriter(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        return new BufferedWriter(new FileWriter(path.toFile(), true));
    }

    /**
     * Logs Players IP address.
     *
     * @param player The player to Log.
     */
    public static void logIP(Player player) {
        if (!Settings.DEBUG) {
            player.setLastIP(player.getIP());
            CoresManager.getServiceProvider().executeNow(() -> {
                try {
                    String FILE_PATH = Settings.LOGS_PATH + "/iplogs/";
                    DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
                    Calendar cal = Calendar.getInstance();
                    try (BufferedWriter writer = newLogWriter(FILE_PATH + player.getUsername() + ".txt")) {
                        writer.write("[" + dateFormat.format(cal.getTime()) + "]: " + "Display name: " + player.getDisplayName()
                                + " (Username: " + player.getUsername() + ") " + "- IP: " + player.getIP() + "; "
                                + "Current MAC: " + player.getCurrentMac() + " " + "(Registered MAC: "
                                + player.getRegisteredMac() + ").");
                        writer.newLine();
                    }
                } catch (IOException e) {
                    Logger.getGlobal().catching(e);
                }
            });
        }
    }

    /**
     * Logs a Private chat message.
     *
     * @param player The player to Log.
     * @param p2 The player to send the Private message.
     * @param fixChatMessage The message to Log.
     */
    public static void logPM(Player player, Player p2, String fixChatMessage) {
        if (Settings.DEBUG)
            return;
        PrivateMessageLog.list.add(new PrivateMessageLog(player, p2.getUsername(), fixChatMessage));
        CoresManager.getServiceProvider().executeNow(() -> {
            try {
                String FILE_PATH = Settings.LOGS_PATH + "/pmlogs/";
                DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
                Calendar cal = Calendar.getInstance();
                try (BufferedWriter writer = newLogWriter(FILE_PATH + player.getUsername() + ".txt")) {
                    writer.write("[" + dateFormat.format(cal.getTime()) + ", IP: " + player.getIP() + "] : "
                            + fixChatMessage + ". To user: " + p2.getUsername());
                    writer.newLine();
                }
            } catch (IOException e) {
                Logger.getGlobal().catching(e);
            }
        });
    }

    /**
     * Logs a Public chat message.
     *
     * @param player The player to Log.
     * @param message The message to Log.
     */
    public static void logPublicChat(Player player, String message) {
        if (Settings.DEBUG)
            return;
        CoresManager.getServiceProvider().executeNow(() -> {
            try {
                String FILE_PATH = Settings.LOGS_PATH + "/chatlogs/";
                DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
                Calendar cal = Calendar.getInstance();
                try (BufferedWriter writer = newLogWriter(FILE_PATH + player.getUsername() + ".txt")) {
                    writer.write("[" + dateFormat.format(cal.getTime()) + ", IP: " + player.getIP() + "] : " + message);
                    writer.newLine();
                }
            } catch (IOException e) {
                Logger.getGlobal().catching(e);
            }
        });
    }
    
    public static void logGambling(Player p, Player p2, List<Item> items) {
    	
    	CoresManager.getServiceProvider().executeNow(() -> {
    		try {
    			 String FILE_PATH = Settings.LOGS_PATH + "/gamblingLogs/";
                 DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
                 Calendar cal = Calendar.getInstance();
                 String msg = p.getUsername()+" won a battle again "+p2.getUsername()+" and won the following items: ";
                 for (Item m : items) {
                	 msg += ""+m.getAmount()+"x "+m.getName()+", ";
                 }
                 try (BufferedWriter writer = newLogWriter(FILE_PATH + p.getUsername() + ".txt")) {
                     writer.write("[" + dateFormat.format(cal.getTime()) + ", IP: " + p.getIP() + "] : " + msg);
                     writer.newLine();
                 }
    		} catch (Exception e) {
    			Logger.getGlobal().catching(e);
    		}
    		
    	});
    	
    }

    /**
     * Logs a Trade session between the Traders.
     *
     * @param player The player 1 trading.
     * @param oldTarget The player 2 trading.
     * @param containedItems The traded items.
     */
    public static void logTrade(Player player, Player oldTarget, CopyOnWriteArrayList<Item> containedItems) {
        if (Settings.DEBUG)
            return;
        if (containedItems == null)
            return;
        if (Settings.SQL_LOGGING) {
            LoggingSqlManager.getInstance().queueTask(new TradeLogSql(player.getUsername(),
                    player.getDisplayName(),
                    player.getIP(),
                    oldTarget.getUsername(),
                    oldTarget.getDisplayName(),
                    oldTarget.getIP(),
                    containedItems));
        } else {
            CoresManager.getServiceProvider().executeNow(() -> {
                try {
                    String FILE_PATH = Settings.LOGS_PATH + "/tradelogs/";
                    DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
                    Calendar cal = Calendar.getInstance();
                    try (BufferedWriter writer = newLogWriter(FILE_PATH + oldTarget.getUsername() + ".txt")) {
                        writer.write("[Trade session started]");
                        writer.newLine();
                        writer.write("Trader Information: Username: " + oldTarget.getUsername() + ". IP "
                                + oldTarget.getIP() + ". Current Mac: " + oldTarget.getCurrentMac() + ". Location: "
                                + oldTarget.getX() + ", " + oldTarget.getY() + ", " + oldTarget.getPlane() + ".");
                        writer.newLine();
                        writer.write("Player Information: Username: " + player.getUsername() + ". IP: " + player.getLastIP()
                                + ". Current Mac: " + player.getCurrentMac() + ". Location: " + player.getX() + ", " + player.getY()
                                + ", " + player.getPlane() + ".");
                        writer.newLine();
                        writer.write("Time: [" + dateFormat.format(cal.getTime()) + "]");
                        for (Item item : containedItems) {
                            if (item == null)
                                continue;
                            ItemDefinitions defs = ItemDefinitions.getItemDefinitions(item.getId());
                            String name = defs == null ? "" : defs.getName().toLowerCase();
                            writer.newLine();
                            writer.write(oldTarget.getUsername() + " Gave: " + name + ", amount: " + Utils.getFormattedNumber(item.getAmount()));
                            if (GrandExchange.getPrice(item.getId()) >= 5_000_000 || (item.getId() == 995 && item.getAmount() >= 5_000_000) || Objects.requireNonNull(defs).getName().equalsIgnoreCase("spirit shards") || item.getId() == 41430) {
                                World.sendWorldMessage("<img=7><col=ff0000>Trade Log: " + player.getDisplayName() + " traded " + oldTarget.getDisplayName() + ": " + name + " X: " + Utils.getFormattedNumber(item.getAmount()) + ".", true);
                            }
                        }
                        writer.newLine();
                        writer.write("[Trade session ended]");
                        writer.newLine();
                    }
                } catch (IOException e) {
                    Logger.getGlobal().catching(e);
                }
            });
        }
    }

    /**
     * Logs when the player drops an item.
     */
    public static void logItemDrop(Player player, Item item, WorldTile tile) {
        if (Settings.DEBUG)
            return;
        if (Settings.SQL_LOGGING) {
            LoggingSqlManager.getInstance().queueTask(new DropPickupLogSql(player.getUsername(),
                    player.getDisplayName(),
                    player.getIP(),
                    item.getName(),
                    item.getAmount(),
                    tile.getX(),
                    tile.getY(),
                    tile.getPlane(),
                    true));
        } else {
            CoresManager.getServiceProvider().executeNow(() -> {
                try {
                    String FILE_PATH = Settings.LOGS_PATH + "/droplogs/";
                    DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
                    Calendar cal = Calendar.getInstance();
                    try (BufferedWriter writer = newLogWriter(FILE_PATH + player.getUsername() + ".txt")) {
                        writer.write("[" + dateFormat.format(cal.getTime()) + ", IP: " + player.getIP()
                                + "] : dropped item: " + item.getAmount() + " x " + item.getName() + " at coords: " + tile.getX()
                                + ", " + tile.getY() + ", " + tile.getPlane() + ".");
                        writer.newLine();
                    }
                } catch (IOException e) {
                    Logger.getGlobal().catching(e);
                }
            });
        }
    }

    /**
     * Logs when the player picks up an item.
     */
    public static void logItemPickup(Player player, Item item, WorldTile tile) {
        if (Settings.DEBUG)
            return;
        if (Settings.SQL_LOGGING) {
            if(item == null || tile == null || player == null)
                return;
            LoggingSqlManager.getInstance().queueTask(new DropPickupLogSql(player.getUsername(),
                    player.getDisplayName(),
                    player.getIP(),
                    item.getName(),
                    item.getAmount(),
                    tile.getX(),
                    tile.getY(),
                    tile.getPlane(),
                    false));
        } else {
            CoresManager.getServiceProvider().executeNow(() -> {
                try {
                    String FILE_PATH = Settings.LOGS_PATH + "/itempickuplogs/";
                    DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
                    Calendar cal = Calendar.getInstance();
                    try (BufferedWriter writer = newLogWriter(FILE_PATH + player.getUsername() + ".txt")) {
                        writer.write("[" + dateFormat.format(cal.getTime()) + ", IP: " + player.getIP()
                                + "] : picked up item: " + item.getAmount() + " x " + item.getName() + " at coords: " + tile.getX()
                                + ", " + tile.getY() + ", " + tile.getPlane() + ".");
                        writer.newLine();
                    }
                } catch (IOException e) {
                    Logger.getGlobal().catching(e);
                }
            });
        }
    }

    /**
     * Logs when the player claims their G.E. items.
     */
    public static void logGrandExchange(Player player, Item item) {
        if (Settings.DEBUG)
            return;
        if(Settings.SQL_LOGGING) {
            LoggingSqlManager.getInstance().queueTask(new GrandExchangeLogSql(player.getUsername(),
                    player.getDisplayName(),
                    player.getIP(),
                    item.getName(),
                    item.getAmount()));
        } else {
            CoresManager.getServiceProvider().executeNow(() -> {
                try {
                    String FILE_PATH = Settings.LOGS_PATH + "/grandexchangelogs/";
                    DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
                    Calendar cal = Calendar.getInstance();
                    try (BufferedWriter writer = newLogWriter(FILE_PATH + player.getUsername() + ".txt")) {
                        writer.write("[" + dateFormat.format(cal.getTime()) + ", IP: " + player.getIP()
                                + "] : collected an item: " + item.getAmount() + " " + "x " + item.getName() + ".");
                        writer.newLine();
                    }
                } catch (IOException e) {
                    Logger.getGlobal().catching(e);
                }
            });
        }

    }

    /**
     * Logs Duel Arena stakes.
     *
     * @param player to Log.
     * @param victor The winner.
     * @param wonItems Items that have been won.
     */
    public static void logDuelStake(Player player, Player victor, CopyOnWriteArrayList<Item> wonItems) {
        CoresManager.getServiceProvider().executeNow(() -> {
            try {
                String FILE_PATH = Settings.LOGS_PATH + "/stakelogs/";
                DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
                Calendar cal = Calendar.getInstance();
                try (BufferedWriter writer = newLogWriter(FILE_PATH + victor.getUsername() + ".txt")) {
                    writer.write("[Stake session started]");
                    writer.newLine();
                    writer.write("Stake Winner Information: Username: " + victor.getUsername() + ". IP "
                            + victor.getIP() + ". Current Mac: " + victor.getCurrentMac() + ". Location: "
                            + victor.getX() + ", " + victor.getY() + ", " + victor.getPlane() + ".");
                    writer.newLine();
                    writer.write("Stake Loser Information: Username: " + player.getUsername() + ". IP: " + player.getLastIP()
                            + ". Current Mac: " + player.getCurrentMac() + ". Location: " + player.getX() + ", " + player.getY()
                            + ", " + player.getPlane() + ".");
                    writer.newLine();
                    writer.write("Time: [" + dateFormat.format(cal.getTime()) + "]");
                    for (Item item : wonItems) {
                        if (item == null)
                            continue;
                        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(item.getId());
                        String name = defs == null ? "" : defs.getName().toLowerCase();
                        writer.newLine();
                        writer.write(victor.getUsername() + " won " + Utils.getFormattedNumber(item.getAmount()) + " x " + name
                                + " from " + player.getUsername() + ".");

                        if (GrandExchange.getPrice(item.getId()) >= 50000000
                                || (item.getId() == 995 && item.getAmount() >= 50000000))
                            World.sendWorldMessage("<img=7><col=ff0000>Stake Log: " + victor.getUsername() + " won "
                                    + Utils.getFormattedNumber(item.getAmount()) + " x " + name + " from "
                                    + player.getUsername() + ".", true);
                    }
                    writer.newLine();
                    writer.write("[Stake session ended]");
                    writer.newLine();
                }
            } catch (IOException e) {
                Logger.getGlobal().catching(e);
            }
        });
    }

    /**
     * Logs Duel Arena lost stakes.
     *
     * @param loser the Looser.
     * @param victor The winner.
     * @param lostItems Items that have been lost.
     */
    public static void logLostDuelStake(Player loser, Player victor, CopyOnWriteArrayList<Item> lostItems) {
        CoresManager.getServiceProvider().executeNow(() -> {
            try {
                String FILE_PATH = Settings.LOGS_PATH + "/stakelogs/";
                DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
                Calendar cal = Calendar.getInstance();
                try (BufferedWriter writer = newLogWriter(FILE_PATH + loser.getUsername() + ".txt")) {
                    writer.write("[Stake session started]");
                    writer.newLine();
                    writer.write("Stake Winner Information: Username: " + victor.getUsername() + ". IP "
                            + victor.getIP() + ". Current Mac: " + victor.getCurrentMac() + ". Location: "
                            + victor.getX() + ", " + victor.getY() + ", " + victor.getPlane() + ".");
                    writer.newLine();
                    writer.write("Stake Loser Information: Username: " + loser.getUsername() + ". IP: " + loser.getLastIP()
                            + ". Current Mac: " + loser.getCurrentMac() + ". Location: " + loser.getX() + ", " + loser.getY()
                            + ", " + loser.getPlane() + ".");
                    writer.newLine();
                    writer.write("Time: [" + dateFormat.format(cal.getTime()) + "]");
                    for (Item item : lostItems) {
                        if (item == null)
                            continue;
                        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(item.getId());
                        String name = defs == null ? "" : defs.getName().toLowerCase();
                        writer.newLine();
                        writer.write(loser.getUsername() + " lost " + Utils.getFormattedNumber(item.getAmount()) + " x " + name
                                + " to " + victor.getUsername() + ".");
                    }
                    writer.newLine();
                    writer.write("[Stake session ended]");
                    writer.newLine();
                }
            } catch (IOException e) {
                Logger.getGlobal().catching(e);
            }
        });
    }

    /**
     * Logs everything player-address related.
     *
     * @param player The player to log.
     */
    public static void logAddress(Player player) {
        CoresManager.getServiceProvider().executeNow(() -> {
            try {
                String FILE_PATH = Settings.LOGS_PATH + "/AddressLogs.txt";
                DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
                Calendar cal = Calendar.getInstance();
                try (BufferedWriter bf = newLogWriter(FILE_PATH)) {
                    bf.write("[" + dateFormat.format(cal.getTime()) + "]: Display name: " + player.getDisplayName()
                            + " (Username: " + player.getUsername() + ") " + "- IP: " + player.getIP()
                            + "; Current MAC: " + player.getCurrentMac() + " (Registered MAC: " + player.getRegisteredMac()
                            + ").");
                    bf.newLine();
                }

            } catch (IOException ignored) {
                Logger.getGlobal().error("Failed 'handleIP(player)'", ignored);
            }
        });
    }
    
    public static void logFailedMac(String username, String mac, String ip) {
        CoresManager.getServiceProvider().executeNow(() -> {
            try {
                String FILE_PATH = Settings.LOGS_PATH + "/FailedMac.txt";
                DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
                Calendar cal = Calendar.getInstance();
                try (BufferedWriter bf = newLogWriter(FILE_PATH)) {
                    bf.write("[" + dateFormat.format(cal.getTime()) + "]: Username: [" + username
                            + "] - Mac Address: [" + mac + "] - IP Address: [" +ip+ "].");
                    bf.newLine();
                }

            } catch (IOException ignored) {
                Logger.getGlobal().error("Failed 'handleIP(player)'", ignored);
            }
        });
    }

    /**
     * Logs the transfer of Ataraxia Dollars from sender to receiver.
     *
     * @param sender the player sending the Ataraxia Dollars.
     * @param receiver the player receiving the Ataraxia Dollars.
     * @param transferAmount the amount of Ataraxia Dollars being transferred from player to player.
     */
    public static void logAtaraxiaDollarTransfer(Player sender, Player receiver, int transferAmount) {
        if (Settings.DEBUG) {
            return;
        }
        if (Settings.SQL_LOGGING) {
            Item dollars = new Item(AtaraxiaDollarsTransferD.ATARAXIA_DOLLARS_ID, transferAmount);
            LoggingSqlManager.getInstance().queueTask(new TradeLogSql(sender.getUsername(),
                    sender.getDisplayName(),
                    sender.getIP(),
                    receiver.getUsername(),
                    receiver.getDisplayName(),
                    receiver.getIP(),
                    ImmutableList.of(dollars)));
        } else {
            final Player[] participants = {sender, receiver};
            CoresManager.getServiceProvider().executeNow(() -> {
                try {
                    String FILE_PATH = Settings.LOGS_PATH + "/tradelogs/";
                    DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
                    Calendar cal = Calendar.getInstance();
                    World.sendWorldMessage(Colors.RED + "[Staff Broadcast]: " + sender.getDisplayName() + " transferred " + receiver.getDisplayName() + " " + transferAmount + " Ataraxia Dollar" + (transferAmount > 1 ? "s." : "."), true);
                    for (Player player : participants) {
                        try (BufferedWriter writer = newLogWriter(FILE_PATH + player.getUsername() + ".txt")) {
                            writer.write("[Transfer session started]");
                            writer.newLine();
                            writer.write("Time: [" + dateFormat.format(cal.getTime()) + "]");
                            writer.newLine();
                            writer.write("Sender Information: Username: " + sender.getUsername() + ". IP: " + sender.getIP() + ". Current Mac: " + sender.getCurrentMac() + ". Location: " + sender.getX() + ", " + sender.getY() + ", " + sender.getPlane() + ".");
                            writer.newLine();
                            writer.write("Receiver Information: Username: " + receiver.getUsername() + ". IP: " + receiver.getLastIP() + ". Current Mac: " + receiver.getCurrentMac() + ". Location: " + receiver.getX() + ", " + receiver.getY() + ", " + receiver.getPlane() + ".");
                            writer.newLine();
                            writer.write(sender.getUsername() + " transferred " + transferAmount + " Ataraxia Dollar" + (transferAmount > 1 ? "s" : "") + " to " + receiver.getUsername() + ".");
                            writer.newLine();
                            writer.write("[Transfer session ended]");
                            writer.newLine();
                        }
                    }
                } catch (IOException e) {
                    Logger.getGlobal().catching(e);
                }
            });
        }
    }

    public static void logStaffSession(Player staffMember) {
        if (Settings.DEBUG || !staffMember.isStaff()) {
            return;
        }
        CoresManager.getServiceProvider().executeNow(() -> {
            try {
                String FILE_PATH = Settings.LOGS_PATH + "/staffsessionlogs/";
                Path fp = Paths.get(FILE_PATH).resolve(staffMember.getUsername() + ".txt");
                DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
                Calendar cal = Calendar.getInstance();
                try (BufferedWriter writer = newLogWriter(fp.toString())) {
                    writer.write("[" + dateFormat.format(cal.getTime()) + " - " + staffMember.getIP() + "]: " + "Session lasted: " + Utils.getTimePlayed(staffMember.getRecordedPlayTime()) + ".");
                    writer.newLine();
                }
            } catch (IOException e) {
                Logger.getGlobal().catching(e);
            }
        });
    }
}
