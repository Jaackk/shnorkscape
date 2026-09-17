package com.rs.game.player;

import com.google.common.collect.ImmutableList;
import com.rs.game.player.content.polls.IncorrectAnswerD;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.InputStringEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DataAnswerInterface extends DataInterface {
    private static final String LETTER_COLOR = Colors.DARK_RED;

    private static final ImmutableList<String> LETTER_MAP = ImmutableList.of("A",
            "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O",
            "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",

            "A1", "B1", "C1", "D1", "E1", "F1", "G1", "H1", "I1", "J1", "K1", "L1", "M1",
            "N1", "O1", "P1", "Q1", "R1", "S1", "T1", "U1", "V1", "W1", "X1", "Y1", "Z1");

    private final Map<String, Runnable> answers;
    private final Runnable incorrectAnswerAction;

    public DataAnswerInterface(String title, Map<String, Runnable> answers, Runnable incorrectAnswerAction) {
        super(title);
        this.answers = answers;
        this.incorrectAnswerAction = incorrectAnswerAction;
    }

    @Override
    public List<String> onShow(Player player) {
        Map<String, String> validAnswers = new HashMap<>(answers.size());
        List<String> lines = new ArrayList<>();
        lines.add("");
        int index = 0;
        for (Map.Entry<String, Runnable> entry : answers.entrySet()) {
            String letter = LETTER_MAP.get(index);
            String answer = entry.getKey();
            lines.add(LETTER_COLOR + letter + "</col>. " + answer);
            validAnswers.put(letter, answer);
            index++;
        }
        player.sendInputString("Enter your answer", new InputStringEvent() {
            @Override
            public void run(Player player) {
                String letterAnswer = getString().toUpperCase();
                if (!validAnswers.containsKey(letterAnswer)) {
                    WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                            player.unlock();
                            player.getDialogueManager().startDialogue(new IncorrectAnswerD(incorrectAnswerAction));
                        }
                    }, 1);
                } else {
                    answers.get(validAnswers.get(letterAnswer)).run();
                }
            }
        });
        return lines;
    }
}

