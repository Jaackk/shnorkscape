package com.rs.game.player.security.pin;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class AccountPinModification implements Serializable {

    private static final long serialVersionUID = -5694119336681841314L;
    private final String pin;
    private final int recoveryDays;
    private final LocalDateTime activationDate;
    private final String description;

    public AccountPinModification(String pin, int recoveryDays, LocalDateTime activationDate, String description) {
        this.pin = pin;
        this.recoveryDays = recoveryDays;
        this.activationDate = activationDate;
        this.description = description;
    }

    public String getPin() {
        return pin;
    }

    public int getRecoveryDays() {
        return recoveryDays;
    }

    public LocalDateTime getActivationDate() {
        return activationDate;
    }

    public String getDescription() {
        return description;
    }
}
