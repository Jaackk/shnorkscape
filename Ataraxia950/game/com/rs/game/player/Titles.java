package com.rs.game.player;

import com.google.common.collect.ImmutableSet;
import com.rs.Settings;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.activities.dfm.DemonFlashMobs;
import com.rs.game.player.content.skillingcontracts.SkillingContractTracker;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

import java.io.Serializable;

/**
 * Used to handle player-unlocked titles.
 *
 * @author Noel
 */
public class Titles implements Serializable {

    /**
     * The generated serial ID.
     */
    private static final long serialVersionUID = -8142899759527457838L;

    /**
     * The player-class to save to.
     */
    private transient Player player;

    /**
     * The player to set the class to.
     *
     * @param player
     */
    protected void setPlayer(final Player player) {
        this.player = player;
    }

    /**
     * Sends the component text on interface.
     *
     * @param componentId the componentId.
     * @param text the String to send.
     */
    private void sendString(final int componentId, final String text) {
        player.getPackets().sendIComponentText(1082, componentId, " " + text);
    }

    /**
     * Checks if the player is a male or not.
     *
     * @return true if Male.
     */
    private boolean isMale() {
        return player.getAppearence().isMale();
    }

    private boolean isIronMan() {
        // this method mashes all the ironman modes to one boolean besides HC Ironman.
        return player.isIronMan() || player.isNoviceIronMan() || player.isExpertIronMan() || player.isIntermediateIronMan();
    }

    /**
     * The players username.
     *
     * @return the Username to return.
     */
    private String username() {
        return player.getDisplayName();
    }

    /**
     * Opens the Title shop.
     */
    public void openShop() {

        /**
         * Clean out the interface.
         */
        for (int i = 0; i < Utils.getInterfaceDefinitionsComponentsSize(1082); i++) {
            sendString(i, "");
        }

        /**
         * Sending Essentials.
         */
        player.getPackets().sendHideIComponent(1082, 1, true);
        sendString(1, Colors.RED + Settings.SERVER_NAME + " Title manager");
        sendString(11,
                Colors.RED + "Red = Locked" + Colors.WHITE + ".   " + Colors.GREEN + "Green = Unlocked" + Colors.WHITE
                        + ".<br>" + Colors.WHITE
                        + "To activate a Title of your choice - press on the requirement text.");
        sendString(41, Colors.CYAN + "   - Title previews:");
        sendString(42, Colors.CYAN + "   - Requirements:");

        /**
         * Title Previews.
         */
        sendString(30, Colors.GRAY + (isMale() ? "Ironman" : "Ironwoman") + " " + Colors.WHITE + username());
        sendString(32, Colors.WHITE + username() + " " + Colors.GRAY + (isMale() ? "the Ironman" : "the Ironwoman"));
        sendString(34, Colors.DARK_RED + (isMale() ? "HC Ironman" : "HC Ironwoman") + " " + Colors.WHITE + username());
        sendString(36,
                Colors.WHITE + username() + " " + Colors.DARK_RED + (isMale() ? "the HC Ironman" : "the HC Ironwoman"));
        sendString(38, Colors.WHITE + username() + " " + Colors.DARK_RED + "the Survivor");

       /* sendString(49, Colors.DARK_RED + "Novice " + Colors.WHITE + username());
        sendString(51, Colors.DARK_RED + "Expert " + Colors.WHITE + username());
        sendString(53, Colors.DARK_RED + "Legendary " + Colors.WHITE + username()); */
     //   sendString(55, Colors.WHITE + username() + Colors.DARK_RED + " the Novice");
     //   sendString(57, Colors.WHITE + username() + Colors.DARK_RED + " the Expert");
     //   sendString(59, Colors.WHITE + username() + Colors.DARK_RED + " the Legendary");

        sendString(85, Colors.PINK + Colors.SHAD + "Custom user titles");
        sendString(190, Colors.RCYAN + Colors.SHAD + "Christmas Titles");

        /** Goodwill well titles **/
        //sendString(62, Colors.white + username() + Colors.brown + " the Wishful");
        //sendString(64, Colors.white + username() + Colors.brown + " the Generous");
       // sendString(97, Colors.WHITE + username() + Colors.BROWN + " the Millionaire");
       // sendString(99, Colors.WHITE + username() + Colors.GOLD + " the Charitable");
       // sendString(101, Colors.WHITE + username() + "<col=fab402> the Billionaire");

        /** Prifddinas titles **/
        sendString(74, Colors.WHITE + username() + "<col=FF08A0> of the Trahaearn");
        sendString(76, Colors.WHITE + username() + "<col=964F03> of the Hefin");

        /** Cloak of Seasons combination **/
      //  sendString(94, Colors.WHITE + username() + Colors.GREEN + "<shad=000000> of Seasons");

        /** Chronicle Fragment offering **/
       // sendString(92, Colors.WHITE + username() + Colors.GREEN + "<shad=000000> of Guthix");

        /** Member Rank titles **/
      /*  sendString(62, "<col=B56A02>Bronze member " + Colors.WHITE + username());
        sendString(64, "<col=A3A3A3>Silver member " + Colors.WHITE + username());
        sendString(66, "<col=D6D600>Gold member " + Colors.WHITE + username());
        sendString(68, "<col=41917B>Platinum member " + Colors.WHITE + username());
        sendString(70, "<col=13D6D6>Diamond member " + Colors.WHITE + username());
        sendString(72, "<col=9400c1>Master member " + Colors.WHITE + username()); */

        /** Special titles **/
        //sendString(88, "<col=FC0000>No-lifer " + Colors.WHITE + username());
        //sendString(90, "<col=977EBF>Enthusiast " + Colors.WHITE + username());
        //sendString(79, "<col=D966AF>The Maxed " + Colors.WHITE + username());
      //  sendString(81, "<col=A200FF>The Completionist " + Colors.WHITE + username());
      //  sendString(83, "<col=6200FF>The Perfectionist " + Colors.WHITE + username());

        /**
         * Title Description/Requirement.
         */
        sendString(31, (isIronMan() ? Colors.GREEN : Colors.RED) + "Be an "
                + (isMale() ? "Ironman" : "Ironwoman") + " to use this title.");
        sendString(33, (isIronMan() ? Colors.GREEN : Colors.RED) + "Be an "
                + (isMale() ? "Ironman" : "Ironwoman") + " to use this title.");
        sendString(35, (player.isHCIronMan() ? Colors.GREEN : Colors.RED) + "Be a "
                + (isMale() ? "HC Ironman" : "HC Ironwoman") + " to use this title.");
        sendString(37, (player.isHCIronMan() ? Colors.GREEN : Colors.RED) + "Be a "
                + (isMale() ? "HC Ironman" : "HC Ironwoman") + " to use this title.");
        sendString(39, (player.getDominionTower().getTotalScore() >= 50000 ? Colors.GREEN + "<i>" : Colors.RED)
                + "Get 50'000 Dominion Factor to use this title.");

        sendString(50, (player.isNovice() || player.isNoviceIronMan() ? Colors.GREEN : Colors.RED)
                + "Be on the Novice EXP mode to use this title.");
        sendString(52, (player.isExpert() || player.isExpertIronMan() ? Colors.GREEN : Colors.RED)
                + "Be on the Expert EXP mode to use this title.");
        sendString(54, (player.isLegendary() || player.isIronMan() ? Colors.GREEN : Colors.RED)
                + "Be on the Legendary EXP mode to use this title.");
        sendString(56, (player.isNovice() || player.isNoviceIronMan() ? Colors.GREEN : Colors.RED)
                + "Be on the Novice EXP mode to use this title.");
        sendString(58,
                (player.isExpert() || player.isExpertIronMan() ? Colors.GREEN : Colors.RED)
                        + "Be on the Expert EXP mode to use this title.");
        sendString(60, (player.isLegendary() || player.isIronMan() ? Colors.GREEN : Colors.RED)
                + "Be on the Legendary EXP mode to use this title.");
        sendString(86,
                (player.hasXmasTitleUnlocked() ? Colors.GREEN : Colors.RED) + "Custom user title for Plat+ / Staff");
        sendString(191,
                (player.hasXmasTitleUnlocked() ? Colors.GREEN : Colors.RED) + "Titles from the 2013 Christmas Event!");
        /** Goodwill well titles **/
        sendString(98, (player.donatedToWell >= 100000000 ? Colors.GREEN : Colors.RED)
                + "Thrown at least 100m coins into the Well.");
        sendString(100, (player.donatedToWell >= 1000000000 ? Colors.GREEN : Colors.RED)
                + "Thrown at least 1b coins into the Well.");
        sendString(102, (player.donatedToWell >= 5000000000L ? Colors.GREEN : Colors.RED)
                + "Thrown at least 5b coins into the Well.");

        /** Prifddinas titles **/
        sendString(75, (player.getSerenStonesMined() >= 100 ? Colors.GREEN : Colors.RED)
                + "Mined a total of 100 Seren stones.");
        sendString(77,
                (player.getHefinLaps() >= 200 ? Colors.GREEN : Colors.RED) + "Ran a total of 200 Hefin Agility laps");

        /** Cloak of Seasons combination **/
        sendString(95, (player.hasCombinedCloaks() ? Colors.GREEN : Colors.RED) + "Combined 4 seasonal cloaks.");

        /** Chronicle Fragment offering **/
        sendString(93, (player.hasGuthixTitleUnlocked() ? Colors.GREEN : Colors.RED)
                + "Exchanged a total of 100 Chronicle Fragments.");

        /** Member Rank titles **/
       /* sendString(63, (player.isDonator() ? Colors.GREEN : Colors.RED) + "Be ranked as Bronze member (20$)");
        sendString(65, (player.isExtremeDonator() ? Colors.GREEN : Colors.RED) + "Be ranked as Silver member (50$)");
        sendString(67, (player.isLegendaryDonator() ? Colors.GREEN : Colors.RED) + "Be ranked as Gold member (100$)");
        sendString(69, (player.isSupremeDonator() ? Colors.GREEN : Colors.RED) + "Be ranked as Platinum member (250$)");
        sendString(71, (player.isUltimateDonator() ? Colors.GREEN : Colors.RED) + "Be ranked as Diamond member (500$)");
        sendString(73, (player.isMasterDonator() ? Colors.GREEN : Colors.RED) + "Be ranked as Master member (1000$)");
       */ /** Special titles **/
        sendString(89, (Utils.getMinutesPlayed(player) >= 15000 ? Colors.GREEN : Colors.RED)
                + "Played for at least 250 hours");
        sendString(91, (player.getVotes() >= 250 ? Colors.GREEN : Colors.RED) + "Voted for " + Settings.SERVER_NAME + " at least 250 times");
        sendString(80, (player.isMax() ? Colors.GREEN : Colors.RED) + "Have claimed the Max cape");
        sendString(82, (player.isComp() ? Colors.GREEN : Colors.RED) + "Have claimed the Completionist cape");
        sendString(84, (player.isCompT() ? Colors.GREEN : Colors.RED) + "Have claimed the Completionist cape (t)");

        /** Now we send the actual interface with all the data **/
        /**
         * Demon flash scrolls
         */
        for (int i = 0; i < 14; i++) {
            final String title = (String) DemonFlashMobs.TITLES[i][0];
            final boolean afterName = (boolean) DemonFlashMobs.TITLES[i][1];
            final int componentId = (int) DemonFlashMobs.TITLES[i][2];
            sendString(componentId, (afterName ? (player.getDisplayName() + title) : (title + "</col>" + player.getDisplayName())));
            sendString(componentId + 1, (player.DFMScroll[i] ? Colors.GREEN : Colors.RED) + "Read " + ItemDefinitions.getItemDefinitions(33936 + (i * 2)).getName());
        }

        /**
         * 182, 183
         * 184, 185,
         * 186, 187
         * 188, 189
         */

  /*      sendString(175, username() + ", " + Colors.GRAY + "Jack of Trades");
        sendString(176, (player.getInteractedNoncombatPets().size() >= 5 ? Colors.GREEN : Colors.RED) + "Interact with non-combat pets(" + player.getInteractedNoncombatPets().size() + "/5)");
        sendString(177, username() + ", " + Colors.ORANGE + "Jack of All Trades");
        sendString(178, (player.getInteractedNoncombatPets().size() >= 18 ? Colors.GREEN : Colors.RED) + "Interact with non-combat pets(" + player.getInteractedNoncombatPets().size() + "/18)");

        sendString(182, Colors.RED + "Skilling Champion " + Colors.WHITE + username());
        sendString(183, (username().equals(SkillingContractTracker.getSingleton().displayLastWinner()) ? Colors.GREEN : Colors.RED) + "Be the current skilling champion.");

		sendString(184, Colors.DPURPLE + "Skilling Addict " + Colors.WHITE + username());
		sendString(185, (player.getContracts().totalContracts >= 1000 ? Colors.GREEN : Colors.RED) + "Have completed 1000 skilling contracts.");
*/
	//	sendString(186, player.getDisplayName() + Colors.GOLD + " the Ataraxian");
	//	sendString(187, (player.totalBoothVotes >= 3 ? Colors.GREEN : Colors.RED) + "Have voted in 3 or more in-game polls.");

		//sendString(188, Colors.RED + "Evil " + Colors.WHITE + username());
	//	sendString(189, (player.evilTreeKc >= 100 ? Colors.GREEN : Colors.RED) + "Have killed 100 or more Evil Trees.");

    //    sendString(104, player.getDisplayName() + Colors.ORANGE + " the Bunny");
    //    sendString(105, (player.easterTitle1 ? Colors.GREEN : Colors.RED) + "Have claimed this easter title.");
        
        player.getInterfaceManager().sendInterface(1082);
    }

    /**
     * Sets the players title.
     *
     * @param titleId the Title to set.
     */
    private void setTitle(final int titleId) {
        player.getAppearence().setTitle(titleId);
        player.sendMessage("Your title has been successfully changed.");
    }


    public static final ImmutableSet<String>  PRE_EXISTING_TITLES = ImmutableSet.of(
            "ironman", "the ironman", "hc ironman", "the hc ironman", "the survivor",
            "novice", "expert", "legendary", "the novice",
            "the expert", "the legendary", "the millionaire", "the charitable",
            "the billionaire", "of the trahaearn", "of seasons", "of guthix",
            "bronze member", "silver member", "gold member", "platinum member",
            "diamond member", "no-lifer", "ATTRIBUTE_KEY_LISTusiast", "the maxed", "the completionist",
            "the perfectionist", "of the hefin", "the blazing", "castellan",
            "the corrupting", "deacon", "executioner", "the frostborn", "general",
            "the glorious", "the infernal", "the obscured", "the pestilent", "the rending",
            "the shattering", "the terrifying", "final boss", "the bunny", "the egg", "of easter",
            "eggcellent", "the hopper", "the egg catcher", "potm", "player of the month", "the warden"
    );

    public static final ImmutableSet<String> UNALLOWED_TITLES = ImmutableSet.of("anus", "arse", "arsehole",
            "assbandit", "assbanger", "assbite", "assclown", "asscock", "asscracker", "asses",
            "assface", "assfuck", "assfucker", "assgoblin", "asshat", "asshead", "asshopper",
            "assjacker", "asslick", "asslicker", "assmonkey", "assmunch", "assmuncher", "assnigger",
            "asspirate", "asshit", "asssucker", "asswipe", "axwound", "bampot", "bastard", "beaner",
            "bitch", "bitchass", "bitches", "bitchtits", "bitchy", "blowjob", "blow job", "blow-job",
            "bollocks", "bollox", "boner", "brotherfucker", "bullshit", "bumblefuck", "buttplug",
            "butt-plug", "butt plug", "butt-pirate", "buttpirate", "buttfucker", "buttfucka",
            "butt-fucka", "butt-fucker", "camel-toe", "cameltoe", "camel toe", "carpet muncher",
            "carpetmuncher", "carpetmuncha", "chesticle", "chinc", "chink", "choad", "chode",
            "clit", "clitface", "clitfuck", "clusterfuck", "cock", "cockass", "cockbite", "cockburger",
            "cockface", "cockfucker", "cockhead", "cockjockey", "cockknocker", "cockmaster",
            "cockmongler", "cockmongruel", "cockmonkey", "cocknose", "cocknugget", "cockshit",
            "cocksmith", "cocksmoke", "cocksmoker", "cocksniffer", "cocksniff", "cocksucker",
            "cocksuck", "cockwaffle", "coochie", "coochy", "coon", "cooter", "cum", "cumbubble",
            "cumdumpster", "cumbin", "cumjockey", "cumbucket", "cumtart", "cumslut", "cunnie", "cunt",
            "cuntass", "cuntface", "cunthole", "cuntlicker", "cuntrag", "cuntslug", "dago", "damn",
            "deggo", "dick", "dick-sneeze", "dicksneeze", "dickbag", "dickbeater", "dickbeaters",
            "dickbeata", "dickface", "dickfuck", "dickhead", "dickfucker", "dickjuice", "dickmilk",
            "dickmonger", "dicks", "dickslap", "dicksucker", "dicksucking", "dicktickler", "dickwad",
            "dickweasel", "dickwod", "dickweed", "dike", "dildo", "dipshit", "dooshbag", "douchebag",
            "douche", "douche-fag", "douchefag", "douchewaffle", "dumbass", "dumass", "dumbcunt",
            "dumbfuck", "dumbshit", "dumshit", "dyke", "doggler", "dogging", "dogger", "dooby", "fag",
            "fagbag", "fagfucker", "faggit", "faggot", "faggotcock", "fagtard", "fatass", "fellatio",
            "feltch", "flamer", "fuck", "fuckass", "fuckbag", "fuckboy", "fuckbrain", "fuckbutt",
            "fuckbutter", "fucked", "fucker", "fucking", "fuckedy", "fucknut", "fuckoff", "fuckstick",
            "fucktard", "fuckup", "fuckwad", "fuckwit", "fuckwitt", "fudgepacker", "Miatabuilds", "gayass",
            "gaybob", "gaydo", "gayfuck", "gayfuckist", "gaylord", "gaytard", "gaywad", "goddamn",
            "goddamnit", "gooch", "gook", "gringo", "guido", "goon", "gloob", "glotty", "handjob",
            "hardon", "hard on", "hard-on", "hell", "heeb", "hoe", "ho", "homo", "homodumbshit",
            "honkey", "humping", "jackass", "jagoff", "jerk", "jerkoff", "jerking", "jerkass",
            "jigalo", "jizz", "jigaboo", "junglebunny", "jungle-bunny", "jungle bunny", "kike",
            "kooch", "kootch", "kraut", "kunt", "kyke", "lame", "lameass", "lardass", "lesbian",
            "lesbo", "lezzie", "mcfagget", "mick", "minge", "motherfucker", "mothafucka",
            "motherfucka", "motherfuck", "motherfucking", "muff", "muffdiver", "munging", "negro",
            "nigaboo", "nigga", "nigger", "niggers", "niglet", "nut sack", "nutsack", "paki",
            "panooch", "pecker", "peckerhead", "penis", "penisbanger", "penisfucker", "penispuffer",
            "piss", "pissed", "pissed off", "pissflap", "pissflaps", "polesmoker", "pollock", "poon",
            "poonani", "poonany", "poontang", "pootang", "poon", "porch monkey", "porchmonkey", "prick",
            "punanny", "punta", "pussies", "pussy", "pussylicking", "puto", "queef", "queer",
            "queetbait", "queerhole", "renob", "rimjob", "ruski", "sandnigger", "schlong", "scrote",
            "shit", "shitass", "shitbag", "shitbagger", "shitbrains", "shitbreath", "shitcanned", "shitcunt",
            "shitdick", "shitface", "shitfaced", "shithead", "shithole", "shithouse", "shitspitter",
            "shitstain", "shitter", "shittiest", "shitting", "shitty", "shiz", "shitnet", "shiet", "shitnit",
            "shiznit", "slut", "skullfuck", "skeet", "skank", "skanky", "skunt", "smeg", "snatch", "spic",
            "spick", "splooge", "spook", "suckass", "sucker", "tard", "testicle", "tundercunt", "tit", "tickfuck",
            "tits", "tittyfuck", "twat", "twatlips", "twats", "twatwaffle", "unclefucker", "va-j-j", "vag",
            "vagina", "vajayjay", "vjayjay", "vinge", "wank", "wankjob", "wanker", "wetback", "whore",
            "whorebag", "whoreface", "ass", "ass-hat", "ass-jabber", "ass-pirate", "assbag");

    /**
     * Handles the Title shop buttons.
     *
     * @param componentId the interfaces buttonId.
     */
    public void handleShop(final int componentId) {
        player.getInterfaceManager().closeChatBoxInterface();
        for (int i = 0; i < 14; i++) {
            if (componentId == ((int) DemonFlashMobs.TITLES[i][2] + 1)) {
                if (!player.DFMScroll[i]) {
                    player.sendMessage("You must have read the " + ItemDefinitions.getItemDefinitions(33936 + (i * 2)).getName() + " to use this title.");
                    return;
                }
                setTitle((int) DemonFlashMobs.TITLES[i][3]);
                return;
            }
        }
        switch (componentId) {
            case 31: /** Ironman after **/
                if (!isIronMan()) {
                    player.getDialogueManager().startDialogue("SimpleMessage",
                            "This title is only available for " + (isMale() ? "Ironman" : "Ironwoman") + " accounts.");
                    return;
                }
                setTitle(1000);
                break;
            case 33: /** Ironman before **/
                if (!isIronMan()) {
                    player.getDialogueManager().startDialogue("SimpleMessage",
                            "This title is only available for " + (isMale() ? "Ironman" : "Ironwoman") + " accounts.");
                    return;
                }
                setTitle(1500);
                break;
            case 35: /** HC Ironman after **/
                if (!player.isHCIronMan()) {
                    player.getDialogueManager().startDialogue("SimpleMessage",
                            "This title is only available for Hardcore " + (isMale() ? "Ironman" : "Ironwoman") + " accounts.");
                    return;
                }
                setTitle(1001);
                break;
            case 37: /** HC Ironman before **/
                if (!player.isHCIronMan()) {
                    player.getDialogueManager().startDialogue("SimpleMessage",
                            "This title is only available for Hardcore " + (isMale() ? "Ironman" : "Ironwoman") + " accounts.");
                    return;
                }
                setTitle(1501);
                break;
            case 39: /** the Survivor **/
                if (player.getDominionTower().getTotalScore() < 50000) {
                    player.getDialogueManager().startDialogue("SimpleMessage",
                            "You have to have a Dominion Factor of at least 50'000 to use this title.");
                    return;

                }
                setTitle(1002);
                break;
            case 50: /** Intermediate before **/
                if ((!player.isNovice() && !player.isATypeOfIronman()) || (!player.isNoviceIronMan() && player.isATypeOfIronman())) {
                    player.getDialogueManager().startDialogue("SimpleMessage",
                            "You have to be on the Novice EXP game mode to use this title.");
                    return;
                }
                setTitle(1003);
                break;
            case 52: /** Veteran before **/
                if ((!player.isExpert() && !player.isATypeOfIronman()) || (!player.isExpertIronMan() && player.isATypeOfIronman())) {
                    player.getDialogueManager().startDialogue("SimpleMessage",
                            "You have to be on the Expert EXP game mode to use this title.");
                    return;
                }
                setTitle(1004);
                break;
            case 54: /** Legendary before **/
                if ((!player.isLegendary() && !player.isATypeOfIronman()) || (!player.isIronMan() && player.isATypeOfIronman())) {
                    player.getDialogueManager().startDialogue("SimpleMessage",
                            "You have to be on the Legendary EXP game mode to use this title.");
                    return;

                }
                setTitle(1033);
                break;
            case 56: /** Intermediate after **/
                if ((!player.isNovice() && !player.isATypeOfIronman()) || (!player.isNoviceIronMan() && player.isATypeOfIronman())) {
                    player.getDialogueManager().startDialogue("SimpleMessage",
                            "You have to be on the Novice EXP game mode to use this title.");
                    return;
                }
                setTitle(1504);
                break;
            case 58: /** Veteran after **/
                if ((!player.isExpert() && !player.isATypeOfIronman()) || (!player.isExpertIronMan() && player.isATypeOfIronman())) {
                    player.getDialogueManager().startDialogue("SimpleMessage",
                            "You have to be on the Expert EXP game mode to use this title.");
                    return;
                }
                setTitle(1505);
                break;
            case 60: /** Legendary after **/
                if ((!player.isLegendary() && !player.isATypeOfIronman()) || (!player.isIronMan() && player.isATypeOfIronman())) {
                    player.getDialogueManager().startDialogue("SimpleMessage",
                            "You have to be on the Legendary EXP game mode to use this title.");
                    return;
                }
                setTitle(1518);
                break;
            case 98: /** Millionaire after **/
                if (player.donatedToWell < 100000000) {
                    player.getDialogueManager().startDialogue("SimpleMessage",
                            "You have to donate at least 100m coins to the Globe of Goodwill.");
                    return;
                }
                setTitle(1508);
                break;
            case 100: /** Charitable after **/
                if (player.donatedToWell < 1000000000) {
                    player.getDialogueManager().startDialogue("SimpleMessage",
                            "You have to donate at least 1b coins to the Globe of Goodwill.");
                    return;
                }
                setTitle(1509);
                break;
            case 102: /** Billionaire after **/
                if (player.donatedToWell < 5000000000L) {
                    player.getDialogueManager().startDialogue("SimpleMessage",
                            "You have to donate at least 5b coins to the Globe of Goodwill.");
                    return;
                }
                setTitle(1510);
                break;
            case 75: /** Trahaern after **/
                if (player.getSerenStonesMined() < 100) {
                    player.getDialogueManager().startDialogue("SimpleMessage",
                            "You have to Mine a total of 100 Seren stones at Prifddinas.");
                    return;
                }
                setTitle(1511);
                break;
            case 95: /** Seasons after **/
                if (!player.hasCombinedCloaks()) {
                    player.getDialogueManager().startDialogue("SimpleMessage",
                            "You have to combine 4 season cloaks. These cloaks can be received "
                                    + "by opening the Crystal chest.");
                    return;
                }
                setTitle(1514);
                break;
            case 93: /** Guthix after **/
                if (!player.hasGuthixTitleUnlocked()) {
                    player.getDialogueManager().startDialogue("SimpleMessage",
                            "You have to offer at least a total of 100 Chronicle Fragments at May Stormbrewer "
                                    + "located in Draynor Village.");
                    return;
                }
                setTitle(1515);
                break;
            case 63: /** Bronze member before **/
                if (!player.isDonator()) {
                    player.getDialogueManager().startDialogue("SimpleMessage",
                            "You have to be ranked as a Bronze member in order to use this title. To rank up to "
                                    + "bronze you have to spend at least 20$ from our ::store page.");
                    return;
                }
                setTitle(1016);
                break;
            case 65: /** Silver member before **/
                if (!player.isExtremeDonator()) {
                    player.getDialogueManager().startDialogue("SimpleMessage",
                            "You have to be ranked as a Silver member in order to use this title. To rank up to "
                                    + "silver you have to spend at least 50$ from our ::store page.");
                    return;
                }
                setTitle(1017);
                break;
            case 67: /** Gold member before **/
                if (!player.isLegendaryDonator()) {
                    player.getDialogueManager().startDialogue("SimpleMessage",
                            "You have to be ranked as a Gold member in order to use this title. To rank up to "
                                    + "gold you have to spend at least 100$ from our ::store page.");
                    return;
                }
                setTitle(1018);
                break;
            case 69: /** Platinum member before **/
                if (!player.isSupremeDonator()) {
                    player.getDialogueManager().startDialogue("SimpleMessage",
                            "You have to be ranked as a Platinum member in order to use this title. To rank up to "
                                    + "platinum you have to spend at least 250$ from our ::store page.");
                    return;
                }
                setTitle(1019);
                break;
            case 71: /** Diamond member before **/
                if (!player.isUltimateDonator()) {
                    player.getDialogueManager().startDialogue("SimpleMessage",
                            "You have to be ranked as a Diamond member in order to use this title. To rank up to "
                                    + "diamond you have to spend at least 500$ from our ::store page.");
                    return;
                }
                setTitle(1020);
                break;
            case 73: /** Master member before **/
                if (!player.isMasterDonator()) {
                    player.getDialogueManager().startDialogue("SimpleMessage",
                            "You have to be ranked as a Master member in order to use this title. To rank up to "
                                    + "Master you have to spend at least 1000$ from our ::store page.");
                    return;
                }
                setTitle(1026);
                break;
            case 89: /** No-lifer before **/
                if (Utils.getMinutesPlayed(player) < 15000) {
                    player.getDialogueManager().startDialogue("SimpleMessage",
                            "You have to have an online time of at least 250 hours.");
                    return;
                }
                setTitle(1021);
                break;
            case 91: /** Enthusiast before **/
                if (player.getVotes() < 250) {
                    player.getDialogueManager().startDialogue("SimpleMessage",
                            "You have to have voted for " + Settings.SERVER_NAME + " at least 250 times in total.");
                    return;
                }
                setTitle(1022);
                break;
            case 80: /** The Maxed before **/
                if (!player.isMax()) {
                    player.getDialogueManager().startDialogue("SimpleMessage",
                            "You have to claim the Max cape at least once from the Cape stand at home.");
                    return;
                }
                setTitle(1023);
                break;
            case 82: /** The Maxed before **/
                if (!player.isComp()) {
                    player.getDialogueManager().startDialogue("SimpleMessage",
                            "You have to claim the Completionist cape at least once from the Cape " + "stand at home.");
                    return;
                }
                setTitle(1024);
                break;
            case 84: /** The Perfectionist before **/
                if (!player.isCompT()) {
                    player.getDialogueManager().startDialogue("SimpleMessage",
                            "You have to claim the trimmed Completionist cape at least once from the "
                                    + "Cape stand at home.");
                    return;
                }
                setTitle(1025);
                break;
            case 77: /** the Hefin after **/
                if (player.getHefinLaps() < 200) {
                    player.getDialogueManager().startDialogue("SimpleMessage",
                            "You will have to complete a total of 200 Prifddinas Hefin district " + "Agility course laps.");
                    return;
                }
                setTitle(1516);
                break;
            case 86: // custom title
                if (player.getMoneySpent() < 250 && (!player.isSupport() && player.getRights() == 0)) {
                    player.getDialogueManager().startDialogue("SimpleMessage",
                            "Only staff members and Platinum donators can set their title!");
                    return;
                }

                break;

            case 176:
                if (player.getInteractedNoncombatPets().size() < 5) {
                    player.sendMessage("You must have interacted with at least 5 non-combat pets to use this title.");
                    return;
                }
                setTitle(3069);
                break;
            case 178:
                if (player.getInteractedNoncombatPets().size() < 18) {
                    player.sendMessage("You must have interacted with at least 18 non-combat pets to use this title.");
                    return;
                }
                setTitle(3070);
                break;
            case 183:
                if(!player.getDisplayName().equals(SkillingContractTracker.getSingleton().getLastWinner())) {
                    player.sendMessage("Only the current Skilling Champion can use this title.");
                    return;
                }
                setTitle(300);
                break;
            case 185:
                if(player.getContracts().totalContracts < 1000) {
                    player.sendMessage("You must have completed 1000 skilling contracts to use this title.");
                    return;
                }
                setTitle(301);
                break;
            case 187:
                if(player.totalBoothVotes < 3) {
                    player.sendMessage("You must have voted in 3 in-game polls to use this title.");
                    return;
                }
                setTitle(302);
                break;
            case 189:
                if(player.evilTreeKc < 100) {
                    player.sendMessage("You must have killed 100 Evil Trees in order to use this title.");
                    return;
                }
                setTitle(303);
                break;
			
		/*	case 178:
			if(!player.easterTitle2) {
				player.sendMessage("You haven't unlocked this title");
				return;
			}
			setTitle(3001);
			break;
			

			

			*/


            case 191: /** Christmas **/
                player.closeInterfaces();
                if (!player.hasXmasTitleUnlocked()) {
                    player.getDialogueManager().startDialogue("SimpleMessage",
                            "You must have activated at least one title from the Christmas event.");
                    return;
                }
                player.getDialogueManager().startDialogue(new Dialogue() {

                    @Override
                    public void start() {
                        sendOptionsDialogue("Christmas titles", "of Christmas", "of Winter", "the Grinch", "Frostweb");
                        stage = 0;
                    }

                    @Override
                    public void run(final int interfaceId, final int componentId) {
                        switch (stage) {
                            case 0:
                                switch (componentId) {
                                    case OPTION_1:
                                        if (player.xmasTitle1) {
                                            setTitle(2001);
                                        } else {
                                            player.sendMessage("You don't have that title!");
                                        }
                                        finish();
                                        break;
                                    case OPTION_2:
                                        if (player.xmasTitle2) {
                                            setTitle(2002);
                                        } else {
                                            player.sendMessage("You don't have that title!");
                                        }
                                        finish();
                                        break;
                                    case OPTION_3:
                                        if (player.xmasTitle3) {
                                            setTitle(2003);
                                        } else {
                                            player.sendMessage("You don't have that title!");
                                        }
                                        finish();
                                        break;
                                    case OPTION_4:
                                        if (player.xmasTitle4) {
                                            setTitle(2004);
                                        } else {
                                            player.sendMessage("You don't have that title!");
                                        }
                                        finish();
                                        break;

                                    case OPTION_5:
                                        finish();
                                        break;
                                }
                                break;
                        }

                    }

                    @Override
                    public void finish() {
                        player.getInterfaceManager().closeChatBoxInterface();

                    }

                });
                break;
        }
    }
}