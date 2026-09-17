package com.rs.game.player.content;

import com.rs.cache.loaders.ClientScriptMap;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Utils;
    
public class PrismaticStars {

    public static final int STAR_SMALL = 0;
    public static final int STAR_MEDIUM = 1;
    public static final int STAR_BIG = 2;
    public static final int STAR_HUGE = 3;

    public static final int[] SELECTABLE_XP_STARS = new int[] { 30550, 30523, 29923, 29896 };
    public static final int[] SELECTABLE_XP_STARS_TYPES = new int[] { STAR_SMALL, STAR_MEDIUM, STAR_BIG, STAR_HUGE };
    public static final int[][] SKILL_STARS = new int[][] { { 30524, 30497, 29897, 29870 },
        { 30526, 30499, 29899, 29872 }, { 30525, 30498, 29898, 29871 }, { 30532, 30505, 29905, 29878 },
        { 30527, 30500, 29900, 29873 }, { 30528, 30501, 29901, 29874 }, { 30529, 30502, 29902, 29875 },
        { 30543, 30516, 29916, 29889 }, { 30545, 30518, 29918, 29891 }, { 30537, 30510, 29910, 29883 },
        { 30542, 30515, 29915, 29888 }, { 30544, 30517, 29917, 29890 }, { 30536, 30509, 29909, 29882 },
        { 30541, 30514, 29914, 29887 }, { 30540, 30513, 29913, 29886 }, { 30534, 30507, 29907, 29880 },
        { 30533, 30506, 29906, 29879 }, { 30535, 30508, 29908, 29881 }, { 30538, 30511, 29911, 29884 },
        { 30546, 30519, 29919, 29892 }, { 30530, 30503, 29903, 29876 }, { 30539, 30512, 29912, 29885 },
        { 30531, 30504, 29904, 29877 }, { 30547, 30520, 29920, 29893 }, { 30549, 30522, 29922, 29895 },
        { 30548, 30521, 29921, 29894 }, { 39396, 39395, 39394, 39393 }};
    public static final int[][] SKILL_STARS_TYPES = new int[][] { { STAR_SMALL, STAR_MEDIUM, STAR_BIG, STAR_HUGE },
            { STAR_SMALL, STAR_MEDIUM, STAR_BIG, STAR_HUGE }, { STAR_SMALL, STAR_MEDIUM, STAR_BIG, STAR_HUGE },
            { STAR_SMALL, STAR_MEDIUM, STAR_BIG, STAR_HUGE }, { STAR_SMALL, STAR_MEDIUM, STAR_BIG, STAR_HUGE },
            { STAR_SMALL, STAR_MEDIUM, STAR_BIG, STAR_HUGE }, { STAR_SMALL, STAR_MEDIUM, STAR_BIG, STAR_HUGE },
            { STAR_SMALL, STAR_MEDIUM, STAR_BIG, STAR_HUGE }, { STAR_SMALL, STAR_MEDIUM, STAR_BIG, STAR_HUGE },
            { STAR_SMALL, STAR_MEDIUM, STAR_BIG, STAR_HUGE }, { STAR_SMALL, STAR_MEDIUM, STAR_BIG, STAR_HUGE },
            { STAR_SMALL, STAR_MEDIUM, STAR_BIG, STAR_HUGE }, { STAR_SMALL, STAR_MEDIUM, STAR_BIG, STAR_HUGE },
            { STAR_SMALL, STAR_MEDIUM, STAR_BIG, STAR_HUGE }, { STAR_SMALL, STAR_MEDIUM, STAR_BIG, STAR_HUGE },
            { STAR_SMALL, STAR_MEDIUM, STAR_BIG, STAR_HUGE }, { STAR_SMALL, STAR_MEDIUM, STAR_BIG, STAR_HUGE },
            { STAR_SMALL, STAR_MEDIUM, STAR_BIG, STAR_HUGE }, { STAR_SMALL, STAR_MEDIUM, STAR_BIG, STAR_HUGE },
            { STAR_SMALL, STAR_MEDIUM, STAR_BIG, STAR_HUGE }, { STAR_SMALL, STAR_MEDIUM, STAR_BIG, STAR_HUGE },
            { STAR_SMALL, STAR_MEDIUM, STAR_BIG, STAR_HUGE }, { STAR_SMALL, STAR_MEDIUM, STAR_BIG, STAR_HUGE },
            { STAR_SMALL, STAR_MEDIUM, STAR_BIG, STAR_HUGE }, { STAR_SMALL, STAR_MEDIUM, STAR_BIG, STAR_HUGE },
            { STAR_SMALL, STAR_MEDIUM, STAR_BIG, STAR_HUGE }, { STAR_SMALL, STAR_MEDIUM, STAR_BIG, STAR_HUGE }};

    // do not look at wiki, wiki contains alot of incorrect xp data (I checked
    // myself)
    private static final double[] BASE_STARS_XP = new double[] { 74.7, 81.9, 91.8, 100.8, 111.6, 124.2, 135.9, 151.2, 166.5, 183.6, 203.4,
            224.1, 246.6, 273.6, 301.5, 313.9, 328, 341.9, 356.8, 371.8, 388.6, 404.1, 421.9, 440.3, 459.1, 478.5, 499.6, 520.7, 543.1, 566.7, 591.1, 616.3,
            643, 671, 699.6, 729.7, 761.7, 794.2, 829.3, 864.4, 902.2, 940.9, 981.3, 1023.7, 1067.8, 1114.5, 1164.2, 1214.7, 1266, 1321.2, 1377.7, 1439.6,
            1499.2, 1565.4, 1634.3, 1706.3, 1781.9, 1781.9, 1854.9, 1939.2, 2021.1, 2108.5, 2202.6, 2293.5, 2404.9, 2500.6, 2605.7, 2722.7, 2854.8,
            2964, 3111.2, 3231.4, 3371, 3535.7, 3698.3, 3856.3, 4007.2, 4193.8, 4375.9, 4550.5, 4776, 4999.1, 5217, 5426.1, 5714.4, 5902.1, 6179.3,
            6450.4, 6710.9, 7107, 7345.8, 7937, 8313.8, 8683, 9038.9, 9677.3, 10016.8, 10322.2 };

    public static void processStarClick(Player player, int slot, int id) {
        if (isSelectable(id)) {
            openSelectableDialog(player, slot, id);
        } else if (isSkillStar(id)) {
            openSkillDialog(player, slot, id);
        }
    }

    private static void openSelectableDialog(Player player, final int slot, final int id) {
        player.getDialogueManager().startDialogue(new Dialogue() {
            private int skillChosen = -1;
            
            @Override
            public void start() {
                player.getInterfaceManager().sendInterface(1263);
                player.getPackets().sendGlobalString(2389, "What sort of Bonus XP would you like?");
                player.getPackets().sendGlobalConfig(1797, 0); // selectable
                                                                // lamps don't
                                                                // show xp
                player.getPackets().sendGlobalConfig(1798, 1); // minimum level
                                                                // of 1 to show
                player.getPackets().sendGlobalConfig(1799, id);
                for(int i=22;i<=72;i++)
                player.getPackets().sendIComponentSettings(1263, i, -1, 0, 2);
                player.getPackets().sendIComponentSettings(1263, 13, -1, 0, 2);
                player.getPackets().sendIComponentSettings(1263, 19, -1, 0, 2);
                player.getPackets().sendIComponentSettings(1263, 79, -1, 0, 2);
                player.getPackets().sendExecuteScript(10370, 1, InterfaceManager.getComponentUId(1263, 13), "");
                player.getPackets().sendExecuteScript(785, 1, 1, id, 0, "What sort of XP would you like?");
            }

            @Override
            public void run(int interfaceId, int componentId, int slotId) {
                if (componentId >= 22 && componentId <= 72) {
                    skillChosen = selectSkill(componentId);
                    player.getPackets().sendExecuteScript(785, 1, 1, id, ClientScriptMap.getMap(1482).getIntValue(skillChosen), "What sort of XP would you like?");
                    player.getPackets().sendExecuteScript(10370, 1, InterfaceManager.getComponentUId(1263, 13), "Confirm "+Skills.SKILL_NAME[skillChosen]+" XP");
                } else if (componentId == 13) {
                    if (skillChosen == -1) {
                        openSelectableDialog(player, slot, id);
                        return;
                    }
                    end();
                    if (!player.getInventory().containsItem(id, 1)) 
                        return;
                    player.getInventory().deleteItem(slot, new Item(id, 1));
                    double exp = player.getSkills().addBonusXpStar(skillChosen,
                            getExp(player.getSkills().getLevelForXp(skillChosen), selectableStarType(id)));
                    player.closeInterfaces();
                    player.getDialogueManager().startDialogue("SimpleMessage",
                            "<col=0000ff>Your wish has been granted!</col>",
                            "You have been awarded " + Utils.getFormattedNumber(exp, ',') + " Bonus XP in "
                                    + Skills.SKILL_NAME[skillChosen] + "!");
                } else if(componentId == 11 || componentId == 6) 
                    end();
            }

            @Override
            public void finish() {
                if (player.getInterfaceManager().containsScreenInter())
                    player.getInterfaceManager().closeScreenInterface();
            }

            private int selectSkill(int componentId) {
            switch(componentId) {    
            case 22:
                return 0;
            case 23:
            return 3;
            case 24:
            return 14;
            case 25:
            return 2;
            case 26:
            return 16;
            case 27:
            return 13;
            case 28:
            return 1;
            case 29:
            return 15;
            case 30:
            return 10;
            case 31:
            return 4;
            case 32:
            return 17;
            case 33:
            return 7;
            case 34:
            return 5;
            case 35:
            return 12;
            case 36:
            return 11;
            case 37:
            return 6;
            case 38:
            return 9;
            case 39:
            return 8;
            case 40:
            return 20;
            case 52:
            return 18;
            case 53:
            return 19;
            case 55:
            return 22;
            case 62:
            return 21;
            case 64:
            return 23;
            case 67:
            return 24;
            case 71:
            return 25;
            case 72:
            return 26;
            }
            return -1;
            }
            @Override
            public void run(int interfaceId, int componentId) {

            }

        });

    }

    private static void openSkillDialog(Player player, final int slot, final int id) {
        final int type = skillStarType(id);
        final int skillId = skillStarSkillId(id);

        player.getDialogueManager().startDialogue(new Dialogue() {
            @Override
            public void start() {
                sendOptionsDialogue("Gain bonus experience in " + Skills.SKILL_NAME[skillId] + ".", "Yes",
                        "No");
            }

            @Override
            public void run(int interfaceId, int componentId) {
                if (componentId != Dialogue.OPTION_1 || !player.getInventory().containsItem(id, 1)) {
                    end();
                    return;
                }

                player.getInventory().deleteItem(slot, new Item(id, 1));
                double exp = player.getSkills().addBonusXpStar(skillId,
                        getExp(player.getSkills().getLevelForXp(skillId), type));
                player.getDialogueManager().startDialogue("SimpleMessage",
                        "<col=0000ff>Your wish has been granted!</col>",
                        "You have been awarded " + Utils.getFormattedNumber(exp, ',') + " Bonus XP in " + Skills.SKILL_NAME[skillId] + "!");
            }

            @Override
            public void finish() {
            }

        });
    }

    public static double getExp(int skillLevel, int lampType) {
        double xp;
        if (skillLevel <= BASE_STARS_XP.length)
            xp = BASE_STARS_XP[skillLevel - 1];
        else
            xp = BASE_STARS_XP[BASE_STARS_XP.length - 1];

        for (int i = 0; i < lampType; i++)
            xp *= 2D;
        return xp;
    }

    public static int selectableStarType(int id) {
        for (int i = 0; i < SELECTABLE_XP_STARS.length; i++) {
            if (SELECTABLE_XP_STARS[i] == id || ItemDefinitions.getItemDefinitions(SELECTABLE_XP_STARS[i]).getName().equalsIgnoreCase(ItemDefinitions.getItemDefinitions(id).getName()))
                return SELECTABLE_XP_STARS_TYPES[i];
        }
        return -1;
    }

    public static boolean isSelectable(int id) {
        for (int i = 0; i < SELECTABLE_XP_STARS.length; i++) {
            if (SELECTABLE_XP_STARS[i] == id || ItemDefinitions.getItemDefinitions(SELECTABLE_XP_STARS[i]).getName().equalsIgnoreCase(ItemDefinitions.getItemDefinitions(id).getName()))
                return true;
        }
        return false;
    }

    private static int skillStarType(int id) {
        for (int skillId = 0; skillId < SKILL_STARS.length; skillId++) {
            for (int i = 0; i < SKILL_STARS[skillId].length; i++) {
                if (SKILL_STARS[skillId][i] == id)
                    return SKILL_STARS_TYPES[skillId][i];
            }
        }
        return -1;
    }

    private static int skillStarSkillId(int id) {
        for (int skillId = 0; skillId < SKILL_STARS.length; skillId++) {
            for (int i = 0; i < SKILL_STARS[skillId].length; i++) {
                if (SKILL_STARS[skillId][i] == id)
                    return skillId;
            }
        }
        return -1;
    }

    public static boolean isSkillStar(int id) {
        for (int skillId = 0; skillId < SKILL_STARS.length; skillId++) {
            for (int i = 0; i < SKILL_STARS[skillId].length; i++) {
                if (SKILL_STARS[skillId][i] == id)
                    return true;
            }
        }
        return false;
    }

}
