package com.rs.utils.data.parsers.misc.pojos;

import com.rs.game.player.Player;

public enum ShopCurrency {
    GP(995),
    DUNGEONEERING_TOKENS {
        @Override
        public int getAmountOfCurrency(Player player) {
            return player.getDungeoneeringManager().getTokens();
        }

        @Override
        public void removeCurrency(Player player, int amount) {
            player.setDungeoneeringTokens(player.getDungeoneeringManager().getTokens() - amount);
        }
    },
    VOTE_POINTS {
        @Override
        public int getAmountOfCurrency(Player player) {
            return player.getVotePoints();
        }

        @Override
        public void removeCurrency(Player player, int amount) {
            player.setVotePoints(player.getVotePoints() - amount);
        }
    },
    PK_POINTS {
        @Override
        public int getAmountOfCurrency(Player player) {
            return player.getPkPoints();
        }

        @Override
        public void removeCurrency(Player player, int amount) {
            player.setPkPoints(player.getPkPoints() - amount);
        }
    },
    SNOW_ENERGY {
        @Override
        public int getAmountOfCurrency(Player player) {
            return player.getXmas().snowEnergy;
        }

        @Override
        public void removeCurrency(Player player, int amount) {
            player.getXmas().snowEnergy -= amount;
        }
    },
    LOYALTY_POINTS {
        @Override
        public int getAmountOfCurrency(Player player) {
            return player.getLoyaltyPoints();
        }

        @Override
        public void removeCurrency(Player player, int amount) {
            player.setLoyaltyPoints(player.getLoyaltyPoints() - amount);
        }
    },
    TRIVIA_POINTS {
        @Override
        public int getAmountOfCurrency(Player player) {
            return player.getTriviaPoints();
        }

        @Override
        public void removeCurrency(Player player, int amount) {
            player.setTriviaPoints(player.getTriviaPoints() - amount);
        }
    },
    SKILLING_TICKETS(39922),
    SKILLING_POINTS(39922),
    REAPER_POINTS {
        @Override
        public int getAmountOfCurrency(Player player) {
            return player.getReaperPoints();
        }

        @Override
        public void removeCurrency(Player player, int amount) {
            player.setReaperPoints(player.getReaperPoints() - amount);
        }
    },





    RUSTY_COINS(18201),

    RARE_ITEM_TOKENS(34027),
    PUMPKINS(1960),
    CHIMES(37753),
    URCHIN(35723),
    TORTURED_SOUL(32707),
    ATARAXIA_DOLLARS(41430),
    ATARAXIA_DOLLARS_VOTE(48792),
    BLACK_COIN(42311),
    HYBRID_TOKENS(48498),
    RUNESPAN_POINTS {
    	
    	@Override
    	public int getAmountOfCurrency(Player player) {
    		return player.getRuneSpanPoints();
    	}
    	
    	@Override
    	public void removeCurrency(Player player, int amount) {
    		player.setRuneSpanPoint(player.getRuneSpanPoints() - amount);
    	}
    	
    };

    private final int itemId;

    /**
     * Constructor for points
     */
    ShopCurrency() {
        itemId = -1;
    }

    ShopCurrency(int itemId) {
        this.itemId = itemId;
    }

    @Override
    public String toString() {
        return name().toLowerCase().replace("_", " ");
    }

    public int getItemId() {
        return itemId;
    }

    public int getAmountOfCurrency(Player player) {
        return player.getInventory().getAmountOf(getItemId());
    }

    public void removeCurrency(Player player, int amount) {
        player.getInventory().deleteItem(getItemId(), amount, true);
    }
}
