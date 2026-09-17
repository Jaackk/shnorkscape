package com.rs.game.activites.gim.event;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rs.cores.CoresManager;
import com.rs.external.api.json.JsonParser;
import com.rs.game.activites.gim.GIM;
import com.rs.utils.Logger;
import com.rs.utils.Utils;
import lombok.val;
import org.apache.logging.log4j.core.Core;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * A class that manages GIM events for bonus points.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class GIMEventManager {

    /**
     * The formatter for the end date.
     */
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.ENGLISH);

    /**
     * The Gson instance.
     */
    private final Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    /**
     * The event JSON file.
     */
    private final Path eventFile = Paths.get("data", "gim", "event.json");

    /**
     * The current event.
     */
    private GIMEvent currentEvent;

    /**
     * Loads the current event.
     */
    public void load() {
        Utils.createFileIfNotExists(eventFile);
        currentEvent = new JsonParser(eventFile.toString(), GIMEvent.class).getFileLoaded();
        checkIfComplete();
    }

    /**
     * Stops the current event. Should not be called on the game thread.
     */
    public void stop() {
       CoresManager.getServiceProvider().addGameTask(() -> currentEvent = null);
        try {
            Files.deleteIfExists(eventFile);
        } catch (IOException e) {
            Logger.getGlobal().catching(e);
        }
    }

    /**
     * Sets a new active event.
     */
    public void setCurrentEvent(GIMEvent newEvent) {
        if (!isRunning()) {
            val now = LocalDate.now();
            val endDate = newEvent.getEndDate();
            if (endDate.isBefore(now) || endDate.isEqual(now)) {
                throw new IllegalStateException("Cannot schedule event in the past! date=" + now.format(formatter));
            }
            currentEvent = newEvent;
            String msg = "Event '" + getEventDescription() + "' is now active until " + getFormattedEndDate() + "!";
            CoresManager.getServiceProvider().executeNow(() -> {
                Utils.createFileIfNotExists(eventFile);
                try (val fw = new FileWriter(eventFile.toFile())) {
                    gson.toJson(newEvent, fw);
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            });
            GIM.sendWorldMsg(msg);
        }
    }

    /**
     * Determines if an event with the specified type is running.
     */
    public boolean isRunning(GIMEventType eventType) {
        checkIfComplete();
        return currentEvent != null && currentEvent.getType() == eventType;
    }

    /**
     * Determines if any event type is running.
     */
    public boolean isRunning() {
        checkIfComplete();
        return currentEvent != null;
    }

    /**
     * Checks if the event is complete, and clears it if so.
     */
    private void checkIfComplete() {
        LocalDate now = LocalDate.now();
        if (currentEvent != null) {
            if (currentEvent.getEndDate().isEqual(now) ||
                    currentEvent.getEndDate().isBefore(now)) {
                currentEvent = null;
                CoresManager.getServiceProvider().executeNow(() -> {
                    try {
                        Files.delete(eventFile);
                    } catch (IOException e) {
                        Logger.getGlobal().catching(e);
                    }
                });
            }
        }
    }

    /**
     * Gets the current event's description.
     */
    public String getEventDescription() {
        return currentEvent == null ? null : currentEvent.getType().getDescription();
    }

    /**
     * Gets the current event's formatted end date.
     */
    public String getFormattedEndDate() {
        return currentEvent == null ? null : currentEvent.getEndDate().format(formatter);
    }

    public DateTimeFormatter getFormatter() {
        return formatter;
    }
}
