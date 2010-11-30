package main;

import java.io.File;
import java.util.Vector;

public class SpotContainer {
	public static final int TYPE_AVAILABLE = 0;
	public static final int TYPE_ACTIVE = 1;
	
	private static final String[][] columnNames = new String[][] {
		{"Navn" , "Varighed"}, //TYPE_AVAILABLE
		{"Nr." , "Navn" , "Varighed", "*"} //TYPE_ACTIVE
	};
	
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
			System.out.println("Added item " + i + " to spotContainer/player");
		}
	}
	
	public void addToEnd(SpotEntry spot) { // TO-DO: add to GUI also
		playQueue.add(spot);
		saveSpotToPrefs(playQueue.size() - 1, spot);
	}
	
	private void saveSpotToPrefs(int position, SpotEntry spot) {
		/**
		 * To be called after spot is added to container.
		 * Overwrites if position exists already
		 */
		Prefs.prefs.putInt(Prefs.SPOTLIST_SIZE + type, playQueue.size());
		Prefs.prefs.put(Prefs.SPOTLIST_ENTRY_NAME + type + "." + position, spot.getName());
		Prefs.prefs.put(Prefs.SPOTLIST_ENTRY_FILENAME + type + "." + position, spot.getFile().getName());		
	}
	
	public SpotEntry getSpotAt(int index) {
		return playQueue.get(index);
	}
	
	public void swapSpots(int index1, int index2) {
		SpotEntry temp = playQueue.get(index1);
		playQueue.set(index1, playQueue.get(index2));
		playQueue.set(index2, temp);
		
		saveSpotToPrefs(index1, playQueue.get(index1));
		saveSpotToPrefs(index2, playQueue.get(index2));
	}
	
	public Object[][] getData() {
		Object[][] data = new Object[playQueue.size()][getColumnNames().length];
		
		for (int i = 0; i < playQueue.size(); i++) {
			if (type == TYPE_ACTIVE) {
				data[i][0] = new Integer(i+1); // "+1" for human readability
				data[i][1] = playQueue.get(i).getName();
				data[i][2] = Calculate.millisToMinsSecsString(playQueue.get(i).getLengthInMillis());
				data[i][3] = "";
			} else {
				data[i][0] = playQueue.get(i).getName();
				data[i][1] = Calculate.millisToMinsSecsString(playQueue.get(i).getLengthInMillis());
			}
		}
		return data;
	}
	
	public String[] getColumnNames() {
		return columnNames[type];
	}
}
