package com.rs.game.activites.lastmanstanding;

import com.rs.game.item.Item;

public enum LastManStandingRewardType {
    COMMON(0, 50, 
    			new LMSReward("5 Tortured Souls!", new Item(32707, 5)),
    			new LMSReward("5 Medium Bonus XP Lamps", 28905, 5),
    			new LMSReward("3 Small Protean Packs!", new Item(34023, 3)),
    			new LMSReward(11694, 1),
				new LMSReward("50m Coins!", new Item(995, 50000000)),
    			new LMSReward("Dragon Claws", new Item(14484), new Item(25555)), new LMSReward(21793, 1),
				new LMSReward(21787, 1),
    			new LMSReward(21790, 1),
				new LMSReward("Golden Dharok's Set", new Item(35473), new Item(35476), new Item(35479), new Item(35482)),
    			new LMSReward("Golden Ahrim's Set", new Item(35461), new Item(35464), new Item(35467), new Item(35470)),
    			new LMSReward("Golden Guthan's Set", new Item(35485), new Item(35488), new Item(35491), new Item(35494)),
    			new LMSReward("Golden Torag's Set", new Item(35509), new Item(35512), new Item(35515), new Item(35518)),
    			new LMSReward("Golden Karil's Set", new Item(35497), new Item(35500), new Item(35503), new Item(35506)),
    			new LMSReward("Golden Verac's Set", new Item(35521), new Item(35524), new Item(35527), new Item(35530)),
                new LMSReward("5 Aggression Potions", new Item(37972, 5)),
                new LMSReward("2 Supply Caches", new Item(35886, 2))),
    RARE(51, 85, 
    			new LMSReward("100m Coins!", 995, 100000000),
    			new LMSReward("5 Large Bonus XP Lamps", 28906, 5),
    			new LMSReward("3 Rare Item Tokens", 34027, 3),
    			new LMSReward("3 Medium Protean Packs!", new Item(34024, 3)),
    			new LMSReward("10 Tortured Souls!", new Item(32707, 10)),
    			new LMSReward("An Ancient Warriors' Equipment Patch!", 39047, 1), 
    			new LMSReward("Statius' Armour Set", new Item(13896), new Item(13884), new Item(13890)),
    			new LMSReward("Statius' Warhammer", new Item(13902)),
				new LMSReward("Vesta's Armour Set", new Item(13887), new Item(13893)),
    			new LMSReward("10 Aggression Potions", new Item(37972, 10)),	
    			new LMSReward("Vesta's Weaponry", new Item(13905), new Item(13899)), 
    			new LMSReward("Morrigan's Armour Set", new Item(13876), new Item(13870), new Item(13873)),
    			new LMSReward("Morrigan's Thrown Equipment", new Item(13879, 100), new Item(13883, 100)),
				new LMSReward("Zuriel's Equipment", new Item(13864), new Item(13858), new Item(13861), new Item(13867))),
    VERY_RARE(86, 100, 
    			new LMSReward("5 Large Protean Packs!", new Item(34025, 5)),
    			new LMSReward("5 HUGE Bonus XP Lamps", 28907, 5),
    			new LMSReward("6 Rare Item Tokens", 34027, 6),
    			new LMSReward("15 Tortured Souls!", new Item(32707, 15)),
    			new LMSReward("A Golden Godsword Set!", new Item(32014), new Item(32018), new Item(32020), new Item(32016)),
    			new LMSReward("Five Deathtouched Darts!", 25202, 5),
    			new LMSReward("200m Coins!", 995, 200000000),
    			new LMSReward("A Golden Ticket", 36965, 1),
    			new LMSReward("Dragon Limbs", 25481, 1),
    			new LMSReward("A Large rune pouch", 38453, 1),
    			new LMSReward("Twin Furies Set", new Item(37090), new Item(37095)),
    			new LMSReward("Dragon Rider Lance", 37070, 1),
    			new LMSReward("Helwyr Set", new Item(37085), new Item(40600)),
    			new LMSReward("Crystal Triskelion Bag", new Item(48480)),
    			new LMSReward("50 Aggression Potions", new Item(37972, 50)));

    private final int lowerBound;
    private final int upperBound;
    private final LMSReward[] rewards;

    public static final LastManStandingRewardType[] VALUES = values();

    LastManStandingRewardType(int lowerBound, int upperBound, LMSReward... rewards) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.rewards = rewards;
    }

    @Override
    public String toString() {
        return name().replace("_", " ");
    }

    public int getLowerBound() {
        return lowerBound;
    }

    public int getUpperBound() {
        return upperBound;
    }

    public LMSReward[] getRewards() {
        return rewards;
    }
}
