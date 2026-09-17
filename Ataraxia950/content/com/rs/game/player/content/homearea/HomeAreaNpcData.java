package com.rs.game.player.content.homearea;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Xenthium.
 */

@AllArgsConstructor
public enum HomeAreaNpcData {
    // Diamond+ npcs
    GREAT_WHITE_SHARK_FISHING_SPOT(500, ""),
    SAWMILL_OPERATOR(0, ""),
    HAZELMERE(0, ""),
    BOB(0, ""),
    WYDIN(0, ""),
    DIANGO(0, ""),
    DEATH(0, ""),

    // Platinum+ npcs
    LADY_ITHELL(250, ""),
    HEFIN_MONK(250, ""),
    HERO(250, ""),
    PIKKUPSTIX(0, ""),

    // Bronze+ npcs
    ELLIS(0, ""),
    TEPLIN_MACAGAN(0, ""),
    NURMOF(20, "Not very tall."),
    SKILLING_MASTER(0, ""),
    DRUID(0, ""),
    SURGEON_GENERAL_TAFANI(0, ""),
    FISHING_SPOT(20, ""),
    KURADAL(0, "Gives Slayer tasks."),
    BANKER(0, ""),
    SOLOMON(0, ""),
    PARTY_PETE(0, ""),
    WISE_OLD_MAN(0, "Looks pretty old."),
    //GRAND_EXCHANGE_CLERK(20, "I wonder if they get bored standing around here all the time."),
    CAVEFISH_SHOAL(20, "Catch Cavefish here."),
    ROCKTAIL_SHOAL(20, "Catch Rocktail here."),
    KARAMBWAN_SHOAL(20, "Catch Karambawn here."),
    SHOPKEEPER(0, "General store."),
    GREEN_PARTYHAT(0, "A nice hat from a cracker.") {
        @Override
        public String toString() {
            return "<col=B8D1D1>Green partyhat";
        }
    },
    BOB_BARTER_HERBS(0, "") {
        @Override
        public String toString() {
            return "Bob Barter (herbs)";
        }
    },
    SKILLING_MASTER_ADVANCED(0, "") {
        @Override
        public String toString() {
            return "Skilling master (advanced)";
        }
    },
    UZIS_PERK_SHOP(0, "Needs to make a pornhub with his mrs.") {
        @Override
        public String toString() {
            return "Uzi's Perk Shop";
        }
    },
    ;

    @Getter
    public int amountRequiredToInteract; // The amount you need to have donated to interact with the npc.
    @Getter
    public String description;

    public String getNpcName() {
        return StringUtils.capitalize(this.toString().toLowerCase().replaceAll("_", " "));
    }
}
