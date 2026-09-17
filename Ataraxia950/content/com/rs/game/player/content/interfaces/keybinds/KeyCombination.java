package com.rs.game.player.content.interfaces.keybinds;

import lombok.Getter;
import lombok.Setter;

import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.Map;

import static com.rs.game.player.content.KeyActionMapper.INDEX_MAP;

/**
 * ataraxia-server
 * paolo 03/10/2019
 * #Shnek6969
 */
public class KeyCombination implements Serializable {

    @Getter @Setter
    public int firstKey;
    @Getter @Setter
    public int secondKey;

    public  KeyCombination(int firstKey, int secondKey){
        this.firstKey = firstKey;
        this.secondKey = secondKey;
    }

    @Override
    public String toString() {
        return  "Ctrl + " +INDEX_MAP
                .entrySet()
                .stream()
                .filter(entry -> secondKey == entry.getValue()).map(Map.Entry::getKey).findAny().orElse(error());
    }

    private String error(){
        return "not found";
    }
}
