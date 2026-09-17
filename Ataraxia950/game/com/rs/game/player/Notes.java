package com.rs.game.player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class Notes implements Serializable {

	private static final long serialVersionUID = 5564620907978487391L;

	private final List<Note> notes;
	private transient Player player;

	public Notes() {
		notes = new ArrayList<Note>(30);
	}

	/**
	 * Gets the primary colour of the notes.
	 *
	 * @param notes
	 *            The notes.
	 * @return
	 */
	public static int getPrimaryColour(Notes notes) {
		int color = 0;
		for (int i = 0; i < 16; i++) {
			if (notes.notes.size() <= i)
				break;
			color += colourize(notes.notes.get(i).colour, i);
		}
		return color;
	}

	/**
	 * Gets the secondary colour of the notes.
	 *
	 * @param notes
	 *            The notes.
	 * @return
	 */
	public static int getSecondaryColour(Notes notes) {
		int color = 0;
		for (int i = 0; i < 14; i++) {
			if (notes.notes.size() - 16 <= i)
				break;
			color += colourize(notes.notes.get(i + 16).colour, i);
		}
		return color;
	}

	public static int colourize(int colour, int noteId) {
		return (int) (Math.pow(4, noteId) * colour);
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public void init() {
	    removeCurrentNote();
		refresh();
		player.getPackets().sendConfig(97, 1); // unlocks notes
	}

	public void refresh() {
	    refreshNotesText();
		player.getPackets().sendConfig(99, getPrimaryColour(this));
		player.getPackets().sendConfig(100, getSecondaryColour(this));
	}

	public int getCurrentNote() {
		Integer note = (Integer) player.getTemporaryAttributtes().get("CURRENT_NOTE");
		if (note == null)
			return -1;
		return note;
	}

	public void setCurrentNote(int id) {
		if (id >= 30)
			return;
		player.getTemporaryAttributtes().put("CURRENT_NOTE", id);
		player.getPackets().sendConfig(98, id);
	}

	public void removeCurrentNote() {
		player.getTemporaryAttributtes().remove("CURRENT_NOTE");
		player.getPackets().sendConfig(98, -1);
	}

	public boolean add(String text) {
		if (notes.size() >= 30) {
			player.getPackets().sendGameMessage("You may only have 30 notes!");
			return false;
		}
		if (text.length() > 50) {
			player.getPackets().sendGameMessage("You can only enter notes up to 50 characters!");
			return false;
		}
		player.getPackets().sendGlobalString(2254 + notes.size(), text);
		setCurrentNote(notes.size());
		return notes.add(new Note(text));
	}

	public boolean edit(String text) {
		if (text.length() > 50) {
			player.getPackets().sendGameMessage("You can only enter notes up to 50 characters!");
			return false;
		}
		int id = getCurrentNote();
		if (id == -1 || notes.size() <= id)
			return false;
		notes.get(id).setText(text);
		player.getPackets().sendGlobalString(2254 + id, text);
		refresh();
		return true;
	}

	public boolean colour(int colour) {
		int id = getCurrentNote();
		if (id == -1 || notes.size() <= id)
			return false;
		notes.get(id).setColour(colour);
		if (id < 16)
			player.getPackets().sendConfig(99, getPrimaryColour(this));
		else
			player.getPackets().sendConfig(100, getSecondaryColour(this));
		refresh();
		return true;
	}

	public void switchNotes(int from, int to) {
		if (notes.size() <= from || notes.size() <= to)
			return;
		notes.set(to, notes.set(from, notes.get(to)));
		refresh();
	}

	public void delete() {
		delete(getCurrentNote());
	}

	public void delete(int id) {
		if (id == -1 || notes.size() <= id)
			return;
		notes.remove(id);
		removeCurrentNote();
		refresh();
	}

	public void deleteAll() {
		notes.clear();
		removeCurrentNote();
		refresh();
	}

	public List<Note> getNotes() {
		return notes;
	}
	
    public void unlockNotes(boolean menuInterface) {
        player.getPackets().sendIComponentSettings(menuInterface ? 34 : 1417, 13, 0, 29, 2621470); // notes
        player.getPackets().sendHideIComponent(menuInterface ? 34 : 1417, menuInterface ? 56 : 54, false); // button
        player.getPackets().sendHideIComponent(menuInterface ? 34 : 1417, menuInterface ? 7 : 5, false);
        refresh();
    }
    
    private void refreshNotesText() {
        for (int i = 0; i < 30; i++)
            player.getPackets().sendGlobalString(2254 + i, notes.size() <= i ? "" : notes.get(i).text);
    }

	public static final class Note implements Serializable {

		private static final long serialVersionUID = -3867862135920019512L;

		private String text;
		private int colour;

		public Note(String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		public int getColour() {
			return colour;
		}

		public void setColour(int colour) {
			this.colour = colour;
		}
	}
}
