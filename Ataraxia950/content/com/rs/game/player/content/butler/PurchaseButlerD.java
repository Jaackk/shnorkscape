package com.rs.game.player.content.butler;

import com.rs.game.player.dialogue.Dialogue;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public final class PurchaseButlerD extends Dialogue {

    private final Queue<Butler> options = new ArrayDeque<>();
    private final Map<Integer, Runnable> optionActions = new HashMap<>();
    private Butler butler;

    @Override
    public void start() {
        loadOptions();
        sendNextOptions();
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 0:
                Runnable action = optionActions.get(componentId);
                if (action == null) {
                    throw new IllegalStateException("Null action button?");
                }
                action.run();
                break;
            case 1:
                break;
        }
    }

    @Override
    public void finish() {

    }

    private void sendNextOptions() {
        optionActions.clear();
        String[] optionArray = new String[5];
        for (int index = 0; index < optionArray.length; index++) {
            int componentId = index + 1;
            if (index == 4) {
                optionArray[index] = "Next";
                optionActions.put(componentId, this::sendNextOptions);
                continue;
            }
            Butler butler = options.poll();
            if (butler == null) {
                optionArray[index] = "Home";
                optionActions.put(componentId, () -> {
                    optionActions.clear();
                    options.clear();
                    start();
                });
                break;
            }
            optionArray[index] = butler.description;
            optionActions.put(componentId, () -> {
                // purchase butler
                this.butler = butler;
            });
        }
        sendOptionsDialogue("Select an option.", optionArray);

        stage = 0;
    }

    private void loadOptions() {

    }
}
