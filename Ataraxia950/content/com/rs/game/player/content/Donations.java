package com.rs.game.player.content;

import com.google.common.collect.ImmutableSet;
import com.rs.game.item.Item;
import com.rs.game.player.Bank;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.utils.Colors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Set;

public class Donations {

    public static final Set<Integer> perkBoxes;

    static int[][] perkBox;
    static {
        try {
            perkBox = new int[][]{{41337, 17820}, {41338, 17822}, {41339, 17827}, {41340, 17828}, {41341, 17829}, {41342, 17830}, {41343, 17831}, {41344, 17832}, {41345, 17833}, {41346, 17834},
                    {41347, 17854}, {41348, 31921}, {41349, 17846}, {41350, 17816}, {41351, 17818}, {41352, 17819}, {41353, 17823}, {41354, 17824}, {41355, 17825}, {41356, 17836}, {41357, 17837}, {41358, 17838},
                    {41359, 17839}, {41360, 17853}, {41361, 18096}, {41362, 18211}, {41363, 18841}, {41364, 18842}, {41365, 17814}, {41366, 17815}, {41367, 17835}, {41368, 17840}, {41369, 18210}, {41370, 17841},
                    {41371, 17842}, {41372, 17843}, {41373, 17844}, {41374, 17845}, {41335, 32554}, {41384, 33185}, {41385, 33187}, {41386, 33184}, {41387, 33186}, {41389, 33265}, {41390, 33520}, {41388, 33183},
                    {41391, 33829}, {41425, 34104}, {41424, 34105}, {41423, 34527}, {30085, 37142}, {48485, 37575}, {41476, 37574},
                    //GIM Perks
                    {41365, 38627}, {41366, 38628}, {41350, 38629}, {41351, 38630}, {41352, 38631}, {41338, 38632}, {41353, 38633}, {41355, 38634}, {41337, 38635},
                    {41340, 38638}, {41341, 38639}, {41342, 38646}, {41343, 38647}, {41344, 38648}, {41345, 38649}, {41346, 38650}, {41367, 38651}, {41357, 38652},
                    {41358, 38655}, {41359, 38656}, {41368, 38657}, {41349, 38658}, {41360, 38659}, {41347, 38660}, {41361, 38661}, {41369, 38662}, {41362, 38663},
                    {41364, 38664}, {41335, 38665}, {41388, 38666}, {41386, 38667}, {41384, 38668}, {41387, 38669}, {41385, 38670}, {41390, 38671}, {41425, 38672},
                    {41424, 38673}, {41423, 38674}, {41482, 38675}, {41363, 38677}, {41354, 38678}, {41549, 38679}, {41548, 38680}, {41547, 38681}, {41546, 38682},
                    {41545, 38683}};
            ImmutableSet.Builder<Integer> perkIds = ImmutableSet.builder();
            for (int i = 0; i < perkBox.length; i++) {
                perkIds.add(perkBox[i][0]);
            }
            perkBoxes = perkIds.build();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    public static void rspsdata(Player player, String username) {
        try {
            username = username.replaceAll(" ", "_");
            String key = System.getProperty("ataraxia.donations.key", "");
            String endpoint = System.getProperty("ataraxia.donations.endpoint", "");
            if (key.isEmpty() || endpoint.isEmpty()) return;
            URL url = new URL(endpoint + "?username=" + java.net.URLEncoder.encode(username, "UTF-8") + "&key=" + java.net.URLEncoder.encode(key, "UTF-8"));
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String results = reader.readLine();
            if (results.toLowerCase().contains("!error:")) {
                //Logger.getGlobal().info(this, "[GPAY-IO]" + results);
            } else {
                String[] ary = results.split(",");
                for (String pid : ary) {
                    int pidAsInt = Integer.parseInt(pid);
                    if (canGivePerkBox(pidAsInt)) {
                        givePerkBox(player, pidAsInt);
                    } else {
                        awardDonation(player, pid);
                    }
                }
            }
        } catch (IOException e) {
        }
    }

    public static void awardDonation(Player player, String id) {
        awardDonation(player, id, true);
    }

    public static void awardDonation(Player player, String id, boolean addAmount) {
        switch (id) {
            case "0":
                player.sendMessage(Colors.GREEN + "There is currently no donation for you to claim!");
                break;
            case "20425":
                player.addBank(false, new Bank());
                if (addAmount) {
                    player.handleDonation(8, "+1 Bank");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "+1 bank container</col>]."
                        + "Type ::perks to see all your game perks.");
                break;
            case "20426":
                for (int i = 0; i < 3; i++)
                    player.addBank(false, new Bank());
                if (addAmount) {
                    player.handleDonation(20, "+3 Banks");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "+3 bank containers</col>]."
                        + "Type ::perks to see all your game perks.");
                break;
            case "20427":
                for (int i = 0; i < 8; i++)
                    player.addBank(false, new Bank());
                if (addAmount) {
                    player.handleDonation(50, "+7 Banks");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "+7 bank containers</col>]."
                        + "Type ::perks to see all your game perks.");
                break;
            case "19949":
                if(player.isGroupIronman()) {
                    return;
                }
                if (player.getInventory().hasFreeSlots())
                    player.getInventory().addItem(new Item(24387, 1));
                else
                    player.getBank().addItem(new Item(24387, 1), true);
                if (addAmount) {
                    player.handleDonation(7, "Corgi");
                }
                player.sendMessage("You've purchased: [" + Colors.RCYAN + "Corgi</col>]."
                        + "Type ::perks to see all your game perks.");
                break;
            case "19950":
                if(player.isGroupIronman()) {
                    return;
                }
                if (player.getInventory().hasFreeSlots())
                    player.getInventory().addItem(new Item(37132, 1));
                else
                    player.getBank().addItem(new Item(37132, 1), true);
                if (addAmount) {
                    player.handleDonation(6, "Hype train");
                }
                player.sendMessage("You've purchased: [" + Colors.RCYAN + "Hype train</col>]."
                        + "Type ::perks to see all your game perks.");
                break;
            case "19951":
                if(player.isGroupIronman()) {
                    return;
                }
                if (player.getInventory().hasFreeSlots())
                    player.getInventory().addItem(new Item(29438, 1));
                else
                    player.getBank().addItem(new Item(29438, 1), true);
                if (addAmount) {
                    player.handleDonation(10, "Golden chinchompa");
                }
                player.sendMessage("You've purchased: [" + Colors.GOLD + "Golden chinchompa</col>]."
                        + "Type ::perks to see all your game perks.");
                break;
            case "19953":
                if(player.isGroupIronman()) {
                    return;
                }
                if (player.getInventory().hasFreeSlots())
                    player.getInventory().addItem(new Item(35908, 1));
                else
                    player.getBank().addItem(new Item(35908, 1), true);
                if (addAmount) {
                    player.handleDonation(5, "Kharidian cat");
                }
                player.sendMessage("You've purchased: [" + Colors.RCYAN + "Kharidian cat</col>]."
                        + "Type ::perks to see all your game perks.");
                break;
            case "19952":
                if(player.isGroupIronman()) {
                    return;
                }
                if (player.getInventory().hasFreeSlots())
                    player.getInventory().addItem(new Item(30380, 1));
                else
                    player.getBank().addItem(new Item(30380, 1), true);
                if (addAmount) {
                    player.handleDonation(6, "San'tar spawnling");
                }
                player.sendMessage("You've purchased: [" + Colors.RCYAN + "San'tar spawnling</col>]."
                        + "Type ::perks to see all your game perks.");
                break;

            // New Pets
            case "35602":
                if(player.isGroupIronman()) {
                    return;
                }
                if (player.getInventory().hasFreeSlots())
                    player.getInventory().addItem(new Item(30233, 1));
                else
                    player.getBank().addItem(new Item(30233, 1), true);
                if (addAmount) {
                    player.handleDonation(6, "Piggles");
                }
                player.sendMessage("You've purchased: [" + Colors.RCYAN + "Piggles</col>]."
                        + "Type ::perks to see all your game perks.");
                break;
            case "35603":
                if(player.isGroupIronman()) {
                    return;
                }
                if (player.getInventory().hasFreeSlots())
                    player.getInventory().addItem(new Item(31025, 1));
                else
                    player.getBank().addItem(new Item(31025, 1), true);
                if (addAmount) {
                    player.handleDonation(6, "Tiny Lucien");
                }
                player.sendMessage("You've purchased: [" + Colors.RCYAN + "Tiny Lucien</col>]."
                        + "Type ::perks to see all your game perks.");
                break;
            case "35604":
                if(player.isGroupIronman()) {
                    return;
                }
                if (player.getInventory().hasFreeSlots())
                    player.getInventory().addItem(new Item(33566, 1));
                else
                    player.getBank().addItem(new Item(33566, 1), true);
                if (addAmount) {
                    player.handleDonation(6, "Kipple Nano");
                }
                player.sendMessage("You've purchased: [" + Colors.RCYAN + "Kipple Nano</col>]."
                        + "Type ::perks to see all your game perks.");
                break;
            case "35605":
                if(player.isGroupIronman()) {
                    return;
                }
                if (player.getInventory().hasFreeSlots())
                    player.getInventory().addItem(new Item(34196, 1));
                else
                    player.getBank().addItem(new Item(34196, 1), true);
                if (addAmount) {
                    player.handleDonation(6, "Tiny Hazeel");
                }
                player.sendMessage("You've purchased: [" + Colors.RCYAN + "Tiny Hazeel</col>]."
                        + "Type ::perks to see all your game perks.");
                break;
            case "35606":
                if(player.isGroupIronman()) {
                    return;
                }
                if (player.getInventory().hasFreeSlots())
                    player.getInventory().addItem(new Item(34753, 1));
                else
                    player.getBank().addItem(new Item(34753, 1), true);
                if (addAmount) {
                    player.handleDonation(6, "Sayln the Shark");
                }
                player.sendMessage("You've purchased: [" + Colors.RCYAN + "Sayln the Shark</col>]."
                        + "Type ::perks to see all your game perks.");
                break;
            case "35607":
                if(player.isGroupIronman()) {
                    return;
                }
                if (player.getInventory().hasFreeSlots())
                    player.getInventory().addItem(new Item(37725, 1));
                else
                    player.getBank().addItem(new Item(37725, 1), true);
                if (addAmount) {
                    player.handleDonation(6, "Tim");
                }
                player.sendMessage("You've purchased: [" + Colors.RCYAN + "Tim</col>]."
                        + "Type ::perks to see all your game perks.");
                break;
            case "35608":
                if(player.isGroupIronman()) {
                    return;
                }
                if (player.getInventory().hasFreeSlots())
                    player.getInventory().addItem(new Item(37727, 1));
                else
                    player.getBank().addItem(new Item(37727, 1), true);
                if (addAmount) {
                    player.handleDonation(6, "Crunchy");
                }
                player.sendMessage("You've purchased: [" + Colors.RCYAN + "Crunchy</col>]."
                        + "Type ::perks to see all your game perks.");
                break;
            case "35609":
                if(player.isGroupIronman()) {
                    return;
                }
                if (player.getInventory().hasFreeSlots())
                    player.getInventory().addItem(new Item(38834, 1));
                else
                    player.getBank().addItem(new Item(38834, 1), true);
                if (addAmount) {
                    player.handleDonation(6, "Idol Crabletine");
                }
                player.sendMessage("You've purchased: [" + Colors.RCYAN + "Idol Crabletine</col>]."
                        + "Type ::perks to see all your game perks.");
                break;
            case "35610":
                if(player.isGroupIronman()) {
                    return;
                }
                if (player.getInventory().hasFreeSlots())
                    player.getInventory().addItem(new Item(39252, 1));
                else
                    player.getBank().addItem(new Item(39252, 1), true);
                if (addAmount) {
                    player.handleDonation(6, "Tiny Kharshai");
                }
                player.sendMessage("You've purchased: [" + Colors.RCYAN + "Tiny Kharshai</col>]."
                        + "Type ::perks to see all your game perks.");
                break;
            case "35611":
                if(player.isGroupIronman()) {
                    return;
                }
                if (player.getInventory().hasFreeSlots())
                    player.getInventory().addItem(new Item(39448, 1));
                else
                    player.getBank().addItem(new Item(39448, 1), true);
                if (addAmount) {
                    player.handleDonation(6, "Tiny Sliske");
                }
                player.sendMessage("You've purchased: [" + Colors.RCYAN + "Tiny Sliske</col>]."
                        + "Type ::perks to see all your game perks.");
                break;
            case "35612":
                if(player.isGroupIronman()) {
                    return;
                }
                if (player.getInventory().hasFreeSlots())
                    player.getInventory().addItem(new Item(39390, 1));
                else
                    player.getBank().addItem(new Item(39390, 1), true);
                if (addAmount) {
                    player.handleDonation(6, "Stringy");
                }
                player.sendMessage("You've purchased: [" + Colors.RCYAN + "Stringy</col>]."
                        + "Type ::perks to see all your game perks.");
                break;

            case "19954":
                if(player.isGroupIronman()) {
                    return;
                }
                if (player.getInventory().hasFreeSlots())
                    player.getInventory().addItem(new Item(35257, 1));
                else
                    player.getBank().addItem(new Item(35257, 1), true);
                if (addAmount) {
                    player.handleDonation(5, "Lion cub");
                }
                player.sendMessage("You've purchased: [" + Colors.RCYAN + "Lion cub</col>]."
                        + "Type ::perks to see all your game perks.");
                break;
            case "18841":
                player.getPerkManager().unlockPerk(DonationPerk.IMBUED_FOCUS);
                if (addAmount) {
                    player.handleDonation(5, "Imbued Focus");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Imbued Focus</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;

            case "18842":
                player.getPerkManager().unlockPerk(DonationPerk.ALCHEMIC_SMITHING);
                if (addAmount) {
                    player.handleDonation(10, "Alchemic Smithing");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Alchemic Smithing</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "17814":
                player.getPerkManager().unlockPerk(DonationPerk.BANK_COMMAND);
                if (addAmount) {
                    player.handleDonation(20, "Bank Command");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Bank Command</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "17815":
                player.getPerkManager().unlockPerk(DonationPerk.ENDLESS_ENERGY);
                if (addAmount) {
                    player.handleDonation(5, "Endless Energy");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Endless Energy</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "17816":
                player.getPerkManager().unlockPerk(DonationPerk.GREEN_THUMB);
                if (addAmount) {
                    player.handleDonation(5, "Green Thumb");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Green Thumb</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "17818":
                player.getPerkManager().unlockPerk(DonationPerk.LUMBER_LEGEND);
                if (addAmount) {
                    player.handleDonation(6, "Lumber Legend");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Lumber Legend</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "17819":
                player.getPerkManager().unlockPerk(DonationPerk.SLEIGHT_OF_HAND);
                if (addAmount) {
                    player.handleDonation(6, "Sleight of Hand");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Sleight of Hand</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "47926":
                player.getPerkManager().unlockPerk(DonationPerk.DOUBLE_SURGE);
                if (addAmount) {
                    player.handleDonation(30, "Double Surge");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Double Surge</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "47929":
                player.getPerkManager().unlockPerk(DonationPerk.DOUBLE_ESCAPE);
                if (addAmount) {
                    player.handleDonation(30, "Double Surge");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Double Surge</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "43393":
                player.getPerkManager().unlockPerk(DonationPerk.DOUBLE_BARGE);
                if (addAmount) {
                    player.handleDonation(30, "Double Surge");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Double Surge</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "17820":
                player.getPerkManager().unlockPerk(DonationPerk.FAMILIAR_EXPERT);
                if (addAmount) {
                    player.handleDonation(5, "Familiar Expert");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Familiar Expert</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "31921":
                if(player.isGroupIronman()) {
                    return;
                }
                player.getPerkManager().unlockPerk(DonationPerk.CHARGE_BEFRIENDER);
                if (addAmount) {
                    player.handleDonation(25, "Charge Befriender");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Charge Befriender</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "17822":
                player.getPerkManager().unlockPerk(DonationPerk.TREASURE_GOBLIN);
                if (addAmount) {
                    player.handleDonation(5, "Treasure Goblin");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Treasure Goblin</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "17823":
                player.getPerkManager().unlockPerk(DonationPerk.HERBIVORE);
                if (addAmount) {
                    player.handleDonation(10, "Herbivore");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Herbivore</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "17824":
                player.getPerkManager().unlockPerk(DonationPerk.MASTER_FISHERMAN);
                if (addAmount) {
                    player.handleDonation(5, "Master Fisherman");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Master Fisherman</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "17825":
                player.getPerkManager().unlockPerk(DonationPerk.DELICATE_CRAFTSMAN);
                if (addAmount) {
                    player.handleDonation(3, "Delicate Craftsman");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Delicate Craftsman</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "17827":
                if(player.isGroupIronman()) {
                    return;
                }
                player.getPerkManager().unlockPerk(DonationPerk.PRAYER_BETRAYER);
                if (addAmount) {
                    player.handleDonation(6, "Prayer Betrayer");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Prayer Betrayer</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "17828":
                player.getPerkManager().unlockPerk(DonationPerk.AVAS_SECRET);
                if (addAmount) {
                    player.handleDonation(3, "Avas Secret");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Avas Secret</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "17829":
                player.getPerkManager().unlockPerk(DonationPerk.KEY_EXPERT);
                if (addAmount) {
                    player.handleDonation(10, "Key Expert");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Key Expert</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "17830":
                player.getPerkManager().unlockPerk(DonationPerk.DRAGON_TRAINER);
                if (addAmount) {
                    player.handleDonation(5, "Dragon Trainer");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Dragon Trainer</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "17831":
                player.getPerkManager().unlockPerk(DonationPerk.GWD_SPECIALIST);
                if (addAmount) {
                    player.handleDonation(5, "GWD Specialist");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "GWD Specialist</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "17832":
                player.getPerkManager().unlockPerk(DonationPerk.DUNGEONS_MASTER);
                if (addAmount) {
                    player.handleDonation(5, "Dungeon Master");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Dungeons Master</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "17833":
                player.getPerkManager().unlockPerk(DonationPerk.PETSCHANTER);
                if (addAmount) {
                    player.handleDonation(6, "Petchanter");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Petchanter</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "17834":
                player.getPerkManager().unlockPerk(DonationPerk.PERSLAYSION);
                if (addAmount) {
                    player.handleDonation(15, "Perslaysion");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Per'slay'sion</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "17835":
                player.getPerkManager().unlockPerk(DonationPerk.OVERCLOCKED);
                if (addAmount) {
                    player.handleDonation(10, "Overclocked");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Overclocked</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "17836":
                if(player.isGroupIronman()) {
                    return;
                }
                player.getPerkManager().unlockPerk(DonationPerk.ELF__S_FRIEND);
                if (addAmount) {
                    player.handleDonation(10, "Elf's Fiend");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Elf's Friend</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "17837":
                player.getPerkManager().unlockPerk(DonationPerk.CONVINCING_COOK);
                if (addAmount) {
                    player.handleDonation(5, "Convincing Cook");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Convincing Cook</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "32554":
                player.getPerkManager().unlockPerk(DonationPerk.THE_DISCOUNTER);
                if (addAmount) {
                    player.handleDonation(15, "The Discounter");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "The Discounter</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "33183":
                player.getPerkManager().unlockPerk(DonationPerk.THE_SKIPPER);
                if (addAmount) {
                    player.handleDonation(15, "The Skipper");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "The Skipper</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "33184":
                player.getPerkManager().unlockPerk(DonationPerk.SOUL_SIPHONER);
                if (addAmount) {
                    player.handleDonation(15, "Soul Siphoner");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Soul Siphoner</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "33185":
                player.getPerkManager().unlockPerk(DonationPerk.THE_BOXER);
                if (addAmount) {
                    player.handleDonation(20, "The Boxer");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "The Boxer</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "33186":
                player.getPerkManager().unlockPerk(DonationPerk.FAVORED_FAMILIARS);
                if (addAmount) {
                    player.handleDonation(10, "Favored Familiars");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Favored Familiars</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "33187":
                player.getPerkManager().unlockPerk(DonationPerk.AUBURY__S_APPRENTICE);
                if (addAmount) {
                    player.handleDonation(10, "Aubury's Apprentice");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Aubury's Apprentice</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "33520":
                player.getPerkManager().unlockPerk(DonationPerk.ARCANE_ALCHEMIST);
                if (addAmount) {
                    player.handleDonation(5, "Arcane Alchemist");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Arcane Alchemist</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "33829":
                if(player.isGroupIronman()) {
                    return;
                }
                player.getPerkManager().unlockPerk(DonationPerk.DROP_CATCHER);
                if (addAmount) {
                    player.handleDonation(20, "Drop Catcher");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Drop Catcher</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "34104": // placeholder
                player.getPerkManager().unlockPerk(DonationPerk.DOMINION_DOMINATION);
                if (addAmount) {
                    player.handleDonation(15, "Dominion Domination");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Dominion Domination</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "34105": // placeholder
                player.getPerkManager().unlockPerk(DonationPerk.THE_EXTERMINATOR);
                if (addAmount) {
                    player.handleDonation(7, "The Exterminator");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "The Exterminator</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "34527":
                player.getPerkManager().unlockPerk(DonationPerk.SKILLING_ADDICT);
                if (addAmount) {
                    player.handleDonation(7, "Skilling Addict");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Skilling Addict</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "17845":
                if(player.isGroupIronman()) {
                    return;
                }
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
                player.getPerkManager().unlockPerk(DonationPerk.DIVINE_DOUBLER);
                player.getPerkManager().unlockPerk(DonationPerk.INVESTIGATOR);
                player.getPerkManager().unlockPerk(DonationPerk.ALCHEMIC_SMITHING);
                player.getPerkManager().unlockPerk(DonationPerk.IMBUED_FOCUS);
                player.getPerkManager().unlockPerk(DonationPerk.SKILLING_ADDICT);
                player.getPerkManager().unlockPerk(DonationPerk.TREE_HUNTER);
                player.getPerkManager().unlockPerk(DonationPerk.PET_TRAINER);
                player.getPerkManager().unlockPerk(DonationPerk.THE_STARGAZER);
                if (addAmount) {
                    player.handleDonation(250, "Complete perk pack");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Complete perk pack</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "17841":
                if(player.isGroupIronman()) {
                    return;
                }
                //	player.getPerkManager().birdMan = true;
                player.getPerkManager().unlockPerk(DonationPerk.TREASURE_GOBLIN);
                player.getPerkManager().unlockPerk(DonationPerk.KEY_EXPERT);
                player.getPerkManager().unlockPerk(DonationPerk.PETSCHANTER);
                player.getPerkManager().unlockPerk(DonationPerk.THE_BOXER);
                player.getPerkManager().unlockPerk(DonationPerk.DROP_CATCHER);
                player.getPerkManager().unlockPerk(DonationPerk.THE_EXTERMINATOR);
                if (addAmount) {
                    player.handleDonation(50, "Looters perk pack");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Looters perk pack</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "17844":
                if(player.isGroupIronman()) {
                    return;
                }
                player.getPerkManager().unlockPerk(DonationPerk.FAMILIAR_EXPERT);
                player.getPerkManager().unlockPerk(DonationPerk.CHARGE_BEFRIENDER);
                player.getPerkManager().unlockPerk(DonationPerk.PRAYER_BETRAYER);
                player.getPerkManager().unlockPerk(DonationPerk.AVAS_SECRET);
                player.getPerkManager().unlockPerk(DonationPerk.DRAGON_TRAINER);
                player.getPerkManager().unlockPerk(DonationPerk.GWD_SPECIALIST);
                player.getPerkManager().unlockPerk(DonationPerk.DUNGEONS_MASTER);
                player.getPerkManager().unlockPerk(DonationPerk.TREASURE_GOBLIN);
                player.getPerkManager().unlockPerk(DonationPerk.KEY_EXPERT);
                player.getPerkManager().unlockPerk(DonationPerk.THE_PYROMANIAC);
                player.getPerkManager().unlockPerk(DonationPerk.PETSCHANTER);
                player.getPerkManager().unlockPerk(DonationPerk.PERSLAYSION);
                player.getPerkManager().unlockPerk(DonationPerk.FAVORED_FAMILIARS);
                player.getPerkManager().unlockPerk(DonationPerk.SOUL_SIPHONER);
                player.getPerkManager().unlockPerk(DonationPerk.AUBURY__S_APPRENTICE);
                if (addAmount) {
                    player.handleDonation(110, "Combatants perk pack");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Combatants perk pack</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "17842":
                if(player.isGroupIronman()) {
                    return;
                }
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
                player.getPerkManager().unlockPerk(DonationPerk.SKILLING_ADDICT);
                player.getPerkManager().unlockPerk(DonationPerk.TREE_HUNTER);
                if (addAmount) {
                    player.handleDonation(80, "Skillers perk pack");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Skillers perk pack</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "17843":
                if(player.isGroupIronman()) {
                    return;
                }
                player.getPerkManager().unlockPerk(DonationPerk.BANK_COMMAND);
                player.getPerkManager().unlockPerk(DonationPerk.ENDLESS_ENERGY);
                player.getPerkManager().unlockPerk(DonationPerk.OVERCLOCKED);
                player.getPerkManager().unlockPerk(DonationPerk.ELF__S_FRIEND);
                player.getPerkManager().unlockPerk(DonationPerk.THE_MINI___GAMER);
                player.getPerkManager().unlockPerk(DonationPerk.PORTS_MASTER);
                player.getPerkManager().unlockPerk(DonationPerk.INVESTIGATOR);
                player.getPerkManager().unlockPerk(DonationPerk.THE_DISCOUNTER);
                player.getPerkManager().unlockPerk(DonationPerk.THE_BOXER);
                player.getPerkManager().unlockPerk(DonationPerk.DROP_CATCHER);
                player.getPerkManager().unlockPerk(DonationPerk.THE_SKIPPER);
                player.getPerkManager().unlockPerk(DonationPerk.ARCANE_ALCHEMIST);
                player.getPerkManager().unlockPerk(DonationPerk.THE_EXTERMINATOR);
                player.getPerkManager().unlockPerk(DonationPerk.DOMINION_DOMINATION);
                if (addAmount) {
                    player.handleDonation(100, "Utility perk pack");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Utility perk pack</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "17838":
                player.getPerkManager().unlockPerk(DonationPerk.DEDICATED_DIVINATION);
                if (addAmount) {
                    player.handleDonation(10, "Dedicated Divination");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Dedicated Divination</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "17839":
                player.getPerkManager().unlockPerk(DonationPerk.MASTER_MINER);
                if (addAmount) {
                    player.handleDonation(5, "Master Miner");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Master Miner</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "17840":
                player.getPerkManager().unlockPerk(DonationPerk.THE_MINI___GAMER);
                if (addAmount) {
                    player.handleDonation(5, "The Mini-Gamer");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "The Mini-Gamer</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "18096":
                player.getPerkManager().unlockPerk(DonationPerk.MASTER_FLEDGER);
                if (addAmount) {
                    player.handleDonation(6, "Master Fledger");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Master Fledger</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "17847":
                player.getTreasureHunter().giveBoughtKeys(5);
                if (addAmount) {
                    player.handleDonation(2, "x5 Treasure hunter Keys");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "x5 Treasure hunter Keys</col>]. "
                        + "Open the Treasure Hunter tab to use them.");
                break;
            case "17848":
                player.getTreasureHunter().giveBoughtKeys(27);
                if (addAmount) {
                    player.handleDonation(10, "x25 Treasure hunter Keys");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "x27 Treasure hunter Keys</col>]. "
                        + "Open the Treasure Hunter tab to use them.");
                break;
            case "17850":
                player.getTreasureHunter().giveBoughtKeys(55);
                if (addAmount) {
                    player.handleDonation(20, "x50 Treasure hunter Keys");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "x55 Treasure hunter Keys</col>]. "
                        + "Open the Treasure Hunter tab to use them.");
                break;
            case "17851":
                player.getTreasureHunter().giveBoughtKeys(175);
                if (addAmount) {
                    player.handleDonation(50, "x150 Treasure hunter Keys");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "x175 Treasure hunter Keys</col>]. "
                        + "Open the Treasure Hunter tab to use them.");
                break;
            case "17852":
                player.getTreasureHunter().giveBoughtKeys(350);
                if (addAmount) {
                    player.handleDonation(100, "x300 Treasure hunter Keys");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "x350 Treasure hunter Keys</col>]. "
                        + "Open the Treasure Hunter tab to use them.");
                break;
            case "17853":
                player.getPerkManager().unlockPerk(DonationPerk.HUNTSMAN);
                if (addAmount) {
                    player.handleDonation(6, "Huntsman");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Huntsman</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "17846":
                player.getPerkManager().unlockPerk(DonationPerk.THE_PYROMANIAC);
                if (addAmount) {
                    player.handleDonation(10, "The Pyromaniac");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "The Pyromaniac</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "18108":
                player.getAnimations().hasBattleCry = true;
                if (addAmount) {
                    player.handleDonation(2, "Slayer Battle Cry");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Slayer Battle Cry</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "18109":
                player.getAnimations().hasEnhancedPotion = true;
                if (addAmount) {
                    player.handleDonation(2, "Enhanced Potion Making");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Enhanced Potion Making</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "18110":
                player.getAnimations().hasLumberjackWc = true;
                if (addAmount) {
                    player.handleDonation(2, "Lumberjack Woodcutting");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Lumberjack Woodcutting</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "18111":
                player.getAnimations().hasDeepFishing = true;
                if (addAmount) {
                    player.handleDonation(2, "Deep-Sea Fishing");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Deep-Sea Fishing</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "18112":
                player.getAnimations().hasZenResting = true;
                if (addAmount) {
                    player.handleDonation(2, "Zen Resting");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Zen Resting</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "18113":
                player.getAnimations().hasKarateFletch = true;
                if (addAmount) {
                    player.handleDonation(2, "Karate-Chop Fletching");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Karate-Chop Fletching</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "18114":
                player.getAnimations().hasIronSmith = true;
                if (addAmount) {
                    player.handleDonation(2, "Iron-Fist Smithing");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Iron-Fist Smithing</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "18115":
                player.getAnimations().hasChiMining = true;
                if (addAmount) {
                    player.handleDonation(2, "Chi-Blast Mining");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Chi-Blast Mining</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "18116":
                player.getAnimations().hasSamuraiCook = true;
                if (addAmount) {
                    player.handleDonation(2, "Samurai Cooking");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Samurai Cooking</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "18118":
                player.getAnimations().hasRoundHouseWc = true;
                if (addAmount) {
                    player.handleDonation(2, "Roundhouse Woodcutting");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Roundhouse Woodcutting</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "18119":
                player.getAnimations().hasBlastMining = true;
                if (addAmount) {
                    player.handleDonation(2, "Blast Mining");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Blast Mining</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "18128":
                player.getAnimations().hasStrongResting = true;
                if (addAmount) {
                    player.handleDonation(2, "Strongarm Resting");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Strongarm Resting</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "18121":
                player.getAnimations().hasArcaneSmelt = true;
                if (addAmount) {
                    player.handleDonation(2, "Arcane Smelting");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Arcane Smelting</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "18122":
                player.getAnimations().hasArcaneResting = true;
                if (addAmount) {
                    player.handleDonation(2, "Arcane Resting");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Arcane Resting</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "18123":
                player.getAnimations().hasStrongWc = true;
                if (addAmount) {
                    player.handleDonation(2, "Strongarm Woodcutting");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Strongarm Woodcutting</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "18124":
                player.getAnimations().hasStrongMining = true;
                if (addAmount) {
                    player.handleDonation(2, "Strongarm Mining");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Strongarm Mining</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "17890":
                player.getAnimations().hasArcaneFishing = true;
                if (addAmount) {
                    player.handleDonation(2, "Arcane Fishing");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Arcane Fishing</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "17891":
                player.getAnimations().hasStrongBurial = true;
                if (addAmount) {
                    player.handleDonation(2, "Strongarm Burial");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Strongarm Burial</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "17892":
                player.getAnimations().hasArcaneCook = true;
                if (addAmount) {
                    player.handleDonation(2, "Arcane Cooking");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Arcane Cooking</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "17893":
                player.getAnimations().hasPowerDivination = true;
                if (addAmount) {
                    player.handleDonation(2, "Powerful Divination");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Powerful Divination</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "17894":
                player.getAnimations().hasPowerConversion = true;
                if (addAmount) {
                    player.handleDonation(2, "Powerful Conversion");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Powerful Conversion</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "17895":
                player.getAnimations().hasAgileDivination = true;
                if (addAmount) {
                    player.handleDonation(2, "Agile Divination");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Agile Divination</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "18127":
                player.getAnimations().hasAgileConversion = true;
                if (addAmount) {
                    player.handleDonation(2, "Agile Conversion");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Agile Conversion</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "17896":
                player.getAnimations().hasSinisterSlumber = true;
                if (addAmount) {
                    player.handleDonation(2, "Sinister Slumber");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Sinister Slumber</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "18130":
                player.getAnimations().hasArmWarrior = true;
                if (addAmount) {
                    player.handleDonation(2, "Armchair Warrior");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Armchair Warrior</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "18129":
                player.getAnimations().hasEneResting = true;
                if (addAmount) {
                    player.handleDonation(2, "Energy Drain Resting");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Energy Drain Resting</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "17897":
                player.getAnimations().hasCrystalResting = true;
                if (addAmount) {
                    player.handleDonation(2, "Crystal Impling Resting");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Crystal Impling Resting</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "17898":
                player.getAnimations().hasHeadMining = true;
                if (addAmount) {
                    player.handleDonation(2, "Headbutt Mining");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Headbutt Mining</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "17899":
                player.getAnimations().hasSandWalk = true;
                if (addAmount) {
                    player.handleDonation(4, "Sandstorm Walk");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Sandstorm Walk</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "18125":
                player.getAnimations().hasSadWalk = true;
                if (addAmount) {
                    player.handleDonation(4, "Sad Walk");
                }
                player.sendMessage(
                        "You've redeemed: [" + Colors.GREEN + "Sad Walk</col>]. " + "Talk to Solomon to toggle it on/off!");
                break;
            case "17273":
                player.getAnimations().hasAngryWalk = true;
                if (addAmount) {
                    player.handleDonation(4, "Angry Walk");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Angry Walk</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "17900":
                player.getAnimations().hasProudWalk = true;
                if (addAmount) {
                    player.handleDonation(4, "Proud Walk");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Proud Walk</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "18126":
                player.getAnimations().hasHappyWalk = true;
                if (addAmount) {
                    player.handleDonation(4, "Happy Walk");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Happy Walk</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "17901":
                player.getAnimations().hasBarbarianWalk = true;
                if (addAmount) {
                    player.handleDonation(4, "Barbarian Walk");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Barbarian Walk</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "17902":
                player.getAnimations().hasRevenantWalk = true;
                if (addAmount) {
                    player.handleDonation(4, "Revenant Walk");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Revenant Walk</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "36233":
                player.getAnimations().hasBlinkWalk = true;
                if (addAmount) {
                    player.handleDonation(5, "Blink Walk");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Blink Walk</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "36234":
                player.getAnimations().hasCatWalk = true;
                if (addAmount) {
                    player.handleDonation(5, "Cat Walk");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Cat Walk</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "39133":
                player.getAnimations().hasZombieWalk = true;
                if (addAmount) {
                    player.handleDonation(5, "Zombie Walk");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Zombie Walk</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "39134":
                player.getAnimations().hasCongaWalk = true;
                if (addAmount) {
                    player.handleDonation(5, "Conga Walk");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Conga Walk</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "39135":
                player.getAnimations().hasLovedUpWalk = true;
                if (addAmount) {
                    player.handleDonation(5, "Loved Up Walk");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Loved Up Walk</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "39136":
                player.getAnimations().hasSlowmoWalk = true;
                if (addAmount) {
                    player.handleDonation(5, "Slowmo Walk");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Slowmo Walk</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "39137":
                player.getAnimations().hasPartyFeverWalk = true;
                if (addAmount) {
                    player.handleDonation(5, "Party Fever Walk");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Party Fever Walk</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "39138":
                player.getAnimations().hasAssassinWalk = true;
                if (addAmount) {
                    player.handleDonation(5, "Assassin Walk");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Assassin Walk</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "39139":
                player.getAnimations().hasTwistedJesterWalk = true;
                if (addAmount) {
                    player.handleDonation(5, "Twisted Jester Walk");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Twisted Jester Walk</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "39140":
                player.getAnimations().hasHotSandWalk = true;
                if (addAmount) {
                    player.handleDonation(5, "Hot Sand Walk");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Hot Sand Walk</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "39141":
                player.getAnimations().hasPlagueDoctorWalk = true;
                if (addAmount) {
                    player.handleDonation(5, "Plague Doctor Walk");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Plague Doctor Walk</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "17854":
                player.getPerkManager().unlockPerk(DonationPerk.PORTS_MASTER);
                if (addAmount) {
                    player.handleDonation(4, "Ports Master");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Ports Master</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "18210":
                player.getPerkManager().unlockPerk(DonationPerk.INVESTIGATOR);
                if (addAmount) {
                    player.handleDonation(15, "Investigator");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Investigator</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "18211":
                player.getPerkManager().unlockPerk(DonationPerk.DIVINE_DOUBLER);
                if (addAmount) {
                    player.handleDonation(10, "Divine Doubler");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Divine Doubler</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "24011":
                if (addAmount) {
                    player.handleDonation(5, "3 keepsake keys");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "3 keepsake keys</col>].");
                player.getBank().addItem(new Item(25430, 3), true);
                break;
            case "24012":
                if (addAmount) {
                    player.handleDonation(10, "7 keepsake keys");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "7 keepsake keys</col>].");
                player.getBank().addItem(new Item(25430, 7),true);
                break;
            case "24013":
                if (addAmount) {
                    player.handleDonation(18, "15 keepsake keys");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "15 keepsake keys</col>].");
                player.getBank().addItem(new Item(25430, 15), true);
                break;
            case "24014":
                if (addAmount) {
                    player.handleDonation(40, "35 keepsake keys");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "35 keepsake keys</col>].");
                player.getBank().addItem(new Item(25430, 35), true);
                break;
            case "34733":
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "5 Ataraxia dollars</col>].");
                player.getBank().addItem(new Item(41430, 5),true);
                break;
            case "34734":
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "15 Ataraxia dollars</col>].");
                player.getBank().addItem(new Item(41430, 15),true);
                break;
            case "34735":
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "25 Ataraxia dollars</col>].");
                player.getBank().addItem(new Item(41430, 25),true);
                break;
            case "34736":
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "50 Ataraxia dollars</col>].");
                player.getBank().addItem(new Item(41430, 50),true);
                break;
            case "34737":
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "75 Ataraxia dollars</col>].");
                player.getBank().addItem(new Item(41430, 75),true);
                break;
            case "34738":
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "100 Ataraxia dollars</col>].");
                player.getInventory().addItem(new Item(41430, 100));
                break;
            case "34739":
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "250 Ataraxia dollars</col>].");
                player.getBank().addItem(new Item(41430, 250),true);
                break;
            case "34740":
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "500 Ataraxia dollars</col>].");
                player.getBank().addItem(new Item(41430, 500),true);
                break;
            case "35614":
                player.getAnimations().hasExplosiveWc = true;
                if (addAmount) {
                    player.handleDonation(2, "Explosive Woodcutting");
                }
                player.sendMessage("You've purchased: [" + Colors.RED + "Explosive Woodcutting</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "35615":
                player.getAnimations().hasSingerWc = true;
                if (addAmount) {
                    player.handleDonation(2, "Crystal Singer Woodcutting");
                }
                player.sendMessage("You've purchased: [" + Colors.RED + "Crystal Singer Woodcutting</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "35616":
                player.getAnimations().hasBombFiremaking = true;
                if (addAmount) {
                    player.handleDonation(2, "Bomb Firemaking");
                }
                player.sendMessage("You've purchased: [" + Colors.RED + "Bomb Firemaking</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "35711":
                player.getAnimations().hasPartyhatFiremaking = true;
                if (addAmount) {
                    player.handleDonation(4, "Partyhat Firemaking");
                }
                player.sendMessage("You've purchased: [" + Colors.RED + "Partyhat Firemaking</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "35712":
                player.getAnimations().hasAncestralDivination1 = true;
                if (addAmount) {
                    player.handleDonation(2, "Ancestral Divination I");
                }
                player.sendMessage("You've purchased: [" + Colors.RED + "Ancestral Divination II</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "35713":
                player.getAnimations().hasAncestralDivination2 = true;
                if (addAmount) {
                    player.handleDonation(2, "Ancestral Divination II");
                }
                player.sendMessage("You've purchased: [" + Colors.RED + "Ancestral Divination II</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "35714":
                player.getAnimations().hasAncestralConversion = true;
                if (addAmount) {
                    player.handleDonation(2, "Ancestral Conversion");
                }
                player.sendMessage("You've purchased: [" + Colors.RED + "Ancestral Conversion</col>]. "
                        + "Talk to Solomon to toggle it on/off!");
                break;
            case "35715":
                if(player.isGroupIronman()) {
                    return;
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Pet perk - Power Exchange</col>].");
                player.getBank().addItem(new Item(41471, 1),true);
                break;
            case "35716":
                if(player.isGroupIronman()) {
                    return;
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Pet perk - Doubled Edge</col>].");
                player.getBank().addItem(new Item(41472, 1),true);
                break;
            case "35717":
                if(player.isGroupIronman()) {
                    return;
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Pet perk - Double Trouble</col>].");
                player.getBank().addItem(new Item(41473, 1),true);
                break;
            case "35718":
                if(player.isGroupIronman()) {
                    return;
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Pet perk - Overloaded</col>].");
                player.getBank().addItem(new Item(41474, 1),true);
                break;
            case "35719":
                if(player.isGroupIronman()) {
                    return;
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Pet perk - Kuradamnn</col>].");
                player.getBank().addItem(new Item(41475, 1),true);
                break;
            case "35720":
                if(player.isGroupIronman()) {
                    return;
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Pet perk - Extra Arms</col>].");
                player.getBank().addItem(new Item(41477, 1),true);
                break;
            case "37582":
                if(player.isGroupIronman()) {
                    return;
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Pet perk - Efficiency Expert</col>].");
                player.getBank().addItem(new Item(41479, 1),true);
                break;
            case "35721":
                if(player.isGroupIronman()) {
                    return;
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Pet perk - Nice But Dim</col>].");
                player.getBank().addItem(new Item(41478, 1),true);
                break;
            case "36395":
                if(player.isGroupIronman()) {
                    return;
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "3x Pet perk - Power Exchange</col>].");
                player.getBank().addItem(new Item(41471, 3),true);
                break;
            case "36396":
                if(player.isGroupIronman()) {
                    return;
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "3x Pet perk - Double Trouble</col>].");
                player.getBank().addItem(new Item(41473, 3),true);
                break;
            case "36398":
                if(player.isGroupIronman()) {
                    return;
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "3x Pet perk - Overloaded</col>].");
                player.getBank().addItem(new Item(41474, 3),true);
                break;
            case "36399":
                if(player.isGroupIronman()) {
                    return;
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "3x Pet perk - Kuradamnn</col>].");
                player.getBank().addItem(new Item(41475, 3),true);
                break;
            case "36400":
                if(player.isGroupIronman()) {
                    return;
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "3x Pet perk - Extra Arms</col>].");
                player.getBank().addItem(new Item(41477, 3),true);
                break;
            case "37583":
                if(player.isGroupIronman()) {
                    return;
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "3x Pet perk - Efficiency Expert</col>].");
                player.getBank().addItem(new Item(41479, 3),true);
                break;
            case "36401":
                if(player.isGroupIronman()) {
                    return;
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "3x Pet perk - Nice But Dim</col>].");
                player.getBank().addItem(new Item(41478, 3),true);
                break;
            case "36402":
                if(player.isGroupIronman()) {
                    return;
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Pet perk - Bundle</col>].");
                if (addAmount) {
                    player.handleDonation(6, "Pet perk - Bundle");
                }
                player.getInventory().addItem(new Item(41478, 3));
                player.getInventory().addItem(new Item(41477, 3));
                player.getInventory().addItem(new Item(41475, 3));
                player.getInventory().addItem(new Item(41474, 3));
                player.getInventory().addItem(new Item(41473, 3));
                player.getInventory().addItem(new Item(41471, 3));
                player.getInventory().addItem(new Item(30233, 1));
                break;
            case "37142":
                player.getPerkManager().unlockPerk(DonationPerk.TREE_HUNTER);
                if (addAmount) {
                    player.handleDonation(10, "Tree Hunter");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Tree Hunter</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "37575":
                if(player.isGroupIronman()) {
                    return;
                }
                player.getPerkManager().unlockPerk(DonationPerk.DUNGEON_ARCHITECT);
                if (addAmount) {
                    player.handleDonation(8, "Dungeon Architect");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Dungeon Architect</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "37574":
                if(player.isGroupIronman()) {
                    return;
                }
                player.getPerkManager().unlockPerk(DonationPerk.PET_TRAINER);
                if (addAmount) {
                    player.handleDonation(10, "Pet Trainer");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Pet Trainer</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;

                // Start of GIM Perks!

            /* Currently disabled for GIM
            *
            * Drop Catcher
            * Dungeon Architect
            * All Store pets
            * All store pet perks
            * Charge befriender
            * Elf's Friend
            * Prayer Betrayer
            * Squeal of Fortune Spins
            * All perk packages
            *
            *
             */
            case "38627":
                player.getPerkManager().unlockPerk(DonationPerk.BANK_COMMAND);
                if (addAmount) {
                    player.handleDonation(20, "Bank Command");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Bank Command</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "38628":
                player.getPerkManager().unlockPerk(DonationPerk.ENDLESS_ENERGY);
                if (addAmount) {
                    player.handleDonation(5, "Endless Energy");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Endless Energy</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "38629":
                player.getPerkManager().unlockPerk(DonationPerk.GREEN_THUMB);
                if (addAmount) {
                    player.handleDonation(5, "Green Thumb");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Green Thumb</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "38630":
                player.getPerkManager().unlockPerk(DonationPerk.LUMBER_LEGEND);
                if (addAmount) {
                    player.handleDonation(6, "Lumber Legend");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Lumber Legend</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "38631":
                player.getPerkManager().unlockPerk(DonationPerk.SLEIGHT_OF_HAND);
                if (addAmount) {
                    player.handleDonation(6, "Sleight of Hand");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Sleight of Hand</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "38632":
                player.getPerkManager().unlockPerk(DonationPerk.TREASURE_GOBLIN);
                if (addAmount) {
                    player.handleDonation(5, "Treasure Goblin");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Treasure Goblin</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "38633":
                player.getPerkManager().unlockPerk(DonationPerk.HERBIVORE);
                if (addAmount) {
                    player.handleDonation(10, "Herbivore");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Herbivore</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "38634":
                player.getPerkManager().unlockPerk(DonationPerk.DELICATE_CRAFTSMAN);
                if (addAmount) {
                    player.handleDonation(3, "Delicate Craftsman");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Delicate Craftsman</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "38635":
                player.getPerkManager().unlockPerk(DonationPerk.FAMILIAR_EXPERT);
                if (addAmount) {
                    player.handleDonation(5, "Familiar Expert");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Familiar Expert</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "38638":
                player.getPerkManager().unlockPerk(DonationPerk.AVAS_SECRET);
                if (addAmount) {
                    player.handleDonation(3, "Avas Secret");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Avas Secret</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "38639":
                player.getPerkManager().unlockPerk(DonationPerk.KEY_EXPERT);
                if (addAmount) {
                    player.handleDonation(10, "Key Expert");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Key Expert</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "38646":
                player.getPerkManager().unlockPerk(DonationPerk.DRAGON_TRAINER);
                if (addAmount) {
                    player.handleDonation(5, "Dragon Trainer");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Dragon Trainer</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "38647":
                player.getPerkManager().unlockPerk(DonationPerk.GWD_SPECIALIST);
                if (addAmount) {
                    player.handleDonation(5, "GWD Specialist");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "GWD Specialist</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "38648":
                player.getPerkManager().unlockPerk(DonationPerk.DUNGEONS_MASTER);
                if (addAmount) {
                    player.handleDonation(5, "Dungeon Master");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Dungeons Master</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "38649":
                player.getPerkManager().unlockPerk(DonationPerk.PETSCHANTER);
                if (addAmount) {
                    player.handleDonation(6, "Petchanter");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Petchanter</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "38650":
                player.getPerkManager().unlockPerk(DonationPerk.PERSLAYSION);
                if (addAmount) {
                    player.handleDonation(15, "Perslaysion");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Per'slay'sion</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "38651":
                player.getPerkManager().unlockPerk(DonationPerk.OVERCLOCKED);
                if (addAmount) {
                    player.handleDonation(10, "Overclocked");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Overclocked</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "38652":
                player.getPerkManager().unlockPerk(DonationPerk.CONVINCING_COOK);
                if (addAmount) {
                    player.handleDonation(5, "Convincing Cook");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Convincing Cook</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "38655":
                player.getPerkManager().unlockPerk(DonationPerk.DEDICATED_DIVINATION);
                if (addAmount) {
                    player.handleDonation(10, "Dedicated Divination");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Dedicated Divination</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "38656":
                player.getPerkManager().unlockPerk(DonationPerk.MASTER_MINER);
                if (addAmount) {
                    player.handleDonation(5, "Master Miner");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Master Miner</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "38657":
                player.getPerkManager().unlockPerk(DonationPerk.THE_MINI___GAMER);
                if (addAmount) {
                    player.handleDonation(5, "The Mini-Gamer");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "The Mini-Gamer</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "38658":
                player.getPerkManager().unlockPerk(DonationPerk.THE_PYROMANIAC);
                if (addAmount) {
                    player.handleDonation(10, "The Pyromaniac");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "The Pyromaniac</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "38659":
                player.getPerkManager().unlockPerk(DonationPerk.HUNTSMAN);
                if (addAmount) {
                    player.handleDonation(6, "Huntsman");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Huntsman</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "38660":
                player.getPerkManager().unlockPerk(DonationPerk.PORTS_MASTER);
                if (addAmount) {
                    player.handleDonation(4, "Ports Master");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Ports Master</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "38661":
                player.getPerkManager().unlockPerk(DonationPerk.MASTER_FLEDGER);
                if (addAmount) {
                    player.handleDonation(6, "Master Fledger");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Master Fledger</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "38662":
                player.getPerkManager().unlockPerk(DonationPerk.INVESTIGATOR);
                if (addAmount) {
                    player.handleDonation(15, "Investigator");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Investigator</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "38663":
                player.getPerkManager().unlockPerk(DonationPerk.DIVINE_DOUBLER);
                if (addAmount) {
                    player.handleDonation(10, "Divine Doubler");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Divine Doubler</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "38664":
                player.getPerkManager().unlockPerk(DonationPerk.ALCHEMIC_SMITHING);
                if (addAmount) {
                    player.handleDonation(10, "Alchemic Smithing");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Alchemic Smithing</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "38665":
                player.getPerkManager().unlockPerk(DonationPerk.THE_DISCOUNTER);
                if (addAmount) {
                    player.handleDonation(15, "The Discounter");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "The Discounter</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "38666":
                player.getPerkManager().unlockPerk(DonationPerk.THE_SKIPPER);
                if (addAmount) {
                    player.handleDonation(15, "The Skipper");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "The Skipper</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "38667":
                player.getPerkManager().unlockPerk(DonationPerk.SOUL_SIPHONER);
                if (addAmount) {
                    player.handleDonation(15, "Soul Siphoner");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Soul Siphoner</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "38668":
                player.getPerkManager().unlockPerk(DonationPerk.THE_BOXER);
                if (addAmount) {
                    player.handleDonation(20, "The Boxer");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "The Boxer</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "38669":
                player.getPerkManager().unlockPerk(DonationPerk.FAVORED_FAMILIARS);
                if (addAmount) {
                    player.handleDonation(10, "Favored Familiars");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Favored Familiars</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "38670":
                player.getPerkManager().unlockPerk(DonationPerk.AUBURY__S_APPRENTICE);
                if (addAmount) {
                    player.handleDonation(10, "Aubury's Apprentice");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Aubury's Apprentice</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "38671":
                player.getPerkManager().unlockPerk(DonationPerk.ARCANE_ALCHEMIST);
                if (addAmount) {
                    player.handleDonation(5, "Arcane Alchemist");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Arcane Alchemist</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "38672":
                player.getPerkManager().unlockPerk(DonationPerk.DOMINION_DOMINATION);
                if (addAmount) {
                    player.handleDonation(15, "Dominion Domination");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Dominion Domination</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "38673":
                player.getPerkManager().unlockPerk(DonationPerk.THE_EXTERMINATOR);
                if (addAmount) {
                    player.handleDonation(7, "The Exterminator");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "The Exterminator</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "38674":
                player.getPerkManager().unlockPerk(DonationPerk.SKILLING_ADDICT);
                if (addAmount) {
                    player.handleDonation(7, "Skilling Addict");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Skilling Addict</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "38675":
                player.getPerkManager().unlockPerk(DonationPerk.TREE_HUNTER);
                if (addAmount) {
                    player.handleDonation(10, "Tree Hunter");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Tree Hunter</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "38677":
                player.getPerkManager().unlockPerk(DonationPerk.IMBUED_FOCUS);
                if (addAmount) {
                    player.handleDonation(5, "Imbued Focus");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Imbued Focus</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "38678":
                player.getPerkManager().unlockPerk(DonationPerk.MASTER_FISHERMAN);
                if (addAmount) {
                    player.handleDonation(5, "Master Fisherman");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Master Fisherman</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "33265":
                player.getPerkManager().unlockPerk(DonationPerk.THE_STARGAZER);
                if (addAmount) {
                    player.handleDonation(7, "The Stargazer");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "The Stargazer</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;

                // GIM PACKAGES

            case "38683":
                player.getPerkManager().unlockPerk(DonationPerk.GREEN_THUMB);
                player.getPerkManager().unlockPerk(DonationPerk.LUMBER_LEGEND);
                player.getPerkManager().unlockPerk(DonationPerk.SLEIGHT_OF_HAND);
                player.getPerkManager().unlockPerk(DonationPerk.HERBIVORE);
                player.getPerkManager().unlockPerk(DonationPerk.MASTER_FISHERMAN);
                player.getPerkManager().unlockPerk(DonationPerk.DELICATE_CRAFTSMAN);
                player.getPerkManager().unlockPerk(DonationPerk.FAMILIAR_EXPERT);
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
                player.getPerkManager().unlockPerk(DonationPerk.OVERCLOCKED);
                player.getPerkManager().unlockPerk(DonationPerk.CONVINCING_COOK);
                player.getPerkManager().unlockPerk(DonationPerk.DEDICATED_DIVINATION);
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
                player.getPerkManager().unlockPerk(DonationPerk.THE_SKIPPER);
                player.getPerkManager().unlockPerk(DonationPerk.THE_EXTERMINATOR);
                player.getPerkManager().unlockPerk(DonationPerk.DOMINION_DOMINATION);
                player.getPerkManager().unlockPerk(DonationPerk.INVESTIGATOR);
                player.getPerkManager().unlockPerk(DonationPerk.DIVINE_DOUBLER);
                player.getPerkManager().unlockPerk(DonationPerk.IMBUED_FOCUS);
                player.getPerkManager().unlockPerk(DonationPerk.ALCHEMIC_SMITHING);
                player.getPerkManager().unlockPerk(DonationPerk.SKILLING_ADDICT);
                player.getPerkManager().unlockPerk(DonationPerk.TREE_HUNTER);
                if (addAmount) {
                    player.handleDonation(200, "Complete perk pack");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Complete perk pack</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "38679":
                if(!player.isGroupIronman()) {
                    return;
                }
                //	player.getPerkManager().birdMan = true;
                player.getPerkManager().unlockPerk(DonationPerk.TREASURE_GOBLIN);
                player.getPerkManager().unlockPerk(DonationPerk.KEY_EXPERT);
                player.getPerkManager().unlockPerk(DonationPerk.PETSCHANTER);
                player.getPerkManager().unlockPerk(DonationPerk.THE_BOXER);
                player.getPerkManager().unlockPerk(DonationPerk.THE_EXTERMINATOR);
                if (addAmount) {
                    player.handleDonation(35, "Looters perk pack");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Looters perk pack (GIM)</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "38682":
                if(!player.isGroupIronman()) {
                    return;
                }
                player.getPerkManager().unlockPerk(DonationPerk.FAMILIAR_EXPERT);
                player.getPerkManager().unlockPerk(DonationPerk.AVAS_SECRET);
                player.getPerkManager().unlockPerk(DonationPerk.DRAGON_TRAINER);
                player.getPerkManager().unlockPerk(DonationPerk.GWD_SPECIALIST);
                player.getPerkManager().unlockPerk(DonationPerk.DUNGEONS_MASTER);
                player.getPerkManager().unlockPerk(DonationPerk.TREASURE_GOBLIN);
                player.getPerkManager().unlockPerk(DonationPerk.KEY_EXPERT);
                player.getPerkManager().unlockPerk(DonationPerk.THE_PYROMANIAC);
                player.getPerkManager().unlockPerk(DonationPerk.PETSCHANTER);
                player.getPerkManager().unlockPerk(DonationPerk.PERSLAYSION);
                player.getPerkManager().unlockPerk(DonationPerk.FAVORED_FAMILIARS);
                player.getPerkManager().unlockPerk(DonationPerk.SOUL_SIPHONER);
                player.getPerkManager().unlockPerk(DonationPerk.AUBURY__S_APPRENTICE);
                if (addAmount) {
                    player.handleDonation(90, "Combatants perk pack");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Combatants perk pack (GIM)</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "38680":
                if(!player.isGroupIronman()) {
                    return;
                }
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
                player.getPerkManager().unlockPerk(DonationPerk.SKILLING_ADDICT);
                player.getPerkManager().unlockPerk(DonationPerk.TREE_HUNTER);
                if (addAmount) {
                    player.handleDonation(70, "Skillers perk pack");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Skillers perk pack (GIM)</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
            case "38681":
                if(!player.isGroupIronman()) {
                    return;
                }
                player.getPerkManager().unlockPerk(DonationPerk.BANK_COMMAND);
                player.getPerkManager().unlockPerk(DonationPerk.ENDLESS_ENERGY);
                player.getPerkManager().unlockPerk(DonationPerk.OVERCLOCKED);
                player.getPerkManager().unlockPerk(DonationPerk.THE_MINI___GAMER);
                player.getPerkManager().unlockPerk(DonationPerk.PORTS_MASTER);
                player.getPerkManager().unlockPerk(DonationPerk.INVESTIGATOR);
                player.getPerkManager().unlockPerk(DonationPerk.THE_DISCOUNTER);
                player.getPerkManager().unlockPerk(DonationPerk.THE_BOXER);
                player.getPerkManager().unlockPerk(DonationPerk.THE_SKIPPER);
                player.getPerkManager().unlockPerk(DonationPerk.ARCANE_ALCHEMIST);
                player.getPerkManager().unlockPerk(DonationPerk.THE_EXTERMINATOR);
                player.getPerkManager().unlockPerk(DonationPerk.DOMINION_DOMINATION);
                if (addAmount) {
                    player.handleDonation(75, "Utility perk pack");
                }
                player.sendMessage("You've redeemed: [" + Colors.GREEN + "Utility perk pack (GIM)</col>]. "
                        + "Type ::perks to see all your game perks.");
                break;
        }
    }


    public static void handlePerkBox(Player player, int id) {
        for (int i = 0; i < perkBox.length; i++) {
            if (perkBox[i][0] == id) {
                awardDonation(player, String.valueOf(perkBox[i][1]));
                player.getInventory().deleteItem(new Item(id));
                break;
            }
        }
    }

    private static boolean canGivePerkBox(int id) {
        for (int[] perkBoxWithPerkId : perkBox) {
            if (perkBoxWithPerkId[1] == id) {
                return true;
            }
        }
        return false;
    }

    public static void givePerkBox(Player player, int id) {
        for (int[] perkBoxWithPerkId : perkBox) {
            if (perkBoxWithPerkId[1] == id) {
                player.getBank().addItem(new Item(perkBoxWithPerkId[0]), true);
                player.sm(Colors.GREEN + "Thanks for your purchase! Your item can be found in your bank.");
                break;
            }
        }
    }
}
