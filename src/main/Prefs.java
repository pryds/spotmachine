package main;

import java.util.prefs.Preferences;

public class Prefs {
	public static final Preferences prefs = Preferences.userRoot().node("SpotMachine");
	
	public static final String MILLIS_BETWEEN_SPOTS = "MillisBetweenSpots";
	public static final long   MILLIS_BETWEEN_SPOTS_DEFAULT = 300000; // 5 mins, i.e 300000 millis
	
	public static final String SPOTLIST_SIZE = "SpotListSize";
	public static final int    SPOTLIST_SIZE_DEFAULT = 0;
	
	public static final String SPOTLIST_ENTRY_NAME = "SpotListEntryName";
	public static final String SPOTLIST_ENTRY_FILENAME = "SpotListEntryFilename";
	
	public static final String NEXT_SPOT_TO_PLAY = "NextSpotToPlay";
	public static final int    NEXT_SPOT_TO_PLAY_DEFAULT = 0;
	
	public static final String  REPEAT_ALL = "RepeatAll";
	public static final boolean REPEAT_ALL_DEFAULT = true;
	
}
