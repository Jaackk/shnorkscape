package com.rs.game.player.content.petperks;

import lombok.Getter;

/**
 * ataraxia-server
 * paolo 10/07/2019
 * #Shnek6969
 */
public enum PetPerk {

    NICE_BUT_DIM("Nice but Dim", "Increases your drop rate", "decreases your experience gain", 0.05, 0.10, 41478), //
    POWER_EXCHANGE("Power Exchange", "Increases your combat damage", null, 0.03, 0.10, 41471), //
    DOUBLE_TROUBLE("Double Trouble", "Increases the chance to double drops you receive", "has a chance to give you no drop by", 0.02, 0.05, 41473), //
    KURADAMN("Kuradamn", "3%, 6%, 10% slayer experience bonus, and a 2%/5%/10% chance for a Slayer kill not to count towards your slayer task", null, 0.05, 0, 41475), //
    OVERLOADED("Overloaded", "Saradomin brews don't drop your stats below 25%/20%/10% of the level and sipping overloads no longer damages you. On tier 3 a regular overload will count as a Supreme overload salve.", null, 0.05, 0, 41474),
    //TODO make item for overload extender
    OVERLOAD_EXTENDER("Overload extender", "Increases the duration of your overload. Tier 1: Regular overload, Tier 2: Holy overload and Tier 3: Supreme overload salve", null, 0.05, 0, -1),
    EFFICIENCY_EXPERT("Efficiency Expert", "2%, 4%, 8% chance to save materials when creating an item using a resource", "1%, 1%, 0% chance to use twice the materials,", 0.05, 0, 41479),
    EXTRA_ARMS("Extra Arms", "2%,4%,8% chance to double resources gathered", "1%, 1%, 0% chance to delete the gathered item", 0.05, 0, 41477);

    @Getter
    private final String name,proDescription;
    private final String conDescription;
    @Getter
    private final double proMultiplier,conMultiplier;
    @Getter
    private final int itemId;


    PetPerk(String name, String proDescription, String conDescription, double proMultiplier, double conMultiplier, int itemId) {
        this.name = name;
        this.proDescription = proDescription;
        this.conDescription = conDescription;
        this.proMultiplier = proMultiplier;
        this.conMultiplier = conMultiplier;
        this.itemId = itemId;
    }

    public String getConDescription(){
        if(conDescription == null)
            return "Nothing";
        return conDescription;
    }

}
