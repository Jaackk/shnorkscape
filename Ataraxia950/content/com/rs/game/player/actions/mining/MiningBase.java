package com.rs.game.player.actions.mining;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.item.Item;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.actions.hunter.TrapAction;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public abstract class MiningBase extends Action {

    protected int emoteId;
    protected int pickaxeTime;
    protected int orePercentage;


    public static void prospect(final Player player, final String endMessage) {
        prospect(player, "You examine the rock for ores....", endMessage);
    }

    public static void prospect(final Player player, String startMessage, final String endMessage) {
        player.getPackets().sendGameMessage(startMessage, true);
        player.lock(5);
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                player.getPackets().sendGameMessage(endMessage);
            }
        }, 4);
    }


    public enum PickaxeDefinitions {

        NOVITE(16295, 1, 13074, 1, 5),

        BATHUS(16297, 10, 13075, 3, 5),

        MARMAROS(16299, 20, 13076, 5, 5),

        KRATONITE(16301, 30, 13077, 7, 5),

        FRACTITE(16303, 40, 13078, 10, 5),

        ZEPHYRIUM(16305, 50, 13079, 12, 5),

        ARGONITE(16307, 60, 13080, 13, 5),

        KATAGON(16309, 70, 13081, 15, 5),

        GORGONITE(16311, 80, 13082, 16, 5),

        PROMETHIUM(16313, 90, 13083, 17, 5),

        PRIMAL(16315, 99, 13084, 20, 5),

        BRONZE(1265, 1, 32540, 1, 15),

        IRON(1267, 1, 32548, 2, 15),

        STEEL(1269, 6, 32552, 3, 20),

        MITHRIL(1273, 21, 32558, 5, 25),

        ADAMANT(1271, 31, 32562, 7, 30),

        RUNE(1275, 41, 32566, 10, 35),

        DRAGON(15259, 61, 32588, 13, 40),

        ADZ(13661, 61, 10222, 13, 40),

        bane(45154, 80, 32606, 21, 40),

        Necronium(45154, 70, 32606, 21, 40),
        elderrune(45642, 90, 32611, 25, 40),

        song(44834, 92, 32618, 30, 50);


        private final int pickAxeId;
        private final int levelRequried;
        private final int animationId;
        private final int pickAxeTime;
        private final int orePercentage;


        PickaxeDefinitions(int pickAxeId, int levelRequried, int animationId, int pickAxeTime, int orePercentage) {
            this.pickAxeId = pickAxeId;
            this.levelRequried = levelRequried;
            this.animationId = animationId;
            this.pickAxeTime = pickAxeTime;
            this.orePercentage = orePercentage;
        }

        public int getPickAxeId() {
            return pickAxeId;
        }

        public int getLevelRequried() {
            return levelRequried;
        }

        public int getAnimationId() {
            return animationId;
        }

        public int getPickAxeTime() {
            return pickAxeTime;
        }

        public int orePercentage() {
            return orePercentage;
        }
    }

    protected boolean hasPickaxe(Player player) {
        Item weapon = player.getEquipment().getItem(Equipment.SLOT_WEAPON);
        boolean hasAugmentedTool = weapon != null && weapon.getInventionData() != null && (weapon.getName().toLowerCase().contains("dragon pickaxe") || weapon.getName().toLowerCase().contains("crystal pickaxe"));
        if (!hasAugmentedTool)
            weapon = null;
        if (weapon != null)
            return true;
        int skillChompa = TrapAction.getSkillChompa(player);
        if (skillChompa != -1)
            return true;
        if (player.getInventory().containsOneItem(15259, 1275, 1271, 1273, 1269, 1267, 1265, 13661, 32646, 44834, 45154, 46372, 45642))
            return true;
        int weaponId = player.getEquipment().getWeaponId();
        if (weaponId == -1)
            return false;
        switch (weaponId) {
            case 1265:// Bronze PickAxe
            case 1267:// Iron PickAxe
            case 1269:// Steel PickAxe
            case 1273:// Mithril PickAxe
            case 1271:// Adamant PickAxe
            case 1275:// Rune PickAxe
            case 15259:// Dragon PickAxe
            case 13661: // Inferno adze
            case 44834: // song
            case 45154: // bane
            case 46372: // Necronium
            case 45642: // elder pickaxe
                return true;
            default:
                return false;
        }
    }

    protected boolean setPickaxe(Player player) {
        int level = player.getSkills().getLevel(Skills.MINING);
        int weaponId = player.getEquipment().getWeaponId();
        Item weapon = player.getEquipment().getItem(Equipment.SLOT_WEAPON);
        boolean hasAugmentedTool = weapon != null && weapon.getInventionData() != null && (weapon.getName().toLowerCase().contains("dragon pickaxe") || weapon.getName().toLowerCase().contains("crystal pickaxe"));
        if (!hasAugmentedTool)
            weapon = null;
        if (weapon != null) {
            if (weapon.getName().toLowerCase().contains("dragon pickaxe")) {
                emoteId = 28304;
                pickaxeTime = 13;
            } else {
                emoteId = 28305;
                pickaxeTime = 15;
            }
            return true;
        }




        int skillChompa = TrapAction.getSkillChompa(player);
        if (skillChompa != -1) {
            int requiredLevel = skillChompa == 40995 ? 71 : 31 + (10 * (skillChompa - 31595));
            if (level > requiredLevel) {
                emoteId = 23793;
                pickaxeTime = skillChompa == 40995 ? 15 : skillChompa == 31595 ? 5 : skillChompa == 31596 ? 7 : skillChompa == 31597 ? 9 : 13;
                return true;
            }
            player.sendMessage("You need a Mining level of " + requiredLevel + " to use this skillchompa.");
            return false;
        }
        if (weaponId != -1) {
            switch (weaponId) {
                case 44834: // song
                    if (level >= 92) {
                        emoteId = 32618;
                        pickaxeTime = 30;
                        return true;
                    }
                    break;
                case 45642: // elder pickaxe
                    if (level >= 90) {
                        emoteId = 32611;
                        pickaxeTime = 25;
                        return true;
                    }
                    break;
                case 45154: // bane
                    if (level >= 80) {
                        emoteId = 32606;
                        pickaxeTime = 21;
                        return true;
                    }
                    break;
                case 46372: // Necronium pickaxe
                    if (level >= 70) {
                        emoteId = 32603;
                        pickaxeTime = 14;
                        return true;
                    }
                    break;
            case 32646: // crystal pickaxe
                if (level >= 71) {
                    emoteId = 25062;
                    pickaxeTime = 15;
                    return true;
                }
                break;
            case 15259: // dragon pickaxe
                if (level >= 61) {
                    emoteId = 12190;
                    pickaxeTime = 13;
                    return true;
                }
                break;
            case 13661: // Inferno adze
                if (level >= 61) {
                    emoteId = 10222;
                    pickaxeTime = 13;
                    return true;
                }
                break;
            case 1275: // rune pickaxe
                if (level >= 41) {
                    emoteId = 32566;
                    pickaxeTime = 9;
                    return true;
                }
                break;
            case 1271: // adam pickaxe
                if (level >= 31) {
                    emoteId = 32562;
                    pickaxeTime = 7;
                    return true;
                }
                break;
            case 1273: // mith pickaxe
                if (level >= 21) {
                    emoteId = 32558;
                    pickaxeTime = 5;
                    return true;
                }
                break;
            case 1269: // steel pickaxe
                if (level >= 6) {
                    emoteId = 32552;
                    pickaxeTime = 3;
                    return true;
                }
                break;
            case 1267: // iron pickaxe
                emoteId = 32548;
                pickaxeTime = 2;
                return true;
            case 1265: // bronze pickaxe
                emoteId = 32540;
                pickaxeTime = 1;
                return true;
            }
        }
        if (player.getInventory().containsOneItem(44834)) {
            if (level >= 92) {
                emoteId = 32618;
                pickaxeTime = 30;
                return true;
            }
        }
        if (player.getInventory().containsOneItem(45642)) {
            if (level >= 90) {
                emoteId = 32611;
                pickaxeTime = 25;
                return true;
            }
        }
        if (player.getInventory().containsOneItem(45154)) {
            if (level >= 80) {
                emoteId = 32606;
                pickaxeTime = 21;
                return true;
            }
        }
        if (player.getInventory().containsOneItem(46372)) {
            if (level >= 70) {
                emoteId = 32603;
                pickaxeTime = 14;
                return true;
            }
        }
        if (player.getInventory().containsOneItem(32646)) {
            if (level >= 71) {
                emoteId = 25062;
                pickaxeTime = 15;
                return true;
            }
        }
        if (player.getInventory().containsOneItem(15259)) {
            if (level >= 61) {
                emoteId = 12190;
                pickaxeTime = 13;
                return true;
            }
        }
        if (player.getInventory().containsOneItem(13661)) {
            if (level >= 61) {
                emoteId = 10222;
                pickaxeTime = 13;
                return true;
            }
        }
        if (player.getInventory().containsOneItem(1275)) {
            if (level >= 41) {
                emoteId = 32566;
                pickaxeTime = 9;
                return true;
            }
        }
        if (player.getInventory().containsOneItem(1271)) {
            if (level >= 31) {
                emoteId = 32562;
                pickaxeTime = 7;
                return true;
            }
        }
        if (player.getInventory().containsOneItem(1273)) {
            if (level >= 21) {
                emoteId = 32558;
                pickaxeTime = 5;
                return true;
            }
        }
        if (player.getInventory().containsOneItem(1269)) {
            if (level >= 6) {
                emoteId = 32552;
                pickaxeTime = 3;
                return true;
            }
        }
        if (player.getInventory().containsOneItem(1267)) {
            emoteId = 32548;
            pickaxeTime = 2;
            return true;
        }
        if (player.getInventory().containsOneItem(1265)) {
            emoteId = 32540;
            pickaxeTime = 1;
            return true;
        }
        return false;
    }

    @Override
    public void stop(Player player) {
        player.setNextAnimation(new Animation(-1));
        setActionDelay(player, 3);
    }

    /**
     * Starts/sets the animation and graphics for mining.
     *
     * @param player The player to handle.
     */
    public void setAnimationAndGFX(Player player) {
        int skillChompa = TrapAction.getSkillChompa(player);
        if (skillChompa != -1) {
            player.setNextAnimation(new Animation(emoteId));// 3037
            return;
        }
        if (player.getAnimations().hasChiMining && player.getAnimations().chiMining) {
            player.setNextAnimation(new Animation(17310));
            player.setNextGraphics(new Graphics(3304));
        } else if (player.getAnimations().hasBlastMining && player.getAnimations().blastMining) {
            player.setNextAnimation(new Animation(17947));
            player.setNextGraphics(new Graphics(3918));
        } else if (player.getAnimations().hasStrongMining && player.getAnimations().strongMining) {
            player.setNextAnimation(new Animation(20284));
            player.setNextGraphics(new Graphics(3998));
        } else if (player.getAnimations().hasHeadMining && player.getAnimations().headMining) {
            player.setNextAnimation(new Animation(17083));
        } else if (player.getAnimations().hasSingerMining && player.getAnimations().singerMining) {
            player.setNextAnimation(new Animation(24626));
            player.setNextGraphics(new Graphics(5162));
        } else
            player.setNextAnimation(new Animation(emoteId));
    }
}