package com.rs.game.player;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.rs.Settings;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

import lombok.Getter;
import lombok.Setter;

/**
 * Handles all Game Perks that have been donated for.
 *
 * @author Noele.
 */
public class PerkManager implements Serializable {

    private static final ImmutableSet<DonationPerk> GIM_DISABLED = Sets.immutableEnumSet(
            DonationPerk.DROP_CATCHER,
            DonationPerk.DUNGEON_ARCHITECT,
            DonationPerk.CHARGE_BEFRIENDER,
            DonationPerk.PET_TRAINER,
            DonationPerk.CHARGE_BEFRIENDER,
            DonationPerk.ELF__S_FRIEND,
            DonationPerk.PRAYER_BETRAYER
    );

    /**
     * Generated serial UID.
     */
    private static final long serialVersionUID = -6454356751078830705L;

    /**
     * A list of available perks. IF A PERK IS ADDED, ADD IT TO {@link PerkManager#displayAvailablePerks()} AND
     * {@link PerkManager#transferPerks(Player, Player)}!!!!!
     */
    public boolean bankCommand, DOUBLE_SURGE, endlessEnergy, greenThumb, lumberLegend, sleightOfHand, familiarExpert,
            chargeBefriender, treasureGoblin, herbivore, masterFisherman, delicateCraftsman,
            prayerBetrayer, avasSecret, keyExpert, dragonTrainer, gwdSpecialist, dungeon, petChanter, perslaysion,
            overclocked, elfFriend, convincingCook, dedicatedDiviner, masterMiner, miniGamer, masterFledger, thePyromaniac,
            huntsman, portsMaster, investigator, divineDoubler, imbuedFocus, alchemicSmith, theDiscounter, auburyApprentice, theSkipper, theBoxer, theStargazer, soulSiphoner,
            dominionDomination, favoredFamiliars, arcaneAlchemist, dropCatcher, theExterminator, skillingAddict, treeHunter,haspetTrainer, dungeonArchitect;
    /**
     * The player instance.
     */
    private transient Player player;
    @Getter
    @Setter
    private Map<DonationPerk, Boolean> donationPerks;
    
    
    
    public PerkManager() {
        donationPerks = new HashMap<DonationPerk, Boolean>(); 
    }
    
    /**
     * The player instance saving to.
     *
     * @param player The player.
     */
    protected void setPlayer(Player player) {
        this.player = player;
        if (donationPerks == null)
            donationPerks = new HashMap<DonationPerk, Boolean>();
        transferOldPerks();
    }
    
    public void unlockPerk(DonationPerk perk) {
        donationPerks.put(perk, true);
    }
    
    public void removePerk(DonationPerk perk) {
        donationPerks.remove(perk);
    }
    
    public boolean hasPerk(DonationPerk perk) {
        return donationPerks.containsKey(perk);
    }
    
    public boolean hasPerkActive(DonationPerk perk) {
        if (!donationPerks.containsKey(perk))
            return false;
        return donationPerks.get(perk) || !perk.isToggleable();
    }
    
    public void togglePerkActivation(DonationPerk perk) {
        if(!perk.isToggleable())
            return;
        donationPerks.put(perk, !donationPerks.get(perk));
    }

    private void transferOldPerks() {
        if (bankCommand) {
            unlockPerk(DonationPerk.BANK_COMMAND);
            bankCommand = false;
        }
        if (endlessEnergy) {
            unlockPerk(DonationPerk.ENDLESS_ENERGY);
        }
        if (greenThumb) {
            unlockPerk(DonationPerk.GREEN_THUMB);
            greenThumb = false;
        }
        if (lumberLegend) {
            unlockPerk(DonationPerk.LUMBER_LEGEND);
            lumberLegend = false;
        }
        if (sleightOfHand) {
            unlockPerk(DonationPerk.SLEIGHT_OF_HAND);
            sleightOfHand = false;
        }
        if (familiarExpert) {
            unlockPerk(DonationPerk.FAMILIAR_EXPERT);
            familiarExpert = false;
        }
        if (chargeBefriender) {
            unlockPerk(DonationPerk.CHARGE_BEFRIENDER);
            chargeBefriender = false;
        }
        if (treasureGoblin) {
            unlockPerk(DonationPerk.TREASURE_GOBLIN);
            treasureGoblin = false;
        }
        if (herbivore) {
            unlockPerk(DonationPerk.HERBIVORE);
            herbivore = false;
        }
        if (masterFisherman) {
            unlockPerk(DonationPerk.MASTER_FISHERMAN);
            masterFisherman = false;
        }
        if (delicateCraftsman) {
            unlockPerk(DonationPerk.DELICATE_CRAFTSMAN);
            delicateCraftsman = false;
        }
        if (prayerBetrayer) {
            unlockPerk(DonationPerk.PRAYER_BETRAYER);
            prayerBetrayer = false;
        }
        if (avasSecret) {
            unlockPerk(DonationPerk.AVAS_SECRET);
            avasSecret = false;
        }
        if (keyExpert) {
            unlockPerk(DonationPerk.KEY_EXPERT);
            keyExpert = false;
        }
        if (dragonTrainer) {
            unlockPerk(DonationPerk.DRAGON_TRAINER);
            dragonTrainer = false;
        }
        if (gwdSpecialist) {
            unlockPerk(DonationPerk.GWD_SPECIALIST);
            gwdSpecialist = false;
        }
        if (dungeon) {
            unlockPerk(DonationPerk.DUNGEONS_MASTER);
            dungeon = false;
        }
        if (petChanter) {
            unlockPerk(DonationPerk.PETSCHANTER);
            petChanter = false;
        }
        if (perslaysion) {
            unlockPerk(DonationPerk.PERSLAYSION);
            perslaysion = false;
        }
        if (overclocked) {
            unlockPerk(DonationPerk.OVERCLOCKED);
            overclocked = false;
        }
        if (elfFriend) {
            unlockPerk(DonationPerk.ELF__S_FRIEND);
            elfFriend = false;
        }
        if (convincingCook) {
            unlockPerk(DonationPerk.CONVINCING_COOK);
            convincingCook = false;
        }
        if (dedicatedDiviner) {
            unlockPerk(DonationPerk.DEDICATED_DIVINATION);
            dedicatedDiviner = false;
        }
        if (masterMiner) {
            unlockPerk(DonationPerk.MASTER_MINER);
            masterMiner = false;
        }
        if (miniGamer) {
            unlockPerk(DonationPerk.THE_MINI___GAMER);
            miniGamer = false;
        }
        if (masterFledger) {
            unlockPerk(DonationPerk.MASTER_FLEDGER);
            masterFledger = false;
        }
        if (thePyromaniac) {
            unlockPerk(DonationPerk.THE_PYROMANIAC);
            thePyromaniac = false;
        }
        if (huntsman) {
            unlockPerk(DonationPerk.HUNTSMAN);
            huntsman = false;
        }
        if (portsMaster) {
            unlockPerk(DonationPerk.PORTS_MASTER);
            portsMaster = false;
        }
        if (investigator) {
            unlockPerk(DonationPerk.INVESTIGATOR);
            investigator = false;
        }
        if (divineDoubler) {
            unlockPerk(DonationPerk.DIVINE_DOUBLER);
            divineDoubler = false;
        }
        if (imbuedFocus) {
            unlockPerk(DonationPerk.IMBUED_FOCUS);
            imbuedFocus = false;
        }
        if (alchemicSmith) {
            unlockPerk(DonationPerk.ALCHEMIC_SMITHING);
            alchemicSmith = false;
        }
        if (theDiscounter) {
            unlockPerk(DonationPerk.THE_DISCOUNTER);
            theDiscounter = false;
        }
        if (auburyApprentice) {
            unlockPerk(DonationPerk.AUBURY__S_APPRENTICE);
            auburyApprentice = false;
        }
        if (theSkipper) {
            unlockPerk(DonationPerk.THE_SKIPPER);
            theSkipper = false;
        }
        if (theBoxer) {
            unlockPerk(DonationPerk.THE_BOXER);
            theBoxer = false;
        }
        if (theStargazer) {
            unlockPerk(DonationPerk.THE_STARGAZER);
            theStargazer = false;
        }
        if (soulSiphoner) {
            unlockPerk(DonationPerk.SOUL_SIPHONER);
            soulSiphoner = false;
        }
        if (dominionDomination) {
            unlockPerk(DonationPerk.DOMINION_DOMINATION);
            dominionDomination = false;
        }
        if (favoredFamiliars) {
            unlockPerk(DonationPerk.FAVORED_FAMILIARS);
            favoredFamiliars = false;
        }
        if (arcaneAlchemist) {
            unlockPerk(DonationPerk.ARCANE_ALCHEMIST);
            arcaneAlchemist = false;
        }
        if (dropCatcher) {
            unlockPerk(DonationPerk.DROP_CATCHER);
            dropCatcher = false;
        }
        if (theExterminator) {
            unlockPerk(DonationPerk.THE_EXTERMINATOR);
            theExterminator = false;
        }
        if (skillingAddict) {
            unlockPerk(DonationPerk.SKILLING_ADDICT);
            skillingAddict = false;
        }
        if (treeHunter) {
            unlockPerk(DonationPerk.TREE_HUNTER);
            treeHunter = false;
        }
        if (haspetTrainer) {
            unlockPerk(DonationPerk.PET_TRAINER);
            haspetTrainer = false;
        }
        if (dungeonArchitect) {
            unlockPerk(DonationPerk.DUNGEON_ARCHITECT);
            dungeonArchitect = false;
        }
    }

    /**
     * Lets shorten the line here. Have to love our eyes man :(.
     *
     * @param line The interID to print.
     * @param message The String to print.
     */
    private void sendText(int line, String message) {
        player.getPackets().sendIComponentText(1245, line, "<shad=000000>" + message);
    }

    
    public enum DonationPerk {
        BANK_COMMAND(false, "(Access your bank using ;;bank in general areas of Ataraxia.)"),
        
        ENDLESS_ENERGY("(Your run energy will never deplete and stay at 100% forever.)"),
        
        GREEN_THUMB("(All crops are immune to disease and have higher yields.)", "(Your yield will be automatically noted.)"),
        
        LUMBER_LEGEND("(Grants a 33% chance to cut 2 logs and banks all nests received.)"),
        
        SLEIGHT_OF_HAND("(Ensures your success rates to 100% in most aspects of Thieving.)", "(Removes damage taken from Home thieving stalls.)"),
        
        FAMILIAR_EXPERT("(Increases your Familiar timer by 50% and Familiar health by 25%.)"),
        DOUBLE_SURGE("(You now have Double Surge.)"),
        DOUBLE_ESCAPE("(You now have Double ESCAPE.)"),
        DOUBLE_BARGE("(You now have Double ESCAPE.)"),


        CHARGE_BEFRIENDER("(This perk makes all of your T80 Armour/Weapons degrade 50% less than normal.)", "(As well as providing 25% less drain rate for Armour and Weapons above T80.)"),
        
        TREASURE_GOBLIN("(Charms are automatically sent to your Bank after slaying monsters.)", "(Also increases charms drop rate by 25%.)"),
        
        HERBIVORE("(Monsters have the ability to drop a box with 20 random clean herbs.)", "(Increases Herblore experience 25% and extends potion timers 200%.)"),
        
        MASTER_FISHERMAN("(Allows you at a chance to catch double the fish at once.)"),
        
        DELICATE_CRAFTSMAN("(Gain an extra 10% experience whilst spinning on the wheel.)", "(Gain 25% experience for crafting dragonhide armours.)", "(Removes the requirement of needing thread.)"),
        
        PRAYER_BETRAYER("(Your Prayer points decrease at a 25% lower rate.)", "(Increases your Prayer training experience by 25%.)"),
        
        AVAS_SECRET("(Acts as a permanently equipped Ava's Alerter.)", "(You have a higher chance of recovering fired ammunition.)"),
        
        KEY_EXPERT("(Receive double the loot upon opening the Crystal chest at Home.)"),
        
        DRAGON_TRAINER("(Acts as a permanently equipped anti-dragon shield.)", "(Increases the chance of receiving a baby pet dragon by 25%.)"),
        
        GWD_SPECIALIST("(Allows you to enter any of the Godwars boss rooms without killcount.)"),
        
        DUNGEONS_MASTER("(Doubles binds and gives 25% extra experience and tokens.)"),
        
        PETSCHANTER("(Increases Boss pet drop rates 25% and Skilling pet rates 50%.)"),
        
        PERSLAYSION("(Allows you to persuade Kuradal to reset Slayer tasks free of charge.)", "(Increases experience and Slayer points 25% on task completion.)"),
        
        OVERCLOCKED("(Your Aura active times are now doubled. Also halves recharge time.)"),
        
        ELF__S_FRIEND("(Allows instant access into the beautiful Prifddinas city.)"),
        
        CONVINCING_COOK("(Food never burns.)", "(Increases Cooking experience 15% and increases your speed 33%.)"),
        
        DEDICATED_DIVINATION("(Increases the rate at which you receive Chronicle Fragments by 25%.)", "(Increases chance of receiving enriched memories by 25%.)", "(Increases Divination experience gained by 25%.)"),
        
        MASTER_MINER("(Grants a 33% chance to mine 2 ores at once.)", "(Increases your Mining experience gained by 25%.)"),
        
        THE_MINI___GAMER("(Doubles your points received from minigame rewards.)"),
        
        MASTER_FLEDGER("(Increases the speed of Fletching items by 2 times.)", "(No longer require bowstring for (u) bows and Feathers for unf ammo.)"),
        
        THE_PYROMANIAC("(Gives a 25% higher chance of receiving fire spirits while Firemaking.)", "(Bonfiring will now grant +15% more experience.)", "(Health boost from stoking a Bonfire increased to 200%.)"),
        
        HUNTSMAN("(You can now place an extra 2 traps at once (7 traps at level 80+).)", "(Gain an additional +25% experience on successful catches.)", "(+15% success rate increase and a 10% chance to receive double loot.)"),
        
        PORTS_MASTER("(Your PoP ships will have a 20% increased return rate.)", "(Receive +15% more rewards from successful PoP voyages.)"),
        
        INVESTIGATOR("(Allows you to finish a clue scroll instantly.)"),
        
        DIVINE_DOUBLER("(Allows you to gather double your divine limit every day.)"),
        
        IMBUED_FOCUS("(Drain the energy from Runespan creatures at a slower rate.)"),
        
        ALCHEMIC_SMITHING("(Grants you the ability to smelt bars without coal.)", "(You will no longer fail when smelting iron ore.)"),
        
        THE_DISCOUNTER("(50% off Instances.)", "(50% off Repairing.)", "(50% off resetting Reaper Assignments.)", "(Substantial discounts on Port Ships prices (up to 50% on Epsilon!).)"),
        
        AUBURY__S_APPRENTICE("(Each spellcast has a 50% chance to save all of your runes.)"),
        
        THE_SKIPPER("(Skips the maze of Barrows.)"),
        
        THE_BOXER("(Gives you two rewards instead of one when opening Mystery Boxes.)"),
        
        THE_STARGAZER("(Teleports you directly to the Shooting star using ;;star.)"),
        
        SOUL_SIPHONER("(Soulsplit is 25% more effective for healing.)"),
        
        FAVORED_FAMILIARS("(Familiars no longer require Scrolls to activate their special ability.)"),
        
        ARCANE_ALCHEMIST("(Removes the requirement for Runes when using High Alchemy.)"),
        
        DROP_CATCHER("(All of the drops from Monsters are instantly caught to your inventory!)"),
        
        DOMINION_DOMINATION("(Gives an extra boss killcount each kill in Dominion Tower.)", "(Gives 2.5x factor each kill in Dominion Tower.)", "(Gives 25% more bonus experience from DT rewards.)", "(Gives 25% more experience to a skill from DT rewards.)", "(Increases the attack power of Dreadnips 25%.)"),
        
        THE_EXTERMINATOR("(25% damage boost within Pest Control.)","(Double the amount of rolls on the Pest Control loot table.)"),
        
        SKILLING_ADDICT("(25% More XP from skilling contracts.)",  "(25% More rewards from skilling contracts.)", "(Temporary contract modifications last twice as long.)"),
        
        TREE_HUNTER("(25% More XP from Evil Tree related actions.)", "(2x Better chance at getting evil herbs, bark, and seeds.)", "(Free banking when fighting the Evil Tree.)", "(Free teleports to the Evil Tree.)"),
        
        PET_TRAINER("(Add Pet perks to Bossing and Skilling pets.)"),
        
        DUNGEON_ARCHITECT("(Create your own private dungeoneering instances.)", "(Up to one friend may join your instance.)"),
        
        ;
        @Getter
        String[] benefits;
        @Getter
        private final boolean toggleable;
        
        DonationPerk(String... benefits) {
            this(true, benefits);
        }
        
        DonationPerk(boolean toggleable, String... benefits) {
            this.benefits = benefits;
            this.toggleable = toggleable;
        }

        @Override
        public String toString() {
            String name = name();
            return Utils.formatPlayerNameForDisplay(name.replace("___", "-").replace("__", "'"));
        }
        
    }
    
    
    /**
     * Displays players activated game perks.
     */
    public void displayAvailablePerks() {
        for (int i = 0; i < 309; i++)
            player.getPackets().sendIComponentText(1245, i, "");
        sendText(330, Settings.SERVER_NAME + " Game Perks");
        int line = 13;
        sendText(line++, Colors.BLACK + "Game Perks can be purchased from our ::store.");
        sendText(line++, Colors.BLACK + "You can toggle the perks activation status by doing ::toggleperks.");
        sendText(line++, Colors.RED + "Red - locked</col>  -  " + Colors.GREEN + "Green - unlocked (active)</col> - "+ Colors.YELLOW + "Yellow - unlocked (inactive)</col>.");
        sendText(line++, Colors.BLACK+ "Some perks can't be toggled these are indicated by (i).");
        sendText(line++, "</shad>----------------------");
        for (DonationPerk perk : DonationPerk.values()) {
            String color = (donationPerks.containsKey(perk) ? (hasPerkActive(perk) ? Colors.GREEN : Colors.YELLOW) : Colors.RED);
            if(player.isGroupIronman() && GIM_DISABLED.contains(perk)) {
                color = Colors.RED;
            }
            sendText(line++, color + perk + (!perk.isToggleable() ? " (i)" : ""));
            for(String benefit : perk.benefits)
                sendText(line++, color + benefit);
            line++;
        }
        player.getPackets().sendRunScript(17037, line - 10);
        player.getInterfaceManager().sendInterface(1245);
    }
    

    public static void transferPerks(Player from, Player to) {
        to.getPerkManager().setDonationPerks(from.getPerkManager().getDonationPerks());
        for (int i = 0; i < from.getBanks().size() - 1; i++) {
            to.addBank(false, new Bank());
        }
    }
    
}