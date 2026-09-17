package com.rs.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class Icons {

    public static int
            MODERATOR = IconData.MODERATOR.getId(),
            ADMINISTRATOR = IconData.ADMINISTRATOR.getId(),
            RIGHTWARD_ARROW = IconData.RIGHTWARD_ARROW.getId(),
            QUICKCHAT = IconData.QUICKCHAT.getId(),
            KING_OF_THE_SKILL = IconData.KING_OF_THE_SKILL.getId(),
            GLOBAL_ANNOUNCEMENT = IconData.GLOBAL_ANNOUNCEMENT.getId(),
            WORLD_ACHIEVEMENT = IconData.WORLD_ACHIEVEMENT.getId(),
            GENERIC_ANNOUNCEMENT_GOLD = IconData.GENERIC_ANNOUNCEMENT_GOLD.getId(),
            LOCAL_MODERATOR = IconData.LOCAL_MODERATOR.getId(),
            VIP = IconData.VIP.getId(),
            VIP_PMOD = IconData.VIP_PMOD.getId(),
            RS3_IRONMAN = IconData.RS3_IRONMAN.getId(),
            RS3_IRONMAN_OLD = IconData.RS3_IRONMAN_OLD.getId(),
            RS3_HARDCORE_IRONMAN = IconData.RS3_HARDCORE_IRONMAN.getId(),
            LINK = IconData.LINK.getId(),
            BRONZE_DONATOR = IconData.BRONZE_DONATOR.getId(),
            SILVER_DONATOR = IconData.SILVER_DONATOR.getId(),
            NOVICE_IRONMAN = IconData.NOVICE_IRONMAN.getId(),
            HARDCORE_IRONMAN = IconData.HARDCORE_IRONMAN.getId(),
            GOLD_DONATOR = IconData.GOLD_DONATOR.getId(),
            PLATINUM_DONATOR = IconData.PLATINUM_DONATOR.getId(),
            DIAMOND_DONATOR = IconData.DIAMOND_DONATOR.getId(),
            SUPPORT = IconData.SUPPORT.getId(),
            MASTER_DONATOR = IconData.MASTER_DONATOR.getId(),
            PINK_CROWN = IconData.PINK_CROWN.getId(),
            DEVELOPER = IconData.DEVELOPER.getId(),
            ORANGE_CROWN = IconData.ORANGE_CROWN.getId(),
            DESIGNER = IconData.DESIGNER.getId(),
            INTERMEDIATE_IRONMAN = IconData.INTERMEDIATE_IRONMAN.getId(),
            EXPERT_IRONMAN = IconData.EXPERT_IRONMAN.getId(),
            LEGENDARY_IRONMAN = IconData.LEGENDARY_IRONMAN.getId(),
            PLAYER_OF_THE_MONTH = IconData.PLAYER_OF_THE_MONTH.getId(),
            GROUP_IRONMAN = IconData.GROUP_IRONMAN.getId();
           // CYAN_CROWN = IconData.CYAN_CROWN.getId();

    @AllArgsConstructor
    public enum IconData {
        MODERATOR(0),
        ADMINISTRATOR(1),
        RIGHTWARD_ARROW(2),
        QUICKCHAT(3),
        KING_OF_THE_SKILL(4),
        GLOBAL_ANNOUNCEMENT(5),
        WORLD_ACHIEVEMENT(6),
        GENERIC_ANNOUNCEMENT_GOLD(7),
        LOCAL_MODERATOR(8),
        VIP(9),
        VIP_PMOD(10),
        RS3_IRONMAN(11),
        RS3_IRONMAN_OLD(12),
        RS3_HARDCORE_IRONMAN(13),
        LINK(14),
        BRONZE_DONATOR(15),
        SILVER_DONATOR(16),
        NOVICE_IRONMAN(17),
        HARDCORE_IRONMAN(18),
        GOLD_DONATOR(19),
        PLATINUM_DONATOR(20),
        DIAMOND_DONATOR(21),
        SUPPORT(22),
        MASTER_DONATOR(24),
        PINK_CROWN(25),
        DEVELOPER(26),
        ORANGE_CROWN(27),
        DESIGNER(28),
        INTERMEDIATE_IRONMAN(29),
        EXPERT_IRONMAN(30),
        LEGENDARY_IRONMAN(31),
        PLAYER_OF_THE_MONTH(32),
        GROUP_IRONMAN(33),
       // CYAN_CROWN(34),
        ;

        @Getter
        private final int id;
    }
}
