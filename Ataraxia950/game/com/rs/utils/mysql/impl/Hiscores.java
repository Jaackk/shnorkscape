package com.rs.utils.mysql.impl;

import com.rs.game.player.Player;
import com.rs.utils.Logger;
import com.rs.utils.Utils;
import com.rs.utils.mysql.DatabaseCredential;
import com.rs.utils.mysql.Pool;
import com.rs.utils.mysql.SQLRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class Hiscores extends SQLRunnable {

    private final Player player;
    private final String table;

    public Hiscores(final Player player, final String table) {
        this.player = player;
        this.table = table;
    }
    
    @Override
    public void execute(final DatabaseCredential auth) {
        if(player != null)
            if(player.getRights() == 2 || player.isDeveloper() || player.isOwner())
                return;

        final String query = generateQuery();
        final String name = Utils.formatPlayerNameForDisplay(player.getUsername());
        
        try(final Connection con = Pool.getConnection(auth, "ataraxia");
            final PreparedStatement del = con.prepareStatement("DELETE FROM "+table+" WHERE username=?");
            final PreparedStatement pst = con.prepareStatement(query)) {

            del.setString(1, name);
            del.execute();
            
            pst.setString(1, name);
            pst.setInt(2, getRights());
            pst.setString(3, getGameMode());
            pst.setString(4, getDifficulty());
            pst.setInt(5, player.getSkills().getTotalLevel());
            pst.setLong(6, player.getSkills().getTotalXp());
            for (int i = 0; i < 27; i++)
                pst.setInt(7 + i, (int) player.getSkills().getXp()[i]);
            pst.setLong(34, System.currentTimeMillis());
            pst.execute();
        } catch(final Exception ex) {
            Logger.getGlobal().error("Could not update hiscores; player: "+name+" table: "+table, ex);
        }
    }

    private int getRights() {
        if (player.getRights() == 2 || player.getRights() == 1)
            return player.getRights();
        if (player.isUltimateDonator())
            return 7;
        if (player.isSupremeDonator())
            return 6;
        if (player.isLegendaryDonator())
            return 5;
        if (player.isExtremeDonator())
            return 4;
        if (player.isDonator())
            return 3;
        return 0;
    }
    
    private String getGameMode() {
        if(player.isKingOfTheSkillGameMode())
            return "KoTS";
        if(player.isATypeOfIronman())
            return "Ironman";
        return "Regular";
    }
    
    private String getDifficulty() {
        if(player.isATypeOfIronman()) {
            if(player.isHCIronMan())
                return "hardcore";
            if(player.isNoviceIronMan())
                return "novice";
            if(player.isIntermediateIronMan())
                return "intermediate";
            if(player.isExpertIronMan())
                return "expert";
            return "legend";
        }
        if(player.isKingOfTheSkillGameMode())
            return "novice";
        return player.isNovice() ? "novice" : player.isIntermediate() ? "intermediate" : player.isExpert() ? "expert" : "legend";
    }

    private String generateQuery() {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO " + table + " (");
        sb.append("username, ");
        sb.append("rights, ");
        sb.append("game_mode, ");
        sb.append("difficulty, ");
        sb.append("total_level, ");
        sb.append("overall_xp, ");
        sb.append("attack_xp, ");
        sb.append("defence_xp, ");
        sb.append("strength_xp, ");
        sb.append("constitution_xp, ");
        sb.append("ranged_xp, ");
        sb.append("prayer_xp, ");
        sb.append("magic_xp, ");
        sb.append("cooking_xp, ");
        sb.append("woodcutting_xp, ");
        sb.append("fletching_xp, ");
        sb.append("fishing_xp, ");
        sb.append("firemaking_xp, ");
        sb.append("crafting_xp, ");
        sb.append("smithing_xp, ");
        sb.append("mining_xp, ");
        sb.append("herblore_xp, ");
        sb.append("agility_xp, ");
        sb.append("thieving_xp, ");
        sb.append("slayer_xp, ");
        sb.append("farming_xp, ");
        sb.append("runecrafting_xp, ");
        sb.append("hunter_xp, ");
        sb.append("construction_xp, ");
        sb.append("summoning_xp, ");
        sb.append("dungeoneering_xp, ");
        sb.append("divination_xp, ");
        sb.append("invention_xp, ");
        sb.append("last_update)");
        sb.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        return sb.toString();
    }
}
