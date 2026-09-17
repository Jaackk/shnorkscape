package com.rs.game.activites.gim.bank;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * A class representing a single entry of GIM bank history.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class GIMBankHistory implements Serializable {

    private static final long serialVersionUID = 1548785430432716114L;

    /**
     * The member name.
     */
    private final String memberName;

    /**
     * The action.
     */
    private final String msg;

    /**
     * The timestamp.
     */
    private final LocalDateTime timestamp;

    /**
     * Creates a new {@link GIMBankHistory}.
     */
    public GIMBankHistory(String memberName, String msg) {
        this.memberName = memberName;
        this.msg = msg;
        timestamp = LocalDateTime.now();
    }

    /**
     * Formats this history instance.
     */
    public String toFormatted() {
        return memberName + " " + msg;
    }

    public String getMemberName() {
        return memberName;
    }

    public String getMsg() {
        return msg;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
