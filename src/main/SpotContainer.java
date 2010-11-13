package main;

import java.io.File;
import java.util.Vector;

public class SpotContainer {
	public static final int TYPE_AVAILABLE = 0;
	public static final int TYPE_ACTIVE = 1;
	
	protected Vector<SpotEntry> playQueue;
	protected int type;
	
	public SpotContainer(int type) {
		playQueue = new Vector<SpotEntry>();
		this.type = type;
		
	}
	
	public void initializeFromPrefs() {
		int size = Prefs.prefs.getInt(Prefs.SPOTLIST_SIZE + type, Prefs.SPOTLIST_SIZE_DEFAULT);
		System.out.println("Reading " + size + " stored spot entries");
		playQueue.removeAllElements();
		
		for (int i = 0; i < size; i++) {
			playQueue.add(new SpotEntry(
					new File(Prefs.prefs.get(Prefs.SPOTLIST_ENTRY_FILENAME + type + "." + i, "not.found")),
					Prefs.prefs.get(Prefs.SPOTLIST_ENTRY_NAME + type + "." + i, "Not found!")
					));
			System.out.println("Added item " + i + " to player");
		}
	}
	
	public void addToEnd(SpotEntry spot) {
		playQueue.add(spot);
		
		Prefs.prefs.putInt(Prefs.SPOTLIST_SIZE + type, playQueue.size());
		Prefs.prefs.put(Prefs.SPOTLIST_ENTRY_NAME + type + "." + (playQueue.size()-1), spot.getName());
		Prefs.prefs.put(Prefs.SPOTLIST_ENTRY_FILENAME + type + "." + (playQueue.size()-1), spot.getFile().getName());
	}
}
