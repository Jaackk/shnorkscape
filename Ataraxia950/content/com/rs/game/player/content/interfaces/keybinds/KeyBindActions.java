package com.rs.game.player.content.interfaces.keybinds;

import lombok.Getter;

import static com.rs.game.player.content.KeyActionMapper.INDEX_MAP;

/**
 * ataraxia-server
 * paolo 03/10/2019
 * #Shnek6969
 */
public enum KeyBindActions {

    OPEN_BANK(new KeyCombination(KeyBindInterface.CTRL_KEY, INDEX_MAP.get("B"))),
    TELEPORT_HOME(new KeyCombination(KeyBindInterface.CTRL_KEY,  INDEX_MAP.get("H"))),
    OPEN_TELEPORT_INTERFACE(new KeyCombination(KeyBindInterface.CTRL_KEY, INDEX_MAP.get("T"))),
    OPEN_LODESTONE(new KeyCombination(KeyBindInterface.CTRL_KEY, INDEX_MAP.get("N"))),
    SHEATH(new KeyCombination(KeyBindInterface.CTRL_KEY, INDEX_MAP.get("S"))),
    OPEN_WORLDMAP(new KeyCombination(KeyBindInterface.CTRL_KEY, INDEX_MAP.get("M"))),
    TOGGLE_QUICKPRAYERS(new KeyCombination(KeyBindInterface.CTRL_KEY, INDEX_MAP.get("Q"))),
    OPEN_FAMILIAR_DETAIL(new KeyCombination(KeyBindInterface.CTRL_KEY, INDEX_MAP.get("F"))),
    TOGGLE_SHIFT_DROP(new KeyCombination(KeyBindInterface.CTRL_KEY, INDEX_MAP.get("SHIFT"))),
    TOGGLE_RUN(new KeyCombination(KeyBindInterface.CTRL_KEY, INDEX_MAP.get("R"))),
    FREEDOM(new KeyCombination(KeyBindInterface.CTRL_KEY, INDEX_MAP.get("X"))),
    OPEN_GIM_BANK(new KeyCombination(KeyBindInterface.CTRL_KEY, INDEX_MAP.get("G"))),
    SURGE(new KeyCombination(KeyBindInterface.CTRL_KEY, INDEX_MAP.get("C"))),
    ESCAPE(new KeyCombination(KeyBindInterface.CTRL_KEY, INDEX_MAP.get("V")));

    @Getter
    public KeyCombination defaultCombination;

    KeyBindActions(KeyCombination keyCombination){
        this.defaultCombination = keyCombination;
    }
}
