package main;

import java.text.DateFormat;
import java.util.Calendar;

public class PlaySchedule {
	private boolean[] months;
	private boolean[] days;
	private boolean[] weekdays;
	private int hour;
	private int minute;
	private Calendar lastPlayIncident;
	
	public PlaySchedule() {
		months = new boolean[12];
		for (int i = 0; i < months.length; i++)
			months[i] = true;
		
		days = new boolean[31];
		for (int i = 0; i < days.length; i++)
			days[i] = true;
		
		weekdays = new boolean[7];
		for (int i = 0; i < weekdays.length; i++)
			weekdays[i] = true;
		
		hour = 0;
		minute = 0;
		lastPlayIncident = null;
	}
	
	public void setMonthsToPlay(boolean[] months) {
		/** 
		 * 0 = January
		 * ...
		 * 11 = December
		 */
		if (months.length == this.months.length) {
			this.months = months;
		} else {
			Util.get().out("PlaySchedule: Trying to set months <> " + this.months.length + ". Ignoring.", Util.VERBOSITY_ERROR);
		}
	}
	
	public void setDaysToPlay(boolean[] days) {
		/**
		 * 0 = 1st day of month
		 * ...
		 * 30 = 31st day of month
		 */
		if (days.length == this.days.length) {
			this.days = days;
		} else {
			Util.get().out("PlaySchedule: Trying to set days <> " + this.days.length + ". Ignoring.", Util.VERBOSITY_ERROR);
		}
	}
	
	public void setWeekdaysToPlay(boolean[] weekdays) {
		/**
		 * 0 = Sunday
		 * 1 = Monday
		 * ...
		 * 6 = Saturday
		 */
		if (weekdays.length == this.weekdays.length) {
			this.weekdays = weekdays;
		} else {
			Util.get().out("PlaySchedule: Trying to set weekdays <> " + this.weekdays.length + ". Ignoring.", Util.VERBOSITY_ERROR);
		}
	}
	
	public void setTimeToPlay(int hour, int minute) {
		if (hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59) {
			this.hour = hour;
			this.minute = minute;
		}
	}
	
	public boolean[] getMonthsToPlay() {
		return months;
	}
	
	public boolean[] getDaysToPlay() {
		return days;
	}
	
	public boolean[] getWeekdaysToPlay() {
		return weekdays;
	}
	
	public int getHourToPlay() {
		return hour;
	}
	
	public int getMinuteToPlay() {
		return minute;
	}
	
	public String getTimeFormattedShortString() {
	    Calendar calendarInstance = Calendar.getInstance();
	    calendarInstance.set(Calendar.HOUR_OF_DAY, hour);
	    calendarInstance.set(Calendar.MINUTE, minute);
	    
	    DateFormat timeFormatter = DateFormat.getTimeInstance(DateFormat.SHORT, Util.get().getCurrentLocale());
	    return timeFormatter.format(calendarInstance.getTime());
	}
	
	public String getTimeFormattedShortStringWithNotAllDaysIndication() {
	    String str = getTimeFormattedShortString();
	    if (!playsOnAllDays())
            str = str + " ¤";
	    return str;
	}
	
	public boolean playsOnAllDays() {
	    boolean allMonths = true;
	    for (int i = 0; i < months.length; i++)
	        allMonths = allMonths & months[i];
	    
	    boolean allDays = true;
	    for (int i = 0; i < days.length; i++)
	        allDays = allDays & days[i];
	    
	    boolean allWeekdays = true;
	    for (int i = 0; i < weekdays.length; i++)
	        allWeekdays = allWeekdays & weekdays[i];
	    
	    return allMonths && allDays && allWeekdays;
	}
	
	public boolean shouldPlayWithinCurrentMinute() {
		Calendar now = Calendar.getInstance();
		Util.get().out("Checking (current value/spot result):\n" +
				"Month:   " + now.get(Calendar.MONTH) + "/" + months[now.get(Calendar.MONTH)] + "\n" +
				"Day:     " + (now.get(Calendar.DAY_OF_MONTH)-1) + "/" + days[now.get(Calendar.DAY_OF_MONTH)-1] + "\n" +
				"Weekday: " + (now.get(Calendar.DAY_OF_WEEK)-1) + "/" + weekdays[now.get(Calendar.DAY_OF_WEEK)-1] + "\n" +
				"Time:    " + now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE) + "/" + hour + ":" + minute
				,
				Util.VERBOSITY_DETAILED_DEBUG_INFO);
		return (
				months[now.get(Calendar.MONTH)] &&
				days[now.get(Calendar.DAY_OF_MONTH)-1] &&
				weekdays[now.get(Calendar.DAY_OF_WEEK)-1] &&
				hour == now.get(Calendar.HOUR_OF_DAY) &&
				minute == now.get(Calendar.MINUTE)
		);
	}
	
	public void setLastPlayIncidentNow() {
		lastPlayIncident = Calendar.getInstance();
	}
	
	public boolean hasPlayedWithinPastMinute() {
		Calendar now = Calendar.getInstance();
		Calendar oneMinuteAgo = Calendar.getInstance(); // create Calendar instance (time unimportant)
		oneMinuteAgo.setTimeInMillis(now.getTimeInMillis()); // make it a copy of now instance
		oneMinuteAgo.add(Calendar.MINUTE, -1); // subtract one minute
		
		if (lastPlayIncident == null)
			return false;
		else
			return (lastPlayIncident.after(oneMinuteAgo) && lastPlayIncident.before(now));
	}
	
	public PlaySchedule duplicate() {
	    PlaySchedule dup = new PlaySchedule();
	    dup.months = this.months;
	    dup.days = this.days;
	    dup.weekdays = this.weekdays;
	    dup.hour = this.hour;
	    dup.minute = this.minute;
	    dup.lastPlayIncident = this.lastPlayIncident;
	    return dup;
	}
	
	public static String arrayToString(boolean[] scheduleArray) {
		StringBuffer str = new StringBuffer();
		for (int i = 0; i < scheduleArray.length; i++) {
			str.append(scheduleArray[i] ? "y" : "n");
		}
		return str.toString();
	}
	
	public static boolean[] stringToArray(String scheduleString) {
		boolean[] arr = new boolean[scheduleString.length()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = scheduleString.substring(i, i+1).equals("y");
		}
		return arr;
	}
}
