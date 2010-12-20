package main;

import java.io.File;
import java.util.Vector;

public class SpotContainer {
	public static final int TYPE_AVAILABLE = 0;
	public static final int TYPE_ACTIVE = 1;
	public static final int TYPE_TEMPORARY = 2;
	
	protected Vector<SpotEntry> spotList;
	protected int type;
	
	public SpotContainer(int type) {
		spotList = new Vector<SpotEntry>();
		this.type = type;
		
	}
	
	public void initializeFromPrefs() {
		if (type != TYPE_TEMPORARY) {
			int size = Prefs.prefs.getInt(Prefs.SPOTLIST_SIZE + type, Prefs.SPOTLIST_SIZE_DEFAULT);
			System.out.println("Reading " + size + " stored spot entries");
			spotList.removeAllElements();
			
			for (int i = 0; i < size; i++) {
				spotList.add(new SpotEntry(
						new File(Prefs.prefs.get(Prefs.SPOTLIST_ENTRY_FILENAME + type + "." + i, "not.found")),
						Prefs.prefs.get(Prefs.SPOTLIST_ENTRY_NAME + type + "." + i, "Not found!")
						));
				System.out.println("Added item " + i + " to spotContainer/player");
			}
		}
	}
	
	public void addToEnd(SpotEntry spot) {
		spotList.add(spot);
		if (type != TYPE_TEMPORARY)
			saveSpotToPrefs(spotList.size() - 1, spot);
	}
	
	public SpotEntry remove(int index) {
		SpotEntry removedSpot = spotList.remove(index);
		if (type != TYPE_TEMPORARY)
			saveAllSpotsToPrefs();
		return removedSpot;
	}
	
	private void saveAllSpotsToPrefs() {
		if (type != TYPE_TEMPORARY) {
			for (int i = 0; i < spotList.size(); i++) {
				saveSpotToPrefs(i, spotList.get(i));
			}
		}
	}
	
	private void saveSpotToPrefs(int position, SpotEntry spot) {
		/**
		 * To be called after spot is added to container.
		 * Overwrites if position exists already
		 */
		if (type != TYPE_TEMPORARY) {
			Prefs.prefs.putInt(Prefs.SPOTLIST_SIZE + type, spotList.size());
			Prefs.prefs.put(Prefs.SPOTLIST_ENTRY_NAME + type + "." + position, spot.getName());
			Prefs.prefs.put(Prefs.SPOTLIST_ENTRY_FILENAME + type + "." + position, spot.getFile().getName());
		}
	}
	
	public SpotEntry getSpotAt(int index) {
		return spotList.get(index);
	}
	
	public void swapSpots(int index1, int index2) {
		SpotEntry temp = spotList.get(index1);
		spotList.set(index1, spotList.get(index2));
		spotList.set(index2, temp);
		
		if (type != TYPE_TEMPORARY) {
			saveSpotToPrefs(index1, spotList.get(index1));
			saveSpotToPrefs(index2, spotList.get(index2));
		}
	}
	
	public void renameSpot(int index, String newName) {
		spotList.get(index).setName(newName);
		if (type != TYPE_TEMPORARY)
			saveSpotToPrefs(index, spotList.get(index));
	}
	
	public Vector<SpotEntry> getDataCopy() {
		Vector<SpotEntry> data = new Vector<SpotEntry>();
		
		for (int i = 0; i < spotList.size(); i++) {
			data.add(spotList.get(i));
		}
		
		return data;
	}
}
