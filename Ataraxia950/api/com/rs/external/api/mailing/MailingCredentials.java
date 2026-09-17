package com.rs.external.api.mailing;

import lombok.Data;

@Data
public class MailingCredentials {
    private final String username;
    private final String password;

    MailingCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
