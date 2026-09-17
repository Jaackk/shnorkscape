package com.rs.game.player.content;

import com.rs.Settings;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.game.World;
import com.rs.game.activites.dnd.eviltree.EvilTreeHandler;
import com.rs.game.activites.gim.GIM;
import com.rs.game.activites.gim.GIMGroup;
import com.rs.game.activities.ActivitiesScheduler;
import com.rs.game.activities.instances.Instance;
import com.rs.game.player.Player;
import com.rs.game.player.content.contracts.ContractHandler;
import com.rs.game.player.content.jujupotions.jadinkos.JadinkoManager;
import com.rs.game.player.content.newlottery.Lottery;
import com.rs.game.player.content.polls.PollManager;
import com.rs.game.player.content.skillingcontracts.SkillingContractManager;
import com.rs.game.player.content.skillingcontracts.SkillingContractTracker;
import com.rs.utils.Colors;
import com.rs.utils.TimeUtils;
import com.rs.utils.Utils;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * @author Noel
 * @date 15.01.2014
 */
public class TaskTab {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, uuuu");

    /**
     * Sends the actual Noticeboard tab.
     *
     * @param player The player to send to.
     */
    public static void sendTab(final Player player) {
        int rights = player.getRights();
        String pName = player.getUsername();
        String title = Colors.CYAN + "Player";
        String gameMode = null;
        String status = null;
        String npcName = null;
        String slayerTask = null;
        String info = getActiveInstances(player);
        GIMGroup gimGroup = null;
        if (player.isGroupIronman()) {
            gimGroup = GIM.getGroupData().get(player.gimKey.getGroupKey());
        }
        LocalDateTime dateFrom = LocalDateTime.now();
        String wellXpTimeLeft = "00:00:00";
        String nextLMSGameTimeLeft = "00:00:00";
        if (World.isWeekend() || World.isWellActive()) {
            LocalDateTime dateTo = null;
            if (World.isWeekend()) {
                dateTo = dateFrom.with(TemporalAdjusters.next(DayOfWeek.MONDAY)).withHour(0).withMinute(0).withSecond(0);
            } else if (World.isWellActive()) {
                dateTo = Instant.ofEpochMilli(WellOfGoodWill.endTime).atZone(ZoneId.systemDefault()).toLocalDateTime();
            }

            wellXpTimeLeft = TimeUtils.getHHMMSSTime(dateFrom, dateTo);
        }

        String activityTimingsMessage = ActivitiesScheduler.getInstance().createTaskTabString();

        // I've added check for if is not null, always check if it's not null
        // else RIP >.>
        if (player.getContract() != null)
            npcName = ContractHandler.getFormattedContractName(player);
        if (player.getTask() != null)
            slayerTask = player.getTask().getName(player).toLowerCase();
        if (player.isSupport())
            title = "<col=83A6F2>Support</col>";
        if (rights == 1)
            title = "<col=B5B5B5>Moderator</col>";
        if (rights == 2)
            title = "<col=1589FF>Administrator</col>";
        if (pName.equalsIgnoreCase("xhybrid"))
            title = "<col=ff6610>Owner</col>";
        if (pName.equalsIgnoreCase("Node"))
            title = "<col=00ffff>Co-Owner</col>";
        if (player.isDev())
            title = "<col=D400FF>Developer</col>";
        player.getPackets().sendIComponentText(635, 8, time("hh:mm"));
        /**
         * Start sending the tab
         */
        player.getPackets().sendIComponentText(930, 10, Colors.WHITE + Settings.SERVER_NAME + " Noticeboard</col>");

        player.getPackets().sendIComponentText(930, 16, Colors.RED + "-- Server --<br>"
                + Colors.WHITE + "- Server date: " + Colors.GREEN + dateFrom.format(formatter) + "<br>" + Colors.WHITE
                + Colors.WHITE + "- Server time: " + Colors.GREEN + time("hh:mm:ss a") + "<br>" /* + Colors.WHITE
                + "- Players online: " + Colors.GREEN + World.getPlayersOnline() + "<br>" + Colors.WHITE
                + "- Bonus XP: " + Colors.GREEN
                + (Settings.TRIPLE_EXP_ENABLED ? "Active (3x event) " : World.isWeekend() ? "Active (weekend)" : (World.isWellActive() ? "Active (well)" : Colors.RED + "Not Active"))
                + (World.isWeekend() || World.isWellActive() ? "<br>" + Colors.WHITE + "- Well XP time left: " + Colors.GREEN + wellXpTimeLeft : "")
                + "<br>" + Colors.WHITE + "- Next LMS game: " + nextLMSGameTimeLeft + "<br>"
                + Colors.WHITE + "- Next lottery in: " + Lottery.getSingleton().getRemainingTimeString() + "<br>"
                + Colors.WHITE + "- Skilling champion: " + SkillingContractTracker.getSingleton().displayLastWinner() + "<br>"
                + Colors.WHITE + "  - Next in: " + SkillingContractTracker.getSingleton().displayNextIn() + "<br>"
                + Colors.WHITE + "  - Top score: " + SkillingContractTracker.getSingleton().displayTopScore() + "<br>"
                + Colors.WHITE + "  - Your score: " + SkillingContractTracker.getSingleton().getYourScore(player) + "<br>"
                + Colors.WHITE + "- Vote party total: " + Colors.GREEN + VoteManager.VOTES + "<br>"
                + (World.getLastVoter() != null
                ? Colors.WHITE + "  - Last voter: " + Colors.GREEN + World.getLastVoter() + "<br>"
                : "")
                + Colors.WHITE + "- God jadinkos: " + JadinkoManager.getStatus() + "<br>"
                + (activityTimingsMessage.equals("") ? "" : "<br>" + Colors.RED + "-- Server Events --<br>" + activityTimingsMessage)
                + "<br>"
                + Colors.RED + "-- Player --<br>" + Colors.WHITE + "- Name: " + Colors.GREEN
                + Utils.formatPlayerNameForDisplay(player.getDisplayName()) + "<br>" + Colors.WHITE + "- Rank: " + title
                + (getGameModeString(player) != null ? "<br>" + Colors.WHITE + "- Mode: " + getGameModeString(player) : "")
                + (player.isDonator() ? "<br>" + Colors.WHITE + "- Status: " + getDonatorString(player) : "") + "<br>" + Colors.WHITE
                + "- Time played: " + Colors.GREEN + Utils.getTimePlayed(player.getTimePlayed()) + "<br>"
                + Colors.WHITE + "- This session: " + Colors.GREEN
                + Utils.getTimePlayed(player.isAFK() ? Utils.currentTimeMillis() : player.getRecordedPlayTime()) + "<br>"
                + (player.hasBonusEXP()
                ? Colors.WHITE + "- XP boost ends: " + player.getTimeLeftString() + "<br>"
                : "")
                + Colors.WHITE + "- Prismatic XP: " + Colors.GREEN
                + Utils.formatNumber((int) player.getSkills().getBonusPrismaticXp()) + "<br>"
                + Colors.WHITE + "- Div Location: " + Colors.GREEN
                + format((24 * 60 * 60 * 1000) - (Utils.currentTimeMillis() - player.lastCreationTime)) + "<br><br>"

                + (gimGroup != null ?
                Colors.RED + "-- Group Ironman --<br>" +
                        Colors.WHITE + "- Group name: " + Colors.GREEN
                        + gimGroup.getGroupName() + "<br>" +
                        Colors.WHITE + "- Group rank: " + Colors.GREEN
                        + gimGroup.getRank() + "<br>" +
                        Colors.WHITE + "- Group score: " + Colors.GREEN
                        + Utils.formatNumber(gimGroup.getScore()) + "<br>" +
                        Colors.WHITE + "- Season end: " + Colors.GREEN
                        + (GIM.getSeasonData() == null ? "Fetching..." : GIM.getSeasonData().getSeasonEnd().format(formatter)) + "<br><br>" : "")
                + Colors.RED + "-- Statistics --<br>"
                + Colors.WHITE + "- Donated: " + Colors.GREEN + "$" + Utils.getFormattedNumber(player.getMoneySpent()) + "<br>"
                + Colors.WHITE + "- coins: " + Colors.GREEN + Utils.getFormattedNumber(player.getAtaraxiaCoins()) + "<br>"
                + Colors.WHITE + "- Vote points: "
                + Colors.GREEN + Utils.getFormattedNumber(player.getVotePoints()) + "<br>" + Colors.WHITE
                + "- Loyalty points: " + Colors.GREEN + Utils.getFormattedNumber(player.getLoyaltyPoints()) + "<br>"
                + Colors.WHITE + "- Trivia points: " + Colors.GREEN + Utils.getFormattedNumber(player.getTriviaPoints())
                + "<br>" + Colors.WHITE + "- PC points: " + Colors.GREEN
                + Utils.getFormattedNumber(player.getPestPoints()) + "<br>" + Colors.WHITE + "- SW zeals: "
                + Colors.GREEN + Utils.getFormattedNumber(player.getZeals()) + "<br>" + Colors.WHITE + "- Dung tokens: "
                + Colors.GREEN
                + Utils.getFormattedNumber(player.getDungeoneeringManager() == null ? 0 : player.getDungeoneeringManager().getTokens())
                + "<br>" + Colors.WHITE + "- DT kills: " + Colors.GREEN
                + Utils.getFormattedNumber(player.getDominionTower().getKilledBossesCount()) + ""
                + (player.getPorts().hasFirstShip ? "<br><br>" + Colors.RED + "-- Ports Information --" + "<br>" : "")
                + (player.getPorts().hasFirstShip
                ? Colors.WHITE + "- Alpha - " + (!player.getPorts().hasFirstShip ? Colors.RED + "Ship locked"
                : (!player.getPorts().hasFirstShipReturned()
                ? Colors.YELLOW + "Minutes: " + player.getPorts().getFirstVoyageTimeLeft()
                + "</col>"
                : (!player.getPorts().firstShipReward ? Colors.GREEN + "Can claim</col>"
                : Colors.ORANGE + "Can deploy")))
                : "")
                + (player.getPorts().hasSecondShip ? "<br>" + Colors.WHITE + "- Beta - "
                + (!player.getPorts().hasSecondShip ? Colors.RED + "Ship locked"
                : (!player.getPorts().hasSecondShipReturned()
                ? Colors.YELLOW + "Minutes: " + player.getPorts().getSecondVoyageTimeLeft()
                + "</col>"
                : (!player.getPorts().secondShipReward ? Colors.GREEN + "Can claim</col>"
                : Colors.ORANGE + "Can deploy")))
                : "")
                + (player.getPorts().hasThirdShip ? "<br>" + Colors.WHITE + "- Gamma - "
                + (!player.getPorts().hasThirdShip ? Colors.RED + "Ship locked"
                : (!player.getPorts().hasThirdShipReturned()
                ? Colors.YELLOW + "Minutes: " + player.getPorts().getThirdVoyageTimeLeft()
                + "</col>"
                : (!player.getPorts().thirdShipReward ? Colors.GREEN + "Can claim</col>"
                : Colors.ORANGE + "Can deploy")))
                : "")
                + (player.getPorts().hasFourthShip ? "<br>" + Colors.WHITE + "- Delta - "
                + (!player.getPorts().hasFourthShip ? Colors.RED + "Ship locked"
                : (!player.getPorts().hasFourthShipReturned()
                ? Colors.YELLOW + "Minutes: " + player.getPorts().getFourthVoyageTimeLeft()
                + "</col>"
                : (!player.getPorts().fourthShipReward ? Colors.GREEN + "Can claim</col>"
                : Colors.ORANGE + "Can deploy")))
                : "")
                + (player.getPorts().hasFifthShip ? "<br>" + Colors.WHITE + "- Epsilon - "
                + (!player.getPorts().hasFifthShip ? Colors.RED + "Ship locked"
                : (!player.getPorts().hasFifthShipReturned()
                ? Colors.YELLOW + "Minutes: " + player.getPorts().getFifthVoyageTimeLeft()
                + "</col>"
                : (!player.getPorts().fifthShipReward ? Colors.GREEN + "Can claim</col>"
                : Colors.ORANGE + "Can deploy")))
                : "")

                + "<br><br>" + Colors.RED + "-- PvP Information --" + "<br>" + Colors.WHITE + "- Pk points: "
                + Colors.GREEN + Utils.getFormattedNumber(player.getPkPoints()) + "<br>" + Colors.WHITE
                + "- Killstreak points: " + Colors.GREEN + Utils.getFormattedNumber(player.getTotalKillStreakPoints())
                + "<br>" + Colors.WHITE + "- Kills: " + Colors.GREEN + Utils.getFormattedNumber(player.getKillCount())
                + "<br>" + Colors.WHITE + "- Deaths: " + Colors.GREEN + Utils.getFormattedNumber(player.getDeathCount())
                + "<br>" + Colors.WHITE + "- Killstreak: " + Colors.GREEN
                + Utils.getFormattedNumber(player.getKillStreak())

                + "<br><br>" + Colors.RED + "-- Slayer Information --"
                + (player.getTask() != null ? "<br>" + Colors.WHITE + "- Task: " + Colors.GREEN + slayerTask + ""
                + "<br>" + Colors.WHITE + "- Kills left: " + Colors.GREEN
                + Utils.getFormattedNumber(player.getTask().getTaskAmount()) : "")
                + "<br>" + Colors.WHITE + "- Slayer points: " + Colors.GREEN
                + Utils.getFormattedNumber(player.getSlayerPoints()) + "<br>" + Colors.WHITE + "- Completed tasks: "
                + Colors.GREEN + Utils.getFormattedNumber(player.getTaskStreak())

                + "<br><br>" + Colors.RED + "-- Reaper Information --"
                // XXX I've added check for if is not null, always check if it's
                // not null else RIP >.>
                + (player.getContract() != null
                ? !player.getContract().hasCompleted() ? "<br>" + Colors.WHITE + "- Contract: <br>"
                + Colors.GREEN + "-- '" + npcName + "' --<br>" + Colors.WHITE + "- Kills left: "
                + Colors.GREEN + Utils.getFormattedNumber(player.getContract().getKillAmount()) + "<br>"
                + Colors.WHITE + "- Reward amount: " + Colors.GREEN
                + Utils.getFormattedNumber(player.getContract().getRewardAmount()) : ""
                : "")
                + "<br>" + Colors.WHITE + "- Reaper points: " + Colors.GREEN
                + Utils.getFormattedNumber(player.getReaperPoints()) + "<br>" + Colors.WHITE + "- Total kills: "
                + Colors.GREEN + Utils.getFormattedNumber(player.getTotalKills()) + "<br>" + Colors.WHITE
                + "- Completed contracts: " + Colors.GREEN + Utils.getFormattedNumber(player.getTotalContract())
                + (info != null && info.length() > 0
                ? ("<br><br>" + Colors.RED + "-- Active Instances -- " + "<br>" + info) : "")

                + "<br><br>" + Colors.RED + "-- Vorago --" + "<br>" + Colors.WHITE + "- Rotation: " + Colors.GREEN
                + Settings.VORAGO_ROTATION_NAMES[Settings.VORAGO_ROTATION]
                + "<br><br>" + Colors.RED + "-- Araxxor --" + "<br>" + Colors.WHITE + "- Paths: " + Colors.GREEN
                + Settings.SPIDER_BOSS_ROTATION_NAMES[Settings.SPIDER_BOSS_ROTATION]
                + "<br><br>" + Colors.RED + "-- Skilling Contract --" +
                "<br>" +
                Colors.WHITE + "- Completed: " + Colors.GREEN + player.getContracts().totalContracts + "<br>" +
                Colors.WHITE + "- Current: " + Colors.GREEN + getContract(player) + "<br>" +
                Colors.WHITE + "- Rewards multiplier: x" + Colors.GREEN + SkillingContractManager.multiplier
                + "<br><br>" + Colors.RED + "-- Evil Tree --" +
                "<br>" + EvilTreeHandler.getFullStatus() */ /*+ "<br><br>" + Colors.RED + "-- Contributer Status --" + "<br>"+ Colors.WHITE + (!player.hasContributerStatus() ? ("- Amount Left: "+Colors.GREEN +"$"+ player.getContributionAmount() + "/$"+Player.CONTRIBUTER_STATUS_THRESSHOLD+" ("+new DecimalFormat("##.##").format(((double)player.getContributionAmount() * 100.00) / (double) Player.CONTRIBUTER_STATUS_THRESSHOLD)+"%)") : ("- Expiration date: " + Colors.GREEN+ player.getContributerDaysLeftMessage()))*/);
    }

    private static String getContract(Player player) {
        if (player.getContracts().current == null)
            return "None";
        return player.getContracts().getContractDescription();
    }

    public static final String getActiveInstances(Player player) {
        StringBuilder info = new StringBuilder();
        ArrayList<Instance> instances = new ArrayList<Instance>();
        for (Instance i : World.getInstances()) {
            if (i.getOwner().getDisplayName().equals(player.getDisplayName()) && i.getMinutesRemaining() >= 0)
                instances.add(i);
        }
        for (int i = 0; i < instances.size(); i++)
            info.append(Colors.WHITE
                    + (instances.get(i).getBoss() == 2880 ? "Dagannoth kings"
                    : instances.get(i).getBoss() == HeartOfGielinor.SEREN ? "Helwyr"
                    : instances.get(i).getBoss() == HeartOfGielinor.SLISKE ? "Gregorovic"
                    : instances.get(i).getBoss() == HeartOfGielinor.ZAMORAK ? "Twin furies"
                    : instances.get(i).getBoss() == HeartOfGielinor.ZAROS ? "Vindicta & Gorvek"
                    : NPCDefinitions.getNPCDefinitions(instances.get(i).getBoss()).getName())
                    + (instances.get(i).isHardMode() ? "(h)" : "") + ": " + instances.get(i).getMinutesRemaining()
                    + " min.<br>");
        return info.toString();
    }

    /**
     * Gets the current systems time.
     *
     * @param dateFormat The format to use.
     * @return The formatted time.
     */
    public static String time(String dateFormat) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(cal.getTime());
    }

    public static String format(long time) {
        final int sec = (int) (time / 1000), h = sec / 3600, m = sec / 60 % 60, s = sec % 60;
        return (h < 1 ? "" : (h < 10 ? "0" + h : h) + "h:") + ((m < 1) && h < 1 ? "" : (m < 10 ? "0" + m : m) + "m:")
                + ((s < 1) && m < 1 ? "" : (s < 10 ? "0" + s + "s" : s + "s"));
    }

    public static String getGameModeString(Player player) {
        String gameMode = " ";
        if (player.isLegendary())
            gameMode = Colors.CYAN + "Legendary (x" + Settings.EXPERT_XP + " XP)";
        if (player.isExpert())
            gameMode = Colors.CYAN + "Expert (x" + Settings.VET_XP + " XP)";
        if (player.isIntermediate())
            gameMode = Colors.CYAN + "Intermediate (x" + Settings.INTERMEDIATE_XP + " XP)";
        if (player.isNovice())
            gameMode = Colors.CYAN + "Novice (x" + Settings.INTERM_XP + " XP)";
        if (player.isIronMan())
            gameMode = Colors.CYAN + "Leg. Ironman (x" + Settings.IRONMAN_XP + " XP)";
        if (player.isNoviceIronMan())
            gameMode = Colors.CYAN + "Nov. Ironman (x" + Settings.INTERM_XP + " XP)";
        if (player.isIntermediateIronMan())
            gameMode = Colors.CYAN + "Inter. Ironman (x" + Settings.INTERMEDIATE_XP + " XP)";
        if (player.isExpertIronMan())
            gameMode = Colors.CYAN + "Exp. Ironman (x" + Settings.VET_XP + " XP)";
        if (player.isHCIronMan())
            gameMode = Colors.CYAN + "HC Ironman (x" + Settings.IRONMAN_XP + " XP)";
        if (player.isKingOfTheSkillGameMode()) {
            gameMode = Colors.CYAN + "King of the Skill (x" + Settings.KING_OF_THE_SKILL_XP + " XP)";
        }
        if (player.isGroupIronman())
            gameMode = Colors.CYAN + "Group Ironman (x" + Settings.EXPERT_XP + " XP)";
        return gameMode;
    }

    public static String getDonatorString(Player player) {
        String status = "/";
        if (player.isDonator())
            status = "<col=C96800>Bronze Member";
        if (player.isExtremeDonator())
            status = Colors.GRAY + "Silver Member";
        if (player.isLegendaryDonator())
            status = Colors.YELLOW + "Gold Member";
        if (player.isSupremeDonator())
            status = "<col=27C4A2>Platinum Member";
        if (player.isUltimateDonator())
            status = "<col=05FFC9>Diamond Member";
        if (player.isMasterDonator())
            status = "<col=67007C>Master Member";
        return status;
    }

}