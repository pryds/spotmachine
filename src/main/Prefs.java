package main;

import java.util.prefs.Preferences;

public class Prefs {
	public static final Preferences prefs = Preferences.userRoot().node("SpotMachine");
	
	public static final String MILLIS_BETWEEN_SPOTS = "MillisBetweenSpots";
	public static final long   MILLIS_BETWEEN_SPOTS_DEFAULT = 300000; // 5 mins, i.e 300000 millis
	
	public static final String MILLIS_BETWEEN_PROBING_FOR_PLAYABLE_SCHEDULED_SPOTS = "MillisBetweenProbingForPlayableScheduledSpots";
	public static final long   MILLIS_BETWEEN_PROBING_FOR_PLAYABLE_SCHEDULED_SPOTS_DEFAULT = 10000; // Should not be more than 60000, i.e. one minute
	
	public static final String SPOTLIST_SIZE = "SpotListSize";
	public static final int    SPOTLIST_SIZE_DEFAULT = 0;
	
	public static final String SPOTLIST_ENTRY_NAME = "SpotListEntryName";
	public static final String SPOTLIST_ENTRY_FILENAME = "SpotListEntryFilename";
	public static final String SPOTLIST_ENTRY_HASSCHEDULE = "SpotListEntryHasSchedule";
	public static final boolean SPOTLIST_ENTRY_HASSCHEDULE_DEFAULT = false;
	public static final String SPOTLIST_ENTRY_SCHEDULE_MONTHS = "SpotListEntryScheduleMonths";
	public static final String SPOTLIST_ENTRY_SCHEDULE_DAYS = "SpotListEntryScheduleDays";
	public static final String SPOTLIST_ENTRY_SCHEDULE_WEEKDAYS = "SpotListEntryScheduleWeekdays";
	public static final String SPOTLIST_ENTRY_SCHEDULE_HOUR = "SpotListEntryScheduleHour";
	public static final String SPOTLIST_ENTRY_SCHEDULE_MINUTE = "SpotListEntryScheduleMinute";
	
	public static final String NEXT_SPOT_TO_PLAY = "NextSpotToPlay";
	public static final int    NEXT_SPOT_TO_PLAY_DEFAULT = 0;
	
	public static final String  REPEAT_ALL = "RepeatAll";
	public static final boolean REPEAT_ALL_DEFAULT = true;
	
	public static final String DATA_DIR = "DataDir";
	
	public static final String LOCALE_COUNTRY = "LocaleCountry";
	public static final String LOCALE_COUNTRY_DEFAULT = null;
	
	public static final String LOCALE_LANGUAGE = "LocaleLanguage";
	public static final String LOCALE_LANGUAGE_DEFAULT = null;
	
	public static final String  RECORDING_DO_DC_OFFSET_REMOVAL = "RecordingDoDCOffsetRemoval";
	public static final boolean RECORDING_DO_DC_OFFSET_REMOVAL_DEFAULT = true;
	
	public static final String  RECORDING_DO_FADEIN_FADEOUT = "RecordingDoFadeinFadeout";
	public static final boolean RECORDING_DO_FADEIN_FADEOUT_DEFAULT = true;
	
	public static final String  RECORDING_DO_VOLUME_NORMALIZATION = "RecordingDoVolumeNormalization";
	public static final boolean RECORDING_DO_VOLUME_NORMALIZATION_DEFAULT = true;
	
	public static final String FORCE_PLAY_ON_MIXER_NUMBER = "ForcePlayOnMixerNumber";
	public static final int    FORCE_PLAY_ON_MIXER_NUMBER_DEFAULT = -1; // -1 = no force, use system default
	
	public static final String FORCE_RECORDING_ON_MIXER_NUMBER = "ForceRecordingOnMixerNumber";
	public static final int    FORCE_RECORDING_ON_MIXER_NUMBER_DEFAULT = -1; // -1 = no force, use system default
	
}
