package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;

public class OptionSelectionD extends Dialogue {

    private static final int SELECTION_INTERFACE = 1578, OPTIONS_SCRIPT = 10890, CLOSE_SCRIPT = 10895, REFRESH_SCRIPT = 8178;
    private static final int SCRIPT_SKIP_LENGTH = 3, OPTIONS_MAX = 10;
    private static final int[] SELECTOR_OPTIONS = { 1, 20, 23, 26, 29, 32, 35, 38, 41, 44 };

    public interface SelectionEvent {
        void run(OptionSelector selector, int option);
    }

    public class OptionSelector {
        public static final int OPTION_1 = 0, OPTION_2 = 1, OPTION_3 = 2, OPTION_4 = 3, OPTION_5 = 4, OPTION_6 = 5, OPTION_7 = 6, OPTION_8 = 7, OPTION_9 = 8, OPTION_10 = 9;

        private int page;
        private final int pageLength;
        private final String title;
        private final String[] options;
        private final String[] tooltips;

        public OptionSelector(String title, String[] tooltips, String[] options) {
            this.title = title;
            this.tooltips = tooltips;
            this.options = options;
            this.pageLength = options.length / OPTIONS_MAX;
        }

        public void send(int page) {
            if (page < 0 || page > pageLength)
                throw new IllegalStateException("Page exceeds the amount of parsable pages: " + page + ", " + pageLength);

            int row = page * OPTIONS_MAX;
            int pageOptionsLength = Math.min(OPTIONS_MAX, options.length - row), scriptLength = pageOptionsLength << 1;

            Object[] arguments = new Object[SCRIPT_SKIP_LENGTH + scriptLength];
            arguments[0] = pageOptionsLength;
            arguments[1] = 0;
            arguments[2] = title;
            for (int col = scriptLength - 1; col >= 0; col--)
                arguments[SCRIPT_SKIP_LENGTH + col] = (col & 0x1) == 1 ? tooltips == null ? "" : tooltips.length != options.length ? tooltips[0] : tooltips[row + (col >> 1)] : options[row + (col >> 1)];

            player.getPackets().sendExecuteScript(OPTIONS_SCRIPT, arguments);
            player.getInterfaceManager().sendInterface(SELECTION_INTERFACE);
            player.getInterfaceManager().refreshInterface(true);
            for (int index = pageOptionsLength - 1; index >= 0; index--)
                player.getPackets().sendIComponentSettings(SELECTION_INTERFACE, SELECTOR_OPTIONS[index], -1, -1, 1);
            player.getPackets().sendExecuteScript(REFRESH_SCRIPT);
        }

        public void next() {
            send(++page);
        }

        public void previous() {
            send(--page);
        }

        public void beginning() {
            send(page = 0);
        }

        public void last() {
            send(page = pageLength);
        }

        public void close() {
            end();
        }

        public int getPage() {
            return page;
        }
    }

    private SelectionEvent event;
    private OptionSelector selector;

    @Override
    public void start() {
        this.selector = new OptionSelector((String) parameters[0], (String[]) parameters[1], (String[]) parameters[2]);
        Object last = parameters[parameters.length - 1];
        if (!(last instanceof SelectionEvent))
            throw new IllegalStateException("Event is missing in option selection.");
        this.event = (SelectionEvent) last;
        selector.beginning();
    }

    @Override
    public void run(int interfaceId, int componentId) {
        event.run(selector, searchPrimitive(SELECTOR_OPTIONS, componentId));
    }

    @Override
    public void finish() {
        player.getInterfaceManager().closeScreenInterface();
        player.getPackets().sendExecuteScript(CLOSE_SCRIPT);
    }

    public static int searchPrimitive(int[] list, int target) {
        int min = 0, max = list.length - 1;

        while (max >= min) {
            int med = (min + max) / 2;

            if (list[med] < target)
                min = med + 1;
            else if (list[med] > target)
                max = med - 1;
            else
                return med;

        }
        return -1;
    }
}