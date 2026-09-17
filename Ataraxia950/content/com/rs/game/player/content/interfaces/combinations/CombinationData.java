package com.rs.game.player.content.interfaces.combinations;

import com.rs.cache.loaders.ItemDefinitions;
import lombok.Getter;
import lombok.Setter;

/**
 * ataraxia-server
 * paolo 08/08/2019
 * #Shnek6969
 */

public enum CombinationData {
    /**
     * add your combinations here
     * note if the description is null it will get the examine message
     */
    NECKLACE_OF_OMNIPOTENCE(new int[]{31875, 31872, 31448, 18335, 31445}, 48484, 15, "Amulet of Omnipotence is the Best-in-slot Amulet & provides the effects of Amulet of Souls!!"),
    RING_OF_OMNIPOTENCE(new int[]{773, 31869, 15018, 15019, 15020, 15220}, 48483, 10, "Ring of Omnipotence is the Best-in-slot Ring & provides the effects of 'perfect' ring and Ring of Death!"),
    GLOVES_OF_OMNIPOTENCE(new int[]{31203, 31189, 30213, 31878}, 48481, 10, "Gloves of Omnipotence are the Best-in-slot Hybrid gloves & provides the same effect as Deathtouch Bracelet!"),

    BOOTS_OF_OMNIPOTENCE(new int[]{34984, 34978, 34981}, 48482, 10, "Boots of Omnipotence are the Best-in-slot Hybrid boots!"),

    BOOTS_OF_OMNIPOTEN(new int[]{34984, 34978, 34981}, 48482, 10, "Boots of Omnipotence are the Best-in-slot Hybrid boots!"),
    PICKAXE_OF_EARTH_AND_SONG(new int[]{32646, 29522, 15259, 45642}, 44834, 50, "Boots of Omnipotence are the Best-in-slot Hybrid boots!");

    @Getter
    @Setter
    public int[] requiredItems;
    @Getter
    @Setter
    public int productId, chance;
    @Setter //custom getter
    public String description;

    CombinationData(int[] requiredItems, int productId, int chance, String description) {
        this.requiredItems = requiredItems;
        this.productId = productId;
        this.chance = chance;
        this.description = description;

    }

    CombinationData(int[] requiredItems, int productId) {
        this(requiredItems, productId, 100, null);
    }

    CombinationData(int[] requiredItems, int productId, String description) {
        this(requiredItems, productId, 100, description);
    }

    public static CombinationData isImbuedGear(int itemId) {
        switch (itemId) {
            case 4308:
                return CombinationData.NECKLACE_OF_OMNIPOTENCE;
        }
        return null;
    }

    /**
     * custom getter for the examine message
     *
     * @return
     */
    public String getDescription() {
        if (this.description == null) {
            return ItemDefinitions.getItemDefinitions(this.getProductId()).getExamine();
        }
        return this.description;
    }
}
