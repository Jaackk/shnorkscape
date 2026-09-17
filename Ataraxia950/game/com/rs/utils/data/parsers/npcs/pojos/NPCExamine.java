package com.rs.utils.data.parsers.npcs.pojos;

import lombok.Data;

@Data
public class NPCExamine {
    private final String name;
    private final String description;

    public NPCExamine(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
