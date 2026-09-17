package com.rs.game.activites.gim.event;

import com.rs.game.activites.gim.GIM;
import com.rs.game.player.Player;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.InputIntegerEvent;

import java.time.LocalDate;

/**
 * A dialogue that enables staff to create events.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class DCreateEvent extends Dialogue {

    /**
     * The selected event.
     */
    private GIMEventType selectedEvent;

    /**
     * The selected date.
     */
    private LocalDate newDate;

    @Override
    public void start() {
        if (GIM.getEventManager().isRunning()) {
            sendDialogue("A " + GIM.getEventManager().getEventDescription() + " event is already running. It will end on " + GIM.getEventManager().getFormattedEndDate() + ".");
            stage = -1;
        } else {
            sendDialogue("This command will create a new GIM event. What type of event would you like to create?");
            stage = 0;
        }
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -1:
                end();
                break;
            case 0:
                sendOptionsDialogue("Select an option.",
                        GIMEventType.XP.getDescription(),
                        GIMEventType.BOSS_POINTS.getDescription());
                stage = 1;
                break;
            case 1:
                if (componentId == OPTION_1) {
                    selectedEvent = GIMEventType.XP;
                } else if (componentId == OPTION_2) {
                    selectedEvent = GIMEventType.BOSS_POINTS;
                }

                if (selectedEvent == null) {
                    end();
                    return;
                }

                player.sendInputInteger("Enter the amount of days (1-7)", new InputIntegerEvent() {
                    @Override
                    public void run(Player player) {
                        int days = getInteger();
                        if (days < 1) {
                            days = 1;
                        } else if (days > 7) {
                            days = 7;
                        }
                        days++;
                        newDate = LocalDate.now().plusDays(days);
                        sendDialogue("Are you sure you would like to start this " + selectedEvent.getDescription() + " event?",
                                "It will end on " + GIM.getEventManager().getFormatter().format(newDate) + ".");
                        stage = 2;
                    }
                });
                break;
            case 2:
                sendOptionsDialogue("Select an option.",
                        "Yes",
                        "No");
                stage = 3;
                break;
            case 3:
                if (componentId == OPTION_1) {
                    GIM.getEventManager().setCurrentEvent(new GIMEvent(selectedEvent, newDate));
                    sendDialogue("The event has been successfully created.");
                    stage = -1;
                } else if (componentId == OPTION_2) {
                    end();
                }
                break;
        }
    }

    @Override
    public void finish() {

    }
}
