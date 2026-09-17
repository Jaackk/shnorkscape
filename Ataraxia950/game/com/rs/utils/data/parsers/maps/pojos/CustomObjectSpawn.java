package com.rs.utils.data.parsers.maps.pojos;

import lombok.Data;

@Data
public class CustomObjectSpawn {
    private int objectId;
    private ObjectType type = ObjectType.REGULAR;
    private ObjectRotation rotation = ObjectRotation.NORTH;
    private ObjectSpawnLocation location;
    private boolean isClipped = true;
}
