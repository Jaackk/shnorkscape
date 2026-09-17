package com.rs.game.player.content.titles;

import com.rs.game.player.Player;
import com.rs.game.player.content.dropcollection.DropCollectionConstants;
import com.rs.game.player.content.skillingcontracts.SkillingContractTracker;
import com.rs.utils.Colors;
import com.rs.utils.Utils;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * ataraxia-server
 * paolo 02/11/2019
 * #Shnek6969
 */

enum TitleType {ALL, PVM,SKILLING,MISC,DONATOR}

public enum PlayerTitle {


    Ataraxian(1, Colors.GOLD +" the Ataraxian</col>", false,"Have voted in 3 or more in-game polls.", TitleType.MISC){
        @Override
        public boolean hasRequirements(Player player) {
            return player.totalBoothVotes >= 3;
        }
    },
    NOVICE(2,  "Novice " + Colors.WHITE, true,"Be on the Novice EXP mode.", TitleType.MISC){
        @Override
        public boolean hasRequirements(Player player) {
            return player.isNovice() || player.isNoviceIronMan();
        }
    },
    EXPERT(3, "Expert " + Colors.WHITE, true,"Be on the Expert EXP mode.", TitleType.MISC){
        @Override
        public boolean hasRequirements(Player player) {
            return player.isExpert() || player.isExpertIronMan();
        }
    },
    LEGENDARY(4, "Legendary " + Colors.WHITE, true,"Be on the Legendary EXP mode.", TitleType.MISC){
        @Override
        public boolean hasRequirements(Player player) {
            return player.isLegendary() || player.isIronMan();
        }
    },
    THE_NOVICE(5, Colors.DARK_RED + " the Novice", false,"Be on the Novice EXP mode.", TitleType.MISC){
        @Override
        public boolean hasRequirements(Player player) {
            return player.isNovice() || player.isNoviceIronMan();
        }
    },
    THE_EXPERT(6, Colors.DARK_RED + " the Expert", false,"Be on the Expert EXP mode.", TitleType.MISC){
        @Override
        public boolean hasRequirements(Player player) {
            return player.isExpert() || player.isExpertIronMan();
        }
    },
    THE_LEGENDARY(7, Colors.DARK_RED + " the Legendary", false,"Be on the Legendary EXP mode.", TitleType.MISC){
        @Override
        public boolean hasRequirements(Player player) {
            return player.isLegendary() || player.isIronMan();
        }
    },
    THE_MAXED(8, "<col=D966AF>The Maxed ", true,"Claim the max cape", TitleType.SKILLING){
        @Override
        public boolean hasRequirements(Player player) {
            return player.isMax();
        }
    },
    ENTHUSIAST(9, "<col=977EBF>Enthusiast " + Colors.WHITE, true,"Claim the max cape", TitleType.MISC){
        @Override
        public boolean hasRequirements(Player player) {
            return player.getVotes() >= 250;
        }
    },
    NOLIFER(10, "<col=FC0000>No-lifer " + Colors.WHITE, true,"Claim the max cape", TitleType.MISC){
        @Override
        public boolean hasRequirements(Player player) {
            return Utils.getMinutesPlayed(player) >= 15000;
        }
    },
    OF_GUTHIX(11, Colors.GREEN + "<shad=000000> of Guthix", false,"Exchanged a total of 100 Chronicle Fragments.", TitleType.SKILLING){
        @Override
        public boolean hasRequirements(Player player) {
            return player.hasGuthixTitleUnlocked();
        }
    },
    OF_SEASONS(12,  Colors.GREEN + "<shad=000000> of Seasons", false,"Combined 4 seasonal cloaks", TitleType.MISC){
        @Override
        public boolean hasRequirements(Player player) {
            return player.hasCombinedCloaks();
        }
    },
    THE_MILLIONAIRE(13,  Colors.BROWN + " the Millionaire", false,"Thrown at least 100m coins into the Well.", TitleType.MISC){
        @Override
        public boolean hasRequirements(Player player) {
            return player.donatedToWell >= 100_000_000;
        }
    },
    THE_CHARITABLE(14,  Colors.GOLD + " the Charitable", false,"Thrown at least 1b coins into the Well.", TitleType.MISC){
        @Override
        public boolean hasRequirements(Player player) {
            return player.donatedToWell >= 1_000_000_000;
        }
    },
    THE_BILLIONAIRE(15,  "<col=fab402> the Billionaire", false,"Thrown at least 5b coins into the Well.", TitleType.MISC){
        @Override
        public boolean hasRequirements(Player player) {
            return player.donatedToWell >= 5_000_000_000L;
        }
    },
    BRONZE_MEMBER(16,  "<col=B56A02>Bronze member " + Colors.WHITE, true,"Be ranked as Bronze member (20$)", TitleType.DONATOR){
        @Override
        public boolean hasRequirements(Player player) {
            return player.isDonator();
        }
    },
    SILVER_MEMBER(17,  "<col=A3A3A3>Silver member " + Colors.WHITE, true,"Be ranked as Silver member (50$)", TitleType.DONATOR){
        @Override
        public boolean hasRequirements(Player player) {
            return player.isExtremeDonator();
        }
    },
    GOLD_MEMBER(18,  "<col=D6D600>Gold member " + Colors.WHITE, true,"Be ranked as Gold member (100$)", TitleType.DONATOR){
        @Override
        public boolean hasRequirements(Player player) {
            return player.isLegendaryDonator();
        }
    },
    PLATINUM_MEMBER(19,  "<col=41917B>Platinum member "  + Colors.WHITE, true,"Be ranked as Platinum member (250$", TitleType.DONATOR){
        @Override
        public boolean hasRequirements(Player player) {
            return player.isSupremeDonator();
        }
    },
    DIAMOND_MEMBER(20,  "<col=13D6D6>Diamond member " + Colors.WHITE, true,"Be ranked as Diamond member (500$)", TitleType.DONATOR){
        @Override
        public boolean hasRequirements(Player player) {
            return player.isUltimateDonator();
        }
    },
    MASTER_MEMBER(21,  "<col=9400c1>Master member " + Colors.WHITE, true,"Be ranked as Master member (1000$)", TitleType.DONATOR){
        @Override
        public boolean hasRequirements(Player player) {
            return player.isMasterDonator();
        }
    },
    THE_PERFECTIONIST(22,  "<col=6200FF>The Perfectionist " + Colors.WHITE, true,"Have claimed the Completionist cape (t)", TitleType.MISC){
        @Override
        public boolean hasRequirements(Player player) {
            return player.isCompT();
        }
    },
    THE_COMPLETIONIST(23,  "<col=A200FF>The Completionist " + Colors.WHITE, true,"Have claimed the Completionist cape", TitleType.MISC){
        @Override
        public boolean hasRequirements(Player player) {
            return player.isComp();
        }
    },
    SKILLING_CHAMPION(24,  Colors.RED + "Skilling Champion " + Colors.WHITE, true, "Being a skilling champion", TitleType.SKILLING){
        @Override
        public boolean hasRequirements(Player player) {
            return player.getDisplayName().equals(SkillingContractTracker.getSingleton().displayLastWinner());
        }
    },
    SKILLING_ADDICT(25,  Colors.DPURPLE + "Skilling Addict " + Colors.WHITE, true,"Have atleast 1,000 contracts completed", TitleType.SKILLING){
        @Override
        public boolean hasRequirements(Player player) {
            return player.getContracts().totalContracts >= 1000;
        }
    },
    JACK_OF_TRADES(26,  ", " + Colors.GRAY + "Jack of Trades", false,"Interact with 5 non-combat pets", TitleType.MISC){
        @Override
        public boolean hasRequirements(Player player) {
            return player.getInteractedNoncombatPets().size() >= 5;
        }
    },
    JACK_OF_ALL_TRADES(27,  ", " + Colors.ORANGE + "Jack of All Trades", false,"Interact with 18 non-combat pets", TitleType.MISC){
        @Override
        public boolean hasRequirements(Player player) {
            return player.getInteractedNoncombatPets().size() >= 18;
        }
    },
    EVIL(28,  Colors.RED + "Evil " + Colors.WHITE, true,"Have killed 100 or more Evil Trees.", TitleType.SKILLING){
        @Override
        public boolean hasRequirements(Player player) {
            return player.evilTreeKc >= 100;
        }
    },
    BUNNY(29,  Colors.ORANGE + " the Bunny", false,"Have claimed this easter title.", TitleType.MISC){
        @Override
        public boolean hasRequirements(Player player) {
            return player.evilTreeKc >= 100;
        }
    },
    GENERAL(30,   "<col=00ff00>General " + Colors.WHITE, true,"Read Demonic title scroll (general)", TitleType.PVM){
        @Override
        public boolean hasRequirements(Player player) {
            return player.DFMScroll[0];
        }
    },
    EXECUTIONER(31,    "<col=990000>Executioner " + Colors.WHITE, true,"Read Demonic title scroll (executioner)", TitleType.PVM){
        @Override
        public boolean hasRequirements(Player player) {
            return player.DFMScroll[1];
        }
    },
    CASTELLAN(32,    "<col=996633>Castellan " + Colors.WHITE, true,"Read Demonic title scroll (Castellan)", TitleType.PVM){
        @Override
        public boolean hasRequirements(Player player) {
            return player.DFMScroll[2];
        }
    },
    DEACON(33,     "<col=ffcc00>Deacon " + Colors.WHITE, true,"Read Demonic title scroll (Deacon)", TitleType.PVM){
        @Override
        public boolean hasRequirements(Player player) {
            return player.DFMScroll[3];
        }
    },
    THE_BLAZING(34,     "<col=ff9900> the Blazing", false,"Read Demonic title scroll (Blazing)", TitleType.PVM){
        @Override
        public boolean hasRequirements(Player player) {
            return player.DFMScroll[4];
        }
    },
    Corrupting(35,     "<col=33cc33> the Corrupting", false,"Read Demonic title scroll (Corrupting)", TitleType.PVM){
        @Override
        public boolean hasRequirements(Player player) {
            return player.DFMScroll[5];
        }
    },
    Frostborn(36,     "<col=00ffff> the Frostborn", false,"Read Demonic title scroll (Frostborn)", TitleType.PVM){
        @Override
        public boolean hasRequirements(Player player) {
            return player.DFMScroll[6];
        }
    },
    Glorious(37,     "<col=ffcc66> the Glorious", false,"Read Demonic title scroll (Glorious)", TitleType.PVM){
        @Override
        public boolean hasRequirements(Player player) {
            return player.DFMScroll[7];
        }
    },
    Infernal(38,     "<col=b32d00> the Infernal", false,"Read Demonic title scroll (Infernal)", TitleType.PVM){
        @Override
        public boolean hasRequirements(Player player) {
            return player.DFMScroll[8];
        }
    },
    Obscured(39,     "<col=a6a6a6> the Obscured", false,"Read Demonic title scroll (Obscured)", TitleType.PVM){
        @Override
        public boolean hasRequirements(Player player) {
            return player.DFMScroll[9];
        }
    },
    Pestilent(40,    "<col=00ff00> the Pestilent", false,"Read Demonic title scroll (Pestilent)", TitleType.PVM){
        @Override
        public boolean hasRequirements(Player player) {
            return player.DFMScroll[10];
        }
    },
    Rending(41,    "<col=ee6600> the Rending", false,"Read Demonic title scroll (Rending)", TitleType.PVM){
        @Override
        public boolean hasRequirements(Player player) {
            return player.DFMScroll[11];
        }
    },
    Shattering(42,    "<col=00cccc> the Shattering", false,"Read Demonic title scroll (Shattering)", TitleType.PVM){
        @Override
        public boolean hasRequirements(Player player) {
            return player.DFMScroll[12];
        }
    },
    Terrifying(42,    "<col=999999> the Terrifying", false,"Read Demonic title scroll (Terrifying)", TitleType.PVM){
        @Override
        public boolean hasRequirements(Player player) {
            return player.DFMScroll[13];
        }
    },

    Reaper(43,    "<col=DF0101> the Reaper", false,"Complete a total of 500 reaper contracts", TitleType.PVM){
        @Override
        public boolean hasRequirements(Player player) {
            return player.getTotalContract() >= 500;
        }
    },
    INSANE_REAPER(44,    "<col=DF0101><shad=9D1309> the Insane Reaper</shad></col>", false,"Complete a total of 1,250 reaper contracts", TitleType.PVM){
        @Override
        public boolean hasRequirements(Player player) {
            return player.getTotalContract() >= 1250;
        }
    },
    FINAL_BOSS(45,    "<col=DF0101>Final Boss</col> ", true,"You need to complete 5,000 reaper contract kills", TitleType.PVM){
        @Override
        public boolean hasRequirements(Player player) {
            return player.getTotalKills() >= 5000;
        }
    },
    INSANE_FINAL_BOSS(46,    "<col=DF0101><shad=9D1309>Insane Final Boss</shad></col> ", true,"You need to complete 15,000 reaper contract kills", TitleType.PVM){
        @Override
        public boolean hasRequirements(Player player) {
            return player.getTotalKills() >= 15000;
        }
    },
    PLAYER_OF_THE_MONTH(47,    "<col=1abc9c>Player of the Month </col>", true,"You need to receive the player of the month reward.", TitleType.MISC){
        @Override
        public boolean hasRequirements(Player player) {
            return player.isPlayerOfTheMonth();
        }
    },
    THE_WARDEN(48,    "<col=84D477> the Warden</col>", false,"Have an 500+ Telos enrage kill", TitleType.PVM){
        @Override
        public boolean hasRequirements(Player player) {
            return player.getMaxEnrage() >= 500;
        }
    },
    THE_WARDEN_2(49,    "<col=BEBEBE> the Warden</col>", false,"Have an 2000+ Telos enrage kill", TitleType.PVM){
        @Override
        public boolean hasRequirements(Player player) {
            return player.getMaxEnrage() >= 2000;
        }
    },//<col=BEBEBE>
    THE_WARDEN_3(50,    "<col=FAB402>  the Warden</col>", false,"Have an 4000+ Telos enrage kill", TitleType.PVM){
        @Override
        public boolean hasRequirements(Player player) {
            return player.getMaxEnrage() >= 4000;
        }
    },
    THE_CABIN(51,     Colors.DARK_RED + " the Cabin Boy" , false,"Have a Portscore of at least 1", TitleType.MISC){
        @Override
        public boolean hasRequirements(Player player) {
            return player.getPorts().portScore > 0;
        }
    },
    BOSUN(52,     Colors.DARK_RED + "Bo'sun</col> " , true,"Have a Portscore of at least 400", TitleType.MISC){
        @Override
        public boolean hasRequirements(Player player) {
            return player.getPorts().portScore >= 400;
        }
    },
    FIRST_MATE(53,     Colors.DARK_RED + "First Mate</col> " , true,"Have a Portscore of at least 800", TitleType.MISC){
        @Override
        public boolean hasRequirements(Player player) {
            return player.getPorts().portScore >= 800;
        }
    },
    CAPN(54,     Colors.DARK_RED + "Cap'n</col> " , true,"Have a Portscore of at least 1200", TitleType.MISC){
        @Override
        public boolean hasRequirements(Player player) {
            return player.getPorts().portScore >= 1200;
        }
    },
    COMMODORE(55,     Colors.DARK_RED + "Commodore</col> " , true,"Have a Portscore of at least 1600", TitleType.MISC){
        @Override
        public boolean hasRequirements(Player player) {
            return player.getPorts().portScore >= 1600;
        }
    },
    ADMIRAL(56,     Colors.DARK_RED + "Admiral</col> " , true,"Have a Portscore of at least 2000", TitleType.MISC){
        @Override
        public boolean hasRequirements(Player player) {
            return player.getPorts().portScore >= 2000;
        }
    },
    ADMIRAL_OF_THE_FLEET(57,     Colors.DARK_RED + "Admiral of the Fleet</col> " , true,"Have a Portscore of at least 3500", TitleType.MISC){
        @Override
        public boolean hasRequirements(Player player) {
            return player.getPorts().portScore >= 3500;
        }
    },
    PORTMASTER(58,     Colors.DARK_RED + "Portmaster</col> " , true,"Have a Portscore of at least 4500", TitleType.MISC){
        @Override
        public boolean hasRequirements(Player player) {
            return player.getPorts().portScore >= 4500;
        }
    },
    //start dropcollection
    STRENGTH_OF_OURGS(59,     "<col=84D477> Strength of Ourgs</col> " , false,"Have a complete dropcollection of General Graardor", TitleType.PVM){
        @Override
        public boolean hasRequirements(Player player) {
            return player.getDropCollectionHandler().completedBossCollection(DropCollectionConstants.BOSS_DATA.BANDOS);
        }
    },
    THE_KINGSLAYER(60,     Colors.DARK_RED + " the Kingslayer" , false,"Have a complete dropcollection of the Kalphite King", TitleType.PVM){
        @Override
        public boolean hasRequirements(Player player) {
            return player.getDropCollectionHandler().completedBossCollection(DropCollectionConstants.BOSS_DATA.KK);
        }
    },
    THE_QUEENSLAYER(61,     Colors.LIME + " the Queenslayer" , false,"Have a complete dropcollection of the Kalphite Queen", TitleType.PVM){
        @Override
        public boolean hasRequirements(Player player) {
            return player.getDropCollectionHandler().completedBossCollection(DropCollectionConstants.BOSS_DATA.KALPHITE_QUEEN);
        }
    },
    LAST_RIDER(62,     Colors.BLACK + " the Last Rider" , false,"Have a complete dropcollection of the King Black Dragon", TitleType.PVM){
        @Override
        public boolean hasRequirements(Player player) {
            return player.getDropCollectionHandler().completedBossCollection(DropCollectionConstants.BOSS_DATA.KBD);
        }
    },
    SWIFTNESS_OF_THE_AVIANSIE(63,     Colors.GRAY + " Swiftness of the Aviansie" , false,"Have a complete dropcollection of Kree'arra", TitleType.PVM){
        @Override
        public boolean hasRequirements(Player player) {
            return player.getDropCollectionHandler().completedBossCollection(DropCollectionConstants.BOSS_DATA.ARMADYL);
        }
    },
    BRAWN_OF_A_TSUTSAROTH(64,     Colors.DARK_RED + " Brawn of a Tsutsaroth" , false,"Have a complete dropcollection of K'ril Tsutsaroth", TitleType.PVM){
        @Override
        public boolean hasRequirements(Player player) {
            return player.getDropCollectionHandler().completedBossCollection(DropCollectionConstants.BOSS_DATA.ZAMORAK);
        }
    },
    TENACITY_OF_ZARYTES(65,     Colors.PINK + " Tenacity of Zarytes" , false,"Have a complete dropcollection of Nex", TitleType.PVM){
        @Override
        public boolean hasRequirements(Player player) {
            return player.getDropCollectionHandler().completedBossCollection(DropCollectionConstants.BOSS_DATA.NEX);
        }
    },
    GRAVEROBBER(66,     Colors.BROWN + "Graverobber</col> " , true,"Have a complete dropcollection of Barrows", TitleType.PVM){
        @Override
        public boolean hasRequirements(Player player) {
            return player.getDropCollectionHandler().completedBossCollection(DropCollectionConstants.BOSS_DATA.BARROW);
        }
    },
    FINESSE_OF_THE_ICYNE(67,     Colors.ORANGE + " Finesse of the Icyene" , false,"Have a complete dropcollection of Commander Zilyana", TitleType.PVM){
        @Override
        public boolean hasRequirements(Player player) {
            return player.getDropCollectionHandler().completedBossCollection(DropCollectionConstants.BOSS_DATA.SARADOMIN);
        }
    },
    DARK_CORE(68,     Colors.PBLUE + "Dark Core</col>" , true,"Have a complete dropcollection of Corporeal Beast", TitleType.PVM){
        @Override
        public boolean hasRequirements(Player player) {
            return player.getDropCollectionHandler().completedBossCollection(DropCollectionConstants.BOSS_DATA.CORP);
        }
    },
    SHAPESHIFTER(69,     Colors.PBLUE + "Shapeshifter</col> " , true,"Have a complete dropcollection of Helwyr", TitleType.PVM){
        @Override
        public boolean hasRequirements(Player player) {
            return player.getDropCollectionHandler().completedBossCollection(DropCollectionConstants.BOSS_DATA.HELWYR);
        }
    },
    FACELESS_ONE(71,     Colors.GRAY + " the Faceless One</col> " , false,"Have a complete dropcollection of Gregorovic", TitleType.PVM){
        @Override
        public boolean hasRequirements(Player player) {
            return player.getDropCollectionHandler().completedBossCollection(DropCollectionConstants.BOSS_DATA.GREG);
        }
    },
    UNDERGROUND(72,     Colors.BROWN + " of the Underground</col> " , false,"Have a complete dropcollection of the Giant Mole", TitleType.PVM){
        @Override
        public boolean hasRequirements(Player player) {
            return player.getDropCollectionHandler().completedBossCollection(DropCollectionConstants.BOSS_DATA.MOLE);
        }
    },
    EXPERIMENTAL(73,     Colors.BLACK + " the Experiment</col> " , false,"Have a complete dropcollection of the Queen Black Dragon", TitleType.PVM){
        @Override
        public boolean hasRequirements(Player player) {
            return player.getDropCollectionHandler().completedBossCollection(DropCollectionConstants.BOSS_DATA.QBD);
        }
    },
    PRAESUL(74,     Colors.PINK + " of the Praesul</col> " , false,"Have a complete dropcollection of Nex: Angel of Death", TitleType.PVM){
        @Override
        public boolean hasRequirements(Player player) {
            return player.getDropCollectionHandler().completedBossCollection(DropCollectionConstants.BOSS_DATA.AOD);
        }
    },
    LONE_FURY(75,     Colors.ORANGE + " the Lone Fury</col> " , false,"Have a complete dropcollection of the Twin Furies", TitleType.PVM){
        @Override
        public boolean hasRequirements(Player player) {
            return player.getDropCollectionHandler().completedBossCollection(DropCollectionConstants.BOSS_DATA.TWINS);
        }
    },
    OMENS(76,     Colors.LIGHT_GRAY + " of Omens</col> " , false,"Have a complete dropcollection of the Vorago", TitleType.PVM){
        @Override
        public boolean hasRequirements(Player player) {
            return player.getDropCollectionHandler().completedBossCollection(DropCollectionConstants.BOSS_DATA.VORAGO);
        }
    },
    DORMANT(77,     Colors.CYAN + " the Dormant</col> " , false,"Have a complete dropcollection of Telos", TitleType.PVM){
        @Override
        public boolean hasRequirements(Player player) {
            return player.getDropCollectionHandler().completedBossCollection(DropCollectionConstants.BOSS_DATA.TELOS);
        }
    },
    DRAGONRIDER(78,     Colors.PBLUE + " the Dragonrider</col> " , false,"Have a complete dropcollection of Vindicta & Gorvek", TitleType.PVM){
        @Override
        public boolean hasRequirements(Player player) {
            return player.getDropCollectionHandler().completedBossCollection(DropCollectionConstants.BOSS_DATA.VINDY);
        }
    };


    private final int titleId;
    @Getter
    public String title;
    @Getter
    public String requirementsString;
    @Getter
    public  TitleType titleType;
    @Getter
    public boolean beforeName;
    public abstract boolean hasRequirements(Player player);

    public int getTitleId(){
        return titleId + 510; //adding 2000 since we don't want to overwrite existing cs2 stored titles
    }

    PlayerTitle(int id, String title, boolean beforeName, String requirementsString, TitleType titleType){
        this.titleId = id;
        this.title = title;
        this.beforeName = beforeName;
        this.requirementsString = requirementsString;
        this.titleType = titleType;
    }

    public static List<PlayerTitle> getByType(TitleType type){
        List<PlayerTitle> titles = new ArrayList<>();
        for (PlayerTitle value : PlayerTitle.values()) {
            if(value.titleType == type)
                titles.add(value);
        }
        return  titles;
    }
    @Getter
    public static HashMap<Integer, PlayerTitle> titlesById;
    static {
        titlesById = new HashMap<>();
        for (PlayerTitle value : PlayerTitle.values()) {
            titlesById.put(value.getTitleId(), value);
        }
    }
}
