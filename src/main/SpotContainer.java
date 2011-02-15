package main;

import java.io.File;
import java.util.Vector;

public class SpotContainer {
	public static final int TYPE_AVAILABLE = 0;
	public static final int TYPE_INTERVALLED = 1;
	public static final int TYPE_SCHEDULED = 3;
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
			Util.get().out("Initializing: Reading " + size + " stored spot entries (type " + type + ")", Util.VERBOSITY_DEBUG_INFO);
			spotList.removeAllElements();
			boolean ignoredOneOrMoreSpots = false;
			
			for (int i = 0; i < size; i++) {
				File spotFile = new File(Util.get().getDataStoreDir(), Prefs.prefs.get(Prefs.SPOTLIST_ENTRY_FILENAME + type + "." + i, "not.found"));
				if (spotFile != null && spotFile.exists()) {
					SpotEntry spot = new SpotEntry(
							spotFile,
							Prefs.prefs.get(Prefs.SPOTLIST_ENTRY_NAME + type + "." + i, "Not found!")
					);
					boolean hasSchedule = Prefs.prefs.getBoolean(Prefs.SPOTLIST_ENTRY_HASSCHEDULE + type + "." + i, Prefs.SPOTLIST_ENTRY_HASSCHEDULE_DEFAULT);
                    if (hasSchedule) {
						PlaySchedule schedule = new PlaySchedule();
						boolean[] months = PlaySchedule.stringToArray(Prefs.prefs.get(Prefs.SPOTLIST_ENTRY_SCHEDULE_MONTHS + type + "." + i, ""));
						schedule.setMonthsToPlay(months);
						boolean[] days = PlaySchedule.stringToArray(Prefs.prefs.get(Prefs.SPOTLIST_ENTRY_SCHEDULE_DAYS + type + "." + i, ""));
						schedule.setDaysToPlay(days);
						boolean[] weekdays = PlaySchedule.stringToArray(Prefs.prefs.get(Prefs.SPOTLIST_ENTRY_SCHEDULE_WEEKDAYS + type + "." + i, ""));
						schedule.setWeekdaysToPlay(weekdays);
						int hour = Prefs.prefs.getInt(Prefs.SPOTLIST_ENTRY_SCHEDULE_HOUR + type + "." + i, 0);
						int minute = Prefs.prefs.getInt(Prefs.SPOTLIST_ENTRY_SCHEDULE_MINUTE + type + "." + i, 0);
						schedule.setTimeToPlay(hour, minute);
						spot.setSchedule(schedule);
					}
					spotList.add(spot);
					Util.get().out("Initializing: Added item " + i + " to spotContainer/player", Util.VERBOSITY_DEBUG_INFO);
				} else {
					ignoredOneOrMoreSpots = true;
					Util.get().out("Initializing: Ignored item " + i + " -- file I/O error.", Util.VERBOSITY_WARNING);
				}
			}
			if (ignoredOneOrMoreSpots) {
				saveAllSpotsToPrefs();
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
	
	public int[] removeAllSpotsContaining(SpotEntry spot) {
		/**
		 *  Removes all spots in list, that point to the same file as 'spot'
		 *  I.e. if a spot has another name than 'spot' it will still be
		 *  removed if it points to the same file as 'spot'
		 */
		Vector<Integer> removedSpotsIndices = new Vector<Integer>();
		
		for (int i = spotList.size() - 1; i >= 0; i--) { // traverse spots backwards, as we very well might remove spots along the way
			if (spot.pointsToSameFileAs(spotList.get(i))) {
				spotList.remove(i);
				removedSpotsIndices.add(i);
			}
		}
		saveAllSpotsToPrefs();
		return Util.get().IntegerVectorToIntArray(removedSpotsIndices);
	}
	
	private void saveAllSpotsToPrefs() {
		if (type != TYPE_TEMPORARY) {
			for (int i = 0; i < spotList.size(); i++) {
				saveSpotToPrefs(i, spotList.get(i));
			}
			if (spotList.size() == 0) {
				Prefs.prefs.putInt(Prefs.SPOTLIST_SIZE + type, spotList.size());
			}
		}
	}
	
	private void saveSpotToPrefs(int position, SpotEntry spot) {
		/**
		 * To be called after spot is added to container.
		 * (to ensure correct spotlist size is saved)
		 * Overwrites if position exists already
		 */
		if (type != TYPE_TEMPORARY) {
			Prefs.prefs.putInt(Prefs.SPOTLIST_SIZE + type, spotList.size());
			Prefs.prefs.put(Prefs.SPOTLIST_ENTRY_NAME + type + "." + position, spot.getName());
			Prefs.prefs.put(Prefs.SPOTLIST_ENTRY_FILENAME + type + "." + position, spot.getFile().getName());
			Prefs.prefs.putBoolean(Prefs.SPOTLIST_ENTRY_HASSCHEDULE + type + "." + position, spot.hasSchedule());
			if (spot.hasSchedule()) {
				Prefs.prefs.put(Prefs.SPOTLIST_ENTRY_SCHEDULE_MONTHS + type + "." + position, PlaySchedule.arrayToString(spot.getSchedule().getMonthsToPlay()));
				Prefs.prefs.put(Prefs.SPOTLIST_ENTRY_SCHEDULE_DAYS + type + "." + position, PlaySchedule.arrayToString(spot.getSchedule().getDaysToPlay()));
				Prefs.prefs.put(Prefs.SPOTLIST_ENTRY_SCHEDULE_WEEKDAYS + type + "." + position, PlaySchedule.arrayToString(spot.getSchedule().getWeekdaysToPlay()));
				Prefs.prefs.putInt(Prefs.SPOTLIST_ENTRY_SCHEDULE_HOUR + type + "." + position, spot.getSchedule().getHourToPlay());
				Prefs.prefs.putInt(Prefs.SPOTLIST_ENTRY_SCHEDULE_MINUTE + type + "." + position, spot.getSchedule().getMinuteToPlay());
			}
		}
		Util.get().out("Saved spot to pref file. Type: " + type + ", pos: " + position + ", name: \"" + spot.getName() + "\", hasSchedule: " + spot.hasSchedule(), Util.VERBOSITY_DEBUG_INFO);
	}
	
	public SpotEntry getSpotAt(int index) {
		if (index < 0 || index >= spotList.size())
			return null;
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
	
	public void setNewScheduleForSpot(int index, PlaySchedule schedule) {
	    spotList.get(index).setSchedule(schedule);
	    if (type != TYPE_TEMPORARY)
	        saveSpotToPrefs(index, spotList.get(index));
	}
	
	public int numberOfSpots() {
		return spotList.size();
	}
	
	public Vector<SpotEntry> getDataCopy() {
	    // Returns a new vector with references to the same SpotEntry instances as this SpotContainer.
		Vector<SpotEntry> data = new Vector<SpotEntry>();
		
		for (int i = 0; i < spotList.size(); i++) {
			data.add(spotList.get(i));
		}
		
		return data;
	}
}
