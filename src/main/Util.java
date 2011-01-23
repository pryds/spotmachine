package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Vector;

import gui.MainFrame;

import javax.swing.ImageIcon;

public class Util {
	
	private Util() {
	}
	private static class UtilSingletonHolder { 
		public static final Util INSTANCE = new Util();
	}
	public static Util get() {
		return UtilSingletonHolder.INSTANCE;
	}
	
	public static final int VERBOSITY_ERROR = 0;
	public static final int VERBOSITY_WARNING = 1;
	public static final int VERBOSITY_DEBUG_INFO = 2;
	public static final int VERBOSITY_DETAILED_DEBUG_INFO = 3;
	
	public String millisToMinsSecsString(long milliSeconds) {
		long totalMinutes = milliSeconds / 1000;
		int hours = (int)(totalMinutes / 60);
		int minutes = (int)(totalMinutes % 60);
		return hours + ":" + (minutes < 10 ? "0" + minutes : minutes);
	}
	
	public ImageIcon createImageIcon(String relativePath) {
		URL url = MainFrame.class.getResource(relativePath);
		if (url == null) {
			out("Resource not found: " + relativePath, VERBOSITY_ERROR);
			System.exit(1);
		}
		return new ImageIcon(url);
	}
	
	public String createLowerCaseRandomWAVFilename() {
		Random rand = new Random();
		StringBuffer str = new StringBuffer();
		int nameLength = 16;
		int firstChar = 97; // 'a'
		int lastChar = 122; // 'z'
		
		for (int i = 0; i < nameLength; i++) {
			str.append((char)(rand.nextInt(lastChar - firstChar + 1) + firstChar));
		}
		str.append(".wav");
		return str.toString();
	}
	
	public File createUniqueLowerCaseRandomWAVFileInDataDir() {
		File outFile = null;
		while (outFile == null || outFile.exists()) { // make sure file doesn't already exist (though unlikely)
			outFile = new File(getDataStoreDir(), createLowerCaseRandomWAVFilename());
		}
		return outFile;
	}
	
	public void deleteFile(File file) {
		if (file.exists() && !file.isDirectory() && file.canWrite()) {
			file.delete();
			out("Deleting file " + file.getName(), VERBOSITY_DEBUG_INFO);
		}
	}
	
	private File dataDir = null;
	
	public File getDataStoreDir() {
		// If we already figured out the data store dir in this session, no need to do so again
		if (dataDir != null)
			return dataDir;
		
		// Get dir from preferences, if exists and is still valid
		String prefsDirStr = Prefs.prefs.get(Prefs.DATA_DIR, null);
		if (prefsDirStr != null) {
			File prefsDir = new File(prefsDirStr);
			if (prefsDir != null && prefsDir.exists() && prefsDir.isDirectory() && prefsDir.canWrite()) {
				dataDir = prefsDir;
				return dataDir;
			}
		}
		
		// Figure out where to store data
		File appDataDir = null;
		String sysName = System.getProperty("os.name").toLowerCase();
		if (sysName.contains("windows")) { // Windows special case
			String winDirStr = System.getenv("APPDATA");
			if (winDirStr != null) {
				File winDir = new File(winDirStr);
				if (winDir != null && winDir.exists() && winDir.isDirectory() && winDir.canWrite()) {
					appDataDir = winDir;
				}
			}
		}
		
		if (appDataDir == null) { // default: the current user's home directory
			String homeDirStr = System.getProperty("user.home");
			if (homeDirStr != null) {
				File homeDir = new File(homeDirStr);
				if (homeDir != null && homeDir.exists() && homeDir.isDirectory() && homeDir.canWrite()) {
					appDataDir = homeDir;
				}
			}
		}
		
		if (appDataDir == null) {
			return null;
		} else { // by now we have a writable dir in which we can make our own dir or use it if it already exists
			File saveDir = new File(appDataDir, ".spotmachine");
			if (!saveDir.exists()) {
				out("Creating data directory " + saveDir.getAbsolutePath(), VERBOSITY_DEBUG_INFO);
				saveDir.mkdir();
			}
			Prefs.prefs.put(Prefs.DATA_DIR, saveDir.getAbsolutePath());
			return saveDir;
		}
	}
	
	public int[] IntegerVectorToIntArray(Vector<Integer> source) {
		int[] target = new int[source.size()];
		for (int i = 0; i < source.size(); i++) {
			target[i] = source.get(i).intValue();
		}
		return target;
	}
	
	public void reverseSort(int[] intArray) {
		Arrays.sort(intArray);
		for(int i = 0; i < intArray.length / 2; i++) {
		     int temp = intArray[i];
		     intArray[i] = intArray[intArray.length - (i + 1)];
		     intArray[intArray.length - (i + 1)] = temp;
		}
	}
	
	private Locale savedLocale;
	
	public Locale getSavedLocale() {
		String lang = Prefs.prefs.get(Prefs.LOCALE_LANGUAGE, Prefs.LOCALE_LANGUAGE_DEFAULT);
		String coun = Prefs.prefs.get(Prefs.LOCALE_COUNTRY, Prefs.LOCALE_COUNTRY_DEFAULT);
		
		if (savedLocale == null && lang != null && coun != null) {
			savedLocale = new Locale(lang, coun);
			Locale.setDefault(savedLocale);
		}
		return savedLocale;
	}
	
	public Locale getCurrentLocale() {
		Locale locale = getSavedLocale();
		if (locale == null) {
			locale = Locale.getDefault();
		}
		return locale;
	}
	
	public String string(String key) {
		ResourceBundle bundle = ResourceBundle.getBundle("strings", getCurrentLocale());
		return bundle.getString(key);
	}
	
	public Locale[] getAvailableLocales() {
		File localeListFile = new File(MainFrame.class.getResource("../strings.list").getFile());
		BufferedReader reader;
		Vector<String> localeStrings = new Vector<String>();
		
		try {
			reader = new BufferedReader(new FileReader(localeListFile));
			String readLine;
			while ((readLine = reader.readLine()) != null && !readLine.trim().equals("")) {
				localeStrings.add(readLine.trim());
			}
			reader.close();
		} catch (IOException ioe) {
			out(ioe.toString(), VERBOSITY_ERROR);
			return new Locale[0];
		}
		
		Locale[] locales = new Locale[localeStrings.size()];
		for (int i = 0; i < locales.length; i++) {
			String[] currentLocale = localeStrings.get(i).split("_");
			if (currentLocale.length != 2) {
				out("At least one line in the strings.list file is incorrect. The file must contain a list of locales, one locale per line, in the form ll_CC, where ll is a two-letter language code and CC is a two-letter country code. Ignoring all locales.", VERBOSITY_ERROR);
				return new Locale[0];
			}
			locales[i] = new Locale(currentLocale[0], currentLocale[1]);
		}
		return locales;
	}
	
	public void out(String text, int verbosityLevel) {
		outNoLN(text + "\n", verbosityLevel);
	}
	
	public void outNoLN(String text, int verbosityLevel) {
		/**
		 * verbosityLevel
		 * 0 = Error
		 * 1 = Warning
		 * 2 = Debug info
		 * 3 = Detailed debug info
		 */
		
		if (verbosityLevel <= SpotMachine.currentVerbosityLevel) {
			if (verbosityLevel <= 1)
				System.err.print(text);
			else
				System.out.print(text);
		}
	}
}
