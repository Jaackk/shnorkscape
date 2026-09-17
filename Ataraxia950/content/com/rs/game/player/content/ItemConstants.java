package com.rs.game.player.content;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.activites.gim.GIM;
import com.rs.game.activites.gim.bank.GIMBank;
import com.rs.game.activites.quest.deathsbounty.DeathsBounty;
import com.rs.game.activites.quest.root_of_evil.RootOfEvil;
import com.rs.game.item.Item;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.QuestManager.Quests;
import com.rs.game.player.Skills;
import com.rs.game.player.TreasureTrails;
import com.rs.game.player.actions.fletching.quickshaft.QuickShafter;
import com.rs.game.player.actions.invention.InventionData;
import com.rs.game.player.actions.invention.InventionData.Gizmo;
import com.rs.game.player.actions.invention.InventionData.Perk;
import com.rs.game.player.content.distinctioncape.DistinctionCape;
import com.rs.game.player.content.items.Ectoplasmator;
import com.rs.game.player.content.pet.Pets;

public class ItemConstants {

    public static boolean canWear(final Item item, final Player player, final boolean keepSake) {
        final String name = item.getName().toLowerCase();
        if (player.isDev())
            return true;
        switch (item.getId()) {
            case 9925:
            case 9924:
            case 9923:
            case 9922:
            case 9921:
                if (player.isPlatinumDonor()) {
                    return true;
                } else {
                    player.sendMessage("Only <img=21> Platinum donators can wear this!");
                    return false;
                }
        }

        if (name.contains("max cape") || name.contains("max hood") || name.contains("completionist cape")
                || name.contains("Hooded completionist cape") || name.contains("Hooded completionist cape (t)")
                || name.contains("completionist hood") || name.contains("(t)") && name.contains("completionist")
                || name.contains("master quest cape")) {
            if (!DistinctionCape.canWear(player, item)) {
                if (!keepSake) {
                    player.sendMessage("<col=ff0000><shad=000000>You are not worthy enough to wear this!");
                }
                return false;
            }
        }
        if (item.getId() == 41383 && player.getEquipment().cantWearWidowsWail()) { // widows wail
            player.sendMessage("You're too strong to wear this item now!");
            return false;
        }
        if (name.equalsIgnoreCase("christmas scythe") && player.getSkills().getLevel(Skills.ATTACK) < 90) {
            player.sendMessage("You need to have an Attack level of 90 to equip this.");
            return false;
        }
        if (name.contains("balmung") && player.getSkills().getLevel(Skills.ATTACK) < 75) {
            player.sendMessage("You need to have an Attack level of 75 to equip this.");
            return false;
        }
        if (name.contains("ahrim")) {
            if (player.getSkills().getLevel(Skills.MAGIC) < 70) {
                if (!keepSake) {
                    player.sendMessage("You need to have a Magic level of 70 to equip this.");
                }
                return false;
            }
        }
        if (item.getId() >= 29185 && item.getId() <= 29188 || item.getId() == 34292 || item.getId() == 34293) {
            if (player.getSkills().getLevel(Skills.DIVINATION) < 99) {
                if (!keepSake) {
                    player.sendMessage("You need to have a Divination level of 99 to equip this.");
                }
                return false;
            }
        }
        if (item.getId() >= 36351 && item.getId() <= 36355) {
            if (!player.isOwner() && player.getSkills().getLevel(Skills.INVENTION) < 99) {
                player.sendMessage("You are not high enough level to use this item.");
                player.sendMessage("You need to have a level of 99 Invention to equip this.");
                return false;
            }
        }
        if (item.getId() == 15241) {
            if (player.getSkills().getLevel(Skills.RANGE) < 75 && player.getSkills().getLevel(Skills.FIREMAKING) < 61) {
                if (!keepSake) {
                    player.sendMessage("You are not high enough level to use this item.");
                    player.sendMessage("You need to have a level of 75 Ranged and 61 Firemaking to equip this.");
                }
                return false;
            }
        }
        if (item.getId() >= 18349 && item.getId() <= 18363) {
            if (player.getSkills().getLevel(Skills.DUNGEONEERING) < 80) {
                if (!keepSake) {
                    player.sendMessage("You are not high enough level to use this item.");
                    player.sendMessage("You need to have at least level 80 Dungeoneering to equip this.");
                }
                return false;
            }
        }


        if (item.getName().contains("Fire cape")) {
            if (!player.isCompletedFightCaves()) {
                if (!keepSake) {
                    player.sendMessage("You have to complete the Fight Caves minigame to wear this.");
                }
                return false;
            }
        }


        if (item.getId() == 20769 || item.getId() == 20770 || item.getId() == 32152) {
            if (!DistinctionCape.isWorthyCompCape(player)) {
                player.sendMessage("You are not worthy enough to wear this cape.");
                return false;
            }
        }
        if (item.getId() == 20771 || item.getId() == 20772 || item.getId() == 32153) {
            if (!DistinctionCape.isWorthyCompCapeT(player)) {
                player.sendMessage("You are not worthy enough to wear this cape.");
                return false;
            }
        }

        if (item.getId() == 4708 || item.getId() == 4710 || item.getId() == 4712 || item.getId() == 4714) {
            if (player.getSkills().getLevel(Skills.DEFENCE) < 70) {
                if (!keepSake) {
                    player.sendMessage("You are not high enough level to use this item.");
                    player.sendMessage("You need to have 70 Defence to wear this item.");
                }
                return false;
            }
            if (player.getSkills().getLevel(Skills.MAGIC) < 70) {
                if (!keepSake) {
                    player.sendMessage("You are not high enough level to use this item.");
                    player.sendMessage("You need to have 70 Magic to wear this item.");
                }
                return false;
            }
        }
        if (item.getId() == 36019) {
            if (player.getSkills().getLevel(Skills.MAGIC) < 85) {
                if (!keepSake) {
                    player.sendMessage("You are not high enough level to use this item.");
                    player.sendMessage("You need to have 85 Magic to wear this item.");
                }
                return false;
            }
        }
        if (item.getName().toLowerCase().contains("invention master cape")) {
            if (!player.isOwner() && player.getSkills().getLevelForXp(Skills.INVENTION) < 120) {
                if (!keepSake) {
                    player.sendMessage("You are not high enough level to use this item.");
                    player.sendMessage("You need to have 120 Invention to wear this item.");
                }
                return false;
            }
        }
        if (item.getName().toLowerCase().contains("invention cape")) {
            if (!player.isOwner() && player.getSkills().getLevelForXp(Skills.INVENTION) < 99) {
                if (!keepSake) {
                    player.sendMessage("You are not high enough level to use this item.");
                    player.sendMessage("You need to have 99 Invention to wear this item.");
                }
                return false;
            }
        }
        if (item.getId() == 31284) {
            if (player.getSkills().getXp(Skills.DIVINATION) < 104273167) {
                if (!keepSake) {
                    player.sendMessage("You are not high enough level to use this item.");
                    player.sendMessage("You need to have 104,273,167 experience in the skill to wear this!");
                }
                return false;
            }
        }
        if (item.getId() == 31277) {
            if (player.getSkills().getXp(Skills.AGILITY) < 104273167) {
                if (!keepSake) {
                    player.sendMessage("You are not high enough level to use this item.");
                    player.sendMessage("You need to have 104,273,167 experience in the skill to wear this!");
                }
                return false;
            }
        }
        if (item.getId() == 31268) {
            if (player.getSkills().getXp(Skills.ATTACK) < 104273167) {
                if (!keepSake) {
                    player.sendMessage("You are not high enough level to use this item.");
                    player.sendMessage("You need to have 104,273,167 experience in the skill to wear this!");
                }
                return false;
            }
        }
        if (item.getId() == 31275) {
            if (player.getSkills().getXp(Skills.CONSTRUCTION) < 104273167) {
                if (!keepSake) {
                    player.sendMessage("You are not high enough level to use this item.");
                    player.sendMessage("You need to have 104,273,167 experience in the skill to wear this!");
                }
                return false;
            }
        }

        if (item.getId() == 31288) {
            if (player.getSkills().getXp(Skills.COOKING) < 104273167) {
                if (!keepSake) {
                    player.sendMessage("You are not high enough level to use this item.");
                    player.sendMessage("You need to have 104,273,167 experience in the skill to wear this!");
                }
                return false;
            }
        }
        if (item.getId() == 31280) {
            if (player.getSkills().getXp(Skills.CRAFTING) < 104273167) {
                if (!keepSake) {
                    player.sendMessage("You are not high enough level to use this item.");
                    player.sendMessage("You need to have 104,273,167 experience in the skill to wear this!");
                }
                return false;
            }
        }
        if (item.getId() == 31270) {
            if (player.getSkills().getXp(Skills.DEFENCE) < 104273167) {
                if (!keepSake) {
                    player.sendMessage("You are not high enough level to use this item.");
                    player.sendMessage("You need to have 104,273,167 experience in the skill to wear this!");
                }
                return false;
            }
        }
        if (item.getId() == 31291) {
            if (player.getSkills().getXp(Skills.FARMING) < 104273167) {
                if (!keepSake) {
                    player.sendMessage("You are not high enough level to use this item.");
                    player.sendMessage("You need to have 104,273,167 experience in the skill to wear this!");
                }
                return false;
            }
        }
        if (item.getId() == 31289) {
            if (player.getSkills().getXp(Skills.FIREMAKING) < 104273167) {
                if (!keepSake) {
                    player.sendMessage("You are not high enough level to use this item.");
                    player.sendMessage("You need to have 104,273,167 experience in the skill to wear this!");
                }
                return false;
            }
        }
        if (item.getId() == 31287) {
            if (player.getSkills().getXp(Skills.FISHING) < 104273167) {
                if (!keepSake) {
                    player.sendMessage("You are not high enough level to use this item.");
                    player.sendMessage("You need to have 104,273,167 experience in the skill to wear this!");
                }
                return false;
            }
        }
        if (item.getId() == 31281) {
            if (player.getSkills().getXp(Skills.FLETCHING) < 104273167) {
                if (!keepSake) {
                    player.sendMessage("You are not high enough level to use this item.");
                    player.sendMessage("You need to have 104,273,167 experience in the skill to wear this!");
                }
                return false;
            }
        }
        if (item.getId() == 31278) {
            if (player.getSkills().getXp(Skills.HERBLORE) < 104273167) {
                if (!keepSake) {
                    player.sendMessage("You are not high enough level to use this item.");
                    player.sendMessage("You need to have 104,273,167 experience in the skill to wear this!");
                }
                return false;
            }
        }
        if (item.getId() == 31276) {
            if (player.getSkills().getXp(Skills.HITPOINTS) < 104273167) {
                if (!keepSake) {
                    player.sendMessage("You are not high enough level to use this item.");
                    player.sendMessage("You need to have 104,273,167 experience in the skill to wear this!");
                }
                return false;
            }
        }
        if (item.getId() == 31283) {
            if (player.getSkills().getXp(Skills.HUNTER) < 104273167) {
                if (!keepSake) {
                    player.sendMessage("You are not high enough level to use this item.");
                    player.sendMessage("You need to have 104,273,167 experience in the skill to wear this!");
                }
                return false;
            }
        }
        if (item.getId() == 31273) {
            if (player.getSkills().getXp(Skills.MAGIC) < 104273167) {
                if (!keepSake) {
                    player.sendMessage("You are not high enough level to use this item.");
                    player.sendMessage("You need to have 104,273,167 experience in the skill to wear this!");
                }
                return false;
            }
        }
        if (item.getId() == 31285) {
            if (player.getSkills().getXp(Skills.MINING) < 104273167) {
                if (!keepSake) {
                    player.sendMessage("You are not high enough level to use this item.");
                    player.sendMessage("You need to have 104,273,167 experience in the skill to wear this!");
                }
                return false;
            }
        }
        if (item.getId() == 31272) {
            if (player.getSkills().getXp(Skills.PRAYER) < 104273167) {
                if (!keepSake) {
                    player.sendMessage("You are not high enough level to use this item.");
                    player.sendMessage("You need to have 104,273,167 experience in the skill to wear this!");
                }
                return false;
            }
        }
        if (item.getId() == 31271) {
            if (player.getSkills().getXp(Skills.RANGE) < 104273167) {
                if (!keepSake) {
                    player.sendMessage("You are not high enough level to use this item.");
                    player.sendMessage("You need to have 104,273,167 experience in the skill to wear this!");
                }
                return false;
            }
        }
        if (item.getId() == 31274) {

            if (player.getSkills().getXp(Skills.RUNECRAFTING) < 104273167) {
                if (!keepSake) {
                    player.sendMessage("You are not high enough level to use this item.");
                    player.sendMessage("You need to have 104,273,167 experience in the skill to wear this!");
                }
                return false;
            }
        }
        if (item.getId() == 31282) {
            if (player.getSkills().getXp(Skills.SLAYER) < 104273167) {
                if (!keepSake) {
                    player.sendMessage("You are not high enough level to use this item.");
                    player.sendMessage("You need to have 104,273,167 experience in the skill to wear this!");
                }
                return false;
            }
        }
        if (item.getId() == 31286) {
            if (player.getSkills().getXp(Skills.SMITHING) < 104273167) {
                if (!keepSake) {
                    player.sendMessage("You are not high enough level to use this item.");
                    player.sendMessage("You need to have 104,273,167 experience in the skill to wear this!");
                }
                return false;
            }
        }
        if (item.getId() == 31269) {
            if (player.getSkills().getXp(Skills.STRENGTH) < 104273167) {
                if (!keepSake) {
                    player.sendMessage("You are not high enough level to use this item.");
                    player.sendMessage("You need to have 104,273,167 experience in the skill to wear this!");
                }
                return false;
            }
        }
        if (item.getId() == 31292) {
            if (player.getSkills().getXp(Skills.SUMMONING) < 104273167) {
                if (!keepSake) {
                    player.sendMessage("You are not high enough level to use this item.");
                    player.sendMessage("You need to have 104,273,167 experience in the skill to wear this!");
                }
                return false;
            }
        }
        if (item.getId() == 31279) {
            if (player.getSkills().getXp(Skills.THIEVING) < 104273167) {
                if (!keepSake) {
                    player.sendMessage("You are not high enough level to use this item.");
                    player.sendMessage("You need to have 104,273,167 experience in the skill to wear this!");
                }
                return false;
            }
        }
        if (item.getId() == 31290) {
            if (player.getSkills().getXp(Skills.WOODCUTTING) < 104273167) {
                if (!keepSake) {
                    player.sendMessage("You are not high enough level to use this item.");
                    player.sendMessage("You need to have 104,273,167 experience in the skill to wear this!");
                }
                return false;
            }
        }
        if (item.getId() == 13661) {
            if (player.getSkills().getLevel(Skills.FIREMAKING) < 92) {
                if (!keepSake) {
                    player.sendMessage("You are not high enough level to use this item.");
                    player.sendMessage("You need a Firemaking of level 92 to equip this item.");
                }
                return false;
            }
        }
        if (item.getId() == 13659) {
            if (player.getSkills().getLevel(Skills.FIREMAKING) < 62) {
                if (!keepSake) {
                    player.sendMessage("You are not high enough level to use this item.");
                    player.sendMessage("You need a Firemaking of level 62 to equip this item.");
                }
                return false;
            }
        }
        if (item.getId() == 13660) {
            if (player.getSkills().getLevel(Skills.FIREMAKING) < 79) {
                if (!keepSake) {
                    player.sendMessage("You are not high enough level to use this item.");
                    player.sendMessage("You need a Firemaking of level 79 to equip this item.");
                }
                return false;
            }
        } else if (item.getId() == 14642 || item.getId() == 14645 || item.getId() == 15433 || item.getId() == 15435
                || item.getId() == 14641 || item.getId() == 15432 || item.getId() == 15434) {
            if (!player.getQuestManager().completedQuest(Quests.NOMADS_REQUIEM)) {
                if (!keepSake) {
                    player.sendMessage("You need to have completed Nomad's Requiem mini-quest to use this cape.");
                }
                return false;
            }
        }
        return true;
    }

    public static boolean canWear(final Item item, final Player player) {
        return ItemConstants.canWear(item, player, false);
    }

    public static int getDegradeItemWhenWear(final int id) { // returns the untradable/damaged variants of items which degrade as soon as you equip them
        // Pvp armors
        if (id == 13958 || id == 13961 || id == 13964 || id == 13967 || id == 13970 || id == 13973 || id == 13908
                || id == 13911 || id == 13914 || id == 13917 || id == 13920 || id == 13923 || id == 13941 || id == 13944
                || id == 13947 || id == 13950 || id == 13958 || id == 13938 || id == 13926 || id == 13929 || id == 13932
                || id == 13935) {
            return id + 2;
        }
        if (id == 29854 || id == 29857 || id == 29860) {
            return id + 2;
        }
        if (id == 33348 || id == 33351 || id == 33354 || id == 42073 || id == 42076 || id == 42079) {
            return id + 1;
        }
        if (id == 33414 || id == 33417 || id == 33420) {
            return id + 1;
        }
        if (id == 33480 || id == 33483 || id == 33486) {
            return id + 1;
        }
        if (id == 36285 || id == 36288 || id == 36291) {
            return id + 1;
        }
        /** Hydrix jewellery */
        if (id == 31878 || id == 31869 || id == 31875 || id == 31872) {
            return id + 2;
        }
        // Superior PVP Armours
        if (id == 39103 || id == 39111 || id == 39121 || id == 39125 || id == 39099 || id == 39107 || id == 39115 || id == 39117
                || id == 39085 || id == 39089 || id == 39093 || id == 39095 || id == 39103 || id == 39111 || id == 39129 || id == 39133
                || id == 39137 || id == 39139 || id == 39143) {
            return id + 1;
        }
        if (id == 28608 || id == 28611 || id == 28614) {
            return id + 2;
        }
        if (id == 33339 || id == 33342 || id == 33345 || id == 33405 || id == 33408 || id == 33411 || id == 33471 || id == 33474 || id == 33477 || id == 36276 || id == 36279 || id == 36282 || id == 42082 || id == 42085 || id == 42088) {
            return id + 1;
        }

        if (id == 33357 || id == 33360 || id == 33363 || id == 33423 || id == 33426 || id == 33429 || id == 33489 || id == 33492 || id == 33495 || id == 36294 || id == 36297 || id == 36300 || id == 42040 || id == 42043 || id == 42046) {
            return id + 1;
        }
        // t90 armours
        if (id == 29587 || id == 29860 || id == 29854 || id == 28608 || id == 28611 || id == 28614 || id == 30005 || id == 30008 || id == 30011) {
            return id + 2;
        }
        if (id == 39053 || id == 39049 || id == 39057) {
            return id + 2;
        }
        if (id == 39234 || id == 39232 || id == 39236) {
            return id + 1;
        }
        if (id == 31203 || id == 30213) {
            return id + 2;
        }
        if (id >= 26346 && id <= 26348) {
            return id + 3;
        }
        if (id >= 26325 && id <= 26327) {
            return id + 3;
        }
        if (id >= 26337 && id <= 26339) {
            return id + 3;
        }
        return -1;
    }


    public static String isBankAble(Player player, final Item item) {
        if (item.getId() >= 20502 && item.getId() <= 20652) {
            return "You can't bank this item.";
        }
//        if (!item.getDefinitions().canBeBanked())
//            return "A magical force prevents you from banking this item.";
        switch (item.getId()) {
            case RootOfEvil.EVIL_LOGS_ID:
            case RootOfEvil.SELF_DESTRUCT_KEY_ID:
                return "A magical force prevents you from banking this item.";
        }
        if (!player.quests.isCompleted(RootOfEvil.class) && (item.getId() == RootOfEvil.EVIL_WATERING_CAN_ID || item.getId() == RootOfEvil.DIARY_ID)) {
            return "A magical force prevents you from banking this item.";
        }
        if (player.gimBank.isOpen()) {
            return GIMBank.canDeposit(player, item);
        }
        return null;
    }

    public static boolean isTradeable(final Item item) {



        if (item.getDefinitions().isLended()) {
            return false;
        }
        if (item.getAttributes() != null)
            return false;
        if (item.getId() == 41083)
            return false;
        if (item.getId() == GIM.getRewards().getRewardBoxId() ||
                item.getId() == GIM.getRewards().getRoyalLetterId()) {
            return false;
        }
        if (item.getId() == 36390 || item.getId() == 36725 || item.getId() == 36730 || item.getId() == 41073 || item.getId() == 39659)//Invention devices.
            return true;
        if (item.getId() == QuickShafter.ID) {
            return true;
        }
        if (item.getId() == 1842) {
            return false;
        }
        if (item.getId() == 41078) {
            return false;
        }
        if (item.getId() == Ectoplasmator.ATTUNED_ECTOPLASMATOR_ID ||
                item.getId() == Ectoplasmator.ECTOPLASMATOR_ID ||
                item.getId() == Ectoplasmator.DEGRADED_ATTUNED_ECTOPLASMATOR_ID) {
            return false;
        }
        String lowercaseName = item.getName().toLowerCase();
        if (lowercaseName.contains("dragonbane") || item.getId() == 21778) { // dragonbane equipment and bane ore
            return false;
        }
        if (SkillingPets.itemIsSkillingPet(item)) {
            return false;
        }

        switch (item.getId()) {
            case 30368:
            case 39265:
            case 36117:
            case 30380:
            case 36165:
            case 6860:
            case 6861:
            case 36118:
            case 36083:
            case 19325:
            case 36082:
            case 13109:
            case 36084:
            case 36085:
            case 36086:
            case 36087:
            case 33730:
            case 44523:
            case 15422:
            case 15423:
            case 15425:
            case 39390:
            case 6858:
            case 6859:
            case 4079:
            case 10507:
            case 20077:
            case 30395:
            case 30396:
            case 26518:
            case 39275:
            case 15426:
            case 26493:
            case DeathsBounty
                    .SOUL_URN_ID:
            case 4084:
            case 6856:
            case 6857:
            case 11950:
            case 47592:
                return false;
            case 39922:
                return true;
            case 30574: // death lotus dart
            case 6570: // firecape
            case 13531: // lent phat
            case 13532: // lent phat
            case 13533: // lent phat
            case 13534: // lent phat
            case 13535: // lent phat
            case 13536: // lent phat
            case 18363: // farseer shield
            case 18361: // eagle eye
            case 18359: // chaotic kite
            case 6529: // tokkul
            case 14641: // SOUL WARS CAPE
            case 14642: // SOULWARS CAPE
            case 24155: // double spin ticket
            case 24154: // spin ticket
            case 299: // mithril seed
            case 10551: // fighter torso
            case 7462: // barrow gloves
            case 23659: // tokhaar-kal
            case 10887: // barrelchest anchor
            case 15584: // Charming imp
            case 15585: // Coin accumulator
            case 32694: // arcane blood neck
            case 32703: // arcane blood neck
            case 47534:
                return false;
            /*
             * Barrows Degraded Items
             */
            case 4880:
            case 4881:
            case 4882:
            case 4883:
            case 4886:
            case 4887:
            case 4888:
            case 4889:
            case 4898:
            case 4899:
            case 4900:
            case 4901:
            case 4856:
            case 4857:
            case 4858:
            case 4859:
            case 4862:
            case 4863:
            case 4864:
            case 4865:
            case 4868:
            case 4869:
            case 4870:
            case 4871:
            case 4874:
            case 4875:
            case 4876:
            case 4877:
            case 4904:
            case 4905:
            case 4906:
            case 4907:
            case 4910:
            case 4911:
            case 4912:
            case 4913:
            case 4916:
            case 4917:
            case 4918:
            case 4920:
            case 4922:
            case 4923:
            case 4924:
            case 4925:
            case 4928:
            case 4929:
            case 4930:
            case 4931:
            case 4934:
            case 4935:
            case 4936:
            case 4937:
            case 4940:
            case 4941:
            case 4942:
            case 4943:
            case 4946:
            case 4947:
            case 4948:
            case 4949:
            case 4958:
            case 4959:
            case 4960:
            case 4961:
            case 4964:
            case 4965:
            case 4966:
            case 4967:
            case 4970:
            case 4971:
            case 4972:
            case 4973:
            case 4976:
            case 4892:
            case 4977:
            case 4978:
            case 4979:
            case 4982:
            case 4983:
            case 4984:
            case 4985:
            case 4988:
            case 4989:
            case 4990:
            case 4991:
            case 4994:
            case 4995:
            case 4996:
            case 4997:
                return false;
        }

        if (item.getId() == 41956)
            return true;

        if (item.getName().startsWith("Augmented ")) {
            return false;
        }

        // voting token
        if (item.getId() == 41418)
            return true;

        if (item.getName().endsWith(" (broken)")) {
            return false;
        }
        if (item.getName().equalsIgnoreCase("Silverhawk feathers")) {
            return true;
        }
        if (item.getId() == 34027 || item.getId() == 36164) {
            return true;
        }

        if (item.getId() == 1050) {
            return true;
        }

        if (item.getId() == 41430) {
            return true;
        }
        if (item.getId() == 30372) {
            return true;
        }
        if (item.getId() >= 20502 && item.getId() <= 20652) {
            return false;
        }
        if (item.getId() >= 32948 && item.getId() <= 33259) {
            return false;
        }
        if (item.getId() >= 28600 && item.getId() <= 28606) {
            return false;
        }
        if (lowercaseName.contains("void")) {
            return false;
        }
        if (item.getId() == 31189 || item.getId() == 31191) {
            return true;
        }
        if (item.getName().equals("Crystal bow") || item.getName().equals("Crystal dagger")
                || item.getName().equals("Crystal halberd") || item.getName().contains("weapon seed")
                || item.getName().contains("armour seed") || item.getName().equals("Crystal chakram")
                || item.getName().equals("Crystal staff") || item.getName().equals("Crystal wand")
                || item.getName().equals("Crystal shield") || item.getName().equals("Crystal ward")
                || item.getName().equals("Crystal deflector") || lowercaseName.contains("ethereal")
                || lowercaseName.contains("golem") || lowercaseName.contains("shark")
                || lowercaseName.contains("sentinel")
                || lowercaseName.contains("elder divi") || lowercaseName.contains("camouflage")
                || lowercaseName.contains("perk") || lowercaseName.contains("deathtouch b")
                || lowercaseName.contains("amulet of sou") || lowercaseName.contains("reaper neck")
                || lowercaseName.contains("ring of death") || lowercaseName.contains("khopesh")
                || lowercaseName.contains("trailblazer") || lowercaseName.contains("superior")) {
            return true;
        }


        if (lowercaseName.contains("tion (i)"))
            return false;
        else if (lowercaseName.equals("decimation") || lowercaseName.equals("obliteration") || lowercaseName.equals("annihilation"))
            return true;
        if (lowercaseName.contains("'perfect'") || lowercaseName.contains("brawler's") || lowercaseName.contains("farsight")
                || lowercaseName.contains("blast n") || lowercaseName.contains("arcane str") || lowercaseName.contains("arcane pul")
                || lowercaseName.contains("mammoth pl") || lowercaseName.contains("snowverload") || lowercaseName.contains("penguin p")
                || lowercaseName.contains("skeleton") || lowercaseName.contains("diamond mys") || lowercaseName.contains("master mys")
                || lowercaseName.contains("silver mys") || lowercaseName.contains("gold mys") || lowercaseName.contains("platinum mys")
                || lowercaseName.contains("pumpkin") || lowercaseName.contains("deathcon") || lowercaseName.contains("warlock")
                || lowercaseName.contains("witch") || lowercaseName.contains("top hat") || lowercaseName.contains("mime")
                || lowercaseName.contains("leprechaun") || lowercaseName.contains("yak scythe") || lowercaseName.contains("death's scythe")
                || lowercaseName.contains("golden scythe") || lowercaseName.contains("obliteration (i)")
                || lowercaseName.contains("annihilation (i)") || lowercaseName.contains("corruption sigil") || lowercaseName.startsWith("sayln")
                || lowercaseName.contains("primal") || lowercaseName.contains("sagitt") || lowercaseName.contains("celestial")) {
            return false;
        }
        if (item.getDefinitions().isDestroyItem() && !lowercaseName.contains("ethereal") && !lowercaseName.contains("morrigan") &&
                !lowercaseName.contains("zuriel") && !lowercaseName.contains("vesta") && !lowercaseName.contains("statius")
                && !(item.getId() >= 35963 && item.getId() <= 35982)) {
            return false;
        }
        for (final int id : TreasureTrails.CLUE_SCROLLS) {
            if (item.getId() == id) {
                return false;
            }
        }
        for (final int id : TreasureTrails.SCROLL_BOXES) {
            if (item.getId() == id) {
                return false;
            }
        }
        for (final int id : TreasureTrails.PUZZLES) {
            if (item.getId() == id) {
                return false;
            }
        }
        final Pets pets = Pets.forId(item.getId());
        if (pets != null) {
            return false;
        }
        if (lowercaseName.contains("aura") || lowercaseName.contains("master quest") || lowercaseName.contains("completionist")
                || lowercaseName.contains("charm") || lowercaseName.contains("clue") || lowercaseName.contains("flarefrost")
                || lowercaseName.contains("dice") || lowercaseName.contains("hatchling") || lowercaseName.contains("max")
                || lowercaseName.contains("effigi") || lowercaseName.contains("lamp") || lowercaseName.contains("recover special")
                || lowercaseName.contains("lucky") || lowercaseName.contains("flaming") || lowercaseName.contains("chaotic")
                || lowercaseName.contains("clue") || lowercaseName.contains("tzrek") || lowercaseName.contains("katana")
                || lowercaseName.contains("ring of vigour") || lowercaseName.contains("bonecrush") || lowercaseName.contains("flaming skull")
                || lowercaseName.contains("veteran") || lowercaseName.equalsIgnoreCase("cannon base") || lowercaseName.contains("herbicid")
                || lowercaseName.equalsIgnoreCase("cannon stand") || lowercaseName.contains(" 25")
                || lowercaseName.equalsIgnoreCase("cannon barrels") || lowercaseName.contains(" 0")
                || lowercaseName.equalsIgnoreCase("cannon furnace") || lowercaseName.contains("slayer") || lowercaseName.contains("deathtouched")
                || lowercaseName.contains(" (deg)") || lowercaseName.contains(" cape (t)")
                || lowercaseName.equalsIgnoreCase("master cape") || lowercaseName.contains("(blood") || lowercaseName.contains("arcane stream")
                || lowercaseName.contains(" 100") || lowercaseName.contains(" 75") || lowercaseName.contains("(i)")
                || lowercaseName.contains("amulet of souls") || lowercaseName.contains("(barrows") || lowercaseName.contains("(shadow")
                || lowercaseName.contains("(third age") || lowercaseName.contains("(ice") || lowercaseName.contains("fist of guthix") || lowercaseName.contains(" 50")
                || lowercaseName.contains("fire cape") || lowercaseName.contains("tokhaar") || lowercaseName.contains("defender")
                || lowercaseName.contains("extreme") || lowercaseName.contains("ancient weapon") || lowercaseName.contains("ring of death")
                || lowercaseName.contains("emberkeen") || lowercaseName.contains("hailfire") || lowercaseName.contains("(worn")
                || lowercaseName.contains("kiba") || lowercaseName.contains("makigai")
                || lowercaseName.contains("tetsu wakizashi") || lowercaseName.contains("sack of eff") || lowercaseName.contains("teddy")
                || lowercaseName.contains("vyrewatch") || lowercaseName.contains("crabclaw") || lowercaseName.contains("diving app")
                || lowercaseName.contains("pumpkin") || lowercaseName.contains("urchin") || lowercaseName.contains("fishbowl h")
                || lowercaseName.contains("flaming sk") || lowercaseName.contains("reaver") || lowercaseName.contains("ancient emblem")
                || lowercaseName.contains("perfect chitin") || lowercaseName.contains("deathtouch") || lowercaseName.contains("reaper") ||
                /** Christmas stuff */
                (lowercaseName.contains("christmas ") && !lowercaseName.contains("cracker")) || lowercaseName.contains("penguin")
                || lowercaseName.contains("prismatic dye") || lowercaseName.contains("sparkles") || lowercaseName.contains("snowball")
                || lowercaseName.contains("carrot") || lowercaseName.contains("yo-yo") || lowercaseName.contains("reindeer") ||
                /** New Rare item shop rotation */
                lowercaseName.contains("pogo") || lowercaseName.contains("flaming skull") || lowercaseName.contains("faithful shield")
                || lowercaseName.contains("sled") || lowercaseName.contains("corgi") || lowercaseName.contains("hype train")
                || lowercaseName.contains("spawnling") || lowercaseName.contains("golden chin") || lowercaseName.contains("kharidian")
                || lowercaseName.contains("lion cub") || lowercaseName.contains("ring of wealth (c)") || lowercaseName.contains("soul reaper")
                || lowercaseName.contains("tortured soul") || lowercaseName.contains("ataraxia start") || lowercaseName.contains("training") ||
                lowercaseName.contains("widow") || lowercaseName.contains("lunarfury") || lowercaseName.contains("golden ticket") || lowercaseName.contains("dreadnip")) {
            return false;
        }
        return !lowercaseName.startsWith("vanguard") && !lowercaseName.startsWith("trickster") && !lowercaseName.startsWith("battle-mage");
    }

    public static boolean turnCoins(final Item item) {
        String lowercaseName = item.getDefinitions().getName();
        if (lowercaseName.contains("(deg)")) {
            return true;
        }
        if (lowercaseName.contains("strength cape")) {
            return true;
        }
        if (lowercaseName.contains("max cape")) {
            return true;
        }
        if (lowercaseName.contains("max hood")) {
            return true;
        }
        if (lowercaseName.contains("completionist cape")) {
            return true;
        }
        if (lowercaseName.contains("completionist hood")) {
            return true;
        }
        switch (item.getId()) {
            case 10887:
            case 7462:
            case 7461:
            case 18349:
            case 18351:
            case 18353:
            case 18355:
            case 18357:
            case 18359:
            case 18361:
            case 18363:
            case 18335:
            case 18334:
            case 18333:
            case 31205:
            case 30215:
                return true;
            default:
                return false;
        }
    }


    public static final boolean isKeptOnDeath(final Item item) {
        final String name = item.getName().toLowerCase();
        if (name.contains("tokhaar-kal-")) {
            return true;
        } else if (name.contains("fire cape")) {
            return true;
        } else if (item.getDefinitions().containsOption("Time remaining")) {
            return true;
        } else return name.contains("void");
    }

    /**
     * Default items kept on death.
     *
     * @param item The item.
     * @return true if kept.
     */
    public static boolean keptOnDeath(final Item item) {
        if (item.getDefinitions().isLended()) {
            return true;
        }
        if (item.getId() == 19888) {
            return true;
        }
        if (item.getId() == 18839) {
            return true;
        }
        // needs to be re-written in the future
        if (!isTradeable(item)) {
            return true;
        }
        if (item.getDefinitions().getName().toLowerCase().contains("sneak")) {
            return true;
        }
        if (item.getDefinitions().getName().toLowerCase().contains("culinaromancer")) {
            return true;
        }
        if (item.getDefinitions().getName().toLowerCase().contains(" charm")) {
            return true;
        }
        if (item.getDefinitions().getName().toLowerCase().contains("sneak")) {
            return true;
        }
        if (item.getDefinitions().getName().toLowerCase().contains("overload")) {
            return true;
        }
        if (item.getDefinitions().getName().toLowerCase().contains("slayer ")) {
            return true;
        }
        switch (item.getId()) {
            case 22899:
            case 22901:
            case 22904:
            case 23876:
            case 22905:
            case 22907:
            case 22909:
            case 23874:
            case 23848:
            case 23850:
            case 23852:
            case 23854:
            case 23856:
            case 23862:
            case 22897:
            case 23866:
            case 23864:
            case 22298:
            case 22300:
            case 23860:
            case 23868:
            case 23858:
            case 2412: // god cape
            case 2413:
            case 2414:
            case 23659:
            case 6570:
            case 23660:
            case 18346:
            case 18335:
            case 10551:
            case 10548:
            case 20072:
            case 8850:
            case 8849:
            case 8848:
            case 8847:
            case 3839:
            case 3840:
            case 3841:
            case 3842:
            case 3843:
            case 3844:
            case 8842:
            case 11663:
            case 11664:
            case 11665:
            case 8839:
            case 8840:
            case 6665:
            case 6666:
                return true;
            default:
                return false;
        }
    }

    public static double getAugmentedItemDrainRate(Player player, Item item) {
        if (item == null)
            return 0;
        ItemDefinitions defs = item.getDefinitions();
        if (defs == null || defs.usesChargesInside())
            return 0;
        String name = defs.getName().toLowerCase();
        boolean tool = name.contains("augmented dragon hatchet") || name.contains("augmented dragon pickaxe")
                || name.contains("augmented crystal pickaxe") || name.contains("augmented crystal hatchet")
                || name.contains("augmented crystal fishing rod") || name.contains("augmented crystal tinderbox")
                || name.contains("augmented crystal hammer") || name.contains("augmented tavia's fishing rod")
                || name.contains("fishing rod-o-matic") || name.contains("pyro-matic") || name.contains("hammer-tron");
        boolean twoHanded = Equipment.isTwoHandedWeapon(item);
        boolean hasInventionCape = player.hasSkillCapePerk(Skills.INVENTION);
        int tier = defs.getCSOpcode(750) < 70 ? 67 : defs.getCSOpcode(750);
        int itemLevel = InventionData.getItemLevel(player, item);
        double s = twoHanded ? 1.5 : defs.getEquipSlot() == Equipment.SLOT_SHIELD ? 0.5 : tool ? 0.25 : 1;
        double r = player.getInventionManager().getDrainReductionModifier();
        double l = itemLevel >= 18 ? 0.85 : itemLevel >= 14 ? 0.875 : itemLevel >= 5 ? 0.9 : 1;
        Perk eff = null;
        if (item.getInventionData() != null)
            for (Gizmo gizmo : item.getInventionData().getGizmos()) {
                if (gizmo != null)
                    for (Perk perk : gizmo.getPerks()) {
                        if (perk == null)
                            continue;
                        if (perk.getId() == 64
                                || (eff != null && eff.getId() == perk.getId() && eff.getRank() < perk.getRank())) {
                            eff = perk;
                        }
                        if ((eff == null || (eff.getId() == perk.getId() && eff.getRank() < perk.getRank()))
                                && (perk.getId() == 28))
                            eff = perk;
                    }
            }
        double e = 1 - (eff == null ? 0 : eff.getId() == 64 ? (0.09 * eff.getRank()) : (0.06 * eff.getRank()));
        double p = hasInventionCape ? 0.98 : 1;

        double drainRate = (((double) tier - 60.00) / 8.00) * s * r * l * e * p;
        if (player.isGroupIronman() || player.isIronMan() || player.isHCIronMan() || player.isNoviceIronMan() || player.isExpertIronMan() || player.isIntermediateIronMan())
            drainRate *= 0.75;
        return drainRate;
    }

    public static int getActualCapeId(int retroCapeId) {
        switch (retroCapeId) {
            case 36791:
                return 36352;
            case 34655:
                return 29185;
            case 36790:
                return 36353;
            case 34657:
                return 29186;
            case 36792:
                return 36354;
            case 34540:
                return 9747;
            case 34542:
                return 9748;
            case 34545:
                return 9750;
            case 34547:
                return 9751;
            case 34550:
                return 9753;
            case 34552:
                return 9754;
            case 34555:
                return 9756;
            case 34557:
                return 9757;
            case 34560:
                return 9759;
            case 34562:
                return 9760;
            case 34565:
                return 9762;
            case 34567:
                return 9763;
            case 34570:
                return 9765;
            case 34572:
                return 9766;
            case 34580:
                return 9768;
            case 34582:
                return 9769;
            case 34585:
                return 9771;
            case 34587:
                return 9772;
            case 34590:
                return 9774;
            case 34592:
                return 9775;
            case 34595:
                return 9777;
            case 34597:
                return 9778;
            case 34600:
                return 9780;
            case 34602:
                return 9781;
            case 34605:
                return 9783;
            case 34607:
                return 9784;
            case 34610:
                return 9786;
            case 34612:
                return 9787;
            case 34615:
                return 9789;
            case 34617:
                return 9790;
            case 34620:
                return 9792;
            case 34622:
                return 9793;
            case 34625:
                return 9795;
            case 34627:
                return 9796;
            case 34630:
                return 9798;
            case 34632:
                return 9799;
            case 34635:
                return 9801;
            case 34637:
                return 9802;
            case 34640:
                return 9804;
            case 34660:
                return 18508;
            case 34642:
                return 9805;
            case 34662:
                return 18509;
            case 34645:
                return 9807;
            case 34647:
                return 9808;
            case 34650:
                return 9810;
            case 34652:
                return 9811;
            case 34665:
                return 12169;
            case 34667:
                return 12170;
            case 34541:
                return 34246;
            case 34543:
                return 34247;
            case 34546:
                return 34248;
            case 34548:
                return 34249;
            case 34551:
                return 34250;
            case 34553:
                return 34251;
            case 34561:
                return 34254;
            case 34563:
                return 34255;
            case 34566:
                return 34256;
            case 34568:
                return 34257;
            case 34571:
                return 34258;
            case 34573:
                return 34259;
            case 34576:
                return 34260;
            case 34578:
                return 34261;
            case 34581:
                return 34262;
            case 34583:
                return 34263;
            case 34586:
                return 34264;
            case 34588:
                return 34265;
            case 34591:
                return 34266;
            case 34593:
                return 34267;
            case 34575:
                return 9948;
            case 34596:
                return 34268;
            case 34577:
                return 9949;
            case 34598:
                return 34269;
            case 34601:
                return 34270;
            case 34603:
                return 34271;
            case 34606:
                return 34272;
            case 34608:
                return 34273;
            case 34611:
                return 34274;
            case 34613:
                return 34275;
            case 34616:
                return 34276;
            case 34618:
                return 34277;
            case 34621:
                return 34278;
            case 34623:
                return 34279;
            case 34626:
                return 34280;
            case 34628:
                return 34281;
            case 34631:
                return 34282;
            case 34633:
                return 34283;
            case 34636:
                return 34284;
            case 34638:
                return 34285;
            case 34641:
                return 34286;
            case 34643:
                return 34287;
            case 34646:
                return 34288;
            case 34648:
                return 34289;
            case 34651:
                return 34290;
            case 34653:
                return 34291;
            case 34656:
                return 34292;
            case 34658:
                return 34293;
            case 34661:
                return 34294;
            case 34663:
                return 34295;
            case 34666:
                return 34296;
            case 34668:
                return 34297;
            case 34556:
                return 34556;
            case 34558:
                return 34557;
            case 36789:
                return 36351;
        }
        return -1;
    }
}
