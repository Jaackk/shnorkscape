package com.rs.game.player.content.presets;

import com.rs.game.item.Item;
import com.rs.game.player.actions.summoning.Summoning;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * ataraxia-server
 * paolo 15/06/2019
 * #Shnek6969
 * TODO summoning familiar
 */
@Data
public class Preset   implements Serializable {

    private static final long serialVersionUID = 1514;
    @Getter @Setter
    public String name;
    @Getter @Setter
    public Item[] equipment, inventory;
    @Getter @Setter
    public int spellBook, prayers;
    @Getter @Setter
    public Summoning.Pouches pouch;



    public Preset(String name, Item[] equipment, Item[] inventory, int spellBook, int praybook, Summoning.Pouches pouch) {
        this.name = name;
        this.equipment = equipment;
        this.inventory = inventory;
        this.spellBook = spellBook;
        this.prayers = praybook;
        this.pouch = pouch;
    }

    @Override
    public String toString(){
        return this.name;
    }
}
