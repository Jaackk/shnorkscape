package com.rs.external.api.mailing;

import com.google.gson.Gson;
import com.rs.game.player.Player;
import com.rs.utils.DataPaths;
import com.rs.utils.Logger;
import org.simplejavamail.email.Email;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.Mailer;
import org.simplejavamail.mailer.MailerBuilder;
import org.simplejavamail.mailer.config.TransportStrategy;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * SMTP bug/suggestion mails. The mailer used to be built in a static
 * initialiser, which threw {@link ExceptionInInitializerError} whenever
 * {@code data/misc/mailingCredentials.json} was absent and connected to Gmail
 * as a side effect of merely touching the class. It is now built lazily on the
 * first send, and when no credentials are staged (the 947 JVM never stages
 * them) every send is logged and dropped instead.
 */
public class MailingAPI {
    /** Relative to {@link DataPaths#root()}; the 947 JVM never stages it, so every send is logged and dropped there. */
    private static final String CREDENTIALS_FILE_PATH = "misc/mailingCredentials.json";
    private static final String FROM = System.getProperty("ataraxia.mail.from", "");
    private static final String ISSUES_TO = System.getProperty("ataraxia.mail.issuesTo", "");
    private static final String SUGGESTIONS_TO = System.getProperty("ataraxia.mail.suggestionsTo", "");

    private static Mailer mailer;
    private static boolean mailerResolved;

    /**
     * Credentials from disk, or null (logged) when the data root is unresolved or the
     * file is missing or unreadable. Mail is optional, so an unresolved root is a log
     * line here rather than the IllegalStateException the parsers get.
     */
    public static MailingCredentials getIssuesCredentials() {
        Path credentials;
        try {
            credentials = DataPaths.path(CREDENTIALS_FILE_PATH);
        } catch (IllegalStateException noRoot) {
            Logger.getGlobal().info("Mailing credentials unavailable (" + noRoot.getMessage() + "); mail is disabled");
            return null;
        }
        if (!Files.isRegularFile(credentials)) {
            Logger.getGlobal().info("Mailing credentials not staged at " + credentials + "; mail is disabled");
            return null;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(credentials.toFile()))) {
            Gson gson = new Gson();
            return gson.fromJson(br, MailingCredentials.class);
        } catch (IOException | RuntimeException e) {
            Logger.getGlobal().catching(e);
        }
        return null;
    }

    /** Builds the SMTP mailer once; null when credentials are absent. No network I/O happens here. */
    private static synchronized Mailer mailer() {
        if (mailerResolved)
            return mailer;
        mailerResolved = true;
        MailingCredentials credentials = getIssuesCredentials();
        if (FROM.isEmpty() || ISSUES_TO.isEmpty() || SUGGESTIONS_TO.isEmpty()
                || credentials == null || credentials.getUsername() == null || credentials.getPassword() == null)
            return null;
        try {
            mailer = MailerBuilder
                    .withSMTPServer("smtp.gmail.com", 587)
                    .withSMTPServerUsername(credentials.getUsername())
                    .withSMTPServerPassword(credentials.getPassword())
                    .withTransportStrategy(TransportStrategy.SMTP_TLS)
                    .withThreadPoolSize(1)
                    .clearEmailAddressCriteria()
                    .buildMailer();
        } catch (RuntimeException e) {
            Logger.getGlobal().catching(e);
        }
        return mailer;
    }

    private static void send(Email email, boolean async) {
        Mailer target = mailer();
        if (target == null) {
            Logger.getGlobal().info("Mail not sent (no mailer): subject=\"" + email.getSubject() + "\"");
            return;
        }
        try {
            target.sendMail(email, async);
        } catch (RuntimeException e) {
            Logger.getGlobal().catching(e);
        }
    }

    public static void sendBugEmail(String playerUsername, String issue) {
        Email email = EmailBuilder.startingBlank()
                .from(FROM)
                .to("GitLab Issue Tracker", ISSUES_TO)
                .withSubject(issue)
                .withPlainText("Bug Report by " + playerUsername + System.lineSeparator()+"/label ~player-reported")
                .buildEmail();
        send(email, true);
    }

    public static void sendCustomEmail(String title, String message) {
        Email email = EmailBuilder.startingBlank()
                .from(FROM)
                .to("GitLab Issue Tracker", ISSUES_TO)
                .withSubject(title)
                .withPlainText(message)
                .buildEmail();
        send(email, false);
    }

    public static void sendBugEmail(Player player, String title, String description) {
        Email email = EmailBuilder.startingBlank()
                .from(FROM)
                .to("GitLab Issue Tracker", ISSUES_TO)
                .withSubject(title)
                .withPlainText(description + System.lineSeparator() + System.lineSeparator() +"Bug report by " + player.getDisplayName() + System.lineSeparator() + "/label ~\"Player Reported\"")
                .buildEmail();
        send(email, true);
    }

    public static void sendSuggestionEmail(String displayName, String description) {
        Email email = EmailBuilder.startingBlank()
                .from(FROM)
                .to("GitLab Issue Tracker", SUGGESTIONS_TO)
                .withSubject(description)
                .withPlainText("Suggestion by " + displayName + System.lineSeparator() + "/label ~player-suggested")
                .buildEmail();
        send(email, true);
    }
}
