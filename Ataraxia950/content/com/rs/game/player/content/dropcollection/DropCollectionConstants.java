package com.rs.game.player.content.dropcollection;

import com.rs.game.item.Item;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * ataraxia-server
 * paolo 07/06/2019
 * #Shnek6969
 */
public class DropCollectionConstants {


    /* Different kind of cluetypes */
    public enum CLUE_TYPE { EASY,MEDIUM,HARD,ELITE }

    /**
     * custom id since those bosses have no default npc id.
     * negative to avoid using existing ids.
     */
    public static final int BARROWS_ID = -20;
    public static final int ROTS_ID = -21;
    public static final int TELOS_ID = -22;
    public static final int AOD_ID = -50;
    public static final int QBD_ID = -23;
    public static final int RAGO_ID = -24;
    public static final int ARAXXOR_ID = 19464; //since drops aren't droped by the monster ID
    /**
     * ADD BOSSES  HERE
     */
    public enum BOSS_DATA {

        BANDOS("General Graardor",6260, new Item[] {new Item(11704), new Item(25022), new Item(11724), new Item(11726), new Item(25025), new Item(11728), new Item(25019), new Item(11710), new Item(11712), new Item(11714), new Item(33806)}),
        ARMADYL("Kree'arra",6222 , new Item[] {new Item(11702), new Item(11718), new Item(11720), new Item(11722), new Item(25016), new Item(25010), new Item(25013), new Item(11710), new Item(11712), new Item(11714), new Item(33804)}, 6223 ,6225, 6227),
        ZAMORAK("K'ril Tsutsaroth",6203, new Item[] {new Item(11708), new Item(24992), new Item(24995), new Item(24998), new Item(25007), new Item(25004), new Item(25001), new Item(11710), new Item(11712), new Item(11714), new Item(11716), new Item(11736), new Item(33805)}),
        SARADOMIN("Commander Zilyana", 6247, new Item[] {new Item(11706), new Item(25028), new Item(25031), new Item(25034), new Item(11730), new Item(25037), new Item(34855), new Item(11710), new Item(11712), new Item(11714), new Item(33807)}),
        NEX("Nex",13450, new Item[] {new Item(36159), new Item(20135), new Item(20139), new Item(20143), new Item(24977), new Item(24983), new Item(20147), new Item(20151), new Item(20155), new Item(24974), new Item(24989), new Item(20171), new Item(20159), new Item(20163), new Item(20167), new Item(24980), new Item(24986), new Item(25654), new Item(25664), new Item(33808)}),

        AOD("Angel of Death",AOD_ID, new Item[] {new Item(39584),new Item(39590), new Item(39586), new Item(39588), new Item(39592), new Item(39579), new Item(39574), new Item(39624)}),
        KALPHITE_QUEEN("Kalphite Queen",1160, new Item[] {new Item(3140), new Item(7158), new Item(3053), new Item(33816)}),
        KK("Kalphite King", 16697, new Item[] {new Item(36163), new Item(26587), new Item(26591), new Item(26579), new Item(26583), new Item(26595), new Item(26599), new Item(33815)}),
        MOLE("Giant Mole",3340, new Item[] {new Item(7158), new Item(33813)}),
        QBD("Queen Black Dragon", QBD_ID, new Item[] {new Item(24365), new Item(24340), new Item(24342), new Item(24344), new Item(24346), new Item(24352), new Item(11286), new Item(33825)}),

        KBD("King Black Dragon",50, new Item[] {new Item(11286),new Item(2581), new Item(2577), new Item(33818)}),
        CORP("Corporeal Beast",8133, new Item[] {new Item (11133), new Item(13734), new Item(13746), new Item(13748), new Item(13750), new Item(13752), new Item(13754), new Item(33812)}),
        CHAOS("Chaos Elemental",3200, new Item[] {new Item(13961), new Item(13958), new Item(13967), new Item(13970), new Item(13964), new Item(13973), new Item(13976), new Item(13979), new Item(13982), new Item(13887), new Item(13893), new Item(13899), new Item(13905), new Item(13896), new Item(13884), new Item(13890), new Item(13902), new Item(13876), new Item(13870), new Item(13873), new Item(13864), new Item(13858), new Item(13861), new Item(13867), new Item(33811)}),
        BARROW("Barrows",BARROWS_ID, new Item[] {new Item(36156), new Item (4716), new Item(4718), new Item(4720), new Item(4722), new Item(4753), new Item(4755), new Item(4757), new Item(4759), new Item(4724), new Item(4726), new Item(4728), new Item(4730), new Item(4745), new Item(4747), new Item(4749), new Item(4751), new Item(4732), new Item(4734), new Item(4736), new Item(4738), new Item(4708), new Item(4710), new Item(4712), new Item(4714), new Item(25652), new Item(25672),

        new Item(25918), new Item(25895),new Item(21736),  new Item(21744),  new Item(21752), new Item(21760),
                new Item(37433), new Item( 37437),  new Item(37441),  new Item(37445),  new Item(37449),

        }),


        TORM("Tormented Demons",8351,new Item[] {new Item(14484), new Item(25555), new Item(25481)}),

        PRIME("Dagannoth Prime", 2882, new Item[] {new Item(6562), new Item(6731), new Item(6739), new Item(33826)}),
        REX("Dagannoth Rex", 2883, new Item[] {new Item(6735), new Item(6737), new Item(6739), new Item(33827)}),
        SUPREME("Dagannoth Supreme", 2881, new Item[] {new Item(6724), new Item(6733), new Item(6739), new Item(33828)}),
        ROTS("Rise of the Six", ROTS_ID, new Item[] {new Item(30014),new Item(30018),new Item(30022), new Item(30026)}),
        TELOS("Telos", TELOS_ID, new Item[] {new Item(37619),new Item(37620),new Item(37621), new Item(37622),new Item(37624),new Item(37626), new Item(37679)}),

        ARRAXOR("Arraxor", ARAXXOR_ID, new Item[] {new Item(31722),new Item(31723),new Item(31724),new Item(31720),new Item(31719),new Item(31718),new Item(33809), new Item(33810),new Item(31748),new Item(31749),new Item(31750),new Item(31751),new Item(31752),new Item(31753)}),
        HELWYR("Helwyr",22310, new Item[] {new Item(37009), new Item(37012), new Item(37015), new Item(37085), new Item(40600), new Item(37033), new Item(37027), new Item(37182)}),
        VINDY("Vindicta And Gorvek",22320, new Item[] {new Item(37009), new Item(37012), new Item(37015), new Item(37070), new Item(37018), new Item(37030), new Item(37180), new Item(37181)}),
        TWINS("Twin Furies",22316, new Item[] {new Item(37009), new Item(37012), new Item(37015), new Item(37095), new Item(37090), new Item(37024), new Item(37032), new Item(37184),new Item(37185)}),
        GREG("Gregorovic",22443, new Item[] {new Item(37009), new Item(37012), new Item(37015), new Item(37075), new Item(37080), new Item(37021), new Item(37031), new Item(37183)}),

        VORAGO("Vorago", RAGO_ID, new Item[] {new Item(28617), new Item(28621), new Item(33717), new Item(28630), new Item(28627 )}),
        LEGPRIM("Legio Primus", 17149, new Item[] {new Item(28457), new Item(33819)}),
        LEGSEC("Legio Secundus", 17150, new Item[] {new Item(28458), new Item(33820)}),
        LEGTER("Legio Tertius", 17151, new Item[] {new Item(28459), new Item(33821)}),
        LEGQUAR("Legio Quartus", 17152, new Item[] {new Item(28460), new Item(33822)}),

        LEGQUIN("Legio Quintus", 17153, new Item[] {new Item(28461), new Item(33823)}),
        LEGSEX("Legio Sextus", 17154, new Item[] {new Item(28462), new Item(33824)}),
        RDRAGS("Rune Dragon", 21136, new Item[] {new Item(34987), new Item(34972), new Item(34974), new Item(34976)}),
        MAGISTER("The Magister", 24765, new Item[] {new Item(40320), new Item(40650), new Item(40669)});


        @Getter
        @Setter
        private String name;
        @Getter @Setter
        private int npcId;
        @Getter @Setter
        private Item[] drops;
        @Getter @Setter
        private int[] otherNpcIds;

        @Getter @Setter
        public static HashMap<Integer, List<Integer>> dropsByNPC;

        /**
         * for getting drops based on npc id
         */
        static {
            dropsByNPC = new HashMap<>();
            for (BOSS_DATA i : BOSS_DATA.values()) {
                List<Integer> dropIds = new ArrayList<>();
                for (Item drop : i.getDrops()) {
                    dropIds.add(drop.getId());
                }
                dropsByNPC.put(i.getNpcId(),dropIds);
                if (i.getOtherNpcIds() != null) {
                    for (int id : i.getOtherNpcIds()) {
                        dropsByNPC.put(id, dropIds);
                    }
                }
            }
        }
        BOSS_DATA(String name, int npcId, Item[] drops, int... otherNpcIds) {
            this.name = name;
            this.setNpcId(npcId);
            this.drops = drops;
            this.otherNpcIds = otherNpcIds;
        }
    }

    /**
     * Data for the minigames
     */
    public enum MINIGAME_DATA {

        FIGHT_CAVES("Fight Caves", 1, new Item[] {new Item(6570),new Item(21512)}),
        FIGHT_KILN("Fight Kiln", 2, new Item[] {new Item(23659),new Item(31610),new Item(31611),new Item(33814)});


        @Getter @Setter
        public String name;
        @Getter @Setter
        public int minigameId;
        @Getter @Setter
        public Item[] drops;

        MINIGAME_DATA(String name, int minigameId, Item[] drops){
            this.setName(name);
            this.setMinigameId(minigameId);
            this.setDrops(drops);
        }
    }

}
