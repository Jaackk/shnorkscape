package com.rs.game.player.content.contracts;

public enum ReaperPerks {

    REAPERS_CHOICE(150, "Random chance of being able to choose your assignment when recieving a new one.", 0.15),

    REAPERS_BLESSING(200, "While killing your assigned reaper target, recieve 5% increased drop rate.", 1),

    EXTENDED_MASSACRE(150, "All reaper tasks will be extended by 25%, and you will recieve 25% more points for completing your task.", 1),

    TAKE_TWO(200, "After completing a task, you get the ability to skip your task once for free.", 0.20);

    private final int price;
    private final String desc;
    private final double activationChance;

    ReaperPerks(int price, String desc, double activationChance) {
        this.price = price;
        this.desc = desc;
        this.activationChance = activationChance;
    }

    public int getPrice() {
        return price;
    }

    public String getDesc() {
        return desc;
    }

    public double getActivationChance() {
        return activationChance;
    }

}