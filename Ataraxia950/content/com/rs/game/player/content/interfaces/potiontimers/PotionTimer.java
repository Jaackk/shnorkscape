package com.rs.game.player.content.interfaces.potiontimers;

import com.rs.game.player.Player;
import com.rs.game.player.content.Pots;
import com.rs.utils.Utils;

/**
 * ataraxia-server
 * paolo 28/07/2019
 * #Shnek6969
 */
public enum PotionTimer {

    ANTI_POISON() {
        @Override
        public long getTime(Player player) {
            return player.getPoisonImmune();
        }

        @Override
        public int getDisplayItem(Player player) {
            return 2446;
        }
    },
    ANTIFIRE() {
        @Override
        public long getTime(Player player) {
            return player.getFireImmune();
        }

        @Override
        public int getDisplayItem(Player player) {
            return 2452;
        }
    },
    SUPER_ANTIFIRE() {
        @Override
        public long getTime(Player player) {
            return player.getSuperAntiFire();
        }

        @Override
        public int getDisplayItem(Player player) {
            return 15304;
        }
    },
    OVERLOAD() {
        @Override
        public long getTime(Player player) {
            return (player.getOverloadDelay() * 600) + Utils.currentTimeMillis();
        }

        @Override
        public int getDisplayItem(Player player) {
            return 15332;
        }
    },
    HOLY_OVERLOAD() {
        @Override
        public long getTime(Player player) {
            return (player.getOverloadDelay() * 600) + Utils.currentTimeMillis();
        }

        @Override
        public int getDisplayItem(Player player) {
            return 33246;
        }
    },
    SEARING_OVERLOAD() {
        @Override
        public long getTime(Player player) {
            return (player.getSupremeOverloadDelay() * 600) + Utils.currentTimeMillis();
        }

        @Override
        public int getDisplayItem(Player player) {
            return 33258;
        }
    },
    OVERLOAD_SALVE() {
        @Override
        public long getTime(Player player) {
            return (player.getSupremeOverloadDelay() * 600) + Utils.currentTimeMillis();
        }

        @Override
        public int getDisplayItem(Player player) {
            return 33222;
        }
    },
    SUPREME_OVERLOAD() {
        @Override
        public long getTime(Player player) {
            return (player.getSupremeOverloadDelay() * 600) + Utils.currentTimeMillis();
        }

        @Override
        public int getDisplayItem(Player player) {
            return 33210;
        }
    },
    SUPREME_OVERLOAD_SALVE() {
        @Override
        public long getTime(Player player) {
            return (player.getSupremeOverloadDelay() * 600) + Utils.currentTimeMillis();
        }

        @Override
        public int getDisplayItem(Player player) {
            return 33222;
        }
    },
    AGGRESSION_POTION() {
        @Override
        public long getTime(Player player) {
            return (player.getAggressiveDelay() * 600) + Utils.currentTimeMillis();
        }

        @Override
        public int getDisplayItem(Player player) {
            return 37971;
        }
    },
    PRAYER_RENEWAL() {
        @Override
        public long getTime(Player player) {
            return (player.getPrayerRenewalDelay() * 600) + Utils.currentTimeMillis();
        }

        @Override
        public int getDisplayItem(Player player) {
            return 21630;
        }
    },
    BLOOD_SERUM() {
        @Override
        public long getTime(Player player) {
            return (player.bloodSerumDelay * 600) + Utils.currentTimeMillis();
        }

        @Override
        public int getDisplayItem(Player player) {
            return 11509;
        }
    },
    NATURES_ESSENCE() {
        @Override
        public long getTime(Player player) {
            return (player.naturesEssenceDelay * 600) + Utils.currentTimeMillis();
        }

        @Override
        public int getDisplayItem(Player player) {
            return 11433;
        }
    },
    BONFIRE() {
        @Override
        public long getTime(Player player) {
            return player.getLastBonfire();
        }

        @Override
        public int getDisplayItem(Player player) {
            return 27984;
        }
    },
    AURA() {
        @Override
        public long getTime(Player player) {
            return 0;
        }

        @Override
        public int getDisplayItem(Player player) {
            return 0;
        }
    },
    PERFECT_WOODCUTTING_JUJU() {
        @Override
        public long getTime(Player player) {
            return -1;
        }

        @Override
        public int getDisplayItem(Player player) {
            return Pots.Pot.PERFECT_WOODCUTTING_JUJU.id[0];
        }
    },
    PERFECT_FARMING_JUJU() {
        @Override
        public long getTime(Player player) {
            return -1;
        }

        @Override
        public int getDisplayItem(Player player) {
            return Pots.Pot.PERFECT_FARMING_JUJU.id[0];
        }
    },
    PERFECT_MINING_JUJU() {
        @Override
        public long getTime(Player player) {
            return -1;
        }


        @Override
        public int getDisplayItem(Player player) {
            return Pots.Pot.PERFECT_MINING_JUJU.id[0];
        }
    },
    PERFECT_SMITHING_JUJU() {
        @Override
        public long getTime(Player player) {
            return -1;
        }


        @Override
        public int getDisplayItem(Player player) {
            return Pots.Pot.PERFECT_SMITHING_JUJU.id[0];
        }
    },
    PERFECT_AGILITY_JUJU() {
        @Override
        public long getTime(Player player) {
            return -1;
        }


        @Override
        public int getDisplayItem(Player player) {
            return Pots.Pot.PERFECT_AGILITY_JUJU.id[0];
        }
    },
    PERFECT_PRAYER_JUJU() {
        @Override
        public long getTime(Player player) {
            return -1;
        }


        @Override
        public int getDisplayItem(Player player) {
            return Pots.Pot.PERFECT_PRAYER_JUJU.id[0];
        }
    },
    PERFECT_HERBLORE_JUJU() {
        @Override
        public long getTime(Player player) {
            return -1;
        }


        @Override
        public int getDisplayItem(Player player) {
            return Pots.Pot.PERFECT_HERBLORE_JUJU.id[0];
        }
    },
    PERFECT_DUNGEONEERING_JUJU() {
        @Override
        public long getTime(Player player) {
            return -1;
        }


        @Override
        public int getDisplayItem(Player player) {
            return Pots.Pot.PERFECT_DUNGEONEERING_JUJU.id[0];
        }
    },
    PERFECT_FISHING_JUJU() {
        @Override
        public long getTime(Player player) {
            return -1;
        }

        @Override
        public int getDisplayItem(Player player) {
            return Pots.Pot.PERFECT_FISHING_JUJU.id[0];
        }
    },
    HUNTER_JUJU() {
        @Override
        public long getTime(Player player) {
            return -1;
        }

        @Override
        public int getDisplayItem(Player player) {
            return Pots.Pot.HUNTER_JUJU.id[0];
        }
    },
    SCENTLESS_JUJU() {
        @Override
        public long getTime(Player player) {
            return -1;
        }

        @Override
        public int getDisplayItem(Player player) {
            return Pots.Pot.SCENTLESS_JUJU.id[0];
        }
    },
    FARMING_JUJU() {
        @Override
        public long getTime(Player player) {
            return -1;
        }

        @Override
        public int getDisplayItem(Player player) {
            return Pots.Pot.FARMING_JUJU.id[0];
        }
    },
    COOKING_JUJU() {
        @Override
        public long getTime(Player player) {
            return -1;
        }

        @Override
        public int getDisplayItem(Player player) {
            return Pots.Pot.COOKING_JUJU.id[0];
        }
    },
    FISHING_JUJU() {
        @Override
        public long getTime(Player player) {
            return -1;
        }

        @Override
        public int getDisplayItem(Player player) {
            return Pots.Pot.FISHING_JUJU.id[0];
        }
    },
    WOODCUTTING_JUJU() {
        @Override
        public long getTime(Player player) {
            return -1;
        }

        @Override
        public int getDisplayItem(Player player) {
            return Pots.Pot.WOODCUTTING_JUJU.id[0];
        }
    },
    MINING_JUJU() {
        @Override
        public long getTime(Player player) {
            return -1;
        }

        @Override
        public int getDisplayItem(Player player) {
            return Pots.Pot.MINING_JUJU.id[0];
        }
    },
    SARADOMINS_BLESSING_JUJU() {
        @Override
        public long getTime(Player player) {
            return -1;
        }

        @Override
        public int getDisplayItem(Player player) {
            return Pots.Pot.SARADOMINS_BLESSING_JUJU.id[0];
        }
    },
    GUTHIXS_GIFT_JUJU() {
        @Override
        public long getTime(Player player) {
            return -1;
        }

        @Override
        public int getDisplayItem(Player player) {
            return Pots.Pot.GUTHIXS_GIFT_JUJU.id[0];
        }
    },
    ZAMORAKS_FAVOUR_JUJU() {
        @Override
        public long getTime(Player player) {
            return -1;
        }

        @Override
        public int getDisplayItem(Player player) {
            return Pots.Pot.ZAMORAKS_FAVOUR_JUJU.id[0];
        }
    };
    public abstract long getTime(Player player);

    public abstract int getDisplayItem(Player player);


    PotionTimer() {
    }

    public static PotionTimer getTimerByItemId(int itemId, Player player) {
        for (PotionTimer value : PotionTimer.values()) {
            if (itemId == value.getDisplayItem(player)) {
                return value;
            }
        }
        return null;
    }

}
