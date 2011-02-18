package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
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
	    // Returns the locale which is saved in the preferences file,
	    // or null if none is saved.
		String lang = Prefs.prefs.get(Prefs.LOCALE_LANGUAGE, Prefs.LOCALE_LANGUAGE_DEFAULT);
		String coun = Prefs.prefs.get(Prefs.LOCALE_COUNTRY, Prefs.LOCALE_COUNTRY_DEFAULT);
		
		if (savedLocale == null && lang != null && coun != null) {
			savedLocale = new Locale(lang, coun);
			Locale.setDefault(savedLocale);
		}
		return savedLocale;
	}
	
	private Locale currentLocale;
	
	public Locale getCurrentLocale() {
	    // Returns the currently used locale, i.e.:
	    // - the one which is saved in the preferences file, if exists, otherwise:
	    // - the system's default, if supported, otherwise:
	    // - the first mentioned locale in the list of supported locales.
	    if (currentLocale != null)
	        return currentLocale;
	    
		currentLocale = getSavedLocale();
		if (currentLocale == null) {
			currentLocale = Locale.getDefault();
            Util.get().out("No locale is saved. Getting system's default locale, " + currentLocale.getDisplayName(), Util.VERBOSITY_DEBUG_INFO);
			if (!isLocaleSupported(currentLocale)) {
	            currentLocale = getSupportedLocales()[0];
                Util.get().out("The system's default locale is not supported. Getting program's default locale, " + currentLocale.getDisplayName(), Util.VERBOSITY_DEBUG_INFO);
			}
		}
		return currentLocale;
	}
	
	public boolean isLocaleSupported(Locale candidate) {
	    Locale[] supportedLocales = getSupportedLocales();
	    for (int i = 0; i < supportedLocales.length; i++) {
	        if (supportedLocales[i].equals(candidate))
	            return true;
	    }
	    return false;
	}
	
	public String string(String key) {
		ResourceBundle bundle = ResourceBundle.getBundle("strings", getCurrentLocale());
		return bundle.getString(key);
	}
	
	private Locale[] supportedLocales;
	
	public Locale[] getSupportedLocales() {
	    // If the file has already been read, no need to read it again
	    // since this method is called at least twice for every string
	    // in the program.
	    if (supportedLocales != null)
	        return supportedLocales;
	    
		File localeListFile = new File(MainFrame.class.getResource("../strings.list").getFile());
		BufferedReader reader;
		Vector<String> localeStrings = new Vector<String>();
		
		try {
			reader = new BufferedReader(new FileReader(localeListFile));
			String readLine;
			while ((readLine = reader.readLine()) != null) {
			    readLine = readLine.trim();
			    if (!readLine.equals("") && readLine.charAt(0) != '#')
			        localeStrings.add(readLine.trim());
			}
			reader.close();
		} catch (IOException ioe) {
			out(ioe.toString(), VERBOSITY_ERROR);
			return new Locale[0];
		}
		
		supportedLocales = new Locale[localeStrings.size()];
		for (int i = 0; i < supportedLocales.length; i++) {
			String[] currentLocale = localeStrings.get(i).split("_");
			if (currentLocale.length != 2) {
				out("At least one line in the strings.list file is incorrect. The file must contain a list of locales, one locale per line, in the form ll_CC, where ll is a two-letter language code and CC is a two-letter country code. Ignoring all locales.", VERBOSITY_ERROR);
				return new Locale[0];
			}
			supportedLocales[i] = new Locale(currentLocale[0], currentLocale[1]);
		}
		return supportedLocales;
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
			Calendar now = Calendar.getInstance();
			int hour = now.get(Calendar.HOUR_OF_DAY);
			int minute = now.get(Calendar.MINUTE);
			int second = now.get(Calendar.SECOND);
			    // (minutes < 10 ? "0" + minutes : minutes)
			String time = "[" + (hour < 10 ? "0" + hour : hour) + ":" +
			        (minute < 10 ? "0" + minute : minute) + ":" +
			        (second < 10 ? "0" + second : second) + "] ";
			if (verbosityLevel <= 1)
				System.err.print(time + text);
			else
				System.out.print(time + text);
		}
	}
	
	public void threadSleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch(Exception e) {
			Util.get().out(e.toString(), Util.VERBOSITY_WARNING);
		}
	}
	
	public String wordWrap(String input, int width) {
	    String[] inputLines = input.split("\n");
	    StringBuffer output = new StringBuffer();
	    for (int i = 0; i < inputLines.length; i++) {
	        if (i != 0)
	            output.append("\n");
	        output.append(wordWrapOneLine(inputLines[i], width));
	    }
	    return output.toString();
	}
	
	private String wordWrapOneLine(String input, int width) {
	    input = input.trim();
	    if (input.length() <= width) {
	        return input;
	    } else {
	        int lastSpaceIndex = input.lastIndexOf(" ", width);
	        if (lastSpaceIndex == -1)
	            lastSpaceIndex = width;
	        
	        String output1 = input.substring(0, lastSpaceIndex).trim() + "\n";
	        String output2 = input.substring(lastSpaceIndex).trim();
	        input = null;
	        return output1 + wordWrapOneLine(output2, width);
	    }
	}
}
