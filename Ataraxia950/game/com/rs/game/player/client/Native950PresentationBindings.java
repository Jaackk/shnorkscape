package com.rs.game.player.client;

/** Cache-derived names mapped by exact group equality; see polish2-gameval-cross-revision-950.json. */
final class Native950PresentationBindings {
    private Native950PresentationBindings(){}
    static int playerAnimation(int ability){
        switch(ability){
            case 14713:return 18081; // combat_2012_ability_defence_basic_resonance
            case 14714:return 18106; // combat_2012_ability_defence_basic_preparation
            case 19252:return 19858; // tsd_player_combat_ability_natural_instinct
            case 19254:return 19866; // tsd_player_combat_ability_sunshine
            case 19251:return 19879; // tsd_player_combat_ability_deaths_swiftness
            case 14726:return 18358; // combat_2012_ability_magic_basic_surge
            case 14665:return 18527; // combat_2012_ability_ranged_basic_escape
            case 47129:return 35755; // combatv2_ability_melee_dive
            case 1488:return 35626; // combatv2_bladed_dive
            default:return -1;
        }
    }
    static int casterGraphic(int ability){
        switch(ability){case 14714:return 3614;case 19252:return 3855;case 25028:return 4503;case 14726:return 3537;case 14665:return 3526;case 48324:return 7883;default:return -1;}
    }
    static int conjureAttack(Native950Conjures.Kind kind,boolean commanded){
        switch(kind){case SKELETON:return commanded?35219:35204;case ZOMBIE:return 35249;default:return -1;}
    }
    static int conjureSpawn(Native950Conjures.Kind kind){
        switch(kind){case SKELETON:return 35216;case ZOMBIE:return 35256;case GHOST:return 24724;case PHANTOM:return 36213;default:return -1;}
    }
    static int conjureExit(Native950Conjures.Kind kind){
        switch(kind){case SKELETON:return 35206;case ZOMBIE:return 35251;case GHOST:return 24727;case PHANTOM:return 36214;default:return -1;}
    }
    static int conjureSpawnGraphic(Native950Conjures.Kind kind){return kind==Native950Conjures.Kind.SKELETON?7815:kind==Native950Conjures.Kind.ZOMBIE?7819:-1;}
    static int conjureExitGraphic(Native950Conjures.Kind kind){return kind==Native950Conjures.Kind.SKELETON?7811:kind==Native950Conjures.Kind.GHOST?5187:-1;}
}
