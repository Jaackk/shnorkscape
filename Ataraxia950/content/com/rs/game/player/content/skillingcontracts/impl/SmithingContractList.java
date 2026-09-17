package com.rs.game.player.content.skillingcontracts.impl;

import com.google.common.collect.ImmutableList;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.smithing.defs.SmeltingBar;
import com.rs.game.player.content.skillingcontracts.ContractList;
import com.rs.game.player.content.skillingcontracts.SkillingContract;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class SmithingContractList extends ContractList {

    public static void listenSmelt(Player player, SmeltingBar bar) {
        switch (bar) {
            case BRONZE:
                player.getContracts().recordAction(Skills.SMITHING, 0);
                break;
            case IRON:
                player.getContracts().recordAction(Skills.SMITHING, 1);
                break;
            case SILVER:
                player.getContracts().recordAction(Skills.SMITHING, 2);
                break;
            case STEEL:
                player.getContracts().recordAction(Skills.SMITHING, 3);
                break;
            case MITHRIL:
                player.getContracts().recordAction(Skills.SMITHING, 4);
                break;
            case GOLD:
                player.getContracts().recordAction(Skills.SMITHING, 5);
                break;
            case ADAMANT:
                player.getContracts().recordAction(Skills.SMITHING, 6);
                break;
            case RUNE:
                player.getContracts().recordAction(Skills.SMITHING, 7);
                break;
            case CORRUPTED_ORE:
                player.getContracts().recordAction(Skills.SMITHING, 41);
                break;
        }
    }

    public static void listenSmith(Player player, int itemId) {
        switch (itemId) {
            // Bronze.
            case 1189: // Kiteshield.
                player.getContracts().recordAction(Skills.SMITHING, 8);
                break;
            case 1155: // Full helm.
                player.getContracts().recordAction(Skills.SMITHING, 9);
                break;
            case 39: // Arrowheads (x15).
                player.getContracts().recordAction(Skills.SMITHING, 10, 15);
                break;
            case 1075: // Platelegs.
                player.getContracts().recordAction(Skills.SMITHING, 11);
                break;
            case 1117: // Platebody.
                player.getContracts().recordAction(Skills.SMITHING, 12);
                break;

            // Iron.
            case 40: // Arrowheads (x15).
                player.getContracts().recordAction(Skills.SMITHING, 13, 15);
                break;
            case 1191: // Kiteshield.
                player.getContracts().recordAction(Skills.SMITHING, 14);
                break;
            case 1153: // Full helm.
                player.getContracts().recordAction(Skills.SMITHING, 15);
                break;
            case 1067: // Platelegs.
                player.getContracts().recordAction(Skills.SMITHING, 16);
                break;
            case 1115: // Platebody.
                player.getContracts().recordAction(Skills.SMITHING, 17);
                break;

            // Steel.
            case 41: // Arrowheads (x15).
                player.getContracts().recordAction(Skills.SMITHING, 18, 15);
                break;
            case 1193: // Kiteshield.
                player.getContracts().recordAction(Skills.SMITHING, 19);
                break;
            case 1157: // Full helm.
                player.getContracts().recordAction(Skills.SMITHING, 20);
                break;
            case 1069: // Platelegs.
                player.getContracts().recordAction(Skills.SMITHING, 21);
                break;
            case 1119: // Platebody.
                player.getContracts().recordAction(Skills.SMITHING, 22);
                break;

            // Mithril.
            case 42: // Arrowheads (x15).
                player.getContracts().recordAction(Skills.SMITHING, 23, 15);
                break;
            case 1197: // Kiteshield.
                player.getContracts().recordAction(Skills.SMITHING, 24);
                break;
            case 1159: // Full helm.
                player.getContracts().recordAction(Skills.SMITHING, 25);
                break;
            case 1071: // Platelegs.
                player.getContracts().recordAction(Skills.SMITHING, 26);
                break;
            case 1121: // Platebody.
                player.getContracts().recordAction(Skills.SMITHING, 27);
                break;

            // Adamant.
            case 43: // Arrowheads (x15).
                player.getContracts().recordAction(Skills.SMITHING, 28, 15);
                break;
            case 1199: // Kiteshield.
                player.getContracts().recordAction(Skills.SMITHING, 29);
                break;
            case 1161: // Full helm.
                player.getContracts().recordAction(Skills.SMITHING, 30);
                break;
            case 1073: // Platelegs.
                player.getContracts().recordAction(Skills.SMITHING, 31);
                break;
            case 1123: // Platebody.
                player.getContracts().recordAction(Skills.SMITHING, 32);
                break;
            case 823: // Dart tips (x10).
                player.getContracts().recordAction(Skills.SMITHING, 38, 10);
                break;

            // Rune.
            case 44: // Arrowheads (x15).
                player.getContracts().recordAction(Skills.SMITHING, 33, 15);
                break;
            case 1201: // Kiteshield.
                player.getContracts().recordAction(Skills.SMITHING, 34);
                break;
            case 1163: // Full helm.
                player.getContracts().recordAction(Skills.SMITHING, 35);
                break;
            case 1079: // Platelegs.
                player.getContracts().recordAction(Skills.SMITHING, 36);
                break;
            case 1127: // Platebody.
                player.getContracts().recordAction(Skills.SMITHING, 37);
                break;
            case 824: // Dart tips (x10).
                player.getContracts().recordAction(Skills.SMITHING, 39, 10);
                break;
            case 9381: // Bolts (unf) (x10).
                player.getContracts().recordAction(Skills.SMITHING, 40, 10);
                break;
        }
    }

    public SmithingContractList() {
        super(Skills.SMITHING, true);
    }

    @Override
    protected ImmutableList<SkillingContract> buildContracts() {
        return ImmutableList.of(
                // Smelting contracts.
                new SkillingContract(0, "Smelt <amount> bronze bars.", 1, 80, 160),
                new SkillingContract(1, "Smelt <amount> iron bars.", 10, 80, 160),
                new SkillingContract(2, "Smelt <amount> silver bars.", 20, 80, 160),
                new SkillingContract(3, "Smelt <amount> steel bars.", 30, 80, 160),
                new SkillingContract(4, "Smelt <amount> mithril bars.", 50, 80, 160),
                new SkillingContract(5, "Smelt <amount> gold bars.", 40, 80, 160),
                new SkillingContract(6, "Smelt <amount> adamant bars.", 70, 60, 120),
                new SkillingContract(7, "Smelt <amount> rune bars.", 85, 40, 80),

                // Smithing contracts.
                new SkillingContract(8, "Smith <amount> bronze kiteshields.", 12, 30, 60),
                new SkillingContract(9, "Smith <amount> bronze full helms.", 7, 30, 60),
                new SkillingContract(10, "Smith <amount> bronze arrowheads.", 5, 450, 900),
                new SkillingContract(11, "Smith <amount> bronze platelegs.", 16, 30, 60),
                new SkillingContract(12, "Smith <amount> bronze platebodies.", 18, 15, 30),

                new SkillingContract(13, "Smith <amount> iron arrowheads.", 20, 350, 700),
                new SkillingContract(14, "Smith <amount> iron kiteshields.", 27, 30, 60),
                new SkillingContract(15, "Smith <amount> iron full helms.", 22, 30, 60),
                new SkillingContract(16, "Smith <amount> iron platelegs.", 31, 30, 60),
                new SkillingContract(17, "Smith <amount> iron platebodies.", 33, 15, 30),

                new SkillingContract(18, "Smith <amount> steel arrowheads.", 35, 350, 700),
                new SkillingContract(19, "Smith <amount> steel kiteshields.", 43, 30, 60),
                new SkillingContract(20, "Smith <amount> steel full helms.", 37, 30, 60),
                new SkillingContract(21, "Smith <amount> steel platelegs.", 46, 30, 60),
                new SkillingContract(22, "Smith <amount> steel platebodies.", 48, 15, 30),

                new SkillingContract(23, "Smith <amount> mithril arrowheads.", 55, 350, 700),
                new SkillingContract(24, "Smith <amount> mithril kiteshields.", 62, 30, 60),
                new SkillingContract(25, "Smith <amount> mithril full helms.", 57, 30, 60),
                new SkillingContract(26, "Smith <amount> mithril platelegs.", 66, 30, 60),
                new SkillingContract(27, "Smith <amount> mithril platebodies.", 68, 15, 30),

                new SkillingContract(28, "Smith <amount> adamant arrowheads.", 75, 350, 700, false),
                new SkillingContract(29, "Smith <amount> adamant kiteshields.", 82, 30, 60, false),
                new SkillingContract(30, "Smith <amount> adamant full helms.", 77, 30, 60, false),
                new SkillingContract(31, "Smith <amount> adamant platelegs.", 86, 30, 60, false),
                new SkillingContract(32, "Smith <amount> adamant platebodies.", 88, 15, 30),

                new SkillingContract(33, "Smith <amount> rune arrowheads.", 90, 350, 700, false),
                new SkillingContract(34, "Smith <amount> rune kiteshields.", 97, 30, 60, false),
                new SkillingContract(35, "Smith <amount> rune full helms.", 92, 30, 60, false),
                new SkillingContract(36, "Smith <amount> rune platelegs.", 99, 30, 60, false),
                new SkillingContract(37, "Smith <amount> rune platebodies.", 99, 15, 30),

                new SkillingContract(38, "Smith <amount> adamant dart tips.", 74, 250, 700, false),
                new SkillingContract(39, "Smith <amount> rune dart tips.", 89, 250, 700, false),
                new SkillingContract(40, "Smith <amount> runite bolts (unf).", 88, 250, 700),
                new SkillingContract(41, "Smelt <amount> corrupted ore.", 89, 40, 80)

        );
    }
}