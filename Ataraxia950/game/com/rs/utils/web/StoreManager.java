/*
*
*
* THIS CLASS IS CURRENTLY UNUSED, DO NOT MAKE UNNECESSARY EDITS TO IT
* Don't follow in my footsteps
* Author: Jaedmo
*
*
*/
package com.rs.utils.web;

import com.rs.Settings;
import com.rs.game.item.Item;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.utils.Colors;
import com.rs.utils.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Used to handle BMT Micro payments.
 *
 * @author Noel.
 */
public class StoreManager implements Runnable {

    /**
     * The player donated.
     */
    private final Player player;

    /**
     * The connection.
     */
    private Connection con;

    /**
     * If is connected to database.
     */
    private boolean isConnected = false;

    /**
     * Database connection data.
     */
    private final String host = System.getProperty("ataraxia.store.host", "");
    private final String user = System.getProperty("ataraxia.store.user", "");
    private final String password = System.getProperty("ataraxia.store.password", "");
    private final String db = System.getProperty("ataraxia.store.db", "");

    /**
     * Creates it.
     *
     * @param player The player.
     */
    public StoreManager(Player player) {
        this.player = player;
    }

    /**
     * Runs the connection.
     */
    @Override
    public void run() {
        if (player == null)
            return;
        if (!Settings.SQL_ENABLED || host.isEmpty() || user.isEmpty() || password.isEmpty() || db.isEmpty()) {
            player.getDialogueManager().startDialogue("SimpleMessage",
                    "Your requested process could not be handled at this time; please try again later");
            return;
        }
        player.getDialogueManager().startDialogue("SimpleMessage", "Checking for rewards, this might take a while...");
        if (!this.isConnected())
            connect();

        String username = player.getUsername().toLowerCase().replaceAll(" ", "_");
        String query = "SELECT `pid` FROM `donations` WHERE `username` = ? and `claimed`= 0 LIMIT 1";

        try {
            ResultSet rs = query(query, new char[]{'s'}, new Object[]{username}, null);
            if (rs.next()) {
                String pid = rs.getString("pid");
                query("UPDATE `donations` SET `claimed` = 1 WHERE `username` = ? and `pid`= ? and `claimed`= 0",
                        new char[]{'s'}, new Object[]{username}, pid);
                award(username, Integer.parseInt(pid));
                return;
            }
        /*   try {
                finalize();
            } catch (Throwable e) {
                Logger.getGlobal().catching(e);
            }*/
        } catch (Exception e) {
            // Logger.getGlobal().catching(e);
            Logger.getGlobal().error("StoreManager ~ ERROR CONNECTING, Disable SQL to prevent lag..", e);
            player.sendMessage("ERROR 909. Please contact an Administrator!");
        }
        player.getInterfaceManager().closeChatBoxInterface();
        player.sendMessage("You have not purchased anything; type ;;store do so now!");
    }

    /**
     * The query results.
     *
     * @param query the Query to run.
     * @param type The type.
     * @param value The value.
     * @return stops.
     * @throws Exception throws if can't run query.
     */
    public ResultSet query(String query, char[] type, Object[] value, String pid) throws Exception {
        PreparedStatement ps = con.prepareStatement(query);
        if (pid != null)
            ps.setString(2, pid);
        if (type == null || value == null) {
            ps.executeQuery();
            return null;
        }

        for (int i = 0; i < type.length; i++) {
            if (type[i] == 's') {
                ps.setString((i + 1), value[i].toString());
            } else if (type[i] == 'i') {
                ps.setInt((i + 1), Integer.parseInt(value[i].toString()));
            } else if (type[i] == 'l') {
                ps.setLong((i + 1), Long.parseLong(value[i].toString()));
            } else if (type[i] == 'd') {
                ps.setDouble((i + 1), Double.parseDouble(value[i].toString()));
            }
        }

        if (query.toLowerCase().startsWith("select")) {
            ResultSet rs = ps.executeQuery();
            return rs;
        }
        ps.executeUpdate();
        return null;
    }

    public void award(String username, int pid) {
        switch (pid) {
            case 1158000000:
                player.getPerkManager().unlockPerk(DonationPerk.BANK_COMMAND);
                player.handleDonation(20, "Bank Command");
                player.sendMessage("You've purchased: [" + Colors.RED + "Bank Command</col>]. "
                        + "Type ;;perks to see all your game perks.");
                break;
            case 1158000001:
                player.getPerkManager().unlockPerk(DonationPerk.ENDLESS_ENERGY);
                player.handleDonation(5, "Endless Energy");
                player.sendMessage("You've purchased: [" + Colors.RED + "Endless Energy</col>]. "
                        + "Type ;;perks to see all your game perks.");
                break;
            case 1158000002:
                player.getPerkManager().unlockPerk(DonationPerk.GREEN_THUMB);
                player.handleDonation(5, "Green Thumb");
                player.sendMessage("You've purchased: [" + Colors.RED + "Green Thumb</col>]. "
                        + "Type ;;perks to see all your game perks.");
                break;
            case 1158000003:
	/*		player.getPerkManager().birdMan = true;
			player.handleDonation(1, "Bird Man");
			player.sendMessage("You've purchased: [" + Colors.RED + "Bird Man</col>]. "
					+ "Type ;;perks to see all your game perks.");
			break;*/
            case 1158000004:
                player.getPerkManager().unlockPerk(DonationPerk.LUMBER_LEGEND);
                player.handleDonation(6, "Lumber Legend");
                player.sendMessage("You've purchased: [" + Colors.RED + "Lumber Legend</col>]. "
                        + "Type ;;perks to see all your game perks.");
                break;
            case 1158000005:
                player.getPerkManager().unlockPerk(DonationPerk.SLEIGHT_OF_HAND);
                player.handleDonation(6, "Sleight of Hand");
                player.sendMessage("You've purchased: [" + Colors.RED + "Sleight of Hand</col>]. "
                        + "Type ;;perks to see all your game perks.");
                break;
            case 1158000006:
                player.getPerkManager().unlockPerk(DonationPerk.FAMILIAR_EXPERT);
                player.handleDonation(5, "Familiar Expert");
                player.sendMessage("You've purchased: [" + Colors.RED + "Familiar Expert</col>]. "
                        + "Type ;;perks to see all your game perks.");
                break;
            case 1158000007:
                player.getPerkManager().unlockPerk(DonationPerk.CHARGE_BEFRIENDER);
                player.handleDonation(25, "Charge Befriender");
                player.sendMessage("You've purchased: [" + Colors.RED + "Charge Befriender</col>]. "
                        + "Type ;;perks to see all your game perks.");
                break;
            case 1158000008:
                player.getPerkManager().unlockPerk(DonationPerk.TREASURE_GOBLIN);
                player.handleDonation(5, "Treasure Goblin");
                player.sendMessage("You've purchased: [" + Colors.RED + "Charm Collector</col>]. "
                        + "Type ;;perks to see all your game perks.");
                break;
            case 1158000009:
                player.getPerkManager().unlockPerk(DonationPerk.HERBIVORE);
                player.handleDonation(10, "Herbivore");
                player.sendMessage("You've purchased: [" + Colors.RED + "Herbivore</col>]. "
                        + "Type ;;perks to see all your game perks.");
                break;
            case 1158000010:
                player.getPerkManager().unlockPerk(DonationPerk.MASTER_FISHERMAN);
                player.handleDonation(5, "Master Fisherman");
                player.sendMessage("You've purchased: [" + Colors.RED + "Master Fisherman</col>]. "
                        + "Type ;;perks to see all your game perks.");
                break;
            case 1158000011:
                player.getPerkManager().unlockPerk(DonationPerk.DELICATE_CRAFTSMAN);
                player.handleDonation(3, "Delicate Craftsman");
                player.sendMessage("You've purchased: [" + Colors.RED + "Delicate Craftsman</col>]. "
                        + "Type ;;perks to see all your game perks.");
                break;
            case 1158000012:
                player.getPerkManager().unlockPerk(DonationPerk.TREASURE_GOBLIN);
                player.handleDonation(5, "Treasure Goblin");
                player.sendMessage("You've purchased: [" + Colors.RED + "Coin Collector</col>]. "
                        + "Type ;;perks to see all your game perks.");
                break;
            case 1158000013:
                player.getPerkManager().unlockPerk(DonationPerk.PRAYER_BETRAYER);
                player.handleDonation(6, "Prayer Betrayer");
                player.sendMessage("You've purchased: [" + Colors.RED + "Prayer Betrayer</col>]. "
                        + "Type ;;perks to see all your game perks.");
                break;
            case 1158000014:
                player.getPerkManager().unlockPerk(DonationPerk.AVAS_SECRET);
                player.handleDonation(3, "Avas Secret");
                player.sendMessage("You've purchased: [" + Colors.RED + "Avas Secret</col>]. "
                        + "Type ;;perks to see all your game perks.");
                break;
            case 1158000015:
                player.getPerkManager().unlockPerk(DonationPerk.KEY_EXPERT);
                player.handleDonation(10, "Key Expert");
                player.sendMessage("You've purchased: [" + Colors.RED + "Key Expert</col>]. "
                        + "Type ;;perks to see all your game perks.");
                break;
            case 1158000016:
                player.getPerkManager().unlockPerk(DonationPerk.DRAGON_TRAINER);
                player.handleDonation(5, "Dragon Trainer");
                player.sendMessage("You've purchased: [" + Colors.RED + "Dragon Trainer</col>]. "
                        + "Type ;;perks to see all your game perks.");
                break;
            case 1158000017:
                player.getPerkManager().unlockPerk(DonationPerk.GWD_SPECIALIST);
                player.handleDonation(5, "GWD Specialist");
                player.sendMessage("You've purchased: [" + Colors.RED + "GWD Specialist</col>]. "
                        + "Type ;;perks to see all your game perks.");
                break;
            case 1158000018:
                player.getPerkManager().unlockPerk(DonationPerk.DUNGEONS_MASTER);
                player.handleDonation(5, "Dungeon Master");
                player.sendMessage("You've purchased: [" + Colors.RED + "Dungeons Master</col>]. "
                        + "Type ;;perks to see all your game perks.");
                break;
            case 1158000019:
                player.getPerkManager().unlockPerk(DonationPerk.PETSCHANTER);
                player.handleDonation(6, "Petchanter");
                player.sendMessage("You've purchased: [" + Colors.RED + "Petchanter</col>]. "
                        + "Type ;;perks to see all your game perks.");
                break;
            case 1158000020:
                player.getPerkManager().unlockPerk(DonationPerk.PERSLAYSION);
                player.handleDonation(15, "Perslaysion");
                player.sendMessage("You've purchased: [" + Colors.RED + "Per'slay'sion</col>]. "
                        + "Type ;;perks to see all your game perks.");
                break;
            case 1158000021:
                player.getPerkManager().unlockPerk(DonationPerk.OVERCLOCKED);
                player.handleDonation(10, "Overclocked");
                player.sendMessage("You've purchased: [" + Colors.RED + "Overclocked</col>]. "
                        + "Type ;;perks to see all your game perks.");
                break;
            case 1158000022:
                player.getPerkManager().unlockPerk(DonationPerk.ELF__S_FRIEND);
                player.handleDonation(10, "Elf's Friend");
                player.sendMessage("You've purchased: [" + Colors.RED + "Elf's Friend</col>]. "
                        + "Type ;;perks to see all your game perks.");
                break;
            case 1158000023:
                player.getPerkManager().unlockPerk(DonationPerk.CONVINCING_COOK);
                player.handleDonation(5, "Convincing Cook");
                player.sendMessage("You've purchased: [" + Colors.RED + "Convincing Cook</col>]. "
                        + "Type ;;perks to see all your game perks.");
                break;
            case 1158000096:
                player.getPerkManager().unlockPerk(DonationPerk.THE_DISCOUNTER);
                player.handleDonation(15, "The Discounter");
                player.sendMessage("You've purchased: [" + Colors.RED + "The Discounter</col>]. "
                        + "Type ;;perks to see all your game perks.");
                break;
            case 1158000097:
                player.getPerkManager().unlockPerk(DonationPerk.THE_SKIPPER);
                player.handleDonation(15, "The Skipper");
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "The Skipper</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case 1158000098:
                player.getPerkManager().unlockPerk(DonationPerk.SOUL_SIPHONER);
                player.handleDonation(15, "Soul Siphoner");
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Soul Siphoner</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case 1158000099:
                player.getPerkManager().unlockPerk(DonationPerk.THE_BOXER);
                player.handleDonation(20, "The Boxer");
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "The Boxer</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case 1158000100:
                player.getPerkManager().unlockPerk(DonationPerk.FAVORED_FAMILIARS);
                player.handleDonation(10, "Favoured Familiars");
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Favoured Familiars</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case 1158000101:
                player.getPerkManager().unlockPerk(DonationPerk.AUBURY__S_APPRENTICE);
                player.handleDonation(10, "Aubury's Apprentice");
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Aubury's Apprentice</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case 1158000102:
                player.getPerkManager().unlockPerk(DonationPerk.THE_STARGAZER);
                player.handleDonation(7, "The Stargazer");
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "The Stargazer</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case 1158000103:
                player.getPerkManager().unlockPerk(DonationPerk.ARCANE_ALCHEMIST);
                player.handleDonation(5, "Arcane Alchemist");
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Arcane Alchemist</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case 1158000104:
                player.getPerkManager().unlockPerk(DonationPerk.DROP_CATCHER);
                player.handleDonation(20, "Drop Catcher");
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Drop Catcher</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case 1158000105:
                player.getPerkManager().unlockPerk(DonationPerk.DOMINION_DOMINATION);
                player.handleDonation(15, "Dominion Domination");
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Dominion Domination</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case 1158000106:
                player.getPerkManager().unlockPerk(DonationPerk.THE_EXTERMINATOR);
                player.handleDonation(7, "The Exterminator");
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "The Exterminator</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case 1158000107:
                player.getPerkManager().unlockPerk(DonationPerk.SKILLING_ADDICT);
                player.handleDonation(7, "Skilling Addict");
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Skilling Addict</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case 1158000108:
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "3 Ataraxia dollars</col>].");
                player.getInventory().addItem(new Item(41430, 5));
                break;
            case 1158000109:
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "15 Ataraxia dollars</col>].");
                player.getInventory().addItem(new Item(41430, 15));
                break;
            case 1158000110:
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "25 Ataraxia dollars</col>].");
                player.getInventory().addItem(new Item(41430, 25));
                break;
            case 1158000111:
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "50 Ataraxia dollars</col>].");
                player.getInventory().addItem(new Item(41430, 50));
                break;
            case 1158000112:
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "75 Ataraxia dollars</col>].");
                player.getInventory().addItem(new Item(41430, 75));
                break;
            case 1158000113:
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "100 Ataraxia dollars</col>].");
                player.getInventory().addItem(new Item(41430, 100));
                break;
            case 1158000114:
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "250 Ataraxia dollars</col>].");
                player.getInventory().addItem(new Item(41430, 250));
                break;
            case 1158000115:
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "500 Ataraxia dollars</col>].");
                player.getInventory().addItem(new Item(41430, 500));
                break;
            case 1158000032:
                player.getPerkManager().unlockPerk(DonationPerk.GREEN_THUMB);
                player.getPerkManager().unlockPerk(DonationPerk.LUMBER_LEGEND);
                player.getPerkManager().unlockPerk(DonationPerk.SLEIGHT_OF_HAND);
                player.getPerkManager().unlockPerk(DonationPerk.HERBIVORE);
                player.getPerkManager().unlockPerk(DonationPerk.MASTER_FISHERMAN);
                player.getPerkManager().unlockPerk(DonationPerk.DELICATE_CRAFTSMAN);
                player.getPerkManager().unlockPerk(DonationPerk.FAMILIAR_EXPERT);
                player.getPerkManager().unlockPerk(DonationPerk.PRAYER_BETRAYER);
                player.getPerkManager().unlockPerk(DonationPerk.AVAS_SECRET);
                player.getPerkManager().unlockPerk(DonationPerk.DRAGON_TRAINER);
                player.getPerkManager().unlockPerk(DonationPerk.GWD_SPECIALIST);
                player.getPerkManager().unlockPerk(DonationPerk.DUNGEONS_MASTER);
                player.getPerkManager().unlockPerk(DonationPerk.PERSLAYSION);
                player.getPerkManager().unlockPerk(DonationPerk.THE_DISCOUNTER);
                player.getPerkManager().unlockPerk(DonationPerk.TREASURE_GOBLIN);
                player.getPerkManager().unlockPerk(DonationPerk.KEY_EXPERT);
                player.getPerkManager().unlockPerk(DonationPerk.PETSCHANTER);
                player.getPerkManager().unlockPerk(DonationPerk.BANK_COMMAND);
                player.getPerkManager().unlockPerk(DonationPerk.ENDLESS_ENERGY);
                player.getPerkManager().unlockPerk(DonationPerk.ELF__S_FRIEND);
                player.getPerkManager().unlockPerk(DonationPerk.OVERCLOCKED);
                player.getPerkManager().unlockPerk(DonationPerk.CONVINCING_COOK);
                player.getPerkManager().unlockPerk(DonationPerk.DEDICATED_DIVINATION);
                player.getPerkManager().unlockPerk(DonationPerk.CHARGE_BEFRIENDER);
                player.getPerkManager().unlockPerk(DonationPerk.MASTER_MINER);
                player.getPerkManager().unlockPerk(DonationPerk.THE_MINI___GAMER);
                player.getPerkManager().unlockPerk(DonationPerk.HUNTSMAN);
                player.getPerkManager().unlockPerk(DonationPerk.MASTER_FLEDGER);
                player.getPerkManager().unlockPerk(DonationPerk.THE_PYROMANIAC);
                player.getPerkManager().unlockPerk(DonationPerk.PORTS_MASTER);
                player.getPerkManager().unlockPerk(DonationPerk.AUBURY__S_APPRENTICE);
                player.getPerkManager().unlockPerk(DonationPerk.FAVORED_FAMILIARS);
                player.getPerkManager().unlockPerk(DonationPerk.THE_BOXER);
                player.getPerkManager().unlockPerk(DonationPerk.SOUL_SIPHONER);
                player.getPerkManager().unlockPerk(DonationPerk.ARCANE_ALCHEMIST);
                player.getPerkManager().unlockPerk(DonationPerk.DUNGEON_ARCHITECT);
                player.getPerkManager().unlockPerk(DonationPerk.DROP_CATCHER);
                player.getPerkManager().unlockPerk(DonationPerk.THE_SKIPPER);
                player.getPerkManager().unlockPerk(DonationPerk.THE_EXTERMINATOR);
                player.getPerkManager().unlockPerk(DonationPerk.DOMINION_DOMINATION);
                player.getPerkManager().unlockPerk(DonationPerk.INVESTIGATOR);
                player.getPerkManager().unlockPerk(DonationPerk.DIVINE_DOUBLER);
                player.getPerkManager().unlockPerk(DonationPerk.IMBUED_FOCUS);
                player.getPerkManager().unlockPerk(DonationPerk.ALCHEMIC_SMITHING);
                player.getPerkManager().unlockPerk(DonationPerk.THE_STARGAZER);
                player.getPerkManager().unlockPerk(DonationPerk.SKILLING_ADDICT);
                player.getPerkManager().unlockPerk(DonationPerk.TREE_HUNTER);
                player.getPerkManager().unlockPerk(DonationPerk.PET_TRAINER);
                player.handleDonation(250, "Complete perk pack");
                player.sendMessage("You've purchased: [" + Colors.RED + "Complete perk pack</col>]. "
                        + "Type ;;perks to see all your game perks.");
                break;
            case 1158000028:
                player.getPerkManager().unlockPerk(DonationPerk.TREASURE_GOBLIN);
                player.getPerkManager().unlockPerk(DonationPerk.KEY_EXPERT);
                player.getPerkManager().unlockPerk(DonationPerk.PETSCHANTER);
                player.getPerkManager().unlockPerk(DonationPerk.THE_BOXER);
                player.getPerkManager().unlockPerk(DonationPerk.DROP_CATCHER);
                player.getPerkManager().unlockPerk(DonationPerk.THE_EXTERMINATOR);
                player.handleDonation(30, "Looters perk pack");
                player.sendMessage("You've purchased: [" + Colors.RED + "Looters perk pack</col>]. "
                        + "Type ;;perks to see all your game perks.");
                break;
            case 1158000031:
                player.getPerkManager().unlockPerk(DonationPerk.FAMILIAR_EXPERT);
                player.getPerkManager().unlockPerk(DonationPerk.PRAYER_BETRAYER);
                player.getPerkManager().unlockPerk(DonationPerk.AVAS_SECRET);
                player.getPerkManager().unlockPerk(DonationPerk.PORTS_MASTER);
                player.getPerkManager().unlockPerk(DonationPerk.DRAGON_TRAINER);
                player.getPerkManager().unlockPerk(DonationPerk.GWD_SPECIALIST);
                player.getPerkManager().unlockPerk(DonationPerk.CHARGE_BEFRIENDER);
                player.getPerkManager().unlockPerk(DonationPerk.DUNGEON_ARCHITECT);
                player.getPerkManager().unlockPerk(DonationPerk.DUNGEONS_MASTER);
                player.getPerkManager().unlockPerk(DonationPerk.TREASURE_GOBLIN);
                player.getPerkManager().unlockPerk(DonationPerk.KEY_EXPERT);
                player.getPerkManager().unlockPerk(DonationPerk.PETSCHANTER);
                player.getPerkManager().unlockPerk(DonationPerk.THE_PYROMANIAC);
                player.getPerkManager().unlockPerk(DonationPerk.PORTS_MASTER);
                player.getPerkManager().unlockPerk(DonationPerk.AUBURY__S_APPRENTICE);

                player.handleDonation(110, "Combatants perk pack");
                player.sendMessage("You've purchased: [" + Colors.RED + "Combatants perk pack</col>]. "
                        + "Type ;;perks to see all your game perks.");
                break;
            case 1158000030:
                player.getPerkManager().unlockPerk(DonationPerk.GREEN_THUMB);
                player.getPerkManager().unlockPerk(DonationPerk.LUMBER_LEGEND);
                player.getPerkManager().unlockPerk(DonationPerk.SLEIGHT_OF_HAND);
                player.getPerkManager().unlockPerk(DonationPerk.HERBIVORE);
                player.getPerkManager().unlockPerk(DonationPerk.MASTER_FISHERMAN);
                player.getPerkManager().unlockPerk(DonationPerk.DELICATE_CRAFTSMAN);
                player.getPerkManager().unlockPerk(DonationPerk.CONVINCING_COOK);
                player.getPerkManager().unlockPerk(DonationPerk.DEDICATED_DIVINATION);
                player.getPerkManager().unlockPerk(DonationPerk.MASTER_MINER);
                player.getPerkManager().unlockPerk(DonationPerk.MASTER_FLEDGER);
                player.getPerkManager().unlockPerk(DonationPerk.THE_PYROMANIAC);
                player.getPerkManager().unlockPerk(DonationPerk.HUNTSMAN);
                player.getPerkManager().unlockPerk(DonationPerk.DIVINE_DOUBLER);
                player.getPerkManager().unlockPerk(DonationPerk.ALCHEMIC_SMITHING);
                player.getPerkManager().unlockPerk(DonationPerk.IMBUED_FOCUS);
                player.getPerkManager().unlockPerk(DonationPerk.ELF__S_FRIEND);
                player.handleDonation(80, "Skillers perk pack");
                player.sendMessage("You've purchased: [" + Colors.RED + "Skillers perk pack</col>]. "
                        + "Type ;;perks to see all your game perks.");
                break;
            case 1158000029:
                player.getPerkManager().unlockPerk(DonationPerk.BANK_COMMAND);
                player.getPerkManager().unlockPerk(DonationPerk.ENDLESS_ENERGY);
                player.getPerkManager().unlockPerk(DonationPerk.OVERCLOCKED);
                player.getPerkManager().unlockPerk(DonationPerk.ELF__S_FRIEND);
                player.getPerkManager().unlockPerk(DonationPerk.THE_MINI___GAMER);
                player.getPerkManager().unlockPerk(DonationPerk.THE_DISCOUNTER);
                player.getPerkManager().unlockPerk(DonationPerk.PORTS_MASTER);
                player.getPerkManager().unlockPerk(DonationPerk.ARCANE_ALCHEMIST);
                player.getPerkManager().unlockPerk(DonationPerk.DROP_CATCHER);
                player.getPerkManager().unlockPerk(DonationPerk.THE_SKIPPER);
                player.handleDonation(90, "Utility perk pack");
                player.sendMessage("You've purchased: [" + Colors.RED + "Utility perk pack</col>]. "
                        + "Type ;;perks to see all your game perks.");
                break;
            case 1158000024:
                player.getPerkManager().unlockPerk(DonationPerk.DEDICATED_DIVINATION);
                player.handleDonation(10, "Dedicated Divination");
                player.sendMessage("You've purchased: [" + Colors.RED + "Dedicated Divination</col>]. "
                        + "Type ;;perks to see all your game perks.");
                break;
            case 1158000025:
                player.getPerkManager().unlockPerk(DonationPerk.MASTER_MINER);
                player.handleDonation(5, "Master Miner");
                player.sendMessage("You've purchased: [" + Colors.RED + "Master Miner</col>]. "
                        + "Type ;;perks to see all your game perks.");
                break;
            case 1158000026:
                player.getPerkManager().unlockPerk(DonationPerk.THE_MINI___GAMER);
                player.handleDonation(5, "The Mini-Gamer");
                player.sendMessage("You've purchased: [" + Colors.RED + "The Mini-Gamer</col>]. "
                        + "Type ;;perks to see all your game perks.");
                break;
            case 1158000027:
                player.getPerkManager().unlockPerk(DonationPerk.MASTER_FLEDGER);
                player.handleDonation(6, "Master Fledger");
                player.sendMessage("You've purchased: [" + Colors.RED + "Master Fledger</col>]. "
                        + "Type ;;perks to see all your game perks.");
                break;
            case 1158000034:
                player.getTreasureHunter().giveBoughtKeys(5);
                player.handleDonation(2, "x5 Treasure hunter Keys");
                player.sendMessage("You've purchased: [" + Colors.RED + "x5 Treasure hunter Keys</col>]. "
                        + "Open the Squeal of Fortune tab to use them.");
                break;
            case 1158000035:
                player.getTreasureHunter().giveBoughtKeys(27);
                player.handleDonation(10, "x25 Treasure hunter Keys");
                player.sendMessage("You've purchased: [" + Colors.RED + "x27 Treasure hunter Keys</col>]. "
                        + "Open the Squeal of Fortune tab to use them.");
                break;
            case 1158000036:
                player.getTreasureHunter().giveBoughtKeys(55);
                player.handleDonation(20, "x50 Treasure hunter Keys");
                player.sendMessage("You've purchased: [" + Colors.RED + "x55 Treasure hunter Keys</col>]. "
                        + "Open the Squeal of Fortune tab to use them.");
                break;
            case 1158000037:
                player.getTreasureHunter().giveBoughtKeys(175);
                player.handleDonation(50, "x150 Treasure hunter Keys");
                player.sendMessage("You've purchased: [" + Colors.RED + "x175 Treasure hunter Keys</col>]. "
                        + "Open the Squeal of Fortune tab to use them.");
                break;
            case 1158000038:
                player.getTreasureHunter().giveBoughtKeys(350);
                player.handleDonation(100, "x300 Treasure hunter Keys");
                player.sendMessage("You've purchased: [" + Colors.RED + "x350 Treasure hunter Keys</col>]. "
                        + "Open the Squeal of Fortune tab to use them.");
                break;
            case 1158000039:
                player.getPerkManager().unlockPerk(DonationPerk.HUNTSMAN);
                player.handleDonation(6, "Huntsman");
                player.sendMessage("You've purchased: [" + Colors.RED + "Huntsman</col>]. "
                        + "Type ;;perks to see all your game perks.");
                break;
            case 1158000033:
                player.getPerkManager().unlockPerk(DonationPerk.THE_PYROMANIAC);
                player.handleDonation(10, "thePyromaniac");
                player.sendMessage("You've purchased: [" + Colors.RED + "The Pyromaniac</col>]. "
                        + "Type ;;perks to see all your game perks.");
                break;
            case 1158000040:
                player.getOverrides().paladin = true;
                player.handleDonation(5, "Paladin Override");
                player.sendMessage("You've purchased: [" + Colors.RED + "Paladin Override</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000041:
                player.getOverrides().warlord = true;
                player.handleDonation(5, "Warlord Override");
                player.sendMessage("You've purchased: [" + Colors.RED + "Warlord Override</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000042:
                player.getOverrides().obsidian = true;
                player.handleDonation(5, "Obsidian Override");
                player.sendMessage("You've purchased: [" + Colors.RED + "Obsidian Override</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000043:
                player.getOverrides().kalphite = true;
                player.handleDonation(5, "Kalphite Override");
                player.sendMessage("You've purchased: [" + Colors.RED + "Kalphite Override</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000044:
                player.getOverrides().demonflesh = true;
                player.handleDonation(5, "Demonflesh Override");
                player.sendMessage("You've purchased: [" + Colors.RED + "Demonflesh Override</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000045:
                player.getOverrides().remokee = true;
                player.handleDonation(5, "Remokee Override");
                player.sendMessage("You've purchased: [" + Colors.RED + "Remokee Override</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000046:
                player.getOverrides().assassin = true;
                player.handleDonation(5, "Assassin Override");
                player.sendMessage("You've purchased: [" + Colors.RED + "Assassin Override</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000047:
                player.getOverrides().skeleton = true;
                player.handleDonation(5, "Skeleton Override");
                player.sendMessage("You've purchased: [" + Colors.RED + "Skeleton Override</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000048:
                player.getOverrides().goth = true;
                player.handleDonation(5, "Goth Override");
                player.sendMessage("You've purchased: [" + Colors.RED + "Goth Override</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000049:
                player.getOverrides().mummy = true;
                player.handleDonation(5, "Mummy Override");
                player.sendMessage("You've purchased: [" + Colors.RED + "Mummy Override</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000050:
                player.getOverrides().replicaDragon = true;
                player.handleDonation(5, "Replica Dragon Override");
                player.sendMessage("You've purchased: [" + Colors.RED + "Replicate Dragon Override</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000051:
                player.getOverrides().sentinel = true;
                player.handleDonation(5, "Sentinel Override");
                player.sendMessage("You've purchased: [" + Colors.RED + "Sentinel Override</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000052:
                player.getOverrides().reaver = true;
                player.handleDonation(5, "Reaver Override");
                player.sendMessage("You've purchased: [" + Colors.RED + "Reaver Override</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000053:
                player.getOverrides().hiker = true;
                player.handleDonation(5, "Hiker Override");
                player.sendMessage("You've purchased: [" + Colors.RED + "Hiker Override</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000054:
                player.getOverrides().skyguard = true;
                player.handleDonation(5, "Skyguard Override");
                player.sendMessage("You've purchased: [" + Colors.RED + "Skyguard Override</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000055:
                player.getOverrides().vyrewatch = true;
                player.handleDonation(5, "Vyrewatch Override");
                player.sendMessage("You've purchased: [" + Colors.RED + "Vyrewatch Override</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000056:
                player.getOverrides().snowman = true;
                player.handleDonation(5, "Snowman Override");
                player.sendMessage("You've purchased: [" + Colors.RED + "Snowman Override</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000057:
                player.getOverrides().samurai = true;
                player.handleDonation(5, "Samurai Override");
                player.sendMessage("You've purchased: [" + Colors.RED + "Samurai Override</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000058:
                player.getOverrides().warmWinter = true;
                player.handleDonation(5, "Warm Winter Override");
                player.sendMessage("You've purchased: [" + Colors.RED + "Warm Winter Override</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000059:
                player.getOverrides().darkLord = true;
                player.handleDonation(5, "Dark Lord Override");
                player.sendMessage("You've purchased: [" + Colors.RED + "Dark Lord Override</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000060:
                player.getAnimations().hasBattleCry = true;
                player.handleDonation(2, "Slayer Battle Cry");
                player.sendMessage("You've purchased: [" + Colors.RED + "Slayer Battle Cry</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000061:
                player.getAnimations().hasEnhancedPotion = true;
                player.handleDonation(2, "Enhanced Potion Making");
                player.sendMessage("You've purchased: [" + Colors.RED + "Enhanced Potion Making</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000062:
                player.getAnimations().hasLumberjackWc = true;
                player.handleDonation(2, "Lumberjack Woodcutting");
                player.sendMessage("You've purchased: [" + Colors.RED + "Lumberjack Woodcutting</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000063:
                player.getAnimations().hasDeepFishing = true;
                player.handleDonation(2, "Deep-Sea Fishing");
                player.sendMessage("You've purchased: [" + Colors.RED + "Deep-Sea Fishing</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000064:
                player.getAnimations().hasZenResting = true;
                player.handleDonation(2, "Zen Resting");
                player.sendMessage("You've purchased: [" + Colors.RED + "Zen Resting</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000065:
                player.getAnimations().hasKarateFletch = true;
                player.handleDonation(2, "Karate-Chop Fletching");
                player.sendMessage("You've purchased: [" + Colors.RED + "Karate-Chop Fletching</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000066:
                player.getAnimations().hasIronSmith = true;
                player.handleDonation(2, "Iron-Fist Smithing");
                player.sendMessage("You've purchased: [" + Colors.RED + "Iron-Fist Smithing</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000067:
                player.getAnimations().hasChiMining = true;
                player.handleDonation(2, "Chi-Blast Mining");
                player.sendMessage("You've purchased: [" + Colors.RED + "Chi-Blast Mining</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000068:
                player.getAnimations().hasSamuraiCook = true;
                player.handleDonation(2, "Samurai Cooking");
                player.sendMessage("You've purchased: [" + Colors.RED + "Samurai Cooking</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000069:
                player.getAnimations().hasRoundHouseWc = true;
                player.handleDonation(2, "Roundhouse Woodcutting");
                player.sendMessage("You've purchased: [" + Colors.RED + "Roundhouse Woodcutting</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000070:
                player.getAnimations().hasBlastMining = true;
                player.handleDonation(2, "Blast Mining");
                player.sendMessage("You've purchased: [" + Colors.RED + "Blast Mining</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000071:
                player.getAnimations().hasStrongResting = true;
                player.handleDonation(2, "Strongarm Resting");
                player.sendMessage("You've purchased: [" + Colors.RED + "Strongarm Resting</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000072:
                player.getAnimations().hasArcaneSmelt = true;
                player.handleDonation(2, "Arcane Smelting");
                player.sendMessage("You've purchased: [" + Colors.RED + "Arcane Smelting</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000073:
                player.getAnimations().hasArcaneResting = true;
                player.handleDonation(2, "Arcane Resting");
                player.sendMessage("You've purchased: [" + Colors.RED + "Arcane Resting</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000074:
                player.getAnimations().hasStrongWc = true;
                player.handleDonation(2, "Strongarm Woodcutting");
                player.sendMessage("You've purchased: [" + Colors.RED + "Strongarm Woodcutting</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000075:
                player.getAnimations().hasStrongMining = true;
                player.handleDonation(2, "Strongarm Mining");
                player.sendMessage("You've purchased: [" + Colors.RED + "Strongarm Mining</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000076:
                player.getAnimations().hasArcaneFishing = true;
                player.handleDonation(2, "Arcane Fishing");
                player.sendMessage("You've purchased: [" + Colors.RED + "Arcane Fishing</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000077:
                player.getAnimations().hasStrongBurial = true;
                player.handleDonation(2, "Strongarm Burial");
                player.sendMessage("You've purchased: [" + Colors.RED + "Strongarm Burial</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000078:
                player.getAnimations().hasArcaneCook = true;
                player.handleDonation(2, "Arcane Cooking");
                player.sendMessage("You've purchased: [" + Colors.RED + "Arcane Cooking</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000079:
                player.getAnimations().hasPowerDivination = true;
                player.handleDonation(2, "Powerful Divination");
                player.sendMessage("You've purchased: [" + Colors.RED + "Powerful Divination</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000080:
                player.getAnimations().hasPowerConversion = true;
                player.handleDonation(2, "Powerful Conversion");
                player.sendMessage("You've purchased: [" + Colors.RED + "Powerful Conversion</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000081:
                player.getAnimations().hasAgileDivination = true;
                player.handleDonation(2, "Agile Divination");
                player.sendMessage("You've purchased: [" + Colors.RED + "Agile Divination</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000082:
                player.getAnimations().hasAgileConversion = true;
                player.handleDonation(2, "Agile Conversion");
                player.sendMessage("You've purchased: [" + Colors.RED + "Agile Conversion</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000083:
                player.getAnimations().hasSinisterSlumber = true;
                player.handleDonation(2, "Sinister Slumber");
                player.sendMessage("You've purchased: [" + Colors.RED + "Sinister Slumber</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000084:
                player.getAnimations().hasArmWarrior = true;
                player.handleDonation(2, "Armchair Warrior");
                player.sendMessage("You've purchased: [" + Colors.RED + "Armchair Warrior</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000085:
                player.getAnimations().hasEneResting = true;
                player.handleDonation(2, "Energy Drain Resting");
                player.sendMessage("You've purchased: [" + Colors.RED + "Energy Drain Resting</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000086:
                player.getAnimations().hasCrystalResting = true;
                player.handleDonation(2, "Crystal Impling Resting");
                player.sendMessage("You've purchased: [" + Colors.RED + "Crystal Impling Resting</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000087:
                player.getAnimations().hasHeadMining = true;
                player.handleDonation(2, "Headbutt Mining");
                player.sendMessage("You've purchased: [" + Colors.RED + "Headbutt Mining</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000088:
                player.getAnimations().hasSandWalk = true;
                player.handleDonation(4, "Sandstorm Walk");
                player.sendMessage("You've purchased: [" + Colors.RED + "Sandstorm Walk</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000089:
                player.getAnimations().hasSadWalk = true;
                player.handleDonation(4, "Sad Walk");
                player.sendMessage(
                        "You've purchased: [" + Colors.RED + "Sad Walk</col>]. " + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000090:
                player.getAnimations().hasAngryWalk = true;
                player.handleDonation(4, "Angry Walk");
                player.sendMessage("You've purchased: [" + Colors.RED + "Angry Walk</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000091:
                player.getAnimations().hasProudWalk = true;
                player.handleDonation(4, "Proud Walk");
                player.sendMessage("You've purchased: [" + Colors.RED + "Proud Walk</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000092:
                player.getAnimations().hasHappyWalk = true;
                player.handleDonation(4, "Happy Walk");
                player.sendMessage("You've purchased: [" + Colors.RED + "Happy Walk</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000093:
                player.getAnimations().hasBarbarianWalk = true;
                player.handleDonation(4, "Barbarian Walk");
                player.sendMessage("You've purchased: [" + Colors.RED + "Barbarian Walk</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000094:
                player.getAnimations().hasRevenantWalk = true;
                player.handleDonation(4, "Revenant Walk");
                player.sendMessage("You've purchased: [" + Colors.RED + "Revenant Walk</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000095:
                player.getPerkManager().unlockPerk(DonationPerk.PORTS_MASTER);
                player.handleDonation(4, "Ports Master");
                player.sendMessage("You've purchased: [" + Colors.RED + "Ports Master</col>]. "
                        + "Type ;;perks to see all your game perks.");
                break;
            case 1158000116:
                player.getAnimations().hasSingerMining = true;
                player.handleDonation(2, "Crystal Singer Mining");
                player.sendMessage("You've purchased: [" + Colors.RED + "Crystal Singer Mining</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000117:
                player.getAnimations().hasExplosiveWc = true;
                player.handleDonation(2, "Explosive Woodcutting");
                player.sendMessage("You've purchased: [" + Colors.RED + "Explosive Woodcutting</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000118:
                player.getAnimations().hasSingerWc = true;
                player.handleDonation(2, "Crystal Singer Woodcutting");
                player.sendMessage("You've purchased: [" + Colors.RED + "Crystal Singer Woodcutting</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000119:
                player.getAnimations().hasBombFiremaking = true;
                player.handleDonation(2, "Bomb Firemaking");
                player.sendMessage("You've purchased: [" + Colors.RED + "Bomb Firemaking</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000120:
                player.getAnimations().hasPartyhatFiremaking = true;
                player.handleDonation(4, "Partyhat Firemaking");
                player.sendMessage("You've purchased: [" + Colors.RED + "Partyhat Firemaking</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000121:
                player.getAnimations().hasAncestralDivination1 = true;
                player.handleDonation(2, "Ancestral Divination I");
                player.sendMessage("You've purchased: [" + Colors.RED + "Ancestral Divination II</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000122:
                player.getAnimations().hasAncestralDivination2 = true;
                player.handleDonation(2, "Ancestral Divination II");
                player.sendMessage("You've purchased: [" + Colors.RED + "Ancestral Divination II</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000123:
                player.getAnimations().hasAncestralConversion = true;
                player.handleDonation(2, "Ancestral Conversion");
                player.sendMessage("You've purchased: [" + Colors.RED + "Ancestral Conversion</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000124:
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Pet perk - Power Exchange</col>].");
                player.handleDonation(5, "Pet perk - Power Exchange");
                player.getInventory().addItem(new Item(41471, 1));
                break;
            case 1158000125:
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Pet perk - Doubled Edge</col>].");
                player.handleDonation(5, "Pet perk - Doubled Edge");
                player.getInventory().addItem(new Item(41472, 1));
                break;
            case 1158000126:
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Pet perk - Double Trouble</col>].");
                player.handleDonation(5, "Pet perk - Double Trouble");
                player.getInventory().addItem(new Item(41473, 1));
                break;
            case 1158000127:
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Pet perk - Overloaded</col>].");
                player.handleDonation(10, "Pet perk - Overloaded");
                player.getInventory().addItem(new Item(41474, 1));
                break;
            case 1158000128:
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Pet perk - Kuradamnn</col>].");
                player.handleDonation(5, "Pet perk - Kuradamnn");
                player.getInventory().addItem(new Item(41475, 1));
                break;
            case 1158000129:
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Pet perk - Executioners Demise</col>].");
                player.handleDonation(5, "Pet perk - Executioners Demise");
                player.getInventory().addItem(new Item(41477, 1));
                break;
            case 1158000130:
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Pet perk - Nice But Dim</col>].");
                player.handleDonation(5, "Pet perk - Nice But Dim");
                player.getInventory().addItem(new Item(41478, 1));
                break;
            case 1158000131:
                player.getAnimations().hasBlinkWalk = true;
                player.handleDonation(5, "Blink Walk");
                player.sendMessage("You've purchased: [" + Colors.RED + "Blink Walk</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000132:
                player.getAnimations().hasCatWalk = true;
                player.handleDonation(5, "Cat Walk");
                player.sendMessage("You've purchased: [" + Colors.RED + "Cat Walk</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case 1158000133:
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "3x Pet perk - Power Exchange</col>].");
                player.handleDonation(15, "3x Pet perk - Power Exchange");
                player.getInventory().addItem(new Item(41471, 3));
                break;
            case 1158000134:
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "3x Pet perk - Double Trouble</col>].");
                player.handleDonation(15, "3x Pet perk - Double Trouble");
                player.getInventory().addItem(new Item(41473, 3));
                break;
            case 1158000135:
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "3x Pet perk - Overloaded</col>].");
                player.handleDonation(30, "3x Pet perk - Overloaded");
                player.getInventory().addItem(new Item(41474, 3));
                break;
            case 1158000136:
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "3x Pet perk - Kuradamnn</col>].");
                player.handleDonation(15, "3x Pet perk - Kuradamnn");
                player.getInventory().addItem(new Item(41475, 3));
                break;
            case 1158000137:
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "3x Pet perk - Executioner's Demise</col>].");
                player.handleDonation(15, "3x Pet perk - Executioner's Demise");
                player.getInventory().addItem(new Item(41477, 3));
                break;
            case 1158000138:
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "3x Pet perk - Nice But Dim</col>].");
                player.handleDonation(15, "3x Pet perk - Nice But Dim");
                player.getInventory().addItem(new Item(41478, 3));
                break;
            case 1158000139:
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Pet perk - Bundle</col>].");
                player.handleDonation(90, "Pet perk - Bundle");
                player.getInventory().addItem(new Item(41478, 3));
                player.getInventory().addItem(new Item(41477, 3));
                player.getInventory().addItem(new Item(41475, 3));
                player.getInventory().addItem(new Item(41474, 3));
                player.getInventory().addItem(new Item(41473, 3));
                player.getInventory().addItem(new Item(41471, 3));
                player.getInventory().addItem(new Item(30233, 1));
                break;
            case 1158000140:
                player.getPerkManager().unlockPerk(DonationPerk.TREE_HUNTER);
                player.handleDonation(10, "Tree Hunter");
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Tree Hunter</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case 1158000141:
                player.getPerkManager().unlockPerk(DonationPerk.DUNGEON_ARCHITECT);
                player.handleDonation(8, "Dungeon Architect");
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Dungeon Architect</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case 1158000142:
                player.getPerkManager().unlockPerk(DonationPerk.PET_TRAINER);
                player.handleDonation(10, "Pet Trainer");
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Pet Trainer</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;

            default:
                player.getInterfaceManager().closeChatBoxInterface();
                player.sendMessage("ERROR 908. Please contact an Administrator!");
                break;
        }
    }

    /**
     * Connects to the database.
     */
    public void connect() {
        if (!Settings.SQL_ENABLED || host.isEmpty() || user.isEmpty() || password.isEmpty() || db.isEmpty()) return;
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            con = DriverManager.getConnection("jdbc:com.rs.utils.mysql://" + host + "/" + db, user, password);
            isConnected = true;
        } catch (Exception e) {
            Logger.getGlobal().catching(e);
        }
    }

    /**
     * If is connected.
     *
     * @return the connection.
     */
    private boolean isConnected() {
        return isConnected;
    }
}