package main;

public class Calculate {
	public static String millisToMinsSecsString(long milliSeconds) {
		long totalMinutes = milliSeconds / 1000;
		int hours = (int)(totalMinutes / 60);
		int minutes = (int)(totalMinutes % 60);
		return hours + ":" + (minutes < 10 ? "0" + minutes : minutes);
	}
}
