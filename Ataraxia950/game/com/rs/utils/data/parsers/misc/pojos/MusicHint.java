package com.rs.utils.data.parsers.misc.pojos;

import lombok.Data;

@Data
public class MusicHint {
    private final int regionId;
    private final String hint;

    public MusicHint(int regionId, String hint) {
        this.regionId = regionId;
        this.hint = hint;
    }
}
