package com.rs.game.player.content.interfaces;
//
//import com.rs.Settings;
//import com.rs.game.item.Item;
//import com.rs.game.player.Player;
//import com.rs.utils.ItemExamines;
//import com.rs.utils.Utils;
//
//public class Shops2 {
//     /*
//     * @author Sagacity - 01/04/2019
//     * To create new shops you have to follow these steps:
//     * 1 - Create a array with the items (only 40 per shops)
//     * 2 - Add the array name to CATTEGORIES array.
//     * 3 - Add the curency check to the getPointsReward() and setPointsReward()
//     * 4 - Shop is pretty much done :) (20 is the maximum amount of cattegories the interface can hold).
//     */
//
//    private static int cattegory = 0;
//
//    public static int getPrice() {
//        return price;
//    }
//
//    public static void setPrice(int price) {
//        Shops2.price = price;
//    }
//
//    private static int price = -1;
//
//    public static int getSelectedItem() {
//        return selectedItem;
//    }
//
//    public static void setSelectedItem(int selectedItem) {
//        Shops2.selectedItem = selectedItem;
//    }
//
//    private static int selectedItem = -1;
//
//    public static Object[][] getSelectedCattegory() {
//        return selectedCattegory;
//    }
//
//    public static void setSelectedCattegory(Object[][] selectedCattegory) {
//        Shops2.selectedCattegory = selectedCattegory;
//    }
//
//    private static Object[][] selectedCattegory = null;
//
//    private static int INTER = 3018; //comecei no 228
//    private static int SHOP_NAME = 16, ITEM_NAME = 324;
//    private static final int[] ITM_COMPS = {107, 114, 121, 128, 135, 142, 149, 155, 161, 168, 174, 180, 186, 192, 198, 204, 210, 216, 222, 228, 234, 240, 246, 252, 258, 264, 270, 276, 282, 288, 294, 300, 306, 312, 318};
//    private static final int[] ITM_PRICE_COMP = {109, 116, 123, 130, 137, 144, 151, 157, 163, 170, 176, 182, 188, 194, 200, 206, 212, 218, 224, 230, 236, 242, 248, 254, 260, 266, 272, 278, 284, 290, 296, 302, 308, 314, 320};
//    private static final int[] ITM_SPRITE_COMPS = {110, 117, 124, 131, 138, 145, 152, 158, 164, 171, 177, 183, 189, 195, 201, 207, 213, 219, 225, 231, 237, 243, 249, 255, 261, 267, 273, 279, 285, 291, 297, 303, 309, 315, 321};
//    private static final int[] ITM_SLOT_COMPS = {119, 120, 127, 134, 141, 148, 154, 166, 167, 173, 179, 185, 191, 197, 203, 209, 215, 221, 227, 233, 239, 245, 251, 257, 263, 269, 275, 281, 287, 293, 299, 305, 311, 317, 323};
//    private static final int[] CAT_COMPS = {27, 31, 35, 39, 43, 47, 51, 55, 59, 63, 67, 71, 75, 79, 83, 87, 91, 95, 99, 103};
//    private static final int[] CAT_CLICKABLE_COMPS = {29, 33, 37, 41, 45, 49, 53, 57, 61, 65, 69, 73, 77, 81, 85, 89, 93, 97, 101, 105};
//
//    public static int getCattegory() {
//        return cattegory;
//    }
//
//    public static void setCattegory(int cattegory) {
//        Shops2.cattegory = cattegory;
//    }
//
//    private static final Object[][] PKP_ITEMS = {{ 3842, 15 }, { 19613, 10 },
//            { 19617, 10 }, { 11090, 1 }, { 8850, 15 }, { 20072, 30 }, { 23691, 30 }, { 23679, 200 }, { 23695, 200 }, { 19784, 500 }, { 15241, 30 }, {21371, 250}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}
//            , {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}};
//
//    private static final Object[][] DONATOR_ITEMS = {{ 27853, 1 }, { 27850, 10 }, { 27851, 18 }, { 27852, 25 }, { 27854, 50 }, { 27855, 78 },{ 27857, 140 },
//            { 1046, 50 }, { 1040, 56 }, { 1044, 61 }, { 1038, 63 }, { 1048, 65 }, { 1042, 65 },
//            { 1053, 25 }, { 1055, 25 }, { 1057, 25 }, { 1050, 70 }, { 9920, 10 }, { 24437, 35 },
//            { 10728, 10 }, { 10727, 10 }, { 10726, 10 }, { 10724, 10 }, { 10725, 10 },
//            { 9470, 15 }, { 22215, 20 }, { 22217, 20 }, { 22218, 20 }, { 14595, 20 }, { 14603, 20 }, { 14605, 10 },  { 14602, 10 },
//            { 27860, 2 }, {27861, 1},{ 1765, 2 }, { 1767, 2 }, { 1771, 2 }, { 1773, 2 }, { 1763, 2 }, { 29995, 75 }};
//
//    private static final Object[][] DONATOR_ITEMS2 = {{ 19580, 110 },{ 11858, 110 },{ 11860, 110 },{ 11862, 110 }, { 989, 1 }, { 605, 2 }, { 6666, 5 }, { 22321, 25 }, { 1419, 15 }, { 26643, 150 },
//            {null, 0}, {null, 0},{null, 0},{null, 0},{null, 0},{null, 0},{null, 0},{null, 0},{null, 0},{null, 0},{null, 0},{null, 0},{null, 0},{null, 0},{null, 0},
//            {null, 0},{null, 0},{null, 0},{null, 0},{null, 0},{null, 0},{null, 0},{null, 0},{null, 0},{null, 0},{null, 0},{null, 0},{null, 0},{null, 0},{null, 0},{null, 0},};
//
//    private static final Object[][] VOTING_ITEMS = {
//            { 24544, 8 }, {620,1}, {27830,15}, {18744,10}, {18745,10},
//            {18746,10}, {11020,10}, {11021,10}, {11022,10}, {27831,8}, {11019,10}, {4566,10}, {19747,10}, {7671,20}, {7673,20}, {19333, 100}
//            , {14632, 10}, {27859, 2}, {null, 0}, {null, 0}, {null, 0}
//            , {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}};
//
//    private static final Object[][] VOTING_ITEMS2 = {
//            {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0},
//            {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}
//            , {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}
//            , {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}};
//
//
//    private static final Object[][] PRESTIGE_ITEMS = {{ 24433, 1 }, { 23674, 2 },
//            { 20929, 3 }, { 10858, 4 }, { 24187, 2 }, { 20821, 5 },
//            { 24334, 2 }, { 18705, 1 }, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}
//            , {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}};
//
//    private static final Object[][] INFERNAL_ITEMS = {{ 27858, 1000 }, { 3751, 250 }, { 2412, 1000 }, { 2413, 1000 }, { 2414, 1000 },
//            { 3749, 250 }, { 3755, 250 }, { 4151, 10900 }, { 13003, 200 }, { 13006, 250 }, { 10548, 5000 },
//            { 11663, 16000 }, { 11664, 16000 }, { 11665, 16000 }, {19712, 16000}, { 8839, 15000 }, { 8840, 15000 }, { 8842, 5000 }, { 8841, 5000 }, { 19712, 15000 },
//            { 12915, 250 }, { 12929, 350 }, { 4131, 300 }, { 11732, 4150 }, { 10551, 15000 }, { 10828, 300 },
//            { 6889, 12000 }, { 6914, 12000 }, { 6918, 4000 }, { 6924, 4000 }, { 6920, 7000 }, { 6916, 4000 }, { 6922, 3000 },
//            { 11920, 4000 }, { 11922, 4000 }, { 11924, 4000 }, { 21365, 50 }, { 24386, 6000 }, { 775, 5000 },
//            { 861, 1500 }};
//
//    private static final Object[][] LOYALTY_ITEMS = {{ 22302, 4000 },
//            { 22298, 2300 }, { 22296, 2300 }, { 22901, 1200 },
//            { 22907, 1200 }, { 23868, 4000 }, { 22300, 2300 }, { 22294, 2300 }, { 22292, 2300 }, { 22290, 1400 },
//            { 22288, 500 }, { 22268, 1250 }, { 22276, 1600 }, { 20965, 500 }, { 22274, 1400 }, { 20966, 500 },
//            { 22270, 1675 }, { 22286, 1400 }, { 22284, 500 }, { 22282, 1400 }, { 22280, 500 },
//            { 23866, 5700 }, { 22272, 1675 }, { 20967, 425 }, { 20958, 1250 }, { 20962, 425 }, { 22897, 5000 },
//            { 23854, 6350 }, { 23876, 6350 }, { 23874, 6350 }, { 23852, 4250 }, { 22903, 3050 }, { 22909, 3050 },
//            { 23850, 1200 }, { 23848, 500 }, { 22899, 500 }, { 22905, 500 }, { 23864, 5700 },{null, 0},{null, 0}};
//
//    private static final Object[][] QUEST_ITEMS = {{null, 0}, {null, 0},
//            {null, 0}, {null, 0}, {null, 0}, {null, 0},
//            {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}
//            , {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}, {null, 0}};
//
//
//    private static final Object[][] DUNGEON_ITEMS = {{18337, 70000},{19675, 55000}, {18336, 10000},{19890, 20000},
//            {18344, 153000},{18839, 153000},{18347, 48500},{18346, 43000},
//            {18333, 6500},{18334, 15500},{19669, 100000},{18335, 50000},
//            {18349, 300000},{18351, 300000},{18353, 300000},{18355, 300000},
//            {18357, 300000},{18359, 250000},{18361, 250000},{18363, 250000},
//            {19894, 2500000},{19886, 8500},{19887, 50000},{18338, 10000},
//            {18339, 10000},{19888, 100000},{29964, 100000},{23752, 30000},{null, 0},{null, 0},{null, 0},{null, 0},{null, 0},{null, 0},{null, 0},
//            {null, 0},{null, 0},{null, 0},{null, 0},{null, 0},{null, 0}};
//
//    private static final Object[][] IRONMAN_ITEMS = {{ 892, 5 }, { 379, 25 }, { 23447, 10 }, { 1625, 100 },
//            { 1627, 105 }, { 1623, 110 }, { 1621, 115 }, { 1619, 120 }, { 199, 10 }, { 201, 15 },  { 203, 20 },
//            { 205, 25 }, {null, 0}, {null, 0}, {null, 0}, {null, 0},{null, 0},{null, 0},{null, 0},{null, 0},{null, 0},
//            {null, 0},{null, 0},{null, 0},{null, 0},{null, 0},{null, 0},{null, 0},{null, 0},{null, 0},{null, 0},{null, 0},{null, 0},{null, 0},{null, 0},
//            {null, 0},{null, 0},{null, 0},{null, 0},{null, 0},{null, 0}};
//
//
//    private static final Object[][] CATTEGORIES = {{"PK Points", /*439*/1583, PKP_ITEMS}, {"Vote Points I", 2404, VOTING_ITEMS}, {"Vote Points II", 2404, VOTING_ITEMS2}, {"Donator Points I", 6230, DONATOR_ITEMS}, {"Donator Points II", 6230, DONATOR_ITEMS2}, {"Prestige Points I", 3139, PRESTIGE_ITEMS}, {Settings.SERVER_NAME+" Points I", /*6272*/4414, INFERNAL_ITEMS},  {"lronman Points I", /*6272*/4414, IRONMAN_ITEMS},
//            {"Achiev Points I", /*6235*/ /*4272*/ 836, INFERNAL_ITEMS}, {"Loyalty Points I", 6234, LOYALTY_ITEMS}, {"AbstractQuest Points I", 835, QUEST_ITEMS}, {"Dungeon Tokens I", 2184, DUNGEON_ITEMS}, null, null, null, null, null, null, null, null};
//
//    public static void sendInterface(Player player) {
//        setCattegory(0);
//        setSelectedItem(-1);
//        setSelectedCattegory(PKP_ITEMS);
//        sendCattegories(player);
//        sendShopItems(player, PKP_ITEMS, 0);
//        player.getInterfaceManager().sendInterface(INTER);
//    }
//
//    public static void sendDungShop(Player player) {
//        setCattegory(11);
//        setSelectedItem(-1);
//        setSelectedCattegory(DUNGEON_ITEMS);
//        sendCattegories(player);
//        sendShopItems(player, DUNGEON_ITEMS, 11);
//        player.getInterfaceManager().sendInterface(INTER);
//    }
//
//    public static void sendIronmanShop(Player player) {
//        setCattegory(10);
//        setSelectedItem(-1);
//        setSelectedCattegory(IRONMAN_ITEMS);
//        sendCattegories(player);
//        sendShopItems(player, IRONMAN_ITEMS, 10);
//        player.getInterfaceManager().sendInterface(INTER);
//    }
//
//    private static void sendShopItems(Player player, Object[][] cattegory, int id) {
//        player.getPackets().sendIComponentText(INTER, SHOP_NAME, "Store - " + CATTEGORIES[id][0]);
//        player.getPackets().sendHideIComponent(INTER, ITEM_NAME, false);
//        updateCurrency(player);
//        for (int i = 0; i < ITM_SLOT_COMPS.length; i++) {
//            sendOptions(player, i);
//            if (cattegory[i][0] == null) {
//                player.getPackets().sendHideIComponent(INTER, ITM_COMPS[i], true);
//                continue;
//            } else {
//                for (int i2 = 0; i2 < ITM_SPRITE_COMPS.length; i2++) {
//                    player.getPackets().sendIComponentSprite(INTER, ITM_SPRITE_COMPS[i2], (Integer) CATTEGORIES[getCattegory()][1]);
//                }
//                player.getPackets().sendHideIComponent(INTER, ITM_COMPS[i], false);
//                player.getPackets().sendIComponentText(INTER, ITM_PRICE_COMP[i], "" + intToKOrMil((int) cattegory[i][1]));
//                player.getPackets().sendItems(i, new Item[]{new Item((Integer) cattegory[i][0], 1)});
//            }
//        }
//    }
//
//    public static void handleButtons(Player player, int componentId, int packetId, int slotId2) {
//        handleCattegoryButton(player, componentId);
//        for (int i3 = 0; i3 < ITM_SLOT_COMPS.length; i3++) {
//            if (componentId == ITM_SLOT_COMPS[i3]) {
//                handleSelectItem(player, componentId, slotId2, getSelectedCattegory());
//                setSelectedItem(slotId2);
//                switch (packetId) {
//                    case 67: //buy 1
//                        buyItem(player, getCattegory(), 1);
//                        break;
//                    case 5: //buy 10
//                        buyItem(player, getCattegory(), 10);
//                        break;
//                    case 55: //buy 50
//                        buyItem(player, getCattegory(), 50);
//                        break;
//                    case 68: //buy 100
//                        buyItem(player, getCattegory(), 100);
//                        break;
//                }
//            }
//        }
//        switch (componentId) {
//            case 328: //Close info tab:
//                player.getPackets().sendHideIComponent(INTER, 325, true); //full panel
//                player.getPackets().sendHideIComponent(INTER, 17, false); //full panel
//            break;
//        }
//    }
//
//    public static void handleCattegoryButton(Player player, int componentId) {
//        //player.getPackets().sendHideIComponent(INTER, 177, true);
//        for (int i = 0; i < CATTEGORIES.length; i++) {
//            if (componentId == CAT_CLICKABLE_COMPS[i]) {
//                if (CATTEGORIES[i][2] == IRONMAN_ITEMS && !player.isIronmanMode()) {
//                    player.sm("This store is restricted to ironmans players!");
//                    return;
//                }
//                if ((CATTEGORIES[i][2] == DONATOR_ITEMS || CATTEGORIES[i][2] == DONATOR_ITEMS2 || CATTEGORIES[i][2] == VOTING_ITEMS || CATTEGORIES[i][2] == VOTING_ITEMS2) && player.isIronmanMode()) {
//                    player.sm("Ironmans cannot open this store, sorry!");
//                    return;
//                }
//                setCattegory(i);
//                sendShopItems(player, (Object[][]) CATTEGORIES[i][2], i);
//                setSelectedCattegory((Object[][]) CATTEGORIES[i][2]);
//            }
//        }
//    }
//
//    public static void sendItemInfo(Player player, int componentId, int slotId2, Object[][] cattegory) {
//        player.getPackets().sendHideIComponent(INTER, 17, true); //full panel
//        player.getPackets().sendHideIComponent(INTER, 336, true); //buy button
//        Item item = new Item(slotId2);
//        String bonuses = "<col=ff9c24>Attack Bonuses</col><br>" + "Stab: "+item.getDefinitions().getStabAttack()+" - Slash: "+item.getDefinitions().getSlashAttack()+"<br>" + "Crush: "+item.getDefinitions().getCrushAttack()+" - Magic: "+item.getDefinitions().getMagicAttack()+"<br>"+ "Ranged: "+item.getDefinitions().getRangeAttack()+"<br>"+"<col=ff9c24>Defence Bonuses</col><br>"+"Stab: "+item.getDefinitions().getStabDef()+" - Slash: "+item.getDefinitions().getSlashDef()+"<br>" +"Crush: "+item.getDefinitions().getCrushDef()+" - Magic: "+item.getDefinitions().getMagicDef()+"<br>"+"Ranged: "+item.getDefinitions().getRangeDef()+" - Summ:" +item.getDefinitions().getSummoningDef()+"<br>"+"Ab. ML: "+item.getDefinitions().getAbsorveMeleeBonus()+" - Ab. MG: "+item.getDefinitions().getAbsorveMageBonus()+"<br>" +"Ab. RG: "+item.getDefinitions().getAbsorveRangeBonus()+"<br>"+"<col=ff9c24>Other Bonuses</col><br>"+"STR: "+item.getDefinitions().getStrengthBonus()+" - RSTR: "+item.getDefinitions().getRangedStrBonus()+"<br>" +"Pray: "+item.getDefinitions().getPrayerBonus()+" - MDMG: "+item.getDefinitions().getMagicDamage()+"<br>";
//        player.getPackets().sendHideIComponent(INTER, 325, false);
//        player.getPackets().sendIComponentText(INTER, 335, "<u><col=ffffff>"+new Item(slotId2).getName()+"</col></u><br><br>"+ ((new Item(slotId2).getDefinitions().isWearItem() && !itemHasBonuses(item)) ? bonuses : ItemExamines.getExamine(item).replaceAll(".{20}", "$0<br>")));
//        player.getPackets().sendIComponentSprite(INTER, 332, (Integer) CATTEGORIES[getCattegory()][1]);
//    }
//
//    public static void updateCurrency(Player player) {
//        player.getPackets().sendIComponentText(INTER, ITEM_NAME, "I have <col=ff9c24>"+ Utils.formatNumber(getPoints(player, getCattegory()))+"</col> "+ CATTEGORIES[getCattegory()][0].toString().replace("I", "").replace("II", "").replace("III", "")+"!");
//    }
//
//    public static void handleSelectItem(Player player, int componentId, int slotId2, Object[][] cattegory) {
//        for (int i = 0; i < ITM_SLOT_COMPS.length; i++) {
//            if (componentId == ITM_SLOT_COMPS[i]) {
//                sendItemInfo(player, componentId, slotId2, cattegory);
//                player.getPackets().sendInterSetItemsOptionsScript(INTER, 334, 90, 1, 1, "");
//                player.getPackets().sendUnlockIComponentOptionSlots(INTER, 334, 0, 160, 0);
//                player.getPackets().sendItems(90,new Item[]{new Item((Integer) cattegory[i][0], 1)});
//                player.getPackets().sendIComponentText(INTER, 331, ""+intToKOrMil((int) cattegory[i][1]));
//                setPrice((Integer) cattegory[i][1]);
//            }
//        }
//    }
//
//    private static void sendCattegories(Player player) {
//        for (int i = 0; i < CAT_COMPS.length; i++) {
//            if (CATTEGORIES[i] == null) {
//                player.getPackets().sendHideIComponent(INTER, CAT_COMPS[i], true);
//            } else {
//                player.getPackets().sendHideIComponent(INTER, CAT_COMPS[i], false);
//                player.getPackets().sendHideIComponent(INTER, CAT_CLICKABLE_COMPS[i], false);
//                player.getPackets().sendIComponentText(INTER, CAT_CLICKABLE_COMPS[i], (String) CATTEGORIES[i][0]);
//                if ((Integer) CATTEGORIES[i][1] != 0) {
//                    player.getPackets().sendHideIComponent(INTER, ITM_SPRITE_COMPS[i], false);
//                    if ((Integer) CATTEGORIES[i][1] != 0) {
//                    } else {
//                        player.getPackets().sendHideIComponent(INTER, ITM_SPRITE_COMPS[i], true);
//                    }
//                }
//            }
//        }
//    }
//
//    public static void sendOptions(Player player, int i) {
//        player.getPackets().sendInterSetItemsOptionsScript(INTER, ITM_SLOT_COMPS[i], i, 1, 1, "Info", "Buy 1", "Buy 10", "Buy 50","Buy 100");
//        player.getPackets().sendUnlockIComponentOptionSlots(INTER, ITM_SLOT_COMPS[i], 0, 160, 0, 1, 2, 3, 4);
//    }
//
//    public static int getPoints(Player player, int type) {
//        switch (type) {
//            case 0: //PK points
//                return player.getPKP();
//            case 1: //vote points
//            case 2:
//                return player.getVotePoints();
//            case 3: //Donator points
//            case 4:
//                return player.getGoldPoints();
//            case 5: //Prestige
//                return player.getPrestigePoints();
//            case 6: //Servername points
//            case 7:
//                return player.getInfernalPoints();
//            case 8: //Infernal points
//                return player.getAchievementPoints();
//            case 9: //AbstractQuest points
//                return player.getQuestPoints();
//            case 10: //Dungeon tokens
//                return player.getDungTokens();
//            default:
//                return 0;
//        }
//    }
//
//    public static void setPoints(Player player, int type, int quant) {
//        switch (type) {
//            case 0: //Pk points
//                player.setPKP(getPoints(player, 0) - quant);
//                break;
//            case 1: //Vote points
//            case 2:
//                player.setVotePoints(getPoints(player, 1) - quant);
//                break;
//            case 3: //Donator points
//            case 4:
//                player.setGoldPoints(getPoints(player, 3) - quant);
//                break;
//            case 5: //Prestige points
//                player.setPrestigePoints(getPoints(player, 5) - quant);
//                break;
//            case 6: //Servername points
//            case 7:
//            case 11:
//                player.setInfernalPoints(getPoints(player, 6) - quant);
//                break;
//            case 8:
//                player.setAchievementPoints(getPoints(player, 8) - quant);
//                break;
//            case 9: //quest points
//                player.setQuestPoints(getPoints(player, 9) - quant);
//                break;
//            case 10:
//                player.setDungTokens(getPoints(player, 10) - quant);
//                break;
//        }
//    }
//
//    public static void buyItem(Player player, int cattegory, int quantity) {
//            if (getSelectedItem() == -1 || getSelectedCattegory() == null || getPrice() == -1) {
//                Logger.getGlobal().info("[buyItem] Null cattegory or selected item");
//                return;
//            }
//            if (!new Item(getSelectedItem()).getDefinitions().isStackable() && quantity > 1) {
//                player.sm("You can only buy stackable items with this option!");
//                return;
//            }
//            if (getPoints(player, cattegory) < getPrice() * quantity) {
//                player.sm("You don't have enough currency, you need at least: <col=f0000>"+Utils.formatNumber(getPrice() * quantity)+"</col> points!");
//                return;
//            }
//            if (player.getInventory().getFreeSlots() < 1) {
//                player.sm("You need at least 1 free inventory slot to buy this");
//                return;
//            }
//            setPoints(player, cattegory, getPrice() * quantity);
//            updateCurrency(player);
//            player.getInventory().addItem(getSelectedItem(), quantity);
//            //player.sm("Item bought");
//    }
//
//    private static char[] c = new char[]{'K', 'M', 'B'};
//
//    private static String formatValue(double a, int b) {
//        double d = ((long) a / 100) / 10.0;
//        boolean isRound = (d * 10) %10 == 0;
//        return (d < 1000?((d > 99.9 || isRound || (!isRound && d > 9.99)?
//                (int) d * 10 / 10 : d + "") + "" + c[b]) : formatValue(d, b+1));
//    }
//
//    public static String intToKOrMil(int j) {
//        if(j < 10000)
//            return String.valueOf(j);
//        if(j < 0x989680)
//            return formatValue(j, 0);
//        else if(j < 0x3B9ACA00)
//            return formatValue(j, 0);
//        else
//            return formatValue(j, 0);
//    }
//
//    public static boolean itemHasBonuses(Item item) {
//        if (item.getDefinitions().getStabAttack() <= 0 && item.getDefinitions().getSlashAttack() <= 0 && item.getDefinitions().getCrushAttack() <= 0 && item.getDefinitions().getMagicAttack() <= 0 && item.getDefinitions().getRangeAttack() <= 0 && item.getDefinitions().getStabDef() <= 0 && item.getDefinitions().getSlashDef() <= 0 && item.getDefinitions().getCrushDef() <= 0 && item.getDefinitions().getMagicDef() <= 0 && item.getDefinitions().getRangeDef() <= 0 && item.getDefinitions().getSummoningDef() <= 0 && item.getDefinitions().getAbsorveMeleeBonus() <= 0 && item.getDefinitions().getAbsorveMageBonus() <= 0 && item.getDefinitions().getAbsorveRangeBonus() <= 0 && item.getDefinitions().getStrengthBonus() <= 0 && item.getDefinitions().getRangedStrBonus() <= 0 && item.getDefinitions().getPrayerBonus() <= 0 && item.getDefinitions().getMagicDamage() <= 0)
//            return true;
//         else
//            return false;
//    }
//}